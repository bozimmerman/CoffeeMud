package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class ClanVote extends BaseClanner
{
	public ClanVote(){}

	private String[] access={"CLANVOTE"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		StringBuffer msg=new StringBuffer("");
		if((mob.getClanID()==null)
		||(mob.getClanID().equalsIgnoreCase(""))
		||(Clans.getClan(mob.getClanID())==null))
		{
			msg.append("You aren't even a member of a clan.");
		}
		else
		if(!mob.isMonster())
		{
			Clan C=Clans.getClan(mob.getClanID());
			if(C==null) return false;
			Vector votesForYou=new Vector();
			for(Enumeration e=C.votes();e.hasMoreElements();)
			{
				Clans.ClanVote CV=(Clans.ClanVote)e.nextElement();
				if(((CV.function==Clan.FUNC_CLANASSIGN)&&(C.allowedToDoThis(mob,Clan.FUNC_CLANVOTEASSIGN)>=0))
				||((CV.function!=Clan.FUNC_CLANASSIGN)&&(C.allowedToDoThis(mob,Clan.FUNC_CLANVOTEOTHER)>=0)))
					votesForYou.addElement(CV);
			}
			if(commands.size()<2)
			{
				if(votesForYou.size()==0)
					msg.append("Your "+C.typeName()+" does not have anything up for your vote.");
				else
				{
					msg.append(" "+Util.padRight("#",3)
							   +Util.padRight("Status",15)
							   +"Command to execute\n\r");
					for(int v=0;v<votesForYou.size();v++)
					{
						Clans.ClanVote CV=(Clans.ClanVote)votesForYou.elementAt(v);
						boolean ivoted=((CV.votes!=null)&&(CV.votes.contains(mob.Name())));
						int votesCast=(CV.votes!=null)?CV.votes.size():0;
						msg.append((ivoted?"*":" ")
								  +Util.padRight(""+(v+1),3)
								  +Util.padRight(((CV.voteStatus==Clan.VSTAT_STARTED)?(votesCast+" votes cast"):(Clan.VSTAT_DESCS[CV.voteStatus])),15)
								  +Util.padRight(CV.matter,55)+"\n\r");
					}
					msg.append("\n\rEnter CLANVOTE [#] to see details or place your vote.");
				}
			}
			else
			{
				int which=Util.s_int(Util.combine(commands,1))-1;
				Clans.ClanVote CV=null;
				if((which>=0)&&(which<votesForYou.size()))
					CV=(Clans.ClanVote)votesForYou.elementAt(which);
				if(CV==null)
					msg.append("That vote does not exist.  Use CLANVOTE to see a list.");
				else
				{
					int yeas=0;
					int nays=0;
					Boolean myVote=null;
					if(CV.votes!=null)
						for(int vs=0;vs<CV.votes.size();vs++)
						{
							if(((String)CV.votes.elementAt(vs,1)).equals(mob.Name()))
							   myVote=(Boolean)CV.votes.elementAt(vs,2);
							if(((Boolean)CV.votes.elementAt(vs,2)).booleanValue())
								yeas++;
							else
								nays++;
						}
					msg.append("Vote       : "+(which+1)+"\n\r");
					msg.append("Started by : "+CV.voteStarter+"\n\r");
					if(CV.voteStatus==Clan.VSTAT_STARTED)
						msg.append("Started on : "+IQCalendar.d2String(CV.voteStarted)+"\n\r");
					else
						msg.append("Ended on   : "+IQCalendar.d2String(CV.voteStarted)+"\n\r");
					msg.append("Status     : "+Clan.VSTAT_DESCS[CV.voteStatus]+"\n\r");
					switch(CV.voteStatus)
					{
					case Clan.VSTAT_STARTED:
						msg.append("If passed, the following command would be executed:\n\r");
						break;
					case Clan.VSTAT_PASSED:
						msg.append("Results    : "+yeas+" Yeas, "+nays+" Nays\n\r");
						msg.append("The following command has been executed:\n\r");
						break;
					case Clan.VSTAT_FAILED:
						msg.append("Results    : "+yeas+" Yeas, "+nays+" Nays\n\r");
						msg.append("The following command will not be executed:\n\r");
						break;
					}
					msg.append(CV.matter+"\n\r");
					if(CV.voteStatus==Clan.VSTAT_STARTED)
					{
						mob.tell(msg.toString());
						msg=new StringBuffer("");
						StringBuffer prompt=new StringBuffer("");
						String choices="";
						if(CV.votes==null) CV.votes=new DVector(2);
						prompt.append("Y)EA N)AY ");
						choices="YN";
						if(CV.voteStarter.equals(mob.Name()))
						{
							prompt.append("C)ANCEL ");
							choices+="C";
						}
						String enterWhat="to skip";
						if(myVote!=null)
							enterWhat=("to keep ("+(myVote.booleanValue()?"Y":"N")+") ");
						boolean updateVote=false;
						if((prompt.length()>0)&&(mob.session()!=null))
						{
							String answer=mob.session().choose("Choices: "+prompt.toString()+"or ENTER "+enterWhat+": ",choices,"");
							if(answer.length()>0)
							switch(answer.toUpperCase().charAt(0))
							{
							case 'Y':
								msg.append("Your YEA vote is recorded.");
								CV.votes.addElement(mob.Name(),new Boolean(true));
								updateVote=true;
								yeas++;
								break;
							case 'N':
								CV.votes.addElement(mob.Name(),new Boolean(false));
								msg.append("Your NAY vote is recorded.");
								updateVote=true;
								nays++;
								break;
							case 'C':
								if((mob.session()!=null)
								&&(mob.session().confirm("This will cancel this entire vote, are you sure (N/y)?","N")))
								{
									C.delVote(CV);
									clanAnnounce(mob,"A prior vote has been deleted.");
									msg.append("The vote has been deleted.");
									updateVote=true;
								}
								break;
							}
						}
						int numVotes=C.getNumVoters(CV.function);
						if(numVotes<=(yeas+nays))
						{
							updateVote=true;
							if(yeas<=nays)
								CV.voteStatus=Clan.VSTAT_FAILED;
							else
							{
								CV.voteStatus=Clan.VSTAT_PASSED;
								MOB mob2=CMClass.getMOB("StdMOB");
								mob2.setName(C.ID());
								mob2.setClanID(C.ID());
								mob2.setClanRole(Clan.POS_BOSS);
								mob2.baseEnvStats().setLevel(1000);
								if(mob2.location()==null)
								{
									mob2.setLocation(mob2.getStartRoom());
									if(mob2.location()==null)
										mob2.setLocation(CMMap.getRandomRoom());
								}
								Vector V=Util.parse(CV.matter);
								mob2.doCommand(V);
							}
						}
						if(updateVote)	C.updateVotes();
					}
				}
			}
		}
		mob.tell(msg.toString());
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return false;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
