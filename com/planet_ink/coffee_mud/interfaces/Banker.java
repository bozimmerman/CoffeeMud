package com.planet_ink.coffee_mud.interfaces;
import java.util.*;

public interface Banker extends ShopKeeper
{
	public void addDepositInventory(MOB mob, Item thisThang);
	public void delDepositInventory(MOB mob, Item thisThang);
	public void delAllDeposits(MOB mob);
	public int numberDeposited(MOB mob);
	public Vector getDepositInventory(MOB mob);
	public Item findDepositInventory(MOB mob, String likeThis);
	public void setCoinInterest(double interest);
	public void setItemInterest(double interest);
	public double getCoinInterest();
	public double getItemInterest();
	public String bankChain();
	public void setBankChain(String name);
}