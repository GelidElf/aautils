package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProcessMTableForQLearning {

	private static Clustering clusteringEstados;
	private static Clustering clusteringDecisiones;
	private static final String NOMBRE_ARCHIVO_CENTROIDES_ESTADOS = "centroidesEstado.txt";
	private static final String NOMBRE_ARCHIVO_CENTROIDES_DECISIONES = "centroidesDecisiones.txt";
	private static final String SPLIT_EXPRESSION = "\\s+";
	private static final String COMMENT_EXPRESSION = "#";


	/**
	 * Reads files, accepts wildcards or lists of files
	 * @param 
	 */
	public static void main(String[] args) {

		ArrayList<String> ficherosEntrada = new ArrayList<String>();

		if (args.length < 1) {
			System.out.println("Se ha de invocar con al menos un parametro, el nombre de un fichero o nombres con un wildcard");
			return;
		}

		for (String entrada : args) {
			if (tieneWildCard(entrada)) {
				ficherosEntrada.addAll(ficherosDelFiltro(entrada));
			} else {
				ficherosEntrada.add(entrada);
			}
		}
		
		clusteringEstados = new Clustering(NOMBRE_ARCHIVO_CENTROIDES_ESTADOS,SPLIT_EXPRESSION);
		clusteringDecisiones = new Clustering(NOMBRE_ARCHIVO_CENTROIDES_DECISIONES,SPLIT_EXPRESSION);
		
		File archivoSalida = createOutputFile();
		try {
			processAndWriteMtable(archivoSalida, ficherosEntrada);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	

	private static void processAndWriteMtable(File archivoSalida, ArrayList<String> ficherosEntrada) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(archivoSalida));
		writeSourceFilesinformation(archivoSalida, ficherosEntrada, writer);
		for (String nombreFichero : ficherosEntrada) {
			processDataSectionAndWriteToOutput(writer, nombreFichero);
		}
		writer.close();
	}

	private static void writeSourceFilesinformation(File archivoSalida,
			ArrayList<String> ficherosEntrada, BufferedWriter writer)
			throws IOException {
		StringBuilder stringSalida = new StringBuilder();
		stringSalida.append(COMMENT_EXPRESSION+"Creating file for output: "+archivoSalida.getName());
		stringSalida.append("\n");
		stringSalida.append(COMMENT_EXPRESSION+"Using: ");
		for (String nombreFichero : ficherosEntrada) {
			stringSalida.append(nombreFichero);
			stringSalida.append(",");
		}
		stringSalida.setLength(stringSalida.length()-1);
		System.out.println(stringSalida.toString());
		writer.write(stringSalida.toString());
		writer.newLine();
	}

	private static void processDataSectionAndWriteToOutput(BufferedWriter writer, String nombreFichero) throws IOException {
		writer.write(COMMENT_EXPRESSION + nombreFichero + " section");
		writer.newLine();
		BufferedReader in = new BufferedReader(new FileReader(nombreFichero));
		String str = null;
		boolean foundDataSection = false;
		LinkedHashMap<String, Integer> attributes = new LinkedHashMap<String, Integer>();
		List<InstanciaTablaM> datosArchivo = new ArrayList<ProcessMTableForQLearning.InstanciaTablaM>();
		while ((str = in.readLine()) != null) {
			
			if (foundDataSection) {
				datosArchivo.add(processLineAndWriteCValues(str,attributes));
			} else {
				String[] words = str.trim().split(" ");
				if ("@Attribute".equalsIgnoreCase(words[0])) {
					attributes.put(words[1], attributes.size());
				}
			}
			if ("@DATA".equalsIgnoreCase(str.trim())) {
				foundDataSection = true;
			}
		}
		for (int i = 0; i < datosArchivo.size()-1; i++) {
			datosArchivo.get(i).setClusterEstadoSiguiente(datosArchivo.get(i+1).getClusterEstado());
			writer.write(datosArchivo.get(i).toString());
			writer.newLine();
		}
	}

	private static InstanciaTablaM processLineAndWriteCValues(String str, LinkedHashMap<String, Integer> attributes) {
		String[] valoresDatos = str.split(",");
		Map<String, Double> fieldsAndValues = new HashMap<String, Double>();
		for(String attribute:attributes.keySet()){
			fieldsAndValues.put(attribute,Double.parseDouble(valoresDatos[attributes.get(attribute)]));
		}
		int clusterEstado = clusteringEstados.getClusterAssignment(fieldsAndValues);
		int clusterDecisiones = clusteringDecisiones.getClusterAssignment(fieldsAndValues);
		double ranking = Double.parseDouble(valoresDatos[attributes.get("ranking")]);
		return new InstanciaTablaM(clusterEstado,clusterDecisiones,ranking);
	}

	private static File createOutputFile() {
		String nombreArchivoSalida = "Mtable" + System.currentTimeMillis() + ".txt";
		File archivoSalida = new File(nombreArchivoSalida);
		return archivoSalida;
	}

	private static Collection<? extends String> ficherosDelFiltro(String entrada) {
		FilenameFilter filter = new WildcardedFileNameFilter(entrada);
		String executionDirectory = System.getProperty("user.dir");
		File currentDirectory = new File(executionDirectory);
		String[] aceptedFilesList = currentDirectory.list(filter);
		return Arrays.asList(aceptedFilesList);
	}

	public static class WildcardedFileNameFilter implements FilenameFilter {

		private String regex;

		public WildcardedFileNameFilter(String filter) {
			regex = "^" + filter.replace(".", "\\.").replace("?", ".?").replace("*", ".*?") + "$";
		}

		@Override
		public boolean accept(File dir, String name) {
			return name.matches(regex);
		}
	};

	private static boolean tieneWildCard(String entrada) {
		return entrada.contains("*");
	}

	
	public static class Clustering{

		private HashMap<String, List<Float>> centroids;
		private int numberOfClusters = -1;
		
		public Clustering(String nombreArchivoCentroides, String caracterSeparador){
			centroids = new HashMap<String, List<Float>>();
			cargarCentroidesEstados(nombreArchivoCentroides,caracterSeparador);
		}

		private int cargarCentroidesEstados(String nombreArchivoCentroides, String splitCharacter){
			try {
				BufferedReader in = new BufferedReader(new FileReader(nombreArchivoCentroides));
				String str = null;
				while ((str = in.readLine()) != null) {
					str = str.trim();
					if (!lineIsAComment(str) && !lineIsEmpty(str)){
						String[] words = str.split(splitCharacter);
						numberOfClusters = words.length - 1; // TODO: Check if all clusters have a centroid for all decisions!!!
						String key = words[0];
						List<Float> centroidValues = new ArrayList<Float>();
						for (int i = 1; i < words.length; i++) {
							centroidValues.add(new Float(words[i]));
						}
						centroids.put(key, centroidValues);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Cargados centroides del archivo "+nombreArchivoCentroides);
			return numberOfClusters;
		}

		private boolean lineIsEmpty(String str) {
			return str.length() == 0;
		}

		private boolean lineIsAComment(String str) {
			return str.startsWith(COMMENT_EXPRESSION);
		}

		public int getClusterAssignment(Map<String, Double> fieldsAndValues){
			
			double[] distancesToClusters = getDistancesToClusters(fieldsAndValues);
			return calcularDistanciaMenor(distancesToClusters);
			
		}
		
		private double[] getDistancesToClusters(Map<String, Double> fieldsAndValues) {
			double[] sumaDeCuadrados = new double[numberOfClusters];
			for (int i = 0; i < numberOfClusters; i++) {
				sumaDeCuadrados[i] = 0;
			}
			double[] distanciasACentroides = new double[numberOfClusters];
			for (String attribute : centroids.keySet()) {
				Double value = fieldsAndValues.get(attribute);
				if (value != null) {
					for (int i = 0; i < numberOfClusters; i++) {
						sumaDeCuadrados[i] += Math.pow(Math.abs(value - centroids.get(attribute).get(i)), 2.0);
					}
				}
			}
			for (int i = 0; i < numberOfClusters; i++) {
				distanciasACentroides[i] = Math.sqrt(sumaDeCuadrados[i]);
				System.out.println("Distancias Euclideas a cluster" + i + ": " + distanciasACentroides[i]);
			}
			return distanciasACentroides;
		}
		
		private int calcularDistanciaMenor(double[] distanciasACentroides) {
			double distanciaMenor = distanciasACentroides[0];
			int posicionMenor = 0;
			for (int i = 1; i < distanciasACentroides.length; i++) {
				if (distanciaMenor > distanciasACentroides[i]) {
					distanciaMenor = distanciasACentroides[i];
					posicionMenor = i;
				}
			}
			return posicionMenor;
		}
		
	}
	
	public static class InstanciaTablaM {
		
		private int clusterEstado;
		private int clusterDecision;
		private double ranking;
		private int clusterEstadoSiguiente;
		
		
		public InstanciaTablaM(int clusterEstado, int clusterDecision,
				double ranking) {
			super();
			this.clusterEstado = clusterEstado;
			this.clusterDecision = clusterDecision;
			this.ranking = ranking;
		}
		
		public int getClusterEstado() {
			return clusterEstado;
		}
		public void setClusterEstado(int clusterEstado) {
			this.clusterEstado = clusterEstado;
		}
		public int getClusterDecision() {
			return clusterDecision;
		}
		public void setClusterDecision(int clusterDecision) {
			this.clusterDecision = clusterDecision;
		}
		public double getRanking() {
			return ranking;
		}
		public void setRanking(double ranking) {
			this.ranking = ranking;
		}
		public int getClusterEstadoSiguiente() {
			return clusterEstadoSiguiente;
		}
		public void setClusterEstadoSiguiente(int clusterEstadoSiguiente) {
			this.clusterEstadoSiguiente = clusterEstadoSiguiente;
		}
		
		@Override
		public String toString() {
			return ""+clusterEstado+","+clusterDecision+","+clusterEstadoSiguiente+","+ranking;
		}
		
		
	}
	
	
}
