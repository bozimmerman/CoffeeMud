package com.planet_ink.coffee_mud.commands.sysop;


import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.db.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.Abilities.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.Exits.*;
import com.planet_ink.coffee_mud.Items.Armor.*;
import com.planet_ink.coffee_mud.Items.Weapons.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.interfaces.*;
import java.util.*;
import java.io.*;

public class Generic
{
	
	public final static long maxLength=60000;
	
	static void genName(MOB mob, Environmental E)
	{
		mob.tell("\n\r\n\rName: '"+E.name()+"'.");
		String newName=mob.session().prompt("Enter a new one\n\r:","");
		if(newName.length()>0)
			E.setName(newName);
		else
			mob.tell("(no change)");
	}
	
	static Room genRoomType(MOB mob, Room R)
		throws IOException
	{
		mob.tell("\n\r\n\rType: '"+INI.className(R)+"'");
		String newName=mob.session().prompt("Enter a new one\n\r:","");
		if(newName.length()>0)
		{
			Room newRoom=MUD.getLocale(newName);
			if(newRoom==null)
			{
				mob.tell("'"+newName+"' does not exist. No Change.");
			}
			else
			if(mob.session().confirm("This will change the room type of room '"+R.ID()+"'.  This is a dangerous procedure.  Are you absolutely sure (y/N)? ","N"))
			{
				mob.tell("\n\rWorking...");
				newRoom=(Room)newRoom.newInstance();
				for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
				{
					newRoom.exits()[d]=R.exits()[d];
					newRoom.doors()[d]=R.doors()[d];
				}
				for(int x=0;x<R.numInhabitants();x++)
				{
					newRoom.addInhabitant(R.fetchInhabitant(x));
					R.fetchInhabitant(x).setLocation(newRoom);
				}
				for(int x=0;x<R.numItems();x++)
					newRoom.addItem(R.fetchItem(x));
				for(Enumeration e=MOBloader.MOBs.elements();e.hasMoreElements();)
				{
					MOB mob2=(MOB)e.nextElement();
					if(mob2.getStartRoom()==R)
					{
						mob2.setStartRoom(newRoom);
						MOBloader.DBUpdate(mob2);
					}
				}
				newRoom.setID(R.ID());
				newRoom.setAreaID(R.getAreaID());
				newRoom.setDisplayText(R.displayText());
				newRoom.setDescription(R.description());
				for(int r=0;r<MUD.map.size();r++)
				{
					Room room=(Room)MUD.map.elementAt(r);
					if(room==R)
					{
						MUD.map.setElementAt(newRoom,r);
						room=newRoom;
					}
					for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
					{
						if(room.doors()[d]==R)
							room.doors()[d]=newRoom;
					}
					for(int i=0;i<room.numInhabitants();i++)
					{
						MOB mob2=room.fetchInhabitant(i);
						if(mob2.getStartRoom()==room)
							mob2.setStartRoom(newRoom);
					}
				}
				R=newRoom;
			}
			R.recoverRoomStats();
		}
		else
			mob.tell("(no change)");
		return R;
	}
	
	static void genDescription(MOB mob, Environmental E)
	{
		mob.tell("\n\r\n\rDescription: '"+E.description()+"'.");
		String newName=mob.session().prompt("Enter a new one\n\r:","");
		if(newName.length()>0)
			E.setDescription(newName);
		else
			mob.tell("(no change)");
	}
	
	static void genDisplayText(MOB mob, Environmental E)
	{
		mob.tell("\n\r\n\rDisplay: '"+E.displayText()+"'.");
		String newName=mob.session().prompt("Enter something new\n\r:","");
		if(newName.length()>0)
			E.setDisplayText(newName);
		else
			mob.tell("(no change)");
	}
	static void genClosedText(MOB mob, GenExit E)
	{
		mob.tell("\n\r\n\rClosed Text: '"+E.closedText()+"'.");
		String newName=mob.session().prompt("Enter something new\n\r:","");
		if(newName.length()>0)
			E.setExitParams(E.doorName(),E.closeWord(),E.openWord(),newName);
		else
			mob.tell("(no change)");
	}
	static void genDoorName(MOB mob, GenExit E)
	{
		mob.tell("\n\r\n\rDoor Name: '"+E.doorName()+"'.");
		String newName=mob.session().prompt("Enter something new\n\r:","");
		if(newName.length()>0)
			E.setExitParams(newName,E.closeWord(),E.openWord(),E.closedText());
		else
			mob.tell("(no change)");
	}
	
	static void genOpenWord(MOB mob, GenExit E)
	{
		mob.tell("\n\r\n\rOpen Word: '"+E.openWord()+"'.");
		String newName=mob.session().prompt("Enter something new\n\r:","");
		if(newName.length()>0)
			E.setExitParams(E.doorName(),E.closeWord(),newName,E.closedText());
		else
			mob.tell("(no change)");
	}
	static void genCloseWord(MOB mob, GenExit E)
	{
		mob.tell("\n\r\n\rClose Word: '"+E.closeWord()+"'.");
		String newName=mob.session().prompt("Enter something new\n\r:","");
		if(newName.length()>0)
			E.setExitParams(E.doorName(),newName,E.openWord(),E.closedText());
		else
			mob.tell("(no change)");
	}
	static void genExitMisc(MOB mob, GenExit E)
		throws IOException
	{
		if(E.hasALock())
		{
			E.setReadable(false);
			E.setClassRestricted(false);
			mob.tell("\n\r\n\rAssigned Key Item: '"+E.keyName()+"'.");
			String newName=mob.session().prompt("Enter something new\n\r:","");
			if(newName.length()>0)
				E.setKeyName(newName);
			else
				mob.tell("(no change)");
		}
		else
		if(E.hasADoor())
		{
			if(mob.session().confirm("Is this door readable (Y/N)",((E.isReadable())?"Y":"N")))
			{
				E.setReadable(true);
				mob.tell("\n\r\n\rText: '"+E.readableText()+"'.");
				String newName=mob.session().prompt("Enter something new\n\r:","");
				if(newName.length()>0)
					E.setReadableText(newName);
				else
					mob.tell("(no change)");
			}
			else
				E.setReadable(false);
		}
		else
		{
			E.setReadable(false);
			boolean ok=false;
			while(!ok)
			{
				if(!mob.session().confirm("Restricted to by class? (Y/N)     : ",((E.classRestricted())?"Y":"N")))
				{
					E.setClassRestricted(false);
					ok=true;
				}
				else
				{
					E.setClassRestricted(true);
					mob.tell("\n\r\n\rClass name: '"+E.classRestrictedName()+"'.");
					String newName=mob.session().prompt("Enter something new\n\r:","");
				
					for(int c=0;c<MUD.charClasses.size();c++)
					{
						CharClass C=(CharClass)MUD.charClasses.elementAt(c);
						if(C.name().equalsIgnoreCase(newName))
						{
							newName=C.ID();
							ok=true;
							break;
						}
					}
					if(ok)
					{
						if(newName.length()>0)
							E.setClassRestrictedName(newName);
						else
							mob.tell("(no change)");
					}
					else
					{
						mob.tell("\n\r'"+newName+"' is not a character class name!");
					}
				}
			}
		}
	}
	static void genGettable(MOB mob, Item E)
		throws IOException
	{
		E.setGettable(mob.session().confirm("Is this item gettable (Y/N)",((E.isGettable())?"Y":"N")));
		E.setDroppable(mob.session().confirm("Is this item droppable (Y/N)",((E.isDroppable())?"Y":"N")));
		E.setRemovable(mob.session().confirm("Is this item removable (Y/N)",((E.isRemovable())?"Y":"N")));
		E.setTrapped(mob.session().confirm("Is this item trapped (Y/N)",((E.isTrapped())?"Y":"N")));
		if(E instanceof GenReadable)
			E.setReadable(true);
		else
			E.setReadable(mob.session().confirm("Is this item readable (Y/N)",((E.isReadable())?"Y":"N")));
		if(E.isReadable())
		{
			mob.tell("\n\r\n\rAssigned Read Text: '"+E.readableText()+"'.");
			String newName=mob.session().prompt("Enter something new\n\r:","");
			if(newName.length()>0)
				E.setReadableText(newName);
			else
				mob.tell("(no change)");
		}
		Thief_Trap.setTrapped(E,E.isTrapped());
	}
	
	static void genLevelRestrict(MOB mob, GenExit E)
		throws IOException
	{
		if(mob.session().confirm("Restricted to level "+E.baseEnvStats().level()+" and above? (Y/N)     : ",((E.levelRestricted())?"Y":"N")))
			E.setLevelRestricted(true);
		else
			E.setLevelRestricted(false);
	}
			
	static void genDispositionTask(MOB mob, Environmental E, String prompt, int mask)
		throws IOException
	{
		prompt=Util.padRight(prompt,15);
		int current=E.baseEnvStats().disposition();
		boolean val=((current&mask)==mask);
		if(val)
			prompt+="(Y/n): ";
		else
			prompt+="(y/N): ";
		
		if(mob.session().confirm(prompt,val?"Y":"N"))
			E.baseEnvStats().setDisposition(current|mask);
		else
			E.baseEnvStats().setDisposition(current&((int)(Sense.ALLMASK-mask)));
	}
	
	static void genDisposition(MOB mob, Environmental E)
		throws IOException
	{
		genDispositionTask(mob,E,"Is Invisible?",Sense.IS_INVISIBLE);
		genDispositionTask(mob,E,"Is Hidden?",Sense.IS_HIDDEN);
		genDispositionTask(mob,E,"Is Unseeable?",Sense.IS_SEEN);
		if(E instanceof MOB)
		{
			genDispositionTask(mob,E,"Is Sneaking?",Sense.IS_SNEAKING);
			genDispositionTask(mob,E,"Is Flying?",Sense.IS_FLYING);
			genDispositionTask(mob,E,"Is Magical?",Sense.IS_BONUS);
		}
		genDispositionTask(mob,E,"Is Glowing?",Sense.IS_LIGHT);
	}
			
	static void genSensesTask(MOB mob, Environmental E, String prompt, int mask)
		throws IOException
	{
		prompt=Util.padRight(prompt,15);
		int current=E.baseEnvStats().sensesMask();
		boolean val=((current&mask)==mask);
		if(val)
			prompt+="(Y/n): ";
		else
			prompt+="(y/N): ";
		
		if(mob.session().confirm(prompt,val?"Y":"N"))
			E.baseEnvStats().setSensesMask(current|mask);
		else
			E.baseEnvStats().setSensesMask(current&((int)(Sense.ALLMASK-mask)));
	}
	
	static void genSensesMask(MOB mob, Environmental E)
		throws IOException
	{
		genSensesTask(mob,E,"Can see in the dark?",Sense.CAN_SEE_DARK);
		genSensesTask(mob,E,"Can see hidden?",Sense.CAN_SEE_HIDDEN);
		genSensesTask(mob,E,"Can see invisible?",Sense.CAN_SEE_INVISIBLE);
		genSensesTask(mob,E,"Can see sneakers?",Sense.CAN_SEE_SNEAKERS);
		genSensesTask(mob,E,"Has infravision?",Sense.CAN_SEE_INFRARED);
		genSensesTask(mob,E,"Can see good?",Sense.CAN_SEE_GOOD);
		genSensesTask(mob,E,"Can see evil?",Sense.CAN_SEE_EVIL);
		genSensesTask(mob,E,"Is Mute?",Sense.CAN_SPEAK);
		genSensesTask(mob,E,"Is Deaf?",Sense.CAN_HEAR);
		genSensesTask(mob,E,"Is Blind?",Sense.CAN_SEE);
	}
			
	static void genDoorsNLocks(MOB mob, GenExit E)
		throws IOException
	{
		if(mob.session().confirm("Has a door (Y/N)     : ",((E.hasADoor())?"Y":"N")))
		{
			E.setHasDoor(true);
			E.setOpen(false);
			E.setDefaultsClosed(mob.session().confirm("Defaults closed (Y/N): ",((E.defaultsClosed())?"Y":"N")));
			if(mob.session().confirm("Has a lock (Y/N)     : ",((E.hasALock())?"Y":"N")))
			{
				E.setHasLock(true);
				E.setLocked(true);
				E.setDefaultsLocked(mob.session().confirm("Defaults locked (Y/N): ",((E.defaultsLocked())?"Y":"N")));
			}
			else
			{
				E.setHasLock(false);
				E.setLocked(false);
				E.setDefaultsLocked(false);
			}
			mob.tell("\n\r\n\rReset Delay (# ticks): '"+E.openDelayTicks()+"'.");
			int newLevel=Util.s_int(mob.session().prompt("Enter a new delay\n\r:",""));
			if(newLevel>0)
				E.setOpenDelayTicks(newLevel);
			else
				mob.tell("(no change)");
		}
		else
		{
			E.setHasDoor(false);
			E.setOpen(false);
			E.setDefaultsClosed(false);
			E.setHasLock(false);
			E.setLocked(false);
			E.setDefaultsLocked(false);
		}
		
		E.setTrapped(mob.session().confirm("\n\r\n\rIs this exit trapped (Y/N)     : ",((E.isTrapped())?"Y":"N")));
		Thief_Trap.setTrapped(E,E.isTrapped());
	}
	
	static void genLidsNLocks(MOB mob, GenContainer E)
		throws IOException
	{
		E.setGettable(mob.session().confirm("Is this item gettable (Y/N)",((E.isGettable())?"Y":"N")));
		if(mob.session().confirm("Has a lid  (Y/N)     : ",((E.hasALid)?"Y":"N")))
		{
			E.hasALid=true;
			E.isOpen=false;
			if(mob.session().confirm("Has a lock (Y/N)     : ",((E.hasALock)?"Y":"N")))
			{
				E.hasALock=true;
				E.isLocked=true;
				mob.tell("\n\r\n\rText: '"+E.keyName+"'.");
				String newName=mob.session().prompt("Enter something new\n\r:","");
				if(newName.length()>0)
					E.keyName=newName;
				else
					mob.tell("(no change)");
			}
			else
			{
				E.keyName="";
				E.hasALock=false;
				E.isLocked=false;
			}
		}
		else
		{
			E.keyName="";
			E.hasALid=false;
			E.hasALock=false;
			E.isOpen=true;
			E.isLocked=false;
		}
		E.setTrapped(mob.session().confirm("\n\r\n\rIs this container trapped (Y/N)     : ",((E.isTrapped())?"Y":"N")));
		Thief_Trap.setTrapped(E,E.isTrapped());
	}
	
	static void genLevel(MOB mob, Environmental E)
	{
		if(E.baseEnvStats().level()<0) 
			E.baseEnvStats().setLevel(1);
		mob.tell("\n\r\n\rLevel: '"+E.baseEnvStats().level()+"'.");
		int newLevel=Util.s_int(mob.session().prompt("Enter a new one\n\r:",""));
		if(newLevel>0)
			E.baseEnvStats().setLevel(newLevel);
		else
			mob.tell("(no change)");
	}
	
	static void genRejuv(MOB mob, Environmental E)
	{
		mob.tell("\n\r\n\rRejuv Ticks: '"+E.baseEnvStats().rejuv()+"'.");
		String rlevel=mob.session().prompt("Enter new amount\n\r:","");
		int newLevel=Util.s_int(rlevel);
		if((newLevel>0)||(rlevel.trim()=="0"))
		{
			E.baseEnvStats().setRejuv(newLevel);
			if(E.baseEnvStats().rejuv()==0)
			{
				E.baseEnvStats().setRejuv(Integer.MAX_VALUE);
				mob.tell(E.name()+" will now never rejuvinate.");
			}
		}
		else
			mob.tell("(no change)");
	}
	
	static void genUses(MOB mob, Item E)
	{
		mob.tell("\n\r\n\rUses Remaining: '"+E.usesRemaining()+"'.");
		int newLevel=Util.s_int(mob.session().prompt("Enter a new value\n\r:",""));
		if(newLevel>0)
			E.setUsesRemaining(newLevel);
		else
			mob.tell("(no change)");
	}
	
	static void genMiscText(MOB mob, Environmental E)
		throws IOException
	{
		if(E instanceof GenContainer)
			modifyGenContainer(mob,(GenContainer)E);
		else
		if(E instanceof GenUndead)
			modifyGenUndead(mob,(GenUndead)E);
		else
		if(E instanceof GenShopkeeper)
			modifyGenShopkeeper(mob,(GenShopkeeper)E);
		else
		if(E instanceof GenMob)
			modifyGenMOB(mob,(GenMob)E);
		else
		if(E instanceof GenExit)
			modifyGenExit(mob,(GenExit)E);
		else
		if(E instanceof GenArmor)
			modifyGenArmor(mob,(GenArmor)E);
		else
		if(E instanceof GenWeapon)
			modifyGenWeapon(mob,(GenWeapon)E);
		else
		if(E instanceof GenFood)
			modifyGenFood(mob,(GenFood)E);
		else
		if(E instanceof GenWater)
			modifyGenDrink(mob,(GenWater)E);
		else
		if(E instanceof GenItem)
			modifyGenItem(mob,(GenItem)E);
		else
		{
			mob.tell("\n\r\n\rMisc Text: '"+E.text()+"'.");
			String newText=mob.session().prompt("Re-enter now ('null'=='')\n\r:","");
			if(newText.equalsIgnoreCase("NULL"))
				E.setMiscText(newText);
			else
			if(newText.length()>0)
				E.setMiscText(newText);
			else
				mob.tell("(no change)");
		}
		
	}
	
	static void genAbility(MOB mob, Environmental E)
	{
		mob.tell("\n\r\n\rMagical Ability: '"+E.baseEnvStats().ability()+"'.");
		String newLevelStr=mob.session().prompt("Enter a new one (0=no magic)\n\r:","");
		int newLevel=Util.s_int(newLevelStr);
		if(newLevel>0)
			E.baseEnvStats().setAbility(newLevel);
		else
		if((newLevelStr.length()>0)&&(newLevelStr.indexOf("0")>=0))
			E.baseEnvStats().setAbility(0);
		else
			mob.tell("(no change)");
	}
	
	static void genHitPoints(MOB mob, Environmental E)
	{
		if(E.baseEnvStats().ability()<1) E.baseEnvStats().setAbility(11);
		mob.tell("\n\r\n\rHit Points/Level Modifier (hp=((10*level) + (random*level*THIS))) : '"+E.baseEnvStats().ability()+"'.");
		String newLevelStr=mob.session().prompt("Enter a new value\n\r:","");
		int newLevel=Util.s_int(newLevelStr);
		if(newLevel>0)
			E.baseEnvStats().setAbility(newLevel);
		else
			mob.tell("(no change)");
	}
	
	static void genValue(MOB mob, Item E)
	{
		mob.tell("\n\r\n\rAdjusted Value: '"+E.value()+"'.");
		int newValue=Util.s_int(mob.session().prompt("Enter a new value\n\r:",""));
		if(newValue>0)
			E.setBaseValue(newValue);
		else
			mob.tell("(no change)");
	}
	
	static void genWeight(MOB mob, Environmental E)
	{
		mob.tell("\n\r\n\rWeight: '"+E.baseEnvStats().weight()+"'.");
		int newValue=Util.s_int(mob.session().prompt("Enter a new weight\n\r:",""));
		if(newValue>0)
			E.baseEnvStats().setWeight(newValue);
		else
			mob.tell("(no change)");
	}
	
	
	static void genCapacity(MOB mob, Item E)
	{
		mob.tell("\n\r\n\rCapacity: '"+E.capacity()+"'.");
		int newValue=Util.s_int(mob.session().prompt("Enter a new capacity\n\r:",""));
		if(newValue>0)
			E.setCapacity(newValue);
		else
			mob.tell("(no change)");
		
	}
	
	static void genAttack(MOB mob, Environmental E)
	{
		mob.tell("\n\r\n\rAttackAdjustment: '"+E.envStats().attackAdjustment()+"'.");
		int newValue=Util.s_int(mob.session().prompt("Enter a new value\n\r:",""));
		if(newValue>0)
			E.baseEnvStats().setAttackAdjustment(newValue);
		else
			mob.tell("(no change)");
	}
	
	static void genDamage(MOB mob, Environmental E)
	{
		mob.tell("\n\r\n\rDamage/Hit: '"+E.baseEnvStats().damage()+"'.");
		int newValue=Util.s_int(mob.session().prompt("Enter a new value\n\r:",""));
		if(newValue>0)
			E.baseEnvStats().setDamage(newValue);
		else
			mob.tell("(no change)");
	}
	
	static void genSpeed(MOB mob, Environmental E)
	{
		mob.tell("\n\r\n\rAttacks/Round: '"+((int)Math.round(E.baseEnvStats().speed()))+"'.");
		int newValue=Util.s_int(mob.session().prompt("Enter a new value\n\r:",""));
		if(newValue>0)
			E.baseEnvStats().setSpeed(newValue);
		else
			mob.tell("(no change)");
	}
	
	static void genArmor(MOB mob, Environmental E)
	{
		mob.tell("\n\r\n\rArmor (lower-better): '"+E.baseEnvStats().armor()+"'.");
		int newValue=Util.s_int(mob.session().prompt("Enter a new value\n\r:",""));
		if(newValue>0)
			E.baseEnvStats().setArmor(newValue);
		else
			mob.tell("(no change)");
	}
	
	static void genMoney(MOB mob, MOB E)
	{
		mob.tell("\n\r\n\rMoney: '"+E.getMoney()+"'.");
		int newValue=Util.s_int(mob.session().prompt("Enter a new value\n\r:",""));
		if(newValue>0)
			E.setMoney(newValue);
		else
			mob.tell("(no change)");
	}
	
	static void genWeaponType(MOB mob, Weapon E)
		throws IOException
	{
		mob.tell("\n\r\n\rWeapon Attack Type: '"+E.typeDescription(E.weaponType)+"'.");
		String newType=mob.session().choose("Enter a new value (N/S/P/B)\n\r:","NSPB","");
		int newValue=-1;
		if(newType.length()>0)
			newValue=("NSPB").indexOf(newType.toUpperCase());
		if(newValue>=0)
			E.weaponType=newValue;
		else
			mob.tell("(no change)");
	}
	
	static void genMaterialCode(MOB mob, Armor E)
		throws IOException
	{
		mob.tell("\n\r\n\rArmor Material Type: '"+E.materialDescription(E.material())+"'.");
		String newType=mob.session().choose("Enter a new value (C/L/M/I/W)\n\r:","CLMIW","");
		int newValue=-1;
		if(newType.length()>0)
			newValue=("CMLIW").indexOf(newType.toUpperCase());
		if(newValue>=0)
			E.setMaterial(newValue);
		else
			mob.tell("(no change)");
	}
	
	static void genAlignment(MOB mob, MOB E)
		throws IOException
	{
		mob.tell("\n\r\n\rAlignment: '"+Scoring.alignmentStr(E)+"'.");
		String newType=mob.session().choose("Enter a new alignment (G/N/E)\n\r:","GNE","");
		int newValue=-1;
		if(newType.length()>0)
			newValue=("GNE").indexOf(newType.toUpperCase());
		if(newValue>=0)
			switch(newValue)
			{
			case 0:
				E.setAlignment(1000);
				break;
			case 1:
				E.setAlignment(500);
				break;
			case 2:
				E.setAlignment(0);
				break;
			}
		else
			mob.tell("(no change)");
	}
	
	static void genGender(MOB mob, MOB E)
		throws IOException
	{
		mob.tell("\n\r\n\rGender: '"+Character.toUpperCase((char)E.baseCharStats().getGender())+"'.");
		String newType=mob.session().choose("Enter a new gender (M/F)\n\r:","MF","");
		int newValue=-1;
		if(newType.length()>0)
			newValue=("MF").indexOf(newType.toUpperCase());
		if(newValue>=0)
			switch(newValue)
			{
			case 0:
				E.baseCharStats().setGender('M');
				break;
			case 1:
				E.baseCharStats().setGender('F');
				break;
			}
		else
			mob.tell("(no change)");
	}
	
	static void genWeaponClassification(MOB mob, Weapon E)
		throws IOException
	{
		mob.tell("\n\r\n\rWeapon Classification: '"+E.classifictionDescription(E.weaponClassification)+"'.");
		String newType=mob.session().choose("Enter a new value (a/b/e/f/h/k/p/r/s)\n\r:","ABEFHKPRS","");
		int newValue=-1;
		if(newType.length()>0)
			newValue=("ABEFHKPRS").indexOf(newType.toUpperCase());
		if(newValue>=0)
			E.weaponClassification=newValue;
		else
			mob.tell("(no change)");
	}
	
	static void genWeaponHands(MOB mob, Weapon E)
		throws IOException
	{
		mob.tell("\n\r\n\rTwo-Handed?: '"+(E.rawLogicalAnd()?"YES":"NO")+"'.");
		String newType=mob.session().choose("Enter a new value (y/N)\n\r:","YN","N");
		if((newType.length()>0))
		{
			if(newType.equalsIgnoreCase("Y"))
				E.setRawLogicalAnd(true);
			else
				E.setRawLogicalAnd(false);
		}
		else
			mob.tell("(no change)");
	}
	
	static void genSecretIdentity(MOB mob, Item E)
	{
		mob.tell("\n\r\n\rSecret Identity: '"+E.secretIdentity()+"'.");
		String newValue=mob.session().prompt("Enter a new identity\n\r:","");
		if(newValue.length()>0)
			E.setSecretIdentity(newValue);
		else
			mob.tell("(no change)");
	}
	
	static void genNourishment(MOB mob, Food E)
	{
		mob.tell("\n\r\n\rNourishment/Eat: '"+E.amountOfNourishment+"'.");
		int newValue=Util.s_int(mob.session().prompt("Enter a new amount\n\r:",""));
		if(newValue>0)
			E.amountOfNourishment=newValue;
		else
			mob.tell("(no change)");
	}
	
	static void genBehaviors(MOB mob, MOB E)
	{
		String behave="NO";
		while(behave.length()>0)
		{
			String behaviorstr="";
			for(int b=0;b<E.numBehaviors();b++)
				behaviorstr+=E.fetchBehavior(b).ID()+", ";
			if(behaviorstr.length()>0)
				behaviorstr=behaviorstr.substring(0,behaviorstr.length()-2);
			mob.tell("\n\r\n\rBehaviors: '"+behaviorstr+"'.");
			behave=mob.session().prompt("Enter a behavior to add/remove\n\r:","");
			if(behave.length()>0)
			{
				if(behave.equalsIgnoreCase("LIST"))
					mob.tell(Lister.reallyList(MUD.behaviors,-1).toString());
				else
				{
					Behavior chosenOne=null;
					for(int b=0;b<E.numBehaviors();b++)
					{
						Behavior B=E.fetchBehavior(b);
						if(B.ID().equalsIgnoreCase(behave))
							chosenOne=B;
					}
					if(chosenOne!=null)
					{
						mob.tell(chosenOne.ID()+" removed.");
						E.delBehavior(chosenOne);
					}
					else
					{
						chosenOne=(Behavior)MUD.getBehavior(behave);
						if(chosenOne!=null)
						{
							boolean alreadyHasIt=false;
							for(int b=0;b<E.numBehaviors();b++)
							{
								Behavior B=E.fetchBehavior(b);
								if(B.ID().equals(chosenOne.ID()))
									alreadyHasIt=true;
							}
							if(!alreadyHasIt)
								mob.tell(chosenOne.ID()+" added.");
							else
								mob.tell(chosenOne.ID()+" re-added.");
							if(!alreadyHasIt)
								E.addBehavior(chosenOne.newInstance());
						}
						else
						{
							mob.tell("'"+behave+"' is not recognized.  Try 'LIST'.");
						}
					}
				}
			}
			else
				mob.tell("(no change)");
		}
	}
	
	static void genShopkeeper(MOB mob, ShopKeeper E)
		throws IOException
	{
		mob.tell("\n\r\n\rShopekeeper type: '"+E.storeKeeperString()+"'.");
		String newType=mob.session().choose("Enter a new value (*/G/A/M/W/P/L/O/T/C)\n\r:","*GAMWPLOTC","");
		int newValue=-1;
		if(newType.length()>0)
			newValue=("*GAMWPLOTC").indexOf(newType.toUpperCase());
		if(newValue>=0)
		{
			if(E.whatISell!=newValue)
			{
				Vector V=E.getUniqueStoreInventory();
				for(int b=0;b<V.size();b++)
					E.delStoreInventory((Environmental)V.elementAt(b));
			}
			E.whatISell=newValue;
		}			
			
		
		String itemstr="NO";
		while(itemstr.length()>0)
		{
			String inventorystr="";
			Vector V=E.getUniqueStoreInventory();
			for(int b=0;b<V.size();b++)
				inventorystr+=INI.className(V.elementAt(b))+" ("+E.numberInStock(INI.className(V.elementAt(b)))+"), ";
			if(inventorystr.length()>0)
				inventorystr=inventorystr.substring(0,inventorystr.length()-2);
			mob.tell("\n\r\n\rInventory: '"+inventorystr+"'.");
			itemstr=mob.session().prompt("Enter something to add/remove\n\r:","");
			if(itemstr.length()>0)
			{
				if(itemstr.equalsIgnoreCase("LIST"))
				{
					mob.tell(Lister.reallyList(MUD.abilities,-1).toString());
					mob.tell(Lister.reallyList(MUD.armor,-1).toString());
					mob.tell(Lister.reallyList(MUD.weapons,-1).toString());
					mob.tell(Lister.reallyList(MUD.miscMagic,-1).toString());
					mob.tell(Lister.reallyList(MUD.items,-1).toString());
					mob.tell(Lister.reallyList(MUD.MOBs,-1).toString());
				}
				else
				{
					Environmental item=E.getStock(itemstr);
					if(item!=null)
					{
						mob.tell(item.ID()+" removed.");
						E.delStoreInventory(item.copyOf());
					}
					else
					{
						item=MUD.getItem(itemstr);
						if(item==null)
							item=MUD.getAbility(itemstr);
						if(item==null)
							item=MUD.getMOB(itemstr);
					
						if(item!=null)
						{
							boolean ok=E.doISellThis(item);
							if((item instanceof Ability)&&((E.whatISell==E.TRAINER)||(E.whatISell==E.CASTER)))
								ok=true;
							else
							if(E.whatISell==E.ONLYBASEINVENTORY)
								ok=true;
							if(!ok)
							{
								mob.tell("The shopkeeper does not sell that.");
							}
							else
							{
								boolean alreadyHasIt=false;
							
								if(E.doIHaveThisInStock(item.name()))
								   alreadyHasIt=true;
							
								if(!alreadyHasIt)
								{
									mob.tell(item.ID()+" added.");
									int num=1;
									if(!(item instanceof Ability))
										num=Util.s_int(mob.session().prompt("How many? :",""));
									E.addStoreInventory(item,num);
								}
							}
						}
						else
						{
							mob.tell("'"+itemstr+"' is not recognized.  Try 'LIST'.");
						}
					}
				}
			}
			else
				mob.tell("(no change)");
		}
	}
	
	static void genAbilities(MOB mob, MOB E)
	{
		String behave="NO";
		while(behave.length()>0)
		{
			String abilitiestr="";
			for(int b=0;b<E.numAbilities();b++)
				abilitiestr+=E.fetchAbility(b).ID()+", ";
			if(abilitiestr.length()>0)
				abilitiestr=abilitiestr.substring(0,abilitiestr.length()-2);
			mob.tell("\n\r\n\rAbilities: '"+abilitiestr+"'.");
			behave=mob.session().prompt("Enter an ability to add/remove\n\r:","");
			if(behave.length()>0)
			{
				if(behave.equalsIgnoreCase("LIST"))
					mob.tell(Lister.reallyList(MUD.abilities,-1).toString());
				else
				{
					Ability chosenOne=null;
					for(int b=0;b<E.numAbilities();b++)
					{
						Ability B=E.fetchAbility(b);
						if(B.ID().equalsIgnoreCase(behave))
							chosenOne=B;
					}
					if(chosenOne!=null)
					{
						mob.tell(chosenOne.ID()+" removed.");
						E.delAbility(chosenOne);
						if(E.fetchAffect(chosenOne.ID())!=null)
							E.delAffect(E.fetchAffect(chosenOne.ID()));
					}
					else
					{
						chosenOne=(Ability)MUD.getAbility(behave);
						if(chosenOne!=null)
						{
							boolean alreadyHasIt=false;
							for(int b=0;b<E.numAbilities();b++)
							{
								Ability B=E.fetchAbility(b);
								if(B.ID().equals(chosenOne.ID()))
									alreadyHasIt=true;
							}
							if(!alreadyHasIt)
								mob.tell(chosenOne.ID()+" added.");
							else
								mob.tell(chosenOne.ID()+" re-added.");
							if(!alreadyHasIt)
							{
								E.addAbility((Ability)chosenOne.copyOf());
								chosenOne.autoInvocation(mob);
							}
						}
						else
						{
							mob.tell("'"+behave+"' is not recognized.  Try 'LIST'.");
						}
					}
				}
			}
			else
				mob.tell("(no change)");
		}
	}
	
	static void genWornLocation(MOB mob, Item E)
	{
		int codeVal=-1;
		while(codeVal!=0)
		{
			mob.tell("\n\r\n\rWearing parameters\n\r0: Done");
			if(!E.rawLogicalAnd())
				mob.tell("1: Able to worn on any ONE of these locations:");
			else
				mob.tell("1: Must be worn on ALL of these locations:");
			int maxCode=1;
			for(int l=0;l<16;l++)
			{
				int wornCode=new Double(Math.pow(2.0,new Integer(l).doubleValue())).intValue();
				if(Sense.wornLocation(wornCode).length()>0)
				{
					String header=(l+2)+": ("+Sense.wornLocation(wornCode)+") : "+(((E.rawProperLocationBitmap()&wornCode)==wornCode)?"YES":"NO");
					mob.tell(header);
					maxCode=l+2;
				}
			}
			codeVal=Util.s_int(mob.session().prompt("Select an option number above to TOGGLE\n\r:"));
			if(codeVal>0)
			{
				if(codeVal==1)
					E.setRawLogicalAnd(!E.rawLogicalAnd());
				else
				{
					int wornCode=new Double(Math.pow(2.0,new Integer(codeVal-2).doubleValue())).intValue();
					if((E.rawProperLocationBitmap()&wornCode)==wornCode)
						E.setRawProperLocationBitmap(E.rawProperLocationBitmap()-wornCode);
					else
						E.setRawProperLocationBitmap(E.rawProperLocationBitmap()|wornCode);
				}
			}
		}
	}
	
	static void genThirstQuenched(MOB mob, Drink E)
	{
		mob.tell("\n\r\n\rQuenched/Drink: '"+E.amountOfThirstQuenched+"'.");
		int newValue=Util.s_int(mob.session().prompt("Enter a new amount\n\r:",""));
		if(newValue>0)
			E.amountOfThirstQuenched=newValue;
		else
			mob.tell("(no change)");
	}
	
	static void genDrinkHeld(MOB mob, Drink E)
	{
		mob.tell("\n\r\n\rAmount of Drink Held: '"+E.amountOfLiquidHeld+"'.");
		int newValue=Util.s_int(mob.session().prompt("Enter a new amount\n\r:",""));
		if(newValue>0)
		{
			E.amountOfLiquidHeld=newValue;
			E.amountOfLiquidRemaining=newValue;
		}
		else
			mob.tell("(no change)");
	}
	

	
	public static void modifyGenFood(MOB mob, GenFood me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		while(!ok)
		{
			genName(mob,me);
			genDisplayText(mob,me);
			genDescription(mob,me);
			genSecretIdentity(mob,me);
			genValue(mob,me);
			genWeight(mob,me);
			genNourishment(mob,me);
			genDisposition(mob,me);
			ok=true;
			if(me.text().length()>=maxLength)
			{
				mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
				ok=false;
			}
		}
		mob.recoverEnvStats();
	}
	public static void modifyGenDrink(MOB mob, GenWater me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		while(!ok)
		{
			genName(mob,me);
			genDisplayText(mob,me);
			genDescription(mob,me);
			genSecretIdentity(mob,me);
			genValue(mob,me);
			genWeight(mob,me);
			genThirstQuenched(mob,me);
			genDrinkHeld(mob,me);
			me.setGettable(mob.session().confirm("Is this item gettable (Y/N)",((me.isGettable())?"Y":"N")));
			genDisposition(mob,me);
			ok=true;
			if(me.text().length()>=maxLength)
			{
				mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
				ok=false;
			}
		}
		mob.recoverEnvStats();
	}
	
	public static void modifyGenItem(MOB mob, GenItem me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		while(!ok)
		{
			genName(mob,me);
			genLevel(mob,me);
			genDisplayText(mob,me);
			genDescription(mob,me);
			genSecretIdentity(mob,me);
			genGettable(mob,me);
			genValue(mob,me);
			genWeight(mob,me);
			genDisposition(mob,me);
			ok=true;
			if(me.text().length()>=maxLength)
			{
				mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
				ok=false;
			}
		}
		mob.recoverEnvStats();
	}
	
	public static void modifyGenReadable(MOB mob, GenReadable me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		while(!ok)
		{
			genName(mob,me);
			genLevel(mob,me);
			genDisplayText(mob,me);
			genDescription(mob,me);
			genGettable(mob,me);
			genValue(mob,me);
			genWeight(mob,me);
			genDisposition(mob,me);
			ok=true;
			if(me.text().length()>=maxLength)
			{
				mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
				ok=false;
			}
		}
		mob.recoverEnvStats();
	}
	
	public static void modifyGenContainer(MOB mob, GenContainer me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		while(!ok)
		{
			genName(mob,me);
			genLevel(mob,me);
			genDisplayText(mob,me);
			genDescription(mob,me);
			genCapacity(mob,me);
			genLidsNLocks(mob,me);
			genSecretIdentity(mob,me);
			genValue(mob,me);
			genWeight(mob,me);
			genDisposition(mob,me);
			ok=true;
			if(me.text().length()>=maxLength)
			{
				mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
				ok=false;
			}
		}
		mob.recoverEnvStats();
	}
	
	public static void modifyGenWeapon(MOB mob, GenWeapon me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		while(!ok)
		{
			genName(mob,me);
			genDisplayText(mob,me);
			genDescription(mob,me);
			genAttack(mob,me);
			genDamage(mob,me);
			genWeaponType(mob,me);
			genWeaponClassification(mob,me);
			genWeaponHands(mob,me);
			genLevel(mob,me);
			genAbility(mob,me);
			genSecretIdentity(mob,me);
			genGettable(mob,me);
			genValue(mob,me);
			genWeight(mob,me);
			genDisposition(mob,me);
			ok=true;
			if(me.text().length()>=maxLength)
			{
				mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
				ok=false;
			}
		}
		mob.recoverEnvStats();
	}
	public static void modifyGenArmor(MOB mob, GenArmor me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		while(!ok)
		{
			genName(mob,me);
			genDisplayText(mob,me);
			genDescription(mob,me);
			genMaterialCode(mob,me);
			genWornLocation(mob,me);
			genLevel(mob,me);
			genArmor(mob,me);
			genAbility(mob,me);
			genCapacity(mob,me);
			genSecretIdentity(mob,me);
			genGettable(mob,me);
			genValue(mob,me);
			genWeight(mob,me);
			genDisposition(mob,me);
			ok=true;
			if(me.text().length()>=maxLength)
			{
				mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
				ok=false;
			}
		}
		mob.recoverEnvStats();
	}
	public static void modifyGenExit(MOB mob, GenExit me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		while(!ok)
		{
			genName(mob,me);
			genDescription(mob,me);
			genDisplayText(mob,me);
			genLevel(mob,me);
			genDoorsNLocks(mob,me);
			if(me.hasADoor())
			{
				genClosedText(mob,me);
				genDoorName(mob,me);
				genOpenWord(mob,me);
				genCloseWord(mob,me);
			}
			genExitMisc(mob,me);
			genLevelRestrict(mob,me);
			genDisposition(mob,me);
			ok=true;
			if(me.text().length()>=maxLength)
			{
				mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
				ok=false;
			}
		}
		mob.recoverEnvStats();
	}
	public static void modifyGenMOB(MOB mob, GenMob me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		while(!ok)
		{
			genName(mob,me);
			genDisplayText(mob,me);
			genDescription(mob,me);
			int oldLevel=me.baseEnvStats().level();
			genLevel(mob,me);
			if((oldLevel==0)&&(me.baseEnvStats().level()>0))
				me.baseCharStats().getMyClass().buildMOB(me,me.baseEnvStats().level(),0,150,0,'M');
			genGender(mob,me);
			genSpeed(mob,me);
			if((oldLevel==0)&&(me.baseEnvStats().level()>0))
				me.baseEnvStats().setDamage((int)Math.round(Util.div(me.baseEnvStats().damage(),me.baseEnvStats().speed())));
			genAttack(mob,me);
			genDamage(mob,me);
			genArmor(mob,me);
			genHitPoints(mob,me);
			genAlignment(mob,me);
			genMoney(mob,me);
			genWeight(mob,me);
			genAbilities(mob,me);
			genBehaviors(mob,me);
			genDisposition(mob,me);
			genSensesMask(mob,me);
			ok=true;
			if(me.text().length()>=maxLength)
			{
				mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
				ok=false;
			}
		}
		
		mob.setMiscText(mob.text());
		mob.recoverMaxState();
		mob.recoverCharStats();
		mob.recoverEnvStats();
		mob.tell("\n\rNow don't forget to equip him with non-generic items before saving! If you DO add items to his list, be sure to come back here in case you've exceeded the string limit again.\n\r");
	}
	public static void modifyGenUndead(MOB mob, GenUndead me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		while(!ok)
		{
			genName(mob,me);
			genDisplayText(mob,me);
			genDescription(mob,me);
			int oldLevel=me.baseEnvStats().level();
			genLevel(mob,me);
			if((oldLevel==0)&&(me.baseEnvStats().level()>0))
				me.baseCharStats().getMyClass().buildMOB(me,me.baseEnvStats().level(),0,150,0,'M');
			genGender(mob,me);
			genSpeed(mob,me);
			if((oldLevel==0)&&(me.baseEnvStats().level()>0))
				me.baseEnvStats().setDamage((int)Math.round(Util.div(me.baseEnvStats().damage(),me.baseEnvStats().speed())));
			genAttack(mob,me);
			genDamage(mob,me);
			genArmor(mob,me);
			genHitPoints(mob,me);
			genAlignment(mob,me);
			genMoney(mob,me);
			genWeight(mob,me);
			genAbilities(mob,me);
			genBehaviors(mob,me);
			genDisposition(mob,me);
			genSensesMask(mob,me);
			ok=true;
			if(me.text().length()>=maxLength)
			{
				mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
				ok=false;
			}
		}
		
		mob.recoverCharStats();
		mob.recoverEnvStats();
		mob.tell("\n\rNow don't forget to equip him with non-generic items before saving! If you DO add items to his list, be sure to come back here in case you've exceeded the string limit again.\n\r");
	}
	
	public static void modifyGenShopkeeper(MOB mob, GenShopkeeper me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		while(!ok)
		{
			genName(mob,me);
			genDisplayText(mob,me);
			genDescription(mob,me);
			int oldLevel=me.baseEnvStats().level();
			genLevel(mob,me);
			if((oldLevel==0)&&(me.baseEnvStats().level()>0))
				me.baseCharStats().getMyClass().buildMOB(me,me.baseEnvStats().level(),0,150,0,'M');
			genGender(mob,me);
			genSpeed(mob,me);
			if((oldLevel==0)&&(me.baseEnvStats().level()>0))
				me.baseEnvStats().setDamage((int)Math.round(Util.div(me.baseEnvStats().damage(),me.baseEnvStats().speed())));
			genAttack(mob,me);
			genDamage(mob,me);
			genArmor(mob,me);
			genHitPoints(mob,me);
			genAlignment(mob,me);
			genMoney(mob,me);
			genWeight(mob,me);
			genAbilities(mob,me);
			genBehaviors(mob,me);
			genShopkeeper(mob,me);
			genDisposition(mob,me);
			genSensesMask(mob,me);
			ok=true;
			if(me.text().length()>=maxLength)
			{
				mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
				ok=false;
			}
		}
		
		mob.recoverCharStats();
		mob.recoverEnvStats();
		mob.tell("\n\rNow don't forget to equip him with non-generic items before saving! If you DO add items to his list, be sure to come back here in case you've exceeded the string limit again.\n\r");
	}
}
