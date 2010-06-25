package com.morelightmorelight.upfuckr;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import android.view.View;

public class Credentials extends Activity
{
  private EditText mUser;
  private EditText mPass;
  private EditText mHost;
  private EditText mPath;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.credentials);
        mUser = (EditText) findViewById(R.id.user);
        mPass = (EditText) findViewById(R.id.pass);
        mHost = (EditText) findViewById(R.id.host);
        mPath = (EditText) findViewById(R.id.path);
        Button confirmButton = (Button) findViewById(R.id.confirm);
        confirmButton.setOnClickListener(new View.OnClickListener() {
          public void onClick(View view) {
            setResult(RESULT_OK);
            finish();
          }
        });

    }

    private void saveState(){


    }
}
