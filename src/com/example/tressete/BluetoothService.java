package com.example.tressete;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import java.util.UUID;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/* Gerenciamento das conexões bluetooth com outros dispositivos.
 * Possui uma thread que aguarda novas conexões (modo servidor), 
 * uma thread para realizar a conexão com outro dispositivo
 * (modo cliente) e uma thread dedicada à transmissão de dados.
 */

public class BluetoothService {

	// Nome utilizado na criação do socket de comunicação
    private static final String NAME = "tressete";
    // UUID unica para a aplicação
    private static final UUID MY_UUID = UUID.fromString("e86db510-60f7-11e4-9803-0800200c9a66");
    
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;
    
    // constantes para auxiliar na identificação do estado de conexão
    public static final int STATE_NONE = 0;       // idle
    public static final int STATE_LISTEN = 1;     // aguardando novas conexões
    public static final int STATE_CONNECTING = 2; // inicializando conexão
    public static final int STATE_CONNECTED = 3;  // conexão estabelecida
    
    /*
     * Construtor
     * @param context Contexto da activity
     * @param handler Utilizado para enviar mensagens para a activity
     */
    public BluetoothService(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
    }
    
    // Define o estado atual da conexão
    private synchronized void setState(int state) {
        mState = state;

        // Passa o novo estado para o handler para moduficar a interface corretamente
        mHandler.obtainMessage(MainActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }
    // Retorna estado atual
    public synchronized int getState() {
        return mState;
    }
    
    // Habilita o estado de espera de conexão
    public synchronized void start() {
        // Cancela threads tentando iniciar uma conexão
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancela thread caso esteja mantendo uma conexão
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Inicia a espera por conexão, thread Accept
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
        setState(STATE_LISTEN);
    }
    
    /**
     * Inicializa a thread de conexão à outros dispositivos
     * @param device  Dispositivo para realizar a conexão
     */
    public synchronized void connect(BluetoothDevice device) {
        // Cancela threads tentando iniciar uma conexão
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        }

        // Cancela thread caso esteja mantendo uma conexão
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Inicializa a thread com o dispositivo escolhido
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }
    
    /**
     * Inicializa a ConnectedThread que manipula a comunicação entre os dispositivos
     * @param socket  Socket de comunicação
     * @param device  Dispositivo remoto utilizado
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        // Cancela a thread Connect após estabelecimento da conexão
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

     // Cancela thread caso esteja mantendo uma conexão
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Cancela a thread Accept, apenas uma conexão por vez
        if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}

        // Inicializa thread de comunicação
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        // Envia o nome do dispositivo para a interface
        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setState(STATE_CONNECTED);
        
       
    }
        
    //Para todas as threads
    public synchronized void stop() {
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
        if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}
        setState(STATE_NONE);
    }
    
    /**
     * Envio de dados
     * @param out Bytes a serem escritos
     */
    public void write(byte[] out) {
        // Objeto temporário
        ConnectedThread r;
        // Sincronização da cópia de ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Realiza a escrita 
        r.write(out);
    }
    
    /**
     * Thread que aguarda requisições de conexão. 
	 * Comporta-se como o servidor da conexão 
     */
    private class AcceptThread extends Thread {
        // Socket de comunicaçõe
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            // Cria socket para aguardar conexão
            try {
                tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
               
            }
            mmServerSocket = tmp;
        }
        

        public void run() {
            setName("AcceptThread");
            BluetoothSocket socket = null;

            // Aguarda requisição de conexão
            while (mState != STATE_CONNECTED) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }

                // Conexão aceita
                if (socket != null) {
                    synchronized (BluetoothService.this) {
                        switch (mState) {
                        case STATE_LISTEN:
                        case STATE_CONNECTING:
                            // Inicializa a thread connected
                            connected(socket, socket.getRemoteDevice());
                            break;
                        case STATE_NONE:
                        case STATE_CONNECTED:
                            // Se já estiver estabelecida a cnexão, termina o socket
                            try {
                                socket.close();
                            } catch (IOException e) {}
                            break;
                        }
                    }
                }
            }
            
        }

        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
            }
        }
    }


    /**
     * Thread que tenta realizar conexão com outro dispositivo
     * no modo cliente 
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // recebe o socket de comunicação
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) { }
            mmSocket = tmp;
        }

        public void run() {
            setName("ConnectThread");

            // Cancela descoberta de dispositivos, não mais necessária
            mAdapter.cancelDiscovery();

            // Realiza conexão 
            try {
                // Blocking call
                mmSocket.connect();
            } catch (IOException e) {
                try {
                    mmSocket.close();
                } catch (IOException e2) { }
                // Reinicializa modo de espera de conexão
                BluetoothService.this.start();
                return;
            }

            // Reinicializa a trhead de conexçâo
            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }

            // Inicializa a thread de manutenção de comunicação
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    /**
     * Gerencia a comunação entre os dispositivos
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Captura os envios e recebimentos de dados
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {  }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            // Aguarda novos recebimentos enquanto conectado
            while (true) {
                try {
                    // Le os dados recebidos
                    bytes = mmInStream.read(buffer);

                    // Envia os dados recebidos para a interface
                    mHandler.obtainMessage(MainActivity.MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                
                } catch (IOException e) {
                    break;
                }
            }
            
        }

        /**
         * Escrita de dados a serem enviados
         * @param buffer  The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                // Envia a mensagem para a interface
                mHandler.obtainMessage(MainActivity.MESSAGE_WRITE, -1, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
            	
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
}
