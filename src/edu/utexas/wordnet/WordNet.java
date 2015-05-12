package edu.utexas.wordnet;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IPointer;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;

public class WordNet {
	private IDictionary mDict;
	private static String mPath = "/usr/local/WordNet-3.0/dict";
	
	public WordNet(String path) {
		try {
			// construct the URL to the Wordnet dictionary directory
			mPath = new String(path);
			URL url = new URL("file", null, path);
			// construct the dictionary object and open it
			mDict = new Dictionary (url);
			mDict.open();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public WordNet() {
		this(mPath);
	}
	
	public ArrayList<String> getAllRelatedWords(String query) {
		ArrayList<String> allRelatedWords = new ArrayList<String>();
		allRelatedWords.addAll(getSynonyms(query));
		//allRelatedWords.addAll(getRelatedWords(Pointer.ANTONYM, query));
		allRelatedWords.addAll(getRelatedWords(Pointer.HYPONYM, query));
		allRelatedWords.addAll(getRelatedWords(Pointer.HYPERNYM, query));
		allRelatedWords.addAll(getRelatedWords(Pointer.ATTRIBUTE, query));
		allRelatedWords.addAll(getRelatedWords(Pointer.CAUSE, query));
		allRelatedWords.addAll(getRelatedWords(Pointer.PERTAINYM, query));
		allRelatedWords.addAll(getRelatedWords(Pointer.SIMILAR_TO, query));
		allRelatedWords.addAll(getRelatedWords(Pointer.HOLONYM_MEMBER, query));
		allRelatedWords.addAll(getRelatedWords(Pointer.MERONYM_MEMBER, query));
		if (!allRelatedWords.contains(query)) allRelatedWords.add(query);
		return allRelatedWords;
	}
	
	public ArrayList<String> getSynonyms(String query) {
		ArrayList<String> synonyms = new ArrayList<String>();
		// get index word
		IIndexWord idxWord = null;
		if (idxWord == null) idxWord = mDict.getIndexWord(query, POS.NOUN);
		if (idxWord == null) idxWord = mDict.getIndexWord(query, POS.VERB);
		if (idxWord == null) idxWord = mDict.getIndexWord(query, POS.ADJECTIVE);
		if (idxWord == null) idxWord = mDict.getIndexWord(query, POS.ADVERB);
		if (idxWord == null) return synonyms;
		// get Synset
		IWordID wordID = idxWord.getWordIDs().get(0); // 1st meaning
		IWord word = mDict.getWord(wordID);
		ISynset synset = word.getSynset();
		// get synonums
		for (IWord iword : synset.getWords()) {
			synonyms.add(iword.getLemma());
		}
		return synonyms;
	}
	
	public ArrayList<String> getRelatedWords(IPointer ptr, String query){
		ArrayList<String> relatedWords = new ArrayList<String>();
		// get index word
		IIndexWord idxWord = null;
		if (idxWord == null) idxWord = mDict.getIndexWord(query, POS.NOUN);
		if (idxWord == null) idxWord = mDict.getIndexWord(query, POS.VERB);
		if (idxWord == null) idxWord = mDict.getIndexWord(query, POS.ADJECTIVE);
		if (idxWord == null) idxWord = mDict.getIndexWord(query, POS.ADVERB);
		if (idxWord == null) return relatedWords;
		// get Synset
		IWordID wordID = idxWord.getWordIDs().get(0); // 1st meaning
		IWord word = mDict.getWord(wordID);
		ISynset synset = word.getSynset();
		// get related words according to the pointer
		List<ISynsetID> relatedSid = synset.getRelatedSynsets(ptr);
		for (ISynsetID sid : relatedSid) {
			List<IWord> iwords = mDict.getSynset(sid).getWords();
			for (IWord iword : iwords) {
				relatedWords.add(iword.getLemma());
			}
		}
		return relatedWords;
	}
}
