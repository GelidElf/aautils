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
import java.util.LinkedHashMap;

public class ProcessMTableForQLearning {

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

		File archivoSalida = createOutputFile();
		try {
			processAndWriteMtable(archivoSalida, ficherosEntrada);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void processAndWriteMtable(File archivoSalida, ArrayList<String> ficherosEntrada) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(archivoSalida));
		for (String nombreFichero : ficherosEntrada) {
			processDataSectionAndWriteToOutput(writer, nombreFichero);
		}
		writer.close();
	}

	private static void processDataSectionAndWriteToOutput(BufferedWriter writer, String nombreFichero) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(nombreFichero));
		String str = null;
		boolean foundDataSection = false;
		LinkedHashMap<String, Integer> attributes = new LinkedHashMap<String, Integer>();
		while ((str = in.readLine()) != null) {

			if (foundDataSection) {
				writer.write(str);
				writer.newLine();
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

}
