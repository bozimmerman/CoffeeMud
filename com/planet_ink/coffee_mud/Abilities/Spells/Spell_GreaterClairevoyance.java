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

/*
   Copyright 2022-2025 Bo Zimmerman

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
public class Spell_GreaterClairevoyance extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_GreaterClairevoyance";
	}

	private final static String localizedName = CMLib.lang().L("Greater Clairevoyance");

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
	public long flags()
	{
		return super.flags() | Ability.FLAG_DIVINING;
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

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		final Physical P=affected;
		if((canBeUninvoked())&&(invoker!=null)&&(P!=null))
			invoker.tell(L("Your visions of '@x1' fade.",P.name(invoker)));
		super.unInvoke();

	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		final MOB mob=invoker;
		final Room R=msg.source().location();
		if(((msg.sourceMinor()==CMMsg.TYP_LOOK)||(msg.sourceMinor()==CMMsg.TYP_EXAMINE))
		&&(mob!=null)
		&&(R!=null)
		&&(mob.location()!=R)
		&&(msg.target() != null)
		&&(!CMSecurity.isAllowed(msg.source(), msg.source().location(), CMSecurity.SecFlag.ALLSKILLS))
		&&(!CMLib.flags().isCloaked(msg.source()))
		&&(msg.othersMessage()!=null))
		{
			final CMMsg msg2=CMClass.getMsg(mob,msg.source(),this,verbalCastCode(mob,msg.source(),false),null);
			if(R.okMessage(mob,msg2))
			{
				final CMMsg newAffect=CMClass.getMsg(mob,msg.target(),msg.sourceMinor(),null);
				msg.target().executeMsg(msg.target(),newAffect);
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
			if(targetName.equalsIgnoreCase("room")
			||targetName.equalsIgnoreCase(CMLib.english().removeArticleLead(localR.Name())))
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
			commonTelL(mob,"You can't seem to focus on '@x1'.",targetName);
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
			commonTelL(mob,"You can't seem to focus on '@x1'.",targetName);
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final Room newRoom=(target instanceof Room)?((Room)target):((Area)target).getRandomProperRoom();
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> invoke(s) a greater clairevoyance calling '@x1'.^?",targetName));
			final CMMsg msg2=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),null);
			if((mob.location().okMessage(mob,msg))
			&&((newRoom==mob.location())||(newRoom.okMessage(mob,msg2))))
			{
				mob.location().send(mob,msg);
				if(newRoom!=mob.location())
					newRoom.send(mob,msg2);
				beneficialAffect(mob,target,asLevel,-50/*%*/);
			}

		}
		else
			beneficialVisualFizzle(mob,null,L("<S-NAME> attempt(s) to invoke a greater clairevoyance, but fizzle(s) the spell."));

		// return whether it worked
		return success;
	}
}
