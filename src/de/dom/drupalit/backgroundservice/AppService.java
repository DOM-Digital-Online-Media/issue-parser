package de.dom.drupalit.backgroundservice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import de.dom.drupalit.IssueDetail;
import de.dom.drupalit.R;
import de.dom.drupalit.objects.Issue;

public class AppService extends WakefulIntentService {

	private static final String BASE_URL = "http://drupal.org/project/issues/user";
	public static ArrayList<Issue> feed;
	public static ArrayList<String> notifiedIDs = new ArrayList<String>();
	
	public AppService() {
		super("AppService");
	}

	@Override
	protected void doInBackground(Intent intent) {
		feed = new ArrayList<Issue>();
		CookieSyncManager.createInstance(this);
		CookieManager mgr = CookieManager.getInstance();
		String cookie_string = mgr.getCookie("drupal.org");
		System.out.println("cookie string: " + cookie_string);
		try {
			Document doc = Jsoup.connect(BASE_URL).cookie("drupal.org", cookie_string).get();
			Elements rows = doc.getElementsByTag("table").get(0).getElementsByTag("tbody").get(0).getElementsByTag("tr");
			System.out.println("[NOTIFIER] found feed items: " + rows.size());
			
			for (int i = 0; i < rows.size(); i++) {
				Elements columns = rows.get(i).getElementsByTag("td");

				Issue issue = new Issue();
				parseIssue(issue, columns);
				feed.add(issue);
			}
			for (int i = 0;i<feed.size();i++)
			{
				System.out.println(feed.get(i).title);
				if (feed.get(i).myStatus!=null && (feed.get(i).myStatus.endsWith("updated") || feed.get(i).myStatus.endsWith("new")))
				{
					showNotification(feed.get(i));
				}
			}
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
	}

	public void parseIssue(Issue issue, Elements columns) {
		issue.title = columns.get(1).getElementsByTag("a").get(0).text();
		if (columns.get(1).getElementsByTag("span") != null
				&& columns.get(1).getElementsByTag("span").size() > 0) {
			issue.myStatus = columns.get(1).getElementsByTag("span").get(0)
					.text();
		} else {
			issue.myStatus = "";
		}

		String issueURL = columns.get(1).getElementsByTag("a").get(0)
				.attr("href");
		issue.issueURL = "http://drupal.org" + issueURL;
		issue.id = issueURL.substring(issueURL.lastIndexOf('/') + 1);
		issue.status = columns.get(2).text();
		issue.priority = columns.get(3).text();
		issue.category = columns.get(4).text();
		issue.version = columns.get(5).text();
		issue.replies = columns.get(6).text();
		issue.lastUpdated = columns.get(7).text();
		issue.assignedTo = columns.get(8).text();
		issue.created = columns.get(9).text();
	}
	private void showNotification(Issue issue) {
		if (notifiedIDs.contains(issue.id))
		{
			return;
		}
		System.out.println("issue has been updated: " + issue.id);
		NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Intent intent = new Intent(this, IssueDetail.class);
		Notification notification = new Notification(R.drawable.ic_notify,
				"Drupal Issue Tracker", System.currentTimeMillis());
		intent.putExtra("issueID", issue.id);
		notification.setLatestEventInfo(AppService.this,
				"Issue #"+issue.id+" updated!", issue.title,
				PendingIntent.getActivity(this.getBaseContext(), new Random().nextInt(), intent,
						0));
		notification.defaults = Notification.DEFAULT_ALL;
		manager.notify(Integer.parseInt(issue.id), notification);
		notifiedIDs.add(issue.id);
	}
}