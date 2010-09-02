
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

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.galleries);
      prefs = PreferenceManager.getDefaultSharedPreferences(this);

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

}
