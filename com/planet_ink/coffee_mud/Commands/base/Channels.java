package com.planet_ink.coffee_mud.Commands.base;

import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Commands.sysop.*;
import com.planet_ink.coffee_mud.Commands.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;
public class Channels
{
	private Channels(){}
	private static int numChannelsLoaded=0;
	private static int numIChannelsLoaded=0;
	private static Vector channelNames=new Vector();
	private static Vector channelLevels=new Vector();
	private static Vector ichannelList=new Vector();
	private static Ability auctionA=null;

	public static void unloadChannels()
	{
		numChannelsLoaded=0;
		numIChannelsLoaded=0;
		channelNames=new Vector();
		channelLevels=new Vector();
		ichannelList=new Vector();
	}

	public static void listChannels(MOB mob)
	{
		PlayerStats pstats=mob.playerStats();
		if(pstats==null) return;
		StringBuffer buf=new StringBuffer("Available channels: \n\r");
		int col=0;
		for(int x=0;x<channelNames.size();x++)
		{
			int minLevel=((Integer)channelLevels.elementAt(x)).intValue();
			if(mob.envStats().level()>=minLevel)
			{
				if((++col)>3)
				{
					buf.append("\n\r");
					col=1;
				}
				String channelName=(String)channelNames.elementAt(x);
				String onoff="";
				if(Util.isSet((int)pstats.getChannelMask(),x))
					onoff=" (OFF)";
				buf.append(Util.padRight(channelName+onoff,24));
			}
		}
		if(channelNames.size()==0) buf.append("None!");
		else
			buf.append("\n\rUse NOCHANNELNAME (ex: NOGOSSIP) to turn a channel off.");
		mob.tell(buf.toString());
	}

	public static void channelWho(MOB mob, String channel)
	{
		if((channel==null)||(channel.length()==0))
		{
			mob.tell("You must specify a channel name. Try CHANNELS for a list.");
			return;
		}
		int x=channel.indexOf("@");
		String mud=null;
		if(x>0)
		{
			mud=channel.substring(x+1);
			channel=getChannelName(channel.substring(0,x).toUpperCase()).toUpperCase();
			if((channel.length()==0)||(getChannelInt(channel)<0))
			{
				mob.tell("You must specify a valid channel name. Try CHANNELS for a list.");
				return;
			}
			ExternalPlay.i3().i3chanwho(mob,channel,mud);
			return;
		}
		channel=getChannelName(channel.toUpperCase()).toUpperCase();
		int channelInt=getChannelInt(channel);
		if(channelInt<0)
		{
			mob.tell("You must specify a valid channel name. Try CHANNELS for a list.");
			return;
		}
		String head=new String("\n\rListening on "+channel+":\n\r");
		StringBuffer buf=new StringBuffer("");
		for(int s=0;s<Sessions.size();s++)
		{
			Session ses=(Session)Sessions.elementAt(s);
			if((!ses.killFlag())&&(ses.mob()!=null)
			&&(!ses.mob().amDead())
			&&(ses.mob().location()!=null)
			&&(ses.mob().playerStats()!=null)
			&&(!Util.isSet(ses.mob().playerStats().getChannelMask(),channelInt)))
				buf.append("["+Util.padRight(ses.mob().name(),20)+"]\n\r");
		}
		if(buf.length()==0)
			mob.tell(head+"Nobody!");
		else
			mob.tell(head+buf.toString());
	}


	public static int getChannelInt(String channelName)
	{
		for(int c=0;c<channelNames.size();c++)
			if(((String)channelNames.elementAt(c)).startsWith(channelName))
				return c;
		return -1;
	}

	public static int getChannelLvl(String channelName)
	{
		for(int c=0;c<channelNames.size();c++)
			if(((String)channelNames.elementAt(c)).startsWith(channelName))
				return ((Integer)channelLevels.elementAt(c)).intValue();
		return -1;
	}

	public static int getChannelNum(String channelName)
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
				return (String)channelNames.elementAt(c);
		return "";
	}

	public static String[][] iChannelsArray()
	{
		String[][] array=new String[numIChannelsLoaded][3];
		int num=0;
		for(int i=0;i<channelNames.size();i++)
		{
			String name=(String)channelNames.elementAt(i);
			int lvl=((Integer)channelLevels.elementAt(i)).intValue();
			String iname=(String)ichannelList.elementAt(i);
			if((iname!=null)&&(iname.trim().length()>0))
			{
				array[num][0]=iname.trim();
				array[num][1]=name.trim();
				array[num][2]=""+lvl;
				num++;
			}
		}
		return array;
	}

	public static int loadChannels(String list, String ilist, CommandSet cmdSet)
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
			if(item.indexOf(" ")>=0)
			{
				int i=item.indexOf(" ");
				channelLevels.addElement(new Integer(Util.s_int(item.substring(i+1).trim())));
				item=item.substring(0,i);
			}
			else
				channelLevels.addElement(new Integer(0));
			ichannelList.addElement("");
			channelNames.addElement(item.toUpperCase().trim());
			if(!cmdSet.containsKey(item.toUpperCase().trim()))
			{
				cmdSet.put(item.toUpperCase().trim(),new Integer(CommandSet.CHANNEL));
				cmdSet.put("NO"+item.toUpperCase().trim(),new Integer(CommandSet.NOCHANNEL));
			}
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
			channelLevels.addElement(new Integer(Util.s_int(lvl)));
			ichannelList.addElement(ichan);
			if(!cmdSet.containsKey(item.toUpperCase().trim()))
			{
				cmdSet.put(item.toUpperCase().trim(),new Integer(CommandSet.CHANNEL));
				cmdSet.put("NO"+item.toUpperCase().trim(),new Integer(CommandSet.NOCHANNEL));
			}
		}
		channelNames.addElement(new String("CLANTALK"));
		channelLevels.addElement(new Integer(0));
		ichannelList.addElement("");
		cmdSet.put(new String("CLANTALK"),new Integer(CommandSet.CHANNEL));
		cmdSet.put("NO"+(new String("CLANTALK")),new Integer(CommandSet.NOCHANNEL));
		numChannelsLoaded++;

		channelNames.addElement(new String("AUCTION"));
		channelLevels.addElement(new Integer(0));
		ichannelList.addElement("");
		//cmdSet.put(new String("AUCTION"),new Integer(CommandSet.CHANNEL));
		cmdSet.put("NO"+(new String("AUCTION")),new Integer(CommandSet.NOCHANNEL));
		numChannelsLoaded++;
		return numChannelsLoaded;
	}

	public static void channel(MOB mob, Vector commands)
	{
		channel(mob, commands, false);
	}

	public static void channel(MOB mob, Vector commands, boolean systemMsg)
	{
		PlayerStats pstats=mob.playerStats();
		String channelName=((String)commands.elementAt(0)).toUpperCase().trim();
		FullMsg msg=null;
		commands.removeElementAt(0);
		int channelInt=getChannelInt(channelName);
		int channelNum=getChannelNum(channelName);

		if((pstats!=null)&&(Util.isSet(pstats.getChannelMask(),channelInt)))
		{
			pstats.setChannelMask(pstats.getChannelMask()&(pstats.getChannelMask()-channelNum));
			mob.tell(channelName+" has been turned on.  Use `NO"+channelName.toUpperCase()+"` to turn it off again.");
			return;
		}

		if(commands.size()==0)
		{
			mob.tell(channelName+" what?");
			return;
		}

		for(int i=0;i<commands.size();i++)
		{
			String s=(String)commands.elementAt(i);
			if(s.indexOf(" ")>=0)
				commands.setElementAt("\""+s+"\"",i);
		}
		int lvl=((Integer)channelLevels.elementAt(channelInt)).intValue();
		if(lvl>mob.envStats().level())
		{
			mob.tell("This channel is not yet available to you.");
			return;
		}
		if((mob.getClanID().equalsIgnoreCase(""))&&(channelName.equalsIgnoreCase("CLANTALK")))
		{
		  mob.tell("You can't talk to your clan - you don't have one.");
		  return;
		}
		if(systemMsg)
		{
		  String str="["+channelName+"] '"+Util.combine(commands,0)+"'^?^.";
		  msg=new FullMsg(mob,null,null,Affect.MASK_CHANNEL|Affect.MASK_GENERAL|Affect.MSG_SPEAK,"^Q"+str,Affect.NO_EFFECT,null,Affect.MASK_CHANNEL|(Affect.TYP_CHANNEL+channelInt),"^Q"+mob.name()+str);
		}
		else
		{
		  String str=" "+channelName+"(S) '"+Util.combine(commands,0)+"'^?^.";
		  msg=new FullMsg(mob,null,null,Affect.MASK_CHANNEL|Affect.MASK_GENERAL|Affect.MSG_SPEAK,"^QYou"+str,Affect.NO_EFFECT,null,Affect.MASK_CHANNEL|(Affect.TYP_CHANNEL+channelInt),"^Q"+mob.name()+str);
		}
		if(mob.location().okAffect(mob,msg))
		{
			for(int s=0;s<Sessions.size();s++)
			{
				Session ses=(Session)Sessions.elementAt(s);
				MOB M=ses.mob();
				if(M==null) continue;
				if(channelName.equalsIgnoreCase("CLANTALK")
				&&(!M.getClanID().equalsIgnoreCase(mob.getClanID())))
					continue;
				if((!ses.killFlag())
				&&(!M.amDead())
				&&(M.location()!=null)
				&&(M.envStats().level()>=lvl)
				&&((M.playerStats()==null)
					||(!M.playerStats().getIgnored().containsKey(mob.Name())))
				&&(M.okAffect(M,msg)))
				{
					M.affect(M,msg);
					if(msg.trailerMsgs()!=null)
					{
						for(int i=0;i<msg.trailerMsgs().size();i++)
						{
							Affect affect=(Affect)msg.trailerMsgs().elementAt(i);
							if((affect!=msg)&&(M.okAffect(M,affect)))
								M.affect(M,affect);
						}
						msg.trailerMsgs().clear();
					}
				}
			}
		}
		if((ExternalPlay.i3().i3online())&&(ExternalPlay.i3().isI3channel(getChannelName(channelName))))
			ExternalPlay.i3().i3channel(mob,getChannelName(channelName),Util.combine(commands,0));
	}
	public static void nochannel(MOB mob, Vector commands)
	{
		PlayerStats pstats=mob.playerStats();
		if(pstats==null) return;
		String channelName=((String)commands.elementAt(0)).toUpperCase().trim().substring(2);
		commands.removeElementAt(0);


		int channelNum=0;
		for(int c=0;c<channelNames.size();c++)
		{
			if(((String)channelNames.elementAt(c)).startsWith(channelName))
				channelNum=c;
		}
		int lvl=((Integer)channelLevels.elementAt(channelNum)).intValue();
		if(lvl>mob.envStats().level())
		{
			mob.tell("This channel is not yet available to you.");
			return;
		}
		if(!Util.isSet(pstats.getChannelMask(),channelNum))
		{
			pstats.setChannelMask(pstats.getChannelMask()|(1<<channelNum));
			mob.tell("The "+channelName+" channel has been turned off.  Use `"+channelName.toUpperCase()+"` to turn it back on.");
		}
		else
			mob.tell("The "+channelName+" channel is already off.");
	}
	
	public static void friends(MOB mob, Vector commands)
	{
		PlayerStats pstats=mob.playerStats();
		if(pstats==null) return;
		Hashtable h=pstats.getFriends();
		if((commands.size()<2)||(((String)commands.elementAt(1)).equalsIgnoreCase("list")))
		{
			if(h.size()==0)
				mob.tell("You have no friends listed.  Use FRIENDS ADD to add more.");
			else
			{
				StringBuffer str=new StringBuffer("Your listed friends are: ");
				for(Enumeration e=h.elements();e.hasMoreElements();)
					str.append(((String)e.nextElement())+" ");
				mob.tell(str.toString());
			}
		}
		else
		if(((String)commands.elementAt(1)).equalsIgnoreCase("ADD"))
		{
			String name=Util.combine(commands,2);
			if(name.length()==0)
			{
				mob.tell("Add whom?");
				return;
			}
			MOB M=CMClass.getMOB("StdMOB");
			if(name.equalsIgnoreCase("all"))
				M.setName("All");
			else
			if(!ExternalPlay.DBUserSearch(M,name))
			{
				mob.tell("No player by that name was found.");
				return;
			}
			if(h.get(M.Name())!=null)
			{
				mob.tell("That name is already on your list.");
				return;
			}
			h.put(M.Name(),M.Name());
			mob.tell("The Player '"+M.Name()+"' has been added to your friends list.");
		}
		else
		if(((String)commands.elementAt(1)).equalsIgnoreCase("REMOVE"))
		{
			String name=Util.combine(commands,2);
			if(name.length()==0)
			{
				mob.tell("Remove whom?");
				return;
			}
			if(h.get(name)==null)
			{
				mob.tell("That name '"+name+"' does not appear on your list.  Watch your casing!");
				return;
			}
			h.remove(name);
			mob.tell("The Player '"+name+"' has been removed from your ignore list.");
		}
		else
		{
			mob.tell("Parameter '"+((String)commands.elementAt(1))+"' is not recognized.  Try LIST, ADD, or REMOVE.");
			return;
		}
	}

	public static void autoNotify(MOB mob)
	{
		if(Util.bset(mob.getBitmap(),MOB.ATT_AUTONOTIFY))
		{
			mob.setBitmap(Util.unsetb(mob.getBitmap(),MOB.ATT_AUTONOTIFY));
			mob.tell("Notificatoin of the arrival of your FRIENDS is now off.");
		}
		else
		{
			mob.setBitmap(Util.setb(mob.getBitmap(),MOB.ATT_AUTONOTIFY));
			mob.tell("Notification of the arrival of your FRIENDS is now on.");
		}
	}

	public static void ignore(MOB mob, Vector commands)
	{
		PlayerStats pstats=mob.playerStats();
		if(pstats==null) return;
		Hashtable h=pstats.getIgnored();
		if((commands.size()<2)||(((String)commands.elementAt(1)).equalsIgnoreCase("list")))
		{
			if(h.size()==0)
				mob.tell("You have no names on your ignore list.  Use IGNORE ADD to add more.");
			else
			{
				StringBuffer str=new StringBuffer("You are ignoring: ");
				for(Enumeration e=h.elements();e.hasMoreElements();)
					str.append(((String)e.nextElement())+" ");
				mob.tell(str.toString());
			}
		}
		else
		if(((String)commands.elementAt(1)).equalsIgnoreCase("ADD"))
		{
			String name=Util.combine(commands,2);
			if(name.length()==0)
			{
				mob.tell("Add whom?");
				return;
			}
			MOB M=CMClass.getMOB("StdMOB");
			if(!ExternalPlay.DBUserSearch(M,name))
			{
				mob.tell("No player by that name was found.");
				return;
			}
			if(h.get(M.Name())!=null)
			{
				mob.tell("That name is already on your list.");
				return;
			}
			h.put(M.Name(),M.Name());
			mob.tell("The Player '"+M.Name()+"' has been added to your ignore list.");
		}
		else
		if(((String)commands.elementAt(1)).equalsIgnoreCase("REMOVE"))
		{
			String name=Util.combine(commands,2);
			if(name.length()==0)
			{
				mob.tell("Remove whom?");
				return;
			}
			if(h.get(name)==null)
			{
				mob.tell("That name '"+name+"' does not appear on your list.  Watch your casing!");
				return;
			}
			h.remove(name);
			mob.tell("The Player '"+name+"' has been removed from your ignore list.");
		}
		else
		{
			mob.tell("Parameter '"+((String)commands.elementAt(1))+"' is not recognized.  Try LIST, ADD, or REMOVE.");
			return;
		}
	}

	public static void quiet(MOB mob)
	{
		PlayerStats pstats=mob.playerStats();
		if(pstats==null) return;
		boolean turnedoff=false;
		for(int c=0;c<channelNames.size();c++)
		{
			if(!Util.isSet(pstats.getChannelMask(),c))
			{
				pstats.setChannelMask(pstats.getChannelMask()|(1<<c));
				turnedoff=true;
			}
		}
		if(turnedoff)
			mob.tell("All channels have been turned off.");
		else
		{
			mob.tell("All channels have been turned back on.");
			pstats.setChannelMask(0);
		}
	}

	public static void auction(MOB mob, Vector commands)
		throws java.io.IOException
	{
		PlayerStats pstats=mob.playerStats();
		if(pstats==null) return;
		int channelInt=getChannelInt("AUCTION");
		int channelNum=getChannelNum("AUCTION");

		if(Util.isSet(pstats.getChannelMask(),channelInt))
		{
			pstats.setChannelMask(pstats.getChannelMask()&(pstats.getChannelMask()-channelNum));
			mob.tell("The AUCTION channel has been turned on.  Use `NOAUCTION` to turn it off again.");
		}

		if((commands.size()>1)
		&&(auctionA!=null)
		&&(auctionA.invoker()==mob))
		{
			if(((String)commands.elementAt(1)).equalsIgnoreCase("CHANNEL"))
			{
				commands.removeElementAt(1);
				channel(mob,commands);
				return;
			}
			else
			if(((String)commands.elementAt(1)).equalsIgnoreCase("CLOSE"))
			{
				commands.removeElementAt(1);
				Vector V=new Vector();
				V.addElement("AUCTION");
				V.addElement("The auction has been closed.");
				ExternalPlay.deleteTick(auctionA,Host.QUEST_TICK);
				auctionA=null;
				channel(mob,commands);
				return;
			}
		}
		if(auctionA==null)
		{
			if(commands.size()==1)
			{
				mob.tell("There is nothing up for auction right now.");
				return;
			}
			Vector V=new Vector();
			if((commands.size()>2)
			&&((Util.s_int((String)commands.lastElement())>0)||(((String)commands.lastElement()).equals("0"))))
			{
				V.addElement((String)commands.lastElement());
				commands.removeElementAt(commands.size()-1);
			}
			else
				V.addElement("0");

			String s=Util.combine(commands,1);
			Environmental E=mob.fetchInventory(null,s);
			if((E==null)||(E instanceof MOB))
			{
				mob.tell("'"+s+"' is not an item you can auction.");
				return;
			}
			if((!mob.isMonster())&&(!mob.session().confirm("Auction "+E.name()+" with a starting bid of "+((String)V.firstElement())+" (Y/n)? ","Y")))
				return;
			auctionA=CMClass.getAbility("Prop_Auction");
			auctionA.invoke(mob,V,E,false);
		}
		else
		{
			commands.removeElementAt(0);
			auctionA.invoke(mob,commands,null,false);
		}
	}
}