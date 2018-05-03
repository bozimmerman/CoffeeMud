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

public class Digging extends GatheringSkill
{
	@Override
	public String ID()
	{
		return "Digging";
	}

	private final static String	localizedName	= CMLib.lang().L("Gem Digging");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "GDIG", "GDIGGING", "GEMDIGGING" });

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
		return "GLASS|PRECIOUS|SAND|STONE";
	}

	protected Item		found			= null;
	protected String	foundShortName	= "";

	public Digging()
	{
		super();
		displayText=L("You are digging for gems...");
		verb=L("digging");
	}

	protected int getDuration(MOB mob, int level)
	{
		return getDuration(60,mob,level,15);
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
			if(tickUp==3)
			{
				if(found!=null)
				{
					commonTell(mob,L("You have found some @x1!",foundShortName));
					displayText=L("You are digging out @x1",foundShortName);
					verb=L("digging out @x1",foundShortName);
					if((found.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_PRECIOUS)
						bumpTickDown(tickDown*2);
				}
				else
				{
					final StringBuffer str=new StringBuffer(L("You can't seem to find anything worth digging up here.\n\r"));
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
				if((found!=null)&&(!aborted)&&(mob.location()!=null))
				{
					int amount=1;
					if(CMLib.dice().rollPercentage()>90)
						amount++;
					if((found.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_PRECIOUS)
						amount=CMLib.dice().roll(1,15,0);
					amount=amount*(baseYield()+abilityCode());
					final CMMsg msg=CMClass.getMsg(mob,found,this,getCompletedActivityMessageType(),null);
					msg.setValue(amount);
					if(mob.location().okMessage(mob, msg))
					{
						String s="s";
						if((msg.value()==1)||(foundShortName.endsWith("s")))
							s="";
						msg.modify(L("<S-NAME> manage(s) to dig out @x1 @x2@x3.",""+msg.value(),foundShortName,s));
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

		verb=L("digging");
		playSound="dig.wav";
		found=null;
		if((!confirmPossibleMaterialLocation(RawMaterial.MATERIAL_PRECIOUS,mob.location()))
		&&(!confirmPossibleMaterialLocation(RawMaterial.MATERIAL_GLASS,mob.location()))
		&&(!confirmPossibleMaterialLocation(RawMaterial.RESOURCE_SAND,mob.location()))
		&&(!confirmPossibleMaterialLocation(RawMaterial.RESOURCE_STONE,mob.location())))
		{
			commonTell(mob,L("You don't think this is a good place to dig for gems."));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		final int resourceType=mob.location().myResource();
		if((proficiencyCheck(mob,0,auto))
		   &&(((resourceType&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_PRECIOUS)
		   ||((resourceType&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_GLASS)
		   ||(resourceType==RawMaterial.RESOURCE_SAND)
		   ||(resourceType==RawMaterial.RESOURCE_STONE)))
		{
			found=(Item)CMLib.materials().makeResource(resourceType,Integer.toString(mob.location().domainType()),false,null);
			foundShortName="nothing";
			if(found!=null)
				foundShortName=RawMaterial.CODES.NAME(found.material()).toLowerCase();
		}

		final int duration=getDuration(mob,1);
		final CMMsg msg=CMClass.getMsg(mob,found,this,getActivityMessageType(),L("<S-NAME> start(s) digging for gems."));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			found=(Item)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
