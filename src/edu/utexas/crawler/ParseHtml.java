package edu.utexas.crawler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ParseHtml {
	private static final String NEW_LINE = System.getProperty("line.separator");
	
	public static void main(String[] args) throws Exception{
		String input = "data/facebook.html";
		String output = "data/facebook.txt";
		parse(input, output);
	}
	
	public static void parse(String input, String output) {
		//String authorPattern = "<span +class=\"author-name\"> <a href=.*>(.*?)</a> *</span>";
		String reviewPattern = "<div +class=\"review-body\">(.*?)</div>";
		String contentPattern = " *<span +class=\"review-title\">(.*?)</span> *(.*?)<div +class=\"review-link\".*";
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
					Matcher contentMatcher = reviewItem.matcher(reviewMatcher.group(1));
					if (contentMatcher.matches()) {
						String title = contentMatcher.group(1);
						String review = contentMatcher.group(2);
						writer.write(new ReviewItem(title, review) + NEW_LINE);
						//System.out.println(title + " " + review);
						++reviewCount;
					} else {
						writer.write("No matches" + NEW_LINE);
						//System.out.println("No matches");
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
