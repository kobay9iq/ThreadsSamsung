package com.example.threads;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

  private EditText etTop, etCenter, etBottom;
  private Handler handler;
  private Button goButton;
  private TextView outputTextView;

  private Object lock = new Object();
  private int currentIndex = 0;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    etTop = findViewById(R.id.ET_top);
    etCenter = findViewById(R.id.ET_center);
    etBottom = findViewById(R.id.ET_bottom);
    goButton = findViewById(R.id.GO_button);
    outputTextView = findViewById(R.id.output);

    handler =
        new Handler(Looper.getMainLooper()) {
          @Override
          public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            String str = (String) msg.obj;
            outputTextView.append(str);
          }
        };

    goButton.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            startThreads();
          }
        });
  }

  private void startThreads() {
    String topText = etTop.getText().toString();
    String centerText = etCenter.getText().toString();
    String bottomText = etBottom.getText().toString();

    Thread threadTop = createThread(topText, 0);
    Thread threadCenter = createThread(centerText, 1);
    Thread threadBottom = createThread(bottomText, 2);

    threadTop.start();
    threadCenter.start();
    threadBottom.start();
  }

  private Thread createThread(final String text, final int threadIndex) {
    return new Thread(
        new Runnable() {
          @Override
          public void run() {
            String[] words = text.split(" ");

            for (String word : words) {
              synchronized (lock) {
                while (currentIndex != threadIndex) {
                  try {
                    lock.wait();
                  } catch (InterruptedException e) {
                    e.printStackTrace();
                  }
                }

                for (int i = 0; i != word.length(); i++) {
                  String ch = String.valueOf(word.charAt(i));

                  if (i == word.length() - 1) {
                    ch += " ";
                  }

                  Message msg = new Message();
                  msg.obj = ch;
                  handler.sendMessage(msg);

                  try {
                    if (ch.contains(" ") || ch.contains(".") || ch.contains("!") || ch.contains("?")) {

                      Thread.sleep(1000);
                    } else {
                      Thread.sleep(500);
                    }
                  } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                  }
                }

                currentIndex = (currentIndex + 1) % 3;

                lock.notifyAll();
              }
            }
          }
        });
  }
}
