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
import com.planet_ink.coffee_mud.Common.interfaces.Clan.Function;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.Authority;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.ClanVote;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.MemberRecord;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.PlayerData;
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.ForumJournal;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/**
 * Portions Copyright (c) 2003 Jeremy Vyska
 * Portions Copyright (c) 2004-2018 Bo Zimmerman
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

	protected String 			clanName="";
	protected String			clanCategory=null;
	protected String 			clanPremise="";
	protected String 			clanRecall="";
	protected String 			clanMorgue="";
	protected String 			clanClass="";
	protected int	 			clanLevel=0;
	protected String 			clanDonationRoom="";
	protected int	 			clanTrophies=0;
	protected Boolean			isRivalrous=null;
	protected int	 			autoPosition=-1;
	protected String 			acceptanceSettings="";
	protected int 	 			clanStatus=0;
	protected String 			lastClanKillRecord=null;
	protected double 			taxRate=0.0;
	protected volatile long 	exp=0;
	protected Object 			expSync = new Object();
	protected List<ClanVote> 	voteList=null;
	protected List<Long> 		clanKills=new Vector<Long>();
	protected Integer			overrideMinClanMembers=null;
	protected long				lastPropsReload=System.currentTimeMillis();
	protected ItemCollection	extItems = (ItemCollection)CMClass.getCommon("WeakItemCollection");
	protected Map<String,long[]>relations=new Hashtable<String,long[]>();
	protected int 				government=0;
	protected long 				lastGovernmentLoadTime=-1;
	protected ClanGovernment 	govt = null;

	protected final static List<Ability> empty=new XVector<Ability>(1,true);

	protected final List<Pair<Clan,Integer>> channelSet = new XVector<Pair<Clan,Integer>>(1,true);

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
	public int compareTo(CMObject o)
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
		if(lastClanKillRecord==null)
		{
			final List<PlayerData> V=CMLib.database().DBReadPlayerData(clanID(),"CLANKILLS",clanID()+"/CLANKILLS");
			clanKills.clear();
			if(V.size()==0)
				lastClanKillRecord="";
			else
			{
				lastClanKillRecord=V.get(0).xml();
				final List<String> V2=CMParms.parseSemicolons(lastClanKillRecord,true);
				for(int v=0;v<V2.size();v++)
					clanKills.add(Long.valueOf(CMath.s_long(V2.get(v))));
			}
		}
	}

	private void updateClanKills()
	{
		Long date=null;
		final StringBuffer str=new StringBuffer("");
		for(int i=clanKills.size()-1;i>=0;i--)
		{
			date=clanKills.get(i);
			if(date.longValue()<(System.currentTimeMillis()))
				clanKills.remove(i);
			else
				str.append(date.longValue()+";");
		}
		if((lastClanKillRecord==null)||(!lastClanKillRecord.equals(str.toString())))
		{
			lastClanKillRecord=str.toString();
			CMLib.database().DBReCreatePlayerData(clanID(),"CLANKILLS",clanID()+"/CLANKILLS",str.toString());
		}
	}

	@Override
	public void updateVotes()
	{
		final StringBuffer str=new StringBuffer("");
		for(final Enumeration<ClanVote> e=votes();e.hasMoreElements();)
		{
			final ClanVote CV=e.nextElement();
			str.append(CMLib.xml().convertXMLtoTag("BY",CV.voteStarter));
			str.append(CMLib.xml().convertXMLtoTag("FUNC",CV.function));
			str.append(CMLib.xml().convertXMLtoTag("ON",""+CV.voteStarted));
			str.append(CMLib.xml().convertXMLtoTag("STATUS",""+CV.voteStatus));
			str.append(CMLib.xml().convertXMLtoTag("CMD",CV.matter));
			if((CV.votes!=null)&&(CV.votes.size()>0))
			{
				str.append("<VOTES>");
				for(int v=0;v<CV.votes.size();v++)
				{
					str.append("<VOTE>");
					str.append(CMLib.xml().convertXMLtoTag("BY",CV.votes.getFirst(v)));
					str.append(CMLib.xml().convertXMLtoTag("YN",CV.votes.getSecond(v).toString()));
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
	public void addVote(ClanVote CV)
	{
		if(CV==null)
			return;
		votes();
		voteList.add(CV);
	}

	@Override
	public void delVote(ClanVote CV)
	{
		votes();
		voteList.remove(CV);
	}

	@Override
	public void recordClanKill(MOB killer, MOB killed)
	{
		clanKills();
		clanKills.add(Long.valueOf(System.currentTimeMillis()));
		updateClanKills();
		if((killer != null)&&(killed != null))
		{
			if(killed.isMonster())
				CMLib.database().DBUpdateClanKills(this.clanID(), killer.Name(), 1, 0);
			else
				CMLib.database().DBUpdateClanKills(this.clanID(), killer.Name(), 0, 1);
		}
	}

	@Override
	public int getCurrentClanKills(MOB killer)
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
	public long calculateMapPoints(List<Area> controlledAreas)
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
			voteList=new Vector<ClanVote>();
			for(int v=0;v<V.size();v++)
			{
				final ClanVote CV=new ClanVote();
				final String rawxml=V.get(v).xml();
				if(rawxml.trim().length()==0)
					return new IteratorEnumeration<Clan.ClanVote>(voteList.iterator());
				final List<XMLLibrary.XMLTag> xml=CMLib.xml().parseAllXML(rawxml);
				if(xml==null)
				{
					Log.errOut("Clans","Unable to parse: "+rawxml);
					return new IteratorEnumeration<Clan.ClanVote>(voteList.iterator());
				}
				final List<XMLLibrary.XMLTag> voteData=CMLib.xml().getContentsFromPieces(xml,"BALLOTS");
				if(voteData==null)
				{
					Log.errOut("Clans","Unable to get BALLOTS data.");
					return new IteratorEnumeration<Clan.ClanVote>(voteList.iterator());
				}
				CV.voteStarter=CMLib.xml().getValFromPieces(voteData,"BY");
				CV.voteStarted=CMLib.xml().getLongFromPieces(voteData,"ON");
				CV.function=CMLib.xml().getIntFromPieces(voteData,"FUNC");
				CV.voteStatus=CMLib.xml().getIntFromPieces(voteData,"STATUS");
				CV.matter=CMLib.xml().getValFromPieces(voteData,"CMD");
				CV.votes=new PairVector<String,Boolean>();
				final List<XMLLibrary.XMLTag> xV=CMLib.xml().getContentsFromPieces(voteData,"VOTES");
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
	public void setAutoPosition(int pos)
	{
		if(pos == govt().getAutoRole())
			autoPosition=-1;
		else
			autoPosition=pos;
	}

	@Override
	public void setExp(long newexp)
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
					clanAnnounce(""+getGovernmentName()+" "+name()+" has attained clan level "+getClanLevel()+"!");
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
					clanAnnounce(""+getGovernmentName()+" "+name()+" has reverted to clan level "+getClanLevel()+"!");
					update();
					prevLevelXP = CMath.parseMathExpression(form, new double[]{getClanLevel()-1}, 0.0);
				}
			}
		}
	}

	@Override
	public void adjExp(int howMuch)
	{
		if (howMuch != 0)
			setExp(getExp() + howMuch);
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
	public void setTrophies(int trophyFlag)
	{
		clanTrophies = trophyFlag;
	}

	@Override
	public void setTaxes(double rate)
	{
		taxRate=rate;
	}

	@Override
	public double getTaxes()
	{
		return taxRate;
	}

	@Override
	public int getClanRelations(String id)
	{
		final long i[]=relations.get(id.toUpperCase());
		if(i!=null)
			return (int)i[0];
		return  REL_NEUTRAL;
	}

	@Override
	public long getLastRelationChange(String id)
	{
		final long i[]=relations.get(id.toUpperCase());
		if(i!=null)
			return i[1];
		return 0;
	}

	@Override
	public void setClanRelations(String id, int rel, long time)
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
	public void setGovernmentID(int type)
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
	public void setMinClanMembers(int amt)
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
	public void setCategory(String newCategory)
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
	public void setRivalrous(boolean isRivalrous)
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
	public void addMember(MOB M, int role)
	{
		M.setClan(clanID(),role);
		CMLib.database().DBUpdateClanMembership(M.Name(), clanID(), role);
		updateClanPrivileges(M);
	}

	@Override
	public void delMember(MOB M)
	{
		CMLib.database().DBUpdateClanMembership(M.Name(), clanID(), -1);
		M.setClan(clanID(),-1);
		updateClanPrivileges(M);
	}

	@Override
	public boolean updateClanPrivileges(MOB M)
	{
		boolean did=false;
		if(M==null)
			return false;
		final Pair<Clan,Integer> p=M.getClanRole(clanID());

		if((p!=null) && (getAuthority(p.second.intValue(),Function.CLAN_BENEFITS)!=Clan.Authority.CAN_NOT_DO))
		{
			final CharClass CC=getClanClassC();
			if((CC!=null)
			&&(CC.availabilityCode()!=0)
			&&(M.baseCharStats().getCurrentClass()!=CC))
			{
				M.baseCharStats().setCurrentClass(CC);
				did=true;
				M.recoverCharStats();
			}
		}
		M.delAbility(M.fetchAbility("Spell_ClanHome"));
		M.delAbility(M.fetchAbility("Spell_ClanDonate"));
		M.delAbility(M.fetchAbility("Spell_Flagportation"));

		if(M.playerStats()!=null)
		for(final ClanPosition pos : govt().getPositions())
		{
			final String title="*, "+CMStrings.capitalizeAndLower(pos.getName())+" of "+name();
			String existingTitle=null;
			for(final String titleCheck : M.playerStats().getTitles())
			{
				if(titleCheck.equalsIgnoreCase(title))
					existingTitle=titleCheck;
			}
			if((p!=null)
			&&(p.second.intValue()==pos.getRoleID())
			&&(getAuthority(p.second.intValue(),Function.CLAN_TITLES)!=Clan.Authority.CAN_NOT_DO))
			{
				if(!M.playerStats().getTitles().contains(title))
				{
					if(existingTitle!=null)
						M.playerStats().getTitles().remove(existingTitle);
					M.playerStats().getTitles().add(title);
				}
			}
			else
			if(M.playerStats().getTitles().contains(title))
				M.playerStats().getTitles().remove(title);
			else
			if(existingTitle!=null)
				M.playerStats().getTitles().remove(existingTitle);
		}
		if(p==null)
		{
			Item I=null;
			final Vector<Item> itemsToMove=new Vector<Item>();
			for(int i=0;i<M.numItems();i++)
			{
				I=M.getItem(i);
				if(I instanceof ClanItem)
					itemsToMove.addElement(I);
			}
			for(int i=0;i<itemsToMove.size();i++)
			{
				I=itemsToMove.elementAt(i);
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
					did=true;
				}
			}
		}
		if((did)&&(!CMSecurity.isSaveFlag(CMSecurity.SaveFlag.NOPLAYERS)))
			CMLib.database().DBUpdatePlayer(M);
		return did;
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
	public String getDetail(MOB mob)
	{
		final int COLBL_WIDTH=CMLib.lister().fixColWidth(16.0,mob);
		final StringBuffer msg=new StringBuffer("");
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
				msg.append("^x"+CMStrings.padRight(CMStrings.capitalizeAndLower(topRankedPos.getPluralName()),COLBL_WIDTH)+":^.^N "+crewList(members, topRankedPos.getRoleID())+"\n\r");
				sortedPositions.add(topRankedPos);
			}
		}
		msg.append("^x"+CMStrings.padRight(L("Total Members"),COLBL_WIDTH)+":^.^N "+members.size()+"\n\r");
		if(CMLib.clans().numClans()>1)
		{
			msg.append("-----------------------------------------------------------------\n\r");
			msg.append("^x"+CMStrings.padRight(L("Clan Relations"),COLBL_WIDTH)+":^.^N \n\r");
			for(final Enumeration<Clan> e=CMLib.clans().clans();e.hasMoreElements();)
			{
				final Clan C=e.nextElement();
				if((C!=this)&&(C.isRivalrous()))
				{
					msg.append("^x"+CMStrings.padRight(C.name(),COLBL_WIDTH)+":^.^N ");
					msg.append(CMStrings.capitalizeAndLower(REL_DESCS[getClanRelations(C.clanID())]));
					final int orel=C.getClanRelations(clanID());
					if(orel!=REL_NEUTRAL)
						msg.append(" (<-"+CMStrings.capitalizeAndLower(REL_DESCS[orel])+")");
					msg.append("\n\r");
				}
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
		final Vector<String> control=new Vector<String>();
		final List<Area> controlledAreas=getControlledAreas();
		final long controlPoints=calculateMapPoints(controlledAreas);
		for(final Area A : controlledAreas)
			control.addElement(A.name());
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
				final Area A=CMLib.map().getArea(control.elementAt(i));
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
		if((CMLib.clans().trophySystemActive())&&(getTrophies()!=0))
		{
			msg.append("-----------------------------------------------------------------\n\r");
			msg.append(L("^xTrophies awarded:^.^N\n\r"));
			for(final Trophy t : Trophy.values())
			{
				if(CMath.bset(getTrophies(),t.flagNum()))
				{
					msg.append(t.codeString+" ");
					switch(t)
					{
					case Areas:
						msg.append("(" + control.size() + ") ");
						break;
					case Points:
						msg.append("(" + controlPoints + ") ");
						break;
					case Experience:
						msg.append("(" + getExp() + ") ");
						break;
					case Members:
						msg.append("(" + members.size() + ") ");
						break;
					case PlayerKills:
						msg.append("(" + getCurrentClanKills(null) + ") ");
						break;
					case MemberLevel:
					{
						msg.append("(" + filterMedianLevel(getFullMemberList()) + ") ");
						break;
					}
					}
					msg.append(L(" Prize: @x1\n\r",CMLib.clans().translatePrize(t)));
				}
			}
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
	public boolean canBeAssigned(MOB mob, int role)
	{
		if(mob==null)
			return false;
		if((role<0)||(role>govt().getPositions().length))
			return false;
		final ClanPosition pos = govt().getPositions()[role];
		return CMLib.masking().maskCheck(fixRequirementMask(pos.getInnerMaskStr()), mob, true);
	}

	@Override
	public Authority getAuthority(int roleID, Function function)
	{
		if((roleID<0)||(roleID>=govt().getPositions().length))
			return Authority.CAN_NOT_DO;
		return govt().getPositions()[roleID].getFunctionChart()[function.ordinal()];
	}

	public String fixRequirementMask(final String oldMask)
	{
		if((oldMask==null)||(oldMask.trim().length()==0))
			return "";
		final StringBuffer mask=new StringBuffer(oldMask.trim());
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
	
	protected List<MemberRecord> getRealMemberList(int PosFilter)
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
		return CMLib.database().DBReadClanMembers(clanID()).size(); 
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
	public void setName(String newName)
	{
		clanName = newName;
	}

	@Override
	public String getPremise()
	{
		return clanPremise;
	}

	@Override
	public void setPremise(String newPremise)
	{
		clanPremise = newPremise;
	}

	@Override
	public int getClanLevel()
	{
		return clanLevel;
	}

	@Override
	public void setClanLevel(int newClanLevel)
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
	public void setAcceptanceSettings(String newSettings)
	{
		acceptanceSettings = newSettings;
	}

	@Override
	public String getClanClass()
	{
		return clanClass;
	}

	@Override
	public void setClanClass(String newClass)
	{
		clanClass = newClass;
	}

	@Override
	public String getPolitics()
	{
		final StringBuffer str=new StringBuffer("");
		str.append("<POLITICS>");
		str.append(CMLib.xml().convertXMLtoTag("GOVERNMENT",""+getGovernmentID()));
		str.append(CMLib.xml().convertXMLtoTag("TAXRATE",""+getTaxes()));
		str.append(CMLib.xml().convertXMLtoTag("EXP",""+getExp()));
		str.append(CMLib.xml().convertXMLtoTag("LEVEL",""+getClanLevel()));
		str.append(CMLib.xml().convertXMLtoTag("CCLASS",""+getClanClass()));
		str.append(CMLib.xml().convertXMLtoTag("AUTOPOS",""+getAutoPosition()));
		if(clanCategory!=null)
			str.append(CMLib.xml().convertXMLtoTag("CATE",clanCategory));
		if(overrideMinClanMembers!=null)
			str.append(CMLib.xml().convertXMLtoTag("MINM",overrideMinClanMembers.toString()));
		if(isRivalrous!=null)
			str.append(CMLib.xml().convertXMLtoTag("RIVAL",isRivalrous.toString()));
		if(relations.size()==0)
			str.append("<RELATIONS/>");
		else
		{
			str.append("<RELATIONS>");
			for(final Iterator<String> e=relations.keySet().iterator();e.hasNext();)
			{
				final String key=e.next();
				str.append("<RELATION>");
				str.append(CMLib.xml().convertXMLtoTag("CLAN",key));
				final long[] i=relations.get(key);
				str.append(CMLib.xml().convertXMLtoTag("STATUS",""+i[0]));
				str.append("</RELATION>");
			}
			str.append("</RELATIONS>");
		}
		str.append("</POLITICS>");
		return str.toString();
	}

	@Override
	public void setPolitics(String politics)
	{
		XMLTag piece;
		relations.clear();
		government=0;
		if(politics.trim().length()==0)
			return;
		final List<XMLLibrary.XMLTag> xml=CMLib.xml().parseAllXML(politics);
		if(xml==null)
		{
			Log.errOut("Clans","Unable to parse: "+politics);
			return;
		}
		final List<XMLLibrary.XMLTag> poliData=CMLib.xml().getContentsFromPieces(xml,"POLITICS");
		if(poliData==null)
		{
			Log.errOut("Clans","Unable to get POLITICS data.");
			return;
		}
		government=CMLib.xml().getIntFromPieces(poliData,"GOVERNMENT");
		exp=CMLib.xml().getLongFromPieces(poliData,"EXP");
		setClanLevel(CMLib.xml().getIntFromPieces(poliData,"LEVEL"));
		setExp(exp); // may change the level
		taxRate=CMLib.xml().getDoubleFromPieces(poliData,"TAXRATE");
		clanClass=CMLib.xml().getValFromPieces(poliData,"CCLASS");
		autoPosition=CMLib.xml().getIntFromPieces(poliData,"AUTOPOS");
		clanCategory=null;
		piece=CMLib.xml().getPieceFromPieces(poliData, "CATE");
		if(piece!=null)
			setCategory(piece.value());
		overrideMinClanMembers=null;
		piece=CMLib.xml().getPieceFromPieces(poliData, "MINM");
		if(piece!=null)
			this.setMinClanMembers(CMath.s_int(piece.value()));
		isRivalrous=null;
		piece=CMLib.xml().getPieceFromPieces(poliData, "RIVAL");
		if(piece!=null)
			setRivalrous(CMath.s_bool(piece.value()));

		// now RESOURCES!
		final List<XMLLibrary.XMLTag> xV=CMLib.xml().getContentsFromPieces(poliData,"RELATIONS");
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
	public void setStatus(int newStatus)
	{
		clanStatus = newStatus;
	}

	@Override
	public String getRecall()
	{
		return clanRecall;
	}

	@Override
	public void setRecall(String newRecall)
	{
		clanRecall = newRecall;
	}

	@Override
	public String getMorgue()
	{
		return clanMorgue;
	}

	@Override
	public void setMorgue(String newMorgue)
	{
		clanMorgue = newMorgue;
	}

	@Override
	public String getDonation()
	{
		return clanDonationRoom;
	}

	@Override
	public void setDonation(String newDonation)
	{
		clanDonationRoom = newDonation;
	}

	@Override
	public List<MemberRecord> getMemberList()
	{
		return getMemberList(-1);
	}

	public int filterMedianLevel(List<FullMemberRecord> members)
	{
		final List<Integer> lvls=new SortedListWrap<Integer>(new XVector<Integer>());
		for(final FullMemberRecord r : members)
			lvls.add(Integer.valueOf(r.level));
		if(lvls.size()>0)
			return lvls.get(lvls.size()/2).intValue();
		return 0;
	}

	public List<MemberRecord> filterMemberList(List<? extends MemberRecord> members, int posFilter)
	{
		final Vector<MemberRecord> filteredMembers=new Vector<MemberRecord>();
		for(final MemberRecord member : members)
		{
			if((member.role==posFilter)||(posFilter<0))
				filteredMembers.add(member);
		}
		return filteredMembers;
	}

	@Override
	public List<MemberRecord> getMemberList(int posFilter)
	{
		return filterMemberList(CMLib.database().DBReadClanMembers(clanID()), posFilter);
	}

	public MemberRecord getMember(String name)
	{
		return CMLib.database().DBGetClanMember(clanID(),name);
	}

	@Override
	public List<FullMemberRecord> getFullMemberList()
	{
		final List<FullMemberRecord> members=new Vector<FullMemberRecord>();
		final List<MemberRecord> subMembers=filterMemberList(CMLib.database().DBReadClanMembers(clanID()), -1);
		for(final MemberRecord member : subMembers)
		{
			if(member!=null)
			{
				final MOB M=CMLib.players().getPlayer(member.name);
				if(M!=null)
				{
					final boolean isAdmin=CMSecurity.isASysOp(M) || M.phyStats().level() > CMProps.getIntVar(CMProps.Int.LASTPLAYERLEVEL);
					if(M.lastTickedDateTime()>0)
						members.add(new FullMemberRecord(member.name,M.basePhyStats().level(),member.role,M.lastTickedDateTime(),member.mobpvps,member.playerpvps,isAdmin));
					else
						members.add(new FullMemberRecord(member.name,M.basePhyStats().level(),member.role,M.playerStats().getLastDateTime(),member.mobpvps,member.playerpvps,isAdmin));
				}
				else
				{
					final PlayerLibrary.ThinPlayer tP = CMLib.database().getThinUser(member.name);
					if(tP != null)
					{
						final boolean isAdmin=CMSecurity.isASysOp(tP) || tP.level() > CMProps.getIntVar(CMProps.Int.LASTPLAYERLEVEL);
						members.add(new FullMemberRecord(member.name,tP.level(),member.role,tP.last(),member.mobpvps,member.playerpvps,isAdmin));
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

	private String crewList(List<? extends MemberRecord> members, int posFilter)
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
	public int getNumVoters(Function function)
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
	public List<Integer> getTopRankedRoles(Function func)
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
	public int getTopQualifiedRoleID(Function func, MOB mob)
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
	public boolean isPubliclyListedFor(MOB mob)
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
	public int getMostInRole(int roleID)
	{
		if((roleID<0)||(roleID>=govt().getPositions().length))
			return 0;
		return govt().getPositions()[roleID].getMax();
	}

	@Override
	public String getRoleName(int roleID, boolean titleCase, boolean plural)
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
			return CMStrings.capitalizeAndLower(pos.getName());
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
	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID!=Tickable.TICKID_CLAN)
			return true;
		if(CMSecurity.isDisabled(CMSecurity.DisFlag.CLANTICKS))
			return true;
		if(lastPropsReload < CMProps.getLastResetTime())
		{
			lastPropsReload=CMProps.getLastResetTime();
			this.overrideMinClanMembers=null;
		}

		try
		{
			List<FullMemberRecord> members=getFullMemberList();
			int activeMembers=0;
			final long deathMilis=CMProps.getIntVar(CMProps.Int.DAYSCLANDEATH)*CMProps.getIntVar(CMProps.Int.TICKSPERMUDDAY)*CMProps.getTickMillis();
			final long overthrowMilis=CMProps.getIntVar(CMProps.Int.DAYSCLANOVERTHROW)*CMProps.getIntVar(CMProps.Int.TICKSPERMUDDAY)*CMProps.getTickMillis();
			for(final FullMemberRecord member : members)
			{
				final long lastLogin=member.timestamp;
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
					if(getStatus()==CLANSTATUS_FADING)
					{
						Log.sysOut("Clans","Clan '"+getName()+" deleted with only "+activeMembers+" having logged on lately.");
						destroyClan();
						final StringBuffer buf=new StringBuffer("");
						for(final FullMemberRecord member : members)
							buf.append(member.name+" on "+CMLib.time().date2String(member.timestamp)+"  ");
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
							final MOB player=CMLib.players().getLoadPlayer(name);
							if(player!=null)
							{
								CMLib.smtp().emailIfPossible("AutoPurge",player.Name(),"AutoPurge: "+name(),
										""+getGovernmentName()+" "+name()+" is in danger of being deleted if at least "+(minimumMembers-activeMembers)
										+" members do not log on within 24 hours.");
							}
						}
					}

					Log.sysOut("Clans","Clan '"+getName()+" fading with only "+activeMembers+" having logged on lately.");
					clanAnnounce(""+getGovernmentName()+" "+name()+" is in danger of being deleted if more members do not log on within 24 hours.");
					update();
				}
				else
				if(getStatus()!=CLANSTATUS_ACTIVE)
					setStatus(CLANSTATUS_ACTIVE);
			}
			else
			switch(getStatus())
			{
			case CLANSTATUS_FADING:
				setStatus(CLANSTATUS_ACTIVE);
				clanAnnounce(""+getGovernmentName()+" "+name()+" is no longer in danger of being deleted.  Be aware that there is required activity level.");
				break;
			case CLANSTATUS_PENDING:
				setStatus(CLANSTATUS_ACTIVE);
				Log.sysOut("Clans",""+getGovernmentName()+" '"+getName()+" now active with "+activeMembers+".");
				clanAnnounce(""+getGovernmentName()+" "+name()+" now has sufficient members.  The "+getGovernmentName()+" is now fully approved.");
				break;
			default:
				break;
			}

			// handle any necessary promotions
			if(govt().getAutoPromoteBy() != AutoPromoteFlag.NONE)
			{
				final List<Integer> highPositionList = getTopRankedRoles(null);
				final List<MemberRecord> highMembers=new LinkedList<MemberRecord>();
				for(final FullMemberRecord member : members)
				{
					if((((System.currentTimeMillis()-member.timestamp)<overthrowMilis)||(overthrowMilis==0))
					&&(highPositionList.contains(Integer.valueOf(member.role))))
						highMembers.add(member);
				}

				AutoPromoteFlag basePromoteBy = govt().getAutoPromoteBy();
				boolean overWrite = false;
				if(basePromoteBy==AutoPromoteFlag.LEVEL_OVERWRITE)
				{
					overWrite=true;
					basePromoteBy=AutoPromoteFlag.LEVEL;
				}
				else
				if(basePromoteBy==AutoPromoteFlag.RANK_OVERWRITE)
				{
					overWrite=true;
					basePromoteBy=AutoPromoteFlag.RANK;
				}

				if(overWrite || (highMembers.size()==0))
				{
					final List<MemberRecord> highestQualifiedMembers = new LinkedList<MemberRecord>();
					for(final FullMemberRecord member : members)
					{
						if((member.role<0)||(member.role>=govt().getPositions().length))
							continue;
						final MOB M=CMLib.players().getLoadPlayer(member.name);
						if(M==null)
							continue;
						for(final Integer posI : highPositionList)
						{
							if((((System.currentTimeMillis()-member.timestamp)<overthrowMilis)||(overthrowMilis==0))
							&&(canBeAssigned(M, posI.intValue())))
								highestQualifiedMembers.add(member);
						}
					}
					if(basePromoteBy==AutoPromoteFlag.RANK)
					{
						ClanPosition bestPos=null;
						for(final MemberRecord member : highestQualifiedMembers)
						{
							final ClanPosition currentPos = govt().getPositions()[member.role];
							if((bestPos==null)||(currentPos.getRank() < bestPos.getRank()))
								bestPos=currentPos;
						}
						if(bestPos!=null)
						{
							for(final Iterator<MemberRecord> i=highestQualifiedMembers.iterator();i.hasNext();)
							{
								if(i.next().role != bestPos.getRoleID())
									i.remove();
							}
						}
					}
					if(basePromoteBy==AutoPromoteFlag.LEVEL)
					{
						int highestLevel=-1;
						for(final MemberRecord member : highestQualifiedMembers)
						{
							final MOB M=CMLib.players().getLoadPlayer(member.name);
							if(M==null)
								continue;
							if(M.basePhyStats().level() > highestLevel)
								highestLevel=M.basePhyStats().level();
						}
						for(final Iterator<MemberRecord> i=highestQualifiedMembers.iterator();i.hasNext();)
						{
							final MOB M=CMLib.players().getLoadPlayer(i.next().name);
							if(M==null)
								continue;
							if(M.basePhyStats().level()!=highestLevel)
								i.remove();
						}
					}
					if(highestQualifiedMembers.size()>0)
					{
						if(overWrite)
						{
							final ClanPosition newRole=govt().getPositions()[govt().getAcceptPos()];
							for(final MemberRecord member : highMembers)
							{
								if(!highestQualifiedMembers.contains(member))
								{
									final MOB M=CMLib.players().getLoadPlayer(member.name);
									if(M==null)
										continue;
									clanAnnounce(member.name+" is now a "+newRole.getName()+" of the "+getGovernmentName()+" "+name()+".");
									Log.sysOut("Clans",member.name+" of "+getGovernmentName()+" "+name()+" was autodemoted to "+newRole.getName()+".");
									M.setClan(clanID(),newRole.getRoleID());
									member.role=newRole.getRoleID();
									CMLib.database().DBUpdateClanMembership(M.Name(), name(), newRole.getRoleID());
								}
							}
						}
						for(final MemberRecord member : highestQualifiedMembers)
						{
							if(!highMembers.contains(member))
							{
								final MOB M=CMLib.players().getLoadPlayer(member.name);
								if(M==null)
									continue;
								ClanPosition newRole=null;
								for(final Integer posI : highPositionList)
								{
									if(canBeAssigned(M, posI.intValue()))
									{
										newRole=govt().getPositions()[posI.intValue()];
										break;
									}
								}
								if(newRole!=null)
								{
									clanAnnounce(member.name+" is now a "+newRole.getName()+" of the "+getGovernmentName()+" "+name()+".");
									Log.sysOut("Clans",member.name+" of "+getGovernmentName()+" "+name()+" was autopromoted to "+newRole.getName()+".");
									M.setClan(clanID(),newRole.getRoleID());
									member.role=newRole.getRoleID();
									CMLib.database().DBUpdateClanMembership(M.Name(), name(), newRole.getRoleID());
									break;
								}
							}
						}
					}
				}
				highMembers.clear();
				members=getFullMemberList();
				for(final FullMemberRecord member : members)
				{
					if((((System.currentTimeMillis()-member.timestamp)<deathMilis)||(deathMilis==0))
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
							buf.append(member.name+" on "+CMLib.time().date2String(member.timestamp)+"  ");
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
	public boolean doesOutRank(int highRoleID, int lowRoleID)
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
	public void clanAnnounce(String msg)
	{
		if(channelSet.size()==0)
		{
			synchronized(channelSet)
			{
				if(channelSet.size()==0)
					channelSet.add(new Pair<Clan,Integer>(this,Integer.valueOf(getGovernment().getAcceptPos())));
			}
		}
		final List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.CLANINFO);
		for(int i=0;i<channels.size();i++)
			CMLib.commands().postChannel(channels.get(i),channelSet,msg,true);
	}

	private static final SearchIDList<Ability> emptyAbles =new CMUniqSortSVec<Ability>(1);

	@Override
	public SearchIDList<Ability> clanAbilities(MOB mob)
	{
		final Pair<Clan,Integer> p=(mob!=null)?mob.getClanRole(clanID()):null;
		if((mob==null)||((p!=null)&&(getAuthority(p.second.intValue(),Function.CLAN_BENEFITS)!=Clan.Authority.CAN_NOT_DO)))
			return govt().getClanLevelAbilities(mob,this,Integer.valueOf(getClanLevel()));
		return emptyAbles;
	}

	@Override
	public int numClanEffects(MOB mob)
	{
		return govt().getClanLevelEffects(mob, this, Integer.valueOf(getClanLevel())).size();
	}

	@Override
	public ChameleonList<Ability> clanEffects(MOB mob)
	{
		return govt().getClanLevelEffects(mob,this, Integer.valueOf(getClanLevel()));
	}

	@Override
	public int applyExpMods(int exp)
	{
		boolean changed=false;
		if((getTaxes()>0.0)&&(exp>1))
		{
			final int clanshare=(int)Math.round(CMath.mul(exp,getTaxes()));
			if(clanshare>0)
			{
				exp-=clanshare;
				adjExp(clanshare);
				changed=true;
			}
		}
		for(final Trophy t : Trophy.values())
		{
			if(CMath.bset(getTrophies(),t.flagNum()))
			{
				String awardStr=null;
				switch(t)
				{
				case Areas:
					awardStr = CMProps.getVar(CMProps.Str.CLANTROPAREA);
					break;
				case Points:
					awardStr = CMProps.getVar(CMProps.Str.CLANTROPCP);
					break;
				case Experience:
					awardStr = CMProps.getVar(CMProps.Str.CLANTROPEXP);
					break;
				case PlayerKills:
					awardStr = CMProps.getVar(CMProps.Str.CLANTROPPK);
					break;
				case Members:
					awardStr = CMProps.getVar(CMProps.Str.CLANTROPMB);
					break;
				case MemberLevel:
					awardStr = CMProps.getVar(CMProps.Str.CLANTROPLVL);
					break;
				}
				if(awardStr!=null)
				{
					int amount=0;
					double pct=0.0;
					final Vector<String> V=CMParms.parse(awardStr);
					if(V.size()>=2)
					{
						final String type=V.lastElement().toUpperCase();
						final String amt=V.firstElement();
						if(amt.endsWith("%"))
							pct=CMath.div(CMath.s_int(amt.substring(0,amt.length()-1)),100.0);
						else
							amount=CMath.s_int(amt);
						if("EXPERIENCE".startsWith(type))
							exp+=((int)Math.round(CMath.mul(exp,pct)))+amount;
					}
				}
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
	public boolean isStat(String code)
	{
		return CMParms.indexOf(getStatCodes(), code.toUpperCase().trim()) >= 0;
	}

	@Override
	public String getStat(String code)
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
			return getPolitics();
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
		}
		return "";
	}

	@Override
	public void setStat(String code, String val)
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
			setPolitics(val);
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
		}
	}
}
