package com.example.tressete;

import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ListDevices extends Activity{
	
	// String de retorno para intent
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    // Adaptador Bluetooth e vetores para dispositivos pareados e novos dispositivos
    private BluetoothAdapter BtAdapter;
    private ArrayAdapter<String> PairedDevices;
    private ArrayAdapter<String> NewDevices;
    public static boolean servidor=false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.device_list);

        // Caso usuário clique em return
        setResult(Activity.RESULT_CANCELED);

        // Inicialização do botão para descobrir novos dispositivos
        Button scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                doDiscovery();
                v.setVisibility(View.GONE);
            }
        });
        
        /*
         * Inicialização dos arrays de dispositivos
         * e recepção dos dispositivos pareados e próximos
         */
        PairedDevices = new ArrayAdapter<String>(this, R.layout.device_name);
        NewDevices = new ArrayAdapter<String>(this, R.layout.device_name);

        // Preenchimento da listview de dispositivos pareados
        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(PairedDevices);
        pairedListView.setOnItemClickListener(DeviceClickListener);

        // Preenchimento da listview de dispositios próximos e dispíveis para conexão 
        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(NewDevices);
        newDevicesListView.setOnItemClickListener(DeviceClickListener);

        // Registra a descoberta de novo dispositivo
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Registro do fim da descoberta de dispositivos
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        // BtAdapter refere-se ao adaptador Bluetooth do dispositivo atual
        BtAdapter = BluetoothAdapter.getDefaultAdapter();

        // Recebe a lista dos dispositivos pareados
        Set<BluetoothDevice> pairedDevices = BtAdapter.getBondedDevices();

        // Adiciona cada um deles ao vetor PairedDevices
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
            	PairedDevices.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = "None paired";
            PairedDevices.add(noDevices);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Termina o processo de descoberta de dispositivos
        if (BtAdapter != null) {
            BtAdapter.cancelDiscovery();
        }
        // Libera listeners de broadcast
        this.unregisterReceiver(mReceiver);
    }

    /**
     * Inicia a descoberta de dispositivos
     */
    private void doDiscovery() {              
        if (BtAdapter.isDiscovering()) {
            BtAdapter.cancelDiscovery();
        }
        // Requisita descoberta
        BtAdapter.startDiscovery();
    }

    // Listener para os itens da lista de dispositivos. Para conectar, basta clicar no item desejado
    private OnItemClickListener DeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            BtAdapter.cancelDiscovery();

            // address contem o MAC do dispositivo a se conectar
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Cria o resultado do intent e passa o MAC como parametro
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

            // Termina activity
            setResult(Activity.RESULT_OK, intent);
            finish();
            
            servidor=true;
        }
    };
    
    /*
     * Acionado quando há descoberta de novos dispositivos
     */
   
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // Quando encontra-se um dispositivo
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Recupera o dispositivo do intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Se já estiver pareado, pula a etapa de pareamento
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                	NewDevices.add(device.getName() + "\n" + device.getAddress());
                }
            // Fim da descoberta
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                   if (NewDevices.getCount() == 0) {
                    String noDevices = "none paired";
                    NewDevices.add(noDevices);
                }
            }
        }
    };
	

}
