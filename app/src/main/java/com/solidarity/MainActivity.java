
package com.solidarity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONObject;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {
    private static final int PERMISSION_REQUEST_SEND_SMS = 123;
    private TextView messageTextView;
    private Button sendButton;
    private String currentMessage = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        messageTextView = new TextView(this);
        sendButton = new Button(this);
        sendButton.setText("إرسال الرسالة");

        setContentView(messageTextView);
        addContentView(sendButton, new android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT));

        fetchMessage();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.SEND_SMS},
                            PERMISSION_REQUEST_SEND_SMS);
                } else {
                    sendSMS("01146028426", currentMessage);
                }
            }
        });
    }

    private void fetchMessage() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    URL url = new URL("https://lobay95670.pythonanywhere.com/get-message");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    InputStreamReader reader = new InputStreamReader(conn.getInputStream());
                    StringBuilder sb = new StringBuilder();
                    int data = reader.read();
                    while (data != -1) {
                        sb.append((char) data);
                        data = reader.read();
                    }
                    reader.close();
                    return sb.toString();
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                try {
                    JSONObject json = new JSONObject(result);
                    currentMessage = json.getString("message");
                    messageTextView.setText(currentMessage);
                } catch (Exception e) {
                    messageTextView.setText("فشل في تحميل الرسالة.");
                }
            }
        }.execute();
    }

    private void sendSMS(String phoneNumber, String message) {
        try {
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(this, "تم إرسال الرسالة", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "فشل في إرسال الرسالة", Toast.LENGTH_SHORT).show();
        }
    }
}
