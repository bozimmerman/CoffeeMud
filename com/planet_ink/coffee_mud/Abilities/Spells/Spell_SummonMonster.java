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
   Copyright 2001-2018 Bo Zimmerman

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

public class Spell_SummonMonster extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_SummonMonster";
	}

	private final static String localizedName = CMLib.lang().L("Monster Summoning");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Monster Summoning)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
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
	public int classificationCode()
	{
		return Ability.ACODE_SPELL | Ability.DOMAIN_CONJURATION;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_SUMMONING;
	}

	@Override
	public void unInvoke()
	{
		final MOB mob=(MOB)affected;
		super.unInvoke();
		if((canBeUninvoked())&&(mob!=null))
		{
			if(mob.amDead())
				mob.setLocation(null);
			else
			if(mob.location()!=null)
				mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> vanish(es)!"));
			mob.destroy();
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected instanceof MOB)
		&&(msg.amISource((MOB)affected)||msg.amISource(((MOB)affected).amFollowing())||(msg.source()==invoker()))
		&&(msg.sourceMinor()==CMMsg.TYP_QUIT))
		{
			unInvoke();
			if(msg.source().playerStats()!=null)
				msg.source().playerStats().setLastUpdated(0);
		}
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		final Room R=mob.location();
		if(success)
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":L("^S<S-NAME> summon(s) help from the Java Plane....^?"));
			if(R.okMessage(mob,msg))
			{
				R.send(mob,msg);
				final MOB monster = determineMonster(mob, mob.phyStats().level()+((getX1Level(mob)+getXLEVELLevel(mob))/2));
				if(monster!=null)
					beneficialAffect(mob,monster,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,L("<S-NAME> call(s) for magical help, but choke(s) on the words."));

		// return whether it worked
		return success;
	}

	public void bringToLife(MOB M)
	{

	}

	public MOB determineMonster(MOB caster, int level)
	{
		final Room R=caster.location();
		if(R==null)
			return null;
		MOB newMOB=null;
		final Vector<MOB> choices=new Vector<MOB>();
		MOB M=null;
		int range=0;
		int diff=2;
		if(level>=100)
			diff=20;
		else
		if(level>=80)
			diff=17;
		else
		if(level>=60)
			diff=15;
		else
		if(level>=40)
			diff=10;
		else
		if(level>=20)
			diff=7;
		else
		if(level>=10)
			diff=5;
		while((choices.size()==0)&&(range<100))
		{
			range+=diff;
			for(final Enumeration<MOB> e=CMClass.mobTypes();e.hasMoreElements();)
			{
				M=(MOB)e.nextElement().newInstance();
				if((M.basePhyStats().level()<level-range)
				||(M.basePhyStats().level()>level+range)
				||(M.isGeneric())
				||(!CMLib.flags().isEvil(M))
				||(!M.baseCharStats().getMyRace().canBreedWith(M.baseCharStats().getMyRace()))
				||CMLib.flags().isGolem(M))
				{
					M.destroy();
					try
					{
						Thread.sleep(1);
					}
					catch (final Exception e1)
					{
					}
					continue;
				}
				choices.addElement(M);
			}
		}
		if(choices.size()>0)
		{
			MOB winM=choices.firstElement();
			for(int i=1;i<choices.size();i++)
			{
				M=choices.elementAt(i);
				if(CMath.pow(level-M.basePhyStats().level(),2)<CMath.pow(level-winM.basePhyStats().level(),2))
					winM=M;
			}
			newMOB=winM;
		}
		else
		{
			newMOB=CMClass.getMOB("GenMOB");
			newMOB.basePhyStats().setLevel(level);
			newMOB.charStats().setMyRace(CMClass.getRace("Unique"));
			newMOB.setName(L("a wierd extra-planar monster"));
			newMOB.setDisplayText(L("a wierd extra-planar monster stands here"));
			newMOB.setDescription(L("It's too difficult to describe what this thing looks like, but he/she/it is definitely angry!"));
			CMLib.factions().setAlignment(newMOB,Faction.Align.NEUTRAL);
			newMOB.baseCharStats().getMyRace().startRacing(newMOB,false);
			newMOB.baseState().setHitPoints(CMLib.dice().rollHP(level, 20));
			newMOB.recoverMaxState();
			newMOB.resetToMaxState();
			newMOB.recoverPhyStats();
			newMOB.recoverCharStats();
			CMLib.leveler().fillOutMOB(newMOB,level);
			newMOB.recoverMaxState();
			newMOB.resetToMaxState();
			newMOB.recoverPhyStats();
			newMOB.recoverCharStats();
		}
		newMOB.setMoney(0);
		newMOB.setMoneyVariation(0);
		newMOB.setLocation(R);
		newMOB.basePhyStats().setRejuv(PhyStats.NO_REJUV);
		newMOB.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience"));
		newMOB.recoverCharStats();
		newMOB.recoverPhyStats();
		newMOB.recoverMaxState();
		newMOB.resetToMaxState();
		newMOB.bringToLife(R,true);
		CMLib.beanCounter().clearZeroMoney(newMOB,null);
		newMOB.setMoneyVariation(0);
		R.showOthers(newMOB,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> appears!"));
		final MOB victim=caster.getVictim();
		newMOB.setStartRoom(null); // keep before postFollow for Conquest
		CMLib.commands().postFollow(newMOB,caster,true);
		if(newMOB.amFollowing()!=caster)
			caster.tell(L("@x1 seems unwilling to follow you.",newMOB.name()));
		else
		if(victim!=null)
		{
			if(newMOB.getVictim()!=victim) 
				newMOB.setVictim(victim);
			R.showOthers(newMOB,victim,CMMsg.MSG_OK_ACTION,L("<S-NAME> start(s) attacking <T-NAMESELF>!"));
		}
		if(newMOB.amDead()||newMOB.amDestroyed())
			return null;
		return(newMOB);
	}
}
