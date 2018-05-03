package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2005-2018 Bo Zimmerman

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

public class Addictions extends StdAbility
{
	@Override
	public String ID()
	{
		return "Addictions";
	}

	private final static String	localizedName	= CMLib.lang().L("Addictions");

	@Override
	public String name()
	{
		return localizedName;
	}

	private long	lastFix	= System.currentTimeMillis();

	@Override
	public String displayText()
	{
		return craving() ? "(Addiction to " + text() + ")" : "";
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
	public int classificationCode()
	{
		return Ability.ACODE_PROPERTY;
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

	private Item puffCredit=null;
	
	private final static long CRAVE_TIME=TimeManager.MILI_HOUR;
	private final static long WITHDRAW_TIME=TimeManager.MILI_DAY;

	private boolean craving()
	{
		return (System.currentTimeMillis() - lastFix) > CRAVE_TIME;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		if((craving())
		&&(CMLib.dice().rollPercentage()<=((System.currentTimeMillis()-lastFix)/TimeManager.MILI_HOUR))
		&&(ticking instanceof MOB))
		{
			if((System.currentTimeMillis()-lastFix)>WITHDRAW_TIME)
			{
				((MOB)ticking).tell(L("You've managed to kick your addiction."));
				canBeUninvoked=true;
				unInvoke();
				((MOB)ticking).delEffect(this);
				return false;
			}
			if((puffCredit!=null)
			&&(puffCredit.amDestroyed()
				||puffCredit.amWearingAt(Wearable.IN_INVENTORY)
				||puffCredit.owner()!=(MOB)affected))
				puffCredit=null;
			switch(CMLib.dice().roll(1,7,0))
			{
			case 1:
				((MOB) ticking).tell(L("Man, you could sure use some @x1.", text()));
				break;
			case 2:
				((MOB) ticking).tell(L("Wouldn't some @x1 be great right about now?", text()));
				break;
			case 3:
				((MOB) ticking).tell(L("You are seriously craving @x1.", text()));
				break;
			case 4:
				((MOB) ticking).tell(L("There's got to be some @x1 around here somewhere.", text()));
				break;
			case 5:
				((MOB) ticking).tell(L("You REALLY want some @x1.", text()));
				break;
			case 6:
				((MOB) ticking).tell(L("You NEED some @x1, NOW!", text()));
				break;
			case 7:
				((MOB) ticking).tell(L("Some @x1 would be lovely.", text()));
				break;
			}

		}
		return true;
	}

	@Override
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if(affected instanceof MOB)
		{
			if((msg.source()==affected)
			&&(msg.targetMinor()==CMMsg.TYP_WEAR)
			&&(msg.target() instanceof Light)
			&&(msg.target() instanceof Container)
			&&(CMath.bset(((Item)msg.target()).rawProperLocationBitmap(),Wearable.WORN_MOUTH)))
			{
				final List<Item> contents=((Container)msg.target()).getContents();
				if(contents.size()>0)
				{
					final Environmental content=contents.get(0);
					if(CMLib.english().containsString(content.Name(),text()))
						puffCredit=(Item)msg.target();
				}
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(affected instanceof MOB)
		{
			if(msg.source()==affected)
			{
				if(((msg.targetMinor()==CMMsg.TYP_EAT)||(msg.targetMinor()==CMMsg.TYP_DRINK))
				&&((msg.target() instanceof Food)||(msg.target() instanceof Drink))
				&&(msg.target() instanceof Item)
				&&(CMLib.english().containsString(msg.target().Name(),text())))
					lastFix=System.currentTimeMillis();

				if((msg.amISource((MOB)affected))
				&&(msg.targetMinor()==CMMsg.TYP_HANDS)
				&&(msg.target() instanceof Light)
				&&(msg.tool() instanceof Light)
				&&(msg.target()==msg.tool())
				&&(((Light)msg.target()).amWearingAt(Wearable.WORN_MOUTH))
				&&(((Light)msg.target()).isLit())
				&&((puffCredit!=null)||CMLib.english().containsString(msg.target().Name(),text())))
					lastFix=System.currentTimeMillis();
			}
		}
		super.executeMsg(myHost,msg);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Physical target=givenTarget;

		if(target==null)
			return false;
		if(target.fetchEffect(ID())!=null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			String addiction=target.Name().toUpperCase();
			if(addiction.toUpperCase().startsWith("A POUND OF "))
				addiction=addiction.substring(11);
			if(addiction.toUpperCase().startsWith("A "))
				addiction=addiction.substring(2);
			if(addiction.toUpperCase().startsWith("AN "))
				addiction=addiction.substring(3);
			if(addiction.toUpperCase().startsWith("SOME "))
				addiction=addiction.substring(5);
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_OK_VISUAL,"");
			if(mob.location()!=null)
			{
				if(mob.location().okMessage(mob,msg))
				{
					mob.location().send(mob,msg);
					final Ability A=(Ability)copyOf();
					A.setMiscText(addiction.trim());
					mob.addNonUninvokableEffect(A);
				}
			}
			else
			{
				final Ability A=(Ability)copyOf();
				A.setMiscText(addiction.trim());
				mob.addNonUninvokableEffect(A);
			}
		}
		return success;
	}
}
