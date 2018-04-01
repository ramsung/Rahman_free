package com.bss.arrahmanlyrics.music;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.bss.arrahmanlyrics.BuildConfig;
import com.bss.arrahmanlyrics.MainActivity;
import com.bss.arrahmanlyrics.R;
import com.bss.arrahmanlyrics.albumArts.albumArts;
import com.bss.arrahmanlyrics.appconfig.AppController;
import com.bss.arrahmanlyrics.model.song;
import com.bss.arrahmanlyrics.utility.Helper;
import com.bss.arrahmanlyrics.utility.StorageUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.danikula.videocache.CacheListener;
import com.danikula.videocache.HttpProxyCacheServer;
import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import static android.content.ContentValues.TAG;

/**
 * Created by Belal on 12/30/2016.
 */

public class MusicService extends Service implements MediaPlayer.OnCompletionListener,
		MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
		MediaPlayer.OnBufferingUpdateListener,CacheListener, AudioManager.OnAudioFocusChangeListener {
	//creating a mediaplayer object
	public MediaPlayer mediaPlayer;
	private final IBinder iBinder = new LocalBinder();
	ArrayList<song> playlist = new ArrayList<>();
	song activeSong;
	int audioIndex;
	boolean shuffleOn = true;
	int resumePosition = 0;
	public mainActivityCallback maincallback;
	private MediaSessionManager mediaSessionManager;
	private MediaSessionCompat mediaSession;
	private MediaControllerCompat.TransportControls transportControls;
	private boolean isPaused = false;
	boolean lostFocusLoss = false;
	public void seekTo(int i) {
		mediaPlayer.seekTo(i);
	}

	private static final int NOTIFICATION_ID = 101;
	NotificationCompat.Builder notificationBuilder;
	Equalizer equalizer;
	// The volume we set the media player to when we lose audio focus, but are
	// allowed to reduce the volume instead of stopping playback.
	public static final float VOLUME_DUCK = 0.2f;
	// The volume we set the media player when we have audio focus.
	public static final float VOLUME_NORMAL = 1.0f;

	// we don't have audio focus, and can't duck (play at a low volume)
	private static final int AUDIO_NO_FOCUS_NO_DUCK = 0;
	// we don't have focus, but can duck (play at a low volume)
	private static final int AUDIO_NO_FOCUS_CAN_DUCK = 1;
	// we have full audio focus
	private static final int AUDIO_FOCUSED = 2;

	private boolean mPlayOnFocusGain;

	private boolean mAudioNoisyReceiverRegistered;
	final String image_path = "https://beyonitysoftwares.cf/arts/";

	private int mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK;
	private AudioManager audioManager;

	boolean ongoingcall = false;
	private static boolean ignoreAudioFocus = false;
	private PhoneStateListener phoneStateListener;

	//final RemoteViews notificationLayout = new RemoteViews("com.bss.arrahmanlyrics", R.layout.notification);
	//final RemoteViews notificationLayoutBig = new RemoteViews("com.bss.arrahmanlyrics", R.layout.notification_big);
	public static final String ACTION_PLAY = "com.bss.arrahmanlyrics.ACTION_PLAY";
	public static final String ACTION_PAUSE = "com.bss.arrahmanlyrics.ACTION_PAUSE";
	public static final String ACTION_PREVIOUS = "com.bss.arrahmanlyrics.ACTION_PREVIOUS";
	public static final String ACTION_NEXT = "com.bss.arrahmanlyrics.ACTION_NEXT";
	public static final String ACTION_STOP = "com.bss.arrahmanlyrics.ACTION_STOP";


	@Override
	public void onCacheAvailable(File cacheFile, String url, int percentsAvailable) {

	}

	public interface mainActivityCallback {
		void update();

		void showDialog(song s);


	}

	@Override
	public void onCreate() {
		Log.i(TAG, "onCreate: ");
		audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);


		try {
			phoneStateListener = new PhoneStateListener() {
				@Override
				public void onCallStateChanged(int state, String incomingNumber) {
					if (state == TelephonyManager.CALL_STATE_RINGING) {
						if (mediaPlayer != null) {
							if (mediaPlayer.isPlaying()) {
								pauseMedia();
								ongoingcall = true;

							}
						}

					} else if (state == TelephonyManager.CALL_STATE_IDLE) {
						if (mediaPlayer != null) {
							if (ongoingcall) {
								ongoingcall = false;
								resumeMedia();
							}
						}

					} else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {

					}
					super.onCallStateChanged(state, incomingNumber);
				}
			};
			TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
			if (mgr != null) {
				mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
			}
		} catch (Exception e) {
			Log.e("tmessages", e.toString());
		}
		super.onCreate();

		register_setNewalbum();
		register_playNewAudio();
	}
	private String setProxyUrl(String url) throws IOException {
		HttpProxyCacheServer proxy = AppController.getProxy(getApplicationContext());
		proxy.registerCacheListener(this, url);
		String proxyUrl = proxy.getProxyUrl(url);
		Log.d("proxy", "Use proxy url " + proxyUrl + " instead of original url " + url);
		return proxyUrl;

	}
	private AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
		@Override
		public void onAudioFocusChange(int i) {
			switch (i){
				case AudioManager.AUDIOFOCUS_LOSS:{
					if(mediaPlayer != null){
						if(mediaPlayer.isPlaying()){
							pauseMedia();
							if(maincallback != null){
								maincallback.update();
							}


						}
					}

					break;
				}

				case AudioManager.AUDIOFOCUS_GAIN:{
					requestAudioFocus();
					if(mediaPlayer != null){
						if(lostFocusLoss){
							resumeMedia();
							lostFocusLoss = false;
							if(maincallback != null){
								maincallback.update();
							}
						}
					}
					break;

				}

				case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:{
					if(mediaPlayer != null){
						if(mediaPlayer.isPlaying()){
							pauseMedia();
							lostFocusLoss = true;
							if(maincallback != null){
								maincallback.update();
							}

						}
					}
					break;
				}
				case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:{
					requestAudioFocus();
					if(mediaPlayer != null){
						if(lostFocusLoss){
							resumeMedia();
							lostFocusLoss = false;
							if(maincallback != null){
								maincallback.update();
							}
						}
					}
					break;

				}

			}
		}
	};

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return iBinder;
	}

	public void setMainCallbacks(mainActivityCallback callbacks) {
		this.maincallback = callbacks;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "onStartCommand: ");
		//getting systems default ringtone
		if (requestAudioFocus() == false) {
			//Could not gain focus
			stopSelf();
		}

		if (mediaSessionManager == null) {
			try {
				initMediaSession();
				//initMediaPlayer();
			} catch (RemoteException e) {
				e.printStackTrace();
				stopSelf();
			}
			//buildNotification(PlaybackStatus.PAUSED);
		}
		handleIncomingActions(intent);
		//we have some options for service
		//start sticky means service will be explicity started and stopped
		return START_STICKY;
	}

	private void initMediaSession() throws RemoteException {
		if (mediaSessionManager != null) return; //mediaSessionManager exists

		mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
		// Create a new MediaSession
		mediaSession = new MediaSessionCompat(getApplicationContext(), "AudioPlayer");
		//Get MediaSessions transport controls
		transportControls = mediaSession.getController().getTransportControls();
		//set MediaSession -> ready to receive media commands
		mediaSession.setActive(true);
		//indicate that the MediaSession handles transport control commands
		// through its MediaSessionCompat.Callback.
		mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

		//Set mediaSession's MetaData
		//updateMetaData();

		// Attach Callback to receive MediaSession updates
		mediaSession.setCallback(new MediaSessionCompat.Callback() {
			// Implement callbacks
			@Override
			public void onPlay() {
				super.onPlay();
				if (mediaPlayer == null) return;
				resumeMedia();
				buildNotification(PlaybackStatus.PLAYING);

				if (maincallback != null) {
					maincallback.update();
				}

			}

			@Override
			public void onPause() {
				super.onPause();
				if (mediaPlayer == null) return;
				pauseMedia();
				buildNotification(PlaybackStatus.PAUSED);


				if (maincallback != null) {
					maincallback.update();
				}

			}

			@Override
			public void onSkipToNext() {
				super.onSkipToNext();
				if (mediaPlayer == null) return;
				skipToNext();


			}

			@Override
			public void onSkipToPrevious() {
				super.onSkipToPrevious();
				if (mediaPlayer == null) return;
				skipToPrevious();

			}

			@Override
			public void onStop() {
				super.onStop();
				if (mediaPlayer == null) return;
				removeNotification();
				//Stop the service
				stopSelf();

				if (maincallback != null) {
					maincallback.update();
				}

			}

			@Override
			public void onSeekTo(long position) {
				super.onSeekTo(position);
			}
		});
	}

	private void updateMetaData() {
		Bitmap albumArt = BitmapFactory.decodeResource(getResources(),
				R.drawable.ic_launcher); //replace with medias albumArt
		// Update the current metadata
		mediaSession.setMetadata(new MediaMetadataCompat.Builder()
				.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
				.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, activeSong.getAlbum_name())
				.putString(MediaMetadataCompat.METADATA_KEY_TITLE, activeSong.getSong_title())
				.build());
	}


	private void buildNotification(PlaybackStatus playbackStatus) {


		int notificationAction = android.R.drawable.ic_media_pause;//needs to be initialized
		PendingIntent play_pauseAction = null;

		//Build a new notification according to the current state of the MediaPlayer
		if (playbackStatus == PlaybackStatus.PLAYING) {
			notificationAction = R.drawable.pause_amber;
			//create the pause action
			play_pauseAction = playbackAction(1);
		} else if (playbackStatus == PlaybackStatus.PAUSED) {
			notificationAction = R.drawable.play_amber;
			//create the play action
			play_pauseAction = playbackAction(0);
		}
		CharSequence name = "rahman";
		String description = ("AR Rahman Songs and Lyrics");
		String CHANNEL_ID = "com.bss.arrahmanlyrics";
		NotificationCompat.Action prev = new NotificationCompat.Action(R.drawable.skip_previous_amber,"",playbackAction(3));
		NotificationCompat.Action next = new NotificationCompat.Action(R.drawable.skip_next_amber,"",playbackAction(2));
		NotificationCompat.Action playpause = new NotificationCompat.Action(notificationAction,"",play_pauseAction);

		Intent intent = new Intent(this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);



		RequestOptions requestOptions = new RequestOptions();
		requestOptions.placeholder(R.mipmap.ic_launcher);
		requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL);
		requestOptions.error(R.mipmap.ic_launcher);
		Glide.with(getApplicationContext())
				.setDefaultRequestOptions(requestOptions)
				.asBitmap()
				.load(image_path + activeSong.getAlbum_id()+ ".png")
				.into(new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
						  @Override
						  public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {

							  Bitmap icon = resource;
							  NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MusicService.this, CHANNEL_ID)
									  .setSmallIcon(R.mipmap.ic_launcher_foreground)
									  .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle(){
										  @Override
										  public android.support.v4.media.app.NotificationCompat.MediaStyle setShowCancelButton(boolean show) {
											  return super.setShowCancelButton(true);
										  }

										  @Override
										  public android.support.v4.media.app.NotificationCompat.MediaStyle setShowActionsInCompactView(int... actions) {
											  return super.setShowActionsInCompactView(actions);
										  }
									  })
									  .setCategory(Intent.CATEGORY_APP_MUSIC)
									  .setPriority(NotificationCompat.PRIORITY_HIGH)
									  .setShowWhen(false)
									  .setAutoCancel(false)
									  .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
									  .setSound(null)
									  .setLargeIcon(icon)
									  .setContentTitle(Helper.FirstLetterCaps(activeSong.getSong_title()))
									  .setColor(ContextCompat.getColor(getApplicationContext(), R.color.primary))
									  .setContentText(Helper.FirstLetterCaps(activeSong.getAlbum_name()))
									  .addAction(prev)
									  .addAction(playpause)
									  .addAction(next)
									  .setContentIntent(pendingIntent);
							  if (playbackStatus == PlaybackStatus.PLAYING) {
								  mBuilder.setOngoing(true);
							  } else if (playbackStatus == PlaybackStatus.PAUSED) {
								  mBuilder.setOngoing(false);
							  }
							  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

								  // Create the NotificationChannel, but only on API 26+ because
								  // the NotificationChannel class is new and not in the support library
								  Log.d(TAG, "buildNotification: am here");

								  @SuppressLint("WrongConstant") NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
								  channel.setDescription(description);
								  channel.setSound(null,null);
								  // Register the channel with the system


								  NotificationManager notifManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
								  notifManager.createNotificationChannel(channel);
// notificationId is a unique int for each notification that you must define
								  notifManager.notify(NOTIFICATION_ID, mBuilder.build());
								  if (playbackStatus == PlaybackStatus.PLAYING) {
									  mBuilder.setOngoing(true);
								  } else if (playbackStatus == PlaybackStatus.PAUSED) {
									  mBuilder.setOngoing(false);
								  }

							  }else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
								  NotificationManager notifManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
								  // notificationId is a unique int for each notification that you must define
								  notifManager.notify(NOTIFICATION_ID, mBuilder.build());

							  }
						  }




					  });






		/*if (TextUtils.isEmpty(activeSong.getSong_title()) && TextUtils.isEmpty(activeSong.getAlbum_name())) {
			notificationLayout.setViewVisibility(R.id.media_titles, View.INVISIBLE);
		} else {
			notificationLayout.setViewVisibility(R.id.media_titles, View.VISIBLE);
			notificationLayout.setTextViewText(R.id.title, activeSong.getSong_title());
			notificationLayout.setTextViewText(R.id.text, activeSong.getAlbum_name());

		}*/

		/*


			//notificationLayoutBig.setViewVisibility(R.id.media_titles, View.VISIBLE);
			notificationLayoutBig.setTextViewText(R.id.title, Helper.FirstLetterCaps(activeSong.getSong_title()));

			notificationLayoutBig.setTextViewText(R.id.textAlbumName, Helper.FirstLetterCaps(activeSong.getAlbum_name()));


		RequestOptions requestOptions = new RequestOptions();
		requestOptions.placeholder(R.mipmap.ic_launcher);
		requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL);
		requestOptions.error(R.mipmap.ic_launcher);

		Glide.with(getApplicationContext())
				.setDefaultRequestOptions(requestOptions)
				.asBitmap()
				.load(image_path + activeSong.getAlbum_id()+ ".png")
				.into(new SimpleTarget<Bitmap>(80, 80) {
					@Override
					public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
						notificationLayoutBig.setImageViewBitmap(R.id.image,resource);
						if(Build.VERSION.SDK_INT >=26){
							NotificationManager notifManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

							String name = "com.bss.arrahmanlyrics";
							String id = "com.bss.arrahmanlyrics_01";
							String des = "Rahman music palyer";

							NotificationChannel channel = notifManager.getNotificationChannel(id);
							if(channel == null){
								channel = new NotificationChannel(id,name,NotificationManager.IMPORTANCE_LOW);
								channel.setDescription(des);
								channel.enableLights(true);
								channel.setSound(null,null);
								channel.setVibrationPattern(null);
								channel.enableVibration(false);
								channel.setShowBadge(false);
								channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
								notifManager.createNotificationChannel(channel);
							}
							int notificationAction = R.drawable.pause_amber;//needs to be initialized
							PendingIntent play_pauseAction = null;

							//Build a new notification according to the current state of the MediaPlayer
							if (playbackStatus == PlaybackStatus.PLAYING) {
								notificationAction = R.drawable.pause_amber;
								//create the pause action
								play_pauseAction = playbackAction(1);
							} else if (playbackStatus == PlaybackStatus.PAUSED) {
								notificationAction = R.drawable.play_amber;
								//create the play action
								play_pauseAction = playbackAction(0);
							}

							Bitmap Icon;


							Icon = BitmapFactory.decodeResource(getResources(),
									R.drawable.ic_launcher);
							Log.d(TAG, "build: getting icon"+activeSong.getAlbum_id());

							//replace with your own image


							//notificationLayoutBig.setImageViewBitmap(R.id.image,Icon);
							notificationLayout.setImageViewBitmap(R.id.image,Icon);

							Bitmap prev = BitmapFactory.decodeResource(getResources(),R.drawable.skip_previous_amber);
							Bitmap next = BitmapFactory.decodeResource(getResources(),R.drawable.skip_next_amber);
							Bitmap playPause = BitmapFactory.decodeResource(getResources(),R.drawable.play_amber);
							Bitmap close = BitmapFactory.decodeResource(getResources(),R.drawable.ic_close_black_24dp);

							if(playbackStatus == PlaybackStatus.PAUSED){
								playPause = BitmapFactory.decodeResource(getResources(),R.drawable.pause_amber);
							}
							notificationLayoutBig.setImageViewBitmap(R.id.action_play_pause,playPause);

			notificationLayout.setImageViewBitmap(R.id.action_prev, prev);
			notificationLayout.setImageViewBitmap(R.id.action_next, next);
			notificationLayout.setImageViewBitmap(R.id.action_play_pause, playPause);



			notificationLayoutBig.setImageViewBitmap(R.id.action_quit, close);
			notificationLayoutBig.setImageViewBitmap(R.id.action_prev, prev);
			notificationLayoutBig.setImageViewBitmap(R.id.action_next, next);
			notificationLayoutBig.setImageViewBitmap(R.id.action_play_pause, playPause);
			notificationLayoutBig.setViewVisibility(R.id.action_quit, View.VISIBLE);
			notificationLayoutBig.setViewVisibility(R.id.action_prev, View.VISIBLE);
			notificationLayoutBig.setViewVisibility(R.id.action_next, View.VISIBLE);
			notificationLayoutBig.setViewVisibility(R.id.action_play_pause, View.VISIBLE);

							Intent intent = new Intent(MusicService.this, MainActivity.class);
							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							PendingIntent pendInt = PendingIntent.getActivity(MusicService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

							notificationLayoutBig.setOnClickPendingIntent(R.id.btnPause,play_pauseAction);
							notificationLayoutBig.setOnClickPendingIntent(R.id.btnPrevious,playbackAction(3));
							notificationLayoutBig.setOnClickPendingIntent(R.id.btnNext,playbackAction(2));


							// Create a new Notification
							notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(MusicService.this,id)


									.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
									.setCategory(Intent.CATEGORY_APP_MUSIC)
									.setPriority(Notification.PRIORITY_DEFAULT)
									// Set the Notification style
									.setStyle(new NotificationCompat.Style() {
										@Override
										public void setBuilder(NotificationCompat.Builder builder) {
											super.setBuilder(builder);
										}
									})


									.setColor(getResources().getColor(R.color.colorPrimary))
									// Set the large and small icons
									.setContent(notificationLayoutBig)
									.setCustomBigContentView(notificationLayoutBig)
									.setSmallIcon(android.R.drawable.stat_sys_headset)
									.setLargeIcon(Icon)
									.setShowWhen(false)
									.setAutoCancel(false)
									// Set Notification content information
									.setContentIntent(pendInt)
									.setContentTitle(activeSong.getAlbum_name())
									.setContentInfo(activeSong.getSong_title());
							// Add playback actions
							//.addAction(R.drawable.skip_previous_amber, "", playbackAction(3))
							//.addAction(notificationAction, "", play_pauseAction)
							//.addAction(R.drawable.skip_next_amber, "", playbackAction(2));
							if (playbackStatus == PlaybackStatus.PLAYING) {
								notificationBuilder.setOngoing(true);
							} else if (playbackStatus == PlaybackStatus.PAUSED) {
								notificationBuilder.setOngoing(false);
							}
							//((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notificationBuilder.build());
							notifManager.notify(NOTIFICATION_ID,notificationBuilder.build());
						}else {
							int notificationAction = android.R.drawable.ic_media_pause;//needs to be initialized
							PendingIntent play_pauseAction = null;

							//Build a new notification according to the current state of the MediaPlayer
							if (playbackStatus == PlaybackStatus.PLAYING) {
								notificationAction = android.R.drawable.ic_media_pause;
								//create the pause action
								play_pauseAction = playbackAction(1);
							} else if (playbackStatus == PlaybackStatus.PAUSED) {
								notificationAction = android.R.drawable.ic_media_play;
								//create the play action
								play_pauseAction = playbackAction(0);
							}

							Bitmap Icon;
							if(albumArts.getBitmap(activeSong.getAlbum_id())!= null){
								Log.d(TAG, "buildNotification: getting icon"+activeSong.getAlbum_id());
								Icon = albumArts.getBitmap(activeSong.getAlbum_id());
							}else {
								Icon = BitmapFactory.decodeResource(getResources(),
										R.drawable.ic_launcher);
								Log.d(TAG, "build: getting icon"+activeSong.getAlbum_id());
							}
							//replace with your own image

							Intent intent = new Intent(MusicService.this, MainActivity.class);
							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							PendingIntent pendInt = PendingIntent.getActivity(MusicService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

							// Create a new Notification
							notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(MusicService.this,"rahman")

									.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
									.setCategory(Intent.CATEGORY_APP_MUSIC)
									.setPriority(Notification.PRIORITY_DEFAULT)
									// Set the Notification style
									.setStyle(new NotificationCompat.Style() {
										@Override
										public void setBuilder(NotificationCompat.Builder builder) {
											super.setBuilder(builder);
										}
									})


									.setColor(getResources().getColor(R.color.colorPrimary))
									// Set the large and small icons

									.setSmallIcon(android.R.drawable.stat_sys_headset)
									.setLargeIcon(resource)
									.setShowWhen(false)
									.setAutoCancel(false)
									// Set Notification content information
									.setContentIntent(pendInt)
									.setContentTitle(activeSong.getAlbum_name())
									.setContentInfo(activeSong.getSong_title())
									// Add playback actions
									.addAction(android.R.drawable.ic_media_previous, "", playbackAction(3))
									.addAction(notificationAction, "", play_pauseAction)
									.addAction(android.R.drawable.ic_media_next, "", playbackAction(2));
							if (playbackStatus == PlaybackStatus.PLAYING) {
								notificationBuilder.setOngoing(true);
							} else if (playbackStatus == PlaybackStatus.PAUSED) {
								notificationBuilder.setOngoing(false);
							}
							((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notificationBuilder.build());

						}
					}


				});

*/

	}

	private void removeNotification() {
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(NOTIFICATION_ID);
		stopForeground(true);
		//notificationManager.cancelAll();
	}

	private PendingIntent playbackAction(int actionNumber) {
		Intent playbackAction = new Intent(this, MusicService.class);
		switch (actionNumber) {
			case 0:
				// Play
				playbackAction.setAction(ACTION_PLAY);
				return PendingIntent.getService(this, actionNumber, playbackAction, 0);
			case 1:
				// Pause
				playbackAction.setAction(ACTION_PAUSE);
				return PendingIntent.getService(this, actionNumber, playbackAction, 0);
			case 2:
				// Next track
				playbackAction.setAction(ACTION_NEXT);
				return PendingIntent.getService(this, actionNumber, playbackAction, 0);
			case 3:
				// Previous track
				playbackAction.setAction(ACTION_PREVIOUS);
				return PendingIntent.getService(this, actionNumber, playbackAction, 0);
			default:
				break;
		}
		return null;
	}

	private void handleIncomingActions(Intent playbackAction) {
		if (playbackAction == null || playbackAction.getAction() == null) return;

		String actionString = playbackAction.getAction();
		if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
			transportControls.play();
		} else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
			transportControls.pause();
		} else if (actionString.equalsIgnoreCase(ACTION_NEXT)) {
			transportControls.skipToNext();
		} else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)) {
			transportControls.skipToPrevious();
		} else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
			transportControls.stop();
		}
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "onDestroy: ");
		//stopping the player when service is destroyed
		if (mediaPlayer != null) {
			new StorageUtil(getApplicationContext()).storeResumePosition(mediaPlayer.getCurrentPosition());
			pauseMedia();
			stopMedia();
			mediaPlayer.release();
		}
		if (phoneStateListener != null) {
			TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
			telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
		}

		unregisterReceiver(setNewAlbum);
		unregisterReceiver(playNewAudio);
		audioManager.abandonAudioFocus(this);
		removeNotification();
		stopSelf();
		super.onDestroy();

	}

	@Override
	public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
		//Log.i(TAG, "onBufferingUpdate: ");
	}

	@Override
	public void onCompletion(MediaPlayer mediaPlayer) {
		Log.i(TAG, "onCompletion: ");
		skipToNext();

	}

	@Override
	public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
		Log.i(TAG, "onError: "+i +" "+i1);
		if(maincallback != null && activeSong != null) {
			//maincallback.showDialog(activeSong);
		}
		return true;
	}

	@Override
	public void onPrepared(MediaPlayer mediaPlayer) {
		Log.i(TAG, "onPrepared: ");
		playMedia();
		if (maincallback != null) {
			maincallback.update();
		}

	}

	@Override
	public void onSeekComplete(MediaPlayer mediaPlayer) {

	}



	@Override
	public void onAudioFocusChange(int i) {
		Log.i(TAG, "onAudioFocusChange: ");
	}

	public class LocalBinder extends Binder {
		public MusicService getService() {
			return MusicService.this;
		}
	}


	private void initMediaPlayer() {

		mediaPlayer = new MediaPlayer();
		//Set up MediaPlayer event listeners
		mediaPlayer.setOnCompletionListener(this);
		mediaPlayer.setOnErrorListener(this);
		mediaPlayer.setOnPreparedListener(this);
		mediaPlayer.setOnBufferingUpdateListener(this);
		mediaPlayer.setOnSeekCompleteListener(this);

		//Reset so that the MediaPlayer is not pointing to another data source
		mediaPlayer.reset();

		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		try {
			// Set the data source to the mediaFile location

			mediaPlayer.setDataSource(setProxyUrl(activeSong.getDownload_link()));
			Log.i(TAG, "initMediaPlayer: " + activeSong.getDownload_link());
		} catch (IOException e) {
			e.printStackTrace();
			stopSelf();
		}
		equalizer = new Equalizer(0, mediaPlayer.getAudioSessionId());
		equalizer.setEnabled(true);
		mediaPlayer.prepareAsync();
		if(maincallback != null) {
			maincallback.showDialog(activeSong);
		}

	}




	private BroadcastReceiver setNewAlbum = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			//Get the new media index form SharedPreferences
			if (shuffleOn) {
				StorageUtil storage = new StorageUtil(getApplicationContext());
				playlist = getShuffledList(storage.loadAudio());
				Log.d(TAG, "onItemClick: received playlist");
				audioIndex = storage.loadAudioIndex();

			} else {
				StorageUtil storage = new StorageUtil(getApplicationContext());
				playlist = storage.loadAudio();
				Log.d(TAG, "onItemClick: received playlist");
				audioIndex = storage.loadAudioIndex();
			}


		}
	};

	private void register_setNewalbum() {
		//Register playNewMedia receiver
		IntentFilter filter = new IntentFilter(MainActivity.Broadcast_NEW_ALBUM);
		registerReceiver(setNewAlbum, filter);

	}

	private ArrayList<song> getShuffledList(ArrayList<song> list) {
		ArrayList<song> shuffleList = new ArrayList<>();
		if (list != null) {

			Random r = new Random();
			List<Integer> randomsNos = new ArrayList<>();
			for (int a = 0; a < list.size(); a++) {
				int b = 0;
				do {
					b = r.nextInt(list.size());
					if (!randomsNos.contains(b)) {
						randomsNos.add(b);
					}
				} while (randomsNos.contains(b) && randomsNos.size() != list.size());

			}

			for (int values : randomsNos) {
				shuffleList.add(list.get(values));
			}

			return shuffleList;
		}
		return null;

	}

	private BroadcastReceiver playNewAudio = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			//Get the new media index form SharedPreferences
			audioIndex = new StorageUtil(getApplicationContext()).loadAudioIndex();
			Log.d(TAG, "onItemClick: am in new song");
			if (audioIndex != -1 && audioIndex < playlist.size()) {
				//index is in a valid range
				song s = new StorageUtil(getApplicationContext()).loadAudio().get(audioIndex);
				for (song sg : playlist) {
					if (s.getSong_title().equals(sg.getSong_title())) {
						audioIndex = playlist.indexOf(sg);
						activeSong = playlist.get(audioIndex);
					}
				}

			} else {
				stopSelf();
			}

			//A PLAY_NEW_AUDIO action received
			//reset mediaPlayer to play the new Audio
			stopMedia();
			if (mediaPlayer != null) {

				mediaPlayer.reset();
			}

			initMediaPlayer();


		}
	};

	private void register_playNewAudio() {
		//Register playNewMedia receiver
		IntentFilter filter = new IntentFilter(MainActivity.Broadcast_PLAY_NEW_AUDIO);
		registerReceiver(playNewAudio, filter);
	}


	private void stopMedia() {
		if (mediaPlayer == null) return;
		if (mediaPlayer.isPlaying()) {

			mediaPlayer.stop();
			isPaused = false;

		}
	}

	private void playMedia() {
		if (mediaPlayer != null) {
			mediaPlayer.start();
			updateMetaData();
			buildNotification(PlaybackStatus.PLAYING);
			isPaused = false;
		}
	}

	public void pauseMedia() {
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.pause();
			updateMetaData();
			buildNotification(PlaybackStatus.PAUSED);
			isPaused = true;

			new StorageUtil(getApplicationContext()).storeResumePosition(mediaPlayer.getCurrentPosition());
		}
	}

	public void resumeMedia() {
		resumePosition = new StorageUtil(getApplicationContext()).getResumePosition();
		if (mediaPlayer != null) {
			if (!mediaPlayer.isPlaying()) {
				if (resumePosition != -1) {
					mediaPlayer.seekTo(resumePosition);
					mediaPlayer.start();
					buildNotification(PlaybackStatus.PLAYING);
					isPaused = false;

				}
			}
		}

	}

	public void skipToPrevious() {

		if (audioIndex <= 0) {
			//if first in playlist
			//set index to the last of audioList
			audioIndex = playlist.size() - 1;
			activeSong = playlist.get(audioIndex);
		} else {
			//get previous in playlist
			activeSong = playlist.get(--audioIndex);
		}
		new StorageUtil(getApplicationContext()).storeAudioIndex(audioIndex);

		stopMedia();
		//reset mediaPlayer
		mediaPlayer.reset();

		initMediaPlayer();
		updateMetaData();
		buildNotification(PlaybackStatus.PLAYING);

		Log.i(TAG, "skipToPrevious: " + audioIndex);
	}

	public void skipToNext() {
		if (playlist.size() > 0) {
			if (audioIndex == playlist.size() - 1) {
				//if last in playlist
				audioIndex = 0;
				activeSong = playlist.get(audioIndex);
			} else if (audioIndex >= playlist.size()) {
				audioIndex = 0;
				activeSong = playlist.get(audioIndex);
				//get next in playlist


			} else {
				activeSong = playlist.get(++audioIndex);
			}
		}
		new StorageUtil(getApplicationContext()).storeAudioIndex(audioIndex);

		stopMedia();
		//reset mediaPlayer
		mediaPlayer.reset();
		initMediaPlayer();
		updateMetaData();
		buildNotification(PlaybackStatus.PLAYING);
		Log.i(TAG, "skipToNext: " + audioIndex);
	}

	public boolean isPlaying() {
		if (mediaPlayer == null) {
			return false;
		} else if (mediaPlayer.isPlaying()) {
			return true;
		}
		return false;
	}

	public int getDuration() {


		return mediaPlayer.getDuration();
	}

	public int getCurrentPosition() {
		return mediaPlayer.getCurrentPosition();
	}

	public song getActiveSong() {
		return activeSong;
	}

	public boolean isShuffleOn() {
		return shuffleOn;
	}

	public void setShuffleOnOff(boolean set) {
		if (set) {

			StorageUtil storage = new StorageUtil(getApplicationContext());
			playlist = getShuffledList(storage.loadAudio());
			if(playlist==null){
				Toast.makeText(this, "no playlist to shuffle", Toast.LENGTH_SHORT).show();
				return;

			}
			if (mediaPlayer != null) {
				if (isPlaying() || mediaPlayer.getCurrentPosition() > 0) {
					for (song sg : playlist) {
						if (sg.getAlbum_name().equals(activeSong.getAlbum_name()) && sg.getSong_title().equals(activeSong.getSong_title())) {
							audioIndex = playlist.indexOf(sg);
							storage.storeAudioIndex(audioIndex);

						}
					}
				}
			}
			shuffleOn = set;

		} else {
			StorageUtil storage = new StorageUtil(getApplicationContext());
			playlist = storage.loadAudio();
			if(playlist==null){
				Toast.makeText(this, "no playlist to shuffle", Toast.LENGTH_SHORT).show();
				return;

			}
			if (mediaPlayer != null) {
				if (isPlaying() || mediaPlayer.getCurrentPosition() > 0) {
					for (song sg : playlist) {
						if (sg.getAlbum_name().equals(activeSong.getAlbum_name()) && sg.getSong_title().equals(activeSong.getSong_title())) {
							audioIndex = playlist.indexOf(sg);
							storage.storeAudioIndex(audioIndex);
							Log.i(TAG, "setShuffleOnOff: inside set index");

						}
					}
				}
			}

			shuffleOn = set;
		}
	}

	public boolean requestAudioFocus() {
		int result = audioManager.requestAudioFocus(afChangeListener,
				// Use the music stream.
				AudioManager.STREAM_MUSIC,
				// Request permanent focus.
				AudioManager.AUDIOFOCUS_GAIN);

		if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
			return true;
			// Start playback.
		} else return false;
	}

	public boolean isPaused(){
		return isPaused;
	}
}
