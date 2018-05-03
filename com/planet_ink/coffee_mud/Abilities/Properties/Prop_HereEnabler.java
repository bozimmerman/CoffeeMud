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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2016-2018 Bo Zimmerman

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
public class Prop_HereEnabler extends Prop_HaveEnabler
{

	@Override
	public String ID()
	{
		return "Prop_HereEnabler";
	}

	@Override
	public String name()
	{
		return "Granting skills on arrival";
	}
	
	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ROOMS|Ability.CAN_AREAS;
	}

	@Override
	public int triggerMask()
	{
		return TriggeredAffect.TRIGGER_ENTER;
	}

	@Override
	public String accountForYourself()
	{
		return spellAccountingsWithMask("Grants ", " to the one who enters.");
	}

	public Prop_HereEnabler()
	{
	}

	@Override
	public void executeMsg(Environmental host, CMMsg msg)
	{
		super.executeMsg(host, msg);
		if((msg.sourceMinor()==CMMsg.TYP_ENTER)||(msg.sourceMinor()==CMMsg.TYP_LEAVE))
		{
			final Physical baseAffectedP = this.affected;
			if(msg.source().fetchEffect(ID())==null)
			{
				synchronized(this)
				{
					addMeIfNeccessary(msg.source(),msg.source(),maxTicks);
					final Prop_HereEnabler here = new Prop_HereEnabler()
					{
						@Override
						public void executeMsg(Environmental host, CMMsg msg)
						{
							if((this.affected==msg.source())
							&&((msg.sourceMinor()==CMMsg.TYP_ENTER)||(msg.sourceMinor()==CMMsg.TYP_LEAVE)))
							{
								final Room R=msg.source().location();
								if((R!=null)&&(R.getArea()!=null))
								{
									if((baseAffectedP instanceof Room)&&(R!=baseAffectedP))
										unInvoke();
									else
									if((baseAffectedP instanceof Area)&&(((Area)baseAffectedP).inMyMetroArea(R.getArea())))
										unInvoke();
									else
									if(R!=CMLib.map().roomLocation(baseAffectedP))
										unInvoke();
								}
							}
						}
						
						@Override
						public boolean canBeUninvoked()
						{
							return true;
						}
						
						@Override
						public boolean isSavable()
						{
							return false;
						}
						
						@Override
						public void unInvoke()
						{
							final Physical M=this.lastMOB;
							super.removeMyAffectsFromLastMob();
							super.unInvoke();
							if(M!=null)
							{
								M.delEffect(this);
							}
						}
					};
					msg.source().addEffect(here);
					here.lastMOB=msg.source();
					here.spellV = spellV;
					here.lastMOBeffects	= lastMOBeffects;
					here.processing2= false;
					here.clearedYet	= clearedYet;
				}
			}
		}
		
	}

	@Override
	public void affectPhyStats(Physical host, PhyStats affectableStats)
	{
		processing=false;
	}
}
