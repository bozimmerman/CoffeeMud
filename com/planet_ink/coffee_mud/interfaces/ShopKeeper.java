package com.planet_ink.coffee_mud.interfaces;
import java.util.*;
public interface ShopKeeper extends Environmental
{
	public final static int DEAL_ANYTHING=0;
	public final static int DEAL_GENERAL=1;
	public final static int DEAL_ARMOR=2;
	public final static int DEAL_MAGIC=3;
	public final static int DEAL_WEAPONS=4;
	public final static int DEAL_PETS=5;
	public final static int DEAL_LEATHER=6;
	public final static int DEAL_INVENTORYONLY=7;
	public final static int DEAL_TRAINER=8;
	public final static int DEAL_CASTER=9;
	public final static int DEAL_JEWELLER=10;
	public final static int DEAL_ALCHEMIST=11;
	public final static int DEAL_BANKER=12;
	public final static int DEAL_LANDSELLER=13;
	public final static int DEAL_ANYTECHNOLOGY=14;
	public final static int DEAL_CLANDSELLER=15;
	public final static int DEAL_FOODSELLER=16;
	public final static int DEAL_BUTCHER=17;
	public final static int DEAL_GROWER=18;
	public final static int DEAL_HIDESELLER=19;
	public final static int DEAL_LUMBERER=20;
	public final static int DEAL_METALSMITH=21;
	public final static int DEAL_STONEYARDER=22;
	public final static int DEAL_CLANBANKER=23;
	public final static int DEAL_INNKEEPER=24;
	public final static int DEAL_SHIPSELLER=25;
	public final static int DEAL_CSHIPSELLER=26;
	
	public final static String[] SOLDCODES={
		"ANYTHING","GENERAL","ARMOR","MAGIC","WEAPONS",
		"PETS","LEATHER","INVENTORY ONLY","TRAINER",
		"CASTER","JEWELLERY","POTIONS","BANKER","LAND",
		"ANY TECHNOLOGY","CLAN LAND","FOODS","MEATS",
	    "VEGETABLES","HIDES","LUMBER","METALS","ROCKS",
		"CLAN BANKER", "INN KEEPER", "SHIP SELLER", "CLAN SHIP SELLER"};
	
	public int whatIsSold();
	public void setWhatIsSold(int newSellCode);
	public void addStoreInventory(Environmental thisThang);
	public int baseStockSize();
	public int totalStockSize();
	public Vector getUniqueStoreInventory();
	public Vector getBaseInventory();
	public String storeKeeperString();
	public Vector removeSellableProduct(String named, MOB mob);
	public void clearStoreInventory();
	public void addStoreInventory(Environmental thisThang, int number, int price);
	public void delStoreInventory(Environmental thisThang);
	public boolean doISellThis(Environmental thisThang);
	public boolean doIHaveThisInStock(String name, MOB mob);
	public int numberInStock(Environmental likeThis);
	public int stockPrice(Environmental likeThis);
	public Environmental getStock(String name, MOB mob);
	public Environmental removeStock(String name, MOB mob);
	public String prejudiceFactors();
	public void setPrejudiceFactors(String factors);
	public int[] yourValue(MOB mob, Environmental product, boolean sellTo);
}