package com.example.daksh.howtousehandlers;

import java.util.Random;

/**
 * This is not a real download task.
 * It just sleeps for some random time when it's launched.
 * The idea is not to require a connection and not to eat it.
 */
public class DownloadTask implements Runnable {

  private static final String TAG = DownloadTask.class.getSimpleName();

  private static final Random random = new Random();

  private int lengthSec;

  DownloadTask() {
    lengthSec = random.nextInt(3) + 1;
  }

  @Override public void run() {
    try {
      Thread.sleep(lengthSec * 100);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
