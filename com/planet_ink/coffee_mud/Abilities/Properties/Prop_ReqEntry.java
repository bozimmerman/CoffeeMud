package com.planet_ink.coffee_mud.Abilities.Properties;
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
   Copyright 2003-2018 Bo Zimmerman

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
@SuppressWarnings({"unchecked","rawtypes"})
public class Prop_ReqEntry extends Property implements TriggeredAffect
{
	@Override
	public String ID()
	{
		return "Prop_ReqEntry";
	}

	@Override
	public String name()
	{
		return "All Room/Exit Limitations";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_EXITS;
	}

	private boolean noFollow=false;
	private boolean noSneak=false;
	private String maskS="";
	private String message="";

	@Override
	public long flags()
	{
		return Ability.FLAG_ZAPPER;
	}

	@Override
	public int triggerMask()
	{
		return TriggeredAffect.TRIGGER_ENTER;
	}

	@Override
	public void setMiscText(String txt)
	{
		noFollow=false;
		noSneak=false;
		maskS=txt;
		message="";
		final Vector<String> parms=CMParms.parse(txt);
		String s;
		for(final Enumeration<String> p=parms.elements();p.hasMoreElements();)
		{
			s=p.nextElement();
			if("NOFOLLOW".startsWith(s.toUpperCase()))
			{
				maskS=CMStrings.replaceFirst(maskS, s, "");
				noFollow=true;
			}
			else
			if(s.toUpperCase().startsWith("NOSNEAK"))
			{
				maskS=CMStrings.replaceFirst(maskS, s, "");
				noSneak=true;
			}
			else
			if((s.toUpperCase().startsWith("MESSAGE"))
			&&(s.substring(7).trim().startsWith("=")))
			{
				message=s.substring(7).trim().substring(1);
				maskS=CMStrings.replaceFirst(maskS, s, "");
			}
		}
		super.setMiscText(txt);
	}

	@Override
	public String accountForYourself()
	{
		return "Entry restricted as follows: "+CMLib.masking().maskDesc(maskS);
	}

	public boolean passesMuster(MOB mob)
	{
		if(mob==null)
			return false;
		if(CMLib.flags().isATrackingMonster(mob))
			return true;
		if(CMLib.flags().isSneaking(mob)&&(!noSneak))
			return true;
		return CMLib.masking().maskCheck(maskS,mob,false);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(affected!=null)
		{
			if((msg.target() instanceof Room)
			&&(msg.targetMinor()==CMMsg.TYP_ENTER)
			&&(!CMLib.flags().isFalling(msg.source()))
			&&((msg.amITarget(affected))||(msg.tool()==affected)||(affected instanceof Area)))
			{
				final HashSet<MOB> H=new HashSet<MOB>();
				if(noFollow)
					H.add(msg.source());
				else
				{
					msg.source().getGroupMembers(H);
					final HashSet<MOB> H2=(HashSet)H.clone();
					for(final Iterator e=H2.iterator();e.hasNext();)
						((MOB)e.next()).getRideBuddies(H);
				}
				for(final Iterator e=H.iterator();e.hasNext();)
				{
					if(passesMuster((MOB)e.next()))
						return super.okMessage(myHost,msg);
				}
				msg.source().tell((message.length()==0)?L("You can not go that way."):message);
				return false;
			}
			else
			if((msg.target() instanceof Rideable)
			&&(msg.amITarget(affected)))
			{
				switch(msg.targetMinor())
				{
				case CMMsg.TYP_SIT:
				case CMMsg.TYP_ENTER:
				case CMMsg.TYP_SLEEP:
				case CMMsg.TYP_MOUNT:
					{
						HashSet<MOB> H=new HashSet<MOB>();
						if(noFollow)
							H.add(msg.source());
						else
						{
							msg.source().getGroupMembers(H);
							final HashSet<MOB> H2=new XHashSet<MOB>(H);
							for(final Iterator<MOB> e=H.iterator();e.hasNext();)
								e.next().getRideBuddies(H2);
							H=H2;
						}
						for(final Iterator<MOB> e=H.iterator();e.hasNext();)
						{
							final MOB E=e.next();
							if(passesMuster(E))
								return super.okMessage(myHost,msg);
						}
						msg.source().tell((message.length()==0)?L("You are not permitted in there."):message);
						return false;
					}
				default:
					break;
				}
			}
		}
		return super.okMessage(myHost,msg);
	}
}
