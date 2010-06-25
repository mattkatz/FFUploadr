package com.morelightmorelight.upfuckr;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;

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
  
}
