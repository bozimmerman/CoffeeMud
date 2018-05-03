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
   Copyright 2016-2018 Bo Zimmerman

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

public class Trawling extends GatheringSkill
{
	@Override
	public String ID()
	{
		return "Trawling";
	}

	private final static String	localizedName	= CMLib.lang().L("Trawling");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "TRAWL" });

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

	protected Item		found			= null;
	protected String	foundShortName	= "";
	protected Room		shipRoom		= null;

	public Trawling()
	{
		super();
		displayText=L("You are trawling for fish...");
		verb=L("trawling");
	}

	protected int getDuration(MOB mob, int level)
	{
		return getDuration(135,mob,level,45);
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
					commonTell(mob,L("You feel your nets filling up!"));
				else
				{
					final StringBuffer str=new StringBuffer(L("Nothing is coming up around here.\n\r"));
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
				if((found!=null)&&(!aborted)&&(!helping)&&(mob.location()!=null))
				{
					final int amount=CMLib.dice().roll(1,30,0)*(baseYield()+abilityCode());
					final CMMsg msg=CMClass.getMsg(mob,found,this,getCompletedActivityMessageType(),null);
					msg.setValue(amount);
					if(mob.location().okMessage(mob, msg))
					{
						String s="s";
						if(msg.value()==1)
							s="";
						msg.modify(L("<S-NAME> manage(s) to catch @x1 pound@x2 of @x3 in <S-HIS-HER> trawling nets.",""+msg.value(),s,foundShortName));
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
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Room R=mob.location();
		if(R==null)
			return false;
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

		int foundFish=-1;
		boolean maybeFish=false;
		Room fishRoom=null;
		//if((mob.riding()!=null)&&(mob.riding().rideBasis()==Rideable.RIDEABLE_WATER))
		//	fishRoom=R;
		//else
		if((R.getArea() instanceof BoardableShip)
		&&((R.domainType()&Room.INDOORS)==0))
			fishRoom=CMLib.map().roomLocation(((BoardableShip)R.getArea()).getShipItem());
		
		if(fishRoom==null)
		{
			this.commonTell(mob, L("You need to be on the deck of a ship to trawl."));
			return false;
		}
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
		if(!maybeFish)
		{
			commonTell(mob,L("The fishing doesn't look too good around here."));
			return false;
		}
		verb=L("trawling");
		found=null;
		playSound="fishreel.wav";
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		if((proficiencyCheck(mob,0,auto))
		&&(foundFish>0))
		{
			found=(Item)CMLib.materials().makeResource(foundFish,Integer.toString(fishRoom.domainType()),false,null);
			foundShortName="nothing";
			if(found!=null)
				foundShortName=RawMaterial.CODES.NAME(found.material()).toLowerCase();
		}
		final int duration=getDuration(mob,1);
		final CMMsg msg=CMClass.getMsg(mob,found,this,getActivityMessageType(),L("<S-NAME> start(s) trawling for fish."));
		if(R.okMessage(mob,msg))
		{
			R.send(mob,msg);
			found=(Item)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
