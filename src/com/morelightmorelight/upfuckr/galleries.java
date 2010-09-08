
package com.morelightmorelight.upfuckr;

import android.app.Activity;
import android.os.Bundle;

import android.view.View;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.net.ftp.*;
import java.io.*;

import android.widget.ListView;
import android.widget.Adapter;

public class galleries extends Activity{

  private SharedPreferences prefs;
  private final String TAG = "galleries";

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.galleries);
      prefs = PreferenceManager.getDefaultSharedPreferences(this);

      //get our shared preferences
      String host = prefs.getString("host","");
      String path = prefs.getString("path","");
      String user = prefs.getString("user","");
      String pass = prefs.getString("pass","");
      Log.i(TAG,"about to ftp to " + host);

      FTPClient ftp = new FTPClient();
      try{
        ftp.connect(host);
        ftp.enterLocalPassiveMode();
        Log.i(TAG,"we connected");
        if(!ftp.login(user,pass)){
          ftp.logout();
          //TODO: alert user it didn't happen
        }
        String replyStatus = ftp.getStatus();
        Log.i(TAG,replyStatus);
        int replyCode = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(replyCode))
        {
          ftp.disconnect();
          //TODO: alert user it didn't happen
        }

        Log.i(TAG,"we logged in");
        ftp.changeWorkingDirectory(path);
        FTPFile[] files = ftp.listFiles();
        for(int i = 0; i < files.length; i++ ){
          FTPFile file = files[i];
          Log.i(TAG, file.getName());
        }
        ftp.disconnect();
      }
      catch(Exception ex){

      }

      

  }
  
  private class GalleryFile extends File{
    public GalleryFile(String path){
      super(path);
    }

    public String toString(){
      String sep = "|";
      String path = getPath();
      int depth = path.split(File.pathSeparator).length;
      return new String(new char[depth]).replace("\0", sep);

      

    }


  }

  private class GalleryData extends ArrayList <GalleryFile> {
    


    

  }
//merging http://commons.apache.org/net/api/org/apache/commons/net/ftp/FTPClient.html
//and http://vafer.org/blog/20071112204524
ftpClient = getConnectedFTPClient();


public class GalleryLister{
  public FTPClient mFtp;
  public ArrayList mAccumulator;
  private String sep = "|";
  private String prefix = "";
  public GalleryLister(FTPClient ftp, ArrayList accumulator){
    mFtp = ftp;
    mAccumulator = accumulator;
    
  }
  public final void traverse(FTPFile f) {
    if(f.isDirectory()){
      prefix.concat(sep);
      onDirectory(f);
      //change to the directory
      mFtp.changeWorkingDirectory(f.getName());
      final FTPFile[] children = mFtp.listFiles();
      for(FTPFile child : children){
        traverse(child);
      }
      //return to parent directory
      mFtp.changeToParentDirectory();
      prefix = prefix.substring(1);
    }
  }
  public void onDirectory(final FTPFile d){

  }
  public void onFile(final FTPFile f){
  }
}


}
