package com.planet_ink.coffee_mud.common;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
/**
  * Global utility Vector for holding and creating Clans
  * @author=Jeremy Vyska
  */
public class Clans implements Clan, Tickable
{
	private long tickStatus=Tickable.STATUS_NOT;
	public long getTickStatus(){return tickStatus;}
	private static Vector all=new Vector();

	
	public static MOB getMOB(String last)
	{
		if(!ExternalPlay.getSystemStarted())
			return null;

		MOB M=CMMap.getPlayer(last);
		if(M==null)
			for(Enumeration p=CMMap.players();p.hasMoreElements();)
			{
				MOB mob2=(MOB)p.nextElement();
				if(mob2.Name().equalsIgnoreCase(last))
				{ M=mob2; break;}
			}
		MOB TM=CMClass.getMOB("StdMOB");
		if((M==null)&&(ExternalPlay.DBUserSearch(TM,last)))
		{
			M=CMClass.getMOB("StdMOB");
			M.setName(TM.Name());
			ExternalPlay.DBReadMOB(M);
			ExternalPlay.DBReadFollowers(M,false);
			if(M.playerStats()!=null)
				M.playerStats().setUpdated(M.playerStats().lastDateTime());
			M.recoverEnvStats();
			M.recoverCharStats();
		}
		return M;
	}
	
	
	public static void shutdownClans()
	{
		for(int i=0;i<all.size();i++)
		{
			Clan C=(Clan)all.elementAt(i);
			ExternalPlay.deleteTick(C,Host.CLAN_TICK);
		}
		all.clear();
	}
	
	public static Clan getClan(String name)
	{
		for(int i=0;i<all.size();i++)
		{
			Clan C=(Clan)all.elementAt(i);
			if(C.ID().equalsIgnoreCase(name))
			{
				return C;
			}
		}
		return null;
	}

	public static Clan getClanType(int type)
	{
		switch(type)
		{
		case Clan.TYPE_CLAN:
			return new Clans();
		default:
			return new Clans();
		}
	}

	public static String getRoleName(int role, boolean titleCase, boolean plural)
	{
		StringBuffer roleName=new StringBuffer();
		Character c;
		switch(role)
		{
		case Clan.POS_APPLICANT:
			roleName.append("applicant");
			break;
		case Clan.POS_STAFF:
			roleName.append("staff");
			break;
		case Clan.POS_MEMBER:
			roleName.append("member");
			break;
		case Clan.POS_TREASURER:
			roleName.append("treasurer");
			break;
		case Clan.POS_LEADER:
			roleName.append("leader");
			break;
		case Clan.POS_BOSS:
			roleName.append("boss");
			break;
		default:
			roleName.append("member");
			break;
		}
		if(titleCase)
		{
		  String titled=Util.capitalize(roleName.toString());
		  roleName.setLength(0);
		  roleName.append(titled);
		}
		if(plural)
		{
			if(new Character(roleName.charAt(roleName.length()-1)).equals(new Character((new String("f")).charAt(0))))
			{
				// do nothing
			}
			else
			if(new Character(roleName.charAt(roleName.length()-1)).equals(new Character((new String("s")).charAt(0))))
			{
				roleName.append("es");
			}
			else
			{
				roleName.append("s");
			}
		}
		return roleName.toString();
	}

	public static boolean checkDates(Clan C)
	{
		return false;
	}

	public static void createClan(Clan C)
	{
		ExternalPlay.DBCreateClan(C);
		addElement(C);
	}

	public static void updateClan(Clan C)
	{
		ExternalPlay.DBUpdateClan(C);
	}

	public static void destroyClan(Clan C)
	{
		Vector members=C.getMemberList();
		for(int m=0;m<members.size();m++)
		{
			String member=(String)members.elementAt(m);
			MOB M=getMOB(member);
			if(M!=null)
			{
				M.setClanID("");
				M.setClanRole(0);
				ExternalPlay.DBUpdateClanMembership(M.Name(), "", 0);
			}
		}
		ExternalPlay.DBDeleteClan(C);
		removeElement(C);
	}

	public static Clan elementAt(int x)
	{
		return (Clan)all.elementAt(x);
	}
	public static int size()
	{
		return all.size();
	}
	public static void addElement(Clan C)
	{
		ExternalPlay.startTickDown(C,Host.CLAN_TICK,(int)Host.TICKS_PER_DAY);
		all.addElement(C);
	}
	public static void removeElementAt(int x)
	{
		removeElement(elementAt(x));
	}
	public static void removeElement(Clan C)
	{
		ExternalPlay.deleteTick(C,Host.CLAN_TICK);
		all.removeElement(C);
	}
	
	String clanName="";
	/**
	  * String containing a short story on the Clan.
	  * Clan's will have premises set in game so people can find out
	  * about Clans via in-game functions and through the web-site.
	  */
	String clanPremise="";
	String clanRecall="";
	String clanDonationRoom="";
	String AcceptanceSettings="";
	int clanType=Clan.TYPE_CLAN;
	int ClanStatus=0;
	String PoliticString="";

	public String getDetail(MOB mob)
	{
		Clan C=this;
		StringBuffer msg=new StringBuffer("");
		msg.append(""+C.typeName()+" Profile: "+C.ID()+"\n\r"
		          +"-----------------------------------------------------------------\n\r"
		          +C.getPremise()+"\n\r"
		          +"-----------------------------------------------------------------\n\r"
		          +Util.padRight(Clans.getRoleName(Clan.POS_BOSS,true,true),16)+": "+crewList(C,Clan.POS_BOSS)+"\n\r"
		          +Util.padRight(Clans.getRoleName(Clan.POS_LEADER,true,true),16)+": "+crewList(C,Clan.POS_LEADER)+"\n\r"
		          +Util.padRight(Clans.getRoleName(Clan.POS_TREASURER,true,true),16)+": "+crewList(C,Clan.POS_TREASURER)+"\n\r"
		          +"Total Members   : "+C.getSize()+"\n\r");
		if((mob.getClanID().equalsIgnoreCase(C.ID()))
		||(Util.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS)))
		{
			msg.append("-----------------------------------------------------------------\n\r"
			          +Util.padRight(Clans.getRoleName(Clan.POS_MEMBER,true,true),16)
					  +": "+crewList(C,Clan.POS_MEMBER)+"\n\r");
			if((mob.getClanRole()==Clan.POS_BOSS)
			||(Util.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS))
			||(mob.getClanRole()==Clan.POS_LEADER))
			{
				msg.append("-----------------------------------------------------------------\n\r"
				        +Util.padRight(Clans.getRoleName(Clan.POS_APPLICANT,true,true),16)+": "+crewList(C,Clan.POS_APPLICANT)+"\n\r");
			}
		}
		return msg.toString();
	}

	private String crewList(Clan C, int posType)
	{
		StringBuffer list=new StringBuffer("");
		Vector Members=new Vector();
		MOB m;

		Members = C.getMemberList(posType);
		Members.trimToSize();
		if(Members.size()>1)
		{
			for(int j=0;j<(Members.size() - 1);j++)
			{
				list.append(Members.elementAt(j)+", ");
			}
			list.append("and "+Members.lastElement());
		}
		else
		if(Members.size()>0)
		{
			list.append((String)Members.firstElement());
		}
		return list.toString();
	}

	public String typeName()
	{
		switch(clanType)
		{
		case Clan.TYPE_CLAN:
			return "Clan";
		}
		return "Clan";
	}
	
	public Vector getRealMemberList(int PosFilter)
	{
		Vector members=getMemberList(PosFilter);
		if(members==null) return null;
		for(int i=members.size()-1;i>=0;i--)
		{
			String member=(String)members.elementAt(i);
			if(CMMap.getPlayer(member)!=null) 
				continue;
			if(ExternalPlay.DBUserSearch(null,member))
				continue;
			members.removeElement(member);
		}
		return members;
	}
	public MOB getHeadBoss()
	{
		Vector members = getRealMemberList(Clan.POS_BOSS);
		if((members==null)||(members.size()==0))
			members = getRealMemberList(Clan.POS_LEADER);
		if((members==null)||(members.size()==0))
			members = getRealMemberList(Clan.POS_TREASURER);
		if((members==null)||(members.size()==0))
			members = getRealMemberList(Clan.POS_STAFF);
		if((members==null)||(members.size()==0))
			members = getRealMemberList(Clan.POS_MEMBER);
		if((members==null)||(members.size()==0))
			members = getRealMemberList(Clan.POS_APPLICANT);
		if((members==null)||(members.size()==0))
			return null;
		return getMOB((String)members.firstElement());
	}
	
	public int getSize()
	{
		Vector members=new Vector();
		return getSize(members);
	}

	public int getSize(Vector members)
	{
		ExternalPlay.DBClanFill(this.ID(), members, new Vector(), new Vector());
		return members.size();
	}

	public String name() {return clanName;}
	public String getName() {return clanName;}
	public String ID() {return clanName;}
	public void setName(String newName) {clanName = newName; }
	public int getType() {return clanType;}

	public String getPremise() {return clanPremise;}
	public void setPremise(String newPremise)
	{
		clanPremise = newPremise;
		update();
	}

	public String getAcceptanceSettings() { return AcceptanceSettings; }
	public void setAcceptanceSettings(String newSettings) { AcceptanceSettings=newSettings; }

	public String getPolitics() { return PoliticString; }
	public void setPolitics(String politics) { PoliticString=politics; }

	public int getStatus() { return ClanStatus; }
	public void setStatus(int newStatus) { ClanStatus=newStatus; }

	public String getRecall() { return clanRecall; }
	public void setRecall(String newRecall) { clanRecall=newRecall; }

	public String getDonation() { return clanDonationRoom; }
	public void setDonation(String newDonation) { clanDonationRoom=newDonation; }

	public Vector getMemberList()
	{
		Vector members=new Vector();
		ExternalPlay.DBClanFill(this.ID(), members, new Vector(), new Vector());
		return members;
	}

	public Vector getMemberList(int PosFilter)
	{
		Vector members=new Vector();
		Vector filteredMembers=new Vector();
		Vector roles=new Vector();
		ExternalPlay.DBClanFill(this.ID(), members, roles, new Vector());
		for(int s=0;s<members.size();s++)
		{
			int posFilter=((Integer)roles.elementAt(s)).intValue();
			if(posFilter==PosFilter)
				filteredMembers.addElement((String)members.elementAt(s));
		}
		return filteredMembers;
	}

	public int getTopRank() { return Clan.POS_BOSS; }

	public void update()
	{
		Clans.updateClan(this);
	}
	/** return a new instance of the object*/
	public Clan newInstance(){return new Clans();}
	public Clan copyOf()
	{
		try
		{
			return (Clan)this.clone();
		}
		catch(CloneNotSupportedException e)
		{
			return new Clans();
		}
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID!=Host.CLAN_TICK)
			return true;
		try{
			Clan C=this;
			Vector members=new Vector();
			Vector lastDates=new Vector();
			Vector V;
			int activeMembers=0;
			ExternalPlay.DBClanFill(C.getName(),members,new Vector(),lastDates);
			for(int j=0;j<members.size();j++)
			{
				if(System.currentTimeMillis()-(((Long)lastDates.elementAt(j)).longValue())<(CommonStrings.getIntVar(CommonStrings.SYSTEMI_DAYSCLANDEATH)*Host.TICKS_PER_DAY*Host.TICK_TIME))
					activeMembers++;
			}
			if(activeMembers<CommonStrings.getIntVar(CommonStrings.SYSTEMI_MINCLANMEMBERS))
			{
				if(C.getStatus()==Clan.CLANSTATUS_FADING)
					destroyClan(C);
				else
				{
					C.setStatus(Clan.CLANSTATUS_FADING);
					clanAnnounce("Clan "+name()+" is in danger of being deleted if more members do not log on within 24 hours.");
				}
			}
			else
			switch(C.getStatus())
			{
			case Clan.CLANSTATUS_FADING:
				C.setStatus(Clan.CLANSTATUS_ACTIVE);
				clanAnnounce("Clan "+name()+" is no longer in danger of being deleted.  Be aware that there is required activity level.");
				break;
			case Clan.CLANSTATUS_PENDING:
				C.setStatus(Clan.CLANSTATUS_ACTIVE);
				clanAnnounce("Clan "+name()+" now has sufficient members.  The Clan is now fully approved.");
				break;
			default:
				return true;
			}
		}
		catch(Exception e)
		{
			Log.errOut("ClanCommands",e);
		}
		return true;
	}
	
	public void clanAnnounce(String msg)
	{
		MOB boss=getHeadBoss();
		if(boss!=null)
			ExternalPlay.channel(boss,"CLANTALK",msg,true);
		else
			ExternalPlay.channel("CLANTALK",msg,true);
	}
}