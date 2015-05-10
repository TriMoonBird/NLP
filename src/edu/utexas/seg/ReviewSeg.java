package edu.utexas.seg;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import uk.ac.man.cs.choif.extend.Argx;
import uk.ac.man.cs.choif.extend.Debugx;
import uk.ac.man.cs.choif.nlp.doc.basic.RawText;
import uk.ac.man.cs.choif.nlp.surface.Stopword;
import edu.utexas.crawler.ReviewItem;

class ReviewSeg {
	public static void main(String[] args) {
		Debugx.header("This is JTextTile.");

		Argx arg = new Argx(args);
		int window = arg.get("-w", 2, "Window size");
		int step = arg.get("-s", 20, "Step size");
		String stopwordList = "stopwords.txt";
		String reviewFile = "review.txt";
		arg.displayHelp();

		ReviewReader reader = new ReviewReader(reviewFile);
		ArrayList<ReviewItem> items = reader.read();
		
		try {
			Debugx.msg("Window", window);
			Debugx.msg("Step", step);
			Debugx.msg("Stopword list", stopwordList);

			// Load data
			Stopword stopwords = new Stopword(new java.io.File(stopwordList));

			String review = items.get(3).sentenceToString();
			RawText rawtext = new RawText(new ByteArrayInputStream(review.getBytes(StandardCharsets.UTF_8)));

			// A bit of error checking
			Debugx.msg("Collection", rawtext.text.size());
			if (rawtext.text.size() <= (window * 2)) {
				Debugx.msg("Fatal error", "Window size (" + window + " * 2 = " + (window * 2) + 
						") larger then collection (" + rawtext.text.size() + ")");
				System.exit(1);
			}

			JTextTile texttile = new JTextTile(rawtext, stopwords, window, step);
			ArrayList<ArrayList<String> > topics = texttile.genTile(rawtext, texttile.boundaries);
			printTopics(topics);
		}
		catch (Exception e) {
			Debugx.handle(e);
			System.exit(1);
		}
	}
	
	public static void printTopics(ArrayList<ArrayList<String> > topics) {
		for (ArrayList<String> topic : topics) {
			System.out.println("==========");
			for (String sentence : topic) {
				System.out.println(sentence);
			}
		}
		System.out.println("==========");
	}
}
