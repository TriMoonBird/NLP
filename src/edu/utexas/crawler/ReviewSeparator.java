package edu.utexas.crawler;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ReviewSeparator {
	private static final String SPLIT_REGEX = "(.+?(?<![0-9])[\\.\\!\\?]+(?!\\S)) *";
	private String mText;
	private ArrayList<String> mSentences;
	
	public ReviewSeparator(String text) {
		mText = text;
	}
	
	public ReviewSeparator() {
		this(null);
	}
	
	public String text() { return mText; }
	
	public void setText(String text) { mText = text; }
	
	public ArrayList<String> separate() {
		mSentences = new ArrayList<String>();
		Pattern pattern = Pattern.compile(SPLIT_REGEX);
		Matcher matcher = pattern.matcher(mText);
		while (matcher.find()) {
			mSentences.add(matcher.group(1));
			matcher = matcher.region(matcher.end(), matcher.regionEnd());
		}
		return mSentences;
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		final String NEW_LINE = System.getProperty("line.separator");
		for (String sentence : mSentences) {
			str.append("<>");
			str.append(sentence + NEW_LINE);
		}
		return str.toString();
	}
}
