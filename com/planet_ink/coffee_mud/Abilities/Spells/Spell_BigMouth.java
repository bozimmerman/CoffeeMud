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

public class Spell_BigMouth extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_BigMouth";
	}

	private final static String localizedName = CMLib.lang().L("Big Mouth");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Big Mouth)");

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
		return Ability.ACODE_SPELL|Ability.DOMAIN_TRANSMUTATION;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!(affected instanceof MOB))
			return super.okMessage(myHost,msg);

		final MOB mob=(MOB)affected;
		if((msg.amISource(mob))
		&&(msg.targetMinor()==CMMsg.TYP_EAT)
		&&(msg.target() instanceof Physical)
		&&(Stomach()!=null))
		{
			final CMMsg maliciousNessMsg=CMClass.getMsg(msg.source(), msg.target(), CMMsg.MSG_OK_ACTION | CMMsg.MASK_MALICIOUS, null);
			final int targetWeight = (msg.target() instanceof MOB) ? ((MOB)msg.target()).baseWeight() : ((Physical)msg.target()).phyStats().weight();
			if((targetWeight<(mob.phyStats().weight()/3))
			&&(mob.location()!=null)
			&&(mob.location().okMessage(myHost, maliciousNessMsg)))
			{
				final int maxInhabitants=1+((mob.fetchAbility(ID())!=null)?super.getXLEVELLevel(mob):0);
				if((Stomach()!=null)&&(Stomach().numInhabitants()>maxInhabitants))
				{
					mob.tell(L("Your stomach is too full."));
					return false;
				}

				if(msg.target() instanceof MOB)
				{
					final MOB target=(MOB)msg.target();
					final boolean isHit=CMLib.combat().rollToHit(msg.source(),target);
					if(!isHit)
					{
						mob.tell(L("You fail to eat @x1.",target.name(mob)));
						if((!target.isInCombat())&&(target.isMonster())&&(target!=msg.source())
						&&(target.location()==msg.source().location())&&(target.location().isInhabitant(msg.source()))
						&&(CMLib.flags().canBeSeenBy(msg.source(),target)))
							CMLib.combat().postAttack(target,msg.source(),target.fetchWieldedItem());
						return false;
					}
				}
				else
				if(msg.target() instanceof Food)
					return super.okMessage(myHost,msg);
				else
				if(!(msg.target() instanceof Item))
					return super.okMessage(myHost,msg);
				else
				if((!CMLib.flags().isGettable((Item)msg.target()))||(msg.target().displayText().length()==0))
				{
					mob.tell(L("You can not eat @x1.",((Item)msg.target()).name(mob)));
					return false;
				}

				msg.modify(msg.source(),msg.target(),msg.tool(),
						   msg.sourceCode()|CMMsg.MASK_ALWAYS|CMMsg.MASK_MALICIOUS,msg.sourceMessage(),
						   CMMsg.MSG_NOISYMOVEMENT|CMMsg.MASK_MALICIOUS,msg.targetMessage(),
						   msg.othersCode()|CMMsg.MASK_ALWAYS|CMMsg.MASK_MALICIOUS,msg.othersMessage());

			}
			else
			{
				mob.tell(L("@x1 is just too large for you to eat!",((Physical)msg.target()).name(mob)));
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(!(affected instanceof MOB))
		{
			super.executeMsg(myHost,msg);
			return;
		}

		final MOB mob=(MOB)affected;

		if((msg.amISource(mob))
		&&(msg.sourceMinor()==CMMsg.TYP_EAT)
		&&(msg.target() instanceof Physical)
		&&(Stomach()!=null))
		{
			final int targetWeight = (msg.target() instanceof MOB) ? ((MOB)msg.target()).baseWeight() : ((Physical)msg.target()).phyStats().weight();
			if(targetWeight<(mob.phyStats().weight()/2))
			{
				if(msg.target() instanceof MOB)
				{
					final MOB TastyMorsel=(MOB)msg.target();
					final CMMsg msg2=CMClass.getMsg(mob,TastyMorsel,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE,null);
					if(TastyMorsel.location().okMessage(mob,msg2))
					{
						TastyMorsel.location().send(mob,msg2);
						if(msg2.value()<=0)
						{
							Stomach().bringMobHere(TastyMorsel,false);
							final CMMsg enterMsg=CMClass.getMsg(TastyMorsel,Stomach(),null,CMMsg.MSG_ENTER,L("<S-NAME> <S-IS-ARE> swallowed whole by @x1!",mob.name()),CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,L("<S-NAME> slide(s) down the gullet into the stomach!"));
							Stomach().send(TastyMorsel,enterMsg);
						}
					}
				}
				if((msg.target() instanceof Item)
				&&(!(msg.target() instanceof Food)))
					Stomach().moveItemTo((Item)msg.target(),ItemPossessor.Expire.Monster_EQ);
			}
		}

		if((msg.amISource(mob))
		&&((msg.sourceMinor()==CMMsg.TYP_QUIT)||(msg.sourceMinor()==CMMsg.TYP_DEATH)))
			kill();

		super.executeMsg(myHost,msg);
	}

	protected Room myStomach = null;
	protected Room lastKnownRoom=null;
	protected Room lastKnownLocation()
	{
		Room R=null;
		if(affected instanceof MOB)
			R=((MOB)affected).location();
		if(R==null)
			R=CMLib.map().roomLocation(invoker());
		if(R==null)
			R=CMLib.map().roomLocation(affected);
		if(R!=null)
			lastKnownRoom=R;
		return lastKnownRoom;
	}

	protected Room Stomach()
	{
		if((myStomach==null)&&(affected!=null))
		{
			myStomach = CMClass.getLocale("StdRoom");
			myStomach.setArea(CMLib.map().getRandomArea());
			myStomach.setName(L("The Stomach of @x1",affected.name()));
			myStomach.setDescription(L("You are in the stomach of @x1.  It is wet with digestive acids, and the walls are grinding you to a pulp.  You have been swallowed whole and are being digested.",affected.name()));
		}
		return myStomach;
	}

	protected int digestDown=4;

	public void kill()
	{
		if((Stomach()==null)||(lastKnownLocation()==null))
			return;

		// ===== move all inhabitants to the dragons location
		// ===== loop through all inhabitants of the stomach
		final int morselCount = Stomach().numInhabitants();
		for (int x=morselCount-1;x>=0;x--)
		{
			// ===== get the tasty morsels
			final MOB TastyMorsel = Stomach().fetchInhabitant(x);
			if(TastyMorsel!=null)
				lastKnownLocation().bringMobHere(TastyMorsel,false);
		}

		// =====move the inventory of the stomach to the room
		final int itemCount = Stomach().numItems();
		for (int y=itemCount-1;y>=0;y--)
		{
			final Item PartiallyDigestedItem = Stomach().getItem(y);
			if (PartiallyDigestedItem!=null)
			{
				lastKnownLocation().addItem(PartiallyDigestedItem,ItemPossessor.Expire.Player_Drop);
				Stomach().delItem(PartiallyDigestedItem);
			}
		}
		if((morselCount>0)||(itemCount>0))
			lastKnownLocation().recoverRoomStats();
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(invoker()==null)
			return true;
		final MOB mob=invoker();
		if((!mob.amDead())&&((--digestDown)<=0)&&(Stomach()!=null))
		{
			digestDown=2;
			for (int x=0;x<Stomach().numInhabitants();x++)
			{
				// ===== get a tasty morsel
				final MOB TastyMorsel = Stomach().fetchInhabitant(x);
				if (TastyMorsel != null)
				{
					final CMMsg DigestMsg=CMClass.getMsg(mob,
											   TastyMorsel,
											   null,
											   CMMsg.MASK_ALWAYS|CMMsg.TYP_ACID,
											   L("<S-NAME> digest(s) <T-NAMESELF>!!"));
					// no OKaffectS, since the dragon is not in his own stomach.
					Stomach().send(mob,DigestMsg);
					int damage=(int)Math.round(CMath.div(TastyMorsel.curState().getHitPoints(),2));
					if(damage<(TastyMorsel.phyStats().level()+6))
						damage=TastyMorsel.curState().getHitPoints()*100;
					CMLib.combat().postDamage(mob,TastyMorsel,null,damage,CMMsg.MASK_ALWAYS|CMMsg.TYP_ACID,Weapon.TYPE_MELTING,L("The stomach acid <DAMAGE> <T-NAME>!"));
				}
			}
		}
		else
		if(mob.amDead())
			kill();

		if((affected instanceof Room)
		&&(((Room)affected).numInhabitants()==0))
			unInvoke();
		return true;
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(affected==null)
			return;

		final Environmental thang=affected;
		super.unInvoke();

		if(canBeUninvoked())
		{
			if(thang instanceof MOB)
			{
				((MOB)thang).tell(L("Your mouth shrinks to normal size."));
				if((Stomach()!=null)&&(Stomach().numInhabitants()>0))
				{
					unInvoked=false;
					final Spell_BigMouth A =(Spell_BigMouth)this.copyOf();
					A.startTickDown(invoker,Stomach(),10000);
				}
			}
			else
			if(thang instanceof Room)
				kill();
		}
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if(target==null)
			return false;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,L("<S-NAME> <S-IS-ARE> already the owner of a huge mouth."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> invoke(s) a spell.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final Ability A=target.fetchEffect("Spell_ShrinkMouth");
				boolean isJustUnInvoking=false;
				if((A!=null)&&(A.canBeUninvoked()))
				{
					A.unInvoke();
					isJustUnInvoking=true;
				}
				if((!isJustUnInvoking)&&(msg.value()<=0))
				{
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> feel(s) <S-HIS-HER> mouth grow to an enormous size!"));
					beneficialAffect(mob,target,asLevel,4);
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> attempt(s) to invoke a spell, but fail(s) miserably."));

		// return whether it worked
		return success;
	}
}
