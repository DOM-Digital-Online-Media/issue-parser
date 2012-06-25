package de.dom.drupalit;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import de.dom.drupalit.backgroundservice.AppService;
import de.dom.drupalit.objects.Comment;
import de.dom.drupalit.objects.Issue;
import de.dom.drupalit.tabs.TabMyIssues;
import de.dom.drupalit.tabs.TabProfile;

public class IssueDetail extends Activity {
	public static Issue item;
	private WebView commentsView;
	private LinearLayout layoutComments;
	private String pageData;
	String followLink = null;
	private Button btnFollow;
	private ProgressBar progFollow;
	private SimpleDateFormat sdf;
	private ScrollView scrollContainer;
	private RelativeLayout loadingContainer;
	private ArrayList<Comment> comments;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rss_detail);
		sdf = new SimpleDateFormat(getString(R.string.date_format));

		scrollContainer = (ScrollView) findViewById(R.id.scrollView1);
		loadingContainer = (RelativeLayout) findViewById(R.id.loadingContainer);
		scrollContainer.setVisibility(View.GONE);
		loadingContainer.setVisibility(View.VISIBLE);

		if (getIntent().getExtras() != null
				&& getIntent().getExtras().containsKey("issueID")) {
			String id = getIntent().getExtras().getString("issueID");
			NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			manager.cancel(Integer.parseInt(id));
			if (AppService.feed == null) {
				finish();
			}
			else
			{
				for (int i = 0; i<AppService.feed.size();i++)
				{
					Issue tmp = AppService.feed.get(i);
					if (tmp.id.equals(id))
					{
						item = tmp;
					}
				}
				if (item==null)
				{
					finish();
				}
			}
		}

		AppService.notifiedIDs.remove(item.id);
		((TextView) findViewById(R.id.txtIssueNo)).setText("Issue #" + item.id);

		((TextView) findViewById(R.id.txtTitle)).setText(item.title);
		layoutComments = (LinearLayout) findViewById(R.id.linearLayoutComments);
		btnFollow = (Button) findViewById(R.id.button1);
		btnFollow.setEnabled(false);
		progFollow = ((ProgressBar) findViewById(R.id.progressBar2));

		commentsView = (WebView) findViewById(R.id.webViewComments);
		commentsView.setVisibility(View.GONE);
		commentsView.loadUrl(item.issueURL);
		commentsView.getSettings().setJavaScriptEnabled(true);
		commentsView.setWebChromeClient(new WebChromeClient() {

			@Override
			public boolean onJsAlert(WebView view, String url, String message,
					JsResult result) {
				pageData = message;
				parseIssue();
				result.confirm();
				return true;
			}

		});

		commentsView.setWebViewClient(new WebViewClient() {

			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}

			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {

				super.onReceivedError(view, errorCode, description, failingUrl);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				System.out.println("finished url: " + url);
				if (url.contains("/flag/unflag/")) {
					btnFollow.setText(getString(R.string.follow));
					btnFollow.setEnabled(true);
					progFollow.setVisibility(View.GONE);
				} else if (url.contains("/flag/flag/")) {
					((TextView) findViewById(R.id.txtYouAreFollowing))
							.setVisibility(View.VISIBLE);
					btnFollow.setEnabled(true);
					progFollow.setVisibility(View.GONE);
				} else {
					view.loadUrl("javascript:alert(document.body.innerHTML);");
				}

				super.onPageFinished(view, url);
			}
		});

	}

	private void parseIssue() {
		new AsyncTask<Object, Object, Object>() {

			@Override
			protected ArrayList<Comment> doInBackground(Object... params) {
				try {
					Document result = Jsoup.parse(pageData);

					followLink = IssueParser.getFollowLink(result);

					comments = IssueParser.getComments(result,new SimpleDateFormat(getString(R.string.date_format)));

					IssueParser.getIssue(item, result);

				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(Object obj) {

				((TextView) findViewById(R.id.txtDescription))
						.setText(item.summary);
				((TextView) findViewById(R.id.txtFollowers)).setText(String
						.format(getString(R.string.follower_count),
								item.followerCount));

				if (comments != null) {

					layoutComments.removeAllViews();
					for (int i = 0; i < comments.size(); i++) {
						Comment c = comments.get(i);
						LinearLayout commentView = (LinearLayout) getLayoutInflater()
								.inflate(R.layout.comment, null);
						((TextView) commentView
								.findViewById(R.id.txtCommentText))
								.setText(c.comment);
						((TextView) commentView
								.findViewById(R.id.txtNumberComment))
								.setText("#" + (i + 1));
						((TextView) commentView
								.findViewById(R.id.txtAuthorDate))
								.setText(String
										.format(getString(R.string.list_item_date_author_string),
												c.author, c.date));
						layoutComments.addView(commentView);
					}
				}

				if (comments == null || comments.size() == 0) {
					((LinearLayout) findViewById(R.id.linearLayout5))
							.setVisibility(View.INVISIBLE);
					((RelativeLayout) findViewById(R.id.relativeLayout2))
							.setVisibility(View.INVISIBLE);
				}

				progFollow.setVisibility(View.GONE);

				if (followLink != null) {
					TabProfile.loggedIn = true;
					btnFollow.setEnabled(true);
					if (followLink.contains("flag/unflag")) {
						btnFollow.setText(getString(R.string.unfollow));
						((TextView) findViewById(R.id.txtYouAreFollowing))
								.setVisibility(View.VISIBLE);
						if (TabMyIssues.instance != null) {
							TabMyIssues.instance.refresh(0);
						}
					} else {
						btnFollow.setText(getString(R.string.follow));
						((TextView) findViewById(R.id.txtYouAreFollowing))
								.setVisibility(View.GONE);
						System.out.println("removing issue from follow list");
						if (AppService.notifiedIDs!=null)
						{
							AppService.notifiedIDs.remove(item.id);
						}
						if (TabMyIssues.instance != null) {
							TabMyIssues.instance.refresh(0);
						}
					}
					btnFollow.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							progFollow.setVisibility(View.VISIBLE);
							btnFollow.setEnabled(false);
							commentsView.loadUrl(followLink);
						}
					});
				} else {
					btnFollow.setEnabled(true);
					btnFollow.setText(getString(R.string.login_to_follow));
					btnFollow.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							Main.instance.switchToProfile();
							finish();
						}
					});
				}
				scrollContainer.setVisibility(View.VISIBLE);
				loadingContainer.setVisibility(View.GONE);
				super.onPostExecute(obj);
			}

		}.execute();

	}
}
