package com.planet_ink.coffee_mud.Races;
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
   Copyright 2001-2025 Bo Zimmerman

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
public class Undead extends StdRace
{
	@Override
	public String ID()
	{
		return "Undead";
	}

	private final static String localizedStaticName = CMLib.lang().L("Undead");

	public Undead()
	{
		super();
		super.naturalAbilImmunities.add("Disease_RoyaltyRot");
	}

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public int shortestMale()
	{
		return 64;
	}

	@Override
	public int shortestFemale()
	{
		return 60;
	}

	@Override
	public int heightVariance()
	{
		return 12;
	}

	@Override
	public int lightestWeight()
	{
		return 100;
	}

	@Override
	public int weightVariance()
	{
		return 100;
	}

	@Override
	public long forbiddenWornBits()
	{
		return 0;
	}

	private final static String localizedStaticRacialCat = CMLib.lang().L("Undead");

	@Override
	public String racialCategory()
	{
		return localizedStaticRacialCat;
	}

	@Override
	public boolean fertile()
	{
		return false;
	}

	@Override
	public boolean infatigueable()
	{
		return true;
	}

	@Override
	public boolean uncharmable()
	{
		return true;
	}

	@Override
	public int[] getBreathables()
	{
		return breatheAnythingArray;
	}

	//  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,2 ,2 ,1 ,2 ,2 ,1 ,0 ,1 ,1 ,0 ,0 };

	@Override
	public int[] bodyMask()
	{
		return parts;
	}

	private final int[]	agingChart	= { 0, 0, 0, 0, 0, YEARS_AGE_LIVES_FOREVER, YEARS_AGE_LIVES_FOREVER, YEARS_AGE_LIVES_FOREVER, YEARS_AGE_LIVES_FOREVER };

	@Override
	public int[] getAgingChart()
	{
		return agingChart;
	}

	@Override
	public int availabilityCode()
	{
		return Area.THEME_FANTASY | Area.THEME_SKILLONLYMASK;
	}

	@Override
	public void affectCharState(final MOB affectedMOB, final CharState affectableState)
	{
		super.affectCharState(affectedMOB, affectableState);
		affectableState.setHunger(CMProps.getIntVar(CMProps.Int.HUNGER_FULL)+10);
		affectedMOB.curState().setHunger(affectableState.getHunger());
		affectableState.setThirst(CMProps.getIntVar(CMProps.Int.THIRST_FULL)+10);
		affectedMOB.curState().setThirst(affectableState.getThirst());
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_GOLEM);
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_SEE_INFRARED);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(msg.amITarget(myHost)
		&&(msg.targetMinor()==CMMsg.TYP_SNIFF)
		&&(CMLib.flags().canSmell(msg.source()))
		&&(myHost instanceof MOB)
		&&(ID().equals("Undead")))
			msg.source().tell(L("@x1 stinks of grime and decay.",name()));
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(ticking instanceof MOB)
		{
			final MOB mob=(MOB)ticking;
			mob.curState().setHunger(mob.maxState().getHunger());
			mob.curState().setThirst(mob.maxState().getThirst());
			final MOB followingM = mob.getGroupLeader();
			if(followingM!=mob)
			{
				if((mob.location() == followingM.location())
				&&(CMLib.dice().rollPercentage()==1)
				&&(CMLib.dice().rollPercentage()==1)
				&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.AUTODISEASE)))
				{
					final Ability A=CMClass.getAbility("Disease_RoyaltyRot");
					if((A!=null)
					&&(mob.fetchEffect(A.ID())==null)
					&&(!CMSecurity.isAbilityDisabled(A.ID())))
						A.invoke(mob,mob,true,0);
				}
			}
		}
		return true;
	}

	@Override
	public String makeMobName(final char gender, final int age)
	{
		switch(age)
		{
			case Race.AGE_INFANT:
				return name().toLowerCase()+" of a baby";
			case Race.AGE_TODDLER:
				return name().toLowerCase()+" of a toddler";
			case Race.AGE_CHILD:
				return name().toLowerCase()+" of a child";
			default :
				return super.makeMobName('N', age);
		}
	}

	@Override
	public Weapon[] getNaturalWeapons()
	{
		if(this.naturalWeaponChoices.length==0)
			this.naturalWeaponChoices = super.getHumanoidWeapons();
		return super.getNaturalWeapons();
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(myHost instanceof MOB)
		{
			final MOB mob=(MOB)myHost;
			if(msg.amITarget(mob))
			{
				switch(msg.targetMinor())
				{
				case CMMsg.TYP_HEALING:
					{
						final int amount=msg.value();
						if((amount>0)
						&&(msg.tool() instanceof Ability))
						{
							if((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_DISEASE)
								break;
							if((CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_HEALINGMAGIC|Ability.FLAG_HOLY))
							&&(!CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_UNHOLY)))
							{
								CMLib.combat().postDamage(msg.source(),mob,msg.tool(),amount,CMMsg.MASK_ALWAYS|CMMsg.TYP_ACID,Weapon.TYPE_BURNING,L("The healing magic from <S-NAME> <DAMAGES> <T-NAMESELF>."));
								if((mob.getVictim()==null)&&(mob!=msg.source())&&(mob.isMonster()))
									mob.setVictim(msg.source());
							}
						}
					}
					if(!(msg.tool() instanceof MOB))
						return false;
					break;
				default:
					if((msg.tool() instanceof Ability)
					&&(CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_UNHOLY))
					&&(!CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_HOLY)))
					{
						// pass through
					}
					else
					{
						break;
					}
					//$FALL-THROUGH$
				case CMMsg.TYP_DAMAGE:
				case CMMsg.TYP_UNDEAD:
					if((msg.sourceMinor()==CMMsg.TYP_UNDEAD)
					||((msg.tool() instanceof Ability)
						&&(CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_UNHOLY))
						&&(!CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_HOLY))))
					{
						final int amount=msg.value();
						if(amount>0)
						{
							msg.modify(msg.source(),mob,msg.tool(),CMMsg.MASK_ALWAYS|CMMsg.TYP_CAST_SPELL,CMMsg.MSG_HEALING,
									CMMsg.MASK_ALWAYS|CMMsg.TYP_CAST_SPELL,L("The harming magic heals <T-NAME>."));
							msg.addTrailerRunnable(new Runnable()
							{
								private final MOB me = mob;
								private final MOB src = msg.source();
								private final MOB follower=mob.amFollowing();
								@Override
								public void run()
								{
									if((me.getVictim()==src)
									&&(src.getVictim()==null))
									{
										me.setVictim(null);
										me.setFollowing(follower);
									}
								}
							});
						}
					}
					break;
				case CMMsg.TYP_GAS:
				case CMMsg.TYP_MIND:
				case CMMsg.TYP_PARALYZE:
				case CMMsg.TYP_POISON:
				case CMMsg.TYP_DISEASE:
					if((!mob.amDead())
					&&(CMath.bset(msg.targetMajor(),CMMsg.MASK_MALICIOUS))
					&&(!msg.sourceMajor(CMMsg.MASK_CNTRLMSG) && !msg.targetMajor(CMMsg.MASK_CNTRLMSG)))
					{
						if((msg.tool()==msg.source())&&(msg.sourceMinor()==CMMsg.TYP_GAS))
							return false;
						String immunityName="certain";
						if(msg.tool() instanceof MOB)
							Log.debugOut("Got a weird Undead Immunity: "+msg.toFlatString());
						if(msg.tool()!=null)
							immunityName=msg.tool().name();
						if(mob!=msg.source())
							mob.location().show(mob,msg.source(),CMMsg.MSG_OK_VISUAL,L("<S-NAME> seem(s) immune to @x1 attacks from <T-NAME>.",immunityName));
						else
							mob.location().show(mob,msg.source(),CMMsg.MSG_OK_VISUAL,L("<S-NAME> seem(s) immune to @x1.",immunityName));
						return false;
					}
					break;
				}
				switch(msg.sourceMinor())
				{
				case CMMsg.TYP_DISEASE:
				case CMMsg.TYP_GAS:
				case CMMsg.TYP_MIND:
				case CMMsg.TYP_PARALYZE:
				case CMMsg.TYP_POISON:
					if((CMath.bset(msg.targetMajor(),CMMsg.MASK_MALICIOUS)
						||(msg.targetMinor()==CMMsg.TYP_DAMAGE))
					&&(!mob.amDead()))
					{
						String immunityName="certain";
						if(msg.tool()!=null)
							immunityName=msg.tool().name();
						if((msg.tool()==msg.source())&&(msg.sourceMinor()==CMMsg.TYP_GAS))
							return false;
						if(mob!=msg.source())
							mob.location().show(mob,msg.source(),CMMsg.MSG_OK_VISUAL,L("<S-NAME> seem(s) immune to @x1 attacks from <T-NAME>.",immunityName));
						else
							mob.location().show(mob,msg.source(),CMMsg.MSG_OK_VISUAL,L("<S-NAME> seem(s) immune to @x1.",immunityName));
						return false;
					}
					break;
				case CMMsg.TYP_UNDEAD:
					{
						final int amount=msg.value();
						if(amount>0)
						{
							msg.modify(msg.source(),mob,msg.tool(),CMMsg.MASK_ALWAYS|CMMsg.TYP_CAST_SPELL,CMMsg.MSG_HEALING,CMMsg.MASK_ALWAYS|CMMsg.TYP_CAST_SPELL,L("The harming magic heals <T-NAMESELF>."));
							msg.addTrailerRunnable(new Runnable()
							{
								private final MOB me = mob;
								private final MOB src = msg.source();
								private final MOB follower=mob.amFollowing();
								@Override
								public void run()
								{
									if((me.getVictim()==src)
									&&(src.getVictim()==null))
									{
										me.setVictim(null);
										me.setFollowing(follower);
									}
								}
							});
						}
					}
					break;
				default:
					break;
				}
			}
			else
			if((msg.source()==mob)
			&&(msg.tool() instanceof Ability)
			&&(msg.tool().ID().equals("Bleeding")))
				return false;
		}
		return super.okMessage(myHost,msg);
	}

	private static final int[] UNDEAD_SAVE_STATS = new int[]
	{
		CharStats.STAT_SAVE_POISON,
		CharStats.STAT_SAVE_MIND,
		CharStats.STAT_SAVE_GAS,
		CharStats.STAT_SAVE_PARALYSIS,
		CharStats.STAT_SAVE_UNDEAD,
		CharStats.STAT_SAVE_DISEASE

	};

	@Override
	public void affectCharStats(final MOB affectedMOB, final CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		for(int statDex=0;statDex<UNDEAD_SAVE_STATS.length;statDex++)
		{
			final int stat = UNDEAD_SAVE_STATS[statDex];
			affectableStats.setStat(stat,affectableStats.getStat(stat)+100);
		}
	}

	@Override
	public DeadBody getCorpseContainer(final MOB mob, final Room room)
	{
		final DeadBody body=super.getCorpseContainer(mob,room);
		if((body!=null)&&(mob!=null))
		{
			if(!CMSecurity.isDisabled(CMSecurity.DisFlag.AUTODISEASE))
			{
				if((mob.name().toUpperCase().indexOf("DRACULA")>=0)
				||(mob.name().toUpperCase().indexOf("VAMPIRE")>=0))
				{
					if(!CMSecurity.isAbilityDisabled("Disease_Vampirism"))
						body.addNonUninvokableEffect(CMClass.getAbility("Disease_Vampirism"));
				}
				else
				if((mob.name().toUpperCase().indexOf("GHOUL")>=0)
				||(mob.name().toUpperCase().indexOf("GHAST")>=0))
				{
					if(!CMSecurity.isAbilityDisabled("Disease_Cannibalism"))
						body.addNonUninvokableEffect(CMClass.getAbility("Disease_Cannibalism"));
				}
			}
			if(ID().equals("Undead"))
			{
				final Ability A=CMClass.getAbility("Prop_Smell");
				body.addNonUninvokableEffect(A);
				A.setMiscText(body.name()+" SMELLS HORRIBLE!");
			}
		}
		return body;
	}

	@Override
	public String healthText(final MOB viewer, final MOB mob)
	{
		final double pct=(CMath.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return L("^r@x1^r is near destruction!^N",mob.name(viewer));
		else
		if(pct<.20)
			return L("^r@x1^r is massively broken and damaged.^N",mob.name(viewer));
		else
		if(pct<.30)
			return L("^r@x1^r is very damaged.^N",mob.name(viewer));
		else
		if(pct<.40)
			return L("^y@x1^y is somewhat damaged.^N",mob.name(viewer));
		else
		if(pct<.50)
			return L("^y@x1^y is very weak and slightly damaged.^N",mob.name(viewer));
		else
		if(pct<.60)
			return L("^p@x1^p has lost stability and is weak.^N",mob.name(viewer));
		else
		if(pct<.70)
			return L("^p@x1^p is unstable and slightly weak.^N",mob.name(viewer));
		else
		if(pct<.80)
			return L("^g@x1^g is unbalanced and unstable.^N",mob.name(viewer));
		else
		if(pct<.90)
			return L("^g@x1^g is somewhat unbalanced.^N",mob.name(viewer));
		else
		if(pct<.99)
			return L("^g@x1^g is no longer in perfect condition.^N",mob.name(viewer));
		else
			return L("^c@x1^c is in perfect condition.^N",mob.name(viewer));
	}

	private static Vector<RawMaterial>	resources	= new Vector<RawMaterial>();

	@Override
	public List<RawMaterial> myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				(L("some @x1 blood",name().toLowerCase()),RawMaterial.RESOURCE_BLOOD));
				final RawMaterial flesh = makeResource
						(L("some @x1 flesh",name().toLowerCase()),RawMaterial.RESOURCE_MEAT);
				final Ability A=CMClass.getAbility("Prop_Smell");
				flesh.addNonUninvokableEffect(A);
				A.setMiscText(flesh.name()+" SMELLS HORRIBLE!");
				final Ability dA=CMClass.getAbility("Disease_Nausea");
				flesh.addNonUninvokableEffect(dA);
				resources.addElement(flesh);
			}
		}
		return resources;
	}
}

