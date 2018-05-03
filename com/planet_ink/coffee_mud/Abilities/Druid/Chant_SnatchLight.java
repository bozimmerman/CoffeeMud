package com.planet_ink.coffee_mud.Abilities.Druid;
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
   Copyright 2004-2018 Bo Zimmerman

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

public class Chant_SnatchLight extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_SnatchLight";
	}

	private final static String localizedName = CMLib.lang().L("Snatch Light");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Snatch Light)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT|Ability.DOMAIN_DEEPMAGIC;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	public Room snatchLocation()
	{
		if((invoker!=null)
		&&CMLib.flags().isInTheGame(invoker,false)
		&&(invoker.fetchEffect(ID())!=null))
		   return invoker.location();
		return null;
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(CMLib.map().roomLocation(affected)==snatchLocation())
		{
			affectableStats.setDisposition(affectableStats.disposition() |  PhyStats.IS_DARK);
			if(CMath.bset(affectableStats.disposition(),PhyStats.IS_LIGHTSOURCE))
				affectableStats.setDisposition(CMath.unsetb(affectableStats.disposition(),PhyStats.IS_LIGHTSOURCE));
			if(CMath.bset(affectableStats.disposition(),PhyStats.IS_GLOWING))
				affectableStats.setDisposition(CMath.unsetb(affectableStats.disposition(),PhyStats.IS_GLOWING));
		}
		else
			unInvoke();
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((affected==invoker)&&(invoker!=null))
		{
			final MOB mob=invoker;
			final Room R=mob.location();
			if(R==null)
				return true;
			if(R.fetchEffect(ID())==null)
			{
				final Ability A=(Ability)copyOf();
				A.setSavable(false);
				R.addEffect(A);
			}
			for(int m=0;m<R.numInhabitants();m++)
			{
				final MOB M=R.fetchInhabitant(m);
				if((M!=null)&&(CMLib.flags().isGlowing(M)||CMLib.flags().isLightSource(M))&&(M.fetchEffect(ID())==null))
				{
					final Ability A=(Ability)copyOf();
					A.setSavable(false);
					M.addEffect(A);
				}
				if(M!=null)
				for(int i=0;i<M.numItems();i++)
				{
					final Item I=M.getItem(i);
					if((I!=null)&&(I.container()==null)&&(CMLib.flags().isGlowing(I)||CMLib.flags().isLightSource(I))&&(I.fetchEffect(ID())==null))
					{
						final Ability A=(Ability)copyOf();
						A.setSavable(false);
						I.addEffect(A);
					}
				}
			}
			for(int i=0;i<R.numItems();i++)
			{
				final Item I=R.getItem(i);
				if((I!=null)&&(CMLib.flags().isGlowing(I)||CMLib.flags().isLightSource(I))&&(I.fetchEffect(ID())==null))
				{
					final Ability A=(Ability)copyOf();
					A.setSavable(false);
					I.addEffect(A);
				}
			}
			R.recoverRoomStats();
			R.recoverRoomStats();
		}
		else
		if(affected!=null)
		{
			final Room R=CMLib.map().roomLocation(affected);
			if((invoker==null)||(R!=invoker.location()))
			{
				unInvoke();
				return false;
			}
		}
		return true;
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(invoker==null)||(affected!=invoker))
		{
			final Environmental E=affected;
			final MOB oldI=invoker;
			super.unInvoke();
			if(E!=null)
			{
				if(E instanceof MOB)
				{
					final MOB M=(MOB)E;
					for(int i=0;i<M.numItems();i++)
					{
						final Item I=M.getItem(i);
						if(I!=null)
						{
							final Ability A=I.fetchEffect(ID());
							if((A!=null)&&(A.invoker()==oldI))
								A.unInvoke();
						}
					}
				}
				final Room R=CMLib.map().roomLocation(E);
				if(R!=null)
					R.recoverRoomStats();
			}
			return;
		}
		final MOB mob=invoker;
		super.unInvoke();

		if((canBeUninvoked())&&(mob!=null))
		{
			mob.tell(L("Your ability to snatch light dissipates."));
			if(mob.location()!=null)
			{
				mob.location().recoverRoomStats();
				mob.location().recoverRoomStats();
			}
		}
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(target,null,null,L("<S-NAME> <S-IS-ARE> already snatching light."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?L("<S-NAME> gain(s) an aura of light snatching!"):L("^S<S-NAME> chant(s), feeling <S-HIS-HER> body become a light snatcher!^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> chant(s), but nothing more happens."));

		// return whether it worked
		return success;
	}
}
