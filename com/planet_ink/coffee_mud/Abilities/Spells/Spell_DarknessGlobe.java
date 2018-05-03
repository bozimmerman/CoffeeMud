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
   Copyright 2016-2018 Bo Zimmerman

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

public class Spell_DarknessGlobe extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_DarknessGlobe";
	}

	private final static String	localizedName	= CMLib.lang().L("Darkness Globe");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Darkness Globe)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
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

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL | Ability.DOMAIN_EVOCATION;
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(CMLib.flags().canSeeInDark(mob))
				return super.castingQuality(mob, target,Ability.QUALITY_BENEFICIAL_SELF);
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean bubbleAffect()
	{
		return true;
	}

	protected volatile boolean norecurse = false;
	
	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		final Room R=CMLib.map().roomLocation(affected);
		if((R!=null) && (!norecurse))
		{
			if(!CMath.bset(R.phyStats().disposition(),PhyStats.IS_DARK))
				R.phyStats().setDisposition(R.phyStats().disposition()|PhyStats.IS_DARK);
			if(CMath.bset(R.phyStats().disposition(),PhyStats.IS_LIGHTSOURCE))
				R.phyStats().setDisposition(R.phyStats().disposition()-PhyStats.IS_LIGHTSOURCE);
			synchronized(this)
			{
				norecurse = true;
				try
				{
					for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
						m.nextElement().recoverPhyStats();
				}
				finally
				{
					norecurse=false;
				}
			}
		}
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		final Room room=CMLib.map().roomLocation(affected);
		if(canBeUninvoked()&&(room!=null)&&(affected instanceof MOB))
			room.show((MOB)affected,null,CMMsg.MSG_OK_VISUAL,L("The darkness globe around <S-NAME> vanishes."));
		super.unInvoke();
		if(canBeUninvoked()&&(room!=null))
			room.recoverRoomStats();
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,L("<S-NAME> already <S-HAS-HAVE> a darkness globe around <S-HIM-HER>."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		final Room room=mob.location();
		if((success)&&(room!=null))
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),
					auto?L("^S<S-NAME> attain(s) a globe of darkness around <S-HIM-HER>!")
						:L("^S<S-NAME> invoke(s) a darkness globe all around <S-HIM-HER>, enveloping everything!^?"));
			if(room.okMessage(mob,msg))
			{
				room.send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
				room.recoverRoomStats();
			}
		}
		else
			beneficialWordsFizzle(mob,mob.location(),L("<S-NAME> attempt(s) to invoke darkness, but fail(s)."));

		return success;
	}
}
