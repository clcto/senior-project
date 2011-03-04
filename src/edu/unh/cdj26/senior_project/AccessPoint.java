
package edu.unh.cdj26.senior_project;

public class AccessPoint
{
   protected double xLoc, yLoc;
   protected double height;
   protected String macAddress;

   protected boolean newLevel;
   protected int level;

   public AccessPoint( String mac )
   {
      this( 0, 0, 0, mac );
   }

   public AccessPoint( double x, double y, String mac )
   {
      this( x, y, 0, mac );
   }

   public AccessPoint( double x, double y, double h,
                       String a )
   {
      xLoc = x;
      yLoc = y;
      height = h;
      macAddress = a;

      newLevel = false;
   }

   public double getX()
   {
      return xLoc;
   }

   public double getY()
   {
      return yLoc;
   }

   public double getHeight()
   {
      return height;
   }


   public boolean is( String address )
   {
      return macAddress.equals( address );
   }

   public boolean equals( AccessPoint ap )
   {
      return
         this.xLoc == ap.xLoc &&
         this.yLoc == ap.yLoc &&
         this.height == ap.height &&
         this.macAddress.equals( ap.macAddress );
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
}
