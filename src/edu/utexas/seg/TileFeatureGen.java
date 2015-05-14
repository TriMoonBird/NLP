package edu.utexas.seg;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import edu.utexas.crawler.ReviewItem;
import edu.utexas.crawler.TaggedSentence;

public class TileFeatureGen {
	
	public static void main(String[] args) throws IOException {
		String reviewFile = "review.txt";
		ReviewReader reader = new ReviewReader(reviewFile);
		ArrayList<ReviewItem> items = reader.read();
		TileFeature tf = new TileFeature(items);
		tf.computeAllReviews();
		outputMalletFormat(tf);
	}
	
	public static void outputMalletFormat(TileFeature tf) {
		String outputFile = "mallet.txt";
		ArrayList<ReviewItem> items = tf.items();
		try {
			FileWriter writer = new FileWriter(outputFile);
			for (ReviewItem item : items) {
				for (TaggedSentence sen : item.sentences()) {
					if (tf.usePreserveWord()) {
						String sentence = sen.sentence();
						String[] tokens = sentence.replaceAll("[^\\p{Alnum}\\s]+", "").split("\\s+");
						for (int i = 0; i < tokens.length; ++i) {
							if (tf.isPreserveWord(tokens[i])) {
								writer.write(tokens[i] + " ");
							}
						}
					} else {
						writer.write(sen.sentence() + " ");
					}
					for (int i = 0; i < sen.feature().size(); ++i) {
						writer.write("@REL"+i+"###" + sen.feature().get(i) + " ");
					}
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
