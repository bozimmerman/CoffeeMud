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
   Copyright 2003-2024 Bo Zimmerman

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
public class Chant_FindMate extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_FindMate";
	}

	private final static String	localizedName	= CMLib.lang().L("Find Mate");

	@Override
	public String name()
	{
		return localizedName;
	}

	protected String	displayText	= L("(Tracking a mate)");

	@Override
	public String displayText()
	{
		return displayText;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_BREEDING;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_TRACKING | Ability.FLAG_DIVINING;
	}

	protected List<Room>	theTrail		= null;
	public int				nextDirection	= -2;

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(tickID==Tickable.TICKID_MOB)
		{
			if((theTrail==null)
			||(!(affected instanceof MOB)))
				return false;

			final MOB mob=(MOB)affected;
			final Room R=mob.location();
			if(R!=null)
			{
				MOB mate=null;
				for(int i=0;i<R.numInhabitants();i++)
				{
					final MOB M=R.fetchInhabitant(i);
					if(isSuitableMate(M,mob))
					{
						mate=M;
						break;
					}
				}
				if(mate!=null)
				{
					mob.tell(L("You peer longingly at @x1.",mate.name()));

					Item I=mob.fetchFirstWornItem(Wearable.WORN_WAIST);
					if(I!=null)
						CMLib.commands().postRemove(mob,I,false);
					I=mob.fetchFirstWornItem(Wearable.WORN_LEGS);
					if(I!=null)
						CMLib.commands().postRemove(mob,I,false);

					if((mob.fetchFirstWornItem(Wearable.WORN_WAIST)!=null)
					||(mob.fetchFirstWornItem(Wearable.WORN_LEGS)!=null))
						unInvoke();
					mob.doCommand(CMParms.parse("MATE \""+mate.name()+"$\""),MUDCmdProcessor.METAFLAG_FORCED);
					unInvoke();
				}
			}

			if(nextDirection==-999)
				return true;

			if(nextDirection==999)
			{
				mob.tell(L("Your yearning for a mate seems to fade."));
				nextDirection=-2;
				unInvoke();
			}
			else
			if(nextDirection==-1)
			{
				mob.tell(L("You no longer want to continue."));
				nextDirection=-999;
				unInvoke();
			}
			else
			if(nextDirection>=0)
			{
				mob.tell(L("You want to continue @x1.",CMLib.directions().getDirectionName(nextDirection)));
				if(R!=null)
				{
					final Room nextRoom=R.getRoomInDir(nextDirection);
					if((nextRoom!=null)
					&&((nextRoom.getArea()==mob.location().getArea())||(!mob.isMonster())))
					{
						final int dir=nextDirection;
						nextDirection=-2;
						CMLib.tracking().walk(mob,dir,false,false);
					}
					else
						unInvoke();
				}
				else
					unInvoke();
			}
		}
		return true;
	}

	@Override
	public void affectPhyStats(final Physical affectedEnv, final PhyStats affectableStats)
	{
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_TRACK);
		super.affectPhyStats(affectedEnv, affectableStats);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		if(!(affected instanceof MOB))
			return;

		final MOB mob=(MOB)affected;
		if((msg.amISource(mob))
		&&(msg.amITarget(mob.location()))
		&&(CMLib.flags().canBeSeenBy(mob.location(),mob))
		&&(msg.targetMinor()==CMMsg.TYP_LOOK))
			nextDirection=CMLib.tracking().trackNextDirectionFromHere(theTrail,mob.location(),true);
	}

	public boolean isSuitableMate(final MOB mate, final MOB forMe)
	{
		if(mate==forMe)
			return false;
		if((mate==null)||(forMe==null))
			return false;
		if(mate.charStats().reproductiveCode()==forMe.charStats().reproductiveCode())
			return false;
		if((mate.charStats().reproductiveCode()!='M')
		&&(mate.charStats().reproductiveCode()!='F'))
			return false;
		if(((mate.charStats().getMyRace().ID().equals("Human"))
		   ||(mate.charStats().getMyRace().ID().equals("Human"))
		   ||(mate.charStats().getMyRace().canBreedWith(mate.charStats().getMyRace(),false)))
		&&(mate.fetchWornItems(Wearable.WORN_LEGS|Wearable.WORN_WAIST,(short)-2048,(short)0).size()==0)
		&&(CMLib.flags().canBeSeenBy(mate,forMe)))
			return true;
		return false;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;
		if((target.charStats().reproductiveCode()!='M')
		&&(target.charStats().reproductiveCode()!='F'))
		{
			mob.tell(L("@x1 is incapable of mating!",target.name(mob)));
			return false;
		}

		final List<Ability> V=CMLib.flags().flaggedAffects(mob,Ability.FLAG_TRACKING);
		for(final Ability A : V) A.unInvoke();
		if(V.size()>0)
		{
			target.tell(L("You stop tracking."));
			return true;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		TrackingLibrary.TrackingFlags flags;
		flags = CMLib.tracking().newFlags()
				.plus(TrackingLibrary.TrackingFlag.OPENONLY);
		final ArrayList<Room> rooms=new ArrayList<Room>();
		final int radius = 50 + (10*super.getXMAXRANGELevel(mob)) + super.getXLEVELLevel(mob);
		final List<Room> trashRooms = new ArrayList<Room>();
		if(CMLib.tracking().getRadiantRoomsToTarget(mob.location(), trashRooms, flags, new TrackingLibrary.RFilter() {
			@Override
			public boolean isFilteredOut(final Room hostR, Room R, final Exit E, final int dir)
			{
				R=CMLib.map().getRoom(R);
				for(int i=0;i<R.numInhabitants();i++)
				{
					final MOB M=R.fetchInhabitant(i);
					if(isSuitableMate(M,target))
						return false;
				}
				return true;
			}
		}, radius))
			rooms.add(trashRooms.get(trashRooms.size()-1));
		//TrackingLibrary.TrackingFlags flags;
		flags = CMLib.tracking().newFlags()
				.plus(TrackingLibrary.TrackingFlag.OPENONLY)
				.plus(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS)
				.plus(TrackingLibrary.TrackingFlag.NOAIR)
				.plus(TrackingLibrary.TrackingFlag.NOWATER);
		if(rooms.size()>0)
			theTrail=CMLib.tracking().findTrailToAnyRoom(mob.location(),rooms,flags,radius);

		if((success)&&(theTrail!=null))
		{
			theTrail.add(mob.location());

			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?null:L("^S<S-NAME> chant(s) to <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
				final Chant_FindMate A=(Chant_FindMate)target.fetchEffect(ID());
				if(A!=null)
				{
					target.location().show(target,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> yearn(s) for a mate!"));
					A.makeLongLasting();
					A.nextDirection=CMLib.tracking().trackNextDirectionFromHere(theTrail,mob.location(),true);
					target.recoverPhyStats();
				}
			}
		}
		else
			beneficialWordsFizzle(mob,target,L("<S-NAME> chant(s) to <T-NAMESELF>, but nothing happen(s)."));

		// return whether it worked
		return success;
	}
}
