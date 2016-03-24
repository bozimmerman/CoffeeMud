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
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2016 Bo Zimmerman

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
public class Thief_SetAlarm extends ThiefSkill implements Trap
{
	@Override
	public String ID()
	{
		return "Thief_SetAlarm";
	}

	private final static String	localizedName	= CMLib.lang().L("Set Alarm");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_EXITS;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_EXITS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_TRAPPING;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[]	triggerStrings	= I(new String[] { "SETALARM" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT | USAGE_MANA;
	}

	protected boolean	sprung	= false;
	public Room			room1	= null;
	public Room			room2	= null;

	@Override
	public boolean isABomb()
	{
		return false;
	}

	@Override
	public void activateBomb()
	{
	}

	@Override
	public boolean sprung()
	{
		return sprung;
	}

	@Override
	public boolean disabled()
	{
		return false;
	}

	@Override
	public void disable()
	{
		unInvoke();
	}

	@Override
	public void setReset(int Reset)
	{
	}

	@Override
	public int getReset()
	{
		return 0;
	}

	@Override
	public void resetTrap(MOB mob)
	{

	}

	@Override
	public boolean maySetTrap(MOB mob, int asLevel)
	{
		return false;
	}

	@Override
	public boolean canSetTrapOn(MOB mob, Physical P)
	{
		return false;
	}

	@Override
	public List<Item> getTrapComponents()
	{
		return new Vector<Item>(1);
	}

	@Override
	public boolean canReSetTrap(MOB mob)
	{
		return false;
	}

	@Override
	public String requiresToSet()
	{
		return "";
	}

	@Override
	public Trap setTrap(MOB mob, Physical P, int trapBonus, int qualifyingClassLevel, boolean perm)
	{
		beneficialAffect(mob, P, qualifyingClassLevel + trapBonus, 0);
		return (Trap) P.fetchEffect(ID());
	}

	@Override
	public void spring(MOB M)
	{
		sprung=true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(sprung){	return;	}
		super.executeMsg(myHost,msg);

		if((msg.amITarget(affected))&&(msg.targetMinor()==CMMsg.TYP_OPEN))
		{
			if((!msg.amISource(invoker())
			&&(CMLib.dice().rollPercentage()>msg.source().charStats().getSave(CharStats.STAT_SAVE_TRAPS))))
				spring(msg.source());
		}
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((affected==null)||(!(affected instanceof Exit))||(room1==null)||(room2==null))
			return false;
		if(sprung)
		{
			final List<Room> rooms=new ArrayList<Room>();
			TrackingLibrary.TrackingFlags flags;
			flags = CMLib.tracking().newFlags()
					.plus(TrackingLibrary.TrackingFlag.OPENONLY)
					.plus(TrackingLibrary.TrackingFlag.AREAONLY);
			CMLib.tracking().getRadiantRooms(room1,rooms,flags,null,10+(getXLEVELLevel(invoker())*2),null);
			CMLib.tracking().getRadiantRooms(room2,rooms,flags,null,10+(getXLEVELLevel(invoker())*2),null);
			final List<MOB> mobsDone=new ArrayList<MOB>();
			room1.showHappens(CMMsg.MSG_NOISE,L("A horrible alarm is going off here."));
			room2.showHappens(CMMsg.MSG_NOISE,L("A horrible alarm is going off here."));
			for(int r=0;r<rooms.size();r++)
			{
				final Room R=rooms.get(r);
				if((R!=room1)&&(R!=room2))
				{
					final int dir=CMLib.tracking().radiatesFromDir(R,rooms);
					if(dir>=0)
					{
						R.showHappens(CMMsg.MSG_NOISE,L("You hear a loud alarm @x1.",CMLib.directions().getInDirectionName(dir)));
						for(int i=0;i<R.numInhabitants();i++)
						{
							final MOB M=R.fetchInhabitant(i);
							if((M!=null)
							&&(M.isMonster())
							&&(!M.isInCombat())
							&&(CMLib.flags().isMobile(M))
							&&(!mobsDone.contains(M))
							&&(CMLib.flags().canHear(M))
							&&(CMLib.dice().rollPercentage()>M.charStats().getSave(CharStats.STAT_SAVE_MIND))
							&&(CMLib.dice().rollPercentage()>M.charStats().getSave(CharStats.STAT_SAVE_TRAPS)))
							{
								mobsDone.add(M);
								CMLib.tracking().walk(M,dir,false,false);
							}
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
		final String whatToalarm=CMParms.combine(commands,0);
		Exit alarmThis=null;
		final int dirCode=CMLib.directions().getGoodDirectionCode(whatToalarm);
		if(dirCode>=0)
			alarmThis=mob.location().getExitInDir(dirCode);
		if((alarmThis==null)||(!alarmThis.hasADoor()))
		{
			mob.tell(L("You can't set an alarm that way."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		final CMMsg msg=CMClass.getMsg(mob,alarmThis,this,auto?CMMsg.MSG_OK_ACTION:CMMsg.MSG_THIEF_ACT,CMMsg.MASK_ALWAYS|CMMsg.MSG_THIEF_ACT,CMMsg.MSG_OK_ACTION,(auto?L("@x1 begins to glow!",alarmThis.name()):L("<S-NAME> attempt(s) to lay a trap on @x1.",alarmThis.name())));
		if(mob.location().okMessage(mob,msg))
		{
			invoker=mob;
			mob.location().send(mob,msg);
			if(success)
			{
				sprung=false;
				room1=mob.location();
				room2=mob.location().getRoomInDir(dirCode);
				mob.tell(L("You have set the alarm."));
				beneficialAffect(mob,alarmThis,asLevel,0);
			}
			else
			{
				if(CMLib.dice().rollPercentage()>50)
				{
					beneficialAffect(mob,alarmThis,asLevel,0);
					mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> trigger(s) the alarm on accident!"));
					final Trap T=(Trap)alarmThis.fetchEffect(ID());
					if(T!=null)
						T.spring(mob);
				}
				else
				{
					mob.tell(L("You fail in your attempt to set an alarm."));
				}
			}
		}
		return success;
	}
}
