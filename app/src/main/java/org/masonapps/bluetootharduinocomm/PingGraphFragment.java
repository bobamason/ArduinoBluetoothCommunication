package org.masonapps.bluetootharduinocomm;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import org.masonapps.bluetootharduinocomm.bluetooth.BluetoothService;
import org.masonapps.bluetootharduinocomm.views.GraphView;


public class PingGraphFragment extends Fragment {

    private static final String ARG_ADDRESS = "address";
    private BluetoothService bluetoothService;
    private String address;
    private ProgressBar progressBar;
    private GraphView graphView;
    
    /*
    private final Handler handler = new Handler() {

        private StringBuffer stringBuffer = new StringBuffer();
        private int startIndex = 0;
        private int endIndex = 0;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case BluetoothService.MESSAGE_READ:
                    final String read = new String((byte[]) msg.obj, 0, msg.arg1);
                    Log.d(PingGraphFragment.class.getSimpleName(), "reading: " + read);
                    stringBuffer.append(read);
                    final int tmpStartIndex = stringBuffer.indexOf("{", startIndex);
                    if(tmpStartIndex > startIndex){
                        startIndex = tmpStartIndex;
                    }
                    final int tmpEndIndex = stringBuffer.indexOf("}", endIndex);
                    if(tmpEndIndex > endIndex) {
                        endIndex = tmpEndIndex;
                        final String substring = stringBuffer.substring(startIndex + 1, endIndex);
                        Log.d(PingGraphFragment.class.getSimpleName(), substring);
                        try {
                            graphView.updateValue(Float.parseFloat(substring));
                        } catch (NumberFormatException e) {
                            Log.e(PingGraphFragment.class.getSimpleName(), "unable to parse number: " + substring, e);
                        }
                    }
                    break;
                case BluetoothService.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
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
     */

    private final Handler handler = new BluetoothService.BluetoothHandler(new BluetoothService.HandlerCallback() {

        public String read;
        private StringBuffer stringBuffer = new StringBuffer();
        private int startIndex = 0;
        private int endIndex = 0;

        @Override
        public void onRead(byte[] buffer, int bytes) {
            read = new String(buffer, 0, bytes);
            stringBuffer.append(read);
            startIndex = stringBuffer.lastIndexOf("{") + 1;
            endIndex = stringBuffer.indexOf("}", startIndex);
            if (startIndex < endIndex) {
                final String substring = stringBuffer.substring(startIndex, endIndex);
                try {
                    graphView.updateValue(Integer.parseInt(substring));
                } catch (NumberFormatException e) {
                    Log.e(PingGraphFragment.class.getSimpleName(), "unable to parse number: " + substring, e);
                }
            }
        }

        @Override
        public void onStateChange(int state) {
            switch (state) {
                case BluetoothService.STATE_NONE:
                    progressBar.setVisibility(View.VISIBLE);
                    break;
                case BluetoothService.STATE_CONNECTING:
                    break;
                case BluetoothService.STATE_CONNECTED:
                    progressBar.setVisibility(View.GONE);
                    bluetoothService.write("connected".getBytes());
                    break;
            }
        }
    });

    public PingGraphFragment() {
        // Required empty public constructor
    }

    public static PingGraphFragment newInstance(String address) {
        PingGraphFragment fragment = new PingGraphFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ADDRESS, address);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            address = getArguments().getString(ARG_ADDRESS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_ping_graph, container, false);
        progressBar = (ProgressBar) view.findViewById(R.id.connection_progress);
        graphView = (GraphView) view.findViewById(R.id.ping_graph);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        bluetoothService = new BluetoothService(handler, BluetoothAdapter.getDefaultAdapter());
        bluetoothService.connect(address);

    }

    @Override
    public void onStop() {
        if (bluetoothService != null) {
            bluetoothService.write("disconnected".getBytes());
            bluetoothService.stop();
        }
        super.onStop();
    }
    
    /*
    ARDUINO CODE
    
const int trigPin = 7;
const int echoPin = 8;
boolean ok = false;
long duration, inches, cm;
String str = "";

void setup() {
  pinMode(trigPin, OUTPUT);
  pinMode(echoPin, INPUT);
  Serial.begin(9600);
  Serial.println("{0}");
  str.reserve(100);
}

void loop()
{
  if (ok) {
    digitalWrite(trigPin, LOW);
    delayMicroseconds(2);
    digitalWrite(trigPin, HIGH);
    delayMicroseconds(5);
    digitalWrite(trigPin, LOW);

    duration = pulseIn(echoPin, HIGH);

    inches = microsecondsToInches(duration);
    cm = microsecondsToCentimeters(duration);

    Serial.print('{');
    Serial.print(cm);
    Serial.print('}');
    Serial.println();

    delay(100);
  }
}

void serialEvent() {
  if (Serial.available()) {
    str = Serial.readStringUntil('\n');
    if (str.equals("connected")) {
      ok = true;
    }
    if (str.equals("disconnected")) {
      ok = false;
    }
  }
}

long microsecondsToInches(long microseconds)
{
  return microseconds / 74 / 2;
}

long microsecondsToCentimeters(long microseconds)
{
  return microseconds / 29 / 2;
}
    
     */
}
