package edu.utexas.crawler;

import java.io.BufferedReader;
import java.io.FileReader;

class ParseHtml {
	public static void main(String[] args) throws Exception{
		String filepath = "data/facebook.html";
		BufferedReader reader = new BufferedReader(new FileReader(filepath));
		String str;
		while ((str = reader.readLine()) != null) {
			if (str.length() < 500) {
				System.out.println(str);
			}
		}
		reader.close();
	}
}
