package com.planet_ink.coffee_mud.core.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.Vector;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
	public final static int DEAL_SLAVES=27;
    public final static int DEAL_POSTMAN=28;
    public final static int DEAL_CLANPOSTMAN=29;
	
    public static class ShopPrice
    {
        public double absoluteGoldPrice=0.0;
        public int experiencePrice=0;
        public int questPointPrice=0;
    }
    
	public final static String[] SOLDCODES={
		"ANYTHING","GENERAL","ARMOR","MAGIC","WEAPONS",
		"PETS","LEATHER","INVENTORY ONLY","TRAINER",
		"CASTER","JEWELRY","POTIONS","BANKER","LAND",
		"ANY TECHNOLOGY","CLAN LAND","FOODS","MEATS",
	    "VEGETABLES","HIDES","LUMBER","METALS","ROCKS",
		"CLAN BANKER", "INN KEEPER", "SHIP SELLER", 
        "CLAN SHIP SELLER", "SLAVES", "POSTMAN", "CLAN POSTMAN"};

    public CoffeeShop getShop();
	public int whatIsSold();
	public void setWhatIsSold(int newSellCode);
	public String storeKeeperString();
	public boolean doISellThis(Environmental thisThang);
	public String prejudiceFactors();
	public void setPrejudiceFactors(String factors);
    public String ignoreMask();
    public void setIgnoreMask(String factors);
	public String budget();
	public void setBudget(String factors);
	public String devalueRate();
	public void setDevalueRate(String factors);
	public int invResetRate();
	public void setInvResetRate(int ticks);
}