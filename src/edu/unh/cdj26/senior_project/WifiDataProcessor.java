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
   private String mac;

   public WifiDataProcessor( Context c, Handler h, String m )
   {
      handler = h;
      context = c;
      mac = m;
      new Thread( this ).start();
   }

   public void run()
   {
      WifiManager manager;
      manager = (WifiManager) context.getSystemService( Context.WIFI_SERVICE );
      List<ScanResult> networks = manager.getScanResults();
      if( networks.size() > 1 )
      {
         if( mac != null && mac.length() > 0 )
         {
            for( ScanResult sr : networks )
               if( sr.BSSID.equals( mac ) )
               {
                  Message msg = handler.obtainMessage();
                  Bundle b = new Bundle();
                  b.putInt( "level", sr.level );
                  msg.setData( b );

                  handler.sendMessage( msg );

                  break;
               }
         }
         else
         {
            ScanResult bestAP = networks.get(0);
            for( ScanResult sr : networks )
            {
               if( sr.level > bestAP.level )
                  bestAP = sr;
            }

            Message msg = handler.obtainMessage();
            Bundle b = new Bundle();
            b.putInt( "level", bestAP.level );
            b.putString( "mac", bestAP.BSSID );
            msg.setData( b );

            handler.sendMessage( msg );
         }
            
      }
   }
}
