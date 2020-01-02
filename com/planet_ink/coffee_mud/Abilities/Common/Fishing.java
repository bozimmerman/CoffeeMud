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
   Copyright 2002-2020 Bo Zimmerman

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
public class Fishing extends GatheringSkill
{
	@Override
	public String ID()
	{
		return "Fishing";
	}

	private final static String	localizedName	= CMLib.lang().L("Fishing");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "FISH" });

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
	public String supportedResourceString()
	{
		return "FLESH";
	}

	// common recipe definition indexes
	protected static final int	RCP_RESOURCE	= 0;
	protected static final int	RCP_FREQ		= 1;
	protected static final int	RCP_FINALNAME	= 2;
	protected static final int	RCP_VALUE		= 3;

	protected Item		found			= null;
	protected String	foundShortName	= "";

	public Fishing()
	{
		super();
		displayText=L("You are fishing...");
		verb=L("fishing");
	}

	protected int getDuration(final MOB mob, final int level)
	{
		return getDuration(45,mob,level,15);
	}

	@Override
	protected int baseYield()
	{
		return 1;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			final MOB mob=(MOB)affected;
			if(tickUp==6)
			{
				if(found!=null)
					commonTell(mob,L("You got a tug on the line!"));
				else
				{
					final StringBuffer str=new StringBuffer(L("Nothing is biting around here.\n\r"));
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
				if((found!=null)
				&&(!aborted)
				&&(!helping)
				&&(mob.location()!=null))
				{
					final CMMsg msg=CMClass.getMsg(mob,found,this,getCompletedActivityMessageType(),null);
					final int yield = super.adjustYieldBasedOnRoomSpam(CMLib.dice().roll(1,3,0)*(baseYield()+abilityCode()), mob.location());
					msg.setValue(yield);
					if(mob.location().okMessage(mob, msg))
					{
						String s="s";
						if(msg.value()==1)
							s="";
						msg.modify(L("<S-NAME> manage(s) to catch @x1 pound@x2 of @x3.",""+msg.value(),s,foundShortName));
						mob.location().send(mob, msg);
						for(int i=0;i<msg.value();i++)
						{
							final Item newFound=(Item)found.copyOf();
							if(!dropAWinner(mob,newFound))
								break;
							if((mob.riding()!=null)&&(mob.riding() instanceof Container))
								newFound.setContainer((Container)mob.riding());
							CMLib.commands().postGet(mob,null,newFound,true);
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

		Room fishRoom = mob.location();
		if((fishRoom != null)
		&&(fishRoom.getArea() instanceof BoardableShip)
		&&((fishRoom.resourceChoices() == null)||(fishRoom.resourceChoices().size()==0))
		&&((fishRoom.domainType()&Room.INDOORS)==0))
			fishRoom = CMLib.map().roomLocation(((BoardableShip)fishRoom.getArea()).getShipItem());
		int foundFish=-1;
		boolean maybeFish=false;
		if(fishRoom!=null)
		{
			for(final int fishCode : RawMaterial.CODES.FISHES())
			{
				if(fishRoom.myResource()==fishCode)
				{
					foundFish=fishCode;
					maybeFish=true;
				}
				else
				if((fishRoom.resourceChoices()!=null)
				&&(fishRoom.resourceChoices().contains(Integer.valueOf(fishCode))))
					maybeFish=true;
			}
		}
		if(!maybeFish)
		{
			commonTell(mob,L("The fishing doesn't look too good around here."));
			return false;
		}
		verb=L("fishing");
		found=null;
		playSound="fishreel.wav";
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		if((proficiencyCheck(mob,0,auto))
	   &&(super.checkIfAnyYield(mob.location()))
		&&(foundFish>0)
		&&(fishRoom!=null))
		{
			found=(Item)CMLib.materials().makeResource(foundFish,Integer.toString(fishRoom.domainType()),false,null, "");
			foundShortName="nothing";
			if(found!=null)
			{
				foundShortName=RawMaterial.CODES.NAME(found.material()).toLowerCase();
				final List<List<String>> recipes = loadRecipes("fishing.txt");
				if((recipes != null)
				&&(recipes.size()>0))
				{
					int totalWeights = 0;
					final List<List<String>> subset=new ArrayList<List<String>>();
					for(final List<String> subl : recipes)
					{
						if(subl.size()<4)
							continue;
						if(subl.get(RCP_RESOURCE).toLowerCase().equals(foundShortName))
						{
							subset.add(subl);
							totalWeights += CMath.s_int(subl.get(RCP_FREQ));
						}
					}
					List<String> winl = null;
					if(totalWeights > 0)
					{
						final int winner=CMLib.dice().roll(1, totalWeights, -1);
						int current = 0;
						for(final List<String> subl : subset)
						{
							current += CMath.s_int(subl.get(RCP_FREQ));
							if(winner < current)
							{
								winl = subl;
								break;
							}
						}
						if(winl == null)
							winl=subset.get(subset.size()-1);
					}
					if(winl != null)
					{
						foundShortName=winl.get(RCP_FINALNAME).toLowerCase();
						found.setName(L("a pound of @x1",foundShortName));
						found.setDisplayText(L("a pound of @x1 has been left here.",foundShortName));
						if(found instanceof RawMaterial)
							((RawMaterial)found).setSubType(foundShortName.toUpperCase().trim());
						if(CMath.isInteger(winl.get(RCP_VALUE)))
							found.setBaseValue(CMath.s_int(winl.get(RCP_VALUE)));
					}
				}
			}
		}
		final int duration=getDuration(mob,1);
		final CMMsg msg=CMClass.getMsg(mob,found,this,getActivityMessageType(),L("<S-NAME> start(s) fishing."));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			found=(Item)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
