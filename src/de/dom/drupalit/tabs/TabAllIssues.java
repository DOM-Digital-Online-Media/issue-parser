package de.dom.drupalit.tabs;

import java.net.URLEncoder;

import org.jsoup.select.Elements;

import de.dom.drupalit.R;
import de.dom.drupalit.objects.Issue;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.view.inputmethod.InputMethodManager;

public class TabAllIssues extends IssueListActivity {

	@Override
	public void onCreate(Bundle icicle) {
		baseURL = "http://drupal.org/project/issues/drupal?categories=All";
		defaultURL = "http://drupal.org/project/issues/drupal?page=CUR_PAGE&text=&status=Open&priorities=All&categories=SEL_CAT&version=All&component=All";
		showMyStatus = false;
		super.onCreate(icicle);
		btnSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				baseURL = defaultURL.replace("SEL_CAT", "All");
				baseURL = baseURL.replace(
						"text=",
						"text="
								+ URLEncoder.encode(editSearch.getText()
										.toString()));
				refresh(0);
				InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				mgr.hideSoftInputFromWindow(editSearch.getWindowToken(), 0);
			}
		});
		btnSearchSwitch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (searchContainer.getVisibility() == View.GONE) {
					AlphaAnimation fadeIn = new AlphaAnimation(0, 1);
					fadeIn.setDuration(300);
					fadeIn.setFillAfter(true);
					searchContainer.startAnimation(fadeIn);
					searchContainer.setVisibility(View.VISIBLE);
				} else {
					AlphaAnimation fadeOut = new AlphaAnimation(1,0);
					fadeOut.setDuration(300);
					fadeOut.setFillAfter(true);
					fadeOut.setAnimationListener(new AnimationListener() {

						@Override
						public void onAnimationStart(Animation animation) {

						}

						@Override
						public void onAnimationRepeat(Animation animation) {

						}

						@Override
						public void onAnimationEnd(Animation animation) {
							searchContainer.setVisibility(View.GONE);
						}
					});
					searchContainer.startAnimation(fadeOut);

				}
				headerContainer.setBackgroundResource(R.drawable.header);
				headerContainer.invalidate();

			}
		});
		txtHeader.setText(R.string.tab_name_all_issues);
	}

	@Override
	public void parseIssue(Issue issue, Elements columns) {
		issue.title = columns.get(0).text();
		String issueURL = columns.get(0).getElementsByTag("a").get(0)
				.attr("href");
		issue.issueURL = "http://drupal.org" + issueURL;
		issue.id = issueURL.substring(issueURL.lastIndexOf('/') + 1);
		issue.status = columns.get(1).text();
		issue.priority = columns.get(2).text();
		issue.category = columns.get(3).text();
		issue.version = columns.get(4).text();
		issue.component = columns.get(5).text();
		issue.replies = columns.get(6).text();
		issue.lastUpdated = columns.get(7).text();
		issue.assignedTo = columns.get(8).text();
		issue.created = columns.get(9).text();
		super.parseIssue(issue, columns);
	}

}
