package com.planet_ink.coffee_mud.Commands.base.sysop;


import com.planet_ink.coffee_mud.Commands.base.Scoring;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;
import java.io.*;

public class Generic
{
	public final long maxLength=65535;
	public Lister myLister=new Lister();

	void genName(MOB mob, Environmental E)
		throws IOException
	{
		mob.tell("\n\rName: '"+E.name()+"'.");
		String newName=mob.session().prompt("Enter a new one\n\r:","");
		if(newName.length()>0)
			E.setName(newName);
		else
			mob.tell("(no change)");
	}

	Room genRoomType(MOB mob, Room R)
		throws IOException
	{
		mob.tell("\n\rType: '"+CMClass.className(R)+"'");
		String newName=mob.session().prompt("Enter a new one\n\r:","");
		if(newName.length()>0)
		{
			Room newRoom=CMClass.getLocale(newName);
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
					newRoom.rawExits()[d]=R.rawExits()[d];
					newRoom.rawDoors()[d]=R.rawDoors()[d];
				}
				for(int x=0;x<R.numInhabitants();x++)
				{
					MOB inhab=R.fetchInhabitant(x);
					if(inhab!=null)
					{
						newRoom.addInhabitant(inhab);
						inhab.setLocation(newRoom);
					}
				}
				for(int x=0;x<R.numItems();x++)
				{
					Item I=R.fetchItem(x);
					if(I!=null)
						newRoom.addItem(I);
				}
				for(Enumeration e=CMMap.MOBs.elements();e.hasMoreElements();)
				{
					MOB mob2=(MOB)e.nextElement();
					if(mob2.getStartRoom()==R)
					{
						mob2.setStartRoom(newRoom);
						ExternalPlay.DBUpdateMOB(mob2);
					}
				}
				newRoom.setID(R.ID());
				newRoom.setArea(R.getArea());
				newRoom.setDisplayText(R.displayText());
				newRoom.setDescription(R.description());
				for(int r=0;r<CMMap.numRooms();r++)
				{
					Room room=CMMap.getRoom(r);
					if(room==R)
					{
						CMMap.setRoomAt(newRoom,r);
						room=newRoom;
					}
					for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
					{
						if(room.rawDoors()[d]==R)
							room.rawDoors()[d]=newRoom;
					}
					for(int i=0;i<room.numInhabitants();i++)
					{
						MOB mob2=room.fetchInhabitant(i);
						if((mob2!=null)&&(mob2.getStartRoom()==room))
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

	void genDescription(MOB mob, Environmental E)
		throws IOException
	{
		mob.tell("\n\rDescription: '"+E.description()+"'.");
		String newName=mob.session().prompt("Enter a new one\n\r:","");
		if(newName.length()>0)
			E.setDescription(newName);
		else
			mob.tell("(no change)");
	}

	void genDisplayText(MOB mob, Environmental E)
		throws IOException
	{
		mob.tell("\n\rDisplay: '"+E.displayText()+"'.");
		String newName=mob.session().prompt("Enter something new (null == blended)\n\r:","");
		if(newName.length()>0)
		{
			if(newName.trim().equalsIgnoreCase("null"))
				newName="";
			E.setDisplayText(newName);
		}
		else
			mob.tell("(no change)");
		if(E.displayText().length()==0)
			mob.tell("(blended)");
	}
	void genClosedText(MOB mob, Exit E)
		throws IOException
	{
		mob.tell("\n\rClosed Text: '"+E.closedText()+"'.");
		String newName=mob.session().prompt("Enter something new\n\r:","");
		if(newName.length()>0)
			E.setExitParams(E.doorName(),E.closeWord(),E.openWord(),newName);
		else
			mob.tell("(no change)");
	}
	void genDoorName(MOB mob, Exit E)
		throws IOException
	{
		mob.tell("\n\rDoor Name: '"+E.doorName()+"'.");
		String newName=mob.session().prompt("Enter something new\n\r:","");
		if(newName.length()>0)
			E.setExitParams(newName,E.closeWord(),E.openWord(),E.closedText());
		else
			mob.tell("(no change)");
	}

	void genOpenWord(MOB mob, Exit E)
		throws IOException
	{
		mob.tell("\n\rOpen Word: '"+E.openWord()+"'.");
		String newName=mob.session().prompt("Enter something new\n\r:","");
		if(newName.length()>0)
			E.setExitParams(E.doorName(),E.closeWord(),newName,E.closedText());
		else
			mob.tell("(no change)");
	}

	void genSubOps(MOB mob, Area A)
		throws IOException
	{
		String newName="Q";
		while(newName.length()>0)
		{
			mob.tell("\n\rArea SubOperator user names: "+A.getSubOpList());
			newName=mob.session().prompt("Enter a name to add or remove\n\r:","");
			if(newName.length()>0)
			{
				if(A.amISubOp(newName))
				{
					A.delSubOp(newName);
					mob.tell("SubOperator removed.");
				}
				else
				if(ExternalPlay.DBUserSearch(null,newName))
				{
					A.addSubOp(newName);
					mob.tell("SubOperator added.");
				}
				else
					mob.tell("'"+newName+"' is not recognized as a valid user name.");
			}
		}
	}

	void genCloseWord(MOB mob, Exit E)
		throws IOException
	{
		mob.tell("\n\rClose Word: '"+E.closeWord()+"'.");
		String newName=mob.session().prompt("Enter something new\n\r:","");
		if(newName.length()>0)
			E.setExitParams(E.doorName(),newName,E.openWord(),E.closedText());
		else
			mob.tell("(no change)");
	}
	void genExitMisc(MOB mob, Exit E)
		throws IOException
	{
		if(E.hasALock())
		{
			E.setReadable(false);
			E.setClassRestricted(false);
			E.setAlignmentRestricted(false);
			mob.tell("\n\rAssigned Key Item: '"+E.keyName()+"'.");
			String newName=mob.session().prompt("Enter something new\n\r:","");
			if(newName.length()>0)
				E.setKeyName(newName);
			else
				mob.tell("(no change)");
		}
		else
		if(E.hasADoor())
		{
			if(genGenericPrompt(mob,"Is this door readable",E.isReadable()))
			{
				E.setReadable(true);
				mob.tell("\n\rText: '"+E.readableText()+"'.");
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
				if(!genGenericPrompt(mob,"Restricted to by class?",E.classRestricted()))
				{
					E.setClassRestricted(false);
					ok=true;
					if(!genGenericPrompt(mob,"Restricted to by alignment?",E.alignmentRestricted()))
					{
						E.setAlignmentRestricted(false);
						ok=true;
					}
					else
					{
						E.setAlignmentRestricted(true);
						mob.tell("\n\rAlignments to mask out: '"+E.alignmentRestrictedMask()+"'.");
						String newName=mob.session().prompt("Enter new list (good, evil, neutral)\n\r:","");
						if(newName.length()>0)
							E.setAlignmentRestrictedMask(newName);
						else
							mob.tell("(no change)");
					}
				}
				else
				{
					E.setClassRestricted(true);
					E.setAlignmentRestricted(false);
					mob.tell("\n\rClass name: '"+E.classRestrictedName()+"'.");
					String newName=mob.session().prompt("Enter something new\n\r:","");

					for(int c=0;c<CMClass.charClasses.size();c++)
					{
						CharClass C=(CharClass)CMClass.charClasses.elementAt(c);
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
	void genReadable(MOB mob, Item E)
		throws IOException
	{
		if((E instanceof Wand)
		 ||(E instanceof Scroll)
		 ||(E instanceof Pill)
		 ||(E instanceof Potion)
		 ||(E instanceof Light)
		 ||(E instanceof Container)
		 ||(E instanceof Key))
			E.setReadable(false);
		else
		if((CMClass.className(E).endsWith("Readable"))
		 ||(E instanceof com.planet_ink.coffee_mud.interfaces.Map))
			E.setReadable(true);
		else
			E.setReadable(genGenericPrompt(mob,"Is this item readable",E.isReadable()));
		if((E.isReadable())
		 ||(E instanceof Wand)
		 ||(E instanceof Scroll)
		 ||(E instanceof Pill)
		 ||(E instanceof Potion)
		 ||(E instanceof Light)
		 ||(E instanceof Key))
		{
			boolean ok=false;
			while(!ok)
			{
				if(CMClass.className(E).endsWith("SuperPill"))
				{
					mob.tell("\n\rAssigned Spell or Parameters: '"+E.readableText()+"'.");
					ok=true;
				}
				else
				if((E instanceof Scroll)
				||(E instanceof Pill)
				||(E instanceof Potion))
					mob.tell("\n\rAssigned Spell(s) ( ';' delimited)\n: '"+E.readableText()+"'.");
				else
				if(E instanceof Wand)
					mob.tell("\n\rAssigned Spell Name: '"+E.readableText()+"'.");
				else
				if(E instanceof Key)
				{
					mob.tell("\n\rAssigned Key Code: '"+E.readableText()+"'.");
					ok=true;
				}
				else
				if(E instanceof com.planet_ink.coffee_mud.interfaces.Map)
				{
					mob.tell("\n\rAssigned Map Area(s): '"+E.readableText()+"'.");
					ok=true;
				}
				else
				if(E instanceof Light)
				{
					mob.tell("\n\rLight duration (before burn out): '"+Util.s_int(E.readableText())+"'.");
					ok=true;
				}
				else
				{
					mob.tell("\n\rAssigned Read Text: '"+E.readableText()+"'.");
					ok=true;
				}
				String newName=mob.session().prompt("Enter something new\n\r:","");
				if((E instanceof Wand)
				||(E instanceof Scroll)
				||((E instanceof Pill)&&(!(CMClass.className(E).endsWith("SuperPill"))))
				||(E instanceof Potion))
				{
					if(newName.length()==0)
						ok=true;
					else
					{
						if(newName.equalsIgnoreCase("LIST"))
							mob.tell(myLister.reallyList(CMClass.abilities,-1).toString());
						else
						if(E instanceof Wand)
						{
							Ability chosenOne=chosenOne=(Ability)CMClass.getAbility(newName);
							if(chosenOne!=null)
								ok=true;
							else
								mob.tell("'"+newName+"' is not recognized.  Try 'LIST'.");
						}
						else
						if((E instanceof Scroll)
						||(E instanceof Pill)
						||(E instanceof Potion))
						{
							String oldName=newName;
							int x=newName.indexOf(";");
							while(x>=0)
							{
								String spellName=newName.substring(0,x).trim();
								Ability chosenOne=chosenOne=(Ability)CMClass.getAbility(spellName);
								if(chosenOne!=null)
									ok=true;
								else
								{
									mob.tell("'"+spellName+"' is not recognized.  Try 'LIST'.");
									break;
								}
								newName=newName.substring(x+1).trim();
								x=newName.indexOf(";");
							}
							newName=oldName;
						}
					}
				}

				if(ok)
				{
					if(newName.length()>0)
						E.setReadableText(newName);
					else
						mob.tell("(no change)");
				}
			}
		}
	}
	void genGettable(MOB mob, Item E)
		throws IOException
	{
		if(E instanceof Potion)
			((Potion)E).setDrunk((Potion)E,false);

		String c="Q";
		while(!c.equals("\n"))
		{
			mob.session().println("\n\r1) Is Gettable   : "+E.isGettable());
			mob.session().println("2) Is Droppable  : "+E.isDroppable());
			mob.session().println("3) Is Removable  : "+E.isRemovable());
			mob.session().println("4) Is Trapped    : "+E.isTrapped());
			if(E instanceof Weapon)
				mob.session().println("5) Is Two-Handed : "+E.rawLogicalAnd());
			c=mob.session().choose("Enter one to change, or ENTER when done: ","12345\n","\n").toUpperCase();
			switch(c.charAt(0))
			{
			case '1': E.setGettable(!E.isGettable()); break;
			case '2': E.setDroppable(!E.isDroppable()); break;
			case '3': E.setRemovable(!E.isRemovable()); break;
			case '4': E.setTrapped(!E.isTrapped()); break;
			case '5': if(E instanceof Weapon) 
						  E.setRawLogicalAnd(!E.rawLogicalAnd()); 
					  break;
			}
		}
		((Trap)CMClass.getAbility("Trap_Trap")).setTrapped(E,E.isTrapped());
	}

	void genLevelRestrict(MOB mob, Exit E)
		throws IOException
	{
		if(genGenericPrompt(mob,"Restricted to level "+E.baseEnvStats().level()+" and above?",E.levelRestricted()))
			E.setLevelRestricted(true);
		else
			E.setLevelRestricted(false);
	}

	void toggleDispositionMask(Environmental E, int mask)
	{
		int current=E.baseEnvStats().disposition();
		if((current&mask)==0)
			E.baseEnvStats().setDisposition(current|mask);
		else
			E.baseEnvStats().setDisposition(current&((int)(EnvStats.ALLMASK-mask)));
	}

	void genDisposition(MOB mob, Environmental E)
		throws IOException
	{
		String c="Q";
		while(!c.equals("\n"))
		{
			mob.session().println("\n\r1) Is Invisible   : "+((E.baseEnvStats().disposition()&EnvStats.IS_INVISIBLE)!=0));
			mob.session().println("2) Is Hidden      : "+((E.baseEnvStats().disposition()&EnvStats.IS_HIDDEN)!=0));
			mob.session().println("3) Is Unseeable   : "+((E.baseEnvStats().disposition()&EnvStats.IS_SEEN)!=0));
			mob.session().println("4) Is Magical     : "+((E.baseEnvStats().disposition()&EnvStats.IS_BONUS)!=0));
			mob.session().println("5) Is Glowing     : "+((E.baseEnvStats().disposition()&EnvStats.IS_LIGHT)!=0));
			if(E instanceof MOB)
			{
			mob.session().println("6) Is Flying      : "+((E.baseEnvStats().disposition()&EnvStats.IS_FLYING)!=0));
			mob.session().println("7) Is Climbing    : "+((E.baseEnvStats().disposition()&EnvStats.IS_CLIMBING)!=0));
			mob.session().println("8) Is Sneaking    : "+((E.baseEnvStats().disposition()&EnvStats.IS_SNEAKING)!=0));
			}
			else
			{
				if(E instanceof Exit)
				{
				mob.session().println("6) Requires Flight: "+((E.baseEnvStats().disposition()&EnvStats.IS_FLYING)!=0));
				mob.session().println("7) Requires Climb : "+((E.baseEnvStats().disposition()&EnvStats.IS_CLIMBING)!=0));
				}
				mob.session().println("E) Is Evil        : "+((E.baseEnvStats().disposition()&EnvStats.IS_EVIL)!=0));
				mob.session().println("G) Is Good        : "+((E.baseEnvStats().disposition()&EnvStats.IS_GOOD)!=0));
			}
			c=mob.session().choose("Enter one to change, or ENTER when done: ","12345678GE\n","\n").toUpperCase();
			switch(c.charAt(0))
			{
			case '1': toggleDispositionMask(E,EnvStats.IS_INVISIBLE); break;
			case '2': toggleDispositionMask(E,EnvStats.IS_HIDDEN); break;
			case '3': toggleDispositionMask(E,EnvStats.IS_SEEN); break;
			case '4': toggleDispositionMask(E,EnvStats.IS_BONUS); break;
			case '5': toggleDispositionMask(E,EnvStats.IS_LIGHT); break;
			case '6': toggleDispositionMask(E,EnvStats.IS_FLYING); break;
			case '7': toggleDispositionMask(E,EnvStats.IS_CLIMBING); break;
			case '8': toggleDispositionMask(E,EnvStats.IS_SNEAKING); break;
			case 'G': toggleDispositionMask(E,EnvStats.IS_GOOD); break;
			case 'E': toggleDispositionMask(E,EnvStats.IS_EVIL); break;
			}
		}
	}

	boolean genGenericPrompt(MOB mob, String prompt, boolean val)
	{
		try
		{
			prompt=Util.padRight(prompt,35);
			if(val)
				prompt+="(Y/n): ";
			else
				prompt+="(y/N): ";
			return mob.session().confirm(prompt,val?"Y":"N");
		}
		catch(IOException e)
		{
			return val;
		}
	}

	void toggleSensesMask(Environmental E, int mask)
	{
		int current=E.baseEnvStats().sensesMask();
		if((current&mask)==0)
			E.baseEnvStats().setSensesMask(current|mask);
		else
			E.baseEnvStats().setSensesMask(current&((int)(EnvStats.ALLMASK-mask)));
	}
	void toggleClimateMask(Area A, int mask)
	{
		int current=A.climateType();
		if((current&mask)==0)
			A.setClimateType(current|mask);
		else
			A.setClimateType(current&((int)(EnvStats.ALLMASK-mask)));
	}



	void genClimateType(MOB mob, Area A)
		throws IOException
	{
		String c="Q";
		while(!c.equals("\n"))
		{
			mob.session().println(" ");
			mob.session().println("R) Wet and Rainy    : "+((A.climateType()&Area.CLIMASK_WET)>0));
			mob.session().println("H) Excessively hot  : "+((A.climateType()&Area.CLIMASK_HOT)>0));
			mob.session().println("C) Excessively cold : "+((A.climateType()&Area.CLIMASK_COLD)>0));
			mob.session().println("W) Very windy       : "+((A.climateType()&Area.CLIMATE_WINDY)>0));
			mob.session().println("D) Very dry         : "+((A.climateType()&Area.CLIMASK_DRY)>0));
			c=mob.session().choose("Enter one to change, or ENTER when done: ","RHCWD\n","\n").toUpperCase();
			switch(c.charAt(0))
			{
			case 'C': toggleClimateMask(A,Area.CLIMASK_COLD); break;
			case 'H': toggleClimateMask(A,Area.CLIMASK_HOT); break;
			case 'R': toggleClimateMask(A,Area.CLIMASK_WET); break;
			case 'W': toggleClimateMask(A,Area.CLIMATE_WINDY); break;
			case 'D': toggleClimateMask(A,Area.CLIMASK_DRY); break;
			}
		}
	}

	void genSensesMask(MOB mob, Environmental E)
		throws IOException
	{
		String c="Q";
		while(!c.equals("\n"))
		{
			mob.session().println("\n\r1) Can see in the dark: "+((E.baseEnvStats().sensesMask()&EnvStats.CAN_SEE_DARK)!=0));
			mob.session().println("2) Can see hidden     : "+((E.baseEnvStats().sensesMask()&EnvStats.CAN_SEE_HIDDEN)!=0));
			mob.session().println("3) Can see invisible  : "+((E.baseEnvStats().sensesMask()&EnvStats.CAN_SEE_INVISIBLE)!=0));
			mob.session().println("4) Can see sneakers   : "+((E.baseEnvStats().sensesMask()&EnvStats.CAN_SEE_SNEAKERS)!=0));
			mob.session().println("5) Has infravision    : "+((E.baseEnvStats().sensesMask()&EnvStats.CAN_SEE_INFRARED)!=0));
			mob.session().println("6) Can see goodness   : "+((E.baseEnvStats().sensesMask()&EnvStats.CAN_SEE_GOOD)!=0));
			mob.session().println("7) Can see evilness   : "+((E.baseEnvStats().sensesMask()&EnvStats.CAN_SEE_EVIL)!=0));
			mob.session().println("8) Can see magicness  : "+((E.baseEnvStats().sensesMask()&EnvStats.CAN_SEE_BONUS)!=0));
			mob.session().println("9) Is Mute            : "+((E.baseEnvStats().sensesMask()&EnvStats.CAN_SPEAK)!=0));
			mob.session().println("A) Is Deaf            : "+((E.baseEnvStats().sensesMask()&EnvStats.CAN_HEAR)!=0));
			mob.session().println("B) Is Blind           : "+((E.baseEnvStats().sensesMask()&EnvStats.CAN_SEE)!=0));
			c=mob.session().choose("Enter one to change, or ENTER when done: ","123456789AB\n","\n").toUpperCase();
			switch(c.charAt(0))
			{
			case '1': toggleSensesMask(E,EnvStats.CAN_SEE_DARK); break;
			case '2': toggleSensesMask(E,EnvStats.CAN_SEE_HIDDEN); break;
			case '3': toggleSensesMask(E,EnvStats.CAN_SEE_INVISIBLE); break;
			case '4': toggleSensesMask(E,EnvStats.CAN_SEE_SNEAKERS); break;
			case '5': toggleSensesMask(E,EnvStats.CAN_SEE_INFRARED); break;
			case '6': toggleSensesMask(E,EnvStats.CAN_SEE_GOOD); break;
			case '7': toggleSensesMask(E,EnvStats.CAN_SEE_EVIL); break;
			case '8': toggleSensesMask(E,EnvStats.CAN_SEE_BONUS); break;
			case '9': toggleSensesMask(E,EnvStats.CAN_SPEAK); break;
			case 'A': toggleSensesMask(E,EnvStats.CAN_HEAR); break;
			case 'B': toggleSensesMask(E,EnvStats.CAN_SEE); break;
			}
		}
	}

	void genDoorsNLocks(MOB mob, Exit E)
		throws IOException
	{
		boolean HasDoor=E.hasADoor();
		boolean Open=E.isOpen();
		boolean DefaultsClosed=E.defaultsClosed();
		boolean HasLock=E.hasALock();
		boolean Locked=E.isLocked();
		boolean DefaultsLocked=E.defaultsLocked();
		if(genGenericPrompt(mob,"Has a door",E.hasADoor()))
		{
			HasDoor=true;
			Open=false;
			DefaultsClosed=genGenericPrompt(mob,"Defaults closed",E.defaultsClosed());
			if(genGenericPrompt(mob,"Has a lock",E.hasALock()))
			{
				HasLock=true;
				Locked=true;
				DefaultsLocked=genGenericPrompt(mob,"Defaults locked",E.defaultsLocked());
			}
			else
			{
				HasLock=false;
				Locked=false;
				DefaultsLocked=false;
			}
			mob.tell("\n\rReset Delay (# ticks): '"+E.openDelayTicks()+"'.");
			int newLevel=Util.s_int(mob.session().prompt("Enter a new delay\n\r:",""));
			if(newLevel>0)
				E.setOpenDelayTicks(newLevel);
			else
				mob.tell("(no change)");
		}
		else
		{
			HasDoor=false;
			Open=true;
			DefaultsClosed=false;
			HasLock=false;
			Locked=false;
			DefaultsLocked=false;
		}
		E.setDoorsNLocks(HasDoor,Open,DefaultsClosed,HasLock,Locked,DefaultsLocked);
		E.setTrapped(genGenericPrompt(mob,"\n\r\n\rIs this exit trapped",E.isTrapped()));
		((Trap)CMClass.getAbility("Trap_Trap")).setTrapped(E,E.isTrapped());
	}

	void genLidsNLocks(MOB mob, Container E)
		throws IOException
	{
		if(genGenericPrompt(mob,"Has a lid ",E.hasALid()))
		{
			E.setLidsNLocks(true,false,E.hasALock(),E.isLocked());
			if(genGenericPrompt(mob,"Has a lock",E.hasALock()))
			{
				E.setLidsNLocks(E.hasALid(),E.isOpen(),true,true);
				mob.tell("\n\rText: '"+E.keyName()+"'.");
				String newName=mob.session().prompt("Enter something new\n\r:","");
				if(newName.length()>0)
					E.setKeyName(newName);
				else
					mob.tell("(no change)");
			}
			else
			{
				E.setKeyName("");
				E.setLidsNLocks(E.hasALid(),E.isOpen(),false,false);
			}
		}
		else
		{
			E.setKeyName("");
			E.setLidsNLocks(false,true,false,false);
		}
		E.setTrapped(genGenericPrompt(mob,"\n\r\n\rIs this container trapped",E.isTrapped()));
		((Trap)CMClass.getAbility("Trap_Trap")).setTrapped(E,E.isTrapped());
	}

	void genLevel(MOB mob, Environmental E)
		throws IOException
	{
		if(E.baseEnvStats().level()<0)
			E.baseEnvStats().setLevel(1);
		mob.tell("\n\rLevel: '"+E.baseEnvStats().level()+"'.");
		int newLevel=Util.s_int(mob.session().prompt("Enter a new one\n\r:",""));
		if(newLevel>0)
			E.baseEnvStats().setLevel(newLevel);
		else
			mob.tell("(no change)");
	}

	void genRejuv(MOB mob, Environmental E)
		throws IOException
	{
		mob.tell("\n\rRejuv Ticks: '"+E.baseEnvStats().rejuv()+"'.");
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

	void genUses(MOB mob, Item E)
		throws IOException
	{
		mob.tell("\n\rUses Remaining: '"+E.usesRemaining()+"'.");
		int newLevel=Util.s_int(mob.session().prompt("Enter a new value\n\r:",""));
		if(newLevel>0)
			E.setUsesRemaining(newLevel);
		else
			mob.tell("(no change)");
	}

	void genCondition(MOB mob, Item E)
		throws IOException
	{
		mob.tell("\n\rCondition: '"+E.usesRemaining()+"'.");
		int newLevel=Util.s_int(mob.session().prompt("Enter a new value\n\r:",""));
		if(newLevel>0)
			E.setUsesRemaining(newLevel);
		else
			mob.tell("(no change)");
	}

	void genMiscSet(MOB mob, Environmental E)
		throws IOException
	{
		if(!E.isGeneric())
			return;

		if(E instanceof ShopKeeper)
			modifyGenShopkeeper(mob,(ShopKeeper)E);
		else
		if(E instanceof MOB)
			modifyGenMOB(mob,(MOB)E);
		else
		if(E instanceof Exit)
			modifyGenExit(mob,(Exit)E);
		else
		if(E instanceof com.planet_ink.coffee_mud.interfaces.Map)
			modifyGenMap(mob,(com.planet_ink.coffee_mud.interfaces.Map)E);
		else
		if(E instanceof Armor)
			modifyGenArmor(mob,(Armor)E);
		else
		if(E instanceof Food)
			modifyGenFood(mob,(Food)E);
		else
		if((E instanceof Drink)&&(E instanceof Item))
			modifyGenDrink(mob,(Drink)E);
		else
		if(E instanceof Weapon)
			modifyGenWeapon(mob,(Weapon)E);
		else
		if(E instanceof Container)
			modifyGenContainer(mob,(Container)E);
		else
		if(E instanceof Item)
		{
			if(E.ID().equals("GenWallpaper"))
				modifyGenWallpaper(mob,(Item)E);
			else
				modifyGenItem(mob,(Item)E);
		}
	}

	void genMiscText(MOB mob, Environmental E)
		throws IOException
	{
		if(E.isGeneric())
			genMiscSet(mob,E);
		else
		{
			mob.tell("\n\rMisc Text: '"+E.text()+"'.");
			String newText=mob.session().prompt("Re-enter now ('null'=='')\n\r:","");
			if(newText.equalsIgnoreCase("NULL"))
				E.setMiscText("");
			else
			if(newText.length()>0)
				E.setMiscText(newText);
			else
				mob.tell("(no change)");
		}

	}

	void genAbility(MOB mob, Environmental E)
		throws IOException
	{
		mob.tell("\n\rMagical Ability: '"+E.baseEnvStats().ability()+"'.");
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

	void genHitPoints(MOB mob, Environmental E)
		throws IOException
	{
		if(E.baseEnvStats().ability()<1) E.baseEnvStats().setAbility(11);
		mob.tell("\n\rHit Points/Level Modifier (hp=((10*level) + (random*level*THIS))) : '"+E.baseEnvStats().ability()+"'.");
		String newLevelStr=mob.session().prompt("Enter a new value\n\r:","");
		int newLevel=Util.s_int(newLevelStr);
		if(newLevel>0)
			E.baseEnvStats().setAbility(newLevel);
		else
			mob.tell("(no change)");
	}

	void genValue(MOB mob, Item E)
		throws IOException
	{
		mob.tell("\n\rAdjusted Value: '"+E.value()+"'.");
		int newValue=Util.s_int(mob.session().prompt("Enter a new value\n\r:",""));
		if(newValue>0)
			E.setBaseValue(newValue);
		else
			mob.tell("(no change)");
	}

	void genWeight(MOB mob, Environmental E)
		throws IOException
	{
		mob.tell("\n\rWeight: '"+E.baseEnvStats().weight()+"'.");
		int newValue=Util.s_int(mob.session().prompt("Enter a new weight\n\r:",""));
		if(newValue>0)
			E.baseEnvStats().setWeight(newValue);
		else
			mob.tell("(no change)");
	}


	void genHeight(MOB mob, Environmental E)
		throws IOException
	{
		mob.tell("\n\rHeight: '"+E.baseEnvStats().height()+"'.");
		int newValue=Util.s_int(mob.session().prompt("Enter a new height\n\r:",""));
		if(newValue>0)
			E.baseEnvStats().setHeight(newValue);
		else
			mob.tell("(no change)");
	}


	void genSize(MOB mob, Armor E)
		throws IOException
	{
		mob.tell("\n\rSize: '"+E.baseEnvStats().height()+"'.");
		int newValue=Util.s_int(mob.session().prompt("Enter a new size\n\r:",""));
		if(newValue>0)
			E.baseEnvStats().setHeight(newValue);
		else
			mob.tell("(no change)");
	}


	void genCapacity(MOB mob, Item E)
		throws IOException
	{
		mob.tell("\n\rCapacity: '"+E.capacity()+"'.");
		int newValue=Util.s_int(mob.session().prompt("Enter a new capacity\n\r:",""));
		if(newValue>0)
			E.setCapacity(newValue);
		else
			mob.tell("(no change)");

	}

	void genAttack(MOB mob, Environmental E)
		throws IOException
	{
		mob.tell("\n\rAttackAdjustment: '"+E.envStats().attackAdjustment()+"'.");
		int newValue=Util.s_int(mob.session().prompt("Enter a new value\n\r:",""));
		if(newValue>0)
			E.baseEnvStats().setAttackAdjustment(newValue);
		else
			mob.tell("(no change)");
	}

	void genDamage(MOB mob, Environmental E)
		throws IOException
	{
		mob.tell("\n\rDamage/Hit: '"+E.baseEnvStats().damage()+"'.");
		int newValue=Util.s_int(mob.session().prompt("Enter a new value\n\r:",""));
		if(newValue>0)
			E.baseEnvStats().setDamage(newValue);
		else
			mob.tell("(no change)");
	}

	void genSpeed(MOB mob, Environmental E)
		throws IOException
	{
		mob.tell("\n\rAttacks/Round: '"+((int)Math.round(E.baseEnvStats().speed()))+"'.");
		int newValue=Util.s_int(mob.session().prompt("Enter a new value\n\r:",""));
		if(newValue>0)
			E.baseEnvStats().setSpeed(newValue);
		else
			mob.tell("(no change)");
	}

	void genArmor(MOB mob, Environmental E)
		throws IOException
	{
		if(E instanceof MOB)
			mob.tell("\n\rArmor (lower-better): '"+E.baseEnvStats().armor()+"'.");
		else
			mob.tell("\n\rArmor (higher-better): '"+E.baseEnvStats().armor()+"'.");
		int newValue=Util.s_int(mob.session().prompt("Enter a new value\n\r:",""));
		if(newValue>0)
			E.baseEnvStats().setArmor(newValue);
		else
			mob.tell("(no change)");
	}

	void genMoney(MOB mob, MOB E)
		throws IOException
	{
		mob.tell("\n\rMoney: '"+E.getMoney()+"'.");
		int newValue=Util.s_int(mob.session().prompt("Enter a new value\n\r:",""));
		if(newValue>0)
			E.setMoney(newValue);
		else
			mob.tell("(no change)");
	}

	void genWeaponAmmo(MOB mob, Weapon E)
		throws IOException
	{
		String defaultAmmo=(E.requiresAmmunition())?"Y":"N";
		
		if(mob.session().confirm("Does this weapon require ammunition (default="+defaultAmmo+") (Y/N)?",defaultAmmo))
		{
			mob.tell("\n\rAmmo type: '"+E.ammunitionType()+"'.");
			String newName=mob.session().prompt("Enter a new one\n\r:","");
			if(newName.length()>0)
			{
				E.setAmmunitionType(newName);
				mob.tell("(Remember to create a readable GenItem with '"+E.ammunitionType()+"' in the secret identity, and the uses remaining above 0!");
			}
			else
				mob.tell("(no change)");
			mob.tell("\n\rAmmo capacity: '"+E.ammunitionCapacity()+"'.");
			int newValue=Util.s_int(mob.session().prompt("Enter a new value\n\r:",""));
			if(newValue>0)
				E.setAmmoCapacity(newValue);
			else
				mob.tell("(no change)");
			E.setAmmoRemaining(E.ammunitionCapacity());
		}
		else
		{
			E.setAmmunitionType("");
			E.setAmmoCapacity(0);
		}
	}
	void genWeaponRanges(MOB mob, Weapon E)
		throws IOException
	{
		mob.tell("\n\rMinimum/Maximum Ranges: "+((int)Math.round(E.minRange()))+"/"+((int)Math.round(E.maxRange()))+".");
		String newMinStr=mob.session().prompt("Enter a new minimum range\n\r:","");
		String newMaxStr=mob.session().prompt("Enter a new maximum range\n\r:","");
		if((newMinStr.length()==0)&&(newMaxStr.length()==0))
			mob.tell("(no change)");
		else
		{
			E.setRanges(Util.s_int(newMinStr),Util.s_int(newMaxStr));
			if((E.minRange()>E.maxRange())||(E.minRange()<0)||(E.maxRange()<0))
			{
				mob.tell("(defective entries.  resetting.)");
				E.setRanges(0,0);
			}
		}
	}
	
	void genWeaponType(MOB mob, Weapon E)
		throws IOException
	{
		mob.tell("\n\rWeapon Attack Type: '"+Weapon.typeDescription[E.weaponType()]+"'.");
		boolean q=false;
		String sel="NSPBFMR";
		while(!q)
		{
			String newType=mob.session().choose("Enter a new value (?)\n\r:",sel+"?","");
			if(newType.equals("?"))
			{
				for(int i=0;i<sel.length();i++)
					mob.tell(sel.charAt(i)+") "+Weapon.typeDescription[i]);
				q=false;
			}
			else
			{
				q=true;
				int newValue=-1;
				if(newType.length()>0)
					newValue=sel.indexOf(newType.toUpperCase());
				if(newValue>=0)
					E.setWeaponType(newValue);
				else
					mob.tell("(no change)");
			}
		}
	}

	void genMaterialCode(MOB mob, Item E)
		throws IOException
	{
		mob.tell("\n\rMaterial Type: '"+EnvResource.RESOURCE_DESCS[E.material()&EnvResource.RESOURCE_MASK]+"'.");
		boolean q=false;
		while(!q)
		{
			String newType=mob.session().prompt("Enter a new material (?)\n\r:",EnvResource.RESOURCE_DESCS[E.material()&EnvResource.RESOURCE_MASK]);
			if(newType.equals("?"))
			{
				StringBuffer say=new StringBuffer("");
				for(int i=0;i<EnvResource.RESOURCE_DESCS.length-1;i++)
					say.append(EnvResource.RESOURCE_DESCS[i]+", ");
				mob.tell(say.toString().substring(0,say.length()-2));
				q=false;
			}
			else
			{
				q=true;
				int newValue=-1;
				for(int i=0;i<EnvResource.RESOURCE_DESCS.length-1;i++)
					if(newType.equalsIgnoreCase(EnvResource.RESOURCE_DESCS[i]))
						newValue=EnvResource.RESOURCE_DATA[i][0];
				if(newValue>=0)
					E.setMaterial(newValue);
				else
					mob.tell("(no change)");
			}
		}
	}

	void genAlignment(MOB mob, MOB E)
		throws IOException
	{
		mob.tell("\n\rAlignment: '"+CommonStrings.alignmentStr(E.getAlignment())+"'.");
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

	void genGender(MOB mob, MOB E)
		throws IOException
	{
		mob.tell("\n\rGender: '"+Character.toUpperCase((char)E.baseCharStats().getStat(CharStats.GENDER))+"'.");
		String newType=mob.session().choose("Enter a new gender (M/F)\n\r:","MF","");
		int newValue=-1;
		if(newType.length()>0)
			newValue=("MF").indexOf(newType.toUpperCase());
		if(newValue>=0)
			switch(newValue)
			{
			case 0:
				E.baseCharStats().setStat(CharStats.GENDER,(int)'M');
				break;
			case 1:
				E.baseCharStats().setStat(CharStats.GENDER,(int)'F');
				break;
			}
		else
			mob.tell("(no change)");
	}

	void genWeaponClassification(MOB mob, Weapon E)
		throws IOException
	{
		mob.tell("\n\rWeapon Classification: '"+Weapon.classifictionDescription[E.weaponClassification()]+"'.");
		boolean q=false;
		String sel=("ABEFHKPRSDTN");
		while(!q)
		{
			String newType=mob.session().choose("Enter a new value (?)\n\r:",sel+"?","");
			if(newType.equals("?"))
			{
				for(int i=0;i<sel.length();i++)
					mob.tell(sel.charAt(i)+") "+Weapon.classifictionDescription[i]);
				q=false;
			}
			else
			{
				q=true;
				int newValue=-1;
				if(newType.length()>0)
					newValue=sel.indexOf(newType.toUpperCase());
				if(newValue>=0)
					E.setWeaponClassification(newValue);
				else
					mob.tell("(no change)");
			}
		}
	}

	void genSecretIdentity(MOB mob, Item E)
		throws IOException
	{
		mob.tell("\n\rSecret Identity: '"+E.rawSecretIdentity()+"'.");
		String newValue=mob.session().prompt("Enter a new identity\n\r:","");
		if(newValue.length()>0)
			E.setSecretIdentity(newValue);
		else
			mob.tell("(no change)");
	}

	void genNourishment(MOB mob, Food E)
		throws IOException
	{
		mob.tell("\n\rNourishment/Eat: '"+E.nourishment()+"'.");
		int newValue=Util.s_int(mob.session().prompt("Enter a new amount\n\r:",""));
		if(newValue>0)
			E.setNourishment(newValue);
		else
			mob.tell("(no change)");
	}

	void genRace(MOB mob, MOB E)
		throws IOException
	{
		String raceID="begin!";
		while(raceID.length()>0)
		{
			mob.tell("\n\rRace: '"+E.baseCharStats().getMyRace().ID()+"'.");
			raceID=mob.session().prompt("Enter a new race or LIST\n\r:","").trim();
			if(raceID.equalsIgnoreCase("LIST"))
				mob.tell(myLister.reallyList(CMClass.races,-1).toString());
			else
			if(raceID.length()==0)
				mob.tell("(no change)");
			else
			{
				Race R=CMClass.getRace(raceID);
				if(R!=null)
				{
					E.baseCharStats().setMyRace(R);
					E.baseCharStats().getMyRace().startRacing(mob,false);
				}
				else
					mob.tell("Unknown race! Try 'LIST'.");
			}
		}
	}

	void genBehaviors(MOB mob, Environmental E)
		throws IOException
	{
		String behave="NO";
		while(behave.length()>0)
		{
			String behaviorstr="";
			for(int b=0;b<E.numBehaviors();b++)
			{
				Behavior B=E.fetchBehavior(b);
				if(B!=null)
				{
					behaviorstr+=B.ID();
					if(B.getParms().trim().length()>0)
						behaviorstr+="("+B.getParms().trim()+"), ";
					else
						behaviorstr+=", ";
				}
			}
			if(behaviorstr.length()>0)
				behaviorstr=behaviorstr.substring(0,behaviorstr.length()-2);
			mob.tell("\n\rBehaviors: '"+behaviorstr+"'.");
			behave=mob.session().prompt("Enter a behavior to add/remove or LIST\n\r:","");
			if(behave.length()>0)
			{
				if(behave.equalsIgnoreCase("LIST"))
					mob.tell(myLister.reallyList(CMClass.behaviors,-1).toString());
				else
				{
					Behavior chosenOne=null;
					for(int b=0;b<E.numBehaviors();b++)
					{
						Behavior B=E.fetchBehavior(b);
						if((B!=null)&&(B.ID().equalsIgnoreCase(behave)))
							chosenOne=B;
					}
					if(chosenOne!=null)
					{
						mob.tell(chosenOne.ID()+" removed.");
						E.delBehavior(chosenOne);
					}
					else
					{
						chosenOne=(Behavior)CMClass.getBehavior(behave);
						if(chosenOne!=null)
						{
							chosenOne=(Behavior)chosenOne.newInstance();

							boolean alreadyHasIt=false;
							for(int b=0;b<E.numBehaviors();b++)
							{
								Behavior B=E.fetchBehavior(b);
								if((B!=null)&&(B.ID().equals(chosenOne.ID())))
								{
									alreadyHasIt=true;
									chosenOne=B;
								}
							}
							String parms=chosenOne.getParms();
							parms=mob.session().prompt("Enter any behavior parameters\n\r:",parms);
							chosenOne.setParms(parms.trim());
							if(!alreadyHasIt)
							{
								mob.tell(chosenOne.ID()+" added.");
								E.addBehavior(chosenOne);
							}
							else
								mob.tell(chosenOne.ID()+" re-added.");
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

	void genAffects(MOB mob, Environmental E)
		throws IOException
	{
		String behave="NO";
		while(behave.length()>0)
		{
			String affectstr="";
			for(int b=0;b<E.numAffects();b++)
			{
				Ability A=E.fetchAffect(b);
				if((A!=null)&&(!A.isBorrowed(E)))
				{
					affectstr+=A.ID();
					if(A.text().trim().length()>0)
						affectstr+="("+A.text().trim()+"), ";
					else
						affectstr+=", ";
				}

			}
			if(affectstr.length()>0)
				affectstr=affectstr.substring(0,affectstr.length()-2);
			mob.tell("\n\rAffects: '"+affectstr+"'.");
			behave=mob.session().prompt("Enter an affect to add/remove or LIST\n\r:","");
			if(behave.length()>0)
			{
				if(behave.equalsIgnoreCase("LIST"))
					mob.tell(myLister.reallyList(CMClass.abilities,-1).toString());
				else
				{
					Ability chosenOne=null;
					for(int a=0;a<E.numAffects();a++)
					{
						Ability A=E.fetchAffect(a);
						if((A!=null)&&(A.ID().equalsIgnoreCase(behave)))
							chosenOne=A;
					}
					if(chosenOne!=null)
					{
						mob.tell(chosenOne.ID()+" removed.");
						E.delAffect(chosenOne);
					}
					else
					{
						chosenOne=(Ability)CMClass.getAbility(behave);
						if(chosenOne!=null)
						{
							chosenOne=(Ability)chosenOne.newInstance();
							String parms=chosenOne.text();
							parms=mob.session().prompt("Enter any affect parameters (';' delimited!)\n\r:",parms);
							chosenOne.setMiscText(parms.trim());
							mob.tell(chosenOne.ID()+" added.");
							E.addNonUninvokableAffect(chosenOne);
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

	void genRideable(MOB mob, Rideable R)
		throws IOException
	{
		mob.tell("\n\rRideable Type: '"+Rideable.RIDEABLE_DESCS[R.rideBasis()]+"'.");
		boolean q=false;
		String sel="LWACBT";
		while(!q)
		{
			String newType=mob.session().choose("Enter a new value (?)\n\r:",sel+"?","");
			if(newType.equals("?"))
			{
				for(int i=0;i<sel.length();i++)
					mob.tell(sel.charAt(i)+") "+Rideable.RIDEABLE_DESCS[i].toLowerCase());
				q=false;
			}
			else
			{
				q=true;
				int newValue=-1;
				if(newType.length()>0)
					newValue=sel.indexOf(newType.toUpperCase());
				if(newValue>=0)
					R.setRideBasis(newValue);
				else
					mob.tell("(no change)");
			}
		}
		
		mob.tell("\n\rNumber of MOBs held: '"+R.mobCapacity()+"'.");
		String newLevelStr=mob.session().prompt("Enter a new value: ","");
		int newLevel=Util.s_int(newLevelStr);
		if(newLevel>0)
			R.setMobCapacity(newLevel);
		else
			mob.tell("(no change)");
	}
	
	void genShopkeeper(MOB mob, ShopKeeper E)
		throws IOException
	{
		mob.tell("\n\rShopekeeper type: '"+E.storeKeeperString()+"'.");
		String newType=mob.session().choose("Enter a new value (*/G/A/M/W/P/L/O/T/C)\n\r:","*GAMWPLOTC","");
		int newValue=-1;
		if(newType.length()>0)
			newValue=("*GAMWPLOTC").indexOf(newType.toUpperCase());
		if(newValue>=0)
		{
			if(E.whatIsSold()!=newValue)
			{
				Vector V=E.getUniqueStoreInventory();
				for(int b=0;b<V.size();b++)
					E.delStoreInventory((Environmental)V.elementAt(b));
			}
			E.setWhatIsSold(newValue);
		}


		String itemstr="NO";
		while(itemstr.length()>0)
		{
			String inventorystr="";
			Vector V=E.getUniqueStoreInventory();
			for(int b=0;b<V.size();b++)
				inventorystr+=CMClass.className(V.elementAt(b))+" ("+E.numberInStock((Environmental)V.elementAt(b))+"), ";
			if(inventorystr.length()>0)
				inventorystr=inventorystr.substring(0,inventorystr.length()-2);
			mob.tell("\n\rInventory: '"+inventorystr+"'.");
			itemstr=mob.session().prompt("Enter something to add/remove or LIST\n\r:","");
			if(itemstr.length()>0)
			{
				if(itemstr.equalsIgnoreCase("LIST"))
				{
					mob.tell(myLister.reallyList(CMClass.abilities,-1).toString());
					mob.tell(myLister.reallyList(CMClass.armor,-1).toString());
					mob.tell(myLister.reallyList(CMClass.weapons,-1).toString());
					mob.tell(myLister.reallyList(CMClass.miscMagic,-1).toString());
					mob.tell(myLister.reallyList(CMClass.items,-1).toString());
					mob.tell(myLister.reallyList(CMClass.MOBs,-1).toString());
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
						item=CMClass.getUnknown(itemstr);
						if(item!=null)
						{
							boolean ok=E.doISellThis(item);
							if((item instanceof Ability)&&((E.whatIsSold()==E.TRAINER)||(E.whatIsSold()==E.CASTER)))
								ok=true;
							else
							if(E.whatIsSold()==E.ONLYBASEINVENTORY)
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

	void genAbilities(MOB mob, MOB E)
		throws IOException
	{
		String behave="NO";
		while(behave.length()>0)
		{
			String abilitiestr="";
			for(int a=0;a<E.numAbilities();a++)
			{
				Ability A=E.fetchAbility(a);
				if((A!=null)&&(!A.isBorrowed(E)))
					abilitiestr+=A.ID()+", ";
			}
			if(abilitiestr.length()>0)
				abilitiestr=abilitiestr.substring(0,abilitiestr.length()-2);
			mob.tell("\n\rAbilities: '"+abilitiestr+"'.");
			behave=mob.session().prompt("Enter an ability to add/remove or LIST\n\r:","");
			if(behave.length()>0)
			{
				if(behave.equalsIgnoreCase("LIST"))
					mob.tell(myLister.reallyList(CMClass.abilities,-1).toString());
				else
				{
					Ability chosenOne=null;
					for(int a=0;a<E.numAbilities();a++)
					{
						Ability A=E.fetchAbility(a);
						if((A!=null)&&(A.ID().equalsIgnoreCase(behave)))
							chosenOne=A;
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
						chosenOne=(Ability)CMClass.getAbility(behave);
						if(chosenOne!=null)
						{
							boolean alreadyHasIt=false;
							for(int a=0;a<E.numAbilities();a++)
							{
								Ability A=E.fetchAbility(a);
								if((A!=null)&&(A.ID().equals(chosenOne.ID())))
									alreadyHasIt=true;
							}
							if(!alreadyHasIt)
								mob.tell(chosenOne.ID()+" added.");
							else
								mob.tell(chosenOne.ID()+" re-added.");
							if(!alreadyHasIt)
							{
								E.addAbility((Ability)chosenOne.copyOf());
								chosenOne.setProfficiency(100);
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

	void genWornLocation(MOB mob, Item E)
		throws IOException
	{
		int codeVal=-1;
		while(codeVal!=0)
		{
			mob.tell("\n\rWearing parameters\n\r0: Done");
			if(!E.rawLogicalAnd())
				mob.tell("1: Able to worn on any ONE of these locations:");
			else
				mob.tell("1: Must be worn on ALL of these locations:");
			int maxCode=1;
			for(int l=0;l<16;l++)
			{
				int wornCode=1<<l;
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
					int wornCode=1<<(codeVal-2);
					if((E.rawProperLocationBitmap()&wornCode)==wornCode)
						E.setRawProperLocationBitmap(E.rawProperLocationBitmap()-wornCode);
					else
						E.setRawProperLocationBitmap(E.rawProperLocationBitmap()|wornCode);
				}
			}
		}
	}

	void genThirstQuenched(MOB mob, Drink E)
		throws IOException
	{
		mob.tell("\n\rQuenched/Drink: '"+E.thirstQuenched()+"'.");
		int newValue=Util.s_int(mob.session().prompt("Enter a new amount\n\r:",""));
		if(newValue>0)
			E.setThirstQuenched(newValue);
		else
			mob.tell("(no change)");
	}

	void genDrinkHeld(MOB mob, Drink E)
		throws IOException
	{
		mob.tell("\n\rAmount of Drink Held: '"+E.liquidHeld()+"'.");
		int newValue=Util.s_int(mob.session().prompt("Enter a new amount\n\r:",""));
		if(newValue>0)
		{
			E.setLiquidHeld(newValue);
			E.setLiquidRemaining(newValue);
		}
		else
			mob.tell("(no change)");
	}



	public void modifyGenFood(MOB mob, Food me)
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
			genRejuv(mob,me);
			genWeight(mob,me);
			genNourishment(mob,me);
			genDisposition(mob,me);
			genGettable(mob,me);
			genReadable(mob,me);
			genAffects(mob,me);
			ok=true;
			if(me.text().length()>=maxLength)
			{
				mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
				ok=false;
			}
		}
		mob.recoverEnvStats();
	}
	public void modifyGenDrink(MOB mob, Drink me)
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
			genSecretIdentity(mob,(Item)me);
			genValue(mob,(Item)me);
			genWeight(mob,me);
			genRejuv(mob,me);
			genThirstQuenched(mob,me);
			genMaterialCode(mob,(Item)me);
			genDrinkHeld(mob,me);
			genGettable(mob,(Item)me);
			genReadable(mob,(Item)me);
			genAffects(mob,me);
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

	public void modifyGenItem(MOB mob, Item me)
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
			genLevel(mob,me);
			genSecretIdentity(mob,me);
			genMaterialCode(mob,me);
			genGettable(mob,me);
			genReadable(mob,me);
			genRejuv(mob,me);
			genAbility(mob,me);
			genUses(mob,me);
			genValue(mob,me);
			genWeight(mob,me);
			genDisposition(mob,me);
			genBehaviors(mob,me);
			genAffects(mob,me);
			ok=true;
			if(me.text().length()>=maxLength)
			{
				mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
				ok=false;
			}
		}
		mob.recoverEnvStats();
	}

	public void modifyGenWallpaper(MOB mob, Item me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		while(!ok)
		{
			genName(mob,me);
			genDescription(mob,me);
			genReadable(mob,me);
			ok=true;
			if(me.text().length()>=maxLength)
			{
				mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
				ok=false;
			}
		}
		mob.recoverEnvStats();
	}

	public void modifyGenMap(MOB mob, com.planet_ink.coffee_mud.interfaces.Map me)
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
			genLevel(mob,me);
			genSecretIdentity(mob,me);
			genGettable(mob,me);
			genReadable(mob,me);
			genValue(mob,me);
			genWeight(mob,me);
			genRejuv(mob,me);
			genMaterialCode(mob,me);
			genDisposition(mob,me);
			genBehaviors(mob,me);
			genAffects(mob,me);
			ok=true;
			if(me.text().length()>=maxLength)
			{
				mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
				ok=false;
			}
		}
		mob.recoverEnvStats();
	}

	public void modifyGenContainer(MOB mob, Container me)
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
			genLevel(mob,me);
			genRejuv(mob,me);
			genCapacity(mob,me);
			genLidsNLocks(mob,me);
			genMaterialCode(mob,me);
			genSecretIdentity(mob,me);
			genValue(mob,me);
			genUses(mob,me);
			genWeight(mob,me);
			genDisposition(mob,me);
			genGettable(mob,me);
			genReadable(mob,me);
			genBehaviors(mob,me);
			genAffects(mob,me);
			if(me instanceof Rideable)
				genRideable(mob,(Rideable)me);
			ok=true;
			if(me.text().length()>=maxLength)
			{
				mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
				ok=false;
			}
		}
		mob.recoverEnvStats();
	}

	public void modifyGenWeapon(MOB mob, Weapon me)
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
			genLevel(mob,me);
			genAttack(mob,me);
			genDamage(mob,me);
			genMaterialCode(mob,me);
			genWeaponType(mob,me);
			genWeaponClassification(mob,me);
			genWeaponRanges(mob,me);
			if(me instanceof Wand)
				genReadable(mob,me);
			else
				genWeaponAmmo(mob,me);
			genRejuv(mob,me);
			genCondition(mob,me);
			genAbility(mob,me);
			genSecretIdentity(mob,me);
			genGettable(mob,me);
			genValue(mob,me);
			genWeight(mob,me);
			genDisposition(mob,me);
			genBehaviors(mob,me);
			genAffects(mob,me);
			ok=true;
			if(me.text().length()>=maxLength)
			{
				mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
				ok=false;
			}
		}
		mob.recoverEnvStats();
	}
	public void modifyGenArmor(MOB mob, Armor me)
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
			genLevel(mob,me);
			genMaterialCode(mob,me);
			genWornLocation(mob,me);
			genRejuv(mob,me);
			genArmor(mob,me);
			genCondition(mob,me);
			genAbility(mob,me);
			genCapacity(mob,me);
			genSecretIdentity(mob,me);
			genGettable(mob,me);
			genReadable(mob,me);
			genValue(mob,me);
			genWeight(mob,me);
			genSize(mob,me);
			genDisposition(mob,me);
			genBehaviors(mob,me);
			genAffects(mob,me);
			ok=true;
			if(me.text().length()>=maxLength)
			{
				mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
				ok=false;
			}
		}
		mob.recoverEnvStats();
	}
	public void modifyGenExit(MOB mob, Exit me)
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
			genBehaviors(mob,me);
			genAffects(mob,me);
			ok=true;
			if(me.text().length()>=maxLength)
			{
				mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
				ok=false;
			}
		}
		mob.recoverEnvStats();
	}
	public void modifyGenMOB(MOB mob, MOB me)
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
				me.baseCharStats().getMyClass().buildMOB(me,me.baseEnvStats().level(),mob.getAlignment(),mob.baseEnvStats().weight(),mob.getWimpHitPoint(),(char)mob.baseCharStats().getStat(CharStats.GENDER));
			genRejuv(mob,me);
			genRace(mob,me);
			genGender(mob,me);
			genHeight(mob,me);
			genWeight(mob,me);
			genSpeed(mob,me);
			if((oldLevel==0)&&(me.baseEnvStats().level()>0))
				me.baseEnvStats().setDamage((int)Math.round(Util.div(me.baseEnvStats().damage(),me.baseEnvStats().speed())));
			genAttack(mob,me);
			genDamage(mob,me);
			genArmor(mob,me);
			genHitPoints(mob,me);
			genAlignment(mob,me);
			genMoney(mob,me);
			genAbilities(mob,me);
			genBehaviors(mob,me);
			genAffects(mob,me);
			genDisposition(mob,me);
			genSensesMask(mob,me);
			if(me instanceof Rideable)
				genRideable(mob,(Rideable)me);
			ok=true;
			if(me.text().length()>=maxLength)
			{
				mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
				ok=false;
			}
		}

		mob.recoverCharStats();
		mob.recoverEnvStats();
		mob.recoverMaxState();
		mob.resetToMaxState();
		mob.tell("\n\rNow don't forget to equip him with stuff before saving!\n\r");
	}
	public void modifyGenShopkeeper(MOB mob, ShopKeeper me)
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
				me.baseCharStats().getMyClass().buildMOB(me,me.baseEnvStats().level(),mob.getAlignment(),mob.baseEnvStats().weight(),mob.getWimpHitPoint(),(char)mob.baseCharStats().getStat(CharStats.GENDER));
			genRejuv(mob,me);
			genRace(mob,me);
			genHeight(mob,me);
			genWeight(mob,me);
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
			genAbilities(mob,me);
			genBehaviors(mob,me);
			genAffects(mob,me);
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
		mob.recoverMaxState();
		mob.recoverEnvStats();
		mob.tell("\n\rNow don't forget to equip him with non-generic items before saving! If you DO add items to his list, be sure to come back here in case you've exceeded the string limit again.\n\r");
	}
}
