package com.planet_ink.coffee_mud.interfaces;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public interface Banker extends ShopKeeper
{
	public final static int DATA_USERID=0;
	public final static int DATA_BANK=1;
	public final static int DATA_KEY=2;
	public final static int DATA_DATA=3;
	
	public void addDepositInventory(String mob, Item thisThang);
	public void addDepositInventory(MOB mob, Item thisThang);
	public boolean delDepositInventory(String mob, Item thisThang);
	public boolean delDepositInventory(MOB mob, Item thisThang);
	public void delAllDeposits(String mob);
	public int numberDeposited(String mob);
	public Vector getAccountNames();
	public Vector getDepositInventory(String mob);
	public Item findDepositInventory(String mob, String likeThis);
	public Item findDepositInventory(MOB mob, String likeThis);
	public void setCoinInterest(double interest);
	public void setItemInterest(double interest);
	public double getCoinInterest();
	public double getItemInterest();
	public String bankChain();
	public void setBankChain(String name);
}