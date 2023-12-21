package com.example.threads;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.threads.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity {
  private static final Object lock = new Object();

  boolean notStarted = true;
  String output = "";
  protected static Object[] turns = {new Object(), new Object(), new Object()};
  protected static int nextTurn = 1;
  protected static Handler msgHandler;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());


    msgHandler = new Handler(Looper.getMainLooper()) {
      @Override
      public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);
        char[] chars = (char[]) msg.obj;
        String str = String.valueOf(chars);
        output += str;
        binding.output.setText(output);
      }
    };


    EditText[] editTexts = {binding.ETTop, binding.ETCenter, binding.ETBottom};

    binding.GOButton.setOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            if (notStarted) {
              MyThread firstThread = new MyThread(editTexts[0].getText().toString());
              MyThread secondThread = new MyThread(editTexts[1].getText().toString());
              MyThread thirdThread = new MyThread(editTexts[2].getText().toString());

              firstThread.start();
              secondThread.start();
              thirdThread.start();

              try {
                synchronized (turns[1]) {
                  turns[1].wait();
                }
                synchronized (turns[2]) {
                  turns[2].wait();
                }
              } catch (InterruptedException e) {
                throw new RuntimeException(e);
              }

              notStarted = false;
              for (EditText et : editTexts) {
                et.setFocusable(false);
                et.setLongClickable(false);
                et.setCursorVisible(false);
              }
            }
          }
        });
  }
}

class MyThread extends Thread {
  private char[] TextToView;
  private String text;

  public MyThread(String text) {
    this.text = text;
    TextToView = new char[text.length()];
  }

  @Override
  public void run() {
    super.run();
    char[] textChars = text.toCharArray();

    synchronized (MainActivity.turns[MainActivity.nextTurn % 3 - 1]) {
      for (int i = 0; i != textChars.length; i++) {
        char ch = textChars[i];
        TextToView[i] = ch;


        Message msg = new Message();
        msg.obj = TextToView;
        MainActivity.msgHandler.sendMessage(msg);

        try {
          if (ch == '.' || ch == '?' || ch == '!') {
            Thread.sleep(1000);
          } else {
            Thread.sleep(200);
          }

          if (ch == ' ') {
            MainActivity.turns[MainActivity.nextTurn % 3].notify();
            MainActivity.turns[MainActivity.nextTurn % 3 - 1].wait();

          }
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }
}


