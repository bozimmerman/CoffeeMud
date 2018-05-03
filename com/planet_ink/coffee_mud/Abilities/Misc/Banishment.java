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

	protected final List<Area> banishedFromAs = new LinkedList<Area>();

	protected boolean badDestination(final MOB mob, final Room R)
	{
		if(banishedFromAs.size() == 0)
			return false;
		
		synchronized(banishedFromAs)
		{
			for(final Area fromA : banishedFromAs)
			{
				if(R.getArea()==fromA)
					return true;
				if(fromA.inMyMetroArea(R.getArea()))
					return true;
			}
		}
		return false;
	}
	
	@Override
	public void setMiscText(String newMiscText)
	{
		//super.setMiscText(newMiscText);
		if(newMiscText.length()>0)
		{
			for(final String areaName : CMParms.parseSemicolons(newMiscText, true))
			{
				Area newArea=CMLib.map().findArea(areaName);
				synchronized(banishedFromAs)
				{
					if((newArea!=null)&&(!banishedFromAs.contains(newArea)))
						banishedFromAs.add(newArea);
				}
			}
		}
		else
		synchronized(banishedFromAs)
		{
			banishedFromAs.clear();
		}
	}
	
	@Override
	public String text()
	{
		StringBuilder names=new StringBuilder("");
		synchronized(banishedFromAs)
		{
			for(final Area fromA : banishedFromAs)
			{
				names.append(fromA.Name()).append(";");
			}
		}
		return names.toString();
	}
	
	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(banishedFromAs.size() == 0)
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
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((givenTarget instanceof Area)&&(auto))
		{
			Banishment B=(Banishment)mob.fetchEffect(ID());
			if(B==null)
				B=this;
			synchronized(B.banishedFromAs)
			{
				B.banishedFromAs.add((Area)givenTarget);
			}
			if((B==this)||(B.affecting()!=mob))
				this.startTickDown(mob, mob, 0);
			return true;
		}
		if((commands.size()<2)&&(auto))
		{
			if(givenTarget instanceof MOB)
			{
				if(commands.size()==0)
				{
					commands.add(givenTarget.Name());
					commands.add(mob.location().getArea().Name());
				}
				else
					commands.add(0,givenTarget.Name());
			}
			else
			{
				if(commands.size()==0)
				{
					commands.add(mob.Name());
					commands.add(mob.location().getArea().Name());
				}
				else
					commands.add(0,mob.Name());
			}
		}
		if(commands.size()<2)
		{
			mob.tell(L("Banish whom from where?"));
			return false;
		}
		
		final MOB target = getTarget(mob, new XVector<String>(commands.get(0)), givenTarget);
		if (target == null)
			return false;
		Area theArea=CMLib.map().findArea(CMParms.combine(commands,1));
		boolean forever=false;
		if((theArea == null)
		&&(commands.size()>1)
		&&(commands.get(commands.size()-1).equalsIgnoreCase("FOREVER")))
		{
			commands.remove(commands.size()-1);
			forever=true;
			theArea=CMLib.map().findArea(CMParms.combine(commands,1));
		}
		if(theArea == null)
		{
			mob.tell(L("You don't know of a place called '@x1'.",CMParms.combine(commands,1)));
			return false;
		}
		if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
			return false;
		final boolean success = proficiencyCheck(mob, 0, auto);
		if (success)
		{
			if (mob.location().show(mob, target, this, CMMsg.TYP_GENERAL, auto ? null : L("<S-NAME> banish(s) <T-NAMESELF> from @x1.",theArea.Name())))
			{
				Banishment B=(Banishment)target.fetchEffect("Banishment");
				if(forever)
				{
					if(B==null)
					{
						B=(Banishment)copyOf();
						target.addNonUninvokableEffect(B);
					}
				}
				else
				{
					B=(Banishment)this.beneficialAffect(mob, target, asLevel, 0);
				}
				synchronized(B.banishedFromAs)
				{
					this.banishedFromAs.clear();
					this.banishedFromAs.add(theArea);
				}
			}
		}
		else if (!auto)
			return beneficialVisualFizzle(mob, target, L("<S-NAME> attempt(s) to banish <T-NAMESELF>, but fail(s)!"));
		return true;
	}
}
