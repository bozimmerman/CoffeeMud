package com.planet_ink.coffee_mud.Commands.extra;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class BaseChanneler extends StdCommand
{
	public static void reallyChannel(MOB mob,
									 String channelName,
									 String message,
									 boolean systemMsg)
	{
		int channelInt=ChannelSet.getChannelInt(channelName);
		if(channelInt<0) return;

		String mask=ChannelSet.getChannelMask(channelInt);
		channelName=ChannelSet.getChannelName(channelInt);

		CMMsg msg=null;
		if(systemMsg)
		{
			String str="["+channelName+"] '"+message+"'^?^.";
			if((!mob.name().startsWith("^"))||(mob.name().length()>2))
				str=" "+str;
			msg=new FullMsg(mob,null,null,CMMsg.MASK_CHANNEL|CMMsg.MASK_GENERAL|CMMsg.MSG_SPEAK,"^Q^q"+str,CMMsg.NO_EFFECT,null,CMMsg.MASK_CHANNEL|(CMMsg.TYP_CHANNEL+channelInt),"^Q^q"+mob.name()+str);
		}
		else
		if((message.startsWith(":")||message.startsWith(","))&&(message.trim().length()>1))
		{
			String msgstr=message.substring(1);
			Vector V=Util.parse(msgstr);
			Social S=Socials.FetchSocial(V,true);
			if(S==null) S=Socials.FetchSocial(V,false);
			if(S!=null)
				msg=S.makeChannelMsg(mob,channelInt,channelName,V,false);
			else
			{
				String str="["+channelName+"] "+mob.name()+" "+msgstr+"^?^.";
				msg=new FullMsg(mob,null,null,CMMsg.MASK_CHANNEL|CMMsg.MASK_GENERAL|CMMsg.MSG_SPEAK,"^Q^q"+str,CMMsg.NO_EFFECT,null,CMMsg.MASK_CHANNEL|(CMMsg.TYP_CHANNEL+channelInt),"^Q^q"+str);
			}
		}
		else
		{
			String str=" "+channelName+"(S) '"+message+"'^?^.";
			msg=new FullMsg(mob,null,null,CMMsg.MASK_CHANNEL|CMMsg.MASK_GENERAL|CMMsg.MSG_SPEAK,"^Q^qYou"+str,CMMsg.NO_EFFECT,null,CMMsg.MASK_CHANNEL|(CMMsg.TYP_CHANNEL+channelInt),"^Q^q"+mob.name()+str);
		}
		if((mob.location()!=null)
		&&((!mob.location().isInhabitant(mob))||(mob.location().okMessage(mob,msg))))
		{
			for(int s=0;s<Sessions.size();s++)
			{
				Session ses=(Session)Sessions.elementAt(s);
				MOB M=ses.mob();
				if(M==null) continue;

				if(channelName.equalsIgnoreCase("CLANTALK")
				&&((!mob.getClanID().equals("ALL"))||(M.getClanID().length()==0))
				&&(!M.getClanID().equalsIgnoreCase(mob.getClanID())))
					continue;

				if((!ses.killFlag())
				&&(!M.amDead())
				&&(M.location()!=null)
				&&(MUDZapper.zapperCheck(mask,M))
				&&((M.playerStats()==null)
					||(!M.playerStats().getIgnored().containsKey(mob.Name())))
				&&(M.okMessage(M,msg)))
				{
					M.executeMsg(M,msg);
					if(msg.trailerMsgs()!=null)
					{
						for(int i=0;i<msg.trailerMsgs().size();i++)
						{
							CMMsg msg2=(CMMsg)msg.trailerMsgs().elementAt(i);
							if((msg!=msg2)&&(M.okMessage(M,msg2)))
								M.executeMsg(M,msg2);
						}
						msg.trailerMsgs().clear();
					}
				}
			}
		}
		if((CMClass.I3Interface().i3online())&&(CMClass.I3Interface().isI3channel(channelName)))
			CMClass.I3Interface().i3channel(mob,channelName,message);
	}
}
