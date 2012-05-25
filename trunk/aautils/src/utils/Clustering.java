package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Clustering{

		private static final String COMMENT_EXPRESSION = "#";
		private HashMap<String, List<Double>> centroids;
		private int numberOfClusters = -1;
		private Map<String, Field> fields;
		
		public Clustering(String nombreArchivoCentroides, String caracterSeparador,Class<?> clase){
			centroids = new HashMap<String, List<Double>>();
			cargarCentroidesEstados(nombreArchivoCentroides,caracterSeparador);
			fields = cargaCamposDeCentroidesPara(clase);
		}
		
		public Clustering(String nombreArchivoCentroides, String caracterSeparador){
			centroids = new HashMap<String, List<Double>>();
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
						List<Double> centroidValues = new ArrayList<Double>();
						for (int i = 1; i < words.length; i++) {
							centroidValues.add(new Double(words[i]));
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

		public int getNumberOfClusters() {
			return numberOfClusters;
		}
		
		public int getNumberOfAttributes (){
			return centroids.size();
		}
		
		private Map<String, Field> cargaCamposDeCentroidesPara(Class<?> clase){
			HashMap<String, Field> fields = new HashMap<String, Field>();
			for (String attribute : centroids.keySet()) {
				try {
					for (Field campo : clase.getDeclaredFields()) {
						if (campo.getName().equalsIgnoreCase(attribute)) {
							campo.setAccessible(true);
							fields.put(attribute, campo);
						}
					}
		
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				}
			}
			return fields;
		}

		public void saveAsAlphabet(File outputFile) throws IOException{
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
			List<String> attributes = new ArrayList<String>(centroids.keySet());
			StringBuffer lineaAEscribir = new StringBuffer();
			
			for (int i =0;i<numberOfClusters;i++){
				for (int j = 0; j<centroids.size();j++){
					lineaAEscribir.append(centroids.get(attributes.get(j)).get(i));
					lineaAEscribir.append(" ");
				}
				writer.write(lineaAEscribir.toString());
				writer.newLine();
				lineaAEscribir = new StringBuffer();
			}
			writer.close();
		}
		
		public HashMap<String,Double> getAttributesAndValuesOfCluestering(int cluster){
			HashMap<String,Double> values = new HashMap<String,Double>();
			for (String attribute:centroids.keySet()){
				values.put(attribute, centroids.get(attribute).get(cluster));
			}
			return  values;
		}
		
		
	}