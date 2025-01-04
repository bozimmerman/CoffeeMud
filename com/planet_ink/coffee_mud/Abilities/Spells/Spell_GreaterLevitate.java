package com.planet_ink.coffee_mud.Abilities.Spells;
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
   Copyright 2021-2025 Bo Zimmerman

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
public class Spell_GreaterLevitate extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_GreaterLevitate";
	}

	private final static String localizedName = CMLib.lang().L("Greater Levitate");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Greater Levitating)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int maxRange()
	{
		return adjustedMaxInvokerRange(5);
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS|CAN_ITEMS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_EVOCATION;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_MOVING;
	}

	protected volatile int maxRise = -1;
	protected volatile boolean temporarilyDisable = false;

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!(affected instanceof MOB))
			return true;

		final MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if(msg.amISource(mob))
		{
			if((msg.sourceMinor()==CMMsg.TYP_ADVANCE)
			||(msg.sourceMinor()==CMMsg.TYP_RETREAT)
			||(msg.sourceMinor()==CMMsg.TYP_LEAVE)
			||(msg.sourceMinor()==CMMsg.TYP_ENTER)
			||(msg.sourceMinor()==CMMsg.TYP_RETREAT))
			{
				mob.tell(L("You can't seem to go anywhere!"));
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_FLYING);
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if(maxRise == 0)
			return true;
		final Physical affected=this.affected;
		final Room curRoom = CMLib.map().roomLocation(affected);
		if(curRoom == null)
			return false;
		final Room upRoom = curRoom.getRoomInDir(Directions.UP);
		final Exit upExit = curRoom.getExitInDir(Directions.UP);
		if((upRoom == null)
		||(upExit == null)
		||(!upExit.isOpen()))
		{
			maxRise = 0;
			return true;
		}
		final long expiration=(affected instanceof Item)?((Item)affected).expirationDate():0;
		final boolean curUnderWatery = CMLib.flags().isUnderWateryRoom(curRoom);
		final boolean upUnderWatery = CMLib.flags().isUnderWateryRoom(upRoom);
		curRoom.show(invoker(),null,affected,CMMsg.MSG_OK_ACTION,L("<O-NAME> levitate(s) upward."));
		if(affected instanceof Item)
		{
			upRoom.moveItemTo((Item)affected,ItemPossessor.Expire.Player_Drop);
			((Item)affected).setExpirationDate(expiration);
		}
		else
		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;
			temporarilyDisable=true;
			try
			{
				CMLib.tracking().walk(mob,Directions.UP,false,false);
			}
			finally
			{
				temporarilyDisable=false;
			}
		}
		upRoom.show(invoker,null,affected,CMMsg.MSG_OK_ACTION,L("<O-NAME> levitates in from below."));
		if(maxRise<0)
		{
			if(curUnderWatery && (!upUnderWatery))
			{
				final Ability sinkA=affected.fetchEffect("Sinking");
				if(sinkA!=null)
				{
					sinkA.unInvoke();
					affected.delEffect(sinkA);
				}
				if((affected instanceof NavigableItem)
				&&(((NavigableItem)affected).navBasis()==Rideable.Basis.WATER_BASED))
				{
					if(((NavigableItem)affected).subjectToWearAndTear())
					{
						if(((NavigableItem)affected).usesRemaining()<5)
							((NavigableItem)affected).setUsesRemaining(5);
						((NavigableItem)affected).setExpirationDate(0);
					}
				}
				maxRise = 0;
			}
		}
		else
		if(maxRise > 0)
			maxRise = maxRise - 1;

		return true;
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
		{
			final Room R=CMLib.map().roomLocation(affected);
			if(R!=null)
				R.show(CMLib.map().deity(),affected,CMMsg.MSG_OK_ACTION,L("<T-NAME> stop(s) levitating."));
			super.unInvoke();
			return;
		}
		final MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> stop(s) levitating."));
			CMLib.commands().postStand(mob,true, false);
		}
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if(mob!=null)
		{
			if((mob.isMonster())&&(mob.isInCombat()))
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		int max = -1;
		if((commands!=null)
		&&(commands.size()>1)
		&&CMath.isInteger(commands.get(commands.size()-1)))
		{
			max=CMath.s_int(commands.remove(commands.size()-1));
			if(max<0)
			{
				mob.tell(L("'@x1' is not a valid number.",""+max));
				return false;
			}
		}
		final Physical target=getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_UNWORNONLY);
		if(target==null)
			return false;
		if(target instanceof Item)
		{
			if(mob.isMine(target))
			{
				mob.tell(L("You'd better set it down first!"));
				return false;
			}
			if(!CMLib.flags().isGettable((Item)target))
			{
				if(!(target instanceof NavigableItem))
				{
					mob.tell(L("You can't levitate @x1!",target.name(mob)));
					return false;
				}
			}
		}
		else
		if(target instanceof MOB)
		{
		}
		else
		{
			mob.tell(L("You can't levitate @x1!",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,somaticCastCode(mob,target,auto),auto?"":L("^S<S-NAME> wave(s) <S-HIS-HER> arms and cast(s) a great spell.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					final Spell_GreaterLevitate A = (Spell_GreaterLevitate)maliciousAffect(mob,target,asLevel,5+super.getXLEVELLevel(mob),-1);
					success = A!=null;
					if((success)&&(A!=null))
					{
						A.maxRise = max;
						if(target instanceof MOB)
							((MOB)target).location().show((MOB)target,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> begin(s) floating straight up!"));
						else
							mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("@x1 begin(s) floating straight up!",target.name()));
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> wave(s) <S-HIS-HER> hands at <T-NAME>, but the great spell fizzles."));
		// return whether it worked
		return success;
	}
}
