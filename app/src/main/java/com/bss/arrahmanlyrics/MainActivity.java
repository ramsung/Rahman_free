package com.bss.arrahmanlyrics;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bss.arrahmanlyrics.appconfig.AppConfig;
import com.bss.arrahmanlyrics.appconfig.AppController;
import com.bss.arrahmanlyrics.databaseHandler.DatabaseHandler;
import com.bss.arrahmanlyrics.databaseHandler.SQLiteSignInHandler;
import com.bss.arrahmanlyrics.databaseHandler.SessionManager;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{
    private static final String TAG = "MainAcitivity";
    GoogleApiClient mGoogleSignInClient;
    DatabaseHandler dbHandler;
    private SessionManager session;
    private SQLiteSignInHandler db;
    private ProgressDialog pDialog;

    private RecyclerView rv1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Fabric.with(this, new Crashlytics());
        db = new SQLiteSignInHandler(getApplicationContext());

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
        if(noOfAlbums<1){
            downloadAlbumDatabase();
        }
        if(noOfSongs<1&&noOfAlbums>0){
            ArrayList<Integer> ids = dbHandler.getAlbumIds();
           for(int a:ids){
               downloadSongDatabase(String.valueOf(a));
           }
        }

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
                hideDialog();

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
                hideDialog();

                Log.d(TAG, "onResponse: "+response);
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
                        String image_link = object.getString("image_link");

                        dbHandler.insertAlbums(album_id,album_name,hero,heroin,language,year,image_link);
                    }
                    Log.d(TAG, "onResponse: "+array.get(2));
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

        pDialog.setMessage("Logging in ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_REGISTER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response.toString());
                hideDialog();

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

        pDialog.setMessage("Logging in ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_LOGIN, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response.toString());
                hideDialog();

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
}
