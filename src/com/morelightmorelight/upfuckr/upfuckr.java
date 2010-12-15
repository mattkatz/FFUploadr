package com.morelightmorelight.upfuckr;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.Intent;
import android.net.Uri;
import android.widget.Button;
import android.widget.ImageView;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.database.Cursor;
import android.provider.MediaStore;


import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;

import android.util.Log;


public class upfuckr extends Activity
{
  private static final int ADD_ID = Menu.FIRST;
  private static final int GALLERIES = ADD_ID +1;
  private static final int ACTIVITY_CREATE = 0;
  private static final int IMAGE_PICK = 1;
  private static final int UPLOAD_IMAGE = 2;
  private static final String TAG = "UpFuckr: ";


  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);
      Button uploadPic = (Button) findViewById(R.id.upload_btn);
      uploadPic.setOnClickListener(new View.OnClickListener(){
      public void onClick(View view){
          setResult(RESULT_OK);
          //get a file to upload, then upload

          //I dunno, upload?
          getImages();

        }
      });
      Intent i = getIntent();
      String action = i.getAction();
      if(null != action){
        Log.i(TAG,action);
      }
      else{
        Log.i(TAG,"no action?!");
      }
      // if we don't have any preferences, let's get them configured
      if(! isConfigured()){
        new AlertDialog.Builder(this)
          .setMessage(R.string.notConfigured)
          .setPositiveButton("OK", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
              add_site();
            }
          })
          .show();
        //add_site();
      }

//      if(Intent.ACTION_SEND.equals(action))
//      {
//        String type = i.getType();
//        Log.i(TAG, "we have action send!");
//        Uri stream = (Uri) i.getParcelableExtra(Intent.EXTRA_STREAM);
//        if ( stream != null && type != null )
//        {
//          ArrayList l = new ArrayList();
//          l.add(stream);
//          upload( l);
//        }
//        else { Log.i(TAG,"null URI");}
//      }
//      else if (Intent.ACTION_SEND_MULTIPLE.equals(action))
//      {
//        String type = i.getType();
//        Log.i(TAG, "we have action send!");
//        //Bundle extras = getIntent().getExtras();
//        ArrayList l = i.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
//        upload(l);
//      }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    super.onCreateOptionsMenu(menu);
    menu.add(0,ADD_ID,0,R.string.add_site);
    menu.add(0,GALLERIES,0,"Galleries");
    return true;
  }

  @Override
  public boolean onMenuItemSelected(int featureId, MenuItem item)
  {
    switch(item.getItemId()){
      case ADD_ID:
        add_site();
        return true;
      case GALLERIES:
        show_galleries();
        return true;
    }
    return super.onMenuItemSelected(featureId,item);
  }

  private void show_galleries(){
    startActivity(new Intent(this, galleries.class));

  }

  private void add_site(){
    startActivity(new Intent(this,Prefs.class));
  }

  private boolean isConfigured()
  {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    String [] prefNames = {"host","path","user", "pass"};
    
    for(int i = 0; i < 4; i++)
    {
      if( prefs.getString( prefNames[i],"").equals("")){
        return false;
      }
    }
    return true;
  }
  protected void onActivityResult(int requestCode, int resultCode, Intent i){
    Log.i(TAG,"Request Code: " + requestCode);
    Log.i(TAG,"Result Code: " + resultCode);

    switch(requestCode){
      case IMAGE_PICK:
        //if (resultCode == RESULT_OK){
          //ArrayList l = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        Uri stream = (Uri) i.getData();
        if ( stream != null )
        {
          //ArrayList l = new ArrayList();
          //l.add(stream);
          Intent intent = new Intent(this, uploadr.class);
          //intent.putExtras(i);
          intent.setData(i.getData());
          startActivityForResult(intent, UPLOAD_IMAGE);
          //upload( l);
        }
        else { Log.i(TAG,"null URI");}
        //  upload(l);
        //}
    }
  }

  private void getImages(){
    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
    intent.setType("image/*");
    startActivityForResult(intent, IMAGE_PICK);
  }
    

  
}
