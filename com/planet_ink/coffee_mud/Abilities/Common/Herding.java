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
   Copyright 2023-2023 Bo Zimmerman

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
public class Herding extends CommonSkill
{
	@Override
	public String ID()
	{
		return "Herding";
	}

	public Herding()
	{
		super();
		displayText=L("You are herding...");
		verb=L("herding");
	}

	private final static String localizedName = CMLib.lang().L("Herding");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"HURD","HURDING"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL|Ability.DOMAIN_ANIMALAFFINITY;
	}

	// common recipe definition indexes
	protected static final int	RCP_RESOURCE= 0;
	protected static final int	RCP_DOMAIN	= 1;
	protected static final int	RCP_FREQ	= 2;
	protected static final int	RCP_MOB		= 3;

	protected MOB		found			= null;
	protected String	foundShortName	= "";

	public Room nearByRoom(final Room R)
	{
		final List<Integer> possibilities=new ArrayList<Integer>(Directions.NUM_DIRECTIONS());
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			if(d!=Directions.UP)
			{
				final Room room=R.getRoomInDir(d);
				final Exit exit=R.getExitInDir(d);
				if((room!=null)
				&&(exit!=null)
				&&(exit.isOpen()))
					possibilities.add(Integer.valueOf(d));
			}
		}
		if(possibilities.size()>0)
		{
			final int dir=possibilities.get(CMLib.dice().roll(1,possibilities.size(),-1)).intValue();
			return R.getRoomInDir(dir);
		}
		return null;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			final MOB mob=(MOB)affected;
			activityRoom=mob.location();
			if(tickUp==0)
			{
				unInvoke();
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public void unInvoke()
	{
		super.unInvoke();
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		verb=L("hurding");
		found=null;
		activityRoom=null;
		final Room R=mob.location();
		if(R==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		
		if(mob.riding() != null)
		{
			
		}

		if(proficiencyCheck(mob,0,auto))
		{

		}
		final int duration=10+mob.phyStats().level()+(super.getXTIMELevel(mob)*2);
		final CMMsg msg=CMClass.getMsg(mob,found,this,getActivityMessageType(),L("<S-NAME> start(s) hurding."));
		if(R.okMessage(mob,msg))
		{
			R.send(mob,msg);
			found=(MOB)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
