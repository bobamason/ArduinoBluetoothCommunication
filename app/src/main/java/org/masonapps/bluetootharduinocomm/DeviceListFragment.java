package org.masonapps.bluetootharduinocomm;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;


public class DeviceListFragment extends Fragment {
    private static final String ARG_LIST = "list";
    
    private static final int REQUEST_ENABLE_BT = 1001;
    private BluetoothAdapter bluetoothAdapter;
    private OnDeviceChosenListener mListener;
    private ArrayAdapter<String> deviceInfoListAdapter;
    private ArrayList<BluetoothDevice> deviceList = new ArrayList<>();
    private BroadcastReceiver receiver = null;

    public static DeviceListFragment newInstance() {
        DeviceListFragment fragment = new DeviceListFragment();
        return fragment;
    }

    public DeviceListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_device_list, container, false);
        
        deviceInfoListAdapter = new ArrayAdapter<>(getActivity().getApplicationContext(), R.layout.list_item);
        ListView listView = (ListView) view.findViewById(R.id.device_listview);
        listView.setAdapter(deviceInfoListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mListener != null) {
                    mListener.onDevicePicked(deviceList.get(position));
                }
            }
        });

        final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.scan_progress);
        
        view.findViewById(R.id.scan_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bluetoothAdapter.isDiscovering()){
                    bluetoothAdapter.cancelDiscovery();
                    ((Button)v).setText("Scan");
                    progressBar.setVisibility(View.GONE);
                } else {
                    if(receiver == null) {
                        receiver = new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context context, Intent intent) {
                                if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
                                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                                    addBluetoothDevice(device);
                                }
                            }
                        };
                        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                        getActivity().registerReceiver(receiver, filter);
                    }
                    boolean started = bluetoothAdapter.startDiscovery();
                    if (started){
                        ((Button)v).setText("Stop Scan");
                        progressBar.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(getActivity().getApplicationContext(), "unable to scan for devices", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter != null) {
            if(!bluetoothAdapter.isEnabled()){
                startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT);
            }else {
                populateList();
            }
        }else{
            Toast.makeText(getActivity(), "Bluetooth is not supported", Toast.LENGTH_LONG).show();
        }
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnDeviceChosenListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnDeviceChosenListener");
        }
    }

    @Override
    public void onPause() {
        if(receiver != null) getActivity().unregisterReceiver(receiver);
        if(bluetoothAdapter != null && bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
        }
        super.onPause();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK){
            populateList();
        }
    }

    private void populateList() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices != null && pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                addBluetoothDevice(device);
            }
        }
    }

    private void addBluetoothDevice(BluetoothDevice device) {
        deviceList.add(device);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(device.getName())
                .append("\n\tAddress: ")
                .append(device.getAddress());
        ParcelUuid[] uuids = device.getUuids();
        if(uuids != null && uuids.length != 0) {
            for (ParcelUuid uuid : uuids) {
                stringBuilder.append("\n\tuuid: ")
                        .append(uuid.getUuid().toString());
            }
        }
        deviceInfoListAdapter.add(stringBuilder.toString());
    }

    public interface OnDeviceChosenListener {
        void onDevicePicked(BluetoothDevice device);
    }

}
