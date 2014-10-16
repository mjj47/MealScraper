package com.mealscraper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.facebook.*;





public class MainActivity extends Activity {
	private boolean serviced = false;
	private Intent intent;

	    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        setContentView(R.layout.activity_main);
        if (!serviced) {
        	intent = new Intent(this, Scraper.class);
        	startService(intent);
        	serviced = true;
        }
        
        
        try {

        	   PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);

        	   for (Signature signature : info.signatures) 
        	   {
        	    MessageDigest md = MessageDigest.getInstance("SHA");
        	    md.update(signature.toByteArray());
        	    Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
        	   }

        	  } catch (NameNotFoundException e) {
        	   Log.e("name not found", e.toString());
        	  } catch (NoSuchAlgorithmException e) {
        	   Log.e("no such an algorithm", e.toString());
        	  }
    }

	    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }
    
//    private void pickUserAccount() {
//    	Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"},
//    	         false, null, null, null, null);
//        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
//    }
    
    @Override
    protected void onStart() {
        super.onStart();
        
        
//        pickUserAccount();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
    
    // Button handler    
    public void handleButton(View view) {
    	//stopService(service);
    	if (intent != null) {
    		stopService(intent);
    		intent = null;
    	} else {
    		intent = new Intent(this, Scraper.class);
    		startService(intent);
    	}
    	
    }


}
