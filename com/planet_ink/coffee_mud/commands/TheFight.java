package com.planet_ink.coffee_mud.commands;

import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.Abilities.*;
import com.planet_ink.coffee_mud.Items.Weapons.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.sysop.SysopItemUsage;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.db.*;
import java.io.*;
import java.util.*;
public class TheFight
{

	public static void kill(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell("Kill whom?");
		}
		boolean reallyKill=false;
		String whomToKill=CommandProcessor.combine(commands,1);
		if(mob.isASysOp()&&(!mob.isMonster()))
		{
			if(((String)commands.elementAt(commands.size()-1)).equalsIgnoreCase("DEAD"))
			{
				commands.removeElementAt(commands.size()-1);
				whomToKill=CommandProcessor.combine(commands,1);
				reallyKill=true;
			}
		}

		MOB target=mob.location().fetchInhabitant(whomToKill);
		if((target==null)||((target!=null)&&(!Sense.canBeSeenBy(target,mob))))
		{
			mob.tell("I don't see that here.");
			return;
		}
		if(reallyKill)
		{
			FullMsg msg=new FullMsg(mob,target,null,Affect.STRIKE_MAGIC,"<S-NAME> touch <T-NAME>.",Affect.STRIKE_MAGIC,"<S-NAME> touches <T-NAME>.",Affect.VISUAL_WNOISE,"<S-NAME> touches <T-NAME>.");
			mob.location().send(mob,msg);
			doDamage(target,target.curState().getHitPoints()+10);
		}
		else
		{
			Item weapon=mob.fetchWieldedItem();
			FullMsg msg=new FullMsg(mob,target,weapon,Affect.STRIKE_HANDS,null,Affect.STRIKE_HANDS,null,Affect.VISUAL_WNOISE,null);
			mob.location().send(mob,msg);
		}

	}

	public static String mobCondition(MOB mob)
	{
		double pct=(Util.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));
		if(pct<.10)
			return "is hovering on deaths door.";
		else
		if(pct<.20)
			return "is covered in blood.";
		else
		if(pct<.30)
			return "is bleeding badly from lots of wounds.";
		else
		if(pct<.40)
			return "has numerous bloody wounds and gashes.";
		else
		if(pct<.50)
			return "has some bloody wounds and gashes.";
		else
		if(pct<.60)
			return "has a few bloody wounds.";
		else
		if(pct<.70)
			return "is cut and bruised.";
		else
		if(pct<.80)
			return "has some minor cuts and bruises.";
		else
		if(pct<.90)
			return "has a few bruises and scratches.";
		else
		if(pct<.99)
			return "has a few small bruises.";
		else
			return "is in perfect health";

	}

	public static Hashtable allPossibleCombatants(MOB mob)
	{
		Hashtable h=new Hashtable();
		Room thisRoom=mob.location();
		if(thisRoom==null) return null;
		Hashtable h1=Grouping.getAllFollowers(mob);
		for(int m=0;m<thisRoom.numInhabitants();m++)
		{
			MOB inhab=thisRoom.fetchInhabitant(m);
			if((inhab!=mob)
			&&(h1.get(inhab.ID())==null))
				h.put(inhab.ID(),inhab);
		}
		return h;
	}

	public static Hashtable allCombatants(MOB mob)
	{
		Hashtable h=new Hashtable();
		Room thisRoom=mob.location();
		if(thisRoom==null) return null;
		if(!mob.isInCombat()) return null;

		Hashtable h1=Grouping.getAllFollowers(mob);

		for(int m=0;m<thisRoom.numInhabitants();m++)
		{
			MOB inhab=thisRoom.fetchInhabitant(m);
			if((inhab==mob.getVictim())
			||((inhab!=mob)
			&&(h1.get(inhab.ID())==null)))
				h.put(inhab.ID(),inhab);
		}
		return h;

	}


	public static long adjustedAttackBonus(MOB mob)
	{
		return	Math.round(mob.envStats().attackAdjustment()
				+(Math.floor(new Integer(mob.charStats().getStrength()).doubleValue())-6)*2)
				-((mob.curState().getHunger()<1)?10:0)
				-((mob.curState().getThirst()<1)?10:0);
	}

	public static long adjustedArmor(MOB mob)
	{
		return  Math.round(mob.envStats().armor()
				-(Math.floor(new Integer(mob.charStats().getDexterity()).doubleValue())-6)*2)
				+((mob.curState().getHunger()<1)?10:0)
				+((mob.curState().getThirst()<1)?10:0)
				+((Sense.isSitting(mob))?25:0)
				+((Sense.isSleeping(mob))?25:0);
	}


	public static boolean isHit(MOB attacker, MOB target)
	{
		if(attacker==null) return false;
		if(attacker.amDead()) return false;
		if(target==null) return false;
		if(target.amDead()) return false;

		long adjArmor=adjustedArmor(target)*(Sense.canBeSeenBy(attacker,target)?1:2);
		long adjAttack=Math.round(adjustedAttackBonus(attacker)*(Sense.canBeSeenBy(target,attacker)?1:.5));

		int attChance=(int)(adjArmor+adjAttack);
		if(attChance>95)
			attChance=95;
		else
		if(attChance<5)
			attChance=5;
		return (Dice.rollPercentage()<attChance);
	}

	public static void doAttack(MOB attacker, MOB target, Item weapon)
	{
		if(weapon==null) return;
		boolean hit=isHit(attacker,target);
		weapon.strike(attacker,target,hit);
	}

	public static void postAttack(MOB attacker, MOB target, Item weapon)
	{
		if(attacker==null) return;
		if(attacker.amDead()) return;
		if(target==null) return;
		if(target.amDead()) return;

		FullMsg msg=new FullMsg(attacker,target,weapon,Affect.STRIKE_HANDS,null,Affect.STRIKE_HANDS,null,Affect.VISUAL_WNOISE,null);
		if(target.location().okAffect(msg))
			target.location().send(attacker,msg);
	}

	public static void doDamage(MOB target, int damageAmount)
	{
		if(!target.curState().adjHitPoints(-damageAmount,target.maxState()))
		{
			if(target.location()!=null)
			{
				Room deathRoom=target.location();
				int numBeneficiaries=0;
				int expAmount=50;
				int totalLevels=0;
				for(int m=0;m<deathRoom.numInhabitants();m++)
				{
					MOB mob=deathRoom.fetchInhabitant(m);
					if(((mob.getVictim()==target)&&(!mob.amDead())&&(!mob.isMonster())))
					{
						numBeneficiaries++;
						expAmount+=10;
						totalLevels+=mob.envStats().level();
					}
				}
				if(numBeneficiaries>0)
				{
					for(int m=0;m<deathRoom.numInhabitants();m++)
					{
						MOB mob=deathRoom.fetchInhabitant(m);
						if(((mob.getVictim()==target)&&(!mob.amDead())&&(!mob.isMonster())))
							if(mob.charStats().getMyClass()!=null)
							{
								int myAmount=(int)Math.round(Util.mul(expAmount,Util.div(totalLevels,mob.envStats().level())));
								if(myAmount>100) myAmount=100;
								mob.charStats().getMyClass().gainExperience(mob,target,myAmount);
							}
					}
				}
				while(target.numFollowers()>0)
					target.fetchFollower(0).setFollowing(null);
				target.setFollowing(null);
				deathRoom.delInhabitant(target);
				deathRoom.show(target,null,Affect.VISUAL_WNOISE,target.name()+" is DEAD!!!\n\r");
				if(!target.isMonster())
				{
					int expLost=100;
					target.setExperience(target.getExperience()-expLost);
					target.tell("You lose 100 experience points.");
				}
				DeadBody Body=new DeadBody();
				Body.baseEnvStats().setWeight(target.envStats().weight());
				if(!target.isMonster())
					Body.baseEnvStats().setRejuv(Body.baseEnvStats().rejuv()*4);
				deathRoom.addItem(Body);
				Body.recoverEnvStats();
				Coins C=new Coins();
				C.baseEnvStats().setAbility(target.getMoney());
				C.recoverEnvStats();
				C.setLocation(Body);
				deathRoom.addItem(C);
				target.setMoney(0);
				for(int i=0;i<target.inventorySize();)
				{
					Item thisItem=target.fetchInventory(i);
					if(target.isMonster())
					{
						Item newItem=(Item)thisItem.copyOf();
						newItem.setLocation(null);
						newItem.recoverEnvStats();
						thisItem=newItem;
						i++;
					}
					else
						target.delInventory(thisItem);
					thisItem.remove();
					if(thisItem.location()==null)
						thisItem.setLocation(Body);
					deathRoom.addItem(thisItem);
				}
				target.kill();
				Body.setName("the body of "+target.name());
				Body.setSecretIdentity(target.name()+"/"+target.description());
				Body.setDisplayText("the body of "+target.name()+" lies here.");
				if((!target.isMonster())||(target.soulMate()!=null))
				{
					if(target.soulMate()==null)
						target.raiseFromDead();
					else
					{
						Archon_Possess.dispossess(target);
						Body.startTicker(deathRoom);
					}
				}
				else
					Body.startTicker(deathRoom);
				deathRoom.recoverRoomStats();
			}
		}
		else
		if(target.curState().getHitPoints()<target.getWimpHitPoint())
			Movement.flee(target,"");
	}



	public static String hitString(int weaponType, int damageAmount, String weaponName)
	{
		if((weaponType!=Weapon.TYPE_NATURAL)||(weaponName==null)||(weaponName.length()==0))
			return "<S-NAME> "+hitWord(weaponType,damageAmount)+" <T-NAME> with "+weaponName;
		else
			return "<S-NAME> "+hitWord(weaponType,damageAmount)+" <T-NAME>";
	}

	public static String hitWord(int weaponType, int damageAmount)
	{
		switch(weaponType)
		{
		case Weapon.TYPE_BASHING:
			if(damageAmount<3)
				return "scratch(es)";
			else
			if(damageAmount<6)
				return "graze(s)";
			else
			if(damageAmount<9)
				return "hit(s)";
			else
			if(damageAmount<13)
				return "smash(es)";
			else
			if(damageAmount<25)
				return "bash(es)";
			else
			if(damageAmount<35)
				return "crush(es)";
			else
			if(damageAmount<50)
				return "crunch(es)";
			else
			if(damageAmount<75)
				return "MASSACRE(S)";
			else
			if(damageAmount<100)
				return "DESTROY(S)";
			else
				return "OBLITERATE(S)";
		case Weapon.TYPE_NATURAL:
			if(damageAmount<3)
				return "graze(s)";
			else
			if(damageAmount<6)
				return "hit(s)";
			else
			if(damageAmount<9)
				return "pound(s)";
			else
			if(damageAmount<13)
				return "pummel(s)";
			else
			if(damageAmount<25)
				return "claw(s)";
			else
			if(damageAmount<35)
				return "rip(s)";
			else
			if(damageAmount<50)
				return "gore(s)";
			else
			if(damageAmount<75)
				return "MASSACRE(S)";
			else
			if(damageAmount<100)
				return "DESTROY(S)";
			else
				return "OBLITERATE(S)";
		case Weapon.TYPE_SLASHING:
			if(damageAmount<3)
				return "scratch(es)";
			else
			if(damageAmount<6)
				return "graze(s)";
			else
			if(damageAmount<9)
				return "wound(s)";
			else
			if(damageAmount<13)
				return "cut(s)";
			else
			if(damageAmount<25)
				return "slice(s)";
			else
			if(damageAmount<35)
				return "pierce(s)";
			else
			if(damageAmount<50)
				return "decimate(s)";
			else
			if(damageAmount<75)
				return "MASSACRE(S)";
			else
			if(damageAmount<100)
				return "DESTROY(S)";
			else
				return "OBLITERATE(S)";
		case Weapon.TYPE_PIERCING:
			if(damageAmount<3)
				return "scratch(es)";
			else
			if(damageAmount<6)
				return "graze(s)";
			else
			if(damageAmount<9)
				return "prick(s)";
			else
			if(damageAmount<13)
				return "cut(s)";
			else
			if(damageAmount<25)
				return "stab(s)";
			else
			if(damageAmount<35)
				return "pierce(s)";
			else
			if(damageAmount<50)
				return "decimate(s)";
			else
			if(damageAmount<75)
				return "MASSACRE(S)";
			else
			if(damageAmount<100)
				return "DESTROY(S)";
			else
				return "OBLITERATE(S)";
		case Weapon.TYPE_BURNING:
			if(damageAmount<3)
				return "warm(s)";
			else
			if(damageAmount<6)
				return "heat(s)";
			else
			if(damageAmount<9)
				return "singe(s)";
			else
			if(damageAmount<13)
				return "burn(s)";
			else
			if(damageAmount<25)
				return "flame(s)";
			else
			if(damageAmount<35)
				return "scorch(es)";
			else
			if(damageAmount<50)
				return "decimate(s)";
			else
			if(damageAmount<75)
				return "MASSACRE(S)";
			else
			if(damageAmount<100)
				return "DESTROY(S)";
			else
				return "OBLITERATE(S)";
		case Weapon.TYPE_BURSTING:
			if(damageAmount<3)
				return "scratch(es)";
			else
			if(damageAmount<6)
				return "graze(s)";
			else
			if(damageAmount<9)
				return "wound(s)";
			else
			if(damageAmount<13)
				return "cut(s)";
			else
			if(damageAmount<25)
				return "badly damage(s)";
			else
			if(damageAmount<35)
				return "murder(s)";
			else
			if(damageAmount<50)
				return "decimate(s)";
			else
			if(damageAmount<75)
				return "MASSACRE(S)";
			else
			if(damageAmount<100)
				return "DESTROY(S)";
			else
				return "OBLITERATE(S)";
		default:
			return hitWord(Weapon.TYPE_BURSTING,damageAmount);
		}
	}

	public static String armorStr(int armor)
	{
		if(armor < 0)
			return "nonexistant ("+armor+")";
		else
		if(armor < 15)
			return "covered ("+armor+")";
		else
		if(armor < 30)
			return "padded ("+armor+")";
		else
		if(armor < 45)
			return "heavily padded ("+armor+")";
		else
		if(armor < 60)
			return "protected ("+armor+")";
		else
		if(armor < 75)
			return "well protected ("+armor+")";
		else
		if(armor < 90)
			return "armored ("+armor+")";
		else
		if(armor < 105)
			return "well armored ("+armor+")";
		else
		if(armor < 120)
			return "heavily armored ("+armor+")";
		else
		if(armor < 135)
			return "unhittable ("+armor+")";
		else
			return "invincible! ("+armor+")";
	}
	public static String fightingProwessStr(int prowess)
	{
		if(prowess < 0)
			return "none ("+prowess+")";
		else
		if(prowess < 15)
			return "hardly any ("+prowess+")";
		else
		if(prowess < 30)
			return "very little ("+prowess+")";
		else
		if(prowess < 45)
			return "a little ("+prowess+")";
		else
		if(prowess < 60)
			return "some skill ("+prowess+")";
		else
		if(prowess < 75)
			return "skilled ("+prowess+")";
		else
		if(prowess < 90)
			return "very skilled ("+prowess+")";
		else
		if(prowess < 105)
			return "a master ("+prowess+")";
		else
		if(prowess < 120)
			return "dangerous ("+prowess+")";
		else
		if(prowess < 135)
			return "extremely dangerous ("+prowess+")";
		else
			return "you are death incarnate! ("+prowess+")";
	}
	
	public static String missString(int weaponType, String weaponName)
	{
			switch(weaponType)
			{
			case Weapon.TYPE_BASHING:
				return "<S-NAME> swing(s) at <T-NAME> and miss(es).";
			case Weapon.TYPE_NATURAL:
				return "<S-NAME> swing(s) at <T-NAME> and miss(es).";
			case Weapon.TYPE_SLASHING:
				return "<S-NAME> swing(s) at <T-NAME> and miss(es).";
			case Weapon.TYPE_PIERCING:
				return "<S-NAME> lunge(s) at <T-NAME> and miss(es).";
			case Weapon.TYPE_BURNING:
				return "<S-NAME> swing(s) at <T-NAME> and miss(es).";
			case Weapon.TYPE_BURSTING:
				return "<S-NAME> attack(s) <T-NAME> and miss(es).";
			default:
				return "<S-NAME> swing(s) at <T-NAME> and miss(es).";
			}
	}

}
