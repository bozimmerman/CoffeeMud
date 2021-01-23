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
   Copyright 2020-2020 Bo Zimmerman

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
public class Thief_CaseJoint extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_CaseJoint";
	}

	private final static String localizedName = CMLib.lang().L("Case Joint");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[] triggerStrings =I(new String[] {"CASEJOINT"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_STREETSMARTS;
	}

	@Override
	public int abilityCode()
	{
		return code;
	}

	@Override
	public void setAbilityCode(final int newCode)
	{
		code=newCode;
	}

	protected int code=0;

	public volatile int ticks=0;
	public Room mark=null;
	public volatile boolean otherSide = false;

	@Override
	public String displayText()
	{
		if(mark!=null)
		{
			if(otherSide)
				return "(Casing: "+mark.name()+", "+ticks+" ticks)";
			else
				return "(Cased: "+mark.name()+")";
		}
		return "";
	}

	@Override
	protected int getProficiencyBonus(final int oldBonus, Ability A)
	{
		if(otherSide && (oldBonus<100))
			return oldBonus+ticks;
		return 0;
	}
	
	@Override
	protected int getExpertiseBonus(final int oldBonus, Ability A)
	{
		if(otherSide && (oldBonus<10))
		{
			final int newBonus=oldBonus+(ticks/2);
			return newBonus>12?12:newBonus;
		}
		return 0;
	}

	@Override
	public void setMiscText(final String newMiscText)
	{
		super.setMiscText(newMiscText);
		otherSide=false;
		ticks=0;
		if(newMiscText.length()>0)
		{
			final Room R=CMLib.map().getRoom(newMiscText);
			if(R!=null)
				mark=R;
		}
	}
	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(otherSide
		&& (affected instanceof MOB)
		&&(((MOB)affected).getVictim()==mark))
		{
			final int xlvl=super.getXLEVELLevel(invoker());
			affectableStats.setDamage(affectableStats.damage()+((ticks+xlvl)/20));
			final double adjustedLevel=adjustedLevel((MOB)affected,0);
			final int expertise=super.getXLEVELLevel((MOB)affected);
			final int attBonus=(int)Math.round(0.33 * adjustedLevel)+3+(3*expertise);
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+attBonus);
			final int armBonus=(int)Math.round(0.14 * adjustedLevel)+1+(1*expertise);
			affectableStats.setArmor(affectableStats.armor()-armBonus);
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((text().length()==0)
		||((affected==null)||(!(affected instanceof MOB))))
		   return super.tick(ticking,tickID);
		final Room mark=this.mark;
		final Physical P=affected;
		if((mark != null)&&(P instanceof MOB))
		{
			final MOB mob=(MOB)P;
			if(mob.location()!=mark)
			{
				unInvoke();
				return false;
			}
			if(!otherSide)
			{
				if(!CMLib.flags().isHidden(mob)||(mob.isInCombat()))
				{
					otherSide=true;
					final int maxTicks=(super.adjustedLevel(mob,0)/5)+super.getXTIMELevel(mob);
					if(ticks>maxTicks)
						ticks=maxTicks;
					setTickDownRemaining(ticks);
					mob.recoverPhyStats();
				}
				else
					ticks++;
			}
		}
		return true;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Room target=mob.location();
		if(target==null)
			return false;

		if(mob.fetchEffect(ID())!=null)
		{
			mob.tell(L("You are already casing a join."));
			return false;
		}

		if((!auto)&&(!CMLib.flags().isHidden(mob)))
		{
			mob.tell(L("You must be hidden to do that."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(!success)
			return beneficialVisualFizzle(mob,target,L("<S-NAME> lose(s) <S-HIS-HER> concentration on <T-NAMESELF>."));
		final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_DELICATE_SMALL_HANDS_ACT,L("<S-NAME> start(s) casing <T-NAMESELF>."),CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			final Ability A=super.beneficialAffect(mob, mob, asLevel, 0);
			if(A!=null)
			{
				A.makeLongLasting();
				A.setMiscText(CMLib.map().getExtendedRoomID(target));
				if(A instanceof Thief_CaseJoint)
					((Thief_CaseJoint)A).mark=target;
			}
		}
		return success;
	}

}
