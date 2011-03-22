
package edu.unh.cdj26.senior_project;

import android.graphics.PointF;

public class AccessPoint
{
   protected float xLoc, yLoc;
   protected float height;
   protected String macAddress;
   protected AccessPoint savedState;

   protected boolean newLevel;
   protected int level;

   protected float rad_m;

   public AccessPoint( String mac )
   {
      this( 0, 0, 0, mac );
   }

   public AccessPoint( float x, float y, String mac )
   {
      this( x, y, 0, mac );
   }

   public AccessPoint( float x, float y, float h,
                       String a )
   {
      xLoc = x;
      yLoc = y;
      height = h;
      macAddress = a;

      newLevel = false;
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


   public boolean is( String address )
   {
      return macAddress.equalsIgnoreCase( address );
   }

   public boolean equals( AccessPoint ap )
   {
      return
         this.xLoc == ap.xLoc &&
         this.yLoc == ap.yLoc &&
         this.height == ap.height &&
         this.macAddress.equalsIgnoreCase( ap.macAddress );
   }

   public boolean hasNewLevel()
   {
      return newLevel;
   }

   /**
    * give the access point a new level to report
    */
   public void setLevel( int l )
   {
      level = l;

      rad_m = (float) Math.pow( 10, (level+47)/-27.5 );

      newLevel = true;
   }

   /**
    * get the signal strength from the AP and no longer
    * consider it a new level
    *
    * @return the rss from the access point if there is
    *         a new level. Otherwise 0.
    */
   public int getLevel()
   {
      int retVal;

      if( newLevel )
         retVal = level;
      else
         retVal = 0;

      newLevel = false;

      return retVal;
   }

   /**
    * get the signal strength from the AP and keep the
    * state of new level available.
    *
    * @return the rss from the access point if there is
    *         a new level. Otherwise 0.
    */
   public int peekLevel()
   {
      if( newLevel )
         return level;
      else
         return 0;
   }

   public float getEstimatedRadiusPixels()
   {
      float retVal;

      retVal = BuildingMap.metersToPixels( newLevel ? rad_m : 0 );

      newLevel = false;

      return retVal;
   }

   public float peekEstimatedRadiusPixels()
   {
      return BuildingMap.metersToPixels( newLevel ? rad_m : 0 );
   }

   public float distanceFromPixels( PointF p )
   {
      return (float) Math.sqrt( Math.pow( p.x - xLoc, 2 ) +
                                Math.pow( p.y - yLoc, 2 ) );
   }

   @Override
   public String toString()
   {
      return "( " + xLoc + ", " + yLoc + ", " + height + 
             ") <" + macAddress + ">";
   }

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
}
