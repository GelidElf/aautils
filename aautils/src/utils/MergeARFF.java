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

/**
 * Une en un mismo archivo la seccion de data de todos los archivos arff que se le pasan por parametro, siendo estos a través de wildcard..
 * Usa como cabeceras aquellas del primer archivo que encuentra, no comprueba si la seccion de cabeceras es igual en todos los archivos.
 * @author Gonzalo Canelada Purcell & Lorena Prieto Horcajo
 *
 */
public class MergeARFF {

	/**
	 * Reads files, accepts wildcards or lists of files
	 * @param 
	 */
	public static void main(String[] args) {
		
		ArrayList<String> ficherosEntrada = new ArrayList<String>();
		
		if (args.length < 1){
			System.out.println("Se ha de invocar con al menos un parametro, el nombre de un fichero o nombres con un wildcard");
			return;
		}

		for (String entrada:args){
			if (tieneWildCard(entrada)){
				ficherosEntrada.addAll(ficherosDelFiltro(entrada));
			}else{
				ficherosEntrada.add(entrada);
			}
		}
		
		
		File archivoSalida = createOutputFile();
		try {
			mergeFiles(archivoSalida,ficherosEntrada);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void duplicaPrimerArchivo(BufferedWriter writer,
			ArrayList<String> ficherosEntrada) throws IOException {
		String primerArchivo = ficherosEntrada.get(0);
		ficherosEntrada.remove(0);
		BufferedReader in = new BufferedReader(new FileReader(primerArchivo));
		String str= null;
		while ((str = in.readLine()) != null){
				writer.write(str);
				writer.flush();
				writer.newLine();
		}
		
	}

	private static File createOutputFile() {
		String nombreArchivoSalida = "MergedOutput"+System.currentTimeMillis()+".arff";
		File archivoSalida = new File(nombreArchivoSalida);
		return archivoSalida;
	}

	private static void mergeFiles(File archivoSalida,
		ArrayList<String> ficherosEntrada) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(archivoSalida));
		writeMergeCommentString(writer,ficherosEntrada);
		duplicaPrimerArchivo(writer,ficherosEntrada);
		for (String nombreFichero:ficherosEntrada){
			writeDataSectionToOuputFile(writer,nombreFichero);
		}
		writer.close();
	}

	private static void writeMergeCommentString(
			BufferedWriter writer, ArrayList<String> ficherosEntrada) throws IOException {
		StringBuilder builder = new StringBuilder();
		builder.append("%Created from: ");
		for (String s:ficherosEntrada){
			builder.append(s + ", ");
		}
		writer.write(builder.toString());
		writer.newLine();
	}

	private static void writeDataSectionToOuputFile(BufferedWriter writer, String nombreFichero) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(nombreFichero));
		String str= null;
		boolean foundDataSection = false;
		while ((str = in.readLine()) != null){
			if (foundDataSection){
				writer.write(str);
				writer.newLine();
			}
			if (str.contains("@DATA")){
				foundDataSection = true;
			}
		}
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
		
		public WildcardedFileNameFilter(String filter){
			regex = "^"+filter.replace(".","\\.").replace("?", ".?").replace("*", ".*?")+"$";
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
