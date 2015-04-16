package edu.utexas.crawler;

class ReviewItem {
	private String mAuthor;
	private String mTitle;
	private String mReview;
	
	public ReviewItem(String author, String title, String review) {
		mAuthor = author;
		mTitle = title;
		mReview = review;
	}
	
	public ReviewItem(String title, String review) {
		this(null, title, review);
	}
	
	public ReviewItem(String review) {
		this(null, null, review);
	}
	
	public ReviewItem() {
		this(null, null, null);
	}
	
	public String author() {
		return mAuthor;
	}
	
	public String review() {
		return mReview;
	}
	
	public String title() {
		return mTitle;
	}
	
	public void setAuthor(String author) {
		mAuthor = author;
	}
	
	public void setReview(String review) {
		mReview = review;
	}
	
	public void setTitle(String title) {
		mTitle = title;
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		final String NEW_LINE = System.getProperty("line.separator");
		if (mAuthor != null) str.append("Author: " + mAuthor + NEW_LINE);
		if (mTitle != null) str.append("Title: " + mTitle + NEW_LINE);
		if (mReview != null) str.append("Review: " + mReview + NEW_LINE);
		return str.toString();
	}
}
