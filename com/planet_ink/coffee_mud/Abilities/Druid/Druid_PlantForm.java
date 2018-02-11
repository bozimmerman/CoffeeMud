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
   Copyright 2003-2018 Bo Zimmerman

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

public class Druid_PlantForm extends StdAbility
{
	@Override
	public String ID()
	{
		return "Druid_PlantForm";
	}

	private final static String localizedName = CMLib.lang().L("Plant Form");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_SHAPE_SHIFTING;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	private static final String[] triggerStrings =I(new String[] {"PLANTFORM"});
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

	public Race newRace=null;
	public String raceName="";

	@Override
	public String displayText()
	{
		if(newRace==null)
		{
			unInvoke();
			return "";
		}
		return "(in "+newRace.name().toLowerCase()+" form)";
	}

	private static String[] shapes={
	"Flower",
	"Vine",
	"Tumbleweed",
	"Shambler"
	};
	private static String[] races={
	"Flower",
	"Vine",
	"Tumbleweed",
	"Shambler"
	};

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(((msg.targetMajor()&CMMsg.MASK_MALICIOUS)>0)
		&&(!CMath.bset(msg.sourceMajor(),CMMsg.MASK_ALWAYS))
		&&((msg.amITarget(affected))))
		{
			final MOB target=(MOB)msg.target();
			if((!target.isInCombat())
			&&(msg.source().isMonster())
			&&(msg.source().location()==target.location())
			&&(msg.source().getVictim()!=target))
			{
				msg.source().tell(L("Attack a plant?!"));
				if(target.getVictim()==msg.source())
				{
					target.makePeace(true);
					target.setVictim(null);
				}
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if((newRace!=null)&&(affected instanceof MOB))
		{
			affectableStats.setName(CMLib.english().startWithAorAn(raceName.toLowerCase()));
			final int oldAdd=affectableStats.weight()-affected.basePhyStats().weight();
			newRace.setHeightWeight(affectableStats,'M');
			if(oldAdd>0)
				affectableStats.setWeight(affectableStats.weight()+oldAdd);
		}
	}

	@Override
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(newRace!=null)
		{
			final int oldCat=affected.baseCharStats().ageCategory();
			affectableStats.setMyRace(newRace);
			affectableStats.setWearableRestrictionsBitmap(affectableStats.getWearableRestrictionsBitmap()|affectableStats.getMyRace().forbiddenWornBits());
			if(affected.baseCharStats().getStat(CharStats.STAT_AGE)>0)
				affectableStats.setStat(CharStats.STAT_AGE,newRace.getAgingChart()[oldCat]);
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
		final int classLevel=CMLib.ableMapper().qualifyingClassLevel(mob,this)+(2*getXLEVELLevel(mob))
							-CMLib.ableMapper().qualifyingLevel(mob,this);
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
			return 3;
	}

	public Race getRace(int classLevel)
	{
		return CMClass.getRace(races[getRaceLevel(classLevel)]);
	}

	public String getRaceName(int classLevel)
	{
		return shapes[getRaceLevel(classLevel)];
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			final Room R=mob.location();
			if(R!=null)
			{
				if((R.domainType()&Room.INDOORS)>0)
					return Ability.QUALITY_INDIFFERENT;
				if((R.domainType()==Room.DOMAIN_OUTDOORS_CITY)
				||(R.domainType()==Room.DOMAIN_OUTDOORS_SPACEPORT))
					return Ability.QUALITY_INDIFFERENT;
			}
			if(target instanceof MOB)
			{
				if((((MOB)target).isInCombat())
				&&(!Druid_ShapeShift.isShapeShifted((MOB)target)))
				{
					final int qualClassLevel=CMLib.ableMapper().qualifyingClassLevel(mob,this)+(2*getXLEVELLevel(mob));
					int classLevel=qualClassLevel-CMLib.ableMapper().qualifyingLevel(mob,this);
					if(qualClassLevel<0)
						classLevel=30;
					if(getRaceLevel(classLevel)==3)
						return super.castingQuality(mob, target,Ability.QUALITY_BENEFICIAL_SELF);
				}
			}
		}
		return super.castingQuality(mob,target);
	}

	public static boolean isShapeShifted(MOB mob)
	{
		if(mob==null)
			return false;
		for(final Enumeration<Ability> a=mob.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)&&(A instanceof Druid_PlantForm))
				return true;
		}
		return false;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		for(final Enumeration<Ability> a=mob.personalEffects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)&&(A instanceof Druid_PlantForm))
			{
				A.unInvoke();
				return true;
			}
		}

		if((mob.location().domainType()&Room.INDOORS)>0)
		{
			mob.tell(L("You must be outdoors to take on your plant form."));
			return false;
		}
		if((mob.location().domainType()==Room.DOMAIN_OUTDOORS_CITY)
		||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_SPACEPORT))
		{
			mob.tell(L("You must be in the wild to take on your plant form."));
			return false;
		}

		final int qualClassLevel=CMLib.ableMapper().qualifyingClassLevel(mob,this)+(2*getXLEVELLevel(mob));
		int classLevel=qualClassLevel-CMLib.ableMapper().qualifyingLevel(mob,this);
		if(qualClassLevel<0)
			classLevel=30;
		final String choice=(mob.isMonster()||(commands.size()==0))?getRaceName(classLevel-1):CMParms.combine(commands,0);
		if(choice.trim().length()>0)
		{
			final StringBuffer buf=new StringBuffer(L("Plant Forms:\n\r"));
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
		raceName=getRaceName(classLevel);
		newRace=getRace(classLevel);

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
				beneficialAffect(mob,mob,asLevel,Ability.TICKS_FOREVER);
				raceName=CMStrings.capitalizeAndLower(CMLib.english().startWithAorAn(raceName.toLowerCase()));
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> take(s) on @x1 form.",raceName.toLowerCase()));
				CMLib.utensils().confirmWearability(mob);
			}
		}
		else
			beneficialWordsFizzle(mob,null,L("<S-NAME> chant(s) to <S-HIM-HERSELF>, but nothing happens."));

		// return whether it worked
		return success;
	}
}
