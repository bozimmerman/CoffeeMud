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
	// showNumber should always be a valid number no less than 1
	// showFlag should be a valid number for editing, or -1 for skipping

	static void genName(MOB mob, Environmental E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Name: '"+E.Name()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt("Enter a new one\n\r:","");
		if(newName.length()>0)
			E.setName(newName);
		else
			mob.tell("(no change)");
	}

	static void genArchivePath(MOB mob, Area E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Archive file name: '"+E.getArchivePath()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt("Enter a new one\n\r:","");
		if(newName.length()>0)
			E.setArchivePath(newName);
		else
			mob.tell("(no change)");
	}

	public static Room changeRoomType(Room R, Room newRoom)
	{
		if((R==null)||(newRoom==null)) return R;
		Room oldR=R;
		R=newRoom;
		for(int a=oldR.numAffects()-1;a>=0;a--)
		{
			Ability A=oldR.fetchAffect(a);
			if(A!=null)
			{
				A.unInvoke();
				oldR.delAffect(A);
			}
		}
		ExternalPlay.deleteTick(oldR,-1);
		CMMap.delRoom(oldR);
		CMMap.addRoom(R);
		R.setArea(oldR.getArea());
		R.setRoomID(oldR.roomID());
		for(int d=0;d<R.rawDoors().length;d++)
			R.rawDoors()[d]=oldR.rawDoors()[d];
		for(int d=0;d<R.rawExits().length;d++)
			R.rawExits()[d]=oldR.rawExits()[d];
		R.setDisplayText(oldR.displayText());
		R.setDescription(oldR.description());
		if(R instanceof GridLocale)
		{
			((GridLocale)R).setXSize(((GridLocale)oldR).xSize());
			((GridLocale)R).setYSize(((GridLocale)oldR).ySize());
			((GridLocale)R).clearGrid();
		}
		Vector allmobs=new Vector();
		int skip=0;
		while(oldR.numInhabitants()>(skip))
		{
			MOB M=oldR.fetchInhabitant(skip);
			if(M.isEligibleMonster())
			{
				if(!allmobs.contains(M))
					allmobs.addElement(M);
				oldR.delInhabitant(M);
			}
			else
			if(oldR!=R)
			{
				oldR.delInhabitant(M);
				R.bringMobHere(M,true);
			}
			else
				skip++;
		}
		Vector allitems=new Vector();
		while(oldR.numItems()>0)
		{
			Item I=oldR.fetchItem(0);
			if(!allitems.contains(I))
				allitems.addElement(I);
			oldR.delItem(I);
		}

		for(int i=0;i<allitems.size();i++)
		{
			Item I=(Item)allitems.elementAt(i);
			if(!R.isContent(I))
			{
				if(I.subjectToWearAndTear())
					I.setUsesRemaining(100);
				I.recoverEnvStats();
				R.addItem(I);
				R.recoverRoomStats();
			}
		}
		for(int m=0;m<allmobs.size();m++)
		{
			MOB M=(MOB)allmobs.elementAt(m);
			if(!R.isInhabitant(M))
			{
				MOB M2=(MOB)M.copyOf();
				M2.setStartRoom(R);
				M2.setLocation(R);
				M2.envStats().setRejuv(5000);
				M2.recoverCharStats();
				M2.recoverEnvStats();
				M2.recoverMaxState();
				M2.resetToMaxState();
				M2.bringToLife(R,true);
				R.recoverRoomStats();
				M.destroy();
			}
		}

		for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
		{
			Room R2=(Room)r.nextElement();
			for(int d=0;d<R2.rawDoors().length;d++)
				if(R2.rawDoors()[d]==oldR)
				{
					R2.rawDoors()[d]=R;
					if(R2 instanceof GridLocale)
						((GridLocale)R2).buildGrid();
				}
		}
		R.getArea().clearMap();
		R.getArea().fillInAreaRoom(R);
		ExternalPlay.DBUpdateRoom(R);
		ExternalPlay.DBUpdateMOBs(R);
		ExternalPlay.DBUpdateItems(R);
		R.startItemRejuv();
		return R;
	}

	static Room genRoomType(MOB mob, Room R, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return R;
		mob.tell(showNumber+". Type: '"+CMClass.className(R)+"'");
		if((showFlag!=showNumber)&&(showFlag>-999)) return R;
		String newName=mob.session().prompt("Enter a new one\n\r:","");
		if(newName.length()>0)
		{
			Room newRoom=CMClass.getLocale(newName);
			if(newRoom==null)
				mob.tell("'"+newName+"' does not exist. No Change.");
			else
			if(mob.session().confirm("This will change the room type of room '"+R.roomID()+"'.  Are you absolutely sure (y/N)? ","N"))
				R=changeRoomType(R,newRoom);
			R.recoverRoomStats();
		}
		else
			mob.tell("(no change)");
		return R;
	}

	static void genDescription(MOB mob, Environmental E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Description: '"+E.description()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt("Enter a new one\n\r:","");
		if(newName.length()>0)
			E.setDescription(newName);
		else
			mob.tell("(no change)");
	}

	public static void genDisplayText(MOB mob, Environmental E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Display: '"+E.displayText()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=null;
		if(E instanceof Item)
			newName=mob.session().prompt("Enter something new (null == blended)\n\r:","");
		else
		if(E instanceof Exit)
			newName=mob.session().prompt("Enter something new (null == see-through)\n\r:","");
		else
			newName=mob.session().prompt("Enter something new\n\r:","");
		if(newName.length()>0)
		{
			if(((E instanceof Item)||(E instanceof Exit))
			&&(newName.trim().equalsIgnoreCase("null")))
				newName="";
			E.setDisplayText(newName);
		}
		else
			mob.tell("(no change)");
		if((E instanceof Item)&&(E.displayText().length()==0))
			mob.tell("(blended)");
	}
	public static void genClosedText(MOB mob, Exit E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Closed Text: '"+E.closedText()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt("Enter something new\n\r:","");
		if(newName.equals("null"))
			E.setExitParams(E.doorName(),E.closeWord(),E.openWord(),"");
		else
		if(newName.length()>0)
			E.setExitParams(E.doorName(),E.closeWord(),E.openWord(),newName);
		else
			mob.tell("(no change)");
	}
	public static void genDoorName(MOB mob, Exit E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Door Name: '"+E.doorName()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt("Enter something new\n\r:","");
		if(newName.length()>0)
			E.setExitParams(newName,E.closeWord(),E.openWord(),E.closedText());
		else
			mob.tell("(no change)");
	}

	public static void genOpenWord(MOB mob, Exit E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Open Word: '"+E.openWord()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt("Enter something new\n\r:","");
		if(newName.length()>0)
			E.setExitParams(E.doorName(),E.closeWord(),newName,E.closedText());
		else
			mob.tell("(no change)");
	}

	public static void genSubOps(MOB mob, Area A, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		String newName="Q";
		while(newName.length()>0)
		{
			mob.tell(showNumber+". Area SubOperator user names: "+A.getSubOpList());
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
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

	public static void genCloseWord(MOB mob, Exit E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Close Word: '"+E.closeWord()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt("Enter something new\n\r:","");
		if(newName.length()>0)
			E.setExitParams(E.doorName(),newName,E.openWord(),E.closedText());
		else
			mob.tell("(no change)");
	}

	public static void genExitMisc(MOB mob, Exit E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		if(E.hasALock())
		{
			E.setReadable(false);
			mob.tell(showNumber+". Assigned Key Item: '"+E.keyName()+"'.");
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			String newName=mob.session().prompt("Enter something new\n\r:","");
			if(newName.length()>0)
				E.setKeyName(newName);
			else
				mob.tell("(no change)");
		}
		else
		{
			if((showFlag!=showNumber)&&(showFlag>-999))
			{
				if(!E.isReadable())
					mob.tell(showNumber+". Door not is readable.");
				else
					mob.tell(showNumber+". Door is readable: "+E.readableText());
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

	public static void genReadable1(MOB mob, Item E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;

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
		if((showFlag!=showNumber)&&(showFlag>-999))
			mob.tell(showNumber+". Item is readable: "+E.isReadable());
		else
			E.setReadable(genGenericPrompt(mob,showNumber+". Is this item readable",E.isReadable()));
	}

	public static void genReadable2(MOB mob, Item E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;

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
					mob.tell(showNumber+". Assigned Spell or Parameters: '"+E.readableText()+"'.");
					ok=true;
				}
				else
				if((E instanceof Scroll)
				||(E instanceof Pill)
				||(E instanceof Potion))
					mob.tell(showNumber+". Assigned Spell(s) ( ';' delimited)\n: '"+E.readableText()+"'.");
				else
				if(E instanceof Wand)
					mob.tell(showNumber+". Assigned Spell Name: '"+E.readableText()+"'.");
				else
				if(E instanceof Key)
				{
					mob.tell(showNumber+". Assigned Key Code: '"+E.readableText()+"'.");
					ok=true;
				}
				else
				if(E instanceof com.planet_ink.coffee_mud.interfaces.Map)
				{
					mob.tell(showNumber+". Assigned Map Area(s): '"+E.readableText()+"'.");
					ok=true;
				}
				else
				if(E instanceof Light)
				{
					mob.tell(showNumber+". Light duration (before burn out): '"+Util.s_int(E.readableText())+"'.");
					ok=true;
				}
				else
				{
					mob.tell(showNumber+". Assigned Read Text: '"+E.readableText()+"'.");
					ok=true;
				}

				if((showFlag!=showNumber)&&(showFlag>-999)) return;
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
								mob.tell("'"+newName+"' is not recognized.  Try '?'.");
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
									mob.tell("'"+spellName+"' is not recognized.  Try '?'.");
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
			mob.session().println(showNumber+". Current liquid type: "+EnvResource.RESOURCE_DESCS[((Drink)E).liquidType()&EnvResource.RESOURCE_MASK]);
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
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

	public static void genGettable(MOB mob, Item E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		if(E instanceof Potion)
			((Potion)E).setDrunk((Potion)E,false);

		String c="Q";
		while(!c.equals("\n"))
		{
			mob.session().println(showNumber+". A) Is Gettable   : "+E.isGettable());
			mob.session().println("    B) Is Droppable  : "+E.isDroppable());
			mob.session().println("    C) Is Removable  : "+E.isRemovable());
			mob.session().println("    D) Non-Locatable : "+(((E.baseEnvStats().sensesMask()&EnvStats.CAN_NOT_SEE)>0)?"true":"false"));
			if(E instanceof Weapon)
				mob.session().println("    E) Is Two-Handed : "+E.rawLogicalAnd());
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			c=mob.session().choose("Enter one to change, or ENTER when done: ","ABCDE\n","\n").toUpperCase();
			switch(Character.toUpperCase(c.charAt(0)))
			{
			case 'A': E.setGettable(!E.isGettable()); break;
			case 'B': E.setDroppable(!E.isDroppable()); break;
			case 'C': E.setRemovable(!E.isRemovable()); break;
			case 'D': if((E.baseEnvStats().sensesMask()&EnvStats.CAN_NOT_SEE)>0)
						  E.baseEnvStats().setSensesMask(E.baseEnvStats().sensesMask()-EnvStats.CAN_NOT_SEE);
					  else
						  E.baseEnvStats().setSensesMask(E.baseEnvStats().sensesMask()|EnvStats.CAN_NOT_SEE);
					  break;
			case 'E': if(E instanceof Weapon)
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

	public static void genDisposition(MOB mob, Environmental E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		int[] disps={EnvStats.IS_INVISIBLE,
					 EnvStats.IS_HIDDEN,
					 EnvStats.IS_NOT_SEEN,
					 EnvStats.IS_BONUS,
					 EnvStats.IS_GLOWING,
					 EnvStats.IS_FLYING,
					 EnvStats.IS_CLIMBING,
					 EnvStats.IS_SNEAKING,
					 EnvStats.IS_SWIMMING,
					 EnvStats.IS_EVIL,
					 EnvStats.IS_GOOD};
		if((showFlag!=showNumber)&&(showFlag>-999))
		{
			String[] briefs={"invisible",
							 "hide",
							 "unseen",
							 "magical",
							 "glowing",
							 "fly",
							 "climb",
							 "sneak",
							 "swimmer",
							 "evil",
							 "good"};
			StringBuffer buf=new StringBuffer(showNumber+". Dispositions: ");
			for(int i=0;i<disps.length;i++)
			{
				int mask=disps[i];
				if((E.baseEnvStats().disposition()&mask)!=0)
					buf.append(briefs[i]+" ");
			}
			mob.tell(buf.toString());
			return;
		}
		String c="Q";
		while(!c.equals("\n"))
		{
			char letter='A';
			for(int i=0;i<disps.length;i++)
			{
				int mask=disps[i];
				for(int num=0;num<EnvStats.dispositionsDesc.length;num++)
					if(mask==Util.pow(2,num))
					{
						mob.session().println("    "+letter+") "+Util.padRight(EnvStats.dispositionsDesc[num],20)+":"+((E.baseEnvStats().disposition()&mask)!=0));
						break;
					}
				letter++;
			}
			c=mob.session().choose("Enter one to change, or ENTER when done: ","ABCDEFGHI\n","\n").toUpperCase();
			letter='A';
			for(int i=0;i<disps.length;i++)
			{
				int mask=disps[i];
				if(letter==Character.toUpperCase(c.charAt(0)))
				{
					toggleDispositionMask(E,mask);
					break;
				}
				letter++;
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



	public static void genClimateType(MOB mob, Area A, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		String c="Q";
		while(!c.equals("\n"))
		{
			mob.session().println(""+showNumber+". Climate:");
			mob.session().println("    R) Wet and Rainy    : "+((A.climateType()&Area.CLIMASK_WET)>0));
			mob.session().println("    H) Excessively hot  : "+((A.climateType()&Area.CLIMASK_HOT)>0));
			mob.session().println("    C) Excessively cold : "+((A.climateType()&Area.CLIMASK_COLD)>0));
			mob.session().println("    W) Very windy       : "+((A.climateType()&Area.CLIMATE_WINDY)>0));
			mob.session().println("    D) Very dry         : "+((A.climateType()&Area.CLIMASK_DRY)>0));
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
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

	public static void genCharStats(MOB mob, MOB E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		if((showFlag!=showNumber)&&(showFlag>-999))
		{
			StringBuffer buf=new StringBuffer(showNumber+". Stats: ");
			for(int i=0;i<CharStats.NUM_BASE_STATS;i++)
				buf.append(CharStats.TRAITABBR1[i]+":"+E.baseCharStats().getStat(i)+" ");
			mob.tell(buf.toString());
			return;
		}
		String c="Q";
		while(!c.equals("\n"))
		{
			for(int i=0;i<CharStats.TRAITS.length;i++)
				if(i!=CharStats.GENDER)
					mob.session().println("    "+(char)((int)('A')+i)+") "+Util.padRight(CharStats.TRAITS[i],20)+":"+((E.baseCharStats().getStat(i))));
			c=mob.session().choose("Enter one to change, or ENTER when done: ","ABCDEFGHIJKLMNOPQRSTUVWXYZ\n","\n").toUpperCase();
			if((c.charAt(0)>='A')&&(c.charAt(0)<='Z'))
			{
				int num=(int)c.charAt(0)-'A';
				String newVal=mob.session().prompt("Enter new value for "+CharStats.TRAITS[num]+" ("+E.baseCharStats().getStat(num)+"): ","");
				if((Util.s_int(newVal)>0)&&(num!=CharStats.GENDER))
					E.baseCharStats().setStat(num,Util.s_int(newVal));
				else
					mob.tell("(no change)");
			}
		}
	}

	public static void genSensesMask(MOB mob, Environmental E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		int[] senses={EnvStats.CAN_SEE_DARK,
					  EnvStats.CAN_SEE_HIDDEN,
					  EnvStats.CAN_SEE_INVISIBLE,
					  EnvStats.CAN_SEE_SNEAKERS,
					  EnvStats.CAN_SEE_INFRARED,
					  EnvStats.CAN_SEE_GOOD,
					  EnvStats.CAN_SEE_EVIL,
					  EnvStats.CAN_SEE_BONUS,
					  EnvStats.CAN_NOT_SPEAK,
					  EnvStats.CAN_NOT_HEAR,
					  EnvStats.CAN_NOT_SEE};
		String[] briefs={"darkvision",
						 "hidden",
						 "invisible",
						 "sneakers",
						 "infrared",
						 "good",
						 "evil",
						 "magic",
						 "MUTE",
						 "DEAF",
						 "BLIND"};
		if((showFlag!=showNumber)&&(showFlag>-999))
		{
			StringBuffer buf=new StringBuffer(showNumber+". Senses: ");
			for(int i=0;i<senses.length;i++)
			{
				int mask=senses[i];
				if((E.baseEnvStats().sensesMask()&mask)!=0)
					buf.append(briefs[i]+" ");
			}
			mob.tell(buf.toString());
			return;
		}
		String c="Q";
		while(!c.equals("\n"))
		{
			char letter='A';
			for(int i=0;i<senses.length;i++)
			{
				int mask=senses[i];
				for(int num=0;num<EnvStats.sensesDesc.length;num++)
					if(mask==Util.pow(2,num))
					{
						mob.session().println("    "+letter+") "+Util.padRight(EnvStats.sensesDesc[num],20)+":"+((E.baseEnvStats().sensesMask()&mask)!=0));
						break;
					}
				letter++;
			}
			c=mob.session().choose("Enter one to change, or ENTER when done: ","ABCDEFGHIJK\n","\n").toUpperCase();
			letter='A';
			for(int i=0;i<senses.length;i++)
			{
				int mask=senses[i];
				if(letter==Character.toUpperCase(c.charAt(0)))
				{
					toggleSensesMask(E,mask);
					break;
				}
				letter++;
			}
		}
	}

	public static void genDoorsNLocks(MOB mob, Exit E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		boolean HasDoor=E.hasADoor();
		boolean Open=E.isOpen();
		boolean DefaultsClosed=E.defaultsClosed();
		boolean HasLock=E.hasALock();
		boolean Locked=E.isLocked();
		boolean DefaultsLocked=E.defaultsLocked();
		if((showFlag!=showNumber)&&(showFlag>-999)){
			mob.tell(showNumber+". Has a door: "+E.hasADoor()
					+"\n\r   Has a lock: "+E.hasALock()
					+"\n\r   Open ticks: "+E.openDelayTicks());
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


	public static void genLidsNLocks(MOB mob, Container E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		if((showFlag!=showNumber)&&(showFlag>-999)){
			mob.tell(showNumber+". Can contain : "+makeContainerTypes(E)
					+"\n\r   Has a lid   : "+E.hasALid()
					+"\n\r   Has a lock  : "+E.hasALock());
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
					mob.tell("Unknown type.  Try '?'.");
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

	public static void genLevel(MOB mob, Environmental E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		if(E.baseEnvStats().level()<0)
			E.baseEnvStats().setLevel(1);
		mob.tell(showNumber+". Level: '"+E.baseEnvStats().level()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.baseEnvStats().setLevel(getNumericData(mob,"Enter a new level\n\r:",E.baseEnvStats().level()));
	}

	public static void genRejuv(MOB mob, Environmental E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Rejuv Ticks: '"+E.baseEnvStats().rejuv()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String rlevel=mob.session().prompt("Enter new amount\n\r:","");
		int newLevel=Util.s_int(rlevel);
		if((newLevel>0)||(rlevel.trim().equals("0")))
		{
			E.baseEnvStats().setRejuv(newLevel);
			if(E.baseEnvStats().rejuv()==0)
			{
				E.baseEnvStats().setRejuv(Integer.MAX_VALUE);
				mob.tell(E.Name()+" will now never rejuvinate.");
			}
		}
		else
			mob.tell("(no change)");
	}

	public static void genUses(MOB mob, Item E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Uses Remaining: '"+E.usesRemaining()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.setUsesRemaining(getNumericData(mob,"Enter a new value\n\r:",E.usesRemaining()));
	}

	public static void genCondition(MOB mob, Item E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Condition: '"+E.usesRemaining()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
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
		if(E instanceof MusicalInstrument)
			modifyGenInstrument(mob,(MusicalInstrument)E);
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

	public static void genMiscText(MOB mob, Environmental E, int showNumber, int showFlag)
		throws IOException
	{
		if(E.isGeneric())
			genMiscSet(mob,E);
		else
		{
			if((showFlag>0)&&(showFlag!=showNumber)) return;
			mob.tell(showNumber+". Misc Text: '"+E.text()+"'.");
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
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

	public static void genTitleRoom(MOB mob, LandTitle E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Land plot ID: '"+E.landRoomID()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
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

	public static void genAbility(MOB mob, Environmental E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Magical Ability: '"+E.baseEnvStats().ability()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.baseEnvStats().setAbility(getNumericData(mob,"Enter a new value (0=no magic)\n\r:",E.baseEnvStats().ability()));
	}

	public static void genHitPoints(MOB mob, MOB E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Hit Points/Level Modifier (hp=((level*level) + (random*level*THIS))) : '"+E.baseEnvStats().ability()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newLevelStr=mob.session().prompt("Enter a new value\n\r:","");
		int newLevel=Util.s_int(newLevelStr);
		if((newLevel!=0)||(newLevelStr.equals("0")))
			E.baseEnvStats().setAbility(newLevel);
		else
			mob.tell("(no change)");
	}

	public static void genValue(MOB mob, Item E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Base Value: '"+E.baseGoldValue()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.setBaseValue(getNumericData(mob,"Enter a new value\n\r:",E.baseGoldValue()));
	}

	public static void genWeight(MOB mob, Environmental E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Weight: '"+E.baseEnvStats().weight()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.baseEnvStats().setWeight(getNumericData(mob,"Enter a new weight\n\r:",E.baseEnvStats().weight()));
	}


	public static void genHeight(MOB mob, Environmental E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Height: '"+E.baseEnvStats().height()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.baseEnvStats().setHeight(getNumericData(mob,"Enter a new height\n\r:",E.baseEnvStats().height()));
	}


	public static void genSize(MOB mob, Armor E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Size: '"+E.baseEnvStats().height()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.baseEnvStats().setHeight(getNumericData(mob,"Enter a new size\n\r:",E.baseEnvStats().height()));
	}


	public static void genCapacity(MOB mob, Container E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Capacity: '"+E.capacity()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.setCapacity(getNumericData(mob,"Enter a new capacity\n\r:",E.capacity()));
	}

	public static void genAttack(MOB mob, Environmental E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". AttackAdjustment: '"+E.baseEnvStats().attackAdjustment()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.baseEnvStats().setAttackAdjustment(getNumericData(mob,"Enter a new value\n\r:",E.baseEnvStats().attackAdjustment()));
	}

	public static void genDamage(MOB mob, Environmental E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Damage/Hit: '"+E.baseEnvStats().damage()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.baseEnvStats().setDamage(getNumericData(mob,"Enter a new value\n\r:",E.baseEnvStats().damage()));
	}

	public static void genBanker1(MOB mob, Banker E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Coin Interest: '"+E.getCoinInterest()+"'% per real day.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.setCoinInterest(getDoubleData(mob,"Enter a new value\n\r:",E.getCoinInterest()));
	}
	public static void genBanker2(MOB mob, Banker E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Item Interest: '"+E.getItemInterest()+"'% per real day.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.setItemInterest(getDoubleData(mob,"Enter a new value\n\r:",E.getItemInterest()));
	}
	public static void genBanker3(MOB mob, Banker E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Bank Chain   : '"+E.bankChain()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newValue=mob.session().prompt("Enter a new chain\n\r:","");
		if(newValue.length()>0)
			E.setBankChain(newValue);
		else
			mob.tell("(no change)");
	}

	public static void genSpeed(MOB mob, Environmental E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Attacks/Round: '"+((int)Math.round(E.baseEnvStats().speed()))+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.baseEnvStats().setSpeed(getNumericData(mob,"Enter a new value\n\r:",(int)Math.round(E.baseEnvStats().speed())));
	}

	public static void genArmor(MOB mob, Environmental E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		if(E instanceof MOB)
			mob.tell(showNumber+". Armor (lower-better): '"+E.baseEnvStats().armor()+"'.");
		else
			mob.tell(showNumber+". Armor (higher-better): '"+E.baseEnvStats().armor()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.baseEnvStats().setArmor(getNumericData(mob,"Enter a new value\n\r:",E.baseEnvStats().armor()));
	}

	public static void genMoney(MOB mob, MOB E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Money: '"+E.getMoney()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.setMoney(getNumericData(mob,"Enter a new value\n\r:",E.getMoney()));
	}

	public static void genWeaponAmmo(MOB mob, Weapon E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		String defaultAmmo=(E.requiresAmmunition())?"Y":"N";
		if((showFlag!=showNumber)&&(showFlag>-999))
		{
			mob.tell(showNumber+". Ammo required: "+(E.requiresAmmunition()?E.ammunitionType():"NO"));
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
	public static void genWeaponRanges(MOB mob, Weapon E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Minimum/Maximum Ranges: "+((int)Math.round(E.minRange()))+"/"+((int)Math.round(E.maxRange()))+".");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
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

	public static void genWeaponType(MOB mob, Weapon E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Weapon Attack Type: '"+Weapon.typeDescription[E.weaponType()]+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
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

	public static void genTechLevel(MOB mob, Area A, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Technology Level: '"+Area.TECH_DESCS[A.getTechLevel()]+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		boolean q=false;
		while(!q)
		{
			String newType=mob.session().prompt("Enter a new level (?)\n\r:",Area.TECH_DESCS[A.getTechLevel()]);
			if(newType.equals("?"))
			{
				StringBuffer say=new StringBuffer("");
				for(int i=0;i<Area.TECH_DESCS.length;i++)
					say.append(Area.TECH_DESCS[i]+", ");
				mob.tell(say.toString().substring(0,say.length()-2));
				q=false;
			}
			else
			{
				q=true;
				int newValue=-1;
				for(int i=0;i<Area.TECH_DESCS.length;i++)
					if(newType.equalsIgnoreCase(Area.TECH_DESCS[i]))
						newValue=i;
				if(newValue>=0)
					A.setTechLevel(newValue);
				else
					mob.tell("(no change)");
			}
		}
	}


	public static void genMaterialCode(MOB mob, Item E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Material Type: '"+EnvResource.RESOURCE_DESCS[E.material()&EnvResource.RESOURCE_MASK]+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
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

	public static void genInstrumentType(MOB mob, MusicalInstrument E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Instrument Type: '"+MusicalInstrument.TYPE_DESC[E.instrumentType()]+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		boolean q=false;
		while(!q)
		{
			String newType=mob.session().prompt("Enter a new type (?)\n\r:",MusicalInstrument.TYPE_DESC[E.instrumentType()]);
			if(newType.equals("?"))
			{
				StringBuffer say=new StringBuffer("");
				for(int i=0;i<MusicalInstrument.TYPE_DESC.length-1;i++)
					say.append(MusicalInstrument.TYPE_DESC[i]+", ");
				mob.tell(say.toString().substring(0,say.length()-2));
				q=false;
			}
			else
			{
				q=true;
				int newValue=-1;
				for(int i=0;i<MusicalInstrument.TYPE_DESC.length-1;i++)
					if(newType.equalsIgnoreCase(MusicalInstrument.TYPE_DESC[i]))
						newValue=i;
				if(newValue>=0)
					E.setInstrumentType(newValue);
				else
					mob.tell("(no change)");
			}
		}
	}

	public static void genAlignment(MOB mob, MOB E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Alignment: '"+CommonStrings.alignmentStr(E.getAlignment())+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
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

	public static void genGender(MOB mob, MOB E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Gender: '"+Character.toUpperCase((char)E.baseCharStats().getStat(CharStats.GENDER))+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
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

	public static void genWeaponClassification(MOB mob, Weapon E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Weapon Classification: '"+Weapon.classifictionDescription[E.weaponClassification()]+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
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

	public static void genSecretIdentity(MOB mob, Item E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Secret Identity: '"+E.rawSecretIdentity()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newValue=mob.session().prompt("Enter a new identity\n\r:","");
		if(newValue.length()>0)
			E.setSecretIdentity(newValue);
		else
			mob.tell("(no change)");
	}

	public static void genNourishment(MOB mob, Food E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Nourishment/Eat: '"+E.nourishment()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		int newValue=Util.s_int(mob.session().prompt("Enter a new amount\n\r:",""));
		if(newValue>0)
			E.setNourishment(newValue);
		else
			mob.tell("(no change)");
	}

	public static void genRace(MOB mob, MOB E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		String raceID="begin!";
		while(raceID.length()>0)
		{
			mob.tell(showNumber+". Race: '"+E.baseCharStats().getMyRace().ID()+"'.");
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
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
					E.baseCharStats().getMyRace().setHeightWeight(E.baseEnvStats(),(char)E.baseCharStats().getStat(CharStats.GENDER));
				}
				else
					mob.tell("Unknown race! Try '?'.");
			}
		}
	}

	public static void genCharClass(MOB mob, MOB E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		String classID="begin!";
		while(classID.length()>0)
		{
			StringBuffer str=new StringBuffer("");
			for(int c=0;c<E.baseCharStats().numClasses();c++)
			{
				CharClass C=E.baseCharStats().getMyClass(c);
				str.append(C.ID()+"("+E.baseCharStats().getClassLevel(C)+") ");
			}
			mob.tell(showNumber+". Class: '"+str.toString()+"'.");
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			classID=mob.session().prompt("Enter a class to add/remove(?)\n\r:","").trim();
			if(classID.equalsIgnoreCase("?"))
				mob.tell(Lister.reallyList(CMClass.charClasses(),-1).toString());
			else
			if(classID.length()==0)
				mob.tell("(no change)");
			else
			{
				CharClass C=CMClass.getCharClass(classID);
				if(C!=null)
				{
					if(E.baseCharStats().getClassLevel(C)>=0)
					{
						if(E.baseCharStats().numClasses()<2)
							mob.tell("Final class may not be removed.  To change a class, add the new one first.");
						else
						{
							StringBuffer charClasses=new StringBuffer("");
							StringBuffer classLevels=new StringBuffer("");
							for(int c=0;c<E.baseCharStats().numClasses();c++)
							{
								CharClass C2=E.baseCharStats().getMyClass(c);
								int L2=E.baseCharStats().getClassLevel(C2);
								if(C2!=C)
								{
									charClasses.append(";"+C2.ID());
									classLevels.append(";"+L2);
								}
							}
							E.baseCharStats().setMyClasses(charClasses.toString());
							E.baseCharStats().setMyLevels(classLevels.toString());
						}
					}
					else
					{
						int highLvl=Integer.MIN_VALUE;
						CharClass highestC=null;
						for(int c=0;c<E.baseCharStats().numClasses();c++)
						{
							CharClass C2=E.baseCharStats().getMyClass(c);
							if(E.baseCharStats().getClassLevel(C2)>highLvl)
							{
								highestC=C2;
								highLvl=E.baseCharStats().getClassLevel(C2);
							}
						}
						E.baseCharStats().setCurrentClass(C);
						int levels=E.baseCharStats().combinedSubLevels();
						levels=E.baseEnvStats().level()-levels;
						String lvl=null;
						if(levels>0)
						{
							lvl=mob.session().prompt("Levels to give this class ("+levels+")\n\r:",""+levels).trim();
							int lvl2=Util.s_int(lvl);
							if(lvl2>levels) lvl2=levels;
							E.baseCharStats().setClassLevel(C,lvl2);
						}
						else
						{
							lvl=mob.session().prompt("Levels to siphon from "+highestC.ID()+" for this class (0)\n\r:",""+0).trim();
							int lvl2=Util.s_int(lvl);
							if(lvl2>highLvl) lvl2=highLvl;
							E.baseCharStats().setClassLevel(highestC,highLvl-lvl2);
							E.baseCharStats().setClassLevel(C,lvl2);
						}
						
					}
					int levels=E.baseCharStats().combinedSubLevels();
					levels=E.baseEnvStats().level()-levels;
					C=E.baseCharStats().getCurrentClass();
					E.baseCharStats().setClassLevel(C,levels);
				}
				else
					mob.tell("Unknown character class! Try '?'.");
			}
		}
	}

	public static void genBehaviors(MOB mob, Environmental E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
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
			mob.tell(showNumber+". Behaviors: '"+behaviorstr+"'.");
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
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
							mob.tell("'"+behave+"' is not recognized.  Try '?'.");
						}
					}
				}
			}
			else
				mob.tell("(no change)");
		}
	}

	public static void genAffects(MOB mob, Environmental E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
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
			mob.tell(showNumber+". Affects: '"+affectstr+"'.");
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
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
							mob.tell("'"+behave+"' is not recognized.  Try '?'.");
						}
					}
				}
			}
			else
				mob.tell("(no change)");
		}
	}

	public static void genRideable1(MOB mob, Rideable R, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Rideable Type: '"+Rideable.RIDEABLE_DESCS[R.rideBasis()]+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		boolean q=false;
		String sel="LWACBTEDG";
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
	}
	public static void genRideable2(MOB mob, Rideable R, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Number of MOBs held: '"+R.riderCapacity()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newLevelStr=mob.session().prompt("Enter a new value: ","");
		int newLevel=Util.s_int(newLevelStr);
		if(newLevel>0)
			R.setRiderCapacity(newLevel);
		else
			mob.tell("(no change)");
	}

	public static void genShopkeeper1(MOB mob, ShopKeeper E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Shopkeeper type: '"+E.storeKeeperString()+"'.");
		StringBuffer buf=new StringBuffer("");
		StringBuffer codes=new StringBuffer("");
		String codeStr="0123456789ABCDEFGHIJKLMNOP";
		if(E instanceof Banker)
		{
			int r=ShopKeeper.DEAL_BANKER;
			char c=codeStr.charAt(r);
			codes.append(c);
			buf.append(c+") "+ShopKeeper.SOLDCODES[r]+"\n\r");
			r=ShopKeeper.DEAL_CLANBANKER;
			c=codeStr.charAt(r);
			codes.append(c);
			buf.append(c+") "+ShopKeeper.SOLDCODES[r]+"\n\r");
		}
		else
		for(int r=0;r<ShopKeeper.SOLDCODES.length;r++)
		{
			if((r!=ShopKeeper.DEAL_CLANBANKER)&&(r!=ShopKeeper.DEAL_BANKER))
			{
				char c=codeStr.charAt(r);
				codes.append(c);
				buf.append(c+") "+ShopKeeper.SOLDCODES[r]+"\n\r");
			}
		}
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
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

	public static void genShopkeeper2(MOB mob, ShopKeeper E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		String itemstr="NO";
		while(itemstr.length()>0)
		{
			String inventorystr="";
			Vector V=E.getUniqueStoreInventory();
			for(int b=0;b<V.size();b++)
			{
				Environmental E2=(Environmental)V.elementAt(b);
				if(E2.isGeneric())
					inventorystr+=E2.name()+" ("+E.numberInStock(E2)+"), ";
				else
					inventorystr+=CMClass.className(E2)+" ("+E.numberInStock(E2)+"), ";
			}
			if(inventorystr.length()>0)
				inventorystr=inventorystr.substring(0,inventorystr.length()-2);
			mob.tell(showNumber+". Inventory: '"+inventorystr+"'.");
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
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
					mob.tell("* Plus! Any items on the ground.");
					mob.tell("* Plus! Any mobs hanging around in the room.");
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
						if((item==null)&&(mob.location()!=null))
						{
							Room R=mob.location();
							item=R.fetchItem(null,itemstr);
							if(item==null)
							{	
								item=R.fetchInhabitant(itemstr);
								if((item instanceof MOB)&&(!((MOB)item).isMonster()))
									item=null;
							}
						}
						if(item!=null)
						{
							item=item.copyOf();
							item.recoverEnvStats();
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

								if(E.doIHaveThisInStock(item.Name(),null))
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
							mob.tell("'"+itemstr+"' is not recognized.  Try '?'.");
						}
					}
				}
			}
			else
				mob.tell("(no change)");
		}
	}
	public static void genShopkeeper3(MOB mob, ShopKeeper E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Prejudice: '"+E.prejudiceFactors()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newValue=mob.session().prompt("Enter a new string\n\r:","");
		if(newValue.length()>0)
			E.setPrejudiceFactors(newValue);
		else
			mob.tell("(no change)");
	}

	public static void genAbilities(MOB mob, MOB E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
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
			mob.tell(showNumber+". Abilities: '"+abilitiestr+"'.");
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
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
								chosenOne=(Ability)chosenOne.copyOf();
								E.addAbility(chosenOne);
								chosenOne.setProfficiency(50);
								chosenOne.autoInvocation(mob);
							}
						}
						else
						{
							mob.tell("'"+behave+"' is not recognized.  Try '?'.");
						}
					}
				}
			}
			else
				mob.tell("(no change)");
		}
	}

	public static void genDeity1(MOB mob, Deity E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Cleric Requirements: '"+E.getClericRequirements()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newValue=mob.session().prompt("Enter new requirements\n\r:","");
		if(newValue.length()>0)
			E.setClericRequirements(newValue);
		else
			mob.tell("(no change)");
	}
	public static void genDeity2(MOB mob, Deity E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Cleric Ritual: '"+E.getClericRitual()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newValue=mob.session().prompt("Enter new ritual\n\r:","");
		if(newValue.length()>0)
			E.setClericRitual(newValue);
		else
			mob.tell("(no change)");
	}
	public static void genDeity3(MOB mob, Deity E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Worshiper Requirements: '"+E.getWorshipRequirements()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newValue=mob.session().prompt("Enter new requirements\n\r:","");
		if(newValue.length()>0)
			E.setWorshipRequirements(newValue);
		else
			mob.tell("(no change)");
	}
	public static void genDeity4(MOB mob, Deity E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Worshiper Ritual: '"+E.getWorshipRitual()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newValue=mob.session().prompt("Enter new ritual\n\r:","");
		if(newValue.length()>0)
			E.setWorshipRitual(newValue);
		else
			mob.tell("(no change)");
	}
	public static void genDeity5(MOB mob, Deity E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
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
			mob.tell(showNumber+". Blessings: '"+abilitiestr+"'.");
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
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
							mob.tell("'"+behave+"' is not recognized.  Try '?'.");
						}
					}
				}
			}
			else
				mob.tell("(no change)");
		}
	}

	public static void genDeity6(MOB mob, Deity E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		String behave="NO";
		while(behave.length()>0)
		{
			String abilitiestr="";
			for(int a=0;a<E.numCurses();a++)
			{
				Ability A=E.fetchCurse(a);
				if((A!=null)&&(!A.isBorrowed(E)))
					abilitiestr+=A.ID()+", ";
			}
			if(abilitiestr.length()>0)
				abilitiestr=abilitiestr.substring(0,abilitiestr.length()-2);
			mob.tell(showNumber+". Curses: '"+abilitiestr+"'.");
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			behave=mob.session().prompt("Enter an ability to add/remove (?)\n\r:","");
			if(behave.length()>0)
			{
				if(behave.equalsIgnoreCase("?"))
					mob.tell(Lister.reallyList(CMClass.abilities(),-1).toString());
				else
				{
					Ability chosenOne=null;
					for(int a=0;a<E.numCurses();a++)
					{
						Ability A=E.fetchCurse(a);
						if((A!=null)&&(A.ID().equalsIgnoreCase(behave)))
							chosenOne=A;
					}
					if(chosenOne!=null)
					{
						mob.tell(chosenOne.ID()+" removed.");
						E.delCurse(chosenOne);
					}
					else
					{
						chosenOne=(Ability)CMClass.getAbility(behave);
						if(chosenOne!=null)
						{
							boolean alreadyHasIt=false;
							for(int a=0;a<E.numCurses();a++)
							{
								Ability A=E.fetchCurse(a);
								if((A!=null)&&(A.ID().equals(chosenOne.ID())))
									alreadyHasIt=true;
							}
							if(!alreadyHasIt)
								mob.tell(chosenOne.ID()+" added.");
							else
								mob.tell(chosenOne.ID()+" re-added.");
							if(!alreadyHasIt)
								E.addCurse((Ability)chosenOne.copyOf());
						}
						else
						{
							mob.tell("'"+behave+"' is not recognized.  Try '?'.");
						}
					}
				}
			}
			else
				mob.tell("(no change)");
		}
	}

	public static void genDeity7(MOB mob, Deity E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		String behave="NO";
		while(behave.length()>0)
		{
			String abilitiestr="";
			for(int a=0;a<E.numPowers();a++)
			{
				Ability A=E.fetchPower(a);
				if((A!=null)&&(!A.isBorrowed(E)))
					abilitiestr+=A.ID()+", ";
			}
			if(abilitiestr.length()>0)
				abilitiestr=abilitiestr.substring(0,abilitiestr.length()-2);
			mob.tell(showNumber+". Granted Powers: '"+abilitiestr+"'.");
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			behave=mob.session().prompt("Enter an ability to add/remove (?)\n\r:","");
			if(behave.length()>0)
			{
				if(behave.equalsIgnoreCase("?"))
					mob.tell(Lister.reallyList(CMClass.abilities(),-1).toString());
				else
				{
					Ability chosenOne=null;
					for(int a=0;a<E.numPowers();a++)
					{
						Ability A=E.fetchPower(a);
						if((A!=null)&&(A.ID().equalsIgnoreCase(behave)))
							chosenOne=A;
					}
					if(chosenOne!=null)
					{
						mob.tell(chosenOne.ID()+" removed.");
						E.delPower(chosenOne);
					}
					else
					{
						chosenOne=(Ability)CMClass.getAbility(behave);
						if(chosenOne!=null)
						{
							boolean alreadyHasIt=false;
							for(int a=0;a<E.numPowers();a++)
							{
								Ability A=E.fetchPower(a);
								if((A!=null)&&(A.ID().equals(chosenOne.ID())))
									alreadyHasIt=true;
							}
							if(!alreadyHasIt)
								mob.tell(chosenOne.ID()+" added.");
							else
								mob.tell(chosenOne.ID()+" re-added.");
							if(!alreadyHasIt)
								E.addPower((Ability)chosenOne.copyOf());
						}
						else
						{
							mob.tell("'"+behave+"' is not recognized.  Try '?'.");
						}
					}
				}
			}
			else
				mob.tell("(no change)");
		}
	}
	public static void genDeity8(MOB mob, Deity E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Cleric Sin: '"+E.getClericSin()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newValue=mob.session().prompt("Enter new sin ritual\n\r:","");
		if(newValue.length()>0)
			E.setClericSin(newValue);
		else
			mob.tell("(no change)");
	}
	public static void genDeity9(MOB mob, Deity E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Worshiper Sin: '"+E.getWorshipSin()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newValue=mob.session().prompt("Enter new sin ritual\n\r:","");
		if(newValue.length()>0)
			E.setWorshipSin(newValue);
		else
			mob.tell("(no change)");
	}
	public static void genDeity0(MOB mob, Deity E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Cleric Power Ritual: '"+E.getClericPowerup()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newValue=mob.session().prompt("Enter new power ritual\n\r:","");
		if(newValue.length()>0)
			E.setClericPowerup(newValue);
		else
			mob.tell("(no change)");
	}
	public static void genGridLocaleX(MOB mob, GridLocale E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Size (X): '"+E.xSize()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newValue=mob.session().prompt("Enter a new size\n\r:","");
		if(Util.s_int(newValue)>0)
			E.setXSize(Util.s_int(newValue));
		else
			mob.tell("(no change)");
	}

	public static void genGridLocaleY(MOB mob, GridLocale E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Size (Y): '"+E.ySize()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newValue=mob.session().prompt("Enter a new size\n\r:","");
		if(Util.s_int(newValue)>0)
			E.setYSize(Util.s_int(newValue));
		else
			mob.tell("(no change)");
	}

	public static void genWornLocation(MOB mob, Item E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		if((showFlag!=showNumber)&&(showFlag>-999))
		{
			StringBuffer buf=new StringBuffer(showNumber+". ");
			if(!E.rawLogicalAnd())
				buf.append("Wear on any one of: ");
			else
				buf.append("Worn on all of: ");
			for(int l=0;l<18;l++)
			{
				int wornCode=1<<l;
				if((Sense.wornLocation(wornCode).length()>0)
				&&(((E.rawProperLocationBitmap()&wornCode)==wornCode)))
					buf.append(Sense.wornLocation(wornCode)+" ");
			}
			mob.tell(buf.toString());
			return;
		}
		int codeVal=-1;
		while(codeVal!=0)
		{
			mob.tell("Wearing parameters\n\r0: Done");
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

	public static void genThirstQuenched(MOB mob, Drink E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Quenched/Drink: '"+E.thirstQuenched()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.setThirstQuenched(getNumericData(mob,"Enter a new amount\n\r:",E.thirstQuenched()));
	}

	public static void genDrinkHeld(MOB mob, Drink E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Amount of Drink Held: '"+E.liquidHeld()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		E.setLiquidHeld(getNumericData(mob,"Enter a new amount\n\r:",E.liquidHeld()));
		E.setLiquidRemaining(E.liquidHeld());
	}



	public static void modifyGenItem(MOB mob, Item me)
		throws IOException
	{
		boolean ok=false;
		int showFlag=-1;
		if(CommonStrings.getIntVar(CommonStrings.SYSTEMI_EDITORTYPE)>0)
			showFlag=-999;
		while(!ok)
		{
			if(mob.isMonster())	return;
			int showNumber=0;
			genName(mob,me,++showNumber,showFlag);
			genDisplayText(mob,me,++showNumber,showFlag);
			genDescription(mob,me,++showNumber,showFlag);
			genLevel(mob,me,++showNumber,showFlag);
			genSecretIdentity(mob,me,++showNumber,showFlag);
			genMaterialCode(mob,me,++showNumber,showFlag);
			genGettable(mob,me,++showNumber,showFlag);
			genReadable1(mob,me,++showNumber,showFlag);
			genReadable2(mob,me,++showNumber,showFlag);
			genRejuv(mob,me,++showNumber,showFlag);
			genAbility(mob,me,++showNumber,showFlag);
			genUses(mob,me,++showNumber,showFlag);
			genValue(mob,me,++showNumber,showFlag);
			genWeight(mob,me,++showNumber,showFlag);
			genDisposition(mob,me,++showNumber,showFlag);
			genBehaviors(mob,me,++showNumber,showFlag);
			genAffects(mob,me,++showNumber,showFlag);
			if(me instanceof LandTitle)
				genTitleRoom(mob,(LandTitle)me,++showNumber,showFlag);
			if(showFlag<-900){ ok=true; break;}
			if(showFlag>0){ showFlag=-1; continue;}
			showFlag=Util.s_int(mob.session().prompt("Edit which? ",""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
				me.recoverEnvStats();
				if(me.text().length()>=maxLength)
				{
					mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
					ok=false;
				}
			}
		}
	}

	public static void modifyGenFood(MOB mob, Food me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		int showFlag=-1;
		if(CommonStrings.getIntVar(CommonStrings.SYSTEMI_EDITORTYPE)>0)
			showFlag=-999;
		while(!ok)
		{
			int showNumber=0;
			genName(mob,me,++showNumber,showFlag);
			genDisplayText(mob,me,++showNumber,showFlag);
			genDescription(mob,me,++showNumber,showFlag);
			genSecretIdentity(mob,me,++showNumber,showFlag);
			genLevel(mob,me,++showNumber,showFlag);
			genValue(mob,me,++showNumber,showFlag);
			genRejuv(mob,me,++showNumber,showFlag);
			genWeight(mob,me,++showNumber,showFlag);
			genMaterialCode(mob,me,++showNumber,showFlag);
			genNourishment(mob,me,++showNumber,showFlag);
			genDisposition(mob,me,++showNumber,showFlag);
			genGettable(mob,me,++showNumber,showFlag);
			genReadable1(mob,me,++showNumber,showFlag);
			genReadable2(mob,me,++showNumber,showFlag);
			genBehaviors(mob,me,++showNumber,showFlag);
			genAffects(mob,me,++showNumber,showFlag);
			if(showFlag<-900){ ok=true; break;}
			if(showFlag>0){ showFlag=-1; continue;}
			showFlag=Util.s_int(mob.session().prompt("Edit which? ",""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
				me.recoverEnvStats();
				if(me.text().length()>=maxLength)
				{
					mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
					ok=false;
				}
			}
		}
	}
	public static void modifyGenDrink(MOB mob, Drink me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		int showFlag=-1;
		if(CommonStrings.getIntVar(CommonStrings.SYSTEMI_EDITORTYPE)>0)
			showFlag=-999;
		while(!ok)
		{
			int showNumber=0;
			genName(mob,me,++showNumber,showFlag);
			genDisplayText(mob,me,++showNumber,showFlag);
			genDescription(mob,me,++showNumber,showFlag);
			genSecretIdentity(mob,(Item)me,++showNumber,showFlag);
			genValue(mob,(Item)me,++showNumber,showFlag);
			genLevel(mob,me,++showNumber,showFlag);
			genWeight(mob,me,++showNumber,showFlag);
			genRejuv(mob,me,++showNumber,showFlag);
			genThirstQuenched(mob,me,++showNumber,showFlag);
			genMaterialCode(mob,(Item)me,++showNumber,showFlag);
			genDrinkHeld(mob,me,++showNumber,showFlag);
			genGettable(mob,(Item)me,++showNumber,showFlag);
			genReadable1(mob,(Item)me,++showNumber,showFlag);
			genReadable2(mob,(Item)me,++showNumber,showFlag);
			genBehaviors(mob,me,++showNumber,showFlag);
			genAffects(mob,me,++showNumber,showFlag);
			genDisposition(mob,me,++showNumber,showFlag);
			if(me instanceof Container)
				genCapacity(mob,(Container)me,++showNumber,showFlag);
			if(showFlag<-900){ ok=true; break;}
			if(showFlag>0){ showFlag=-1; continue;}
			showFlag=Util.s_int(mob.session().prompt("Edit which? ",""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
				me.recoverEnvStats();
				if(me.text().length()>=maxLength)
				{
					mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
					ok=false;
				}
			}
		}
	}

	public static void modifyGenWallpaper(MOB mob, Item me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		int showFlag=-1;
		if(CommonStrings.getIntVar(CommonStrings.SYSTEMI_EDITORTYPE)>0)
			showFlag=-999;
		while(!ok)
		{
			int showNumber=0;
			genName(mob,me,++showNumber,showFlag);
			genDescription(mob,me,++showNumber,showFlag);
			genReadable1(mob,me,++showNumber,showFlag);
			genReadable2(mob,me,++showNumber,showFlag);
			if(showFlag<-900){ ok=true; break;}
			if(showFlag>0){ showFlag=-1; continue;}
			showFlag=Util.s_int(mob.session().prompt("Edit which? ",""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
				me.recoverEnvStats();
				if(me.text().length()>=maxLength)
				{
					mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
					ok=false;
				}
			}
		}
	}

	public static void modifyGenMap(MOB mob, com.planet_ink.coffee_mud.interfaces.Map me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		int showFlag=-1;
		if(CommonStrings.getIntVar(CommonStrings.SYSTEMI_EDITORTYPE)>0)
			showFlag=-999;
		while(!ok)
		{
			int showNumber=0;
			genName(mob,me,++showNumber,showFlag);
			genDisplayText(mob,me,++showNumber,showFlag);
			genDescription(mob,me,++showNumber,showFlag);
			genLevel(mob,me,++showNumber,showFlag);
			genSecretIdentity(mob,me,++showNumber,showFlag);
			genGettable(mob,me,++showNumber,showFlag);
			genReadable1(mob,me,++showNumber,showFlag);
			genReadable2(mob,me,++showNumber,showFlag);
			genValue(mob,me,++showNumber,showFlag);
			genWeight(mob,me,++showNumber,showFlag);
			genRejuv(mob,me,++showNumber,showFlag);
			genMaterialCode(mob,me,++showNumber,showFlag);
			genDisposition(mob,me,++showNumber,showFlag);
			genBehaviors(mob,me,++showNumber,showFlag);
			genAffects(mob,me,++showNumber,showFlag);
			if(showFlag<-900){ ok=true; break;}
			if(showFlag>0){ showFlag=-1; continue;}
			showFlag=Util.s_int(mob.session().prompt("Edit which? ",""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
				me.recoverEnvStats();
				if(me.text().length()>=maxLength)
				{
					mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
					ok=false;
				}
			}
		}
	}

	public static void modifyGenContainer(MOB mob, Container me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		int showFlag=-1;
		if(CommonStrings.getIntVar(CommonStrings.SYSTEMI_EDITORTYPE)>0)
			showFlag=-999;
		while(!ok)
		{
			int showNumber=0;
			genName(mob,me,++showNumber,showFlag);
			genDisplayText(mob,me,++showNumber,showFlag);
			genDescription(mob,me,++showNumber,showFlag);
			genLevel(mob,me,++showNumber,showFlag);
			genRejuv(mob,me,++showNumber,showFlag);
			genCapacity(mob,me,++showNumber,showFlag);
			genLidsNLocks(mob,me,++showNumber,showFlag);
			genMaterialCode(mob,me,++showNumber,showFlag);
			genSecretIdentity(mob,me,++showNumber,showFlag);
			genValue(mob,me,++showNumber,showFlag);
			genUses(mob,me,++showNumber,showFlag);
			genWeight(mob,me,++showNumber,showFlag);
			genDisposition(mob,me,++showNumber,showFlag);
			genGettable(mob,me,++showNumber,showFlag);
			genReadable1(mob,me,++showNumber,showFlag);
			genReadable2(mob,me,++showNumber,showFlag);
			genBehaviors(mob,me,++showNumber,showFlag);
			genAffects(mob,me,++showNumber,showFlag);
			if(me instanceof Rideable)
			{
				genRideable1(mob,(Rideable)me,++showNumber,showFlag);
				genRideable2(mob,(Rideable)me,++showNumber,showFlag);
			}
			if(showFlag<-900){ ok=true; break;}
			if(showFlag>0){ showFlag=-1; continue;}
			showFlag=Util.s_int(mob.session().prompt("Edit which? ",""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
				me.recoverEnvStats();
				if(me.text().length()>=maxLength)
				{
					mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
					ok=false;
				}
			}
		}
	}

	public static void modifyGenWeapon(MOB mob, Weapon me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		int showFlag=-1;
		if(CommonStrings.getIntVar(CommonStrings.SYSTEMI_EDITORTYPE)>0)
			showFlag=-999;
		while(!ok)
		{
			int showNumber=0;
			genName(mob,me,++showNumber,showFlag);
			genDisplayText(mob,me,++showNumber,showFlag);
			genDescription(mob,me,++showNumber,showFlag);
			genLevel(mob,me,++showNumber,showFlag);
			genAttack(mob,me,++showNumber,showFlag);
			genDamage(mob,me,++showNumber,showFlag);
			genMaterialCode(mob,me,++showNumber,showFlag);
			genWeaponType(mob,me,++showNumber,showFlag);
			genWeaponClassification(mob,me,++showNumber,showFlag);
			genWeaponRanges(mob,me,++showNumber,showFlag);
			if(me instanceof Wand)
			{
				genReadable1(mob,me,++showNumber,showFlag);
				genReadable2(mob,me,++showNumber,showFlag);
			}
			else
				genWeaponAmmo(mob,me,++showNumber,showFlag);
			genRejuv(mob,me,++showNumber,showFlag);
			if((!me.requiresAmmunition())&&(!(me instanceof Wand)))
				genCondition(mob,me,++showNumber,showFlag);
			genAbility(mob,me,++showNumber,showFlag);
			genSecretIdentity(mob,me,++showNumber,showFlag);
			genGettable(mob,me,++showNumber,showFlag);
			genValue(mob,me,++showNumber,showFlag);
			genWeight(mob,me,++showNumber,showFlag);
			genDisposition(mob,me,++showNumber,showFlag);
			genBehaviors(mob,me,++showNumber,showFlag);
			genAffects(mob,me,++showNumber,showFlag);
			if(showFlag<-900){ ok=true; break;}
			if(showFlag>0){ showFlag=-1; continue;}
			showFlag=Util.s_int(mob.session().prompt("Edit which? ",""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
				me.recoverEnvStats();
				if(me.text().length()>=maxLength)
				{
					mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
					ok=false;
				}
			}
		}
	}
	public static void modifyGenArmor(MOB mob, Armor me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		int showFlag=-1;
		if(CommonStrings.getIntVar(CommonStrings.SYSTEMI_EDITORTYPE)>0)
			showFlag=-999;
		while(!ok)
		{
			int showNumber=0;
			genName(mob,me,++showNumber,showFlag);
			genDisplayText(mob,me,++showNumber,showFlag);
			genDescription(mob,me,++showNumber,showFlag);
			genLevel(mob,me,++showNumber,showFlag);
			genMaterialCode(mob,me,++showNumber,showFlag);
			genWornLocation(mob,me,++showNumber,showFlag);
			genRejuv(mob,me,++showNumber,showFlag);
			genArmor(mob,me,++showNumber,showFlag);
			genCondition(mob,me,++showNumber,showFlag);
			genAbility(mob,me,++showNumber,showFlag);
			genSecretIdentity(mob,me,++showNumber,showFlag);
			genGettable(mob,me,++showNumber,showFlag);
			genCapacity(mob,me,++showNumber,showFlag);
			genLidsNLocks(mob,me,++showNumber,showFlag);
			genReadable1(mob,me,++showNumber,showFlag);
			genReadable2(mob,me,++showNumber,showFlag);
			genValue(mob,me,++showNumber,showFlag);
			genWeight(mob,me,++showNumber,showFlag);
			genSize(mob,me,++showNumber,showFlag);
			genDisposition(mob,me,++showNumber,showFlag);
			genBehaviors(mob,me,++showNumber,showFlag);
			genAffects(mob,me,++showNumber,showFlag);
			if(showFlag<-900){ ok=true; break;}
			if(showFlag>0){ showFlag=-1; continue;}
			showFlag=Util.s_int(mob.session().prompt("Edit which? ",""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
				me.recoverEnvStats();
				if(me.text().length()>=maxLength)
				{
					mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
					ok=false;
				}
			}
		}
	}
	public static void modifyGenInstrument(MOB mob, MusicalInstrument me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		int showFlag=-1;
		if(CommonStrings.getIntVar(CommonStrings.SYSTEMI_EDITORTYPE)>0)
			showFlag=-999;
		while(!ok)
		{
			int showNumber=0;
			genName(mob,me,++showNumber,showFlag);
			genDisplayText(mob,me,++showNumber,showFlag);
			genDescription(mob,me,++showNumber,showFlag);
			genLevel(mob,me,++showNumber,showFlag);
			genMaterialCode(mob,me,++showNumber,showFlag);
			genWornLocation(mob,me,++showNumber,showFlag);
			genRejuv(mob,me,++showNumber,showFlag);
			genAbility(mob,me,++showNumber,showFlag);
			genSecretIdentity(mob,me,++showNumber,showFlag);
			genGettable(mob,me,++showNumber,showFlag);
			genInstrumentType(mob,me,++showNumber,showFlag);
			genValue(mob,me,++showNumber,showFlag);
			genWeight(mob,me,++showNumber,showFlag);
			genDisposition(mob,me,++showNumber,showFlag);
			genBehaviors(mob,me,++showNumber,showFlag);
			genAffects(mob,me,++showNumber,showFlag);
			if(showFlag<-900){ ok=true; break;}
			if(showFlag>0){ showFlag=-1; continue;}
			showFlag=Util.s_int(mob.session().prompt("Edit which? ",""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
				me.recoverEnvStats();
				if(me.text().length()>=maxLength)
				{
					mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
					ok=false;
				}
			}
		}
	}
	public static void modifyGenExit(MOB mob, Exit me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		int showFlag=-1;
		if(CommonStrings.getIntVar(CommonStrings.SYSTEMI_EDITORTYPE)>0)
			showFlag=-999;
		while(!ok)
		{
			int showNumber=0;
			genName(mob,me,++showNumber,showFlag);
			genDisplayText(mob,me,++showNumber,showFlag);
			genDescription(mob,me,++showNumber,showFlag);
			genLevel(mob,me,++showNumber,showFlag);
			genDoorsNLocks(mob,me,++showNumber,showFlag);
			if(me.hasADoor())
			{
				genClosedText(mob,me,++showNumber,showFlag);
				genDoorName(mob,me,++showNumber,showFlag);
				genOpenWord(mob,me,++showNumber,showFlag);
				genCloseWord(mob,me,++showNumber,showFlag);
			}
			genExitMisc(mob,me,++showNumber,showFlag);
			genDisposition(mob,me,++showNumber,showFlag);
			genBehaviors(mob,me,++showNumber,showFlag);
			genAffects(mob,me,++showNumber,showFlag);
			if(showFlag<-900){ ok=true; break;}
			if(showFlag>0){ showFlag=-1; continue;}
			showFlag=Util.s_int(mob.session().prompt("Edit which? ",""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
				me.recoverEnvStats();
				if(me.text().length()>=maxLength)
				{
					mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
					ok=false;
				}
			}
		}
	}
	public static void modifyGenMOB(MOB mob, MOB me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		int showFlag=-1;
		if(CommonStrings.getIntVar(CommonStrings.SYSTEMI_EDITORTYPE)>0)
			showFlag=-999;
		while(!ok)
		{
			int showNumber=0;
			genName(mob,me,++showNumber,showFlag);
			genDisplayText(mob,me,++showNumber,showFlag);
			genDescription(mob,me,++showNumber,showFlag);
			int oldLevel=me.baseEnvStats().level();
			genLevel(mob,me,++showNumber,showFlag);
			if((oldLevel==0)&&(me.baseEnvStats().level()>0))
				me.baseCharStats().getCurrentClass().buildMOB(me,me.baseEnvStats().level(),mob.getAlignment(),mob.baseEnvStats().weight(),mob.getWimpHitPoint(),(char)mob.baseCharStats().getStat(CharStats.GENDER));
			genRejuv(mob,me,++showNumber,showFlag);
			genRace(mob,me,++showNumber,showFlag);
			genGender(mob,me,++showNumber,showFlag);
			genHeight(mob,me,++showNumber,showFlag);
			genWeight(mob,me,++showNumber,showFlag);
			genSpeed(mob,me,++showNumber,showFlag);
			if((oldLevel==0)&&(me.baseEnvStats().level()>0))
				me.baseEnvStats().setDamage((int)Math.round(Util.div(me.baseEnvStats().damage(),me.baseEnvStats().speed())));
			genAttack(mob,me,++showNumber,showFlag);
			genDamage(mob,me,++showNumber,showFlag);
			genArmor(mob,me,++showNumber,showFlag);
			genHitPoints(mob,me,++showNumber,showFlag);
			genAlignment(mob,me,++showNumber,showFlag);
			genMoney(mob,me,++showNumber,showFlag);
			genAbilities(mob,me,++showNumber,showFlag);
			genBehaviors(mob,me,++showNumber,showFlag);
			genAffects(mob,me,++showNumber,showFlag);
			genDisposition(mob,me,++showNumber,showFlag);
			genSensesMask(mob,me,++showNumber,showFlag);
			if(me instanceof Rideable)
			{
				genRideable1(mob,(Rideable)me,++showNumber,showFlag);
				genRideable2(mob,(Rideable)me,++showNumber,showFlag);
			}
			if(me instanceof Deity)
			{
				genDeity1(mob,(Deity)me,++showNumber,showFlag);
				genDeity2(mob,(Deity)me,++showNumber,showFlag);
				genDeity3(mob,(Deity)me,++showNumber,showFlag);
				genDeity4(mob,(Deity)me,++showNumber,showFlag);
				genDeity5(mob,(Deity)me,++showNumber,showFlag);
				genDeity8(mob,(Deity)me,++showNumber,showFlag);
				genDeity9(mob,(Deity)me,++showNumber,showFlag);
				genDeity6(mob,(Deity)me,++showNumber,showFlag);
				genDeity0(mob,(Deity)me,++showNumber,showFlag);
				genDeity7(mob,(Deity)me,++showNumber,showFlag);
			}
			if(showFlag<-900){ ok=true; break;}
			if(showFlag>0){ showFlag=-1; continue;}
			showFlag=Util.s_int(mob.session().prompt("Edit which? ",""));
			if(showFlag<=0)
			{
				showFlag=-1;
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
				me.setMiscText(me.text());
			}
		}

		mob.tell("\n\rNow don't forget to equip him with stuff before saving!\n\r");
	}

	public static void modifyPlayer(MOB mob, MOB me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		boolean ok=false;
		int showFlag=-1;
		if(CommonStrings.getIntVar(CommonStrings.SYSTEMI_EDITORTYPE)>0)
			showFlag=-999;
		String oldName=me.Name();
		while(!ok)
		{
			int showNumber=0;
			genName(mob,me,++showNumber,showFlag);
			while((!me.Name().equals(oldName))&&(ExternalPlay.DBUserSearch(null,me.Name())))
			{
				mob.tell("The name given cannot be chosen, as it is already being used.");
				genName(mob,me,showNumber,showFlag);
			}

			genDescription(mob,me,++showNumber,showFlag);
			genLevel(mob,me,++showNumber,showFlag);
			genRace(mob,me,++showNumber,showFlag);
			genCharClass(mob,me,++showNumber,showFlag);
			genCharStats(mob,me,++showNumber,showFlag);
			genGender(mob,me,++showNumber,showFlag);
			genHeight(mob,me,++showNumber,showFlag);
			genWeight(mob,me,++showNumber,showFlag);
			genSpeed(mob,me,++showNumber,showFlag);
			genAttack(mob,me,++showNumber,showFlag);
			genDamage(mob,me,++showNumber,showFlag);
			genArmor(mob,me,++showNumber,showFlag);
			genHitPoints(mob,me,++showNumber,showFlag);
			genAlignment(mob,me,++showNumber,showFlag);
			genMoney(mob,me,++showNumber,showFlag);
			genAbilities(mob,me,++showNumber,showFlag);
			genAffects(mob,me,++showNumber,showFlag);
			genBehaviors(mob,me,++showNumber,showFlag);
			genDisposition(mob,me,++showNumber,showFlag);
			genSensesMask(mob,me,++showNumber,showFlag);
			if(me instanceof Rideable)
			{
				genRideable1(mob,(Rideable)me,++showNumber,showFlag);
				genRideable2(mob,(Rideable)me,++showNumber,showFlag);
			}
			if(showFlag<-900){ ok=true; break;}
			if(showFlag>0){ showFlag=-1; continue;}
			showFlag=Util.s_int(mob.session().prompt("Edit which? ",""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
				me.recoverCharStats();
				me.recoverMaxState();
				me.recoverEnvStats();
				me.resetToMaxState();
				if(!oldName.equals(me.Name()))
				{
					MOB fakeMe=(MOB)me.copyOf();
					fakeMe.setName(oldName);
					ExternalPlay.DBDeleteMOB(fakeMe);
					ExternalPlay.DBCreateCharacter(me);
					ExternalPlay.DBUpdateMOB(me);
				}
			}
		}
	}

	public static void modifyGenShopkeeper(MOB mob, ShopKeeper me)
		throws IOException
	{
		if(mob.isMonster())
			return;
		if(!(me instanceof MOB))
			return;
		MOB mme=(MOB)me;
		boolean ok=false;
		int showFlag=-1;
		if(CommonStrings.getIntVar(CommonStrings.SYSTEMI_EDITORTYPE)>0)
			showFlag=-999;
		while(!ok)
		{
			int showNumber=0;
			genName(mob,me,++showNumber,showFlag);
			genDisplayText(mob,me,++showNumber,showFlag);
			genDescription(mob,me,++showNumber,showFlag);
			int oldLevel=me.baseEnvStats().level();
			genLevel(mob,me,++showNumber,showFlag);
			if((oldLevel==0)&&(me.baseEnvStats().level()>0))
				mme.baseCharStats().getCurrentClass().buildMOB(mme,me.baseEnvStats().level(),mob.getAlignment(),mob.baseEnvStats().weight(),mob.getWimpHitPoint(),(char)mob.baseCharStats().getStat(CharStats.GENDER));
			genRejuv(mob,me,++showNumber,showFlag);
			genRace(mob,mme,++showNumber,showFlag);
			genHeight(mob,me,++showNumber,showFlag);
			genWeight(mob,me,++showNumber,showFlag);
			genGender(mob,mme,++showNumber,showFlag);
			genSpeed(mob,me,++showNumber,showFlag);
			if((oldLevel==0)&&(me.baseEnvStats().level()>0))
				me.baseEnvStats().setDamage((int)Math.round(Util.div(me.baseEnvStats().damage(),me.baseEnvStats().speed())));
			genAttack(mob,me,++showNumber,showFlag);
			genDamage(mob,me,++showNumber,showFlag);
			genArmor(mob,me,++showNumber,showFlag);
			if(me instanceof MOB)
				genHitPoints(mob,(MOB)me,++showNumber,showFlag);
			genAlignment(mob,mme,++showNumber,showFlag);
			genMoney(mob,mme,++showNumber,showFlag);
			genAbilities(mob,mme,++showNumber,showFlag);
			genBehaviors(mob,me,++showNumber,showFlag);
			genAffects(mob,me,++showNumber,showFlag);
			genShopkeeper1(mob,me,++showNumber,showFlag);
			genShopkeeper2(mob,me,++showNumber,showFlag);
			genShopkeeper3(mob,me,++showNumber,showFlag);
			if(me instanceof Banker)
			{
				genBanker1(mob,(Banker)me,++showNumber,showFlag);
				genBanker2(mob,(Banker)me,++showNumber,showFlag);
				genBanker3(mob,(Banker)me,++showNumber,showFlag);
			}
			genDisposition(mob,me,++showNumber,showFlag);
			genSensesMask(mob,me,++showNumber,showFlag);
			if(showFlag<-900){ ok=true; break;}
			if(showFlag>0){ showFlag=-1; continue;}
			showFlag=Util.s_int(mob.session().prompt("Edit which? ",""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
				mme.recoverCharStats();
				mme.recoverMaxState();
				me.recoverEnvStats();
				mme.resetToMaxState();
				if(me.text().length()>=maxLength)
				{
					mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.  Please modify!");
					ok=false;
				}
				me.setMiscText(me.text());
			}
		}

		mob.tell("\n\rNow don't forget to equip him with non-generic items before saving! If you DO add items to his list, be sure to come back here in case you've exceeded the string limit again.\n\r");
	}
}
