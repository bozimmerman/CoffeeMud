package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
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
   Copyright 2000-2005 Bo Zimmerman

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
    public String lastStr="";
    public long lastRes=0;

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
		if(A.quality()!=Ability.MALICIOUS)
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

    public boolean rollToHit(MOB attacker, MOB defender)
    {
        if((attacker==null)||(defender==null)) return false;
        return rollToHit(attacker.adjustedAttackBonus(defender),defender.adjustedArmor());
    }
    
    public boolean rollToHit(int attack, int defense)
    {
        double myArmor=new Integer(-(defense-100)).doubleValue();
        if(myArmor==0) myArmor=1.0;
        else
        if(myArmor<0.0) myArmor=-CMath.div(1.0,myArmor);
        double hisAttack=new Integer(attack+50).doubleValue();
        if(hisAttack==0.0) hisAttack=1.0;
        else
        if(hisAttack<0.0) hisAttack=-CMath.div(1.0,myArmor);
        return CMLib.dice().normalizeAndRollLess((int)Math.round(50.0*(hisAttack/myArmor)));
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
		CMMsg msg=CMClass.getMsg(deadM,null,null,
			CMMsg.MSG_OK_VISUAL,"^f^*^<FIGHT^>!!!!!!!!!!!!!!YOU ARE DEAD!!!!!!!!!!!!!!^</FIGHT^>^?^.\n\r"+msp,
			CMMsg.MSG_OK_VISUAL,null,
			CMMsg.MSG_OK_VISUAL,"^F^<FIGHT^><S-NAME> is DEAD!!!^</FIGHT^>^?\n\r"+msp);
		CMMsg msg2=CMClass.getMsg(deadM,null,killerM,
			CMMsg.MSG_DEATH,null,
			CMMsg.MSG_DEATH,null,
			CMMsg.MSG_DEATH,null);
		if(addHere!=null)
		{
			if((deathRoom==null)||(deathRoom.okMessage(deadM,msg2)))
			{
				addHere.addTrailerMsg(msg);
				addHere.addTrailerMsg(msg2);
			}
		}
		else
		if((deathRoom!=null)&&(deathRoom.okMessage(deadM,msg)))
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
		if(target.location().okMessage(target,msg))
		{ target.location().send(target,msg); return true;}
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
		CMMsg msg=CMClass.getMsg(attacker,target,weapon,messageCode,CMMsg.MSG_DAMAGE,messageCode,allDisplayMessage);
		msg.setValue(damage);
        CMLib.color().fixSourceFightColor(msg);
		if(target.location().okMessage(target,msg))
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
			target.location().send(target,msg);
		}
	}

	public boolean postExperience(MOB mob,
								  MOB victim,
								  String homage,
								  int amount,
								  boolean quiet)
	{
		if((mob==null)
		||(CMSecurity.isDisabled("EXPERIENCE"))
		||mob.charStats().getCurrentClass().expless()
		||mob.charStats().getMyRace().expless())
	        return false;
		CMMsg msg=CMClass.getMsg(mob,victim,null,CMMsg.MASK_GENERAL|CMMsg.TYP_EXPCHANGE,null,CMMsg.NO_EFFECT,homage,CMMsg.NO_EFFECT,""+quiet);
		msg.setValue(amount);
		if(mob.location()!=null)
		{
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
			else
				return false;
		}
		else
		if(amount>=0)
			mob.charStats().getCurrentClass().gainExperience(mob,victim,homage,amount,quiet);
		else
			mob.charStats().getCurrentClass().loseExperience(mob,-amount);
		return true;
	}

    public boolean changeFactions(MOB mob,
                                         MOB victim,
                                         int amount,
                                         boolean quiet)
    {
        if((mob==null))
            return false;
        CMMsg msg=CMClass.getMsg(mob,victim,null,CMMsg.MASK_GENERAL|CMMsg.TYP_FACTIONCHANGE,null,CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,""+quiet);
        msg.setValue(amount);
        if(mob.location()!=null)
        {
            if(mob.location().okMessage(mob,msg))
                mob.location().send(mob,msg);
            else
                return false;
        }
        return true;
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
			damageInt=source.adjustedDamage(weapon,target);
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
			if((source.location().okMessage(source,msg))
            &&(source.location()!=null))
				source.location().send(source,msg);
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
		int tries=0;
		MOB leader=mob;
		while((leader.amFollowing()!=null)&&(((++tries)<1000)))
			leader=leader.amFollowing();
	    return leader;
	}

	public Vector[] getFormation(MOB mob)
	{
		int tries=0;
		MOB leader=mob;
		while((leader.amFollowing()!=null)&&(((++tries)<1000)))
			leader=leader.amFollowing();
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

    public HashSet getCombatBeneficiaries(MOB killer, MOB killed, CharClass combatCharClass)
    {
        if((killer==null)||(killed==null)) return new HashSet();
        Room deathRoom=killed.location();
        if(deathRoom==null) deathRoom=killer.location();

        HashSet beneficiaries=new HashSet();
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
        return beneficiaries;
    }
    
    
	public DeadBody justDie(MOB source, MOB target)
	{
		if(target==null) return null;
		Room deathRoom=target.location();

        CharClass combatCharClass=getCombatDominantClass(source,target);
		HashSet beneficiaries=getCombatBeneficiaries(source,target,combatCharClass);
        combatCharClass.dispenseExperience(beneficiaries,target);
        
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

		DeadBody Body=null;
		if((target.soulMate()==null)&&(!target.isMonster()))
		{
			Vector whatsToDo=CMParms.parse(CMProps.getVar(CMProps.SYSTEM_PLAYERDEATH));
			for(int w=0;w<whatsToDo.size();w++)
			{
				String whatToDo=(String)whatsToDo.elementAt(w);
				if(whatToDo.startsWith("UNL"))
					target.charStats().getCurrentClass().unLevel(target);
				else
				if(whatToDo.startsWith("ASTR"))
				{
					Ability A=CMClass.getAbility("Prop_AstralSpirit");
					if((A!=null)&&(target.fetchAbility(A.ID())==null))
					{
						target.addAbility(A);
						A.autoInvocation(target);
					}
				}
				else
                if(whatToDo.startsWith("LOSESK"))
                {
                    if(target.numLearnedAbilities()>0)
                    {
                        Ability A=target.fetchAbility(CMLib.dice().roll(1,target.numLearnedAbilities(),-1));
                        if(A!=null)
                        {
                            target.tell("You've forgotten "+A.Name()+".");
                            target.delAbility(A);
                            if(A.isAutoInvoked())
                            {
                                Ability A2=target.fetchEffect(A.ID());
                                A2.unInvoke();
                                target.delEffect(A2);
                            }
                        }
                    }
                }
                else
				if(whatToDo.startsWith("PUR"))
				{
					MOB deadMOB=CMLib.map().getLoadPlayer(target.Name());
					if(deadMOB!=null)
					{
						Body=target.killMeDead(true);
						CMLib.utensils().obliteratePlayer(deadMOB,false);
					}
				}
				else
				if((whatToDo.trim().equals("0"))||(CMath.s_int(whatToDo)>0))
				{
					int expLost=CMath.s_int(whatToDo);
					target.tell("^*You lose "+expLost+" experience points.^?^.");
					postExperience(target,null,null,-expLost,false);
				}
				else
				if(whatToDo.length()<3)
					continue;
				else
				{
					int expLost=100*target.envStats().level();
					target.tell("^*You lose "+expLost+" experience points.^?^.");
					postExperience(target,null,null,-expLost,false);
				}
			}
		}
		if(Body==null) Body=target.killMeDead(true);
        if((source!=null)&&(Body!=null))
        {
            Body.setKillerName(source.Name());
            Body.setKillerPlayer(!source.isMonster());
        }

		if((!target.isMonster())&&(CMLib.dice().rollPercentage()==1))
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
        &&(deathRoom!=null)
		&&(source.location()==deathRoom)
		&&(deathRoom.isInhabitant(source))
		&&(CMath.bset(source.getBitmap(),MOB.ATT_AUTOLOOT)))
		{
			if((source.riding()!=null)&&(source.riding() instanceof MOB))
				source.tell("You'll need to dismount to loot the body.");
			else
			if((source.riding()!=null)&&(source.riding() instanceof MOB))
				source.tell("You'll need to disembark to loot the body.");
			else
			for(int i=deathRoom.numItems()-1;i>=0;i--)
			{
				Item item=deathRoom.fetchItem(i);
				if((item!=null)
				&&(item.container()==Body)
				&&(CMLib.flags().canBeSeenBy(Body,source))
				&&((!Body.destroyAfterLooting())||(!(item instanceof EnvResource)))
				&&(CMLib.flags().canBeSeenBy(item,source)))
					CMLib.commands().postGet(source,Body,item,false);
			}
			if(Body.destroyAfterLooting())
				deathRoom.recoverRoomStats();
		}

		Coins C=null;
		if((deadMoney>0)&&(myAmountOfDeadMoney>0))
		for(int g=0;g<goldLooters.size();g++)
		{
		    C=CMLib.beanCounter().makeBestCurrency(currency,myAmountOfDeadMoney,null,Body);
		    if(C!=null)
		    {
				C.recoverEnvStats();
				deathRoom.addItemRefuse(C,Item.REFUSE_MONSTER_EQ);
				deathRoom.recoverRoomStats();
				MOB mob=(MOB)goldLooters.elementAt(g);
				if((mob.riding()!=null)&&(mob.riding() instanceof MOB))
					mob.tell("You'll need to dismount to get "+C.name()+" off the body.");
				else
				if((mob.riding()!=null)&&(mob.riding() instanceof Item))
					mob.tell("You'll need to disembark to get "+C.name()+" off the body.");
				else
				if(CMLib.flags().canBeSeenBy(Body,mob))
					CMLib.commands().postGet(mob,Body,C,false);
		    }
		}

		if(Body.destroyAfterLooting())
		{
			for(int i=deathRoom.numItems()-1;i>=0;i--)
			{
				Item item=deathRoom.fetchItem(i);
				if((item!=null)&&(item.container()==Body))
					item.setContainer(null);
			}
			Body.destroy();
			deathRoom.recoverEnvStats();
            return null;
		}
        return Body;
	}

    public int[] damageThresholds(){return CombatLibrary.DEFAULT_DAMAGE_THRESHHOLDS;}
    public String[][] hitWords(){return CombatLibrary.DEFAULT_DAMAGE_WORDS;}
    public String[] armorDescs(){return CombatLibrary.DEFAULT_ARMOR_DESCS;}
    public String[] prowessDescs(){return CombatLibrary.DEFAULT_PROWESS_DESCS;}
    public String[] missWeaponDescs(){return CombatLibrary.DEFAULT_WEAPON_MISS_DESCS;}
    public String[] missDescs(){return CombatLibrary.DEFAULT_MISS_DESCS;}
    
    public String standardHitWord(int type, int damage)
    {
        if(type<0) type=Weapon.TYPE_BURSTING;
        int[] thresholds=damageThresholds();
        String[][] hitwords=hitWords();
        int damnCode=0;
        for(int i=0;i<thresholds.length;i++)
            if(damage<=thresholds[i]){ damnCode=i; break;}
        damnCode++; // always add 1 because index into hitwords is type=0, annoy=1;
        String[] ALL=null;
        String[] MINE=null;
        for(int i=0;i<hitwords.length;i++)
        {
            if(hitwords[i][0].equalsIgnoreCase("ALL"))
                ALL=hitwords[i];
            else
            if(hitwords[i][0].equals(""+type))
            { MINE=hitwords[i]; break;}
        }
        if(damnCode<1) damnCode=1;
        if((MINE==null)||(damnCode>=MINE.length)) MINE=ALL;
        if(damnCode<MINE.length) return MINE[damnCode];
        return MINE[MINE.length-1];
    }

    public String armorStr(int armor){
        return (armor<0)?armorDescs()[0]:(
               (armor>=ARMOR_CEILING)?armorDescs()[armorDescs().length-1]+(CMStrings.repeat("!",(armor-ARMOR_CEILING)/100))+" ("+armor+")":(
                       armorDescs()[(int)Math.round(Math.floor(CMath.mul(CMath.div(armor,ARMOR_CEILING),armorDescs().length)))]+" ("+armor+")"));}
    public String fightingProwessStr(int prowess){
        return (prowess<0)?prowessDescs()[0]:(
               (prowess>=PROWESS_CEILING)?prowessDescs()[prowessDescs().length-1]+(CMStrings.repeat("!",(prowess-PROWESS_CEILING)/100))+" ("+prowess+")":(
                prowessDescs()[(int)Math.round(Math.floor(CMath.mul(CMath.div(prowess,PROWESS_CEILING),prowessDescs().length)))]+" ("+prowess+")"));}
    
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

    public String[] healthDescs(){return DEFAULT_HEALTH_CHART;}
    public String standardMobCondition(MOB mob)
    {
        int pct=(int)Math.round(Math.floor((CMath.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()))*10));
        if(pct<0) pct=0;
        if(pct>=healthDescs().length) pct=healthDescs().length-1;
        return CMStrings.replaceAll(healthDescs()[pct],"<MOB>",mob.name());
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

    public void handleBeingDamaged(CMMsg msg)
    {
        if(!(msg.target() instanceof MOB)) return;
        MOB attacker=msg.source();
        MOB target=(MOB)msg.target();
        int dmg=msg.value();
        synchronized(target)
        {
            if((dmg>0)&&(target.curState().getHitPoints()>0))
            {
                if((!target.curState().adjHitPoints(-dmg,target.maxState()))
                &&(target.curState().getHitPoints()<1)
                &&(target.location()!=null))
                    CMLib.combat().postDeath(attacker,target,msg);
                else
                if((target.curState().getHitPoints()<target.getWimpHitPoint())
                &&(target.getWimpHitPoint()>0)
                &&(target.isInCombat()))
                    CMLib.combat().postPanic(target,msg);
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
    
    public void handleExperienceChange(CMMsg msg)
    {
        MOB mob=msg.source();
        if(!CMSecurity.isDisabled("EXPERIENCE")
        &&!mob.charStats().getCurrentClass().expless()
        &&!mob.charStats().getMyRace().expless())
        {
            MOB expFromKilledmob=null;
            if(msg.target() instanceof MOB)
                expFromKilledmob=(MOB)msg.target();

            if(msg.value()>=0)
                mob.charStats().getCurrentClass().gainExperience(mob,
                                                                 expFromKilledmob,
                                                                 msg.targetMessage(),
                                                                 msg.value(),
                                                                 CMath.s_bool(msg.othersMessage()));
            else
                mob.charStats().getCurrentClass().loseExperience(mob,-msg.value());
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
                Vector channels=CMLib.channels().getFlaggedChannelNames("DETAILEDDEATHS");
                Vector channels2=CMLib.channels().getFlaggedChannelNames("DEATHS");
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
                CMLib.combat().justDie(killer,deadmob);
                if((!deadmob.isMonster())&&(deadmob.soulMate()==null)&&(killer!=deadmob)&&(!killer.isMonster()))
                    CMLib.coffeeTables().bump(deadmob,CoffeeTableRow.STAT_PKDEATHS);
                if((deadmob.getClanID().length()>0)
                &&(killer.getClanID().length()>0)
                &&(!deadmob.getClanID().equals(killer.getClanID())))
                {
                    Clan C=CMLib.clans().getClan(killer.getClanID());
                    if(C!=null) C.recordClanKill();
                }
            }
            else
                CMLib.combat().justDie(null,deadmob);
            deadmob.tell(deadmob,msg.target(),msg.tool(),msg.sourceMessage());
            if(deadmob.riding()!=null) deadmob.riding().delRider(deadmob);
        }
    }
    
    public void handleObserveDeath(MOB observer, MOB fighting, CMMsg msg)
    {
        MOB deadmob=msg.source();
        if(fighting==deadmob)
        {
            Room R=observer.location();
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
                if((M!=observer)
                &&(M!=deadmob)
                &&(M!=null)
                &&(hisGroupH.contains(M)
                    ||((M.getVictim()!=null)&&(myGroupH.contains(M.getVictim()))))
                &&(!M.amDead())
                &&(CMLib.flags().isInTheGame(M,true)))
                {
                    newTargetM=M;
                    break;
                }
            }
            observer.setVictim(newTargetM);
        }
    }
    
    public void handleBeingAssaulted(CMMsg msg)
    {
        if(!(msg.target() instanceof MOB)) return;
        MOB attacker=msg.source();
        MOB target=(MOB)msg.target();
        
        if((!target.isInCombat())
        &&(target.location().isInhabitant(attacker))
        &&((!CMath.bset(msg.sourceCode(),CMMsg.MASK_GENERAL))
            ||(!(msg.tool() instanceof DiseaseAffect))))
        {
            establishRange(target,attacker,msg.tool());
            target.setVictim(attacker);
        }
        if(target.isInCombat())
        {
            if((!target.isMonster())&&(!attacker.isMonster()))
                attacker.session().setLastPKFight();
            if(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
            {
                Item weapon=attacker.myNaturalWeapon();
                if((msg.tool()!=null)&&(msg.tool() instanceof Item))
                    weapon=(Item)msg.tool();
                if(weapon!=null)
                {
                    CMLib.combat().postWeaponDamage(attacker,target,weapon,CMLib.combat().rollToHit(attacker,target));
                    msg.setValue(1);
                }
                if((target.soulMate()==null)&&(target.playerStats()!=null)&&(target.location()!=null))
                    target.playerStats().adjHygiene(PlayerStats.HYGIENE_FIGHTDIRTY);
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
        if((source==null)||(target==null)) return;
        if(source==target) return;
        if((target==observer)||(source==observer)) return;
        if((target.location()!=observer.location())||(target.location()!=source.location()))
            return;
        if((CMath.bset(observer.getBitmap(),MOB.ATT_AUTOASSIST))) return;
        if(observer.isInCombat()) return;

        if((observer.amFollowing()==target)
        ||(target.amFollowing()==observer)
        ||((target.amFollowing()!=null)&&(target.amFollowing()==observer.amFollowing())))
        {
            observer.setVictim(source);
            establishRange(observer,source,observer.fetchWieldedItem());
        }
        else
        if((observer.amFollowing()==source)
        ||(source.amFollowing()==observer)
        ||((source.amFollowing()!=null)&&(source.amFollowing()==observer.amFollowing())))
        {
            observer.setVictim(target);
            establishRange(observer,target,observer.fetchWieldedItem());
        }
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
                    if((otherMOB!=null)
                       &&(otherMOB!=source)
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
        if((weapon!=null)&&(weapon.amWearingAt(Item.INVENTORY)))
            weapon=fighter.fetchWieldedItem();
        if((!CMath.bset(fighter.getBitmap(),MOB.ATT_AUTOMELEE)))
            CMLib.combat().postAttack(fighter,fighter.getVictim(),weapon);
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
            if((weapon!=null)&&inminrange&&inmaxrange)
                CMLib.combat().postAttack(fighter,fighter.getVictim(),weapon);
        }
    }
    
    protected double subtickBeforeAttack(MOB fighter, double actions)
    {
        if(CMProps.getIntVar(CMProps.SYSTEMI_COMBATSYSTEM)!=CombatLibrary.COMBAT_DEFAULT)
            while((actions>=1.0)&&(fighter.commandQueSize()>0))
            {
                if(fighter.session()!=null)
                    fighter.session().dequeCommand();
                else
                    fighter.dequeCommand();
                actions=actions-1.0;
            }
        return actions;
    }
    
    protected double subtickAfterAttack(MOB fighter, double actions)
    {
        if(CMProps.getIntVar(CMProps.SYSTEMI_COMBATSYSTEM)==CombatLibrary.COMBAT_DEFAULT)
            if(fighter.session()!=null)
                fighter.session().dequeCommand();
            else
                fighter.dequeCommand();
        
        MOB target=fighter.getVictim();
        if(!fighter.isMonster())
        {
            if((target!=null)&&(!target.amDead())&&(CMLib.flags().canBeSeenBy(target,fighter)))
                fighter.session().print(target.healthText()+"\n\r\n\r");
        }
        else
        if((target!=null)
        &&((fighter.amFollowing()==null)||(fighter.amFollowing().isMonster()))
        &&(target.isMonster())
        &&(!target.amDead())
        &&(CMLib.dice().rollPercentage()<33)
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
            if(nextVictimM!=null)
                fighter.setVictim(nextVictimM);
        }
        return actions;
    }
    
    public double tickCombat(MOB fighter, double actions)
    {
        Item weapon=fighter.fetchWieldedItem();

        if((CMath.bset(fighter.getBitmap(),MOB.ATT_AUTODRAW))&&(weapon==null))
        {
            CMLib.commands().postDraw(fighter,false,true);
            weapon=fighter.fetchWieldedItem();
        }

        actions=subtickBeforeAttack(fighter,actions);
        
        int folrange=(CMath.bset(fighter.getBitmap(),MOB.ATT_AUTOMELEE)
                        &&(fighter.amFollowing()!=null)
                        &&(fighter.amFollowing().getVictim()==fighter.getVictim())
                        &&(fighter.amFollowing().rangeToTarget()>=0)
                        &&(fighter.amFollowing().fetchFollowerOrder(fighter)>=0))?
                                fighter.amFollowing().fetchFollowerOrder(fighter)+fighter.amFollowing().rangeToTarget():-1;
        if(CMLib.flags().aliveAwakeMobile(fighter,true))
        {
            int numAttacks=(int)Math.round(Math.floor(actions));
            for(int s=0;s<numAttacks;s++)
            {
                if((!fighter.amDead())
                &&(fighter.curState().getHitPoints()>0)
                &&(fighter.isInCombat())
                &&((s==0)||(CMLib.flags().isStanding(fighter))))
                    subtickAttack(fighter,weapon,folrange);
                else
                    break;
            }

            if(CMLib.dice().rollPercentage()>(fighter.charStats().getStat(CharStats.CONSTITUTION)*4))
                fighter.curState().adjMovement(-1,fighter.maxState());
        }

        actions=subtickAfterAttack(fighter,actions);
        
        return actions;
    }
}
