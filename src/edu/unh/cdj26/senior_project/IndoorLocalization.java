package edu.unh.cdj26.senior_project;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.net.wifi.*;
import android.content.Context;
import android.content.Intent;
import java.util.List;

public class IndoorLocalization extends Activity
{
   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
/*
      TextView tv = new TextView( this );
      tv.setText( "Scanning for Wifi Networks...\n" +
                  "\tDisplays best signals\n" );

      setContentView( tv );
*/
      BuildingMap map = new BuildingMap( this ); 
      setContentView( map );
      map.draw();
      map.setCenterPixel( 0, 0 );
   }

   @Override
   public void onStart()
   {
      startService( 
         new Intent( this, WifiService.class ) );
      super.onStart();
   }

   @Override
   public void onStop()
   {
      stopService( 
         new Intent( this, WifiService.class ) );
      super.onStop();
   }
}
