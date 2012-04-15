import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AgenteClustering {

	private Map<String, List<Float>> centroids = new HashMap<String, List<Float>>();
	private int numberOfClusters = 0;
	
	private enum readingState {initial,centroidConfigurationReached,centroidConfigurationDataReached,end};
	private readingState currentState = readingState.initial;
	
	private final String CENTROID_CONFIGURATION_START = "Cluster centroids:";
	private boolean ignoreLines = false;
	private String mustContainToStopIgnoring = "";
	
	
	public AgenteClustering (){
	}
	
	private void processConfigurationFile(){
		
		String line = "";
		if (!ignoreLines){
			processLine(line);
		}else{
			if (line.contains(mustContainToStopIgnoring)){
				ignoreLines = false;
			}
		}
		
	}
	
	private void processLine(String line){
		switch (currentState) {
		case initial:
			if (line.trim().startsWith(CENTROID_CONFIGURATION_START))
				currentState = readingState.centroidConfigurationDataReached;
			break;
		case centroidConfigurationDataReached:
			String[] words = line.trim().split(" ");
			String key = words[0];
			List<Float> centroidValues = new ArrayList<Float>();
			if (words.length > 3){
				for (int i = 2; i < words.length; i++) {
					centroidValues.add(new Float(words[i]));
				}
			}
			centroids.put(key, centroidValues);
			break;
		case centroidConfigurationReached:
			ignoreLines = true;
			mustContainToStopIgnoring = "======";
			break;
		case end:
			// don't do 
			break;
		default:
			break;
		}
	}
}
