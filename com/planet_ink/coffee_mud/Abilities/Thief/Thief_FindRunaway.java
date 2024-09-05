package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.Area.Stats;
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
   Copyright 2024-2024 Bo Zimmerman

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
public class Thief_FindRunaway extends StdAbility
{
	@Override
	public String ID()
	{
		return "Thief_FindRunaway";
	}

	private final static String	localizedName	= CMLib.lang().L("Find Runaway");

	@Override
	public String name()
	{
		return localizedName;
	}

	protected String	displayText	= L("(Runaway Tracking)");

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
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	private static final String[]	triggerStrings	= I(new String[] { "RTRACK", "FINDRUNAWAY", "RUNAWAYTRACK" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public boolean putInCommandlist()
	{
		return false;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_STREETSMARTS;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_TRACKING;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	protected List<Room>	theTrail		= null;
	protected volatile int	nextDirection	= -2;
	protected MOB			runawayM		= null;
	protected long			lastCast		= -1;

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(tickID==Tickable.TICKID_MOB)
		{
			if(nextDirection==-999)
				return true;

			if((theTrail==null)
			||(affected == null)
			||(!(affected instanceof MOB)))
				return false;

			final MOB mob=(MOB)affected;

			if(nextDirection==999)
			{
				mob.tell(L("The clues seem to lead here."));
				nextDirection=-2;
				unInvoke();
			}
			else
			if(nextDirection==-1)
			{
				mob.tell(L("The clues dry up here."));
				nextDirection=-999;
				unInvoke();
			}
			else
			if(nextDirection>=0)
			{
				mob.tell(L("The clues lead you @x1.",CMLib.directions().getInDirectionName(nextDirection)));
				if(mob.isMonster())
				{
					final Room nextRoom=mob.location().getRoomInDir(nextDirection);
					if((nextRoom!=null)&&(nextRoom.getArea()==mob.location().getArea()))
					{
						final int dir=nextDirection;
						nextDirection=-2;
						CMLib.tracking().walk(mob,dir,false,false);
					}
					else
						unInvoke();
				}
				else
					nextDirection=-2;
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

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;

		if(!(affected instanceof MOB))
			return true;

		final MOB mob=(MOB)affected;
		if((msg.amISource(mob))
		&&(msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(theTrail!=null)
		&&(theTrail.size()>0)
		&&(this.runawayM==null)
		&&(msg.amITarget(theTrail.get(0))))
		{
			final Area A = ((Room)msg.target()).getArea();
			String raceName = "Human";
			if(A.isAreaStatsLoaded())
			{
				final Race R = A.getAreaRace();
				if(R != null)
					raceName = R.ID();
			}
			final Race R = CMClass.getRace(raceName);
			final MOB newMOB = CMClass.getMOB("GenMob");
			newMOB.basePhyStats().setLevel(Math.min(1+super.getXLEVELLevel(msg.source()), msg.source().phyStats().level()));
			newMOB.baseCharStats().setMyRace(R);
			final char mobGender = (CMLib.dice().roll(1, 2, 0) ==0) ? 'F' : 'M';
			String name=R.makeMobName(mobGender, R.getAgingChart()[Race.AGE_CHILD])+L(" child");
			name=CMLib.english().startWithAorAn(name).toLowerCase();
			newMOB.setName(name);
			newMOB.setDisplayText(L("@x1 is here.",name));
			newMOB.setDescription("");
			CMLib.factions().setAlignment(newMOB,Faction.Align.NEUTRAL);
			newMOB.recoverPhyStats();
			newMOB.recoverCharStats();
			newMOB.basePhyStats().setAbility(CMProps.getMobHPBase()*2);
			newMOB.basePhyStats().setArmor(CMLib.leveler().getLevelMOBArmor(newMOB));
			newMOB.basePhyStats().setAttackAdjustment((int)CMath.mul(CMLib.leveler().getLevelAttack(newMOB),0.8));
			newMOB.basePhyStats().setDamage((int)CMath.mul(CMLib.leveler().getLevelMOBDamage(newMOB),0.9));
			newMOB.basePhyStats().setSpeed((int)CMath.mul(CMLib.leveler().getLevelMOBSpeed(newMOB),0.9));
			newMOB.addTattoo("SYSTEM_SUMMONED");
			newMOB.baseCharStats().setStat(CharStats.STAT_GENDER, mobGender);
			newMOB.setLocation((Room)msg.target());
			newMOB.basePhyStats().setRejuv(PhyStats.NO_REJUV);
			newMOB.setMiscText(newMOB.text());
			newMOB.recoverCharStats();
			newMOB.recoverPhyStats();
			newMOB.recoverMaxState();
			newMOB.resetToMaxState();
			newMOB.bringToLife((Room)msg.target(),true);
			CMLib.beanCounter().clearZeroMoney(newMOB,null);
			newMOB.setMoneyVariation(0);
			newMOB.setStartRoom(null);
			final Ability effA = CMClass.getAbility("ScriptLater");
			final long mudHrFromNow = System.currentTimeMillis() + CMProps.getMillisPerMudHour();
			final StringBuilder script = new StringBuilder("");
			script.append("RAND_PROG 10\n\r");
			script.append("  IF isbehave($i *) OR affected($i *)\n\r");
			script.append("    MPSCRIPT $i DELETE *\n\r");
			script.append("    RETURN\n\r");
			script.append("  ELSE\n\r");
			script.append("    IF numpcsroom(< 1)\n\r");
			script.append("      MPPURGE self\n\r");
			script.append("    ENDIF\n\r");
			script.append("  ENDIF\n\r");
			effA.invoke(newMOB, new XVector<String>(newMOB.Name(), ""+mudHrFromNow, script.toString()),mob,true,0);
			script.append("~\n\r");

			this.runawayM = newMOB;
		}
		return true;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(!CMLib.flags().isAliveAwakeMobile(mob,false))
			return false;

		if(!CMLib.flags().canBeSeenBy(mob.location(),mob))
		{
			mob.tell(L("You can't see anything!"));
			return false;
		}

		if(!CMLib.law().isACity(mob.location().getArea()))
		{
			mob.tell(L("This can only be done in a city."));
			return false;
		}

		if((this.lastCast > 0)&&(System.currentTimeMillis() < this.lastCast))
		{
			mob.tell(L("You need to give some time for kids to become tired of their parents."));
			return false;
		}

		final List<Ability> V=CMLib.flags().flaggedAffects(mob,Ability.FLAG_TRACKING);
		for(final Ability A : V) A.unInvoke();
		if(V.size()>0)
		{
			mob.tell(L("You stop following the clues."));
			if((commands.size()==0)||(CMParms.combine(commands,0).equalsIgnoreCase("stop")))
				return true;
		}

		theTrail=null;
		nextDirection=-2;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		TrackingLibrary.TrackingFlags flags;
		flags=CMLib.tracking().newFlags()
			.plus(TrackingLibrary.TrackingFlag.PASSABLE)
			.plus(TrackingLibrary.TrackingFlag.AREAONLY)
			.plus(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS)
			.plus(TrackingLibrary.TrackingFlag.NOAIR)
			.plus(TrackingLibrary.TrackingFlag.NOWATER);
		final int range=Math.max(5,(int)Math.round(Math.sqrt(mob.location().getArea().numberOfProperIDedRooms())
				- (super.getXLEVELLevel(mob))-super.getXMAXRANGELevel(mob)));
		final List<Room> rooms = CMLib.tracking().getRadiantRooms(mob.location(), flags, range);
		Room targetR = null;
		for(int i=rooms.size()-1; i>=0; i--)
		{
			final Room R = rooms.get(i);
			if((R!=null)&&(CMLib.flags().canAccess(mob, R)))
			{
				targetR = R;
				break;
			}
		}
		if(targetR == null)
		{
			mob.tell(L("This place doesn't look like it would have runaways."));
			return false;
		}
		theTrail=CMLib.tracking().findTrailToRoom(mob.location(),targetR,flags,range+5);
		if((success)&&(theTrail!=null))
		{
			theTrail.add(mob.location());

			final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> look(s) for signs of runaways."));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				displayText=L("(Tracking a Runaway)");
				final Thief_FindRunaway newOne=(Thief_FindRunaway)this.copyOf();
				if(mob.fetchEffect(newOne.ID())==null)
					mob.addEffect(newOne);
				mob.recoverPhyStats();
				this.lastCast = System.currentTimeMillis() + (CMProps.getMillisPerMudHour() * CMLib.time().homeClock(mob).getHoursInDay());
				newOne.nextDirection=CMLib.tracking().trackNextDirectionFromHere(theTrail,mob.location(),true);
			}
		}
		else
			return beneficialVisualFizzle(mob,null,L("<S-NAME> attempt(s) to look for runaways, but can't find any clues."));
		// return whether it worked
		return success;
	}
}
