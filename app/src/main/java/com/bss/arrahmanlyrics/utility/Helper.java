package com.bss.arrahmanlyrics.utility;

import android.util.Log;

/**
 * Created by mohan on 6/17/17.
 */

public class Helper {

	public static String FirstLetterCaps(String source) {
		source = source.toLowerCase();
		StringBuffer res = new StringBuffer();

		String[] strArr = source.split(" ");
		Log.e("string array", source);

		for (String str : strArr) {
			char[] stringArray = str.trim().toCharArray();

				if (stringArray.length > 0) {
					if((int)stringArray[0] != 32) {
						stringArray[0] = Character.toUpperCase(stringArray[0]);
						str = new String(stringArray);
					}
				}



			res.append(str).append(" ");
		}

		return res.toString().trim();
	}
	public static String durationCalculator(long id) {
		String finalTimerString = "";
		String secondsString = "";
		String mp3Minutes = "";
		// Convert total duration into time

		int minutes = (int) (id % (1000 * 60 * 60)) / (1000 * 60);
		int seconds = (int) ((id % (1000 * 60 * 60)) % (1000 * 60) / 1000);

		// Prepending 0 to seconds if it is one digit
		if (seconds < 10) {
			secondsString = "0" + seconds;
		} else {
			secondsString = "" + seconds;
		}
		if (minutes < 10) {
			mp3Minutes = "0" + minutes;
		} else {
			mp3Minutes = "" + minutes;
		}
		finalTimerString = finalTimerString + mp3Minutes + ":" + secondsString;
		// return timer string
		return finalTimerString;
	}
}
