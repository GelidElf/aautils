import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Clustering {

	public static final String SPLIT_CHARACTER = ",";
	private Map<String, List<Float>> centroids = new HashMap<String, List<Float>>();
	private int numberOfClusters = 0;
	private double[] sumaDeCuadrados = new double[2];
	private double[] distanciasACentroides = new double[2];

	public Clustering(String nameOfClusterFile) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(nameOfClusterFile));
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
	}

}
