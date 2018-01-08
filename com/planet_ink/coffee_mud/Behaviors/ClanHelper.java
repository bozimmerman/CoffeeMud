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
   Copyright 2001-2018 Bo Zimmerman

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

	@Override
	public String accountForYourself()
	{
		if(parms.length()>0)
			return "fellow '"+parms+"' protecting";
		else
			return "fellow clan members protecting";
	}

	protected boolean mobKiller=false;

	@Override
	public void startBehavior(PhysicalAgent forMe)
	{
		super.startBehavior(forMe);
		if(forMe instanceof MOB)
		{
			if(parms.length()>0)
			{
				Clan C=CMLib.clans().getClan(parms.trim());
				if(C==null)
					C=CMLib.clans().findClan(parms.trim());
				if(C!=null)
					((MOB)forMe).setClan(C.clanID(),C.getGovernment().getAcceptPos());
				else
					Log.errOut("ClanHelper","Unknown clan "+parms+" for "+forMe.Name()+" in "+CMLib.map().getDescriptiveExtendedRoomID(CMLib.map().roomLocation(forMe)));
			}
		}
	}

	@Override
	public void executeMsg(Environmental affecting, CMMsg msg)
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
				Clan C=null;
				for(final Triad<Clan,Integer,Integer> t : list)
				{
					if(source.getClanRole(t.first.clanID())==null)
					{
						C=t.first;
						break;
					}
				}
				String reason="WE ARE UNDER ATTACK!! CHARGE!!";
				if(C!=null)
					reason=C.getName().toUpperCase()+"S UNITE! CHARGE!";
				Aggressive.startFight(observer,source,true,false,reason);
			}
		}
	}
}
