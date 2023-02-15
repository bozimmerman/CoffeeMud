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
   Copyright 2019-2023 Bo Zimmerman

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
public class Prop_ShortEffects extends Property
{
	@Override
	public String ID()
	{
		return "Prop_ShortEffects";
	}

	@Override
	public String name()
	{
		return "Short Effects";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS|Ability.CAN_ITEMS|Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_EXITS;
	}

	@Override
	public String accountForYourself()
	{
		return "";
	}

	protected volatile int	lastNumEffects	= 0;
	protected int			maxTicks		= 3;
	protected boolean		remoteOk		= false;
	protected boolean		longTimeOk		= false;
	protected boolean		owner			= false;
	protected boolean		wearer			= false;
	protected CompiledZMask	mask			= null;

	@Override
	public void setMiscText(String newMiscText)
	{
		super.setMiscText(newMiscText);
		final int x=newMiscText.indexOf(';');
		maxTicks=3;
		mask=null;
		remoteOk = false;
		longTimeOk = false;
		if(x>0)
		{
			final List<String> parms = CMParms.parse(newMiscText.substring(0,x).trim());
			newMiscText=newMiscText.substring(x+1).trim();
			for(final String s : parms)
			{
				if(CMath.isInteger(s))
					maxTicks = CMath.s_int(s);
				else
				if(s.equalsIgnoreCase("OWNER"))
					owner=true;
				else
				if(s.equalsIgnoreCase("WEARER"))
					wearer=true;
				if(s.equalsIgnoreCase("REMOTEOK"))
					remoteOk = true;
				else
				if(s.equalsIgnoreCase("LONGTIMEOK"))
					longTimeOk = true;
			}
		}
		if(newMiscText.trim().length()>0)
			mask=CMLib.masking().getPreCompiledMask(newMiscText);
	}

	protected Set<MOB> getBadInvokers()
	{
		final Set<MOB> badInvokers = new HashSet<MOB>();
		if((affected instanceof Item)
		&&(((Item)affected).owner() instanceof MOB))
			badInvokers.add((MOB)((Item)affected).owner());
		else
		if(affected instanceof MOB)
			((MOB)affected).getGroupMembers(badInvokers);
		return badInvokers;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if ((msg.target() != null)
		&& (!remoteOk)
		&& (msg.tool() instanceof Ability )
		&& (msg.sourceMinor()!=CMMsg.TYP_TEACH)
		&& ((msg.amITarget(affected))
			||(affected instanceof Area)
			||(owner && (affected instanceof Item) && (msg.target()==((Item)affected).owner()))
			||(owner && (affected instanceof Item) && (msg.target()==((Item)affected).owner()) && (!((Item)affected).amWearingAt(Wearable.IN_INVENTORY))))
		&& ((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)!=Ability.ACODE_POISON)
		&& ((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)!=Ability.ACODE_TRAP)
		&& ((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)!=Ability.ACODE_DISEASE)
		&&(msg.source().location()!=null)
		&&((msg.source().location() != CMLib.map().roomLocation(affected))
			||((affected instanceof Area) && (CMLib.map().areaLocation(msg.source())!=affected)))
		&&(CMLib.flags().isInTheGame(msg.source(), true))
		&&((mask==null)||(CMLib.masking().maskCheck(mask, msg.tool(), true)))
		&&(!getBadInvokers().contains(msg.source())))
		{
			final Room R = msg.source().location();
			if(msg.sourceMinor()==CMMsg.TYP_CAST_SPELL)
				R.show(msg.source(), msg.target(), CMMsg.MSG_OK_VISUAL, L("<S-YOUPOSS> magic energy is disspated into the air."));
			else
				msg.source().tell(L("You can't seem to do that to @x1.",msg.target().name()));
			return false;
		}
		return super.okMessage(myHost, msg);
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(longTimeOk)
			return super.tick(ticking, tickID);
		final Physical affected = this.affected;
		if(affected != null)
		{
			if(affected.numEffects() != this.lastNumEffects)
			{
				this.lastNumEffects = affected.numEffects();
				final Set<MOB> badInvokers = getBadInvokers();
				for(final Enumeration<Ability> a=affected.effects();a.hasMoreElements();)
				{
					final Ability A=a.nextElement();
					if((A!=null)
					&&(A.canBeUninvoked())
					&&((A.invoker()==null)||(!badInvokers.contains(A.invoker())))
					&&((mask==null)||(CMLib.masking().maskCheck(mask, A, true))))
					{
						if(CMath.s_int(A.getStat("TICKDOWN"))>maxTicks)
							A.setStat("TICKDOWN", ""+maxTicks);
					}
				}
			}
		}
		return super.tick(ticking,tickID);
	}
}

