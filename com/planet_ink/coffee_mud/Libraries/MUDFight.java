package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.Fighter;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.AccountStats.PrideStat;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

import java.util.*;

/*
   Copyright 2000-2014 Bo Zimmerman

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
	public String ID(){return "MUDFight";}
	static final char[] PARENS={'(',')'};
	
	public String lastStr="";
	public long lastRes=0;
	public Object[][][] hitWordIndex=null;
	public Object[][][] hitWordsChanged=null;
	protected LinkedList<CMath.CompiledOperation> attackAdjustmentFormula = null;
	protected LinkedList<CMath.CompiledOperation> armorAdjustmentFormula = null;
	protected LinkedList<CMath.CompiledOperation> attackerFudgeBonusFormula  = null;
	protected LinkedList<CMath.CompiledOperation> pvpAttackerFudgeBonusFormula  = null;
	protected LinkedList<CMath.CompiledOperation> spellFudgeDamageFormula  = null;
	protected LinkedList<CMath.CompiledOperation> pvpSpellFudgeDamageFormula  = null;
	protected LinkedList<CMath.CompiledOperation> spellCritChanceFormula = null;
	protected LinkedList<CMath.CompiledOperation> pvpSpellCritChanceFormula = null;
	protected LinkedList<CMath.CompiledOperation> spellCritDmgFormula = null;
	protected LinkedList<CMath.CompiledOperation> pvpSpellCritDmgFormula = null;
	protected LinkedList<CMath.CompiledOperation> targetedRangedDamageFormula = null;
	protected LinkedList<CMath.CompiledOperation> pvpTargetedRangedDamageFormula = null;
	protected LinkedList<CMath.CompiledOperation> rangedFudgeDamageFormula  = null;
	protected LinkedList<CMath.CompiledOperation> pvpRangedFudgeDamageFormula  = null;
	protected LinkedList<CMath.CompiledOperation> targetedMeleeDamageFormula = null;
	protected LinkedList<CMath.CompiledOperation> pvpTargetedMeleeDamageFormula = null;
	protected LinkedList<CMath.CompiledOperation> meleeFudgeDamageFormula  = null;
	protected LinkedList<CMath.CompiledOperation> pvpMeleeFudgeDamageFormula  = null;
	protected LinkedList<CMath.CompiledOperation> staticRangedDamageFormula = null;
	protected LinkedList<CMath.CompiledOperation> staticMeleeDamageFormula = null;
	protected LinkedList<CMath.CompiledOperation> weaponCritChanceFormula = null;
	protected LinkedList<CMath.CompiledOperation> pvpWeaponCritChanceFormula = null;
	protected LinkedList<CMath.CompiledOperation> weaponCritDmgFormula = null;
	protected LinkedList<CMath.CompiledOperation> pvpWeaponCritDmgFormula = null;
	protected LinkedList<CMath.CompiledOperation> stateHitPointRecoverFormula = null;
	protected LinkedList<CMath.CompiledOperation> stateManaRecoverFormula = null;
	protected LinkedList<CMath.CompiledOperation> stateMovesRecoverFormula  = null;
	
	private static final int ATTACK_ADJUSTMENT = 50;

	public boolean activate()
	{
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
Log.errOut("BZ: asdf: "+attackerFudgeBonusFormula+"/"+pvpAttackerFudgeBonusFormula);
		return true; 
	}
	
	@Override 
	public void propertiesLoaded() 
	{
Log.errOut("BZ: activate: "+this);
		activate(); 
	}
	
	public Set<MOB> allPossibleCombatants(MOB mob, boolean beRuthless)
	{
		SHashSet<MOB> h=new SHashSet<MOB>();
		Room thisRoom=mob.location();
		if(thisRoom==null) return null;
		Set<MOB> h1=mob.getGroupMembers(new HashSet<MOB>());
		for(int m=0;m<thisRoom.numInhabitants();m++)
		{
			MOB inhab=thisRoom.fetchInhabitant(m);
			if((inhab!=null)
			&&(inhab!=mob)
			&&(!h1.contains(inhab))
			&&(CMLib.flags().isSeen(inhab)||CMLib.flags().canMove(inhab))
			&&(CMLib.flags().isSeen(inhab)||(!CMLib.flags().isCloaked(inhab)))
			&&((beRuthless)||(!mob.isMonster())||(!inhab.isMonster())))
				h.addUnsafe(inhab);
		}
		return h;
	}

	public Set<MOB> properTargets(Ability A, MOB caster, boolean beRuthless)
	{
		Set<MOB> h=null;
		if(A.abstractQuality()!=Ability.QUALITY_MALICIOUS)
		{
			if(caster.Name().equalsIgnoreCase("somebody"))
				h=new SHashSet<MOB>();
			else
				h=caster.getGroupMembers(new SHashSet<MOB>());
			for(Iterator<MOB> e=h.iterator();e.hasNext();)
			{
				MOB M=e.next();
				if(M.location()!=caster.location())
					h.remove(M);
			}
		}
		else
		if(caster.isInCombat())
			h=allCombatants(caster);
		else
			h=allPossibleCombatants(caster,beRuthless);
		return h;
	}
	
	public int adjustedAttackBonus(MOB mob, MOB target)
	{
		int maxStr = mob.charStats().getMaxStat(CharStats.STAT_STRENGTH);
		int currStr = mob.charStats().getStat(CharStats.STAT_STRENGTH);
		int strBonus = 0;
		if(currStr > maxStr)
		{
			strBonus = currStr - maxStr;
			currStr = maxStr;
		}
		int baseStr = mob.baseCharStats().getStat(CharStats.STAT_STRENGTH);
		if(baseStr > maxStr) baseStr = maxStr;
		double[] vars = {mob.phyStats().attackAdjustment(),
						 currStr,
						 baseStr,
						 strBonus,
						 (mob.curState().getHunger()<1)?1.0:0.0,
						 (mob.curState().getThirst()<1)?1.0:0.0,
						 (mob.curState().getFatigue()>CharState.FATIGUED_MILLIS)?1.0:0.0
						};
		return (int)Math.round(CMath.parseMathExpression(attackAdjustmentFormula, vars, 0.0));
	}

	public void postItemDamage(MOB mob, Item I, Environmental tool, int damageAmount, int messageType, String message)
	{
		if(mob==null) return ;
		Room R=mob.location();
		if(R==null) return ;
		CMMsg msg=CMClass.getMsg(mob,I,tool,CMMsg.MASK_MALICIOUS|CMMsg.MASK_ALWAYS|messageType,message,CMMsg.MASK_MALICIOUS|CMMsg.MASK_ALWAYS|messageType,message,CMMsg.NO_EFFECT,null);
		if(R.okMessage(mob,msg))
		{
			R.send(mob,msg);
			if(msg.value()<=0)
			{
				I.setUsesRemaining(I.usesRemaining()-damageAmount);
				I.recoverPhyStats(); // important relation to setuses -- for brittle
				if(I.usesRemaining()<=0)
				{
					I.setUsesRemaining(100);
					I.unWear();
					msg=CMClass.getMsg(mob,null,I,CMMsg.MSG_OK_VISUAL,"<O-NAME> is destroyed!",null,"<O-NAME> carried by <S-NAME> is destroyed!");
					if(R.okMessage(mob,msg))
						R.send(mob,msg);
					I.destroy();
					mob.recoverPhyStats();
					mob.recoverCharStats();
					mob.recoverMaxState();
					R.recoverRoomStats();
				}
				else
				if(I.usesRemaining()<=10)
				{
					mob.tell(I.name(mob)+" is looking really bad.");
				}
			}
		}
	}

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
	
	public int adjustedArmor(MOB mob)
	{
		int currDex=mob.charStats().getStat(CharStats.STAT_DEXTERITY);
		int maxDex = mob.charStats().getMaxStat(CharStats.STAT_DEXTERITY);
		int dexBonus = 0;
		if(currDex > maxDex)
		{
			dexBonus = currDex - maxDex;
			currDex = maxDex;
		}
		double baseDex=mob.baseCharStats().getStat(CharStats.STAT_DEXTERITY);
		if(baseDex > maxDex) baseDex = maxDex;
		
		double[] vars = {
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

	public boolean rollToHit(MOB attacker, MOB defender)
	{
		if((attacker==null)||(defender==null)) return false;
		double vars[] = {
			attacker.phyStats().level(),
			defender.phyStats().level(),
			attacker.phyStats().level() > defender.phyStats().level() ? 1 : -1
		};
		final boolean isPVP=(attacker.isPlayer()&&defender.isPlayer());
		int attackerFudgeBonusAmt = (int)Math.round(CMath.parseMathExpression(isPVP?pvpAttackerFudgeBonusFormula:attackerFudgeBonusFormula, vars, 0.0));
		return rollToHit(adjustedAttackBonus(attacker,defender),adjustedArmor(defender),attackerFudgeBonusAmt);
	}

	public boolean rollToHit(int attack, int defence, int adjustment)
	{
		double myArmor= -((double)defence);
		if(myArmor==0) myArmor=1.0;
		else
		if(myArmor<0.0) myArmor=-CMath.div(1.0,myArmor);
		double hisAttack=attack;
		if(hisAttack==0.0) hisAttack=1.0;
		else
		if(hisAttack<0.0) hisAttack=-CMath.div(1.0,myArmor);
		return CMLib.dice().normalizeAndRollLess((int)Math.round(50.0*(hisAttack/myArmor)) + adjustment);
	}
	
	public Set<MOB> allCombatants(MOB mob)
	{
		SHashSet<MOB> h=new SHashSet<MOB>();
		Room thisRoom=mob.location();
		if(thisRoom==null) return null;
		if(!mob.isInCombat()) return null;

		Set<MOB> h1=null;
		if(mob.Name().equalsIgnoreCase("nobody"))
			h1=new HashSet<MOB>();
		else
			h1=mob.getGroupMembers(new HashSet<MOB>());
		for(int m=0;m<thisRoom.numInhabitants();m++)
		{
			MOB inhab=thisRoom.fetchInhabitant(m);
			if((inhab!=null)
			&&(inhab!=mob)
			&&((inhab==mob.getVictim())
				||((inhab!=mob)
					&&(inhab.getVictim()!=mob.getVictim())
					&&(CMLib.flags().isSeen(inhab)||(!CMLib.flags().isCloaked(inhab)))
					&&(CMLib.flags().canMove(inhab)||CMLib.flags().isSeen(inhab))
					&&(!h1.contains(inhab)))))
				 h.addUnsafe(inhab);
		}
		return h;

	}

	public void makePeaceInGroup(MOB mob)
	{
		Set<MOB> myGroup=mob.getGroupMembers(new HashSet<MOB>());
		for(Iterator<MOB> e=myGroup.iterator();e.hasNext();)
		{
			MOB mob2=e.next();
			if(mob2.isInCombat()&&(myGroup.contains(mob2.getVictim())))
				mob2.makePeace();
		}
	}

	public void postPanic(MOB mob, CMMsg addHere)
	{
		if(mob==null) return;

		// make sure he's not already dead, or with a pending death.
		if(mob.amDead()) return;
		if((addHere!=null)&&(addHere.trailerMsgs()!=null))
			for(CMMsg msg : addHere.trailerMsgs())
				if((msg.source()==mob)
				&&((msg.sourceMinor()==CMMsg.TYP_PANIC))
				   ||(msg.sourceMinor()==CMMsg.TYP_DEATH))
					return;
		CMMsg msg=CMClass.getMsg(mob,null,CMMsg.MSG_PANIC,null);
		if(addHere!=null)
			addHere.addTrailerMsg(msg);
		else
		if((mob.location()!=null)&&(mob.location().okMessage(mob,msg)))
			mob.location().send(mob,msg);
	}

	public void postDeath(MOB killerM, MOB deadM, CMMsg addHere)
	{
		if(deadM==null) return;
		Room deathRoom=deadM.location();
		if(deathRoom==null) return;

		// make sure he's not already dead, or with a pending death.
		if(deadM.amDead()) return;
		if((addHere!=null)&&(addHere.trailerMsgs()!=null))
			for(CMMsg msg : addHere.trailerMsgs())
				if((msg.source()==deadM)
				&&((msg.sourceMinor()==CMMsg.TYP_PANIC))
				   ||(msg.sourceMinor()==CMMsg.TYP_DEATH))
					return;

		String msp=CMLib.protocol().msp("death"+CMLib.dice().roll(1,7,0)+".wav",50);
		CMMsg msg=null;
		if(isKnockedOutUponDeath(deadM,killerM))
			msg=CMClass.getMsg(deadM,null,killerM,
					CMMsg.MSG_OK_VISUAL,"^f^*^<FIGHT^>!!!!!!!!!YOU ARE DEFEATED!!!!!!!!!!^</FIGHT^>^?^.\n\r"+msp,
					CMMsg.MSG_OK_VISUAL,null,
					CMMsg.MSG_DEATH,"^F^<FIGHT^><S-NAME> is DEFEATED!!!^</FIGHT^>^?\n\r"+msp);
		else
			msg=CMClass.getMsg(deadM,null,killerM,
				CMMsg.MSG_OK_VISUAL,"^f^*^<FIGHT^>!!!!!!!!!!!!!!YOU ARE DEAD!!!!!!!!!!!!!!^</FIGHT^>^?^.\n\r"+msp,
				CMMsg.MSG_OK_VISUAL,null,
				CMMsg.MSG_DEATH,"^F^<FIGHT^><S-NAME> is DEAD!!!^</FIGHT^>^?\n\r"+msp);
		CMLib.map().sendGlobalMessage(deadM,CMMsg.TYP_DEATH, CMClass.getMsg(deadM,null,killerM, CMMsg.TYP_DEATH,null, CMMsg.TYP_DEATH,null, CMMsg.TYP_DEATH,null));
		CMMsg msg2=CMClass.getMsg(deadM,null,killerM, CMMsg.MSG_DEATH,null, CMMsg.MSG_DEATH,null, CMMsg.MSG_DEATH,null);
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

	public boolean postAttack(MOB attacker, MOB target, Item weapon)
	{
		if((attacker==null)||(!attacker.mayPhysicallyAttack(target)))
			return false;
		if((weapon==null)
		&&(CMath.bset(attacker.getBitmap(),MOB.ATT_AUTODRAW)))
		{
			CMLib.commands().postDraw(attacker,false,true);
			weapon=attacker.fetchWieldedItem();
		}
		CMMsg msg=CMClass.getMsg(attacker,target,weapon,CMMsg.MSG_WEAPONATTACK,null);
		Room R=target.location();
		if(R!=null)
			if(R.okMessage(attacker,msg))
			{
				R.send(attacker,msg);
				return msg.value()>0;
			}
		return false;
	}

	public boolean postHealing(MOB healer,
							   MOB target,
							   Environmental tool,
							   int messageCode,
							   int healing,
							   String allDisplayMessage)
	{
		if(healer==null) healer=target;
		if((healer==null)||(target==null)||(target.location()==null)) return false;
		CMMsg msg=CMClass.getMsg(healer,target,tool,messageCode,CMMsg.MSG_HEALING,messageCode,allDisplayMessage);
		msg.setValue(healing);
		Room R=target.location();
		if(R!=null)
			if(R.okMessage(target,msg))
			{ R.send(target,msg); return true;}
		return false;
	}

	public String replaceDamageTag(String str, int damage, int damageType, char sourceTargetSTO)
	{
		if(str==null) return null;
		final int replace=str.indexOf("<DAMAGE");
		if(replace < 0)
			return str;
		if(str.length() < replace+9)
			return str;
		final boolean damages = (str.charAt(replace+7)=='S') && (str.charAt(replace+8)=='>');
		final String showDamage = CMProps.getVar(CMProps.Str.SHOWDAMAGE);
		final boolean showNumbers = showDamage.equalsIgnoreCase("YES")
								||((sourceTargetSTO=='S')&&showDamage.equalsIgnoreCase("SOURCE"))
								||((sourceTargetSTO=='T')&&showDamage.equalsIgnoreCase("TARGET"));
		if(damages)
		{
			final String hitWord=CMStrings.deleteAllofAny(standardHitWord(damageType,damage),PARENS);
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

	public void postDamage(MOB attacker, MOB target, Environmental weapon, int damage, int messageCode, int damageType, String allDisplayMessage)
	{
		if((attacker==null)||(target==null)||(target.location()==null)) return;
		if(allDisplayMessage!=null) allDisplayMessage="^F^<FIGHT^>"+allDisplayMessage+"^</FIGHT^>^?";
		
		final int damageTypeMsg;
		if(attacker != target)
		{
			if((weapon instanceof Ability)
			&&(damage>0)
			&&(attacker.isMine(weapon)||(attacker.phyStats().level()>1))) // why >1? because quickly made fake-mobs tend to have lvl=1
				damage = modifySpellDamage(attacker, target, damage);
			damageTypeMsg = CMMsg.TYP_DAMAGE | (CMMsg.MASK_MALICIOUS & messageCode);
		}
		else
			damageTypeMsg = CMMsg.TYP_DAMAGE;
		
		CMMsg msg=CMClass.getMsg(attacker,target,weapon,messageCode,damageTypeMsg,messageCode,allDisplayMessage);
		msg.setValue(damage);
		CMLib.color().fixSourceFightColor(msg);
		Room R=target.location();
		if(R!=null)
			if(R.okMessage(target,msg))
			{
				if(damageType>=0)
					msg.modify(msg.source(),
							   msg.target(),
							   msg.tool(),
							   msg.sourceCode(),
							   replaceDamageTag(msg.sourceMessage(),msg.value(),damageType,'S'),
							   msg.targetCode(),
							   replaceDamageTag(msg.targetMessage(),msg.value(),damageType,'T'),
							   msg.othersCode(),
							   replaceDamageTag(msg.othersMessage(),msg.value(),damageType,'O'));
				R.send(target,msg);
			}
	}
	
	public int modifySpellDamage(MOB attacker, MOB target, int baseDamage)
	{
		int maxInt = attacker.charStats().getMaxStat(CharStats.STAT_INTELLIGENCE);
		int currInt = attacker.charStats().getStat(CharStats.STAT_INTELLIGENCE);
		int intBonus = 0;
		if(currInt > maxInt) 
		{
			intBonus = currInt - maxInt;
			currInt = maxInt;
		}
		int baseInt = attacker.baseCharStats().getStat(CharStats.STAT_INTELLIGENCE);
		if(baseInt > maxInt) baseInt = maxInt;
		double[] vars = {
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
		int spellCritChancePct = (int)Math.round(CMath.parseMathExpression(isPVP?pvpSpellCritChanceFormula:spellCritChanceFormula, vars, 0.0));
		if(CMLib.dice().rollPercentage()<spellCritChancePct)
		{
			int spellCritDamageAmt = (int)Math.round(CMath.parseMathExpression(isPVP?pvpSpellCritDmgFormula:spellCritDmgFormula, vars, 0.0));
			baseDamage+=spellCritDamageAmt;
		}
		return baseDamage;
	}
	
	public int adjustedDamage(MOB mob, Weapon weapon, MOB target, int bonusDamage, boolean allowCrits)
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
		if(target!=null)
		{
			double[] vars = {
					useDmg.phyStats().damage()+bonusDamage,
					mob.charStats().getStat(CharStats.STAT_STRENGTH),
					mob.phyStats().level(),
					target.phyStats().level(),
					(mob.curState().getHunger()<1)?1.0:0.0,
					(mob.curState().getThirst()<1)?1.0:0.0,
					(mob.curState().getFatigue()>CharState.FATIGUED_MILLIS)?1.0:0.0,
					CMLib.flags().canBeSeenBy(target,mob)?0:1,
					CMLib.flags().isSleeping(target)?1:0,
					CMLib.flags().isSitting(target)?1:0
				};
			if(rangedAttack)
				damageAmount = CMath.parseMathExpression(isPVP?pvpTargetedRangedDamageFormula:targetedRangedDamageFormula, vars, 0.0);
			else
				damageAmount = CMath.parseMathExpression(isPVP?pvpTargetedMeleeDamageFormula:targetedMeleeDamageFormula, vars, 0.0);
		}
		else
		{
			double[] vars = {
					useDmg.phyStats().damage()+bonusDamage,
					mob.charStats().getStat(CharStats.STAT_STRENGTH),
					mob.phyStats().level(),
					0,
					(mob.curState().getHunger()<1)?1.0:0.0,
					(mob.curState().getThirst()<1)?1.0:0.0,
					(mob.curState().getFatigue()>CharState.FATIGUED_MILLIS)?1.0:0.0,
					0,
					0,
					0
				};
			if((weapon!=null)&&((weapon.weaponClassification()==Weapon.CLASS_RANGED)||(weapon.weaponClassification()==Weapon.CLASS_THROWN)))
				damageAmount = CMath.parseMathExpression(staticRangedDamageFormula, vars, 0.0);
			else
				damageAmount = CMath.parseMathExpression(staticMeleeDamageFormula, vars, 0.0);
		}
		
		int maxDex = mob.charStats().getMaxStat(CharStats.STAT_DEXTERITY);
		int currDex = mob.charStats().getStat(CharStats.STAT_DEXTERITY);
		int dexBonus = 0;
		if(currDex > maxDex)
		{
			dexBonus = currDex - maxDex;
			currDex = maxDex;
		}
		int baseDex = mob.baseCharStats().getStat(CharStats.STAT_DEXTERITY);
		if(baseDex > maxDex) baseDex = maxDex;
		
		double[] vars = {
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
			int weaponCritChancePct = (int)Math.round(CMath.parseMathExpression(isPVP?pvpWeaponCritChanceFormula:weaponCritChanceFormula, vars, 0.0));
			if(CMLib.dice().rollPercentage()<weaponCritChancePct)
			{
				int weaponCritDmgAmt = (int)Math.round(CMath.parseMathExpression(isPVP?pvpWeaponCritDmgFormula:weaponCritDmgFormula, vars, 0.0));
				damageAmount += weaponCritDmgAmt;
			}
		}
		if(target != null)
		{
			vars[0] = damageAmount;
			if(rangedAttack)
				damageAmount = CMath.parseMathExpression(isPVP?pvpRangedFudgeDamageFormula:rangedFudgeDamageFormula, vars, 0.0);
			else
				damageAmount = CMath.parseMathExpression(isPVP?pvpMeleeFudgeDamageFormula:meleeFudgeDamageFormula, vars, 0.0);
		}
		return (int)Math.round(damageAmount);
	}
	
	
	public void recoverTick(MOB mob)
	{
		if((mob!=null)
		&&(!mob.isInCombat())
		&&(!CMLib.flags().isClimbing(mob))
		&&(CMProps.getBoolVar(CMProps.Bool.MUDSTARTED)))
		{
			final CharStats charStats=mob.charStats();
			final CharState curState=mob.curState();
			final CharState maxState=mob.maxState();
			final boolean isSleeping=(CMLib.flags().isSleeping(mob));
			final boolean isSittingOrRiding=(!isSleeping) && ((CMLib.flags().isSitting(mob))||(mob.riding()!=null));
			final boolean isFlying=(!isSleeping) && (!isSittingOrRiding) && CMLib.flags().isFlying(mob);
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
			double hpGain = CMath.parseMathExpression(stateHitPointRecoverFormula, vals, 0.0);
			
			if((hpGain>0)&&(!CMLib.flags().isGolem(mob)))
				curState.adjHitPoints((int)Math.round(hpGain),maxState);

			vals[0]=((charStats.getStat(CharStats.STAT_INTELLIGENCE)+charStats.getStat(CharStats.STAT_WISDOM)));
			double manaGain = CMath.parseMathExpression(stateManaRecoverFormula, vals, 0.0);
			
			if(manaGain>0)
				curState.adjMana((int)Math.round(manaGain),maxState);
			
			vals[0]=charStats.getStat(CharStats.STAT_STRENGTH);
			double moveGain = CMath.parseMathExpression(this.stateMovesRecoverFormula, vals, 0.0);
			
			if(moveGain>0)
				curState.adjMovement((int)Math.round(moveGain),maxState);
		}
	}

	
	public void postWeaponDamage(MOB source, MOB target, Item item, boolean success)
	{
		if(source==null) return;
		if(!source.mayIFight(target)) return;
		Weapon weapon=null;
		int damageInt=0;
		int damageType=Weapon.TYPE_BASHING;
		if(item instanceof Weapon)
		{
			weapon=(Weapon)item;
			damageInt=CMLib.combat().adjustedDamage(source,weapon,target,0,true);
			damageType=weapon.weaponType();
		}
		if(success)
		{
			// calculate Base Damage (with Strength bonus)
			String oldHitString="^F^<FIGHT^>"+((weapon!=null)?
								weapon.hitString(damageInt):
								standardHitString(Weapon.TYPE_NATURAL,Weapon.CLASS_BLUNT,damageInt,item.name()))+"^</FIGHT^>^?";
			CMMsg msg=CMClass.getMsg(source,
									target,
									item,
									CMMsg.MSG_OK_VISUAL,
									CMMsg.MSG_DAMAGE,
									CMMsg.MSG_OK_VISUAL,
									oldHitString);
			CMLib.color().fixSourceFightColor(msg);

			msg.setValue(damageInt);
			// why was there no okaffect here?
			Room room=source.location();
			if((room!=null)&&(room.okMessage(source,msg)))
			{
				if(msg.targetMinor()==CMMsg.TYP_DAMAGE)
				{
					damageInt=msg.value();
					msg.modify(msg.source(),
							   msg.target(),
							   msg.tool(),
							   msg.sourceCode(),
							   replaceDamageTag(msg.sourceMessage(),msg.value(),damageType,'S'),
							   msg.targetCode(),
							   replaceDamageTag(msg.targetMessage(),msg.value(),damageType,'T'),
							   msg.othersCode(),
							   replaceDamageTag(msg.othersMessage(),msg.value(),damageType,'O'));
				}
				if((source.mayIFight(target))
				&&(source.location()==room)
				&&(target.location()==room))
					room.send(source,msg);
			}
		}
		else
		{
			String missString="^F^<FIGHT^>"+((weapon!=null)?
								weapon.missString():
								standardMissString(Weapon.TYPE_BASHING,Weapon.CLASS_BLUNT,item.name(),false))+"^</FIGHT^>^?";
			CMMsg msg=CMClass.getMsg(source,
									target,
									weapon,
									CMMsg.MSG_NOISYMOVEMENT,
									missString);
			CMLib.color().fixSourceFightColor(msg);
			// why was there no okaffect here?
			Room R=source.location();
			if(R!=null)
			if(R.okMessage(source,msg) && (!source.amDead()) && (!source.amDestroyed()))
				R.send(source,msg);
		}
	}

	public void processFormation(List<MOB>[] done, MOB leader, int level)
	{
		for(int i=0;i<done.length;i++)
			if((done[i]!=null)&&(done[i].contains(leader)))
				return;
		if(level>=done.length) return;
		if(done[level]==null) done[level]=new Vector<MOB>();
		done[level].add(leader);
		for(int f=0;f<leader.numFollowers();f++)
		{
			MOB M=leader.fetchFollower(f);
			if(M==null) continue;
			int range=leader.fetchFollowerOrder(M);
			if(range<0) range=0;
			processFormation(done,M,level+range);
		}
	}

	public MOB getFollowedLeader(MOB mob)
	{
		MOB leader=mob;
		if(leader.amFollowing()!=null)
			leader=leader.amUltimatelyFollowing();
		return leader;
	}

	@SuppressWarnings("unchecked")
	public List<MOB>[] getFormation(MOB mob)
	{
		MOB leader=mob;
		if(leader.amFollowing()!=null)
			leader=leader.amUltimatelyFollowing();
		Vector<MOB>[] done=new Vector[20];
		processFormation(done,leader,0);
		return done;
	}

	public List<MOB> getFormationFollowed(MOB mob)
	{
		List<MOB>[] form=getFormation(mob);
		for(int i=1;i<form.length;i++)
		{
			if((form[i]!=null)&&(form[i].contains(mob)))
			{
				i--;
				while(i>=0)
				{
					if((form[i]!=null)&&(form[i].size()>0))
						return form[i];
					i--;
				}
			}
		}
		return null;
	}

	public int getFormationAbsOrder(MOB mob)
	{
		List<MOB>[] form=getFormation(mob);
		for(int i=1;i<form.length;i++)
		{
			if((form[i]!=null)&&(form[i].contains(mob)))
				return i;
		}
		return 0;
	}
	
	public MOB getCombatDominentKiller(MOB killer, MOB killed)
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
		for(Iterator<MOB> m=checked.iterator(); m.hasNext(); )
		{
			M=m.next();
			if((!M.isMonster())&&(M.charStats()!=null))
				return M;
		}
		return killer;
	}

	public CharClass getCombatDominantClass(MOB killer, MOB killed)
	{
		CharClass C=null;

		if((killer!=null)&&(killer.charStats()!=null))
		{
			C=killer.charStats().getCurrentClass();
			MOB M=killer;
			HashSet<MOB> checked=new HashSet<MOB>();
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

	protected Set<MOB> getCombatBeneficiaries(MOB killer, MOB killed, Room deathRoom, Set<MOB> beneficiaries, CharClass combatCharClass)
	{
		Set<MOB> followers=(killer!=null)?killer.getGroupMembers(new HashSet<MOB>()):(new SHashSet<MOB>());
		if(combatCharClass==null) combatCharClass=CMClass.getCharClass("StdCharClass");
		if(deathRoom!=null)
		{
			for(int m=0;m<deathRoom.numInhabitants();m++)
			{
				MOB mob=deathRoom.fetchInhabitant(m);
				if((combatCharClass.isValidClassBeneficiary(killer,killed,mob,followers))
				&&(!beneficiaries.contains(mob)))
					beneficiaries.add(mob);
			}
		}
		if((killer!=null)&&(!beneficiaries.contains(killer))&&(killer!=killed)&&(CMLib.flags().isInTheGame(killer,true)))
			beneficiaries.add(killer);
		return beneficiaries;
	}

	public Set<MOB> getCombatBeneficiaries(MOB killer, MOB killed, CharClass combatCharClass)
	{
		if((killer==null)||(killed==null)) return new SHashSet<MOB>();
		SHashSet<MOB> beneficiaries=new SHashSet<MOB>();
		Room R=killer.location();
		if(R!=null) getCombatBeneficiaries(killer,killed,R,beneficiaries,combatCharClass);
		R=killed.location();
		if((R!=null)&&(R!=killer.location())) getCombatBeneficiaries(killer,killed,R,beneficiaries,combatCharClass);
		return beneficiaries;
	}

	protected Set<MOB> getCombatDividers(MOB killer, MOB killed, Room deathRoom, Set<MOB> dividers, CharClass combatCharClass)
	{
		Set<MOB> followers=(killer!=null)?killer.getGroupMembers(new HashSet<MOB>()):(new HashSet<MOB>());
		if(combatCharClass==null) combatCharClass=CMClass.getCharClass("StdCharClass");
		if(deathRoom!=null)
		{
			for(int m=0;m<deathRoom.numInhabitants();m++)
			{
				MOB mob=deathRoom.fetchInhabitant(m);
				if((combatCharClass.isValidClassDivider(killer,killed,mob,followers))
				&&(!dividers.contains(mob)))
					dividers.add(mob);
			}
		}
		if((killer!=null)&&(!dividers.contains(killer))&&(killer!=killed)&&(CMLib.flags().isInTheGame(killer,true)))
			dividers.add(killer);
		return dividers;
	}

	public Set<MOB> getCombatDividers(MOB killer, MOB killed, CharClass combatCharClass)
	{
		if((killer==null)||(killed==null)) return new SHashSet<MOB>();
		Set<MOB> dividers=new SHashSet<MOB>();
		Room R=killer.location();
		if(R!=null) getCombatDividers(killer,killed,R,dividers,combatCharClass);
		R=killed.location();
		if((R!=null)&&(R!=killer.location())) getCombatDividers(killer,killed,R,dividers,combatCharClass);
		return dividers;
	}

	public DeadBody justDie(MOB source, MOB target)
	{
		if(target==null) return null;
		Room deathRoom=target.location();

		//TODO: this creates too many loops.  The right thing is to loop once
		// and call  a boolean function to populate all these lists.
		CharClass combatCharClass=getCombatDominantClass(source,target);
		Set<MOB> beneficiaries=getCombatBeneficiaries(source,target,combatCharClass);
		Set<MOB> hisGroupH=target.getGroupMembers(new HashSet<MOB>());
		for(Enumeration<MOB> m=deathRoom.inhabitants();m.hasMoreElements();)
		{
			MOB M=m.nextElement();
			if((M!=null)&&(M.getVictim()==target))
				pickNextVictim(M, target, hisGroupH);
		}
		Set<MOB> dividers=getCombatDividers(source,target,combatCharClass);
		

		dispenseExperience(beneficiaries,dividers,target);

		String currency=CMLib.beanCounter().getCurrency(target);
		double deadMoney=CMLib.beanCounter().getTotalAbsoluteValue(target,currency);
		double myAmountOfDeadMoney=0.0;
		Vector<MOB> goldLooters=new Vector<MOB>();
		for(Iterator<MOB> e=beneficiaries.iterator();e.hasNext();)
		{
			MOB M=e.next();
			if(((CMath.bset(M.getBitmap(),MOB.ATT_AUTOGOLD))
			&&(!goldLooters.contains(M)))
			&&(M!=target)
			&&(M.location()==deathRoom)
			&&(deathRoom.isInhabitant(M)))
			   goldLooters.addElement(M);
		}
		if((goldLooters.size()>0)&&(deadMoney>0))
		{
			myAmountOfDeadMoney=CMath.div(deadMoney,goldLooters.size());
			CMLib.beanCounter().subtractMoney(target,deadMoney);
		}

		int[] expLost={100*target.phyStats().level()};
		if(expLost[0]<100) expLost[0]=100;
		String[] cmds=null;
		if((target.isMonster())||(target.soulMate()!=null))
			cmds=CMParms.toStringArray(CMParms.parseCommas(CMProps.getVar(CMProps.Str.MOBDEATH),true));
		else
			cmds=CMParms.toStringArray(CMParms.parseCommas(CMProps.getVar(CMProps.Str.PLAYERDEATH),true));
		
		DeadBody body=null; //must be done before consequences because consequences could be purging
		if((!CMParms.containsIgnoreCase(cmds,"RECALL"))
		&&(!isKnockedOutUponDeath(target,source)))
			body=target.killMeDead(true);
		
		handleConsequences(target,source,cmds,expLost,"^*You lose @x1 experience points.^?^.");

		if(!isKnockedOutUponDeath(target,source))
		{
			Room bodyRoom=deathRoom;
			if((body!=null)&&(body.owner() instanceof Room)&&(((Room)body.owner()).isContent(body)))
				bodyRoom=(Room)body.owner();
			if((source!=null)&&(body!=null))
			{
				body.setKillerName(source.Name());
				body.setKillerPlayer(!source.isMonster());
			}

			if((!target.isMonster())&&(CMLib.dice().rollPercentage()==1)&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.AUTODISEASE)))
			{
				Ability A=CMClass.getAbility("Disease_Amnesia");
				if((A!=null)&&(target.fetchEffect(A.ID())==null))
					A.invoke(target,target,true,0);
			}

			if(target.soulMate()!=null)
			{
				Session s=target.session();
				s.setMob(target.soulMate());
				target.soulMate().setSession(s);
				target.setSession(null);
				target.soulMate().tell("^HYour spirit has returned to your body...\n\r\n\r^N");
				CMLib.commands().postLook(target.soulMate(),true);
				target.setSoulMate(null);
			}

			if((source!=null)
			&&(bodyRoom!=null)
			&&(body!=null)
			&&(source.location()==bodyRoom)
			&&(bodyRoom.isInhabitant(source))
			&&(CMath.bset(source.getBitmap(),MOB.ATT_AUTOLOOT)))
			{
				if((source.riding()!=null)&&(source.riding() instanceof MOB))
					source.tell("You'll need to dismount to loot the body.");
				else
				if((source.riding()!=null)&&(source.riding() instanceof MOB))
					source.tell("You'll need to disembark to loot the body.");
				else
				for(int i=bodyRoom.numItems()-1;i>=0;i--)
				{
					Item item=bodyRoom.getItem(i);
					if((item!=null)
					&&(item.container()==body)
					&&(CMLib.flags().canBeSeenBy(body,source))
					&&((!body.destroyAfterLooting())||(!(item instanceof RawMaterial)))
					&&(CMLib.flags().canBeSeenBy(item,source)))
						CMLib.commands().postGet(source,body,item,false);
				}
				if(body.destroyAfterLooting())
					bodyRoom.recoverRoomStats();
			}

			Coins C=null;
			if((deadMoney>0)&&(myAmountOfDeadMoney>0)&&(body!=null)&&(bodyRoom!=null))
			for(int g=0;g<goldLooters.size();g++)
			{
				C=CMLib.beanCounter().makeBestCurrency(currency,myAmountOfDeadMoney,null,body);
				if(C!=null)
				{
					C.recoverPhyStats();
					bodyRoom.addItem(C,ItemPossessor.Expire.Monster_EQ);
					bodyRoom.recoverRoomStats();
					MOB mob=goldLooters.elementAt(g);
					if(mob.location()==bodyRoom)
					{
						if((mob.riding()!=null)&&(mob.riding() instanceof MOB))
							mob.tell("You'll need to dismount to get "+C.name()+" off the body.");
						else
						if((mob.riding()!=null)&&(mob.riding() instanceof Item))
							mob.tell("You'll need to disembark to get "+C.name()+" off the body.");
						else
						if(CMLib.flags().canBeSeenBy(body,mob))
							CMLib.commands().postGet(mob,body,C,false);
					}
				}
			}

			if((source != null)&&(source.getVictim()==target))
				source.setVictim(null);
			target.setVictim(null);
			if((body!=null)&&(bodyRoom!=null)&&(body.destroyAfterLooting()))
			{
				for(int i=bodyRoom.numItems()-1;i>=0;i--)
				{
					Item item=bodyRoom.getItem(i);
					if((item!=null)&&(item.container()==body))
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

	public String standardHitWord(int type, int damage)
	{
		if((type<0)||(type>=Weapon.TYPE_DESCS.length))
			type=Weapon.TYPE_BURSTING;
		final int[] thresholds=CMProps.getListFileIntList(CMProps.ListFile.DAMAGE_WORDS_THRESHOLDS);
		int damnCode=thresholds.length-2;
		for(int i=0;i<thresholds.length;i++)
			if(damage<=thresholds[i]){ damnCode=i; break;}
		damnCode++; // always add 1 because index into hitwords is type=0, annoy=1;
		final Object[][][] hitWords = CMProps.getListFileGrid(CMProps.ListFile.DAMAGE_WORDS);
		if(hitWords != hitWordsChanged)
		{
			hitWordsChanged=hitWords;
			hitWordIndex=null;
		}
		if(hitWordIndex==null)
		{
			Object[][][] newWordIndex=new Object[Weapon.TYPE_DESCS.length][][];
			for(int w=0;w<Weapon.TYPE_DESCS.length;w++)
			{
				Object[][] ALL=null;
				Object[][] MINE=null;
				for(int i=0;i<hitWords.length;i++)
				{
					if(((String)hitWords[i][0][0]).equalsIgnoreCase("ALL"))
						ALL=hitWords[i];
					else
					if(((String)hitWords[i][0][0]).equalsIgnoreCase(Weapon.TYPE_DESCS[w]))
					{ MINE=hitWords[i]; break;}
				}
				if(MINE!=null)
					newWordIndex[w]=MINE;
				else
					newWordIndex[w]=ALL;
			}
			hitWordIndex=newWordIndex;
		}
		Object[][] HIT_WORDS=hitWordIndex[type];
		if(damnCode<1) damnCode=1;
		if(damnCode>=HIT_WORDS.length) 
			damnCode=HIT_WORDS.length-1;
		return (String)CMLib.dice().pick(HIT_WORDS[damnCode]);
	}

	public String armorStr(MOB mob)
	{
		final int armor = -adjustedArmor(mob);
		int ARMOR_CEILING=CMProps.getListFileFirstInt(CMProps.ListFile.ARMOR_DESCS_CEILING);
		final int numArmorDescs = CMProps.getListFileSize(CMProps.ListFile.ARMOR_DESCS);
		return (armor<0)?CMProps.getListFileValue(CMProps.ListFile.ARMOR_DESCS,0):(
			   (armor>=ARMOR_CEILING)?
					   CMProps.getListFileValue(CMProps.ListFile.ARMOR_DESCS,numArmorDescs-1)
					   +(CMStrings.repeat("!",(armor-ARMOR_CEILING)/100))
					   +"^. ("+armor+")"
										:
					   (CMProps.getListFileValue(CMProps.ListFile.ARMOR_DESCS,(int)Math.round(Math.floor(CMath.mul(CMath.div(armor,ARMOR_CEILING),numArmorDescs))))
					   +"^. ("+armor+")"));
	}
	
	public String fightingProwessStr(MOB mob)
	{
		final int prowess = adjustedAttackBonus(mob,null) - ATTACK_ADJUSTMENT;
		final int PROWESS_CEILING=CMProps.getListFileFirstInt(CMProps.ListFile.PROWESS_DESCS_CEILING);
		final int numProwessDescs = CMProps.getListFileSize(CMProps.ListFile.PROWESS_DESCS);
		return (prowess<0)?CMProps.getListFileValue(CMProps.ListFile.PROWESS_DESCS,0):(
			   (prowess>=PROWESS_CEILING)
										 ?
								 CMProps.getListFileValue(CMProps.ListFile.PROWESS_DESCS,numProwessDescs-1)
								 +(CMStrings.repeat("!",(prowess-PROWESS_CEILING)/100))+"^. ("+prowess+")"
										 :
								 (CMProps.getListFileValue(CMProps.ListFile.PROWESS_DESCS,(int)Math.round(Math.floor(CMath.mul(CMath.div(prowess,PROWESS_CEILING),numProwessDescs))))
								 +"^. ("+prowess+")"));
	}

	protected int getWeaponAttackIndex(final int weaponType, final int weaponClassification)
	{
		switch(weaponClassification)
		{
		case Weapon.CLASS_RANGED: return (weaponType==Weapon.TYPE_LASERING) ? 5 : 0;
		case Weapon.CLASS_THROWN: return (weaponType==Weapon.TYPE_LASERING) ? 5 : 1;
		default:
			switch(weaponType)
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
	
	public String standardMissString(final int weaponType, final int weaponClassification, final String weaponName, final boolean useExtendedMissString)
	{
		final int listIndex = getWeaponAttackIndex(weaponType, weaponClassification);
		if(!useExtendedMissString) return CMProps.getListFileValue(CMProps.ListFile.MISS_DESCS,listIndex);
		return CMStrings.replaceAll(CMProps.getListFileValue(CMProps.ListFile.WEAPON_MISS_DESCS,listIndex),"<TOOLNAME>",weaponName)+CMLib.protocol().msp("missed.wav",20);
	}


	public String standardHitString(final int weaponType, final int weaponClass, final int damageAmount, final String weaponName)
	{
		final int listIndex;
		if((weaponName==null)||(weaponName.length()==0))
			listIndex = getWeaponAttackIndex(weaponType, Weapon.CLASS_NATURAL);
		else
			listIndex = getWeaponAttackIndex(weaponType, weaponClass);
		final StringBuilder str=new StringBuilder(CMStrings.replaceAll(CMProps.getListFileValue(CMProps.ListFile.WEAPON_HIT_DESCS,listIndex),"<TOOLNAME>",weaponName));
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

	public String standardMobCondition(MOB viewer,MOB mob)
	{
		int pct=(int)Math.round(Math.floor((CMath.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()))*10));
		if(pct<0) pct=0;
		final int numHealthDescs=CMProps.getListFileSize(CMProps.ListFile.HEALTH_CHART);
		if(pct>=numHealthDescs) pct=numHealthDescs-1;
		return CMStrings.replaceAll(CMProps.getListFileValue(CMProps.ListFile.HEALTH_CHART,pct),"<MOB>",mob.name(viewer));
	}

	public void resistanceMsgs(CMMsg msg, MOB source, MOB target)
	{
		if(msg.value()>0) return;

		if(target.amDead()) return;

		String tool=null;
		String endPart=" from <T-NAME>.";
		if(source==target)
		{
			source=null;
			endPart=".";
		}
		if(msg.tool()!=null)
		{
			if(msg.tool() instanceof Trap)
				endPart=".";
			else
			if(msg.tool() instanceof Ability)
				tool=((Ability)msg.tool()).name();
		}

		String tackOn=null;
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_MIND: tackOn="<S-NAME> shake(s) off the "+((tool==null)?"mental attack":tool)+endPart; break;
		case CMMsg.TYP_GAS: tackOn="<S-NAME> resist(s) the "+((tool==null)?"noxious fumes":tool)+endPart; break;
		case CMMsg.TYP_COLD: tackOn="<S-NAME> shake(s) off the "+((tool==null)?"cold blast":tool)+endPart;  break;
		case CMMsg.TYP_ELECTRIC: tackOn="<S-NAME> shake(s) off the "+((tool==null)?"electrical attack":tool)+endPart; break;
		case CMMsg.TYP_FIRE: tackOn="<S-NAME> resist(s) the "+((tool==null)?"blast of heat":tool)+endPart; break;
		case CMMsg.TYP_WATER: tackOn="<S-NAME> dodge(s) the "+((tool==null)?"wet blast":tool)+endPart;  break;
		case CMMsg.TYP_UNDEAD:  tackOn="<S-NAME> shake(s) off the "+((tool==null)?"evil attack":tool)+endPart; break;
		case CMMsg.TYP_POISON:  tackOn="<S-NAME> shake(s) off the "+((tool==null)?"poison":tool)+endPart; break;
		case CMMsg.TYP_DISEASE: tackOn="<S-NAME> resist(s) the "+((tool==null)?"disease":tool); break;
		case CMMsg.TYP_JUSTICE:break;
		case CMMsg.TYP_CAST_SPELL:  tackOn="<S-NAME> resist(s) the "+((tool==null)?"magical attack":tool)+endPart; break;
		case CMMsg.TYP_PARALYZE: tackOn="<S-NAME> resist(s) the "+((tool==null)?"paralysis":tool)+endPart; break;
		case CMMsg.TYP_SONIC:  tackOn="<S-NAME> shake(s) off the "+((tool==null)?"sonic":tool)+endPart; break;
		case CMMsg.TYP_LASER:  tackOn="<S-NAME> dodge(s) the "+((tool==null)?"laser":tool)+endPart; break;
		}
		if(tackOn!=null)
		{
			String newStr=target+"/"+source+"/"+tool;
			if(!newStr.equals(lastStr)||((System.currentTimeMillis()-lastRes)>250))
				msg.addTrailerMsg(CMClass.getMsg(target,source,CMMsg.MSG_OK_ACTION,tackOn));
			lastStr=newStr;
			lastRes=System.currentTimeMillis();
		}
		msg.setValue(msg.value()+1);
	}

	public void handleBeingHealed(CMMsg msg)
	{
		if(!(msg.target() instanceof MOB)) return;
		MOB target=(MOB)msg.target();
		int amt=msg.value();
		if(amt>0) target.curState().adjHitPoints(amt,target.maxState());
	}

	protected boolean bleedableWeapon(Environmental E)
	{
		if(E==null) return false;
		if(E instanceof Weapon)
		{
			return true;
		}
		else
		if(E instanceof Ability)
		{
			int code=((Ability)E).classificationCode()&Ability.ALL_ACODES;
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

	public void handleBeingDamaged(CMMsg msg)
	{
		if(!(msg.target() instanceof MOB)) return;
		MOB attacker=msg.source();
		MOB target=(MOB)msg.target();
		int dmg=msg.value();
		if(Log.combatChannelOn())
		{
			Item DI=target.fetchWieldedItem();
			Item KI=attacker.fetchWieldedItem();
			String tool=(msg.tool()==null)?"null":msg.tool().name();
			String type=(msg.sourceMinor()==CMMsg.NO_EFFECT)?"??":CMMsg.TYPE_DESCS[msg.sourceMinor()];
			Log.combatOut("DAMG",attacker.Name()+":"+attacker.phyStats().getCombatStats()+":"+attacker.curState().getCombatStats()+":"+((KI==null)?"null":KI.name())+":"+target.Name()+":"+target.phyStats().getCombatStats()+":"+target.curState().getCombatStats()+":"+((DI==null)?"null":DI.name())+":"+tool+":"+type+":"+dmg);
		}
		synchronized(("DMG"+target.Name().toUpperCase()).intern())
		{
			if((dmg>0)&&(target.curState().getHitPoints()>0))
			{
				if((!target.curState().adjHitPoints(-dmg,target.maxState()))
				&&(target.curState().getHitPoints()<1)
				&&(target.location()!=null))
					postDeath(attacker,target,msg);
				else
				{
					if((Math.round(CMath.div(dmg,target.maxState().getHitPoints())*100.0)>=CMProps.getIntVar(CMProps.Int.INJBLEEDPCTHP))
					&&bleedableWeapon(msg.tool()))
					{
						Ability A2=CMClass.getAbility("Bleeding");
						if(A2!=null) A2.invoke((target),(target),true,0);
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
						if(A!=null) A.invoke(target,new XVector<Object>(msg),target,true,0);
					}
				}
			}
		}
	}

	public void handleDeath(CMMsg msg)
	{
		final MOB deadmob=msg.source();
		if(!deadmob.amDead())
		{
			if((!deadmob.isMonster())&&(deadmob.soulMate()==null))
			{
				CMLib.coffeeTables().bump(deadmob,CoffeeTableRow.STAT_DEATHS);
				final PlayerStats playerStats=deadmob.playerStats();
				if(playerStats!=null)
					playerStats.setHygiene(0);
				List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.DETAILEDDEATHS);
				List<String> channels2=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.DEATHS);
				if(!CMLib.flags().isCloaked(deadmob))
				for(int i=0;i<channels.size();i++)
				{
					if((msg.tool()!=null)&&(msg.tool() instanceof MOB))
						CMLib.commands().postChannel(channels.get(i),deadmob.clans(),deadmob.Name()+" was just killed in "+CMLib.map().getExtendedRoomID(deadmob.location())+" by "+msg.tool().Name()+".",true);
					else
						CMLib.commands().postChannel(channels.get(i),deadmob.clans(),deadmob.Name()+" has just died at "+CMLib.map().getExtendedRoomID(deadmob.location()),true);
				}
				if(!CMLib.flags().isCloaked(deadmob))
				{
					for(int i=0;i<channels2.size();i++)
						if((msg.tool()!=null)&&(msg.tool() instanceof MOB))
							CMLib.commands().postChannel(channels2.get(i),deadmob.clans(),deadmob.Name()+" was just killed.",true);
				}
			}
			if(msg.tool() instanceof MOB)
			{
				MOB killer=(MOB)msg.tool();
				doDeathPostProcessing(msg);
				justDie(killer,deadmob);
			}
			else
				justDie(null,deadmob);
			deadmob.tell(deadmob,msg.target(),msg.tool(),msg.sourceMessage());
			if(deadmob.riding()!=null) 
				deadmob.riding().delRider(deadmob);
			if(CMLib.flags().isCataloged(deadmob))
				CMLib.catalog().bumpDeathPickup(deadmob);
		}
	}
	
	public void doDeathPostProcessing(CMMsg msg)
	{
		final MOB deadmob=msg.source();
		if(msg.tool() instanceof MOB)
		{
			MOB killer=(MOB)msg.tool();
			if(Log.killsChannelOn())
			{
				Item KI=killer.fetchWieldedItem();
				Item DI=deadmob.fetchWieldedItem();
				String room=CMLib.map().getExtendedRoomID((killer.location()!=null)?killer.location():deadmob.location());
				Log.killsOut("KILL",room+":"+killer.Name()+":"+killer.phyStats().getCombatStats()+":"+killer.curState().getCombatStats()+":"+((KI==null)?"null":KI.name())+":"+deadmob.Name()+":"+deadmob.phyStats().getCombatStats()+":"+deadmob.curState().getCombatStats()+":"+((DI==null)?"null":DI.name()));
			}
			if(Log.combatChannelOn())
			{
				Item DI=deadmob.fetchWieldedItem();
				Item KI=killer.fetchWieldedItem();
				Log.combatOut("KILL",killer.Name()+":"+killer.phyStats().getCombatStats()+":"+killer.curState().getCombatStats()+":"+((KI==null)?"null":KI.name())+":"+deadmob.Name()+":"+deadmob.phyStats().getCombatStats()+":"+deadmob.curState().getCombatStats()+":"+((DI==null)?"null":DI.name()));
			}
			if((deadmob!=null)&&(killer!=null)
			&&(deadmob.soulMate()==null)
			&&(killer!=deadmob)&&(!killer.isMonster()))
			{
				if(!deadmob.isMonster())
				{
					CMLib.coffeeTables().bump(deadmob,CoffeeTableRow.STAT_PKDEATHS);
					if(killer.playerStats()!=null)
						CMLib.players().bumpPrideStat(killer,PrideStat.PVPKILLS, 1);
				}
				if((killer.session()!=null)
				&&((deadmob.session()==null)||(!deadmob.session().getAddress().equalsIgnoreCase(killer.session().getAddress()))))
				{
					List<Pair<Clan,Integer>> list = CMLib.clans().findRivalrousClans(killer, deadmob);
					for(Pair<Clan,Integer> c : list)
						c.first.recordClanKill(killer,deadmob);
				}
			}
		}
	}
	
	protected void pickNextVictim(MOB observer, MOB deadmob, Set<MOB> deadGroupH)
	{
		Room R=observer.location();
		if(R!=null)
		{
			MOB newTargetM=null;
			Set<MOB> myGroupH=observer.getGroupMembers(new HashSet<MOB>());
			for(int r=0;r<R.numInhabitants();r++)
			{
				MOB M=R.fetchInhabitant(r);
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
				MOB M=R.fetchInhabitant(r);
				if(M==null) continue;
				MOB vic=M.getVictim();
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
			observer.setVictim(null);
			if(newTargetM!=null)
				observer.setVictim(newTargetM);
		}
	}

	public void handleObserveDeath(MOB observer, MOB fighting, CMMsg msg)
	{
		// no longer does a damn thing
	}

	public void handleBeingAssaulted(CMMsg msg)
	{
		if(!(msg.target() instanceof MOB)) return;
		MOB attacker=msg.source();
		MOB target=(MOB)msg.target();

		if((!target.isInCombat())
		&&(target.location()!=null)
		&&(target.location().isInhabitant(attacker))
		&&((!CMath.bset(msg.sourceMajor(),CMMsg.MASK_ALWAYS))
			||(!(msg.tool() instanceof DiseaseAffect))))
		{
			establishRange(target,attacker,msg.tool());
			target.setVictim(attacker);
		}
		if(target.isInCombat())
		{
			if(attacker.session()!=null)
			{
				if(!target.isMonster())
					attacker.session().setLastPKFight();
				else
					attacker.session().setLastNPCFight();
			}
			if(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
			{
				Item weapon=attacker.myNaturalWeapon();
				if((msg.tool()!=null)&&(msg.tool() instanceof Item))
					weapon=(Item)msg.tool();
				if(weapon!=null)
				{
					boolean isHit=rollToHit(attacker,target);
					postWeaponDamage(attacker,target,weapon,isHit);
					if(isHit) msg.setValue(1);
				}
				if((target.soulMate()==null)&&(target.playerStats()!=null)&&(target.location()!=null))
					target.playerStats().adjHygiene(PlayerStats.HYGIENE_FIGHTDIRTY);

				if((attacker.isMonster())&&(!attacker.isInCombat()))
					attacker.setVictim(target);
			}
			else
			if((msg.tool()!=null)
			&&(msg.tool() instanceof Item))
				postWeaponDamage(attacker,target,(Item)msg.tool(),true);
		}
		if(CMLib.flags().isSitting(target)||CMLib.flags().isSleeping(target))
			CMLib.commands().postStand(target,true);
	}

	public void makeFollowersFight(MOB observer, MOB target, MOB source)
	{
		if((source==null)||(target==null)||observer==null) return;
		if(source==target) return;
		if((target==observer)||(source==observer)) return;
		if((target.location()!=observer.location())||(target.location()!=source.location()))
			return;
		if((CMath.bset(observer.getBitmap(),MOB.ATT_AUTOASSIST))) return;
		if(observer.isInCombat()) return;
		MOB observerFollows=observer.amFollowing();
		MOB targetFollows=target.amFollowing();
		MOB sourceFollows=source.amFollowing();

		if((observerFollows==target)
		||(targetFollows==observer)
		||((targetFollows!=null)&&(targetFollows==observerFollows)))
		{
			observer.setVictim(source);
			establishRange(observer,source,observer.fetchWieldedItem());
		}
		else
		if((observerFollows==source)
		||(sourceFollows==observer)
		||((sourceFollows!=null)&&(sourceFollows==observerFollows)))
		{
			observer.setVictim(target);
			establishRange(observer,target,observer.fetchWieldedItem());
		}
	}

	public List<MOB> getAllInProximity(MOB to, int distance)
	{
		Room R=to.location();
		Vector<MOB> V=new Vector<MOB>();
		V.addElement(to);
		if(R==null) return V;
		Vector<MOB> everyV=new Vector<MOB>();
		for(int i=0;i<R.numInhabitants();i++)
			everyV.addElement(R.fetchInhabitant(i));
		if(!everyV.contains(to)) everyV.addElement(to);
		int[][] map=new int[everyV.size()][everyV.size()];
		for(int x=0;x<map.length;x++)
			for(int y=0;y<map.length;y++)
				map[x][y]=-1;

		return V;
	}

	public void establishRange(MOB source, MOB target, Environmental tool)
	{
		// establish and enforce range
		if((source.rangeToTarget()<0))
		{
			if(source.riding()!=null)
			{
				if((target==source.riding())||(source.riding().amRiding(target)))
					source.setAtRange(0);
				else
				if((source.riding() instanceof MOB)
				   &&(((MOB)source.riding()).isInCombat())
				   &&(((MOB)source.riding()).getVictim()==target)
				   &&(((MOB)source.riding()).rangeToTarget()>=0)
				   &&(((MOB)source.riding()).rangeToTarget()<source.rangeToTarget()))
				{
					source.setAtRange(((MOB)source.riding()).rangeToTarget());
					source.recoverPhyStats();
					return;
				}
				else
				for(int r=0;r<source.riding().numRiders();r++)
				{
					Rider rider=source.riding().fetchRider(r);
					if(!(rider instanceof MOB)) continue;
					MOB otherMOB=(MOB)rider;
					if((otherMOB!=source)
					   &&(otherMOB.isInCombat())
					   &&(otherMOB.getVictim()==target)
					   &&(otherMOB.rangeToTarget()>=0)
					   &&(otherMOB.rangeToTarget()<source.rangeToTarget()))
					{
						source.setAtRange(otherMOB.rangeToTarget());
						source.recoverPhyStats();
						return;
					}
				}
			}

			MOB follow=source.amFollowing();
			if((target.getVictim()==source)&&(target.rangeToTarget()>=0))
				source.setAtRange(target.rangeToTarget());
			else
			if((follow!=null)&&(follow.location()==source.location()))
			{
				int newRange=follow.fetchFollowerOrder(source);
				if(newRange<0)
				{
					if(follow.rangeToTarget()>=0)
					{
						newRange=follow.rangeToTarget();
						if(newRange<source.maxRange(tool))
							newRange=source.maxRange(tool);
					}
					else
						newRange=source.maxRange(tool);
				}
				else
				{
					if(follow.rangeToTarget()>=0)
						newRange=newRange+follow.rangeToTarget();
				}
				if((source.location()!=null)&&(source.location().maxRange()<newRange))
					newRange=source.location().maxRange();
				source.setAtRange(newRange);
			}
			else
				source.setAtRange(source.maxRange(tool));
			source.recoverPhyStats();
		}
	}

	protected void subtickAttack(MOB fighter, Item weapon, int folrange)
	{
		if((weapon!=null)&&(weapon.amWearingAt(Wearable.IN_INVENTORY)))
			weapon=fighter.fetchWieldedItem();
		if((!CMath.bset(fighter.getBitmap(),MOB.ATT_AUTOMELEE)))
			postAttack(fighter,fighter.getVictim(),weapon);
		else
		{
			boolean inminrange=(fighter.rangeToTarget()>=fighter.minRange(weapon));
			boolean inmaxrange=(fighter.rangeToTarget()<=fighter.maxRange(weapon));
			if((folrange>=0)&&(fighter.rangeToTarget()>=0)&&(folrange!=fighter.rangeToTarget()))
			{
				if(fighter.rangeToTarget()<folrange)
					inminrange=false;
				else
				if(fighter.rangeToTarget()>folrange)
				{
					// these settings are ONLY to ensure that neither of the
					// next two conditions evaluate to true.
					inminrange=true;
					inmaxrange=false;
					// we advance
					CMMsg msg=CMClass.getMsg(fighter,fighter.getVictim(),CMMsg.MSG_ADVANCE,"<S-NAME> advances(s) at <T-NAMESELF>.");
					if(fighter.location().okMessage(fighter,msg))
					{
						fighter.location().send(fighter,msg);
						fighter.setAtRange(fighter.rangeToTarget()-1);
						if((fighter.getVictim()!=null)&&(fighter.getVictim().getVictim()==fighter))
						{
							fighter.getVictim().setAtRange(fighter.rangeToTarget());
							fighter.getVictim().recoverPhyStats();
						}
					}
				}
			}

			if((!inminrange)&&(fighter.curState().getMovement()>=25))
			{
				CMMsg msg=CMClass.getMsg(fighter,fighter.getVictim(),CMMsg.MSG_RETREAT,"<S-NAME> retreat(s) before <T-NAME>.");
				if(fighter.location().okMessage(fighter,msg))
					fighter.location().send(fighter,msg);
			}
			else
			if(inminrange&&inmaxrange&&((weapon!=null)||(fighter.rangeToTarget()==0)))
				postAttack(fighter,fighter.getVictim(),weapon);
		}
	}

	protected void subtickBeforeAttack(final MOB fighter, final int combatSystem)
	{
		// combat que system eats up standard commands
		// before using any attacks
		while(((combatSystem==CombatLibrary.COMBAT_QUEUE)||(combatSystem==CombatLibrary.COMBAT_TURNBASED))
		&&(!fighter.amDead())
		&&(fighter.dequeCommand()))
			{}
	}
	protected void subtickAfterAttack(MOB fighter)
	{
		// this code is for auto-retargeting of players
		// is mostly not handled by combatabilities in a smarter way
		MOB target=fighter.getVictim();
		if((target!=null)
		&&(fighter.isMonster())
		&&(target.isMonster())
		&&(CMLib.dice().rollPercentage()==1)
		&&((fighter.amFollowing()==null)||(fighter.amFollowing().isMonster()))
		&&(!target.amDead())
		&&(fighter.location()!=null))
		{
			MOB M=null;
			Room R=fighter.location();
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

	public void dispenseExperience(Set<MOB> killers, Set<MOB> dividers, MOB killed)
	{
		int totalLevels=0;
		int expAmount=100;
		int expAddition=25;

		for(Iterator<MOB> i=dividers.iterator();i.hasNext();)
		{
			MOB mob=i.next();
			totalLevels += (mob.phyStats().level()*mob.phyStats().level());
			expAmount += expAddition;
			expAddition -= expAddition/4;
		}
		for(Iterator<MOB> i=killers.iterator();i.hasNext();)
		{
			MOB mob=i.next();
			int myAmount=(int)Math.round(CMath.mul(expAmount,CMath.div(mob.phyStats().level()*mob.phyStats().level(),totalLevels)));
			if(myAmount>100) myAmount=100;
			CMLib.leveler().postExperience(mob,killed,"",myAmount,false);
		}
	}

	public void tickCombat(MOB fighter)
	{
		Item weapon=fighter.fetchWieldedItem();

		if((CMath.bset(fighter.getBitmap(),MOB.ATT_AUTODRAW))&&(weapon==null))
		{
			CMLib.commands().postDraw(fighter,false,true);
			weapon=fighter.fetchWieldedItem();
		}

		final int combatSystem=CMProps.getIntVar(CMProps.Int.COMBATSYSTEM);
		
		subtickBeforeAttack(fighter, combatSystem);

		int folrange=(CMath.bset(fighter.getBitmap(),MOB.ATT_AUTOMELEE)
						&&(fighter.amFollowing()!=null)
						&&(fighter.amFollowing().getVictim()==fighter.getVictim())
						&&(fighter.amFollowing().rangeToTarget()>=0)
						&&(fighter.amFollowing().fetchFollowerOrder(fighter)>=0))?
							fighter.amFollowing().fetchFollowerOrder(fighter)+fighter.amFollowing().rangeToTarget():-1;
		if(CMLib.flags().aliveAwakeMobile(fighter,true))
		{
			if(((combatSystem!=CombatLibrary.COMBAT_MANUAL)&&(combatSystem!=CombatLibrary.COMBAT_TURNBASED))
			||(fighter.isMonster()))
			{
				final int saveAction=(combatSystem!=CombatLibrary.COMBAT_DEFAULT)?0:1;
				int numAttacks=(int)Math.round(Math.floor(fighter.actions()))-saveAction;
				if((combatSystem==CombatLibrary.COMBAT_DEFAULT)
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

	public boolean isKnockedOutUponDeath(MOB mob, MOB fighting)
	{
		String whatToDo=null;
		if(((mob.isMonster())||(mob.soulMate()!=null)))
			whatToDo=CMProps.getVar(CMProps.Str.MOBDEATH).toUpperCase();
		else
			whatToDo=CMProps.getVar(CMProps.Str.PLAYERDEATH).toUpperCase();
		List<String> whatsToDo=CMParms.parseCommas(whatToDo,true);
		double[] fakeVarVals={1.0,1.0,1.0};
		for(int w=0;w<whatsToDo.size();w++)
		{
			whatToDo=whatsToDo.get(w);
			if(whatToDo.startsWith("OUT ")&&(CMath.isMathExpression(whatToDo.substring(4).trim(),fakeVarVals)))
				return true;
		}
		return false;
	}

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
				int move=-room.pointsPerMove(mob);
				if(mob.phyStats().weight()>mob.maxCarry())
					move+=(int)Math.round(CMath.mul(move,10.0*CMath.div(mob.phyStats().weight()-mob.maxCarry(),mob.maxCarry())));
				curState.adjMovement(move,maxState);
			}
			if((!CMLib.flags().isSleeping(mob))
			&&(!CMSecurity.isAllowed(mob,room,CMSecurity.SecFlag.IMMORT)))
			{
				int factor=mob.baseWeight()/500;
				if(factor<1) factor=1;
				if((!CMSecurity.isDisabled(CMSecurity.DisFlag.THIRST))
				&&(mob.maxState().getThirst() < (Integer.MAX_VALUE/2)))
					curState.adjThirst(-(room.thirstPerRound(mob)*factor),maxState.maxThirst(mob.baseWeight()));
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
						mob.tell("YOU ARE DYING OF THIRST!");
					if(hungry)
						mob.tell("YOU ARE DYING OF HUNGER!");
					CMLib.combat().postDeath(null,mob,null);
				}
				else
				if(ticksThirsty>CharState.DEATH_THIRST_TICKS-30)
					mob.tell("You are dehydrated, and near death.  DRINK SOMETHING!");
				else
				if(ticksHungry>CharState.DEATH_HUNGER_TICKS-30)
					mob.tell("You are starved, and near death.  EAT SOMETHING!");
				else
				{
					if(thirsty && ((ticksThirsty-1 % CharState.ANNOYANCE_DEFAULT_TICKS)==0))
					{
						if(ticksThirsty>((CharState.DEATH_THIRST_TICKS/2)+(CharState.DEATH_THIRST_TICKS/4)))
							mob.tell("You are dehydrated! Drink something!");
						else
						if(ticksThirsty>(CharState.DEATH_THIRST_TICKS/2))
							mob.tell("You are parched! Drink something!");
						else
							mob.tell("You are thirsty.");
					}
					if((hungry) && ((ticksHungry-1 % CharState.ANNOYANCE_DEFAULT_TICKS)==0))
					{
						if(ticksHungry>((CharState.DEATH_HUNGER_TICKS/2)+(CharState.DEATH_HUNGER_TICKS/4)))
							mob.tell("You are starved! Eat something!");
						else
						if(ticksHungry>(CharState.DEATH_HUNGER_TICKS/2))
							mob.tell("You are famished! Eat something!");
						else
							mob.tell("You are hungry.");
					}
				}
			}
		}
	}
	
	public boolean handleConsequences(MOB mob, MOB fighting, String[] commands, int[] lostExperience, String message)
	{
		if((commands==null)||(commands.length==0)) return false;
		if(lostExperience==null) lostExperience=new int[1];
		int baseExperience=lostExperience[0];
		lostExperience[0]=0;
		int rejuv=mob.phyStats().rejuv();
		if((rejuv==0)||(rejuv==Integer.MAX_VALUE)) rejuv=mob.phyStats().level();
		if(((!mob.isMonster())&&(mob.soulMate()==null))) rejuv=1;
		double[] varVals={
				mob.basePhyStats().level()>mob.phyStats().level()?mob.basePhyStats().level():mob.phyStats().level(),
				(fighting!=null)?fighting.phyStats().level():0,
				rejuv
		};
		for(int w=0;w<commands.length;w++)
		{
			String whatToDo=commands[w].toUpperCase();
			if(whatToDo.startsWith("UNL"))
			{
				Vector<String> V=CMParms.parse(whatToDo);
				int times=1;
				if((V.size()>1)&&(CMath.s_int(V.lastElement())>1))
					times=CMath.s_int(V.lastElement());
				for(int t=0;t<times;t++)
					CMLib.leveler().unLevel(mob);
			}
			else
			if(whatToDo.startsWith("RECALL"))
				mob.killMeDead(false);
			else
			if(whatToDo.startsWith("ASTR"))
			{
				Ability A=CMClass.getAbility("Prop_AstralSpirit");
				if((A!=null)&&(mob.fetchAbility(A.ID())==null))
				{
					mob.tell("^HYou are now a spirit.^N");
					if(whatToDo.startsWith("ASTRAL_R"))
					{
						A.setMiscText("SELF-RES");
						mob.tell("^HFind your corpse and use ENTER [body name] to re-enter your body.^N");
					}
					else
						mob.tell("^HFind your corpse have someone resurrect it.^N");
					mob.addAbility(A);
					A.autoInvocation(mob);
				}
			}
			else
			if(whatToDo.startsWith("OUT ")&&(CMath.isMathExpression(whatToDo.substring(4).trim(),varVals)))
			{
				Ability A=CMClass.getAbility("Skill_ArrestingSap");
				int tickDown=CMath.s_parseIntExpression(whatToDo.substring(4).trim(),varVals);
				if((A!=null)&&(tickDown>0))
				{
					A.invoke(mob,new XVector<Object>(""+tickDown,"SAFELY"),mob,true,0);
					mob.resetToMaxState();
				}
			}
			else
			if(whatToDo.startsWith("PUR"))
			{
				MOB deadMOB=CMLib.players().getLoadPlayer(mob.Name());
				if(deadMOB!=null)
				{
					CMLib.players().obliteratePlayer(deadMOB,true,false);
					return false;
				}
			}
			else
			if(whatToDo.startsWith("LOSESK"))
			{
				if(mob.numAbilities()>0)
				{
					Ability A=mob.fetchAbility(CMLib.dice().roll(1,mob.numAbilities(),-1));
					if(A!=null)
					{
						mob.tell("You've forgotten "+A.Name()+".");
						mob.delAbility(A);
						if(A.isAutoInvoked())
						{
							Ability A2=mob.fetchEffect(A.ID());
							A2.unInvoke();
							mob.delEffect(A2);
						}
					}
				}
			}
			else
			if(CMath.isMathExpression(whatToDo,varVals))
			{
				lostExperience[0]=CMath.s_parseIntExpression(whatToDo,varVals);
				if(lostExperience[0]>0)
				{
					message=CMStrings.replaceAll(message,"@x1",""+lostExperience[0]);
					mob.tell(message);
					CMLib.leveler().postExperience(mob,null,null,-lostExperience[0],false);
				}
			}
			else
			if(whatToDo.startsWith("EXPER"))
			{
				lostExperience[0]=baseExperience;
				if(lostExperience[0]>0)
				{
					message=CMStrings.replaceAll(message,"@x1",""+baseExperience);
					mob.tell(message);
					CMLib.leveler().postExperience(mob,null,null,-baseExperience,false);
				}
			}
		}
		return true;
	}
}
