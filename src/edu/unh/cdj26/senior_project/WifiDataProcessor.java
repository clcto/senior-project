package edu.unh.cdj26.senior_project;

import java.lang.Thread;
import java.util.List;

import android.content.Context;
import android.graphics.*;
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

      // guess location
      PointF guess = guessLocation( new PointF( 596, 896 ), Float.MAX_VALUE );

      // set all aps to have no new levels
      Message msg = handler.obtainMessage();
      msg.getData().putFloat( "x", guess.x );
      msg.getData().putFloat( "y", guess.y );
      handler.sendMessage( msg );
   }

   
   public PointF guessLocation( PointF prevGuess,
                                float  value )
   {
      System.err.println( "guessLocation called." );
      PointF minPt = prevGuess;
      float  minLSE = value;

      float[] lse = {0,0,0,0};
      PointF[] pts = { new PointF( prevGuess.x + 8, prevGuess.y ),
                       new PointF( prevGuess.x - 8, prevGuess.y ),
                       new PointF( prevGuess.x, prevGuess.y + 8 ),
                       new PointF( prevGuess.x, prevGuess.y - 8 ) };

      // get value of least squares fuction at 
      // points adjacent to the guess and find minimum
      for( AccessPoint ap : IndoorLocalization.getAPs() )
      {
         // calculate radius from each direction
         // and add to the appropriate index of the least
         // squares sum
          
         if( ap.hasNewLevel() )
         {
            float r, r_est;
           
            r_est = ap.peekEstimatedRadiusPixels();
            
            for( int i = 0; i < lse.length; ++i )
            {
               r = ap.distanceFromPixels( pts[i] );
               System.err.println( i + ": " + r );
               lse[i] += Math.pow( r_est - r, 2 );
            }
         }
      }
      
      System.err.println( minLSE );

      for( int i = 0; i < lse.length; ++i )
      {

      System.err.println( "  " + lse[i] );
         if( lse[i] < minLSE )
         {
            minLSE = lse[i];
            minPt  = pts[i];
         }
      }


      System.err.println( "Guess Location: " + minPt.x + ", " + minPt.y );
      if( minLSE == value )
      {
         System.err.println( "       best guess ^^^" );
         return prevGuess;
      }
      else
         return guessLocation( minPt, minLSE );
   }
}
