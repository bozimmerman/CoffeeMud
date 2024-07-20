package com.planet_ink.coffee_mud.MOBS;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Event;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2000-2024 Mike Rundell

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
public class Dragon extends StdMOB implements MOBPossessor
{
	@Override
	public String ID()
	{
		return "Dragon";
	}

	protected int breatheDown=4;
	protected int swallowDown=5;
	protected int digestDown=4;

	protected int birthColor=0;
	protected int birthAge=0;

	protected Ability dragonbreath = null;

	// ===== Defined Values for Dragon Ages
	public final static int HATCHLING			= 0;	// 10
	public final static int VERYYOUNG			= 1;	// 10
	public final static int YOUNG				= 2;	// 15
	public final static int SUBADULT			= 3;	// 20
	public final static int YOUNGADULT			= 4;	// 22
	public final static int ADULT				= 5;	// 15
	public final static int OLD					= 6;	// 10
	public final static int VERYOLD				= 7;	// 5
	public final static int ANCIENT				= 8;	// 3

	public final static int DRAGONCOLORCOUNT	= 10;

	// ===== Defined Values for Dragon Colors
	public final static int WHITE				= 0;
	public final static int BLACK				= 1;
	public final static int BLUE				= 2;
	public final static int GREEN				= 3;
	public final static int RED					= 4;
	public final static int BRASS				= 5;
	public final static int COPPER				= 6;
	public final static int BRONZE				= 7;
	public final static int SILVER				= 8;
	public final static int GOLD				= 9;

	// ===== Defined Value for holding the Dragon Type
	protected int DragonColor()
	{
		return basePhyStats().ability();
	}

	protected int DragonAge()
	{
		return basePhyStats().level() / 10;
	}

	protected Room myStomachR = null;

	// ===== random constructor
	public Dragon()
	{
		// ===== creates a random color and age of dragon
		this((short)Math.round(Math.random()*DRAGONCOLORCOUNT));
	}

	// ===== constructs a dragon of a specified color, but a random age
	public Dragon(final int colorValue)
	{
		this(colorValue,determineAge());
	}

	// ===== public constructor
	public Dragon(final int colorValue, final int ageValue)
	{
		super();
		basePhyStats().setAbility(colorValue);
		basePhyStats().setLevel(5+(ageValue*10));
		birthColor=0;
		birthAge=0;
		setupDragonIfNecessary();
	}

	public void setupDragonIfNecessary()
	{
		// ===== set the parameter stuff		DragonAge() = ageValue;

		if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			return;
		if((DragonAge()==birthAge)&&(DragonColor()==birthColor))
			return;
		final int colorValue=DragonColor();
		final int ageValue=DragonAge();

		birthAge=ageValue;
		birthColor=colorValue;

		// ===== is it a male or female
		final short gend = (short)Math.round(Math.random());
		if (gend == 0)
		{
			baseCharStats().setStat(CharStats.STAT_GENDER,'F');
		}
		else
		{
			baseCharStats().setStat(CharStats.STAT_GENDER,'M');
		}
		// ===== set the basics
		setName(getAgeDescription(DragonAge()).toString() + " " + getColorDescription(DragonColor()) + " Dragon");
		setDescription("A majestic " + getColorDescription(DragonColor()) + " Dragon, simply being in its presence makes you uneasy.");
		setDisplayText(getAgeDescription(DragonAge()).toString() + " " + getColorDescription(DragonColor()) + " Dragon watches you intently.");

		// ===== arm him
		final Weapon ClawOne=CMClass.getWeapon("DragonClaw");
		final Weapon ClawTwo=CMClass.getWeapon("DragonClaw");
		if(ClawOne!=null)
		{
			ClawOne.basePhyStats().setLevel(basePhyStats().level());
			ClawOne.basePhyStats().setDamage(basePhyStats().level());
			ClawOne.recoverPhyStats();
			ClawTwo.basePhyStats().setLevel(basePhyStats().level());
			ClawOne.basePhyStats().setDamage(basePhyStats().level());
			ClawTwo.recoverPhyStats();

			ClawOne.wearAt(Wearable.WORN_WIELD);
			ClawTwo.wearAt(Wearable.WORN_WIELD);

			addItem(ClawOne);
			addItem(ClawTwo);
		}

		// ===== hitpoints are muxed by 10 To beef them up
		int PointMod = 1;

		// ===== set the mod based on the color
		switch (DragonColor())
		{
		case WHITE:
			PointMod = 1;
			CMLib.factions().setAlignment(this, Faction.Align.EVIL);
			break;
		case BLACK:
			PointMod = 2;
			CMLib.factions().setAlignment(this, Faction.Align.EVIL);
			break;
		case BLUE:
			PointMod = 3;
			CMLib.factions().setAlignment(this, Faction.Align.EVIL);
			break;
		case GREEN:
			PointMod = 4;
			CMLib.factions().setAlignment(this, Faction.Align.EVIL);
			break;
		case RED:
			PointMod = 5;
			CMLib.factions().setAlignment(this, Faction.Align.EVIL);
			break;
		case BRASS:
			PointMod = 1;
			CMLib.factions().setAlignment(this, Faction.Align.GOOD);
			break;
		case COPPER:
			PointMod = 2;
			CMLib.factions().setAlignment(this, Faction.Align.GOOD);
			break;
		case BRONZE:
			PointMod = 3;
			CMLib.factions().setAlignment(this, Faction.Align.GOOD);
			break;
		case SILVER:
			PointMod = 4;
			CMLib.factions().setAlignment(this, Faction.Align.GOOD);
			break;
		case GOLD:
			PointMod = 5;
			CMLib.factions().setAlignment(this, Faction.Align.GOOD);
			break;
		default:
			PointMod = 3;
			CMLib.factions().setAlignment(this, Faction.Align.NEUTRAL);
			break;
		}

		CMLib.leveler().fillOutMOB(this,basePhyStats().level());
		baseState.setHitPoints(baseState.getHitPoints() * PointMod);
		setMoney(getMoney()*PointMod);
		basePhyStats().setWeight(1500 * DragonAge());

		// ===== Dragons never flee.
		setWimpHitPoint(0);

		// ===== Dragons get tougher with age
		for(final int i : CharStats.CODES.BASECODES())
			baseCharStats().setStat(i,13 + (DragonAge()*2));
		baseCharStats().setMyRace(CMClass.getRace("Dragon"));
		baseCharStats().getMyRace().startRacing(this,false);

		// ===== if the dragon is an adult or larger add the swallow whole
		myStomachR=null;
		// ===== Recover from birth.
		recoverMaxState();
		resetToMaxState();
		recoverPhyStats();
		recoverCharStats();
	}

	protected static int determineAge()
	{
		// ===== Get a percent chance
		final int iRoll = CMLib.dice().rollPercentage()+1;

		// ===== Determine the age based upon this
		if (iRoll == 1)
			return HATCHLING;
		if (iRoll <= 10)
			return VERYYOUNG;
		if (iRoll <= 25)
			return YOUNG;
		if (iRoll <= 45)
			return SUBADULT;
		if (iRoll <= 67)
			return YOUNGADULT;
		if (iRoll <= 82)
			return ADULT;
		if (iRoll <= 92)
			return OLD;
		if (iRoll <= 97)
			return VERYOLD;
		if (iRoll <= 100)
			return ANCIENT;
		return SUBADULT;
	}

	protected StringBuffer getAgeDescription(final int draconianAge)
	{
		StringBuffer returnVal = null;

		// ===== return a string that represents the age of the Dragon
		switch (draconianAge)
		{
		case HATCHLING:
			returnVal = new StringBuffer("a hatchling");
			break;
		case VERYYOUNG:
			returnVal = new StringBuffer("a very young");
			break;
		case YOUNG:
			returnVal = new StringBuffer("a young");
			break;
		case SUBADULT:
			returnVal = new StringBuffer("a sub-adult");
			break;
		case YOUNGADULT:
			returnVal = new StringBuffer("a young adult");
			break;
		case ADULT:
			returnVal = new StringBuffer("an adult");
			break;
		case OLD:
			returnVal = new StringBuffer("an old");
			break;
		case VERYOLD:
			returnVal = new StringBuffer("a very old");
			break;
		case ANCIENT:
			returnVal = new StringBuffer("an ancient");
			break;
		default:
			returnVal = new StringBuffer("a");
			break;
		}

		return returnVal;
	}

	protected StringBuffer getColorDescription(final int colorVal)
	{
		StringBuffer returnVal = null;

		// ===== return the color of the dragon
		switch (colorVal)
		{
		case WHITE:
			returnVal = new StringBuffer("White");
			break;
		case BLACK:
			returnVal = new StringBuffer("Black");
			break;
		case BLUE:
			returnVal = new StringBuffer("Blue");
			break;
		case GREEN:
			returnVal = new StringBuffer("Green");
			break;
		case RED:
			returnVal = new StringBuffer("Red");
			break;
		case BRASS:
			returnVal = new StringBuffer("Brass");
			break;
		case COPPER:
			returnVal = new StringBuffer("Copper");
			break;
		case BRONZE:
			returnVal = new StringBuffer("Bronze");
			break;
		case SILVER:
			returnVal = new StringBuffer("Silver");
			break;
		case GOLD:
			returnVal = new StringBuffer("Gold");
			break;
		default:
			returnVal = new StringBuffer("Unknown");
			break;
		}

		return returnVal;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(tickID!=Tickable.TICKID_MOB)
			return super.tick(ticking, tickID);
		setupDragonIfNecessary();
		if(!amDead())
		{
			if((myStomachR==null)
			&&(location()!=null)
			&&(DragonAge()>=ADULT))
			{
				myStomachR = CMClass.getLocale("StoneRoom");
				if(myStomachR!=null)
				{
					myStomachR.setSavable(false);
					myStomachR.setRoomID("");
					myStomachR.setName(L("Dragon Stomach"));
					myStomachR.setDisplayText(L("Dragon Stomach"));
					myStomachR.setArea(location().getArea());
					myStomachR.setDescription(L("You are in the stomach of a dragon.  It is wet with digestive acids, and the walls are grinding you to a pulp.  You have been Swallowed whole and are being digested."));
				}
			}
			if((--digestDown)<=0)
			{
				digestDown=4;
				digestTastyMorsels();
			}
			if (isInCombat())
			{
				if((--breatheDown)<=0)
				{
					breatheDown=4;
					useBreathWeapon();
				}
				if((--swallowDown)<=0)
				{
					swallowDown=4;
					trySwallowWhole();
				}
			}

		}
		return super.tick(ticking,tickID);
	}

	protected boolean useBreathWeapon()
	{
		// ===== the text to post
		MOB target = null;
		int affectCode = CMMsg.TYP_JUSTICE;
		int weaponType= Weapon.TYPE_BURNING;
		String msgText = "";

		// ===== if we are following don't Breath, we might
		//  	 hurt the one we follow...
		if (amFollowing()!=null)
		{
			// ===== if we breath we might hurt him
			return true;
		}

		if(!CMLib.flags().canBreatheHere(this,location()))
		{
			// ===== if you can't breathe, you can't breathe fire
			return false;
		}

		// ===== Tell What the Beast is doing
		switch (DragonColor())
		{
		case WHITE:
			msgText = "The dragon breathes frost at <T-NAME>.";
			affectCode = CMMsg.TYP_COLD;
			weaponType= Weapon.TYPE_FROSTING;
			break;
		case BLACK:
			msgText = "The dragon spits acid at <T-NAME>.";
			affectCode = CMMsg.TYP_ACID;
			weaponType= Weapon.TYPE_MELTING;
			break;
		case BLUE:
			msgText = "Lightning shoots forth from the dragons mouth striking <T-NAME>.";
			affectCode = CMMsg.TYP_ELECTRIC;
			weaponType= Weapon.TYPE_STRIKING;
			break;
		case GREEN:
			msgText = "The dragon breathes a cloud of noxious vapors choking <T-NAME>.";
			affectCode = CMMsg.TYP_GAS;
			weaponType= Weapon.TYPE_GASSING;
			break;
		case RED:
			msgText = "The dragon torches <T-NAME> with fiery breath!.";
			affectCode = CMMsg.TYP_FIRE;
			weaponType= Weapon.TYPE_BURNING;
			break;
		case BRASS:
			msgText = "The dragon cooks <T-NAME> with a blast of pure heat!.";
			affectCode = CMMsg.TYP_FIRE;
			weaponType= Weapon.TYPE_BURNING;
			break;
		case COPPER:
			msgText = "The dragon spits acid at <T-NAME>.";
			affectCode = CMMsg.TYP_ACID;
			weaponType= Weapon.TYPE_MELTING;
			break;
		case BRONZE:
			msgText = "Lightning shoots forth from the dragons mouth striking <T-NAME>.";
			affectCode = CMMsg.TYP_ELECTRIC;
			weaponType= Weapon.TYPE_STRIKING;
			break;
		case SILVER:
			msgText = "The dragon breathes frost at <T-NAME>.";
			affectCode = CMMsg.TYP_COLD;
			weaponType= Weapon.TYPE_FROSTING;
			break;
		case GOLD:
			if ((int)Math.round(Math.random())==1)
			{
				msgText = "The dragon torches <T-NAME> with fiery breath!.";
				affectCode = CMMsg.TYP_FIRE;
				weaponType= Weapon.TYPE_BURNING;
			}
			else
			{
				msgText = "The dragon breathes a cloud of noxious vapors choking <T-NAME>.";
				affectCode = CMMsg.TYP_GAS;
				weaponType= Weapon.TYPE_GASSING;
			}
			break;
		default:
			return false;
		}

		final Room room=location();
		if(room==null)
			return false;
		for (int x=0;x<room.numInhabitants();x++)
		{
			// ===== get the next target
			target = room.fetchInhabitant(x);
			// ===== do not attack yourself
			if ((target!=null)&&(!target.ID().equals(ID())))
			{
				final CMMsg message = CMClass.getMsg(this,
											  target,
											  null,
											  CMMsg.MSK_MALICIOUS_MOVE|affectCode,
											  CMMsg.MSK_MALICIOUS_MOVE|affectCode,
											  CMMsg.MSG_NOISYMOVEMENT,
											  msgText);
				if (room.okMessage(this,message))
				{
					room.send(this,message);
					int damage=((short)Math.round(CMath.div(CMath.mul(Math.random(),7*DragonAge()),2.0)));
					if(message.value()<=0)
						damage=((short)Math.round(Math.random()*7)*DragonAge());
					if(dragonbreath==null)
						dragonbreath=CMClass.getAbility("Dragonbreath");
					CMLib.combat().postDamage(this,target,dragonbreath,damage,CMMsg.MASK_ALWAYS|affectCode,weaponType,L("The blast <DAMAGE> <T-NAME>."));
				}
			}
		}
		return true;
	}

	public Room getStomach()
	{
		return myStomachR;
	}

	protected boolean trySwallowWhole()
	{
		final Room myStomachR = getStomach();
		if(myStomachR==null)
			return true;
		if (CMLib.flags().isAliveAwakeMobileUnbound(this,true)
			&&(rangeToTarget()==0)
			&&(CMLib.flags().canHear(this)||CMLib.flags().canSee(this)||CMLib.flags().canSmell(this)))
		{
			final MOB tastyMorselM = getVictim();
			if(tastyMorselM==null)
				return true;
			if (tastyMorselM.phyStats().weight()<1500)
			{
				// ===== if it is less than three so roll for it
				final int roll = (int)Math.round(Math.random()*99);

				// ===== check the result
				if (roll<2)
				{
					// ===== The player has been eaten.
					// ===== move the tasty morsel to the stomach
					final CMMsg eatMsg=CMClass.getMsg(this,
											   tastyMorselM,
											   null,
											   CMMsg.MSG_EAT,
											   CMMsg.MASK_ALWAYS|CMMsg.TYP_JUSTICE,
											   CMMsg.MSG_NOISYMOVEMENT,
											   L("<S-NAME> swallow(es) <T-NAMESELF> WHOLE!"));
					if(location().okMessage(tastyMorselM,eatMsg))
					{
						location().send(tastyMorselM,eatMsg);
						if(eatMsg.value()==0)
						{
							curState().setHunger(maxState().maxHunger(baseWeight()));
							myStomachR.bringMobHere(tastyMorselM,false);
							final CMMsg enterMsg=CMClass.getMsg(tastyMorselM,myStomachR,null,CMMsg.MSG_ENTER,myStomachR.description(),CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,L("<S-NAME> slide(s) down the gullet into the stomach!"));
							myStomachR.send(tastyMorselM,enterMsg);
							if(tastyMorselM.isPlayer())
								CMLib.achievements().possiblyBumpAchievement(tastyMorselM, Event.AREAVISIT, 1, new Object[] {myStomachR.getArea(), myStomachR});
						}
					}
				}
			}
		}
		return true;
	}

	@Override
	public void recoverPhyStats()
	{
		super.recoverPhyStats();
		setupDragonIfNecessary();
	}

	@Override
	public void recoverCharStats()
	{
		super.recoverCharStats();
		int dragonAge=DragonAge();
		if((dragonAge<0)||(dragonAge>20))
			dragonAge=20;
		charStats().setStat(CharStats.STAT_SAVE_MAGIC,charStats().getStat(CharStats.STAT_SAVE_MAGIC)+dragonAge*5);
		switch(DragonColor())
		{
		case GOLD:
			charStats().setStat(CharStats.STAT_SAVE_FIRE,charStats().getStat(CharStats.STAT_SAVE_FIRE)+100);
			charStats().setStat(CharStats.STAT_SAVE_GAS,charStats().getStat(CharStats.STAT_SAVE_GAS)+100);
			break;
		case RED:
		case BRASS:
			charStats().setStat(CharStats.STAT_SAVE_FIRE,charStats().getStat(CharStats.STAT_SAVE_FIRE)+100);
			break;
		case GREEN:
			charStats().setStat(CharStats.STAT_SAVE_GAS,charStats().getStat(CharStats.STAT_SAVE_GAS)+100);
			break;
		case BLUE:
		case BRONZE:
			charStats().setStat(CharStats.STAT_SAVE_ELECTRIC,charStats().getStat(CharStats.STAT_SAVE_ELECTRIC)+100);
			break;
		case WHITE:
		case SILVER:
			charStats().setStat(CharStats.STAT_SAVE_COLD,charStats().getStat(CharStats.STAT_SAVE_COLD)+100);
			break;
		case BLACK:
		case COPPER:
			charStats().setStat(CharStats.STAT_SAVE_ACID,charStats().getStat(CharStats.STAT_SAVE_ACID)+100);
			break;
		}
	}

	protected boolean digestTastyMorsels()
	{
		final Room myStomachR = getStomach();
		if(myStomachR==null)
			return true;
		// ===== loop through all inhabitants of the stomach
		final int morselCount = myStomachR.numInhabitants();
		for (int x=0;x<morselCount;x++)
		{
			// ===== get a tasty morsel
			final MOB TastyMorsel = myStomachR.fetchInhabitant(x);
			if (TastyMorsel != null)
			{
				final CMMsg digestMsg=CMClass.getMsg(this,
										   TastyMorsel,
										   null,
										   CMMsg.MSG_OK_ACTION,
										   L("<S-NAME> digest(s) <T-NAMESELF>!!"));
				myStomachR.send(this,digestMsg);
				int damage=((int)Math.round(CMath.div(TastyMorsel.curState().getHitPoints(),5)));
				if(damage<(TastyMorsel.phyStats().level()+6))
					damage=TastyMorsel.curState().getHitPoints()+1;
				if(digestMsg.value()!=0)
					damage=damage/2;
				CMLib.combat().postDamage(this,TastyMorsel,null,damage,CMMsg.MASK_ALWAYS|CMMsg.TYP_ACID,Weapon.TYPE_BURNING,L("The stomach acid <DAMAGE> <T-NAME>!"));
			}
		}
		return true;
	}

	@Override
	public DeadBody killMeDead(final boolean createBody)
	{
		// ===== move all inhabitants to the dragons location
		// ===== loop through all inhabitants of the stomach
		Room room = location();
		if(room == null)
			room = CMLib.map().getRandomRoom();
		final Room myStomachR = getStomach();
		if((myStomachR!=null)&&(room != null))
		{
			final int morselCount = myStomachR.numInhabitants();
			for (int x=morselCount-1;x>=0;x--)
			{
				// ===== get the tasty morsels
				final MOB TastyMorsel = myStomachR.fetchInhabitant(x);
				if(TastyMorsel!=null)
					room.bringMobHere(TastyMorsel,false);
			}

			// =====move the inventory of the stomach to the room
			final int itemCount = myStomachR.numItems();
			for (int y=itemCount-1;y>=0;y--)
			{
				final Item PartiallyDigestedItem = myStomachR.getItem(y);
				if(PartiallyDigestedItem!=null)
				{
					room.addItem(PartiallyDigestedItem,ItemPossessor.Expire.Player_Drop);
					myStomachR.delItem(PartiallyDigestedItem);
				}
			}
			room.recoverRoomStats();
		}
		// ===== Bury Him
		return super.killMeDead(createBody);
	}

	@Override
	public MOB fetchInhabitant(final String inhabitantID)
	{
		if(getStomach() != null)
			return getStomach().fetchInhabitant(inhabitantID);
		return null;
	}

	@Override
	public MOB fetchInhabitantExact(final String inhabitantID)
	{
		if(getStomach() != null)
			return getStomach().fetchInhabitantExact(inhabitantID);
		return null;
	}

	@Override
	public List<MOB> fetchInhabitants(final String inhabitantID)
	{
		if(getStomach() != null)
			return getStomach().fetchInhabitants(inhabitantID);
		return null;
	}

	@Override
	public MOB fetchInhabitant(final int i)
	{
		if(getStomach() != null)
			return getStomach().fetchInhabitant(i);
		return null;
	}

	@Override
	public Enumeration<MOB> inhabitants()
	{
		if(getStomach() != null)
			return getStomach().inhabitants();
		return null;
	}

	@Override
	public void addInhabitant(final MOB mob)
	{
		if(getStomach() != null)
			getStomach().addInhabitant(mob);
	}

	@Override
	public void delInhabitant(final MOB mob)
	{
		if(getStomach() != null)
			getStomach().delInhabitant(mob);
	}

	@Override
	public int numInhabitants()
	{
		if(getStomach() != null)
			return getStomach().numInhabitants();
		return 0;
	}

	@Override
	public boolean isInhabitant(final MOB mob)
	{
		if(getStomach() != null)
			return getStomach().isInhabitant(mob);
		return false;
	}

	@Override
	public void delAllInhabitants(final boolean destroy)
	{
		if(getStomach() != null)
			getStomach().delAllInhabitants(destroy);
	}

	@Override
	public MOB fetchRandomInhabitant()
	{
		if(getStomach() != null)
			return getStomach().fetchRandomInhabitant();
		return null;
	}

	@Override
	public void bringMobHere(final MOB mob, final boolean andFollowers)
	{
		if(getStomach() != null)
			getStomach().bringMobHere(mob, andFollowers);
	}

	@Override
	public void eachInhabitant(final EachApplicable<MOB> applier)
	{
		if(getStomach() != null)
			getStomach().eachInhabitant(applier);
	}
}

