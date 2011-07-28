
package com.morelightmorelight.upfuckr;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.AsyncTask;
import android.net.Uri;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import android.view.Menu;
import android.view.MenuItem;

import android.app.ProgressDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.app.AlertDialog;
import android.content.DialogInterface;

import java.lang.reflect.Type;
import com.google.gson.*;

import com.google.gson.reflect.TypeToken;

import com.morelightmorelight.upfuckr.util.*;

public class galleries extends ListActivity{

  private SharedPreferences prefs;
  private final String TAG = "galleries";
  private final String GALLERIES = "galleries_json";
  private final int REFRESH_GALLERIES = Menu.FIRST;
  private final int CREATE_FOLDER = Menu.FIRST+1;
  private final static int ID_WAIT = 1;
  //This is the data root of your fuckflickr installation
  private GalleryFile gr = null;
  private GalleryFile currentGallery = null;
  private GalleryAdapter ga = null;
  private GalleryAdapter breadcrumb = null;
  private Spinner gallerySpinner = null;
  private ProgressDialog progress = null;
  public static final int IMAGE_PICK = 1;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.galleries);
      prefs = PreferenceManager.getDefaultSharedPreferences(this);
  }
  /**
   * Gets called when we start
   * 
   * @return void
   */
  public void onStart() {
    super.onStart();
    prepareGalleryUI();
  }
  /**
   * When we resume back in
   * 
   * @return void
   */
  public void onResume() {
    super.onResume();
    //we don't want to prepareGalleryUI here, it's too heavy. 
    //plus it gets called twice that way!
    checkIntent();

  }
  /**
   * Checks the intent and then calls sets appropriate selection callback
   * 
   * @return void
   */
  private void checkIntent() {

      Intent i = getIntent();
      String action = i.getAction();
      if(null != action){
        Log.i(TAG,action);
      }
      else{
        Log.i(TAG,"no action?!");
      }
      

      ArrayList l = null;
      String type = i.getType();
      Button uploadDone = (Button) findViewById(R.id.done_upload_btn);
      if (Intent.ACTION_SEND_MULTIPLE.equals(action) 
          || Intent.ACTION_SEND.equals(action))
      {
        //set the correct callback
        Log.i(TAG, "setting callback");
        uploadDone.setOnClickListener(new View.OnClickListener(){
          public void onClick(View view){
            setResult(RESULT_OK);
            //UPLOAD ALL THE IMAGES YOU GOT
            Log.i(TAG,"I SHOULD BE UPLOADING IMAGES!");
            Log.i(TAG,"The current gallery is " + currentGallery.getPath());
            uploadWithPath(null);
          }
        });
      }
      else{
        uploadDone.setOnClickListener(new View.OnClickListener(){
          public void onClick(View view){
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, IMAGE_PICK);
          }
        });
      }
  }
  protected void onActivityResult(int requestCode, int resultCode, Intent i){
    Log.i(TAG,"Request Code: " + requestCode);
    Log.i(TAG,"Result Code: " + resultCode);
    switch(requestCode){
      case IMAGE_PICK:
        if (resultCode == RESULT_OK){
          Uri stream = (Uri) i.getData();
          if ( stream != null )
          {
            uploadWithPath(i);
          }
          else { Log.i(TAG,"null URI");}
        }
    }
  }
  protected void uploadWithPath(Intent passed){
    Intent intent = new Intent(this, uploadr.class);
    
    Intent i = passed;
    if(null == i)
    {
     i = getIntent();
    }
    intent.fillIn(i, Intent.FILL_IN_ACTION);
    String action = i.getAction();
    //intent.setAction(action);
    if (Intent.ACTION_SEND_MULTIPLE.equals(action))
    {
      Log.v(TAG, "we have action send multiple!");
      ArrayList l = i.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
      intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,l);
    }
    else if (Intent.ACTION_SEND.equals(action)){
    
      Log.v(TAG, "we have action send!");
      Uri stream =  i.getParcelableExtra(Intent.EXTRA_STREAM);
      intent.putExtra(Intent.EXTRA_STREAM,stream);
    }
    else{
      Log.v(TAG, "Action: " + action);
      Uri stream = i.getParcelableExtra(Intent.EXTRA_STREAM);
      if( null != stream){
        Log.v(TAG, "non null stream!");
        intent.putExtra(Intent.EXTRA_STREAM, stream);
      }
      else{
        Log.v(TAG, "null stream, getting an arraylist");
        ArrayList l = i.getParcelableArrayListExtra(Intent.EXTRA_STREAM);

        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, l);
      }
    }


    //hack to get around concatenating the root of the path with the path stored in memory to the upfuckr root.
    intent.putExtra("PATH_EXTENSION",currentGallery.getPath().substring(2));
    startActivity(intent); 
    this.finish();
  }
  /**
   * Begins async gallery refresh
   * 
   * @return void
   */
  public void prepareGalleryUI() {
    Runnable showGalleries = new Runnable(){
      @Override 
      public void run() {
        setUpList();
      }
    };
    new Thread(showGalleries).start();
    progress = ProgressDialog.show(this, "Just a moment", "Getting galleries", true);
  }
  /**
   * Set up options menus
   */
  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    super.onCreateOptionsMenu(menu);
    menu.add(0,REFRESH_GALLERIES,0,R.string.refresh_galleries);
    menu.add(0,CREATE_FOLDER,1,R.string.create_folders);
    return true;
  }

  @Override
  public boolean onMenuItemSelected(int featureId, MenuItem item)
  {
    switch(item.getItemId()){
      case REFRESH_GALLERIES:
        Editor editor = prefs.edit();
        editor.remove(GALLERIES);
        editor.commit();
        gr = null;
        prepareGalleryUI();
        return true;
      case CREATE_FOLDER:
        //create a folder 
        create_folder();
        return true;
    }
    return super.onMenuItemSelected(featureId,item);
  }
  /**
   * create a folder async
   * @return void
   */
  public void create_folder() {
    Log.i(TAG,"time to create the folder");
    //create aa popup dialog
    AlertDialog.Builder alert = new AlertDialog.Builder(this);
    alert.setTitle("Create Folder");
    alert.setMessage("Pick a name");
    // Set an EditText view to get user input 
    final EditText input = new EditText(this);
    alert.setView(input);
    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) {
        //take the folder name from the dialog return
        final String value = input.getText().toString();
        showDialog(ID_WAIT);
        new CreateFolderTask().execute(value);
       /* 
        new Thread(new Runnable(){
          public void run(){
            //async create the folder
            FTPClient ftp = getConnected();
            try{
              ftp.changeWorkingDirectory(currentGallery.getPath());
              ftp.makeDirectory(value);
              ftp.disconnect();

              //add it to the cached folder list
              GalleryFile newGuy = new GalleryFile(value);
              newGuy.isDirectory=true;
              currentGallery.children.add(newGuy);
              this.post(new Runnable(){
                public void run(){
                  displayGallery(currentGallery);
                }
              });
            }
            catch(Exception ex){
              ex.printStackTrace();
            }
            dismissDialog(galleries.ID_WAIT);
          }
        }).start();
        */
        Log.i(TAG, "new name is :" + value);
      }
    });

    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) {
        // Canceled.
      }
    });

    alert.show(); 
  }
   /**
    * an override method to maintain dialogs during orientation shift
    * creates the dialog based on ID
    * @return void
    */
   protected  Dialog  onCreateDialog(int id) {
     switch(id){
       case ID_WAIT:
         ProgressDialog waitDialog = new ProgressDialog(this);
         waitDialog.setMessage("Creating your new folder");
         waitDialog.setIndeterminate(true);
         waitDialog.setCancelable(false);
         return waitDialog;

       default:
         Log.v(TAG,"trying to create a weird dialog: " + id);
         break;
         
     }
     return super.onCreateDialog(id);
   }
  private class CreateFolderTask extends AsyncTask<String, String,String>{
    @Override
    protected String doInBackground(String... names){
      String value = names[0];
      FTPClient ftp = getConnected();
      try{
        ftp.changeWorkingDirectory(currentGallery.getPath());
        ftp.makeDirectory(value);
        ftp.disconnect();

        //add it to the cached folder list
        GalleryFile newGuy = new GalleryFile(value);
        newGuy.isDirectory=true;
        currentGallery.children.add(newGuy);
        //storeGalleryList(gr);
      }
      catch(Exception ex){
        ex.printStackTrace();
      }
      return "Created " + value;

    } 

   protected void onPostExecute(String message){
     dismissDialog(ID_WAIT);
     displayGallery(currentGallery);


   }

  }




  
  /**
   * Set up the gallery adapter, bind it to the layout
   * 
   * @return void
   */
  public void setUpList() {
    
    if(null == gr){
      gr = getGalleryList();
    }
    ga = new GalleryAdapter(this, R.layout.gallery_row, new GalleryData());
    
    breadcrumb = new GalleryAdapter(this, android.R.layout.simple_spinner_dropdown_item,new GalleryData() );
    //breadcrumb = new GalleryAdapter(this, R.layout.gallery_row,new GalleryData() );
    runOnUiThread(returnRes);
  }
  /** Show the gallery designated */
  public void displayGallery(GalleryFile gallery){
    //if(gallery == currentGallery){
      //return;
    //}
    Log.i(TAG,"displaying " +gallery.getPath());
    
    
    ga.clear();
    for(int i=0; i < gallery.children.size(); i++){
      ga.add(gallery.children.get(i));
    }
    ga.notifyDataSetChanged();
    int index = breadcrumb.getPosition(gallery);
    Log.i(TAG, "index is " + index);
    if(index >= 0){
      for(int i = breadcrumb.getCount() - 1;  i > index; i--){
        GalleryFile g = breadcrumb.getItem(i);
        Log.i(TAG, g.getName());
        breadcrumb.remove(breadcrumb.getItem(i));
      }
    }
    else{
      breadcrumb.add(gallery);
    }
    currentGallery = gallery;
    gallerySpinner.setSelection(breadcrumb.getCount() -1);
  }

  private Runnable returnRes = new Runnable(){
    public void run() {
      setListAdapter(ga);
      gallerySpinner = (Spinner) findViewById(R.id.current_gallery);
      breadcrumb.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      gallerySpinner.setAdapter(breadcrumb);
      gallerySpinner.setOnItemSelectedListener(new breadcrumbSelection());
      progress.dismiss();
      displayGallery(gr);
    }

  };
  /**
   * handles clicks for the spinner
   */
  private class breadcrumbSelection implements OnItemSelectedListener {
    public void onItemSelected(AdapterView<?> parent,
                View view, int pos, long id) {
      GalleryFile selected = (GalleryFile) parent.getItemAtPosition(pos);
      displayGallery(selected);
    }
    public void onNothingSelected(AdapterView parent) {
            // Do nothing.
    }
  }

  private class UpdateGalleryListTask extends AsyncTask<String, String, String>{
    @Override
    protected String doInBackground(String... input){
      return "done";
      

    }

    protected void onProgressUpdate(String... messages){
      progress.setMessage(messages[0]);

    }

    protected void onPostExecute(String message){
    }



  }

  /** gets called when anything in the list gets clicked */
  @Override
  public void onListItemClick(ListView I, View v, int position, long id){
    //display the folder
    displayGallery(ga.getItem(position));
  }

  


  /**Serializes the root folder to json and stores it as a string in the prefs*/
  public void storeGalleryList(GalleryFile root){
    Gson gson = new Gson();
    String json = gson.toJson(root);
    //stuff away the json of the directories
    Editor editor = prefs.edit();
    editor.putString(GALLERIES, json);
    Log.i(TAG, json);
    editor.commit();
  }
  /**
   * Get a fully connected FTPClient
   * 
   * @return FTPClient
   */
  public FTPClient getConnected() {
      //get our shared preferences
      String host = prefs.getString("host","");
      String path = prefs.getString("path","");
      String user = prefs.getString("user","");
      String pass = prefs.getString("pass","");
      Log.i(TAG,"about to ftp to " + host);
      FTPClient ftp = new FTPClient();
      try{
        ftp.connect(host);
        Log.i(TAG,"we connected");
        ftp.enterLocalPassiveMode();
        Log.v(TAG,"passive!");
        if(!ftp.login(user,pass)){
          ftp.logout();
          //TODO: alert user it didn't happen
          Log.v(TAG, "couldn't log in");
          return null;
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

        
        //now to walk the directory tree
        if(ftp.changeWorkingDirectory(path)){
          Log.i(TAG, "changed to " + path);
        }
      }
      catch(Exception ex){
        //TODO: handle handle handle
            ex.printStackTrace();
      }
      finally{
        return ftp;

      }
    
  }

  /**gets the gallery list from the server then caches it*/
  public GalleryFile refreshGalleryList(){

    FTPClient ftp = getConnected();
    GalleryLister gl = new GalleryLister(ftp);
    GalleryFile galleryRoot = new GalleryFile();
    galleryRoot.isDirectory = true;
    gl.traverse(galleryRoot);
    try{
      ftp.disconnect();
    }
    catch(Exception ex){
      ex.printStackTrace();
    }
    storeGalleryList(galleryRoot);
    return galleryRoot;

  }

  /** Attempts to deserialize gallery list or update from server */
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
  private GalleryData current;
  public GalleryAdapter(Context context, int textViewResourceId, GalleryFile gf){
    super(context, textViewResourceId, gf.children);
    this.current = gf.children;
    
  }
  public GalleryAdapter(Context context, int textViewResourceId, GalleryData gd){
    super(context, textViewResourceId, gd);
    this.current = gd;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent){
    View v = convertView;
    if (v == null) {
      LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      v = vi.inflate(R.layout.gallery_row, null);
    }
    //GalleryFile f = current.get(position);
    GalleryFile f = getItem(position);
    if(f !=null){
      TextView tt = (TextView) v.findViewById(R.id.longname);
      TextView bt = (TextView) v.findViewById(R.id.subfolders);
      if(tt != null){
        tt.setText(f.getName());
      }
      if(bt != null && f.children.size() > 0){
        int folders = f.folders().size();
        int pics = f.children.size() - folders;
        String mesg = "";

        if(folders > 0){
          mesg = folders + " folders";
          if (pics > 0) {
            mesg += ", ";
          }
        }
        if(pics > 0){
          mesg += pics + " pics";
        }
        bt.setText(mesg);
      }
    }
    return v;
  }

}



}

