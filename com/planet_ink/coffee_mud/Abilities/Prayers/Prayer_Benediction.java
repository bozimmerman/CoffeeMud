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
   Copyright 2004-2021 Bo Zimmerman

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
public class Prayer_Benediction extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_Benediction";
	}

	private final static String localizedName = CMLib.lang().L("Benediction");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Benediction)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_COMMUNING;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_OTHERS;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_HOLY;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_MOBS;
	}

	protected Integer[] whichStats = null;
	protected MOB amtAffected = null;
	protected int amt=-1;
	
	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(invoker==null)
			return;

		if((amt<0)||(amtAffected!=affected)||(whichStats==null))
		{
			final MOB mob=affected;
			int pts=adjustedLevel(invoker(),0)/5;
			if((invoker().isPlayer())
			&&(invoker()!=affected)
			&&(pts > (affected.phyStats().level()/5)+CMProps.getIntVar(CMProps.Int.EXPRATE)))
				pts= (affected.phyStats().level()/5)+CMProps.getIntVar(CMProps.Int.EXPRATE);
			CharStats chk=(CharStats)CMClass.getCommon("DefaultCharStats"); 
			chk.setAllBaseValues(0);
			chk.setCurrentClass(mob.charStats().getCurrentClass());
			for(int c=0;c<mob.charStats().numClasses();c++)
				mob.charStats().getMyClass(c).affectCharStats(mob,chk);
			List<Integer> whichOnes = new ArrayList<Integer>();
			for(final int i: CharStats.CODES.MAXCODES())
			{
				if(chk.getStat(i)>0)
					whichOnes.add(Integer.valueOf(i));
			}
			whichStats = whichOnes.toArray(new Integer[whichOnes.size()]);
			amtAffected=affected;
			amt=pts/whichStats.length;
		}
		for(final Integer I : whichStats)
			affectableStats.setStat(I.intValue(),affectableStats.getStat(I.intValue())+amt);
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
			mob.tell(L("Your benediction fades."));
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?L("<T-NAME> become(s) filled with benediction!"):L("^S<S-NAME> @x1 for a benediction over <T-NAMESELF>!^?",prayWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> @x1 for a benediction over <T-YOUPOSS>  but there is no answer.",prayWord(mob)));

		// return whether it worked
		return success;
	}
}
