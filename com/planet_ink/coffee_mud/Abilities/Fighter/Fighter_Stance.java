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

import java.util.*;

/*
   Copyright 2020-2024 Bo Zimmerman

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
public class Fighter_Stance extends FighterSkill
{
	@Override
	public String ID()
	{
		return "Fighter_Stance";
	}

	private final static String localizedName = CMLib.lang().L("Stance");

	@Override
	public String name()
	{
		return localizedName;
	}

	private enum Stances
	{
		None,
		Defensive,
		Offensive,
		Wild
	}

	@Override
	public String displayText()
	{
		if((this.miscText.length()==0)
		||((affected instanceof MOB)&&(!((MOB)affected).isInCombat())))
			return "";
		else
			return L("(Stance: @x1)",text());
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_SELF;
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
	public boolean isAutoInvoked()
	{
		final Physical P=affecting();
		if(P==null)
			return true;
		if((P instanceof MOB)
		&&(((MOB)P).isInCombat())
		&&(((MOB)P).isMonster())
		&&(!((MOB)P).amDead()))
			return false; // makes combat abilities look at it.
		return true;
	}

	@Override
	public boolean canBeUninvoked()
	{
		return false;
	}

	private static final String[] triggerStrings =I(new String[] {"STANCE"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_MARTIALLORE;
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if((mob!=null)&&(mob.isInCombat()))
		{
			final Stances stance=this.stance;
			if((stance==null)||(stance==Stances.None))
				return Ability.QUALITY_BENEFICIAL_SELF;
			final MOB victiM=mob.getVictim();
			if(victiM != null)
			{
				final Fighter_Stance vicStanceA= (Fighter_Stance)victiM.fetchEffect(ID());
				if(vicStanceA!=null)
				{
					final Stances counterStance=(Stances)CMath.s_valueOf(Stances.class, CMStrings.capitalizeAndLower(vicStanceA.text()));
					if(counterStance == null)
						return Ability.QUALITY_BENEFICIAL_SELF;
					switch(stance)
					{
					case Defensive:
						if(counterStance==Stances.Wild)
							return Ability.QUALITY_BENEFICIAL_SELF;
						break;
					case None:
						return Ability.QUALITY_BENEFICIAL_SELF;
					case Offensive:
						if(counterStance==Stances.Defensive)
							return Ability.QUALITY_BENEFICIAL_SELF;
						break;
					case Wild:
						if(counterStance==Stances.Offensive)
							return Ability.QUALITY_BENEFICIAL_SELF;
						break;
					}
				}
			}
			return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	protected volatile MOB				lastVictimM			= null;
	protected volatile Fighter_Stance	lastVictimStancerA	= null;
	protected volatile Stances			stance				= Stances.None;

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(!(affected instanceof MOB))
			return true;

		final MOB mob=(MOB)affected;
		if(mob.isInCombat())
		{
			if(lastVictimM != mob.getVictim())
			{
				lastVictimM=mob.getVictim();
				lastVictimStancerA=(Fighter_Stance)lastVictimM.fetchEffect(ID());
				mob.recoverPhyStats();
				mob.recoverMaxState();
			}
		}
		return true;
	}

	@Override
	public void setMiscText(final String newMiscText)
	{
		if(newMiscText.length()>0)
		{
			final Stances newStance=(Stances)CMath.s_valueOf(Stances.class, CMStrings.capitalizeAndLower(newMiscText));
			if(newStance!=null)
				stance=newStance;
		}
		super.setMiscText(newMiscText);
	}

	@Override
	public void affectPhyStats(final Physical affectedEnv, final PhyStats affectableStats)
	{
		if((affectedEnv instanceof MOB)
		&&(((MOB)affectedEnv).isInCombat())
		&&(this.stance!=null))
		{
			final Stances stance=this.stance;
			if(stance != null)
			{
				affectableStats.addAmbiance("(Stance: "+stance.toString()+")");
				final Fighter_Stance vicStanceA=lastVictimStancerA;
				final MOB victimM=lastVictimM;
				Stances counterStance=null;
				if((vicStanceA!=null)
				&&(vicStanceA.text().length()>0)
				&&(victimM!=null)
				&&(victimM.getVictim()==affectedEnv)
				&&(((MOB)affectedEnv).getVictim()==victimM))
					counterStance=(Stances)CMath.s_valueOf(Stances.class, CMStrings.capitalizeAndLower(lastVictimStancerA.text()));
				final double baseArmor=affectableStats.armor()-100.0;
				final double exp=super.getXLEVELLevel((MOB)affectedEnv)/100.0;
				final double exp2=exp/2.0;
				switch(stance)
				{
				case Offensive:
					if(counterStance==Stances.Defensive)
						return;
					affectableStats.setAttackAdjustment((int)Math.round(CMath.mul(affectableStats.attackAdjustment(),1.1+exp)));
					affectableStats.setArmor(affectableStats.armor()-(int)Math.round(CMath.mul(baseArmor,0.05+exp2))); // actually adds, since baseArmor <=0
					break;
				case Defensive:
					if(counterStance==Stances.Wild)
						return;
					affectableStats.setAttackAdjustment((int)Math.round(CMath.mul(affectableStats.attackAdjustment(),0.95-exp2)));
					affectableStats.setArmor(affectableStats.armor()+(int)Math.round(CMath.mul(baseArmor,0.1+exp))); // actually subs, since baseArmor <=0
					break;
				case None:
					break;
				case Wild:
					if(counterStance==Stances.Offensive)
						return;
					affectableStats.setDamage((int)Math.round(CMath.mul(affectableStats.damage(),1.1+(exp/2))));
					affectableStats.setAttackAdjustment((int)Math.round(CMath.mul(affectableStats.attackAdjustment(),0.95-exp2)));
					affectableStats.setArmor(affectableStats.armor()-(int)Math.round(CMath.mul(baseArmor,0.05+exp2))); // actually adds, since baseArmor <=0
					break;
				}
			}
		}
		super.affectPhyStats(affectedEnv, affectableStats);
	}

	@Override
	public boolean autoInvocation(final MOB mob, final boolean force)
	{
		final boolean res=super.autoInvocation(mob, force);
		if(res&&(mob != null)&&(this.stance==Stances.None))
		{
			String stanceStr=text();
			if(stanceStr.length()==0)
			{
				if(mob.isPlayer())
					return res;
				stanceStr=Stances.values()[CMLib.dice().roll(1, Stances.values().length, -1)].toString();
			}
			final Fighter_Stance stanceA= (Fighter_Stance)mob.fetchEffect(ID());
			if(stanceA!=null)
				stanceA.setMiscText(stanceStr);
			final Fighter_Stance myA=(Fighter_Stance)mob.fetchAbility(ID());
			if(myA!=null)
				myA.setAffectedOne(mob); // special thing for combat abilities
		}
		return res;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(!mob.isInCombat())
		{
			mob.tell(L("You must be in combat to change your stance."));
			return false;
		}

		if(mob.isMonster())
		{
			final String args=CMParms.combine(commands,0);
			if(CMath.s_valueOf(Stances.class, args)==null)
			{
				String newStance=text();
				while(newStance.equalsIgnoreCase(text())||(newStance.equalsIgnoreCase(Stances.None.toString())))
					newStance=Stances.values()[CMLib.dice().roll(1, Stances.values().length, -1)].toString();
				commands.clear();
				commands.add(newStance);
			}
		}

		if(commands.size()<1)
		{
			if(text().length()>0)
				mob.tell(L("Current Stance: @x1",text()));
			mob.tell(L("Use which fighting stance?  Try @x1.",CMParms.toListString(Stances.values())));
			return false;
		}

		final String stanceStr=CMStrings.capitalizeAndLower(CMParms.combine(commands,0));
		final Stances stance=(Stances)CMath.s_valueOf(Stances.class, stanceStr);
		if(stance==null)
		{
			mob.tell(L("'@x1' is an invalid fighting stance.  Try @x2.",stanceStr,CMParms.toListString(Stances.values())));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			invoker=mob;
			final Room R=mob.location();
			if(R==null)
				return false;
			final CMMsg msg;
			if(stance==Stances.None)
				msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> <S-IS-ARE> no longre in a formal fighting stance.^N",stanceStr));
			else
				msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,L("^F<S-NAME> get(s) into a @x1 fighting stance.^N",stanceStr));
			if((R!=null)&&(R.okMessage(mob,msg)))
			{
				R.send(mob,msg);
				setMiscText(stanceStr);
				final Fighter_Stance stanceA= (Fighter_Stance)mob.fetchEffect(ID());
				if(stanceA!=null)
					stanceA.setMiscText(stanceStr);
				mob.recoverPhyStats();
				mob.recoverMaxState();
			}
		}
		else
			return maliciousFizzle(mob,null,L("<S-NAME> fail(s) to change <S-HIS-HER> fighting stance."));

		// return whether it worked
		return success;
	}

}
