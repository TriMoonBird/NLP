package edu.utexas.seg;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

import edu.utexas.crawler.ReviewItem;
import edu.utexas.crawler.TaggedSentence;
import edu.utexas.wordnet.WordNet;

public class TileFeature {
	protected boolean mUseWordNet;
	protected boolean mUseStopword;
	protected boolean mUseStemmer;
	protected WordNet mWordNet;
	protected ArrayList<ReviewItem> mReviewItems;
	protected String mStopwordFile;
	protected HashSet<String> mStopwords;
	protected SnowballStemmer mStemmer;
	
	public boolean useWordNet() { return mUseWordNet; }
	public boolean useStopword() { return mUseStopword; }
	public boolean useStemmer() { return mUseStemmer; }
	public ArrayList<ReviewItem> items() { return mReviewItems; }
	
	public void setWordNet(boolean flag) { mUseWordNet = flag; }
	public void setStopword(boolean flag) { mUseStopword = flag; }
	public void setStemmer(boolean flag) { mUseStemmer = flag; }
	
	public TileFeature(ArrayList<ReviewItem> items) {
		mUseWordNet = true;
		mUseStopword = true;
		mUseStemmer = true;
		mWordNet = new WordNet();
		mReviewItems = items;
		mStopwordFile = "stopwords.txt";
		mStopwords = new HashSet<String>();
		mStemmer = new englishStemmer();
	}
	
	protected void loadStopwords() {
		loadStopwords(mStopwordFile);
	}
	protected void loadStopwords(String file) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = reader.readLine()) != null) {
				mStopwords.add(line.trim());
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected boolean isStopword(String word) {
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
	
	protected double computeSimilarity(Hashtable<String, Integer> first, Hashtable<String, Integer> second) {
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
		ArrayList<TaggedSentence> sentences = item.sentences();
		Hashtable<String, Integer> first = null;
		Hashtable<String, Integer> second = parseSentence(sentences.get(0).sentence());
		for (int i = 1; i < sentences.size(); ++i) {
			first = second;
			second = parseSentence(sentences.get(i).sentence());
			double similarity = computeSimilarity(first, second);
			sentences.get(i).setRel(scoreToString(similarity, precision));
		}
	}
	
	public void computeAllReviews() {
		for (ReviewItem item : mReviewItems) {
			computeOneReview(item);
		}
	}
}
