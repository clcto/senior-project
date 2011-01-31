
package edu.unh.cdj26.senior_project;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;

public class BuildingMap extends View
{

   private Drawable mapImage;

   public BuildingMap( Context c )
   {
      super( c );
      mapImage = c.getResources().getDrawable( 
                  R.drawable.android );
   }

   @Override
   protected void onDraw( Canvas canvas )
   {
      super.onDraw( canvas );
      mapImage.setBounds( -50, 0, getWidth(), getHeight() );
      mapImage.draw( canvas );
   }
}
