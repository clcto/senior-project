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
   private static final int CAPTURE_FILENAME = 0;
   private BroadcastReceiver wifiRecv, orientationRecv;
   private static BuildingMap map;
   private static boolean running = true;

   private static final double WALK_SPEED = BuildingMap.metersToPixels( 1.4 * Math.pow( 10, -9 ) );

   private static boolean first_run = true;
   private static boolean first_data = true;

   private static PointF offset;

   private void setup()
   {
      if( first_run )
      {
         map = BuildingMap.instance();
         if( map == null );
         {
            BuildingMap.setContext( this );
            map = BuildingMap.instance();
         }

         accessPoints = Collections.synchronizedList(
            new ArrayList<AccessPoint>() );
         AccessPoint ap;

         ap = new AccessPoint( 1616, 856, 1.9f, 35, 34.5f );
         ap.addMAC( "00:1F:45:48:3E:49" ).addMAC( "00:1F:45:48:3E:48" ).addMAC( "00:1F:45:48:3E:4B" );
         accessPoints.add( ap );

         ap = new AccessPoint( 465, 757, 1.9f, 35, 34.5f );
         ap.addMAC( "00:1A:E8:14:D2:AB" ).addMAC( "00:1A:E8:14:D2:A8" ).addMAC( "00:1A:E8:14:D2:A9" );
         accessPoints.add( ap );

         ap = new AccessPoint( 2708, 823, 1.9f, 35, 34.5f );
         ap.addMAC( "00:1F:45:48:38:3B" ).addMAC( "00:1F:45:48:38:3A" ).addMAC( "00:1F:45:48:38:39" );
         accessPoints.add( ap );

         ap = new AccessPoint( 780, 808, 4, 60, 34.5f );
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

         WifiDataProcessor.numScans = 10;

         first_run = false;

         offset = new PointF( 0, 0 );

      }
   }
   
   private static List<AccessPoint> accessPoints;

   private Context context;

   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      setup();
      map.removeFromParent();
      map.reset();
      setContentView( map );


      context = this;
   }

   @Override
   public void onStart()
   {
      super.onStart();

   }

   @Override
   public void onResume()
   {
      super.onResume();
      if( running )
         resume();
   }

   @Override
   public void onPause()
   {
      super.onPause();
      if( running )
         pause();
   }

   @Override
   public void onStop()
   {
      super.onStop();
   }

   @Override
   public boolean onCreateOptionsMenu( Menu menu )
   {
      getMenuInflater().inflate( R.menu.map_menu, menu );
      return true;
   }

   @Override
   public boolean onOptionsItemSelected( MenuItem item )
   {
      switch( item.getItemId() )
      {
         case R.id.pause_resume:
            if( running )
            {
               item.setTitle( "Resume" );
               pause();
               running = false;
            }
            else
            {
               item.setTitle( "Pause" );
               resume();
               running = true;
            }
            break;
         case R.id.capture:
            if( running )
               pause();
            showDialog( CAPTURE_FILENAME, null );
            break; 
         case R.id.calibrate:
            first_data = true;
            WifiDataProcessor.numScans = 10;
            if( !running )
               resume(); 
            break;
         case R.id.aps:
            BuildingMap.showAPs = ! BuildingMap.showAPs;
            break;
         case R.id.wifi_locations:
            BuildingMap.showWifi = ! BuildingMap.showWifi;
            break;
         case R.id.movement_areas:
            BuildingMap.showRect = ! BuildingMap.showRect;
            break;
      }

      return true;
   }

   @Override
   public Dialog onCreateDialog( int id, Bundle args )
   {
      Dialog d = null;
      switch( id )
      {
         case CAPTURE_FILENAME:
            final Dialog filename_dialog = new Dialog( context );
            filename_dialog.setContentView( R.layout.capture_entry );
            filename_dialog.setTitle( "Capture Filename" );
            filename_dialog.setOnDismissListener( new DialogInterface.OnDismissListener()
               {
                  @Override
                  public void onDismiss( DialogInterface di )
                  {
                     if( running )
                        resume();
                  }
               } );

            Button ok = (Button) filename_dialog.findViewById( R.id.capture_entry_ok );
            Button cancel = (Button) filename_dialog.findViewById( R.id.capture_entry_cancel );
             
            ok.setOnClickListener( new View.OnClickListener()
               {
                  public void onClick( View v )
                  {
                     EditText input = (EditText) filename_dialog.findViewById( R.id.filename_input );
                     final String filename = ("" + input.getText()).trim();

                     dismissDialog( CAPTURE_FILENAME );

                     if( filename == null || filename.length() == 0 )
                     {
                        Toast.makeText( context, R.string.ERR_NO_FILENAME, Toast.LENGTH_SHORT ).show();
                        showDialog( CAPTURE_FILENAME, null );
                        return;
                     }
                     

                     final File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                     if( dir == null )
                     {
                        Toast.makeText( context, R.string.ERR_EXTERNAL_FAIL, Toast.LENGTH_SHORT ).show();
                        return;
                     }

                     Thread save_thread = new Thread( new Runnable()
                        {
                           @Override
                           public void run()
                           {
                              ProgressDialog saving = ProgressDialog.show( context, "", 
                                                "Saving. Pleas Wait", true);
                              try
                              {
                                 File final_dir = new File( dir.getPath() + "/IndoorLocalization/" ); 

                                 if( !final_dir.exists() )
                                    final_dir.mkdirs();


                                 File file = new File( final_dir.getPath() + "/" + filename );
                                 
                                 System.err.println( "here" );
                                 OutputStream out = new BufferedOutputStream( new FileOutputStream(file) );

                                 System.err.println( "here2" );
                                 if( !map.saveToFile( out ) )
                                    Toast.makeText( context, R.string.ERR_WRITE_FAIL, Toast.LENGTH_SHORT ).show();
                                 else
                                    Toast.makeText( context, R.string.WRITE_SUCCESS, Toast.LENGTH_SHORT ).show();
                                 saving.dismiss();

                              }
                              catch( IOException e )
                              {
                                 System.err.println( e.getMessage() );
                                 saving.dismiss();
                                 Toast.makeText( context, R.string.ERR_FILE_OPEN_FAIL, Toast.LENGTH_SHORT ).show();
                              }
                           }
                        } );
                     save_thread.run();
                  }
               } );

            cancel.setOnClickListener( new View.OnClickListener()
               {
                  public void onClick( View v )
                  {
                     dismissDialog( CAPTURE_FILENAME );
                  }
               } );

            d = filename_dialog;
            break;
      }

      return d;
      
   }
   
   private void pause()
   {
      Intent service = new Intent( this, WifiService.class );
      stopService( service );

      service = new Intent( this, DirectionSensor.class );
      stopService( service );

      if( wifiRecv != null )
         unregisterReceiver( wifiRecv );
      if( orientationRecv != null )
         unregisterReceiver( orientationRecv );

      wifiRecv = null;
      orientationRecv = null;
   }

   private void resume()
   {
      Intent service = new Intent( this, WifiService.class );
      startService( service );

      service = new Intent( this, DirectionSensor.class );
      startService( service );

      wifiRecv = new ServiceNotificationReceiver();
      registerReceiver( wifiRecv, new IntentFilter( "WIFI_DATA_PROCESSED" ) );
      
      orientationRecv = new OrientationReceiver();
      registerReceiver( orientationRecv, new IntentFilter( "COMPASS_DATA_PROCESSED" ) );
   }

   private class OrientationReceiver extends BroadcastReceiver
   {
      float prev_z = Float.MIN_VALUE;
      long prev_move_timestamp = 0;

      @Override
      public void onReceive( Context context, Intent intent )
      {
         float orientation = BuildingMap.toScreenAngle( intent.getFloatExtra( "orientation", 0 ) );
         
         long timestamp = intent.getLongExtra( "timestamp", -1 );
         float[] accel = intent.getFloatArrayExtra( "accelerometer" );
         if( accel == null ) return;

         long dt_ns = timestamp - prev_move_timestamp;
         System.err.println( Math.abs( accel[2] - prev_z ) );
         System.err.println( dt_ns );

         if( Math.abs( accel[2] - prev_z ) > 0.35 || dt_ns < 200000000 )
         {
               // show user which way the device is pointed and if he is moving
            map.newAccelerometerData( orientation, true );
            if( Math.abs( accel[2] - prev_z ) > 0.35 )
               prev_move_timestamp = timestamp;

            if( offset != null )
            {
               offset.x += dt_ns * WALK_SPEED * Math.sin( Math.toRadians( orientation ) );
               offset.y -= dt_ns * WALK_SPEED * Math.cos( Math.toRadians( orientation ) );
            }
         }
         else
            map.newAccelerometerData( orientation, false );

         prev_z = accel[2];
      }
   }

   private class ServiceNotificationReceiver extends BroadcastReceiver
   {
      float prev_x2, prev_y2, prev_r = 1;

      @Override
      public void onReceive( Context context, Intent intent )
      {
         float new_x = intent.getFloatExtra( "x", -1 );
         float new_y = intent.getFloatExtra( "y", -1 );
         float new_r = intent.getFloatExtra( "radius", 1 );
         if( first_data )
         {
            map.newWifiData( new_x, new_y, new_r );

            prev_x2 = new_x;
            prev_y2 = new_y;
            prev_r = new_r;
             
            first_data = false;
         }

         
         double prev_x = prev_x2 + offset.x;
         double prev_y = prev_y2 + offset.y;
         prev_r *= 1.1;

         double dx = new_x - prev_x;
         double dy = new_y - prev_y;

         double dist = Math.sqrt( dx*dx + dy*dy );
         if( dist > prev_r + new_r || dist < Math.abs( new_r - prev_r ) )
            // no intersect
         {
           /* 
            map.newWifiData( new_x, new_y, new_r );

            prev_x2 = new_x;
            prev_y2 = new_y;
            prev_r = new_r;
            */
         }
         else
         {
            double dist_1 = (prev_r + dist - new_r) / 2;

            double x = prev_x + dx * (dist_1/dist);
            double y = prev_y + dy * (dist_1/dist);

            // intersect
            double p_int = (Math.pow(dist,2) - Math.pow(new_r,2) + Math.pow(prev_r,2)) / ( 2*dist );
            double q_int = Math.sqrt( Math.pow(prev_r,2) - Math.pow(p_int,2) );

            double x_2 = prev_x + p_int * dx / dist;
            double y_2 = prev_y + p_int * dy / dist;

            double x_int = x_2 - q_int * ( dy / dist );
            double y_int = y_2 + q_int * ( dx / dist );

            dx = x_int - x;
            dy = y_int - y;

            double r = Math.sqrt( dx*dx + dy*dy );

            r = Math.max( r, BuildingMap.metersToPixels( 9 ) );

            map.newData( prev_x2, prev_y2, prev_r, new_x, new_y, new_r, x, y, r, offset );

            prev_x2 = (float)x;
            prev_y2 = (float)y;
            prev_r = (float)r;

            
         }

         offset.set( 0, 0 );
      }
   }

   public static List<AccessPoint> getAPs()
   {
      return accessPoints;
   }
}
