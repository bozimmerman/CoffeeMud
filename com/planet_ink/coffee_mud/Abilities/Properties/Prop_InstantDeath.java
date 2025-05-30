package com.planet_ink.coffee_mud.Abilities.Properties;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB.Attrib;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2012-2025 Bo Zimmerman

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
public class Prop_InstantDeath extends Property
{
	@Override
	public String ID()
	{
		return "Prop_InstantDeath";
	}

	@Override
	public String name()
	{
		return "An Instant Death Property";
	}

	@Override
	public long flags()
	{
		return super.flags()|Ability.FLAG_POTENTIALLY_DEADLY;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_ITEMS|Ability.CAN_MOBS;
	}

	protected CompiledZMask			mask			= null;
	protected volatile boolean[]	killTrigger		= { false };
	protected double				damagePercent	= 100.0;
	protected int					damageAmount	= 0;
	protected String				message			= "";

	public Prop_InstantDeath()
	{
		super();
	}

	@Override
	public void setMiscText(final String newMiscText)
	{
		super.setMiscText(newMiscText);
		final String maskStr=CMParms.getParmStr(newMiscText,"mask","");
		mask=null;
		if((maskStr!=null)&&(maskStr.trim().length()>0))
			mask=CMLib.masking().getPreCompiledMask(maskStr);
		message=CMParms.getParmStr(newMiscText, "msg", "");
		damageAmount=CMParms.getParmInt(newMiscText, "damage", 0);
		damagePercent=CMParms.getParmDouble(newMiscText, "dmgpct", 100.0)/100.0;

	}

	@Override
	public String accountForYourself()
	{
		return "instant killing";
	}

	public Set<MOB> getEveryoneHere(final MOB spareMe, final Room R)
	{
		final Set<MOB> V=new HashSet<MOB>();
		if(R==null)
			return V;
		for(int i=0;i<R.numInhabitants();i++)
		{
			final MOB M=R.fetchInhabitant(i);
			if((spareMe!=null)&&(spareMe==M))
				continue;
			if((M!=null)
			&&(!CMSecurity.isAllowed(M,R,CMSecurity.SecFlag.IMMORT))
			&&((mask==null)||(CMLib.masking().maskCheck(mask, M, false))))
				V.add(M);
		}
		return V;
		//CMLib.combat().postDeath(null,M,null);
	}

	protected MOB getTickersMOB(final Tickable ticking)
	{
		if(ticking==null)
			return null;

		if(ticking instanceof MOB)
			return (MOB)ticking;
		else
		if(ticking instanceof Item)
		{
			if(((Item)ticking).owner() != null)
			{
				if(((Item)ticking).owner() instanceof MOB)
					return (MOB)((Item)ticking).owner();
			}

		}
		return null;
	}

	protected Room getTickersRoom(final Tickable ticking)
	{
		if(ticking==null)
			return null;

		if(ticking instanceof Room)
			return (Room)ticking;

		final MOB mob=getTickersMOB(ticking);
		if(mob!=null)
			return mob.location();

		if(ticking instanceof Item)
		{
			if(((Item)ticking).owner() != null)
			{
				if(((Item)ticking).owner() instanceof Room)
					return (Room)((Item)ticking).owner();
			}

		}
		return null;
	}

	public Set<MOB> getDeadMOBsFrom(final Environmental whoE)
	{
		if(whoE instanceof MOB)
		{
			final MOB mob=(MOB)whoE;
			final Room room=mob.location();
			if(room!=null)
				return getEveryoneHere(mob,room);
		}
		else
		if(whoE instanceof Item)
		{
			final Item item=(Item)whoE;
			final Environmental E=item.owner();
			if(E!=null)
			{
				final Room room=getTickersRoom(whoE);
				if(room!=null)
				{
					if((E instanceof MOB)&&((mask==null)||(CMLib.masking().maskCheck(mask, E, false))))
						return new XHashSet<MOB>((MOB)E);
					else
					if(E instanceof Room)
						return getEveryoneHere(null,(Room)E);
					room.recoverRoomStats();
				}
			}
		}
		else
		if(whoE instanceof Room)
			return getEveryoneHere(null,(Room)whoE);
		else
		if(whoE instanceof Area)
		{
			final Set<MOB> allMobs=new HashSet<MOB>();
			for(final Enumeration<Room> r=((Area)whoE).getMetroMap();r.hasMoreElements();)
			{
				final Room R=r.nextElement();
				allMobs.addAll(getEveryoneHere(null,R));
			}
		}
		return new HashSet<MOB>();
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(tickID!=Tickable.TICKID_MISCELLANEOUS)
			return super.tick(ticking, tickID);
		if(killTrigger[0])
		{
			final LinkedList<MOB> killThese=new LinkedList<MOB>();
			synchronized(killTrigger)
			{
				killThese.addAll(getDeadMOBsFrom(affected));
			}
			final MOB killerM=CMLib.map().getFactoryMOB(CMLib.map().roomLocation(affected));
			killerM.setName(affected.Name());
			final MOB killer = (affected instanceof MOB)?((MOB)affected):killerM;
			final String msgStr=message.length()>0?message:L("<S-NAME> <DAMAGES> <T-NAME>!");
			try
			{
				synchronized(killTrigger)
				{
					killTrigger[0]=false;
				}
				for(final MOB M : killThese)
				{
					if(M.isAttributeSet(Attrib.SYSOPMSGS))
						continue;
					final int dmgAmount=damageAmount + (int)Math.round(CMath.mul(damagePercent, M.maxState().getHitPoints()));
					if(dmgAmount>=M.curState().getHitPoints())
					{
						if(message.length()>0)
							M.location().show(killer, M, CMMsg.MSG_OK_VISUAL, message);
						CMLib.combat().postDeath(null, M, null);
					}
					else
					{
						CMLib.combat().postDamage(killer, M, null, dmgAmount, CMMsg.MASK_ALWAYS|CMMsg.MASK_MALICIOUS|CMMsg.TYP_JUSTICE,Weapon.TYPE_BURSTING,msgStr);
						synchronized(killTrigger)
						{
							killTrigger[0]=true;
						}
					}
				}
			}
			finally
			{
				killerM.destroy();
			}
		}
		synchronized(killTrigger)
		{
			if(killTrigger[0])
			{
				if(CMLib.threads().getTickGroupPeriod(this, Tickable.TICKID_MISCELLANEOUS)==500)
				{
					CMLib.threads().deleteTick(this, Tickable.TICKID_MISCELLANEOUS);
					CMLib.threads().startTickDown(this, Tickable.TICKID_MISCELLANEOUS, 4000, 1);
					return false;
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public void executeMsg(final Environmental affecting, final CMMsg msg)
	{
		super.executeMsg(affecting,msg);
		if(msg.amITarget(affecting))
		{
			boolean activated=false;
			if(affecting instanceof MOB)
			{
				if((msg.targetMajor(CMMsg.MASK_MALICIOUS))
				&&(!msg.source().isMonster()))
					activated=true;
			}
			else
			if((affecting instanceof Food)
			||(affecting instanceof Drink))
			{
				if((msg.targetMinor()==CMMsg.TYP_EAT)
				||(msg.targetMinor()==CMMsg.TYP_DRINK))
					activated=true;
			}
			else
			if((affecting instanceof Armor)
			||(affecting instanceof Weapon))
			{
				if((msg.targetMinor()==CMMsg.TYP_WEAR)
				||(msg.targetMinor()==CMMsg.TYP_HOLD)
				||(msg.targetMinor()==CMMsg.TYP_WIELD))
					activated=true;
			}
			else
			if(affecting instanceof Item)
			{
				if((msg.targetMinor()==CMMsg.TYP_GET)||(msg.targetMinor()==CMMsg.TYP_PUSH)||(msg.targetMinor()==CMMsg.TYP_PULL))
					activated=true;
			}
			else
			if(msg.targetMinor()==CMMsg.TYP_ENTER)
				activated=true;
			if(activated)
			{
				synchronized(killTrigger)
				{
					killTrigger[0]=true;
					if(!CMLib.threads().isTicking(this, Tickable.TICKID_MISCELLANEOUS))
						CMLib.threads().startTickDown(this, Tickable.TICKID_MISCELLANEOUS, 500,1);
				}
			}
		}
	}
}
