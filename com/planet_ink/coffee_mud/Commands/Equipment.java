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
   Copyright 2000-2012 Bo Zimmerman

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
@SuppressWarnings({"unchecked","rawtypes"})
public class Equipment extends StdCommand
{
	public Equipment(){}

	private final String[] access={"EQUIPMENT","EQ","EQUIP"};
	public String[] getAccessWords(){return access;}

	private final static Class[][] internalParameters=new Class[][]{{MOB.class},{Boolean.class},{}};
	
	public static StringBuilder getEquipment(MOB seer, MOB mob, boolean allPlaces)
	{
		StringBuilder msg=new StringBuilder("");
		if(CMLib.flags().isSleeping(seer))
			return new StringBuilder("(nothing you can see right now)");

		int wrap=ListingLibrary.ColFixer.fixColWidth(74,seer.session());
		int shortWrap=ListingLibrary.ColFixer.fixColWidth(50,seer.session());
		int headWrap=26;
		
		long wornCode=0;
		String header=null;
		String wornName=null;
		Item thisItem=null;
		String tat=null;
		boolean paragraphView=(CMProps.getIntVar(CMProps.SYSTEMI_EQVIEW)>1)
							||((seer!=mob)&&(CMProps.getIntVar(CMProps.SYSTEMI_EQVIEW)>0))
							||CMath.bset(seer.getBitmap(),MOB.ATT_COMPRESS);
		HashSet alreadyDone=new HashSet();
		Wearable.CODES codes = Wearable.CODES.instance();
		for(int l=0;l<codes.all_ordered().length;l++)
		{
			wornCode=codes.all_ordered()[l];
			wornName=codes.name(wornCode);
			if(paragraphView)
				header=" ^!";
			else
			{
				header="^N(^H"+wornName+"^?)";
				header+=CMStrings.SPACES.substring(0,headWrap-header.length())+": ^!";
			}
			List<Item> wornHere=mob.fetchWornItems(wornCode,(short)(Short.MIN_VALUE+1),(short)0);
			int shownThisLoc=0;
			int numLocations=mob.getWearPositions(wornCode);
			if(numLocations==0) numLocations=1;
			int emptySlots=numLocations;
			if(wornHere.size()>0)
			{
				List<List<Item>> sets=new Vector<List<Item>>(numLocations);
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
					I=(Item)wornHere.get(i);
					if(I.container()!=null) continue;
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
							I2=(Item)set.get(s2);
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
				for(int i=0;i<wornHere.size();i++)
				{
					thisItem=(Item)wornHere.get(i);
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
								if(!allPlaces) name=CMStrings.ellipse(name,wrap);
								if(wornCode==Wearable.WORN_HELD)
								{
									if(msg.length()==0) msg.append("nothing.");
									if(mob==seer)
										msg.append("\n\rHolding ^<EItem^>"+name+"^</EItem^>"+CMLib.flags().colorCodes(thisItem,seer).toString().trim()+"^N");
									else
										msg.append("\n\r" + mob.charStats().HeShe() + " is holding " +
												 name.trim() + CMLib.flags().colorCodes(thisItem, seer).toString().trim() + "^N.");
								}
								else
								if(wornCode==Wearable.WORN_WIELD)
								{
									if(msg.length()==0) msg.append("nothing.");
									if(mob==seer)
										msg.append("\n\rWielding ^<EItem^>"+name+"^</EItem^>"+CMLib.flags().colorCodes(thisItem,seer).toString().trim()+"^N.");
									else
										msg.append("\n\r" + mob.charStats().HeShe() + " is wielding " +
												 name.trim() + CMLib.flags().colorCodes(thisItem, seer).toString().trim() + "^N.");
								}
								else
								{
									if(mob==seer)
										msg.append(header+"^<EItem^>"+name+"^</EItem^>"+CMLib.flags().colorCodes(thisItem,seer).toString().trim()+"^N,");
									else
										msg.append(header+name.trim()+CMLib.flags().colorCodes(thisItem,seer).toString().trim()+"^N,");
								}
							}
							else
							{
								String name=thisItem.name();
								if((name.length()>shortWrap)&&(!allPlaces)) name=name.substring(0,shortWrap)+"...";
								if(mob==seer)
									msg.append(header+"^<EItem^>"+name+"^</EItem^>"+CMLib.flags().colorCodes(thisItem,seer).toString().trim()+"^?\n\r");
								else
									msg.append(header+name.trim()+CMLib.flags().colorCodes(thisItem,seer).toString().trim()+"^?\n\r");
							}
							shownThisLoc++;
						}
						else
						if(seer==mob)
						{
							msg.append(header+"(something you can`t see)"+CMLib.flags().colorCodes(thisItem,seer).toString().trim()+"^?\n\r");
							shownThisLoc++;
						}
					}
				}
			}
			if(emptySlots>0)
			{
				double numTattoosTotal=0;
				wornName=wornName.toUpperCase();
				for(Enumeration<MOB.Tattoo> e=mob.tattoos();e.hasMoreElements();)
				{
					MOB.Tattoo T = e.nextElement();
					if(T.tattooName.startsWith(wornName+":"))
						numTattoosTotal+=1.0;
				}
				int numTattoosToShow=(int)Math.round(Math.ceil(CMath.mul(numTattoosTotal,CMath.div(emptySlots,numLocations))));
				for(Enumeration<MOB.Tattoo> e=mob.tattoos();e.hasMoreElements();)
				{
					MOB.Tattoo T = e.nextElement();
					if((T.tattooName.startsWith(wornName+":"))
					&&((--numTattoosToShow)>=0))
					{
						tat=T.tattooName;
						if(paragraphView)
						{
							tat=tat.substring(wornName.length()+1).toLowerCase();
							if(tat.length()>wrap) tat=tat.substring(0,wrap)+"...";
							msg.append(header+tat+"^?,");
						}
						else
						{
							tat=CMStrings.capitalizeAndLower(tat.substring(wornName.length()+1).toLowerCase());
							if(tat.length()>shortWrap) tat=tat.substring(0,shortWrap)+"...";
							msg.append(header+tat+"^?\n\r");
						}
						shownThisLoc++;
					}
				}
			}
			if((allPlaces)&&(shownThisLoc==0))
			{
				if(((!paragraphView)&&(wornCode!=Wearable.WORN_FLOATING_NEARBY))
				||((paragraphView)&&(wornCode!=Wearable.WORN_WIELD)))
					for(int i=mob.getWearPositions(wornCode)-1;i>=0;i--)
						msg.append(header+"^?\n\r");
			}
		}
		if(msg.length()==0)
		{
			msg.append("^!(nothing)^?\n\r");
		}
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

	private void viewEquipment(MOB mob, boolean longView)
	{
		Session session=mob.session();
		if(session!=null)
		{
			boolean paragraphView=(CMProps.getIntVar(CMProps.SYSTEMI_EQVIEW)==2);
			if(paragraphView)
			{
				if(longView)
					session.wraplessPrintln("You are wearing "+getEquipment(mob,mob,true));
				else
					session.wraplessPrintln("You are wearing "+getEquipment(mob,mob,false));
			}
			else
			if(longView)
				session.wraplessPrintln("You are wearing:\n\r"+getEquipment(mob,mob,true));
			else
				session.wraplessPrintln("You are wearing:\n\r"+getEquipment(mob,mob,false));
		}
	}
	
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if((commands.size()==1)&&(commands.firstElement() instanceof MOB))
		{
			commands.addElement(getEquipment((MOB)commands.firstElement(),mob,false));
			return true;
		}
		viewEquipment(mob,(commands.size()>1)&&(CMParms.combine(commands,1).equalsIgnoreCase("long")));
		return false;
	}
	
	public boolean canBeOrdered(){return true;}

	public Object executeInternal(MOB mob, int metaFlags, Object... args) throws java.io.IOException
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
