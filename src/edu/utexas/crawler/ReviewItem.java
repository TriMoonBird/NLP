package edu.utexas.crawler;

class ReviewItem {
	private static int mCount = 1;
	private String mRating;
	private String mAuthor;
	private String mTitle;
	private String mReview;
	
	public ReviewItem(String rating, String author, String title, String review) {
		mRating = rating;
		mAuthor = author;
		mTitle = title;
		mReview = review;
	}
	
	public ReviewItem(String rating, String title, String review) {
		this(rating, null, title, review);
	}
	
	public ReviewItem() {
		this(null, null, null, null);
	}
	
	public String rating() { return mRating; }
	public String author() { return mAuthor; }
	public String review() { return mReview; }
	public String title() { return mTitle; }
	
	public void setRating(String rating) { mRating = rating; }
	public void setAuthor(String author) { mAuthor = author; }
	public void setReview(String review) { mReview = review; }
	public void setTitle(String title) { mTitle = title; }
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		final String NEW_LINE = System.getProperty("line.separator");
		str.append("{" + NEW_LINE);
		if (mReview != null) str.append("Valid: 1"); else str.append("Valid: 0"); str.append(NEW_LINE);
		if (mReview != null) str.append("Index: " + (mCount++) + NEW_LINE);
		if (mRating != null) str.append("Rating: " + mRating + NEW_LINE);
		if (mAuthor != null) str.append("Author: " + mAuthor + NEW_LINE);
		if (mTitle != null) str.append("Title: " + mTitle + NEW_LINE);
		if (mReview != null) {
			str.append("Review: " + NEW_LINE);
			ReviewSeparator separatedReview = new ReviewSeparator(mReview);
			separatedReview.separate();
			str.append(separatedReview);
		}
		str.append("}" + NEW_LINE);
		return str.toString();
	}
}
