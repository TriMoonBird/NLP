package edu.utexas.seg;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import uk.ac.man.cs.choif.extend.Argx;
import uk.ac.man.cs.choif.extend.Debugx;
import uk.ac.man.cs.choif.nlp.doc.basic.RawText;
import uk.ac.man.cs.choif.nlp.surface.Stopword;
import edu.utexas.crawler.ReviewItem;

class ReviewSeg {
	public static void main(String[] args) {
		Argx arg = new Argx(args);
		int window = arg.get("-w", 10, "Window size");
		int step = arg.get("-s", 10, "Step size");
		String stopwordList = "stopwords.txt";
		String reviewFile = "review.txt";
		//arg.displayHelp();
		performSegment(reviewFile, stopwordList, window, step);
	}
	
	public static void performSegment(String reviewFile, String stopwordList, int window, int step) {
		ReviewReader reader = new ReviewReader(reviewFile);
		ArrayList<ReviewItem> items = reader.read();
		Stopword stopwords = new Stopword(new File(stopwordList));
		try {
			Debugx.msg("Window", window);
			Debugx.msg("Step", step);
			Debugx.msg("Stopword list", stopwordList);

			SegEvaluation evaluation = new SegEvaluation(items);
			
			for (ReviewEvaluation eval : evaluation.evalItems()) {
				String review = eval.reviewItem().sentenceToString();
				RawText rawtext = new RawText(new ByteArrayInputStream(review.getBytes(StandardCharsets.UTF_8)));

				// A bit of error checking
				Debugx.msg("Collection", rawtext.text.size());
				if (rawtext.text.size() <= (window * 2)) {
					Debugx.msg("Fatal error", "Window size (" + window + " * 2 = " + (window * 2) + 
							") larger then collection (" + rawtext.text.size() + ")");
					System.exit(1);
				}

				TextTile texttile = new TextTile(rawtext, stopwords, window, step);
				ArrayList<ArrayList<String> > topics = texttile.genTile(rawtext, texttile.boundaries);
				printTopics(topics);
				eval.setSegment(formBoundaries(topics));
			}
			
			evaluation.evaluate();
			evaluation.printResults();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static ArrayList<Integer> formBoundaries(ArrayList<ArrayList<String> > topics) {
		ArrayList<Integer> boundaries = new ArrayList<Integer>();
		int curPos = -1;
		for (ArrayList<String> topic : topics) {
			curPos += topic.size();
			boundaries.add(curPos);
		}
		return boundaries;
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
