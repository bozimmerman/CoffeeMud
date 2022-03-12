package com.planet_ink.coffee_mud.Abilities.Spells;
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
import java.util.concurrent.atomic.AtomicBoolean;

/*
   Copyright 2022-2022 Bo Zimmerman

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
public class Spell_GreaterScry extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_GreaterScry";
	}

	private final static String localizedName = CMLib.lang().L("Greater Scry");

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
		return CAN_AREAS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_DIVINATION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	@Override
	protected int overrideMana()
	{
		if(affected instanceof Boardable)
			return Ability.COST_ALL;
		return Ability.COST_ALL;
	}

	private final AtomicBoolean recurse=new AtomicBoolean(false);

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		final Physical P=affected;
		if((canBeUninvoked())&&(invoker!=null)&&(P!=null))
			invoker.tell(L("Your knowledge of '@x1' fade.",P.name(invoker)));
		super.unInvoke();

	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((invoker!=msg.source())
		&&(invoker!=null))
		{
			if(((msg.sourceMinor()==CMMsg.TYP_LOOK)||(msg.sourceMinor()==CMMsg.TYP_EXAMINE))
			&&(msg.target()!=null)
			&&((invoker.location()!=msg.source().location())||(!(msg.target() instanceof Room)))
			&&(!CMSecurity.isAllowed(msg.source(), msg.source().location(), CMSecurity.SecFlag.ALLSKILLS))
			&&(!CMLib.flags().isCloaked(msg.source()))
			&&(!recurse.get()))
			{
				final CMMsg newAffect=CMClass.getMsg(invoker,msg.target(),msg.sourceMinor(),null);
				try
				{
					recurse.set(true);
					msg.target().executeMsg(msg.target(),newAffect);
				}
				finally
				{
					recurse.set(false);
				}
			}
			else
			if((invoker.location()!=((MOB)affected).location())
			&&(msg.othersCode()!=CMMsg.NO_EFFECT)
			&&(msg.othersMessage()!=null)
			&&(!CMath.bset(msg.sourceMajor(),CMMsg.MASK_CHANNEL))
			&&(!CMSecurity.isAllowed(msg.source(), msg.source().location(), CMSecurity.SecFlag.ALLSKILLS))
			&&(!CMLib.flags().isCloaked(msg.source()))
			&&(!recurse.get()))
			{
				try
				{
					recurse.set(true);
					invoker.executeMsg(invoker,msg);
				}
				finally
				{
					recurse.set(false);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(commands.size()<1)
		{
			mob.tell(L("Cast on what or where? Do you mean Here?"));
			return false;
		}
		final String targetName=CMParms.combine(commands,0).trim().toUpperCase();
		Places target=null;
		if(givenTarget instanceof Room)
			target=(Room)givenTarget;
		if(givenTarget instanceof Area)
			target=(Area)givenTarget;
		final Area localA=CMLib.map().areaLocation(mob);
		final Room localR=CMLib.map().roomLocation(mob);
		Room finalR=null;
		PhysicalAgent finalI=null;
		if(target==null)
		{
			if(targetName.equalsIgnoreCase("here")
			||targetName.equalsIgnoreCase("area")
			||(CMLib.english().containsString(localA.name(), targetName)))
				target=localA;
			else
			if(targetName.equalsIgnoreCase("room"))
				target=localR;
			else
			if((localA instanceof Boardable)
			&&((localR.domainType()&Room.INDOORS)==0)
			&&((finalR=CMLib.map().roomLocation(((Boardable)localA).getBoardableItem()))!=null)
			&&((finalI=finalR.fetchFromMOBRoomFavorsItems(mob, null, targetName, Filterer.ANYTHING)) instanceof Boardable)
			&&(CMLib.flags().canBeSeenBy(finalI, mob)))
				target=((Boardable)finalI).getArea();
			else
			if((CMLib.english().containsString(localR.name(), targetName))
			||(CMLib.english().containsString(localR.displayText(mob), targetName))
			||(CMLib.english().containsString(localR.description(mob), targetName)))
				target=localR;
			else
			if(((finalI=localR.fetchFromMOBRoomFavorsItems(mob, null, targetName, Filterer.ANYTHING)) instanceof Boardable)
			&&(CMLib.flags().canBeSeenBy(finalI, mob)))
				target=((Boardable)finalI).getArea();
		}
		if(target == null)
		{
			mob.tell(L("You can't seem to focus on '@x1'.",targetName));
			return false;
		}

		final Ability A=target.fetchEffect(ID());
		if((A!=null)&&(A.invoker()==mob))
		{
			A.unInvoke();
			return true;
		}
		else
		if(A!=null)
		{
			mob.tell(L("You can't seem to focus on '@x1'.",targetName));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final Room newRoom=(target instanceof Room)?((Room)target):((Area)target).getRandomProperRoom();
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> invoke(s) a greater scrying calling '@x1'.^?",targetName));
			final CMMsg msg2=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),null);
			if((mob.location().okMessage(mob,msg))
			&&((newRoom==mob.location())||(newRoom.okMessage(mob,msg2))))
			{
				mob.location().send(mob,msg);
				if(newRoom!=mob.location())
					newRoom.send(mob,msg2);
				beneficialAffect(mob,target,asLevel,-25/*%*/);
			}

		}
		else
			beneficialVisualFizzle(mob,null,L("<S-NAME> attempt(s) to invoke a greater scrying, but fizzle(s) the spell."));

		// return whether it worked
		return success;
	}
}
