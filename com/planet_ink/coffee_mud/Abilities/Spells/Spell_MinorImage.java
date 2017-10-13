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
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2017-2017 Bo Zimmerman

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
public class Spell_MinorImage extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_MinorImage";
	}

	private final static String	localizedName	= CMLib.lang().L("Minor Image");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL | Ability.DOMAIN_ILLUSION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	protected int getDuration(final MOB caster, final int asLevel)
	{
		return 10 + (super.adjustedLevel(caster, asLevel) /6);
	}
	
	protected boolean canSeeAppearance()
	{
		return false;
	}
	
	protected boolean canTargetOthers()
	{
		return false;
	}
	
	protected volatile MOB parentM = null;
	
	@Override
	public void setMiscText(String newMiscText)
	{
		super.setMiscText(newMiscText);
		if((newMiscText!=null)&&(newMiscText.length()>0))
		{
			if(CMLib.players().playerExists(newMiscText))
				parentM=CMLib.players().getLoadPlayer(newMiscText);
		}
	}
	
	@Override
	public void unInvoke()
	{
		final MOB mob=(MOB)affected;
		super.unInvoke();
		if(canBeUninvoked())
		{
			if((mob!=null)&&(mob.playerStats()==null))
			{
				final Room R=mob.location();
				if(R!=null)
					R.show(mob,null,null,CMMsg.MSG_OK_VISUAL, L("<S-NAME> vanish(es)!"));
				if(mob.amDead())
					mob.setLocation(null);
				mob.destroy();
			}
		}
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if((affected==null)||(parentM==null)||(affected==parentM))
			return;
		affectableStats.setName(parentM.Name());
		affectableStats.setWeight(0);
		affectableStats.setHeight(-1);
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_GOLEM);
	}

	@Override
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if((affected==null)||(parentM==null)||(affected==parentM))
			return;
		affectableStats.setRaceName(parentM.charStats().raceName());
		affectableStats.setDisplayClassName(parentM.charStats().displayClassName());
		affectableStats.setGenderName(parentM.charStats().genderName());
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if((affected instanceof MOB)&&(affected != parentM))
		{
			final MOB simulacruM=(MOB)affected;
			if(msg.amISource(simulacruM) && (!CMath.bset(msg.sourceMajor(), CMMsg.MASK_ALWAYS)))
			{
				msg.source().tell("You can't do anything, you're just a stationary illusion!");
				return false;
			}
			else
			if(msg.amITarget(simulacruM))
			{
				if(CMath.bset(msg.targetMajor(), CMMsg.MASK_MALICIOUS))
					unInvoke();
				else
				switch(msg.targetMinor())
				{
				case CMMsg.TYP_DAMAGE:
				case CMMsg.TYP_WEAPONATTACK:
				case CMMsg.TYP_CAST_SPELL:
					unInvoke();
					break;
				case CMMsg.TYP_LOOK:
				case CMMsg.TYP_EXAMINE:
					if(canSeeAppearance())
					{
						msg.setTarget(parentM);
						parentM.executeMsg(parentM, msg);
						return false;
					}
					break;
				}
			}
		}
		return true;
	}
	
	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target;
		if(canTargetOthers())
		{
			target=super.getTarget(mob, commands, givenTarget);
			if(target==null)
				return false;
		}
		else
		if(givenTarget instanceof MOB)
			target=(MOB)givenTarget;
		else
			target=mob;
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,somanticCastCode(mob,target,auto),auto?"":L("^S<S-NAME> wave(s) <S-HIS-HER> arms around <T-NAMESELF>, incanting.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				MOB M=determineMonster(target,target.location(),target.phyStats().level());
				Spell_MinorImage A = (Spell_MinorImage)beneficialAffect(mob,M,asLevel,getDuration(mob,asLevel));
				if(A!=null)
				{
					A.setMiscText(target.Name());
					A.parentM=target;
					M.setFollowing(mob);
					mob.location().show(target,M,CMMsg.MSG_OK_VISUAL,L("An image of <S-NAME> appears!"));
				}
			}
		}
		else
			beneficialVisualFizzle(mob,target,L("<S-NAME> wave(s) <S-HIS-HER> arms around <T-NAMESELF>, but nothing happens."));
		// return whether it worked
		return success;
	}
	
	public MOB determineMonster(MOB target, Room R, int level)
	{

		final MOB newMOB=CMClass.getMOB("GenMob");
		newMOB.basePhyStats().setAbility(CMProps.getMobHPBase());
		newMOB.basePhyStats().setLevel(target.basePhyStats().level());
		newMOB.basePhyStats().setWeight(target.basePhyStats().weight());
		newMOB.basePhyStats().setRejuv(PhyStats.NO_REJUV);
		newMOB.baseCharStats().setMyRace(CMClass.getRace("Spirit"));
		newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'N');
		newMOB.setSavable(false);
		newMOB.baseCharStats().getMyRace().startRacing(newMOB,false);
		newMOB.recoverPhyStats();
		newMOB.recoverCharStats();
		newMOB.basePhyStats().setSpeed(target.basePhyStats().speed());
		newMOB.setName(L("an image of @x1",target.Name()));
		newMOB.setDisplayText(L("@x1 is here.",target.Name()));
		newMOB.setDescription(target.description());
		newMOB.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience"));
		newMOB.recoverCharStats();
		newMOB.recoverPhyStats();
		newMOB.recoverMaxState();
		CMLib.factions().setAlignment(newMOB,Faction.Align.NEUTRAL);
		newMOB.resetToMaxState();
		newMOB.text();
		newMOB.bringToLife(target.location(),true);
		newMOB.recoverCharStats();
		newMOB.recoverPhyStats();
		newMOB.recoverMaxState();
		CMLib.beanCounter().clearZeroMoney(newMOB,null);
		newMOB.setMoneyVariation(0);
		R.recoverRoomStats();
		newMOB.setStartRoom(null);
		return(newMOB);
	}

}
