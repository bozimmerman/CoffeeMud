package com.planet_ink.coffee_mud.Commands.base;

import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.Commands.sysop.*;
import java.io.*;
import java.util.*;
public class TheFight
{
	private TheFight(){}

	public static void kill(MOB mob, Vector commands)
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
			FullMsg msg=new FullMsg(mob,target,null,Affect.MSG_OK_ACTION,"^F<S-NAME> touch(es) <T-NAMESELF>.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				target.curState().setHitPoints(0);
				postDeath(mob,target,null);
			}
		}
		else
		if(mob.isInCombat())
		{
			if((mob.getVictim()!=null)&&(mob.getVictim()==target))
				mob.tell("^FYou are already fighting "+mob.getVictim().name()+".^?");
			else
			if(mob.location().okAffect(mob,new FullMsg(mob,target,Affect.MSG_WEAPONATTACK,null)))
			{
				mob.tell("^FYou are now targeting "+target.name()+".^?");
				mob.setVictim(target);
			}
			return;
		}
		else
		if((!mob.mayPhysicallyAttack(target)))
			mob.tell("You are not allowed to attack "+target.name()+".");
		else
			postAttack(mob,target,mob.fetchWieldedItem());

	}

	public static Hashtable allPossibleCombatants(MOB mob, boolean beRuthless)
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

	public static Hashtable properTargets(Ability A, MOB caster, boolean beRuthless)
	{
		Hashtable h=null;
		if(A.quality()!=Ability.MALICIOUS)
		{
			h=caster.getGroupMembers(new Hashtable());
			for(Enumeration e=h.keys();e.hasMoreElements();)
			{
				MOB M=(MOB)e.nextElement();
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

	public static Hashtable allCombatants(MOB mob)
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


	public static void postDeath(MOB source, MOB target, Affect addHere)
	{
		if(target==null) return;
		Room deathRoom=target.location();
		if(deathRoom==null) return;

		// make sure he's not already dead, or with a pending death.
		if(target.amDead()) return;
		if((addHere!=null)&&(addHere.trailerMsgs()!=null))
		for(int i=0;i<addHere.trailerMsgs().size();i++)
		{
			Affect affect=(Affect)addHere.trailerMsgs().elementAt(i);
			if((affect.source()==target)
			&&((affect.sourceMinor()==Affect.TYP_PANIC))
			   ||(affect.sourceMinor()==Affect.TYP_DEATH))
				return;
		}

		String msp=CommonStrings.msp("death"+Dice.roll(1,4,0)+".wav",50);
		FullMsg msg=new FullMsg(target,null,null,
			Affect.MSG_OK_VISUAL,"^F^*!!!!!!!!!!!!!!YOU ARE DEAD!!!!!!!!!!!!!!^?^.\n\r"+msp,
			Affect.MSG_OK_VISUAL,null,
			Affect.MSG_OK_VISUAL,"^F<S-NAME> is DEAD!!!^?\n\r"+msp);
		FullMsg msg2=new FullMsg(target,null,source,
			Affect.MSG_DEATH,null,
			Affect.MSG_DEATH,null,
			Affect.MSG_DEATH,null);
		if(addHere!=null)
		{
			addHere.addTrailerMsg(msg);
			addHere.addTrailerMsg(msg2);
		}
		else
		if((deathRoom!=null)&&(deathRoom.okAffect(target,msg)))
		{
			deathRoom.send(target,msg);
			if(deathRoom.okAffect(target,msg2))
				deathRoom.send(target,msg2);
		}
	}

	public static void postAttack(MOB attacker, MOB target, Item weapon)
	{
		if((attacker==null)||(!attacker.mayPhysicallyAttack(target)))
			return;

		if((weapon==null)
		&&(Util.bset(attacker.getBitmap(),MOB.ATT_AUTODRAW)))
		{
			draw(attacker,new Vector(),true,false);
			weapon=attacker.fetchWieldedItem();
		}
		FullMsg msg=new FullMsg(attacker,target,weapon,Affect.MSG_WEAPONATTACK,null);
		if(target.location().okAffect(attacker,msg))
			target.location().send(attacker,msg);
	}
	public static void postPanic(MOB mob, Affect addHere)
	{
		if(mob==null) return;

		// make sure he's not already dead, or with a pending death.
		if(mob.amDead()) return;
		if((addHere!=null)&&(addHere.trailerMsgs()!=null))
		for(int i=0;i<addHere.trailerMsgs().size();i++)
		{
			Affect affect=(Affect)addHere.trailerMsgs().elementAt(i);
			if((affect.source()==mob)
			&&((affect.sourceMinor()==Affect.TYP_PANIC))
			   ||(affect.sourceMinor()==Affect.TYP_DEATH))
				return;
		}
		FullMsg msg=new FullMsg(mob,null,Affect.MSG_PANIC,null);
		if(addHere!=null)
			addHere.addTrailerMsg(msg);
		else
		if((mob.location()!=null)&&(mob.location().okAffect(mob,msg)))
			mob.location().send(mob,msg);
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
		if(damage>=1024) damage=1023;
		if(allDisplayMessage!=null) allDisplayMessage="^F"+allDisplayMessage+"^?";
		FullMsg msg=new FullMsg(attacker,target,weapon,messageCode,Affect.MASK_HURT+damage,messageCode,allDisplayMessage);
		if(target.location().okAffect(target,msg))
		{
			int targetCode=msg.targetCode();
			if(Util.bset(targetCode,Affect.MASK_HURT))
			{
				damage=targetCode-Affect.MASK_HURT;
				if(damage>=1024) damage=1023;
				targetCode=Affect.MASK_HURT+damage;
			}

			if(damageType>=0)
			msg.modify(msg.source(),
					   msg.target(),
					   msg.tool(),
					   msg.sourceCode(),
					   replaceDamageTag(msg.sourceMessage(),damage,damageType),
					   targetCode,
					   replaceDamageTag(msg.targetMessage(),damage,damageType),
					   msg.othersCode(),
					   replaceDamageTag(msg.othersMessage(),damage,damageType));
			target.location().send(target,msg);
		}
	}

	public static void justDie(MOB source, MOB target)
	{
		if(target==null) return;
		Room deathRoom=target.location();

		Hashtable beneficiaries=new Hashtable();
		if((source!=null)&&(source.charStats()!=null))
		{
			CharClass C=source.charStats().getCurrentClass();
			if(source.isMonster()
			   &&(source.amFollowing()!=null)
			   &&(!source.amFollowing().isMonster())
			   &&(source.amFollowing().charStats()!=null))
				C=source.amFollowing().charStats().getCurrentClass();

			beneficiaries=C.dispenseExperience(source,target);
		}

		int deadMoney=target.getMoney();
		if((source!=null)&&(Util.bset(source.getBitmap(),MOB.ATT_AUTOGOLD)))
			target.setMoney(0);

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
					MOB deadMOB=(MOB)CMClass.getMOB("StdMOB");
					boolean found=ExternalPlay.DBUserSearch(deadMOB,target.ID());
					if(found)
					{
						Body=target.killMeDead(true);
						ExternalPlay.destroyUser(deadMOB);
					}
				}
				else
				if((whatToDo.trim().equals("0"))||(Util.s_int(whatToDo)>0))
				{
					int expLost=Util.s_int(whatToDo);
					target.tell("^F^*You lose "+expLost+" experience points.^?^.");
					target.charStats().getCurrentClass().loseExperience(target,expLost);
				}
				else
				if(whatToDo.length()<3)
					continue;
				else
				{
					int expLost=100*target.envStats().level();
					target.tell("^F^*You lose "+expLost+" experience points.^?^.");
					target.charStats().getCurrentClass().loseExperience(target,expLost);
				}
			}
		}

		if(Body==null) Body=target.killMeDead(true);

		if((!target.isMonster())&&(Dice.rollPercentage()==1))
		{
			Ability A=CMClass.getAbility("Disease_Amnesia");
			if((A!=null)&&(target.fetchAffect(A.ID())==null))
				A.invoke(target,target,true);
		}

		if(target.soulMate()!=null) SysOpSkills.dispossess(target);

		Vector goldLooters=new Vector();
		for(Enumeration e=beneficiaries.keys();e.hasMoreElements();)
		{
			MOB M=(MOB)e.nextElement();
			if(((Util.bset(M.getBitmap(),MOB.ATT_AUTOGOLD))
			&&(!goldLooters.contains(M)))
			&&(M.location()==deathRoom)
			&&(deathRoom.isInhabitant(M)))
			   goldLooters.addElement(M);
		}
		if(deadMoney>0)
			for(int g=0;g<goldLooters.size();g++)
			{
				MOB mob=(MOB)goldLooters.elementAt(g);
				if((mob.riding()!=null)&&(mob.riding() instanceof MOB))
					mob.tell("You'll need to dismount to get gold off the body.");
				else
				if((mob.riding()!=null)&&(mob.riding() instanceof MOB))
					mob.tell("You'll need to disembark to get gold off the body.");
				else
				{
					int myAmount=(int)Math.round(Util.div(deadMoney,goldLooters.size()));
					if(myAmount>0)
					{
						Item C=CMClass.getItem("StdCoins");
						C.baseEnvStats().setAbility(myAmount);
						C.setContainer(Body);
						C.recoverEnvStats();
						deathRoom.addItemRefuse(C,Item.REFUSE_MONSTER_EQ);
						deathRoom.recoverRoomStats();
						if(Sense.canBeSeenBy(Body,mob))
							ExternalPlay.get(mob,Body,C,false);
					}
				}
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
				&&(Sense.canBeSeenBy(item,source)))
					ExternalPlay.get(source,Body,item,false);
			}
			deathRoom.recoverRoomStats();
		}
	}

	public static void autoloot(MOB mob)
	{
		if(Util.bset(mob.getBitmap(),MOB.ATT_AUTOLOOT))
		{
			mob.setBitmap(Util.unsetb(mob.getBitmap(),MOB.ATT_AUTOLOOT));
			mob.tell("Autolooting has been turned off.");
		}
		else
		{
			mob.setBitmap(Util.setb(mob.getBitmap(),MOB.ATT_AUTOLOOT));
			mob.tell("Autolooting has been turned on.");
		}
	}
	public static void playerkill(MOB mob)
		throws IOException
	{
		if(CommonStrings.getVar(CommonStrings.SYSTEM_PKILL).startsWith("ALWAYS")
			||CommonStrings.getVar(CommonStrings.SYSTEM_PKILL).startsWith("NEVER"))
		{
			mob.tell("This option has been disabled.");
			return;
		}

		if(mob.isInCombat())
		{
			mob.tell("YOU CANNOT TOGGLE THIS FLAG WHILE IN COMBAT!");
			return;
		}
		if(Util.bset(mob.getBitmap(),MOB.ATT_PLAYERKILL))
		{
			if(CommonStrings.getVar(CommonStrings.SYSTEM_PKILL).startsWith("ONEWAY"))
			{
				mob.tell("Once turned on, this flag may not be turned off again.");
				return;
			}
			mob.setBitmap(Util.unsetb(mob.getBitmap(),MOB.ATT_PLAYERKILL));
			mob.tell("Your playerkill flag has been turned off.");
		}
		else
		if(!mob.isMonster())
		{
			mob.tell("Turning on this flag will allow you to kill and be killed by other players.");
			if(mob.session().confirm("Are you absolutely sure (y/N)?","N"))
			{
				mob.setBitmap(Util.setb(mob.getBitmap(),MOB.ATT_PLAYERKILL));
				mob.tell("Your playerkill flag has been turned on.");
			}
			else
				mob.tell("Your playerkill flag remains OFF.");
		}
	}
	public static void autogold(MOB mob)
	{
		if(Util.bset(mob.getBitmap(),MOB.ATT_AUTOGOLD))
		{
			mob.setBitmap(Util.unsetb(mob.getBitmap(),MOB.ATT_AUTOGOLD));
			mob.tell("Autogold has been turned off.");
		}
		else
		{
			mob.setBitmap(Util.setb(mob.getBitmap(),MOB.ATT_AUTOGOLD));
			mob.tell("Autogold has been turned on.");
		}
	}

	public static void autoAssist(MOB mob)
	{
		if(Util.bset(mob.getBitmap(),MOB.ATT_AUTOASSIST))
		{
			mob.setBitmap(Util.unsetb(mob.getBitmap(),MOB.ATT_AUTOASSIST));
			mob.tell("Autoassist has been turned on.");
		}
		else
		{
			mob.setBitmap(Util.setb(mob.getBitmap(),MOB.ATT_AUTOASSIST));
			mob.tell("Autoassist has been turned off.");
		}
	}

	public static void autoMelee(MOB mob)
	{
		if(!Util.bset(mob.getBitmap(),MOB.ATT_AUTOMELEE))
		{
			mob.setBitmap(Util.setb(mob.getBitmap(),MOB.ATT_AUTOMELEE));
			mob.tell("Automelee has been turned off.  You will no longer charge into melee combat from a ranged position.");
			if(mob.isMonster())
				SocialProcessor.quickSay(mob,null,"I will no longer charge into melee.",false,false);
		}
		else
		{
			mob.setBitmap(Util.unsetb(mob.getBitmap(),MOB.ATT_AUTOMELEE));
			mob.tell("Automelee has been turned back on.  You will now enter melee combat normally.");
			if(mob.isMonster())
				SocialProcessor.quickSay(mob,null,"I will now enter melee combat normally.",false,false);
		}
	}

	public static void autoDraw(MOB mob)
	{
		if(!Util.bset(mob.getBitmap(),MOB.ATT_AUTODRAW))
		{
			mob.setBitmap(Util.setb(mob.getBitmap(),MOB.ATT_AUTODRAW));
			mob.tell("Auto weapon drawing has been turned on.  You will now draw a weapon when one is handy, and sheath one a few seconds after combat.");
		}
		else
		{
			mob.setBitmap(Util.unsetb(mob.getBitmap(),MOB.ATT_AUTODRAW));
			mob.tell("Auto weapon drawing has been turned off.  You will no longer draw or sheath your weapon automatically.");
		}
	}

	public static void throwit(MOB mob, Vector commands)
	{
		if((commands.size()==2)&&(mob.isInCombat()))
			commands.addElement(mob.getVictim().name()+"$");
		if(commands.size()<3)
		{
			mob.tell("Throw what, where or at whom?");
			return;
		}
		commands.removeElementAt(0);
		String str=(String)commands.lastElement();
		commands.removeElement(str);
		String what=Util.combine(commands,0);
		Item item=mob.fetchWornItem(what);
		if(item==null) item=mob.fetchInventory(what);
		if((item==null)||(!Sense.canBeSeenBy(item,mob)))
		{
			mob.tell("You don't seem to have a '"+what+"'!");
			return;
		}
		if((!item.amWearingAt(Item.HELD))&&(!item.amWearingAt(Item.WIELD)))
		{
			mob.tell("You aren't holding or wielding "+item.name()+"!");
			return;
		}

		int dir=Directions.getGoodDirectionCode(str);
		Environmental target=null;
		if(dir<0)
			target=mob.location().fetchInhabitant(str);
		else
		{
			target=mob.location().getRoomInDir(dir);
			if((target==null)
			||(mob.location().getExitInDir(dir)==null)
			||(!mob.location().getExitInDir(dir).isOpen()))
			{
				mob.tell("You can't throw anything that way!");
				return;
			}
			boolean amOutside=((mob.location().domainType()&Room.INDOORS)==0);
			boolean isOutside=((((Room)target).domainType()&Room.INDOORS)==0);
			boolean isUp=(mob.location().getRoomInDir(Directions.UP)==target);
			boolean isDown=(mob.location().getRoomInDir(Directions.DOWN)==target);

			if(amOutside&&isOutside&&(!isUp)&&(!isDown)
			&&((((Room)target).domainType()&Room.DOMAIN_OUTDOORS_AIR)==0))
			{
				mob.tell("That's too far to throw "+item.name()+".");
				return;
			}
		}
		if((dir<0)&&((target==null)||((target!=mob.getVictim())&&(!Sense.canBeSeenBy(target,mob)))))
		{
			mob.tell("You can't target "+item.name()+" at '"+str+"'!");
			return;
		}
		if(!(target instanceof Room))
		{
			if((item.amWearingAt(Item.HELD))
			&&(item instanceof Weapon)
			&&(item.canBeWornAt(Item.WIELD)))
			{
				if(ItemUsage.remove(mob,item,true))
					ItemUsage.wield(mob,item,false);
			}

			if(item.amWearingAt(Item.WIELD))
				ExternalPlay.postAttack(mob,(MOB)target,item);
			else
			{
				FullMsg msg=new FullMsg(mob,item,mob.location(),Affect.MASK_MALICIOUS|Affect.MSG_THROW,"<S-NAME> throw(s) <T-NAME> at "+target.name()+".");
				if(mob.location().okAffect(mob,msg))
					mob.location().send(mob,msg);
			}
		}
		else
		{
			FullMsg msg=new FullMsg(mob,item,target,Affect.MSG_THROW,"<S-NAME> throw(s) <T-NAME> "+Directions.getInDirectionName(dir).toLowerCase()+".");
			FullMsg msg2=new FullMsg(mob,item,target,Affect.MSG_THROW,"<T-NAME> fl(ys) in from "+Directions.getFromDirectionName(Directions.getOpDirectionCode(dir)).toLowerCase()+".");
			if(mob.location().okAffect(mob,msg)&&((Room)target).okAffect(mob,msg2))
			{
				mob.location().send(mob,msg);
				((Room)target).sendOthers(mob,msg2);
			}
		}
	}

	public static void autoGuard(MOB mob, Vector commands)
	{
		if((!Util.bset(mob.getBitmap(),MOB.ATT_AUTOGUARD))
		   ||((commands.size()>0)&&(((String)commands.firstElement()).toUpperCase().startsWith("G"))))
		{
			mob.setBitmap(Util.setb(mob.getBitmap(),MOB.ATT_AUTOGUARD));
			mob.tell("You are now on guard. You will no longer follow group leaders.");
			if(mob.isMonster())
				ExternalPlay.quickSay(mob,null,"I am now on guard.",false,false);
		}
		else
		{
			mob.setBitmap(Util.unsetb(mob.getBitmap(),MOB.ATT_AUTOGUARD));
			mob.tell("You are no longer on guard.  You will now follow group leaders.");
			if(mob.isMonster())
				ExternalPlay.quickSay(mob,null,"I will now follow my group leader.",false,false);
		}
	}

	public static Vector getSheaths(MOB mob, boolean withWeapons)
	{
		Vector sheaths=new Vector();
		if(mob!=null)
		for(int i=0;i<mob.inventorySize();i++)
		{
			Item I=mob.fetchInventory(i);
			if((I!=null)
			&&(!I.amWearingAt(Item.INVENTORY))
			&&(I instanceof Container)
			&&(((Container)I).capacity()>0)
			&&(((Container)I).containTypes()!=Container.CONTAIN_ANYTHING))
			{
				if(withWeapons)
				{
					Vector contents=((Container)I).getContents();
					for(int c=0;c<contents.size();c++)
						if(contents.elementAt(c) instanceof Weapon)
						{
							sheaths.addElement(I);
							break;
						}
				}
				else
					sheaths.addElement(I);
			}
		}
		return sheaths;
	}

	public static void sheathIfPossible(MOB mob)
	{ sheath(mob,new Vector(),true,false);}
	public static void sheath(MOB mob, Vector commands)
	{ sheath(mob,commands,false,false);}
	
	public static void sheath(MOB mob, Vector commands, boolean noerrors, boolean quiet)
	{
		Item item1=null;
		Item item2=null;
		if(commands.size()>0)
			commands.removeElementAt(0);
		if(commands.size()==0)
		{
			for(int i=0;i<mob.inventorySize();i++)
			{
				Item I=mob.fetchInventory(i);
				if((I!=null)
				&&(I instanceof Weapon)
				&&(!I.amWearingAt(Item.INVENTORY)))
				{
					if(I.amWearingAt(Item.WIELD))
						item1=I;
					else
					if(I.amWearingAt(Item.HELD))
						item2=I;
				}
			}
			if((noerrors)&&(item1==null)&&(item2==null))
				return;
		}
		Vector sheaths=getSheaths(mob,false);
		Vector items=new Vector();
		Vector containers=new Vector();
		Item sheathable=null;
		if(commands.size()==0)
		{
			if(item2==item1) item2=null;
			for(int i=0;i<sheaths.size();i++)
			{
				Container sheath=(Container)sheaths.elementAt(i);
				if((item1!=null)
				&&(!items.contains(item1))
				&&(sheath.canContain(item1)))
				{
					items.addElement(item1);
					containers.addElement(sheath);
				}
				else
				if((item2!=null)
				&&(!items.contains(item2))
				&&(sheath.canContain(item2)))
				{
					items.addElement(item2);
					containers.addElement(sheath);
				}
			}
			if(item2!=null)
			for(int i=0;i<sheaths.size();i++)
			{
				Container sheath=(Container)sheaths.elementAt(i);
				if((sheath.canContain(item2))
				&&(!items.contains(item2)))
				{
					items.addElement(item2);
					containers.addElement(sheath);
				}
			}
			if(item1!=null)	sheathable=item1;
			else
			if(item2!=null)	sheathable=item2;
		}
		else
		{
			commands.insertElementAt("all",0);
			Container container=(Container)ItemUsage.possibleContainer(mob,commands,false,Item.WORN_REQ_WORNONLY);
			String thingToPut=Util.combine(commands,0);
			int addendum=1;
			String addendumStr="";
			boolean allFlag=((String)commands.elementAt(0)).equalsIgnoreCase("all");
			if(thingToPut.toUpperCase().startsWith("ALL.")){ allFlag=true; thingToPut="ALL "+thingToPut.substring(4);}
			if(thingToPut.toUpperCase().endsWith(".ALL")){ allFlag=true; thingToPut="ALL "+thingToPut.substring(0,thingToPut.length()-4);}
			do
			{
				Item putThis=mob.fetchWornItem(thingToPut+addendumStr);
				if(putThis==null) break;
				if(((putThis.amWearingAt(Item.WIELD))
				   ||(putThis.amWearingAt(Item.HELD)))
				   &&(putThis instanceof Weapon))
				{
					if(Sense.canBeSeenBy(putThis,mob)&&(!items.contains(putThis)))
					{
						sheathable=putThis;
						items.addElement(putThis);
						if((container!=null)&&(container.canContain(putThis)))
							containers.addElement(container);
						else
						{
							Container tempContainer=null;
							for(int i=0;i<sheaths.size();i++)
							{
								Container sheath=(Container)sheaths.elementAt(i);
								if(sheath.canContain(putThis))
								{tempContainer=sheath; break;}
							}
							if(tempContainer==null)
								items.remove(putThis);
							else
								containers.addElement(tempContainer);
						}
					}
				}
				addendumStr="."+(++addendum);
			}
			while(allFlag);
		}

		if(items.size()==0)
		{
			if(!noerrors)
				if(sheaths.size()==0)
					mob.tell("You are not wearing an appropriate sheath.");
				else
				if(sheathable!=null)
					mob.tell("You aren't wearing anything you can sheath "+sheathable.name()+" in.");
				else
				if(commands.size()==0)
					mob.tell("You don't seem to be wielding anything you can sheath.");
				else
					mob.tell("You don't seem to be wielding that.");
		}
		else
		for(int i=0;i<items.size();i++)
		{
			Item putThis=(Item)items.elementAt(i);
			Container container=(Container)containers.elementAt(i);
			if(ExternalPlay.remove(mob,putThis,true))
			{
				FullMsg putMsg=new FullMsg(mob,container,putThis,Affect.MSG_PUT,((quiet?null:"<S-NAME> sheath(s) <O-NAME> in <T-NAME>.")));
				if(mob.location().okAffect(mob,putMsg))
					mob.location().send(mob,putMsg);
			}
		}
	}

	public static void drawIfNecessary(MOB mob, boolean held)
	{
		if(held)
		{
			if(mob.fetchWornItem(Item.HELD)==null)
				draw(mob,new Vector(),true,true);
		}
		else
		if(mob.fetchWieldedItem()==null)
			draw(mob,new Vector(),true,true);
	}

	public static void draw(MOB mob, Vector commands, boolean noerrors, boolean quiet)
	{
		boolean allFlag=false;
		Vector containers=new Vector();
		String containerName="";
		String whatToGet="";
		int c=0;
		Vector sheaths=getSheaths(mob,true);
		if(commands.size()>0)
			commands.removeElementAt(0);
		if(commands.size()==0)
		{
			if(sheaths.size()>0)
				containerName=((Item)sheaths.elementAt(0)).name();
			else
				containerName="a weapon";
			for(int i=0;i<mob.inventorySize();i++)
			{
				Item I=mob.fetchInventory(i);
				if((I instanceof Weapon)
				   &&(I.container()!=null)
				   &&(sheaths.contains(I.container())))
				{
					containers.addElement(I.container());
					whatToGet=I.name();
					break;
				}
			}
			if(whatToGet.length()==0)
				for(int i=0;i<mob.inventorySize();i++)
				{
					Item I=mob.fetchInventory(i);
					if(I instanceof Weapon)
					{
						whatToGet=I.name();
						break;
					}
				}
		}
		else
		{
			containerName=(String)commands.lastElement();
			commands.insertElementAt("all",0);
			containers=ItemUsage.possibleContainers(mob,commands,Item.WORN_REQ_WORNONLY);
			if(containers.size()==0) containers=sheaths;
			whatToGet=Util.combine(commands,0);
			allFlag=((String)commands.elementAt(0)).equalsIgnoreCase("all");
			if(whatToGet.toUpperCase().startsWith("ALL.")){ allFlag=true; whatToGet="ALL "+whatToGet.substring(4);}
			if(whatToGet.toUpperCase().endsWith(".ALL")){ allFlag=true; whatToGet="ALL "+whatToGet.substring(0,whatToGet.length()-4);}
		}
		boolean doneSomething=false;
		while((c<containers.size())||(containers.size()==0))
		{
			Vector V=new Vector();
			Item container=null;
			if(containers.size()>0) container=(Item)containers.elementAt(c++);
			int addendum=1;
			String addendumStr="";
			do
			{
				Environmental getThis=null;
				if((container!=null)&&(mob.isMine(container)))
				   getThis=mob.fetchInventory((Item)container,whatToGet+addendumStr);
				if(getThis==null) break;
				if((getThis instanceof Weapon)&&(Sense.canBeSeenBy(getThis,mob)))
					V.addElement(getThis);
				addendumStr="."+(++addendum);
			}
			while(allFlag);

			for(int i=0;i<V.size();i++)
			{
				Item getThis=(Item)V.elementAt(i);
				long wearCode=0;
				if(container!=null)	wearCode=container.rawWornCode();
				if(ItemUsage.get(mob,container,(Item)getThis,quiet,"draw",false))
				{
					if(getThis.container()==null)
					{
						if(mob.amWearingSomethingHere(Item.WIELD))
							ItemUsage.hold(mob,getThis,true);
						else
							ItemUsage.wield(mob,getThis,true);
					}
				}
				if(container!=null)	container.setRawWornCode(wearCode);
				doneSomething=true;
			}

			if(containers.size()==0) break;
		}
		if((!doneSomething)&&(!noerrors))
		{
			if(containers.size()>0)
			{
				Item container=(Item)containers.elementAt(0);
				if(((Container)container).isOpen())
					mob.tell("You don't see that in "+container.name()+".");
				else
					mob.tell(container.name()+" is closed.");
			}
			else
				mob.tell("You don't see "+containerName+" here.");
		}
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
		if(damageInt>=1024) damageInt=1023;
		if(success)
		{
            // calculate Base Damage (with Strength bonus)
			String oldHitString="^F"+weapon.hitString(damageInt)+"^?";
			FullMsg msg=new FullMsg(source,
									target,
									weapon,
									Affect.MSG_OK_VISUAL,
									Affect.MASK_HURT+damageInt,
									Affect.MSG_OK_VISUAL,
									oldHitString);
			msg.tagModified(true);
			// why was there no okaffect here?
			Room room=source.location();
			if((room!=null)&&(room.okAffect(source,msg)))
			{
				if((msg.targetCode()&Affect.MASK_HURT)==Affect.MASK_HURT)
				{
					damageInt=msg.targetCode()-Affect.MASK_HURT;
					if(damageInt>=1024) damageInt=1023;
					String newMsg="^F"+weapon.hitString(damageInt)+"^?";
					msg.modify(msg.source(),
							   msg.target(),
							   msg.tool(),
							   msg.sourceCode(),
							   pickMsg(msg.sourceMessage(),oldHitString,newMsg),
							   Affect.MASK_HURT+damageInt,
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
									Affect.MSG_NOISYMOVEMENT,
									weapon.missString());
			// why was there no okaffect here?
			if(source.location().okAffect(source,msg))
				source.location().send(source,msg);
		}
	}
}
