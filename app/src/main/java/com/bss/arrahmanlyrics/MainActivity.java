package com.bss.arrahmanlyrics;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bss.arrahmanlyrics.Fragments.EnglishFragment;
import com.bss.arrahmanlyrics.Fragments.TamilFragment;
import com.bss.arrahmanlyrics.adapter.ExpandableListAdapterMysql;
import com.bss.arrahmanlyrics.adapter.SongAdapter;
import com.bss.arrahmanlyrics.albumArts.albumArts;
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

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Task;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.hoang8f.android.segmented.SegmentedGroup;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener,MusicService.mainActivityCallback{
    private static final String TAG = "MainAcitivity";
    GoogleApiClient mGoogleSignInClient;
    DatabaseHandler dbHandler;
    private SessionManager session;
    private SQLiteSignInHandler db;
    private ProgressDialog pDialog;


    ProgressBar bar;

    List<song> songList;
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

    SlidingUpPanelLayout favoritePanel;
    SegmentedGroup segmentedGroup;
    EnglishFragment englishFragment;
    TamilFragment tamilFragment;
    private Handler mHandler = new Handler();

    Thread t;

    CustomViewPager viewPager;

    int totalSongs = 0;
    Point p;


    int imageReq = 0;
    int imageReqCom = 0;
    ArrayList<song> playlist = new ArrayList<>();

    final String image_path = "https://beyonitysoftwares.cf/arts/";
    public static final String Broadcast_PLAY_NEW_AUDIO = "com.bss.arrahmanlyrics.activites.PlayNewAudio";
    public static final String Broadcast_NEW_ALBUM = "com.bss.arrahmanlyrics.activites.PlayNewAlbum";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Fabric.with(this, new Crashlytics());


        db = new SQLiteSignInHandler(getApplicationContext());
        songList = new ArrayList<>();
        rv1 = (RecyclerView) findViewById(R.id.rv1);
        rv1.addItemDecoration(new DividerItemDecoration(getApplicationContext(), LinearLayoutManager.VERTICAL));
        rv1.setItemAnimator(new DefaultItemAnimator());
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        rv1.setLayoutManager(layoutManager);

        rv1.addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                song song = songAdapter.getItem(position);
                downloadLyrics(String.valueOf(song.getSong_id()));
               /* StorageUtil storageUtil = new StorageUtil(getApplicationContext());

                if (storageUtil.loadAudio() == null || totalSongs > storageUtil.loadAudio().size()) {
                    Log.d(TAG, "onItemClick: its null");
                    for (song songs : songList) {
                        song s = new song(songs.getSong_id(),songs.getSong_title(),songs.getAlbum_id(),songs.getAlbum_name(), songs.getDownload_link(),songs.getLyricist(),songs.getTrack_no());
                        playlist.add(s);
                    }
                    Log.d(TAG, "onItemClick: playlist = "+playlist.size());
                    int index = 0;
                    for (song s : playlist) {
                        if (s.getSong_title().equals(song.getSong_title()) && s.getAlbum_name().equals(song.getAlbum_name())) {
                            index = playlist.indexOf(s);
                        }
                    }
                    storageUtil.storeAudio(playlist);
                    storageUtil.storeAudioIndex(index);
                    Log.d(TAG, "onItemClick: storage = "+storageUtil.loadAudio().size());
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
*/

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
                    song s = new song(songs.getSong_id(),songs.getSong_title(),songs.getAlbum_id(),songs.getAlbum_name(), songs.getDownload_link(),songs.getLyricist(),songs.getTrack_no());
                    playlist.add(s);
                }
                downloadLyrics(String.valueOf(playlist.get(i1).getSong_id()));
                /*storageUtil.storeAudio(playlist);
                storageUtil.storeAudioIndex(i1);
                Intent setplaylist = new Intent(MainActivity.Broadcast_NEW_ALBUM);
                sendBroadcast(setplaylist);
                Intent broadcastIntent = new Intent(MainActivity.Broadcast_PLAY_NEW_AUDIO);
                sendBroadcast(broadcastIntent);
                closeDrawer();*/

                return false;
            }
        });
        albumList = new ArrayList<>();
        albumSongList = new HashMap<>();

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        // Session manager
        session = new SessionManager(getApplicationContext());
        if (!session.isLoggedIn()) {

            signIn();
        }else {
            HashMap<String,String> details = db.getUserDetails();
            Toast.makeText(this, "Welcome back "+details.get("displayName"), Toast.LENGTH_SHORT).show();
            //logoutUser();
        }

        dbHandler = new DatabaseHandler(getApplicationContext());
        int noOfSongs = dbHandler.getNoOfSongs();
        int noOfAlbums = dbHandler.getNoOfAlbums();
        Log.d(TAG, "onCreate: songs = "+noOfSongs+" albums = "+noOfAlbums);

        setUpLyricsPage();

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

        setNavigation();
        //Log.d(TAG, "onCreate: album name = "+dbHandler.getAlbumName(2));
        if(noOfAlbums<1){
            downloadAlbumDatabase();
        }
        if(noOfSongs>0&&noOfAlbums>0){
            pDialog.setMessage("Loading songs ...");
            showDialog();
            setUpImages();
            setUpSongsAlbums();
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
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    segmentedGroup.check(R.id.tamil);
                } else if (position == 1) {
                    segmentedGroup.check(R.id.english);
                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {}
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
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
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
        songList.clear();
        songList = dbHandler.getSongs();
        songAdapter = new SongAdapter(getApplicationContext(), songList);
        rv1.setAdapter(songAdapter);
        Log.d(TAG, "setUpSongs: "+songList.size());
        songAdapter.notifyDataSetChanged();

        albumList = dbHandler.getAlbums();

        for(albums a : albumList){
            int index = albumList.indexOf(a);
           List<song> oneAlbumSongs = dbHandler.getSongsByAlbumId(a.getAlbum_id());
           albumList.get(index).setList(oneAlbumSongs);
           albumSongList.put(a.getAlbum_name(),a.getSonglist());
        }

        Log.d(TAG, "setUpSongsAlbums: "+albumList.size());
        albumAdapter = new ExpandableListAdapterMysql(getApplicationContext(),albumList,albumSongList,MainActivity.this);
        rv2.setAdapter(albumAdapter);

        totalSongs = songList.size();
        hideDialog();
        Log.d(TAG, "setUpSongsAlbums: dialog hidden");
        Log.d(TAG, "setUpSongs: "+songList.size());
    }

    private void setNavigation() {

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            NavigationView navigationView2 = (NavigationView) findViewById(R.id.nav_view2);
            View view = navigationView.getHeaderView(0);
            View view2 = navigationView2.getHeaderView(0);
    }


    private void downloadSongDatabase(final String album_id) {

        String tag_string_req = "req_songs";

        pDialog.setMessage("Loading songs ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.GET_SONGS, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                //Log.d(TAG, "Albums Response: " + response.toString());


                //Log.d(TAG, "onResponse: "+response);
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                   JSONArray array = jObj.getJSONArray("songs");
                    for(int a = 0;a<array.length();a++){
                        JSONObject object = array.getJSONObject(a);
                        String song_id = object.getString("song_id");
                        String id = object.getString("album_id");
                        String song_title = object.getString("song_title");
                        String download_link = object.getString("download_link");
                        String lyricist = object.getString("lyricist");
                        String track_no = object.getString("track_no");
                        dbHandler.insertSongs(song_id,id,song_title,download_link,lyricist,track_no);

                    }
                    Log.d(TAG, "onResponse: "+jObj);
                    Log.d(TAG, "onResponse: "+(--request));
                    if(request == 0){
                        ArrayList<Integer> ids = dbHandler.getAlbumIds();
                        for(int a : ids) {
                            String imageLink = dbHandler.getImageLink(a);
                            new DownloadImageTask(a,MainActivity.this).execute(imageLink);
                        }

                        //setUpSongsAlbums();
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
                //signinTry();
                hideDialog();
                Log.d(TAG, "onErrorResponse: dialog hidden");
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("album_id", album_id);



                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
        Log.d(TAG, "downloadSongDatabase: "+(++request));
    }



    private void downloadLyrics(final String id) {

        String tag_string_req = "req_lyrics";



        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.GET_LYRICS, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Albums Response: " + response.toString());


                Log.d(TAG, "onResponse: "+response);
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                  /* JSONArray array = jObj.getJSONArray("songs");
                    for(int a = 0;a<array.length();a++){
                        JSONObject object = array.getJSONObject(a);
                        String song_id = object.getString("song_id");
                        String id = object.getString("album_id");
                        String song_title = object.getString("song_title");
                        String download_link = object.getString("download_link");
                        String lyricist = object.getString("lyricist");
                        String track_no = object.getString("track_no");
                        dbHandler.insertSongs(song_id,id,song_title,download_link,lyricist,track_no);

                    }
                    Log.d(TAG, "onResponse: "+jObj);
                    Log.d(TAG, "onResponse: "+(--request));
                    if(request == 0){
                        ArrayList<Integer> ids = dbHandler.getAlbumIds();
                        for(int a : ids) {
                            String imageLink = dbHandler.getImageLink(a);
                            new DownloadImageTask(a,MainActivity.this).execute(imageLink);
                        }

                        //setUpSongsAlbums();
                    }*/

                    Log.d(TAG, "onResponse: "+jObj);

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
                //signinTry();
                hideDialog();
                Log.d(TAG, "onErrorResponse: dialog hidden");
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                Log.d(TAG, "onResponse: song id = "+id);
                params.put("song_id", id);



                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
        Log.d(TAG, "downloadSongDatabase: "+(++request));
    }

    private void setUpImages() {


        for(int a: dbHandler.getAlbumIds()){
            albumArts.setBitmaps(a,dbHandler.getImageBlob(a));
        }
    }

    private void downloadAlbumDatabase() {

        String tag_string_req = "req_albums";

        pDialog.setMessage("Loading Albums ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.GET_ALBUMS, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                //Log.d(TAG, "Albums Response: " + response.toString());


               // Log.d(TAG, "onResponse: "+response);
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                   /* if (!error) {
                        // User successfully stored in MySQL
                        // Now store the user in sqlite
                        /*String id = jObj.getString("id");

                        JSONObject user = jObj.getJSONObject("user");
                        //int id = user.getInt("id");
                        String name = user.getString("displayName");
                        String email = user.getString("email");

                        // Inserting row in users table
                        db.addUser(Integer.parseInt(id) ,email,name);

                        Toast.makeText(getApplicationContext(), "User successfully registered. Try login now!", Toast.LENGTH_LONG).show();


                        checkLogin(email);
                        Log.d(TAG, "onResponse: "+jObj);
                    } else {

                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("error_msg");
                        Log.e(TAG, "onResponse: "+errorMsg);
                    }*/
                    JSONArray array = jObj.getJSONArray("albums");
                    for(int a = 0;a<array.length();a++){
                        JSONObject object = array.getJSONObject(a);
                        String album_id = object.getString("album_id");
                        String album_name = object.getString("album_name");
                        String hero = object.getString("hero");
                        String heroin = object.getString("heroin");
                        String language = object.getString("language");
                        String year = object.getString("year");
                        String image_link = image_path+album_id+".png";

                        dbHandler.insertAlbums(album_id,album_name,hero,heroin,language,year,image_link);
                    }
                    ArrayList<Integer> ids = dbHandler.getAlbumIds();
                    for(int a:ids){
                        downloadSongDatabase(String.valueOf(a));
                    }
                    //setUpSongsAlbums();


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
                //signinTry();
                hideDialog();
                Log.d(TAG, "onErrorResponse: dialog hidden");
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("artist", "1");



                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
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
        }
        else {
            signinTry();
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            //updateUI(account);
            Toast.makeText(this, "Successfully Signed in as "+account.getDisplayName(), Toast.LENGTH_SHORT).show();
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
                        db.addUser(Integer.parseInt(id) ,email,name);

                        Toast.makeText(getApplicationContext(), "User successfully registered. Try login now!", Toast.LENGTH_LONG).show();


                        checkLogin(email);
                    } else {

                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("error_msg");
                        if(errorMsg.contains("User already existed"))
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
                        db.addUser(Integer.parseInt(id),email,name);

                        // Launch main activity

                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
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
        Log.d(TAG, "checkLogin: "+strReq);
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

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };
    @Override
    protected void onStart() {
        Log.i(TAG, "onStart: on start called");
        super.onStart();
        if (!serviceBound) {
            Intent playerIntent = new Intent(MainActivity.this, MusicService.class);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            Log.i("bounded", "service bounded");


        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        Log.i("testing", "am in stop");
        if (serviceBound) {
            if (player != null) {
                player.setMainCallbacks(null);
            }
        }
        Log.i("testing", "finished");

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
            /*case R.id.fav_pop: {

                if (player != null) {
                    if (player.mediaPlayer != null) {
                        song s = player.getActiveSong();
                        if (checkFavoriteItem()) {
                            removeFavorite(s);

                        } else {
                            addFavorite(s);

                        }
                    }
                }


                break;

            }*/

        }
    }

    @Override
    public void update() {
        if (player != null && player.mediaPlayer != null) {
            if (player.isPlaying()) {
                if (pDialog.isShowing()) {
                    pDialog.hide();
                }

                seekBar.setMax(player.getDuration());
                totalTime.setText(Helper.durationCalculator(player.getDuration()));
                playpause.setImageResource(R.drawable.pause);
                //setLyrics(player.getActiveSong());

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
    public void showDialog(String name, String movie) {
        if (pDialog!= null) {
            Log.i(TAG, "showDialog: loading ");

            pDialog.setMessage("Loading " + Helper.FirstLetterCaps(name) + "\nFrom " + Helper.FirstLetterCaps(movie));
            pDialog.show();

        }
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

   /* private void setLyrics(song activeSong) {
        String movieTitle = activeSong.getAlbum_name();
        String songTitle = activeSong.getSong_title();
        songname.setText(Helper.FirstLetterCaps(songTitle));
        moviename.setText(Helper.FirstLetterCaps(movieTitle));

        //HashMap<String, Object> songs = (HashMap<String, Object>) values.get(movieTitle);
        //HashMap<String, Object> songlyrics = (HashMap<String, Object>) songs.get(songTitle);
        String english1 = database.getLyricsOne(movieTitle,songTitle);
        String english2 = database.getLyricsTwo(movieTitle,songTitle);
        englishFragment.setLyrics(english1, english2);

        String tamil1 = database.getLyricsThree(movieTitle,songTitle);
        String tamil2 = database.getLyricsFour(movieTitle,songTitle);
        tamilFragment.setLyrics(tamil1, tamil2);


    }*/
   private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
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
           result.compress(Bitmap.CompressFormat.PNG,100,stream);
           byte[] imagebyte = stream.toByteArray();
           dbHandler.insertImage(String.valueOf(id),imagebyte);
           checkTaskComplete();
          
       }
   }
   
   public void checkTaskComplete(){
       ++imageReqCom;
       if(imageReqCom==dbHandler.getNoOfAlbums()){
           setUpImages();
           setUpSongsAlbums();
       }
   }
}
