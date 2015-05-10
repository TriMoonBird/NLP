package edu.utexas.crawler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ParseHtml {
	public static final int OUTPUT_REVIEW_NUMBER = 60;
	public static final int REVIEW_LENGTH_LIMIT = 150;
	public static final String NEW_LINE = System.getProperty("line.separator");
	
	public static void main(String[] args) throws Exception{
		String name = "SwiftKey";
		String input = "data/productivity/" + name + ".html";
		String output = "data/productivity/" + name + ".txt";
		parse(input, output);
	}
	
	public static String processEnd(String text) {
		final String END_REGEX = "(.*[\\.\\!\\?]+) *$";
		Pattern pattern = Pattern.compile(END_REGEX);
		Matcher matcher = pattern.matcher(text);
		if (!matcher.matches()) {
			text = text.trim() + ".";
		}
		return text;
	}
	
	public static void saveReviewItem(FileWriter writer, ArrayList<ReviewItem> reviews, int number) throws IOException {
		Collections.shuffle(reviews);
		for (int i = 0; i < number; ++i) {
			if (i >= reviews.size())
				break;
			writer.write(reviews.get(i) + NEW_LINE);
		}
	}
	
	public static void saveReviewItem(FileWriter writer, ArrayList<ReviewItem> reviews) throws IOException {
		for (ReviewItem review : reviews) {
			writer.write(review + NEW_LINE);
		}
	}
	
	public static void parse(String input, String output) {
		ArrayList<ReviewItem> reviews = new ArrayList<ReviewItem>();
		String reviewPattern = "<div +class=\"tiny-star star-rating-non-editable-container\" *aria-label=\" Rated ([1-5]) stars.*?<div +class=\"review-body\">(.*?)</div>";
		String contentPattern = ".*<span +class=\"review-title\">(.*?)</span> *(.*?)<div +class=\"review-link\".*";
		Pattern reviewSection = Pattern.compile(reviewPattern);
		Pattern reviewItem = Pattern.compile(contentPattern);
		int reviewCount = 0;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(input));
			String str;
			while ((str = reader.readLine()) != null) {
				Matcher reviewMatcher = reviewSection.matcher(str);
				while (reviewMatcher.find()) {
					Matcher contentMatcher = reviewItem.matcher(reviewMatcher.group(2));
					if (contentMatcher.matches()) {
						String rating = reviewMatcher.group(1);
						String title = contentMatcher.group(1);
						String review = processEnd(contentMatcher.group(2));
						if (review.length() < REVIEW_LENGTH_LIMIT) 
							continue;
						reviews.add(new ReviewItem(rating, title, review));
						++reviewCount;
					} else {
						System.out.println("No matches: " + reviewCount + NEW_LINE);
					}
					reviewMatcher = reviewMatcher.region(reviewMatcher.end(), reviewMatcher.regionEnd());
				}
			}
			FileWriter writer = new FileWriter(output);
			saveReviewItem(writer, reviews, OUTPUT_REVIEW_NUMBER);
			reader.close();
			writer.close();
			System.out.println("Review Count: " + reviewCount);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
