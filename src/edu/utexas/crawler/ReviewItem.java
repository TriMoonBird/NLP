package edu.utexas.crawler;

class ReviewItem {
	private String mAuthor;
	private String mReview;
	
	public ReviewItem(String author, String review) {
		mAuthor = author;
		mReview = review;
	}
	
	public ReviewItem() {
		this(null, null);
	}
	
	public String author() {
		return mAuthor;
	}
	
	public String review() {
		return mReview;
	}
	
	public void setAuthor(String author) {
		mAuthor = author;
	}
	
	public void setReview(String review) {
		mReview = review;
	}
}
