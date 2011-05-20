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
   public static int numScans = 3;

   private PointF lseStart = new PointF( 465, 750 );
   private Context context;
   private Handler handler;
   private float lseAvg;
   private static Random rng = new Random(0);

   public WifiDataProcessor( Context c, Handler h )
   {
      handler = h;
      context = c;
      new Thread( this ).start();
   }

   public void run()
   {
      List<AccessPoint> aps = IndoorLocalization.getAPs();
      for( AccessPoint ap : aps )
         ap.clear();

      /* USE THIS CODE WHEN IN TARGET AREA */
      WifiManager manager;
      manager = (WifiManager) context.getSystemService( Context.WIFI_SERVICE );
      List<ScanResult> networks = manager.getScanResults();

      for( ScanResult sr : networks )
      {
         for( AccessPoint ap : aps )
          {
             if( ap.hasMAC( sr.BSSID ) )
                ap.addRxLevel( sr.level );
          }
      }

      /* USE THIS CODE WHEN NOT IN TARGET AREA */
      /*
      aps.get(0).addRxLevel( rng.nextInt(15) - 85 );
      aps.get(1).addRxLevel( rng.nextInt(15) - 82 );
      aps.get(5).addRxLevel( rng.nextInt(15) - 87);
      */
      
      if( wifiScans < numScans )
      {
         wifiScans++;
         Message msg = handler.obtainMessage();
         msg.getData().putBoolean( "finished", false );
         handler.sendMessage( msg );
         return;
      }
      
      ArrayList<AccessPoint> rx = getReceivedAP( aps );

      PointF best = getBestReceived( rx );
      if( best != null )
      {
         while( ( best = guessLocation( best, Float.MAX_VALUE, 5, 30 ) ) == null );
         double lseAvg = getLS( rx, best ) / rx.size();

         float radius = BuildingMap.metersToPixels( 10 ) + (float) Math.sqrt(lseAvg)/23;

            // set all aps to have no new levels
         Message msg = handler.obtainMessage();
         msg.getData().putFloat( "x", best.x );
         msg.getData().putFloat( "y", best.y );
         msg.getData().putFloat( "radius", radius );
         msg.getData().putBoolean( "finished", true );
         wifiScans = 0;
         handler.sendMessage( msg );
      }
   }

   private PointF getBest( List<AccessPoint> aps )
   {
      return getBestReceived( getReceivedAP( aps ) );
   }

   private PointF getBestReceived( ArrayList<AccessPoint> rx )
   {

      double r_min, r_max, r_approx, r_delta;
      double d_theta;

      double ls_min = Double.MAX_VALUE;
      PointF best = null;

      for( AccessPoint ap : rx )
      {
         r_approx = ap.getApproxRadiusPixels();
         r_min = Math.max( 2, r_approx - 100 );
         r_max = r_approx + 200;
         r_delta = ( r_max - r_min ) / 20;
         d_theta = Math.toRadians( 45 );

         for( double r = r_min; r < r_max; r += r_delta )
         {
            for( double a = 0; a < 2 * Math.PI; a += d_theta )
            {
               double x = ap.getY() + r * Math.cos( a );
               double y = ap.getX() + r * Math.sin( a );

               PointF loc = new PointF( (float) x, (float) y );
               double ls = getLS( rx, loc );
               if( ls < ls_min)
               {
                  ls_min = ls;
                  best = loc;
               }
            }
         }
      }

      return best;
   }

   private double getLS( ArrayList<AccessPoint> rx, PointF loc )
   {
      double ls_val = 0;

      for( AccessPoint ap : rx )
         ls_val += Math.pow( ap.getApproxRadiusPixels() - ap.distanceFromPixels( loc ), 2 );

      if( ls_val == 0 )
         ls_val = Double.MAX_VALUE;

      return ls_val;
   }

   private ArrayList<AccessPoint> getReceivedAP( List<AccessPoint> aps )
   {
         // get a guess
         // on one circle in the direction of the next
      Iterator<AccessPoint> iter = aps.iterator();
      ArrayList<AccessPoint> rx = new ArrayList<AccessPoint>();

      while( iter.hasNext() )
      {
         AccessPoint ap = iter.next();
         ap.save();
         if( ap.hasNewLevel() )
            rx.add( ap );
      }

      return rx;
   }
      
   PointF last_guess;

   public PointF guessLocation( PointF prevGuess,
                                float  value,
                                float  delta, int maxDepth )
   {
      if( maxDepth == 0 )
         return null;

      if( prevGuess == null )
         return guessLocation( last_guess, value, delta, maxDepth );

      PointF minPt = prevGuess;
      float  minLSE = value;
      last_guess = prevGuess;

      float[] lse = {0,0,0,0};
      PointF[] pts = { new PointF( prevGuess.x + delta, prevGuess.y ),
                       new PointF( prevGuess.x - delta, prevGuess.y ),
                       new PointF( prevGuess.x, prevGuess.y + delta ),
                       new PointF( prevGuess.x, prevGuess.y - delta ) };

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
         return guessLocation( minPt, minLSE, delta, maxDepth - 1 );
   }
}
