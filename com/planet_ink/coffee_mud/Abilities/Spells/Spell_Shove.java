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
   Copyright 2003-2018 Bo Zimmerman

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

public class Spell_Shove extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_Shove";
	}

	private final static String localizedName = CMLib.lang().L("Shove");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Shoved Down)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int maxRange()
	{
		return adjustedMaxInvokerRange(4);
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
		return Tickable.TICKID_MOB;
	}

	public boolean doneTicking=false;

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_EVOCATION;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_MOVING;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		int dir=-1;
		if(commands.size()>0)
		{
			dir=CMLib.directions().getGoodDirectionCode(commands.get(commands.size()-1));
			commands.remove(commands.size()-1);
		}
		if(dir<0)
		{
			if(mob.isMonster())
			{
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
					if((mob.location().getRoomInDir(d)!=null)
					&&(mob.location().getExitInDir(d)!=null)
					&&(mob.location().getExitInDir(d).isOpen()))
						dir=d;
			}
			if(dir<0)
			{
				mob.tell(L("Shove whom which direction?  Try north, south, east, or west..."));
				return false;
			}
		}
		if((mob.location().getRoomInDir(dir)==null)
		   ||(mob.location().getExitInDir(dir)==null)
		   ||(!mob.location().getExitInDir(dir).isOpen()))
		{
			mob.tell(L("You can't shove anyone that way!"));
			return false;
		}

		final MOB target=getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?L("<T-NAME> get(s) shoved back!"):L("<S-NAME> incant(s) and shove(s) at <T-NAMESELF>."));
			if((mob.location().okMessage(mob,msg))&&(target.fetchEffect(this.ID())==null))
			{
				if((msg.value()<=0)&&(target.location()==mob.location()))
				{
					mob.location().send(mob,msg);
					target.makePeace(true);
					final Room newRoom=mob.location().getRoomInDir(dir);
					final Room thisRoom=mob.location();
					final CMMsg enterMsg=CMClass.getMsg(target,newRoom,this,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,L("<S-NAME> fly(s) in from @x1.",CMLib.directions().getFromCompassDirectionName(Directions.getOpDirectionCode(dir))));
					final CMMsg leaveMsg=CMClass.getMsg(target,thisRoom,this,CMMsg.MSG_LEAVE|CMMsg.MASK_MAGIC,L("<S-NAME> <S-IS-ARE> shoved forcefully into the air and out @x1.",CMLib.directions().getInDirectionName(dir)));
					if(thisRoom.okMessage(target,leaveMsg)&&newRoom.okMessage(target,enterMsg))
					{
						thisRoom.send(target,leaveMsg);
						newRoom.bringMobHere(target,false);
						newRoom.send(target,enterMsg);
						target.tell(L("\n\r\n\r"));
						CMLib.commands().postLook(target,true);
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,null,L("<S-NAME> incant(s), but nothing seems to happen."));

		// return whether it worked
		return success;
	}
}
