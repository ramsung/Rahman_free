package com.bss.arrahmanlyrics.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bss.arrahmanlyrics.MainActivity;
import com.bss.arrahmanlyrics.R;
import com.bss.arrahmanlyrics.model.albums;
import com.bss.arrahmanlyrics.model.song;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.bss.arrahmanlyrics.albumArts.albumArts;
import com.bss.arrahmanlyrics.utility.Helper;
import com.bumptech.glide.Glide;

import static android.content.ContentValues.TAG;

public class ExpandableListAdapterMysql extends BaseExpandableListAdapter {

	private Context _context;
	private List<albums> _listDataHeader; // header titles
	MainActivity activity;
	// child data in format of header title, child title
	private HashMap<String, List<song>> _listDataChild;
	final String image_path = "https://beyonitysoftwares.cf/arts/";
	public ExpandableListAdapterMysql(Context context, List<albums> listDataHeader,
                                      HashMap<String, List<song>> listChildData, MainActivity activity) {
		this._context = context;
		this._listDataHeader = listDataHeader;
		this._listDataChild = listChildData;
		this.activity = activity;
	}

	@Override
	public song getChild(int groupPosition, int childPosititon) {
		return this._listDataHeader.get(groupPosition).getSonglist().get(childPosititon);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, final int childPosition,
	                         boolean isLastChild, View convertView, ViewGroup parent) {

		song song = getChild(groupPosition, childPosition);
		Log.i(TAG, "getChildView: " + String.valueOf(song));

		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) this._context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.album_song_list_view, null);
		}
		TextView trackNo = (TextView) convertView.findViewById(R.id.trackNo);
		TextView lyricist = (TextView) convertView.findViewById(R.id.Songlyricist);
		TextView songtitle = (TextView) convertView.findViewById(R.id.Songtitle);
		trackNo.setText(String.valueOf(song.getTrack_no()));
		lyricist.setText((Helper.FirstLetterCaps(song.getLyricist())));
		songtitle.setText((Helper.FirstLetterCaps(song.getSong_title())));
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		Log.i(TAG, "getChildrenCount: " + _listDataChild);

		return this._listDataHeader.get(groupPosition).getSonglist().size();

	}

	@Override
	public albums getGroup(int groupPosition) {
		return this._listDataHeader.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return this._listDataHeader.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
	                         View convertView, ViewGroup parent) {
		albums album = getGroup(groupPosition);
		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) this._context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.album_view, null);
		}

		TextView title = (TextView) convertView.findViewById(R.id.Title);
		TextView count = (TextView) convertView.findViewById(R.id.TotalSongs);
		ImageView thumbnail = (ImageView) convertView.findViewById(R.id.albumimg);
		title.setText((Helper.FirstLetterCaps(album.getAlbum_name())));
		count.setText(album.getSonglist().size() + " songs");
		Glide.with(_context).load(image_path + album.getAlbum_id()+ ".png").into(thumbnail);
		//thumbnail.setImageBitmap(albumArts.getBitmap(album.getAlbum_id()));
		//Glide.with(context).load(album.getImageString()).into(holder.thumbnail);
		//thumbnail.setImageBitmap(activity.getImageBitmap(album.getAlbum_name()));

		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	/*public void setFilter(List<albumModel> list,HashMap<String,List<albumsongs>> map){
		this._listDataHeader.clear();
		this._listDataHeader = list;
		this._listDataChild.clear();
		this._listDataChild = map;
		notifyDataSetChanged();

	}*/


	public HashMap<String,List<song>> get_listDataChild(){
		return _listDataChild;
	}
	public List<albums> get_listDataHeader(){
		return _listDataHeader;
	}
}

