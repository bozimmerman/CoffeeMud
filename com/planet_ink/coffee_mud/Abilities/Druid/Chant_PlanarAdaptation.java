package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.PlanarAbility.PlanarVar;
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
   Copyright 2020-2020 Bo Zimmerman

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
public class Chant_PlanarAdaptation extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_PlanarAdaptation";
	}

	private final static String	localizedName	= CMLib.lang().L("Planar Adaptation");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Planar Adaptation)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_COSMOLOGY;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	protected int[]	lastSet	= null;
	protected int[]	newSet	= null;
	protected int[] fixSet	= null;

	@Override
	public void unInvoke()
	{
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
			mob.tell(L("Your planar adaptations revert."));
	}

	protected int[] getBreatheSet(final MOB mob)
	{
		int[] fixSet = new int[0];
		final Room R=CMLib.map().roomLocation(mob);
		if(R==null)
			return fixSet;
		final String planeName = CMLib.flags().getPlaneOfExistence(R);
		if(planeName != null)
		{
			final PlanarAbility planeA=(PlanarAbility)CMClass.getAbility("StdPlanarAbility");
			final Map<String,String> planeVars = planeA.getPlanarVars(planeName);
			final String atmosphere = planeVars.get(PlanarVar.ATMOSPHERE.toString());
			if(atmosphere!=null)
			{
				if(atmosphere.length()>0)
				{
					final int atmo=RawMaterial.CODES.FIND_IgnoreCase(atmosphere);
					if(atmo > 0)
						fixSet = new int[] {atmo};
				}
			}
		}
		return fixSet;
	}

	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		final int[] breatheables=affectableStats.getBreathables();
		if(breatheables.length==0)
			return;
		if((lastSet!=breatheables)||(newSet==null))
		{
			if(fixSet == null)
				fixSet = getBreatheSet(affected);
			if(fixSet.length>0)
			{
				newSet=Arrays.copyOf(affectableStats.getBreathables(),affectableStats.getBreathables().length+fixSet.length);
				for(int i=0;i<fixSet.length;i++)
					newSet[newSet.length-1-i]=fixSet[i];
				Arrays.sort(newSet);
			}
			else
				newSet=breatheables;
			lastSet=breatheables;
		}
		affectableStats.setBreathables(newSet);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(CMLib.flags().getPlaneOfExistence(target.location())==null)
		{
			mob.tell(L("This chant requires being on another plane of existence."));
			return false;
		}

		if(target.fetchEffect(this.ID())!=null)
		{
			final Chant_PlanarAdaptation adaptA = (Chant_PlanarAdaptation)target.fetchEffect(ID());
			if(adaptA == null)
				return false;
			if(this.getBreatheSet(mob) == adaptA.fixSet)
			{
				mob.tell(target,null,null,L("<S-NAME> <S-IS-ARE> already adapted to this plane of existence."));
				return false;
			}
			else
				adaptA.unInvoke();
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> chant(s) to <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> adapt(s) to this plane of existence!"));
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,L("<S-NAME> chant(s) to <T-NAMESELF>, but nothing happens."));

		return success;
	}
}
