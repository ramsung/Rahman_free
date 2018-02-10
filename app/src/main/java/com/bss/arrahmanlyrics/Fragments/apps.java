package com.bss.arrahmanlyrics.Fragments;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bss.arrahmanlyrics.R;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link apps.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link apps#newInstance} factory method to
 * create an instance of this fragment.
 */
public class apps extends Fragment {
	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";

	// TODO: Rename and change types of parameters
	private String mParam1;
	private String mParam2;
	TextView rating,install1;
	CardView top;
	private OnFragmentInteractionListener mListener;

	public apps() {
		// Required empty public constructor
	}

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @param param1 Parameter 1.
	 * @param param2 Parameter 2.
	 * @return A new instance of fragment apps.
	 */
	// TODO: Rename and change types and number of parameters
	public static apps newInstance(String param1, String param2) {
		apps fragment = new apps();
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
		View view = inflater.inflate(R.layout.fragment_apps, container, false);
		rating = (TextView) view.findViewById(R.id.rating1);

		install1 = (TextView) view.findViewById(R.id.install);
		if(appInstalledOrNot("com.beyonity.matchinggame")){
			install1.setText("Play Now");
		}else {
			install1.setText("Install");
		}
		rating.setText("5.0 \u2730");
		top = (CardView) view.findViewById(R.id.one);
		top.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(appInstalledOrNot("com.beyonity.matchinggame")){
					Intent LaunchIntent = getActivity().getPackageManager()
							.getLaunchIntentForPackage("com.beyonity.matchinggame");
					startActivity(LaunchIntent);

				}else {
					try {
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(Uri.parse("market://details?id=com.beyonity.matchinggame"));
						startActivity(intent);
					} catch (Exception e) { //google play app is not installed
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.beyonity.matchinggame"));
						startActivity(intent);
					}
				}

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
	private boolean appInstalledOrNot(String uri) {
		PackageManager pm = getActivity().getPackageManager();
		try {
			pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
			return true;
		} catch (PackageManager.NameNotFoundException e) {
		}

		return false;
	}
}
