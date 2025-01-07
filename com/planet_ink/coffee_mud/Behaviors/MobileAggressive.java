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
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2001-2024 Bo Zimmerman

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
public class MobileAggressive extends Mobile
{
	@Override
	public String ID()
	{
		return "MobileAggressive";
	}

	protected int	tickWait	= 0;

	@Override
	public long flags()
	{
		return Behavior.FLAG_POTENTIALLYAGGRESSIVE | Behavior.FLAG_TROUBLEMAKING | super.flags();
	}

	protected boolean			mobkill				= false;
	protected boolean			misbehave			= false;
	protected String			attackMsg			= null;
	protected int				aggressiveTickDown	= 0;
	protected boolean			noGangUp			= false;
	protected boolean			levelcheck			= false;
	protected VeryAggressive	veryA				= new VeryAggressive();
	protected CompiledZMask		mask				= null;
	protected String			maskStr				= "";

	public MobileAggressive()
	{
		super();

		tickDown = 0;
		aggressiveTickDown = 0;
	}

	@Override
	public String accountForYourself()
	{
		if(maskStr.trim().length()>0)
			return "wandering aggression against "+CMLib.masking().maskDesc(maskStr,true).toLowerCase();
		else
			return "wandering aggressiveness";
	}

	@Override
	public void setParms(final String newParms)
	{
		super.setParms(newParms);
		tickWait=CMParms.getParmInt(newParms,"delay",0);
		attackMsg=CMParms.getParmStr(newParms,"MESSAGE",null);
		tickDown=tickWait;
		aggressiveTickDown=tickWait;
		final List<String> V=CMParms.parse(newParms.toUpperCase());
		levelcheck=V.remove("CHECKLEVEL");
		mobkill=V.remove("MOBKILL");
		noGangUp=V.remove("NOGANG")||V.remove("NOGANGUP");
		misbehave=V.remove("MISBEHAVE");
		V.removeAll(getMobileRemovables());
		maskStr = CMLib.masking().separateZapperMask(V);
		final Collection<String> removables = getMobileRemovables();
		this.mask=null;
		if(maskStr.length()>0)
			this.mask=CMLib.masking().getPreCompiledMask(maskStr);
		String fixedParms = newParms;
		for(int i=0;i<fixedParms.length();i++)
		{
			final char c=fixedParms.charAt(i);
			if((c=='+')||(c=='-'))
			{
				int sp = fixedParms.indexOf(' ',i);
				if(sp<0)
					sp=fixedParms.length();
				final String chk = fixedParms.substring(i,sp).toUpperCase();
				if(removables.contains(chk))
					fixedParms = fixedParms.substring(0,i)+fixedParms.substring(sp);
			}
		}
		this.veryA.setParms(fixedParms);
	}

	@Override
	public boolean grantsAggressivenessTo(final MOB M)
	{
		if(M==null)
			return true;
		return CMLib.masking().maskCheck(mask,M,false);
	}

	@Override
	public void executeMsg(final Environmental affecting, final CMMsg msg)
	{
		super.executeMsg(affecting, msg);
		veryA.executeMsg(affecting, msg);
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		tickStatus=Tickable.STATUS_MISC+0;
		super.tick(ticking,tickID);
		tickStatus=Tickable.STATUS_MISC+1;
		if(tickID!=Tickable.TICKID_MOB)
		{
			tickStatus=Tickable.STATUS_NOT;
			return true;
		}
		if((--aggressiveTickDown)<0)
		{
			aggressiveTickDown=tickWait;
			tickStatus=Tickable.STATUS_MISC+2;
			veryA.tickAggressively(ticking,tickID,mobkill,misbehave,levelcheck,this.mask,attackMsg,noGangUp);
			tickStatus=Tickable.STATUS_MISC+3;
			veryA.tickVeryAggressively(ticking,tickID,wander,mobkill,misbehave,levelcheck,this.mask,attackMsg,noGangUp);
		}
		tickStatus=Tickable.STATUS_NOT;
		return true;
	}
}
