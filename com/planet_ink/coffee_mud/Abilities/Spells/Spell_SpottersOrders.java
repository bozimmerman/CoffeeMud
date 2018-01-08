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
   Copyright 2011-2018 Bo Zimmerman

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

public class Spell_SpottersOrders extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_SpottersOrders";
	}

	private final static String localizedName = CMLib.lang().L("Spotters Orders");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return L("(Spotting weaknesses of "+text()+")");
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_SELF;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_DIVINATION;
	}

	protected MOB spottedM=null;
	protected boolean activated=true;
	protected List<Triad<MOB,Ability,long[]>> groupMembers=null;

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(target instanceof MOB)
		{
			final MOB M=(MOB)target;
			if(!M.isInCombat())
				return Ability.QUALITY_INDIFFERENT;
			if(M.fetchEffect("Spell_DetectWeaknesses")==null)
				return Ability.QUALITY_INDIFFERENT;
			if((M.amFollowing()==null)&&(M.numFollowers()==0))
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);

		if((affected instanceof MOB)&&(activated))
			affectableStats.setSpeed(affectableStats.speed()-0.5);
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if(ticking instanceof MOB)
		{
			final MOB mob=(MOB)ticking;
			if((!mob.isInCombat())
			||((spottedM!=null) && (spottedM.amDead())))
			{
				unInvoke();
				return false;
			}
			final MOB victim=mob.getVictim();
			if((victim!=null)
			&&((victim==spottedM)||( (spottedM==null) && victim.Name().equalsIgnoreCase(text()))))
			{
				if(!activated)
				{
					activated=true;
					mob.recoverPhyStats();
				}
			}
			else
			{
				if(activated)
				{
					activated=false;
					mob.recoverPhyStats();
				}
			}
			if(groupMembers==null)
			{
				final Set<MOB> grp=mob.getGroupMembers(new TreeSet<MOB>());
				groupMembers=new LinkedList<Triad<MOB,Ability,long[]>>();
				for(final MOB M : grp)
				{
					final long[] time=new long[]{System.currentTimeMillis()-1};
					final Triad<MOB,Ability,long[]> P=new Triad<MOB,Ability,long[]>(M,null,time);
					groupMembers.add(P);
				}
			}
			for(final Triad<MOB,Ability,long[]> P : groupMembers)
			{
				boolean ishouldhaveit=activated;
				if(P.first.location()!=mob.location())
					ishouldhaveit=false;
				if(ishouldhaveit)
				{
					if(System.currentTimeMillis() > P.third[0])
					{
						P.third[0]=System.currentTimeMillis() + (CMLib.dice().roll(1, 4, 0) * CMProps.getTickMillis()) -1;
						if(mob!=P.first)
							P.first.tell(mob,spottedM,null,L("<S-NAME> telepathically imparts the weaknesses of <T-NAME> to you."));
					}
					if(P.second==null)
					{
						P.second=CMClass.getAbility("Spell_DetectWeaknesses");
						if(P.second!=null)
						{
							P.second.setMiscText(text());
							if(P.second instanceof Spell_DetectWeaknesses)
								((Spell_DetectWeaknesses)P.second).spottedM=spottedM;
							P.second.setStat("TICKDOWN", Integer.toString(tickDown));
							P.second.setInvoker(mob);
							P.first.addEffect(P.second);
							P.second.tick(P.first, Tickable.TICKID_MOB);
							P.first.recoverPhyStats();
							if((invoker()!=null)&&(invoker()!=P.first))
								P.first.tell(L("You can sense the shared thoughts of @x1.",invoker().Name()));
						}
					}
				}
				else
				{
					if(P.second!=null)
					{
						P.first.delEffect(P.second);
						P.second=null;
						P.first.recoverPhyStats();
					}
				}
			}
		}
		return true;
	}

	@Override
	public void unInvoke()
	{
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		if(canBeUninvoked())
		{
			if(groupMembers!=null)
			{
				for(final Triad<MOB,Ability,long[]> Ms : groupMembers)
				{
					final Ability A=Ms.second;
					if((A!=null)&&(A.invoker()==mob))
						A.unInvoke();
				}
			}
			groupMembers=null;
		}
		super.unInvoke();
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(!target.isInCombat())
		{
			mob.tell(target,null,null,L("<T-NAME> <T-IS-ARE> not in combat."));
			return false;
		}

		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,L("<S-NAME> <S-IS-ARE> already knowledgable about <S-HIS-HER> target."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,somanticCastCode(mob,target,auto),auto?"":L("^S<S-NAME> point(s) at <S-HIS-HER> group members and knowingly cast(s) a spell concerning <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(beneficialAffect(mob,target,asLevel,0)!=null)
				{
					final Spell_SpottersOrders A=(Spell_SpottersOrders)target.fetchEffect(ID());
					final MOB victim=target.getVictim();
					if(A!=null)
					{
						A.spottedM=victim;
						A.setMiscText(victim.Name());
						mob.location().show(target,victim,CMMsg.MSG_OK_VISUAL,L("<S-NAME> attain(s) knowledge of <T-YOUPOSS> weaknesses!"));
					}
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,target,L("<S-NAME> point(s) at <S-HIS-HER> group members speak(s) knowingly about <T-NAMESELF>, but nothing more happens."));

		// return whether it worked
		return success;
	}
}
