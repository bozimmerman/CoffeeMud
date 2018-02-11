package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
   Copyright 2004-2018 Bo Zimmerman

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

public class Druid_GolemForm extends StdAbility
{
	@Override
	public String ID()
	{
		return "Druid_GolemForm";
	}

	private final static String	localizedName	= CMLib.lang().L("Golem Form");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	private static final String[]	triggerStrings	= I(new String[] { "GOLEMFORM" });

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
		return Ability.ACODE_SKILL | Ability.DOMAIN_SHAPE_SHIFTING;
	}

	protected Race		newRace		= null;
	protected String	raceName	= "";
	protected int		raceLevel	= 0;

	@Override
	public String displayText()
	{
		if(newRace==null)
		{
			unInvoke();
			return "";
		}
		return "(in "+raceName+" form)";
	}

	private static String[] shapes={
		"Steel Golem",
		"Quartz Golem",
		"Mithril Golem",
		"Diamond Golem",
		"Adamantite Golem"
	};
	private static String[] races={
		"MetalGolem",
		"StoneGolem",
		"MetalGolem",
		"StoneGolem",
		"MetalGolem"
	};

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if((newRace!=null)&&(affected instanceof MOB))
		{
			affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_SPEAK);
			affectableStats.setName(CMLib.english().startWithAorAn(raceName.toLowerCase()));
			final int oldAdd=affectableStats.weight()-affected.basePhyStats().weight();
			newRace.setHeightWeight(affectableStats,'M');
			if(oldAdd>0)
				affectableStats.setWeight(affectableStats.weight()+oldAdd);
			final int xlvl=getXLEVELLevel(invoker());
			final double bonus=CMath.mul(0.1,xlvl);
			switch(raceLevel)
			{
			case 0:
				affectableStats.setArmor(affectableStats.armor()-10-(xlvl));
				affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+10+(xlvl));
				affectableStats.setDamage(affectableStats.damage()+5+(xlvl/2));
				affectableStats.setSpeed(affectableStats.speed()/(1.5-bonus));
				break;
			case 1:
				affectableStats.setArmor(affectableStats.armor()-20-(2*xlvl));
				affectableStats.setSpeed(affectableStats.speed()/(2.0-bonus));
				affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+20+(2*xlvl));
				affectableStats.setDamage(affectableStats.damage()+10+(xlvl));
				break;
			case 2:
				affectableStats.setArmor(affectableStats.armor()-40-(4*xlvl));
				affectableStats.setSpeed(affectableStats.speed()/(2.5-bonus));
				affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+40+(4*xlvl));
				affectableStats.setDamage(affectableStats.damage()+20+(xlvl*2));
				break;
			case 3:
				affectableStats.setArmor(affectableStats.armor()-60-(6*xlvl));
				affectableStats.setSpeed(affectableStats.speed()/(3.0-bonus));
				affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+80+(8*xlvl));
				affectableStats.setDamage(affectableStats.damage()+40+(xlvl*4));
				break;
			case 4:
				affectableStats.setArmor(affectableStats.armor()-80-(8*xlvl));
				affectableStats.setSpeed(affectableStats.speed()/(4.0-bonus));
				affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+160+(16*xlvl));
				affectableStats.setDamage(affectableStats.damage()+80+(xlvl*8));
				break;
			}
		}
	}

	@Override
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(newRace!=null)
		{
			affectableStats.setMyRace(newRace);
			affectableStats.setWearableRestrictionsBitmap(affectableStats.getWearableRestrictionsBitmap()|affectableStats.getMyRace().forbiddenWornBits());
		}
	}

	@Override
	public void affectCharState(MOB affected, CharState affectableState)
	{
		super.affectCharState(affected,affectableState);
		switch(raceLevel)
		{
		case 0:
			affectableState.setMovement(affectableState.getMovement()/1);
			break;
		case 1:
			affectableState.setMovement(affectableState.getMovement()/2);
			break;
		case 2:
			affectableState.setMovement(affectableState.getMovement()/4);
			break;
		case 3:
			affectableState.setMovement(affectableState.getMovement()/8);
			break;
		case 4:
			affectableState.setMovement(affectableState.getMovement()/16);
			break;
		}
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		super.unInvoke();
		if((canBeUninvoked())&&(mob.location()!=null))
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> revert(s) to @x1 form.",mob.charStats().raceName().toLowerCase()));
	}

	public void setRaceName(MOB mob)
	{
		final int qualClassLevel=CMLib.ableMapper().qualifyingClassLevel(mob,this)+(2*getXLEVELLevel(mob));
		int classLevel=qualClassLevel-CMLib.ableMapper().qualifyingLevel(mob,this);
		if(qualClassLevel<0)
			classLevel=30;
		raceName=getRaceName(classLevel);
		newRace=getRace(classLevel);
	}

	public int getRaceLevel(int classLevel)
	{
		if(classLevel<5)
			return 0;
		else
		if(classLevel<10)
			return 1;
		else
		if(classLevel<15)
			return 2;
		else
		if(classLevel<25)
			return 3;
		else
			return 4;
	}

	public Race getRace(int classLevel)
	{
		return CMClass.getRace(races[getRaceLevel(classLevel)]);
	}

	public String getRaceName(int classLevel)
	{
		return shapes[getRaceLevel(classLevel)];
	}

	public static boolean isShapeShifted(MOB mob)
	{
		if(mob==null)
			return false;
		for(final Enumeration<Ability> a=mob.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)&&(A instanceof Druid_GolemForm))
				return true;
		}
		return false;
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(target instanceof MOB)
			{
				if((((MOB)target).isInCombat())
				&&(!Druid_ShapeShift.isShapeShifted((MOB)target)))
					return super.castingQuality(mob, target,Ability.QUALITY_BENEFICIAL_SELF);
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		for(final Enumeration<Ability> a=mob.personalEffects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)&&(A instanceof Druid_GolemForm))
			{
				A.unInvoke();
				return true;
			}
		}

		final int qualClassLevel=CMLib.ableMapper().qualifyingClassLevel(mob,this)+(2*getXLEVELLevel(mob));
		int classLevel=qualClassLevel-CMLib.ableMapper().qualifyingLevel(mob,this);
		if(qualClassLevel<0)
			classLevel=30;
		final String choice=(mob.isMonster()||(commands.size()==0))?getRaceName(classLevel-1):CMParms.combine(commands,0);
		if(choice.trim().length()>0)
		{
			final StringBuffer buf=new StringBuffer(L("Golem Forms:\n\r"));
			final Vector<String> choices=new Vector<String>();
			for(int i=0;i<classLevel;i++)
			{
				final String s=getRaceName(i);
				if(!choices.contains(s))
				{
					choices.addElement(s);
					buf.append(s+"\n\r");
				}
				if(CMLib.english().containsString(s,choice))
				{
					classLevel=i;
					break;
				}
			}
			if(choice.equalsIgnoreCase("list"))
			{
				mob.tell(buf.toString());
				return true;
			}
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if((!appropriateToMyFactions(mob))&&(!auto))
		{
			if((CMLib.dice().rollPercentage()<50))
			{
				mob.tell(L("Extreme emotions disrupt your change."));
				return false;
			}
		}

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_OK_ACTION,null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				raceName=getRaceName(classLevel);
				raceName=CMStrings.capitalizeAndLower(CMLib.english().startWithAorAn(raceName.toLowerCase()));
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> take(s) on @x1 form.",raceName.toLowerCase()));
				newRace=getRace(classLevel);
				raceLevel=getRaceLevel(classLevel);
				beneficialAffect(mob,mob,asLevel,Ability.TICKS_FOREVER);
				CMLib.utensils().confirmWearability(mob);
			}
		}
		else
			beneficialWordsFizzle(mob,null,L("<S-NAME> chant(s) to <S-HIM-HERSELF>, but nothing happens."));

		// return whether it worked
		return success;
	}
}
