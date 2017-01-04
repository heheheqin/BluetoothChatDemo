package com.example.bluetooth;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.example.entity.Bluetooth;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity {

    private final MyReceiver myReceiver = new MyReceiver();
    private Button search_again;
    private ListView bluetooth_listview;
    private List<Bluetooth> list;
    private BluetoothAdapter bluetoothAdapter;
    private MyBluetoothListAdapter listAdapter;
    private ProgressBar progressBar1;
    ;
    private String bluetoothaddress;
    private String name;
    private boolean flag;
    private Button servic;
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {

        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            Bluetooth bluetooth = list.get(position);
            bluetoothaddress = bluetooth.getAddr();
            name = bluetooth.getName();
            AlertDialog.Builder stopDialog = new AlertDialog.Builder(MainActivity.this);
            stopDialog.setTitle("连接");//标题
            stopDialog.setMessage(bluetooth.getName() + "\n" + bluetooth.getAddr());
            stopDialog.setPositiveButton("连接", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    bluetoothAdapter.cancelDiscovery();
                    search_again.setText("重新搜索");
                    //传递参数到 ChatActivity 页面
                    Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                    Bundle bundle = new Bundle();
                    flag = false;
                    bundle.putBoolean("servicflag", flag);   //  不是是服务端
                    bundle.putString("bluetoothAddr", bluetoothaddress); //该行的蓝牙地址
                    bundle.putString("bluetoothName", name);   //蓝牙地址的名字
                    intent.putExtra("bundle", bundle);
                    startActivity(intent, bundle);
                    dialog.cancel();
                }
            });
            stopDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            stopDialog.show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        search_again = (Button) findViewById(R.id.search_again);
        bluetooth_listview = (ListView) findViewById(R.id.bluetooth_listview);
        progressBar1 = (ProgressBar) findViewById(R.id.progressBar1);
        servic = (Button) findViewById(R.id.Servic);
        // 初始化 蓝牙设备列表
        list = new ArrayList<>();
        // 注册广播
        initBlueToothBroadCast();
        listAdapter = new MyBluetoothListAdapter(list, this);
        bluetooth_listview.setAdapter(listAdapter);
        bluetooth_listview.setOnItemClickListener(mDeviceClickListener);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        search_again.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (bluetoothAdapter.isDiscovering()) {
                    bluetoothAdapter.cancelDiscovery();
//					progressBar1.setActivated(false);
                    search_again.setText("Search again");
                } else {
                    list.clear();
                    listAdapter.notifyDataSetChanged();
                    findBluetooth();
                    /* 开始搜索 */
                    bluetoothAdapter.startDiscovery();
//					progressBar1.setActivated(true);
                    search_again.setText("Stop Search");
                }
            }

        });


        servic.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                Bundle bundle = new Bundle();
                flag = true;
                bundle.putBoolean("servicflag", flag);
                intent.putExtra("bundle", bundle);
                startActivity(intent, bundle);
            }
        });
    }

    /**
     * 根据适配器得到所有的设备信息
     */
    private void findBluetooth() {
        Set<BluetoothDevice> deviceSet = bluetoothAdapter.getBondedDevices();
        if (deviceSet.size() > 0) {
            for (BluetoothDevice device : deviceSet) {
                updateList(device);
                listAdapter.notifyDataSetChanged();
                bluetooth_listview.setSelection(list.size() - 1);
            }
        }
//		else {
//			Bluetooth bluetooth = new Bluetooth();
//			bluetooth.setAddr("no device");
//			bluetooth.setName("serch again");
//			list.add(bluetooth);
//			listAdapter.notifyDataSetChanged();
//		}
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 判断设备是否有蓝牙。
        if (bluetoothAdapter == null) {
            Toast.makeText(MainActivity.this, "-- no bluetooth-->>>", Toast.LENGTH_SHORT).show();
            return;
        } else {
            // 判断是否开启蓝牙 没开启提示用户开启蓝牙
            if (!bluetoothAdapter.isEnabled()) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(intent);
            }
        }
        // 自动搜索设备
        if (!bluetoothAdapter.isDiscovering() && bluetoothAdapter.startDiscovery()) {
            Toast.makeText(MainActivity.this, "蓝牙查找功能启动成功", Toast.LENGTH_LONG).show();
        }

    }

    private void initBlueToothBroadCast() {

        IntentFilter filter = new IntentFilter();
        // 加入扫描开始的广播
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        // 加入扫描完成的广播
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        // 加入发现设备的广播
        filter.addAction(BluetoothDevice.ACTION_FOUND);

        registerReceiver(myReceiver, filter);
    }

    public void updateList(BluetoothDevice device) {
        Bluetooth bluetooth = new Bluetooth();
        int deviceClass = device.getBluetoothClass().getDeviceClass();
        Toast.makeText(getApplicationContext(), "device:" + deviceClass, 0).show();
        if (deviceClass >= 2048 && deviceClass <= 2064) {// 玩具 0x800
            bluetooth.setImage(R.drawable.game);
        } else if (deviceClass >= 1792 && deviceClass <= 1812) {// 可穿戴设备 0x700
            bluetooth.setImage(R.drawable.wearable);
        } else if (deviceClass >= 512 && deviceClass <= 524) {// 手机 0x200
            bluetooth.setImage(R.drawable.phone);
        } else if (deviceClass >= 2304 && deviceClass <= 2332) {// 健康 0x900
            bluetooth.setImage(R.drawable.health);
        } else if (deviceClass >= 256 && deviceClass <= 280) {// 电脑 0x100
            bluetooth.setImage(R.drawable.computer);
        } else if (deviceClass >= 1024 && deviceClass <= 1088) {// 耳机 0x400
            bluetooth.setImage(R.drawable.headset);
        }


        bluetooth.setAddr(device.getAddress());
        bluetooth.setName(device.getName());
        list.add(bluetooth);
    }


    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (listAdapter != null) {
            bluetoothAdapter.cancelDiscovery();
        }
        this.unregisterReceiver(myReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // 广播接收者:
    public class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //
//			Toast.makeText(MainActivity.this, "--BroadcastReceiver", 0).show();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // 发现蓝牙设备 从Intent中获取设备对象
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    // 将设备名称和地址放入list adapter，以便在ListView中显示
                    updateList(device);
                    listAdapter.notifyDataSetChanged();
                    bluetooth_listview.setSelection(list.size() - 1);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                progressBar1.setActivated(isFinishing());
                Toast.makeText(MainActivity.this, "--ACTION_DISCOVERY_FINISHED", 0).show();
                if (listAdapter.getCount() == 0) {
                    Bluetooth bluetooth = new Bluetooth();
                    bluetooth.setAddr("no device");
                    bluetooth.setName("serch again");
                    list.add(bluetooth);
                    listAdapter.notifyDataSetChanged();
                    bluetooth_listview.setSelection(list.size() - 1);
                }
                search_again.setText("serch again");
            }

        }

    }
}
