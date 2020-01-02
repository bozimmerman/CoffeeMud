package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.threads.ServiceEngine;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.database.DBConnections;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.AccountStats.Agent;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.Function;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.Authority;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.ClanVote;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.MemberRecord;
import com.planet_ink.coffee_mud.Common.interfaces.PlayerAccount.AccountFlag;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.AbilityMapping;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Achievement;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.AchievementLoadFlag;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Award;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Tracker;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.PlayerData;
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.ForumJournal;
import com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary.ThinPlayer;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;

/**
 * Portions Copyright (c) 2003 Jeremy Vyska
 * Portions Copyright (c) 2004-2020 Bo Zimmerman
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class DefaultClan implements Clan
{
	@Override
	public String ID()
	{
		return "DefaultClan";
	}

	private final int	tickStatus	= Tickable.STATUS_NOT;

	@Override
	public int getTickStatus()
	{
		return tickStatus;
	}

	protected String				clanName				= "";
	protected String				clanCategory			= null;
	protected String				clanPremise				= "";
	protected String				clanRecall				= "";
	protected String				clanMorgue				= "";
	protected String				clanClass				= "";
	protected int					clanLevel				= 0;
	protected String				clanDonationRoom		= "";
	protected int					clanTrophies			= 0;
	protected Boolean				isRivalrous				= null;
	protected int					autoPosition			= -1;
	protected String				acceptanceSettings		= "";
	protected int					clanStatus				= 0;
	protected long					lastStatusChange		= 0;
	protected String				lastClanKillLog			= null;
	protected double				taxRate					= 0.0;
	protected volatile long			exp						= 0;
	protected volatile long			lastClanTickMs			= System.currentTimeMillis();
	protected Object				expSync					= new Object();
	protected List<ClanVote>		voteList				= null;
	protected List<Long>			clanKills				= new Vector<Long>();
	protected Integer				overrideMinClanMembers	= null;
	protected long					lastPropsReload			= System.currentTimeMillis();
	protected ItemCollection		extItems				= (ItemCollection) CMClass.getCommon("WeakItemCollection");
	protected Map<String, long[]>	relations				= new Hashtable<String, long[]>();
	protected int					government				= 0;
	protected long					lastGovernmentLoadTime	= -1;
	protected ClanGovernment		govt					= null;
	protected long					totalOnlineMins			= 0;
	protected long					totalLevelsGained		= 0;
	protected int					monthOnlineMins			= 0;
	protected int					monthPlayerXP			= 0;
	protected int					monthClanXP				= 0;
	protected int					monthConquered			= 0;
	protected int					monthClanLevels			= 0;
	protected int					monthControlPoints		= 0;
	protected int					monthNewMembers			= 0;
	protected volatile int			tickUp					= 0;
	protected volatile int			transientSize			= -1;
	protected Set<ClanFlag>			clanFlags				= new SHashSet<ClanFlag>();

	protected final static List<Ability> empty=new XVector<Ability>(1,true);

	protected final List<Pair<Clan, Integer>>	channelSet		= new XVector<Pair<Clan, Integer>>(1, true);
	protected CMUniqNameSortSVec<Tattoo>		tattoos			= new CMUniqNameSortSVec<Tattoo>(1);
	protected Map<String, Tracker>				achievementers	= new STreeMap<String, Tracker>();

	/** return a new instance of the object*/
	@Override
	public CMObject newInstance()
	{
		try
		{
			return getClass().newInstance();
		}
		catch (final Exception e)
		{
			return new DefaultClan();
		}
	}

	@Override
	public void initializeClass()
	{
	}

	@Override
	public int compareTo(final CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	@Override
	public CMObject copyOf()
	{
		try
		{
			final DefaultClan C=(DefaultClan)this.clone();
			C.extItems=(ItemCollection)extItems.copyOf();
			C.clanFlags = new SHashSet<ClanFlag>(clanFlags);
			return C;
		}
		catch(final CloneNotSupportedException e)
		{
			return new DefaultClan();
		}
	}

	@Override
	public ClanGovernment getGovernment()
	{
		return govt();
	}

	protected ClanGovernment govt()
	{
		if((govt != null) && ((government < 0) || (lastGovernmentLoadTime == CMLib.clans().getLastGovernmentLoad())))
			return govt;
		else
		{

			ClanGovernment govt = CMLib.clans().getStockGovernment(government);
			if(govt == null)
			{
				govt = CMLib.clans().getDefaultGovernment();
				government = govt.getID();
			}
			lastGovernmentLoadTime = CMLib.clans().getLastGovernmentLoad();
			return govt;
		}
	}

	private synchronized void clanKills()
	{
		if(lastClanKillLog==null)
		{
			final List<PlayerData> V=CMLib.database().DBReadPlayerData(clanID(),"CLANKILLS",clanID()+"/CLANKILLS");
			clanKills.clear();
			if(V.size()==0)
				lastClanKillLog="";
			else
			{
				lastClanKillLog=V.get(0).xml();
				final List<String> V2=CMParms.parseSemicolons(lastClanKillLog,true);
				for(int v=0;v<V2.size();v++)
					clanKills.add(Long.valueOf(CMath.s_long(V2.get(v))));
			}
		}
	}

	private void updateClanKills()
	{
		Long date=null;
		final StringBuffer str=new StringBuffer("");
		final long now=System.currentTimeMillis();
		for(int i=clanKills.size()-1;i>=0;i--)
		{
			date=clanKills.get(i);
			if(date.longValue()<now)
				clanKills.remove(i);
			else
				str.append(date.longValue()).append(";");
		}
		if((lastClanKillLog==null)||(!lastClanKillLog.equals(str.toString())))
		{
			lastClanKillLog=str.toString();
			CMLib.database().DBReCreatePlayerData(clanID(),"CLANKILLS",clanID()+"/CLANKILLS",str.toString());
		}
	}

	@Override
	public void resetMonthlyTrophyData()
	{
		monthOnlineMins = 0;
		monthPlayerXP = 0;
		monthClanXP = 0;
		monthConquered = 0;
		monthClanLevels = 0;
		monthControlPoints = 0;
		monthNewMembers = 0;
	}

	@Override
	public long getTrophyData(final Trophy trophy)
	{
		switch(trophy)
		{
		case MonthlyPlayerMinutes:
			return monthOnlineMins;
		case MonthlyPlayerXP:
			return monthPlayerXP;
		case MonthlyClanXP:
			return monthClanXP;
		case MonthlyConquests:
			return monthConquered;
		case MonthlyClanLevels:
			return monthClanLevels;
		case MonthlyControlPoints:
			return monthControlPoints;
		case MonthlyNewMembers:
			return monthNewMembers;
		case Points:
			return calculateMapPoints();
		case Experience:
			return exp;
		case  Areas:
			return getControlledAreas().size();
		case ClanKills:
			return getCurrentClanKills(null);
		case Members:
			return getSize();
		case MemberLevel:
			return filterMedianLevel(getFullMemberList());
		case PlayerMinutes:
			return totalOnlineMins;
		case PlayerLevelsGained:
			return totalLevelsGained;
		default:
			return 0;
		}
	}

	@Override
	public void bumpTrophyData(final Trophy trophy, final int amt)
	{
		switch(trophy)
		{
		case MonthlyPlayerMinutes:
			monthOnlineMins += amt;
			break;
		case MonthlyPlayerXP:
			monthPlayerXP += amt;
			break;
		case MonthlyClanXP:
			monthClanXP += amt;
			break;
		case MonthlyConquests:
			monthConquered += amt;
			break;
		case MonthlyClanLevels:
			monthClanLevels += amt;
			break;
		case MonthlyControlPoints:
			monthControlPoints += amt;
			break;
		case MonthlyNewMembers:
			monthNewMembers += amt;
			break;
		case Points:
			// derived
			break;
		case Experience:
			this.exp += amt;
			break;
		case  Areas:
			// derived
			break;
		case ClanKills:
			// derived from member records
			break;
		case Members:
			// derived from member records
			break;
		case  MemberLevel:
			// derived from mob records of member records
			break;
		case PlayerMinutes:
			totalOnlineMins += amt;
			break;
		case PlayerLevelsGained:
			totalLevelsGained += amt;
			break;
		}
	}

	@Override
	public void updateVotes()
	{
		final XMLLibrary xml=CMLib.xml();
		final StringBuilder str=new StringBuilder("");
		for(final Enumeration<ClanVote> e=votes();e.hasMoreElements();)
		{
			final ClanVote CV=e.nextElement();
			str.append(xml.convertXMLtoTag("BY",CV.voteStarter));
			str.append(xml.convertXMLtoTag("FUNC",CV.function));
			str.append(xml.convertXMLtoTag("ON",""+CV.voteStarted));
			str.append(xml.convertXMLtoTag("STATUS",""+CV.voteStatus));
			str.append(xml.convertXMLtoTag("CMD",CV.matter));
			if((CV.votes!=null)&&(CV.votes.size()>0))
			{
				str.append("<VOTES>");
				for(int v=0;v<CV.votes.size();v++)
				{
					str.append("<VOTE>");
					str.append(xml.convertXMLtoTag("BY",CV.votes.getFirst(v)));
					str.append(xml.convertXMLtoTag("YN",CV.votes.getSecond(v).toString()));
					str.append("</VOTE>");
				}
				str.append("</VOTES>");
			}
		}
		if(str.length()>0)
			CMLib.database().DBReCreatePlayerData(clanID(),"CLANVOTES",clanID()+"/CLANVOTES","<BALLOTS>"+str.toString()+"</BALLOTS>");
		else
			CMLib.database().DBDeletePlayerData(clanID(),"CLANVOTES",clanID()+"/CLANVOTES");
	}

	@Override
	public void addVote(final ClanVote CV)
	{
		if(CV==null)
			return;
		votes();
		voteList.add(CV);
	}

	@Override
	public void delVote(final ClanVote CV)
	{
		votes();
		voteList.remove(CV);
	}

	@Override
	public void recordClanKill(final MOB killer, final MOB killed)
	{
		clanKills();
		final Area A=CMLib.map().areaLocation(killer);
		if(A!=null)
			clanKills.add(Long.valueOf(System.currentTimeMillis() + (365L * 24L * 60L * 60L * 1000)));
		updateClanKills();
		if((killer != null)
		&&(killed != null))
		{
			if(killed.isMonster())
				CMLib.database().DBUpdateClanKills(this.clanID(), killer.Name(), 1, 0);
			else
				CMLib.database().DBUpdateClanKills(this.clanID(), killer.Name(), 0, 1);
		}
	}

	@Override
	public int getCurrentClanKills(final MOB killer)
	{
		if(killer==null)
		{
			clanKills();
			return clanKills.size();
		}
		else
		{
			final MemberRecord M = CMLib.database().DBGetClanMember(this.clanID(), killer.Name());
			return M.playerpvps;
		}
	}


	@Override
	public double getCurrentClanGoldDonations(final MOB killer)
	{
		if(killer==null)
		{
			return 0;
		}
		else
		{
			final MemberRecord M = CMLib.database().DBGetClanMember(this.clanID(), killer.Name());
			return M.donatedGold;
		}
	}

	@Override
	public long getCurrentClanXPDonations(final MOB killer)
	{
		if(killer==null)
		{
			return this.exp;
		}
		else
		{
			final MemberRecord M = CMLib.database().DBGetClanMember(this.clanID(), killer.Name());
			return M.donatedXP;
		}
	}

	@Override
	public boolean isSet(final ClanFlag flag)
	{
		return clanFlags.contains(flag);
	}

	@Override
	public void setFlag(final ClanFlag flag, final boolean setOrUnset)
	{
		if(setOrUnset)
			clanFlags.add(flag);
		else
			clanFlags.remove(flag);
	}


	@Override
	public boolean isOnlyFamilyApplicants()
	{
		return govt().isFamilyOnly();
	}

	@Override
	public boolean isLoyaltyThroughItems()
	{
		return govt().isConquestItemLoyalty();
	}

	@Override
	public boolean isWorshipConquest()
	{
		return govt().isConquestByWorship();
	}

	@Override
	public long calculateMapPoints()
	{
		return calculateMapPoints(getControlledAreas());
	}

	@Override
	public long calculateMapPoints(final List<Area> controlledAreas)
	{
		long points=0;
		if(controlledAreas!=null)
		{
			for(final Area A : controlledAreas)
			{
				final LegalBehavior B=CMLib.law().getLegalBehavior(A);
				if(B!=null)
					points+=B.controlPoints();
			}
		}
		return points;
	}

	@Override
	public List<Area> getControlledAreas()
	{
		final Vector<Area> done=new Vector<Area>();
		for(final Enumeration<Area> e=CMLib.map().areas();e.hasMoreElements();)
		{
			final Area A=e.nextElement();
			final LegalBehavior B=CMLib.law().getLegalBehavior(A);
			if(B!=null)
			{
				final String controller=B.rulingOrganization();
				final Area A2=CMLib.law().getLegalObject(A);
				if(controller.equals(clanID())&&(!done.contains(A2)))
					done.addElement(A2);
			}
		}
		return done;
	}

	@Override
	public Enumeration<ClanVote> votes()
	{
		if(voteList==null)
		{
			final List<PlayerData> V=CMLib.database().DBReadPlayerData(clanID(),"CLANVOTES",clanID()+"/CLANVOTES");
			final XMLLibrary xmlLib=CMLib.xml();
			voteList=new Vector<ClanVote>();
			for(int v=0;v<V.size();v++)
			{
				final ClanVote CV=new ClanVote();
				final String rawxml=V.get(v).xml();
				if(rawxml.trim().length()==0)
					return new IteratorEnumeration<Clan.ClanVote>(voteList.iterator());
				final List<XMLLibrary.XMLTag> xml=xmlLib.parseAllXML(rawxml);
				if(xml==null)
				{
					Log.errOut("Clans","Unable to parse: "+rawxml);
					return new IteratorEnumeration<Clan.ClanVote>(voteList.iterator());
				}
				final List<XMLLibrary.XMLTag> voteData=xmlLib.getContentsFromPieces(xml,"BALLOTS");
				if(voteData==null)
				{
					Log.errOut("Clans","Unable to get BALLOTS data.");
					return new IteratorEnumeration<Clan.ClanVote>(voteList.iterator());
				}
				CV.voteStarter=xmlLib.getValFromPieces(voteData,"BY");
				CV.voteStarted=xmlLib.getLongFromPieces(voteData,"ON");
				CV.function=xmlLib.getIntFromPieces(voteData,"FUNC");
				CV.voteStatus=xmlLib.getIntFromPieces(voteData,"STATUS");
				CV.matter=xmlLib.getValFromPieces(voteData,"CMD");
				CV.votes=new PairVector<String,Boolean>();
				final List<XMLLibrary.XMLTag> xV=xmlLib.getContentsFromPieces(voteData,"VOTES");
				if((xV!=null)&&(xV.size()>0))
				{
					for(int x=0;x<xV.size();x++)
					{
						final XMLTag iblk=xV.get(x);
						if((!iblk.tag().equalsIgnoreCase("VOTE"))||(iblk.contents()==null))
							continue;
						final String userID=iblk.getValFromPieces("BY");
						final boolean yn=iblk.getBoolFromPieces("YN");
						CV.votes.addElement(userID,Boolean.valueOf(yn));
					}
				}
				voteList.add(CV);
			}
		}
		return new IteratorEnumeration<Clan.ClanVote>(voteList.iterator());
	}

	@Override
	public int getAutoPosition()
	{
		return autoPosition<0?govt().getAutoRole():autoPosition;
	}

	@Override
	public void setAutoPosition(final int pos)
	{
		if(pos == govt().getAutoRole())
			autoPosition=-1;
		else
			autoPosition=pos;
	}

	@Override
	public void setExp(final long newexp)
	{
		synchronized(expSync)
		{
			final long oldxp=exp;
			exp=newexp;
			if(exp<0)
				exp=0;
			final CMath.CompiledFormula form = govt().getXPCalculationFormula();
			if(oldxp < exp) // we gained
			{
				double nextLevelXP = CMath.parseMathExpression(form, new double[]{getClanLevel()}, 0.0);
				while(exp > nextLevelXP)
				{
					setClanLevel(getClanLevel()+1);
					bumpTrophyData(Trophy.MonthlyClanLevels, 1);
					clanAnnounce(""+getGovernmentName()+" "+name()+" has attained clan level "+getClanLevel()+"!");
					CMLib.achievements().possiblyBumpAchievement(getResponsibleMember(), AchievementLibrary.Event.CLANLEVELSGAINED, 1, this);
					update();
					nextLevelXP = CMath.parseMathExpression(form, new double[]{getClanLevel()}, 0.0);
				}
			}
			else
			if((oldxp > exp) && (getClanLevel()>1))
			{
				double prevLevelXP = CMath.parseMathExpression(form, new double[]{getClanLevel()-1}, 0.0);
				while(exp < prevLevelXP)
				{
					setClanLevel(getClanLevel()-1);
					CMLib.achievements().possiblyBumpAchievement(getResponsibleMember(), AchievementLibrary.Event.CLANLEVELSGAINED, -1, this);
					clanAnnounce(""+getGovernmentName()+" "+name()+" has reverted to clan level "+getClanLevel()+"!");
					update();
					prevLevelXP = CMath.parseMathExpression(form, new double[]{getClanLevel()-1}, 0.0);
				}
			}
		}
	}

	@Override
	public void adjExp(final MOB memberM, final int howMuch)
	{
		if (howMuch != 0)
		{
			setExp(getExp() + howMuch);
			if(howMuch > 0)
				bumpTrophyData(Trophy.MonthlyClanXP, howMuch);
			if(memberM != null)
				CMLib.database().DBUpdateClanDonates(this.clanID(), memberM.Name(), 0, howMuch);
		}
	}

	@Override
	public void adjDeposit(final MOB memberM, final double howMuch)
	{
		if(memberM != null)
			CMLib.database().DBUpdateClanDonates(this.clanID(), memberM.Name(), howMuch, 0);
	}


	@Override
	public long getExp()
	{
		return exp;
	}

	@Override
	public int getTrophies()
	{
		return clanTrophies;
	}

	@Override
	public void setTrophies(final int trophyFlag)
	{
		clanTrophies = trophyFlag;
	}

	@Override
	public void setTaxes(final double rate)
	{
		taxRate=rate;
	}

	@Override
	public double getTaxes()
	{
		return taxRate;
	}

	@Override
	public int getClanRelations(final String id)
	{
		final long i[]=relations.get(id.toUpperCase());
		if(i!=null)
			return (int)i[0];
		return  REL_NEUTRAL;
	}

	@Override
	public long getLastRelationChange(final String id)
	{
		final long i[]=relations.get(id.toUpperCase());
		if(i!=null)
			return i[1];
		return 0;
	}

	@Override
	public void setClanRelations(final String id, final int rel, final long time)
	{
		relations.remove(id.toUpperCase());
		final long[] i=new long[2];
		i[0]=rel;
		i[1]=time;
		relations.put(id.toUpperCase(),i);
	}

	@Override
	public int getGovernmentID()
	{
		return government;
	}

	@Override
	public void setGovernmentID(final int type)
	{
		government = type;
		lastGovernmentLoadTime = -1;
	}

	@Override
	public String getCategory()
	{
		if(clanCategory!=null)
			return clanCategory;
		return govt().getCategory();
	}

	@Override
	public int getMinClanMembers()
	{
		if(overrideMinClanMembers!=null)
			return overrideMinClanMembers.intValue();
		if(govt().getOverrideMinMembers()!=null)
			return govt().getOverrideMinMembers().intValue();
		return CMProps.getIntVar(CMProps.Int.MINCLANMEMBERS);
	}

	@Override
	public void setMinClanMembers(final int amt)
	{
		overrideMinClanMembers=null;
		if(govt().getOverrideMinMembers()!=null)
		{
			if(govt().getOverrideMinMembers().intValue()==amt)
				return;
			overrideMinClanMembers=Integer.valueOf(amt);
		}
		else
		{
			if(CMProps.getIntVar(CMProps.Int.MINCLANMEMBERS)==amt)
				return;
			overrideMinClanMembers=Integer.valueOf(amt);
		}
	}

	@Override
	public void setCategory(final String newCategory)
	{
		if(govt().getCategory().equalsIgnoreCase(newCategory))
			clanCategory=null;
		else
			clanCategory=newCategory;
	}

	@Override
	public boolean isRivalrous()
	{
		if(isRivalrous==null)
			return govt().isRivalrous();
		return isRivalrous.booleanValue();
	}

	@Override
	public void setRivalrous(final boolean isRivalrous)
	{
		if(govt().isRivalrous()==isRivalrous)
			this.isRivalrous=null;
		else
			this.isRivalrous=Boolean.valueOf(isRivalrous);
	}

	@Override
	public void create()
	{
		CMLib.database().DBCreateClan(this);
		CMLib.clans().addClan(this);
	}

	@Override
	public void update()
	{
		CMLib.database().DBUpdateClan(this);
	}

	@Override
	public void addMember(final MOB M, final int role)
	{
		transientSize=-1;
		M.setClan(clanID(),role);
		CMLib.database().DBUpdateClanMembership(M.Name(), clanID(), role);
		updateClanPrivileges(M);
		bumpTrophyData(Trophy.MonthlyNewMembers, 1);
	}

	@Override
	public void delMember(final MOB M)
	{
		transientSize=-1;
		CMLib.database().DBUpdateClanMembership(M.Name(), clanID(), -1);
		M.setClan(clanID(),-1);
		updateClanPrivileges(M);
	}

	@Override
	public boolean updateClanPrivileges(final MOB M)
	{
		boolean didUpdatePlayer=false;
		if(M==null)
			return false;
		final Pair<Clan,Integer> p=M.getClanRole(clanID());

		if((p!=null)
		&& (getAuthority(p.second.intValue(),Function.CLAN_BENEFITS)!=Clan.Authority.CAN_NOT_DO))
		{
			final CharClass CC=getClanClassC();
			if((CC!=null)
			&&(CC.availabilityCode()!=0)
			&&(M.baseCharStats().getCurrentClass()!=CC))
			{
				M.baseCharStats().setCurrentClass(CC);
				didUpdatePlayer=true;
				M.recoverCharStats();
			}
			CMLib.achievements().loadClanAchievements(M,AchievementLoadFlag.NORMAL);
		}
		else
		{
			final String removeMsg =CMLib.achievements().removeClanAchievementAwards(M, this);
			if((removeMsg != null)&&(removeMsg.length()>0))
				M.tell(removeMsg);
		}

		// Get a list of all possible spell grants for this clan, check against class qualifications.
		// Form a list of forbidden spells, and then remove THOSE.
		final Map<String, AbilityMapping> allAbles =  CMLib.ableMapper().getAbleMapping(getGovernment().getName());
		final Set<String> qualAbleIDS = new TreeSet<String>();
		for(final Ability A :  getGovernment().getClanLevelAbilities(M, this, Integer.valueOf(getClanLevel())))
			qualAbleIDS.add(A.ID());
		for(final String ableID : allAbles.keySet())
		{
			final Ability A=M.fetchAbility(ableID);
			if((A != null)
			&&(!qualAbleIDS.contains(ableID))
			&&(!CMLib.ableMapper().qualifiesByLevel(M, ableID)) // this will check ALL clans, as well as other sources.
			&&(allAbles.get(ableID).autoGain()))
				M.delAbility(A);
		}

		final PlayerStats pStats = M.playerStats();
		if(pStats!=null)
		{
			final Set<String> myAllowedTitles = new TreeSet<String>();
			if(p != null)
			{
				final ClanPosition myPos = govt().findPositionRole(p.second);
				final String myNicePosName = CMStrings.capitalizeAllFirstLettersAndLower(myPos.getName());
				for(final String baseTitle : govt().getTitleAwards())
					myAllowedTitles.add(L(baseTitle,name(),myNicePosName));
				for(final String posTitle : myPos.getTitleAwards())
					myAllowedTitles.add(L(posTitle,name(),myNicePosName));
				if(getAuthority(p.second.intValue(),Function.CLAN_TITLES)!=Clan.Authority.CAN_NOT_DO)
				{
					for(final String title : myAllowedTitles)
					{
						if(!pStats.getTitles().contains(title))
							pStats.getTitles().add(title);
					}
				}
			}
			for(final ClanPosition pos : govt().getPositions())
			{
				if((p==null)||(p.second.intValue()!=pos.getRoleID()))
				{
					final String nicePosName = CMStrings.capitalizeAllFirstLettersAndLower(pos.getName());
					for(final String baseTitle : govt().getTitleAwards())
					{
						final String badTitle = L(baseTitle,name(),nicePosName);
						if(!myAllowedTitles.contains(badTitle))
						{
							for(final String titleCheck : pStats.getTitles())
							{
								if(titleCheck.equalsIgnoreCase(badTitle))
									pStats.getTitles().remove(titleCheck);

							}
						}
					}
					for(final String posTitle : pos.getTitleAwards())
					{
						final String badTitle = L(posTitle,name(),nicePosName);
						if(!myAllowedTitles.contains(badTitle))
						{
							for(final String titleCheck : pStats.getTitles())
							{
								if(titleCheck.equalsIgnoreCase(badTitle))
									pStats.getTitles().remove(titleCheck);

							}
						}
					}
				}
			}
		}
		if(p==null)
		{
			Item I=null;
			final List<Item> itemsToMove=new ArrayList<Item>();
			for(int i=0;i<M.numItems();i++)
			{
				I=M.getItem(i);
				if(I instanceof ClanItem)
					itemsToMove.add(I);
			}
			for(int i=0;i<itemsToMove.size();i++)
			{
				I=itemsToMove.get(i);
				if(I!=null)
				{
					Room R=null;
					if((getDonation()!=null)
					&&(getDonation().length()>0))
						R=CMLib.map().getRoom(getDonation());
					if((R==null)
					&&(getRecall()!=null)
					&&(getRecall().length()>0))
						R=CMLib.map().getRoom(getRecall());
					if(I instanceof Container)
					{
						final List<Item> V=((Container)I).getDeepContents();
						for(int v=0;v<V.size();v++)
							V.get(v).setContainer(null);
					}
					I.setContainer(null);
					I.wearAt(Wearable.IN_INVENTORY);
					if(R!=null)
						R.moveItemTo(I);
					else
					if(M.isMine(I))
						I.destroy();
					didUpdatePlayer=true;
				}
			}
		}
		if((didUpdatePlayer)&&(!CMSecurity.isSaveFlag(CMSecurity.SaveFlag.NOPLAYERS)))
			CMLib.database().DBUpdatePlayer(M);
		return didUpdatePlayer;
	}

	@Override
	public void destroyClan()
	{
		final List<MemberRecord> members=getMemberList();
		for(final MemberRecord member : members)
		{
			final MOB M=CMLib.players().getLoadPlayer(member.name);
			if(M!=null)
			{
				M.setClan(clanID(),-1);
				updateClanPrivileges(M);
				CMLib.database().DBUpdateClanMembership(M.Name(), clanID(), -1);
			}
		}
		CMLib.database().DBDeleteJournal("a Journal of "+getGovernmentName()+" "+getName(), null);
		CMLib.database().DBDeleteJournal("CLAN_MOTD"+clanID(), null);
		CMLib.database().DBDeleteClan(this);
		CMLib.clans().removeClan(this);
	}

	protected CharClass getClanClassC()
	{
		if(clanClass.length()==0)
			return null;
		CharClass C=CMClass.getCharClass(clanClass);
		if(C==null)
			C=CMClass.findCharClass(clanClass);
		return C;
	}

	@Override
	public String getDetail(final MOB mob)
	{
		final int COLBL_WIDTH=CMLib.lister().fixColWidth(16.0,mob);
		final StringBuilder msg=new StringBuilder("");
		final Pair<Clan,Integer> mobClanRole=(mob!=null)?(mob.getClanRole(clanID())):null;
		final boolean member=(mob!=null)
							&&(mobClanRole!=null)
							&&(getAuthority(mobClanRole.second.intValue(),Function.LIST_MEMBERS)!=Authority.CAN_NOT_DO);
		final boolean sysmsgs=(mob!=null)&&mob.isAttributeSet(MOB.Attrib.SYSOPMSGS);
		final CMath.CompiledFormula form = govt().getXPCalculationFormula();
		final double nextLevelXP = CMath.parseMathExpression(form, new double[]{getClanLevel()}, 0.0);
		msg.append("^x"+CMStrings.padRight(L(getGovernmentName()+" Profile"),COLBL_WIDTH)+":^.^N "+clanID()+"\n\r");
		msg.append("-----------------------------------------------------------------\n\r");
		msg.append(getPremise()+"\n\r");
		msg.append("-----------------------------------------------------------------\n\r");
		msg.append("^x"+CMStrings.padRight(L("Level"),COLBL_WIDTH)+":^.^N "+getClanLevel());
		if(member||sysmsgs)
			msg.append(L("                      (Next at ^w@x1^Nxp)",""+nextLevelXP));
		msg.append("\n\r");
		msg.append("^x"+CMStrings.padRight(L("Type"),COLBL_WIDTH)+":^.^N "+CMStrings.capitalizeAndLower(govt().getName())+"\n\r");

		if(getAcceptanceSettings().length()>0)
		{
			msg.append("^x"+CMStrings.padRight(L("Qualifications"),COLBL_WIDTH)+":^.^N "+CMLib.masking().maskDesc(getAcceptanceSettings())+"\n\r");
			if(getBasicRequirementMask().length()>0)
				msg.append("^x"+CMStrings.padLeft(L("Plus "),COLBL_WIDTH)+":^.^N "+CMLib.masking().maskDesc(getBasicRequirementMask())+"\n\r");
		}
		else
		if(getBasicRequirementMask().length()>0)
			msg.append("^x"+CMStrings.padRight(L("Qualifications"),COLBL_WIDTH)+":^.^N "+CMLib.masking().maskDesc(getBasicRequirementMask())+"\n\r");
		else
			msg.append("^x"+CMStrings.padRight(L("Qualifications"),COLBL_WIDTH)+":^.^N "+L("Anyone may apply")+"\n\r");
		final CharClass clanC=getClanClassC();
		if(clanC!=null)
			msg.append("^x"+CMStrings.padRight(L("Class"),COLBL_WIDTH)+":^.^N "+clanC.name()+"\n\r");
		msg.append("^x"+CMStrings.padRight(L("Exp. Tax Rate"),COLBL_WIDTH)+":^.^N "+((int)Math.round(getTaxes()*100))+"%\n\r");
		if(member||sysmsgs)
		{
			msg.append("^x"+CMStrings.padRight(L("Experience Pts."),COLBL_WIDTH)+":^.^N "+getExp()+"\n\r");
			if(getMorgue().length()>0)
			{
				final Room R=CMLib.map().getRoom(getMorgue());
				if(R!=null)
					msg.append("^x"+CMStrings.padRight(L("Morgue"),COLBL_WIDTH)+":^.^N "+R.displayText(mob)+"\n\r");
			}
			if(getDonation().length()>0)
			{
				final Room R=CMLib.map().getRoom(getDonation());
				if(R!=null)
					msg.append("^x"+CMStrings.padRight(L("Donations"),COLBL_WIDTH)+":^.^N "+R.displayText(mob)+"\n\r");
			}
			if(getRecall().length()>0)
			{
				final Room R=CMLib.map().getRoom(getRecall());
				if(R!=null)
					msg.append("^x"+CMStrings.padRight(L("Recall"),COLBL_WIDTH)+":^.^N "+R.displayText(mob)+"\n\r");
			}
		}
		final List<MemberRecord> members=getMemberList();
		final Set<ClanPosition> sortedPositions=new HashSet<ClanPosition>();
		for(int i=0;i<govt().getPositions().length;i++)
		{
			ClanPosition topRankedPos=null;
			for(final ClanPosition pos : govt().getPositions())
			{
				if((pos.isPublic())
				&&(!sortedPositions.contains(pos))
				&&((topRankedPos==null)||(pos.getRank() < topRankedPos.getRank())))
					topRankedPos = pos;
			}
			if(topRankedPos != null)
			{
				msg.append("^x"+CMStrings.padRight(CMStrings.capitalizeAllFirstLettersAndLower(topRankedPos.getPluralName()),COLBL_WIDTH)+":^.^N "+crewList(members, topRankedPos.getRoleID())+"\n\r");
				sortedPositions.add(topRankedPos);
			}
		}
		msg.append("^x"+CMStrings.padRight(L("Total Members"),COLBL_WIDTH)+":^.^N "+members.size()+"\n\r");
		if(CMLib.clans().numClans()>1)
		{
			msg.append("-----------------------------------------------------------------\n\r");
			msg.append("^x"+CMStrings.padRight(L("Clan Relations"),COLBL_WIDTH)+":^.^N \n\r");
			boolean others = false;
			final int COLCL_WIDTH=CMLib.lister().fixColWidth(26.0,mob);
			for(final Enumeration<Clan> e=CMLib.clans().clans();e.hasMoreElements();)
			{
				final Clan C=e.nextElement();
				if((C!=this)&&(C.isRivalrous()))
				{
					final int rel=getClanRelations(C.clanID());
					final int orel=C.getClanRelations(clanID());
					if((rel!=REL_NEUTRAL) || (orel != REL_NEUTRAL))
					{
						msg.append("^H"+CMStrings.padRight(C.name(),COLCL_WIDTH)+":^.^N ");
						msg.append(REL_COLORS[rel]).append(CMStrings.capitalizeAndLower(REL_DESCS[rel]));
						if((rel!=REL_NEUTRAL) || (orel != REL_NEUTRAL))
							msg.append("^N (^W<-").append(REL_COLORS[orel]).append(CMStrings.capitalizeAndLower(REL_DESCS[orel])).append("^N)");
						else
							msg.append("^N");
						msg.append("\n\r");
					}
					else
						others=true;
				}
			}
			if(others)
			{
				msg.append("^H"+CMStrings.padRight("All Others",COLCL_WIDTH)+":^.^N ");
				msg.append(REL_COLORS[REL_NEUTRAL]).append(CMStrings.capitalizeAndLower(REL_DESCS[REL_NEUTRAL]));
				msg.append("^N").append("\n\r");
			}
		}
		if(member||sysmsgs)
		{
			updateClanPrivileges(mob);
			for(final ClanPosition pos : govt().getPositions())
			{
				if((!pos.isPublic())&&(member)
				&&((pos.getRoleID()!=govt().getAutoRole())||(pos.getRoleID()==govt().getAcceptPos())))
				{
					msg.append("-----------------------------------------------------------------\n\r");
					msg.append("^x"+CMStrings.padRight(CMStrings.capitalizeAndLower(pos.getPluralName()),COLBL_WIDTH)
							  +":^.^N "+crewList(members, pos.getRoleID())+"\n\r");
				}
			}
			if((mobClanRole!=null)
			&&(govt().getAutoRole()!=govt().getAcceptPos())
			&&((getAuthority(mobClanRole.second.intValue(),Function.ACCEPT)!=Clan.Authority.CAN_NOT_DO)||sysmsgs))
			{
				final ClanPosition pos=govt().getPositions()[getAutoPosition()];
				msg.append("-----------------------------------------------------------------\n\r");
				msg.append("^x"+CMStrings.padRight(CMStrings.capitalizeAndLower(pos.getPluralName()),COLBL_WIDTH)
						  +":^.^N "+crewList(members, pos.getRoleID())+"\n\r");
			}
		}

		final List<String> control=new ArrayList<String>();
		final List<Area> controlledAreas=getControlledAreas();
		for(final Area A : controlledAreas)
			control.add(A.name());
		if(control.size()>0)
		{
			msg.append("-----------------------------------------------------------------\n\r");
			msg.append(L("^xClan Controlled Areas (% revolt):^.^N\n\r"));
			Collections.sort(control);
			int col=0;
			final int COL_LEN=CMLib.lister().fixColWidth(25.0,mob);
			for(int i=0;i<control.size();i++)
			{
				if((++col)>3)
				{
					msg.append("\n\r");
					col=1;
				}
				final Area A=CMLib.map().getArea(control.get(i));
				if(A!=null)
				{
					final LegalBehavior B=CMLib.law().getLegalBehavior(A);
					final Area legalA=CMLib.law().getLegalObject(A);
					int pctRevolt=0;
					if((B!=null)&&(legalA!=null))
						pctRevolt=B.revoltChance();
					msg.append("^c"+CMStrings.padRight(A.name()+"^N ("+pctRevolt+"%)",COL_LEN)+"^N");
				}
			}
			msg.append("\n\r");
		}
		if((CMLib.clans().trophySystemActive())
		&&(getTrophies()!=0)
		&&(!isSet(ClanFlag.NOTROPHY)))
		{
			msg.append("-----------------------------------------------------------------\n\r");
			msg.append(L("^xTrophies awarded:^.^N\n\r"));
			for(final Trophy t : Trophy.values())
			{
				if(CMath.bset(getTrophies(),t.flagNum()))
				{
					msg.append(t.codeString+" ");
					if(t.name().toUpperCase().indexOf("MONTHLY")<0)
						msg.append("(").append(this.getTrophyData(t)).append(") ");
					else
						msg.append(":");
					msg.append(L(" Prize: @x1\n\r",CMLib.clans().translatePrize(t)));
				}
			}
		}
		final List<Achievement> achievements = new ArrayList<Achievement>(); // current users achievements rechecked above
		for(final Tattoo tatt : this.tattoos)
		{
			final Achievement A=CMLib.achievements().getAchievement(tatt.getTattooName());
			if(A!=null) // let's just trust this one.
				achievements.add(A);
		}
		if(achievements.size()>0)
		{
			msg.append("-----------------------------------------------------------------\n\r");
			msg.append(L("^xClan Achievements:^.^N\n\r"));
			for(final Achievement A : achievements)
				msg.append("^N"+A.getDisplayStr()+"\n\r");
		}
		if(((mobClanRole!=null)&&(getAuthority(mobClanRole.second.intValue(),Function.CLAN_BENEFITS)!=Clan.Authority.CAN_NOT_DO))||sysmsgs)
		{
			msg.append("-----------------------------------------------------------------\n\r");
			msg.append(L("^xClan Level Benefits:^.^N\n\r"));
			final List<AbilityMapper.AbilityMapping> abilities=CMLib.ableMapper().getUpToLevelListings(govt().getName(),getClanLevel(),true,false);
			if(abilities.size()>0)
			{
				final List<String> names = new Vector<String>();
				for(final AbilityMapper.AbilityMapping aMap : abilities)
				{
					final Ability A=CMClass.getAbility(aMap.abilityID());
					if(A!=null)
					{
						if((aMap.extFields().size()==0)
						||(mobClanRole==null)
						||(sysmsgs)
						||(aMap.extFields().containsKey(mobClanRole.second.toString())))
							names.add(A.name()+(aMap.autoGain()?"":"(q)")+((aMap.extFields().size()>0)?"*":""));
					}
				}
				for(final Achievement A : achievements)
				{
					final Award[] awards = A.getRewards();
					for(final Award award : awards)
						names.add(CMLib.achievements().fixAwardDescription(A, award, mob, mob));
				}
				msg.append(CMLib.lister().makeColumns(mob,names,null,3));
				msg.append("\n\r");
			}
			final int numReff=CMath.s_int(govt().getStat("NUMREFF"));
			for(int i=0;i<numReff;i++)
			{
				final String ableName=govt().getStat("GETREFF"+i);
				final String ableText=govt().getStat("GETREFFPARM"+i);
				final int ableLvl=CMath.s_int(govt().getStat("GETREFFLVL"+i));
				final List<String> ableRoles=CMParms.parseCommas(govt().getStat("GETREFFROLE"+i),true);
				final Ability A=CMClass.getAbility(ableName);
				if((A!=null)
				&&(ableLvl<=this.clanLevel)
				&&((ableRoles.size()==0)
					||(mobClanRole==null)
					||(sysmsgs)
					||(ableRoles.contains(mobClanRole.second.toString()))))
				{
					A.setMiscText(ableText);
					msg.append(A.accountForYourself()).append("\n\r");
				}
			}
		}
		return msg.toString();
	}

	public String L(final String str, final String ... xs)
	{
		return CMLib.lang().fullSessionTranslation(str, xs);
	}

	@Override
	public String getGovernmentName()
	{
		return CMStrings.capitalizeAndLower(govt().getName());
	}

	@Override
	public boolean canBeAssigned(final MOB mob, final int role)
	{
		if(mob==null)
			return false;
		if((role<0)||(role>govt().getPositions().length))
			return false;
		final ClanPosition pos = govt().getPositions()[role];
		return CMLib.masking().maskCheck(fixRequirementMask(pos.getInnerMaskStr()), mob, true);
	}

	private boolean canBeAssigned(final ThinPlayer mob, final int role)
	{
		if(mob==null)
			return false;
		if((role<0)||(role>govt().getPositions().length))
			return false;
		final ClanPosition pos = govt().getPositions()[role];
		if((pos.getInnerMaskStr() == null)
		||(pos.getInnerMaskStr().trim().length()==0))
			return true;
		final String mask = this.fixRequirementMask(pos.getInnerMaskStr());
		return CMLib.masking().maskCheck(CMLib.masking().getPreCompiledMask(mask), mob);
	}

	@Override
	public Authority getAuthority(final int roleID, final Function function)
	{
		if((roleID<0)||(roleID>=govt().getPositions().length))
			return Authority.CAN_NOT_DO;
		return govt().getPositions()[roleID].getFunctionChart()[function.ordinal()];
	}

	public String fixRequirementMask(final String oldMask)
	{
		if((oldMask==null)||(oldMask.trim().length()==0))
			return "";
		final StringBuilder mask=new StringBuilder(oldMask.trim());
		if(mask.length()==0)
			return "";
		final MOB M=getResponsibleMember();
		int x=mask.indexOf("%[");
		while(x>=0)
		{
			final int y=mask.indexOf("]%",x+1);
			if(y>x)
			{
				final String tag=mask.substring(x+2,y);
				String value="Unknown";
				if(isStat(tag))
					value=getStat(tag);
				else
				if(M!=null)
				{
					if(tag.equalsIgnoreCase("WORSHIPCHARID"))
					{
						value=M.getWorshipCharID();
						if(value.length()==0)
							value="ANY";
					}
					else
					if(CMLib.coffeeMaker().isAnyGenStat(M, tag))
						value=CMLib.coffeeMaker().getAnyGenStat(M, tag);
				}
				else
				if(tag.equalsIgnoreCase("WORSHIPCHARID"))
					value="ANY";
				mask.replace(x, y+2, value);
			}
			if(x>=mask.length()-1)
				break;
			x=mask.indexOf("%[",x+1);
		}
		return mask.toString();
	}

	@Override
	public String getBasicRequirementMask()
	{
		return fixRequirementMask(govt().getRequiredMaskStr());
	}

	protected List<MemberRecord> getRealMemberList(final int PosFilter)
	{
		final List<MemberRecord> members=getMemberList(PosFilter);
		if(members==null)
			return null;
		final List<MemberRecord> realMembers=new Vector<MemberRecord>();
		for(final MemberRecord member : members)
		{
			if(CMLib.players().playerExists(member.name))
				realMembers.add(member);
		}
		return members;
	}

	@Override
	public int getSize()
	{
		if(transientSize < 0)
			transientSize = CMLib.database().DBReadClanMembers(clanID()).size();
		return transientSize;
	}

	@Override
	public String name()
	{
		return clanName;
	}

	@Override
	public String getName()
	{
		return clanName;
	}

	@Override
	public String clanID()
	{
		return clanName;
	}

	@Override
	public void setName(final String newName)
	{
		clanName = newName;
	}

	@Override
	public String getPremise()
	{
		return clanPremise;
	}

	@Override
	public void setPremise(final String newPremise)
	{
		clanPremise = newPremise;
	}

	@Override
	public int getClanLevel()
	{
		return clanLevel;
	}

	@Override
	public void setClanLevel(final int newClanLevel)
	{
		if(newClanLevel<=0)
			clanLevel=1;
		else
			clanLevel = newClanLevel;
	}

	@Override
	public String getAcceptanceSettings()
	{
		return acceptanceSettings;
	}

	@Override
	public void setAcceptanceSettings(final String newSettings)
	{
		acceptanceSettings = newSettings;
	}

	@Override
	public String getClanClass()
	{
		return clanClass;
	}

	@Override
	public void setClanClass(final String newClass)
	{
		clanClass = newClass;
	}

	@Override
	public String getDataXML()
	{
		final StringBuilder str=new StringBuilder("");
		final XMLLibrary xmlLib=CMLib.xml();
		str.append("<POLITICS>");
		str.append(xmlLib.convertXMLtoTag("GOVERNMENT",""+getGovernmentID()));
		str.append(xmlLib.convertXMLtoTag("TAXRATE",""+getTaxes()));
		str.append(xmlLib.convertXMLtoTag("EXP",""+getExp()));
		str.append(xmlLib.convertXMLtoTag("ONLINEMINS",""+totalOnlineMins));
		str.append(xmlLib.convertXMLtoTag("LVLSGAINED",""+totalLevelsGained));
		str.append(xmlLib.convertXMLtoTag("LEVEL",""+getClanLevel()));
		str.append(xmlLib.convertXMLtoTag("CCLASS",""+getClanClass()));
		str.append(xmlLib.convertXMLtoTag("AUTOPOS",""+getAutoPosition()));
		str.append(xmlLib.convertXMLtoTag("FLAGS",""+getStat("FLAGS")));
		str.append(xmlLib.convertXMLtoTag("LASTSTATUSCHANGE",""+this.lastStatusChange));
		if(clanCategory!=null)
			str.append(xmlLib.convertXMLtoTag("CATE",clanCategory));
		if(overrideMinClanMembers!=null)
			str.append(xmlLib.convertXMLtoTag("MINM",overrideMinClanMembers.toString()));
		if(isRivalrous!=null)
			str.append(xmlLib.convertXMLtoTag("RIVAL",isRivalrous.toString()));
		str.append("<ACHIEVEMENTS");
		for(final Iterator<Tracker> i=achievementers.values().iterator();i.hasNext();)
		{
			final Tracker T = i.next();
			if(T.getAchievement().isSavableTracker() && (T.getCount(null) != 0))
				str.append(" ").append(T.getAchievement().getTattoo()).append("=").append(T.getCount(null));
			// getCount(null) should be ok, because it's only the un-savable trackers that need the mob obj
		}
		str.append(" />");
		str.append("<TATTOOS>").append(CMParms.toListString(tattoos)).append("</TATTOOS>");
		if(relations.size()==0)
			str.append("<RELATIONS/>");
		else
		{
			str.append("<RELATIONS>");
			for(final Iterator<String> e=relations.keySet().iterator();e.hasNext();)
			{
				final String key=e.next();
				str.append("<RELATION>");
				str.append(xmlLib.convertXMLtoTag("CLAN",key));
				final long[] i=relations.get(key);
				str.append(xmlLib.convertXMLtoTag("STATUS",""+i[0]));
				str.append("</RELATION>");
			}
			str.append("</RELATIONS>");
		}
		str.append("</POLITICS>");
		str.append(xmlLib.convertXMLtoTag("ONLINEMINS",""+totalOnlineMins));
		str.append(xmlLib.convertXMLtoTag("LVLSGAINED",""+totalLevelsGained));
		final StringBuilder monthlies = new StringBuilder();
		monthlies.append(monthOnlineMins).append(",");
		monthlies.append(monthPlayerXP).append(",");
		monthlies.append(monthClanXP).append(",");
		monthlies.append(monthConquered).append(",");
		monthlies.append(monthClanLevels).append(",");
		monthlies.append(monthControlPoints).append(",");
		monthlies.append(monthNewMembers);
		str.append(xmlLib.convertXMLtoTag("MONTHLYSTATS",monthlies.toString()));

		return str.toString();
	}

	@Override
	public void setDataXML(final String politics)
	{
		final XMLLibrary xmlLib=CMLib.xml();
		XMLTag piece;
		relations.clear();
		government=0;
		if(politics.trim().length()==0)
			return;
		final List<XMLLibrary.XMLTag> xml=xmlLib.parseAllXML(politics);
		if(xml==null)
		{
			Log.errOut("Clans","Unable to parse: "+politics);
			return;
		}

		final List<XMLLibrary.XMLTag> poliData=xmlLib.getContentsFromPieces(xml,"POLITICS");
		if(poliData==null)
		{
			Log.errOut("Clans","Unable to get POLITICS data.");
			return;
		}
		government=xmlLib.getIntFromPieces(poliData,"GOVERNMENT");
		exp=xmlLib.getLongFromPieces(poliData,"EXP");
		setClanLevel(xmlLib.getIntFromPieces(poliData,"LEVEL"));
		setExp(exp); // may change the level
		taxRate=xmlLib.getDoubleFromPieces(poliData,"TAXRATE");
		clanClass=xmlLib.getValFromPieces(poliData,"CCLASS");
		lastStatusChange=xmlLib.getLongFromPieces(poliData,"LASTSTATUSCHANGE");

		autoPosition=xmlLib.getIntFromPieces(poliData,"AUTOPOS");
		clanCategory=null;
		piece=xmlLib.getPieceFromPieces(poliData, "CATE");
		if(piece!=null)
			setCategory(piece.value());
		overrideMinClanMembers=null;
		piece=xmlLib.getPieceFromPieces(poliData, "MINM");
		if(piece!=null)
			this.setMinClanMembers(CMath.s_int(piece.value()));
		isRivalrous=null;
		piece=xmlLib.getPieceFromPieces(poliData, "RIVAL");
		if(piece!=null)
			setRivalrous(CMath.s_bool(piece.value()));

		final String monthlyData = xmlLib.getValFromPieces(poliData, "MONTHLYSTATS");
		final int[] data=CMParms.parseIntList(monthlyData, ',');
		if((data != null)&&(data.length>6))
		{
			monthOnlineMins = data[0];
			monthPlayerXP = data[1];
			monthClanXP = data[2];
			monthConquered = data[3];
			monthClanLevels = data[4];
			monthControlPoints = data[5];
			monthNewMembers = data[6];
		}
		totalOnlineMins=xmlLib.getIntFromPieces(poliData,"ONLINEMINS");
		totalLevelsGained=xmlLib.getIntFromPieces(poliData,"LVLSGAINED");
		setStat("FLAGS",xmlLib.getValFromPieces(poliData,"FLAGS"));

		final XMLTag achievePiece = xmlLib.getPieceFromPieces(poliData, "ACHIEVEMENTS");
		achievementers.clear();
		for(final Enumeration<Achievement> a=CMLib.achievements().achievements(Agent.CLAN);a.hasMoreElements();)
		{
			final Achievement A=a.nextElement();
			if((achievePiece != null) && achievePiece.parms().containsKey(A.getTattoo()))
				achievementers.put(A.getTattoo(), A.getTracker(CMath.s_int(achievePiece.parms().get(A.getTattoo()).trim())));
			else
				achievementers.put(A.getTattoo(), A.getTracker(0));
		}
		final String[] allTattoos=xmlLib.getValFromPieces(poliData, "TATTOOS").split(",");
		this.tattoos.clear();
		for(final String tattoo : allTattoos)
			this.addTattoo(tattoo);

		// now RESOURCES!
		final List<XMLLibrary.XMLTag> xV=xmlLib.getContentsFromPieces(poliData,"RELATIONS");
		if((xV!=null)&&(xV.size()>0))
		{
			for(int x=0;x<xV.size();x++)
			{
				final XMLTag iblk=xV.get(x);
				if((!iblk.tag().equalsIgnoreCase("RELATION"))||(iblk.contents()==null))
					continue;
				final String relClanID=iblk.getValFromPieces("CLAN");
				final int rel=iblk.getIntFromPieces("STATUS");
				setClanRelations(relClanID,rel,0);
			}
		}
	}

	@Override
	public int getStatus()
	{
		return clanStatus;
	}

	@Override
	public void setStatus(final int newStatus)
	{
		if(newStatus != clanStatus)
			this.lastStatusChange=System.currentTimeMillis();
		clanStatus = newStatus;
	}

	@Override
	public String getRecall()
	{
		return clanRecall;
	}

	@Override
	public void setRecall(final String newRecall)
	{
		clanRecall = newRecall;
	}

	@Override
	public String getMorgue()
	{
		return clanMorgue;
	}

	@Override
	public void setMorgue(final String newMorgue)
	{
		clanMorgue = newMorgue;
	}

	@Override
	public String getDonation()
	{
		return clanDonationRoom;
	}

	@Override
	public void setDonation(final String newDonation)
	{
		clanDonationRoom = newDonation;
	}

	@Override
	public List<MemberRecord> getMemberList()
	{
		return getMemberList(-1);
	}

	public int filterMedianLevel(final List<FullMemberRecord> members)
	{
		final List<Integer> lvls=new SortedListWrap<Integer>(new XVector<Integer>());
		for(final FullMemberRecord r : members)
			lvls.add(Integer.valueOf(r.level));
		if(lvls.size()>0)
			return lvls.get(lvls.size()/2).intValue();
		return 0;
	}

	public List<MemberRecord> filterMemberList(final List<? extends MemberRecord> members, final int posFilter)
	{
		final Vector<MemberRecord> filteredMembers=new Vector<MemberRecord>();
		for(final MemberRecord member : members)
		{
			if(((member.role==posFilter)||(posFilter<0))
			&&(member.name.length()>0))
				filteredMembers.add(member);
		}
		return filteredMembers;
	}

	@Override
	public List<MemberRecord> getMemberList(final int posFilter)
	{
		final List<MemberRecord> members=CMLib.database().DBReadClanMembers(clanID());
		transientSize = members.size();
		return filterMemberList(members, posFilter);
	}

	@Override
	public MemberRecord findMemberRecord(final String name)
	{
		final List<MemberRecord> members=CMLib.database().DBReadClanMembers(clanID());
		transientSize = members.size();
		for(final MemberRecord member : members)
		{
			if(member.name.equalsIgnoreCase(name))
				return member;
		}
		for(final MemberRecord member : members)
		{
			if(member.name.startsWith(name))
				return member;
		}
		return null;
	}

	@Override
	public MOB findMember(final String name)
	{
		final MemberRecord M=findMemberRecord(name);
		if(M != null)
			return CMLib.players().getPlayerAllHosts(M.name);
		return null;
	}

	@Override
	public MemberRecord getMember(final String name)
	{
		return CMLib.database().DBGetClanMember(clanID(),name);
	}

	@Override
	public List<FullMemberRecord> getFullMemberList()
	{
		final List<FullMemberRecord> members=new Vector<FullMemberRecord>();
		final List<MemberRecord> fullMembers = CMLib.database().DBReadClanMembers(clanID());
		final List<MemberRecord> subMembers=filterMemberList(fullMembers, -1);
		transientSize = fullMembers.size();
		for(final MemberRecord member : subMembers)
		{
			if(member!=null)
			{
				final MOB M=CMLib.players().getPlayer(member.name);
				if((M!=null)
				&&(M.playerStats()!=null))
				{
					final boolean isAdmin=CMSecurity.isASysOp(M) || M.phyStats().level() > CMProps.get(M.session()).getInt(CMProps.Int.LASTPLAYERLEVEL);
					if(M.lastTickedDateTime()>0)
						members.add(new FullMemberRecord(member,M.basePhyStats().level(),M.lastTickedDateTime(),isAdmin));
					else
						members.add(new FullMemberRecord(member,M.basePhyStats().level(),M.playerStats().getLastDateTime(),isAdmin));
				}
				else
				{
					final PlayerLibrary.ThinPlayer tP = CMLib.database().getThinUser(member.name);
					if(tP != null)
					{
						final boolean isAdmin=CMSecurity.isASysOp(tP) || tP.level() > CMProps.getIntVar(CMProps.Int.LASTPLAYERLEVEL);
						members.add(new FullMemberRecord(member,tP.level(),tP.last(),isAdmin));
					}
					else
					{
						Log.warnOut("Clan "+clanID()+" removed member '"+member.name+"' due to being nonexistant!");
						CMLib.database().DBUpdateClanMembership(member.name, clanID(), -1);
					}
				}
			}
		}
		return members;
	}

	private String crewList(List<? extends MemberRecord> members, final int posFilter)
	{
		final StringBuffer list=new StringBuffer("");
		members = filterMemberList(members, posFilter);
		if(members.size()>1)
		{
			for(int j=0;j<(members.size() - 1);j++)
				list.append(members.get(j).name+", ");
			list.append("and "+members.get(members.size()-1).name);
		}
		else
		if(members.size()>0)
			list.append(members.get(0).name);
		return list.toString();
	}

	@Override
	public int getNumVoters(final Function function)
	{
		int voters=0;
		final List<MemberRecord> members=getMemberList();
		final Function voteFunc = (function == Function.ASSIGN) ? Function.VOTE_ASSIGN : Function.VOTE_OTHER;
		for(final MemberRecord member : members)
		{
			if(getAuthority(member.role, voteFunc)==Authority.CAN_DO)
				voters++;
		}
		return voters;
	}

	@Override
	public List<Integer> getTopRankedRoles(final Function func)
	{
		final List<ClanPosition> allRoles=new LinkedList<ClanPosition>();
		for(final ClanPosition pos : govt().getPositions())
		{
			if((func==null)||(pos.getFunctionChart()[func.ordinal()]!=Authority.CAN_NOT_DO))
				allRoles.add(pos);
		}
		final List<Integer> roleIDs=new LinkedList<Integer>();
		int topRank=Integer.MAX_VALUE;
		for(final ClanPosition pos : allRoles)
		{
			if(pos.getRank() < topRank)
				topRank=pos.getRank();
		}
		for(final ClanPosition pos : allRoles)
		{
			if(pos.getRank() == topRank)
				roleIDs.add(Integer.valueOf(pos.getRoleID()));
		}
		return roleIDs;
	}

	@Override
	public int getNumberRoles()
	{
		return govt().getPositions().length;
	}

	@Override
	public int getTopQualifiedRoleID(final Function func, final MOB mob)
	{
		if(mob==null)
			return govt().getAutoRole();
		ClanPosition topPos = null;
		for(final ClanPosition pos : govt().getPositions())
		{
			if(canBeAssigned(mob,pos.getRoleID())
			&&((topPos==null)||(pos.getRank() < topPos.getRank()))
			&&((func==null)||(getAuthority(pos.getRoleID(),func)!=Authority.CAN_NOT_DO)))
				topPos = pos;
		}
		if(topPos == null)
			return govt().getAutoRole();
		return topPos.getRoleID();
	}

	@Override
	public int getRoleFromName(String position)
	{
		position=position.toUpperCase().trim();
		for(final ClanPosition pos : govt().getPositions())
		{
			if(pos.getID().equalsIgnoreCase(position)
			||pos.getName().equalsIgnoreCase(position)
			||pos.getPluralName().equalsIgnoreCase(position))
				return pos.getRoleID();
		}
		for(final ClanPosition pos : govt().getPositions())
		{
			if(pos.getID().toUpperCase().startsWith(position)
			||pos.getName().toUpperCase().equalsIgnoreCase(position))
				return pos.getRoleID();
		}
		for(final ClanPosition pos : govt().getPositions())
		{
			if((pos.getID().toUpperCase().indexOf(position)>0)
			||(pos.getName().toUpperCase().indexOf(position)>0))
				return pos.getRoleID();
		}
		return -1;
	}

	@Override
	public boolean isPubliclyListedFor(final MOB mob)
	{
		if((!govt().isPublic())&&(mob.getClanRole(clanID())==null))
			return false;
		return true;
	}

	@Override
	public String[] getRolesList()
	{
		final List<String> roleNames=new LinkedList<String>();
		for(final ClanPosition pos : govt().getPositions())
			roleNames.add(pos.getName());
		return roleNames.toArray(new String[0]);
	}

	@Override
	public int getMostInRole(final int roleID)
	{
		if((roleID<0)||(roleID>=govt().getPositions().length))
			return 0;
		final double most = govt().getPositions()[roleID].getMax();
		if(most >= 1.0)
			return (int)Math.round(most);
		if(most <= 0)
			return 1;
		final double rawMost = CMath.mul(getSize(), most);
		if(rawMost < 1.0)
			return 1;
		return (int)Math.round(rawMost);
	}

	@Override
	public String getRoleName(final int roleID, final boolean titleCase, final boolean plural)
	{
		if((roleID<0)||(roleID>=govt().getPositions().length))
			return "";
		final ClanPosition pos=govt().getPositions()[roleID];
		if(plural)
		{
			if(!titleCase)
				return pos.getPluralName().toLowerCase();
			else
				return CMStrings.capitalizeAndLower(pos.getPluralName());
		}
		if(!titleCase)
			return pos.getName().toLowerCase();
		else
			return CMStrings.capitalizeAllFirstLettersAndLower(pos.getName());
	}

	protected boolean isSafeFromPurge()
	{
		if(getMinClanMembers()<=0)
			return true;
		final List<String> protectedOnes=Resources.getFileLineVector(Resources.getFileResource("protectedplayers.ini",false));
		if((protectedOnes!=null)&&(protectedOnes.size()>0))
		{
			for(int b=0;b<protectedOnes.size();b++)
			{
				final String B=protectedOnes.get(b);
				if(B.equalsIgnoreCase(clanID()))
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(tickID!=Tickable.TICKID_CLAN)
			return true;
		if(CMSecurity.isDisabled(CMSecurity.DisFlag.CLANTICKS))
			return true;

		synchronized(this)
		{
			tickUp++;
		}

		if(lastPropsReload < CMProps.getLastResetTime())
		{
			lastPropsReload=CMProps.getLastResetTime();
			this.overrideMinClanMembers=null;
		}

		try
		{
			if((tickUp % CMProps.getTicksPerMudHour())==0)
			{
				int onlineMembers=0;
				for(final Iterator<Session> s=CMLib.sessions().sessions();s.hasNext();)
				{
					final Session S=s.next();
					if(S!=null)
					{
						final MOB M=S.mob();
						if((M!=null)
						&&(M.getClanRole(clanID())!=null))
							onlineMembers++;
					}
				}
				final long ellapsedMs = System.currentTimeMillis() - this.lastClanTickMs;
				final int playerMinutes = (int)((onlineMembers * ellapsedMs) / (1000 * 60));
				bumpTrophyData(Trophy.MonthlyPlayerMinutes, playerMinutes);
				bumpTrophyData(Trophy.PlayerMinutes, playerMinutes);
				this.lastClanTickMs = System.currentTimeMillis();
			}

			// only do the following once per rl day
			if(tickUp < CMProps.getTicksPerDay())
			{
				return true;
			}
			tickUp = 0;

			final List<FullMemberRecord> members=getFullMemberList();
			int activeMembers=0;
			final long deathMilis=CMProps.getIntVar(CMProps.Int.DAYSCLANDEATH)*CMProps.getIntVar(CMProps.Int.TICKSPERMUDDAY)*CMProps.getTickMillis();
			final long overthrowMilis=CMProps.getIntVar(CMProps.Int.DAYSCLANOVERTHROW)*CMProps.getIntVar(CMProps.Int.TICKSPERMUDDAY)*CMProps.getTickMillis();

			for(final FullMemberRecord member : members)
			{
				final long lastLogin=member.lastActiveTimeMs;
				if(((System.currentTimeMillis()-lastLogin)<deathMilis)||(deathMilis==0))
					activeMembers++;
			}
			final int minimumMembers = getMinClanMembers();
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.CLANS))
				Log.debugOut("DefaultClan","("+clanID()+"): "+activeMembers+"/"+minimumMembers+" active members.");
			if(activeMembers<minimumMembers)
			{
				if(!isSafeFromPurge())
				{
					final long duration=(3L * 24L * 60L * 60L * 1000L);
					if((System.currentTimeMillis() - this.lastStatusChange)>duration)
					{
						if(getStatus()==CLANSTATUS_FADING)
						{
							Log.sysOut("Clans","Clan '"+getName()+" deleted with only "+activeMembers+" having logged on lately.");
							destroyClan();
							final StringBuffer buf=new StringBuffer("");
							for(final FullMemberRecord member : members)
								buf.append(member.name+" on "+CMLib.time().date2String(member.lastActiveTimeMs)+"  ");
							Log.sysOut("Clans","Clan '"+getName()+" had the following membership: "+buf.toString());
							return true;
						}
						setStatus(CLANSTATUS_FADING);
						final List<Integer> topRoles=getTopRankedRoles(Function.ASSIGN);
						for(final MemberRecord member : members)
						{
							final String name = member.name;
							final int role=member.role;
							//long lastLogin=((Long)members.elementAt(j,3)).longValue();
							if(topRoles.contains(Integer.valueOf(role)))
							{
								if(CMLib.players().playerExists(name))
								{
									CMLib.smtp().emailIfPossible("AutoPurge",CMStrings.capitalizeAndLower(name),"AutoPurge: "+name(),
											""+getGovernmentName()+" "+name()+" is in danger of being deleted if at least "+(minimumMembers-activeMembers)
											+" members do not log on within 24 hours.");
								}
							}
						}

						Log.sysOut("Clans","Clan '"+getName()+"' fading with only "+activeMembers+" having logged on lately.  Will purge on "+CMLib.time().date2String(this.lastStatusChange)+duration);
						clanAnnounce(""+getGovernmentName()+" "+name()+" is in danger of being deleted if more members do not log on within 24 hours.");
						update();
					}
				}
				else
				if(getStatus()!=CLANSTATUS_ACTIVE)
				{
					setStatus(CLANSTATUS_ACTIVE);
					update();
				}
			}
			else
			switch(getStatus())
			{
			case CLANSTATUS_FADING:
				setStatus(CLANSTATUS_ACTIVE);
				clanAnnounce(""+getGovernmentName()+" "+name()+" is no longer in danger of being deleted.  Be aware that there is required activity level.");
				update();
				break;
			case CLANSTATUS_PENDING:
				setStatus(CLANSTATUS_ACTIVE);
				Log.sysOut("Clans",""+getGovernmentName()+" '"+getName()+" now active with "+activeMembers+".");
				clanAnnounce(""+getGovernmentName()+" "+name()+" now has sufficient members.  The "+getGovernmentName()+" is now fully approved.");
				update();
				break;
			default:
				break;
			}

			// handle any necessary promotions
			if(govt().getAutoPromoteBy() != AutoPromoteFlag.NONE)
			{
				// first step is to figure out which positions need filling
				// 1. if at least one position can assign others, then the highest of those is the answer.
				// 2. if none can assign others, than each over the applicants is the answer

				// Algorithm:
				// Get all the members who qualify by their last login time
				// Sort them by the qualifying criteria
				// for each available position, either fill holes (no overwrite), or move them (overwrite)
				final List<FullMemberRecord> highestQualifiedMembers = new LinkedList<FullMemberRecord>();
				for(final FullMemberRecord member : members)
				{
					if((member.role<0)||(member.role>=govt().getPositions().length))
						continue;
					// checking if they are only an applicant
					if((member.role == govt().getAutoRole())
					&&(govt().getAcceptPos() != govt().getAutoRole())
					&&(govt().getAutoRole() >= 0)
					&&(govt().getAcceptPos() >= 0)
					&&(getTopRankedRoles(Function.ACCEPT).size()>0))
						continue;
					final ThinPlayer M = CMLib.database().getThinUser(member.name);
					if(M==null)
						continue;
					if((((System.currentTimeMillis()-member.lastActiveTimeMs)<overthrowMilis)||(overthrowMilis==0))
					&&(!highestQualifiedMembers.contains(member)))
						highestQualifiedMembers.add(member);
				}
				if(highestQualifiedMembers.size()==0)
				{
					for(final FullMemberRecord member : members)
					{
						if((member.role<0)||(member.role>=govt().getPositions().length))
							member.role=0;
						final ThinPlayer M = CMLib.database().getThinUser(member.name);
						if(M==null)
							continue;
						if((((System.currentTimeMillis()-member.lastActiveTimeMs)<overthrowMilis)||(overthrowMilis==0))
						&&(!highestQualifiedMembers.contains(member)))
							highestQualifiedMembers.add(member);
					}
				}
				if(highestQualifiedMembers.size()==0)
				{
					for(final FullMemberRecord member : members)
					{
						if((member.role<0)||(member.role>=govt().getPositions().length))
							member.role=0;
						final ThinPlayer M = CMLib.database().getThinUser(member.name);
						if(M==null)
							continue;
						highestQualifiedMembers.add(member);
					}
				}

				// now sort them.
				AutoPromoteFlag basePromoteBy = govt().getAutoPromoteBy();
				boolean overWrite = false;
				// Now, sort the members by the qualifying criteria
				switch(basePromoteBy)
				{
				case LEVEL_OVERWRITE:
					overWrite=true;
					basePromoteBy=AutoPromoteFlag.LEVEL;
					//$FALL-THROUGH$
				case LEVEL:
					Collections.sort(highestQualifiedMembers,new Comparator<FullMemberRecord>()
					{
						@Override
						public int compare(final FullMemberRecord o1, final FullMemberRecord o2)
						{
							if(o2.level==o1.level)
								return 0;
							if(o2.level<o1.level)
								return -1;
							return 1;
						}
					});
					break;
				case RANK_OVERWRITE:
					overWrite=true;
					basePromoteBy=AutoPromoteFlag.RANK;
					//$FALL-THROUGH$
				case RANK:
					Collections.sort(highestQualifiedMembers,new Comparator<MemberRecord>()
					{
						@Override
						public int compare(final MemberRecord o1, final MemberRecord o2)
						{
							final ClanPosition cp1 = govt().getPositions()[o1.role];
							final ClanPosition cp2 = govt().getPositions()[o2.role];
							if(cp1.getRank()==cp2.getRank())
								return 0;
							if(cp1.getRank()<cp2.getRank())
								return -1;
							return 1;
						}
					});
					break;
				case GOLD_OVERWRITE:
					overWrite=true;
					basePromoteBy=AutoPromoteFlag.GOLD;
					//$FALL-THROUGH$
				case GOLD:
					Collections.sort(highestQualifiedMembers,new Comparator<MemberRecord>()
					{
						@Override
						public int compare(final MemberRecord o1, final MemberRecord o2)
						{
							if(o2.donatedGold==o1.donatedGold)
								return 0;
							if(o2.donatedGold<o1.donatedGold)
								return -1;
							return 1;
						}
					});
					break;
				case XP_OVERWRITE:
					overWrite=true;
					basePromoteBy=AutoPromoteFlag.XP;
					//$FALL-THROUGH$
				case XP:
					Collections.sort(highestQualifiedMembers,new Comparator<MemberRecord>()
					{
						@Override
						public int compare(final MemberRecord o1, final MemberRecord o2)
						{
							if(o2.donatedXP==o1.donatedXP)
								return 0;
							if(o2.donatedXP<o1.donatedXP)
								return -1;
							return 1;
						}
					});
					break;
				case JOINDATE_OVERWRITE:
					overWrite=true;
					basePromoteBy=AutoPromoteFlag.JOINDATE;
					//$FALL-THROUGH$
				case JOINDATE:
					Collections.sort(highestQualifiedMembers,new Comparator<MemberRecord>()
					{
						@Override
						public int compare(final MemberRecord o1, final MemberRecord o2)
						{
							if(o1.joinDate==o2.joinDate)
								return 0;
							if(o1.joinDate<o2.joinDate)
								return -1;
							return 1;
						}
					});
					break;
				case NONE:
					break;
				default:
					break;
				}

				// now get all the positions we need to fill
				// if a position can assign, then that's the winner(s)
				final List<Integer> highPositionList = getTopRankedRoles(Function.ASSIGN);
				if(highPositionList.size()==0)
				{
					// otherwise, every position that's not an applicant is the winner
					for(final ClanPosition pos : govt().getPositions())
					{
						if(((pos.getRoleID() == govt().getAutoRole())||(pos.getRoleID() == govt().getAcceptPos()))
						&&(govt().getAcceptPos() != govt().getAutoRole())
						&&(govt().getAutoRole() >= 0)
						&&(govt().getAcceptPos() >= 0)
						&&(getTopRankedRoles(Function.ACCEPT).size()>0))
							continue;
						highPositionList.add(Integer.valueOf(pos.getRoleID()));
					}
				}
				Collections.sort(highPositionList, new Comparator<Integer>()
				{
					@Override
					public int compare(final Integer o1, final Integer o2)
					{
						final ClanPosition pos1=govt().getPositions()[o1.intValue()];
						final ClanPosition pos2=govt().getPositions()[o2.intValue()];
						if(pos1.getRank()==pos2.getRank())
							return 0;
						if(pos1.getRank()<pos2.getRank())
							return -1;
						return 1;
					}
				});

				final HashMap<String,Reference<ThinPlayer>> thinCache = new HashMap<String,Reference<ThinPlayer>>();
				final boolean fillAll = getTopRankedRoles(Function.ASSIGN).size()==0;
				// finally we fill the positions
				// if we are overwriting, then we always pick the best people who fit and
				// kick out the rest.  If we are NOT overwriting, then we are only filling
				// holes for when inactivity drops one out.
				final Map<FullMemberRecord,Integer> finalHighRollers = new HashMap<FullMemberRecord,Integer>();
				for(final Integer highPosI : highPositionList)
				{
					final int highRoleID = highPosI.intValue();
					int most=getMostInRole(highRoleID);
					if(most>getSize())
						most=getSize();
					int numToAdd = 0;
					if(!overWrite)
					{
						int current = 0;
						for(final Iterator<FullMemberRecord> i=highestQualifiedMembers.iterator();i.hasNext();)
						{
							final FullMemberRecord M=i.next();
							if(M.role == highRoleID)
							{
								i.remove();
								finalHighRollers.put(M, highPosI);
								current++;
							}
						}
						numToAdd = most-current;
						if(!fillAll)
						{
							if(current>0)
								numToAdd=0;
							else
							if(numToAdd>0)
								numToAdd=1;
						}
					}
					else
					{
						numToAdd = most;
						if(!fillAll)
						{
							for(final Iterator<FullMemberRecord> i=highestQualifiedMembers.iterator();i.hasNext();)
							{
								final FullMemberRecord M=i.next();
								if(M.role == highRoleID)
								{
									i.remove();
									finalHighRollers.put(M, highPosI);
									numToAdd=0;
								}
							}
							if(numToAdd>0)
								numToAdd=1;
						}
					}
					for(int i=0;i<numToAdd;i++)
					{
						for(int s=0;s<highestQualifiedMembers.size();s++)
						{
							final FullMemberRecord M=highestQualifiedMembers.get(s);
							if(!thinCache.containsKey(M.name))
								thinCache.put(M.name, new WeakReference<ThinPlayer>(CMLib.database().getThinUser(M.name)));
							final ThinPlayer tP=thinCache.get(M.name).get();
							if((tP!=null)
							&&(canBeAssigned(tP, highRoleID)))
							{
								highestQualifiedMembers.remove(s);
								finalHighRollers.put(M, highPosI);
								break;
							}
						}
					}
				}
				final ClanPosition acceptRole=govt().getPositions()[govt().getAcceptPos()];
				final Map<FullMemberRecord,Integer> finalRollers = finalHighRollers;
				for(final FullMemberRecord member : members)
				{
					if(highPositionList.contains(Integer.valueOf(member.role))
					&&(!finalRollers.containsKey(member)))
					{
						boolean someoneIsTakingMyPlace = false;
						for(final FullMemberRecord R : finalRollers.keySet())
						{
							if((finalRollers.get(R).intValue() == member.role)
							&&(R.role != member.role))
								someoneIsTakingMyPlace=true;
						}
						if(someoneIsTakingMyPlace)
							finalRollers.put(member, Integer.valueOf(acceptRole.getRoleID()));
					}
				}

				for(final FullMemberRecord member : members)
				{
					if(finalRollers.containsKey(member))
					{
						final Integer newRoleI = finalRollers.get(member);
						if(newRoleI.intValue() != member.role)
						{
							final ClanPosition oldPos = govt().getPositions()[member.role];
							final ClanPosition newPos = govt().getPositions()[newRoleI.intValue()];

							final MOB mob=CMLib.players().getPlayerAllHosts(member.name);
							clanAnnounce(member.name+" is now a "+newPos.getName()+" of the "+getGovernmentName()+" "+name()+".");
							if(oldPos.getRank() < newPos.getRank())
								Log.sysOut("Clans",member.name+" of "+getGovernmentName()+" "+name()+" was auto-demoted to "+newPos.getName()+".");
							else
							if(oldPos.getRank() > newPos.getRank())
								Log.sysOut("Clans",member.name+" of "+getGovernmentName()+" "+name()+" was auto-promoted to "+newPos.getName()+".");
							else
								Log.sysOut("Clans",member.name+" of "+getGovernmentName()+" "+name()+" was auto-assigned to "+newPos.getName()+".");
							if((mob!=null)
							&&(mob.getClanRole(clanID())!=null))
								mob.setClan(clanID(),newPos.getRoleID());
							member.role=newPos.getRoleID();
							CMLib.database().DBUpdateClanMembership(member.name, name(), newPos.getRoleID());
						}
					}
				}

				final List<MemberRecord> highMembers=new LinkedList<MemberRecord>();
				for(final FullMemberRecord member : members)
				{
					if((((System.currentTimeMillis()-member.lastActiveTimeMs)<deathMilis)||(deathMilis==0))
					&&(highPositionList.contains(Integer.valueOf(member.role))))
						highMembers.add(member);
				}

				if(highMembers.size()==0)
				{
					if(!isSafeFromPurge())
					{
						Log.sysOut("Clans","Clan '"+getName()+" deleted for lack of leadership.");
						destroyClan();
						final StringBuffer buf=new StringBuffer("");
						for(final FullMemberRecord member : members)
							buf.append(member.name+" on "+CMLib.time().date2String(member.lastActiveTimeMs)+"  ");
						Log.sysOut("Clans","Clan '"+getName()+" had the following membership: "+buf.toString());
						return true;
					}
				}
			}

			boolean anyVoters = false;
			for(final ClanPosition pos : govt().getPositions())
			{
				if((pos.getFunctionChart()[Function.VOTE_ASSIGN.ordinal()]==Clan.Authority.CAN_DO)
				||(pos.getFunctionChart()[Function.VOTE_OTHER.ordinal()]==Clan.Authority.CAN_DO))
					anyVoters=true;
			}

			// now do votes
			if(anyVoters&&(votes()!=null))
			{
				boolean updateVotes=false;
				final Vector<ClanVote> votesToRemove=new Vector<ClanVote>();
				long duration=govt().getMaxVoteDays();
				if(duration<=0)
					duration=54;
				duration=duration*CMProps.getIntVar(CMProps.Int.TICKSPERMUDDAY)*CMProps.getTickMillis();
				for(final Enumeration<ClanVote> e=votes();e.hasMoreElements();)
				{
					final ClanVote CV=e.nextElement();
					final int numVotes=getNumVoters(Function.values()[CV.function]);
					int quorum=govt().getVoteQuorumPct();
					quorum=(int)Math.round(CMath.mul(CMath.div(quorum,100.0),numVotes));
					if(quorum<2)
						quorum=2;
					if(numVotes==1)
						quorum=1;
					final long endsOn=CV.voteStarted+duration;
					if(CV.voteStatus==VSTAT_STARTED)
					{
						if(CV.votes==null)
							CV.votes=new PairVector<String,Boolean>();
						boolean voteIsOver=false;
						if(System.currentTimeMillis()>endsOn)
							voteIsOver=true;
						else
						if(CV.votes.size()==numVotes)
							voteIsOver=true;
						if(voteIsOver)
						{
							CV.voteStarted=System.currentTimeMillis();
							updateVotes=true;
							if(CV.votes.size()<quorum)
								CV.voteStatus=VSTAT_FAILED;
							else
							{
								int yeas=0;
								int nays=0;
								for(int i=0;i<CV.votes.size();i++)
								{
									if(CV.votes.getSecond(i).booleanValue())
										yeas++;
									else
										nays++;
								}
								if(yeas<=nays)
									CV.voteStatus=VSTAT_FAILED;
								else
								{
									CV.voteStatus=VSTAT_PASSED;
									final MOB mob=CMClass.getFactoryMOB();
									mob.setName(clanID());
									mob.setClan(clanID(),getTopRankedRoles(Function.values()[CV.function]).get(0).intValue());
									mob.basePhyStats().setLevel(1000);
									if(mob.location()==null)
									{
										mob.setLocation(mob.getStartRoom());
										if(mob.location()==null)
											mob.setLocation(CMLib.map().getRandomRoom());
									}
									final Vector<String> V=CMParms.parse(CV.matter);
									mob.doCommand(V,MUDCmdProcessor.METAFLAG_FORCED);
									mob.destroy();
								}
							}
						}
					}
					else
					if(System.currentTimeMillis()>endsOn)
					{
						updateVotes=true;
						votesToRemove.addElement(CV);
					}
				}
				for(int v=0;v<votesToRemove.size();v++)
					delVote(votesToRemove.elementAt(v));
				if(updateVotes)
					updateVotes();
			}

			update(); // also saves exp, and trophies
			CMLib.database().DBUpdateClanItems(this);
		}
		catch(final Exception x2)
		{
			Log.errOut("Clans",x2);
		}
		return true;
	}

	@Override
	public boolean doesOutRank(final int highRoleID, final int lowRoleID)
	{
		final ClanGovernment govt=govt();
		if((highRoleID == lowRoleID)
		||(highRoleID < 0)
		||(highRoleID >= govt.getPositions().length))
			return false;
		if((lowRoleID<0)
		||(lowRoleID >= govt.getPositions().length))
			return true;
		return govt.getPositions()[highRoleID].getRank() < govt.getPositions()[lowRoleID].getRank();
	}

	@Override
	public void clanAnnounce(final String msg)
	{
		if(channelSet.size()==0)
		{
			synchronized(channelSet)
			{
				if(channelSet.size()==0)
					channelSet.add(new Pair<Clan,Integer>(this,Integer.valueOf(getGovernment().getAcceptPos())));
			}
		}
		final List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.CLANINFO, null);
		for(int i=0;i<channels.size();i++)
			CMLib.commands().postChannel(channels.get(i),channelSet,msg,true);
	}

	private static final SearchIDList<Ability> emptyAbles =new CMUniqSortSVec<Ability>(1);

	@Override
	public SearchIDList<Ability> clanAbilities(final MOB mob)
	{
		final Pair<Clan,Integer> p=(mob!=null)?mob.getClanRole(clanID()):null;
		if((mob==null)||((p!=null)&&(getAuthority(p.second.intValue(),Function.CLAN_BENEFITS)!=Clan.Authority.CAN_NOT_DO)))
			return govt().getClanLevelAbilities(mob,this,Integer.valueOf(getClanLevel()));
		return emptyAbles;
	}

	@Override
	public int numClanEffects(final MOB mob)
	{
		return govt().getClanLevelEffects(mob, this, Integer.valueOf(getClanLevel())).size();
	}

	@Override
	public ChameleonList<Ability> clanEffects(final MOB mob)
	{
		return govt().getClanLevelEffects(mob,this, Integer.valueOf(getClanLevel()));
	}

	@Override
	public int applyExpMods(final MOB memberM, int exp)
	{
		boolean changed=false;
		if((getTaxes()>0.0)&&(exp>1))
		{
			final int clanshare=(int)Math.round(CMath.mul(exp,getTaxes()));
			if(clanshare>0)
			{
				exp-=clanshare;
				adjExp(memberM, clanshare);
				changed=true;
			}
		}

		// player xp punished for taxes, but not awarded for trophies
		bumpTrophyData(Trophy.MonthlyPlayerXP, exp);

		if(getTrophies() != 0)
		{
			for(final Trophy t : Trophy.values())
			{
				if(CMath.bset(getTrophies(),t.flagNum()))
					exp = CMLib.clans().adjustXPAward(t, exp);
			}
		}


		if(changed)
			update();
		return exp;
	}

	@Override
	public MOB getResponsibleMember()
	{
		final List<MemberRecord> members=getMemberList();
		final List<Integer> topRoles=getTopRankedRoles(null);
		MOB respMember = null;
		final int level = -1;
		for(final MemberRecord member : members)
		{
			if(topRoles.contains(Integer.valueOf(member.role)))
			{
				final MOB M=CMLib.players().getLoadPlayer(member.name);
				if((M!=null)&&(M.basePhyStats().level() > level))
					respMember = M;
			}
		}
		if(respMember != null)
			return respMember;
		String memberName = null;
		ClanPosition newPos=null;
		for(final MemberRecord member : members)
		{
			if((member.role<govt().getPositions().length)
			&&(member.role>=0)
			&&((newPos==null)||(govt().getPositions()[member.role].getRank()<newPos.getRank())))
			{
				newPos = govt().getPositions()[member.role];
				memberName = member.name;
			}
		}
		if(memberName != null)
			return CMLib.players().getLoadPlayer(memberName);
		return null;
	}

	@Override
	public ItemCollection getExtItems()
	{
		return extItems;
	}

	/** Manipulation of the tatoo list */
	@Override
	public void addTattoo(final String of)
	{
		final Tattoo T=(Tattoo)CMClass.getCommon("DefaultTattoo");
		addTattoo(T.set(of));
	}

	@Override
	public void addTattoo(final String of, final int tickDown)
	{
		final Tattoo T=(Tattoo)CMClass.getCommon("DefaultTattoo");
		addTattoo(T.set(of,tickDown));
	}

	@Override
	public void delTattoo(final String of)
	{
		final Tattoo T=findTattoo(of);
		if(T!=null)
			tattoos.remove(T);
	}

	@Override
	public void addTattoo(final Tattoo of)
	{
		if ((of == null) || (of.getTattooName() == null) || (of.getTattooName().length() == 0) || findTattoo(of.getTattooName()) != null)
			return;
		tattoos.addElement(of);
	}

	@Override
	public void delTattoo(final Tattoo of)
	{
		if ((of == null) || (of.getTattooName() == null) || (of.getTattooName().length() == 0))
			return;
		final Tattoo tat = findTattoo(of.getTattooName());
		if (tat == null)
			return;
		tattoos.remove(tat);
	}

	@Override
	public Enumeration<Tattoo> tattoos()
	{
		return tattoos.elements();
	}

	@Override
	public Tattoo findTattoo(final String of)
	{
		if ((of == null) || (of.length() == 0))
			return null;
		return tattoos.find(of.trim());
	}

	@Override
	public Tattoo findTattooStartsWith(final String of)
	{
		if ((of == null) || (of.length() == 0))
			return null;
		return tattoos.findStartsWith(of.trim());
	}

	@Override
	public void killAchievementTracker(final Achievement A, final Tattooable C, final MOB mob)
	{
		if(achievementers.containsKey(A.getTattoo()))
		{
			achievementers.remove(A.getTattoo());
		}
	}

	@Override
	public Tracker getAchievementTracker(final Achievement A, final Tattooable C, final MOB mob)
	{
		final Tracker T;
		if(achievementers.containsKey(A.getTattoo()))
		{
			T=achievementers.get(A.getTattoo());
		}
		else
		{
			T=A.getTracker(0);
			achievementers.put(A.getTattoo(), T);
		}
		return T;
	}

	@Override
	public void rebuildAchievementTracker(final Tattooable C, final MOB mob, final String achievementTattoo)
	{
		final Achievement A=CMLib.achievements().getAchievement(achievementTattoo);
		if(A!=null)
		{
			if(achievementers.containsKey(A.getTattoo()))
				achievementers.put(A.getTattoo(), A.getTracker(achievementers.get(A.getTattoo()).getCount(C)));
			else
				achievementers.put(A.getTattoo(), A.getTracker(0));
		}
		else
			achievementers.remove(achievementTattoo);
	}

	/** Stat variables associated with clan objects. */
	private final static String[] CLAN_STATS={
		"ACCEPTANCE", // 0
		"DETAIL", // 1
		"DONATEROOM", // 2
		"EXP", // 3
		"GOVT", // 4
		"MORGUE", // 5
		"POLITICS", // 6
		"PREMISE", // 7
		"RECALL", // 8
		"SIZE", // 9
		"STATUS", // 10
		"TAXES", // 11
		"TROPHIES", // 12
		"TYPE", // 13
		"AREAS", // 14
		"MEMBERLIST", // 15
		"TOPMEMBER", // 16
		"CLANLEVEL", // 17
		"CATEGORY", // 18
		"RIVALROUS",//19
		"MINMEMBERS", //20
		"CLANCHARCLASS", // 21
		"NAME", // 22
		"FLAGS" // 23
	};

	@Override
	public String[] getStatCodes()
	{
		return CLAN_STATS;
	}

	@Override
	public int getSaveStatIndex()
	{
		return CLAN_STATS.length;
	}

	@Override
	public boolean isStat(final String code)
	{
		return CMParms.indexOf(getStatCodes(), code.toUpperCase().trim()) >= 0;
	}

	@Override
	public String getStat(final String code)
	{
		final int dex=CMParms.indexOf(getStatCodes(),code.toUpperCase().trim());
		if(dex<0)
			return "";
		switch(dex)
		{
		case 0:
			return getAcceptanceSettings();
		case 1:
			return getDetail(null);
		case 2:
			return getDonation();
		case 3:
			return "" + getExp();
		case 4:
			return "" + getGovernmentName();
		case 5:
			return getMorgue();
		case 6:
			return getDataXML();
		case 7:
			return getPremise();
		case 8:
			return getRecall();
		case 9:
			return "" + getSize();
		case 10:
			return Clan.CLANSTATUS_DESC[getStatus()];
		case 11:
			return "" + getTaxes();
		case 12:
			return "" + getTrophies();
		case 13:
			return "0";
		case 14:
		{
			final List<Area> areas = getControlledAreas();
			final StringBuffer list = new StringBuffer("");
			for (int i = 0; i < areas.size(); i++)
				list.append("\"" + areas.get(i).name() + "\" ");
			return list.toString().trim();
		}
		case 15:
		{
			final List<MemberRecord> members = getMemberList();
			final StringBuffer list = new StringBuffer("");
			for (final MemberRecord member : members)
				list.append("\"" + member.name + "\" ");
			return list.toString().trim();
		}
		case 16:
		{
			final MOB M = getResponsibleMember();
			if (M != null)
				return M.Name();
			return "";
		}
		case 17:
			return Integer.toString(getClanLevel());
		case 18:
			return "" + getCategory();
		case 19:
			return "" + isRivalrous();
		case 20:
			return "" + getMinClanMembers();
		case 21:
			return "" + getClanClass();
		case 22:
			return "" + getName();
		case 23:
			return CMParms.toListString(clanFlags);
		}
		return "";
	}

	@Override
	public void setStat(final String code, final String val)
	{
		final int dex=CMParms.indexOf(getStatCodes(),code.toUpperCase().trim());
		if(dex<0)
			return;
		switch(dex)
		{
		case 0:
			setAcceptanceSettings(val);
			break;
		case 1:
			break; // detail
		case 2:
			setDonation(val);
			break;
		case 3:
			setExp(CMath.s_long(val.trim()));
			break;
		case 4:
			setGovernmentID(CMath.s_int(val.trim()));
			break;
		case 5:
			setMorgue(val);
			break;
		case 6:
			setDataXML(val);
			break;
		case 7:
			setPremise(val);
			break;
		case 8:
			setRecall(val);
			break;
		case 9:
			break; // size
		case 10:
			if(CMath.s_int(val.trim())!=getStatus())
				setStatus(CMath.s_int(val.trim()));
			break;
		case 11:
			setTaxes(CMath.s_double(val.trim()));
			break;
		case 12:
			setTrophies(CMath.s_int(val.trim()));
			break;
		case 13:
			break; // type
		case 14:
			break; // areas
		case 15:
			break; // memberlist
		case 16:
			break; // topmember
		case 17:
			setClanLevel(CMath.s_int(val.trim()));
			break; // clanlevel
		case 18:
			setCategory(val.trim());
			break; // clancategory
		case 19:
			setRivalrous(CMath.s_bool(val.trim()));
			break; // isrivalrous
		case 20:
			setMinClanMembers(CMath.s_int(val.trim()));
			break; // minmembers
		case 21:
			setClanClass(val.trim());
			break; // clancharclass
		case 22:
			this.setName(val.trim());
			break; // name
		case 23:
		{
			clanFlags = new SHashSet<ClanFlag>();
			for(final String s : CMParms.parseCommas(val.toUpperCase(),true))
			{
				final ClanFlag flag = (ClanFlag)CMath.s_valueOf(ClanFlag.class, s);
				if(flag != null)
					clanFlags.add(flag);
			}
			break;
		}
		}
	}
}
