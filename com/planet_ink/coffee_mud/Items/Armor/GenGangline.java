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
   Copyright 2021-2022 Bo Zimmerman

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
		if((msg.source()==this)
		&&(msg.targetMinor()==CMMsg.TYP_LEAVE)
		&&(msg.target() instanceof Room))
		{
			//TODO: shouldn't be able to do unless
			//1. is their leader
			//2. their leader is not present
			// howevever, okmess happens before any mobs actually move... <sigh>
		}
		else
		if((msg.targetMinor()==CMMsg.TYP_MOUNT)
		&&(owner() instanceof MOB)
		&&(this.amBeingWornProperly()))
		{
			final MOB ownM=(MOB)owner();
			if((msg.target()==owner())
			&&(msg.tool() instanceof MOB)
			&&(msg.tool() != owner()))
			{
				final MOB toM=(MOB)msg.tool();
				if(!isGanglined(toM))
				{
					msg.source().tell(L("Both targets must be wearing @x1 to do that.",name()));
					return false;
				}
				if(toM.riding()!=null)
				{
					if(isGanglined(toM))
					{
						msg.source().tell(L("@x1 is already connected to @x2.",toM.name(),toM.riding().name()));
						return false;
					}
					else
					{
						msg.source().tell(L("@x1 is already riding @x2.",toM.name(),toM.riding().name()));
						return false;
					}
				}
				if(ownM.riding()!=null)
				{
					if(isGanglined(ownM))
					{
						msg.source().tell(L("@x1 is already connected to @x2.",ownM.name(),ownM.riding().name()));
						return false;
					}
					else
					{
						msg.source().tell(L("@x1 is already riding @x2.",ownM.name(),ownM.riding().name()));
						return false;
					}
				}
				final MOB actorM=msg.source();
				final String newMsgText = L("<O-NAME> connect(s) <S-NAME> to <T-NAME>.");
				final int newCd = CMMsg.MASK_ALWAYS|CMMsg.MSG_FOLLOW;
				msg.modify((MOB)msg.tool(), msg.target(), actorM, msg.sourceCode(), newMsgText, newCd, newMsgText, msg.othersCode(), newMsgText);
				msg.addTrailerRunnable(new Runnable()
				{
					final MOB fixM=ownM;
					final MOB toM=msg.source();
					@Override
					public void run()
					{
						if(toM instanceof Rideable)
						{
							toM.setFollowing(null);
							toM.setRiding((Rideable)fixM);
						}
						fixM.recoverCharStats();
					}
				});
				ownM.charStats().setStat(CharStats.STAT_CHARISMA, 25);
				//((MOB)msg.tool()).charStats().setStat(CharStats.STAT_CHARISMA, 25);
			}
			else
			if((msg.tool()==owner())
			&&(msg.target() instanceof Item))
			{
				if(ownM.riding()!=null)
				{
					msg.source().tell(L("@x1 is already mounted to @x2.",ownM.name(),ownM.riding().name()));
					return false;
				}
			}
		}
		else
		if((msg.sourceMinor()==CMMsg.TYP_COMMANDFAIL)
		&&(msg.targetMessage()!=null)
		&&(msg.targetMessage().length()>0))
		{
			if(Character.toUpperCase(msg.targetMessage().charAt(0)) == 'D')
			{
				final List<String> parsedFail = CMParms.parse(msg.targetMessage());
				if(parsedFail.size()<2)
					return true;
				final String cmd=parsedFail.get(0).toUpperCase();
				if(!("DISMOUNT".startsWith(cmd)))
					return true;
				final ItemPossessor P=owner();
				if(P instanceof MOB)
				{
					final MOB M=(MOB)P;
					final Room R=M.location();
					if(R!=null)
					{
						final MOB targetM=R.fetchInhabitant(CMParms.combine(parsedFail,1));
						if(targetM == M)
						{
							if(this.amBeingWornProperly()
							&&(M.riding() instanceof MOB))
							{
								final MOB folM=(MOB)M.riding();
								if(folM==null)
								{
									msg.source().tell(L("@x1 is not connected.",M.name(msg.source())));
									return false;
								}
								CMLib.commands().forceStandardCommand(targetM, "Dismount", new XVector<String>("Dismount"));
								if(M.riding()==null)
									R.show(msg.source(), M, folM, CMMsg.MSG_OK_VISUAL, L("<S-NAME> disconnects <T-NAME> from <O-NAME>."));
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
			if(M.riding()!=null)
				CMLib.commands().forceStandardCommand(M, "Dismount", new XVector<String>("Dismount"));
		}
		else
		if((msg.targetMinor()==CMMsg.TYP_LEAVE)
		&&(owner() instanceof MOB)
		&&(msg.source()==((MOB)owner()).riding())
		&&(this.amBeingWornProperly()))
		{
			this.nextCheck=System.currentTimeMillis()+4000;
		}
		else
		if(System.currentTimeMillis()>this.nextCheck)
		{
			this.nextCheck=Long.MAX_VALUE;
			final ItemPossessor P=owner();
			if((P instanceof MOB)
			&&(((MOB)P).riding() instanceof MOB))
			{
				final MOB M=(MOB)P;
				final MOB fM=(MOB)M.riding();
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
						final MOB rideM = fM;
						final MOB meM=M;
						final Item meI=I;
						@Override
						public void run()
						{
							final Room R=rideM.location();
							if((R!=meM.location())
							&&(R!=null)
							&&(!rideM.amDead())
							&&(!rideM.amDestroyed())
							&&(!M.amDead())
							&&(!M.amDestroyed())
							&&(R.isInhabitant(rideM))
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
