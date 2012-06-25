package de.dom.drupalit.adapters;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;
import de.dom.drupalit.R;
import de.dom.drupalit.listeners.RSSItemClickListener;
import de.dom.drupalit.objects.Issue;

public class FeedAdapter extends ArrayAdapter<Issue> {

	private ArrayList<Issue> feed;
	private SimpleDateFormat sdf;
	private LayoutInflater li;
	private boolean showMyStatus;

	public FeedAdapter(Context context, int resID) {
		super(context, resID);
		sdf = new SimpleDateFormat(context.getString(R.string.date_format));
		// sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a", new Locale("en"));
		li = LayoutInflater.from(getContext());
	}

	public ListAdapter setFeed(ArrayList<Issue> feed2) {
		this.feed = feed2;
		return this;
	}
	
	public void setShowMyStatus(boolean showMyStatus) {
		this.showMyStatus = showMyStatus;
	}

	@Override
	public int getCount() {
		if (feed != null) {
			return feed.size();
		} else {
			return 0;
		}
	}

	@Override
	public View getView(int position, View cacheView, ViewGroup parent) {
		View view = cacheView;

		if (feed.get(position).isLoadingItem) {
			view = (LinearLayout) li.inflate(R.layout.feed_list_last_row, null);
			return view;
		}

		ViewHolder holder;
		if (view == null
				|| ((TextView) view.findViewById(R.id.txtDate)) == null) {
			view = (LinearLayout) li.inflate(R.layout.feed_list_row, null);
			holder = new ViewHolder();
			holder.txtDate = ((TextView) view.findViewById(R.id.txtDate));
			holder.txtTitle = ((TextView) view.findViewById(R.id.txtTitle));
			holder.txtMyStatus = ((TextView) view.findViewById(R.id.txtStatus));
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

	
		Issue item = this.feed.get(position);
		holder.txtTitle.setText(item.title);
		holder.txtMyStatus.setText(item.myStatus);
		holder.txtDate.setText(String.format(
				getContext().getString(R.string.ago), item.lastUpdated));
		view.setOnClickListener(new RSSItemClickListener(getContext(), item));
		return view;
	}

	class ViewHolder {
		TextView txtTitle;
		TextView txtDate;
		TextView txtMyStatus;
	}

}
