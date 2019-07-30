package affily.id.mymediaplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class MediaService extends Service implements MediaPlayerCallback {
    MediaPlayer mediaPlayer = null;
    boolean isReady;

    public final static String ACTION_CREATE = "create";
    public final static String ACTION_DESTROY = "destroy";
    public static final int PLAY = 1;
    public static final int STOP = 0;

    final String TAG = MediaService.class.getSimpleName();

    public MediaService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }


    final Messenger messenger = new Messenger(new IncomingHandler(this));

    private static class IncomingHandler extends Handler {
        private WeakReference<MediaPlayerCallback> callbackWeakReference;

        IncomingHandler(MediaPlayerCallback callback) {
            this.callbackWeakReference = new WeakReference<>(callback);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case PLAY:
                    callbackWeakReference.get().onPlay();
                    break;
                case STOP:
                    callbackWeakReference.get().onStop();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case ACTION_CREATE:
                    if (mediaPlayer == null) {
                        init();
                    }
                    break;
                case ACTION_DESTROY:
                    if (!mediaPlayer.isPlaying()) {
                        stopSelf();
                    }
                    break;
                default:
                    break;
            }
        }

        Log.d(TAG, "startCommand");
        return flags;
    }

    @Override
    public void onPlay() {
        //apakah media player sudah siap
        if (!isReady) {
            //jika belum
            mediaPlayer.prepareAsync();
        } else {
            //jika sudah
            //apakah media player sedang diputar
            if (mediaPlayer.isPlaying()) {
                //jika iya
                showNotifMusic();
                mediaPlayer.pause();
            } else {
                //jika tidak
                mediaPlayer.start();
            }
        }
    }

    @Override
    public void onStop() {
        //apakah media player sedang diputar atau sudah siap
        if (mediaPlayer.isPlaying() || isReady) {
            mediaPlayer.stop();
            stopNotifMusic();
            isReady = false;
        }
    }

    private void init() {
        //siapkan media playernya
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        //tentukan asset audio yg diputar
        AssetFileDescriptor assetFileDescriptor = getApplicationContext().getResources().openRawResourceFd(R.raw.gontor);
        try {
            mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                isReady = true;
                mediaPlayer.start();
                showNotifMusic();
            }
        });

        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                return false;
            }
        });
    }

    void showNotifMusic(){
        Intent notifIntent = new Intent(this,MainActivity.class);
        notifIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,notifIntent,0);

        String CHANNEL_DEFAULT_IMPORTANT = "Channel_test";
        int ONGOING_NOTIFICATION_ID = 1;

        Notification notification = new NotificationCompat.Builder(this,CHANNEL_DEFAULT_IMPORTANT)
                .setContentTitle("TEST 1")
                .setContentText("TEST 2")
                .setSmallIcon(R.drawable.ic_music_note_black_24dp)
                .setContentIntent(pendingIntent)
                .setTicker("TEST 3")
                .build();

        createChannel(CHANNEL_DEFAULT_IMPORTANT);

        startForeground(ONGOING_NOTIFICATION_ID,notification);

    }

    private void createChannel(String CHANNEL_ID) {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,"Battery",NotificationManager.IMPORTANCE_DEFAULT);
            channel.setShowBadge(false);
            channel.setSound(null,null);
            notificationManager.createNotificationChannel(channel);
        }
    }

    void stopNotifMusic(){
        stopForeground(false);
    }

}
