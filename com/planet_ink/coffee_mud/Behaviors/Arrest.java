package com.planet_ink.coffee_mud.Behaviors;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.PlayerData;
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary.TrackingFlags;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/*
   Copyright 2002-2024 Bo Zimmerman

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
public class Arrest extends StdBehavior implements LegalBehavior
{
	@Override
	public String ID()
	{
		return "Arrest";
	}

	@Override
	public long flags()
	{
		return Behavior.FLAG_LEGALBEHAVIOR;
	}

	@Override
	protected int canImproveCode()
	{
		return Behavior.CAN_AREAS;
	}

	protected String				lastAreaName		= null;
	protected boolean				loadAttempt			= false;
	protected Map<MOB, Double>		finesAssessed		= new Hashtable<MOB, Double>();
	protected volatile Room			lastBanishR			= null;
	protected Map<String, Object>	suppressedCrimes	= Collections.synchronizedMap(new TreeMap<String, Object>());
	protected Map<String,Boolean>	bannedItemCache		= new LimitedTreeMap<String,Boolean>(99999,1000,false);
	protected Set<String>			bannedMOBCheck		= new LimitedTreeSet<String>(60000,250,false);

	@Override
	public boolean isFullyControlled()
	{
		return true;
	}

	@Override
	public int addGetLoyaltyBonus(final int delta)
	{
		return 0;
	}

	protected String getLawParms()
	{
		final String parms=getParms();
		final int x=parms.indexOf(';');
		if(x>=0)
			return parms.substring(0, x).trim();
		return parms;
	}

	public final String getExtraLawString()
	{
		final String extraLawString=getParms();
		final int x=extraLawString.indexOf(';');
		if(x>=0)
		{
			return extraLawString.substring(x+1).trim();
		}
		return "";
	}

	public final Properties getExtraLawParms()
	{
		final String extraLawString=getExtraLawString();
		final Properties p=new Properties();
		if(extraLawString.length()>0)
			p.putAll(CMParms.parseEQParms(extraLawString));
		return p;
	}

	@Override
	public String accountForYourself()
	{
		return "legaliness";
	}

	public void debugLogLostConvicts(final String lead, final LegalWarrant W, final MOB officer)
	{
		final StringBuilder errLogMsg=new StringBuilder("("+lastAreaName+"): ");
		errLogMsg.append(!W.criminal().location().isInhabitant(officer)?L("AE1 "):"");
		errLogMsg.append(W.criminal().amDead()?L("AE2 "):"");
		errLogMsg.append(!CMLib.flags().isAliveAwakeMobile(W.criminal(),true)?L("AE3 "):"");
		errLogMsg.append(!CMLib.flags().isInTheGame(W.criminal(),true)?L("AE4 "):"");
		errLogMsg.append(W.crime().equalsIgnoreCase("pardoned")?L("AE5 "):"");
		errLogMsg.append(!((W.travelAttemptTime()==0)||((System.currentTimeMillis()-W.travelAttemptTime())<(5*60*1000)))?L("AE6 "):"");
		errLogMsg.append(!CMLib.flags().isAliveAwakeMobile(officer,true)?L("AE7 "):"");
		errLogMsg.append(!CMLib.flags().isBound(W.criminal())?L("AE8 "):"");
		if(CMSecurity.isDebugging(DbgFlag.ARREST))
			Log.debugOut("Arrest",lead+errLogMsg.toString());
	}

	@Override
	public boolean frame(final Area myArea, final MOB accused, final MOB framed)
	{
		if(!theLawIsEnabled())
			return false;
		final Law laws=getLaws(myArea,false);
		LegalWarrant W=null;
		if(laws!=null)
		{
			for(int i=0;(W=laws.getWarrant(accused,i))!=null;i++)
			{
				if(W.criminal()==accused)
				{
					W.setCriminal(framed);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean arrest(final Area myArea, final MOB officer, final MOB accused)
	{
		if(!theLawIsEnabled())
			return false;
		final Law laws=getLaws(myArea,false);
		final LegalWarrant W=(laws!=null)?laws.getWarrant(accused,0):null;
		if((W!=null)&&((W.arrestingOfficer()==null)||(W.arrestingOfficer().location()!=accused.location())))
		{
			W.setArrestingOfficer(myArea,officer);
			CMLib.commands().postSay(W.arrestingOfficer(),W.criminal(),
					L("You are under arrest for @x1! Sit down on the ground immediately!",restOfCharges(laws,W.criminal())),false,false);
			W.setState(Law.STATE_ARRESTING);
			return true;
		}
		return false;
	}

	@Override
	public int revoltChance()
	{
		return 0;
	}

	@Override
	public Law legalInfo(final Area myArea)
	{
		if(!theLawIsEnabled())
			return null;
		return getLaws(myArea,false);
	}

	@Override
	public boolean isEligibleOfficer(final Area myArea, final MOB mob)
	{
		if(!theLawIsEnabled())
			return false;
		final Law laws=getLaws(myArea,false);
		if((mob.isMonster())
		&&(mob.location()!=null)
		&&(laws!=null)
		&&(isEligibleOfficer(laws,mob,mob.location().getArea())))
			return true;
		return false;
	}

	@Override
	public boolean hasWarrant(final Area myArea, final MOB accused)
	{
		if(!theLawIsEnabled())
			return false;
		final Law laws=getLaws(myArea,false);
		return (laws!=null)?((laws.getWarrant(accused,0))!=null):false;
	}

	@Override
	public boolean isAnyOfficer(final Area myArea, final MOB mob)
	{
		if(!theLawIsEnabled())
			return false;
		final Law laws=getLaws(myArea,false);
		if((mob.isMonster())
		&&(mob.location()!=null)
		&&(laws!=null)
		&&(isAnyKindOfOfficer(laws,mob)))
			return true;
		return false;
	}

	@Override
	public boolean isJudge(final Area myArea, final MOB mob)
	{
		if(!theLawIsEnabled())
			return false;
		final Law laws=getLaws(myArea,false);
		if((mob.isMonster())
		&&(mob.location()!=null)
		&&(laws!=null)
		&&(isTheJudge(laws,mob)))
			return true;
		return false;
	}

	@Override
	public void modifyAssessedFines(final double d, final MOB mob)
	{
		final Double D=finesAssessed.get(mob);
		if(D!=null)
			finesAssessed.remove(mob);
		if(d>0)
			finesAssessed.put(mob,Double.valueOf(d));
	}

	@Override
	public double finesOwed(final MOB mob)
	{
		if(!theLawIsEnabled())
			return 0.0;
		final Double D=finesAssessed.get(mob);
		if(D!=null)
			return D.doubleValue();
		return 0.0;
	}

	@Override
	public boolean updateLaw(final Area myArea)
	{
		if(!theLawIsEnabled())
			return false;
		final Law laws=getLaws(myArea,false);
		if(laws!=null)
		{
			laws.resetLaw();
			if(getLawParms().equalsIgnoreCase("custom")
			&&(myArea!=null))
			{
				CMLib.database().DBReCreatePlayerData(myArea.Name(),"ARREST",myArea.Name()+"/ARREST",laws.rawLawString());
				return true;
			}
		}
		return false;
	}

	@Override
	public String rulingOrganization()
	{
		return "";
	}

	@Override
	public String conquestInfo(final Area myArea)
	{
		return "";
	}

	@Override
	public int controlPoints()
	{
		return 0;
	}

	@Override
	public void setControlPoints(final String clanID, final int newControlPoints)
	{
	}

	@Override
	public int getControlPoints(final String clanID)
	{
		return 0;
	}

	@Override
	public List<MOB> getCriminals(final Area myArea, final String searchStr)
	{
		final Vector<MOB> criminalsV=new Vector<MOB>();
		if(!theLawIsEnabled())
			return criminalsV;
		final Law laws=getLaws(myArea,false);
		final boolean debugging=CMSecurity.isDebugging(CMSecurity.DbgFlag.ARREST);
		for(final LegalWarrant W : laws.warrants())
		{
			if((isStillACrime(W,debugging))
			&&((searchStr==null)||(CMLib.english().containsString(W.criminal().name(),searchStr)))
			&&(!criminalsV.contains(W.criminal())))
				criminalsV.addElement(W.criminal());
		}
		return criminalsV;
	}

	@Override
	public List<LegalWarrant> getWarrantsOf(final Area myArea, final MOB accused)
	{
		final Vector<LegalWarrant> warrantsV=new Vector<LegalWarrant>();
		if(!theLawIsEnabled())
			return warrantsV;
		final Law laws=getLaws(myArea,false);
		final boolean debugging=CMSecurity.isDebugging(CMSecurity.DbgFlag.ARREST);
		for(final LegalWarrant W : laws.warrants())
		{
			if((isStillACrime(W,debugging))&&((accused==null)||(W.criminal()==accused)))
				warrantsV.addElement(W);
		}
		return warrantsV;
	}

	protected boolean isCrimeSuppressed(String crime)
	{
		if(crime == null)
			return false;
		crime = crime.toUpperCase().trim();
		synchronized(suppressedCrimes)
		{
			if(suppressedCrimes.size()>0)
			{
				Object obj = suppressedCrimes.get(crime);
				if((obj == null)
				&&(!crime.equals("ALL")))
				{
					for(final String key : suppressedCrimes.keySet())
					{
						if((crime.indexOf(key)>=0)&&(!key.equals("ALL")))
						{
							obj = suppressedCrimes.get(key);
							crime=key;
							break;
						}
					}
				}
				if(obj == null)
					return false;
				if(obj instanceof Long)
				{
					if(System.currentTimeMillis()<((Long)obj).longValue())
						return true;
				}
				else
				if(obj instanceof Ability)
				{
					if((((Ability)obj).affecting() != null)
					&&(((Ability)obj).canBeUninvoked()))
						return true;
				}
				else
				if(obj instanceof Quest)
				{
					if(((Quest)obj).running())
						return true;
				}
				suppressedCrimes.remove(crime);
			}
		}
		return false;
	}

	public boolean addWarrant(final Law laws, final LegalWarrant W)
	{
		if(!theLawIsEnabled())
			return false;
		if(isCrimeSuppressed(W.crime()) || isCrimeSuppressed("ALL"))
			return false;
		if((laws!=null)&&(!laws.warrants().contains(W)))
		{
			final Room R=CMLib.map().roomLocation(W.criminal());
			if(R!=null)
			{
				MOB accuser=W.witness();
				if(accuser==null)
					accuser=W.victim();
				if(accuser==null)
					accuser=W.criminal();
				final CMMsg msg=CMClass.getMsg(accuser, W.criminal(), W.victim(),
						CMMsg.MASK_ALWAYS|CMMsg.MSG_LEGALWARRANT, CMMsg.MSG_LEGALWARRANT, CMMsg.MSG_LEGALWARRANT, W.crime());
				if(R.okMessage(W.criminal(),msg))
				{
					R.send(W.criminal(), msg);
					CMLib.map().sendGlobalMessage(accuser, CMMsg.TYP_LEGALWARRANT, msg);
				}
				else
					return false;
			}
			if(W.criminal().isPlayer())
				CMLib.coffeeTables().bump(W.criminal(), CoffeeTableRow.STAT_WARRANTS);
			laws.warrants().add(W);
			if(W.criminal()!=null)
			{
				final List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.WARRANTS, W.criminal());
				for(int i=0;i<channels.size();i++)
					CMLib.commands().postChannel(channels.get(i),null,L("@x1 has been accused of @x2.",W.criminal().name(),fixCharge(W)),true);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean addWarrant(final Area myArea, final LegalWarrant W)
	{
		if(!theLawIsEnabled())
			return false;
		final Law laws=getLaws(myArea,false);
		return addWarrant(laws,W);
	}

	@Override
	public boolean addWarrant(final Area myArea, final MOB accused, final MOB victim, final String crimeLocs, final String crimeFlags, final String crime, final String sentence, final String warnMsg)
	{
		if(!theLawIsEnabled())
			return false;
		final Law laws=getLaws(myArea,false);
		if(laws!=null)
			return fillOutWarrant(accused,laws,myArea,victim,crimeLocs,crimeFlags,crime,sentence,warnMsg);
		return false;
	}

	@Override
	public boolean deleteWarrant(final Area myArea, final LegalWarrant W)
	{
		if(!theLawIsEnabled())
			return false;
		final Law laws=getLaws(myArea,false);
		if((laws!=null)&&(laws.warrants().contains(W)))
		{
			laws.warrants().remove(W);
			return true;
		}
		return false;
	}

	@Override
	public boolean aquit(final Area myArea, final MOB accused, final String[] acquittableLaws)
	{
		if(!theLawIsEnabled())
			return false;
		final Law laws=getLaws(myArea,false);
		if(laws!=null)
		{
			final boolean debugging=CMSecurity.isDebugging(CMSecurity.DbgFlag.ARREST);
			String[] info=null;
			if(acquittableLaws!=null)
			{
				for (final String brokenLaw : acquittableLaws)
				{
					if((laws.basicCrimes().containsKey(brokenLaw))&&(laws.basicCrimes().get(brokenLaw) !=null))
					{
						info=laws.basicCrimes().get(brokenLaw);
						break;
					}
					else
					if((laws.taxLaws().containsKey(brokenLaw))&&(laws.taxLaws().get(brokenLaw) instanceof String[]))
					{
						info=(String[])laws.taxLaws().get(brokenLaw);
						break;
					}
					else
					if((laws.abilityCrimes().containsKey(brokenLaw))&&(laws.abilityCrimes().get(brokenLaw) !=null))
					{
						info=laws.abilityCrimes().get(brokenLaw);
						break;
					}
				}
				if(info==null)
					return false;
			}
			boolean somethingDone=false;
			for(final LegalWarrant W : laws.warrants())
			{
				if((isStillACrime(W,debugging))
				&&(W.criminal()==accused)
				&&((info==null)||(W.crime().equalsIgnoreCase(info[Law.BIT_CRIMENAME]))))
				{
					laws.warrants().remove(W);
					somethingDone=true;
				}
			}
			return somethingDone;
		}
		return false;
	}

	protected boolean isJailRoom(final Area myArea, final Room room)
	{
		if(!theLawIsEnabled())
			return false;
		final Law laws=getLaws(myArea,false);
		if((laws!=null)&&(room!=null))
			return getRooms(myArea,laws.jailRooms()).contains(room);
		return false;
	}

	@Override
	public boolean isJailRoom(final Area myArea, final List<Room> jails)
	{
		if(!theLawIsEnabled())
			return false;
		final Law laws=getLaws(myArea,false);
		if(laws!=null)
		{
			final List<Room> rooms=getRooms(myArea,laws.jailRooms());
			boolean answer=false;
			for(int i=0;i<jails.size();i++)
				answer=answer||rooms.contains(jails.get(i));
			return answer;
		}
		return false;
	}

	@Override
	public boolean accuse(final Area myArea, final MOB accused, final MOB victim, final String[] accusableLaws)
	{
		if(!theLawIsEnabled())
			return false;
		final Law laws=getLaws(myArea,false);
		if(laws!=null)
		{
			for (final String brokenLaw : accusableLaws)
			{
				String[] info=null;
				if((laws.basicCrimes().containsKey(brokenLaw))&&(laws.basicCrimes().get(brokenLaw) !=null))
					info=laws.basicCrimes().get(brokenLaw);
				else
				if((laws.taxLaws().containsKey(brokenLaw))&&(laws.taxLaws().get(brokenLaw) instanceof String[]))
					info=(String[])laws.taxLaws().get(brokenLaw);
				else
				if((laws.abilityCrimes().containsKey(brokenLaw))&&(laws.abilityCrimes().get(brokenLaw) !=null))
					info=laws.abilityCrimes().get(brokenLaw);
				if(info!=null)
				{
					if((info[Law.BIT_CRIMENAME]!=null)
					&&(info[Law.BIT_CRIMENAME].length()>0))
					{
						final boolean kaplah=
							fillOutWarrant(accused,
									laws,
									myArea,
									(victim==accused)?null:victim,
									info[Law.BIT_CRIMELOCS],
									info[Law.BIT_CRIMEFLAGS],
									info[Law.BIT_CRIMENAME],
									info[Law.BIT_SENTENCE],
									info[Law.BIT_WARNMSG]);
						if(kaplah)
							return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public void setParms(final String newParms)
	{
		super.setParms(newParms);
		loadAttempt=false;
	}

	protected boolean defaultModifiableNames()
	{
		return true;
	}

	@Override
	public void suppressLaws(String crime, final Object until)
	{
		if(crime == null)
			return;
		crime = crime.toUpperCase().trim();
		synchronized(suppressedCrimes)
		{
			if((this.lastAreaName != null)
			&&(!crime.equals("ALL")))
			{
				final Area A = CMLib.map().getArea(this.lastAreaName);
				final Law laws = this.getLaws(A, true);
				if(laws != null)
				{
					final String[] crimeSet = laws.basicCrimes().get(crime);
					if(crimeSet != null)
					{
						crime = crimeSet[Law.BIT_CRIMENAME].toUpperCase().trim();
					}
				}
			}
			if(until == null)
			{
				if(crime.equals("ALL"))
					suppressedCrimes.clear();
				else
					suppressedCrimes.remove(crime);
			}
			else
				suppressedCrimes.put(crime, until);
		}
	}

	@Override
	public List<String> externalFiles()
	{
		String lawName=getLawParms();
		if(lawName.length()==0)
			lawName="laws.ini";
		if(lawName.equalsIgnoreCase("custom"))
			return super.externalFiles();
		if(lawName.equalsIgnoreCase("laws.ini"))
			return super.externalFiles();
		if(new CMFile(Resources.makeFileResourceName(lawName),null).exists())
			return new XVector<String>(lawName);
		return super.externalFiles();
	}

	public final String getResourceKey(final String lawName)
	{
		return "LEGAL-"+lawName+Long.toString(getExtraLawString().hashCode());
	}

	protected Law getLaws(final Environmental what, final boolean cleanOnly)
	{
		String lawName=getLawParms();

		boolean modifiableLaw=false;
		boolean modifiableNames=defaultModifiableNames();

		Law laws=null;
		if((lawName.equalsIgnoreCase("custom"))&&(what!=null))
		{
			modifiableLaw=true;
			laws=(Law)Resources.getResource(getResourceKey(what.Name()));
		}
		else
		{
			if(lawName.length()==0)
				lawName="laws.ini";
			laws=(Law)Resources.getResource(getResourceKey(lawName));
			modifiableNames=false;
		}

		if((laws==null)&&(cleanOnly))
			return null;

		if(laws==null)
		{
			final Properties lawprops=new Properties();
			try
			{
				if((lawName.equalsIgnoreCase("custom"))&&(what!=null))
				{
					final List<PlayerData> data=CMLib.database().DBReadPlayerData(what.Name(),"ARREST",what.Name()+"/ARREST");
					if((data!=null)&&(data.size()>0))
					{
						final DatabaseEngine.PlayerData pdata=data.get(0);
						String s=CMStrings.replaceAll(pdata.xml(),"~","\n");
						s=CMStrings.replaceAll(s,"`","'");
						lawprops.load(new ByteArrayInputStream(CMStrings.strToBytes(s)));
					}
					else
					{
						String s=Law.defaultLaw;
						lawprops.load(new ByteArrayInputStream(CMStrings.strToBytes(s)));
						s=CMStrings.replaceAll(s,"\n","~");
						s=CMStrings.replaceAll(s,"\r","~");
						s=CMStrings.replaceAll(s,"'","`");
						CMLib.database().DBCreatePlayerData(what.Name(),"ARREST",what.Name()+"/ARREST",s);
					}
				}
				if(lawprops.isEmpty())
				{
					for(final CMFile F : CMFile.getExistingExtendedFiles(Resources.makeFileResourceName(lawName),null, CMFile.FLAG_FORCEALLOW))
					{
						final Properties props=new Properties();
						props.load(new ByteArrayInputStream(F.raw()));
						lawprops.putAll(props);
					}
				}
			}
			catch(final IOException e)
			{
				if(!loadAttempt)
				{
					Log.errOut("Arrest","Unable to load: "+lawName+", legal system inoperable.");
					loadAttempt=true;
				}
				return (Law)CMClass.getCommon("DefaultLawSet");
			}
			lawprops.putAll(getExtraLawParms());
			loadAttempt=true;
			laws=(Law)CMClass.getCommon("DefaultLawSet");
			laws.initialize(this,lawprops,modifiableNames,modifiableLaw);
			if(lawName.equalsIgnoreCase("custom")&&(what!=null))
				Resources.submitResource(getResourceKey(what.name()),laws);
			else
				Resources.submitResource(getResourceKey(lawName),laws);
		}
		return laws;
	}

	public void unCuff(final MOB mob)
	{
		final Ability A=mob.fetchEffect("Skill_HandCuff");
		if(A!=null)
			A.unInvoke();
	}

	public void dismissOfficer(final MOB officer)
	{
		if(officer==null)
			return;
		if((officer.getStartRoom()!=null)
		&&(officer.location()!=null)
		&&(officer.getStartRoom()==officer.location()))
			return;
		if(officer.isMonster())
			CMLib.tracking().wanderAway(officer,false,true);
	}

	public MOB getAWitnessHere(final Area myArea, final Room R, final MOB accused)
	{
		if(R!=null)
		for(int i=0;i<R.numInhabitants();i++)
		{
			final MOB M=R.fetchInhabitant(i);
			if((M!=null)
			&&M.isMonster()
			&&(M!=accused)
			&&(M.charStats().getStat(CharStats.STAT_INTELLIGENCE)>3)
			&&(CMLib.dice().rollPercentage()>=(CMLib.flags().isChaotic(accused)?25:(CMLib.flags().isLawful(accused)?95:50))
				||(isAnyOfficer(myArea, M))))
				return M;
		}
		return null;
	}

	public MOB getWitness(final Area A, final MOB accused)
	{
		final Room R=accused.location();

		if((A!=null)&&(!A.inMyMetroArea(R.getArea())))
			return null;
		MOB M=getAWitnessHere(A,R,accused);
		if(M!=null)
			return M;

		if(R!=null)
		{
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			{
				final Room R2=R.getRoomInDir(d);
				M=getAWitnessHere(A,R2,accused);
				if(M!=null)
					return M;
			}
		}
		return null;
	}

	public boolean isAnyKindOfOfficer(final Law laws, final MOB M)
	{
		if((laws.officerNames().size()<=0)
		||(laws.officerNames().get(0).equals("@")))
			return false;
		if((M.isMonster())
		&&(M.location()!=null)
		&&(CMLib.flags().isMobile(M))
		&&(!M.phyStats().isAmbiance(PhyStats.Ambiance.SUPPRESS_OFFICIALDOM)))
		{
			if(M.phyStats().isAmbiance(PhyStats.Ambiance.IS_OFFICER))
				return true;
			for(int i=0;i<laws.officerNames().size();i++)
			{
				if((CMLib.english().containsString(M.displayText(),laws.officerNames().get(i))
				||(CMLib.english().containsString(M.Name(),laws.officerNames().get(i)))))
					return true;
			}
		}
		return false;
	}

	public boolean isEligibleOfficer(final Law laws, final MOB M, final Area myArea)
	{
		if((M!=null)&&(M.isMonster())&&(M.location()!=null))
		{
			if((myArea!=null)&&(!myArea.inMyMetroArea(M.location().getArea())))
				return false;

			if(isAnyKindOfOfficer(laws,M)
			&&(!isBusyWithJustice(laws,M))
			&&(CMLib.flags().isAliveAwakeMobile(M,true))
			&&(!M.isInCombat()))
				return true;
		}
		return false;
	}

	public MOB getEligibleOfficerHere(final Law laws,
									  final Area myArea,
									  final Room R,
									  final MOB criminal,
									  final MOB victim)
	{
		if(R==null)
			return null;
		for(int i=0;i<R.numInhabitants();i++)
		{
			final MOB M=R.fetchInhabitant(i);
			if((M!=null)
			&&(M!=criminal)
			&&(M.location()!=null)
			&&(myArea.inMyMetroArea(M.location().getArea()))
			&&((victim==null)||(M!=victim))
			&&(isEligibleOfficer(laws,M,myArea))
			&&(CMLib.flags().canBeSeenBy(criminal,M)))
				return M;
		}
		return null;
	}

	public MOB getAnyEligibleOfficer(final Law laws,
									 final Area myArea,
									 final MOB criminal,
									 final MOB victim)
	{
		final Room R=criminal.location();
		if(R==null)
			return null;
		if((myArea!=null)&&(!myArea.inMyMetroArea(R.getArea())))
			return null;
		MOB M=getEligibleOfficerHere(laws,myArea,R,criminal,victim);
		if((M==null)&&(myArea!=null))
		{
			for(final Enumeration<Room> e=myArea.getMetroMap();e.hasMoreElements();)
			{
				final Room R2=e.nextElement();
				M=getEligibleOfficerHere(laws,myArea,R2,criminal,victim);
				if(M!=null)
					break;
			}
		}
		return M;
	}

	public MOB getEligibleOfficer(final Law laws,
								  final Area myArea,
								  final MOB criminal,
								  final MOB victim)
	{
		final Room R=criminal.location();
		if((R==null)||(!R.isInhabitant(criminal)))
			return null;
		if((myArea!=null)&&(!myArea.inMyMetroArea(R.getArea())))
			return null;
		MOB M=getEligibleOfficerHere(laws,myArea,R,criminal,victim);
		if(M!=null)
			return M;
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			final Room R2=R.getRoomInDir(d);
			if(R2!=null)
			{
				M=getEligibleOfficerHere(laws,myArea,R2,criminal,victim);
				if(M!=null)
				{
					final int direction=R.getReverseDir(d);
					CMLib.tracking().walk(M,direction,false,false);
					if(M.location()==R)
						return M;
				}
			}
		}
		return null;
	}

	public boolean canFocusOn(final MOB officer, final MOB criminal)
	{
		final CMMsg msg=CMClass.getMsg(officer,criminal,CMMsg.MSG_LOOK,L("<S-NAME> look(s) closely at <T-NAME>."));
		if((officer!=null)&&(officer.location()!=null)&&(criminal.location()==officer.location()))
		{
			if(!officer.location().okMessage(officer,msg))
				return false;
			if(msg.sourceMessage().indexOf("<T-NAME>")<0)
				return false;
			if((criminal.name().toUpperCase().equals(criminal.Name().toUpperCase()))
			||(criminal.name().toUpperCase().startsWith(criminal.Name().toUpperCase()+" "))
			||(criminal.name().toUpperCase().endsWith(" "+criminal.Name().toUpperCase())))
				return true;
		}
		return true;
	}

	@Override
	public boolean isStillACrime(final LegalWarrant W, final boolean debugging)
	{
		// will witness talk, or victim press charges?
		final Set<MOB> H=W.criminal().getGroupMembers(new HashSet<MOB>());
		if((W.witness()!=null)&&W.witness().amDead())
		{
			if(debugging)
				Log.debugOut("ARREST", "("+lastAreaName+"): "+W.crime()+" Witness ("+W.witness().Name()+") is DEAD!");
			return false;
		}
		if(W.arrestingOfficer()!=null)
		{
			if(W.witness()==W.arrestingOfficer())
				return true;
			if((W.victim()!=null)&&(W.victim()==W.arrestingOfficer()))
				return true;
		}

		if((W.witness()!=null)&&H.contains(W.witness()))
		{
			if(debugging)
				Log.debugOut("ARREST", "("+lastAreaName+"): "+W.crime()+" Witness ("+W.witness().Name()+") is a friend of the accused!");
			return false;
		}
		if((W.victim()!=null)&&(H.contains(W.victim())))
		{
			if(debugging)
				Log.debugOut("ARREST", "("+lastAreaName+"): "+W.crime()+" Victim ("+W.victim().Name()+") is a friend of the accused!");
			return false;
		}
		// crimes expire after three real days
		if((W.lastOffense()>0)&&((System.currentTimeMillis()-W.lastOffense())>EXPIRATION_MILLIS))
		{
			if(debugging)
				Log.debugOut("ARREST","("+lastAreaName+"): "+W.crime()+" Crime has expired: "+W.lastOffense());
			return false;
		}
		return true;
	}

	public List<LegalWarrant> getRelevantWarrants(final List<LegalWarrant> warrants, final LegalWarrant W, final MOB criminal)
	{
		final List<LegalWarrant> warrantsV=new Vector<LegalWarrant>();
		if(W!=null)
			warrantsV.add(W);
		for(final LegalWarrant W2 : warrants)
		{
			if((W2.criminal()==criminal)
			&&(W2!=W)
			&&((W==null)
				||(W2.crime()==null)
				||(!CMath.bset(W.punishmentCode(),Law.PUNISHMENTMASK_SEPARATE))
				||(W2.crime().equalsIgnoreCase(W.crime()))))
					warrantsV.add(W2);
		}
		return warrantsV;
	}

	public double getFine(final Law laws, final LegalWarrant W, final MOB criminal)
	{
		String s=null;
		if(CMath.bset(W.punishmentCode(),Law.PUNISHMENTMASK_SEPARATE))
		{
			s=W.getPunishmentParm(Law.PUNISHMENTMASK_FINE);
			if((s==null)||(s.length()==0)||(!CMath.isNumber(s)))
				return 0;
			return CMath.s_double(s);
		}
		double fine=0.0;
		final List<LegalWarrant> V=getRelevantWarrants(laws.warrants(),W,criminal);
		for(int w2=0;w2<V.size();w2++)
		{
			final LegalWarrant W2=V.get(w2);
			if(!CMath.bset(W2.punishmentCode(),Law.PUNISHMENTMASK_SEPARATE))
			{
				s=W.getPunishmentParm(Law.PUNISHMENTMASK_FINE);
				if((s!=null)&&(s.length()>0)&&(CMath.isNumber(s)))
					fine+=CMath.s_double(s);
			}
		}
		return fine;
	}

	public int getBanishmentTicks(final Law laws, final LegalWarrant W, final MOB criminal)
	{
		final TimeClock C=CMLib.time().localClock(criminal);
		String s=null;
		int days=0;
		if(CMath.bset(W.punishmentCode(),Law.PUNISHMENTMASK_SEPARATE))
		{
			s=W.getPunishmentParm(Law.PUNISHMENTMASK_BANISH);
			if((s != null)&&(s.indexOf(',')>0))
				s=s.substring(0, s.indexOf(',')).trim();
			if((s==null)||(s.length()==0)||(!CMath.isNumber(s)))
				return 0;
			days=CMath.s_int(s);
		}
		else
		{
			final List<LegalWarrant> V=getRelevantWarrants(laws.warrants(),W,criminal);
			for(int w2=0;w2<V.size();w2++)
			{
				final LegalWarrant W2=V.get(w2);
				if(!CMath.bset(W2.punishmentCode(),Law.PUNISHMENTMASK_SEPARATE))
				{
					s=W.getPunishmentParm(Law.PUNISHMENTMASK_BANISH);
					if((s != null)&&(s.indexOf(',')>0))
						s=s.substring(0, s.indexOf(',')).trim();
					if((s!=null)&&(s.length()>0)&&(CMath.isNumber(s)))
						days += CMath.s_int(s);
				}
			}
		}
		if(days >= 255)
			return Integer.MAX_VALUE;
		else
			return (int)(days * CMProps.getTicksPerMudHour() * C.getHoursInDay());
	}

	public int getShameTicks(final Law laws, final LegalWarrant W, final MOB criminal)
	{
		final TimeClock C=CMLib.time().localClock(criminal);
		String s=null;
		int days=0;
		if(CMath.bset(W.punishmentCode(),Law.PUNISHMENTMASK_SEPARATE))
		{
			s=W.getPunishmentParm(Law.PUNISHMENTMASK_SHAME);
			if((s==null)||(s.length()==0)||(!CMath.isNumber(s)))
				return 0;
			days=CMath.s_int(s);
		}
		else
		{
			final List<LegalWarrant> V=getRelevantWarrants(laws.warrants(),W,criminal);
			for(int w2=0;w2<V.size();w2++)
			{
				final LegalWarrant W2=V.get(w2);
				if(!CMath.bset(W2.punishmentCode(),Law.PUNISHMENTMASK_SEPARATE))
				{
					s=W.getPunishmentParm(Law.PUNISHMENTMASK_SHAME);
					if((s!=null)&&(s.length()>0)&&(CMath.isNumber(s)))
						days += CMath.s_int(s);
				}
			}
		}
		if(days >= 255)
			return Integer.MAX_VALUE;
		else
			return (int)(days * CMProps.getTicksPerMudHour() * C.getHoursInDay());
	}

	protected String getDetainParm(final Law laws, final LegalWarrant W, final MOB criminal)
	{
		String s=null;
		if(CMath.bset(W.punishmentCode(),Law.PUNISHMENTMASK_SEPARATE))
		{
			s=W.getPunishmentParm(Law.PUNISHMENTMASK_DETAIN);
			if((s==null)||(s.length()==0))
				return "";
		}
		s=W.getPunishmentParm(Law.PUNISHMENTMASK_DETAIN);
		if((s==null)||(s.length()==0))
		{
			final List<LegalWarrant> V=getRelevantWarrants(laws.warrants(),W,criminal);
			for(int w2=0;w2<V.size();w2++)
			{
				final LegalWarrant W2=V.get(w2);
				if(!CMath.bset(W2.punishmentCode(),Law.PUNISHMENTMASK_SEPARATE))
				{
					s=W.getPunishmentParm(Law.PUNISHMENTMASK_DETAIN);
					if((s!=null)&&(s.length()>0))
						break;
				}
			}
		}
		if(s!=null)
		{
			return s;
		}
		return "";
	}

	protected String getBanishRoom(final Law laws, final LegalWarrant W, final MOB criminal)
	{
		final String s=W.getPunishmentParm(Law.PUNISHMENTMASK_BANISH);
		if((s==null)||(s.length()==0))
			return "";

		final int x=s.indexOf(',');
		if(x<0)
			return "";
		return s.substring(x+1).trim();
	}

	protected String getDetainRoom(final Law laws, final LegalWarrant W, final MOB criminal)
	{
		final String s=getDetainParm(laws,W,criminal);
		if((s==null)||(s.length()==0))
			return "";
		final int x=s.indexOf(',');
		if((x<0)||(!CMath.isInteger(s.substring(x+1))))
			return s;
		return s.substring(0,x);
	}

	protected int getDetainTime(final Law laws, final LegalWarrant W, final MOB criminal)
	{
		final String s=getDetainParm(laws,W,criminal);
		if((s==null)||(s.length()==0))
			return -1;
		final int x=s.indexOf(',');
		if((x<0)||(!CMath.isInteger(s.substring(x+1))))
			return laws.jailTimes()[0].intValue();
		return CMath.s_int(s.substring(x+1));
	}

	public int highestCrimeAction(final Law laws, final LegalWarrant W, final MOB criminal)
	{
		int highest=W.punishmentCode();
		if(CMath.bset(W.punishmentCode(),Law.PUNISHMENTMASK_SEPARATE))
			return W.punishmentCode();
		final List<LegalWarrant> V=getRelevantWarrants(laws.warrants(),W,criminal);
		for(int w2=0;w2<V.size();w2++)
		{
			final LegalWarrant W2=V.get(w2);
			if(!CMath.bset(W2.punishmentCode(),Law.PUNISHMENTMASK_SEPARATE))
			{
				if(((W2.punishmentCode()&Law.PUNISHMENT_MASK)+W2.offenses())>(highest&Law.PUNISHMENT_MASK))
					highest=(W2.punishmentCode()&Law.PUNISHMENT_MASK)+((W2.offenses()<4)?W2.offenses():3);
			}
		}
		for(int w2=0;w2<V.size();w2++)
		{
			final LegalWarrant W2=V.get(w2);
			if((!CMath.bset(W2.punishmentCode(),Law.PUNISHMENTMASK_SEPARATE))
			&&(highest<((W2.punishmentCode()&Law.PUNISHMENT_MASK)+4)))
				highest++;
		}
		if((highest&Law.PUNISHMENT_MASK)>Law.PUNISHMENT_HIGHEST)
			highest=Law.PUNISHMENT_HIGHEST | (highest - (highest&Law.PUNISHMENT_MASK));
		String cap = W.getPunishmentParm(Law.PUNISHMENTMASK_PUNISHCAP);
		if((cap != null)&&(cap.trim().length()>0))
		{
			cap=cap.trim();
			if(CMath.isInteger(cap))
			{
				if(highest>CMath.s_int(cap))
					highest=CMath.s_int(cap);
			}
			else
			{
				final int x=CMParms.indexOf(Law.PUNISHMENT_DESCS,cap.toUpperCase());
				if(highest>x)
					highest=x;
			}
		}

		int adjusted=highest;
		if((CMLib.flags().isGood(criminal))&&(adjusted>0))
			adjusted--;
		return adjusted;
	}

	public boolean isBusyWithJustice(final Law laws, final MOB M)
	{
		for(final LegalWarrant W : laws.warrants())
		{
			if(W.arrestingOfficer()!=null)
			{
				if(W.criminal()==M)
					return true;
				else
				if(W.arrestingOfficer()==M)
					return true;
			}
		}
		return false;
	}

	public String fixCharge(final LegalWarrant W)
	{
		if(W==null)
			return "";
		final String charge=W.crime();
		if(W.victim()==null)
			return charge;
		if(charge.indexOf("<T-NAME>")<0)
			return charge;
		return charge.replaceFirst("<T-NAME>",W.victim().name());
	}

	public String restOfCharges(final Law laws, final MOB mob)
	{
		final StringBuffer msg=new StringBuffer("");
		for(int w=0;(laws.getWarrant(mob,w)!=null);w++)
		{
			final LegalWarrant W=laws.getWarrant(mob,w);
			if(W!=null)
			{
				if(w==0)
					msg.append(L("@x1",fixCharge(W)));
				else
				if(laws.getWarrant(mob,w+1)==null)
					msg.append(L(", and for @x1",fixCharge(W)));
				else
					msg.append(L(", for @x1",fixCharge(W)));
			}
		}
		return msg.toString();
	}

	public void makePeace(final Room R)
	{
		if(R==null)
			return;
		for(int i=0;i<R.numInhabitants();i++)
		{
			final MOB inhab=R.fetchInhabitant(i);
			if((inhab!=null)&&(inhab.isInCombat()))
				inhab.makePeace(true);
		}
	}

	public boolean isTheJudge(final Law laws, final MOB M)
	{
		if((laws.judgeNames().size()<=0)
		||(laws.judgeNames().get(0).equals("@")))
			return false;
		if((M!=null)
		&&((M.isMonster()||M.soulMate()!=null))
		&&(!CMLib.flags().isMobile(M))
		&&(M.location()!=null)
		&&(!M.phyStats().isAmbiance(PhyStats.Ambiance.SUPPRESS_OFFICIALDOM)))
		{
			if(M.phyStats().isAmbiance(PhyStats.Ambiance.IS_JUDGE))
				return true;
			for(int i=0;i<laws.judgeNames().size();i++)
			{
				if((CMLib.english().containsString(M.displayText(),laws.judgeNames().get(i)))
				||(CMLib.english().containsString(M.Name(),laws.judgeNames().get(i))))
					return true;
			}
		}
		return false;
	}

	public MOB getTheJudgeHere(final Law laws, final Room R)
	{
		for(int i=0;i<R.numInhabitants();i++)
		{
			final MOB M=R.fetchInhabitant(i);
			if(isTheJudge(laws,M))
				return M;
		}
		return null;
	}

	public Room findTheJudge(final Law laws, final Area myArea)
	{
		for(final Enumeration<Room> r=myArea.getMetroMap();r.hasMoreElements();)
		{
			final Room R=r.nextElement();
			for(int i=0;i<R.numInhabitants();i++)
			{
				final MOB M=R.fetchInhabitant(i);
				if(isTheJudge(laws,M))
					return R;
			}
		}
		return null;
	}

	protected String trackingModifiers(final MOB officer)
	{
		final StringBuilder modifiers=new StringBuilder("");
		if(officer!=null)
		{
			if((!CMLib.flags().isFlying(officer))&&(!CMLib.flags().isSwimming(officer)))
				modifiers.append(" LANDONLY");
			else
			{
				if(!CMLib.flags().isFlying(officer))
					modifiers.append(" NOAIR");
				if(!CMLib.flags().isSwimming(officer))
					modifiers.append(" NOWATER");
			}
		}
		return modifiers.toString();
	}

	public boolean startTracking(final MOB officer, final Room R)
	{
		CMLib.tracking().stopTracking(officer);
		if(R!=null)
		{
			final Ability A=CMClass.getAbility("Skill_Track");
			if(A!=null)
			{
				A.invoke(officer,CMParms.parse("\""+CMLib.map().getExtendedRoomID(R)+"\" "+trackingModifiers(officer)),R,true,0);
				return officer.fetchEffect("Skill_Track") !=null;
			}
		}
		return false;
	}

	public Room getReleaseRoom(final Law laws, final Area myArea, final MOB criminal, final LegalWarrant W)
	{
		Room room=null;
		if((criminal.isMonster())&&(criminal.getStartRoom()!=null))
			room=criminal.getStartRoom();
		else
		{
			if((laws.releaseRooms().size()==0)||(laws.releaseRooms().get(0).equals("@")))
				return myArea.getMetroMap().nextElement();
			if(criminal.location()!=null)
				room=getRoom(criminal.location().getArea(),laws.releaseRooms());
			if(room==null)
				room=getRoom(myArea,laws.releaseRooms());
			if(room==null)
				room=findTheJudge(laws,myArea);
			if(room==null)
				room=myArea.getMetroMap().nextElement();
		}
		return room;
	}

	public boolean isTroubleMaker(final MOB M)
	{
		if(M==null)
			return false;
		for(final Enumeration<Behavior> e=M.behaviors();e.hasMoreElements();)
		{
			final Behavior B=e.nextElement();
			if((B!=null)&&(CMath.bset(B.flags(),Behavior.FLAG_TROUBLEMAKING)))
				return true;
		}
		return (M.fetchEffect("QuestBound")!=null); // questing mobs are, by default, trouble makers
	}

	public List<Room> getRooms(final Area A, final List<String> V)
	{
		final Vector<Room> finalV=new Vector<Room>();
		Room jail=null;
		if(V.size()==0)
			return finalV;
		for(int v=0;v<V.size();v++)
		{
			final String which=V.get(v);
			jail=getRoom(A,which);
			if((jail!=null)
			&&(!finalV.contains(jail)))
				finalV.addElement(jail);
		}
		return finalV;
	}

	public Room getRoom(final Area A, final String which)
	{
		Room jail=null;
		jail=CMLib.map().getRoom(which);
		if(jail==null)
		{
			for(final Enumeration<Room> r=A.getMetroMap();r.hasMoreElements();)
			{
				final Room R=r.nextElement();
				if(CMLib.english().containsString(R.displayText(),which))
				{
					jail = R;
					break;
				}
			}
		}
		if(jail==null)
		{
			for(final Enumeration<Room> r=A.getMetroMap();r.hasMoreElements();)
			{
				final Room R=r.nextElement();
				if(CMLib.english().containsString(R.description(),which))
				{
					jail = R;
					break;
				}
			}
		}
		return jail;
	}

	public Room getRoom(final Area A, final List<String> V)
	{
		if(V.size()==0)
			return null;
		final String which=V.get(CMLib.dice().roll(1,V.size(),-1));
		return getRoom(A,which);
	}

	public void fileAllWarrants(final Law laws, final LegalWarrant W1, final MOB mob)
	{
		final List<LegalWarrant> V=new ArrayList<LegalWarrant>();
		{
			LegalWarrant W=null;
			if((W1!=null)&&(CMath.bset(W1.punishmentCode(),Law.PUNISHMENTMASK_SEPARATE)))
			{
				for(int i=0;(W=laws.getWarrant(mob,i))!=null;i++)
				{
					if((W.criminal()==mob)&&(W1.crime().equalsIgnoreCase(W.crime())))
						V.add(W);
				}
			}
			else
			{
				for(int i=0;(W=laws.getWarrant(mob,i))!=null;i++)
				{
					if(W.criminal()==mob)
						V.add(W);
				}
			}
		}
		for(final LegalWarrant W : V)
		{
			laws.warrants().remove(W);
			if(W.crime()!=null)
			{
				boolean found=false;
				for(final LegalWarrant oW : laws.oldWarrants())
				{
					if((oW.criminal()==mob)
					&&(oW.crime()!=null)
					&&(oW.crime().equals(W.crime())))
						found=true;
				}
				if(!found)
				{
					W.setOffenses(W.offenses()+1);
					laws.oldWarrants().add(W);
				}
			}
		}
	}

	public Room findTheJail(final MOB mob, final Area myArea, final Law laws)
	{
		Room jail=null;
		if((laws.jailRooms().size()==0)||(laws.jailRooms().get(0).equals("@")))
			return null;
		jail=getRoom(mob.location().getArea(),laws.jailRooms());
		if(jail==null)
			jail=getRoom(myArea,laws.jailRooms());
		return jail;
	}

	public Room findTheDetentionCenter(final MOB mob, final Area myArea, final Law laws, final LegalWarrant W)
	{
		final String detentionCenter=getDetainRoom(laws,W,W.criminal());
		if(detentionCenter.length()==0)
			return null;
		Room detainer=getRoom(mob.location().getArea(),detentionCenter);
		if(detainer==null)
			detainer=getRoom(myArea,detentionCenter);
		return detainer;
	}

	public Room findTheBanishingPoint(final MOB mob, final Area myArea, final Law laws, final LegalWarrant W)
	{
		final String banishingPoint=getDetainRoom(laws,W,W.criminal());
		Room banishRoom = null;
		if(banishingPoint.length()>0)
		{
			banishRoom=getRoom(mob.location().getArea(),banishingPoint);
			if(banishRoom==null)
				banishRoom=getRoom(myArea,banishingPoint);
		}
		final Room mobR=mob.location();
		if((banishRoom == null)
		&&(mobR!=null))
		{
			// this algorithm is SUPER expensive, so let's cache it.
			if(lastBanishR != null)
				return lastBanishR;
			int range = myArea.metroSize() * 5;
			TrackingLibrary.TrackingFlags flags;
			flags = CMLib.tracking().newFlags()
					.plus(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS)
					.plus(TrackingLibrary.TrackingFlag.NOAIR)
					.plus(TrackingLibrary.TrackingFlag.NOHOMES)
					.plus(TrackingLibrary.TrackingFlag.UNLOCKEDONLY);
			List<Room> radiusRooms = CMLib.tracking().getRadiantRooms(mobR, (TrackingFlags)null, range);
			int numFound=0;
			for(final Room R : radiusRooms)
			{
				if(!myArea.inMyMetroArea(R.getArea()))
					numFound++;
			}
			if(numFound == 0)
			{
				range *= 2;
				flags = CMLib.tracking().newFlags()
						.plus(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS)
						.plus(TrackingLibrary.TrackingFlag.NOAIR)
						.plus(TrackingLibrary.TrackingFlag.PASSABLE)
						.plus(TrackingLibrary.TrackingFlag.NOHOMES);
				radiusRooms = CMLib.tracking().getRadiantRooms(mobR, (TrackingFlags)null, range);
			}
			final Map<Area,int[]> areaCandidates = new HashMap<Area,int[]>();
			for(final Room R : radiusRooms)
			{
				if((!myArea.inMyMetroArea(R.getArea()))
				&&(!areaCandidates.containsKey(R.getArea())))
					areaCandidates.put(R.getArea(), new int[] {0});
			}
			final List<Area> removeThese = new ArrayList<Area>();
			for(final Area oA : areaCandidates.keySet())
			{
				boolean ok = false;
				for(final Enumeration<Room> o = oA.getMetroMap();o.hasMoreElements();)
				{
					final Room oR=o.nextElement();
					if(oR!=null)
					{
						for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
						{
							final Room ooR=oR.getRoomInDir(d);
							final Exit ooE=oR.getExitInDir(d);
							if((ooR!=null)
							&&(ooE != null))
							{
								if(myArea.inMyMetroArea(ooR.getArea()))
									ok=true;
								else
								if(ooR.getArea() != oR.getArea())
									areaCandidates.get(oA)[0]++;
							}
						}
					}
					if((!ok)||(areaCandidates.get(oA)[0]==0))
						removeThese.add(oA);
				}
			}
			for(final Area dA : removeThese)
				areaCandidates.remove(dA);
			final PairList<Area,int[]> goodAreas = new PairVector<Area,int[]>(areaCandidates.size());
			for(final Area dA : areaCandidates.keySet())
				goodAreas.add(dA,areaCandidates.get(dA));
			Collections.sort(goodAreas, new Comparator<Pair<Area,int[]>>() {
				@Override
				public int compare(final Pair<Area,int[]> o1, final Pair<Area,int[]> o2)
				{
					if(o1.second[0] == o2.second[0])
						return 0;
					if(o1.second[0] < o2.second[0])
						return 1;
					return -1;
				}
			});
			final Map<Area,Room> winners=new HashMap<Area,Room>();
			for(final Room R : radiusRooms)
			{
				if(areaCandidates.containsKey(R.getArea())
				&&(!winners.containsKey(R.getArea()))
				&&(CMLib.tracking().findTrailToRoom(mobR, R, flags, range).size()>0))
					winners.put(R.getArea(), R);
			}
			if(winners.size()>0)
			{
				for(int i=0;i<goodAreas.size();i++)
				{
					final Room R=winners.get(goodAreas.get(i).first);
					if(R!=null)
					{
						banishRoom=R;
						break;
					}
				}
			}
			if(banishRoom == null)
			{
				for(final Room R : radiusRooms)
				{
					if(!myArea.inMyMetroArea(R.getArea())
					&&(CMLib.tracking().findTrailToRoom(mobR, R, flags, range).size()>0))
					{
						banishRoom=R;
						break;
					}
				}
			}
		}
		return banishRoom;
	}

	public boolean judgeMe(final Law laws, MOB judge, final MOB officer, final MOB criminal, final LegalWarrant W, final Area A, final boolean debugging)
	{
		final List<LegalWarrant> relevantCrimes=getRelevantWarrants(laws.warrants(),W,criminal);
		if(CMath.bset(W.punishmentCode(),Law.PUNISHMENTMASK_SKIPTRIAL))
			judge=officer;
		if(debugging)
			Log.debugOut("Arrest","("+lastAreaName+"): "+W.crime()+" "+criminal.Name()+" judged for "+W.crime()+" has base action "+W.punishmentCode()+", and final judgement "+highestCrimeAction(laws,W,W.criminal()));
		boolean totallyDone=false;
		switch(highestCrimeAction(laws,W,W.criminal())&Law.PUNISHMENT_MASK)
		{
		case Law.PUNISHMENT_WARN:
		{
			if((judge==null)&&(officer!=null))
				judge=officer;
			final StringBuffer str=new StringBuffer("");
			str.append(L("@x1, you are in trouble for @x2.  ",criminal.name(),restOfCharges(laws,criminal)));
			for(int w2=0;w2<relevantCrimes.size();w2++)
			{
				final LegalWarrant W2=relevantCrimes.get(w2);
				if(W2.criminal()==criminal)
				{
					if(W2.witness()!=null)
						str.append(L("The charge of @x1 was witnessed by @x2.  ",fixCharge(W2),W2.witness().name()));
					if((W2.warnMsg()!=null)&&(W2.warnMsg().length()>0))
						str.append(W2.warnMsg()+"  ");
					if((W2.offenses()>0)&&(laws.getMessage(Law.MSG_PREVOFF).length()>0)&&(!CMath.bset(W.punishmentCode(),Law.PUNISHMENTMASK_SEPARATE)))
						str.append(laws.getMessage(Law.MSG_PREVOFF)+"  ");
				}
			}
			if((laws.getMessage(Law.MSG_WARNING).length()>0)&&(!CMath.bset(W.punishmentCode(),Law.PUNISHMENTMASK_DETAIN)))
				str.append(laws.getMessage(Law.MSG_WARNING)+"  ");
			if(criminal.isPlayer())
				CMLib.coffeeTables().bump(criminal, CoffeeTableRow.STAT_PAROLES);
			CMLib.commands().postSay(judge,criminal,str.toString(),false,false);
			totallyDone=true;
			break;
		}
		case Law.PUNISHMENT_THREATEN:
		{
			if((judge==null)&&(officer!=null))
				judge=officer;
			final StringBuffer str=new StringBuffer("");
			str.append(L("@x1, you are in trouble for @x2.  ",criminal.name(),restOfCharges(laws,criminal)));
			for(int w2=0;w2<relevantCrimes.size();w2++)
			{
				final LegalWarrant W2=relevantCrimes.get(w2);
				if(W2.criminal()==criminal)
				{
					if(W2.witness()!=null)
						str.append(L("The charge of @x1 was witnessed by @x2.  ",fixCharge(W2),W2.witness().name()));
					if((W2.warnMsg()!=null)&&(W2.warnMsg().length()>0))
						str.append(W2.warnMsg()+"  ");
					if((W2.offenses()>0)&&(laws.getMessage(Law.MSG_PREVOFF).length()>0)&&(!CMath.bset(W.punishmentCode(),Law.PUNISHMENTMASK_SEPARATE)))
						str.append(laws.getMessage(Law.MSG_PREVOFF)+"  ");
				}
			}
			if(criminal.isPlayer())
				CMLib.coffeeTables().bump(criminal, CoffeeTableRow.STAT_PAROLES);
			if((laws.getMessage(Law.MSG_THREAT).length()>0)&&(!CMath.bset(W.punishmentCode(),Law.PUNISHMENTMASK_DETAIN)))
				str.append(laws.getMessage(Law.MSG_THREAT)+"  ");
			CMLib.commands().postSay(judge,criminal,str.toString(),false,false);
			totallyDone=true;
			break;
		}
		case Law.PUNISHMENT_PAROLE1:
			if(judge!=null)
			{
				if((W.offenses()>0)&&(laws.getMessage(Law.MSG_PREVOFF).length()>0)&&(!CMath.bset(W.punishmentCode(),Law.PUNISHMENTMASK_SEPARATE)))
					CMLib.commands().postSay(judge,W.criminal(),laws.getMessage(Law.MSG_PREVOFF),false,false);
				if(laws.paroleMessages(0).length()>0)
					CMLib.commands().postSay(judge,criminal,laws.paroleMessages(0),false,false);
				W.setJailTime(laws.paroleTimes(0));
				W.setState(Law.STATE_PAROLING);
			}
			totallyDone=false;
			break;
		case Law.PUNISHMENT_PAROLE2:
			if(judge!=null)
			{
				if((W.offenses()>0)&&(laws.getMessage(Law.MSG_PREVOFF).length()>0)&&(!CMath.bset(W.punishmentCode(),Law.PUNISHMENTMASK_SEPARATE)))
					CMLib.commands().postSay(judge,W.criminal(),laws.getMessage(Law.MSG_PREVOFF),false,false);
				if(laws.paroleMessages(1).length()>0)
					CMLib.commands().postSay(judge,criminal,laws.paroleMessages(1),false,false);
				W.setJailTime(laws.paroleTimes(1));
				W.setState(Law.STATE_PAROLING);
			}
			totallyDone=false;
			break;
		case Law.PUNISHMENT_PAROLE3:
			if(judge!=null)
			{
				if((W.offenses()>0)&&(laws.getMessage(Law.MSG_PREVOFF).length()>0)&&(!CMath.bset(W.punishmentCode(),Law.PUNISHMENTMASK_SEPARATE)))
					CMLib.commands().postSay(judge,W.criminal(),laws.getMessage(Law.MSG_PREVOFF),false,false);
				if(laws.paroleMessages(2).length()>0)
					CMLib.commands().postSay(judge,criminal,laws.paroleMessages(2),false,false);
				W.setJailTime(laws.paroleTimes(2));
				W.setState(Law.STATE_PAROLING);
			}
			totallyDone=false;
			break;
		case Law.PUNISHMENT_PAROLE4:
			if(judge!=null)
			{
				if((W.offenses()>0)&&(laws.getMessage(Law.MSG_PREVOFF).length()>0)&&(!CMath.bset(W.punishmentCode(),Law.PUNISHMENTMASK_SEPARATE)))
					CMLib.commands().postSay(judge,W.criminal(),laws.getMessage(Law.MSG_PREVOFF),false,false);
				if(laws.paroleMessages(3).length()>0)
					CMLib.commands().postSay(judge,criminal,laws.paroleMessages(3),false,false);
				W.setJailTime(laws.paroleTimes(3));
				W.setState(Law.STATE_PAROLING);
			}
			totallyDone=false;
			break;
		case Law.PUNISHMENT_JAIL1:
			if(judge!=null)
			{
				if((W.offenses()>0)&&(laws.getMessage(Law.MSG_PREVOFF).length()>0)&&(!CMath.bset(W.punishmentCode(),Law.PUNISHMENTMASK_SEPARATE)))
					CMLib.commands().postSay(judge,W.criminal(),laws.getMessage(Law.MSG_PREVOFF),false,false);
				if(laws.jailMessages(0).length()>0)
					CMLib.commands().postSay(judge,criminal,laws.jailMessages(0),false,false);
				W.setJailTime(laws.jailTimes(0));
				W.setState(Law.STATE_JAILING);
			}
			totallyDone=false;
			break;
		case Law.PUNISHMENT_JAIL2:
			if(judge!=null)
			{
				if((W.offenses()>0)&&(laws.getMessage(Law.MSG_PREVOFF).length()>0)&&(!CMath.bset(W.punishmentCode(),Law.PUNISHMENTMASK_SEPARATE)))
					CMLib.commands().postSay(judge,W.criminal(),laws.getMessage(Law.MSG_PREVOFF),false,false);
				if(laws.jailMessages(1).length()>0)
					CMLib.commands().postSay(judge,criminal,laws.jailMessages(1),false,false);
				W.setJailTime(laws.jailTimes(1));
				W.setState(Law.STATE_JAILING);
			}
			totallyDone=false;
			break;
		case Law.PUNISHMENT_JAIL3:
			if(judge!=null)
			{
				if((W.offenses()>0)&&(laws.getMessage(Law.MSG_PREVOFF).length()>0)&&(!CMath.bset(W.punishmentCode(),Law.PUNISHMENTMASK_SEPARATE)))
					CMLib.commands().postSay(judge,W.criminal(),laws.getMessage(Law.MSG_PREVOFF),false,false);
				if(laws.jailMessages(2).length()>0)
					CMLib.commands().postSay(judge,criminal,laws.jailMessages(2),false,false);
				W.setJailTime(laws.jailTimes(2));
				W.setState(Law.STATE_JAILING);
			}
			totallyDone=false;
			break;
		case Law.PUNISHMENT_JAIL4:
			if(judge!=null)
			{
				if((W.offenses()>0)&&(laws.getMessage(Law.MSG_PREVOFF).length()>0)&&(!CMath.bset(W.punishmentCode(),Law.PUNISHMENTMASK_SEPARATE)))
					CMLib.commands().postSay(judge,W.criminal(),laws.getMessage(Law.MSG_PREVOFF),false,false);
				if(laws.jailMessages(3).length()>0)
					CMLib.commands().postSay(judge,criminal,laws.jailMessages(3),false,false);
				W.setJailTime(laws.jailTimes(3));
				W.setState(Law.STATE_JAILING);
			}
			totallyDone=false;
			break;
		case Law.PUNISHMENT_EXECUTE:
			if(judge!=null)
			{
				criminal.setFollowing(null);
				if((W.offenses()>0)&&(laws.getMessage(Law.MSG_PREVOFF).length()>0)&&(!CMath.bset(W.punishmentCode(),Law.PUNISHMENTMASK_SEPARATE)))
					CMLib.commands().postSay(judge,W.criminal(),laws.getMessage(Law.MSG_PREVOFF),false,false);
				if(laws.getMessage(Law.MSG_EXECUTE).length()>0)
					CMLib.commands().postSay(judge,criminal,laws.getMessage(Law.MSG_EXECUTE),false,false);
				W.setState(Law.STATE_EXECUTING);
			}
			totallyDone=false;
			break;
		}
		if(CMath.bset(W.punishmentCode(),Law.PUNISHMENTMASK_FINE)
		&&((judge != null)||(totallyDone)))
		{
			final double fines=getFine(laws,W,criminal);
			if((judge==null)&&(officer!=null))
				judge=officer;
			if((fines>0.0)&&(judge!=null))
			{
				CMLib.commands().postSay(judge,criminal,L("You are hereby fined @x1, payable to the local tax assessor.",CMLib.beanCounter().nameCurrencyShort(judge,fines)),false,false);
				Double D=finesAssessed.get(criminal);
				if(D==null)
					D=Double.valueOf(0.0);
				else
					finesAssessed.remove(criminal);
				finesAssessed.put(criminal,Double.valueOf(D.doubleValue()+fines));
			}
		}

		if((!totallyDone)
		&&(CMath.bset(W.punishmentCode(),Law.PUNISHMENTMASK_BANISH))
		&&(judge != null))
		{
			final int ticks=getBanishmentTicks(laws,W,criminal);
			final TimeClock C=CMLib.time().localClock(criminal);
			if((ticks > 0)
			&&(judge!=null)
			&&(criminal != null)
			&&(criminal.fetchEffect("Banishment")==null))
			{
				Ability bA=CMClass.getAbility("Banishment");
				if(ticks == Integer.MAX_VALUE)
				{
					CMLib.commands().postSay(judge,criminal,L("You are hereby banished!"),false,false);
					bA.invoke(judge, criminal, true, 0);
					bA = criminal.fetchEffect("Banishment");
					if(bA != null)
						bA.makeLongLasting();
				}
				else
				{
					CMLib.commands().postSay(judge,criminal,L("You are hereby banished for @x1 days.",""+(ticks/(CMProps.getTicksPerMudHour()*C.getHoursInDay()))),false,false);
					bA.invoke(judge, criminal, true, 0);
					bA = criminal.fetchEffect("Banishment");
					if(bA != null)
						bA.startTickDown(judge, criminal, ticks);
				}
				if((W.state() != Law.STATE_EXECUTING)
				&&(W.state() != Law.STATE_JAILING))
					W.setState(Law.STATE_BANISHING);
			}
		}

		if((!totallyDone)
		&&(CMath.bset(W.punishmentCode(),Law.PUNISHMENTMASK_SHAME))
		&&(judge != null))
		{
			final int ticks=getShameTicks(laws,W,criminal);
			final TimeClock C=CMLib.time().localClock(criminal);
			if((ticks > 0)
			&&(judge!=null)
			&&(criminal != null)
			&&(criminal.fetchEffect("Shaming")==null))
			{
				Ability bA=CMClass.getAbility("Shaming");
				if(ticks == Integer.MAX_VALUE)
				{
					CMLib.commands().postSay(judge,criminal,L("You are to be subject to public shaming!"),false,false);
					bA.invoke(judge, criminal, true, 0);
					bA = criminal.fetchEffect("Shaming");
					if(bA != null)
						bA.makeLongLasting();
				}
				else
				{
					CMLib.commands().postSay(judge,criminal,L("You are to be subject to public shaming for @x1 days.",""+(ticks/(CMProps.getTicksPerMudHour()*C.getHoursInDay()))),false,false);
					bA.invoke(judge, criminal, true, 0);
					bA = criminal.fetchEffect("Shaming");
					if(bA != null)
						bA.startTickDown(judge, criminal, ticks);
				}

			}
		}

		if((totallyDone)
		&&(CMath.bset(W.punishmentCode(),Law.PUNISHMENTMASK_DETAIN)))
		{
			W.setState(Law.STATE_DETAINING);
			if(officer!=null)
				W.setArrestingOfficer(A,officer);
			if(debugging)
				Log.debugOut("Arrest","("+lastAreaName+"): "+W.crime()+" Putting the above crime into a detain state, officer="+(W.arrestingOfficer()!=null)+".");
			return false;
		}
		return totallyDone;
	}

	@Override
	public boolean fillOutWarrant(final MOB criminalM,
								  final Law laws,
								  final Area myArea,
								  final Environmental victimM,
								  final String crimeLocs,
								  final String crimeFlags,
								  final String crime,
								  String sentence,
								  final String warnMsg)
	{
		if(criminalM.amDead())
		{
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.ARREST))
				Log.debugOut("ARREST","("+lastAreaName+"): "+criminalM.name()+", data: "+crimeLocs+"->"+crimeFlags+"->"+crime+"->"+sentence+" * IS DEAD!");
			return false;
		}
		if(criminalM.location()==null)
		{
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.ARREST))
				Log.debugOut("ARREST","("+lastAreaName+"): "+criminalM.name()+", data: "+crimeLocs+"->"+crimeFlags+"->"+crime+"->"+sentence+"* Accused is not here.");
			return false;
		}
		if((myArea!=null)&&(!myArea.inMyMetroArea(criminalM.location().getArea())))
		{
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.ARREST))
				Log.debugOut("ARREST","("+lastAreaName+"): "+criminalM.name()+", data: "+crimeLocs+"->"+crimeFlags+"->"+crime+"->"+sentence+"* Accused is not in the area.");
			return false;
		}

		if(isAnyKindOfOfficer(laws,criminalM)
		||(isTheJudge(laws,criminalM))
		||CMSecurity.isAllowed(criminalM,criminalM.location(),CMSecurity.SecFlag.ABOVELAW)
		||(this.isAnUltimateAuthorityHere(criminalM, laws)))
		{
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.ARREST))
				Log.debugOut("ARREST","("+lastAreaName+"): "+criminalM.name()+", data: "+crimeLocs+"->"+crimeFlags+"->"+crime+"->"+sentence+"* Accused is an officer ("+isAnyKindOfOfficer(laws,criminalM)+"), judge ("+isTheJudge(laws,criminalM)+"), or above the law ("+CMSecurity.isAllowed(criminalM,criminalM.location(),CMSecurity.SecFlag.ABOVELAW)+").");
			return false;
		}

		// is there a witness
		final MOB witness=getWitness(myArea,criminalM);
		boolean requiresWitness=true;

		// is there a victim (if necessary)
		MOB victim=null;
		if((victimM!=null)&&(victimM instanceof MOB))
			victim=(MOB)victimM;
		if(criminalM==victim)
		{
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.ARREST))
				Log.debugOut("ARREST","("+lastAreaName+"): "+criminalM.name()+", data: "+crimeLocs+"->"+crimeFlags+"->"+crime+"->"+sentence+"* Accused and victim are the same.");
			return false;
		}

		// any special circumstances?
		if(crimeFlags.trim().length()>0)
		{
			final Vector<String> V=CMParms.parse(crimeFlags.toUpperCase());
			for(int v=0;v<V.size();v++)
			{
				final String str=V.elementAt(v);
				if(str.endsWith("WITNESS")&&(str.length()<9))
				{
					if(str.startsWith("!"))
						requiresWitness=false;
					else
					if((witness!=null)&&(witness.location()!=criminalM.location()))
					{
						if(CMSecurity.isDebugging(CMSecurity.DbgFlag.ARREST))
							Log.debugOut("ARREST","("+lastAreaName+"): "+criminalM.name()+", data: "+crimeLocs+"->"+crimeFlags+"->"+crime+"->"+sentence+"* Witness required, but not present.");
						return false;
					}
				}
				else
				if(str.startsWith("IGNORE")&&(str.length()<9))
				{
					final int chanceToIgnore = CMath.s_int(str.substring(6));
					if(CMLib.dice().rollPercentage()<=chanceToIgnore)
					{
						if(CMSecurity.isDebugging(CMSecurity.DbgFlag.ARREST))
							Log.debugOut("ARREST","("+lastAreaName+"): "+criminalM.name()+", data: "+crimeLocs+"->"+crimeFlags+"->"+crime+"->"+sentence+"* Ignore chance.");
						return false;
					}
				}
				else
				if(str.startsWith("!IGNORE")&&(str.length()<10))
				{
					final int chanceToIgnore = 100-CMath.s_int(str.substring(7));
					if(CMLib.dice().rollPercentage()<=chanceToIgnore)
					{
						if(CMSecurity.isDebugging(CMSecurity.DbgFlag.ARREST))
							Log.debugOut("ARREST","("+lastAreaName+"): "+criminalM.name()+", data: "+crimeLocs+"->"+crimeFlags+"->"+crime+"->"+sentence+"* !Ignore chance.");
						return false;
					}
				}
				else
				if(str.endsWith("COMBAT")&&(str.length()<8))
				{
					if(criminalM.isInCombat())
					{
						if(str.startsWith("!"))
						{
							if(CMSecurity.isDebugging(CMSecurity.DbgFlag.ARREST))
								Log.debugOut("ARREST","("+lastAreaName+"): "+criminalM.name()+", data: "+crimeLocs+"->"+crimeFlags+"->"+crime+"->"+sentence+"* In combat, but shouldn't be!");
							return false;
						}
					}
					else
					{
						if(!str.startsWith("!"))
						{
							if(CMSecurity.isDebugging(CMSecurity.DbgFlag.ARREST))
								Log.debugOut("ARREST","("+lastAreaName+"): "+criminalM.name()+", data: "+crimeLocs+"->"+crimeFlags+"->"+crime+"->"+sentence+"* Not in combat, but should be!");
							return false;
						}
					}
				}
				else
				if(str.endsWith("RECENTLY")&&(str.length()<10))
				{
					final LegalWarrant W=laws.getOldWarrant(criminalM,crime,false);
					final long thisTime=System.currentTimeMillis();
					if((W!=null)&&((thisTime-W.lastOffense())<600000))
					{
						if(str.startsWith("!"))
						{
							if(CMSecurity.isDebugging(CMSecurity.DbgFlag.ARREST))
								Log.debugOut("ARREST","("+lastAreaName+"): "+criminalM.name()+", data: "+crimeLocs+"->"+crimeFlags+"->"+crime+"->"+sentence+"* Not recently, but is!");
							return false;
						}
					}
					else
					{
						if(!str.startsWith("!"))
						{
							if(CMSecurity.isDebugging(CMSecurity.DbgFlag.ARREST))
								Log.debugOut("ARREST","("+lastAreaName+"): "+criminalM.name()+", data: "+crimeLocs+"->"+crimeFlags+"->"+crime+"->"+sentence+"* Recently required, but it isn't!");
							return false;
						}
					}
				}
			}
		}
		if((requiresWitness)&&(witness==null))
		{
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.ARREST))
				Log.debugOut("ARREST","("+lastAreaName+"): "+criminalM.name()+", data: "+crimeLocs+"->"+crimeFlags+"->"+crime+"->"+sentence+"* Witness required, and none present: .");
			return false;
		}

		// is the location significant to this crime?
		if(crimeLocs.trim().length()>0)
		{
			boolean aCrime=false;
			final Vector<String> V=CMParms.parse(crimeLocs);
			final String display=criminalM.location().displayText().toUpperCase().trim();
			for(int v=0;v<V.size();v++)
			{
				final String str=V.elementAt(v).toUpperCase();
				if(str.endsWith("INDOORS")&&(str.length()<9))
				{
					if((criminalM.location().domainType()&Room.INDOORS)>0)
					{
						if(str.startsWith("!"))
						{
							if(CMSecurity.isDebugging(CMSecurity.DbgFlag.ARREST))
								Log.debugOut("ARREST","("+lastAreaName+"): "+criminalM.name()+", data: "+crimeLocs+"->"+crimeFlags+"->"+crime+"->"+sentence+"* Shouldn't be indoors, but is!");
							return false;
						}
					}
					else
					{
						if(!str.startsWith("!"))
						{
							if(CMSecurity.isDebugging(CMSecurity.DbgFlag.ARREST))
								Log.debugOut("ARREST","("+lastAreaName+"): "+criminalM.name()+", data: "+crimeLocs+"->"+crimeFlags+"->"+crime+"->"+sentence+"* Should be indoors, but isn't!");
							return false;
						}
					}
					aCrime=true;
				}
				else
				if(str.endsWith("HOME")&&(str.length()<6))
				{
					if(CMLib.law().doesHavePriviledgesHere(criminalM,criminalM.location()))
					{
						if(str.startsWith("!"))
						{
							if(CMSecurity.isDebugging(CMSecurity.DbgFlag.ARREST))
								Log.debugOut("ARREST","("+lastAreaName+"): "+criminalM.name()+", data: "+crimeLocs+"->"+crimeFlags+"->"+crime+"->"+sentence+"* Should not be home, but is!");
							return false;
						}
					}
					if(!str.startsWith("!"))
					{
						if(CMSecurity.isDebugging(CMSecurity.DbgFlag.ARREST))
							Log.debugOut("ARREST","("+lastAreaName+"): "+criminalM.name()+", data: "+crimeLocs+"->"+crimeFlags+"->"+crime+"->"+sentence+"* Should be home, but is not!");
						return false;
					}
					aCrime=true;
				}
				else
				if(str.startsWith("!")&&(CMLib.english().containsString(display,str.substring(1))))
				{
					if(CMSecurity.isDebugging(CMSecurity.DbgFlag.ARREST))
						Log.debugOut("ARREST","("+lastAreaName+"): "+criminalM.name()+", data: "+crimeLocs+"->"+crimeFlags+"->"+crime+"->"+sentence+"* Should not be at '"+str.substring(1)+"', but is!");
					return false;
				}
				else
				if(CMLib.english().containsString(display,str))
				{
					aCrime = true;
					break;
				}
			}
			if(!aCrime)
			{
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.ARREST))
					Log.debugOut("ARREST","("+lastAreaName+"): "+criminalM.name()+", data: "+crimeLocs+"->"+crimeFlags+"->"+crime+"->"+sentence+"* Crime flag failure!");
				return false;
			}
		}

		// is the victim a protected race?
		if((victim!=null)&&(!(victim instanceof Deity)))
		{
			if(!CMLib.masking().maskCheck(laws.getMessage(Law.MSG_PROTECTEDMASK),victim,false))
			{
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.ARREST))
					Log.debugOut("ARREST","("+lastAreaName+"): "+criminalM.name()+", data: "+crimeLocs+"->"+crimeFlags+"->"+crime+"->"+sentence+"* Victim is not a protected race!");
				return false;
			}
		}

		// does a warrant already exist?
		LegalWarrant W=null;
		for(int i=0;(W=laws.getWarrant(criminalM,i))!=null;i++)
		{
			if((W.criminal()==criminalM)
			&&(W.victim()==victim)
			&&(W.crime().equals(crime)))
			{
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.ARREST))
					Log.debugOut("ARREST","("+lastAreaName+"): "+criminalM.name()+", data: "+crimeLocs+"->"+crimeFlags+"->"+crime+"->"+sentence+"* Warrant already exists.");
				return false;
			}
		}
		W=laws.getOldWarrant(criminalM,crime,true);
		if(W==null)
			W=(LegalWarrant)CMClass.getCommon("DefaultArrestWarrant");

		// fill out the warrant!
		W.setCriminal(criminalM);
		W.setVictim(victim);
		W.setCrime(crime);
		W.setState(Law.STATE_SEEKING);
		W.setWitness(requiresWitness?witness:null);
		W.setLastOffense(System.currentTimeMillis());
		W.setWarnMsg(warnMsg);
		sentence=sentence.trim();
		final Vector<String> sentences=CMParms.parse(sentence);
		W.setPunishment(0);
		for(int v=0;v<sentences.size();v++)
		{
			String s=sentences.elementAt(v);
			final int x=s.indexOf('=');
			String parm=null;
			if(x>0)
			{
				parm=s.substring(x+1);
				s=s.substring(0,x+1);
			}
			boolean actionCodeSet=false;
			for(int i=0;i<Law.PUNISHMENT_DESCS.length;i++)
			{
				if(s.equalsIgnoreCase(Law.PUNISHMENT_DESCS[i]))
				{
					actionCodeSet=true;
					W.setPunishment(i);
					if(parm!=null)
						W.addPunishmentParm(i,parm);
				}
			}
			for(int i=0;i<Law.PUNISHMENTMASK_DESCS.length;i++)
			{
				if(s.equalsIgnoreCase(Law.PUNISHMENTMASK_DESCS[i]))
				{
					actionCodeSet=true;
					W.setPunishment(W.punishmentCode()|Law.PUNISHMENTMASK_CODES[i]);
					if(parm!=null)
						W.addPunishmentParm(Law.PUNISHMENTMASK_CODES[i],parm);
				}
			}
			if(!actionCodeSet)
			{
				Log.errOut("Arrest","Unknown sentence: "+s+" for crime "+crime);
				return false;
			}
		}

		if((W.victim()!=null)
		&&(isTroubleMaker(W.victim()))
		&&(!CMath.bset(W.punishmentCode(),Law.PUNISHMENTMASK_SEPARATE)))
			W.setPunishment((W.punishmentCode()&Law.PUNISHMENT_MASK)/2);

		if((isStillACrime(W,CMSecurity.isDebugging(CMSecurity.DbgFlag.ARREST)))
		&&((W.witness()==null)||CMLib.flags().canBeSeenBy(W.criminal(),W.witness())))
		{
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.ARREST))
				Log.debugOut("ARREST","("+lastAreaName+"): "+criminalM.name()+", data: "+crimeLocs+"->"+crimeFlags+"->"+crime+"->"+sentence+"* Warrant filled out.");
			if(!addWarrant(laws,W))
				return false;
		}
		else
		if(CMSecurity.isDebugging(CMSecurity.DbgFlag.ARREST))
			Log.debugOut("ARREST","("+lastAreaName+"): "+criminalM.name()+", data: "+crimeLocs+"->"+crimeFlags+"->"+crime+"->"+sentence+"* Warrant fails the is a crime check.");
		return true;
	}

	protected boolean isAnUltimateAuthorityHere(final MOB M, final Law laws)
	{
		if(CMSecurity.isAllowed(M,M.location(),CMSecurity.SecFlag.ABOVELAW)||(isTheJudge(laws,M)))
			return true;
		return false;
	}

	protected boolean theLawIsEnabled()
	{
		return ((CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
				&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.ARREST)));
	}

	public boolean testBannedItem(final Law laws, final Area myArea, final MOB testMOB, final Item I)
	{
		if((I!=null)
		&&(!CMath.bset(I.phyStats().sensesMask(), PhyStats.SENSE_UNLOCATABLE)))
		{
			final Boolean B = bannedItemCache.get(I.Name());
			if(B != null)
			{
				if(!B.booleanValue())
					return false;
			}
			final String rsc=RawMaterial.CODES.NAME(I.material()).toUpperCase();
			String subType = null;
			if((I instanceof RawMaterial)
			&&(((RawMaterial)I).getSubType().length()>0))
				subType = ((RawMaterial)I).getSubType().toUpperCase();
			for(final Pair<List<String>,String[]> crime : laws.bannedItems())
			{
				for(final String word : crime.first)
				{
					if((CMLib.english().containsString(I.name(),word))
					||rsc.equals(word)
					||((subType != null) && word.equals(subType)))
					{
						final String[] info=crime.second;
						fillOutWarrant(testMOB,
										laws,
										myArea,
										I,
										info[Law.BIT_CRIMELOCS],
										info[Law.BIT_CRIMEFLAGS],
										info[Law.BIT_CRIMENAME],
										info[Law.BIT_SENTENCE],
										info[Law.BIT_WARNMSG]);
						bannedItemCache.put(I.Name(), Boolean.TRUE);
						return true;
					}
				}
			}
			bannedItemCache.put(I.Name(), Boolean.FALSE);
		}
		return false;
	}

	public void testEntryLaw(final Law laws, final Area myArea, final MOB testMOB, final Room R)
	{
		if((laws.basicCrimes().containsKey("NUDITY"))
		&&(!testMOB.isMonster())
		&&(!CMLib.flags().isGolem(testMOB))
		&&(!CMLib.flags().isInsect(testMOB))
		&&(!CMLib.flags().isAPlant(testMOB))
		&&((testMOB.charStats().getWearableRestrictionsBitmap()&(Wearable.WORN_LEGS|Wearable.WORN_WAIST|Wearable.WORN_ABOUT_BODY))==0)
		&&(testMOB.fetchFirstWornItem(Wearable.WORN_LEGS)==null)
		&&(testMOB.getWearPositions(Wearable.WORN_LEGS)>0)
		&&(testMOB.fetchFirstWornItem(Wearable.WORN_WAIST)==null)
		&&(testMOB.getWearPositions(Wearable.WORN_WAIST)>0)
		&&(testMOB.fetchFirstWornItem(Wearable.WORN_ABOUT_BODY)==null)
		&&(testMOB.getWearPositions(Wearable.WORN_ABOUT_BODY)>0))
		{
			final String info[]=laws.basicCrimes().get("NUDITY");
			fillOutWarrant(testMOB,
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
		&&((!testMOB.isMonster())||(!myArea.inMyMetroArea(CMLib.map().getStartArea(testMOB))))
		&&(!testMOB.isInCombat())
		&&((w=testMOB.fetchWieldedItem())!=null)
		&&(w instanceof Weapon)
		&&(testMOB.getPeaceTime()>(2*MOB.END_SHEATH_TIME))
		&&(((Weapon)w).weaponClassification()!=Weapon.CLASS_NATURAL)
		&&(((Weapon)w).weaponClassification()!=Weapon.CLASS_HAMMER)
		&&(((Weapon)w).weaponClassification()!=Weapon.CLASS_STAFF)
		&&(CMLib.flags().isSeeable(w))
		&&(!CMLib.flags().isHidden(w))
		&&(!CMLib.flags().isInvisible(w)))
		{
			final String info[]=laws.basicCrimes().get("ARMED");
			if((testMOB.session()==null)
			||(((System.currentTimeMillis()-testMOB.session().getLastNPCFight())>30000)
				&&((System.currentTimeMillis()-testMOB.session().getLastPKFight())>30000)))
			{
				fillOutWarrant(testMOB,
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

		if((laws.basicCrimes().containsKey("TRESPASSING"))
		&&((CMLib.masking().maskCheck(laws.getMessage(Law.MSG_TRESPASSERMASK),testMOB,false))
			||(testMOB.isMonster()
				&&(testMOB.getStartRoom()!=null)
				&&(testMOB.getStartRoom().getArea()!=R.getArea())
				&&(CMLib.flags().isPossiblyAggressive(testMOB))
				&&((!(testMOB instanceof Rideable))||(((Rideable)testMOB).numRiders()==0))
				&&((testMOB.amFollowing()==null)
					||((!testMOB.amFollowing().isMonster())&&(testMOB.amFollowing().location()==testMOB.location())))
				&&(!CMLib.masking().maskCheck(laws.getMessage(Law.MSG_PROTECTEDMASK),testMOB,false)))))
		{
			final String[] info=laws.basicCrimes().get("TRESPASSING");
			fillOutWarrant(testMOB,
							laws,
						   myArea,
						   null,
						   info[Law.BIT_CRIMELOCS],
						   info[Law.BIT_CRIMEFLAGS],
						   info[Law.BIT_CRIMENAME],
						   info[Law.BIT_SENTENCE],
						   info[Law.BIT_WARNMSG]);
		}
		if(laws.bannedItems().size()>0)
		{
			if(!bannedMOBCheck.contains(testMOB.Name()))
			{
				if((!testMOB.isPlayer())
				&&(testMOB.getStartRoom() != null)
				&&(testMOB.getStartRoom().getArea()== R.getArea())
				&&(CMLib.dice().rollPercentage()>1))
				{
					// locals get a pass almost all of the time.
				}
				else
				{
					bannedMOBCheck.add(testMOB.Name());
					int total = testMOB.numItems() / 5;
					if((total < 10) && (testMOB.numItems() >= 10))
						total = 10;
					for(int i=0;i<total;i++)
					{
						final Item I=testMOB.getRandomItem();
						if(testBannedItem(laws, myArea, testMOB, I))
							break;
					}
					final Set<? extends Rider> folMs=testMOB.getGroupMembersAndRideables(new HashSet<Rider>());
					for(final Rider rid : folMs)
					{
						if(rid instanceof Container)
						{
							for(final Item I : ((Container)rid).getDeepContents())
							{
								if(testBannedItem(laws, myArea, testMOB, I))
									break;
							}
						}
					}
				}
			}
		}
	}

	private void fillOutMurderWarrant(final Law laws, final Area myArea, final MOB criminal, final MOB victim)
	{
		for(final LegalWarrant W : laws.warrants())
		{
			if((W.victim()!=null)
			&&(W.criminal()!=null)
			&&(W.victim()==victim)
			&&(!CMath.bset(W.punishmentCode(),Law.PUNISHMENTMASK_SEPARATE))
			&&(W.criminal()==criminal))
				laws.warrants().remove(W);
		}
		final String[] bits=laws.basicCrimes().get("MURDER");
		fillOutWarrant(criminal,
					   laws,
					   myArea,
					   victim,
					   bits[Law.BIT_CRIMELOCS],
					   bits[Law.BIT_CRIMEFLAGS],
					   bits[Law.BIT_CRIMENAME],
					   bits[Law.BIT_SENTENCE],
					   bits[Law.BIT_WARNMSG]);
	}

	@Override
	public void executeMsg(final Environmental affecting, final CMMsg msg)
	{
		super.executeMsg(affecting, msg);
		if(!(affecting instanceof Area))
			return;
		if(!theLawIsEnabled())
			return;

		final Area myArea=(Area)affecting;
		final Law laws=getLaws(affecting,false);
		if(!laws.lawIsActivated())
			return;
		if(msg.source()==null)
			return;
		if (lastAreaName == null) lastAreaName = myArea.Name();

		// the archons pardon
		if((msg.sourceMinor()==CMMsg.TYP_SPEAK)
		&&(msg.sourceMessage()!=null)
		&&(isAnUltimateAuthorityHere(msg.source(),laws)))
		{
			final int x=msg.sourceMessage().toUpperCase().indexOf("I HEREBY PARDON ");
			if(x>0)
			{
				int y=msg.sourceMessage().lastIndexOf('\'');
				if(y<x)
					y=msg.sourceMessage().lastIndexOf('`');
				String name=null;
				if(y>x)
					name=msg.sourceMessage().substring(x+16,y).trim();
				else
					name=msg.sourceMessage().substring(x+16).trim();
				if(name.length()>0)
				{
					for(final LegalWarrant W : laws.warrants())
					{
						if((W.criminal()!=null)&&(CMLib.english().containsString(W.criminal().Name(),name)))
						{
							final Ability A=W.criminal().fetchEffect("Prisoner");
							if(A!=null)
								A.unInvoke();
							if(W.jail()!=W.criminal().location())
							{
								if(W.arrestingOfficer()!=null)
									dismissOfficer(W.arrestingOfficer());
								laws.warrants().remove(W);
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
		}

		if((msg.sourceMinor()==CMMsg.TYP_DEATH)
		&&(msg.tool() instanceof MOB)
		&&(laws.basicCrimes().containsKey("MURDER"))
		&&((!msg.source().isMonster())
			||(!isTroubleMaker(msg.source()))
			||(this.isAnyKindOfOfficer(laws, msg.source()))))
		{
			final MOB criminal=(MOB)msg.tool();
			this.fillOutMurderWarrant(laws, myArea, criminal, msg.source());
			if(criminal.isMonster() && (isAnyKindOfOfficer(laws, msg.source())))
			{
				final MOB leaderM = criminal.getGroupLeader();
				if((leaderM != criminal) && (!leaderM.isMonster()))
					this.fillOutMurderWarrant(laws, myArea, leaderM, msg.source());
			}
			return;
		}

		if(isAnyKindOfOfficer(laws,msg.source())
		||(isTheJudge(laws,msg.source())))
		{
			if((msg.sourceMinor()==CMMsg.TYP_ENTER)
			&&(msg.target() instanceof Room))
			{
				final Room R=(Room)msg.target();
				MOB M=null;
				for(int m=0;m<R.numInhabitants();m++)
				{
					M=R.fetchInhabitant(m);
					if((M!=null)
					&&(M!=msg.source())
					&&(!isAnyKindOfOfficer(laws,M))
					&&(!isTheJudge(laws,M)))
						testEntryLaw(laws,myArea,M,R);
				}
			}
			//this is why we are testing for officers and judges.. so we can get out of here
			return;
		}

		if((msg.source().isMonster())&&(!laws.arrestMobs()))
			return;

		if(!CMLib.flags().isAliveAwakeMobile(msg.source(),true))
			return;

		final Room R=msg.source().location();
		if(R==null)
			return;

		if((msg.tool() instanceof Ability)
		&&(msg.othersMessage()!=null)
		&&((laws.abilityCrimes().containsKey(msg.tool().ID().toUpperCase()))
			||(laws.abilityCrimes().containsKey(CMLib.flags().getAbilityType_((Ability)msg.tool())))
			||(laws.abilityCrimes().containsKey(CMLib.flags().getAbilityDomain((Ability)msg.tool()))))
		&&(msg.sourceMinor()!=CMMsg.TYP_TEACH))
		{
			String[] info=laws.abilityCrimes().get(msg.tool().ID().toUpperCase());
			if(info==null)
				info=laws.abilityCrimes().get(CMLib.flags().getAbilityType_((Ability)msg.tool()));
			if(info==null)
				info=laws.abilityCrimes().get(CMLib.flags().getAbilityDomain((Ability)msg.tool()));
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

		for(final Enumeration<Ability> a=msg.source().effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)
			&&(!A.isAutoInvoked())
			&&((A.canBeUninvoked()||(!msg.source().isMonster())))
			&&((laws.abilityCrimes().containsKey("$"+A.ID().toUpperCase()))
				||(laws.abilityCrimes().containsKey("$"+CMLib.flags().getAbilityType_(A)))
				||(laws.abilityCrimes().containsKey("$"+CMLib.flags().getAbilityDomain(A)))))
			{
				String[] info=laws.abilityCrimes().get("$"+A.ID().toUpperCase());
				if(info==null)
					info=laws.abilityCrimes().get("$"+CMLib.flags().getAbilityType_(A));
				if(info==null)
					info=laws.abilityCrimes().get("$"+CMLib.flags().getAbilityDomain(A));
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

		if((CMath.bset(msg.targetMajor(),CMMsg.MASK_MALICIOUS))
		&&(msg.target() instanceof MOB)
		&&(!CMath.bset(msg.sourceMajor(),CMMsg.MASK_ALWAYS))
		&&((msg.tool()==null)
			||(msg.source().isMine(msg.tool())
				&& ((!(msg.tool() instanceof DiseaseAffect)) || (((DiseaseAffect)msg.tool()).isMalicious()))))
		&&(msg.target()!=msg.source())
		&&(!msg.target().name().equals(msg.source().name())))
		{
			if(isTheJudge(laws,(MOB)msg.target()))
			{
				if(!msg.source().isMonster())
				{
					for(int i=0;i<R.numInhabitants();i++)
					{
						final MOB M=R.fetchInhabitant(i);
						if((M!=null)
						&&(M!=msg.target())
						&&(M!=msg.source())
						&&(M.getVictim()!=msg.source())
						&&(isAnyKindOfOfficer(laws,M)))
						{
							if(msg.source().amFollowing()==M)
								msg.source().setFollowing(null);
							CMLib.commands().postSay(M,null,L("Ack! Treason! Die!"),false,false);
							M.setVictim(msg.source());
						}
					}
				}
			}
			else
			{
				boolean justResisting=false;
				boolean turnAbout=false;
				final boolean targetIsOfficer=isAnyKindOfOfficer(laws,(MOB)msg.target());
				final String[] assaultInfo=laws.basicCrimes().get("ASSAULT");
				final String[] murderInfo=laws.basicCrimes().get("MURDER");
				if((assaultInfo!=null)&&(murderInfo!=null))
				{
					for(final LegalWarrant W : laws.warrants())
					{
						if(targetIsOfficer
						&&(W.criminal()==msg.source())
						&&(W.arrestingOfficer()!=null)
						&&(W.criminal().location()!=null)
						&&(W.criminal().location().isInhabitant(W.arrestingOfficer())))
							justResisting=true;
						else
						if((!targetIsOfficer)
						&&(W.criminal()==msg.target())
						&&((W.victim()==msg.source())||((msg.source().amFollowing()!=null)&&(W.victim()==msg.source().amFollowing())))
						&&(W.crime().equals(assaultInfo[Law.BIT_CRIMENAME])||W.crime().equals(murderInfo[Law.BIT_CRIMENAME]))
						&&(isStillACrime(W,false)))
							turnAbout=true;
						else
						if((!targetIsOfficer)
						&&(W.victim()==msg.target())
						&&(W.criminal()==msg.source())
						&&(W.crime().equals(murderInfo[Law.BIT_CRIMENAME]))
						&&(isStillACrime(W,false)))
							turnAbout=true;
					}
				}
				if(justResisting)
				{
					if(laws.basicCrimes().containsKey("RESISTINGARREST"))
					{
						final String[] info=laws.basicCrimes().get("RESISTINGARREST");
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
				if((!turnAbout)
				&&(assaultInfo!=null)
				&&((msg.source().isMonster())||(!isTroubleMaker((MOB)msg.target()))))
				{
					fillOutWarrant(msg.source(),
									laws,
									myArea,
									msg.target(),
									assaultInfo[Law.BIT_CRIMELOCS],
									assaultInfo[Law.BIT_CRIMEFLAGS],
									assaultInfo[Law.BIT_CRIMENAME],
									assaultInfo[Law.BIT_SENTENCE],
									assaultInfo[Law.BIT_WARNMSG]);
				}
			}
		}

		if((msg.othersCode()!=CMMsg.NO_EFFECT)
		   &&(msg.othersMessage()!=null))
		{
			if((msg.targetMinor()==CMMsg.TYP_GET)
			&&(msg.target() instanceof Item)
			&&(laws.bannedItems().size()>0))
				testBannedItem(laws, myArea, msg.source(), (Item)msg.target());

			if(msg.sourceMinor()==CMMsg.TYP_ENTER)
				testEntryLaw(laws,myArea,msg.source(),R);

			for(int i=0;i<laws.otherCrimes().size();i++)
			{
				final List<String> V=laws.otherCrimes().get(i);
				for(int v=0;v<V.size();v++)
				{
					if(CMLib.english().containsString(msg.othersMessage(),V.get(v)))
					{
						final String[] info=laws.otherBits().get(i);
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

	public void haveMobReactToLaw(final MOB mob, final MOB officer)
	{
		if((mob.isMonster())
		&&(!CMLib.flags().isSitting(mob))
		&&(mob.amFollowing()==null)
		&&(!mob.isInCombat()))
		{
			final boolean good=CMLib.flags().isGood(mob);
			final boolean evil=CMLib.flags().isEvil(mob);
			final boolean neutral=(!good)&&(!evil);
			if(evil
			||(neutral&&(CMLib.dice().rollPercentage()>50))
			||(CMLib.flags().flaggedBehaviors(mob,Behavior.FLAG_POTENTIALLYAGGRESSIVE).size()>0))
			{
				if(mob.phyStats().level()>(officer.phyStats().level()/2))
					mob.setVictim(officer);
				else
				if(!CMLib.flags().isAnimalIntelligence(mob))
					mob.enqueCommand(CMParms.parse("FLEE"),MUDCmdProcessor.METAFLAG_FORCED|MUDCmdProcessor.METAFLAG_ORDER,1);
			}
			else
			if((good||neutral)
			&&(!CMLib.flags().isAnimalIntelligence(mob)))
			{
				mob.makePeace(true);
				mob.doCommand(CMParms.parse("SIT"),MUDCmdProcessor.METAFLAG_FORCED|MUDCmdProcessor.METAFLAG_ORDER);
			}
			else
			if((CMLib.flags().isAnimalIntelligence(mob))&&(CMLib.dice().rollPercentage()>50))
			{
				mob.makePeace(true);
				mob.doCommand(CMParms.parse("SIT"),MUDCmdProcessor.METAFLAG_FORCED|MUDCmdProcessor.METAFLAG_ORDER);
			}
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		super.tick(ticking,tickID);

		if(!(ticking instanceof Area))
			return true;
		if(tickID!=Tickable.TICKID_AREA)
			return true;
		final Area myArea=(Area)ticking;

		if(!theLawIsEnabled())
			return true;

		final Law laws=getLaws(myArea,false);
		if(!laws.lawIsActivated())
		{
			laws.warrants().clear();
			laws.oldWarrants().clear();
			return true;
		}
		final boolean debugging=CMSecurity.isDebugging(CMSecurity.DbgFlag.ARREST);

		laws.propertyTaxTick(myArea,debugging);

		final HashSet<String> handled=new HashSet<String>();
		for(final LegalWarrant W : laws.warrants())
		{
			if((W.criminal()==null)
			||(W.criminal().location()==null))
			{
				if(debugging)
					Log.debugOut("Arrest","("+lastAreaName+"): Tick: "+W.crime()+": Criminal or Location is null. Skipping.");
				continue;
			}

			if(!isStillACrime(W,debugging))
			{
				if(getWarrantsOf(myArea,W.criminal()).size()== 0)
				{
					unCuff(W.criminal());
					if(W.arrestingOfficer()!=null)
					{
						dismissOfficer(W.arrestingOfficer());
					}
					W.setArrestingOfficer(myArea,null);
				}
				W.setOffenses(W.offenses()+1);
				laws.oldWarrants().add(W);
				laws.warrants().remove(W);
				if(debugging)
					Log.debugOut("Arrest","("+lastAreaName+"): Tick: "+W.crime()+": No longer a crime.");
				continue;
			}

			if(!CMath.bset(W.punishmentCode(),Law.PUNISHMENTMASK_SEPARATE))
			{
				if(handled.contains(W.criminal().Name()))
					continue;
				handled.add(W.criminal().Name());
			}
			else
			{
				if(handled.contains(W.criminal().Name()+"/"+W.crime()))
					continue;
				handled.add(W.criminal().Name()+"/"+W.crime());
			}
			if(debugging)
				Log.debugOut("Arrest","("+lastAreaName+"): Tick: Handling "+W.crime()+" for "+W.criminal().Name()+": State "+W.state());

			if(System.currentTimeMillis() < W.getIgnoreUntilTime())
			{
				if(debugging)
					Log.debugOut("Arrest","("+lastAreaName+"): Tick: Ignoring "+W.crime()+" for "+W.criminal().Name()+": State "+W.state());
				continue;
			}

			processWarrant(myArea, laws, W, debugging);
		}
		return true;
	}

	protected void fileArrestResister(final Law laws, final Area myArea, final LegalWarrant W)
	{
		if((W.criminal()!=null)
		&&(W.arrestingOfficer()!=null)
		&&(!W.arrestingOfficer().amDead())
		&&(!W.crime().equalsIgnoreCase("pardoned"))
		&&(!CMLib.flags().isInTheGame(W.criminal(),true))
		&&(isStillACrime(W,false)))
		{
			if(laws.basicCrimes().containsKey("RESISTINGARREST"))
			{
				final String[] info=laws.basicCrimes().get("RESISTINGARREST");
				fillOutWarrant(W.criminal(),
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
	}

	protected void processWarrant(final Area myArea, final Law laws, final LegalWarrant W, final boolean debugging)
	{
		MOB officer=W.arrestingOfficer();
		if((officer!=null) && (W.state()!=Law.STATE_SEEKING))
		{
			for(int b=0;b<officer.numBehaviors();b++)
			{
				final Behavior B=officer.fetchBehavior(b);
				if(B instanceof MobileBehavior)
					((MobileBehavior)B).suspendMobility(1);
			}
		}
		switch(W.state())
		{
		case Law.STATE_SEEKING:
			{
				final Room criminR = W.criminal().location();
				if((criminR==null)||(!criminR.isInhabitant(W.criminal())))
					break; // they are offline right now
				if((officer==null)||(!criminR.isInhabitant(officer)))
					officer=null;
				if(officer==null)
					officer=getEligibleOfficer(laws,myArea,W.criminal(),W.victim());
				W.setTravelAttemptTime(0);
				if((officer!=null)
				&&(W.criminal().location()!=null)
				&&(W.criminal().location().isInhabitant(officer))
				&&(!W.criminal().amDead())
				&&(CMLib.flags().isInTheGame(W.criminal(),true))
				&&(CMLib.flags().canBeSeenBy(W.criminal(),officer))
				&&(canFocusOn(officer,W.criminal())))
				{
					if(CMSecurity.isAllowed(W.criminal(),W.criminal().location(),CMSecurity.SecFlag.ABOVELAW))
					{
						CMLib.commands().postSay(officer,W.criminal(),L("Damn, I can't arrest you."),false,false);
						if(CMSecurity.isAllowedEverywhere(W.criminal(),CMSecurity.SecFlag.ABOVELAW))
						{
							fileAllWarrants(laws,W,W.criminal());
							unCuff(W.criminal());
							W.setArrestingOfficer(myArea,null);
						}
					}
					else
					if(W.crime().equalsIgnoreCase("pardoned"))
					{
						fileAllWarrants(laws,W,W.criminal());
						unCuff(W.criminal());
						W.setArrestingOfficer(myArea,null);
					}
					else
					if(judgeMe(laws,null,officer,W.criminal(),W,myArea,debugging))
					{
						fileAllWarrants(laws,W,W.criminal());
						unCuff(W.criminal());
						dismissOfficer(officer);
						W.setArrestingOfficer(myArea,null);
					}
					else
					if(W.state()!=Law.STATE_DETAINING)
					{
						if(!CMLib.flags().isAnimalIntelligence(W.criminal()))
						{
							W.setArrestingOfficer(myArea,officer);
							final LegalWarrant copKillerW=laws.getCopkiller(myArea,this,W.criminal());
							if(copKillerW!=null)
							{
								CMLib.commands().postSay(W.arrestingOfficer(),W.criminal(),laws.getMessage(Law.MSG_COPKILLER),false,false);
								W.setState(Law.STATE_SUBDUEING);
								W.arrestingOfficer().setVictim(W.criminal());
								processWarrant(myArea, laws, W, debugging);
								return;
							}
							final LegalWarrant lawResistW=laws.getLawResister(myArea,this,W.criminal());
							if(lawResistW!=null)
							{
								CMLib.commands().postSay(W.arrestingOfficer(),W.criminal(),laws.getMessage(Law.MSG_RESISTFIGHT),false,false);
								W.setState(Law.STATE_SUBDUEING);
								W.arrestingOfficer().setVictim(W.criminal());
								processWarrant(myArea, laws, W, debugging);
								return;
							}

							CMLib.commands().postSay(W.arrestingOfficer(),W.criminal(),L("You are under arrest for @x1! Sit down on the ground immediately!",restOfCharges(laws,W.criminal())),false,false);
							W.setState(Law.STATE_ARRESTING);
						}
						else
						{
							W.setArrestingOfficer(myArea,officer);
							CMLib.commands().postSay(W.arrestingOfficer(),W.criminal(),L("You are headed to the pound for @x1!",restOfCharges(laws,W.criminal())),false,false);
							W.setState(Law.STATE_ARRESTING);
						}
					}
				}
				else
				if(W.crime().equalsIgnoreCase("pardoned"))
				{
					fileAllWarrants(laws,W,W.criminal());
					unCuff(W.criminal());
					W.setArrestingOfficer(myArea,null);
				}
				break;
			}
		case Law.STATE_ARRESTING:
			{
				W.setTravelAttemptTime(0);
				if((officer!=null)
				&&(W.criminal().location()!=null)
				&&(W.criminal().location().isInhabitant(officer))
				&&(CMLib.flags().isInTheGame(W.criminal(),true))
				&&(!W.criminal().amDead())
				&&(CMLib.flags().isAliveAwakeMobile(officer,true))
				&&(CMLib.flags().canBeSeenBy(W.criminal(),officer)))
				{
					if(officer.isInCombat())
					{
						if(officer.getVictim()==W.criminal())
						{
							CMLib.commands().postSay(officer,W.criminal(),laws.getMessage(Law.MSG_RESISTFIGHT),false,false);
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
						fileAllWarrants(laws,W,W.criminal());
						unCuff(W.criminal());
						W.setArrestingOfficer(myArea,null);
					}
					else
					{
						haveMobReactToLaw(W.criminal(),officer);
						if(CMLib.flags().isSitting(W.criminal())||CMLib.flags().isSleeping(W.criminal()))
						{
							if(!CMLib.flags().isAnimalIntelligence(W.criminal()))
								CMLib.commands().postSay(officer,W.criminal(),laws.getMessage(Law.MSG_NORESIST),false,false);
							W.setState(Law.STATE_SUBDUEING);
						}
						else
						if((System.currentTimeMillis() - W.getLastStateChangeTime())>(CMath.mul(CMProps.getTickMillis(),1.5))) // only leave this state after they've had enough time.
						{
							W.setState(Law.STATE_SUBDUEING);
							CMLib.commands().postSay(officer,W.criminal(),laws.getMessage(Law.MSG_RESISTWARN),false,false);
						}
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
				if((officer!=null)
				&&(W.criminal().location()!=null)
				&&(W.criminal().location().isInhabitant(officer))
				&&(!W.criminal().amDead())
				&&(CMLib.flags().isInTheGame(W.criminal(),true))
				&&(CMLib.flags().isAliveAwakeMobile(officer,true))
				&&(CMLib.flags().canBeSeenBy(W.criminal(),officer)))
				{
					W.setTravelAttemptTime(0);
					haveMobReactToLaw(W.criminal(),officer);
					if(W.crime().equalsIgnoreCase("pardoned"))
					{
						fileAllWarrants(laws,W,W.criminal());
						unCuff(W.criminal());
						W.setArrestingOfficer(myArea,null);
					}
					else
					if(CMLib.flags().isStanding(W.criminal()))
					{
						if(!W.arrestingOfficer().isInCombat())
							CMLib.commands().postSay(officer,W.criminal(),laws.getMessage(Law.MSG_RESIST),false,false);

						Ability A=CMClass.getAbility("Skill_ArrestingSap");
						if(A!=null)
						{
							final int curPoints=(int)Math.round(CMath.div(W.criminal().curState().getHitPoints(),W.criminal().maxState().getHitPoints())*100.0);
							A.setProficiency(100);
							A.setAbilityCode(10);
							if(!A.invoke(officer,W.criminal(),(curPoints<=25),0))
							{
								A=CMClass.getAbility("Skill_Trip");
								if(A!=null)
								{
									A.setProficiency(100);
									A.setAbilityCode(30);
									if(!A.invoke(officer,W.criminal(),(curPoints<=50),0))
										CMLib.combat().postAttack(officer,W.criminal(),officer.fetchWieldedItem());
								}
							}
						}
					}
					final Ability cuff=W.criminal().fetchEffect("Skill_HandCuff");
					if((CMLib.flags().isSitting(W.criminal())
						||(cuff!=null)
						||(CMLib.flags().isSleeping(W.criminal())))
					&&(!W.criminal().amDead())
					&&(CMLib.flags().isInTheGame(W.criminal(),true)))
					{
						makePeace(officer.location());
						// cuff him!
						final Room destR=findTheJudge(laws,myArea);
						W.setJail(destR);
						if(CMLib.flags().isAnimalIntelligence(W.criminal()))
							W.setState(Law.STATE_JAILING);
						else
						{
							if(W.criminal().isPlayer())
								CMLib.coffeeTables().bump(W.criminal(), CoffeeTableRow.STAT_ARRESTS);
							W.setState(Law.STATE_MOVING);
						}
						if (cuff != null)
						{
							cuff.unInvoke();
							W.criminal().delEffect(cuff);
						}
						Ability A=CMClass.getAbility("Skill_HandCuff");
						if(A!=null)
							A.invoke(officer,W.criminal(),true,0);
						W.criminal().makePeace(true);
						makePeace(officer.location());
						A=W.criminal().fetchEffect("Skill_ArrestingSap");
						if(A!=null)
							A.unInvoke();
						A=W.criminal().fetchEffect("Fighter_Whomp");
						if(A!=null)
							A.unInvoke();
						A=W.criminal().fetchEffect("Skill_Trip");
						if(A!=null)
							A.unInvoke();
						makePeace(officer.location());
						CMLib.commands().postStand(W.criminal(),true, false);
						W.setTravelAttemptTime(System.currentTimeMillis());
						if(startTracking(officer,W.jail()))
							makePeace(officer.location());
						else
						{
							makePeace(officer.location());
							CMLib.commands().postSay(officer,W.criminal(),L("Since there is no judge, you may go."),false,false);
							W.setTravelAttemptTime(0);
							fileAllWarrants(laws,W,W.criminal());
							unCuff(W.criminal());
							if(W.arrestingOfficer()!=null)
								dismissOfficer(W.arrestingOfficer());
						}
					}
					else
					{
						CMLib.commands().postSay(officer,null,L("Hmph."),false,false);
						fileArrestResister(laws,myArea,W);
						W.setTravelAttemptTime(0);
						unCuff(W.criminal());
						W.setArrestingOfficer(myArea,null);
						W.setState(Law.STATE_SEEKING);
					}
				}
				else
				{
					if(officer!=null)
					{
						CMLib.commands().postSay(officer,null,L("Darn."),false,false);
						fileArrestResister(laws,myArea,W);
					}
					W.setTravelAttemptTime(0);
					unCuff(W.criminal());
					W.setArrestingOfficer(myArea,null);
					W.setState(Law.STATE_SEEKING);
				}
			}
			break;
		case Law.STATE_MOVING:
			{
				if((officer!=null)
				&&(W.criminal().location().isInhabitant(officer))
				&&(!W.criminal().amDead())
				&&(CMLib.flags().isInTheGame(W.criminal(),true))
				&&(!W.crime().equalsIgnoreCase("pardoned"))
				&&((W.travelAttemptTime()==0)||((System.currentTimeMillis()-W.travelAttemptTime())<(5*60*1000)))
				&&(CMLib.flags().isAliveAwakeMobile(officer,true)))
				{
					if(W.criminal().curState().getMovement()<50)
						W.criminal().curState().setMovement(50);
					if(officer.curState().getMovement()<50)
						officer.curState().setMovement(50);
					makePeace(officer.location());
					if(officer.isMonster())
						CMLib.commands().postLook(officer,true);
					if(getTheJudgeHere(laws,officer.location())!=null)
						W.setState(Law.STATE_REPORTING);
					else
					if(!CMLib.flags().isTracking(officer))
					{
						if(!startTracking(officer,W.jail()))
						{
							CMLib.commands().postSay(officer,null,L("Now where was that court?."),false,false);
							W.setTravelAttemptTime(0);
							unCuff(W.criminal());
							W.setArrestingOfficer(myArea,null);
							W.setState(Law.STATE_SEEKING);
						}
					}
					else
					if((CMLib.dice().rollPercentage()>75)&&(laws.chitChat().size()>0))
						CMLib.commands().postSay(officer,W.criminal(),laws.chitChat().get(CMLib.dice().roll(1,laws.chitChat().size(),-1)),false,false);
				}
				else
				{
					if(officer!=null)
					{
						debugLogLostConvicts("Officer lost criminal: ",W,officer);
						CMLib.commands().postSay(officer,null,L("Drat! Lost another one!"),false,false);
						fileArrestResister(laws,myArea,W);
					}
					W.setTravelAttemptTime(0);
					unCuff(W.criminal());
					W.setArrestingOfficer(myArea,null);
					W.setState(Law.STATE_SEEKING);
				}
			}
			break;
		case Law.STATE_REPORTING:
			{
				if((officer!=null)
				&&(W.criminal().location().isInhabitant(officer))
				&&(!W.criminal().amDead())
				&&(CMLib.flags().isInTheGame(W.criminal(),true))
				&&(!W.crime().equalsIgnoreCase("pardoned"))
				&&(CMLib.flags().isAliveAwakeMobile(officer,true)))
				{
					final MOB judge=getTheJudgeHere(laws,officer.location());
					if(judge==null)
					{
						final Room destR=findTheJudge(laws,myArea);
						W.setJail(destR);
						W.setState(Law.STATE_MOVING);
						if(!startTracking(officer,W.jail()))
						{
							CMLib.commands().postSay(officer,null,L("Where was that darn court!"),false,false);
							W.setTravelAttemptTime(0);
							unCuff(W.criminal());
							W.setArrestingOfficer(myArea,null);
							W.setState(Law.STATE_SEEKING);
						}
						else
						if(W.criminal().isPlayer())
							CMLib.coffeeTables().bump(W.criminal(), CoffeeTableRow.STAT_ARRESTS);

					}
					else
					if(CMLib.flags().isAliveAwakeMobile(judge,true))
					{
						CMLib.tracking().stopTracking(officer);
						W.setTravelAttemptTime(0);
						final String sirmaam=judge.charStats().SirMadam();
						CMLib.commands().postSay(officer,judge,L("@x1, @x2 has been arrested for @x3.",sirmaam,W.criminal().name(),restOfCharges(laws,W.criminal())),false,false);
						final List<LegalWarrant> warrants=getRelevantWarrants(laws.warrants(),W,W.criminal());
						for(int w2=0;w2<warrants.size();w2++)
						{
							final LegalWarrant W2=warrants.get(w2);
							if(W2.witness()!=null)
								CMLib.commands().postSay(officer,judge,L("The charge of @x1 was witnessed by @x2.",fixCharge(W2),W2.witness().name()),false,false);
						}
						W.setState(Law.STATE_WAITING);
						if((highestCrimeAction(laws,W,W.criminal())==Law.PUNISHMENT_EXECUTE)
						&&(judge.location()!=null))
						{
							final List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.EXECUTIONS, W.criminal());
							for(int i=0;i<channels.size();i++)
								CMLib.commands().postChannel(judge,channels.get(i),L("@x1 is being executed at @x2 for @x3 crimes.",W.criminal().Name(),judge.location().displayText(judge),W.criminal().charStats().hisher()),true);
						}
					}
					else
					{
						CMLib.commands().postSay(officer,W.criminal(),L("I guess court is not in session today."),false,false);
						W.setTravelAttemptTime(0);
						unCuff(W.criminal());
						W.setArrestingOfficer(myArea,null);
						W.setState(Law.STATE_SEEKING);
					}
				}
				else
				{
					if(officer!=null)
					{
						debugLogLostConvicts("Officer can't report criminal: ",W,officer);
						CMLib.commands().postSay(officer,null,L("Wha? Where'd he go?"),false,false);
						fileArrestResister(laws,myArea,W);
					}
					W.setTravelAttemptTime(0);
					unCuff(W.criminal());
					W.setArrestingOfficer(myArea,null);
					W.setState(Law.STATE_SEEKING);
				}
			}
			break;
		case Law.STATE_WAITING:
			{
				if((officer!=null)
				&&(!W.criminal().amDead())
				&&(W.criminal().location().isInhabitant(officer))
				&&(CMLib.flags().isInTheGame(W.criminal(),true))
				&&(!W.crime().equalsIgnoreCase("pardoned"))
				&&(CMLib.flags().isAliveAwakeMobile(officer,true)))
				{
					final MOB judge=getTheJudgeHere(laws,officer.location());
					if(judge==null)
					{
						final Room destR=findTheJudge(laws,myArea);
						W.setJail(destR);
						W.setState(Law.STATE_MOVING);
						if(!startTracking(officer,W.jail()))
						{
							CMLib.commands().postSay(officer,null,L("Where was that darn court?!"),false,false);
							W.setTravelAttemptTime(0);
							unCuff(W.criminal());
							W.setArrestingOfficer(myArea,null);
							W.setState(Law.STATE_SEEKING);
						}
					}
					else
					if(CMLib.flags().isAliveAwakeMobile(judge,true))
					{
						if(judgeMe(laws,judge,officer,W.criminal(),W,myArea,debugging))
						{
							W.setTravelAttemptTime(0);
							unCuff(W.criminal());
							dismissOfficer(officer);
							fileAllWarrants(laws,W,W.criminal());
							unCuff(W.criminal());
							W.setArrestingOfficer(myArea,null);
						}
						// else, still stuff to do
					}
					else
					{
						CMLib.commands().postSay(officer,W.criminal(),L("Court is not in session today."),false,false);
						W.setTravelAttemptTime(0);
						unCuff(W.criminal());
						W.setArrestingOfficer(myArea,null);
						W.setState(Law.STATE_SEEKING);
					}
				}
				else
				{
					if(officer!=null)
					{
						debugLogLostConvicts("Officer can't await criminal: ",W,officer);
						CMLib.commands().postSay(officer,null,L("Wha? Huh?"),false,false);
						fileArrestResister(laws,myArea,W);
					}
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
				if((officer!=null)
				&&(!W.criminal().amDead())
				&&(W.criminal().location().isInhabitant(officer))
				&&(CMLib.flags().isInTheGame(W.criminal(),true))
				&&(CMLib.flags().isAliveAwakeMobile(officer,true))
				&&(!W.crime().equalsIgnoreCase("pardoned"))
				&&(CMLib.flags().canBeSeenBy(W.criminal(),officer)))
				{
					final MOB judge=getTheJudgeHere(laws,officer.location());
					fileAllWarrants(laws,W,W.criminal());
					unCuff(W.criminal());
					if((judge!=null)
					&&(CMLib.flags().isAliveAwakeMobile(judge,true)))
					{
						judge.location().show(judge,W.criminal(),CMMsg.MSG_OK_VISUAL,L("<S-NAME> put(s) <T-NAME> on parole!"));
						if(W.criminal().isPlayer())
							CMLib.coffeeTables().bump(W.criminal(), CoffeeTableRow.STAT_PAROLES);
						final Ability A=CMClass.getAbility("Prisoner");
						A.startTickDown(judge,W.criminal(),W.jailTime());
						W.criminal().recoverPhyStats();
						W.criminal().recoverCharStats();
						CMLib.commands().postSay(judge,W.criminal(),laws.getMessage(Law.MSG_PAROLEDISMISS),false,false);
						dismissOfficer(officer);
						W.setArrestingOfficer(myArea,null);
						W.criminal().tell(L("\n\r\n\r"));
						if(W.criminal().isMonster())
							CMLib.tracking().wanderAway(W.criminal(),false,true);
					}
					else
					{
						CMLib.commands().postSay(officer,null,L("No court today."),false,false);
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
					{
						debugLogLostConvicts("Officer can't parole criminal: ",W,officer);
						CMLib.commands().postSay(officer,null,L("That was weird."),false,false);
					}
					unCuff(W.criminal());
					W.setArrestingOfficer(myArea,null);
					W.setState(Law.STATE_SEEKING);
				}
			}
			break;
		case Law.STATE_JAILING:
			{
				if((officer!=null)
				&&(!W.criminal().amDead())
				&&(W.criminal().location().isInhabitant(officer))
				&&(CMLib.flags().isInTheGame(W.criminal(),true))
				&&(CMLib.flags().isAliveAwakeMobile(officer,true))
				&&(!W.crime().equalsIgnoreCase("pardoned"))
				&&(CMLib.flags().canBeSeenBy(W.criminal(),officer)))
				{
					final Room jail=findTheJail(W.criminal(),myArea,laws);
					if(jail!=null)
					{
						Ability A=W.criminal().fetchEffect("Prisoner");
						if(A!=null)
						{
							A.unInvoke();
							W.criminal().delEffect(A);
						}
						makePeace(officer.location());
						W.setJail(jail);
						// cuff him!
						W.setState(Law.STATE_MOVING2);
						A=CMClass.getAbility("Skill_HandCuff");
						if((A!=null)&&(!CMLib.flags().isBoundOrHeld(W.criminal())))
							A.invoke(officer,W.criminal(),true,0);
						W.criminal().makePeace(true);
						makePeace(officer.location());
						CMLib.commands().postStand(W.criminal(),true, false);
						CMLib.tracking().stopTracking(officer);
						A=CMClass.getAbility("Skill_Track");
						if(A!=null)
						{
							W.setTravelAttemptTime(System.currentTimeMillis());
							A.setAbilityCode(1);
							A.invoke(officer,CMParms.parse(CMLib.map().getExtendedRoomID(jail)+trackingModifiers(officer)),jail,true,0);
						}
						if(officer.fetchEffect("Skill_Track")==null)
						{
							W.setTravelAttemptTime(0);
							fileAllWarrants(laws,W,W.criminal());
							unCuff(W.criminal());
							CMLib.commands().postSay(officer,W.criminal(),L("I can't find the jail, you are free to go."),false,false);
							dismissOfficer(officer);
							W.setArrestingOfficer(myArea,null);
						}
						makePeace(officer.location());
					}
					else
					{
						W.setTravelAttemptTime(0);
						fileAllWarrants(laws,W,W.criminal());
						unCuff(W.criminal());
						CMLib.commands().postSay(W.arrestingOfficer(),W.criminal(),L("But since there IS no jail, I will let you go."),false,false);
						dismissOfficer(officer);
						W.setArrestingOfficer(myArea,null);
					}
				}
				else
				{
					if(officer!=null)
					{
						debugLogLostConvicts("Officer can't jail criminal: ",W,officer);
						CMLib.commands().postSay(officer,null,L("Crazy."),false,false);
						fileArrestResister(laws,myArea,W);
					}
					unCuff(W.criminal());
					W.setArrestingOfficer(myArea,null);
					W.setState(Law.STATE_SEEKING);
					W.setTravelAttemptTime(0);
				}
			}
			break;
		case Law.STATE_DETAINING:
			{
				if((officer!=null)
				&&(!W.criminal().amDead())
				&&(W.criminal().location().isInhabitant(officer))
				&&(CMLib.flags().isInTheGame(W.criminal(),true))
				&&(CMLib.flags().isAliveAwakeMobile(officer,true))
				&&(!W.crime().equalsIgnoreCase("pardoned"))
				&&(CMLib.flags().canBeSeenBy(W.criminal(),officer)))
				{
					final Room jail=findTheDetentionCenter(W.criminal(),myArea,laws,W);
					final int time=getDetainTime(laws,W,W.criminal());
					if((jail!=null)&&(time>=0))
					{
						Ability A=W.criminal().fetchEffect("Prisoner");
						if(A!=null)
						{
							A.unInvoke();
							W.criminal().delEffect(A);
						}

						makePeace(officer.location());
						W.setJail(jail);
						W.setJailTime(time);
						// cuff him!
						W.setState(Law.STATE_MOVING3);
						A=CMClass.getAbility("Skill_HandCuff");
						W.criminal().basePhyStats().setDisposition(W.criminal().basePhyStats().disposition()|PhyStats.IS_SITTING);
						W.criminal().phyStats().setDisposition(W.criminal().phyStats().disposition()|PhyStats.IS_SITTING);
						if((A!=null)&&(!CMLib.flags().isBoundOrHeld(W.criminal())))
							A.invoke(officer,W.criminal(),true,0);
						W.criminal().makePeace(true);
						makePeace(officer.location());
						CMLib.commands().postStand(W.criminal(),true, false);
						CMLib.tracking().stopTracking(officer);
						A=CMClass.getAbility("Skill_Track");
						if(A!=null)
						{
							W.setTravelAttemptTime(System.currentTimeMillis());
							A.setAbilityCode(1);
							A.invoke(officer,CMParms.parse(CMLib.map().getExtendedRoomID(jail)+trackingModifiers(officer)),jail,true,0);
						}
						if(officer.fetchEffect("Skill_Track")==null)
						{
							W.setTravelAttemptTime(0);
							fileAllWarrants(laws,W,W.criminal());
							unCuff(W.criminal());
							CMLib.commands().postSay(officer,W.criminal(),L("I can't find the detention center, you are free to go."),false,false);
							dismissOfficer(officer);
							W.setArrestingOfficer(myArea,null);
						}
						makePeace(officer.location());
					}
					else
					{
						W.setTravelAttemptTime(0);
						fileAllWarrants(laws,W,W.criminal());
						unCuff(W.criminal());
						CMLib.commands().postSay(W.arrestingOfficer(),W.criminal(),L("But since there IS no detention center, I will let you go."),false,false);
						dismissOfficer(officer);
						W.setArrestingOfficer(myArea,null);
					}
				}
				else
				{
					if(officer!=null)
					{
						debugLogLostConvicts("Officer can't detain criminal: ",W,officer);
						CMLib.commands().postSay(officer,null,L("Sad."),false,false);
						fileArrestResister(laws,myArea,W);
						dismissOfficer(officer);
					}
					W.setTravelAttemptTime(0);
					fileAllWarrants(laws,W,W.criminal());
					unCuff(W.criminal());
					W.setArrestingOfficer(myArea,null);
					W.setState(Law.STATE_SEEKING);
				}
			}
			break;
		case Law.STATE_EXECUTING:
			{
				final MOB criminalM=W.criminal();
				if((officer!=null)
				&&(criminalM!=null)
				&&(CMLib.flags().isInTheGame(criminalM,true))
				&&(!criminalM.amDead())
				&&(criminalM.location().isInhabitant(officer))
				&&(CMLib.flags().isAliveAwakeMobile(officer,true))
				&&(!W.crime().equalsIgnoreCase("pardoned"))
				&&(CMLib.flags().canBeSeenBy(criminalM,officer))
				&&(canFocusOn(officer,criminalM)))
				{
					final MOB judgeM=getTheJudgeHere(laws,officer.location());
					if((judgeM!=null)
					&&(CMLib.flags().isAliveAwakeMobile(judgeM,true))
					&&(judgeM.location()==criminalM.location()))
					{
						dismissOfficer(officer);
						CMLib.commands().postFollow(criminalM, null, true);
						for(final Enumeration<Pair<MOB,Short>> f=criminalM.followers();f.hasMoreElements();)
						{
							final Pair<MOB,Short> F=f.nextElement();
							CMLib.commands().postFollow(F.first, null, true);
						}
						Ability A=CMClass.getAbility("Prisoner");
						A.startTickDown(judgeM,criminalM,100);
						A=judgeM.fetchAbility("Fighter_Behead");
						if(A==null)
							A=judgeM.fetchAbility("Prayer_Stoning");
						boolean served=false;
						if(A!=null)
						{
							A.setProficiency(100);
							served=A.invoke(judgeM,criminalM,false,0);
						}
						if(criminalM.isPlayer())
							CMLib.coffeeTables().bump(criminalM, CoffeeTableRow.STAT_EXECUTIONS);
						fileAllWarrants(laws,null,criminalM);
						criminalM.recoverPhyStats();
						criminalM.recoverCharStats();
						if(!served)
							CMLib.combat().postAttack(judgeM,criminalM,judgeM.fetchWieldedItem());
						else
						if(CMath.bset(W.punishmentCode(),Law.PUNISHMENTMASK_BANISH))
						{
							W.setState(Law.STATE_BANISHING);
							break;
						}
						else
						{
							W.setArrestingOfficer(myArea,null);
							W.setTravelAttemptTime(0);
							unCuff(criminalM);
						}
					}
					else
					{
						CMLib.commands().postSay(officer,null,L("Looks like court is not in session."),false,false);
						W.setTravelAttemptTime(0);
						unCuff(criminalM);
						if(W.arrestingOfficer()!=null)
							dismissOfficer(W.arrestingOfficer());
						W.setArrestingOfficer(myArea,null);
						W.setState(Law.STATE_SEEKING);
					}
				}
				else
				{
					if(officer!=null)
					{
						debugLogLostConvicts("Officer can't execute criminal: ",W,officer);
						CMLib.commands().postSay(officer,null,L("Didn't see that coming."),false,false);
						fileArrestResister(laws,myArea,W);
					}
					W.setTravelAttemptTime(0);
					unCuff(criminalM);
					W.setArrestingOfficer(myArea,null);
					W.setState(Law.STATE_SEEKING);
				}
			}
			break;
		case Law.STATE_MOVING2:
			{
				if((officer!=null)
				&&(!W.criminal().amDead())
				&&(W.criminal().location().isInhabitant(officer))
				&&(CMLib.flags().isInTheGame(W.criminal(),true))
				&&((W.travelAttemptTime()==0)||((System.currentTimeMillis()-W.travelAttemptTime())<(5*60*1000)))
				&&(CMLib.flags().isAliveAwakeMobile(officer,true))
				&&(W.jail()!=null))
				{
					if(W.criminal().curState().getMovement()<50)
						W.criminal().curState().setMovement(50);
					if(officer.curState().getMovement()<50)
						officer.curState().setMovement(50);
					makePeace(officer.location());
					if(officer.isMonster())
						CMLib.commands().postLook(officer,true);
					if(W.jail()==W.criminal().location())
					{
						if(W.criminal().isPlayer())
							CMLib.coffeeTables().bump(W.criminal(), CoffeeTableRow.STAT_JAILINGS);
						unCuff(W.criminal());
						final Ability A=CMClass.getAbility("Prisoner");
						if(A!=null)
							A.startTickDown(officer,W.criminal(),W.jailTime()+5);
						W.criminal().recoverPhyStats();
						W.criminal().recoverCharStats();
						dismissOfficer(officer);
						if(W.criminal().fetchEffect("Prisoner")==null)
						{
							fileAllWarrants(laws,W,W.criminal());
							if(isJailRoom(myArea, new XVector<Room>(W.jail())))
								unCuff(W.criminal());
						}
						else
							W.setState(Law.STATE_RELEASE);
					}
					else
					if(!CMLib.flags().isTracking(officer))
					{
						final Ability A=CMClass.getAbility("Skill_Track");
						if(A!=null)
						{
							CMLib.tracking().stopTracking(officer);
							A.setAbilityCode(1); // tells track to cache the path
							A.invoke(officer,CMParms.parse(CMLib.map().getExtendedRoomID(W.jail())+trackingModifiers(officer)),W.jail(),true,0);
						}
						if(officer.fetchEffect("Skill_Track")==null)
						{
							W.setTravelAttemptTime(0);
							fileAllWarrants(laws,W,W.criminal());
							unCuff(W.criminal());
							CMLib.commands().postSay(officer,W.criminal(),L("I lost the jail, so you are free to go."),false,false);
							dismissOfficer(officer);
							W.setArrestingOfficer(myArea,null);
						}
					}
					else
					if((CMLib.dice().rollPercentage()>75)&&(laws.chitChat2().size()>0))
						CMLib.commands().postSay(officer,W.criminal(),laws.chitChat2().get(CMLib.dice().roll(1,laws.chitChat2().size(),-1)),false,false);
				}
				else
				{
					debugLogLostConvicts("Officer can't move2 criminal: ",W,officer);
					fileArrestResister(laws,myArea,W);
					unCuff(W.criminal());
					W.setArrestingOfficer(myArea,null);
					W.setState(Law.STATE_SEEKING);
					W.setTravelAttemptTime(0);
				}
			}
			break;
		case Law.STATE_MOVING3:
		case Law.STATE_MOVING4:
			{
				if((officer!=null)
				&&(!W.criminal().amDead())
				&&(W.criminal().location().isInhabitant(officer))
				&&(CMLib.flags().isInTheGame(W.criminal(),true))
				&&((W.travelAttemptTime()==0)||((System.currentTimeMillis()-W.travelAttemptTime())<(5*60*1000)))
				&&(CMLib.flags().isAliveAwakeMobile(officer,true))
				&&(W.jail()!=null))
				{
					if(W.criminal().curState().getMovement()<50)
						W.criminal().curState().setMovement(50);
					if(officer.curState().getMovement()<50)
						officer.curState().setMovement(50);
					makePeace(officer.location());
					if(officer.isMonster())
						CMLib.commands().postLook(officer,true);
					if(W.crime().equalsIgnoreCase("pardoned"))
					{
						fileAllWarrants(laws,W,W.criminal());
						unCuff(W.criminal());
						W.setArrestingOfficer(myArea,null);
						W.setState(Law.STATE_SEEKING);
						W.setTravelAttemptTime(0);
					}
					else
					if(W.jail()==W.criminal().location())
					{
						unCuff(W.criminal());
						if(W.state() == Law.STATE_MOVING3)
						{
							final Ability A=CMClass.getAbility("Prisoner");
							if(A!=null)
								A.startTickDown(officer,W.criminal(),W.jailTime()+5);
							W.criminal().recoverPhyStats();
							W.criminal().recoverCharStats();
							dismissOfficer(officer);
							if(W.criminal().fetchEffect("Prisoner")==null)
							{
								fileAllWarrants(laws,W,W.criminal());
								unCuff(W.criminal());
							}
							else
								W.setState(Law.STATE_RELEASE);
						}
						else
						{
							W.setArrestingOfficer(myArea,null);
							W.setState(Law.STATE_SEEKING);
							W.setTravelAttemptTime(0);
							fileAllWarrants(laws,W,W.criminal());
							unCuff(W.criminal());
						}
					}
					else
					if(CMLib.flags().flaggedAffects(officer,Ability.FLAG_TRACKING).size()==0)
					{
						final Ability A=CMClass.getAbility("Skill_Track");
						if(A!=null)
						{
							CMLib.tracking().stopTracking(officer);
							A.setAbilityCode(1); // tells track to cache the path
							A.invoke(officer,CMParms.parse(CMLib.map().getExtendedRoomID(W.jail())+trackingModifiers(officer)),W.jail(),true,0);
						}
						if(officer.fetchEffect("Skill_Track")==null)
						{
							W.setTravelAttemptTime(0);
							fileAllWarrants(laws,W,W.criminal());
							unCuff(W.criminal());
							CMLib.commands().postSay(officer,W.criminal(),L("I lost my way, so you are free to go."),false,false);
							dismissOfficer(officer);
							W.setArrestingOfficer(myArea,null);
						}
					}
					else
					{
						final List<String> chitChat = (W.state() == Law.STATE_MOVING3) ? laws.chitChat3() : laws.chitChat4();
						if((CMLib.dice().rollPercentage()>75)&&(chitChat.size()>0))
							CMLib.commands().postSay(officer,W.criminal(),chitChat.get(CMLib.dice().roll(1,chitChat.size(),-1)),false,false);
					}
				}
				else
				{
					debugLogLostConvicts("Officer can't move("+W.state()+") criminal: ",W,officer);
					fileArrestResister(laws,myArea,W);
					unCuff(W.criminal());
					W.setArrestingOfficer(myArea,null);
					W.setState(Law.STATE_SEEKING);
					W.setTravelAttemptTime(0);
					fileAllWarrants(laws,W,W.criminal());
				}
			}
			break;
		case Law.STATE_BANISHING:
			{
				if((officer!=null)
				&&(!W.criminal().amDead())
				&&(W.criminal().location().isInhabitant(officer))
				&&(CMLib.flags().isInTheGame(W.criminal(),true))
				&&(CMLib.flags().isAliveAwakeMobile(officer,true))
				&&(!W.crime().equalsIgnoreCase("pardoned"))
				&&(CMLib.flags().canBeSeenBy(W.criminal(),officer)))
				{
					final Room banishR=findTheBanishingPoint(W.criminal(),myArea,laws,W);
					if(banishR!=null)
					{
						Ability A=W.criminal().fetchEffect("Prisoner");
						if(A!=null)
						{
							A.unInvoke();
							W.criminal().delEffect(A);
						}
						makePeace(officer.location());
						W.setJail(banishR);
						// cuff him!
						W.setState(Law.STATE_MOVING4);
						A=CMClass.getAbility("Skill_HandCuff");
						if((A!=null)
						&&(!CMLib.flags().isBoundOrHeld(W.criminal())))
							A.invoke(officer,W.criminal(),true,0);
						W.criminal().makePeace(true);
						makePeace(officer.location());
						CMLib.commands().postStand(W.criminal(),true, false);
						CMLib.tracking().stopTracking(officer);
						A=CMClass.getAbility("Skill_Track");
						if(A!=null)
						{
							W.setTravelAttemptTime(System.currentTimeMillis());
							A.setAbilityCode(1);
							A.invoke(officer,CMParms.parse(CMLib.map().getExtendedRoomID(banishR)+trackingModifiers(officer)),banishR,true,0);
						}
						if(officer.fetchEffect("Skill_Track")==null)
						{
							W.setTravelAttemptTime(0);
							fileAllWarrants(laws,W,W.criminal());
							unCuff(W.criminal());
							CMLib.commands().postSay(officer,W.criminal(),L("I can't find the way out of here, you are free to go."),false,false);
							dismissOfficer(officer);
							W.setArrestingOfficer(myArea,null);
						}
						makePeace(officer.location());
					}
					else
					{
						W.setTravelAttemptTime(0);
						fileAllWarrants(laws,W,W.criminal());
						unCuff(W.criminal());
						CMLib.commands().postSay(W.arrestingOfficer(),W.criminal(),L("But since there IS no way out, I will let you go."),false,false);
						dismissOfficer(officer);
						W.setArrestingOfficer(myArea,null);
					}
				}
				else
				{
					if(officer!=null)
					{
						debugLogLostConvicts("Officer can't banish criminal: ",W,officer);
						CMLib.commands().postSay(officer,null,L("Crazy."),false,false);
						fileArrestResister(laws,myArea,W);
					}
					unCuff(W.criminal());
					W.setArrestingOfficer(myArea,null);
					W.setState(Law.STATE_SEEKING);
					W.setTravelAttemptTime(0);
				}
			}
			break;
		case Law.STATE_RELEASE:
			{
				if(((W.criminal().fetchEffect("Prisoner")==null)
					||(W.crime().equalsIgnoreCase("pardoned")))
				&&(!W.criminal().amDead())
				&&(W.jail()!=null)
				&&(CMLib.flags().isInTheGame(W.criminal(), false)))
				{
					final Ability P=W.criminal().fetchEffect("Prisoner");
					if(P!=null)
						P.unInvoke();
					if(CMath.bset(highestCrimeAction(laws,W,W.criminal()),Law.PUNISHMENTMASK_NORELEASE))
					{
						W.setTravelAttemptTime(0);
						fileAllWarrants(laws,W,W.criminal());
						unCuff(W.criminal());
						W.setArrestingOfficer(myArea,null);
					}
					else
					if(W.criminal().location()==W.jail())
					{
						if((officer==null)
						||(!CMLib.flags().isAliveAwakeMobile(officer,true))
						||(W.criminal().amDead())
						||(!CMLib.flags().isInTheGame(W.criminal(),true))
						||(!W.criminal().location().isInhabitant(officer)))
						{
							W.setArrestingOfficer(myArea,getAnyEligibleOfficer(laws,W.jail().getArea(),W.criminal(),W.victim()));
							if(W.arrestingOfficer()==null)
								W.setArrestingOfficer(myArea,getAnyEligibleOfficer(laws,myArea,W.criminal(),W.victim()));
							if(W.arrestingOfficer()==null)
								break;
							officer=W.arrestingOfficer();
							W.jail().bringMobHere(officer,false);
							if(!canFocusOn(officer,W.criminal()))
							{
								W.jail().show(officer,W.criminal(),CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> arrive(s) to release <T-NAME>, but can't find <T-HIM-HER>."));
								dismissOfficer(officer);
								W.setArrestingOfficer(myArea,null);
							}
							else
								W.jail().show(officer,W.criminal(),CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> arrive(s) to release <T-NAME>."));
							final Ability A=CMClass.getAbility("Skill_HandCuff");
							if((A!=null)&&(!CMLib.flags().isBoundOrHeld(W.criminal())))
								A.invoke(officer,W.criminal(),true,0);
						}
						W.setReleaseRoom(getReleaseRoom(laws,myArea,W.criminal(),W));
						W.criminal().makePeace(true);
						makePeace(officer.location());
						CMLib.tracking().stopTracking(officer);
						final Ability A=CMClass.getAbility("Skill_Track");
						if(A!=null)
						{
							W.setTravelAttemptTime(System.currentTimeMillis());
							A.invoke(officer,CMParms.parse(CMLib.map().getExtendedRoomID(W.releaseRoom())+trackingModifiers(officer)),W.releaseRoom(),true,0);
						}
						if(officer.fetchEffect("Skill_Track")==null)
						{
							W.setTravelAttemptTime(0);
							fileAllWarrants(laws,W,W.criminal());
							unCuff(W.criminal());
							CMLib.commands().postSay(officer,W.criminal(),L("Well, you can always recall."),false,false);
							dismissOfficer(officer);
							W.setArrestingOfficer(myArea,null);
						}
					}
					else
					if(W.releaseRoom()!=null)
					{
						if(W.criminal().location()==W.releaseRoom())
						{
							if(CMath.bset(W.punishmentCode(),Law.PUNISHMENTMASK_BANISH))
							{
								W.setState(Law.STATE_BANISHING);
								break;
							}
							fileAllWarrants(laws,W,W.criminal());
							unCuff(W.criminal());

							if(officer!=null)
							{
								if((CMLib.flags().isAliveAwakeMobile(officer,true))
								&&(W.criminal().location().isInhabitant(officer)))
									CMLib.commands().postSay(officer,null,laws.getMessage(Law.MSG_LAWFREE),false,false);
								dismissOfficer(officer);
							}
							W.setTravelAttemptTime(0);
						}
						else
						{
							if((officer!=null)
							&&(CMLib.flags().isAliveAwakeMobile(officer,true))
							&&(W.criminal().location().isInhabitant(officer))
							&&((W.travelAttemptTime()==0)||((System.currentTimeMillis()-W.travelAttemptTime())<(5*60*1000))))
							{
								if(officer.isMonster())
									CMLib.commands().postLook(officer,true);
								if(W.criminal().curState().getMovement()<20)
									W.criminal().curState().setMovement(20);
								if(officer.curState().getMovement()<20)
									officer.curState().setMovement(20);
								if(W.arrestingOfficer().fetchEffect("Skill_Track")==null)
								{
									CMLib.tracking().stopTracking(officer);
									final Ability A=CMClass.getAbility("Skill_Track");
									if(A!=null)
										A.invoke(officer,CMParms.parse(CMLib.map().getExtendedRoomID(W.releaseRoom())+trackingModifiers(officer)),W.releaseRoom(),true,0);
									if(W.arrestingOfficer().fetchEffect("Skill_Track")==null)
									{
										W.setTravelAttemptTime(0);
										fileAllWarrants(laws,W,W.criminal());
										unCuff(W.criminal());
										final Ability pA=W.criminal().fetchEffect("Prisoner");
										if(pA!=null)
										{
											pA.unInvoke();
											W.criminal().delEffect(pA);
										}
										CMLib.commands().postSay(W.arrestingOfficer(),W.criminal(),L("Don't worry, you can always recall."),false,false);
										dismissOfficer(W.arrestingOfficer());
										W.setArrestingOfficer(myArea,null);
									}
								}
							}
							else
							{
								if(officer!=null)
								{
									debugLogLostConvicts("Officer can't release criminal: ",W,officer);
									CMLib.commands().postSay(officer,null,L("There's always recall."),false,false);
								}
								W.setTravelAttemptTime(0);
								final Ability pA=W.criminal().fetchEffect("Prisoner");
								if(pA!=null)
								{
									pA.unInvoke();
									W.criminal().delEffect(pA);
								}
								fileAllWarrants(laws,W,W.criminal());
								unCuff(W.criminal());
								if(officer!=null)
									dismissOfficer(officer);
							}
						}
					}
					else
					{
						if(W.arrestingOfficer()!=null)
						{
							debugLogLostConvicts("Officer can't release2 criminal: ",W,W.arrestingOfficer());
							CMLib.commands().postSay(W.arrestingOfficer(),null,L("Well, the prisoner can always recall."),false,false);
						}
						W.setTravelAttemptTime(0);
						final Ability pA=W.criminal().fetchEffect("Prisoner");
						if(pA!=null)
						{
							pA.unInvoke();
							W.criminal().delEffect(pA);
						}
						fileAllWarrants(laws,W,W.criminal());
						unCuff(W.criminal());
						if(W.arrestingOfficer()!=null)
							dismissOfficer(W.arrestingOfficer());
					}
				}
				else
				if(((W.criminal().fetchEffect("Prisoner")!=null)
					&&(!W.crime().equalsIgnoreCase("pardoned")))
				&&(!W.criminal().amDead())
				&&(W.jail()!=null)
				&&(!W.criminal().amDead())
				&&(!W.jail().isInhabitant(W.criminal()))
				&&(CMLib.flags().isInTheGame(W.criminal(), false))
				&&(laws.basicCrimes().containsKey("PRISONBREAK")))
				{
					final Room criminR = W.criminal().location();
					if(isJailRoom(myArea, criminR))
						W.setJail(criminR);
					else
					{
						W.setState(Law.STATE_SEEKING);
						W.setTravelAttemptTime(0);
						final Ability A=W.criminal().fetchEffect("Prisoner");
						if(A!=null)
						{
							A.unInvoke();
							W.criminal().delEffect(A);
						}
						final String[] info=laws.basicCrimes().get("PRISONBREAK");
						fillOutWarrant(W.criminal(),
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
			}
			break;
		default:
			Log.warnOut("Unknown Arrest state: "+W.state());
			break;
		}
	}

	@Override
	public void release(final Area myArea, final LegalWarrant warrant)
	{
		final long delay = CMProps.getTickMillis() * 5;
		switch(warrant.state())
		{
		case Law.STATE_SEEKING:
			warrant.setIgnoreUntilTime(System.currentTimeMillis() + delay);
			warrant.setArrestingOfficer(myArea, null);
			break;
		case Law.STATE_RELEASE:
			// this is a good thing!
			break;
		case Law.STATE_ARRESTING:
		case Law.STATE_SUBDUEING:
		case Law.STATE_MOVING:
		case Law.STATE_REPORTING:
		case Law.STATE_WAITING:
		case Law.STATE_PAROLING:
		case Law.STATE_JAILING:
		case Law.STATE_EXECUTING:
		case Law.STATE_MOVING3:
		case Law.STATE_MOVING4:
		case Law.STATE_DETAINING:
			this.unCuff(warrant.criminal());
			final MOB officer=warrant.arrestingOfficer();
			if(officer!=null)
			{
				if(officer.getVictim()==warrant.criminal())
					officer.makePeace(true);
				if(warrant.criminal()==officer)
					warrant.criminal().makePeace(true);
			}
			warrant.setArrestingOfficer(myArea, null);
			warrant.setIgnoreUntilTime(System.currentTimeMillis() + delay);
			break;
		case Law.STATE_MOVING2:
			// this is a good thing!
			break;
		}
	}
}
