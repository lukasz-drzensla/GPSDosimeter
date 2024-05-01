package pl.edu.agh.gpsdosimeter;

import static androidx.core.app.ActivityCompat.*;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;


public class BTTools {

    class ConnInfo {
        private String name = "";
        private String address = "";
        public ConnInfo(String _name, String _address) {
            this.name = _name;
            this.address = _address;
        }

        public String getName() {
            return this.name;
        }

        public String getAddress() {
            return this.address;
        }
    }

    public static enum permission_codes {
        BT,
        BT_ADMIN,
        BT_CONN,
        EXT_ST
    };

    static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private Set<BluetoothDevice> pairedDevices;

    private boolean conn = false;

    private int ubyteToInt (byte b)
    {
        return b & 0xFF;
    }

    public ConnInfo connect(Activity parent, BTCb btCb) throws IOException {
        String address = "";
        String name = "";
        try {
            btAdapter = BluetoothAdapter.getDefaultAdapter();

            if (checkSelfPermission(parent, android.Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_DENIED) {
                Log.e("BT Connect", "Insufficient permissions.");
                return new ConnInfo("Error", "Error");
            }
            pairedDevices = btAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice bt : pairedDevices) {
                    address = bt.getAddress().toString();
                    name = bt.getName().toString();
                    Log.i("name: ", name);
                }
            }

        }
        catch(Exception we){}
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice btDevice = btAdapter.getRemoteDevice(address);
        btSocket = btDevice.createInsecureRfcommSocketToServiceRecord(uuid);
        btSocket.connect();

        Thread receiveThread = new Thread(() -> {
            InputStream socketInputStream = null;
            try {
                socketInputStream = btSocket.getInputStream();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            byte[] buffer = new byte[256];
            int[] frame = new int[JRadicom.RC.FRAME_SIZE];
            boolean got_begin = false;
            int frame_bytes = 0;
            int bytes;
            while (true) {
                try {
                    bytes = socketInputStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    Log.i("logging", readMessage + "");
                    if (ubyteToInt(buffer[0]) == 219)
                    {
                        Log.e("Recv", "Frame begin");
                        got_begin = true;
                    }
                    if (got_begin)
                    {
                        for (int i = 0; i < bytes; i++)
                        {
                            frame[frame_bytes+i] = ubyteToInt(buffer[i]);
                        }
                        frame_bytes += bytes;
                    }
                    if (frame_bytes >= 100)
                    {
                        frame_bytes = 0;
                        got_begin = false;
                        //decode frame
                        Log.i("Recv", "Decode frame");
                        btCb.notify(frame);
                        frame = new int[JRadicom.RC.FRAME_SIZE];
                    }
                } catch (IOException e) {
                    break;
                }
            }
        });
        receiveThread.start();
        conn = true;
        return new ConnInfo(name, address);
    }

    void write(byte[] msg)
    {
        if (conn)
        {
            if (btSocket!=null)
            {
                try {
                    btSocket.getOutputStream().write(msg);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public boolean isConnected()
    {
        return conn;
    }

}