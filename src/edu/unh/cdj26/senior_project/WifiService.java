
package edu.unh.cdj26.senior_project;

import android.app.*;
import android.content.*;
import android.widget.*;
import android.net.wifi.*;
import android.os.*;
import java.io.*;

public class WifiService extends Service
{
   private BroadcastReceiver receiver;
   private boolean isRunning;

   private double distance;
   private String macAddress;
   private File csvFile;
   private EditText macEntry;

   @Override
   public IBinder onBind( Intent intent )
   {
      return null;
   }

   @Override
   public void onCreate()
   {
      isRunning = false;

   }

   @Override
   public int onStartCommand( Intent intent, 
                              int flags, int startid )
   {
      handleCommand( intent );

      return START_STICKY;
   }

   @Override
   public void onStart( Intent intent, int startid )
   {
      handleCommand( intent );
   }

   @Override
   public void onDestroy()
   {
      unregisterReceiver( receiver );
      super.onDestroy();
   }

   private void handleCommand( Intent intent )
   {
      if( !isRunning )
      {
         isRunning = true;
         Application app = getApplication();

         distance = Math.sqrt( Math.pow( intent.getDoubleExtra( "vert", 0 ), 2 ) +
                               Math.pow( intent.getDoubleExtra( "horiz", 0 ), 2 ) );

         macAddress = intent.getStringExtra( "mac" );
          
         IntentFilter i = new IntentFilter();
         i.addAction( WifiManager.SCAN_RESULTS_AVAILABLE_ACTION );
         receiver = new WifiReceiver();

         registerReceiver( receiver, i );
         WifiManager wm;
         wm = (WifiManager) 
            getApplicationContext().getSystemService( Context.WIFI_SERVICE );
         wm.startScan();
      }
   }

   private class WifiReceiver extends BroadcastReceiver
   {
      @Override
      public void onReceive( Context c, Intent i )
      {
         new WifiDataProcessor( c, new WifiHandler(), macAddress );
            
      }
      
      private class WifiHandler extends Handler
      {
         @Override
         public void handleMessage( Message m )
         {
            Bundle info = m.getData();

            File appDir = Environment.getExternalStorageDirectory();

            if( !appDir.exists() )
               appDir.mkdir();

            File csvFile = new File( appDir, "wifi_data.csv" );
            try
            { 
               FileWriter output = new FileWriter( csvFile, true );
               BufferedWriter out = new BufferedWriter( output );
               out.write( distance + "," + info.getInt( "level" ) + "\n" );
               out.close();
            }
            catch( FileNotFoundException e ){ System.err.println( "fnf ex" ); }
            catch( IOException e ){ System.err.println( "io ex" ); }

            Toast.makeText( 
               getApplicationContext(),
               distance + ", " + info.getInt( "level" ), 
               Toast.LENGTH_LONG 
            ).show();

               // use the notification system to post the MAC address used.
               // this allows for the user to place it in the form field 
               // so future scans will use the proper AP.
            if( info.containsKey( "mac" ) )
            {
               String ns = Context.NOTIFICATION_SERVICE;
               NotificationManager nm = (NotificationManager)getSystemService(ns);
               Notification notif = new Notification( 
                  R.drawable.notification, info.getString("mac"),
                  System.currentTimeMillis() );

               Context cont = getApplicationContext();
               PendingIntent contentIntent = PendingIntent.getActivity(
                  cont, 0, null, 0 );

               notif.setLatestEventInfo( cont, "Best AP MAC", 
                  info.getString("mac") + " -- " + info.getInt( "level"), 
                  contentIntent );

               nm.notify( 1, notif );

            }
            stopSelf();
         }
         
      }

   }
}
