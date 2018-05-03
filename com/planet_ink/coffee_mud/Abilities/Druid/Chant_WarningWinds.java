package com.planet_ink.coffee_mud.Abilities.Druid;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2018 Bo Zimmerman

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

public class Chant_WarningWinds extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_WarningWinds";
	}

	private final static String	localizedName	= CMLib.lang().L("Warning Winds");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Warning Winds)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_WEATHER_MASTERY;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	Room	lastRoom	= null;

	@Override
	public void unInvoke()
	{
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		if(canBeUninvoked())
			lastRoom=null;
		super.unInvoke();
		if(canBeUninvoked())
			mob.tell(L("Your senses are no longer attuned to the winds."));
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((tickID==Tickable.TICKID_MOB)
		&&(affected instanceof MOB)
		&&(((MOB)affected).location()!=null)
		&&(((MOB)affected).location()!=lastRoom)
		&&((((MOB)affected).location().domainType()&Room.INDOORS)==0)
		&&(!CMLib.flags().isUnderWateryRoom(((MOB)affected).location())))
		{
			lastRoom=((MOB)affected).location();
			final Vector<Room> V=new Vector<Room>();
			TrackingLibrary.TrackingFlags flags;
			flags = CMLib.tracking().newFlags()
					.plus(TrackingLibrary.TrackingFlag.OPENONLY);
			CMLib.tracking().getRadiantRooms(lastRoom,V,flags,null,2,null);
			boolean fighting=false;
			boolean enemy=false;
			for(int r=0;r<V.size();r++)
			{
				final Room R=V.elementAt(r);
				fighting=false;
				enemy=false;
				if(R!=lastRoom)
				{
					for(int i=0;i<R.numInhabitants();i++)
					{
						final MOB M=R.fetchInhabitant(i);
						if((M!=null)&&(M!=affected))
						{
							if(M.isInCombat())
							{
								fighting=true;
								break;
							}
							for(final Enumeration<Behavior> e=M.behaviors();e.hasMoreElements();)
							{
								final Behavior B=e.nextElement();
								if((B!=null)&&(B.grantsAggressivenessTo((MOB)affected)))
									enemy=true;
							}
						}
						if(enemy||fighting)
							break;
					}
					if(enemy||fighting)
					{
						int dir=CMLib.tracking().radiatesFromDir(R,V);
						if(dir>=0)
						{
							String far="far ";
							for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
							{
								if(lastRoom.getRoomInDir(d)==R)
									far="";
							}
							dir=Directions.getOpDirectionCode(dir);
							if(fighting)
								((MOB)affected).tell(L("The winds tell of fighting @x1@x2.",far,CMLib.directions().getInDirectionName(dir)));
							else
							if(enemy)
								((MOB)affected).tell(L("The winds tell of enemies @x1@x2.",far,CMLib.directions().getInDirectionName(dir)));
						}
					}
				}
			}
		}
		return true;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,L("<S-NAME> <S-IS-ARE> already attuned to the winds."));
			return false;
		}

		if(((target.location().domainType()&Room.INDOORS)>0)&&(!auto))
		{
			target.tell(L("You must be outdoors for this chant to work."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?L("<T-NAME> gain(s) a sense of the winds!"):L("^S<S-NAME> chant(s) for a sense of the winds!^?"));
			if(mob.location().okMessage(mob,msg))
			{
				lastRoom=null;
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			beneficialVisualFizzle(mob,null,L("<S-NAME> chant(s) into the air, but the magic fizzles."));

		return success;
	}
}
