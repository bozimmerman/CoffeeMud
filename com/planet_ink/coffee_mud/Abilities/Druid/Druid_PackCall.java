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

public class Druid_PackCall extends StdAbility
{
	@Override
	public String ID()
	{
		return "Druid_PackCall";
	}

	private final static String localizedName = CMLib.lang().L("Pack Call");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Pack Call)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	private static final String[] triggerStrings =I(new String[] {"PACKCALL"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_SELF;
	}

	@Override
	public int enchantQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_ANIMALAFFINITY;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Tickable.TICKID_MOB)
		{
			if((affected!=null)
			&&(affected instanceof MOB)
			&&(invoker!=null))
			{
				final MOB mob=(MOB)affected;
				if(((mob.amFollowing()==null)
				||(mob.amDead())
				||(!mob.isInCombat())
				||(mob.location()!=invoker.location())))
					unInvoke();
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public void unInvoke()
	{
		final MOB mob=(MOB)affected;
		super.unInvoke();
		if((canBeUninvoked())
		&&(mob!=null)
		&&(!mob.amDestroyed()))
		{
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> wander(s) off."));
			if(mob.amDead())
				mob.setLocation(null);
			mob.destroy();
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected)||msg.amISource(((MOB)affected).amFollowing())||(msg.source()==invoker()))
		&&(msg.sourceMinor()==CMMsg.TYP_QUIT))
		{
			unInvoke();
			if(msg.source().playerStats()!=null)
				msg.source().playerStats().setLastUpdated(0);
		}
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(!CMLib.flags().isInWilderness(mob))
				return Ability.QUALITY_INDIFFERENT;
			if(target instanceof MOB)
			{
				if(!(((MOB)target).isInCombat()))
					return Ability.QUALITY_INDIFFERENT;

				if(!Druid_ShapeShift.isShapeShifted((MOB)target))
					return Ability.QUALITY_INDIFFERENT;

				if(((MOB)target).totalFollowers()>=((MOB)target).maxFollowers())
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((!CMLib.flags().isInWilderness(mob)))
		{
			mob.tell(L("You must be outdoors to call your pack."));
			return false;
		}
		if((mob.location().domainType()==Room.DOMAIN_OUTDOORS_CITY)
		||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_SPACEPORT))
		{
			mob.tell(L("You must be in the wild to call your pack."));
			return false;
		}
		if(!mob.isInCombat())
		{
			mob.tell(L("Only the anger of combat can call your pack."));
			return false;
		}
		Druid_ShapeShift D=null;
		for(final Enumeration<Ability> a=mob.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)&&(A instanceof Druid_ShapeShift))
				D=(Druid_ShapeShift)A;
		}
		if(D==null)
		{
			mob.tell(L("You must be in your animal form to call the pack."));
			return false;
		}

		if(mob.totalFollowers()>=mob.maxFollowers())
		{
			mob.tell(L("You can't have any more followers!"));
			return false;
		}

		final Vector<Integer> choices=new Vector<Integer>();
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			final Room R=mob.location().getRoomInDir(d);
			final Exit E=mob.location().getExitInDir(d);
			if((R!=null)&&(E!=null)&&(E.isOpen())&&(d!=Directions.UP))
				choices.addElement(Integer.valueOf(d));
		}

		if(choices.size()==0)
		{
			mob.tell(L("Your call would not be heard here."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_NOISE,auto?"":L("^S<S-NAME> call(s) for help from <S-HIS-HER> pack!^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				int levelsRemaining=90+(10*getXLEVELLevel(mob));
				while((mob.totalFollowers()<mob.maxFollowers())&&(levelsRemaining>0))
				{
					final MOB victim=mob.getVictim();
					final MOB newMOB=CMClass.getMOB("GenMOB");
					final int MOBRaceCode=D.myRaceCode;
					if(D.raceName==null)
						D.setRaceName(mob);
					int level=1;
					while(!D.raceName.equals(D.getRaceName(level,MOBRaceCode)))
						level++;
					level--;
					newMOB.basePhyStats().setLevel(level);
					levelsRemaining-=level;
					if(levelsRemaining<0)
						break;
					newMOB.baseCharStats().setMyRace(D.getRace(level,MOBRaceCode));
					final String raceName=D.getRaceName(level,MOBRaceCode).toLowerCase();
					final String name=CMLib.english().startWithAorAn(raceName).toLowerCase();
					newMOB.setName(name);
					newMOB.setDisplayText(L("a loyal @x1 is here",raceName));
					newMOB.setDescription("");
					newMOB.copyFactions(mob);
					final Ability A=CMClass.getAbility("Fighter_Rescue");
					A.setProficiency(100);
					newMOB.addAbility(A);
					newMOB.setVictim(victim);
					newMOB.setLocation(mob.location());
					newMOB.basePhyStats().setRejuv(PhyStats.NO_REJUV);
					newMOB.recoverPhyStats();
					newMOB.basePhyStats().setAbility(newMOB.basePhyStats().ability()*2);
					newMOB.basePhyStats().setArmor(CMLib.leveler().getLevelMOBArmor(newMOB));
					newMOB.basePhyStats().setAttackAdjustment(CMLib.leveler().getLevelAttack(newMOB));
					newMOB.basePhyStats().setDamage(CMLib.leveler().getLevelMOBDamage(newMOB));
					newMOB.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience"));
					newMOB.setMiscText(newMOB.text());
					newMOB.recoverPhyStats();
					newMOB.recoverCharStats();
					newMOB.recoverMaxState();
					newMOB.resetToMaxState();
					newMOB.bringToLife(mob.location(),true);
					CMLib.beanCounter().clearZeroMoney(newMOB,null);
					newMOB.setMoneyVariation(0);
					if(victim.getVictim()!=newMOB)
						victim.setVictim(newMOB);
					final int dir=choices.elementAt(CMLib.dice().roll(1,choices.size(),-1)).intValue();
					if(newMOB.getVictim()!=victim)
						newMOB.setVictim(victim);
					newMOB.location().showOthers(newMOB,victim,CMMsg.MSG_OK_ACTION,L("<S-NAME> arrive(s) @x1 and attack(s) <T-NAMESELF>!",CMLib.directions().getFromCompassDirectionName(dir)));
					newMOB.setStartRoom(null); // keep before postFollow for Conquest
					CMLib.commands().postFollow(newMOB,mob,true);
					if(newMOB.amFollowing()!=mob)
					{
						newMOB.destroy();
						break;
					}
					beneficialAffect(mob,newMOB,asLevel,0);
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,null,L("<S-NAME> call(s) for help from <S-HIS-HER> pack, but nothing happens."));

		// return whether it worked
		return success;
	}
}
