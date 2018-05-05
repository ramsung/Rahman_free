package com.bss.arrahmanlyrics;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.bss.arrahmanlyrics.Fragments.EnglishFragment;
import com.bss.arrahmanlyrics.Fragments.FavFragment;
import com.bss.arrahmanlyrics.Fragments.TamilFragment;
import com.bss.arrahmanlyrics.Fragments.about;
import com.bss.arrahmanlyrics.adapter.ExpandableListAdapterMysql;
import com.bss.arrahmanlyrics.adapter.SongAdapter;
//import com.bss.arrahmanlyrics.albumArts.albumArts;
import com.bss.arrahmanlyrics.appconfig.AppConfig;
import com.bss.arrahmanlyrics.appconfig.AppController;
import com.bss.arrahmanlyrics.custom_pages.CustomViewPager;
import com.bss.arrahmanlyrics.databaseHandler.DatabaseHandler;
import com.bss.arrahmanlyrics.databaseHandler.SQLiteSignInHandler;
import com.bss.arrahmanlyrics.databaseHandler.SessionManager;
import com.bss.arrahmanlyrics.model.albums;
import com.bss.arrahmanlyrics.model.song;
import com.bss.arrahmanlyrics.music.MusicService;
import com.bss.arrahmanlyrics.utility.Helper;
import com.bss.arrahmanlyrics.utility.RecyclerItemClickListener;
import com.bss.arrahmanlyrics.utility.StorageUtil;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.hoang8f.android.segmented.SegmentedGroup;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener,MusicService.mainActivityCallback,SearchView.OnQueryTextListener {
    private static final String TAG = "MainAcitivity";
    GoogleApiClient mGoogleSignInClient;
    public static DatabaseHandler dbHandler;
    private SessionManager session;
    private SQLiteSignInHandler db;
    private ProgressDialog pDialog;
    private ProgressDialog Dialog;
    int updateBoth = 0;

    ProgressBar bar;

    List<song> songList;
    List<song> songListSearch;

    private RecyclerView rv1;
    SongAdapter songAdapter;
    int request = 0;

    List<albums> albumList;
    private HashMap<String, List<song>> albumSongList;
    private ExpandableListView rv2;
    ExpandableListAdapterMysql albumAdapter;

    //mediaconrols
    ImageButton playpause, previous, next, shuffle, fav;
    SeekBar seekBar;
    TextView currentTime, totalTime, moviename, songname;

    //musicservice
    boolean serviceBound = false;
    MusicService player;


    SegmentedGroup segmentedGroup;
    EnglishFragment englishFragment;
    TamilFragment tamilFragment;
    private Handler mHandler = new Handler();

    Thread t;

    CustomViewPager viewPager;
    CustomViewPager viewPager2;

    int totalSongs = 0;
    Point p;


    int imageReq = 0;
    int imageReqCom = 0;
    ArrayList<song> playlist = new ArrayList<>();

    final String image_path = "https://beyonitysoftwares.cf/arts/";
    public static final String Broadcast_PLAY_NEW_AUDIO = "com.bss.arrahmanlyrics.activites.PlayNewAudio";
    public static final String Broadcast_NEW_ALBUM = "com.bss.arrahmanlyrics.activites.PlayNewAlbum";

    private InterstitialAd mInterstitialAd;

    BottomNavigationView bottomMenu;
    ImageView up;
    SlidingUpPanelLayout favoritePanel;
    FavFragment favFragment;
    about aboutFragment;
    //apps appsFragment;

    String titleFromFirebase = "";
    String desFromfirebase="";

    SearchView songsearch;
    SearchView albumsearch;

    boolean newIntent= false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "calls: on create");
        FirebaseInstanceId.getInstance().getToken();
        Fabric.with(this,new Crashlytics());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Log.d("Firebase", "token "+ FirebaseInstanceId.getInstance().getToken());
        //Log.d(TAG, "onCreate: "+getVersionCode());


        Handler h = new Handler();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                MobileAds.initialize(MainActivity.this, "ca-app-pub-7987343674758455~2523296928");
                mInterstitialAd = new InterstitialAd(MainActivity.this);
                mInterstitialAd.setAdUnitId("ca-app-pub-7987343674758455/6284132866");
                mInterstitialAd.loadAd(new AdRequest.Builder().addTestDevice("45AEA33662E36BBB9B11FE55E4EFA874").build());
                mInterstitialAd.setAdListener(new AdListener() {
                    @Override
                    public void onAdLoaded() {
                        // Code to be executed when an ad finishes loading.
                        Log.i("Ads Interstitial", "onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        // Code to be executed when an ad request fails.
                        Log.i("Ads Interstitial", "onAdFailedToLoad" + errorCode);
                    }

                    @Override
                    public void onAdOpened() {
                        // Code to be executed when an ad opens an overlay that
                        // covers the screen.
                        Log.i("Ads Interstitial", "onAdOpened");
                    }

                    @Override
                    public void onAdLeftApplication() {
                        // Code to be executed when the user has left the app.
                        Log.i("Ads Interstitial", "onAdLeftApplication");
                    }

                    @Override
                    public void onAdClosed() {
                        // Code to be executed when when the user is about to return
                        // to the app after tapping on an ad.
                        Log.i("Ads Interstitial", "onAdClosed");
                    }
                });

            }
        };
        h.post(r);


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        db = new SQLiteSignInHandler(getApplicationContext());

        // Session manager
        session = new SessionManager(getApplicationContext());
        if (!session.isLoggedIn()) {

            signIn();

        } else {
            HashMap<String, String> details = db.getUserDetails();
            Toast.makeText(this, "Welcome back " + details.get("displayName"), Toast.LENGTH_SHORT).show();


            //logoutUser();
        }
        Log.d(TAG, "onCreate: before init");
        dbHandler = new DatabaseHandler(getApplicationContext());
        init();
        setNavigation();
        setUpLyricsPage();
        String local_time_songs = dbHandler.getUpdateDetails("songs");
        String local_time_albums = dbHandler.getUpdateDetails("albums");
        pDialog.setMessage("loading songs");
        showDialog();
        getupdatetime(local_time_songs,local_time_albums);


/*

        int noOfSongs = dbHandler.getNoOfSongs();
        int noOfAlbums = dbHandler.getNoOfAlbums();
        Log.d(TAG, "onCreate: songs = " + noOfSongs + " albums = " + noOfAlbums);


        //Log.d(TAG, "onCreate: album name = "+dbHandler.getAlbumName(2));
        if (noOfAlbums < 1) {
            downloadAlbumDatabase();
        }
        if (noOfSongs > 0 && noOfAlbums > 0) {
            //Log.d(TAG, "onCreate: " + albumArts.getSize() + ", " + noOfAlbums);
            if (dbHandler.getNumberOfImages() == noOfAlbums) {
                pDialog.setMessage("Loading songs ...");
                showDialog();
                setUpImages();
                //setUpSongsAlbums();
                //Log.d(TAG, "dbhander: " + dbHandler.getFavorites(Integer.parseInt(db.getUserDetails().get("id"))));

            } else {
                pDialog.setMessage("Loading songs ...");
                showDialog();
                setUpImages();
               /* ArrayList<Integer> ids = dbHandler.getAlbumIds();
                for (int a : ids) {
                    String imageLink = dbHandler.getImageLink(a);
                    new DownloadImageTask(a, MainActivity.this).execute(imageLink);
                }
                if (session.isLoggedIn()) {
                    getFavFromDatabase(db.getUserDetails().get("id"));
                }

            }
        }*/


        // ATTENTION: This was auto-generated to handle app links.

    }

    public void init() {

        Log.d(TAG, "init: called");
        songList = new ArrayList<>();
        songListSearch = new ArrayList<>();
        rv1 = (RecyclerView) findViewById(R.id.rv1);
        rv1.addItemDecoration(new DividerItemDecoration(getApplicationContext(), LinearLayoutManager.VERTICAL));
        rv1.setItemAnimator(new DefaultItemAnimator());
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        rv1.setLayoutManager(layoutManager);

        rv1.addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                song song = songAdapter.getItem(position);
                Log.d(TAG, "onItemClick: " + songList.indexOf(song));

                StorageUtil storageUtil = new StorageUtil(getApplicationContext());

                if (storageUtil.loadAudio() == null || totalSongs > storageUtil.loadAudio().size()) {
                    Log.d(TAG, "onItemClick: its null");
                    for (song songs : songList) {
                        song s = new song(songs.getSong_id(), songs.getSong_title(), songs.getAlbum_id(), songs.getAlbum_name(), songs.getDownload_link(), songs.getLyricist(), songs.getTrack_no());
                        playlist.add(s);
                    }
                    Log.d(TAG, "onItemClick: playlist = " + playlist.size());
                    int index = 0;
                    for (song s : playlist) {
                        if (s.getSong_title().equals(song.getSong_title()) && s.getAlbum_name().equals(song.getAlbum_name())) {
                            index = playlist.indexOf(s);
                        }
                    }
                    storageUtil.storeAudio(playlist);
                    storageUtil.storeAudioIndex(index);
                    Log.d(TAG, "onItemClick: storage = " + storageUtil.loadAudio().size());
                    Intent setplaylist = new Intent(MainActivity.Broadcast_NEW_ALBUM);
                    sendBroadcast(setplaylist);
                    Intent broadcastIntent = new Intent(MainActivity.Broadcast_PLAY_NEW_AUDIO);
                    sendBroadcast(broadcastIntent);
                    closeDrawer();

                } else {
                    int index = 0;
                    Log.i(TAG, "onItemClick: " + song.getSong_title() + " " + song.getAlbum_name());
                    ArrayList<song> array = new StorageUtil(getApplicationContext()).loadAudio();
                    for (song s : array) {
                        if (s.getSong_title().equals(song.getSong_title()) && s.getAlbum_name().equals(song.getAlbum_name())) {
                            Log.i(TAG, "onItemClick: " + s.getSong_title() + " " + s.getAlbum_name());
                            index = array.indexOf(s);
                            Log.i(TAG, "onItemClick: " + s.getSong_title() + " " + s.getAlbum_name() + " " + index);
                        }
                    }
                    storageUtil.storeAudioIndex(index);
                    Intent broadcastIntent = new Intent(MainActivity.Broadcast_PLAY_NEW_AUDIO);
                    sendBroadcast(broadcastIntent);
                    closeDrawer();
                }


            }
        }));

        rv2 = (ExpandableListView) findViewById(R.id.rv2);
        rv2.setAdapter(albumAdapter);

        rv2.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {

                List<albums> model = albumAdapter.get_listDataHeader();
                HashMap<String, List<song>> map = albumAdapter.get_listDataChild();


                List<song> songlistalbums = map.get(model.get(i).getAlbum_name());
                StorageUtil storageUtil = new StorageUtil(getApplicationContext());

                playlist.clear();
                for (song songs : songlistalbums) {
                    song s = new song(songs.getSong_id(), songs.getSong_title(), songs.getAlbum_id(), songs.getAlbum_name(), songs.getDownload_link(), songs.getLyricist(), songs.getTrack_no());
                    playlist.add(s);
                }

                storageUtil.storeAudio(playlist);
                storageUtil.storeAudioIndex(i1);
                Intent setplaylist = new Intent(MainActivity.Broadcast_NEW_ALBUM);
                sendBroadcast(setplaylist);
                Intent broadcastIntent = new Intent(MainActivity.Broadcast_PLAY_NEW_AUDIO);
                sendBroadcast(broadcastIntent);
                closeDrawer();

                return false;
            }
        });
        albumList = new ArrayList<>();
        albumSongList = new HashMap<>();

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(true);

        Dialog = new ProgressDialog(this);
        Dialog.setCancelable(true);

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ImageButton menuLeft = (ImageButton) findViewById(R.id.menuleft);
        ImageButton menuRight = (ImageButton) findViewById(R.id.menuright);
        menuLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    drawer.openDrawer(GravityCompat.START);
                }
            }
        });

        menuRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawer.isDrawerOpen(GravityCompat.END)) {
                    drawer.closeDrawer(GravityCompat.END);
                } else {
                    drawer.openDrawer(GravityCompat.END);
                }
            }
        });


    }


    private void getupdatetime(String local_time_songs,String local_time_albums) {
        AndroidNetworking.post(AppConfig.GET_UPDATE_TIME)
                .addBodyParameter("updated", "checking update")
                .setTag("update time")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "onResponse: "+response);
                        try {
                            JSONArray update = response.getJSONArray("update");
                            //Log.d(TAG, "onResponse: length = "+update.length());
                            for(int a =0;a<update.length();a++){
                                JSONObject object = update.getJSONObject(a);

                                String table_name = String.valueOf(object.get("table_name"));
                                String remote_time = String.valueOf(object.get("update_time"));
                                if(table_name.equals("albums")){

                                    long local_time_long = Long.parseLong(local_time_albums);
                                    long remote_time_long = Long.parseLong(remote_time);
                                    if(local_time_albums.equals("0")){
                                             updateBoth++;
                                            getAlbums(table_name,remote_time,local_time_albums);


                                    }else if(remote_time_long>local_time_long){
                                            updateBoth++;
                                            getAlbums(table_name, remote_time, local_time_albums);

                                    }else {
                                        updateBoth++;
                                        callSetUp();
                                    }
                                }else if(table_name.equals("songs")){

                                    long local_time_long = Long.parseLong(local_time_songs);
                                    long remote_time_long = Long.parseLong(remote_time);
                                    if(local_time_songs.equals("0")){
                                        updateBoth++;
                                            getSongs(table_name,remote_time,local_time_songs);


                                    }else if(remote_time_long>local_time_long){
                                        updateBoth++;

                                            getSongs(table_name, remote_time, local_time_songs);




                                    }else {
                                        updateBoth++;
                                        callSetUp();
                                    }
                                }

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                    @Override
                    public void onError(ANError error) {
                        Log.e(TAG, "onError: "+error.getErrorDetail());
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                        builder.setTitle("Error While Connecting");
                        builder.setMessage("oops Looks like network issues make sure your internet connection is on and try again... ");
                        builder.setNegativeButton("Quit",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        System.exit(1);
                                    }
                                });
                        builder.setPositiveButton("Try again",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int which) {

                                        String local_time_songs = dbHandler.getUpdateDetails("songs");
                                        String local_time_albums = dbHandler.getUpdateDetails("albums");
                                        pDialog.setMessage("loading songs");
                                        showDialog();
                                        getupdatetime(local_time_songs,local_time_albums);

                                    }
                                });

                        builder.show();
                        hideDialog();
                        //setVisibleFalse();
                        //isLoading = false;
                    }
                });
    }

    private void callSetUp() {
        if(updateBoth==2){
            setUpSongs();
            if (session.isLoggedIn()) {
                getFavFromDatabase(db.getUserDetails().get("id"));
            }
        }
    }

    public void getAlbums(String table_name, String remote_time, String local_time){
        AndroidNetworking.post(AppConfig.GET_ALBUMS)
                .addBodyParameter("artist", "1")
                .setTag("albums")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "onResponse: "+response);

                        try {

                            if(response.getString("error").equals("false")) {

                                if(!local_time.equals("0")){
                                    dbHandler.deleteRecords(table_name);
                                }
                                JSONArray array = response.getJSONArray("albums");
                                for (int a = 0; a < array.length(); a++) {
                                    JSONObject object = array.getJSONObject(a);
                                    String album_id = object.getString("album_id");
                                    String album_name = object.getString("album_name");
                                    String hero = object.getString("hero");
                                    String heroin = object.getString("heroin");
                                    String language = object.getString("language");
                                    String year = object.getString("year");
                                    String image_link = image_path + album_id + ".png";
                                    if(dbHandler.insertAlbums(album_id, album_name, hero, heroin, language, year, image_link)){

                                    }else {
                                        Log.d(TAG, "onResponse: error inserting albums in local database");
                                    }


                                }
                                callSetUp();
                                if(local_time.equals("0")){
                                    dbHandler.insertUpdate(table_name,remote_time);
                                }else {
                                    dbHandler.updateUpdateTable(table_name,remote_time);
                                }
                                Log.d(TAG, "onResponse: length " + array.length());

                            }else {
                                Toast.makeText(getApplicationContext(), "Failed to get albums from database", Toast.LENGTH_SHORT).show();

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }



                    }
                    @Override
                    public void onError(ANError error) {
                        Log.e(TAG, "onError: "+error.getErrorDetail());
                        Toast.makeText(getApplicationContext(), "error loading albums from the database", Toast.LENGTH_SHORT).show();

                        //isLoading = false;
                    }
                });
    }

    public void checkForUpdate(){
        AndroidNetworking.post(AppConfig.GET_APP_UPDATE)
                .addBodyParameter("app", "app")
                .setTag("update")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "onResponse: "+response);
                        try {
                            if(response.getString("error").equals("false")) {

                                try {
                                    PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                                    String version = pInfo.versionName;
                                    int versioncode = pInfo.versionCode;
                                    Log.d(TAG, "onResponse: versionname = " + version + " version code = " + versioncode);

                                    JSONArray array = response.getJSONArray("app");
                                    JSONObject object = array.getJSONObject(0);
                                    int vc = object.getInt("version_code");
                                    String versionName = object.getString("version_name");
                                    if(vc>versioncode){
                                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                                        builder.setTitle("Update Available");


                                        builder.setMessage("A new Version ("+versionName+"("+vc+")) available with performance improvement,stability and important bug fixes, please udpate now");

                                        builder.setNegativeButton("Not Now",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog,
                                                                        int which) {
                                                        return;
                                                    }
                                                });

                                        builder.setPositiveButton("Update Now!",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog,
                                                                        int which) {
                                                        final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                                                        try {
                                                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                                        } catch (android.content.ActivityNotFoundException anfe) {
                                                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                                        }
                                                    }
                                                });

                                        builder.show();
                                    }
                                } catch (PackageManager.NameNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                    @Override
                    public void onError(ANError error) {
                        Log.e(TAG, "onError: "+error.getErrorDetail());
                        Toast.makeText(getApplicationContext(), "Error checking for updates", Toast.LENGTH_SHORT).show();

                        //isLoading = false;
                    }
                });
    }

    public void getSongs(String table_name, String remote_time, String local_time){
        AndroidNetworking.post(AppConfig.GET_ALL_SONGS)
                .addBodyParameter("songs", "all")
                .setTag("songs")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "onResponse: "+response);

                        try {

                            if(response.getString("error").equals("false")) {

                                if(!local_time.equals("0")){
                                    dbHandler.deleteRecords(table_name);
                                }
                                JSONArray array = response.getJSONArray("songs");
                                for (int a = 0; a < array.length(); a++) {
                                    JSONObject object = array.getJSONObject(a);
                                    String song_id = object.getString("song_id");
                                    String id = object.getString("album_id");
                                    String song_title = object.getString("song_title");
                                    String download_link = object.getString("download_link");
                                    String lyricist = object.getString("lyricist");
                                    String track_no = object.getString("track_no");

                                    if(dbHandler.insertSongs(song_id, id, song_title, download_link, lyricist, track_no)){

                                    }else {
                                        Log.d(TAG, "onResponse: error inserting albums in local database");
                                    }


                                }
                                callSetUp();
                                if(local_time.equals("0")){
                                    dbHandler.insertUpdate(table_name,remote_time);
                                }else {
                                    dbHandler.updateUpdateTable(table_name,remote_time);
                                }
                                Log.d(TAG, "onResponse: length " + array.length());

                            }else {
                                Log.d(TAG, "onResponse: failed to get songs from database");
                                Toast.makeText(getApplicationContext(), "Failed to get albums from database", Toast.LENGTH_SHORT).show();

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }



                    }
                    @Override
                    public void onError(ANError error) {
                        Log.e(TAG, "onError: "+error.getErrorDetail());
                        Toast.makeText(getApplicationContext(), "error loading albums from the database", Toast.LENGTH_SHORT).show();

                        //isLoading = false;
                    }
                });
    }

    public void handleIntent() {

        newIntent = true;

    }

    public void handleNotificationsAndLinks(){
        String songIdFromFirebase = "";

        Log.d(TAG, "handleIntent: called");
        if (getIntent().getStringExtra("song") != null&&getIntent().getStringExtra("des")!=null&&getIntent().getStringExtra("title")!=null) {
            Log.d(TAG, "onCreate: " + getIntent().getStringExtra("song"));
            songIdFromFirebase = getIntent().getStringExtra("song");
            desFromfirebase = getIntent().getStringExtra("des");
            titleFromFirebase = getIntent().getStringExtra("title");
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            builder.setTitle(titleFromFirebase);


            builder.setMessage(desFromfirebase);

            builder.setNegativeButton("Don't Play",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            return;
                        }
                    });
            String finalSongIdFromFirebase = songIdFromFirebase;
            builder.setPositiveButton("Play Now!",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            handleSongRequest(finalSongIdFromFirebase);
                        }
                    });

            builder.show();
        }else if(getIntent().getStringExtra("album")!=null&&getIntent().getStringExtra("des")!=null){

            String albumName  = getIntent().getStringExtra("album");
            Log.d(TAG, "handleNotificationsAndLinks: "+albumName);
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            builder.setTitle(Helper.FirstLetterCaps(albumName));


            builder.setMessage(getIntent().getStringExtra("des")+" check out album list now");

            builder.setNegativeButton("Don't Play",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            return;
                        }
                    });
            String finalSongIdFromFirebase = songIdFromFirebase;
            builder.setPositiveButton("Play Songs!",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            handleAlbumRequest(albumName);
                        }
                    });

            builder.show();



        }else {

            Log.d(TAG, "handleIntent: am inside else");
            Intent appLinkIntent = getIntent();
            String appLinkAction = appLinkIntent.getAction();
            Uri appLinkData = appLinkIntent.getData();
            if (appLinkData != null) {
                String songId = appLinkData.getQueryParameter("song");
                Log.d(TAG, "calls: song =  " + songId);
                if (songId == null) {
                    return;
                }
                handleSongRequest(songId);

            }else {
                checkForUpdate();
            }
        }



    }

        @Override
        protected void onNewIntent (Intent intent){
            super.onNewIntent(intent);
            setIntent(intent);
            handleIntent();
        }

    public void share(View view) {
        if (player != null) {
            if (player.isPlaying() || player.isPaused()) {
                //Toast.makeText(this, "click share", Toast.LENGTH_SHORT).show();
                Bitmap bitmap;
                Bitmap b;

                try {
                    RelativeLayout layout = (RelativeLayout) findViewById(R.id.maincontent);

                    layout.getRootView().setDrawingCacheEnabled(true);

                    bitmap = Bitmap.createBitmap(layout.getRootView().getDrawingCache());
                    layout.getRootView().setDrawingCacheEnabled(false);

                } catch (Exception e) {
                    Toast.makeText(this, "something went wrong try again later", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (bitmap == null) {
                    Toast.makeText(this, "something went wrong try again later", Toast.LENGTH_SHORT).show();
                    return;
                }


                File mainDir = new File(
                        getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share");

                //If File is not present create directory
                if (!mainDir.exists()) {
                    if (mainDir.mkdir())
                        Log.e("Create Directory", "Main Directory Created : " + mainDir);
                }

                File savefile = mainDir;


                File dir = new File(savefile.getAbsolutePath());
                if (!dir.exists())
                    dir.mkdirs();
                File file = new File(savefile.getAbsolutePath(), "song.jpg");
                try {
                    FileOutputStream fOut = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
                    fOut.flush();
                    fOut.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Uri uri = Uri.fromFile(file);//Convert file path into Uri for sharing
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                song song = player.getActiveSong();
                String songName = song.getSong_title().replaceAll(" ", "%20");
                String link = AppController.getAppLink() + "/?song=" + songName;
                sendIntent.putExtra(Intent.EXTRA_TEXT, link);
                sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
                sendIntent.setType("image/*");
                sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(sendIntent, "Choose your loved one to share this song"));
            } else {
                Toast.makeText(this, "Play a song to share!", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void setUp_favoritePanel() {
        Log.d(TAG, "setUp_favoritePanel: called favorite");
        up = (ImageView) findViewById(R.id.favup);


        favoritePanel = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        favoritePanel.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (favoritePanel != null &&
                        (favoritePanel.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED || favoritePanel.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
                    if (mInterstitialAd != null) {
                        if (mInterstitialAd.isLoaded()) {
                            mInterstitialAd.show();
                        } else {
                            Log.d("TAG", "The interstitial wasn't loaded yet.");
                        }
                    }
                    up.setImageResource(R.drawable.down);
                } else if (favoritePanel != null &&
                        (favoritePanel.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED)) {
                    up.setImageResource(R.drawable.up);
                }
            }
        });
        favoritePanel.setAnchorPoint(0.7f);

        favFragment = new FavFragment();
        aboutFragment = new about();
        // appsFragment = new apps();

        viewPager2 = (CustomViewPager) findViewById(R.id.rvg);
        favPageAdapter favPageAdapter = new favPageAdapter(getSupportFragmentManager());
        favPageAdapter.addFragment(favFragment, "Favorite");
        favPageAdapter.addFragment(aboutFragment, "About");
        // favPageAdapter.addFragment(appsFragment, "Apps");


        viewPager2.setAdapter(favPageAdapter);
        viewPager2.setPagingEnabled(false);

        bottomMenu = (BottomNavigationView) findViewById(R.id.navigation);

        bottomMenu.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.navigation_home) {
                    viewPager2.setCurrentItem(0);
                } else if (item.getItemId() == R.id.navigation_notifications) {
                    viewPager2.setCurrentItem(1);
                }/* else if (item.getItemId() == R.id.navigation_dashboard) {
                    viewPager2.setCurrentItem(2);
                }*/
                updateNavigationBarState(item.getItemId());

                return true;
            }
        });

    }

    private void updateNavigationBarState(int actionId) {
        Menu menu = bottomMenu.getMenu();

        for (int i = 0, size = menu.size(); i < size; i++) {
            MenuItem item = menu.getItem(i);
            item.setChecked(item.getItemId() == actionId);
        }
    }

    @Override
    public void onBackPressed() {
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (drawer.isDrawerOpen(GravityCompat.END)) {
            drawer.closeDrawer(GravityCompat.END);
        } else if (favoritePanel != null &&
                (favoritePanel.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED || favoritePanel.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
            favoritePanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

        } else {

            super.onBackPressed();
        }
    }

    public void closeDrawer() {
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (drawer.isDrawerOpen(GravityCompat.END)) {
            drawer.closeDrawer(GravityCompat.END);
        }
    }
    public void OpenDrawer() {
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if(!drawer.isDrawerVisible(GravityCompat.END)){
            drawer.openDrawer(GravityCompat.END);
        }
    }

    private void setUpLyricsPage() {

        mHandler.post(runnable);
        playpause = (ImageButton) findViewById(R.id.playpause);
        previous = (ImageButton) findViewById(R.id.previous);
        next = (ImageButton) findViewById(R.id.next);
        shuffle = (ImageButton) findViewById(R.id.shuffle);
        fav = (ImageButton) findViewById(R.id.fav_pop);
        fav.setOnClickListener(this);

        viewPager = (CustomViewPager) findViewById(R.id.vg);
        //viewPager.setPagingEnabled(false);
        englishFragment = new EnglishFragment();
        tamilFragment = new TamilFragment();
        currentTime = (TextView) findViewById(R.id.currentTime);
        totalTime = (TextView) findViewById(R.id.totalTime);
        songname = (TextView) findViewById(R.id.songname);
        moviename = (TextView) findViewById(R.id.moviename);

        segmentedGroup = (SegmentedGroup) findViewById(R.id.segmented);
        segmentedGroup.setTintColor(getResources().getColor(R.color.amber_900));
        segmentedGroup.check(R.id.tamil);
        segmentedGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.tamil:
                        viewPager.setCurrentItem(0);
                        break;
                    case R.id.english:
                        viewPager.setCurrentItem(1);
                        break;
                }
            }
        });
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    segmentedGroup.check(R.id.tamil);
                } else if (position == 1) {
                    segmentedGroup.check(R.id.english);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        SectionsPagerAdapter lyricsAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        lyricsAdapter.addFragment(tamilFragment, "Tamil");
        lyricsAdapter.addFragment(englishFragment, "English");
        viewPager.setAdapter(lyricsAdapter);
        playpause.setOnClickListener(this);
        previous.setOnClickListener(this);
        next.setOnClickListener(this);
        shuffle.setOnClickListener(this);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    if (player != null) {
                        if (player.mediaPlayer != null) {
                            player.seekTo(i);
                        }
                    }

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (player != null) {
                if (player.isPlaying()) {
                    int position = player.getCurrentPosition();
                    seekBar.setProgress(position);
                    currentTime.setText(Helper.durationCalculator(position));

                }
            }
            mHandler.postDelayed(runnable, 1000);
        }
    };

    private void setUpSongsAlbums() {
        StorageUtil storageUtil = new StorageUtil(getApplicationContext());
        storageUtil.clearCachedAudioPlaylist();
        songList = dbHandler.getSongs();
        songAdapter = new SongAdapter(getApplicationContext(), songList);
        rv1.setAdapter(songAdapter);
        Log.d(TAG, "setUpSongs: " + songList.size());
        songAdapter.notifyDataSetChanged();

        albumList = dbHandler.getAlbums();

        for (albums a : albumList) {
            int index = albumList.indexOf(a);
            List<song> oneAlbumSongs = dbHandler.getSongsByAlbumId(a.getAlbum_id());
            albumList.get(index).setList(oneAlbumSongs);
            albumSongList.put(a.getAlbum_name(), a.getSonglist());
        }

        Log.d(TAG, "setUpSongsAlbums: " + albumList.size());
        albumAdapter = new ExpandableListAdapterMysql(getApplicationContext(), albumList, albumSongList, MainActivity.this);
        rv2.setAdapter(albumAdapter);

        totalSongs = songList.size();
        hideDialog();
        //handleIntent();
        Log.d(TAG, "setUpSongsAlbums: dialog hidden");
        Log.d(TAG, "setUpSongs: " + songList.size());
    }

    private void setNavigation() {

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        NavigationView navigationView2 = (NavigationView) findViewById(R.id.nav_view2);
        View view = navigationView.getHeaderView(0);
        View view2 = navigationView2.getHeaderView(0);
        songsearch = (SearchView) view.findViewById(R.id.songsearch);
        songsearch.setQueryHint("Song, Year, Lyricist");
        songsearch.setOnQueryTextListener(this);
        albumsearch = (SearchView) view2.findViewById(R.id.albumsearch);
        albumsearch.setQueryHint("Movie Name, Year");
        albumsearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                query = query.toLowerCase().trim();
                Log.d(TAG, "setUpalbums: " + songList.size());
                if (query.length() < 1) {
                    albumAdapter = new ExpandableListAdapterMysql(getApplicationContext(), albumList, albumSongList, MainActivity.this);
                    rv2.setAdapter(albumAdapter);
                    Log.d(TAG, "setUpalbums: " + songList.size());
                    albumAdapter.notifyDataSetChanged();
                    return false;
                }
                if (query.isEmpty()) {
                    albumAdapter = new ExpandableListAdapterMysql(getApplicationContext(), albumList, albumSongList, MainActivity.this);
                    rv2.setAdapter(albumAdapter);
                    Log.d(TAG, "setUpalbums: " + songList.size());
                    albumAdapter.notifyDataSetChanged();
                    return false;
                }

                if (query.length() == 4) {

                    try {
                        int year = Integer.parseInt(query);
                        List<albums> FiltertedAlbumList = new ArrayList<>();
                        HashMap<String, List<song>> filteredAlbumSongList = new HashMap<>();
                     /*   for (albums album : albumList) {
                            Log.d(TAG, "setUpSongs: 4");
                            final String y = String.valueOf(album.getYear());
                            if (y.equals(query)) {
                                FiltertedAlbumList.add(album);

                            }
                        }*/
                        FiltertedAlbumList = dbHandler.getAlbumsByYear(year);
                        if (FiltertedAlbumList.size() > 0) {
                            for (albums album : FiltertedAlbumList) {
                                List<song> filteredlist = dbHandler.getSongsByAlbumId(album.getAlbum_id());
                                album.setList(filteredlist);
                                filteredAlbumSongList.put(album.getAlbum_name(), filteredlist);
                            }
                            albumAdapter = new ExpandableListAdapterMysql(getApplicationContext(), FiltertedAlbumList, filteredAlbumSongList, MainActivity.this);
                            rv2.setAdapter(albumAdapter);
                            albumAdapter.notifyDataSetChanged();
                        }


                        return false;
                    } catch (NumberFormatException e) {
                        Log.d(TAG, "onQueryTextChange: " + e.getMessage());
                    }
                }
                List<albums> FiltertedAlbumList = new ArrayList<>();
                HashMap<String, List<song>> filteredAlbumSongList = new HashMap<>();

               /* for (albums album : albumList) {
                    String text1 = album.getAlbum_name().toLowerCase();

                    if (text1.contains(query)) {
                        FiltertedAlbumList.add(album);
                        filteredAlbumSongList.put(album.getAlbum_name(),album.getSonglist());
                    }
                }*/
                FiltertedAlbumList = dbHandler.getAlbumsByName(query);
                if (FiltertedAlbumList.size() > 0) {

                    for (albums album : FiltertedAlbumList) {
                        List<song> filteredlist = dbHandler.getSongsByAlbumId(album.getAlbum_id());
                        album.setList(filteredlist);
                        filteredAlbumSongList.put(album.getAlbum_name(), filteredlist);
                    }
                    albumAdapter = new ExpandableListAdapterMysql(getApplicationContext(), FiltertedAlbumList, filteredAlbumSongList, MainActivity.this);
                    rv2.setAdapter(albumAdapter);
                    albumAdapter.notifyDataSetChanged();
                }


                return false;
            }
        });
    }





    private void downloadLyrics(final song song_id) {

        String tag_string_req = "req_lyrics";


        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.GET_LYRICS, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Albums Response: " + response.toString());


                Log.d(TAG, "onResponse: " + response);
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    JSONArray array = jObj.getJSONArray("lyrics");
                    for (int a = 0; a < array.length(); a++) {
                        JSONObject object = array.getJSONObject(a);
                        String lyrics_one = object.getString("lyrics_one");
                        String lyrics_two = object.getString("lyrics_two");
                        String lyrics_three = object.getString("lyrics_three");
                        String lyrics_four = object.getString("lyrics_four");
                        setLyrics(song_id, lyrics_one, lyrics_two, lyrics_three, lyrics_four);

                    }


                    Log.d(TAG, "onResponse: " + jObj);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Download Lyrics: " + error.getMessage());
                //Toast.makeText(getApplicationContext(),
                       // error.getMessage(), Toast.LENGTH_LONG).show();
                //signinTry();
                hideDialog();
                Log.d(TAG, "onErrorResponse: dialog hidden");
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                Log.d(TAG, "onResponse: song id = " + song_id.getSong_id());

                params.put("song_id", String.valueOf(song_id.getSong_id()));


                return params;
            }


        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
        Log.d(TAG, "downloadSongDatabase: " + (++request));
    }

    private void setUpSongs() {

        Handler handler = new Handler();
        final Runnable r = new Runnable() {
            @Override
            public void run() {
               /* for (int a : dbHandler.getAlbumIds()) {
                    albumArts.setBitmaps(a, dbHandler.getImageBlob(a));
                }*/
                setUpSongsAlbums();

                setUp_favoritePanel();
                if(newIntent){

                    newIntent = false;
                    handleNotificationsAndLinks();
                }


            }

        };
        handler.post(r);


    }



    //Google Sign in
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleSignInClient);
        startActivityForResult(signInIntent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == 1) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        } else {
            signinTry();
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            //updateUI(account);
            Toast.makeText(this, "Successfully Signed in as " + account.getDisplayName(), Toast.LENGTH_SHORT).show();
            String DisplayName = account.getDisplayName();
            String email = account.getEmail();


            registerUser(email, DisplayName);

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            signinTry();
            //updateUI(null);
        }
    }

    private void registerUser(final String email,
                              final String displayName) {
        // Tag used to cancel the request
        String tag_string_req = "req_register";


        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_REGISTER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response.toString());

                Log.d(TAG, "onResponse: dialog hidden");

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        // User successfully stored in MySQL
                        // Now store the user in sqlite
                        String id = jObj.getString("id");

                        JSONObject user = jObj.getJSONObject("user");
                        //int id = user.getInt("id");
                        String name = user.getString("displayName");
                        String email = user.getString("email");

                        // Inserting row in users table
                        db.addUser(Integer.parseInt(id), email, name);

                        Toast.makeText(getApplicationContext(), "User successfully registered. Try login now!", Toast.LENGTH_LONG).show();


                        checkLogin(email);
                    } else {

                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("error_msg");
                        if (errorMsg.contains("User already existed"))
                            checkLogin(email);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Registration Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                signinTry();
                hideDialog();
                Log.d(TAG, "onErrorResponse: dialoghidden");
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("displayName", displayName);


                return params;
            }

        };

        // Adding request to request queue

        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void checkLogin(final String email) {
        // Tag used to cancel the request
        String tag_string_req = "req_login";


        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_LOGIN, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response.toString());

                Log.d(TAG, "onResponse: dialog hidden");

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                        // user successfully logged in
                        // Create login session
                        session.setLogin(true);

                        // Now store the user in SQLite
                        String id = jObj.getString("id");

                        JSONObject user = jObj.getJSONObject("user");

                        String name = user.getString("displayName");
                        String email = user.getString("email");

                        // Inserting row in users table
                        db.addUser(Integer.parseInt(id), email, name);
                        if (db.getUserDetails().get("id") != null) {
                            getFavFromDatabase(db.getUserDetails().get("id"));
                        }

                        // Launch main activity

                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        //Toast.makeText(getApplicationContext(),
                        //errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                signinTry();
                hideDialog();
                Log.d(TAG, "onErrorResponse: dialoghidden");
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                return params;
            }

        };

        // Adding request to request queue
        Log.d(TAG, "checkLogin: " + strReq);
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();
        Auth.GoogleSignInApi.signOut(mGoogleSignInClient);       // Launching the login activity

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void signinTry() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setTitle("Error While Connecting");
        builder.setMessage("oops Looks like network issues make sure your internet connection is on and try again... ");
        builder.setNegativeButton("Quit",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        System.exit(1);
                    }
                });
        builder.setPositiveButton("Try again",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        signIn();
                    }
                });

        builder.show();

    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            player = binder.getService();
            player.setMainCallbacks(MainActivity.this);
            update();
            serviceBound = true;
            Log.d(TAG, "calls: now service bound true");
            handleIntent();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    @Override
    protected void onStart() {
        Log.i(TAG, "calls: on start called");
        super.onStart();
        if (!serviceBound) {
            Intent playerIntent = new Intent(MainActivity.this, MusicService.class);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);

            Log.i("calls", "service bounded");


        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("calls", "am in stop");
        if (serviceBound) {
            if (player != null) {
                player.setMainCallbacks(null);
            }
        }
        Log.i("calls", "finished");

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.playpause: {
                if (player != null) {
                    if (player.isPlaying()) {
                        playpause.setImageResource(R.drawable.play);
                        player.pauseMedia();
                    } else {
                        if (player.mediaPlayer != null) {
                            if (player.getCurrentPosition() > 0) player.resumeMedia();
                            playpause.setImageResource(R.drawable.pause);
                        }
                    }
                }
                break;
            }
            case R.id.previous: {
                if (player != null) {
                    if (player.isPlaying()) {
                        if (new StorageUtil(getApplicationContext()).loadAudio().size() > 0) {
                            player.skipToPrevious();
                        }
                    }
                }
                break;
            }
            case R.id.next: {
                if (player != null) {
                    if (player.isPlaying()) {
                        if (new StorageUtil(getApplicationContext()).loadAudio().size() > 0) {
                            player.skipToNext();
                        }
                    }
                }
                break;
            }
            case R.id.shuffle: {
                StorageUtil storage = new StorageUtil(getApplicationContext());
                ArrayList<song> playlist = (storage.loadAudio());
                if(playlist==null){
                    Toast.makeText(this, "no playlist to shuffle", Toast.LENGTH_SHORT).show();
                    return;

                }
                if (player != null) {
                    if (player.isShuffleOn()) {
                        player.setShuffleOnOff(false);
                        shuffle.setImageResource(R.drawable.shuffleoff);
                    } else {
                        player.setShuffleOnOff(true);
                        shuffle.setImageResource(R.drawable.shuffle);

                    }
                }
                break;
            }
            case R.id.fav_pop: {

                if (player != null) {
                    if (player.mediaPlayer != null) {
                        song s = player.getActiveSong();
                        String song_id = String.valueOf(s.getSong_id());
                        if (session.isLoggedIn()) {
                            boolean exists = dbHandler.isFavExists(Integer.parseInt(db.getUserDetails().get("id")), Integer.parseInt(song_id));
                            Log.d(TAG, "onClick: fav = " + exists);
                            if (exists) {

                                deleteFromFavDatabase(String.valueOf(s.getSong_id()));

                            } else {
                                addFavToDatabase(String.valueOf(s.getSong_id()));

                            }

                        } else {
                            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
                        }
                        
                        
                        /*if (checkFavoriteItem()) {
                            removeFavorite(s);

                        } else {
                            addFavorite(s);

                        }*/
                    }
                }


                break;

            }

        }
    }

    private void getFavFromDatabase(String user_id) {

        String tag_string_req = "get_fav";


        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.GET_FAV, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                //Log.d(TAG, "Albums Response: " + response.toString());


                // Log.d(TAG, "onResponse: "+response);
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        JSONArray array = jObj.getJSONArray("favs");
                        for (int a = 0; a < array.length(); a++) {
                            JSONObject object = array.getJSONObject(a);
                            int user_id = object.getInt("user_id");
                            int song_id = object.getInt("song_id");


                            dbHandler.insertFavorites(user_id, song_id);

                        }

                        setUp_favoritePanel();
                    }


                    Log.d(TAG, "onResponse: " + jObj);
                    //hideDialog();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "get fav: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                //signinTry();

                Log.d(TAG, "onErrorResponse: dialog hidden");
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("user_id", user_id);
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void addFavToDatabase(String song_id) {

        String tag_string_req = "add_fav";

        pDialog.setMessage("Adding Favorite ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.ADD_FAV, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                //Log.d(TAG, "Albums Response: " + response.toString());


                // Log.d(TAG, "onResponse: "+response);
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        dbHandler.insertFavorites(Integer.parseInt(db.getUserDetails().get("id")), Integer.parseInt(song_id));
                    }
                    if (dbHandler.isFavExists(Integer.parseInt(db.getUserDetails().get("id")), Integer.parseInt(song_id))) {
                        fav.setImageResource(R.drawable.favon);
                    }
                    hideDialog();
                    setUp_favoritePanel();
                    Log.d(TAG, "onResponse: " + jObj);


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "add fav: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                //signinTry();

                Log.d(TAG, "onErrorResponse: dialog hidden");
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("user_id", db.getUserDetails().get("id"));
                params.put("song_id", song_id);


                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void deleteFromFavDatabase(String song_id) {

        String tag_string_req = "delete_fav";

        pDialog.setMessage("Deleting Favorite ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.DELETE_FAV, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                //Log.d(TAG, "Albums Response: " + response.toString());


                // Log.d(TAG, "onResponse: "+response);
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    Log.d(TAG, "onResponse: " + jObj);
                    if (!error) {
                        dbHandler.deleteFavorites(Integer.parseInt(db.getUserDetails().get("id")), Integer.parseInt(song_id));
                    }
                    if (!dbHandler.isFavExists(Integer.parseInt(db.getUserDetails().get("id")), Integer.parseInt(song_id))) {
                        fav.setImageResource(R.drawable.heart);
                    }
                    hideDialog();
                    setUp_favoritePanel();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "delete Fav: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                //signinTry();
                hideDialog();
                Log.d(TAG, "onErrorResponse: dialog hidden");
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("user_id", db.getUserDetails().get("id"));
                params.put("song_id", song_id);


                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    @Override
    public void update() {
        if (player != null && player.mediaPlayer != null) {
            if (player.isPlaying()) {
                if (pDialog.isShowing()) {
                    pDialog.dismiss();

                }
                if (Dialog.isShowing()) {
                    Dialog.dismiss();
                }

                seekBar.setMax(player.getDuration());
                totalTime.setText(Helper.durationCalculator(player.getDuration()));
                playpause.setImageResource(R.drawable.pause);
                downloadLyrics(player.getActiveSong());

                /*if (checkFavoriteItem()) {
                    fav.setImageResource(R.drawable.favon);
                } else {
                    fav.setImageResource(R.drawable.heart);
                }*/
                Log.i("CalledSet", "called set details");

            } else {

                if (player.mediaPlayer != null) {
                    seekBar.setMax(player.getDuration());
                    playpause.setImageResource(R.drawable.play);
                }


                //playpause.setImageResource(android.R.drawable.ic_media_play);
            }


        }
    }


    @Override
    public void showDialog(song s) {
        if (Dialog != null) {
            Log.i(TAG, "showDialog: loading ");
            downloadLyrics(s);
            if (session.isLoggedIn()) {
                if (dbHandler.isFavExists(Integer.parseInt(db.getUserDetails().get("id")), s.getSong_id())) {
                    fav.setImageResource(R.drawable.favon);
                } else {
                    fav.setImageResource(R.drawable.heart);
                }
            }
            if (!player.isPlaying()) {
                Dialog.setMessage("Loading " + Helper.FirstLetterCaps(s.getSong_title()) + "\nFrom " + Helper.FirstLetterCaps(s.getAlbum_name()));
                Dialog.show();
            }

        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        query = query.toLowerCase().trim();
        Log.d(TAG, "setUpSongs: " + songList.size());
        if (query.length() < 1) {
            songAdapter = new SongAdapter(getApplicationContext(), songList);
            rv1.setAdapter(songAdapter);
            Log.d(TAG, "setUpSongs: " + songList.size());
            songAdapter.notifyDataSetChanged();
            return false;
        }
        if (query.isEmpty()) {
            songAdapter = new SongAdapter(getApplicationContext(), songList);
            rv1.setAdapter(songAdapter);
            Log.d(TAG, "setUpSongs: " + songList.size());
            songAdapter.notifyDataSetChanged();
            return false;
        }

        if (query.length() == 4) {

            try {
                int year = Integer.parseInt(query);
                    /*final List<song> filteralbumlist = new ArrayList<>();
                    for (song songs : songList) {
                        Log.d(TAG, "setUpSongs: 4");
                        final String text4 = String.valueOf(dbHandler.getYearByAlbumId(songs.getAlbum_id()));
                        if (text4.equals(query)) {
                            filteralbumlist.add(songs);
                            Log.d(TAG, "onQueryTextChange: " + filteralbumlist.size());
                            songAdapter = new SongAdapter(getApplicationContext(), filteralbumlist);
                            rv1.setAdapter(songAdapter);
                            Log.d(TAG, "setUpSongs: "+songList.size());
                            songAdapter.notifyDataSetChanged();
                        }
                    }*/
                final List<song> filtersonglist = dbHandler.getSongsByYear(year);
                if (filtersonglist.size() > 0) {
                    songAdapter = new SongAdapter(getApplicationContext(), filtersonglist);
                    rv1.setAdapter(songAdapter);
                    Log.d(TAG, "setUpSongs: " + songList.size());
                    songAdapter.notifyDataSetChanged();
                }


                return false;
            } catch (NumberFormatException e) {
                Log.d(TAG, "onQueryTextChange: " + e.getMessage());
            }
        }

        List<song> filtersonglist = new ArrayList<>();
           /* for (song songs : songList) {
                final String text1 = songs.getSong_title().toLowerCase();
                final String text2 = songs.getAlbum_name().toLowerCase();
                final String text3 = songs.getLyricist().toLowerCase();

                if (text1.contains(query) || text2.contains(query) || text3.contains(query)) {
                    filteralbumlist.add(songs);
                    Log.d(TAG, "onQueryTextChange: "+filteralbumlist.size());
                    Log.d(TAG, "onQueryTextChange: "+text1+" ,"+text2+" ,"+text3);
                    Log.d(TAG, "onQueryTextChange: " + filteralbumlist.size());
                    songAdapter = new SongAdapter(getApplicationContext(), filteralbumlist);
                    rv1.setAdapter(songAdapter);
                    Log.d(TAG, "setUpSongssetUpSongs: "+songList.size());
                    songAdapter.notifyDataSetChanged();
                }

        }*/
        filtersonglist = dbHandler.getSongsBySearch(query);
        if (filtersonglist.size() > 0) {
            songAdapter = new SongAdapter(getApplicationContext(), filtersonglist);
            rv1.setAdapter(songAdapter);
            Log.d(TAG, "setUpSongs: " + songList.size());
            songAdapter.notifyDataSetChanged();
        }

        return false;
    }


    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

    }

    private void setLyrics(song s, String lyrics_one, String lyrics_two, String lyrics_three, String lyrics_four) {
        String movieTitle = s.getAlbum_name();
        String songTitle = s.getSong_title();
        songname.setText(Helper.FirstLetterCaps(songTitle));
        moviename.setText(Helper.FirstLetterCaps(movieTitle));

        //HashMap<String, Object> songs = (HashMap<String, Object>) values.get(movieTitle);
        //HashMap<String, Object> songlyrics = (HashMap<String, Object>) songs.get(songTitle);
        String english1 = lyrics_one;
        String english2 = lyrics_two;
        englishFragment.setLyrics(english1, english2);

        String tamil1 = lyrics_three;
        String tamil2 = lyrics_four;
        tamilFragment.setLyrics(tamil1, tamil2);


    }

    /*private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        int id;
        Activity activity;

        public DownloadImageTask(int id, Activity activity) {
            this.id = id;
            this.activity = activity;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Log.i("LEGGERE", urldisplay);
            Bitmap mIcon11 = null;
            try {
                URL url = new URL(urldisplay);
                mIcon11 = BitmapFactory.decodeStream(url.openConnection().getInputStream());

                if (null != mIcon11)
                    Log.i("BITMAP", "ISONOTNULL");
                else
                    Log.i("BITMAP", "ISNULL");
            } catch (Exception e) {
                Log.e("Error", "PORCA VACCA");

            }

            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            result.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] imagebyte = stream.toByteArray();
            if (dbHandler.getImageBlob(id) == null) {
                dbHandler.insertImage(String.valueOf(id), imagebyte);
            }
            checkTaskComplete();

        }
    }*/

    /*public void checkTaskComplete() {
        ++imageReqCom;
        if (imageReqCom == dbHandler.getNoOfAlbums()) {
            setUpImages();
            //setUpSongsAlbums();

            if (session.isLoggedIn()) {
                getFavFromDatabase(db.getUserDetails().get("id"));
            }

        }
    }*/


    public class favPageAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public favPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

    }


    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy: on destroy called");
        if (serviceBound) {
            unbindService(serviceConnection);
            serviceBound = false;
            player.setMainCallbacks(null);
        }

        /*NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();*/
        Log.i(TAG, "onDestroy: am in destory");
        if (pDialog != null) {
            pDialog.dismiss();
        }
        if (pDialog != null) {
            pDialog.dismiss();
        }
        super.onDestroy();


    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "calls: on resume called");
        if (serviceBound) {
            if (player != null) {
                player.setMainCallbacks(MainActivity.this);
                if (player.isPlaying()) {
                    Log.d(TAG, "calls: playing");
                    downloadLyrics(player.getActiveSong());
                }
                update();

            }
        }


        Log.i(TAG, "calls: on resume over");
    }

    public void handleSongRequest(String songId) {
        Log.d(TAG, "handleSongRequest: "+songId);
        songId = songId.replaceAll("%20", " ");
        songId = songId.toUpperCase();
        StorageUtil storageUtil = new StorageUtil(getApplicationContext());
        song song = dbHandler.getSongBySongTitle(songId);
        if (song == null) {
            Toast.makeText(player, songId + " is not found in our database", Toast.LENGTH_SHORT).show();
            return;
        }
        if (storageUtil.loadAudio() == null || totalSongs > storageUtil.loadAudio().size()) {
            Log.d(TAG, "calls: its null");
            for (song songs : songList) {
                song s = new song(songs.getSong_id(), songs.getSong_title(), songs.getAlbum_id(), songs.getAlbum_name(), songs.getDownload_link(), songs.getLyricist(), songs.getTrack_no());
                playlist.add(s);
            }
            Log.d(TAG, "calls: playlist = " + playlist.size());
            int index = 0;
            for (song s : playlist) {
                if (s.getSong_title().equals(song.getSong_title()) && s.getAlbum_name().equals(song.getAlbum_name())) {
                    index = playlist.indexOf(s);
                    Log.d(TAG, "calls: " + s);
                }
            }
            storageUtil.storeAudio(playlist);
            storageUtil.storeAudioIndex(index);
            Log.d(TAG, "calls: storage = " + storageUtil.loadAudio().size() + " service bond = " + serviceBound);
            Intent setplaylist = new Intent(MainActivity.Broadcast_NEW_ALBUM);
            sendBroadcast(setplaylist);
            Intent broadcastIntent = new Intent(MainActivity.Broadcast_PLAY_NEW_AUDIO);
            sendBroadcast(broadcastIntent);


        } else {
            int index = 0;
            Log.i(TAG, "calls: " + song.getSong_title() + " " + song.getAlbum_name());
            ArrayList<song> array = new StorageUtil(getApplicationContext()).loadAudio();
            for (song s : array) {
                if (s.getSong_title().equals(song.getSong_title()) && s.getAlbum_name().equals(song.getAlbum_name())) {
                    Log.i(TAG, "calls: " + s.getSong_title() + " " + s.getAlbum_name());
                    index = array.indexOf(s);
                    Log.i(TAG, "ca;;s: " + s.getSong_title() + " " + s.getAlbum_name() + " " + index);
                }
            }
            storageUtil.storeAudioIndex(index);
            Intent broadcastIntent = new Intent(MainActivity.Broadcast_PLAY_NEW_AUDIO);
            sendBroadcast(broadcastIntent);

        }

        Log.d(TAG,"calls: got false in handle itself");
    }
    public void handleAlbumRequest(String AlbumName) {
        Log.d(TAG, "handleAlbumRequest: "+AlbumName);
        AlbumName = AlbumName.toUpperCase();
        List<albums> model = albumAdapter.get_listDataHeader();
        HashMap<String, List<song>> map = albumAdapter.get_listDataChild();


        List<song> songlistalbums = map.get(AlbumName);
        StorageUtil storageUtil = new StorageUtil(getApplicationContext());
        if(songlistalbums!=null) {
            playlist.clear();
            for (song songs : songlistalbums) {
                song s = new song(songs.getSong_id(), songs.getSong_title(), songs.getAlbum_id(), songs.getAlbum_name(), songs.getDownload_link(), songs.getLyricist(), songs.getTrack_no());
                playlist.add(s);
            }

            storageUtil.storeAudio(playlist);
            storageUtil.storeAudioIndex(0);
            Intent setplaylist = new Intent(MainActivity.Broadcast_NEW_ALBUM);
            sendBroadcast(setplaylist);
            Intent broadcastIntent = new Intent(MainActivity.Broadcast_PLAY_NEW_AUDIO);
            sendBroadcast(broadcastIntent);
            OpenDrawer();
            if (albumsearch != null) {
                albumsearch.setQuery(Helper.FirstLetterCaps(AlbumName), true);
            }
            Log.d(TAG, "handleAlbumRequest: " + model.indexOf(AlbumName));
        }else {
            Toast.makeText(this, Helper.FirstLetterCaps(AlbumName)+" not found in the database", Toast.LENGTH_SHORT).show();
        }
    }

    public int getVersionCode(){
        PackageInfo pinfo = null;
        int versioncode = 0;
        try {
            pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versioncode = pinfo.versionCode;
            String versionName = pinfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
       return versioncode;
    }

}




