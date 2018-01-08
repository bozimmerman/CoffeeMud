package com.planet_ink.coffee_mud.Abilities.Ranger;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
   Copyright 2014-2018 Bo Zimmerman

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
public class Ranger_SenseTraps extends StdAbility
{
	@Override
	public String ID()
	{
		return "Ranger_SenseTraps";
	}

	private final static String localizedName = CMLib.lang().L("Sense Snares and Pits");

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
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_NATURELORE;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	@Override
	public int enchantQuality()
	{
		return Ability.QUALITY_BENEFICIAL_SELF;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
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

	protected Room lastRoom=null;

	public String trapCheck(final MOB viewer, final Physical P, final int dir)
	{
		if(P!=null)
		if(CMLib.utensils().fetchMyTrap(P)!=null)
		{
			if(dir >= 0)
				return L("To the @x1, @x2 is trapped.\n\r",CMLib.directions().getDirectionName(dir),P.name(viewer));
			else
				return L("@x1 is trapped.\n\r",P.name(viewer));
		}
		return "";
	}

	public String trapHere(MOB mob, Physical P)
	{
		final StringBuffer msg=new StringBuffer("");
		if(P==null)
			return msg.toString();
		if((P instanceof Room)&&(CMLib.flags().canBeSeenBy(P,mob)))
		{
			final Room R=(Room)P;
			if(CMLib.flags().isInWilderness(R))
				msg.append(trapCheck(mob,P,-1));
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			{
				final Exit E=R.getExitInDir(d);
				final Room R2=R.getRoomInDir(d);
				if((E != null)&&(R2 != null) 
				&&((CMLib.flags().isInWilderness(R)) || (CMLib.flags().isInWilderness(R2))))
				{
					final Exit E2=R.getReverseExit(d);
					msg.append(trapHere(mob,E));
					msg.append(trapHere(mob,E2));
					msg.append(trapCheck(mob,R2,d));
				}
			}
			if(CMLib.flags().isInWilderness(R))
			{
				for(int i=0;i<R.numItems();i++)
				{
					final Item I=R.getItem(i);
					if((I!=null)&&(I.container()==null))
						msg.append(trapHere(mob,I));
				}
			}
		}
		else
		if((P instanceof Item)&&(CMLib.flags().canBeSeenBy(P,mob)))
			msg.append(trapCheck(mob,P,-1));
		else
		if((P instanceof Exit)&&(CMLib.flags().canBeSeenBy(P,mob)))
			msg.append(trapCheck(mob,P,-1));
		return msg.toString();
	}

	public void messageTo(MOB mob)
	{
		final String here=trapHere(mob,mob.location());
		if(here.length()>0)
		{
			mob.tell(here);
			super.helpProficiency(mob, 0);
		}
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((tickID==Tickable.TICKID_MOB)
		   &&(affected instanceof MOB)
		   &&(((MOB)affected).location()!=null)
		   &&((lastRoom==null)||(((MOB)affected).location()!=lastRoom)))
		{
			lastRoom=((MOB)affected).location();
			if(proficiencyCheck((MOB)affected,0,false))
				messageTo((MOB)affected);
		}
		return true;
	}
}
