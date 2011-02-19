package edu.unh.cdj26.senior_project;

import android.app.Activity;
import android.os.Bundle;
import android.widget.*;
import android.view.*;
import android.net.wifi.*;
import android.content.Context;
import android.content.Intent;
import java.util.List;
import java.io.*;
import android.os.*;

public class IndoorLocalization extends Activity
{
   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      setContentView( new BuildingMap( this ) );

      /*
      Button submit = (Button) findViewById( R.id.record_button );
      submit.setOnClickListener( 
         new View.OnClickListener()
         {
            public void onClick( View v )
            {
               CheckBox overwrite = (CheckBox) findViewById( R.id.overwrite );
               if( overwrite.isChecked() )
               {
                  File f = new File( Environment.getExternalStorageDirectory(),
                                     "wifi_data.csv" );
                  f.delete();
                  overwrite.setChecked( false );
               }

               go();
            }
         } );
      */
   }

   private void go()
   {
      EditText v = (EditText) findViewById( R.id.height_entry );
      EditText h = (EditText) findViewById( R.id.horizontal_entry );
      EditText m = (EditText) findViewById( R.id.mac_entry );

      Intent serviceIntent = new Intent( this, WifiService.class );

      try{
         double vert = Double.valueOf( v.getText().toString() );
         double horiz = Double.valueOf( h.getText().toString() );

         serviceIntent.putExtra( "vert", vert );
         serviceIntent.putExtra( "horiz", horiz );
      }
      catch( NumberFormatException e ){}


      serviceIntent.putExtra( "mac", 
                              m.getText().toString() );
      startService( serviceIntent );
   }

   /*
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
   */
}
