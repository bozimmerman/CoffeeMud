package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class CMChannels extends StdLibrary implements ChannelsLibrary
{
    public String ID(){return "CMChannels";}
	public final int QUEUE_SIZE=100;
	
	public int numChannelsLoaded=0;
	public int numIChannelsLoaded=0;
	public int numImc2ChannelsLoaded=0;
	public Vector channelNames=new Vector();
	public Vector channelMasks=new Vector();
    public Vector<HashSet<ChannelFlag>> channelFlags=new Vector<HashSet<ChannelFlag>>();
	public Vector ichannelList=new Vector();
	public Vector imc2channelList=new Vector();
	public Vector channelQue=new Vector();
	//public final Vector emptyVector=new Vector(1);
	public final HashSet<ChannelFlag> emptyFlags=new HashSet<ChannelFlag>(1);
	
	public int getNumChannels()
	{
		return channelNames.size();
	}
	
	public String getChannelMask(int i)
	{
		if((i>=0)&&(i<channelMasks.size()))
			return (String)channelMasks.elementAt(i);
		return "";
	}

    
    public HashSet<ChannelFlag> getChannelFlags(int i)
    {
        if((i>=0)&&(i<channelFlags.size()))
            return (HashSet<ChannelFlag>)channelFlags.elementAt(i);
        return emptyFlags;
    }

	public String getChannelName(int i)
	{
		if((i>=0)&&(i<channelNames.size()))
			return (String)channelNames.elementAt(i);
		return "";
	}

	public Vector getChannelQue(int i)
	{
		if((i>=0)&&(i<channelQue.size()))
			return (Vector)channelQue.elementAt(i);
		return new Vector();
	}
	
    public boolean mayReadThisChannel(MOB sender, boolean areaReq, MOB M, int i)
    { return mayReadThisChannel(sender,areaReq,M,i,false);}
	public boolean mayReadThisChannel(MOB sender,
									  boolean areaReq,
									  MOB M, 
									  int i,
                                      boolean offlineOK)
	{
		if((sender==null)||(M==null)||(M.playerStats()==null)) return false;
		Room R=M.location();
        if(((!offlineOK))
        &&((M.amDead())||(R==null)))
		    return false;
		if(getChannelFlags(i).contains(ChannelFlag.CLANONLY)||getChannelFlags(i).contains(ChannelFlag.CLANALLYONLY))
        {
            // only way to fail an all-clan send is to have NO clan.
            if((M.getClanID().length()==0)||(M.getClanRole()==Clan.POS_APPLICANT))
                return false;
            if((!sender.getClanID().equalsIgnoreCase("ALL"))
            &&(!M.getClanID().equalsIgnoreCase(sender.getClanID()))
            &&((!getChannelFlags(i).contains(ChannelFlag.CLANALLYONLY))
        		||(CMLib.clans().getClanRelations(M.getClanID(),sender.getClanID())!=Clan.REL_ALLY)))
            	return false;
        }
		
		if((!M.playerStats().getIgnored().contains(sender.Name()))
		&&(CMLib.masking().maskCheck(getChannelMask(i),M,true))
		&&((!areaReq)
		   ||(sender.location()==null)
		   ||(R.getArea()==sender.location().getArea()))
		&&(!CMath.isSet(M.playerStats().getChannelMask(),i)))
			return true;
		return false;
	}
	
	public boolean mayReadThisChannel(MOB sender,
									  boolean areaReq,
									  Session ses, 
									  int i)
	{
		if(ses==null) 
		    return false;
		MOB M=ses.mob();
		
		if((sender==null)
		||(M==null)
		||(M.amDead())
		||(M.location()==null)
		||(M.playerStats()==null))
		    return false;
		String senderName=sender.Name();
		int x=senderName.indexOf("@");
		if(x>0) senderName=senderName.substring(0,x);
		
		if(getChannelFlags(i).contains(ChannelFlag.CLANONLY)||getChannelFlags(i).contains(ChannelFlag.CLANALLYONLY))
        {
            // only way to fail an all-clan send is to have NO clan.
            if((M.getClanID().length()==0)||(M.getClanRole()==Clan.POS_APPLICANT))
                return false;
            if((!sender.getClanID().equalsIgnoreCase("ALL"))
            &&(!M.getClanID().equalsIgnoreCase(sender.getClanID()))
            &&((!getChannelFlags(i).contains(ChannelFlag.CLANALLYONLY))
        		||(CMLib.clans().getClanRelations(M.getClanID(),sender.getClanID())!=Clan.REL_ALLY)))
            	return false;
        }
		
		Room R=M.location();
		if((!ses.killFlag())
		&&(R!=null)
		&&(!M.playerStats().getIgnored().contains(senderName))
		&&(CMLib.masking().maskCheck(getChannelMask(i),M,true))
		&&((!areaReq)
		   ||(sender.location()==null)
		   ||(R.getArea()==sender.location().getArea()))
		&&(!CMath.isSet(M.playerStats().getChannelMask(),i)))
			return true;
		return false;
	}
	
	public boolean mayReadThisChannel(MOB M, int i, boolean zapCheckOnly)
	{
	    if(M==null) return false;
	    
	    if(i>=getNumChannels())
	        return false;
	    
		if((getChannelFlags(i).contains(ChannelFlag.CLANONLY)||getChannelFlags(i).contains(ChannelFlag.CLANALLYONLY))
		&&((M.getClanID().length()==0)||(M.getClanRole()==Clan.POS_APPLICANT)))
		    return false;

		if(((zapCheckOnly)||((!M.amDead())&&(M.location()!=null)))
		&&(CMLib.masking().maskCheck(getChannelMask(i),M,true))
		&&(!CMath.isSet(M.playerStats().getChannelMask(),i)))
			return true;
		return false;
	}

	public void channelQueUp(int i, CMMsg msg)
	{
        CMLib.map().sendGlobalMessage(msg.source(),CMMsg.TYP_CHANNEL,msg);
		Vector q=getChannelQue(i);
		synchronized(q)
		{
			if(q.size()>=QUEUE_SIZE)
				q.removeElementAt(0);
			q.addElement(msg);
		}
	}
	
	public int getChannelIndex(String channelName)
	{
        channelName=channelName.toUpperCase();
		for(int c=0;c<channelNames.size();c++)
			if(((String)channelNames.elementAt(c)).startsWith(channelName))
				return c;
		return -1;
	}

	public int getChannelCodeNumber(String channelName)
	{
        channelName=channelName.toUpperCase();
		for(int c=0;c<channelNames.size();c++)
			if(((String)channelNames.elementAt(c)).startsWith(channelName))
				return 1<<c;
		return -1;
	}

	public String getChannelName(String channelName)
	{
        channelName=channelName.toUpperCase();
		for(int c=0;c<channelNames.size();c++)
			if(((String)channelNames.elementAt(c)).startsWith(channelName))
				return ((String)channelNames.elementAt(c)).toUpperCase();
		return "";
	}

	public Vector getFlaggedChannelNames(ChannelFlag flag)
	{
        Vector channels=new Vector();
		for(int c=0;c<channelNames.size();c++)
			if(((HashSet<ChannelFlag>)channelFlags.elementAt(c)).contains(flag))
                channels.addElement(((String)channelNames.elementAt(c)).toUpperCase());
		return channels;
	}
	
	public String getExtraChannelDesc(String channelName)
	{
		StringBuilder str=new StringBuilder("");
		int dex = getChannelIndex(channelName);
		if(dex >= 0)
		{
			HashSet<ChannelFlag> flags = getChannelFlags(dex);
			String mask = getChannelMask(dex);
			if(flags.contains(ChannelFlag.CLANALLYONLY))
				str.append(" This is a channel for clans and their allies.");
			if(flags.contains(ChannelFlag.CLANONLY))
				str.append(" Only members of the same clan can see messages on this channel.");
			if(flags.contains(ChannelFlag.PLAYERREADONLY)||flags.contains(ChannelFlag.READONLY))
				str.append(" This channel is read-only.");
			if(flags.contains(ChannelFlag.SAMEAREA))
				str.append(" Only people in the same area can see messages on this channel.");
			if((mask!=null)&&(mask.trim().length()>0))
				str.append(" The following may read this channel : "+CMLib.masking().maskDesc(mask));
		}
		return str.toString();
	}

    private void clearChannels()
    {
        numChannelsLoaded=0;
        numIChannelsLoaded=0;
        numImc2ChannelsLoaded=0;
        channelNames=new Vector();
        channelMasks=new Vector();
        channelFlags=new Vector<HashSet<ChannelFlag>>();
        ichannelList=new Vector();
        imc2channelList=new Vector();
        channelQue=new Vector();
    }
    
    public boolean shutdown()
	{
        clearChannels();
        return true;
	}

	public String[][] imc2ChannelsArray()
	{
		String[][] array=new String[numImc2ChannelsLoaded][4];
		int num=0;
		for(int i=0;i<channelNames.size();i++)
		{
			String name=(String)channelNames.elementAt(i);
			String mask=(String)channelMasks.elementAt(i);
			HashSet<ChannelFlag> flags=channelFlags.elementAt(i);
			String iname=(String)imc2channelList.elementAt(i);
			if((iname!=null)&&(iname.trim().length()>0))
			{
				array[num][0]=iname.trim();
				array[num][1]=name.trim();
				array[num][2]=mask;
                array[num][3]=CMParms.combine(flags,0);
				num++;
			}
		}
		return array;
	}
	public String[][] iChannelsArray()
	{
		String[][] array=new String[numIChannelsLoaded][4];
		int num=0;
		for(int i=0;i<channelNames.size();i++)
		{
			String name=(String)channelNames.elementAt(i);
			String mask=(String)channelMasks.elementAt(i);
			String iname=(String)ichannelList.elementAt(i);
			HashSet<ChannelFlag> flags=channelFlags.elementAt(i);
			if((iname!=null)&&(iname.trim().length()>0))
			{
				array[num][0]=iname.trim();
				array[num][1]=name.trim();
				array[num][2]=mask;
                array[num][3]=CMParms.combine(flags,0);
				num++;
			}
		}
		return array;
	}
	public String[] getChannelNames()
	{
		if(channelNames.size()==0) return null;
		return CMParms.toStringArray(channelNames);
	}
	
	public Vector clearInvalidSnoopers(Session mySession, int channelCode)
	{
	    Vector invalid=null;
	    if(mySession!=null)
	    {
		    Session S=null;
		    for(int s=0;s<CMLib.sessions().size();s++)
		    {
		        S=CMLib.sessions().elementAt(s);
		        if((S!=mySession)
		        &&(S.mob()!=null)
		        &&(mySession.amBeingSnoopedBy(S))
		        &&(!mayReadThisChannel(S.mob(),channelCode,false)))
		        {
		            if(invalid==null) invalid=new Vector();
		            invalid.add(S);
		            mySession.stopBeingSnoopedBy(S);
		        }
		    }
	    }
	    return invalid;	    
	}
	
	public void restoreInvalidSnoopers(Session mySession, Vector invalid)
	{
	    if((mySession==null)||(invalid==null)) return;
		for(int s=0;s<invalid.size();s++)
		    mySession.startBeingSnoopedBy((Session)invalid.elementAt(s));
	}

    public String parseOutFlags(String mask, HashSet<ChannelFlag> flags)
    {
        Vector V=CMParms.parse(mask);
        for(int v=V.size()-1;v>=0;v--)
        {
            String s=((String)V.elementAt(v)).toUpperCase();
            if(CMParms.contains(CMParms.toStringArray(ChannelFlag.values()), s))
            {
                V.removeElementAt(v);
                flags.add(ChannelFlag.valueOf(s));
            }
        }
        return CMParms.combine(V,0);
    }
    
	public int loadChannels(String list, String ilist, String imc2list)
	{
        clearChannels();
		while(list.length()>0)
		{
			int x=list.indexOf(",");

			String item=null;
			if(x<0)
			{
				item=list.trim();
				list="";
			}
			else
			{
				item=list.substring(0,x).trim();
				list=list.substring(x+1);
			}
			numChannelsLoaded++;
			x=item.indexOf(" ");
			HashSet<ChannelFlag> flags=new HashSet<ChannelFlag>();
			if(x>0)
			{
				channelMasks.addElement(parseOutFlags(item.substring(x+1).trim(),flags));
				item=item.substring(0,x);
			}
			else
				channelMasks.addElement("");
			ichannelList.addElement("");
			imc2channelList.addElement("");
			channelNames.addElement(item.toUpperCase().trim());
            channelFlags.addElement(flags);
			channelQue.addElement(new Vector());
		}
		while(ilist.length()>0)
		{
			int x=ilist.indexOf(",");

			String item=null;
			if(x<0)
			{
				item=ilist.trim();
				ilist="";
			}
			else
			{
				item=ilist.substring(0,x).trim();
				ilist=ilist.substring(x+1);
			}
			int y1=item.indexOf(" ");
			int y2=item.lastIndexOf(" ");
			if((y1<0)||(y2<=y1)) continue;
			numChannelsLoaded++;
			numIChannelsLoaded++;
			String lvl=item.substring(y1+1,y2).trim();
			String ichan=item.substring(y2+1).trim();
			item=item.substring(0,y1);
			channelNames.addElement(item.toUpperCase().trim());
			channelQue.addElement(new Vector());
			HashSet<ChannelFlag> flags=new HashSet<ChannelFlag>();
			channelMasks.addElement(parseOutFlags(lvl,flags));
            channelFlags.addElement(flags);
			imc2channelList.addElement("");
			ichannelList.addElement(ichan);
		}
		while(imc2list.length()>0)
		{
			int x=imc2list.indexOf(",");

			String item=null;
			if(x<0)
			{
				item=imc2list.trim();
				imc2list="";
			}
			else
			{
				item=imc2list.substring(0,x).trim();
				imc2list=imc2list.substring(x+1);
			}
			int y1=item.indexOf(" ");
			int y2=item.lastIndexOf(" ");
			if((y1<0)||(y2<=y1)) continue;
			numChannelsLoaded++;
			numImc2ChannelsLoaded++;
			String lvl=item.substring(y1+1,y2).trim();
			String ichan=item.substring(y2+1).trim();
			item=item.substring(0,y1);
			channelNames.addElement(item.toUpperCase().trim());
			channelQue.addElement(new Vector());
			HashSet<ChannelFlag> flags=new HashSet<ChannelFlag>();
            channelMasks.addElement(parseOutFlags(lvl,flags));
            channelFlags.addElement(flags);
			imc2channelList.addElement(ichan);
			ichannelList.addElement("");
		}

		channelNames.addElement("AUCTION");
		channelQue.addElement(new Vector());
		channelMasks.addElement("");
        channelFlags.addElement(new HashSet<ChannelFlag>());
		ichannelList.addElement("");
		imc2channelList.addElement("");
		numChannelsLoaded++;

		numChannelsLoaded++;
		return numChannelsLoaded;
	}
    
    public boolean channelTo(Session ses, boolean areareq, int channelInt, CMMsg msg, MOB sender)
    {
        MOB M=ses.mob();
        boolean didIt=false;
        if(mayReadThisChannel(sender,areareq,ses,channelInt)
        &&(M.location()!=null)
        &&(M.location().okMessage(ses.mob(),msg)))
        {
            M.executeMsg(M,msg);
            didIt=true;
            if(msg.trailerMsgs()!=null)
            {
                for(int i=0;i<msg.trailerMsgs().size();i++)
                {
                    CMMsg msg2=(CMMsg)msg.trailerMsgs().elementAt(i);
                    if((msg!=msg2)&&(M.location()!=null)&&(M.location().okMessage(M,msg2)))
                        M.executeMsg(M,msg2);
                }
                msg.trailerMsgs().clear();
            }
        }
        return didIt;
    }
    
    public void reallyChannel(MOB mob, String channelName, String message, boolean systemMsg)
    {
        int channelInt=getChannelIndex(channelName);
        if(channelInt<0) return;
        
        message=CMProps.applyINIFilter(message,CMProps.SYSTEM_CHANNELFILTER);
        
        HashSet<ChannelFlag> flags=getChannelFlags(channelInt);
        channelName=getChannelName(channelInt);

        CMMsg msg=null;
        if(systemMsg)
        {
            String str="["+channelName+"] '"+message+"'^</CHANNEL^>^?^.";
            if((!mob.name().startsWith("^"))||(mob.name().length()>2))
                str="<S-NAME> "+str;
            msg=CMClass.getMsg(mob,null,null,CMMsg.MASK_CHANNEL|CMMsg.MASK_ALWAYS|CMMsg.MSG_SPEAK,"^Q^<CHANNEL \""+channelName+"\"^>"+str,CMMsg.NO_EFFECT,null,CMMsg.MASK_CHANNEL|(CMMsg.TYP_CHANNEL+channelInt),"^Q^<CHANNEL \""+channelName+"\"^>"+str);
        }
        else
        if(message.startsWith(",")
        ||(message.startsWith(":")
            &&(message.length()>1)
            &&(Character.isLetter(message.charAt(1))||message.charAt(1)==' ')))
        {
            String msgstr=message.substring(1);
            Vector V=CMParms.parse(msgstr);
            Social S=CMLib.socials().fetchSocial(V,true,false);
            if(S==null) S=CMLib.socials().fetchSocial(V,false,false);
            if(S!=null)
                msg=S.makeChannelMsg(mob,channelInt,channelName,V,false);
            else
            {
                msgstr=CMProps.applyINIFilter(msgstr,CMProps.SYSTEM_EMOTEFILTER);
                if(msgstr.trim().startsWith("'")||msgstr.trim().startsWith("`"))
                    msgstr=msgstr.trim();
                else
                    msgstr=" "+msgstr.trim();
                String srcstr="^<CHANNEL \""+channelName+"\"^>["+channelName+"] "+mob.name()+msgstr+"^</CHANNEL^>^N^.";
                String reststr="^<CHANNEL \""+channelName+"\"^>["+channelName+"] <S-NAME>"+msgstr+"^</CHANNEL^>^N^.";
                msg=CMClass.getMsg(mob,null,null,CMMsg.MASK_CHANNEL|CMMsg.MASK_ALWAYS|CMMsg.MSG_SPEAK,"^Q"+srcstr,CMMsg.NO_EFFECT,null,CMMsg.MASK_CHANNEL|(CMMsg.TYP_CHANNEL+channelInt),"^Q"+reststr);
            }
        }
        else
            msg=CMClass.getMsg(mob,null,null,CMMsg.MASK_CHANNEL|CMMsg.MASK_ALWAYS|CMMsg.MSG_SPEAK,"^Q^<CHANNEL \""+channelName+"\"^>You "+channelName+" '"+message+"'^</CHANNEL^>^N^.",CMMsg.NO_EFFECT,null,CMMsg.MASK_CHANNEL|(CMMsg.TYP_CHANNEL+channelInt),"^Q^<CHANNEL \""+channelName+"\"^><S-NAME> "+channelName+"S '"+message+"'^</CHANNEL^>^N^.");
        if((mob.location()!=null)
        &&((!mob.location().isInhabitant(mob))||(mob.location().okMessage(mob,msg))))
        {
            boolean areareq=flags.contains(ChannelsLibrary.ChannelFlag.SAMEAREA);
            channelQueUp(channelInt,msg);
            for(int s=0;s<CMLib.sessions().size();s++)
            {
                Session ses=CMLib.sessions().elementAt(s);
                channelTo(ses,areareq,channelInt,msg,mob);
            }
        }
        if((CMLib.intermud().i3online()&&(CMLib.intermud().isI3channel(channelName)))
        ||(CMLib.intermud().imc2online()&&(CMLib.intermud().isIMC2channel(channelName))))
            CMLib.intermud().i3channel(mob,channelName,message);
    }
    
}
