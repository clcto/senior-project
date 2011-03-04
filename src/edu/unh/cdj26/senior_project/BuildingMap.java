
package edu.unh.cdj26.senior_project;

import android.content.Context;
import android.graphics.drawable.*;
import android.view.*;
import android.graphics.*;
import java.util.*;


public class BuildingMap extends View
                         implements GestureDetector.OnGestureListener
{

   private Drawable mapImage;
   private int mapWidth, mapHeight;
   private int xLoc, yLoc;
   private Location locRelative;
   private GestureDetector gestures;
   private ArrayList<ActiveAPState> actives;
   private boolean newWifiData;

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

      gestures = new GestureDetector( c, this ); 

      actives = new ArrayList<ActiveAPState>();

      newWifiData = false;
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
      brush.setAntiAlias( true );
      brush.setStrokeJoin( Paint.Join.ROUND );
      brush.setStrokeCap( Paint.Cap.ROUND );
      brush.setStrokeWidth( 2 );

      Rect bounds = mapImage.getBounds();

      List<AccessPoint> aps = IndoorLocalization.getAPs();
      if( newWifiData )
      {
         for( AccessPoint ap : aps )
            ap.saveState();

         newWifiData = false;
      }

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
            p.rLineTo( -5, -10 );
            p.rLineTo( -5, 10 );

            brush.setColor( 0xFF000000 );
            brush.setStyle( Paint.Style.FILL_AND_STROKE );
            canvas.drawPath( p, brush );
         }
         
         AccessPoint state = ap.getSavedState(); 
         System.err.println( state );
         if( state != null && state.hasNewLevel() )
         {
            System.err.println( " - here " );
            int rss = state.peekLevel();
            double dist_meter = ( rss + 49 ) / (-1.84);

            double h = state.getHeight();

            double d_m_low = dist_meter - 5;
            d_m_low = d_m_low > 0 ? d_m_low : 0 ;

            double rad_m_low = Math.sqrt( d_m_low*d_m_low - h*h );
            double rad_l = rad_m_low * 20;

            double d_m_high = dist_meter + 5;
            d_m_high = d_m_high > 0 ? d_m_high : 0 ;

            double rad_m_high = Math.sqrt( d_m_high*d_m_high - h*h );
            double rad_h = rad_m_high * 20;

            brush.setColor( 0xFFCC1111 );
            brush.setStyle( Paint.Style.STROKE );
            canvas.drawCircle( (float) apX + bounds.left,
                               (float) apY + bounds.top, 
                               (float) rad_l,
                               brush );

            canvas.drawCircle( (float) apX + bounds.left,
                               (float) apY + bounds.top, 
                               (float) rad_h,
                               brush );

            brush.setColor( 0x22CC1111 );
            brush.setStyle( Paint.Style.FILL );
            canvas.drawCircle( (float) apX + bounds.left,
                               (float) apY + bounds.top, 
                               (float) rad_h,
                               brush );

            brush.setColor( 0x88FFFFFF );
            brush.setStyle( Paint.Style.FILL );
            canvas.drawCircle( (float) apX + bounds.left,
                               (float) apY + bounds.top, 
                               (float) rad_l,
                               brush );
         }
      }
   }

   @Override
   public boolean onTouchEvent( MotionEvent me )
   {
      System.out.println( "onScroll ");
      return gestures.onTouchEvent( me );
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

   public void newWifiData()
   {
      newWifiData = true;
      invalidate();
   }

   @Override
   public boolean onScroll( MotionEvent me1, MotionEvent me2, float dX, float dY )
   {
      System.out.println( "onScroll " + dX + " " + dY );
      xLoc += dX;
      yLoc += dY;

      invalidate();

      return true;
   }

   @Override
   public boolean onDown( MotionEvent me )
   {
      return true;
   }

   @Override
   public boolean onFling( MotionEvent me1, MotionEvent me2, float vX, float vY )
   {
      return false;
   }

   @Override
   public void onLongPress( MotionEvent me )
   {
   }

   @Override
   public void onShowPress( MotionEvent me )
   {
   }

   @Override
   public boolean onSingleTapUp( MotionEvent me )
   {
      return false;
   }

   private class ActiveAPState
   {
      public float x_px, y_px, inner, outer;

      public ActiveAPState( float x, float y,
                            float i, float o )
      {
         x_px = x;
         y_px = y;
         inner = i;
         outer = o;
      }
   }
}
