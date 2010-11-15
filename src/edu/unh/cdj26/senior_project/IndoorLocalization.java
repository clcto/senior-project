package edu.unh.cdj26.senior_project;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.net.wifi.*;
import android.content.Context;
import java.util.List;

public class IndoorLocalization extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView( this );
        tv.setText( "start" );

        Context c = getApplicationContext();
        if( c != null )
        {
           tv.setText( "c != null" );
           WifiManager wm = 
               (WifiManager) c.getSystemService( Context.WIFI_SERVICE );
           if( wm != null )
           {
              tv.setText( "wm != null" );
              wm.startScan();
              try
              {
                 Thread.sleep( 10000 );
              }
              catch( Exception e ){}
              List<ScanResult> networks = wm.getScanResults();

              tv.setText( "Number of networks: " + networks.size() );
           }
        }

        setContentView( tv );
    }
}
