package com.planet_ink.coffee_mud.Abilities.Prayers;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Expire;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Move;
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

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/*
   Copyright 2015-2018 Bo Zimmerman

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

public class Prayer_MassGrave extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_MassGrave";
	}

	private final static String	localizedName	= CMLib.lang().L("Mass Grave");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Mass Grave)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER | Ability.DOMAIN_DEATHLORE;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_UNHOLY;
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
	
	public Room previousLocation=null;
	public Room shelter=null;

	public Room getPreviousLocation(MOB mob)
	{
		if((previousLocation==null)||(previousLocation.amDestroyed()))
		{
			if(text().length()>0)
				previousLocation=CMLib.map().getRoom(text());
			while((previousLocation==null)||(previousLocation.amDestroyed())||(!CMLib.flags().canAccess(mob, previousLocation)))
				previousLocation=CMLib.map().getRandomRoom();
		}
		return previousLocation;
	}

	@Override
	public void unInvoke()
	{
		if(!(affected instanceof MOB))
			return;
		final MOB M=(MOB)affected;

		if(canBeUninvoked())
		{
			Room shelter=this.shelter;
			if(shelter==null)
				shelter=M.location();
			if(shelter != null)
			{
				this.shelter=null;
				Room backToRoom=M.getStartRoom();
				int i=0;
				final LinkedList<MOB> mobs=new LinkedList<MOB>();
				for(final Enumeration<MOB> m=shelter.inhabitants();m.hasMoreElements();)
					mobs.add(m.nextElement());
				for(final MOB mob : mobs)
				{
					if(mob==null)
						break;
					mob.tell(L("You return to world of the living."));

					final CMMsg enterMsg=CMClass.getMsg(mob,previousLocation,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,L("<S-NAME> crawl(s) out of the ground!"));
					backToRoom=getPreviousLocation(mob);
					if(backToRoom==null)
						backToRoom=mob.getStartRoom();
					backToRoom.bringMobHere(mob,false);
					backToRoom.send(mob,enterMsg);
					CMLib.commands().postLook(mob,true);
				}
				final LinkedList<Item> items=new LinkedList<Item>();
				for(final Enumeration<Item> e=shelter.items();e.hasMoreElements();)
					items.add(e.nextElement());
				for(final Item I : items)
				{
					if(I.container()==null)
						backToRoom.moveItemTo(I, Expire.Player_Drop, Move.Followers);
				}
				i=0;
				while(i<shelter.numItems())
				{
					final Item I=shelter.getItem(i);
					backToRoom.moveItemTo(I, Expire.Player_Drop, Move.Followers);
					if(shelter.isContent(I))
						i++;
				}
				this.shelter=null;
				previousLocation=null;
			}
		}
		super.unInvoke();
	}

	@Override
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if(((msg.sourceMinor()==CMMsg.TYP_QUIT)
			||(msg.sourceMinor()==CMMsg.TYP_SHUTDOWN)
			||((msg.targetMinor()==CMMsg.TYP_EXPIRE)&&(msg.target()==shelter))
			||(msg.sourceMinor()==CMMsg.TYP_DEATH)
			||(msg.sourceMinor()==CMMsg.TYP_ROOMRESET))
		&&(shelter!=null)
		&&(shelter.isInhabitant(msg.source())))
		{
			getPreviousLocation(msg.source()).bringMobHere(msg.source(),false);
			unInvoke();
		}
		return super.okMessage(host,msg);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if(mob.fetchEffect(ID())!=null)
		{
			mob.fetchEffect(ID()).unInvoke();
			return false;
		}

		switch(mob.location().domainType())
		{
		case Room.DOMAIN_INDOORS_CAVE:
		case Room.DOMAIN_OUTDOORS_WOODS:
		case Room.DOMAIN_OUTDOORS_ROCKS:
		case Room.DOMAIN_OUTDOORS_PLAINS:
		case Room.DOMAIN_OUTDOORS_JUNGLE:
		case Room.DOMAIN_OUTDOORS_SWAMP:
		case Room.DOMAIN_OUTDOORS_DESERT:
		case Room.DOMAIN_OUTDOORS_HILLS:
		case Room.DOMAIN_OUTDOORS_MOUNTAINS:
			break;
		default:
			mob.tell(L("This magic will only work on soil or natural stone."));
			return false;
		}
		
		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,somanticCastCode(mob,null,auto),auto?"":L("^S<S-NAME> @x1 for an immediate burial.^?",prayWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final Set<MOB> h=properTargets(mob,givenTarget,false);
				if(h==null)
					return false;

				final Room thisRoom=mob.location();
				previousLocation=thisRoom;
				final Room newRoom=CMClass.getLocale("CaveRoom");
				shelter = newRoom;
				newRoom.setDisplayText(L("An underground grave"));
				newRoom.setDescription(L("You are in a dark dirty place!"));
				newRoom.setArea(mob.location().getArea());
				newRoom.setAtmosphere(RawMaterial.RESOURCE_NOTHING);
				Ability A=CMClass.getAbility("Prop_PeaceMaker");
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
				shelter.setArea(mob.location().getArea());
				shelter.setAtmosphere(RawMaterial.RESOURCE_DIRT);
				miscText=CMLib.map().getExtendedRoomID(mob.location());
				for (final Object element : h)
				{
					final MOB follower=(MOB)element;
					final CMMsg enterMsg=CMClass.getMsg(follower,newRoom,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,L("<S-NAME> <S-IS-ARE> buried here."));
					final CMMsg leaveMsg=CMClass.getMsg(follower,thisRoom,this,somanticCastCode(mob,newRoom,auto),L("<S-NAME> <S-IS-ARE> buried underground."));
					if(thisRoom.okMessage(follower,leaveMsg)&&newRoom.okMessage(follower,enterMsg))
					{
						if(follower.isInCombat())
						{
							CMLib.commands().postFlee(follower,("NOWHERE"));
							follower.makePeace(false);
						}
						thisRoom.send(follower,leaveMsg);
						newRoom.bringMobHere(follower,false);
						thisRoom.delInhabitant(follower);
						newRoom.send(follower,enterMsg);
						follower.tell(L("\n\r\n\r"));
						CMLib.commands().postLook(follower,true);
						if(follower==mob)
							beneficialAffect(mob,mob,asLevel,999999);
					}
				}
			}
		}
		else
			beneficialVisualFizzle(mob,null,L("<S-NAME> wave(s) <S-HIS-HER> arms and and speak(s), but nothing happens."));

		return success;
	}
}
