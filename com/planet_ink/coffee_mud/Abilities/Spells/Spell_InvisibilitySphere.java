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

import java.lang.ref.WeakReference;
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

public class Spell_InvisibilitySphere extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_InvisibilitySphere";
	}

	private final static String localizedName = CMLib.lang().L("Invisibility Sphere");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Invisibility Sphere)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_ILLUSION;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS|Ability.CAN_ITEMS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}
	
	protected volatile WeakReference<Room> lastRoom = new WeakReference<Room>(null);
	protected volatile long lastRoomHash=0;

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
		{
			final Room centerR=mob.location();
			if((centerR!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> <S-IS-ARE> outside the invisibility sphere."));
			if(centerR!=null)
			{
				for(Enumeration<MOB> m=centerR.inhabitants();m.hasMoreElements();)
					removeFromSphere(m.nextElement());
				for(Enumeration<Item> i=centerR.items();i.hasMoreElements();)
					removeFromSphere(i.nextElement());
			}
		}
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		// when this spell is on a MOBs Affected list,
		// it should consistantly put the mob into
		// a sleeping state, so that nothing they do
		// can get them out of it.
		final Physical centerP = (invoker() != null) ? invoker() : affecting();
		if((centerP!=null)
		&&(!this.unInvoked)
		&&((!(centerP instanceof MOB))||(!((MOB)centerP).isInCombat()))
		&&((!(affected instanceof MOB))||(!((MOB)affected).isInCombat())))
		{
			final Room centerR=CMLib.map().roomLocation(centerP);
			if(centerR.isHere(centerP))
			{
				if(((affected==centerP)||(CMLib.map().roomLocation(affected)==centerR))
				&&((!(affected instanceof Item))||(((Item)affected).owner()==centerR)))
					affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_INVISIBLE);
				else
					removeFromSphere(affected);
			}
			else
				removeFromSphere(affected);
		}
		else
			removeFromSphere(affected);
	}

	protected synchronized void removeFromSphere(Physical affected)
	{
		if(affected != null)
		{
			if((invoker == null)&&(affecting() instanceof MOB))
				invoker=(MOB)affecting();
			final Physical invoker = (invoker() != null) ? invoker() : affecting();
			affected.delEffect(this);
			this.setAffectedOne(invoker);
			if(affected instanceof MOB)
				((MOB)affected).tell((MOB)affected,null,null,L("<S-NAME> <S-IS-ARE> outside the invisibility sphere."));
			affected.recoverPhyStats();
		}
	}
	
	protected synchronized void addToSphere(Physical affected)
	{
		if(affected != null)
		{
			if(affected.fetchEffect(ID())==null)
			{
				if((invoker == null)&&(affecting() instanceof MOB))
					invoker=(MOB)affecting();
				final Physical invoker = (invoker() != null) ? invoker() : affecting();
				if(affected instanceof MOB)
					((MOB)affected).tell((MOB)affected,null,null,L("<S-NAME> <S-IS-ARE> now inside the invisibility sphere."));
				affected.addEffect(this);
				this.setAffectedOne(invoker);
				affected.recoverPhyStats();
			}
		}
	}
	
	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(mob.isInCombat())
				return Ability.QUALITY_INDIFFERENT;
			if(mob.isMonster())
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		final Physical invoker = (invoker() != null) ? invoker() : affecting();
		if(msg.source() == invoker)
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_ENTER:
			{
				final Room invokerRoom=CMLib.map().roomLocation(invoker);
				for(Enumeration<MOB> m=invokerRoom.inhabitants();m.hasMoreElements();)
					addToSphere(m.nextElement());
				for(Enumeration<Item> i=invokerRoom.items();i.hasMoreElements();)
				{
					Item I=i.nextElement();
					if((I!=null)&&(I.container()==null))
						addToSphere(I);
				}
				break;
			}
			case CMMsg.TYP_LEAVE:
			{
				final Room invokerRoom=CMLib.map().roomLocation(invoker);
				if((msg.target() instanceof Room)&&(msg.target() != invokerRoom))
				{
					for(Enumeration<Item> i=((Room)msg.target()).items();i.hasMoreElements();)
						removeFromSphere(i.nextElement());
				}
				break;
			}
			}
		}
	}
	
	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		final Physical invoker = (invoker() != null) ? invoker() : affecting();
		final Room invokerRoom=CMLib.map().roomLocation(invoker);
		if(invokerRoom == null)
		{
			unInvoke();
			return false;
		}
		long newHash = 0;
		for(Enumeration<MOB> m=invokerRoom.inhabitants();m.hasMoreElements();)
			newHash = newHash ^ m.nextElement().hashCode();
		for(Enumeration<Item> i=invokerRoom.items();i.hasMoreElements();)
			newHash = newHash ^ i.nextElement().hashCode();
		if(invokerRoom == lastRoom.get())
		{
			if(newHash == this.lastRoomHash)
				return true;
		}
		else
			lastRoom = new WeakReference<Room>(invokerRoom);
		this.lastRoomHash = newHash;
		for(Enumeration<MOB> m=invokerRoom.inhabitants();m.hasMoreElements();)
			addToSphere(m.nextElement());
		for(Enumeration<Item> i=invokerRoom.items();i.hasMoreElements();)
		{
			Item I=i.nextElement();
			if((I!=null)&&(I.container()==null))
				addToSphere(I);
		}
		return true;
	}
	
	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(mob.fetchEffect(ID())!=null)
		{
			mob.tell(mob,null,null,L("You are already inside an invisibility sphere!"));
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),L(auto?"<T-NAME> lie(s) inside an invisibility sphere!":"^S<S-NAME> casts a spell and summons a sphere of invisibility.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,L("<S-NAME> cast(s) a spell, but nothing happens."));

		// return whether it worked
		return success;
	}
}
