import java.awt.Event;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgenteClustering extends Agente {

	/**
	 * Constructor
	 * 
	 * @See {@link Agente}
	 * @param codSimulacion
	 * @param nombreMercado
	 * @param nombreEquipo
	 */
	public AgenteClustering(String codSimulacion, String nombreMercado,
			String nombreEquipo) {
		super(codSimulacion, nombreMercado, nombreEquipo);
	}

	private static final String SPLIT_CHARACTER = ",";
	private static final String NOMBRE_ARCHIVO_CENTROIDES = "kmediasRanking.txt";
	private static final String NOMBRE_ARCHIVO_CLUSTERIZADO = "clusteringRanking.arff";
	private static Map<String, Field> fields = new HashMap<String, Field>();
	private static Map<String, List<Float>> centroids = new HashMap<String, List<Float>>();
	private static int numberOfClusters = 0;
	private static double[] sumaDeCuadrados = new double[2];
	private static double[] distanciasACentroides = new double[2];

	static {
		try {
			BufferedReader in = new BufferedReader(new FileReader(
					NOMBRE_ARCHIVO_CENTROIDES));
			String str = null;
			while ((str = in.readLine()) != null) {
				String[] words = str.trim().split(SPLIT_CHARACTER);
				numberOfClusters = words.length - 1; // TODO: Check if all clusters have a centroid for all decisions!!!
				String key = words[0];
				List<Float> centroidValues = new ArrayList<Float>();
				for (int i = 1; i < words.length; i++) {
					centroidValues.add(new Float(words[i]));
				}
				centroids.put(key, centroidValues);
			}
		} catch (IOException e) {

		}
		System.out.println("Cargados centroides");
		sumaDeCuadrados = new double[numberOfClusters];
		distanciasACentroides = new double[numberOfClusters];
		
		for (String attribute : centroids.keySet()) {
			try {
				for (Field campo : Estado.class.getDeclaredFields()) {
					if (campo.getName().equalsIgnoreCase(attribute)) {
						campo.setAccessible(true);
						fields.put(attribute, campo);
					}
				}

			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
	}

	public static void main(String[] args) {
		AgenteClustering agente = new AgenteClustering("", "", "");
		agente.tomarDecisiones();
	}

	@Override
	public Decisiones tomarDecisiones() {
		List<String[]> instanciaAGuardar = new ArrayList<String[]>();
		getDistancesToClusters();
		String cluster = "cluster" + calcularDistanciaMenor();
		System.out.println(cluster);
		// Cargar arff de los estados clusterizados
		try {
			BufferedReader in = new BufferedReader(new FileReader(NOMBRE_ARCHIVO_CLUSTERIZADO));
			String str = null;
			boolean ignorar = true;
			while ((str = in.readLine()) != null) {
				if (str.equalsIgnoreCase("@data")) {
					ignorar = false;
				} else {
					if (!ignorar) {
						String[] words = str.trim().split(",");
						// Seleccionar instancias que pertenezcan al cluster
						// "String cluster"
						if (words[words.length - 1].equals(cluster)) {
							instanciaAGuardar.add(words);
						}
					}
				}
			}

		} catch (IOException e) {

		}
		// ordenar instanciaAGuardar por ranking
		Collections.sort(instanciaAGuardar,new InstanciasComparator());
		String[] mejorInstancia = instanciaAGuardar.get(0);
		Decisiones decisionesNuevas = new Decisiones();
		//decisionesNuevas.set
		return null;//super.tomarDecisiones();
	}

	private class InstanciasComparator implements Comparator<String[]>{

		@Override
		public int compare(String[] o1, String[] o2) {
			// coger la columna correcta, transformar en double y comparar
			return o1[2].compareTo(o2[2]);
		}
		
	}
	
	
	private int calcularDistanciaMenor() {
		double distanciaMenor = distanciasACentroides[0];
		int posicionMenor = 0;
		for (int i = 1; i < numberOfClusters; i++) {
			if (distanciaMenor > distanciasACentroides[i]) {
				distanciaMenor = distanciasACentroides[i];
				posicionMenor = i;
			}
		}
		return posicionMenor;
	}

	/*
	 * private String capitalize(String s) { String firstLetter = s.substring(0,
	 * 1); String tail = s.substring(2); return firstLetter.toUpperCase() +
	 * tail; }
	 */

	private void getDistancesToClusters() {
		estado = new Estado();
		// double result = 0.0;
		double variableConvertida = 0.0;
		for (String attribute : centroids.keySet()) {
			// Class<Estado> c = Estado.class;
			// Method getter;
			Object getterResult = null;
			try {
				getterResult = fields.get(attribute).get(estado);
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (getterResult != null) {
				if (getterResult.getClass().equals(double.class)) {
					variableConvertida = (Double) getterResult;
				} else if (getterResult.getClass().equals(int.class)) {
					variableConvertida = (Double) getterResult;
				} else if (getterResult instanceof Double) {
					variableConvertida = (Double) getterResult;
				} else if (getterResult instanceof Integer) {
					variableConvertida = (Integer) getterResult;
				}

				for (int i = 0; i < numberOfClusters; i++) {
					sumaDeCuadrados[i] += Math.pow(Math.abs(variableConvertida - centroids.get(attribute).get(i)), 2.0);
				}
			}
		}
		for (int i = 0; i < numberOfClusters; i++) {
			distanciasACentroides[i] = Math.sqrt(sumaDeCuadrados[i]);
			System.out.println(distanciasACentroides[i]);
		}
	}

}
