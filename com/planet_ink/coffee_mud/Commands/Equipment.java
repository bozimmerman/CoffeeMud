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
import com.planet_ink.coffee_mud.Libraries.interfaces.ListingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2004-2023 Bo Zimmerman

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
public class Equipment extends StdCommand
{
	public Equipment()
	{
	}

	private final String[]	access	= I(new String[] { "EQUIPMENT", "EQ", "EQUIP" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	private final static Class<?>[][] internalParameters=new Class<?>[][]{{MOB.class},{Boolean.class},{}};

	public StringBuilder getEquipment(final MOB seer, final MOB mob, final boolean allPlaces)
	{
		if(CMLib.flags().isSleeping(seer))
			return new StringBuilder("(nothing you can see right now)");

		final int wrap=CMLib.lister().fixColWidth(74,seer.session());
		final int shortWrap=CMLib.lister().fixColWidth(50,seer.session());
		final int headWrap=26;

		final boolean paragraphView=(CMProps.getIntVar(CMProps.Int.EQVIEW)>CMProps.Int.EQVIEW_MIXED)
							||((seer!=mob)&&(CMProps.getIntVar(CMProps.Int.EQVIEW)>CMProps.Int.EQVIEW_DEFAULT))
							||seer.isAttributeSet(MOB.Attrib.COMPRESS);
		final HashSet<Item> alreadyDone=new HashSet<Item>();
		final Wearable.CODES codes = Wearable.CODES.instance();
		final Map<Long,Pair<double[],List<String>>> tattoos = new TreeMap<Long,Pair<double[],List<String>>>();
		for(int l=0;l<codes.all_ordered().length;l++)
		{
			final long wornCode=codes.all_ordered()[l];
			final String wornLocName=codes.name(wornCode).toUpperCase().trim();
			for(final Enumeration<Tattoo> e=mob.tattoos();e.hasMoreElements();)
			{
				final Tattoo T = e.nextElement();
				if(T.getTattooName().startsWith(wornLocName+":"))
				{
					final Long t=Long.valueOf(wornCode);
					if(!tattoos.containsKey(t))
						tattoos.put(t, new Pair<double[],List<String>>(new double[]{0}, new ArrayList<String>()));
					tattoos.get(t).second.add(T.getTattooName().substring(wornLocName.length()+1).trim());
				}
			}
		}
		final Map<Long,String> headers =  new TreeMap<Long,String>();
		for(int l=0;l<codes.all_ordered().length;l++)
		{
			final long wornCode=codes.all_ordered()[l];
			final String wornLocName=codes.name(wornCode);
			final Long wC = Long.valueOf(wornCode);
			if(paragraphView)
				headers.put(wC, " ^!");
			else
			{
				final String headerHead = "^N(^H"+wornLocName+"^?)";
				headers.put(wC, headerHead
								+ CMStrings.SPACES.substring(0,headWrap-headerHead.length())
								+ ": ^!");
			}
		}
		final Map<Long,StringBuilder> strs = new TreeMap<Long, StringBuilder>();
		for(int l=0;l<codes.all_ordered().length;l++)
		{
			final long wornCode=codes.all_ordered()[l];
			final Long wC = Long.valueOf(wornCode);
			final String wornLocHeader = headers.get(wC);
			final StringBuilder msg = new StringBuilder("");
			strs.put(wC, msg);
			final List<Item> wornHere=mob.fetchWornItems(wornCode,(short)(Short.MIN_VALUE+1),(short)0);
			int numLocations=mob.getWearPositions(wornCode);
			if(numLocations==0)
				numLocations=1;
			int emptySlots=numLocations;
			if(wornHere.size()>0)
			{
				if(!allPlaces)
				{
					final List<List<Item>> sets=new Vector<List<Item>>(numLocations);
					for(int i=0;i<numLocations;i++)
						sets.add(new Vector<Item>());
					Item I=null;
					Item I2=null;
					short layer=Short.MAX_VALUE;
					short layerAtt=0;
					short layer2=Short.MAX_VALUE;
					short layerAtt2=0;
					List<Item> set=null;
					for(int i=0;i<wornHere.size();i++)
					{
						I=wornHere.get(i);
						if(I.container()!=null)
							continue;
						if(I instanceof Armor)
						{
							layer=((Armor)I).getClothingLayer();
							layerAtt=((Armor)I).getLayerAttributes();
						}
						else
						{
							layer=0;
							layerAtt=0;
						}
						for(int s=0;s<sets.size();s++)
						{
							set=sets.get(s);
							if(set.size()==0)
							{
								set.add(I);
								break;
							}
							for(int s2=0;s2<set.size();s2++)
							{
								I2=set.get(s2);
								if(I2 instanceof Armor)
								{
									layer2=((Armor)I2).getClothingLayer();
									layerAtt2=((Armor)I2).getLayerAttributes();
								}
								else
								{
									layer2=0;
									layerAtt2=0;
								}
								if(layer2==layer)
								{
									if(((layerAtt&Armor.LAYERMASK_MULTIWEAR)>0)
									&&((layerAtt2&Armor.LAYERMASK_MULTIWEAR)>0))
										set.add(s2,I);
									break;
								}
								if(layer2>layer)
								{
									set.add(s2,I);
									break;
								}
							}
							if(set.contains(I))
								break;
							if(layer2<layer)
							{
								set.add(I);
								break;
							}
						}
					}
					wornHere.clear();
					for(int s=0;s<sets.size();s++)
					{
						set=sets.get(s);
						int s2=set.size()-1;
						for(;s2>=0;s2--)
						{
							I2=set.get(s2);
							wornHere.add(I2);
							if((!(I2 instanceof Armor))
							||(!CMath.bset(((Armor)I2).getLayerAttributes(),Armor.LAYERMASK_SEETHROUGH)))
							{
								emptySlots--;
								break;
							}
						}
					}
				}
				for(int i=0;i<wornHere.size();i++)
				{
					final Item thisItem=wornHere.get(i);
					if((thisItem.container()==null)&&(thisItem.amWearingAt(wornCode)))
					{
						if(paragraphView)
						{
							if(alreadyDone.contains(thisItem))
								continue;
							alreadyDone.add(thisItem);
						}
						if(CMLib.flags().canBeSeenBy(thisItem,seer))
						{
							if(paragraphView)
							{
								String name=thisItem.name();
								if(!allPlaces)
									name=CMStrings.ellipseColored(name,wrap);
								if(wornCode==Wearable.WORN_HELD)
								{
									if(msg.length()==0)
										msg.append(L("nothing."));
									if(mob==seer)
										msg.append(L("\n\rHolding ^<EItem^>@x1^</EItem^>@x2^N",name,CMLib.flags().getDispositionBlurbs(thisItem,seer).toString().trim()));
									else
										msg.append(L("\n\r@x1 is holding @x2@x3^N.",mob.charStats().HeShe(),name.trim(),CMLib.flags().getDispositionBlurbs(thisItem, seer).toString().trim()));
								}
								else
								if(wornCode==Wearable.WORN_WIELD)
								{
									if(msg.length()==0)
										msg.append(L("nothing."));
									if(mob==seer)
										msg.append(L("\n\rWielding ^<EItem^>@x1^</EItem^>@x2^N.",name,CMLib.flags().getDispositionBlurbs(thisItem,seer).toString().trim()));
									else
										msg.append(L("\n\r@x1 is wielding @x2@x3^N.",mob.charStats().HeShe(),name.trim(),CMLib.flags().getDispositionBlurbs(thisItem, seer).toString().trim()));
								}
								else
								{
									if(mob==seer)
										msg.append(wornLocHeader+"^<EItem^>"+name+"^</EItem^>"+CMLib.flags().getDispositionBlurbs(thisItem,seer).toString().trim()+"^N,");
									else
										msg.append(wornLocHeader+name.trim()+CMLib.flags().getDispositionBlurbs(thisItem,seer).toString().trim()+"^N,");
								}
							}
							else
							{
								String name=thisItem.name();
								if((CMStrings.lengthMinusColors(name)>shortWrap)&&(!allPlaces))
									name=CMStrings.ellipseColored(name,shortWrap);
								if(mob==seer)
									msg.append(wornLocHeader+"^<EItem^>"+name+"^</EItem^>"+CMLib.flags().getDispositionBlurbs(thisItem,seer).toString().trim()+"^?\n\r");
								else
									msg.append(wornLocHeader+name.trim()+CMLib.flags().getDispositionBlurbs(thisItem,seer).toString().trim()+"^?\n\r");
							}
						}
						else
						if(seer==mob)
							msg.append(L("@x1(something you can`t see)@x2^?\n\r",wornLocHeader,CMLib.flags().getDispositionBlurbs(thisItem,seer).toString().trim()));
					}
				}
			}
			if(tattoos.size()>0)
			{
				if((emptySlots == 0)||(numLocations == 0))
				{
					if((wC.longValue() == Wearable.WORN_ABOUT_BODY)
					||(wC.longValue() == Wearable.WORN_TORSO))
						tattoos.remove(Long.valueOf(Wearable.WORN_BACK));
					tattoos.remove(wC);
				}
				else
				if(tattoos.containsKey(wC))
					tattoos.get(wC).first[0] = CMath.div(emptySlots,numLocations);
			}
		}
		for(int l=0;l<codes.all_ordered().length;l++)
		{
			final long wornCode=codes.all_ordered()[l];
			final Long wC = Long.valueOf(wornCode);
			final String wornLocHeader = headers.get(wC);
			final Pair<double[], List<String>> p = tattoos.get(wC);
			if(p != null)
			{
				final StringBuilder msg = strs.get(wC);
				final List<String> tatts = p.second;
				final double numTattoosTotal=tatts.size();
				int numTattoosToShow=(int)Math.round(Math.ceil(CMath.mul(numTattoosTotal,p.first[0])));
				for(String tat : tatts)
				{
					if((--numTattoosToShow)>=0)
					{
						if(paragraphView)
						{
							tat=tat.toLowerCase();
							if((tat.length()>wrap)&&(!allPlaces))
								tat=tat.substring(0,wrap)+"...";
							msg.append(wornLocHeader+tat+"^?,");
						}
						else
						{
							tat=CMStrings.capitalizeAndLower(tat);
							if((tat.length()>shortWrap)&&(!allPlaces))
								tat=tat.substring(0,shortWrap)+"...";
							msg.append(wornLocHeader+tat+"^?\n\r");
						}
					}
				}
			}
			if((allPlaces)&&(strs.get(wC).length()==0))
			{
				if(((!paragraphView)&&(wornCode!=Wearable.WORN_FLOATING_NEARBY))
				||((paragraphView)&&(wornCode!=Wearable.WORN_WIELD)))
					for(int i=mob.getWearPositions(wornCode)-1;i>=0;i--)
						strs.get(wC).append(wornLocHeader+"^?\n\r");
			}
		}
		final StringBuilder msg = new StringBuilder();
		for(int l=0;l<codes.all_ordered().length;l++)
		{
			final long wornCode=codes.all_ordered()[l];
			final Long wC = Long.valueOf(wornCode);
			if(strs.containsKey(wC))
				msg.append(strs.get(wC));
		}
		if(msg.length()==0)
			msg.append(L("^!(nothing)^?\n\r"));
		else
		{
			int commaDex=(paragraphView)?msg.lastIndexOf(","):-1;
			if((paragraphView)&&(commaDex > -1))
			{
				msg.insert(commaDex + 1, ".");
				msg.deleteCharAt(commaDex);
				commaDex=msg.lastIndexOf(",");
				if(commaDex > -1)
					msg.insert(commaDex + 1, " and");
			}
		}
		return msg;
	}

	private void viewEquipment(final MOB mob, final boolean longView)
	{
		final Session session=mob.session();
		if(session!=null)
		{
			final boolean paragraphView=(CMProps.getIntVar(CMProps.Int.EQVIEW)==CMProps.Int.EQVIEW_PARAGRAPH);
			if(paragraphView)
			{
				if(longView)
					session.wraplessPrintln(L("You are wearing @x1",getEquipment(mob,mob,true).toString()));
				else
					session.wraplessPrintln(L("You are wearing @x1",getEquipment(mob,mob,false).toString()));
			}
			else
			if(longView)
				session.wraplessPrintln(L("You are wearing:\n\r@x1",getEquipment(mob,mob,true).toString()));
			else
				session.wraplessPrintln(L("You are wearing:\n\r@x1",getEquipment(mob,mob,false).toString()));
		}
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		viewEquipment(mob,(commands.size()>1)&&(CMParms.combine(commands,1).equalsIgnoreCase("long")));
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

	@Override
	public Object executeInternal(final MOB mob, final int metaFlags, final Object... args) throws java.io.IOException
	{
		if(!super.checkArguments(internalParameters, args))
			return null;

		if((args.length>0)&&(args[0] instanceof MOB))
			return getEquipment((MOB)args[0],mob,false);
		else
		if((args.length>0)&&(args[0] instanceof Boolean))
			viewEquipment(mob,((Boolean)args[0]).booleanValue());
		else
			viewEquipment(mob,false);
		return null;
	}

}
