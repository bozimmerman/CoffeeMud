package com.planet_ink.coffee_mud.Commands;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
* <p>Portions Copyright (c) 2003 Jeremy Vyska</p>
* <p>Portions Copyright (c) 2004-2018 Bo Zimmerman</p>

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

public class Deviations extends StdCommand
{
	public Deviations(){}

	private final String[]	access	= I(new String[] { "DEVIATIONS" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

	protected String mobHeader(Faction useFaction)
	{
		final StringBuffer str=new StringBuffer();
		str.append("\n\r");
		str.append(CMStrings.padRight(L("Name"),20)+" ");
		str.append(CMStrings.padRight(L("Lvl"),4)+" ");
		str.append(CMStrings.padRight(L("Att"),5)+" ");
		str.append(CMStrings.padRight(L("Dmg"),5)+" ");
		str.append(CMStrings.padRight(L("Armor"),5)+" ");
		str.append(CMStrings.padRight(L("Speed"),5)+" ");
		str.append(CMStrings.padRight(L("Rejuv"),5)+" ");
		if(useFaction!=null)
			str.append(CMStrings.padRight(useFaction.name(),7)+" ");
		str.append(CMStrings.padRight(L("Money"),5)+" ");
		str.append(CMStrings.padRight(L("Worn"),5));
		str.append("\n\r");
		return str.toString();
	}

	protected String itemHeader()
	{
		final StringBuffer str=new StringBuffer();
		str.append("\n\r");
		str.append(CMStrings.padRight(L("Name"),20)+" ");
		str.append(CMStrings.padRight(L("Type"),10)+" ");
		str.append(CMStrings.padRight(L("Lvl"),4)+" ");
		str.append(CMStrings.padRight(L("Att"),5)+" ");
		str.append(CMStrings.padRight(L("Dmg"),5)+" ");
		str.append(CMStrings.padRight(L("Armor"),5)+" ");
		str.append(CMStrings.padRight(L("Value"),5)+" ");
		str.append(CMStrings.padRight(L("Rejuv"),5)+" ");
		str.append(CMStrings.padRight(L("Wght."),4)+" ");
		str.append(CMStrings.padRight(L("Size"),4));
		str.append("\n\r");
		return str.toString();
	}

	public boolean alreadyDone(Environmental E, Vector<Environmental> itemsDone)
	{
		for(int i=0;i<itemsDone.size();i++)
		{
			if(itemsDone.get(i).sameAs(E))
				return true;
		}
		return false;
	}

	private void fillCheckDeviations(Room R, String type, Vector<Environmental> check)
	{
		if(type.equalsIgnoreCase("mobs")||type.equalsIgnoreCase("both"))
		{
			for(int m=0;m<R.numInhabitants();m++)
			{
				final MOB M=R.fetchInhabitant(m);
				if((M!=null)&&(M.isSavable())&&(!alreadyDone(M,check)))
					check.add(M);
			}
		}
		if(type.equalsIgnoreCase("items")||type.equalsIgnoreCase("both"))
		{
			for(int i=0;i<R.numItems();i++)
			{
				final Item I=R.getItem(i);
				if((I!=null)
				&&((I instanceof Armor)||(I instanceof Weapon))
				&&(!alreadyDone(I,check)))
					check.add(I);
			}
			for(int m=0;m<R.numInhabitants();m++)
			{
				final MOB M=R.fetchInhabitant(m);
				if(M!=null)
				{
					for(int i=0;i<M.numItems();i++)
					{
						final Item I=M.getItem(i);
						if((I!=null)
						&&((I instanceof Armor)||(I instanceof Weapon))
						&&(!alreadyDone(I,check)))
							check.add(I);
					}
					final ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(M);
					if(SK!=null)
					{
						for(final Iterator<Environmental> i=SK.getShop().getStoreInventory();i.hasNext();)
						{
							final Environmental E2=i.next();
							if(E2 instanceof Item)
							{
								final Item I=(Item)E2;
								if(((I instanceof Armor)||(I instanceof Weapon))
								&&(!alreadyDone(I,check)))
									check.add(I);
							}
						}
					}
				}
			}
		}
	}

	protected String getDeviation(int val, Map<String, String> vals, String key)
	{
		if(!vals.containsKey(key))
			return " - ";
		final int val2=CMath.s_int(vals.get(key));
		return getDeviation(val,val2);
	}
	
	protected String getDeviation(double val, double val2)
	{
		if(val==val2)
			return "0%";
		final double oval=val2-val;
		final int pval=(int)Math.round(CMath.div((oval<0)?(oval*-1):oval,val2==0?1:val2)*100.0);
		if(oval>0)
			return "-"+pval+"%";
		return "+"+pval+"%";
	}

	public StringBuffer deviations(MOB mob, String rest)
	{
		final Vector<String> V=CMParms.parse(rest);
		if((V.size()==0)
		||((!V.get(0).equalsIgnoreCase("mobs"))
		   &&(!V.get(0).equalsIgnoreCase("items"))
		   &&(!V.get(0).equalsIgnoreCase("both"))))
			return new StringBuffer("You must specify whether you want deviations on MOBS, ITEMS, or BOTH.");

		final String type=V.get(0).toLowerCase();
		if(V.size()==1)
			return new StringBuffer("You must also specify a mob or item name, or the word room, or the word area.");

		final Room mobR=mob.location();
		Faction useFaction=null;
		for(final Enumeration<Faction> e=CMLib.factions().factions();e.hasMoreElements();)
		{
			final Faction F=e.nextElement();
			if(F.showInSpecialReported())
				useFaction=F;
		}
		final String where=V.get(1).toLowerCase();
		final Environmental E=mobR.fetchFromMOBRoomFavorsItems(mob,null,where,Wearable.FILTER_ANY);
		final Vector<Environmental> check=new Vector<Environmental>();
		if(where.equalsIgnoreCase("room"))
			fillCheckDeviations(mobR,type,check);
		else
		if(where.equalsIgnoreCase("area"))
		{
			for(final Enumeration<Room> r=mobR.getArea().getFilledCompleteMap();r.hasMoreElements();)
			{
				final Room R=r.nextElement();
				fillCheckDeviations(R,type,check);
			}
		}
		else
		if(where.equalsIgnoreCase("world"))
		{
			for(final Enumeration<Room> r=CMLib.map().roomsFilled();r.hasMoreElements();)
			{
				final Room R=r.nextElement();
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
			check.add(E);
		final StringBuffer str=new StringBuffer("");
		str.append(L("Deviations Report:\n\r"));
		final StringBuffer itemResults = new StringBuffer();
		final StringBuffer mobResults = new StringBuffer();
		for(int c=0;c<check.size();c++)
		{
			if(check.get(c) instanceof Item)
			{
				final Item I=(Item)check.get(c);
				Weapon W=null;
				if(I instanceof Weapon)
					W=(Weapon)I;
				final Map<String,String> vals=CMLib.itemBuilder().timsItemAdjustments(
										I,I.phyStats().level(),I.material(),
										I.rawLogicalAnd()?2:1,
										(W==null)?0:W.weaponClassification(),
										I.maxRange(),
										I.rawProperLocationBitmap());
				itemResults.append(CMStrings.padRight(I.name(),20)+" ");
				itemResults.append(CMStrings.padRight(I.ID(),10)+" ");
				itemResults.append(CMStrings.padRight(""+I.phyStats().level(),4)+" ");
				itemResults.append(CMStrings.padRight(""+getDeviation(
												I.basePhyStats().attackAdjustment(),
												vals,"ATTACK"),5)+" ");
				itemResults.append(CMStrings.padRight(""+getDeviation(
												I.basePhyStats().damage(),
												vals,"DAMAGE"),5)+" ");
				itemResults.append(CMStrings.padRight(""+getDeviation(
												I.basePhyStats().damage(),
												vals,"ARMOR"),5)+" ");
				itemResults.append(CMStrings.padRight(""+getDeviation(
												I.baseGoldValue(),
												vals,"VALUE"),5)+" ");
				itemResults.append(CMStrings.padRight(""+((I.phyStats().rejuv()==PhyStats.NO_REJUV)?" MAX":""+I.phyStats().rejuv()),5)+" ");
				if(I instanceof Weapon)
					itemResults.append(CMStrings.padRight(""+I.basePhyStats().weight(),4));
				else
					itemResults.append(CMStrings.padRight(""+getDeviation(
													I.basePhyStats().weight(),
													vals, "WEIGHT"), 4)+" ");
				if(I instanceof Armor)
					itemResults.append(CMStrings.padRight(""+((Armor)I).phyStats().height(),4));
				else
					itemResults.append(CMStrings.padRight(" - ",4)+" ");
				itemResults.append("\n\r");
			}
			else
			{
				final MOB M=(MOB)check.get(c);
				mobResults.append(CMStrings.padRight(M.name(),20)+" ");
				mobResults.append(CMStrings.padRight(""+M.phyStats().level(),4)+" ");
				mobResults.append(CMStrings.padRight(""+getDeviation(
												M.basePhyStats().attackAdjustment(),
												CMLib.leveler().getLevelAttack(M)),5)+" ");
				mobResults.append(CMStrings.padRight(""+getDeviation(
												M.basePhyStats().damage(),
												CMLib.leveler().getLevelMOBDamage(M)),5)+" ");
				mobResults.append(CMStrings.padRight(""+getDeviation(
												M.basePhyStats().armor(),
												CMLib.leveler().getLevelMOBArmor(M)),5)+" ");
				mobResults.append(CMStrings.padRight(""+getDeviation(
												M.basePhyStats().speed(),
												CMLib.leveler().getLevelMOBSpeed(M)),5)+" ");
				mobResults.append(CMStrings.padRight(""+((M.phyStats().rejuv()==PhyStats.NO_REJUV)?" MAX":""+M.phyStats().rejuv()) ,5)+" ");
				if(useFaction!=null)
					mobResults.append(CMStrings.padRight(""+(M.fetchFaction(useFaction.factionID())==Integer.MAX_VALUE?"N/A":""+M.fetchFaction(useFaction.factionID())),7)+" ");
				double value = CMLib.beanCounter().getTotalAbsoluteNativeValue(M);
				double[] range = CMLib.leveler().getLevelMoneyRange(M);
				if(value < range[0])
					mobResults.append(CMStrings.padRight(""+getDeviation(value,range[0]),5)+" ");
				else
				if(value > range[1])
					mobResults.append(CMStrings.padRight(""+getDeviation(value,range[1]),5)+" ");
				else
					mobResults.append(CMStrings.padRight("0%",5)+" ");
				int reallyWornCount = 0;
				for(int j=0;j<M.numItems();j++)
				{
					final Item Iw=M.getItem(j);
					if(!(Iw.amWearingAt(Wearable.IN_INVENTORY)))
						reallyWornCount++;
				}
				mobResults.append(CMStrings.padRight(""+reallyWornCount,5)+" ");
				mobResults.append("\n\r");
			}
		}
		if(itemResults.length()>0) 
			str.append(itemHeader()+itemResults.toString());
		if(mobResults.length()>0)
			str.append(mobHeader(useFaction)+mobResults.toString());
		return str;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		mob.tell(deviations(mob,CMParms.combine(commands,1)).toString());
		return false;
	}

	@Override
	public boolean securityCheck(MOB mob)
	{
		return CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDITEMS)
			|| CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.LISTADMIN)
			|| CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDMOBS);
	}
}
