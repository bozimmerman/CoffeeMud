package com.planet_ink.coffee_mud.Commands.sysop;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;
import java.io.*;

public class Races
{
	private Races(){}
	public static boolean destroy(MOB mob, Vector commands)
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is DESTROY RACE [RACE ID]\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}

		String raceID=Util.combine(commands,2);
		Race R=CMClass.getRace(raceID);
		if(R==null)
		{
			mob.tell("'"+raceID+"' is an invalid race id.");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		if(!(R.isGeneric()))
		{
			mob.tell("'"+R.ID()+"' is not generic, and may not be deleted.");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		for(Enumeration e=CMMap.rooms();e.hasMoreElements();)
		{
			Room room=(Room)e.nextElement();
			for(int i=0;i<room.numInhabitants();i++)
			{
				MOB M=(MOB)room.fetchInhabitant(i);
				if(M.baseCharStats().getMyRace()==R)
				{
					mob.tell("A MOB called '"+M.Name()+" in "+room.roomID()+" is this race, and must first be deleted.");
					mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
					return false;
				}
			}
		}
		CMClass.delRace(R);
		ExternalPlay.DBDeleteRace(R.ID());
		mob.location().showHappens(Affect.MSG_OK_ACTION,"The diversity of the world just decreased!");
		return true;
	}
	public static boolean modify(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY RACE [RACE ID]\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}

		String raceID=Util.combine(commands,2);
		Race R=CMClass.getRace(raceID);
		if(R==null)
		{
			mob.tell("'"+raceID+"' is an invalid race id.");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		if(!(R.isGeneric()))
		{
			mob.tell("'"+R.ID()+"' is not generic, and may not be modified.");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		modifyGenRace(mob,R);
		ExternalPlay.DBDeleteRace(R.ID());
		ExternalPlay.DBCreateRace(R.ID(),R.racialParms());
		mob.location().showHappens(Affect.MSG_OK_ACTION,R.name()+"'s everywhere shake under the transforming power!");
		return true;
	}
	static void genText(MOB mob, Race E, int showNumber, int showFlag, String FieldDisp, String Field)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". "+FieldDisp+": '"+E.getStat(Field)+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt("Enter a new one\n\r:","");
		if(newName.length()>0)
			E.setStat(Field,newName);
		else
			mob.tell("(no change)");
	}
	static void genBool(MOB mob, Race E, int showNumber, int showFlag, String FieldDisp, String Field)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". "+FieldDisp+": '"+E.getStat(Field)+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt("Enter a new true/false\n\r:","");
		if((newName.length()>0)&&(newName.equalsIgnoreCase("true")||newName.equalsIgnoreCase("false")))
			E.setStat(Field,newName.toLowerCase());
		else
			mob.tell("(no change)");
	}
	static void genCat(MOB mob, Race E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Racial Category: '"+E.racialCategory()+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt("Enter a new one\n\r:","");
		if(newName.length()>0)
		{
			boolean found=false;
			if(newName.startsWith("new "))
			{
				newName=Util.capitalize(newName.substring(4));
				if(newName.length()>0)
					found=true;
			}
			else
			for(Enumeration r=CMClass.races();r.hasMoreElements();)
			{
				Race R=(Race)r.nextElement();
				if(newName.equalsIgnoreCase(R.racialCategory()))
				{
					newName=R.racialCategory();
					found=true;
					break;
				}
			}
			if(!found)
			{
				StringBuffer str=new StringBuffer("That category does not exist.  Valid categories include: ");
				HashSet H=new HashSet();
				for(Enumeration r=CMClass.races();r.hasMoreElements();)
				{
					Race R=(Race)r.nextElement();
					if(!H.contains(R.racialCategory()))
					{
						H.add(R.racialCategory());
						str.append(R.racialCategory()+", ");
					}
				}
				mob.tell(str.toString().substring(0,str.length()-2)+".");
			}
			else
				E.setStat("CAT",newName);
		}
		else
			mob.tell("(no change)");
	}
	static void genHealthBuddy(MOB mob, Race E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		mob.tell(showNumber+". Health Race: '"+E.getStat("HEALTHRACE")+"'.");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt("Enter a new one\n\r:","");
		if(newName.length()>0)
		{
			Race R2=CMClass.getRace(newName);
			if((R2!=null)&&(R2.isGeneric()))
				R2=null;
			if(R2==null)
			{
				StringBuffer str=new StringBuffer("That race name is invalid.  Valid races include: ");
				for(Enumeration r=CMClass.races();r.hasMoreElements();)
				{
					Race R=(Race)r.nextElement();
					if(!R.isGeneric())
						str.append(R.ID()+", ");
				}
				mob.tell(str.toString().substring(0,str.length()-2)+".");
			}
			else
				E.setStat("HEALTHRACE",R2.ID());
		}
		else
			mob.tell("(no change)");
	}
	static void genBodyParts(MOB mob, Race E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		StringBuffer parts=new StringBuffer("");
		for(int i=0;i<Race.BODYPARTSTR.length;i++)
			if(E.bodyMask()[i]!=0) parts.append(Race.BODYPARTSTR[i].toLowerCase()+"("+E.bodyMask()[i]+") ");
		mob.tell(showNumber+". Body Parts: "+parts.toString()+".");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt("Enter a body part\n\r:","");
		if(newName.length()>0)
		{
			int partNum=-1;
			for(int i=0;i<Race.BODYPARTSTR.length;i++)
				if(newName.equalsIgnoreCase(Race.BODYPARTSTR[i]))
				{ partNum=i; break;}
			if(partNum<0)
			{
				StringBuffer str=new StringBuffer("That body part is invalid.  Valid parts include: ");
				for(int i=0;i<Race.BODYPARTSTR.length;i++)
					str.append(Race.BODYPARTSTR[i]+", ");
				mob.tell(str.toString().substring(0,str.length()-2)+".");
			}
			else
			{
				newName=mob.session().prompt("Enter new number ("+E.bodyMask()[partNum]+"), 0=none\n\r:",""+E.bodyMask()[partNum]);
				if(newName.length()>0)
					E.bodyMask()[partNum]=Util.s_int(newName);
				else
					mob.tell("(no change)");
			}
		}
		else
			mob.tell("(no change)");
	}
	static void genEStats(MOB mob, Race R, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		EnvStats S=new DefaultEnvStats(0);
		com.planet_ink.coffee_mud.common.Generic.setEnvStats(S,R.getStat("ESTATS"));
		StringBuffer parts=new StringBuffer("");
		for(int i=0;i<S.getCodes().length;i++)
			if(Util.s_int(S.getStat(S.getCodes()[i]))!=0)
				parts.append(Util.capitalize(S.getCodes()[i])+"("+S.getStat(S.getCodes()[i])+") ");
		mob.tell(showNumber+". EStat Adjustments: "+parts.toString()+".");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt("Enter a stat name\n\r:","");
		if(newName.length()>0)
		{
			String partName=null;
			for(int i=0;i<S.getCodes().length;i++)
				if(newName.equalsIgnoreCase(S.getCodes()[i]))
				{ partName=S.getCodes()[i]; break;}
			if(partName==null)
			{
				StringBuffer str=new StringBuffer("That stat is invalid.  Valid stats include: ");
				for(int i=0;i<S.getCodes().length;i++)
					str.append(S.getCodes()[i]+", ");
				mob.tell(str.toString().substring(0,str.length()-2)+".");
			}
			else
			{
				boolean checkChange=false;
				if(partName.equals("DISPOSITION"))
				{
					Generic.genDisposition(mob,S,0,0);
					checkChange=true;
				}
				else
				if(partName.equals("SENSES"))
				{
					Generic.genSensesMask(mob,S,0,0);
					checkChange=true;
				}
				else
				{
					newName=mob.session().prompt("Enter a value\n\r:","");
					if(newName.length()>0)
					{
						S.setStat(partName,newName);
						checkChange=true;
					}
					else
						mob.tell("(no change)");
				}
				if(checkChange)
				{
					boolean zereoed=true;
					for(int i=0;i<S.getCodes().length;i++)
					{ 
						if(Util.s_int(S.getStat(S.getCodes()[i]))!=0)
						{ zereoed=false; break;}
					}
					if(zereoed)
						R.setStat("ESTATS","");
					else
						R.setStat("ESTATS",com.planet_ink.coffee_mud.common.Generic.getEnvStatsStr(S));
				}
			}
		}
		else
			mob.tell("(no change)");
	}
	static void genAState(MOB mob, Race R, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		CharState S=new DefaultCharState(0);
		com.planet_ink.coffee_mud.common.Generic.setCharState(S,R.getStat("ASTATE"));
		StringBuffer parts=new StringBuffer("");
		for(int i=0;i<S.getCodes().length;i++)
			if(Util.s_int(S.getStat(S.getCodes()[i]))!=0)
				parts.append(Util.capitalize(S.getCodes()[i])+"("+S.getStat(S.getCodes()[i])+") ");
		mob.tell(showNumber+". State Adjustments: "+parts.toString()+".");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt("Enter a stat name\n\r:","");
		if(newName.length()>0)
		{
			String partName=null;
			for(int i=0;i<S.getCodes().length;i++)
				if(newName.equalsIgnoreCase(S.getCodes()[i]))
				{ partName=S.getCodes()[i]; break;}
			if(partName==null)
			{
				StringBuffer str=new StringBuffer("That stat is invalid.  Valid stats include: ");
				for(int i=0;i<S.getCodes().length;i++)
					str.append(S.getCodes()[i]+", ");
				mob.tell(str.toString().substring(0,str.length()-2)+".");
			}
			else
			{
				boolean checkChange=false;
				newName=mob.session().prompt("Enter a value\n\r:","");
				if(newName.length()>0)
				{
					S.setStat(partName,newName);
					boolean zereoed=true;
					for(int i=0;i<S.getCodes().length;i++)
					{ 
						if(Util.s_int(S.getStat(S.getCodes()[i]))!=0)
						{ zereoed=false; break;}
					}
					if(zereoed)
						R.setStat("ASTATE","");
					else
						R.setStat("ASTATE",com.planet_ink.coffee_mud.common.Generic.getCharStateStr(S));
				}
				else
					mob.tell("(no change)");
			}
		}
		else
			mob.tell("(no change)");
	}
	static void genAStats(MOB mob, Race R, String Field, String FieldName, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		CharStats S=new DefaultCharStats(0);
		com.planet_ink.coffee_mud.common.Generic.setCharStats(S,R.getStat(Field));
		StringBuffer parts=new StringBuffer("");
		for(int i=0;i<S.TRAITS.length;i++)
			if(S.getStat(i)!=0)
				parts.append(Util.capitalize(S.TRAITS[i])+"("+S.getStat(i)+") ");
		mob.tell(showNumber+". "+FieldName+": "+parts.toString()+".");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt("Enter a stat name\n\r:","");
		if(newName.length()>0)
		{
			int partNum=-1;
			for(int i=0;i<S.TRAITS.length;i++)
				if(newName.equalsIgnoreCase(S.TRAITS[i]))
				{ partNum=i; break;}
			if(partNum<0)
			{
				StringBuffer str=new StringBuffer("That stat is invalid.  Valid stats include: ");
				for(int i=0;i<S.TRAITS.length;i++)
					str.append(S.TRAITS[i]+", ");
				mob.tell(str.toString().substring(0,str.length()-2)+".");
			}
			else
			{
				newName=mob.session().prompt("Enter a value\n\r:","");
				if(newName.length()>0)
				{
					S.setStat(partNum,Util.s_int(newName));
					boolean zereoed=true;
					for(int i=0;i<S.TRAITS.length;i++)
					{ 
						if(S.getStat(i)!=0)
						{ zereoed=false; break;}
					}
					if(zereoed)
						R.setStat(Field,"");
					else
						R.setStat(Field,com.planet_ink.coffee_mud.common.Generic.getCharStatsStr(S));
				}
				else
					mob.tell("(no change)");
			}
		}
		else
			mob.tell("(no change)");
	}
	
	static void genResources(MOB mob, Race E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		while(true)
		{
			StringBuffer parts=new StringBuffer("");
			int numResources=Util.s_int(E.getStat("NUMRSC"));
			Vector V=new Vector();
			for(int v=0;v<numResources;v++)
			{
				Item I=CMClass.getItem(E.getStat("GETRSCID"+v));
				if(I!=null)
				{
					I.setMiscText(E.getStat("GETRSCPARM"+v));
					I.recoverEnvStats();
					parts.append(I.name()+", ");
					V.addElement(I);
				}
			}
			if(parts.toString().endsWith(", "))
			{parts.deleteCharAt(parts.length()-1);parts.deleteCharAt(parts.length()-1);}
			mob.tell(showNumber+". Resources: "+parts.toString()+".");
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			String newName=mob.session().prompt("Enter a resource name to remove or\n\rthe word new and an item name to add from your inventory\n\r:","");
			if(newName.length()>0)
			{
				int partNum=-1;
				for(int i=0;i<V.size();i++)
					if(CoffeeUtensils.containsString(((Item)V.elementAt(i)).name(),newName))
					{ partNum=i; break;}
				boolean updateList=false;
				if(partNum<0)
				{
					if(!newName.toLowerCase().startsWith("new "))
						mob.tell("That is neither an existing resource name, or the word new followed by a valid item name.");
					else
					{
						Item I=mob.fetchCarried(null,newName.substring(4).trim());
						if(I!=null)
						{
							I=(Item)I.copyOf();
							V.addElement(I);
							mob.tell(I.name()+" added.");
							updateList=true;
						}
							
					}
				}
				else
				{
					Item I=(Item)V.elementAt(partNum);
					V.removeElementAt(partNum);
					mob.tell(I.name()+" removed.");
					updateList=true;
				}
				if(updateList)
				{
					E.setStat("NUMRSC","");
					for(int i=0;i<V.size();i++)
						E.setStat("GETRSCID"+i,((Item)V.elementAt(i)).ID());
					for(int i=0;i<V.size();i++)
						E.setStat("GETRSCPARM"+i,((Item)V.elementAt(i)).text());
				}
			}
			else
			{
				mob.tell("(no change)");
				return;
			}
		}
	}
	static void genWeapon(MOB mob, Race E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		StringBuffer parts=new StringBuffer("");
		Item I=CMClass.getItem(E.getStat("WEAPONCLASS"));
		if(I!=null)
		{
			I.setMiscText(E.getStat("WEAPONXML"));
			I.recoverEnvStats();
			parts.append(I.name());
		}
		mob.tell(showNumber+". Natural Weapon: "+parts.toString()+".");
		if((showFlag!=showNumber)&&(showFlag>-999)) return;
		String newName=mob.session().prompt("Enter a weapon name from your inventory to change, or 'null' for human\n\r:","");
		if(newName.equalsIgnoreCase("null"))
		{
			E.setStat("WEAPONCLASS","");
			mob.tell("Human weapons set.");
		}
		else
		if(newName.length()>0)
		{
			I=mob.fetchCarried(null,newName);
			if(I==null)
			{
				mob.tell("'"+newName+"' is not in your inventory.");
				mob.tell("(no change)");
				return;
			}
			else
			{
				I=(Item)I.copyOf();
				E.setStat("WEAPONCLASS",I.ID());
				E.setStat("WEAPONXML",I.text());
			}
		}
		else
		{
			mob.tell("(no change)");
			return;
		}
	}
	
	static void genRacialAbilities(MOB mob, Race E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		while(true)
		{
			StringBuffer parts=new StringBuffer("");
			int numResources=Util.s_int(E.getStat("NUMRABLE"));
			Vector ables=new Vector();
			Vector data=new Vector();
			for(int v=0;v<numResources;v++)
			{
				Ability A=CMClass.getAbility(E.getStat("GETRABLE"+v));
				if(A!=null)
				{
					parts.append("("+A.ID()+"/"+E.getStat("GETRABLELVL"+v)+"/"+E.getStat("GETRABLEQUAL"+v)+"/"+E.getStat("GETRABLEPROF"+v)+"), ");
					ables.addElement(A);
					data.addElement(A.ID()+";"+E.getStat("GETRABLELVL"+v)+";"+E.getStat("GETRABLEQUAL"+v)+";"+E.getStat("GETRABLEPROF"+v));
				}
			}
			if(parts.toString().endsWith(", "))
			{parts.deleteCharAt(parts.length()-1);parts.deleteCharAt(parts.length()-1);}
			mob.tell(showNumber+". Racial Abilities: "+parts.toString()+".");
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			String newName=mob.session().prompt("Enter an ability name to add or remove\n\r:","");
			if(newName.equalsIgnoreCase("?"))
				mob.tell(Lister.reallyList(CMClass.abilities(),-1).toString());
			else
			if(newName.length()>0)
			{
				int partNum=-1;
				for(int i=0;i<ables.size();i++)
					if(CoffeeUtensils.containsString(((Ability)ables.elementAt(i)).ID(),newName))
					{ partNum=i; break;}
				boolean updateList=false;
				if(partNum<0)
				{
					Ability A=CMClass.getAbility(newName);
					if(A==null)
						mob.tell("That is neither an existing ability name, nor a valid one to add.  Use ? for a list.");
					else
					if(A.isAutoInvoked())
						mob.tell("'"+A.name()+"' cannot be named, as it is autoinvoked.");
					else
					if((A.triggerStrings()==null)||(A.triggerStrings().length==0))
						mob.tell("'"+A.name()+"' cannot be named, as it has no trigger/command words.");
					else
					{
						StringBuffer str=new StringBuffer(A.ID()+";");
						String level=mob.session().prompt("Enter the level of this skill (1): ","1");
						str.append((""+Util.s_int(level))+";");
						if(mob.session().confirm("Is this skill only qualified for (y/N)?","N"))
							str.append("false;");
						else
							str.append("true;");
						String prof=mob.session().prompt("Enter the (perm) profficiency level (100): ","100");
						str.append((""+Util.s_int(prof)));
						data.addElement(str.toString());
						ables.addElement(A);
						mob.tell(A.name()+" added.");
						updateList=true;
					}
				}
				else
				{
					Ability A=(Ability)ables.elementAt(partNum);
					ables.removeElementAt(partNum);
					data.removeElementAt(partNum);
					updateList=true;
					mob.tell(A.name()+" removed.");
				}
				if(updateList)
				{
					if(data.size()>0)
						E.setStat("NUMRABLE",""+data.size());
					else
						E.setStat("NUMRABLE","");
					for(int i=0;i<data.size();i++)
					{
						Vector V=Util.parseSemicolons((String)data.elementAt(i));
						E.setStat("GETRABLE"+i,((String)V.elementAt(0)));
						E.setStat("GETRABLELVL"+i,((String)V.elementAt(1)));
						E.setStat("GETRABLEQUAL"+i,((String)V.elementAt(2)));
						E.setStat("GETRABLEPROF"+i,((String)V.elementAt(3)));
					}
				}
			}
			else
			{
				mob.tell("(no change)");
				return;
			}
		}
	}
	static void genCulturalAbilities(MOB mob, Race E, int showNumber, int showFlag)
		throws IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber)) return;
		while(true)
		{
			StringBuffer parts=new StringBuffer("");
			int numResources=Util.s_int(E.getStat("NUMCABLE"));
			Vector ables=new Vector();
			Vector data=new Vector();
			for(int v=0;v<numResources;v++)
			{
				Ability A=CMClass.getAbility(E.getStat("GETCABLE"+v));
				if(A!=null)
				{
					parts.append("("+A.ID()+"/"+E.getStat("GETCABLEPROF"+v)+"), ");
					ables.addElement(A);
					data.addElement(A.ID()+";"+E.getStat("GETCABLEPROF"+v));
				}
			}
			if(parts.toString().endsWith(", "))
			{parts.deleteCharAt(parts.length()-1);parts.deleteCharAt(parts.length()-1);}
			mob.tell(showNumber+". Cultural Abilities: "+parts.toString()+".");
			if((showFlag!=showNumber)&&(showFlag>-999)) return;
			String newName=mob.session().prompt("Enter an ability name to add or remove\n\r:","");
			if(newName.equalsIgnoreCase("?"))
				mob.tell(Lister.reallyList(CMClass.abilities(),-1).toString());
			else
			if(newName.length()>0)
			{
				int partNum=-1;
				for(int i=0;i<ables.size();i++)
					if(CoffeeUtensils.containsString(((Ability)ables.elementAt(i)).ID(),newName))
					{ partNum=i; break;}
				boolean updateList=false;
				if(partNum<0)
				{
					Ability A=CMClass.getAbility(newName);
					if(A==null)
						mob.tell("That is neither an existing ability name, nor a valid one to add.  Use ? for a list.");
					else
					{
						StringBuffer str=new StringBuffer(A.ID()+";");
						String prof=mob.session().prompt("Enter the default profficiency level (100): ","100");
						str.append((""+Util.s_int(prof)));
						data.addElement(str.toString());
						ables.addElement(A);
						mob.tell(A.name()+" added.");
						updateList=true;
					}
				}
				else
				{
					Ability A=(Ability)ables.elementAt(partNum);
					ables.removeElementAt(partNum);
					data.removeElementAt(partNum);
					updateList=true;
					mob.tell(A.name()+" removed.");
				}
				if(updateList)
				{
					if(data.size()>0)
						E.setStat("NUMCABLE",""+data.size());
					else
						E.setStat("NUMCABLE","");
					for(int i=0;i<data.size();i++)
					{
						Vector V=Util.parseSemicolons((String)data.elementAt(i));
						E.setStat("GETCABLE"+i,((String)V.elementAt(0)));
						E.setStat("GETCABLEPROF"+i,((String)V.elementAt(1)));
					}
				}
			}
			else
			{
				mob.tell("(no change)");
				return;
			}
		}
	}
	public static void modifyGenRace(MOB mob, Race me)
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
			genText(mob,me,++showNumber,showFlag,"Name","NAME");
			genCat(mob,me,++showNumber,showFlag);
			genText(mob,me,++showNumber,showFlag,"Base Weight","BWEIGHT");
			genText(mob,me,++showNumber,showFlag,"Weight Variance","VWEIGHT");
			genText(mob,me,++showNumber,showFlag,"Base Male Height","MHEIGHT");
			genText(mob,me,++showNumber,showFlag,"Base Female Height","FHEIGHT");
			genText(mob,me,++showNumber,showFlag,"Height Variance","VHEIGHT");
			genBool(mob,me,++showNumber,showFlag,"Player Race","PLAYER");
			genText(mob,me,++showNumber,showFlag,"Leaving text","LEAVE");
			genText(mob,me,++showNumber,showFlag,"Arriving text","ARRIVE");
			genHealthBuddy(mob,me,++showNumber,showFlag);
			genBodyParts(mob,me,++showNumber,showFlag);
			genEStats(mob,me,++showNumber,showFlag);
			genAStats(mob,me,"ASTATS","CharStat Adjustments",++showNumber,showFlag);
			genAStats(mob,me,"CSTATS","CharStat Settings",++showNumber,showFlag);
			genAState(mob,me,++showNumber,showFlag);
			genResources(mob,me,++showNumber,showFlag);
			genWeapon(mob,me,++showNumber,showFlag);
			genRacialAbilities(mob,me,++showNumber,showFlag);
			genCulturalAbilities(mob,me,++showNumber,showFlag);
			if(showFlag<-900){ ok=true; break;}
			if(showFlag>0){ showFlag=-1; continue;}
			showFlag=Util.s_int(mob.session().prompt("Edit which? ",""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
			}
		}
	}
	
	public static void create(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is CREATE RACE [RACE ID]\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		String raceID=Util.combine(commands,2);
		Race R=CMClass.getRace(raceID);
		if((R!=null)&&(R.isGeneric()))
		{
			mob.tell("A generic race with the ID '"+R.ID()+"' already exists!");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		if(raceID.indexOf(" ")>=0)
		{
			mob.tell("'"+raceID+"' is an invalid race id, because it contains a space.");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		Race GR=CMClass.getRace("GenRace").copyOf();
		GR.setRacialParms("<RACE><ID>"+Util.capitalize(raceID)+"</ID><NAME>"+Util.capitalize(raceID)+"</NAME></RACE>");
		CMClass.addRace(GR);
		modifyGenRace(mob,GR);
		ExternalPlay.DBCreateRace(GR.ID(),GR.racialParms());
		mob.location().showHappens(Affect.MSG_OK_ACTION,"The diversity of the world just increased!");
	}

}
