package bt.lcy.btread.use;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.UUID;

import adapters.BtDevicesAdapter;
import bt.lcy.btread.BtDeviceServicesActivity;
import bt.lcy.btread.BtService;
import bt.lcy.btread.R;

public class MainUseActivity extends AppCompatActivity {

    private final static String TAG = MainUseActivity.class.getSimpleName();
    private BtService btService;
    private MenuItem stopScann;



    private BluetoothAdapter bluetoothAdapter;
    private BtScanner btScanner;
    private BtDevicesAdapter btDevicesAdapter;
    private boolean isBinded = false;

    private  boolean isConnected;
    public static final String BLE_DEVICE ="AEAEAJBTC";
    public static final String BLE_ADDR = "06:05:04:04:03:09";
    public static final UUID UUID_CHAR_READ_NOTIFY = UUID.fromString("00000001-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_GATT_SERVICE_READ_NOTIFY = UUID.fromString("00000001-0000-1000-8000-00805f9b34fb");
    public static final int RETRY = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_main);


        getSupportActionBar().setTitle(R.string.init);



        // Example of a call to a native method
//        TextView tv = (TextView) findViewById(R.id.sample_text);
//        tv.setText(stringFromJNI());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_scan, menu);
       // menu.findItem(R.id.menu_about).setVisible(true);
        stopScann = menu.findItem(R.id.menu_scan);
        if (btScanner == null || !btScanner.isScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
//            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            // menu.findItem(R.id.menu_refresh).setActionView(null);
        }

        return true;
    }




    @Override
    protected void onResume() {
        super.onResume();


        //要注册才能开启使用
        registerReceiver(gattBroadcastReceiver,makeGattUpdateIntenFilter());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
        }

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        // buletooth not supported!!!
        if (bluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            final Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

            startActivityForResult(enableBtIntent, 1);
            return;
        }
        init();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_CANCELED) {
                finish();
            } else {
                init();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        Log.i(TAG,TAG + " onPause.....");
        if (btScanner != null) {
            btScanner.stopScanning();
            btScanner = null;
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(gattBroadcastReceiver);
        unbindService(serviceConnection);
    }


    private final ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            isBinded = true;
            btService = ((BtService.LocalBinder)service).getService();

            final String deviceAddress = getIntent().getStringExtra(BtDeviceServicesActivity.EXTRAS_DEVICE_ADDR);
            if(!btService.initialize())
            {
                Toast.makeText(MainUseActivity.this,R.string.error_bluetooth_initialize,Toast.LENGTH_SHORT).show();
                finish();
            }
            Log.i(TAG,"Connection Service");
//            btService.connect(deviceAddress);
            btService.connect();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.w(TAG,"Disconnection Service");
            Toast.makeText(MainUseActivity.this,R.string.srv_disconnect,Toast.LENGTH_SHORT).show();
          //  clearUI();

            btService = null;
        }
    };

//    @Override
//    protected void onListItemClick(ListView listView, View view, int position, long id) {
//
//        view.setSelected(true);
//        btDevicesAdapter.setProcessFlag(true);
//        if(isBinded)
//            unbindService(serviceConnection);
//
//        if (btScanner != null) {
//            btScanner.stopScanning();
//            btScanner = null;
//            invalidateOptionsMenu();
//        }
//        final BluetoothDevice device = btDevicesAdapter.getDevice(position);
//        if (device == null) return;
//
//
//        final Intent gattIntent = new Intent(this,BtService.class);
//        gattIntent.putExtra(BtDeviceServicesActivity.EXTRAS_DEVICE_NAME, device.getName());
//        gattIntent.putExtra(BtDeviceServicesActivity.EXTRAS_DEVICE_ADDR, device.getAddress());
//        bindService(gattIntent,serviceConnection,BIND_AUTO_CREATE);
//        Log.d(TAG, "Connect to device: " + device.getName() + " addr: " + device.getAddress());
//
//    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:

                if (btScanner == null) {
                    btScanner = new BtScanner(bluetoothAdapter, mLeScanCallback);
                    btScanner.startScanning();
                    invalidateOptionsMenu();
                }
                onResume();
                break;
            case R.id.menu_stop:
                if (btScanner != null) {
                    btScanner.stopScanning();
                    btScanner = null;
                    invalidateOptionsMenu();
                }
                onResume();
                break;
        }
        return true;
    }


    //搜索蓝牙的回调函数
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            btDevicesAdapter.addDevice(device, rssi);
//                            btDevicesAdapter.notifyDataSetChanged();
//                        }
//                    });
                }
            };

    private void init() {

//        if (btScanner == null) {
//            Toast.makeText(this, "Start Scanning....", Toast.LENGTH_SHORT).show();
//            btScanner = new BtScanner(bluetoothAdapter, mLeScanCallback);
//            btScanner.startScanning();
//        }
        invalidateOptionsMenu();
        //TODO 遍历整个列表，核实是否存在目标设备


        if(isBinded)
            unbindService(serviceConnection);

//        if (btScanner != null) {
//            btScanner.stopScanning();
//            btScanner = null;
//            invalidateOptionsMenu();
//        }


        final Intent gattIntent = new Intent(this,BtService.class);
        gattIntent.putExtra(BtDeviceServicesActivity.EXTRAS_DEVICE_NAME, BLE_DEVICE);
        gattIntent.putExtra(BtDeviceServicesActivity.EXTRAS_DEVICE_ADDR, BLE_ADDR);
        bindService(gattIntent,serviceConnection,BIND_AUTO_CREATE);
        Log.d(TAG, "Connect to device: " + BLE_DEVICE + " addr: " + BLE_ADDR);

//        //startActivity();
//        if(isConnected ){
//            if(handler!=null)
//            handler.removeCallbacks(runnable);
//        }else {
//            if(handler!=null)
//            handler.postDelayed(runnable, 3000);
//        }

    }




    private static IntentFilter makeGattUpdateIntenFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BtService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BtService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BtService.ACTION_GATT_SERVICES_DISCOVERED);
        return intentFilter;
    }

    private void startActivity()
    {


            BtDeviceServicesActivity.setBtService(btService);
            final Intent intent = new Intent(this, BtDeviceServicesActivity.class);
            intent.putExtra(BtDeviceServicesActivity.EXTRAS_DEVICE_NAME, BLE_DEVICE);
            intent.putExtra(BtDeviceServicesActivity.EXTRAS_DEVICE_ADDR, BLE_ADDR);
            //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

    }

    /**
     * 搜索蓝牙的线程
     */
    private static class BtScanner extends Thread {
        private final static String TAG = BtScanner.class.getSimpleName();

        private final BluetoothAdapter bluetoothAdapter;
        private final BluetoothAdapter.LeScanCallback mLeScanCallback;

        private volatile boolean isScanning = false;

        BtScanner(BluetoothAdapter adapter, BluetoothAdapter.LeScanCallback callback) {
            bluetoothAdapter = adapter;
            mLeScanCallback = callback;
        }

        public boolean isScanning() {
            return isScanning;
        }

        public void startScanning() {
            synchronized (this) {
                isScanning = true;
                start();
            }
        }

        public void stopScanning() {
            synchronized (this) {
                isScanning = false;
                bluetoothAdapter.stopLeScan(mLeScanCallback);
            }
        }

        @Override
        public void run() {
            try {
                Log.w(TAG, "start to scanning........");
                while (true) {
                    synchronized (this) {
                        if (!isScanning)
                            break;
                        bluetoothAdapter.startLeScan(mLeScanCallback);
                    }

                    sleep(1000*8);

                    synchronized (this) {
                        bluetoothAdapter.stopLeScan(mLeScanCallback);
                    }
                }
            } catch (InterruptedException ignore) {

            } finally {
                bluetoothAdapter.stopLeScan(mLeScanCallback);
            }
        }
    }




    private final BroadcastReceiver gattBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final  String action = intent.getAction();

            Log.i(TAG,"!!!!!Got a BroadCast: " + action);
            if(BtService.ACTION_GATT_SERVICES_DISCOVERED.equals(action))
            {
                startActivity();
            }

//            Log.i(TAG,"!!!!!Got a BroadCast: " + action);
            if(BtService.ACTION_GATT_CONNECTED.equals(action))
            {
                isConnected = true;
                invalidateOptionsMenu();
            }
            else if(BtService.ACTION_GATT_DISCONNECTED.equals(action))
            {
                isConnected = false;

                Toast.makeText(getApplicationContext(),R.string.srv_disconnect,Toast.LENGTH_SHORT).show();
                invalidateOptionsMenu();

            }
            else if(BtService.ACTION_GATT_SERVICES_DISCOVERED.equals(action))
            {
                //TODO 支持的 gattServiceList
//                List<BluetoothGattService> gattServiceList;
//                gattServiceList = btService.getSupportedGattServices();
//
//                final BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(UUID_CHAR_READ_NOTIFY,BluetoothGattCharacteristic.PROPERTY_READ |BluetoothGattCharacteristic.PROPERTY_WRITE |BluetoothGattCharacteristic.PROPERTY_NOTIFY, BluetoothGattCharacteristic.PERMISSION_READ);
//                ConsoleActivity.setCharacteristic(characteristic);
//                ConsoleActivity.setBtService(btService);
//
//
//                final Intent cmdIntent = new Intent(MainUseActivity.this,ConsoleActivity.class);
//                startActivity(cmdIntent);
                    startActivity();
            }else if(BtService.ACTION_DATA_AVAILABLE.equals(action))
            {
                final String uuid = intent.getStringExtra(BtService.EXTRA_SERVICE_UUID);
                final String text = intent.getStringExtra(BtService.EXTRA_TEXT);
                final String cuuid = intent.getStringExtra(BtService.EXTRA_CHARACTERISTIC_UUID);
              Log.i(TAG, " ACTION_DATA_AVAILABLE uuid: " + uuid + " cuuid :" + cuuid + " text:" + text);
            }
        }
    };


    private Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RETRY:
//                    final Intent gattIntent = new Intent(MainUseActivity.this,BtService.class);
//                    gattIntent.putExtra(BtDeviceServicesActivity.EXTRAS_DEVICE_NAME, BLE_DEVICE);
//                    gattIntent.putExtra(BtDeviceServicesActivity.EXTRAS_DEVICE_ADDR, BLE_ADDR);
//                    bindService(gattIntent,serviceConnection,BIND_AUTO_CREATE);
//                    Log.d(TAG, "Connect to device: " + BLE_DEVICE + " addr: " + BLE_ADDR);
                    onResume();

                    break;
                default:
                    break;
            }
        }

    };


    private Runnable runnable = new Runnable() {
        @Override
        public void run() {

            Message message = new Message();
            message.what = RETRY;
            handler.sendMessage(message);
            handler.postDelayed(runnable, 1000*20);

        }
    };
}

