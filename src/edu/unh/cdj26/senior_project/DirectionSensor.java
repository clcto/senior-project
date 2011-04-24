
package edu.unh.cdj26.senior_project;

import android.hardware.*;
import android.content.*;
import android.os.*;
import android.app.*;
import java.util.*;

public class DirectionSensor extends Service
{
   private float[] gravity;
   private long accel_time;
   private float[] geomagnetic;
   private SensorManager sm;
   private SensorEventListener gravListener, geomagListener, accelListener;

   @Override
   public IBinder onBind( Intent intent )
   {
      return null;
   }

   @Override
   public void onCreate()
   {

   }

   @Override
   public int onStartCommand( Intent intent, 
                              int flags, int startid )
   {
      handleCommand( intent );

      return START_STICKY;
   }

   @Override
   public void onStart( Intent intent, int startid )
   {
      handleCommand( intent );
   }

   private void handleCommand( Intent intent )
   {
      gravity = new float[3];
      geomagnetic = new float[3];

      sm = (SensorManager) getApplicationContext().getSystemService( Context.SENSOR_SERVICE );
      Sensor grav, geomag;

      grav = sm.getDefaultSensor( Sensor.TYPE_ACCELEROMETER );
      gravListener = new SensorEventListener()
         {
            @Override
            public void onAccuracyChanged( Sensor s, int a ){}

            @Override
            public void onSensorChanged( SensorEvent se )
            {
               gravity = se.values;
               accel_time = se.timestamp;
               update();
            }
         };
      sm.registerListener( gravListener, grav, SensorManager.SENSOR_DELAY_NORMAL );

      geomag = sm.getDefaultSensor( Sensor.TYPE_MAGNETIC_FIELD );
      geomagListener = new SensorEventListener()
         {
            @Override
            public void onAccuracyChanged( Sensor s, int a ){}

            @Override
            public void onSensorChanged( SensorEvent se )
            {
               geomagnetic = se.values;
               update();
            }
         };
      sm.registerListener( geomagListener, geomag, SensorManager.SENSOR_DELAY_NORMAL );
   }
   
   private void update()
   {
      float[] R = new float[9];
      float[] I = new float[9];
      float[] values = new float[3];

      if( SensorManager.getRotationMatrix( R, I, gravity, geomagnetic ) )
      {
         SensorManager.getOrientation( R, values );
         
         Intent done = new Intent( "COMPASS_DATA_PROCESSED" );
         done.putExtra( "orientation", (float) Math.toDegrees( values[0] ) );
         done.putExtra( "accelerometer", gravity );
         done.putExtra( "timestamp", accel_time );
         sendBroadcast( done );
      }
   }


}
