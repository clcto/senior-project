package edu.unh.cdj26.senior_project;

import java.lang.Thread;
import java.util.List;

import android.content.Context;
import android.graphics.*;
import android.net.wifi.*;
import android.widget.*;
import android.os.*;
import android.app.*;
import java.util.*;

public class WifiDataProcessor implements java.lang.Runnable
{
   private static int wifiScans = 0;
   private static final int NUM_SCANS = 10;

   private PointF lseStart = new PointF( 465, 750 );
   private Context context;
   private Handler handler;
   private float lseAvg;

   public WifiDataProcessor( Context c, Handler h )
   {
      handler = h;
      context = c;
      new Thread( this ).start();
   }

   public void run()
   {
      /* USE THIS CODE WHEN IN TARGET AREA */

      WifiManager manager;
      manager = (WifiManager) context.getSystemService( Context.WIFI_SERVICE );
      List<ScanResult> networks = manager.getScanResults();
      List<AccessPoint> aps = IndoorLocalization.getAPs();
      for( AccessPoint ap : aps )
         ap.clear();

      for( ScanResult sr : networks )
      {
         for( AccessPoint ap : aps )
          {
             if( ap.hasMAC( sr.BSSID ) )
                ap.addRxLevel( sr.level );
          }
      }
      
      if( wifiScans < NUM_SCANS )
      {
         wifiScans++;
         Message msg = handler.obtainMessage();
         msg.getData().putBoolean( "finished", false );
         handler.sendMessage( msg );
         return;
      }

      

      /* USE THIS CODE WHEN NOT IN TARGET AREA */
      /*
      List<AccessPoint> aps = IndoorLocalization.getAPs();
      Random rng = new Random();
      aps.get(0).addRxLevel( rng.nextInt(20) - 80 );
      aps.get(2).addRxLevel( rng.nextInt(20) - 80 );
      aps.get(3).addRxLevel( rng.nextInt(20) - 85 );
      */

         // get a guess
         // on one circle in the direction of the next
      int numAP = 0;
      Iterator<AccessPoint> iter = aps.iterator();
      AccessPoint[] rxAPs = new AccessPoint[2];

      while( iter.hasNext() )
      {
         AccessPoint ap = iter.next();
         if( ap.hasNewLevel() )
         {
            rxAPs[ numAP ] = ap;
            numAP++;
            if( numAP > 1 )
               break;
         }
      }
      
         // check to see if we got 2 ap
      if( rxAPs[1] == null )
      {
         wifiScans = 0;
         Message msg = handler.obtainMessage();
         msg.getData().putBoolean( "finished", false );
         handler.sendMessage( msg );
         return;
      }

      double total_distance = 
         Math.sqrt( Math.pow( rxAPs[0].getX() - rxAPs[1].getX(), 2 ) +
                    Math.pow( rxAPs[0].getY() - rxAPs[1].getY(), 2 ) );

      System.err.println( rxAPs[0] );
      System.err.println( rxAPs[1] );

      double rad = 0.5 * ( rxAPs[0].getApproxRadiusPixels() + total_distance - rxAPs[1].getApproxRadiusPixels() );

      double ratio = rad / total_distance;

      // check to see which side of rxAPs[0] rxAPs[1] thinks I am
      if( rxAPs[1].getApproxRadiusPixels() > total_distance )
         ratio *= -1;

      double gX = rxAPs[0].getX() + ratio * (rxAPs[1].getX() - rxAPs[0].getX());
      double gY = rxAPs[0].getY() + ratio * (rxAPs[1].getY() - rxAPs[0].getY());

      lseStart = new PointF( (float) gX, (float) gY );
     
      System.err.println( "starting guess" );

      lseStart = guessLocation( lseStart, Float.MAX_VALUE );

      float radius = 200 + (float) Math.sqrt(lseAvg) / 15;
      System.err.println( "cdj26 lseAvg: " + lseAvg );
      System.err.println( "cdj26 rad: " + radius );

         // set all aps to have no new levels
      Message msg = handler.obtainMessage();
      msg.getData().putFloat( "x", lseStart.x );
      msg.getData().putFloat( "y", lseStart.y );
      msg.getData().putFloat( "radius", radius );
      msg.getData().putBoolean( "finished", true );
      handler.sendMessage( msg );
   }

   
   public PointF guessLocation( PointF prevGuess,
                                float  value )
   {
      PointF minPt = prevGuess;
      float  minLSE = value;

      float[] lse = {0,0,0,0};
      PointF[] pts = { new PointF( prevGuess.x + 12, prevGuess.y ),
                       new PointF( prevGuess.x - 12, prevGuess.y ),
                       new PointF( prevGuess.x, prevGuess.y + 12 ),
                       new PointF( prevGuess.x, prevGuess.y - 12 ) };

      int numAP = 0;
      // get value of least squares fuction at 
      // points adjacent to the guess and find minimum
      for( AccessPoint ap : IndoorLocalization.getAPs() )
      {
         // calculate radius from each direction
         // and add to the appropriate index of the least
         // squares sum
          
         if( ap.hasNewLevel() )
         {
            ++numAP;
            float r, r_est;
           
            r_est = ap.getApproxRadiusPixels();
            
            for( int i = 0; i < lse.length; ++i )
            {
               r = ap.distanceFromPixels( pts[i] );
               lse[i] += Math.pow( r_est - r, 2 );
            }
         }
      }
      
      for( int i = 0; i < lse.length; ++i )
      {

         if( lse[i] < minLSE )
         {
            minLSE = lse[i];
            minPt  = pts[i];
         }
      }


      if( minLSE == value )
      {
         lseAvg = value / numAP;
         return prevGuess;
      }
      else
         return guessLocation( minPt, minLSE );
   }
}
