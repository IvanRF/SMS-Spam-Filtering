/*
 * Copyright (C) 2013 Ivan Ridao Freitas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ivanrf.smsspam;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Scanner;

import com.ivanrf.utils.Utils;

public class ARFFBuilder {

	public static void main(String[] args) throws Exception {
		long start = System.currentTimeMillis();
		
		String arff = "@relation sms_spam\n\n";
		
		arff += "@attribute spam_class { ham, spam }\n";
		arff += "@attribute text String\n\n";
		
		String data = parse(new FileInputStream(new File("SMSSpamCollection")), "UTF-8");
		arff += "@data\n";
		arff += data;
		
		System.out.println(arff);
		Utils.saveFile(new File("SMSSpamCollection.arff"), arff);
		
		System.out.println("Generado en: "+Utils.getDateHsMinSegString(System.currentTimeMillis()-start));
	}
	
	private static String parse(InputStream src, String fileEncoding) {
		String data = "";
		Scanner scanner = new Scanner(src, fileEncoding);
		try {
			while (scanner.hasNextLine())
				data += parseLine(scanner.nextLine());
		} finally {
			scanner.close();
		}
		return data;
	}
	
	private static String parseLine(String str) {
		int tabIndex = str.indexOf('\t');
		String type = str.substring(0, tabIndex);
		String msg = str.substring(tabIndex + 1);
		msg = msg.replaceAll("\"", "\\\\\""); //Hago escape de "
		msg = msg.replaceAll("\\\\'", "'"); //Saco escape de ' si ya tenian
		msg = msg.replaceAll("'", "\\\\'"); //Hago escape de '
		return type + ",\t'" + msg + "'\n";
	}
}
