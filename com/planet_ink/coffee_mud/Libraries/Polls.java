package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.DefaultPoll;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.util.*;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;

/*
   Copyright 2005-2018 Bo Zimmerman

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

public class Polls extends StdLibrary implements PollManager
{
	@Override
	public String ID()
	{
		return "Polls";
	}

	public SVector<Poll> pollCache=null;

	@Override
	public boolean shutdown()
	{
		pollCache=null;
		return true;
	}

	@Override
	public void addPoll(Poll P)
	{
		if (getCache() != null)
			getCache().add(P);
	}

	@Override
	public void removePoll(Poll P)
	{
		if (getCache() != null)
			getCache().remove(P);
	}

	public synchronized List<Poll> getCache()
	{
		if(CMSecurity.isDisabled(CMSecurity.DisFlag.POLLCACHE))
			return null;
		if(pollCache==null)
		{
			pollCache=new SVector<Poll>();
			final List<DatabaseEngine.PollData> list=CMLib.database().DBReadPollList();
			Poll P=null;
			for(final DatabaseEngine.PollData data : list)
			{
				P=loadPollByName(data.name());
				if(P!=null)
					pollCache.addElement(P);
			}
		}
		return pollCache;
	}

	@Override
	public Poll getPoll(String named)
	{
		final List<Poll> V=getCache();
		if(V!=null)
		{
			for(final Poll P : V)
				if(P.getName().equalsIgnoreCase(named))
					return P;
		}
		else
		{
			final Poll P=loadPollByName(named);
			return P;
		}
		return null;
	}

	@Override
	public Poll getPoll(int x)
	{
		final Iterator<Poll> p=getPollList();
		Poll P=null;
		for(int i=0;i<=x;i++)
		{
			if(!p.hasNext())
				return null;
			P=p.next();
		}
		return P;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Poll>[] getMyPollTypes(MOB mob, boolean login)
	{
		final Iterator<Poll> i=getPollList();
		final List<Poll> list[]=new List[3];
		for(int l=0;l<3;l++)
			list[l]=new Vector<Poll>();
		for(;i.hasNext();)
		{
			final Poll P = i.next();
			if(loadPollIfNecessary(P))
			{
				if((P.mayIVote(mob))&&(login)&&(CMath.bset(P.getFlags(),Poll.FLAG_NOTATLOGIN)))
					list[1].add(P);
				else
				if(P.mayIVote(mob))
					list[0].add(P);
				else
				if(P.mayISeeResults(mob))
					list[2].add(P);
			}
		}
		return list;
	}

	@Override
	public Iterator<Poll> getPollList()
	{
		final List<Poll> L=getCache();
		if(L!=null)
			return L.iterator();
		final List<DatabaseEngine.PollData> V=CMLib.database().DBReadPollList();
		final List<Poll> list=new Vector<Poll>();
		for(final DatabaseEngine.PollData data : V)
		{
			final Poll P=(Poll)CMClass.getCommon("DefaultPoll");
			P.setName(data.name());
			P.setFlags(data.flag());
			P.setQualZapper(data.qualifyingMask());
			P.setExpiration(data.expiration());
			P.setLoaded(false);
			list.add(P);
		}
		return list.iterator();
	}

	@Override
	public void processVote(Poll P, MOB mob)
	{
		if(!P.mayIVote(mob))
			return;
		try
		{
			if(!loadPollIfNecessary(P))
				return;
			final StringBuffer present=new StringBuffer("");
			present.append("^O"+P.getDescription()+"^N\n\r\n\r");
			if(P.getOptions().size()==0)
			{
				mob.tell(L("@x1Oops! No options defined!",present.toString()));
				return;
			}
			Poll.PollOption PO=null;
			for(int o=0;o<P.getOptions().size();o++)
			{
				PO=P.getOptions().get(o);
				present.append("^H"+CMStrings.padLeft(""+(o+1),2)+": ^N"+PO.text+"\n\r");
			}
			if(CMath.bset(P.getFlags(),Poll.FLAG_ABSTAIN))
				present.append("^H  : ^NPress ENTER to abstain from voting.^?\n\r");

			mob.tell(present.toString());
			int choice=-1;
			while((choice<0)&&(mob.session()!=null)&&(!mob.session().isStopped()))
			{

				final String s=mob.session().prompt(L("Please make your selection (1-@x1): ",""+P.getOptions().size()));
				if((s.length()==0)&&(CMath.bset(P.getFlags(),Poll.FLAG_ABSTAIN)))
					break;
				if(CMath.isInteger(s)&&(CMath.s_int(s)>=1)&&(CMath.s_int(s)<=P.getOptions().size()))
					choice=CMath.s_int(s);
			}
			final Poll.PollResult R=new Poll.PollResult(mob.Name(),"",""+choice);
			if(CMath.bset(P.getFlags(),Poll.FLAG_VOTEBYIP))
			{
				R.ip=mob.session().getAddress();
				if((mob.playerStats()!=null)&&(mob.playerStats().getAccount()!=null))
					R.ip+="\t"+mob.playerStats().getAccount().getAccountName();
			}
			P.addVoteResult(R);
		}
		catch(final java.io.IOException x)
		{
			if(Log.isMaskedErrMsg(x.getMessage()))
				Log.errOut("Polls",x.getMessage());
			else
				Log.errOut("Polls",x);
		}
	}

	@Override
	public void modifyVote(Poll P, MOB mob) throws java.io.IOException
	{
		if((mob.isMonster())||(!CMSecurity.isAllowedAnywhere(mob,CMSecurity.SecFlag.POLLS)))
			return;
		boolean ok=false;
		int showFlag=-1;
		if(CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0)
			showFlag=-999;
		final String oldName=P.getName();
		while(!ok)
		{
			int showNumber=0;
			String possName=CMLib.genEd().prompt(mob,P.getName(),++showNumber,showFlag,L("Name"));
			while((!possName.equalsIgnoreCase(P.getName()))&&(CMLib.polls().getPoll(possName)!=null))
				possName=possName+"!";
			P.setName(possName);
			P.setDescription(CMLib.genEd().prompt(mob,P.getDescription(),++showNumber,showFlag,L("Introduction")));
			P.setSubject(CMLib.genEd().prompt(mob,P.getSubject(),++showNumber,showFlag,L("Results Header")));
			if(P.getSubject().length()>250)
				P.setSubject(P.getSubject().substring(0,250));
			if(P.getAuthor().length()==0)
				P.setAuthor(mob.Name());
			P.setQualZapper(CMLib.genEd().prompt(mob,P.getQualZapper(),++showNumber,showFlag,L("Qual. Mask"),true));
			P.setFlags((CMLib.genEd().prompt(mob,CMath.bset(P.getFlags(),Poll.FLAG_ACTIVE),++showNumber,showFlag,L("Poll Active")))?
				CMath.setb(P.getFlags(),Poll.FLAG_ACTIVE):CMath.unsetb(P.getFlags(),Poll.FLAG_ACTIVE));
			P.setFlags((CMLib.genEd().prompt(mob,CMath.bset(P.getFlags(),Poll.FLAG_PREVIEWRESULTS),++showNumber,showFlag,L("Preview Results")))?
					CMath.setb(P.getFlags(),Poll.FLAG_PREVIEWRESULTS):CMath.unsetb(P.getFlags(),Poll.FLAG_PREVIEWRESULTS));
			P.setFlags((CMLib.genEd().prompt(mob,CMath.bset(P.getFlags(),Poll.FLAG_ABSTAIN),++showNumber,showFlag,L("Allow Abstention")))?
					CMath.setb(P.getFlags(),Poll.FLAG_ABSTAIN):CMath.unsetb(P.getFlags(),Poll.FLAG_ABSTAIN));
			P.setFlags((CMLib.genEd().prompt(mob,CMath.bset(P.getFlags(),Poll.FLAG_VOTEBYIP),++showNumber,showFlag,L("Use IP Addresses/Accounts")))?
					CMath.setb(P.getFlags(),Poll.FLAG_VOTEBYIP):CMath.unsetb(P.getFlags(),Poll.FLAG_VOTEBYIP));
			P.setFlags((CMLib.genEd().prompt(mob,CMath.bset(P.getFlags(),Poll.FLAG_HIDERESULTS),++showNumber,showFlag,L("Hide Results")))?
					CMath.setb(P.getFlags(),Poll.FLAG_HIDERESULTS):CMath.unsetb(P.getFlags(),Poll.FLAG_HIDERESULTS));
			P.setFlags((CMLib.genEd().prompt(mob,CMath.bset(P.getFlags(),Poll.FLAG_NOTATLOGIN),++showNumber,showFlag,L("POLL CMD only")))?
					CMath.setb(P.getFlags(),Poll.FLAG_NOTATLOGIN):CMath.unsetb(P.getFlags(),Poll.FLAG_NOTATLOGIN));
			String expirationDate="NA";
			if(P.getExpiration()>0)
				expirationDate=CMLib.time().date2String(P.getExpiration());

			expirationDate=CMLib.genEd().prompt(mob,expirationDate,++showNumber,showFlag,L("Exp. Date (MM/DD/YYYY HH:MM AP)"),true);
			if((expirationDate.trim().length()==0)||(expirationDate.equalsIgnoreCase("NA")))
				P.setExpiration(0);
			else
			{ try{P.setExpiration(CMLib.time().string2Millis(expirationDate.trim()));}catch(final Exception e){}}

			final Vector<Poll.PollOption> del=new Vector<Poll.PollOption>();
			for(int i=0;i<P.getOptions().size();i++)
			{
				final Poll.PollOption PO=P.getOptions().get(i);
				PO.text=CMLib.genEd().prompt(mob,PO.text,++showNumber,showFlag,L("Vote Option"),true);
				if(PO.text.length()==0)
					del.addElement(PO);
			}
			for(int i=0;i<del.size();i++)
				P.getOptions().remove(del.elementAt(i));

			Poll.PollOption PO=null;
			while(!mob.session().isStopped())
			{
				PO=new Poll.PollOption(
						CMLib.genEd().prompt(mob,"",++showNumber,showFlag,L("New Vote Option"),true)
				);
				if(PO.text.length()==0)
					break;
				P.getOptions().add(PO);
			}
			if(showFlag<-900)
			{
				ok=true;
				break;
			}
			if(showFlag>0)
			{
				showFlag=-1;
				continue;
			}
			showFlag=CMath.s_int(mob.session().prompt(L("Edit which? "),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
			}
		}
		updatePoll(oldName,P);
	}

	@Override
	public void processResults(Poll P, MOB mob)
	{
		if(!P.mayISeeResults(mob))
			return;
		if(!loadPollIfNecessary(P))
			return;
		final StringBuffer present=new StringBuffer("");
		present.append("^O"+P.getSubject()+"^N\n\r\n\r");
		if(P.getOptions().size()==0)
		{
			mob.tell(L("@x1Oops! No options defined!",present.toString()));
			return;
		}
		int total=0;
		final int[] votes=new int[P.getOptions().size()+(CMath.bset(P.getFlags(),Poll.FLAG_ABSTAIN)?1:0)];
		Poll.PollResult R=null;
		int choice=0;
		for(int r=0;r<P.getResults().size();r++)
		{
			R=P.getResults().get(r);
			choice=CMath.s_int(R.answer);
			if(((choice<=0)&&CMath.bset(P.getFlags(),Poll.FLAG_ABSTAIN))
			||((choice>=0)&&(choice<=P.getOptions().size())))
			{
				total++;
				if(choice<=0)
					votes[votes.length-1]++;
				else
					votes[choice-1]++;
			}
		}
		Poll.PollOption O=null;
		for(int o=0;o<P.getOptions().size();o++)
		{
			O=P.getOptions().get(o);
			int pct=0;
			if(total>0)
				pct=(int)Math.round(CMath.div(votes[o],total)*100.0);
			present.append(CMStrings.padRight("^H"+(o+1),2)+": ^N"+O.text+" ^O(Votes: "+votes[o]+" - "+pct+"%)^N\n\r");
		}
		if(CMath.bset(P.getFlags(),Poll.FLAG_ABSTAIN))
		{
			int pct=0;
			if(total>0)
				pct=(int)Math.round(CMath.div(votes[votes.length-1],total)*100.0);
			present.append("    ^NAbstentions ^O("+votes[votes.length-1]+" - "+pct+"%)^N\n\r");
		}
		mob.tell(present.toString());
	}

	@Override
	public void createPoll(Poll P)
	{
		addPoll(P);
		CMLib.database().DBCreatePoll(P.getName(),
									  P.getAuthor(),
									  P.getSubject(),
									  P.getDescription(),
									  P.getOptionsXML(),
									  (int)P.getFlags(),
									  P.getQualZapper(),
									  P.getResultsXML(),
									  P.getExpiration());
	}

	@Override
	public void updatePollResults(Poll P)
	{
		CMLib.database().DBUpdatePollResults(P.getName(),P.getResultsXML());
	}

	@Override
	public void updatePoll(String oldName, Poll P)
	{
		CMLib.database().DBUpdatePoll(oldName,
									  P.getName(),
									  P.getAuthor(),
									  P.getSubject(),
									  P.getDescription(),
									  P.getOptionsXML(),
									  (int)P.getFlags(),
									  P.getQualZapper(),
									  P.getResultsXML(),
									  P.getExpiration());
	}

	@Override
	public void deletePoll(Poll P)
	{
		removePoll(P);
		CMLib.database().DBDeletePoll(P.getName());
	}

	@Override
	public boolean loadPollIfNecessary(Poll P)
	{
		if(P.loaded())
			return true;
		final DatabaseEngine.PollData data =CMLib.database().DBReadPoll(P.getName());
		if(data==null)
			return false;
		P.setName(data.name());
		P.setAuthor(data.authorName());
		P.setSubject(data.subject());
		P.setDescription(data.description());
		final Vector<Poll.PollOption> options=new Vector<Poll.PollOption>();
		P.setOptions(options);
		final String optionsXML=data.optionsXml();
		List<XMLLibrary.XMLTag> V2=CMLib.xml().parseAllXML(optionsXML);
		XMLTag OXV=CMLib.xml().getPieceFromPieces(V2,"OPTIONS");
		if((OXV!=null)&&(OXV.contents()!=null)&&(OXV.contents().size()>0))
		for(int v2=0;v2<OXV.contents().size();v2++)
		{
			final XMLTag XP=OXV.contents().get(v2);
			if(!XP.tag().equalsIgnoreCase("option"))
				continue;
			final Poll.PollOption PO=new Poll.PollOption(
					CMLib.xml().restoreAngleBrackets(XP.getValFromPieces("TEXT"))
			);
			options.addElement(PO);
		}
		P.setFlags(data.flag());
		P.setQualZapper(data.qualifyingMask());
		final Vector<Poll.PollResult> results=new Vector<Poll.PollResult>();
		P.setResults(results);
		final String resultsXML=data.resultsXml();
		V2=CMLib.xml().parseAllXML(resultsXML);
		OXV=CMLib.xml().getPieceFromPieces(V2,"RESULTS");
		if((OXV!=null)&&(OXV.contents()!=null)&&(OXV.contents().size()>0))
		for(int v2=0;v2<OXV.contents().size();v2++)
		{
			final XMLTag XP=OXV.contents().get(v2);
			if(!XP.tag().equalsIgnoreCase("result"))
				continue;
			final Poll.PollResult PR=new Poll.PollResult(
					XP.getValFromPieces("USER"),
					XP.getValFromPieces("IP"),
					XP.getValFromPieces("ANS"));
			results.addElement(PR);
		}
		P.setExpiration(data.expiration());
		P.setLoaded(true);
		return true;
	}

	@Override
	public Poll loadPollByName(String name)
	{
		final Poll P=(Poll)CMClass.getCommon("DefaultPoll");
		P.setLoaded(false);
		P.setName(name);
		return loadPollIfNecessary(P)?P:null;
	}

}
