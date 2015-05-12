package edu.utexas.wordnet;

import java.util.ArrayList;

class WordNetTest{
	public static void main(String[] args) {
		System.out.println("This is a test program for WordNet 3.0 + Synset.");
		WordNet wordnet = new WordNet();
		ArrayList<String> words = wordnet.getAllRelatedWords("dog");
		for (String word : words) {
			System.out.println(word);
		}
	}
}
