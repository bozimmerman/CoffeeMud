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
import com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimeOfDay;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB.Attrib;
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
public class Thief_Urchinize extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_Urchinize";
	}

	private final static String	localizedName	= CMLib.lang().L("Urchinize");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return "(Urchin)";
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
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_STREETSMARTS;
	}

	private static final String[]	triggerStrings	= I(new String[] { "URCHINIZE" });

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

	protected Map<MOB, Long>	failures		= new Hashtable<MOB, Long>();
	protected Boolean urchanizing = null;
	protected int tickUp = 0;
	protected int tickSuccess = 0;
	protected int lastDropOffDay = -1;

	protected static Set<MOB> myUrchins = Collections.synchronizedSet(new HashSet<MOB>());

	protected boolean forceUninvoke()
	{
		final Physical P = affected;
		if(P != null)
		{
			urchanizing = Boolean.FALSE;
			this.canBeUninvoked = true;
			unInvoke();
			P.delEffect(this);
		}
		return false;
	}

	@Override
	public void unInvoke()
	{
		if((affected instanceof MOB)
		&&(urchanizing == Boolean.FALSE)
		&&(this.canBeUninvoked))
			((MOB)affected).location().show((MOB)affected, null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> fail(s) to become an urchin."));
		super.unInvoke();
	}

	protected boolean urchanTick()
	{
		final Physical P = affected;
		final MOB invoker = invoker();
		if((!(P instanceof MOB))||(invoker==null)||(invoker==P))
			return forceUninvoke();
		final MOB M = (MOB)P;
		final Room R = M.location();
		if(M.isInCombat()||invoker.isInCombat()
		||(R != invoker.location())
		||(!CMLib.flags().isAliveAwakeMobileUnbound(M,true))
		||(!CMLib.flags().isAliveAwakeMobileUnbound(invoker,true)))
			return forceUninvoke();
		if((invoker.riding()!=null)
		||(!CMLib.flags().canBeSeenBy(M,invoker)))
			return forceUninvoke();
		if((++tickUp % 4)==0)
		{
			if(tickUp > tickSuccess)
			{
				R.show(M, null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> <S-IS-ARE> now an urchin."));
				CMLib.commands().postFollow(M, null, false);
				urchanizing = null;
				final Ability A = CMClass.getAbility("Thief_Swipe");
				if(M.fetchAbility(A.ID())==null)
				{
					A.setProficiency(100);
					M.addAbility(A);
				}
				Behavior B = CMClass.getBehavior("Thiefness");
				B.setParms("Thief");
				M.addBehavior(B);
				B=CMClass.getBehavior("Scavenger");
				B.setParms("TRASH=\""+CMLib.map().getExtendedRoomID(R)+"\" ATNIGHT=TRUE");
				M.addBehavior(B);
				B = CMClass.getBehavior("Mobile");
				M.addBehavior(B);
				M.setLiegeID(invoker.Name());
				synchronized(myUrchins)
				{
					myUrchins.add(M);
				}
			}
			else
			if(tickDown==4)
			{
				if(!R.show(invoker, M,CMMsg.MSG_HANDS,L("<S-NAME> <S-IS-ARE> almost done urchanizing <T-NAME>.")))
					return forceUninvoke();
			}
			else
			{
				if(!R.show(invoker, M,CMMsg.MSG_HANDS,L("<S-NAME> conitnue(s) urchanizing <T-NAME>.")))
					return forceUninvoke();
			}
		}
		return true;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if(urchanizing == Boolean.TRUE)
			return this.urchanTick();
		return true;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((msg.targetMinor()==CMMsg.TYP_ORDER)
		&&(this.urchanizing == null)
		&&(msg.source().location()!=null)
		&&(msg.target() ==affected)
		&&(affected instanceof MOB)
		&&(msg.source().name().equals(((MOB)affected).getLiegeID()))
		&&(msg.sourceMessage()!=null)
		&&(msg.sourceMessage().length()>0))
		{
			final String say = CMStrings.getSayFromMessage(msg.sourceMessage()).toUpperCase();
			if((say.indexOf("DROP ")==0)&&(CMStrings.indexOfWord(say, "HERE")>0))
			{
				final Behavior B = ((MOB)affected).fetchBehavior("Scavenger");
				if(B != null)
				{
					CMLib.commands().postSay((MOB)affected, "Yessir");
					B.setParms("TRASH=\""+CMLib.map().getExtendedRoomID(msg.source().location())+"\" ATNIGHT=TRUE");
				}
				else
					CMLib.commands().postSay((MOB)affected, "You're not the boss of me!");
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if((mob.isInCombat())&&(!auto))
		{
			mob.tell(L("Not while you are fighting!"));
			return false;
		}
		final MOB target=getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(failures.containsKey(target) && (System.currentTimeMillis()<failures.get(target).longValue()))
		{
			mob.tell(L("You can't attempt to urchinize @x1 again so soon.",target.name(mob)));
			return false;
		}
		failures.remove(target);

		if((!auto)&&(target.amFollowing()!=mob))
		{
			mob.tell(L("@x1 is not your follower.",target.name(mob)));
			return false;
		}

		if((!Thief_Kidnapping.isKidnappable(mob, target))
		||(target.findTattoo("CHRISTENED")!=null))
		{
			mob.tell(L("@x1 doesn't seem like a viable urchin.",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final String str=L("<S-NAME> start(s) urchinizing <T-NAME>!");
			final CMMsg msg=CMClass.getMsg(mob,target,this,(auto?CMMsg.MASK_ALWAYS:0)|CMMsg.MSG_THIEF_ACT|CMMsg.MASK_SOUND,str);
			if(target.location().okMessage(mob,msg))
			{
				target.location().send(mob,msg);
				if(msg.value()<=0)
				{
					final Thief_Urchinize kA = (Thief_Urchinize)beneficialAffect(mob, target, asLevel, 0);
					if(kA != null)
					{
						kA.invoker = mob;
						kA.tickSuccess = 11 - super.getXTIMELevel(mob);
						kA.urchanizing = Boolean.TRUE;
						kA.makeNonUninvokable();
					}
				}
			}
		}
		else
		{
			failures.put(target, Long.valueOf(System.currentTimeMillis()+TimeManager.MILI_HOUR));
			return beneficialSoundFizzle(mob,target,L("<S-NAME> attempt(s) to urchinize <T-NAME> and fail(s)."));
		}
		return success;
	}
}
