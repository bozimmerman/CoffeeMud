package com.planet_ink.coffee_mud.Commands.base;

import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Commands.base.sysop.CreateEdit;
import com.planet_ink.coffee_mud.Commands.base.sysop.SysopItemUsage;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;
public class Channels
{
	private int numChannelsLoaded=0;
	private int numIChannelsLoaded=0;
	private Vector channelNames=new Vector();
	private Vector channelLevels=new Vector();
	private Vector ichannelList=new Vector();

	public void listChannels(MOB mob)
	{
		StringBuffer buf=new StringBuffer("Available channels: \n\r");
		int col=0;
		for(int x=0;x<channelNames.size();x++)
		{
			if((++col)>3)
			{
				buf.append("\n\r");
				col=1;
			}
			String channelName=(String)channelNames.elementAt(x);
			int minLevel=((Integer)channelLevels.elementAt(x)).intValue();
			String onoff="";
			if(Util.isSet((int)mob.getChannelMask(),x))
				onoff=" (OFF)";
			buf.append(Util.padRight(channelName+onoff,24));
		}
		if(channelNames.size()==0) buf.append("None!");
		mob.tell(buf.toString());
	}
	
	public int getChannelInt(String channelName)
	{
		for(int c=0;c<channelNames.size();c++)
			if(((String)channelNames.elementAt(c)).startsWith(channelName))
				return c;
		return -1;
	}

	public int getChannelNum(String channelName)
	{
		for(int c=0;c<channelNames.size();c++)
			if(((String)channelNames.elementAt(c)).startsWith(channelName))
				return 1<<c;
		return -1;
	}

	public String getChannelName(String channelName)
	{
		for(int c=0;c<channelNames.size();c++)
			if(((String)channelNames.elementAt(c)).startsWith(channelName))
				return (String)channelNames.elementAt(c);
		return "";
	}
	
	public String[][] iChannelsArray()
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
	
	public int loadChannels(String list, String ilist, CommandSet cmdSet)
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
			cmdSet.put(item.toUpperCase().trim(),new Integer(CommandSet.CHANNEL));
			cmdSet.put("NO"+item.toUpperCase().trim(),new Integer(CommandSet.NOCHANNEL));
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
			cmdSet.put(item.toUpperCase().trim(),new Integer(CommandSet.CHANNEL));
			cmdSet.put("NO"+item.toUpperCase().trim(),new Integer(CommandSet.NOCHANNEL));
		}
		return numChannelsLoaded;
	}

	public void channel(MOB mob, Vector commands)
	{
		String channelName=((String)commands.elementAt(0)).toUpperCase().trim();
		commands.removeElementAt(0);


		int channelInt=getChannelInt(channelName);
		int channelNum=getChannelNum(channelName);

		if(Util.isSet(mob.getChannelMask(),channelInt))
		{
			mob.setChannelMask(mob.getChannelMask()&(mob.getChannelMask()-channelNum));
			mob.tell(channelName+" has been turned back on.");
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
		String str=" "+channelName+"(S) '"+Util.combine(commands,0)+"'^?^^";
		FullMsg msg=new FullMsg(mob,null,null,Affect.MSG_OK_ACTION,"^QYou"+str,Affect.NO_EFFECT,null,Affect.MASK_CHANNEL|channelInt,"^Q"+mob.name()+str);
		if(mob.location().okAffect(msg))
		{
			for(int s=0;s<Sessions.size();s++)
			{
				Session ses=(Session)Sessions.elementAt(s);
				if((!ses.killFlag())&&(ses.mob()!=null)
				&&(!ses.mob().amDead())
				&&(ses.mob().location()!=null)
				&&(ses.mob().envStats().level()>=lvl)
				&&(ses.mob().okAffect(msg)))
					ses.mob().affect(msg);
			}
		}
		if((ExternalPlay.i3().i3online())&&(ExternalPlay.i3().isI3channel(getChannelName(channelName))))
			ExternalPlay.i3().i3channel(mob,getChannelName(channelName),Util.combine(commands,0));
	}
	public void nochannel(MOB mob, Vector commands)
	{
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
		if(!Util.isSet(mob.getChannelMask(),channelNum))
		{
			mob.setChannelMask(mob.getChannelMask()|(1<<channelNum));
			mob.tell("The "+channelName+" channel has been turned off.");
		}
		else
			mob.tell("The "+channelName+" channel is already off.");
	}

	public void quiet(MOB mob)
	{
		boolean turnedoff=false;
		for(int c=0;c<channelNames.size();c++)
		{
			if(!Util.isSet(mob.getChannelMask(),c))
			{
				mob.setChannelMask(mob.getChannelMask()|(1<<c));
				turnedoff=true;
			}
		}
		if(turnedoff)
			mob.tell("All channels have been turned off.");
		else
		{
			mob.tell("All channels have been turned back on.");
			mob.setChannelMask(0);
		}
	}
}
