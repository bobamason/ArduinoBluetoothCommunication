package org.masonapps.bluetootharduinocomm;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends AppCompatActivity implements DeviceListFragment.OnDeviceChosenListener{

    private static final String DEVICE_LIST_FRAGMENT = "deviceListTag";
    private static final String CONNECTION_FRAGMENT = "connectionFragment";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        if(savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, DeviceListFragment.newInstance(), DEVICE_LIST_FRAGMENT)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    public void onDevicePicked(BluetoothDevice device) {
//        getSupportFragmentManager()
//                .beginTransaction()
//                .replace(R.id.container, ServoFragment.newInstance(device.getAddress()), CONNECTION_FRAGMENT)
//                .commit();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, PingGraphFragment.newInstance(device.getAddress()), CONNECTION_FRAGMENT)
                .commit();
    }
}
