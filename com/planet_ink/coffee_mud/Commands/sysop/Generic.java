package com.planet_ink.coffee_mud.Commands.sysop;

import com.planet_ink.coffee_mud.Commands.base.Scoring;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;
import java.io.*;

public class Generic
{
	private Generic(){}
	
	private static final long maxLength=65535;

	static void genName(MOB mob, Environmental E, boolean showOnly)
		throws IOException
	{
		mob.tell("\n\rName: '"+E.name()+"'.");
		if(showOnly) return;
		String newName=mob.session().prompt("Enter a new one\n\r:","");
		if(newName.length()>0)
			E.setName(newName);
		else
			mob.tell("(no change)");
	}

	static Room genRoomType(MOB mob, Room R, boolean showOnly)
		throws IOException
	{
		mob.tell("\n\rType: '"+CMClass.className(R)+"'");
		if(showOnly) return R;
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
				for(Enumeration e=CMMap.players();e.hasMoreElements();)
				{
					MOB mob2=(MOB)e.nextElement();
					if(mob2.getStartRoom()==R)
					{
						mob2.setStartRoom(newRoom);
						ExternalPlay.DBUpdateMOB(mob2);
					}
				}
				if(R instanceof GridLocale)
					((GridLocale)R).clearGrid();
				newRoom.setID(R.ID());
				newRoom.setArea(R.getArea());
				newRoom.setDisplayText(R.displayText());
				newRoom.setDescription(R.description());
				for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
				{
					Room room=(Room)r.nextElement();
					if(room==R)
					{
						CMMap.replaceRoom(newRoom,room);
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

	static void genDescription(MOB mob, Environmental E, boolean showOnly)
		throws IOException
	{
		mob.tell("\n\rDescription: '"+E.description()+"'.");
		if(showOnly) return;
		String newName=mob.session().prompt("Enter a new one\n\r:","");
		if(newName.length()>0)
			E.setDescription(newName);
		else
			mob.tell("(no change)");
	}

	public static void genDisplayText(MOB mob, Environmental E, boolean showOnly)
		throws IOException
	{
		mob.tell("\n\rDisplay: '"+E.displayText()+"'.");
		if(showOnly) return;
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
	public static void genClosedText(MOB mob, Exit E, boolean showOnly)
		throws IOException
	{
		mob.tell("\n\rClosed Text: '"+E.closedText()+"'.");
		if(showOnly) return;
		String newName=mob.session().prompt("Enter something new\n\r:","");
		if(newName.length()>0)
			E.setExitParams(E.doorName(),E.closeWord(),E.openWord(),newName);
		else
			mob.tell("(no change)");
	}
	public static void genDoorName(MOB mob, Exit E, boolean showOnly)
		throws IOException
	{
		mob.tell("\n\rDoor Name: '"+E.doorName()+"'.");
		if(showOnly) return;
		String newName=mob.session().prompt("Enter something new\n\r:","");
		if(newName.length()>0)
			E.setExitParams(newName,E.closeWord(),E.openWord(),E.closedText());
		else
			mob.tell("(no change)");
	}

	public static void genOpenWord(MOB mob, Exit E, boolean showOnly)
		throws IOException
	{
		mob.tell("\n\rOpen Word: '"+E.openWord()+"'.");
		if(showOnly) return;
		String newName=mob.session().prompt("Enter something new\n\r:","");
		if(newName.length()>0)
			E.setExitParams(E.doorName(),E.closeWord(),newName,E.closedText());
		else
			mob.tell("(no change)");
	}

	public static void genSubOps(MOB mob, Area A, boolean showOnly)
		throws IOException
	{
		String newName="Q";
		while(newName.length()>0)
		{
			mob.tell("\n\rArea SubOperator user names: "+A.getSubOpList());
			if(showOnly) return;
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

	public static void genCloseWord(MOB mob, Exit E, boolean showOnly)
		throws IOException
	{
		mob.tell("\n\rClose Word: '"+E.closeWord()+"'.");
		if(showOnly) return;
		String newName=mob.session().prompt("Enter something new\n\r:","");
		if(newName.length()>0)
			E.setExitParams(E.doorName(),newName,E.openWord(),E.closedText());
		else
			mob.tell("(no change)");
	}
	
	public static void genExitMisc(MOB mob, Exit E, boolean showOnly)
		throws IOException
	{
		if(E.hasALock())
		{
			E.setReadable(false);
			mob.tell("\n\rAssigned Key Item: '"+E.keyName()+"'.");
			if(showOnly) return;
			String newName=mob.session().prompt("Enter something new\n\r:","");
			if(newName.length()>0)
				E.setKeyName(newName);
			else
				mob.tell("(no change)");
		}
		else
		{
			if(showOnly)
			{
				if(!E.isReadable())
					mob.tell("Door not is readable.");
				else
					mob.tell("Door is readable: "+E.readableText());
				return;
			}
			else
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
	}
	
	public static void genReadable(MOB mob, Item E, boolean showOnly)
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
		if(showOnly)
			mob.tell("\n\rItem is readable: "+E.isReadable());
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
				
				if(showOnly) return;
				String newName=mob.session().prompt("Enter something new (?)\n\r:","");
				if((E instanceof Wand)
				||(E instanceof Scroll)
				||((E instanceof Pill)&&(!(CMClass.className(E).endsWith("SuperPill"))))
				||(E instanceof Potion))
				{
					if(newName.length()==0)
						ok=true;
					else
					{
						if(newName.equalsIgnoreCase("?"))
							mob.tell(Lister.reallyList(CMClass.abilities(),-1).toString());
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
							if(!newName.endsWith(";")) newName+=";";
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
		else
		if(E instanceof Drink)
		{
			mob.session().println("\n\rCurrent liquid type: "+EnvResource.RESOURCE_DESCS[((Drink)E).liquidType()&EnvResource.RESOURCE_MASK]);
			if(showOnly) return;
			boolean q=false;
			while(!q)
			{
				String newType=mob.session().prompt("Enter a new type (?)\n\r:",EnvResource.RESOURCE_DESCS[((Drink)E).liquidType()&EnvResource.RESOURCE_MASK]);
				if(newType.equals("?"))
				{
					StringBuffer say=new StringBuffer("");
					for(int i=0;i<EnvResource.RESOURCE_DESCS.length-1;i++)
						if((EnvResource.RESOURCE_DATA[i][0]&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_LIQUID)
							say.append(EnvResource.RESOURCE_DESCS[i]+", ");
					mob.tell(say.toString().substring(0,say.length()-2));
					q=false;
				}
				else
				{
					q=true;
					int newValue=-1;
					for(int i=0;i<EnvResource.RESOURCE_DESCS.length-1;i++)
						if((EnvResource.RESOURCE_DATA[i][0]&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_LIQUID)
							if(newType.equalsIgnoreCase(EnvResource.RESOURCE_DESCS[i]))
								newValue=EnvResource.RESOURCE_DATA[i][0];
					if(newValue>=0)
						((Drink)E).setLiquidType(newValue);
					else
						mob.tell("(no change)");
				}
			}
		}
	}
	
	public static void genGettable(MOB mob, Item E, boolean showOnly)
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
			mob.session().println("4) Non-Locatable : "+(((E.baseEnvStats().sensesMask()&EnvStats.CAN_NOT_SEE)>0)?"true":"false"));
			if(E instanceof Weapon)
				mob.session().println("5) Is Two-Handed : "+E.rawLogicalAnd());
			if(showOnly) return;
			c=mob.session().choose("Enter one to change, or ENTER when done: ","12345\n","\n").toUpperCase();
			switch(c.charAt(0))
			{
			case '1': E.setGettable(!E.isGettable()); break;
			case '2': E.setDroppable(!E.isDroppable()); break;
			case '3': E.setRemovable(!E.isRemovable()); break;
			case '4': if((E.baseEnvStats().sensesMask()&EnvStats.CAN_NOT_SEE)>0)
						  E.baseEnvStats().setSensesMask(E.baseEnvStats().sensesMask()-EnvStats.CAN_NOT_SEE);
					  else
						  E.baseEnvStats().setSensesMask(E.baseEnvStats().sensesMask()|EnvStats.CAN_NOT_SEE);
					  break;
			case '5': if(E instanceof Weapon) 
						  E.setRawLogicalAnd(!E.rawLogicalAnd()); 
					  break;
			}
		}
	}

	public static void toggleDispositionMask(Environmental E, int mask)
	{
		int current=E.baseEnvStats().disposition();
		if((current&mask)==0)
			E.baseEnvStats().setDisposition(current|mask);
		else
			E.baseEnvStats().setDisposition(current&((int)(EnvStats.ALLMASK-mask)));
	}

	public static void genDisposition(MOB mob, Environmental E, boolean showOnly)
		throws IOException
	{
		String c="Q";
		while(!c.equals("\n"))
		{
			mob.session().println("\n\r1) Is Invisible   : "+((E.baseEnvStats().disposition()&EnvStats.IS_INVISIBLE)!=0));
			mob.session().println("2) Is Hidden      : "+((E.baseEnvStats().disposition()&EnvStats.IS_HIDDEN)!=0));
			mob.session().println("3) Is Unseeable   : "+((E.baseEnvStats().disposition()&EnvStats.IS_NOT_SEEN)!=0));
			mob.session().println("4) Is Magical     : "+((E.baseEnvStats().disposition()&EnvStats.IS_BONUS)!=0));
			mob.session().println("5) Is Glowing     : "+((E.baseEnvStats().disposition()&EnvStats.IS_GLOWING)!=0));
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
			if(showOnly) return;
			c=mob.session().choose("Enter one to change, or ENTER when done: ","12345678GE\n","\n").toUpperCase();
			switch(c.charAt(0))
			{
			case '1': toggleDispositionMask(E,EnvStats.IS_INVISIBLE); break;
			case '2': toggleDispositionMask(E,EnvStats.IS_HIDDEN); break;
			case '3': toggleDispositionMask(E,EnvStats.IS_NOT_SEEN); break;
			case '4': toggleDispositionMask(E,EnvStats.IS_BONUS); break;
			case '5': toggleDispositionMask(E,EnvStats.IS_GLOWING); break;
			case '6': toggleDispositionMask(E,EnvStats.IS_FLYING); break;
			case '7': toggleDispositionMask(E,EnvStats.IS_CLIMBING); break;
			case '8': toggleDispositionMask(E,EnvStats.IS_SNEAKING); break;
			case 'G': toggleDispositionMask(E,EnvStats.IS_GOOD); break;
			case 'E': toggleDispositionMask(E,EnvStats.IS_EVIL); break;
			}
		}
	}

	public static boolean genGenericPrompt(MOB mob, String prompt, boolean val)
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

	public static void toggleSensesMask(Environmental E, int mask)
	{
		int current=E.baseEnvStats().sensesMask();
		if((current&mask)==0)
			E.baseEnvStats().setSensesMask(current|mask);
		else
			E.baseEnvStats().setSensesMask(current&((int)(EnvStats.ALLMASK-mask)));
	}
	
	public static void toggleClimateMask(Area A, int mask)
	{
		int current=A.climateType();
		if((current&mask)==0)
			A.setClimateType(current|mask);
		else
			A.setClimateType(current&((int)(EnvStats.ALLMASK-mask)));
	}



	public static void genClimateType(MOB mob, Area A, boolean showOnly)
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
			if(showOnly) return;
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

	public static void genSensesMask(MOB mob, Environmental E, boolean showOnly)
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
			mob.session().println("9) Is Mute            : "+((E.baseEnvStats().sensesMask()&EnvStats.CAN_NOT_SPEAK)!=0));
			mob.session().println("A) Is Deaf            : "+((E.baseEnvStats().sensesMask()&EnvStats.CAN_NOT_HEAR)!=0));
			mob.session().println("B) Is Blind           : "+((E.baseEnvStats().sensesMask()&EnvStats.CAN_NOT_SEE)!=0));
			if(showOnly) return;
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
			case '9': toggleSensesMask(E,EnvStats.CAN_NOT_SPEAK); break;
			case 'A': toggleSensesMask(E,EnvStats.CAN_NOT_HEAR); break;
			case 'B': toggleSensesMask(E,EnvStats.CAN_NOT_SEE); break;
			}
		}
	}

	public static void genDoorsNLocks(MOB mob, Exit E, boolean showOnly)
		throws IOException
	{
		boolean HasDoor=E.hasADoor();
		boolean Open=E.isOpen();
		boolean DefaultsClosed=E.defaultsClosed();
		boolean HasLock=E.hasALock();
		boolean Locked=E.isLocked();
		boolean DefaultsLocked=E.defaultsLocked();
		if(showOnly){
			mob.tell("\n\rHas a door: "+E.hasADoor()
					+"\n\rHas a lock: "+E.hasALock()
					+"\n\rOpen ticks: "+E.openDelayTicks());
			return;
		}
		
		if(genGenericPrompt(mob,"Has a door",E.hasADoor()))
		{
			HasDoor=true;
			DefaultsClosed=genGenericPrompt(mob,"Defaults closed",E.defaultsClosed());
			Open=!DefaultsClosed;
			if(genGenericPrompt(mob,"Has a lock",E.hasALock()))
			{
				HasLock=true;
				DefaultsLocked=genGenericPrompt(mob,"Defaults locked",E.defaultsLocked());
				Locked=DefaultsLocked;
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
	}

	public static String makeContainerTypes(Container E)
	{
		String canContain=", "+Container.CONTAIN_DESCS[0];
		if(E.containTypes()>0)
		{
			canContain="";
			for(int i=0;i<20;i++)
				if(Util.isSet((int)E.containTypes(),i))
					canContain+=", "+Container.CONTAIN_DESCS[i+1];
		}
		return canContain.substring(2);
	}
	
	
	public static void genLidsNLocks(MOB mob, Container E, boolean showOnly)
		throws IOException
	{
		if(showOnly){
			mob.tell("\n\rCan contain : "+makeContainerTypes(E)
					+"\n\rHas a lid   : "+E.hasALid()
					+"\n\rHas a lock  : "+E.hasALock());
			return;
		}
		String change="NO";
		while(change.length()>0)
		{
			mob.tell("\n\rCan only contain: "+makeContainerTypes(E));
			change=mob.session().prompt("Enter a type to add/remove (?)\n\r:","");
			if(change.length()==0) break;
			int found=-1;
			if(change.equalsIgnoreCase("?"))
				for(int i=0;i<Container.CONTAIN_DESCS.length;i++)
					mob.tell(Container.CONTAIN_DESCS[i]);
			else
			{
				for(int i=0;i<Container.CONTAIN_DESCS.length;i++)
					if(Container.CONTAIN_DESCS[i].startsWith(change.toUpperCase()))
						found=i;
				if(found<0)
					mob.tell("Unknown type.  Try LIST.");
				else
				if(found==0)
					E.setContainTypes(0);
				else
				if(Util.isSet((int)E.containTypes(),found-1))
					E.setContainTypes(E.containTypes()-Util.pow(2,found-1));
				else
					E.setContainTypes(E.containTypes()|Util.pow(2,found-1));
			}
		}
		
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
	}

	public static void genLevel(MOB mob, Environmental E, boolean showOnly)
		throws IOException
	{
		if(E.baseEnvStats().level()<0)
			E.baseEnvStats().setLevel(1);
		mob.tell("\n\rLevel: '"+E.baseEnvStats().level()+"'.");
		if(showOnly) return;
		E.baseEnvStats().setLevel(getNumericData(mob,"Enter a new level\n\r:",E.baseEnvStats().level()));
	}

	public static void genRejuv(MOB mob, Environmental E, boolean showOnly)
		throws IOException
	{
		mob.tell("\n\rRejuv Ticks: '"+E.baseEnvStats().rejuv()+"'.");
		if(showOnly) return;
		String rlevel=mob.session().prompt("Enter new amount\n\r:","");
		int newLevel=Util.s_int(rlevel);
		if((newLevel>0)||(rlevel.trim().equals("0")))
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

	public static void genUses(MOB mob, Item E, boolean showOnly)
		throws IOException
	{
		mob.tell("\n\rUses Remaining: '"+E.usesRemaining()+"'.");
		if(showOnly) return;
		E.setUsesRemaining(getNumericData(mob,"Enter a new value\n\r:",E.usesRemaining()));
	}

	public static void genCondition(MOB mob, Item E, boolean showOnly)
		throws IOException
	{
		mob.tell("\n\rCondition: '"+E.usesRemaining()+"'.");
		if(showOnly) return;
		E.setUsesRemaining(getNumericData(mob,"Enter a new value\n\r:",E.usesRemaining()));
	}

	public static void genMiscSet(MOB mob, Environmental E)
		throws IOException
	{
		if(E instanceof ShopKeeper)
			modifyGenShopkeeper(mob,(ShopKeeper)E);
		else
		if(E instanceof MOB)
		{
			if(((MOB)E).isMonster())
				modifyGenMOB(mob,(MOB)E);
			else
				modifyPlayer(mob,(MOB)E);
		}
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

	
	public static int getNumericData(MOB mob, String prompt, int oldValue)
		throws IOException
	{
		String value=mob.session().prompt(prompt,"");
		int numValue=Util.s_int(value);
		if((numValue==0)&&(!value.trim().equals("0")))
		{
			mob.tell("(no change)");
			return oldValue;
		}
		return numValue;
	}
	
	public static double getDoubleData(MOB mob, String prompt, double oldValue)
		throws IOException
	{
		String value=mob.session().prompt(prompt,"");
		double numValue=Util.s_double(value);
		if((numValue==0.0)&&(!value.trim().equals("0")))
		{
			mob.tell("(no change)");
			return oldValue;
		}
		return numValue;
	}
	
	public static void genMiscText(MOB mob, Environmental E, boolean showOnly)
		throws IOException
	{
		if(E.isGeneric())
			genMiscSet(mob,E);
		else
		{
			mob.tell("\n\rMisc Text: '"+E.text()+"'.");
			if(showOnly) return;
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

	public static void genTitleRoom(MOB mob, LandTitle E, boolean showOnly)
		throws IOException
	{
		mob.tell("\n\rLand plot ID: '"+E.landRoomID()+"'.");
		if(showOnly) return;
		String newText="?!?!";
		while((newText.length()>0)&&(CMMap.getRoom(newText)==null))
		{
			newText=mob.session().prompt("New Room ID:","");
			if((newText.length()==0)&&(CMMap.getRoom(newText)==null))
				mob.tell("That room ID doesn't exist!");
		}
		if(newText.length()>0)
			E.setLandRoomID(newText);
		else
			mob.tell("(no change)");

	}

	public static void genAbility(MOB mob, Environmental E, boolean showOnly)
		throws IOException
	{
		mob.tell("\n\rMagical Ability: '"+E.baseEnvStats().ability()+"'.");
		if(showOnly) return;
		E.baseEnvStats().setAbility(getNumericData(mob,"Enter a new value (0=no magic)\n\r:",E.baseEnvStats().ability()));
	}

	public static void genHitPoints(MOB mob, Environmental E, boolean showOnly)
		throws IOException
	{
		if(E.baseEnvStats().ability()<1) E.baseEnvStats().setAbility(11);
		mob.tell("\n\rHit Points/Level Modifier (hp=((10*level) + (random*level*THIS))) : '"+E.baseEnvStats().ability()+"'.");
		if(showOnly) return;
		String newLevelStr=mob.session().prompt("Enter a new value\n\r:","");
		int newLevel=Util.s_int(newLevelStr);
		if(newLevel>0)
			E.baseEnvStats().setAbility(newLevel);
		else
			mob.tell("(no change)");
	}

	public static void genValue(MOB mob, Item E, boolean showOnly)
		throws IOException
	{
		mob.tell("\n\rBase Value: '"+E.baseGoldValue()+"'.");
		if(showOnly) return;
		E.setBaseValue(getNumericData(mob,"Enter a new value\n\r:",E.baseGoldValue()));
	}

	public static void genWeight(MOB mob, Environmental E, boolean showOnly)
		throws IOException
	{
		mob.tell("\n\rWeight: '"+E.baseEnvStats().weight()+"'.");
		if(showOnly) return;
		E.baseEnvStats().setWeight(getNumericData(mob,"Enter a new weight\n\r:",E.baseEnvStats().weight()));
	}


	public static void genHeight(MOB mob, Environmental E, boolean showOnly)
		throws IOException
	{
		mob.tell("\n\rHeight: '"+E.baseEnvStats().height()+"'.");
		if(showOnly) return;
		E.baseEnvStats().setHeight(getNumericData(mob,"Enter a new height\n\r:",E.baseEnvStats().height()));
	}


	public static void genSize(MOB mob, Armor E, boolean showOnly)
		throws IOException
	{
		mob.tell("\n\rSize: '"+E.baseEnvStats().height()+"'.");
		if(showOnly) return;
		E.baseEnvStats().setHeight(getNumericData(mob,"Enter a new size\n\r:",E.baseEnvStats().height()));
	}


	public static void genCapacity(MOB mob, Container E, boolean showOnly)
		throws IOException
	{
		mob.tell("\n\rCapacity: '"+E.capacity()+"'.");
		if(showOnly) return;
		E.setCapacity(getNumericData(mob,"Enter a new capacity\n\r:",E.capacity()));
	}

	public static void genAttack(MOB mob, Environmental E, boolean showOnly)
		throws IOException
	{
		mob.tell("\n\rAttackAdjustment: '"+E.envStats().attackAdjustment()+"'.");
		if(showOnly) return;
		E.baseEnvStats().setAttackAdjustment(getNumericData(mob,"Enter a new value\n\r:",E.baseEnvStats().attackAdjustment()));
	}

	public static void genDamage(MOB mob, Environmental E, boolean showOnly)
		throws IOException
	{
		mob.tell("\n\rDamage/Hit: '"+E.baseEnvStats().damage()+"'.");
		if(showOnly) return;
		E.baseEnvStats().setDamage(getNumericData(mob,"Enter a new value\n\r:",E.baseEnvStats().damage()));
	}

	public static void genBanker(MOB mob, Banker E, boolean showOnly)
		throws IOException
	{
		mob.tell("\n\rCoin Interest: '"+E.getCoinInterest()+"'% per real day.");
		if(!showOnly)
			E.setCoinInterest(getDoubleData(mob,"Enter a new value\n\r:",E.getCoinInterest()));
		mob.tell("\n\rItem Interest: '"+E.getItemInterest()+"'% per real day.");
		if(!showOnly)
			E.setItemInterest(getDoubleData(mob,"Enter a new value\n\r:",E.getItemInterest()));
		mob.tell("\n\rBank Chain   : '"+E.bankChain()+"'.");
		if(showOnly) return;
		String newValue=mob.session().prompt("Enter a new chain\n\r:","");
		if(newValue.length()>0)
			E.setBankChain(newValue);
		else
			mob.tell("(no change)");
	}

	public static void genSpeed(MOB mob, Environmental E, boolean showOnly)
		throws IOException
	{
		mob.tell("\n\rAttacks/Round: '"+((int)Math.round(E.baseEnvStats().speed()))+"'.");
		if(showOnly) return;
		E.baseEnvStats().setSpeed(getNumericData(mob,"Enter a new value\n\r:",(int)Math.round(E.baseEnvStats().speed())));
	}

	public static void genArmor(MOB mob, Environmental E, boolean showOnly)
		throws IOException
	{
		if(E instanceof MOB)
			mob.tell("\n\rArmor (lower-better): '"+E.baseEnvStats().armor()+"'.");
		else
			mob.tell("\n\rArmor (higher-better): '"+E.baseEnvStats().armor()+"'.");
		if(showOnly) return;
		E.baseEnvStats().setArmor(getNumericData(mob,"Enter a new value\n\r:",E.baseEnvStats().armor()));
	}

	public static void genMoney(MOB mob, MOB E, boolean showOnly)
		throws IOException
	{
		mob.tell("\n\rMoney: '"+E.getMoney()+"'.");
		if(showOnly) return;
		E.setMoney(getNumericData(mob,"Enter a new value\n\r:",E.getMoney()));
	}

	public static void genWeaponAmmo(MOB mob, Weapon E, boolean showOnly)
		throws IOException
	{
		String defaultAmmo=(E.requiresAmmunition())?"Y":"N";
		if(showOnly)
		{
			mob.tell("Ammo required: "+(E.requiresAmmunition()?E.ammunitionType():"NO"));
			return;
		}
		
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
	public static void genWeaponRanges(MOB mob, Weapon E, boolean showOnly)
		throws IOException
	{
		mob.tell("\n\rMinimum/Maximum Ranges: "+((int)Math.round(E.minRange()))+"/"+((int)Math.round(E.maxRange()))+".");
		if(showOnly) return;
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
	
	public static void genWeaponType(MOB mob, Weapon E, boolean showOnly)
		throws IOException
	{
		mob.tell("\n\rWeapon Attack Type: '"+Weapon.typeDescription[E.weaponType()]+"'.");
		if(showOnly) return;
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

	public static void genTechLevel(MOB mob, Area A, boolean showOnly)
		throws IOException
	{
		mob.tell("\n\rTechnology Level: '"+Area.TECH_DESCS[A.getTechLevel()]+"'.");
		if(showOnly) return;
		boolean q=false;
		while(!q)
		{
			String newType=mob.session().prompt("Enter a new level (?)\n\r:",Area.TECH_DESCS[A.getTechLevel()]);
			if(newType.equals("?"))
			{
				StringBuffer say=new StringBuffer("");
				for(int i=0;i<Area.TECH_DESCS.length-1;i++)
					say.append(Area.TECH_DESCS[i]+", ");
				mob.tell(say.toString().substring(0,say.length()-2));
				q=false;
			}
			else
			{
				q=true;
				int newValue=-1;
				for(int i=0;i<Area.TECH_DESCS.length-1;i++)
					if(newType.equalsIgnoreCase(Area.TECH_DESCS[i]))
						newValue=i;
				if(newValue>=0)
					A.setTechLevel(newValue);
				else
					mob.tell("(no change)");
			}
		}
	}

	
	public static void genMaterialCode(MOB mob, Item E, boolean showOnly)
		throws IOException
	{
		mob.tell("\n\rMaterial Type: '"+EnvResource.RESOURCE_DESCS[E.material()&EnvResource.RESOURCE_MASK]+"'.");
		if(showOnly) return;
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

	public static void genAlignment(MOB mob, MOB E, boolean showOnly)
		throws IOException
	{
		mob.tell("\n\rAlignment: '"+CommonStrings.alignmentStr(E.getAlignment())+"'.");
		if(showOnly) return;
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

	public static void genGender(MOB mob, MOB E, boolean showOnly)
		throws IOException
	{
		mob.tell("\n\rGender: '"+Character.toUpperCase((char)E.baseCharStats().getStat(CharStats.GENDER))+"'.");
		if(showOnly) return;
		String newType=mob.session().choose("Enter a new gender (M/F/N)\n\r:","MFN","");
		int newValue=-1;
		if(newType.length()>0)
			newValue=("MFN").indexOf(newType.trim().toUpperCase());
		if(newValue>=0)
		{
			switch(newValue)
			{
			case 0:
				E.baseCharStats().setStat(CharStats.GENDER,(int)'M');
				break;
			case 1:
				E.baseCharStats().setStat(CharStats.GENDER,(int)'F');
				break;
			case 2:
				E.baseCharStats().setStat(CharStats.GENDER,(int)'N');
				break;
			}
		}
		else
			mob.tell("(no change)");
	}

	public static void genWeaponClassification(MOB mob, Weapon E, boolean showOnly)
		throws IOException
	{
		mob.tell("\n\rWeapon Classification: '"+Weapon.classifictionDescription[E.weaponClassification()]+"'.");
		if(showOnly) return;
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

	public static void genSecretIdentity(MOB mob, Item E, boolean showOnly)
		throws IOException
	{
		mob.tell("\n\rSecret Identity: '"+E.rawSecretIdentity()+"'.");
		if(showOnly) return;
		String newValue=mob.session().prompt("Enter a new identity\n\r:","");
		if(newValue.length()>0)
			E.setSecretIdentity(newValue);
		else
			mob.tell("(no change)");
	}

	public static void genNourishment(MOB mob, Food E, boolean showOnly)
		throws IOException
	{
		mob.tell("\n\rNourishment/Eat: '"+E.nourishment()+"'.");
		if(showOnly) return;
		int newValue=Util.s_int(mob.session().prompt("Enter a new amount\n\r:",""));
		if(newValue>0)
			E.setNourishment(newValue);
		else
			mob.tell("(no change)");
	}

	public static void genRace(MOB mob, MOB E, boolean showOnly)
		throws IOException
	{
		String raceID="begin!";
		while(raceID.length()>0)
		{
			mob.tell("\n\rRace: '"+E.baseCharStats().getMyRace().ID()+"'.");
			if(showOnly) return;
			raceID=mob.session().prompt("Enter a new race (?)\n\r:","").trim();
			if(raceID.equalsIgnoreCase("?"))
				mob.tell(Lister.reallyList(CMClass.races(),-1).toString());
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

	public static void genCharClass(MOB mob, MOB E, boolean showOnly)
		throws IOException
	{
		String classID="begin!";
		while(classID.length()>0)
		{
			mob.tell("\n\rClass: '"+E.baseCharStats().getCurrentClass().ID()+"'.");
			if(showOnly) return;
			classID=mob.session().prompt("Enter a new class (?)\n\r:","").trim();
			if(classID.equalsIgnoreCase("?"))
				mob.tell(Lister.reallyList(CMClass.charClasses(),-1).toString());
			else
			if(classID.length()==0)
				mob.tell("(no change)");
			else
			{
				CharClass C=CMClass.getCharClass(classID);
				if(C!=null)
					E.baseCharStats().setCurrentClass(C);
				else
					mob.tell("Unknown character class! Try 'LIST'.");
			}
		}
	}

	public static void genBehaviors(MOB mob, Environmental E, boolean showOnly)
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
			if(showOnly) return;
			behave=mob.session().prompt("Enter a behavior to add/remove (?)\n\r:","");
			if(behave.length()>0)
			{
				if(behave.equalsIgnoreCase("?"))
					mob.tell(Lister.reallyList(CMClass.behaviors(),-1).toString());
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

	public static void genAffects(MOB mob, Environmental E, boolean showOnly)
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
			if(showOnly) return;
			behave=mob.session().prompt("Enter an affect to add/remove (?)\n\r:","");
			if(behave.length()>0)
			{
				if(behave.equalsIgnoreCase("?"))
					mob.tell(Lister.reallyList(CMClass.abilities(),-1).toString());
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

	public static void genRideable(MOB mob, Rideable R, boolean showOnly)
		throws IOException
	{
		mob.tell("\n\rRideable Type: '"+Rideable.RIDEABLE_DESCS[R.rideBasis()]+"'.");
		boolean q=false;
		String sel="LWACBTEDG";
		while((!q)&&(!showOnly))
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
		
		mob.tell("\n\rNumber of MOBs held: '"+R.riderCapacity()+"'.");
		if(showOnly) return;
		String newLevelStr=mob.session().prompt("Enter a new value: ","");
		int newLevel=Util.s_int(newLevelStr);
		if(newLevel>0)
			R.setRiderCapacity(newLevel);
		else
			mob.tell("(no change)");
	}
	
	public static void genShopkeeper(MOB mob, ShopKeeper E, boolean showOnly)
		throws IOException
	{
		mob.tell("\n\rShopkeeper type: '"+E.storeKeeperString()+"'.");
		StringBuffer buf=new StringBuffer("");
		StringBuffer codes=new StringBuffer("");
		String codeStr="0123456789ABCDEFGHIJKLMNOP";
		for(int r=0;r<ShopKeeper.SOLDCODES.length;r++)
		{
			if((E instanceof Banker)||(r!=ShopKeeper.DEAL_BANKER))
			{
				char c=codeStr.charAt(r);
				codes.append(c);
				buf.append(c+") "+ShopKeeper.SOLDCODES[r]+"\n\r");
			}
		}
		if(showOnly)
			mob.tell(buf.toString());
		else
		{
			String newType=mob.session().choose(buf.toString()+"Enter a new value\n\r:",codes.toString(),"");
			int newValue=-1;
			if(newType.length()>0)
				newValue=codeStr.indexOf(newType.toUpperCase());
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
			if(showOnly)
			{
				mob.tell("Prejudice: '"+E.prejudiceFactors()+"'.");
				return;
			}
			itemstr=mob.session().prompt("Enter something to add/remove (?)\n\r:","");
			if(itemstr.length()>0)
			{
				if(itemstr.equalsIgnoreCase("?"))
				{
					mob.tell(Lister.reallyList(CMClass.abilities(),-1).toString());
					mob.tell(Lister.reallyList(CMClass.armor(),-1).toString());
					mob.tell(Lister.reallyList(CMClass.weapons(),-1).toString());
					mob.tell(Lister.reallyList(CMClass.miscMagic(),-1).toString());
					mob.tell(Lister.reallyList(CMClass.items(),-1).toString());
					mob.tell(Lister.reallyList(CMClass.mobTypes(),-1).toString());
				}
				else
				{
					Environmental item=E.getStock(itemstr,null);
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
							if((item instanceof Ability)
							   &&((E.whatIsSold()==E.DEAL_TRAINER)||(E.whatIsSold()==E.DEAL_CASTER)))
								ok=true;
							else
							if(E.whatIsSold()==E.DEAL_INVENTORYONLY)
								ok=true;
							if(!ok)
							{
								mob.tell("The shopkeeper does not sell that.");
							}
							else
							{
								boolean alreadyHasIt=false;

								if(E.doIHaveThisInStock(item.name(),null))
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
		mob.tell("\n\rPrejudice: '"+E.prejudiceFactors()+"'.");
		if(showOnly) return;
		String newValue=mob.session().prompt("Enter a new string\n\r:","");
		if(newValue.length()>0)
			E.setPrejudiceFactors(newValue);
		else
			mob.tell("(no change)");
	}

	public static void genAbilities(MOB mob, MOB E, boolean showOnly)
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
			if(showOnly) return;
			behave=mob.session().prompt("Enter an ability to add/remove (?)\n\r:","");
			if(behave.length()>0)
			{
				if(behave.equalsIgnoreCase("?"))
					mob.tell(Lister.reallyList(CMClass.abilities(),-1).toString());
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

	public static void genDeity(MOB mob, Deity E, boolean showOnly)
		throws IOException
	{
		mob.tell("\n\rCleric Requirements: '"+E.getClericRequirements()+"'.");
		if(!showOnly)
		{
			String newValue=mob.session().prompt("Enter new requirements\n\r:","");
			if(newValue.length()>0)
				E.setClericRequirements(newValue);
			else
				mob.tell("(no change)");
		}
		mob.tell("\n\rCleric Ritual: '"+E.getClericRitual()+"'.");
		if(!showOnly)
		{
			String newValue=mob.session().prompt("Enter new ritual\n\r:","");
			if(newValue.length()>0)
				E.setClericRitual(newValue);
			else
				mob.tell("(no change)");
		}
		mob.tell("\n\rWorshiper Requirements: '"+E.getWorshipRequirements()+"'.");
		if(!showOnly)
		{
			String newValue=mob.session().prompt("Enter new requirements\n\r:","");
			if(newValue.length()>0)
				E.setWorshipRequirements(newValue);
			else
				mob.tell("(no change)");
		}
		mob.tell("\n\rWorshiper Ritual: '"+E.getWorshipRitual()+"'.");
		if(!showOnly)
		{
			String newValue=mob.session().prompt("Enter new ritual\n\r:","");
			if(newValue.length()>0)
				E.setWorshipRitual(newValue);
			else
				mob.tell("(no change)");
		}
		String behave="NO";
		while(behave.length()>0)
		{
			String abilitiestr="";
			for(int a=0;a<E.numBlessings();a++)
			{
				Ability A=E.fetchBlessing(a);
				if((A!=null)&&(!A.isBorrowed(E)))
					abilitiestr+=A.ID()+", ";
			}
			if(abilitiestr.length()>0)
				abilitiestr=abilitiestr.substring(0,abilitiestr.length()-2);
			mob.tell("\n\rBlessings: '"+abilitiestr+"'.");
			if(showOnly) return;
			behave=mob.session().prompt("Enter an ability to add/remove (?)\n\r:","");
			if(behave.length()>0)
			{
				if(behave.equalsIgnoreCase("?"))
					mob.tell(Lister.reallyList(CMClass.abilities(),-1).toString());
				else
				{
					Ability chosenOne=null;
					for(int a=0;a<E.numBlessings();a++)
					{
						Ability A=E.fetchBlessing(a);
						if((A!=null)&&(A.ID().equalsIgnoreCase(behave)))
							chosenOne=A;
					}
					if(chosenOne!=null)
					{
						mob.tell(chosenOne.ID()+" removed.");
						E.delBlessing(chosenOne);
					}
					else
					{
						chosenOne=(Ability)CMClass.getAbility(behave);
						if(chosenOne!=null)
						{
							boolean alreadyHasIt=false;
							for(int a=0;a<E.numBlessings();a++)
							{
								Ability A=E.fetchBlessing(a);
								if((A!=null)&&(A.ID().equals(chosenOne.ID())))
									alreadyHasIt=true;
							}
							if(!alreadyHasIt)
								mob.tell(chosenOne.ID()+" added.");
							else
								mob.tell(chosenOne.ID()+" re-added.");
							if(!alreadyHasIt)
								E.addBlessing((Ability)chosenOne.copyOf());
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

	public static void genGridLocale(MOB mob, GridLocale E, boolean showOnly)
		throws IOException
	{
		mob.tell("\n\rSize (X): '"+E.xSize()+"'.");
		if(!showOnly)
		{
			String newValue=mob.session().prompt("Enter a new size\n\r:","");
			if(Util.s_int(newValue)>0)
				E.setXSize(Util.s_int(newValue));
			else
				mob.tell("(no change)");
		}
		mob.tell("\n\rSize (Y): '"+E.ySize()+"'.");
		if(!showOnly)
		{
			String newValue=mob.session().prompt("Enter a new size\n\r:","");
			if(Util.s_int(newValue)>0)
				E.setYSize(Util.s_int(newValue));
			else
				mob.tell("(no change)");
		}
	}
	
	public static void genWornLocation(MOB mob, Item E, boolean showOnly)
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
			for(int l=0;l<18;l++)
			{
				int wornCode=1<<l;
				if(Sense.wornLocation(wornCode).length()>0)
				{
					String header=(l+2)+": ("+Sense.wornLocation(wornCode)+") : "+(((E.rawProperLocationBitmap()&wornCode)==wornCode)?"YES":"NO");
					mob.tell(header);
					maxCode=l+2;
				}
			}
			if(showOnly) return;
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

	public static void genThirstQuenched(MOB mob, Drink E, boolean showOnly)
		throws IOException
	{
		mob.tell("\n\rQuenched/Drink: '"+E.thirstQuenched()+"'.");
			if(showOnly) return;
		E.setThirstQuenched(getNumericData(mob,"Enter a new amount\n\r:",E.thirstQuenched()));
	}

	public static void genDrinkHeld(MOB mob, Drink E, boolean showOnly)
		throws IOException
	{
		mob.tell("\n\rAmount of Drink Held: '"+E.liquidHeld()+"'.");
		if(showOnly) return;
		E.setLiquidHeld(getNumericData(mob,"Enter a new amount\n\r:",E.liquidHeld()));
		E.setLiquidRemaining(E.liquidHeld());
	}



	public static void modifyGenItem(MOB mob, Item me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		boolean showOnly=false;
		while((!ok)&&(!showOnly))
		{
			genName(mob,me,showOnly);
			genDisplayText(mob,me,showOnly);
			genDescription(mob,me,showOnly);
			genLevel(mob,me,showOnly);
			genSecretIdentity(mob,me,showOnly);
			genMaterialCode(mob,me,showOnly);
			genGettable(mob,me,showOnly);
			genReadable(mob,me,showOnly);
			genRejuv(mob,me,showOnly);
			genAbility(mob,me,showOnly);
			genUses(mob,me,showOnly);
			genValue(mob,me,showOnly);
			genWeight(mob,me,showOnly);
			genDisposition(mob,me,showOnly);
			genBehaviors(mob,me,showOnly);
			genAffects(mob,me,showOnly);
			if(me instanceof LandTitle)
				genTitleRoom(mob,(LandTitle)me,showOnly);
			ok=true;
			me.recoverEnvStats();
			if(me.text().length()>=maxLength)
			{
				mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
				ok=false;
			}
		}
	}

	public static void modifyGenFood(MOB mob, Food me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		boolean showOnly=false;
		while((!ok)&&(!showOnly))
		{
			genName(mob,me,showOnly);
			genDisplayText(mob,me,showOnly);
			genDescription(mob,me,showOnly);
			genSecretIdentity(mob,me,showOnly);
			genLevel(mob,me,showOnly);
			genValue(mob,me,showOnly);
			genRejuv(mob,me,showOnly);
			genWeight(mob,me,showOnly);
			genNourishment(mob,me,showOnly);
			genDisposition(mob,me,showOnly);
			genGettable(mob,me,showOnly);
			genReadable(mob,me,showOnly);
			genBehaviors(mob,me,showOnly);
			genAffects(mob,me,showOnly);
			ok=true;
			me.recoverEnvStats();
			if(me.text().length()>=maxLength)
			{
				mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
				ok=false;
			}
		}
	}
	public static void modifyGenDrink(MOB mob, Drink me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		boolean showOnly=false;
		while((!ok)&&(!showOnly))
		{
			genName(mob,me,showOnly);
			genDisplayText(mob,me,showOnly);
			genDescription(mob,me,showOnly);
			genSecretIdentity(mob,(Item)me,showOnly);
			genValue(mob,(Item)me,showOnly);
			genLevel(mob,me,showOnly);
			genWeight(mob,me,showOnly);
			genRejuv(mob,me,showOnly);
			genThirstQuenched(mob,me,showOnly);
			genMaterialCode(mob,(Item)me,showOnly);
			genDrinkHeld(mob,me,showOnly);
			genGettable(mob,(Item)me,showOnly);
			genReadable(mob,(Item)me,showOnly);
			genBehaviors(mob,me,showOnly);
			genAffects(mob,me,showOnly);
			genDisposition(mob,me,showOnly);
			ok=true;
			me.recoverEnvStats();
			if(me.text().length()>=maxLength)
			{
				mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
				ok=false;
			}
		}
	}

	public static void modifyGenWallpaper(MOB mob, Item me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		boolean showOnly=false;
		while((!ok)&&(!showOnly))
		{
			genName(mob,me,showOnly);
			genDescription(mob,me,showOnly);
			genReadable(mob,me,showOnly);
			ok=true;
			me.recoverEnvStats();
			if(me.text().length()>=maxLength)
			{
				mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
				ok=false;
			}
		}
	}

	public static void modifyGenMap(MOB mob, com.planet_ink.coffee_mud.interfaces.Map me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		boolean showOnly=false;
		while((!ok)&&(!showOnly))
		{
			genName(mob,me,showOnly);
			genDisplayText(mob,me,showOnly);
			genDescription(mob,me,showOnly);
			genLevel(mob,me,showOnly);
			genSecretIdentity(mob,me,showOnly);
			genGettable(mob,me,showOnly);
			genReadable(mob,me,showOnly);
			genValue(mob,me,showOnly);
			genWeight(mob,me,showOnly);
			genRejuv(mob,me,showOnly);
			genMaterialCode(mob,me,showOnly);
			genDisposition(mob,me,showOnly);
			genBehaviors(mob,me,showOnly);
			genAffects(mob,me,showOnly);
			ok=true;
			me.recoverEnvStats();
			if(me.text().length()>=maxLength)
			{
				mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
				ok=false;
			}
		}
	}

	public static void modifyGenContainer(MOB mob, Container me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		boolean showOnly=false;
		while((!ok)&&(!showOnly))
		{
			genName(mob,me,showOnly);
			genDisplayText(mob,me,showOnly);
			genDescription(mob,me,showOnly);
			genLevel(mob,me,showOnly);
			genRejuv(mob,me,showOnly);
			genCapacity(mob,me,showOnly);
			genLidsNLocks(mob,me,showOnly);
			genMaterialCode(mob,me,showOnly);
			genSecretIdentity(mob,me,showOnly);
			genValue(mob,me,showOnly);
			genUses(mob,me,showOnly);
			genWeight(mob,me,showOnly);
			genDisposition(mob,me,showOnly);
			genGettable(mob,me,showOnly);
			genReadable(mob,me,showOnly);
			genBehaviors(mob,me,showOnly);
			genAffects(mob,me,showOnly);
			if(me instanceof Rideable)
				genRideable(mob,(Rideable)me,showOnly);
			ok=true;
			me.recoverEnvStats();
			if(me.text().length()>=maxLength)
			{
				mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
				ok=false;
			}
		}
	}

	public static void modifyGenWeapon(MOB mob, Weapon me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		boolean showOnly=false;
		while((!ok)&&(!showOnly))
		{
			genName(mob,me,showOnly);
			genDisplayText(mob,me,showOnly);
			genDescription(mob,me,showOnly);
			genLevel(mob,me,showOnly);
			genAttack(mob,me,showOnly);
			genDamage(mob,me,showOnly);
			genMaterialCode(mob,me,showOnly);
			genWeaponType(mob,me,showOnly);
			genWeaponClassification(mob,me,showOnly);
			genWeaponRanges(mob,me,showOnly);
			if(me instanceof Wand)
				genReadable(mob,me,showOnly);
			else
				genWeaponAmmo(mob,me,showOnly);
			genRejuv(mob,me,showOnly);
			if((!me.requiresAmmunition())&&(!(me instanceof Wand)))
				genCondition(mob,me,showOnly);
			genAbility(mob,me,showOnly);
			genSecretIdentity(mob,me,showOnly);
			genGettable(mob,me,showOnly);
			genValue(mob,me,showOnly);
			genWeight(mob,me,showOnly);
			genDisposition(mob,me,showOnly);
			genBehaviors(mob,me,showOnly);
			genAffects(mob,me,showOnly);
			ok=true;
			me.recoverEnvStats();
			if(me.text().length()>=maxLength)
			{
				mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
				ok=false;
			}
		}
	}
	public static void modifyGenArmor(MOB mob, Armor me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		boolean showOnly=false;
		while((!ok)&&(!showOnly))
		{
			genName(mob,me,showOnly);
			genDisplayText(mob,me,showOnly);
			genDescription(mob,me,showOnly);
			genLevel(mob,me,showOnly);
			genMaterialCode(mob,me,showOnly);
			genWornLocation(mob,me,showOnly);
			genRejuv(mob,me,showOnly);
			genArmor(mob,me,showOnly);
			genCondition(mob,me,showOnly);
			genAbility(mob,me,showOnly);
			genSecretIdentity(mob,me,showOnly);
			genGettable(mob,me,showOnly);
			genCapacity(mob,me,showOnly);
			genLidsNLocks(mob,me,showOnly);
			genReadable(mob,me,showOnly);
			genValue(mob,me,showOnly);
			genWeight(mob,me,showOnly);
			genSize(mob,me,showOnly);
			genDisposition(mob,me,showOnly);
			genBehaviors(mob,me,showOnly);
			genAffects(mob,me,showOnly);
			ok=true;
			me.recoverEnvStats();
			if(me.text().length()>=maxLength)
			{
				mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
				ok=false;
			}
		}
	}
	public static void modifyGenExit(MOB mob, Exit me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		boolean showOnly=false;
		while((!ok)&&(!showOnly))
		{
			genName(mob,me,showOnly);
			genDisplayText(mob,me,showOnly);
			genDescription(mob,me,showOnly);
			genLevel(mob,me,showOnly);
			genDoorsNLocks(mob,me,showOnly);
			if(me.hasADoor())
			{
				genClosedText(mob,me,showOnly);
				genDoorName(mob,me,showOnly);
				genOpenWord(mob,me,showOnly);
				genCloseWord(mob,me,showOnly);
			}
			genExitMisc(mob,me,showOnly);
			genDisposition(mob,me,showOnly);
			genBehaviors(mob,me,showOnly);
			genAffects(mob,me,showOnly);
			ok=true;
			me.recoverEnvStats();
			if(me.text().length()>=maxLength)
			{
				mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
				ok=false;
			}
		}
	}
	public static void modifyGenMOB(MOB mob, MOB me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		boolean showOnly=false;
		while((!ok)&&(!showOnly))
		{
			genName(mob,me,showOnly);
			genDisplayText(mob,me,showOnly);
			genDescription(mob,me,showOnly);
			int oldLevel=me.baseEnvStats().level();
			genLevel(mob,me,showOnly);
			if((oldLevel==0)&&(me.baseEnvStats().level()>0))
				me.baseCharStats().getCurrentClass().buildMOB(me,me.baseEnvStats().level(),mob.getAlignment(),mob.baseEnvStats().weight(),mob.getWimpHitPoint(),(char)mob.baseCharStats().getStat(CharStats.GENDER));
			genRejuv(mob,me,showOnly);
			genRace(mob,me,showOnly);
			genGender(mob,me,showOnly);
			genHeight(mob,me,showOnly);
			genWeight(mob,me,showOnly);
			genSpeed(mob,me,showOnly);
			if((oldLevel==0)&&(me.baseEnvStats().level()>0))
				me.baseEnvStats().setDamage((int)Math.round(Util.div(me.baseEnvStats().damage(),me.baseEnvStats().speed())));
			genAttack(mob,me,showOnly);
			genDamage(mob,me,showOnly);
			genArmor(mob,me,showOnly);
			genHitPoints(mob,me,showOnly);
			genAlignment(mob,me,showOnly);
			genMoney(mob,me,showOnly);
			genAbilities(mob,me,showOnly);
			genBehaviors(mob,me,showOnly);
			genAffects(mob,me,showOnly);
			genDisposition(mob,me,showOnly);
			genSensesMask(mob,me,showOnly);
			if(me instanceof Rideable)
				genRideable(mob,(Rideable)me,showOnly);
			if(me instanceof Deity)
				genDeity(mob,(Deity)me,showOnly);
			ok=true;
			me.recoverCharStats();
			me.recoverMaxState();
			me.recoverEnvStats();
			me.resetToMaxState();
			if(me.text().length()>=maxLength)
			{
				mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
				ok=false;
			}
		}

		mob.tell("\n\rNow don't forget to equip him with stuff before saving!\n\r");
	}
	
	public static void modifyPlayer(MOB mob, MOB me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		if(me.isMonster())
			return;
		boolean ok=false;
		boolean showOnly=false;
		while((!ok)&&(!showOnly))
		{
			String oldName=me.ID();
			genName(mob,me,showOnly);
			while((!me.name().equals(oldName))&&(ExternalPlay.DBUserSearch(null,me.name())))
			{
				mob.tell("The name given cannot be chosen, as it is already being used.");
				genName(mob,me,showOnly);
			}

			genDescription(mob,me,showOnly);
			genLevel(mob,me,showOnly);
			genRace(mob,me,showOnly);
			genCharClass(mob,me,showOnly);
			genGender(mob,me,showOnly);
			genHeight(mob,me,showOnly);
			genWeight(mob,me,showOnly);
			genSpeed(mob,me,showOnly);
			genAttack(mob,me,showOnly);
			genDamage(mob,me,showOnly);
			genArmor(mob,me,showOnly);
			genHitPoints(mob,me,showOnly);
			genAlignment(mob,me,showOnly);
			genMoney(mob,me,showOnly);
			genAbilities(mob,me,showOnly);
			genDisposition(mob,me,showOnly);
			genSensesMask(mob,me,showOnly);
			if(me instanceof Rideable)
				genRideable(mob,(Rideable)me,showOnly);
			ok=true;
			me.recoverCharStats();
			me.recoverMaxState();
			me.recoverEnvStats();
			me.resetToMaxState();
			if(!oldName.equals(me.ID()))
			{
				MOB fakeMe=(MOB)me.copyOf();
				fakeMe.setName(oldName);
				ExternalPlay.DBDeleteMOB(fakeMe);
				ExternalPlay.DBCreateCharacter(me);
				ExternalPlay.DBUpdateMOB(me);
			}
		}
	}
	
	public static void modifyGenShopkeeper(MOB mob, ShopKeeper me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		boolean showOnly=false;
		while((!ok)&&(!showOnly))
		{
			genName(mob,me,showOnly);
			genDisplayText(mob,me,showOnly);
			genDescription(mob,me,showOnly);
			int oldLevel=me.baseEnvStats().level();
			genLevel(mob,me,showOnly);
			if((oldLevel==0)&&(me.baseEnvStats().level()>0))
				me.baseCharStats().getCurrentClass().buildMOB(me,me.baseEnvStats().level(),mob.getAlignment(),mob.baseEnvStats().weight(),mob.getWimpHitPoint(),(char)mob.baseCharStats().getStat(CharStats.GENDER));
			genRejuv(mob,me,showOnly);
			genRace(mob,me,showOnly);
			genHeight(mob,me,showOnly);
			genWeight(mob,me,showOnly);
			genGender(mob,me,showOnly);
			genSpeed(mob,me,showOnly);
			if((oldLevel==0)&&(me.baseEnvStats().level()>0))
				me.baseEnvStats().setDamage((int)Math.round(Util.div(me.baseEnvStats().damage(),me.baseEnvStats().speed())));
			genAttack(mob,me,showOnly);
			genDamage(mob,me,showOnly);
			genArmor(mob,me,showOnly);
			genHitPoints(mob,me,showOnly);
			genAlignment(mob,me,showOnly);
			genMoney(mob,me,showOnly);
			genAbilities(mob,me,showOnly);
			genBehaviors(mob,me,showOnly);
			genAffects(mob,me,showOnly);
			genShopkeeper(mob,me,showOnly);
			if(me instanceof Banker)
				genBanker(mob,(Banker)me,showOnly);
			genDisposition(mob,me,showOnly);
			genSensesMask(mob,me,showOnly);
			ok=true;
			me.recoverCharStats();
			me.recoverMaxState();
			me.recoverEnvStats();
			me.resetToMaxState();
			if(me.text().length()>=maxLength)
			{
				mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
				ok=false;
			}
		}

		mob.tell("\n\rNow don't forget to equip him with non-generic items before saving! If you DO add items to his list, be sure to come back here in case you've exceeded the string limit again.\n\r");
	}
}
