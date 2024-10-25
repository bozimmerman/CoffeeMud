package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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
   Copyright 2004-2024 Bo Zimmerman

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
public class GTell extends StdCommand
{
	public GTell()
	{
	}

	private final String[] access=I(new String[]{"GTELL","FTELL","GT"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		final String cmd = (commands.size()>1)?commands.get(0).toUpperCase():"GTELL";
		String text=CMParms.combine(commands,1);
		if(text.length()==0)
		{
			mob.tell(L("Tell your group what?"));
			return false;
		}
		text=CMProps.applyINIFilter(text,CMProps.Str.SAYFILTER);

		if((commands.size()>2)
		&&((commands.get(1).equalsIgnoreCase("last"))
		&&(CMath.isNumber(CMParms.combine(commands,2))))
		&&(mob.playerStats()!=null))
		{
			final java.util.List<PlayerStats.TellMsg> V=mob.playerStats().getGTellStack();
			if(V.size()==0)
				mob.tell(L("No telling."));
			else
			{
				int num=CMath.s_int(CMParms.combine(commands,2));
				if(num>V.size())
					num=V.size();
				for(int i=V.size()-num;i<V.size();i++)
					mob.tell(V.get(i).message());
			}
			return false;
		}

		CMMsg tellMsg=CMClass.getMsg(mob,null,null,CMMsg.MSG_TELL,null,CMMsg.NO_EFFECT,null,CMMsg.MSG_TELL,null);
		text=text.trim();
		final String rawTextMsgStr;
		if(text.startsWith(",")
		||(text.startsWith(":")
			&&(text.length()>1)
			&&(Character.isLetter(text.charAt(1))||text.charAt(1)==' ')))
		{
			text=text.substring(1);
			final Vector<String> V=CMParms.parse(text);
			Social S=CMLib.socials().fetchSocial(V,true,false);
			if(S==null)
				S=CMLib.socials().fetchSocial(V,false,false);
			if((S!=null)
			&&(S.meetsCriteriaToUse(mob)))
			{
				tellMsg=S.makeMessage(mob,
						"^t^<GTELL \""+CMStrings.removeColors(mob.name())+"\"^>[GTELL] ",
						"^</GTELL^>^?^.",
						CMMsg.MASK_ALWAYS,
						CMMsg.MSG_TELL,
						V,
						null,
						false);
				if((tellMsg.othersMessage()!=null)&&(tellMsg.othersMessage().length()>0))
					rawTextMsgStr=CMStrings.removeColors(tellMsg.othersMessage());
				else
					rawTextMsgStr=CMStrings.removeColors(tellMsg.sourceMessage());
			}
			else
			{
				if(text.trim().startsWith("'")||text.trim().startsWith("`"))
					text=text.trim();
				else
					text=" "+text.trim();
				tellMsg.setSourceMessage("^t^<GTELL \""+CMStrings.removeColors(mob.name())+"\"^>[GTELL] <S-NAME>"+text+"^</GTELL^>^?^.");
				tellMsg.setOthersMessage("^t^<GTELL \""+CMStrings.removeColors(mob.name())+"\"^>"+mob.name()+" tells the group '"+text+"'^</GTELL^>^?^.");
				rawTextMsgStr=mob.name()+" tells the group '"+text+"'";
			}
		}
		else
		{
			tellMsg.setSourceMessage("^t^<GTELL \""+CMStrings.removeColors(mob.name())+"\"^><S-NAME> tell(s) the group '"+text+"'^</GTELL^>^?^.");
			tellMsg.setOthersMessage("^t^<GTELL \""+CMStrings.removeColors(mob.name())+"\"^>"+mob.name()+" tells the group '"+text+"'^</GTELL^>^?^.");
			rawTextMsgStr=mob.name()+" tells the group '"+text+"'";
		}

		if((mob.session()!=null)
		&&(mob.session().getClientTelnetMode(Session.TELNET_GMCP)))
		{
			mob.session().sendGMCPEvent("comm.channel", "{\"chan\":\"GTELL\","
					+ "\"msg\":\""+MiniJSON.toJSONString(rawTextMsgStr)+"\""
					+ ",\"player\":\""+mob.name()+"\"}");
		}

		final Set<MOB> group;
		if(cmd.startsWith("F"))
		{
			group = new XTreeSet<MOB>();
			final Stack<MOB> stk = new Stack<MOB>();
			stk.add(mob);
			while(stk.size() > 0)
			{
				final MOB M = stk.pop();
				if(!group.contains(M))
				{
					group.add(M);
					for(int f=0;f<M.numFollowers();f++)
					{
						final MOB F = M.fetchFollower(f);
						if(F != null)
							stk.add(F);
					}
				}
			}
		}
		else
			group = mob.getGroupMembers(new HashSet<MOB>());
		final CMMsg msg=tellMsg;
		for (final MOB target : group)
		{
			if((mob.location().okMessage(mob,msg))
			&&(target.okMessage(target,msg)))
			{
				if((target.session()!=null)
				&&(target!=mob)
				&&(target.session().getClientTelnetMode(Session.TELNET_GMCP)))
				{
					target.session().sendGMCPEvent("comm.channel", "{\"chan\":\"GTELL\","
							+ "\"msg\":\""+MiniJSON.toJSONString(rawTextMsgStr)+"\""
							+ ",\"player\":\""+mob.name()+"\"}");
				}
				if(target.playerStats()!=null)
				{
					final String tellStr=(target==mob)?msg.sourceMessage():(
									(target==msg.target())?msg.targetMessage():msg.othersMessage()
									);
					final String msgStr = CMLib.coffeeFilter().fullOutFilter(target.session(),target,mob,msg.target(),null,CMStrings.removeColors(tellStr),false);
					target.playerStats().addGTellStack(mob.Name(), target.Name(), msgStr);
				}
				target.executeMsg(target,msg);
				if(msg.trailerMsgs()!=null)
				{
					for(final CMMsg msg2 : msg.trailerMsgs())
					{
						if((msg2!=msg)&&(target.okMessage(target,msg2)))
							target.executeMsg(target,msg2);
					}
					msg.trailerMsgs().clear();
				}
				if(msg.trailerRunnables()!=null)
				{
					for(final Runnable r : msg.trailerRunnables())
						CMLib.threads().executeRunnable(r);
					msg.trailerRunnables().clear();
				}
			}
		}
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

}
