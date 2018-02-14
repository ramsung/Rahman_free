package com.bss.arrahmanlyrics.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bss.arrahmanlyrics.MainActivity;
import com.bss.arrahmanlyrics.R;
import com.bss.arrahmanlyrics.albumArts.albumArts;
import com.bss.arrahmanlyrics.model.song;
import com.bss.arrahmanlyrics.utility.Helper;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mohan on 5/20/17.
 */

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.MyViewHolder>{

	private Context mContext;
	private List<song> songlist;
	MainActivity activity;

	public SongAdapter(Context mContext, List<song> songlist) {
		this.mContext = mContext;
		this.songlist = songlist;
		
		//QuickAction.setDefaultColor(ResourcesCompat.getColor(s.getResources(), R.color.white, null));
		//QuickAction.setDefaultTextColor(Color.BLACK);


	}

	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.song_list_view, parent, false);
		RecyclerView.ViewHolder holder = new MyViewHolder(itemView);

		return (MyViewHolder) holder;
	}

	@Override
	public void onBindViewHolder(MyViewHolder holder, int position) {
		song actualsong = songlist.get(position);


		holder.name.setText(Helper.FirstLetterCaps(actualsong.getSong_title()));
		//holder.name.setText(actualsong.getSongTitle());
		//holder.imageView.setImageBitmap(albumArts.getBitmap(actualsong.getAlbum_id()));
		//holder.imageView.setImageBitmap(activity.getImageBitmap(actualsong.getMovietitle()));
		holder.imageView.setImageBitmap(albumArts.getBitmap(actualsong.getAlbum_id()));
		holder.lyricist.setText("Lyricist: " + Helper.FirstLetterCaps(actualsong.getLyricist()));
		holder.movietitle.setText("Movie: " + Helper.FirstLetterCaps(actualsong.getAlbum_name()));

	}

	@Override
	public int getItemCount() {
		Log.d("adapter", "getItemCount: "+songlist.size());
		return songlist.size();
	}


	public class MyViewHolder extends RecyclerView.ViewHolder {
		public TextView name, lyricist, movietitle;
		ImageView imageView;


		public MyViewHolder(View view) {
			super(view);
			name = (TextView) view.findViewById(R.id.Songtitle);
			lyricist = (TextView) view.findViewById(R.id.MovieTitle);
			movietitle = (TextView) view.findViewById(R.id.Songlyricist);
			imageView = (ImageView) view.findViewById(R.id.songCover);


			//albumCover = (ImageView) view.findViewById(R.id.album_artwork);
		}








	}

	public song getItem(int position){
		return songlist.get(position);
	}


	public List<song> getSonglist(){
		return songlist;
	}


}
