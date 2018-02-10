package com.bss.arrahmanlyrics.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bss.arrahmanlyrics.MainActivity;
import com.bss.arrahmanlyrics.R;
import com.bss.arrahmanlyrics.model.song;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mohan on 5/20/17.
 */

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.MyViewHolder>{

	private Context mContext;
	private List<song> songlist;
	MainActivity activity;

	public SongAdapter(List<song> songlist) {

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


		holder.name.setText(actualsong.getSong_title());
		//holder.name.setText(actualsong.getSongTitle());
//		Glide.with(mContext).load(actualsong.getImages()).into(holder.imageView);
		//holder.imageView.setImageBitmap(activity.getImageBitmap(actualsong.getMovietitle()));
		holder.lyricist.setText("Lyricist: " + actualsong.getLyricist());
		holder.movietitle.setText("Movie: " + actualsong.getAlbum_name());

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





}
