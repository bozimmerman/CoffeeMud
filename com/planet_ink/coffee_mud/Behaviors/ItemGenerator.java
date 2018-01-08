package com.planet_ink.coffee_mud.Behaviors;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2006-2018 Bo Zimmerman

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
	@Override
	public String ID()
	{
		return "ItemGenerator";
	}

	@Override
	protected int canImproveCode()
	{
		return Behavior.CAN_ROOMS|Behavior.CAN_AREAS|Behavior.CAN_ITEMS|Behavior.CAN_MOBS;
	}

	protected static volatile Tickable[] builerTick=new Tickable[1];

	protected Vector<Item>		maintained			= new Vector<Item>();
	protected int				minItems			= 1;
	protected int				maxItems			= 1;
	protected int				avgItems			= 1;
	protected int				maxDups				= 1;
	protected int				enchantPct			= 10;
	protected boolean			favorMobs			= false;
	protected Vector<Integer>	restrictedLocales	= null;

	@Override
	public String accountForYourself()
	{
		return "item generating";
	}

	private static class GeneratedItemSet extends Vector<Item>
	{
		private static final long serialVersionUID = -4240751718776459599L;
		public double totalValue=0.0;
		public int maxValue=0;
	}

	@Override
	public void setParms(String newParms)
	{
		favorMobs=false;
		maintained=new Vector<Item>();
		restrictedLocales=null;
		String parms=newParms;
		if(parms.indexOf(';')>=0)
			parms=parms.substring(0,parms.indexOf(';'));
		final Vector<String> V=CMParms.parse(parms);
		for(int v=0;v<V.size();v++)
		{
			String s=V.elementAt(v);
			if(s.equalsIgnoreCase("MOBS"))
				favorMobs=true;
			else
			if((s.startsWith("+")||s.startsWith("-"))&&(s.length()>1))
			{
				if(restrictedLocales==null)
					restrictedLocales=new Vector<Integer>();
				if(s.equalsIgnoreCase("+ALL"))
					restrictedLocales.clear();
				else
				if(s.equalsIgnoreCase("-ALL"))
				{
					restrictedLocales.clear();
					for(int i=0;i<Room.DOMAIN_INDOORS_DESCS.length;i++)
						restrictedLocales.addElement(Integer.valueOf(Room.INDOORS+i));
					for(int i=0;i<Room.DOMAIN_OUTDOOR_DESCS.length;i++)
						restrictedLocales.addElement(Integer.valueOf(i));
				}
				else
				{
					final char c=s.charAt(0);
					s=s.substring(1).toUpperCase().trim();
					int code=-1;
					for(int i=0;i<Room.DOMAIN_INDOORS_DESCS.length;i++)
					{
						if(Room.DOMAIN_INDOORS_DESCS[i].startsWith(s))
							code=Room.INDOORS+i;
					}
					if(code>=0)
					{
						if((c=='+')&&(restrictedLocales.contains(Integer.valueOf(code))))
							restrictedLocales.removeElement(Integer.valueOf(code));
						else
						if((c=='-')&&(!restrictedLocales.contains(Integer.valueOf(code))))
							restrictedLocales.addElement(Integer.valueOf(code));
					}
					code=-1;
					for(int i=0;i<Room.DOMAIN_OUTDOOR_DESCS.length;i++)
					{
						if(Room.DOMAIN_OUTDOOR_DESCS[i].startsWith(s))
							code=i;
					}
					if(code>=0)
					{
						if((c=='+')&&(restrictedLocales.contains(Integer.valueOf(code))))
							restrictedLocales.removeElement(Integer.valueOf(code));
						else
						if((c=='-')&&(!restrictedLocales.contains(Integer.valueOf(code))))
							restrictedLocales.addElement(Integer.valueOf(code));
					}
				}
			}
		}
		super.setParms(newParms);
		minItems=CMParms.getParmInt(parms,"minitems",1);
		maxItems=CMParms.getParmInt(parms,"maxitems",1);
		maxDups=CMParms.getParmInt(parms,"maxdups",Integer.MAX_VALUE);
		if(minItems>maxItems)
			maxItems=minItems;
		avgItems=CMLib.dice().roll(1,maxItems-minItems,minItems);
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
		if(newRoom==null)
			return false;
		if(restrictedLocales==null)
			return true;
		return !restrictedLocales.contains(Integer.valueOf(newRoom.domainType()));
	}

	public boolean isStillMaintained(Environmental thang, ShopKeeper SK, Item I)
	{
		if((I==null)||(I.amDestroyed()))
			return false;
		if(SK!=null)
			return SK.getShop().doIHaveThisInStock(I.Name(),null);
		if(thang instanceof Area)
		{
			final Room R=CMLib.map().roomLocation(I);
			if(R==null)
				return false;
			return ((Area)thang).inMyMetroArea(R.getArea());
		}
		else
		if(thang instanceof Room)
			return CMLib.map().roomLocation(I)==thang;
		else
		if(thang instanceof MOB)
			return (I.owner()==thang);
		else
		if(thang instanceof Container)
			return (I.owner()==((Container)thang).owner())&&(I.container()==thang);
		return I.owner()==CMLib.map().roomLocation(thang);
	}

	protected class ItemGenerationTicker implements Tickable
	{
		@Override
		public String ID()
		{
			return "ItemGenerationTicker";
		}

		@Override
		public String name()
		{
			return "ItemGenerationTicker";
		}

		@Override
		public CMObject newInstance()
		{
			return this;
		}

		@Override
		public void initializeClass()
		{
		}

		@Override
		public CMObject copyOf()
		{
			return this;
		}

		@Override
		public int compareTo(CMObject o)
		{
			return (o==this)?1:0;
		}

		private final int tickStatus=0;

		@Override
		public int getTickStatus()
		{
			return tickStatus;
		}

		@Override
		public boolean tick(Tickable host, int tickID)
		{
			@SuppressWarnings("unchecked")
			List<Item> allItems=(List<Item>)Resources.getResource("ITEMGENERATOR-ALLITEMS");
			if(allItems!=null)
				return false;
			allItems=new Vector<Item>();

			final List<ItemCraftor> skills=new Vector<ItemCraftor>();
			for(final Enumeration<Ability> e=CMClass.abilities();e.hasMoreElements();)
			{
				final Ability A=e.nextElement();
				if(A instanceof ItemCraftor)
					skills.add((ItemCraftor)A.copyOf());
			}
			List<ItemCraftor.ItemKeyPair> skillSet=null;
			for(final ItemCraftor skill : skills)
			{
				skillSet=skill.craftAllItemSets(false);
				if(skillSet!=null)
				for(final ItemCraftor.ItemKeyPair materialSet: skillSet)
					allItems.add(materialSet.item);
			}
			Resources.submitResource("ITEMGENERATOR-ALLITEMS",allItems);
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	public synchronized GeneratedItemSet getItems(Tickable thang, String theseparms)
	{
		String mask=parms;
		if(mask.indexOf(';')>=0)
			mask=mask.substring(parms.indexOf(';')+1);
		GeneratedItemSet items=(GeneratedItemSet)Resources.getResource("ITEMGENERATOR-"+mask.toUpperCase().trim());
		if(items==null)
		{
			List<Item> allItems=(List<Item>)Resources.getResource("ITEMGENERATOR-ALLITEMS");
			if(allItems==null)
			{
				synchronized(builerTick)
				{
					allItems=(List<Item>)Resources.getResource("ITEMGENERATOR-ALLITEMS");
					if(allItems==null)
					{
						if(builerTick[0]==null)
						{
							builerTick[0]=new ItemGenerationTicker();
							CMLib.threads().startTickDown(builerTick[0],Tickable.TICKID_ITEM_BEHAVIOR|Tickable.TICKID_LONGERMASK,1234,1);
						}
						return null;
					}
				}
			}
			items=new GeneratedItemSet();
  			Item I=null;
  			final MaskingLibrary.CompiledZMask compiled=CMLib.masking().maskCompile(mask);
			double totalValue=0;
			int maxValue=-1;
			for(int a=0;a<allItems.size();a++)
			{
				I=allItems.get(a);
				if((CMLib.masking().maskCheck(compiled,I,true))
				&&(!(I instanceof ClanItem)))
				{
					if(I.value()>maxValue)
						maxValue=I.value();
					items.add(I);
				}
			}
			for(int a=0;a<items.size();a++)
			{
				I=items.get(a);
				totalValue+=CMath.div(maxValue,I.value()+1);
			}
			if(items.size()>0)
			{
				items.maxValue=maxValue;
				items.totalValue=totalValue;
			}
			Resources.submitResource("ITEMGENERATOR-"+mask.toUpperCase().trim(),items);
		}
		return items;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if((!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
		||(!(ticking instanceof Environmental))
		||(CMSecurity.isDisabled(CMSecurity.DisFlag.RANDOMITEMS)))
			return true;
		Item I=null;
		final Environmental E=(Environmental)ticking;
		final ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(E);
		for(int i=maintained.size()-1;i>=0;i--)
		{
			try
			{
				I=maintained.elementAt(i);
				if(!isStillMaintained(E,SK,I))
					maintained.removeElement(I);
			}
			catch(final Exception e){	}
		}
		if(maintained.size()>=maxItems)
			return true;
		if((canAct(ticking,tickID))||(maintained.size()<minItems))
		{
			final GeneratedItemSet items=getItems(ticking,getParms());
			if(items==null)
				return true;
			if((ticking instanceof Environmental)&&(((Environmental)ticking).amDestroyed()))
				return false;

			if((maintained.size()<avgItems)
			&&(items.size()>1))
			{
				final double totalValue=items.totalValue;
				final int maxValue=items.maxValue;
				double pickedTotal=Math.random()*totalValue;
				double value=-1;
				for(int i=2;i<items.size();i++)
				{
					I=items.elementAt(i);
					value=CMath.div(maxValue,I.value()+1.0);
					if(pickedTotal<=value)
					{
						break;
					}
					pickedTotal-=value;
				}
				if(I!=null)
				{

					if((maxDups<Integer.MAX_VALUE)&&(maxDups>0))
					{
						int numDups=0;
						for(int m=0;m<maintained.size();m++)
						{
							if(I.sameAs(maintained.elementAt(m)))
								numDups++;
						}
						if((maxDups>0)&&(numDups>=maxDups))
							return true;
					}

					I=(Item)I.copyOf();
					I.basePhyStats().setRejuv(PhyStats.NO_REJUV);
					I.recoverPhyStats();
					I.text();
					if(SK!=null)
					{
						if(SK.doISellThis(I))
						{
							maintained.addElement(I);
							SK.getShop().addStoreInventory(CMLib.itemBuilder().enchant(I,enchantPct),1,-1);
						}
					}
					else
					if(ticking instanceof Container)
					{
						if(((Container)ticking).owner() instanceof Room)
							((Room)((Container)ticking).owner()).addItem(CMLib.itemBuilder().enchant(I,enchantPct));
						else
						if(((Container)ticking).owner() instanceof MOB)
							((MOB)((Container)ticking).owner()).addItem(CMLib.itemBuilder().enchant(I,enchantPct));
						else
							return true;
						maintained.addElement(I);
						I.setContainer((Container)ticking);
					}
					else
					if(ticking instanceof MOB)
					{
						((MOB)ticking).addItem(CMLib.itemBuilder().enchant(I,enchantPct));
						I.wearIfPossible((MOB)ticking);
						maintained.addElement(I);
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
									while((room==null)&&((++tries)<100))
										room=((Area)ticking).getRandomMetroRoom();
								}
								else
								{
									int tries=0;
									while(((room==null)||(!okRoomForMe(room)))
									&&((++tries)<100))
										room=((Area)ticking).getRandomMetroRoom();
								}
							}
							else
								return true;
						}
						else
						if(ticking instanceof Environmental)
							room=CMLib.map().roomLocation((Environmental)ticking);
						else
							return true;

						if(room instanceof GridLocale)
							room=((GridLocale)room).getRandomGridChild();
						if(room!=null)
						{
							if(CMLib.flags().isGettable(I)&&(!(I instanceof Rideable)))
							{
								final Vector<MOB> inhabs=new Vector<MOB>();
								for(int m=0;m<room.numInhabitants();m++)
								{
									final MOB M=room.fetchInhabitant(m);
									if((M.isSavable())&&(M.getStartRoom().getArea().inMyMetroArea(room.getArea())))
										inhabs.addElement(M);
								}
								if(inhabs.size()>0)
								{
									final MOB M=inhabs.elementAt(CMLib.dice().roll(1,inhabs.size(),-1));
									M.addItem(CMLib.itemBuilder().enchant(I,enchantPct));
									I.wearIfPossible(M);
									maintained.addElement(I);
								}
							}
							if(!favorMobs)
							{
								maintained.addElement(I);
								room.addItem(CMLib.itemBuilder().enchant(I,enchantPct));
							}
						}
					}
				}
			}
		}
		return true;
	}
}
