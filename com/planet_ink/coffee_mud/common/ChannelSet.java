package com.planet_ink.coffee_mud.common;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class ChannelSet
{
	protected static final int QUEUE_SIZE=100;
	
	protected static int numChannelsLoaded=0;
	protected static int numIChannelsLoaded=0;
	protected static int numImc2ChannelsLoaded=0;
    protected static int numCommandJournalsLoaded=0;
	protected static Vector channelNames=new Vector();
	protected static Vector channelMasks=new Vector();
    protected static Vector channelFlags=new Vector();
	protected static Vector ichannelList=new Vector();
	protected static Vector imc2channelList=new Vector();
	protected static Vector channelQue=new Vector();
    protected static Vector commandJournalNames=new Vector();
    protected static Vector commandJournalMasks=new Vector();
    protected static Vector commandJournalFlags=new Vector();
    protected static final Vector emptyVector=new Vector();
	
	public static int getNumChannels()
	{
		return channelNames.size();
	}
	
	public static String getChannelMask(int i)
	{
		if((i>=0)&&(i<channelMasks.size()))
			return (String)channelMasks.elementAt(i);
		return "";
	}

    
    public static Vector getChannelFlags(int i)
    {
        if((i>=0)&&(i<channelFlags.size()))
            return (Vector)channelFlags.elementAt(i);
        return emptyVector;
    }

	public static String getChannelName(int i)
	{
		if((i>=0)&&(i<channelNames.size()))
			return (String)channelNames.elementAt(i);
		return "";
	}

	public static Vector getChannelQue(int i)
	{
		if((i>=0)&&(i<channelQue.size()))
			return (Vector)channelQue.elementAt(i);
		return new Vector();
	}
	
	public static boolean mayReadThisChannel(MOB sender,
											 boolean areaReq,
											 MOB M, 
											 int i)
	{
		if((sender==null)
		||(M==null)
		||(M.amDead())
		||(M.location()==null)
		||(M.playerStats()==null))
		    return false;
		
		if(getChannelFlags(i).contains("CLANONLY"))
        {
            // only way to fail an all-clan send is to have NO clan.
            if((M.getClanID().length()==0)
            ||(M.getClanRole()==Clan.POS_APPLICANT))
                return false;
            
            // now either clans must be same, or must be for all
            if((!sender.getClanID().equalsIgnoreCase("ALL"))
    		&&(!M.getClanID().equalsIgnoreCase(sender.getClanID())))
    			return false;
        }
		
		if((!M.playerStats().getIgnored().contains(sender.Name()))
		&&(MUDZapper.zapperCheck(getChannelMask(i),M))
		&&((!areaReq)
		   ||(M.location().getArea()==sender.location().getArea()))
		&&(!Util.isSet(M.playerStats().getChannelMask(),i)))
			return true;
		return false;
	}
	
	public static boolean mayReadThisChannel(MOB sender,
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
		
        if(getChannelFlags(i).contains("CLANONLY"))
        {
            // only way to fail an all-clan send is to have NO clan.
            if((M.getClanID().length()==0)
            ||(M.getClanRole()==Clan.POS_APPLICANT))
                return false;
            // now either clans must be same, or must be for all
            if((!sender.getClanID().equalsIgnoreCase("ALL"))
            &&(!M.getClanID().equalsIgnoreCase(sender.getClanID())))
                return false;
        }
		
		if((!ses.killFlag())
		&&(!M.playerStats().getIgnored().contains(senderName))
		&&(MUDZapper.zapperCheck(getChannelMask(i),M))
		&&((!areaReq)
		   ||(M.location().getArea()==sender.location().getArea()))
		&&(!Util.isSet(M.playerStats().getChannelMask(),i)))
			return true;
		return false;
	}
	
	public static boolean mayReadThisChannel(MOB M, int i, boolean zapCheckOnly)
	{
	    if(M==null) return false;
	    
	    if(i>=ChannelSet.getNumChannels())
	        return false;
	    
		if(getChannelFlags(i).contains("CLANONLY")
		&&((M.getClanID().length()==0)||(M.getClanRole()==Clan.POS_APPLICANT)))
		    return false;

		if(((zapCheckOnly)||((!M.amDead())&&(M.location()!=null)))
		&&(MUDZapper.zapperCheck(getChannelMask(i),M))
		&&(!Util.isSet(M.playerStats().getChannelMask(),i)))
			return true;
		return false;
	}

	public static void channelQueUp(int i, CMMsg msg)
	{
        CMMap.sendGlobalMessage(msg.source(),CMMsg.TYP_CHANNEL,msg);
		Vector q=getChannelQue(i);
		synchronized(q)
		{
			if(q.size()>=QUEUE_SIZE)
				q.removeElementAt(0);
			q.addElement(msg);
		}
	}
	
	public static int getChannelIndex(String channelName)
	{
		for(int c=0;c<channelNames.size();c++)
			if(((String)channelNames.elementAt(c)).startsWith(channelName))
				return c;
		return -1;
	}

	public static int getChannelCodeNumber(String channelName)
	{
		for(int c=0;c<channelNames.size();c++)
			if(((String)channelNames.elementAt(c)).startsWith(channelName))
				return 1<<c;
		return -1;
	}

	public static String getChannelName(String channelName)
	{
		for(int c=0;c<channelNames.size();c++)
			if(((String)channelNames.elementAt(c)).startsWith(channelName))
				return ((String)channelNames.elementAt(c)).toUpperCase();
		return "";
	}

	public static Vector getFlaggedChannelNames(String flag)
	{
        flag=flag.toUpperCase().trim();
        Vector channels=new Vector();
		for(int c=0;c<channelNames.size();c++)
			if(((Vector)channelFlags.elementAt(c)).contains(flag))
                channels.addElement(((String)channelNames.elementAt(c)).toUpperCase());
		return channels;
	}
	
	public static void unloadChannelsAndCommandJournals()
	{
		numChannelsLoaded=0;
		numIChannelsLoaded=0;
        numCommandJournalsLoaded=0;
        numImc2ChannelsLoaded=0;
		channelNames=new Vector();
		channelMasks=new Vector();
        channelFlags=new Vector();
		ichannelList=new Vector();
		imc2channelList=new Vector();
		channelQue=new Vector();
        commandJournalMasks=new Vector();
        commandJournalFlags=new Vector();
        commandJournalNames=new Vector();
	}

	public static String[][] imc2ChannelsArray()
	{
		String[][] array=new String[numImc2ChannelsLoaded][4];
		int num=0;
		for(int i=0;i<channelNames.size();i++)
		{
			String name=(String)channelNames.elementAt(i);
			String mask=(String)channelMasks.elementAt(i);
            Vector flags=(Vector)channelFlags.elementAt(i);
			String iname=(String)imc2channelList.elementAt(i);
			if((iname!=null)&&(iname.trim().length()>0))
			{
				array[num][0]=iname.trim();
				array[num][1]=name.trim();
				array[num][2]=mask;
                array[num][3]=Util.combine(flags,0);
				num++;
			}
		}
		return array;
	}
	public static String[][] iChannelsArray()
	{
		String[][] array=new String[numIChannelsLoaded][4];
		int num=0;
		for(int i=0;i<channelNames.size();i++)
		{
			String name=(String)channelNames.elementAt(i);
			String mask=(String)channelMasks.elementAt(i);
			String iname=(String)ichannelList.elementAt(i);
            Vector flags=(Vector)channelFlags.elementAt(i);
			if((iname!=null)&&(iname.trim().length()>0))
			{
				array[num][0]=iname.trim();
				array[num][1]=name.trim();
				array[num][2]=mask;
                array[num][3]=Util.combine(flags,0);
				num++;
			}
		}
		return array;
	}
	public static String[] getChannelNames()
	{
		if(channelNames.size()==0) return null;
		return Util.toStringArray(channelNames);
	}
	
	public static Vector clearInvalidSnoopers(Session mySession, int channelCode)
	{
	    Vector invalid=null;
	    if(mySession!=null)
	    {
		    Session S=null;
		    for(int s=0;s<Sessions.size();s++)
		    {
		        S=Sessions.elementAt(s);
		        if((S!=mySession)
		        &&(S.mob()!=null)
		        &&(mySession.amBeingSnoopedBy(S))
		        &&(!ChannelSet.mayReadThisChannel(S.mob(),channelCode,false)))
		        {
		            if(invalid==null) invalid=new Vector();
		            invalid.add(S);
		            mySession.stopBeingSnoopedBy(S);
		        }
		    }
	    }
	    return invalid;	    
	}
	
	public static void restoreInvalidSnoopers(Session mySession, Vector invalid)
	{
	    if((mySession==null)||(invalid==null)) return;
		for(int s=0;s<invalid.size();s++)
		    mySession.startBeingSnoopedBy((Session)invalid.elementAt(s));
	}

    public static String[] ALLFLAGS={
        "DEFAULT","SAMEAREA","CLANONLY","READONLY",
        "EXECUTIONS","LOGINS","LOGOFFS","BIRTHS","MARRIAGES", 
        "DIVORCES","CHRISTENINGS","LEVELS","DETAILEDLEVELS","DEATHS","DETAILEDDEATHS",
        "CONQUESTS","CONCEPTIONS","NEWPLAYERS","LOSTLEVELS","PLAYERPURGES","CLANINFO"};
    
    public static String parseOutFlags(String mask, Vector flags)
    {
        Vector V=Util.parse(mask);
        for(int v=V.size()-1;v>=0;v--)
        {
            String s=((String)V.elementAt(v)).toUpperCase();
            for(int i=0;i<ALLFLAGS.length;i++)
            if(s.equals(ALLFLAGS[i]))
            {
                V.removeElementAt(v);
                flags.addElement(s.trim().toUpperCase());
                break;
            }
        }
        return Util.combine(V,0);
    }
    
	public static int loadChannels(String list, String ilist, String imc2list)
	{
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
            Vector flags=new Vector();
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
            Vector flags=new Vector();
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
            Vector flags=new Vector();
            channelMasks.addElement(parseOutFlags(lvl,flags));
            channelFlags.addElement(flags);
			imc2channelList.addElement(ichan);
			ichannelList.addElement("");
		}

		channelNames.addElement(new String("AUCTION"));
		channelQue.addElement(new Vector());
		channelMasks.addElement("");
        channelFlags.addElement(new Vector());
		ichannelList.addElement("");
		imc2channelList.addElement("");
		numChannelsLoaded++;

		numChannelsLoaded++;
		return numChannelsLoaded;
	}

    public static int loadCommandJournals(String list)
    {
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
            numCommandJournalsLoaded++;
            x=item.indexOf(" ");
            Hashtable flags=new Hashtable();
            if(x>0)
            {
                String mask=item.substring(x+1).trim();
                String[] possflags={"CHANNEL=","ADDROOM","EXPIRE=","ADMINECHO"};
                for(int pf=0;pf<possflags.length;pf++)
                {
                    int keyx=mask.toUpperCase().indexOf(possflags[pf]);
                    if(keyx>=0)
                    {
                        int keyy=mask.indexOf(" ",keyx+1);
                        if(keyy<0) keyy=mask.length();
                        if((keyx==0)||(Character.isWhitespace(mask.charAt(keyx-1))))
                        {
                            String parm=mask.substring(keyx+possflags[pf].length(),keyy).trim();
                            if((parm.length()==0)||(possflags[pf].endsWith("=")))
                            {
                                flags.put(possflags[pf],parm);
                                mask=mask.substring(0,keyx).trim()+" "+mask.substring(keyy).trim();
                            }
                        }
                    }
                }
                commandJournalMasks.addElement(mask);
                item=item.substring(0,x);
            }
            else
                commandJournalMasks.addElement("");
            commandJournalFlags.addElement(flags);
            commandJournalNames.addElement(item.toUpperCase().trim());
        }
        return numCommandJournalsLoaded;
    }
    
    public static int getNumCommandJournals()
    {
        return commandJournalNames.size();
    }
    
    public static String getCommandJournalMask(int i)
    {
        if((i>=0)&&(i<commandJournalMasks.size()))
            return (String)commandJournalMasks.elementAt(i);
        return "";
    }

    public static String getCommandJournalName(int i)
    {
        if((i>=0)&&(i<commandJournalNames.size()))
            return (String)commandJournalNames.elementAt(i);
        return "";
    }

    public static Hashtable getCommandJournalFlags(int i)
    {
        if((i>=0)&&(i<commandJournalFlags.size()))
            return (Hashtable)commandJournalFlags.elementAt(i);
        return new Hashtable();
    }
    public static String[] getCommandJournalNames()
    {
        if(commandJournalNames.size()==0) return null;
        return Util.toStringArray(commandJournalNames);
    }
}
	
