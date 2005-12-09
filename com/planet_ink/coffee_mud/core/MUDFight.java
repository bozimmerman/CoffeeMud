package com.planet_ink.coffee_mud.utils;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
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
public class MUDFight
{
	private MUDFight(){};

	public static final int COMBAT_DEFAULT=0;
	public static final int COMBAT_QUEUE=1;


	public static HashSet allPossibleCombatants(MOB mob, boolean beRuthless)
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
			&&(Sense.isSeen(inhab)||Sense.canMove(inhab))
			&&(Sense.isSeen(inhab)||(!Sense.isCloaked(inhab)))
			&&((beRuthless)||(!mob.isMonster())||(!inhab.isMonster())))
				h.add(inhab);
		}
		return h;
	}

	public static HashSet properTargets(Ability A, MOB caster, boolean beRuthless)
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

    public static boolean rollToHit(MOB attacker, MOB defender)
    {
        if((attacker==null)||(defender==null)) return false;
        return rollToHit(attacker.adjustedAttackBonus(defender),defender.adjustedArmor());
    }
    
    public static boolean rollToHit(int attack, int defense)
    {
        double myArmor=new Integer(-(defense-100)).doubleValue();
        if(myArmor==0) myArmor=1.0;
        else
        if(myArmor<0.0) myArmor=-Util.div(1.0,myArmor);
        double hisAttack=new Integer(attack+50).doubleValue();
        if(hisAttack==0.0) hisAttack=1.0;
        else
        if(hisAttack<0.0) hisAttack=-Util.div(1.0,myArmor);
        return Dice.normalizeAndRollLess((int)Math.round(50.0*(hisAttack/myArmor)));
    }
	public static HashSet allCombatants(MOB mob)
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
					&&(Sense.isSeen(inhab)||(!Sense.isCloaked(inhab)))
					&&(Sense.canMove(inhab)||Sense.isSeen(inhab))
					&&(!h1.contains(inhab)))))
			 	h.add(inhab);
		}
		return h;

	}

	public static void makePeaceInGroup(MOB mob)
	{
		HashSet myGroup=mob.getGroupMembers(new HashSet());
		for(Iterator e=myGroup.iterator();e.hasNext();)
		{
			MOB mob2=(MOB)e.next();
			if(mob2.isInCombat()&&(myGroup.contains(mob2.getVictim())))
				mob2.makePeace();
		}
	}

	public static void postPanic(MOB mob, CMMsg addHere)
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
		FullMsg msg=new FullMsg(mob,null,CMMsg.MSG_PANIC,null);
		if(addHere!=null)
			addHere.addTrailerMsg(msg);
		else
		if((mob.location()!=null)&&(mob.location().okMessage(mob,msg)))
			mob.location().send(mob,msg);
	}

	public static void postDeath(MOB killerM, MOB deadM, CMMsg addHere)
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

		String msp=CommonStrings.msp("death"+Dice.roll(1,7,0)+".wav",50);
		FullMsg msg=new FullMsg(deadM,null,null,
			CMMsg.MSG_OK_VISUAL,"^f^*^<FIGHT^>!!!!!!!!!!!!!!YOU ARE DEAD!!!!!!!!!!!!!!^</FIGHT^>^?^.\n\r"+msp,
			CMMsg.MSG_OK_VISUAL,null,
			CMMsg.MSG_OK_VISUAL,"^F^<FIGHT^><S-NAME> is DEAD!!!^</FIGHT^>^?\n\r"+msp);
		FullMsg msg2=new FullMsg(deadM,null,killerM,
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

	public static boolean postAttack(MOB attacker, MOB target, Item weapon)
	{
		if((attacker==null)||(!attacker.mayPhysicallyAttack(target)))
			return false;

		if((weapon==null)
		&&(Util.bset(attacker.getBitmap(),MOB.ATT_AUTODRAW)))
		{
			CommonMsgs.draw(attacker,false,true);
			weapon=attacker.fetchWieldedItem();
		}
		FullMsg msg=new FullMsg(attacker,target,weapon,CMMsg.MSG_WEAPONATTACK,null);
        Room R=target.location();
		if(R.okMessage(attacker,msg))
		{
			R.send(attacker,msg);
			return msg.value()>0;
		}
		return false;
	}

	public static boolean postHealing(MOB healer,
									  MOB target,
									  Environmental tool,
									  int messageCode,
									  int healing,
									  String allDisplayMessage)
	{
		if(healer==null) healer=target;
		if((healer==null)||(target==null)||(target.location()==null)) return false;
		FullMsg msg=new FullMsg(healer,target,tool,messageCode,CMMsg.MSG_HEALING,messageCode,allDisplayMessage);
		msg.setValue(healing);
		if(target.location().okMessage(target,msg))
		{ target.location().send(target,msg); return true;}
		return false;
	}

	public static String replaceDamageTag(String str, int damage, int damageType)
	{
		if(str==null) return null;
		int replace=str.indexOf("<DAMAGE>");
		if(replace>=0)
		{
			if(!CommonStrings.getVar(CommonStrings.SYSTEM_SHOWDAMAGE).equalsIgnoreCase("YES"))
				return str.substring(0,replace)+CommonStrings.standardHitWord(damageType,damage)+str.substring(replace+8);
			return str.substring(0,replace)+CommonStrings.standardHitWord(damageType,damage)+" ("+damage+")"+ str.substring(replace+8);
		}
        replace=str.indexOf("<DAMAGES>");
        if(replace>=0)
        {
            String hitWord=CommonStrings.standardHitWord(damageType,damage);
            hitWord=Util.replaceAll(hitWord,"(","");
            hitWord=Util.replaceAll(hitWord,")","");
            if(!CommonStrings.getVar(CommonStrings.SYSTEM_SHOWDAMAGE).equalsIgnoreCase("YES"))
                return str.substring(0,replace)+hitWord+str.substring(replace+9);
            return str.substring(0,replace)+hitWord+" ("+damage+")"+ str.substring(replace+9);
        }
		return str;
	}

	public static void postDamage(MOB attacker,
								  MOB target,
								  Environmental weapon,
								  int damage,
								  int messageCode,
								  int damageType,
								  String allDisplayMessage)
	{
		if((attacker==null)||(target==null)||(target.location()==null)) return;
		if(allDisplayMessage!=null) allDisplayMessage="^F^<FIGHT^>"+allDisplayMessage+"^</FIGHT^>^?";
		FullMsg msg=new FullMsg(attacker,target,weapon,messageCode,CMMsg.MSG_DAMAGE,messageCode,allDisplayMessage);
		msg.setValue(damage);
        CMColor.fixSourceFightColor(msg);
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

	public static boolean postExperience(MOB mob,
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
		FullMsg msg=new FullMsg(mob,victim,null,CMMsg.MASK_GENERAL|CMMsg.TYP_EXPCHANGE,null,CMMsg.NO_EFFECT,homage,CMMsg.NO_EFFECT,""+quiet);
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

    public static boolean changeFactions(MOB mob,
                                         MOB victim,
                                         int amount,
                                         boolean quiet)
    {
        if((mob==null))
            return false;
        FullMsg msg=new FullMsg(mob,victim,null,CMMsg.MASK_GENERAL|CMMsg.TYP_FACTIONCHANGE,null,CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,""+quiet);
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

	public static void postWeaponDamage(MOB source, MOB target, Item item, boolean success)
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
								CommonStrings.standardHitString(Weapon.CLASS_BLUNT,damageInt,item.name()))+"^</FIGHT^>^?";
			FullMsg msg=new FullMsg(source,
									target,
									item,
									CMMsg.MSG_OK_VISUAL,
									CMMsg.MSG_DAMAGE,
									CMMsg.MSG_OK_VISUAL,
									oldHitString);
            CMColor.fixSourceFightColor(msg);

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
								CommonStrings.standardMissString(Weapon.TYPE_BASHING,Weapon.CLASS_BLUNT,item.name(),false))+"^</FIGHT^>^?";
			FullMsg msg=new FullMsg(source,
									target,
									weapon,
									CMMsg.MSG_NOISYMOVEMENT,
									missString);
            CMColor.fixSourceFightColor(msg);
			// why was there no okaffect here?
			if((source.location().okMessage(source,msg))
            &&(source.location()!=null))
				source.location().send(source,msg);
		}
	}

	public static void processFormation(Vector[] done, MOB leader, int level)
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

	public static MOB getFollowedLeader(MOB mob)
	{
		int tries=0;
		MOB leader=mob;
		while((leader.amFollowing()!=null)&&(((++tries)<1000)))
			leader=leader.amFollowing();
	    return leader;
	}

	public static Vector[] getFormation(MOB mob)
	{
		int tries=0;
		MOB leader=mob;
		while((leader.amFollowing()!=null)&&(((++tries)<1000)))
			leader=leader.amFollowing();
		Vector[] done=new Vector[20];
		processFormation(done,leader,0);
	    return done;
	}

	public static Vector getFormationFollowed(MOB mob)
	{
	    Vector[] form=MUDFight.getFormation(mob);
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

	public static int getFormationAbsOrder(MOB mob)
	{
	    Vector[] form=MUDFight.getFormation(mob);
	    for(int i=1;i<form.length;i++)
	    {
	        if((form[i]!=null)&&(form[i].contains(mob)))
	            return i;
	    }
	    return 0;
	}
    
    public static CharClass getCombatDominantClass(MOB killer, MOB killed)
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

    public static HashSet getCombatBeneficiaries(MOB killer, MOB killed, CharClass combatCharClass)
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
    
    
	public static DeadBody justDie(MOB source, MOB target)
	{
		if(target==null) return null;
		Room deathRoom=target.location();

        CharClass combatCharClass=getCombatDominantClass(source,target);
		HashSet beneficiaries=getCombatBeneficiaries(source,target,combatCharClass);
        combatCharClass.dispenseExperience(beneficiaries,target);
        
	    String currency=BeanCounter.getCurrency(target);
		double deadMoney=BeanCounter.getTotalAbsoluteValue(target,currency);
		double myAmountOfDeadMoney=0.0;
		Vector goldLooters=new Vector();
		for(Iterator e=beneficiaries.iterator();e.hasNext();)
		{
			MOB M=(MOB)e.next();
			if(((Util.bset(M.getBitmap(),MOB.ATT_AUTOGOLD))
			&&(!goldLooters.contains(M)))
			&&(M!=target)
			&&(M.location()==deathRoom)
			&&(deathRoom.isInhabitant(M)))
			   goldLooters.addElement(M);
		}
		if((goldLooters.size()>0)&&(deadMoney>0))
		{
			myAmountOfDeadMoney=Util.div(deadMoney,goldLooters.size());
			BeanCounter.subtractMoney(target,deadMoney);
		}

		DeadBody Body=null;
		if((target.soulMate()==null)&&(!target.isMonster()))
		{
			Vector whatsToDo=Util.parse(CommonStrings.getVar(CommonStrings.SYSTEM_PLAYERDEATH));
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
                        Ability A=target.fetchAbility(Dice.roll(1,target.numLearnedAbilities(),-1));
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
					MOB deadMOB=CMMap.getLoadPlayer(target.Name());
					if(deadMOB!=null)
					{
						Body=target.killMeDead(true);
						CoffeeUtensils.obliteratePlayer(deadMOB,false);
					}
				}
				else
				if((whatToDo.trim().equals("0"))||(Util.s_int(whatToDo)>0))
				{
					int expLost=Util.s_int(whatToDo);
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

		if((!target.isMonster())&&(Dice.rollPercentage()==1))
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
			CommonMsgs.look(target.soulMate(),true);
			target.setSoulMate(null);
		}

		if((source!=null)
        &&(deathRoom!=null)
		&&(source.location()==deathRoom)
		&&(deathRoom.isInhabitant(source))
		&&(Util.bset(source.getBitmap(),MOB.ATT_AUTOLOOT)))
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
				&&(Sense.canBeSeenBy(Body,source))
				&&((!Body.destroyAfterLooting())||(!(item instanceof EnvResource)))
				&&(Sense.canBeSeenBy(item,source)))
					CommonMsgs.get(source,Body,item,false);
			}
			if(Body.destroyAfterLooting())
				deathRoom.recoverRoomStats();
		}

		Coins C=null;
		if((deadMoney>0)&&(myAmountOfDeadMoney>0))
		for(int g=0;g<goldLooters.size();g++)
		{
		    C=BeanCounter.makeBestCurrency(currency,myAmountOfDeadMoney,null,Body);
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
				if(Sense.canBeSeenBy(Body,mob))
					CommonMsgs.get(mob,Body,C,false);
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

}
