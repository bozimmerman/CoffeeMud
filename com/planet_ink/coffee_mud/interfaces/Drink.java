package com.planet_ink.coffee_mud.interfaces;

public interface Drink extends Environmental
{
	public int thirstQuenched();
	public int liquidHeld();
	public int liquidRemaining();
	public int liquidType();
	public void setLiquidType(int newLiquidType);
		
	public void setThirstQuenched(int amount);
	public void setLiquidHeld(int amount);
	public void setLiquidRemaining(int amount);
	
	public boolean containsDrink();
	
	// poison types are stored in the ability code of poisons
	public final static int POISON_NOT=0;
	public final static int POISON_BEESTING=1;
	public final static int POISON_BLOODBOIL=2;
	public final static int POISON_PEPPERSAUCE=3;
	public final static int POISON_VENOM=4;
	public final static int POISON_MINDSAP=5;
	public final static int POISON_HEARTSTOPPER=6;
	public final static int POISON_GOBLINDRIP=7;
	public final static int POISON_GHOULTOUCH=8;
	public final static int POISON_DECREPTIFIER=9;
	public final static int POISON_XXX=10;
	public final static int POISON_TRANQUILIZER=11;
	public final static int POISON_DRAINING=12;
	public final static String[] Poison_Descs={
		"HARMLESS",
		"BEESTING",
		"BLOODBOIL",
		"PEPPERSAUCE",
		"VENOM",
		"MINDSAP",
		"HEARTSTOPPER",
		"GOBLINDRIP",
		"GHOULTOUCH",
		"DECRETIFIER",
		"XXX",
		"TRANQUILIZER",
		"DRAINING"
	};
	
	public final static int LIQUOR_BEER=0;
	public final static int LIQUOR_LIQUOR=1;
	public final static int LIQUOR_FIREBREATHER=2;
	public final static int LIQUOR_TEQUILA=3;
	public final static String[] Liquor_Descs={
		"BEER/ALE",
		"HARD LIQUOR",
		"FIREBREATHER",
		"TEQUILA"
	};
}
