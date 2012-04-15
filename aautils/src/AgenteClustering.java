import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgenteClustering extends Agente {

	/**
	 * Constructor
	 * @See {@link Agente}
	 * @param codSimulacion
	 * @param nombreMercado
	 * @param nombreEquipo
	 */
	public AgenteClustering(String codSimulacion, String nombreMercado, String nombreEquipo) {
		super(codSimulacion, nombreMercado, nombreEquipo);
	}

	private static final String SPLIT_CHARACTER = ",";
	private static final String NOMBRE_ARCHIVO_CENTROIDES = "kmediasRanking.txt";

	private static Map<String, List<Float>> centroids = new HashMap<String, List<Float>>();
	private static int numberOfClusters = 0;

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

		}
		System.out.println("Cargados centroides");
	}

	public static void main(String[] args) {
		new AgenteClustering("", "", "");
	}

}
