import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgenteClusteringRanking extends Agente {

	/**
	 * Constructor
	 * 
	 * @See {@link Agente}
	 * @param codSimulacion
	 * @param nombreMercado
	 * @param nombreEquipo
	 */
	public AgenteClusteringRanking(String codSimulacion, String nombreMercado, String nombreEquipo) {
		super(codSimulacion, nombreMercado, nombreEquipo);
	}

	/**
	 * Constructor de la clase.
	 * @param String _codSimulacion Codigo de la simulacion donde va a participar el agente.
	 * @param String _nombreMercado Nombre del mercado donde participa el agente.
	 * @param String _nombreEquipo Nombre del equipo cuyas decisiones van a ser tomadas por el agente.
	 * @param String _nombreLogEstados Nombre del fichero donde se almacenan los estados por los que transita el agente.
	 * @param String _nombreLogDecisiones Nombre del fichero donde se almacenan las decisiones tomadas por el agente.
	 */
	public AgenteClusteringRanking(String _codSimulacion, String _nombreMercado, String _nombreEquipo, String _nombreLogEstados, String _nombreLogDecisiones) {
		super(_codSimulacion, _nombreMercado, _nombreEquipo, _nombreLogEstados, _nombreLogDecisiones);
	}

	/**
	 * Constructor de la clase
	 * @param String _codSimulacion Codigo de la simulacion donde va a participar el agente.
	 * @param String _nombreMercado Nombre del mercado donde participa el agente.
	 * @param String _nombreEquipo Nombre del equipo cuyas decisiones van a ser tomadas por el agente.
	 * @param String _nombreLogEstados Nombre del fichero donde se almacenan los estados por los que transita el agente.
	 * @param String _nombreLogDecisiones Nombre del fichero donde se almacenan las decisiones tomadas por el agente.
	 * @param String _nombreLogEstadosDecisiones Nombre del fichero donde se almancenan los estados y las decisiones tomadas por el agente.
	 */
	public AgenteClusteringRanking(String _codSimulacion, String _nombreMercado, String _nombreEquipo, String _nombreLogEstados, String _nombreLogDecisiones, String _nombreLogEstadosDecisiones) {
		super(_codSimulacion, _nombreMercado, _nombreEquipo, _nombreLogEstados, _nombreLogDecisiones, _nombreLogEstadosDecisiones);
	}

	private static final String SPLIT_CHARACTER = ",";
	private static final String NOMBRE_ARCHIVO_CENTROIDES = "kmediasRanking.txt";
	private static final String NOMBRE_ARCHIVO_CLUSTERIZADO = "clusteringRanking.arff";
	private static final String NOMBRE_ARCHIVO_RANKING = "ranking.arff";
	private static Map<String, Field> fields = new HashMap<String, Field>();
	private static Map<String, List<Float>> centroids = new HashMap<String, List<Float>>();
	private static int numberOfClusters = 0;
	private static double[] sumaDeCuadrados = new double[2];
	private static double[] distanciasACentroides = new double[2];

	static {
		try {
			BufferedReader in = new BufferedReader(new FileReader(NOMBRE_ARCHIVO_CENTROIDES));
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
			e.printStackTrace();
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
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		AgenteClusteringRanking agente = new AgenteClusteringRanking("", "", "");
		agente.tomarDecisiones();
	}

	@Override
	public Decisiones tomarDecisiones() {
		List<String[]> instanciaAGuardar = new ArrayList<String[]>();
		getDistancesToClusters();
		String cluster = "cluster" + calcularDistanciaMenor();
		System.out.println("Cluster elegido: " + cluster);
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
						if (words[words.length - 1].equals(cluster)) {
							instanciaAGuardar.add(words);
						}
					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		// ordenar instanciaAGuardar por ranking
		Collections.sort(instanciaAGuardar, new InstanciasComparator());
		// Como se ordena de menor a mayor, cojo la última instancia, que será la que mejor ranking_siguiente tenga
		String[] mejorInstancia = instanciaAGuardar.get(instanciaAGuardar.size() - 1);
		// Cojo el número de la instancia para poder buscarla en el archivo global de ranking
		String instancia = mejorInstancia[0];
		Integer numeroInstancia = Integer.parseInt(instancia);
		// Leo el archivo inicial de ranking para encontrar la instancia que se corresponde con la mejorInstancia y ver sus decisiones
		List<String[]> instanciasRankingArff = new ArrayList<String[]>();
		try {
			BufferedReader in = new BufferedReader(new FileReader(NOMBRE_ARCHIVO_RANKING));
			String str = null;
			boolean ignorar = true;
			while ((str = in.readLine()) != null) {
				if (str.equalsIgnoreCase("@data")) {
					ignorar = false;
				} else {
					if (!ignorar) {
						String[] words = str.trim().split(",");
						instanciasRankingArff.add(words);
					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		//Seleccionar la instancia que se corresponde con mejorInstancia
		String[] mejorInstanciaArff = instanciasRankingArff.get(numeroInstancia);
		//Mirar las decisiones tomadas y asignarlas a decisiones
		Decisiones decisionesNuevas = new Decisiones();
		decisionesNuevas.setCodSimulacion(this.getCodSimulacion());
		decisionesNuevas.setNombreMercado(this.getNombreMercado());
		decisionesNuevas.setNombreEquipo(this.getNombreEquipo());
		decisionesNuevas.setDPrecioEmp(Double.parseDouble(mejorInstanciaArff[135]));
		decisionesNuevas.setDPublicida(Double.parseDouble(mejorInstanciaArff[136]));
		decisionesNuevas.setDPromred(Double.parseDouble(mejorInstanciaArff[137]));
		decisionesNuevas.setDInfComer(Double.parseDouble(mejorInstanciaArff[138]));
		decisionesNuevas.setDAmplRrhh(Double.parseDouble(mejorInstanciaArff[139]));
		decisionesNuevas.setDRedRrhh(Double.parseDouble(mejorInstanciaArff[140]));
		decisionesNuevas.setDFormacion(Double.parseDouble(mejorInstanciaArff[141]));
		decisionesNuevas.setDAmpCap(Double.parseDouble(mejorInstanciaArff[142]));
		decisionesNuevas.setRedCap(Double.parseDouble(mejorInstanciaArff[143]));
		decisionesNuevas.setDProdProg(Double.parseDouble(mejorInstanciaArff[144]));
		decisionesNuevas.setDcompraMP(Double.parseDouble(mejorInstanciaArff[145]));
		decisionesNuevas.setRPrecioCom(Double.parseDouble(mejorInstanciaArff[146]));
		decisionesNuevas.setDid(Double.parseDouble(mejorInstanciaArff[147]));
		decisionesNuevas.setDPrestamo(Double.parseDouble(mejorInstanciaArff[148]));
		decisionesNuevas.setDPlazoPres(Double.parseDouble(mejorInstanciaArff[149]));
		decisionesNuevas.setDActFinco(Double.parseDouble(mejorInstanciaArff[150]));
		decisionesNuevas.setDActFinVta(Double.parseDouble(mejorInstanciaArff[151]));
		decisionesNuevas.setDCompraPla(Double.parseDouble(mejorInstanciaArff[152]));
		decisionesNuevas.setDAccemi(Double.parseDouble(mejorInstanciaArff[153]));
		decisionesNuevas.setDPrecEmi(Double.parseDouble(mejorInstanciaArff[154]));
		decisionesNuevas.setDDivBene(Double.parseDouble(mejorInstanciaArff[155]));
		decisionesNuevas.setDDivRes(Double.parseDouble(mejorInstanciaArff[156]));
		return decisionesNuevas;
	}

	private class InstanciasComparator implements Comparator<String[]> {

		/*
		 * Representa la columna que queremos comparar. Estamos interesados en
		 * ranking_siguiente
		 */
		private int columna = 137;

		@Override
		public int compare(String[] o1, String[] o2) {
			// Coger la columna correcta, transformar en double y comparar
			Double valueOfo1 = Double.parseDouble(o1[columna]);
			Double valueOfo2 = Double.parseDouble(o2[columna]);
			return valueOfo1.compareTo(valueOfo2);
		}

	}

	/*
	 * Método que calcula cuál es la distancia menor del nuevo estado a cada
	 * clúster.
	 */
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
	 * Método que calcula las distancias Euclidianas de los atributos del nuevo
	 * estado a cada clúster.
	 */
	private void getDistancesToClusters() {
		// double result = 0.0;
		double variableConvertida = 0.0;
		for (String attribute : centroids.keySet()) {
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
			System.out.println("Distancias Euclideas a cluster" + i + ": " + distanciasACentroides[i]);
		}
	}

}
