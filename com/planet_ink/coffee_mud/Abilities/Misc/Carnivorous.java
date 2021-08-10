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
public class Carnivorous extends StdAbility
{
	@Override
	public String ID()
	{
		return "Carnivorous";
	}

	private final static String	localizedName	= CMLib.lang().L("Carnivorous");

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

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if((msg.targetMinor()==CMMsg.TYP_EAT)
		&&(myHost instanceof MOB)
		&&(msg.amISource((MOB)myHost))
		&&(msg.target() instanceof Food))
		{
			final int hunger = msg.source().curState().getHunger();
			if((((Food)msg.target()).material()&RawMaterial.MATERIAL_MASK) == RawMaterial.MATERIAL_FLESH)
			{
				msg.addTrailerRunnable(new Runnable() {
					final MOB M=msg.source();
					final int oldH = hunger;
					@Override
					public void run()
					{
						final int hungerDiff = M.curState().getHunger()-oldH;
						if(hungerDiff > 0)
							M.curState().adjHunger(hungerDiff, M.maxState().maxHunger(M.baseWeight()));
					}
				});
			}
			else
			{
				msg.addTrailerRunnable(new Runnable() {
					final MOB M=msg.source();
					final int oldH = hunger;
					@Override
					public void run()
					{
						M.curState().setHunger(oldH);
					}
				});
			}
		}
		return true;
	}


	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		String choice="";
		if(givenTarget!=null)
		{
			if((commands.size()>0)&&((commands.get(0)).equals(givenTarget.name())))
				commands.remove(0);
			choice=CMParms.combine(commands,0);
			commands.clear();
		}
		else
		if(commands.size()>1)
		{
			choice=CMParms.combine(commands,1);
			while(commands.size()>1)
				commands.remove(1);
		}
		final MOB target=getTarget(mob,commands,givenTarget);

		if(target==null)
			return false;
		if(target.fetchEffect(ID())!=null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final ArrayList<String> allChoices=new ArrayList<String>();
			for(final int code : RawMaterial.CODES.ALL())
				allChoices.add(RawMaterial.CODES.NAME(code));
			Race R=null;
			for(final Enumeration<Race> r=CMClass.races();r.hasMoreElements();)
			{
				R=r.nextElement();
				allChoices.add(R.ID().toUpperCase());
			}
			String foodType="";
			if((choice.length()>0)&&(allChoices.contains(choice.toUpperCase())))
				foodType=choice.toUpperCase();
			else
			for(int i=0;i<allChoices.size();i++)
			{
				if((CMLib.dice().roll(1,allChoices.size(),0)==1)
				&&(!(allChoices.get(i).equalsIgnoreCase(mob.charStats().getMyRace().ID().toUpperCase()))))
					foodType+=" "+allChoices.get(i);
			}
			if(foodType.length()==0)
				return false;

			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_OK_VISUAL,"");
			if(target.location()!=null)
			{
				if(target.location().okMessage(target,msg))
				{
					target.location().send(target,msg);
					final Ability A=(Ability)copyOf();
					A.setMiscText(foodType.trim());
					target.addNonUninvokableEffect(A);
				}
			}
			else
			{
				final Ability A=(Ability)copyOf();
				A.setMiscText(foodType.trim());
				target.addNonUninvokableEffect(A);
			}
		}
		return success;
	}
}
