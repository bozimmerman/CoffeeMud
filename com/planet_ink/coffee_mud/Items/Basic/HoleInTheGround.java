package com.planet_ink.coffee_mud.Items.Basic;
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
   Copyright 2011-2018 Bo Zimmerman

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
public class HoleInTheGround extends GenContainer
{
	@Override
	public String ID()
	{
		return "HoleInTheGround";
	}

	public HoleInTheGround()
	{
		super();
		setName("a hole in the ground");
		setDisplayText("a hole in the ground");
		setDescription("Looks like someone has dug hole here.  Perhaps something is in it?");
		capacity=0;
		baseGoldValue=0;
		basePhyStats().setWeight(0);
		basePhyStats().setSensesMask(basePhyStats.sensesMask()
									|PhyStats.SENSE_ITEMNOTGET
									|PhyStats.SENSE_ITEMNOWISH
									|PhyStats.SENSE_ITEMNORUIN
									|PhyStats.SENSE_UNLOCATABLE);
		basePhyStats.setDisposition(basePhyStats.disposition()
									|PhyStats.IS_UNSAVABLE
									|PhyStats.IS_NOT_SEEN);
		setMaterial(RawMaterial.RESOURCE_DUST);
		recoverPhyStats();
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(msg.amITarget(owner()))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_ENTER:
			case CMMsg.TYP_LEAVE:
			case CMMsg.TYP_RECALL:
				if((owner() instanceof Room)
				&&(((Room)owner()).numPCInhabitants()==0))
				{
					if(!hasContent())
					{
						destroy();
						return true;
					}
					else
					{
						basePhyStats().setDisposition(basePhyStats().disposition()|PhyStats.IS_HIDDEN);
						recoverPhyStats();
					}
				}
				break;
			case CMMsg.TYP_EXPIRE:
				if(hasContent())
				{
					return false;
				}
				break;
			}
		}
		else
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_CLOSE:
				msg.setSourceMessage("<S-NAME> fill(s) the hole back in.");
				msg.setOthersMessage("<S-NAME> fill(s) the hole back in.");
				return true;
			case CMMsg.TYP_PUT:
				if((msg.tool() instanceof Item))
				{
					if((readableText().length()>0)&&(!readableText().equals(msg.source().Name())))
					{
						msg.source().tell(L("Go find your own hole."));
						return false;
					}
				}
				if((msg.tool() instanceof ClanItem))
				{
					msg.source().tell(L("Go may not bury a clan item."));
					return false;
				}
				break;
			}
		}
		return super.okMessage(myHost, msg);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(msg.target()==owner())
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_DIG:
				if(CMath.bset(basePhyStats().disposition(), PhyStats.IS_NOT_SEEN)
				||CMath.bset(basePhyStats().disposition(), PhyStats.IS_HIDDEN))
				{
					basePhyStats().setDisposition(CMath.unsetb(basePhyStats().disposition(), PhyStats.IS_NOT_SEEN));
					basePhyStats().setDisposition(CMath.unsetb(basePhyStats().disposition(), PhyStats.IS_HIDDEN));
					recoverPhyStats();
				}
				setCapacity(capacity()+msg.value());
				break;
			}
		}
		else
		if(msg.amITarget(this))
		{
			final MOB mob=msg.source();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_CLOSE:
				if(!hasContent())
					destroy();
				else
				{
					basePhyStats().setDisposition(basePhyStats().disposition()|PhyStats.IS_NOT_SEEN);
					setCapacity(0);
					recoverPhyStats();
				}
				return;
			case CMMsg.TYP_PUT:
				if((msg.tool() instanceof Item))
				{
					final PlayerStats pstats=mob.playerStats();
					if(pstats!=null)
					{
						if(readableText().length()==0)
							setReadableText(mob.Name());
						if(!pstats.getExtItems().isContent(this))
							pstats.getExtItems().addItem(this);
						if(msg.tool() instanceof Decayable)
							((Decayable)msg.tool()).setDecayTime(((Decayable)msg.tool()).decayTime()/2);
						((Item)msg.tool()).setExpirationDate(0);
					}
				}
				break;
			}
		}
		super.executeMsg(myHost, msg);
	}
	
	@Override
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof HoleInTheGround))
			return false;
		return super.sameAs(E);
	}
}
