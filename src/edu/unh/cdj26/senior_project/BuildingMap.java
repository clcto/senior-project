
package edu.unh.cdj26.senior_project;

import android.content.Context;
import android.graphics.drawable.*;
import android.view.*;
import android.graphics.*;
import java.util.*;

public class BuildingMap extends View
{

   private Drawable mapImage;
   private int mapWidth, mapHeight;
   private int xLoc, yLoc;
   private Location locRelative;

      // access points will be passed in
   public BuildingMap( Context c )
   {
      super( c );
      mapImage = c.getResources().getDrawable( 
                  R.drawable.floorplan );

      Bitmap bitmap = BitmapFactory.decodeResource(
         getResources(), R.drawable.floorplan );
      mapWidth = bitmap.getWidth();
      mapHeight = bitmap.getHeight();

      setUpperLeftPixel( 0, 0 );
   }

   @Override
   protected void onDraw( Canvas canvas )
   {
      super.onDraw( canvas );

      switch( locRelative )
      {
         case UpperLeft:
            mapImage.setBounds( -xLoc, -yLoc, 
                  -xLoc + mapWidth, -yLoc + mapHeight );
            break;
         case Center:
            int x = xLoc - getWidth()/2;
            int y = yLoc - getHeight()/2;
            mapImage.setBounds( -x, -y, 
                  -x + mapWidth, -y + mapHeight );
            break;
      }

      mapImage.draw( canvas );
      
      Paint brush = new Paint();
      brush.setDither( true );
      brush.setStyle( Paint.Style.STROKE );
      brush.setStrokeJoin( Paint.Join.ROUND );
      brush.setStrokeCap( Paint.Cap.ROUND );
      brush.setStrokeWidth( 2 );

      Rect bounds = mapImage.getBounds();

      List<AccessPoint> aps = IndoorLocalization.getAPs();

      for( AccessPoint ap : aps )
      {
         double apX = ap.getX(); 
         double apY = ap.getY();

         if(  apX + bounds.left >= 0 &&
              apX + bounds.left <= getWidth() &&
              apY + bounds.top  >= 0 && 
              apY + bounds.top  <= getHeight() )
         {
            Path p = new Path();
            p.moveTo( (float) apX + bounds.left - 5, 
                      (float) apY + bounds.top + 4 );
            
            p.rLineTo( 10, 0 );
            p.rLineTo( -5, -8 );
            p.rLineTo( -5, 8 );

            brush.setColor( 0xFF000000 );
            canvas.drawPath( p, brush );
         }

         if( ap.hasNewLevel() )
         {
            int rss = ap.getLevel();
            double dist_meter = ( rss + 49 ) / (-1.84);


            double h = ap.getHeight();

            double d_m_low = dist_meter - 6;
            d_m_low = d_m_low > 0 ? d_m_low : 0 ;

            double rad_m_low = Math.sqrt( d_m_low*d_m_low - h*h );
            double rad_l = rad_m_low * 30 / 2.3;

            double d_m_high = dist_meter + 6;
            d_m_high = d_m_high > 0 ? d_m_high : 0 ;

            double rad_m_high = Math.sqrt( d_m_high*d_m_high - h*h );
            double rad_h = rad_m_high * 30 / 2.3;


            
            brush.setColor( 0xFFCC1111 );
            canvas.drawCircle( (int) apX + bounds.left,
                               (int) apY + bounds.top, 
                               (int) rad_l,
                               brush );

            canvas.drawCircle( (int) apX + bounds.left,
                               (int) apY + bounds.top, 
                               (int) rad_h,
                               brush );
         }
      }
   }

   public void setCenterPixel( int x, int y )
   {
      locRelative = Location.Center;
      xLoc = x;
      yLoc = y;
   }

   public void setUpperLeftPixel( int x, int y )
   {
      locRelative = Location.UpperLeft;
      xLoc = x;
      yLoc = y;
   }

   private enum Location
   {
      UpperLeft, Center
   }
}
