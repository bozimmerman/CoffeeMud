package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.PlanarAbility.PlanarVar;
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
   Copyright 2004-2020 Bo Zimmerman

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
public class Fighter_CosmicAdaptation extends FighterSkill
{
	@Override
	public String ID()
	{
		return "Fighter_CosmicAdaptation";
	}

	private final static String localizedName = CMLib.lang().L("Cosmic Adaptation");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_SELF;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_COSMOLOGY;
	}

	@Override
	public boolean isAutoInvoked()
	{
		return true;
	}

	@Override
	public boolean canBeUninvoked()
	{
		return false;
	}

	protected volatile boolean	activated	= false;
	protected volatile boolean	inPlane		= false;
	protected volatile Area		lastArea	= null;
	protected int[]				lastSet		= null;
	protected int[]				newSet		= null;
	protected int[]				fixSet		= null;

	protected boolean appliesHere(final Room R)
	{
		if(R==null)
			return false;
		final Area lastArea;
		synchronized(this)
		{
			lastArea=this.lastArea;
		}
		if(R.getArea() != lastArea)
		{
			if(text().length()==0)
				inPlane = CMLib.flags().getPlaneOfExistence(R.getArea()) != null;
			else
				inPlane = text().equalsIgnoreCase(CMLib.flags().getPlaneOfExistence(R.getArea()));
			this.lastArea=R.getArea();
		}
		return inPlane;
	}


	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((affected!=null)&&(affected instanceof MOB))
		{
			final MOB mob;
			synchronized(this)
			{
				mob=(MOB)affected;
			}
			if(appliesHere(mob.location()))
			{
				final boolean success = this.proficiencyCheck(null, 0, false);
				if(success)
				{
					if(!activated)
					{
						activated=true;
						lastSet = null;
						newSet = null;
						fixSet = null;
						mob.recoverCharStats();
					}
					helpProficiency(mob, 0);
				}
				else
				if(activated)
				{
					activated=false;
					lastSet = null;
					newSet = null;
					fixSet = null;
					mob.recoverCharStats();
				}
			}
			else
			if(activated)
			{
				activated=false;
				lastSet = null;
				newSet = null;
				fixSet = null;
				mob.recoverCharStats();
			}
		}
		return true;
	}

	protected int[] getBreatheSet(final MOB mob)
	{
		int[] fixSet = new int[0];
		final Room R=CMLib.map().roomLocation(mob);
		if(R==null)
			return fixSet;
		final String planeName = CMLib.flags().getPlaneOfExistence(R);
		if(planeName != null)
		{
			final PlanarAbility planeA=(PlanarAbility)CMClass.getAbility("StdPlanarAbility");
			final Map<String,String> planeVars = planeA.getPlanarVars(planeName);
			final String atmosphere = planeVars.get(PlanarVar.ATMOSPHERE.toString());
			if(atmosphere!=null)
			{
				if(atmosphere.length()>0)
				{
					final int atmo=RawMaterial.CODES.FIND_IgnoreCase(atmosphere);
					if(atmo > 0)
						fixSet = new int[] {atmo};
				}
			}
		}
		return fixSet;
	}

	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		synchronized(this)
		{
			if(!activated)
				return;
		}
		final int[] breatheables=affectableStats.getBreathables();
		if(breatheables.length==0)
			return;
		if((lastSet!=breatheables)||(newSet==null))
		{
			if(fixSet == null)
				fixSet = getBreatheSet(affected);
			if(fixSet.length>0)
			{
				newSet=Arrays.copyOf(affectableStats.getBreathables(),affectableStats.getBreathables().length+fixSet.length);
				for(int i=0;i<fixSet.length;i++)
					newSet[newSet.length-1-i]=fixSet[i];
				Arrays.sort(newSet);
			}
			else
				newSet=breatheables;
			lastSet=breatheables;
		}
		affectableStats.setBreathables(newSet);
	}
}
