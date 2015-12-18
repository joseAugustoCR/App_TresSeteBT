package com.example.tressete;


public class Carta {
	int valor,tipo;
	String naipe;
	int pontos;
	int id;
	
	
	
	Carta(int v, String n, int t, int p)
	{
		valor=v;
		naipe =n;
		tipo=t;
		pontos =p;
		id =0;
	
	}
	
	public void setTipo(int t)
	{
		tipo=t;
	}
	public void setID(int t)
	{
		id=t;
	}
	
	public String toString()
	{
		String imprime = naipe+valor;
		return imprime;
	}
	
	public String stringToSend()
	{
		if (pontos<10)
		{
			String imprime = valor + " " + naipe+ " ."+tipo+"|"+"0"+pontos+" ";
			return imprime;
		}
		else
		{
			String imprime = valor + " " + naipe+ " ."+tipo+"|"+pontos+" ";
			return imprime;
		}
		
	}
	
	
/*	public String[] convString()
	{
		String[] string = new String[3]; 
		string[0] = String.valueOf(valor);
		string[1] = naipe;
		string[2] = String.valueOf(tipo);
		return string;
	}*/
}
