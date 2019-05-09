package bt.lcy.btread.use;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.easyandroidanimations.library.Animation;
import com.easyandroidanimations.library.FoldLayout;
import com.easyandroidanimations.library.SlideOutUnderneathAnimation;
import com.easyandroidanimations.library.UnfoldAnimation;

import java.util.UUID;

import adapters.ReadDataAdapter;
import bt.lcy.btread.BtService;
import bt.lcy.btread.R;

public class ConsoleActivity extends AppCompatActivity {

    private final static String TAG = ConsoleActivity.class.getSimpleName();
    private ListView listData;

    ImageView card;
    TextView tv_carin,tv_carout,tv_gas,tv_timer;
    private MenuItem hexMenuItem;
    private MenuItem stringMenuItem;
    private static BluetoothGattCharacteristic characteristic;
    private static BtService btService;
    private boolean write = false;
    private boolean read = false;
    private boolean notify = false;
    private int i =0;
    private double k1=0;
    private double k0=0;
    private double a1=0.05;
    private  int w1= 0;
    private  int q1= 0;
    private  int N = 20;
    private  int K = 50;

    //磁场强度的阈值
    private int threshold = 15;
    double[] m=new double[N];
    private ReadDataAdapter readDataAdapter;

    private boolean onlyRead = false;
    private boolean isCarIn =true;
    public static final UUID UUID_GATT_SERVICE_READ_NOTIFY = UUID.fromString("00000002-0000-1000-8000-00805f9b34fb");
    public static void setBtService(final BtService btService) {
        ConsoleActivity.btService = btService;
    }

    public static void setCharacteristic(final BluetoothGattCharacteristic characteristic) {
        ConsoleActivity.characteristic = characteristic;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.rwnn_console);



    }

    private final BroadcastReceiver gattBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();


            if (BtService.ACTION_DATA_AVAILABLE.equals(action)) {
                final String uuid = intent.getStringExtra(BtService.EXTRA_SERVICE_UUID);
//                final String text = intent.getStringExtra(BtService.EXTRA_TEXT);
                final String cuuid = intent.getStringExtra(BtService.EXTRA_CHARACTERISTIC_UUID);
                Log.i(TAG, " ACTION_DATA_AVAILABLE uuid: " + uuid + " cuuid :" + cuuid);
                String text = "empty";
                try {
                    text = new String(characteristic.getValue());

                } catch (NullPointerException e) {

                } finally {

                }
                sensorDataBase(text);
                //sensorDataBasePlus(text);

               // final byte[] arr = text.getBytes();



                Log.i(TAG, "+++Get Notify data is " + text);
            } else if (BtService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Toast.makeText(ConsoleActivity.this, R.string.srv_disconnect, Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onResume() {

        registerReceiver(gattBroadcastReceiver, makeGattUpdateIntenFilter());
        super.onResume();

        card = findViewById(R.id.imageView);
        tv_carin =findViewById(R.id.tv_in);
        tv_carout = findViewById(R.id.tv_out);
        tv_gas = findViewById(R.id.tv_gas);
        tv_gas.setText(getString(R.string.now_threshold)+threshold);
        tv_timer =findViewById(R.id.tv_timer);

        final Intent intent = getIntent();

        int properties = characteristic.getProperties();


        readDataAdapter = new ReadDataAdapter(this);

        listData = (ListView) findViewById(R.id.list_read);

        listData.setAdapter(readDataAdapter);



        read = (properties & BluetoothGattCharacteristic.PROPERTY_READ) != 0;
        write = (properties & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0;
        notify = (properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;

        if (notify) {
            Toast.makeText(this, R.string.startlistener, Toast.LENGTH_SHORT).show();
            btService.setCharacteristicNotification(characteristic, true);
        }

        final BluetoothGattDescriptor descriptor = characteristic.getDescriptor(characteristic.getUuid());

        if (descriptor != null) {
            Log.i(TAG,"getDescriptor ONE:  " + descriptor.getUuid());
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            btService.writeDescriptor(descriptor);
        } else {
            Log.w(TAG,"characteristic.getUuid() " + characteristic.getUuid() + " get descriptor null ");


        }


       // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.stringmode);
        getSupportActionBar().setTitle(R.string.data_verb);
    }

    private static IntentFilter makeGattUpdateIntenFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BtService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BtService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @Override
    protected void onPause() {

        unregisterReceiver(gattBroadcastReceiver);
        Log.i(TAG, "!!! ConsoleActivity onPause");
        readDataAdapter.clear();
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (notify&& characteristic != null) {
            if(characteristic!=null){
                btService.setCharacteristicNotification(characteristic, false);
                final BluetoothGattDescriptor descriptor = characteristic.getDescriptor(characteristic.getUuid());
                Log.i(TAG, "get descriptor on stop " + descriptor);
                if (descriptor != null) {
                    descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                    btService.writeDescriptor(descriptor);
                }
            }

        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "!!! ConsoleActivity onDestroy ---");

    }

@Override
public boolean dispatchKeyEvent(KeyEvent event) {
    if(event.getKeyCode() == KeyEvent.KEYCODE_BACK ) {
        //do something.
        return true;
    }else {
        return super.dispatchKeyEvent(event);
    }
}

    public void sensorDataBase(String str){
        String[] res= null;
        //  Log.d(TAG,"----str:"+str);
        if(!TextUtils.isEmpty(str)){
            // Log.d(TAG,"--TextUtils--str:"+str);
            if(str.contains(",")){
                res = str.split("[,]");
                str = res[0];
                //  Log.d(TAG,"-split---str:"+str);
            }
            // Log.d(TAG,"----str:"+str);
            if(str.contains("|")){
                str = str.substring(1);
            }

            int x = Integer.parseInt(str);

            int y = Integer.parseInt(res[1]);
            String [] ys ;
            int z;
            if(res[2].contains("|")){
                ys = res[2].split("\\|");
                z = Integer.parseInt(ys[0]);
            }else {
                z = Integer.parseInt(res[2]);
            }

          //  Log.d(TAG,"----X:"+x+"----Y:"+y+"----Z:"+z);
            double x2 = (Math.abs(x))^2;
//            if(x<0){
//                x2 = -x2;
//            }
            double y2 = (Math.abs(y))^2;
//            if(y<0){
//                y2 = -y2;
//            }
            double z2 = (Math.abs(y))^2;
//            if(z<0){
//                z2 = -z2;
//            }
            double sum = x2+y2+z2;
            double s;
            if(sum<0){
                 s = 0;
            }else {
                s = Math.sqrt(sum);
            }

            Log.d(TAG,"----X:"+x+"----Y:"+y+"----Z:"+z+"----X2:"+x2+"----Y2:"+y2+"----Z2:"+z2+"----sum--"+sum+"---sqrt:"+s+"---i---"+i);


            m[i%N]= s;

            if(i== N-1){
                k0 = MathUtils.meanAverage(m);
            }
            if(K>i){
                tv_timer.setVisibility(View.VISIBLE);
                tv_timer.setText(getString(R.string.data_verb)+(K-i));
            }else {
                tv_timer.setVisibility(View.INVISIBLE);
            }


            i++;
            Log.d(TAG,"---sMathUtils.variance k0:"+k0+"---s--"+s+"--k1--"+k1);

            if (i>K){

                if(getSupportActionBar().getTitle().toString().equals(getString(R.string.data_verb))){
                    getSupportActionBar().setTitle(R.string.data_check);
                }
                /**
                 *
                 * gaussage 磁场强度变化绝对值
                 *
                 * */
                double gaussage = Math.abs(s-k0);

                readDataAdapter.add(gaussage+"");

                if(gaussage>threshold){
                    w1++;
                    q1=0;
                }else {
                    w1=0;
                    q1++;
                }

                Log.d(TAG,"---sMathUtils.variance w1:"+w1+"---q1--"+q1+"---gaussage---"+gaussage);
                if(w1>10){
                    isCarIn = true;

                    carAnimate(isCarIn);
                }

                if(q1>10){
                        isCarIn = false;
                        carAnimate(isCarIn);
                        q1= q1%12;
                }



                if(i>2*K+1){
                    i = i-K;
                }
                if(w1>2*K+1){
                    w1 = w1-K;
                }
            }else {
                if(getSupportActionBar().getTitle().toString().equals(getString(R.string.data_check))){
                    getSupportActionBar().setTitle(R.string.data_verb);
                }


            }

        }


    }

        /**
         *
         * 测试车辆通过的算发
         *
         * 基线数据处理部分
         *
         * ***/
    public void sensorDataBasePlus(String str){
        String[] res= null;
        //  Log.d(TAG,"----str:"+str);
        if(!TextUtils.isEmpty(str)){
            // Log.d(TAG,"--TextUtils--str:"+str);
            if(str.contains(",")){
                res = str.split("[,]");
                str = res[0];
                //  Log.d(TAG,"-split---str:"+str);
            }
            // Log.d(TAG,"----str:"+str);
            if(str.contains("|")){
                str = str.substring(1);
            }

            int x = Integer.parseInt(str);

            int y = Integer.parseInt(res[1]);
            String [] ys ;
            int z;
            if(res[2].contains("|")){
                ys = res[2].split("\\|");
                z = Integer.parseInt(ys[0]);
            }else {
                z = Integer.parseInt(res[2]);
            }

            double x2 = (Math.abs(x))^2;
//            if(x<0){
//                x2 = -x2;
//            }
            double y2 = (Math.abs(y))^2;
//            if(y<0){
//                y2 = -y2;
//            }
            double z2 = (Math.abs(y))^2;
//            if(z<0){
//                z2 = -z2;
//            }
            double sum = x2+y2+z2;
            double s;
            if(sum<0){
                s = 0;
            }else {
                s = Math.sqrt(sum);
            }

            Log.d(TAG,"----X:"+x+"----Y:"+y+"----Z:"+z+"----X2:"+x2+"----Y2:"+y2+"----Z2:"+z2+"----sum--"+sum+"---sqrt:"+s+"---i---"+i);




            m[i%N]= s;

            if(i== N-1){
                k0 = MathUtils.meanAverage(m);
                // Log.d(TAG,"---sqrt(x^2+y^2+z^2):"+s+"---i---"+i);
            }


           /***
            * 基线值平滑处理
            *
            * */
            if(i >N-1){
                k1 = MathUtils.meanAverage(m);
            }

            if(i>K){

                if(getSupportActionBar().getTitle().toString().equals(getString(R.string.data_verb))){
                    getSupportActionBar().setTitle(R.string.data_check);
                }

                if(s-k0>threshold){
                    k0 = k1;
                }else {
                    k0 = k0*(1-a1)+k1*a1;
                }
            }

            i++;



            Log.d(TAG,"---sMathUtils.variance k0:"+k0+"---s--"+s+"--k1--"+k1);

            if (i>K){

                if(s-k0>threshold){
                    w1++;
                }else {
                    w1=0;
                }


                if(s-k0< -threshold){
                    q1++;
                }else {
                    q1=0;
                }
                Log.d(TAG,"---sMathUtils.variance w1:"+w1+"---q1--"+q1);
                if(w1>10){
                    isCarIn = true;

                    carAnimate(isCarIn);
                }
                if(q1>5) {
                    isCarIn = false;
                    carAnimate(isCarIn);
                }



                /***
                 *
                 * 防止i,w1,值超限
                 * */
                if(i>2*K+1){
                    i = i-K;
                }
                if(w1>2*K+1){
                    w1 = w1-K;
                }
            }else {
                if (getSupportActionBar().getTitle().toString().equals(getString(R.string.data_check))) {
                    getSupportActionBar().setTitle(R.string.data_verb);
                }
            }

        }


    }



    public void carAnimate(boolean isCarIn){

        if(isCarIn){
            card.setVisibility(View.VISIBLE);
            tv_carout.setVisibility(View.INVISIBLE);
            tv_carin.setVisibility(View.VISIBLE);

//           new UnfoldAnimation(card).setNumOfFolds(10).setDuration(1000)
//                    .setOrientation(FoldLayout.Orientation.HORIZONTAL).animate();

        }else {

            tv_carout.setVisibility(View.VISIBLE);
            tv_carin.setVisibility(View.INVISIBLE);
//            new SlideOutUnderneathAnimation(card).setDirection(
//                    Animation.DIRECTION_RIGHT).animate();
            card.setVisibility(View.INVISIBLE);
        }
    }

}
