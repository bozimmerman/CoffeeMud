package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Druid_ShapeShift extends StdAbility
{
	private int myRaceCode=-1;
	private Race newRace=null;
	private String raceName="";
	
	private static String[][] shapes={
	{"Mouse",   "Kitten",   "Puppy",    "Robin",  "Garden Snake", "Cub",    "Grasshopper","Spider Monkey","Calf"},
	{"Rat",     "Cat",      "Dog",      "Owl",    "Snake",        "Cub",    "Centipede",  "Chimp",        "Cow"},
	{"Dire Rat","Puma",     "Wolf",     "Hawk",   "Python",    "Brown Bear","Tarantula",  "Ape",          "Buffalo"},
	{"WereRat", "Lion",     "Dire Wolf","Eagle",  "Cobra",     "Black Bear","Scarab",     "Gorilla",      "Bull"},
	{"WereBat", "Manticore","WereWolf", "Griffon","Naga",      "WereBear",  "ManScorpion","Sasquatch",    "Minotaur"}
	};
	private static String[][] races={
	{"Mouse",  "Kitten",   "Puppy",   "Robin",  "GardenSnake","Cub",     "Grasshopper","Monkey",   "Calf"},
	{"Rat",    "Cat",      "Dog",     "Owl",    "Snake",      "Cub",     "Centipede",  "Chimp",    "Cow"},
	{"DireRat","Puma",     "Wolf",    "Hawk",   "Python",     "Bear",    "Tarantula",  "Ape",      "Buffalo"},
	{"WereRat","Lion",     "DireWolf","Eagle",  "Cobra",      "Bear",    "Scarab",     "Gorilla",  "Bull"},
	{"WereBat","Manticore","WereWolf","Griffon","Naga",       "WereBear","ManScorpion","Sasquatch","Minotaur"}
	};
	
	public Druid_ShapeShift()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Shape Shift";

		displayText="(in your animal form)";
		miscText="";
		triggerStrings.addElement("SHAPESHIFT");
		quality=Ability.OK_SELF;
		
		canAffectCode=Ability.CAN_MOBS;
		canTargetCode=0;

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Druid_ShapeShift();
	}

	public int classificationCode()
	{
		return Ability.SKILL;
	}
	public void setMiscText(String newText)
	{
		if(newText.length()>0)
			myRaceCode=Util.s_int(newText);
		super.setMiscText(newText);
	}
	
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if((newRace!=null)&&(affected instanceof MOB))
		{
			affectableStats.setReplacementName(raceName);
			newRace.setHeightWeight(affectableStats,(char)((MOB)affected).charStats().getStat(CharStats.GENDER));
		}
	}
	
	public String displayText()
	{
		if((myRaceCode<0)||(newRace==null))
			return super.displayText();
		return "(in "+newRace.name()+" form)";
	}
	
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(newRace!=null) affectableStats.setMyRace(newRace);
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();
		mob.tell("You have reverted to your normal form.");
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Ability A=mob.fetchAffect(ID());
		if(A!=null)
		{
			A.unInvoke();
			return true;
		}
		
		if(mob.isMonster()) return false;
		
		if(myRaceCode<0)
		{
			if(mob.isInCombat())
				return false;
			try{
			if(!mob.session().confirm("You have not yet chosen your form, would you like to now (Y/n)?","Y"))
				return false;
			StringBuffer str=new StringBuffer("Choose from the following:\n\r");
			str.append("1) Rodent form\n\r");
			str.append("2) Feline form\n\r");
			str.append("3) K-9 form\n\r");
			str.append("4) Bird form\n\r");
			str.append("5) Snake form\n\r");
			str.append("6) Bear form\n\r");
			str.append("7) Insect form\n\r");
			str.append("8) Monkey form\n\r");
			str.append("9) Bovine form\n\r");
			str.append("Please select: ");
			String choice=mob.session().choose(str.toString(),"123456789","");
			myRaceCode=Util.s_int(choice)-1;
			}catch(Exception e){};
		}
		if(myRaceCode<0) return false;
		else
		{
			setMiscText(""+myRaceCode);
			int raceLevel=4;
			if(mob.envStats().level()<6)
				raceLevel=0;
			else
			if(mob.envStats().level()<12)
				raceLevel=1;
			else
			if(mob.envStats().level()<18)
				raceLevel=2;
			else
			if(mob.envStats().level()<24)
				raceLevel=3;
			raceName=shapes[raceLevel][myRaceCode];
			newRace=CMClass.getRace(races[raceLevel][myRaceCode]);
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if((!appropriateToMyAlignment(mob.getAlignment()))&&(!auto))
		{
			if((Dice.rollPercentage()<50))
			{
				mob.tell("Extreme emotions disrupt your change.");
				return false;
			}
		}
		
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,null,this,Affect.MSG_OK_ACTION,"<S-NAME> change(s) shape...");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob,Integer.MAX_VALUE);
				if(("AEIOU").indexOf(Character.toUpperCase(raceName.charAt(0)))>=0)
					raceName="An "+raceName;
				else
					raceName="A "+raceName;
				mob.tell("You have become "+raceName.toLowerCase()+".");
				newRace.confirmGear(mob);
				mob.makePeace();
			}
		}
		else
			beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) to <S-HIM-HER>self, but nothing happens.");


		// return whether it worked
		return success;
	}
}