package com.planet_ink.coffee_mud.Abilities;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DisFlag;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.AbilityMapping;
import com.planet_ink.coffee_mud.core.interfaces.CostDef.CostType;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB.Attrib;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2001-2024 Bo Zimmerman

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

	private static final int[] STATIC_USAGE_NADA= new int[Ability.USAGEINDEX_TOTAL];

	public StdAbility()
	{
		super();
		//CMClass.bumpCounter(this,CMClass.CMObjectType.ABILITY);//removed for mem & perf
	}

	/*
	protected void finalize()
	{
		CMClass.unbumpCounter(this, CMClass.CMObjectType.ABILITY);
	}// removed for mem & perf
	*/

	@Override
	public CMObject newInstance()
	{
		try
		{
			return this.getClass().getDeclaredConstructor().newInstance();
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
	public void setImage(final String newImage)
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

	protected boolean ignoreCompounding()
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

	protected void setTimeOfNextCast(final long absoluteTime)
	{
	}

	protected CostDef getRawTrainingCost()
	{
		return CMProps.getNormalSkillGainCost(ID());
	}

	public void setTickDown(final int down)
	{
		this.tickDown = down;
	}

	@Override
	public CostManager getTrainingCost(final MOB mob)
	{
		int qualifyingLevel;
		int playerLevel=1;
		if(mob!=null)
		{
			final Integer[] O=CMLib.ableMapper().getCostOverrides(mob,ID());
			if(O!=null)
			{
				Integer val=O[AbilityMapper.AbilCostType.TRAIN.ordinal()];
				if(val!=null)
					return CMLib.utensils().createCostManager(CostType.TRAIN,Double.valueOf(val.intValue()));
				val=O[AbilityMapper.AbilCostType.PRAC.ordinal()];
				if(val!=null)
					return CMLib.utensils().createCostManager(CostType.PRACTICE,Double.valueOf(val.intValue()));
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
		final CostDef rawCost=getRawTrainingCost();
		if(rawCost==null)
			return CMLib.utensils().createCostManager(CostType.TRAIN,Double.valueOf(1.0));
		final double[] vars=new double[]{ qualifyingLevel,playerLevel};
		final double value=CMath.parseMathExpression(rawCost.costDefinition(),vars);
		return CMLib.utensils().createCostManager(new CostDef.Cost(value, rawCost.type(), rawCost.typeCurrency()));
	}

	protected int practicesToPractice(final MOB mob)
	{
		if(mob!=null)
		{
			final Integer[] O=CMLib.ableMapper().getCostOverrides(mob,ID());
			if((O!=null)&&(O[AbilityMapper.AbilCostType.PRACPRAC.ordinal()]!=null))
				return O[AbilityMapper.AbilCostType.PRACPRAC.ordinal()].intValue();
		}
		return iniPracticesToPractice();
	}

	protected int iniPracticesToPractice()
	{
		return 1;
	}

	protected void setTimeOfNextCast(final MOB caster)
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
	public boolean mayBeEnchanted()
	{
		if((CMLib.ableMapper().lowestQualifyingLevel(ID())>POWER_LEVEL_THRESHOLD)
		||(usageCost(null,true)[0]>POWER_COST_THRESHOLD)
		||(overrideMana()>POWER_OVERRIDE_THRESHOLD)
		||(CMath.bset(flags(), FLAG_CLANMAGIC)))
			return false;
		return true;
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

	protected int castingQuality(final MOB mob, final Physical target, final int abstractQuality)
	{
		if((target!=null)&&(target.fetchEffect(ID())!=null))
			return Ability.QUALITY_INDIFFERENT;
		if(isAutoInvoked())
			return Ability.QUALITY_INDIFFERENT;
		if(mob != null)
		{
			if((target!=null)
			&&(mob.getVictim()==target))
			{
				if((minRange()>0)&&(mob.rangeToTarget()<minRange()))
					return Ability.QUALITY_INDIFFERENT;
				if(mob.rangeToTarget()>maxRange())
					return Ability.QUALITY_INDIFFERENT;
			}
			final Room R=mob.location();
			if((R!=null)
			&&(this.canAffect(CAN_ROOMS)||this.canTarget(CAN_ROOMS))
			&&(R.fetchEffect(ID())!=null))
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
	public int castingQuality(final MOB mob, final Physical target)
	{
		return castingQuality(mob,target,abstractQuality());
	}

	protected synchronized int expertise(final MOB mob, final Ability A, final ExpertiseLibrary.XType code)
	{
		if((mob!=null)
		&&(A.isNowAnAutoEffect()
			||(A.canBeUninvoked())
			||A.isAutoInvoked()))
		{
			int xlevel = CMLib.expertises().getExpertiseLevelCached(mob, A.ID(), code);
			final CharStats charStats=mob.charStats(); // circumstantial bonuses
			final String codeStr="X"+code.name()+"+";
			xlevel += charStats.getAbilityAdjustment(codeStr+ID().toUpperCase());
			xlevel += charStats.getAbilityAdjustment(codeStr+Ability.ACODE.DESCS.get(classificationCode()&Ability.ALL_ACODES));
			xlevel += charStats.getAbilityAdjustment(codeStr+Ability.DOMAIN.DESCS.get((classificationCode()&Ability.ALL_DOMAINS)>> 5));
			xlevel += charStats.getAbilityAdjustment(codeStr+"*");
			return xlevel;
		}
		return 0;
	}

	protected int getX1Level(final MOB mob)
	{
		return expertise(mob, this, ExpertiseLibrary.XType.X1);
	}

	protected int getX2Level(final MOB mob)
	{
		return expertise(mob, this, ExpertiseLibrary.XType.X2);
	}

	protected int getX3Level(final MOB mob)
	{
		return expertise(mob, this, ExpertiseLibrary.XType.X3);
	}

	protected int getX4Level(final MOB mob)
	{
		return expertise(mob, this, ExpertiseLibrary.XType.X4);
	}

	protected int getX5Level(final MOB mob)
	{
		return expertise(mob, this, ExpertiseLibrary.XType.X5);
	}

	protected int getXLEVELLevel(final MOB mob)
	{
		return expertise(mob, this, ExpertiseLibrary.XType.LEVEL);
	}

	protected int getXLOWCOSTLevel(final MOB mob)
	{
		return expertise(mob, this, ExpertiseLibrary.XType.LOWCOST);
	}

	protected int getXLOWFREECOSTLevel(final MOB mob)
	{
		return expertise(mob, this, ExpertiseLibrary.XType.LOWFREECOST);
	}

	protected int getXMAXRANGELevel(final MOB mob)
	{
		return expertise(mob, this, ExpertiseLibrary.XType.MAXRANGE);
	}

	protected int getXTIMELevel(final MOB mob)
	{
		return expertise(mob, this, ExpertiseLibrary.XType.TIME);
	}

	protected int getXPCOSTLevel(final MOB mob)
	{
		return expertise(mob, this, ExpertiseLibrary.XType.XPCOST);
	}

	protected int getXPCOSTAdjustment(final MOB mob, final int xpLoss)
	{
		final int xLevel=getXPCOSTLevel(mob);
		if(xLevel<=0)
			return xpLoss;
		return xpLoss-(int)Math.round(CMath.mul(xpLoss,CMath.mul(.05,xLevel)));
	}

	protected int adjustedMaxInvokerRange(final int max)
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
	protected int canAffectCode()
	{
		return Ability.CAN_AREAS|
				 Ability.CAN_ITEMS|
				 Ability.CAN_MOBS|
				 Ability.CAN_ROOMS|
				 Ability.CAN_EXITS;
	}

	/**
	 * Designates whether, when invoked as a skill, what sort of objects this
	 * ability can effectively target. Uses the Ability.CAN_* constants.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability
	 * @return a mask showing the type of objects this ability can target
	 */
	protected int canTargetCode()
	{
		return Ability.CAN_AREAS|
				 Ability.CAN_ITEMS|
				 Ability.CAN_MOBS|
				 Ability.CAN_ROOMS|
				 Ability.CAN_EXITS;
	}

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
	public void setExpirationDate(final long time)
	{
		if(time>System.currentTimeMillis())
			tickDown=(int)((time-System.currentTimeMillis())/CMProps.getTickMillis());
		else
			tickDown=(int)Math.round(CMath.div(time,CMProps.getTickMillis()));
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
	public void setSavable(final boolean truefalse)
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
	public void setName(final String newName)
	{
	}

	@Override
	public void setDisplayText(final String newDisplayText)
	{
	}

	@Override
	public void setDescription(final String newDescription)
	{
	}

	@Override
	public int abilityCode()
	{
		return 0;
	}

	@Override
	public void setAbilityCode(final int newCode)
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
	public void setMiscText(final String newMiscText)
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
	public void setProficiency(final int newProficiency)
	{
		proficiency=newProficiency;
		if(proficiency>100)
			proficiency=100;
	}

	protected int addedTickTime(final MOB invokerMOB, final int baseTickTime)
	{
		return (int)Math.round(CMath.mul(baseTickTime,CMath.mul(getXTIMELevel(invokerMOB),0.20)));
	}

	@Override
	public void startTickDown(final MOB invokerMOB, final Physical affected, int tickTime)
	{
		if(invokerMOB!=null)
			invoker=invokerMOB;

		savable=false; // makes it so that the effect does not save!

		if((classificationCode() != (Ability.ACODE_COMMON_SKILL|Ability.DOMAIN_CRAFTINGSKILL))
		&&(classificationCode() != (Ability.ACODE_COMMON_SKILL|Ability.DOMAIN_GATHERINGSKILL))
		&&(classificationCode() != (Ability.ACODE_COMMON_SKILL|Ability.DOMAIN_EPICUREAN))
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
			{
				affected.addEffect(this);
				final int ecap=CMProps.getIntVar(CMProps.Int.EFFECTCAP);
				if(ecap>0)
				{
					final List<Ability> candidates = new ArrayList<Ability>(mob.numEffects());
					for(final Enumeration<Ability> e = mob.effects();e.hasMoreElements();)
					{
						final Ability eA=e.nextElement();
						if((eA!=null)
						&&(eA.canBeUninvoked())
						&&(eA.invoker()!=null))
							candidates.add(eA);
					}
					if(candidates.size()>ecap)
					{
						final Ability deadA=candidates.get(CMLib.dice().roll(1, candidates.size(), -1));
						if(deadA!=null)
							deadA.unInvoke();
					}
				}
			}
			if(mob.isPlayer())
				CMLib.achievements().possiblyBumpAchievement(mob, AchievementLibrary.Event.EFFECTSHAD, 1, this);
			try
			{
				room.recoverRoomStats();
			}
			catch(final Exception e)
			{
				Log.errOut(e);
			}
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

	protected void commonTelL(final MOB mob, final Environmental target, final Environmental tool, String str, final String... vars)
	{
		if(mob.isMonster())
		{
			str = CMStrings.replaceWord(str, "you are", "I am");
			str = CMStrings.replaceWord(str, "you", "I");
			str = CMStrings.replaceWord(str, "your", "my");
			str = CMStrings.replaceWord(str, "you've", "I've");
			str = L(str,vars);
			if(target!=null)
				str=CMStrings.replaceAll(str,"<T-NAME>",target.name());
			if(tool!=null)
				str=CMStrings.replaceAll(str,"<O-NAME>",tool.name());
			CMLib.commands().postSay(mob,null,str,false,false);
			return;
		}
		mob.tell(mob,target,tool,L(str,vars));
	}

	protected void commonTelL(final MOB mob, String str, final String... vars)
	{
		if(mob==null)
			return;

		if(mob.isMonster())
		{
			str = CMStrings.replaceWord(str, "you are", "I am");
			str = CMStrings.replaceWord(str, "you", "I");
			str = CMStrings.replaceWord(str, "your", "my");
			str = CMStrings.replaceWord(str, "you've", "I've");
			str = L(str,vars);
			CMLib.commands().postSay(mob,null,str,false,false);
			return;
		}

		mob.tell(L(str,vars));
	}

	protected void commonTell(final MOB mob, String str)
	{
		if(mob==null)
			return;

		if(mob.isMonster())
		{
			str = CMStrings.replaceWord(str, "you are", "I am");
			str = CMStrings.replaceWord(str, "you", "I");
			str = CMStrings.replaceWord(str, "your", "my");
			str = CMStrings.replaceWord(str, "you've", "I've");
			CMLib.commands().postSay(mob,null,str,false,false);
			return;
		}

		mob.tell(str);
	}

	public boolean disregardsArmorCheck(final MOB mob)
	{
		// armor checks are mostly handled by classes.
		// this is here for cases when someone gets a skill without the class to keep it in check.
		// that's why you disregard the armor check with you DO qualify
		return ((mob==null)
				||(mob.isMonster())
				||(CMLib.ableMapper().qualifiesByLevel(mob,this)));
	}

	protected int getPersonalLevelAdjustments(final MOB caster)
	{
		final CharStats charStats = caster.charStats();
		return  charStats.getAbilityAdjustment("LEVEL+"+ID().toUpperCase())
			+ charStats.getAbilityAdjustment("LEVEL+"+Ability.ACODE.DESCS.get(classificationCode()&Ability.ALL_ACODES))
			+ charStats.getAbilityAdjustment("LEVEL+"+Ability.DOMAIN.DESCS.get((classificationCode()&Ability.ALL_DOMAINS)>> 5))
			+ charStats.getAbilityAdjustment("LEVEL+*");
	}

	@Override
	public int adjustedLevel(final MOB caster, final int asLevel)
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

	@Override
	public boolean canTarget(final int can_code)
	{
		final int canTarget=canTargetCode();
		if((can_code==0)||(canTarget==0))
			return can_code == canTarget;
		return CMath.bset(canTargetCode(),can_code);
	}

	@Override
	public boolean canAffect(final int can_code)
	{
		final int canAffect=canAffectCode();
		if((can_code==0)||(canAffect==0))
			return can_code == canAffect;
		return CMath.bset(canAffectCode(),can_code);
	}

	@Override
	public boolean canAffect(final Physical P)
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
	public boolean canTarget(final Physical P)
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

	protected MOB getTarget(final MOB mob, final List<String> commands, final Environmental givenTarget)
	{
		return getTarget(mob,commands,givenTarget,false,false);
	}

	//BZ: The magical pre-cast mana-saving range check, pt 1/2
	protected boolean checkTargetRange(final MOB mob, final MOB target)
	{
		if((target != null)
		&&(target!=mob)
		&&((minRange()>0)||((maxRange()>0)&&(maxRange()<99999)))
		&&(abstractQuality()==Ability.QUALITY_MALICIOUS))
		{
			if((minRange()>0)
			&&(((mob.getVictim()==target)&&(mob.rangeToTarget()<minRange()))
			  ||((target.getVictim()==mob)&&(target.rangeToTarget()<minRange())))
			)
			{
				failureTell(mob,mob,false,L("You are too close to @x1 to do that.",target.name()));
				return false;
			}
			else
			if((maxRange()>0)
			&&(maxRange()<99999)
			&&(((mob.getVictim()==target)&&(mob.rangeToTarget()>maxRange()))
			  ||((target.getVictim()==mob)&&(target.rangeToTarget()>maxRange())))
			)
			{
				failureTell(mob,mob,false,L("You are too far away from @x1 to do that.",target.name()));
				return false;
			}
		}
		return true;
	}

	protected MOB getVisibleRoomTarget(final MOB mob, final String whom)
	{
		if(mob == null)
			return null;
		final Room R=mob.location();
		if(R==null)
			return null;
		MOB target = R.fetchInhabitant(whom);
		int ctr=1;
		while ((target != null)
		&& (!CMLib.flags().canBeSeenBy(target, mob))
		&&(whom.indexOf('.')<0))
			target = R.fetchInhabitant(whom+"."+(++ctr));
		return target;
	}

	protected MOB getTarget(final MOB mob, final List<String> commands, final Environmental givenTarget, final boolean quiet, final boolean alreadyAffOk)
	{
		final String targetName=CMParms.combine(commands,0);
		MOB target=null;
		if((givenTarget!=null)
		&&(givenTarget instanceof MOB))
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
			if(target == null)
			{
				failureTell(mob,mob,false,L("You need to specify a target."));
				return null;
			}
		}
		else
		if(targetName.equalsIgnoreCase("self")||targetName.equalsIgnoreCase("me"))
			target=mob;
		else
		if(mob.location()!=null)
		{
			target=getVisibleRoomTarget(mob,targetName);
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

		if((target==null)
		||((givenTarget==null)
			&&(!CMLib.flags().canBeSeenBy(target,mob))
			&&((!CMLib.flags().canBeHeardMovingBy(target,mob))||(!target.isInCombat()))))
		{
			if(!quiet)
			{
				if(targetName.length()==0)
					failureTell(mob,mob,false,L("You don't see them here."));
				else
					failureTell(mob,mob,false,L("You don't see anyone called '@x1' here.",targetName));
			}
			return null;
		}

		if((!alreadyAffOk)&&(!isAutoInvoked())&&(target.fetchEffect(this.ID())!=null))
		{
			if((givenTarget==null)&&(!quiet))
			{
				if(target==mob)
					failureTell(mob,mob,false,L("You are already affected by @x1.",name()));
				else
					failureTell(mob,target,false,L("<S-NAME> is already affected by @x1.",name()));
			}
			return null;
		}

		if(!checkTargetRange(mob,target))
			target=null;

		return target;
	}

	protected Physical getAnyTarget(final MOB mob, final List<String> commands, final Physical givenTarget, final Filterer<Environmental> filter)
	{
		return getAnyTarget(mob,commands,givenTarget,filter,false,false);
	}

	protected Physical getAnyTarget(final MOB mob, final Room location, final boolean anyContainer, final List<String> commands,
									final Physical givenTarget, final Filterer<Environmental> filter)
	{
		final Physical P=getAnyTarget(mob,commands,givenTarget,filter,false,false, true);
		if(P!=null)
			return P;
		return getTarget(mob, location, givenTarget, anyContainer, commands, filter);
	}

	protected Physical getAnyTarget(final MOB mob, final Room location, final boolean anyContainer, final List<String> commands,
			final Physical givenTarget, final Filterer<Environmental> filter, final boolean quiet)
	{
		final Physical P=getAnyTarget(mob,commands,givenTarget,filter,false,false, true);
		if(P!=null)
			return P;
		return getTarget(mob, location, givenTarget, anyContainer, commands, filter, quiet);
	}

	protected Physical getAnyTarget(final MOB mob, final List<String> commands, final Physical givenTarget, final Filterer<Environmental> filter, final boolean checkOthersInventory)
	{
		return getAnyTarget(mob,commands,givenTarget,filter,checkOthersInventory,false);
	}

	protected Physical getAnyTarget(final MOB mob, final List<String> commands, final Physical givenTarget,
			final Filterer<Environmental> filter,
			final boolean checkOthersInventory, final boolean alreadyAffOk)
	{
		return getAnyTarget(mob,commands,givenTarget,filter,checkOthersInventory,alreadyAffOk,false);
	}

	protected Physical getAnyTarget(final MOB mob, final List<String> commands, final Physical givenTarget,
			final Filterer<Environmental> filter,
			final boolean checkOthersInventory, final boolean alreadyAffOk,
			final boolean quiet)
	{
		final Room R=mob.location();
		final String targetName=CMParms.combine(commands,0);
		Physical target=null;
		if(givenTarget != null)
			target=givenTarget;
		else
		if((targetName.length()==0)&&(mob.isInCombat())&&(castingQuality(mob,mob.getVictim())==Ability.QUALITY_MALICIOUS))
			target=mob.getVictim();
		else
		if(targetName.equalsIgnoreCase("self")
		||targetName.equalsIgnoreCase("me")
		||((targetName.length()==0)&&(castingQuality(mob,mob)==Ability.QUALITY_BENEFICIAL_SELF)))
			target=mob;
		else
		if((targetName.length()==0)&&(mob.isInCombat())&&(abstractQuality()==Ability.QUALITY_MALICIOUS))
			target=mob.getVictim();
		else
		if(R!=null)
		{
			if((target==null)
			&&(targetName.equalsIgnoreCase("here")))
				target=R;
			if(target == null)
				target=R.fetchFromRoomFavorMOBs(null,targetName);
			if(target==null)
				target=R.fetchFromMOBRoomFavorsItems(mob,null,targetName,filter);
			if((target==null)
			&&(targetName.equalsIgnoreCase("room")
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
		if((target==null)
		||((givenTarget==null)
		   &&(!CMLib.flags().canBeSeenBy(target,mob))
		   &&((!CMLib.flags().canBeHeardMovingBy(target,mob)) // do you REALLY can't detect them
				||((target instanceof MOB)&&(!((MOB)target).isInCombat())))))
		{
			if(!quiet)
			{
				if(targetName.trim().length()==0)
					failureTell(mob,mob,false,L("You don't see that here."));
				else
				if(!CMLib.flags().isSleeping(mob))
					failureTell(mob,mob,false,L("You don't see '@x1' here.",targetName));
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
						failureTell(mob,mob,false,L("You are already affected by @x1.",name()));
					else
						mob.tell(mob,target,null,L("<T-NAME> is already affected by @x1.",name()));
				}
			}
			return null;
		}
		return target;
	}

	protected static Item possibleContainer(final MOB mob, final List<String> commands, final boolean withStuff, final Filterer<Environmental> filter)
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

	protected Item getTarget(final MOB mob, final Room location, final Environmental givenTarget, final List<String> commands, final Filterer<Environmental> filter)
	{
		return getTarget(mob,location,givenTarget,null,commands,filter);
	}

	protected Item getTarget(final MOB mob, final Room location, final Environmental givenTarget,
			final boolean anyContainer, final List<String> commands, final Filterer<Environmental> filter)
	{
		return getTarget(mob, location, givenTarget, anyContainer, commands, filter, false);
	}

	protected Item getTarget(final MOB mob, final Room location, final Environmental givenTarget,
			final boolean anyContainer, final List<String> commands, final Filterer<Environmental> filter,
			final boolean quiet)
	{
		Item I=this.getTarget(mob, location, givenTarget, null, commands, filter, anyContainer);
		if(I!=null)
			return I;
		if(!anyContainer)
			return I;
		final List<Item> containers=new ArrayList<Item>();
		if(location!=null)
		{
			for(final Enumeration<Item> i=location.items();i.hasMoreElements();)
			{
				final Item C=i.nextElement();
				if((C instanceof Container)
				&&(((Container)C).isOpen()))
					containers.add(C);
			}
		}
		else
		{
			for(final Enumeration<Item> i=mob.items();i.hasMoreElements();)
			{
				final Item C=i.nextElement();
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
				final Item C=containers.get(c);
				I=this.getTarget(mob, location, givenTarget, C, commands, filter, quiet || (c<containers.size()-1));
				if(I!=null)
					return I;
			}
		}
		return null;
	}

	protected Item getTarget(final MOB mob, final Room location, final Environmental givenTarget, final Item container,
			final List<String> commands, final Filterer<Environmental> filter)
	{
		return getTarget(mob, location, givenTarget, container, commands, filter, false);
	}

	protected Item evalTargetItem(final MOB mob, final Room location, final Environmental givenTarget, final Environmental target,
								  final String targetName, final String ogTargetName, final boolean quiet)
	{
		if((target==null)
		||(!(target instanceof Item))
		||((givenTarget==null)&&(!CMLib.flags().canBeSeenBy(target,mob))))
		{
			if(!quiet)
			{
				if(targetName.length()==0)
					failureTell(mob,mob,false,L("You need to be more specific."));
				else
				if((target==null)||(target instanceof Item))
				{
					if(targetName.trim().length()==0)
						failureTell(mob,mob,false,L("You don't see that here."));
					else
					if(!CMLib.flags().isSleeping(mob)) // no idea why this is here :(
					{
						if(location != null)
							failureTell(mob,mob,false,L("You don't see anything called '@x1' here.",ogTargetName));
						else
							failureTell(mob,mob,false,L("You don't have anything called '@x1'.",ogTargetName));
					}
					else // this was added for clan donate (and other things I'm sure) while sleeping.
						failureTell(mob,mob,false,L("You don't see '@x1' in your dreams.",ogTargetName));
				}
				else
					mob.tell(mob,target,null,L("You can't do that to <T-NAMESELF>."));
			}
			return null;
		}
		return (Item)target;
	}

	protected Item getTarget(final MOB mob, final Room location, final Environmental givenTarget, final Item container,
			final List<String> commands, final Filterer<Environmental> filter, final boolean quiet)
	{
		String targetName=CMParms.combine(commands,0);
		final String ogTargetName = targetName;

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
			if(mob != null)
				target=mob.fetchItem(container, filter, targetName);
		}
		if(target!=null)
			targetName=target.name();

		return evalTargetItem(mob, location, givenTarget, target, targetName, ogTargetName, quiet);

	}

	protected Item getTargetItemFavorMOB(final MOB mob, final Room location, final Physical givenTarget, final Item container,
			final List<String> commands, final Filterer<Environmental> filter)
	{
		return getTargetItemFavorMOB(mob, location, givenTarget, container, commands, filter, false);
	}

	protected Item getTargetItemFavorMOB(final MOB mob, final Room location, final Physical givenTarget,
			final List<String> commands, final Filterer<Environmental> filter)
	{
		return getTargetItemFavorMOB(mob, location, givenTarget, null, commands, filter, false);
	}

	protected Item getTargetItemFavorMOB(final MOB mob, final Room location, final Physical givenTarget, final Item container,
			final List<String> commands, final Filterer<Environmental> filter, final boolean quiet)
	{
		String targetName=CMParms.combine(commands,0);
		final String ogTargetname = targetName;

		Environmental target=null;
		if((givenTarget!=null)&&(givenTarget instanceof Item))
			target=givenTarget;

		if((target==null)&&(targetName.length()>0))
		{
			if(location!=null)
				target=location.fetchFromMOBRoomFavorsItems(mob,container,targetName,filter);
			else
				target=mob.fetchItem(container, filter, targetName);
		}
		if(target!=null)
			targetName=target.name();

		return evalTargetItem(mob, location, givenTarget, target, targetName, ogTargetname, quiet);
	}

	@Override
	public int compareTo(final CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	protected void cloneFix(final Ability E)
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
	public boolean proficiencyCheck(final MOB mob, final int adjustment, final boolean auto)
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
			pctChance += charStats.getAbilityAdjustment("PROF+"+ID().toUpperCase());
			pctChance += charStats.getAbilityAdjustment("PROF+"+Ability.ACODE.DESCS.get(classificationCode()&Ability.ALL_ACODES));
			pctChance += charStats.getAbilityAdjustment("PROF+"+Ability.DOMAIN.DESCS.get((classificationCode()&Ability.ALL_DOMAINS)>> 5));
			pctChance += charStats.getAbilityAdjustment("PROF+*");
		}

		final int xlevel = getXLEVELLevel(mob);
		pctChance += (2*xlevel);

		if(!CMSecurity.isDisabled(DisFlag.DIS955RULE))
		{
			if(pctChance>95)
				pctChance=95;
			if(pctChance<5)
				pctChance=5;
		}

		pctChance += xlevel/2;

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
	public void setAffectedOne(final Physical P)
	{
		affected=P;
	}

	@Override
	public void unInvoke()
	{
		unInvoked=true;

		final Physical being=affected;
		if(being==null)
			return;

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
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
	}

	@Override
	public void affectCharStats(final MOB affectedMob, final CharStats affectableStats)
	{
	}

	@Override
	public void affectCharState(final MOB affectedMob, final CharState affectableMaxState)
	{
	}

	@Override
	public MOB invoker()
	{
		return invoker;
	}

	@Override
	public void setInvoker(final MOB mob)
	{
		invoker=mob;
	}

	protected int[] buildCostArray(final MOB mob, final int consumed, int minimum)
	{
		final boolean useMana=CMath.bset(usageType(),Ability.USAGE_MANA);
		final boolean useMoves=CMath.bset(usageType(),Ability.USAGE_MOVEMENT);
		final boolean useHits=CMath.bset(usageType(),Ability.USAGE_HITPOINTS);
		final int[] usageCosts=new int[Ability.USAGEINDEX_TOTAL];
		final int divider=Math.max(1,(useMana?1:0) + (useMoves?1:0) + (useHits?1:0));
		final boolean[] useCostTypes = new boolean[] {useMana, useMoves, useHits};
		for(int costType=0;costType<Ability.USAGEINDEX_TOTAL;costType++)
		{
			if(useCostTypes[costType])
			{
				int maxAmt;
				int baseAmt;
				if(mob == null)
				{
					maxAmt=1000;
					baseAmt=1000;
				}
				else
				{
					switch(costType)
					{
					case 1:
						maxAmt=mob.maxState().getMovement();
						baseAmt=mob.baseState().getMovement();
						break;
					case 2:
						maxAmt=mob.maxState().getHitPoints();
						baseAmt=mob.baseState().getHitPoints();
						break;
					default:
					case 0:
						maxAmt=mob.maxState().getMana();
						baseAmt=mob.baseState().getMana();
						break;
					}
				}
				int costDown=0;
				final int rawCost;
				if(consumed==COST_ALL)
					rawCost = (baseAmt>maxAmt) ? baseAmt : maxAmt;
				else
				if(consumed>COST_PCT)
					rawCost = (int)Math.round(CMath.mul(maxAmt,CMath.div((COST_ALL-consumed),100.0)));
				else
					rawCost = consumed / divider;
				if((rawCost>2)&&(mob!=null))
				{
					costDown=getXLOWCOSTLevel(mob);
					final int fivePercent=(int)Math.round(CMath.mul(rawCost,0.05));
					if(fivePercent > 1)
						costDown *= fivePercent;
					if((costDown>=rawCost)&&(rawCost<Integer.MAX_VALUE/2))
						costDown=rawCost/2;
					minimum=(minimum-costDown);
					if(minimum<5)
						minimum=5;
					final int freeDown=getXLOWFREECOSTLevel(mob) / 2;
					costDown += freeDown;
					minimum=(minimum-freeDown);
					if(minimum<0)
						minimum=0;
				}
				usageCosts[costType]= rawCost - costDown;
				if(usageCosts[costType]<minimum)
					usageCosts[costType]=minimum;
			}
		}
		return usageCosts;
	}

	protected Map<String, int[]> getHardOverrideManaCache()
	{
		@SuppressWarnings("unchecked")
		Map<String,int[]> hardOverrideCache	= (Map<String,int[]>)Resources.getResource("SYSTEM_ABLEUSAGE_HARD_OVERRIDE_CACHE");
		if(hardOverrideCache == null)
		{
			hardOverrideCache = new Hashtable<String,int[]>();
			Resources.submitResource("SYSTEM_ABLEUSAGE_HARD_OVERRIDE_CACHE", hardOverrideCache);
		}
		return hardOverrideCache;
	}

	@Override
	public int[] usageCost(final MOB mob, final boolean ignoreClassOverride)
	{
		final Map<String,int[]> overrideCache=getHardOverrideManaCache();
		if(mob==null)
		{
			if(overrideCache.containsKey(ID()))
				return overrideCache.get(ID());

		}
		if(usageType()==Ability.USAGE_NADA)
			return STATIC_USAGE_NADA;

		final int[][] abilityUsageCache=(mob==null)?new int[Ability.CACHEINDEX_TOTAL][]:mob.getAbilityUsageCache(ID());
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
			if(mob != null)
			{
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
			}
			if(lowest==Integer.MAX_VALUE)
			{
				lowest=CMLib.ableMapper().lowestQualifyingLevel(ID());
				if(lowest<0)
					lowest=0;
			}

			Integer[] costOverrides=null;
			if((!ignoreClassOverride)&&(mob!=null))
				costOverrides=CMLib.ableMapper().getCostOverrides(mob,ID());
			minimum=CMProps.getMinManaException(ID());
			if(minimum==Integer.MIN_VALUE)
				minimum=CMProps.getIntVar(CMProps.Int.MANAMINCOST);
			if(minimum<0)
			{
				minimum=(lowest*CMath.abs(minimum));
				int actualMinimum=5;
				final int exception = CMProps.getMinManaException("_DEFAULT");
				if(exception > 0 )
					actualMinimum = exception;
				if(minimum<actualMinimum)
					minimum=actualMinimum;
			}
			final Object cost = CMProps.getManaCostObject(ID());
			if(cost instanceof Integer)
				consumed=((Integer)cost).intValue();
			else
			if(cost instanceof CMath.CompiledFormula)
			{
				final double[] vars = new double[] {
					lowest,
					(mob==null)?lowest:mob.phyStats().level(),
					minimum,
					(mob==null)?lowest:adjustedLevel(mob,0),
					diff
				};
				consumed=(int)CMath.parseMathExpression((CMath.CompiledFormula)cost, vars, 0.0);
			}
			else
				consumed=-1;
			if((consumed<0)&&(consumed>-9999))
				consumed=50+(lowest*CMath.abs(consumed));
			if((diff>0)
			&&(!(cost instanceof CMath.CompiledFormula)))
				consumed=(consumed - (consumed/30 * diff));
			if(consumed<minimum)
				consumed=minimum;
			if((overrideMana()>=0) && (CMProps.getManaCostExceptionObject(ID()) == null))
				consumed=overrideMana();
			if((costOverrides!=null)&&(costOverrides[AbilityMapper.AbilCostType.MANA.ordinal()]!=null))
			{
				consumed=costOverrides[AbilityMapper.AbilCostType.MANA.ordinal()].intValue();
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
		if(mob == null)
			overrideCache.put(ID(), usageCost);
		return usageCost;
	}

	@Override
	public void helpProficiency(final MOB mob, final int adjustment)
	{
		if(mob==null)
			return;
		final Ability A=mob.fetchAbility(ID());
		if(A==null)
			return;

		if(mob.isPlayer())
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
				final int adjustedChance=CMLib.ableMapper().getProfGainChance(mob, A);
				if(CMLib.dice().rollPercentage()<adjustedChance)
				{
					// very important, since these can be autoinvoked affects (copies)!
					A.setProficiency(A.proficiency()+1);
					if((this!=A)
					&&(this.isSavable())
					&&(proficiency()<maxProficiency))
						setProficiency(A.proficiency());
					final Ability effA=mob.fetchEffect(ID());
					if((effA!=null)
					&& (effA!=A)
					&& (effA!=this)
					&&(effA.invoker()==mob)
					&&(effA.proficiency()<maxProficiency))
						effA.setProficiency(A.proficiency());
					if(mob.isAttributeSet(MOB.Attrib.AUTOIMPROVE))
						mob.tell(L("You become better at @x1.",A.name()));
					((StdAbility)A).lastCastHelp=System.currentTimeMillis();
				}
			}
			if((A.proficiency() >= maxProficiency)
			&&(mob.isPlayer()))
			{
				final List<String> channels = CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.PROFICIENT, mob);
				for (int i = 0; i < channels.size(); i++)
					CMLib.commands().postChannel(channels.get(i), mob.clans(), L("@x1 is now proficient at @x2.", mob.name(), A.Name()), true);
				CMLib.achievements().possiblyBumpAchievement(mob, AchievementLibrary.Event.SKILLPROF, 1, A);
			}
		}
		else
			A.setProficiency(maxProficiency);
	}

	protected boolean failureTell(final MOB mob, final MOB targetM, final boolean auto, final String msg)
	{
		if(mob==null)
			return false;
		if(auto
		&&(mob.isMonster())
		&&(targetM!=null)
		&&(targetM.isPlayer()))
			targetM.tell(targetM,null,null,msg);
		else
			mob.tell(targetM,null,null,msg);
		return false;
	}

	@Override
	public boolean preInvoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel, final int secondsElapsed, final double actionsRemaining)
	{
		if(mob.commandQueSize() > 1)
		{
			mob.tell(L("You cancel @x1.",name()));
			return false;
		}
		return true;
	}

	@Override
	public boolean invoke(final MOB mob, final Physical target, final boolean auto, final int asLevel)
	{
		final Vector<String> V=new Vector<String>(1);
		if(target!=null)
			V.addElement(target.name());
		return invoke(mob,V,target,auto,asLevel);
	}

	// needs to be public because StdAbility is not local to any of the skills
	public boolean testUsageCost(final MOB mob, final boolean auto, final int[] consumed, final boolean quiet)
	{
		if(mob.curState().getMana()<consumed[Ability.USAGEINDEX_MANA])
		{
			if(!quiet)
			{
				if(mob.maxState().getMana()==consumed[Ability.USAGEINDEX_MANA])
					failureTell(mob,mob,auto,L("You must be at full mana to do that."));
				else
					failureTell(mob,mob,auto,L("You don't have enough mana to do that."));
			}
			return false;
		}
		if(mob.curState().getMovement()<consumed[Ability.USAGEINDEX_MOVEMENT])
		{
			if(!quiet)
			{
				if(mob.maxState().getMovement()==consumed[Ability.USAGEINDEX_MOVEMENT])
					failureTell(mob,mob,auto,L("You must be at full movement to do that."));
				else
					failureTell(mob,mob,auto,L("You don't have enough movement to do that.  You are too tired."));
			}
			return false;
		}
		if(mob.curState().getHitPoints()<consumed[Ability.USAGEINDEX_HITPOINTS])
		{
			if(!quiet)
			{
				if(mob.maxState().getHitPoints()==consumed[Ability.USAGEINDEX_HITPOINTS])
					failureTell(mob,mob,auto,L("You must be at full health to do that."));
				else
					failureTell(mob,mob,auto,L("You don't have enough hit points to do that."));
			}
			return false;
		}
		return true;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical target, final boolean auto, final int asLevel)
	{
		//expertiseCache=null; // this was insane!
		if((mob!=null)&&(getXMAXRANGELevel(mob)>0)) // wut? this must be preventing an npe in some edge case...
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
			&&(room!=null)
			&&(room.getArea()!=null)
			&&(!CMSecurity.isAllowed(mob, room, CMSecurity.SecFlag.ALLSKILLS)))
			{
				final TimeClock C=room.getArea().getTimeObj();
				if(C!=null)
					failureTell(mob,mob,auto,L("You must wait @x1 before you can do that again.",C.deriveEllapsedTimeString(getTimeOfNextCast()-System.currentTimeMillis())));
				return false;
			}

			if(CMath.bset(usageType(),Ability.USAGE_MOVEMENT)
			&&(CMLib.flags().isBound(mob)))
			{
				failureTell(mob,mob,auto,L("You are bound!"));
				return false;
			}

			int[] consumed=usageCost(mob,false);
			final int[] timeCache;
			final int nowLSW = (int)(System.currentTimeMillis()&0x7FFFFFFF);
			final AbilityMapper.CompoundingRule rule = CMLib.ableMapper().getCompoundingRule(mob, this);
			if((rule!=null)
			&&(rule.compoundingTicks() > 0)
			&&(consumed != STATIC_USAGE_NADA)
			&&(overrideMana()<Integer.MAX_VALUE-51))
			{
				final int[][] abilityUsageCache=mob.getAbilityUsageCache(ID());
				if(abilityUsageCache[Ability.CACHEINDEX_LASTTIME] == null)
					abilityUsageCache[Ability.CACHEINDEX_LASTTIME] = new int[USAGEINDEX_TOTAL];
				timeCache = abilityUsageCache[Ability.CACHEINDEX_LASTTIME];
				if(timeCache[USAGEINDEX_TIMELSW]>nowLSW)
					timeCache[USAGEINDEX_TIMELSW]=0;
				final int numTicksSinceLastCast=(int)((nowLSW-timeCache[USAGEINDEX_TIMELSW]) / CMProps.getTickMillis());
				if((numTicksSinceLastCast >= rule.compoundingTicks())||(ignoreCompounding()))
					timeCache[USAGEINDEX_COUNT]=0;
				else
				{
					consumed=Arrays.copyOf(consumed, consumed.length);
					final double pctPenalty = rule.pctPenalty();
					final double amtPenalty = rule.amtPenalty();
					if(amtPenalty < 0)
					{
						failureTell(mob,mob,auto,L("You can't do that again just yet."));
						return false;
					}
					else
					{
						for(int usageIndex = 0 ; usageIndex < Ability.USAGEINDEX_TOTAL; usageIndex++)
						{
							if(consumed[usageIndex]>0)
							{
								double newAmt=consumed[usageIndex];
								for(int ct=0;ct<timeCache[USAGEINDEX_COUNT];ct++)
								{
									if(newAmt<Short.MAX_VALUE)
									{
										newAmt+=amtPenalty;
										newAmt+=CMath.mul(newAmt, pctPenalty);
									}
								}
								consumed[usageIndex]=(int)Math.round(Math.ceil(newAmt));
							}
						}
					}
				}
			}
			else
				timeCache=null;

			if(!testUsageCost(mob,false,consumed,auto))
				return false;

			if((minCastWaitTime()>0)&&(lastCastHelp>0))
			{
				if((System.currentTimeMillis()-lastCastHelp)<minCastWaitTime())
				{
					if(minCastWaitTime()<=1000)
						failureTell(mob,mob,auto,L("You need a second to recover before doing that again."));
					else
					if(minCastWaitTime()<=5000)
						failureTell(mob,mob,auto,L("You need a few seconds to recover before doing that again."));
					else
						failureTell(mob,mob,auto,L("You need awhile to recover before doing that again."));
					return false;
				}
			}
			if(!checkComponents(mob))
				return false;
			if(timeCache!=null)
			{
				timeCache[USAGEINDEX_COUNT]++;
				timeCache[USAGEINDEX_TIMELSW]=nowLSW;
			}
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

	protected boolean checkComponents(final MOB mob)
	{
		if((mob!=null)
		&&(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.COMPONENTS))
		)
		{
			final Vector<AbilityComponent> componentsRequirements=(Vector<AbilityComponent>)CMLib.ableComponents().getAbilityComponentMap().get(ID().toUpperCase());
			if(componentsRequirements!=null)
			{
				final List<Object> components=CMLib.ableComponents().componentCheck(mob,componentsRequirements, false);
				if(components==null)
				{
					failureTell(mob,mob,false,L("The requirements to use this @x1 are: @x2.",
							Ability.ACODE.DESCS.get(classificationCode()&Ability.ALL_ACODES).toLowerCase(),
							CMLib.ableComponents().getAbilityComponentDesc(mob,ID())));
					if(!mob.isPlayer())
						CMLib.ableComponents().startAbilityComponentTrigger(mob, this);
					return false;
				}
				CMLib.ableComponents().destroyAbilityComponents(components);
			}
		}
		return true;
	}

	protected Set<MOB> properTargets(final MOB mob, final Environmental givenTarget, final boolean auto)
	{
		Set<MOB> h=CMLib.combat().properTargets(this,mob,auto);
		if((givenTarget instanceof MOB)
		&&(CMLib.flags().isInTheGame((MOB)givenTarget,true)))
		{
			if(h==null)
				h=new SHashSet<MOB>();
			if(!h.contains(givenTarget))
				h.add((MOB)givenTarget);
		}
		return h;
	}

	protected List<MOB> properTargetList(final MOB mob, final Environmental givenTarget, final boolean auto)
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
			int levelDiff = target.phyStats().level()-adjustedLevel(mob,asLevel);
			final int expRate=CMProps.getIntVar(CMProps.Int.EXPRATE);
			if(baseTicks > Integer.MAX_VALUE/4)
				levelDiff=0;
			else
			if(levelDiff > expRate)
				levelDiff = expRate;
			else
			if(levelDiff < -expRate)
				levelDiff = -expRate;
			final double levelTimeFudge = CMath.div(levelDiff, expRate+1);
			tickDown-=(int)Math.round(CMath.mul(tickDown,levelTimeFudge));
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
		final int tickTime = adjustMaliciousTickdownTime(mob,target,((int)Math.round(CMath.mul(adjustedLevel(mob,asLevel),1.1)))+25,asLevel);
		if(tickAdjustmentFromStandard == 0)
			return tickTime;
		else
			return (int)Math.round(CMath.mul(CMath.div(-tickAdjustmentFromStandard, 100.0) , (double)tickTime));
	}

	public Ability maliciousAffect(final MOB mob, final Physical target, final int asLevel, int tickAdjustmentFromStandard, final int additionAffectCheckCode)
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

	protected boolean beneficialWordsFizzle(final MOB mob, final Environmental target, final String message)
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

	protected boolean beneficialVisualFizzle(final MOB mob, final Environmental target, final String message)
	{
		// it didn't work, but tell everyone you tried.

		final CMMsg msg;
		if(mob.charStats().getBodyPart(Race.BODY_HAND)>0)
			msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_HANDS,message);
		else
			msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_OK_VISUAL,message);
		final Room room=mob.location();
		if(room==null)
			return false;
		if(room.okMessage(mob,msg))
			room.send(mob,msg);

		return false;
	}

	protected boolean beneficialSoundFizzle(final MOB mob, final Environmental target, final String message)
	{
		// it didn't work, but tell everyone you tried.

		final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,message);
		final Room room=mob.location();
		if(room==null)
			return false;
		if(room.okMessage(mob,msg))
			room.send(mob,msg);
		return false;
	}

	protected boolean maliciousFizzle(final MOB mob, final Environmental target, final String message)
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

	protected boolean aPossibleAbuserOfCasterLevel(final MOB casterM)
	{
		if(casterM == null)
			return false;
		if(casterM.isPlayer())
			return true;
		final Room R=casterM.location();
		if(R==null)
			return false;
		if(casterM instanceof Deity)
			return false;
		final MOB folM=casterM.getGroupLeader();
		if(folM.isPlayer())
			return true;
		/* too much
		for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
		{
			final MOB M=m.nextElement();
			if((M!=null)
			&&(M!=casterM)
			&&(M.isPlayer())
			&&(casterM.willFollowOrdersOf(M)))
				return true;
		}
		*/
		return false;
	}

	protected int getBeneficialTickdownTime(final MOB mob, final Environmental target, final int tickAdjustmentFromStandard, final int asLevel)
	{
		if(tickAdjustmentFromStandard>0)
			return tickAdjustmentFromStandard;
		int casterLevel = adjustedLevel(mob,asLevel);
		if((mob != target)
		&&(target instanceof MOB)
		&&(aPossibleAbuserOfCasterLevel(mob)))
		{
			final int levelCap = ((MOB)target).phyStats().level() + CMProps.getIntVar(CMProps.Int.EXPRATE);
			if(casterLevel >  levelCap)
				casterLevel = levelCap;
		}
		final int beneficialTicks = (casterLevel*4)+40;
		final int tickTime = adjustBeneficialTickdownTime(mob,target,beneficialTicks);
		if(tickAdjustmentFromStandard==0)
			return tickTime;
		else
			return (int)Math.round(CMath.mul(CMath.div(-tickAdjustmentFromStandard, 100.0) , (double)tickTime));
	}

	public int getTickdownTime(final MOB mob, final Physical target, final int asLevel, final int tickAdjustmentFromStandard)
	{
		if(abstractQuality()==Ability.QUALITY_MALICIOUS)
			return getMaliciousTickdownTime(mob, target, tickAdjustmentFromStandard, asLevel);
		else
			return getBeneficialTickdownTime(mob, target, tickAdjustmentFromStandard, asLevel);
	}

	public Ability beneficialAffect(final MOB mob, final Physical target, final int asLevel, int tickAdjustmentFromStandard)
	{
		invoker=mob;
		final Ability newOne=(Ability)this.copyOf();
		((StdAbility)newOne).canBeUninvoked=true;
		tickAdjustmentFromStandard=getBeneficialTickdownTime(mob,target,tickAdjustmentFromStandard,asLevel);
		newOne.startTickDown(invoker,target,tickAdjustmentFromStandard);
		return newOne;
	}

	public void spreadImmunity(final MOB mob)
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
	public boolean autoInvocation(final MOB mob, final boolean force)
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

	public void setTickDownRemaining(final int newTick)
	{
		tickDown=newTick;
	}

	@Override
	public int getTickStatus()
	{
		return Tickable.STATUS_NOT;
	}

	@Override
	public boolean canBeTaughtBy(final MOB teacher, final MOB student)
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
	public String requirements(final MOB mob)
	{
		final CostManager cost=getTrainingCost(mob);
		return cost.requirements(mob);
	}

	@Override
	public boolean canBeLearnedBy(final MOB teacher, final MOB student)
	{
		final CostManager cost=getTrainingCost(student);
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

		final String qualClassID = CMLib.ableMapper().qualifyingID(student, this);
		if((qualClassID != null) && (qualClassID.equalsIgnoreCase("All")||(CMClass.getCharClass(qualClassID) != null)))
		{
			int highestSkillLevel=-1;
			for(final AbilityMapping mA : CMLib.ableMapper().getAbleMapping(qualClassID).values())
			{
				if(mA.qualLevel() > highestSkillLevel)
					highestSkillLevel = mA.qualLevel();
			}
			if(highestSkillLevel > 0)
			{
				final int baseInt = CMProps.getIntVar(CMProps.Int.BASEMAXSTAT); // 18
				final int normalizedReqInt = (int)Math.round(Math.ceil(CMath.mul(baseInt, CMath.div(qLevel, highestSkillLevel))));
				final int studentInt=student.charStats().getStat(CharStats.STAT_INTELLIGENCE);
				if(studentInt + 6 < normalizedReqInt)
				{
					if(teacher != null)
						teacher.tell(L("@x1 is not smart enough to learn level @x2 skills.",student.name(),qLevel+""));
					student.tell(L("You are not of high enough intelligence to learn level @x1 skills.",qLevel+""));
					return false;
				}
			}
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
			student.tell(L("You may not learn '@x1' at this time due to the @x2",name(),reason));
			if(teacher != null)
				teacher.tell(L("@x1 does not fit the '@x2' @x3",student.name(),name(),reason));
			return false;
		}

		final DVector prereqs=CMLib.ableMapper().getUnmetPreRequisites(student,this);
		if((prereqs!=null)&&(prereqs.size()>0))
		{
			final String names=CMLib.ableMapper().formatPreRequisites(prereqs);
			student.tell(L("You must learn @x1 before you can gain @x2.",names,name()));
			if(teacher != null)
				teacher.tell(L("@x1 has not learned the pre-requisites to @x2 yet.",student.name(),name()));
			return false;
		}

		return true;
	}

	protected Map<MOB,MOB> saveCombatState(final MOB mob, final boolean andFollowers)
	{
		final Map<MOB,MOB> map = new TreeMap<MOB,MOB>();
		final HashSet<MOB> fols = new HashSet<MOB>();
		fols.add(mob);
		if(andFollowers)
			mob.getGroupMembers(fols);
		for(final MOB M : fols)
			map.put(M,M.getVictim());
		return map;
	}

	protected void restoreCombatState(final Map<MOB,MOB> map)
	{
		for(final MOB M : map.keySet())
		{
			M.setVictim(map.get(M));
		}
	}

	protected int modifyCastCode(final int castCode, final MOB mob, final Physical target, final boolean auto)
	{
		return castCode;
	}

	protected int verbalCastCode(final MOB mob, final Physical target, final boolean auto)
	{
		int affectType=CMMsg.MSG_CAST_VERBAL_SPELL;
		if(castingQuality(mob,target)==Ability.QUALITY_MALICIOUS)
			affectType=CMMsg.MSG_CAST_ATTACK_VERBAL_SPELL;
		if(auto)
			affectType=affectType|CMMsg.MASK_ALWAYS;
		return modifyCastCode(affectType, mob, target, auto);
	}

	protected int verbalSpeakCode(final MOB mob, final Physical target, final boolean auto)
	{
		int affectType=CMMsg.MSG_NOISE|CMMsg.MASK_MOUTH;
		if(castingQuality(mob,target)==Ability.QUALITY_MALICIOUS)
			affectType=CMMsg.MSG_NOISE|CMMsg.MASK_MOUTH|CMMsg.MASK_MALICIOUS;
		if(auto)
			affectType=affectType|CMMsg.MASK_ALWAYS;
		return modifyCastCode(affectType, mob, target, auto);
	}

	protected int verbalCastMask(final MOB mob,final Physical target, final boolean auto)
	{
		return verbalCastCode(mob,target,auto)&CMMsg.MAJOR_MASK;
	}

	protected int somaticCastCode(final MOB mob, final Physical target, final boolean auto)
	{
		int affectType=CMMsg.MSG_CAST_SOMANTIC_SPELL;
		if(castingQuality(mob,target)==Ability.QUALITY_MALICIOUS)
			affectType=CMMsg.MSG_CAST_ATTACK_SOMANTIC_SPELL;
		if(auto)
			affectType=affectType|CMMsg.MASK_ALWAYS;
		return affectType;
	}

	protected int somaticCastMask(final MOB mob, final Physical target, final boolean auto)
	{
		return somaticCastCode(mob,target,auto)&CMMsg.MAJOR_MASK;
	}

	@Override
	public boolean canBePracticedBy(final MOB teacher, final MOB student)
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

		if(yourAbility.proficiency()>=teacherAbility.proficiency())
		{
			teacher.tell(L("You aren't proficient enough to teach any more about '@x1'.",name()));
			student.tell(L("@x1 isn't proficient enough to teach any more about '@x2'.",teacher.name(),name()));
			return false;
		}
		else
		{
			final double max75 =CMath.div(CMProps.getIntVar(CMProps.Int.PRACMAXPCT), 100.0);
			final int prof75=(int)Math.round(CMath.mul(CMLib.ableMapper().getMaxProficiency(student,true,yourAbility.ID()), max75));
			if(yourAbility.proficiency()>prof75-1)
			{
				teacher.tell(L("You can't teach @x1 any more about '@x2'.",student.charStats().himher(),name()));
				student.tell(L("You can't learn any more about '@x1' except through diligence.",name()));
				return false;
			}
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
	public void teach(final MOB teacher, final MOB student)
	{
		if(student.fetchAbility(ID())==null)
		{
			final CostManager cost=getTrainingCost(student);
			if(!cost.doesMeetCostRequirements(student))
				return;
			cost.doSpend(student);
			final Ability newAbility=(Ability)newInstance();
			final double max75 =CMath.div(CMProps.getIntVar(CMProps.Int.PRACMAXPCT), 100.0);
			final int prof75=(int)Math.round(CMath.mul(CMLib.ableMapper().getMaxProficiency(student,true,newAbility.ID()), max75));
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
	public void unlearn(final MOB student)
	{
		if(student == null)
			return;
		final Ability A=student.fetchAbility(ID());
		if(A==this)
			student.delAbility(this);
		final Ability eA=student.fetchEffect(ID());
		if(eA!=null)
		{
			eA.unInvoke();
			student.delEffect(eA);
		}
	}

	@Override
	public void practice(final MOB teacher, final MOB student)
	{
		if(student.getPractices()<practicesToPractice(student))
			return;

		final Ability yourAbility=student.fetchAbility(ID());
		if(yourAbility!=null)
		{
			final Ability teachAbility=teacher.fetchAbility(ID());
			final double max75 =CMath.div(CMProps.getIntVar(CMProps.Int.PRACMAXPCT), 100.0);
			final int prof75=(int)Math.round(CMath.mul(CMLib.ableMapper().getMaxProficiency(student,true,yourAbility.ID()), max75));
			if(yourAbility.proficiency()<prof75)
			{
				student.setPractices(student.getPractices()-practicesToPractice(student));
				final int wisint = teacher.charStats().getStat(CharStats.STAT_WISDOM)
								 + student.charStats().getStat(CharStats.STAT_INTELLIGENCE);
				int newProf = yourAbility.proficiency()+(int)Math.round(25.0*(CMath.div(wisint,36.0)));
				if(newProf > prof75)
					newProf=prof75;
				if((teachAbility!=null)&&(newProf > teachAbility.proficiency()))
					newProf = teachAbility.proficiency();
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
	public boolean tick(final Tickable ticking, final int tickID)
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
	public boolean appropriateToMyFactions(final MOB mob)
	{
		if(mob == null)
			return true;
		return getInappropriateFaction(mob) == null;
	}

	protected Faction getInappropriateFaction(final MOB mob)
	{
		if(mob == null)
			return null;
		for(final Enumeration<String> e=mob.factions();e.hasMoreElements();)
		{
			final String factionID=e.nextElement();
			final Faction F=CMLib.factions().getFaction(factionID);
			if((F!=null)
			&&(F.hasUsage(this))
			&&(!F.canUse(mob,this)))
				return F;
		}
		return null;
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
	private static final String[]	INTERNAL_CODES	= { "TICKDOWN","LEVEL","ISANAUTOEFFECT","NAME","NEXTCAST","CANUNINVOKE" };

	@Override
	public String[] getStatCodes()
	{
		return CODES;
	}

	@Override
	public boolean isStat(final String code)
	{
		return CMParms.indexOf(getStatCodes(), code.toUpperCase().trim()) >= 0;
	}

	protected int getCodeNum(final String code)
	{
		for(int i=0;i<CODES.length;i++)
		{
			if(code.equalsIgnoreCase(CODES[i]))
				return i;
		}
		return -1;
	}

	protected int getInternalCodeNum(final String code)
	{
		for(int i=0;i<INTERNAL_CODES.length;i++)
		{
			if(code.equalsIgnoreCase(INTERNAL_CODES[i]))
				return i;
		}
		return -1;
	}

	@Override
	public String getStat(final String code)
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
			case 3:
				return name();
			case 4:
				return ""+this.getTimeOfNextCast();
			case 5:
				return ""+this.canBeUninvoked();
			default:
				break;
			}
			break;
		}
		return "";
	}

	@Override
	public void setStat(final String code, final String val)
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
			case 3:
				break;
			case 4:
				if(CMath.isLong(val))
					setTimeOfNextCast(CMath.s_long(val));
				break;
			case 5:
				this.canBeUninvoked = CMath.s_bool(val);
				this.unInvoked=false;
				break;
			default:
				break;
			}
			break;
		}
	}

	@Override
	public boolean sameAs(final Environmental E)
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
