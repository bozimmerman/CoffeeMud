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
   Copyright 2000-2010 Bo Zimmerman

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
public class Equipment extends StdCommand
{
	public Equipment(){}

	private String[] access={"EQUIPMENT","EQ","EQUIP"};
	public String[] getAccessWords(){return access;}

	public static StringBuilder getEquipment(MOB seer, MOB mob, boolean allPlaces)
	{
		StringBuilder msg=new StringBuilder("");
		if(CMLib.flags().isSleeping(seer))
			return new StringBuilder("(nothing you can see right now)");

	    long wornCode=0;
	    String header=null;
	    int found=0;
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
		    found=0;
			wornCode=codes.all_ordered()[l];
			wornName=codes.name(wornCode);
            if(paragraphView)
    			header=" ^!";
            else
            {
                header="^N(^H"+wornName+"^?)";
                header+=CMStrings.SPACES.substring(0,26-header.length())+": ^!";
            }
            Vector wornHere=mob.fetchWornItems(wornCode,(short)(Short.MIN_VALUE+1),(short)0);
            int shownThisLoc=0;
            int numLocations=mob.getWearPositions(wornCode);
            if(numLocations==0) numLocations=1;
            int emptySlots=numLocations;
            if(wornHere.size()>0)
            {
	            Vector sets=new Vector(numLocations);
	            for(int i=0;i<numLocations;i++)
	            	sets.addElement(new Vector());
	            Item I=null;
	            Item I2=null;
	            short layer=Short.MAX_VALUE;
	            short layerAtt=0;
	            short layer2=Short.MAX_VALUE;
	            short layerAtt2=0;
	            Vector set=null;
	            for(int i=0;i<wornHere.size();i++)
	            {
	            	I=(Item)wornHere.elementAt(i);
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
	            		set=(Vector)sets.elementAt(s);
	            		if(set.size()==0)
	            		{ 
	            			set.addElement(I); 
	            			break;
	            		}
	            		for(int s2=0;s2<set.size();s2++)
	            		{
	            			I2=(Item)set.elementAt(s2);
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
		                    		set.insertElementAt(I,s2);
	                			break;
	                    	}
	                    	if(layer2>layer)
	                    	{
	                    		set.insertElementAt(I,s2);
	                    		break;
	                    	}
	            		}
	            		if(set.contains(I)) 
	            			break;
	            		if(layer2<layer)
	            		{ 
	            			set.addElement(I); 
	            			break;
	            		}
	            	}
	            }
	            wornHere.clear();
	            for(int s=0;s<sets.size();s++)
	            {
	            	set=(Vector)sets.elementAt(s);
	            	int s2=set.size()-1;
	            	for(;s2>=0;s2--)
	            	{
	            		I2=(Item)set.elementAt(s2);
	        			wornHere.addElement(I2);
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
					thisItem=(Item)wornHere.elementAt(i);
					if((thisItem.container()==null)&&(thisItem.amWearingAt(wornCode)))
					{
						if(paragraphView)
						{
		                	if(alreadyDone.contains(thisItem))
		                		continue;
		                	alreadyDone.add(thisItem);
						}
						found++;
						if(CMLib.flags().canBeSeenBy(thisItem,seer))
						{
	                        if(paragraphView)
	                        {
	                            String name=thisItem.name();
	                            if(name.length()>75) name=name.substring(0,75)+"...";
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
	                            if(name.length()>53) name=name.substring(0,50)+"...";
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
				for(int i=0;i<mob.numTattoos();i++)
				{
				    tat=mob.fetchTattoo(i).toUpperCase();
				    if((tat.startsWith(wornName+":"))) numTattoosTotal+=1.0;
				}
				int numTattoosToShow=(int)Math.round(Math.ceil(CMath.mul(numTattoosTotal,CMath.div(emptySlots,numLocations))));
				for(int i=0;i<mob.numTattoos();i++)
				{
				    tat=mob.fetchTattoo(i).toUpperCase();
				    if((tat.startsWith(wornName+":"))
				    &&((--numTattoosToShow)>=0))
				    {
                        if(paragraphView)
                        {
                            tat=tat.substring(wornName.length()+1).toLowerCase();
                            if(tat.length()>75) tat=tat.substring(0,75)+"...";
                            msg.append(header+tat+"^?,");
                        }
                        else
                        {
    				        tat=CMStrings.capitalizeAndLower(tat.substring(wornName.length()+1).toLowerCase());
                            if(tat.length()>53) tat=tat.substring(0,50)+"...";
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
            if(mob.isMonster())
                return null;
			msg.append("^!(nothing)^?\n\r");
        }
        else
        if((paragraphView)&&(msg.lastIndexOf(",") > -1))
        {
            msg.insert(msg.lastIndexOf(",") + 1, ".");
            msg.deleteCharAt(msg.lastIndexOf(","));
            if(msg.lastIndexOf(",") > -1)
                msg.insert(msg.lastIndexOf(",") + 1, " and");
        }
        return msg;
	}

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if((commands.size()==1)&&(commands.firstElement() instanceof MOB))
		{
			commands.addElement(getEquipment((MOB)commands.firstElement(),mob,false));
			return true;
		}
		if(!mob.isMonster())
		{
            boolean paragraphView=(CMProps.getIntVar(CMProps.SYSTEMI_EQVIEW)==2);
            if(paragraphView)
            {
    			if((commands.size()>1)&&(CMParms.combine(commands,1).equalsIgnoreCase("long")))
    				mob.session().wraplessPrintln("You are wearing "+getEquipment(mob,mob,true));
    			else
    				mob.session().wraplessPrintln("You are wearing "+getEquipment(mob,mob,false));
            }
            else
            if((commands.size()>1)&&(CMParms.combine(commands,1).equalsIgnoreCase("long")))
                mob.session().wraplessPrintln("You are wearing:\n\r"+getEquipment(mob,mob,true));
            else
                mob.session().wraplessPrintln("You are wearing:\n\r"+getEquipment(mob,mob,false));
		}
		return false;
	}
	
	public boolean canBeOrdered(){return true;}

	
}
