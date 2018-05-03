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
   Copyright 2014-2018 Bo Zimmerman

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

public class Spell_DisguiseSelf extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_DisguiseSelf";
	}

	private final static String localizedName = CMLib.lang().L("Disguise Self");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Disguise Self)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_ILLUSION;
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
		return Ability.QUALITY_OK_OTHERS;
	}

	private String raceName="Human";
	private String newName="Bob";
	private String className="Fighter";
	private String genderName="M";
	
	@Override
	public void setMiscText(String text)
	{
		super.setMiscText(text);
		raceName=CMParms.getParmStr(text,"RACE","");
		newName=CMParms.getParmStr(text,"NAME","");
		className=CMParms.getParmStr(text,"CLASS","");
		genderName=CMParms.getParmStr(text,"GENDER","");
	}
	
	@Override
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(affected==null)
			return;
		if(raceName.length()>0)
			affectableStats.setRaceName(raceName);
		if(className.length()>0)
			affectableStats.setDisplayClassName(className);
		if(genderName.length()>0)
			affectableStats.setGenderName(genderName);
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected==null)
			return;
		if(newName.length()>0)
			affectableStats.setName(newName);
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-YOUPOSS> disguise fades."));
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(mob.isInCombat())
				return Ability.QUALITY_INDIFFERENT;
			if(mob.isMonster())
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=(givenTarget instanceof MOB) ? (MOB)givenTarget : mob;
		
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(mob,target,null,L("<T-NAME> <T-IS-ARE> already in disguise!"));
			return false;
		}
		
		if(commands.size()<1)
		{
			mob.tell(mob,target,null,L("Disguise <T-NAMESELF> as whom?"));
			return false;
		}
		
		String whomName=CMParms.combine(commands);
		
		MOB targetM = CMLib.map().findFirstInhabitant(new XVector<Room>(mob.location()).elements(), mob, whomName, 5);
		if(targetM == null)
			targetM=CMLib.map().findFirstInhabitant(mob.location().getArea().getCompleteMap(), mob, whomName, 5);
		if(targetM == null)
			targetM=CMLib.map().findFirstInhabitant(CMLib.map().rooms(), mob, whomName, 5);
		if(targetM == null)
		{
			mob.tell(L("You can't seem to picture '@x1' in your mind.  Perhaps if you saw them again?",whomName));
			return false;
		}
		if(targetM.isPlayer())
		{
			mob.tell(mob,target,null,L("You can't disguise <T-NAMESELF> as  @x1?",targetM.name()));
			return false;
		}
		
		if((targetM.phyStats().level() > mob.phyStats().level()))
		{
			mob.tell(mob,target,null,L("You aren't powerful enough to disguise <T-NAMESELF> as @x1?",targetM.name()));
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),L(auto?"<T-NAME> gain(s) a disguise!":"^S<S-NAME> casts a spell on <T-NAMESELF>, causing <T-HIS-HER> appearance to change.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Ability A=beneficialAffect(mob,target,asLevel,adjustedLevel(mob,asLevel));
				if(A!=null)
					A.setMiscText("RACE=\""+targetM.charStats().raceName()+"\" CLASS=\""+targetM.charStats().displayClassName()+"\" NAME=\""+targetM.name()+"\" GENDER="+targetM.charStats().genderName());
				target.recoverCharStats();
				target.recoverPhyStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> cast(s) a spell for <T-NAMESELF>, but nothing happens."));

		// return whether it worked
		return success;
	}
}
