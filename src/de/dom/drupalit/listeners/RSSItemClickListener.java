package de.dom.drupalit.listeners;

import org.mcsoxford.rss.RSSItem;

import de.dom.drupalit.IssueDetail;
import de.dom.drupalit.objects.Issue;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

public class RSSItemClickListener implements OnClickListener {

	private Context context;
	private Issue item;

	public RSSItemClickListener(Context context, Issue item2) {
		this.context = context;
		this.item =item2;
	}

	@Override
	public void onClick(View v) {
		Intent i = new Intent(context,IssueDetail.class);
		IssueDetail.item = item;
		context.startActivity(i);
	}

}
