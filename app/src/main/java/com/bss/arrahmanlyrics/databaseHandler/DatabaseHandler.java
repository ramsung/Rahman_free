package com.bss.arrahmanlyrics.databaseHandler;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mohan on 4/2/18.
 */

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "beyonity_albums";
    private static final String TABLE_albums = "albums";
    private static final String TABLE_language = "language";
    private static final String TABLE_songs= "songs";

    //album
    private static final String KEY_ALBUM_ID = "album_id";
    private static final String KEY_ALBUM_NAME = "album_name";
    private static final String KEY_ARTIST = "artist";
    private static final String KEY_HERO = "hero";
    private static final String KEY_HEROIN = "heroin";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_YEAR = "year";
    private static final String KEY_IMAGE_LINK = "image_link";

    //language
    private static final String KEY_LANGUAGE_ID = "id";
    private static final String KEY_LANGUAGE_NAME = "language_name";

    //songs
    private static final String KEY_SONG_ID = "song_id";
    private static final String KEY_SONG_TITLE = "song_title";
    private static final String KEY_DOWNLOAD_LINK = "DOWNLOAD_LINK";
    private static final String KEY_LYRICIST = "lyricist";
    private static final String KEY_TRACK_NO = "track_no";

    private static final String TAG = DatabaseHandler.class.getSimpleName();


    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_ALBUMS_TABLE = "CREATE TABLE IF NOT EXISTS "+TABLE_albums+" ("+KEY_ALBUM_ID+ " INTEGER UNIQUE, "+KEY_ALBUM_NAME+" varchar(255),"
                + KEY_HERO+" varchar(255),"+KEY_HEROIN+" varchar(255),"+KEY_LANGUAGE+" INTEGER,"+KEY_YEAR+" INTEGER,"+KEY_IMAGE_LINK+" varchar(255))";

        String CREATE_LANGUAGE_TABLE = "CREATE TABLE IF NOT EXISTS "+TABLE_language+" ("+KEY_LANGUAGE_ID+ " INTEGER UNIQUE, "+KEY_LANGUAGE_NAME+" varchar(255))";


        String CREATE_SONGS_TABLE = "CREATE TABLE IF NOT EXISTS "+TABLE_songs+" ("+KEY_SONG_ID+ " INTEGER UNIQUE, "+KEY_ALBUM_ID+" INTEGER,"
                + KEY_SONG_TITLE+" varchar(255),"+KEY_DOWNLOAD_LINK+" text,"+KEY_LYRICIST+" varchar(255),"+KEY_TRACK_NO+" INTEGER)";

        db.execSQL(CREATE_ALBUMS_TABLE);
        db.execSQL(CREATE_LANGUAGE_TABLE);
        db.execSQL(CREATE_SONGS_TABLE);




    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


        public int getNoOfSongs() {

            String selectQuery = "SELECT  * FROM " + TABLE_songs;

            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);
            // Move to first row
            //cursor.moveToFirst();
            int count = cursor.getCount();
            cursor.close();
            db.close();
            // return user
            Log.d(TAG, "Fetching user from Sqlite: " + count);

            return count;

    }

    public int getNoOfAlbums() {

        String selectQuery = "SELECT  * FROM " + TABLE_albums;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        //cursor.moveToFirst();
        int count = cursor.getCount();
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching user from Sqlite: " + count);

        return count;

    }

    public boolean insertAlbums(String album_id,String album_name, String hero, String heroin, String language, String year, String image_link){

        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_ALBUM_ID,album_id);
        contentValues.put(KEY_ALBUM_NAME,album_name);
        contentValues.put(KEY_HERO,hero);
        contentValues.put(KEY_HEROIN,heroin);
        contentValues.put(KEY_LANGUAGE,language);
        contentValues.put(KEY_YEAR,year);
        contentValues.put(KEY_IMAGE_LINK,image_link);
        db.insert(TABLE_albums,null,contentValues);
        return true;



    }

    public ArrayList<Integer> getAlbumIds(){
        ArrayList<Integer> ids = new ArrayList<>();
        String st = "SELECT album_id FROM albums";
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(st,null);
        while (c.moveToNext()){
            ids.add(c.getInt(c.getColumnIndex("album_id")));
        }

        return ids;
    }

    public boolean insertSongs(String song_id, String id, String song_title, String download_link, String lyricist, String track_no) {

        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_SONG_ID,song_id);
        contentValues.put(KEY_ALBUM_ID,id);
        contentValues.put(KEY_SONG_TITLE,song_title);
        contentValues.put(KEY_DOWNLOAD_LINK,download_link);
        contentValues.put(KEY_LYRICIST,lyricist);
        contentValues.put(KEY_TRACK_NO,track_no);
        db.insert(TABLE_songs,null,contentValues);
        return true;


    }
}
