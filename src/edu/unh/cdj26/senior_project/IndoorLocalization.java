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
      map.setUpperLeftPixel( 0, 0 );

      accessPoints = Collections.synchronizedList(
         new ArrayList<AccessPoint>() );
      

      //DEBUG 
      AccessPoint ap;

      ap = new AccessPoint( 1616, 856, 1.9f, 35, 35 );
      ap.addMAC( "00:1F:45:48:3E:49" ).addMAC( "00:1F:45:48:3E:48" ).addMAC( "00:1F:45:48:3E:4B" );
      accessPoints.add( ap );

      ap = new AccessPoint( 465, 757, 1.9f, 35, 35 );
      ap.addMAC( "00:1A:E8:14:D2:AB" ).addMAC( "00:1A:E8:14:D2:A8" ).addMAC( "00:1A:E8:14:D2:A9" );
      accessPoints.add( ap );

      ap = new AccessPoint( 2708, 823, 1.9f, 35, 35 );
      ap.addMAC( "00:1F:45:48:38:3B" ).addMAC( "00:1F:45:48:38:3A" ).addMAC( "00:1F:45:48:38:39" );
      accessPoints.add( ap );


      ap = new AccessPoint( 30, 930, 1, 50, 32 );
      ap.addMAC( "00:01:F4:5B:78:36" );
      accessPoints.add( ap );


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
         float x = intent.getFloatExtra( "x", -1 );
         float y = intent.getFloatExtra( "y", -1 );
         float r = intent.getFloatExtra( "radius", -1 );
         System.out.println( "cdj26 -- r: " + r );
         //Toast.makeText( getApplicationContext(), "( " + x + ", " + y + " )", Toast.LENGTH_LONG ).show();
         map.newWifiData( x, y, r );
      }
   }

   public static List<AccessPoint> getAPs()
   {
      return accessPoints;
   }
}
