package com.bss.arrahmanlyrics.databaseHandler;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.bss.arrahmanlyrics.custom_pages.CustomViewPager;
import com.bss.arrahmanlyrics.model.albums;
import com.bss.arrahmanlyrics.model.song;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by mohan on 4/2/18.
 */

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "beyonity_albums";
    private static final String TABLE_albums = "albums";
    private static final String TABLE_language = "language";
    private static final String TABLE_songs = "songs";
    private static final String TABLE_IMAGES = "images";
    private static final String TABLE_FAVORITE = "favorite";

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


    //images
    private static final String KEY_IMAGE_BLOB = "image";
    private static final String TAG = DatabaseHandler.class.getSimpleName();

    //favorites
    private static final String KEY_USER_ID = "user_id";


    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_ALBUMS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_albums + " (" + KEY_ALBUM_ID + " INTEGER UNIQUE, " + KEY_ALBUM_NAME + " varchar(255),"
                + KEY_HERO + " varchar(255)," + KEY_HEROIN + " varchar(255)," + KEY_LANGUAGE + " INTEGER," + KEY_YEAR + " INTEGER," + KEY_IMAGE_LINK + " varchar(255))";

        String CREATE_LANGUAGE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_language + " (" + KEY_LANGUAGE_ID + " INTEGER UNIQUE, " + KEY_LANGUAGE_NAME + " varchar(255))";


        String CREATE_SONGS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_songs + " (" + KEY_SONG_ID + " INTEGER UNIQUE, " + KEY_ALBUM_ID + " INTEGER,"
                + KEY_SONG_TITLE + " varchar(255)," + KEY_DOWNLOAD_LINK + " text," + KEY_LYRICIST + " varchar(255)," + KEY_TRACK_NO + " INTEGER)";

        String CREATE_IMAGE_TABLE = "CREATE TABLE IF NOT EXISTS "+TABLE_IMAGES +" ("+ KEY_ALBUM_ID + " INTEGER UNIQUE, "+ KEY_IMAGE_BLOB +" BLOB)";

        String CREATE_FAVORITE_TABLE = "CREATE TABLE IF NOT EXISTS "+TABLE_FAVORITE +" ("+KEY_USER_ID+ " INTEGER, "+KEY_SONG_ID + " INTEGER)";

        db.execSQL(CREATE_ALBUMS_TABLE);
        db.execSQL(CREATE_LANGUAGE_TABLE);
        db.execSQL(CREATE_SONGS_TABLE);
        db.execSQL(CREATE_IMAGE_TABLE);
        db.execSQL(CREATE_FAVORITE_TABLE);


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

    public boolean insertImage(String album_id,byte[] bytearray){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_ALBUM_ID,album_id);
        contentValues.put(KEY_IMAGE_BLOB,bytearray);
        db.insert(TABLE_IMAGES,null,contentValues);

        return true;
    }

    public byte[] getImageBlob(int album_id){
        byte[] array = null;
        SQLiteDatabase db = getReadableDatabase();
        String st = "SELECT image FROM "+TABLE_IMAGES+" WHERE album_id = '"+album_id+"'";
        Cursor c = db.rawQuery(st,null);
        if(c != null){
            while (c.moveToNext()){
                array =  c.getBlob(c.getColumnIndex(KEY_IMAGE_BLOB));
            }
        }
        if(c != null){
            c.close();
        }

        return array;

    }

    public int getNumberOfImages(){
        SQLiteDatabase db = getReadableDatabase();
        String st = "SELECT * FROM "+TABLE_IMAGES;
        Cursor c = db.rawQuery(st,null);
        int count = c.getCount();
        c.close();
        db.close();
        return count;


    }

    public boolean insertAlbums(String album_id, String album_name, String hero, String heroin, String language, String year, String image_link) {

        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_ALBUM_ID, album_id);
        contentValues.put(KEY_ALBUM_NAME, album_name);
        contentValues.put(KEY_HERO, hero);
        contentValues.put(KEY_HEROIN, heroin);
        contentValues.put(KEY_LANGUAGE, language);
        contentValues.put(KEY_YEAR, year);
        contentValues.put(KEY_IMAGE_LINK, image_link);
        db.insert(TABLE_albums, null, contentValues);
        return true;


    }

    public ArrayList<Integer> getAlbumIds() {
        ArrayList<Integer> ids = new ArrayList<>();
        String st = "SELECT album_id FROM albums";
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(st, null);
        while (c.moveToNext()) {
            ids.add(c.getInt(c.getColumnIndex("album_id")));
        }
        if(c != null){
            c.close();
        }
        return ids;
    }

    public boolean insertSongs(String song_id, String id, String song_title, String download_link, String lyricist, String track_no) {

        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_SONG_ID, song_id);
        contentValues.put(KEY_ALBUM_ID, id);
        contentValues.put(KEY_SONG_TITLE, song_title);
        contentValues.put(KEY_DOWNLOAD_LINK, download_link);
        contentValues.put(KEY_LYRICIST, lyricist);
        contentValues.put(KEY_TRACK_NO, track_no);

        db.insert(TABLE_songs, null, contentValues);
        return true;


    }

    public String getAlbumName(int album_id) {
        String albumName = "";
        SQLiteDatabase db = getReadableDatabase();
        String st = "SELECT album_name FROM " + TABLE_albums + " WHERE album_id = '" + album_id + "'";
        Cursor c = db.rawQuery(st, null);
        if (c != null) {
            while (c.moveToNext()) {
                albumName = c.getString(c.getColumnIndex(KEY_ALBUM_NAME));
            }
        }
        if(c!=null){
            c.close();
        }

        return albumName;
    }

    public List<song> getSongs() {
        List<song> songList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_songs +" ORDER BY song_title ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        //cursor.moveToFirst();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int song_id = cursor.getInt(cursor.getColumnIndex(KEY_SONG_ID));
                int album_id = cursor.getInt(cursor.getColumnIndex(KEY_ALBUM_ID));
                String album_name = getAlbumName(album_id);
                String song_title = cursor.getString(cursor.getColumnIndex(KEY_SONG_TITLE));
                String download_link = cursor.getString(cursor.getColumnIndex(KEY_DOWNLOAD_LINK));
                String lyricist = cursor.getString(cursor.getColumnIndex(KEY_LYRICIST));
                String track_no = cursor.getString(cursor.getColumnIndex(KEY_TRACK_NO));

                song s = new song(song_id, song_title, album_id, album_name, download_link, lyricist, track_no);
                songList.add(s);
            }
        }
        // return user
        if(cursor != null){
            cursor.close();
        }
        return songList;
    }

    public int getYearByAlbumId(int album_id){
        int year = 0;
        SQLiteDatabase db = getReadableDatabase();
        String selectQuery = "SELECT "+KEY_YEAR+" FROM " + TABLE_albums +" WHERE album_id  = '"+album_id+"'";
        Cursor c = db.rawQuery(selectQuery,null);
        if(c != null){
            while (c.moveToNext()){
                year = c.getInt(c.getColumnIndex(KEY_YEAR));
            }
        }

        c.close();
        return year;

    }
    public List<albums> getAlbums(){
        List<albums> Albums = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_albums +" ORDER BY album_name ASC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery,null);
        if(c != null){
            while (c.moveToNext()){
                int album_id = c.getInt(c.getColumnIndex(KEY_ALBUM_ID));
                String album_name = c.getString(c.getColumnIndex(KEY_ALBUM_NAME));

                String hero = c.getString(c.getColumnIndex(KEY_HERO));
                String heroin = c.getString(c.getColumnIndex(KEY_HEROIN));

                int year = c.getInt(c.getColumnIndex(KEY_YEAR));
                String image_link = c.getString(c.getColumnIndex(KEY_IMAGE_LINK));
                albums a = new albums(album_id,album_name,hero,heroin,year,image_link);
                Albums.add(a);
            }
        }
        if(c != null){
            c.close();
        }
        return Albums;
    }

    public String getImageLink(int album_id){
        String image_link = "";
        List<albums> Albums = new ArrayList<>();
        String selectQuery = "SELECT  image_link FROM " + TABLE_albums +" WHERE album_id = '"+album_id+"'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery,null);
        if(c != null){
            while (c.moveToNext()){
               image_link = c.getString(c.getColumnIndex(KEY_IMAGE_LINK));
            }
        }
        if(c != null){
            c.close();
        }
        return image_link;
    }
    public List<song> getSongsByAlbumId(int album_id) {
        List<song> songList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_songs +" WHERE album_id = '"+album_id+"' ORDER BY track_no ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        //cursor.moveToFirst();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int song_id = cursor.getInt(cursor.getColumnIndex(KEY_SONG_ID));
                int id = cursor.getInt(cursor.getColumnIndex(KEY_ALBUM_ID));
                String album_name = getAlbumName(album_id);
                String song_title = cursor.getString(cursor.getColumnIndex(KEY_SONG_TITLE));
                String download_link = cursor.getString(cursor.getColumnIndex(KEY_DOWNLOAD_LINK));
                String lyricist = cursor.getString(cursor.getColumnIndex(KEY_LYRICIST));
                String track_no = cursor.getString(cursor.getColumnIndex(KEY_TRACK_NO));

                song s = new song(song_id, song_title, id, album_name, download_link, lyricist, track_no);
                songList.add(s);
            }
        }
        // return user

        if(cursor != null){
            cursor.close();
        }
        return songList;
    }


    public boolean insertFavorites(int user_id, int song_id) {

        if(!isFavExists(user_id,song_id)){
            SQLiteDatabase db = getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(KEY_USER_ID, user_id);
            contentValues.put(KEY_SONG_ID, song_id);

            db.insert(TABLE_FAVORITE, null, contentValues);
            Log.d(TAG, "insertFavorites: successfully added fav");
            return true;
        }

        return false;


    }
    public boolean deleteFavorites(int user_id, int song_id) {

        SQLiteDatabase db = getWritableDatabase();
        String st = "DELETE FROM "+TABLE_FAVORITE+" WHERE user_id = '"+user_id+"' AND song_id = '"+song_id+"'";
        db.execSQL(st);
        return true;


    }
    public song getSongById(int song_id){
        song s=null;
        String selectQuery = "SELECT  * FROM " + TABLE_songs +" WHERE song_id = '"+song_id+"'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        //cursor.moveToFirst();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int sid = cursor.getInt(cursor.getColumnIndex(KEY_SONG_ID));
                int id = cursor.getInt(cursor.getColumnIndex(KEY_ALBUM_ID));
                String album_name = getAlbumName(id);
                String song_title = cursor.getString(cursor.getColumnIndex(KEY_SONG_TITLE));
                String download_link = cursor.getString(cursor.getColumnIndex(KEY_DOWNLOAD_LINK));
                String lyricist = cursor.getString(cursor.getColumnIndex(KEY_LYRICIST));
                String track_no = cursor.getString(cursor.getColumnIndex(KEY_TRACK_NO));

                song sa = new song(song_id, song_title, id, album_name, download_link, lyricist, track_no);
                s= sa;

            }
        }
        // return user

        if(cursor != null){
            cursor.close();
        }
        return s;
    }
    public song getSongBySongTitle(String song_Title){
        song s=null;
        String selectQuery = "SELECT  * FROM " + TABLE_songs +" WHERE song_title = '"+song_Title+"'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        //cursor.moveToFirst();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int sid = cursor.getInt(cursor.getColumnIndex(KEY_SONG_ID));
                int id = cursor.getInt(cursor.getColumnIndex(KEY_ALBUM_ID));
                String album_name = getAlbumName(id);
                String song_title = cursor.getString(cursor.getColumnIndex(KEY_SONG_TITLE));
                String download_link = cursor.getString(cursor.getColumnIndex(KEY_DOWNLOAD_LINK));
                String lyricist = cursor.getString(cursor.getColumnIndex(KEY_LYRICIST));
                String track_no = cursor.getString(cursor.getColumnIndex(KEY_TRACK_NO));

                song sa = new song(sid, song_title, id, album_name, download_link, lyricist, track_no);
                s= sa;

            }
        }
        // return user

        if(cursor != null){
            cursor.close();
        }
        return s;
    }
    public List<song> getFavorites(int user_id){
        List<song> songList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_FAVORITE +" WHERE user_id = '"+user_id+"'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        //cursor.moveToFirst();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int song_id = cursor.getInt(cursor.getColumnIndex(KEY_SONG_ID));
                song s = getSongById(song_id);
                songList.add(s);
            }
        }
        // return user

        if(cursor != null){
            cursor.close();
        }
        return songList;
    }

    public boolean isFavExists(int user_id,int song_id){
        List<song> songList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_FAVORITE +" WHERE user_id = '"+user_id+"' AND song_id = '"+song_id+"'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        //cursor.moveToFirst();
        int count = cursor.getCount();

        if(cursor != null){
            cursor.close();
        }
        if(count == 0){
            return false;
        }else {
            return true;
        }

        // return user


    }

    public List<song> getSongsByYear(int year){
        Log.d(TAG, "getSongsByYear: "+year);
        SQLiteDatabase db = getReadableDatabase();
        List<song> list = new ArrayList<>();
        String st = "SELECT "+KEY_ALBUM_ID+" FROM "+TABLE_albums+" WHERE "+KEY_YEAR+" LIKE '"+year+"%'";
        Cursor c = db.rawQuery(st,null);

        Log.d(TAG, "getSongsByYear: count = "+c.getCount()+" "+st);
        if(c!=null){
            while (c.moveToNext()){
                int album_id = c.getInt(c.getColumnIndex(KEY_ALBUM_ID));
                List<song> s = getSongsByAlbumId(album_id);
                for(song a : s){
                    list.add(a);
                }
            }
        }

        c.close();
        return list;





    }

    public List<song> getSongsBySearch(String query){
        SQLiteDatabase db = getReadableDatabase();
        List<song> list = new ArrayList<>();
        String st = "SELECT * FROM "+TABLE_songs+" WHERE "+KEY_SONG_TITLE+" LIKE '"+query+"%' OR "+KEY_SONG_TITLE+" LIKE '%"+query+"' OR "+KEY_LYRICIST+" LIKE '"+query+"%' OR "+KEY_LYRICIST+" LIKE '%"+query+"'";
        Cursor cursor = db.rawQuery(st,null);

        Log.d(TAG, "getSongsByYear: count = "+cursor.getCount()+" "+st);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int song_id = cursor.getInt(cursor.getColumnIndex(KEY_SONG_ID));
                int id = cursor.getInt(cursor.getColumnIndex(KEY_ALBUM_ID));
                String album_name = getAlbumName(id);
                String title = cursor.getString(cursor.getColumnIndex(KEY_SONG_TITLE));
                String download_link = cursor.getString(cursor.getColumnIndex(KEY_DOWNLOAD_LINK));
                String ly = cursor.getString(cursor.getColumnIndex(KEY_LYRICIST));
                String track_no = cursor.getString(cursor.getColumnIndex(KEY_TRACK_NO));

                song s = new song(song_id, title, id, album_name, download_link, ly, track_no);
                list.add(s);
            }
        }
        // return user

        if(cursor != null){
            cursor.close();
        }
        return list;


    }
    public List<albums> getAlbumsByYear(int year){
        List<albums> Albums = new ArrayList<>();
        String st = "SELECT * FROM "+TABLE_albums+" WHERE "+KEY_YEAR+" LIKE '"+year+"%'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(st,null);
        if(c != null){
            while (c.moveToNext()){
                int album_id = c.getInt(c.getColumnIndex(KEY_ALBUM_ID));
                String album_name = c.getString(c.getColumnIndex(KEY_ALBUM_NAME));

                String hero = c.getString(c.getColumnIndex(KEY_HERO));
                String heroin = c.getString(c.getColumnIndex(KEY_HEROIN));

                int y = c.getInt(c.getColumnIndex(KEY_YEAR));
                String image_link = c.getString(c.getColumnIndex(KEY_IMAGE_LINK));
                albums a = new albums(album_id,album_name,hero,heroin,y,image_link);
                Albums.add(a);
            }
        }
        if(c != null){
            c.close();
        }
        return Albums;
    }

    public List<albums> getAlbumsByName(String query){
        List<albums> Albums = new ArrayList<>();
        String st = "SELECT * FROM "+TABLE_albums+" WHERE "+KEY_ALBUM_NAME+" LIKE '"+query+"%' OR "+KEY_ALBUM_NAME+" LIKE '%"+query+"'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(st,null);
        if(c != null){
            while (c.moveToNext()){
                int album_id = c.getInt(c.getColumnIndex(KEY_ALBUM_ID));
                String album_name = c.getString(c.getColumnIndex(KEY_ALBUM_NAME));

                String hero = c.getString(c.getColumnIndex(KEY_HERO));
                String heroin = c.getString(c.getColumnIndex(KEY_HEROIN));

                int y = c.getInt(c.getColumnIndex(KEY_YEAR));
                String image_link = c.getString(c.getColumnIndex(KEY_IMAGE_LINK));
                albums a = new albums(album_id,album_name,hero,heroin,y,image_link);
                Albums.add(a);
            }
        }
        if(c != null){
            c.close();
        }
        return Albums;
    }


}
