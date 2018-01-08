package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.Authority;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.Function;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2004-2018 Bo Zimmerman

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

public class ClanVote extends StdCommand
{
	public ClanVote()
	{
	}

	private final String[]	access	= I(new String[] { "CLANVOTE" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		StringBuffer msg=new StringBuffer("");
		String voteNumStr=(commands.size()>1)?(String)commands.get(commands.size()-1):"";
		String clanName="";
		if(!CMath.isInteger(voteNumStr))
		{
			clanName=(commands.size()>2)?CMParms.combine(commands,1,commands.size()):"";
			voteNumStr="";
		}
		else
			clanName=(commands.size()>2)?CMParms.combine(commands,1,commands.size()-1):"";

		Clan C=null;
		Integer clanRole=null;
		for(final Pair<Clan,Integer> c : mob.clans())
		{
			if((clanName.length()==0)||(CMLib.english().containsString(c.first.getName(), clanName)))
			{
				C = c.first;
				clanRole = c.second;
				break;
			}
		}

		if((C==null)||(clanRole==null))
		{
			mob.tell(L("You can't vote for anything in @x1.",((clanName.length()==0)?"any clan":clanName)));
			return false;
		}
		else
		if(!mob.isMonster())
		{
			final Vector<Clan.ClanVote> votesForYou=new Vector<Clan.ClanVote>();
			for(final Enumeration<Clan.ClanVote> e=C.votes();e.hasMoreElements();)
			{
				final Clan.ClanVote CV=e.nextElement();
				if(((CV.function==Clan.Function.ASSIGN.ordinal())
					&&(C.getAuthority(clanRole.intValue(),Clan.Function.VOTE_ASSIGN)!=Clan.Authority.CAN_NOT_DO))
				||((CV.function!=Clan.Function.ASSIGN.ordinal())
					&&(C.getAuthority(clanRole.intValue(),Clan.Function.VOTE_OTHER)!=Clan.Authority.CAN_NOT_DO)))
						votesForYou.add(CV);
			}
			if(voteNumStr.length()==0)
			{
				if(votesForYou.size()==0)
					msg.append(L("Your @x1 does not have anything up for your vote.",C.getGovernmentName()));
				else
				{
					msg.append(L(" @x1@x2Command to execute\n\r",CMStrings.padRight("#",3),CMStrings.padRight(L("Status"),15)));
					for(int v=0;v<votesForYou.size();v++)
					{
						final Clan.ClanVote CV=votesForYou.get(v);
						final boolean ivoted=((CV.votes!=null)&&(CV.votes.containsFirst(mob.Name())));
						final int votesCast=(CV.votes!=null)?CV.votes.size():0;
						msg.append((ivoted?"*":" ")
								  +CMStrings.padRight(""+(v+1),3)
								  +CMStrings.padRight(((CV.voteStatus==Clan.VSTAT_STARTED)?(votesCast+" votes cast"):(Clan.VSTAT_DESCS[CV.voteStatus])),15)
								  +CMStrings.padRight(CV.matter,55)+"\n\r");
					}
					msg.append(L("\n\rEnter CLANVOTE [#] to see details or place your vote."));
				}
			}
			else
			{
				final int which=CMath.s_int(voteNumStr)-1;
				Clan.ClanVote CV=null;
				if((which>=0)&&(which<votesForYou.size()))
					CV=votesForYou.get(which);
				if(CV==null)
					msg.append(L("That vote does not exist.  Use CLANVOTE to see a list."));
				else
				{
					int yeas=0;
					int nays=0;
					Boolean myVote=null;
					if(CV.votes!=null)
					{
						for(int vs=0;vs<CV.votes.size();vs++)
						{
							if(CV.votes.getFirst(vs).equals(mob.Name()))
								myVote=CV.votes.getSecond(vs);
							if(CV.votes.getSecond(vs).booleanValue())
								yeas++;
							else
								nays++;
						}
					}
					msg.append(L("Vote       : @x1\n\r",""+(which+1)));
					msg.append(L("Started by : @x1\n\r",CV.voteStarter));
					if(CV.voteStatus==Clan.VSTAT_STARTED)
						msg.append(L("Started on : @x1\n\r",CMLib.time().date2String(CV.voteStarted)));
					else
						msg.append(L("Ended on   : @x1\n\r",CMLib.time().date2String(CV.voteStarted)));
					msg.append(L("Status     : @x1\n\r",Clan.VSTAT_DESCS[CV.voteStatus]));
					switch(CV.voteStatus)
					{
					case Clan.VSTAT_STARTED:
						msg.append(L("If passed, the following command would be executed:\n\r"));
						break;
					case Clan.VSTAT_PASSED:
						msg.append(L("Results    : @x1 Yeas, @x2 Nays\n\r",""+yeas,""+nays));
						msg.append(L("The following command has been executed:\n\r"));
						break;
					case Clan.VSTAT_FAILED:
						msg.append(L("Results    : @x1 Yeas, @x2 Nays\n\r",""+yeas,""+nays));
						msg.append(L("The following command will not be executed:\n\r"));
						break;
					}
					msg.append(CV.matter+"\n\r");
					if((CV.voteStatus==Clan.VSTAT_STARTED)&&(myVote==null))
					{
						mob.tell(msg.toString());
						msg=new StringBuffer("");
						final StringBuffer prompt=new StringBuffer("");
						String choices="";
						if(CV.votes==null)
							CV.votes=new PairVector<String,Boolean>();
						prompt.append("Y)EA N)AY ");
						choices="YN";
						if(CV.voteStarter.equals(mob.Name()))
						{
							prompt.append("C)ANCEL ");
							choices+="C";
						}
						final String enterWhat="to skip";
						//if(myVote!=null) enterWhat=("to keep ("+(myVote.booleanValue()?"Y":"N")+") "); // no revote
						boolean updateVote=false;
						if((prompt.length()>0)&&(mob.session()!=null))
						{
							final String answer=mob.session().choose(L("Choices: @x1or ENTER @x2: ",prompt.toString(),enterWhat),choices,"");
							if(answer.length()>0)
							switch(answer.toUpperCase().charAt(0))
							{
							case 'Y':
								msg.append(L("Your YEA vote is recorded."));
								CV.votes.add(mob.Name(),Boolean.TRUE);
								updateVote=true;
								yeas++;
								break;
							case 'N':
								CV.votes.add(mob.Name(),Boolean.FALSE);
								msg.append(L("Your NAY vote is recorded."));
								updateVote=true;
								nays++;
								break;
							case 'C':
								if((mob.session()!=null)
								&&(mob.session().confirm(L("This will cancel this entire vote, are you sure (N/y)?"),"N")))
								{
									C.delVote(CV);
									CMLib.clans().clanAnnounce(mob,L("A prior vote for @x1 @x2 has been deleted.",C.getGovernmentName(),C.clanID()));
									msg.append(L("The vote has been deleted."));
									updateVote=true;
								}
								break;
							}
						}
						final int numVotes=C.getNumVoters(Function.values()[CV.function]);
						if(numVotes<=(yeas+nays))
						{
							updateVote=true;
							if(yeas<=nays)
								CV.voteStatus=Clan.VSTAT_FAILED;
							else
							{
								CV.voteStatus=Clan.VSTAT_PASSED;
								final MOB mob2=CMClass.getFactoryMOB();
								mob2.setName(C.clanID());
								mob2.setClan(C.clanID(),C.getTopRankedRoles(Function.ASSIGN).get(0).intValue());
								mob2.basePhyStats().setLevel(1000);
								if(mob2.location()==null)
								{
									mob2.setLocation(mob2.getStartRoom());
									if(mob2.location()==null)
										mob2.setLocation(CMLib.map().getRandomRoom());
								}
								final Vector<String> V=CMParms.parse(CV.matter);
								mob2.doCommand(V,metaFlags|MUDCmdProcessor.METAFLAG_FORCED);
								mob2.destroy();
							}
						}
						if(updateVote)
							C.updateVotes();
					}
				}
			}
		}
		mob.tell(msg.toString());
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return false;
	}

}
