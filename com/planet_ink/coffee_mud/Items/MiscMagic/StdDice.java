package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.Items.Basic.StdFood;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
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

public class StdDice extends StdItem implements MiscMagic
{
	@Override
	public String ID()
	{
		return "StdDice";
	}

	public StdDice()
	{
		super();

		setName("a six sided die");
		basePhyStats.setWeight(1);
		basePhyStats().setAbility(6);
		setDisplayText("A six sided die lies here.");
		setDescription("It looks like a cube.");
		secretIdentity="Try THROWing it, or ROLLing it!";
		baseGoldValue=5;
		recoverPhyStats();
		material=RawMaterial.RESOURCE_BONE;
	}

	protected boolean rollTheBones(final CMMsg msg, final List<String> commands)
	{
		final MOB mob=msg.source();
		String word = commands.remove(0);
		final Room R=mob.location();
		if(R==null)
			return false;

		final Vector<Item> V=new Vector<Item>();
		final int maxToDrop=CMLib.english().calculateMaxToGive(mob,commands,true,mob,false);
		if(maxToDrop<0)
			return false;

		String whatToDrop=CMParms.combine(commands,0);
		boolean allFlag=(commands.size()>0)?commands.get(0).equalsIgnoreCase("all"):false;
		if(whatToDrop.toUpperCase().startsWith("ALL."))
		{
			allFlag=true;
			whatToDrop="ALL "+whatToDrop.substring(4);
		}
		if(whatToDrop.toUpperCase().endsWith(".ALL"))
		{
			allFlag=true;
			whatToDrop="ALL "+whatToDrop.substring(0,whatToDrop.length()-4);
		}
		int addendum=1;
		String addendumStr="";
		Item dropThis=null;
		boolean doBugFix = true;
		if(V.size()==0)
		{
			while(doBugFix || ((allFlag)&&(addendum<=maxToDrop)))
			{
				doBugFix=false;
				dropThis=mob.fetchItem(null,Wearable.FILTER_UNWORNONLY,whatToDrop+addendumStr);
				if((dropThis==null)
				&&(V.size()==0)
				&&(addendumStr.length()==0)
				&&(!allFlag))
				{
					dropThis=mob.fetchItem(null,Wearable.FILTER_WORNONLY,whatToDrop);
					if(dropThis!=null)
					{
						if((!dropThis.amWearingAt(Wearable.WORN_HELD))&&(!dropThis.amWearingAt(Wearable.WORN_WIELD)))
						{
							mob.tell(L("You must remove that first."));
							return false;
						}
						final CMMsg newMsg=CMClass.getMsg(mob,dropThis,null,CMMsg.MSG_REMOVE,null);
						if(R.okMessage(mob,newMsg))
							R.send(mob,newMsg);
						else
							return false;
					}
				}
				if(dropThis==null)
					break;
				if((CMLib.flags().canBeSeenBy(dropThis,mob)||(dropThis instanceof Light))
				&&(!V.contains(dropThis))
				&&(dropThis instanceof StdDice))
					V.add(dropThis);
				addendumStr="."+(++addendum);
			}
		}

		if(V.size()==0)
		{
			if(word.toUpperCase().startsWith("T"))
				return true;
			else
				mob.tell(L("Roll what now?"));
			return false;
		}
		else
		for(int i=0;i<V.size();i++)
		{
			final Item I=V.get(i);
			if(!I.amDestroyed())
			{
				final CMMsg msg2=CMClass.getMsg(mob,I,null,CMMsg.MSG_DROP,L("<S-NAME> roll(s) <T-NAME>."));
				if(R.okMessage(mob, msg2))
					R.send(mob, msg2);
			}
		}
		return false;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(msg.target()==this)
		{
			if(msg.sourceMinor()==CMMsg.TYP_GET)
				this.setDisplayText(L("@x1 is lying here.",name()));
			else
			if(msg.sourceMinor()==CMMsg.TYP_DROP)
			{
				if(phyStats.ability()<2)
				{
					basePhyStats.setAbility(2);
					phyStats.setAbility(2);
				}
				int roll=CMLib.dice().roll(1, phyStats().ability(), 0);
				this.setDisplayText(L("@x1 is lying here, showing @x2.",name(),""+roll));
			}
		}
		super.executeMsg(myHost, msg);
	}
	
	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if(((msg.sourceMinor()==CMMsg.TYP_HUH)
			&&(msg.targetMessage()!=null)
			&&(msg.targetMessage().length()>0))
		||((msg.tool() instanceof Social)
			&&(((Social)msg.tool()).baseName().toUpperCase().equals("ROLL"))))
		{
			if(msg.tool() instanceof Social)
			{
				final Session sess=msg.source().session();
				if(msg.source().session()!=null)
				{
					List<String> prev = sess.getPreviousCMD();
					if((prev!=null)&&(prev.size()>0)&&("ROLL".startsWith(prev.get(0).toUpperCase())))
						return rollTheBones(msg, new XVector<String>(prev));
				}
				return rollTheBones(msg, new XVector<String>(new String[]{"ROLL","$"+Name()+"$"}));
			}
			if(Character.toUpperCase(msg.targetMessage().charAt(0)) == 'R')
			{
				List<String> parsedFail = CMParms.parse(msg.targetMessage());
				if(parsedFail.size()<1)
					return true;
				String cmd=parsedFail.get(0).toUpperCase();
				if(!("ROLL".startsWith(cmd)))
					return true;
				if(parsedFail.size()<2)
				{
					msg.source().tell(L("Roll what?"));
					return false;
				}
				return rollTheBones(msg, parsedFail);
			}
		}
		else
		if((msg.sourceMinor()==CMMsg.TYP_COMMANDFAIL)
		&&(msg.targetMessage()!=null)
		&&(msg.targetMessage().length()>0))
		{
			if(Character.toUpperCase(msg.targetMessage().charAt(0)) == 'T')
			{
				List<String> parsedFail = CMParms.parse(msg.targetMessage());
				if(parsedFail.size()<2)
					return true;
				String cmd=parsedFail.get(0).toUpperCase();
				if(!("THROW".startsWith(cmd)))
					return true;
				return rollTheBones(msg, parsedFail);
			}
		}
		return true;
	}
}
