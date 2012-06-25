package de.dom.drupalit.objects;

import java.text.SimpleDateFormat;

import android.content.Context;
import de.dom.drupalit.R;

public class Comment {
	
	public String date;
	public String author;
	public String comment;
	public boolean isNew = false;
	
	public String localizedCreationString(Context context) {
		SimpleDateFormat sdf = new SimpleDateFormat(context.getString(R.string.date_format));
		return String.format(context.getString(R.string.list_item_date_author_string), this.author, sdf.format(this.date));
	}

}