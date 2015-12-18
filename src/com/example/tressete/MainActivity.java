package com.example.tressete;

import java.util.ArrayList;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {

	// Códigos para troca de intent
	private static final int REQUEST_ENABLE_BT = 1;
	private static final int REQUEST_CONNECT_DEVICE = 2;
	private static final int RESTART = 3;

	// Itens de layout
	private TextView mCartasRestantes;
	private ImageView mCartaJogador1;
	private ImageView mCartaJogador2;
	private ImageView mBaralho;
	private ImageButton mCarta1;
	private ImageButton mCarta2;
	private ImageButton mCarta3;
	private BluetoothAdapter myBluetoothAdapter;
	// Nome do dispositivo conectado
	public static String mConnectedDeviceName = null;

	// Cartas do jogo
	public Carta carta1=null, carta2=null, carta3=null;

	private ArrayList<Carta> mesa = new ArrayList<Carta>();
	private static ArrayList<Carta> jog1 = new ArrayList<Carta>();
	private static ArrayList<Carta> jog2 = new ArrayList<Carta>();

	// Retornos do handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";
	// Adaptador Bluetooth
	public static BluetoothService mBtService = null;

	// Tipos de mensagem enviadas pelo Handler do BluetoothService
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;   

	private boolean iniciaJogo=true, vez=false;
	private Baralho baralho;
	static String pontosCliente="";
	static String pontosServidor="";
	public static boolean reiniciou=false;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		getWindow().getDecorView().setBackgroundColor(Color.rgb(0, 80, 3));

		// instancia o adaptador bluetooth do dispositivo 
		myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(myBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
			finish();
			return;
		} 
	}

	public void onStart() {
		super.onStart();

		// Verifica ao iniciar se o BT já está ativado
		if (!myBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT); // Requisita habilitação do bluetooth

			// Chama método para preparar futuras conexões
		} else {
			if (mBtService == null) setupConnection();
		}
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		/*
		 * Caso o BT não tenha sido ligado no inicio,
		 * deve ser habilitado nesse momento.
		 * onResume() é executada após o retorno da 
		 * requisição de ativação do adaptador
		 */
		if (mBtService != null) {
			// Estado NONE idica que as threads de conexão ainda não foram iniciadas
			if (mBtService.getState() == BluetoothService.STATE_NONE) {
				// Inicializa as threads necessárias para comunicação -> Ver BluetoothService.java
				mBtService.start();
			}
		}
	}

	private void setupConnection() {
		// Inicialização dos campos de comunicação
		mCartasRestantes = (TextView) findViewById(R.id.textView1);
		mCartaJogador1 = (ImageView) findViewById(R.id.imageView1);
		mCartaJogador2 = (ImageView) findViewById(R.id.imageView2);
		mBaralho = (ImageView) findViewById(R.id.imageView3);
		mCartaJogador1.setImageAlpha(0);
		mCartaJogador2.setImageAlpha(0);

		// Inicialização dos botôes de envio de cartas
		mCarta1 = (ImageButton) findViewById(R.id.imageButton1);
		mCarta1.getBackground().setAlpha(0);
		mCarta1.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (vez==true)
				{
					mCarta1.setEnabled(false);
					sendMessage(carta1.stringToSend());

					carta1=null;
					mCarta1.setImageAlpha(0);

					vez=false;
				}
			}
		});
		mCarta2 = (ImageButton) findViewById(R.id.imageButton2);
		mCarta2.getBackground().setAlpha(0);
		mCarta2.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (vez==true)
				{	   
					mCarta2.setEnabled(false);
					sendMessage(carta2.stringToSend());

					carta2=null;
					mCarta2.setImageAlpha(0);

					vez=false;
				}
			}
		});
		mCarta3 = (ImageButton) findViewById(R.id.imageButton3);
		mCarta3.getBackground().setAlpha(0);
		mCarta3.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (vez==true)
				{
					mCarta3.setEnabled(false);
					sendMessage(carta3.stringToSend());

					carta3=null;
					mCarta3.setImageAlpha(0);

					vez=false;
				}
			}
		});

		// Inicialização dos serviços relacionados à comunicação bluetooth
		mBtService = new BluetoothService(this, mHandler);     
	}  


	@Override
	public synchronized void onPause() {
		super.onPause();  
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mBtService != null) mBtService.stop();
	}

	private void ensureDiscoverable() {
		if (myBluetoothAdapter.getScanMode() !=
				BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}

	/**
	 * Envia mensagens
	 * @param message  String a ser enviada
	 */
	private void sendMessage(String message) {
		// Verifica se há conexão
		if (mBtService.getState() != BluetoothService.STATE_CONNECTED) {
			Toast.makeText(this, "Não há conexão estabelecida!", Toast.LENGTH_SHORT).show();
			return;
		}

		// Verifica se há informação para ser enviada
		if (message.length() > 0) {
			// Recebe a mensagem e manda para o método de envio de mensagens -> Ver BluetoothService.java
			byte[] send = message.getBytes();
			mBtService.write(send);   
			message="";
		}
	}

	// Utilizado para receber as mensagens da classe BluetoothService
	private final Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case BluetoothService.STATE_CONNECTED:
					Toast.makeText(getApplicationContext(),"Connected to "+ mConnectedDeviceName,
							Toast.LENGTH_LONG).show();
					mCartasRestantes.setText("");
					mCartaJogador1.setImageAlpha(0);
					mCartaJogador2.setImageAlpha(0);
					if ((ListDevices.servidor==true)&&(iniciaJogo==true))
					{
						iniciaJogo=false; 
						reiniciou=true;
						iniciaJogo();
					}
					break;
				case BluetoothService.STATE_CONNECTING:
					Toast.makeText(getApplicationContext(),"Connecting...",
							Toast.LENGTH_SHORT).show();
					break;
				case BluetoothService.STATE_LISTEN:
				case BluetoothService.STATE_NONE:
					break;
				}
				break;
			case MESSAGE_WRITE:

				byte[] writeBuf = (byte[]) msg.obj;
				String writeMessage = new String(writeBuf);

				if (writeMessage.contains("reiniciou")==false && writeMessage.contains("pontosServidor")==false && writeMessage.contains("wait")==false)
				{
					String valor2 = writeMessage.substring(0, writeMessage.indexOf(" "));    
					String naipe2 = writeMessage.substring(writeMessage.indexOf(" ")+1, writeMessage.indexOf(".")-1);
					String tipo2 = writeMessage.substring(writeMessage.indexOf(".")+1, writeMessage.indexOf("|"));
					String pontos2 = writeMessage.substring(writeMessage.indexOf("|")+1, writeMessage.indexOf("|")+3);

					Carta c2 = new Carta(Integer.parseInt(valor2), naipe2, 1, Integer.parseInt(pontos2));
					if ( Integer.parseInt(tipo2)==1) // Jogada
					{
						mCartaJogador1.setImageAlpha(255);
						mCartaJogador1.setImageResource(getImageId(getApplicationContext(), c2.toString()));
						if (ListDevices.servidor==true)
						{
							mesa.add(c2);
							if (mesa.size()==2)
							{
								rodada();
							}
						}
					}
					else if(Integer.parseInt(tipo2)==2 && ListDevices.servidor == true){ // vez
						try {/////////////////////////////
							Thread.sleep(1500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						mCartaJogador1.setImageAlpha(0);
						mCartaJogador2.setImageAlpha(0);
						mCartasRestantes.setText("");
					}
				}
				break;
			case MESSAGE_READ:

				byte[] readBuf = (byte[]) msg.obj;
				String readMessage = new String(readBuf, 0, msg.arg1);

				if (readMessage.contains("pontosServidor")==true && ListDevices.servidor==false) // envio dos pontos finais
				{
					pontosServidor =  readMessage.substring(15, readMessage.indexOf("|"));
					pontosCliente =  readMessage.substring( (readMessage.indexOf("|")+15), (readMessage.indexOf(".")) );
					telaResultado();

				}
				else if (readMessage.contains("reiniciou") && ListDevices.servidor==true) 
				{
					reiniciou = true;
				}
				else if (readMessage.contains("reiniciou")==false && readMessage.contains("pontosServidor")==false)
				{
					String valor = readMessage.substring(0, readMessage.indexOf(" "));    
					String naipe = readMessage.substring(readMessage.indexOf(" ")+1, readMessage.indexOf(".")-1);
					String tipo = readMessage.substring(readMessage.indexOf(".")+1, readMessage.indexOf("|"));
					String pontos = readMessage.substring(readMessage.indexOf("|")+1, readMessage.indexOf("|")+3);
										
					Carta c = new Carta(Integer.parseInt(valor), naipe, 1, Integer.parseInt(pontos));

					if (Integer.parseInt(tipo)==2 && ListDevices.servidor==false) // Vez
					{
						if (Integer.parseInt(valor)==1)
						{
							vez = true;
							mCartasRestantes.setText("Ganhou!");
						}
						else
						{
							vez = false;
							mCartasRestantes.setText("Perdeu!");
						}
						try {
							Thread.sleep(1500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						mCartaJogador1.setImageAlpha(0);
						mCartaJogador2.setImageAlpha(0);
						//mCartasRestantes.setText("");
					}
					else if (Integer.parseInt(tipo)==0 && ListDevices.servidor==false) // Pesca
					{ 
						c.setTipo(1);
						
						if (carta1==null)
						{
							carta1 = c;
							mCarta1.setImageResource(getImageId(getApplicationContext(), c.toString()));
							mCarta1.setImageAlpha(255);
							mCarta1.setEnabled(true);
						}
						else if (carta2==null)
						{
							carta2=c;
							mCarta2.setImageResource(getImageId(getApplicationContext(), c.toString()));
							mCarta2.setImageAlpha(255);
							mCarta2.setEnabled(true);
						}
						else if (carta3==null)
						{
							carta3=c;
							mCarta3.setImageResource(getImageId(getApplicationContext(), c.toString()));
							mCarta3.setImageAlpha(255);
							mCarta3.setEnabled(true);
						}
						mCartasRestantes.setText("");
					}  
					else if (Integer.parseInt(tipo)==1) // Jogada
					{
						mCartaJogador2.setImageAlpha(255);
						mCartaJogador2.setImageResource(getImageId(getApplicationContext(), c.toString()));
						c.setID(1);
						vez=true;
						if (ListDevices.servidor==true)
						{	
							mesa.add(c);
							if (mesa.size()==2)
							{
								rodada();
							}
						}
					}
				}
				break;
			case MESSAGE_DEVICE_NAME:
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(), "Connected to "
						+ mConnectedDeviceName, Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
						Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};
	
	public static int getImageId(Context context, String imageName) {
	    return context.getResources().getIdentifier("drawable/" + imageName, null, context.getPackageName());
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// Quando a classe ListDevice retorna um dispositivo para se conectar
			if (resultCode == Activity.RESULT_OK) {
				// Recebimento do MAC
				String address = data.getExtras().getString(ListDevices.EXTRA_DEVICE_ADDRESS);
				// Recebe o objeto do dispositivo a se conectar
				BluetoothDevice device = myBluetoothAdapter.getRemoteDevice(address);
				// Tenta estabelecer uma conexão
				mBtService.connect(device);
			}
			break;
		case REQUEST_ENABLE_BT:
			// Retorno da requisição de habilitar bt
			if (resultCode == Activity.RESULT_OK) {
				// BT ligado, prepara para conexões
				setupConnection();
			} else {
				// Caso usuário não habilite o bt, encerra o aplicativo
				Toast.makeText(this, "Bluetooth não ativado...", Toast.LENGTH_SHORT).show();
				finish();
			}
		case RESTART:
			if (resultCode == Activity.RESULT_OK) {  
				if (ListDevices.servidor==false)
				{
					sendMessage("reiniciou");
					mCartaJogador1.setImageAlpha(0);
					mCartaJogador2.setImageAlpha(0);
					mCartasRestantes.setText("");
				}
				else
				{
					iniciaJogo();
				}
			} else {   
				Toast.makeText(this, "Problema ao reiniciar jogo!", Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	} 


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.scan:
			// Inicia ListDevices para procura e conexão de dispositivos
			Intent serverIntent = new Intent(this, ListDevices.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			return true;
		case R.id.discoverable:
			// Garante que o dispositivo está visível para outros
			ensureDiscoverable();
			return true;
		}
		return false;
	}

	private void iniciaJogo() {

		carta1 = null;
		carta2 = null;
		carta3 = null;
		jog1.removeAll(jog1);
		jog2.removeAll(jog2);
		mesa.removeAll(mesa);
		mBaralho.setImageAlpha(255);
		mCartaJogador1.setImageAlpha(0);
		mCartaJogador2.setImageAlpha(0);

		baralho = new Baralho();

		if (ListDevices.servidor==true)
		{
			carta1 = baralho.pesca();
			mCarta1.setImageResource(getImageId(getApplicationContext(), carta1.toString()));
			mCarta1.setImageAlpha(255);
			carta2 = baralho.pesca();
			mCarta2.setImageResource(getImageId(getApplicationContext(), carta2.toString()));
			mCarta2.setImageAlpha(255);
			carta3 = baralho.pesca();
			mCarta3.setImageResource(getImageId(getApplicationContext(), carta3.toString()));
			mCarta3.setImageAlpha(255);

			mCarta1.setEnabled(true);
			mCarta2.setEnabled(true);
			mCarta3.setEnabled(true);

			carta1.setTipo(1);
			carta2.setTipo(1);
			carta3.setTipo(1);


			vez = true;

			/*while (reiniciou == false)
			{
			}*/
			sendMessage(baralho.pesca().stringToSend());

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			sendMessage(baralho.pesca().stringToSend());

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			sendMessage(baralho.pesca().stringToSend());

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}			
	}

	public void rodada()
	{	
		mCartaJogador1.setImageAlpha(255);
		mCartaJogador2.setImageAlpha(255);
		Carta c1 = mesa.get(0);
		Carta c2 = mesa.get(1);

		if (c1.naipe.equals(c2.naipe))
		{
			if (c1.pontos==0 && c2.pontos==0) //as duas sem ponto
			{	
				if (c1.valor>= c2.valor)
				{
					if (c1.id==0)
					{
						vez=true;
					}
					else
					{
						vez=false;
					}
				}
				else
				{
					if (c2.id==0)
					{
						vez=true;
					}
					else
					{
						vez=false;
					}
				}	
			}
			else
			{
				if (c1.pontos!=0 && c2.pontos==0) //c1 tem ponto e c2 nao
				{	

					if (c1.id==0)
					{
						vez=true;
					}
					else
					{
						vez=false;
					}
				}
				else if (c1.pontos==0 && c2.pontos!=0)//c2 tem ponto e c1 nao
				{

					if (c2.id==0)
					{
						vez=true;
					}
					else
					{
						vez=false;
					}
				}
				else // as duas tem pontos
				{
					if (c1.valor <4 && c2.valor<4)
					{
						if (c1.valor>c2.valor)
						{
							if (c1.id==0)
							{
								vez=true;
							}
							else
							{
								vez=false;
							}
						}
						else
						{
							if (c2.id==0)
							{
								vez=true;
							}
							else
							{
								vez=false;
							}
						}
					}
					else if (c1.valor >4 && c2.valor<4)
					{
						if (c2.id==0)
						{
							vez=true;
						}
						else
						{
							vez=false;
						}
					}
					else if (c1.valor <4 && c2.valor>4)
					{
						if (c1.id==0)
						{
							vez=true;
						}
						else
						{
							vez=false;
						}
					}
					else if (c1.valor>4 && c2.valor>4) // as duas são figura
					{
						if (c1.valor >c2.valor)
						{
							if (c1.id==0)
							{
								vez=true;
							}
							else
							{
								vez=false;
							}
						}
						else
						{
							if (c2.id==0)
							{
								vez=true;
							}
							else
							{
								vez=false;
							}
						}
					}
				}

			} // fim else com pontos
		}
		else // naipes diferentes
		{
			if (c1.id==0)
			{
				vez=true;
			}
			else
			{
				vez=false;
			}
		}

		if (vez==false) // envia mensagem dizendo de quem é a vez
		{
			sendMessage("1"+ " " + "0000"+ " ."+"2"+"|"+"00"+" ");
			jog2.add(c1);
			jog2.add(c2);
			mCartasRestantes.setText("Perdeu!");
		}
		else
		{
			sendMessage("0"+ " " + "0000"+ " ."+"2"+"|"+"00"+" ");
			jog1.add(c1);
			jog1.add(c2);
			mCartasRestantes.setText("Ganhou!");
		}

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		mesa.removeAll(mesa);

		if (baralho.getCartasNoBaralho()>0)
		{
			pesca();
		}
		else
		{
			mBaralho.setImageAlpha(0);
			if (carta1==null && carta2==null && carta3==null) // se terminou o jogo
			{

				int pontos1=0;
				int pontos2=0;
				for (Carta i : jog1)
				{
					pontos1 += i.pontos;
				}
				for (Carta i2 : jog2)
				{
					pontos2 += i2.pontos;
				}
				pontosServidor = String.valueOf(pontos1);
				pontosCliente = String.valueOf(pontos2);

				sendMessage("pontosServidor:"+pontos1 + "|pontosCliente:"+pontos2+".");

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				reiniciou = false;
				Intent resultIntent = new Intent(this, Resultado.class);
				startActivityForResult(resultIntent, RESTART);
			}
		}
		
	}

	private void pesca()
	{		
		if (vez==true)
		{
			Carta c = baralho.pesca();

			c.setTipo(1);

			if (carta1==null)
			{
				carta1 = c;
				mCarta1.setImageResource(getImageId(getApplicationContext(), carta1.toString()));
				mCarta1.setImageAlpha(255);
				mCarta1.setEnabled(true);
			}
			else if (carta2==null)
			{
				carta2=c;
				mCarta2.setImageResource(getImageId(getApplicationContext(), carta2.toString()));
				mCarta2.setImageAlpha(255);
				mCarta2.setEnabled(true);
			}
			else if (carta3==null)
			{
				carta3=c;
				mCarta3.setImageResource(getImageId(getApplicationContext(), carta3.toString()));
				mCarta3.setImageAlpha(255);
				mCarta3.setEnabled(true);
			}

			Carta cartaEnviada = baralho.pesca();

			sendMessage(cartaEnviada.stringToSend());
			
		}
		else
		{
			Carta cartaEnviada = baralho.pesca();

			sendMessage(cartaEnviada.stringToSend());
			
			Carta c = baralho.pesca();

			c.setTipo(1);

			if (carta1==null)
			{
				carta1 = c;
				mCarta1.setImageResource(getImageId(getApplicationContext(), carta1.toString()));
				mCarta1.setImageAlpha(255);
				mCarta1.setEnabled(true);
			}
			else if (carta2==null)
			{
				carta2=c;
				mCarta2.setImageResource(getImageId(getApplicationContext(), carta2.toString()));
				mCarta2.setImageAlpha(255);
				mCarta2.setEnabled(true);
			}
			else if (carta3==null)
			{
				carta3=c;
				mCarta3.setImageResource(getImageId(getApplicationContext(), carta3.toString()));
				mCarta3.setImageAlpha(255);
				mCarta3.setEnabled(true);
			}
		}
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void telaResultado()
	{
		Intent resultIntent = new Intent(this, Resultado.class);
		startActivityForResult(resultIntent, RESTART);
	}

	public static ArrayList<Carta> getPontosJog1()
	{
		return jog1;
	}

	public static ArrayList<Carta> getPontosJog2()
	{
		return jog2;
	}


}


