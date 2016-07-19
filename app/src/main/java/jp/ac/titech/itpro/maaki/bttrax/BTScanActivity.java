package jp.ac.titech.itpro.maaki.bttrax;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class BTScanActivity extends AppCompatActivity{
    private ListView devListView;
    private ArrayList<BluetoothDevice> devList;
    private ArrayAdapter<BluetoothDevice> devListAdapter;
    private Resources resources;
    private ProgressBar scanProgress;

    private final static String[] PERMISSIONS = {
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private BluetoothAdapter btAdapter;
    private BroadcastReceiver btScanReceiver;
    private IntentFilter btScanFilter;

    private static final int REQCODE_ENABLE_BT = 100;
    private static final int REQCODE_PERMISSIONS = 103;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.btscan_layout);
        resources = getResources();

        scanProgress = (ProgressBar)findViewById(R.id.scan_progress);
        devListView = (ListView)findViewById(R.id.dev_list);
        devList = new ArrayList<>();
        devListAdapter = new ArrayAdapter<BluetoothDevice>(this, 0, devList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(convertView == null) {
                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    convertView = inflater.inflate(android.R.layout.simple_list_item_2, parent, false);
                }
                BluetoothDevice dev = getItem(position);
                TextView nameView = (TextView) convertView.findViewById(android.R.id.text1);
                TextView addrView = (TextView) convertView.findViewById(android.R.id.text2);
                nameView.setText(dev.getName());
                addrView.setText(dev.getAddress());
                return convertView;
            }
        };
        devListView.setAdapter(devListAdapter);
        devListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final BluetoothDevice dev = (BluetoothDevice) parent.getItemAtPosition(position);
                new AlertDialog.Builder(BTScanActivity.this)
                        .setTitle(dev.getName())
                        .setMessage(resources.getString(R.string.try_connection))
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.yes,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (btAdapter.isDiscovering())
                                            btAdapter.cancelDiscovery();
                                        Intent data = new Intent();
                                        data.putExtra(BluetoothDevice.EXTRA_DEVICE, dev);
                                        BTScanActivity.this.setResult(Activity.RESULT_OK, data);
                                        BTScanActivity.this.finish();

                                    }
                                })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            }
        });

        btScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) {
                    case BluetoothDevice.ACTION_FOUND:
                        BluetoothDevice dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        devListAdapter.add(dev);
                        devListAdapter.notifyDataSetChanged();
                        devListView.smoothScrollToPosition(devListAdapter.getCount());
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                        scanProgress.setIndeterminate(true);
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                        scanProgress.setIndeterminate(false);
                        break;
                }
            }
        };
        btScanFilter = new IntentFilter();
        btScanFilter.addAction(BluetoothDevice.ACTION_FOUND);
        btScanFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        btScanFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if(btAdapter != null)
            setupBT();
        else {
            Toast.makeText(this, resources.getString(R.string.bluetooth_not_available), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(btScanReceiver, btScanFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(btScanReceiver);
    }

    private void setupBT() {
        if (!btAdapter.isEnabled())
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQCODE_ENABLE_BT);
        else
            setupBT1();
    }

    private void setupBT1() {
        for (BluetoothDevice device : btAdapter.getBondedDevices())
            devListAdapter.add(device);
        devListAdapter.notifyDataSetChanged();
        scanProgress.setIndeterminate(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQCODE_ENABLE_BT:
                if(resultCode == Activity.RESULT_OK)
                    setupBT1();
                else {
                    Toast.makeText(this, resources.getString(R.string.bluetooth_not_accept), Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQCODE_PERMISSIONS:
                for(int i=0; i<permissions.length; i++) {
                    if(grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, resources.getString(R.string.scanning_requires_permission, permissions[i]), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                startScan();
                break;
        }
    }
    public void onClickStart(View v) {
        for(String permission : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, REQCODE_PERMISSIONS);
                return;
            }
        }
        startScan();
    }

    public void startScan() {
        devListAdapter.clear();
        if(btAdapter.isDiscovering())
            btAdapter.cancelDiscovery();
        btAdapter.startDiscovery();
    }

    public void onClickStop(View v) {
        btAdapter.cancelDiscovery();
    }
}
