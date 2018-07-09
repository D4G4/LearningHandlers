package com.example.daksh.howtousehandlers;

import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Locale;
import java.util.Random;

//DownloadQueueActivity
public class MainActivity extends AppCompatActivity implements DownloadThreadListener {

  DownloadThread downloadThread;
  Handler mHander;
  private ProgressBar progressBar;
  private TextView statusText;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    //Create and start DownloadThread
    downloadThread = new DownloadThread(this);
    downloadThread.start();

    //Create the Handler here and it will implicitly bind the Looper
    //which is internally created for this thread(Main/UI thread)
    mHander = new Handler();
    initViews();
  }

  private void initViews() {
    progressBar = findViewById(R.id.progress_bar);
    statusText = findViewById(R.id.status_text);
    Button scheduleButton = findViewById(R.id.schedule_button);
    scheduleButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        int totalTasks = new Random().nextInt(3) + 1;
        for (int i = 0; i < totalTasks; i++) {
          if (downloadThread != null) {
            downloadThread.enqueueDownload(new DownloadTask());
          }else{
            Toast.makeText(MainActivity.this, "Oops!", Toast.LENGTH_SHORT).show();
          }
        }
      }
    });
  }

  @Override public void handleDownloadThreadUpdate() {
    //Update progress bar in Main/UI thread from background thread?
    //Use Handlers ;)
    mHander.post(new Runnable() {
      @Override public void run() {
        int total = downloadThread.getTotalQueued();
        int completed = downloadThread.getTotalCompleted();

        progressBar.setMax(total);

        progressBar.setProgress(0);
        progressBar.setProgress(completed);
        statusText.setText(String.format(Locale.US, "Downloaded %d/%d", completed, total));

        //Viberate for fun
        if (completed == total) {
          Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
          if (vibrator != null) {
            vibrator.vibrate(100);
          }
        }
      }
    });
    //Wait what? That's it?
    //Yep, the thing is, mHandler is associated with your current thread i.e. UI thread whereas
    //the Handler inside your DownloadThread class is associated with "that" background thread.
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    downloadThread.requestStop();
  }
}
