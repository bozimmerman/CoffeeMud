package com.planet_ink.coffee_mud.Commands;
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
public class GTell extends StdCommand
{
	public GTell(){}

	private String[] access={"GTELL","GT"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		String text=CMParms.combine(commands,1);
		if(text.length()==0)
		{
			mob.tell("Tell the group what?");
			return false;
		}
		text=CMProps.applyINIFilter(text,CMProps.SYSTEM_SAYFILTER);
		
		if((commands.size()>2)
		&&((((String)commands.elementAt(1)).equalsIgnoreCase("last"))
		&&(CMath.isNumber(CMParms.combine(commands,2))))
		&&(mob.playerStats()!=null))
		{
			Vector V=mob.playerStats().getGTellStack();
			if(V.size()==0)
				mob.tell("No telling.");
			else
			{
				int num=CMath.s_int(CMParms.combine(commands,2));
				if(num>V.size()) num=V.size();
				for(int i=V.size()-num;i<V.size();i++)
					mob.tell((String)V.elementAt(i));
			}
			return false;
		}
								 
		CMMsg tellMsg=CMClass.getMsg(mob,null,null,CMMsg.MSG_TELL,null,CMMsg.NO_EFFECT,null,CMMsg.MSG_TELL,null);
		text=text.trim();
		if(text.startsWith(",")
		||(text.startsWith(":")
			&&(text.length()>1)
			&&(Character.isLetter(text.charAt(1))||text.charAt(1)==' ')))
		{
			text=text.substring(1);
			Vector V=CMParms.parse(text);
			Social S=CMLib.socials().fetchSocial(V,true,false);
			if(S==null) S=CMLib.socials().fetchSocial(V,false,false);
			if(S!=null)
			{
				tellMsg=S.makeMessage(mob,
						"^t^<GTELL \""+mob.name()+"\"^>[GTELL] ",
						"^</GTELL^>^?^.",
						CMMsg.MASK_ALWAYS,
						CMMsg.MSG_TELL,
						V,
						null,
						false);
			}
			else
			{
                if(text.trim().startsWith("'")||text.trim().startsWith("`"))
                	text=text.trim();
                else
                	text=" "+text.trim();
    			tellMsg.setSourceMessage("^t^<GTELL \""+mob.name()+"\"^>[GTELL] <S-NAME>"+text+"^</GTELL^>^?^.");
    			tellMsg.setOthersMessage(tellMsg.sourceMessage());
			}
		}
		else
		{
			tellMsg.setSourceMessage("^t^<GTELL \""+mob.name()+"\"^><S-NAME> tell(s) the group '"+text+"'^</GTELL^>^?^.");
			tellMsg.setOthersMessage(tellMsg.sourceMessage());
		}
		
		HashSet group=mob.getGroupMembers(new HashSet());
		CMMsg msg=tellMsg;
		for(Iterator e=group.iterator();e.hasNext();)
		{
			MOB target=(MOB)e.next();
			if((mob.location().okMessage(mob,msg))
			&&(target.okMessage(target,msg)))
			{
				if(target.playerStats()!=null)
				{
					String tellStr=(target==mob)?msg.sourceMessage():(
									(target==msg.target())?msg.targetMessage():msg.othersMessage()
									);
					target.playerStats().addGTellStack(CMLib.coffeeFilter().fullOutFilter(target.session(),target,mob,msg.target(),null,CMStrings.removeColors(tellStr),false));
				}
				target.executeMsg(target,msg);
				if(msg.trailerMsgs()!=null)
				{
					for(int i=0;i<msg.trailerMsgs().size();i++)
					{
						CMMsg msg2=(CMMsg)msg.trailerMsgs().elementAt(i);
						if((msg2!=msg)&&(target.okMessage(target,msg2)))
							target.executeMsg(target,msg2);
					}
					msg.trailerMsgs().clear();
				}
			}
		}
		return false;
	}
	
	public boolean canBeOrdered(){return true;}

	
}
