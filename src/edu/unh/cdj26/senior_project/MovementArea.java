package edu.unh.cdj26.senior_project;

import android.graphics.*;
import java.util.*;

public class MovementArea
{
   private Rect bounds;
   private ArrayList<PointF> dirs;

   private boolean arc = false;
   private PointF prevPoint;
   
   public MovementArea( int l, int t, int r, int b, PointF dir )
   {
      bounds = new Rect( l, t, r, b );

      dirs = new ArrayList<PointF>();

      addDirection( dir );
   }

   public void addDirection( PointF dir )
   {
      dir.x /= dir.length();
      dir.y /= dir.length();

      dirs.add( dir );
   }

   private Path intersectsCircle( float x, float y, float r )
   {
      PointF[] top    = circleHorizLineIntersection( x, y, r, bounds.left, bounds.right, bounds.top ); 
      PointF[] bottom = circleHorizLineIntersection( x, y, r, bounds.left, bounds.right, bounds.bottom );
      PointF[] left   = circleVertLineIntersection( x, y, r, bounds.top, bounds.bottom, bounds.left ); 
      PointF[] right  = circleVertLineIntersection( x, y, r, bounds.top, bounds.bottom, bounds.right ); 




      if( top == null && bottom == null && left == null && right == null )
      {
         if( bounds.top < y - r && bounds.bottom > y + r )
         {
            Path ret = new Path();
            ret.addCircle( x, y, r, Path.Direction.CW );
            ret.close();
            return ret;
         }
         else
            return null;
      }
      
      // switch indexes for the bottom and left sides because we are working clockwise
      if( bottom != null )
      {
         PointF temp = bottom[0];
         bottom[0] = bottom[1];
         bottom[1] = temp;
      }

      if( left != null )
      {
         PointF temp = left[0];
         left[0] = left[1];
         left[1] = temp;
      }
      
      PointF[][] intersects = { top, right, bottom, left };
      
      int start = 0;

      for( int i = 0; i < intersects.length; ++i )
         if( intersects[i] != null )
         {
            start = i;
            break;
         }

      Path p = new Path();
      p.moveTo( intersects[start][0].x, intersects[start][0].y );
      p.lineTo( intersects[start][1].x, intersects[start][1].y );
      prevPoint = intersects[start][1];
      
      arc = false;

      for( int i = 1; i < intersects.length; ++i )
      {
         int index = (i+start) % intersects.length;
         PointF[] side = intersects[index];
         if( side == null )
            arc = true;
         else if( arc || ! pointsEqual( prevPoint, side[0], 0.01 ) )
         {
            float start_deg = (float) Math.toDegrees( Math.atan2( prevPoint.y - y, prevPoint.x - x ) );
            float end_deg = (float) Math.toDegrees( Math.atan2( side[0].y - y, side[0].x - x ) );

            float sweep_deg = end_deg - start_deg;
            while( sweep_deg < 0 )
               sweep_deg += 360;

            p.arcTo( new RectF( x - r, y - r, x + r, y + r ), start_deg, sweep_deg );
            p.lineTo( side[1].x, side[1].y );
            prevPoint = side[1];
            arc = false;
         }
         else
         {
            p.lineTo( side[1].x, side[1].y );
            prevPoint = side[1];
         }
      }

      if( arc || ! pointsEqual( prevPoint, intersects[start][0], 0.01 ) )
      {
         float start_deg = (float) Math.toDegrees( Math.atan2( prevPoint.y - y, prevPoint.x - x ) );
         float end_deg = (float) Math.toDegrees( Math.atan2( intersects[start][0].y - y, intersects[start][0].x - x ) );

         float sweep_deg = end_deg - start_deg;
         while( sweep_deg < 0 )
            sweep_deg += 360;


         p.arcTo( new RectF( x - r, y - r, x + r, y + r ), start_deg, sweep_deg );
      }

      p.close();
      return p;


      
   }

   public static PointF[] circleHorizLineIntersection(
      float xc, float yc, float r, float begin, float end, float yl )
   {
      PointF[] ret = { null, null };

      double disc = r*r - Math.pow( yl - yc, 2 );
      if( disc <= 0 )
         return null;

      double x_calc = xc - Math.sqrt( disc );
      ret[ 0 ] = new PointF( (float) Math.max( begin, x_calc ), yl );

      x_calc = xc + Math.sqrt( disc );
      ret[ 1 ] = new PointF( (float) Math.min( end, x_calc ), yl );

      if( ret[0].x > ret[1].x )
         return null; // outside
      
      return ret;
   }

   public static PointF[] circleVertLineIntersection(
      float xc, float yc, float r, float begin, float end, float xl )
   {
      PointF[] ret = { null, null };

      double disc = r*r - Math.pow( xl - xc, 2 );
      if( disc <= 0 )
         return null;

      double y_calc = yc - Math.sqrt( disc );
      ret[ 0 ] = new PointF( xl, (float) Math.max( begin, y_calc ) );

      y_calc = yc + Math.sqrt( disc );
      ret[ 1 ] = new PointF( xl, (float) Math.min( end, y_calc ) );
      if( ret[0].y > ret[1].y )
         return null; // outside
      
      return ret;
   }

   private float getProbability( PointF dir )
   {
      if( dir.length() == 0 )
         return Float.MIN_VALUE;

      dir.x /= dir.length();
      dir.y /= dir.length();

      float max = Float.MIN_VALUE;
      
      for( PointF d : dirs )
      {
         float dot_prod = Math.abs( d.x * dir.x + d.y * dir.y );
         if( dot_prod > max )
            max = dot_prod;
      }
         
      return max;
   }

   public Intersection getIntersection( float x, float y, float r,
                                        PointF dom )
   {
      Path p = intersectsCircle( x, y, r );
      if( p == null )
         return null;

      float prob = getProbability( dom );
      
      return new Intersection( p, prob );

   }

   public Rect getBounds()
   {
      return bounds;
   }

   public class Intersection
   {
      public float probability;
      public Path path;

      public Intersection( Path p, float prob )
      {
         path = new Path( p );
         probability = prob;
      }
   }

   private static boolean pointsEqual( PointF p1, PointF p2, double error )
   {
      return Math.abs( p1.x - p2.x ) < error && Math.abs( p1.y - p2.y ) < error;
   }
}
