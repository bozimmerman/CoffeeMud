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
   Copyright 2017-2018 Bo Zimmerman

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
public class Prop_WearOverride extends Property
{
	@Override
	public String ID()
	{
		return "Prop_WearOverride";
	}

	@Override
	public String name()
	{
		return "Wearable Unzapper";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ITEMS;
	}

	protected MaskingLibrary.CompiledZMask mask = null;
	protected String maskDesc = "";
	protected long	 locMaskAdj = Integer.MAX_VALUE;
	protected volatile boolean activated = false;
	
	public String accountForYourself()
	{
		if(affected != null)
			return "Allows "+affected.Name()+" to be worn by: "+maskDesc;
		else
			return "Allows an item to be worn by "+maskDesc;
	}

	@Override
	public void setMiscText(String newText)
	{
		maskDesc="";
		mask=null;
		if(newText.length()>0)
		{
			mask=CMLib.masking().maskCompile(newText);
			maskDesc=CMLib.masking().maskDesc(newText,true);
		}
		super.setMiscText(newText);
	}

	@Override
	public boolean bubbleAffect()
	{
		return activated;
	}

	@Override
	public void affectCharStats(MOB affectMOB, CharStats affectableStats)
	{
		if(this.activated)
			affectableStats.setWearableRestrictionsBitmap(affectableStats.getWearableRestrictionsBitmap() & this.locMaskAdj);
	}
	
	@Override
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if((msg.target() == affected)
		&&(msg.sourceMinor()==CMMsg.TYP_WEAR)
		&&(affected instanceof Item))
		{
			if((mask != null)
			&&(CMLib.masking().maskCheck(mask, msg.source(), true)))
			{
				activated=true;
				this.locMaskAdj=~((Item)msg.target()).rawProperLocationBitmap();
				msg.source().recoverCharStats();
				msg.source().recoverCharStats();
			}
			else
			{
				msg.source().tell(L("That won't fit on the likes of you."));
				return false;
			}
		}
		return super.okMessage(host, msg);
	}
}
