package com.example.tressete;

import java.util.ArrayList;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

	public class Resultado extends Activity{
	@Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        int pontos1=0, pontos2=0;
	
	        setContentView(R.layout.finish);
	
	        // Caso usuário clique em return
	        setResult(Activity.RESULT_CANCELED);
	
	        // Inicialização do botão restart
	        Button restartButton = (Button) findViewById(R.id.button1);
	        restartButton.setOnClickListener(new OnClickListener() {
	            public void onClick(View v) {
	                 // Termina activity
	            	 Intent it = new Intent(Resultado.this, MainActivity.class);
	            	 setResult(Activity.RESULT_OK, it);

	            	 finish();  
	            }
	        });
	        
	        ArrayList<Carta> jog1 = MainActivity.getPontosJog1();   
	        ArrayList<Carta> jog2 =  MainActivity.getPontosJog2();

	        TextView pontosJog1 = (TextView) findViewById(R.id.textView1);
	        TextView  pontosJog2 = (TextView) findViewById(R.id.textView2);
	        
	        for (Carta c : jog1)
	        {
	        	pontos1 += c.pontos;
	        }
	        for (Carta c1 : jog2)
	        {
	        	pontos2 += c1.pontos;
	        }
	        
	        if (ListDevices.servidor==true)
	        {
	        	pontosJog1.setText("Jogador "+MainActivity.mConnectedDeviceName+": " + pontos2);
	   	        pontosJog2.setText("Jogador "+ BluetoothAdapter.getDefaultAdapter().getName() + " : "+pontos1) ;
	        }
	        else
	        {
	        	pontosJog1.setText("Jogador "+MainActivity.mConnectedDeviceName+": " + MainActivity.pontosServidor);
	   	        pontosJog2.setText("Jogador "+ BluetoothAdapter.getDefaultAdapter().getName() + " : "+MainActivity.pontosCliente) ;
	        }
	}  
}
