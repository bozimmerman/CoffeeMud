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
   Copyright 2003-2020 Bo Zimmerman

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
public class FaithHelper extends StdBehavior
{
	@Override
	public String ID()
	{
		return "FaithHelper";
	}

	protected int		num			= 999;
	protected String	msg			= null;
	protected String	deityName	= "";

	@Override
	public String accountForYourself()
	{
		if(parms.length()>0)
		{
			if(num > 0)
				return "worshippers of "+parms+" protecting";
			else
				return "worshipper of "+parms;
		}
		else
			return "worshipper protecting";
	}

	protected int canImproveCode()
	{
		return Behavior.CAN_MOBS|Behavior.CAN_AREAS|Behavior.CAN_ROOMS;
	}

	@Override
	public void startBehavior(final PhysicalAgent forMe)
	{
		super.startBehavior(forMe);
		msg=null;
		if(parms.length()>0)
		{
			deityName=parms;
			if(parms.indexOf(' ')>0)
			{
				String parms=this.parms;
				final int x=parms.toUpperCase().lastIndexOf("MSG");
				if((x>0)
				&&(parms.substring(x+3).trim().startsWith("=")))
				{
					msg=CMParms.getParmStr(parms.substring(x), "MSG", null);
					parms=parms.substring(0,x);
				}

				final List<String> V=CMParms.parse(parms);
				for(int i=V.size()-1;i>=0;i--)
				{
					if(CMath.isInteger(V.get(i)))
					{
						num=CMath.s_int(V.get(i));
						V.remove(i);
						break;
					}
				}
				deityName=CMParms.combine(V);
				if(forMe instanceof MOB)
				{
					((MOB)forMe).baseCharStats().setWorshipCharID(deityName);
					((MOB)forMe).charStats().setWorshipCharID(deityName);
				}
			}
			else
			if(forMe instanceof MOB)
			{
				((MOB)forMe).baseCharStats().setWorshipCharID(parms);
				((MOB)forMe).charStats().setWorshipCharID(parms);
			}
		}
	}

	@Override
	public void executeMsg(final Environmental affecting, final CMMsg msg)
	{
		super.executeMsg(affecting,msg);
		if((affecting instanceof Room)||(affecting instanceof Area))
		{
			if((msg.sourceMinor()==CMMsg.TYP_LIFE)
			&&(msg.source().isMonster())
			&&(!msg.source().isPlayer())
			&&(deityName.length()>0))
			{
				msg.source().baseCharStats().setWorshipCharID(deityName);
				msg.source().charStats().setWorshipCharID(deityName);
			}
			return;
		}
		else
		if((!(msg.target() instanceof MOB))
		||(!(affecting instanceof MOB)))
			return;

		final MOB source=msg.source();
		final MOB observer=(MOB)affecting;
		final MOB target=(MOB)msg.target();
		if((source!=observer)
		&&(num != 1)
		&&(CMath.bset(msg.targetMajor(),CMMsg.MASK_MALICIOUS))
		&&(!observer.isInCombat())
		&&(target!=observer)
		&&(source!=target)
		&&(observer.charStats().getWorshipCharID().length()>0)
		&&(CMLib.flags().canBeSeenBy(source,observer))
		&&(CMLib.flags().canBeSeenBy(target,observer))
		&&((!(msg.tool() instanceof DiseaseAffect))||(((DiseaseAffect)msg.tool()).isMalicious()))
		&&(!BrotherHelper.isBrother(source,observer,false)))
		{
			final Room R=source.location();
			if(observer.charStats().getWorshipCharID().equalsIgnoreCase(target.charStats().getWorshipCharID())
			&&(R!=null))
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
					String reason=(this.msg!=null)?this.msg:"THAT`S MY FRIEND!! CHARGE!!";
					if((observer.charStats().getWorshipCharID().equals(target.charStats().getWorshipCharID()))
					&&(!observer.charStats().getWorshipCharID().equals(source.charStats().getWorshipCharID())))
						reason=(this.msg!=null)?this.msg:"BELIEVERS OF "+observer.charStats().getWorshipCharID().toUpperCase()+" UNITE! CHARGE!";
					Aggressive.startFight(observer,source,true,false,reason);
				}
			}
		}
	}
}
