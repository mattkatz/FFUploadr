package com.morelightmorelight.upfuckr.util;
import org.apache.commons.net.ftp.*;
import android.util.Log;
import java.io.*;
public class GalleryFile extends File implements Serializable {
  public GalleryData children;
  public boolean isDirectory;
  private final String TAG = "GalleryFile";
  public GalleryFile(FTPFile f){
    this( f.getName());
    this.isDirectory = f.isDirectory();
  }
  public GalleryFile(String path){
    super(path);
    children = new GalleryData();
  }
  public GalleryFile(){
    super(".");
    this.isDirectory = false;
    this.children = new GalleryData();
    Log.i(TAG,"In default constructor");
  }

  public GalleryData folders(){
    int limit = children.size();
    GalleryData ret = new GalleryData();
    for (int i = 0; i < limit; i ++){
      GalleryFile f = children.get(i);
      if(f.isDirectory){
        ret.add(f);
      }
    }
    return ret;
  }
  public String toString(){
    //return getPath();
    String sep = "|";
    String path = getPath();
    int depth = path.split("/").length;
    return new String(new char[depth]).replace("\0", sep)+getName();
  }
}
