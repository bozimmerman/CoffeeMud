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
   Copyright 2002-2025 Bo Zimmerman

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
public class Chopping extends GatheringSkill
{
	@Override
	public String ID()
	{
		return "Chopping";
	}

	private final static String	localizedName	= CMLib.lang().L("Wood Chopping");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "CHOP", "CHOPPING" });

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
		return "WOODEN";
	}

	protected Item		found			= null;
	protected String	foundShortName	= "";

	public Chopping()
	{
		super();
		displayText=L("You are looking for a good tree...");
		verb=L("looking");
	}

	protected int getDuration(final MOB mob, final int level)
	{
		return getDuration(40,mob,level,15);
	}

	@Override
	protected int baseYield()
	{
		return 1;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			final MOB mob=(MOB)affected;
			if(tickUp==6)
			{
				if(found!=null)
				{
					commonTelL(mob,"You have a good tree for @x1.",foundShortName);
					displayText=L("You are chopping up @x1",foundShortName);
					verb=L("chopping @x1",foundShortName);
					playSound="chopping.wav";
				}
				else
				{
					final int d=lookingForMat(RawMaterial.MATERIAL_WOODEN,mob.location());
					if(d<0)
						commonTelL(mob,"You can't seem to find any trees worth cutting around here.\n\rYou might try elsewhere.");
					else
						commonTelL(mob,"You might try @x1.",CMLib.directions().getInDirectionName(d));
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
				if((found!=null)&&(!aborted)&&(mob.location()!=null))
				{
					final CMMsg msg=CMClass.getMsg(mob,found,this,getCompletedActivityMessageType(),null);
					final int yield = super.adjustYieldBasedOnRoomSpam(CMLib.dice().roll(1,7,3)*(baseYield()+abilityCode()), mob.location());
					msg.setValue(yield);
					if(mob.location().okMessage(mob, msg))
					{
						if(msg.value()<2)
							msg.modify(L("<S-NAME> manage(s) to chop up @x1.",found.name()));
						else
							msg.modify(L("<S-NAME> manage(s) to chop up @x1 pounds of @x2.",""+msg.value(),foundShortName));
						mob.location().send(mob, msg);
						for(int i=0;i<msg.value();i++)
						{
							final Item newFound=(Item)found.copyOf();
							if(!dropAWinner(mob,newFound))
							{
								break;
							}
						}
					}
				}
			}
		}
		super.unInvoke();
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
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

		verb=L("chopping");
		playSound=null;
		found=null;
		if((!confirmPossibleMaterialLocation(RawMaterial.MATERIAL_WOODEN,mob.location()))
		&&(!CMParms.contains(RawMaterial.CODES.WOODIES(), mob.location().myResource())))
		{
			commonTelL(mob,"You can't find anything to chop here.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		final int resourceType=mob.location().myResource();
		if(proficiencyCheck(mob,0,auto) && (super.checkIfAnyYield(mob.location())))
		{
			if((resourceType&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_WOODEN)
			{
				found=(Item)CMLib.materials().makeResource(resourceType,Integer.toString(mob.location().domainType()),false,null, "");
				foundShortName="nothing";
				if(found!=null)
					foundShortName=RawMaterial.CODES.NAME(found.material()).toLowerCase();
			}
			else
			if(CMParms.contains(RawMaterial.CODES.WOODIES(), resourceType))
			{
				found=(Item)CMLib.materials().makeResource(RawMaterial.RESOURCE_WOOD,Integer.toString(mob.location().domainType()),false,null, "");
				foundShortName="nothing";
				if(found!=null)
					foundShortName=L("@x1 tree wood",CMLib.english().makeSingular(RawMaterial.CODES.NAME(resourceType).toLowerCase()));
			}
		}
		final int duration=getDuration(mob,1);
		final String oldFoundName = (found==null)?"":found.Name();
		final CMMsg msg=CMClass.getMsg(mob,found,this,getActivityMessageType(),L("<S-NAME> start(s) looking for a good tree to chop."));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			found=(Item)msg.target();
			if((found!=null)&&(!found.Name().equals(oldFoundName)))
				foundShortName=CMLib.english().removeArticleLead(found.Name());
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
