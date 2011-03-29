
package edu.unh.cdj26.senior_project;

import android.graphics.PointF;
import java.util.*;

public class AccessPoint
{
   protected float xLoc, yLoc; // pixels
   protected float height; // meters
   protected List<String> macs;
   //protected AccessPoint savedState;

   protected float rad_m;

   float rxPower0;
   float exponent;

   private int numRx;
   private float rssAvg;

   public AccessPoint()
   {
      this( 0, 0, 0, 0, 0 );
   }

   public AccessPoint( float x, float y, float h, float p0, float n )
   {
      xLoc = x;
      yLoc = y;
      height = h;

      numRx = 0;
      rssAvg = 0;
      
      rxPower0 = p0;
      exponent = n;

      macs = new ArrayList<String>();

   }

   public float getX()
   {
      return xLoc;
   }

   public float getY()
   {
      return yLoc;
   }

   public float getHeight()
   {
      return height;
   }


   public boolean hasMAC( String address )
   {
      return macs.contains( address.toLowerCase() );
   }

   public boolean equals( AccessPoint ap )
   {
      return
         this.xLoc == ap.xLoc &&
         this.yLoc == ap.yLoc &&
         this.height == ap.height;
   }

   public boolean hasNewLevel()
   {
      return numRx != 0;
   }

   public AccessPoint addMAC( String m )
   {
      macs.add( m.toLowerCase() );
      return this;
   }

   public void addRxLevel( float l )
   {
      rssAvg = ( (rssAvg * numRx) + l ) / (numRx + 1);
      ++numRx;
      
      rad_m = (float) Math.pow( 10, - ( rssAvg + rxPower0 ) / exponent );
      rad_m = (float) Math.sqrt( rad_m * rad_m - height * height );
   }

   public float getApproxRadiusPixels()
   {
      return BuildingMap.metersToPixels( rad_m );
   }

   public float getApproxRadiusMeters()
   {
      return rad_m;
   }

   public float getLevel()
   {
      return rssAvg;
   }

   public float distanceFromPixels( PointF p )
   {
      return (float) Math.sqrt( Math.pow( p.x - xLoc, 2 ) +
                                Math.pow( p.y - yLoc, 2 ) );
   }

   public void clear()
   {
      numRx = 0;
   }

   @Override
   public String toString()
   {
      return "( " + xLoc + ", " + yLoc + ", " + height + ")";
   }

   /*
   public void saveState()
   {
      if( savedState == null )
         savedState = new AccessPoint( xLoc, yLoc, height,
                                       macAddress );
      else
      {
         savedState.xLoc = xLoc;
         savedState.yLoc = yLoc;
         savedState.height = height;
         savedState.macAddress = macAddress;
      }

      savedState.newLevel = newLevel;
      savedState.level = level;

      savedState.rad_m = rad_m;
      
   }

   public AccessPoint getSavedState()
   {
      return savedState;
   }
   */
}
