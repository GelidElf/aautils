package utils;


import java.io.File;
import java.io.IOException;

public class CreateQLearningAlphabet {

	private static final String SPLIT_EXPRESSION = "\\s+";

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if (args.length != 2) {
			System.out.println("Se ha de invocar con dos parametros, " +
					"\t\t - El nombre del fichero que contiene los clusters" +
					"\t\t - El nombre del fichero donde guardar el alfabeto");
			return;
		}
		
		String nombreFicheroEntrada = args[0];
		Clustering clustering = new Clustering(nombreFicheroEntrada,SPLIT_EXPRESSION);
		String nombreFicheroSalida = args[1];
		try {
			clustering.saveAsAlphabet(new File(nombreFicheroSalida));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
