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
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2017-2017 Bo Zimmerman

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

public class Banishment extends StdAbility
{
	@Override
	public String ID()
	{
		return "Banishment";
	}

	private final static String	localizedName	= CMLib.lang().L("Banishment");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Banishment)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PROPERTY;
	}

	protected Area banishedFrom = null;

	protected boolean badDestination(final MOB mob, final Room R)
	{
		final Area fromA=banishedFrom;
		if(fromA == null)
			return false;
		
		if(R.getArea()==fromA)
			return true;
		if(fromA.inMyMetroArea(R.getArea()))
			return true;
		return false;
	}
	
	@Override
	public void setMiscText(String newMiscText)
	{
		super.setMiscText(newMiscText);
		if(newMiscText.length()>0)
		{
			Area newArea=CMLib.map().findArea(newMiscText);
			if(newArea!=null)
				banishedFrom=newArea;
		}
		else
			banishedFrom=null;
	}
	
	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(banishedFrom == null)
			return super.okMessage(myHost, msg);
		
		if((affected instanceof MOB)&&(msg.amISource((MOB)affected)))
		{
			if(msg.sourceMinor()==CMMsg.TYP_RECALL)
			{
				final MOB mob=msg.source();
				Room recallRoom=CMLib.map().getStartRoom(mob);
				if((recallRoom==null)&&(!mob.isMonster()))
				{
					mob.setStartRoom(CMLib.login().getDefaultStartRoom(mob));
					recallRoom=CMLib.map().getStartRoom(mob);
				}
				if(this.badDestination(mob, recallRoom))
				{
					if(msg.source().location()!=null)
						msg.source().location().show(msg.source(),null,CMMsg.MSG_OK_ACTION,L("<S-NAME> attempt(s) to recall, but a geas prevents <S-HIM-HER>."));
					return false;
				}
			}
			else
			if(msg.sourceMinor()==CMMsg.TYP_FLEE)
			{
				if((msg.target() instanceof Room)
				&&(badDestination(msg.source(), (Room)msg.target())))
				{
					msg.source().location().show(msg.source(),null,CMMsg.MSG_OK_ACTION,L("<S-NAME> attempt(s) to flee, but a geas prevents <S-HIM-HER>."));
					return false;
				}
			}
			else
			if((msg.targetMinor()==CMMsg.TYP_ENTER)
			&&(msg.target() instanceof Room)
			&&(badDestination(msg.source(), (Room)msg.target())))
			{
				msg.source().location().show(msg.source(),null,CMMsg.MSG_OK_ACTION,L("<S-NAME> attempt(s) to defy <S-HIS-HER> exile, but a geas prevents <S-HIM-HER>."));
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			mob.tell(L("Your exile has been lifted."));
	}
	
	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical target, boolean auto, int asLevel)
	{
		if(target instanceof Area)
			this.banishedFrom=(Area)target;
		this.startTickDown(mob, target, 0);
		return true;
	}
}
