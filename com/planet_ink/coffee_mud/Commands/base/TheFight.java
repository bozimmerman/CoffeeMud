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
			FullMsg msg=new FullMsg(mob,target,null,Affect.MSG_OK_ACTION,"^F<S-NAME> touch(es) <T-NAMESELF>.^?");
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
				mob.tell("^FYou are already fighting "+mob.getVictim().name()+".^?");
			else
			if(mob.location().okAffect(new FullMsg(mob,target,Affect.MSG_WEAPONATTACK,null)))
			{
				mob.tell("^FYou are now targeting "+target.name()+".^?");
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
		if((attacker==null)||(target==null)||(target.location()==null)) return;
		if(allDisplayMessage!=null) allDisplayMessage="^F"+allDisplayMessage+"^?";
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
		deathRoom.showSource(target,null,Affect.MSG_DEATH,"^F^*!!!!!!!!!!!!!!YOU ARE DEAD!!!!!!!!!!!!!!^?^^\n\r");
		deathRoom.showOthers(target,null,Affect.MSG_DEATH,"^F<S-NAME> is DEAD!!!^?\n\r");
		
		Hashtable beneficiaries=new Hashtable();
		if((target.charStats()!=null)&&(target.charStats().getMyClass()!=null)&&(source!=null))
			beneficiaries=target.charStats().getMyClass().dispenseExperience(source,target);
		
		if(target.soulMate()==null)
		{
			int expLost=100;
			target.tell("^F^*You lose "+expLost+" experience points.^?^^");
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
						deathRoom.addItemRefuse(C,Item.REFUSE_MONSTER_EQ);
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

	public Vector getSheaths(MOB mob, boolean withWeapons)
	{
		Vector sheaths=new Vector();
		if(mob!=null)
		for(int i=0;i<mob.inventorySize();i++)
		{
			Item I=mob.fetchInventory(i);
			if((I!=null)
			&&(!I.amWearingAt(Item.INVENTORY))
			&&(I instanceof Container)
			&&(((Container)I).capacity()>0))
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
	
	public void sheath(MOB mob, Vector commands)
	{
		commands.removeElementAt(0);
		Vector sheaths=getSheaths(mob,false);
		Vector items=new Vector();
		Vector containers=new Vector();
		if(commands.size()==0)
		{
			Item item1=mob.fetchWieldedItem();
			Item item2=mob.fetchWornItem(Item.HELD);
			if(item2==item1) item2=null;
			for(int i=0;i<sheaths.size();i++)
			{
				Container sheath=(Container)sheaths.elementAt(i);
				if((item1!=null)
				   &&(sheath.canContain(item1)))
				{
					items.addElement(item1);
					containers.addElement(sheath);
				}
				else
				if((item2!=null)
				   &&(sheath.canContain(item2)))
				{
					items.addElement(item2);
					containers.addElement(sheath);
				}
			}
			if((item2!=null)&&(!items.contains(item2)))
			for(int i=0;i<sheaths.size();i++)
			{
				Container sheath=(Container)sheaths.elementAt(i);
				if(sheath.canContain(item2))
				{
					items.addElement(item2);
					containers.addElement(sheath);
				}
			}
		}
		else
		{
			commands.insertElementAt("all",0);
			Container container=(Container)new ItemUsage().possibleContainer(mob,commands,Item.WORN_REQ_WORNONLY);
			String thingToPut=Util.combine(commands,0);
			int addendum=1;
			String addendumStr="";
			Vector V=new Vector();
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
					if(Sense.canBeSeenBy(putThis,mob))
					{
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
				FullMsg putMsg=new FullMsg(mob,container,putThis,Affect.MSG_PUT,"<S-NAME> sheath(s) "+putThis.name()+" in <T-NAME>");
				if(mob.location().okAffect(putMsg))
					mob.location().send(mob,putMsg);
			}
		}
	}
	
	public void draw(MOB mob, Vector commands)
	{
		boolean allFlag=false;
		Vector containers=new Vector();
		String containerName="";
		String whatToGet="";
		int c=0;
		Vector sheaths=getSheaths(mob,true);
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
			containers=new ItemUsage().possibleContainers(mob,commands,Item.WORN_REQ_WORNONLY);
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
				if(new ItemUsage().get(mob,container,(Item)getThis,false,"draw"))
				{
					if(getThis.container()==null)
					{
						if(mob.amWearingSomethingHere(Item.WIELD))
							new ItemUsage().hold(mob,getThis,true);
						else
							new ItemUsage().wield(mob,getThis,true);
					}
				}
				if(container!=null)	container.setRawWornCode(wearCode);
				doneSomething=true;
			}
			
			if(containers.size()==0) break;
		}
		if(!doneSomething)
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
									"^F"+weapon.hitString(damageInt)+"^?");
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
