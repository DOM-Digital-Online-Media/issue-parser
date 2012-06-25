package de.dom.drupalit;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.text.format.DateFormat;

import de.dom.drupalit.objects.Comment;
import de.dom.drupalit.objects.Issue;

public class IssueParser {

	public static String getFollowLink(Document result) {
		try {

			String followLink = result
					.getElementsByClass("flag-project-issue-follow").first()
					.getElementsByTag("a").first().attr("href");
			followLink = "http://drupal.org" + followLink;
			return followLink;

		} catch (Exception e) {
			// not logged in
		}
		return null;
	}

	public static ArrayList<Comment> getComments(Document result, SimpleDateFormat sdf) {
		Element comments = result.getElementById("comments");
		Elements commentsArray = comments.getElementsByClass("comment");
		ArrayList<Comment> comArray = new ArrayList<Comment>();
		for (int i = 0; i < commentsArray.size(); i++) {
			Comment c = new Comment();
			c.date = commentsArray.get(i).getElementsByClass("submitted")
					.get(0).getElementsByTag("em").text();
			try {
				
				SimpleDateFormat formatter;
				Date date;
				// May 2, 2012 at 11:29pm
				formatter = new SimpleDateFormat("MMMM dd, yyyy 'at' KK:mma",Locale.ENGLISH);
				date = (Date) formatter.parse(c.date);
				c.date = sdf.format(date);

			} catch (ParseException e) {
				e.printStackTrace();
			}

			c.author = commentsArray.get(i).getElementsByClass("submitted")
					.get(0).getElementsByTag("a").text();

			Element detailsContent = commentsArray.get(i)
					.getElementsByClass("content").get(0)
					.getElementsByClass("clear-block").get(0);

			if (detailsContent.getElementsByClass("project-issue").size() > 0) {
				c.comment = detailsContent.getElementsByClass("project-issue")
						.text();
			} else if (commentsArray.get(i).getElementsByClass("content")
					.get(0).getElementsByClass("clear-block").get(0)
					.getElementsByTag("p").size() > 0) {
				c.comment = commentsArray.get(i).getElementsByClass("content")
						.get(0).getElementsByClass("clear-block").get(0)
						.getElementsByTag("p").get(0).text();
			} else if (commentsArray.get(i).getElementsByClass("content")
					.get(0).getElementsByClass("clear-block").get(0).text()
					.contains("Attachment")
					&& commentsArray.get(i).getElementsByClass("content")
							.get(0).getElementsByClass("clear-block").get(0)
							.getElementsByTag("a").size() > 0) {
				c.comment = "Added attachment: "
						+ commentsArray.get(i).getElementsByClass("content")
								.get(0).getElementsByClass("clear-block")
								.get(0).getElementsByTag("a").get(0).text();
			} else {
				c.comment = commentsArray.get(i).getElementsByClass("content")
						.get(0).getElementsByClass("clear-block").get(0).text();
			}

			// System.out.println("found date: " + c.comment);
			comArray.add(c);
		}
		return comArray;
	}

	public static void getIssue(Issue item, Document result) {
		item.summary = result.getElementsByClass("node-content").get(0)
				.getElementsByTag("p").text();
		item.followerCount = result
				.getElementsByClass("project-issue-follow-count").get(0).text();
		System.out.println("follower count: " + item.followerCount);
		item.followerCount = item.followerCount.substring(0,
				item.followerCount.indexOf(' '));
		System.out.println("follower count: " + item.followerCount);

	}

}
