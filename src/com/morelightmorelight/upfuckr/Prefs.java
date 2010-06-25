package com.morelightmorelight.upfuckr;

import android.app.Activity;
import android.preference.PreferenceActivity;
import android.os.Bundle;

public class Prefs extends PreferenceActivity 
{ 

    @Override 
    public void onCreate(Bundle savedInstanceState) { 
      super.onCreate(savedInstanceState); 
      
      addPreferencesFromResource(R.xml.preferences); 
    }
}
