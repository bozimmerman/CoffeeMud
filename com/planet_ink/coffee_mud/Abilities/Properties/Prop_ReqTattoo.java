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
   Copyright 2002-2018 Bo Zimmerman

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
public class Prop_ReqTattoo extends Property implements TriggeredAffect
{
	@Override
	public String ID()
	{
		return "Prop_ReqTattoo";
	}

	@Override
	public String name()
	{
		return "Tattoo Limitations";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_EXITS|Ability.CAN_ITEMS;
	}

	private String themsg="";

	@Override
	public long flags()
	{
		return Ability.FLAG_ZAPPER;
	}

	@Override
	public String accountForYourself()
	{
		return "Ownership restricted as follows: "+CMLib.masking().maskDesc(text());
	}

	@Override
	public int triggerMask()
	{
		return TriggeredAffect.TRIGGER_ENTER;
	}

	@Override
	public String text()
	{
		return themsg+";"+super.text();
	}

	@Override
	public void setMiscText(String newText)
	{
		themsg="";
		final int x=newText.indexOf(';');
		if(x<0)
			super.setMiscText(newText);
		else
		if(newText.substring(0,x).indexOf('+')>=0)
			super.setMiscText(newText);
		else
		if(newText.substring(0,x).indexOf('-')>=0)
			super.setMiscText(newText);
		else
		{
			themsg=newText.substring(0,x).trim();
			super.setMiscText(newText.substring(x+1));
		}
	}

	public Vector<String> getMask(boolean[] flags)
	{
		final Vector<String> V=CMParms.parse(miscText.toUpperCase());
		String s=null;
		for(int v=V.size()-1;v>=1;v--)
		{
			s=V.elementAt(v);
			if(s.startsWith("NOFOL"))
			{
				flags[0]=true;
				V.removeElementAt(v);
			}
			else
			if(s.startsWith("NOSNEAK"))
			{
				flags[1]=true;
				V.removeElementAt(v);
			}
			else
			if("+-".indexOf(s.charAt(0))<0)
			{
				V.removeElementAt(v);
				V.setElementAt((V.elementAt(v-1))+" "+s,v-1);
			}
		}
		return V;
	}

	public boolean passesMuster(Vector<String> mask, boolean[] flags, MOB mob)
	{
		if(mob==null)
			return false;
		if(CMLib.flags().isATrackingMonster(mob))
			return true;
		if(CMLib.flags().isSneaking(mob)&&(flags[1]))
			return true;
		int allFlag=0;
		String s=null;
		for(int v=0;v<mask.size();v++)
		{
			s=mask.elementAt(v);
			if(s.equals("+ALL"))
				allFlag=1;
			else
			if(s.equals("+NONE"))
				allFlag=0;
			else
			if(s.equals("-ALL"))
				allFlag=-1;
			else
			if(s.startsWith("+")||s.startsWith("-"))
			{
				final char c=s.charAt(0);
				final String tattooName;
				if((c=='+')||(c=='-'))
					tattooName = s.substring(1);
				else
					tattooName = s;
				final boolean found;
				if(tattooName.toLowerCase().startsWith("account ")
				&&(mob.playerStats()!=null)
				&&(mob.playerStats().getAccount()!=null))
					found=mob.playerStats().getAccount().findTattoo(tattooName.substring(8).trim())!=null;
				else
					found=mob.findTattoo(tattooName)!=null;
				switch(allFlag)
				{
				case 0: // +NONE -- HAS/LACKS ALL
					if(c=='-')
					{
						if(found)
							return false;
					}
					else
					if(!found)
						return false;
					break;
				case 1: // +ALL -- LACKS ANY
					if(c!='+') // ----------------
					{
						if(!found)
							return true;
					}
					else
					if(found)
						return true;
					break;
				case -1: // -ALL -- HAS ANY
					if(c!='-') // ++++++++++++++++++++
					{
						if(found)
							return true;
					}
					else
					if(found)
						return false;
					break;
				}
			}
		}
		if(allFlag<0) return false; // if not returned, does not have any of them
		if(allFlag==0) return true; // none were missing, so its all good.
		if(allFlag>0) return true; // all were missing, so its all good.
		return true;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected!=null)
		&&(msg.target()!=null)
		&&(!CMLib.flags().isFalling(msg.source()))
		&&((msg.amITarget(affected))||(msg.tool()==affected)||(affected instanceof Area)))
		{
			if(((msg.target() instanceof Room)&&(msg.targetMinor()==CMMsg.TYP_ENTER))
			||((msg.target() instanceof Item)&&((msg.targetMinor()==CMMsg.TYP_GET)||(msg.targetMinor()==CMMsg.TYP_SIT))))
			{
				final boolean[] flags=new boolean[2];
				final Vector<String> V=getMask(flags);
				final HashSet<MOB> H=new HashSet<MOB>();
				if(flags[0])
					H.add(msg.source());
				else
				{
					msg.source().getGroupMembers(H);
					final HashSet<MOB> H2=new XHashSet(H);
					for(final Iterator e=H2.iterator();e.hasNext();)
						((MOB)e.next()).getRideBuddies(H);
				}
				for(final Iterator<MOB> e=H.iterator();e.hasNext();)
				{
					final MOB E=e.next();
					if(passesMuster(V,flags,E))
						return super.okMessage(myHost,msg);
				}
				if(msg.target() instanceof Room)
					msg.source().tell(themsg.length()==0?L("You have not been granted authorization to go that way."):themsg);
				else
				if(msg.source().location()!=null)
					msg.source().location().show(msg.source(),null,affected,CMMsg.MSG_OK_ACTION,themsg.length()==0?L("<O-NAME> flashes and flies out of <S-HIS-HER> hands!"):themsg);
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}
}
