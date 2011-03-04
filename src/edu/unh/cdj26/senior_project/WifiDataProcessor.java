package edu.unh.cdj26.senior_project;

import java.lang.Thread;
import java.util.List;

import android.content.Context;
import android.net.wifi.*;
import android.widget.*;
import android.os.*;
import android.app.*;


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
      
      List<AccessPoint> aps = IndoorLocalization.getAPs();

      for( ScanResult sr : networks )
      {
         for( AccessPoint ap : aps )
          {
             if( ap.is( sr.BSSID ) )
                ap.setLevel( sr.level );
          }
      }

      handler.sendMessage( handler.obtainMessage() );
   }

}
