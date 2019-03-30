package com.deerdeveloper.cetakthermal;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.deerdeveloper.cetakthermal.library.ESC_POS_EPSON_ANDROID;
import com.deerdeveloper.cetakthermal.library.USBPort;

import java.util.HashMap;
import java.util.Iterator;

public class CekPrinter extends AppCompatActivity implements View.OnClickListener {

    private static final String ACTION_USB_PERMISSION = "USB_PERMISSION";

    private PendingIntent permissionIntent;
    private UsbManager usbManager;
    private USBPort usbPort;

    private ESC_POS_EPSON_ANDROID escPosEpsonAndroid;

    Button btn_sample_1, btn_sample_2, btn_connect;
    TextView connection_info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cek_printer);

        btn_sample_1 = findViewById(R.id.btn_sample_1);
        btn_sample_2 = findViewById(R.id.btn_sample_2);
        btn_connect = findViewById(R.id.btn_connect);
        connection_info = findViewById(R.id.connection_info);

        btn_sample_1.setOnClickListener(this);
        btn_sample_2.setOnClickListener(this);
        btn_connect.setOnClickListener(this);

        usbManager = (UsbManager)getSystemService(Context.USB_SERVICE);

        permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);

        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(broadcastReceiver, filter);
        filter = new IntentFilter(usbManager.ACTION_USB_DEVICE_ATTACHED);
        registerReceiver(broadcastReceiver, filter);
        filter = new IntentFilter(usbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(broadcastReceiver, filter);

        usbPort = new USBPort(usbManager);
        escPosEpsonAndroid = new ESC_POS_EPSON_ANDROID(usbPort);

        connection_info.setText("USB belum terhubung");
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(ACTION_USB_PERMISSION.equals(action)){
                synchronized (this){
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if(intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)){
                        // jika diijinkan
                        if(device != null){
                            connection_info.setText("USB telah terhubung ya ... silahkan print");
                        }
                    } else {
                        // jika permission tidak di izinkan
                        connection_info.setText("Dikasih ijin dulu ya ...");
                    }
                }
            }

            if(UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)){
                UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if(device != null){
                    Toast.makeText(getApplicationContext(), "USB Device ATTACHED", Toast.LENGTH_SHORT).show();
                }
            }

            if(UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)){
                UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if(device != null){
                    Toast.makeText(getApplicationContext(), "USB e di copot jeh ya..", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    private void connect(){
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        Iterator<UsbDevice> iterator = deviceList.values().iterator();
        while (iterator.hasNext()){
            UsbDevice device = iterator.next();
            if(!usbManager.hasPermission(device)) {
                Toast.makeText(getApplicationContext(), "Harus diijinin dulu akses USB nya", Toast.LENGTH_SHORT).show();
                connection_info.setText("USB harus diijinkan coyy");
                usbManager.requestPermission(device, permissionIntent);
                return;
            }
            try {
                usbPort.connect_device(device);
                connection_info.setText("USB Terhubung :)");
                Toast.makeText(getApplicationContext(), "USB Connected", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                connection_info.setText("USB gagal terhubung , ada masalah :(");
                Toast.makeText(getApplicationContext(), "Yahh gagal koneksi ke USB", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void print_sample_1(){
        escPosEpsonAndroid.print_sample();
    }

    private void print_sample_2(){
        escPosEpsonAndroid.print_sample1();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_sample_1 :
                print_sample_1();
                break;
            case R.id.btn_sample_2 :
                print_sample_2();
                break;
            case R.id.btn_connect :
                connect();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        try {
            usbPort.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}
