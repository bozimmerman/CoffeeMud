package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.AbilityMapping;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;
import com.planet_ink.coffee_mud.core.threads.ServiceEngine;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.DefaultClan;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.AutoPromoteFlag;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.FullMemberRecord;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.Function;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.Authority;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.MemberRecord;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.Trophy;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
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
public class Clans extends StdLibrary implements ClanManager
{
	protected SHashtable<String,Clan>	all					= new SHashtable<String,Clan>();
	protected List<Pair<Clan,Integer>>	all2				= new Vector<Pair<Clan,Integer>>();
	protected long	 		  		  	lastGovernmentLoad  = 0;
	protected Map<String,Clan>  	  	webPathClanMappings = new SHashtable<String,Clan>();
	protected Map<String,String>		clanWebPathMappings = new SHashtable<String,String>();

	@Override
	public String ID()
	{
		return "Clans";
	}

	public boolean isCommonClanRelations(Clan C1, Clan C2, int relation)
	{
		if((C1==null)||(C2==null))
			return relation==Clan.REL_NEUTRAL;
		int i1=C1.getClanRelations(C2.clanID());
		int i2=C2.getClanRelations(C1.clanID());
		if((i1==i2)
		&&((i1==Clan.REL_WAR)
		   ||(i1==Clan.REL_ALLY)))
		   return i1==relation;
		for(final Enumeration<Clan> e=clans();e.hasMoreElements();)
		{
			final Clan C=e.nextElement();
			if((C!=C1)&&(C!=C2))
			{
				if((i1!=Clan.REL_WAR)
				&&(C1.getClanRelations(C.clanID())==Clan.REL_ALLY)
				&&(C.getClanRelations(C2.clanID())==Clan.REL_WAR))
					i1=Clan.REL_WAR;
				if((i2!=Clan.REL_WAR)
				&&(C2.getClanRelations(C.clanID())==Clan.REL_ALLY)
				&&(C.getClanRelations(C1.clanID())==Clan.REL_WAR))
					i2=Clan.REL_WAR;
			}
		}
		if(i1==i2)
			return relation==i1;

		if(Clan.REL_NEUTRALITYGAUGE[i1]<Clan.REL_NEUTRALITYGAUGE[i2])
			return relation==i1;
		return relation==i2;
	}

	@Override
	public boolean isCommonClanRelations(String clanID1, String clanID2, int relation)
	{
		if((clanID1==null)||(clanID2==null)||(clanID1.length()==0)||(clanID2.length()==0))
			return Clan.REL_NEUTRAL==relation;
		return isCommonClanRelations(getClan(clanID1),getClan(clanID1), relation);
	}

	@Override
	public boolean isAtClanWar(MOB M1, MOB M2)
	{
		final List<Pair<Clan,Clan>> pairs=findUncommonRivalrousClans(M1, M2);
		for(final Pair<Clan,Clan> p : pairs)
		{
			if(isCommonClanRelations(p.first,p.second, Clan.REL_WAR))
				return true;
		}
		return false;
	}

	@Override
	public boolean checkClanPrivilege(MOB mob, Clan.Function func)
	{
		return findPrivilegedClan(mob,func)!=null;
	}

	@Override
	public boolean checkClanPrivilege(MOB mob, String clanID, Clan.Function func)
	{
		if(mob==null)
			return false;
		final Pair<Clan,Integer> c=mob.getClanRole(clanID);
		if(c==null)
			return false;
		if(c.first.getAuthority(c.second.intValue(), func) != Clan.Authority.CAN_NOT_DO)
			return true;
		return false;
	}

	@Override
	public Pair<Clan,Integer> findPrivilegedClan(MOB mob, Clan.Function func)
	{
		for(final Pair<Clan,Integer> c : mob.clans())
		{
			if(c.first.getAuthority(c.second.intValue(), func) != Clan.Authority.CAN_NOT_DO)
				return c;
		}
		return null;
	}

	@Override
	public List<Pair<Clan,Integer>> findPrivilegedClans(MOB mob, Clan.Function func)
	{
		final List<Pair<Clan,Integer>> set=new Vector<Pair<Clan,Integer>>();
		for(final Pair<Clan,Integer> c : mob.clans())
		{
			if(c.first.getAuthority(c.second.intValue(), func) != Clan.Authority.CAN_NOT_DO)
				set.add(c);
		}
		return set;
	}

	@Override
	public Clan findRivalrousClan(MOB mob)
	{
		for(final Pair<Clan,Integer> c : mob.clans())
		{
			if(c.first.isRivalrous())
			{
				return c.first;
			}
		}
		return null;
	}

	@Override
	public Clan findConquerableClan(MOB mob)
	{
		for(final Pair<Clan,Integer> c : mob.clans())
		{
			if(c.first.getGovernment().isConquestEnabled())
			{
				return c.first;
			}
		}
		return null;
	}

	@Override
	public List<Triad<Clan,Integer,Integer>> findCommonRivalrousClans(MOB mob1, MOB mob2)
	{
		final List<Triad<Clan,Integer,Integer>> list=new XVector<Triad<Clan,Integer,Integer>>(1,true);
		if((mob1==null)||(mob2==null))
			return list;
		for(final Pair<Clan,Integer> c : mob1.clans())
		{
			if(c.first.isRivalrous())
			{
				final Pair<Clan,Integer> c2=mob2.getClanRole(c.first.clanID());
				if(c2!=null)
					list.add(new Triad<Clan,Integer,Integer>(c.first,c.second,c2.second));
			}
		}
		return list;
	}

	@Override
	public List<Pair<Clan,Integer>> findRivalrousClans(MOB mob)
	{
		final List<Pair<Clan,Integer>> list=new XVector<Pair<Clan,Integer>>(1,true);
		for(final Pair<Clan,Integer> c : mob.clans())
		{
			if(c.first.isRivalrous())
				list.add(c);
		}
		return list;
	}

	@Override
	public List<Pair<Clan,Integer>> findRivalrousClans(MOB clanSourceMob, MOB filterMob)
	{
		final List<Pair<Clan,Integer>> list=new XVector<Pair<Clan,Integer>>(1,true);
		if((clanSourceMob==null)||(filterMob==null))
			return list;
		for(final Pair<Clan,Integer> c : clanSourceMob.clans())
		{
			if(c.first.isRivalrous())
			{
				final Pair<Clan,Integer> c2=filterMob.getClanRole(c.first.clanID());
				if(c2==null)
					list.add(c);
			}
		}
		return list;
	}

	public List<Pair<Clan,Clan>> findUncommonRivalrousClans(MOB M1, MOB M2)
	{
		final List<Pair<Clan,Clan>> list=new XVector<Pair<Clan,Clan>>(1,true);
		if((M1==null)||(M2==null))
			return list;
		// i need the disunion here (what's the word for that?), as a order-irrelevant set of pairs
		for(final Pair<Clan,Integer> c : M1.clans())
		{
			if(c.first.isRivalrous())
			{
				final Pair<Clan,Integer> c2=M2.getClanRole(c.first.clanID());
				if(c2==null)
					list.add(new Pair<Clan,Clan>(c.first,null));
			}
		}
		final List<Pair<Clan,Clan>> finalList=new XVector<Pair<Clan,Clan>>(1,true);
		for(final Pair<Clan,Clan> p : list)
			for(final Pair<Clan,Integer> c : M2.clans())
				if(c.first.isRivalrous())
					finalList.add(new Pair<Clan,Clan>(p.first,c.first));
		return finalList;
	}

	public List<Pair<Clan,Clan>> getAllClanPairs(MOB M1, MOB M2)
	{
		final List<Pair<Clan,Clan>> list=new XVector<Pair<Clan,Clan>>(1,true);
		if((M1==null)||(M2==null))
			return list;
		// i need the disunion here (what's the word for that?), as a order-irrelevant set of pairs
		for(final Pair<Clan,Integer> c : M1.clans())
			list.add(new Pair<Clan,Clan>(c.first,null));
		final List<Pair<Clan,Clan>> finalList=new XVector<Pair<Clan,Clan>>(1,true);
		for(final Pair<Clan,Clan> p : list)
			for(final Pair<Clan,Integer> c : M2.clans())
				if(p.first!=c.first)
					finalList.add(new Pair<Clan,Clan>(p.first,c.first));
		return finalList;
	}

	public int getClanRelations(Clan C1, Clan C2)
	{
		if((C1==null)||(C2==null))
			return Clan.REL_NEUTRAL;
		final int i1=C1.getClanRelations(C2.clanID());
		final int i2=C2.getClanRelations(C1.clanID());
		final int rel=Clan.RELATIONSHIP_VECTOR[i1][i2];
		if(rel==Clan.REL_WAR)
			return Clan.REL_WAR;
		if(rel==Clan.REL_ALLY)
			return Clan.REL_ALLY;
		for(final Enumeration<Clan> e=clans();e.hasMoreElements();)
		{
			final Clan C=e.nextElement();
			if((C!=C1)
			&&(C!=C2)
			&&(((C1.getClanRelations(C.clanID())==Clan.REL_ALLY)&&(C.getClanRelations(C2.clanID())==Clan.REL_WAR)))
				||((C2.getClanRelations(C.clanID())==Clan.REL_ALLY)&&(C.getClanRelations(C1.clanID())==Clan.REL_WAR)))
					return Clan.REL_WAR;
		}
		return rel;
	}

	@Override
	public int getClanRelations(String clanID1, String clanID2)
	{
		if((clanID1==null)||(clanID2==null)||(clanID1.length()==0)||(clanID2.length()==0))
			return Clan.REL_NEUTRAL;
		return getClanRelations(getClan(clanID1),getClan(clanID2));
	}

	@Override
	public boolean findAnyClanRelations(MOB M1, MOB M2, int relation)
	{
		for(final Pair<Clan,Clan> c : getAllClanPairs(M1, M2))
		{
			if(getClanRelations(c.first, c.second)==relation)
				return true;
		}
		return false;
	}

	@Override
	public boolean isAnyCommonClan(MOB M1, MOB M2)
	{
		if((M1==null)||(M2==null))
			return false;
		for(final Pair<Clan,Integer> p : M1.clans())
		{
			if(M2.getClanRole(p.first.clanID())!=null)
				return true;
		}
		return false;
	}

	@Override
	public Clan getClan(String id)
	{
		if((id==null)||(id.length()==0))
			return null;
		Clan C=all.get(id.toUpperCase());
		if(C!=null)
			return C;
		for(final Enumeration<Clan> e=all.elements();e.hasMoreElements();)
		{
			C=e.nextElement();
			if(CMLib.english().containsString(CMStrings.removeColors(C.name()),id))
				return C;
		}
		return null;
	}

	@Override
	public Clan findClan(String id)
	{
		Clan C=getClan(id);
		if(C!=null)
			return C;
		for(final Enumeration<Clan> e=all.elements();e.hasMoreElements();)
		{
			C=e.nextElement();
			if(CMLib.english().containsString(CMStrings.removeColors(C.name()),id))
				return C;
		}
		return null;
	}

	protected boolean isMember(List<MemberRecord> members, String name)
	{
		for(MemberRecord R : members)
		{
			if(R.name.equals(name))
				return true;
		}
		return false;
	}
	
	@Override
	public boolean isFamilyOfMembership(MOB M, List<MemberRecord> members)
	{
		if(M == null)
			return false;
		if(isMember(members,M.Name()))
			return true;
		if((M.getLiegeID().length()>0)
		&&(M.isMarriedToLiege())
		&&(isMember(members,M.getLiegeID())))
			return true;
		final String[] familyTattoos = new String[] {"PARENT:","BROTHER:","SISTER:","SIBLING:","BLOODBROTHER:"};
		for(final Enumeration<Tattoo> e=M.tattoos();e.hasMoreElements();)
		{
			final Tattoo T=e.nextElement();
			for(final String familyTattoo : familyTattoos)
			{
				if(T.getTattooName().startsWith(familyTattoo))
				{
					final String name=T.getTattooName().substring(familyTattoo.length());
					final MOB M2=CMLib.players().getLoadPlayer(name.toLowerCase());
					if((M2 != null)&&isFamilyOfMembership(M2,members))
						return true;
				}
			}
		}
		return false;
	}

	@Override
	public Enumeration<Clan> clans()
	{
		return all.elements();
	}

	@Override
	public int numClans()
	{
		return all.size();
	}

	@Override
	public void addClan(Clan C)
	{
		synchronized(all)
		{
			if(!CMSecurity.isDisabled(CMSecurity.DisFlag.CLANTICKS))
				CMLib.threads().startTickDown(C,Tickable.TICKID_CLAN,(int)CMProps.getTicksPerDay());
			all.put(C.clanID().toUpperCase(),C);
			all2.add(new Pair<Clan,Integer>(C,Integer.valueOf(C.getGovernment().getAcceptPos())));
			setClanWebSiteMappings(C,CMProps.getVar(CMProps.Str.CLANWEBSITES));
			CMLib.journals().registerClanForum(C,CMProps.getVar(CMProps.Str.CLANFORUMDATA));
			CMLib.map().sendGlobalMessage(CMLib.map().deity(), CMMsg.TYP_CLANEVENT,
				CMClass.getMsg(CMLib.map().deity(), CMMsg.MSG_CLANEVENT, "+"+C.name()));
		}
	}

	@Override
	public void removeClan(Clan C)
	{
		synchronized(all)
		{
			CMLib.threads().deleteTick(C,Tickable.TICKID_CLAN);
			all.remove(C.clanID().toUpperCase());
			for(final Pair<Clan,Integer> p : all2)
			{
				if(p.first==C)
				{
					all2.remove(p);
					break;
				}
			}
			setClanWebSiteMappings(C,null); // will delete mapping
			CMLib.journals().registerClanForum(C,null); // will remove mapping
			CMLib.map().sendGlobalMessage(CMLib.map().deity(), CMMsg.TYP_CLANEVENT,
					CMClass.getMsg(CMLib.map().deity(), CMMsg.MSG_CLANEVENT, "-"+C.name()));
		}
	}

	@Override
	public void tickAllClans()
	{
		for(final Enumeration<Clan> e=clans();e.hasMoreElements();)
		{
			final Clan C=e.nextElement();
			C.tick(C,Tickable.TICKID_CLAN);
		}
	}

	@Override
	public void clanAnnounceAll(String msg)
	{
		final List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.CLANINFO);
		for(int i=0;i<channels.size();i++)
			CMLib.commands().postChannel(channels.get(i),clanRoles(),msg,true);
	}

	@Override
	public Enumeration<String> clansNames()
	{
		return all.keys();
	}

	@Override
	public Iterable<Pair<Clan,Integer>> clanRoles()
	{
		return all2;
	}

	@Override
	public String translatePrize(Trophy trophy)
	{
		String prizeStr="";
		switch(trophy)
		{
			case Areas: prizeStr=CMProps.getVar(CMProps.Str.CLANTROPAREA); break;
			case Points: prizeStr=CMProps.getVar(CMProps.Str.CLANTROPCP); break;
			case Experience: prizeStr=CMProps.getVar(CMProps.Str.CLANTROPEXP); break;
			case PlayerKills: prizeStr=CMProps.getVar(CMProps.Str.CLANTROPPK); break;
			case Members: prizeStr=CMProps.getVar(CMProps.Str.CLANTROPMB); break;
			case MemberLevel: prizeStr=CMProps.getVar(CMProps.Str.CLANTROPLVL); break;
		}
		if(prizeStr.length()==0)
			return "None";
		if(prizeStr.length()>0)
		{
			final Vector<String> V=CMParms.parse(prizeStr);
			if(V.size()>=2)
			{
				final String type=V.lastElement().toUpperCase();
				final String amt=V.firstElement();
				if("EXPERIENCE".startsWith(type))
					return amt+" experience point bonus.";
			}
		}
		return prizeStr;
	}

	@Override
	public boolean trophySystemActive()
	{
		return (CMProps.getVar(CMProps.Str.CLANTROPAREA).length()>0)
			|| (CMProps.getVar(CMProps.Str.CLANTROPCP).length()>0)
			|| (CMProps.getVar(CMProps.Str.CLANTROPEXP).length()>0)
			|| (CMProps.getVar(CMProps.Str.CLANTROPMB).length()>0)
			|| (CMProps.getVar(CMProps.Str.CLANTROPLVL).length()>0)
			|| (CMProps.getVar(CMProps.Str.CLANTROPPK).length()>0);

	}

	@Override
	public boolean goForward(MOB mob, Clan C, List<String> commands, Clan.Function function, boolean voteIfNecessary)
	{
		if((mob==null)||(C==null))
			return false;
		final Pair<Clan,Integer> clanRole=mob.getClanRole(C.clanID());
		if(clanRole==null)
			return false;
		final int role=clanRole.second.intValue();
		final Clan.Authority allowed=C.getAuthority(role,function);
		if(allowed==Clan.Authority.CAN_DO)
			return true;
		if(allowed==Clan.Authority.CAN_NOT_DO)
			return false;
		if(function==Clan.Function.ASSIGN)
		{
			if(C.getAuthority(role,Clan.Function.VOTE_ASSIGN)!=Clan.Authority.CAN_DO)
				return false;
		}
		else
		if(C.getAuthority(role,Clan.Function.VOTE_OTHER)!=Clan.Authority.CAN_DO)
			return false;
		if(!voteIfNecessary)
			return true;
		final String matter=CMParms.combine(commands,0);
		for(final Enumeration<Clan.ClanVote> e=C.votes();e.hasMoreElements();)
		{
			final Clan.ClanVote CV=e.nextElement();
			if((CV.voteStarter.equalsIgnoreCase(mob.Name()))
			&&(CV.voteStatus==Clan.VSTAT_STARTED))
			{
				mob.tell(L("This matter must be voted upon, but you already have a vote underway."));
				return false;
			}
			if(CV.matter.equalsIgnoreCase(matter))
			{
				mob.tell(L("This matter must be voted upon, and is already BEING voted upon.  Use CLANVOTE to see."));
				return false;
			}
		}
		if(mob.session()==null)
			return false;
		try
		{
			final int numVotes=C.getNumVoters(function);
			if(numVotes==1)
				return true;

			if(mob.session().confirm(L("This matter must be voted upon.  Would you like to start the vote now (y/N)?"),"N"))
			{
				final Clan.ClanVote CV=new Clan.ClanVote();
				CV.matter=matter;
				CV.voteStarter=mob.Name();
				CV.function=function.ordinal();
				CV.voteStarted=System.currentTimeMillis();
				CV.votes=new PairVector<String,Boolean>();
				CV.voteStatus=Clan.VSTAT_STARTED;
				C.addVote(CV);
				C.updateVotes();
				final Clan.Function voteFunctionType = (function == Clan.Function.ASSIGN) ? Clan.Function.VOTE_ASSIGN : Clan.Function.VOTE_OTHER;
				final List<Integer> votingRoles = new Vector<Integer>();
				for(int i=0;i<C.getRolesList().length;i++)
				{
					if(C.getAuthority(i, voteFunctionType)==Clan.Authority.CAN_DO)
						votingRoles.add(Integer.valueOf(i));
				}
				if(votingRoles.size()>0)
				{
					final String firstRoleName = C.getRoleName(votingRoles.iterator().next().intValue(), true, true);
					final String rest = " "+firstRoleName+" should use CLANVOTE to participate.";
					if(votingRoles.size() >= (C.getRolesList().length-2))
					{
						if(function == Clan.Function.ASSIGN)
							clanAnnounce(mob,"The "+C.getGovernmentName()+" "+C.clanID()+" has a new election to vote upon. "+rest);
						else
							clanAnnounce(mob,"The "+C.getGovernmentName()+" "+C.clanID()+" has a new matter to vote upon. "+rest);
					}
					else
					if(votingRoles.size()==1)
						clanAnnounce(mob,"The "+C.getGovernmentName()+" "+C.clanID()+" has a new matter to vote upon. "+rest);
					else
					{
						final String[] roleNames = new String[votingRoles.size()];
						for(int i=0;i<votingRoles.size();i++)
						{
							final Integer roleID=votingRoles.get(i);
							roleNames[i]=C.getRoleName(roleID.intValue(), true, true);
						}
						final String list = CMLib.english().toEnglishStringList(roleNames);
						clanAnnounce(mob,"The "+C.getGovernmentName()+" "+C.clanID()+" has a new matter to vote upon. "
								+list+" should use CLANVOTE to participate.");
					}
				}
				mob.tell(L("Your vote has started.  Use CLANVOTE to cast your vote."));
				return false;
			}
		}
		catch(final java.io.IOException e)
		{
		}
		mob.tell(L("Without a vote, this command can not be executed."));
		return false;
	}

	protected String indt(int x)
	{
		return CMStrings.SPACES.substring(0,x*4);
	}

	@Override
	public long getLastGovernmentLoad()
	{
		return lastGovernmentLoad;
	}

	@Override
	public String getGovernmentHelp(MOB mob, String named, boolean exact)
	{
		ClanGovernment helpG=null;
		for(final ClanGovernment G : getStockGovernments())
		{
			if(G.getName().equalsIgnoreCase(named))
				helpG=G;
		}
		if((helpG==null)&&(exact))
			return null;
		if(helpG==null)
			for(final ClanGovernment G : getStockGovernments())
				if(G.getName().toUpperCase().startsWith(named.toUpperCase()))
					helpG=G;
		if(helpG==null)
		{
			final List<ClanGovernment> gtypes=new Vector<ClanGovernment>();
			String name=null;
			for(final ClanGovernment G : getStockGovernments())
			{
				for(final ClanPosition P : G.getPositions())
				{
					if(P.getName().equalsIgnoreCase(named)||P.getPluralName().equalsIgnoreCase(named))
					{
						gtypes.add(G);
						name=P.getName();
					}
				}
			}
			if(gtypes.size()==0)
			{
				if(exact)
					return null;
				for(final ClanGovernment G : getStockGovernments())
					for(final ClanPosition P : G.getPositions())
						if(P.getName().toUpperCase().startsWith(named.toUpperCase())
							||P.getPluralName().toUpperCase().startsWith(named.toUpperCase()))
						{
							gtypes.add(G);
							name=P.getName();
						}
			}
			if(gtypes.size()==0)
				return null;
			final String[] typeNames=new String[gtypes.size()];
			for(int g=0;g<gtypes.size();g++)
				typeNames[g]=CMStrings.capitalizeAndLower(gtypes.get(g).getName());
			return "The "+name+" is a rank or position within the following clan types: "
				   +CMLib.english().toEnglishStringList(typeNames)
				   +".  Please see help on CLAN or on one of the listed clan types for more information. ";
		}
		return helpG.getHelpStr();
	}

	@Override
	public ClanGovernment createSampleGovernment()
	{
		final Authority[] pows1=new Authority[Function.values().length];
		for(int i=0;i<pows1.length;i++) pows1[i]=Authority.CAN_NOT_DO;
		final Authority[] pows2=new Authority[Function.values().length];
		for(int i=0;i<pows2.length;i++) pows2[i]=Authority.CAN_DO;
		final ClanPosition P1=(ClanPosition)CMClass.getCommon("DefaultClanPosition");
		P1.setID("APPLICANT");
		P1.setRoleID(0);
		P1.setRank(0);
		P1.setName(L("Applicant"));
		P1.setPluralName("Applicants");
		P1.setMax(Integer.MAX_VALUE);
		P1.setInnerMaskStr("");
		P1.setFunctionChart(pows1);
		P1.setPublic(false);
		final ClanPosition P2=(ClanPosition)CMClass.getCommon("DefaultClanPosition");
		P2.setID("MEMBER");
		P2.setRoleID(1);
		P2.setRank(1);
		P2.setName(L("Member"));
		P2.setPluralName("Members");
		P2.setMax(Integer.MAX_VALUE);
		P2.setInnerMaskStr("");
		P2.setFunctionChart(pows2);
		P2.setPublic(false);
		final Set<Integer> usedTypeIDs=new HashSet<Integer>();
		final ClanGovernment[] gvts=(ClanGovernment[])Resources.getResource("parsed_clangovernments");
		int id=0;
		if(gvts!=null)
		{
			for(final ClanGovernment G2 : gvts)
				usedTypeIDs.add(Integer.valueOf(G2.getID()));
			for(int i=0;i<gvts.length;i++)
			{
				if(!usedTypeIDs.contains(Integer.valueOf(i)))
				{
					id=i; break;
				}
			}
		}

		final ClanGovernment G=(ClanGovernment)CMClass.getCommon("DefaultClanGovernment");
		G.setID(id);
		G.setName(L("Sample Govt"));
		G.setCategory("");
		G.setPositions(new ClanPosition[]{P1,P2});
		G.setAutoRole(0);
		G.setAcceptPos(1);
		G.setRequiredMaskStr("");
		G.setEntryScript("");
		G.setExitScript("");
		G.setAutoPromoteBy(AutoPromoteFlag.NONE);
		G.setPublic(true);
		G.setFamilyOnly(false);
		G.setOverrideMinMembers(Integer.valueOf(1));
		G.setConquestEnabled(true);
		G.setConquestItemLoyalty(true);
		G.setConquestByWorship(false);
		G.setShortDesc("Change Me!");
		G.setLongDesc("");
		G.setMaxVoteDays(10);
		G.setRivalrous(true);
		G.setVoteQuorumPct(66);
		G.setDefault(true);
		return G;
	}

	@Override
	public void reSaveGovernmentsXML()
	{
		final ClanGovernment[] govt = getStockGovernments();
		final String xml = makeGovernmentXML(govt);
		if(!Resources.updateFileResource("clangovernments.xml", xml))
		{
			Log.errOut("Clans","Can't save clangovernments.xml");
		}
		Resources.removeResource("parsed_clangovernments");
		getStockGovernments();
	}

	@Override
	public ClanGovernment createGovernment(String name)
	{
		final ClanGovernment[] gvts=getStockGovernments();
		final List<ClanGovernment> govts = new SVector<ClanGovernment>(gvts);
		for(final ClanGovernment G : gvts)
		{
			if(G.getName().equalsIgnoreCase(name))
				return null;
		}
		final ClanGovernment newG=createSampleGovernment();
		final Set<Integer> takenIDs=new HashSet<Integer>();
		for(final ClanGovernment g : gvts)
			takenIDs.add(Integer.valueOf(g.getID()));
		int newID=CMLib.dice().roll(1, Integer.MAX_VALUE, 0);
		for(int i=0;i<gvts.length+1;i++)
		{
			if(!takenIDs.contains(Integer.valueOf(i)))
				newID=i;
		}
		newG.setID(newID);
		newG.setName(name);
		govts.add(newG);
		Resources.submitResource("parsed_clangovernments", govts.toArray(new ClanGovernment[0]));
		return newG;
	}

	@Override
	public boolean removeGovernment(ClanGovernment government)
	{
		final ClanGovernment[] gvts=getStockGovernments();
		if(gvts.length==1)
			return false;
		final List<ClanGovernment> govts = new SVector<ClanGovernment>(gvts);
		govts.remove(government);
		if(govts.size()==gvts.length)
			return false;
		Resources.submitResource("parsed_clangovernments", govts.toArray(new ClanGovernment[0]));
		return true;
	}

	@Override
	public ClanGovernment[] getStockGovernments()
	{
		ClanGovernment[] gvts=(ClanGovernment[])Resources.getResource("parsed_clangovernments");
		if(gvts==null)
		{
			synchronized(this)
			{
				gvts=(ClanGovernment[])Resources.getResource("parsed_clangovernments");
				if(gvts==null)
				{
					final StringBuffer str=Resources.getFileResource("clangovernments.xml", true);
					if(str==null)
						gvts=new ClanGovernment[0];
					else
					{
						gvts=parseGovernmentXML(str);
					}
					if((gvts==null)||(gvts.length==0))
					{
						final ClanGovernment gvt=createSampleGovernment();
						gvt.setDefault(true);
						gvts=new ClanGovernment[]{gvt};
					}
					lastGovernmentLoad=System.currentTimeMillis();
					Resources.submitResource("parsed_clangovernments",gvts);
				}
			}
		}
		return gvts;
	}

	@Override
	public ClanGovernment getDefaultGovernment()
	{
		final ClanGovernment[] gvts=getStockGovernments();
		for(final ClanGovernment gvt : gvts)
		{
			if(gvt.isDefault())
				return gvt;
		}
		return gvts[0];
	}

	@Override
	public ClanGovernment getStockGovernment(int typeid)
	{
		final ClanGovernment[] gvts=getStockGovernments();
		if(gvts.length <= typeid)
		{
			Log.errOut("Clans","Someone mistakenly requested stock government typeid "+typeid);
			return gvts[0];
		}
		return gvts[typeid];
	}

	@Override
	public String makeGovernmentXML(ClanGovernment gvt)
	{
		final StringBuilder str=new StringBuilder("");
		str.append("<CLANTYPE ").append("TYPEID="+gvt.getID()+" ").append("NAME=\""+gvt.getName()+"\" ").append("CATEGORY=\""+gvt.getCategory()+"\"").append(">\n");
		if(gvt.isDefault())
			str.append(indt(1)).append("<ISDEFAULT>true</ISDEFAULT>\n");
		str.append(indt(1)).append("<SHORTDESC>").append(CMLib.xml().parseOutAngleBrackets(gvt.getShortDesc())).append("</SHORTDESC>\n");
		str.append(indt(1)).append("<LONGDESC>").append(CMLib.xml().parseOutAngleBrackets(gvt.getLongDesc())).append("</LONGDESC>\n");
		str.append(indt(1)).append("<POSITIONS>\n");
		final Set<Clan.Function> voteSet = new HashSet<Clan.Function>();
		for(int p=gvt.getPositions().length-1;p>=0;p--)
		{
			final ClanPosition pos = gvt.getPositions()[p];
			str.append(indt(2)).append("<POSITION ").append("ID=\""+pos.getID()+"\" ").append("ROLEID="+pos.getRoleID()+" ")
								.append("RANK="+pos.getRank()+" ").append("NAME=\""+pos.getName()+"\" ").append("PLURAL=\""+pos.getPluralName()+"\" ")
								.append("MAX="+pos.getMax()+" ").append("INNERMASK=\""+CMLib.xml().parseOutAngleBrackets(pos.getInnerMaskStr())+"\" ")
								.append("PUBLIC=\""+pos.isPublic()+"\">\n");
			for(final Clan.Function func : Clan.Function.values())
				if(pos.getFunctionChart()[func.ordinal()]==Clan.Authority.CAN_DO)
					str.append(indt(3)).append("<POWER>").append(func.toString()).append("</POWER>\n");
				else
				if(pos.getFunctionChart()[func.ordinal()]==Clan.Authority.MUST_VOTE_ON)
					voteSet.add(func);
			str.append(indt(2)).append("</POSITION>\n");
		}
		str.append(indt(1)).append("</POSITIONS>\n");
		if(voteSet.size()==0)
			str.append(indt(1)).append("<VOTING />\n");
		else
		{
			str.append(indt(1)).append("<VOTING ").append("MAXDAYS="+gvt.getMaxVoteDays()+" QUORUMPCT="+gvt.getVoteQuorumPct()+">\n");
			for(final Clan.Function func : voteSet)
				str.append(indt(2)).append("<POWER>").append(func.toString()).append("</POWER>\n");
			str.append(indt(1)).append("</VOTING>\n");
		}
		str.append(indt(1)).append("<AUTOPOSITION>").append(gvt.getPositions()[gvt.getAutoRole()].getID()).append("</AUTOPOSITION>\n");
		str.append(indt(1)).append("<ACCEPTPOSITION>").append(gvt.getPositions()[gvt.getAcceptPos()].getID()).append("</ACCEPTPOSITION>\n");
		str.append(indt(1)).append("<REQUIREDMASK>").append(CMLib.xml().parseOutAngleBrackets(gvt.getRequiredMaskStr())).append("</REQUIREDMASK>\n");
		str.append(indt(1)).append("<ENTRYSCRIPT>").append(CMLib.xml().parseOutAngleBrackets(gvt.getEntryScript())).append("</ENTRYSCRIPT>\n");
		str.append(indt(1)).append("<EXITSCRIPT>").append(CMLib.xml().parseOutAngleBrackets(gvt.getExitScript())).append("</EXITSCRIPT>\n");
		str.append(indt(1)).append("<AUTOPROMOTEBY>").append(gvt.getAutoPromoteBy().toString()).append("</AUTOPROMOTEBY>\n");
		str.append(indt(1)).append("<PUBLIC>").append(gvt.isPublic()).append("</PUBLIC>\n");
		str.append(indt(1)).append("<FAMILYONLY>").append(gvt.isFamilyOnly()).append("</FAMILYONLY>\n");
		str.append(indt(1)).append("<RIVALROUS>").append(gvt.isRivalrous()).append("</RIVALROUS>\n");
		str.append(indt(1)).append("<XPPERLEVELFORMULA>").append(gvt.getXpCalculationFormulaStr()).append("</XPPERLEVELFORMULA>\n");
		if(gvt.getOverrideMinMembers() == null)
			str.append(indt(1)).append("<OVERRIDEMINMEMBERS />\n");
		else
			str.append(indt(1)).append("<OVERRIDEMINMEMBERS>").append(gvt.getOverrideMinMembers().toString()).append("</OVERRIDEMINMEMBERS>\n");
		str.append(indt(1)).append("<CONQUEST>\n");
		{
			str.append(indt(2)).append("<ENABLED>").append(gvt.isConquestEnabled()).append("</ENABLED>\n");
			str.append(indt(2)).append("<ITEMLOYALTY>").append(gvt.isConquestItemLoyalty()).append("</ITEMLOYALTY>\n");
			str.append(indt(2)).append("<DEITYBASIS>").append(gvt.isConquestByWorship()).append("</DEITYBASIS>\n");
		}
		str.append(indt(1)).append("</CONQUEST>\n");
		gvt.getClanLevelAbilities(null,null,Integer.valueOf(Integer.MAX_VALUE));
		final Enumeration<AbilityMapping> m= CMLib.ableMapper().getClassAbles(gvt.getName(), false);
		if(!m.hasMoreElements())
			str.append(indt(1)).append("<ABILITIES />\n");
		else
		{
			str.append(indt(1)).append("<ABILITIES>\n");
			for(;m.hasMoreElements();)
			{
				final AbilityMapping map=m.nextElement();
				final String addExt;
				if(map.extFields().size()>0)
				{
					final List<String> posNames=new ArrayList<String>();
					for(String I : map.extFields().keySet())
					{
						final ClanPosition P=gvt.findPositionRole(I);
						if(P!=null)
							posNames.add(P.getID());
					}
					addExt="ROLES=\""+CMParms.toListString(posNames)+"\" ";
				}
				else
					addExt="";
				str.append(indt(2)).append("<ABILITY ID=\""+map.abilityID()+"\" "
						+ "PROFF="+map.defaultProficiency()+" "
						+ "LEVEL="+map.qualLevel()+" "
						+ "QUALIFYONLY="+(!map.autoGain())+" "
						+ "PARM=\""+CMLib.xml().parseOutAngleBrackets(map.defaultParm())+"\" "
						+addExt+"/>\n");
			}
			str.append(indt(1)).append("</ABILITIES>\n");
		}
		final int numEffects=CMath.s_int(gvt.getStat("NUMREFF"));
		if(numEffects==0)
			str.append(indt(1)).append("<EFFECTS />\n");
		else
		{
			str.append(indt(1)).append("<EFFECTS>\n");
			for(int a=0;a<numEffects;a++)
			{
				final String ableID=gvt.getStat("GETREFF"+a);
				final String ableParm=gvt.getStat("GETREFFPARM"+a);
				final int lvl = CMath.s_int(gvt.getStat("GETREFFLVL"+a));
				final String roleList=gvt.getStat("GETREFFROLE"+a);
				final String addExt;
				if(roleList.trim().length()>0)
				{
					final List<String> posNames=new ArrayList<String>();
					for(String I : CMParms.parseCommas(roleList,true))
					{
						final ClanPosition P=gvt.findPositionRole(I);
						if(P!=null)
							posNames.add(P.getID());
					}
					addExt="ROLES=\""+CMParms.toListString(posNames)+"\" ";
				}
				else
					addExt="";
				str.append(indt(2)).append("<EFFECT ID=\""+ableID+"\" LEVEL="+lvl+" PARMS=\""+CMLib.xml().parseOutAngleBrackets(ableParm)+"\" "+addExt+"/>\n");
			}
			str.append(indt(1)).append("</EFFECTS>\n");
		}

		str.append("</CLANTYPE>\n");
		return str.toString();
	}

	@Override
	public String makeGovernmentXML(ClanGovernment gvts[])
	{
		final StringBuilder str=new StringBuilder("");
		str.append("<CLANTYPES>\n");
		for(final ClanGovernment gvt : gvts)
			str.append(makeGovernmentXML(gvt));
		str.append("</CLANTYPES>\n");
		return str.toString();
	}

	@Override
	public List<Pair<Clan,Integer>> getClansByCategory(MOB M, String category)
	{
		final List<Pair<Clan,Integer>> list=new Vector<Pair<Clan,Integer>>(1);
		if(M==null)
			return list;
		if(category==null)
			category="";
		for(final Pair<Clan,Integer> p : M.clans())
		{
			if(p.first.getCategory().equalsIgnoreCase(category))
				list.add(p);
		}
		return list;
	}

	@Override
	public ClanGovernment[] parseGovernmentXML(StringBuffer xml)
	{
		final List<XMLLibrary.XMLTag> xmlV = CMLib.xml().parseAllXML(xml);
		final XMLTag clanTypesTag = CMLib.xml().getPieceFromPieces(xmlV, "CLANTYPES");
		List<XMLLibrary.XMLTag> clanTypes = null;
		if(clanTypesTag != null)
			clanTypes = clanTypesTag.contents();
		else
		{
			final XMLLibrary.XMLTag clanType = CMLib.xml().getPieceFromPieces(xmlV, "CLANTYPE");
			if(clanType != null)
			{
				clanTypes = new SVector<XMLLibrary.XMLTag>();
				clanTypes.add(clanType);
			}
			else
			{
				Log.errOut("Clans","No CLANTYPES found in xml");
				return null;
			}
		}

		final List<ClanGovernment> governments=new SVector<ClanGovernment>();
		for(final XMLTag clanTypePieceTag : clanTypes)
		{
			final String typeName=clanTypePieceTag.parms().get("NAME");
			final int typeID=CMath.s_int(clanTypePieceTag.parms().get("TYPEID"));
			final boolean isDefault=CMath.s_bool(clanTypePieceTag.parms().get("ISDEFAULT"));
			String category=clanTypePieceTag.parms().get("CATEGORY");
			if(category==null)
				category="";

			final Authority[]	baseFunctionChart = new Authority[Function.values().length];
			for(int i=0;i<Function.values().length;i++)
				baseFunctionChart[i]=Authority.CAN_NOT_DO;
			final XMLTag votingTag = clanTypePieceTag.getPieceFromPieces( "VOTING");
			final int maxVotingDays = CMath.s_int(votingTag.parms().get("MAXDAYS"));
			final int minVotingPct = CMath.s_int(votingTag.parms().get("QUORUMPCT"));
			for(final XMLTag piece : votingTag.contents())
			{
				if(piece.tag().equalsIgnoreCase("POWER"))
				{
					final Function power = (Function)CMath.s_valueOf(Clan.Function.values(),piece.value());
					if(power == null)
						Log.errOut("Clans","Illegal power found in xml: "+piece.value());
					else
						baseFunctionChart[power.ordinal()] = Authority.MUST_VOTE_ON;
				}
			}

			final List<ClanPosition> positions=new SVector<ClanPosition>();
			final XMLTag positionsTag = clanTypePieceTag.getPieceFromPieces( "POSITIONS");
			for(final XMLTag posPiece : positionsTag.contents())
			{
				if(posPiece.tag().equalsIgnoreCase("POSITION"))
				{
					final Authority[]	functionChart = baseFunctionChart.clone();
					final String ID=posPiece.parms().get("ID");
					final int roleID=CMath.s_int(posPiece.parms().get("ROLEID"));
					final int rank=CMath.s_int(posPiece.parms().get("RANK"));
					final String name=posPiece.parms().get("NAME");
					final String pluralName=posPiece.parms().get("PLURAL");
					final int max=CMath.s_int(posPiece.parms().get("MAX"));
					final boolean isPublic=CMath.s_bool(posPiece.parms().get("PUBLIC"));
					final String innerMaskStr=CMLib.xml().restoreAngleBrackets(posPiece.parms().get("INNERMASK"));
					for(final XMLTag powerPiece : posPiece.contents())
					{
						if(powerPiece.tag().equalsIgnoreCase("POWER"))
						{
							final Function power = (Function)CMath.s_valueOf(Clan.Function.values(),powerPiece.value());
							if(power == null)
								Log.errOut("Clans","Illegal power found in xml: "+powerPiece.value());
							else
								functionChart[power.ordinal()] = Authority.CAN_DO;
						}
					}
					final ClanPosition P=(ClanPosition)CMClass.getCommon("DefaultClanPosition");
					P.setID(ID);
					P.setRoleID(roleID);
					P.setRank(rank);
					P.setName(name);
					P.setPluralName(pluralName);
					P.setMax(max);
					P.setInnerMaskStr(innerMaskStr);
					P.setFunctionChart(functionChart);
					P.setPublic(isPublic);
					positions.add(P);
				}
			}
			ClanPosition[] posArray = new ClanPosition[positions.size()];
			for(final ClanPosition pos : positions)
			{
				if((pos.getRoleID()>=0)&&(pos.getRoleID()<positions.size()))
				{
					if(posArray[pos.getRoleID()]!=null)
					{
						Log.errOut("Clans","Bad ROLEID "+pos.getRoleID()+" in positions list in "+typeName);
						posArray=new ClanPosition[0];
						break;
					}
					else
					{
						posArray[pos.getRoleID()]=pos;
					}
				}
			}
			if(posArray.length==0)
			{
				Log.errOut("Clans","Missing positions in "+typeName);
				continue;
			}
			final String	autoRoleStr=clanTypePieceTag.getValFromPieces( "AUTOPOSITION");
			ClanPosition autoRole=null;
			for(final ClanPosition pos : positions)
			{
				if(pos.getID().equalsIgnoreCase(autoRoleStr) )
					autoRole=pos;
			}
			if(autoRole==null)
			{
				Log.errOut("Clans","Illegal role found in xml: "+autoRoleStr);
				continue;
			}
			final String	acceptRoleStr=clanTypePieceTag.getValFromPieces( "ACCEPTPOSITION");
			ClanPosition acceptRole=null;
			for(final ClanPosition pos : positions)
			{
				if(pos.getID().equalsIgnoreCase(acceptRoleStr) )
					acceptRole=pos;
			}
			if(acceptRole==null)
			{
				Log.errOut("Clans","Illegal acceptRole found in xml: "+acceptRoleStr);
				continue;
			}
			final String requiredMaskStr=CMLib.xml().restoreAngleBrackets(clanTypePieceTag.getValFromPieces( "REQUIREDMASK"));
			final String entryScript=CMLib.xml().restoreAngleBrackets(clanTypePieceTag.getValFromPieces( "ENTRYSCRIPT"));
			final String exitScript=CMLib.xml().restoreAngleBrackets(clanTypePieceTag.getValFromPieces( "EXITSCRIPT"));
			final String shortDesc=CMLib.xml().restoreAngleBrackets(clanTypePieceTag.getValFromPieces( "SHORTDESC"));
			final String longDesc=CMLib.xml().restoreAngleBrackets(clanTypePieceTag.getValFromPieces( "LONGDESC"));
			final String autoPromoteStr=clanTypePieceTag.getValFromPieces( "AUTOPROMOTEBY");
			final Clan.AutoPromoteFlag autoPromote = AutoPromoteFlag.valueOf(autoPromoteStr);
			if(autoPromote==null)
			{
				Log.errOut("Clans","Illegal AUTOPROMOTEBY found in xml: "+autoPromoteStr);
				continue;
			}
			final boolean isPublic=CMath.s_bool(clanTypePieceTag.getValFromPieces( "PUBLIC"));
			final boolean isFamilyOnly=CMath.s_bool(clanTypePieceTag.getValFromPieces( "FAMILYONLY"));
			final String	overrideMinMembersStr=clanTypePieceTag.getValFromPieces( "OVERRIDEMINMEMBERS");
			Integer overrideMinMembers = null;
			if((overrideMinMembersStr!=null)&&CMath.isInteger(overrideMinMembersStr))
				overrideMinMembers=Integer.valueOf(CMath.s_int(overrideMinMembersStr));
			boolean isRivalrous=true;
			final String rivalrousStr=clanTypePieceTag.getValFromPieces( "RIVALROUS");
			if(CMath.isBool(rivalrousStr))
				isRivalrous=CMath.s_bool(rivalrousStr);
			final String xpPerLevelFormulaStr=clanTypePieceTag.getValFromPieces( "XPPERLEVELFORMULA");
			final XMLTag conquestTag = clanTypePieceTag.getPieceFromPieces( "CONQUEST");
			boolean conquestEnabled=true;
			boolean conquestItemLoyalty=true;
			boolean conquestDeityBasis=false;
			if(conquestTag!=null)
			{
				conquestEnabled=CMath.s_bool(conquestTag.getValFromPieces( "ENABLED"));
				conquestItemLoyalty=CMath.s_bool(conquestTag.getValFromPieces( "ITEMLOYALTY"));
				conquestDeityBasis=CMath.s_bool(conquestTag.getValFromPieces( "DEITYBASIS"));
			}
			final ClanGovernment G=(ClanGovernment)CMClass.getCommon("DefaultClanGovernment");
			G.setID(typeID);
			G.setName(typeName);
			G.setCategory(category);
			G.setPositions(posArray);
			G.setAutoRole(autoRole.getRoleID());
			G.setAcceptPos(acceptRole.getRoleID());
			G.setRequiredMaskStr(requiredMaskStr);
			G.setEntryScript(entryScript);
			G.setExitScript(exitScript);
			G.setAutoPromoteBy(autoPromote);
			G.setPublic(isPublic);
			G.setFamilyOnly(isFamilyOnly);
			G.setOverrideMinMembers(overrideMinMembers);
			G.setConquestEnabled(conquestEnabled);
			G.setConquestItemLoyalty(conquestItemLoyalty);
			G.setConquestByWorship(conquestDeityBasis);
			G.setShortDesc(shortDesc);
			G.setLongDesc(longDesc);
			G.setXpCalculationFormulaStr(xpPerLevelFormulaStr);
			G.setMaxVoteDays(maxVotingDays);
			G.setRivalrous(isRivalrous);
			G.setVoteQuorumPct(minVotingPct);
			G.setDefault(isDefault);

			final XMLTag abilitiesTag = clanTypePieceTag.getPieceFromPieces( "ABILITIES");
			if((abilitiesTag!=null)&&(abilitiesTag.contents()!=null)&&(abilitiesTag.contents().size()>0))
			{
				G.setStat("NUMRABLE", Integer.toString(abilitiesTag.contents().size()));
				for(int x=0;x<abilitiesTag.contents().size();x++)
				{
					final XMLTag able = abilitiesTag.contents().get(x);
					G.setStat("GETRABLE"+x, able.parms().get("ID"));
					G.setStat("GETRABLEPROF"+x, able.parms().get("PROFF"));
					G.setStat("GETRABLEQUAL"+x, able.parms().get("QUALIFYONLY"));
					G.setStat("GETRABLELVL"+x, able.parms().get("LEVEL"));
					G.setStat("GETRABLEPARM"+x, able.parms().get("PARM"));
					G.setStat("GETRABLEROLE"+x, able.parms().get("ROLES"));
				}
			}
			final XMLTag effectsTag = clanTypePieceTag.getPieceFromPieces( "EFFECTS");
			if((effectsTag!=null)&&(effectsTag.contents()!=null)&&(effectsTag.contents().size()>0))
			{
				G.setStat("NUMREFF", Integer.toString(effectsTag.contents().size()));
				for(int x=0;x<effectsTag.contents().size();x++)
				{
					final XMLTag able = effectsTag.contents().get(x);
					G.setStat("GETREFF"+x, able.parms().get("ID"));
					G.setStat("GETREFFPARM"+x, CMLib.xml().restoreAngleBrackets(able.parms().get("PARMS")));
					G.setStat("GETREFFLVL"+x, able.parms().get("LEVEL"));
					G.setStat("GETREFFROLE"+x, able.parms().get("ROLES"));
				}
			}
			governments.add(G);
		}
		final ClanGovernment[] govts=new ClanGovernment[governments.size()];
		for(final ClanGovernment govt : governments)
		{
			if((govt.getID() < 0)||(govt.getID() >=governments.size()) || (govts[govt.getID()]!=null))
			{
				Log.errOut("Clans","Bad TYPEID "+govt.getID());
				return new ClanGovernment[0];
			}
			else
				govts[govt.getID()]=govt;

		}
		if(governments.size()>0)
		{
			for(int i=0;i<govts.length;i++)
			{
				if(govts[i]==null)
					govts[i]=governments.get(0);
			}
			return govts;
		}
		else
		{
			return null;
		}
	}

	@Override
	public Clan getWebPathClanMapping(String webPath)
	{
		if(webPath==null)
			return null;
		return this.webPathClanMappings.get(webPath.toLowerCase().trim());
	}

	@Override
	public String getClanWebTemplateDir(String webPath)
	{
		if(webPath==null)
			return null;
		return this.clanWebPathMappings.get(webPath.toLowerCase().trim());
	}

	private void setClanWebSiteMappings(Clan clan, String allMappingsStr)
	{
		for(final Iterator<String> keyIter = this.webPathClanMappings.keySet().iterator();keyIter.hasNext();)
		{
			final String key=keyIter.next();
			final Clan foundClan = this.webPathClanMappings.get(key);
			if(foundClan == clan)
			{
				keyIter.remove();
				clanWebPathMappings.remove(key);
			}
		}
		if(allMappingsStr==null)
			return;
		final List<String> set=CMParms.parseCommas(allMappingsStr,true);
		for(String s : set)
		{
			final String originalS=s;
			s=s.trim();
			if(s.startsWith("["))
			{
				int x=s.indexOf(']');
				final String cat=s.substring(1,x).trim();
				if(clan.getGovernment().getCategory().equalsIgnoreCase(cat))
				{
					s=s.substring(x+1).trim();
					x=s.lastIndexOf(' ');
					if(x>0)
					{
						String siteFilesPathStr=CMStrings.replaceAll(s.substring(0,x),"<CLANNAME>",clan.clanID());
						siteFilesPathStr=CMStrings.replaceAll(siteFilesPathStr,"<CLANTYPE>",clan.getGovernmentName());
						final String siteFilesPath = new CMFile(siteFilesPathStr.trim(),null).getAbsolutePath();
						final String siteTemplatePath = new CMFile(s.substring(x+1).trim(),null).getAbsolutePath();
						if(webPathClanMappings.containsKey(siteFilesPath.toLowerCase()))
						{
							Log.errOut("Clans","Multiple clans at same webclansites path: in coffeemud.ini: "+originalS);
						}
						else
						{
							webPathClanMappings.put(siteFilesPath.toLowerCase(), clan);
							clanWebPathMappings.put(siteFilesPath.toLowerCase(), siteTemplatePath);
						}
						return;
					}
					else
					{
						Log.errOut("Clans","Unparseable webclansites bit in coffeemud.ini: "+originalS);
					}
				}
			}
		}
	}

	@Override
	public Clan getWebPathClan(String sitePath)
	{
		return webPathClanMappings.get(sitePath.toLowerCase());
	}

	@Override
	public void clanAnnounce(MOB mob, String msg)
	{
		final List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.CLANINFO);
		for(int i=0;i<channels.size();i++)
		{
			CMLib.commands().postChannel(mob,channels.get(i),msg,true);
		}
	}

	protected int filterMedianLevel(List<FullMemberRecord> members)
	{
		final List<Integer> lvls=new SortedListWrap<Integer>(new XVector<Integer>());
		for(final FullMemberRecord r : members)
		{
			if(!r.isAdmin)
			{
				lvls.add(Integer.valueOf(r.level));
			}
		}
		if(lvls.size()>0)
		{
			return lvls.get(lvls.size()/2).intValue();
		}
		return 0;
	}

	protected Clan getTrophyWinner(Trophy trophy)
	{
		for(final Enumeration<Clan> e=clans();e.hasMoreElements();)
		{
			final Clan C=e.nextElement();
			if(CMath.bset(C.getTrophies(),trophy.flagNum()))
			{
				return C;
			}
		}
		return null;
	}

	public void clanTrophyScan()
	{
		if(trophySystemActive())
		{
			// calculate winner of the members count contest
			if(CMProps.getVar(CMProps.Str.CLANTROPMB).length()>0)
			{
				Clan winnerC=getTrophyWinner(Trophy.Members);
				int winnerMembers=(winnerC==null)?0:winnerC.getSize();
				for(final Enumeration<Clan> e=clans();e.hasMoreElements();)
				{
					final Clan C=e.nextElement(); if(C==winnerC) continue;
					final int numMembers=C.getSize();
					if(numMembers>winnerMembers)
					{
						winnerC=C;
						winnerMembers=numMembers;
					}
				}
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.CLANS))
					Log.debugOut("Clans","MBTrophy: "+((winnerC==null)?"Noone":winnerC.clanID())+" won with "+winnerMembers);
				if((winnerC!=null)&&(!CMath.bset(winnerC.getTrophies(),Trophy.Members.flagNum()))&&(winnerC.getExp()>0))
				{
					winnerC.setTrophies(winnerC.getTrophies()|Trophy.Members.flagNum());
					clanAnnounceAll("The "+winnerC.getGovernmentName()+" "+winnerC.name()+" has been awarded the trophy for "+Trophy.Members.description+".");
				}
				for(final Enumeration<Clan> e=clans();e.hasMoreElements();)
				{
					final Clan C=e.nextElement();
					if((winnerC!=C)&&(CMath.bset(C.getTrophies(),Trophy.Members.flagNum())))
					{
						C.setTrophies(C.getTrophies()-Trophy.Members.flagNum());
						C.clanAnnounce(L("The @x1 @x2 has lost control of the trophy for @x3.",C.getGovernmentName(),C.name(),Trophy.Members.description));
					}
				}
			}

			// calculate winner of the member level contest
			if(CMProps.getVar(CMProps.Str.CLANTROPLVL).length()>0)
			{
				Clan winnerC=getTrophyWinner(Trophy.MemberLevel);
				int winnerLevel=(winnerC==null)?0:filterMedianLevel(winnerC.getFullMemberList());
				for(final Enumeration<Clan> e=clans();e.hasMoreElements();)
				{
					final Clan C=e.nextElement(); if(C==winnerC) continue;
					final int highestLevel=filterMedianLevel(C.getFullMemberList());
					if(highestLevel>winnerLevel)
					{
						winnerC=C;
						winnerLevel=highestLevel;
					}
				}
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.CLANS))
					Log.debugOut("DefaultClan","LVLTrophy: "+((winnerC==null)?"Noone":winnerC.clanID())+" won with "+winnerLevel);
				if((winnerC!=null)&&(!CMath.bset(winnerC.getTrophies(),Trophy.MemberLevel.flagNum()))&&(winnerC.getExp()>0))
				{
					winnerC.setTrophies(winnerC.getTrophies()|Trophy.MemberLevel.flagNum());
					clanAnnounceAll("The "+winnerC.getGovernmentName()+" "+winnerC.name()+" has been awarded the trophy for "+Trophy.MemberLevel.description+".");
				}
				for(final Enumeration<Clan> e=clans();e.hasMoreElements();)
				{
					final Clan C=e.nextElement();
					if((winnerC!=C)&&(CMath.bset(C.getTrophies(),Trophy.MemberLevel.flagNum())))
					{
						C.setTrophies(C.getTrophies()-Trophy.MemberLevel.flagNum());
						C.clanAnnounce(L("The @x1 @x2 has lost control of the trophy for @x3.",C.getGovernmentName(),C.name(),Trophy.MemberLevel.description));
					}
				}
			}

			// calculate winner of the exp contest
			if(CMProps.getVar(CMProps.Str.CLANTROPEXP).length()>0)
			{
				Clan winnerC=getTrophyWinner(Trophy.Experience);
				for(final Enumeration<Clan> e=clans();e.hasMoreElements();)
				{
					final Clan C=e.nextElement(); 
					if(C==winnerC) 
						continue;
					if((winnerC==null)||(C.getExp()>winnerC.getExp()))
						winnerC=C;
				}
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.CLANS))
					Log.debugOut("DefaultClan","EXPTrophy: "+((winnerC==null)?"Noone":winnerC.clanID())+" won with "+((winnerC==null)?"0":""+winnerC.getExp()));
				if((winnerC!=null)&&(!CMath.bset(winnerC.getTrophies(),Trophy.Experience.flagNum()))&&(winnerC.getExp()>0))
				{
					winnerC.setTrophies(winnerC.getTrophies()|Trophy.Experience.flagNum());
					clanAnnounceAll("The "+winnerC.getGovernmentName()+" "+winnerC.name()+" has been awarded the trophy for "+Trophy.Experience.description+".");
				}
				for(final Enumeration<Clan> e=clans();e.hasMoreElements();)
				{
					final Clan C=e.nextElement();
					if((winnerC!=C)&&(CMath.bset(C.getTrophies(),Trophy.Experience.flagNum())))
					{
						C.setTrophies(C.getTrophies()-Trophy.Experience.flagNum());
						C.clanAnnounce(L("The @x1 @x2 has lost control of the trophy for @x3.",C.getGovernmentName(),C.name(),Trophy.Experience.description));
					}
				}
			}

			// calculate winner of the pk contest
			if(CMProps.getVar(CMProps.Str.CLANTROPPK).length()>0)
			{
				Clan winnerC=getTrophyWinner(Trophy.PlayerKills);
				for(final Enumeration<Clan> e=clans();e.hasMoreElements();)
				{
					final Clan C=e.nextElement(); 
					if(C==winnerC) 
						continue;
					if((winnerC==null)||(C.getCurrentClanKills(null)>winnerC.getCurrentClanKills(null)))
						winnerC=C;
				}
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.CLANS))
					Log.debugOut("DefaultClan","PKTrophy: "+((winnerC==null)?"Noone":winnerC.clanID())+" won with "+((winnerC==null)?"0":""+winnerC.getCurrentClanKills(null)));
				if((winnerC!=null)
				&&(!CMath.bset(winnerC.getTrophies(),Trophy.PlayerKills.flagNum()))
				&&(winnerC.getCurrentClanKills(null)>0))
				{
					winnerC.setTrophies(winnerC.getTrophies()|Trophy.PlayerKills.flagNum());
					clanAnnounceAll("The "+winnerC.getGovernmentName()+" "+winnerC.name()+" has been awarded the trophy for "+Trophy.PlayerKills.description+".");
				}
				for(final Enumeration<Clan> e=clans();e.hasMoreElements();)
				{
					final Clan C=e.nextElement();
					if((winnerC!=C)&&(CMath.bset(C.getTrophies(),Trophy.PlayerKills.flagNum())))
					{
						C.setTrophies(C.getTrophies()-Trophy.PlayerKills.flagNum());
						C.clanAnnounce(L("The @x1 @x2 has lost control of the trophy for @x3.",C.getGovernmentName(),C.name(),Trophy.PlayerKills.description));
					}
				}
			}

			// calculate winner of the conquest contests
			if((CMProps.getVar(CMProps.Str.CLANTROPAREA).length()>0)
			||(CMProps.getVar(CMProps.Str.CLANTROPCP).length()>0))
			{
				Clan winnerMostClansControlledC=getTrophyWinner(Trophy.Areas);
				long mostClansControlled=(winnerMostClansControlledC==null)?-1:winnerMostClansControlledC.getControlledAreas().size();
				Clan winnerMostControlPointsC=getTrophyWinner(Trophy.Points);
				long mostControlPoints=(winnerMostControlPointsC==null)?-1:winnerMostControlPointsC.calculateMapPoints();
				for(final Enumeration<Clan> e=clans();e.hasMoreElements();)
				{
					final Clan C=e.nextElement();
					if((C!=winnerMostClansControlledC)&&(CMProps.getVar(CMProps.Str.CLANTROPAREA).length()>0))
					{
						final int controlledAreas=C.getControlledAreas().size();
						if(controlledAreas>mostClansControlled)
						{
							winnerMostClansControlledC=C;
							mostClansControlled=controlledAreas;
						}
					}
					if((C!=winnerMostControlPointsC)&&(CMProps.getVar(CMProps.Str.CLANTROPCP).length()>0))
					{
						final long mapPoints=C.calculateMapPoints();
						if(mapPoints>mostControlPoints)
						{
							winnerMostControlPointsC=C;
							mostControlPoints=mapPoints;
						}
					}
				}
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.CLANS))
					Log.debugOut("DefaultClan","AREATrophy: "+((winnerMostClansControlledC==null)?"Noone":winnerMostClansControlledC.clanID())+" won with "+mostClansControlled);
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.CLANS))
					Log.debugOut("DefaultClan","CPTrophy: "+((winnerMostControlPointsC==null)?"Noone":winnerMostControlPointsC.clanID())+" won with "+mostControlPoints);
				if((winnerMostClansControlledC!=null)
				&&(CMProps.getVar(CMProps.Str.CLANTROPAREA).length()>0)
				&&(mostClansControlled>0))
				{
					if(!CMath.bset(winnerMostClansControlledC.getTrophies(),Trophy.Areas.flagNum()))
					{
						winnerMostClansControlledC.setTrophies(winnerMostClansControlledC.getTrophies()|Trophy.Areas.flagNum());
						clanAnnounceAll("The "+winnerMostClansControlledC.getGovernmentName()+" "+winnerMostClansControlledC.name()+" has been awarded the trophy for "+Trophy.Areas.description+".");
					}
				}
				for(final Enumeration<Clan> e=clans();e.hasMoreElements();)
				{
					final Clan C=e.nextElement();
					if((winnerMostClansControlledC!=C)
					&&(CMath.bset(C.getTrophies(),Trophy.Areas.flagNum())))
					{
						C.setTrophies(C.getTrophies()-Trophy.Areas.flagNum());
						C.clanAnnounce(L("The @x1 @x2 has lost control of the trophy for @x3.",C.getGovernmentName(),C.name(),Trophy.Areas.description));
					}
				}
				if((winnerMostControlPointsC!=null)
				&&(CMProps.getVar(CMProps.Str.CLANTROPCP).length()>0)
				&&(mostControlPoints>0))
				{
					if(!CMath.bset(winnerMostControlPointsC.getTrophies(),Trophy.Points.flagNum()))
					{
						winnerMostControlPointsC.setTrophies(winnerMostControlPointsC.getTrophies()|Trophy.Points.flagNum());
						clanAnnounceAll("The "+winnerMostControlPointsC.getGovernmentName()+" "+winnerMostControlPointsC.name()+" has been awarded the trophy for "+Trophy.Points.description+".");
					}
				}
				for(final Enumeration<Clan> e=clans();e.hasMoreElements();)
				{
					final Clan C=e.nextElement();
					if((winnerMostControlPointsC!=C)
					&&(CMath.bset(C.getTrophies(),Trophy.Points.flagNum())))
					{
						C.setTrophies(C.getTrophies()-Trophy.Points.flagNum());
						C.clanAnnounce(L("The @x1 @x2 has lost control of the trophy for @x3.",C.getGovernmentName(),C.name(),Trophy.Points.description));
					}
				}
			}
		}
	}

	@Override
	public boolean activate()
	{
		if(serviceClient==null)
		{
			name="THClans"+Thread.currentThread().getThreadGroup().getName().charAt(0);
			serviceClient=CMLib.threads().startTickDown(this, Tickable.TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK, CMProps.getTickMillis()*CMProps.getTicksPerDay(), 1);
		}
		return true;
	}

	@Override public boolean tick(Tickable ticking, int tickID)
	{
		try
		{
			if(!CMSecurity.isDisabled(CMSecurity.DisFlag.CLANTICKS))
			{
				tickStatus=Tickable.STATUS_ALIVE;
				isDebugging=CMSecurity.isDebugging(DbgFlag.CLANS);
				setThreadStatus(serviceClient,"clan trophy scan");
				clanTrophyScan();
				setThreadStatus(serviceClient,"sleeping");
			}
		}
		finally
		{
			tickStatus=Tickable.STATUS_NOT;
			setThreadStatus(serviceClient,"sleeping");
		}
		return true;
	}

	@Override
	public boolean shutdown()
	{
		for(final Enumeration<Clan> e=all.elements();e.hasMoreElements();)
		{
			final Clan C=e.nextElement();
			CMLib.threads().deleteTick(C,Tickable.TICKID_CLAN);
			CMLib.database().DBUpdateClan(C);
			CMLib.database().DBUpdateClanItems(C);
		}
		all.clear();
		all2.clear();
		if(CMLib.threads().isTicking(this, TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK))
		{
			CMLib.threads().deleteTick(this, TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK);
			serviceClient=null;
		}
		return true;
	}

	@Override
	public void forceTick()
	{
		serviceClient.tickTicker(false);
	}

	@Override
	public void propertiesLoaded()
	{
		super.propertiesLoaded();
		for(final String clanID : all.keySet())
		{
			final Clan C=all.get(clanID);
			setClanWebSiteMappings(C,null);
			CMLib.journals().registerClanForum(C,null);
		}
		for(final String clanID : all.keySet())
		{
			final Clan C=all.get(clanID);
			setClanWebSiteMappings(C,CMProps.getVar(CMProps.Str.CLANWEBSITES));
			CMLib.journals().registerClanForum(C,CMProps.getVar(CMProps.Str.CLANFORUMDATA));
		}
	}

}
