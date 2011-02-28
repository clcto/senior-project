
package edu.unh.cdj26.senior_project;

public class AccessPoint
{
   protected double xLoc, yLoc;
   protected double height;

   protected String macAddress;

   public AccessPoint()
   {
      this( 0, 0, 0, "" );
   }

   public AccessPoint( double x, double y, double h )
   {
      this( x, y, h, "" );
   }

   public AccessPoint( double x, double y, double h,
                       String a )
   {
      xLoc = x;
      yLoc = y;
      height = h;
      macAddress = a;
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
   
}
