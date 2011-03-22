
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

         IntentFilter i = new IntentFilter();
         i.addAction( WifiManager.SCAN_RESULTS_AVAILABLE_ACTION );
         receiver = new WifiReceiver();

         registerReceiver( receiver, i );
         WifiManager wm;
         wm = (WifiManager) getApplicationContext().getSystemService( Context.WIFI_SERVICE );
         wm.startScan();
      }
   }

   private class WifiReceiver extends BroadcastReceiver
   {
      @Override
      public void onReceive( Context c, Intent i )
      {
            // probably don't need to create one of these
            // every time... that is the wifihandler
         new WifiDataProcessor( c, new WifiHandler() );
      }
      
      private class WifiHandler extends Handler
      {
         @Override
         public void handleMessage( Message m )
         {
            Bundle info = m.getData();
            Intent scanDone = new Intent( "WIFI_DATA_PROCESSED" );
            scanDone.putExtra( "x", info.getFloat( "x" ) );
            scanDone.putExtra( "y", info.getFloat( "y" ) );
            sendBroadcast( scanDone );

               // tell the wifiservice to stop. we only
               // want(ed) to run once.
            stopSelf();
         }
         
      }

   }
}
