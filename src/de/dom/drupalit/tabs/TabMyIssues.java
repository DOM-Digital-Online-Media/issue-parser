package de.dom.drupalit.tabs;

import org.jsoup.select.Elements;

import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.TextView;
import de.dom.drupalit.R;
import de.dom.drupalit.objects.Issue;

public class TabMyIssues extends IssueListActivity {

	public static TabMyIssues instance;
	private boolean cleardAfterLogout;

	public void onCreate(Bundle icicle) {
		instance = this;
		baseURL = "http://drupal.org/project/issues/user";
		defaultURL = "http://drupal.org/project/issues/user?page=CUR_PAGE&text=&projects=&status=Open&priorities=All&categories=SEL_CAT";
		showMyStatus = true;

		super.onCreate(icicle);

		btnSearchSwitch.setVisibility(View.INVISIBLE);
		imgSearchSwitchDivider.setVisibility(View.INVISIBLE);
		txtHeader.setText(R.string.tab_name_your_issues);
		txtHeader.setPadding(60, 0, 0, 0);
	}

	@Override
	protected void onResume() {
		System.out.println("on resume my issues");
		CookieSyncManager.createInstance(this);
		CookieManager mgr = CookieManager.getInstance();
		String cookie_string = mgr.getCookie("drupal.org");
		if (cleardAfterLogout || cookie_string == null || !cookie_string.contains("authenticated")) {
			((TextView) getListView().getEmptyView())
					.setText(R.string.login_for_my_issues);
			prog.setVisibility(View.GONE);
			feed.clear();
			adapter.notifyDataSetChanged();
		}
		cleardAfterLogout = false;
		super.onResume();
	}

	@Override
	protected int getEmptyListString() {
		return R.string.empty_list_my_issues;
	}

	@Override
	protected int getEmptyCategoryListString() {
		return R.string.empty_list_category;
	}

	@Override
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
		super.parseIssue(issue, columns);
	}

	public boolean remove(Issue item) {
		if (feed != null) {
			boolean result = feed.remove(item);
			if (adapter != null) {
				adapter.notifyDataSetChanged();
				adapter.notifyDataSetInvalidated();
			}
			return result;
		}
		return false;
	}

	public void clearAfterLogout() {
		if (feed != null) {
			((TextView) getListView().getEmptyView())
					.setText(R.string.login_for_my_issues);
			prog.setVisibility(View.GONE);
			feed.clear();
			adapter.notifyDataSetChanged();
			cleardAfterLogout = true;
		}
	}

}
