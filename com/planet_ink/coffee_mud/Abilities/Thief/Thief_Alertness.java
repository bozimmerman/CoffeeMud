package com.planet_ink.coffee_mud.Abilities.Thief;
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

public class Thief_Alertness extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_Alertness";
	}

	private final static String localizedName = CMLib.lang().L("Alertness");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Alertness)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[] triggerStrings =I(new String[] {"ALERTNESS"});
	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_ALERT;
	}

	@Override
	public boolean disregardsArmorCheck(MOB mob)
	{
		return true;
	}

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}
	Room room=null;

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(affected instanceof MOB)
		{

			final MOB mob=(MOB)affected;
			if(!CMLib.flags().isAliveAwakeMobile(mob,true))
			{
				unInvoke();
				return false;
			}
			if(mob.location()!=room)
			{
				room=mob.location();
				Vector<Item> choices=null;
				for(int i=0;i<room.numItems();i++)
				{
					final Item I=room.getItem(i);
					if((I!=null)
					&&(CMLib.flags().canBeSeenBy(I,mob))
					&&(I.displayText().length()==0))
					{
						if(choices==null)
							choices=new Vector<Item>();
						choices.addElement(I);
					}
				}
				if(choices!=null)
				{
					int alert=getXLEVELLevel(mob);
					if(alert<=0)
						alert=1;
					while((alert>0)&&(choices.size()>0))
					{
						final Item I=choices.elementAt(CMLib.dice().roll(1,choices.size(),-1));
						choices.removeElement(I);
						mob.tell(I.name(mob)+": "+I.description());
						alert--;
					}
				}
			}
		}
		return true;
	}

	@Override
	public void unInvoke()
	{
		final MOB M=(MOB)affected;
		super.unInvoke();
		if((M!=null)&&(!M.amDead())&&(super.canBeUninvoked()))
			M.tell(L("You don't feel quite so alert any more."));
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,L("<S-NAME> <S-IS-ARE> already alert."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		final CMMsg msg=CMClass.getMsg(mob,target,this,auto?CMMsg.MSG_OK_ACTION:(CMMsg.MSG_DELICATE_HANDS_ACT|CMMsg.MASK_EYES),auto?L("<T-NAME> become(s) alert."):L("<S-NAME> become(s) suddenly alert."));
		if(!success)
			return beneficialVisualFizzle(mob,null,L("<S-NAME> attempt(s) to look alert, but become(s) distracted."));
		else
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,target,asLevel,0);
		}
		return success;
	}
}
