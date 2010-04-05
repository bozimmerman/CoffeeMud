package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.core.threads.ServiceEngine;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.DefaultClan;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.MemberRecord;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;
/**
 * <p>Portions Copyright (c) 2003 Jeremy Vyska</p>
 * <p>Portions Copyright (c) 2004-2010 Bo Zimmerman</p>
 * <p>Licensed under the Apache License, Version 2.0 (the "License");
 * <p>you may not use this file except in compliance with the License.
 * <p>You may obtain a copy of the License at
 *
 * <p>       http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software
 * <p>distributed under the License is distributed on an "AS IS" BASIS,
 * <p>WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * <p>See the License for the specific language governing permissions and
 * <p>limitations under the License.
 */
@SuppressWarnings("unchecked")
public class Clans extends StdLibrary implements ClanManager
{
	public Hashtable all=new Hashtable();

    public String ID(){return "Clans";}
	public boolean shutdown()
	{
		for(Enumeration e=all.elements();e.hasMoreElements();)
		{
			Clan C=(Clan)e.nextElement();
			CMLib.threads().deleteTick(C,Tickable.TICKID_CLAN);
		}
		all.clear();
        return true;
	}

    public boolean isCommonClanRelations(String id1, String id2, int relation)
    {
        if((id1.length()==0)||(id2.length()==0)) return relation==Clan.REL_NEUTRAL;
        Clan C1=getClan(id1);
        Clan C2=getClan(id2);
        if((C1==null)||(C2==null)) return relation==Clan.REL_NEUTRAL;
        int i1=C1.getClanRelations(id2);
        int i2=C2.getClanRelations(id1);
        if((i1==i2)
        &&((i1==Clan.REL_WAR)
           ||(i1==Clan.REL_ALLY)))
           return i1==relation;
        for(Enumeration e=clans();e.hasMoreElements();)
        {
            Clan C=(Clan)e.nextElement();
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
        if(i1==i2) return relation==i1;
        
        if(Clan.REL_NEUTRALITYGAUGE[i1]<Clan.REL_NEUTRALITYGAUGE[i2]) return relation==i1;
        return relation==i2;
    }
    
	public int getClanRelations(String id1, String id2)
	{
		if((id1.length()==0)||(id2.length()==0)) return Clan.REL_NEUTRAL;
		Clan C1=getClan(id1);
		Clan C2=getClan(id2);
		if((C1==null)||(C2==null)) return Clan.REL_NEUTRAL;
		int i1=C1.getClanRelations(id2);
		int i2=C2.getClanRelations(id1);
		int rel=Clan.RELATIONSHIP_VECTOR[i1][i2];
		if(rel==Clan.REL_WAR) return Clan.REL_WAR;
		if(rel==Clan.REL_ALLY) return Clan.REL_ALLY;
		for(Enumeration e=clans();e.hasMoreElements();)
		{
			Clan C=(Clan)e.nextElement();
			if((C!=C1)
			&&(C!=C2)
			&&(((C1.getClanRelations(C.clanID())==Clan.REL_ALLY)&&(C.getClanRelations(C2.clanID())==Clan.REL_WAR)))
				||((C2.getClanRelations(C.clanID())==Clan.REL_ALLY)&&(C.getClanRelations(C1.clanID())==Clan.REL_WAR)))
					return Clan.REL_WAR;
		}
		return rel;
	}

	public Clan getClan(String id)
	{
		if(id.length()==0) return null;
		Clan C=(Clan)all.get(id.toUpperCase());
        if(C!=null) return C;
        for(Enumeration e=all.elements();e.hasMoreElements();)
        {
            C=(Clan)e.nextElement();
            if(CMLib.english().containsString(CMStrings.removeColors(C.name()),id))
                return C;
        }
        return null;
	}
    public Clan findClan(String id)
    {
        Clan C=getClan(id);
        if(C!=null) return C;
        for(Enumeration e=all.elements();e.hasMoreElements();)
        {
            C=(Clan)e.nextElement();
            if(CMLib.english().containsString(CMStrings.removeColors(C.name()),id))
                return C;
        }
        return null;
    }

	public Clan getClanType(int type)
	{
		switch(type)
		{
		case Clan.TYPE_CLAN:
			return (Clan)CMClass.getCommon("DefaultClan");
		default:
			return (Clan)CMClass.getCommon("DefaultClan");
		}
	}

    public boolean isFamilyOfMembership(MOB M, List<MemberRecord> members) {
        if(M == null)
            return false;
        if(members.contains(M.Name()))
            return true;
        if((M.getLiegeID().length()>0)
        &&(M.isMarriedToLiege())
        &&(members.contains(M.getLiegeID())))
            return true;
        for(int t = 0;t < M.numTattoos();t++) 
        {
            String tattoo = M.fetchTattoo(t);
            if(tattoo.startsWith("PARENT:"))
            {
                String name=tattoo.substring("PARENT:".length());
                MOB M2=CMLib.players().getLoadPlayer(name.toLowerCase());
                if((M2 != null)&&isFamilyOfMembership(M2,members))
                    return true;
            }
        }
        return false;
    }
    
	public int getRoleOrder(int role)
	{
		for(int i=0;i<Clan.POSORDER.length;i++)
			if(Clan.POSORDER[i]==role) return i;
		return 1;
	}


	public String getRoleName(int government, int role, boolean titleCase, boolean plural)
	{
		StringBuffer roleName=new StringBuffer();
		if((government<0)||(government>=Clan.ROL_DESCS.length))
			government=0;
		String[] roles=Clan.ROL_DESCS[government];
		roleName.append(roles[getRoleOrder(role)].toLowerCase());
		if(titleCase)
		{
			String titled=CMStrings.capitalizeAndLower(roleName.toString());
			roleName.setLength(0);
			roleName.append(titled);
		}
		if(plural)
		{
			if(roleName.toString().equalsIgnoreCase("Staff"))
			{
				// do nothing
			}
			else
			if(new Character(roleName.charAt(roleName.length()-1)).equals(new Character(("y").charAt(0))))
			{
				roleName.setCharAt(roleName.length()-1,'i');
				roleName.append("es");
			}
			else
			if(new Character(roleName.charAt(roleName.length()-1)).equals(new Character(("s").charAt(0))))
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

	public Enumeration clans()
	{
		return all.elements();
	}
	public int size()
	{
		return all.size();
	}
	public void addClan(Clan C)
	{
		if(!CMSecurity.isDisabled("CLANTICKS"))
			CMLib.threads().startTickDown(C,Tickable.TICKID_CLAN,CMProps.getIntVar(CMProps.SYSTEMI_TICKSPERMUDDAY));
		all.put(C.clanID().toUpperCase(),C);
		CMLib.map().sendGlobalMessage(CMLib.map().deity(), CMMsg.TYP_CLANEVENT, 
				CMClass.getMsg(CMLib.map().deity(), CMMsg.MSG_CLANEVENT, "+"+C.name()));
	}
	public void removeClan(Clan C)
	{
		CMLib.threads().deleteTick(C,Tickable.TICKID_CLAN);
		all.remove(C.clanID().toUpperCase());
		CMLib.map().sendGlobalMessage(CMLib.map().deity(), CMMsg.TYP_CLANEVENT, 
				CMClass.getMsg(CMLib.map().deity(), CMMsg.MSG_CLANEVENT, "-"+C.name()));
	}

	public void tickAllClans()
	{
		for(Enumeration e=clans();e.hasMoreElements();)
		{
			Clan C=(Clan)e.nextElement();
			C.tick(C,Tickable.TICKID_CLAN);
		}
	}
	
	public void clanAnnounceAll(String msg)
	{
        Vector channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.CLANINFO);
        for(int i=0;i<channels.size();i++)
            CMLib.commands().postChannel((String)channels.elementAt(i),"ALL",msg,true);
	}

    public int numClans(){return all.size();}
    public Enumeration allClans(){return ((Hashtable)(all.clone())).elements();}
	public String translatePrize(int trophy)
	{
	    String prizeStr="";
	    switch(trophy)
	    {
	    	case Clan.TROPHY_AREA: prizeStr=CMProps.getVar(CMProps.SYSTEM_CLANTROPAREA); break;
	    	case Clan.TROPHY_CONTROL: prizeStr=CMProps.getVar(CMProps.SYSTEM_CLANTROPCP); break;
	    	case Clan.TROPHY_EXP: prizeStr=CMProps.getVar(CMProps.SYSTEM_CLANTROPEXP); break;
	    	case Clan.TROPHY_PK: prizeStr=CMProps.getVar(CMProps.SYSTEM_CLANTROPPK); break;
	    }
	    if(prizeStr.length()==0) return "None";
        if(prizeStr.length()>0)
        {
            Vector V=CMParms.parse(prizeStr);
            if(V.size()>=2)
            {
                String type=((String)V.lastElement()).toUpperCase();
                String amt=(String)V.firstElement();
                if("EXPERIENCE".startsWith(type))
                    return amt+" experience point bonus.";
            }
        }
	    return prizeStr;
	}
	public boolean trophySystemActive()
	{
	    return (CMProps.getVar(CMProps.SYSTEM_CLANTROPAREA).length()>0)
	        || (CMProps.getVar(CMProps.SYSTEM_CLANTROPCP).length()>0)
	        || (CMProps.getVar(CMProps.SYSTEM_CLANTROPEXP).length()>0)
	        || (CMProps.getVar(CMProps.SYSTEM_CLANTROPPK).length()>0);
	    
	}
    
    public boolean goForward(MOB mob, Clan C, Vector commands, int function, boolean voteIfNecessary)
    {
        if((mob==null)||(C==null)) return false;
        int allowed=C.allowedToDoThis(mob,function);
        if(allowed==1) return true;
        if(allowed==-1) return false;
        if(function==Clan.FUNC_CLANASSIGN)
        {
            if(C.allowedToDoThis(mob,Clan.FUNC_CLANVOTEASSIGN)<=0)
               return false;
        }
        else
        if(C.allowedToDoThis(mob,Clan.FUNC_CLANVOTEOTHER)<=0)
           return false;
        if(!voteIfNecessary) return true;
        String matter=CMParms.combine(commands,0);
        for(Enumeration e=C.votes();e.hasMoreElements();)
        {
            Clan.ClanVote CV=(Clan.ClanVote)e.nextElement();
            if((CV.voteStarter.equalsIgnoreCase(mob.Name()))
            &&(CV.voteStatus==Clan.VSTAT_STARTED))
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
                Clan.ClanVote CV=new Clan.ClanVote();
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
                    clanAnnounce(mob,"The "+C.typeName()+" "+C.clanID()+" has a new matter to vote on. Members should use CLANVOTE to participate.");
                    break;
                case Clan.GVT_DICTATORSHIP:
                case Clan.GVT_THEOCRACY:
                case Clan.GVT_FAMILY:
                    clanAnnounce(mob,"The "+C.typeName()+" "+C.clanID()+" has a vote -- lord only knows how.");
                    break;
                case Clan.GVT_OLIGARCHY:
                    clanAnnounce(mob,"The guildmasters of the "+C.typeName()+" "+C.clanID()+" have a new matter to vote upon. They should use CLANVOTE to participate.");
                    break;
                case Clan.GVT_REPUBLIC:
                    if(function==Clan.FUNC_CLANASSIGN)
                        clanAnnounce(mob,"The "+C.typeName()+" "+C.clanID()+" has a new election to vote upon. Citizens should use CLANVOTE to participate.");
                    else
                        clanAnnounce(mob,"The senators of "+C.typeName()+" "+C.clanID()+" have a new matter to vote upon. They should use CLANVOTE to participate.");
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


    public void clanAnnounce(MOB mob, String msg)
    {
        Vector channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.CLANINFO);
        for(int i=0;i<channels.size();i++)
            CMLib.commands().postChannel(mob,(String)channels.elementAt(i),msg,true);
    }

    public int getIntFromRole(int roleType)
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
    
    public int getRoleFromName(int government, String position)
    {
        if((government<0)||(government>=Clan.GVT_DESCS.length))
            government=0;
        String[] roles=Clan.ROL_DESCS[government];
        for(int i=0;i<roles.length;i++)
            if(roles[i].startsWith(position.toUpperCase()))
                return (int)CMath.pow(2,i-1);
        return -1;
    }
}