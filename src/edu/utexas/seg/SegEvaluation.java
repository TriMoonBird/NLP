package edu.utexas.seg;

import java.util.ArrayList;
import java.util.HashSet;

import edu.utexas.crawler.ReviewItem;
import edu.utexas.crawler.TaggedSentence;

class SegEvaluation {
	private int mItemCount = 0;
	private ArrayList<ReviewEvaluation> mEvaluations;
	private double mRecall = 0.0;
	private double mPrecision = 0.0;
	private double mF1score = 0.0;
	
	public SegEvaluation(ArrayList<ReviewItem> reviewItems) {
		mEvaluations = new ArrayList<ReviewEvaluation>();
		mItemCount = 0;
		mRecall = 0.0;
		mPrecision = 0.0;
		mF1score = 0.0;
		for (ReviewItem item : reviewItems) {
			mItemCount ++;
			mEvaluations.add(new ReviewEvaluation(item));
		}
	}
	
	public int itemCount() { return mItemCount; }
	public double recall() { return mRecall; }
	public double precision() { return mPrecision; }
	public double f1score() { return mF1score; }
	public ArrayList<ReviewEvaluation> evalItems() { return mEvaluations; }
	
	public void printResults() {
		System.out.println("Precision: " + mPrecision);
		System.out.println("Recall: " + mRecall);
		System.out.println("F1: " + mF1score);
	}
	
	private void computeGroundTruth() {
		for (ReviewEvaluation eval : mEvaluations) {
			ArrayList<Integer> boundaries = new ArrayList<Integer>();
			ArrayList<TaggedSentence> tsList = eval.reviewItem().sentences();
			String prev = tsList.get(0).tag();
			int i = 1;
			for (; i < tsList.size(); ++i) {
				String cur = tsList.get(i).tag();
				if (!cur.equals(prev)) {
					boundaries.add(i-1);
				}
			}
			boundaries.add(i-1);
			eval.setGroundTruth(boundaries);
		}
	}
	
	public void evaluate() {
		computeGroundTruth();
		int recallTotal = 0;
		int returnTotal = 0;
		int correctCount = 0;
		for (ReviewEvaluation eval : mEvaluations) {
			HashSet<Integer> groundtruth = new HashSet<Integer>(eval.groundTruth());
			HashSet<Integer> seg = new HashSet<Integer>(eval.segment());
			recallTotal += groundtruth.size();
			returnTotal += seg.size();
			seg.retainAll(groundtruth);
			correctCount += seg.size();
		}
		mRecall = ((double)correctCount) / recallTotal;
		mPrecision = ((double)correctCount) / returnTotal;
		calculateF1score();
	}
	
	private void calculateF1score() {
		mF1score = 2*mRecall*mPrecision / (mRecall+mPrecision);
	}
}
