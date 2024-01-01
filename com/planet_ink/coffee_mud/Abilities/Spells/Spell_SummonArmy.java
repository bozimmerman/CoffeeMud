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
   Copyright 2003-2024 Bo Zimmerman

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
public class Spell_SummonArmy extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_SummonArmy";
	}

	private final static String	localizedName	= CMLib.lang().L("Summon Army");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Monster Summoning)");

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
	public int enchantQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	public boolean	hasFought	= false;

	@Override
	public void unInvoke()
	{
		final MOB mob=(MOB)affected;
		super.unInvoke();
		if((canBeUninvoked())&&(mob!=null))
		{
			if(mob.amDead())
				mob.setLocation(null);
			mob.destroy();
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;
			final MOB folM=mob.amFollowing();
			if((msg.amISource(mob)||msg.amISource(folM)||(msg.source()==invoker()))
			&&(msg.sourceMinor()==CMMsg.TYP_QUIT))
			{
				unInvoke();
				if(msg.source().playerStats()!=null)
					msg.source().playerStats().setLastUpdated(0);
			}
			else
			if((mob.isInCombat())
			&&(folM != null)
			&&(msg.target() == folM)
			&&(msg.source().getVictim()==folM)
			&&((msg.targetMajor()&CMMsg.MASK_MALICIOUS)>0)
			&&(mob.getVictim()!=msg.source())
			&&(CMLib.flags().isAliveAwakeMobile(mob, true)))
			{
				msg.source().setVictim(mob); // if you don't do this now, then EVERY soldier will jump in...
				CMLib.threads().scheduleRunnable(new Runnable() 
				{
					final MOB targetM=msg.source();
					@Override
					public void run()
					{
						final Room R=mob.location();
						if((R!=null)
						&&(targetM!=null)
						&&(CMLib.flags().isAliveAwakeMobile(mob, true))
						&&(CMLib.flags().isAliveAwakeMobile(targetM, true)))
							R.show(mob, folM, CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> leap(s) to <T-YOUPOSS> rescue by jumping in front of @x1!",targetM.name()));
					}
				}, 500);
			}
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		final Physical affected=this.affected;
		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;
			final MOB folM = mob.amFollowing();
			if((mob.amDead())
			||(folM==null)
			||(folM.amDead())
			||((hasFought)&&(!mob.isInCombat())))
			{
				unInvoke();
				return false;
			}
			else
			if(mob.isInCombat())
			{
				if(!hasFought)
					hasFought=true;
			}
		}
		else
		{
			unInvoke();
			return false;
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":L("^S<S-NAME> summon(s) help from the Java Plane.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final String[] choices={"Dog","Orc","Tiger","Troll","Chimp","BrownBear","Goblin","LargeBat","GiantScorpion","Rattlesnake","Ogre"};
				final int num=(mob.phyStats().level()+(getXLEVELLevel(mob)+(2*getX1Level(mob))))/3;
				for(int i=0;i<num;i++)
				{
					final MOB newMOB=CMClass.getMOB(choices[CMLib.dice().roll(1,choices.length,-1)]);
					newMOB.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience","0"));
					newMOB.addTattoo("SYSTEM_SUMMONED");
					newMOB.setLocation(mob.location());
					newMOB.basePhyStats().setRejuv(PhyStats.NO_REJUV);
					newMOB.recoverCharStats();
					newMOB.recoverPhyStats();
					newMOB.recoverMaxState();
					newMOB.resetToMaxState();
					newMOB.bringToLife(mob.location(),true);
					CMLib.beanCounter().clearZeroMoney(newMOB,null);
					newMOB.setMoneyVariation(0);
					newMOB.location().showOthers(newMOB,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> appears!"));
					newMOB.setStartRoom(null); // keep before postFollow for Conquest
					newMOB.setVictim(mob.getVictim());
					CMLib.commands().postFollow(newMOB,mob,true);
					if(newMOB.amFollowing()!=mob)
						newMOB.setFollowing(mob);
					if(newMOB.getVictim()!=null)
						newMOB.getVictim().setVictim(newMOB);
					hasFought=false;
					beneficialAffect(mob,newMOB,asLevel,0);
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,null,L("<S-NAME> call(s) for magical help, but chokes on the words."));

		// return whether it worked
		return success;
	}

}
