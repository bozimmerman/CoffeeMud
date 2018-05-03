package com.planet_ink.coffee_mud.Abilities.Common;
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
   Copyright 2002-2018 Bo Zimmerman

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

public class Mining extends GatheringSkill
{
	@Override
	public String ID()
	{
		return "Mining";
	}

	private final static String	localizedName	= CMLib.lang().L("Mining");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "MINE", "MINING" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL | Ability.DOMAIN_GATHERINGSKILL;
	}

	@Override
	protected boolean allowedWhileMounted()
	{
		return false;
	}

	@Override
	public String supportedResourceString()
	{
		return "GLASS|PRECIOUS|SAND|ROCK|METAL|MITHRIL";
	}

	protected Item		found			= null;
	protected String	foundShortName	= "";

	public Mining()
	{
		super();
		displayText=L("You are mining...");
		verb=L("mining");
	}

	protected int getDuration(MOB mob, int level)
	{
		return getDuration(50,mob,level,15);
	}

	@Override
	protected int baseYield()
	{
		return 1;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			final MOB mob=(MOB)affected;
			if(tickUp==6)
			{
				if(found!=null)
				{
					commonTell(mob,L("You have found a vein of @x1!",foundShortName));
					displayText=L("You are mining @x1",foundShortName);
					verb=L("mining @x1",foundShortName);
				}
				else
				{
					final StringBuffer str=new StringBuffer(L("You can't seem to find anything worth mining here.\n\r"));
					commonTell(mob,str.toString());
					unInvoke();
				}

			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if(affected instanceof MOB)
			{
				final MOB mob=(MOB)affected;
				if((found!=null)&&(!aborted))
				{
					int amount;
					if(RawMaterial.CODES.VALUE(found.material())>=50)
						amount=CMLib.dice().roll(1,4,0);
					else
						amount=CMLib.dice().roll(1,6,0);
					if(((found.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_ROCK)
					&&(found.material()!=RawMaterial.RESOURCE_COAL))
						amount=CMLib.dice().roll(1,25,0);
					amount=amount*(baseYield()+abilityCode());
					final CMMsg msg=CMClass.getMsg(mob,found,this,getCompletedActivityMessageType(),null);
					msg.setValue(amount);
					if(mob.location().okMessage(mob, msg))
					{
						String s="s";
						if(msg.value()==1)
							s="";
						msg.modify(L("<S-NAME> manage(s) to mine @x1 pound@x2 of @x3.",""+msg.value(),s,foundShortName));
						mob.location().send(mob, msg);
						for(int i=0;i<msg.value();i++)
						{
							final Item newFound=(Item)found.copyOf();
							if(!dropAWinner(mob,newFound))
								break;
						}
					}
				}
			}
		}
		super.unInvoke();
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		bundling=false;
		if((!auto)
		&&(commands.size()>0)
		&&((commands.get(0)).equalsIgnoreCase("bundle")))
		{
			bundling=true;
			if(super.invoke(mob,commands,givenTarget,auto,asLevel))
				return super.bundle(mob,commands);
			return false;
		}
		verb=L("mining");
		playSound="dig.wav";
		found=null;
		if((!confirmPossibleMaterialLocation(RawMaterial.MATERIAL_PRECIOUS,mob.location()))
		&&(!confirmPossibleMaterialLocation(RawMaterial.MATERIAL_GLASS,mob.location()))
		&&(!confirmPossibleMaterialLocation(RawMaterial.RESOURCE_SAND,mob.location()))
		&&(!confirmPossibleMaterialLocation(RawMaterial.MATERIAL_ROCK,mob.location()))
		&&(!confirmPossibleMaterialLocation(RawMaterial.MATERIAL_METAL,mob.location()))
		&&(!confirmPossibleMaterialLocation(RawMaterial.MATERIAL_MITHRIL,mob.location())))
		{
			commonTell(mob,L("You don't think this is a good place to mine."));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		final int resourceType=mob.location().myResource();
		if((proficiencyCheck(mob,0,auto))
		   &&((resourceType&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_PRECIOUS)
		   &&((resourceType&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_GLASS)
		   &&(resourceType!=RawMaterial.RESOURCE_SAND)
		   &&(((resourceType&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_ROCK)
		   ||((resourceType&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_METAL)
		   ||((resourceType&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_MITHRIL)))
		{
			found=(Item)CMLib.materials().makeResource(resourceType,Integer.toString(mob.location().domainType()),false,null);
			foundShortName="nothing";
			if(found!=null)
				foundShortName=RawMaterial.CODES.NAME(found.material()).toLowerCase();
		}
		final int duration=getDuration(mob,1);
		final CMMsg msg=CMClass.getMsg(mob,found,this,getActivityMessageType(),L("<S-NAME> start(s) mining."));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			found=(Item)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
