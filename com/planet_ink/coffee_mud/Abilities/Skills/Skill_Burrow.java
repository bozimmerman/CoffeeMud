package com.planet_ink.coffee_mud.Abilities.Skills;
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
   Copyright 2023-2024 Bo Zimmerman

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
public class Skill_Burrow extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_Burrow";
	}

	private final static String	localizedName	= CMLib.lang().L("Burrow");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedDisplay	= CMLib.lang().L("Digging a Burrow");

	@Override
	public String displayText()
	{
		return localizedDisplay;
	}

	protected int burrowDirection = -1;
	protected int asLevel = -1;
	protected boolean aborted = false;
	protected Room activityRoom = null;

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS|CAN_ROOMS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_ROOMS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_SELF;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_RACIALABILITY;
	}

	protected boolean isDiggableRoom(final Room R)
	{
		if(R==null)
			return false;
		if(((R.domainType()&Room.INDOORS)>0)
		&&(R.domainType()!=Room.DOMAIN_INDOORS_CAVE)
		&&((R.getAtmosphere()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_ROCK))
			return false;
		if((CMLib.flags().isACityRoom(R))
		   ||(R.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
		   ||(R.domainType()==Room.DOMAIN_OUTDOORS_AIR)
		   ||(R.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
			return false;
		return true;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((affected instanceof MOB)
		&&(tickID==Tickable.TICKID_MOB))
		{
			final MOB mob=(MOB)affected;
			final Room R=mob.location();
			if((mob.isInCombat())
			||(R!=activityRoom)
			||(!CMLib.flags().isAliveAwakeMobileUnbound(mob,true)))
			{
				aborted=true;
				unInvoke();
				return false;
			}
			if(tickDown==2)
			{
				if(!R.show(mob,null,CMMsg.MSG_NOISYMOVEMENT,L("You are almost done digging your burrow.")))
				{
					aborted=true;
					unInvoke();
					return false;
				}
			}
			if((mob.soulMate()==null)
			&&(mob.playerStats()!=null)
			&&(R!=null)
			&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.HYGIENE)))
				mob.playerStats().adjHygiene(PlayerStats.HYGIENE_COMMONDIRTY);
		}
		return super.tick(ticking,tickID);
	}

	protected Room makeBurrow(final MOB mob, final int asLevel)
	{
		final Room newRoom=CMClass.getLocale("CaveRoom");
		newRoom.setDisplayText(L("A grimey burrow"));
		newRoom.setDescription(L("You are in a dark dirty burrow!"));
		final Area areaA=mob.location().getArea();
		newRoom.setArea(areaA);
		newRoom.setRoomID(areaA.getNewRoomID(newRoom,Directions.UP));
		newRoom.setSavable(false);
		mob.location().rawDoors()[this.burrowDirection]=newRoom;
		mob.location().setRawExit(this.burrowDirection,CMClass.getExit("HiddenWalkway"));
		newRoom.rawDoors()[Directions.getOpDirectionCode(this.burrowDirection)]=mob.location();
		final Ability A=CMClass.getAbility("Prop_RoomView");
		A.setMiscText(CMLib.map().getExtendedRoomID(mob.location()));
		final Exit E=CMClass.getExit("Open");
		E.addNonUninvokableEffect(A);

		newRoom.setRawExit(Directions.getOpDirectionCode(this.burrowDirection),E);
		newRoom.getArea().fillInAreaRoom(newRoom);
		beneficialAffect(mob,mob.location(),asLevel,CMProps.getIntVar(CMProps.Int.TICKSPERMUDMONTH));
		mob.location().showHappens(CMMsg.MSG_OK_VISUAL,L("Your burrow has been dug to the @x1!",
				CMLib.directions().getDirectionName(this.burrowDirection)));
		return newRoom;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if((myHost instanceof MOB)&&(myHost == this.affected)&&(((MOB)myHost).location()!=null))
		{
			if((msg.sourceMinor()==CMMsg.TYP_SHUTDOWN)
			||((msg.sourceMinor()==CMMsg.TYP_QUIT)&&(msg.amISource((MOB)myHost))))
			{
				aborted=true;
				unInvoke();
			}
		}
		return true;
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(affected==null)
			return;
		if(canBeUninvoked())
		{
			if(affected instanceof Room)
			{
				final Room room=(Room)affected;
				if((canBeUninvoked())&&(burrowDirection >= 0))
				{
					final int burrowDirection=this.burrowDirection;
					this.burrowDirection=-1;
					final Room R=room.getRoomInDir(burrowDirection);
					if((R!=null)&&(R.roomID().equalsIgnoreCase("")))
					{
						R.showHappens(CMMsg.MSG_OK_VISUAL,L("The burrow fills itself in..."));
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
			}
			else
			if(affected instanceof MOB)
			{
				if(!aborted)
				{
					final Room R = makeBurrow((MOB)affected,this.asLevel);
					if(R != null)
						super.beneficialAffect((MOB)affected, R, this.asLevel, 0);
				}
			}
		}
		super.unInvoke();
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Physical target = mob.location();
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(L("There is already a burrow here!"));
			return false;
		}
		if(!isDiggableRoom(mob.location()))
		{
			mob.tell(L("You can't dig a burrow here."));
			return false;
		}
		final List<Integer> dirChoices=new ArrayList<Integer>();
		final List<String> dirSChoices = new ArrayList<String>();
		for(final int dir : Directions.CODES())
		{
			if((mob.location().getRoomInDir(dir)==null)
			&&(dir != Directions.UP))
			{
				dirChoices.add(Integer.valueOf(dir));
				dirSChoices.add(CMLib.directions().getDirectionName(dir, Directions.DirType.COMPASS));
			}
		}
		if(dirChoices.size()==0)
		{
			mob.tell(L("There is nowhere to dig a burrow here."));
			return false;
		}
		final int d;
		if(mob.isMonster())
			d = dirChoices.get(CMLib.dice().roll(1,dirChoices.size(),-1)).intValue();
		else
		{
			final String str = CMParms.combine(commands, 0).trim();
			if((str.length()==0)&&(!auto))
			{
				mob.tell(L("Burrow which direction?  Available directions: @x1",CMLib.english().toEnglishStringList(dirSChoices)));
				return false;
			}
			final int dir = CMLib.directions().getGoodDirectionCode(str, Directions.DirType.COMPASS);
			if(dir >= 0)
				d=dir;
			else
			if(!auto)
			{
				mob.tell(L("'@x1' is not a valid direction?  Available directions: @x1",CMLib.english().toEnglishStringList(dirSChoices)));
				return false;
			}
			else
				d = dirChoices.get(CMLib.dice().roll(1,dirChoices.size(),-1)).intValue();
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg = CMClass.getMsg(mob, null, this, CMMsg.MSG_NOISYMOVEMENT, auto?"":L("^S<S-NAME> start(s) digging a burrow.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				this.aborted=false;
				this.burrowDirection=d;
				this.asLevel = asLevel;
				this.activityRoom = mob.location();
				super.beneficialAffect(mob, mob, CMLib.ableMapper().qualifyingLevel(mob, this), 4);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,L("<S-NAME> attempt(s) to dig a burrow, but can't get started."));

		// return whether it worked
		return success;
	}
}

