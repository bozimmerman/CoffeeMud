package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.Fighter;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

import java.util.*;

/*
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class MUDFight extends StdLibrary implements CombatLibrary
{
    public String ID(){return "MUDFight";}
    public String lastStr="";
    public long lastRes=0;
    public String[][] hitWordIndex=null;
    public String[][] hitWordsChanged=null;
    protected LinkedList<CMath.CompiledOperation> attackAdjustmentFormula = null;
    protected LinkedList<CMath.CompiledOperation>  armorAdjustmentFormula = null;
    protected LinkedList<CMath.CompiledOperation>  attackerFudgeBonusFormula  = null;
    protected LinkedList<CMath.CompiledOperation>  spellFudgeDamageFormula  = null;
    protected LinkedList<CMath.CompiledOperation>  spellCritChanceFormula = null;
    protected LinkedList<CMath.CompiledOperation>  spellCritDmgFormula = null;
    protected LinkedList<CMath.CompiledOperation> targetedRangedDamageFormula = null;
    protected LinkedList<CMath.CompiledOperation> rangedFudgeDamageFormula  = null;
    protected LinkedList<CMath.CompiledOperation> targetedMeleeDamageFormula = null;
    protected LinkedList<CMath.CompiledOperation> meleeFudgeDamageFormula  = null;
    protected LinkedList<CMath.CompiledOperation> staticRangedDamageFormula = null;
    protected LinkedList<CMath.CompiledOperation> staticMeleeDamageFormula = null;
    protected LinkedList<CMath.CompiledOperation>  weaponCritChanceFormula = null;
    protected LinkedList<CMath.CompiledOperation>  weaponCritDmgFormula = null;
    
    private static final int ATTACK_ADJUSTMENT = 50;

    public boolean activate()
    {
		attackAdjustmentFormula = CMath.compileMathExpression(CMProps.getVar(CMProps.SYSTEM_FORMULA_ATTACKADJUSTMENT));
		armorAdjustmentFormula= CMath.compileMathExpression(CMProps.getVar(CMProps.SYSTEM_FORMULA_ARMORADJUSTMENT));
	    attackerFudgeBonusFormula = CMath.compileMathExpression(CMProps.getVar(CMProps.SYSTEM_FORMULA_ATTACKFUDGEBONUS));
		spellCritChanceFormula = CMath.compileMathExpression(CMProps.getVar(CMProps.SYSTEM_FORMULA_CHANCESPELLCRIT));
		spellCritDmgFormula = CMath.compileMathExpression(CMProps.getVar(CMProps.SYSTEM_FORMULA_DAMAGESPELLCRIT));
		targetedRangedDamageFormula=CMath.compileMathExpression(CMProps.getVar(CMProps.SYSTEM_FORMULA_DAMAGERANGEDTARGETED));
		targetedMeleeDamageFormula=CMath.compileMathExpression(CMProps.getVar(CMProps.SYSTEM_FORMULA_DAMAGEMELEETARGETED));
		staticRangedDamageFormula=CMath.compileMathExpression(CMProps.getVar(CMProps.SYSTEM_FORMULA_DAMAGERANGEDSTATIC));
		staticMeleeDamageFormula=CMath.compileMathExpression(CMProps.getVar(CMProps.SYSTEM_FORMULA_DAMAGEMELEESTATIC));
		weaponCritChanceFormula = CMath.compileMathExpression(CMProps.getVar(CMProps.SYSTEM_FORMULA_CHANCEWEAPONCRIT));
		weaponCritDmgFormula = CMath.compileMathExpression(CMProps.getVar(CMProps.SYSTEM_FORMULA_DAMAGEWEAPONCRIT));
		spellFudgeDamageFormula = CMath.compileMathExpression(CMProps.getVar(CMProps.SYSTEM_FORMULA_DAMAGESPELLFUDGE));
		meleeFudgeDamageFormula = CMath.compileMathExpression(CMProps.getVar(CMProps.SYSTEM_FORMULA_DAMAGEMELEEFUDGE));
		rangedFudgeDamageFormula = CMath.compileMathExpression(CMProps.getVar(CMProps.SYSTEM_FORMULA_DAMAGERANGEDFUDGE));
    	return true; 
    }
    
    public void propertiesLoaded() { activate(); }
    
	public HashSet allPossibleCombatants(MOB mob, boolean beRuthless)
	{
		HashSet h=new HashSet();
		Room thisRoom=mob.location();
		if(thisRoom==null) return null;
        HashSet h1=mob.getGroupMembers(new HashSet());
		for(int m=0;m<thisRoom.numInhabitants();m++)
		{
			MOB inhab=thisRoom.fetchInhabitant(m);
			if((inhab!=null)
			&&(inhab!=mob)
			&&(!h1.contains(inhab))
			&&(CMLib.flags().isSeen(inhab)||CMLib.flags().canMove(inhab))
			&&(CMLib.flags().isSeen(inhab)||(!CMLib.flags().isCloaked(inhab)))
			&&((beRuthless)||(!mob.isMonster())||(!inhab.isMonster())))
				h.add(inhab);
		}
		return h;
	}

	public HashSet properTargets(Ability A, MOB caster, boolean beRuthless)
	{
		HashSet h=null;
		if(A.abstractQuality()!=Ability.QUALITY_MALICIOUS)
		{
            if(caster.Name().equalsIgnoreCase("somebody"))
    			h=new HashSet();
            else
                h=caster.getGroupMembers(new HashSet());
			for(Iterator e=((HashSet)h.clone()).iterator();e.hasNext();)
			{
				MOB M=(MOB)e.next();
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
		double[] vars = {mob.envStats().attackAdjustment(),
						 currStr,
						 baseStr,
						 strBonus,
						 (mob.curState().getHunger()<1)?1.0:0.0,
						 (mob.curState().getThirst()<1)?1.0:0.0,
						 (mob.curState().getFatigue()>CharState.FATIGUED_MILLIS)?1.0:0.0
						};
		return (int)Math.round(CMath.parseMathExpression(attackAdjustmentFormula, vars, 0.0));
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
		double baseDex=(double)mob.baseCharStats().getStat(CharStats.STAT_DEXTERITY);
		if(baseDex > maxDex) baseDex = maxDex;
		
		double[] vars = {
				mob.envStats().armor(),
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
        	attacker.envStats().level(),
        	defender.envStats().level(),
        	attacker.envStats().level() > defender.envStats().level() ? 1 : -1
        };
        int attackerFudgeBonusAmt = (int)Math.round(CMath.parseMathExpression(attackerFudgeBonusFormula, vars, 0.0));
        return rollToHit(adjustedAttackBonus(attacker,defender),adjustedArmor(defender),attackerFudgeBonusAmt);
    }

    public boolean rollToHit(int attack, int defence, int adjustment)
    {
        double myArmor= -((double)defence);
        if(myArmor==0) myArmor=1.0;
        else
        if(myArmor<0.0) myArmor=-CMath.div(1.0,myArmor);
        double hisAttack=(double)attack;
        if(hisAttack==0.0) hisAttack=1.0;
        else
        if(hisAttack<0.0) hisAttack=-CMath.div(1.0,myArmor);
        return CMLib.dice().normalizeAndRollLess((int)Math.round(50.0*(hisAttack/myArmor)) + adjustment);
    }
    
	public HashSet allCombatants(MOB mob)
	{
		HashSet h=new HashSet();
		Room thisRoom=mob.location();
		if(thisRoom==null) return null;
		if(!mob.isInCombat()) return null;

        HashSet h1=null;
        if(mob.Name().equalsIgnoreCase("nobody"))
            h1=new HashSet();
        else
            h1=mob.getGroupMembers(new HashSet());
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
			 	h.add(inhab);
		}
		return h;

	}

	public void makePeaceInGroup(MOB mob)
	{
		HashSet myGroup=mob.getGroupMembers(new HashSet());
		for(Iterator e=myGroup.iterator();e.hasNext();)
		{
			MOB mob2=(MOB)e.next();
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
		for(int i=0;i<addHere.trailerMsgs().size();i++)
		{
			CMMsg msg=(CMMsg)addHere.trailerMsgs().elementAt(i);
			if((msg.source()==mob)
			&&((msg.sourceMinor()==CMMsg.TYP_PANIC))
			   ||(msg.sourceMinor()==CMMsg.TYP_DEATH))
				return;
		}
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
		for(int i=0;i<addHere.trailerMsgs().size();i++)
		{
			CMMsg msg=(CMMsg)addHere.trailerMsgs().elementAt(i);
			if((msg.source()==deadM)
			&&((msg.sourceMinor()==CMMsg.TYP_PANIC))
			   ||(msg.sourceMinor()==CMMsg.TYP_DEATH))
				return;
		}

		String msp=CMProps.msp("death"+CMLib.dice().roll(1,7,0)+".wav",50);
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
		CMMsg msg2=CMClass.getMsg(deadM,null,killerM,
			CMMsg.MSG_DEATH,null,
			CMMsg.MSG_DEATH,null,
			CMMsg.MSG_DEATH,null);
		CMLib.map().sendGlobalMessage(deadM,CMMsg.TYP_DEATH, msg2);
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

	public String replaceDamageTag(String str, int damage, int damageType)
	{
		if(str==null) return null;
		int replace=str.indexOf("<DAMAGE>");
		if(replace>=0)
		{
			if(!CMProps.getVar(CMProps.SYSTEM_SHOWDAMAGE).equalsIgnoreCase("YES"))
				return str.substring(0,replace)+standardHitWord(damageType,damage)+str.substring(replace+8);
			return str.substring(0,replace)+standardHitWord(damageType,damage)+" ("+damage+")"+ str.substring(replace+8);
		}
        replace=str.indexOf("<DAMAGES>");
        if(replace>=0)
        {
            String hitWord=standardHitWord(damageType,damage);
            hitWord=CMStrings.replaceAll(hitWord,"(","");
            hitWord=CMStrings.replaceAll(hitWord,")","");
            if(!CMProps.getVar(CMProps.SYSTEM_SHOWDAMAGE).equalsIgnoreCase("YES"))
                return str.substring(0,replace)+hitWord+str.substring(replace+9);
            return str.substring(0,replace)+hitWord+" ("+damage+")"+ str.substring(replace+9);
        }
		return str;
	}

	public void postDamage(MOB attacker,
						   MOB target,
						   Environmental weapon,
						   int damage,
						   int messageCode,
						   int damageType,
						   String allDisplayMessage)
	{
		if((attacker==null)||(target==null)||(target.location()==null)) return;
		if(allDisplayMessage!=null) allDisplayMessage="^F^<FIGHT^>"+allDisplayMessage+"^</FIGHT^>^?";
		if((weapon instanceof Ability)
		&&(damage>0)
		&&(attacker != target)
		&&(attacker != null)
		&&(target != null)
		&&(attacker.isMine(weapon)||(attacker.envStats().level()>1))) // why >1? because quickly made fake-mobs tend to have lvl=1
			damage = modifySpellDamage(attacker, target, damage);
		
		CMMsg msg=CMClass.getMsg(attacker,target,weapon,messageCode,CMMsg.MSG_DAMAGE,messageCode,allDisplayMessage);
		msg.setValue(damage);
        CMLib.color().fixSourceFightColor(msg);
        Room R=(target==null)?null:target.location();
        if(R!=null)
			if(R.okMessage(target,msg))
			{
				if(damageType>=0)
					msg.modify(msg.source(),
							   msg.target(),
							   msg.tool(),
							   msg.sourceCode(),
							   replaceDamageTag(msg.sourceMessage(),msg.value(),damageType),
							   msg.targetCode(),
							   replaceDamageTag(msg.targetMessage(),msg.value(),damageType),
							   msg.othersCode(),
							   replaceDamageTag(msg.othersMessage(),msg.value(),damageType));
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
				attacker.envStats().level(),
				target.envStats().level()
				};
		baseDamage = (int)Math.round(CMath.parseMathExpression(spellFudgeDamageFormula, vars, 0.0));
		vars[0]=baseDamage;
		int spellCritChancePct = (int)Math.round(CMath.parseMathExpression(spellCritChanceFormula, vars, 0.0));
		if(CMLib.dice().rollPercentage()<spellCritChancePct)
		{
			int spellCritDamageAmt = (int)Math.round(CMath.parseMathExpression(spellCritDmgFormula, vars, 0.0));
			baseDamage+=spellCritDamageAmt;
		}
		return baseDamage;
	}
	
	public int adjustedDamage(MOB mob, Weapon weapon, MOB target)
	{
		double damageAmount=0.0;
		Environmental useDmg = null;
		boolean rangedAttack = false;
		if((weapon!=null)
		&&((weapon.weaponClassification()==Weapon.CLASS_RANGED)||(weapon.weaponClassification()==Weapon.CLASS_THROWN)))
		{
			useDmg = weapon;
			rangedAttack = true;
		}
		else
			useDmg = mob;
		if(target!=null)
		{
			double[] vars = {
					useDmg.envStats().damage(),
					mob.charStats().getStat(CharStats.STAT_STRENGTH),
					mob.envStats().level(),
					target.envStats().level(),
					(mob.curState().getHunger()<1)?1.0:0.0,
					(mob.curState().getThirst()<1)?1.0:0.0,
					(mob.curState().getFatigue()>CharState.FATIGUED_MILLIS)?1.0:0.0,
					CMLib.flags().canBeSeenBy(target,mob)?0:1,
					CMLib.flags().isSleeping(target)?1:0,
					CMLib.flags().isSitting(target)?1:0
				};
			if(rangedAttack)
				damageAmount = CMath.parseMathExpression(targetedRangedDamageFormula, vars, 0.0);
			else
				damageAmount = CMath.parseMathExpression(targetedMeleeDamageFormula, vars, 0.0);
		}
		else
		{
			double[] vars = {
					useDmg.envStats().damage(),
					mob.charStats().getStat(CharStats.STAT_STRENGTH),
					mob.envStats().level(),
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
				mob.envStats().level(),
				(target==null)?0:target.envStats().level()
			};
		int weaponCritChancePct = (int)Math.round(CMath.parseMathExpression(weaponCritChanceFormula, vars, 0.0));
		if(CMLib.dice().rollPercentage()<weaponCritChancePct)
		{
			int weaponCritDmgAmt = (int)Math.round(CMath.parseMathExpression(weaponCritDmgFormula, vars, 0.0));
			damageAmount += weaponCritDmgAmt;
		}
		if(target != null)
		{
			vars[0] = damageAmount;
			if(rangedAttack)
				damageAmount = CMath.parseMathExpression(rangedFudgeDamageFormula, vars, 0.0);
			else
				damageAmount = CMath.parseMathExpression(meleeFudgeDamageFormula, vars, 0.0);
		}
		return (int)Math.round(damageAmount);
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
			damageInt=CMLib.combat().adjustedDamage(source,weapon,target);
			damageType=weapon.weaponType();
		}
		if(success)
		{
            // calculate Base Damage (with Strength bonus)
			String oldHitString="^F^<FIGHT^>"+((weapon!=null)?
								weapon.hitString(damageInt):
								standardHitString(Weapon.CLASS_BLUNT,damageInt,item.name()))+"^</FIGHT^>^?";
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
							   replaceDamageTag(msg.sourceMessage(),msg.value(),damageType),
							   msg.targetCode(),
							   replaceDamageTag(msg.targetMessage(),msg.value(),damageType),
							   msg.othersCode(),
							   replaceDamageTag(msg.othersMessage(),msg.value(),damageType));
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

	public void processFormation(Vector[] done, MOB leader, int level)
	{
		for(int i=0;i<done.length;i++)
			if((done[i]!=null)&&(done[i].contains(leader)))
				return;
	    if(level>=done.length) return;
		if(done[level]==null) done[level]=new Vector();
		done[level].addElement(leader);
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

	public Vector[] getFormation(MOB mob)
	{
		MOB leader=mob;
        if(leader.amFollowing()!=null)
            leader=leader.amUltimatelyFollowing();
		Vector[] done=new Vector[20];
		processFormation(done,leader,0);
	    return done;
	}

	public Vector getFormationFollowed(MOB mob)
	{
	    Vector[] form=getFormation(mob);
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
	    Vector[] form=getFormation(mob);
	    for(int i=1;i<form.length;i++)
	    {
	        if((form[i]!=null)&&(form[i].contains(mob)))
	            return i;
	    }
	    return 0;
	}

    public CharClass getCombatDominantClass(MOB killer, MOB killed)
    {
        CharClass C=null;

        if((killer!=null)&&(killer.charStats()!=null))
        {
            C=killer.charStats().getCurrentClass();
            MOB M=killer;
            HashSet checked=new HashSet();
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

    protected HashSet getCombatBeneficiaries(MOB killer, MOB killed, Room deathRoom, HashSet beneficiaries, CharClass combatCharClass)
    {
        HashSet followers=(killer!=null)?killer.getGroupMembers(new HashSet()):(new HashSet());
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

    public HashSet getCombatBeneficiaries(MOB killer, MOB killed, CharClass combatCharClass)
    {
        if((killer==null)||(killed==null)) return new HashSet();
        HashSet beneficiaries=new HashSet();
        Room R=killer.location();
        if(R!=null) getCombatBeneficiaries(killer,killed,R,beneficiaries,combatCharClass);
        R=killed.location();
        if((R!=null)&&(R!=killer.location())) getCombatBeneficiaries(killer,killed,R,beneficiaries,combatCharClass);
        return beneficiaries;
    }


    protected HashSet getCombatDividers(MOB killer, MOB killed, Room deathRoom, HashSet dividers, CharClass combatCharClass)
    {
        HashSet followers=(killer!=null)?killer.getGroupMembers(new HashSet()):(new HashSet());
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

    public HashSet getCombatDividers(MOB killer, MOB killed, CharClass combatCharClass)
    {
        if((killer==null)||(killed==null)) return new HashSet();
        HashSet dividers=new HashSet();
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

        CharClass combatCharClass=getCombatDominantClass(source,target);
		HashSet beneficiaries=getCombatBeneficiaries(source,target,combatCharClass);
		HashSet dividers=getCombatDividers(source,target,combatCharClass);

        dispenseExperience(beneficiaries,dividers,target);

	    String currency=CMLib.beanCounter().getCurrency(target);
		double deadMoney=CMLib.beanCounter().getTotalAbsoluteValue(target,currency);
		double myAmountOfDeadMoney=0.0;
		Vector goldLooters=new Vector();
		for(Iterator e=beneficiaries.iterator();e.hasNext();)
		{
			MOB M=(MOB)e.next();
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

		int[] expLost={100*target.envStats().level()};
		if(expLost[0]<100) expLost[0]=100;
		String[] cmds=null;
		if((target.isMonster())||(target.soulMate()!=null))
			cmds=CMParms.toStringArray(CMParms.parseCommas(CMProps.getVar(CMProps.SYSTEM_MOBDEATH),true));
		else
			cmds=CMParms.toStringArray(CMParms.parseCommas(CMProps.getVar(CMProps.SYSTEM_PLAYERDEATH),true));
		
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

			if((!target.isMonster())&&(CMLib.dice().rollPercentage()==1)&&(!CMSecurity.isDisabled("AUTODISEASE")))
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
					Item item=bodyRoom.fetchItem(i);
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
					C.recoverEnvStats();
					bodyRoom.addItemRefuse(C,CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_MONSTER_EQ));
					bodyRoom.recoverRoomStats();
					MOB mob=(MOB)goldLooters.elementAt(g);
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

			if((body!=null)&&(bodyRoom!=null)&&(body.destroyAfterLooting()))
			{
				for(int i=bodyRoom.numItems()-1;i>=0;i--)
				{
					Item item=bodyRoom.fetchItem(i);
					if((item!=null)&&(item.container()==body))
						item.setContainer(null);
				}
				body.destroy();
				bodyRoom.recoverEnvStats();
	            return null;
			}
	        return body;
		}
		return null;
	}

    private int[] damageThresholds(){return CMProps.getI1ListVar(CMProps.SYSTEML_DAMAGE_WORDS_THRESHOLDS);}
    private String[][] hitWords(){ return CMProps.getS2ListVar(CMProps.SYSTEML_DAMAGE_WORDS); }
    private String[] armorDescs(){return CMProps.getSListVar(CMProps.SYSTEML_ARMOR_DESCS);}
    private String[] prowessDescs(){return CMProps.getSListVar(CMProps.SYSTEML_PROWESS_DESCS);}
    private String[] missWeaponDescs(){return CMProps.getSListVar(CMProps.SYSTEML_WEAPON_MISS_DESCS);}
    private String[] missDescs(){return CMProps.getSListVar(CMProps.SYSTEML_MISS_DESCS);}

    public String standardHitWord(int type, int damage)
    {
        if((type<0)||(type>=Weapon.TYPE_DESCS.length))
            type=Weapon.TYPE_BURSTING;
        int[] thresholds=damageThresholds();
        int damnCode=thresholds.length-2;
        for(int i=0;i<thresholds.length;i++)
            if(damage<=thresholds[i]){ damnCode=i; break;}
        damnCode++; // always add 1 because index into hitwords is type=0, annoy=1;
        if(hitWords() != hitWordsChanged)
        {
            hitWordsChanged=hitWords();
            hitWordIndex=null;
        }
        if(hitWordIndex==null)
        {
            String[][] newWordIndex=new String[Weapon.TYPE_DESCS.length][];
            String[][] hitWords=hitWords();
            for(int w=0;w<Weapon.TYPE_DESCS.length;w++)
            {
                String[] ALL=null;
                String[] MINE=null;
                for(int i=0;i<hitWords.length;i++)
                {
                    if(hitWords[i][0].equalsIgnoreCase("ALL"))
                        ALL=hitWords[i];
                    else
                    if(hitWords[i][0].equalsIgnoreCase(Weapon.TYPE_DESCS[w]))
                    { MINE=hitWords[i]; break;}
                }
                if(MINE!=null)
                    newWordIndex[w]=MINE;
                else
                    newWordIndex[w]=ALL;
            }
            hitWordIndex=newWordIndex;
        }
        String[] HIT_WORDS=hitWordIndex[type];
        if(damnCode<1) damnCode=1;
        if(damnCode<HIT_WORDS.length) return HIT_WORDS[damnCode];
        return HIT_WORDS[HIT_WORDS.length-1];
    }

    public String armorStr(MOB mob)
    {
    	int armor = -adjustedArmor(mob);
        int ARMOR_CEILING=CMProps.getIListVar(CMProps.SYSTEML_ARMOR_DESCS_CEILING);
        return (armor<0)?armorDescs()[0]:(
               (armor>=ARMOR_CEILING)?armorDescs()[armorDescs().length-1]+(CMStrings.repeat("!",(armor-ARMOR_CEILING)/100))+" ("+armor+")":(
                       armorDescs()[(int)Math.round(Math.floor(CMath.mul(CMath.div(armor,ARMOR_CEILING),armorDescs().length)))]+" ("+armor+")"));
    }
    
    public String fightingProwessStr(MOB mob)
    {
    	int prowess = adjustedAttackBonus(mob,null) - ATTACK_ADJUSTMENT;
        int PROWESS_CEILING=CMProps.getIListVar(CMProps.SYSTEML_PROWESS_DESCS_CEILING);
        return (prowess<0)?prowessDescs()[0]:(
               (prowess>=PROWESS_CEILING)?prowessDescs()[prowessDescs().length-1]+(CMStrings.repeat("!",(prowess-PROWESS_CEILING)/100))+" ("+prowess+")":(
                prowessDescs()[(int)Math.round(Math.floor(CMath.mul(CMath.div(prowess,PROWESS_CEILING),prowessDescs().length)))]+" ("+prowess+")"));
    }

    public String standardMissString(int weaponType, int weaponClassification, String weaponName, boolean useExtendedMissString)
    {
        int dex=3;
        switch(weaponClassification)
        {
        case Weapon.CLASS_RANGED: dex=0; break;
        case Weapon.CLASS_THROWN: dex=1; break;
        default:
            switch(weaponType)
            {
            case Weapon.TYPE_SLASHING:
            case Weapon.TYPE_BASHING:
                dex=2; break;
            case Weapon.TYPE_PIERCING:
                dex=4; break;
            case Weapon.TYPE_SHOOT:
                dex=0; break;
            default:
                dex=3;
                break;
            }
            break;
        }
        if(!useExtendedMissString) return missDescs()[dex];
        return CMStrings.replaceAll(missWeaponDescs()[dex],"<TOOLNAME>",weaponName)+CMProps.msp("missed.wav",20);
    }


    public String standardHitString(int weaponClass, int damageAmount,  String weaponName)
    {
        if((weaponName==null)||(weaponName.length()==0))
            weaponClass=Weapon.CLASS_NATURAL;
        switch(weaponClass)
        {
        case Weapon.CLASS_RANGED:
            return "<S-NAME> fire(s) "+weaponName+" at <T-NAMESELF> and <DAMAGE> <T-HIM-HER>."+CMProps.msp("arrow.wav",20);
        case Weapon.CLASS_THROWN:
            return "<S-NAME> throw(s) "+weaponName+" at <T-NAMESELF> and <DAMAGE> <T-HIM-HER>."+CMProps.msp("arrow.wav",20);
        default:
            return "<S-NAME> <DAMAGE> <T-NAMESELF> with "+weaponName+"."+CMProps.msp("punch"+CMLib.dice().roll(1,7,0)+".wav",20);
        }
    }

    public String[] healthDescs(){return CMProps.getSListVar(CMProps.SYSTEML_HEALTH_CHART);}
    public String standardMobCondition(MOB viewer,MOB mob)
    {
        int pct=(int)Math.round(Math.floor((CMath.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()))*10));
        if(pct<0) pct=0;
        if(pct>=healthDescs().length) pct=healthDescs().length-1;
        return CMStrings.replaceAll(healthDescs()[pct],"<MOB>",mob.displayName(viewer));
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
        	Log.combatOut("DAMG",attacker.Name()+":"+attacker.envStats().getCombatStats()+":"+attacker.curState().getCombatStats()+":"+((KI==null)?"null":KI.name())+":"+target.Name()+":"+target.envStats().getCombatStats()+":"+target.curState().getCombatStats()+":"+((DI==null)?"null":DI.name())+":"+tool+":"+type+":"+dmg);
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
            		if((Math.round(CMath.div(dmg,target.maxState().getHitPoints())*100.0)>=CMProps.getIntVar(CMProps.SYSTEMI_INJBLEEDPCTHP))
            		&&bleedableWeapon(msg.tool()))
    				{
    					Ability A2=CMClass.getAbility("Bleeding");
    					if(A2!=null) A2.invoke(((MOB)target),((MOB)target),true,0);
    				}
	                if((target.curState().getHitPoints()<target.getWimpHitPoint())
	                &&(target.getWimpHitPoint()>0)
	                &&(target.isInCombat()))
	                    postPanic(target,msg);
	                else
	                if((CMProps.getIntVar(CMProps.SYSTEMI_INJPCTHP)>=(int)Math.round(CMath.div(target.curState().getHitPoints(),target.maxState().getHitPoints())*100.0))
	                &&(!CMLib.flags().isGolem(target))
	                &&(target.fetchEffect("Injury")==null))
	                {
	                    Ability A=CMClass.getAbility("Injury");
	                    if(A!=null) A.invoke(target,CMParms.makeVector(msg),target,true,0);
	                }
                }
            }
        }
    }

    public void handleDeath(CMMsg msg)
    {
        MOB deadmob=msg.source();

        if(!deadmob.amDead())
        {
            if((!deadmob.isMonster())&&(deadmob.soulMate()==null))
            {
                CMLib.coffeeTables().bump(deadmob,CoffeeTableRow.STAT_DEATHS);
                Vector channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.DETAILEDDEATHS);
                Vector channels2=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.DEATHS);
                if(!CMLib.flags().isCloaked(deadmob))
                for(int i=0;i<channels.size();i++)
                if((msg.tool()!=null)&&(msg.tool() instanceof MOB))
                    CMLib.commands().postChannel((String)channels.elementAt(i),deadmob.getClanID(),deadmob.Name()+" was just killed in "+CMLib.map().getExtendedRoomID(deadmob.location())+" by "+msg.tool().Name()+".",true);
                else
                    CMLib.commands().postChannel((String)channels.elementAt(i),deadmob.getClanID(),deadmob.Name()+" has just died at "+CMLib.map().getExtendedRoomID(deadmob.location()),true);
                if(!CMLib.flags().isCloaked(deadmob))
                for(int i=0;i<channels2.size();i++)
                    if((msg.tool()!=null)&&(msg.tool() instanceof MOB))
                        CMLib.commands().postChannel((String)channels2.elementAt(i),deadmob.getClanID(),deadmob.Name()+" was just killed.",true);
            }
            if(msg.tool() instanceof MOB)
            {
                MOB killer=(MOB)msg.tool();
                if(Log.killsChannelOn())
                {
                	Item KI=killer.fetchWieldedItem();
                	Item DI=deadmob.fetchWieldedItem();
                	String room=CMLib.map().getExtendedRoomID((killer.location()!=null)?killer.location():deadmob.location());
                	Log.killsOut("KILL",room+":"+killer.Name()+":"+killer.envStats().getCombatStats()+":"+killer.curState().getCombatStats()+":"+((KI==null)?"null":KI.name())+":"+deadmob.Name()+":"+deadmob.envStats().getCombatStats()+":"+deadmob.curState().getCombatStats()+":"+((DI==null)?"null":DI.name()));
                }
                if(Log.combatChannelOn())
                {
                	Item DI=deadmob.fetchWieldedItem();
                	Item KI=killer.fetchWieldedItem();
                	Log.combatOut("KILL",killer.Name()+":"+killer.envStats().getCombatStats()+":"+killer.curState().getCombatStats()+":"+((KI==null)?"null":KI.name())+":"+deadmob.Name()+":"+deadmob.envStats().getCombatStats()+":"+deadmob.curState().getCombatStats()+":"+((DI==null)?"null":DI.name()));
                }
                justDie(killer,deadmob);
                if((!deadmob.isMonster())&&(deadmob.soulMate()==null)&&(killer!=deadmob)&&(!killer.isMonster()))
                {
                    CMLib.coffeeTables().bump(deadmob,CoffeeTableRow.STAT_PKDEATHS);
	                if((deadmob.getClanID().length()>0)
	                &&(killer.getClanID().length()>0)
	                &&(!deadmob.getClanID().equals(killer.getClanID()))
	                &&(deadmob.session()!=null)
	                &&(killer.session()!=null)
	                &&(!deadmob.session().getAddress().equalsIgnoreCase(killer.session().getAddress())))
	                {
	                    Clan C=CMLib.clans().getClan(killer.getClanID());
	                    if(C!=null) C.recordClanKill();
	                }
                }
            }
            else
                justDie(null,deadmob);
            deadmob.tell(deadmob,msg.target(),msg.tool(),msg.sourceMessage());
            if(deadmob.riding()!=null) deadmob.riding().delRider(deadmob);
            if(CMLib.flags().isCataloged(deadmob))
    	        CMLib.catalog().bumpDeathPickup(deadmob);
        }
    }

    public void handleObserveDeath(MOB observer, MOB fighting, CMMsg msg)
    {
        Room R=observer.location();
        MOB deadmob=msg.source();
        if((fighting==deadmob)&&(R!=null))
        {
            MOB newTargetM=null;
            HashSet hisGroupH=deadmob.getGroupMembers(new HashSet());
            HashSet myGroupH=observer.getGroupMembers(new HashSet());
            for(int r=0;r<R.numInhabitants();r++)
            {
                MOB M=R.fetchInhabitant(r);
                if((M!=observer)
                &&(M!=deadmob)
                &&(M!=null)
                &&(M.getVictim()==observer)
                &&(!M.amDead())
                &&(CMLib.flags().isInTheGame(M,true)))
                {
                    newTargetM=M;
                    break;
                }
            }
            if(newTargetM==null)
            for(int r=0;r<R.numInhabitants();r++)
            {
                MOB M=R.fetchInhabitant(r);
                MOB vic=M.getVictim();
                if((M!=observer)
                &&(M!=deadmob)
                &&(M!=null)
                &&(hisGroupH.contains(M)
                    ||((vic!=null)&&(myGroupH.contains(vic))))
                &&(!M.amDead())
                &&(CMLib.flags().isInTheGame(M,true)))
                {
                    newTargetM=M;
                    break;
                }
            }
            if((newTargetM==null)||(newTargetM.isInCombat()))
                observer.setVictim(newTargetM);
        }
    }

    public void handleBeingAssaulted(CMMsg msg)
    {
        if(!(msg.target() instanceof MOB)) return;
        MOB attacker=msg.source();
        MOB target=(MOB)msg.target();

        if((!target.isInCombat())
        &&(target.location()!=null)
        &&(target.location().isInhabitant(attacker))
        &&((!CMath.bset(msg.sourceCode(),CMMsg.MASK_ALWAYS))
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

    public Vector getAllInProximity(MOB to, int distance)
    {
    	Room R=to.location();
    	Vector V=new Vector();
    	V.addElement(to);
    	if(R==null) return V;
    	Vector everyV=new Vector();
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
                    source.recoverEnvStats();
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
                        source.recoverEnvStats();
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
            source.recoverEnvStats();
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
                            fighter.getVictim().recoverEnvStats();
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

    protected void subtickBeforeAttack(MOB fighter)
    {
        // combat que system eats up standard commands
        // before using any attacks
        while((CMProps.getIntVar(CMProps.SYSTEMI_COMBATSYSTEM)==CombatLibrary.COMBAT_QUEUE)
        &&(!fighter.amDead())
        &&(fighter.dequeCommand()));
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

    public void dispenseExperience(HashSet killers, HashSet dividers, MOB killed)
    {
        int totalLevels=0;
        int expAmount=100;
        int expAddition=25;

        for(Iterator i=dividers.iterator();i.hasNext();)
        {
            MOB mob=(MOB)i.next();
            totalLevels += (mob.envStats().level()*mob.envStats().level());
            expAmount += expAddition;
            expAddition -= expAddition/4;
        }
		for(Iterator i=killers.iterator();i.hasNext();)
		{
			MOB mob=(MOB)i.next();
			int myAmount=(int)Math.round(CMath.mul(expAmount,CMath.div(mob.envStats().level()*mob.envStats().level(),totalLevels)));
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


        subtickBeforeAttack(fighter);
        int combatSystem=CMProps.getIntVar(CMProps.SYSTEMI_COMBATSYSTEM);
        int saveAction=(combatSystem!=CombatLibrary.COMBAT_DEFAULT)?0:1;

        int folrange=(CMath.bset(fighter.getBitmap(),MOB.ATT_AUTOMELEE)
                        &&(fighter.amFollowing()!=null)
                        &&(fighter.amFollowing().getVictim()==fighter.getVictim())
                        &&(fighter.amFollowing().rangeToTarget()>=0)
                        &&(fighter.amFollowing().fetchFollowerOrder(fighter)>=0))?
                                fighter.amFollowing().fetchFollowerOrder(fighter)+fighter.amFollowing().rangeToTarget():-1;
        if(CMLib.flags().aliveAwakeMobile(fighter,true))
        {
            if((combatSystem!=CombatLibrary.COMBAT_MANUAL)
            ||(fighter.isMonster()))
            {
                int numAttacks=(int)Math.round(Math.floor(fighter.actions()))-saveAction;
                if((combatSystem==CombatLibrary.COMBAT_DEFAULT)
                &&(numAttacks>(int)Math.round(Math.floor(fighter.envStats().speed()+0.9))))
                    numAttacks=(int)Math.round(Math.floor(fighter.envStats().speed()+0.9));
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
            }
            if(CMLib.dice().rollPercentage()>(fighter.charStats().getStat(CharStats.STAT_CONSTITUTION)*4))
                fighter.curState().adjMovement(-1,fighter.maxState());
        }

        subtickAfterAttack(fighter);
    }

    public boolean isKnockedOutUponDeath(MOB mob, MOB fighting)
    {
    	String whatToDo=null;
    	if(((mob.isMonster())||(mob.soulMate()!=null)))
    		whatToDo=CMProps.getVar(CMProps.SYSTEM_MOBDEATH).toUpperCase();
    	else
    		whatToDo=CMProps.getVar(CMProps.SYSTEM_PLAYERDEATH).toUpperCase();
		Vector whatsToDo=CMParms.parseCommas(whatToDo,true);
		double[] fakeVarVals={1.0,1.0,1.0};
		for(int w=0;w<whatsToDo.size();w++)
		{
			whatToDo=(String)whatsToDo.elementAt(w);
			if(whatToDo.startsWith("OUT ")&&(CMath.isMathExpression(whatToDo.substring(4).trim(),fakeVarVals)))
				return true;
		}
		return false;
    }

    public boolean handleConsequences(MOB mob, MOB fighting, String[] commands, int[] lostExperience, String message)
    {
    	if((commands==null)||(commands.length==0)) return false;
		if(lostExperience==null) lostExperience=new int[1];
		int baseExperience=lostExperience[0];
		lostExperience[0]=0;
        int rejuv=mob.envStats().rejuv();
        if((rejuv==0)||(rejuv==Integer.MAX_VALUE)) rejuv=mob.envStats().level();
        if(((!mob.isMonster())&&(mob.soulMate()==null))) rejuv=1;
        double[] varVals={
                mob.baseEnvStats().level()>mob.envStats().level()?mob.baseEnvStats().level():mob.envStats().level(),
                (fighting!=null)?fighting.envStats().level():0,
                rejuv
        };
		for(int w=0;w<commands.length;w++)
		{
			String whatToDo=commands[w].toUpperCase();
			if(whatToDo.startsWith("UNL"))
			{
				Vector V=CMParms.parse(whatToDo);
				int times=1;
				if((V.size()>1)&&(CMath.s_int((String)V.lastElement())>1))
					times=CMath.s_int((String)V.lastElement());
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
					A.invoke(mob,CMParms.makeVector(""+tickDown,"SAFELY"),mob,true,0);
					mob.resetToMaxState();
				}
			}
			else
			if(whatToDo.startsWith("PUR"))
			{
				MOB deadMOB=CMLib.players().getLoadPlayer(mob.Name());
				if(deadMOB!=null)
				{
					CMLib.players().obliteratePlayer(deadMOB,false);
					return false;
				}
			}
			else
	        if(whatToDo.startsWith("LOSESK"))
	        {
	            if(mob.numLearnedAbilities()>0)
	            {
	                Ability A=mob.fetchAbility(CMLib.dice().roll(1,mob.numLearnedAbilities(),-1));
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
