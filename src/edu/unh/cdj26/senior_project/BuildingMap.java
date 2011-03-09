
package edu.unh.cdj26.senior_project;

import android.content.Context;
import android.graphics.drawable.*;
import android.view.*;
import android.graphics.*;
import java.util.*;


public class BuildingMap extends View
               implements GestureDetector.OnGestureListener,
                          ScaleGestureDetector.OnScaleGestureListener
                        
{

   private Drawable mapImage;
   private int mapWidth, mapHeight;
   private float xLoc, yLoc;
   private float xScreen, yScreen;
   private Location locRelative;
   private GestureDetector gestures;
   private ScaleGestureDetector scaleDetector;
   private ArrayList<ActiveAPState> actives;
   private boolean newWifiData;
   private float scale;

   private float foc_x_old, foc_y_old;

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
      scaleDetector = new ScaleGestureDetector( c, this );


      actives = new ArrayList<ActiveAPState>();

      newWifiData = false;

      scale = 0.5f;
   }

   @Override
   protected void onDraw( Canvas canvas )
   {
      super.onDraw( canvas );
      canvas.save();

      float x, y;
      switch( locRelative )
      {
         case UpperLeft:
            mapImage.setBounds( (int) (-xLoc * scale),
                                (int) (-yLoc * scale), 
                                (int) ((-xLoc + mapWidth)  * scale), 
                                (int) ((-yLoc + mapHeight) * scale) );
            break;
         case Center:
            x = xLoc - getWidth()/(2*scale);
            y = yLoc - getHeight()/(2*scale);
            mapImage.setBounds( (int) (-x * scale), 
                                (int) (-y * scale),
                                (int) ((-x + mapWidth)  * scale),
                                (int) ((-y + mapHeight) * scale) );
            break;
         case Pixel:
            x = xLoc - xScreen/scale;
            y = yLoc - yScreen/scale;
            mapImage.setBounds( (int) (-x * scale), 
                                (int) (-y * scale),
                                (int) ((-x + mapWidth)  * scale),
                                (int) ((-y + mapHeight) * scale) );
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
      
      float ap_x, ap_y;
      
      ap_x = 464;
      ap_y = 761;
           // test numbers



      //    for each access point do this
         
      ap_x *= scale;
      ap_y *= scale;
      
      ap_x += bounds.left;
      ap_y += bounds.top;

      Path p = new Path();
      p.moveTo( ap_x - 5, ap_y + 4 );
      p.rLineTo( 10, 0 );
      p.rLineTo( -5, -10 );
      p.rLineTo( -5, 10 );

      brush.setColor( 0xFFDD7700 );
      brush.setStyle( Paint.Style.STROKE );
      canvas.drawPath( p, brush );
   
      brush.setColor( 0x55DD7700 );
      brush.setStyle( Paint.Style.FILL );
      canvas.drawPath( p, brush );


     /* REMOVE CODE TO DRAW CIRCLES -----------------------
      *
      *
      
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

         if( state != null && state.hasNewLevel() )
         {
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
      */


      canvas.restore();
   }

   @Override
   public boolean onTouchEvent( MotionEvent me )
   {
      scaleDetector.onTouchEvent( me );

      if( scaleDetector.isInProgress() )
         return true;

      gestures.onTouchEvent(me);
      switch( me.getAction() )
      {
         /* No longer need action up moves to center of screen
         case MotionEvent.ACTION_UP:
            Rect bounds = mapImage.getBounds();

            float x = (me.getX() - bounds.left) / scale;
            float y = (me.getY() - bounds.top) / scale;
            System.err.println( x + ", " +  y );
            setCenterPixel( (int)x, (int)y );
            break;
          */
      }

      return true;
      //return gestures.onTouchEvent( me );
   }

   public void setCenterPixel( float x, float y )
   {
      locRelative = Location.Center;
      xLoc = x;
      yLoc = y;
      invalidate();
   }

   public void setUpperLeftPixel( float x, float y )
   {
      locRelative = Location.UpperLeft;
      xLoc = x;
      yLoc = y;
      invalidate();
   }
   
   public void setPixelRelativeTo( float sX, float sY, float pX, float pY )
   {
      locRelative = Location.Pixel;
      xScreen = sX;
      yScreen = sY;

      xLoc = pX;
      yLoc = pY;

      invalidate();
   }

   private enum Location
   {
      UpperLeft, Center, Pixel
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
      xLoc += dX/scale;
      yLoc += dY/scale;

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

   @Override
   public boolean onScale( ScaleGestureDetector sgd )
   {
      /*
      float foc_x = sgd.getFocusX();
      float foc_y = sgd.getFocusY();
      
      float foc_x_ave = ( foc_x_old + foc_x ) / 2;
      float foc_y_ave = ( foc_y_old + foc_y ) / 2;

      foc_x_old = foc_x;
      foc_y_old = foc_y;
      */
      float bx = mapImage.getBounds().left;
      float by = mapImage.getBounds().top;

      float fx = foc_x_old;
      float fy = foc_y_old;

      float newX = (fx-bx)/scale;
      float newY = (fy-by)/scale;
      System.err.println( "onScale\n  focus = ( " + fx + ", " + fx + " )" );
      System.err.println( "  bound = ( " + bx + ", " + by + " )" );

      System.err.println( "  scale = " + scale );

      scale *= sgd.getScaleFactor();

      scale = Math.max( 0.25f, Math.min( scale, 1.0f ) );

      System.err.println( "  upperleft = ( " + newX + ", " + newY + " )" );
      System.err.println();
      setPixelRelativeTo( fx, fy, newX, newY );

      return true;
   }

   @Override
   public boolean onScaleBegin( ScaleGestureDetector sgd )
   {
      foc_x_old = sgd.getFocusX();
      foc_y_old = sgd.getFocusY();
      return true;
   }

   @Override
   public void onScaleEnd( ScaleGestureDetector sgd )
   {
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
