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

import java.util.List;
import java.util.Vector;

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

public class Chant_Den extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_Den";
	}

	private final static String localizedName = CMLib.lang().L("Den");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Den)");

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
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ROOMS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_ROOMS;
	}
	
	protected int denDirection = -1;

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(affected==null)
			return;
		if(!(affected instanceof Room))
			return;
		final Room room=(Room)affected;
		if((canBeUninvoked())&&(denDirection >= 0))
		{
			int denDirection=this.denDirection;
			this.denDirection=-1;
			final Room R=room.getRoomInDir(denDirection);
			if((R!=null)&&(R.roomID().equalsIgnoreCase("")))
			{
				R.showHappens(CMMsg.MSG_OK_VISUAL,L("The den fades away..."));
				room.rawDoors()[Directions.UP]=null;
				room.setRawExit(Directions.UP,null);
				if(room.amDestroyed())
					CMLib.map().emptyRoom(R, null, true);
				else
					CMLib.map().emptyRoom(R, room, true);
				R.destroy();
			}
			room.clearSky();
		}
		super.unInvoke();
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Physical target = mob.location();
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(L("There is already a den here!"));
			return false;
		}
		if((mob.location().domainType()!=Room.DOMAIN_INDOORS_CAVE)
		&&((mob.location().getAtmosphere()&RawMaterial.MATERIAL_ROCK)==0))
		{
			mob.tell(L("This magic will only work in a cave."));
			return false;
		}
		if(mob.location().roomID().length()==0)
		{
			mob.tell(L("This magic will not work here."));
			return false;
		}
		final Vector<Integer> dirChoices=new Vector<Integer>();
		for(final int dir : Directions.CODES())
		{
			if(mob.location().getRoomInDir(dir)==null)
				dirChoices.addElement(Integer.valueOf(dir));
		}
		if(dirChoices.size()==0)
		{
			mob.tell(L("This magic will not work here."));
			return false;
		}
		final int d=dirChoices.elementAt(CMLib.dice().roll(1,dirChoices.size(),-1)).intValue();

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{

			final CMMsg msg = CMClass.getMsg(mob, null, this, verbalCastCode(mob,null,auto), auto?"":L("^S<S-NAME> chant(s) for a den!^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().showHappens(CMMsg.MSG_OK_VISUAL,L("Your den, carefully covered, appears to the @x1!",CMLib.directions().getDirectionName(d)));
				final Room newRoom=CMClass.getLocale("CaveRoom");
				newRoom.setDisplayText(L("A musty den"));
				newRoom.setDescription(L("You are in a dark rocky den!"));
				newRoom.setArea(mob.location().getArea());
				mob.location().rawDoors()[d]=newRoom;
				mob.location().setRawExit(d,CMClass.getExit("HiddenWalkway"));
				this.denDirection=d;
				newRoom.rawDoors()[Directions.getOpDirectionCode(d)]=mob.location();
				Ability A=CMClass.getAbility("Prop_RoomView");
				A.setMiscText(CMLib.map().getExtendedRoomID(mob.location()));
				final Exit E=CMClass.getExit("Open");
				E.addNonUninvokableEffect(A);
				A=CMClass.getAbility("Prop_PeaceMaker");
				if(A!=null)
					newRoom.addEffect(A);
				A=CMClass.getAbility("Prop_NoRecall");
				if(A!=null)
					newRoom.addEffect(A);
				A=CMClass.getAbility("Prop_NoSummon");
				if(A!=null)
					newRoom.addEffect(A);
				A=CMClass.getAbility("Prop_NoTeleport");
				if(A!=null)
					newRoom.addEffect(A);
				A=CMClass.getAbility("Prop_NoTeleportOut");
				if(A!=null)
					newRoom.addEffect(A);

				newRoom.setRawExit(Directions.getOpDirectionCode(d),E);
				newRoom.getArea().fillInAreaRoom(newRoom);
				beneficialAffect(mob,mob.location(),asLevel,CMProps.getIntVar(CMProps.Int.TICKSPERMUDMONTH));
			}
		}
		else
			return beneficialWordsFizzle(mob,null,L("<S-NAME> chant(s) for a den, but the magic fades."));

		// return whether it worked
		return success;
	}
}
