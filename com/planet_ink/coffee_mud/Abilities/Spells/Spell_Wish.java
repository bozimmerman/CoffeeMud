package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Wish extends Spell
{

	public Spell_Wish()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Wish";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Wish Spell)";


		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(25);

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Wish();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_ALTERATION;
	}
	private Environmental maybeAdd(Environmental E, Vector foundAll, Environmental foundThang)
	{
		if((E!=null)
		&&((foundThang==null)
		   ||((foundThang.ID().equals(E.ID()))&&(foundThang.name().equals(E.name())))))
		{
			if(foundThang==null) foundThang=E;
			foundAll.addElement(E);
		}
		return foundThang;
	}

	private void bringThangHere(MOB mob, Room here, Environmental target)
	{
		if(target instanceof MOB)
		{
			mob.location().show((MOB)target,null,Affect.MSG_OK_VISUAL,"<S-NAME> is teleported to "+here.displayText()+".");
			here.bringMobHere((MOB)target,false);
		}
		else
		if(target instanceof Item)
		{
			Item item=(Item)target;
			mob.location().show(mob,null,Affect.MSG_OK_VISUAL,target.name()+" is teleport to "+here.displayText()+"!");
			item.remove();
			item.setLocation(null);
			item.removeThis();
			here.addItem(item);
			mob.location().show(mob,null,Affect.MSG_OK_VISUAL,target.name()+" appears out of the java plain!");
		}
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		String myWish=Util.combine(commands,0);
		if(mob.curState().getMana()<mob.maxState().getMana())
		{
			mob.tell("You need to be at full mana to cast this.");
			return false;
		}
		if(mob.envStats().level()<20)
		{
			mob.tell("You are too weak to wish.");
			return false;
		}
		if(myWish.length()>0)
		{
			mob.tell("What would you like to wish for?");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		mob.curState().setMana(0);
		int baseLoss=25;
		mob.setExperience(mob.getExperience()-baseLoss);
		FullMsg msg=new FullMsg(mob,null,this,affectType,"<S-NAME> wish(es) for '"+myWish+"'!!");
		boolean success=profficiencyCheck(0,auto);
		if(!success)
		{
			beneficialWordsFizzle(mob,null,"<S-NAME> wish(es) for '"+myWish+"', but the spell fizzles.");
			return false;
		}
		else
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			
			StringBuffer wish=new StringBuffer(myWish);
			for(int i=0;i<wish.length();i++)
				if(!Character.isLetterOrDigit(wish.charAt(i)))
					wish.setCharAt(i,' ');
			myWish=wish.toString().trim().toUpperCase();
			if(myWish.startsWith(" FOR ")) myWish=myWish.substring(4);
			Vector wishV=Util.parse(myWish);
			myWish=" "+myWish+" ";
			if(wishV.size()==0)
			{
				beneficialWordsFizzle(mob,null,"<S-NAME> wish comes true! Nothing happens!");
				return false;
			}
			
			// do locate object first.. its the most likely
			String objectWish=myWish;
			String[] redundantStarts={"CREATE","ANOTHER","MAY I HAVE","CAN I HAVE","CAN YOU","CAN I","MAKE","GIVE","ME","TO HAVE","TO GET","A NEW","SOME MORE","MY OWN","A","PLEASE"};
			String[] redundantEnds={"TO APPEAR","OF MY OWN","FOR ME","BE","CREATED","PLEASE","HERE"};
			for(int i=0;i<redundantStarts.length;i++)
				while(objectWish.startsWith(" "+redundantStarts[i]+" "))
				{	objectWish=objectWish.substring(1+redundantStarts[i].length()); i=-1;}
			for(int i=0;i<redundantEnds.length;i++)
				while(objectWish.endsWith(" "+redundantEnds[i]+" "))
				{	objectWish=objectWish.substring(0,objectWish.length()-(1+redundantEnds[i].length())); i=-1;}
			Vector thangsFound=new Vector();
			Environmental foundThang=null;
			Environmental E=mob.location().fetchFromRoomFavorItems(null,objectWish);
			foundThang=maybeAdd(E,thangsFound,foundThang);
			for(int m=0;m<CMMap.map.size();m++)
			{
				Room room=(Room)CMMap.map.elementAt(m);
				E=room.fetchFromRoomFavorMOBs(null,objectWish);
				foundThang=maybeAdd(E,thangsFound,foundThang);
			}
			for(int r=0;r<CMMap.map.size();r++)
			{
				Room room=(Room)CMMap.map.elementAt(r);
				for(int m=0;m<room.numInhabitants();m++)
				{
					MOB mob2=room.fetchInhabitant(m);
					if(mob2!=null)
					{
						E=mob2.fetchInventory(objectWish);
						foundThang=maybeAdd(E,thangsFound,foundThang);
						if(mob2 instanceof ShopKeeper)
						{
							E=((ShopKeeper)mob2).getStock(objectWish);
							foundThang=maybeAdd(E,thangsFound,foundThang);
						}
					}
				}
			}
			if((thangsFound.size()>0)&&(foundThang!=null))
			{
				// yea, we get to DO something!
				int experienceRequired=100*(foundThang.envStats().level()-1);
				if(foundThang instanceof MOB)
				{
					MOB newMOB=(MOB)foundThang.copyOf();
					newMOB.setStartRoom(null);
					newMOB.setLocation(mob.location());
					newMOB.recoverCharStats();
					newMOB.recoverEnvStats();
					newMOB.recoverMaxState();
					newMOB.resetToMaxState();
					newMOB.bringToLife(mob.location());
					mob.location().show(mob,null,Affect.MSG_OK_ACTION,"Suddenly, "+newMOB.name()+" instantiates from the Java plain.");
					newMOB.setFollowing(mob);
					Log.sysOut("Wish",mob.ID()+" wished for mob "+newMOB.ID()+".");
				}
				else
				if(foundThang instanceof Item)
				{
					Item newItem=(Item)foundThang.copyOf();
					newItem.setLocation(null);
					newItem.wearAt(0);
					mob.location().addItem(newItem);
					mob.location().show(mob,null,Affect.MSG_OK_ACTION,"Suddenly, "+newItem.name()+" drops from the sky.");
					mob.location().recoverRoomStats();
					Log.sysOut("Wish",mob.ID()+" wished for item "+newItem.ID()+".");
				}
				if(experienceRequired<=0) 
					experienceRequired=0;
				mob.tell("Your wish has drained you of "+(baseLoss+experienceRequired)+" experience points.");
				mob.setExperience(mob.getExperience()-experienceRequired);
				return true;
			}
			
			// anything else may refer to another person or item
			Environmental target=mob.location().fetchFromRoomFavorMOBs(null,(String)wishV.elementAt(0));
			if(target==null) 
				target=mob;
			else
			{ 
				wishV.removeElementAt(0); 
				myWish=" "+Util.combine(wishV,0).toUpperCase().trim()+" ";
			}
			
			// a wish for recall
			if((myWish.startsWith(" TO BE RECALLED "))
			||(myWish.startsWith(" TO RECALL "))
			||(myWish.startsWith(" RECALL "))
			||(myWish.endsWith(" TO RECALL "))
			||(myWish.endsWith(" TO BE RECALLED "))
			||(myWish.endsWith(" RECALL ")))
			{
				Room recallRoom=mob.getStartRoom();
				if((recallRoom==null)&&(target instanceof MOB)&&(((MOB)target).getStartRoom()!=null))
					recallRoom=((MOB)target).getStartRoom();
				if(recallRoom!=null)
				{
					mob.tell("Your wish has drained you of "+baseLoss+" experience points.");
					bringThangHere(mob,recallRoom,target);
					return true;
				}
			}
				
			// a wish for death or destruction
			if((myWish.startsWith(" TO DIE "))
			||(myWish.startsWith(" TO BE DESTROYED "))
			||(myWish.startsWith(" TO CROAK "))
			||(myWish.startsWith(" DEATH "))
			||(myWish.startsWith(" FOR DEATH "))
			||(myWish.startsWith(" DESTRUCTION "))
			||(myWish.startsWith(" TO BE BANISHED "))
			||(myWish.startsWith(" TO BE DEAD "))
			||(myWish.startsWith(" TO BE GONE "))
			||(myWish.startsWith(" TO DISAPPEAR "))
			||(myWish.startsWith(" TO VANISH "))
			||(myWish.startsWith(" TO BE INVISIBLE "))
			||(myWish.startsWith(" TO GO AWAY "))
			||(myWish.startsWith(" TO GO TO HELL ")))
			{
				int experienceRequired=10*(target.envStats().level()-1);
				mob.location().show(mob,null,Affect.MSG_OK_VISUAL,target.name()+" quietly vanishes.");
				if(target instanceof Item)
					((Item)target).destroyThis();
				if(target instanceof MOB)
				{ 
					((MOB)target).location().delInhabitant((MOB)target);
					((MOB)target).kill();
				}
				mob.tell("Your wish has drained you of "+baseLoss+" experience points.");
				return true;
			}
			
			// a wish for movement
			String locationWish=myWish;
			String[] redundantStarts2={"TO GO TO",
									  "TO TELEPORT TO",
									  "TO TRANSPORT TO",
									  "TO TRANSFER TO",
									  "TO PORTAL TO",
									  "TO BE TELEPORTED TO",
									  "TO BE TRANSPORTED TO",
									  "TO BE TRANSFERRED TO",
									  "TO BE PORTALLED TO",
									  "TO BE PORTALED TO",
									  "TO BE TELEPORTED",
									  "TO BE TRANSPORTED",
									  "TO BE TRANSFERRED",
									  "TO BE PORTALLED",
									  "TO BE PORTALED",
									  "TO APPEAR IN ",
									  "TO BE IN",
									  "TO APPEAR AT",
									  "TO BE AT",
									  "TO GO",
									  "TO MOVE TO",
									  "TO MOVE",
									  "TO BE AT",
									  "TO BE IN",
									  "TO BE",
									  "TO TRAVEL",
									  "TO WALK TO",
									  "TO WALK",
									  "TO TRAVEL TO",
									  "TO GOTO",
									  "TELEPORTATION TO",
									  "TRANSPORTED TO",
									  "TELEPORTED TO",
									  "TRANSFERRED TO",
									  "TELEPORT",
									  "GO",
									  "GOTO",
									  "TRANSFER",
									  "PORTAL",
									  "TELEPORTATION"};
			String[] redundantEnds2={"IMMEDIATELY","PLEASE","NOW","AT ONCE"};
			boolean validStart=false;
			for(int i=0;i<redundantStarts2.length;i++)
				while(locationWish.startsWith(" "+redundantStarts2[i]+" "))
				{	validStart=true; locationWish=locationWish.substring(1+redundantStarts2[i].length()); i=-1;}
			for(int i=0;i<redundantEnds2.length;i++)
				while(locationWish.endsWith(" "+redundantEnds2[i]+" "))
				{	locationWish=locationWish.substring(0,locationWish.length()-(1+redundantEnds2[i].length())); i=-1;}
			// a wish for teleportation
			if(validStart)
			{
				Room newRoom=null;
				int dir=Directions.getGoodDirectionCode((String)wishV.lastElement());
				if(dir>=0)
					newRoom=mob.location().doors()[dir];
				if(newRoom==null)
				for(int m=0;m<CMMap.map.size();m++)
				{
					Room room=(Room)CMMap.map.elementAt(m);
					if(CoffeeUtensils.containsString(room.displayText().toUpperCase(),locationWish))
					{
					   newRoom=room;
					   break;
					}
				}
				if(newRoom!=null)
				{
					bringThangHere(mob,newRoom,target);
					mob.tell("Your wish has drained you of "+baseLoss+" experience points.");
					return true;
				}
			}
			
			// temporary stat changes
			if((target instanceof MOB)
			&&((myWish.indexOf(" MORE ")>=0)
			||(myWish.indexOf(" HIGHER ")>=0)
			||(myWish.indexOf(" BIGGER ")>=0)
			||(myWish.indexOf(" TO HAVE ")>=0)))
			{
				MOB tm=(MOB)target;
				if((myWish.indexOf("HIT POINT")>=0)&&(tm.curState().getHitPoints()<tm.maxState().getHitPoints()))
				{
					tm.curState().setHitPoints(tm.maxState().getHitPoints());
					mob.tell("Your wish has drained you of "+baseLoss+" experience points.");
					return true;
				}
				else
				if(myWish.indexOf("HIT POINT")>=0)
				{
					tm.baseState().setHitPoints(tm.baseState().getHitPoints()+2);
					tm.recoverMaxState();
					mob.tell("Your wish has drained you of "+baseLoss+" experience points.");
					return true;
				}
				if((myWish.indexOf("MANA")>=0)&&(tm.curState().getMana()<tm.maxState().getMana()))
				{
					tm.curState().setMana(tm.maxState().getMana());
					mob.tell("Your wish has drained you of "+baseLoss+" experience points.");
					return true;
				}
				else
				if(myWish.indexOf("MANA")>=0)
				{
					tm.baseState().setMana(tm.baseState().getMana()+2);
					tm.recoverMaxState();
					mob.tell("Your wish has drained you of "+baseLoss+" experience points.");
					return true;
				}
				if((myWish.indexOf("MOVE")>=0)&&(tm.curState().getMovement()<tm.maxState().getMovement()))
				{
					tm.curState().setMovement(tm.maxState().getMovement());
					mob.tell("Your wish has drained you of "+baseLoss+" experience points.");
					return true;
				}
				else
				if(myWish.indexOf("MOVE")>=0)
				{
					tm.baseState().setMovement(tm.baseState().getMovement()+5);
					tm.recoverMaxState();
					mob.tell("Your wish has drained you of "+baseLoss+" experience points.");
					return true;
				}
				
			}
			if((target instanceof MOB)
			&&((myWish.indexOf(" MALE ")>=0)
			||(myWish.indexOf(" MAN ")>=0)
			||(myWish.indexOf(" BOY ")>=0)))
			{
				mob.tell("Your wish has drained you of "+baseLoss+" experience points.");
				((MOB)target).baseCharStats().setStat(CharStats.GENDER,'M');
				((MOB)target).recoverCharStats();
				return true;
			}
			if((target instanceof MOB)
			&&((myWish.indexOf(" MALE ")>=0)
			||(myWish.indexOf(" WOMAN ")>=0)
			||(myWish.indexOf(" GIRL ")>=0)))
			{
				mob.tell("Your wish has drained you of "+baseLoss+" experience points.");
				((MOB)target).baseCharStats().setStat(CharStats.GENDER,'F');
				((MOB)target).recoverCharStats();
				return true;
			}
			
			// change race
			if(target instanceof MOB)
			{
				Race R=CMClass.getRace((String)wishV.lastElement());
				if(R!=null)
				{
					mob.tell("Your wish has drained you of "+baseLoss+" experience points.");
					((MOB)target).baseCharStats().setMyRace(R);
					((MOB)target).recoverCharStats();
					return true;
				}
			}
			
			// change class
			if(target instanceof MOB)
			{
				CharClass C=CMClass.getCharClass((String)wishV.lastElement());
				if(C!=null)
				{
					mob.baseCharStats().getMyClass().unLevel(mob);
					((MOB)target).baseCharStats().setMyClass(C);
					((MOB)target).recoverCharStats();
					return true;
				}
			}
				
			// gaining new abilities!
			if((target instanceof MOB)
			&&((myWish.indexOf(" KNOW ")>=0)
			||(myWish.indexOf(" LEARN ")>=0)
			||(myWish.indexOf(" COULD ")>=0)
			||(myWish.indexOf(" BE TAUGHT ")>=0)
			||(myWish.indexOf(" BE ABLE TO ")>=0)
			||(myWish.indexOf(" ABLE TO ")>=0)
			||(myWish.indexOf(" CAST ")>=0)))
			{
				MOB tm=(MOB)target;
				Ability A=CMClass.getAbility((String)wishV.lastElement());
				if((A!=null)
				&&(CMAble.getQualifyingLevel("Archon",A.ID())>0))
				{
					if(tm.fetchAbility(A.ID())!=null)
					{
						A=tm.fetchAbility(A.ID());
						mob.tell("Your wish has drained you of "+baseLoss+" experience points.");
					}
					else
					{
						tm.addAbility(A);
						mob.baseCharStats().getMyClass().unLevel(mob);
					}
					A=tm.fetchAbility(A.ID());
					A.baseEnvStats().setLevel(tm.envStats().level());
					A.setProfficiency(100);
					A.autoInvocation(tm);
					return true;
				}
				
			}
			
			// attributes will be hairy
			int foundAttribute=-1;
			for(int attributes=0;attributes<CharStats.NUM_STATS;attributes++)
			{
				if(CoffeeUtensils.containsString(myWish,CharStats.TRAITS[attributes]))
				{	foundAttribute=attributes; break;}
			}
			if(myWish.indexOf("STRONG")>=0)
				foundAttribute=CharStats.STRENGTH;
			if(myWish.indexOf(" INTELLIGEN")>=0)
				foundAttribute=CharStats.INTELLIGENCE;
			if(myWish.indexOf(" WISE ")>=0)
				foundAttribute=CharStats.WISDOM;
			if(myWish.indexOf(" FAST")>=0)
				foundAttribute=CharStats.DEXTERITY;
			if(myWish.indexOf("DEXTROUS")>=0)
				foundAttribute=CharStats.DEXTERITY;
			if(myWish.indexOf(" HEALTH")>=0)
				foundAttribute=CharStats.CONSTITUTION;
			if(myWish.indexOf("PRETTY")>=0)
				foundAttribute=CharStats.CHARISMA;
			if((myWish.indexOf("RESIST")>=0)
			||(myWish.indexOf("IMMUN")>=0))
			{
				if(myWish.indexOf(" PARALY")>=0)
					foundAttribute=CharStats.SAVE_PARALYSIS;
				if(myWish.indexOf(" FIRE")>=0)
					foundAttribute=CharStats.SAVE_FIRE;
				if(myWish.indexOf(" FLAMES")>=0)
					foundAttribute=CharStats.SAVE_FIRE;
				if(myWish.indexOf(" COLD")>=0)
					foundAttribute=CharStats.SAVE_COLD;
				if(myWish.indexOf(" FROST")>=0)
					foundAttribute=CharStats.SAVE_COLD;
				if(myWish.indexOf(" GAS")>=0)
					foundAttribute=CharStats.SAVE_GAS;
				if(myWish.indexOf(" ACID")>=0)
					foundAttribute=CharStats.SAVE_ACID;
				if(myWish.indexOf(" SPELL ")>=0)
					foundAttribute=CharStats.SAVE_MAGIC;
				if(myWish.indexOf(" SPELLS ")>=0)
					foundAttribute=CharStats.SAVE_MAGIC;
				if(myWish.indexOf(" SONGS")>=0)
					foundAttribute=CharStats.SAVE_MIND;
				if(myWish.indexOf(" CHARMS")>=0)
					foundAttribute=CharStats.SAVE_MIND;
				if(myWish.indexOf(" ELECTRI")>=0)
					foundAttribute=CharStats.SAVE_ELECTRIC;
				if(myWish.indexOf(" POISON")>=0)
					foundAttribute=CharStats.SAVE_POISON;
				if(myWish.indexOf(" DEATH")>=0)
					foundAttribute=CharStats.SAVE_UNDEAD;
				if(myWish.indexOf(" UNDEAD")>=0)
					foundAttribute=CharStats.SAVE_UNDEAD;
				if(myWish.indexOf(" EVIL")>=0)
					foundAttribute=CharStats.SAVE_UNDEAD;
			}
			if((foundAttribute>0)
			&&(target instanceof MOB)
			&&((myWish.indexOf(" MORE ")>=0)
			||(myWish.indexOf(" HIGHER ")>=0)
			||(myWish.indexOf("RESIST")>=0)
			||(myWish.indexOf("IMMUN")>=0)
			||(myWish.indexOf(" BIGGER ")>=0)
			||(myWish.indexOf(" TO HAVE ")>=0)
			||(myWish.indexOf(" GAIN ")>=0)
			||(myWish.indexOf(" TO BE ")>=0)))
			{
				mob.baseCharStats().getMyClass().unLevel(mob);
				if(foundAttribute<=6)
					mob.baseCharStats().setStat(foundAttribute,mob.baseCharStats().getStat(foundAttribute)+1);
				else
					mob.baseCharStats().setStat(foundAttribute,mob.baseCharStats().getStat(foundAttribute)+33);
				return true;
			}
			if((foundAttribute>0)
			&&(target instanceof MOB)
			&&((myWish.indexOf(" LESS ")>=0)
			||(myWish.indexOf(" LOWER ")>=0)
			||(myWish.indexOf(" LESS RESIST")>=0)
			||(myWish.indexOf(" LESS IMMUN")>=0)
			||(myWish.indexOf(" NO RESIST")>=0)
			||(myWish.indexOf(" NO IMMUN")>=0)
			||(myWish.indexOf(" LOSE ")>=0)))
			{
				mob.baseCharStats().getMyClass().unLevel(mob);
				if(foundAttribute<=6)
					mob.baseCharStats().setStat(foundAttribute,mob.baseCharStats().getStat(foundAttribute)-1);
				else
					mob.baseCharStats().setStat(foundAttribute,mob.baseCharStats().getStat(foundAttribute)-33);
				return true;
			}
			
			mob.tell("Your attempted wish has cost you "+baseLoss+" experience points, but it was poorly worded.");
			return false;
		}
		return success;
	}
}