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
   Copyright 2020-2025 Bo Zimmerman

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
public class Prayer_AttuneScroll extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_AttuneScroll";
	}

	private final static String localizedName = CMLib.lang().L("Attune Scroll");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_COMMUNING;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_HOLY;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_ITEMS;
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if((commands.size()<1)&&(givenTarget==null))
		{
			mob.tell(L("Attune to what?"));
			return false;
		}
		final Item target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_ANY);
		if(target==null)
			return false;

		if(!(target instanceof Scroll))
		{
			mob.tell(L("You can't attune to that."));
			return false;
		}

		if(((Scroll)target).getSpells().size()==0)
		{
			mob.tell(L("That lacks any divine magic to attune to."));
			return false;
		}

		boolean isDivine=false;
		for(final Ability A : ((Scroll)target).getSpells())
		{
			if((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
				isDivine=true;
		}
		if(!isDivine)
		{
			mob.tell(L("That does not appear to be a divine scroll."));
			return false;
		}

		if(target.usesRemaining()<=0)
		{
			mob.tell(L("That is too depleted to be attuned to."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if((target!=null)&&(success))
		{
			final Room R=mob.location();
			if(R==null)
				return false;
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?L("<T-NAME> become(s) attuned to."):L("^S<S-NAME> @x1, sweeping <S-HIS-HER> hands over <T-NAMESELF>.^?",prayWord(mob)));
			if(R.okMessage(mob,msg))
			{
				R.send(mob,msg);
				final List<Ability> spells=((Scroll)target).getSpells();
				final List<Ability> mySpells = new ArrayList<Ability>();
				for(final Ability A : spells)
				{
					if(((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
					&&(A.appropriateToMyFactions(mob)))
						mySpells.add(A);
				}
				if((mySpells.size()>0)
				&&(target.usesRemaining()>0))
				{
					MOB M=null;
					try
					{
						M=CMClass.getFactoryMOB(target.Name(), mob.phyStats().level(), R);
						R.addInhabitant(M);
						for(final Ability A : mySpells)
						{
							M.addAbility((Ability)A.copyOf());
							final CMMsg cmsg=CMClass.getMsg(M,null,A,CMMsg.TYP_CAST_SPELL,L("You feel @x1 release <O-NAME>",target.Name()),CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null);
							R.send(mob, cmsg);
						}
					}
					finally
					{
						if(M!=null)
						{
							R.delInhabitant(M);
							while(R.isInhabitant(M))
								R.delInhabitant(M);
							M.destroy();
						}
					}
				}
				if(target.usesRemaining()>0)
					target.setUsesRemaining(target.usesRemaining()-1);
			}
			else
				beneficialWordsFizzle(mob,target,auto?"":L("<S-NAME> sweep(s) <S-HIS-HER> hands over <T-NAMESELF>, but @x1 does not heed.",hisHerDiety(mob)));
		}

		// return whether it worked
		return success;
	}
}
