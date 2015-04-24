package edu.utexas.crawler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ParseHtml {
	public static final int REVIEW_LENGTH_LIMIT = 50;
	public static final String NEW_LINE = System.getProperty("line.separator");
	
	public static void main(String[] args) throws Exception{
		String input = "data/Office.html";
		String output = "data/Office.txt";
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
	
	public static void parse(String input, String output) {
		String reviewPattern = "<div +class=\"tiny-star star-rating-non-editable-container\" *aria-label=\" Rated ([1-5]) stars.*?<div +class=\"review-body\">(.*?)</div>";
		String contentPattern = ".*<span +class=\"review-title\">(.*?)</span> *(.*?)<div +class=\"review-link\".*";
		Pattern reviewSection = Pattern.compile(reviewPattern);
		Pattern reviewItem = Pattern.compile(contentPattern);
		int reviewCount = 0;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(input));
			FileWriter writer = new FileWriter(output);
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
						writer.write(new ReviewItem(rating, title, review) + NEW_LINE);
						++reviewCount;
					} else {
						writer.write("No matches" + NEW_LINE);
					}
					reviewMatcher = reviewMatcher.region(reviewMatcher.end(), reviewMatcher.regionEnd());
				}
			}
			reader.close();
			writer.close();
			System.out.println("Review Count: " + reviewCount);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
