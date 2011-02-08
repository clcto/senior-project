
package edu.unh.cdj26.senior_project;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.View;

public class BuildingMap extends View
{

   private Drawable mapImage;
   private int mapWidth, mapHeight;


   public BuildingMap( Context c )
   {
      super( c );
      mapImage = c.getResources().getDrawable( 
                  R.drawable.floorplan );

      Bitmap bitmap = BitmapFactory.decodeResource(
         getResources(), R.drawable.floorplan );
      mapWidth = bitmap.getWidth();
      mapHeight = bitmap.getHeight();
   }

   @Override
   protected void onDraw( Canvas canvas )
   {
      super.onDraw( canvas );
      mapImage.draw( canvas );
   }

   public void setCenterPixel( int x, int y )
   {
      setUpperLeftPixel( x - getWidth()/2,
                         y - getHeight()/2 );
   }

   public void setUpperLeftPixel( int x, int y )
   {
      System.out.println( "( " + x + ", " + y + " )" );
      mapImage.setBounds( -x, -y, 
                          -x + mapWidth, -y + mapHeight );
   }
}
