
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
   private WifiManager wm;

   private double distance;

   @Override
   public IBinder onBind( Intent intent )
   {
      return null;
   }

   @Override
   public void onCreate()
   {

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
      if( receiver != null )
      {
         unregisterReceiver( receiver );
         receiver = null;
      }
      super.onDestroy();
   }

   private void handleCommand( Intent intent )
   {
      IntentFilter i = new IntentFilter();
      i.addAction( WifiManager.SCAN_RESULTS_AVAILABLE_ACTION );
      receiver = new WifiReceiver();

      registerReceiver( receiver, i );
      wm = (WifiManager) getApplicationContext().getSystemService( Context.WIFI_SERVICE );
      wm.startScan();
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
            if( info.getBoolean( "finished" ) )
            {
               Intent scanDone = new Intent( "WIFI_DATA_PROCESSED" );
               scanDone.putExtra( "x", info.getFloat( "x" ) );
               scanDone.putExtra( "y", info.getFloat( "y" ) );
               scanDone.putExtra( "radius", info.getFloat( "radius" ) );
               sendBroadcast( scanDone );
            }

            wm.startScan();
         }
         
      }

   }
}
