package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class BaseClanner extends StdCommand
{
	public static boolean goForward(MOB mob, Clan C, Vector commands, int function, boolean voteIfNecessary)
	{
		if((mob==null)||(C==null)) return false;
		int allowed=C.allowedToDoThis(mob,function);
		//System.out.println(mob.Name()+"/"+C.ID()+"/role="+mob.getClanRole()+"/f="+function+"/vnow="+voteIfNecessary+"/gvt="+C.getGovernment()+"/ans="+allowed);
		if(allowed==1) return true;
		if(allowed==-1) return false;
		if(function==Clans.FUNC_CLANASSIGN)
		{
			if(C.allowedToDoThis(mob,Clans.FUNC_CLANVOTEASSIGN)<=0)
			   return false;
		}
		else
		if(C.allowedToDoThis(mob,Clans.FUNC_CLANVOTEOTHER)<=0)
		   return false;
		if(!voteIfNecessary) return true;
		String matter=Util.combine(commands,0);
		for(Enumeration e=C.votes();e.hasMoreElements();)
		{
			Clans.ClanVote CV=(Clans.ClanVote)e.nextElement();
			if((CV.voteStarter.equalsIgnoreCase(mob.Name()))
			&&(CV.voteStatus==Clans.VSTAT_STARTED))
			{
				mob.tell("This matter must be voted upon, but you already have a vote underway.");
				return false;
			}
			if(CV.matter.equalsIgnoreCase(matter))
			{
				mob.tell("This matter must be voted upon, and is already BEING voted upon.  Use CLANVOTE to see.");
				return false;
			}
		}
		if(mob.session()==null) return false;
		try{
			int numVotes=C.getNumVoters(function);
			if(numVotes==1) return true;

			if(mob.session().confirm("This matter must be voted upon.  Would you like to start the vote now (y/N)?","N"))
			{
				Clans.ClanVote CV=new Clans.ClanVote();
				CV.matter=matter;
				CV.voteStarter=mob.Name();
				CV.function=function;
				CV.voteStarted=System.currentTimeMillis();
				CV.votes=new DVector(2);
				CV.voteStatus=Clan.VSTAT_STARTED;
				C.addVote(CV);
				C.updateVotes();
				switch(C.getGovernment())
				{
				case Clan.GVT_DEMOCRACY:
					clanAnnounce(mob,"A new matter has come up for your votes. Use CLANVOTE to participate.");
					break;
				case Clan.GVT_DICTATORSHIP:
					clanAnnounce(mob,"Something has come up for vote -- lord only knows how.");
					break;
				case Clan.GVT_OLIGARCHY:
					clanAnnounce(mob,"The guildmasters have a new matter to vote upon. They should use CLANVOTE to participate.");
					break;
				case Clan.GVT_REPUBLIC:
					if(function==Clan.FUNC_CLANASSIGN)
						clanAnnounce(mob,"A new election has come up for your votes. Use CLANVOTE to participate.");
					else
						clanAnnounce(mob,"The senators have a new matter to vote upon. They should use CLANVOTE to participate.");
					break;
				}
				mob.tell("Your vote has started.  Use CLANVOTE to cast your vote.");
				return false;
			}
		}
		catch(java.io.IOException e){}
		mob.tell("Without a vote, this command can not be executed.");
		return false;
	}


	public static void clanAnnounce(MOB mob, String msg)
	{
		CommonMsgs.channel(mob,"CLANTALK",msg,true);
	}

	public static void addClanHomeSpell(MOB M)
	{
		if(M.fetchAbility("Spell_ClanHome")==null)
		{
			M.addAbility(CMClass.findAbility("Spell_ClanHome"));
			(M.fetchAbility("Spell_ClanHome")).setProfficiency(50);
		}
		if(M.fetchAbility("Spell_ClanDonate")==null)
		{
			M.addAbility(CMClass.findAbility("Spell_ClanDonate"));
			(M.fetchAbility("Spell_ClanDonate")).setProfficiency(100);
		}
		CMClass.DBEngine().DBUpdateMOB(M);
	}

	public static void delClanHomeSpell(MOB mob)
	{
		mob.delAbility(mob.fetchAbility("Spell_ClanHome"));
		mob.delAbility(mob.fetchAbility("Spell_ClanDonate"));
		CMClass.DBEngine().DBUpdateMOB(mob);
	}

	public static int getIntFromRole(int roleType)
	{
		switch(roleType)
		{
		case Clan.POS_APPLICANT: return 0;
		case Clan.POS_MEMBER: return 1;
		case Clan.POS_STAFF: return 2;
		case Clan.POS_ENCHANTER: return 3;
		case Clan.POS_TREASURER: return 4;
		case Clan.POS_LEADER: return 5;
		case Clan.POS_BOSS: return 6;

		}
		return 0;
	}
	public static int getRoleFromName(int government, String position)
	{
		if((government<0)||(government>=Clans.GVT_DESCS.length))
			government=0;
		String[] roles=Clans.ROL_DESCS[government];
		for(int i=0;i<roles.length;i++)
			if(roles[i].startsWith(position.toUpperCase()))
				return Util.pow(2,i-1);
		return -1;
	}

}
