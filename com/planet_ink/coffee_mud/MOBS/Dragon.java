package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Dragon extends StdMOB
{
	public String ID(){return "Dragon";}
	private boolean started=false;
	private int breatheDown=4;
	private int swallowDown=5;
	private int digestDown=4;

	private int birthColor=0;
	private int birthAge=0;


	// ===== Defined Values for Dragon Ages
	public final static int VERYYOUNG			= 1;	// 10
	public final static int YOUNG				= 2;	// 15
	public final static int SUBADULT			= 3;	// 20
	public final static int YOUNGADULT			= 4;	// 22
	public final static int ADULT				= 5;	// 15
	public final static int OLD					= 6;	// 10
	public final static int VERYOLD				= 7;	// 5
	public final static int ANCIENT				= 8;	// 3

	public final static int DRAGONCOLORCOUNT	= 5;

	// ===== Defined Values for Dragon Colors
	public final static int WHITE				= 1;
	public final static int BLACK				= 2;
	public final static int BLUE				= 3;
	public final static int GREEN				= 4;
	public final static int RED					= 5;
	public final static int BRASS				= 6;
	public final static int COPPER				= 7;
	public final static int BRONZE				= 8;
	public final static int SILVER				= 9;
	public final static int GOLD				= 10;


	// ===== Defined Value for holding the Dragon Type
	private int DragonColor = WHITE;
	private int DragonAge = VERYYOUNG;
	private Room Stomach = null;

	// ===== random constructor
	public Dragon()
	{
		// ===== creates a random color and age of dragon
		this((short)Math.round(Math.random()*DRAGONCOLORCOUNT-1)+1);
	}

	// ===== constructs a dragon of a specified color, but a random age
	public Dragon(int colorValue)
	{
		// ===== doit
		this(colorValue,determineAge());
	}

	public void setupDragon(int colorValue, int ageValue)
	{
		// ===== set the parameter stuff		DragonAge = ageValue;
		DragonColor = colorValue;
		baseEnvStats().setLevel(8*DragonAge);
		baseEnvStats().setAbility(colorValue);
		birthAge=8*DragonAge;
		birthColor=colorValue;

		if(!ExternalPlay.getSystemStarted()) return;
		// ===== is it a male or female
		short gend = (short)Math.round(Math.random());
		if (gend == 0)
		{
			baseCharStats().setStat(CharStats.GENDER,(int)'F');
		}
		else
		{
			baseCharStats().setStat(CharStats.GENDER,(int)'M');
		}

		// ===== set the basics
		Username=getAgeDescription(DragonAge).toString() + " " + getColorDescription(DragonColor) + " Dragon";
		setDescription("A majestic " + getColorDescription(DragonColor) + " Dragon, simply being in its presence makes you uneasy.");
		setDisplayText(getAgeDescription(DragonAge).toString() + " " + getColorDescription(DragonColor) + " Dragon watches you intently.");

		// ===== arm him
		Weapon ClawOne=(Weapon)CMClass.getWeapon("DragonClaw");
		Weapon ClawTwo=(Weapon)CMClass.getWeapon("DragonClaw");
		if(ClawOne!=null)
		{
			ClawOne.wearAt(Item.WIELD);
			ClawTwo.wearAt(Item.WIELD);
			addInventory(ClawOne);
			addInventory(ClawTwo);
		}

		// ===== Set his defenses based upon his age as well
		baseEnvStats().setArmor(20 - (DragonAge*15));

		// ===== hitpoints are muxed by 10 To beef them up
		int PointMod = 1;

		// ===== set the mod based on the color
		switch (DragonColor)
		{
			case WHITE:		PointMod = 1;	setAlignment(0);	break;
			case BLACK:		PointMod = 2;	setAlignment(0);	break;
			case BLUE:		PointMod = 3;	setAlignment(0);	break;
			case GREEN:		PointMod = 4;	setAlignment(0);	break;
			case RED:		PointMod = 5;	setAlignment(0);	break;
			case BRASS:		PointMod = 1;	setAlignment(1000);	break;
			case COPPER:	PointMod = 2;	setAlignment(1000);	break;
			case BRONZE:	PointMod = 3;	setAlignment(1000);	break;
			case SILVER:	PointMod = 4;	setAlignment(1000);	break;
			case GOLD:		PointMod = 5;	setAlignment(1000);	break;
			default:		PointMod = 3;	setAlignment(500);	break;
		}

		baseState.setHitPoints(((7+PointMod) * 10 * DragonAge));
		setMoney(1000 * DragonAge);
		baseEnvStats().setWeight(1500 * DragonAge);

		// ===== Dragons never flee.
		setWimpHitPoint(0);

		// ===== Dragons Get two attacks per round with their claws
		baseEnvStats().setSpeed(2.0);

		// ===== Dragons get tougher with age
		baseCharStats().setStat(CharStats.STRENGTH,13 + (DragonAge*2));
		baseCharStats().setStat(CharStats.INTELLIGENCE,13 + (DragonAge*2));
		baseCharStats().setStat(CharStats.WISDOM,13 + (DragonAge*2));
		baseCharStats().setStat(CharStats.DEXTERITY,13 + (DragonAge*2));
		baseCharStats().setStat(CharStats.CONSTITUTION,13 + (DragonAge*2));
		baseCharStats().setStat(CharStats.CHARISMA,13 + (DragonAge*2));
		baseCharStats().setMyRace(CMClass.getRace("Dragon"));
		baseCharStats().getMyRace().startRacing(this,false);

		// ===== if the dragon is an adult or larger add the swallow whole
		Stomach=null;
		started=true;
		// ===== Recover from birth.
		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}

	// ===== public constructor
	public Dragon(int colorValue, int ageValue)
	{
		super();
		setupDragon(colorValue,ageValue);
	}

	private static int determineAge()
	{
		// ===== Get a percent chance
		int iRoll = Dice.rollPercentage()+1;

		// ===== Determine the age based upon this
		if (iRoll<=10) return VERYYOUNG;
		if (iRoll<=25) return YOUNG;
		if (iRoll<=45) return SUBADULT;
		if (iRoll<=67) return YOUNGADULT;
		if (iRoll<=82) return ADULT;
		if (iRoll<=92) return OLD;
		if (iRoll<=97) return VERYOLD;
		if (iRoll<=100) return ANCIENT;
		else return SUBADULT;
	}

	public Environmental newInstance()
	{
		// ===== Hatch one!
		return new Dragon();
	}

	protected StringBuffer getAgeDescription(int draconianAge)
	{
		StringBuffer returnVal = null;

		// ===== return a string that represents the age of the Dragon
		switch (draconianAge)
		{
			case VERYYOUNG:		returnVal = new StringBuffer("a very young");break;
			case YOUNG:			returnVal = new StringBuffer("a young");break;
			case SUBADULT:		returnVal = new StringBuffer("a sub-adult");break;
			case YOUNGADULT:	returnVal = new StringBuffer("a young adult");break;
			case ADULT:			returnVal = new StringBuffer("an adult");break;
			case OLD:			returnVal = new StringBuffer("an old");break;
			case VERYOLD:		returnVal = new StringBuffer("a very old");break;
			case ANCIENT:		returnVal = new StringBuffer("an ancient");break;
			default:			returnVal = new StringBuffer("");break;
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
			default:	returnVal = new StringBuffer("");break;
		}

		return returnVal;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((!amDead())&&(tickID==Host.TICK_MOB))
		{
			if(!started)setupDragon(birthColor,DragonAge/8);

			if((Stomach==null)
			&&(location()!=null)
			&&(baseEnvStats().level()>=ADULT))
			{
				Stomach = CMClass.getLocale("StdRoom");
				if(Stomach!=null)
				{
					Stomach.setName("Dragon Stomach");
					Stomach.setArea(location().getArea());
					Stomach.setDescription("You are in the stomach of a dragon.  It is wet with digestive acids, and the walls are grinding you to a pulp.  You have been Swallowed whole and are being digested.");
				}
			}
			if((baseEnvStats().level()!=this.birthAge)
			||(baseEnvStats().ability()!=this.birthColor))
				setupDragon((int)Math.floor(Util.div(baseEnvStats().level(),8))+1,baseEnvStats().ability());
			if (isInCombat())
			{
				if((--swallowDown)<=0)
				{
					swallowDown=2;
					digestTastyMorsels();
				}
				if((--breatheDown)<=0)
				{
					breatheDown=4;
					useBreathWeapon();
				}
				if((--digestDown)<=0)
				{
					digestDown=4;
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

		// ===== Tell What the Beast is doing
		switch (DragonColor)
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
			if ((target!=null)&&(!target.ID().equals(this.ID())))
			{
				FullMsg Message = new FullMsg(this,
											  target,
											  null,
											  CMMsg.MSK_MALICIOUS_MOVE|AffectCode,
											  CMMsg.MSK_MALICIOUS_MOVE|AffectCode,
											  CMMsg.MSG_NOISYMOVEMENT,
											  msgText);
				if (room.okMessage(this,Message))
				{
					room.send(this,Message);
					int damage=((short)Math.round(Util.div(Util.mul(Math.random(),7*DragonAge),2.0)));
					if(Message.value()<=0)
						damage=((short)Math.round(Math.random()*7)*DragonAge);
					ExternalPlay.postDamage(this,target,null,damage,CMMsg.MASK_GENERAL|AffectCode,WeaponType,"The blast <DAMAGE> <T-NAME>");
				}
			}
		}
		return true;
	}

	protected boolean trySwallowWhole()
	{
		if(Stomach==null) return true;
		if (Sense.aliveAwakeMobile(this,true)
			&&(rangeToTarget()==0)
			&&(Sense.canHear(this)||Sense.canSee(this)||Sense.canSmell(this)))
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
					FullMsg EatMsg=new FullMsg(this,
											   TastyMorsel,
											   null,
											   CMMsg.MSG_EAT,
											   CMMsg.MASK_GENERAL|CMMsg.TYP_JUSTICE,
											   CMMsg.MSG_NOISYMOVEMENT,
											   "<S-NAME> swallow(es) <T-NAMESELF> WHOLE!");
					if(location().okMessage(TastyMorsel,EatMsg))
					{
						location().send(TastyMorsel,EatMsg);
						Stomach.bringMobHere(TastyMorsel,false);
						FullMsg enterMsg=new FullMsg(TastyMorsel,Stomach,null,CMMsg.MSG_ENTER,Stomach.description(),CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,"<S-NAME> slide(s) down the gullet into the stomach!");
						Stomach.send(TastyMorsel,enterMsg);
					}
				}
			}
		}
		return true;
	}

	public void recoverCharStats()
	{
		super.recoverCharStats();
		if(!started)setupDragon(birthColor,DragonAge/8);
		charStats().setStat(CharStats.SAVE_MAGIC,charStats().getStat(CharStats.SAVE_MAGIC)+DragonAge*5);
		switch(DragonColor)
		{
		case GOLD:
			charStats().setStat(CharStats.SAVE_FIRE,charStats().getStat(CharStats.SAVE_FIRE)+100);
			charStats().setStat(CharStats.SAVE_GAS,charStats().getStat(CharStats.SAVE_GAS)+100);
			break;
		case RED:
		case BRASS:
			charStats().setStat(CharStats.SAVE_FIRE,charStats().getStat(CharStats.SAVE_FIRE)+100);
			break;
		case GREEN:
			charStats().setStat(CharStats.SAVE_GAS,charStats().getStat(CharStats.SAVE_GAS)+100);
			break;
		case BLUE:
		case BRONZE:
			charStats().setStat(CharStats.SAVE_ELECTRIC,charStats().getStat(CharStats.SAVE_ELECTRIC)+100);
			break;
		case WHITE:
		case SILVER:
			charStats().setStat(CharStats.SAVE_COLD,charStats().getStat(CharStats.SAVE_COLD)+100);
			break;
		case BLACK:
		case COPPER:
			charStats().setStat(CharStats.SAVE_ACID,charStats().getStat(CharStats.SAVE_ACID)+100);
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
				FullMsg DigestMsg=new FullMsg(this,
										   TastyMorsel,
										   null,
										   CMMsg.MSG_OK_ACTION,
										   "<S-NAME> digest(s) <T-NAMESELF>!!");
				Stomach.send(this,DigestMsg);
				int damage=((int)Math.round(Util.div(TastyMorsel.curState().getHitPoints(),2)));
				if(damage<(TastyMorsel.envStats().level()+6)) damage=TastyMorsel.curState().getHitPoints()+1;
				ExternalPlay.postDamage(this,TastyMorsel,null,damage,CMMsg.MASK_GENERAL|CMMsg.TYP_ACID,Weapon.TYPE_BURNING,"The stomach acid <DAMAGE> <T-NAME>!");
			}
		}
		return true;
	}

	public DeadBody killMeDead(boolean createBody)
	{
		// ===== move all inhabitants to the dragons location
		// ===== loop through all inhabitants of the stomach
		int morselCount = Stomach.numInhabitants();
		for (int x=morselCount-1;x>=0;x--)
		{
			// ===== get the tasty morsels
			MOB TastyMorsel = Stomach.fetchInhabitant(x);
			if((TastyMorsel!=null)&&(location()!=null))
				location().bringMobHere(TastyMorsel,false);
		}

		// =====move the inventory of the stomach to the room
		int itemCount = Stomach.numItems();
		for (int y=itemCount-1;y>=0;y--)
		{
			Item PartiallyDigestedItem = Stomach.fetchItem(y);
			if((PartiallyDigestedItem!=null)&&(location()!=null))
			{
				location().addItemRefuse(PartiallyDigestedItem,Item.REFUSE_PLAYER_DROP);
				Stomach.delItem(PartiallyDigestedItem);
			}
		}
		this.location().recoverRoomStats();

		// ===== Bury Him
		return super.killMeDead(createBody);
	}
}
