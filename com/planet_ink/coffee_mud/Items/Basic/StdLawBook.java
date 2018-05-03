package com.planet_ink.coffee_mud.Items.Basic;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/*
   Copyright 2003-2018 Bo Zimmerman

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
	@Override
	public String ID()
	{
		return "StdLawBook";
	}

	public StdLawBook()
	{
		super();
		setName("a law book");
		setDisplayText("a law book sits here.");
		setDescription("Enter `READ [PAGE NUMBER] \"law book\"` to read an entry.%0D%0AUse your WRITE skill to add new entries. ");
		material=RawMaterial.RESOURCE_PAPER;
		basePhyStats().setSensesMask(PhyStats.SENSE_ITEMREADABLE);
		recoverPhyStats();
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(msg.amITarget(this))
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_WRITE:
		case CMMsg.TYP_REWRITE:
			msg.source().tell(L("You are not allowed to write on @x1. Try reading it.",name()));
			return false;
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		final MOB mob=msg.source();
		if(msg.amITarget(this))
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_READ:
			if(!CMLib.flags().canBeSeenBy(this,mob))
				mob.tell(L("You can't see that!"));
			else
			if(!mob.isMonster())
			{
				final Area A=CMLib.map().getArea(readableText());
				final LegalBehavior B=CMLib.law().getLegalBehavior(A);
				if(B==null)
				{
					msg.source().tell(L("The pages appear blank, and damaged."));
					return;
				}

				final Area A2=CMLib.law().getLegalObject(A);
				final Law theLaw=B.legalInfo(A2);
				if(theLaw==null)
				{
					msg.source().tell(L("There is no law here."));
					return;
				}

				int which=-1;
				if(CMath.s_long(msg.targetMessage())>0)
					which=CMath.s_int(msg.targetMessage());

				boolean allowedToModify=(CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.ABOVELAW));
				if(A.getMetroMap().hasMoreElements())
					allowedToModify=(CMSecurity.isAllowed(mob,(A.getMetroMap().nextElement()),CMSecurity.SecFlag.ABOVELAW));
				final String rulingClan=B.rulingOrganization();
				if((!allowedToModify)
				&&(rulingClan.length()>0)
				&&(mob.getClanRole(rulingClan)!=null))
				{
					final Clan C=CMLib.clans().getClan(rulingClan);
					if((C!=null)&&(C.getAuthority(mob.getClanRole(rulingClan).second.intValue(),Clan.Function.ORDER_CONQUERED)==Clan.Authority.CAN_DO))
						allowedToModify=true;
				}

				if((allowedToModify)&&(!theLaw.lawIsActivated()))
					changeTheLaw(A2,B,mob,theLaw,"ACTIVATED","TRUE");

				try
				{
					if(which<1)
					{
						if(mob.session()!=null)
						{
							final StringBuffer str=new StringBuffer();
							str.append("^HLaws of "+A.name()+"^?\n\r\n\r");
							str.append(getFromTOC("TOC"));
							mob.session().colorOnlyPrintln(str.toString());
						}
					}
					else
					switch(which)
					{
					case 1:
						if(mob.session()!=null)
							mob.tell(CMStrings.replaceAll(getFromTOC("P1"+(theLaw.hasModifiableLaws()?"MOD":"")+(theLaw.hasModifiableNames()?"NAM":"")),"<AREA>",A.name()));
						break;
					case 2:
						doOfficersAndJudges(A, B, A2, theLaw, mob, allowedToModify);
						break;
					case 3:
						doVictimsOfCrime(A, B, theLaw, mob, allowedToModify);
						break;
					case 4:
						doJailPolicy(A, B, theLaw, mob, allowedToModify);
						break;
					case 5:
						doParoleAndRelease(A, B, theLaw, mob, allowedToModify);
						break;
					case 6:
						doBasicLaw(A, B, theLaw, mob, allowedToModify);
						break;
					case 7:
						doTresspassingLaw(A, B, theLaw, mob, allowedToModify);
						break;
					case 8:
						doIllegalInfluence(A, B, theLaw, mob, allowedToModify);
						break;
					case 9:
						doIllegalSkill(A, B, theLaw, mob, allowedToModify);
						break;
					case 10:
						doIllegalEmotation(A, B, theLaw, mob, allowedToModify);
						break;
					case 11:
						doTaxLaw(A, B, theLaw, mob, allowedToModify);
						break;
					case 12:
						doBannedSubstances(A, B, theLaw, mob, allowedToModify);
						break;
					}
				}
				catch(final Exception e)
				{
					Log.errOut("LawBook",e);
				}
			}
			return;
		case CMMsg.TYP_REWRITE:
		case CMMsg.TYP_WRITE:
			try
			{
				final Area A=CMLib.map().getArea(readableText());
				final Area A2=CMLib.law().getLegalObject(A);
				if(A2==null)
				{
					msg.source().tell(L("The pages appear blank, and too damaged to write on."));
					return;
				}
				return;
			}
			catch(final Exception e)
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
		try
		{
			if((lawProps==null)||(lawProps.isEmpty()))
			{
				lawProps=new Properties();
				lawProps.load(new ByteArrayInputStream(new CMFile("resources/lawtoc.ini",null).raw()));
				Resources.submitResource("LAWBOOKTOC",lawProps);
			}
			final String s=(String)lawProps.get(tag);
			if(s==null)
				return "\n\r";
			return s+"\n\r";
		}
		catch(final Exception e)
		{
			Log.errOut("LawBook",e);
		}
		return "";
	}

	public void changeTheLaw(Environmental A,
							 LegalBehavior B,
							 MOB mob,
							 Law theLaw,
							 String tag,
							 String newValue)
	{
		theLaw.setInternalStr(tag,newValue);
		if(A instanceof Area)
			B.updateLaw((Area)A);
	}

	public String shortLawDesc(String[] bits)
	{
		if((bits==null)||(bits.length<Law.BIT_NUMBITS))
			return "Not illegal.";
		final String flags=bits[Law.BIT_CRIMEFLAGS]+" "+bits[Law.BIT_CRIMELOCS].trim();
		return CMStrings.padRight(bits[Law.BIT_CRIMENAME],19)+" "
			   +CMStrings.padRight(((flags.length()==0)?"":flags),24)+" "
			   +bits[Law.BIT_SENTENCE];
	}

	public String shortLawHeader()
	{
		return CMStrings.padRight(L("Crime"),19)+" "
			+CMStrings.padRight(L("Flags"),24)+" "
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
		{"A crime ONLY if perpetrator is NOT in combat.","!combat"},
		{"Ignore this crime 1% of the time","ignore99"},
		{"Ignore this crime 5% of the time","ignore95"},
		{"Ignore this crime 10% of the time","!ignore90"},
		{"Ignore this crime 25% of the time","!ignore75"},
		{"Ignore this crime 50% of the time","!ignore50"},
		{"Ignore this crime 50% of the time","ignore50"},
		{"Ignore this crime 75% of the time","ignore75"},
		{"Ignore this crime 90% of the time","ignore90"},
		{"Ignore this crime 95% of the time","ignore95"},
		{"Ignore this crime 99% of the time","ignore99"},
	};

	//	@SupressWarnings
	public String[] modifyLaw(Area A, LegalBehavior B, Law theLaw, MOB mob, String[] oldLaw)
		throws IOException
	{
		if(mob.session()==null)
			return oldLaw;
		mob.tell(getFromTOC("MODLAW"));
		if(oldLaw==null)
		{
			if(mob.session().confirm(L("This is not presently a crime, would you like to make it one (Y/n)?"),"Y"))
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

		while((mob.session()!=null)&&(!mob.session().isStopped()))
		{
			final StringBuffer str=new StringBuffer(L("Modify Law: @x1\n\r\n\r",oldLaw[Law.BIT_CRIMENAME]));
			str.append(L("1. Name          : @x1\n\r",oldLaw[Law.BIT_CRIMENAME]));
			str.append(L("2. Flags         : @x1\n\r",oldLaw[Law.BIT_CRIMEFLAGS]));
			str.append(L("3. Locations mask: @x1\n\r",oldLaw[Law.BIT_CRIMELOCS]));
			str.append(L("4. Sentence      : @x1\n\r",oldLaw[Law.BIT_SENTENCE]));
			str.append(L("5. Justification : @x1\n\r",oldLaw[Law.BIT_WARNMSG]));
			str.append(L("6. DELETE THIS CRIME\n\r"));
			mob.session().colorOnlyPrintln(str.toString());
			final String s=mob.session().choose(L("Enter a number to modify or RETURN: "),"123456\n","\n");
			final int x=CMath.s_int(s);
			if(x==0)
				return oldLaw;
			oldLaw=oldLaw.clone();
			switch(x)
			{
			case 1:
				oldLaw[Law.BIT_CRIMENAME]=mob.session().prompt(L("Enter a new name for this crime: "),oldLaw[Law.BIT_CRIMENAME]);
				break;
			case 5:
				oldLaw[Law.BIT_WARNMSG]=mob.session().prompt(L("Shame/Justification Message: "),oldLaw[Law.BIT_WARNMSG]);
				break;
			case 6:
				if(mob.session().confirm(L("Are you sure you want to delete this crime (y/N)?"),"N"))
					return null;
				break;
			case 4:
				{
					StringBuffer msg=new StringBuffer("Sentences ( ");
					for (final String sentence : Law.PUNISHMENT_DESCS)
					{
						msg.append(sentence.toLowerCase()+" ");
					}
					String oldSentence="";
					final Vector<String> V=CMParms.parse(oldLaw[Law.BIT_SENTENCE]);
					final DVector V2=new DVector(2);
					for(int v=0;v<V.size();v++)
					{
						final String t=V.elementAt(v);
						boolean sent=false;
						for (final String element : Law.PUNISHMENT_DESCS)
						{
							if(element.startsWith(t.toUpperCase()))
							{
								oldSentence=t.toLowerCase();
								sent=true;
								V2.addElement(oldSentence,"");
								break;
							}
						}
						if(!sent)
						{
							for (final String element : Law.PUNISHMENTMASK_DESCS)
							{
								if(t.toUpperCase().startsWith(element))
								{
									final int x1=t.indexOf('=');
									if(x1>0)
										V2.addElement(element.toLowerCase(),t.substring(x1+1));
									else
										V2.addElement(element.toLowerCase(),"");
									break;
								}
							}
						}
					}
					msg.append(L("\n\rSelect a sentence (@x1): ",oldSentence));
					String t=mob.session().prompt(msg.toString(),oldSentence);
					for (final String element : Law.PUNISHMENT_DESCS)
					{
						if(element.startsWith(t.toUpperCase()))
						{
							final int x1=V2.indexOf(oldSentence);
							oldSentence=element.toLowerCase();
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
							msg=new StringBuffer(L("Sentence Flags ( "));
							for (final String element : Law.PUNISHMENTMASK_DESCS)
							{
								String sentence=element;
								if(sentence.indexOf('=')>0)
									sentence=sentence.substring(0,sentence.indexOf('='));
								msg.append(sentence.toLowerCase()+" ");
							}
							final StringBuffer oldFlags=new StringBuffer("");
							for(int v=0;v<V2.size();v++)
							{
								t=(String)V2.elementAt(v,1);
								if(t.equalsIgnoreCase(oldSentence))
									continue;
								oldFlags.append(t+((String)V2.elementAt(v,2))+" ");
							}
							msg.append(L("\n\rSelect a flag to toggle or RETURN (@x1): ",oldFlags.toString()));
							int selectedMask=-1;
							t=mob.session().prompt(msg.toString(),"");
							if(t.length()==0)
								break;
							int indexIfExists=-1;
							for(int i=0;i<Law.PUNISHMENTMASK_DESCS.length;i++)
							{
								if(Law.PUNISHMENTMASK_DESCS[i].startsWith(t.toUpperCase()))
								{
									selectedMask=i;
									indexIfExists=V2.indexOf(Law.PUNISHMENTMASK_DESCS[selectedMask].toLowerCase());
									t=null;
									break;
								}
							}
							if(t==null)
							{
								if(indexIfExists>=0)
								{
									mob.tell(L("'@x1' has been removed.",V2.elementAt(indexIfExists,1).toString()));
									V2.removeElementAt(indexIfExists);
								}
								else
								{
									String parm="";
									boolean abort=false;
									switch(Law.PUNISHMENTMASK_CODES[selectedMask])
									{
									case Law.PUNISHMENTMASK_DETAIN:
										if(!CMLib.law().getLegalObject(A).inMyMetroArea(mob.location().getArea()))
										{
											mob.tell(L("You can not add this room as a detention center, as it is not in the area."));
											abort=true;
										}
										else
										if(mob.session().confirm(L("Add this room as a new detention center room (y/N)? "),"N"))
										{
											final String time=mob.session().prompt(L("Enter the amount of time before they are released: "),"");
											if((time.length()==0)||(!CMath.isInteger(time))||(CMath.s_int(time)<0)||(CMath.s_int(time)>10000))
											{
												mob.tell(L("Invalid entry.  Aborted."));
												abort=true;
											}
											else
												parm=CMLib.map().getExtendedRoomID(mob.location())+","+time;
										}
										else
											abort=true;
										break;
									case Law.PUNISHMENTMASK_FINE:
									{
										final String fine=mob.session().prompt(L("Enter the amount of the fine in base-gold value: "),"");
										if((fine.length()==0)||(!CMath.isNumber(fine))||(CMath.s_double(fine)<0)||(CMath.s_double(fine)>100000.0))
										{
											mob.tell(L("Invalid entry.  Aborted."));
											abort=true;
										}
										else
											parm=fine;
										break;
									}
									}
									if(!abort)
									{
										V2.addElement(Law.PUNISHMENTMASK_DESCS[selectedMask],parm);
										mob.tell(L("'@x1@x2' has been added.",V2.elementAt(V2.size()-1,1).toString(),parm));
									}
									else
										mob.tell(L("'@x1@x2' has been aborted.",V2.elementAt(V2.size()-1,1).toString(),parm));
								}
							}
							else
								mob.tell(L("'@x1' is not a valid flag.  Unchanged.",t));
						}
						final StringBuffer newSentence=new StringBuffer("");
						for(int v2=0;v2<V2.size();v2++)
						{
							t=(String)V2.elementAt(v2,1);
							final String p=(String)V2.elementAt(v2,2);
							if(p.indexOf(' ')>0)
								newSentence.append("\""+t+p+"\" ");
							else
								newSentence.append(t+p+" ");
						}
						oldLaw[Law.BIT_SENTENCE]=newSentence.toString().trim();
					}
					else
						mob.tell(L("'@x1' is not a valid sentence.  Unchanged.",t));
				}
				break;
			case 3:
				{
					final StringBuffer s2=new StringBuffer("");
					final String oldVal=oldLaw[Law.BIT_CRIMELOCS].toUpperCase();
					String lastOle="";
					boolean lastAnswer=false;
					final Vector<String> allloca1=CMParms.parse(oldVal);
					final Vector<String> allloca2=CMParms.parse(oldVal.toUpperCase());
					for (final String[] locflag : locflags)
					{
						final int dex=allloca2.indexOf(locflag[1].toUpperCase());
						if(dex>=0)
						{
							allloca1.removeElementAt(dex);
							allloca2.removeElementAt(dex);
						}
						if(lastAnswer
						&&((("!"+lastOle).equals(locflag[1]))
							||(lastOle.equals("!"+locflag[1]))))
						{
							lastAnswer=false;
							lastOle="";
							continue;
						}

						boolean there=false;
						if(oldVal.startsWith(locflag[1].toUpperCase())
						||(oldVal.indexOf(" "+locflag[1].toUpperCase())>=0))
							there=true;
						lastAnswer=false;
						lastOle=locflag[1];
						if(mob.session().confirm(locflag[0]
												 +" "
												 +(there?"(Y/n)":"(y/N)")
												 +"?",
												 there?L("Y"):L("N")))
						{
							lastAnswer=true;
							s2.append(" "+lastOle);
						}
					}
					String restLoca=CMParms.combineQuoted(allloca1,0).trim();
					restLoca=mob.session().prompt(L("Enter any other location masks (@x1): ",restLoca),restLoca);
					oldLaw[Law.BIT_CRIMELOCS]=(s2.toString()+" "+restLoca).trim();
					break;
				}
			case 2:
				{
					final StringBuffer s2=new StringBuffer("");
					final String oldVal=oldLaw[Law.BIT_CRIMEFLAGS].toUpperCase();
					String lastOle="";
					boolean lastAnswer=false;
					for (final String[] lawflag : lawflags)
					{
						if(lastAnswer
						&&((("!"+lastOle).equals(lawflag[1]))
							||(lastOle.equals("!"+lawflag[1]))))
						{
							lastAnswer=false;
							lastOle="";
							continue;
						}

						boolean there=false;
						if(oldVal.startsWith(lawflag[1].toUpperCase())
						||(oldVal.indexOf(" "+lawflag[1].toUpperCase())>=0))
							there=true;
						lastAnswer=false;
						lastOle=lawflag[1];
						if(mob.session().confirm(lawflag[0]
												 +" "
												 +(there?"(Y/n)":"(y/N)")
												 +"?",
												 there?L("Y"):L("N")))
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
		return oldLaw;
	}

	public void doIllegalEmotation(Area A, LegalBehavior B, Law theLaw, MOB mob, boolean allowedToModify)
		throws IOException
	{
		if(mob.session()==null)
			return;
		mob.tell(getFromTOC("P10"+(theLaw.hasModifiableLaws()?"MOD":"")));
		while((mob.session()!=null)&&(!mob.session().isStopped()))
		{
			final StringBuffer str=new StringBuffer("");
			str.append(CMStrings.padRight(L("#  Words"),20)+" "+shortLawHeader()+"\n\r");
			for(int x=0;x<theLaw.otherCrimes().size();x++)
			{
				final String crime=CMParms.combineQuoted(theLaw.otherCrimes().get(x),0);
				final String[] set=theLaw.otherBits().get(x);
				str.append(CMStrings.padRight(""+(x+1)+". "+crime,20)+" "+shortLawDesc(set)+"\n\r");
			}
			str.append("A. ADD A NEW ONE\n\r");
			mob.session().colorOnlyPrintln(str.toString());
			if((!theLaw.hasModifiableLaws())||(!allowedToModify))
				break;
			String s=mob.session().prompt(L("\n\rEnter number to modify, A, or RETURN: "),"");
			if(s.length()==0)
				break;
			else
			if(s.equalsIgnoreCase("A"))
			{
				s=mob.session().prompt(L("\n\rEnter some key words to make illegal: "),"");
				if(s.length()>0)
				{
					final String[] newValue=modifyLaw(A,B,theLaw,mob,null);
					if(newValue!=null)
					{
						final StringBuffer s2=new StringBuffer(s+";");
						for(int i=0;i<newValue.length;i++)
						{
							s2.append(newValue[i]);
							if(i<(newValue.length-1))
								s2.append(";");
						}
						changeTheLaw(A,B,mob,theLaw,"CRIME"+(theLaw.otherBits().size()+1),s2.toString());
						mob.tell(L("Added."));
					}
				}
			}
			else
			{
				final int x=CMath.s_int(s);
				if((x>0)&&(x<=theLaw.otherCrimes().size()))
				{
					final String[] crimeSet=theLaw.otherBits().get(x-1);
					final String[] oldLaw=crimeSet;
					final String[] newValue=modifyLaw(A,B,theLaw,mob,crimeSet);
					if(newValue!=oldLaw)
					{
						if(newValue!=null)
							theLaw.otherBits().set(x-1,newValue);
						else
						{
							theLaw.otherCrimes().remove(x-1);
							theLaw.otherBits().remove(x-1);
						}
						final String[] newBits=new String[theLaw.otherBits().size()];
						for(int c=0;c<theLaw.otherCrimes().size();c++)
						{
							final String crimeWords=CMParms.combineQuoted(theLaw.otherCrimes().get(c),0);
							final String[] thisLaw=theLaw.otherBits().get(c);
							final StringBuffer s2=new StringBuffer("");
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
							mob.tell(L("Changed."));
						else
							mob.tell(L("Deleted."));
					}
				}
				else
					break;
			}
		}
	}

	public void doBannedSubstances(Area A, LegalBehavior B, Law theLaw, MOB mob, boolean allowedToModify)
	throws IOException
	{
		if(mob.session()==null)
			return;
		mob.tell(getFromTOC("P10"+(theLaw.hasModifiableLaws()?"MOD":"")));
		while((mob.session()!=null)&&(!mob.session().isStopped()))
		{
			final StringBuffer str=new StringBuffer("");
			str.append(CMStrings.padRight(L("#  Items"),20)+" "+shortLawHeader()+"\n\r");
			for(int x=0;x<theLaw.bannedSubstances().size();x++)
			{
				final String crime=CMParms.combineQuoted(theLaw.bannedSubstances().get(x),0);
				final String[] set=theLaw.bannedBits().get(x);
				str.append(CMStrings.padRight(""+(x+1)+". "+crime,20)+" "+shortLawDesc(set)+"\n\r");
			}
			str.append(L("A. ADD A NEW ONE\n\r"));
			mob.session().colorOnlyPrintln(str.toString());
			if((!theLaw.hasModifiableLaws())||(!allowedToModify))
				break;
			String s=mob.session().prompt(L("\n\rEnter number to modify, A, or RETURN: "),"");
			if(s.length()==0)
				break;
			else
			if(s.equalsIgnoreCase("A"))
			{
				s=mob.session().prompt(L("\n\rEnter item key words or resource types to make illegal (?)\n\r: "),"");
				if(s.equals("?"))
					mob.tell(L("Valid resources: @x1",CMParms.toListString(RawMaterial.CODES.NAMES())));
				else
				if(s.length()>0)
				{
					s=s.toUpperCase();
					final boolean resource=RawMaterial.CODES.FIND_CaseSensitive(s)>=0;
					if(resource||mob.session().confirm(L("'@x1' is not a known resource.  Add as a key word anyway (y/N)?",s),"N"))
					{
						final String[] newValue=modifyLaw(A,B,theLaw,mob,null);
						if(newValue!=null)
						{
							final StringBuffer s2=new StringBuffer(s+";");
							for(int i=0;i<newValue.length;i++)
							{
								s2.append(newValue[i]);
								if(i<(newValue.length-1))
									s2.append(";");
							}
							changeTheLaw(A,B,mob,theLaw,"BANNED"+(theLaw.bannedBits().size()+1),s2.toString());
							mob.tell(L("Added."));
						}
					}
				}
			}
			else
			{
				final int x=CMath.s_int(s);
				if((x>0)&&(x<=theLaw.bannedSubstances().size()))
				{
					final String[] crimeSet=theLaw.bannedBits().get(x-1);
					final String[] oldLaw=crimeSet;
					final String[] newValue=modifyLaw(A,B,theLaw,mob,crimeSet);
					if(newValue!=oldLaw)
					{
						if(newValue!=null)
							theLaw.bannedBits().set(x-1,newValue);
						else
						{
							theLaw.bannedSubstances().remove(x-1);
							theLaw.bannedBits().remove(x-1);
						}
						final String[] newBits=new String[theLaw.bannedBits().size()];
						for(int c=0;c<theLaw.bannedSubstances().size();c++)
						{
							final String crimeWords=CMParms.combineQuoted(theLaw.bannedSubstances().get(c),0);
							final String[] thisLaw=theLaw.bannedBits().get(c);
							final StringBuffer s2=new StringBuffer("");
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
							mob.tell(L("Changed."));
						else
							mob.tell(L("Deleted."));
					}
				}
				else
					break;
			}
		}
	}

	public void doIllegalSkill(Area A, LegalBehavior B, Law theLaw, MOB mob, boolean allowedToModify)
		throws IOException
	{
		if(mob.session()==null)
			return;
		mob.tell(getFromTOC("P9"+(theLaw.hasModifiableLaws()?"MOD":"")));
		while((mob.session()!=null)&&(!mob.session().isStopped()))
		{
			final StringBuffer str=new StringBuffer("");
			str.append(CMStrings.padRight(L("#  Ability"),20)+" "+shortLawHeader()+"\n\r");
			final Hashtable<String,String[]> filteredTable=new Hashtable<String,String[]>();
			for(final String key : theLaw.abilityCrimes().keySet())
			{
				final String[] set=theLaw.abilityCrimes().get(key);
				if(key.startsWith("$"))
					continue;
				final Ability AB=CMClass.getAbility(key);
				if(((AB==null)
					&&(CMLib.flags().getAbilityType_(key)<0)
					&&(CMLib.flags().getAbilityDomain(key)<0))
				||(set==null)
				||(set.length<Law.BIT_NUMBITS))
					continue;
				filteredTable.put(key.toUpperCase(),set);
			}
			int highest=0;
			for(final Enumeration<String> e=filteredTable.keys();e.hasMoreElements();)
			{
				final String key=e.nextElement();
				final String[] set=filteredTable.get(key);
				final Ability AB=CMClass.getAbility(key);
				final String name=(AB!=null)?AB.name():key;
				str.append(CMStrings.padRight(""+(highest+1)+". "+name,20)+" "+shortLawDesc(set)+"\n\r");
				highest++;
			}
			str.append(L("A. ADD A NEW ONE\n\r"));
			mob.session().colorOnlyPrintln(str.toString());
			if((!theLaw.hasModifiableLaws())||(!allowedToModify))
				break;
			String s=mob.session().prompt(L("\n\rEnter number to modify, A, or RETURN: "),"");
			if(s.length()==0)
				break;
			else
			if(s.equalsIgnoreCase("A"))
			{
				s=mob.session().prompt(L("\n\rEnter a skill name to make illegal: "),"");
				if(s.length()>0)
				{
					final Ability AB=CMClass.findAbility(s);
					if(AB!=null)
						s=AB.ID();
					if((AB==null)
					&&(CMLib.flags().getAbilityType_(s)<0)
					&&(CMLib.flags().getAbilityDomain(s)<0))
						mob.tell(L("That skill name or skill class is unknown."));
					else
					if(filteredTable.containsKey(s.toUpperCase()))
						mob.tell(L("That skill or skill class is already illegal."));
					else
					{
						final String[] newValue=modifyLaw(A,B,theLaw,mob,null);
						if(newValue!=null)
						{
							final StringBuffer s2=new StringBuffer("");
							for(int i=0;i<newValue.length;i++)
							{
								s2.append(newValue[i]);
								if(i<(newValue.length-1))
									s2.append(";");
							}
							changeTheLaw(A,B,mob,theLaw,s.toUpperCase(),s2.toString());
							mob.tell(L("Added."));
						}
					}
				}
			}
			else
			{
				final int x=CMath.s_int(s);
				String crimeName="";
				String[] crimeSet=null;
				int count=1;
				if((x>0)&&(x<=highest))
				{
					for(final Enumeration<String> e=filteredTable.keys();e.hasMoreElements();)
					{
						final String key=e.nextElement();
						final String[] set=filteredTable.get(key);
						if(count==x)
						{
							crimeName=key;
							crimeSet=set;
							break;
						}
						count++;
					}
				}
				if(crimeName.length()>0)
				{
					final String[] oldLaw=crimeSet;
					final String[] newValue=modifyLaw(A,B,theLaw,mob,crimeSet);
					if(newValue!=oldLaw)
					{
						final StringBuffer s2=new StringBuffer("");
						if(newValue!=null)
						{
							for(int i=0;i<newValue.length;i++)
							{
								s2.append(newValue[i]);
								if(i<(newValue.length-1))
									s2.append(";");
							}
						}
						changeTheLaw(A,B,mob,theLaw,crimeName,s2.toString());
						mob.tell(L("Changed."));
					}
				}
				else
					break;
			}
		}
	}

	public void doTaxLaw(Area A, LegalBehavior B, Law theLaw, MOB mob, boolean allowedToModify)
	throws IOException
	{
		if(mob.session()==null)
			return;
		mob.tell(getFromTOC("P11"+(theLaw.hasModifiableLaws()?"MOD":"")));
		while((mob.session()!=null)&&(!mob.session().isStopped()))
		{
			final StringBuffer str=new StringBuffer("");
			str.append(L("1. PROPERTY TAX   : @x1%\n\r",""+(CMath.s_double(theLaw.getInternalStr("PROPERTYTAX")))));
			str.append(L("2. SALES TAX      : @x1%\n\r",""+(CMath.s_double(theLaw.getInternalStr("SALESTAX")))));
			str.append(L("3. CITIZEN TAX    : @x1%\n\r",""+(CMath.s_double(theLaw.getInternalStr("CITTAX")))));
			str.append(L("4. TAX EVASION    : @x1\n\r",shortLawDesc((String[])theLaw.taxLaws().get("TAXEVASION"))));
			str.append(L("5. TREASURY       : "));
			final String S=theLaw.getInternalStr("TREASURY").trim();
			String room="*";
			String item="";
			final List<String> V=CMParms.parseSemicolons(S,false);
			if((S.length()==0)||(V.size()==0))
				str.append(L("Not defined"));
			else
			{
				room=V.get(0);
				if(V.size()>1)
					item=CMParms.combine(V,1);
				if(room.equalsIgnoreCase("*"))
					str.append("Any (*)");
				else
				{
					final Room R=CMLib.map().getRoom(room);
					if(R==null)
						str.append("Unknown");
					else
						str.append(R.displayText(mob)+" ("+R.roomID()+")");
				}
				if(item.length()>0)
					str.append(". Container: "+item+"\n\r");
				else
					str.append("\n\r");
			}
			mob.session().colorOnlyPrintln(str.toString());
			if((!theLaw.hasModifiableLaws())||(!allowedToModify))
				break;
			String s=mob.session().prompt(L("\n\rEnter a number to modify: "),"");
			final int x=CMath.s_int(s);
			if(x==0)
				break;
			switch(x)
			{
			case 1:
				s=mob.session().prompt(L("Enter a new tax amount: "),theLaw.getInternalStr("PROPERTYTAX"));
				if(CMath.s_double(s)!=CMath.s_double(theLaw.getInternalStr("PROPERTYTAX")))
				{
					changeTheLaw(A,B,mob,theLaw,"PROPERTYTAX",""+CMath.s_double(s));
					mob.tell(L("Changed."));
				}
				break;
			case 2:
				s=mob.session().prompt(L("Enter a new tax amount: "),theLaw.getInternalStr("SALESTAX"));
				if(CMath.s_double(s)!=CMath.s_double(theLaw.getInternalStr("SALESTAX")))
				{
					changeTheLaw(A,B,mob,theLaw,"SALESTAX",""+CMath.s_double(s));
					mob.tell(L("Changed."));
				}
				break;
			case 3:
				s=mob.session().prompt(L("Enter a new tax amount: "),theLaw.getInternalStr("CITTAX"));
				if(CMath.s_double(s)!=CMath.s_double(theLaw.getInternalStr("CITTAX")))
				{
					changeTheLaw(A,B,mob,theLaw,"CITTAX",""+CMath.s_double(s));
					mob.tell(L("Changed."));
				}
				break;
			case 4:
				{
					final String[] oldLaw=(String[])theLaw.taxLaws().get("TAXEVASION");
					final String[] newValue=modifyLaw(A,B,theLaw,mob,oldLaw);
					if(newValue!=oldLaw)
					{
						final StringBuffer s2=new StringBuffer("");
						if(newValue!=null)
						{
							for(int i=0;i<newValue.length;i++)
							{
								s2.append(newValue[i]);
								if(i<(newValue.length-1))
									s2.append(";");
							}
						}
						changeTheLaw(A,B,mob,theLaw,"TAXEVASION",s2.toString());
						mob.tell(L("Changed."));
					}
					break;
				}
			case 5:
				{
					String room2="/";
					while((room2.equals("/"))||(!room2.equals("*"))&&(room2.length()>0)&&(CMLib.map().getRoom(room2)==null))
						room2=mob.session().prompt(L("Enter a new room ID (RETURN=@x1, *=any): ",room),room);
					final String item2=mob.session().prompt(L("Enter an optional container name (RETURN=@x1): ",item),item);
					if((!room.equalsIgnoreCase(room2))||(!item.equalsIgnoreCase(item2)))
					{
						changeTheLaw(A,B,mob,theLaw,"TREASURY",""+room2+";"+item2);
						mob.tell(L("Changed."));
					}
					break;
				}
			}
		}
	}

	public void doIllegalInfluence(Area A, LegalBehavior B, Law theLaw, MOB mob, boolean allowedToModify)
		throws IOException
	{
		if(mob.session()==null)
			return;
		mob.tell(getFromTOC("P8"+(theLaw.hasModifiableLaws()?"MOD":"")));
		while((mob.session()!=null)&&(!mob.session().isStopped()))
		{
			final StringBuffer str=new StringBuffer("");
			str.append(CMStrings.padRight(L("#  Effect"),20)+" "+shortLawHeader()+"\n\r");
			final Hashtable<String,String[]> filteredTable=new Hashtable<String,String[]>();
			for(final String key : theLaw.abilityCrimes().keySet())
			{
				final String[] set=theLaw.abilityCrimes().get(key);
				if(!key.startsWith("$"))
					continue;
				final Ability AB=CMClass.getAbility(key.substring(1));
				if(((AB==null)
					&&(CMLib.flags().getAbilityType_(key.substring(1))<0)
					&&(CMLib.flags().getAbilityDomain(key.substring(1))<0))
				||(set==null)
				||(set.length<Law.BIT_NUMBITS)) 
					continue;
				filteredTable.put(key,set);
			}
			int highest=0;
			for(final Enumeration<String> e=filteredTable.keys();e.hasMoreElements();)
			{
				final String key=e.nextElement();
				final String[] set=filteredTable.get(key);
				final Ability AB=CMClass.getAbility(key.substring(1));
				final String name=(AB!=null)?AB.name():key.substring(1);
				str.append(CMStrings.padRight(""+(highest+1)+". "+name,20)+" "+shortLawDesc(set)+"\n\r");
				highest++;
			}
			str.append(L("A. ADD A NEW ONE\n\r"));
			mob.session().colorOnlyPrintln(str.toString());
			if((!theLaw.hasModifiableLaws())||(!allowedToModify))
				break;
			String s=mob.session().prompt(L("\n\rEnter number to modify, A, or RETURN: "),"");
			if(s.length()==0)
				break;
			else
			if(s.equalsIgnoreCase("A"))
			{
				s=mob.session().prompt(L("\n\rEnter a skill name to make an illegal influence: "),"");
				if(s.length()>0)
				{
					final Ability AB=CMClass.findAbility(s);
					if(AB!=null)
						s=AB.ID();
					if((AB==null)
					&&(CMLib.flags().getAbilityType_(s)<0)
					&&(CMLib.flags().getAbilityDomain(s)<0))
						mob.tell(L("That skill name or skill class is unknown."));
					else
					if(filteredTable.containsKey("$"+s.toUpperCase()))
						mob.tell(L("That skill or skill class is already an illegal influence."));
					else
					{
						final String[] newValue=modifyLaw(A,B,theLaw,mob,null);
						if(newValue!=null)
						{
							final StringBuffer s2=new StringBuffer("");
							for(int i=0;i<newValue.length;i++)
							{
								s2.append(newValue[i]);
								if(i<(newValue.length-1))
									s2.append(";");
							}
							changeTheLaw(A,B,mob,theLaw,"$"+s.toUpperCase(),s2.toString());
							mob.tell(L("Added."));
						}
					}
				}
			}
			else
			{
				final int x=CMath.s_int(s);
				String crimeName="";
				String[] crimeSet=null;
				int count=1;
				if((x>0)&&(x<=highest))
				{
					for(final Enumeration<String> e=filteredTable.keys();e.hasMoreElements();)
					{
						final String key=e.nextElement();
						final String[] set=filteredTable.get(key);
						if(count==x)
						{
							crimeName=key;
							crimeSet=set;
							break;
						}
						count++;
					}
				}
				if(crimeName.length()>0)
				{
					final String[] oldLaw=crimeSet;
					final String[] newValue=modifyLaw(A,B,theLaw,mob,crimeSet);
					if(newValue!=oldLaw)
					{
						final StringBuffer s2=new StringBuffer("");
						if(newValue!=null)
						{
							for(int i=0;i<newValue.length;i++)
							{
								s2.append(newValue[i]);
								if(i<(newValue.length-1))
									s2.append(";");
							}
						}
						changeTheLaw(A,B,mob,theLaw,crimeName,s2.toString());
						mob.tell(L("Changed."));
					}
				}
				else
					break;
			}
		}
	}

	public void doBasicLaw(Area A, LegalBehavior B, Law theLaw, MOB mob, boolean allowedToModify)
		throws IOException
	{
		if(mob.session()==null)
			return;
		mob.tell(getFromTOC("P6"+(theLaw.hasModifiableLaws()?"MOD":"")));
		while((mob.session()!=null)&&(!mob.session().isStopped()))
		{
			final StringBuffer str=new StringBuffer("");
			str.append(CMStrings.padRight(L("#  Law Name"),20)+" "+shortLawHeader()+"\n\r");
			str.append(L("1. ASSAULT           @x1\n\r",""+shortLawDesc(theLaw.basicCrimes().get("ASSAULT"))));
			str.append(L("2. MURDER            @x1\n\r",""+shortLawDesc(theLaw.basicCrimes().get("MURDER"))));
			str.append(L("3. NUDITY            @x1\n\r",""+shortLawDesc(theLaw.basicCrimes().get("NUDITY"))));
			str.append(L("4. ARMED             @x1\n\r",""+shortLawDesc(theLaw.basicCrimes().get("ARMED"))));
			str.append(L("5. RESISTING ARREST  @x1\n\r",""+shortLawDesc(theLaw.basicCrimes().get("RESISTINGARREST"))));
			str.append(L("6. ROBBING HOMES     @x1\n\r",""+shortLawDesc(theLaw.basicCrimes().get("PROPERTYROB"))));
			str.append("\n\r");
			mob.session().colorOnlyPrintln(str.toString());
			if((!theLaw.hasModifiableLaws())||(!allowedToModify))
				break;
			final String s=mob.session().prompt(L("\n\rEnter number to modify or RETURN: "),"");
			final int x=CMath.s_int(s);
			String crimeName="";
			if((x>0)&&(x<=6))
			{
				switch(x)
				{
				case 1:
					crimeName = "ASSAULT";
					break;
				case 2:
					crimeName = "MURDER";
					break;
				case 3:
					crimeName = "NUDITY";
					break;
				case 4:
					crimeName = "ARMED";
					break;
				case 5:
					crimeName = "RESISTINGARREST";
					break;
				case 6:
					crimeName = "PROPERTYROB";
					break;
				}
			}
			if(crimeName.length()>0)
			{
				final String[] oldLaw=theLaw.basicCrimes().get(crimeName);
				final String[] newValue=modifyLaw(A,B,theLaw,mob,oldLaw);
				if(newValue!=oldLaw)
				{
					final StringBuffer s2=new StringBuffer("");
					if(newValue!=null)
					{
						for(int i=0;i<newValue.length;i++)
						{
							s2.append(newValue[i]);
							if(i<(newValue.length-1))
								s2.append(";");
						}
					}
					changeTheLaw(A,B,mob,theLaw,crimeName,s2.toString());
					mob.tell(L("Changed."));
				}
			}
			else
				break;
		}
	}

	public void doParoleAndRelease(Area A, LegalBehavior B, Law theLaw, MOB mob, boolean allowedToModify)
		throws IOException
	{
		if(mob.session()==null)
			return;
		mob.tell(getFromTOC("P5"+(theLaw.hasModifiableLaws()?"MOD":"")));
		while((mob.session()!=null)&&(!mob.session().isStopped()))
		{
			final StringBuffer str=new StringBuffer("");
			str.append(L("1. LEVEL 1 PAROLE TIME: @x1 seconds.\n\r",""+(CMath.s_int(theLaw.getInternalStr("PAROLE1TIME"))*CMProps.getTickMillis()/1000)));
			str.append(L("2. LEVEL 2 PAROLE TIME: @x1 seconds.\n\r",""+(CMath.s_int(theLaw.getInternalStr("PAROLE2TIME"))*CMProps.getTickMillis()/1000)));
			str.append(L("3. LEVEL 3 PAROLE TIME: @x1 seconds.\n\r",""+(CMath.s_int(theLaw.getInternalStr("PAROLE3TIME"))*CMProps.getTickMillis()/1000)));
			str.append(L("4. LEVEL 4 PAROLE TIME: @x1 seconds.\n\r",""+(CMath.s_int(theLaw.getInternalStr("PAROLE4TIME"))*CMProps.getTickMillis()/1000)));
			str.append("\n\r");
			List<String> V=theLaw.releaseRooms();
			if(CMParms.combine(V,0).equals("@"))
				V=new Vector<String>();
			int highest=4;
			for(int v=0;v<V.size();v++)
			{
				final String s=V.get(v);
				highest++;
				final Room R=CMLib.map().getRoom(s);
				if(R!=null)
					str.append((5+v)+". RELEASE ROOM: "+R.displayText(mob)+"\n\r");
				else
					str.append((5+v)+". RELEASE ROOM: Rooms called '"+s+"'.\n\r");
			}
			mob.session().colorOnlyPrintln(str.toString());
			if((!theLaw.hasModifiableLaws())||(!allowedToModify))
				break;
			String s=mob.session().prompt(L("\n\rEnter 'A' to add a new release room, or enter a number to modify: "),"");
			boolean changed=false;
			if(s.equalsIgnoreCase("A"))
			{
				if(!CMLib.law().getLegalObject(A).inMyMetroArea(mob.location().getArea()))
					mob.tell(L("You can not add this room as a release room, as it is not in the area."));
				else
				if(mob.session().confirm(L("Add this room as a new release room (y/N)? "),"N"))
				{
					V.add(CMLib.map().getExtendedRoomID(mob.location()));
					changed=true;
				}
			}
			else
			{
				final int x=CMath.s_int(s);
				if((x>0)&&(x<=highest))
				{
					if(x>4)
					{
						if(mob.session().confirm(L("Remove this room as a release room (y/N)? "),"N"))
						{
							V.remove(x-5);
							changed=true;
						}
					}
					else
					{
						final long oldTime=CMath.s_int(theLaw.getInternalStr("PAROLE"+x+"TIME"))*CMProps.getTickMillis()/1000;
						s=mob.session().prompt(L("Enter a new number of seconds (@x1): ",""+oldTime),""+oldTime);
						if((CMath.s_int(s)!=oldTime)&&(CMath.s_int(s)>0))
						{
							long x1=CMath.s_int(s);
							x1=x1*1000/CMProps.getTickMillis();
							changeTheLaw(A,B,mob,theLaw,"PAROLE"+x+"TIME",""+x1);
							mob.tell(L("Changed."));
						}
					}
				}
				else
					break;
			}
			if(changed)
			{
				final StringBuffer s2=new StringBuffer("");
				for(int v=0;v<V.size();v++)
					s2.append((V.get(v))+";");
				if(s2.length()==0)
					s2.append("@");
				else
					s2.deleteCharAt(s2.length()-1);
				changeTheLaw(A,B,mob,theLaw,"RELEASEROOM",s2.toString());
				mob.tell(L("Changed."));
			}
		}
	}

	public void doJailPolicy(Area A, LegalBehavior B, Law theLaw, MOB mob, boolean allowedToModify)
		throws IOException
	{
		if(mob.session()==null)
			return;
		mob.tell(getFromTOC("P4"+(theLaw.hasModifiableLaws()?"MOD":"")));
		while((mob.session()!=null)&&(!mob.session().isStopped()))
		{
			final StringBuffer str=new StringBuffer("");
			str.append(L("1. LEVEL 1 JAIL TIME:  @x1 seconds.\n\r",""+(CMath.s_int(theLaw.getInternalStr("JAIL1TIME"))*CMProps.getTickMillis()/1000)));
			str.append(L("2. LEVEL 2 JAIL TIME:  @x1 seconds.\n\r",""+(CMath.s_int(theLaw.getInternalStr("JAIL2TIME"))*CMProps.getTickMillis()/1000)));
			str.append(L("3. LEVEL 3 JAIL TIME:  @x1 seconds.\n\r",""+(CMath.s_int(theLaw.getInternalStr("JAIL3TIME"))*CMProps.getTickMillis()/1000)));
			str.append(L("4. LEVEL 4 JAIL TIME:  @x1 seconds.\n\r",""+(CMath.s_int(theLaw.getInternalStr("JAIL4TIME"))*CMProps.getTickMillis()/1000)));
			str.append("\n\r");
			List<String> V=theLaw.jailRooms();
			if(CMParms.combine(V,0).equals("@"))
				V=new Vector<String>();
			int highest=4;
			for(int v=0;v<V.size();v++)
			{
				final String s=V.get(v);
				highest++;
				final Room R=CMLib.map().getRoom(s);
				if(R!=null)
					str.append((5+v)+". JAIL ROOM: "+R.displayText(mob)+"\n\r");
				else
					str.append((5+v)+". JAIL ROOM: Rooms called '"+s+"'.\n\r");
			}
			mob.session().colorOnlyPrintln(str.toString());
			if((!theLaw.hasModifiableLaws())||(!allowedToModify))
				break;
			String s=mob.session().prompt(L("\n\rEnter 'A' to add a new jail room, or enter a number to modify: "),"");
			boolean changed=false;
			if(s.equalsIgnoreCase("A"))
			{
				if(!CMLib.law().getLegalObject(A).inMyMetroArea(mob.location().getArea()))
					mob.tell(L("You can not add this room as a jail, as it is not in the area."));
				else
				if(mob.session().confirm(L("Add this room as a new jail room (y/N)? "),"N"))
				{
					V.add(CMLib.map().getExtendedRoomID(mob.location()));
					changed=true;
				}
			}
			else
			{
				final int x=CMath.s_int(s);
				if((x>0)&&(x<=highest))
				{
					if(x>4)
					{
						if(mob.session().confirm(L("Remove this room as a jail room (y/N)? "),"N"))
						{
							V.remove(x-5);
							changed=true;
						}
					}
					else
					{
						final long oldTime=CMath.s_int(theLaw.getInternalStr("JAIL"+x+"TIME"))*CMProps.getTickMillis()/1000;
						s=mob.session().prompt(L("Enter a new number of seconds (@x1): ",""+oldTime),""+oldTime);
						if((CMath.s_int(s)!=oldTime)&&(CMath.s_int(s)>0))
						{
							long x1=CMath.s_int(s);
							x1=x1*1000/CMProps.getTickMillis();
							changeTheLaw(A,B,mob,theLaw,"JAIL"+x+"TIME",""+x1);
							mob.tell(L("Changed."));
						}
					}
				}
				else
					break;
			}
			if(changed)
			{
				final StringBuffer s2=new StringBuffer("");
				for(int v=0;v<V.size();v++)
					s2.append((V.get(v))+";");
				if(s2.length()==0)
					s2.append("@");
				else
					s2.deleteCharAt(s2.length()-1);
				changeTheLaw(A,B,mob,theLaw,"JAIL",s2.toString());
				mob.tell(L("Changed."));
			}
		}
	}

	public void doTresspassingLaw(Area A, LegalBehavior B, Law theLaw, MOB mob, boolean allowedToModify)
		throws IOException
	{
		if(mob.session()==null)
			return;
		mob.tell(getFromTOC("P7"+(theLaw.hasModifiableLaws()?"MOD":"")));
		while((mob.session()!=null)&&(!mob.session().isStopped()))
		{
			mob.tell(L("1. Trespassers : @x1",CMLib.masking().maskDesc(theLaw.getInternalStr("TRESPASSERS"))));
			mob.tell(L("2. Law         : @x1",shortLawDesc(theLaw.basicCrimes().get("TRESPASSING"))));
			if((!theLaw.hasModifiableLaws())||(!allowedToModify))
				return;
			final String prompt=mob.session().choose(L("Enter one to change or RETURN: "),"12\n","\n");
			final int x=CMath.s_int(prompt);
			if((x<=0)||(x>2))
				return;
			if(x==1)
			{
				String s="?";
				while(s.trim().equals("?"))
				{
					s=mob.session().prompt(L("Enter a new mask, ? for help, or RETURN=[@x1]\n\r: ",theLaw.getInternalStr("TRESPASSERS")),theLaw.getInternalStr("TRESPASSERS"));
					if(s.trim().equals("?"))
						mob.tell(CMLib.masking().maskHelp("\n\r","arrests"));
					else
					if(!s.equals(theLaw.getInternalStr("TRESPASSERS")))
					{
						changeTheLaw(A,B,mob,theLaw,"TRESPASSERS",s);
						mob.tell(L("Changed."));
					}
				}
			}
			else
			if(x==2)
			{
				final String[] oldLaw=theLaw.basicCrimes().get("TRESPASSING");
				final String[] newValue=modifyLaw(A,B,theLaw,mob,oldLaw);
				if(newValue!=oldLaw)
				{
					final StringBuffer s2=new StringBuffer("");
					if(newValue!=null)
					{
						for(int i=0;i<newValue.length;i++)
						{
							s2.append(newValue[i]);
							if(i<(newValue.length-1))
								s2.append(";");
						}
					}
					changeTheLaw(A,B,mob,theLaw,"TRESPASSING",s2.toString());
					mob.tell(L("Changed."));
				}
			}
		}
	}

	public void doVictimsOfCrime(Area A, LegalBehavior B, Law theLaw, MOB mob, boolean allowedToModify)
		throws IOException
	{
		if(mob.session()==null)
			return;
		mob.tell(getFromTOC("P3"+(theLaw.hasModifiableLaws()?"MOD":"")));
		mob.tell(L("Protected victims: @x1",CMLib.masking().maskDesc(theLaw.getInternalStr("PROTECTED"))));
		if((theLaw.hasModifiableLaws())&&(allowedToModify))
		{
			String s="?";
			while(s.trim().equals("?"))
			{
				s=mob.session().prompt(L("Enter a new mask, ? for help, or RETURN=[@x1]\n\r: ",theLaw.getInternalStr("PROTECTED")),theLaw.getInternalStr("PROTECTED"));
				if(s.trim().equals("?"))
					mob.tell(CMLib.masking().maskHelp("\n\r","protects"));
				else
				if(!s.equals(theLaw.getInternalStr("PROTECTED")))
				{
					changeTheLaw(A,B,mob,theLaw,"PROTECTED",s);
					mob.tell(L("Changed."));
				}
			}
		}
	}

	public void doOfficersAndJudges(Area A,
									LegalBehavior B,
									Area legalO,
									Law theLaw,
									MOB mob,
									boolean allowedToModify)
		throws IOException
	{
		if(mob.session()==null)
			return;
		mob.tell(getFromTOC("P2"+(theLaw.hasModifiableLaws()?"MOD":"")+(theLaw.hasModifiableNames()?"NAM":"")));
		String duhJudge=L("No Judge Found!\n\r");
		final StringBuffer duhOfficers=new StringBuffer("");
		for(final Enumeration<Room> e=A.getMetroMap();e.hasMoreElements();)
		{
			final Room R=e.nextElement();
			for(int i=0;i<R.numInhabitants();i++)
			{
				final MOB M=R.fetchInhabitant(i);
				if(M!=null)
				{
					Room R2=M.getStartRoom();
					if(R2==null)
						R2=M.location();
					if(B.isAnyOfficer(legalO,M))
						duhOfficers.append(M.name(mob)+" from room '"+R2.displayText(mob)+"'\n\r");
					else
					if(B.isJudge(legalO,M))
						duhJudge=M.name(mob)+" from room '"+R2.displayText(mob)+"'\n\r";
				}
			}
		}
		if(duhOfficers.length()==0)
			duhOfficers.append(L("No Officers Found!\n\r"));
		mob.tell(L("1. Area Judge: \n\r@x1\n\r2. Area Officers: \n\r@x2",duhJudge,duhOfficers.toString()));
		if(theLaw.hasModifiableNames()&&theLaw.hasModifiableLaws()&&allowedToModify)
		{
			final int w=CMath.s_int(mob.session().choose(L("Enter one to modify, or RETURN to cancel: "),"12\n",""));
			if(w==0)
				return;
			final String modifiableTag=(w==1)?"JUDGE":"OFFICERS";
			final String s=mob.session().prompt(L("Enter key words from officials name(s) [@x1]\n\r: ",theLaw.getInternalStr(modifiableTag)),theLaw.getInternalStr(modifiableTag));
			if(!s.equals(theLaw.getInternalStr(modifiableTag)))
			{
				changeTheLaw(A,B,mob,theLaw,modifiableTag,s);
				mob.tell(L("Changed."));
			}
		}
	}
}
