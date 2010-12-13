
package edu.unh.cdj26.senior_project;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.widget.Toast;
import android.content.Context;
import android.net.wifi.*;
import android.os.*;

public class WifiService extends Service
{
   private BroadcastReceiver receiver;

   @Override
   public IBinder onBind( Intent intent )
   {
      return null;
   }

   @Override
   public void onCreate()
   {
      IntentFilter i = new IntentFilter();
      i.addAction( WifiManager.SCAN_RESULTS_AVAILABLE_ACTION );
      receiver = new BroadcastReceiver()
         {
            public void onReceive( Context c, Intent i )
            {
               new WifiDataProcessor( c, 
                     new Handler()
                     {
                        @Override
                        public void handleMessage( Message m )
                        {
                           Toast.makeText( 
                              getApplicationContext(),
                              m.getData().getString("infoString"),
                              Toast.LENGTH_SHORT ).show();
                        }
                     } );
                  
            }
         };

      registerReceiver( receiver, i );
   }

   @Override
   public void onDestroy()
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

   private void handleCommand( Intent intent )
   {
      WifiManager wm;
      wm = (WifiManager) getApplicationContext().getSystemService( 
                                                 Context.WIFI_SERVICE );
      wm.startScan();
   }
}
