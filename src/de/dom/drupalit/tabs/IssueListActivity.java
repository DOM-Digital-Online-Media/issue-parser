package de.dom.drupalit.tabs;

import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import de.dom.drupalit.R;
import de.dom.drupalit.Utils;
import de.dom.drupalit.adapters.FeedAdapter;
import de.dom.drupalit.objects.Issue;

public class IssueListActivity extends ListActivity implements OnScrollListener {

	protected ArrayList<Issue> feed = new ArrayList<Issue>();
	protected ProgressBar prog;
	protected String baseURL = null;
	protected String defaultURL = null;
	private int selectedType = 0;
	private ImageButton btnRefresh;
	private WebView backgroundWeb;
	private int page = 0;
	protected FeedAdapter adapter;
	protected boolean showMyStatus;
	protected RelativeLayout searchContainer;
	protected ImageButton btnSearch;
	protected EditText editSearch;
	protected ImageButton btnSearchSwitch;
	protected ImageView imgSearchSwitchDivider;
	protected RelativeLayout headerContainer;
	protected TextView txtHeader;

	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.tab_all_issues);
		prog = ((ProgressBar) findViewById(R.id.progressBar1));
		btnRefresh = (ImageButton) findViewById(R.id.ImageButton01);
		btnSearchSwitch = (ImageButton) findViewById(R.id.ImageButton02);
		imgSearchSwitchDivider = (ImageView) findViewById(R.id.ImageView02);
		btnRefresh.setVisibility(View.INVISIBLE);
		searchContainer = ((RelativeLayout) findViewById(R.id.relativeLayoutSearch));
		headerContainer = ((RelativeLayout) findViewById(R.id.relativeLayout1));
		searchContainer.setVisibility(View.GONE);
		editSearch = (EditText) findViewById(R.id.editText1);
		btnSearch = (ImageButton) findViewById(R.id.button1);
		txtHeader = (TextView) findViewById(R.id.textView1);
		getListView().getEmptyView().setPadding(20,0,0,20);
		setupWebView();
		refresh(0);
	}

	private void setupWebView() {
		backgroundWeb = new WebView(this);
		backgroundWeb.getSettings().setJavaScriptEnabled(true);
		backgroundWeb.getSettings().setSaveFormData(true);
		backgroundWeb.getSettings().setSavePassword(false);
		backgroundWeb.getSettings().setLoadsImagesAutomatically(false);
		backgroundWeb.setWebChromeClient(new WebChromeClient() {

			@Override
			public boolean onJsAlert(WebView view, String url, String message,
					JsResult result) {
				result.confirm();
				// System.out.println("msg: " + message);

				if (message == null || message.equals("undefined")) {
					return true;
				}
				if (message.equals("EMPTY_LIST")) {
					System.out.println("empty list...");
					if (feed != null) {
						feed.clear();
					}
					prog.setVisibility(View.GONE);
					btnRefresh.setVisibility(View.VISIBLE);
					setFeedIsLoadedState();
					if (!TabProfile.loggedIn && IssueListActivity.this instanceof TabMyIssues)
					{
						((TextView) getListView().getEmptyView())
						.setText(R.string.login_for_my_issues);
					}
					else if (selectedType!=0)
					{
						((TextView) getListView().getEmptyView())
						.setText(getEmptyCategoryListString());	
					}
					else
					{
						((TextView) getListView().getEmptyView())
						.setText(getEmptyListString());
					}
					
					return true;
				}
				message = "<html><body><table>" + message
						+ "</table></body></html>";
				Document doc = Jsoup.parse(message);
				Elements rows = doc.getElementsByTag("tr");
				System.out.println("found rows: " + rows.size());
				
				if (feed != null && page > 0) {
					System.out.println("removing loading item");
					feed.remove(feed.size() - 1);
					adapter.notifyDataSetChanged();
				}
				ArrayList<Issue> tmpFeed = new ArrayList<Issue>();
				for (int i = 0; i < rows.size(); i++) {
					Elements columns = rows.get(i).getElementsByTag("td");

					Issue issue = new Issue();
					parseIssue(issue, columns);
					tmpFeed.add(issue);
				}
				
				if (feed != null && page == 0) {
					System.out.println("clearing feed");
					feed.clear();
				}
				
				feed.addAll((ArrayList<Issue>)tmpFeed.clone());
				
				System.out.println("new items added to feed: " + feed.size());
				if (page == 0) {
					adapter = new FeedAdapter(IssueListActivity.this,
							R.layout.feed_list_row);
					adapter.setFeed(feed);
					adapter.setShowMyStatus(showMyStatus);
					getListView().setAdapter(adapter);
				} else {
					adapter.notifyDataSetChanged();
				}

				

				prog.setVisibility(View.GONE);
				btnRefresh.setVisibility(View.VISIBLE);
				setFeedIsLoadedState();
				getListView().setOnScrollListener(IssueListActivity.this);
				((TextView) getListView().getEmptyView())
				.setText(getEmptyListString());
				return true;
			}

		});

		backgroundWeb.setWebViewClient(new WebViewClient() {

			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				System.out.println("loading url: " + url);
				view.loadUrl(url);
				return true;
			}

			@Override
			public void onReceivedSslError(WebView view,
					SslErrorHandler handler, SslError error) {
				handler.proceed();
				return;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				Utils.removeLoadingDialog();
				System.out.println("loaded url: " + url);
				view.loadUrl("javascript:if (document.getElementsByClassName('view-empty').length>0){alert('EMPTY_LIST');};");	
				
				view.loadUrl("javascript:alert(document.getElementsByTagName('table')[1].getElementsByTagName('tbody')[0].innerHTML);");

				super.onPageFinished(view, url);
			}

		});
	}

	public void refresh(int page) {
		System.out.println("refreshing page: " + page);
		getListView().setOnScrollListener(null);
		((TextView) getListView().getEmptyView()).setText(R.string.loading);
		backgroundWeb.loadUrl(baseURL);

		if (page == 0) {
			prog.setVisibility(View.VISIBLE);
			btnRefresh.setVisibility(View.INVISIBLE);
			IssueListActivity.this.page = 0;
			if (feed != null) {
				feed.clear();
				adapter = new FeedAdapter(this, R.layout.feed_list_row);
				adapter.setFeed(feed);
				adapter.setShowMyStatus(showMyStatus);
				getListView().setAdapter(adapter);
			}
		} else {
			Issue loadingItem = new Issue();
			loadingItem.isLoadingItem = true;
			feed.add(loadingItem);
			adapter.notifyDataSetChanged();
		}

	}

	protected int getEmptyListString() {
		return R.string.empty_list;
	}

	private void setFeedIsLoadedState() {

		ImageButton btnCategory = (ImageButton) findViewById(R.id.imageButton1);
		btnRefresh.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				refresh(0);
			}
		});
		btnCategory.setOnClickListener(new OnClickListener() {

			private AlertDialog alert;

			@Override
			public void onClick(View v) {
				final CharSequence[] items = { getString(R.string.any),
						getString(R.string.bug_reports),
						getString(R.string.tasks),
						getString(R.string.feature_requests),
						getString(R.string.support_requests) };

				AlertDialog.Builder builder = new AlertDialog.Builder(
						IssueListActivity.this);
				builder.setTitle(R.string.filter_title);
				builder.setSingleChoiceItems(items, selectedType,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int item) {
								String selCat = "";
								if (item == 0) {
									selCat = "All";
								} else if (item == 1) {
									selCat = "bug";
								} else if (item == 2) {
									selCat = "task";
								} else if (item == 3) {
									selCat = "feature";
								} else if (item == 4) {
									selCat = "support";
								}
								selectedType = item;

								baseURL = defaultURL.replace("SEL_CAT", selCat);
								baseURL = baseURL
										.replace("CUR_PAGE", "" + page);
								SharedPreferences prefs = getSharedPreferences(
										"drupal_service", MODE_PRIVATE);
								String uid = prefs.getString("uid", null);
								if (uid != null) {
									baseURL = baseURL.replace("USER_ID", uid);
									System.out.println("getting url: "
											+ baseURL);
								}
								alert.dismiss();
								refresh(0);
							}
						});
				alert = builder.create();
				alert.show();
			}
		});

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleCount, int totalCount) {
		if (totalCount < 50) {
			return;
		}
		if ((totalCount - visibleCount) <= (firstVisibleItem + 0)) {
			page++;
			refresh(page);
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		//
	}

	public void parseIssue(Issue issue, Elements columns) {

	}

	protected int getEmptyCategoryListString() {
		return R.string.empty_list;
	}

}
