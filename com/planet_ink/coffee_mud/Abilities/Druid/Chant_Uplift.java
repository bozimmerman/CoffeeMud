package com.planet_ink.coffee_mud.Abilities.Druid;
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
   Copyright 2019-2020 Bo Zimmerman

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
public class Chant_Uplift extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_Uplift";
	}

	private final static String	localizedName	= CMLib.lang().L("Uplift");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Uplift Morphing)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_ANIMALAFFINITY;
	}

	protected boolean inDruidicGrove(final Physical P)
	{
		if(P instanceof MOB)
		{
			final MOB mob=(MOB)P;
			return mob.location().findItem(null,"DruidicMonument")!=null;
		}
		return false;
	}

	protected int neoTickDown = -1;

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats stats)
	{
		if((affected instanceof MOB)
		&&(neoTickDown > 0))
		{
			if(super.tickDown<this.neoTickDown)
				stats.addAmbiance("(neomorphing)");
			else
				stats.addAmbiance("(morphing)");
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((tickID==Tickable.TICKID_MOB)
		&&(affected instanceof MOB)
		&&(invoker!=null))
		{
			if(neoTickDown < 0)
				neoTickDown = tickDown - (tickDown / 4);
			final MOB M=(MOB)affected;
			if(M.amDead()
			||(!CMLib.flags().isInTheGame(M,false)))
			{
				tickDown=99999;
				unInvoke();
			}
			if((M.amFollowing()!=invoker)
			||(!inDruidicGrove(M)))
				unInvoke();
			if((tickDown > neoTickDown-1)&&(tickDown < neoTickDown+1))
				M.recoverPhyStats();
		}
		return true;
	}

	@Override
	protected int overrideMana()
	{
		return Ability.COST_ALL;
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		if(canBeUninvoked())
		{
			final MOB mob=(MOB)affected;
			final Room mobR=mob.location();
			if (mobR!=null)
			{
				if((super.tickDown<2) 
				|| (super.tickDown<this.neoTickDown))
				{
					final boolean totallyComplete = tickDown < 2;
					final Race oldR=mob.baseCharStats().getMyRace();
					String raceName=oldR.name();
					final String raceID;
					if(totallyComplete)
					{
						raceName+="-Man";
						raceID=oldR.ID()+"Man";
					}
					else
					{
						final int x=raceName.indexOf(' ');
						if(x>0)
							raceName=raceName.substring(0,x+1)+"Neo"+raceName.substring(x+1);
						else
							raceName="Neo"+raceName;
						raceID="Neo"+oldR.ID();
					}
					Race R=CMClass.getRace(raceID);
					if((R!=null)&&(R.isGeneric()))
					{
						if(CMLib.database().isRaceExpired(R.ID()))
						{
							CMLib.database().DBDeleteRace(R.ID());
							CMClass.delRace(R);
							R=null;
						}
					}
					if(R==null)
					{
						if(oldR.isGeneric())
							R=(Race)oldR.copyOf();
						else
							R=oldR.makeGenRace();
						R.setStat("ID", raceID);
						R.setStat("NAME", raceName);
						if(R.bodyMask()[Race.BODY_MOUTH]<=0)
							R.bodyMask()[Race.BODY_MOUTH]=1;
						if(R.bodyMask()[Race.BODY_EYE]<=0)
							R.bodyMask()[Race.BODY_EYE]=2;
						if(totallyComplete)
						{
							if(R.bodyMask()[Race.BODY_LEG]<2)
								R.bodyMask()[Race.BODY_LEG]=2;
							if(R.bodyMask()[Race.BODY_ARM]<2)
							{
								if(R.bodyMask()[Race.BODY_LEG]>2)
								{
									R.bodyMask()[Race.BODY_LEG]-=2;
									R.bodyMask()[Race.BODY_ARM]+=2-R.bodyMask()[Race.BODY_ARM];
								}
								else
									R.bodyMask()[Race.BODY_ARM]=2;
							}
							R.bodyMask()[Race.BODY_HAND]=R.bodyMask()[Race.BODY_ARM];
							R.bodyMask()[Race.BODY_FOOT]=R.bodyMask()[Race.BODY_LEG];
	
							long forbidden = R.forbiddenWornBits();
							forbidden = forbidden &
									~(Wearable.WORN_ARMS |
									Wearable.WORN_ABOUT_BODY |
									Wearable.WORN_BACK |
									Wearable.WORN_LEGS |
									Wearable.WORN_EYES |
									Wearable.WORN_WAIST|
									Wearable.WORN_TORSO|
									Wearable.WORN_WIELD|
									Wearable.WORN_HELD|
									Wearable.WORN_MOUTH|
									Wearable.WORN_NECK|
									Wearable.WORN_LEFT_WRIST|
									Wearable.WORN_RIGHT_WRIST|
									Wearable.WORN_LEFT_FINGER|
									Wearable.WORN_RIGHT_FINGER);
							R.setStat("WEAR",""+forbidden);
						}
	
						final CharStats adjCStats=(CharStats)CMClass.getCommon("DefaultCharStats");
						adjCStats.setAllValues(0);
						final String adjCStatsStr=R.getStat("ASTATS");
						if(adjCStatsStr.length()>0)
							CMLib.coffeeMaker().setCharStats(adjCStats,adjCStatsStr);
						final CharStats oldCStats=(CharStats)CMClass.getCommon("DefaultCharStats");
						oldCStats.setAllValues(0);
						final String oldCStatsStr=R.getStat("CSTATS");
						if(oldCStatsStr.length()>0)
							CMLib.coffeeMaker().setCharStats(oldCStats,oldCStatsStr);
						for(final int code : CharStats.CODES.BASECODES())
						{
							int value=oldCStats.getStat(code);
							if(value != 0)
							{
								value = value-10;
								final int maxCode = CharStats.CODES.toMAXBASE(code);
								oldCStats.setStat(code, 0);
								adjCStats.setStat(code, adjCStats.getStat(code) + (value/2));
								adjCStats.setStat(maxCode, adjCStats.getStat(maxCode) + (value/2));
							}
						}
						if(!totallyComplete)
						{
							adjCStats.setStat(CharStats.STAT_INTELLIGENCE, adjCStats.getStat(CharStats.STAT_INTELLIGENCE) + 6);
							adjCStats.setStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ, adjCStats.getStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ) + 6);
						}
						else
						{
							adjCStats.setStat(CharStats.STAT_INTELLIGENCE, adjCStats.getStat(CharStats.STAT_INTELLIGENCE) + 2);
							adjCStats.setStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ, adjCStats.getStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ) + 2);
							adjCStats.setStat(CharStats.STAT_MAX_STRENGTH_ADJ, adjCStats.getStat(CharStats.STAT_MAX_STRENGTH_ADJ) + 1);
							adjCStats.setStat(CharStats.STAT_MAX_DEXTERITY_ADJ, adjCStats.getStat(CharStats.STAT_MAX_DEXTERITY_ADJ) + 1);
							adjCStats.setStat(CharStats.STAT_MAX_CONSTITUTION_ADJ, adjCStats.getStat(CharStats.STAT_MAX_CONSTITUTION_ADJ) + 1);
						}
						R.setStat("ASTATS", CMLib.coffeeMaker().getCharStatsStr(adjCStats));
						R.setStat("CSTATS", CMLib.coffeeMaker().getCharStatsStr(oldCStats));
	
						final int numRable = CMath.s_int(R.getStat("NUMRABLE"));
						if(numRable <= 0)
							R.setStat("NUMRABLE","");
						else
						{
							final List<String[]> rableData = new ArrayList<String[]>();
							for(int i=0;i<numRable;i++)
							{
								final String[] data = new String[]
								{
									R.getStat("GETRABLE"+i),
									R.getStat("GETRABLELVL"+i),
									R.getStat("GETRABLEQUAL"+i),
									R.getStat("GETRABLEPROF"+i),
									R.getStat("GETRABLEPARM"+i)
								};
								rableData.add(data);
							}
							R.setStat("NUMRABLE",""+numRable);
							for(int i=0;i<numRable;i++)
							{
								final String[] data=rableData.get(i);
								final Ability A=CMClass.getAbilityPrototype(data[0]);
								if(A instanceof Language)
									R.setStat("GETRABLE"+i,"Common");
								else
									R.setStat("GETRABLE"+i,data[0]);
								R.setStat("GETRABLELVL"+i,data[1]);
								R.setStat("GETRABLEQUAL"+i,data[2]);
								R.setStat("GETRABLEPROF"+i,data[3]);
								R.setStat("GETRABLEPARM"+i,data[4]);
							}
						}
	
						CMClass.addRace(R);
						CMLib.database().DBCreateRace(raceID, R.racialParms());
					}
					mob.baseCharStats().setMyRace(R);
					mob.recoverCharStats();
					mobR.recoverRoomStats();
					mobR.show(mob, null, CMMsg.MSG_OK_VISUAL, L("<S-NAME> morph(s) into a @x1.",raceName));
					if(CMStrings.containsWordIgnoreCase(mob.Name(), oldR.name()))
						mob.setName(CMStrings.replaceWord(mob.Name(), oldR.name(), raceName));
					if(CMStrings.containsWordIgnoreCase(mob.displayText(), oldR.name()))
						mob.setDisplayText(CMStrings.replaceWord(mob.displayText(), oldR.name(), raceName));
					if(CMStrings.containsWordIgnoreCase(mob.description(), oldR.name()))
						mob.setDisplayText(CMStrings.replaceWord(mob.description(), oldR.name(), raceName));
				}
				else
					mobR.show(mob, null, CMMsg.MSG_OK_VISUAL, L("<S-YOUPOSS> uplift morphing ended unsuccessfully."));
			}
		}
		super.unInvoke();
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(commands.size()<1)
		{
			mob.tell(L("Chant to whom?"));
			return false;
		}
		final String mobName=CMParms.combine(commands,0).trim().toUpperCase();
		final MOB target=getTarget(mob,commands,givenTarget);

		Room newRoom=mob.location();
		if(target!=null)
		{
			newRoom=target.location();
			if((!CMLib.flags().isAnimalIntelligence(target))
			||(target.amFollowing()!=mob))
			{
				mob.tell(L("You have no animal follower named '@x1' here.",mobName));
				return false;
			}
			if(CMLib.flags().isGolem(target)
			||CMLib.flags().isAPlant(target)
			||CMLib.flags().isUndead(target))
			{
				mob.tell(L("@x1 can not be affected by this magic.",mobName));
				return false;
			}

		}
		else
		{
			mob.tell(L("You have no animal follower named '@x1' here.",mobName));
			return false;
		}

		final Race humanR=CMClass.getRace("Human");

		if((target.baseCharStats().getMyRace().ID().indexOf("Neo")>=0)
		||(target.baseCharStats().getMyRace().ID().indexOf("_Man")>=0)
		||((humanR!=null)&&(!target.baseCharStats().getMyRace().canBreedWith(humanR, true)))
		||(target.baseCharStats().getMyRace().racialCategory().toLowerCase().indexOf("elemental")>=0))
		{
			mob.tell(L("This magic would not affect a @x1.",target.baseCharStats().getMyRace().name()));
			return false;
		}

		if((target.charStats().getMyRace().ID().indexOf("Neo")>=0)
		||(target.charStats().getMyRace().ID().indexOf("_Man")>=0)
		||((humanR!=null)&&(!target.charStats().getMyRace().canBreedWith(humanR, true)))
		||(target.charStats().getMyRace().racialCategory().toLowerCase().indexOf("elemental")>=0))
		{
			mob.tell(L("This magic would not affect a @x1.",target.charStats().getMyRace().name()));
			return false;
		}

		if(!inDruidicGrove(mob))
		{
			mob.tell(L("There is no druidic monument here.  You can only use this chant in a druidic grove."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> chant(s) to <T-NAMESELF>, invoking an uplifting aura.^?"));
			final CMMsg msg2=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),null);
			if((mob.location().okMessage(mob,msg))&&((newRoom==mob.location())||(newRoom.okMessage(mob,msg2))))
			{
				mob.location().send(mob,msg);
				if(newRoom!=mob.location())
					newRoom.send(target,msg2);
				final Chant_Uplift upliftA = (Chant_Uplift)beneficialAffect(mob,target,asLevel,(int)CMProps.getTicksPerMudHour()*2);
				if(upliftA != null)
				{
					mob.location().show(target, null, CMMsg.MSG_OK_VISUAL, L("<S-NAME> begin(s) a slow morphing process..."));
					final Chant_Hibernation sleepA=(Chant_Hibernation)CMClass.getAbility("Chant_Hibernation");
					if(sleepA != null)
					{
						mob.phyStats().setDisposition(mob.phyStats().disposition()|PhyStats.IS_SITTING);
						mob.basePhyStats().setDisposition(mob.basePhyStats().disposition()|PhyStats.IS_SITTING);
						sleepA.invoke(mob, mob, true, 0);
						final Chant_Hibernation sleepEffectA=(Chant_Hibernation)mob.fetchEffect(sleepA.ID());
						if(sleepEffectA != null)
						{
							sleepEffectA.setMiscText("DEEP=true FULLMANAREVOKE=true");
							if(sleepEffectA.getTickDownRemaining() >= (upliftA.getTickDownRemaining()/2))
								sleepEffectA.setTickDownRemaining(upliftA.getTickDownRemaining()/2);
						}
					}
				}
			}

		}
		else
			beneficialVisualFizzle(mob,target,L("<S-NAME> chant(s) to <T-NAMESELF>, but the magic fades."));

		// return whether it worked
		return success;
	}
}
