package com.planet_ink.coffee_mud.Commands.base;

import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.Commands.base.sysop.SysopItemUsage;
import com.planet_ink.coffee_mud.Commands.base.sysop.SysOpSkills;
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
		if(commands.size()<2)
		{
			if(!mob.isInCombat())
				mob.tell("Kill whom?");
			return;
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
		else
		if(reallyKill)
		{
			FullMsg msg=new FullMsg(mob,target,null,Affect.MSG_OK_ACTION,"<S-NAME> touch(es) <T-NAMESELF>.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				target.curState().setHitPoints(0);
				die(mob,target);
			}
		}
		else
		if(mob.isInCombat())
		{
			if((mob.getVictim()!=null)&&(mob.getVictim()==target))
				mob.tell("You are already fighting "+mob.getVictim().name()+".");
			else
			if(mob.location().okAffect(new FullMsg(mob,target,Affect.MSG_WEAPONATTACK,null)))
			{
				mob.tell("You are now targeting "+target.name()+".");
				mob.setVictim(target);
			}
			return;
		}
		else
			postAttack(mob,target,mob.fetchWieldedItem());
	}

	public Hashtable allPossibleCombatants(MOB mob, boolean beRuthless)
	{
		Hashtable h=new Hashtable();
		Room thisRoom=mob.location();
		if(thisRoom==null) return null;
		Hashtable h1=mob.getGroupMembers(new Hashtable());
		for(int m=0;m<thisRoom.numInhabitants();m++)
		{
			MOB inhab=thisRoom.fetchInhabitant(m);
			if((inhab!=null)
			&&(inhab!=mob)
			&&(h1.get(inhab)==null)
			&&((beRuthless)||(!mob.isMonster())||(!inhab.isMonster())))
				h.put(inhab,inhab);
		}
		return h;
	}

	public Hashtable properTargets(Ability A, MOB caster, boolean beRuthless)
	{
		Hashtable h=null;
		if(A.quality()!=Ability.MALICIOUS)
			h=caster.getGroupMembers(new Hashtable());
		else
		if(caster.isInCombat())
			h=allCombatants(caster);
		else
			h=allPossibleCombatants(caster,beRuthless);
		return h;
	}

	public Hashtable allCombatants(MOB mob)
	{
		Hashtable h=new Hashtable();
		Room thisRoom=mob.location();
		if(thisRoom==null) return null;
		if(!mob.isInCombat()) return null;

		Hashtable h1=mob.getGroupMembers(new Hashtable());
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


	public void postAttack(MOB attacker, MOB target, Item weapon)
	{
		if((attacker==null)||(!attacker.mayPhysicallyAttack(target))) return;
		FullMsg msg=new FullMsg(attacker,target,weapon,Affect.MSG_WEAPONATTACK,null);
		if(target.location().okAffect(msg))
			target.location().send(attacker,msg);
	}
	public void postDamage(MOB attacker, 
						   MOB target, 
						   Environmental weapon, 
						   int damage,
						   int messageCode,
						   int damageType,
						   String allDisplayMessage)
	{
		if((attacker==null)||(target==null)) return;
		FullMsg msg=new FullMsg(attacker,target,weapon,messageCode,Affect.MASK_HURT+damage,messageCode,allDisplayMessage);
		if(target.location().okAffect(msg))
		{
			allDisplayMessage=msg.othersMessage();
			if((allDisplayMessage!=null)
			   &&(msg.sourceCode()>0)
			   &&(allDisplayMessage.equals(msg.sourceMessage()))
			   &&(damageType>=0)
			   &&(Util.bset(msg.targetCode(),Affect.MASK_HURT)))
			{
				if((weapon==null)||(!(weapon instanceof Weapon)))
				{
					int replace=allDisplayMessage.indexOf("<DAMAGE>");
					if(replace>=0)
					{
						int dmg=msg.targetCode()-Affect.MASK_HURT;
						String damageWord=CommonStrings.standardHitWord(damageType,dmg);
						allDisplayMessage=allDisplayMessage.substring(0,replace)+damageWord+allDisplayMessage.substring(replace+8);
					}
				}
				FullMsg msg2=new FullMsg(msg.source(),
										 msg.target(),
										 msg.tool(),
										 msg.sourceCode(),
										 allDisplayMessage,
										 msg.othersCode(),
										 allDisplayMessage,
										 msg.othersCode(),
										 allDisplayMessage);
				target.location().send(target,msg2);
				msg.modify(msg.source(),
						   msg.target(),
						   msg.tool(),
						   Affect.NO_EFFECT,
						   null,
						   msg.targetCode(),
						   null,
						   Affect.NO_EFFECT,
						   null);
			}
			target.location().send(target,msg);
		}
	}

	public void die(MOB source, MOB target)
	{
		if(target==null) return;
		Room deathRoom=target.location();
		deathRoom.showSource(target,null,Affect.MSG_DEATH,"!!!!!!!!!!!!!!YOU ARE DEAD!!!!!!!!!!!!!!\n\r");
		deathRoom.showOthers(target,null,Affect.MSG_DEATH,"<S-NAME> is DEAD!!!\n\r");
		
		Hashtable beneficiaries=new Hashtable();
		if((target.charStats()!=null)&&(target.charStats().getMyClass()!=null)&&(source!=null))
			beneficiaries=target.charStats().getMyClass().dispenseExperience(source,target);
		
		if(target.soulMate()==null)
		{
			int expLost=100;
			target.tell("You lose "+expLost+" experience points.");
			target.charStats().getMyClass().loseExperience(target,expLost);
		}
		
		int deadMoney=target.getMoney();
		if((source!=null)&&((source.getBitmap()&MOB.ATT_AUTOGOLD)>0))
			target.setMoney(0);
		
		DeadBody Body=target.killMeDead();
		
		if(target.soulMate()!=null) new SysOpSkills().dispossess(target);
		
		if(source!=null)
		{
			if((deadMoney>0)&&((source.getBitmap()&MOB.ATT_AUTOGOLD)>0))
			{
				if((source.riding()!=null)&&(source.riding() instanceof MOB))
					source.tell("You'll need to dismount to get gold off the body.");
				else
				if((source.riding()!=null)&&(source.riding() instanceof MOB))
					source.tell("You'll need to disembark to get gold off the body.");
				else
				for(Enumeration e=beneficiaries.elements();e.hasMoreElements();)
				{
					MOB mob=(MOB)e.nextElement();
					int myAmount=(int)Math.round(Util.div(deadMoney,beneficiaries.size()));
					if(myAmount>0)
					{
						Item C=CMClass.getItem("StdCoins");
						C.baseEnvStats().setAbility(myAmount);
						C.setContainer(Body);
						C.recoverEnvStats();
						deathRoom.addItemRefuse(C);
						deathRoom.recoverRoomStats();
						if(Sense.canBeSeenBy(Body,mob))
							ExternalPlay.get(mob,Body,C,false);
					}
				}
			}
			if((source.getBitmap()&MOB.ATT_AUTOLOOT)>0)
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
					&&(Sense.canBeSeenBy(item,source)))
						ExternalPlay.get(source,Body,item,false);
				}
			}
			deathRoom.recoverRoomStats();
		}
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
	public void playerkill(MOB mob)
		throws IOException
	{
		if(mob.isInCombat())
		{
			mob.tell("YOU CANNOT TOGGLE THIS FLAG WHILE IN COMBAT!");
			return;
		}
		if((mob.getBitmap()&MOB.ATT_PLAYERKILL)>0)
		{
			mob.setBitmap(mob.getBitmap()-MOB.ATT_PLAYERKILL);
			mob.tell("Your playerkill flag has been turned off.");
		}
		else
		if(!mob.isMonster())
		{
			mob.tell("Turning on this flag will allow you to kill and be killed by other players.");
			if(mob.session().confirm("Are you absolutely sure (y/N)?","N"))
			{
				mob.setBitmap(mob.getBitmap()|MOB.ATT_PLAYERKILL);
				mob.tell("Your playerkill flag has been turned on.");
			}
			else
				mob.tell("Your playerkill flag remains OFF.");
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

	public void autoMelee(MOB mob)
	{
		if((mob.getBitmap()&MOB.ATT_AUTOMELEE)==0)
		{
			mob.setBitmap(mob.getBitmap()|MOB.ATT_AUTOMELEE);
			mob.tell("Automelee has been turned off.  You will no longer charge into melee combat from a ranged position.");
		}
		else
		{
			mob.setBitmap(mob.getBitmap()-MOB.ATT_AUTOMELEE);
			mob.tell("Automelee has been turned back on.  You will now enter melee combat normally.");
		}
	}
	
	public void postWeaponDamage(MOB source, MOB target, Weapon weapon, boolean success)
	{
		if(source==null) return;
		if(!source.mayIFight(target)) return;
		int damageInt=source.adjustedDamage(weapon,target);
		if(success)
		{
            // calculate Base Damage (with Strength bonus)
			FullMsg msg=new FullMsg(source,
									target,
									weapon,
									Affect.MSG_NOISYMOVEMENT,
									weapon.hitString(damageInt));
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
									weapon.missString());
			// why was there no okaffect here?
			if(source.location().okAffect(msg))
				source.location().send(source,msg);
		}
	}
}
