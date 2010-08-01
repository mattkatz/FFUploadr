package com.morelightmorelight.upfuckr;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.Intent;
import android.net.Uri;
import android.widget.Button;
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
  private static final int ACTIVITY_CREATE = 0;
  private static final String TAG = "UpFuckr: ";


  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);
      Button uploadPic = (Button) findViewById(R.id.upload);
      uploadPic.setOnClickListener(new View.OnClickListener(){
        public void onClick(View view){
          setResult(RESULT_OK);
          //get a file to upload, then upload

          //I dunno, upload?

          upload(null);
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

      if(Intent.ACTION_SEND.equals(action))
      {
        String type = i.getType();
        Log.i(TAG, "we have action send!");
        //Bundle extras = getIntent().getExtras();
        Uri stream = (Uri) i.getParcelableExtra(Intent.EXTRA_STREAM);
        if ( stream != null && type != null )
        {
          Log.i(TAG,stream.toString());
          upload( getRealPathFromURI( stream));


        }
        else { Log.i(TAG,"null URI");}
      }

  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    super.onCreateOptionsMenu(menu);
    menu.add(0,ADD_ID,0,R.string.add_site);
    return true;
  }

  @Override
  public boolean onMenuItemSelected(int featureId, MenuItem item)
  {
    switch(item.getItemId()){
      case ADD_ID:
        add_site();
        return true;
    }
    return super.onMenuItemSelected(featureId,item);
  }

  private void add_site(){
    //Intent i = new Intent(this, Credentials.class);
    //startActivityForResult(i,ACTIVITY_CREATE);
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
    

  private void upload(String contentPath)
  {
    if(null == contentPath){
      return;
    }
    Log.i(TAG, contentPath);
    //get our shared preferences
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    String host = prefs.getString("host","");
    String path = prefs.getString("path","");
    String user = prefs.getString("user","");
    String pass = prefs.getString("pass","");
    boolean pasv = prefs.getBoolean("pasv",false);

    Intent intent = new Intent();
    intent.setAction(Intent.ACTION_PICK);
    // FTP URL (Starts with ftp://, sftp:// or ftps:// followed by hostname and port).
    Uri ftpUri = Uri.parse("ftp://"+host+":21");
    intent.setDataAndType(ftpUri, "vnd.android.cursor.dir/lysesoft.andftp.uri");
    // // FTP credentials (optional)
    intent.putExtra("ftp_username", user);
    intent.putExtra("ftp_password", pass);
    //intent.putExtra("ftp_keyfile", "/sdcard/dsakey.txt");
    //intent.putExtra("ftp_keypass", "optionalkeypassword");
    // FTP settings (optional)
    intent.putExtra("ftp_pasv", "true");
    //intent.putExtra("ftp_resume", "true");
    //intent.putExtra("ftp_encoding", "UTF8");
    // Upload
    intent.putExtra("command_type", "upload");
    // Activity title
    intent.putExtra("progress_title", "Uploading files ...");
    //intent.putExtra("local_file1", "/sdcard/subfolder1/file1.zip");
    //intent.putExtra("local_file2", "/sdcard/subfolder2/file2.zip");
    intent.putExtra("local_file1", contentPath);
    //intent.putExtra("local_file1", "/sdcard/DCIM/Camera/2010-06-09 03.28.22.jpg");
    // Optional initial remote folder (it must exist before upload)
    Log.i(TAG,path);
    intent.putExtra("remote_folder", path);
    startActivityForResult(intent, 1);
  }

  // And to convert the image URI to the direct file system path of the image file
  public String getRealPathFromURI(Uri contentUri) {

    // can post image
    String [] proj={MediaStore.Images.Media.DATA};
    Cursor cursor = managedQuery( contentUri,
        proj, // Which columns to return
        null,       // WHERE clause; which rows to return (all rows)
        null,       // WHERE clause selection arguments (none)
        null); // Order-by clause (ascending by name)
    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
    cursor.moveToFirst();

    return cursor.getString(column_index);
  }
  
}
