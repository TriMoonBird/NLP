package edu.utexas.seg;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

import uk.ac.man.cs.choif.nlp.doc.basic.RawText;
import uk.ac.man.cs.choif.nlp.doc.basic.Sentences;
import uk.ac.man.cs.choif.nlp.surface.Stemmer;
import uk.ac.man.cs.choif.nlp.surface.Stopword;
import edu.utexas.wordnet.WordNet;

/**
 * An implementation of Marti Hearst's text tiling algorithm.
 * Creation date: (07/12/99 01:31:36)
 * Modified by: Yuepeng Wang
 * @author: Freddy Choi
 */
public class TextTile {
	//======================
	protected WordNet mWordNet;
	//======================
	
	/* Program parameters */
	protected int w = 100; // Size of the sliding window
	protected int s = 10; // Step size

	/* Data sets */
	protected Sentences C = new RawText(); // A collection for segmentation
	// Modification from N Hernandez
	protected Stopword S = null ;//new Stopword(); // A stopword list for noise reduction


	/* Token -> stem dictionary */
	protected Hashtable stemOf = new Hashtable(); // Token -> stem

	/* Similarity scores and the corresponding locations */
	protected float[] sim_score = new float[0];
	protected int[] site_loc = new int[0];

	/* Depth scores */
	protected float[] depth_score = new float[0];

	/* Segment boundaries */
	public Vector boundaries = new Vector();
	/**
	 * Segment a collection
	 * Creation date: (07/12/99 01:33:32)
	 * @param c uk.ac.man.cs.choif.nlp.struct.RawText
	 * @param s uk.ac.man.cs.choif.nlp.stopword.Stopword
	 * @param w Window size
	 * @param step Step size
	 */
	public TextTile(Sentences sentence, Stopword stopwords, int window, int step) {
		C = sentence;
		S = stopwords;
		this.w = window;
		this.s = step;
		//=============
		mWordNet = new WordNet();
		//=============
		preprocess();
		similarityDetermination();	// Compute similarity scores
		depthScore();				// Compute depth scores using the similarity scores
		boundaryIdentification();	// Identify the boundaries
	}
	/**
	 * Add a term to a block
	 * Creation date: (07/12/99 01:41:24)
	 * @param term java.lang.String
	 * @param B java.util.Hashtable
	 */
	private void blockAdd(final String term, Hashtable B) {
		Integer freq = (Integer) B.get(term);

		if (freq == null) freq = new Integer(1);
		else freq = new Integer(freq.intValue() + 1);

		B.put(term, freq);
	}
	/*==================================
	private float blockCosine(final Hashtable<String, Integer> B1, final Hashtable<String, Integer> B2) {
		int equiCount = 0;
		for (String word : B1.keySet()) {
			//
			// Equal word number: using WordNet
			HashSet<String> synset = new HashSet<String>(mWordNet.getAllRelatedWords(word));
			synset.retainAll(B2.keySet());
			//System.out.println("B1\n" + B1);
			//System.out.println("B2\n" + B2);
			//System.out.println("synset\n" + synset);
			if (!synset.isEmpty()) {
				if (!B2.contains(word)) equiCount += B1.get(word);
				else equiCount += Math.min(B1.get(word), B2.get(word));
			}
			//
			/*
			// Equal word number: without WordNet
			if (B2.containsKey(word)) {
				equiCount += Math.min(B1.get(word), B2.get(word));
			}
			//
		}
		float sim = ((float) equiCount) / w;
		return sim;
	}
	*/
	//==================================
	/**
	 * Compute the cosine similarity measure for two blocks
	 * Creation date: (07/12/99 01:49:16)
	 * @return float
	 * @param B1 java.util.Hashtable
	 * @param B2 java.util.Hashtable
	 */
	private float blockCosine(final Hashtable B1, final Hashtable B2) {
		// 1. Declare variables
		int W; // Weight of a term (temporary variable)
		int sq_b1 = 0; // Sum of squared weights for B1
		int sq_b2 = 0; // Sum of squared weights for B2
		int sum_b = 0; // Sum of product of weights for common terms in B1 and B2

		// 2. Compute the squared sum of term weights for B1
		for (Enumeration e=B1.elements(); e.hasMoreElements();) {
			W = ((Integer) e.nextElement()).intValue();
			sq_b1 += (W * W);
		}

		// 3. Compute the squared sum of term weights for B2 
		for (Enumeration e=B2.elements(); e.hasMoreElements();) {
			W = ((Integer) e.nextElement()).intValue();
			sq_b2 += (W * W);
		}

		// 4. Compute sum of term weights for common terms in B1 and B2

		// 4.1. Union of terms in B1 and B2 
		Hashtable union = new Hashtable(B1.size() + B2.size());
		for (Enumeration e=B1.keys(); e.hasMoreElements();) union.put((String) e.nextElement(), new Boolean(true));
		for (Enumeration e=B2.keys(); e.hasMoreElements();) union.put((String) e.nextElement(), new Boolean(true));

		// 4.2. Compute sum 
		Integer W1; // Weight of a term in B1 (temporary variable)
		Integer W2; // Weight of a term in B2 (temporary variable)
		String term; // A term (temporary variable)
		for (Enumeration e=union.keys(); e.hasMoreElements();) {
			term = (String) e.nextElement();
			W1 = (Integer) B1.get(term);
			W2 = (Integer) B2.get(term);
			if (W1!=null && W2!=null) sum_b += (W1.intValue() * W2.intValue());
		}
		
		// 5. Compute similarity
		float sim;
		sim = (float) sum_b / (float) Math.sqrt(sq_b1 * sq_b2);

		return sim;
	}
	//
	/**
	 * Remove a term from the block
	 * Creation date: (07/12/99 01:46:39)
	 * @param term java.lang.String
	 * @param B java.util.Hashtable
	 */
	private void blockRemove(final String term, Hashtable B) {
		Integer freq = (Integer) B.get(term);

		if (freq != null) {
			if (freq.intValue() == 1) B.remove(term);
			else B.put(term, new Integer(freq.intValue() - 1));
		}
	}
	/**
	 * Identify the boundaries
	 * Creation date: (07/12/99 07:05:04)
	 */
	private void boundaryIdentification() {
		/* Declare variables */
		float mean = 0; // Mean depth score
		float sd = 0; // S.D. of depth score
		float threshold; // Threshold to use for determining boundaries
		int neighbours = 3; // The area to check before assigning boundary

		/* Compute mean and s.d. from depth scores */
		for (int i=depth_score.length; i-->0;) mean += depth_score[i];
		mean = mean / depth_score.length;

		for (int i=depth_score.length; i-->0;) sd += Math.pow(depth_score[i] - mean, 2);
		sd = sd / depth_score.length;

		/* Compute threshold */
		threshold = mean - sd / 2;

		/* Identify segments in pseudo-sentence terms */
		Vector pseudo_boundaries = new Vector();
		boolean largest = true; // Is the potential boundary the largest in the local area?
		for (int i=depth_score.length; i-->0;) {

			/* Found a potential boundary */
			if (depth_score[i] >= threshold) {

				/* Check if the nearby area has anything better */
				largest = true;

				/* Scan left */
				for (int j=neighbours; largest && j>0 && (i-j)>0; j--) {
					if (depth_score[i-j] > depth_score[i]) largest=false;
				}

				/* Scan right */
				for (int j=neighbours; largest && j>0 && (i+j)<depth_score.length; j--) {
					if (depth_score[i+j] > depth_score[i]) largest=false;
				}

				/* Lets make the decision */
				if (largest) pseudo_boundaries.addElement(new Integer(site_loc[i]));
			}
		}

		/* Convert pseudo boundaries into real boundaries.
	We use the nearest true boundary. */

		/* Convert real boundaries into array for faster access */
		int[] true_boundaries = new int[C.sentenceBoundaries().size()];
		for (int i=true_boundaries.length; i-->0;) true_boundaries[i]= ((Integer) C.sentenceBoundaries().elementAt(i)).intValue();

		int pseudo_boundary;
		int distance; // Distance between pseudo and true boundary
		int smallest_distance; // Shortest distance
		int closest_boundary; // Nearest real boundary
		for (int i=pseudo_boundaries.size(); i-->0;) {
			pseudo_boundary = ((Integer) pseudo_boundaries.elementAt(i)).intValue();

			/* This is pretty moronic, but it works. Can definitely be improved */
			smallest_distance = Integer.MAX_VALUE;
			closest_boundary = true_boundaries[0];
			for (int j=true_boundaries.length; j-->0;) {
				distance = Math.abs(true_boundaries[j] - pseudo_boundary);
				if (distance <= smallest_distance) {
					smallest_distance = distance;
					closest_boundary = true_boundaries[j];
				}
			}

			boundaries.addElement(new Integer(closest_boundary));
		}
	}
	/**
	 * Compute depth score after applying similarityDetermination()
	 * Creation date: (07/12/99 06:54:32)
	 */
	private void depthScore() {
		/* Declare variables */
		float maxima = 0; // Local maxima
		float dleft = 0; // Difference for the left side
		float dright = 0; // Difference for the right side

		/* For each position, compute depth score */
		depth_score = new float[sim_score.length];
		for (int i=sim_score.length; i-->0;) {

			/* Scan left */
			maxima = sim_score[i];
			for (int j=i; j>0 && sim_score[j] >= maxima; j--) maxima = sim_score[j];
			dleft = maxima - sim_score[i];

			/* Scan right */
			maxima = sim_score[i];
			for (int j=i; j<sim_score.length && sim_score[j] >= maxima; j++) maxima = sim_score[j];
			dright = maxima - sim_score[i];

			/* Declare depth score */
			depth_score[i] = dleft + dright;
		}
	}
	
	public ArrayList<ArrayList<String> > genTile(Sentences c, Vector seg) {
		ArrayList<ArrayList<String> > topics = new ArrayList<ArrayList<String> >();
		Vector text = c.tokens(); // The text
		Vector sentence = c.sentenceBoundaries(); // Sentence boundaries
		String line;
		int start, end; // Sentence boundaries

		ArrayList<String> topic = new ArrayList<String>();
		for (int i = 1; i < sentence.size(); i++) {
			// Get sentence boundaries
			start = ((Integer) sentence.elementAt(i-1)).intValue();
			end = ((Integer) sentence.elementAt(i)).intValue();
			// If start is a topic boundary, create a new topic
			if (seg.contains(new Integer(start))) {
				topics.add(topic);
				topic = new ArrayList<String>();
			}
			// add one sentence to a topic
			line = "";
			for (int j = start; j < end; j++) {
				line += (text.elementAt(j) + " ");
			}
			topic.add(line.trim());
		}
		topics.add(topic);
		return topics;
	}
	
	/**
	 * Generate text output with topic boundary markers.
	 * Creation date: (07/12/99 07:39:00)
	 */
	public void genOutput(Sentences c, Vector seg) {
		/* Declare variables */
		Vector text = c.tokens(); // The text
		Vector sentence = c.sentenceBoundaries(); // Sentence boundaries
		String line;
		int start, end; // Sentence boundaries

		/* The implicit boundary at the beginning of the file */
		System.out.println("==========");

		/* Print all the sentences */
		for (int i=1; i<sentence.size(); i++) {

			/* Get sentence boundaries */
			start = ((Integer) sentence.elementAt(i-1)).intValue();
			end = ((Integer) sentence.elementAt(i)).intValue();

			/* If start is a topic boundary, print marker */
			if (seg.contains(new Integer(start))) System.out.println("==========");

			/* Print a sentence */
			line = "";
			for (int j=start; j<end; j++) line += (text.elementAt(j) + " ");
			System.out.println(line.trim());
		}

		/* The implicit boundary at the end of the file */
		System.out.println("==========");
	}
	/**
	 * Decide whether word i is worth using as feature for segmentation.
	 * Creation date: (07/12/99 23:39:51)
	 * @return boolean
	 * @param i int
	 */
	private boolean include(int i) {
		/* Noise reduction by filtering out everything but nouns and verbs - 
	Best but requires POS tagging
	String pos = (String) C.pos.elementAt(i);
	return (pos.startsWith("N") || pos.startsWith("V")); */

		/* Noise reduction by stopword removal - OK */
		String token = (String) C.tokens().elementAt(i);
		return !S.isStopword(token.toLowerCase());

		/* No noise reduction -- Worst
	return true; */
	}

	/**
	 * Perform some preprocessing to save execution time
	 * Creation date: (07/12/99 03:21:34)
	 */
	private void preprocess() {
		/* Declare variables */
		Vector text = C.tokens(); // Text of the collection
		String token; // A token

		/* Construct a dictionary of tokens */
		for (int i=text.size(); i-->0;) {
			token = (String) text.elementAt(i);
			stemOf.put(token, new Integer(0));
		}

		/* Complete mapping token -> stem */
		for (Enumeration e=stemOf.keys(); e.hasMoreElements();) {
			token = (String) e.nextElement();
			stemOf.put(token, Stemmer.stemOf(token));
		}
	}
	/**
	 * Compute the similarity score.
	 * Creation date: (07/12/99 03:17:31)
	 */
	private void similarityDetermination() {
		/* Declare variables */
		Vector text = C.tokens(); // The source text
		Hashtable left = new Hashtable(); // Left sliding window
		Hashtable right = new Hashtable(); // Right sliding window
		Vector score = new Vector(); // Scores
		Vector site = new Vector(); // Locations

		/* Initialise windows */
		for (int i=w; i-->0;) blockAdd((String) stemOf.get((String) text.elementAt(i)), left);
		for (int i=w*2; i-->w;) blockAdd((String) stemOf.get((String) text.elementAt(i)), right);

		/* Slide window and compute score */
		final int end = text.size() - w; // Last index to check
		String token; //  A stem
		int step=0; // Step counter
		int i; // Counter

		for (i=w; i<end; i++) {
			/* Compute score for a step */
			if (step == 0) {
				score.addElement(new Float(blockCosine(left, right)));
				site.addElement(new Integer(i));
				step = s;
			}

			/* Remove word which is at the very left of the left window */
			if (include(i-w)) {
				blockRemove((String) stemOf.get((String) text.elementAt(i-w)), left);
			}

			/* Add current word to the left window and remove it from the right window */
			if (include(i)) {
				token = (String) text.elementAt(i);
				blockAdd((String) stemOf.get(token), left);
				blockRemove((String) stemOf.get(token), right);
			}

			/* Add the first word after the very right of the right window */
			if (include(i+w)) {
				blockAdd((String) stemOf.get((String) text.elementAt(i+w)), right);
			}

			step--;
		}
		/* Compute score for the last step */
		if (step == 0) {
			score.addElement(new Float(blockCosine(left, right)));
			site.addElement(new Integer(i));
			step = s;
		}

		/* Smoothing with a window size of 3 
		sim_score = new float[score.size()-2];
		site_loc = new int[site.size()-2];
		for (int j=0; j<sim_score.length; j++) {
			sim_score[j] = (((Float) score.elementAt(j)).floatValue() + ((Float) score.elementAt(j+1)).floatValue() + ((Float) score.elementAt(j+2)).floatValue()) / 3;
			site_loc[j] = ((Integer) site.elementAt(j+1)).intValue();
		}
		//====================================
		*/
		sim_score = new float[score.size()];
		site_loc = new int[site.size()];
		for (int j=0; j<sim_score.length; j++) {
			sim_score[j] = ((Float) score.elementAt(j)).floatValue();
			site_loc[j] = ((Integer) site.elementAt(j)).intValue();
		}
		//
	}
}
