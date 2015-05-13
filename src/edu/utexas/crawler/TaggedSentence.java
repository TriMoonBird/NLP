package edu.utexas.crawler;

import java.util.ArrayList;

public class TaggedSentence{
	private String mTag;
	private String mSentence;
	private ArrayList<String> mFeature;
	
	public String tag() { return mTag; }
	public String sentence() { return mSentence; }
	public ArrayList<String> feature() { return mFeature; }
	
	public void setTag(String tag) { mTag = tag; }
	public void setSentence(String sentence) { mSentence = sentence; }
	public void addFeature(String feature) {
		mFeature.add(feature);
	}
	
	public TaggedSentence(String tag, String sentence) {
		mTag = tag;
		mSentence = sentence;
		mFeature = new ArrayList<String>();
	}
	
	public TaggedSentence(String sentence) {
		this(null, sentence);
	}
	
	public TaggedSentence() {
		this(null, null);
	}
}
