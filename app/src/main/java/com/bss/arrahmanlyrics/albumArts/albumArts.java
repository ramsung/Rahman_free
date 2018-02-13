package com.bss.arrahmanlyrics.albumArts;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.HashMap;

/**
 * Created by mohan on 11/2/18.
 */

public class albumArts {

    static HashMap<Integer, Bitmap> arts = new HashMap<>();
    public static void setBitmaps(int id, byte[]array){
        Bitmap bitmap = BitmapFactory.decodeByteArray(array,0,array.length);
        arts.put(id,bitmap);
    }

    public static Bitmap getBitmap(int id){
        return arts.get(id);
    }

    public static int getSize(){return arts.size();}
}
