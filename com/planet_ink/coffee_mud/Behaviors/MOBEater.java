package com.planet_ink.coffee_mud.Behaviors;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary.SkillCost;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.lang.ref.WeakReference;
import java.util.*;

/*
   Copyright 2000-2018 Mike Rundell

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

public class MOBEater extends ActiveTicker
{
	@Override
	public String ID()
	{
		return "MOBEater";
	}

	@Override
	protected int canImproveCode()
	{
		return Behavior.CAN_MOBS;
	}

	@Override
	public long flags()
	{
		return super.flags() | Behavior.FLAG_POTENTIALLYAUTODEATHING;
	}

	protected Room	stomachR			= null;
	protected int	digestDown			= 4;
	protected Room	lastKnownLocationR	= null;
	protected MOB	lastKnownEaterM		= null;
	protected int	chanceToEat			= 5;
	protected int	pctAcidHp			= 10;

	@Override
	public String accountForYourself()
	{
		return "creature eating";
	}

	public MOBEater()
	{
		super();
		minTicks=4; maxTicks=8; chance=50;
		tickReset();
	}

	@Override
	public void setParms(String parms)
	{
		super.setParms(parms);
		pctAcidHp=CMParms.getParmInt(parms,"acidpct",50);
	}

	@Override
	public void startBehavior(final PhysicalAgent forMe)
	{
		if((forMe!=null)&&(forMe instanceof MOB))
		{
			if(stomachR==null)
				stomachR = CMClass.getLocale("StoneRoom");
			lastKnownEaterM=(MOB)forMe;
			lastKnownLocationR=((MOB)forMe).location();
			if(lastKnownLocationR!=null)
				stomachR.setArea(lastKnownLocationR.getArea());
			stomachR.setDisplayText(L("The Stomach of @x1",forMe.name()));
			stomachR.setName(L("the stomach of @x1",forMe.name()));
			stomachR.setDescription(L("You are in the stomach of @x1.  It is wet with digestive acids, and the walls are grinding you to a pulp.  You have been Swallowed whole and are being digested.",forMe.name()));
			stomachR.addNonUninvokableEffect(CMClass.getAbility("Prop_NoRecall"));
			stomachR.addNonUninvokableEffect(CMClass.getAbility("Prop_NoTeleportOut"));
			final ExtendableAbility A=(ExtendableAbility)CMClass.getAbility("ExtAbility");
			final WeakReference<MOB> eater=new WeakReference<MOB>(lastKnownEaterM);
			stomachR.addNonUninvokableEffect(A.setAbilityID("MOBEaterStomachWatcher").setMsgListener(new MsgListener(){
				@Override
				public void executeMsg(Environmental myHost, CMMsg msg) 
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
				public boolean okMessage(Environmental myHost, CMMsg msg)
				{
					if((msg.sourceMinor()==CMMsg.TYP_COMMANDFAIL)
					&&(msg.targetMessage()!=null)
					&&(msg.targetMessage().length()>0))
					{
						final char c=Character.toUpperCase(msg.targetMessage().charAt(0));
						if((c=='K')||(c=='A'))
						{
							List<String> parsedFail = CMParms.parse(msg.targetMessage());
							String cmd=parsedFail.get(0).toUpperCase();
							if("ATTACK".startsWith(cmd)||"KILL".startsWith(cmd))
							{
								String rest = CMParms.combine(parsedFail,1).toUpperCase().trim();
								if("HERE".equals(rest)||"STOMACH".startsWith(rest)||"WALLS".startsWith(rest))
								{
									Item I=msg.source().fetchWieldedItem();
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
									final Weapon weapon=(Weapon)I;
									final int dmg = CMLib.combat().adjustedDamage(msg.source(), (Weapon)I, (MOB)forMe, 0, true, false)/10;
									MOB M=CMClass.getFactoryMOB(L("Someone inside @x1",forMe.name()), msg.source().phyStats().level(), ((MOB)forMe).location());
									try
									{
										if(stomachR.show(msg.source(), forMe, CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> @x1 <T-YOUPOSS> stomach with @x2!",CMLib.combat().standardHitWord(weapon.weaponDamageType(),dmg),I.name(msg.source()))))
											CMLib.combat().postDamage(M, (MOB)forMe, I, dmg, CMMsg.MSG_WEAPONATTACK, weapon.weaponDamageType(), L("<S-NAME> <DAMAGE> <T-HIM-HER>!"));
									}
									finally
									{
										M.destroy();
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
	}

	public void kill()
	{
		if((lastKnownLocationR==null)
		||(stomachR==null)) 
			return;

		// ===== move all inhabitants to the dragons location
		// ===== loop through all inhabitants of the stomach
		final Vector<Physical> these=new Vector<Physical>();
		for (int x=0;x<stomachR.numInhabitants();x++)
		{
			// ===== get the tasty morsels
			final MOB tastyMorselM = stomachR.fetchInhabitant(x);
			if(tastyMorselM!=null)
				these.addElement(tastyMorselM);
		}
		
		for(Enumeration<MOB> p=CMLib.players().players();p.hasMoreElements();)
		{
			final MOB M=p.nextElement();
			if((M!=null)&&(M.location()==stomachR)&&(!CMLib.flags().isInTheGame(M,true))&&(!these.contains(M)))
			{
				M.setLocation(lastKnownLocationR);
				for(int f=0;f<M.numFollowers();f++)
				{
					MOB F=M.fetchFollower(f);
					if((F!=null)&&(F.location()==stomachR)&&(!CMLib.flags().isInTheGame(F,true))&&(!these.contains(F)))
						F.setLocation(lastKnownLocationR);
				}
				CMLib.database().DBUpdatePlayerMOBOnly(M);
			}
		}

		// =====move the inventory of the stomach to the room
		for (int y=0;y<stomachR.numItems();y++)
		{
			final Item PartiallyDigestedItem = stomachR.getItem(y);
			if((PartiallyDigestedItem!=null)&&(PartiallyDigestedItem.container()==null))
				these.addElement(PartiallyDigestedItem);
		}

		for(int i=0;i<these.size();i++)
		{
			if(these.elementAt(i) instanceof Item)
				lastKnownLocationR.moveItemTo((Item)these.elementAt(i),ItemPossessor.Expire.Player_Drop);
			else
			if(these.elementAt(i) instanceof MOB)
				lastKnownLocationR.bringMobHere((MOB)these.elementAt(i),false);
		}
		stomachR.recoverPhyStats();
		lastKnownLocationR.recoverRoomStats();
		lastKnownLocationR=null;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if(ticking==null) 
			return true;
		if(!(ticking instanceof MOB))
			return true;

		final MOB mob=(MOB)ticking;
		if(this.lastKnownEaterM!=mob)
			this.lastKnownEaterM=mob;
		if(mob.location()!=null)
			lastKnownLocationR=mob.location();

		if((--digestDown)<=0)
		{
			digestDown=4;
			digestTastyMorsels(mob);
		}

		if((canAct(ticking,tickID))
		&&(((MOB)ticking).isInCombat())
		&&(!mob.amDead()))
			trySwallowWhole(mob);
		return true;
	}

	@Override
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(this.lastKnownEaterM != null)
		{
			if(((msg.amISource(this.lastKnownEaterM))&&(msg.sourceMinor()==CMMsg.TYP_DEATH))
			||(!CMLib.flags().isInTheGame(this.lastKnownEaterM, true)))
				kill();
		}
		super.executeMsg(myHost,msg);
	}

	protected boolean trySwallowWhole(MOB mob)
	{
		if(stomachR==null) 
			return true;
		if (CMLib.flags().isAliveAwakeMobile(mob,true)
			&&(mob.rangeToTarget()==0)
			&&(CMLib.flags().canHear(mob)||CMLib.flags().canSee(mob)||CMLib.flags().canSmell(mob)))
		{
			final MOB tastyMorselM = mob.getVictim();
			if(tastyMorselM==null) 
				return true;
			if (tastyMorselM.baseWeight()<(mob.phyStats().weight()/2))
			{
				// ===== The player has been eaten.
				// ===== move the tasty morsel to the stomach
				final CMMsg eatMsg=CMClass.getMsg(mob,
												  tastyMorselM,
												  null,
												  CMMsg.MSG_EAT,
												  CMMsg.MASK_ALWAYS|CMMsg.TYP_JUSTICE,
												  CMMsg.MSG_NOISYMOVEMENT,
												  L("<S-NAME> swallow(es) <T-NAMESELF> WHOLE!"));
				if(mob.location().okMessage(tastyMorselM,eatMsg))
				{
					mob.location().send(tastyMorselM,eatMsg);
					if(eatMsg.value()==0)
					{
						stomachR.bringMobHere(tastyMorselM,false);
						final CMMsg enterMsg=CMClass.getMsg(tastyMorselM,stomachR,null,CMMsg.MSG_ENTER,stomachR.description(),CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,L("<S-NAME> slide(s) down the gullet into the stomach!"));
						stomachR.send(tastyMorselM,enterMsg);
					}
				}
			}
		}
		return true;
	}

	protected boolean digestTastyMorsels(MOB mob)
	{
		if(stomachR==null) 
			return true;
		// ===== loop through all inhabitants of the stomach
		final int morselCount = stomachR.numInhabitants();
		for (int x=0;x<morselCount;x++)
		{
			// ===== get a tasty morsel
			final MOB tastyMorselM = stomachR.fetchInhabitant(x);
			if (tastyMorselM != null)
			{
				final CMMsg DigestMsg=CMClass.getMsg(mob,
													 tastyMorselM,
													 null,
													 CMMsg.MASK_ALWAYS|CMMsg.TYP_ACID,
													 L("<S-NAME> digest(s) <T-NAMESELF>!!"));
				// no OKaffectS, since the dragon is not in his own stomach.
				stomachR.send(mob,DigestMsg);
				int damage=(int)Math.round(tastyMorselM.maxState().getHitPoints() * CMath.div(pctAcidHp, 100));
				if(damage<2) 
					damage=2;
				if(DigestMsg.value()!=0) 
					damage=damage/2;
				CMLib.combat().postDamage(mob,tastyMorselM,null,damage,CMMsg.MASK_ALWAYS|CMMsg.TYP_ACID,Weapon.TYPE_MELTING,L("The stomach acid <DAMAGE> <T-NAME>!"));
			}
		}
		return true;
	}
}
