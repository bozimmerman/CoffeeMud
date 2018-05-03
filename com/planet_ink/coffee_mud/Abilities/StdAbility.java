package com.planet_ink.coffee_mud.Abilities;
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
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB.Attrib;
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

@SuppressWarnings({"unchecked","rawtypes"})
public class StdAbility implements Ability
{
	@Override
	public String ID()
	{
		return "StdAbility";
	}

	protected boolean			isAnAutoEffect	= false;
	protected int				proficiency		= 0;
	protected boolean			savable			= true;
	protected String			miscText		= "";
	protected MOB				invoker			= null;
	protected Physical			affected		= null;
	protected boolean 			canBeUninvoked	= true;
	protected volatile boolean	unInvoked		= false;
	protected volatile int 		tickDown		= -1;
	protected long 				lastCastHelp	= 0;
	protected boolean 			amDestroyed		= false;

	private static final int[] STATIC_USAGE_NADA= new int[3];

	public StdAbility()
	{
		super();
		//CMClass.bumpCounter(this,CMClass.CMObjectType.ABILITY);//removed for mem & perf
	}
	//protected void finalize(){ CMClass.unbumpCounter(this,CMClass.CMObjectType.ABILITY); }//removed for mem & perf

	@Override
	public CMObject newInstance()
	{
		try
		{
			return this.getClass().newInstance();
		}
		catch(final Exception e)
		{
			Log.errOut(ID(),e);
		}
		return new StdAbility();
	}

	@Override
	public String Name()
	{
		return name();
	}

	@Override
	public String name()
	{
		return "an ability";
	}

	@Override
	public String description()
	{
		return "&";
	}

	@Override
	public String displayText()
	{
		return "Affected list display for " + ID();
	}

	@Override
	public String image()
	{
		return "";
	}

	@Override
	public String rawImage()
	{
		return "";
	}

	@Override
	public void setImage(String newImage)
	{
	}

	public static final String[]	empty	= {};

	@Override
	public String[] triggerStrings()
	{
		return empty;
	}

	@Override
	public int maxRange()
	{
		return adjustedMaxInvokerRange(0);
	}

	@Override
	public int minRange()
	{
		return 0;
	}

	@Override
	public double castingTime(final MOB mob, final List<String> cmds)
	{
		return CMProps.getSkillActionCost(ID());
	}

	@Override
	public double combatCastingTime(final MOB mob, final List<String> cmds)
	{
		return CMProps.getSkillCombatActionCost(ID());
	}

	@Override
	public double checkedCastingCost(final MOB mob, final List<String> commands)
	{
		if(mob!=null)
		{
			if(mob.isInCombat())
				return combatCastingTime(mob,commands);
			if(abstractQuality()==Ability.QUALITY_MALICIOUS)
				return combatCastingTime(mob,commands);
		}
		return castingTime(mob,commands);
	}

	@Override
	public boolean putInCommandlist()
	{
		return true;
	}

	@Override
	public boolean isAutoInvoked()
	{
		return false;
	}

	@Override
	public boolean bubbleAffect()
	{
		return false;
	}

	protected int getTicksBetweenCasts()
	{
		return 0;
	}

	protected long getTimeOfNextCast()
	{
		return 0;
	}

	protected void setTimeOfNextCast(long absoluteTime)
	{
	}

	protected ExpertiseLibrary.SkillCostDefinition getRawTrainingCost()
	{
		return CMProps.getNormalSkillGainCost(ID());
	}

	@Override
	public ExpertiseLibrary.SkillCost getTrainingCost(MOB mob)
	{
		int qualifyingLevel;
		int playerLevel=1;
		if(mob!=null)
		{
			final Integer[] O=CMLib.ableMapper().getCostOverrides(mob,ID());
			if(O!=null)
			{
				Integer val=O[AbilityMapper.Cost.TRAIN.ordinal()];
				if(val!=null)
					return CMLib.expertises().createNewSkillCost(ExpertiseLibrary.CostType.TRAIN,Double.valueOf(val.intValue()));
				val=O[AbilityMapper.Cost.PRAC.ordinal()];
				if(val!=null)
					return CMLib.expertises().createNewSkillCost(ExpertiseLibrary.CostType.PRACTICE,Double.valueOf(val.intValue()));
			}
			qualifyingLevel=CMLib.ableMapper().qualifyingLevel(mob, this);
			playerLevel=mob.basePhyStats().level();
		}
		else
		{
			qualifyingLevel=CMLib.ableMapper().lowestQualifyingLevel(ID());
			playerLevel=1;
		}
		if(qualifyingLevel<=0)
			qualifyingLevel=1;
		final ExpertiseLibrary.SkillCostDefinition rawCost=getRawTrainingCost();
		if(rawCost==null)
			return CMLib.expertises().createNewSkillCost(ExpertiseLibrary.CostType.TRAIN,Double.valueOf(1.0));
		final double[] vars=new double[]{ qualifyingLevel,playerLevel};
		final double value=CMath.parseMathExpression(rawCost.costDefinition(),vars);
		return CMLib.expertises().createNewSkillCost(rawCost.type(),Double.valueOf(value));
	}

	protected int practicesToPractice(MOB mob)
	{
		if(mob!=null)
		{
			final Integer[] O=CMLib.ableMapper().getCostOverrides(mob,ID());
			if((O!=null)&&(O[AbilityMapper.Cost.PRACPRAC.ordinal()]!=null))
				return O[AbilityMapper.Cost.PRACPRAC.ordinal()].intValue();
		}
		return iniPracticesToPractice();
	}

	protected int iniPracticesToPractice()
	{
		return 1;
	}

	protected void setTimeOfNextCast(MOB caster)
	{
		long newTime=(getTicksBetweenCasts()*CMProps.getTickMillis());
		double mul=1.0;
		mul -= (0.05 * getXLEVELLevel(caster));
		mul -= (0.1 * getXTIMELevel(caster));
		newTime=Math.round(CMath.mul(newTime,mul));
		setTimeOfNextCast(System.currentTimeMillis() +newTime);
	}

	@Override
	public String miscTextFormat()
	{
		return CMParms.FORMAT_UNDEFINED;
	}

	@Override
	public long flags()
	{
		return 0;
	}

	@Override
	public int usageType()
	{
		return USAGE_MANA;
	}

	/**
	 *  amount of mana/move used by this ability, overriding ini file
	 *  -1=normal, Ability.COST_ALL=all, Ability.COST_PCT
	 * @return amount of mana/move used by this ability, overriding ini file
	 */
	protected int overrideMana()
	{
		return Ability.COST_NORMAL;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public int enchantQuality()
	{
		return abstractQuality();
	}

	@Override
	public void initializeClass()
	{
	}

	@Override
	public String L(final String str, final String ... xs)
	{
		return CMLib.lang().fullSessionTranslation(str, xs);
	}

	protected static String[] I(final String[] str)
	{
		for(int i=0;i<str.length;i++)
			str[i]=CMLib.lang().commandWordTranslation(str[i]);
		return str;
	}

	protected int castingQuality(MOB mob, Physical target, int abstractQuality)
	{
		if((target!=null)&&(target.fetchEffect(ID())!=null))
			return Ability.QUALITY_INDIFFERENT;
		if(isAutoInvoked())
			return Ability.QUALITY_INDIFFERENT;
		if((mob!=null)&&(target!=null)&&(mob.getVictim()==target))
		{
			if((minRange()>0)&&(mob.rangeToTarget()<minRange()))
				return Ability.QUALITY_INDIFFERENT;
			if(mob.rangeToTarget()>maxRange())
				return Ability.QUALITY_INDIFFERENT;

		}
		switch(abstractQuality)
		{
		case Ability.QUALITY_BENEFICIAL_OTHERS:
			if(mob==target)
				return Ability.QUALITY_BENEFICIAL_SELF;
			return Ability.QUALITY_BENEFICIAL_OTHERS;
		case Ability.QUALITY_MALICIOUS:
			return Ability.QUALITY_MALICIOUS;
		case Ability.QUALITY_BENEFICIAL_SELF:
			if((target instanceof MOB)&&(mob!=target))
				return Ability.QUALITY_INDIFFERENT;
			return Ability.QUALITY_BENEFICIAL_SELF;
		default:
			return Ability.QUALITY_INDIFFERENT;
		}
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		return castingQuality(mob,target,abstractQuality());
	}

	protected synchronized int expertise(final MOB mob, final Ability A, final ExpertiseLibrary.Flag code)
	{
		if((mob!=null)
		&&(A.isNowAnAutoEffect()
			||(A.canBeUninvoked())
			||A.isAutoInvoked()))
		{
			return CMLib.expertises().getExpertiseLevel(mob, A.ID(), code);
		}
		return 0;
	}

	protected int getX1Level(MOB mob)
	{
		return expertise(mob, this, ExpertiseLibrary.Flag.X1);
	}

	protected int getX2Level(MOB mob)
	{
		return expertise(mob, this, ExpertiseLibrary.Flag.X2);
	}

	protected int getX3Level(MOB mob)
	{
		return expertise(mob, this, ExpertiseLibrary.Flag.X3);
	}

	protected int getX4Level(MOB mob)
	{
		return expertise(mob, this, ExpertiseLibrary.Flag.X4);
	}

	protected int getX5Level(MOB mob)
	{
		return expertise(mob, this, ExpertiseLibrary.Flag.X5);
	}

	protected int getXLEVELLevel(MOB mob)
	{
		return expertise(mob, this, ExpertiseLibrary.Flag.LEVEL);
	}

	protected int getXLOWCOSTLevel(MOB mob)
	{
		return expertise(mob, this, ExpertiseLibrary.Flag.LOWCOST);
	}

	protected int getXLOWFREECOSTLevel(MOB mob)
	{
		return expertise(mob, this, ExpertiseLibrary.Flag.LOWFREECOST);
	}

	protected int getXMAXRANGELevel(MOB mob)
	{
		return expertise(mob, this, ExpertiseLibrary.Flag.MAXRANGE);
	}

	protected int getXTIMELevel(MOB mob)
	{
		return expertise(mob, this, ExpertiseLibrary.Flag.TIME);
	}

	protected int getXPCOSTLevel(MOB mob)
	{
		return expertise(mob, this, ExpertiseLibrary.Flag.XPCOST);
	}

	protected int getXPCOSTAdjustment(MOB mob, int xpLoss)
	{
		final int xLevel=getXPCOSTLevel(mob);
		if(xLevel<=0)
			return xpLoss;
		return xpLoss-(int)Math.round(CMath.mul(xpLoss,CMath.mul(.05,xLevel)));
	}

	protected int adjustedMaxInvokerRange(int max)
	{
		if(invoker==null)
			return max;
		final int level=getXMAXRANGELevel(invoker);
		if(level<=0)
			return  max;
		return max+(int)Math.round(Math.ceil(CMath.mul(max,CMath.mul(level,0.2))));
	}

	/**
	 * Designates whether, when used as a property/effect, what sort of objects this
	 * ability can affect. Uses the Ability.CAN_* constants.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability
	 * @return a mask showing the type of objects this ability can affect
	 */
	protected int canAffectCode(){return Ability.CAN_AREAS|
										 Ability.CAN_ITEMS|
										 Ability.CAN_MOBS|
										 Ability.CAN_ROOMS|
										 Ability.CAN_EXITS;}
	/**
	 * Designates whether, when invoked as a skill, what sort of objects this
	 * ability can effectively target. Uses the Ability.CAN_* constants.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability
	 * @return a mask showing the type of objects this ability can target
	 */
	protected int canTargetCode(){return Ability.CAN_AREAS|
										 Ability.CAN_ITEMS|
										 Ability.CAN_MOBS|
										 Ability.CAN_ROOMS|
										 Ability.CAN_EXITS;}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL;
	}

	@Override
	public long expirationDate()
	{
		return (tickDown) * CMProps.getTickMillis();
	}

	@Override
	public void setExpirationDate(long time)
	{
		if(time>System.currentTimeMillis())
			tickDown=(int)((time-System.currentTimeMillis())/CMProps.getTickMillis());
	}

	@Override
	public boolean isNowAnAutoEffect()
	{
		return isAnAutoEffect;
	}

	@Override
	public boolean isSavable()
	{
		return savable;
	}

	@Override
	public void setSavable(boolean truefalse)
	{
		savable=truefalse;
	}

	@Override
	public void destroy()
	{
		amDestroyed=true;
		affected=null;
		invoker=null;
		miscText=null;
	}

	@Override
	public boolean amDestroyed()
	{
		return amDestroyed;
	}

	@Override
	public void setName(String newName)
	{
	}

	@Override
	public void setDisplayText(String newDisplayText)
	{
	}

	@Override
	public void setDescription(String newDescription)
	{
	}

	@Override
	public int abilityCode()
	{
		return 0;
	}

	@Override
	public void setAbilityCode(int newCode)
	{
	}

	@Override
	public List<String> externalFiles()
	{
		return null;
	}

	protected long minCastWaitTime()
	{
		return 0;
	}

	// ** For most abilities, the following stuff actually matters */
	@Override
	public void setMiscText(String newMiscText)
	{
		miscText=newMiscText;
	}

	@Override
	public String text()
	{
		return miscText;
	}

	@Override
	public int proficiency()
	{
		return proficiency;
	}

	@Override
	public void setProficiency(int newProficiency)
	{
		proficiency=newProficiency;
		if(proficiency>100) 
			proficiency=100;
	}

	protected int addedTickTime(MOB invokerMOB, int baseTickTime)
	{
		return (int)Math.round(CMath.mul(baseTickTime,CMath.mul(getXTIMELevel(invokerMOB),0.20)));
	}

	@Override
	public void startTickDown(MOB invokerMOB, Physical affected, int tickTime)
	{
		if(invokerMOB!=null) 
			invoker=invokerMOB;

		savable=false; // makes it so that the effect does not save!

		if((classificationCode() != (Ability.ACODE_COMMON_SKILL|Ability.DOMAIN_CRAFTINGSKILL))
		&&(classificationCode() != (Ability.ACODE_COMMON_SKILL|Ability.DOMAIN_GATHERINGSKILL))
		&&(classificationCode() != (Ability.ACODE_COMMON_SKILL|Ability.DOMAIN_BUILDINGSKILL)))
			tickTime+=addedTickTime(invokerMOB,tickTime);
		if(invoker()!=null)
		{
			for(int c=0;c<invoker().charStats().numClasses();c++)
				tickTime=invoker().charStats().getMyClass(c).classDurationModifier(invoker(),this,tickTime);
		}
		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;
			final Room room=mob.location();
			if(room==null) 
				return;
			if(affected.fetchEffect(ID())==null)
				affected.addEffect(this);
			room.recoverRoomStats();
			if(invoker()!=affected)
			{
				for(int c=0;c<mob.charStats().numClasses();c++)
					tickTime=mob.charStats().getMyClass(c).classDurationModifier(mob,this,tickTime);
			}
		}
		else
		{
			if(affected.fetchEffect(this.ID())==null)
				affected.addEffect(this);

			if(affected instanceof Room)
				((Room)affected).recoverRoomStats();
			else
				affected.recoverPhyStats();
			CMLib.threads().startTickDown(this,Tickable.TICKID_MOB,1);
		}
		tickDown=tickTime;
	}

	public boolean disregardsArmorCheck(MOB mob)
	{
		return ((mob==null)
				||(mob.isMonster())
				||(CMLib.ableMapper().qualifiesByLevel(mob,this)));
	}

	protected int getPersonalLevelAdjustments(final MOB caster)
	{
		final CharStats charStats = caster.charStats();
		return  charStats.getAbilityAdjustment("level+"+ID())
			+ charStats.getAbilityAdjustment("level+"+Ability.ACODE_DESCS[classificationCode()&Ability.ALL_ACODES])
			+ charStats.getAbilityAdjustment("level+"+Ability.DOMAIN_DESCS[(classificationCode()&Ability.ALL_DOMAINS)>> 5])
			+ charStats.getAbilityAdjustment("level+*");
	}
	
	@Override
	public int adjustedLevel(MOB caster, int asLevel)
	{
		if(caster==null)
			return 1;
		final int lowestQualifyingLevel=CMLib.ableMapper().lowestQualifyingLevel(this.ID());
		int adjLevel=lowestQualifyingLevel;
		if(asLevel<=0)
		{
			final int qualifyingLevel=CMLib.ableMapper().qualifyingLevel(caster,this);
			if((caster.isMonster())||(qualifyingLevel>=0))
				adjLevel+=(CMLib.ableMapper().qualifyingClassLevel(caster,this)-qualifyingLevel);
			else
				adjLevel=caster.phyStats().level()-lowestQualifyingLevel-25;
		}
		else
			adjLevel=asLevel;
		if(adjLevel<lowestQualifyingLevel)
			adjLevel=lowestQualifyingLevel;
		adjLevel += getPersonalLevelAdjustments(caster);
		if(adjLevel<1)
			return 1;
		int level=adjLevel+getXLEVELLevel(caster);
		final CharStats CS=caster.charStats();
		for(int c=0;c<CS.numClasses();c++)
			level=CS.getMyClass(c).classDurationModifier(invoker(),this,level);
		return level;
	}

	protected int experienceLevels(MOB caster, int asLevel)
	{
		if(caster==null)
			return 1;
		int adjLevel=1;
		final int qualifyingLevel=CMLib.ableMapper().qualifyingLevel(caster,this);
		final int lowestQualifyingLevel=CMLib.ableMapper().lowestQualifyingLevel(this.ID());
		if(qualifyingLevel>=0)
		{
			final int qualClassLevel=CMLib.ableMapper().qualifyingClassLevel(caster,this);
			if(qualClassLevel>=qualifyingLevel)
				adjLevel=(qualClassLevel-qualifyingLevel)+1;
			else
			if(caster.phyStats().level()>=qualifyingLevel)
				adjLevel=(caster.phyStats().level()-qualifyingLevel)+1;
			else
			if(caster.phyStats().level()>=lowestQualifyingLevel)
				adjLevel=(caster.phyStats().level()-lowestQualifyingLevel)+1;
		}
		else
		if(caster.phyStats().level()>=lowestQualifyingLevel)
			adjLevel=(caster.phyStats().level()-lowestQualifyingLevel)+1;
		if(asLevel>0)
			adjLevel=asLevel;
		adjLevel += getPersonalLevelAdjustments(caster);
		if(adjLevel<1)
			return 1;
		return adjLevel+getXLEVELLevel(caster);
	}

	@Override
	public boolean canTarget(int can_code)
	{
		return CMath.bset(canTargetCode(),can_code);
	}

	@Override
	public boolean canAffect(int can_code)
	{
		return CMath.bset(canAffectCode(),can_code);
	}

	@Override
	public boolean canAffect(Physical P)
	{
		if((P==null)&&(canAffectCode()==0))
			return true;
		if(P==null)
			return false;
		if((P instanceof MOB)&&((canAffectCode()&Ability.CAN_MOBS)>0))
			return true;
		if((P instanceof Item)&&((canAffectCode()&Ability.CAN_ITEMS)>0))
			return true;
		if((P instanceof Exit)&&((canAffectCode()&Ability.CAN_EXITS)>0))
			return true;
		if((P instanceof Room)&&((canAffectCode()&Ability.CAN_ROOMS)>0))
			return true;
		if((P instanceof Area)&&((canAffectCode()&Ability.CAN_AREAS)>0))
			return true;
		return false;
	}

	@Override
	public boolean canTarget(Physical P)
	{
		if((P==null)&&(canTargetCode()==0))
			return true;
		if(P==null)
			return false;
		if((P instanceof MOB)&&((canTargetCode()&Ability.CAN_MOBS)>0))
			return true;
		if((P instanceof Item)&&((canTargetCode()&Ability.CAN_ITEMS)>0))
			return true;
		if((P instanceof Room)&&((canTargetCode()&Ability.CAN_ROOMS)>0))
			return true;
		if((P instanceof Area)&&((canTargetCode()&Ability.CAN_AREAS)>0))
			return true;
		return false;
	}

	protected MOB getTarget(MOB mob, List commands, Environmental givenTarget)
	{
		return getTarget(mob,commands,givenTarget,false,false);
	}

	protected MOB getTarget(MOB mob, List commands, Environmental givenTarget, boolean quiet, boolean alreadyAffOk)
	{
		String targetName=CMParms.combine(commands,0);
		MOB target=null;
		if((givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		else
		if(targetName.length()==0)
		{
			final boolean inCombat = mob.isInCombat();
			if(inCombat
			&&(castingQuality(mob,mob.getVictim())==Ability.QUALITY_MALICIOUS)
			&&(mob.getVictim()!=null))
				target=mob.getVictim();
			else
			if(castingQuality(mob,mob)==Ability.QUALITY_BENEFICIAL_SELF)
				target=mob;
			else
			if(inCombat
			&&(abstractQuality()==Ability.QUALITY_MALICIOUS)
			&&(mob.getVictim()!=null))
				target=mob.getVictim();
			else
			if(abstractQuality()!=Ability.QUALITY_MALICIOUS)
				target=mob;
		}
		else
		if(targetName.equalsIgnoreCase("self")||targetName.equalsIgnoreCase("me"))
			target=mob;
		else
		if((targetName.length()>0)&&(mob.location()!=null))
		{
			target=mob.location().fetchInhabitant(targetName);
			if(target==null)
			{
				final Environmental t=mob.location().fetchFromRoomFavorItems(null,targetName);
				if((t!=null)&&(!(t instanceof MOB)))
				{
					if(!quiet)
						mob.tell(mob,t,null,L("You can't do that to <T-NAMESELF>."));
					return null;
				}
			}
		}

		if(target!=null)
			targetName=target.name();

		if((target==null)
		||((givenTarget==null)
			&&(!CMLib.flags().canBeSeenBy(target,mob))
			&&((!CMLib.flags().canBeHeardMovingBy(target,mob))||(!target.isInCombat()))))
		{
			if(!quiet)
			{
				if(targetName.trim().length()==0)
					mob.tell(L("You don't see them here."));
				else
					mob.tell(L("You don't see anyone called '@x1' here.",targetName));
			}
			return null;
		}

		if((!alreadyAffOk)&&(!isAutoInvoked())&&(target.fetchEffect(this.ID())!=null))
		{
			if((givenTarget==null)&&(!quiet))
			{
				if(target==mob)
					mob.tell(L("You are already affected by @x1.",name()));
				else
					mob.tell(target,null,null,L("<S-NAME> is already affected by @x1.",name()));
			}
			return null;
		}
		return target;
	}

	protected Physical getAnyTarget(MOB mob, List<String> commands, Physical givenTarget, Filterer<Environmental> filter)
	{
		return getAnyTarget(mob,commands,givenTarget,filter,false,false);
	}

	protected Physical getAnyTarget(MOB mob, Room location, boolean anyContainer, List<String> commands, 
									Physical givenTarget, Filterer<Environmental> filter)
	{
		final Physical P=getAnyTarget(mob,commands,givenTarget,filter,false,false, true);
		if(P!=null)
			return P;
		return getTarget(mob, location, givenTarget, anyContainer, commands, filter);
	}

	protected Physical getAnyTarget(MOB mob, Room location, boolean anyContainer, List<String> commands, 
			Physical givenTarget, Filterer<Environmental> filter, boolean quiet)
	{
		final Physical P=getAnyTarget(mob,commands,givenTarget,filter,false,false, true);
		if(P!=null)
			return P;
		return getTarget(mob, location, givenTarget, anyContainer, commands, filter, quiet);
	}
	
	protected Physical getAnyTarget(MOB mob, List<String> commands, Physical givenTarget, Filterer<Environmental> filter, boolean checkOthersInventory)
	{
		return getAnyTarget(mob,commands,givenTarget,filter,checkOthersInventory,false);
	}

	protected Physical getAnyTarget(MOB mob, List<String> commands, Physical givenTarget, 
			Filterer<Environmental> filter, 
			boolean checkOthersInventory, boolean alreadyAffOk)
	{
		return getAnyTarget(mob,commands,givenTarget,filter,checkOthersInventory,alreadyAffOk,false);
	}
	
	protected Physical getAnyTarget(MOB mob, List<String> commands, Physical givenTarget, 
			Filterer<Environmental> filter, 
			boolean checkOthersInventory, boolean alreadyAffOk,
			boolean quiet)
	{
		final Room R=mob.location();
		String targetName=CMParms.combine(commands,0);
		Physical target=null;
		if(givenTarget != null)
			target=givenTarget;
		else
		if((targetName.length()==0)&&(mob.isInCombat())&&(castingQuality(mob,mob.getVictim())==Ability.QUALITY_MALICIOUS))
			target=mob.getVictim();
		else
		if(targetName.equalsIgnoreCase("self")||targetName.equalsIgnoreCase("me"))
			target=mob;
		else
		if((targetName.length()==0)&&(mob.isInCombat())&&(abstractQuality()==Ability.QUALITY_MALICIOUS))
			target=mob.getVictim();
		else
		if(R!=null)
		{
			target=R.fetchFromRoomFavorMOBs(null,targetName);
			if(target==null)
				target=R.fetchFromMOBRoomFavorsItems(mob,null,targetName,filter);
			if((target==null)
			&&(targetName.equalsIgnoreCase("room")
				||targetName.equalsIgnoreCase("here")
				||targetName.equalsIgnoreCase("place")))
				target=R;
			int dir=-1;
			if((target==null)&&((dir=CMLib.directions().getGoodDirectionCode(targetName))>=0))
				target=R.getExitInDir(dir);
			if((target==null)&&(checkOthersInventory))
			{
				for(int i=0;i<R.numInhabitants();i++)
				{
					final MOB M=R.fetchInhabitant(i);
					target=M.fetchItem(null,filter,targetName);
					if(target!=null)
						break;
				}
			}
		}
		if(target!=null)
			targetName=target.name();

		if((target==null)
		||((givenTarget==null)
		   &&(!CMLib.flags().canBeSeenBy(target,mob))
		   &&((!CMLib.flags().canBeHeardMovingBy(target,mob))
				||((target instanceof MOB)&&(!((MOB)target).isInCombat())))))
		{
			if(!quiet)
			{
				if(targetName.trim().length()==0)
					mob.tell(L("You don't see that here."));
				else
				if(!CMLib.flags().isSleeping(mob))
					mob.tell(L("You don't see '@x1' here.",targetName));
			}
			return null;
		}

		if((!alreadyAffOk)&&(target.fetchEffect(this.ID())!=null))
		{
			if(givenTarget==null)
			{
				if(!quiet)
				{
					if(target==mob)
						mob.tell(L("You are already affected by @x1.",name()));
					else
						mob.tell(mob,target,null,L("<T-NAME> is already affected by @x1.",name()));
				}
			}
			return null;
		}
		return target;
	}

	protected static Item possibleContainer(MOB mob, List<String> commands, boolean withStuff, Filterer<Environmental> filter)
	{
		if((commands==null)||(commands.size()<2))
			return null;

		final String possibleContainerID=commands.get(commands.size()-1);
		final Environmental thisThang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,possibleContainerID,filter);
		if((thisThang!=null)
		&&(thisThang instanceof Item)
		&&(((Item)thisThang) instanceof Container)
		&&((!withStuff)||(((Container)thisThang).hasContent())))
		{
			commands.remove(commands.size()-1);
			return (Item)thisThang;
		}
		return null;
	}

	protected Item getTarget(MOB mob, Room location, Environmental givenTarget, List<String> commands, Filterer<Environmental> filter)
	{
		return getTarget(mob,location,givenTarget,null,commands,filter);
	}

	protected Item getTarget(MOB mob, Room location, Environmental givenTarget, 
			boolean anyContainer, List<String> commands, Filterer<Environmental> filter)
	{
		return getTarget(mob, location, givenTarget, anyContainer, commands, filter, false);
	}
	
	protected Item getTarget(MOB mob, Room location, Environmental givenTarget, 
			boolean anyContainer, List<String> commands, Filterer<Environmental> filter, 
			boolean quiet)
	{
		Item I=this.getTarget(mob, location, givenTarget, null, commands, filter, anyContainer);
		if(I!=null)
			return I;
		if(!anyContainer)
			return I;
		List<Item> containers=new ArrayList<Item>();
		if(location!=null)
		{
			for(Enumeration<Item> i=location.items();i.hasMoreElements();)
			{
				Item C=i.nextElement();
				if((C instanceof Container)
				&&(((Container)C).isOpen()))
					containers.add(C);
			}
		}
		else
		{
			for(Enumeration<Item> i=mob.items();i.hasMoreElements();)
			{
				Item C=i.nextElement();
				if((C instanceof Container)
				&&(((Container)C).isOpen()))
					containers.add(C);
			}
		}
		if(containers.size()==0)
			return this.getTarget(mob, location, givenTarget, null, commands, filter, quiet);
		else
		{
			for(int c=0;c<containers.size();c++)
			{
				Item C=containers.get(c);
				I=this.getTarget(mob, location, givenTarget, C, commands, filter, quiet || (c<containers.size()-1));
				if(I!=null)
					return I;
			}
		}
		return null;
	}
	
	protected Item getTarget(MOB mob, Room location, Environmental givenTarget, Item container, 
			List<String> commands, Filterer<Environmental> filter)
	{
		return getTarget(mob, location, givenTarget, container, commands, filter, false);
	}
	
	protected Item getTarget(MOB mob, Room location, Environmental givenTarget, Item container, 
			List<String> commands, Filterer<Environmental> filter, boolean quiet)
	{
		String targetName=CMParms.combine(commands,0);

		Environmental target=null;
		if((givenTarget!=null)&&(givenTarget instanceof Item))
			target=givenTarget;

		if((location!=null)&&(target==null)&&(targetName.length()>0))
			target=location.fetchFromRoomFavorItems(container,targetName);
		if((target==null)&&(targetName.length()>0))
		{
			if(location!=null)
				target=location.fetchFromMOBRoomFavorsItems(mob,container,targetName,filter);
			else
				target=mob.fetchItem(container, filter, targetName);
		}
		if(target!=null)
			targetName=target.name();

		if((target==null)
		||(!(target instanceof Item))
		||((givenTarget==null)&&(!CMLib.flags().canBeSeenBy(target,mob))))
		{
			if(!quiet)
			{
				if(targetName.length()==0)
					mob.tell(L("You need to be more specific."));
				else
				if((target==null)||(target instanceof Item))
				{
					if(targetName.trim().length()==0)
						mob.tell(L("You don't see that here."));
					else
					if(!CMLib.flags().isSleeping(mob)) // no idea why this is here :(
						mob.tell(L("You don't see anything called '@x1' here.",targetName));
					else // this was added for clan donate (and other things I'm sure) while sleeping.
						mob.tell(L("You don't see '@x1' in your dreams.",targetName));
				}
				else
					mob.tell(mob,target,null,L("You can't do that to <T-NAMESELF>."));
			}
			return null;
		}
		return (Item)target;
	}

	@Override
	public int compareTo(CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	protected void cloneFix(Ability E)
	{
	}

	@Override
	public CMObject copyOf()
	{
		try
		{
			final StdAbility E=(StdAbility)this.clone();
			//CMClass.bumpCounter(E,CMClass.CMObjectType.ABILITY);//removed for mem & perf
			E.cloneFix(this);
			return E;

		}
		catch(final CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	}

	@Override
	public boolean proficiencyCheck(MOB mob, int adjustment, boolean auto)
	{
		if(auto)
		{
			isAnAutoEffect=true;
			if((mob!=null)&&(!mob.isMine(this)))
				setProficiency(100);
			return true;
		}

		isAnAutoEffect=false;
		int pctChance=proficiency();
		if(mob != null)
		{
			if(CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.SUPERSKILL))
				return true;
			final CharStats charStats = mob.charStats();
			pctChance += charStats.getAbilityAdjustment("prof+"+ID());
			pctChance += charStats.getAbilityAdjustment("prof+"+Ability.ACODE_DESCS[classificationCode()&Ability.ALL_ACODES]);
			pctChance += charStats.getAbilityAdjustment("prof+"+Ability.DOMAIN_DESCS[(classificationCode()&Ability.ALL_DOMAINS)>> 5]);
			pctChance += charStats.getAbilityAdjustment("prof+*");
		}
		
		if(pctChance>95)
			pctChance=95;
		if(pctChance<5)
			pctChance=5;

		if(adjustment>=0)
			pctChance+=adjustment;
		else
		if(CMLib.dice().rollPercentage()>(100+adjustment))
			return false;
		return (CMLib.dice().rollPercentage()<pctChance);
	}

	@Override
	public Physical affecting()
	{
		return affected;
	}

	@Override
	public void setAffectedOne(Physical P)
	{
		affected=P;
	}

	@Override
	public void unInvoke()
	{
		unInvoked=true;

		if(affected==null)
			return;
		final Physical being=affected;

		if(canBeUninvoked())
		{
			being.delEffect(this);
			if(being instanceof Room)
				((Room)being).recoverRoomStats();
			else
			if(being instanceof MOB)
			{
				final MOB M=(MOB)being;
				final Room R=M.location();
				if((R!=null)&&(R.isInhabitant(M)))
					R.recoverRoomStats();
				else
				{
					M.recoverPhyStats();
					M.recoverCharStats();
					M.recoverMaxState();
				}
			}
			else
				being.recoverPhyStats();
		}
	}

	@Override
	public boolean canBeUninvoked()
	{
		return canBeUninvoked;
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
	}

	@Override
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{
	}

	@Override
	public void affectCharState(MOB affectedMob, CharState affectableMaxState)
	{
	}

	@Override
	public MOB invoker()
	{
		return invoker;
	}

	@Override
	public void setInvoker(MOB mob)
	{
		invoker=mob;
	}

	protected int[] buildCostArray(MOB mob, int consumed, int minimum)
	{
		final int[] usageCosts=new int[3];
		int costDown=0;
		if(consumed>2)
		{
			costDown=getXLOWCOSTLevel(mob);
			if(costDown>=consumed)
				costDown=consumed/2;
			minimum=(minimum-costDown);
			if(minimum<5) 
				minimum=5;
			int freeDown=getXLOWFREECOSTLevel(mob) / 2;
			costDown += freeDown;
			minimum=(minimum-freeDown);
			if(minimum<0) 
				minimum=0;
		}
		final boolean useMana=CMath.bset(usageType(),Ability.USAGE_MANA);
		final boolean useMoves=CMath.bset(usageType(),Ability.USAGE_MOVEMENT);
		final boolean useHits=CMath.bset(usageType(),Ability.USAGE_HITPOINTS);
		int divider=1;
		if((useMana)&&(useMoves)&&(useHits)) 
			divider=3;
		else
		if((useMana)&&(useMoves)&&(!useHits)) 
			divider=2;
		else
		if((useMana)&&(!useMoves)&&(useHits)) 
			divider=2;
		else
		if((!useMana)&&(useMoves)&&(useHits)) 
			divider=2;

		if(useMana)
		{
			if(consumed==COST_ALL)
			{
				usageCosts[0]=(mob.maxState().getMana()-costDown);
				if(mob.baseState().getMana()>mob.maxState().getMana())
					usageCosts[0]=(mob.baseState().getMana()-costDown);
			}
			else
			if(consumed>COST_PCT)
				usageCosts[0]=(int)(Math.round(CMath.mul(mob.maxState().getMana(),CMath.div((COST_ALL-consumed),100.0)))-costDown);
			else
				usageCosts[0]=((consumed-costDown)/divider);
			if(usageCosts[0]<minimum) 
				usageCosts[0]=minimum;
		}
		if(useMoves)
		{
			if(consumed==COST_ALL)
			{
				usageCosts[1]=(mob.maxState().getMovement()-costDown);
				if(mob.baseState().getMovement()>mob.maxState().getMovement())
					usageCosts[1]=(mob.baseState().getMovement()-costDown);
			}
			else
			if(consumed>COST_PCT)
				usageCosts[1]=(int)(Math.round(CMath.mul(mob.maxState().getMovement(),CMath.div((COST_ALL-consumed),100.0)))-costDown);
			else
				usageCosts[1]=((consumed-costDown)/divider);
			if(usageCosts[1]<minimum) 
				usageCosts[1]=minimum;
		}
		if(useHits)
		{
			if(consumed==COST_ALL)
			{
				usageCosts[2]=(mob.maxState().getHitPoints()-costDown);
				if(mob.baseState().getHitPoints()>mob.maxState().getHitPoints())
					usageCosts[2]=(mob.baseState().getHitPoints()-costDown);
			}
			else
			if(consumed>COST_PCT)
				usageCosts[2]=(int)(Math.round(CMath.mul(mob.maxState().getHitPoints(),CMath.div((COST_ALL-consumed),100.0)))-costDown);
			else
				usageCosts[2]=((consumed-costDown)/divider);
			if(usageCosts[2]<minimum)
				usageCosts[2]=minimum;
		}
		return usageCosts;
	}

	protected Map<String, int[]> getHardOverrideManaCache()
	{
		Map<String,int[]> hardOverrideCache	= (Map<String,int[]>)Resources.getResource("SYSTEM_ABLEUSAGE_HARD_OVERRIDE_CACHE");
		if(hardOverrideCache == null)
		{
			hardOverrideCache = new Hashtable<String,int[]>();
			Resources.submitResource("SYSTEM_ABLEUSAGE_HARD_OVERRIDE_CACHE", hardOverrideCache);
		}
		return hardOverrideCache;
	}

	@Override
	public int[] usageCost(MOB mob, boolean ignoreClassOverride)
	{
		if(mob==null)
		{
			final Map<String,int[]> overrideCache=getHardOverrideManaCache();
			if(!overrideCache.containsKey(ID()))
			{
				final int[] usage=new int[3];
				Arrays.fill(usage,overrideMana());
				overrideCache.put(ID(), usage);
			}
			return overrideCache.get(ID());
		}
		if(usageType()==Ability.USAGE_NADA)
			return STATIC_USAGE_NADA;

		final int[][] abilityUsageCache=mob.getAbilityUsageCache(ID());
		final int myCacheIndex=ignoreClassOverride?Ability.CACHEINDEX_CLASSLESS:Ability.CACHEINDEX_NORMAL;
		final int[] myCache=abilityUsageCache[myCacheIndex];
		final boolean rebuildCache=(myCache==null);
		int consumed;
		int minimum;
		if(!rebuildCache && (myCache!=null ))
		{
			if(myCache.length==3)
				return myCache;
			consumed=myCache[0];
			minimum=myCache[1];
		}
		else
		{
			int diff=0;
			int lowest=Integer.MAX_VALUE;
			for(int c=0;c<mob.charStats().numClasses();c++)
			{
				final CharClass C=mob.charStats().getMyClass(c);
				final int qualifyingLevel=CMLib.ableMapper().getQualifyingLevel(C.ID(),true,ID());
				final int classLevel=mob.charStats().getClassLevel(C.ID());
				if((qualifyingLevel>=0)&&(classLevel>=qualifyingLevel))
				{
					diff+=(classLevel-qualifyingLevel);
					if(qualifyingLevel<lowest) 
						lowest=qualifyingLevel;
				}
			}
			if(lowest==Integer.MAX_VALUE)
			{
				lowest=CMLib.ableMapper().lowestQualifyingLevel(ID());
				if(lowest<0) 
					lowest=0;
			}

			Integer[] costOverrides=null;
			if(!ignoreClassOverride)
				costOverrides=CMLib.ableMapper().getCostOverrides(mob,ID());
			consumed=CMProps.getMaxManaException(ID());
			if(consumed==Short.MIN_VALUE) 
				consumed=CMProps.getIntVar(CMProps.Int.MANACOST);
			if(consumed<0) 
				consumed=(50+lowest);
			minimum=CMProps.getMinManaException(ID());
			if(minimum==Short.MIN_VALUE)
				minimum=CMProps.getIntVar(CMProps.Int.MANAMINCOST);
			if(minimum<0)
			{ 
				minimum=lowest; 
				if(minimum<5) 
					minimum=5;
			}
			if(diff>0) 
				consumed=(consumed - (consumed /10 * diff));
			if(consumed<minimum)
				consumed=minimum;
			if((overrideMana()>=0) && (CMProps.getMaxManaException(ID()) == Integer.MIN_VALUE))
				consumed=overrideMana();
			if((costOverrides!=null)&&(costOverrides[AbilityMapper.Cost.MANA.ordinal()]!=null))
			{
				consumed=costOverrides[AbilityMapper.Cost.MANA.ordinal()].intValue();
				if((consumed<minimum)&&(consumed>=0)) 
					minimum=consumed;
			}
		}
		final int[] usageCost=buildCostArray(mob,consumed,minimum);
		if(rebuildCache)
		{
			if(consumed > COST_PCT-1)
				abilityUsageCache[myCacheIndex]=new int[]{consumed,minimum};
			else
				abilityUsageCache[myCacheIndex]=usageCost;
		}
		return usageCost;
	}

	@Override
	public void helpProficiency(MOB mob, int adjustment)
	{
		if(mob==null)
			return;
		final Ability A=mob.fetchAbility(ID());
		if(A==null)
			return;

		if(!mob.isMonster())
		{
			CMLib.coffeeTables().bump(this,CoffeeTableRow.STAT_SKILLUSE);
			CMLib.achievements().possiblyBumpAchievement(mob, AchievementLibrary.Event.SKILLUSE, 1, this);
		}
		
		if(!A.isSavable())
			return;

		if((System.currentTimeMillis()-((StdAbility)A).lastCastHelp)<300000)
			return;

		if(!A.appropriateToMyFactions(mob))
			return;

		final int maxProficiency = CMLib.ableMapper().getMaxProficiency(mob,true,ID());
		if(A.proficiency()< maxProficiency)
		{
			final int currentProficiency=A.proficiency()+adjustment;
			if(((int)Math.round(Math.sqrt((mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)))*34.0*Math.random()))>=currentProficiency)
			{
				final int qualLevel=CMLib.ableMapper().qualifyingLevel(mob,A);
				final double adjustedChance;
				if((qualLevel<0)
				||(qualLevel>30))
					adjustedChance=100.1;
				else
				{
					final float fatigueFactor=(mob.curState().getFatigue() > CharState.FATIGUED_MILLIS ? 50.0f : 100.0f);
					final int maxLevel=CMProps.getIntVar(CMProps.Int.LASTPLAYERLEVEL);
					adjustedChance=fatigueFactor * CMath.div((maxLevel+1-qualLevel),((2*maxLevel)+(10*qualLevel)));
				}
				if(CMLib.dice().rollPercentage()<Math.round(adjustedChance))
				{
					// very important, since these can be autoinvoked affects (copies)!
					A.setProficiency(A.proficiency()+1);
					if((this!=A)&&(proficiency()<maxProficiency))
						setProficiency(A.proficiency());
					final Ability effA=mob.fetchEffect(ID());
					if((effA!=null) && (effA!=A) && (effA!=this)
					&&(effA.invoker()==mob)
					&&(effA.proficiency()<maxProficiency))
						effA.setProficiency(A.proficiency());
					if(mob.isAttributeSet(MOB.Attrib.AUTOIMPROVE))
						mob.tell(L("You become better at @x1.",A.name()));
					((StdAbility)A).lastCastHelp=System.currentTimeMillis();
				}
			}
		}
		else
			A.setProficiency(maxProficiency);
	}

	@Override
	public boolean preInvoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel, int secondsElapsed, double actionsRemaining)
	{
		return true;
	}

	@Override
	public boolean invoke(MOB mob, Physical target, boolean auto, int asLevel)
	{
		final Vector<String> V=new Vector<String>(1);
		if(target!=null)
			V.addElement(target.name());
		return invoke(mob,V,target,auto,asLevel);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical target, boolean auto, int asLevel)
	{
		//expertiseCache=null; // this was insane!
		if((mob!=null)&&(getXMAXRANGELevel(mob)>0))
			invoker=mob;
		if((!auto)&&(mob!=null))
		{
			isAnAutoEffect=false;

			// if you can't move, you can't cast! Not even verbal!
			if(!CMLib.flags().isAliveAwakeMobile(mob,false))
				return false;

			final Room room=mob.location();
			if((getTicksBetweenCasts()>0)
			&&(getTimeOfNextCast()>0)
			&&(System.currentTimeMillis()<getTimeOfNextCast())
			&&(room!=null)&&(room.getArea()!=null))
			{
				final TimeClock C=room.getArea().getTimeObj();
				if(C!=null)
					mob.tell(L("You must wait @x1 before you can do that again.",C.deriveEllapsedTimeString(getTimeOfNextCast()-System.currentTimeMillis())));
				return false;
			}

			if(CMath.bset(usageType(),Ability.USAGE_MOVEMENT)
			&&(CMLib.flags().isBound(mob)))
			{
				mob.tell(L("You are bound!"));
				return false;
			}

			final int[] consumed=usageCost(mob,false);
			if(mob.curState().getMana()<consumed[Ability.USAGEINDEX_MANA])
			{
				if(mob.maxState().getMana()==consumed[Ability.USAGEINDEX_MANA])
					mob.tell(L("You must be at full mana to do that."));
				else
					mob.tell(L("You don't have enough mana to do that."));
				return false;
			}
			if(mob.curState().getMovement()<consumed[Ability.USAGEINDEX_MOVEMENT])
			{
				if(mob.maxState().getMovement()==consumed[Ability.USAGEINDEX_MOVEMENT])
					mob.tell(L("You must be at full movement to do that."));
				else
					mob.tell(L("You don't have enough movement to do that.  You are too tired."));
				return false;
			}
			if(mob.curState().getHitPoints()<consumed[Ability.USAGEINDEX_HITPOINTS])
			{
				if(mob.maxState().getHitPoints()==consumed[Ability.USAGEINDEX_HITPOINTS])
					mob.tell(L("You must be at full health to do that."));
				else
					mob.tell(L("You don't have enough hit points to do that."));
				return false;
			}

			if((minCastWaitTime()>0)&&(lastCastHelp>0))
			{
				if((System.currentTimeMillis()-lastCastHelp)<minCastWaitTime())
				{
					if(minCastWaitTime()<=1000)
						mob.tell(L("You need a second to recover before doing that again."));
					else
					if(minCastWaitTime()<=5000)
						mob.tell(L("You need a few seconds to recover before doing that again."));
					else
						mob.tell(L("You need awhile to recover before doing that again."));
					return false;
				}
			}
			if(!checkComponents(mob))
				return false;
			mob.curState().adjMana(-consumed[0],mob.maxState());
			mob.curState().adjMovement(-consumed[1],mob.maxState());
			mob.curState().adjHitPoints(-consumed[2],mob.maxState());
			helpProficiency(mob, 0);
			final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_PREINVOKE, null);
			if(!mob.okMessage(mob, msg))
				return false;
			mob.executeMsg(mob, msg);
		}
		else
			isAnAutoEffect=true;
		return true;
	}

	protected boolean checkComponents(MOB mob)
	{
		if((mob!=null)
		&&(mob.session()!=null)
		&&(mob.soulMate()==null)
		&&(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.COMPONENTS))
		)
		{
			final Vector<AbilityComponent> componentsRequirements=(Vector<AbilityComponent>)CMLib.ableComponents().getAbilityComponentMap().get(ID().toUpperCase());
			if(componentsRequirements!=null)
			{
				final List<Object> components=CMLib.ableComponents().componentCheck(mob,componentsRequirements, false);
				if(components==null)
				{
					mob.tell(L("You lack the necessary materials to use this @x1, the requirements are: @x2.",
							Ability.ACODE_DESCS[classificationCode()&Ability.ALL_ACODES].toLowerCase(),
							CMLib.ableComponents().getAbilityComponentDesc(mob,ID())));
					return false;
				}
				CMLib.ableComponents().destroyAbilityComponents(components);
			}
		}
		return true;
	}

	protected Set<MOB> properTargets(MOB mob, Environmental givenTarget, boolean auto)
	{
		Set<MOB> h=CMLib.combat().properTargets(this,mob,auto);
		if((givenTarget instanceof MOB)
		&&(CMLib.flags().isInTheGame((MOB)givenTarget,true)))
		{
			if(h==null)
				h=new SHashSet();
			if(!h.contains(givenTarget))
				h.add((MOB)givenTarget);
		}
		return h;
	}

	protected List<MOB> properTargetList(MOB mob, Environmental givenTarget, boolean auto)
	{
		final Set<MOB> h=properTargets(mob,givenTarget,auto);
		final List<MOB> list=new ArrayList<MOB>(h.size());
		if(h.contains(mob))
		{
			h.remove(mob);
			list.add(mob);
		}
		list.addAll(h);
		return list;
	}

	protected int adjustMaliciousTickdownTime(final MOB mob, final Physical target, final int baseTicks, final int asLevel)
	{
		int tickDown = baseTicks;
		if((target!=null)
		&&(asLevel<=0)
		&&(mob!=null)
		&&(!(target instanceof Room)))
		{
			double levelDiff = CMath.div(mob.phyStats().level(),target.phyStats().level());
			levelDiff = Math.min(levelDiff,CMProps.getIntVar(CMProps.Int.EXPRATE));
			tickDown=(int)Math.round(CMath.mul(tickDown,levelDiff));
			if((tickDown>(CMProps.getTicksPerHour()/3))
			||(mob instanceof Deity))
				tickDown=(int)(CMProps.getTicksPerHour()/3);
		}

		if((tickDown>(CMProps.getIntVar(CMProps.Int.TICKSPERMUDDAY)))
		||(mob instanceof Deity))
			tickDown=(CMProps.getIntVar(CMProps.Int.TICKSPERMUDDAY));

		if(tickDown<2)
			tickDown=2;
		return tickDown;
	}
	
	protected int getMaliciousTickdownTime(final MOB mob, final Physical target, final int tickAdjustmentFromStandard, final int asLevel)
	{
		if(tickAdjustmentFromStandard>0)
			return tickAdjustmentFromStandard;
		return adjustMaliciousTickdownTime(mob,target,((int)Math.round(CMath.mul(adjustedLevel(mob,asLevel),1.3)))+25,asLevel);
	}

	public Ability maliciousAffect(MOB mob, Physical target, int asLevel, int tickAdjustmentFromStandard, int additionAffectCheckCode)
	{
		final Room room=mob.location();
		if(room==null) 
			return null;
		if(additionAffectCheckCode>=0)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.NO_EFFECT,additionAffectCheckCode,CMMsg.NO_EFFECT,null);
			if(room.okMessage(mob,msg))
			{
				room.send(mob,msg);
				if(msg.value()>0)
					return null;
			}
			else
				return null;
		}
		invoker=mob;
		final Ability newOne=(Ability)copyOf();
		((StdAbility)newOne).canBeUninvoked=true;
		tickAdjustmentFromStandard=getMaliciousTickdownTime(mob,target,tickAdjustmentFromStandard,asLevel);
		newOne.startTickDown(invoker,target,tickAdjustmentFromStandard);
		return newOne;
	}

	protected boolean beneficialWordsFizzle(MOB mob, Environmental target, String message)
	{
		// it didn't work, but tell everyone you tried.
		final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_SPEAK,"^T"+message+"^?");
		final Room room=mob.location();
		if(room==null)
			return false;
		if(room.okMessage(mob,msg))
			room.send(mob,msg);
		return false;
	}

	protected boolean beneficialVisualFizzle(MOB mob, Environmental target, String message)
	{
		// it didn't work, but tell everyone you tried.
		final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_OK_VISUAL,message);
		final Room room=mob.location();
		if(room==null)
			return false;
		if(room.okMessage(mob,msg))
			room.send(mob,msg);

		return false;
	}

	protected boolean maliciousFizzle(MOB mob, Environmental target, String message)
	{
		// it didn't work, but tell everyone you tried.
		final String targetMessage;
		if((target instanceof MOB)
		&&(mob!=target)
		&&(((MOB)target).isAttributeSet(Attrib.NOBATTLESPAM)))
			targetMessage=null;
		else
			targetMessage=message;
		final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_OK_VISUAL|CMMsg.MASK_MALICIOUS,message,targetMessage,message);
		final Room room=mob.location();
		if(room==null)
			return false;
		CMLib.color().fixSourceFightColor(msg);
		if(room.okMessage(mob,msg))
			room.send(mob,msg);
		return false;
	}

	protected int adjustBeneficialTickdownTime(final MOB mob, final Environmental target, final int baseTicks)
	{
		int tickDown = baseTicks;
		if((tickDown>(CMProps.getIntVar(CMProps.Int.TICKSPERMUDDAY)))
		||(mob instanceof Deity))
			tickDown=(CMProps.getIntVar(CMProps.Int.TICKSPERMUDDAY));
		if(tickDown<5)
			tickDown=5;
		return tickDown;
	}

	protected int getBeneficialTickdownTime(MOB mob, Environmental target, int tickAdjustmentFromStandard, int asLevel)
	{
		if(tickAdjustmentFromStandard>0)
			return tickAdjustmentFromStandard;
		return adjustBeneficialTickdownTime(mob,target,(adjustedLevel(mob,asLevel)*5)+60);
	}

	public Ability beneficialAffect(MOB mob, Physical target, int asLevel, int tickAdjustmentFromStandard)
	{
		invoker=mob;
		final Ability newOne=(Ability)this.copyOf();
		((StdAbility)newOne).canBeUninvoked=true;
		tickAdjustmentFromStandard=getBeneficialTickdownTime(mob,target,tickAdjustmentFromStandard,asLevel);
		newOne.startTickDown(invoker,target,tickAdjustmentFromStandard);
		return newOne;
	}

	protected void spreadImmunity(MOB mob)
	{
		if((mob==null)||(mob.fetchEffect(ID())!=null))
			return;
		Ability A=mob.fetchEffect("TemporaryImmunity");
		if(A==null)
		{
			A=CMClass.getAbility("TemporaryImmunity");
			A.setSavable(false);
			A.makeLongLasting();
			mob.addEffect(A);
			A.makeLongLasting();
		}
		A.setMiscText("+"+ID());
	}

	@Override
	public boolean autoInvocation(MOB mob, boolean force)
	{
		if(isAutoInvoked())
		{
			if(!force)
			{
				final PlayerStats pStats = mob.playerStats();
				if((pStats != null) && (pStats.isOnAutoInvokeList(ID())))
					return false;
			}
			final Ability thisAbility=mob.fetchEffect(ID());
			if(thisAbility!=null)
				return false;
			final StdAbility thatAbility=(StdAbility)copyOf();
			thatAbility.canBeUninvoked=true;
			thatAbility.setSavable(false);
			thatAbility.setInvoker(mob);
			thatAbility.isAnAutoEffect=true;
			mob.addEffect(thatAbility);
			return true;
		}
		return false;
	}

	@Override
	public void makeNonUninvokable()
	{
		unInvoked=false;
		canBeUninvoked=false;
		savable=true;
	}

	@Override
	public String accountForYourself()
	{
		return name();
	}

	public int getTickDownRemaining()
	{
		return tickDown;
	}

	public void setTickDownRemaining(int newTick)
	{
		tickDown=newTick;
	}

	@Override
	public int getTickStatus()
	{
		return Tickable.STATUS_NOT;
	}

	@Override
	public boolean canBeTaughtBy(MOB teacher, MOB student)
	{
		if(teacher.isAttributeSet(MOB.Attrib.NOTEACH))
		{
			teacher.tell(L("You are refusing to teach right now."));
			student.tell(L("@x1 is refusing to teach right now.",teacher.name()));
			return false;
		}
		if(CMLib.flags().isSleeping(teacher)||CMLib.flags().isSitting(teacher))
		{
			teacher.tell(L("You need to stand up to teach."));
			student.tell(L("@x1 needs to stand up to teach.",teacher.name()));
			return false;
		}
		if(teacher.isInCombat())
		{
			student.tell(L("@x1 seems busy right now.",teacher.name()));
			teacher.tell(L("Not while you are fighting!"));
			return false;
		}
		final Ability yourAbility=teacher.fetchAbility(ID());
		if(yourAbility!=null)
		{
			final int prof25=(int)Math.round(CMath.mul(CMLib.ableMapper().getMaxProficiency(student,true,yourAbility.ID()), .25));
			if(yourAbility.proficiency()<prof25)
			{
				teacher.tell(L("You are not proficient enough to teach '@x1'",name()));
				student.tell(L("@x1 is not proficient enough to teach '@x2'.",teacher.name(),name()));
				return false;
			}
			return true;
		}
		teacher.tell(L("You don't know '@x1'.",name()));
		student.tell(L("@x1 doesn't know '@x2'.",teacher.name(),name()));
		return false;
	}

	@Override
	public String requirements(MOB mob)
	{
		final ExpertiseLibrary.SkillCost cost=getTrainingCost(mob);
		return cost.requirements(mob);
	}

	@Override
	public boolean canBeLearnedBy(MOB teacher, MOB student)
	{
		final ExpertiseLibrary.SkillCost cost=getTrainingCost(student);
		if(!cost.doesMeetCostRequirements(student))
		{
			final String ofWhat=cost.costType(student);
			if(teacher != null)
				teacher.tell(L("@x1 does not have enough @x2 to learn '@x3'.",student.name(),ofWhat,name()));
			student.tell(L("You do not have enough @x1.",ofWhat));
			return false;
		}
		if((student.isAttributeSet(MOB.Attrib.NOTEACH))
		&&((!student.isMonster())||(!student.willFollowOrdersOf(teacher))))
		{
			if(teacher != null)
				teacher.tell(L("@x1 is refusing training at this time.",student.name()));
			student.tell(L("You are refusing training at this time."));
			return false;
		}
		final int qLevel=CMLib.ableMapper().qualifyingLevel(student,this);
		if(qLevel<0)
		{
			if(teacher != null)
				teacher.tell(L("@x1 is not the right class to learn '@x2'.",student.name(),name()));
			student.tell(L("You are not the right class to learn '@x1'.",name()));
			return false;
		}
		if((!student.charStats().getCurrentClass().leveless())
		&&(!CMLib.ableMapper().qualifiesByLevel(student,this))
		&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.LEVELS)))
		{
			if(teacher != null)
				teacher.tell(L("@x1 is not high enough level to learn '@x2'.",student.name(),name()));
			student.tell(L("You are not high enough level to learn '@x1'.",name()));
			return false;
		}
		if(student.charStats().getStat(CharStats.STAT_INTELLIGENCE)<2)
		{
			if(teacher != null)
				teacher.tell(L("@x1 is too stupid to learn new skills.",student.name()));
			student.tell(L("You are too stupid to learn new skills."));
			return false;
		}
		if(qLevel>(student.charStats().getStat(CharStats.STAT_INTELLIGENCE)+18))
		{
			if(teacher != null)
				teacher.tell(L("@x1 is not smart enough to learn level @x2 skills.",student.name(),qLevel+""));
			student.tell(L("You are not of high enough intelligence to learn level @x2 skills.",qLevel+""));
			return false;
		}
		final Ability yourAbility=student.fetchAbility(ID());
		final Ability teacherAbility=(teacher != null) ? teacher.fetchAbility(ID()) : null;
		if(yourAbility!=null)
		{
			if(teacher != null)
				teacher.tell(L("@x1 already knows '@x2'.",student.name(),name()));
			student.tell(L("You already know '@x1'.",name()));
			return false;
		}

		if(teacher != null)
		{
			if(teacherAbility!=null)
			{
				final int prof25=(int)Math.round(CMath.mul(CMLib.ableMapper().getMaxProficiency(student,true,teacherAbility.ID()), .25));
				if(teacherAbility.proficiency()<prof25)
				{
					teacher.tell(L("You aren't proficient enough to teach '@x1'.",name()));
					student.tell(L("@x1 isn't proficient enough to teach you '@x2'.",teacher.name(),name()));
					return false;
				}
			}
			else
			{
				student.tell(L("@x1 does not know anything about that.",teacher.name()));
				teacher.tell(L("You don't know that."));
				return false;
			}
		}
		if(student.isInCombat())
		{
			if(teacher != null)
				teacher.tell(L("@x1 seems busy right now.",student.name()));
			student.tell(L("Not while you are fighting!"));
			return false;
		}

		if(CMLib.flags().isSleeping(student)||CMLib.flags().isSitting(student))
		{
			student.tell(L("You need to stand up and be alert to learn."));
			if(teacher != null)
				teacher.tell(L("@x1 needs to stand up to be taught about that.",student.name()));
			return false;
		}

		final String extraMask=CMLib.ableMapper().getApplicableMask(student,this);
		if((extraMask.length()>0)&&(!CMLib.masking().maskCheck(extraMask,student,true)))
		{
			final String reason="requirements: "+CMLib.masking().maskDesc(extraMask);
			student.tell(L("You may not learn '@x1' at this time due to the @x2.",name(),reason));
			if(teacher != null)
				teacher.tell(L("@x1 does not fit the '@x2' @x3.",student.name(),name(),reason));
			return false;
		}

		final DVector prereqs=CMLib.ableMapper().getUnmetPreRequisites(student,this);
		if((prereqs!=null)&&(prereqs.size()>0))
		{
			final String names=CMLib.ableMapper().formatPreRequisites(prereqs);
			student.tell(L("You must learn @x1 before you can gain @x2.",names,name()));
			if(teacher != null)
				teacher.tell(L("@x1 has net learned the pre-requisites to @x2 yet.",student.name(),name()));
			return false;
		}

		return true;
	}

	protected Map<MOB,MOB> saveCombatState(MOB mob, boolean andFollowers)
	{
		Map<MOB,MOB> map = new TreeMap<MOB,MOB>();
		HashSet<MOB> fols = new HashSet<MOB>();
		fols.add(mob);
		if(andFollowers)
			mob.getGroupMembers(fols);
		for(MOB M : fols)
			map.put(M,M.getVictim());
		return map;
	}
	
	protected void restoreCombatState(Map<MOB,MOB> map)
	{
		for(MOB M : map.keySet())
		{
			M.setVictim(map.get(M));
		}
	}
	
	protected int verbalCastCode(MOB mob, Physical target, boolean auto)
	{
		int affectType=CMMsg.MSG_CAST_VERBAL_SPELL;
		if(castingQuality(mob,target)==Ability.QUALITY_MALICIOUS)
			affectType=CMMsg.MSG_CAST_ATTACK_VERBAL_SPELL;
		if(auto)
			affectType=affectType|CMMsg.MASK_ALWAYS;
		return affectType;
	}

	protected int verbalSpeakCode(MOB mob, Physical target, boolean auto)
	{
		int affectType=CMMsg.MSG_NOISE|CMMsg.MASK_MOUTH;
		if(castingQuality(mob,target)==Ability.QUALITY_MALICIOUS)
			affectType=CMMsg.MSG_NOISE|CMMsg.MASK_MOUTH|CMMsg.MASK_MALICIOUS;
		if(auto)
			affectType=affectType|CMMsg.MASK_ALWAYS;
		return affectType;
	}

	protected int verbalCastMask(MOB mob,Physical target, boolean auto)
	{
		return verbalCastCode(mob,target,auto)&CMMsg.MAJOR_MASK;
	}

	protected int somanticCastCode(MOB mob, Physical target, boolean auto)
	{
		int affectType=CMMsg.MSG_CAST_SOMANTIC_SPELL;
		if(castingQuality(mob,target)==Ability.QUALITY_MALICIOUS)
			affectType=CMMsg.MSG_CAST_ATTACK_SOMANTIC_SPELL;
		if(auto)
			affectType=affectType|CMMsg.MASK_ALWAYS;
		return affectType;
	}

	protected int somanticCastMask(MOB mob, Physical target, boolean auto)
	{
		return somanticCastCode(mob,target,auto)&CMMsg.MAJOR_MASK;
	}

	@Override
	public boolean canBePracticedBy(MOB teacher, MOB student)
	{
		if((practicesToPractice(student)>0)&&(student.getPractices()<practicesToPractice(student)))
		{
			teacher.tell(L("@x1 does not have enough practices to practice '@x2'.",student.name(),name()));
			student.tell(L("You do not have enough practices."));
			return false;
		}

		if(teacher.isAttributeSet(MOB.Attrib.NOTEACH))
		{
			teacher.tell(L("You are refusing to teach right now."));
			student.tell(L("@x1 is refusing to teach right now.",teacher.name()));
			return false;
		}
		if((student.isAttributeSet(MOB.Attrib.NOTEACH))
		&&((!student.isMonster())||(!student.willFollowOrdersOf(teacher))))
		{
			teacher.tell(L("@x1 is refusing training at this time.",student.name()));
			student.tell(L("You are refusing training at this time."));
			return false;
		}

		final Ability yourAbility=student.fetchAbility(ID());
		final Ability teacherAbility=teacher.fetchAbility(ID());
		if(yourAbility==null)
		{
			teacher.tell(L("@x1 has not gained '@x2' yet.",student.name(),name()));
			student.tell(L("You havn't gained '@x1' yet.",name()));
			return false;
		}

		if(teacherAbility==null)
		{
			student.tell(L("@x1 does not know anything about '@x2'.",teacher.name(),name()));
			teacher.tell(L("You don't know '@x1'.",name()));
			return false;
		}

		final int prof75=(int)Math.round(CMath.mul(CMLib.ableMapper().getMaxProficiency(student,true,yourAbility.ID()), .75));
		if(yourAbility.proficiency()>teacherAbility.proficiency())
		{
			teacher.tell(L("You aren't proficient enough to teach any more about '@x1'.",name()));
			student.tell(L("@x1 isn't proficient enough to teach any more about '@x2'.",teacher.name(),name()));
			return false;
		}
		else
		if(yourAbility.proficiency()>prof75-1)
		{
			teacher.tell(L("You can't teach @x1 any more about '@x2'.",student.charStats().himher(),name()));
			student.tell(L("You can't learn any more about '@x1' except through dilligence.",name()));
			return false;
		}

		final int prof25=(int)Math.round(CMath.mul(CMLib.ableMapper().getMaxProficiency(student,true,teacherAbility.ID()), .25));
		if(teacherAbility.proficiency()<prof25)
		{
			teacher.tell(L("You aren't proficient enough to teach '@x1'.",name()));
			student.tell(L("@x1 isn't proficient enough to teach you '@x2'.",teacher.name(),name()));
			return false;
		}
		if(CMLib.flags().isSleeping(student)||CMLib.flags().isSitting(student))
		{
			student.tell(L("You need to stand up to practice."));
			teacher.tell(L("@x1 needs to stand up to practice that.",student.name()));
			return false;
		}
		if(student.isInCombat())
		{
			teacher.tell(L("@x1 seems busy right now.",student.name()));
			student.tell(L("Not while you are fighting!"));
			return false;
		}

		return true;
	}

	@Override
	public void teach(MOB teacher, MOB student)
	{
		if(student.fetchAbility(ID())==null)
		{
			final ExpertiseLibrary.SkillCost cost=getTrainingCost(student);
			if(!cost.doesMeetCostRequirements(student))
				return;
			cost.spendSkillCost(student);
			final Ability newAbility=(Ability)newInstance();
			final int prof75=(int)Math.round(CMath.mul(CMLib.ableMapper().getMaxProficiency(student,true,newAbility.ID()), .75));
			newAbility.setProficiency((int)Math.round(CMath.mul(proficiency(),((CMath.div(teacher.charStats().getStat(CharStats.STAT_WISDOM)+student.charStats().getStat(CharStats.STAT_INTELLIGENCE),100.0))))));
			if(newAbility.proficiency()>prof75)
				newAbility.setProficiency(prof75);
			final int defProficiency=CMLib.ableMapper().getDefaultProficiency(student.charStats().getCurrentClass().ID(),true,ID());
			if((defProficiency>0)&&(defProficiency>newAbility.proficiency()))
				newAbility.setProficiency(defProficiency);
			final String defParms=CMLib.ableMapper().getDefaultParm(student.charStats().getCurrentClass().ID(), true, ID());
			newAbility.setMiscText(defParms);
			student.addAbility(newAbility);
			newAbility.autoInvocation(student, false);
		}
		student.recoverCharStats();
		student.recoverPhyStats();
		student.recoverMaxState();
	}

	@Override
	public void practice(MOB teacher, MOB student)
	{
		if(student.getPractices()<practicesToPractice(student))
			return;

		final Ability yourAbility=student.fetchAbility(ID());
		if(yourAbility!=null)
		{
			final int prof75=(int)Math.round(CMath.mul(CMLib.ableMapper().getMaxProficiency(student,true,yourAbility.ID()), .75));
			if(yourAbility.proficiency()<prof75)
			{
				student.setPractices(student.getPractices()-practicesToPractice(student));
				int newProf = yourAbility.proficiency()+(int)Math.round(25.0*(CMath.div(teacher.charStats().getStat(CharStats.STAT_WISDOM)+student.charStats().getStat(CharStats.STAT_INTELLIGENCE),36.0)));
				if(newProf > prof75)
					newProf=prof75;
				yourAbility.setProficiency(newProf);
				final Ability yourEffect=student.fetchEffect(ID());
				if((yourEffect!=null)&&(yourEffect.invoker()==student))
					yourEffect.setProficiency(yourAbility.proficiency());
			}
		}
	}

	@Override
	public void makeLongLasting()
	{
		tickDown=Integer.MAX_VALUE;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		return;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		return true;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((unInvoked)&&(canBeUninvoked()))
			return false;

		if((canBeUninvoked())
		&&(tickID==Tickable.TICKID_MOB)
		&&(tickDown!=Integer.MAX_VALUE))
		{
			if(tickDown<0)
				return !unInvoked;
			if((--tickDown)<=0)
			{
				tickDown=-1;
				unInvoke();
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean appropriateToMyFactions(MOB mob)
	{
		for(final Enumeration e=mob.factions();e.hasMoreElements();)
		{
			final String factionID=(String)e.nextElement();
			final Faction F=CMLib.factions().getFaction(factionID);
			if((F!=null)&&F.hasUsage(this))
				return F.canUse(mob,this);
		}
		return true;
	}

	@Override
	public boolean isGeneric()
	{
		return false;
	}

	@Override
	public int getSaveStatIndex()
	{
		return getStatCodes().length;
	}

	private static final String[]	CODES			= { "CLASS", "TEXT" };
	private static final String[]	INTERNAL_CODES	= { "TICKDOWN","LEVEL","ISANAUTOEFFECT" };

	@Override
	public String[] getStatCodes()
	{
		return CODES;
	}

	@Override
	public boolean isStat(String code)
	{
		return CMParms.indexOf(getStatCodes(), code.toUpperCase().trim()) >= 0;
	}

	protected int getCodeNum(String code)
	{
		for(int i=0;i<CODES.length;i++)
		{
			if(code.equalsIgnoreCase(CODES[i]))
				return i;
		}
		return -1;
	}

	protected int getInternalCodeNum(String code)
	{
		for(int i=0;i<INTERNAL_CODES.length;i++)
		{
			if(code.equalsIgnoreCase(INTERNAL_CODES[i]))
				return i;
		}
		return -1;
	}

	@Override
	public String getStat(String code)
	{
		switch(getCodeNum(code))
		{
		case 0:
			return ID();
		case 1:
			return text();
		default:
			switch(getInternalCodeNum(code))
			{
			case 0:
				return Integer.toString(tickDown);
			case 1:
				return "0";
			case 2:
				return Boolean.toString(isAnAutoEffect);
			default:
				break;
			}
			break;
		}
		return "";
	}

	@Override
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code))
		{
		case 0:
			return;
		case 1:
			setMiscText(val);
			break;
		default:
			switch(getInternalCodeNum(code))
			{
			case 0:
				tickDown = CMath.s_int(val);
				break;
			case 1:
				break;
			case 2:
				isAnAutoEffect = CMath.s_bool(val);
				break;
			default:
				break;
			}
			break;
		}
	}

	@Override
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof StdAbility))
			return false;
		final String[] codes=getStatCodes();
		for(int i=0;i<codes.length;i++)
		{
			if(!E.getStat(codes[i]).equals(getStat(codes[i])))
				return false;
		}
		return true;
	}
}
