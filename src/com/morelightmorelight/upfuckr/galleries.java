
package com.morelightmorelight.upfuckr;

import android.app.ListActivity;
import android.os.Bundle;

import android.view.View;
import android.content.Intent;
import android.content.Context;
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
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;


import java.lang.reflect.Type;
import com.google.gson.*;

import com.google.gson.reflect.TypeToken;

import com.morelightmorelight.upfuckr.util.*;

public class galleries extends ListActivity{

  private SharedPreferences prefs;
  private final String TAG = "galleries";
  private final String GALLERIES = "galleries_json";
  private GalleryFile gr = null;
  private GalleryAdapter ga = null;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.galleries);
      prefs = PreferenceManager.getDefaultSharedPreferences(this);
      
      //if we don't have the gallery root folder, get it
      if(null == gr){
        //gr = new GalleryFile();
        gr = getGalleryList();
      }
      //now let's prove that we have deserialized the gr
      this.ga = new GalleryAdapter(this, R.layout.gallery_row, gr);
      this.setListAdapter( this.ga);
      
      
  }
  //Serializes the root folder to json and stores it as a string in the prefs
  public void storeGalleryList(GalleryFile root){
    Gson gson = new Gson();
    String json = gson.toJson(root);
    //stuff away the json of the directories
    Editor editor = prefs.edit();
    editor.putString(GALLERIES, json);
    Log.i(TAG, json);
    editor.commit();
  }

  //gets the gallery list from the server
  //then caches it
  public GalleryFile refreshGalleryList(){
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
        GalleryLister gl = new GalleryLister(ftp);
        FTPFile root = new FTPFile();
        root.setName(path);
        root.setType(FTPFile.DIRECTORY_TYPE);
        GalleryFile galleryRoot = new GalleryFile(root);
        Log.i(TAG, root.getName());
        gl.traverse(galleryRoot);
        ftp.disconnect();
        storeGalleryList(galleryRoot);
        //Log.i(TAG, "serialized galleries");
        //Log.i(TAG, "Proving it");
        //Log.i(TAG, prefs.getString(GALLERIES,""));
        //Log.i(TAG, "proved it");
        return galleryRoot;
      }
      catch(Exception ex){
        //TODO: handle handle handle
      }
      //TODO: handle this better
      return null;

  }

  public GalleryFile getGalleryList(){
      //do we have a cached list of galleries?
      String grSerial = "";
      if(null == gr){
        grSerial = prefs.getString(GALLERIES, "");
        if( grSerial.equals("")){
          //nope - time to ftp out and get them
          //store the root for later
          gr = refreshGalleryList();
        }
        else
        {
          //great - we can deserialize
          Gson gson = new GsonBuilder()
            .registerTypeAdapter(GalleryFile.class, new GalleryFileDeserializer())
            .registerTypeAdapter(GalleryData.class, new GalleryDataDeserializer())
            .create();
          
          try{
            gr = gson.fromJson(grSerial, new TypeToken<GalleryFile>(){}.getType());
          }
          catch(Exception e){
            e.printStackTrace();
            //seems we can't deserialize what we've got.
            //get it back from the server
            gr = refreshGalleryList();
          }
          finally{
          }
        }
      }
      Log.i(TAG, "Deserialized a gallery file with size: " + gr.children.size());
      return gr;
  }
  
public class GalleryFileDeserializer implements JsonDeserializer<GalleryFile> {
  public GalleryFile deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) 
      throws JsonParseException {
    JsonObject job = json.getAsJsonObject();
    String path = job.getAsJsonPrimitive("path").getAsString();
    Boolean isDirectory = job.getAsJsonPrimitive("isDirectory").getAsBoolean();
    GalleryData children = new GalleryData();
    if(job.has("children")){ 
      //presumably they will be infected with boils ha ha just a little bible humor there.
      children = context.deserialize(job.get("children"), new TypeToken<GalleryData>(){}.getType()); 
    }
    GalleryFile gf = new GalleryFile(path);
    gf.isDirectory = isDirectory;
    gf.children = children;
    return gf;
  
  }

}

public class GalleryDataDeserializer implements JsonDeserializer<GalleryData>{
  public GalleryData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    JsonArray ja = json.getAsJsonArray();
    GalleryData da = new GalleryData();
    for(int i = 0; i < ja.size(); i++){
      JsonElement je = ja.get(i);
      GalleryFile f = context.deserialize(je, new TypeToken<GalleryFile>(){}.getType());
      da.add(f);
    }

    return da;

  }

}

public class GalleryAdapter extends ArrayAdapter<GalleryFile>
{
  private GalleryFile current;
  public GalleryAdapter(Context context, int textViewResourceId, GalleryFile gf){
    super(context, textViewResourceId, gf.children);
    this.current = gf;
    
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent){
    View v = convertView;
    if (v == null) {
      LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      v = vi.inflate(R.layout.gallery_row, null);
    }
    GalleryFile f = current.children.get(position);
    if(f !=null){
      TextView tt = (TextView) v.findViewById(R.id.toptext);
      TextView bt = (TextView) v.findViewById(R.id.bottomtext);
      if(tt != null){
        tt.setText(f.getName());
      }
      if(bt != null){
        bt.setText(f.children.size() + " subfolders");
      }
    }
    return v;
  }

}



}

