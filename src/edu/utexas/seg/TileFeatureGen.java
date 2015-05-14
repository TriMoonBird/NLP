package edu.utexas.seg;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import edu.utexas.crawler.ReviewItem;
import edu.utexas.crawler.TaggedSentence;

public class TileFeatureGen {
	private static boolean mOutputWords = true;
	
	public static void main(String[] args) throws IOException {
		String reviewFile = "review.txt";
		ReviewReader reader = new ReviewReader(reviewFile);
		ArrayList<ReviewItem> items = reader.read();
		mOutputWords = true;
		TileFeature tf = new TileFeature(items);
		tf.computeAllReviews();
		outputMalletFormat(tf);
	}
	
	public static void formBoundaryTag(ArrayList<ReviewItem> items) {
		for (ReviewItem item : items) {
			ArrayList<TaggedSentence> tsList = item.sentences();
			String prev = tsList.get(0).tag();
			int i = 1;
			for (; i < tsList.size(); ++i) {
				String cur = tsList.get(i).tag();
				if (!cur.equals(prev)) {
					tsList.get(i-1).setTag("1");
				} else {
					tsList.get(i-1).setTag("0");
				}
				prev = cur;
			}
			tsList.get(i-1).setTag("1");
		}
	}
	
	public static void outputMalletFormat(TileFeature tf) {
		String outputFile = "mallet.txt";
		ArrayList<ReviewItem> items = tf.items();
		//formBoundaryTag(items);
		try {
			FileWriter writer = new FileWriter(outputFile);
			for (ReviewItem item : items) {
				for (TaggedSentence sen : item.sentences()) {
					if (tf.usePreserveWord()) {
						if (mOutputWords == true) {
							String sentence = sen.sentence();
							String[] tokens = sentence.replaceAll("[^\\p{Alnum}\\s]+", "").split("\\s+");
							for (int i = 0; i < tokens.length; ++i) {
								if (tf.isPreserveWord(tokens[i])) {
									writer.write(tokens[i] + " ");
								}
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
