package practica1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Muestra por pantalla el nombre de la variable y todos los datos de esa
 * variable del archivo arff que
 * 
 * @author Gonzalo Canelada Purcell & Lorena Prieto Horcajo
 * 
 */
public class ColumnaDelFuturoARFF {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		String sufijoSiguiente = "_siguiente";

		if (args.length != 2) {
			System.out
					.println("Error parametros, primer parametro columna, segudo nombre del archivo");
			return;
		}

		String valorColumna = args[0];
		String fileName = args[1];
		if (!checkFile(fileName)) {
			return;
		}
		int columna = -1;
		try {
			columna = Integer.parseInt(valorColumna);
			if (columna <= 0) {
				System.out
						.println("El valor suministrado,"
								+ valorColumna
								+ " para el inidicador de la columna ha de ser mayor o igual a 1");
			}
		} catch (NumberFormatException e) {
			System.out.println("El valor suministrado," + valorColumna
					+ " para el inidicador de la columna no es correcto");
			return;
		}

		BufferedReader in = new BufferedReader(new FileReader(fileName));
		String str = null;
		int attributeCount = 0;
		String selectedAttribute = null;
		boolean inDataSection = false;

		String filaDatosVieja = null;

		File archivoSalida = createOutputFile(fileName);
		BufferedWriter writer = new BufferedWriter(
				new FileWriter(archivoSalida));
		while ((str = in.readLine()) != null) {

			if (str.contains("@ATTRIBUTE")) {
				attributeCount++;
			}
			if (attributeCount == columna) {
				selectedAttribute = str;
				System.out.println("Attrubuto seleccionado: "
						+ selectedAttribute);
			}
			if (inDataSection) {
				if (attributeCount < columna - 1) {
					System.out
							.println("Se ha seleccionado un numero de columna demasiado grande");
					return;
				} else {
					if (filaDatosVieja != null) {
						if (str.length() != 0) {
							writeToFile(writer,
									filaDatosVieja + ','
											+ str.split(",")[columna - 1]);
						}
					} else {
						filaDatosVieja = str;
					}
				}
			}
			if (str.contains("@DATA")) {
				inDataSection = true;
				String columnName = selectedAttribute.split(" ")[1];
				String newAttribute = selectedAttribute.replace(columnName,
						columnName + sufijoSiguiente);
				writeToFile(writer, newAttribute);
				writeToFile(writer, str);
			} else {
				if (!inDataSection) {
					writeToFile(writer, str);
				}
			}
		}
		writer.close();
	}

	private static File createOutputFile(String nombreFicheroEntrada) {
		String nombreArchivoSalida = nombreFicheroEntrada.substring(0,
				nombreFicheroEntrada.lastIndexOf('.'))
				+ "-siguiente.arff";
		File archivoSalida = new File(nombreArchivoSalida);
		return archivoSalida;
	}

	private static void writeToFile(BufferedWriter writer, String str) {
		try {
			writer.write(str);
			writer.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static boolean checkFile(String fileName) {
		File archivo = new File(fileName);
		if (!fileName.endsWith(".arff")) {
			System.out.println("El archivo " + fileName
					+ " ha de acabar con extension .arff");
			return false;
		}
		if (!archivo.exists()) {
			System.out.println("El archivo especificado " + fileName
					+ " no existe");
			return false;
		}
		return true;
	}

}
