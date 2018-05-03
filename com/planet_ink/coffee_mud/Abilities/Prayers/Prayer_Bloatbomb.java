package com.planet_ink.coffee_mud.Abilities.Prayers;
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
   Copyright 2014-2018 Bo Zimmerman

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

public class Prayer_Bloatbomb extends Prayer implements Trap
{
	@Override
	public String ID()
	{
		return "Prayer_Bloatbomb";
	}

	private final static String	localizedName	= CMLib.lang().L("Bloat Bomb");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Bloat Bomb)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ITEMS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER | Ability.DOMAIN_VEXING;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_UNHOLY;
	}
	
	@Override
	protected int overrideMana()
	{
		return Ability.COST_PCT+50;
	}
	
	@Override
	public void setAffectedOne(Physical affected)
	{
		super.setAffectedOne(affected);
	}
	
	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(affected==null)
		{
			this.unInvoke();
			return;
		}
		if(affected instanceof Item)
		{
			final Item body=(Item)affected;
			if(msg.amITarget(body))
			{
				if((msg.targetMinor()==CMMsg.TYP_OPEN)
				 ||(msg.targetMinor()==CMMsg.TYP_GIVE)
				 ||(msg.targetMinor()==CMMsg.TYP_GET)
				 ||(msg.targetMinor()==CMMsg.TYP_JUSTICE)
				 ||(msg.targetMinor()==CMMsg.TYP_GENERAL)
				 ||(msg.targetMinor()==CMMsg.TYP_LOCK)
				 ||(msg.targetMinor()==CMMsg.TYP_PULL)
				 ||(msg.targetMinor()==CMMsg.TYP_PUSH)
				 ||(msg.targetMinor()==CMMsg.TYP_UNLOCK))
				{
					this.spring(msg.source());
					return;
				}
			}
		}
	}

	@Override
	public boolean isABomb()
	{
		return true;
	}

	@Override
	public void activateBomb()
	{
	}

	@Override
	public void setReset(int Reset)
	{
	}

	@Override
	public int getReset()
	{
		return 0;
	}

	@Override
	public boolean maySetTrap(MOB mob, int asLevel)
	{
		return false;
	}

	@Override
	public boolean canSetTrapOn(MOB mob, Physical P)
	{
		return P instanceof DeadBody;
	}

	@Override
	public boolean canReSetTrap(MOB mob)
	{
		return false;
	}

	@Override
	public List<Item> getTrapComponents()
	{
		return new Vector<Item>();
	}

	@Override
	public String requiresToSet()
	{
		return "";
	}

	@Override
	public void resetTrap(MOB mob)
	{
	}

	@Override
	public Trap setTrap(MOB mob, Physical P, int trapBonus, int qualifyingClassLevel, boolean perm)
	{
		beneficialAffect(mob, P, qualifyingClassLevel + trapBonus, 0);
		return (Trap) P.fetchEffect(ID());
	}

	@Override
	public boolean disabled()
	{
		return false;
	}

	@Override
	public boolean sprung()
	{
		return false;
	}

	@Override
	public void disable()
	{
		unInvoke();
	}

	@Override
	public void spring(MOB mob)
	{
		final Room room=mob.location();
		if(room != null)
		{
			final Set<MOB> friendlySet=new HashSet<MOB>();
			if(invoker()!=null)
				invoker().getGroupMembers(friendlySet);
			room.show(mob, affected, CMMsg.MSG_OK_ACTION, L("<T-NAME> explodes, spraying clumps of stomach acid everywhere!"));
			for(final Enumeration<MOB> m=room.inhabitants();m.hasMoreElements();)
			{
				MOB M=m.nextElement();
				if((M!=null)&&(!friendlySet.contains(M)))
				{
					final MOB invoker=(invoker()!=null) ? invoker() : M;
					final int damage=CMLib.dice().roll(4,5+invoker.phyStats().level(),0);
					CMLib.combat().postDamage(invoker,M,this,damage,CMMsg.MASK_MALICIOUS|CMMsg.MASK_ALWAYS|CMMsg.TYP_ACID,Weapon.TYPE_MELTING,L("The acid clumps <DAMAGE> <T-NAME>!"));
					CMLib.combat().postRevengeAttack(M, invoker);
				}
			}
		}
		Physical affected=this.affected;
		unInvoke();
		if(affected instanceof Item)
			((Item)affected).destroy();
			
	}
	
	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success && (target.phyStats().level() < ((mob.phyStats().level() + super.getXLEVELLevel(mob))/2)))
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto)|CMMsg.MASK_MALICIOUS,L("^S<S-NAME> @x1 for <T-NAME> to die.^?",prayForWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				CMLib.combat().postDeath(target, target, null);
				DeadBody body=null;
				for(Enumeration<Item> i=mob.location().items();i.hasMoreElements();)
				{
					final Item I=i.nextElement();
					if((I instanceof DeadBody)&&(((DeadBody)I).getMobName().equals(target.Name())))
						body=(DeadBody)I;
				}
				if(body==null)
					mob.tell(L("The death did not appear to create a body!"));
				else
					beneficialAffect(mob, body, asLevel, 0);
			}
		}
		else
			return maliciousFizzle(mob,target,L("^S<S-NAME> @x1 <T-NAME> to die, but nothing happens.^?",prayForWord(mob)));
		// return whether it worked
		return success;
	}
}

