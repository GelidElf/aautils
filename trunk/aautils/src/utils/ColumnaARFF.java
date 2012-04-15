package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Muestra por pantalla el nombre de la variable y todos los datos de esa variable del archivo arff que 
 * @author Gonzalo Canelada Purcell & Lorena Prieto Horcajo
 *
 */
public class ColumnaARFF {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		if (args.length != 2){
			System.out.println("Error parametros, primer parametro columna, segudo nombre del archivo");
			return;
		}

		String valorColumna = args[0];
		String fileName = args[1];
		if (!checkFile(fileName)){
			return;
		}
		int columna = -1;
		try{
			columna = Integer.parseInt(valorColumna);
			if (columna <= 0){
				System.out.println("El valor suministrado,"+valorColumna+" para el inidicador de la columna ha de ser mayor o igual a 1");
			}
		}catch (NumberFormatException e){
			System.out.println("El valor suministrado,"+valorColumna+" para el inidicador de la columna no es correcto");
			return;
		}
		
		BufferedReader in = new BufferedReader(new FileReader(fileName));
		String str= null;
		int attributeCount = 0;
		String selectedAttribute = null;
		boolean inDataSection = false;
		List<String> salida = new ArrayList<String>();
		while ((str = in.readLine()) != null){
			if (str.contains("@ATTRIBUTE")){
				attributeCount++;
			}
			if (attributeCount == columna){
				selectedAttribute = str.split(" ")[1]; //Seleccionamos el nombre de la columna
			}
			if (inDataSection){
				if (attributeCount < columna-1){
					System.out.println("Se ha seleccionado un numero de columna demasiado grande");
					return;
				}else{
					salida.add(str.split(",")[columna-1]);
				}
			}
			if (str.contains("@DATA")){
				inDataSection = true;
			}
		}
		System.out.println("Attrubuto seleccionado: " +selectedAttribute);
		for (String s:salida){
			System.out.println(s);
		}
	}

	private static boolean checkFile(String fileName) {
		File archivo = new File(fileName);
		if (!fileName.endsWith(".arff")){
			System.out.println("El archivo "+ fileName + " ha de acabar con extension .arff");
			return false;
		}
		if(!archivo.exists()){
			System.out.println("El archivo especificado " + fileName +" no existe");
			return false;
		}
		return true;
	}

}
