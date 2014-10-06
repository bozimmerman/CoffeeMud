package com.planet_ink.coffee_mud.MOBS.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.MoneyLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.PlayerData;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.List;
import java.util.Vector;


/*
   Copyright 2003-2014 Bo Zimmerman

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
	public final static double MIN_ITEM_BALANCE_DIVIDEND=10.0;

	public String getBankClientName(MOB mob, Clan.Function func, boolean checked);
	public void addDepositInventory(String depositorName, Item thisThang);
	public boolean delDepositInventory(String depositorName, Item thisThang);
	public void delAllDeposits(String depositorName);
	public int numberDeposited(String depositorName);
	public List<String> getAccountNames();
	public List<PlayerData> getRawPDDepositInventory(String depositorName);
	public List<Item> getDepositedItems(String depositorName);
	public Item findDepositInventory(String mob, String likeThis);
	public void setCoinInterest(double interest);
	public void setItemInterest(double interest);
	public void setLoanInterest(double interest);
	public double getLoanInterest();
	public double getCoinInterest();
	public double getItemInterest();
	public String bankChain();
	public void setBankChain(String name);
	public double getBalance(String depositorName);
	public double totalItemsWorth(String depositorName);
	public MoneyLibrary.DebtItem getDebtInfo(String depositorName);
}
