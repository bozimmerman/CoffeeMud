package com.planet_ink.coffee_mud.Items.Basic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;
import java.io.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
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
				Behavior B=CoffeeUtensils.getLegalBehavior(A);
				if(B==null)
				{
					msg.source().tell("The pages appear blank, and damaged.");
					return;
				}
				Vector VB=new Vector();
				Area A2=CoffeeUtensils.getLegalObject(A);
				VB.addElement(new Integer(Law.MOD_LEGALINFO));
				B.modifyBehavior(A2,mob,VB);
				Law theLaw=(Law)VB.firstElement();

				int which=-1;
				if(Util.s_long(msg.targetMessage())>0)
					which=Util.s_int(msg.targetMessage());

				boolean allowedToModify=(CMSecurity.isAllowed(mob,mob.location(),"ABOVELAW"));
				if(A.getMetroMap().hasMoreElements())
					allowedToModify=(CMSecurity.isAllowed(mob,((Room)A.getMetroMap().nextElement()),"ABOVELAW"));
				Vector V=new Vector();
				V.addElement(new Integer(Law.MOD_RULINGCLAN));
				if((!allowedToModify)
				&&(B.modifyBehavior(A2,mob,V))
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
					changeTheLaw(A2,B,mob,theLaw,"ACTIVATED","TRUE");

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
					case 2:	doOfficersAndJudges(A,B,A2,theLaw,mob); break;
					case 3:	doVictimsOfCrime(A,B,theLaw,mob); break;
					case 4: doJailPolicy(A,B,theLaw,mob); break;
					case 5: doParoleAndRelease(A,B,theLaw,mob); break;
					case 6: doBasicLaw(A,B,theLaw,mob); break;
					case 7: doTresspassingLaw(A,B,theLaw,mob); break;
					case 8: doIllegalInfluence(A,B,theLaw,mob); break;
					case 9: doIllegalSkill(A,B,theLaw,mob); break;
					case 10: doIllegalEmotation(A,B,theLaw,mob); break;
					case 11: doTaxLaw(A,B,theLaw,mob); break;
					case 12: doBannedSubstances(A,B,theLaw,mob); break;
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
				Area A2=CoffeeUtensils.getLegalObject(A);
				if(A2==null)
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

	public void changeTheLaw(Environmental A,
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
                    {
                        String sentence=Law.ACTION_DESCS[i];
						msg.append(sentence.toLowerCase()+" ");
                    }
                    String oldSentence="";
                    Vector V=Util.parse(oldLaw[Law.BIT_SENTENCE]);
                    DVector V2=new DVector(2);
                    for(int v=0;v<V.size();v++)
                    {
                        String t=(String)V.elementAt(v);
                        boolean sent=false;
                        for(int i=0;i<Law.ACTION_DESCS.length;i++)
                        {
                            if(Law.ACTION_DESCS[i].startsWith(t.toUpperCase()))
                            {
                                oldSentence=t.toLowerCase();
                                sent=true;
                                V2.addElement(oldSentence,"");
                                break;
                            }
                        }
                        if(!sent)
                        {
                            for(int i=0;i<Law.ACTIONMASK_DESCS.length;i++)
                            {
                                if(t.toUpperCase().startsWith(Law.ACTIONMASK_DESCS[i]))
                                {
                                    int x1=t.indexOf("=");
                                    if(x1>0)
                                        V2.addElement(Law.ACTIONMASK_DESCS[i].toLowerCase(),t.substring(x1+1));
                                    else
                                        V2.addElement(Law.ACTIONMASK_DESCS[i],"");
                                    break;
                                }
                            }
                        }
                    }
					msg.append("\n\rSelect a sentence ("+oldSentence+"): ");
					String t=mob.session().prompt(msg.toString(),oldSentence);
					for(int i=0;i<Law.ACTION_DESCS.length;i++)
					{
						if(Law.ACTION_DESCS[i].startsWith(t.toUpperCase()))
						{
                            int x1=V2.indexOf(oldSentence);
                            oldSentence=Law.ACTION_DESCS[i].toLowerCase();
                            V2.setElementAt(x1,1,oldSentence);
                            V2.setElementAt(x1,2,"");
                            t=null;
							break;
						}
					}
                    if(t==null)
                    {
                        while(t==null)
                        {
                            msg=new StringBuffer("Sentence Flags ( ");
                            for(int i=0;i<Law.ACTIONMASK_DESCS.length;i++)
                            {
                                String sentence=Law.ACTIONMASK_DESCS[i];
                                if(sentence.indexOf("=")>0) sentence=sentence.substring(0,sentence.indexOf("="));
                                msg.append(sentence.toLowerCase()+" ");
                            }
                            StringBuffer oldFlags=new StringBuffer("");
                            for(int v=0;v<V2.size();v++)
                            {
                                t=(String)V2.elementAt(v,1);
                                if(t.equalsIgnoreCase(oldSentence)) continue;
                                oldFlags.append(t+((String)V2.elementAt(v,2))+" ");
                            }
                            msg.append("\n\rSelect a flag to toggle or RETURN ("+oldFlags+"): ");
                            int selectedMask=-1;
                            t=mob.session().prompt(msg.toString(),"");
                            if(t.length()==0) break;
                            int indexIfExists=-1;
                            for(int i=0;i<Law.ACTIONMASK_DESCS.length;i++)
                            {
                                if(Law.ACTIONMASK_DESCS[i].startsWith(t.toUpperCase()))
                                {
                                    selectedMask=i;
                                    indexIfExists=V2.indexOf(Law.ACTIONMASK_DESCS[selectedMask].toLowerCase());
                                    t=null;
                                    break;
                                }
                            }
                            if(t==null)
                            {
                                if(indexIfExists>=0)
                                {
                                    mob.tell("'"+V2.elementAt(indexIfExists,1)+"' has been removed.");
                                    V2.removeElementAt(indexIfExists);
                                }
                                else
                                {
                                    String parm="";
                                    boolean abort=false;
                                    switch(Law.ACTIONMASK_CODES[selectedMask])
                                    {
                                    case Law.ACTIONMASK_DETAIN:
                                        if(!CoffeeUtensils.getLegalObject(A).inMetroArea(mob.location().getArea()))
                                        {
                                            mob.tell("You can not add this room as a detention center, as it is not in the area.");
                                            abort=true;
                                        }
                                        else
                                        if(mob.session().confirm("Add this room as a new detention center room (y/N)? ","N"))
                                        {
                                            String time=mob.session().prompt("Enter the amount of time before they are released: ","");
                                            if((time.length()==0)||(!Util.isInteger(time))||(Util.s_int(time)<0)||(Util.s_int(time)>10000))
                                            {
                                                mob.tell("Invalid entry.  Aborted.");
                                                abort=true;
                                            }
                                            else
                                                parm=CMMap.getExtendedRoomID(mob.location())+","+time;
                                        }
                                        else
                                            abort=true;
                                        break;
                                    case Law.ACTIONMASK_FINE:
                                    {
                                        String fine=mob.session().prompt("Enter the amount of the fine in base-gold value: ","");
                                        if((fine.length()==0)||(!Util.isNumber(fine))||(Util.s_double(fine)<0)||(Util.s_double(fine)>100000.0))
                                        {
                                            mob.tell("Invalid entry.  Aborted.");
                                            abort=true;
                                        }
                                        else
                                            parm=fine;
                                        break;
                                    }
                                    }
                                    if(!abort)
                                    {
                                        V2.addElement(Law.ACTIONMASK_DESCS[selectedMask],parm);
                                        mob.tell("'"+V2.elementAt(V2.size()-1,1)+parm+"' has been added.");
                                    }
                                    else
                                        mob.tell("'"+V2.elementAt(V2.size()-1,1)+parm+"' has been aborted.");
                                }
                            }
                            else
                                mob.tell("'"+t+"' is not a valid flag.  Unchanged.");
                        }
                        StringBuffer newSentence=new StringBuffer("");
                        for(int v2=0;v2<V2.size();v2++)
                        {
                            t=(String)V2.elementAt(v2,1);
                            String p=(String)V2.elementAt(v2,2);
                            if(p.indexOf(" ")>0)
                                newSentence.append("\""+t+p+"\" ");
                            else
                                newSentence.append(t+p+" ");
                        }
                        oldLaw[Law.BIT_SENTENCE]=newSentence.toString().trim();
                    }
                    else
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
					String restLoca=Util.combineWithQuotes(allloca1,0).trim();
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
				String crime=Util.combineWithQuotes((Vector)theLaw.otherCrimes().elementAt(x),0);
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
						StringBuffer s2=new StringBuffer(s+";");
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
				if((x>0)&&(x<=theLaw.otherCrimes().size()))
				{
					String[] crimeSet=(String[])theLaw.otherBits().elementAt(x-1);
					String[] oldLaw=crimeSet;
					String[] newValue=modifyLaw(A,B,theLaw,mob,crimeSet);
					if(newValue!=oldLaw)
					{
						if(newValue!=null) 
							theLaw.otherBits().setElementAt(newValue,x-1);
						else
						{
							theLaw.otherCrimes().removeElementAt(x-1);
							theLaw.otherBits().removeElementAt(x-1);
						}
						String[] newBits=new String[theLaw.otherBits().size()];
						for(int c=0;c<theLaw.otherCrimes().size();c++)
						{
							String crimeWords=Util.combineWithQuotes((Vector)theLaw.otherCrimes().elementAt(c),0);
							String[] thisLaw=(String[])theLaw.otherBits().elementAt(c);
							StringBuffer s2=new StringBuffer("");
							for(int i=0;i<thisLaw.length;i++)
							{
								s2.append(thisLaw[i]);
								if(i<(thisLaw.length-1))
									s2.append(";");
							}
							newBits[c]=crimeWords+";"+s2.toString();
						}
						for(int c=0;c<newBits.length;c++)
							changeTheLaw(A,B,mob,theLaw,"CRIME"+(c+1),newBits[c]);
						changeTheLaw(A,B,mob,theLaw,"CRIME"+(newBits.length+1),"");
						if(newValue!=null)
							mob.tell("Changed.");
						else
							mob.tell("Deleted.");
					}
				}
				else
					break;
			}
		}
	}

	public void doBannedSubstances(Area A, Behavior B, Law theLaw, MOB mob)
	throws IOException
	{
		if(mob.session()==null) return;
		mob.tell(getFromTOC("P10"+(theLaw.hasModifiableLaws()?"MOD":"")));
		while(true)
		{
			StringBuffer str=new StringBuffer("");
			str.append(Util.padRight("#  Items",20)+" "+shortLawHeader()+"\n\r");
			for(int x=0;x<theLaw.bannedSubstances().size();x++)
			{
				String crime=Util.combineWithQuotes((Vector)theLaw.bannedSubstances().elementAt(x),0);
				String[] set=(String[])theLaw.bannedBits().elementAt(x);
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
				s=mob.session().prompt("\n\rEnter item key words or resource types to make illegal (?)\n\r: ","");
				if(s.equals("?"))
				    mob.tell("Valid resources: "+Util.toStringList(EnvResource.RESOURCE_DESCS));
				else
				if(s.length()>0)
				{
				    s=s.toUpperCase();
				    boolean resource=false;
				    for(int i=0;i<EnvResource.RESOURCE_DESCS.length;i++)
				    {
				        if(EnvResource.RESOURCE_DESCS[i].equals(s))
				            resource=true;
				    }
				    if(resource||mob.session().confirm("'"+s+"' is not a known resource.  Add as a key word anyway (y/N)?","N"))
				    {
						String[] newValue=modifyLaw(A,B,theLaw,mob,null);
						if(newValue!=null)
						{
							StringBuffer s2=new StringBuffer(s+";");
							for(int i=0;i<newValue.length;i++)
							{
								s2.append(newValue[i]);
								if(i<(newValue.length-1))
									s2.append(";");
							}
							changeTheLaw(A,B,mob,theLaw,"BANNED"+(theLaw.bannedBits().size()+1),s2.toString());
							mob.tell("Added.");
						}
				    }
				}
			}
			else
			{
				int x=Util.s_int(s);
				if((x>0)&&(x<=theLaw.bannedSubstances().size()))
				{
					String[] crimeSet=(String[])theLaw.bannedBits().elementAt(x-1);
					String[] oldLaw=crimeSet;
					String[] newValue=modifyLaw(A,B,theLaw,mob,crimeSet);
					if(newValue!=oldLaw)
					{
						if(newValue!=null) 
							theLaw.bannedBits().setElementAt(newValue,x-1);
						else
						{
							theLaw.bannedSubstances().removeElementAt(x-1);
							theLaw.bannedBits().removeElementAt(x-1);
						}
						String[] newBits=new String[theLaw.bannedBits().size()];
						for(int c=0;c<theLaw.bannedSubstances().size();c++)
						{
							String crimeWords=Util.combineWithQuotes((Vector)theLaw.bannedSubstances().elementAt(c),0);
							String[] thisLaw=(String[])theLaw.bannedBits().elementAt(c);
							StringBuffer s2=new StringBuffer("");
							for(int i=0;i<thisLaw.length;i++)
							{
								s2.append(thisLaw[i]);
								if(i<(thisLaw.length-1))
									s2.append(";");
							}
							newBits[c]=crimeWords+";"+s2.toString();
						}
						for(int c=0;c<newBits.length;c++)
							changeTheLaw(A,B,mob,theLaw,"BANNED"+(c+1),newBits[c]);
						changeTheLaw(A,B,mob,theLaw,"BANNED"+(newBits.length+1),"");
						if(newValue!=null)
							mob.tell("Changed.");
						else
							mob.tell("Deleted.");
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
				if(((AB==null)
                    &&(CoffeeUtensils.getAbilityType(key)<0)
                    &&(CoffeeUtensils.getAbilityDomain(key)<0))
                ||(set==null)
                ||(set.length<Law.BIT_NUMBITS)) 
                    continue;
				filteredTable.put(key.toUpperCase(),set);
			}
			int highest=0;
			for(Enumeration e=filteredTable.keys();e.hasMoreElements();)
			{
				String key=(String)e.nextElement();
				String[] set=(String[])filteredTable.get(key);
				Ability AB=CMClass.getAbility(key);
                String name=(AB!=null)?AB.name():key;
				str.append(Util.padRight(""+(highest+1)+". "+name,20)+" "+shortLawDesc(set)+"\n\r");
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
					Ability AB=CMClass.findAbility(s);
                    if(AB!=null) s=AB.ID();
					if((AB==null)
                    &&(CoffeeUtensils.getAbilityType(s)<0)
                    &&(CoffeeUtensils.getAbilityDomain(s)<0))
						mob.tell("That skill name or skill class is unknown.");
					else
					if(filteredTable.containsKey(s.toUpperCase()))
						mob.tell("That skill or skill class is already illegal.");
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
							changeTheLaw(A,B,mob,theLaw,s.toUpperCase(),s2.toString());
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

	public void doTaxLaw(Area A, Behavior B, Law theLaw, MOB mob)
	throws IOException
	{
		if(mob.session()==null) return;
		mob.tell(getFromTOC("P11"+(theLaw.hasModifiableLaws()?"MOD":"")));
		while(true)
		{
			StringBuffer str=new StringBuffer("");
			str.append("1. PROPERTY TAX   : "+(Util.s_double(theLaw.getInternalStr("PROPERTYTAX")))+"%\n\r");
			str.append("2. SALES TAX      : "+(Util.s_double(theLaw.getInternalStr("SALESTAX")))+"%\n\r");
			str.append("3. CITIZEN TAX    : "+(Util.s_double(theLaw.getInternalStr("CITTAX")))+"%\n\r");
			str.append("4. TAX EVASION    : "+shortLawDesc((String[])theLaw.taxLaws().get("TAXEVASION"))+"\n\r");
			str.append("5. TREASURY       : ");
			String S=theLaw.getInternalStr("TREASURY").trim();
			String room="*";
			String item="";
			Vector V=Util.parseSemicolons(S,false);
			if((S.length()==0)||(V.size()==0))
				str.append("Not defined");
			else
			{
				room=(String)V.firstElement();
				if(V.size()>1) item=Util.combine(V,1);
				if(room.equalsIgnoreCase("*")) 
					str.append("Any (*)");
				else
				{
					Room R=CMMap.getRoom(room);
					if(R==null)
						str.append("Unknown");
					else
						str.append(R.displayText()+" ("+R.roomID()+")");
				}
				if(item.length()>0)
					str.append(". Container: "+item+"\n\r");
				else
					str.append("\n\r");
			}
			mob.session().colorOnlyPrintln(str.toString());
			if(!theLaw.hasModifiableLaws())
				break;
			String s=mob.session().prompt("\n\rEnter a number to modify: ","");
			int x=Util.s_int(s);
			if(x==0) break;
			switch(x)
			{
			case 1:
				s=mob.session().prompt("Enter a new tax amount: ",theLaw.getInternalStr("PROPERTYTAX"));
				if(Util.s_double(s)!=Util.s_double(theLaw.getInternalStr("PROPERTYTAX")))
				{
					changeTheLaw(A,B,mob,theLaw,"PROPERTYTAX",""+Util.s_double(s));
					mob.tell("Changed.");
				}
				break;
			case 2:
				s=mob.session().prompt("Enter a new tax amount: ",theLaw.getInternalStr("SALESTAX"));
				if(Util.s_double(s)!=Util.s_double(theLaw.getInternalStr("SALESTAX")))
				{
					changeTheLaw(A,B,mob,theLaw,"SALESTAX",""+Util.s_double(s));
					mob.tell("Changed.");
				}
				break;
			case 3:
				s=mob.session().prompt("Enter a new tax amount: ",theLaw.getInternalStr("CITTAX"));
				if(Util.s_double(s)!=Util.s_double(theLaw.getInternalStr("CITTAX")))
				{
					changeTheLaw(A,B,mob,theLaw,"CITTAX",""+Util.s_double(s));
					mob.tell("Changed.");
				}
				break;
			case 4:
				{
					String[] oldLaw=(String[])theLaw.taxLaws().get("TAXEVASION");
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
						changeTheLaw(A,B,mob,theLaw,"TAXEVASION",s2.toString());
						mob.tell("Changed.");
					}
					break;
				}
			case 5:
				{
				    String room2="/";
					while((room2.equals("/"))||(!room2.equals("*"))&&(room2.length()>0)&&(CMMap.getRoom(room2)==null))
					    room2=mob.session().prompt("Enter a new room ID (RETURN="+room+", *=any): ",room);
					String item2=mob.session().prompt("Enter an optional container name (RETURN="+item+"): ",item);
					if((!room.equalsIgnoreCase(room2))||(!item.equalsIgnoreCase(item2)))
					{
						changeTheLaw(A,B,mob,theLaw,"TREASURY",""+room2+";"+item2);
						mob.tell("Changed.");
					}
					break;
				}
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
				Ability AB=CMClass.getAbility(key.substring(1));
				if(((AB==null)
                    &&(CoffeeUtensils.getAbilityType(key.substring(1))<0)
                    &&(CoffeeUtensils.getAbilityDomain(key.substring(1))<0))
                ||(set==null)
                ||(set.length<Law.BIT_NUMBITS)) continue;
				filteredTable.put(key,set);
			}
			int highest=0;
			for(Enumeration e=filteredTable.keys();e.hasMoreElements();)
			{
				String key=(String)e.nextElement();
				String[] set=(String[])filteredTable.get(key);
				Ability AB=CMClass.getAbility(key.substring(1));
                String name=(AB!=null)?AB.name():key.substring(1);
				str.append(Util.padRight(""+(highest+1)+". "+name,20)+" "+shortLawDesc(set)+"\n\r");
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
					Ability AB=CMClass.findAbility(s);
                    if(AB!=null)s=AB.ID();
					if((AB==null)
                    &&(CoffeeUtensils.getAbilityType(s)<0)
                    &&(CoffeeUtensils.getAbilityDomain(s)<0))
						mob.tell("That skill name or skill class is unknown.");
					else
					if(filteredTable.containsKey("$"+s.toUpperCase()))
						mob.tell("That skill or skill class is already an illegal influence.");
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
							changeTheLaw(A,B,mob,theLaw,"$"+s.toUpperCase(),s2.toString());
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
			str.append("6. ROBBING HOMES     "+shortLawDesc((String[])theLaw.basicCrimes().get("PROPERTYROB"))+"\n\r");
			str.append("\n\r");
			mob.session().colorOnlyPrintln(str.toString());
			if(!theLaw.hasModifiableLaws())
				break;
			String s=mob.session().prompt("\n\rEnter number to modify or RETURN: ","");
			int x=Util.s_int(s);
			String crimeName="";
			if((x>0)&&(x<=6))
				switch(x)
				{
				case 1: crimeName="ASSAULT"; break;
				case 2: crimeName="MURDER"; break;
				case 3: crimeName="NUDITY"; break;
				case 4: crimeName="ARMED"; break;
				case 5: crimeName="RESISTINGARREST"; break;
				case 6: crimeName="PROPERTYROB"; break;
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
				if(CoffeeUtensils.getLegalObject(A).inMetroArea(mob.location().getArea()))
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
				if(!CoffeeUtensils.getLegalObject(A).inMetroArea(mob.location().getArea()))
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

	public void doOfficersAndJudges(Area A, 
							        Behavior B,
							        Environmental legalO,
							        Law theLaw, 
							        MOB mob)
		throws IOException
	{
		if(mob.session()==null) return;
		mob.tell(getFromTOC("P2"+(theLaw.hasModifiableLaws()?"MOD":"")+(theLaw.hasModifiableNames()?"NAM":"")));
		String duhJudge="No Judge Found!\n\r";
		StringBuffer duhOfficers=new StringBuffer("");
		for(Enumeration e=A.getMetroMap();e.hasMoreElements();)
		{
			Room R=(Room)e.nextElement();
			for(int i=0;i<R.numInhabitants();i++)
			{
				MOB M=R.fetchInhabitant(i);
				if(M!=null)
				{
					Room R2=M.getStartRoom();
					if(R==null) R=M.location();
					if(B.modifyBehavior(legalO,M,new Integer(Law.MOD_ISOFFICER)))
						duhOfficers.append(M.name()+" from room '"+R2.displayText()+"'\n\r");
					else
					if(B.modifyBehavior(legalO,M,new Integer(Law.MOD_ISJUDGE)))
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
