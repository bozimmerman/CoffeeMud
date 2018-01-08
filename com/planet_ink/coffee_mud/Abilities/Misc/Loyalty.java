package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB.Attrib;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.lang.ref.WeakReference;
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
public class Loyalty extends StdAbility
{
	@Override
	public String ID()
	{
		return "Loyalty";
	}

	private final static String	localizedName	= CMLib.lang().L("Loyalty");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= "(Loyal to @x1)";

	@Override
	public String displayText()
	{
		return CMLib.lang().L(localizedStaticDisplay, loyaltyName);
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

	protected final static int		INTERVAL		= (2 * 60) / 4000;
	protected String				loyaltyName		= "";
	protected boolean				teleport		= false;
	protected volatile boolean		watchForMaster	= true;
	protected int					checkDown		= INTERVAL;
	protected WeakReference<MOB>	loyaltyPlayer	= null;

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PROPERTY;
	}

	@Override
	public void setMiscText(String newMiscText)
	{
		super.setMiscText(newMiscText);
		loyaltyName = CMParms.getParmStr(newMiscText, "NAME", "");
		teleport = CMParms.getParmBool(newMiscText, "TELEPORT", false);
	}
	
	protected MOB getPlayer()
	{
		if((loyaltyPlayer != null)
		&&(loyaltyPlayer.get() != null)
		&&(!loyaltyPlayer.get().amDestroyed()))
			return loyaltyPlayer.get();
		final MOB player=CMLib.players().getLoadPlayer(loyaltyName);
		if(player == null)
		{
			loyaltyName = "";
			if(affected != null)
				affected.delEffect(this);
		}
		else
			loyaltyPlayer = new WeakReference<MOB>(player);
		return player;
	}
	
	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((ticking instanceof MOB)
		&&(tickID==Tickable.TICKID_MOB)
		&&(loyaltyName.length()>0))
		{
			final MOB M=(MOB)ticking;
			final MOB player=getPlayer();
			if(--checkDown<=0)
			{
				checkDown = INTERVAL;
				watchForMaster = false;
				if(CMLib.flags().canFreelyBehaveNormal(M) && (player != null))
				{
					watchForMaster = true;
					if(CMLib.flags().isInTheGame(player, true) && (player.location()!=null) && (!M.isAttributeSet(Attrib.AUTOGUARD)) && teleport)
						CMLib.tracking().wanderCheckedFromTo(M, player.location(), true);
				}
			}
			if((watchForMaster) && (player != null) && (player.location()==M.location()) && (M.amFollowing()==null))
			{
				CMLib.commands().postFollow(M,player,false);
				if(M.amFollowing()==player)
					watchForMaster = false;
			}
		}
		return super.tick(ticking, tickID);
	}
}
