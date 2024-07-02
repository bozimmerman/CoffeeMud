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
public class CorpseEater extends ActiveTicker
{
	@Override
	public String ID()
	{
		return "CorpseEater";
	}

	@Override
	protected int canImproveCode()
	{
		return Behavior.CAN_MOBS;
	}

	private boolean eatItems=false;
	private boolean eatPlayers=false;
	private boolean eatMobs=true;
	private String maskStr = "";
	private MaskingLibrary.CompiledZMask mask = null;

	public CorpseEater()
	{
		super();
		minTicks=5; maxTicks=20; chance=75;
		tickReset();
	}

	@Override
	public String accountForYourself()
	{
		return "corpse eating";
	}

	@Override
	public void setParms(final String newParms)
	{
		super.setParms(newParms);
		final List<String> ps = CMParms.parse(newParms);
		eatItems=false;
		eatPlayers=false;
		eatMobs=true;
		for(int p = ps.size()-1;p>=0;p--)
		{
			final String s = ps.get(p).toUpperCase();
			if(s.equals("EATITEMS"))
			{
				eatItems=true;
				ps.remove(p);
			}
			else
			if(s.equals("+PLAYER"))
			{
				eatPlayers=true;
				ps.remove(p);
			}
			else
			if(s.equals("-NPC")||s.equals("-MOB"))
			{
				eatMobs=false;
				ps.remove(p);
			}
		}
		this.maskStr = CMParms.combineQuoted(ps, 0).trim();
		this.mask = null;
		if(this.maskStr.length()>0)
			this.mask = CMLib.masking().maskCompile(this.maskStr);
	}

	public static MOB makeMOBfromCorpse(final DeadBody corpse, String type)
	{
		if((type==null)||(type.length()==0))
			type="StdMOB";
		final MOB mob=CMClass.getMOB(type);
		if(corpse!=null)
		{
			mob.setName(corpse.name());
			mob.setDisplayText(corpse.displayText());
			mob.setDescription(corpse.description());
			mob.setBaseCharStats((CharStats)corpse.charStats().copyOf());
			mob.setBasePhyStats((PhyStats)corpse.basePhyStats().copyOf());
			mob.recoverCharStats();
			mob.recoverPhyStats();
			mob.baseState().setHitPoints(CMLib.leveler().getLevelHitPoints(mob));
			mob.baseState().setMana(CMLib.leveler().getLevelMana(mob));
			mob.baseState().setMovement(CMLib.leveler().getLevelMove(mob));
			mob.recoverMaxState();
			mob.resetToMaxState();
			mob.baseCharStats().getMyRace().startRacing(mob,false);
		}
		return mob;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		super.tick(ticking,tickID);
		if((canAct(ticking,tickID))&&(ticking instanceof MOB))
		{
			final MOB mob=(MOB)ticking;
			final Room thisRoom=mob.location();
			if(thisRoom.numItems()==0)
				return true;
			for(int i=0;i<thisRoom.numItems();i++)
			{
				final Item I=thisRoom.getItem(i);
				if((I instanceof DeadBody)
				&&(CMLib.flags().canBeSeenBy(I,mob)||CMLib.flags().canSmell(mob)))
				{
					if(((DeadBody)I).isPlayerCorpse())
					{
						if(!eatPlayers)
							continue;
					}
					else
					if(!eatMobs)
						continue;
					if((maskStr.length()>0) || (mask != null))
					{
						final MOB mob2=makeMOBfromCorpse((DeadBody)I,null);
						if(!CMLib.masking().maskCheck(mask,mob2,false))
						{
							mob2.destroy();
							continue;
						}
						mob2.destroy();
					}

					if((I instanceof Container)&&(!eatItems))
						((Container)I).emptyPlease(false);
					thisRoom.show(mob,null,I,CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> eat(s) <O-NAME>."));
					I.destroy();
					return true;
				}
			}
		}
		return true;
	}
}
