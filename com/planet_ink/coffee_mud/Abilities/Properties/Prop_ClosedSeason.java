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
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2004-2025 Bo Zimmerman

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
public class Prop_ClosedSeason extends Property
{
	@Override
	public String ID()
	{
		return "Prop_ClosedSeason";
	}

	@Override
	public String name()
	{
		return "Contingent Visibility";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ITEMS|Ability.CAN_MOBS|Ability.CAN_EXITS|Ability.CAN_ROOMS;
	}

	protected Vector<String>	closedV		= null;
	boolean						doneToday	= false;
	private Area				exitArea	= null;
	protected CompiledZMask		mask		= null;

	@Override
	public String accountForYourself()
	{
		return "";
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_ADJUSTER;
	}

	@Override
	public void setMiscText(String text)
	{
		super.setMiscText(text);
		int x=text.toUpperCase().lastIndexOf("MASK=");
		if(x<0)
			x=text.toUpperCase().lastIndexOf("MASK =");
		if(x>0)
		{
			String mask=text.substring(text.indexOf("=",x+1)+1).trim();
			if(mask.startsWith("\"")&&(mask.endsWith("\"")))
				mask = CMStrings.deEscape(mask.substring(1,mask.length()-1)).trim();
			text=text.substring(0,x);
			this.mask = CMLib.masking().getPreCompiledMask(mask);
		}
		else
			this.mask = null;
		closedV=CMParms.parse(text.toUpperCase());
	}

	@Override
	public void executeMsg(final Environmental host, final CMMsg msg)
	{
		super.executeMsg(host,msg);
		if(exitArea!=null)
			return;
		if(!(affected instanceof Exit))
			return;
		if(msg.source().location()!=null)
			exitArea=msg.source().location().getArea();
	}

	protected boolean closed(final Physical P, final Area A)
	{
		if((A==null)||(P==null))
			return false;

		for(final Room.VariationCode code : Room.VariationCode.values())
		{
			if(closedV.contains(code.toString()))
				switch(code.c)
				{
				case 'W':
					if(A.getClimateObj().weatherType(null)==code.num)
					{
						if((this.mask != null)
						&& (!CMLib.masking().maskCheck(mask, P, true)))
							return false;
						return true;
					}
					break;
				case 'C':
					if(A.getTimeObj().getTODCode().ordinal()==code.num)
					{
						if((this.mask != null)
						&& (!CMLib.masking().maskCheck(mask, P, true)))
							return false;
						return true;
					}
					break;
				case 'S':
					if(A.getTimeObj().getSeasonCode().ordinal()==code.num)
					{
						if((this.mask != null)
						&& (!CMLib.masking().maskCheck(mask, P, true)))
							return false;
						return true;
					}
					break;
				}
		}
		return false;
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		if(affected==null)
			return;
		if((affected instanceof MOB)||(affected instanceof Item))
		{
			final Room R=CMLib.map().roomLocation(affected);
			if((R!=null)
			&&(closed(affected,R.getArea()))
			&&((!(affected instanceof MOB))||(!((MOB)affected).isInCombat())))
			{
				affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_NOT_SEEN);
				affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_SEE);
				affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_MOVE);
				affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_SPEAK);
				affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_HEAR);
			}
		}
		else
		if((affected instanceof Room)&&(closed(affected,((Room)affected).getArea())))
			affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_DARK);
		else
		if(affected instanceof Exit)
		{
			if(closed(affected,exitArea==null?CMLib.map().getFirstArea():exitArea))
			{
				if(!doneToday)
				{
					doneToday=true;
					final Exit e=((Exit)affected);
					e.setDoorsNLocks(e.hasADoor(),false,e.defaultsClosed(),e.hasALock(),e.hasALock(),e.defaultsLocked());
				}
			}
			else
			{
				if(doneToday)
				{
					doneToday=false;
					final Exit e=((Exit)affected);
					e.setDoorsNLocks(e.hasADoor(),!e.defaultsClosed(),e.defaultsClosed(),e.hasALock(),e.defaultsLocked(),e.defaultsLocked());
				}
			}
		}

	}
}
