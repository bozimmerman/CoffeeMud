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
   Copyright 2016-2016 Bo Zimmerman

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

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Pet Spy)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_ANIMALAFFINITY;
	}

	protected List<Integer>	path	= new ArrayList<Integer>(1);

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((tickID==Tickable.TICKID_MOB)&&(affected instanceof MOB))
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
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		if(path != null)
			path.clear();
		if(canBeUninvoked())
		{
			final MOB M=(MOB)affected;
			final MOB invoker=this.invoker;
			if((M!=null)&&(invoker!=null))
			{
				final Room R=CMLib.map().roomLocation(invoker);
				if(R!=null)
					CMLib.tracking().wanderIn(M, R);
			}
		}
		super.unInvoke();
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Room R=mob.location();
		if(R==null)
			return false;
		MOB target=null;
		for(int f=0;f<mob.numFollowers();f++)
		{
			final MOB M=mob.fetchFollower(f);
			if((M!=null)
			&&(M.isMonster())
			&&(M.location()==R)
			&&(M.fetchEffect("Prop_Familiar")!=null))
			{
				target=M;
				break;
			}
		}
		if(target==null)
		{
			mob.tell(L("You need a familiar to send them on a spy mission."));
			return false;
		}
		if(commands.size()<1)
		{
			mob.tell(L("Send @x1 where or which directions?",target.name(mob)));
			return false;
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
				R.show(target,mob,CMMsg.MSG_OK_VISUAL,L("<S-NAME> look(s) at <T-NAME> confusedly."));
				return false;
			}
			List<Room> trail=CMLib.tracking().findTrailToRoom(R, R2, flags, range, rooms);
			if((trail.size()==0)||(trail.get(trail.size()-1)==R))
			{
				R.show(target,mob,CMMsg.MSG_OK_VISUAL,L("<S-NAME> look(s) at <T-NAME> sadly."));
				return false;
			}
			Room R3=R;
			for(int r=trail.size()-2;r>=0;r--)
			{
				final int dir=CMLib.map().getRoomDir(R3, trail.get(r));
				if(dir < 0)
				{
					R.show(target,mob,CMMsg.MSG_OK_VISUAL,L("<S-NAME> look(s) at <T-NAME> and frowns."));
					return false;
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
					R.show(target,mob,CMMsg.MSG_OK_VISUAL,L("<S-NAME> look(s) at <T-NAME> confusedly."));
					return false;
				}
				directions.add(Integer.valueOf(dirs));
				if(directions.size()>= ( range / 3) )
					break;
			}
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> send(s) <T-NAMESELF> on a spy mission.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Thief_PetSpy affect = (Thief_PetSpy)beneficialAffect(mob,target,asLevel,0);
				if(affect != null)
				{
					affect.path=directions;
				}
			}
		}
		else
			beneficialVisualFizzle(mob,target,L("<S-NAME> attempt(s) to send <T-NAMESELF> on a spy mission, but <T-HE-SHE> doesn't seem to understand."));

		// return whether it worked
		return success;
	}
}
