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

public class upfuckr extends Activity
{
  private static final int ADD_ID = Menu.FIRST;
  private static final int ACTIVITY_CREATE = 0;

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
          //I dunno, upload?
          upload();
        }
      });


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

  private void upload()
  {
    //get our shared preferences
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    String host = prefs.getString("host","");
    String path = prefs.getString("path","");
    String user = prefs.getString("user","");
    String pass = prefs.getString("pass","");

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
    intent.putExtra("local_file1", "/sdcard/subfolder1/file1.zip");
    intent.putExtra("local_file2", "/sdcard/subfolder2/file2.zip");
    // Optional initial remote folder (it must exist before upload)
    intent.putExtra("remote_folder", path);
    startActivityForResult(intent, 1);
  }
  
}
