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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2002-2018 Bo Zimmerman

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

public class Thief_Bind extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_Bind";
	}

	private final static String localizedName = CMLib.lang().L("Bind");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return L("(Bound by "+ropeName+")");
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS|CAN_ROOMS;
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
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_BINDING;
	}

	private static final String[] triggerStrings =I(new String[] {"BIND"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	protected int maxRange=0;

	@Override
	public int maxRange()
	{
		return maxRange;
	}

	@Override
	public int minRange()
	{
		return 0;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_BINDING;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	@Override
	public boolean bubbleAffect()
	{
		return affected instanceof Room;
	}

	public int amountRemaining=500;
	public String ropeName="the ropes";

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_BOUND);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if((msg.source()==affected)||(affected instanceof Room))
		{
			if(((msg.sourceMinor()==CMMsg.TYP_SIT)||(msg.sourceMinor()==CMMsg.TYP_STAND))
			&&(affected instanceof MOB))
				return true;
			else
			if(((msg.sourceMinor()==CMMsg.TYP_LEAVE)||(msg.sourceMinor()==CMMsg.TYP_ENTER))
			&&(affected instanceof Room))
				return true;
			else
			if((!msg.sourceMajor(CMMsg.MASK_ALWAYS))
			&&((msg.sourceMajor(CMMsg.MASK_HANDS))
			||(msg.sourceMajor(CMMsg.MASK_MOVE))))
			{
				if(canBeUninvoked())
				{
					if(msg.source().location().show(msg.source(),null,CMMsg.MSG_OK_ACTION,L("<S-NAME> struggle(s) against @x1 binding <S-HIM-HER>.",ropeName.toLowerCase())))
					{
						amountRemaining-=(msg.source().charStats().getStat(CharStats.STAT_STRENGTH)+msg.source().phyStats().level());
						if(amountRemaining<0)
							unInvoke();
					}
				}
				else
					msg.source().tell(L("You are constricted by @x1 and can't move!",ropeName.toLowerCase()));
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public void setAffectedOne(Physical P)
	{
		if(!(P instanceof Item))
			super.setAffectedOne(P);
		else
			ropeName=P.name();
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
		{
			super.unInvoke();
			return;
		}
		final MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
		{
			if(!mob.amDead())
				mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> manage(s) to break <S-HIS-HER> way free of @x1.",ropeName));
			CMLib.commands().postStand(mob,true);
		}
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if((mob!=null)&&(target!=null))
		{
			if(!(target instanceof MOB))
				return Ability.QUALITY_INDIFFERENT;
			if(mob.isInCombat())
				return Ability.QUALITY_INDIFFERENT;
			if((!CMLib.flags().isSleeping(target))&&(CMLib.flags().canMove((MOB)target)))
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((mob.isInCombat())&&(!auto))
		{
			mob.tell(L("Not while you are fighting!"));
			return false;
		}
		if((commands.size()>0)&&(commands.get(0)).equalsIgnoreCase("UNTIE"))
		{
			commands.remove(0);
			final MOB target=super.getTarget(mob,commands,givenTarget,false,true);
			if(target==null)
				return false;
			final Ability A=target.fetchEffect(ID());
			if(A!=null)
			{
				if(mob.location().show(mob,target,null,CMMsg.MSG_HANDS,L("<S-NAME> attempt(s) to unbind <T-NAMESELF>.")))
				{
					A.unInvoke();
					return true;
				}
				return false;
			}
			mob.tell(L("@x1 doesn't appear to be bound with ropes.",target.name(mob)));
			return false;
		}

		final MOB target=getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if((!CMLib.flags().isSleeping(target))&&(CMLib.flags().canMove(target)&&(!auto)))
		{
			mob.tell(L("@x1 doesn't look willing to cooperate.",target.name(mob)));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			if(auto)
				maxRange=10;
			final String str=auto?L("<T-NAME> become(s) bound by @x1.",ropeName):L("<S-NAME> bind(s) <T-NAME> with @x1.",ropeName);
			final CMMsg msg=CMClass.getMsg(mob,target,this,(auto?CMMsg.MASK_ALWAYS:0)|CMMsg.MSG_THIEF_ACT|CMMsg.MASK_SOUND|CMMsg.MASK_MALICIOUS,auto?"":str,str,str);
			if((target.location().okMessage(mob,msg))&&(target.fetchEffect(this.ID())==null))
			{
				target.location().send(mob,msg);
				if(msg.value()<=0)
				{
					if(auto)
					{
						maxRange=0;
						double prof=0.0;
						final Ability A=mob.fetchAbility("Specialization_Ranged");
						if(A!=null)
							prof=CMath.div(A.proficiency(),20);
						amountRemaining=(mob.charStats().getStat(CharStats.STAT_STRENGTH)+mob.phyStats().level()+(2*getXLEVELLevel(mob)))*((int)Math.round(5.0+prof));
					}
					else
						amountRemaining=(adjustedLevel(mob,asLevel))*25;
					if((target.location()==mob.location())||(auto))
						success=maliciousAffect(mob,target,asLevel,Ability.TICKS_FOREVER,-1)!=null;
				}
				if((mob.getVictim()==target)&&(!auto))
				{
					final Set<MOB> H=mob.getGroupMembers(new HashSet<MOB>());
					MOB M=null;
					mob.makePeace(true);
					for (final Object element : H)
					{
						M=(MOB)element;
						if(M.getVictim()==target)
							M.setVictim(null);
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> attempt(s) to bind <T-NAME> and fail(s)."));

		// return whether it worked
		return success;
	}
}
