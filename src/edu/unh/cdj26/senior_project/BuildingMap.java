
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
   private PointF userLocation, prev_userLocation, new_userLocation;
   private float userOrientation;
   private float userRad = 0, prev_userRad = 0, new_userRad = 0;

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
      mapImage.setBounds( 0, 0, mapWidth, mapHeight );

      setUpperLeftPixel( 0, 0 );

      gestures = new GestureDetector( c, this ); 
      scaleDetector = new ScaleGestureDetector( c, this );


      actives = new ArrayList<ActiveAPState>();

      newWifiData = false;
      scale = 0.5f;


      setUpperLeftPixel( 0, 0 );
   }

   @Override
   protected void onDraw( Canvas canvas )
   {
      super.onDraw( canvas );
      canvas.save();

      canvas.scale( scale, scale );
      switch( locRelative )
      {
         case Center:
            xLoc -= getWidth() / 2 / scale;
            yLoc -= getHeight() / 2 / scale;
            locRelative = Location.UpperLeft;
            break;
         case Pixel:
            xLoc -= xScreen;
            yLoc -= yScreen;
            locRelative = Location.UpperLeft;
            break;
      }
      
      canvas.translate( -xLoc, -yLoc );


      Paint brush = new Paint();
      brush.setDither( true );
      brush.setAntiAlias( true );
      brush.setStrokeJoin( Paint.Join.ROUND );
      brush.setStrokeCap( Paint.Cap.ROUND );
      brush.setStrokeWidth( 2/scale );
      brush.setColor( 0xFF333333 );
      brush.setStyle( Paint.Style.STROKE );


      canvas.drawRGB( 200, 200, 200 );
      mapImage.draw( canvas );
      canvas.drawRect( -10, -10, mapWidth + 9, mapHeight + 9, brush );


      Path triangle = new Path();
      triangle.moveTo( -5, 4 );
      triangle.rLineTo( 10, 0 );
      triangle.rLineTo( -5, -10 );
      triangle.rLineTo( -5, 10 );


      
      List<AccessPoint> aps = IndoorLocalization.getAPs();

      for( AccessPoint ap : aps )
      {
         float apX = ap.getX(); 
         float apY = ap.getY();

         Matrix m = new Matrix();
         m.preTranslate( apX, apY );
         m.preScale( 1/scale, 1/scale );

         Path ap_tri = new Path(); 
         triangle.transform( m, ap_tri );

         if( ap.hasNewLevel() )
         {
            brush.setColor( 0x88DD7700 );
            brush.setStyle( Paint.Style.STROKE );
            canvas.drawPath( ap_tri, brush );
         
            brush.setColor( 0x44DD7700 );
            brush.setStyle( Paint.Style.FILL );
            canvas.drawPath( ap_tri, brush );

            double rad_p = ap.getApproxRadiusPixels();

            brush.setColor( 0x44CC1111 );
            brush.setStyle( Paint.Style.STROKE );
            canvas.drawCircle( (float) apX,
                               (float) apY, 
                               (float) rad_p,
                               brush );
         }
      }

      if( userLocation != null )
      {
         brush.setColor( 0x442222CC );
         brush.setStyle( Paint.Style.FILL );
         canvas.drawCircle( userLocation.x,
                            userLocation.y,
                            userRad, brush );

         Matrix m = new Matrix();
         m.preTranslate( userLocation.x, userLocation.y );
         m.preRotate( userOrientation );
         m.preScale( 1/scale, 1/scale );

         Path user_icon = new Path();
         user_icon.arcTo( new RectF( -7, -7, 7, 7 ), -65, 310 );
         user_icon.lineTo( 0, -14 );
         user_icon.close();

         user_icon.transform( m );

         brush.setColor( 0xFFFFFF00 );
         canvas.drawPath( user_icon, brush );

         brush.setColor( 0xFF2222CC );
         brush.setStyle( Paint.Style.STROKE );
         canvas.drawPath( user_icon, brush );

      }

      if( prev_userLocation != null )
      {
         brush.setColor( 0xFF333333 );
         brush.setStyle( Paint.Style.STROKE );
         canvas.drawCircle( prev_userLocation.x,
                            prev_userLocation.y,
                            prev_userRad, brush );
      }

      if( new_userLocation != null )
      {
         brush.setColor( 0xFF333333 );
         brush.setStyle( Paint.Style.STROKE );
         canvas.drawCircle( new_userLocation.x,
                            new_userLocation.y,
                            new_userRad, brush );
      }

      canvas.restore();
   }

   @Override
   public boolean onTouchEvent( MotionEvent me )
   {
      scaleDetector.onTouchEvent( me );

      if( scaleDetector.isInProgress() )
         return true;

      gestures.onTouchEvent(me);

      return true;
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
      xScreen = sX/scale;
      yScreen = sY/scale;

      xLoc = pX;
      yLoc = pY;

      invalidate();
   }

   private enum Location
   {
      UpperLeft, Center, Pixel
   }

   public void newCompassData( float deg )
   {
      userOrientation = deg - 40;
      invalidate();
   }

   public void newData( double px, double py, double pr, 
                        double nx, double ny, double nr,
                        double cx, double cy, double cr )
   {
      newData( (float) px, (float) py, (float) pr,
               (float) nx, (float) ny, (float) nr,
               (float) cx, (float) cy, (float) cr );
   }

   public void newData( float px, float py, float pr, 
                        float nx, float ny, float nr,
                        float cx, float cy, float cr )
   {
      newWifiData = true;
      userLocation = new PointF( cx, cy );
      userRad = cr;

      prev_userLocation = new PointF( px, py );
      prev_userRad = pr;

      new_userLocation = new PointF( nx, ny );
      new_userRad = nr;

      invalidate();
   }

   public void newWifiData( float x, float y, float r )
   {
      newWifiData = true;
      userLocation = new PointF( x, y );
      userRad = r;

      prev_userLocation = null;
      new_userLocation = null;

      invalidate();
   }

   @Override
   public boolean onScroll( MotionEvent me1, MotionEvent me2, float dX, float dY )
   {
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
      float foc_x = sgd.getFocusX();
      float foc_y = sgd.getFocusY();

      scale *= sgd.getScaleFactor();

      scale = Math.max( 0.01f, Math.min( scale, 1.0f ) );

      setPixelRelativeTo( foc_x, foc_y, foc_x_old, foc_y_old );

      return true;
   }

   @Override
   public boolean onScaleBegin( ScaleGestureDetector sgd )
   {
      foc_x_old = sgd.getFocusX();
      foc_y_old = sgd.getFocusY();

      foc_x_old /= scale;
      foc_y_old /= scale;

      foc_x_old += xLoc;
      foc_y_old += yLoc;

      return true;
   }

   @Override
   public void onScaleEnd( ScaleGestureDetector sgd )
   {
   }

   public static float metersToPixels( float meters )
   {
      return meters * 33.1f;
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
