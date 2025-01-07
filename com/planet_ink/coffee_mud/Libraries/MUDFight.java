package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
import com.planet_ink.coffee_mud.core.CMSecurity.DisFlag;
import com.planet_ink.coffee_mud.core.CMath.CompiledFormula;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.Fighter;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.PrideStats.PrideStat;
import com.planet_ink.coffee_mud.Common.interfaces.CMMsg.View;
import com.planet_ink.coffee_mud.Common.interfaces.PlayerStats.PlayerCombatStat;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB.Attrib;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

import java.util.*;

/*
   Copyright 2004-2024 Bo Zimmerman

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
public class MUDFight extends StdLibrary implements CombatLibrary
{
	@Override
	public String ID()
	{
		return "MUDFight";
	}
	static final char[] PARENS={'(',')'};

	public String 		lastStr			= "";
	public long 		lastRes			= 0;
	public Object[][][] hitWordIndex	= null;
	public Object[][][] hitWordsChanged	= null;

	protected CMath.CompiledFormula	attackAdjustmentFormula			= null;
	protected CMath.CompiledFormula	armorAdjustmentFormula			= null;
	protected CMath.CompiledFormula	attackerFudgeBonusFormula		= null;
	protected CMath.CompiledFormula	pvpAttackerFudgeBonusFormula	= null;
	protected CMath.CompiledFormula	spellFudgeDamageFormula			= null;
	protected CMath.CompiledFormula	pvpSpellFudgeDamageFormula		= null;
	protected CMath.CompiledFormula	spellCritChanceFormula			= null;
	protected CMath.CompiledFormula	pvpSpellCritChanceFormula		= null;
	protected CMath.CompiledFormula	spellCritDmgFormula				= null;
	protected CMath.CompiledFormula	pvpSpellCritDmgFormula			= null;
	protected CMath.CompiledFormula	targetedRangedDamageFormula		= null;
	protected CMath.CompiledFormula	pvpTargetedRangedDamageFormula	= null;
	protected CMath.CompiledFormula	rangedFudgeDamageFormula		= null;
	protected CMath.CompiledFormula	pvpRangedFudgeDamageFormula		= null;
	protected CMath.CompiledFormula	targetedMeleeDamageFormula		= null;
	protected CMath.CompiledFormula	pvpTargetedMeleeDamageFormula	= null;
	protected CMath.CompiledFormula	meleeFudgeDamageFormula			= null;
	protected CMath.CompiledFormula	pvpMeleeFudgeDamageFormula		= null;
	protected CMath.CompiledFormula	staticRangedDamageFormula		= null;
	protected CMath.CompiledFormula	staticMeleeDamageFormula		= null;
	protected CMath.CompiledFormula	weaponCritChanceFormula			= null;
	protected CMath.CompiledFormula	pvpWeaponCritChanceFormula		= null;
	protected CMath.CompiledFormula	weaponCritDmgFormula			= null;
	protected CMath.CompiledFormula	pvpWeaponCritDmgFormula			= null;
	protected CMath.CompiledFormula	stateHitPointRecoverFormula		= null;
	protected CMath.CompiledFormula	stateManaRecoverFormula			= null;
	protected CMath.CompiledFormula	stateMovesRecoverFormula		= null;
	protected CMath.CompiledFormula totalCombatExperienceFormula	= null;
	protected CMath.CompiledFormula individualCombatExpFormula		= null;

	private static final int ATTACK_ADJUSTMENT = 50;

	@Override
	public boolean activate()
	{
		if(!super.activate())
			return false;
		attackAdjustmentFormula = CMath.compileMathExpression(CMProps.getVar(CMProps.Str.FORMULA_ATTACKADJUSTMENT));
		armorAdjustmentFormula= CMath.compileMathExpression(CMProps.getVar(CMProps.Str.FORMULA_ARMORADJUSTMENT));
		attackerFudgeBonusFormula = CMath.compileMathExpression(CMProps.getVar(CMProps.Str.FORMULA_ATTACKFUDGEBONUS));
		pvpAttackerFudgeBonusFormula = CMath.compileMathExpression(CMProps.getVar(CMProps.Str.FORMULA_PVPATTACKFUDGEBONUS));
		spellCritChanceFormula = CMath.compileMathExpression(CMProps.getVar(CMProps.Str.FORMULA_CHANCESPELLCRIT));
		pvpSpellCritChanceFormula = CMath.compileMathExpression(CMProps.getVar(CMProps.Str.FORMULA_PVPCHANCESPELLCRIT));
		spellCritDmgFormula = CMath.compileMathExpression(CMProps.getVar(CMProps.Str.FORMULA_DAMAGESPELLCRIT));
		pvpSpellCritDmgFormula = CMath.compileMathExpression(CMProps.getVar(CMProps.Str.FORMULA_PVPDAMAGESPELLCRIT));
		targetedRangedDamageFormula=CMath.compileMathExpression(CMProps.getVar(CMProps.Str.FORMULA_DAMAGERANGEDTARGETED));
		pvpTargetedRangedDamageFormula=CMath.compileMathExpression(CMProps.getVar(CMProps.Str.FORMULA_PVPDAMAGERANGEDTARGETED));
		targetedMeleeDamageFormula=CMath.compileMathExpression(CMProps.getVar(CMProps.Str.FORMULA_DAMAGEMELEETARGETED));
		pvpTargetedMeleeDamageFormula=CMath.compileMathExpression(CMProps.getVar(CMProps.Str.FORMULA_PVPDAMAGEMELEETARGETED));
		staticRangedDamageFormula=CMath.compileMathExpression(CMProps.getVar(CMProps.Str.FORMULA_DAMAGERANGEDSTATIC));
		staticMeleeDamageFormula=CMath.compileMathExpression(CMProps.getVar(CMProps.Str.FORMULA_DAMAGEMELEESTATIC));
		weaponCritChanceFormula = CMath.compileMathExpression(CMProps.getVar(CMProps.Str.FORMULA_CHANCEWEAPONCRIT));
		pvpWeaponCritChanceFormula = CMath.compileMathExpression(CMProps.getVar(CMProps.Str.FORMULA_PVPCHANCEWEAPONCRIT));
		weaponCritDmgFormula = CMath.compileMathExpression(CMProps.getVar(CMProps.Str.FORMULA_DAMAGEWEAPONCRIT));
		pvpWeaponCritDmgFormula = CMath.compileMathExpression(CMProps.getVar(CMProps.Str.FORMULA_PVPDAMAGEWEAPONCRIT));
		spellFudgeDamageFormula = CMath.compileMathExpression(CMProps.getVar(CMProps.Str.FORMULA_DAMAGESPELLFUDGE));
		pvpSpellFudgeDamageFormula = CMath.compileMathExpression(CMProps.getVar(CMProps.Str.FORMULA_PVPDAMAGESPELLFUDGE));
		meleeFudgeDamageFormula = CMath.compileMathExpression(CMProps.getVar(CMProps.Str.FORMULA_DAMAGEMELEEFUDGE));
		pvpMeleeFudgeDamageFormula = CMath.compileMathExpression(CMProps.getVar(CMProps.Str.FORMULA_PVPDAMAGEMELEEFUDGE));
		rangedFudgeDamageFormula = CMath.compileMathExpression(CMProps.getVar(CMProps.Str.FORMULA_DAMAGERANGEDFUDGE));
		pvpRangedFudgeDamageFormula = CMath.compileMathExpression(CMProps.getVar(CMProps.Str.FORMULA_PVPDAMAGERANGEDFUDGE));

		stateHitPointRecoverFormula = CMath.compileMathExpression(CMProps.getVar(CMProps.Str.FORMULA_HITPOINTRECOVER));
		stateManaRecoverFormula = CMath.compileMathExpression(CMProps.getVar(CMProps.Str.FORMULA_MANARECOVER));
		stateMovesRecoverFormula = CMath.compileMathExpression(CMProps.getVar(CMProps.Str.FORMULA_MOVESRECOVER));

		totalCombatExperienceFormula = CMath.compileMathExpression(CMProps.getVar(CMProps.Str.FORMULA_TOTALCOMBATXP));
		individualCombatExpFormula = CMath.compileMathExpression(CMProps.getVar(CMProps.Str.FORMULA_INDCOMBATXP));

		if(serviceClient==null)
		{
			name="THCombat"+Thread.currentThread().getThreadGroup().getName().charAt(0);
			serviceClient=CMLib.threads().startTickDown(this, Tickable.TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK, CMProps.getTickMillis(), TICKS_PER_SHIP_COMBAT);
		}
		return true;
	}

	@Override
	public void propertiesLoaded()
	{
		activate();
	}

	@Override
	public Set<MOB> allPossibleCombatants(final MOB mob, final boolean includePlayers)
	{
		final SHashSet<MOB> h=new SHashSet<MOB>();
		final Room thisRoom=mob.location();
		if(thisRoom==null)
			return null;
		final Set<MOB> h1=mob.getGroupMembers(new HashSet<MOB>());
		for(int m=0;m<thisRoom.numInhabitants();m++)
		{
			final MOB inhab=thisRoom.fetchInhabitant(m);
			if((inhab!=null)
			&&(inhab!=mob)
			&&(!h1.contains(inhab))
			&&(CMLib.flags().isSeeable(inhab)
				||(CMLib.flags().canMove(inhab)&&(!CMLib.flags().isCloaked(inhab))))
			&&((includePlayers)||inhab.isMonster()||mob.isMonster()))
				h.addUnsafe(inhab);
		}
		return h;
	}

	@Override
	public Set<MOB> properTargets(final Ability A, final MOB caster, final boolean includePlayers)
	{
		Set<MOB> h=null;
		if(A.abstractQuality()!=Ability.QUALITY_MALICIOUS)
		{
			if(caster.Name().equalsIgnoreCase("somebody"))
				h=new SHashSet<MOB>();
			else
				h=caster.getGroupMembers(new SHashSet<MOB>());
			for (final MOB M : h)
			{
				if(M.location()!=caster.location())
					h.remove(M);
			}
		}
		else
		{
			if(caster.isInCombat())
				h=allCombatants(caster);
			else
				h=allPossibleCombatants(caster,includePlayers);
			//BZ: The magical pre-cast mana-saving range check, pt 2/2
			if((A!=null)
			&&(h!=null)
			&&(h.size()>0)
			&&((A.minRange()>0)||((A.maxRange()>0))&&(A.maxRange()<9999))
			&&(A.abstractQuality()==Ability.QUALITY_MALICIOUS))
			{
				int disqualified=0;
				for(final MOB M : h)
				{
					if((A.minRange()>0)
					&&(((caster.getVictim()==M)&&(caster.rangeToTarget()<A.minRange()))
					  ||((M.getVictim()==caster)&&(M.rangeToTarget()<A.minRange())))
					)	disqualified++;
					else
					if((A.maxRange()>0)
					&&(A.maxRange()<99999)
					&&(((caster.getVictim()==M)&&(caster.rangeToTarget()>A.maxRange()))
					  ||((M.getVictim()==caster)&&(M.rangeToTarget()>A.maxRange())))
					)	disqualified++;
				}
				if(disqualified>=h.size())
				{
					caster.tell(L("You aren't in range!"));
					h.clear();
				}
			}
		}
		return h;
	}

	@Override
	public int adjustedAttackBonus(final MOB mob, final MOB target)
	{
		final int maxStr = mob.charStats().getMaxStat(CharStats.STAT_STRENGTH);
		int currStr = mob.charStats().getStat(CharStats.STAT_STRENGTH);
		int strBonus = 0;
		if(currStr > maxStr)
		{
			strBonus = currStr - maxStr;
			currStr = maxStr;
		}
		int baseStr = mob.baseCharStats().getStat(CharStats.STAT_STRENGTH);
		if(baseStr > maxStr)
			baseStr = maxStr;
		final double[] vars = {mob.phyStats().attackAdjustment(),
						 currStr,
						 baseStr,
						 strBonus,
						 (mob.curState().getHunger()<1)?1.0:0.0,
						 (mob.curState().getThirst()<1)?1.0:0.0,
						 (mob.curState().getFatigue()>CharState.FATIGUED_MILLIS)?1.0:0.0
						};
		return (int)Math.round(CMath.parseMathExpression(attackAdjustmentFormula, vars, 0.0));
	}

	@Override
	public int adjustedAttackBonus(final int baseAttack)
	{
		final double[] vars = {baseAttack,
						CharStats.VALUE_ALLSTATS_DEFAULT,
						CharStats.VALUE_ALLSTATS_DEFAULT,
						 0,//strength bonus
						 0.0,//hunger
						 0.0,//thirst
						 0.0//fatigue
						};
		return (int)Math.round(CMath.parseMathExpression(attackAdjustmentFormula, vars, 0.0));
	}

	@Override
	public void postItemDamage(final MOB mob, final Item I, final Environmental tool, final int damageAmount, final int messageType, final String message)
	{
		if(mob==null)
			return ;
		final Room R=mob.location();
		if(R==null)
			return ;
		CMMsg msg=CMClass.getMsg(mob,I,tool,CMMsg.MASK_MALICIOUS|CMMsg.MASK_ALWAYS|messageType,message,CMMsg.MASK_MALICIOUS|CMMsg.MASK_ALWAYS|messageType,message,CMMsg.NO_EFFECT,null);
		if(R.okMessage(mob,msg))
		{
			R.send(mob,msg);
			if(msg.value()<=0)
			{
				if(I.subjectToWearAndTear())
				{
					I.setUsesRemaining(I.usesRemaining()-damageAmount);
					I.recoverPhyStats(); // important relation to setuses -- for brittle
					if(I.usesRemaining()<=0)
					{
						I.setUsesRemaining(100);
						I.unWear();
						final MOB targetM = (I.owner() instanceof MOB)?(MOB)I.owner():mob;
						msg=CMClass.getMsg(mob,targetM,I,CMMsg.MSG_OK_VISUAL,L("<O-NAME> is destroyed!"),null,L("<O-NAME> carried by <T-NAME> is destroyed!"));
						if(R.okMessage(mob,msg))
						{
							if(I instanceof Container)
								((Container)I).emptyPlease(false);
							R.send(mob,msg);
						}
						I.destroy();
						mob.recoverPhyStats();
						mob.recoverCharStats();
						mob.recoverMaxState();
						R.recoverRoomStats();
					}
					else
					if(I.usesRemaining()<=10)
					{
						mob.tell(L("@x1 is looking really bad.",I.name(mob)));
					}
				}
				else
				if(damageAmount > 0)
				{
					I.unWear();
					msg=CMClass.getMsg(mob,null,I,CMMsg.MSG_OK_VISUAL,L("<O-NAME> is destroyed!"),null,L("<O-NAME> carried by <S-NAME> is destroyed!"));
					if(R.okMessage(mob,msg))
						R.send(mob,msg);
					I.destroy();
					mob.recoverPhyStats();
					mob.recoverCharStats();
					mob.recoverMaxState();
					R.recoverRoomStats();
				}
			}
		}
	}

	@Override
	public boolean doTurnBasedCombat(final MOB mob, final Room R, final Area A)
	{
		int index = R.getCombatTurnMobIndex();
		MOB M=null;
		if(mob.actions() < 1.0)
		{
			if((index >= R.numInhabitants())||((M=R.fetchInhabitant(index))==mob)||(M==null)||(!M.isInCombat()))
			{
				if((index<0)||(index>=R.numInhabitants()-1))
					index=-1;
				for(index++;index<R.numInhabitants();index++)
				{
					M=R.fetchInhabitant(index);
					if((M!=null)&&(M.isInCombat()))
					{
						M.setActions((M.actions() - Math.floor(M.actions())) + (CMLib.flags().isSitting(M) ? M.phyStats().speed() / 2.0 : M.phyStats().speed()));
						R.setCombatTurnMobIndex(index);
						break;
					}
				}
				return false;
			}
			else
			{
				return true;
			}
		}
		else
		if((index < R.numInhabitants())&&((M=R.fetchInhabitant(index))!=null)&&(M!=mob)&&(M.isInCombat()))
			mob.setActions(mob.actions()-Math.floor(mob.actions()));
		return false;
	}

	@Override
	public int adjustedArmor(final MOB mob)
	{
		int currDex=mob.charStats().getStat(CharStats.STAT_DEXTERITY);
		final int maxDex = mob.charStats().getMaxStat(CharStats.STAT_DEXTERITY);
		int dexBonus = 0;
		if(currDex > maxDex)
		{
			dexBonus = currDex - maxDex;
			currDex = maxDex;
		}
		double baseDex=mob.baseCharStats().getStat(CharStats.STAT_DEXTERITY);
		if(baseDex > maxDex)
			baseDex = maxDex;

		final double[] vars = {
				mob.phyStats().armor(),
				currDex,
				baseDex,
				dexBonus,
				(mob.curState().getHunger()<1)?1.0:0.0,
				(mob.curState().getThirst()<1)?1.0:0.0,
				(mob.curState().getFatigue()>CharState.FATIGUED_MILLIS)?1.0:0.0,
				CMLib.flags().isSitting(mob)?0.0:1.0,
				CMLib.flags().isSleeping(mob)?0.0:1.0
				};
		return (int)Math.round(CMath.parseMathExpression(armorAdjustmentFormula, vars, 0.0));
	}

	@Override
	public int adjustedArmor(final int armorValue)
	{
		final double[] vars = {
				armorValue,
				CharStats.VALUE_ALLSTATS_DEFAULT,
				CharStats.VALUE_ALLSTATS_DEFAULT,
				0, //dexBonus,
				0.0,//hungry
				0.0,//thirsty
				0.0,//fatigued
				1.0,//sitting
				1.0 //sleeping
				};
		return (int)Math.round(CMath.parseMathExpression(armorAdjustmentFormula, vars, 0.0));
	}

	@Override
	public boolean rollToHit(final MOB attacker, final MOB defender)
	{
		if((attacker==null)||(defender==null))
			return false;
		final double vars[] = {
			attacker.phyStats().level(),
			defender.phyStats().level(),
			attacker.phyStats().level() > defender.phyStats().level() ? 1 : -1
		};
		final boolean isPVP=(attacker.isPlayer()&&defender.isPlayer());
		final int attackerFudgeBonusAmt = (int)Math.round(CMath.parseMathExpression(isPVP?pvpAttackerFudgeBonusFormula:attackerFudgeBonusFormula, vars, 0.0));
		return rollToHit(adjustedAttackBonus(attacker,defender),adjustedArmor(defender),attackerFudgeBonusAmt);
	}

	@Override
	public boolean rollToHit(final int attack, final int defence, final int adjustment)
	{
		double myArmor= -((double)defence);
		if(myArmor==0)
			myArmor=1.0;
		else
		if(myArmor<0.0)
			myArmor=-CMath.div(1.0,myArmor);
		double hisAttack=attack;
		if(hisAttack==0.0)
			hisAttack=1.0;
		else
		if(hisAttack<0.0)
			hisAttack=-CMath.div(1.0,myArmor);
		return CMLib.dice().normalizeAndRollLess((int)Math.round(50.0*(hisAttack/myArmor)) + adjustment);
	}

	@Override
	public Set<MOB> allCombatants(final MOB mob)
	{
		final SHashSet<MOB> h=new SHashSet<MOB>();
		final Room thisRoom=mob.location();
		if(thisRoom==null)
			return null;
		if(!mob.isInCombat())
			return null;

		Set<MOB> h1=null;
		if(mob.Name().equalsIgnoreCase("nobody"))
			h1=new HashSet<MOB>();
		else
			h1=mob.getGroupMembers(new HashSet<MOB>());
		for(int m=0;m<thisRoom.numInhabitants();m++)
		{
			final MOB inhab=thisRoom.fetchInhabitant(m);
			if((inhab!=null) && (inhab!=mob))
			{
				if((inhab==mob.getVictim())
				||((inhab!=mob)
					&&(inhab.getVictim() != mob.getVictim())
					&&(CMLib.flags().isSeeable(inhab)
						||(CMLib.flags().canMove(inhab)&&(!CMLib.flags().isCloaked(inhab))))
					&&(!h1.contains(inhab))))
						h.addUnsafe(inhab);
			}
		}
		return h;

	}

	@Override
	public void makePeaceInGroup(final MOB mob)
	{
		final Set<MOB> myGroup=mob.getGroupMembers(new XTreeSet<MOB>());
		for (final MOB mob2 : myGroup)
		{
			if(mob2.isInCombat()
			&&(myGroup.contains(mob2.getVictim())))
				mob2.makePeace(true);
		}
	}

	@Override
	public void postPanic(final MOB mob, final CMMsg addHere)
	{
		if(mob==null)
			return;

		// make sure he's not already dead, or with a pending death.
		if(mob.amDead())
			return;
		if((addHere!=null)&&(addHere.trailerMsgs()!=null))
		{
			for(final CMMsg msg : addHere.trailerMsgs())
			{
				if((msg.source()==mob)
				&&((msg.sourceMinor()==CMMsg.TYP_PANIC))
				   ||(msg.sourceMinor()==CMMsg.TYP_DEATH))
					return;
			}
		}
		final CMMsg msg=CMClass.getMsg(mob,null,CMMsg.MSG_PANIC,null);
		if(addHere!=null)
			addHere.addTrailerMsg(msg);
		else
		if((mob.location()!=null)&&(mob.location().okMessage(mob,msg)))
			mob.location().send(mob,msg);
	}

	@Override
	public void postDeath(final MOB killerM, final MOB deadM, final CMMsg addHere)
	{
		if(deadM==null)
			return;
		final Room deathRoom=deadM.location();
		if(deathRoom==null)
			return;

		// make sure he's not already dead, or with a pending death.
		if(deadM.amDead())
			return;
		if((addHere!=null)&&(addHere.trailerMsgs()!=null))
		{
			for(final CMMsg msg : addHere.trailerMsgs())
			{
				if((msg.source()==deadM)
				&&((msg.sourceMinor()==CMMsg.TYP_PANIC))
				   ||(msg.sourceMinor()==CMMsg.TYP_DEATH))
					return;
			}
		}

		if((killerM!=null)&&(killerM.getVictim()==deadM))
			handleDamageSpamSummary(killerM);

		final String msp=CMLib.protocol().msp("death"+CMLib.dice().roll(1,7,0)+".wav",50);
		CMMsg msg=null;
		if((deadM.playerStats()!=null)
		&&(deadM.playerStats().getDeathPoof().length()>0))
		{
			final String dpoof=deadM.playerStats().getDeathPoof();
			msg=CMClass.getMsg(deadM,null,killerM,
					CMMsg.MSG_OK_VISUAL,"^f^*^<FIGHT^>"+dpoof+"^</FIGHT^>^?^.\n\r"+msp,
					CMMsg.MSG_OK_VISUAL,null,
					CMMsg.MSG_DEATH,"^F^<FIGHT^>"+dpoof+"</FIGHT^>^?\n\r@x1"+msp);
		}
		else
		if(isKnockedOutUponDeath(deadM,killerM))
		{
			msg=CMClass.getMsg(deadM,null,killerM,
					CMMsg.MSG_OK_VISUAL,L("^f^*^<FIGHT^>!!!!!!!!!!!!!!YOU ARE DEFEATED!!!!!!!!!!!!!!^</FIGHT^>^?^.\n\r@x1",msp),
					CMMsg.MSG_OK_VISUAL,null,
					CMMsg.MSG_DEATH,L("^F^<FIGHT^><S-NAME> is DEFEATED!!!^</FIGHT^>^?\n\r@x1",msp));
		}
		else
		{
			msg=CMClass.getMsg(deadM,null,killerM,
					CMMsg.MSG_OK_VISUAL,L("^f^*^<FIGHT^>!!!!!!!!!!!!!!YOU ARE DEAD!!!!!!!!!!!!!!^</FIGHT^>^?^.\n\r@x1",msp),
					CMMsg.MSG_OK_VISUAL,null,
					CMMsg.MSG_DEATH,L("^F^<FIGHT^><S-NAME> is DEAD!!!^</FIGHT^>^?\n\r@x1",msp));
		}
		String targetMsg = null;
		if(addHere!=null)
		{
			final Environmental E = addHere.tool();
			final Integer xtraCode=Weapon.MSG_TYPE_MAP.get(Integer.valueOf(addHere.sourceMinor()));
			targetMsg=getDeathCorpseAddOn(E,xtraCode);
			if((targetMsg!=null)&&(targetMsg.trim().length()==0))
				targetMsg=null;
		}

		CMLib.map().sendGlobalMessage(deadM,CMMsg.TYP_DEATH, CMClass.getMsg(deadM,null,killerM, CMMsg.TYP_DEATH,null, CMMsg.TYP_DEATH,null, CMMsg.TYP_DEATH,null));
		final CMMsg msg2=CMClass.getMsg(deadM,null,killerM, CMMsg.MSG_DEATH,null, CMMsg.MSG_DEATH,targetMsg, CMMsg.MSG_DEATH,null);
		if(addHere!=null)
		{
			if(deathRoom.okMessage(deadM,msg2))
			{
				addHere.addTrailerMsg(msg);
				addHere.addTrailerMsg(msg2);
			}
		}
		else
		if(deathRoom.okMessage(deadM,msg))
		{
			deathRoom.send(deadM,msg);
			if(deathRoom.okMessage(deadM,msg2))
				deathRoom.send(deadM,msg2);
		}
	}

	@Override
	public boolean postAttack(final MOB attacker, final MOB target, Item weapon)
	{
		if((attacker==null)||(!attacker.mayPhysicallyAttack(target)))
			return false;
		if((weapon==null)
		&&(attacker.isAttributeSet(MOB.Attrib.AUTODRAW)))
		{
			CMLib.commands().postDraw(attacker,false,true);
			weapon=attacker.fetchWieldedItem();
		}
		final CMMsg msg=CMClass.getMsg(attacker,target,weapon,CMMsg.MSG_WEAPONATTACK,null);
		final Room R=target.location();
		if(R!=null)
		{
			if(R.okMessage(attacker,msg))
			{
				R.send(attacker,msg);
				return msg.value()>0;
			}
		}
		return false;
	}

	protected static boolean ownerSecurityCheck(final String ownerName, final MOB mob)
	{
		return (ownerName.length()>0)
			 &&(mob!=null)
			 &&((mob.Name().equals(ownerName))
				||(mob.getLiegeID().equals(ownerName)&mob.isMarriedToLiege())
				||(CMLib.clans().checkClanPrivilege(mob, ownerName, Clan.Function.PROPERTY_OWNER)));
	}

	@Override
	public boolean mayIAttackThisVessel(final MOB mob, final PhysicalAgent defender)
	{
		String defenderOwnerName = (defender instanceof PrivateProperty) ? ((PrivateProperty)defender).getOwnerName() : "";
		// is this how we determine npc ships?
		if(((defenderOwnerName == null)||(defenderOwnerName.length()==0))&&(defender instanceof PrivateProperty))
		{
			if(!(defender instanceof ClanItem))
				return true;
			defenderOwnerName = ((ClanItem)defender).clanID();
			if((defenderOwnerName == null)||(defenderOwnerName.length()==0))
				return true;
			final Clan C=CMLib.clans().getClan(defenderOwnerName);
			final Clan srcC=CMLib.clans().findConquerableClan(mob);
			if((C!=null)
			&&(srcC!=null)
			&&(C!=srcC)
			&&(srcC.getClanRelations(defenderOwnerName)==Clan.REL_WAR))
				return true;
		}
		if(CMSecurity.isASysOp(mob) && mob.isAttributeSet(Attrib.PLAYERKILL))
			return true;
		if(CMLib.flags().isUnattackable(defender))
			return false;
		if(defender instanceof Boardable)
		{
			final Area otherArea = ((Boardable)defender).getArea();
			if(otherArea != null)
			{
				for(final Enumeration<Room> r=otherArea.getProperMap();r.hasMoreElements();)
				{
					final Room R=r.nextElement();
					if(R!=null)
					{
						for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
						{
							final MOB M=m.nextElement();
							if((M!=null)
							&&(ownerSecurityCheck(defenderOwnerName,M))
							&&(mob.mayIFight(M)))
							{
								return true;
							}
						}
					}
				}
			}
		}
		else
		if(defender instanceof Rideable)
		{
			final Rideable rideableDefender=(Rideable)defender;
			for(int i=0;i<rideableDefender.numRiders();i++)
			{
				final Rider R=rideableDefender.fetchRider(i);
				if((R instanceof MOB)
				&&(!mob.mayIFight(R)))
				{
					return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public final boolean isASiegeWeapon(final Item I)
	{
		if((I instanceof AmmunitionWeapon)
		&&(I instanceof Rideable)
		&&((AmmunitionWeapon)I).isFreeStanding()
		&&(((AmmunitionWeapon)I).requiresAmmunition()))
			return true;
		return false;
	}

	@Override
	public boolean postSiegeAttack(final MOB attacker, final SiegableItem siegeAttacker, final SiegableItem siegeTarget, final Weapon weapon, final boolean wasAHit)
	{
		// if not in combat, howd you get here? if you are, this MUST happen
		//(!mayIAttack(attacker,attackingShip, target))
		if((attacker==null)||(weapon==null))
			return false;
		final CMMsg msg=CMClass.getMsg(attacker,siegeTarget,weapon,CMMsg.MSG_WEAPONATTACK,null);
		final Room R=CMLib.map().roomLocation(siegeTarget);
		if(R!=null)
		{
			msg.setValue(wasAHit?1:0);
			if(R.okMessage(attacker,msg))
			{
				R.send(attacker,msg);
				return wasAHit;
			}
		}
		return false;
	}

	@Override
	public boolean postHealing(MOB healer,
							   final MOB target,
							   final Ability tool,
							   final int healing,
							   final int messageCode,
							   final String allDisplayMessage)
	{
		if(healer==null)
			healer=target;
		if((healer==null)||(target==null)||(target.location()==null))
			return false;
		final CMMsg msg=CMClass.getMsg(healer,target,tool,messageCode,CMMsg.MSG_HEALING,messageCode,allDisplayMessage);
		msg.setValue(healing);
		final Room R=target.location();
		if(R!=null)
		{
			if(R.okMessage(target,msg))
			{
				R.send(target,msg);
				return true;
			}
		}
		return false;
	}

	@Override
	public String replaceDamageTag(final String str, final int damage, final int damageType, final View sourceTargetSTO)
	{
		if(str==null)
			return null;
		final int replace=str.indexOf("<DAMAGE");
		if(replace < 0)
			return str;
		if(str.length() < replace+9)
			return str;
		final boolean damages = (str.charAt(replace+7)=='S') && (str.charAt(replace+8)=='>');
		final boolean damage_ = (str.charAt(replace+7)=='-') && (str.charAt(replace+8)=='>');
		final String showDamage = CMProps.getVar(CMProps.Str.SHOWDAMAGE);
		final boolean showNumbers = showDamage.equalsIgnoreCase("YES")
								||((sourceTargetSTO==CMMsg.View.SOURCE)&&showDamage.equalsIgnoreCase("SOURCE"))
								||((sourceTargetSTO==CMMsg.View.TARGET)&&showDamage.equalsIgnoreCase("TARGET"));
		if(damages)
		{
			final String hitWord=CMStrings.deleteAllofAny(standardHitWord(damageType,damage),PARENS);
			if(!showNumbers)
				return str.substring(0,replace)+hitWord+str.substring(replace+9);
			return str.substring(0,replace)+hitWord+" ("+damage+")"+ str.substring(replace+9);
		}
		else
		if(damage_)
		{
			String hitWord=standardHitWord(damageType,damage);
			if(hitWord.indexOf(')')>=0)
			{
				final String[][] allPlurals=new String[][] {
					{"(S)",""},{"(s)",""},{"(YS)",""},{"(ys)",""}
				};
				hitWord=CMStrings.replaceAlls(hitWord, allPlurals);
			}
			if(!showNumbers)
				return str.substring(0,replace)+hitWord+str.substring(replace+9);
			return str.substring(0,replace)+hitWord+" ("+damage+")"+ str.substring(replace+9);
		}
		else
		if(str.charAt(replace+7)=='>')
		{
			if(!showNumbers)
				return str.substring(0,replace)+standardHitWord(damageType,damage)+str.substring(replace+8);
			return str.substring(0,replace)+standardHitWord(damageType,damage)+" ("+damage+")"+ str.substring(replace+8);
		}
		return str;
	}

	@Override
	public void forcePeaceAllFightingAgainst(final MOB mob, final Set<MOB> exceptionSet)
	{
		final Room R=mob.location();
		if(R==null)
			return;
		for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
		{
			final MOB M=m.nextElement();
			if((M!=null)
			&&(M.getVictim()==mob)
			&&(M!=mob)
			&&((exceptionSet==null)||(!exceptionSet.contains(M))))
				M.setVictim(null);
		}
	}

	@Override
	public Set<MOB> getAllFightingAgainst(final MOB mob, Set<MOB> set)
	{
		if(set == null)
			set=new HashSet<MOB>(1);
		final Room R=mob.location();
		if(R==null)
			return set;
		for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
		{
			final MOB M=m.nextElement();
			if((M!=null)&&(M.getVictim()==mob)&&(M!=mob))
				set.add(M);
		}
		return set;
	}

	@Override
	public void postDamage(final MOB attacker, final MOB target, final Environmental weapon,
						   int damage, final int messageCode, final int damageType, String allDisplayMessage)
	{
		if((attacker==null)||(target==null)||(target.location()==null))
			return;
		if(allDisplayMessage!=null)
			allDisplayMessage="^F^<FIGHT^>"+allDisplayMessage+"^</FIGHT^>^?";

		final int damageTypeMsg;
		final int srcMsgCode = messageCode;
		String srcMsgStr=allDisplayMessage;
		if(attacker != target)
		{
			if(weapon instanceof Ability)
			{
				if((damage>0)
				&&(attacker.isMine(weapon)||(attacker.phyStats().level()>1))) // why >1? because quickly made fake-mobs tend to have lvl=1
					damage = modifySpellDamage(attacker, target, damage);
				if((((Ability)weapon).affecting() instanceof Item)
				&&(((Item)((Ability)weapon).affecting()).owner()==attacker)
				&&((((Ability)weapon).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PROPERTY)
				&&(attacker.isAttributeSet(Attrib.NOBATTLESPAM)))
					srcMsgStr=null;
			}
			damageTypeMsg = CMMsg.TYP_DAMAGE | (CMMsg.MASK_MALICIOUS & messageCode);
		}
		else
			damageTypeMsg = CMMsg.TYP_DAMAGE;

		final CMMsg msg=CMClass.getMsg(attacker,target,weapon,srcMsgCode,srcMsgStr,
															  damageTypeMsg,allDisplayMessage,
															  messageCode,allDisplayMessage);
		msg.setValue(damage);
		CMLib.color().fixSourceFightColor(msg);
		final Room R=target.location();
		if(R!=null)
		{
			if(R.okMessage(target,msg))
			{
				if(damageType>=0)
				{
					msg.modify(msg.source(),
							   msg.target(),
							   msg.tool(),
							   msg.sourceCode(),
							   replaceDamageTag(msg.sourceMessage(),msg.value(),damageType,CMMsg.View.SOURCE),
							   msg.targetCode(),
							   replaceDamageTag(msg.targetMessage(),msg.value(),damageType,CMMsg.View.TARGET),
							   msg.othersCode(),
							   replaceDamageTag(msg.othersMessage(),msg.value(),damageType,CMMsg.View.OTHERS));
				}
				R.send(target,msg);
			}
		}
	}

	public int modifySpellDamage(final MOB attacker, final MOB target, int baseDamage)
	{
		final int maxInt = attacker.charStats().getMaxStat(CharStats.STAT_INTELLIGENCE);
		int currInt = attacker.charStats().getStat(CharStats.STAT_INTELLIGENCE);
		int intBonus = 0;
		if(currInt > maxInt)
		{
			intBonus = currInt - maxInt;
			currInt = maxInt;
		}
		int baseInt = attacker.baseCharStats().getStat(CharStats.STAT_INTELLIGENCE);
		if(baseInt > maxInt)
			baseInt = maxInt;
		final double[] vars = {
				baseDamage,
				currInt,
				baseInt,
				intBonus,
				(attacker.curState().getHunger()<1)?1.0:0.0,
				(attacker.curState().getThirst()<1)?1.0:0.0,
				(attacker.curState().getFatigue()>CharState.FATIGUED_MILLIS)?1.0:0.0,
				attacker.phyStats().level(),
				target.phyStats().level()
				};
		final boolean isPVP=(attacker.isPlayer()&&target.isPlayer());
		baseDamage = (int)Math.round(CMath.parseMathExpression(isPVP?pvpSpellFudgeDamageFormula:spellFudgeDamageFormula, vars, 0.0));
		vars[0]=baseDamage;
		final int spellCritChancePct = (int)Math.round(CMath.parseMathExpression(isPVP?pvpSpellCritChanceFormula:spellCritChanceFormula, vars, 0.0))
									+ attacker.charStats().getStat(CharStats.STAT_CRIT_CHANCE_PCT_MAGIC);
		if(CMLib.dice().rollPercentage()<spellCritChancePct)
		{
			final int spellCritDamageAmt = (int)Math.round(CMath.parseMathExpression(isPVP?pvpSpellCritDmgFormula:spellCritDmgFormula, vars, 0.0));
			baseDamage+=spellCritDamageAmt;
			if(attacker.charStats().getStat(CharStats.STAT_CRIT_DAMAGE_PCT_MAGIC)>0)
				baseDamage += (int)Math.round(CMath.mul(spellCritDamageAmt,CMath.div(attacker.charStats().getStat(CharStats.STAT_CRIT_DAMAGE_PCT_MAGIC),100.0)));
		}
		return baseDamage;
	}

	@Override
	public int adjustedDamage(final MOB mob, final Weapon weapon, final MOB target, final int bonusDamage, final boolean allowCrits, final boolean biasHigh)
	{
		double damageAmount=0.0;
		Physical useDmg = null;
		boolean rangedAttack = false;
		if((weapon!=null)
		&&((weapon.weaponClassification()==Weapon.CLASS_RANGED)||(weapon.weaponClassification()==Weapon.CLASS_THROWN)))
		{
			useDmg = weapon;
			rangedAttack = true;
		}
		else
			useDmg = mob;
		final boolean isPVP=((target!=null)&&mob.isPlayer()&&target.isPlayer());
		final int maxStr = mob.charStats().getMaxStat(CharStats.STAT_STRENGTH);
		int currStr = mob.charStats().getStat(CharStats.STAT_STRENGTH);
		if(currStr > maxStr)
			currStr = maxStr;
		if(target!=null)
		{
			final double[] vars = {
					useDmg.phyStats().damage()+bonusDamage,
					currStr,
					mob.phyStats().level(),
					target.phyStats().level(),
					(mob.curState().getHunger()<1)?1.0:0.0,
					(mob.curState().getThirst()<1)?1.0:0.0,
					(mob.curState().getFatigue()>CharState.FATIGUED_MILLIS)?1.0:0.0,
					CMLib.flags().canBeSeenBy(target,mob)?0:1,
					CMLib.flags().isSleeping(target)?1:0,
					CMLib.flags().isSitting(target)?1:0
			};
			CMath.CompiledFormula formula;
			if(rangedAttack)
				formula = isPVP?pvpTargetedRangedDamageFormula:targetedRangedDamageFormula;
			else
				formula = isPVP?pvpTargetedMeleeDamageFormula:targetedMeleeDamageFormula;
			if(biasHigh)
				damageAmount = CMath.parseMathExpression(formula, CMath.NotRandomHigh, vars, 0.0);
			else
				damageAmount = CMath.parseMathExpression(formula, vars, 0.0);
		}
		else
		{
			final double[] vars = {
					useDmg.phyStats().damage()+bonusDamage,
					currStr,
					mob.phyStats().level(),
					0,
					(mob.curState().getHunger()<1)?1.0:0.0,
					(mob.curState().getThirst()<1)?1.0:0.0,
					(mob.curState().getFatigue()>CharState.FATIGUED_MILLIS)?1.0:0.0,
					0,
					0,
					0
			};
			CMath.CompiledFormula formula;
			if((weapon!=null)&&((weapon.weaponClassification()==Weapon.CLASS_RANGED)||(weapon.weaponClassification()==Weapon.CLASS_THROWN)))
				formula = staticRangedDamageFormula;
			else
				formula = staticMeleeDamageFormula;
			if(biasHigh)
				damageAmount = CMath.parseMathExpression(formula, CMath.NotRandomHigh, vars, 0.0);
			else
				damageAmount = CMath.parseMathExpression(formula, vars, 0.0);
		}

		final int maxDex = mob.charStats().getMaxStat(CharStats.STAT_DEXTERITY);
		int currDex = mob.charStats().getStat(CharStats.STAT_DEXTERITY);
		int dexBonus = 0;
		if(currDex > maxDex)
		{
			dexBonus = currDex - maxDex;
			currDex = maxDex;
		}
		int baseDex = mob.baseCharStats().getStat(CharStats.STAT_DEXTERITY);
		if(baseDex > maxDex)
			baseDex = maxDex;

		final double[] vars = {
				damageAmount,
				currDex,
				baseDex,
				dexBonus,
				(mob.curState().getHunger()<1)?1.0:0.0,
				(mob.curState().getThirst()<1)?1.0:0.0,
				(mob.curState().getFatigue()>CharState.FATIGUED_MILLIS)?1.0:0.0,
				mob.phyStats().level(),
				(target==null)?0:target.phyStats().level()
		};
		if(allowCrits)
		{
			final int weaponCritChancePct = (int)Math.round(CMath.parseMathExpression(isPVP?pvpWeaponCritChanceFormula:weaponCritChanceFormula, vars, 0.0))
											+ mob.charStats().getStat(CharStats.STAT_CRIT_CHANCE_PCT_WEAPON);
			if((CMLib.dice().rollPercentage()<weaponCritChancePct)||(biasHigh))
			{
				final CMath.CompiledFormula formula = isPVP?pvpWeaponCritDmgFormula:weaponCritDmgFormula;
				final int weaponCritDmgAmt;
				if(biasHigh)
					weaponCritDmgAmt = (int)Math.round(CMath.parseMathExpression(formula, CMath.NotRandomHigh, vars, 0.0));
				else
					weaponCritDmgAmt = (int)Math.round(CMath.parseMathExpression(formula, vars, 0.0));
				damageAmount += weaponCritDmgAmt;
				if(mob.charStats().getStat(CharStats.STAT_CRIT_DAMAGE_PCT_WEAPON)>0)
					damageAmount += (int)Math.round(CMath.mul(weaponCritDmgAmt,CMath.div(mob.charStats().getStat(CharStats.STAT_CRIT_DAMAGE_PCT_WEAPON),100.0)));
			}
		}
		if(target != null)
		{
			vars[0] = damageAmount;
			CMath.CompiledFormula formula;
			if(rangedAttack)
				formula = isPVP?pvpRangedFudgeDamageFormula:rangedFudgeDamageFormula;
			else
				formula = isPVP?pvpMeleeFudgeDamageFormula:meleeFudgeDamageFormula;
			if(biasHigh)
				damageAmount = CMath.parseMathExpression(formula, CMath.NotRandomHigh, vars, 0.0);
			else
				damageAmount = CMath.parseMathExpression(formula, vars, 0.0);
		}
		return (int)Math.round(damageAmount);
	}

	@Override
	public int adjustedDamage(final int baseDamage, final int level, final boolean biasHigh)
	{
		double damageAmount=0.0;
		final double[] vars = {
				baseDamage,
				CharStats.VALUE_ALLSTATS_DEFAULT,
				level,
				0,
				0.0,//hunger
				0.0,//thirst
				0.0,//fatigue
				0,
				0,
				0
		};
		if(biasHigh)
			damageAmount = CMath.parseMathExpression(staticMeleeDamageFormula, CMath.NotRandomHigh, vars, 0.0);
		else
			damageAmount = CMath.parseMathExpression(staticMeleeDamageFormula, vars, 0.0);
		return (int)Math.round(damageAmount);
	}

	@Override
	public boolean recoverTick(final MOB mob)
	{
		if((mob!=null)
		&&(!mob.isInCombat()))
		{
			final CharStats charStats=mob.charStats();
			final CharState curState=mob.curState();
			final CharState maxState=mob.maxState();
			if((curState.getHitPoints()<maxState.getHitPoints())
			||(curState.getMana()<maxState.getMana())
			||(curState.getMovement()<maxState.getMovement()))
			{
				final Room R=mob.location();
				final Area A=(R!=null)?R.getArea():null;
				if((A instanceof Boardable)
				&&(((Boardable)A).getBoardableItem() instanceof Combatant)
				&&(((Combatant)((Boardable)A).getBoardableItem()).isInCombat()))
					return false;

				final Rideable riding=mob.riding();
				final boolean isSleeping=CMLib.flags().isSleeping(mob);
				final boolean bedBonus=isSleeping && (riding!=null) && (riding.rideBasis()==Rideable.Basis.FURNITURE_SLEEP);
				final boolean isSittingOrRiding=((!isSleeping) && ((CMLib.flags().isSitting(mob))||(mob.riding()!=null))) || bedBonus;
				final boolean isFlying=((!isSleeping) && (!isSittingOrRiding) && CMLib.flags().isFlying(mob)) || bedBonus;
				final boolean isSwimming=(!isSleeping) && (!isSittingOrRiding) && (!isFlying) && CMLib.flags().isSwimming(mob);
				final double[] vals=new double[]{
					charStats.getStat(CharStats.STAT_CONSTITUTION),
					mob.phyStats().level(),
					(curState.getHunger()<1)?1.0:0.0,
					(curState.getThirst()<1)?1.0:0.0,
					(curState.getFatigue()>CharState.FATIGUED_MILLIS)?1.0:0.0,
					isSleeping?1.0:0.0,
					isSittingOrRiding?1.0:0.0,
					isFlying?1.0:0.0,
					isSwimming?1.0:0.0
				};
				boolean changed = false;
				final double hpGain = CMath.parseMathExpression(stateHitPointRecoverFormula, vals, 0.0);

				if((hpGain>0)&&(!CMLib.flags().isGolem(mob)))
					changed = !curState.adjHitPoints((int)Math.round(hpGain),maxState) || changed;

				vals[0]=((charStats.getStat(CharStats.STAT_INTELLIGENCE)+charStats.getStat(CharStats.STAT_WISDOM)));
				final double manaGain = CMath.parseMathExpression(stateManaRecoverFormula, vals, 0.0);

				if(manaGain>0)
					changed = !curState.adjMana((int)Math.round(manaGain),maxState) || changed;

				vals[0]=charStats.getStat(CharStats.STAT_STRENGTH);
				final double moveGain = CMath.parseMathExpression(this.stateMovesRecoverFormula, vals, 0.0);

				if(moveGain>0)
					changed = !curState.adjMovement((int)Math.round(moveGain),maxState) || changed;

				if(((hpGain>0)||(manaGain>0)||(moveGain>0))
				&&(!isSleeping)
				&&(!isSittingOrRiding)
				&&(!CMSecurity.isDisabled(DisFlag.FATIGUE))
				&&(!mob.charStats().getMyRace().infatigueable()))
					mob.curState().adjFatigue(Math.round(CMProps.getTickMillis()), mob.maxState());
				return changed;
			}
		}
		return false;
	}

	@Override
	public CMMsg postWeaponDamage(final MOB source, final MOB target, final Item item, int damageInt)
	{
		int damageType=Weapon.TYPE_BASHING;
		Weapon weapon=null;
		if(item instanceof Weapon)
		{
			weapon=(Weapon)item;
			damageType=weapon.weaponDamageType();
		}
		// calculate Base Damage (with Strength bonus)
		final String oldHitString="^F^<FIGHT^>"+((weapon!=null)?
							weapon.hitString(damageInt):
							standardHitString(Weapon.TYPE_NATURAL,Weapon.CLASS_BLUNT,damageInt,item.name()))+"^</FIGHT^>^?";
		final CMMsg msg=CMClass.getMsg(source,
								target,
								item,
								CMMsg.MSG_OK_VISUAL,
								CMMsg.MSG_DAMAGE,
								CMMsg.MSG_OK_VISUAL,
								oldHitString);
		CMLib.color().fixSourceFightColor(msg);

		msg.setValue(damageInt);
		// why was there no okaffect here?
		final Room room=source.location();
		if((room!=null)&&(room.okMessage(source,msg)))
		{
			if(msg.targetMinor()==CMMsg.TYP_DAMAGE)
			{
				damageInt=msg.value();
				msg.modify(msg.source(),
						   msg.target(),
						   msg.tool(),
						   msg.sourceCode(),
						   replaceDamageTag(msg.sourceMessage(),msg.value(),damageType,CMMsg.View.SOURCE),
						   msg.targetCode(),
						   replaceDamageTag(msg.targetMessage(),msg.value(),damageType,CMMsg.View.TARGET),
						   msg.othersCode(),
						   replaceDamageTag(msg.othersMessage(),msg.value(),damageType,CMMsg.View.OTHERS));
			}
			if(source.mayIFight(target))
			{
				if((msg.source().riding() instanceof Boardable)
				&&(CMLib.combat().isASiegeWeapon(item)))
				{
					room.send(source,msg);
					return msg;
				}
				else
				if((source.location()==room)
				&&(target.location()==room))
				{
					room.send(source,msg);
					return msg;
				}
			}
		}
		return null;
	}

	@Override
	public CMMsg postWeaponAttackResult(final MOB sourceM, final MOB targetM, final Item item, final int bonusDmg, final boolean success)
	{
		if(sourceM==null)
			return null;
		if(!sourceM.mayIFight(targetM))
			return null;
		Weapon weapon=null;
		int damageInt = 0;
		if(item instanceof Weapon)
		{
			weapon=(Weapon)item;
			damageInt=adjustedDamage(sourceM,weapon,targetM,0,true,false);
			if(damageInt > 0)
				damageInt += bonusDmg;
			if(damageInt < 0)
				damageInt = 0;
		}
		if(success)
		{
			if((sourceM.playerStats()!=null)
			&&(sourceM!=targetM))
				sourceM.playerStats().bumpLevelCombatStat(PlayerCombatStat.HITS_DONE, sourceM.basePhyStats().level(), 1);
			if(targetM.playerStats()!=null)
				targetM.playerStats().bumpLevelCombatStat(PlayerCombatStat.HITS_TAKEN, targetM.basePhyStats().level(), 1);
			postWeaponDamage(sourceM,targetM,item,damageInt);
		}
		else
		{
			final String missString="^F^<FIGHT^>"+((weapon!=null)?
								weapon.missString():
								standardMissString(Weapon.TYPE_BASHING,Weapon.CLASS_BLUNT,item.name(),false))+"^</FIGHT^>^?";
			final CMMsg msg=CMClass.getMsg(sourceM,
									targetM,
									weapon,
									CMMsg.MSG_ATTACKMISS,
									missString);
			CMLib.color().fixSourceFightColor(msg);
			// why was there no okaffect here?
			final Room R=sourceM.location();
			if(R!=null)
			if(R.okMessage(sourceM,msg) && (!sourceM.amDead()) && (!sourceM.amDestroyed()))
			{
				R.send(sourceM,msg);
				return msg;
			}
		}
		return null;
	}

	@Override
	public void postSiegeDamage(final MOB source, final PhysicalAgent attacker, final PhysicalAgent defender,
								final Environmental weapon, final String oldHitString,
								final int damageType, int damageInt)
	{
		final CMMsg msg=CMClass.getMsg(source,
				defender,
				weapon,
				CMMsg.MSG_OK_VISUAL,
				CMMsg.MSG_DAMAGE,
				CMMsg.MSG_OK_VISUAL,
				oldHitString);
		CMLib.color().fixSourceFightColor(msg);
		msg.setValue(damageInt);
		// why was there no okaffect here?
		final Room room=CMLib.map().roomLocation(attacker);
		if((room!=null)&&(room.okMessage(source,msg)))
		{
			if(msg.targetMinor()==CMMsg.TYP_DAMAGE)
			{
				damageInt=msg.value();
				msg.modify(msg.source(),
						   msg.target(),
						   msg.tool(),
						   msg.sourceCode(),
						   replaceDamageTag(msg.sourceMessage(),msg.value(),damageType,CMMsg.View.SOURCE),
						   msg.targetCode(),
						   replaceDamageTag(msg.targetMessage(),msg.value(),damageType,CMMsg.View.TARGET),
						   msg.othersCode(),
						   replaceDamageTag(msg.othersMessage(),msg.value(),damageType,CMMsg.View.OTHERS));
			}
			if(//(mayIAttack(source,attacker,defender))&&
			(CMLib.map().roomLocation(attacker)==room)
			&&(CMLib.map().roomLocation(defender)==room))
				room.send(source,msg);
		}
	}

	@Override
	public void postSiegeWeaponAttackResult(final MOB source, final PhysicalAgent attacker, final PhysicalAgent defender, final Weapon weapon, final boolean success)
	{
		if(source==null)
			return;
		// if you aren't in combat, how'd you get here.
		// if you are in combat, this needs to happen regardless
		//if(!mayIAttack(source, attacker, defender))
		//	return;
		final int damageInt=adjustedDamage(source,weapon,null,0,false,false);
		int damageType=Weapon.TYPE_BASHING;
		if(weapon != null)
			damageType= weapon.weaponDamageType();
		final Room room=CMLib.map().roomLocation(attacker);
		if(success)
		{
			// calculate Base Damage (with Strength bonus)
			final String oldHitString="^F^<FIGHT^>"+((weapon!=null)?
								weapon.hitString(damageInt):
								standardHitString(Weapon.TYPE_NATURAL,Weapon.CLASS_BLUNT,(int)Math.round(Math.pow(2,damageInt)),attacker.Name()))+"^</FIGHT^>^?";
			postSiegeDamage(source, attacker, defender, weapon, oldHitString, damageType, damageInt);
		}
		else
		{
			final String missString="^F^<FIGHT^>"+((weapon!=null)?
								weapon.missString():
								standardMissString(Weapon.TYPE_BASHING,Weapon.CLASS_BLUNT,attacker.name(),false))+"^</FIGHT^>^?";
			final CMMsg msg=CMClass.getMsg(source,
											defender,
											weapon,
											CMMsg.MSG_ATTACKMISS,
											missString);
			CMLib.color().fixSourceFightColor(msg);
			// why was there no okaffect here?
			if((room!=null)
			&&(room.okMessage(source,msg)))
				room.send(source,msg);
		}
	}

	protected List<MOB>[] processFormation(List<MOB>[] done, final MOB leader, final int baseLevel, final int relOrder)
	{
		for (final List<MOB> element : done)
		{
			if((element!=null)
			&&(element.contains(leader)))
				return done;
		}
		final int level = baseLevel + relOrder;
		if(level>=done.length)
			done = Arrays.copyOf(done, level+1);
		if(done[level]==null)
			done[level]=new Vector<MOB>();
		done[level].add(leader);
		for(int f=0;f<leader.numFollowers();f++)
		{
			final MOB M=leader.fetchFollower(f);
			if(M==null)
				continue;
			int order=leader.fetchFollowerOrder(M);
			if(order<0)
				order=0;
			done = processFormation(done, M, level, order-level);
		}
		return done;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<MOB>[] getFormation(final MOB mob)
	{
		final MOB leader=mob.getGroupLeader();
		int leaderLevel = leader.fetchFollowerOrder(leader);
		if(leaderLevel < 0)
			leaderLevel = 0;
		final List<MOB>[] first = new List[] { new Vector<MOB>() };
		return processFormation(first, leader, 0, leaderLevel);
	}

	@Override
	public List<MOB> getFormationFollowed(final MOB mob)
	{
		final List<MOB>[] form=getFormation(mob);
		for(int i=1;i<form.length;i++)
		{
			if((form[i]!=null)
			&&(form[i].contains(mob)))
			{
				i--;
				while(i>=0)
				{
					if((form[i]!=null)
					&&(form[i].size()>0))
						return form[i];
					i--;
				}
			}
		}
		return null;
	}

	@Override
	public int getFormationAbsOrder(final MOB mob)
	{
		final List<MOB>[] form=getFormation(mob);
		for(int i=1;i<form.length;i++)
		{
			if((form[i]!=null)
			&&(form[i].contains(mob)))
				return i;
		}
		return 0;
	}

	public MOB getCombatDominentKiller(final MOB killer, final MOB killed)
	{
		if((!killer.isMonster())&&(killer.charStats()!=null))
			return killer;
		MOB M=killer;
		Set<MOB> checked=new HashSet<MOB>();
		checked.add(M);
		while(M.isMonster()
		&&(M.amFollowing()!=null)
		&&(!checked.contains(M.amFollowing())))
		{
			M=M.amFollowing();
			checked.add(M);
		}
		if((!M.isMonster())&&(M.charStats()!=null))
			return M;
		checked.clear();
		checked=killer.getGroupMembers(checked);
		for(final Iterator<MOB> m=checked.iterator(); m.hasNext(); )
		{
			M=m.next();
			if((!M.isMonster())&&(M.charStats()!=null))
				return M;
		}
		return killer;
	}

	@Override
	public CharClass getCombatDominantClass(final MOB killer, final MOB killed)
	{
		CharClass C=null;

		if((killer!=null)&&(killer.charStats()!=null))
		{
			C=killer.charStats().getCurrentClass();
			MOB M=killer;
			final HashSet<MOB> checked=new HashSet<MOB>();
			checked.add(M);
			while(M.isMonster()
			&&(M.amFollowing()!=null)
			&&(!checked.contains(M.amFollowing())))
			{
				M=M.amFollowing();
				checked.add(M);
			}
			if((!M.isMonster())&&(M.charStats()!=null))
				C=M.charStats().getCurrentClass();
		}
		else
			C=CMClass.getCharClass("StdCharClass");
		return C;
	}

	protected Set<MOB> getCombatBeneficiaries(final MOB killer, final MOB killed, final Room deathRoom, final Set<MOB> beneficiaries, CharClass combatCharClass)
	{
		final Set<MOB> followers=(killer!=null)?killer.getGroupMembers(new HashSet<MOB>()):(new SHashSet<MOB>());
		if(combatCharClass==null)
			combatCharClass=CMClass.getCharClass("StdCharClass");
		if(deathRoom!=null)
		{
			for(int m=0;m<deathRoom.numInhabitants();m++)
			{
				final MOB mob=deathRoom.fetchInhabitant(m);
				if((combatCharClass.isValidClassBeneficiary(killer,killed,mob,followers))
				&&(!beneficiaries.contains(mob)))
					beneficiaries.add(mob);
			}
		}
		if((killer!=null)&&(!beneficiaries.contains(killer))&&(killer!=killed)&&(CMLib.flags().isInTheGame(killer,true)))
			beneficiaries.add(killer);
		return beneficiaries;
	}

	@Override
	public Set<MOB> getCombatBeneficiaries(final MOB killer, final MOB killed, final CharClass combatCharClass)
	{
		if((killer==null)||(killed==null))
			return new SHashSet<MOB>();
		final SHashSet<MOB> beneficiaries=new SHashSet<MOB>();
		Room R=killer.location();
		if(R!=null)
			getCombatBeneficiaries(killer,killed,R,beneficiaries,combatCharClass);
		R=killed.location();
		if((R!=null)&&(R!=killer.location()))
			getCombatBeneficiaries(killer,killed,R,beneficiaries,combatCharClass);
		return beneficiaries;
	}

	protected Set<MOB> getCombatDividers(final MOB killer, final MOB killed, final Room deathRoom, final Set<MOB> dividers, CharClass combatCharClass)
	{
		final Set<MOB> followers=(killer!=null)?killer.getGroupMembers(new HashSet<MOB>()):(new HashSet<MOB>());
		if(combatCharClass==null)
			combatCharClass=CMClass.getCharClass("StdCharClass");
		if(deathRoom!=null)
		{
			for(int m=0;m<deathRoom.numInhabitants();m++)
			{
				final MOB mob=deathRoom.fetchInhabitant(m);
				if((combatCharClass.isValidClassDivider(killer,killed,mob,followers))
				&&(!dividers.contains(mob)))
					dividers.add(mob);
			}
		}
		if((killer!=null)&&(!dividers.contains(killer))&&(killer!=killed)&&(CMLib.flags().isInTheGame(killer,true)))
			dividers.add(killer);
		return dividers;
	}

	@Override
	public Set<MOB> getCombatDividers(final MOB killer, final MOB killed, final CharClass combatCharClass)
	{
		if((killer==null)||(killed==null))
			return new SHashSet<MOB>();
		final Set<MOB> dividers=new SHashSet<MOB>();
		Room R=killer.location();
		if(R!=null)
			getCombatDividers(killer,killed,R,dividers,combatCharClass);
		R=killed.location();
		if((R!=null)&&(R!=killer.location()))
			getCombatDividers(killer,killed,R,dividers,combatCharClass);
		return dividers;
	}

	protected DeadBody justDie(final MOB killerM, final MOB deadM, final String specialCorpseDescription)
	{
		if(deadM==null)
			return null;
		final Room deathRoom=deadM.location();
		if(deathRoom == null)
			return null;
		if(deadM.isPlayer())
		{
			if(killerM != null)
				CMLib.achievements().possiblyBumpAchievement(deadM, AchievementLibrary.Event.DEATHS, 1, killerM);
			else
				CMLib.achievements().possiblyBumpAchievement(deadM, AchievementLibrary.Event.DEATHS, 1, deadM);
		}

		final Session killerSess=(killerM==null)?null:killerM.session();
		final Session deadMSess=deadM.session();

		//TODO: this creates too many loops.  The right thing is to loop once
		// and call  a boolean function to populate all these lists.
		final CharClass combatCharClass=getCombatDominantClass(killerM,deadM);
		final Set<MOB> beneficiaries=getCombatBeneficiaries(killerM,deadM,combatCharClass);
		final Set<MOB> hisGroupH=deadM.getGroupMembers(new HashSet<MOB>());
		for(final Enumeration<MOB> m=deathRoom.inhabitants();m.hasMoreElements();)
		{
			final MOB M=m.nextElement();
			if((M!=null)
			&&(M.getVictim()==deadM))
				pickNextVictim(M, deadM, hisGroupH);
		}
		final Set<MOB> dividers=getCombatDividers(killerM,deadM,combatCharClass);

		if(killerM != null)
			CMLib.get(killerSess)._combat().dispenseExperience(beneficiaries,dividers,deadM);
		else
			CMLib.get(deadMSess)._combat().dispenseExperience(beneficiaries,dividers,deadM);

		final String currency=CMLib.beanCounter().getCurrency(deadM);
		final double deadMoney=CMLib.beanCounter().getTotalAbsoluteValue(deadM,currency);
		double myAmountOfDeadMoney=0.0;
		final List<MOB> goldLooters=new ArrayList<MOB>();
		for (final MOB M : beneficiaries)
		{
			if(((M.isAttributeSet(MOB.Attrib.AUTOGOLD))
			&&(!goldLooters.contains(M)))
			&&(M!=deadM)
			&&(M.location()==deathRoom)
			&&(deathRoom.isInhabitant(M)))
				goldLooters.add(M);
		}
		if((goldLooters.size()>0)&&(deadMoney>0))
		{
			myAmountOfDeadMoney=CMath.div(deadMoney,goldLooters.size());
			CMLib.beanCounter().subtractMoney(deadM,deadMoney);
		}

		final int[] expLost={100*deadM.phyStats().level()};
		if(expLost[0]<100)
			expLost[0]=100;
		String[] cmds=null;
		if((deadM.isMonster())||(deadM.soulMate()!=null))
			cmds=CMParms.toStringArray(CMParms.parseCommas(CMProps.getVar(CMProps.Str.MOBDEATH),true));
		else
			cmds=CMParms.toStringArray(CMParms.parseCommas(CMProps.get(deadM.session()).getStr(CMProps.Str.PLAYERDEATH),true));

		DeadBody body=null; //must be done before consequences because consequences could be purging
		if((!CMParms.containsIgnoreCase(cmds,"RECALL"))
		&&(!isKnockedOutUponDeath(deadM,killerM)))
		{
			body=(DeadBody)deadM.killMeDead(true);
			if((body!=null)&&(specialCorpseDescription!=null)&&(specialCorpseDescription.length()>0))
			{
				String displayText = body.displayText();
				if(displayText.endsWith(".")||displayText.endsWith("!")||displayText.endsWith("?"))
				{
					displayText=displayText.substring(0,displayText.length()-1)
							+ ", "+specialCorpseDescription
							+ displayText.charAt(displayText.length()-1);
				}
				else
					displayText += ", "+specialCorpseDescription;
				body.setDisplayText(displayText);
			}
		}

		if(deadM.isPlayer())
			deadM.playerStats().deathCounter(1);
		final boolean stillExists = handleCombatLossConsequences(deadM,killerM,cmds,expLost,"^*You lose @x1 experience points.^?^.");
		if(!isKnockedOutUponDeath(deadM,killerM))
		{
			final Room bodyRoom;
			if((body!=null)
			&&(body.owner() instanceof Room)
			&&(((Room)body.owner()).isContent(body)))
				bodyRoom=(Room)body.owner();
			else
				bodyRoom=deathRoom;
			if((killerM!=null)&&(body!=null))
			{
				body.setKillerName(killerM.Name());
				body.setIsKillerPlayer(!killerM.isMonster());
				body.setKillerTool(killerM.fetchWieldedItem());
				if(body.getKillerTool()==null)
					body.setKillerTool(killerM.getNaturalWeapon());
			}

			if((!deadM.isMonster())
			&&(CMLib.dice().rollPercentage()==1)
			&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.AUTODISEASE))
			&&(stillExists))
			{
				final Ability A=CMClass.getAbility("Disease_Amnesia");
				if((A!=null)
				&&(deadM.fetchEffect(A.ID())==null)
				&&(!CMSecurity.isAbilityDisabled(A.ID())))
					A.invoke(deadM,deadM,true,0);
			}

			if(deadM.soulMate()!=null)
			{
				final Session s=deadM.session();
				s.setMob(deadM.soulMate());
				deadM.soulMate().setSession(s);
				deadM.setSession(null);
				deadM.soulMate().tell(L("^HYour spirit has returned to your body...\n\r\n\r^N"));
				CMLib.commands().postLook(deadM.soulMate(),true);
				deadM.setSoulMate(null);
			}

			if((killerM!=null)
			&&(bodyRoom!=null)
			&&(body!=null)
			&&(killerM.location()==bodyRoom)
			&&(bodyRoom.isInhabitant(killerM))
			&&(killerM.isAttributeSet(MOB.Attrib.AUTOLOOT)))
			{
				if((killerM.riding()!=null)&&(killerM.riding() instanceof MOB))
					killerM.tell(L("You'll need to dismount to loot the body."));
				else
				if((killerM.riding()!=null)&&(killerM.riding() instanceof MOB))
					killerM.tell(L("You'll need to disembark to loot the body."));
				else
				for(int i=bodyRoom.numItems()-1;i>=0;i--)
				{
					final Item item=bodyRoom.getItem(i);
					if((item!=null)
					&&(item.container()==body)
					&&(CMLib.flags().canBeSeenBy(body,killerM))
					&&((!body.isDestroyedAfterLooting())||(!(item instanceof RawMaterial)))
					&&(CMLib.flags().canBeSeenBy(item,killerM)))
						CMLib.commands().postGet(killerM,body,item,false);
				}
				if(body.isDestroyedAfterLooting())
					bodyRoom.recoverRoomStats();
			}

			Coins C=null;
			if((deadMoney>0)
			&&(myAmountOfDeadMoney>0)
			&&(body!=null)
			&&(bodyRoom!=null))
			{
				for(int g=0;g<goldLooters.size();g++)
				{
					C=CMLib.beanCounter().makeBestCurrency(currency,myAmountOfDeadMoney,null,body);
					if(C!=null)
					{
						C.recoverPhyStats();
						bodyRoom.addItem(C,ItemPossessor.Expire.Monster_EQ);
						bodyRoom.recoverRoomStats();
						final MOB mob=goldLooters.get(g);
						if(mob.location()==bodyRoom)
						{
							if((mob.riding()!=null)&&(mob.riding() instanceof MOB))
								mob.tell(L("You'll need to dismount to get @x1 off the body.",C.name()));
							else
							if((mob.riding()!=null)&&(mob.riding() instanceof Item))
								mob.tell(L("You'll need to disembark to get @x1 off the body.",C.name()));
							else
							if(CMLib.flags().canBeSeenBy(body,mob))
								CMLib.commands().postGet(mob,body,C,false);
						}
					}
				}
			}

			if((killerM != null)
			&&(killerM.getVictim()==deadM))
				killerM.setVictim(null);
			deadM.setVictim(null);
			if((body!=null)
			&&(bodyRoom!=null)
			&&(body.isDestroyedAfterLooting()))
			{
				for(int i=bodyRoom.numItems()-1;i>=0;i--)
				{
					final Item item=bodyRoom.getItem(i);
					if((item!=null)
					&&(item.container()==body))
						item.setContainer(null);
				}
				body.destroy();
				bodyRoom.recoverPhyStats();
				return null;
			}
			return body;
		}
		return null;
	}

	@Override
	public String standardHitWord(int type, final double pct)
	{
		if((type<0)||(type>=Weapon.TYPE_DESCS.length))
			type=Weapon.TYPE_BURSTING;
		final int[] thresholds=CMProps.getListFileIntList(CMProps.ListFile.DAMAGE_WORDS_THRESHOLDS);
		int damnCode=(int)Math.round(pct * (thresholds.length-1));
		if(damnCode > thresholds.length-1)
			damnCode = thresholds.length-1;
		return getStandardHitWordInternal(type, damnCode);
	}

	protected String getDeathCorpseAddOn(final Environmental E, final Integer xtraCode)
	{
		if((E==null)&&(xtraCode==null))
			return null;
		final Object[][][] addOns = CMProps.getListFileGrid(CMProps.ListFile.CORPSE_BLURBS);
		final Object[] hitWords = null;
		if(E instanceof Ability)
		{
			final String ID = ((Ability)E).ID().trim();
			for (final Object[][] addOnList : addOns)
			{
				if(((String)addOnList[0][0]).equalsIgnoreCase(ID))
				{
					final Object[] pick = (Object[])CMLib.dice().pick(addOnList, addOnList[0]);
					return (pick != null)?(String)pick[0]:null;
				}
			}
		}
		else
		if(E instanceof Weapon)
		{
			try
			{
				final String weaponClassName=Weapon.CLASS_DESCS[((Weapon)E).weaponClassification()];
				final String damageTypeName=Weapon.TYPE_DESCS[((Weapon)E).weaponDamageType()];
				for (final Object[][] addOnList : addOns)
				{
					if(((String)addOnList[0][0]).equalsIgnoreCase(damageTypeName))
					{
						final Object[] pick = (Object[])CMLib.dice().pick(addOnList, addOnList[0]);
						return (pick != null)?(String)pick[0]:null;
					}
				}
				if(hitWords == null)
				{
					for (final Object[][] addOnList : addOns)
					{
						if(((String)addOnList[0][0]).equalsIgnoreCase(weaponClassName))
						{
							final Object[] pick = (Object[])CMLib.dice().pick(addOnList, addOnList[0]);
							return (pick != null)?(String)pick[0]:null;
						}
					}
				}
			}
			catch(final Exception e)
			{}
		}
		if((hitWords == null)&&(xtraCode!=null)&&(xtraCode.intValue()>=0))
		{
			try
			{
				final String damageTypeName=Weapon.TYPE_DESCS[xtraCode.intValue()];
				for (final Object[][] addOnList : addOns)
				{
					if(((String)addOnList[0][0]).equalsIgnoreCase(damageTypeName))
					{
						final Object[] pick = (Object[])CMLib.dice().pick(addOnList, addOnList[0]);
						return (pick != null)?(String)pick[0]:null;
					}
				}
			}
			catch(final Exception e)
			{}
		}
		return null;
	}


	protected String getStandardHitWordInternal(final int type, int damnCode)
	{
		damnCode++; // always add 1 because index into hitwords is type=0, annoy=1;
		final Object[][][] hitWords = CMProps.getListFileGrid(CMProps.ListFile.DAMAGE_WORDS);
		if(hitWords != hitWordsChanged)
		{
			hitWordsChanged=hitWords;
			hitWordIndex=null;
		}
		if(hitWordIndex==null)
		{
			final Object[][][] newWordIndex=new Object[Weapon.TYPE_DESCS.length][][];
			for(int w=0;w<Weapon.TYPE_DESCS.length;w++)
			{
				Object[][] ALL=null;
				Object[][] MINE=null;
				for (final Object[][] hitWord : hitWords)
				{
					if(((String)hitWord[0][0]).equalsIgnoreCase("ALL"))
						ALL=hitWord;
					else
					if(((String)hitWord[0][0]).equalsIgnoreCase(Weapon.TYPE_DESCS[w]))
					{
						MINE = hitWord;
						break;
					}
				}
				if(MINE!=null)
					newWordIndex[w]=MINE;
				else
					newWordIndex[w]=ALL;
			}
			hitWordIndex=newWordIndex;
		}
		final Object[][] HIT_WORDS=hitWordIndex[type];
		if(damnCode<1)
			damnCode=1;
		if(damnCode>=HIT_WORDS.length)
			damnCode=HIT_WORDS.length-1;
		return (String)CMLib.dice().pick(HIT_WORDS[damnCode]);
	}

	@Override
	public String standardHitWord(int type, final int damage)
	{
		if((type<0)||(type>=Weapon.TYPE_DESCS.length))
			type=Weapon.TYPE_BURSTING;
		final int[] thresholds=CMProps.getListFileIntList(CMProps.ListFile.DAMAGE_WORDS_THRESHOLDS);
		int damnCode=thresholds.length-2;
		for(int i=0;i<thresholds.length;i++)
		{
			if (damage <= thresholds[i])
			{
				damnCode = i;
				break;
			}
		}
		return getStandardHitWordInternal(type, damnCode);
	}

	protected String getExtremeValue(final int extreme)
	{
		final StringBuilder str=new StringBuilder("");
		for(final char c : CMath.convertToRoman(extreme).toCharArray())
		{
			switch(c)
			{
			case 'I':
				str.append(CMProps.getListFileChoiceFromIndexedList(CMProps.ListFile.EXTREME_ADVS,0)).append(" ");
				break;
			case 'V':
				str.append(CMProps.getListFileChoiceFromIndexedList(CMProps.ListFile.EXTREME_ADVS,1)).append(" ");
				break;
			case 'X':
				str.append(CMProps.getListFileChoiceFromIndexedList(CMProps.ListFile.EXTREME_ADVS,2)).append(" ");
				break;
			case 'L':
				str.append(CMProps.getListFileChoiceFromIndexedList(CMProps.ListFile.EXTREME_ADVS,3)).append(" ");
				break;
			case 'C':
				str.append(CMProps.getListFileChoiceFromIndexedList(CMProps.ListFile.EXTREME_ADVS,4)).append(" ");
				break;
			case 'D':
				str.append(CMProps.getListFileChoiceFromIndexedList(CMProps.ListFile.EXTREME_ADVS,5)).append(" ");
				break;
			case 'M':
				str.append(CMProps.getListFileChoiceFromIndexedList(CMProps.ListFile.EXTREME_ADVS,6)).append(" ");
				break;
			}
		}
		return str.toString();
	}

	@Override
	public String armorStr(final MOB mob)
	{
		final int prowessCode = CMProps.getIntVar(CMProps.Int.COMBATPROWESS);
		if(CMProps.Int.Prowesses.NONE.is(prowessCode))
			return "";
		final int armor = -adjustedArmor(mob);
		final StringBuilder str=new StringBuilder("");
		if(CMProps.Int.Prowesses.ARMOR_ADJ.is(prowessCode)||CMProps.Int.Prowesses.ARMOR_ADV.is(prowessCode))
		{
			final int normalizedArmor = (int)Math.round(Math.ceil(CMath.div(armor + adjustedArmor(CMLib.leveler().getLevelMOBArmor(mob)),5.0)));
			final int normalizedMax = CMProps.getListFileIndexedListSize(CMProps.ListFile.ARMOR_ADJS);
			final int medianValue = normalizedMax / 2;
			int adjIndex = normalizedArmor + medianValue;
			int extreme = 0;
			if(adjIndex < 0)
			{
				extreme = -adjIndex;
				adjIndex = 0;
			}
			if(adjIndex >= normalizedMax)
			{
				extreme = normalizedMax-adjIndex;
				adjIndex = normalizedMax-1;
			}
			if((extreme != 0)&&(CMProps.Int.Prowesses.ARMOR_ADV.is(prowessCode)))
				str.append(this.getExtremeValue(extreme));
			if(CMProps.Int.Prowesses.ARMOR_ADJ.is(prowessCode))
				str.append(CMProps.getListFileChoiceFromIndexedList(CMProps.ListFile.ARMOR_ADJS,adjIndex)).append(" ");
		}
		if(CMProps.Int.Prowesses.ARMOR_ABSOLUTE.is(prowessCode))
		{
			final int ARMOR_CEILING=CMProps.getListFileFirstInt(CMProps.ListFile.ARMOR_DESCS_CEILING);
			final int numArmorDescs = CMProps.getListFileIndexedListSize(CMProps.ListFile.ARMOR_DESCS);
			str.append((armor<0)?CMProps.getListFileChoiceFromIndexedList(CMProps.ListFile.ARMOR_DESCS,0):(
				   (armor>=ARMOR_CEILING)?
						   CMProps.getListFileChoiceFromIndexedList(CMProps.ListFile.ARMOR_DESCS,numArmorDescs-1)
						   +(CMStrings.repeatWithLimit('!',((armor-ARMOR_CEILING)/100),10))
						:
						   (CMProps.getListFileChoiceFromIndexedList(CMProps.ListFile.ARMOR_DESCS,(int)Math.round(Math.floor(CMath.mul(CMath.div(armor,ARMOR_CEILING),numArmorDescs)))))));
			str.append(" ");
		}
		if(CMProps.Int.Prowesses.ARMOR_NUMBER.is(prowessCode))
			str.append("^.("+armor+")");
		return str.toString().trim()+"^.";
	}

	@Override
	public String fightingProwessStr(final MOB mob)
	{
		final int prowessCode = CMProps.getIntVar(CMProps.Int.COMBATPROWESS);
		if(CMProps.Int.Prowesses.NONE.is(prowessCode))
			return "";
		final int attackProwess = adjustedAttackBonus(mob,null) - ATTACK_ADJUSTMENT;
		final StringBuilder str=new StringBuilder("");
		if(CMProps.Int.Prowesses.COMBAT_ADJ.is(prowessCode)||CMProps.Int.Prowesses.COMBAT_ADV.is(prowessCode))
		{
			final int normalizedAttack = (int)Math.round(Math.ceil(CMath.div(attackProwess - (adjustedAttackBonus(CMLib.leveler().getLevelAttack(mob))- ATTACK_ADJUSTMENT),12.0)));
			final int normalizedMax = CMProps.getListFileIndexedListSize(CMProps.ListFile.COMBAT_ADJS);
			final int medianValue = normalizedMax / 2;
			int adjIndex = normalizedAttack + medianValue;
			int extreme = 0;
			if(adjIndex < 0)
			{
				extreme = -adjIndex;
				adjIndex = 0;
			}
			if(adjIndex >= normalizedMax)
			{
				extreme = normalizedMax-adjIndex;
				adjIndex = normalizedMax-1;
			}
			if((extreme != 0)&&(CMProps.Int.Prowesses.COMBAT_ADV.is(prowessCode)))
				str.append(this.getExtremeValue(extreme));
			if(CMProps.Int.Prowesses.COMBAT_ADJ.is(prowessCode))
				str.append(CMProps.getListFileChoiceFromIndexedList(CMProps.ListFile.COMBAT_ADJS,adjIndex)).append(" ");
		}
		if(CMProps.Int.Prowesses.COMBAT_ABSOLUTE.is(prowessCode))
		{
			final int PROWESS_CEILING=CMProps.getListFileFirstInt(CMProps.ListFile.PROWESS_DESCS_CEILING);
			final int numProwessDescs = CMProps.getListFileIndexedListSize(CMProps.ListFile.PROWESS_DESCS);
			str.append((attackProwess<0)?CMProps.getListFileChoiceFromIndexedList(CMProps.ListFile.PROWESS_DESCS,0):(
				   (attackProwess>=PROWESS_CEILING)
											 ?
									 CMProps.getListFileChoiceFromIndexedList(CMProps.ListFile.PROWESS_DESCS,numProwessDescs-1)
									 +(CMStrings.repeatWithLimit('!',((attackProwess-PROWESS_CEILING)/100),10))
											 :
									 (CMProps.getListFileChoiceFromIndexedList(CMProps.ListFile.PROWESS_DESCS,(int)Math.round(Math.floor(CMath.mul(CMath.div(attackProwess,PROWESS_CEILING),numProwessDescs)))))));
		}
		if(CMProps.Int.Prowesses.COMBAT_NOUN.is(prowessCode))
		{
			final int normalizedMax = CMProps.getListFileIndexedListSize(CMProps.ListFile.COMBAT_NOUNS);
			final double divisor = CMath.div(CMProps.get(mob.session()).getInt(CMProps.Int.LASTPLAYERLEVEL),normalizedMax);
			int nounIndex = (int)Math.round(CMath.div(mob.phyStats().level()-1, divisor));
			if(nounIndex >= normalizedMax)
				nounIndex = normalizedMax-1;
			if(nounIndex<0)
				nounIndex=0;
			str.append(CMProps.getListFileChoiceFromIndexedList(CMProps.ListFile.COMBAT_NOUNS,nounIndex)).append(" ");
		}
		if(CMProps.Int.Prowesses.COMBAT_NUMBER.is(prowessCode))
			str.append("^.("+attackProwess+")");
		return str.toString().trim()+"^.";
	}

	@Override
	public String damageProwessStr(final MOB mob)
	{
		final int prowessCode = CMProps.getIntVar(CMProps.Int.COMBATPROWESS);
		if(CMProps.Int.Prowesses.NONE.is(prowessCode))
			return "";
		final int damageProwess = this.adjustedDamage(mob, (Weapon)mob.fetchWieldedItem(), null, 0, false,true);
		final StringBuilder str=new StringBuilder("");
		if(CMProps.Int.Prowesses.DAMAGE_ADJ.is(prowessCode)||CMProps.Int.Prowesses.DAMAGE_ADV.is(prowessCode))
		{
			final int normalizedDamage = (int)Math.round(Math.ceil(CMath.div(damageProwess - (adjustedDamage(CMLib.leveler().getLevelMOBDamage(mob),mob.phyStats().level(),true)),3.0)));
			final int normalizedMax = CMProps.getListFileIndexedListSize(CMProps.ListFile.DAMAGE_ADJS);
			final int medianValue = normalizedMax / 2;
			int adjIndex = normalizedDamage + medianValue;
			int extreme = 0;
			if(adjIndex < 0)
			{
				extreme = -adjIndex;
				adjIndex = 0;
			}
			if(adjIndex >= normalizedMax)
			{
				extreme = normalizedMax-adjIndex;
				adjIndex = normalizedMax-1;
			}
			if((extreme != 0)&&(CMProps.Int.Prowesses.DAMAGE_ADV.is(prowessCode)))
				str.append(this.getExtremeValue(extreme));
			if(CMProps.Int.Prowesses.DAMAGE_ADJ.is(prowessCode))
				str.append(CMProps.getListFileChoiceFromIndexedList(CMProps.ListFile.DAMAGE_ADJS,adjIndex)).append(" ");
		}
		if(CMProps.Int.Prowesses.DAMAGE_ABSOLUTE.is(prowessCode))
		{
			final int DAMAGE_CEILING=CMProps.getListFileFirstInt(CMProps.ListFile.DAMAGE_DESCS_CEILING);
			final int numProwessDescs = CMProps.getListFileIndexedListSize(CMProps.ListFile.DAMAGE_DESCS);
			str.append((damageProwess<0)?CMProps.getListFileChoiceFromIndexedList(CMProps.ListFile.DAMAGE_DESCS,0):(
				   (damageProwess>=DAMAGE_CEILING)
											 ?
									 CMProps.getListFileChoiceFromIndexedList(CMProps.ListFile.DAMAGE_DESCS,numProwessDescs-1)
									 +(CMStrings.repeatWithLimit('!',((damageProwess-DAMAGE_CEILING)/100),10))
											 :
									 (CMProps.getListFileChoiceFromIndexedList(CMProps.ListFile.DAMAGE_DESCS,(int)Math.round(Math.floor(CMath.mul(CMath.div(damageProwess,DAMAGE_CEILING),numProwessDescs)))))));
		}
		if(CMProps.Int.Prowesses.DAMAGE_NUMBER.is(prowessCode))
			str.append("^.("+damageProwess+")");
		return str.toString().trim()+"^.";
	}

	protected int getWeaponAttackIndex(final int weaponDamageType, final int weaponClassification)
	{
		switch(weaponClassification)
		{
		case Weapon.CLASS_RANGED:
			return (weaponDamageType == Weapon.TYPE_LASERING) ? 5 : 0;
		case Weapon.CLASS_THROWN:
			return (weaponDamageType == Weapon.TYPE_LASERING) ? 5 : 1;
		default:
			switch(weaponDamageType)
			{
			case Weapon.TYPE_SLASHING:
			case Weapon.TYPE_BASHING:
				return 2;
			case Weapon.TYPE_PIERCING:
				return 4;
			case Weapon.TYPE_SHOOT:
				return 0;
			case Weapon.TYPE_LASERING:
				return 5;
			default:
				return 3;
			}
		}
	}

	@Override
	public String standardMissString(final int weaponDamageType, final int weaponClassification, final String weaponName, final boolean useExtendedMissString)
	{
		final int listIndex = getWeaponAttackIndex(weaponDamageType, weaponClassification);
		if(!useExtendedMissString)
			return CMProps.getListFileChoiceFromIndexedList(CMProps.ListFile.MISS_DESCS,listIndex);
		return CMStrings.replaceAll(CMProps.getListFileChoiceFromIndexedList(CMProps.ListFile.WEAPON_MISS_DESCS,listIndex),"<TOOLNAME>",weaponName)+CMLib.protocol().msp("missed.wav",20);
	}

	@Override
	public String standardHitString(final int weaponDamageType, final int weaponClass, final int damageAmount, final String weaponName)
	{
		final int listIndex;
		if((weaponName==null)||(weaponName.length()==0))
			listIndex = getWeaponAttackIndex(weaponDamageType, Weapon.CLASS_NATURAL);
		else
			listIndex = getWeaponAttackIndex(weaponDamageType, weaponClass);
		final StringBuilder str=new StringBuilder(CMStrings.replaceAll(CMProps.getListFileChoiceFromIndexedList(CMProps.ListFile.WEAPON_HIT_DESCS,listIndex),"<TOOLNAME>",weaponName));
		switch(weaponClass)
		{
		case Weapon.CLASS_RANGED:
			return str.append(CMLib.protocol().msp("arrow.wav",20)).toString();
		case Weapon.CLASS_THROWN:
			return str.append(CMLib.protocol().msp("arrow.wav",20)).toString();
		default:
			return str.append(CMLib.protocol().msp("punch"+CMLib.dice().roll(1,7,0)+".wav",20)).toString();
		}
	}

	@Override
	public String standardMobCondition(final MOB viewer,final MOB mob)
	{
		int pct=(int)Math.round(Math.floor((CMath.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()))*10));
		if(pct<0)
			pct=0;
		final int numHealthDescs=CMProps.getListFileIndexedListSize(CMProps.ListFile.HEALTH_CHART);
		if(pct>=numHealthDescs)
			pct=numHealthDescs-1;
		return CMStrings.replaceAll(CMProps.getListFileChoiceFromIndexedList(CMProps.ListFile.HEALTH_CHART,pct),"<MOB>",mob.name(viewer));
	}

	@Override
	public void resistanceMsgs(MOB source, final MOB target, final CMMsg msg)
	{
		if((msg.value()>0)||(msg.sourceMinor()==CMMsg.TYP_TEACH))
			return;

		if(target.amDead())
			return;

		String tool=null;
		String endPart=" from <T-NAME>.";
		if(source==target)
		{
			source=null;
			endPart=".";
		}
		if(msg.tool() instanceof Trap)
			endPart=".";
		else
		if(msg.tool() instanceof Ability)
			tool=((Ability)msg.tool()).name();

		String tackOn=null;
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_MIND:
			if(tool == null)
				tackOn = L("<S-NAME> shake(s) off the mental attack" + endPart);
			else
				tackOn = L("<S-NAME> shake(s) off the @x1" + endPart,tool);
			break;
		case CMMsg.TYP_GAS:
			if(tool == null)
				tackOn = L("<S-NAME> resist(s) the noxious fumes" + endPart);
			else
				tackOn = L("<S-NAME> resist(s) the @x1" + endPart,tool);
			break;
		case CMMsg.TYP_COLD:
			if(tool == null)
				tackOn = L("<S-NAME> shake(s) off the cold blast" + endPart);
			else
				tackOn = L("<S-NAME> shake(s) off the @x1" + endPart,tool);
			break;
		case CMMsg.TYP_ELECTRIC:
			if(tool == null)
				tackOn = L("<S-NAME> shake(s) off the electrical attack" + endPart);
			else
				tackOn = L("<S-NAME> shake(s) off the @x1" + endPart,tool);
			break;
		case CMMsg.TYP_FIRE:
			if(tool == null)
				tackOn = L("<S-NAME> resist(s) the blast of heat" + endPart);
			else
				tackOn = L("<S-NAME> resist(s) the @x1" + endPart,tool);
			break;
		case CMMsg.TYP_WATER:
			if(tool == null)
				tackOn = L("<S-NAME> dodge(s) the wet blast" + endPart);
			else
				tackOn = L("<S-NAME> dodge(s) the @x1" + endPart,tool);
			break;
		case CMMsg.TYP_UNDEAD:
			if(tool == null)
				tackOn = L("<S-NAME> shake(s) off the evil attack" + endPart);
			else
				tackOn = L("<S-NAME> shake(s) off the @x1" + endPart,tool);
			break;
		case CMMsg.TYP_POISON:
			if(tool == null)
				tackOn = L("<S-NAME> shake(s) off the poison" + endPart);
			else
				tackOn = L("<S-NAME> shake(s) off the @x1" + endPart,tool);
			break;
		case CMMsg.TYP_DISEASE:
			if(tool == null)
				tackOn = L("<S-NAME> resist(s) the disease.");
			else
				tackOn = L("<S-NAME> resist(s) the @x1.",tool);
			break;
		case CMMsg.TYP_JUSTICE:
			break;
		case CMMsg.TYP_CAST_SPELL:
			if(tool == null)
				tackOn = L("<S-NAME> resist(s) the magical attack" + endPart);
			else
				tackOn = L("<S-NAME> resist(s) the @x1" + endPart,tool);
			break;
		case CMMsg.TYP_PARALYZE:
			if(tool == null)
				tackOn = L("<S-NAME> resist(s) the paralysis" + endPart);
			else
				tackOn = L("<S-NAME> resist(s) the @x1" + endPart,tool);
			break;
		case CMMsg.TYP_SONIC:
			if(tool == null)
				tackOn = L("<S-NAME> shake(s) off the sonic" + endPart);
			else
				tackOn = L("<S-NAME> shake(s) off the @x1" + endPart,tool);
			break;
		case CMMsg.TYP_LASER:
			if(tool == null)
				tackOn = L("<S-NAME> dodge(s) the laser" + endPart);
			else
				tackOn = L("<S-NAME> dodge(s) the @x1" + endPart,tool);
			break;
		}
		if(tackOn!=null)
		{
			final String newStr=target+"/"+source+"/"+tool;
			if(!newStr.equals(lastStr)||((System.currentTimeMillis()-lastRes)>250))
				msg.addTrailerMsg(CMClass.getMsg(target,source,CMMsg.MSG_OK_ACTION,tackOn));
			lastStr=newStr;
			lastRes=System.currentTimeMillis();
		}
		msg.setValue(msg.value()+1);
	}

	@Override
	public boolean checkDamageSaves(final MOB mob, final CMMsg msg)
	{
		int chanceToFail = 0;
		if(msg.tool() instanceof Weapon)
		{
			int charStatCode = -1;
			switch(((Weapon)msg.tool()).weaponDamageType())
			{
			case Weapon.TYPE_BASHING:
				charStatCode=CharStats.STAT_SAVE_BLUNT;
				break;
			case Weapon.TYPE_PIERCING:
				charStatCode=CharStats.STAT_SAVE_PIERCE;
				break;
			case Weapon.TYPE_SLASHING:
				charStatCode=CharStats.STAT_SAVE_SLASH;
				break;
			default:
				return true;
			}
			chanceToFail = mob.charStats().getSave(charStatCode);
		}
		else
		if((msg.tool() instanceof Ability)
		&&(msg.sourceMinor()!=CMMsg.TYP_TEACH))
		{
			switch(((Ability)msg.tool()).classificationCode() & Ability.ALL_ACODES)
			{
			case Ability.ACODE_CHANT:
				chanceToFail=mob.charStats().getSave(CharStats.STAT_SAVE_CHANTS)
							+mob.charStats().getSave(CharStats.STAT_SAVE_MAGIC);
				break;
			case Ability.ACODE_PRAYER:
				chanceToFail=mob.charStats().getSave(CharStats.STAT_SAVE_PRAYERS)
							+mob.charStats().getSave(CharStats.STAT_SAVE_MAGIC);
				break;
			case Ability.ACODE_SPELL:
				chanceToFail=mob.charStats().getSave(CharStats.STAT_SAVE_SPELLS)
							+mob.charStats().getSave(CharStats.STAT_SAVE_MAGIC);
				break;
			case Ability.ACODE_SONG:
				chanceToFail=mob.charStats().getSave(CharStats.STAT_SAVE_SONGS)
							+mob.charStats().getSave(CharStats.STAT_SAVE_MAGIC);
				break;
			}
			final int charStatCode = CharStats.CODES.RVSCMMSGMAP(msg.sourceMinor());
			if(charStatCode >= 0)
				chanceToFail += mob.charStats().getSave(charStatCode);
		}
		if ((chanceToFail != 0) && (chanceToFail > (Integer.MIN_VALUE/2)))
		{
			if (chanceToFail < -100)
				chanceToFail = -100;
			else
			if (chanceToFail > 100)
				chanceToFail = 100;
			if (CMLib.dice().rollPercentage() < ((chanceToFail < 0) ? (-chanceToFail) : chanceToFail))
				msg.setValue((int)Math.round(CMath.mul(msg.value(),CMath.div(100-chanceToFail,100))));
		}
		return true;
	}

	@Override
	public void fixDependentRanges(final MOB mob)
	{
		final Rideable riddenM = mob.riding();
		if((riddenM instanceof MOB)
		&&(((MOB)riddenM).getVictim()==mob.getVictim()))
		{
			if(((MOB)riddenM).rangeToTarget() != mob.rangeToTarget())
			{
				((MOB)riddenM).setRangeToTarget(mob.rangeToTarget());
				((MOB)riddenM).recoverPhyStats();
			}
		}
		if((mob instanceof Rideable)
		&&(((Rideable)mob).numRiders()>0))
		{
			for(final Enumeration<Rider> r = ((Rideable)mob).riders();r.hasMoreElements();)
			{
				final Rider R = r.nextElement();
				if((R instanceof MOB)
				&&(((MOB)R).getVictim()==mob.getVictim()))
				{
					if(((MOB)R).rangeToTarget() != mob.rangeToTarget())
					{
						((MOB)R).setRangeToTarget(mob.rangeToTarget());
						((MOB)R).recoverPhyStats();
					}
				}
			}
		}
	}

	@Override
	public boolean checkSavingThrows(final MOB mob, final CMMsg msg)
	{
		if ((msg.targetMinor() != CMMsg.TYP_WEAPONATTACK) && (msg.value() <= 0))
		{
			int charStatCode = -1;
			int chanceToFail = 0;
			if(msg.tool() instanceof Ability)
			{
				switch(((Ability)msg.tool()).classificationCode() & Ability.ALL_ACODES)
				{
				case Ability.ACODE_CHANT:
					charStatCode=CharStats.STAT_SAVE_CHANTS;
					break;
				case Ability.ACODE_PRAYER:
					charStatCode=CharStats.STAT_SAVE_PRAYERS;
					break;
				case Ability.ACODE_SPELL:
					charStatCode=CharStats.STAT_SAVE_SPELLS;
					break;
				case Ability.ACODE_SONG:
					charStatCode=CharStats.STAT_SAVE_SONGS;
					break;
				}
				if((charStatCode > 0)
				&&(msg.targetMinor()==CMMsg.TYP_CAST_SPELL))
					chanceToFail = mob.charStats().getSave(CharStats.STAT_SAVE_MAGIC);
			}
			if(charStatCode<0)
				charStatCode = CharStats.CODES.RVSCMMSGMAP(msg.targetMinor());
			if(charStatCode >= 0)
			{
				chanceToFail += mob.charStats().getSave(charStatCode);
				if (chanceToFail > (Integer.MIN_VALUE/2))
				{
					final int diff = (mob.phyStats().level() - msg.source().phyStats().level());
					final int diffSign = diff < 0 ? -1 : 1;
					chanceToFail += (diffSign * (diff * diff));
					if (chanceToFail < 5)
						chanceToFail = 5;
					else
					if (chanceToFail > 95)
						chanceToFail = 95;

					if (CMLib.dice().rollPercentage() < chanceToFail)
						CMLib.combat().resistanceMsgs(msg.source(), mob, msg); // also applies the +1 to msg.value()
				}
			}
		}
		return true;
	}

	@Override
	public void handleBeingGravitied(final MOB mob, final CMMsg msg)
	{
		if(msg.value() >= SpaceObject.ACCELERATION_UNCONSCIOUSNESS)
		{
			final Room R=mob.location();
			if(R==null)
				return;
			if(msg.value() >= SpaceObject.ACCELERATION_INSTANTDEATH)
			{
				R.show(mob,null,null,CMMsg.MSG_OK_ACTION, L("<S-NAME> is crushed to jelly by acceleration!"));
				postDeath(null, mob, msg);
			}
			else
			if(msg.value() >= SpaceObject.ACCELERATION_UNCONSCIOUSNESS)
			{
				Ability A=mob.fetchEffect("Fighter_Whomp");
				if(A==null)
					A=CMClass.getAbility("Fighter_Whomp");
				A.startTickDown(msg.source(), mob, 30);
				mob.recoverPhyStats();
				if(CMLib.flags().isSleeping(mob))
					R.show(mob,null,null,CMMsg.MSG_OK_ACTION, L("<S-NAME> fall(s) unconscious from extreme acceleration."));
			}
			else
			if(msg.value() >= SpaceObject.ACCELERATION_UNCONSCIOUSNESS)
			{
				Ability A=mob.fetchEffect("Fighter_Whomp");
				if(A==null)
				{
					A=CMClass.getAbility("Fighter_Whomp");
					A.startTickDown(msg.source(), mob, 3);
					mob.recoverPhyStats();
					if(CMLib.flags().isSleeping(mob))
						R.show(mob,null,null,CMMsg.MSG_OK_ACTION, L("<S-NAME> pass(es) out from high acceleration."));
				}
			}
		}
	}

	@Override
	public void handleBeingHealed(final CMMsg msg)
	{
		if(!(msg.target() instanceof MOB))
			return;
		final MOB target=(MOB)msg.target();
		final int amt=msg.value();
		if(amt>0)
			target.curState().adjHitPoints(amt,target.maxState());
	}

	protected boolean bleedableWeapon(final Environmental E)
	{
		if(E==null)
			return false;
		if(E instanceof Weapon)
		{
			return true;
		}
		else
		if(E instanceof Ability)
		{
			final int code=((Ability)E).classificationCode()&Ability.ALL_ACODES;
			switch(code)
			{
			case Ability.ACODE_DISEASE:
			case Ability.ACODE_POISON:
				return false;
			}
			return true;
		}
		else
			return true;
	}

	@Override
	public void handleBeingDamaged(final CMMsg msg)
	{
		if(!(msg.target() instanceof MOB))
			return;
		final MOB attacker=msg.source();
		final MOB target=(MOB)msg.target();
		if(target.amDead()) // already dead, don't take more damage.
			return;
		final int dmg=msg.value();
		if(Log.combatChannelOn())
		{
			final Item DI=target.fetchWieldedItem();
			final Item KI=attacker.fetchWieldedItem();
			final String tool=(msg.tool()==null)?"null":msg.tool().name();
			final String type=(msg.sourceMinor()==CMMsg.NO_EFFECT)?"??":CMMsg.TYPE_DESCS[msg.sourceMinor()];
			Log.combatOut("DAMG",attacker.Name()+":"+attacker.phyStats().getCombatStats()+":"+attacker.curState().getCombatStats()+":"+((KI==null)?"null":KI.name())+":"+target.Name()+":"+target.phyStats().getCombatStats()+":"+target.curState().getCombatStats()+":"+((DI==null)?"null":DI.name())+":"+tool+":"+type+":"+dmg);
		}
		//re-consider this synchro-block -- looks heady and dangerous, but this is the only spot, so ok for now
		synchronized(target.curState())
		{
			if((dmg>0)
			&&(target.curState().getHitPoints()>0))
			{
				if((attacker!=null)
				&&(attacker.playerStats()!=null)
				&&(attacker!=target))
					attacker.playerStats().bumpLevelCombatStat(PlayerCombatStat.DAMAGE_DONE, attacker.basePhyStats().level(), dmg);
				if(target.playerStats()!=null)
					target.playerStats().bumpLevelCombatStat(PlayerCombatStat.DAMAGE_TAKEN, target.basePhyStats().level(), dmg);
				if((!target.curState().adjHitPoints(-dmg,target.maxState()))
				&&(target.curState().getHitPoints()<1)
				&&(target.location()!=null))
					postDeath(attacker,target,msg);
				else
				{
					if((Math.round(CMath.div(dmg,target.maxState().getHitPoints())*100.0)>=CMProps.getIntVar(CMProps.Int.INJBLEEDPCTHP))
					&&bleedableWeapon(msg.tool())
					&&(target.phyStats().level()>=CMProps.getIntVar(CMProps.Int.INJBLEEDMINLEVEL)))
					{
						final Ability A2=CMClass.getAbility("Bleeding");
						if(A2!=null)
							A2.invoke((target),(target),true,0);
					}
					if((target.curState().getHitPoints()<target.getWimpHitPoint())
					&&(target.getWimpHitPoint()>0)
					&&(target.isInCombat()))
						postPanic(target,msg);
					else
					if((CMProps.getIntVar(CMProps.Int.INJPCTHP)>=(int)Math.round(CMath.div(target.curState().getHitPoints(),target.maxState().getHitPoints())*100.0))
					&&(!CMLib.flags().isGolem(target))
					&&(target.fetchEffect("Injury")==null))
					{
						Ability A=CMClass.getAbility("Injury");
						if(A!=null)
						{
							A.startTickDown(target,target,Ability.TICKS_ALMOST_FOREVER);
							A=target.fetchEffect(A.ID());
							if(A!=null)
								A.okMessage(target,msg);
						}
					}
				}
			}
		}
	}

	@Override
	public void handleDeath(final CMMsg msg)
	{
		final MOB deadmob=msg.source();
		if(!deadmob.amDead())
		{
			if((!deadmob.isMonster())&&(deadmob.soulMate()==null))
			{
				if(deadmob.playerStats()!=null)
					deadmob.playerStats().bumpLevelCombatStat(PlayerCombatStat.DEATHS_TAKEN, deadmob.basePhyStats().level(), 1);
				CMLib.coffeeTables().bump(deadmob,CoffeeTableRow.STAT_DEATHS);
				final PlayerStats playerStats=deadmob.playerStats();
				if(playerStats!=null)
					playerStats.setHygiene(0);
				final List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.DETAILEDDEATHS, deadmob);
				final List<String> channels2=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.DEATHS, deadmob);
				if(!CMLib.flags().isCloaked(deadmob))
				{
					for(int i=0;i<channels.size();i++)
					{
						if((msg.tool() instanceof MOB))
							CMLib.commands().postChannel(channels.get(i),deadmob.clans(),L("@x1 was just killed in @x2 by @x3.",deadmob.Name(),CMLib.map().getExtendedRoomID(deadmob.location()),msg.tool().Name()),true);
						else
							CMLib.commands().postChannel(channels.get(i),deadmob.clans(),L("@x1 has just died at @x2",deadmob.Name(),CMLib.map().getExtendedRoomID(deadmob.location())),true);
					}
				}
				if(!CMLib.flags().isCloaked(deadmob))
				{
					for(int i=0;i<channels2.size();i++)
						if((msg.tool() instanceof MOB))
							CMLib.commands().postChannel(channels2.get(i),deadmob.clans(),L("@x1 was just killed.",deadmob.Name()),true);
				}
			}
			if(msg.tool() instanceof MOB)
			{
				final MOB killer=(MOB)msg.tool();
				doDeathPostProcessing(msg);
				int count=10;
				MOB killerM=killer;
				while((killerM != null)
				&&(--count>0))
				{
					if(killerM.playerStats()!=null)
						killerM.playerStats().bumpLevelCombatStat(PlayerCombatStat.DEATHS_DONE, killerM.basePhyStats().level(), 1);
					killerM=killerM.amFollowing();
				}
				justDie(killer,deadmob,msg.targetMessage());
			}
			else
				justDie(null,deadmob,msg.targetMessage());
			deadmob.tell(deadmob,msg.target(),msg.tool(),msg.sourceMessage());
			if(deadmob.riding()!=null)
				deadmob.riding().delRider(deadmob);
			if(CMLib.flags().isCataloged(deadmob))
				CMLib.catalog().bumpDeathPickup(deadmob);
		}
	}

	@Override
	public boolean handleDamageSpam(final MOB observerM, final Physical target, final int amount)
	{
		if((observerM!=null)
		&&(observerM.playerStats()!=null)
		&&(target!=null)
		&&(amount>0))
		{
			final Map<String,int[]> spam=observerM.playerStats().getCombatSpams();
			synchronized(spam)
			{
				final String targetName = CMLib.flags().canBeSeenBy(target, observerM)?target.Name():L("Someone");
				if(!spam.containsKey(targetName))
					spam.put(targetName,new int[]{0});
				spam.get(targetName)[0]+=amount;
			}
			return true;
		}
		return false;
	}

	@Override
	public void doDeathPostProcessing(final CMMsg msg)
	{
		final MOB deadmob=msg.source();
		if(msg.tool() instanceof MOB)
		{
			final MOB killer=(MOB)msg.tool();
			if(Log.killsChannelOn())
			{
				final Item KI=killer.fetchWieldedItem();
				final Item DI=deadmob.fetchWieldedItem();
				final String room=CMLib.map().getExtendedRoomID((killer.location()!=null)?killer.location():deadmob.location());
				Log.killsOut("KILL",room+":"+killer.Name()+":"+killer.phyStats().getCombatStats()+":"+killer.curState().getCombatStats()+":"+((KI==null)?"null":KI.name())+":"+deadmob.Name()+":"+deadmob.phyStats().getCombatStats()+":"+deadmob.curState().getCombatStats()+":"+((DI==null)?"null":DI.name()));
			}
			if(Log.combatChannelOn())
			{
				final Item DI=deadmob.fetchWieldedItem();
				final Item KI=killer.fetchWieldedItem();
				Log.combatOut("KILL",killer.Name()+":"+killer.phyStats().getCombatStats()+":"+killer.curState().getCombatStats()+":"+((KI==null)?"null":KI.name())+":"+deadmob.Name()+":"+deadmob.phyStats().getCombatStats()+":"+deadmob.curState().getCombatStats()+":"+((DI==null)?"null":DI.name()));
			}
			if(killer.isPlayer())
				CMLib.achievements().possiblyBumpAchievement(killer, AchievementLibrary.Event.KILLS, 1, deadmob);
			final Set<MOB> grpS=killer.getGroupMembers(new HashSet<MOB>());
			for(final MOB M : grpS)
			{
				if(M.isPlayer())
					CMLib.achievements().possiblyBumpAchievement(M, AchievementLibrary.Event.GROUPKILLS, 1, deadmob, Integer.valueOf(grpS.size()));
			}
			if((deadmob!=null)
			&&(killer!=null)
			&&(killer!=deadmob)
			&&(deadmob.soulMate()==null)
			&&(!killer.isMonster()))
			{
				if((!deadmob.isMonster())
				&&((killer.isPlayer())
				&&(deadmob.isPlayer()))
				&&(!CMLib.players().isSameAccount(killer, deadmob)))
				{
					CMLib.coffeeTables().bump(deadmob,CoffeeTableRow.STAT_PKDEATHS);
					if(killer.playerStats()!=null)
						CMLib.get(killer.session())._players().bumpPrideStat(killer,PrideStat.PVPKILLS, 1);
				}
				if((killer.session()!=null)
				&&(deadmob.clans().iterator().hasNext())
				&&((deadmob.session()==null)
					||(!deadmob.session().getAddress().equalsIgnoreCase(killer.session().getAddress())))
				&&((deadmob.playerStats()==null)
					||(killer.playerStats()==null)
					||(deadmob.playerStats().getAccount()==null)
					||(killer.playerStats().getAccount()==null)
					||(deadmob.playerStats().getAccount()!=killer.playerStats().getAccount())))
				{
					final List<Pair<Clan,Integer>> list = CMLib.clans().findRivalrousClans(killer, deadmob);
					if(list.size()>0)
					{
						for(final Pair<Clan,Integer> c : list)
						{
							c.first.recordClanKill(killer,deadmob);
							if(killer.getClanRole(c.first.clanID()) != null)
								CMLib.achievements().possiblyBumpAchievement(killer, AchievementLibrary.Event.CLANKILLS, 1, c.first, deadmob);
						}
					}
				}
			}
		}
	}

	protected void pickNextVictim(final MOB observer, final MOB deadmob, final Set<MOB> deadGroupH)
	{
		final Room R=observer.location();
		if(R!=null)
		{
			MOB newTargetM=null;
			final Set<MOB> myGroupH=observer.getGroupMembers(new HashSet<MOB>());
			for(int r=0;r<R.numInhabitants();r++)
			{
				final MOB M=R.fetchInhabitant(r);
				if((M!=observer)
				&&(M!=deadmob)
				&&(M!=null)
				&&(M.getVictim()==observer)
				&&(!M.amDead())
				&&(CMLib.flags().isInTheGame(M,true))
				&&(observer.mayIFight(M)))
				{
					newTargetM=M;
					break;
				}
			}
			if(newTargetM==null)
			for(int r=0;r<R.numInhabitants();r++)
			{
				final MOB M=R.fetchInhabitant(r);
				if(M==null)
					continue;
				final MOB vic=M.getVictim();
				if((M!=observer)
				&&(M!=deadmob)
				&&(deadGroupH.contains(M)
					||((vic!=null)&&(myGroupH.contains(vic))))
				&&(!M.amDead())
				&&(CMLib.flags().isInTheGame(M,true))
				&&(observer.mayIFight(M)))
				{
					newTargetM=M;
					break;
				}
			}
			observer.setVictim(null); //BZ: cause of range issues.
			if(newTargetM!=null)
			{
				observer.setVictim(newTargetM);
				if(newTargetM.getVictim()==observer)
					observer.setRangeToTarget(newTargetM.rangeToTarget());
			}
		}
	}

	@Override
	public void handleObserveDeath(final MOB observer, final MOB fighting, final CMMsg msg)
	{
		// no longer does a damn thing
	}

	@Override
	public void handleDamageSpamSummary(final MOB mob)
	{
		if((mob.isAttributeSet(MOB.Attrib.NOBATTLESPAM))
		&&(mob.playerStats()!=null))
		{
			int numEnemies=0;
			final Room R=mob.location();
			for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
			{
				final MOB M=m.nextElement();
				if((M!=null)&&(M!=mob)&&(M.getVictim()==mob))
					numEnemies++;
			}
			final Map<String,int[]> combatSpam = mob.playerStats().getCombatSpams();
			final StringBuilder msg=new StringBuilder("");
			synchronized(combatSpam)
			{
				if(numEnemies>1)
					msg.append("^<FIGHT^>"+L("Fighting @x1 enemies.  ",""+numEnemies)+"^</FIGHT^>");
				if(combatSpam.size()==0)
				{
					if(mob.isInCombat()||(numEnemies>0))
						msg.append("^<FIGHT^>"+L("No new combat damage reported.")+"^</FIGHT^>");
				}
				else
				{
					msg.append("^<FIGHT^>"+L("New combat damage: "));
					for(final String str : combatSpam.keySet())
					{
						msg.append(str).append(" ").append(combatSpam.get(str)[0]).append(" points. ");
					}
					msg.append("^</FIGHT^>");
				}
				mob.tell(msg.toString());
				combatSpam.clear();
			}
		}
	}

	@Override
	public void handleBeingAssaulted(final CMMsg msg)
	{
		if(!(msg.target() instanceof MOB))
			return;
		MOB attacker=msg.source();
		final MOB target=(MOB)msg.target();

		final Room R = target.location();
		if((!target.isInCombat())
		&&(R!=null)
		&&(R.isInhabitant(attacker))
		&&((!CMath.bset(msg.sourceMajor(),CMMsg.MASK_ALWAYS))
			||(!(msg.tool() instanceof DiseaseAffect))))
		{
			if((target.getVictim()==null)
			&& (R!=null)
			&&((attacker.amFollowing()!=null)
				||(attacker.numFollowers()>1)))
			{
				MOB frontM = null;
				for(final List<MOB> formation : getFormation(attacker))
				{
					if((formation!=null)
					&&(formation.size()>0))
					{
						if(formation.contains(attacker))
							break; // this means nothing would have changed.
						boolean brk=false;
						for(final MOB M : formation)
						{
							if((M.location()==R)
							&&(!M.isAttributeSet(MOB.Attrib.AUTOASSIST)) // ! means they DO autoassist
							&&(!CMath.bset(M.phyStats().disposition(), PhyStats.IS_UNHELPFUL)))
							{
								frontM=M;
								brk=true;
							}
						}
						if(brk)
							break;
					}
				}
				if((frontM!=null)
				&&(frontM!=attacker))
				{
					if(!frontM.isInCombat())
					{
						final CMMsg msgf = CMClass.getMsg(target, frontM, null,
								CMMsg.MASK_MALICIOUS|CMMsg.MSG_OK_VISUAL,null);
						if(R.okMessage(target, msgf))
							R.send(target, msgf);
					}
					attacker=frontM;
				}
			}
			establishRange(target,attacker,msg.tool());
			target.setVictim(attacker);
		}
		if(target.isInCombat())
		{
			if((attacker.session()!=null)
			&&(!attacker.isPossessing()))
			{
				if(!target.isMonster())
					attacker.session().setLastPKFight();
				else
					attacker.session().setLastNPCFight();
			}
			if(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
			{
				Item weapon=attacker.getNaturalWeapon();
				if((msg.tool() instanceof Item))
					weapon=(Item)msg.tool();
				if(weapon!=null)
				{
					final boolean isHit=rollToHit(attacker,target);
					postWeaponAttackResult(attacker,target,weapon,0, isHit);
					if(isHit)
						msg.setValue(1);
				}
				if((target.soulMate()==null)
				&&(target.playerStats()!=null)
				&&(target.location()!=null)
				&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.HYGIENE)))
					target.playerStats().adjHygiene(PlayerStats.HYGIENE_FIGHTDIRTY);

				if((attacker.isMonster())&&(!attacker.isInCombat()))
					attacker.setVictim(target);
			}
			else
			if(msg.tool() instanceof Item)
				postWeaponAttackResult(attacker,target,(Item)msg.tool(),0, true);
		}
		if(CMLib.flags().isSitting(target)||CMLib.flags().isSleeping(target))
			CMLib.commands().postStand(target,true, false);
	}

	@Override
	public void makeFollowersFight(final MOB observerM, final MOB defenderM, final MOB attackerM)
	{
		if((attackerM==null)||(defenderM==null)||observerM==null)
			return;
		if(attackerM==defenderM)
			return;
		if((defenderM==observerM)||(attackerM==observerM))
			return;
		if((defenderM.location()!=observerM.location())
		||(defenderM.location()!=attackerM.location()))
			return;
		if((observerM.isAttributeSet(MOB.Attrib.AUTOASSIST))
		||(CMath.bset(observerM.phyStats().disposition(), PhyStats.IS_UNHELPFUL)))
			return;
		if(observerM.isInCombat())
			return;
		final MOB observerFollows=observerM.amFollowing();
		final MOB targetFollows=defenderM.amFollowing();
		final MOB sourceFollows=attackerM.amFollowing();

		if((observerFollows==defenderM)
		||(targetFollows==observerM)
		||((targetFollows!=null)&&(targetFollows==observerFollows)))
		{
			establishRange(observerM,attackerM,observerM.fetchWieldedItem());
			observerM.setVictim(attackerM);
		}
		else
		if((observerFollows==attackerM)
		||(sourceFollows==observerM)
		||((sourceFollows!=null)&&(sourceFollows==observerFollows)))
		{
			establishRange(observerM,defenderM,observerM.fetchWieldedItem());
			observerM.setVictim(defenderM);
		}
	}

	public List<MOB> getAllInProximity(final MOB to, final int distance)
	{
		final Room R=to.location();
		final Vector<MOB> V=new Vector<MOB>();
		V.addElement(to);
		if(R==null)
			return V;
		final List<MOB> everyV=new ArrayList<MOB>();
		for(int i=0;i<R.numInhabitants();i++)
			everyV.add(R.fetchInhabitant(i));
		if(!everyV.contains(to))
			everyV.add(to);
		final int[][] map=new int[everyV.size()][everyV.size()];
		for(int x=0;x<map.length;x++)
		{
			for(int y=0;y<map.length;y++)
				map[x][y]=-1;
		}

		return V;
	}

	private int maxRangeWith(final MOB mob, final Environmental tool)
	{
		int max = 0;
		if (tool != null)
			max = tool.maxRange();
		if (mob.maxRange() < max)
			max = mob.maxRange();
		return max;
	}

	private int minRangeWith(final MOB mob, final Environmental tool)
	{
		if (tool != null)
			return tool.minRange();
		return mob.minRange();
	}

	@Override
	public int calculateRangeToTarget(final MOB source, final MOB target, final Environmental tool)
	{
		// establish and enforce range for the target, who is being assaulted

		// if your victim already has a range, you inherit that
		if((target.getVictim()==source)
		&&(target.rangeToTarget()>=0))
			return target.rangeToTarget();

		// if you are riding something, you inherit the range of the horse, or other riders
		// already in combat.
		if(source.riding()!=null)
		{
			if((target==source.riding())||(source.riding().amRiding(target)))
				return 0;
			else
			if((source.riding() instanceof MOB)
			&&(((MOB)source.riding()).isInCombat())
			&&(((MOB)source.riding()).getVictim()==target)
			&&(((MOB)source.riding()).rangeToTarget()>=0)
			&&(((MOB)source.riding()).rangeToTarget()<source.rangeToTarget()))
				return ((MOB)source.riding()).rangeToTarget();
			else
			for(int r=0;r<source.riding().numRiders();r++)
			{
				final Rider rider=source.riding().fetchRider(r);
				if(!(rider instanceof MOB))
					continue;
				final MOB otherMOB=(MOB)rider;
				if((otherMOB!=source)
				&&(otherMOB.isInCombat())
				&&(otherMOB.getVictim()==target)
				&&(otherMOB.rangeToTarget()>=0)
				&&((otherMOB.rangeToTarget()<source.rangeToTarget())||(source.rangeToTarget()<0)))
					return otherMOB.rangeToTarget();
			}
		}

		// if the source is a rideable, and you are being ridden,
		// then you inherit the range of a rider, if any
		if((source instanceof Rideable)
		&&(((Rideable)source).numRiders()>0))
		{
			for(int r=0;r<((Rideable)source).numRiders();r++)
			{
				final Rider rider=((Rideable)source).fetchRider(r);
				if(!(rider instanceof MOB))
					continue;
				final MOB otherMOB=(MOB)rider;
				if((otherMOB.isInCombat())
				&&(otherMOB.getVictim()==target)
				&&(otherMOB.rangeToTarget()>=0)
				&&((otherMOB.rangeToTarget()<source.rangeToTarget())||(source.rangeToTarget()<0)))
					return otherMOB.rangeToTarget();
			}
		}

		final Room R = source.location();
		final int maxToolRange = maxRangeWith(source,tool);
		final MOB leader=(source.amFollowing()!=null)?source.amFollowing():null;
		if((leader!=null)
		&&(leader.location()==source.location()))
		{
			int newRange=leader.fetchFollowerOrder(source);
			int leaderRange=0;
			if(leader.amFollowing()!=null)
			{
				if(leader.isInCombat() && (leader.rangeToTarget()>=0))
					leaderRange=leader.rangeToTarget();
				else
					leaderRange=calculateRangeToTarget(leader, target, leader.fetchWieldedItem());
			}
			if(newRange<0)
			{
				// if you aren't in formation, then just follow your leader
				newRange=maxToolRange;
				if((leaderRange >= 0) && (leaderRange < newRange))
					newRange = leaderRange;
			}
			else
			{
				boolean found=false;
				MOB firstElligibleM=null;
				for(final List<MOB> form :  getFormation(leader))
				{
					if((form!=null)&&(form.size()>0))
					{
						for(final MOB M : form)
						{
							if(M.location()==R)
							{
								if(firstElligibleM==null)
									firstElligibleM=M;
								if((M.getVictim()==target)
								&&(M.rangeToTarget()>=0))
								{
									leaderRange+=M.rangeToTarget();
									found=true;
									break;
								}
							}
						}
					}
					if(found)
						break;
				}
				if((!found)&&(firstElligibleM!=null)&&(firstElligibleM!=source))
					leaderRange+=calculateRangeToTarget(firstElligibleM, target, firstElligibleM.fetchWieldedItem());
				newRange=newRange+leaderRange;
			}
			return (R!=null)?Math.min(newRange,R.maxRange()):newRange;
		}
		if(source.numFollowers()>0)
		{
			final int position = source.fetchFollowerOrder(source);
			if(position > 0)
				return (R!=null)?Math.min(position,R.maxRange()):position;
		}
		return (R!=null)?Math.min(maxToolRange,R.maxRange()):maxToolRange;
	}

	@Override
	public void establishRange(final MOB source, final MOB target, final Environmental tool)
	{
		if((source.rangeToTarget()<0)||(source.getVictim()!=target))
		{
			final int newRange = calculateRangeToTarget(source, target, tool);
			if(newRange != source.rangeToTarget())
			{
				source.setRangeToTarget(newRange);
				source.recoverPhyStats();
			}
		}
	}

	protected boolean subtickAttack(final MOB fighter, Item weapon, final int folrange)
	{
		if((weapon!=null)
		&&(weapon.amWearingAt(Wearable.IN_INVENTORY)))
			weapon=fighter.fetchWieldedItem();
		if((!fighter.isAttributeSet(MOB.Attrib.AUTOMELEE)))
			return postAttack(fighter,fighter.getVictim(),weapon);
		else
		{
			boolean inminrange=(fighter.rangeToTarget()>=minRangeWith(fighter, weapon));
			boolean inmaxrange=(fighter.rangeToTarget()<=maxRangeWith(fighter, weapon));
			if((folrange>=0)
			&&(fighter.rangeToTarget()>=0)
			&&(folrange!=fighter.rangeToTarget()))
			{
				if(fighter.rangeToTarget()<folrange)
					inminrange=false;
				else
				if(fighter.rangeToTarget()>folrange)
				{
					final MOB victim=fighter.getVictim();
					// these settings are ONLY to ensure that neither of the
					// next two conditions evaluate to true.
					inminrange=true;
					inmaxrange=false;
					// we advance
					final CMMsg msg=CMClass.getMsg(fighter,victim,CMMsg.MSG_ADVANCE,L("<S-NAME> advance(s) at <T-NAMESELF>."));
					if(fighter.location().okMessage(fighter,msg))
						fighter.location().send(fighter,msg);
				}
			}

			if((!inminrange)
			&&(fighter.curState().getMovement()>=25))
			{
				final CMMsg msg=CMClass.getMsg(fighter,fighter.getVictim(),CMMsg.MSG_RETREAT,L("<S-NAME> retreat(s) before <T-NAME>."));
				if(fighter.location().okMessage(fighter,msg))
					fighter.location().send(fighter,msg);
			}
			else
			if(inminrange
			&&inmaxrange
			&&((weapon!=null)||(fighter.rangeToTarget()==0)))
				return postAttack(fighter,fighter.getVictim(),weapon);
		}
		return false;
	}

	protected void subtickBeforeAttack(final MOB fighter, final CombatSystem combatSystem)
	{
		// combat que system eats up standard commands
		// before using any attacks
		while(((combatSystem==CombatLibrary.CombatSystem.QUEUE)||(combatSystem==CombatLibrary.CombatSystem.TURNBASED))
		&&(!fighter.amDead())
		&&(fighter.dequeCommand()))
			{
			}
	}

	protected void subtickAfterAttack(final MOB fighter)
	{
		// this code is for auto-retargeting of players
		// is mostly not handled by combatabilities in a smarter way
		final MOB target=fighter.getVictim();
		if((target!=null)
		&&(fighter.isMonster())
		&&(target.isMonster())
		&&(CMLib.dice().rollPercentage()==1)
		&&((fighter.amFollowing()==null)||(fighter.amFollowing().isMonster()))
		&&(!target.amDead())
		&&(fighter.location()!=null))
		{
			MOB M=null;
			final Room R=fighter.location();
			MOB nextVictimM=null;
			for(int m=0;m<R.numInhabitants();m++)
			{
				M=R.fetchInhabitant(m);
				if((M!=null)
				&&(!M.isMonster())
				&&(M.getVictim()==fighter)
				&&((nextVictimM==null)||(M.rangeToTarget()<nextVictimM.rangeToTarget())))
					nextVictimM=M;
			}
			if((nextVictimM!=null)&&(nextVictimM.isInCombat()))
				fighter.setVictim(nextVictimM);
		}
	}

	protected MUDFight getSessionMUDFight(final Session sess)
	{
		if(sess == null)
			return this;
		final CombatLibrary lib=CMLib.get(sess)._combat();
		if(lib instanceof MUDFight)
			return (MUDFight)lib;
		return this;
	}

	@Override
	public void dispenseExperience(final Set<MOB> killers, final Set<MOB> dividers, final MOB killed)
	{
		int totalLevels=0;
		int totalSquaredLevels=0;
		int highestLevel=0;

		for (final MOB mob : dividers)
		{
			final int lvl=mob.phyStats().level();
			totalSquaredLevels += (lvl*lvl);
			totalLevels += lvl;
			if(lvl>highestLevel)
				highestLevel=lvl;
		}

		final int killedLevel=(killed==null)?highestLevel:killed.phyStats().level();
		final double[] totalVars={
			dividers.size(),
			totalSquaredLevels,
			totalLevels,
			killers.size(),
			highestLevel,
			killedLevel
		};
		final double expAmount = CMath.parseMathExpression(this.totalCombatExperienceFormula, totalVars, 0.0);

		final double[] indiVars = {
			expAmount,
			0.0,
			totalSquaredLevels,
			0.0,
			totalLevels,
			killers.size(),
			highestLevel,
			killedLevel
		};
		for (final MOB mob : killers)
		{
			indiVars[1]=mob.phyStats().level()*mob.phyStats().level();
			indiVars[3]=mob.phyStats().level();
			final CompiledFormula indXPformula = getSessionMUDFight(mob.session()).individualCombatExpFormula;
			final int myAmount=(int)Math.round(CMath.parseMathExpression(indXPformula, indiVars, 0.0));
			final String playerFlag = (killed!=null)&&(killed.isPlayer())?"P":"";
			CMLib.get(mob.session())._leveler().postExperience(mob,"COMBAT:"+killedLevel+playerFlag,killed,"",myAmount, false);
		}
	}

	@Override
	public void tickCombat(final MOB fighter)
	{
		Item weapon=fighter.fetchWieldedItem();

		if((fighter.isAttributeSet(MOB.Attrib.AUTODRAW))&&(weapon==null))
		{
			CMLib.commands().postDraw(fighter,false,true);
			weapon=fighter.fetchWieldedItem();
		}

		final CombatSystem combatSystem=CombatSystem.values()[CMProps.getIntVar(CMProps.Int.COMBATSYSTEM) % CombatSystem.values().length];

		subtickBeforeAttack(fighter, combatSystem);

		final int folrange=(fighter.isAttributeSet(MOB.Attrib.AUTOMELEE)
						&&(fighter.amFollowing()!=null)
						&&(fighter.amFollowing().getVictim()==fighter.getVictim())
						&&(fighter.amFollowing().rangeToTarget()>=0)
						&&(fighter.amFollowing().fetchFollowerOrder(fighter)>=0))?
							fighter.amFollowing().fetchFollowerOrder(fighter)+fighter.amFollowing().rangeToTarget():-1;
		if(CMLib.flags().isAliveAwakeMobile(fighter,true))
		{
			if(((combatSystem!=CombatLibrary.CombatSystem.MANUAL)&&(combatSystem!=CombatLibrary.CombatSystem.TURNBASED))
			||(fighter.isMonster()))
			{
				final int saveAction=(combatSystem!=CombatLibrary.CombatSystem.DEFAULT)?0:1;
				int numAttacks=(int)Math.round(Math.floor(fighter.actions()))-saveAction;
				if((combatSystem==CombatLibrary.CombatSystem.DEFAULT)
				&&(numAttacks>(int)Math.round(Math.floor(fighter.phyStats().speed()+0.9))))
					numAttacks=(int)Math.round(Math.floor(fighter.phyStats().speed()+0.9));
				for(int s=0;s<numAttacks;s++)
				{
					if((!fighter.amDead())
					&&(fighter.curState().getHitPoints()>0)
					&&(fighter.isInCombat())
					&&(fighter.actions()>=1.0)
					&&((s==0)||(CMLib.flags().isStanding(fighter))))
					{
						fighter.setActions(fighter.actions()-1.0);
						subtickAttack(fighter,weapon,folrange);
					}
					else
						break;
				}
				if(CMLib.dice().rollPercentage()>(fighter.charStats().getStat(CharStats.STAT_CONSTITUTION)*4))
					fighter.curState().adjMovement(-1,fighter.maxState());
			}
		}
		subtickAfterAttack(fighter);
	}

	@Override
	public boolean isKnockedOutUponDeath(final MOB deadM, final MOB killerM)
	{
		String whatToDo=null;
		if(((deadM.isMonster())||(deadM.soulMate()!=null)))
			whatToDo=CMProps.getVar(CMProps.Str.MOBDEATH).toUpperCase();
		else
			whatToDo=CMProps.get(deadM.session()).getStr(CMProps.Str.PLAYERDEATH).toUpperCase();
		final List<String> whatsToDo=CMParms.parseCommas(whatToDo,true);
		final double[] fakeVarVals={1.0,1.0,1.0};
		for(int w=0;w<whatsToDo.size();w++)
		{
			whatToDo=whatsToDo.get(w);
			if(whatToDo.startsWith("OUT ")&&(CMath.isMathExpression(whatToDo.substring(4).trim(),fakeVarVals)))
				return true;
		}
		return false;
	}

	@Override
	public void expendEnergy(final MOB mob, final boolean expendMovement)
	{
		if(mob==null)
		{
			return;
		}
		final Room room=mob.location();
		if(room!=null)
		{
			final CharState curState=mob.curState();
			final CharState maxState=mob.maxState();
			if(expendMovement)
			{
				int move=-room.pointsPerMove();
				if(mob.phyStats().weight()>mob.maxCarry())
					move+=(int)Math.round(CMath.mul(move,10.0*CMath.div(mob.phyStats().weight()-mob.maxCarry(),mob.maxCarry())));
				curState.adjMovement(move,maxState);
			}
			if((!CMLib.flags().isSleeping(mob))
			&&(!CMSecurity.isAllowed(mob,room,CMSecurity.SecFlag.IMMORT)))
			{
				double factor=CMath.div(mob.baseWeight(),500.0);
				if(factor<0.001)
					factor=0.001;
				if((!CMSecurity.isDisabled(CMSecurity.DisFlag.THIRST))
				&&(mob.maxState().getThirst() < (Integer.MAX_VALUE/2)))
					curState.adjThirst(-CMath.mul(room.thirstPerRound(),factor),maxState.maxThirst(mob.baseWeight()));
				if((!CMSecurity.isDisabled(CMSecurity.DisFlag.HUNGER))
				&&(mob.maxState().getHunger() < (Integer.MAX_VALUE/2)))
					curState.adjHunger(-factor,maxState.maxHunger(mob.baseWeight()));
			}
			final boolean thirsty=(curState.getThirst()<=0);
			final boolean hungry=(curState.getHunger()<=0);
			if((hungry||thirsty)&&(!expendMovement))
			{
				final int ticksThirsty=curState.adjTicksThirsty(thirsty);
				final int ticksHungry=curState.adjTicksHungry(hungry);

				if((ticksThirsty>CharState.DEATH_THIRST_TICKS)
				||(ticksHungry>CharState.DEATH_HUNGER_TICKS))
				{
					if(thirsty)
						mob.tell(L("YOU ARE DYING OF THIRST!"));
					if(hungry)
						mob.tell(L("YOU ARE DYING OF HUNGER!"));
					postDeath(null,mob,null);
				}
				else
				if(ticksThirsty>CharState.DEATH_THIRST_TICKS-30)
					mob.tell(L("You are dehydrated, and near death.  DRINK SOMETHING!"));
				else
				if(ticksHungry>CharState.DEATH_HUNGER_TICKS-30)
					mob.tell(L("You are starved, and near death.  EAT SOMETHING!"));
				else
				{
					if(thirsty && (((ticksThirsty-1) % CharState.ANNOYANCE_DEFAULT_TICKS)==0))
					{
						if(ticksThirsty>((CharState.DEATH_THIRST_TICKS/2)+(CharState.DEATH_THIRST_TICKS/4)))
							mob.tell(L("You are dehydrated! Drink something!"));
						else
						if(ticksThirsty>(CharState.DEATH_THIRST_TICKS/2))
							mob.tell(L("You are parched! Drink something!"));
						else
							mob.tell(L("You are thirsty."));
					}
					if((hungry) && (((ticksHungry-1) % CharState.ANNOYANCE_DEFAULT_TICKS)==0))
					{
						if(ticksHungry>((CharState.DEATH_HUNGER_TICKS/2)+(CharState.DEATH_HUNGER_TICKS/4)))
							mob.tell(L("You are starved! Eat something!"));
						else
						if(ticksHungry>(CharState.DEATH_HUNGER_TICKS/2))
							mob.tell(L("You are famished! Eat something!"));
						else
							mob.tell(L("You are hungry."));
					}
				}
			}
		}
	}

	@Override
	public boolean handleCombatLossConsequences(final MOB deadM, final MOB killerM, final String[] consequences, int[] lostExperience, String message)
	{
		if((consequences==null)||(consequences.length==0))
			return false;
		if(lostExperience==null)
			lostExperience=new int[1];
		final int baseExperience=lostExperience[0];
		lostExperience[0]=0;
		int rejuv=deadM.phyStats().rejuv();
		if((rejuv==0)||(rejuv==PhyStats.NO_REJUV))
			rejuv=deadM.phyStats().level();
		if(((!deadM.isMonster())&&(deadM.soulMate()==null)))
			rejuv=1;
		final double[] varVals={
				deadM.basePhyStats().level()>deadM.phyStats().level()?deadM.basePhyStats().level():deadM.phyStats().level(),
				(killerM!=null)?killerM.phyStats().level():0,
				rejuv
		};
		for (final String command : consequences)
		{
			final String whatToDo=command.toUpperCase();
			if(whatToDo.startsWith("UNL")) // "UNLEVEL
			{
				final Vector<String> V=CMParms.parse(whatToDo);
				int times=1;
				if((V.size()>1)&&(CMath.s_int(V.lastElement())>1))
					times=CMath.s_int(V.lastElement());
				for(int t=0;t<times;t++)
					CMLib.leveler().unLevel(deadM, true);
			}
			else
			if(whatToDo.startsWith("RECALL"))
				deadM.killMeDead(false);
			else
			if(whatToDo.startsWith("ASTR")) // "ASTRAL
			{
				final Ability A=CMClass.getAbility("Prop_AstralSpirit");
				if((A!=null)&&(deadM.fetchAbility(A.ID())==null))
				{
					deadM.tell(L("^HYou are now a spirit.^N"));
					if(whatToDo.startsWith("ASTRAL_R"))
					{
						A.setMiscText("SELF-RES");
						deadM.tell(L("^HFind your corpse and use ENTER [body name] to re-enter your body.^N"));
					}
					else
						deadM.tell(L("^HFind your corpse have someone resurrect it.^N"));
					deadM.addAbility(A);
					A.autoInvocation(deadM, false);
				}
			}
			else
			if(whatToDo.startsWith("OUT ")&&(CMath.isMathExpression(whatToDo.substring(4).trim(),varVals)))
			{
				final Ability A=CMClass.getAbility("Skill_ArrestingSap");
				final int tickDown=CMath.s_parseIntExpression(whatToDo.substring(4).trim(),varVals);
				if((A!=null)&&(tickDown>0))
				{
					A.invoke(deadM,new XVector<String>(""+tickDown,"SAFELY"),deadM,true,0);
					deadM.resetToMaxState();
				}
			}
			else
			if(whatToDo.startsWith("PUR")) // "PURGE"
			{
				int maxLives = 1;
				final int x = whatToDo.indexOf(' ');
				if(x>0)
					maxLives = CMath.s_int(whatToDo.substring(x+1).trim());
				final MOB deadMOB=(!deadM.isPlayer())?CMLib.get(deadM.session())._players().getLoadPlayer(deadM.Name()):deadM;
				if((deadMOB!=null)&&(deadM.session()!=null))
				{
					if((deadMOB.playerStats()==null) // this happens after the bump, so...
					||(deadMOB.playerStats().deathCounter(0)>=maxLives))
					{
						CMLib.threads().executeRunnable(deadM.session().getGroupName(), new Runnable()
						{
							final MOB mob = deadMOB;
							@Override
							public void run()
							{
								final Session session=mob.session();
								if(session!=null)
									session.stopSession(true, true, true);
								// preserve their corpse, if it is in the game
								final PlayerStats pStat = mob.playerStats();
								if(pStat != null)
								{
									final List<Item> removeThese=new ArrayList<Item>(1);
									for(final Enumeration<Item>  o = pStat.getExtItems().items();o.hasMoreElements();)
									{
										final Item I = o.nextElement();
										Item chkI = I;
										if(I.container() instanceof DeadBody)
											chkI=I.container();
										if((chkI instanceof DeadBody)
										&&(((DeadBody)chkI).isPlayerCorpse())
										&&(CMLib.flags().isInTheGame(chkI, true)))
											removeThese.add(I);
									}
									for(final Item I : removeThese)
										pStat.getExtItems().delItem(I);
								}
								CMLib.players().obliteratePlayer(mob,true,CMSecurity.isDisabled(CMSecurity.DisFlag.DEATHCRY));
								mob.destroy();
							}
						});
						return false;
					}
					else
					if(deadMOB.isPlayer())
					{
						final int remain = (maxLives-deadMOB.playerStats().deathCounter(0));
						if(remain == 1)
							deadMOB.tell(L("\n\r^HYou have ^W1^? life remaining.^N"));
						else
							deadMOB.tell(L("\n\r^HYou have ^W@x1^? lives remaining.^N",""+remain));
					}
				}
			}
			else
			if(whatToDo.startsWith("LOSESK")) // "LOSESKILL
			{
				if(deadM.numAbilities()>0)
				{
					final Ability A=deadM.fetchAbility(CMLib.dice().roll(1,deadM.numAbilities(),-1));
					if(A!=null)
					{
						deadM.tell(L("You've forgotten @x1.",A.Name()));
						deadM.delAbility(A);
						if(A.isAutoInvoked())
						{
							final Ability A2=deadM.fetchEffect(A.ID());
							A2.unInvoke();
							deadM.delEffect(A2);
						}
					}
				}
			}
			else
			if(CMath.s_parseIntExpression(whatToDo,varVals)>0)
			{
				lostExperience[0]=CMath.s_parseIntExpression(whatToDo,varVals);
				if((lostExperience[0]>0)
				&&(!deadM.charStats().getCurrentClass().expless()))
				{
					lostExperience[0]=-CMLib.leveler().postExperience(deadM,"DEATH:",null,null,-lostExperience[0], false);
					message=L(message,""+lostExperience[0]);
					deadM.tell(message);
				}
			}
			else
			if(whatToDo.startsWith("EXPER")) // "EXPERIENCE
			{
				lostExperience[0]=baseExperience;
				if((lostExperience[0]>0)
				&&(!deadM.charStats().getCurrentClass().expless()))
				{
					lostExperience[0]=-CMLib.leveler().postExperience(deadM,"DEATH:",null,null,-lostExperience[0], false);
					message=L(message,""+lostExperience[0]);
					deadM.tell(message);
				}
			}
		}
		return true;
	}

	@Override
	public boolean postRevengeAttack(final MOB attacker, final MOB defender)
	{
		if((attacker!=null)
		&&(!attacker.isInCombat())
		&&(!attacker.amDead())
		&&(!attacker.amDestroyed())
		&&(attacker.isMonster())
		&&(attacker!=defender)
		&&(defender!=null)
		&&(attacker.location()==defender.location())
		&&(attacker.location().isInhabitant(defender))
		&&(CMLib.flags().canBeSeenBy(defender,attacker)))
			return postAttack(attacker,defender,attacker.fetchWieldedItem());
		return false;
	}

	@Override
	public int getTickStatus()
	{
		return tickStatus;
	}

	protected void tickAllShips()
	{
		if((CMLib.map() != CMLib.get(MudHost.MAIN_HOST)._map())
		||(Thread.currentThread().getThreadGroup().getName().charAt(0)==MudHost.MAIN_HOST))
		{
			for(final Enumeration<Boardable> s = CMLib.map().ships();s.hasMoreElements();)
			{
				final Boardable ship = s.nextElement();
				ship.tick(ship, Tickable.TICKID_SPECIALMANEUVER);
			}
			for(final Enumeration<Boardable> s = CMLib.map().ships();s.hasMoreElements();)
			{
				final Boardable ship = s.nextElement();
				ship.tick(ship, Tickable.TICKID_SPECIALCOMBAT);
			}
		}
	}

	protected void runSpecialCombat()
	{
		tickAllShips();
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		try
		{
			if(!CMSecurity.isDisabled(CMSecurity.DisFlag.SPECOMBATTHREAD))
			{
				isDebugging=CMSecurity.isDebugging(DbgFlag.UTILITHREAD);
				tickStatus=Tickable.STATUS_ALIVE;
				runSpecialCombat();
			}
		}
		finally
		{
			tickStatus=Tickable.STATUS_NOT;
			setThreadStatus(serviceClient,"sleeping");
		}
		return true;
	}

	@Override
	public MOB getBreatheKiller(final MOB victim)
	{
		MOB victimKiller = victim.getVictim();
		if(victimKiller != null)
			return victimKiller;
		final Room R=victim.location();
		if(R!=null)
		{
			for(final Enumeration<Ability> a=R.effects();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if((A!=null)
				&&(A.invoker()!=null)
				&&(A.invoker()!=victim))
				{
					if(A.abstractQuality()==Ability.QUALITY_MALICIOUS)
						return A.invoker();
					victimKiller = A.invoker();
				}
			}
		}
		for(final Enumeration<Ability> a=victim.personalEffects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)
			&&(A.invoker()!=null)
			&&(A.invoker()!=victimKiller))
			{
				if(A.abstractQuality()==Ability.QUALITY_MALICIOUS)
					return A.invoker();
				victimKiller = A.invoker();
			}
		}
		return (victimKiller == null) ? victim : victimKiller;
	}

	@Override
	public boolean shutdown()
	{
		if(CMLib.threads().isTicking(this, TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK))
		{
			CMLib.threads().deleteTick(this, TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK);
			serviceClient=null;
		}
		return true;
	}
}
