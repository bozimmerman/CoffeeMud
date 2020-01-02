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
   Copyright 2002-2020 Bo Zimmerman

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
public class Prop_ReqLevels extends Property implements TriggeredAffect
{
	@Override
	public String ID()
	{
		return "Prop_ReqLevels";
	}

	@Override
	public String name()
	{
		return "Level Limitations";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_EXITS;
	}

	private boolean			noFollow	= false;
	private boolean			noSneak		= false;
	private boolean			allFlag		= false;
	private final boolean	sysopFlag	= false;
	private String			message		= L("You are not allowed to go that way.");
	private int[]			lvls		= new int[0];

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
	public void setMiscText(final String txt)
	{
		noFollow=false;
		noSneak=false;
		message = L("You are not allowed to go that way.");
		lvls = new int[0];
		final Vector<String> parms=CMParms.parse(txt.toUpperCase());
		String s;
		for(final Enumeration<String> p=parms.elements();p.hasMoreElements();)
		{
			s=p.nextElement();
			if("NOFOLLOW".startsWith(s))
				noFollow=true;
			else
			if(s.startsWith("NOSNEAK"))
				noSneak=true;
			else
			if("ALL".equals(s))
				allFlag=true;
			else
			if("SYSOP".equals(s))
				noSneak=true;
		}
		message=CMParms.getParmStr(txt, "MESSAGE", message);
		int lastPlace=0;
		int x=0;
		final String text=txt.trim();
		if(text.length()>0)
		{
			final List<Integer> vals=new LinkedList<Integer>();
			while(x>=0)
			{
				x=text.indexOf('>',lastPlace);
				if(x<0)
					x=text.indexOf('<',lastPlace);
				if(x<0)
					x=text.indexOf('=',lastPlace);
				if(x>=0)
				{
					final char primaryChar=text.charAt(x);
					x++;
					boolean andEqual=false;
					if(text.charAt(x)=='=')
					{
						andEqual=true;
						x++;
					}
					lastPlace=x;
					String cmpString="";
					while((x<text.length())
					&&(((text.charAt(x)==' ')&&(cmpString.length()==0))
						||(Character.isDigit(text.charAt(x)))))
					{
						if(Character.isDigit(text.charAt(x)))
							cmpString+=text.charAt(x);
						x++;
					}
					if(cmpString.length()>0)
					{
						final int cmpLevel=CMath.s_int(cmpString);
						vals.add(Integer.valueOf(primaryChar));
						vals.add(Integer.valueOf(cmpLevel));
						if(andEqual)
						{
							vals.add(Integer.valueOf('='));
							vals.add(Integer.valueOf(cmpLevel));
						}
					}
				}
			}
			if(vals.size()>0)
			{
				lvls=new int[vals.size()];
				int i=0;
				for(final Integer I : vals)
					lvls[i++]=I.intValue();
			}
		}
		super.setMiscText(txt);
	}

	public boolean passesMuster(final MOB mob, final Environmental R)
	{
		if(mob==null)
			return false;
		if(CMLib.flags().isATrackingMonster(mob))
			return true;
		if(CMLib.flags().isSneaking(mob)&&(!noSneak))
			return true;

		if((allFlag)
		||(text().length()==0)
		||(!(R instanceof Room))
		||(CMSecurity.isAllowed(mob,(Room)R,CMSecurity.SecFlag.GOTO)))
			return true;

		if(sysopFlag)
			return false;

		final int lvl=mob.phyStats().level();
		for(int i=0;i<lvls.length;i+=2)
		{
			switch(lvls[i])
			{
			case '=':
				if(lvl == lvls[i+1])
					return true;
				break;
			case '>':
				if(lvl > lvls[i+1])
					return true;
				break;
			case '<':
				if(lvl < lvls[i+1])
					return true;
				break;
			}
		}


		return false;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected!=null)
		&&(((msg.target() instanceof Room)&&(msg.targetMinor()==CMMsg.TYP_ENTER))
			||((msg.target() instanceof Rideable)&&(msg.targetMinor()==CMMsg.TYP_SIT)))
		&&(!CMLib.flags().isFalling(msg.source()))
		&&((msg.amITarget(affected))||(msg.tool()==affected)||(affected instanceof Area)))
		{
			final HashSet<MOB> H=new HashSet<MOB>();
			if(noFollow)
				H.add(msg.source());
			else
			{
				msg.source().getGroupMembers(H);
				final HashSet<MOB> H2=new XHashSet<MOB>(H);
				for(final Iterator<MOB> e=H2.iterator();e.hasNext();)
					e.next().getRideBuddies(H);
			}
			for(final Iterator<MOB> e=H.iterator();e.hasNext();)
			{
				final Environmental E=e.next();
				if((E instanceof MOB)
				&&(passesMuster((MOB)E,msg.target())))
					return super.okMessage(myHost,msg);
			}
			msg.source().tell(message);
			return false;
		}
		return super.okMessage(myHost,msg);
	}
}
