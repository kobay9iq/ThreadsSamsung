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

  boolean notStarted = true;
  private boolean switchTurn = true;
  String output = "";
  protected static Object[] turns = {new Object(), new Object(), new Object()};
  protected static Handler msgHandler;
  protected static Handler turnHandler;
  protected static int currentTurn = 1;

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

    turnHandler = new Handler(Looper.getMainLooper()) {
      @Override
      public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);
        switchTurn = (boolean) msg.obj;
      }
    };


    EditText[] editTexts = {binding.ETTop, binding.ETCenter, binding.ETBottom};

    binding.GOButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        int nextTurn = 0;

        if (notStarted) {
          MyThread firstThread = new MyThread(editTexts[0].getText().toString(), turns[0]);
          MyThread secondThread = new MyThread(editTexts[1].getText().toString(), turns[1]);
          MyThread thirdThread = new MyThread(editTexts[2].getText().toString(), turns[2]);


          try {
            synchronized (turns[0]) {
              turns[0].wait();
            }
            synchronized (turns[1]) {
              turns[1].wait();
            }
            synchronized (turns[2]) {
              turns[2].wait();
            }
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }

          firstThread.start();
          secondThread.start();
          thirdThread.start();



          notStarted = false;
          for (EditText et : editTexts) {
            et.setFocusable(false);
            et.setLongClickable(false);
            et.setCursorVisible(false);
          }


          while (true) {
            if (switchTurn) {
              turns[nextTurn % 3].notify();
              nextTurn++;
              switchTurn = false;
            }
          }
        }
      }
    });


  }
}

class MyThread extends Thread {
  private char[] TextToView;
  private String text;
  private Object obj;

  public MyThread(String text, Object obj) {
    this.text = text;
    this.obj = obj;
    TextToView = new char[text.length()];
  }

  @Override
  public void run() {
    super.run();
    char[] textChars = text.toCharArray();

    synchronized (obj) {
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
            Message turnOver = new Message();
            turnOver.obj = true;
            MainActivity.turnHandler.sendMessage(msg);
            obj.wait();
          }
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }
}


