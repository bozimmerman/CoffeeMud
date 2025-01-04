package com.planet_ink.coffee_mud.Abilities.Fighter;
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

import java.io.IOException;
import java.util.*;

/*
   Copyright 2023-2025 Bo Zimmerman

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
public class Fighter_ScoutAhead extends FighterSkill
{
	@Override
	public String ID()
	{
		return "Fighter_ScoutAhead";
	}

	private final static String	localizedName	= CMLib.lang().L("Scout Ahead");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "SCOUT" });

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_EVASIVE;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	protected long expire = Long.MAX_VALUE;
	protected Room expireR = null;

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(System.currentTimeMillis()<expire)
			affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.IS_SNEAKING);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(System.currentTimeMillis()>expire)
		{
			unInvoke();
			return true;
		}
		if(((msg.targetMajor()&CMMsg.MASK_MALICIOUS)>0)
		&&(!CMath.bset(msg.sourceMajor(),CMMsg.MASK_ALWAYS))
		&&((msg.amITarget(affected))))
		{
			final MOB target=(MOB)msg.target();
			if((!target.isInCombat())
			&&(msg.source().getVictim()!=target)
			&&(msg.source().location()==target.location()))
			{
				msg.source().tell(L("You don't catch @x1",target.name(msg.source())));
				if(target.getVictim()==msg.source())
				{
					target.makePeace(true);
					target.setVictim(null);
				}
				return false;
			}
		}
		if((msg.source()==affected)
		&&(msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(msg.target() == expireR))
			unInvoke();
		return super.okMessage(myHost,msg);
	}

	@Override
	public void executeMsg(final Environmental affecting, final CMMsg msg)
	{
		super.executeMsg(affecting, msg);
		if((msg.source()==affected)
		&&(msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(msg.target() == expireR))
			unInvoke();
	}

	@Override
	public void unInvoke()
	{
		final Physical P = affected;
		super.unInvoke();
		if(P != null)
			P.recoverPhyStats();
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		return System.currentTimeMillis()<expire;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(mob.isInCombat()
		&&(!auto))
		{
			mob.tell(L("Not while you are in combat!"));
			return false;
		}

		if((!(mob.riding() instanceof MOB))
		&&(!auto))
		{
			mob.tell(L("You need to be mounted to scout ahead."));
			return false;
		}

		if(commands.size()==0)
		{
			if(!auto)
			{
				mob.tell(L("You need to specify which directions to scout."));
				return false;
			}
			final int dir = CMLib.dice().roll(1, Directions.NUM_DIRECTIONS(), -1);
			commands.add(CMLib.directions().getDirectionName(dir));
		}

		final int limit = 1 + super.getXLEVELLevel(mob);
		final List<String> backDirs = new ArrayList<String>();
		int totMoves = 0;
		int direction;
		for(int v=0;v<commands.size();v++)
		{
			int num=1;
			String s=commands.get(v);
			if(CMath.s_int(s)>0)
			{
				num=CMath.s_int(s);
				v++;
				if(v<commands.size())
					s=commands.get(v);
			}
			else
			if((s.length()>0) && (Character.isDigit(s.charAt(0))))
			{
				int x=1;
				while((x<s.length()-1)&&(Character.isDigit(s.charAt(x))))
					x++;
				num=CMath.s_int(s.substring(0,x));
				s=s.substring(x);
			}

			final Directions.DirType dirType = CMLib.flags().getDirType(mob.location());
			if(mob.isMonster())
				direction=CMLib.directions().getGoodDirectionCode(s);
			else
				direction=CMLib.directions().getGoodDirectionCode(s, dirType);
			if(direction>=0)
			{
				for(int i=0;i<num;i++)
				{
					totMoves++;
					if((totMoves > limit)&&(!auto))
					{
						if(limit == 1)
							mob.tell(L("You can only scout 1 move ahead."));
						else
							mob.tell(L("You can only scout up to @x1 moves ahead.",""+limit));
						return false;
					}
					backDirs.add(CMLib.directions().getDirectionName(Directions.getOpDirectionCode(direction), dirType));
				}
			}
			else
			{
				mob.tell(L("'@x1' is not a valid direction to scout through.",s));
				return false;
			}
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			invoker=mob;
			final Room R=mob.location();
			final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_NOISYMOVEMENT|(auto?CMMsg.MASK_ALWAYS:0),
					L("<S-NAME> scout(s) ahead."));
			if((R!=null)
			&&(R.okMessage(mob,msg)))
			{
				R.send(mob,msg);
				final Rideable ride = mob.riding();
				if(ride instanceof MOB)
				{
					this.expire = System.currentTimeMillis() + CMProps.getTickMillis();
					this.tickDown=2;
					this.expireR = mob.location();
					Ability rideA = (Ability)copyOf();
					rideA.setSavable(false);
					ride.addEffect(rideA);
					ride.recoverPhyStats();
					for(int r=0;r<ride.numRiders();r++)
					{
						final Rider mR = ride.fetchRider(r);
						if(mR instanceof MOB)
						{
							rideA = (Ability)copyOf();
							rideA.setSavable(false);
							mR.addEffect(rideA);
							ride.recoverPhyStats();
						}
					}
					commands.add(0,"GO");
					final Command C = CMClass.getCommand("Go");
					commands.addAll(backDirs);
					try
					{
						C.execute(mob, commands, MUDCmdProcessor.METAFLAG_FORCED);
					}
					catch (final IOException e)
					{
					}
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,null,L("<S-NAME> attempt(s) to scout ahead, but get(s) lost."));

		// return whether it worked
		return success;
	}
}
