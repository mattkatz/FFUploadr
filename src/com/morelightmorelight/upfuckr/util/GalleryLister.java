package com.morelightmorelight.upfuckr.util;
import org.apache.commons.net.ftp.*;
import android.util.Log;
//merging http://commons.apache.org/net/api/org/apache/commons/net/ftp/FTPClient.html
//and http://vafer.org/blog/20071112204524
public class GalleryLister{
  private final String TAG = "GalleryLister";
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
    if(f.isDirectory){
      prefix = prefix.concat(sep);
      curPath = curPath + name + pathSep;

      onDirectory(f);
    }
    else{
      onFile(f);
    }
  }
  public void onDirectory(final GalleryFile d){
    String name = d.getName();
    Log.i(TAG, "Changing wd to " + name);
    //change to the directory
    try{
      mFtp.changeWorkingDirectory(name);
      final FTPFile[] children = mFtp.listFiles();
      for(FTPFile child : children){
        String childName = child.getName();
        if (childName.equals("thumb") || childName.equals("info.yml") || childName.equals("web")){
          continue;
        }
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
