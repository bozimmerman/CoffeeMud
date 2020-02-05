package com.planet_ink.coffee_mud.Abilities.Diseases;
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
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB.Attrib;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2019-2020 Bo Zimmerman

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
public class Disease_RoyaltyRot extends Disease
{
	@Override
	public String ID()
	{
		return "Disease_RoyaltyRot";
	}

	private final static String localizedName = CMLib.lang().L("Royal Rot");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Royal Rot)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
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
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public boolean putInCommandlist()
	{
		return false;
	}

	@Override
	protected int DISEASE_TICKS()
	{
		return getTicksPerDay() * CMLib.dice().roll(4, 9, 4);
	}

	@Override
	protected int DISEASE_DELAY()
	{
		return 7;
	}

	@Override
	protected String DISEASE_DONE()
	{
		return L("Your balance is restored.");
	}

	@Override
	protected String DISEASE_START()
	{
		return L("^G<S-NAME> start(s) suffering vertigo.^?");
	}

	@Override
	protected String DISEASE_AFFECT()
	{
		return "";
	}

	@Override
	public int abilityCode()
	{
		return 0;
	}

	@Override
	public int difficultyLevel()
	{
		return 3;
	}

	@Override
	public int spreadBitmap()
	{
		return 0;
	}

	protected volatile int fallDown = 0;
	protected volatile Ability lastBleed = null;

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(fallDown > 0)
			affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_SLEEPING);
	}

	@Override
	public void affectCharStats(final MOB E, final CharStats stats)
	{
		super.affectCharStats(E,stats);
		stats.setStat(CharStats.STAT_STRENGTH,stats.getStat(CharStats.STAT_STRENGTH)-5);
		if(stats.getStat(CharStats.STAT_STRENGTH) < 1)
			stats.setStat(CharStats.STAT_STRENGTH,1);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(msg.source()==affected)
		{
			if((fallDown>0)
			&&(msg.sourceMinor()==CMMsg.TYP_STAND))
				return false;
			else
			if((msg.source()==affected)
			&&(msg.sourceMinor()==CMMsg.TYP_ENTER)
			&&(msg.target() instanceof Room)
			&&(msg.tool() instanceof Exit)
			&&(!CMLib.flags().isFlying(msg.source()))
			&&(msg.source().location()!=null))
			{
				final double moveCost = msg.source().location().pointsPerMove()/5.0 * (msg.source().isAttributeSet(Attrib.AUTORUN)?2.0:1.0);
				if((Math.abs(Math.random())*100.0) <= moveCost)
				{
					final Disease_RoyaltyRot myA=this;
					if(CMLib.flags().isStanding(msg.source()))
					{
						CMLib.threads().executeRunnable(new Runnable() {
							final MOB mob=msg.source();
							final Disease_RoyaltyRot meA=myA;
							@Override
							public void run()
							{
								if((!mob.amDead())
								&&(mob.location()!=null))
								{
									final CMMsg msg2=CMClass.getMsg(mob,mob,meA,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|CMMsg.MASK_ALWAYS,L("<T-NAME> feel(s) weak..."));
									if(mob.location().okMessage(mob,msg2))
									{
										mob.location().send(mob,msg2);
										if(msg2.value()<=0)
										{
											mob.location().show(mob, null, CMMsg.MSG_OK_ACTION, L("<S-NAME> faint(s)!"));
											meA.fallDown=3;
											mob.recoverPhyStats();
										}
									}
								}
							}
						});
					}
				}
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		if((msg.target()==affected)
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.tool() instanceof Weapon)
		&&(msg.value()>0)
		&&(msg.target() instanceof MOB))
		{
			if((Math.round(CMath.div(msg.value(),((MOB)msg.target()).maxState().getHitPoints())*100.0)>=(CMProps.getIntVar(CMProps.Int.INJBLEEDPCTHP)/2)))
			{
				final Ability A2=CMClass.getAbility("Bleeding");
				if(A2!=null)
					A2.invoke((MOB)msg.target(),(MOB)msg.target(),true,0);
			}
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(affected==null)
			return false;
		if(!(affected instanceof MOB))
			return true;

		final MOB mob=(MOB)affected;
		if(!mob.amDead())
		{
			final Ability bleedA = mob.fetchEffect("Bleeding");
			if((bleedA != null)
			&&(bleedA != lastBleed))
			{
				lastBleed = bleedA;
				final int tickDown = CMath.s_int(bleedA.getStat("TICKDOWN"));
				if(tickDown>0)
					bleedA.setStat("TICKDOWN", ""+(tickDown*2));
			}

		}
		if((!mob.amDead())&&((--diseaseTick)<=0))
		{
			diseaseTick=DISEASE_DELAY();
			if(fallDown > 0)
			{
				if(--fallDown == 0)
					CMLib.commands().postStand(mob, true, false);
				mob.recoverPhyStats();
			}
			return true;
		}
		return true;
	}
}
