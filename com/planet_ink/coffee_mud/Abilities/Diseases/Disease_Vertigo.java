package com.planet_ink.coffee_mud.Abilities.Diseases;
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
   Copyright 2019-2020 Bo Zimmerman

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
public class Disease_Vertigo extends Disease
{
	@Override
	public String ID()
	{
		return "Disease_Vertigo";
	}

	private final static String localizedName = CMLib.lang().L("Vertigo");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Vertigo)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public boolean putInCommandlist()
	{
		return false;
	}

	@Override
	protected int DISEASE_TICKS()
	{
		return CMLib.dice().roll(4, 9, 4);
	}

	@Override
	protected int DISEASE_DELAY()
	{
		return 1;
	}

	@Override
	protected String DISEASE_DONE()
	{
		return L("Your balance is restored.");
	}

	@Override
	protected String DISEASE_START()
	{
		return L("^G<S-NAME> start(s) suffering vertigo.^?");
	}

	@Override
	protected String DISEASE_AFFECT()
	{
		return "";
	}

	@Override
	public int abilityCode()
	{
		return 0;
	}

	@Override
	public int difficultyLevel()
	{
		return 3;
	}

	@Override
	public int spreadBitmap()
	{
		return 0;
	}

	protected volatile int fallDown = 0;

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(fallDown > 0)
			affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_SITTING);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(msg.source()==affected)
		{
			if((fallDown>0)
			&&(msg.sourceMinor()==CMMsg.TYP_STAND))
				return false;
			else
			if((msg.sourceMinor()==CMMsg.TYP_ENTER)
			&&(CMLib.dice().rollPercentage()<8)
			&&(msg.tool() instanceof Exit)
			&&(msg.target() instanceof Room))
			{
				final Room R=msg.source().location();
				final int dir=CMLib.map().getRoomDir(R, (Room)msg.target());
				if((dir >= 0)
				&&(R.getExitInDir(dir) == msg.tool()))
				{
					int newDir = -1;
					for(int i=0;(i<100) && (newDir < 0);i++)
					{
						final int possDir = CMLib.dice().roll(1, Directions.NUM_DIRECTIONS(), -1);
						if((possDir != dir)
						&&(R.getRoomInDir(possDir)!=null)
						&&(R.getExitInDir(possDir)!=null))
							newDir = possDir;
					}
					if(newDir >= 0)
					{
						msg.setTarget(R.getRoomInDir(newDir));
						msg.setTool(R.getExitInDir(newDir));
						return msg.target().okMessage(myHost, msg);
					}
				}
			}
		}
		return true;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(affected==null)
			return false;
		if(!(affected instanceof MOB))
			return true;

		final MOB mob=(MOB)affected;
		if((!mob.amDead())&&((--diseaseTick)<=0))
		{
			diseaseTick=DISEASE_DELAY();
			if(fallDown > 0)
			{
				if(--fallDown == 0)
					CMLib.commands().postStand(mob, true, false);
			}
			if(CMLib.dice().rollPercentage()<6)
			{
				if(CMLib.flags().isStanding(mob))
				{
					final CMMsg msg=CMClass.getMsg(mob,mob,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|CMMsg.MASK_ALWAYS,L("<T-NAME> feel(s) dizzy..."));
					if(mob.location().okMessage(mob,msg))
					{
						mob.location().send(mob,msg);
						if(msg.value()<=0)
						{
							mob.location().show(mob, null, CMMsg.MSG_OK_ACTION, L("<S-NAME> fall(s) down!"));
							fallDown=3;
							mob.recoverPhyStats();
						}
					}
				}
			}
			mob.recoverPhyStats();
			return true;
		}
		return true;
	}
}
