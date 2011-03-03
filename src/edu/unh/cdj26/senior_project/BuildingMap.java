
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
      brush.setColor( 0xFF000000 );
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

            canvas.drawPath( p, brush );
         }

         if( ap.hasNewLevel() )
            canvas.drawCircle( (float) apX + bounds.left,
                               (float) apY + bounds.top, 
                               15, 
                               brush );
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
