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
public class Prayer_Disown extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_Disown";
	}

	private final static String	localizedName	= CMLib.lang().L("Disown");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER | Ability.DOMAIN_CORRUPTION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_UNHOLY;
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(mob.isInCombat())
				return Ability.QUALITY_INDIFFERENT;
			if(mob.isMonster())
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	public List<Tattoo> getParentTattoos(final MOB mob)
	{
		final LinkedList<Tattoo> list=new LinkedList<Tattoo>();
		for(Enumeration<Tattoo> t= mob.tattoos();t.hasMoreElements();)
		{
			final Tattoo T=t.nextElement();
			if(T.getTattooName().startsWith("PARENT:"))
				list.add(T);
		}
		return list;
	}
	
	public void delEffect(final MOB M, String effectID)
	{
		final Ability A=M.fetchEffect(effectID);
		if(A!=null)
			M.delEffect(A);
	}
	
	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(mob.isInCombat())
		{
			mob.tell(L("Not while you're fighting!"));
			return false;
		}
		if((!auto)&&(commands.size()<1))
		{
			mob.tell(L("Disown whom?"));
			return false;
		}
		final Physical target=getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_UNWORNONLY);
		if(target==null)
			return false;
		
		if((target instanceof CagedAnimal)
		&&(target.isGeneric())
		&&((target.phyStats().ability()>0))
		&&(target.fetchEffect("Age")!=null))
		{
			// all good.. we gots a baby!
		}
		else
		if((target instanceof MOB)
		&&(((MOB)target).fetchEffect("Age")!=null)
		&&(((MOB)target).fetchEffect("Prop_SafePET")!=null)
		&&(getParentTattoos((MOB)target).size()>0)
		&&(target.isGeneric()))
		{
			if(!mob.getGroupMembers(new HashSet<MOB>()).contains(target))
			{
				mob.tell(L("@x1 is not in your group.",target.name(mob)));
				return false;
			}
			// all good.. we gots a toddler!
		}
		else
		{
			mob.tell(L("You may only disown a legitimate child."));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?L("<T-NAME> becomes disowned."):L("^S<S-NAME> disown(s) <T-NAMESELF> and renders <T-HIM-HER> disavowed.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(target instanceof MOB)
				{
					final MOB M=(MOB)target;
					char gender=(char)M.baseCharStats().getStat(CharStats.STAT_GENDER);
					final Race R=M.baseCharStats().getMyRace();
					String name = CMLib.english().startWithAorAn(R.makeMobName(gender, 2)).toLowerCase();
					M.setName(name);
					M.setDisplayText(L("@x1 is here",name));
					delEffect(M,"Prop_SafePET");
					delEffect(M,"Age");
					final List<Tattoo> parents=getParentTattoos((MOB)target);
					for(final Tattoo T : parents)
						((MOB)target).delTattoo(T);
				}
				else
				if(target instanceof CagedAnimal)
				{
					final MOB M=((CagedAnimal)target).unCageMe();
					delEffect(M,"Prop_SafePET");
					delEffect(M,"Age");
					final List<Tattoo> parents=getParentTattoos(M);
					for(final Tattoo T : parents)
						M.delTattoo(T);
					char gender=(char)M.baseCharStats().getStat(CharStats.STAT_GENDER);
					final Race R=M.baseCharStats().getMyRace();
					String name = CMLib.english().startWithAorAn(R.makeMobName(gender, 2)).toLowerCase();
					M.setName(name);
					M.setDisplayText(L("@x1 is here",name));
					if(CMLib.flags().isEggLayer(mob.charStats().getMyRace()))
						name = CMLib.english().startWithAorAn(L("@x1 egg",R.name())).toLowerCase();
					else
						name = CMLib.english().startWithAorAn(R.makeMobName(gender, 0)).toLowerCase();
					target.setName(name);
					target.setDisplayText(L("@x1 is here",name));
					((CagedAnimal)target).cageMe(M);
					M.destroy();
					target.basePhyStats().setAbility(0);
					target.phyStats().setAbility(0);
				}
				CMLib.leveler().postExperience(mob,null,null,5,false);
			}
		}
		else
			beneficialWordsFizzle(mob,target,L("<S-NAME> @x1 over <T-NAME>, but lose(s) <S-HIS-HER> concentration.",prayWord(mob)));
		// return whether it worked
		return success;
	}
}
