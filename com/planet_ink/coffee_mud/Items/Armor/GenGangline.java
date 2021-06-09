package com.planet_ink.coffee_mud.Items.Armor;
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
   Copyright 2021-2021 Bo Zimmerman

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
public class GenGangline extends GenArmor
{
	@Override
	public String ID()
	{
		return "GenGangline";
	}

	public GenGangline()
	{
		super();

		setName("a gangline");
		setDisplayText("a set of leather straps to bind a team together.");
		setDescription("");
		properWornBitmap=Wearable.WORN_ABOUT_BODY;
		wornLogicalAnd=false;
		basePhyStats().setArmor(0);
		basePhyStats().setAbility(0);
		basePhyStats().setWeight(5);

		recoverPhyStats();
		material=RawMaterial.RESOURCE_LEATHER;
	}

	protected volatile long nextCheck = Long.MAX_VALUE;

	protected boolean isGanglined(final Environmental E)
	{
		boolean found=false;
		if(E instanceof MOB)
		{
			for(final Enumeration<Item> i=((MOB)E).items();i.hasMoreElements();)
			{
				final Item I=i.nextElement();
				if(I.ID().equals(ID())
				&&(!I.amWearingAt(Item.IN_INVENTORY))
				&&(I.amBeingWornProperly()))
					found=true;
			}
		}
		return found;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if((msg.targetMinor()==CMMsg.TYP_MOUNT)
		&&(owner() instanceof MOB)
		&&(this.amBeingWornProperly()))
		{
			final MOB ownM=(MOB)owner();
			if((msg.target()==owner())
			&&(msg.tool() instanceof MOB))
			{
				final MOB toM=(MOB)msg.tool();
				if(!isGanglined(toM))
				{
					msg.source().tell(L("Both targets must be wearing @x1 to do that.",name()));
					return false;
				}
				if(toM.amFollowing()!=null)
				{
					if(isGanglined(toM))
					{
						msg.source().tell(L("@x1 is already connected to @x2.",toM.name(),toM.amFollowing().name()));
						return false;
					}
					else
					{
						msg.source().tell(L("@x1 is already following @x2.",toM.name(),toM.amFollowing().name()));
						return false;
					}
				}
				if(ownM.amFollowing()!=null)
				{
					if(isGanglined(ownM))
					{
						msg.source().tell(L("@x1 is already connected to @x2.",ownM.name(),ownM.amFollowing().name()));
						return false;
					}
					else
					{
						msg.source().tell(L("@x1 is already following @x2.",ownM.name(),ownM.amFollowing().name()));
						return false;
					}
				}
				final MOB actorM=msg.source();
				final String newMsgText = L("<O-NAME> connect(s) <S-NAME> to <T-NAME>.");
				final int newCd = CMMsg.MASK_ALWAYS|CMMsg.MSG_FOLLOW;
				msg.modify((MOB)msg.tool(), msg.target(), actorM, newCd, newMsgText, newCd, newMsgText, newCd, newMsgText);
			}
			else
			if((msg.tool()==owner())
			&&(msg.target() instanceof Item))
			{
				for(final MOB M : ownM.getGroupMembers(new HashSet<MOB>()))
				{
					if(isGanglined(M)
					&&(M instanceof Rideable)
					&&(((Rideable)M).numRiders()>0))
					{
						for(final Enumeration<Rider> r=((Rideable)M).riders();r.hasMoreElements();)
						{
							final Rider R=r.nextElement();
							if(R instanceof Item)
							{
								msg.source().tell(L("@x1 is already mounted to @x2.",R.name(),M.amFollowing().name()));
								return false;
							}
						}
					}
				}
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		if((msg.targetMinor()==CMMsg.TYP_REMOVE)
		&&(msg.target()==this)
		&&(owner() instanceof MOB)
		&&(this.amBeingWornProperly()))
		{
			final MOB M=(MOB)owner();
			if(M.amFollowing()!=null)
				CMLib.commands().postFollow(M, null, true);
		}
		else
		if((msg.targetMinor()==CMMsg.TYP_LEAVE)
		&&(owner() instanceof MOB)
		&&(msg.source()==((MOB)owner()).amFollowing())
		&&(this.amBeingWornProperly()))
		{
			this.nextCheck=System.currentTimeMillis()+4000;
		}
		else
		if(System.currentTimeMillis()>this.nextCheck)
		{
			this.nextCheck=Long.MAX_VALUE;
			final ItemPossessor P=owner();
			if(P instanceof MOB)
			{
				final MOB M=(MOB)P;
				final MOB fM=M.amFollowing();
				final Item I=this;
				if((fM!=null)
				&&(fM.location()!=M.location())
				&&(!fM.amDead())
				&&(!fM.amDestroyed())
				&&(!M.amDead())
				&&(!M.amDestroyed())
				&&(this.amBeingWornProperly()))
				{
					CMLib.threads().scheduleRunnable(new Runnable()
					{
						final MOB followM = fM;
						final MOB meM=M;
						final Item meI=I;
						@Override
						public void run()
						{
							final Room R=followM.location();
							if((R!=meM.location())
							&&(R!=null)
							&&(!followM.amDead())
							&&(!followM.amDestroyed())
							&&(!M.amDead())
							&&(!M.amDestroyed())
							&&(R.isInhabitant(followM))
							&&(meI.amBeingWornProperly()))
							{
								R.bringMobHere(meM, true);
							}
						}

					}, 1500);
				}
			}
			this.nextCheck=Long.MAX_VALUE;
		}
	}
}
