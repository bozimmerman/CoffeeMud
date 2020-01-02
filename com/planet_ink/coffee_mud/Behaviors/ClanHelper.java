package com.planet_ink.coffee_mud.Behaviors;
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
   Copyright 2001-2020 Bo Zimmerman

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
public class ClanHelper extends StdBehavior
{
	@Override
	public String ID()
	{
		return "ClanHelper";
	}

	protected boolean	mobKiller	= false;
	protected int		num			= 999;
	protected String	clanName	= "";
	protected String	msg			= null;

	@Override
	public String accountForYourself()
	{
		if(clanName.length()>0)
			return "fellow '"+clanName+"' protecting";
		else
			return "fellow clan members protecting";
	}

	@Override
	public void startBehavior(final PhysicalAgent forMe)
	{
		super.startBehavior(forMe);
		num=999;
		if(forMe instanceof MOB)
		{
			String clanName=parms.trim();
			final int x=clanName.lastIndexOf(' ');
			if(x>0)
			{
				msg=null;
				String s=clanName.substring(x+1).trim();
				final int y=s.toUpperCase().lastIndexOf("MSG");
				if((y>0)
				&&(s.substring(y+3).trim().startsWith("=")))
				{
					msg=CMParms.getParmStr(s.substring(y), "MSG", null);
					s=s.substring(0,y);
				}
				final List<String> V=CMParms.parse(s);
				for(int i=V.size()-1;i>=0;i--)
				{
					if(CMath.isInteger(V.get(i)))
					{
						num=CMath.s_int(V.get(i));
						V.remove(i);
						break;
					}
				}
				clanName=CMParms.combine(V);
			}
			if(clanName.length()>0)
			{
				this.clanName=clanName;
				Clan C=CMLib.clans().getClan(clanName.trim());
				if(C==null)
					C=CMLib.clans().findClan(clanName.trim());
				if(C!=null)
					((MOB)forMe).setClan(C.clanID(),C.getGovernment().getAcceptPos());
				else
					Log.errOut("ClanHelper","Unknown clan "+clanName+" for "+forMe.Name()+" in "+CMLib.map().getDescriptiveExtendedRoomID(CMLib.map().roomLocation(forMe)));
			}
		}
	}

	@Override
	public void executeMsg(final Environmental affecting, final CMMsg msg)
	{
		super.executeMsg(affecting,msg);
		if((msg.target()==null)||(!(msg.target() instanceof MOB)))
			return;
		final MOB source=msg.source();
		final MOB observer=(MOB)affecting;
		final MOB target=(MOB)msg.target();

		if((target==null)||(observer==null))
			return;
		if((source!=observer)
		&&(CMath.bset(msg.targetMajor(),CMMsg.MASK_MALICIOUS))
		&&(target!=observer)
		&&(source!=target)
		&&(!observer.isInCombat())
		&&(CMLib.flags().canBeSeenBy(source,observer))
		&&(CMLib.flags().canBeSeenBy(target,observer))
		&&((!(msg.tool() instanceof DiseaseAffect))||(((DiseaseAffect)msg.tool()).isMalicious()))
		&&(!BrotherHelper.isBrother(source,observer,false)))
		{
			final List<Triad<Clan,Integer,Integer>> list=CMLib.clans().findCommonRivalrousClans(observer, target);
			if(list.size()>0)
			{
				final Room R=source.location();
				if(R!=null)
				{
					int numInFray=0;
					if((num > 0) && (num < 999))
					{
						for(int m=0;m<R.numInhabitants();m++)
						{
							final MOB M=R.fetchInhabitant(m);
							if((M!=null)&&(M.getVictim()==source))
								numInFray++;
						}
					}
					if(((num==0)||(numInFray<num)))
					{
						Clan C=null;
						for(final Triad<Clan,Integer,Integer> t : list)
						{
							if(source.getClanRole(t.first.clanID())==null)
							{
								C=t.first;
								break;
							}
						}
						String reason=(this.msg!=null)?this.msg:"WE ARE UNDER ATTACK!! CHARGE!!";
						if(C!=null)
							reason=(this.msg!=null)?this.msg:C.getName().toUpperCase()+"S UNITE! CHARGE!";
						Aggressive.startFight(observer,source,true,false,reason);
					}
				}
			}
		}
	}
}
