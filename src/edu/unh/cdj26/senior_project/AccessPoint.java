
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
   private float rssSum;
   private boolean newLevel;

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
      rssSum = 0;
      
      rxPower0 = p0;
      exponent = n;

      newLevel = false;

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
      return newLevel;
   }

   public AccessPoint addMAC( String m )
   {
      macs.add( m.toLowerCase() );
      return this;
   }

   public void addRxLevel( float l )
   {
      rssSum += l;
      ++numRx;
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
      rssSum = 0;
   }

   public void save()
   {
      rssAvg = rssSum / numRx;
      newLevel = (numRx != 0);

      rad_m = (float) Math.pow( 10, - ( rssAvg + rxPower0 ) / exponent );
      if( height > rad_m )
         rad_m = 1;
      else
         rad_m = (float) Math.sqrt( rad_m * rad_m - height * height );
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
