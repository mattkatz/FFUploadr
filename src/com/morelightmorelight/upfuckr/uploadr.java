package com.morelightmorelight.upfuckr;

import android.app.Activity;
import android.os.Bundle;

import android.view.View;
import android.content.Intent;
import android.net.Uri;
import android.widget.Button;
import android.widget.TextView;
import android.graphics.drawable.Drawable;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.database.Cursor;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.net.ftp.*;
import java.io.*;

import android.os.Handler;
import android.os.AsyncTask;


import android.util.Log;

public class uploadr extends Activity
{
  //private static final int ADD_ID = Menu.FIRST;
  private static final String TAG = "UpLoadr: ";
  private static final int FINISH_PAUSE = 6000;
  private SharedPreferences prefs;


  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.upload);
      Button cancel = (Button) findViewById(R.id.cancel_btn);
      cancel.setOnClickListener(new View.OnClickListener(){
        public void onClick(View view){
          setResult(RESULT_OK);
          //TODO:cancel the upload
          finish();
        }
      });
      prefs = PreferenceManager.getDefaultSharedPreferences(this);
  }
  public void onStart(){
      super.onStart();
      Log.v(TAG, "onStart");
  }
  public void onResume(){
      super.onResume();
      Log.v(TAG, "onResume");
      checkAndUpload();
  }

  private void checkAndUpload(){
      Intent i = getIntent();
      String action = i.getAction();
      if(null != action){
        Log.v(TAG,action);
      }
      else{
        Log.v(TAG,"no action?!");
      }
      

      ArrayList l = null;
      //String type = i.getType();
      String pathExt = i.getStringExtra("PATH_EXTENSION");
      if (Intent.ACTION_SEND_MULTIPLE.equals(action))
      {
        Log.v(TAG, "we have action send multiple!");
        l = i.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
      }
      else{
        Log.v(TAG, "we have action send!");
        Uri stream = (Uri) i.getParcelableExtra(Intent.EXTRA_STREAM);
        if(null == stream){
          stream = (Uri) i.getData();
        }
        Log.v(TAG, "stream: " + stream);
        if ( stream != null /*&& type != null*/ )
        {
          l = new ArrayList<Uri>();
          l.add(stream);
        }
        else { Log.v(TAG,"null URI or type");}
      }
      ArrayList pathList = new ArrayList();
      pathList.add(pathExt);
      new UploadTask().execute(l,pathList);
  }
  private Runnable getOut = new Runnable(){
    public void run(){
      finish();
    }
  };
  private void waitALilBit(){
    Handler h = new Handler();
    h.postDelayed(getOut, FINISH_PAUSE);
  }
  private void status(String message){
    TextView t = (TextView) findViewById(R.id.uploading);
    t.setText("Uploading \n" + message);
  }

  public void setBackground(String filePath){
    //ImageView img = (ImageView) findViewById(R.id.img);
    //img.setImageURI(uri);
    Log.v(TAG, filePath);
    Drawable d = Drawable.createFromPath(filePath);
    findViewById(R.id.uploading).setBackgroundDrawable(d);
  }



  // And to convert the image URI to the direct file system path of the image file
  public String getRealPathFromURI(Uri contentUri) {
    Log.v(TAG, "uri: " + contentUri);
    // can post image
    String [] proj={MediaStore.Images.Media.DATA};
    Cursor cursor = managedQuery( contentUri,
        proj, // Which columns to return
        null,       // WHERE clause; which rows to return (all rows)
        null,       // WHERE clause selection arguments (none)
        null); // Order-by clause (ascending by name)
    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
    cursor.moveToFirst();
    String path = cursor.getString(column_index); 
    Log.v(TAG,"path: " + path);
    return path;
  }

  /**
   * this handles all the async business of logging in, uploading the images, etc.
   */
  public class UploadTask extends AsyncTask<ArrayList, String, String>{

    @Override
    protected String doInBackground(ArrayList... uris){
      //Don't understand how to just send my arraylist or array into this params style method...  There has to be a simpler more elegant way than this.
      ArrayList contentUris = uris[0];
      Log.v(TAG,"contentUris.size()="+contentUris.size());
        

      //get our shared preferences
      String host = prefs.getString("host","");
      String path = prefs.getString("path","");
      String user = prefs.getString("user","");
      String pass = prefs.getString("pass","");
      Log.v(TAG,"about to ftp to " + host);
      Log.v(TAG,"uris.length="+uris.length);
      if(uris.length > 1){
        ArrayList paths = uris[1];
        Log.v(TAG,"paths.size="+paths.size());
        Object p = paths.get(0);
        if(null != p){
          String pathExt = p.toString();
          path += pathExt;
        }
      }

      FTPClient ftp = new FTPClient();
      try{
        publishProgress("Connecting:\n" + host);
        ftp.connect(host);
        ftp.enterLocalPassiveMode();
        Log.v(TAG,"we connected");
        publishProgress("Connected:\n" + host);
        if(!ftp.login(user,pass)){
          ftp.logout();
          //TODO: alert user it didn't happen
          return "Didn't Work!\nWe Couldn't log in";
        }
        String replyStatus = ftp.getStatus();
        Log.v(TAG,replyStatus);
        int replyCode = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(replyCode))
        {
          ftp.disconnect();
          //TODO: alert user it didn't happen
          return "Didn't Work!\nWe Couldn't log in";
        }

        Log.v(TAG,"we logged in");
        if(ftp.changeWorkingDirectory(path)){
          Log.i(TAG, "changed to " + path);
        }
        else{
          Log.i(TAG, "we didn't change directory! ");
          Log.i(TAG,"Does "+ path+" make sense?");
        }
        Log.v(TAG, "about to set file type");
        ftp.setFileType(ftp.BINARY_FILE_TYPE);
        Log.v(TAG, "set file type");
        for(int i = 0; i < contentUris.size(); i++){
          Log.v(TAG,"uploading new file");
          Uri uri = (Uri) contentUris.get(i);
          String filePath = getRealPathFromURI(uri);
          File file = new File(filePath);
          String fileName = file.getName();
          publishProgress(fileName,filePath);
          //InputStream in = new FileInputStream(filePath);
          InputStream in = new FileInputStream(file);
          ftp.setFileType(ftp.BINARY_FILE_TYPE);
          //why? because otherwise you don't see progress and you freak out
          Thread.sleep(200);
          boolean Store = ftp.storeFile(fileName, in);
          in.close();
          Log.v(TAG, "uploaded "+ fileName);
        }
        //TODO: probably move this to a finally block
        ftp.disconnect();
      }
      catch(Exception ex){
        //TODO: properly handle exception
        Log.e(TAG,ex.toString());
        //TODO:Alert the user this failed
      }

      //the main work
      return "Done!";

    }
    protected void onProgressUpdate(String... messages){
      //change the status 
      status( messages[0]);
      if(messages.length > 1){
        //change the background image
        setBackground(messages[1]);
      }
    }

    protected void onPostExecute(String message){
      //change the status to done
      status(message);
      //wait and finish
      waitALilBit();
    }
    protected void onPreExecute(){
      //do we even need this?

    }

  }
}
