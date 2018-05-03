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

public class Thief_SmugglersHold extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_SmugglersHold";
	}

	private final static String	localizedName	= CMLib.lang().L("Smugglers Hold");

	@Override
	public String name()
	{
		return localizedName;
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

	private static final String[]	triggerStrings	= I(new String[] { "BUILDHOLD","SMUGGLERSHOLD" });

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_STEALTHY;
	}

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT|USAGE_MANA;
	}
	
	protected Room fromRoom = null;
	protected int direction = -1;
	protected boolean messedUp = false;
	protected boolean aborted = false;
	protected boolean fullMode = false;
	
	@Override
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		if(affected instanceof Exit)
		{
			final Room R=msg.source().location();
			if((text().length()>0)&&(R!=null))
			{
				if(R.fetchInhabitant(text())==null)
				{
					if(!fullMode)
					{
						fullMode=true;
						affected.recoverPhyStats();
					}
				}
				else
				{
					if(fullMode)
					{
						fullMode=false;
						affected.recoverPhyStats();
					}
				}
			}
		}
	}
	
	protected void commonTell(MOB mob, String str)
	{
		if(mob.isMonster()&&(mob.amFollowing()!=null))
		{
			if(str.startsWith("You"))
				str=L("I@x1",str.substring(3));
			CMLib.commands().postSay(mob,null,str,false,false);
		}
		else
			mob.tell(str);
	}

	protected void commonEmote(MOB mob, String str)
	{
		if(mob.isMonster()&&(mob.amFollowing()!=null))
			mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT|CMMsg.MASK_ALWAYS,str);
		else
			mob.tell(mob,null,null,str);
	}
	
	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if(affected instanceof MOB)
			{
				final MOB mob=(MOB)affected;
				final Room room = CMLib.map().getRoom(fromRoom);
				final int direction=this.direction;
				if((messedUp)||(direction<0)||(fromRoom==null))
				{
					commonTell(mob,L("You've ruined the smuggler's hold!"));
				}
				else
				{
					synchronized(("SYNC"+room.roomID()).intern())
					{
						final Room R=CMClass.getLocale("WoodRoom");
						R.setRoomID(room.getArea().getNewRoomID(room,direction));
						if(R.roomID().length()==0)
						{
							commonTell(mob,L("You've ruined the smuggler's hold!"));
						}
						else
						{
							R.setArea(room.getArea());
							if(invoker==null)
								R.setDisplayText(L("The Smuggler's Hold"));
							else
								R.setDisplayText(L(invoker.name()+"'s Smuggler's Hold"));
							R.setDescription("");
							
							Exit newExit=CMClass.getExit("GenDoor");
							newExit.setName(L("a false wall"));
							newExit.setExitParams("wall","close","open","a false wall");
							Thief_SmugglersHold holder=(Thief_SmugglersHold)this.copyOf();
							holder.setMiscText(mob.Name());
							holder.fullMode=false;
							newExit.addNonUninvokableEffect(holder);
							newExit.recoverPhyStats();
							newExit.text();
							
							room.rawDoors()[direction]=R;
							room.setRawExit(direction, newExit);
							
							Exit opExit=CMClass.getExit("Open");
							R.rawDoors()[Directions.getOpDirectionCode(direction)]=room;
							R.setRawExit(Directions.getOpDirectionCode(direction), opExit);
							commonEmote(mob,L("<S-NAME> finish(es) the smuggler's hold!"));
						}
					}
				}
			}
		}
		super.unInvoke();
	}

	private static final int totalTicks=20;
	
	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		final Physical affected=this.affected;
		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;
			if((mob.isInCombat())
			||(CMLib.flags().isFalling(affected))
			||(!CMLib.flags().isAliveAwakeMobileUnbound(mob,true)))
			{
				aborted=true;
				unInvoke();
				return false;
			}
			if(tickDown==4)
				mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> <S-IS-ARE> almost done building a smuggler's hold."));
			else
			if((tickDown%4)==0)
			{
				final int pct=(int)Math.round(CMath.div(totalTicks-tickDown,totalTicks)*100.0);
				mob.location().show(mob,null,this,CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> continue(s) building a smuggler's hold (@x1% completed).",""+pct),null,L("<S-NAME> continue(s) building a smuggler's hold."));
			}
		}
		return true;
	}
	
	@Override
	public void affectPhyStats(Physical affectedEnv, PhyStats affectableStats)
	{
		if(affectedEnv instanceof MOB)
			affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_TRACK);
		else
		if(affectedEnv instanceof Exit)
		{
			if(fullMode)
				affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_HIDDEN);
			else
				affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_HIDDEN|PhyStats.IS_INVISIBLE);
		}
		super.affectPhyStats(affectedEnv, affectableStats);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		this.fromRoom = null;
		this.direction = -1;
		this.messedUp = false;
		this.aborted = false;
		
		if((CMLib.flags().isSitting(mob)||CMLib.flags().isSleeping(mob)))
		{
			mob.tell(L("You are on the floor!"));
			return false;
		}

		if(!CMLib.flags().isAliveAwakeMobileUnbound(mob,false))
			return false;
		
		final Room R=mob.location();
		if(R==null)
			return false;
		
		final Item target;
		if((R.getArea() instanceof BoardableShip)
		&&(((BoardableShip)R.getArea()).getShipItem() instanceof BoardableShip))
		{
			target=((BoardableShip)R.getArea()).getShipItem();
		}
		else
		{
			mob.tell(L("You must be on a ship to build a smuggler's hold!"));
			return false;
		}
		
		for(final Enumeration<Room> r=R.getArea().getProperMap();r.hasMoreElements();)
		{
			final Room R2=r.nextElement();
			if((R2!=null)
			&&(R2.fetchEffect(ID())!=null))
			{
				mob.tell(L("Your ship already has a smuggler's hold!"));
				return false;
			}
		}

		if(commands.size()==0)
		{
			mob.tell(L("You must specify which direction to build your hold in."));
			return false;
		}
		
		String dirStr=CMParms.combine(commands,0);
		final int direction=CMLib.directions().getGoodShipDirectionCode(dirStr);
		if(direction<0)
		{
			mob.tell(L("@x1 is not a valid direction to build your hold in.",dirStr));
			return false;
		}
		dirStr=CMLib.directions().getShipInDirectionName(direction);
		
		final Room R2=R.getRoomInDir(direction);
		final Exit E2=R.getExitInDir(direction);
		if((R2!=null)||(E2!=null))
		{
			mob.tell(L("You can not build in that direction."));
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_THIEF_ACT,auto?"":L("<S-NAME> start(s) building a smuggler's hold @x1!",dirStr));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			Thief_SmugglersHold A=(Thief_SmugglersHold)beneficialAffect(mob, mob, asLevel, totalTicks);
			if(A!=null)
			{
				A.fromRoom = mob.location();
				A.direction = direction;
				A.messedUp = !success;
				A.aborted = false;
			}
		}
		return success;
	}
}
