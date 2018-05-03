package com.planet_ink.coffee_mud.Abilities.Thief;
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
   Copyright 2003-2018 Bo Zimmerman

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

public class Thief_SenseLaw extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_SenseLaw";
	}

	private final static String localizedName = CMLib.lang().L("Sense Law");

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
		return Ability.QUALITY_OK_SELF;
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

	public static final Vector<MOB> empty=new ReadOnlyVector<MOB>();
	protected Room oldroom=null;
	protected String lastReport="";

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_STREETSMARTS;
	}

	public Vector<MOB> getLawMen(Area legalObject, Room room, LegalBehavior B)
	{
		if(room==null)
			return empty;
		if(room.numInhabitants()==0)
			return empty;
		if(B==null)
			return empty;
		final Vector<MOB> V=new Vector<MOB>();
		for(int m=0;m<room.numInhabitants();m++)
		{
			final MOB M=room.fetchInhabitant(m);
			if((M!=null)&&(M.isMonster())&&(B.isElligibleOfficer(legalObject,M)))
				V.addElement(M);
		}
		return V;
	}

	public boolean findLaw(Room R, int depth, int maxDepth)
	{
		return true;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;
			if((mob.location()!=null)&&(!mob.isMonster()))
			{
				final LegalBehavior B=CMLib.law().getLegalBehavior(mob.location());
				if(B==null)
					return super.tick(ticking,tickID);
				final StringBuffer buf=new StringBuffer("");
				Vector<MOB> V=getLawMen(CMLib.law().getLegalObject(mob.location()),mob.location(),B);
				for(int l=0;l<V.size();l++)
				{
					final MOB M=V.elementAt(l);
					if(CMLib.flags().canBeSeenBy(M,mob))
						buf.append(L("@x1 is an officer of the law.  ",M.name(mob)));
					else
						buf.append(L("There is an officer of the law here.  "));
				}
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					final Room R=mob.location().getRoomInDir(d);
					final Exit E=mob.location().getExitInDir(d);
					if((R!=null)&&(E!=null)&&(E.isOpen()))
					{
						V=getLawMen(mob.location().getArea(),R,B);
						if((V!=null)&&(V.size()>0))
							buf.append(L("There is an officer of the law @x1.  ",CMLib.directions().getInDirectionName(d)));
					}
				}
				if((buf.length()>0)
				&&((mob.location()!=oldroom)||(!buf.toString().equals(lastReport)))
				&&((mob.fetchAbility(ID())==null)||proficiencyCheck(mob,0,false)))
				{
					mob.tell(L("You sense: @x1",buf.toString()));
					oldroom=mob.location();
					helpProficiency(mob, 0);
					lastReport=buf.toString();
				}
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public boolean autoInvocation(MOB mob, boolean force)
	{
		return super.autoInvocation(mob, force);
	}
}
