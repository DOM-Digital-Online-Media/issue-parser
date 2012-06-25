package de.dom.drupalit;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import de.dom.drupalit.backgroundservice.AppService;
import de.dom.drupalit.backgroundservice.OnAlarmReceiver;
import de.dom.drupalit.backgroundservice.OnBootReceiver;
import de.dom.drupalit.tabs.TabAllIssues;
import de.dom.drupalit.tabs.TabMyIssues;
import de.dom.drupalit.tabs.TabProfile;

public class Main extends TabActivity {
	public static Main instance;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Main.instance = this;
		WebView backgroundWeb = new WebView(this);
		CookieSyncManager.createInstance(this);
		CookieManager mgr = CookieManager.getInstance();

		System.out.println(mgr.getCookie("drupal.org") + " test");
		String cookie_string = mgr.getCookie("drupal.org");
		System.out.println("cookie string: " + cookie_string);
		if (cookie_string != null && cookie_string.contains("authenticated")) {
			backgroundWeb.loadUrl("http://drupal.org/user?destination=home");
			backgroundWeb.getSettings().setJavaScriptEnabled(true);
			backgroundWeb.getSettings().setSaveFormData(true);
			backgroundWeb.getSettings().setSavePassword(false);
			backgroundWeb.getSettings().setLoadsImagesAutomatically(false);
			backgroundWeb.setWebChromeClient(new WebChromeClient() {

				@Override
				public boolean onJsAlert(WebView view, String url,
						String message, JsResult result) {
					result.confirm();
					System.out.println(message);
					if (message.contains("/user/")) {
						TabProfile.userID = message.substring(message
								.lastIndexOf('/') + 1);
						System.out.println("saving user id");
						SharedPreferences prefs = getSharedPreferences(
								"drupal_service", MODE_PRIVATE);
						Editor editor = prefs.edit();
						editor.putString("uid", TabProfile.userID);
						editor.commit();
						System.out.println("UID:" + TabProfile.userID);
						TabProfile.loggedIn = true;
						TabProfile.registerAlarm(Main.this);
					} else {
						TabProfile.userID = null;
						TabProfile.loggedIn = false;
					}
					Utils.removeLoadingDialog();

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

					if (url.contains("drupal.org/user?destination=home")) {
						view.loadUrl("javascript:alert(document.body.getElementsByClassName('your-profile')[0].getElementsByTagName('a')[0].href);");
					}
					super.onPageFinished(view, url);
				}
			});

		}

		addTabs();
	}

	private void addTabs() {
		addTab(getResources().getString(R.string.tab_name_all_issues),
				R.drawable.all_issues, TabAllIssues.class);
		addTab(getResources().getString(R.string.tab_name_your_issues),
				R.drawable.my_issues, TabMyIssues.class);
		addTab(getResources().getString(R.string.tab_name_profile),
				R.drawable.profile, TabProfile.class);
	}

	public void switchToProfile() {

		getTabWidget().removeAllViews();
		addTabs();
		getTabHost().setCurrentTab(2);
		getTabWidget().setCurrentTab(2);
		getTabWidget().invalidate();
		getTabHost().invalidate();
	}

	private void addTab(String labelId, int drawableId, Class<?> c) {
		TabHost tabHost = getTabHost();
		//tabHost.setBackgroundResource(R.drawable.header);
		Intent intent = new Intent(this, c);
		TabHost.TabSpec spec = tabHost.newTabSpec("tab" + labelId);
		View tabIndicator = LayoutInflater.from(this).inflate(
				R.layout.tab_indicator, getTabWidget(), false);
		TextView title = (TextView) tabIndicator.findViewById(R.id.title);
		title.setText(labelId);
		ImageView icon = (ImageView) tabIndicator.findViewById(R.id.icon);
		icon.setImageResource(drawableId);
		spec.setIndicator(tabIndicator);
		spec.setContent(intent);
		tabHost.addTab(spec);
	}
}