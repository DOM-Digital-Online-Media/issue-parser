package de.dom.drupalit.tabs;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;
import de.dom.drupalit.R;
import de.dom.drupalit.Utils;
import de.dom.drupalit.backgroundservice.OnAlarmReceiver;

public class TabProfile extends Activity {

	private Button btnLogin;
	private WebView backgroundWeb;
	private String login;
	private String password;
	private EditText txtLogin;
	private EditText txtPassword;
	private boolean stopSubmit;
	private int step = 0;
	private CheckBox checkAutoUpdate;
	private Spinner intervalSpinner;
	public static boolean loggedIn = false;
	public static String userID = null;
	public static final long[] INTERVAL_ARRAY = new long[] { 120000, 300000,
			900000, 1800000, 3600000, 3600000 * 2, 3600000 * 5, 3600000 * 24 };
	private int interval = 0;

	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.profile);

		SharedPreferences prefs = getSharedPreferences("drupal", MODE_PRIVATE);
		login = prefs.getString("login", null);
		password = prefs.getString("passwd", null);

		txtLogin = (EditText) findViewById(R.id.editLogin);
		txtPassword = (EditText) findViewById(R.id.editPassword);
		intervalSpinner = (Spinner) findViewById(R.id.spinner1);
		txtLogin.setText(login);
		txtPassword.setText(password);
		checkAutoUpdate = ((CheckBox) findViewById(R.id.checkBox1));
		boolean doAutoUpdate = prefs.getBoolean("notify", false);
		checkAutoUpdate.setChecked(doAutoUpdate);
		intervalSpinner.setEnabled(doAutoUpdate);
		checkAutoUpdate
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						SharedPreferences prefs = getSharedPreferences(
								"drupal", MODE_PRIVATE);
						Editor editor = prefs.edit();
						editor.putBoolean("notify", isChecked);
						editor.putInt("notify_interval", interval);
						editor.commit();
						intervalSpinner.setEnabled(isChecked);
						registerAlarm(TabProfile.this);
					}
				});

		setupSpinner();
		setupWebView();

		btnLogin = ((Button) findViewById(R.id.btnLogin));
		if (userID != null && loggedIn) {
			setLogoutState();
		} else {
			setLoginClickListener();
		}
	}

	private void setupSpinner() {
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.interval_array,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		intervalSpinner.setAdapter(adapter);
		SharedPreferences prefs = getSharedPreferences("drupal", MODE_PRIVATE);
		int value = prefs.getInt("notify_interval", 0);
		intervalSpinner.setSelection(value);
		intervalSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				interval = arg2;
				SharedPreferences prefs = getSharedPreferences("drupal",
						MODE_PRIVATE);
				Editor editor = prefs.edit();
				editor.putBoolean("notify", checkAutoUpdate.isChecked());
				editor.putInt("notify_interval", interval);
				editor.commit();
				registerAlarm(TabProfile.this);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				//
			}
		});

	}

	private void setLoginClickListener() {
		((TextView) findViewById(R.id.txtLoggedInAs)).setVisibility(View.GONE);
		btnLogin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (txtLogin.getText().toString().length() == 0
						|| txtPassword.getText().toString().length() == 0) {
					Toast.makeText(TabProfile.this,
							"Please enter your login information first.",
							Toast.LENGTH_SHORT).show();
					return;
				}
				step = 0;
				stopSubmit = false;
				SharedPreferences prefs = getSharedPreferences("drupal",
						MODE_PRIVATE);
				Editor editor = prefs.edit();
				editor.putString("login", txtLogin.getText().toString());
				editor.putString("passwd", txtPassword.getText().toString());
				editor.commit();
				login = prefs.getString("login", null);
				password = prefs.getString("passwd", null);

				InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				mgr.hideSoftInputFromWindow(txtLogin.getWindowToken(), 0);
				mgr.hideSoftInputFromWindow(txtPassword.getWindowToken(), 0);

				Utils.showLoadingDialog(TabProfile.this, null);
				backgroundWeb
						.loadUrl("http://drupal.org/user?destination=home");
			}
		});
	}

	private void setLogoutState() {
		SharedPreferences prefs = getSharedPreferences("drupal", MODE_PRIVATE);
		String name = prefs.getString("username", "");
		((TextView) findViewById(R.id.txtLoggedInAs)).setText(String.format(
				getString(R.string.logged_in_as), name));
		((TextView) findViewById(R.id.txtLoggedInAs))
				.setVisibility(View.VISIBLE);

		if (TabMyIssues.instance != null) {
			TabMyIssues.instance.refresh(0);
		}

		Editor editor = prefs.edit();
		editor.putString("passwd", "");
		txtPassword.setText("");
		editor.commit();

		startBGService();
		Utils.removeLoadingDialog();
		btnLogin.setText(R.string.logout);
		txtLogin.setVisibility(View.GONE);
		txtPassword.setVisibility(View.GONE);
		btnLogin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Utils.showLoadingDialog(TabProfile.this, null);
				backgroundWeb.loadUrl("http://drupal.org/logout");
			}
		});
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
				System.out.println("msg: " + message);
				if (message.contains("Logged in as")) {
					SharedPreferences prefs = getSharedPreferences("drupal",
							MODE_PRIVATE);
					Editor editor = prefs.edit();
					editor.putString("username", message.substring(13));
					editor.commit();
					loggedIn = true;
					setLogoutState();

				} else if (message.contains("/user/")) {
					userID = message.substring(message.lastIndexOf('/') + 1);
					System.out.println("UID:" + userID);
					saveUserID();
					startBGService();
				} else if (message.contains("SUCCESS")) {
					loggedIn = true;
					setLogoutState();
					startBGService();
				} else if (message.contains("ERROR")) {
					stopSubmit = true;
					loggedIn = false;
					final AlertDialog.Builder builder = new AlertDialog.Builder(
							TabProfile.this);
					builder.setMessage(R.string.error_login).setNeutralButton(
							"OK", null);
					final AlertDialog alert = builder.create();
					alert.show();
					Utils.removeLoadingDialog();
				} else if (message.contains("LOGGED_IN")) {
					loggedIn = true;
					setLogoutState();
				}
				return true;
			}

			private void saveUserID() {
				System.out.println("saving user id");
				SharedPreferences prefs = getSharedPreferences(
						"drupal_service", MODE_PRIVATE);
				Editor editor = prefs.edit();
				editor.putString("uid", userID);
				editor.commit();
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
			public void onPageStarted(WebView view, String url, Bitmap favicon) {

				super.onPageStarted(view, url, favicon);
			}

			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {

				super.onReceivedError(view, errorCode, description, failingUrl);
			}

			@Override
			public void onPageFinished(WebView view, String url) {

				System.out.println("loaded url: " + url);

				System.out.println("step: " + step);
				System.out.println("stop submit? " + stopSubmit);
				if (url.contains("drupal.org/user?destination=home")) {
					view.loadUrl("javascript:alert(document.getElementById('userinfo').getElementsByTagName('a')[0].text);");
					view.loadUrl("javascript:alert(document.body.getElementsByClassName('your-profile')[0].getElementsByTagName('a')[0].href);");
					if (step == 1) {
						view.loadUrl("javascript:if (document.body.innerHTML.indexOf('Logged in')>-1){alert('SUCCESS');}else{alert('ERROR');}");
					}
					if (step == 0) {
						view.loadUrl("javascript:if (document.body.innerHTML.indexOf('Logged in')>-1){alert('LOGGED_IN');};");

						view.loadUrl("javascript:document.getElementById('edit-name').value='"
								+ login + "';");
						view.loadUrl("javascript:document.getElementById('edit-pass').value='"
								+ password + "';");
						if (!stopSubmit) {
							view.loadUrl("javascript:document.getElementById('user-login').submit();");
						}
						step++;
					}

				} else if (url.equals("http://drupal.org/")) {
					userID = null;
					txtLogin.setVisibility(View.VISIBLE);
					txtPassword.setVisibility(View.VISIBLE);

					btnLogin.setText(R.string.login);
					step = 0;
					setLoginClickListener();
					if (TabMyIssues.instance != null) {
						TabMyIssues.instance.clearAfterLogout();
					}
					Utils.removeLoadingDialog();

				} else {
					view.loadUrl("javascript:alert(document.getElementById('userinfo').getElementsByTagName('a')[0].text);");
				}

				super.onPageFinished(view, url);
			}

		});

	}

	private void startBGService() {
		registerAlarm(this);
	}

	public static void registerAlarm(Context context) {
		SharedPreferences prefs = context.getSharedPreferences("drupal",
				MODE_PRIVATE);
		AlarmManager mgr = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		boolean doAutoUpdate = prefs.getBoolean("notify", false);
		Intent i = new Intent(context, OnAlarmReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);

		if (!doAutoUpdate) {
			mgr.cancel(pi);
			System.out.println("autoupdate is turned off.");
			return;
		}

		int value = prefs.getInt("notify_interval", 0);
		long period = TabProfile.INTERVAL_ARRAY[value];
		mgr.cancel(pi);
		mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				SystemClock.elapsedRealtime() + 6000, period, pi);
		System.out.println("STARTED BACKGROUND TASK WITH INTERVAL: " + period);
	}
}
