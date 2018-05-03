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

import java.util.*;

/*
   Copyright 2003-2018 Bo Zimmerman

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

public class Spell_Cogniportive extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_Cogniportive";
	}

	private final static String	localizedName	= CMLib.lang().L("Cogniportive");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ITEMS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_ITEMS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL | Ability.DOMAIN_CONJURATION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_TRANSPORTING;
	}

	public String establishHome(MOB mob, Item me, boolean beLoose)
	{
		if(me instanceof LandTitle)
			return ((LandTitle)me).getAllTitledRooms().get(0).roomID();
		// check mobs worn items first!
		final String srchStr="$"+me.Name()+"$";
		List<Item> mobInventory=new Vector<Item>(1);
		try
		{
			mobInventory=CMLib.map().findInventory(CMLib.map().rooms(),null, srchStr, 10);
			for(final Item I : mobInventory)
			{
				final Environmental owner=I.owner();
				final Room room=CMLib.map().roomLocation(owner);
				if((owner instanceof MOB)
				&&(((MOB)owner).isMonster())
				&&(!I.amWearingAt(Wearable.IN_INVENTORY))
				&&((beLoose) || me.sameAs(I))
				&&(CMLib.law().getLandTitle(room)==null))
					return CMLib.map().getExtendedRoomID(room);
			}
		}
		catch (final NoSuchElementException nse)
		{
		}
		try
		{
			final List<Environmental> all=CMLib.map().findShopStockers(CMLib.map().rooms(), mob, srchStr, 10);
			for(final Environmental O : all)
			{
				if(O instanceof ShopKeeper)
				{
					final ShopKeeper S=(ShopKeeper)O;
					final Room room=CMLib.map().getStartRoom(S);
					final Environmental E=S.getShop().getStock(me.Name(), null);
					if((E instanceof Item)
					&&((beLoose) || me.sameAs(E))
					&&(CMLib.law().getLandTitle(room)==null))
						return CMLib.map().getExtendedRoomID(room);
				}
			}
		}
		catch (final NoSuchElementException nse)
		{
		}
		try
		{
			// check mobs inventory items third!
			for(final Item I : mobInventory)
			{
				final Environmental owner=I.owner();
				final Room room=CMLib.map().getStartRoom(owner);
				if((owner instanceof MOB)
				&&(((MOB)owner).isMonster())
				&&(I.amWearingAt(Wearable.IN_INVENTORY))
				&&((beLoose) || me.sameAs(I))
				&&(CMLib.law().getLandTitle(room)==null))
					return CMLib.map().getExtendedRoomID(room);
			}
		}
		catch (final NoSuchElementException nse)
		{
		}
		try
		{
			// check room stuff last
			final List<Item> targets=CMLib.map().findRoomItems(CMLib.map().rooms(), mob, me.Name(), false,10);
			for(final Item I : targets)
			{
				final Room R=CMLib.map().roomLocation(I);
				if((R!=null)
				&&((beLoose) || me.sameAs(I))
				&&(CMLib.law().getLandTitle(R)==null))
					return CMLib.map().getExtendedRoomID(R);
			}
		}
		catch (final NoSuchElementException nse)
		{
		}
		return "";
	}

	public void waveIfAble(MOB mob, Environmental afftarget, Item me)
	{
		if((mob!=null) && (mob.isMine(me)) && (mob.location()!=null) && (me!=null))
		{
			if(text().length()==0)
				setMiscText(establishHome(mob,me,false));
			if(text().length()==0)
				setMiscText(establishHome(mob,me,true));
			final Room home=CMLib.map().getRoom(text());
			if((home==null)||(!CMLib.flags().canAccess(mob,home)))
				mob.location().showHappens(CMMsg.MSG_OK_VISUAL,L("Strange fizzled sparks fly from @x1.",me.name()));
			else
			{
				final List<MOB> h=properTargetList(mob,null,false);
				if(h==null)
					return;

				final Room thisRoom=mob.location();
				for (final MOB follower : h)
				{
					final CMMsg enterMsg=CMClass.getMsg(follower,home,this,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,L("<S-NAME> appears in a puff of smoke."));
					final CMMsg leaveMsg=CMClass.getMsg(follower,thisRoom,this,CMMsg.MSG_LEAVE|CMMsg.MASK_MAGIC,L("<S-NAME> disappear(s) in a puff of smoke."));
					if(thisRoom.isInhabitant(follower)
					&&thisRoom.okMessage(follower,leaveMsg)
					&&(!home.isInhabitant(follower))
					&&home.okMessage(follower,enterMsg))
					{
						if(follower.isInCombat())
						{
							CMLib.commands().postFlee(follower,("NOWHERE"));
							follower.makePeace(false);
						}
						thisRoom.send(follower,leaveMsg);
						home.bringMobHere(follower,false);
						home.send(follower,enterMsg);
						follower.tell(L("\n\r\n\r"));
						CMLib.commands().postLook(follower,true);
					}
					else
					if(follower==mob)
						break;
				}
			}
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		final MOB mob=msg.source();

		if(affected instanceof Item)
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_WAND_USE:
				if(msg.amITarget(affected)&&((msg.tool()==null)||(msg.tool() instanceof Physical)))
					waveIfAble(mob,msg.tool(),(Item)affected);
				break;
			case CMMsg.TYP_SPEAK:
				if((msg.sourceMinor()==CMMsg.TYP_SPEAK)
				&&(msg.sourceMessage()!=null))
				{
					final String msgStr=CMStrings.getSayFromMessage(msg.sourceMessage());
					if(msgStr!=null)
					{
						final Vector<String> V=CMParms.parse(msgStr);
						if((V.size()>=2)
						&&(V.firstElement().equalsIgnoreCase("HOME")))
						{
							final String str=CMParms.combine(V,1);
							if((str.length()>0)
							&&((CMLib.english().containsString(affected.name(),str)
								||CMLib.english().containsString(affected.displayText(),str))))
							{
								boolean alreadyWanding=false;
								final List<CMMsg> trailers =msg.trailerMsgs();
								if(trailers!=null)
								{
									for(final CMMsg msg2 : trailers)
									{
										if((msg2.targetMinor()==CMMsg.TYP_WAND_USE)
										&&(msg2.target() == affected))
											alreadyWanding=true;
									}
								}
								if(!alreadyWanding)
									msg.addTrailerMsg(CMClass.getMsg(msg.source(),affected,msg.target(),CMMsg.NO_EFFECT,null,CMMsg.MASK_ALWAYS|CMMsg.TYP_WAND_USE,CMStrings.getSayFromMessage(msg.sourceMessage()),CMMsg.NO_EFFECT,null));
							}
						}
					}
				}
				break;
			default:
				break;
			}
		}
		super.executeMsg(myHost,msg);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Item target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_ANY);
		if(target==null)
		{
			final String str=CMParms.combine(commands,0).toUpperCase();
			if(str.equals("MONEY")||str.equals("GOLD")||str.equals("COINS"))
				mob.tell(L("You can't cast this spell on coins!"));
			return false;
		}

		Ability A=target.fetchEffect(ID());
		if(A!=null)
		{
			mob.tell(L("@x1 is already cogniportive!",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,somanticCastCode(mob,target,auto),auto?"":L("^S<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>, incanting.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,CMMsg.MSG_OK_ACTION,L("<T-NAME> glow(s) softly!"));
				beneficialAffect(mob,target,asLevel,1000);
				A=target.fetchEffect(ID());
				if(A!=null)
				{
					String home=((Spell_Cogniportive)A).establishHome(mob,target,false);
					if(home.length()==0)
						home=((Spell_Cogniportive)A).establishHome(mob,target,true);
					A.setMiscText(home);
				}
				target.recoverPhyStats();
				mob.recoverPhyStats();
				mob.location().recoverRoomStats();
			}

		}
		else
			beneficialWordsFizzle(mob,target,L("<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>, incanting, but nothing happens."));

		// return whether it worked
		return success;
	}
}
