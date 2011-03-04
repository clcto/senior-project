package edu.unh.cdj26.senior_project;

import android.app.*;
import android.widget.*;
import android.view.*;
import android.net.wifi.*;
import android.content.*;
import java.util.*;
import java.io.*;
import android.os.*;

public class IndoorLocalization extends Activity
{
   private ServiceNotificationReceiver receiver;
   private BuildingMap map;
   
   private static List<AccessPoint> accessPoints;

   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      map = new BuildingMap( this );
      setContentView( map );
      map.setCenterPixel( 750, 250 );

      accessPoints = Collections.synchronizedList(
         new ArrayList<AccessPoint>() );
      

      //DEBUG 
      accessPoints.add( new AccessPoint( 179, 324, 1.9, "00:1A:E8:14:D2:AB" ) );
      accessPoints.add( new AccessPoint( 179, 324, 1.9, "00:1A:E8:14:D2:A8" ) );
      accessPoints.add( new AccessPoint( 179, 324, 1.9, "00:1A:E8:14:D2:A9" ) );

      accessPoints.add( new AccessPoint( 1521, 368.8, 1.9, "00:1F:45:48:38:3B" ) );
      accessPoints.add( new AccessPoint( 1521, 368.8, 1.9, "00:1F:45:48:38:3A" ) );
      accessPoints.add( new AccessPoint( 1521, 368.8, 1.9, "00:1F:45:48:38:39" ) );

      Intent serviceIntent = new Intent( this, WifiService.class );
      startService( serviceIntent );
   }

   @Override
   public void onStart()
   {
      super.onStart();
      receiver = new ServiceNotificationReceiver();
      registerReceiver( receiver, new IntentFilter( "WIFI_DATA_PROCESSED" ) );
   }

   @Override
   public void onPause()
   {
      super.onPause();
      unregisterReceiver( receiver );
   }

   @Override
   public void onStop()
   {
      super.onStop();
   }

   private class ServiceNotificationReceiver extends BroadcastReceiver
   {
      @Override
      public void onReceive( Context context, Intent intent )
      {
         map.newWifiData();
      }
   }

   public static List<AccessPoint> getAPs()
   {
      return accessPoints;
   }
}
