package edu.unh.cdj26.senior_project;

import java.lang.Thread;
import java.util.List;

import android.content.Context;
import android.net.wifi.*;
import android.widget.Toast;
import android.os.*;


public class WifiDataProcessor implements java.lang.Runnable
{
   private static int id = 0;


   private Context context;
   private Handler handler;

   public WifiDataProcessor( Context c, Handler h )
   {
      handler = h;
      context = c;
      new Thread( this ).start();
   }

   public void run()
   {
      WifiManager manager;
      manager = (WifiManager) context.getSystemService( Context.WIFI_SERVICE );
      List<ScanResult> networks = manager.getScanResults();
      if( networks.size() > 1 )
      {
         // simulate calculations
         try{
            Thread.sleep( 3000 );
         }
         catch( java.lang.InterruptedException ex )
         {
         }

         Message msg = handler.obtainMessage();
         Bundle b = new Bundle();
         b.putString( "infoString", 
            "Name:" + networks.get(0).SSID + "\n" +
            "Mac Address: " + networks.get(0).BSSID + "\n" +
            "Strength (dBm): " + networks.get(0).level );
         msg.setData( b );

         handler.sendMessage( msg );
      }

      manager.startScan();
   }
}
