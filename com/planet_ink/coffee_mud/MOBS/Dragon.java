package com.planet_ink.coffee_mud.MOBS;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;


/* 
   Copyright 2000-2010 Mike Rundell

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
public class Dragon extends StdMOB
{
	public String ID(){return "Dragon";}
	protected int breatheDown=4;
	protected int swallowDown=5;
	protected int digestDown=4;

	protected int birthColor=0;
	protected int birthAge=0;


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
	protected int DragonColor(){ return baseEnvStats().ability();}
	protected int DragonAge(){ return baseEnvStats().level()/10;}
	protected Room Stomach = null;

	// ===== random constructor
	public Dragon()
	{
		// ===== creates a random color and age of dragon
		this((short)Math.round(Math.random()*DRAGONCOLORCOUNT));
	}

	// ===== constructs a dragon of a specified color, but a random age
	public Dragon(int colorValue)
	{
		this(colorValue,determineAge());
	}

	// ===== public constructor
	public Dragon(int colorValue, int ageValue)
	{
		super();
		baseEnvStats().setAbility(colorValue);
		baseEnvStats().setLevel(5+(ageValue*10));
		birthColor=0;
		birthAge=0;
		setupDragonIfNecessary();
	}

	public void setupDragonIfNecessary()
	{
		// ===== set the parameter stuff		DragonAge() = ageValue;

		if(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
			return;
        if((DragonAge()==birthAge)&&(DragonColor()==birthColor))
        	return;
        int colorValue=DragonColor(); 
        int ageValue=DragonAge();

        birthAge=ageValue;
        birthColor=colorValue;
        
		// ===== is it a male or female
		short gend = (short)Math.round(Math.random());
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
		Weapon ClawOne=CMClass.getWeapon("DragonClaw");
		Weapon ClawTwo=CMClass.getWeapon("DragonClaw");
		if(ClawOne!=null)
		{
			ClawOne.wearAt(Wearable.WORN_WIELD);
			ClawTwo.wearAt(Wearable.WORN_WIELD);
			addInventory(ClawOne);
			addInventory(ClawTwo);
		}

		// ===== hitpoints are muxed by 10 To beef them up
		int PointMod = 1;

		// ===== set the mod based on the color
		switch (DragonColor())
		{
			case WHITE:		PointMod = 1;	CMLib.factions().setAlignment(this,Faction.ALIGN_EVIL);	break;
			case BLACK:		PointMod = 2;	CMLib.factions().setAlignment(this,Faction.ALIGN_EVIL);	break;
			case BLUE:		PointMod = 3;	CMLib.factions().setAlignment(this,Faction.ALIGN_EVIL);	break;
			case GREEN:		PointMod = 4;	CMLib.factions().setAlignment(this,Faction.ALIGN_EVIL);	break;
			case RED:		PointMod = 5;	CMLib.factions().setAlignment(this,Faction.ALIGN_EVIL);	break;
			case BRASS:		PointMod = 1;	CMLib.factions().setAlignment(this,Faction.ALIGN_GOOD);	break;
			case COPPER:	PointMod = 2;	CMLib.factions().setAlignment(this,Faction.ALIGN_GOOD);	break;
			case BRONZE:	PointMod = 3;	CMLib.factions().setAlignment(this,Faction.ALIGN_GOOD);	break;
			case SILVER:	PointMod = 4;	CMLib.factions().setAlignment(this,Faction.ALIGN_GOOD);	break;
			case GOLD:		PointMod = 5;	CMLib.factions().setAlignment(this,Faction.ALIGN_GOOD);	break;
			default:		PointMod = 3;	CMLib.factions().setAlignment(this,Faction.ALIGN_NEUTRAL);	break;
		}

        CMLib.leveler().fillOutMOB(this,baseEnvStats().level());
		baseState.setHitPoints(baseState.getHitPoints() * PointMod);
		setMoney(getMoney()*PointMod);
		baseEnvStats().setWeight(1500 * DragonAge());

		// ===== Dragons never flee.
		setWimpHitPoint(0);


		// ===== Dragons get tougher with age
		for(int i : CharStats.CODES.BASE())
			baseCharStats().setStat(i,13 + (DragonAge()*2));
		baseCharStats().setMyRace(CMClass.getRace("Dragon"));
		baseCharStats().getMyRace().startRacing(this,false);

		// ===== if the dragon is an adult or larger add the swallow whole
		Stomach=null;
		// ===== Recover from birth.
		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}

    protected static int determineAge()
	{
		// ===== Get a percent chance
		int iRoll = CMLib.dice().rollPercentage()+1;

		// ===== Determine the age based upon this
		if (iRoll==1) return HATCHLING;
		if (iRoll<=10) return VERYYOUNG;
		if (iRoll<=25) return YOUNG;
		if (iRoll<=45) return SUBADULT;
		if (iRoll<=67) return YOUNGADULT;
		if (iRoll<=82) return ADULT;
		if (iRoll<=92) return OLD;
		if (iRoll<=97) return VERYOLD;
		if (iRoll<=100) return ANCIENT;
		return SUBADULT;
	}

	protected StringBuffer getAgeDescription(int draconianAge)
	{
		StringBuffer returnVal = null;

		// ===== return a string that represents the age of the Dragon
		switch (draconianAge)
		{
			case HATCHLING:		returnVal = new StringBuffer("a hatchling");break;
			case VERYYOUNG:		returnVal = new StringBuffer("a very young");break;
			case YOUNG:			returnVal = new StringBuffer("a young");break;
			case SUBADULT:		returnVal = new StringBuffer("a sub-adult");break;
			case YOUNGADULT:	returnVal = new StringBuffer("a young adult");break;
			case ADULT:			returnVal = new StringBuffer("an adult");break;
			case OLD:			returnVal = new StringBuffer("an old");break;
			case VERYOLD:		returnVal = new StringBuffer("a very old");break;
			case ANCIENT:		returnVal = new StringBuffer("an ancient");break;
			default:			returnVal = new StringBuffer("a");break;
		}

		return returnVal;
	}

	protected StringBuffer getColorDescription(int colorVal)
	{
		StringBuffer returnVal = null;

		// ===== return the color of the dragon
		switch (colorVal)
		{
			case WHITE:	returnVal = new StringBuffer("White");break;
			case BLACK:	returnVal = new StringBuffer("Black");break;
			case BLUE:	returnVal = new StringBuffer("Blue");break;
			case GREEN:	returnVal = new StringBuffer("Green");break;
			case RED:	returnVal = new StringBuffer("Red");break;
			case BRASS:	returnVal = new StringBuffer("Brass");break;
			case COPPER:returnVal = new StringBuffer("Copper");break;
			case BRONZE:returnVal = new StringBuffer("Bronze");break;
			case SILVER:returnVal = new StringBuffer("Silver");break;
			case GOLD:	returnVal = new StringBuffer("Gold");break;
			default:	returnVal = new StringBuffer("Unknown");break;
		}

		return returnVal;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
        if(tickID!=Tickable.TICKID_MOB)
        	return super.tick(ticking, tickID);
        setupDragonIfNecessary();
		if(!amDead())
		{
			if((Stomach==null)
			&&(location()!=null)
			&&(DragonAge()>=ADULT))
			{
				Stomach = CMClass.getLocale("StdRoom");
				if(Stomach!=null)
				{
                    Stomach.setName("Dragon Stomach");
					Stomach.setDisplayText("Dragon Stomach");
					Stomach.setArea(location().getArea());
					Stomach.setDescription("You are in the stomach of a dragon.  It is wet with digestive acids, and the walls are grinding you to a pulp.  You have been Swallowed whole and are being digested.");
				}
			}
            if((--digestDown)<=0)
            {
                digestDown=2;
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
		int AffectCode = CMMsg.TYP_JUSTICE;
		int WeaponType= Weapon.TYPE_BURNING;
		String msgText = "";

		// ===== if we are following don't Breath, we might
		//       hurt the one we follow...
		if (amFollowing()!=null)
		{
			// ===== if we breath we might hurt him
			return true;
		}

		if(!CMLib.flags().canBreathe(this))
		{
			// ===== if you can't breathe, you can't breathe fire
			return false;
		}
		
		// ===== Tell What the Beast is doing
		switch (DragonColor())
		{
		case WHITE:
			msgText = "The dragon breathes frost at <T-NAME>.";
			AffectCode = CMMsg.TYP_COLD;
			WeaponType= Weapon.TYPE_FROSTING;
			break;
		case BLACK:
			msgText = "The dragon spits acid at <T-NAME>.";
			AffectCode = CMMsg.TYP_ACID;
			WeaponType= Weapon.TYPE_MELTING;
			break;
		case BLUE:
			msgText = "Lightning shoots forth from the dragons mouth striking <T-NAME>.";
			AffectCode = CMMsg.TYP_ELECTRIC;
			WeaponType= Weapon.TYPE_STRIKING;
			break;
		case GREEN:
			msgText = "The dragon breathes a cloud of noxious vapors choking <T-NAME>.";
			AffectCode = CMMsg.TYP_GAS;
			WeaponType= Weapon.TYPE_GASSING;
			break;
		case RED:
			msgText = "The dragon torches <T-NAME> with fiery breath!.";
			AffectCode = CMMsg.TYP_FIRE;
			WeaponType= Weapon.TYPE_BURNING;
			break;
		case BRASS:
			msgText = "The dragon cooks <T-NAME> with a blast of pure heat!.";
			AffectCode = CMMsg.TYP_FIRE;
			WeaponType= Weapon.TYPE_BURNING;
			break;
		case COPPER:
			msgText = "The dragon spits acid at <T-NAME>.";
			AffectCode = CMMsg.TYP_ACID;
			WeaponType= Weapon.TYPE_MELTING;
			break;
		case BRONZE:
			msgText = "Lightning shoots forth from the dragons mouth striking <T-NAME>.";
			AffectCode = CMMsg.TYP_ELECTRIC;
			WeaponType= Weapon.TYPE_STRIKING;
			break;
		case SILVER:
			msgText = "The dragon breathes frost at <T-NAME>.";
			AffectCode = CMMsg.TYP_COLD;
			WeaponType= Weapon.TYPE_FROSTING;
			break;
		case GOLD:
			if ((int)Math.round(Math.random())==1)
			{
				msgText = "The dragon torches <T-NAME> with fiery breath!.";
				AffectCode = CMMsg.TYP_FIRE;
				WeaponType= Weapon.TYPE_BURNING;
			}
			else
			{
				msgText = "The dragon breathes a cloud of noxious vapors choking <T-NAME>.";
				AffectCode = CMMsg.TYP_GAS;
				WeaponType= Weapon.TYPE_GASSING;
			}
			break;
		default:
			return false;
		}

		Room room=location();
		if(room!=null)
		for (int x=0;x<room.numInhabitants();x++)
		{
			// ===== get the next target
			target = room.fetchInhabitant(x);
			// ===== do not attack yourself
			if ((target!=null)&&(!target.ID().equals(ID())))
			{
				CMMsg Message = CMClass.getMsg(this,
											  target,
											  null,
											  CMMsg.MSK_MALICIOUS_MOVE|AffectCode,
											  CMMsg.MSK_MALICIOUS_MOVE|AffectCode,
											  CMMsg.MSG_NOISYMOVEMENT,
											  msgText);
				if (room.okMessage(this,Message))
				{
					room.send(this,Message);
					int damage=((short)Math.round(CMath.div(CMath.mul(Math.random(),7*DragonAge()),2.0)));
					if(Message.value()<=0)
						damage=((short)Math.round(Math.random()*7)*DragonAge());
					CMLib.combat().postDamage(this,target,null,damage,CMMsg.MASK_ALWAYS|AffectCode,WeaponType,"The blast <DAMAGE> <T-NAME>.");
				}
			}
		}
		return true;
	}

	protected boolean trySwallowWhole()
	{
		if(Stomach==null) return true;
		if (CMLib.flags().aliveAwakeMobileUnbound(this,true)
			&&(rangeToTarget()==0)
			&&(CMLib.flags().canHear(this)||CMLib.flags().canSee(this)||CMLib.flags().canSmell(this)))
		{
			MOB TastyMorsel = getVictim();
			if(TastyMorsel==null) return true;
			if (TastyMorsel.envStats().weight()<1500)
			{
				// ===== if it is less than three so roll for it
				int roll = (int)Math.round(Math.random()*99);

				// ===== check the result
				if (roll<2)
				{
					// ===== The player has been eaten.
					// ===== move the tasty morsel to the stomach
					CMMsg EatMsg=CMClass.getMsg(this,
											   TastyMorsel,
											   null,
											   CMMsg.MSG_EAT,
											   CMMsg.MASK_ALWAYS|CMMsg.TYP_JUSTICE,
											   CMMsg.MSG_NOISYMOVEMENT,
											   "<S-NAME> swallow(es) <T-NAMESELF> WHOLE!");
					if(location().okMessage(TastyMorsel,EatMsg))
					{
						location().send(TastyMorsel,EatMsg);
						Stomach.bringMobHere(TastyMorsel,false);
						CMMsg enterMsg=CMClass.getMsg(TastyMorsel,Stomach,null,CMMsg.MSG_ENTER,Stomach.description(),CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,"<S-NAME> slide(s) down the gullet into the stomach!");
						Stomach.send(TastyMorsel,enterMsg);
					}
				}
			}
		}
		return true;
	}

	public void recoverEnvStats()
	{
		super.recoverEnvStats();
		setupDragonIfNecessary();
	}
	public void recoverCharStats()
	{
		super.recoverCharStats();
		charStats().setStat(CharStats.STAT_SAVE_MAGIC,charStats().getStat(CharStats.STAT_SAVE_MAGIC)+DragonAge()*5);
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
		if(Stomach==null) return true;
		// ===== loop through all inhabitants of the stomach
		int morselCount = Stomach.numInhabitants();
		for (int x=0;x<morselCount;x++)
		{
			// ===== get a tasty morsel
			MOB TastyMorsel = Stomach.fetchInhabitant(x);
			if (TastyMorsel != null)
			{
				CMMsg DigestMsg=CMClass.getMsg(this,
										   TastyMorsel,
										   null,
										   CMMsg.MSG_OK_ACTION,
										   "<S-NAME> digest(s) <T-NAMESELF>!!");
				Stomach.send(this,DigestMsg);
				int damage=((int)Math.round(CMath.div(TastyMorsel.curState().getHitPoints(),2)));
				if(damage<(TastyMorsel.envStats().level()+6)) damage=TastyMorsel.curState().getHitPoints()+1;
				CMLib.combat().postDamage(this,TastyMorsel,null,damage,CMMsg.MASK_ALWAYS|CMMsg.TYP_ACID,Weapon.TYPE_BURNING,"The stomach acid <DAMAGE> <T-NAME>!");
			}
		}
		return true;
	}

	public DeadBody killMeDead(boolean createBody)
	{
		// ===== move all inhabitants to the dragons location
		// ===== loop through all inhabitants of the stomach
    	Room room = location();
    	if(room == null) room = CMLib.map().getRandomRoom();
	    if((Stomach!=null)&&(room != null))
	    {
			int morselCount = Stomach.numInhabitants();
			for (int x=morselCount-1;x>=0;x--)
			{
				// ===== get the tasty morsels
				MOB TastyMorsel = Stomach.fetchInhabitant(x);
				if(TastyMorsel!=null)
					room.bringMobHere(TastyMorsel,false);
			}
	
			// =====move the inventory of the stomach to the room
			int itemCount = Stomach.numItems();
			for (int y=itemCount-1;y>=0;y--)
			{
				Item PartiallyDigestedItem = Stomach.fetchItem(y);
				if(PartiallyDigestedItem!=null)
				{
					room.addItemRefuse(PartiallyDigestedItem,CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_PLAYER_DROP));
					Stomach.delItem(PartiallyDigestedItem);
				}
			}
			room.recoverRoomStats();
	    }
		// ===== Bury Him
		return super.killMeDead(createBody);
	}
}
