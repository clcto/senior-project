package edu.unh.cdj26.senior_project;

import android.content.*;
import android.graphics.drawable.*;
import android.view.*;
import android.graphics.*;
import java.util.*;
import java.io.*;


public class BuildingMap extends View
               implements GestureDetector.OnGestureListener,
                          ScaleGestureDetector.OnScaleGestureListener
                        
{
   private static BuildingMap instance;
   public static synchronized BuildingMap instance()
   {
      if( instance == null )
         if( context != null )
            instance = new BuildingMap( context );
      return instance;
   }

   public static void setContext( Context c )
   {
      context = c;
   }


   private ArrayList<MovementArea> areas;

   private Drawable mapImage;
   private int mapWidth, mapHeight;
   private float xLoc = 0, yLoc = 0;
   private float xScreen, yScreen;
   private Location locRelative = Location.UpperLeft;
   private GestureDetector gestures;
   private ScaleGestureDetector scaleDetector;
   private boolean newWifiData = false;
   private float scale = 0.2f;
   private PointF userLocation, prev_userLocation, new_userLocation;
   private float userOrientation;
   private float userRad = 0, prev_userRad = 0, new_userRad = 0;
   private boolean scaleToFit = true;

   private PointF direction = new PointF( 0, 0 );

   private boolean isMoving = false;

   private float foc_x_old, foc_y_old;

   private static Context context;

   public void removeFromParent()
   {
      ViewGroup parent = (ViewGroup) getParent();
      if( parent != null )
         parent.removeView( this );
   }

      // access points will be passed in
   private BuildingMap( Context c )
   {
      super( c );

      mapImage = c.getResources().getDrawable( 
                  R.drawable.floorplan );

      Bitmap bitmap = BitmapFactory.decodeResource(
         getResources(), R.drawable.floorplan );

            // still dont know why i need this 1.5 bullshit
      mapWidth  = (int) ( bitmap.getWidth() / 1.5 );
      mapHeight = (int) ( bitmap.getHeight() / 1.5 );
      
      mapImage.setBounds( 0, 0, mapWidth, mapHeight );

      gestures = new GestureDetector( c, this ); 
      scaleDetector = new ScaleGestureDetector( c, this );

      createMovementAreas();
      
      setCenterPixel( mapWidth/2, mapHeight/2 );
   }

   public void reset()
   {
      scaleToFit = true;
      setCenterPixel( mapWidth/2, mapHeight/2 );
   }
   
   private void drawMapCoord( Canvas screen, float scale_width, float icon_scale )
   {

      Paint brush = new Paint();
      brush.setDither( true );
      brush.setAntiAlias( true );
      brush.setStrokeJoin( Paint.Join.ROUND );
      brush.setStrokeCap( Paint.Cap.ROUND );
      brush.setColor( 0xFF333333 );
      brush.setStyle( Paint.Style.STROKE );
      brush.setStrokeWidth( scale_width );

      screen.drawRGB( 200, 200, 200 );
      mapImage.draw( screen );

      screen.drawRect( -1, -1, mapWidth, mapHeight, brush );

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
         m.preScale( icon_scale, icon_scale );

         Path ap_tri = new Path(); 
         triangle.transform( m, ap_tri );

         if( ap.hasNewLevel() )
         {
            brush.setColor( 0x88DD7700 );
            brush.setStyle( Paint.Style.STROKE );
            screen.drawPath( ap_tri, brush );
         
            brush.setColor( 0x44DD7700 );
            brush.setStyle( Paint.Style.FILL );
            screen.drawPath( ap_tri, brush );

            double rad_p = ap.getApproxRadiusPixels();

            brush.setColor( 0xAAFF0000 );
            brush.setStyle( Paint.Style.STROKE );
            screen.drawCircle( (float) apX,
                               (float) apY, 
                               (float) rad_p,
                               brush );

         }
      }

      if( userLocation != null )
      {
         brush.setColor( 0x442222CC );
         brush.setStyle( Paint.Style.FILL );
         screen.drawCircle( userLocation.x,
                            userLocation.y,
                            userRad, brush );

         Matrix m = new Matrix();
         m.preTranslate( userLocation.x, userLocation.y );
         m.preRotate( userOrientation );
         m.preScale( icon_scale, icon_scale );

         Path user_icon = new Path();
         user_icon.arcTo( new RectF( -7, -7, 7, 7 ), -65, 310 );
         user_icon.lineTo( 0, -14 );
         user_icon.close();

            Path scrn_user = new Path();
            user_icon.transform( m, scrn_user );

         if( isMoving )
         {
            brush.setColor( 0xFFFFFF00 );
            screen.drawPath( scrn_user, brush );
         }

         //debug
         if( direction != null )
            screen.drawLine( userLocation.x, userLocation.y,
                             userLocation.x + direction.x, userLocation.y + direction.y, brush );
         
         brush.setColor( 0xFF2222CC );
         brush.setStyle( Paint.Style.STROKE );
         screen.drawPath( scrn_user, brush );

         for( MovementArea ma : areas )
         {
            MovementArea.Intersection i = ma.getIntersection( userLocation.x, userLocation.y, 
                                                              userRad, new PointF( 1, 0 ) );
            if( i != null )
               screen.drawPath( i.path, brush );
         }
      }

      brush.setColor( 0xFF333333 );
      brush.setStyle( Paint.Style.STROKE );

      if( prev_userLocation != null )
         screen.drawCircle( prev_userLocation.x,
                            prev_userLocation.y,
                            prev_userRad, brush );

      if( new_userLocation != null )
         screen.drawCircle( new_userLocation.x,
                            new_userLocation.y,
                            new_userRad, brush );
   }

   @Override
   protected void onDraw( Canvas screen )
   {
      super.onDraw( screen );
      
      screen.save();
      
      if( scaleToFit )
      {
         scale = (float) Math.min( (float) getWidth()/(mapWidth+50), (float) getHeight()/(mapHeight+50) );
         scaleToFit = false;
      }

      screen.scale( scale, scale );
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
      
      screen.translate( -xLoc, -yLoc );

      drawMapCoord( screen, 2/scale, 1/scale );

      screen.restore();
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

   public void newAccelerometerData( float deg, boolean moving )
   {
      userOrientation = deg;
      isMoving = moving;
      invalidate();
   }

   public void newData( double px, double py, double pr, 
                        double nx, double ny, double nr,
                        double cx, double cy, double cr, PointF dom )
   {
      newData( (float) px, (float) py, (float) pr,
               (float) nx, (float) ny, (float) nr,
               (float) cx, (float) cy, (float) cr, dom );
   }

   public void newData( float px, float py, float pr, 
                        float nx, float ny, float nr,
                        float cx, float cy, float cr, PointF dom )
   {
      newWifiData = true;
      userLocation = new PointF( cx, cy );
      userRad = cr;

      prev_userLocation = new PointF( px, py );
      prev_userRad = pr;

      new_userLocation = new PointF( nx, ny );
      new_userRad = nr;

      if( dom != null )
      {
         prev_userLocation.offset( dom.x, dom.y );
         direction.set( dom );
      }
      else
         direction.set( 0, 0 );

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

   public boolean saveToFile( OutputStream file )
   {
      Bitmap image = Bitmap.createBitmap( (mapWidth + 600), (mapHeight + 600), Bitmap.Config.ARGB_8888 );
      Canvas canvas = new Canvas( image );

      canvas.translate( 300, 300 );

      drawMapCoord( canvas, 7, 4 );


      boolean ret = true;

      try
      {
         try
         {
            image.compress( Bitmap.CompressFormat.PNG, 30, file );

            file.flush();
         }
         catch( IOException e ){ ret = false; }
         finally
         {
            file.close();
         }
      }
      catch( IOException e )
      {
         ret = false;
      }

      return ret;
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

   public static double metersToPixels( double meters )
   {
      return meters * 33.1;
   }

   public static float toScreenAngle( float orientation )
   {
      return orientation - 40;
   }

   private void createMovementAreas()
   {
      areas = new ArrayList<MovementArea> ();

      MovementArea a;
      a = new MovementArea( 120, 771, 635, 896, new PointF( 1, 0 ) );
      areas.add( a );

      a = new MovementArea( 0, 660, 100, 770, new PointF( 0, 1 ) );
      a.addDirection( new PointF( 2, 1 ) );
      areas.add( a );

      a = new MovementArea( 636, 686, 824, 770, new PointF( 1, 0 ) );
      areas.add( a );

      a = new MovementArea( 636, 806, 737, 896, new PointF( 1, 0 ) );
      a.addDirection( new PointF( 0, 1 ) );
      areas.add( a );

      a = new MovementArea( 636, 771, 732, 805, new PointF( 0, 1 ) );
      areas.add( a );

      a = new MovementArea( 593, 897, 725, 1173, new PointF( 0, 1 ) );
      areas.add( a );

      a = new MovementArea( 738, 811, 903, 881, new PointF( 1, 0 ) );
      areas.add( a );

      a = new MovementArea( 904, 851, 1036, 881, new PointF( 1, 0 ) );
      areas.add( a );

      a = new MovementArea( 1006, 811, 1036, 850, new PointF( 1, 0 ) );
      a.addDirection( new PointF( 1, 2 ) );
      areas.add( a );
      
      a = new MovementArea( 904, 811, 1006, 850,  new PointF( 1, 0 ) );
      a.addDirection( new PointF( 0, 1 ) );
      areas.add( a );

      a = new MovementArea( 933, 435, 1006, 810, new PointF( 0, 1 ) );
      areas.add( a );

      a = new MovementArea( 903, 716, 932, 810,  new PointF( -1, 1 ) );
      areas.add( a );

      a = new MovementArea( 1037, 826, 1537, 881, new PointF( 1, 0 ) );
      areas.add( a );

      a = new MovementArea( 1537, 853, 1664, 881, new PointF( 1, 0 ) );
      areas.add( a );

      a = new MovementArea( 1537, 0, 1613, 787, new PointF( 0, 1 ) );
      areas.add( a );

      a = new MovementArea( 1537, 788, 1613, 852, new PointF( 0, 1 ) );
      a.addDirection( new PointF( 1, 0 ) );
      areas.add( a );

      a = new MovementArea( 1613, 788, 1664, 852, new PointF( 1, 0 ) );
      a.addDirection( new PointF( 1, 1 ) );
      areas.add( a );

      a = new MovementArea( 1488, 788, 1537, 852, new PointF( 1, 0 ) );
      a.addDirection( new PointF( -1, 1 ) );
      areas.add( a );

      a = new MovementArea( 1665, 826, 2615, 881, new PointF( 1, 0 ) );
      areas.add( a );

      a = new MovementArea( 2616, 810, 2969, 881, new PointF( 1, 0 ) );
      areas.add( a );

      a = new MovementArea( 0, 830, 120, 896, new PointF( 1, 0 ) );
      areas.add( a );

      a = new MovementArea( 0, 771, 120, 829, new PointF( 0, 1 ) );
      a.addDirection( new PointF( 1, 0 ) );
      areas.add( a );
   }
}
