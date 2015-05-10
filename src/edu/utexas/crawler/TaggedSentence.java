package edu.utexas.crawler;

public class TaggedSentence{
	private String mTag;
	private String mSentence;
	
	public String tag() { return mTag; }
	public String sentence() { return mSentence; }
	
	public void setTag(String tag) { mTag = tag; }
	public void setSentence(String sentence) { mSentence = sentence; }
	
	public TaggedSentence(String tag, String sentence) {
		mTag = tag;
		mSentence = sentence;
	}
	
	public TaggedSentence(String sentence) {
		this(null, sentence);
	}
	
	public TaggedSentence() {
		this(null, null);
	}
}
