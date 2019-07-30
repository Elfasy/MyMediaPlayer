package affily.id.mymediaplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    Button btnPlay, btnStop;
    Messenger messenger = null;
    Intent bindServiceIntent;
    boolean isBind = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnPlay = findViewById(R.id.btn_play);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isBind) return;
                try {
                    messenger.send(Message.obtain(null,MediaService.PLAY,0,0));
                }catch (RemoteException e){
                    e.printStackTrace();
                }
            }
        });
        btnStop = findViewById(R.id.btn_stop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isBind) return;
                try{
                    messenger.send(Message.obtain(null,MediaService.STOP,0,0));
                }catch (RemoteException e){
                    e.printStackTrace();
                }
            }
        });

        bindServiceIntent = new Intent(MainActivity.this,MediaService.class);
        bindServiceIntent.setAction(MediaService.ACTION_CREATE);
        startService(bindServiceIntent);
        bindService(bindServiceIntent,serviceConnection,BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
        bindServiceIntent.setAction(MediaService.ACTION_DESTROY);

        startService(bindServiceIntent);
    }

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            messenger = new Messenger(iBinder);
            isBind = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            messenger = null;
            isBind = false;
        }
    };
}
