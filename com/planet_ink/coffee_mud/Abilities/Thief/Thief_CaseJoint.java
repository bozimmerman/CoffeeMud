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
   Copyright 2020-2021 Bo Zimmerman

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
				return "(Cased: "+mark.name()+")";
			else
				return "(Casing: "+mark.name()+", "+ticks+" ticks)";
		}
		return "";
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
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!(affected instanceof MOB))
			return super.okMessage(myHost, msg);

		final MOB mob=(MOB)affected;

		if(msg.amISource(mob) && (!otherSide))
		{
			if(((msg.sourceMinor()==CMMsg.TYP_ENTER)
				||(msg.sourceMinor()==CMMsg.TYP_LEAVE)
				||(msg.sourceMinor()==CMMsg.TYP_FLEE)
				||(msg.sourceMinor()==CMMsg.TYP_RECALL))
			&&(!msg.sourceMajor(CMMsg.MASK_ALWAYS))
			&&(msg.sourceMajor()>0))
			{
				stopCasing(msg.source());
			}
			else
			if((abilityCode()==0)
			&&(!msg.sourceMajor(CMMsg.MASK_ALWAYS))
			&&(msg.othersMinor()!=CMMsg.TYP_LOOK)
			&&(msg.othersMinor()!=CMMsg.TYP_EXAMINE)
			&&(msg.othersMajor()>0))
			{
				if(msg.othersMajor(CMMsg.MASK_SOUND))
				{
					stopCasing(msg.source());
				}
				else
				switch(msg.othersMinor())
				{
				case CMMsg.TYP_SPEAK:
				case CMMsg.TYP_CAST_SPELL:
					{
						stopCasing(msg.source());
					}
					break;
				case CMMsg.TYP_OPEN:
				case CMMsg.TYP_CLOSE:
				case CMMsg.TYP_LOCK:
				case CMMsg.TYP_UNLOCK:
				case CMMsg.TYP_PUSH:
				case CMMsg.TYP_PULL:
					if(((msg.target() instanceof Exit)
						||((msg.target() instanceof Item)
						   &&(!msg.source().isMine(msg.target())))))
					{
						stopCasing(msg.source());
					}
					break;
				}
			}
		}
		return super.okMessage(myHost, msg);
	}


	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(otherSide
		&& (affected instanceof MOB)
		&&(((MOB)affected).location()==mark))
		{
			final double adjustedLevel=adjustedLevel((MOB)affected,0);
			final int expertise=super.getXLEVELLevel((MOB)affected);
			final int attBonus=(int)Math.round(0.25 * adjustedLevel)+2+(2*expertise);
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+attBonus);
			final int armBonus=(int)Math.round(0.12 * adjustedLevel)+1+(1*expertise);
			affectableStats.setArmor(affectableStats.armor()-armBonus);
		}
	}

	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(otherSide
		&& (affected instanceof MOB)
		&&(affected.location()==mark))
		{
			// theoretical "ticks" max is 30
			affectableStats.adjustAbilityAdjustment("PROF+THIEF SKILL",affectableStats.getAbilityAdjustment("PROF+THIEF SKILL")+(ticks*3));
			if(ticks>9) // allow up to 3 expertise bonus
				affectableStats.adjustAbilityAdjustment("XLEVEL+THIEF SKILL",affectableStats.getAbilityAdjustment("XLEVEL+THIEF SKILL")+(ticks/10));
		}
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if(mob!=null)
		{
			if(mob.isInCombat())
				return Ability.QUALITY_INDIFFERENT;
			if(!CMLib.flags().isHidden(mob))
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	protected void stopCasing(final MOB mob)
	{
		if((mob!=null)
		&&(mark!=null)
		&&(!otherSide))
		{
			otherSide=true;
			setTickDownRemaining(ticks);
			mob.tell(L("You've finished casing @x1, and feel pretty confident.",mark.name(mob)));
			mob.recoverPhyStats();
			mob.recoverCharStats();
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
				final int maxTicks=(super.adjustedLevel(mob,0)/5)+super.getXTIMELevel(mob);
				if(!CMLib.flags().isHidden(mob)||(mob.isInCombat()))
					stopCasing(mob);
				else
				if(ticks<maxTicks)
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

		final Thief_CaseJoint jointA= (Thief_CaseJoint)mob.fetchEffect(ID());
		if(jointA!=null)
		{
			if(jointA.otherSide)
				mob.tell(L("You have already cased this joint."));
			else
				jointA.stopCasing(mob);
			return false;
		}

		if((!auto)&&((target.domainType()&Room.INDOORS)==0))
		{
			mob.tell(L("This only works indoors."));
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
