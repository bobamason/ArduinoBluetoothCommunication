package org.masonapps.bluetootharduinocomm;


import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import org.masonapps.bluetootharduinocomm.bluetooth.BluetoothService;

public class ServoFragment extends Fragment {
    private static final String ARG_ADDRESS = "address";
    private BluetoothService bluetoothService;
    private String address;
    private ProgressBar progressBar;
    private StringBuilder stringBuilder = new StringBuilder();
    private TextView textView;
    
    private final Handler handler = new Handler(){
        
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case BluetoothService.MESSAGE_READ:
                    String read = new String((byte[]) msg.obj, 0, msg.arg1);
                    stringBuilder.append(read);
                    textView.setText(stringBuilder);
                    scrollView.fullScroll(View.FOCUS_DOWN);
                    break;
                case BluetoothService.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1){
                        case BluetoothService.STATE_NONE:
                            progressBar.setVisibility(View.VISIBLE);
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            break;
                        case BluetoothService.STATE_CONNECTED:
                            progressBar.setVisibility(View.GONE);
                            break;
                    }
                    break;
            }
        }
    };
    private SeekBar seekbar;
    private ScrollView scrollView;

    public static ServoFragment newInstance(String address) {
        ServoFragment fragment = new ServoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ADDRESS, address);
        fragment.setArguments(args);
        return fragment;
    }

    public ServoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            address = getArguments().getString(ARG_ADDRESS);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_servo, container, false);
        progressBar = (ProgressBar) view.findViewById(R.id.connection_progress);
        textView = (TextView) view.findViewById(R.id.servo_textview);
        scrollView = (ScrollView) view.findViewById(R.id.servo_scrollview);
        seekbar = (SeekBar) view.findViewById(R.id.servo_seekbar);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public int currentAngle;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentAngle = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                write(Integer.toString(currentAngle) + "\n");
            }
        });
        return view;
    }
    
    private void write(String s){
        if(bluetoothService != null){
            bluetoothService.write(s.getBytes());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        bluetoothService = new BluetoothService(handler, BluetoothAdapter.getDefaultAdapter());
        bluetoothService.connect(address);
        
    }

    @Override
    public void onStop() {
        if(bluetoothService != null){
            bluetoothService.stop();
        }
        super.onStop();
    }
}
