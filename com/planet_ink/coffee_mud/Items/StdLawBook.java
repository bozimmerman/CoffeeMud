package com.planet_ink.coffee_mud.Items;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.*;

public class StdLawBook extends StdItem
{
	public String ID(){	return "StdLawBook";}
	public StdLawBook()
	{
		super();
		setName("a law book");
		setDisplayText("a law book sits here.");
		setDescription("Enter `READ [PAGE NUMBER] \"law book\"` to read an entry.%0D%0AUse your WRITE skill to add new entries. ");
		material=EnvResource.RESOURCE_PAPER;
		baseEnvStats().setSensesMask(EnvStats.SENSE_ITEMREADABLE);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new StdLawBook();
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(msg.amITarget(this))
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_WRITE:
			msg.source().tell("You are not allowed to write on "+name()+". Try reading it.");
			return false;
		}
		return super.okMessage(myHost,msg);
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		MOB mob=msg.source();
		if(msg.amITarget(this))
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_READSOMETHING:
			if(!Sense.canBeSeenBy(this,mob))
				mob.tell("You can't see that!");
			else
			if(!mob.isMonster())
			{
				Area A=CMMap.getArea(readableText());
				Vector VB=null;
				if(A!=null)	VB=Sense.flaggedBehaviors(A,Behavior.FLAG_LEGALBEHAVIOR);
				if((VB==null)||(VB.size()==0))
				{
					msg.source().tell("The pages appear blank, and damaged.");
					return;
				}
				Behavior B=(Behavior)VB.firstElement();
				VB=new Vector();
				VB.addElement(new Integer(Law.MOD_LEGALINFO));
				B.modifyBehavior(A,mob,VB);
				Law theLaw=(Law)VB.firstElement();

				int which=-1;
				if(Util.s_long(msg.targetMessage())>0)
					which=Util.s_int(msg.targetMessage());

				boolean allowedToModify=mob.isASysOp(null);
				if(A.getMap().hasMoreElements())
					allowedToModify=mob.isASysOp((Room)A.getMap().nextElement());
				Vector V=new Vector();
				V.addElement(new Integer(Law.MOD_RULINGCLAN));
				if((!allowedToModify)
				&&(B.modifyBehavior(A,mob,V))
				&&(V.size()==1)
				&&(V.firstElement() instanceof String))
				{
					String clanID=(String)V.elementAt(0);
					if((clanID.length()>0)
					&&(mob.getClanID().equals(clanID)))
					{
						Clan C=Clans.getClan(clanID);
						if((C!=null)&&(C.allowedToDoThis(mob,Clan.FUNC_CLANCANORDERCONQUERED)==1))
							allowedToModify=true;
					}
				}

				if((allowedToModify)&&(!theLaw.lawIsActivated()))
					changeTheLaw(A,B,mob,theLaw,"ACTIVATED","TRUE");

				try{
					if(which<1)
					{
						if(mob.session()!=null)
						{
							StringBuffer str=new StringBuffer();
							str.append("^hLaws of "+A.name()+"^?\n\r\n\r");
							str.append(getFromTOC("TOC"));
							mob.session().colorOnlyPrintln(str.toString());
						}
					}
					else
					switch(which)
					{
					case 1:
						if(mob.session()!=null)
							mob.tell(Util.replaceAll(getFromTOC("P1"+(theLaw.hasModifiableLaws()?"MOD":"")+(theLaw.hasModifiableNames()?"NAM":"")),"<AREA>",A.name()));
						break;
					case 2:	doOfficersAndJudges(A,B,theLaw,mob); break;
					case 3:	doVictimsOfCrime(A,B,theLaw,mob); break;
					case 4: doJailPolicy(A,B,theLaw,mob); break;
					case 5: doParoleAndRelease(A,B,theLaw,mob); break;
					case 6: doBasicLaw(A,B,theLaw,mob); break;
					case 7: doTresspassingLaw(A,B,theLaw,mob); break;
					case 8: doIllegalInfluence(A,B,theLaw,mob); break;
					case 9: doIllegalSkill(A,B,theLaw,mob); break;
					case 10: doIllegalEmotation(A,B,theLaw,mob); break;
					}
				}
				catch(Exception e)
				{
					Log.errOut("LawBook",e);
				}
			}
			return;
		case CMMsg.TYP_WRITE:
			try
			{
				Area A=CMMap.getArea(readableText());
				Vector VB=null;
				if(A!=null)	VB=Sense.flaggedBehaviors(A,Behavior.FLAG_LEGALBEHAVIOR);
				if((VB==null)||(VB.size()==0))
				{
					msg.source().tell("The pages appear blank, and too damaged to write on.");
					return;
				}
				return;
			}
			catch(Exception e)
			{
				Log.errOut("LawBook",e);
			}
			return;
		}
		super.executeMsg(myHost,msg);
	}

	public String getFromTOC(String tag)
	{
		Properties lawProps=(Properties)Resources.getResource("LAWBOOKTOC");
		try{
			if((lawProps==null)||(lawProps.isEmpty()))
			{
				lawProps=new Properties();
				lawProps.load(new FileInputStream("resources"+File.separatorChar+"lawtoc.ini"));
				Resources.submitResource("LAWBOOKTOC",lawProps);
			}
			String s=(String)lawProps.get(tag);
			if(s==null) return "\n\r";
			return s+"\n\r";
		}
		catch(Exception e)
		{
			Log.errOut("LawBook",e);
		}
		return "";
	}

	public void changeTheLaw(Area A,
							 Behavior B,
							 MOB mob,
							 Law theLaw,
							 String tag,
							 String newValue)
	{
		theLaw.setInternalStr(tag,newValue);
		B.modifyBehavior(A,mob,new Integer(Law.MOD_SETNEWLAW));
	}

	public String shortLawDesc(String[] bits)
	{
		if((bits==null)||(bits.length<Law.BIT_NUMBITS))
			return "Not illegal.";
		String flags=bits[Law.BIT_CRIMEFLAGS]+" "+bits[Law.BIT_CRIMELOCS].trim();
		return Util.padRight(bits[Law.BIT_CRIMENAME],19)+" "
			   +Util.padRight(((flags.length()==0)?"":flags),24)+" "
			   +bits[Law.BIT_SENTENCE];
	}
	public String shortLawHeader()
	{
		return Util.padRight("Crime",19)+" "
			+Util.padRight("Flags",24)+" "
			+"Sentence";
	}


	public final static String[][] locflags={
		{"Only a crime if the person is not at home","!home"},
		{"A crime ONLY if the person is at home","home"},
		{"A crime ONLY if the person is outside and not inside","!indoors"},
		{"A crime ONLY if the person is inside and not outside","indoors"}
	};

	public final static String[][] lawflags={
		{"Only a crime if not recently caught for it","!recently"},
		{"A crime ONLY if witness is in the same room","witness"},
		{"A crime ONLY if witness is NOT in the same room","!witness"},
		{"A crime ONLY if perpetrator is in combat.","combat"},
		{"A crime ONLY if perpetrator is NOT in combat.","!combat"}
	};

	public String[] modifyLaw(Area A, Behavior B, Law theLaw, MOB mob, String[] oldLaw)
		throws IOException
	{
		if(mob.session()==null) return oldLaw;
		mob.tell(getFromTOC("MODLAW"));
		if(oldLaw==null)
		{
			if(mob.session().confirm("This is not presently a crime, would you like to make it one (Y/n)?","Y"))
			{
				oldLaw=new String[Law.BIT_NUMBITS];
				oldLaw[Law.BIT_CRIMENAME]="Name of the crime";
				oldLaw[Law.BIT_CRIMEFLAGS]="";
				oldLaw[Law.BIT_CRIMELOCS]="";
				oldLaw[Law.BIT_SENTENCE]="jail1";
				oldLaw[Law.BIT_WARNMSG]="Shaming/Justification Message to the offender";
			}
			else
				return oldLaw;
		}

		while(true)
		{
			StringBuffer str=new StringBuffer("Modify Law: "+oldLaw[Law.BIT_CRIMENAME]+"\n\r\n\r");
			str.append("1. Name          : "+oldLaw[Law.BIT_CRIMENAME]+"\n\r");
			str.append("2. Flags         : "+oldLaw[Law.BIT_CRIMEFLAGS]+"\n\r");
			str.append("3. Locations mask: "+oldLaw[Law.BIT_CRIMELOCS]+"\n\r");
			str.append("4. Sentence      : "+oldLaw[Law.BIT_SENTENCE]+"\n\r");
			str.append("5. Justification : "+oldLaw[Law.BIT_WARNMSG]+"\n\r");
			str.append("6. DELETE THIS CRIME\n\r");
			mob.session().colorOnlyPrintln(str.toString());
			String s=mob.session().choose("Enter a number to modify or RETURN: ","123456\n","\n");
			int x=Util.s_int(s);
			if(x==0) return oldLaw;
			oldLaw=(String[])oldLaw.clone();
			switch(x)
			{
			case 1:
				oldLaw[Law.BIT_CRIMENAME]=mob.session().prompt("Enter a new name for this crime: ",oldLaw[Law.BIT_CRIMENAME]);
				break;
			case 5:
				oldLaw[Law.BIT_WARNMSG]=mob.session().prompt("Shame/Justification Message: ",oldLaw[Law.BIT_WARNMSG]);
				break;
			case 6:
				if(mob.session().confirm("Are you sure you want to delete this crime (y/N)?","N"))
					return null;
				break;
			case 4:
				{
					StringBuffer msg=new StringBuffer("Sentences ( ");
					for(int i=0;i<Law.ACTION_DESCS.length;i++)
						msg.append(Law.ACTION_DESCS[i].toLowerCase()+" ");
					msg.append("\n\rSelect a sentence ("+oldLaw[Law.BIT_SENTENCE]+"): ");
					String t=mob.session().prompt(msg.toString(),oldLaw[Law.BIT_SENTENCE]);
					for(int i=0;i<Law.ACTION_DESCS.length;i++)
					{
						if(Law.ACTION_DESCS[i].equals(t.toUpperCase()))
						{
							oldLaw[Law.BIT_SENTENCE]=t.toLowerCase();
							break;
						}
					}
					mob.tell("'"+t+"' is not a valid sentence.  Unchanged.");
				}
				break;
			case 3:
				{
					StringBuffer s2=new StringBuffer("");
					String oldVal=oldLaw[Law.BIT_CRIMELOCS].toUpperCase();
					String lastOle="";
					boolean lastAnswer=false;
					Vector allloca1=Util.parse(oldVal);
					Vector allloca2=Util.parse(oldVal.toUpperCase());
					for(int i=0;i<locflags.length;i++)
					{
						int dex=allloca2.indexOf(locflags[i][1].toUpperCase());
						if(dex>=0)
						{
							allloca1.removeElementAt(dex);
							allloca2.removeElementAt(dex);
						}
						if(lastAnswer
						&&((("!"+lastOle).equals(locflags[i][1]))
							||(lastOle.equals("!"+locflags[i][1]))))
						{
							lastAnswer=false;
							lastOle="";
							continue;
						}

						boolean there=false;
						if(oldVal.startsWith(locflags[i][1].toUpperCase())
						||(oldVal.indexOf(" "+locflags[i][1].toUpperCase())>=0))
							there=true;
						lastAnswer=false;
						lastOle=locflags[i][1];
						if(mob.session().confirm(locflags[i][0]
												 +" "
												 +(there?"(Y/n)":"(y/N)")
												 +"?",
												 there?"Y":"N"))
						{
							lastAnswer=true;
							s2.append(" "+lastOle);
						}
					}
					String restLoca=Util.combine(allloca1,0).trim();
					restLoca=mob.session().prompt("Enter any other location masks ("+restLoca+"): ",restLoca);
					oldLaw[Law.BIT_CRIMELOCS]=(s2.toString()+" "+restLoca).trim();
					break;
				}
			case 2:
				{
					StringBuffer s2=new StringBuffer("");
					String oldVal=oldLaw[Law.BIT_CRIMEFLAGS].toUpperCase();
					String lastOle="";
					boolean lastAnswer=false;
					for(int i=0;i<lawflags.length;i++)
					{
						if(lastAnswer
						&&((("!"+lastOle).equals(lawflags[i][1]))
							||(lastOle.equals("!"+lawflags[i][1]))))
						{
							lastAnswer=false;
							lastOle="";
							continue;
						}

						boolean there=false;
						if(oldVal.startsWith(lawflags[i][1].toUpperCase())
						||(oldVal.indexOf(" "+lawflags[i][1].toUpperCase())>=0))
							there=true;
						lastAnswer=false;
						lastOle=lawflags[i][1];
						if(mob.session().confirm(lawflags[i][0]
												 +" "
												 +(there?"(Y/n)":"(y/N)")
												 +"?",
												 there?"Y":"N"))
						{
							lastAnswer=true;
							s2.append(" "+lastOle);
						}
					}
					oldLaw[Law.BIT_CRIMEFLAGS]=s2.toString().trim();
					break;
				}
			}
		}
	}

	public void doIllegalEmotation(Area A, Behavior B, Law theLaw, MOB mob)
		throws IOException
	{
		if(mob.session()==null) return;
		mob.tell(getFromTOC("P10"+(theLaw.hasModifiableLaws()?"MOD":"")));
		while(true)
		{
			StringBuffer str=new StringBuffer("");
			str.append(Util.padRight("#  Words",20)+" "+shortLawHeader()+"\n\r");
			for(int x=0;x<theLaw.otherCrimes().size();x++)
			{
				String crime=Util.combine((Vector)theLaw.otherCrimes().elementAt(x),0);
				String[] set=(String[])theLaw.otherBits().elementAt(x);
				str.append(Util.padRight(""+(x+1)+". "+crime,20)+" "+shortLawDesc(set)+"\n\r");
			}
			str.append("A. ADD A NEW ONE\n\r");
			mob.session().colorOnlyPrintln(str.toString());
			if(!theLaw.hasModifiableLaws())
				break;
			String s=mob.session().prompt("\n\rEnter number to modify, A, or RETURN: ","");
			if(s.length()==0)
				break;
			else
			if(s.equalsIgnoreCase("A"))
			{
				s=mob.session().prompt("\n\rEnter some key words to make illegal: ","");
				if(s.length()>0)
				{
					String[] newValue=modifyLaw(A,B,theLaw,mob,null);
					if(newValue!=null)
					{
						StringBuffer s2=new StringBuffer("");
						for(int i=0;i<newValue.length;i++)
						{
							s2.append(newValue[i]);
							if(i<(newValue.length-1))
								s2.append(";");
						}
						changeTheLaw(A,B,mob,theLaw,"CRIME"+(theLaw.otherBits().size()+1),s2.toString());
						mob.tell("Added.");
					}
				}
			}
			else
			{
				int x=Util.s_int(s);
				String crimeName="";
				if((x>0)&&(x<=theLaw.otherCrimes().size()))
				{
					crimeName="CRIME"+x;
					String crimeWords=Util.combine((Vector)theLaw.otherCrimes().elementAt(x-1),0);
					String[] crimeSet=(String[])theLaw.otherBits().elementAt(x-1);
					String[] oldLaw=crimeSet;
					String[] newValue=modifyLaw(A,B,theLaw,mob,crimeSet);
					if(newValue!=oldLaw)
					{
						if(newValue!=null)
						{
							StringBuffer s2=new StringBuffer("");
							for(int i=0;i<newValue.length;i++)
							{
								s2.append(newValue[i]);
								if(i<(newValue.length-1))
									s2.append(";");
							}
							changeTheLaw(A,B,mob,theLaw,crimeName,crimeWords+";"+s2.toString());
							mob.tell("Changed.");
						}
						else
						{
							for(int i=x;i<theLaw.otherCrimes().size();i++)
							{
								StringBuffer s2=new StringBuffer("");
								crimeName="CRIME"+i;
								crimeWords=Util.combine((Vector)theLaw.otherCrimes().elementAt(i),0);
								newValue=(String[])theLaw.otherBits().elementAt(i);
								for(int v=0;v<newValue.length;v++)
								{
									s2.append(newValue[v]);
									if(v<(newValue.length-1))
										s2.append(";");
								}
								changeTheLaw(A,B,mob,theLaw,crimeName,crimeWords+";"+s2.toString());
							}
							changeTheLaw(A,B,mob,theLaw,"CRIME"+(theLaw.otherCrimes().size()),"");
							mob.tell("Changed.");
						}
					}
				}
				else
					break;
			}
		}
	}

	public void doIllegalSkill(Area A, Behavior B, Law theLaw, MOB mob)
		throws IOException
	{
		if(mob.session()==null) return;
		mob.tell(getFromTOC("P9"+(theLaw.hasModifiableLaws()?"MOD":"")));
		while(true)
		{
			StringBuffer str=new StringBuffer("");
			str.append(Util.padRight("#  Ability",20)+" "+shortLawHeader()+"\n\r");
			Hashtable filteredTable=new Hashtable();
			for(Enumeration e=theLaw.abilityCrimes().keys();e.hasMoreElements();)
			{
				String key=(String)e.nextElement();
				String[] set=(String[])theLaw.abilityCrimes().get(key);
				if(key.startsWith("$")) continue;
				Ability AB=CMClass.getAbility(key);
				if((AB==null)||(set==null)||(set.length<Law.BIT_NUMBITS)) continue;
				filteredTable.put(key.toUpperCase(),set);
			}
			int highest=0;
			for(Enumeration e=filteredTable.keys();e.hasMoreElements();)
			{
				String key=(String)e.nextElement();
				String[] set=(String[])filteredTable.get(key);
				Ability AB=CMClass.getAbility(key);
				str.append(Util.padRight(""+(highest+1)+". "+AB.name(),20)+" "+shortLawDesc(set)+"\n\r");
				highest++;
			}
			str.append("A. ADD A NEW ONE\n\r");
			mob.session().colorOnlyPrintln(str.toString());
			if(!theLaw.hasModifiableLaws())
				break;
			String s=mob.session().prompt("\n\rEnter number to modify, A, or RETURN: ","");
			if(s.length()==0)
				break;
			else
			if(s.equalsIgnoreCase("A"))
			{
				s=mob.session().prompt("\n\rEnter a skill name to make illegal: ","");
				if(s.length()>0)
				{
					Ability AB=(Ability)CMClass.findAbility(s);
					if(AB==null)
						mob.tell("That skill name is unknown.");
					else
					if(filteredTable.containsKey(AB.ID().toUpperCase()))
						mob.tell("That skill is already illegal.");
					else
					{
						String[] newValue=modifyLaw(A,B,theLaw,mob,null);
						if(newValue!=null)
						{
							StringBuffer s2=new StringBuffer("");
							for(int i=0;i<newValue.length;i++)
							{
								s2.append(newValue[i]);
								if(i<(newValue.length-1))
									s2.append(";");
							}
							changeTheLaw(A,B,mob,theLaw,AB.ID().toUpperCase(),s2.toString());
							mob.tell("Added.");
						}
					}
				}
			}
			else
			{
				int x=Util.s_int(s);
				String crimeName="";
				String[] crimeSet=null;
				int count=1;
				if((x>0)&&(x<=highest))
					for(Enumeration e=filteredTable.keys();e.hasMoreElements();)
					{
						String key=(String)e.nextElement();
						String[] set=(String[])filteredTable.get(key);
						if(count==x)
						{
							crimeName=key;
							crimeSet=set;
							break;
						}
						else
							count++;
					}
				if(crimeName.length()>0)
				{
					String[] oldLaw=crimeSet;
					String[] newValue=modifyLaw(A,B,theLaw,mob,crimeSet);
					if(newValue!=oldLaw)
					{
						StringBuffer s2=new StringBuffer("");
						if(newValue!=null)
							for(int i=0;i<newValue.length;i++)
							{
								s2.append(newValue[i]);
								if(i<(newValue.length-1))
									s2.append(";");
							}
						changeTheLaw(A,B,mob,theLaw,crimeName,s2.toString());
						mob.tell("Changed.");
					}
				}
				else
					break;
			}
		}
	}

	public void doIllegalInfluence(Area A, Behavior B, Law theLaw, MOB mob)
		throws IOException
	{
		if(mob.session()==null) return;
		mob.tell(getFromTOC("P8"+(theLaw.hasModifiableLaws()?"MOD":"")));
		while(true)
		{
			StringBuffer str=new StringBuffer("");
			str.append(Util.padRight("#  Effect",20)+" "+shortLawHeader()+"\n\r");
			Hashtable filteredTable=new Hashtable();
			for(Enumeration e=theLaw.abilityCrimes().keys();e.hasMoreElements();)
			{
				String key=(String)e.nextElement();
				String[] set=(String[])theLaw.abilityCrimes().get(key);
				if(!key.startsWith("$")) continue;
				Ability AB=CMClass.getAbility(key);
				if((AB==null)||(set==null)||(set.length<Law.BIT_NUMBITS)) continue;
				filteredTable.put(key,set);
			}
			int highest=0;
			for(Enumeration e=filteredTable.keys();e.hasMoreElements();)
			{
				String key=(String)e.nextElement();
				String[] set=(String[])filteredTable.get(key);
				Ability AB=CMClass.getAbility(key);
				str.append(Util.padRight(""+(highest+1)+". "+AB.name(),20)+" "+shortLawDesc(set)+"\n\r");
				highest++;
			}
			str.append("A. ADD A NEW ONE\n\r");
			mob.session().colorOnlyPrintln(str.toString());
			if(!theLaw.hasModifiableLaws())
				break;
			String s=mob.session().prompt("\n\rEnter number to modify, A, or RETURN: ","");
			if(s.length()==0)
				break;
			else
			if(s.equalsIgnoreCase("A"))
			{
				s=mob.session().prompt("\n\rEnter a skill name to make an illegal influence: ","");
				if(s.length()>0)
				{
					Ability AB=(Ability)CMClass.findAbility(s);
					if(AB==null)
						mob.tell("That skill name is unknown.");
					else
					if(filteredTable.containsKey("$"+AB.ID().toUpperCase()))
						mob.tell("That skill is already an illegal influence.");
					else
					{
						String[] newValue=modifyLaw(A,B,theLaw,mob,null);
						if(newValue!=null)
						{
							StringBuffer s2=new StringBuffer("");
							for(int i=0;i<newValue.length;i++)
							{
								s2.append(newValue[i]);
								if(i<(newValue.length-1))
									s2.append(";");
							}
							changeTheLaw(A,B,mob,theLaw,"$"+AB.ID().toUpperCase(),s2.toString());
							mob.tell("Added.");
						}
					}
				}
			}
			else
			{
				int x=Util.s_int(s);
				String crimeName="";
				String[] crimeSet=null;
				int count=1;
				if((x>0)&&(x<=highest))
					for(Enumeration e=filteredTable.keys();e.hasMoreElements();)
					{
						String key=(String)e.nextElement();
						String[] set=(String[])filteredTable.get(key);
						if(count==x)
						{
							crimeName=key;
							crimeSet=set;
							break;
						}
						else
							count++;
					}
				if(crimeName.length()>0)
				{
					String[] oldLaw=crimeSet;
					String[] newValue=modifyLaw(A,B,theLaw,mob,crimeSet);
					if(newValue!=oldLaw)
					{
						StringBuffer s2=new StringBuffer("");
						if(newValue!=null)
							for(int i=0;i<newValue.length;i++)
							{
								s2.append(newValue[i]);
								if(i<(newValue.length-1))
									s2.append(";");
							}
						changeTheLaw(A,B,mob,theLaw,crimeName,s2.toString());
						mob.tell("Changed.");
					}
				}
				else
					break;
			}
		}
	}

	public void doBasicLaw(Area A, Behavior B, Law theLaw, MOB mob)
		throws IOException
	{
		if(mob.session()==null) return;
		mob.tell(getFromTOC("P6"+(theLaw.hasModifiableLaws()?"MOD":"")));
		while(true)
		{
			StringBuffer str=new StringBuffer("");
			str.append(Util.padRight("#  Law Name",20)+" "+shortLawHeader()+"\n\r");
			str.append("1. ASSAULT           "+shortLawDesc((String[])theLaw.basicCrimes().get("ASSAULT"))+"\n\r");
			str.append("2. MURDER            "+shortLawDesc((String[])theLaw.basicCrimes().get("MURDER"))+"\n\r");
			str.append("3. NUDITY            "+shortLawDesc((String[])theLaw.basicCrimes().get("NUDITY"))+"\n\r");
			str.append("4. ARMED             "+shortLawDesc((String[])theLaw.basicCrimes().get("ARMED"))+"\n\r");
			str.append("5. RESISTING ARREST  "+shortLawDesc((String[])theLaw.basicCrimes().get("RESISTINGARREST"))+"\n\r");
			str.append("\n\r");
			mob.session().colorOnlyPrintln(str.toString());
			if(!theLaw.hasModifiableLaws())
				break;
			String s=mob.session().prompt("\n\rEnter number to modify or RETURN: ","");
			int x=Util.s_int(s);
			String crimeName="";
			if((x>0)&&(x<=5))
				switch(x)
				{
				case 1: crimeName="ASSAULT"; break;
				case 2: crimeName="MURDER"; break;
				case 3: crimeName="NUDITY"; break;
				case 4: crimeName="ARMED"; break;
				case 5: crimeName="RESISTINGARREST"; break;
				}
			if(crimeName.length()>0)
			{
				String[] oldLaw=(String[])theLaw.basicCrimes().get(crimeName);
				String[] newValue=modifyLaw(A,B,theLaw,mob,oldLaw);
				if(newValue!=oldLaw)
				{
					StringBuffer s2=new StringBuffer("");
					if(newValue!=null)
						for(int i=0;i<newValue.length;i++)
						{
							s2.append(newValue[i]);
							if(i<(newValue.length-1))
								s2.append(";");
						}
					changeTheLaw(A,B,mob,theLaw,crimeName,s2.toString());
					mob.tell("Changed.");
				}
			}
			else
				break;
		}
	}

	public void doParoleAndRelease(Area A, Behavior B, Law theLaw, MOB mob)
		throws IOException
	{
		if(mob.session()==null) return;
		mob.tell(getFromTOC("P5"+(theLaw.hasModifiableLaws()?"MOD":"")));
		while(true)
		{
			StringBuffer str=new StringBuffer("");
			str.append("1. LEVEL 1 PAROLE TIME: "+(Util.s_int(theLaw.getInternalStr("PAROLE1TIME"))*MudHost.TICK_TIME/1000)+" seconds.\n\r");
			str.append("2. LEVEL 2 PAROLE TIME: "+(Util.s_int(theLaw.getInternalStr("PAROLE2TIME"))*MudHost.TICK_TIME/1000)+" seconds.\n\r");
			str.append("3. LEVEL 3 PAROLE TIME: "+(Util.s_int(theLaw.getInternalStr("PAROLE3TIME"))*MudHost.TICK_TIME/1000)+" seconds.\n\r");
			str.append("4. LEVEL 4 PAROLE TIME: "+(Util.s_int(theLaw.getInternalStr("PAROLE4TIME"))*MudHost.TICK_TIME/1000)+" seconds.\n\r");
			str.append("\n\r");
			Vector V=theLaw.releaseRooms();
			if(Util.combine(V,0).equals("@"))
				V=new Vector();
			int highest=4;
			for(int v=0;v<V.size();v++)
			{
				String s=(String)V.elementAt(v);
				highest++;
				Room R=CMMap.getRoom(s);
				if(R!=null)
					str.append((5+v)+". RELEASE ROOM: "+R.displayText()+"\n\r");
				else
					str.append((5+v)+". RELEASE ROOM: Rooms called '"+s+"'.\n\r");
			}
			mob.session().colorOnlyPrintln(str.toString());
			if(!theLaw.hasModifiableLaws())
				break;
			String s=mob.session().prompt("\n\rEnter 'A' to add a new release room, or enter a number to modify: ","");
			boolean changed=false;
			if(s.equalsIgnoreCase("A"))
			{
				if(mob.location().getArea()!=A)
					mob.tell("You can not add this room as a release room, as it is not in the area.");
				else
				if(mob.session().confirm("Add this room as a new release room (y/N)? ","N"))
				{
					V.addElement(CMMap.getExtendedRoomID(mob.location()));
					changed=true;
				}
			}
			else
			{
				int x=Util.s_int(s);
				if((x>0)&&(x<=highest))
				{
					if(x>4)
					{
						if(mob.session().confirm("Remove this room as a release room (y/N)? ","N"))
						{
							V.removeElementAt(x-5);
							changed=true;
						}
					}
					else
					{
						long oldTime=Util.s_int(theLaw.getInternalStr("PAROLE"+x+"TIME"))*MudHost.TICK_TIME/1000;
						s=mob.session().prompt("Enter a new number of seconds ("+oldTime+"): ",""+oldTime);
						if((Util.s_int(s)!=oldTime)&&(Util.s_int(s)>0))
						{
							long x1=Util.s_int(s);
							x1=x1*1000/MudHost.TICK_TIME;
							changeTheLaw(A,B,mob,theLaw,"PAROLE"+x+"TIME",""+x1);
							mob.tell("Changed.");
						}
					}
				}
				else
					break;
			}
			if(changed)
			{
				StringBuffer s2=new StringBuffer("");
				for(int v=0;v<V.size();v++)
					s2.append(((String)V.elementAt(v))+";");
				if(s2.length()==0)
					s2.append("@");
				else
					s2.deleteCharAt(s2.length()-1);
				changeTheLaw(A,B,mob,theLaw,"RELEASEROOM",s2.toString());
				mob.tell("Changed.");
			}
		}
	}


	public void doJailPolicy(Area A, Behavior B, Law theLaw, MOB mob)
		throws IOException
	{
		if(mob.session()==null) return;
		mob.tell(getFromTOC("P4"+(theLaw.hasModifiableLaws()?"MOD":"")));
		while(true)
		{
			StringBuffer str=new StringBuffer("");
			str.append("1. LEVEL 1 JAIL TIME: "+(Util.s_int(theLaw.getInternalStr("JAIL1TIME"))*MudHost.TICK_TIME/1000)+" seconds.\n\r");
			str.append("2. LEVEL 2 JAIL TIME: "+(Util.s_int(theLaw.getInternalStr("JAIL2TIME"))*MudHost.TICK_TIME/1000)+" seconds.\n\r");
			str.append("3. LEVEL 3 JAIL TIME: "+(Util.s_int(theLaw.getInternalStr("JAIL3TIME"))*MudHost.TICK_TIME/1000)+" seconds.\n\r");
			str.append("4. LEVEL 4 JAIL TIME: "+(Util.s_int(theLaw.getInternalStr("JAIL4TIME"))*MudHost.TICK_TIME/1000)+" seconds.\n\r");
			str.append("\n\r");
			Vector V=theLaw.jailRooms();
			if(Util.combine(V,0).equals("@"))
				V=new Vector();
			int highest=4;
			for(int v=0;v<V.size();v++)
			{
				String s=(String)V.elementAt(v);
				highest++;
				Room R=CMMap.getRoom(s);
				if(R!=null)
					str.append((5+v)+". JAIL ROOM: "+R.displayText()+"\n\r");
				else
					str.append((5+v)+". JAIL ROOM: Rooms called '"+s+"'.\n\r");
			}
			mob.session().colorOnlyPrintln(str.toString());
			if(!theLaw.hasModifiableLaws())
				break;
			String s=mob.session().prompt("\n\rEnter 'A' to add a new jail room, or enter a number to modify: ","");
			boolean changed=false;
			if(s.equalsIgnoreCase("A"))
			{
				if(mob.location().getArea()!=A)
					mob.tell("You can not add this room as a jail, as it is not in the area.");
				else
				if(mob.session().confirm("Add this room as a new jail room (y/N)? ","N"))
				{
					V.addElement(CMMap.getExtendedRoomID(mob.location()));
					changed=true;
				}
			}
			else
			{
				int x=Util.s_int(s);
				if((x>0)&&(x<=highest))
				{
					if(x>4)
					{
						if(mob.session().confirm("Remove this room as a jail room (y/N)? ","N"))
						{
							V.removeElementAt(x-5);
							changed=true;
						}
					}
					else
					{
						long oldTime=Util.s_int(theLaw.getInternalStr("JAIL"+x+"TIME"))*MudHost.TICK_TIME/1000;
						s=mob.session().prompt("Enter a new number of seconds ("+oldTime+"): ",""+oldTime);
						if((Util.s_int(s)!=oldTime)&&(Util.s_int(s)>0))
						{
							long x1=Util.s_int(s);
							x1=x1*1000/MudHost.TICK_TIME;
							changeTheLaw(A,B,mob,theLaw,"JAIL"+x+"TIME",""+x1);
							mob.tell("Changed.");
						}
					}
				}
				else
					break;
			}
			if(changed)
			{
				StringBuffer s2=new StringBuffer("");
				for(int v=0;v<V.size();v++)
					s2.append(((String)V.elementAt(v))+";");
				if(s2.length()==0)
					s2.append("@");
				else
					s2.deleteCharAt(s2.length()-1);
				changeTheLaw(A,B,mob,theLaw,"JAIL",s2.toString());
				mob.tell("Changed.");
			}
		}
	}


	public void doTresspassingLaw(Area A, Behavior B, Law theLaw, MOB mob)
		throws IOException
	{
		if(mob.session()==null) return;
		mob.tell(getFromTOC("P7"+(theLaw.hasModifiableLaws()?"MOD":"")));
		while(true)
		{
			mob.tell("1. Trespassers : "+MUDZapper.zapperDesc(theLaw.getInternalStr("TRESPASSERS")));
			mob.tell("2. Law         : "+shortLawDesc((String[])theLaw.basicCrimes().get("TRESPASSING")));
			if(!theLaw.hasModifiableLaws())
				return;
			String prompt=mob.session().choose("Enter one to change or RETURN: ","12\n","\n");
			int x=Util.s_int(prompt);
			if((x<=0)||(x>2))
				return;
			if(x==1)
			{
				String s="?";
				while(s.trim().equals("?"))
				{
					s=mob.session().prompt("Enter a new mask, ? for help, or RETURN=["+theLaw.getInternalStr("TRESPASSERS")+"]\n\r: ",theLaw.getInternalStr("TRESPASSERS"));
					if(s.trim().equals("?"))
						mob.tell(MUDZapper.zapperInstructions("\n\r","arrests"));
					else
					if(!s.equals(theLaw.getInternalStr("TRESPASSERS")))
					{
						changeTheLaw(A,B,mob,theLaw,"TRESPASSERS",s);
						mob.tell("Changed.");
					}
				}
			}
			else
			if(x==2)
			{
				String[] oldLaw=(String[])theLaw.basicCrimes().get("TRESPASSING");
				String[] newValue=modifyLaw(A,B,theLaw,mob,oldLaw);
				if(newValue!=oldLaw)
				{
					StringBuffer s2=new StringBuffer("");
					if(newValue!=null)
						for(int i=0;i<newValue.length;i++)
						{
							s2.append(newValue[i]);
							if(i<(newValue.length-1))
								s2.append(";");
						}
					changeTheLaw(A,B,mob,theLaw,"TRESPASSING",s2.toString());
					mob.tell("Changed.");
				}
			}
		}
	}

	public void doVictimsOfCrime(Area A, Behavior B, Law theLaw, MOB mob)
		throws IOException
	{
		if(mob.session()==null) return;
		mob.tell(getFromTOC("P3"+(theLaw.hasModifiableLaws()?"MOD":"")));
		mob.tell("Protected victims: "+MUDZapper.zapperDesc(theLaw.getInternalStr("PROTECTED")));
		if(theLaw.hasModifiableLaws())
		{
			String s="?";
			while(s.trim().equals("?"))
			{
				s=mob.session().prompt("Enter a new mask, ? for help, or RETURN=["+theLaw.getInternalStr("PROTECTED")+"]\n\r: ",theLaw.getInternalStr("PROTECTED"));
				if(s.trim().equals("?"))
					mob.tell(MUDZapper.zapperInstructions("\n\r","protects"));
				else
				if(!s.equals(theLaw.getInternalStr("PROTECTED")))
				{
					changeTheLaw(A,B,mob,theLaw,"PROTECTED",s);
					mob.tell("Changed.");
				}
			}
		}
	}

	public void doOfficersAndJudges(Area A, Behavior B, Law theLaw, MOB mob)
		throws IOException
	{
		if(mob.session()==null) return;
		mob.tell(getFromTOC("P2"+(theLaw.hasModifiableLaws()?"MOD":"")+(theLaw.hasModifiableNames()?"NAM":"")));
		String duhJudge="No Judge Found!\n\r";
		StringBuffer duhOfficers=new StringBuffer("");
		for(Enumeration e=A.getMap();e.hasMoreElements();)
		{
			Room R=(Room)e.nextElement();
			for(int i=0;i<R.numInhabitants();i++)
			{
				MOB M=(MOB)R.fetchInhabitant(i);
				if(M!=null)
				{
					Room R2=M.getStartRoom();
					if(R==null) R=M.location();
					if(B.modifyBehavior(A,M,new Integer(Law.MOD_ISOFFICER)))
						duhOfficers.append(M.name()+" from room '"+R2.displayText()+"'\n\r");
					else
					if(B.modifyBehavior(A,M,new Integer(Law.MOD_ISJUDGE)))
						duhJudge=M.name()+" from room '"+R2.displayText()+"'\n\r";
				}
			}
		}
		if(duhOfficers.length()==0) duhOfficers.append("No Officers Found!\n\r");
		mob.tell("1. Area Judge: \n\r"+duhJudge+"\n\r2. Area Officers: \n\r"+duhOfficers.toString());
		if(theLaw.hasModifiableNames()&&theLaw.hasModifiableLaws())
		{
			int w=Util.s_int(mob.session().choose("Enter one to modify, or RETURN to cancel: ","12\n",""));
			if(w==0) return;
			String modifiableTag=(w==1)?"JUDGE":"OFFICERS";
			String s=mob.session().prompt("Enter key words from officials name(s) ["+theLaw.getInternalStr(modifiableTag)+"]\n\r: ",theLaw.getInternalStr(modifiableTag));
			if(!s.equals(theLaw.getInternalStr(modifiableTag)))
			{
				changeTheLaw(A,B,mob,theLaw,modifiableTag,s);
				mob.tell("Changed.");
			}
		}
	}
}
