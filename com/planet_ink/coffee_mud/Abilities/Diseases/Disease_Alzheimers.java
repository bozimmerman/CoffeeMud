package com.planet_ink.coffee_mud.Abilities.Diseases;
import java.util.Vector;

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

/*
   Copyright 2006-2018 Bo Zimmerman

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

public class Disease_Alzheimers extends Disease
{
	@Override
	public String ID()
	{
		return "Disease_Alzheimers";
	}

	private final static String localizedName = CMLib.lang().L("Alzheimers");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Alzheimers)");

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
	public int difficultyLevel()
	{
		return 9;
	}

	@Override
	protected int DISEASE_TICKS()
	{
		return 34;
	}

	@Override
	protected int DISEASE_DELAY()
	{
		return 5;
	}

	@Override
	protected String DISEASE_DONE()
	{
		return L("Your alzheimers is cured!");
	}

	@Override
	protected String DISEASE_START()
	{
		return L("^G<S-NAME> feel(s) like <S-HE-SHE> <S-HAS-HAVE> forgotten something.^?");
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

	protected int everyTick=0;

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!(affected instanceof MOB))
			return super.okMessage(myHost,msg);

		final MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if((msg.amISource(mob))
		&&(msg.tool() instanceof Ability)
		&&(mob.fetchAbility(msg.tool().ID())==msg.tool())
		&&(CMLib.dice().rollPercentage()>(mob.charStats().getSave(CharStats.STAT_SAVE_MIND))))
		{
			mob.tell(L("You can't remember @x1!",msg.tool().name()));
			return false;
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(affected==null)
			return false;
		if(((everyTick>0)||(CMLib.dice().roll(1,150,0)==1))
		&&(affected instanceof MOB))
		{
			if(everyTick<=0)
				everyTick=CMLib.dice().roll(1,20,0);
			else
			if((--everyTick)<=0)
				return true;
			final MOB mob=(MOB)affected;
			if(CMLib.flags().isAliveAwakeMobileUnbound(mob,true))
			{
				if(!CMLib.flags().isStanding(mob))
					CMLib.commands().postStand(mob,true);
				final Room R=mob.location();
				if((CMLib.flags().isStanding(mob))&&(R!=null)&&(CMLib.flags().isInTheGame(mob,true)))
				{
					final Vector<Integer> dirs=new Vector<Integer>();
					for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
					{
						if((R.getRoomInDir(d)!=null)
						&&(R.getExitInDir(d)!=null)
						&&(R.getExitInDir(d).isOpen()))
							dirs.addElement(Integer.valueOf(d));
					}
					if(dirs.size()==0)
						everyTick=0;
					else
					{
						final int dir=dirs.elementAt(CMLib.dice().roll(1,dirs.size(),-1)).intValue();
						CMLib.tracking().walk(mob,dir,false,false,false);
					}
				}
			}
		}
		return true;
	}

	@Override
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		if(affected==null)
			return;
		affectableStats.setStat(CharStats.STAT_INTELLIGENCE,affectableStats.getStat(CharStats.STAT_INTELLIGENCE)/2);
		if(affectableStats.getStat(CharStats.STAT_INTELLIGENCE)<=0)
			affectableStats.setStat(CharStats.STAT_INTELLIGENCE,1);
		affectableStats.setStat(CharStats.STAT_WISDOM,affectableStats.getStat(CharStats.STAT_WISDOM)/2);
		affectableStats.setStat(CharStats.STAT_CHARISMA,affectableStats.getStat(CharStats.STAT_CHARISMA)+2);
		if(affectableStats.getStat(CharStats.STAT_WISDOM)<=0)
			affectableStats.setStat(CharStats.STAT_WISDOM,1);
	}
}
