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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2025 Bo Zimmerman

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
public class Chant_Reincarnation extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_Reincarnation";
	}

	private final static String localizedName = CMLib.lang().L("Reincarnation");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Reincarnation Geas)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT|Ability.DOMAIN_BREEDING;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	@Override
	protected int overrideMana()
	{
		return 200;
	}

	@Override
	public long flags()
	{
		return super.flags() | Ability.FLAG_POLYMORPHING;
	}

	Race newRace=null;
	volatile int newBaseWeightAdj = 0;

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(newRace!=null)
		{
			if(affected.name().indexOf(' ')>0)
				affectableStats.setName(L("@x1 called @x2",CMLib.english().startWithAorAn(newRace.name()),affected.name()));
			else
				affectableStats.setName(L("@x1 the @x2",affected.name(),newRace.name()));
			affectableStats.setWeight(affectableStats.weight()+newBaseWeightAdj);
		}
	}

	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(newRace!=null)
		{
			final int oldBaseWeight = affected.baseWeight();
			if(affectableStats.getMyRace()!=newRace)
			{
				affectableStats.getMyRace().unaffectCharStats(affected, affectableStats);
				affectableStats.setMyRace(newRace);
				newRace.affectCharStats(affected, affectableStats);
			}
			if(this.newBaseWeightAdj == 0)
				this.newBaseWeightAdj = affected.baseWeight() - oldBaseWeight;
			affectableStats.setWearableRestrictionsBitmap(affectableStats.getMyRace().forbiddenWornBits());
		}
	}

	@Override
	public void unInvoke()
	{
		super.unInvoke();
		if((!this.canBeUninvoked)
		&&(affected!=null)
		&&(affected.fetchEffect(ID())==this))
			this.unInvoked=false;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((tickID==Tickable.TICKID_MOB)
		&&(tickDown!=Integer.MAX_VALUE))
		{
			if((tickDown<=1)&&(!unInvoked))
			{
				tickDown=-1;
				// undo the affects of this spell
				if(!(affected instanceof MOB))
					return super.tick(ticking,tickID);
				final MOB mob=(MOB)affected;
				mob.tell(L("Your reincarnation geas is lifted as your form solidifies."));
				if(newRace!=null)
				{
					mob.baseCharStats().setMyRace(newRace);
					newRace.setHeightWeight(mob.basePhyStats(), mob.charStats().reproductiveCode());
					mob.recoverPhyStats();
					mob.recoverCharStats();
					mob.recoverMaxState();
				}
				unInvoke();
				if(mob.location()!=null)
					mob.location().recoverRoomStats();
			}
			if(!super.canBeUninvoked) // called during bring-to-life, which is why its down here
			{
				if((CMLib.flags().isInTheGame(affected, true))
				&&(!((MOB)affected).amDead()))
					super.canBeUninvoked=true;
				else
					tickDown--;
			}
		}
		return true;
	}

	public boolean isGolem(final Race R)
	{
		final MOB M=CMClass.getFactoryMOB();
		R.affectPhyStats(M,M.phyStats());
		final boolean golem= CMLib.flags().isGolem(M);
		M.destroy();
		return golem;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return true;
		final MOB mob=(MOB)affected;
		if((msg.sourceMinor()==CMMsg.TYP_DEATH)
			&&(msg.amISource(mob)))
		{
			newRace=null;
			while((newRace==null)
			||(isGolem(newRace))
			||(!newRace.canBreedWith(newRace, false))
			||(!CMath.bset(newRace.availabilityCode(),Area.THEME_FANTASY))
			||(newRace==mob.charStats().getMyRace())
			||(newRace.ID().equals("StdRace")))
				newRace=CMClass.randomRace();
			if(newRace!=null)
				mob.tell(L("You are being reincarnated as a @x1!!",newRace.name()));
			this.newBaseWeightAdj=0;
			msg.source().recoverCharStats();
			msg.source().recoverPhyStats();
			super.canBeUninvoked=false; // without this, bring to life removes it
			if((newRace != null)
			&&(!msg.source().isMonster()))
			{
				final String[] cmds=CMParms.toStringArray(CMParms.parseCommas(CMProps.get(msg.source().session()).getStr(CMProps.Str.PLAYERDEATH),true));
				for(final String cmd : cmds)
				{
					if(cmd.toUpperCase().startsWith("PUR"))
					{
						int maxLives = 1;
						final int x = cmd.indexOf(' ');
						if(x>0)
							maxLives = CMath.s_int(cmd.substring(x+1).trim());
						if((msg.source().playerStats()==null)
						||(msg.source().playerStats().deathCounter(0)>=maxLives-1))
							return false;
					}
				}
			}
		}
		return true;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=getTarget(mob,commands,givenTarget,false,true);
		if(target==null)
			return false;
		if(target.fetchEffect(ID())!=null)
		{
			if(mob.location().show(mob,target,null,CMMsg.MSG_CAST,L("<S-NAME> lift(s) the reincarnation geas on <T-NAMESELF>.")))
				target.delEffect(target.fetchEffect(ID()));
			else
				mob.location().show(mob,target,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> fail(s) to lift the reincarnation geas on <T-NAMESELF>."));
			return false;
		}
		if(target.isMonster()
		|| (CMLib.flags().isUndead(target))
		|| (CMLib.flags().isGolem(target)))
		{
			mob.tell(L("Your chant would have no effect on such a creature."));
			return false;
		}

		boolean success=proficiencyCheck(mob,0,auto);
		final Set<MOB> groupMembers=mob.getGroupMembers(new HashSet<MOB>());
		if(success
		&&(!auto)
		&&(mob!=target)
		&&(!mob.mayIFight(target))
		&&(!groupMembers.contains(target)))
		{
			mob.tell(L("@x1 is a player, so you must be group members, and your playerkill flags must be on for this to work.",target.name(mob)));
			success=false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if(success)
		{
			int modifier=0;
			if((target!=mob)&&(!groupMembers.contains(target)))
				modifier=CMMsg.MASK_MALICIOUS;
			final CMMsg msg=CMClass.getMsg(mob,target,this,modifier|verbalCastCode(mob,target,auto),
					auto?L("^S<S-NAME> get(s) put under a reincarnation geas!^?"):
						L("^S<S-NAME> chant(s) a reincarnation geas upon <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final Ability A = beneficialAffect(mob,target,asLevel,1800);
				if(A != null)
				{
					target.delEffect(A);
					target.addPriorityEffect(A);
				}
			}
		}
		else
			beneficialWordsFizzle(mob,target,L("<S-NAME> chant(s) for a reincarnation geas, but nothing happens."));

		return success;
	}
}
