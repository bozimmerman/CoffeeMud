package com.planet_ink.coffee_mud.utils;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;
import java.io.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
			&&(Sense.canMove(inhab)||Sense.isSeen(inhab))
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

	public static HashSet allCombatants(MOB mob)
	{
		HashSet h=new HashSet();
		Room thisRoom=mob.location();
		if(thisRoom==null) return null;
		if(!mob.isInCombat()) return null;

		HashSet h1=mob.getGroupMembers(new HashSet());
		for(int m=0;m<thisRoom.numInhabitants();m++)
		{
			MOB inhab=thisRoom.fetchInhabitant(m);
			if((inhab!=null)
			&&((inhab==mob.getVictim())
				||((inhab!=mob)
					&&(inhab.getVictim()!=mob.getVictim())
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

	public static void postDeath(MOB source, MOB target, CMMsg addHere)
	{
		if(target==null) return;
		Room deathRoom=target.location();
		if(deathRoom==null) return;

		// make sure he's not already dead, or with a pending death.
		if(target.amDead()) return;
		if((addHere!=null)&&(addHere.trailerMsgs()!=null))
		for(int i=0;i<addHere.trailerMsgs().size();i++)
		{
			CMMsg msg=(CMMsg)addHere.trailerMsgs().elementAt(i);
			if((msg.source()==target)
			&&((msg.sourceMinor()==CMMsg.TYP_PANIC))
			   ||(msg.sourceMinor()==CMMsg.TYP_DEATH))
				return;
		}

		String msp=CommonStrings.msp("death"+Dice.roll(1,4,0)+".wav",50);
		FullMsg msg=new FullMsg(target,null,null,
			CMMsg.MSG_OK_VISUAL,"^F^*!!!!!!!!!!!!!!YOU ARE DEAD!!!!!!!!!!!!!!^?^.\n\r"+msp,
			CMMsg.MSG_OK_VISUAL,null,
			CMMsg.MSG_OK_VISUAL,"^F<S-NAME> is DEAD!!!^?\n\r"+msp);
		FullMsg msg2=new FullMsg(target,null,source,
			CMMsg.MSG_DEATH,null,
			CMMsg.MSG_DEATH,null,
			CMMsg.MSG_DEATH,null);
		if(addHere!=null)
		{
			if((deathRoom==null)||(deathRoom.okMessage(target,msg2)))
			{
				addHere.addTrailerMsg(msg);
				addHere.addTrailerMsg(msg2);
			}
		}
		else
		if((deathRoom!=null)&&(deathRoom.okMessage(target,msg)))
		{
			deathRoom.send(target,msg);
			if(deathRoom.okMessage(target,msg2))
				deathRoom.send(target,msg2);
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
		if(target.location().okMessage(attacker,msg))
		{
			target.location().send(attacker,msg);
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

	private static String replaceDamageTag(String str, int damage, int damageType)
	{
		if(str==null) return null;
		int replace=str.indexOf("<DAMAGE>");
		if(replace>=0)
		{
			if(!CommonStrings.getVar(CommonStrings.SYSTEM_SHOWDAMAGE).equalsIgnoreCase("YES"))
				return str.substring(0,replace)+CommonStrings.standardHitWord(damageType,damage)+str.substring(replace+8);
			else
				return str.substring(0,replace)+CommonStrings.standardHitWord(damageType,damage)+" ("+damage+")"+ str.substring(replace+8);
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
		if(allDisplayMessage!=null) allDisplayMessage="^F"+allDisplayMessage+"^?";
		FullMsg msg=new FullMsg(attacker,target,weapon,messageCode,CMMsg.MSG_DAMAGE,messageCode,allDisplayMessage);
		msg.setValue(damage);
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
		if(mob==null) return false;
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

	private static String pickMsg(String msg, String oldHit, String newHit)
	{
		if(msg==null) return null;
		if(msg.equals(oldHit)) return newHit;
		return oldHit;
	}

	public static void postWeaponDamage(MOB source, MOB target, Weapon weapon, boolean success)
	{
		if(source==null) return;
		if(!source.mayIFight(target)) return;
		int damageInt=source.adjustedDamage(weapon,target);
		if(success)
		{
            // calculate Base Damage (with Strength bonus)
			String oldHitString="^F"+weapon.hitString(damageInt)+"^?";
			FullMsg msg=new FullMsg(source,
									target,
									weapon,
									CMMsg.MSG_OK_VISUAL,
									CMMsg.MSG_DAMAGE,
									CMMsg.MSG_OK_VISUAL,
									oldHitString);

			msg.setValue(damageInt);
			// why was there no okaffect here?
			Room room=source.location();
			if((room!=null)&&(room.okMessage(source,msg)))
			{
				if(msg.targetMinor()==CMMsg.TYP_DAMAGE)
				{
					damageInt=msg.value();
					String newMsg="^F"+weapon.hitString(damageInt)+"^?";
					msg.modify(msg.source(),
							   msg.target(),
							   msg.tool(),
							   msg.sourceCode(),
							   pickMsg(msg.sourceMessage(),oldHitString,newMsg),
							   msg.targetCode(),
							   pickMsg(msg.targetMessage(),oldHitString,newMsg),
							   msg.othersCode(),
							   pickMsg(msg.othersMessage(),oldHitString,newMsg));
				}
				if((source.mayIFight(target))
				&&(source.location()==room)
				&&(target.location()==room))
					room.send(source,msg);
			}
		}
		else
		{
			FullMsg msg=new FullMsg(source,
									target,
									weapon,
									CMMsg.MSG_NOISYMOVEMENT,
									weapon.missString());
			// why was there no okaffect here?
			if(source.location().okMessage(source,msg))
				source.location().send(source,msg);
		}
	}

	public static void justDie(MOB source, MOB target)
	{
		if(target==null) return;
		Room deathRoom=target.location();

		HashSet beneficiaries=new HashSet();
		if((source!=null)&&(source.charStats()!=null))
		{
			CharClass C=source.charStats().getCurrentClass();
			MOB M=source;
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
			beneficiaries=C.dispenseExperience(source,target);
		}

		int deadMoney=target.getMoney();
		int myAmountOfDeadMoney=0;
		Vector goldLooters=new Vector();
		for(Iterator e=beneficiaries.iterator();e.hasNext();)
		{
			MOB M=(MOB)e.next();
			if(((Util.bset(M.getBitmap(),MOB.ATT_AUTOGOLD))
			&&(!goldLooters.contains(M)))
			&&(M.location()==deathRoom)
			&&(deathRoom.isInhabitant(M)))
			   goldLooters.addElement(M);
		}
		if((goldLooters.size()>0)&&(deadMoney>0))
		{
			myAmountOfDeadMoney=(int)Math.round(Util.div(deadMoney,goldLooters.size()));
			target.setMoney(0);
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
					target.tell("^F^*You lose "+expLost+" experience points.^?^.");
					postExperience(target,null,null,-expLost,false);
				}
				else
				if(whatToDo.length()<3)
					continue;
				else
				{
					int expLost=100*target.envStats().level();
					target.tell("^F^*You lose "+expLost+" experience points.^?^.");
					postExperience(target,null,null,-expLost,false);
				}
			}
		}
		if(Body==null) Body=target.killMeDead(true);

		if((!target.isMonster())&&(Dice.rollPercentage()==1))
		{
			Ability A=CMClass.getAbility("Disease_Amnesia");
			if((A!=null)&&(target.fetchEffect(A.ID())==null))
				A.invoke(target,target,true);
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
		
		if((deadMoney>0)&&(myAmountOfDeadMoney>0))
		for(int g=0;g<goldLooters.size();g++)
		{
			Item C=CMClass.getItem("StdCoins");
			C.baseEnvStats().setAbility(myAmountOfDeadMoney);
			C.setContainer(Body);
			C.recoverEnvStats();
			deathRoom.addItemRefuse(C,Item.REFUSE_MONSTER_EQ);
			deathRoom.recoverRoomStats();
			MOB mob=(MOB)goldLooters.elementAt(g);
			if((mob.riding()!=null)&&(mob.riding() instanceof MOB))
				mob.tell("You'll need to dismount to get gold off the body.");
			else
			if((mob.riding()!=null)&&(mob.riding() instanceof MOB))
				mob.tell("You'll need to disembark to get gold off the body.");
			else
			if(Sense.canBeSeenBy(Body,mob))
				CommonMsgs.get(mob,Body,C,false);
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
		}
	}

}
