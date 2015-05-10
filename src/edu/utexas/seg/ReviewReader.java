package edu.utexas.seg;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.utexas.crawler.ReviewItem;
import edu.utexas.crawler.TaggedSentence;

public class ReviewReader{
	private String mReviewFile;
	private ArrayList<ReviewItem> mItems;
	
	public void setReviewFile(String file) { mReviewFile = file; }
	public String getReviewFile() { return mReviewFile; }
	
	public ReviewReader(String file) {
		mReviewFile = file;
		mItems = new ArrayList<ReviewItem>();
	}
	
	public ReviewReader() {
		this(null);
	}
	
	public ArrayList<ReviewItem> read() {
		if (mReviewFile == null) return null;
		
		Pattern reviewBegin		= Pattern.compile("^\\s*\\{\\s*$");
		Pattern validField		= Pattern.compile("^\\s*Valid:\\s+(\\d+)$");
		Pattern indexField		= Pattern.compile("^\\s*Index:\\s+(\\d+)$");
		Pattern ratingField		= Pattern.compile("^\\s*Rating:\\s+(\\d+)$");
		Pattern titleField		= Pattern.compile("^\\s*Title:\\s+(.+)$");
		Pattern sentenceField	= Pattern.compile("^\\s*<(.*)>(.*)$");
		Pattern reviewEnd		= Pattern.compile("^\\s*\\}\\s*$");
		
		int itemCount = 0;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(mReviewFile));
			ReviewItem curReviewItem = null;
			String line;
			while ((line = reader.readLine()) != null) {
				Matcher matcher;
				if (reviewBegin.matcher(line).matches()) {
					curReviewItem = new ReviewItem();
				} else if (validField.matcher(line).matches()) {
					;
				} else if (indexField.matcher(line).matches()) {
					;
				} else if ((matcher = ratingField.matcher(line)).matches()) {
					curReviewItem.setRating(matcher.group(1));
				} else if ((matcher = titleField.matcher(line)).matches()) {
					curReviewItem.setTitle(matcher.group(1));
				} else if ((matcher = sentenceField.matcher(line)).matches()) {
					String tag = matcher.group(1);
					String sentence = matcher.group(2);
					curReviewItem.sentences().add(new TaggedSentence(tag, sentence));
				} else if (reviewEnd.matcher(line).matches()) {
					mItems.add(curReviewItem);
					itemCount++;
				}
			}
			reader.close();
			System.out.println("Item Count: " + itemCount);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return mItems;
	}
}
