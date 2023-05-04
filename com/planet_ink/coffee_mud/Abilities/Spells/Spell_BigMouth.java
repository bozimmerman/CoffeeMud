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
   Copyright 2003-2023 Bo Zimmerman

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
		&&(getStomach()!=null))
		{
			final CMMsg maliciousNessMsg=CMClass.getMsg(msg.source(), msg.target(), CMMsg.MSG_OK_ACTION | CMMsg.MASK_MALICIOUS, null);
			final int targetWeight = (msg.target() instanceof MOB) ? ((MOB)msg.target()).baseWeight() : ((Physical)msg.target()).phyStats().weight();
			final Room stomachR=getStomach();
			if((targetWeight<(mob.baseWeight()/3)) // THIS is min size, not stomach capacity
			&&(mob.location()!=null))
			{
				if(mob.location().okMessage(myHost, maliciousNessMsg))
				{
					final int maxInhabitants=1+((mob.fetchAbility(ID())!=null)?(super.getXLEVELLevel(mob)/2):0);
					if((stomachR!=null)
					&&(stomachR.numInhabitants()>maxInhabitants))
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
		&&(getStomach()!=null))
		{
			final Room stomachR=getStomach();
			final int targetWeight = (msg.target() instanceof MOB) ? ((MOB)msg.target()).baseWeight() : ((Physical)msg.target()).phyStats().weight();
			if(targetWeight<(mob.baseWeight()/3))
			{
				if(msg.target() instanceof MOB)
				{
					final MOB tastyMorselM=(MOB)msg.target();
					final CMMsg msg2=CMClass.getMsg(mob,tastyMorselM,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE,null);
					if(tastyMorselM.location().okMessage(mob,msg2))
					{
						tastyMorselM.location().send(mob,msg2);
						if(msg2.value()<=0)
						{
							mob.curState().setHunger(mob.maxState().maxHunger(mob.baseWeight()));
							stomachR.bringMobHere(tastyMorselM,false);
							final CMMsg enterMsg=CMClass.getMsg(tastyMorselM,stomachR,null,CMMsg.MSG_ENTER,L("<S-NAME> <S-IS-ARE> swallowed whole by @x1!",mob.name()),CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,L("<S-NAME> slide(s) down the gullet into the stomach!"));
							stomachR.send(tastyMorselM,enterMsg);
						}
						else
							msg.addTrailerMsg(CMClass.getMsg(tastyMorselM, null, CMMsg.MSG_OK_VISUAL, L("<S-NAME> avoid(s) being eaten!")));
					}
				}
				else
				if((msg.target() instanceof Item)
				&&(!(msg.target() instanceof Food)))
					stomachR.moveItemTo((Item)msg.target(),ItemPossessor.Expire.Monster_EQ);
			}
		}

		if((msg.amISource(mob))
		&&((msg.sourceMinor()==CMMsg.TYP_QUIT)||(msg.sourceMinor()==CMMsg.TYP_DEATH)))
			kill();

		super.executeMsg(myHost,msg);
	}

	protected Room myStomachR = null;
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

	protected Room getStomach()
	{
		if(myStomachR==null)
		{
			if(!(affected instanceof MOB))
				return null;
			final MOB forMe = (MOB)affected;
			myStomachR = CMClass.getLocale("StdRoom");
			myStomachR.setSavable(false);
			myStomachR.setRoomID("");
			myStomachR.setArea(CMLib.map().getRandomArea());
			myStomachR.setName(L("The Stomach of @x1",affected.name()));
			myStomachR.setDescription(L("You are in the stomach of @x1.  It is wet with digestive acids, and the walls are grinding you to a pulp.  You have been swallowed whole and are being digested.",affected.name()));
			myStomachR.addNonUninvokableEffect(CMClass.getAbility("Prop_NoRecall"));
			myStomachR.addNonUninvokableEffect(CMClass.getAbility("Prop_NoTeleportOut"));
			final ExtendableAbility A=(ExtendableAbility)CMClass.getAbility("ExtAbility");
			final WeakReference<MOB> eater=new WeakReference<MOB>(forMe);
			myStomachR.addNonUninvokableEffect(A.setAbilityID("MOBEaterStomachWatcher").setMsgListener(new MsgListener()
			{
				@Override
				public void executeMsg(final Environmental myHost, final CMMsg msg)
				{
					if(A.affecting() instanceof Room)
					{
						if((eater.get()==null)||(eater.get().amDestroyed())||(eater.get().amDead()))
						{
							CMLib.map().emptyRoom((Room)A.affecting(), null, true);
						}
					}
				}

				@Override
				public boolean okMessage(final Environmental myHost, final CMMsg msg)
				{
					if((msg.sourceMinor()==CMMsg.TYP_COMMANDFAIL)
					&&(msg.targetMessage()!=null)
					&&(msg.targetMessage().length()>0))
					{
						final char c=Character.toUpperCase(msg.targetMessage().charAt(0));
						if((c=='K')||(c=='A'))
						{
							final List<String> parsedFail = CMParms.parse(msg.targetMessage());
							final String cmd=parsedFail.get(0).toUpperCase();
							if("ATTACK".startsWith(cmd)||"KILL".startsWith(cmd))
							{
								final String rest = CMParms.combine(parsedFail,1).toUpperCase().trim();
								if("HERE".equals(rest)||"STOMACH".startsWith(rest)||"WALLS".startsWith(rest))
								{
									Item I=msg.source().fetchWieldedItem();
									if(I == null)
										I=msg.source().getNaturalWeapon();
									if((!(I instanceof Weapon))
									||(I.minRange()>0))
									{
										msg.source().tell(L("You aren't wielding an appropriate weapon."));
										return false;
									}
									if(msg.source().getPeaceTime()<CMProps.getTickMillis())
									{
										msg.source().tell(L("You are too busy trying to survive right now!"));
										return false;
									}
									if(!CMLib.combat().rollToHit(msg.source(), forMe))
									{
										final Weapon weapon=(Weapon)I;
										msg.source().tell(L("Your @x1 attack fails to penetrate the stomach.", weapon.name(msg.source())));
									}
									else
									{
										final Weapon weapon=(Weapon)I;
										final int dmg = CMLib.combat().adjustedDamage(msg.source(), (Weapon)I, forMe, 0, true, false)/10;
										final MOB M=CMClass.getFactoryMOB(L("Someone inside @x1",forMe.name()), msg.source().phyStats().level(), forMe.location());
										try
										{
											if(myStomachR.show(msg.source(), forMe, CMMsg.MSG_NOISYMOVEMENT,
													L("<S-NAME> @x1 <T-YOUPOSS> stomach with @x2!",CMLib.combat().standardHitWord(weapon.weaponDamageType(),dmg),
													I.name(msg.source()))))
											{
												CMLib.combat().postDamage(M, forMe, I, dmg, CMMsg.MSG_WEAPONATTACK, weapon.weaponDamageType(),
														L("<S-NAME> <DAMAGE> <T-HIM-HER>!"));
											}
										}
										finally
										{
											M.destroy();
										}
									}
									return false;
								}
							}
						}
					}
					return true;
				}
			}));
		}
		return myStomachR;
	}

	protected int digestDown=4;

	public void kill()
	{
		final Room stomachR=getStomach();
		if((stomachR==null)||(lastKnownLocation()==null))
			return;

		// ===== move all inhabitants to the dragons location
		// ===== loop through all inhabitants of the stomach
		final int morselCount = stomachR.numInhabitants();
		for (int x=morselCount-1;x>=0;x--)
		{
			// ===== get the tasty morsels
			final MOB TastyMorsel = stomachR.fetchInhabitant(x);
			if(TastyMorsel!=null)
				lastKnownLocation().bringMobHere(TastyMorsel,false);
		}

		// =====move the inventory of the stomach to the room
		final int itemCount = stomachR.numItems();
		for (int y=itemCount-1;y>=0;y--)
		{
			final Item partiallyDigestedItem = stomachR.getItem(y);
			if (partiallyDigestedItem!=null)
			{
				lastKnownLocation().addItem(partiallyDigestedItem,ItemPossessor.Expire.Player_Drop);
				stomachR.delItem(partiallyDigestedItem);
			}
		}
		if((morselCount>0)||(itemCount>0))
			lastKnownLocation().recoverRoomStats();
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(!(affected instanceof MOB))
			return true;
		final MOB mob=(MOB)affected;
		final Room stomachR=getStomach();
		if((!mob.amDead())
		&&(stomachR!=null))
		{
			if((--digestDown)<=0)
			{
				digestDown=2;
				for (int x=0;x<stomachR.numInhabitants();x++)
				{
					// ===== get a tasty morsel
					final MOB tastyMorselM = stomachR.fetchInhabitant(x);
					if (tastyMorselM != null)
					{
						final CMMsg digestMsg=CMClass.getMsg(mob,
															 tastyMorselM,
															 null,
															 CMMsg.MASK_ALWAYS|CMMsg.TYP_ACID,
															 L("<S-NAME> digest(s) <T-NAMESELF>."));
						// no OKaffectS, since the dragon is not in his own stomach.
						stomachR.send(mob,digestMsg);
						int damage=(int)Math.round(CMath.mul(tastyMorselM.curState().getHitPoints(), (0.15 + (0.01 * super.getXLEVELLevel(mob)))));
						if(damage<tastyMorselM.maxState().getHitPoints()/15)
							damage=tastyMorselM.maxState().getHitPoints()/15;
						if(damage<2)
							damage=2;
						if(digestMsg.value()!=0)
							damage=damage/2;
						CMLib.combat().postDamage(mob,tastyMorselM,null,damage,CMMsg.MASK_ALWAYS|CMMsg.TYP_ACID,Weapon.TYPE_MELTING,
								L("The stomach acid <DAMAGE> <T-NAME>!"));
					}
				}
			}
			for (int x=0;x<stomachR.numInhabitants();x++)
			{
				// ===== get a tasty morsel and allow them to fight back
				final MOB tastyMorselM = stomachR.fetchInhabitant(x);
				if((tastyMorselM != null)
				&&(tastyMorselM.isMonster()))
					tastyMorselM.enqueCommand(new XVector<String>("ATTACK", "STOMACH"), MUDCmdProcessor.METAFLAG_FORCED, 1.0);
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
				final Room stomachR=getStomach();
				((MOB)thang).tell(L("Your mouth shrinks to normal size."));
				if((stomachR!=null)&&(stomachR.numInhabitants()>0))
				{
					unInvoked=false;
					final Spell_BigMouth A =(Spell_BigMouth)this.copyOf();
					A.startTickDown(invoker,stomachR,10000);
				}
			}
			else
			if(thang instanceof Room)
				kill();
		}
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		MOB target=mob;
		if(target==null)
			return false;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(this.ID())!=null)
		{
			failureTell(mob,target,auto,L("<S-NAME> <S-IS-ARE> already the owner of a huge mouth."));
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
					beneficialAffect(mob,target,asLevel,0);
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> attempt(s) to invoke a spell, but fail(s) miserably."));

		// return whether it worked
		return success;
	}
}
