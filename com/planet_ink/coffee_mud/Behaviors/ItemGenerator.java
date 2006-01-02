package com.planet_ink.coffee_mud.Behaviors;
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
   Copyright 2000-2006 Bo Zimmerman

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
public class ItemGenerator extends ActiveTicker
{
	public String ID(){return "ItemGenerator";}
	protected int canImproveCode(){return Behavior.CAN_ROOMS|Behavior.CAN_AREAS|Behavior.CAN_ITEMS|Behavior.CAN_MOBS;}

	protected Vector maintained=new Vector();
	protected int minItems=1;
	protected int maxItems=1;
	protected int enchantPct=10;
	protected boolean favorMobs=false;
	protected Vector restrictedLocales=null;
	
	public void setParms(String newParms)
	{
		favorMobs=false;
        maintained=new Vector();
		restrictedLocales=null;
		String parms=newParms;
		if(parms.indexOf(";")>=0)
			parms=parms.substring(0,parms.indexOf(";"));
		Vector V=CMParms.parse(parms);
		for(int v=0;v<V.size();v++)
		{
			String s=(String)V.elementAt(v);
			if(s.equalsIgnoreCase("MOBS"))
				favorMobs=true;
			else
			if((s.startsWith("+")||s.startsWith("-"))&&(s.length()>1))
			{
				if(restrictedLocales==null)
					restrictedLocales=new Vector();
				if(s.equalsIgnoreCase("+ALL"))
					restrictedLocales.clear();
				else
				if(s.equalsIgnoreCase("-ALL"))
				{
					restrictedLocales.clear();
					for(int i=0;i<Room.indoorDomainDescs.length;i++)
						restrictedLocales.addElement(new Integer(Room.INDOORS+i));
					for(int i=0;i<Room.outdoorDomainDescs.length;i++)
						restrictedLocales.addElement(new Integer(i));
				}
				else
				{
					char c=s.charAt(0);
					s=s.substring(1).toUpperCase().trim();
					int code=-1;
					for(int i=0;i<Room.indoorDomainDescs.length;i++)
						if(Room.indoorDomainDescs[i].startsWith(s))
							code=Room.INDOORS+i;
					if(code>=0)
					{
						if((c=='+')&&(restrictedLocales.contains(new Integer(code))))
							restrictedLocales.removeElement(new Integer(code));
						else
						if((c=='-')&&(!restrictedLocales.contains(new Integer(code))))
							restrictedLocales.addElement(new Integer(code));
					}
					code=-1;
					for(int i=0;i<Room.outdoorDomainDescs.length;i++)
						if(Room.outdoorDomainDescs[i].startsWith(s))
							code=i;
					if(code>=0)
					{
						if((c=='+')&&(restrictedLocales.contains(new Integer(code))))
							restrictedLocales.removeElement(new Integer(code));
						else
						if((c=='-')&&(!restrictedLocales.contains(new Integer(code))))
							restrictedLocales.addElement(new Integer(code));
					}

				}
			}
		}
		super.setParms(newParms);
		minItems=CMParms.getParmInt(parms,"minitems",1);
		maxItems=CMParms.getParmInt(parms,"maxitems",1);
		if(minItems>maxItems) maxItems=minItems;
		enchantPct=CMParms.getParmInt(parms,"enchanted",10);
		if((restrictedLocales!=null)&&(restrictedLocales.size()==0))
			restrictedLocales=null;
	}

	public ItemGenerator()
	{
        super();
		tickReset();
	}


	public boolean okRoomForMe(Room newRoom)
	{
		if(newRoom==null) return false;
		if(restrictedLocales==null) return true;
		return !restrictedLocales.contains(new Integer(newRoom.domainType()));
	}
	
	public boolean isStillMaintained(Environmental thang, ShopKeeper SK, Item I)
	{
		if((I==null)||(I.amDestroyed())) return false;
		if(SK!=null) return SK.getShop().doIHaveThisInStock(I.Name(),null,SK.whatIsSold(),null);
		if(thang instanceof Area)
		{
			Room R=CMLib.utensils().roomLocation(I);
			if(R==null) return false;
			return ((Area)thang).inMetroArea(R.getArea());
		}
		else
        if(thang instanceof Room)
        	return CMLib.utensils().roomLocation(I)==thang;
        else
	    if(thang instanceof MOB)
	    	return (I.owner()==thang);
	    else
	    if(thang instanceof Container)
	    	return (I.owner()==((Container)thang).owner())&&(I.container()==thang);
    	return I.owner()==CMLib.utensils().roomLocation(thang);
	}

	public synchronized Vector getItems(Tickable thang, String theseparms)
	{
		String mask=parms;
		if(mask.indexOf(";")>=0) mask=mask.substring(parms.indexOf(";")+1);
		Vector items=(Vector)Resources.getResource("ITEMGENERATOR-"+mask.toUpperCase().trim());
		if(items==null)
		{
			items=new Vector();
			Vector allItems=null;
			synchronized(ID())
			{
				allItems=(Vector)Resources.getResource("ITEMGENERATOR-ALLITEMS");
				if(allItems==null)
				{
					allItems=new Vector();
				    Vector skills=new Vector();
					for(Enumeration e=CMClass.abilities();e.hasMoreElements();)
					{
						Ability A=(Ability)e.nextElement();
						if(A instanceof ItemCraftor)
							skills.addElement(A.copyOf());
					}
					Vector V=null;
					for(int s=0;s<skills.size();s++)
					{
						V=((ItemCraftor)skills.elementAt(s)).craftAllItemsVectors();
						if(V!=null)
						for(int v=0;v<V.size();v++)
							allItems.addElement(((Vector)V.elementAt(v)).firstElement());
					}
					Resources.submitResource("ITEMGENERATOR-ALLITEMS",allItems);
				}
			}
			Item I=null;
			Vector compiled=CMLib.masking().maskCompile(mask);
			for(int a=0;a<allItems.size();a++)
			{
				I=(Item)allItems.elementAt(a);
				if(CMLib.masking().maskCheck(compiled,I))
					items.addElement(I);
			}
			Resources.submitResource("ITEMGENERATOR-"+mask.toUpperCase().trim(),items);
		}
		return items;
	}

	public Item enchant(Item I, int pct)
	{
		if(CMLib.dice().rollPercentage()>pct) return I;
		if(I instanceof Ammunition)
		{
			
		}
		else
		if(I instanceof Weapon)
		{
		}
		else
		if(I instanceof Armor)
		{
			
		}
		return I;
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if((!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
	    ||(!(ticking instanceof Environmental))
		||(CMSecurity.isDisabled("RANDOMITEMS")))
			return true;
		Item I=null;
		Environmental E=(Environmental)ticking;
		ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(E);
		for(int i=maintained.size()-1;i>=0;i--)
		{
			try
			{
				I=(Item)maintained.elementAt(i);
				if(!isStillMaintained(E,SK,I)) maintained.removeElement(I);
			} catch(Exception e){	}
		}
		if(maintained.size()>=maxItems)
			return true;
		if((canAct(ticking,tickID))||(maintained.size()<minItems))
		{
			Vector items=getItems(ticking,getParms());
			if(items==null) return true;
			int num=minItems;
			if(maintained.size()>=minItems)
				num=maintained.size()+1;
			if(num>maxItems) num=maxItems;
			int attempts=30;
			if((ticking instanceof Environmental)&&(((Environmental)ticking).amDestroyed()))
				return false; 
			while((maintained.size()<num)&&(((--attempts)>0)))
			{
				I=(Item)items.elementAt(CMLib.dice().roll(1,items.size(),-1));
				if(I!=null)
				{
					I=(Item)I.copyOf();
					I.baseEnvStats().setRejuv(0);
					I.recoverEnvStats();
					I.text();
					if(SK!=null)
					{
						if(SK.doISellThis(I))
						{
							maintained.addElement(I);
							SK.getShop().addStoreInventory(enchant(I,enchantPct),1,-1,SK);
						}
					}
					else
				    if(ticking instanceof Container)
				    {
				    	if(((Container)ticking).owner() instanceof Room)
				    		((Room)((Container)ticking).owner()).addItem(enchant(I,enchantPct));
				    	else
				    	if(((Container)ticking).owner() instanceof MOB)
				    		((MOB)((Container)ticking).owner()).addInventory(enchant(I,enchantPct));
				    	else
				    		break;
						maintained.addElement(I);
				    	I.setContainer((Container)ticking);
				    }
					else
				    if(ticking instanceof MOB)
				    {
			    		((MOB)ticking).addInventory(enchant(I,enchantPct));
			    		I.wearIfPossible((MOB)ticking);
						maintained.addElement(I);
				    	I.setContainer((Container)ticking);
				    }
				    else
					{
						Room room=null;
						if(ticking instanceof Room)
							room=(Room)ticking;
						else
						if(ticking instanceof Area)
						{
							if(((Area)ticking).metroSize()>0)
							{
								Resources.removeResource("HELP_"+ticking.name().toUpperCase());
								if(restrictedLocales==null)
								{
									int tries=0;
									while(((room==null)||(room.roomID().length()==0))
									&&((++tries)<100))
										room=((Area)ticking).getRandomMetroRoom();
								}
								else
								{
									Vector map=new Vector();
									for(Enumeration e=((Area)ticking).getMetroMap();e.hasMoreElements();)
									{
										Room R=(Room)e.nextElement();
										if(okRoomForMe(R)
										&&(R.roomID().trim().length()>0))
											map.addElement(R);
									}
									if(map.size()>0)
										room=(Room)map.elementAt(CMLib.dice().roll(1,map.size(),-1));
								}
							}
							else
								break;
						}
						else
					    if(ticking instanceof Environmental)
							room=CMLib.utensils().roomLocation((Environmental)ticking);
						else
							break;
							
						if(room instanceof GridLocale)
							room=((GridLocale)room).getRandomChild();
						if(room!=null)
						{
							Vector inhabs=new Vector();
							for(int m=0;m<room.numInhabitants();m++)
							{
								MOB M=room.fetchInhabitant(m);
								if((M.savable())&&(M.getStartRoom().getArea().inMetroArea(room.getArea())))
									inhabs.addElement(M);
							}
							if(inhabs.size()>0)
							{
								MOB M=(MOB)inhabs.elementAt(CMLib.dice().roll(1,inhabs.size(),-1));
								M.addInventory(enchant(I,enchantPct));
								I.wearIfPossible(M);
								maintained.addElement(I);
							}
							if((!favorMobs)&&(room!=null))
							{
								maintained.addElement(I);
								room.addItem(enchant(I,enchantPct));
							}
						}
					}
				}
			}
		}
		return true;
	}
}