package edu.utexas.crawler;

public class TaggedSentence{
	private String mTag;
	private String mSentence;
	private String mRel;
	
	public String tag() { return mTag; }
	public String sentence() { return mSentence; }
	public String rel() { return mRel; }
	
	public void setTag(String tag) { mTag = tag; }
	public void setSentence(String sentence) { mSentence = sentence; }
	public void setRel(String rel) { mRel = rel; }
	
	public TaggedSentence(String tag, String sentence) {
		mTag = tag;
		mSentence = sentence;
		mRel = null;
	}
	
	public TaggedSentence(String sentence) {
		this(null, sentence);
	}
	
	public TaggedSentence() {
		this(null, null);
	}
}
