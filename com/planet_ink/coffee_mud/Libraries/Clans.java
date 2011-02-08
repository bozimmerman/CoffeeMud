package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.core.threads.ServiceEngine;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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
 * <p>Portions Copyright (c) 2004-2011 Bo Zimmerman</p>
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
	public SHashtable<String,Clan> all=new SHashtable<String,Clan>();

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

    public boolean isCommonClanRelations(String clanName1, String clanName2, int relation)
    {
        if((clanName1.length()==0)||(clanName2.length()==0)) return relation==Clan.REL_NEUTRAL;
        Clan C1=getClan(clanName1);
        Clan C2=getClan(clanName2);
        if((C1==null)||(C2==null)) return relation==Clan.REL_NEUTRAL;
        int i1=C1.getClanRelations(clanName2);
        int i2=C2.getClanRelations(clanName1);
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
    
	public int getClanRelations(String clanName1, String clanName2)
	{
		if((clanName1.length()==0)||(clanName2.length()==0)) return Clan.REL_NEUTRAL;
		Clan C1=getClan(clanName1);
		Clan C2=getClan(clanName2);
		if((C1==null)||(C2==null)) return Clan.REL_NEUTRAL;
		int i1=C1.getClanRelations(clanName2);
		int i2=C2.getClanRelations(clanName1);
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

	public Clan getNewClanObjectOfType(int type)
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
        for(Enumeration<MOB.Tattoo> e=M.tattoos();e.hasMoreElements();)
        {
            MOB.Tattoo T=e.nextElement();
            if(T.tattooName.startsWith("PARENT:"))
            {
                String name=T.tattooName.substring("PARENT:".length());
                MOB M2=CMLib.players().getLoadPlayer(name.toLowerCase());
                if((M2 != null)&&isFamilyOfMembership(M2,members))
                    return true;
            }
        }
        return false;
    }
    
	public Enumeration<Clan> clans()
	{
		return all.elements();
	}
	public int numClans()
	{
		return all.size();
	}
	public void addClan(Clan C)
	{
		if(!CMSecurity.isDisabled("CLANTICKS"))
			CMLib.threads().startTickDown(C,Tickable.TICKID_CLAN,(int)CMProps.getTicksPerDay());
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
		List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.CLANINFO);
        for(int i=0;i<channels.size();i++)
            CMLib.commands().postChannel((String)channels.get(i),"ALL",msg,true);
	}

    public Enumeration<String> clansNames(){return all.keys();}
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
            Vector<String> V=CMParms.parse(prizeStr);
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
        Clan.ClanPositionPower allowed=C.getAuthority(mob.getClanRole(),function);
        if(allowed==Clan.ClanPositionPower.CAN_DO) return true;
        if(allowed==Clan.ClanPositionPower.CAN_NOT_DO) return false;
        if(function==Clan.FUNC_CLANASSIGN)
        {
            if(C.getAuthority(mob.getClanRole(),Clan.FUNC_CLANVOTEASSIGN)!=Clan.ClanPositionPower.CAN_DO)
               return false;
        }
        else
        if(C.getAuthority(mob.getClanRole(),Clan.FUNC_CLANVOTEOTHER)!=Clan.ClanPositionPower.CAN_DO)
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
                final int voteFunctionType = (function == Clan.FUNC_CLANASSIGN) ? Clan.FUNC_CLANVOTEASSIGN : Clan.FUNC_CLANVOTEOTHER;
                final List<Integer> votingRoles = new LinkedList<Integer>();
                for(int i=0;i<C.getRolesList().length;i++)
                	if(C.getAuthority(i, voteFunctionType)==Clan.ClanPositionPower.CAN_DO)
                		votingRoles.add(Integer.valueOf(i));
                if(votingRoles.size()>0)
                {
                	final String firstRoleName = C.getRoleName(votingRoles.iterator().next().intValue(), true, true);
                	final String rest = " "+firstRoleName+" should use CLANVOTE to participate.";
	                if(votingRoles.size() >= (C.getRolesList().length-2))
	                {
	                	if(function == Clan.FUNC_CLANASSIGN)
	                        clanAnnounce(mob,"The "+C.typeName()+" "+C.clanID()+" has a new election to vote upon. "+rest);
	                	else
	                        clanAnnounce(mob,"The "+C.typeName()+" "+C.clanID()+" has a new matter to vote upon. "+rest);
	                }
	                else
	                if(votingRoles.size()==1)
                        clanAnnounce(mob,"The "+C.typeName()+" "+C.clanID()+" has a new matter to vote upon. "+rest);
	                else
	                {
	                	final String[] roleNames = new String[votingRoles.size()];
	                	for(int i=0;i<votingRoles.size();i++)
	                	{
	                		Integer roleID=votingRoles.get(i);
	                		roleNames[i]=C.getRoleName(roleID.intValue(), true, true);
	                	}
	                	String list = CMLib.english().toEnglishStringList(roleNames);
                        clanAnnounce(mob,"The "+C.typeName()+" "+C.clanID()+" has a new matter to vote upon. "
                        		+list+" should use CLANVOTE to participate.");
	                }
                }
                mob.tell("Your vote has started.  Use CLANVOTE to cast your vote.");
                return false;
            }
        }
        catch(java.io.IOException e){}
        mob.tell("Without a vote, this command can not be executed.");
        return false;
    }

    public Clan.ClanGovernment parseGovernmentXML(StringBuffer xml)
    {
    	List<XMLLibrary.XMLpiece> xmlV = CMLib.xml().parseAllXML(xml);
    	XMLLibrary.XMLpiece clanTypesTag = CMLib.xml().getPieceFromPieces(xmlV, "CLANTYPES");
    	List<XMLLibrary.XMLpiece> clanTypes = null;
    	if(clanTypesTag != null)
    		clanTypes = clanTypesTag.contents;
    	else
    	{
        	XMLLibrary.XMLpiece clanType = CMLib.xml().getPieceFromPieces(xmlV, "CLANTYPE");
        	if(clanType != null)
        	{
        		clanTypes = new LinkedList<XMLLibrary.XMLpiece>();
        		clanTypes.add(clanType);
        	}
        	else
        	{
	    		Log.errOut("Clans","No CLANTYPES found in xml"); 
	        	return null;
        	}
    	}
    	
    	return null;
    }

    public void clanAnnounce(MOB mob, String msg)
    {
    	List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.CLANINFO);
        for(int i=0;i<channels.size();i++)
            CMLib.commands().postChannel(mob,(String)channels.get(i),msg,true);
    }
}
