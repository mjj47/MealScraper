package com.mealscraper;

//import java.io.IOException;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphObject;

import android.app.Service;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
//import android.os.AsyncTask;
import android.os.IBinder;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.util.Log;



public class Scraper extends Service {
	private boolean scheduled = false;
	static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
	
	protected static final int REQUEST_AUTHORIZATION = 1002;
	private static final int MAX_STATUSES_RETURNED_FROM_JASON = 4;
	
	//	Controls how often to check the facebook statuses		
	private static final int TIME_DIF = 1;
	private static final TimeUnit TIME_UNIT = TimeUnit.MINUTES;
	

	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d("Service", "onStartCommand");
		if (!scheduled) {
        	testSchedule();
        	scheduled = true;
        }
		return START_STICKY;
	}
	
	public void doFacebook() {
		
		// start Facebook Login
        Session.openActiveSession(this, null, true, new Session.StatusCallback() {
        	// callback when session changes state
        	@Override
        	public void call(Session session, SessionState state, Exception exception) {
        		
        		if (session.isOpened()) {
        			Log.d("Response - Facebook", "Here");
        			// make request to the /me API
        			Request.newGraphPathRequest(session, "282814411746515/statuses", new Request.Callback() {
        				
        				// callback after Graph API response with user object
        				@Override
        				public void onCompleted(Response response) {
        					if (response == null) {
        						return;
        					}
        					GraphObject graphResponse1 = response.getGraphObject();
        					if (graphResponse1 == null) {
        						return;
        					}
        					JSONObject graphResponse = graphResponse1.getInnerJSONObject();
        					JSONArray jArray = null;
        					try {
        						jArray = graphResponse.getJSONArray("data");
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
        					int max = MAX_STATUSES_RETURNED_FROM_JASON;
        					if (max > jArray.length()){
        						max = jArray.length();
        					}
        					for(int i = 0; i < max; i++) {
        						Object resp = null;
        						try {
        							resp = jArray.get(i);
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
        						String message = ((JSONObject) resp).optString("message");
        						String time = ((JSONObject) resp).optString("updated_time");
        						Log.d("JSON parse", message + " ::--::" + time);
        						Meal m = new Meal(message, time);
        						addEventToCal(m);
        					}
        					//Log.d("Response - Facebook", "Repon:" + graphResponse.toString());
//        					TextView welcome = (TextView) findViewById(R.id.welcome);
//        					welcome.setText("Hello you!");
        				}
        			}).executeAsync();
        		} else {
        			Log.d("Response - Facebook", "session closed");
        		}
        	}
        });
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
//	private void pickUserAccount() {
//    	Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"},
//    	         false, null, null, null, null);
//        startActivity(intent);
//    }
	
    private boolean checkExists(Meal m) {
    	Uri.Builder eventsUriBuilder = CalendarContract.Instances.CONTENT_URI
                .buildUpon();
    	
	    ContentUris.appendId(eventsUriBuilder, m.start - 1);
	    ContentUris.appendId(eventsUriBuilder, m.end + 1);
	    Uri eventsUri = eventsUriBuilder.build();
	    Cursor cursor = null;       
	    cursor = this.getContentResolver().query(eventsUri, null, null, null, CalendarContract.Instances.DTSTART + " ASC");
	    
		if (cursor.moveToFirst()) {
			Log.d("woot", "" + cursor.getColumnCount());
		   do {
			   int titleIndex = cursor.getColumnIndex("title");
			   int descrIndex = cursor.getColumnIndex("description");
			   String title = cursor.getString(titleIndex);
			   String descr = cursor.getString(descrIndex);
			   if (title.equals(m.mealType) && descr.equals(m.mealDescription)) {
				   Log.i("Meal Check", "already exists");
				   cursor.close();
				   return true;
			   }		      
		      
		   } while (cursor.moveToNext());
		}
		cursor.close();
    	return false;
    }
    
    protected void addEventToCal(Meal m) {
    	Log.d("Meal Check", "Checking Meal" + m.mealDescription);
    	if (!checkExists(m)) {
    		Log.i("Meal Check", "Meal Not Found... Adding to calendar");
	    	ContentValues event = new ContentValues();
	        event.put("calendar_id", 1);
	
	        event.put("title", m.mealType);
	
			// Setting dates
			
				
	        event.put("dtstart", m.start);
	        event.put("dtend", m.end);
	        event.put("allDay", 0);   // 0 for false, 1 for true
	        event.put("hasAlarm", 0); // 0 for false, 1 for true
	        event.put(Events.DESCRIPTION, m.mealDescription);
	
	        String timeZone = TimeZone.getDefault().getID();
	        event.put("eventTimezone", timeZone);
	
	        Uri baseUri;
	        
	        baseUri = Uri.parse("content://com.android.calendar/events");
	        this.getContentResolver().insert(baseUri, event);
    	}
        
    }
    


    public void testSchedule() {
	    ScheduledExecutorService scheduler =
	    	    Executors.newSingleThreadScheduledExecutor();
	
	    	scheduler.scheduleAtFixedRate
	    	      (new Runnable() {
	    	         public void run() {
	    	            Log.d("Scheduler",	"awake");
    	            	doFacebook();
	    	         }
	    	      }, 0, TIME_DIF, TIME_UNIT);
    }

	public void fetchToken(final String email) {
//		AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
//			@Override
//            protected String doInBackground(Void... params) {
//				String token = null;
//				 try {
//					 Log.d("woo", "authing");
//					 String scopesString = Scopes.PLUS_LOGIN;
//					 String scopes = "oauth2:server:client_id:822890253500-nrkc0si7c0t6kd7smmd74putrua28d97.apps.googleusercontent.com:api_scope:" + scopesString;
//					 
//			         token = GoogleAuthUtil.getToken(MainActivity.this, email, scopes);
//			         Log.d("woo", token);
//			         
//			     } catch (UserRecoverableAuthException userRecoverableException) {
//			         // GooglePlayServices.apk is either old, disabled, or not present
//			         // so we need to show the user some UI in the activity to recover.
//			         Log.d("fetch", "recoverable");
//			         MainActivity.this.startActivityForResult(userRecoverableException.getIntent(), REQUEST_AUTHORIZATION);
//			     } catch (GoogleAuthException fatalException) {
//			         // Some other type of unrecoverable exception has occurred.
//			         // Report and log the error as appropriate for your app.
//			    	 Log.d("fetch", fatalException.getLocalizedMessage());
//			     } catch (IOException e) {
//					// TODO Auto-generated catch block
//			    	 Log.d("l", "l");
//					e.printStackTrace();
//				} catch (Exception e) {
//					Log.d("Other", e.getMessage());
//				}
//				 return token;
//			}
//			@Override
//            protected void onPostExecute(String token) {
//                Log.i("ASYNC", "Access token retrieved:" + token);
//                
//            }
//		};
//		task.execute();
//		
//		
////		try {
////			String token = task.get();
////			Log.i("Token", "recieved token: " + token);
////		} catch (InterruptedException e) {
////			// TODO Auto-generated catch block
////			e.printStackTrace();
////		} catch (ExecutionException e) {
////			// TODO Auto-generated catch block
////			e.printStackTrace();
////		}
//		
	}

}
