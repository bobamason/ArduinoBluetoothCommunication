package org.masonapps.bluetootharduinocomm.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.UUID;

/**
 * Created by Bob on 7/21/2015.
 */
public class BluetoothService {

    public static final UUID MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    public static final int MESSAGE_READ = 1;
    public static final int MESSAGE_STATE_CHANGE = 2;
    public static final int STATE_NONE = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;
    private static final String TAG = "BluetoothService";
    private ConnectThread connectThread = null;
    private CommunicationThread communicationThread = null;
    private final BluetoothAdapter bluetoothAdapter;
    private final Handler handler;
    private int state;

    public BluetoothService(Handler handler, BluetoothAdapter bluetoothAdapter) {
        this.handler = handler;
        this.bluetoothAdapter = bluetoothAdapter;
        setState(STATE_NONE);
    }

    public void connect(String address) {
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
        if (communicationThread != null) {
            communicationThread.cancel();
            communicationThread = null;
        }

        Log.d(TAG, "mac address: " + address);
        connectThread = new ConnectThread(address);
        connectThread.start();
    }
    
    public void write(byte[] buffer){
        if(communicationThread == null) return;
        CommunicationThread r;
        
        synchronized (this){
            r = communicationThread;
        }
        
        r.write(buffer);
    }

    public void stop() {
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
        if (communicationThread != null) {
            communicationThread.cancel();
            communicationThread = null;
        }
        
        setState(STATE_NONE);
    }

    private void connected(BluetoothSocket socket) {

        if (communicationThread != null) {
            communicationThread.cancel();
            communicationThread = null;
        }
        
        communicationThread = new CommunicationThread(socket);
        communicationThread.start();
    }

    private void connectionFailed(String address) {
        Log.e(TAG, "connection failed");
        connect(address);
    }
    
    private void setState(int state){
        this.state = state;
        handler.obtainMessage(MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    private class ConnectThread extends Thread {

        private final BluetoothSocket socket;
        private final String address;

        public ConnectThread(String address) {
            this.address = address;
            BluetoothSocket tmpSocket = null;
            try {
                final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
                Log.d(TAG, "device name: " + device.getName() + " address: " + device.getAddress());
                ParcelUuid[] parcelUuids = device.getUuids();
                UUID uuid;
                if(parcelUuids != null && parcelUuids.length > 0) {
                    uuid = parcelUuids[0].getUuid();
                } else {
                    uuid = MODULE_UUID;
                }
                Log.d(TAG, "device uuid: "  + uuid.toString());
                tmpSocket = device.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
                Log.e(TAG, "failed to create socket", e);
            }
            socket = tmpSocket;
        }

        @Override
        public void run() {
//            bluetoothAdapter.cancelDiscovery();
            if (socket != null) {
                try {
                    socket.connect();
                    Log.d(TAG, "connecting...");
                    setState(STATE_CONNECTING);
                } catch (IOException e) {
                    Log.e(TAG, "failed to connect socket", e);
                    try {
                        socket.close();
                    } catch (IOException e1) {
                        Log.e(TAG, "failed to close socket", e1);
                    }
                    connectionFailed(address);
                    return;
                }

                synchronized (BluetoothService.this) {
                    connectThread = null;
                }

                connected(socket);
                Log.d(TAG, "connected");
                setState(STATE_CONNECTED);
            }
        }

        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "failed to close socket", e);
            }
        }
    }

    private class CommunicationThread extends Thread {

        private final BluetoothSocket socket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public CommunicationThread(BluetoothSocket socket) {
            this.socket = socket;
            InputStream tmpInStream = null;
            OutputStream tmpOutStream = null;
            try {
                tmpInStream = socket.getInputStream();
                tmpOutStream = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "failed to get streams", e);
            }
            inputStream = tmpInStream;
            outputStream = tmpOutStream;
        }

        @Override
        public void run() {
            int bytes;
            byte[] buffer = new byte[1024];
            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                    handler.obtainMessage(BluetoothService.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "failed to read inputstream", e);
                    break;
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                outputStream.write(buffer);
                Log.d(TAG, "writing: " + new String(buffer));
            } catch (IOException e) {
                Log.e(TAG, "failed to write outputstream", e);
            }
        }


        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "failed to close socket", e);
            }
        }
    }
    
    public static class BluetoothHandler extends Handler{
        
        private final WeakReference<HandlerCallback> callbackWeakReference;

        public BluetoothHandler(HandlerCallback callback) {
            callbackWeakReference = new WeakReference<>(callback);
        }

        @Override
        public void handleMessage(Message msg) {
            final HandlerCallback callback = callbackWeakReference.get();
            if(callback == null) return;
            switch (msg.what){
                case BluetoothService.MESSAGE_READ:
                    callback.onRead((byte[]) msg.obj, msg.arg1);
                    break;
                case BluetoothService.MESSAGE_STATE_CHANGE:
                    callback.onStateChange(msg.arg1);
                    break;
            }
        }
    }
    
    public interface HandlerCallback{
        void onRead(byte[] buffer, int bytes);
        void onStateChange(int state);
    }
}
