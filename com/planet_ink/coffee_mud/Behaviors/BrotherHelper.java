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
public class BrotherHelper extends StdBehavior
{
	@Override
	public String ID()
	{
		return "BrotherHelper";
	}

	//protected boolean mobKiller=false;
	protected boolean	nameOnly	= true;
	protected int		num			= -1;

	@Override
	public String accountForYourself()
	{
		return "neighbor protecting";
	}

	@Override
	public void setParms(String parms)
	{
		super.setParms(parms);
		num=0;
		nameOnly=false;
		if(parms != null)
		{
			final List<String> V=CMParms.parse(parms.toUpperCase());
			nameOnly=V.contains("NAMEONLY");
			for(String s : V)
			{
				if(CMath.isInteger(s))
					num=CMath.s_int(s);
			}
		}
	}

	public static boolean isBrother(MOB target, MOB observer, boolean nameOnly)
	{
		if((observer==null)||(target==null))
			return false;
		if(!nameOnly)
		{
			if((observer.getStartRoom()!=null)&&(target.getStartRoom()!=null))
			{
				if (observer.getStartRoom() == target.getStartRoom())
					return true;
			}
		}
		if((observer.ID().equals(target.ID()))&&(observer.name().equals(target.name())))
			return true;
		return false;
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

		final Room R=source.location();
		if((source!=observer)
		&&(target!=observer)
		&&(source!=target)
		&&(CMath.bset(msg.targetMajor(),CMMsg.MASK_MALICIOUS))
		&&(!observer.isInCombat())
		&&(CMLib.flags().canBeSeenBy(source,observer))
		&&(CMLib.flags().canBeSeenBy(target,observer))
		&&(isBrother(target,observer,nameOnly))
		&&(!isBrother(source,observer,nameOnly))
		&&((!(msg.tool() instanceof DiseaseAffect))||(((DiseaseAffect)msg.tool()).isMalicious()))
		&&(R!=null))
		{
			int numInFray=0;
			for(int m=0;m<R.numInhabitants();m++)
			{
				final MOB M=R.fetchInhabitant(m);
				if((M!=null)&&(M.getVictim()==source))
					numInFray++;
			}
			boolean yep=true;
			if(CMLib.law().isLegalOfficerHere(observer))
			{
				yep=false;
				if(CMLib.law().isLegalOfficialHere(target))
					yep=true;
				else
				if(!CMLib.flags().isAggressiveTo(target,source))
					yep=true;
			}
			if(yep&&((num==0)||(numInFray<num)))
			{
				yep=Aggressive.startFight(observer,source,true,false,"DON'T HURT MY FRIEND!");
			}
		}
	}

}
