package com.planet_ink.coffee_mud.Clans;
import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

/**
  * @author=Jeremy Vyska
  */
public class StdClan implements Clan
{

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

	public int getAlign()
	{
		int AvgAlign = 0;
		Vector members=new Vector();
		ExternalPlay.DBClanFill(this.ID(), members, new Vector());
		if(members.size()<1) return 500;
		for(int s=0;s<members.size();s++)
		{
			MOB mob=CMClass.getMOB("StdMOB");
			mob.setName((String)members.elementAt(s));
			ExternalPlay.DBReadUserOnly(mob);
			AvgAlign+=mob.getAlignment();
		}
		if(AvgAlign>0)
		{
			AvgAlign = AvgAlign / getSize();
			return AvgAlign;
		}
		else
		{
			return AvgAlign;
		}
	}

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
		          +"Total Members   : "+C.getSize()+"\n\r"
		          +"Clan Alignment  : "+CommonStrings.alignmentStr(C.getAlign())+"\n\r");
		if(mob.getClanID().equalsIgnoreCase(C.ID()))
		{
			msg.append("-----------------------------------------------------------------\n\r"
			          +Util.padRight(Clans.getRoleName(Clan.POS_MEMBER,true,true),16)+": "+crewList(C,Clan.POS_MEMBER)+"\n\r");
			if((mob.getClanRole()==Clan.POS_BOSS)||(mob.getClanRole()==Clan.POS_LEADER))
			{
				msg.append("-----------------------------------------------------------------\n\r"
				        +Util.padRight(Clans.getRoleName(Clan.POS_APPLICANT,true,true),16)+": "+crewList(C,Clan.POS_APPLICANT)+"\n\r");
			}
		}

		if(Util.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS))
		{
			msg.append("-----------------------------------------------------------------\n\r"
			          +Util.padRight(Clans.getRoleName(Clan.POS_MEMBER,true,true),16)+": "+crewList(C,Clan.POS_MEMBER)+"\n\r");
			msg.append("-----------------------------------------------------------------\n\r"
			          +Util.padRight(Clans.getRoleName(Clan.POS_APPLICANT,true,true),16)+": "+crewList(C,Clan.POS_APPLICANT)+"\n\r");
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


	public int getSize()
	{
		Vector members=new Vector();
		return getSize(members);
	}

	public int getSize(Vector members)
	{
		ExternalPlay.DBClanFill(this.ID(), members, new Vector());
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
		ExternalPlay.DBClanFill(this.ID(), members, new Vector());
		return members;
	}

	public Vector getMemberList(int PosFilter)
	{
		Vector members=new Vector();
		Vector filteredMembers=new Vector();
		ExternalPlay.DBClanFill(this.ID(), members, new Vector());
		for(int s=0;s<members.size();s++)
		{
			MOB mob=CMClass.getMOB("StdMOB");
			mob.setName((String)members.elementAt(s));
			ExternalPlay.DBReadUserOnly(mob);
			if(mob.getClanRole()==PosFilter) filteredMembers.addElement(mob.Name());
		}
		return filteredMembers;
	}

	public int getTopRank() { return Clan.POS_BOSS; }

	public void update()
	{
		Clans.updateClan(this);
	}
	/** return a new instance of the object*/
	public Clan newInstance(){return new StdClan();}
	public Clan copyOf()
	{
		try
		{
			return (Clan)this.clone();
		}
		catch(CloneNotSupportedException e)
		{
			return new StdClan();
		}
	}
}