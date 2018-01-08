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
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary.TrackingFlag;
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary.TrackingFlags;
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

public class Spell_Lighthouse extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_Lighthouse";
	}

	private final static String	localizedName	= CMLib.lang().L("Lighthouse");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Lighthouse)");

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
			if(!CMLib.flags().canBeSeenBy(mob.location(), mob))
				return super.castingQuality(mob, target,Ability.QUALITY_BENEFICIAL_SELF);
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		if(!(affected instanceof Room))
			affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_LIGHTSOURCE);
		if(CMLib.flags().isInDark(affected))
			affectableStats.setDisposition(affectableStats.disposition()-PhyStats.IS_DARK);
	}

	protected Set<Room> roomSet=new HashSet<Room>();
	protected volatile Room lastRoom=null;
	protected volatile Room lastShipRoom=null;
	protected volatile int lastDir = -1;
	
	public void addToRoom(final Room R, final Item fromShip, final int fromDir)
	{
		if(R==null)
			return;
		final Ability A=CMClass.getAbility("Spell_Light");
		if((A!=null)&&(R.fetchEffect(ID())==null))
		{
			final boolean isInDark=CMLib.flags().isInDark(R);
			A.setInvoker(invoker());
			A.setSavable(false);
			A.setExpirationDate(System.currentTimeMillis()+8000);
			R.addEffect(A);
			R.recoverPhyStats();
			R.recoverRoomStats();
			if(isInDark && (!CMLib.flags().isInDark(R)))
			{
				String dirName;
				if(fromShip != null)
				{
					dirName=fromShip.Name();
					R.showHappens(CMMsg.MSG_OK_VISUAL,L("A bright light shines in from @x1.",dirName));
				}
				else
				if(fromDir >= 0)
				{
					if(R.getArea() instanceof BoardableShip)
						dirName=CMLib.directions().getFromShipDirectionName(fromDir);
					else
						dirName=CMLib.directions().getFromCompassDirectionName(fromDir);
					R.showHappens(CMMsg.MSG_OK_VISUAL,L("A bright light shines in from @x1.",dirName));
				}
				else
				{
					R.showHappens(CMMsg.MSG_OK_VISUAL,L("A bright light shines in."));
				}
			}
		}
	}
	
	public void delFromRoom(final Room R, final Item fromShip, final int fromDir)
	{
		if(R==null)
			return;
		try
		{
			final Ability A=R.fetchEffect("Spell_Light");
			if((A!=null)&&(!A.isSavable())&&(A.invoker()==invoker()))
			{
				final boolean isInDark=CMLib.flags().isInDark(R);
				R.delEffect(A);
				R.recoverPhyStats();
				final boolean darkNow=CMLib.flags().isInDark(R);
				R.addEffect(A);
				R.recoverPhyStats();
				if(!isInDark && darkNow)
				{
					String dirName;
					if(fromShip != null)
					{
						dirName=fromShip.Name();
						R.showHappens(CMMsg.MSG_OK_VISUAL,L("A bright light from @x1 disappears.",dirName));
					}
					else
					if(fromDir >= 0)
					{
						if(R.getArea() instanceof BoardableShip)
							dirName=CMLib.directions().getFromShipDirectionName(fromDir);
						else
							dirName=CMLib.directions().getFromCompassDirectionName(fromDir);
						R.showHappens(CMMsg.MSG_OK_VISUAL,L("A bright light from @x1 disappears.",dirName));
					}
					else
					{
						R.showHappens(CMMsg.MSG_OK_VISUAL,L("A bright light disappears."));
					}
					R.delEffect(A);
					A.destroy();
					R.recoverPhyStats();
					R.recoverRoomStats();
				}
			}
		}
		catch(Exception e)
		{
			Log.errOut(e);
		}
	}
	
	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		final boolean canBeUninvoked=canBeUninvoked();
		final MOB invoker=invoker();
		final Room room=CMLib.map().roomLocation(affected);
		if(canBeUninvoked&&(room!=null)&&(affected instanceof MOB))
			room.show((MOB)affected,null,CMMsg.MSG_OK_VISUAL,L("The lighthouse above <S-NAME> dims."));
		super.unInvoke();
		this.invoker=invoker;
		if(canBeUninvoked&&(room!=null))
		{
			final List<Room> rooms=new LinkedList<Room>();
			synchronized(roomSet)
			{
				rooms.addAll(roomSet);
				roomSet.clear();
			}
			for(Room R : rooms)
				this.delFromRoom(R, null, -1);
		}
		if(canBeUninvoked&&(room!=null))
			room.recoverRoomStats();
	}
	
	protected int nextDir(final Room R)
	{
		if(lastDir < 0)
			lastDir=Directions.NORTH;
		int dir=lastDir;
		if(dir >= Directions.CODES().length)
			dir=0;
		else
			dir++;
		while(((R.getRoomInDir(dir)==null)
				||(R.getExitInDir(dir)==null)
				||(!R.getExitInDir(dir).isOpen()))
			&&(dir != lastDir))
		{
			if(dir >= Directions.CODES().length)
				dir=0;
			else
				dir++;
		}
		return dir;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		final Room currentRoom = CMLib.map().roomLocation(affected);
		if((currentRoom == null)||(!currentRoom.isHere(affected)))
		{
			unInvoke();
			return false;
		}
		if(((currentRoom.domainType()&Room.INDOORS)==0)
		&&(currentRoom.getArea() instanceof BoardableShip))
		{
			final Item shipItem = ((BoardableShip)currentRoom.getArea()).getShipItem();
			final Room shipRoom = CMLib.map().roomLocation(shipItem);
			if((shipRoom == null)||(!shipRoom.isHere(shipItem)))
			{
				unInvoke();
				return false;
			}
			lastRoom=currentRoom;
			final Set<Room> newRooms = new HashSet<Room>();
			final TrackingFlags flags=CMLib.tracking().newFlags()
									.plus(TrackingFlag.AREAONLY)
									.plus(TrackingFlag.OPENONLY)
									.plus(TrackingFlag.OUTDOORONLY);
			newRooms.add(currentRoom);
			int range=10 + super.getXLEVELLevel(invoker())+(2*super.getXMAXRANGELevel(invoker()));
			newRooms.addAll(CMLib.tracking().getRadiantRooms(currentRoom, flags, range));
			int prevDir=Directions.getOpDirectionCode(this.lastDir);
			this.lastDir = nextDir(shipRoom);
			newRooms.add(shipRoom);
			if((this.lastDir>=0)
			&&(shipRoom.getRoomInDir(this.lastDir)!=null)
			&&(shipRoom.getExitInDir(this.lastDir)!=null)
			&&(shipRoom.getExitInDir(this.lastDir).isOpen()))
				newRooms.add(shipRoom.getRoomInDir(this.lastDir));
			List<Room> oldRooms;
			synchronized(roomSet)
			{
				oldRooms=new LinkedList<Room>(roomSet);
			}
			for(Room R : oldRooms)
			{
				if(!newRooms.contains(R))
					this.delFromRoom(R, shipItem, prevDir);
			}
			for(Room R : newRooms)
			{
				if(!oldRooms.contains(R))
					this.addToRoom(R, shipItem, Directions.getOpDirectionCode(this.lastDir));
			}
			synchronized(roomSet)
			{
				synchronized(roomSet)
				{
					this.roomSet.clear();
					this.roomSet.addAll(newRooms);
				}
			}
		}
		else
		{
			lastRoom=currentRoom;
			final List<Room> newRooms = new LinkedList<Room>();
			newRooms.add(currentRoom);
			int prevDir=Directions.getOpDirectionCode(this.lastDir);
			this.lastDir = nextDir(currentRoom);
			newRooms.add(currentRoom);
			if((this.lastDir>=0)
			&&(currentRoom.getRoomInDir(this.lastDir)!=null)
			&&(currentRoom.getExitInDir(this.lastDir)!=null)
			&&(currentRoom.getExitInDir(this.lastDir).isOpen()))
				newRooms.add(currentRoom.getRoomInDir(this.lastDir));
			List<Room> oldRooms;
			synchronized(roomSet)
			{
				oldRooms=new LinkedList<Room>(roomSet);
			}
			for(Room R : oldRooms)
			{
				if(!newRooms.contains(R))
					this.delFromRoom(R, null, prevDir);
			}
			for(Room R : newRooms)
			{
				if(!oldRooms.contains(R))
					this.addToRoom(R, null, Directions.getOpDirectionCode(this.lastDir));
			}
			synchronized(roomSet)
			{
				synchronized(roomSet)
				{
					this.roomSet.clear();
					this.roomSet.addAll(newRooms);
				}
			}
		}
		return true;
	}
	
	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,L("<S-NAME> already <S-HAS-HAVE> a lighthouse."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		final Room room=mob.location();
		if((success)&&(room!=null))
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?L("^S<S-NAME> attain(s) a pulsing lighthouse above <S-HIS-HER> head!"):L("^S<S-NAME> invoke(s) a lighthouse above <S-HIS-HER> head!^?"));
			if(room.okMessage(mob,msg))
			{
				room.send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
				room.recoverRoomStats();
			}
		}
		else
			beneficialWordsFizzle(mob,mob.location(),L("<S-NAME> attempt(s) to invoke a lighthouse, but fail(s)."));

		return success;
	}
}
