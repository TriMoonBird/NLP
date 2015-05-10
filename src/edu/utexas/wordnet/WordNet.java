package edu.utexas.wordnet;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;

class WordNet {
	public static void main(String[] args) {
		try {
			// construct the URL to the Wordnet dictionary directory
			//String wnhome = System.getenv("WNHOME");
			String path = "/usr/local/WordNet-3.0/dict";
			URL url = new URL("file", null, path);

			// construct the dictionary object and open it
			IDictionary dict = new Dictionary (url);
			dict.open ();

			// look up first sense of the word "dog "
			IIndexWord idxWord = dict.getIndexWord ("dog", POS.NOUN);
			IWordID wordID = idxWord.getWordIDs().get(0);
			IWord word = dict.getWord ( wordID );
			getSynonyms(dict);
			/*
			System .out . println ("Id = " + wordID );
			System .out . println (" Lemma = " + word . getLemma ());
			System .out . println (" Gloss = " + word . getSynset (). getGloss ());
			*/
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void getSynonyms(IDictionary dict) {

		// look up first sense of the word "dog "
		IIndexWord idxWord = dict . getIndexWord ("bad", POS.ADJECTIVE);
		IWordID wordID = idxWord . getWordIDs ().get (0) ; // 1st meaning
		IWord word = dict . getWord ( wordID );
		ISynset synset = word . getSynset ();

		// iterate over words associated with the synset
		for( IWord w : synset . getWords ())
			System .out . println (w. getLemma ());
	}
}
