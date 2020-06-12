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
   Copyright 2002-2020 Bo Zimmerman

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
public class Spell_PlanarBlock extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_PlanarBlock";
	}

	private final static String localizedName = CMLib.lang().L("Planar Block");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Planar Block: ");

	protected final List<String> forbiddenPlanes = new Vector<String>();

	@Override
	public String displayText()
	{
		return localizedStaticDisplay+CMParms.toListString(forbiddenPlanes)+")";
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_COSMOLOGY;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!(affected instanceof MOB))
			return true;

		final MOB mob=(MOB)affected;
		final Room R=mob.location();
		if((msg.tool() instanceof PlanarAbility)
		&&(msg.source()==mob)
		&&(msg.sourceMinor()==CMMsg.TYP_ENTER)
		&&(R!=null)
		&&(msg.target() instanceof Room))
		{
			final boolean summon=CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_SUMMONING);
			final boolean teleport=CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_TRANSPORTING);
			if((summon||teleport))
			{
				final String targetPlane = CMLib.flags().getPlaneOfExistence((Room)msg.target());
				if((targetPlane != null)
				&&(this.forbiddenPlanes.contains(targetPlane.toLowerCase())))
				{
					msg.source().tell(L("A powerful blocking geas prevents the teleporting magic from working."));
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		super.unInvoke();

		if((mob.location()!=null)&&(!mob.amDead())&&(super.canBeUninvoked()))
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> no longer feel(s) planar blocked."));
	}



	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if(mob!=null)
		{
			if((!(target instanceof MOB))||(CMLib.flags().getPlaneOfExistence(target)==null))
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public void setMiscText(final String newMiscText)
	{
		super.setMiscText(newMiscText);
		if((newMiscText!=null)&&(newMiscText.length()>0))
		{
			this.forbiddenPlanes.clear();
			this.forbiddenPlanes.addAll(CMParms.parseCommas(newMiscText.toLowerCase(),true));
		}
	}

	@Override
	public String text()
	{
		return CMParms.combineWith(this.forbiddenPlanes, ',');
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final String currentPlane = CMLib.flags().getPlaneOfExistence(mob);
		if(currentPlane == null)
		{
			mob.tell(L("This magic would not work here."));
			return false;
		}

		final MOB target=this.getTarget(mob, commands, givenTarget, false, true);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,target,this,somanticCastCode(mob,target,auto),auto?"":L("^S<S-NAME> gesture(s) at <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					Spell_PlanarBlock A=(Spell_PlanarBlock)target.fetchEffect(ID());
					if(A==null)
						A=(Spell_PlanarBlock)maliciousAffect(mob,target,asLevel,0,-1);
					if(!A.forbiddenPlanes.contains(currentPlane.toLowerCase()))
						A.forbiddenPlanes.add(currentPlane.toLowerCase());
					A.tickDown = getMaliciousTickdownTime(mob,target,(12+adjustedLevel(mob,asLevel))*(int)CMProps.getTicksPerMudHour(),asLevel);
					final MOB fakeInvoker = CMClass.getMOB("StdMOB");
					final int betterLevel = mob.basePhyStats().level()+50;
					if(betterLevel > A.invoker.basePhyStats().level())
					{
						fakeInvoker.setName(mob.Name());
						fakeInvoker.basePhyStats().setLevel(betterLevel);
						fakeInvoker.phyStats().setLevel(betterLevel);
						A.invoker=fakeInvoker;
					}
					target.location().show(target,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> <S-IS-ARE> brought under a planar block!"));
				}
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> gesture(s) at <T-NAMESELF>, but nothing happens."));

		// return whether it worked
		return success;
	}
}
