package com.planet_ink.coffee_mud.Abilities.Thief;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary.TrackingFlags;
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
public class Thief_PetSpy extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_PetSpy";
	}

	private final static String	localizedName	= CMLib.lang().L("Pet Spy");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_ANIMALAFFINITY;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	public boolean isAutoInvoked()
	{
		return true;
	}

	@Override
	public boolean canBeUninvoked()
	{
		return affected != invoker;
	}

	protected List<Integer>	path	= new ArrayList<Integer>(1);

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		
		if((tickID==Tickable.TICKID_MOB)
		&&(affected instanceof MOB)
		&&(affected != invoker))
		{
			final MOB M=(MOB)affected;
			final MOB mob=invoker();
			final Room R=CMLib.map().roomLocation(M);
			if((R==null)||(this.path==null)||(this.path.size()==0))
			{
				unInvoke();
				return false;
			}
			int direction = path.remove(0).intValue();
			final Room nextR=R.getRoomInDir(direction);
			if(nextR==null)
			{
				unInvoke();
				return false;
			}
			CMLib.tracking().walk(M, direction, false, true, false, true);
			if(M.location()==nextR)
			{
				final CMMsg msg2=CMClass.getMsg(mob,nextR,CMMsg.MSG_LOOK,null);
				nextR.executeMsg(mob,msg2);
			}
			else
				unInvoke();
		}
		return true;
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			Physical affected=this.affected;
			if(affected instanceof MOB)
			{
				if(path != null)
					path.clear();
				final MOB invoker=this.invoker;
				final MOB M=(MOB)affected;
				if(invoker!=null)
				{
					final Room R=CMLib.map().roomLocation(invoker);
					if(R!=null)
						CMLib.tracking().wanderIn(M, R);
				}
			}
		}
		super.unInvoke();
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(msg.amISource(invoker)
		&&(invoker == affected)
		&&(msg.sourceMinor()==CMMsg.TYP_SPEAK)
		&&(msg.sourceMessage()!=null)
		&&(msg.target() instanceof MOB)
		&&(((MOB)msg.target()).amFollowing()==msg.source())
		&&(((MOB)msg.target()).fetchEffect("Prop_Familiar")!=null)
		&&((msg.sourceMajor()&CMMsg.MASK_MAGIC)==0))
		{
			final MOB mob=msg.source();
			final List<String> commands = CMParms.parseSpaces(CMStrings.getSayFromMessage(msg.sourceMessage()),true);
			if(commands.size()==0)
				return;
			if(!commands.get(0).equalsIgnoreCase("SPY"))
				return;
			commands.remove(0);
			final Room R=mob.location();
			if(R==null)
				return;
			MOB target=(MOB)msg.target();
			if(commands.size()<1)
			{
				R.show(target,mob,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> look(s) at <T-NAME> expectantly."));
				return;
			}
			if(target.fetchEffect(ID())!=null)
			{
				mob.tell(L("@x1 is already on a spy mission!",target.name(mob)));
				return;
			}
			
			if(!super.proficiencyCheck(mob, 0, false))
			{
				R.show(target,mob,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> refuse(s)."));
				return;
			}
			
			int dirs=0;
			for(String s : commands)
			{
				if(CMLib.directions().getProbableDirectionCode(s)>=0)
					dirs++;
			}
			double pct=CMath.div(dirs, commands.size());
			final List<Integer> directions=new ArrayList<Integer>();
			int range=30 + (10 * super.getXLEVELLevel(mob));
			if(pct < .75)
			{
				final String roomName=CMParms.combine(commands,0).trim();
				final TrackingFlags flags=CMLib.tracking().newFlags();
				final List<Room> rooms=CMLib.tracking().getRadiantRooms(R, flags, range);
				Room R2=(Room)CMLib.english().fetchEnvironmental(rooms,roomName,true);
				if(R2 == null)
					R2=(Room)CMLib.english().fetchEnvironmental(rooms,roomName,false);
				if(R2 == null)
				{
					R.show(target,mob,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> look(s) at <T-NAME> confusedly."));
					return;
				}
				List<Room> trail=CMLib.tracking().findTrailToRoom(R, R2, flags, range, rooms);
				if((trail.size()==0)||(trail.get(trail.size()-1)==R))
				{
					R.show(target,mob,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> look(s) at <T-NAME> sadly."));
					return;
				}
				Room R3=R;
				for(int r=trail.size()-2;r>=0;r--)
				{
					final int dir=CMLib.map().getRoomDir(R3, trail.get(r));
					if(dir < 0)
					{
						R.show(target,mob,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> look(s) at <T-NAME> and frowns."));
						return;
					}
					directions.add(Integer.valueOf(dir));
				}
			}
			else
			{
				for(String s : commands)
				{
					dirs = CMLib.directions().getProbableDirectionCode(s);
					if(dirs < 0)
					{
						R.show(target,mob,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> look(s) at <T-NAME> confusedly."));
						return;
					}
					directions.add(Integer.valueOf(dirs));
					if(directions.size()>= ( range / 3) )
						break;
				}
			}
			Thief_PetSpy affect = (Thief_PetSpy)beneficialAffect(mob,target,0,0);
			if(affect != null)
			{
				affect.path=directions;
			}
		}
	}
}
