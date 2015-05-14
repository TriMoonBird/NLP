package edu.utexas.seg;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.utexas.crawler.ReviewItem;
import edu.utexas.crawler.TaggedSentence;
import edu.utexas.wordnet.WordNet;

public class TileFeature {
	protected boolean mUseWordNet;
	protected boolean mUseStopword;
	protected boolean mUseStemmer;
	protected boolean mUsePreserveWord;
	protected WordNet mWordNet;
	protected ArrayList<ReviewItem> mReviewItems;
	protected String mStopwordFile;
	protected HashSet<String> mStopwords;
	protected SnowballStemmer mStemmer;
	protected HashSet<String> mPronounSet;
	protected String mPreserveWordFile;
	protected HashSet<String> mPreserveSet;
	protected MaxentTagger mTagger;
	
	private Hashtable<String, Double> mIdf;
	
	public boolean useWordNet() { return mUseWordNet; }
	public boolean useStopword() { return mUseStopword; }
	public boolean useStemmer() { return mUseStemmer; }
	public boolean usePreserveWord() { return mUsePreserveWord; }
	public ArrayList<ReviewItem> items() { return mReviewItems; }
	
	public void setWordNet(boolean flag) { mUseWordNet = flag; }
	public void setStopword(boolean flag) { mUseStopword = flag; }
	public void setStemmer(boolean flag) { mUseStemmer = flag; }
	public void setPreserveWord(boolean flag) { mUsePreserveWord = flag; }
	
	public TileFeature(ArrayList<ReviewItem> items) {
		mUseWordNet = true;
		mUseStopword = true;
		mUseStemmer = true;
		mUsePreserveWord = true;
		mWordNet = new WordNet();
		mReviewItems = items;
		mStopwordFile = "stopwords.txt";
		mStopwords = loadStopwords();
		mStemmer = new englishStemmer();
		mPronounSet = buildPronounSet();
		mPreserveWordFile = "wordlist.txt";
		mPreserveSet = loadPreserveWord();
		mIdf = buildIdf();
		mTagger = new MaxentTagger("lib/english-left3words-distsim.tagger");
	}
	
	protected HashSet<String> loadPreserveWord() {
		return loadPreserveWord(mPreserveWordFile);
	}
	protected HashSet<String> loadPreserveWord(String file) {
		HashSet<String> preserve = new HashSet<String>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = reader.readLine()) != null) {
				preserve.add(line.trim());
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return preserve;
	}
	
	public boolean isPreserveWord(String word) {
		return mPreserveSet.contains(word);
	}
	
	protected HashSet<String> loadStopwords() {
		return loadStopwords(mStopwordFile);
	}
	protected HashSet<String> loadStopwords(String file) {
		HashSet<String> stopwords = new HashSet<String>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = reader.readLine()) != null) {
				stopwords.add(line.trim());
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return stopwords;
	}
	
	public boolean isStopword(String word) {
		return mStopwords.contains(word);
	}
	
	protected String getStem(String word) {
		String token = word.toLowerCase();
		if (mUseStemmer) {
			mStemmer.setCurrent(token);
			mStemmer.stem();
			token = mStemmer.getCurrent();
		}
		return token;
	}
	
	protected Hashtable<String, Integer> parseSentence(String sentence) {
		Hashtable<String, Integer> stat = new Hashtable<String, Integer>();
		String[] tokens = sentence.replaceAll("[^\\p{Alnum}\\s]+", "").split("\\s+");
		for (int i = 0; i < tokens.length; ++i) {
			if (mUseStopword && isStopword(tokens[i])) continue;
			String token = getStem(tokens[i]);
			if (stat.containsKey(token)) {
				stat.put(token, stat.get(token));
			} else {
				stat.put(token, 1);
			}
		}
		return stat;
	}
	
	protected ArrayList<Hashtable<String, Integer> > parseAllSentences(ReviewItem item) {
		ArrayList<Hashtable<String, Integer> > allSentences = new ArrayList<Hashtable<String, Integer>>();
		for (TaggedSentence sen : item.sentences()) {
			allSentences.add(parseSentence(sen.sentence()));
		}
		return allSentences;
	}
	
	protected Hashtable<String, Double> buildIdf() {
		int sentenceCount = 0;
		Hashtable<String, Double> idf = new Hashtable<String, Double>();
		for (ReviewItem item : mReviewItems) {
			ArrayList<Hashtable<String, Integer> > all = parseAllSentences(item);
			for (Hashtable<String, Integer> sen : all) {
				sentenceCount ++;
				for (String word : sen.keySet()) {
					if (idf.containsKey(word)) {
						idf.put(word, idf.get(word)+1.0);
					} else {
						idf.put(word, 1.0);
					}
				}
			}
		}
		for (String word : idf.keySet()) {
			idf.put(word, Math.log10(sentenceCount/idf.get(word)));
		}
		return idf;
	}
	
	protected double cosineSimilarity(Hashtable<String, Integer> first, Hashtable<String, Integer> second) {
		HashSet<String> intersection = new HashSet<String>(first.keySet());
		intersection.retainAll(second.keySet());
		if (intersection.isEmpty()) return 0.0;
		double cosine = 0.0;
		double first_sq = 0.0;
		double second_sq = 0.0;
		for (String word : intersection) {
			cosine += first.get(word) * mIdf.get(word) * second.get(word) * mIdf.get(word);
		}
		for (String word : first.keySet()) {
			first_sq += Math.pow(first.get(word) * mIdf.get(word), 2);
		}
		for (String word : second.keySet()) {
			second_sq += Math.pow(second.get(word) * mIdf.get(word), 2);
		}
		cosine = cosine / Math.sqrt(first_sq * second_sq);
		return cosine;
	}
	
	protected HashSet<String> buildPronounSet() {
		String[] str = {"it", "its", "this", "these", "those", "their"};
		HashSet<String> pronoun = new HashSet<String>(Arrays.asList(str));
		return pronoun;
	}
	
	protected double pronounSimilarity(Hashtable<String, Integer> block) {
		HashSet<String> tokens = new HashSet<String>(block.keySet());
		tokens.retainAll(mPronounSet);
		return (double) tokens.size();
	}
	
	protected boolean validPronounFeature(String sentence) {
		String tagged = mTagger.tagString(sentence.replaceAll("[^\\p{Alnum}\\s+]+", ""));
		String[] tokens = tagged.split("\\s+");
		for (int i = 0; i < tokens.length; ++i) {
			String[] pair = tokens[i].split("_");
			if (pair.length > 1 && pair[1].startsWith("NN")) break;
			if (mPronounSet.contains(pair[0].toLowerCase())) return true;
		}
		return false;
	}
	
	protected double cutSimilarity(ArrayList<Hashtable<String, Integer> > all, int first, int window) {
		int start = first + 1;
		int end = Math.min(first + window, all.size() - 1);
		int cutCount = 0;
		HashSet<String> tokens = new HashSet<String>();
		for (int i = start; i <= end; ++i) {
			tokens.addAll(all.get(i).keySet());
		}
		for (String word : all.get(first).keySet()) {
			if (tokens.contains(word)) cutCount++;
		}
		return (double) cutCount;
	}
	
	protected double lengthSimilarity(Hashtable<String, Integer> block) {
		int wordsCount = 0;
		for (String word : block.keySet()) {
			wordsCount += block.get(word);
		}
		return (double) wordsCount;
	}
	
	protected double equiSimilarity(Hashtable<String, Integer> first, Hashtable<String, Integer> second) {
		int equiCount = 0;
		int firstCount = 0;
		int secondCount = 0;
		for (String word : second.keySet()) {
			secondCount += second.get(word);
		}
		for (String word : first.keySet()) {
			firstCount += first.get(word);
			HashSet<String> synset = null;
			if (mUseWordNet == true && word.matches("\\p{Alnum}+")) {
				// use WordNet
				synset = new HashSet<String>(mWordNet.getAllRelatedWords(word));
			} else {
				// do not use WordNet
				synset = new HashSet<String>();
				synset.add(word);
			}
			synset.retainAll(second.keySet());
			if (!synset.isEmpty()) {
				if (!second.contains(word)) equiCount += first.get(word);
				else equiCount += Math.min(first.get(word), second.get(word));
			}
		}
		//System.out.println(first);
		//System.out.println(second);
		double similarity = ((double) equiCount) / Math.sqrt(firstCount * secondCount);
		return similarity;
	}
	
	protected String scoreToString(double score, int precision) {
		return String.format("%."+precision+"f", score);
	}
	
	public void computeOneReview(ReviewItem item) {
		int precision = 4;
		int window = 2;
		ArrayList<TaggedSentence> sentences = item.sentences();
		ArrayList<Hashtable<String, Integer> > all = parseAllSentences(item);
		Hashtable<String, Integer> first = null;
		Hashtable<String, Integer> second = all.get(0);
		// features of all sentences
		//sentences.get(0).addFeature(scoreToString(lengthSimilarity(second), 0));
		// features that first sentence does not have
		for (int i = 1; i < sentences.size(); ++i) {
			first = second;
			second = all.get(i);
			//sentences.get(i).addFeature(scoreToString(lengthSimilarity(second), 0));
			sentences.get(i).addFeature(scoreToString(equiSimilarity(first, second), precision));
			//sentences.get(i).addFeature(scoreToString(pronounSimilarity(second), 0));
			sentences.get(i).addFeature(scoreToString(cutSimilarity(all, i-1, window), 0));
			//sentences.get(i).addFeature(scoreToString(cosineSimilarity(first, second), precision));
			if (equiSimilarity(first, second) == 0) sentences.get(i).addFeature("EQUIZERO");
			//if (pronounSimilarity(second) == 0) sentences.get(i).addFeature("PRONOUNZERO");
			if (validPronounFeature(sentences.get(i).sentence())) sentences.get(i).addFeature("VALIDPRONOUN");
			if (cutSimilarity(all, i-1, window) == 0) sentences.get(i).addFeature("CUTZERO");
			//if (cosineSimilarity(first, second) == 0) sentences.get(i).addFeature("COSINEZERO");
		}
	}
	
	public void computeAllReviews() {
		for (ReviewItem item : mReviewItems) {
			computeOneReview(item);
		}
	}
}
