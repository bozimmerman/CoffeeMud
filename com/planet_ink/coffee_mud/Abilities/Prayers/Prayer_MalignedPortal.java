package com.planet_ink.coffee_mud.Abilities.Prayers;
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
public class Prayer_MalignedPortal extends Prayer
{

	@Override
	public String ID()
	{
		return "Spell_Portal";
	}

	private final static String localizedName = CMLib.lang().L("Maligned Portal");

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
		return Ability.ACODE_PRAYER|Ability.DOMAIN_COSMOLOGY;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_UNHOLY;
	}


	@Override
	protected int overrideMana()
	{
		return Ability.COST_ALL-90;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	protected volatile Room newRoom=null;
	protected volatile Room oldRoom=null;

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if(newRoom!=null)
			{
				newRoom.showHappens(CMMsg.MSG_OK_VISUAL,L("The swirling portal closes."));
				newRoom.rawDoors()[Directions.GATE]=null;
				newRoom.setRawExit(Directions.GATE,null);
			}
			if(oldRoom!=null)
			{
				oldRoom.showHappens(CMMsg.MSG_OK_VISUAL,L("The swirling portal closes."));
				oldRoom.rawDoors()[Directions.GATE]=null;
				oldRoom.setRawExit(Directions.GATE,null);
			}
		}
		super.unInvoke();
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		newRoom=null;
		oldRoom=mob.location();
		if(oldRoom == null)
			return false;

		if(newRoom==null)
		{
			mob.tell(L("You don't know of a place called '@x1'.",CMParms.combine(commands,0)));
			return false;
		}

		if((oldRoom.getRoomInDir(Directions.GATE)!=null)
		||(oldRoom.getExitInDir(Directions.GATE)!=null))
		{
			mob.tell(L("A portal cannot be created here."));
			return false;
		}
		
		PlanarAbility planeAble = (PlanarAbility)CMClass.getAbility("StdPlanarAbility");
		String planeName = "";
		final List<String> choices = new ArrayList<String>();
		for(final String planeKey : planeAble.getAllPlaneKeys())
		{
			final Map<String,String> planeVars = planeAble.getPlanarVars(planeKey);
			final String catStr=planeVars.get(PlanarVar.CATEGORY.toString());
			final List<String> categories=CMParms.parseCommas(catStr.toLowerCase(), true);
			if(categories.contains("lower"))
				choices.add(CMStrings.capitalizeAllFirstLettersAndLower(planeKey));
		}
		if(choices.size()==0)
		{
			mob.tell(L("There is nowhere to portal to."));
			return false;
		}
		planeName = choices.get(CMLib.dice().roll(1, choices.size(), -1));

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final int profNeg = 0; // 
		
		final boolean success=proficiencyCheck(mob,-profNeg,auto);

		if((success)
		&&((newRoom.getRoomInDir(Directions.GATE)==null)
		&&(newRoom.getExitInDir(Directions.GATE)==null)))
		{
			final CMMsg msg=CMClass.getMsg(mob,oldRoom,this,verbalCastCode(mob,oldRoom,auto),L("^S<S-NAME> evoke(s) a blinding, swirling portal here.^?"));
			final CMMsg msg2=CMClass.getMsg(mob,newRoom,this,verbalCastCode(mob,newRoom,auto),L("A blinding, swirling portal appears here."));
			if((oldRoom.okMessage(mob,msg))&&(newRoom.okMessage(mob,msg2)))
			{
				oldRoom.send(mob,msg);
				newRoom=(Room)msg2.target();
				newRoom.send(mob,msg2);
				final Exit e=CMClass.getExit("GenExit");
				e.setDescription(L("A swirling portal to somewhere"));
				e.setDisplayText(L("A swirling portal to somewhere"));
				e.setDoorsNLocks(false,true,false,false,false,false);
				e.setExitParams("portal","close","open","closed.");
				e.setName(L("a swirling portal"));
				final Ability A1=CMClass.getAbility("Prop_RoomView");
				if(A1!=null)
				{
					A1.setMiscText(CMLib.map().getExtendedRoomID(newRoom));
					e.addNonUninvokableEffect(A1);
				}
				final Exit e2=(Exit)e.copyOf();
				final Ability A2=CMClass.getAbility("Prop_RoomView");
				if(A2!=null)
				{
					A2.setMiscText(CMLib.map().getExtendedRoomID(oldRoom));
					e2.addNonUninvokableEffect(A2);
				}
				oldRoom.rawDoors()[Directions.GATE]=newRoom;
				newRoom.rawDoors()[Directions.GATE]=oldRoom;
				oldRoom.setRawExit(Directions.GATE,e);
				newRoom.setRawExit(Directions.GATE,e2);
				beneficialAffect(mob,e,asLevel,15);
			}
		}
		else
			beneficialWordsFizzle(mob,null,L("<S-NAME> attempt(s) to evoke a portal, but fizzle(s) the spell."));

		// return whether it worked
		return success;
	}
}
