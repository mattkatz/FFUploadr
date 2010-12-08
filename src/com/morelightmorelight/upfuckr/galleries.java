
package com.morelightmorelight.upfuckr;

import android.app.Activity;
import android.os.Bundle;

import android.view.View;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.net.ftp.*;
import java.io.*;

import android.widget.ListView;
import android.widget.Adapter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.morelightmorelight.upfuckr.util.ObjectSerializer;

public class galleries extends Activity{

  private SharedPreferences prefs;
  private final String TAG = "galleries";
  private final String GALLERIES = "galleries_json";
  private ArrayList<GalleryFile> ga = null;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.galleries);
      prefs = PreferenceManager.getDefaultSharedPreferences(this);
      //do we have a cached list of galleries?
      if(null == ga){
        ga = new ArrayList<GalleryFile>();
        Gson gson = new Gson();
        try{
          ga = gson.fromJson(prefs.getString(GALLERIES, gson.toJson(ga)), new TypeToken<ArrayList<GalleryFile>>(){}.getType());
        }
        catch(Exception e){
          e.printStackTrace();
        }
      }
      //try {
        //ga = (ArrayList<GalleryFile>) ObjectSerializer.deserialize(prefs.getString(GALLERIES, ObjectSerializer.serialize(new ArrayList<GalleryFile>())));
      //} catch (IOException e) {
        //e.printStackTrace();
      //} 
      
      if(0 == ga.size()){
        ga = new ArrayList<GalleryFile>();
      }
      else{
        Log.i(TAG, "deserialized galleries");
        for(int i = 0; i< ga.size(); i++){
          GalleryFile file = ga.get(i);
          Log.i(TAG, file.toString());
        }
        return;
      }

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
        //ArrayList<GalleryFile> accumulator = new ArrayList<GalleryFile>();
        GalleryLister gl = new GalleryLister(ftp);
        FTPFile root = new FTPFile();
        root.setName(path);
        root.setType(FTPFile.DIRECTORY_TYPE);
        GalleryFile galleryRoot = new GalleryFile(root);
        Log.i(TAG, root.getName());
        gl.traverse(galleryRoot);
        //gl.traverse(root);
        //traversed!
        ftp.disconnect();
        Gson gson = new Gson();
        String json = gson.toJson(galleryRoot);
        //stuff away the json of the directories
        Editor editor = prefs.edit();
        editor.putString(GALLERIES, json);
        Log.i(TAG, json);
        editor.commit();
        Log.i(TAG, "serialized galleries");

      }
      catch(Exception ex){
        //TODO: handle handle handle
      }
  }
  
  private class GalleryFile extends File implements Serializable {
    public GalleryFile(FTPFile f){
      this( f.getName());
      this.isDirectory = f.isDirectory();
    }
    public GalleryFile(String path){
      super(path);
      children = new GalleryData();
    }
    public String toString(){
      //return getPath();
      String sep = "|";
      String path = getPath();
      int depth = path.split("/").length;
      return new String(new char[depth]).replace("\0", sep)+getName();
    }
    public GalleryData children;
    public boolean isDirectory;
  }

  private class GalleryData extends ArrayList <GalleryFile> {
    


    

  }
//merging http://commons.apache.org/net/api/org/apache/commons/net/ftp/FTPClient.html
//and http://vafer.org/blog/20071112204524


public class GalleryLister{
  public FTPClient mFtp;
  private String sep = "|";
  private String prefix = "";
  private String curPath = "/";
  private String pathSep = "/";
  public GalleryLister(FTPClient ftp ){
    mFtp = ftp;
  }
  public final GalleryFile traverse(){
    FTPFile root = new FTPFile();
    root.setName(".");
    root.setType(FTPFile.DIRECTORY_TYPE);
    GalleryFile galleryRoot = new GalleryFile(root);
    traverse(galleryRoot);
    return galleryRoot;

  }
  public final void traverse(GalleryFile f) {
    //we don't need thumb or web directories
    String name = f.getName();
    if (name.equals("thumb") || name.equals("web")){
      return;
    }
    if(f.isDirectory){
      prefix = prefix.concat(sep);
      curPath = curPath + name + pathSep;

      onDirectory(f);
    }
    onFile(f);
  }
  public void onDirectory(final GalleryFile d){
    String name = d.getName();
    Log.i(TAG, "Changing wd to " + name);
    //change to the directory
    try{
      mFtp.changeWorkingDirectory(name);
      final FTPFile[] children = mFtp.listFiles();
      for(FTPFile child : children){
        GalleryFile galleryChild = new GalleryFile(child);
        Log.i(TAG, "adding " + galleryChild.getName());
        d.children.add(galleryChild );
        Log.i(TAG,"child list now " + d.children.size());

        traverse(galleryChild);
      }

    }
    catch(Exception ex){
      //TODO handle the exceptions properly
      Log.i(TAG,ex.toString());

    }
    //return to parent directory
    try{
      Log.i(TAG, "Changing up one level");
      mFtp.changeToParentDirectory();
    }
    catch(Exception ex){
      //TODO handle the exceptions properly
    }
  }
  public void onFile(final GalleryFile f){
  }
}


}
