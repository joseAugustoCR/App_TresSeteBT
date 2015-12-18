package com.example.tressete;


import java.util.ArrayList;
import java.util.Collections;

public class Baralho {
	static ArrayList<Carta> baralho = new ArrayList<Carta>(); // Coleçao armazenará as cartas do baralho
	
	
	
	
	Baralho()
	{
	// Naipe Espada
		Carta umEspada = new Carta(1, "espada",0,12);
		baralho.add(umEspada);
		
		Carta doisEspada = new Carta(2, "espada",0,11);
		baralho.add(doisEspada);
		
		Carta tresEspada = new Carta(3, "espada",0,10);
		baralho.add(tresEspada);
		
		Carta quatroEspada = new Carta(4, "espada",0,0);
		baralho.add(quatroEspada);
		
		Carta cincoEspada = new Carta(5, "espada",0,0);
		baralho.add(cincoEspada);
		
		Carta seisEspada = new Carta(6, "espada",0,0);
		baralho.add(seisEspada);
		
		Carta seteEspada = new Carta(7, "espada",0,0);
		baralho.add(seteEspada);
		
		Carta fanteEspada = new Carta(10,"espada",0,2);
		baralho.add(fanteEspada);
		
		Carta cavaloEspada = new Carta(11, "espada",0,3);
		baralho.add(cavaloEspada);
		
		Carta reiEspada = new Carta(12, "espada",0,4);
		baralho.add(reiEspada);
			
	// Naipe Pau
		Carta umPau = new Carta(1, "pau",0,12);
		baralho.add(umPau);
				
		Carta doisPau = new Carta(2, "pau",0,11);
		baralho.add(doisPau);
		
		Carta tresPau = new Carta(3, "pau",0,10);
		baralho.add(tresPau);
				
		Carta quatroPau = new Carta(4, "pau",0,0);
		baralho.add(quatroPau);
			
		Carta cincoPau = new Carta(5, "pau",0,0);
		baralho.add(cincoPau);
				
		Carta seisPau = new Carta(6, "pau",0,0);
		baralho.add(seisPau);
				
		Carta setePau = new Carta(7, "pau",0,0);
		baralho.add(setePau);
				
		Carta fantePau = new Carta(10,"pau",0,2);
		baralho.add(fantePau);
				
		Carta cavaloPau = new Carta(11, "pau",0,3);
		baralho.add(cavaloPau);
				
		Carta reiPau = new Carta(12, "pau",0,4);
		baralho.add(reiPau);
		
	// Naipe Copa
		Carta umCopa = new Carta(1, "copa",0,12);
		baralho.add(umCopa);
						
		Carta doisCopa = new Carta(2, "copa",0,11);
		baralho.add(doisCopa);
				
		Carta tresCopa = new Carta(3, "copa",0,10);
		baralho.add(tresCopa);
						
		Carta quatroCopa = new Carta(4, "copa",0,0);
		baralho.add(quatroCopa);
					
		Carta cincoCopa = new Carta(5, "copa",0,0);
		baralho.add(cincoCopa);
						
		Carta seisCopa = new Carta(6, "copa",0,0);
		baralho.add(seisCopa);
						
		Carta seteCopa = new Carta(7, "copa",0,0);
		baralho.add(seteCopa);
						
		Carta fanteCopa = new Carta(10, "copa",0,2);
		baralho.add(fanteCopa);
						
		Carta cavaloCopa = new Carta(11, "copa",0,3);
		baralho.add(cavaloCopa);
					
		Carta reiCopa = new Carta(12, "copa",0,4);
		baralho.add(reiCopa);
		
	// Naipe Ouro
		Carta umOuro = new Carta(1, "ouro",0,12);
		baralho.add(umOuro);
								
		Carta doisOuro = new Carta(2, "ouro",0,11);
		baralho.add(doisOuro);
						
		Carta tresOuro = new Carta(3, "ouro",0,10);
		baralho.add(tresOuro);
								
		Carta quatroOuro = new Carta(4, "ouro",0,0);
		baralho.add(quatroOuro);
							
		Carta cincoOuro = new Carta(5, "ouro",0,0);
		baralho.add(cincoOuro);
								
		Carta seisOuro = new Carta(6, "ouro",0,0);
		baralho.add(seisOuro);
								
		Carta seteOuro = new Carta(7, "ouro",0,0);
		baralho.add(seteOuro);
								
		Carta fanteOuro = new Carta(10, "ouro",0,2);
		baralho.add(fanteOuro);
								
		Carta cavaloOuro = new Carta(11, "ouro",0,3);
		baralho.add(cavaloOuro);
							
		Carta reiOuro = new Carta(12, "ouro",0,4);
		baralho.add(reiOuro);
		
		Collections.shuffle(baralho);
	}
	
	public Carta pesca()
	{
		if (baralho.size()>0)
		{ 
			Carta a = baralho.get(0);
			baralho.remove(a);
			//a.setTipo(1);

			return a;	
		}	
		return null;
	}
	
	
			
	public int getCartasNoBaralho()
	{
		int t = baralho.size();
		return t;
	}
	
}
