package com.planet_ink.coffee_mud.interfaces;
import java.util.*;
public interface ShopKeeper extends MOB
{
	public final static int ANYTHING=0;
	public final static int GENERAL=1;
	public final static int ARMOR=2;
	public final static int MAGIC=3;
	public final static int WEAPONS=4;
	public final static int PETS=5;
	public final static int LEATHER=6;
	public final static int ONLYBASEINVENTORY=7;
	public final static int TRAINER=8;
	public final static int CASTER=9;
	public final static int JEWELLER=10;
	public final static int ALCHEMIST=11;
	public final static int BANKER=12;
	public final static int LANDSELLER=13;
	
	public final static String[] SOLDCODES={
		"ANYTHING","GENERAL","ARMOR","MAGIC","WEAPONS",
		"PETS","LEATHER","ONLYBASEINVENTORY","TRAINER",
		"CASTER","JEWELLERY","POTIONS","BANKER","LAND"};
	
	public int whatIsSold();
	public void setWhatIsSold(int newSellCode);
	public void addStoreInventory(Environmental thisThang);
	public int baseStockSize();
	public int totalStockSize();
	public Vector getUniqueStoreInventory();
	public Vector getBaseInventory();
	public String storeKeeperString();
	public void clearStoreInventory();
	public void addStoreInventory(Environmental thisThang, int number);
	public void delStoreInventory(Environmental thisThang);
	public boolean doISellThis(Environmental thisThang);
	public boolean doIHaveThisInStock(String name, MOB mob);
	public int numberInStock(Environmental likeThis);
	public Environmental getStock(String name, MOB mob);
	public Environmental removeStock(String name, MOB mob);
}