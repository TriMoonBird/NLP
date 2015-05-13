package edu.utexas.seg;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import edu.utexas.crawler.ReviewItem;
import edu.utexas.crawler.TaggedSentence;

public class TileFeatureGen {
	
	public static void main(String[] args) {
		String reviewFile = "review.txt";
		ReviewReader reader = new ReviewReader(reviewFile);
		ArrayList<ReviewItem> items = reader.read();
		TileFeature tf = new TileFeature(items);
		tf.computeAllReviews();
		outputMalletFormat(tf.items());
	}
	
	public static void outputMalletFormat(ArrayList<ReviewItem> items) {
		String outputFile = "mallet.txt";
		try {
			FileWriter writer = new FileWriter(outputFile);
			for (ReviewItem item : items) {
				for (TaggedSentence sen : item.sentences()) {
					writer.write(sen.sentence() + " ");
					if (sen.rel() != null) writer.write("@REL@###" + sen.rel() + " ");
					writer.write(sen.tag() + "\n");
				}
				writer.write("\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
