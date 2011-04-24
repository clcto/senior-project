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
   private FileWriter accel_data;

   private static boolean first_run = true;

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

         first_run = false;
         System.err.println( "first run setup" );

         if( accel_data != null )
         {
            try{
               accel_data.write( "-1,-1,-1,-1,-1\n" ); // write -1s signifing new open
               accel_data.flush();
            }
            catch( IOException e )
            {
               try{ accel_data.close(); }
               catch( IOException double_e ){}
               finally{ accel_data = null; }
            }
         }


      }
   }
   
   private static List<AccessPoint> accessPoints;

   private Context context;

   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      File dir = getExternalFilesDir( null );
      dir = new File( dir, "acclerometer" );
      if( !dir.exists() )
         dir.mkdirs();

      try
      {
         accel_data = new FileWriter( new File( dir, "latest_data.csv" ), true );
      }
      catch( IOException e )
      {
         if( accel_data != null )
         {
            try{ accel_data.close(); }
            catch( IOException double_e ){}
            finally{ accel_data = null; }
         }
      }

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
      if( accel_data != null )
      {
         try{ accel_data.close(); }
         catch( IOException e ){ accel_data = null; }
      }
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
                              System.err.println( "ok click" );
                     EditText input = (EditText) filename_dialog.findViewById( R.id.filename_input );
                     final String filename = ("" + input.getText()).trim();

                     dismissDialog( CAPTURE_FILENAME );

                     if( filename == null || filename.length() == 0 )
                     {
                        Toast.makeText( context, R.string.ERR_NO_FILENAME, Toast.LENGTH_SHORT ).show();
                        showDialog( CAPTURE_FILENAME, null );
                        return;
                     }
                     

                     final File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsoluteFile();
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
                              System.err.println( "creating process dialog" );
                              ProgressDialog saving = ProgressDialog.show( context, "", 
                                                "Saving. Pleas Wait", true);
                              try
                              {
                                 File final_dir = new File( dir.getPath() + "/captures/" ); 
                                 File file = new File( final_dir.getPath() + "/" + filename );

                                 if( !dir.exists() )
                                    dir.mkdirs();
                                 
                                 OutputStream out = new BufferedOutputStream( new FileOutputStream(file) );
                                 if( !map.saveToFile( out ) )
                                    Toast.makeText( context, R.string.ERR_WRITE_FAIL, Toast.LENGTH_SHORT ).show();
                                 else
                                    Toast.makeText( context, R.string.WRITE_SUCCESS, Toast.LENGTH_SHORT ).show();
                                 saving.dismiss();

                              }
                              catch( IOException e )
                              {
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
      System.err.println( "paused" );
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
      @Override
      public void onReceive( Context context, Intent intent )
      {
         if( accel_data != null )
         {
            long timestamp = intent.getLongExtra( "timestamp", -1 );
            float[] accel = intent.getFloatArrayExtra( "accelerometer" );
            if( accel.length == 3 )
            {
                  // 0 means not a new session
               try
               {
                  accel_data.write( timestamp + "," + accel[0] + "," + accel[1] + "," + accel[2] + ",0\n" );
                  accel_data.flush();
               }
               catch( IOException e )
               {
                  try{ accel_data.close(); }
                  catch( IOException double_e ){}
                  finally{ accel_data = null; }
               }
            }

         }

         map.newCompassData( intent.getFloatExtra( "orientation", 0 ) );
      }
   }

   float prev_x = 0, prev_y = 0, prev_r = 1;

   private class ServiceNotificationReceiver extends BroadcastReceiver
   {
      @Override
      public void onReceive( Context context, Intent intent )
      {
         float new_x = intent.getFloatExtra( "x", -1 );
         float new_y = intent.getFloatExtra( "y", -1 );
         float new_r = intent.getFloatExtra( "radius", 1 );

         double dx = new_x - prev_x;
         double dy = new_y - prev_y;

         double dist = Math.sqrt( dx*dx + dy*dy );
         if( dist > prev_r + new_r || dist < Math.abs( new_r - prev_r ) )
            // no intersect
         {
            map.newWifiData( new_x, new_y, new_r );

            prev_x = new_x;
            prev_y = new_y;
            prev_r = new_r;
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

            r = Math.max( r, BuildingMap.metersToPixels( 7 ) );

            map.newData( prev_x, prev_y, prev_r, new_x, new_y, new_r, x, y, r ); 

            prev_x = (float)x;
            prev_y = (float)y;
            prev_r = (float)r;

            
         }
      }
   }

   public static List<AccessPoint> getAPs()
   {
      return accessPoints;
   }
}
