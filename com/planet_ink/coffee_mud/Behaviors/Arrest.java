package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.io.*;
import java.util.*;

public class Arrest extends StdBehavior
{
	public String ID(){return "Arrest";}
	
	public Behavior newInstance()
	{
		return new Arrest();
	}
	private Vector oldWarrants=new Vector();
	private Vector warrants=new Vector();
	private Properties laws=null;
	private Vector otherCrimes=new Vector();
	private Vector otherBits=new Vector();
	private Vector officerNames=new Vector();
	private Vector chitChat=new Vector();
	
	private static final int ACTION_WARN=0;
	private static final int ACTION_THREATEN=1;
	private static final int ACTION_PAROLE1=2;
	private static final int ACTION_PAROLE2=3;
	private static final int ACTION_PAROLE3=4;
	private static final int ACTION_PAROLE4=5;
	private static final int ACTION_JAIL1=6;
	private static final int ACTION_JAIL2=7;
	private static final int ACTION_JAIL3=8;
	private static final int ACTION_JAIL4=9;
	private static final int ACTION_EXECUTE=10;
	private static final int ACTION_HIGHEST=10;
	
	private static final int STATE_SEEKING=0;
	private static final int STATE_ARRESTING=1;
	private static final int STATE_SUBDUEING=2;
	private static final int STATE_MOVING=3;
	private static final int STATE_REPORTING=4;
	private static final int STATE_WAITING=5;
	private static final int STATE_PAROLING=6;
	private static final int STATE_JAILING=7;
	private static final int STATE_EXECUTING=8;
	
	private static final int BIT_CRIMELOCS=0;
	private static final int BIT_CRIMEFLAGS=1;
	private static final int BIT_CRIMENAME=2;
	private static final int BIT_SENTENCE=3;
	private static final int BIT_WARNMSG=4;
	
	private class ArrestWarrant implements Cloneable
	{
		public MOB criminal=null;
		public MOB victim=null;
		public MOB witness=null;
		public MOB arrestingOfficer=null;
		public String crime="";
		public int actionCode=-1;
		public int jailTime=0;
		public int state=-1;
		public int offenses=0;
		public long lastOffense=0;
		public String warnMsg=null;
		public void setArrestingOfficer(MOB mob){ arrestingOfficer=mob;	}
	}

	private Properties getLaws()
	{
		if(laws==null)
		{
			String lawName=getParms();
			if(lawName.length()==0)
				lawName="laws.ini";
			laws=new Properties();
			try{laws.load(new FileInputStream("resources"+File.separatorChar+lawName));}catch(IOException e){Log.errOut("Arrest",e);}
			String officers=(String)laws.get("OFFICERS");
			if((officers!=null)&&(officers.length()>0))
				officerNames=Util.parse(officers);
			if((officers!=null)&&(officers.length()>0))
				officerNames=Util.parse(officers);
			String chat=(String)laws.get("CHITCHAT");
			if((chat!=null)&&(chat.length()>0))
				chitChat=Util.parse(chat);
			
			for(Enumeration e=laws.keys();e.hasMoreElements();)
			{
				String key=(String)e.nextElement();
				String words=(String)laws.get(key);
				if(key.startsWith("CRIME"))
				{
					int x=words.indexOf(";");
					if(x>0)
					{
						otherCrimes.addElement(Util.parse(words.substring(0,x)));
						otherBits.addElement(words.substring(x+1));
					}
					laws.remove(key);
				}
			}
		}
		return laws;
	}
	
	public String getBit(String words, int which)
	{
		int x=words.indexOf(";");
		int one=0;
		while(x>=0)
		{
			if(one==which)
				return words.substring(0,x);
			one++;
			words=words.substring(x+1);
			x=words.indexOf(";");
		}
		if(which==one)
			return words;
		return "";
	}

	public ArrestWarrant getWarrant(MOB mob, int which)
	{
		int one=0;
		for(int i=0;i<warrants.size();i++)
		{
			ArrestWarrant W=(ArrestWarrant)warrants.elementAt(i);
			if(W.criminal==mob)
			{
				if(which==one)
					return W;
				one++;
			}
		}
		return null;
	}
	
	public void unCuff(MOB mob)
	{
		Ability A=mob.fetchAffect("Skill_HandCuff");
		if(A!=null) A.unInvoke();
	}

	
	public void setFree(MOB mob)
	{
		fileAllWarrants(mob);
		unCuff(mob);
	}
	public void dismissOfficer(MOB officer)
	{
		if(officer==null) return;
		if((officer.getStartRoom()!=null)
		&&(officer.location()!=null)
		&&(officer.getStartRoom()==officer.location()))
			return;
		Behavior B=null;
		for(int i=0;i<officer.numBehaviors();i++)
		{
			Behavior B2=officer.fetchBehavior(i);
			if(B2 instanceof Mobile)
			{
				B=B2;
				if(B2.ID().equalsIgnoreCase("Mobile"))
					break;
			}
		}
		if(B!=null)
		for(int i=0;i<20;i++)
			B.tick(officer,Host.MOB_TICK);
		if(officer.getStartRoom()!=null)
			officer.getStartRoom().bringMobHere(officer,false);
	}
	
	public MOB getAWitnessHere(Room R)
	{
		if(R!=null)
		for(int i=0;i<R.numInhabitants();i++)
		{
			MOB M=R.fetchInhabitant(i);
			if(M.isMonster()
			   &&(M.charStats().getStat(CharStats.INTELLIGENCE)>3)
			   &&(Dice.rollPercentage()<=(M.getAlignment()/10))
			   )
				return M;
		}
		return null;
	}
	
	public MOB getWitness(Area A, MOB mob)
	{
		Room R=mob.location();
		
		if((A!=null)&&(R.getArea()!=A)) 
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
	
	public boolean isStillACrime(ArrestWarrant W)
	{
		// will witness talk, or victim press charges?
		Hashtable H=W.criminal.getGroupMembers(new Hashtable());
		if(W.witness.amDead()) return false;
		if(W.arrestingOfficer!=null)
		{ 
			if(W.witness==W.arrestingOfficer)
				return true;
			if((W.victim!=null)&&(W.victim==W.arrestingOfficer))
				return true;
		}
		   
		if(H.containsKey(W.witness)) return false;
		if((W.victim!=null)&&(H.containsKey(W.victim))) return false;
		return true;
	}
	
	public int highestCrimeAction(MOB criminal)
	{
		int num=0;
		int highest=-1;
		for(int w2=0;w2<warrants.size();w2++)
		{
			ArrestWarrant W2=(ArrestWarrant)warrants.elementAt(w2);
			if(W2.criminal==criminal)
			{
				num++;
				if((W2.actionCode+W2.offenses)>highest)
					highest=(W2.actionCode+W2.offenses);
			}
		}
		highest+=num;
		highest--;
		if(highest>ACTION_HIGHEST) highest=ACTION_HIGHEST;
		int adjusted=highest;
		if((criminal.getAlignment()>650)&&(adjusted>0))
			adjusted--;
		return adjusted;
	}
	
	public boolean judgeMe(MOB judge, MOB officer, MOB criminal, ArrestWarrant W)
	{
		switch(highestCrimeAction(criminal))
		{
		case ACTION_WARN:
			{
			if((judge==null)&&(officer!=null)) judge=officer;
			StringBuffer str=new StringBuffer("");
			str.append(criminal.name()+", you are in trouble for "+restOfCharges(criminal)+".  ");
			for(int w2=0;w2<warrants.size();w2++)
			{
				ArrestWarrant W2=(ArrestWarrant)warrants.elementAt(w2);
				if(W2.criminal==criminal)
				{
					str.append("The charge of "+fixCharge(W2)+" was witnessed by "+W2.witness.name()+".  ");
					if((W2.warnMsg!=null)&&(W2.warnMsg.length()>0))
						str.append(W2.warnMsg+"  ");
					if((W2.offenses>0)&&(laws.get("PREVOFFMSG")!=null)&&(((String)laws.get("PREVOFFMSG")).length()>0))
						str.append(((String)laws.get("PREVOFFMSG"))+"  ");
				}
			}
			if((laws.get("WARNINGMSG")!=null)&&(((String)laws.get("WARNINGMSG")).length()>0))
				str.append(((String)laws.get("WARNINGMSG"))+"  ");
			ExternalPlay.quickSay(judge,criminal,str.toString(),false,false);
			}
			return true;
		case ACTION_THREATEN:
			{
			if((judge==null)&&(officer!=null)) judge=officer;
			StringBuffer str=new StringBuffer("");
			str.append(criminal.name()+", you are in trouble for "+restOfCharges(criminal)+".  ");
			for(int w2=0;w2<warrants.size();w2++)
			{
				ArrestWarrant W2=(ArrestWarrant)warrants.elementAt(w2);
				if(W2.criminal==criminal)
				{
					str.append("The charge of "+fixCharge(W2)+" was witnessed by "+W2.witness.name()+".  ");
					if((W2.warnMsg!=null)&&(W2.warnMsg.length()>0))
						str.append(W2.warnMsg+"  ");
					if((W2.offenses>0)&&(laws.get("PREVOFFMSG")!=null)&&(((String)laws.get("PREVOFFMSG")).length()>0))
						str.append(((String)laws.get("PREVOFFMSG"))+"  ");
				}
			}
			if((laws.get("THREATMSG")!=null)&&(((String)laws.get("THREATMSG")).length()>0))
				str.append(((String)laws.get("THREATMSG"))+"  ");
			ExternalPlay.quickSay(judge,criminal,str.toString(),false,false);
			}
			return true;
		case ACTION_PAROLE1:
			if(judge!=null)
			{
				if((W.offenses>0)&&(laws.get("PREVOFFMSG")!=null)&&(((String)laws.get("PREVOFFMSG")).length()>0))
					ExternalPlay.quickSay(judge,W.criminal,(String)laws.get("PREVOFFMSG"),false,false);
				if((laws.get("PAROLE1MSG")!=null)&&(((String)laws.get("PAROLE1MSG")).length()>0))
					ExternalPlay.quickSay(judge,criminal,(String)laws.get("PAROLE1MSG"),false,false);
				W.jailTime=Util.s_int((String)laws.get("PAROLE1TIME"));
				W.state=STATE_PAROLING;
			}
			return false;
		case ACTION_PAROLE2:
			if(judge!=null)
			{
				if((W.offenses>0)&&(laws.get("PREVOFFMSG")!=null)&&(((String)laws.get("PREVOFFMSG")).length()>0))
					ExternalPlay.quickSay(judge,W.criminal,(String)laws.get("PREVOFFMSG"),false,false);
				if((laws.get("PAROLE2MSG")!=null)&&(((String)laws.get("PAROLE2MSG")).length()>0))
					ExternalPlay.quickSay(judge,criminal,(String)laws.get("PAROLE2MSG"),false,false);
				W.jailTime=Util.s_int((String)laws.get("PAROLE2TIME"));
				W.state=STATE_PAROLING;
			}
			return false;
		case ACTION_PAROLE3:
			if(judge!=null)
			{
				if((W.offenses>0)&&(laws.get("PREVOFFMSG")!=null)&&(((String)laws.get("PREVOFFMSG")).length()>0))
					ExternalPlay.quickSay(judge,W.criminal,(String)laws.get("PREVOFFMSG"),false,false);
				if((laws.get("PAROLE3MSG")!=null)&&(((String)laws.get("PAROLE3MSG")).length()>0))
					ExternalPlay.quickSay(judge,criminal,(String)laws.get("PAROLE3MSG"),false,false);
				W.jailTime=Util.s_int((String)laws.get("PAROLE3TIME"));
				W.state=STATE_PAROLING;
			}
			return false;
		case ACTION_PAROLE4:
			if(judge!=null)
			{
				if((W.offenses>0)&&(laws.get("PREVOFFMSG")!=null)&&(((String)laws.get("PREVOFFMSG")).length()>0))
					ExternalPlay.quickSay(judge,W.criminal,(String)laws.get("PREVOFFMSG"),false,false);
				if((laws.get("PAROLE4MSG")!=null)&&(((String)laws.get("PAROLE4MSG")).length()>0))
					ExternalPlay.quickSay(judge,criminal,(String)laws.get("PAROLE4MSG"),false,false);
				W.jailTime=Util.s_int((String)laws.get("PAROLE4TIME"));
				W.state=STATE_PAROLING;
			}
			return false;
		case ACTION_JAIL1:
			if(judge!=null)
			{
				if((W.offenses>0)&&(laws.get("PREVOFFMSG")!=null)&&(((String)laws.get("PREVOFFMSG")).length()>0))
					ExternalPlay.quickSay(judge,W.criminal,(String)laws.get("PREVOFFMSG"),false,false);
				if((laws.get("JAIL1MSG")!=null)&&(((String)laws.get("JAIL1MSG")).length()>0))
					ExternalPlay.quickSay(judge,criminal,(String)laws.get("JAIL1MSG"),false,false);
				W.jailTime=Util.s_int((String)laws.get("JAIL1TIME"));
				W.state=STATE_JAILING;
			}
			return false;
		case ACTION_JAIL2:
			if(judge!=null)
			{
				if((W.offenses>0)&&(laws.get("PREVOFFMSG")!=null)&&(((String)laws.get("PREVOFFMSG")).length()>0))
					ExternalPlay.quickSay(judge,criminal,(String)laws.get("PREVOFFMSG"),false,false);
				if((laws.get("JAIL2MSG")!=null)&&(((String)laws.get("JAIL2MSG")).length()>0))
					ExternalPlay.quickSay(judge,criminal,(String)laws.get("JAIL2MSG"),false,false);
				W.jailTime=Util.s_int((String)laws.get("JAIL2TIME"));
				W.state=STATE_JAILING;
			}
			return false;
		case ACTION_JAIL3:
			if(judge!=null)
			{
				if((W.offenses>0)&&(laws.get("PREVOFFMSG")!=null)&&(((String)laws.get("PREVOFFMSG")).length()>0))
					ExternalPlay.quickSay(judge,criminal,(String)laws.get("PREVOFFMSG"),false,false);
				if((laws.get("JAIL3MSG")!=null)&&(((String)laws.get("JAIL3MSG")).length()>0))
					ExternalPlay.quickSay(judge,criminal,(String)laws.get("JAIL3MSG"),false,false);
				W.jailTime=Util.s_int((String)laws.get("JAIL3TIME"));
				W.state=STATE_JAILING;
			}
			return false;
		case ACTION_JAIL4:
			if(judge!=null)
			{
				if((W.offenses>0)&&(laws.get("PREVOFFMSG")!=null)&&(((String)laws.get("PREVOFFMSG")).length()>0))
					ExternalPlay.quickSay(judge,criminal,(String)laws.get("PREVOFFMSG"),false,false);
				if((laws.get("JAIL4MSG")!=null)&&(((String)laws.get("JAIL4MSG")).length()>0))
					ExternalPlay.quickSay(judge,criminal,(String)laws.get("JAIL4MSG"),false,false);
				W.jailTime=Util.s_int((String)laws.get("JAIL4TIME"));
				W.state=STATE_JAILING;
			}
			return false;
		case ACTION_EXECUTE:
			if(judge!=null)
			{
				if((W.offenses>0)&&(laws.get("PREVOFFMSG")!=null)&&(((String)laws.get("PREVOFFMSG")).length()>0))
					ExternalPlay.quickSay(judge,criminal,(String)laws.get("PREVOFFMSG"),false,false);
				if((laws.get("EXECUTEMSG")!=null)&&(((String)laws.get("EXECUTEMSG")).length()>0))
					ExternalPlay.quickSay(judge,criminal,(String)laws.get("EXECUTEMSG"),false,false);
				W.state=STATE_EXECUTING;
			}
			return false;
		}
		return true;
	}
	
	public void fileAllWarrants(MOB mob)
	{
		ArrestWarrant W=null;
		Vector V=new Vector();
		for(int i=0;(W=getWarrant(mob,i))!=null;i++)
			if(W.criminal==mob)
				V.addElement(W);
		for(int v=0;v<V.size();v++)
		{
			W=(ArrestWarrant)V.elementAt(v);
			warrants.removeElement(W);
			if(W.crime!=null)
			{
				boolean found=false;
				for(int w=0;w<oldWarrants.size();w++)
				{
					ArrestWarrant oW=(ArrestWarrant)oldWarrants.elementAt(w);
					if((oW.criminal==mob)
					&&(oW.crime!=null)
					&&(oW.crime.equals(W.crime)))
						found=true;
				}
				if(!found)
				{
					W.offenses++;
					oldWarrants.addElement(W);
				}
			}
		}
		
	}

	public ArrestWarrant getOldWarrant(MOB criminal, String crime, boolean pull)
	{
		ArrestWarrant W=null;
		for(int i=0;i<oldWarrants.size();i++)
		{
			ArrestWarrant W2=(ArrestWarrant)oldWarrants.elementAt(i);
			if((W2.criminal==criminal)&&(W2.crime.equals(crime)))
			{
				W=W2;
				if(pull) oldWarrants.removeElement(W2);
				break;
			}
		}
		return W;
	}
	
	public boolean fillOutWarrant(MOB mob, 
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
		if((myArea!=null)&&(mob.location().getArea()!=myArea)) 
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
					for(int a=0;a<mob.location().numAffects();a++)
					{
						Ability A=mob.location().fetchAffect(a);
						if((A!=null)&&(A instanceof LandTitle))
						{
							LandTitle L=(LandTitle)A;
							if(L.landOwner().equals(mob.name()))
								if(str.startsWith("!")) return false;
						}
					}
					if(!str.startsWith("!")) return false;
					aCrime=true;
				}
				else
				if(str.startsWith("!")&&(CoffeeUtensils.containsString(display,str.substring(1))))
					return false;
				else
				if(CoffeeUtensils.containsString(display,str))
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
					ArrestWarrant W=getOldWarrant(mob,crime,false);
					long thisTime=System.currentTimeMillis();
					if((W!=null)&&((thisTime-W.lastOffense)<600000))
					{
						if(str.startsWith("!")) return false;
					}
					else
						if(!str.startsWith("!")) return false;
				}
			}
		}
		
		// is the victim a protected race?
		if(victim!=null)
		{
			String races=(String)laws.get("PROTECTED");
			if((races!=null)&&(races.length()>0)&&(!CoffeeUtensils.containsString(races,victim.charStats().getMyRace().racialCategory())))
			   return false;
		}
		
		// does a warrant already exist?
		ArrestWarrant W=null;
		for(int i=0;(W=getWarrant(mob,i))!=null;i++)
		{
			if((W.criminal==mob)
			&&(W.victim==victim)
			&&(W.crime.equals(crime)))
				return false;
		}
		if(W==null) W=getOldWarrant(mob,crime,true);
		if(W==null) W=new ArrestWarrant();
		
		// fill out the warrant!
		W.criminal=mob;
		W.victim=victim;
		W.crime=crime;
		W.state=STATE_SEEKING;
		W.witness=witness;
		W.lastOffense=System.currentTimeMillis();
		W.warnMsg=warnMsg;
		sentence=sentence.trim();
		if(sentence.equalsIgnoreCase("warning"))
			W.actionCode=ACTION_WARN;
		else
		if(sentence.equalsIgnoreCase("threat"))
			W.actionCode=ACTION_THREATEN;
		else
		if(sentence.equalsIgnoreCase("parole1"))
			W.actionCode=ACTION_PAROLE1;
		else
		if(sentence.equalsIgnoreCase("parole2"))
			W.actionCode=ACTION_PAROLE2;
		else
		if(sentence.equalsIgnoreCase("parole3"))
			W.actionCode=ACTION_PAROLE3;
		else
		if(sentence.equalsIgnoreCase("parole4"))
			W.actionCode=ACTION_PAROLE4;
		else
		if(sentence.equalsIgnoreCase("jail1"))
			W.actionCode=ACTION_JAIL1;
		else
		if(sentence.equalsIgnoreCase("jail2"))
			W.actionCode=ACTION_JAIL2;
		else
		if(sentence.equalsIgnoreCase("jail3"))
			W.actionCode=ACTION_JAIL3;
		else
		if(sentence.equalsIgnoreCase("jail4"))
			W.actionCode=ACTION_JAIL4;
		else
		if(sentence.equalsIgnoreCase("death"))
			W.actionCode=ACTION_EXECUTE;
		else
		{
			Log.errOut("Arrest","Unknown sentence: "+sentence+" for crime "+crime);
			return false;
		}
		
		if((isStillACrime(W))
		&&(Sense.canBeSeenBy(W.criminal,W.witness)))
			warrants.addElement(W);
		return true;
	}
	
	public void affect(Environmental affecting, Affect affect)
	{
		super.affect(affecting, affect);
		if(!(affecting instanceof Area)) return;
		Area myArea=(Area)affecting;
		
		if(laws==null) laws=getLaws();
		if(affect.source()==null) return;
		
		if(affect.sourceMinor()==Affect.TYP_DEATH)
		{
			String info=(String)laws.get("MURDER");
			if((info!=null)&&(info.length()>0))
			for(int i=warrants.size()-1;i>=0;i--)
			{
				ArrestWarrant W=(ArrestWarrant)warrants.elementAt(i);
				if((W.victim!=null)&&(W.victim==affect.source()))
				{
					fillOutWarrant(W.criminal,
								   myArea,
								   W.victim,
								   getBit(info,BIT_CRIMELOCS),
								   getBit(info,BIT_CRIMEFLAGS),
								   getBit(info,BIT_CRIMENAME),
								   getBit(info,BIT_SENTENCE),
								   getBit(info,BIT_WARNMSG));
				}
				else
				if(W.criminal==affect.source())
					warrants.removeElement(W);
			}
		}
		
		if(affect.source().isMonster()) return;

		if(!Sense.aliveAwakeMobile(affect.source(),true))
			return;
		
		if(affect.source().location()==null) return;
		
		if((affect.tool()!=null)
		   &&(affect.tool() instanceof Ability)
		   &&(affect.othersMessage()!=null))
		{
			String info=(String)laws.get(affect.tool().ID().toUpperCase());
			if((info!=null)&&(info.length()>0))
				fillOutWarrant(affect.source(),
								myArea,
								affect.target(),
								getBit(info,BIT_CRIMELOCS),
								getBit(info,BIT_CRIMEFLAGS),
								getBit(info,BIT_CRIMENAME),
								getBit(info,BIT_SENTENCE),
								getBit(info,BIT_WARNMSG));
		}
		
		if((Util.bset(affect.targetCode(),Affect.MASK_MALICIOUS))
		   &&(affect.target()!=null)
		   &&((affect.tool()==null)||(affect.source().isMine(affect.tool())))
		   &&(affect.target()!=affect.source()))
		{
			String info=(String)laws.get("ASSAULT");
			if((info!=null)&&(info.length()>0))
				fillOutWarrant(affect.source(),
								myArea,
								affect.target(),
								getBit(info,BIT_CRIMELOCS),
								getBit(info,BIT_CRIMEFLAGS),
								getBit(info,BIT_CRIMENAME),
								getBit(info,BIT_SENTENCE),
								getBit(info,BIT_WARNMSG));
		}
		
		if((affect.othersCode()!=Affect.NO_EFFECT)
		   &&(affect.othersMessage()!=null))
		{
			if(affect.sourceMinor()==Affect.TYP_EXAMINESOMETHING)
			{
				String nudity=(String)laws.get("NUDITY");
				if((nudity!=null)
				&&(nudity.length()>0)
				&&(affect.source().fetchWornItem(Item.ON_LEGS)==null)
				&&(affect.source().fetchWornItem(Item.ON_WAIST)==null)
				&&(affect.source().fetchWornItem(Item.ABOUT_BODY)==null))
					fillOutWarrant(affect.source(),
								   myArea,
								   null,
								   getBit(nudity,BIT_CRIMELOCS),
								   getBit(nudity,BIT_CRIMEFLAGS),
								   getBit(nudity,BIT_CRIMENAME),
								   getBit(nudity,BIT_SENTENCE),
								   getBit(nudity,BIT_WARNMSG));
				
				String armed=(String)laws.get("ARMED");
				Item w=null;
				if((armed!=null)
				&&(armed.length()>0)
				&&((w=affect.source().fetchWieldedItem())!=null)
				&&(w instanceof Weapon)
				&&(((Weapon)w).weaponClassification()!=Weapon.CLASS_NATURAL)
				&&(((Weapon)w).weaponClassification()!=Weapon.CLASS_HAMMER)
				&&(((Weapon)w).weaponClassification()!=Weapon.CLASS_STAFF)
				&&(Sense.isSeen(w))
				&&(!Sense.isHidden(w))
				&&(!Sense.isInvisible(w)))
					fillOutWarrant(affect.source(),
								   myArea,
								   null,
								   getBit(armed,BIT_CRIMELOCS),
								   getBit(armed,BIT_CRIMEFLAGS),
								   getBit(armed,BIT_CRIMENAME),
								   getBit(armed,BIT_SENTENCE),
								   getBit(armed,BIT_WARNMSG));
			}
			for(int i=0;i<otherCrimes.size();i++)
			{
				Vector V=(Vector)otherCrimes.elementAt(i);
				for(int v=0;v<V.size();v++)
				{
					if(CoffeeUtensils.containsString(affect.othersMessage(),(String)V.elementAt(v)))
					{
						String info=(String)otherBits.elementAt(i);
						fillOutWarrant(affect.source(),
										myArea,
										affect.target(),
										getBit(info,BIT_CRIMELOCS),
										getBit(info,BIT_CRIMEFLAGS),
										getBit(info,BIT_CRIMENAME),
										getBit(info,BIT_SENTENCE),
										getBit(info,BIT_WARNMSG));
					}
				}
			}
		}
	}
	
	
	public boolean isBusyWithJustice(MOB M)
	{
		for(int w=0;w<warrants.size();w++)
		{
			ArrestWarrant W=(ArrestWarrant)warrants.elementAt(w);
			if(W.arrestingOfficer!=null)
			{
				if(W.criminal==M) return true;
				else
				if(W.arrestingOfficer==M) return true;
			}
		}
		return false;
	}
	
	public boolean isElligibleOfficer(MOB M, Area myArea)
	{
		if((M.isMonster())&&(M.location()!=null))
		{
			if((myArea!=null)&&(M.location().getArea()!=myArea)) return false;
			
			for(int i=0;i<officerNames.size();i++)
				if(CoffeeUtensils.containsString(M.displayText(),(String)officerNames.elementAt(i)))
				{
					if((!isBusyWithJustice(M))
					   &&(Sense.aliveAwakeMobile(M,true))
					   &&(!M.isInCombat()))
					{
						for(int b=0;b<M.numBehaviors();b++)
						{
							Behavior B=M.fetchBehavior(b);
							if((B!=null)&&(B instanceof Mobile))
								return true;
						}
					}
					return false;
				}
		}
		return false;
	}
	
	public MOB getElligibleOfficerHere(Area myArea, Room R, MOB criminal, MOB victim)
	{
		if(R==null) return null;
		for(int i=0;i<R.numInhabitants();i++)
		{
			MOB M=R.fetchInhabitant(i);
			if((M!=criminal)
			   &&(M.location()!=null)
			   &&(M.location().getArea()==myArea)
			   &&((victim==null)||(M!=victim))
			   &&(isElligibleOfficer(M,myArea))
			   &&(Sense.canBeSeenBy(criminal,M)))
				return M;
		}
		return null;
	}
	
	public MOB getElligibleOfficer(Area myArea, MOB criminal, MOB victim)
	{
		Room R=criminal.location();
		if(R==null) return null;
		if((myArea!=null)&&(R.getArea()!=myArea)) return null;
		MOB M=getElligibleOfficerHere(myArea,R,criminal,victim);
		if(M!=null) return M;
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			Room R2=R.getRoomInDir(d);
			if(R2!=null)
			{
				M=getElligibleOfficerHere(myArea,R2,criminal,victim);
				if(M!=null)
				{
					int direction=Directions.getOpDirectionCode(d);
					ExternalPlay.move(M,direction,false,false);
					if(M.location()==R) return M;
				}
			}
		}
		return null;
	}

	public String fixCharge(ArrestWarrant W)
	{
		if(W==null) return "";
		String charge=W.crime;
		if(W.victim==null) return charge;
		if(charge.indexOf("<T-NAME>")<0) return charge;
		return charge.replaceFirst("<T-NAME>",W.victim.name());
	}
	
	public String restOfCharges(MOB mob)
	{
		StringBuffer msg=new StringBuffer("");
		for(int w=0;(getWarrant(mob,w)!=null);w++)
		{
			ArrestWarrant W=getWarrant(mob,w);
			if(W!=null)
			{
				if(w==0)
					msg.append("for "+fixCharge(W));
				else
				if(getWarrant(mob,w+1)==null)
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
	
	public void tick(Environmental ticking, int tickID)
	{
		super.tick(ticking,tickID);

		if(tickID!=Host.AREA_TICK) return;
		if(laws==null) laws=getLaws();
		if(!(ticking instanceof Area)) return;
		Area myArea=(Area)ticking;
		
		
		
		Hashtable handled=new Hashtable();
		for(int w=warrants.size()-1;w>=0;w--)
		{
			ArrestWarrant W=null;
			try{ W=(ArrestWarrant)warrants.elementAt(w);
			} catch(Exception e){ continue;}
			
			if(!handled.contains(W.criminal))
			{
				if(!isStillACrime(W))
				{
					unCuff(W.criminal);
					if(W.arrestingOfficer!=null)
						dismissOfficer(W.arrestingOfficer);
					W.setArrestingOfficer(null);
					W.offenses++;
					oldWarrants.addElement(W);
					warrants.removeElement(W);
					continue;
				}
				handled.put(W.criminal,W.criminal);
				switch(W.state)
				{
				case STATE_SEEKING:
					{
						MOB officer=W.arrestingOfficer;
						if((officer==null)||(!W.criminal.location().isInhabitant(officer)))
						   officer=null;
						if(officer==null)
							officer=getElligibleOfficer(myArea,W.criminal,W.victim);
						if((officer!=null)
						&&(W.criminal.location().isInhabitant(officer))
						&&(Sense.canBeSeenBy(W.criminal,officer)))
						{
							if(W.criminal.isASysOp(W.criminal.location()))
							{
								ExternalPlay.quickSay(officer,W.criminal,"Damn, I can't arrest you.",false,false);
								if(W.criminal.isASysOp(null))
								{
									setFree(W.criminal);
									W.setArrestingOfficer(null);
								}
							}
							else
							if(judgeMe(null,officer,W.criminal,W))
							{
								setFree(W.criminal);
								dismissOfficer(officer);
								W.setArrestingOfficer(null);
							}
							else
							{
								W.setArrestingOfficer(officer);
								ExternalPlay.quickSay(W.arrestingOfficer,W.criminal,"You are under arrest "+restOfCharges(W.criminal)+"! Sit down on the ground immediately!",false,false);
								W.state=STATE_ARRESTING;
							}
						}
					}
					break;
				case STATE_ARRESTING:
					{
						MOB officer=W.arrestingOfficer;
						if((officer!=null)
						&&(W.criminal.location().isInhabitant(officer))
						&&(Sense.aliveAwakeMobile(officer,true))
						&&(Sense.canBeSeenBy(W.criminal,officer)))
						{
							if(officer.isInCombat())
							{
								if(officer.getVictim()==W.criminal)
								{
									ExternalPlay.quickSay(officer,W.criminal,(String)laws.get("RESISTFIGHTMSG"),false,false);
									W.state=STATE_SUBDUEING;
								}
								else
								{
									W.setArrestingOfficer(null);
									W.state=STATE_SEEKING;
								}
							}
							else
							{
								if(Sense.isSitting(W.criminal)||Sense.isSleeping(W.criminal))
									ExternalPlay.quickSay(officer,W.criminal,(String)laws.get("NORESISTMSG"),false,false);
								else
									ExternalPlay.quickSay(officer,W.criminal,(String)laws.get("RESISTWARNMSG"),false,false);
								W.state=STATE_SUBDUEING;
							}
						}
						else
						{
							W.setArrestingOfficer(null);
							W.state=STATE_SEEKING;
						}
					}
					break;
				case STATE_SUBDUEING:
					{
						MOB officer=W.arrestingOfficer;
						if((officer!=null)
						&&(W.criminal.location().isInhabitant(officer))
						&&(Sense.aliveAwakeMobile(officer,true))
						&&(Sense.canBeSeenBy(W.criminal,officer)))
						{
							if(!Sense.isSitting(W.criminal)&&(!Sense.isSleeping(W.criminal)))
							{
								if(!W.arrestingOfficer.isInCombat())
									ExternalPlay.quickSay(officer,W.criminal,(String)laws.get("RESISTMSG"),false,false);
								
								Ability A=CMClass.getAbility("Fighter_Whomp");
								if(A!=null){
									int curPoints=W.criminal.curState().getHitPoints();
									double pct=Util.div(curPoints,W.criminal.maxState().getHitPoints());
									A.setProfficiency((int)(100-Math.round(Util.mul(pct,50))));
									if(!A.invoke(officer,W.criminal,(curPoints<25)))
									{
										A=CMClass.getAbility("Skill_Trip");
										curPoints=W.criminal.curState().getHitPoints();
										pct=Util.div(curPoints,W.criminal.maxState().getHitPoints());
										A.setProfficiency((int)(100-Math.round(Util.mul(pct,50))));
										if(!A.invoke(officer,W.criminal,(curPoints<25)))
											ExternalPlay.postAttack(officer,W.criminal,officer.fetchWieldedItem());
									}
								}
							}
							if(Sense.isSitting(W.criminal)||(Sense.isSleeping(W.criminal)))
							{
								makePeace(officer.location());
								
								// cuff him!
								W.state=STATE_MOVING;
								Ability A=CMClass.getAbility("Skill_HandCuff");
								if(A!=null)	A.invoke(officer,W.criminal,true);
								A=W.criminal.fetchAffect("Fighter_Whomp");
								if(A!=null)A.unInvoke();
								A=W.criminal.fetchAffect("Skill_Trip");
								if(A!=null)A.unInvoke();
								makePeace(officer.location());
								ExternalPlay.standIfNecessary(W.criminal);
								A=CMClass.getAbility("Ranger_Track");
								if(A!=null)	A.invoke(officer,Util.parse((String)laws.get("JUDGE")),null,true);
							}
						}
						else
						{
							unCuff(W.criminal);
							W.setArrestingOfficer(null);
							W.state=STATE_SEEKING;
						}
					}
					break;
				case STATE_MOVING:
					{
						MOB officer=W.arrestingOfficer;
						
						if((officer!=null)
						&&(W.criminal.location().isInhabitant(officer))
						&&(Sense.aliveAwakeMobile(officer,true))
						&&(Sense.canBeSeenBy(W.criminal,officer)))
						{
							if(W.criminal.curState().getMovement()<20)
								W.criminal.curState().setMovement(20);
							if(officer.curState().getMovement()<20)
								officer.curState().setMovement(20);
							makePeace(officer.location());
							ExternalPlay.look(officer,null,true);
							if(officer.location().fetchInhabitant((String)laws.get("JUDGE"))!=null)
								W.state=STATE_REPORTING;
							else
							if(W.arrestingOfficer.fetchAffect("Ranger_Track")==null)
							{
								Ability A=CMClass.getAbility("Ranger_Track");
								if(A!=null)	A.invoke(officer,Util.parse((String)laws.get("JUDGE")),null,true);
							}
							else
							if((Dice.rollPercentage()>75)&&(chitChat.size()>0))
								ExternalPlay.quickSay(officer,W.criminal,(String)chitChat.elementAt(Dice.roll(1,chitChat.size(),-1)),false,false);
						}
						else
						{
							unCuff(W.criminal);
							W.setArrestingOfficer(null);
							W.state=STATE_SEEKING;
						}
					}
					break;
				case STATE_REPORTING:
					{
						MOB officer=W.arrestingOfficer;
						if((officer!=null)
						&&(W.criminal.location().isInhabitant(officer))
						&&(Sense.aliveAwakeMobile(officer,true))
						&&(Sense.canBeSeenBy(W.criminal,officer)))
						{
							MOB judge=officer.location().fetchInhabitant((String)laws.get("JUDGE"));
							if(judge==null)
							{
								W.state=STATE_MOVING;
								Ability A=W.arrestingOfficer.fetchAffect("Ranger_Track");
								if(A!=null) officer.delAffect(A);
								A=CMClass.getAbility("Ranger_Track");
								if(A!=null)	A.invoke(officer,Util.parse((String)laws.get("JUDGE")),null,true);
							}
							else
							if(Sense.aliveAwakeMobile(judge,true))
							{
								
								String sirmaam="Sir";
								if(Character.toString((char)judge.charStats().getStat(CharStats.GENDER)).equalsIgnoreCase("F"))
									sirmaam="Ma'am";
								ExternalPlay.quickSay(officer,judge,sirmaam+", "+W.criminal.name()+" has been arrested "+restOfCharges(W.criminal)+".",false,false);
								for(int w2=0;w2<warrants.size();w2++)
								{
									ArrestWarrant W2=(ArrestWarrant)warrants.elementAt(w2);
									if(W2.criminal==W.criminal)
										ExternalPlay.quickSay(officer,judge,"The charge of "+fixCharge(W2)+" was witnessed by "+W2.witness.name()+".",false,false);
								}
								W.state=STATE_WAITING;
							}
							else
							{
								unCuff(W.criminal);
								W.setArrestingOfficer(null);
								W.state=STATE_SEEKING;
							}
						}
						else
						{
							unCuff(W.criminal);
							W.setArrestingOfficer(null);
							W.state=STATE_SEEKING;
						}
					}
					break;
				case STATE_WAITING:
					{
						MOB officer=W.arrestingOfficer;
						if((officer!=null)
						&&(W.criminal.location().isInhabitant(officer))
						&&(Sense.aliveAwakeMobile(officer,true))
						&&(Sense.canBeSeenBy(W.criminal,officer)))
						{
							MOB judge=officer.location().fetchInhabitant((String)laws.get("JUDGE"));
							if(judge==null)
							{
								W.state=STATE_MOVING;
								Ability A=W.arrestingOfficer.fetchAffect("Ranger_Track");
								if(A!=null) officer.delAffect(A);
								A=CMClass.getAbility("Ranger_Track");
								A.invoke(officer,Util.parse((String)laws.get("JUDGE")),null,true);
							}
							else
							if(Sense.aliveAwakeMobile(judge,true))
							{
								if(judgeMe(judge,officer,W.criminal,W))
								{
									unCuff(W.criminal);
									dismissOfficer(officer);
									setFree(W.criminal);
									W.setArrestingOfficer(null);
								}
								// else, still stuff to do
							}
							else
							{
								unCuff(W.criminal);
								W.setArrestingOfficer(null);
								W.state=STATE_SEEKING;
							}
						}
						else
						{
							unCuff(W.criminal);
							W.setArrestingOfficer(null);
							W.state=STATE_SEEKING;
						}
					}
					break;
				case STATE_PAROLING:
					{
						MOB officer=W.arrestingOfficer;
						if((officer!=null)
						&&(W.criminal.location().isInhabitant(officer))
						&&(Sense.aliveAwakeMobile(officer,true))
						&&(Sense.canBeSeenBy(W.criminal,officer)))
						{
							MOB judge=officer.location().fetchInhabitant((String)laws.get("JUDGE"));
							setFree(W.criminal);
							if((judge!=null)
							&&(Sense.aliveAwakeMobile(judge,true)))
							{
								judge.location().show(judge,W.criminal,Affect.MSG_OK_VISUAL,"<S-NAME> put(s) <T-NAME> on parole!");
								Ability A=CMClass.getAbility("Prisoner");
								A.startTickDown(W.criminal,W.jailTime);
								W.criminal.recoverEnvStats();
								W.criminal.recoverCharStats();
								ExternalPlay.quickSay(judge,W.criminal,(String)laws.get("PAROLEDISMISS"),false,false);
								dismissOfficer(officer);
								W.setArrestingOfficer(null);
								W.criminal.tell("\n\r\n\r");
							}
							else
							{
								unCuff(W.criminal);
								if(W.arrestingOfficer!=null)
									dismissOfficer(W.arrestingOfficer);
								W.setArrestingOfficer(null);
								W.state=STATE_SEEKING;
							}
						}
						else
						{
							unCuff(W.criminal);
							W.setArrestingOfficer(null);
							W.state=STATE_SEEKING;
						}
					}
					break;
				case STATE_JAILING:
					{
						MOB officer=W.arrestingOfficer;
						if((officer!=null)
						&&(W.criminal.location().isInhabitant(officer))
						&&(Sense.aliveAwakeMobile(officer,true))
						&&(Sense.canBeSeenBy(W.criminal,officer)))
						{
							MOB judge=officer.location().fetchInhabitant((String)laws.get("JUDGE"));
							setFree(W.criminal);
							if((judge!=null)
							&&(Sense.aliveAwakeMobile(judge,true)))
							{
								Vector V=judge.location().getArea().getMyMap();
								Room jail=null;
								for(int v=0;v<V.size();v++)
								{
									Room R=(Room)V.elementAt(v);
									if(CoffeeUtensils.containsString(R.displayText(),(String)laws.get("JAIL")))
									{ jail=R; break; }
								}
								if(jail==null)
								for(int v=0;v<V.size();v++)
								{
									Room R=(Room)V.elementAt(v);
									if(CoffeeUtensils.containsString(R.description(),(String)laws.get("JAIL")))
									{ jail=R; break; }
								}
								if(jail!=null)
								{
									judge.location().show(judge,W.criminal,Affect.MSG_OK_VISUAL,"<S-NAME> banish(es) <T-NAME> to the jail!");
									jail.bringMobHere(W.criminal,false);
									if(W.criminal.location()==jail)
									{
										Ability A=CMClass.getAbility("Prisoner");
										A.startTickDown(W.criminal,W.jailTime);
										W.criminal.recoverEnvStats();
										W.criminal.recoverCharStats();
									}
									dismissOfficer(officer);
									W.setArrestingOfficer(null);
									W.criminal.tell("\n\r\n\r");
									ExternalPlay.look(W.criminal,null,true);
								}
								else
								{
									ExternalPlay.quickSay(judge,W.criminal,"But since there IS no jail, I will let you go.",false,false);
									dismissOfficer(officer);
									W.setArrestingOfficer(null);
								}
							}
							else
							{
								unCuff(W.criminal);
								if(W.arrestingOfficer!=null)
									dismissOfficer(W.arrestingOfficer);
								W.setArrestingOfficer(null);
								W.state=STATE_SEEKING;
							}
						}
						else
						{
							unCuff(W.criminal);
							W.setArrestingOfficer(null);
							W.state=STATE_SEEKING;
						}
					}
					break;
				case STATE_EXECUTING:
					{
						MOB officer=W.arrestingOfficer;
						if((officer!=null)
						&&(W.criminal.location().isInhabitant(officer))
						&&(Sense.aliveAwakeMobile(officer,true))
						&&(Sense.canBeSeenBy(W.criminal,officer)))
						{
							MOB judge=officer.location().fetchInhabitant((String)laws.get("JUDGE"));
							if((judge!=null)&&(Sense.aliveAwakeMobile(judge,true))&&(judge.location()==W.criminal.location()))
							{
								setFree(W.criminal);
								dismissOfficer(officer);
								Ability A=CMClass.getAbility("Prisoner");
								A.startTickDown(W.criminal,100);
								W.criminal.recoverEnvStats();
								W.criminal.recoverCharStats();
								ExternalPlay.postAttack(judge,W.criminal,judge.fetchWieldedItem());
								W.setArrestingOfficer(null);
							}
							else
							{
								unCuff(W.criminal);
								if(W.arrestingOfficer!=null)
									dismissOfficer(W.arrestingOfficer);
								W.setArrestingOfficer(null);
								W.state=STATE_SEEKING;
							}
						}
						else
						{
							unCuff(W.criminal);
							W.setArrestingOfficer(null);
							W.state=STATE_SEEKING;
						}
					}
					break;
				}
			}
		}
		
	}
}
