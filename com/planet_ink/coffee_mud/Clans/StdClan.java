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
			if(mob.getClanRole()==PosFilter) filteredMembers.addElement(mob.ID());
		}
		return filteredMembers;
	}

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