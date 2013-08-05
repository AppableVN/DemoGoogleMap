package com.example.mapdemo;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindowAdapter implements InfoWindowAdapter {

	// These a both viewgroups containing an ImageView with id "badge" and two
	// TextViews with id
	// "title" and "snippet".
	private final View mWindow;
//	private final View mContents;

	public CustomInfoWindowAdapter(Context mContext) {
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		mWindow = inflater.inflate(R.layout.custom_info_window, null);
//		mContents = inflater.inflate(R.layout.custom_info_contents, null);
	}

	@Override
	public View getInfoContents(Marker marker) {
//		render(marker, mContents);
		return null;
	}

	@Override
	public View getInfoWindow(Marker marker) {
		render(marker, mWindow);
		return mWindow;
	}

	private void render(Marker marker, View view) {
		int badge = R.drawable.badge_sa;
		ImageView imgBadge = (ImageView) view.findViewById(R.id.badge);
		imgBadge.setImageResource(badge);

		String title = marker.getTitle();
		TextView titleUi = ((TextView) view.findViewById(R.id.title));
		if (title != null) {
			// Spannable string allows us to edit the formatting of the text.
			SpannableString titleText = new SpannableString(title);
			titleText.setSpan(new ForegroundColorSpan(Color.RED), 0,
					titleText.length(), 0);
			titleUi.setText(titleText);
		} else {
			titleUi.setText("");
		}

		String snippet = marker.getSnippet();
		TextView snippetUi = ((TextView) view.findViewById(R.id.snippet));
		if (snippet != null && snippet.length() > 12) {
			SpannableString snippetText = new SpannableString(snippet);
			snippetText.setSpan(new ForegroundColorSpan(Color.MAGENTA), 0, 10, 0);
			snippetText.setSpan(new ForegroundColorSpan(Color.BLUE), 12, snippet.length(), 0);
			snippetUi.setText(snippetText);
		} else {
			snippetUi.setText("");
		}
	}
}
