package com.planet_ink.coffee_mud.commands;

import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.commands.sysop.SysopItemUsage;
import java.io.*;
import java.util.*;
public class TheFight
{

	private Grouping grouping=new Grouping();
	public TheFight()
	{
	}
	
	public TheFight(Grouping grouper)
	{
		grouping=grouper;
	}
	public void kill(MOB mob, Vector commands)
	{
		if(mob.isInCombat())
			return;

		if(commands.size()<2)
		{
			mob.tell("Kill whom?");
		}
		boolean reallyKill=false;
		String whomToKill=Util.combine(commands,1);
		if(mob.isASysOp(mob.location())&&(!mob.isMonster()))
		{
			if(((String)commands.elementAt(commands.size()-1)).equalsIgnoreCase("DEAD"))
			{
				commands.removeElementAt(commands.size()-1);
				whomToKill=Util.combine(commands,1);
				reallyKill=true;
			}
		}

		MOB target=mob.location().fetchInhabitant(whomToKill);
		if((target==null)||((target!=null)&&(!Sense.canBeSeenBy(target,mob))))
		{
			mob.tell("I don't see '"+whomToKill+"' here.");
			return;
		}
		if(reallyKill)
		{
			FullMsg msg=new FullMsg(mob,target,null,Affect.MSG_OK_ACTION,"<S-NAME> touch(es) <T-NAMESELF>.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				doDamage(mob,target,target.curState().getHitPoints()+10);
			}
		}
		else
			postAttack(mob,target,mob.fetchWieldedItem());
	}

	public String mobCondition(MOB mob)
	{
		double pct=(Util.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));
	
		if(pct<.10)
			return "^r" + mob.name() + "^r is hovering on deaths door!^N";
		else
		if(pct<.20)
			return "^r" + mob.name() + "^r is covered in blood.^N";
		else
		if(pct<.30)
			return "^r" + mob.name() + "^r is bleeding badly from lots of wounds.^N";
		else
		if(pct<.40)
			return "^y" + mob.name() + "^y has numerous bloody wounds and gashes.^N";
		else
		if(pct<.50)
			return "^y" + mob.name() + "^y has some bloody wounds and gashes.^N";
		else
		if(pct<.60)
			return "^p" + mob.name() + "^p has a few bloody wounds.^N";
		else
		if(pct<.70)
			return "^p" + mob.name() + "^p is cut and bruised.^N";
		else
		if(pct<.80)
			return "^g" + mob.name() + "^g has some minor cuts and bruises.^N";
		else
		if(pct<.90)
			return "^g" + mob.name() + "^g has a few bruises and scratches.^N";
		else
		if(pct<.99)
			return "^g" + mob.name() + "^g has a few small bruises.^N";
		else
			return "^c" + mob.name() + "^c is in perfect health^N";
	}

	public Hashtable allPossibleCombatants(MOB mob)
	{
		Hashtable h=new Hashtable();
		Room thisRoom=mob.location();
		if(thisRoom==null) return null;
		Hashtable h1=grouping.getGroupMembers(mob);
		for(int m=0;m<thisRoom.numInhabitants();m++)
		{
			MOB inhab=thisRoom.fetchInhabitant(m);
			if((inhab!=null)
			&&(inhab!=mob)
			&&(h1.get(inhab)==null)
			&&((!mob.isMonster())||(!inhab.isMonster())))
				h.put(inhab,inhab);
		}
		return h;
	}

	public Hashtable properTargets(Ability A, MOB caster)
	{
		Hashtable h=null;
		if(A.quality()!=Ability.MALICIOUS)
			h=grouping.getGroupMembers(caster);
		else
		if(caster.isInCombat())
			h=allCombatants(caster);
		else
			h=allPossibleCombatants(caster);
		return h;
	}

	public Hashtable allCombatants(MOB mob)
	{
		Hashtable h=new Hashtable();
		Room thisRoom=mob.location();
		if(thisRoom==null) return null;
		if(!mob.isInCombat()) return null;

		Hashtable h1=grouping.getGroupMembers(mob);

		for(int m=0;m<thisRoom.numInhabitants();m++)
		{
			MOB inhab=thisRoom.fetchInhabitant(m);
			if((inhab!=null)
			   &&((inhab==mob.getVictim())
				||((inhab!=mob)
				  &&(inhab.getVictim()!=mob.getVictim())
				  &&(h1.get(inhab)==null))))
					h.put(inhab,inhab);
		}
		return h;

	}


	public long adjustedAttackBonus(MOB mob)
	{
		return	mob.envStats().attackAdjustment()
				+(Math.round((new Integer(mob.charStats().getStrength()).doubleValue()-9.0)*3.0))
				-((mob.curState().getHunger()<1)?10:0)
				-((mob.curState().getThirst()<1)?10:0);
	}

	public long adjustedArmor(MOB mob)
	{
		return  mob.envStats().armor()
				-(Math.round((new Integer(mob.charStats().getDexterity()).doubleValue()-9.0)*3.0))
				+((mob.curState().getHunger()<1)?10:0)
				+((mob.curState().getThirst()<1)?10:0)
				+((Sense.isSitting(mob))?15:0)
				+((Sense.isSleeping(mob))?30:0);
	}


	public boolean isHit(MOB attacker, MOB target)
	{
		if(!canDamageEachOther(attacker,target)) return false;

		long adjArmor=adjustedArmor(target)-50;
		long adjAttack=Math.round(adjustedAttackBonus(attacker)*(Sense.canBeSeenBy(target,attacker)?1:.5));

		int attChance=(int)(adjArmor+adjAttack);
		if(attChance>95)
			attChance=95;
		else
		if(attChance<5)
			attChance=5;
		return (Dice.rollPercentage()<attChance);
	}

	public boolean doAttack(MOB attacker, MOB target, Weapon weapon)
	{
		if(weapon==null) return false;
		boolean hit=isHit(attacker,target);
		strike(attacker,target,weapon,hit);
		return hit;
	}

	public boolean canDamageEachOther(MOB attacker, MOB target)
	{
		if((attacker==null)
		||(attacker.amDead())
		||(target==null)
		||(target.location()==null)
		||(target.amDead()))
			return false;
		return true;
	}
	
	public boolean canFightEachOther(MOB attacker, MOB target)
	{
		if((!canDamageEachOther(attacker,target))
		||(attacker.location()==null)
		||(attacker.location()!=target.location())
		||(!attacker.location().isInhabitant(attacker))
		||(!target.location().isInhabitant(target)))
		   return false;
		return true;
	}
	
	public void postAttack(MOB attacker, MOB target, Item weapon)
	{
		if(!canFightEachOther(attacker,target)) return;
		FullMsg msg=new FullMsg(attacker,target,weapon,Affect.MSG_WEAPONATTACK,null);
		if(target.location().okAffect(msg))
			target.location().send(attacker,msg);
	}
	public void postDamage(MOB attacker, MOB target, Environmental weapon, int damage)
	{
		if(!canDamageEachOther(attacker,target)) return;

		FullMsg msg=new FullMsg(attacker,target,weapon,Affect.NO_EFFECT,Affect.MASK_HURT+damage,Affect.NO_EFFECT,null);
		if(target.location().okAffect(msg))
			target.location().send(target,msg);
	}

	public void doDamage(MOB source, MOB target, int damageAmount)
	{
		if((!target.curState().adjHitPoints(-damageAmount,target.maxState()))
		   &&(target.location()!=null))
		{
			Room deathRoom=target.location();
			int expAmount=60;
			int totalLevels=0;
			Hashtable beneficiaries=new Hashtable();
			Hashtable followers=new Hashtable();
			if(source!=null)
				followers=grouping.getGroupMembers(source);

			for(int m=0;m<deathRoom.numInhabitants();m++)
			{
				MOB mob=deathRoom.fetchInhabitant(m);
				if((mob!=null)
				&&(!mob.amDead())
				&&(mob.charStats().getMyClass()!=null)
				&&(beneficiaries.get(mob)==null)
				&&(mob!=target))
				{
					if((mob.getVictim()==target)
					||(followers.get(mob)!=null)
					||(mob==source))
					{
						beneficiaries.put(mob,mob);
						expAmount+=10;
						totalLevels+=mob.envStats().level();
					}
				}
			}
			if(beneficiaries.size()>0)
			{
				for(Enumeration e=beneficiaries.elements();e.hasMoreElements();)
				{
					MOB mob=(MOB)e.nextElement();
					int myAmount=(int)Math.round(Util.mul(expAmount,Util.div(mob.envStats().level(),totalLevels)));
					if(myAmount>100) myAmount=100;
					mob.charStats().getMyClass().gainExperience(mob,target,myAmount);
				}
			}
			while(target.numFollowers()>0)
			{
				MOB follower=target.fetchFollower(0);
				if(follower!=null)
					follower.setFollowing(null);
			}
			target.setFollowing(null);
			deathRoom.delInhabitant(target);
			deathRoom.show(target,null,Affect.MSG_OK_ACTION,target.name()+" is DEAD!!!\n\r");
			if(!target.isMonster())
			{
				int expLost=100;
				target.setExperience(target.getExperience()-expLost);
				target.tell("You lose 100 experience points.");
			}
			DeadBody Body=(DeadBody)CMClass.getItem("Corpse");
			Body.baseEnvStats().setWeight(target.baseEnvStats().weight());
			if(!target.isMonster())
				Body.baseEnvStats().setRejuv(Body.baseEnvStats().rejuv()*10);
			deathRoom.addItem(Body);
			Body.recoverEnvStats();
			
			int deadMoney=target.getMoney();
			target.setMoney(0);
			Vector items=new Vector();
			for(int i=0;i<target.inventorySize();)
			{
				Item thisItem=target.fetchInventory(i);
				if((thisItem!=null)&&(thisItem.savable()))
				{
					if(target.isMonster())
					{
						Item newItem=(Item)thisItem.copyOf();
						newItem.setLocation(null);
						newItem.setPossessionTime(Calendar.getInstance());
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
					items.addElement(thisItem);
				}
				else
				if(thisItem!=null)
					target.delInventory(thisItem);
				else
					i++;
			}
			target.kill();
			
			// lastly, clean up victimhood
			for(int i=0;i<deathRoom.numInhabitants();i++)
			{
				MOB inhab=deathRoom.fetchInhabitant(i);
				// if a mob is not fighting the dead man (fighting someone else), 
				// and the person this mob is fighting is either fighting noone, or the victim
				// then he should fight this mob!
				// otherwise, if the mob is still fighting the target, stop him!
				if((inhab!=null)&&(inhab.getVictim()!=null))
				{
					if(inhab.getVictim()!=target)
					{
						MOB victim=inhab.getVictim();
						if((victim.getVictim()==null)||(victim.getVictim()==target))
						{
							if((inhab.amFollowing()!=null)&&(victim.amFollowing()!=null)&&(inhab.amFollowing()==victim.amFollowing()))
								inhab.setVictim(null);
							else
								victim.setVictim(inhab);
						}
							
					}
					else
						inhab.setVictim(null);
				}
			}
			
			Body.setName("the body of "+target.name());
			Body.setSecretIdentity(target.name()+"/"+target.description());
			Body.setDisplayText("the body of "+target.name()+" lies here.");
			if((!target.isMonster())||(target.soulMate()!=null))
			{
				if(target.soulMate()==null)
					target.raiseFromDead();
				else
				{
					Ability A=CMClass.getAbility("Archon_Possess");
					A.setAffectedOne(target);
					A.unInvoke();
				}
			}
			Body.startTicker(deathRoom);
			deathRoom.recoverRoomStats();
			
			if(deadMoney>0)
			{
				if((source.getBitmap()&MOB.ATT_AUTOGOLD)==0)
				{
					Item C=(Item)CMClass.getItem("Coins");
					C.baseEnvStats().setAbility(deadMoney);
					C.recoverEnvStats();
					C.setPossessionTime(Calendar.getInstance());
					C.setLocation(Body);
					deathRoom.addItem(C);
					deathRoom.recoverRoomStats();
				}
				else
				for(Enumeration e=beneficiaries.elements();e.hasMoreElements();)
				{
					MOB mob=(MOB)e.nextElement();
					int myAmount=(int)Math.round(Util.div(deadMoney,beneficiaries.size()));
					if(myAmount>0)
					{
						Item C=CMClass.getItem("Coins");
						C.baseEnvStats().setAbility(myAmount);
						C.setLocation(Body);
						C.setPossessionTime(Calendar.getInstance());
						C.recoverEnvStats();
						deathRoom.addItem(C);
						deathRoom.recoverRoomStats();
						if(Sense.canBeSeenBy(Body,mob))
							new ItemUsage().get(mob,Body,C,false);
					}
				}
			}
			if((source.getBitmap()&MOB.ATT_AUTOLOOT)>0)
				for(int i=items.size()-1;i>=0;i--)
					if(Sense.canBeSeenBy(Body,source))
						new ItemUsage().get(source,Body,(Item)items.elementAt(i),false);
		}
		else
		if((target.curState().getHitPoints()<target.getWimpHitPoint())&&(target.isInCombat()))
			ExternalPlay.flee(target,"");
	}

	public void autoloot(MOB mob)
	{
		if((mob.getBitmap()&MOB.ATT_AUTOLOOT)>0)
		{
			mob.setBitmap(mob.getBitmap()-MOB.ATT_AUTOLOOT);
			mob.tell("Autolooting has been turned off.");
		}
		else
		{
			mob.setBitmap(mob.getBitmap()|MOB.ATT_AUTOLOOT);
			mob.tell("Autolooting has been turned on.");
		}
	}
	public void autogold(MOB mob)
	{
		if((mob.getBitmap()&MOB.ATT_AUTOGOLD)>0)
		{
			mob.setBitmap(mob.getBitmap()-MOB.ATT_AUTOGOLD);
			mob.tell("Autogold has been turned off.");
		}
		else
		{
			mob.setBitmap(mob.getBitmap()|MOB.ATT_AUTOGOLD);
			mob.tell("Autogold has been turned on.");
		}
	}

	public void autoAssist(MOB mob)
	{
		if((mob.getBitmap()&MOB.ATT_AUTOASSIST)>0)
		{
			mob.setBitmap(mob.getBitmap()-MOB.ATT_AUTOASSIST);
			mob.tell("Autoassist has been turned on.");
		}
		else
		{
			mob.setBitmap(mob.getBitmap()|MOB.ATT_AUTOASSIST);
			mob.tell("Autoassist has been turned off.");
		}
	}


	public String hitString(int weaponType, int damageAmount, String weaponName)
	{
		if((weaponType!=Weapon.TYPE_NATURAL)||(weaponName==null)||(weaponName.length()==0))
			return "<S-NAME> "+hitWord(weaponType,damageAmount)+" <T-NAMESELF> with "+weaponName;
		else
			return "<S-NAME> "+hitWord(weaponType,damageAmount)+" <T-NAMESELF>";
	}

	public String hitWord(int weaponType, int damageAmount)
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
				return "scratch(es)";
			else
			if(damageAmount<6)
				return "graze(s)";
			else
			if(damageAmount<9)
				return "hit(s)";
			else
			if(damageAmount<13)
				return "cut(s)";
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
				return "gut(s)";
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
		case Weapon.TYPE_SHOOT:
			if(damageAmount<3)
				return "scratch(es)";
			else
			if(damageAmount<9)
				return "graze(s)";
			else
			if(damageAmount<20)
				return "hit(s)";
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

	public String armorStr(int armor)
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
			return "completely armored ("+armor+")";
		else
		if(armor < 150)
			return "divinely armored ("+armor+")";
		else
		if(armor < 165)
			return "practically unhittable ("+armor+")";
		else
		if(armor < 180)
			return "almost impenetrable ("+armor+")";
		else
		if(armor < 195)
			return "almost invincible ("+armor+")";
		else
			return "invincible! ("+armor+")";
	}
	public String fightingProwessStr(int prowess)
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
		if(prowess < 150)
			return "deadly ("+prowess+")";
		else
		if(prowess < 165)
			return "extremely deadly ("+prowess+")";
		else
		if(prowess < 180)
			return "a dealer of death ("+prowess+")";
		else
		if(prowess < 195)
			return "a master of death ("+prowess+")";
		else
			return "you are death incarnate! ("+prowess+")";
	}

	public String missString(int weaponType, String weaponName)
	{
		switch(weaponType)
		{
		case Weapon.TYPE_BASHING:
			return "<S-NAME> swing(s) at <T-NAMESELF> and miss(es).";
		case Weapon.TYPE_NATURAL:
			return "<S-NAME> attack(s) <T-NAMESELF> and miss(es).";
		case Weapon.TYPE_SLASHING:
			return "<S-NAME> swing(s) at <T-NAMESELF> and miss(es).";
		case Weapon.TYPE_PIERCING:
			return "<S-NAME> lunge(s) at <T-NAMESELF> and miss(es).";
		case Weapon.TYPE_BURNING:
			return "<S-NAME> attack(s) <T-NAMESELF> and miss(es).";
		case Weapon.TYPE_BURSTING:
			return "<S-NAME> attack(s) <T-NAMESELF> and miss(es).";
		case Weapon.TYPE_SHOOT:
			return "<S-NAME> shoot(s) at <T-NAMESELF> and miss(es).";
		default:
			return "<S-NAME> attack(s) <T-NAMESELF> and miss(es).";
		}
	}

	public void strike(MOB source, MOB target, Weapon weapon, boolean success)
	{
		if(target.amDead()) return;

		if(success)
		{
            // calculate Base Damage (with Strength bonus)
            double damageAmount = new Integer(Dice.roll(1, source.envStats().damage(), (source.charStats().getStrength() / 3)-2)).doubleValue();

            // modify damage if target can not be seen
            if(!Sense.canBeSeenBy(target,source))
                damageAmount *=.5;

            // modify damage if target is sitting
            // modify damage if target is asleep
            if(Sense.isSleeping(target))
                damageAmount *=1.5;
			else
            if(Sense.isSitting(target))
                damageAmount *=1.2;

            // modify damage if source is hungry
			if(source.curState().getHunger() < 1)
                damageAmount *= .8;

            //modify damage if source is thirtsy
			if(source.curState().getThirst() < 1)
                damageAmount *= .9;

			if(damageAmount<1.0)
				damageAmount=1.0;
			
			int damageInt=(int)Math.round(damageAmount);

			FullMsg msg=new FullMsg(source,
									target,
									weapon,
									Affect.MSG_NOISYMOVEMENT,
									hitString(weapon.weaponType(),damageInt,weapon.name()));
			msg.tagModified(true);
			// why was there no okaffect here?
			if(source.location().okAffect(msg))
			{
				source.location().send(source,msg);
				msg=new FullMsg(source,target,weapon,Affect.NO_EFFECT,Affect.MASK_HURT+damageInt,Affect.NO_EFFECT,null);
				if(source.location().okAffect(msg))
					source.location().send(source,msg);
			}
		}
		else
		{
			FullMsg msg=new FullMsg(source,
									target,
									weapon,
									Affect.MSG_NOISYMOVEMENT,
									missString(weapon.weaponType(),weapon.name()));
			// why was there no okaffect here?
			if(source.location().okAffect(msg))
				source.location().send(source,msg);
		}
	}

	public void resistanceMsgs(Affect affect, MOB source, MOB target)
	{
		if(affect.wasModified()) return;
		if(target.amDead()) return;

		String tool=null;
		String endPart=" from <S-NAME>.";
		if(affect.tool()!=null)
		{
			if(affect.tool() instanceof Trap)
				endPart=".";
			else
		    if(affect.tool() instanceof Ability)
				tool=((Ability)affect.tool()).name();
		}

		switch(affect.targetMinor())
		{
		case Affect.TYP_MIND:
			affect.addTrailerMsg(new FullMsg(source,target,Affect.MSG_NOISYMOVEMENT,"<T-NAME> shake(s) off the "+((tool==null)?"mental attack":tool)+endPart));
			break;
		case Affect.TYP_GAS:
			affect.addTrailerMsg(new FullMsg(source,target,Affect.MSG_NOISYMOVEMENT,"<T-NAME> avoid(s) the "+((tool==null)?"noxious fumes":tool)+endPart));
			break;
		case Affect.TYP_COLD:
			affect.addTrailerMsg(new FullMsg(source,target,Affect.MSG_NOISYMOVEMENT,"<T-NAME> shake(s) off the "+((tool==null)?"cold blast":tool)+endPart));
			break;
		case Affect.TYP_ELECTRIC:
			affect.addTrailerMsg(new FullMsg(source,target,Affect.MSG_NOISYMOVEMENT,"<T-NAME> shake(s) off the "+((tool==null)?"electrical attack":tool)+endPart));
			break;
		case Affect.TYP_FIRE:
			affect.addTrailerMsg(new FullMsg(source,target,Affect.MSG_NOISYMOVEMENT,"<T-NAME> avoid(s) the "+((tool==null)?"blast of heat":tool)+endPart));
			break;
		case Affect.TYP_WATER:
			affect.addTrailerMsg(new FullMsg(source,target,Affect.MSG_NOISYMOVEMENT,"<T-NAME> dodge(s) the "+((tool==null)?"wet blast":tool)+endPart));
			break;
		case Affect.TYP_UNDEAD:
			affect.addTrailerMsg(new FullMsg(source,target,Affect.MSG_NOISYMOVEMENT,"<T-NAME> shake(s) off the "+((tool==null)?"evil attack":tool)+endPart));
			break;
		case Affect.TYP_POISON:
			//affect.addTrailerMsg(new FullMsg(source,target,Affect.MSG_NOISYMOVEMENT,"<T-NAME> shake(s) off the "+((tool==null)?"poisonous attack":tool)+endPart));
			break;
		case Affect.TYP_JUSTICE:
			//affect.addTrailerMsg(new FullMsg(source,target,Affect.MSG_NOISYMOVEMENT,"<T-NAME> shake(s) off the "+((tool==null)?"poisonous attack":tool)+endPart));
			break;
		case Affect.TYP_CAST_SPELL:
			affect.addTrailerMsg(new FullMsg(source,target,Affect.MSG_NOISYMOVEMENT,"<T-NAME> resist(s) the "+((tool==null)?"magical attack":tool)+endPart));
			break;
		}
		affect.tagModified(true);
	}
}
