package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.core.CMClass;
import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.CMParms;
import com.planet_ink.coffee_mud.core.collections.SLinkedList;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.CharClass;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.Races.interfaces.Race;

import java.util.*;

/*
   Copyright 2017-2018 Bo Zimmerman

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
public class Prop_OutfitContainer extends Property
{
	@Override
	public String ID()
	{
		return "Prop_OutfitContainer";
	}

	@Override
	public String name()
	{
		return "Remembers whats put in, and wear the contents when worn";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ITEMS;
	}

	@Override
	public boolean bubbleAffect()
	{
		return true;
	}
	
	protected List<Item> outfitContents = new SLinkedList<Item>();
	protected boolean fixedYet = false;

	@Override
	public void setMiscText(String text)
	{
		super.setMiscText(text);
		fixedYet = false;
		this.outfitContents.clear();
	}
	
	protected void fixContentsFromText()
	{
		final Item affected = (this.affected instanceof Item)? (Item)this.affected : null;
		if((!fixedYet)
		&&(affected != null)
		&&(affected.owner() instanceof MOB))
		{
			this.outfitContents.clear();
			final MOB mob=(MOB)affected.owner();
			if(mob.numItems() > 0)
			{
				fixedYet = true;
				if(super.miscText.length()>1)
				{
					final int xmlStart = super.miscText.indexOf(';');
					if(xmlStart >=0)
					{
						final List<XMLTag> tags=CMLib.xml().parseAllXML(miscText.substring(xmlStart+1));
						final List<String> itemNames = new ArrayList<String>(tags.size());
						for(XMLTag tag : tags)
							itemNames.add(tag.value());
						Collections.sort(itemNames);
						String lastName="";
						int ctr=1;
						for(String name : itemNames)
						{
							if(name.equals(lastName))
							{
								ctr++;
								name = "$"+name+"$."+ctr;
							}
							else
								name = "$"+name+"$";
							Item I=mob.findItem(name);
							if(I!=null)
							{
								if((I.container() == affected)
								||(!I.amWearingAt(Item.IN_INVENTORY))
								||((I.container()!=null)&&(!I.ultimateContainer(null).amWearingAt(Item.IN_INVENTORY))))
									this.outfitContents.add(I);
							}
						}
					}
				}
			}
		}
	}
	
	@Override
	public String text()
	{
		fixContentsFromText();
		StringBuilder str=new StringBuilder(";");
		for(Item I : this.outfitContents)
			str.append("<I>"+CMLib.xml().parseOutAngleBrackets(I.Name())+"</I>");
		return str.toString();
	}
	
	@Override
	public String accountForYourself()
	{
		StringBuilder str=new StringBuilder("");
		return str.toString();
	}
	
	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		final Item affected = (this.affected instanceof Item)? (Item)this.affected : null;
		if((msg.target() == affected) 
		&& (msg.othersMessage()!=null)
		&& (msg.othersMessage().length()>0))
		{
			this.fixContentsFromText();
			final Item item = (Item)msg.tool();
			if (msg.tool() instanceof Item)
			{
				if (msg.targetMinor() == CMMsg.TYP_PUT)
				{
					if(!outfitContents.contains(item))
					{
						outfitContents.add(item);
					}
				}
				else
				if((msg.targetMinor() == CMMsg.TYP_GET)
				&&(item.container()==affected))
				{
					if(outfitContents.contains(item))
					{
						outfitContents.remove(item);
					}
				}
			}
			if((msg.targetMinor() == CMMsg.TYP_WEAR)
			||(msg.targetMinor() == CMMsg.TYP_HOLD)
			||(msg.targetMinor() == CMMsg.TYP_WIELD))
			{
				fixContentsFromText();
				final List<Item> thingsToWear = new ArrayList<Item>(outfitContents.size());
				for(Item I : outfitContents)
				{
					if(I.amWearingAt(Item.IN_INVENTORY))
						thingsToWear.add(I);
				}
				if(thingsToWear.size()>0)
				{
					final MOB mob=msg.source();
					final List<Item> thingsToRemove = new ArrayList<Item>(outfitContents.size());
					List<Item> thingsToReDo = new ArrayList<Item>(1);
					for(Item I : thingsToWear)
					{
						if(I.canWear(msg.source(), 0))
							continue;
						long cantWearAt=I.whereCantWear(mob);
						final Item alreadyWearingI=(cantWearAt==0)?null:mob.fetchFirstWornItem(cantWearAt);
						if((alreadyWearingI != null)
						&&(!thingsToRemove.contains(alreadyWearingI))
						&&(!thingsToWear.contains(alreadyWearingI)))
						{
							thingsToRemove.add(alreadyWearingI);
							continue;
						}
						
						boolean done=false;
						for(Enumeration<Item> i=msg.source().items();i.hasMoreElements();)
						{
							final Item chkI=i.nextElement();
							if(chkI==null)
								continue;
							if((thingsToRemove.contains(chkI))
							||(thingsToWear.contains(chkI))
							||(chkI.amWearingAt(Item.IN_INVENTORY))
							||((chkI.rawProperLocationBitmap() & I.rawProperLocationBitmap())==0))
								continue;
							long oldWornCode = chkI.rawWornCode();
							chkI.setRawWornCode(Item.IN_INVENTORY);
							if(I.canWear(msg.source(), 0))
							{
								chkI.setRawWornCode(oldWornCode);
								thingsToRemove.add(I);
								done=true;
								break;
							}
						}
						if(!done)
							thingsToReDo.add(I);
					}
					//for(Item I : thingsToReDo)
					//{
						//TODO: I have no idea .. but it will be layer related..
					//}
					
					for(Item I : thingsToRemove)
						CMLib.commands().postRemove(msg.source(), I, true);
					for(Item I : thingsToWear)
					{
						if(I.container()==affected)
						{
							CMLib.commands().postGet(msg.source(), affected, I, true);
							I.setContainer(null);
						}
						CMLib.commands().postWear(msg.source(),I, false);
					}
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		final Container affected = (this.affected instanceof Container)? (Container)this.affected : null;
		super.executeMsg(myHost, msg);
		if((msg.targetMinor() == CMMsg.TYP_REMOVE)
		&&(msg.target() instanceof Item)
		&&(outfitContents.contains(msg.target())))
		{
			final Item item=(Item)msg.target();
			final MOB mob=msg.source();
			if(item.container() == null)
			{
				msg.addTrailerRunnable(new Runnable(){
					final Item I=item;
					final MOB M = mob;
					@Override
					public void run()
					{
						CMLib.commands().postPut(M, affected, I, true);
						
					}
				});
			}
		}
	}

}
