package com.example.daksh.howtousehandlers;

import android.os.Looper;
import android.os.Handler;
import android.util.Log;

/**
 * Created by daksh
 */
public class DownloadThread extends Thread {

  private static final String TAG = DownloadThread.class.getSimpleName();

  private Handler handler;

  private int totalQueued;

  private int totalCompleted;

  private DownloadThreadListener listener;

  DownloadThread(DownloadThreadListener listener) {
    this.listener = listener;
  }

  @Override public void run() {
    try {
      //Preparing a looper on current thread
      //the "current thread" is being detected implicitly
      Looper.prepare();

      //Now, the handler will automatically bind to the Looper that is attached to the current thread.
      //You don't need to specify the Looper explicitely.
      handler = new Handler();

      //Now, after the following line i.e. Looper.loop()
      //the thread will start running the message loop i.e. pipeline
      //and will not normally exit the loop unlessa a problem orrurs or
      //you call the quit() method.
      Looper.loop();

      Log.i(TAG, "Pipeline constructed successfully");
    } catch (Throwable t) {
      Log.e(TAG, "DownloadThread halted due to an error", t);
    }
  }

  //The method is allowed to be called from any thread
  public synchronized void requestStop() {
    //using the handler, post a Runnable that will
    //quit() the looper attached to our DownloadThread
    //But, all the queued tasks will be executed
    //before the looper gets to quit the Runnable
    handler.post(new Runnable() {
      @Override public void run() {
        //This is guaranteed to run on the DownloadThread
        //so we can use myLooper() to get the current looper
        Log.i(TAG, "DownloadThread loop exited by request");
        Looper myLooper = Looper.myLooper();
        if (myLooper != null) {
          myLooper.quit();
        }
      }
    });
  }

  public synchronized void enqueueDownload(final DownloadTask task) {
    //Wrap DownloadTask into another Runnable to track the statistics
    handler.post(new Runnable() {
      @Override public void run() {
        try {
          task.run();
        } finally {
          synchronized (DownloadThread.this) {
            totalCompleted++;
          }
          signalUpdate();
        }
      }
    });
    totalQueued++;
    // tell the listeners that queue has now increased
    signalUpdate();
  }

  public synchronized int getTotalQueued() {
    return totalQueued;
  }

  public synchronized int getTotalCompleted() {
    return totalCompleted;
  }

  //Because this method is being called from a thread, so it is the responsibility of the activity
  //that extends the listener to handle the code in UI thread using a handler.
  private void signalUpdate() {
    if (listener != null) {
      listener.handleDownloadThreadUpdate();
    }
  }
}
