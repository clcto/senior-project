package edu.unh.cdj26.senior_project;

import android.app.*;
import android.widget.*;
import android.view.*;
import android.net.wifi.*;
import android.content.*;
import java.util.*;
import java.io.*;
import android.os.*;
import android.graphics.*;

public class IndoorLocalization extends Activity
{
   private BroadcastReceiver wifiRecv, orientationRecv;
   private BuildingMap map;
   
   private static List<AccessPoint> accessPoints;

   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      map = new BuildingMap( this );
      setContentView( map );

      accessPoints = Collections.synchronizedList(
         new ArrayList<AccessPoint>() );
      
      //DEBUG 
      AccessPoint ap;

      ap = new AccessPoint( 1616, 856, 1.9f, 35, 34.7f );
      ap.addMAC( "00:1F:45:48:3E:49" ).addMAC( "00:1F:45:48:3E:48" ).addMAC( "00:1F:45:48:3E:4B" );
      accessPoints.add( ap );

      ap = new AccessPoint( 465, 757, 1.9f, 35, 34.7f );
      ap.addMAC( "00:1A:E8:14:D2:AB" ).addMAC( "00:1A:E8:14:D2:A8" ).addMAC( "00:1A:E8:14:D2:A9" );
      accessPoints.add( ap );

      ap = new AccessPoint( 2708, 823, 1.9f, 35, 34.7f );
      ap.addMAC( "00:1F:45:48:38:3B" ).addMAC( "00:1F:45:48:38:3A" ).addMAC( "00:1F:45:48:38:39" );
      accessPoints.add( ap );

      ap = new AccessPoint( 780, 808, 4, 60, 34.7f );
      ap.addMAC( "00:1A:E8:14:B5:19" ).addMAC( "00:1A:E8:14:B5:1A" ).addMAC( "00:1A:E8:14:B5:18" );
      accessPoints.add( ap );

      ap = new AccessPoint( 1305, 253, 2, 50, 34.7f );
      ap.addMAC( "00:1A:E8:35:70:DB" ).addMAC( "00:1A:E8:35:70:D9" ).addMAC( "00:1A:E8:35:70:D8" );
      accessPoints.add( ap );

      ap = new AccessPoint( 1675, 1130, 2, 35, 34.7f );
      ap.addMAC( "00:1A:E8:35:8f:3b" ).addMAC( "00:1A:E8:35:8f:39" ).addMAC( "00:1A:E8:35:8f:38" );
      accessPoints.add( ap );

      ap = new AccessPoint( 30, 930, 1, 50, 32 );
      ap.addMAC( "00:01:F4:5B:78:36" );
      accessPoints.add( ap );

      ap = new AccessPoint( 2247, 966, 7, 60, 32 );
      ap.addMAC( "00:16:B6:32:29:12" );
      accessPoints.add( ap );

      ap = new AccessPoint( 2340, 634, 7, 60, 32 );
      ap.addMAC( "00:0C:41:12:3A:7D" );
      accessPoints.add( ap );

      Intent serviceIntent = new Intent( this, WifiService.class );
      startService( serviceIntent );

      serviceIntent = new Intent( this, DirectionSensor.class );
      startService( serviceIntent );
   }

   @Override
   public void onStart()
   {
      super.onStart();
      wifiRecv = new ServiceNotificationReceiver();
      registerReceiver( wifiRecv, new IntentFilter( "WIFI_DATA_PROCESSED" ) );
      
      orientationRecv = new OrientationReceiver();
      registerReceiver( orientationRecv, new IntentFilter( "COMPASS_DATA_PROCESSED" ) );

   }

   @Override
   public void onPause()
   {
      super.onPause();
      //unregisterReceiver( wifiRecv );
      //unregisterReceiver( orientationRecv );
   }

   @Override
   public void onStop()
   {
      super.onStop();
   }

   private class OrientationReceiver extends BroadcastReceiver
   {
      @Override
      public void onReceive( Context context, Intent intent )
      {
         map.newCompassData( intent.getFloatExtra( "orientation", 0 ) );
      }
   }

   float prev_x, prev_y, prev_r = 1;

   private class ServiceNotificationReceiver extends BroadcastReceiver
   {
      @Override
      public void onReceive( Context context, Intent intent )
      {
         float new_x = intent.getFloatExtra( "x", -1 );
         float new_y = intent.getFloatExtra( "y", -1 );
         float new_r = intent.getFloatExtra( "radius", -1 );
         
         double x = new_x - prev_x;
         x /= prev_r;

         double y = new_y - prev_y;
         y /= prev_r;

         double r = new_r / prev_r;

         double dist = Math.sqrt( x*x + y*y );
         if( r > dist - 1 && r < dist + 1 )
         {
            // intersect
            double u_int = ( dist*dist - r*r + 1 ) / ( 2*dist );
            double v_int = Math.sqrt( 1 - u_int*u_int );

            double theta = Math.atan2( y, x );
            double x_int = (u_int * Math.cos( theta ) + v_int * Math.sin( theta ));
            double y_int = (u_int * Math.sin( theta ) + v_int * Math.cos( theta ));

            u_int = ( 1 + ( dist - r ) ) / 2;

            x = u_int * Math.cos( theta );
            y = u_int * Math.sin( theta );

            r = Math.sqrt( (x-x_int)*(x-x_int) + (y-y_int)*(y-y_int) );

            x *= prev_r;
            x += prev_x;

            y *= prev_r;
            y += prev_y;

            r *= prev_r;

            map.newData( prev_x, prev_y, prev_r, new_x, new_y, new_r, x, y, r ); 

            prev_x = (float)x;
            prev_y = (float)y;
            prev_r = (float)r;

            
         }
         else
         {
            map.newWifiData( new_x, new_y, new_r );

            prev_x = new_x;
            prev_y = new_y;
            prev_r = new_r;
         }
      }
   }

   public static List<AccessPoint> getAPs()
   {
      return accessPoints;
   }
}
