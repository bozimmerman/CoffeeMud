package com.planet_ink.coffee_mud.Abilities.Prayers;
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
   Copyright 2001-2018 Bo Zimmerman

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

public class Prayer_Freedom extends Prayer implements MendingSkill
{
	@Override
	public String ID()
	{
		return "Prayer_Freedom";
	}

	private final static String localizedName = CMLib.lang().L("Freedom");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_RESTORATION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_HOLY;
	}

	@Override
	public boolean supportsMending(Physical item)
	{
		if(!(item instanceof MOB))
			return false;
		final MOB caster=CMClass.getFactoryMOB();
		caster.basePhyStats().setLevel(CMProps.getIntVar(CMProps.Int.LASTPLAYERLEVEL));
		caster.phyStats().setLevel(CMProps.getIntVar(CMProps.Int.LASTPLAYERLEVEL));
		final boolean canMend=returnOffensiveAffects(caster,item).size()>0;
		caster.destroy();
		return canMend;
	}

	public List<Ability> returnOffensiveAffects(MOB caster, Physical fromMe)
	{
		final MOB newMOB=CMClass.getFactoryMOB();
		final Vector<Ability> offenders=new Vector<Ability>(1);

		final CMMsg msg=CMClass.getMsg(newMOB,null,null,CMMsg.MSG_SIT,null);
		for(int a=0;a<fromMe.numEffects();a++) // personal
		{
			final Ability A=fromMe.fetchEffect(a);
			if(A!=null)
			{
				try
				{
					newMOB.recoverPhyStats();
					A.affectPhyStats(newMOB,newMOB.phyStats());
					final int clas=A.classificationCode()&Ability.ALL_ACODES;
					if((!CMLib.flags().isAliveAwakeMobileUnbound(newMOB,true))
					   ||(CMath.bset(A.flags(),Ability.FLAG_BINDING))
					   ||(!A.okMessage(newMOB,msg)))
					if((A.invoker()==null)
					||((clas!=Ability.ACODE_SPELL)&&(clas!=Ability.ACODE_CHANT)&&(clas!=Ability.ACODE_PRAYER)&&(clas!=Ability.ACODE_SONG))
					||((A.invoker()!=null)
						&&(A.invoker().phyStats().level()<=(caster.phyStats().level()+1+(2*getXLEVELLevel(caster))))))
						  offenders.addElement(A);
				}
				catch(final Exception e)
				{
				}
			}
		}
		newMOB.destroy();
		return offenders;
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(target instanceof MOB)
			{
				if(supportsMending(target))
					return super.castingQuality(mob, target,Ability.QUALITY_BENEFICIAL_OTHERS);
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		final List<Ability> offensiveAffects=returnOffensiveAffects(mob,target);

		if((success)&&(offensiveAffects.size()>0))
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?L("<T-NAME> feel(s) lightly touched."):L("^S<S-NAME> @x1 to deliver a light unbinding touch to <T-NAMESELF>.^?",prayForWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				for(int a=offensiveAffects.size()-1;a>=0;a--)
					offensiveAffects.get(a).unInvoke();
				if(!CMLib.flags().isStillAffectedBy(target,offensiveAffects,false))
					target.tell(L("You feel less constricted!"));
			}
		}
		else
			this.beneficialWordsFizzle(mob,target,L("<S-NAME> @x1 for <T-NAMESELF>, but nothing happens.",prayWord(mob)));
		// return whether it worked
		return success;
	}
}
