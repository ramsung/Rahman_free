package com.bss.arrahmanlyrics.Fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bss.arrahmanlyrics.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TamilFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TamilFragment#newInstance} factory method to
 * create an instance of this fragment.
 */


public class TamilFragment extends Fragment {
	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";

	// TODO: Rename and change types of parameters
	private String mParam1;
	private String mParam2;
	TextView lyrics1,lyrics2;
	AdView top,middle;
	private OnFragmentInteractionListener mListener;

	private AdView mAdView;
	private AdView mAdView1;
	public TamilFragment() {
		// Required empty public constructor
	}

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @param param1 Parameter 1.
	 * @param param2 Parameter 2.
	 * @return A new instance of fragment TamilFragment.
	 */
	// TODO: Rename and change types and number of parameters
	public static TamilFragment newInstance(String param1, String param2) {
		TamilFragment fragment = new TamilFragment();
		Bundle args = new Bundle();
		args.putString(ARG_PARAM1, param1);
		args.putString(ARG_PARAM2, param2);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			mParam1 = getArguments().getString(ARG_PARAM1);
			mParam2 = getArguments().getString(ARG_PARAM2);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_other_lyrics, container, false);
		lyrics1 = (TextView) view.findViewById(R.id.lyricsOthers);
		lyrics2 = (TextView) view.findViewById(R.id.lyricsOthers2);
		mAdView = (AdView) view.findViewById(R.id.tamil_top);

		AdRequest adRequest = new AdRequest.Builder()
				.addTestDevice("45AEA33662E36BBB9B11FE55E4EFA874")
				.build();
		mAdView.loadAd(adRequest);

		mAdView.setAdListener(new AdListener() {
			@Override
			public void onAdLoaded() {
				// Code to be executed when an ad finishes loading.
				Log.i("Ads", "onAdLoaded");
			}

			@Override
			public void onAdFailedToLoad(int errorCode) {
				// Code to be executed when an ad request fails.
				Log.i("Ads", "onAdFailedToLoad");
			}

			@Override
			public void onAdOpened() {
				// Code to be executed when an ad opens an overlay that
				// covers the screen.
				Log.i("Ads", "onAdOpened");
			}

			@Override
			public void onAdLeftApplication() {
				// Code to be executed when the user has left the app.
				Log.i("Ads", "onAdLeftApplication");
			}

			@Override
			public void onAdClosed() {
				// Code to be executed when when the user is about to return
				// to the app after tapping on an ad.
				Log.i("Ads", "onAdClosed");
			}
		});

		mAdView1 = (AdView) view.findViewById(R.id.tamil_middle);

		AdRequest adRequest1 = new AdRequest.Builder()
				.addTestDevice("45AEA33662E36BBB9B11FE55E4EFA874")
				.build();
		mAdView1.loadAd(adRequest1);

		mAdView1.setAdListener(new AdListener() {
			@Override
			public void onAdLoaded() {
				// Code to be executed when an ad finishes loading.
				Log.i("Ads", "onAdLoaded");
			}

			@Override
			public void onAdFailedToLoad(int errorCode) {
				// Code to be executed when an ad request fails.
				Log.i("Ads", "onAdFailedToLoad");
			}

			@Override
			public void onAdOpened() {
				// Code to be executed when an ad opens an overlay that
				// covers the screen.
				Log.i("Ads", "onAdOpened");
			}

			@Override
			public void onAdLeftApplication() {
				// Code to be executed when the user has left the app.
				Log.i("Ads", "onAdLeftApplication");
			}

			@Override
			public void onAdClosed() {
				// Code to be executed when when the user is about to return
				// to the app after tapping on an ad.
				Log.i("Ads", "onAdClosed");
			}
		});

		return view;
	}

	// TODO: Rename method, update argument and hook method into UI event
	public void onButtonPressed(Uri uri) {
		if (mListener != null) {
			mListener.onFragmentInteraction(uri);
		}
	}



	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated
	 * to the activity and potentially other fragments contained in that
	 * activity.
	 * <p>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	public interface OnFragmentInteractionListener {
		// TODO: Update argument type and name
		void onFragmentInteraction(Uri uri);
	}

	public void setLyrics(String text1,String text2){
		lyrics1.setText(text1);
		lyrics2.setText(text2);

	}
}
