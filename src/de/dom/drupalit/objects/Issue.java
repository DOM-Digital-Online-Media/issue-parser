package de.dom.drupalit.objects;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.mcsoxford.rss.RSSItem;

import android.content.Context;
import android.text.Html;
import de.dom.drupalit.R;

public class Issue {
	
	public String id;
	public String author;
	public Date date;
	public String title;
	public String issue;
	public String issueURL;
	public ArrayList<Comment> comments;
	public String status;
	public String priority;
	public String category;
	public String version;
	public String component;
	public String replies;
	public String lastUpdated;
	public String assignedTo;
	public String created;
	public boolean isLoadingItem = false;
	public String myStatus;
	public String summary;
	public String followerCount;
	
	public Issue(RSSItem item) {
		this.id = item.getGuid().substring(0, item.getGuid().indexOf(" "));
		this.author = item.getCreator();
		this.date = item.getPubDate();
		this.title = item.getTitle();
		this.issue = Html.fromHtml(item.getDescription()).toString();
		this.issueURL = item.getComments();
	}
	
	public Issue() {
		
	}

	public String localizedCreationString(Context context) {
		SimpleDateFormat sdf = new SimpleDateFormat(context.getString(R.string.date_format));
		return String.format(context.getString(R.string.list_item_date_author_string), this.author, sdf.format(this.date));
	}

}
