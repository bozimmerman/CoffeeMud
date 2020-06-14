package com.planet_ink.coffee_mud.Abilities.Paladin;
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
   Copyright 2020-2020 Bo Zimmerman

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
public class Paladin_PlanarDefiance extends PaladinSkill
{
	@Override
	public String ID()
	{
		return "Paladin_PlanarDefiance";
	}

	private final static String localizedName = CMLib.lang().L("Planar Defiance");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_COSMOLOGY;
	}

	protected volatile Area lastArea = null;
	protected volatile boolean inPlane = false;
	protected volatile String defiance = null;
	protected final List<MOB> oldSet = new Vector<MOB>();

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(ticking == invoker())
		{
			synchronized(oldSet)
			{
				final String defiance=this.defiance;
				if((defiance == null)
				||(defiance.length()==0))
				{
					for(final Iterator<MOB> i = oldSet.iterator();i.hasNext();)
					{
						final MOB M=i.next();
						if(!this.paladinsGroup.contains(M))
						{
							M.delEffect(this);
							this.affected = invoker();
							M.recoverPhyStats();
							i.remove();
						}
					}
				}
				else
				{
					if(this.paladinsGroup != null)
					{
						for(final Iterator<MOB> i = oldSet.iterator();i.hasNext();)
						{
							final MOB M=i.next();
							if(!this.paladinsGroup.contains(M))
							{
								M.delEffect(this);
								this.affected = invoker();
								M.recoverPhyStats();
								i.remove();
							}
						}
						for(final Iterator<MOB> i=paladinsGroup.iterator();i.hasNext();)
						{
							final MOB M=i.next();
							if((M!=invoker())
							&&(!oldSet.contains(M))
							&&(M.location()==invoker().location()))
							{
								M.delEffect(this);
								this.affected = invoker();
								oldSet.add(M);
							}
						}
					}
				}
			}
		}
		else
		if(((defiance==null)||(!CMLib.flags().isInTheGame(invoker(), true)))
		&&(ticking instanceof MOB))
		{
			((MOB)ticking).delEffect(this);
			this.affected = invoker();
			((MOB)ticking).recoverPhyStats();
		}
		return true;
	}

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
				this.inPlane = CMLib.flags().getPlaneOfExistence(R.getArea()) != null;
			else
				this.inPlane = text().equalsIgnoreCase(CMLib.flags().getPlaneOfExistence(R.getArea()));
			this.lastArea=R.getArea();
		}
		return inPlane;

	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;
			if(appliesHere(mob.location()))
			{
				String defiance;
				synchronized(this)
				{
					defiance=this.defiance;
				}
				if(defiance == null)
				{
					final Area planeA = (mob.location()!=null) ? mob.location().getArea() : null;
					if(planeA != null)
					{
						final Ability A=planeA.fetchEffect("Prop_AbsorbDamage");
						if(A==null)
							defiance="";
						else
							defiance="-"+A;
					}
					this.defiance=defiance;
				}
				if((defiance!=null)&&(defiance.length()>0))
					affectableStats.addAmbiance(defiance);
			}
			else
				defiance=null;
		}
	}
}
