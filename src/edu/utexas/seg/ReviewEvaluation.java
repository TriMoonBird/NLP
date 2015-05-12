package edu.utexas.seg;

import java.util.ArrayList;

import edu.utexas.crawler.ReviewItem;

public class ReviewEvaluation {
	private ReviewItem mReviewItem;
	private ArrayList<Integer> mGroundTruth;
	private ArrayList<Integer> mSeg;
	
	public ReviewEvaluation(ReviewItem reviewItem) {
		mReviewItem = reviewItem;
		mGroundTruth = new ArrayList<Integer>();
		mSeg = new ArrayList<Integer>();
	}
	
	public ReviewItem reviewItem() { return mReviewItem; }
	public ArrayList<Integer> groundTruth() { return mGroundTruth; }
	public ArrayList<Integer> segment() { return mSeg; }
	
	public void setSegment(ArrayList<Integer> seg) { mSeg = seg; }
	public void setGroundTruth(ArrayList<Integer> gt) { mGroundTruth = gt; }

}
