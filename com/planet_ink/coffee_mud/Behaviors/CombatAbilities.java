package com.planet_ink.coffee_mud.Behaviors;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
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
@SuppressWarnings("rawtypes")
public class CombatAbilities extends StdBehavior
{
	@Override
	public String ID()
	{
		return "CombatAbilities";
	}

	public int				combatMode	= 0;
	public Map<MOB,int[]>	aggro		= null;
	public short			chkDown		= 0;
	public List<String>		skillsNever = null;
	public List<String>		skillsAlways= null;
	protected boolean[]		wandUseCheck= {false,false};
	protected boolean		proficient	= false;
	protected int			preCastSet	= Integer.MAX_VALUE;
	protected int			preCastDown	= Integer.MAX_VALUE;
	protected String		lastSpell	= null;
	protected boolean		noStat		= false;
	protected boolean		noCombatStat= false;
	protected StringBuffer	record		= null;
	protected int			physicalDamageTaken=0;
	protected InternalWeaponSet weaponSet	= new InternalWeaponSet();

	public final static int COMBAT_RANDOM=0;
	public final static int COMBAT_DEFENSIVE=1;
	public final static int COMBAT_OFFENSIVE=2;
	public final static int COMBAT_MIXEDOFFENSIVE=3;
	public final static int COMBAT_MIXEDDEFENSIVE=4;
	public final static int COMBAT_ONLYALWAYS=5;
	public final static String[] names={
		"RANDOM",
		"DEFENSIVE",
		"OFFENSIVE",
		"MIXEDOFFENSIVE",
		"MIXEDDEFENSIVE",
		"ONLYALWAYS"
	};

	private static class InternalWeaponSet
	{
		public Item wand=null;
		public Item offHandWand=null;
		public Item weapon=null;
	}

	@Override
	public String accountForYourself()
	{
		return "skillful ability using";
	}

	protected void makeClass(MOB mob, String theParms, String defaultClassName)
	{
		CharClass C=null;
		if(theParms.trim().length()==0)
		{
			C=CMClass.findCharClass(defaultClassName);
			if((mob.baseCharStats().getCurrentClass()!=C)&&(C!=null)&&(C.availabilityCode()!=0))
			{
				mob.baseCharStats().setCurrentClass(C);
				mob.recoverCharStats();
			}
			return;
		}
		final Vector<String> V=CMParms.parse(theParms.trim());
		final Vector<CharClass> classes=new Vector<CharClass>();
		for(int v=0;v<V.size();v++)
		{
			C=CMClass.findCharClass(V.elementAt(v));
			if((C!=null)&&(C.availabilityCode()!=0))
				classes.addElement(C);
		}
		if(classes.size()==0)
		{
			C=CMClass.findCharClass(defaultClassName);
			if((mob.baseCharStats().getCurrentClass()!=C)&&(C!=null)&&(C.availabilityCode()!=0))
			{
				mob.baseCharStats().setCurrentClass(C);
				mob.recoverCharStats();
			}
			return;
		}
		for(int i=0;i<classes.size();i++)
		{
			C=classes.elementAt(i);
			mob.baseCharStats().setCurrentClass(C);
			mob.baseCharStats().setClassLevel(C,mob.basePhyStats().level()/classes.size());
		}
		mob.recoverCharStats();
	}

	protected String getParmsMinusCombatMode()
	{
		final Vector<String> V=CMParms.parse(getParms());
		for(int v=V.size()-1;v>=0;v--)
		{
			final String s=V.elementAt(v).toUpperCase();
			for(int i=0;i<names.length;i++)
			{
				if(names[i].startsWith(s))
				{
					combatMode=i;
					V.removeElementAt(v);
				}
			}
		}
		return CMParms.combine(V,0);
	}

	protected void newCharacter(MOB mob)
	{
		final Set<Ability> oldAbilities=new HashSet<Ability>();
		for(final Enumeration<Ability> a=mob.allAbilities();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if(A!=null)
			{
				final int proficiency=CMLib.ableMapper().getMaxProficiency(mob,true,A.ID())/2;
				if(A.proficiency()<proficiency)
					A.setProficiency(proficiency);
				oldAbilities.add(A);
			}
		}
		mob.charStats().getCurrentClass().startCharacter(mob,true,false);
		for(int a=0;a<mob.numAllAbilities();a++)
		{
			final Ability newOne=mob.fetchAbility(a);
			if((newOne!=null)&&(!oldAbilities.contains(newOne)))
			{
				if(!CMLib.ableMapper().qualifiesByLevel(mob,newOne))
				{
					mob.delAbility(newOne);
					mob.delEffect(mob.fetchEffect(newOne.ID()));
					a=a-1;
				}
				else
				{
					final int newProf = CMLib.ableMapper().getMaxProficiency(newOne.ID());
					if(newOne.proficiency()<newProf)
					{
						newOne.setProficiency(newProf);
						if(newOne.isAutoInvoked())
						{
							final Ability newAffect=mob.fetchEffect(newOne.ID());
							if(newAffect!=null)
								newAffect.setProficiency(newProf);
						}
					}
				}
			}
		}
	}

	public void setCombatStats(MOB mob, int attack, int armor, int damage, int hp, int mana, int move, boolean pushDownSpeed)
	{
		if(this.noCombatStat)
			return;
		Ability A=mob.fetchEffect("Prop_CombatAdjuster");
		if(A==null)
		{
			A=CMClass.getAbility("Prop_CombatAdjuster");
			if(A!=null)
			{
				String text="";
				if(attack!=0) 
					text+=" ATTACK"+(attack>0?"+":"")+attack;
				if(armor!=0) 
					text+=" ARMOR"+(armor>0?"+":"")+armor;
				if(damage!=0) 
					text+=" DAMAGE"+(damage>0?"+":"")+damage;
				if(hp!=0) 
					text+=" HP"+(hp>0?"+":"")+hp;
				if(mana!=0) 
					text+=" MANA"+(mana>0?"+":"")+mana;
				if(move!=0) 
					text+=" MOVE"+(move>0?"+":"")+move;
				if(pushDownSpeed) 
					text+=" SPEED=1";
				if(text.length()>0)
				{
					mob.addPriorityEffect(A);
					A.makeNonUninvokable();
					A.makeLongLasting();
					A.setMiscText(text);
					A.setSavable(false);
					mob.recoverPhyStats();
					mob.recoverMaxState();
					mob.resetToMaxState();
				}
			}
		}
	}

	public void setCharStats(MOB mob)
	{
		if(this.noStat)
			return;
		Ability A=mob.fetchEffect("Prop_StatAdjuster");
		if(A==null)
		{
			A=CMClass.getAbility("Prop_StatAdjuster");
			if(A!=null)
			{
				final CharClass C=mob.charStats().getCurrentClass();
				final int[] stats=C.maxStatAdjustments();
				int numStats=0;
				for(final int stat : CharStats.CODES.BASECODES())
				{
					if(stats[stat]!=0)
						numStats++;
				}
				if(numStats==0)
					return;
				int numPoints=mob.phyStats().level();
				if(mob.phyStats().level()>5)
					numPoints=5+((mob.phyStats().level()-5)/8);
				numPoints=numPoints/numStats;
				final StringBuilder parm=new StringBuilder("");
				for(final int stat : CharStats.CODES.BASECODES())
				{
					if(stats[stat]!=0)
						parm.append(CMStrings.limit(CharStats.CODES.NAME(stat),3)).append("=").append(numPoints).append(" ");
				}
				if(parm.length()>0)
				{
					mob.addNonUninvokableEffect(A);
					A.setMiscText(parm.toString().trim());
					A.setSavable(false);
					mob.recoverPhyStats();
					mob.recoverMaxState();
					mob.resetToMaxState();
				}
			}
		}
	}

	public void adjustAggro(MOB hostM, MOB attackerM, int amt)
	{
		if(aggro==null) 
			aggro=new Hashtable<MOB,int[]>();
		synchronized(aggro)
		{
			int[] I = aggro.get(attackerM);
			if(I==null)
			{
				I=new int[]{0};
				aggro.put(attackerM, I);
			}
			I[0]+=amt;
			final MOB curVictim=hostM.getVictim();
			if((curVictim==attackerM)
			||(curVictim==null)
			||(!aggro.containsKey(curVictim)))
				return;
			final int vicAmt=aggro.get(curVictim)[0];
			if((I[0]>(vicAmt*1.5))
			&&(I[0]>hostM.maxState().getHitPoints()/10)
			&&(!attackerM.amDead())
			&&(attackerM.isInCombat()))
			{
				if((hostM.getGroupMembers(new HashSet<MOB>()).contains(attackerM))
				||(!CMLib.flags().canBeSeenBy(attackerM, hostM)))
					I[0]=0;
				else
				{
					hostM.setVictim(attackerM);
					for(final MOB M : aggro.keySet())
					if(M!=attackerM)
					{
						final int[] set=aggro.get(M);
						set[0]=set[0]/2;
					}
				}
			}
		}
	}

	@Override
	public void executeMsg(Environmental host, CMMsg msg)
	{
		if(host instanceof MOB)
		{
			final MOB mob=(MOB)host;
			if(mob.isInCombat())
			{
				final MOB victim=mob.getVictim();
				if(victim==null){}else
				if((msg.targetMinor()==CMMsg.TYP_DAMAGE)
				&&(msg.value()>0)
				&&(msg.source()!=mob))
				{
					if(((msg.tool()==null)||(msg.tool() instanceof Item))
					&&(msg.target()==mob)
					&&(msg.source()==mob.getVictim()))
						physicalDamageTaken+=msg.value();
					if(msg.target()==host)
						adjustAggro(mob,msg.source(),msg.value()*2);
					else
					{
						if((victim==msg.source())
						||(msg.source().getGroupMembers(new HashSet<MOB>()).contains(victim)))
							adjustAggro(mob,msg.source(),msg.value());
					}
				}
				else
				if((msg.targetMinor()==CMMsg.TYP_HEALING)&&(msg.value()>0)
				&&(msg.source()!=mob)
				&&(msg.target()!=mob))
				{
					if((msg.target()==victim)
					||(msg.source().getGroupMembers(new HashSet<MOB>()).contains(victim)))
						adjustAggro(mob,msg.source(),msg.value()*3);
				}
				else
				if((msg.target()==victim)
				&&(msg.source()!=host)
				&&(msg.tool() instanceof Ability)
				&&(CMath.bset(((Ability)msg.tool()).flags(), Ability.FLAG_AGGROFYING)))
					adjustAggro(mob,msg.source(),((Ability)msg.tool()).adjustedLevel(msg.source(), 0)*2);
				else
				if((msg.sourceMinor()==CMMsg.TYP_CAST_SPELL)
				&&(!msg.sourceMajor(CMMsg.MASK_ALWAYS))
				&&(msg.source()!=host)
				&&(msg.tool() instanceof Ability)
				&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)!=Ability.ACODE_SONG)
				&&(msg.source().isInCombat()))
				{
					if((msg.source()==victim)
					||(msg.source().getGroupMembers(new HashSet<MOB>()).contains(victim)))
					{
						int level=CMLib.ableMapper().qualifyingLevel(msg.source(),(Ability)msg.tool());
						if(level<=0)
							level=CMLib.ableMapper().lowestQualifyingLevel(msg.tool().ID());
						if(level>0)
							adjustAggro(mob,msg.source(),level);
					}
				}
			}
		}
		super.executeMsg(host,msg);
	}

	@Override
	public void startBehavior(PhysicalAgent forMe)
	{
		super.startBehavior(forMe);
		skillsNever=null;
		wandUseCheck[0]=false;
		proficient=false;
		final Vector<String> V=CMParms.parse(getParms());
		String s=null;
		Ability A=null;
		for(int v=0;v<V.size();v++)
		{
			s=V.elementAt(v);
			if(s.equalsIgnoreCase("proficient"))
				proficient=true;
			else
			if(s.equalsIgnoreCase("nostat")||s.equalsIgnoreCase("nostats"))
				noStat=true;
			else
			if(s.equalsIgnoreCase("noCombatStat")||s.equalsIgnoreCase("noCombatStats"))
				noStat=true;
			else
			if((s.startsWith("-"))
			&&((A=CMClass.getAbility(s.substring(1)))!=null))
			{
				if(skillsNever==null)
					skillsNever=new Vector<String>();
				skillsNever.add(A.ID());
			}
			else
			if((s.startsWith("+"))
			&&((A=CMClass.getAbility(s.substring(1)))!=null))
			{
				if(skillsAlways==null)
					skillsAlways=new Vector<String>();
				skillsAlways.add(A.ID());
			}
		}
		if(skillsNever instanceof Vector) 
			((Vector)skillsNever).trimToSize();
		if(skillsAlways instanceof Vector) 
			((Vector)skillsAlways).trimToSize();
	}

	protected boolean isRightCombatAbilities(MOB mob)
	{
		// insures we only try this once!
		Behavior B;
		for(final Enumeration<Behavior> e=mob.behaviors();e.hasMoreElements();)
		{
			B=e.nextElement();
			if((B==null)||(B==this))
				return true;
			else
			if(B instanceof CombatAbilities)
				return false;
		}
		return true;
	}

	protected Ability useSkill(MOB mob, MOB victim, MOB leader) throws CMException
	{
		int tries=0;
		Ability tryA=null;
		// now find a skill to use
		Ability A=null;

		MOB target = null;
		int victimQuality=Ability.QUALITY_INDIFFERENT;
		int selfQuality=Ability.QUALITY_INDIFFERENT;
		int leaderQuality=Ability.QUALITY_INDIFFERENT;
		while((tryA==null)&&((++tries)<100)&&(mob.numAllAbilities()>0))
		{
			if((combatMode==COMBAT_ONLYALWAYS)&&(this.skillsAlways!=null)&&(this.skillsAlways.size()>0))
				A=mob.fetchAbility(skillsAlways.get(CMLib.dice().roll(1,skillsAlways.size(),-1)));
			else
				A=mob.fetchRandomAbility();

			if((A==null)
			||(A.isAutoInvoked())
			||(A.triggerStrings()==null)
			||(A.triggerStrings().length==0)
			||((skillsAlways!=null)&&(!skillsAlways.contains(A.ID())))
			||((skillsNever!=null)&&(skillsNever.contains(A.ID()))))
				continue;

			victimQuality=(victim!=null)?A.castingQuality(mob,victim):Ability.QUALITY_INDIFFERENT;
			selfQuality=A.castingQuality(mob,mob);
			leaderQuality=((mob==leader)||(leader==null))?Ability.QUALITY_INDIFFERENT:A.castingQuality(mob,leader);

			if(victimQuality==Ability.QUALITY_MALICIOUS)
			{
				switch(combatMode)
				{
				case COMBAT_RANDOM:
				case COMBAT_ONLYALWAYS:
					tryA=A;
					break;
				case COMBAT_DEFENSIVE:
					if(CMLib.dice().rollPercentage()<=5)
						tryA=A;
					break;
				case COMBAT_OFFENSIVE:
					tryA=A;
					break;
				case COMBAT_MIXEDOFFENSIVE:
					if(CMLib.dice().rollPercentage()<=75)
						tryA=A;
					break;
				case COMBAT_MIXEDDEFENSIVE:
					if(CMLib.dice().rollPercentage()<=25)
						tryA=A;
					break;
				}
			}
			else
			if((selfQuality==Ability.QUALITY_BENEFICIAL_SELF)
			||(leaderQuality==Ability.QUALITY_BENEFICIAL_OTHERS))
			{
				switch(combatMode)
				{
				case COMBAT_RANDOM:
				case COMBAT_ONLYALWAYS:
					tryA=A;
					break;
				case COMBAT_DEFENSIVE:
					tryA=A;
					break;
				case COMBAT_OFFENSIVE:
					if(CMLib.dice().rollPercentage()<=5)
						tryA=A;
					break;
				case COMBAT_MIXEDOFFENSIVE:
					if(CMLib.dice().rollPercentage()<=25)
						tryA=A;
					break;
				case COMBAT_MIXEDDEFENSIVE:
					if(CMLib.dice().rollPercentage()<=75)
						tryA=A;
					break;
				}
			}
			target=victim;
			if(selfQuality==Ability.QUALITY_BENEFICIAL_SELF)
				target=mob;
			else
			if(leaderQuality==Ability.QUALITY_BENEFICIAL_OTHERS)
				target=((leader==null)||(mob.location()!=leader.location()))?mob:leader;
			if((target != null) && (tryA != null) && (target.fetchEffect(tryA.ID())!=null))
				tryA = null;
		}

		if(tryA!=null)
		{
			if(CMath.bset(tryA.usageType(),Ability.USAGE_MANA))
			{
				if((Math.random()>CMath.div(mob.curState().getMana(), mob.maxState().getMana()))
				||(mob.curState().getMana() < tryA.usageCost(mob,false)[0]))
				{
				   if((CMLib.dice().rollPercentage()>30)
				   ||(CMProps.getIntVar(CMProps.Int.MANACONSUMETIME)<=0)
				   ||((mob.amFollowing()!=null)&&(!mob.amFollowing().isMonster())))
					   throw new CMException("Not enough mana");
				   mob.curState().adjMana(tryA.usageCost(mob,false)[0],mob.maxState());
				}
				mob.curState().adjMana(5,mob.maxState());
			}
			if(CMath.bset(tryA.usageType(),Ability.USAGE_MOVEMENT))
			{
				if((Math.random()>CMath.div(mob.curState().getMovement(),mob.maxState().getMovement()))
				||(mob.curState().getMovement()<tryA.usageCost(mob,false)[1]))
			 	   throw new CMException("Not enough movement");
				mob.curState().adjMovement(5,mob.maxState());
			}
			if(CMath.bset(tryA.usageType(),Ability.USAGE_HITPOINTS))
			{
				if((Math.random()>CMath.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()))
				   ||(mob.curState().getHitPoints()<tryA.usageCost(mob,false)[2]))
				 	   throw new CMException("Not enough hp");
			}

			if(proficient)
				tryA.setProficiency(100);
			else
			{
				final int qualLevel=CMLib.ableMapper().qualifyingLevel(mob,tryA);
				if(qualLevel<=0)
					tryA.setProficiency(75);
				else
				{
					int levelDiff=mob.basePhyStats().level()-qualLevel;
					if((levelDiff>50)||(levelDiff<0))
						levelDiff=50;
					tryA.setProficiency(50+levelDiff);
				}
			}
			if(target==null)
				return null;
			boolean skillUsed=tryA.invoke(mob,new XVector<String>(target.name()),null,false,0);
			if((combatMode==COMBAT_ONLYALWAYS)&&(!skillUsed))
			{
				int retries=0;
				while((++retries<10)&&(!skillUsed))
					skillUsed=tryA.invoke(mob,new XVector<String>(target.name()),null,false,0);
			}
			if(skillUsed)
			{
				skillUsed=true;
				if(lastSpell!=null)
					lastSpell=tryA.ID();
			}
			else
			{
				if(lastSpell!=null)
					lastSpell="!"+tryA.ID();
				if(record!=null)
					record.append("!");
			}
			if(record!=null)
				record.append(tryA.ID()).append("; ");
			return skillUsed?tryA:null;
		}
		return null;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if(ticking==null)
			return true;
		if(tickID!=Tickable.TICKID_MOB)
		{
			Log.errOut("CombatAbilities",ticking.name()+" wants to fight?!");
			return true;
		}
		final MOB mob=(MOB)ticking;

		if(!mob.isInCombat())
		{
			if(aggro!=null)
			{
				synchronized(aggro)
				{
					aggro=null;
				}
			}
			if((preCastSet < Integer.MAX_VALUE) && (preCastSet >0) && ((--preCastDown)<=0))
			{
				if(!canActAtAll(mob))
					return true;
				preCastDown=preCastSet;
				if(!isRightCombatAbilities(mob))
					return true;
				try
				{
					useSkill(mob,null,null);
				}
				catch(final CMException cme)
				{
				}
			}
			return true;
		}
		MOB victim=mob.getVictim();
		if(victim==null)
			return true;

		if(!canActAtAll(mob))
			return true;

		// insures we only try this once!
		if(!isRightCombatAbilities(mob))
			return true;

		final Room R=mob.location();
		if((lastSpell!=null)&&(lastSpell.length()>0))
			lastSpell="";

		if(!wandUseCheck[0])
		{
			wandUseCheck[0]=true;
			final Ability wandUse=mob.fetchAbility("Skill_WandUse");
			wandUseCheck[1]=false;
			if(wandUse!=null)
			{
				wandUseCheck[1]=true;
				wandUse.setProficiency(100);
				wandUse.setInvoker(mob);
			}
		}

		boolean rebuildSet=false;
		final int rtt=mob.rangeToTarget();
		if((weaponSet.weapon==null)||(weaponSet.weapon.owner()!=mob)
		||(weaponSet.weapon.amDestroyed())
		||(weaponSet.weapon.amWearingAt(Wearable.IN_INVENTORY))
		||(weaponSet.weapon.minRange()>rtt)
		||(weaponSet.weapon.maxRange()<rtt))
			rebuildSet=true;
		if((weaponSet.wand!=null)
		&&((weaponSet.wand.owner()!=mob)||(weaponSet.wand.amDestroyed())||(weaponSet.wand.amWearingAt(Wearable.IN_INVENTORY))))
			rebuildSet=true;
		if((weaponSet.offHandWand!=null)
		&&((weaponSet.offHandWand.owner()!=mob)||(weaponSet.offHandWand.amDestroyed())||(!weaponSet.offHandWand.amWearingAt(Wearable.IN_INVENTORY))))
			rebuildSet=true;
		if(rebuildSet)
		{
			weaponSet.weapon=null;
			weaponSet.wand=null;
			weaponSet.offHandWand=null;
			Item newWeapon=null;
			for(final Enumeration<Item> i=mob.items();i.hasMoreElements();)
			{
				final Item I=i.nextElement();
				if(I instanceof Wand)
				{
					if(!I.amWearingAt(Wearable.IN_INVENTORY))
						weaponSet.wand=I;
					else
						weaponSet.offHandWand=I;
				}
				if(I instanceof Weapon)
				{
					if((((Weapon)I).minRange()<=rtt)&&(((Weapon)I).maxRange()>=rtt))
					{
						if(I.amWearingAt(Wearable.WORN_WIELD))
							weaponSet.weapon=I;
						else
						if(((newWeapon==null)&&(weaponSet.weapon==null))
						||(I.amWearingAt(Wearable.WORN_HELD)))
							newWeapon=I;
					}
				}
			}
			// first look for an appropriate weapon to weild
			if((weaponSet.weapon==null)&&((--chkDown)<=0))
			{
				if((newWeapon==null)&&(R!=null))
				{
					final Vector<Item> choices=new Vector<Item>(1);
					for(final Enumeration<Item> i=R.items();i.hasMoreElements();)
					{
						final Item I=i.nextElement();
						if((!(I instanceof Weapon))
						||(((Weapon)I).minRange()>rtt)
						||(((Weapon)I).maxRange()<rtt)
						||(I.container()!=null)
						||(!CMLib.flags().isGettable(I))
						||(I.phyStats().level()>mob.phyStats().level()))
							continue;
						choices.addElement(I);
					}
					final Item I=(choices.size()==0)?null:(Item)choices.elementAt(CMLib.dice().roll(1,choices.size(),-1));
					if(I!=null)
					{
						CMLib.commands().forceInternalCommand(mob,"GET",I);
						if(mob.isMine(I))
							newWeapon=I;
					}
				}
				if(newWeapon!=null)
				{
					CMLib.commands().forceInternalCommand(mob,"WIELD",newWeapon);
				}
				chkDown=5;
			}
		}

		// next deal with aggro changes
		if(aggro!=null)
		{
			synchronized(aggro)
			{
				int winAmt=0;
				MOB winMOB = null;
				int vicAmt=0;
				final int minAmt=mob.maxState().getHitPoints()/10;
				if(aggro.containsKey(victim))
					vicAmt = aggro.get(victim)[0];
				int[] amt = null;
				for(final MOB M : aggro.keySet())
				{
					amt = aggro.get(M);
					if((amt[0]>winAmt)
					&&(CMLib.flags().canBeSeenBy(M,mob)))
					{
						winAmt=amt[0];
						winMOB=M;
					}
				}
				if((winAmt>minAmt)
				&&(winAmt>(vicAmt+(vicAmt/2)))
				&&(winMOB!=null)
				&&(!winMOB.amDead())
				&&(winMOB.isInCombat())
				&&(!mob.getGroupMembers(new HashSet<MOB>()).contains(winMOB)))
				{
					mob.setVictim(winMOB);
					victim=mob.getVictim();
					aggro.clear();
				}
			}
		}
		if(victim==null)
			return true;

		final MOB leader=mob.amFollowing();

		boolean skillUsed=false;
		try
		{
			skillUsed=useSkill(mob, victim, leader)!=null;
		}
		catch(final CMException cme) { return true;}

		Ability A=null;
		// if a skill use failed, take a stab at wanding
		if((!skillUsed)
		&&(wandUseCheck[1])
		&&(victim.location()!=null)
		&&(!victim.amDead())
		&&((weaponSet.wand!=null)||(weaponSet.offHandWand!=null)))
		{
			if((weaponSet.wand==null)&&(weaponSet.offHandWand!=null)&&(weaponSet.offHandWand.canWear(mob,Wearable.WORN_HELD)))
			{
				final Vector<String> V=new Vector<String>();
				V.addElement("hold");
				V.addElement(weaponSet.offHandWand.name());
				mob.doCommand(V,MUDCmdProcessor.METAFLAG_FORCED);
			}
			else
			if(weaponSet.wand!=null)
			{
				A=((Wand)weaponSet.wand).getSpell();
				if(A!=null)
				{
					final MOB target;
					if(A.castingQuality(mob,mob)==Ability.QUALITY_BENEFICIAL_SELF)
						target=mob;
					else
					if(A.castingQuality(mob,victim)==Ability.QUALITY_MALICIOUS)
						target=victim;
					else
					if(((mob!=leader)&&(leader!=null))&&(A.castingQuality(mob,leader)==Ability.QUALITY_BENEFICIAL_OTHERS))
						target=((mob.location()!=leader.location()))?mob:leader;
					else
						target=null;
					if(target!=null)
					{
						final Vector<String> V=new Vector<String>();
						V.addElement("sayto");
						V.addElement(target.name());
						V.addElement(((Wand)weaponSet.wand).magicWord());
						mob.doCommand(V,MUDCmdProcessor.METAFLAG_FORCED);
					}
				}
			}
		}
		return true;
	}

	protected static String[] CODES=null;

	@Override
	public String[] getStatCodes()
	{
		if(CombatAbilities.CODES==null)
		{
			final String[] superCodes=super.getStatCodes();
			CODES=new String[superCodes.length+8];
			for(int c=0;c<superCodes.length;c++)
				CODES[c]=superCodes[c];
			CODES[CODES.length-8]="RECORD";
			CODES[CODES.length-7]="PROF";
			CODES[CODES.length-6]="LASTSPELL";
			CODES[CODES.length-5]="PRECAST";
			CODES[CODES.length-4]="PHYSDAMTAKEN";
			CODES[CODES.length-3]="SKILLSALWAYS";
			CODES[CODES.length-2]="SKILLSNEVER";
			CODES[CODES.length-1]="COMBATMODE";
		}
		return CODES;
	}

	@Override
	protected int getCodeNum(String code)
	{
		final String[] CODES=getStatCodes();
		for(int i=0;i<CODES.length;i++)
		{
			if(code.equalsIgnoreCase(CODES[i]))
				return i;
		}
		return -1;
	}

	@Override
	public String getStat(String code)
	{
		int x=getCodeNum(code);
		if(x<super.getStatCodes().length)
			return super.getStat(code);
		x=x-super.getStatCodes().length;
		switch(x)
		{
		case 0:
			return (record == null) ? "" : record.toString();
		case 1:
			return Boolean.toString(proficient);
		case 2:
			return lastSpell != null ? lastSpell : "";
		case 3:
			return Integer.toString(preCastSet);
		case 4:
			return Integer.toString(physicalDamageTaken);
		case 5:
			return (skillsAlways == null) ? "" : CMParms.toSemicolonListString(skillsAlways);
		case 6:
			return (skillsAlways == null) ? "" : CMParms.toSemicolonListString(skillsNever);
		case 7:
			return Integer.toString(combatMode);
		}
		return "";
	}

	@Override
	public void setStat(String code, String val)
	{
		int x=getCodeNum(code);
		if(x<super.getStatCodes().length)
			super.setStat(code,val);
		x=x-super.getStatCodes().length;
		switch(x)
		{
		case 0:
			if(val.length()==0)
				record=null;
			else
				record=new StringBuffer(val.trim());
			break;
		case 1:
			proficient=CMath.s_bool(val);
			break;
		case 2:
			lastSpell=val;
			break;
		case 3:
			preCastSet=CMath.s_int(val);
			preCastDown=CMath.s_int(val);
			break;
		case 4:
			physicalDamageTaken=CMath.s_int(val);
			break;
		case 5:
			skillsAlways=CMParms.parseSemicolons(val,true);
			if(skillsAlways.size()==0)
				skillsAlways=null;
			break;
		case 6:
			skillsNever=CMParms.parseSemicolons(val,true);
			if(skillsNever.size()==0)
				skillsNever=null;
			break;
		case 7:
			if(CMath.isInteger(val))
				combatMode=CMath.s_int(val);
			else
				combatMode=CMParms.indexOf(names,val.toUpperCase().trim());
			break;
		}
	}
}
