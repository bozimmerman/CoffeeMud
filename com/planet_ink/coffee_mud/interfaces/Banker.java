package com.planet_ink.coffee_mud.interfaces;
import java.util.*;

public interface Banker extends ShopKeeper
{
	public void addDepositInventory(String mob, Item thisThang);
	public void delDepositInventory(String mob, Item thisThang);
	public void delAllDeposits(String mob);
	public int numberDeposited(String mob);
	public Vector getAccountNames();
	public Vector getDepositInventory(String mob);
	public Item findDepositInventory(String mob, String likeThis);
	public void setCoinInterest(double interest);
	public void setItemInterest(double interest);
	public double getCoinInterest();
	public double getItemInterest();
	public String bankChain();
	public void setBankChain(String name);
}