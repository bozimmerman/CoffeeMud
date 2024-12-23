package com.planet_ink.coffee_mud.Abilities.Songs;
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
   Copyright 2011-2024 Bo Zimmerman

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
public class Skill_QuickChange extends BardSkill
{
	@Override
	public String ID()
	{
		return "Skill_QuickChange";
	}

	private final static String	localizedName	= CMLib.lang().L("QuickChange");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[]	triggerStrings	= I(new String[] { "QUICKCHANGE" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_FOOLISHNESS;
	}

	static final String	locationsDelim	= "<ITEMLOCATIONS>";
	static final String	containerDelim	= "<ITEMCONTAINERS>";

	private class PackedItem
	{
		public Item I;
		public long wornLoc;
		public Item containerI;
		public PackedItem(final Item I, final Item containerI, final long wornLocation)
		{
			this.I=I;
			this.wornLoc=wornLocation;
			this.containerI=containerI;
		}
	}

	public List<PackedItem> getAllWornItems(final MOB mob)
	{
		final List<PackedItem> items=new LinkedList<PackedItem>();
		for(final Enumeration<Item> i= mob.items(); i.hasMoreElements();)
		{
			final Item I=i.nextElement();
			if((!I.amWearingAt(Wearable.IN_INVENTORY))
			||(!I.ultimateContainer(null).amWearingAt(Wearable.IN_INVENTORY)))
				items.add(new PackedItem(I,I.container(),I.rawWornCode()));
		}
		Log.debugOut("Skill_QuickChange", "getAllWornItems: Found " + items.size() + " worn items");
		return items;
	}

	public List<PackedItem> getAllPackedItems(final Session S)
	{
		Log.debugOut("Skill_QuickChange", "getAllPackedItems: Full miscText content:\n" + super.miscText);

		final List<PackedItem> items=new LinkedList<PackedItem>();
		if(super.miscText.trim().length()>0)
		{
			final int locStart=super.miscText.lastIndexOf(locationsDelim);
			final int contStart=super.miscText.lastIndexOf(containerDelim);
			if((locStart>0)&&(contStart>locStart))
			{
				final List<Item> itemList=new Vector<Item>();
				String xmlContent = super.miscText.substring(0,locStart);
				Log.debugOut("Skill_QuickChange", "getAllPackedItems: XML content:\n" + xmlContent);
				CMLib.coffeeMaker().addItemsFromXML(xmlContent, itemList, S);
				final List<String> itemLocList=CMParms.parseAny(super.miscText.substring(locStart+locationsDelim.length(),contStart), ';', true);
				final List<String> itemConList=CMParms.parseAny(super.miscText.substring(contStart+containerDelim.length()), ';', true);
				Log.debugOut("Skill_QuickChange", "getAllPackedItems: Item locations: " + itemLocList);
				Log.debugOut("Skill_QuickChange", "getAllPackedItems: Item containers: " + itemConList);
				if((itemLocList.size()==itemList.size())&&(itemLocList.size()==itemConList.size()))
				{
					for(int i=0;i<itemList.size();i++)
					{
						final long wornLoc=CMath.s_long(itemLocList.get(i));
						final int containerDex=CMath.s_int(itemConList.get(i));
						Item containerI=null;
						if(containerDex>=0)
							containerI=itemList.get(containerDex);
						items.add(new PackedItem(itemList.get(i),containerI,wornLoc));
					}
				}
				Log.debugOut("Skill_QuickChange", "getAllPackedItems: Parsed " + items.size() + " items from miscText");
			}
			else
			{
				Log.debugOut("Skill_QuickChange", "getAllPackedItems: Invalid miscText format");
			}
		}
		else
		{
			Log.debugOut("Skill_QuickChange", "getAllPackedItems: No items found in miscText");
		}

		return items;
	}

	public void wearThese(final MOB mob, final List<PackedItem> items)
	{
		for(final PackedItem I : items)
		{
			if(I.containerI instanceof Container)
				I.I.setContainer((Container)I.containerI);
			mob.addItem(I.I);
			I.I.wearAt(I.wornLoc);

			Log.debugOut("Skill_QuickChange", "wearThese: Wore item " + I.I.name() + " at location " + I.wornLoc);
		}
		// this is to clear the wear/wield cache
		final CMMsg msg=CMClass.getMsg(mob, null, null, CMMsg.MASK_ALWAYS|CMMsg.MSG_WIELD,null,CMMsg.MSG_OK_VISUAL,null,CMMsg.MSG_OK_VISUAL,null);
		mob.executeMsg(mob, msg);
	}

	public void packThese(final List<PackedItem> items)
	{
		Log.debugOut("Skill_QuickChange", "packThese: Packing " + items.size() + " items");

		final List<Item> itemList=new Vector<Item>();
		for(final PackedItem I : items)
			itemList.add(I.I);
		final StringBuilder str=new StringBuilder("<ITEMS>");
		str.append(CMLib.coffeeMaker().getUniqueItemsXML(itemList, new Hashtable<String,List<Item>>(), new HashSet<String>(), null));
		str.append("</ITEMS>");
		str.append(locationsDelim);
		for(final PackedItem I : items)
			str.append(I.wornLoc).append(";");
		str.append(containerDelim);
		for(final PackedItem I : items)
			str.append(itemList.indexOf(I.containerI)).append(";");
		Log.debugOut("Skill_QuickChange", "packThese: Full XML content:\n" + str.toString());
		super.miscText=str.toString();
		for(final PackedItem I : items)
		{
			I.I.destroy();

			Log.debugOut("Skill_QuickChange", "packThese: Destroyed item " + I.I.name());
		}
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_DELICATE_HANDS_ACT|(auto?CMMsg.MASK_ALWAYS:0),L("<S-NAME> perform(s) a quick costume change."));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final List<PackedItem> myCurrentGear=getAllWornItems(mob);
				final List<PackedItem> mySavedGear=getAllPackedItems(mob.session());
				packThese(myCurrentGear);
				if(mySavedGear.size()==0)
				{
					mob.tell(L("That outfit is now tucked away for a quick change later on."));
					Log.debugOut("Skill_QuickChange", "invoke: Current gear size: " + myCurrentGear.size() + ", Saved gear size: " + mySavedGear.size());
				}
				else
				{
					wearThese(mob, mySavedGear);

					Log.debugOut("Skill_QuickChange", "invoke: Wore saved gear");
				}
			}
		}
		else {
			Log.debugOut("Skill_QuickChange", "invoke: Skill check failed");

			return beneficialVisualFizzle(mob,null,L("<S-NAME> attempt(s) to change clothes, but forget(s) how."));
		}

		return success;
	}

}
