package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.io.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class Arrest extends StdBehavior
{
	public String ID(){return "Arrest";}
	public long flags(){return Behavior.FLAG_LEGALBEHAVIOR;}
	protected int canImproveCode(){return Behavior.CAN_AREAS;}

	protected boolean loadAttempt=false;

	protected static final long ONE_REAL_DAY=(long)1000*60*60*24;
	protected static final long EXPIRATION_MILLIS=ONE_REAL_DAY*7; // 7 real days

	protected String getLawParms(){ return getParms();}

	protected class ArrestWarrant implements Cloneable, LegalWarrant
	{
		private MOB criminal=null;
		private MOB victim=null;
		private MOB witness=null;
		private MOB arrestingOfficer=null;
		private Room jail=null;
		private Room releaseRoom=null;
		private String crime="";
		private int actionCode=-1;
		private int jailTime=0;
		private int state=0;
		private int offenses=0;
		private long lastOffense=0;
		private long travelAttemptTime=0;
		private String warnMsg=null;
		public void setArrestingOfficer(Area legalArea, MOB mob)
		{
			if((arrestingOfficer!=null)
			&&(arrestingOfficer.getStartRoom()!=null)
			&&(arrestingOfficer.location()!=null)
			&&(legalArea!=null)
			&&(arrestingOfficer.getStartRoom().getArea()!=arrestingOfficer.location().getArea())
			&&(!legalArea.inMetroArea(arrestingOfficer.location().getArea())))
				MUDTracker.wanderAway(arrestingOfficer,true,true);
			if((mob==null)&&(arrestingOfficer!=null))
				stopTracking(arrestingOfficer);
			arrestingOfficer=mob;
		}
		public MOB criminal(){ return criminal;}
		public MOB victim()	{ return victim;}
		public MOB witness(){ return witness;}
		public MOB arrestingOfficer(){ return arrestingOfficer;}
		public Room jail(){ return jail;}
		public Room releaseRoom(){ return releaseRoom;}
		public String crime(){ return crime;}
		public int actionCode(){ return actionCode;}
		public int jailTime(){ return jailTime;}
		public int state(){ return state;}
		public int offenses(){ return offenses;}
		public long lastOffense(){ return lastOffense;}
		public long travelAttemptTime(){ return travelAttemptTime;}
		public String warnMsg(){ return warnMsg;}
		public void setCriminal(MOB mob){ criminal=mob;}
		public void setVictim(MOB mob){ victim=mob;}
		public void setWitness(MOB mob){ witness=mob;}
		public void setJail(Room R){ jail=R;}
		public void setReleaseRoom(Room R){ releaseRoom=R;}
		public void setCrime(String newcrime){ crime=newcrime;}
		public void setActionCode(int code){ actionCode=code;}
		public void setJailTime(int time){ jailTime=time;}
		public void setState(int newstate){ state=newstate;}
		public void setOffenses(int num){ offenses=num;}
		public void setLastOffense(long last){ lastOffense=last;}
		public void setTravelAttemptTime(long time){ travelAttemptTime=time;}
		public void setWarnMsg(String msg){ warnMsg=msg;}
	}

	protected class Laws implements Law
	{
		private boolean namesModifiable=false;
		private boolean lawsModifiable=false;

		private Vector otherCrimes=new Vector();
		private Vector otherBits=new Vector();
		private Hashtable abilityCrimes=new Hashtable();
		private Hashtable basicCrimes=new Hashtable();

		private Vector chitChat=new Vector();
		private Vector chitChat2=new Vector();
		private Vector jailRooms=new Vector();
		private Vector releaseRooms=new Vector();
		private Vector officerNames=new Vector();
		private Vector judgeNames=new Vector();
		private String[] messages=new String[Law.MSG_TOTAL];

		private boolean activated=true;

		private Vector oldWarrants=new Vector();
		private Vector warrants=new Vector();

		private boolean arrestMobs=false;

		private Properties theLaws=null;

		private String[] paroleMessages=new String[4];
		private Integer[] paroleTimes=new Integer[4];

		private String[] jailMessages=new String[4];
		private Integer[] jailTimes=new Integer[4];
		public Laws(){}

		public Laws(Properties laws,
					boolean modifiableNames,
					boolean modifiableLaws)
		{
			namesModifiable=modifiableNames;
			lawsModifiable=modifiableLaws;
			resetLaw(laws);
		}

		public Vector otherCrimes()	{ return otherCrimes;}
		public Vector otherBits() { return otherBits;}
		public Hashtable abilityCrimes(){ return abilityCrimes;}
		public Hashtable basicCrimes(){ return basicCrimes;}

		public boolean hasModifiableNames(){return namesModifiable;}
		public boolean hasModifiableLaws(){return lawsModifiable;}

		public Vector chitChat(){ return chitChat;}
		public Vector chitChat2(){ return chitChat2;}
		public Vector jailRooms(){ return jailRooms;}
		public Vector releaseRooms(){ return releaseRooms;}
		public Vector officerNames(){ return officerNames;}
		public Vector judgeNames(){ return judgeNames;}
		public String[] messages(){ return messages;}

		public Vector oldWarrants(){ return oldWarrants;}
		public Vector warrants(){ return warrants;}

		public boolean arrestMobs(){ return arrestMobs;}

		public String[] paroleMessages(){ return paroleMessages;}
		public Integer[] paroleTimes(){ return paroleTimes;}

		public String[] jailMessages(){ return jailMessages;}
		public Integer[] jailTimes(){ return jailTimes;}


		public void changeStates(LegalWarrant W, int state)
		{
			if((W==null)||(W.criminal()==null)) return;
			if(warrants.contains(W))
				for(int w=0;w<warrants.size();w++)
				{
					LegalWarrant W2=(LegalWarrant)warrants.elementAt(w);
					if(W2.criminal()==W.criminal())
						W2.setState(state);
				}
		}

		public String getMessage(int which)
		{
			if((which>=0)&&(which<messages.length)&&(messages[which]!=null))
			   return (String)messages[which];
			return "";
		}
		public String paroleMessages(int which)
		{
			if((which>=0)
			&&(which<paroleMessages.length)
			&&(paroleMessages[which]!=null))
			   return paroleMessages[which];
			return "";
		}
		public int paroleTimes(int which)
		{
			if((which>=0)
			&&(which<paroleTimes.length)
			&&(paroleTimes[which]!=null))
			   return paroleTimes[which].intValue();
			return 0;
		}
		public String jailMessages(int which)
		{
			if((which>=0)
			&&(which<jailMessages.length)
			&&(jailMessages[which]!=null))
			   return jailMessages[which];
			return "";
		}
		public int jailTimes(int which)
		{
			if((which>=0)
			&&(which<jailTimes.length)
			&&(jailTimes[which]!=null))
			   return jailTimes[which].intValue();
			return 0;
		}

		public String getInternalStr(String msg)
		{
			if((theLaws!=null)&&(theLaws.get(msg)!=null))
				return (String)theLaws.get(msg);
			return "";
		}
		public void setInternalStr(String tag, String value)
		{
			if(theLaws!=null)
			{
				if(theLaws.get(tag)!=null)
					theLaws.remove(tag);
				theLaws.put(tag,value);
			}
		}
		public boolean lawIsActivated(){ return activated;}

		public void resetLaw()
		{
			if(theLaws!=null)
				resetLaw(theLaws);
		}
		private void resetLaw(Properties laws)
		{
			theLaws=laws;
			activated=(!getInternalStr("ACTIVATED").equalsIgnoreCase("FALSE"));
			officerNames=Util.parse(getInternalStr("OFFICERS"));
			chitChat=Util.parse(getInternalStr("CHITCHAT"));
			chitChat2=Util.parse(getInternalStr("CHITCHAT2"));
			judgeNames=Util.parse(getInternalStr("JUDGE"));

			arrestMobs=getInternalStr("ARRESTMOBS").equalsIgnoreCase("true");

			messages=new String[Law.MSG_TOTAL];
			messages[Law.MSG_PREVOFF]=getInternalStr("PREVOFFMSG");
			messages[Law.MSG_WARNING]=getInternalStr("WARNINGMSG");
			messages[Law.MSG_THREAT]=getInternalStr("THREATMSG");
			messages[Law.MSG_EXECUTE]=getInternalStr("EXECUTEMSG");
			messages[Law.MSG_PROTECTEDMASK]=getInternalStr("PROTECTED");
			messages[Law.MSG_TRESPASSERMASK]=getInternalStr("TRESPASSERS");
			messages[Law.MSG_RESISTFIGHT]=getInternalStr("RESISTFIGHTMSG");
			messages[Law.MSG_NORESIST]=getInternalStr("NORESISTMSG");
			messages[Law.MSG_RESISTWARN]=getInternalStr("RESISTWARNMSG");
			messages[Law.MSG_PAROLEDISMISS]=getInternalStr("PAROLEDISMISS");
			messages[Law.MSG_RESIST]=getInternalStr("RESISTMSG");
			messages[Law.MSG_LAWFREE]=getInternalStr("LAWFREE");

			paroleMessages[0]=getInternalStr("PAROLE1MSG");
			paroleMessages[1]=getInternalStr("PAROLE2MSG");
			paroleMessages[2]=getInternalStr("PAROLE3MSG");
			paroleMessages[3]=getInternalStr("PAROLE4MSG");
			paroleTimes[0]=new Integer(Util.s_int(getInternalStr("PAROLE1TIME")));
			paroleTimes[1]=new Integer(Util.s_int(getInternalStr("PAROLE2TIME")));
			paroleTimes[2]=new Integer(Util.s_int(getInternalStr("PAROLE3TIME")));
			paroleTimes[3]=new Integer(Util.s_int(getInternalStr("PAROLE4TIME")));

			jailMessages[0]=getInternalStr("JAIL1MSG");
			jailMessages[1]=getInternalStr("JAIL2MSG");
			jailMessages[2]=getInternalStr("JAIL3MSG");
			jailMessages[3]=getInternalStr("JAIL4MSG");
			jailTimes[0]=new Integer(Util.s_int(getInternalStr("JAIL1TIME")));
			jailTimes[1]=new Integer(Util.s_int(getInternalStr("JAIL2TIME")));
			jailTimes[2]=new Integer(Util.s_int(getInternalStr("JAIL3TIME")));
			jailTimes[3]=new Integer(Util.s_int(getInternalStr("JAIL4TIME")));

			jailRooms=Util.parseSemicolons(getInternalStr("JAIL"),true);
			releaseRooms=Util.parseSemicolons(getInternalStr("RELEASEROOM"),true);

			basicCrimes.clear();
			String basicLaw=getInternalStr("MURDER");
			if(basicLaw.length()>0) basicCrimes.put("MURDER",getInternalBits(basicLaw));
			basicLaw=getInternalStr("RESISTINGARREST");
			if(basicLaw.length()>0) basicCrimes.put("RESISTINGARREST",getInternalBits(basicLaw));
			basicLaw=getInternalStr("NUDITY");
			if(basicLaw.length()>0) basicCrimes.put("NUDITY",getInternalBits(basicLaw));
			basicLaw=getInternalStr("ASSAULT");
			if(basicLaw.length()>0) basicCrimes.put("ASSAULT",getInternalBits(basicLaw));
			basicLaw=getInternalStr("ARMED");
			if(basicLaw.length()>0) basicCrimes.put("ARMED",getInternalBits(basicLaw));
			basicLaw=getInternalStr("TRESPASSING");
			if(basicLaw.length()>0) basicCrimes.put("TRESPASSING",getInternalBits(basicLaw));

			abilityCrimes.clear();
			otherCrimes.clear();
			otherBits.clear();
			for(Enumeration e=laws.keys();e.hasMoreElements();)
			{
				String key=(String)e.nextElement();
				String words=(String)laws.get(key);
				int x=words.indexOf(";");
				if(x>=0)
				{
					if(key.startsWith("CRIME"))
					{
						otherCrimes.addElement(Util.parse(words.substring(0,x)));
						String[] bits=new String[Law.BIT_NUMBITS];
						Vector parsed=Util.parseSemicolons(words.substring(x+1),false);
						for(int i=0;i<Law.BIT_NUMBITS;i++)
							if(i<parsed.size())
								bits[i]=(String)parsed.elementAt(i);
							else
								bits[i]="";
						otherBits.addElement(bits);
					}
					else
					if((key.startsWith("$")&&(CMClass.getAbility(key.substring(1))!=null))
					||(CMClass.getAbility(key)!=null))
						abilityCrimes.put(key.toUpperCase(),getInternalBits(words));
				}
			}
		}

		public String rawLawString()
		{
			if(theLaws!=null)
			{
				ByteArrayOutputStream out=new ByteArrayOutputStream();
				try{ theLaws.store(out,"");}catch(IOException e){}
				String s=Util.replaceAll(out.toString(),"\n\r","~");
				s=Util.replaceAll(s,"\r\n","~");
				s=Util.replaceAll(s,"\n","~");
				s=Util.replaceAll(s,"\r","~");
				s=Util.replaceAll(s,"'","`");
				return s;
			}
			return "";
		}

		private String[] getInternalBits(String bitStr)
		{
			String[] bits=new String[Law.BIT_NUMBITS];
			Vector parsed=Util.parseSemicolons(bitStr,false);
			for(int i=0;i<Law.BIT_NUMBITS;i++)
				if(i<parsed.size())
					bits[i]=(String)parsed.elementAt(i);
				else
					bits[i]="";
			return bits;
		}

		public LegalWarrant getWarrant(MOB mob, int which)
		{
			int one=0;
			for(int i=0;i<warrants.size();i++)
			{
				LegalWarrant W=(LegalWarrant)warrants.elementAt(i);
				if(W.criminal()==mob)
				{
					if(which==one)
						return W;
					one++;
				}
			}
			return null;
		}

		public LegalWarrant getOldWarrant(MOB criminal, String crime, boolean pull)
		{
			LegalWarrant W=null;
			for(int i=0;i<oldWarrants.size();i++)
			{
				LegalWarrant W2=(LegalWarrant)oldWarrants.elementAt(i);
				if((W2.criminal()==criminal)&&(W2.crime().equals(crime)))
				{
					W=W2;
					if(pull) oldWarrants.removeElement(W2);
					break;
				}
			}
			return W;
		}

	}


	// here are the codes for interacting with this behavior
	// see Law.java for info
	public boolean modifyBehavior(Environmental hostObj,
								  MOB mob,
								  Object O)
	{
		if((mob!=null)
		&&(mob.location()!=null)
		&&(hostObj!=null)
		&&(hostObj instanceof Area))
		{
			Law laws=getLaws((Area)hostObj,false);
			Integer I=null;
			Vector V=null;
			if(O instanceof Integer)
				I=(Integer)O;
			else
			if(O instanceof Vector)
			{
				V=(Vector)O;
				if(V.size()==0)
					return false;
				I=(Integer)V.firstElement();
			}
			else
				return false;

			switch(I.intValue())
			{
			case Law.MOD_FRAME: // frame
				if(V.size()>0)
				{
					MOB framed=(MOB)V.elementAt(1);
					LegalWarrant W=null;
					if(laws!=null)
						for(int i=0;(W=laws.getWarrant(mob,i))!=null;i++)
							if(W.criminal()==mob)
								W.setCriminal(framed);
					return true;
				}
				break;
			case Law.MOD_ARREST: //arrest
				if(V.size()>0)
				{
					MOB officer=(MOB)V.elementAt(1);
					LegalWarrant W=(laws!=null)?laws.getWarrant(mob,0):null;
					if(W!=null)
					{
						if((W.arrestingOfficer()==null)||(W.arrestingOfficer().location()!=mob.location()))
						{
							W.setArrestingOfficer((Area)hostObj,officer);
							CommonMsgs.say(W.arrestingOfficer(),W.criminal(),"You are under arrest "+restOfCharges(laws,W.criminal())+"! Sit down on the ground immediately!",false,false);
							W.setState(Law.STATE_ARRESTING);
							return true;
						}
						else
							return false;
					}
					return false;
				}
				break;
			case Law.MOD_WARRANTINFO: // warrant info
				{
					V.clear();
					if(laws!=null)
						for(int i=0;i<laws.warrants().size();i++)
						{
							LegalWarrant W=(LegalWarrant)laws.warrants().elementAt(i);
							if(isStillACrime(W))
							{
								Vector V2=new Vector();
								V2.addElement(W.criminal().name());
								if(W.victim()==null) V2.addElement("");
								else V2.addElement(W.victim().name());
								if(W.witness()==null) V2.addElement("");
								else V2.addElement(W.witness().name());
								V2.addElement(fixCharge(W));
								V.addElement(V2);
							}
						}
				}
				return true;
			case Law.MOD_LEGALINFO: // legal info
				{
					V.clear();
					if(laws!=null) V.addElement(laws);
				}
				break;
			case Law.MOD_LEGALTEXT: // legal text
				break;
			case Law.MOD_ISELLIGOFFICER: // is an elligible officer
				if((mob.isMonster())
				&&(mob.location()!=null)
				&&(laws!=null)
				&&(isElligibleOfficer(laws,mob,mob.location().getArea())))
					return true;
				return false;
			case Law.MOD_HASWARRANT: // has a warrant out
				return (laws!=null)?((laws.getWarrant(mob,0))!=null):false;
			case Law.MOD_ISOFFICER: // is a officer
				if((mob.isMonster())
				&&(mob.location()!=null)
				&&(laws!=null)
				&&(isAnyKindOfOfficer(laws,mob)))
					return true;
				return false;
			case Law.MOD_ISJUDGE: // is a judge
				if((mob.isMonster())
				&&(mob.location()!=null)
				&&(laws!=null)
				&&(isTheJudge(laws,mob)))
					return true;
				return false;
			case Law.MOD_SETNEWLAW:
				if(laws!=null)
				{
					laws.resetLaw();
					if(getLawParms().equalsIgnoreCase("custom")
					&&(hostObj!=null))
					{
						CMClass.DBEngine().DBDeleteData(hostObj.Name(),"ARREST",hostObj.Name()+"/ARREST");
						CMClass.DBEngine().DBCreateData(hostObj.Name(),"ARREST",hostObj.Name()+"/ARREST",laws.rawLawString());
					}
				}
				break;
			case Law.MOD_RULINGCLAN:
				if(V!=null){V.clear();V.addElement("");}
				return false;
			case Law.MOD_WARINFO:
				if(V!=null){V.clear();V.addElement("");}
				return false;
			case Law.MOD_CONTROLPOINTS:
				if(V!=null){V.clear();V.addElement(new Integer(0));}
				return false;
			case Law.MOD_GETWARRANTSOF:
				if(laws!=null)
				{
					boolean didSomething=false;
					if((V!=null)&&(V.size()>1)&&(V.elementAt(1) instanceof String))
					{
						String name=(String)V.elementAt(1);
						V.clear();
						for(int i=0;i<laws.warrants().size();i++)
						{
							LegalWarrant W=(LegalWarrant)laws.warrants().elementAt(i);
							if((isStillACrime(W))
							&&(EnglishParser.containsString(W.criminal().name(),name)))
							{
								didSomething=true;
								W.setLastOffense(System.currentTimeMillis()+EXPIRATION_MILLIS+(long)10);
								V.addElement(W);
							}
						}
					}
					else
					{
						V.clear();
						for(int i=0;i<laws.warrants().size();i++)
						{
							LegalWarrant W=(LegalWarrant)laws.warrants().elementAt(i);
							if((isStillACrime(W))&&(W.criminal()==mob))
							{
								didSomething=true;
								V.addElement(W);
							}
						}
					}
					return didSomething;
				}
				return false;
			case Law.MOD_ADDWARRANT:
				if((laws!=null)&&(V!=null)&&(V.elementAt(1) instanceof LegalWarrant))
				{
					laws.warrants().addElement(V.elementAt(1));
					return true;
				}
				else
				if((laws!=null)&&(V!=null)&&(V.size()>5))
				{
					MOB deity=(MOB)V.elementAt(1);
					String crimeLocs=(String)V.elementAt(2);
					String crimeFlags=(String)V.elementAt(3);
					String crime=(String)V.elementAt(4);
					String sentence=(String)V.elementAt(5);
					String warnMsg=(String)V.elementAt(6);
					return fillOutWarrant(mob,laws,(Area)hostObj,deity,crimeLocs,crimeFlags,crime,sentence,warnMsg);
				}
				return false;
			case Law.MOD_DELWARRANT:
				if((laws!=null)&&(V!=null)&&(V.elementAt(1) instanceof LegalWarrant))
				{
					laws.warrants().removeElement(V.elementAt(1));
					return true;
				}
				return false;
			}
		}
		return super.modifyBehavior(hostObj,mob,O);
	}

	public void setParms(String newParms)
	{
		super.setParms(newParms);
		loadAttempt=false;
	}

	protected boolean defaultModifiableNames(){return true;}

	protected Law getLaws(Environmental what, boolean cleanOnly)
	{
		String lawName=getLawParms();

		boolean modifiableLaw=false;
		boolean modifiableNames=defaultModifiableNames();

		Law laws=null;
		if((lawName.equalsIgnoreCase("custom"))&&(what!=null))
		{
			modifiableLaw=true;
			laws=(Law)Resources.getResource("LEGAL-"+what.Name());
		}
		else
		{
			if(lawName.length()==0)
				lawName="laws.ini";
			laws=(Law)Resources.getResource("LEGAL-"+lawName);
			modifiableNames=false;
		}
		if((laws==null)&&(cleanOnly)) return null;

		if(laws==null)
		{
			Properties lawprops=new Properties();
			try
			{
				if((lawName.equalsIgnoreCase("custom"))&&(what!=null))
				{
					Vector data=CMClass.DBEngine().DBReadData(what.Name(),"ARREST",what.Name()+"/ARREST");
					if((data!=null)&&(data.size()>0))
					{
						data=(Vector)data.firstElement();
						if((data!=null)&&(data.size()>0))
						{
							String s=Util.replaceAll((String)data.elementAt(3),"~","\n");
							s=Util.replaceAll(s,"`","'");
							lawprops.load(new ByteArrayInputStream(s.getBytes()));
						}
						else
						{
							String s=Law.defaultLaw;
							lawprops.load(new ByteArrayInputStream(s.getBytes()));
							s=Util.replaceAll(s,"\n","~");
							s=Util.replaceAll(s,"'","`");
							CMClass.DBEngine().DBCreateData(what.Name(),"ARREST",what.Name()+"/ARREST",s);
						}
					}
					else
					{
						String s=Law.defaultLaw;
						lawprops.load(new ByteArrayInputStream(s.getBytes()));
						s=Util.replaceAll(s,"\n","~");
						s=Util.replaceAll(s,"\r","~");
						s=Util.replaceAll(s,"'","`");
						CMClass.DBEngine().DBCreateData(what.Name(),"ARREST",what.Name()+"/ARREST",s);
					}
				}
				if(lawprops.isEmpty())
					lawprops.load(new FileInputStream("resources"+File.separatorChar+lawName));
			}
			catch(IOException e)
			{
				if(!loadAttempt)
				{
					Log.errOut("Arrest","Unable to load: "+lawName+", legal system inoperable.");
					loadAttempt=true;
				}
				return new Laws();
			}
			loadAttempt=true;
			laws=new Laws(lawprops,modifiableNames,modifiableLaw);
			if(lawName.equalsIgnoreCase("custom")&&(what!=null))
				Resources.submitResource("LEGAL-"+what.name(),laws);
			else
				Resources.submitResource("LEGAL-"+lawName,laws);
		}
		return laws;
	}

	public void unCuff(MOB mob)
	{
		Ability A=mob.fetchEffect("Skill_HandCuff");
		if(A!=null) A.unInvoke();
	}


	public void dismissOfficer(MOB officer)
	{
		if(officer==null) return;
		if((officer.getStartRoom()!=null)
		&&(officer.location()!=null)
		&&(officer.getStartRoom()==officer.location()))
			return;
		if(officer.isMonster())
			MUDTracker.wanderAway(officer,true,true);
	}

	public MOB getAWitnessHere(Room R)
	{
		if(R!=null)
		for(int i=0;i<R.numInhabitants();i++)
		{
			MOB M=R.fetchInhabitant(i);
			if(M.isMonster()
			&&(M.charStats().getStat(CharStats.INTELLIGENCE)>3)
			&&(Dice.rollPercentage()<=(M.getAlignment()/10)))
				return M;
		}
		return null;
	}

	public MOB getWitness(Area A, MOB mob)
	{
		Room R=mob.location();

		if((A!=null)&&(!A.inMetroArea(R.getArea())))
			return null;
		MOB M=getAWitnessHere(R);
		if(M!=null) return M;

		if(R!=null)
		for(int i=0;i<Directions.NUM_DIRECTIONS;i++)
		{
			Room R2=R.getRoomInDir(i);
			M=getAWitnessHere(R2);
			if(M!=null) return M;
		}
		return null;
	}

	public boolean isAnyKindOfOfficer(Law laws, MOB M)
	{
		if((M.isMonster())
		&&(M.location()!=null)
		&&(Sense.isMobile(M)))
		{
			if((laws.officerNames().size()<=0)
			||(((String)laws.officerNames().firstElement()).equals("@")))
			   return false;
			else
			for(int i=0;i<laws.officerNames().size();i++)
				if((EnglishParser.containsString(M.displayText(),(String)laws.officerNames().elementAt(i))
				||(EnglishParser.containsString(M.Name(),(String)laws.officerNames().elementAt(i)))))
					return true;
		}
		return false;
	}

	public boolean isElligibleOfficer(Law laws, MOB M, Area myArea)
	{
		if((M!=null)&&(M.isMonster())&&(M.location()!=null))
		{
			if((myArea!=null)&&(!myArea.inMetroArea(M.location().getArea()))) return false;

			if(isAnyKindOfOfficer(laws,M)
			&&(!isBusyWithJustice(laws,M))
			&&(Sense.aliveAwakeMobile(M,true))
			&&(!M.isInCombat()))
				return true;
		}
		return false;
	}

	public MOB getElligibleOfficerHere(Law laws,
									   Area myArea,
									   Room R,
									   MOB criminal,
									   MOB victim)
	{
		if(R==null) return null;
		for(int i=0;i<R.numInhabitants();i++)
		{
			MOB M=R.fetchInhabitant(i);
			if((M!=null)
			&&(M!=criminal)
			&&(M.location()!=null)
			&&(myArea.inMetroArea(M.location().getArea()))
			&&((victim==null)||(M!=victim))
			&&(isElligibleOfficer(laws,M,myArea))
			&&(Sense.canBeSeenBy(criminal,M)))
				return M;
		}
		return null;
	}

	public MOB getAnyElligibleOfficer(Law laws,
									  Area myArea,
									  MOB criminal,
									  MOB victim)
	{
		Room R=criminal.location();
		if(R==null) return null;
		if((myArea!=null)&&(!myArea.inMetroArea(R.getArea()))) return null;
		MOB M=getElligibleOfficerHere(laws,myArea,R,criminal,victim);
		if(M==null)
			for(Enumeration e=myArea.getMetroMap();e.hasMoreElements();)
			{
				Room R2=(Room)e.nextElement();
				M=getElligibleOfficerHere(laws,myArea,R2,criminal,victim);
				if(M!=null) break;
			}
		return M;
	}

	public MOB getElligibleOfficer(Law laws,
								   Area myArea,
								   MOB criminal,
								   MOB victim)
	{
		Room R=criminal.location();
		if(R==null) return null;
		if((myArea!=null)&&(!myArea.inMetroArea(R.getArea()))) return null;
		MOB M=getElligibleOfficerHere(laws,myArea,R,criminal,victim);
		if(M!=null) return M;
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			Room R2=R.getRoomInDir(d);
			if(R2!=null)
			{
				M=getElligibleOfficerHere(laws,myArea,R2,criminal,victim);
				if(M!=null)
				{
					int direction=Directions.getOpDirectionCode(d);
					MUDTracker.move(M,direction,false,false);
					if(M.location()==R) return M;
				}
			}
		}
		return null;
	}

	public boolean canFocusOn(MOB officer, MOB criminal)
	{
		FullMsg msg=new FullMsg(officer,criminal,CMMsg.MSG_EXAMINESOMETHING,"<S-NAME> look(s) closely at <T-NAME>.");
		if((officer!=null)&&(officer.location()!=null)&&(criminal.location()==officer.location()))
		{
			if(!officer.location().okMessage(officer,msg))
				return false;
			if(msg.sourceMessage().indexOf("<T-NAME>")<0)
				return false;
		}
		return true;
	}

	public boolean isStillACrime(LegalWarrant W)
	{
		// will witness talk, or victim press charges?
		HashSet H=W.criminal().getGroupMembers(new HashSet());
		if(W.witness().amDead()) return false;
		if(W.arrestingOfficer()!=null)
		{
			if(W.witness()==W.arrestingOfficer())
				return true;
			if((W.victim()!=null)&&(W.victim()==W.arrestingOfficer()))
				return true;
		}

		if(H.contains(W.witness())) return false;
		if((W.victim()!=null)&&(H.contains(W.victim()))) return false;
		// crimes expire after three real days
		if((W.lastOffense()>0)&&((System.currentTimeMillis()-W.lastOffense())>EXPIRATION_MILLIS))
			return false;
		return true;
	}

	public int highestCrimeAction(Law laws, MOB criminal)
	{
		int num=0;
		int highest=-1;
		for(int w2=0;w2<laws.warrants().size();w2++)
		{
			LegalWarrant W2=(LegalWarrant)laws.warrants().elementAt(w2);
			if(W2.criminal()==criminal)
			{
				num++;
				if((W2.actionCode()+W2.offenses())>highest)
					highest=(W2.actionCode()+W2.offenses());
			}
		}
		highest+=num;
		highest--;
		if(highest>Law.ACTION_HIGHEST) highest=Law.ACTION_HIGHEST;
		int adjusted=highest;
		if((criminal.getAlignment()>650)&&(adjusted>0))
			adjusted--;
		return adjusted;
	}

	public boolean isBusyWithJustice(Law laws, MOB M)
	{
		for(int w=0;w<laws.warrants().size();w++)
		{
			LegalWarrant W=(LegalWarrant)laws.warrants().elementAt(w);
			if(W.arrestingOfficer()!=null)
			{
				if(W.criminal()==M) return true;
				else
				if(W.arrestingOfficer()==M) return true;
			}
		}
		return false;
	}

	public String fixCharge(LegalWarrant W)
	{
		if(W==null) return "";
		String charge=W.crime();
		if(W.victim()==null) return charge;
		if(charge.indexOf("<T-NAME>")<0) return charge;
		return charge.replaceFirst("<T-NAME>",W.victim().name());
	}

	public String restOfCharges(Law laws, MOB mob)
	{
		StringBuffer msg=new StringBuffer("");
		for(int w=0;(laws.getWarrant(mob,w)!=null);w++)
		{
			LegalWarrant W=laws.getWarrant(mob,w);
			if(W!=null)
			{
				if(w==0)
					msg.append("for "+fixCharge(W));
				else
				if(laws.getWarrant(mob,w+1)==null)
					msg.append(", and for "+fixCharge(W));
				else
					msg.append(", for "+fixCharge(W));
			}
		}
		return msg.toString();
	}

	public void makePeace(Room R)
	{
		if(R==null) return;
		for(int i=0;i<R.numInhabitants();i++)
		{
			MOB inhab=R.fetchInhabitant(i);
			if((inhab!=null)&&(inhab.isInCombat()))
				inhab.makePeace();
		}
	}

	public boolean isTheJudge(Law laws, MOB M)
	{
		if(((M.isMonster()||M.soulMate()!=null))
		&&(!Sense.isMobile(M))
		&&(M.location()!=null))
		{
			if((laws.judgeNames().size()<=0)||(((String)laws.judgeNames().firstElement()).equals("@")))
				return false;
			else
			for(int i=0;i<laws.judgeNames().size();i++)
			{
				if((EnglishParser.containsString(M.displayText(),(String)laws.judgeNames().elementAt(i)))
				||(EnglishParser.containsString(M.Name(),(String)laws.judgeNames().elementAt(i))))
					return true;
			}
		}
		return false;
	}

	public MOB getTheJudgeHere(Law laws, Room R)
	{
		for(int i=0;i<R.numInhabitants();i++)
		{
			MOB M=R.fetchInhabitant(i);
			if(isTheJudge(laws,M))
				return M;
		}
		return null;
	}

	public Room findTheJudge(Law laws, Area myArea)
	{
		for(Enumeration r=myArea.getMetroMap();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			for(int i=0;i<R.numInhabitants();i++)
			{
				MOB M=R.fetchInhabitant(i);
				if(isTheJudge(laws,M))
					return R;
			}
		}
		return null;
	}

	public boolean trackTheJudge(MOB officer, Area myArea, Law laws)
	{
		stopTracking(officer);
		Ability A=CMClass.getAbility("Skill_Track");
		if(A!=null)
		{
			Room R=findTheJudge(laws,myArea);
			if(R!=null)
			{
				A.invoke(officer,Util.parse("\""+CMMap.getExtendedRoomID(R)+"\""),R,true);
				return true;
			}
		}
		return false;
	}

	public void stopTracking(MOB officer)
	{
		Vector V=Sense.flaggedAffects(officer,Ability.FLAG_TRACKING);
		for(int v=0;v<V.size();v++)
		{ ((Ability)V.elementAt(v)).unInvoke(); officer.delEffect((Ability)V.elementAt(v));}
	}

	public Room setReleaseRoom(Law laws, Area myArea, MOB criminal)
	{
		Room room=null;
		if((criminal.isMonster())&&(criminal.getStartRoom()!=null))
			room=criminal.getStartRoom();
		else
		if((laws.releaseRooms().size()==0)||(((String)laws.releaseRooms().firstElement()).equals("@")))
			return (Room)myArea.getMetroMap().nextElement();
		else
		{
			room=getRoom(criminal.location().getArea(),laws.releaseRooms());
			if(room==null) room=getRoom(myArea,laws.releaseRooms());
			if(room==null) room=findTheJudge(laws,myArea);
			if(room==null) room=(Room)myArea.getMetroMap().nextElement();
		}
		return room;
	}


	public boolean isTroubleMaker(MOB M)
	{
		if(M==null) return false;
		for(int b=0;b<M.numBehaviors();b++)
		{
			Behavior B=M.fetchBehavior(b);
			if((B!=null)&&(Util.bset(B.flags(),Behavior.FLAG_TROUBLEMAKING)))
				return true;
		}
		return false;
	}

	public Room getRoom(Area A, Vector V)
	{
		Room jail=null;
		if(V.size()==0) return jail;
		String which=(String)V.elementAt(Dice.roll(1,V.size(),-1));
		jail=CMMap.getRoom(which);
		if(jail==null)
		for(Enumeration r=A.getMetroMap();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			if(EnglishParser.containsString(R.displayText(),which))
			{ jail=R; break; }
		}
		if(jail==null)
		for(Enumeration r=A.getMetroMap();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			if(EnglishParser.containsString(R.description(),which))
			{ jail=R; break; }
		}
		return jail;
	}

	public void fileAllWarrants(Law laws, MOB mob)
	{
		LegalWarrant W=null;
		Vector V=new Vector();
		for(int i=0;(W=laws.getWarrant(mob,i))!=null;i++)
			if(W.criminal()==mob)
				V.addElement(W);
		for(int v=0;v<V.size();v++)
		{
			W=(LegalWarrant)V.elementAt(v);
			laws.warrants().removeElement(W);
			if(W.crime()!=null)
			{
				boolean found=false;
				for(int w=0;w<laws.oldWarrants().size();w++)
				{
					LegalWarrant oW=(LegalWarrant)laws.oldWarrants().elementAt(w);
					if((oW.criminal()==mob)
					&&(oW.crime()!=null)
					&&(oW.crime().equals(W.crime())))
						found=true;
				}
				if(!found)
				{
					W.setOffenses(W.offenses()+1);
					laws.oldWarrants().addElement(W);
				}
			}
		}
	}

	public Room findTheJail(MOB mob, Area myArea, Law laws)
	{
		Room jail=null;
		if((laws.jailRooms().size()==0)||(((String)laws.jailRooms().firstElement()).equals("@")))
			return null;
		else
		{
			jail=getRoom(mob.location().getArea(),laws.jailRooms());
			if(jail==null) jail=getRoom(myArea,laws.jailRooms());
		}
		return jail;
	}

	public boolean judgeMe(Law laws, MOB judge, MOB officer, MOB criminal, LegalWarrant W)
	{
		switch(highestCrimeAction(laws,criminal))
		{
		case Law.ACTION_WARN:
			{
			if((judge==null)&&(officer!=null)) judge=officer;
			StringBuffer str=new StringBuffer("");
			str.append(criminal.name()+", you are in trouble for "+restOfCharges(laws,criminal)+".  ");
			for(int w2=0;w2<laws.warrants().size();w2++)
			{
				LegalWarrant W2=(LegalWarrant)laws.warrants().elementAt(w2);
				if(W2.criminal()==criminal)
				{
					str.append("The charge of "+fixCharge(W2)+" was witnessed by "+W2.witness().name()+".  ");
					if((W2.warnMsg()!=null)&&(W2.warnMsg().length()>0))
						str.append(W2.warnMsg()+"  ");
					if((W2.offenses()>0)&&(laws.getMessage(Law.MSG_PREVOFF).length()>0))
						str.append(laws.getMessage(Law.MSG_PREVOFF)+"  ");
				}
			}
			if(laws.getMessage(Law.MSG_WARNING).length()>0)
				str.append(laws.getMessage(Law.MSG_WARNING)+"  ");
			CommonMsgs.say(judge,criminal,str.toString(),false,false);
			}
			return true;
		case Law.ACTION_THREATEN:
			{
			if((judge==null)&&(officer!=null)) judge=officer;
			StringBuffer str=new StringBuffer("");
			str.append(criminal.name()+", you are in trouble for "+restOfCharges(laws,criminal)+".  ");
			for(int w2=0;w2<laws.warrants().size();w2++)
			{
				LegalWarrant W2=(LegalWarrant)laws.warrants().elementAt(w2);
				if(W2.criminal()==criminal)
				{
					str.append("The charge of "+fixCharge(W2)+" was witnessed by "+W2.witness().name()+".  ");
					if((W2.warnMsg()!=null)&&(W2.warnMsg().length()>0))
						str.append(W2.warnMsg()+"  ");
					if((W2.offenses()>0)&&(laws.getMessage(Law.MSG_PREVOFF).length()>0))
						str.append(laws.getMessage(Law.MSG_PREVOFF)+"  ");
				}
			}
			if(laws.getMessage(Law.MSG_THREAT).length()>0)
				str.append(laws.getMessage(Law.MSG_THREAT)+"  ");
			CommonMsgs.say(judge,criminal,str.toString(),false,false);
			}
			return true;
		case Law.ACTION_PAROLE1:
			if(judge!=null)
			{
				if((W.offenses()>0)&&(laws.getMessage(Law.MSG_PREVOFF).length()>0))
					CommonMsgs.say(judge,W.criminal(),laws.getMessage(Law.MSG_PREVOFF),false,false);
				if(laws.paroleMessages(0).length()>0)
					CommonMsgs.say(judge,criminal,laws.paroleMessages(0),false,false);
				W.setJailTime(laws.paroleTimes(0));
				W.setState(Law.STATE_PAROLING);
			}
			return false;
		case Law.ACTION_PAROLE2:
			if(judge!=null)
			{
				if((W.offenses()>0)&&(laws.getMessage(Law.MSG_PREVOFF).length()>0))
					CommonMsgs.say(judge,W.criminal(),laws.getMessage(Law.MSG_PREVOFF),false,false);
				if(laws.paroleMessages(1).length()>0)
					CommonMsgs.say(judge,criminal,laws.paroleMessages(1),false,false);
				W.setJailTime(laws.paroleTimes(1));
				W.setState(Law.STATE_PAROLING);
			}
			return false;
		case Law.ACTION_PAROLE3:
			if(judge!=null)
			{
				if((W.offenses()>0)&&(laws.getMessage(Law.MSG_PREVOFF).length()>0))
					CommonMsgs.say(judge,W.criminal(),laws.getMessage(Law.MSG_PREVOFF),false,false);
				if(laws.paroleMessages(2).length()>0)
					CommonMsgs.say(judge,criminal,laws.paroleMessages(2),false,false);
				W.setJailTime(laws.paroleTimes(2));
				W.setState(Law.STATE_PAROLING);
			}
			return false;
		case Law.ACTION_PAROLE4:
			if(judge!=null)
			{
				if((W.offenses()>0)&&(laws.getMessage(Law.MSG_PREVOFF).length()>0))
					CommonMsgs.say(judge,W.criminal(),laws.getMessage(Law.MSG_PREVOFF),false,false);
				if(laws.paroleMessages(3).length()>0)
					CommonMsgs.say(judge,criminal,laws.paroleMessages(3),false,false);
				W.setJailTime(laws.paroleTimes(3));
				W.setState(Law.STATE_PAROLING);
			}
			return false;
		case Law.ACTION_JAIL1:
			if(judge!=null)
			{
				if((W.offenses()>0)&&(laws.getMessage(Law.MSG_PREVOFF).length()>0))
					CommonMsgs.say(judge,W.criminal(),laws.getMessage(Law.MSG_PREVOFF),false,false);
				if(laws.jailMessages(0).length()>0)
					CommonMsgs.say(judge,criminal,laws.jailMessages(0),false,false);
				W.setJailTime(laws.jailTimes(0));
				W.setState(Law.STATE_JAILING);
			}
			return false;
		case Law.ACTION_JAIL2:
			if(judge!=null)
			{
				if((W.offenses()>0)&&(laws.getMessage(Law.MSG_PREVOFF).length()>0))
					CommonMsgs.say(judge,W.criminal(),laws.getMessage(Law.MSG_PREVOFF),false,false);
				if(laws.jailMessages(1).length()>0)
					CommonMsgs.say(judge,criminal,laws.jailMessages(1),false,false);
				W.setJailTime(laws.jailTimes(1));
				W.setState(Law.STATE_JAILING);
			}
			return false;
		case Law.ACTION_JAIL3:
			if(judge!=null)
			{
				if((W.offenses()>0)&&(laws.getMessage(Law.MSG_PREVOFF).length()>0))
					CommonMsgs.say(judge,W.criminal(),laws.getMessage(Law.MSG_PREVOFF),false,false);
				if(laws.jailMessages(2).length()>0)
					CommonMsgs.say(judge,criminal,laws.jailMessages(2),false,false);
				W.setJailTime(laws.jailTimes(2));
				W.setState(Law.STATE_JAILING);
			}
			return false;
		case Law.ACTION_JAIL4:
			if(judge!=null)
			{
				if((W.offenses()>0)&&(laws.getMessage(Law.MSG_PREVOFF).length()>0))
					CommonMsgs.say(judge,W.criminal(),laws.getMessage(Law.MSG_PREVOFF),false,false);
				if(laws.jailMessages(3).length()>0)
					CommonMsgs.say(judge,criminal,laws.jailMessages(3),false,false);
				W.setJailTime(laws.jailTimes(3));
				W.setState(Law.STATE_JAILING);
			}
			return false;
		case Law.ACTION_EXECUTE:
			if(judge!=null)
			{
				if((W.offenses()>0)&&(laws.getMessage(Law.MSG_PREVOFF).length()>0))
					CommonMsgs.say(judge,W.criminal(),laws.getMessage(Law.MSG_PREVOFF),false,false);
				if(laws.getMessage(Law.MSG_EXECUTE).length()>0)
					CommonMsgs.say(judge,criminal,laws.getMessage(Law.MSG_EXECUTE),false,false);
				W.setState(Law.STATE_EXECUTING);
			}
			return false;
		}
		return true;
	}


	public boolean fillOutWarrant(MOB mob,
								Law laws,
								Area myArea,
								Environmental target,
								String crimeLocs,
								String crimeFlags,
								String crime,
								String sentence,
								String warnMsg)
	{
		if(mob.amDead()) return false;
		if(mob.location()==null) return false;
		if((myArea!=null)&&(!myArea.inMetroArea(mob.location().getArea())))
			return false;

		if(isAnyKindOfOfficer(laws,mob)
		||(isTheJudge(laws,mob))
		||CMSecurity.isAllowed(mob,mob.location(),"ABOVELAW"))
			return false;

		// is there a witness
		MOB witness=getWitness(myArea,mob);
		if(witness==null) return false;

		// is there a victim (if necessary)
		MOB victim=null;
		if((target!=null)&&(target instanceof MOB))
			victim=(MOB)target;
		if(mob==victim) return false;

		// is the location significant to this crime?
		if(crimeLocs.trim().length()>0)
		{
			boolean aCrime=false;
			Vector V=Util.parse(crimeLocs);
			String display=mob.location().displayText().toUpperCase().trim();
			for(int v=0;v<V.size();v++)
			{
				String str=((String)V.elementAt(v)).toUpperCase();
				if(str.endsWith("INDOORS")&&(str.length()<9))
				{
					if((mob.location().domainType()&Room.INDOORS)>0)
					{
						if(str.startsWith("!")) return false;
					}
					else
						if(!str.startsWith("!")) return false;
					aCrime=true;
				}
				else
				if(str.endsWith("HOME")&&(str.length()<6))
				{
					if(CoffeeUtensils.doesOwnThisProperty(mob,mob.location()))
						if(str.startsWith("!")) return false;
					if(!str.startsWith("!")) return false;
					aCrime=true;
				}
				else
				if(str.startsWith("!")&&(EnglishParser.containsString(display,str.substring(1))))
					return false;
				else
				if(EnglishParser.containsString(display,str))
				{ aCrime=true; break;}
			}
			if(!aCrime) return false;
		}

		// any special circumstances?
		if(crimeFlags.trim().length()>0)
		{
			Vector V=Util.parse(crimeFlags);
			for(int v=0;v<V.size();v++)
			{
				String str=((String)V.elementAt(v)).toUpperCase();
				if(str.endsWith("WITNESS")&&(str.length()<9))
				{
					if((witness!=null)&&(witness.location()==mob.location()))
					{
						if(str.startsWith("!"))	return false;
					}
					else
						if(!str.startsWith("!")) return false;
				}
				else
				if(str.endsWith("COMBAT")&&(str.length()<8))
				{
					if(mob.isInCombat())
					{
						if(str.startsWith("!")) return false;
					}
					else
						if(!str.startsWith("!")) return false;

				}
				else
				if(str.endsWith("RECENTLY")&&(str.length()<10))
				{
					LegalWarrant W=laws.getOldWarrant(mob,crime,false);
					long thisTime=System.currentTimeMillis();
					if((W!=null)&&((thisTime-W.lastOffense())<600000))
					{
						if(str.startsWith("!")) return false;
					}
					else
						if(!str.startsWith("!")) return false;
				}
			}
		}

		// is the victim a protected race?
		if((victim!=null)&&(!(victim instanceof Deity)))
		{
			if(!MUDZapper.zapperCheck(laws.getMessage(Law.MSG_PROTECTEDMASK),victim))
			   return false;
		}

		// does a warrant already exist?
		LegalWarrant W=null;
		for(int i=0;(W=laws.getWarrant(mob,i))!=null;i++)
		{
			if((W.criminal()==mob)
			&&(W.victim()==victim)
			&&(W.crime().equals(crime)))
				return false;
		}
		if(W==null) W=laws.getOldWarrant(mob,crime,true);
		if(W==null) W=new ArrestWarrant();

		// fill out the warrant!
		W.setCriminal(mob);
		W.setVictim(victim);
		W.setCrime(crime);
		W.setState(Law.STATE_SEEKING);
		W.setWitness(witness);
		W.setLastOffense(System.currentTimeMillis());
		W.setWarnMsg(warnMsg);
		sentence=sentence.trim();
		if(sentence.equalsIgnoreCase("warning"))
			W.setActionCode(Law.ACTION_WARN);
		else
		if(sentence.equalsIgnoreCase("threat"))
			W.setActionCode(Law.ACTION_THREATEN);
		else
		if(sentence.equalsIgnoreCase("parole1"))
			W.setActionCode(Law.ACTION_PAROLE1);
		else
		if(sentence.equalsIgnoreCase("parole2"))
			W.setActionCode(Law.ACTION_PAROLE2);
		else
		if(sentence.equalsIgnoreCase("parole3"))
			W.setActionCode(Law.ACTION_PAROLE3);
		else
		if(sentence.equalsIgnoreCase("parole4"))
			W.setActionCode(Law.ACTION_PAROLE4);
		else
		if(sentence.equalsIgnoreCase("jail1"))
			W.setActionCode(Law.ACTION_JAIL1);
		else
		if(sentence.equalsIgnoreCase("jail2"))
			W.setActionCode(Law.ACTION_JAIL2);
		else
		if(sentence.equalsIgnoreCase("jail3"))
			W.setActionCode(Law.ACTION_JAIL3);
		else
		if(sentence.equalsIgnoreCase("jail4"))
			W.setActionCode(Law.ACTION_JAIL4);
		else
		if(sentence.equalsIgnoreCase("death"))
			W.setActionCode(Law.ACTION_EXECUTE);
		else
		{
			Log.errOut("Arrest","Unknown sentence: "+sentence+" for crime "+crime);
			return false;
		}

		if((W.victim()!=null)&&(isTroubleMaker(W.victim())))
			W.setActionCode(W.actionCode()/2);

		if((isStillACrime(W))
		&&(Sense.canBeSeenBy(W.criminal(),W.witness())))
			laws.warrants().addElement(W);
		return true;
	}

	protected boolean isAnUltimateAuthorityHere(MOB M, Law laws)
	{
		if(CMSecurity.isAllowed(M,M.location(),"ABOVELAW")||(isTheJudge(laws,M)))
			return true;
		return false;
	}

	protected boolean theLawIsEnabled()
	{
		return ((CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MUDSTARTED))
				&&(!CMSecurity.isDisabled("ARREST")));
	}

	public void executeMsg(Environmental affecting, CMMsg msg)
	{
		super.executeMsg(affecting, msg);
		if(!(affecting instanceof Area)) return;
		if(!theLawIsEnabled()) return;

		Area myArea=(Area)affecting;
		Law laws=getLaws(affecting,false);
		if(!laws.lawIsActivated()) return;
		if(msg.source()==null) return;

		// the archons pardon
		if((msg.sourceMinor()==CMMsg.TYP_SPEAK)
		&&(msg.sourceMessage()!=null)
		&&(isAnUltimateAuthorityHere(msg.source(),laws)))
		{
			int x=msg.sourceMessage().toUpperCase().indexOf("I HEREBY PARDON ");
			if(x>0)
			{
				int y=msg.sourceMessage().lastIndexOf("'");
				if(y<x)	y=msg.sourceMessage().lastIndexOf("`");
				String name=null;
				if(y>x)
					name=msg.sourceMessage().substring(x+16,y).trim();
				else
					name=msg.sourceMessage().substring(x+16).trim();
				Vector warrs=(Vector)laws.warrants().clone();
				if(name.length()>0)
				for(int i=warrs.size()-1;i>=0;i--)
				{
					LegalWarrant W=(LegalWarrant)warrs.elementAt(i);
					if((W.criminal()!=null)&&(EnglishParser.containsString(W.criminal().Name(),name)))
					{
						Ability A=W.criminal().fetchEffect("Prisoner");
						if(A!=null) A.unInvoke();
						if(W.jail()!=W.criminal().location())
						{
							if(W.arrestingOfficer()!=null)
								dismissOfficer(W.arrestingOfficer());
							laws.warrants().removeElement(W);
						}
						else
						{
							W.setCrime("pardoned");
							W.setOffenses(0);
						}
					}
				}
			}
		}

		if((msg.sourceMinor()==CMMsg.TYP_DEATH)
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof MOB)
		&&(laws.basicCrimes().containsKey("MURDER")))
		{
			MOB criminal=(MOB)msg.tool();
			for(int i=laws.warrants().size()-1;i>=0;i--)
			{
				LegalWarrant W=(LegalWarrant)laws.warrants().elementAt(i);
				if((W.victim()!=null)
				&&(W.criminal()!=null)
				&&(W.victim()==msg.source())
				&&(W.criminal()==criminal))
					laws.warrants().removeElement(W);
			}
			String[] bits=(String[])laws.basicCrimes().get("MURDER");
			fillOutWarrant(criminal,
						   laws,
						   myArea,
						   msg.source(),
						   bits[Law.BIT_CRIMELOCS],
						   bits[Law.BIT_CRIMEFLAGS],
						   bits[Law.BIT_CRIMENAME],
						   bits[Law.BIT_SENTENCE],
						   bits[Law.BIT_WARNMSG]);
			return;
		}

		if((msg.source().isMonster())&&(!laws.arrestMobs()))
			return;

		if(isAnyKindOfOfficer(laws,msg.source())||(isTheJudge(laws,msg.source())))
			return;

		if(!Sense.aliveAwakeMobile(msg.source(),true))
			return;

		if(msg.source().location()==null) return;

		if((msg.tool()!=null)
		&&(msg.tool() instanceof Ability)
		&&(msg.othersMessage()!=null)
		&&(laws.abilityCrimes().containsKey(msg.tool().ID().toUpperCase())))
		{
			String[] info=(String[])laws.abilityCrimes().get(msg.tool().ID().toUpperCase());
			fillOutWarrant(msg.source(),
						   laws,
							myArea,
							msg.target(),
							info[Law.BIT_CRIMELOCS],
							info[Law.BIT_CRIMEFLAGS],
							info[Law.BIT_CRIMENAME],
							info[Law.BIT_SENTENCE],
							info[Law.BIT_WARNMSG]);
		}

		for(int a=0;a<msg.source().numAllEffects();a++)
		{
			Ability A=msg.source().fetchEffect(a);
			if((A!=null)
			&&(!A.isAutoInvoked())
			&&(laws.abilityCrimes().containsKey("$"+A.ID().toUpperCase())))
			{
				String[] info=(String[])laws.abilityCrimes().get("$"+A.ID().toUpperCase());
				fillOutWarrant(msg.source(),
								laws,
								myArea,
								null,
								info[Law.BIT_CRIMELOCS],
								info[Law.BIT_CRIMEFLAGS],
								info[Law.BIT_CRIMENAME],
								info[Law.BIT_SENTENCE],
								info[Law.BIT_WARNMSG]);
			}
		}

		if((Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
		&&(msg.target()!=null)
		&&(!Util.bset(msg.sourceCode(),CMMsg.MASK_GENERAL))
		&&((msg.tool()==null)||(msg.source().isMine(msg.tool())))
		&&(msg.target()!=msg.source())
		&&(!msg.target().name().equals(msg.source().name()))
		&&(msg.target() instanceof MOB)
		&&(!isTheJudge(laws,(MOB)msg.target())))
		{
			boolean justResisting=false;
			if(isAnyKindOfOfficer(laws,(MOB)msg.target()))
				for(int i=laws.warrants().size()-1;i>=0;i--)
				{
					LegalWarrant W=(LegalWarrant)laws.warrants().elementAt(i);
					if((W.criminal()==msg.source())
					&&(W.arrestingOfficer()!=null)
					&&(W.criminal().location()!=null)
					&&(W.criminal().location().isInhabitant(W.arrestingOfficer())))
					{
						justResisting=true;
						break;
					}
				}
			if(justResisting)
			{
				if(laws.basicCrimes().containsKey("RESISTINGARREST"))
				{
					String[] info=(String[])laws.basicCrimes().get("RESISTINGARREST");
					fillOutWarrant(msg.source(),
									laws,
									myArea,
									null,
									info[Law.BIT_CRIMELOCS],
									info[Law.BIT_CRIMEFLAGS],
									info[Law.BIT_CRIMENAME],
									info[Law.BIT_SENTENCE],
									info[Law.BIT_WARNMSG]);
				}
			}
			else
			if(laws.basicCrimes().containsKey("ASSAULT"))
			{
				String[] info=(String[])laws.basicCrimes().get("ASSAULT");
				fillOutWarrant(msg.source(),
								laws,
								myArea,
								msg.target(),
								info[Law.BIT_CRIMELOCS],
								info[Law.BIT_CRIMEFLAGS],
								info[Law.BIT_CRIMENAME],
								info[Law.BIT_SENTENCE],
								info[Law.BIT_WARNMSG]);
			}
		}

		if((msg.othersCode()!=CMMsg.NO_EFFECT)
		   &&(msg.othersMessage()!=null))
		{
			if(msg.sourceMinor()==CMMsg.TYP_ENTER)
			{
				if((laws.basicCrimes().containsKey("NUDITY"))
				&&(!msg.source().isMonster())
				&&(msg.source().fetchFirstWornItem(Item.ON_LEGS)==null)
				&&(msg.source().getWearPositions(Item.ON_LEGS)>0)
				&&(msg.source().fetchFirstWornItem(Item.ON_WAIST)==null)
				&&(msg.source().getWearPositions(Item.ON_WAIST)>0)
				&&(msg.source().fetchFirstWornItem(Item.ABOUT_BODY)==null)
				&&(msg.source().getWearPositions(Item.ABOUT_BODY)>0))
				{
					String info[]=(String[])laws.basicCrimes().get("NUDITY");
					fillOutWarrant(msg.source(),
									laws,
								   myArea,
								   null,
								   info[Law.BIT_CRIMELOCS],
								   info[Law.BIT_CRIMEFLAGS],
								   info[Law.BIT_CRIMENAME],
								   info[Law.BIT_SENTENCE],
								   info[Law.BIT_WARNMSG]);
				}

				Item w=null;
				if((laws.basicCrimes().containsKey("ARMED"))
				&&((!msg.source().isMonster())||(laws.arrestMobs()))
				&&((w=msg.source().fetchWieldedItem())!=null)
				&&(w instanceof Weapon)
				&&(((Weapon)w).weaponClassification()!=Weapon.CLASS_NATURAL)
				&&(((Weapon)w).weaponClassification()!=Weapon.CLASS_HAMMER)
				&&(((Weapon)w).weaponClassification()!=Weapon.CLASS_STAFF)
				&&(Sense.isSeen(w))
				&&(!Sense.isHidden(w))
				&&(!Sense.isInvisible(w)))
				{
					String info[]=(String[])laws.basicCrimes().get("ARMED");
					fillOutWarrant(msg.source(),
									laws,
								   myArea,
								   null,
								   info[Law.BIT_CRIMELOCS],
								   info[Law.BIT_CRIMEFLAGS],
								   info[Law.BIT_CRIMENAME],
								   info[Law.BIT_SENTENCE],
								   info[Law.BIT_WARNMSG]);
				}

				if((laws.basicCrimes().containsKey("TRESPASSING"))
				&&(MUDZapper.zapperCheck(laws.getMessage(Law.MSG_TRESPASSERMASK),msg.source())))
				{
					String[] info=(String[])laws.basicCrimes().get("TRESPASSING");
					fillOutWarrant(msg.source(),
									laws,
								   myArea,
								   null,
								   info[Law.BIT_CRIMELOCS],
								   info[Law.BIT_CRIMEFLAGS],
								   info[Law.BIT_CRIMENAME],
								   info[Law.BIT_SENTENCE],
								   info[Law.BIT_WARNMSG]);
				}
			}
			for(int i=0;i<laws.otherCrimes().size();i++)
			{
				Vector V=(Vector)laws.otherCrimes().elementAt(i);
				for(int v=0;v<V.size();v++)
				{
					if(EnglishParser.containsString(msg.othersMessage(),(String)V.elementAt(v)))
					{
						String[] info=(String[])laws.otherBits().elementAt(i);
						fillOutWarrant(msg.source(),
										laws,
										myArea,
										msg.target(),
										info[Law.BIT_CRIMELOCS],
										info[Law.BIT_CRIMEFLAGS],
										info[Law.BIT_CRIMENAME],
										info[Law.BIT_SENTENCE],
										info[Law.BIT_WARNMSG]);
					}
				}
			}
		}
	}


	public void haveMobReactToLaw(MOB mob, MOB officer)
	{
		if((mob.isMonster())&&(!Sense.isSitting(mob))&&(mob.amFollowing()==null)&&(!mob.isInCombat()))
		{
			boolean good=Sense.isGood(mob);
			boolean evil=Sense.isEvil(mob);
			boolean neutral=(!good)&&(!evil);
			if(evil
			||(neutral&&(Dice.rollPercentage()>50))
			||(Sense.flaggedBehaviors(mob,Behavior.FLAG_POTENTIALLYAGGRESSIVE).size()>0))
			{
				if(mob.envStats().level()>(officer.envStats().level()/2))
					mob.setVictim(officer);
				else
				if(!Sense.isAnimalIntelligence(mob))
					mob.enqueCommand(Util.parse("FLEE"),1);
			}
			else
			if((good||neutral)
			&&(!Sense.isAnimalIntelligence(mob)))
			{
				mob.makePeace();
				mob.doCommand(Util.parse("SIT"));
			}
			else
			if((Sense.isAnimalIntelligence(mob))&&(Dice.rollPercentage()>50))
			{
				mob.makePeace();
				mob.doCommand(Util.parse("SIT"));
			}
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);

		if(!(ticking instanceof Area)) return true;
		if(tickID!=MudHost.TICK_AREA) return true;
		Area myArea=(Area)ticking;

		if(!theLawIsEnabled())return true;

		Law laws=getLaws(myArea,false);
		if(!laws.lawIsActivated())
		{
			laws.warrants().clear();
			laws.oldWarrants().clear();
			return true;
		}


		HashSet handled=new HashSet();
		Vector warrs=(Vector)laws.warrants().clone();
		for(int w=0;w<warrs.size();w++)
		{
			LegalWarrant W=(LegalWarrant)warrs.elementAt(w);

			if((W.criminal()==null)||(W.criminal().location()==null))
				continue;

			if(!isStillACrime(W))
			{
				unCuff(W.criminal());
				if(W.arrestingOfficer()!=null)
					dismissOfficer(W.arrestingOfficer());
				W.setArrestingOfficer(myArea,null);
				W.setOffenses(W.offenses()+1);
				laws.oldWarrants().addElement(W);
				laws.warrants().removeElement(W);
				continue;
			}

			if(handled.contains(W.criminal()))
				continue;
			handled.add(W.criminal());

			switch(W.state())
			{
			case Law.STATE_SEEKING:
				{
					MOB officer=W.arrestingOfficer();
					if((officer==null)||(!W.criminal().location().isInhabitant(officer)))
					   officer=null;
					if(officer==null)
						officer=getElligibleOfficer(laws,myArea,W.criminal(),W.victim());
					W.setTravelAttemptTime(0);
					if((officer!=null)
					&&(W.criminal().location().isInhabitant(officer))
					&&(!W.criminal().amDead())
					&&(Sense.isInTheGame(W.criminal()))
					&&(Sense.canBeSeenBy(W.criminal(),officer))
					&&(canFocusOn(officer,W.criminal())))
					{
						if(CMSecurity.isAllowed(W.criminal(),W.criminal().location(),"ABOVELAW"))
						{
							CommonMsgs.say(officer,W.criminal(),"Damn, I can't arrest you.",false,false);
							if(CMSecurity.isAllowedEverywhere(W.criminal(),"ABOVELAW"))
							{
								fileAllWarrants(laws,W.criminal());
								unCuff(W.criminal());
								W.setArrestingOfficer(myArea,null);
							}
						}
						else
						if(W.crime().equalsIgnoreCase("pardoned"))
						{
							fileAllWarrants(laws,W.criminal());
							unCuff(W.criminal());
							W.setArrestingOfficer(myArea,null);
						}
						else
						if(judgeMe(laws,null,officer,W.criminal(),W))
						{
							fileAllWarrants(laws,W.criminal());
							unCuff(W.criminal());
							dismissOfficer(officer);
							W.setArrestingOfficer(myArea,null);
						}
						else
						{
							if(!Sense.isAnimalIntelligence(W.criminal()))
							{
								W.setArrestingOfficer(myArea,officer);
								CommonMsgs.say(W.arrestingOfficer(),W.criminal(),"You are under arrest "+restOfCharges(laws,W.criminal())+"! Sit down on the ground immediately!",false,false);
								W.setState(Law.STATE_ARRESTING);
							}
							else
							{
								W.setArrestingOfficer(myArea,officer);
								CommonMsgs.say(W.arrestingOfficer(),W.criminal(),"You are headed to the pound for "+restOfCharges(laws,W.criminal())+"!",false,false);
								W.setState(Law.STATE_ARRESTING);
							}
						}
					}
					else
					if(W.crime().equalsIgnoreCase("pardoned"))
					{
						fileAllWarrants(laws,W.criminal());
						unCuff(W.criminal());
						W.setArrestingOfficer(myArea,null);
					}
				}
				break;
			case Law.STATE_ARRESTING:
				{
					MOB officer=W.arrestingOfficer();
					W.setTravelAttemptTime(0);
					if((officer!=null)
					&&(W.criminal().location().isInhabitant(officer))
					&&(Sense.isInTheGame(W.criminal()))
					&&(!W.criminal().amDead())
					&&(Sense.aliveAwakeMobile(officer,true))
					&&(Sense.canBeSeenBy(W.criminal(),officer)))
					{
						if(officer.isInCombat())
						{
							if(officer.getVictim()==W.criminal())
							{
								CommonMsgs.say(officer,W.criminal(),laws.getMessage(Law.MSG_RESISTFIGHT),false,false);
								W.setState(Law.STATE_SUBDUEING);
							}
							else
							{
								W.setArrestingOfficer(myArea,null);
								W.setState(Law.STATE_SEEKING);
							}
						}
						else
						if(W.crime().equalsIgnoreCase("pardoned"))
						{
							fileAllWarrants(laws,W.criminal());
							unCuff(W.criminal());
							W.setArrestingOfficer(myArea,null);
						}
						else
						{
							haveMobReactToLaw(W.criminal(),officer);
							W.setState(Law.STATE_SUBDUEING);
							if(Sense.isSitting(W.criminal())||Sense.isSleeping(W.criminal()))
							{
								if(!Sense.isAnimalIntelligence(W.criminal()))
									CommonMsgs.say(officer,W.criminal(),laws.getMessage(Law.MSG_NORESIST),false,false);
							}
							else
								CommonMsgs.say(officer,W.criminal(),laws.getMessage(Law.MSG_RESISTWARN),false,false);
							if(W.criminal().isMonster())
								haveMobReactToLaw(W.criminal(),officer);
						}
					}
					else
					{
						W.setArrestingOfficer(myArea,null);
						W.setState(Law.STATE_SEEKING);
					}
				}
				break;
			case Law.STATE_SUBDUEING:
				{
					MOB officer=W.arrestingOfficer();
					if((officer!=null)
					&&(W.criminal().location().isInhabitant(officer))
					&&(!W.criminal().amDead())
					&&(Sense.isInTheGame(W.criminal()))
					&&(Sense.aliveAwakeMobile(officer,true))
					&&(Sense.canBeSeenBy(W.criminal(),officer)))
					{
						W.setTravelAttemptTime(0);
						haveMobReactToLaw(W.criminal(),officer);
						if(W.crime().equalsIgnoreCase("pardoned"))
						{
							fileAllWarrants(laws,W.criminal());
							unCuff(W.criminal());
							W.setArrestingOfficer(myArea,null);
						}
						else
						if(!Sense.isSitting(W.criminal())&&(!Sense.isSleeping(W.criminal())))
						{
							if(!W.arrestingOfficer().isInCombat())
								CommonMsgs.say(officer,W.criminal(),laws.getMessage(Law.MSG_RESIST),false,false);

							Ability A=CMClass.getAbility("Skill_ArrestingSap");
							if(A!=null){
								int curPoints=(int)Math.round(Util.div(W.criminal().curState().getHitPoints(),W.criminal().maxState().getHitPoints())*100.0);
								A.setProfficiency(100);
								A.setAbilityCode(10);
								if(!A.invoke(officer,W.criminal(),(curPoints<=25)))
								{
									A=CMClass.getAbility("Skill_Trip");
									A.setAbilityCode(30);
									if(!A.invoke(officer,W.criminal(),(curPoints<=50)))
										MUDFight.postAttack(officer,W.criminal(),officer.fetchWieldedItem());
								}
							}
						}
						Ability cuff=W.criminal().fetchEffect("Skill_HandCuff");
						if((Sense.isSitting(W.criminal())||(cuff!=null)||(Sense.isSleeping(W.criminal())))
						&&(!W.criminal().amDead())
						&&(Sense.isInTheGame(W.criminal())))
						{
							makePeace(officer.location());
							// cuff him!
							if(Sense.isAnimalIntelligence(W.criminal()))
								W.setState(Law.STATE_JAILING);
							else
								W.setState(Law.STATE_MOVING);
							if(cuff!=null){ cuff.unInvoke(); W.criminal().delEffect(cuff);}
							Ability A=CMClass.getAbility("Skill_HandCuff");
							if(A!=null)	A.invoke(officer,W.criminal(),true);
							W.criminal().makePeace();
							makePeace(officer.location());
							A=W.criminal().fetchEffect("Skill_ArrestingSap");
							if(A!=null)A.unInvoke();
							A=W.criminal().fetchEffect("Fighter_Whomp");
							if(A!=null)A.unInvoke();
							A=W.criminal().fetchEffect("Skill_Trip");
							if(A!=null)A.unInvoke();
							makePeace(officer.location());
							CommonMsgs.stand(W.criminal(),true);
							W.setTravelAttemptTime(System.currentTimeMillis());
							if(trackTheJudge(officer,myArea,laws))
								makePeace(officer.location());
							else
							{
								makePeace(officer.location());
								CommonMsgs.say(officer,W.criminal(),"Since there is no judge, you may go.",false,false);
								W.setTravelAttemptTime(0);
								fileAllWarrants(laws,W.criminal());
								unCuff(W.criminal());
								if(W.arrestingOfficer()!=null)
									dismissOfficer(W.arrestingOfficer());
							}
						}
						else
						{
							if(officer!=null)
								CommonMsgs.say(officer,null,"Hmph.",false,false);
							W.setTravelAttemptTime(0);
							unCuff(W.criminal());
							W.setArrestingOfficer(myArea,null);
							W.setState(Law.STATE_SEEKING);
						}
					}
					else
					{
						if(officer!=null)
							CommonMsgs.say(officer,null,"Darn.",false,false);
						W.setTravelAttemptTime(0);
						unCuff(W.criminal());
						W.setArrestingOfficer(myArea,null);
						W.setState(Law.STATE_SEEKING);
					}
				}
				break;
			case Law.STATE_MOVING:
				{
					MOB officer=W.arrestingOfficer();

					if((officer!=null)
					&&(W.criminal().location().isInhabitant(officer))
					&&(!W.criminal().amDead())
					&&(Sense.isInTheGame(W.criminal()))
					&&(!W.crime().equalsIgnoreCase("pardoned"))
					&&((W.travelAttemptTime()==0)||((System.currentTimeMillis()-W.travelAttemptTime())<(5*60*1000)))
					&&(Sense.aliveAwakeMobile(officer,true)))
					{
						if(W.criminal().curState().getMovement()<50)
							W.criminal().curState().setMovement(50);
						if(officer.curState().getMovement()<50)
							officer.curState().setMovement(50);
						makePeace(officer.location());
						if(officer.isMonster()) CommonMsgs.look(officer,true);
						if(getTheJudgeHere(laws,officer.location())!=null)
							W.setState(Law.STATE_REPORTING);
						else
						if(Sense.flaggedAffects(officer,Ability.FLAG_TRACKING).size()==0)
						{
							if(!trackTheJudge(officer,myArea,laws))
							{
								CommonMsgs.say(officer,null,"Now where was that court?.",false,false);
								W.setTravelAttemptTime(0);
								unCuff(W.criminal());
								W.setArrestingOfficer(myArea,null);
								W.setState(Law.STATE_SEEKING);
							}
						}
						else
						if((Dice.rollPercentage()>75)&&(laws.chitChat().size()>0))
							CommonMsgs.say(officer,W.criminal(),(String)laws.chitChat().elementAt(Dice.roll(1,laws.chitChat().size(),-1)),false,false);
					}
					else
					{
						if(officer!=null)
							CommonMsgs.say(officer,null,"Drat! Lost another one!",false,false);
						W.setTravelAttemptTime(0);
						unCuff(W.criminal());
						W.setArrestingOfficer(myArea,null);
						W.setState(Law.STATE_SEEKING);
					}
				}
				break;
			case Law.STATE_REPORTING:
				{
					MOB officer=W.arrestingOfficer();
					if((officer!=null)
					&&(W.criminal().location().isInhabitant(officer))
					&&(!W.criminal().amDead())
					&&(Sense.isInTheGame(W.criminal()))
					&&(!W.crime().equalsIgnoreCase("pardoned"))
					&&(Sense.aliveAwakeMobile(officer,true)))
					{
						MOB judge=getTheJudgeHere(laws,officer.location());
						if(judge==null)
						{
							W.setState(Law.STATE_MOVING);
							if(!trackTheJudge(officer,myArea,laws))
							{
								CommonMsgs.say(officer,null,"Where was that darn court!",false,false);
								W.setTravelAttemptTime(0);
								unCuff(W.criminal());
								W.setArrestingOfficer(myArea,null);
								W.setState(Law.STATE_SEEKING);
							}

						}
						else
						if(Sense.aliveAwakeMobile(judge,true))
						{
							W.setTravelAttemptTime(0);
							String sirmaam="Sir";
							if(Character.toString((char)judge.charStats().getStat(CharStats.GENDER)).equalsIgnoreCase("F"))
								sirmaam="Ma'am";
							CommonMsgs.say(officer,judge,sirmaam+", "+W.criminal().name()+" has been arrested "+restOfCharges(laws,W.criminal())+".",false,false);
							for(int w2=0;w2<laws.warrants().size();w2++)
							{
								LegalWarrant W2=(LegalWarrant)laws.warrants().elementAt(w2);
								if(W2.criminal()==W.criminal())
									CommonMsgs.say(officer,judge,"The charge of "+fixCharge(W2)+" was witnessed by "+W2.witness().name()+".",false,false);
							}
							W.setState(Law.STATE_WAITING);
						}
						else
						{
							CommonMsgs.say(officer,W.criminal(),"I guess court is not in session today.",false,false);
							W.setTravelAttemptTime(0);
							unCuff(W.criminal());
							W.setArrestingOfficer(myArea,null);
							W.setState(Law.STATE_SEEKING);
						}
					}
					else
					{
						if(officer!=null)
							CommonMsgs.say(officer,null,"Wha? Where'd he go?",false,false);
						W.setTravelAttemptTime(0);
						unCuff(W.criminal());
						W.setArrestingOfficer(myArea,null);
						W.setState(Law.STATE_SEEKING);
					}
				}
				break;
			case Law.STATE_WAITING:
				{
					MOB officer=W.arrestingOfficer();
					if((officer!=null)
					&&(!W.criminal().amDead())
					&&(W.criminal().location().isInhabitant(officer))
					&&(Sense.isInTheGame(W.criminal()))
					&&(!W.crime().equalsIgnoreCase("pardoned"))
					&&(Sense.aliveAwakeMobile(officer,true)))
					{
						MOB judge=getTheJudgeHere(laws,officer.location());
						if(judge==null)
						{
							W.setState(Law.STATE_MOVING);
							if(!trackTheJudge(officer,myArea,laws))
							{
								CommonMsgs.say(officer,null,"Where was that darn court?!",false,false);
								W.setTravelAttemptTime(0);
								unCuff(W.criminal());
								W.setArrestingOfficer(myArea,null);
								W.setState(Law.STATE_SEEKING);
							}
						}
						else
						if(Sense.aliveAwakeMobile(judge,true))
						{
							if(judgeMe(laws,judge,officer,W.criminal(),W))
							{
								W.setTravelAttemptTime(0);
								unCuff(W.criminal());
								dismissOfficer(officer);
								fileAllWarrants(laws,W.criminal());
								unCuff(W.criminal());
								W.setArrestingOfficer(myArea,null);
							}
							// else, still stuff to do
						}
						else
						{
							CommonMsgs.say(officer,W.criminal(),"Court is not in session today.",false,false);
							W.setTravelAttemptTime(0);
							unCuff(W.criminal());
							W.setArrestingOfficer(myArea,null);
							W.setState(Law.STATE_SEEKING);
						}
					}
					else
					{
						if(officer!=null)
							CommonMsgs.say(officer,null,"Wha? Huh?",false,false);
						W.setTravelAttemptTime(0);
						unCuff(W.criminal());
						W.setArrestingOfficer(myArea,null);
						W.setState(Law.STATE_SEEKING);
					}
				}
				break;
			case Law.STATE_PAROLING:
				{
					W.setTravelAttemptTime(0);
					MOB officer=W.arrestingOfficer();
					if((officer!=null)
					&&(!W.criminal().amDead())
					&&(W.criminal().location().isInhabitant(officer))
					&&(Sense.isInTheGame(W.criminal()))
					&&(Sense.aliveAwakeMobile(officer,true))
					&&(!W.crime().equalsIgnoreCase("pardoned"))
					&&(Sense.canBeSeenBy(W.criminal(),officer)))
					{
						MOB judge=getTheJudgeHere(laws,officer.location());
						fileAllWarrants(laws,W.criminal());
						unCuff(W.criminal());
						if((judge!=null)
						&&(Sense.aliveAwakeMobile(judge,true)))
						{
							judge.location().show(judge,W.criminal(),CMMsg.MSG_OK_VISUAL,"<S-NAME> put(s) <T-NAME> on parole!");
							Ability A=CMClass.getAbility("Prisoner");
							A.startTickDown(judge,W.criminal(),W.jailTime());
							W.criminal().recoverEnvStats();
							W.criminal().recoverCharStats();
							CommonMsgs.say(judge,W.criminal(),laws.getMessage(Law.MSG_PAROLEDISMISS),false,false);
							dismissOfficer(officer);
							W.setArrestingOfficer(myArea,null);
							W.criminal().tell("\n\r\n\r");
							if(W.criminal().isMonster())
								MUDTracker.wanderAway(W.criminal(),true,true);
						}
						else
						{
							if(officer!=null)
								CommonMsgs.say(officer,null,"No court today.",false,false);
							unCuff(W.criminal());
							if(W.arrestingOfficer()!=null)
								dismissOfficer(W.arrestingOfficer());
							W.setArrestingOfficer(myArea,null);
							W.setState(Law.STATE_SEEKING);
						}
					}
					else
					{
						if(officer!=null)
							CommonMsgs.say(officer,null,"That was wierd.",false,false);
						unCuff(W.criminal());
						W.setArrestingOfficer(myArea,null);
						W.setState(Law.STATE_SEEKING);
					}
				}
				break;
			case Law.STATE_JAILING:
				{
					MOB officer=W.arrestingOfficer();
					if((officer!=null)
					&&(!W.criminal().amDead())
					&&(W.criminal().location().isInhabitant(officer))
					&&(Sense.isInTheGame(W.criminal()))
					&&(Sense.aliveAwakeMobile(officer,true))
					&&(!W.crime().equalsIgnoreCase("pardoned"))
					&&(Sense.canBeSeenBy(W.criminal(),officer)))
					{
						Room jail=findTheJail(W.criminal(),myArea,laws);
						if(jail!=null)
						{
							
							Ability A=W.criminal().fetchEffect("Prisoner");
							if(A!=null){ A.unInvoke(); W.criminal().delEffect(A);}
							
							makePeace(officer.location());
							W.setJail(jail);
							// cuff him!
							W.setState(Law.STATE_MOVING2);
							A=CMClass.getAbility("Skill_HandCuff");
							if((A!=null)&&(!Sense.isBoundOrHeld(W.criminal())))
								A.invoke(officer,W.criminal(),true);
							W.criminal().makePeace();
							makePeace(officer.location());
							CommonMsgs.stand(W.criminal(),true);
							stopTracking(officer);
							A=CMClass.getAbility("Skill_Track");
							if(A!=null)
							{
								W.setTravelAttemptTime(System.currentTimeMillis());
								A.setAbilityCode(1);
								A.invoke(officer,Util.parse(CMMap.getExtendedRoomID(jail)),jail,true);
							}
							if(officer.fetchEffect("Skill_Track")==null)
							{
								W.setTravelAttemptTime(0);
								fileAllWarrants(laws,W.criminal());
								unCuff(W.criminal());
								CommonMsgs.say(officer,W.criminal(),"I can't find the jail, you are free to go.",false,false);
								dismissOfficer(officer);
								W.setArrestingOfficer(myArea,null);
							}
							makePeace(officer.location());
						}
						else
						{
							W.setTravelAttemptTime(0);
							fileAllWarrants(laws,W.criminal());
							unCuff(W.criminal());
							CommonMsgs.say(W.arrestingOfficer(),W.criminal(),"But since there IS no jail, I will let you go.",false,false);
							dismissOfficer(officer);
							W.setArrestingOfficer(myArea,null);
						}
					}
					else
					{
						if(officer!=null)
							CommonMsgs.say(officer,null,"Crazy.",false,false);
						unCuff(W.criminal());
						W.setArrestingOfficer(myArea,null);
						W.setState(Law.STATE_SEEKING);
						W.setTravelAttemptTime(0);
					}
				}
				break;
			case Law.STATE_EXECUTING:
				{
					MOB officer=W.arrestingOfficer();
					if((officer!=null)
					&&(Sense.isInTheGame(W.criminal()))
					&&(!W.criminal().amDead())
					&&(W.criminal().location().isInhabitant(officer))
					&&(Sense.aliveAwakeMobile(officer,true))
					&&(!W.crime().equalsIgnoreCase("pardoned"))
					&&(Sense.canBeSeenBy(W.criminal(),officer))
					&&(canFocusOn(officer,W.criminal())))
					{
						MOB judge=getTheJudgeHere(laws,officer.location());
						if((judge!=null)
						&&(Sense.aliveAwakeMobile(judge,true))
						&&(judge.location()==W.criminal().location()))
						{
							fileAllWarrants(laws,W.criminal());
							unCuff(W.criminal());
							dismissOfficer(officer);
							Ability A=CMClass.getAbility("Prisoner");
							A.startTickDown(judge,W.criminal(),100);
							W.criminal().recoverEnvStats();
							W.criminal().recoverCharStats();
							MUDFight.postAttack(judge,W.criminal(),judge.fetchWieldedItem());
							W.setArrestingOfficer(myArea,null);
							W.setTravelAttemptTime(0);
						}
						else
						{
							if(officer!=null)
								CommonMsgs.say(officer,null,"Looks like court is not in session.",false,false);
							W.setTravelAttemptTime(0);
							unCuff(W.criminal());
							if(W.arrestingOfficer()!=null)
								dismissOfficer(W.arrestingOfficer());
							W.setArrestingOfficer(myArea,null);
							W.setState(Law.STATE_SEEKING);
						}
					}
					else
					{
						if(officer!=null)
							CommonMsgs.say(officer,null,"Didn't see that coming.",false,false);
						W.setTravelAttemptTime(0);
						unCuff(W.criminal());
						W.setArrestingOfficer(myArea,null);
						W.setState(Law.STATE_SEEKING);
					}
				}
				break;
			case Law.STATE_MOVING2:
				{
					MOB officer=W.arrestingOfficer();
					if((officer!=null)
					&&(!W.criminal().amDead())
					&&(W.criminal().location().isInhabitant(officer))
					&&(Sense.isInTheGame(W.criminal()))
					&&((W.travelAttemptTime()==0)||((System.currentTimeMillis()-W.travelAttemptTime())<(5*60*1000)))
					&&(Sense.aliveAwakeMobile(officer,true))
					&&(W.jail()!=null))
					{
						if(W.criminal().curState().getMovement()<50)
							W.criminal().curState().setMovement(50);
						if(officer.curState().getMovement()<50)
							officer.curState().setMovement(50);
						makePeace(officer.location());
						if(officer.isMonster()) CommonMsgs.look(officer,true);
						if(W.jail()==W.criminal().location())
						{
							unCuff(W.criminal());
							Ability A=CMClass.getAbility("Prisoner");
							if(A!=null)A.startTickDown(officer,W.criminal(),W.jailTime());
							W.criminal().recoverEnvStats();
							W.criminal().recoverCharStats();
							dismissOfficer(officer);
							if(W.criminal().fetchEffect("Prisoner")==null)
							{
								fileAllWarrants(laws,W.criminal());
								unCuff(W.criminal());
							}
							else
								W.setState(Law.STATE_RELEASE);
						}
						else
						if(Sense.flaggedAffects(officer,Ability.FLAG_TRACKING).size()==0)
						{
							Ability A=CMClass.getAbility("Skill_Track");
							if(A!=null)
							{
								stopTracking(officer);
								A.setAbilityCode(1); // tells track to cache the path
								A.invoke(officer,Util.parse(CMMap.getExtendedRoomID(W.jail())),W.jail(),true);
							}
							if(officer.fetchEffect("Skill_Track")==null)
							{
								W.setTravelAttemptTime(0);
								fileAllWarrants(laws,W.criminal());
								unCuff(W.criminal());
								CommonMsgs.say(officer,W.criminal(),"I lost the jail, so you are free to go.",false,false);
								dismissOfficer(officer);
								W.setArrestingOfficer(myArea,null);
							}
						}
						else
						if((Dice.rollPercentage()>75)&&(laws.chitChat2().size()>0))
							CommonMsgs.say(officer,W.criminal(),(String)laws.chitChat2().elementAt(Dice.roll(1,laws.chitChat2().size(),-1)),false,false);
					}
					else
					{
						unCuff(W.criminal());
						W.setArrestingOfficer(myArea,null);
						W.setState(Law.STATE_SEEKING);
						W.setTravelAttemptTime(0);
					}
				}
				break;
			case Law.STATE_RELEASE:
				{
					if(((W.criminal().fetchEffect("Prisoner")==null)||(W.crime().equalsIgnoreCase("pardoned")))
					&&(W.jail()!=null))
					{
						Ability P=W.criminal().fetchEffect("Prisoner");
						if(P!=null) P.unInvoke();
						if(W.criminal().location()==W.jail())
						{
							MOB officer=W.arrestingOfficer();
							if((officer==null)
							||(!Sense.aliveAwakeMobile(officer,true))
							||(W.criminal().amDead())
							||(!Sense.isInTheGame(W.criminal()))
							||(!W.criminal().location().isInhabitant(officer)))
							{
								W.setArrestingOfficer(myArea,getAnyElligibleOfficer(laws,W.jail().getArea(),W.criminal(),W.victim()));
								if(W.arrestingOfficer()==null) W.setArrestingOfficer(myArea,getAnyElligibleOfficer(laws,myArea,W.criminal(),W.victim()));
								if(W.arrestingOfficer()==null) break;
								officer=W.arrestingOfficer();
								W.jail().bringMobHere(officer,false);
								if(!canFocusOn(officer,W.criminal()))
								{
									W.jail().show(officer,W.criminal(),CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> arrive(s) to release <T-NAME>, but can't find <T-HIM-HER>.");
									dismissOfficer(officer);
									W.setArrestingOfficer(myArea,null);
								}
								else
									W.jail().show(officer,W.criminal(),CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> arrive(s) to release <T-NAME>.");
								Ability A=CMClass.getAbility("Skill_HandCuff");
								if((A!=null)&&(!Sense.isBoundOrHeld(W.criminal())))
									A.invoke(officer,W.criminal(),true);
							}
							W.setReleaseRoom(setReleaseRoom(laws,myArea,W.criminal()));
							W.criminal().makePeace();
							makePeace(officer.location());
							stopTracking(officer);
							Ability A=CMClass.getAbility("Skill_Track");
							if(A!=null)
							{
								W.setTravelAttemptTime(System.currentTimeMillis());
								A.invoke(officer,Util.parse(CMMap.getExtendedRoomID(W.releaseRoom())),W.releaseRoom(),true);
							}
							if(officer.fetchEffect("Skill_Track")==null)
							{
								W.setTravelAttemptTime(0);
								fileAllWarrants(laws,W.criminal());
								unCuff(W.criminal());
								CommonMsgs.say(officer,W.criminal(),"Well, you can always recall.",false,false);
								dismissOfficer(officer);
								W.setArrestingOfficer(myArea,null);
							}
						}
						else
						if(W.releaseRoom()!=null)
						{
							MOB officer=W.arrestingOfficer();
							if(W.criminal().location()==W.releaseRoom())
							{
								fileAllWarrants(laws,W.criminal());
								unCuff(W.criminal());

								if(officer!=null)
								{
									if((Sense.aliveAwakeMobile(officer,true))
									&&(W.criminal().location().isInhabitant(officer)))
										CommonMsgs.say(officer,null,laws.getMessage(Law.MSG_LAWFREE),false,false);
									dismissOfficer(officer);
								}
								W.setTravelAttemptTime(0);
							}
							else
							{
								if((officer!=null)
								&&(Sense.aliveAwakeMobile(officer,true))
								&&(W.criminal().location().isInhabitant(officer))
								&&((W.travelAttemptTime()==0)||((System.currentTimeMillis()-W.travelAttemptTime())<(5*60*1000))))
								{
									if(officer.isMonster()) CommonMsgs.look(officer,true);
									if(W.criminal().curState().getMovement()<20)
										W.criminal().curState().setMovement(20);
									if(officer.curState().getMovement()<20)
										officer.curState().setMovement(20);
									if(W.arrestingOfficer().fetchEffect("Skill_Track")==null)
									{
										stopTracking(officer);
										Ability A=CMClass.getAbility("Skill_Track");
										if(A!=null)	A.invoke(officer,Util.parse(CMMap.getExtendedRoomID(W.releaseRoom())),W.releaseRoom(),true);
										if(W.arrestingOfficer().fetchEffect("Skill_Track")==null)
										{
											W.setTravelAttemptTime(0);
											fileAllWarrants(laws,W.criminal());
											unCuff(W.criminal());
											CommonMsgs.say(W.arrestingOfficer(),W.criminal(),"Don't worry, you can always recall.",false,false);
											dismissOfficer(W.arrestingOfficer());
											W.setArrestingOfficer(myArea,null);
										}
									}
								}
								else
								{
									if(officer!=null)
										CommonMsgs.say(officer,null,"There's always recall.",false,false);
									W.setTravelAttemptTime(0);
									fileAllWarrants(laws,W.criminal());
									unCuff(W.criminal());
									if(officer!=null)
										dismissOfficer(officer);
								}
							}
						}
						else
						{
							if(W.arrestingOfficer()!=null)
								CommonMsgs.say(W.arrestingOfficer(),null,"Well, he can always recall.",false,false);
							W.setTravelAttemptTime(0);
							fileAllWarrants(laws,W.criminal());
							unCuff(W.criminal());
							if(W.arrestingOfficer()!=null)
								dismissOfficer(W.arrestingOfficer());
						}
					}
				}
				break;
			}
		}
		return true;
	}
}
