package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.Races.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.db.*;
import com.planet_ink.coffee_mud.Items.Weapons.DragonClaw;
import com.planet_ink.coffee_mud.StdAffects.FullMsg;
import com.planet_ink.coffee_mud.commands.TheFight;
import com.planet_ink.coffee_mud.Locales.StdRoom;

public class Dragon extends StdMOB
{

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
	private StdRoom Stomach = null;

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

		// ===== set the parameter stuff
		DragonAge = ageValue;
		DragonColor = colorValue;
		baseEnvStats().setLevel(8*DragonAge);
		baseEnvStats().setAbility(colorValue);
		birthAge=8*DragonAge;
		birthColor=colorValue;

		// ===== is it a male or female
		short gend = (short)Math.round(Math.random());
		if (gend == 0)
		{
			baseCharStats().setGender('F');
		}
		else
		{
			baseCharStats().setGender('M');
		}

		// ===== set the basics
		Username=getAgeDescription(DragonAge).toString() + " " + getColorDescription(DragonColor) + " Dragon";
		setDescription("A majestic " + getColorDescription(DragonColor) + " Dragon, simply being in its presence makes you uneasy.");
		setDisplayText(getAgeDescription(DragonAge).toString() + " " + getColorDescription(DragonColor) + " Dragon watches you intently.");

		// ===== arm him
		DragonClaw ClawOne = new DragonClaw();
		DragonClaw ClawTwo = new DragonClaw();
		ClawOne.wear(Item.WIELD);
		this.addInventory(ClawOne);
		this.addInventory(ClawTwo);

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

		maxState.setHitPoints(((7+PointMod) * 10 * DragonAge));
		setMoney(1000 * DragonAge);
		baseEnvStats().setWeight(1500 * DragonAge);

		// ===== Dragons never flee.
		setWimpHitPoint(0);

		// ===== Dragons Get two attacks per round with their claws
		baseEnvStats().setSpeed(2.0);

		// ===== Dragons get tougher with age
		baseCharStats().setStrength(13 + (DragonAge*2));
		baseCharStats().setIntelligence(13 + (DragonAge*2));
		baseCharStats().setWisdom(13 + (DragonAge*2));
		baseCharStats().setDexterity(13 + (DragonAge*2));
		baseCharStats().setConstitution(13 + (DragonAge*2));
		baseCharStats().setCharisma(13 + (DragonAge*2));

		// ===== if the dragon is an adult or larger add the swallow whole
		Stomach=null;
		if (baseEnvStats().level()>=ADULT)
		{
			//System.out.println("enabling swallow...");
			Stomach = new StdRoom();
			Stomach.setName("Dragon Stomach");
			Stomach.setDescription("You are in the stomach of a dragon.  It is wet with digestive acids, and the walls are grinding you to a pulp.  You have been Swallowed whole and are being digested.");
		}

		// ===== Recover from birth.
		recoverMaxState();
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

	public boolean tick(int tickID)
	{
		if((!amDead())&&(tickID==ServiceEngine.MOB_TICK))
		{
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
		return super.tick(tickID);
	}

	protected boolean useBreathWeapon()
	{
		// ===== the text to post
		MOB target = null;
		int AffectCode = Affect.VISUAL_WNOISE;
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
			AffectCode = Affect.STRIKE_COLD;
			break;
		case BLACK:
			msgText = "The dragon spits acid at <T-NAME>.";
			AffectCode = Affect.STRIKE_ACID;
			break;
		case BLUE:
			msgText = "Lightning shoots forth from the dragons mouth striking <T-NAME>.";
			AffectCode = Affect.STRIKE_ELECTRIC;
			break;
		case GREEN:
			msgText = "The dragon breathes a cloud of noxious vapors choking <T-NAME>.";
			AffectCode = Affect.STRIKE_GAS;
			break;
		case RED:
			msgText = "The dragon torches <T-NAME> with fiery breath!.";
			AffectCode = Affect.STRIKE_FIRE;
			break;
		case BRASS:
			msgText = "The dragon cooks <T-NAME> with a blast of pure heat!.";
			AffectCode = Affect.STRIKE_FIRE;
			break;
		case COPPER:
			msgText = "The dragon spits acid at <T-NAME>.";
			AffectCode = Affect.STRIKE_ACID;
			break;
		case BRONZE:
			msgText = "Lightning shoots forth from the dragons mouth striking <T-NAME>.";
			AffectCode = Affect.STRIKE_ELECTRIC;
			break;
		case SILVER:
			msgText = "The dragon breathes frost at <T-NAME>.";
			AffectCode = Affect.STRIKE_COLD;
			break;
		case GOLD:
			if ((int)Math.round(Math.random())==1)
			{
				msgText = "The dragon torches <T-NAME> with fiery breath!.";
				AffectCode = Affect.STRIKE_FIRE;
			}
			else
			{
				msgText = "The dragon breathes a cloud of noxious vapors choking <T-NAME>.";
				AffectCode = Affect.STRIKE_GAS;
			}
			break;
		default:
			return false;
		}

		for (int x=0;x<location().numInhabitants();x++)
		{
			// ===== get the next target
			target = location().fetchInhabitant(x);
			// ===== do not attack yourself
			if (!target.ID().equals(this.ID()))
			{
				FullMsg Message = new FullMsg(this,
											  target,
											  null,
											  AffectCode,
											  AffectCode,
											  Affect.VISUAL_WNOISE,
											  msgText);
				if (location().okAffect(Message))
				{
					location().send(this,Message);
					if(Message.wasModified())
						TheFight.doDamage(target,((short)Math.round(Util.div(Util.mul(Math.random(),7*DragonAge),2.0))));
					else
						TheFight.doDamage(target,(short)Math.round(Math.random()*7)*DragonAge);
				}
			}
		}
		return true;
	}

	protected boolean trySwallowWhole()
	{
		if(Stomach==null) return true;
		if (Sense.canPerformAction(this)&&
			(Sense.canHear(this)||Sense.canSee(this)||Sense.canSmell(this)))
		{
			MOB TastyMorsel = getVictim();
			if (TastyMorsel.envStats().weight()<1500)
			{
				// ===== if it is less than three so roll for it
				int roll = (int)Math.round(Math.random()*99);

				// ===== check the result
				if (roll<2)
				{
					// ===== The player has been eaten.
					//System.out.println("The Dragon swallowed " + TastyMorsel.name() + " WHOLE!!");

					// ===== move the tasty morsel to the stomach
					FullMsg EatMsg=new FullMsg(this,
											   TastyMorsel,
											   null,
											   Affect.TASTE_FOOD,
											   Affect.STRIKE_JUSTICE,
											   Affect.STRIKE_JUSTICE,
											   "<S-NAME> swallowed <T-NAME> WHOLE!");
					if(this.location().okAffect(EatMsg))
					{
						this.location().send(TastyMorsel,EatMsg);
						Stomach.bringMobHere(TastyMorsel,false);
						FullMsg enterMsg=new FullMsg(TastyMorsel,Stomach,null,Affect.MOVE_ENTER,Stomach.description(),Affect.MOVE_ENTER,null,Affect.MOVE_ENTER,"<S-NAME> Slides down the gullet into the stomach!");
						Stomach.send(TastyMorsel,enterMsg);
					}
				}
			}
		}
		return true;
	}

	public boolean okAffect(Affect affect)
	{
		boolean retval = super.okAffect(affect);
		MOB SourceMOB = affect.source();

		if(affect.amITarget(this))
		{
			if ((this.DragonAge*5)>=(Dice.rollPercentage()+1))
			{
				// ===== check for magic resistance
				if((affect.targetCode()==Affect.STRIKE_MAGIC)||
				   (affect.targetCode()==Affect.SOUND_MAGIC))
				{
					affect.source().tell("The Dragon resisted your spell!");
					return false;
				}
			}

			// ===== check for natural protections
			switch (affect.targetCode())
			{
			case Affect.STRIKE_FIRE:
				if (this.DragonColor==RED)
				{
					affect.source().tell("Red Dragons are immune to Fire!");
					return false;
				}
				if (this.DragonColor==GOLD)
				{
					affect.source().tell("Gold Dragons are immune to Fire!");
					return false;
				}
				if (this.DragonColor==BRASS)
				{
					affect.source().tell("Brass Dragons are immune to Acid!");
					return false;
				}
				break;
			case Affect.STRIKE_GAS:
				if (this.DragonColor==GREEN)
				{
					affect.source().tell("Green Dragons are immune to gas attacks!");
					return false;
				}
				if (this.DragonColor==GOLD)
				{
					affect.source().tell("Gold Dragons are immune to gas attacks!");
					return false;
				}
				break;
			case Affect.STRIKE_ELECTRIC:
				if (this.DragonColor==BLUE)
				{
					affect.source().tell("Blue Dragons are immune to Electrical attacks!");
					return false;
				}
				if (this.DragonColor==BRONZE)
				{
					affect.source().tell("Bronze Dragons are immune to Electrical attacks!");
					return false;
				}
				break;
			case Affect.STRIKE_COLD:
				if (this.DragonColor==WHITE)
				{
					affect.source().tell("White Dragons are immune to cold attacks!");
					return false;
				}
				if (this.DragonColor==SILVER)
				{
					affect.source().tell("Silver Dragons are immune to cold attacks!");
					return false;
				}
				break;
			case Affect.STRIKE_ACID:
				if (this.DragonColor==BLACK)
				{
					affect.source().tell("Black Dragons are immune to Acid!");
					return false;
				}
				if (this.DragonColor==COPPER)
				{
					affect.source().tell("Copper Dragons are immune to Acid!");
					return false;
				}
				break;
			}
		}
		return retval;
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
										   Affect.STRIKE,
										   Affect.STRIKE,
										   Affect.STRIKE,
										   "<S-NAME> Digests <T-NAME>!!");
				Stomach.send(this,DigestMsg);
				TheFight.doDamage(TastyMorsel,(int)Math.round(Util.div(TastyMorsel.curState().getHitPoints(),2)));
			}
		}
		return true;
	}

	public void kill()
	{
		// ===== move all inhabitants to the dragons location
		// ===== loop through all inhabitants of the stomach
		int morselCount = Stomach.numInhabitants();
		for (int x=morselCount-1;x>=0;x--)
		{
			// ===== get the tasty morsels
			MOB TastyMorsel = Stomach.fetchInhabitant(x);
			this.location().bringMobHere(TastyMorsel,false);
		}

		// =====move the inventory of the stomach to the room
		int itemCount = Stomach.numItems();
		for (int y=itemCount-1;y>=0;y--)
		{
			Item PartiallyDigestedItem = Stomach.fetchItem(y);
			if (PartiallyDigestedItem!=null)
			{
				this.location().addItem(PartiallyDigestedItem);
				Stomach.delItem(PartiallyDigestedItem);
			}
		}
		this.location().recoverRoomStats();

		// ===== Bury Him
		super.kill();
	}
}
