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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2005-2018 Bo Zimmerman

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

public class RandomItems extends ActiveTicker
{
	@Override
	public String ID()
	{
		return "RandomItems";
	}

	@Override
	protected int canImproveCode()
	{
		return Behavior.CAN_ROOMS|Behavior.CAN_AREAS|Behavior.CAN_ITEMS|Behavior.CAN_MOBS;
	}

	protected Vector<Item>		maintained			= new Vector<Item>();
	protected int				minItems			= 1;
	protected int				maxItems			= 1;
	protected int				avgItems			= 1;
	protected boolean			favorMobs			= false;
	protected Vector<Integer>	restrictedLocales	= null;
	protected boolean			alreadyTriedLoad	= false;

	@Override
	public String accountForYourself()
	{
		return "random item generating";
	}

	@Override
	public List<String> externalFiles()
	{
		final Vector<String> xmlfiles=new Vector<String>();
		final String theseparms=getParms();
		final int x=theseparms.indexOf(';');
		String filename=(x>=0)?theseparms.substring(x+1):theseparms;
		if(filename.trim().length()==0)
			return null;
		final int start=filename.indexOf("<ITEMS>");
		if((start<0)||(start>20))
		{
			final int extraSemicolon=filename.indexOf(';');
			if(extraSemicolon>=0)
				filename=filename.substring(0,extraSemicolon);
			if(filename.trim().length()>0)
				xmlfiles.addElement(filename.trim());
			return xmlfiles;
		}
		return null;
	}

	@Override
	public void setParms(String newParms)
	{
		favorMobs=false;
		maintained=new Vector<Item>();
		final int x=newParms.indexOf(';');
		String oldParms=newParms;
		restrictedLocales=null;
		if(x>=0)
		{
			oldParms=newParms.substring(0,x).trim();
			String extraParms=oldParms;
			int extraX=newParms.indexOf("<ITEMS>");
			if(extraX<0)
			{
				final String xtra=newParms.substring(x+1);
				extraX=xtra.indexOf(';');
				if(extraX>=0)
					extraParms=xtra.substring(extraX+1);
			}
			final Vector<String> V=CMParms.parse(extraParms);
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
		}
		super.setParms(oldParms);
		minItems=CMParms.getParmInt(oldParms,"minitems",1);
		maxItems=CMParms.getParmInt(oldParms,"maxitems",1);
		if(minItems>maxItems)
			maxItems=minItems;
		avgItems=CMLib.dice().roll(1,(maxItems-minItems),minItems);
		parms=newParms;
		alreadyTriedLoad=false;
		if((restrictedLocales!=null)&&(restrictedLocales.size()==0))
			restrictedLocales=null;
	}

	public RandomItems()
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

	@SuppressWarnings("unchecked")
	public List<Item> getItems(Tickable thang, String theseparms)
	{
		List<Item> items=null;
		final int x=theseparms.indexOf(';');
		String thangName="null";
		if(thang instanceof Room)
			thangName=CMLib.map().getExtendedRoomID((Room)thang);
		else
		if((thang instanceof MOB)&&(((MOB)thang).getStartRoom())!=null)
			thangName=CMLib.map().getExtendedRoomID(((MOB)thang).getStartRoom());
		else
		if(thang!=null)
			thangName=thang.name();
		final String thangID=CMClass.classID(thang);
		String filename=(x>=0)?theseparms.substring(x+1):theseparms;
		if(filename.trim().length()==0)
		{
			if(alreadyTriedLoad)
				return null;
			alreadyTriedLoad=true;
			Log.errOut("RandomItems: Blank XML/filename: '"+filename+"' on object "+thangName+" ("+thangID+").");
			return null;
		}
		final int start=filename.indexOf("<ITEMS>");
		if((start>=0)&&(start<=20))
		{
			int end=start+20;
			if(end>filename.length())
				end=filename.length();
			items=(List<Item>)Resources.getResource("RANDOMITEMS-XML/"+filename.length()+"/"+filename.hashCode());
			if(items!=null)
				return items;
			items=new Vector<Item>();
			final String error=CMLib.coffeeMaker().addItemsFromXML(filename,items,null);
			if(error.length()>0)
			{
				if(alreadyTriedLoad)
					return null;
				alreadyTriedLoad=true;
				Log.errOut("RandomItems: Error on import of xml for '"+thangName+"' ("+thangID+"): "+error+".");
				return null;
			}
			if(items.size()<=0)
			{
				if(alreadyTriedLoad)
					return null;
				alreadyTriedLoad=true;
				Log.errOut("RandomItems: No items loaded for '"+thangName+"' ("+thangID+").");
				return null;
			}
			Resources.submitResource("RANDOMITEMS-XML/"+filename.length()+"/"+filename.hashCode(),items);
		}
		else
		{
			final int extraSemicolon=filename.indexOf(';');
			if(extraSemicolon>=0)
				filename=filename.substring(0,extraSemicolon);
			filename=filename.trim();
			items=(List<Item>)Resources.getResource("RANDOMITEMS-"+filename);
			if((items==null)&&(!alreadyTriedLoad))
			{
				alreadyTriedLoad=true;
				final StringBuffer buf=Resources.getFileResource(filename,true);

				if((buf==null)||(buf.length()<20))
				{
					Log.errOut("RandomItems: Unknown XML file: '"+filename+"' for '"+thangName+"' ("+thangID+").");
					return null;
				}
				if(buf.substring(0,20).indexOf("<ITEMS>")<0)
				{
					Log.errOut("RandomItems: Invalid XML file: '"+filename+"' for '"+thangName+"' ("+thangID+").");
					return null;
				}
				items=new Vector<Item>();
				final String error=CMLib.coffeeMaker().addItemsFromXML(buf.toString(),items,null);
				if(error.length()>0)
				{
					Log.errOut("RandomItems: Error on import of: '"+filename+"' for '"+thangName+"' ("+thangID+"): "+error+".");
					return null;
				}
				if(items.size()<=0)
				{
					Log.errOut("RandomItems: No items loaded: '"+filename+"' for '"+thangName+"' ("+thangID+").");
					return null;
				}

				Resources.submitResource("RANDOMITEMS-"+filename,items);
			}
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
			final List<Item> items=getItems(ticking,getParms());
			if(items==null)
				return true;
			int attempts=10;
			if((ticking instanceof Environmental)&&(((Environmental)ticking).amDestroyed()))
				return false;
			while((maintained.size()<avgItems)&&(((--attempts)>0)))
			{
				I=items.get(CMLib.dice().roll(1,items.size(),-1));
				if(I!=null)
				{
					I=(Item)I.copyOf();
					I.basePhyStats().setRejuv(PhyStats.NO_REJUV);
					I.recoverPhyStats();
					I.text();
					if(SK!=null)
					{
						if(SK.doISellThis(I))
						{
							maintained.addElement(I);
							SK.getShop().addStoreInventory((Environmental)ticking,1,-1);
						}
					}
					else
					if(ticking instanceof Container)
					{
						if(((Container)ticking).owner() instanceof Room)
							((Room)((Container)ticking).owner()).addItem(I);
						else
						if(((Container)ticking).owner() instanceof MOB)
							((MOB)((Container)ticking).owner()).addItem(I);
						else
							break;
						maintained.addElement(I);
						I.setContainer((Container)ticking);
					}
					else
					if(ticking instanceof MOB)
					{
						((MOB)ticking).addItem(I);
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
									while((room==null)
									&&((++tries)<100))
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
								break;
						}
						else
						if(ticking instanceof Environmental)
							room=CMLib.map().roomLocation((Environmental)ticking);
						else
							break;

						if(room instanceof GridLocale)
							room=((GridLocale)room).getRandomGridChild();
						if(room!=null)
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
								M.addItem(I);
								I.wearIfPossible(M);
								maintained.addElement(I);
							}
							if(!favorMobs)
							{
								maintained.addElement(I);
								room.addItem(I);
							}
						}
					}
				}
			}
		}
		return true;
	}
}
