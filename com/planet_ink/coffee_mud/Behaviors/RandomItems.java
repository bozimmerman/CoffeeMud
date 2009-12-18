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
public class RandomItems extends ActiveTicker
{
	public String ID(){return "RandomItems";}
	protected int canImproveCode(){return Behavior.CAN_ROOMS|Behavior.CAN_AREAS|Behavior.CAN_ITEMS|Behavior.CAN_MOBS;}

	protected Vector maintained=new Vector();
	protected int minItems=1;
	protected int maxItems=1;
	protected int avgItems=1;
	protected boolean favorMobs=false;
	protected Vector restrictedLocales=null;
	protected boolean alreadyTriedLoad=false;

	public Vector externalFiles()
	{
        Vector xmlfiles=new Vector();
        String theseparms=getParms();
		int x=theseparms.indexOf(";");
		String filename=(x>=0)?theseparms.substring(x+1):theseparms;
		if(filename.trim().length()==0)
		    return null;
		int start=filename.indexOf("<ITEMS>");
		if((start<0)||(start>20))
		{
			int extraSemicolon=filename.indexOf(";");
			if(extraSemicolon>=0) filename=filename.substring(0,extraSemicolon);
			if(filename.trim().length()>0)
			    xmlfiles.addElement(filename.trim());
		    return xmlfiles;
	    }
		return null;
	}


	public void setParms(String newParms)
	{
		favorMobs=false;
        maintained=new Vector();
		int x=newParms.indexOf(";");
		String oldParms=newParms;
		restrictedLocales=null;
		if(x>=0)
		{
			oldParms=newParms.substring(0,x).trim();
			String extraParms=oldParms;
			int extraX=newParms.indexOf("<ITEMS>");
			if(extraX<0)
			{
				String xtra=newParms.substring(x+1);
				extraX=xtra.indexOf(";");
				if(extraX>=0) extraParms=xtra.substring(extraX+1);
			}
			Vector V=CMParms.parse(extraParms);
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
							restrictedLocales.addElement(Integer.valueOf(Room.INDOORS+i));
						for(int i=0;i<Room.outdoorDomainDescs.length;i++)
							restrictedLocales.addElement(Integer.valueOf(i));
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
							if((c=='+')&&(restrictedLocales.contains(Integer.valueOf(code))))
								restrictedLocales.removeElement(Integer.valueOf(code));
							else
							if((c=='-')&&(!restrictedLocales.contains(Integer.valueOf(code))))
								restrictedLocales.addElement(Integer.valueOf(code));
						}
						code=-1;
						for(int i=0;i<Room.outdoorDomainDescs.length;i++)
							if(Room.outdoorDomainDescs[i].startsWith(s))
								code=i;
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
		if(minItems>maxItems) maxItems=minItems;
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
		if(newRoom==null) return false;
		if(restrictedLocales==null) return true;
		return !restrictedLocales.contains(Integer.valueOf(newRoom.domainType()));
	}

	public boolean isStillMaintained(Environmental thang, ShopKeeper SK, Item I)
	{
		if((I==null)||(I.amDestroyed())) return false;
		if(SK!=null) return SK.getShop().doIHaveThisInStock(I.Name(),null);
		if(thang instanceof Area)
		{
			Room R=CMLib.map().roomLocation(I);
			if(R==null) return false;
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

	public Vector getItems(Tickable thang, String theseparms)
	{
		Vector items=null;
		int x=theseparms.indexOf(";");
		String filename=(x>=0)?theseparms.substring(x+1):theseparms;
		if(filename.trim().length()==0)
		{
			Log.errOut("RandomItems","Blank XML/filename: '"+filename+"'.");
			return null;
		}
		int start=filename.indexOf("<ITEMS>");
		if((start>=0)&&(start<=20))
		{
			int end=start+20;
			if(end>filename.length()) end=filename.length();
			items=(Vector)Resources.getResource("RANDOMITEMS-XML/"+filename.length()+"/"+filename.hashCode());
			if(items!=null) return items;
			items=new Vector();
			String error=CMLib.coffeeMaker().addItemsFromXML(filename,items,null);
			String thangName="null";
			if(thang instanceof Room)
			    thangName=CMLib.map().getExtendedRoomID((Room)thang);
			else
			if((thang instanceof MOB)&&(((MOB)thang).getStartRoom())!=null)
			    thangName=CMLib.map().getExtendedRoomID(((MOB)thang).getStartRoom());
			else
			if(thang!=null)
			    thangName=thang.name();
			if(error.length()>0)
			{
				Log.errOut("RandomItems","Error on import of xml for '"+thangName+"': "+error+".");
				return null;
			}
			if(items.size()<=0)
			{
				Log.errOut("RandomItems","No items loaded for '"+thangName+"'.");
				return null;
			}
			Resources.submitResource("RANDOMITEMS-XML/"+filename.length()+"/"+filename.hashCode(),items);
		}
		else
		{
			int extraSemicolon=filename.indexOf(";");
			if(extraSemicolon>=0) filename=filename.substring(0,extraSemicolon);
			filename=filename.trim();
			items=(Vector)Resources.getResource("RANDOMITEMS-"+filename);
			if((items==null)&&(!alreadyTriedLoad))
			{
				alreadyTriedLoad=true;
				StringBuffer buf=Resources.getFileResource(filename,true);
				String thangName="null";
				if(thang instanceof Room)
				    thangName=CMLib.map().getExtendedRoomID((Room)thang);
				else
				if((thang instanceof MOB)&&(((MOB)thang).getStartRoom())!=null)
				    thangName=CMLib.map().getExtendedRoomID(((MOB)thang).getStartRoom());
				else
				if(thang!=null)
				    thangName=thang.name();

				if((buf==null)||(buf.length()<20))
				{
					Log.errOut("RandomItems","Unknown XML file: '"+filename+"' for '"+thangName+"'.");
					return null;
				}
				if(buf.substring(0,20).indexOf("<ITEMS>")<0)
				{
					Log.errOut("RandomItems","Invalid XML file: '"+filename+"' for '"+thangName+"'.");
					return null;
				}
				items=new Vector();
				String error=CMLib.coffeeMaker().addItemsFromXML(buf.toString(),items,null);
				if(error.length()>0)
				{
					Log.errOut("RandomItems","Error on import of: '"+filename+"' for '"+thangName+"': "+error+".");
					return null;
				}
				if(items.size()<=0)
				{
					Log.errOut("RandomItems","No items loaded: '"+filename+"' for '"+thangName+"'.");
					return null;
				}

				Resources.submitResource("RANDOMITEMS-"+filename,items);
			}
		}
		return items;
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
			int attempts=10;
			if((ticking instanceof Environmental)&&(((Environmental)ticking).amDestroyed()))
				return false;
			while((maintained.size()<avgItems)&&(((--attempts)>0)))
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
				    		((MOB)((Container)ticking).owner()).addInventory(I);
				    	else
				    		break;
						maintained.addElement(I);
				    	I.setContainer((Container)ticking);
				    }
					else
				    if(ticking instanceof MOB)
				    {
			    		((MOB)ticking).addInventory(I);
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
							Vector inhabs=new Vector();
							for(int m=0;m<room.numInhabitants();m++)
							{
								MOB M=room.fetchInhabitant(m);
								if((M.savable())&&(M.getStartRoom().getArea().inMyMetroArea(room.getArea())))
									inhabs.addElement(M);
							}
							if(inhabs.size()>0)
							{
								MOB M=(MOB)inhabs.elementAt(CMLib.dice().roll(1,inhabs.size(),-1));
								M.addInventory(I);
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
