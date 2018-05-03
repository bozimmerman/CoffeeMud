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

public class Spell_Simulacrum extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_Simulacrum";
	}

	private final static String localizedName = CMLib.lang().L("Simulacrum");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Simulacrum)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ITEMS;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_ITEMS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_ILLUSION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	protected volatile boolean norecurse=false;
	
	@Override
	public void unInvoke()
	{
		final MOB mob=(MOB)affected;
		final MOB invoker=invoker();
		super.unInvoke();
		if(canBeUninvoked())
		{
			if((mob!=null)&&(mob.playerStats()==null))
			{
				if(mob.amDead())
					mob.setLocation(null);
				mob.destroy();
			}
			if(invoker != null)
			{
				invoker.delEffect(this);
				invoker.tell(L("Your simulacrum has vanished."));
			}
		}
	}
	
	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if((affected==null)||(invoker()==null)||(affected==invoker()))
			return;
		affectableStats.setName(invoker().Name());
		affectableStats.setWeight(0);
		affectableStats.setHeight(-1);
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_GOLEM);
	}

	@Override
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if((affected==null)||(invoker()==null)||(affected==invoker()))
			return;
		affectableStats.setRaceName(invoker().charStats().raceName());
		affectableStats.setDisplayClassName(invoker().charStats().displayClassName());
		affectableStats.setGenderName(invoker().charStats().genderName());
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if((affected instanceof MOB)&&(affected != invoker()))
		{
			final MOB simulacruM=(MOB)affected;
			final MOB casterM=invoker();
			if(msg.amISource(simulacruM))
			{
				switch(msg.targetMinor())
				{
				case CMMsg.TYP_DAMAGE:
					msg.setValue(0);
					simulacruM.makePeace(true);
					break;
				case CMMsg.TYP_GET:
				case CMMsg.TYP_PUSH:
				case CMMsg.TYP_PULL:
					return false;
				}
				if(msg.sourceMajor(CMMsg.MASK_HANDS)&&(msg.target() instanceof Physical))
					return false;
			}
			else
			if(msg.amITarget(simulacruM))
			{
				switch(msg.targetMinor())
				{
				case CMMsg.TYP_DAMAGE:
					msg.setValue(0);
					simulacruM.makePeace(true);
					break;
				case CMMsg.TYP_LOOK:
				case CMMsg.TYP_EXAMINE:
					msg.setTarget(casterM);
					invoker().executeMsg(casterM, msg);
					return false;
				case CMMsg.TYP_CAST_SPELL:
					if(msg.tool() instanceof Ability)
						msg.source().location().show(msg.source(),affected,CMMsg.MSG_OK_VISUAL,L("The spell <S-NAME> cast(s) at <T-NAME> goes right through <T-HIM-HER>!"));
					return false;
				}
			}
		}
		return true;
	}
	
	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected instanceof MOB)&&(affected != invoker()))
		{
			final MOB simulacruM=(MOB)affected;
			final MOB casterM=invoker();
			if((msg.amISource(simulacruM)||msg.amISource(casterM))
			&&(msg.sourceMinor()==CMMsg.TYP_QUIT))
			{
				unInvoke();
				if(msg.source().playerStats()!=null)
					msg.source().playerStats().setLastUpdated(0);
				return;
			}
			if((simulacruM!=null)&&(casterM!=null))
			{
				if((msg.targetMinor()==CMMsg.TYP_SPEAK)
				&&(msg.source()==casterM)
				&&(myHost==casterM)
				&&(msg.sourceMessage()!=null))
				{
					synchronized(this)
					{
						try
						{
							if(!norecurse)
							{
								norecurse=true;
								final String say = CMStrings.getSayFromMessage(msg.sourceMessage());
								if(say != null)
									simulacruM.doCommand(CMParms.parse(say), MUDCmdProcessor.METAFLAG_ORDER);
							}
						}
						finally
						{
							norecurse=false;
						}
					}
				}
				/*
				else
				if((msg.othersCode()!=CMMsg.NO_EFFECT)
				&&(msg.othersMessage()!=null)
				&&(msg.othersMessage().length()>0)
				&&(myHost == simulacruM)
				&&(simulacruM.location() != casterM.location()))
					casterM.tell(L("^hSimulacrum^N: ^W")+CMLib.coffeeFilter().fullOutFilter(null, null, msg.source(), msg.target(), msg.tool(), CMStrings.removeColors(msg.othersMessage()), false)+"^.^N");
				*/
			}
		}
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Physical target=(givenTarget instanceof MOB) ? givenTarget : mob;
		if(target==null)
			return false;

		if(!(target instanceof MOB))
		{
			mob.tell(L("You can't cast this spell on that."));
			return false;
		}

		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(L("@x1 is already a simulacrum!",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,somanticCastCode(mob,target,auto),auto?"":L("^S<S-NAME> point(s) <S-HIS-HER> finger at <S-NAMESELF>, incanting.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Spell_Simulacrum A = (Spell_Simulacrum)beneficialAffect(mob,target,asLevel,0);
				if(A!=null)
				{
					MOB M=determineMonster(mob,mob.phyStats().level(),A);
					M.setFollowing(mob);
					mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,L("A simulacrum of <S-NAME> appears, while <S-NAME> open(s) <S-HIS-HER> transluscent eyes."));
					mob.tell(L("^xYour simulacrum will now do anything you say, even if they are in another room."));
				}
			}
		}
		else
			beneficialVisualFizzle(mob,target,L("<S-NAME> point(s) at <T-NAMESELF>, incanting, but nothing happens."));
		// return whether it worked
		return success;
	}
	
	public MOB determineMonster(MOB caster, int level, Spell_Simulacrum A)
	{

		final MOB newMOB=CMClass.getMOB("GenMob");
		newMOB.basePhyStats().setAbility(CMProps.getMobHPBase());
		newMOB.basePhyStats().setDisposition(newMOB.basePhyStats().disposition()|PhyStats.IS_FLYING);
		newMOB.basePhyStats().setLevel(caster.basePhyStats().level());
		newMOB.basePhyStats().setWeight(caster.basePhyStats().weight());
		newMOB.basePhyStats().setRejuv(PhyStats.NO_REJUV);
		newMOB.baseCharStats().setMyRace(CMClass.getRace("Spirit"));
		newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'N');
		newMOB.setSavable(false);
		newMOB.baseCharStats().getMyRace().startRacing(newMOB,false);
		newMOB.recoverPhyStats();
		newMOB.recoverCharStats();
		newMOB.basePhyStats().setSpeed(caster.basePhyStats().speed());
		newMOB.setName(L("an simulacrum of @x1",caster.Name()));
		newMOB.setDisplayText(L("@x1 is here.",caster.Name()));
		newMOB.setDescription(caster.description());
		newMOB.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience"));
		newMOB.recoverCharStats();
		newMOB.recoverPhyStats();
		newMOB.recoverMaxState();
		CMLib.factions().setAlignment(newMOB,Faction.Align.NEUTRAL);
		newMOB.resetToMaxState();
		newMOB.text();
		newMOB.bringToLife(caster.location(),true);
		newMOB.addEffect(A);
		A.setAffectedOne(newMOB);
		newMOB.recoverCharStats();
		newMOB.recoverPhyStats();
		newMOB.recoverMaxState();
		CMLib.beanCounter().clearZeroMoney(newMOB,null);
		newMOB.setMoneyVariation(0);
		newMOB.location().showOthers(newMOB,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> appears!"));
		caster.location().recoverRoomStats();
		newMOB.setStartRoom(null);
		return(newMOB);
	}

}
