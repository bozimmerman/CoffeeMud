package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
   Copyright 2001-2018 Bo Zimmerman

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

public class Falling extends StdAbility
{
	@Override
	public String ID()
	{
		return "Falling";
	}

	private final static String	localizedName	= CMLib.lang().L("Falling");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Falling)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ITEMS | Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	boolean			temporarilyDisable	= false;
	public Room		room				= null;
	boolean			hitTheCeiling		= false;
	int				damageToTake		= 0;
	protected int	fallTickDown		= 1;

	protected boolean reversed()
	{
		return proficiency() == 100;
	}

	protected boolean isAirRoom(Room R)
	{
		if(R==null)
			return false;
		if((R.domainType()==Room.DOMAIN_INDOORS_AIR)
		||(R.domainType()==Room.DOMAIN_OUTDOORS_AIR))
			return true;
		return false;
	}

	protected boolean canFallFrom(Room fromHere, int direction)
	{
		if((fromHere==null)||(direction<0)||(direction>=Directions.NUM_DIRECTIONS()))
			return false;

		final Room toHere=fromHere.getRoomInDir(direction);
		if((toHere==null)
		||(fromHere.getExitInDir(direction)==null)
		||(!fromHere.getExitInDir(direction).isOpen()))
			return false;
		if(CMLib.flags().isWaterySurfaceRoom(fromHere)&&CMLib.flags().isUnderWateryRoom(toHere))
			return false;
		return true;
	}

	@Override
	public void setMiscText(String newMiscText)
	{
		super.setMiscText(newMiscText);
		if((newMiscText!=null) && (newMiscText.length()>0))
		{
			for(final String parm : CMParms.parse(newMiscText.toUpperCase()))
			{
				if(parm.equals("REVERSED"))
					this.setProficiency(100);
				else
				if(parm.equals("NORMAL"))
					this.setProficiency(0);
			}
		}
	}
	
	protected boolean stopFalling(MOB mob)
	{
		final Room R=mob.location();
		if(reversed())
		{
			if(!hitTheCeiling)
			{
				hitTheCeiling=true;
				if(R!=null)
					R.show(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> hit(s) the ceiling.@x1",CMLib.protocol().msp("splat.wav",50)));
				CMLib.combat().postDamage(mob,mob,this,damageToTake,CMMsg.MASK_ALWAYS|CMMsg.TYP_JUSTICE,-1,null);
			}
			return true;
		}
		hitTheCeiling=false;
		unInvoke();
		if(R!=null)
		{
			if(isAirRoom(R))
				R.show(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> stop(s) falling.@x1",CMLib.protocol().msp("splat.wav",50)));
			else
			if(CMLib.flags().isWaterySurfaceRoom(R)||CMLib.flags().isUnderWateryRoom(R))
				R.show(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> hit(s) the water.@x1",CMLib.protocol().msp("splat.wav",50)));
			else
			{
				R.show(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> hit(s) the ground.@x1",CMLib.protocol().msp("splat.wav",50)));
				if(CMath.div(damageToTake, mob.maxState().getHitPoints())>0.05)
				{
					LimbDamage damage = (LimbDamage)mob.fetchEffect("BrokenLimbs");
					if(damage == null)
					{
						damage = (LimbDamage)CMClass.getAbility("BrokenLimbs");
						damage.setAffectedOne(mob);
					}
					List<String> limbs = damage.unaffectedLimbSet();
					if(limbs.size()>0)
					{
						if(mob.fetchEffect(damage.ID()) == null)
						{
							mob.addEffect(damage);
							damage.makeLongLasting();
						}
						damage.damageLimb(limbs.get(CMLib.dice().roll(1, limbs.size(), -1)));
					}
				}
			}
			CMLib.combat().postDamage(mob,mob,this,damageToTake,CMMsg.MASK_ALWAYS|CMMsg.TYP_JUSTICE,-1,null);
		}
		mob.delEffect(this);
		return false;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		if(tickID!=Tickable.TICKID_MOB)
			return true;

		if(affected==null)
			return false;
		if(--fallTickDown>0)
			return true;
		fallTickDown=1;

		int direction=Directions.DOWN;
		String addStr=L("down");
		if(reversed())
		{
			direction=Directions.UP;
			addStr=L("upwards");
		}
		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;
			if(mob==null)
				return false;
			if(mob.location()==null)
				return false;

			if(CMLib.flags().isInFlight(mob))
			{
				damageToTake=0;
				unInvoke();
				return false;
			}
			else
			if(!canFallFrom(mob.location(),direction))
				return stopFalling(mob);
			else
			{
				if(mob.phyStats().weight()<1)
				{
					mob.tell(L("\n\r\n\rYou are floating gently @x1.\n\r\n\r",addStr));
				}
				else
				{
					mob.tell(L("\n\r\n\rYOU ARE FALLING @x1!!\n\r\n\r",addStr.toUpperCase()));
					int damage = CMLib.dice().roll(1,(int)Math.round(CMath.mul(CMath.mul(mob.maxState().getHitPoints(),0.1),CMath.div(mob.baseWeight(),150.0))),0);
					if(damage > (mob.maxState().getHitPoints()/3))
						damage = (mob.maxState().getHitPoints()/3);
					damageToTake=reversed()?damage:(damageToTake+damage);
				}
				temporarilyDisable=true;
				CMLib.tracking().walk(mob,direction,false,false);
				temporarilyDisable=false;
				if(!canFallFrom(mob.location(),direction))
					return stopFalling(mob);
				return true;
			}
		}
		else
		if(affected instanceof Item)
		{
			final Item item=(Item)affected;
			if((room==null)
			&&(item.owner()!=null)
			&&(item.owner() instanceof Room))
				room=(Room)item.owner();

			if((room==null)
			||((room!=null)&&(!room.isContent(item)))
			||(!CMLib.flags().isGettable(item))
			||(item.container()!=null)
			||(CMLib.flags().isInFlight(item.ultimateContainer(null)))
			||(room.getRoomInDir(direction)==null))
			{
				unInvoke();
				return false;
			}
			if(room.numItems()>100)
			{
				fallTickDown=CMLib.dice().roll(1,room.numItems()/50,0);
				if((--fallTickDown)>0)
					return true;
			}
			final Room nextRoom=room.getRoomInDir(direction);
			if(canFallFrom(room,direction))
			{
				room.show(invoker,null,item,CMMsg.MSG_OK_ACTION,L("<O-NAME> falls @x1.",addStr));
				nextRoom.moveItemTo(item,ItemPossessor.Expire.Player_Drop);
				room=nextRoom;
				nextRoom.show(invoker,null,item,CMMsg.MSG_OK_ACTION,L("<O-NAME> falls in from @x1.",(reversed()?"below":"above")));
				return true;
			}
			if(reversed())
				return true;
			unInvoke();
			return false;
		}

		return false;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if(temporarilyDisable)
			return true;
		final MOB mob=msg.source();
		if(affected instanceof MOB)
		{
			if(msg.amISource((MOB)affected))
			{
				if(CMLib.flags().isInFlight(mob))
				{
					damageToTake=0;
					unInvoke();
					return true;
				}
				if(msg.targetMajor(CMMsg.MASK_MOVE) 
				&&(!hitTheCeiling)
				&&(msg.sourceMinor()!=CMMsg.TYP_FLEE))
				{
					msg.source().tell(L("You are too busy falling to do that right now."));
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected!=null)&&(affected instanceof MOB)&&(msg.amISource((MOB)affected)))
		{
			if(msg.sourceMinor()==CMMsg.TYP_RECALL)
			{
				damageToTake=0;
				unInvoke();
			}
			else
			if((msg.tool() instanceof Ability)
			&&(CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_TRANSPORTING)))
			{
				damageToTake=0;
				unInvoke();
			}
		}
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if((affectableStats.disposition()&PhyStats.IS_FLYING)==0)
			affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_FALLING);
	}

	@Override
	public void setAffectedOne(Physical P)
	{
		if(P instanceof Room)
			room=(Room)P;
		else
			super.setAffectedOne(P);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical target, boolean auto, int asLevel)
	{
		if(!auto)
			return false;
		final Physical P=target;
		if(P==null)
			return false;
		if((P instanceof Item)&&(room==null))
			return false;
		if(P.fetchEffect("Falling")==null)
		{
			final Falling F=new Falling();
			F.setProficiency(proficiency());
			F.invoker=null;
			if(P instanceof MOB)
				F.invoker=(MOB)P;
			else
				F.invoker=CMClass.getMOB("StdMOB");
			F.setSavable(false);
			F.makeLongLasting();
			P.addEffect(F);
			if(!(P instanceof MOB))
				CMLib.threads().startTickDown(F,Tickable.TICKID_MOB,1);
			P.recoverPhyStats();

		}
		return true;
	}

	@Override
	public void setStat(String code, String val)
	{
		if(code==null)
			return;
		if(code.equalsIgnoreCase("DAMAGE"))
			this.damageToTake=CMath.s_int(val);
		else
		if(code.equalsIgnoreCase("REVERSED"))
			this.setProficiency(CMath.s_bool(val)?100:0);
		else
		if(code.equalsIgnoreCase("NORMAL"))
			this.setProficiency(CMath.s_bool(val)?0:100);
		else
			super.setStat(code, val);
	}
	
	@Override
	public String getStat(String code)
	{
		if(code==null)
			return "";
		if(code.equalsIgnoreCase("DAMAGE"))
			return ""+this.damageToTake;
		else
		if(code.equalsIgnoreCase("REVERSED"))
			return ""+(this.proficiency()==100);
		else
		if(code.equalsIgnoreCase("NORMAL"))
			return ""+(this.proficiency()==0);
		else
			return super.getStat(code);
	}
}
