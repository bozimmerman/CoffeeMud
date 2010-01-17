package com.planet_ink.coffee_mud.Commands;
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


import java.util.*;

/*
* <p>Portions Copyright (c) 2003 Jeremy Vyska</p>
* <p>Portions Copyright (c) 2004-2010 Bo Zimmerman</p>


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
@SuppressWarnings("unchecked")
public class Deviations extends StdCommand
{
	public Deviations(){}

	private String[] access={"DEVIATIONS"};
	public String[] getAccessWords(){return access;}
	
	public boolean canBeOrdered(){return true;}

	protected String mobHeader(Faction useFaction)
	{
		StringBuffer str=new StringBuffer();
		str.append("\n\r");
		str.append(CMStrings.padRight("Name",20)+" ");
		str.append(CMStrings.padRight("Lvl",4)+" ");
		str.append(CMStrings.padRight("Att",5)+" ");
		str.append(CMStrings.padRight("Dmg",5)+" ");
		str.append(CMStrings.padRight("Armor",5)+" ");
		str.append(CMStrings.padRight("Speed",5)+" ");
		str.append(CMStrings.padRight("Rejuv",5)+" ");
		if(useFaction!=null)
		    str.append(CMStrings.padRight(useFaction.name(),7)+" ");
		str.append(CMStrings.padRight("Worn",5));
		str.append("\n\r");
		return str.toString();
	}
	protected String itemHeader()
	{
		StringBuffer str=new StringBuffer();
		str.append("\n\r");
		str.append(CMStrings.padRight("Name",20)+" ");
		str.append(CMStrings.padRight("Type",10)+" ");
		str.append(CMStrings.padRight("Lvl",4)+" ");
		str.append(CMStrings.padRight("Att",5)+" ");
		str.append(CMStrings.padRight("Dmg",5)+" ");
		str.append(CMStrings.padRight("Armor",5)+" ");
		str.append(CMStrings.padRight("Value",5)+" ");
		str.append(CMStrings.padRight("Rejuv",5)+" ");
		str.append(CMStrings.padRight("Wght.",4)+" ");
		str.append(CMStrings.padRight("Size",4));
		str.append("\n\r");
		return str.toString();
	}

	public boolean alreadyDone(Environmental E, Vector itemsDone)
	{
		for(int i=0;i<itemsDone.size();i++)
			if(((Environmental)itemsDone.elementAt(i)).sameAs(E))
				return true;
		return false;
	}
	
	private void fillCheckDeviations(Room R, String type, Vector check)
	{
		if(type.equalsIgnoreCase("mobs")||type.equalsIgnoreCase("both"))
		{
			for(int m=0;m<R.numInhabitants();m++)
			{
				MOB M=R.fetchInhabitant(m);
				if((M!=null)&&(M.savable())&&(!alreadyDone(M,check)))
					check.addElement(M);
			}
		}
		if(type.equalsIgnoreCase("items")||type.equalsIgnoreCase("both"))
		{
			for(int i=0;i<R.numItems();i++)
			{
				Item I=R.fetchItem(i);
				if((I!=null)
				&&((I instanceof Armor)||(I instanceof Weapon))
				&&(!alreadyDone(I,check)))
					check.addElement(I);
			}
			for(int m=0;m<R.numInhabitants();m++)
			{
				MOB M=R.fetchInhabitant(m);
				if(M!=null)
				{
					for(int i=0;i<M.inventorySize();i++)
					{
						Item I=M.fetchInventory(i);
						if((I!=null)
						&&((I instanceof Armor)||(I instanceof Weapon))
						&&(!alreadyDone(I,check)))
							check.addElement(I);
					}
					ShopKeeper sk=CMLib.coffeeShops().getShopKeeper(M);
					if(sk!=null)
					{
						Vector V=sk.getShop().getBaseInventory();
						for(int i=0;i<V.size();i++)
							if(V.elementAt(i) instanceof Item)
							{
								Item I=(Item)V.elementAt(i);
								if(((I instanceof Armor)||(I instanceof Weapon))
								&&(!alreadyDone(I,check)))
									check.addElement(I);
							}
					}
				}
			}
		}
	}

	protected String getDeviation(int val, Hashtable vals, String key)
	{
		if(!vals.containsKey(key))
			return " - ";
		int val2=CMath.s_int((String)vals.get(key));
		return getDeviation(val,val2);
	}
	protected String getDeviation(int val, int val2)
	{
		
		if(val==val2) return "0%";
		int oval=val2-val;
		int pval=(int)Math.round(CMath.div((oval<0)?(oval*-1):oval,val2==0?1:val2)*100.0);
		if(oval>0) return "-"+pval+"%";
        return "+"+pval+"%";
	}

	public StringBuffer deviations(MOB mob, String rest)
	{
		Vector V=CMParms.parse(rest);
		if((V.size()==0)
		||((!((String)V.firstElement()).equalsIgnoreCase("mobs"))
		   &&(!((String)V.firstElement()).equalsIgnoreCase("items"))
		   &&(!((String)V.firstElement()).equalsIgnoreCase("both"))))
			return new StringBuffer("You must specify whether you want deviations on MOBS, ITEMS, or BOTH.");

		String type=((String)V.firstElement()).toLowerCase();
		if(V.size()==1)
			return new StringBuffer("You must also specify a mob or item name, or the word room, or the word area.");

		Faction useFaction=null;
		for(Enumeration e=CMLib.factions().factions();e.hasMoreElements();)
		{
		    Faction F=(Faction)e.nextElement();
		    if(F.showInSpecialReported()) useFaction=F;
		        
		}
		String where=((String)V.elementAt(1)).toLowerCase();
		Environmental E=mob.location().fetchFromMOBRoomFavorsItems(mob,null,where,Wearable.FILTER_ANY);
		Vector check=new Vector();
		if(where.equalsIgnoreCase("room"))
			fillCheckDeviations(mob.location(),type,check);
		else
		if(where.equalsIgnoreCase("area"))
		{
			for(Enumeration r=mob.location().getArea().getCompleteMap();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				fillCheckDeviations(R,type,check);
			}
		}
		else
		if(where.equalsIgnoreCase("world"))
		{
			for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				fillCheckDeviations(R,type,check);
			}
		}
		else
		if(E==null)
			return new StringBuffer("'"+where+"' is an unknown item or mob name.");
		else
		if(type.equals("items")
		&&(!(E instanceof Weapon))
		&&(!(E instanceof Armor)))
			return new StringBuffer("'"+where+"' is not a weapon or armor item.");
		else
		if(type.equals("mobs")
		&&(!(E instanceof MOB)))
			return new StringBuffer("'"+where+"' is not a MOB.");
		else
		if((!(E instanceof Weapon))
		&&(!(E instanceof Armor))
		&&(!(E instanceof MOB)))
			return new StringBuffer("'"+where+"' is not a MOB, or Weapon, or Item.");
		else
			check.addElement(E);
		StringBuffer str=new StringBuffer("");
		str.append("Deviations Report:\n\r");
		StringBuffer itemResults = new StringBuffer();
		StringBuffer mobResults = new StringBuffer();
		for(int c=0;c<check.size();c++)
		{
			if(check.elementAt(c) instanceof Item)
			{
				Item I=(Item)check.elementAt(c);
				Weapon W=null;
				if(I instanceof Weapon)
					W=(Weapon)I;
				Hashtable vals=CMLib.itemBuilder().timsItemAdjustments(
								I,I.envStats().level(),I.material(),
								I.rawLogicalAnd()?2:1,
								(W==null)?0:W.weaponClassification(),
								I.maxRange(),
								I.rawProperLocationBitmap());
				itemResults.append(CMStrings.padRight(I.name(),20)+" ");
				itemResults.append(CMStrings.padRight(I.ID(),10)+" ");
				itemResults.append(CMStrings.padRight(""+I.envStats().level(),4)+" ");
				itemResults.append(CMStrings.padRight(""+getDeviation(
												I.baseEnvStats().attackAdjustment(),
												vals,"ATTACK"),5)+" ");
				itemResults.append(CMStrings.padRight(""+getDeviation(
												I.baseEnvStats().damage(),
												vals,"DAMAGE"),5)+" ");
				itemResults.append(CMStrings.padRight(""+getDeviation(
												I.baseEnvStats().damage(),
												vals,"ARMOR"),5)+" ");
				itemResults.append(CMStrings.padRight(""+getDeviation(
												I.baseGoldValue(),
												vals,"VALUE"),5)+" ");
				itemResults.append(CMStrings.padRight(""+((I.envStats().rejuv()==Integer.MAX_VALUE)?" MAX":""+I.envStats().rejuv()),5)+" ");
				if(I instanceof Weapon)
					itemResults.append(CMStrings.padRight(""+I.baseEnvStats().weight(),4));
				else
					itemResults.append(CMStrings.padRight(""+getDeviation(
													I.baseEnvStats().weight(),
													vals, "WEIGHT"), 4)+" ");
				if(I instanceof Armor)
					itemResults.append(CMStrings.padRight(""+((Armor)I).envStats().height(),4));
				else
					itemResults.append(CMStrings.padRight(" - ",4)+" ");
				itemResults.append("\n\r");
			}
			else
			{
				MOB M=(MOB)check.elementAt(c);
				mobResults.append(CMStrings.padRight(M.name(),20)+" ");
				mobResults.append(CMStrings.padRight(""+M.envStats().level(),4)+" ");
				mobResults.append(CMStrings.padRight(""+getDeviation(
												M.baseEnvStats().attackAdjustment(),
												CMLib.leveler().getLevelAttack(M)),5)+" ");
				mobResults.append(CMStrings.padRight(""+getDeviation(
												M.baseEnvStats().damage(),
												(int)Math.round(CMath.div(CMLib.leveler().getLevelMOBDamage(M),M.baseEnvStats().speed()))),5)+" ");
				mobResults.append(CMStrings.padRight(""+getDeviation(
												M.baseEnvStats().armor(),
												CMLib.leveler().getLevelMOBArmor(M)),5)+" ");
				mobResults.append(CMStrings.padRight(""+getDeviation(
												(int)Math.round(M.baseEnvStats().speed()),
												(int)Math.round(CMLib.leveler().getLevelMOBSpeed(M))),5)+" ");
				mobResults.append(CMStrings.padRight(""+((M.envStats().rejuv()==Integer.MAX_VALUE)?" MAX":""+M.envStats().rejuv()) ,5)+" ");
				if(useFaction!=null) 
				    mobResults.append(CMStrings.padRight(""+(M.fetchFaction(useFaction.factionID())==Integer.MAX_VALUE?"N/A":""+M.fetchFaction(useFaction.factionID())),7)+" ");
				int reallyWornCount = 0;
				for(int j=0;j<M.inventorySize();j++)
				{
					Item Iw=M.fetchInventory(j);
					if(!(Iw.amWearingAt(Wearable.IN_INVENTORY)))
						reallyWornCount++;
				}
				mobResults.append(CMStrings.padRight(""+reallyWornCount,5)+" ");
				mobResults.append("\n\r");
			}
		}
		if(itemResults.length()>0) str.append(itemHeader()+itemResults.toString());
		if(mobResults.length()>0) str.append(mobHeader(useFaction)+mobResults.toString());
		return str;
	}

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		mob.tell(deviations(mob,CMParms.combine(commands,1)).toString());
		return false;
	}

	
	public boolean securityCheck(MOB mob)
	{
		return CMSecurity.isAllowedStartsWith(mob,mob.location(),"CMDITEMS")
			|| CMSecurity.isAllowed(mob,mob.location(),"LISTADMIN")
			|| CMSecurity.isAllowed(mob,mob.location(),"CMDMOBS");
	}
}
