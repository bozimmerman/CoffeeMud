package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Druid_ShapeShift extends StdAbility
{
	public String ID() { return "Druid_ShapeShift"; }
	public String name(){ return "Shape Shift";}
	public int quality(){return Ability.OK_SELF;}
	private static final String[] triggerStrings = {"SHAPESHIFT"};
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}

	public Environmental newInstance(){	return new Druid_ShapeShift();}
	public int classificationCode(){return Ability.SKILL;}

	public int myRaceCode=-1;
	public Race newRace=null;
	public String raceName="";
	
	public String displayText()
	{
		if((myRaceCode<0)||(newRace==null))
			return super.displayText();
		return "(in "+newRace.name().toLowerCase()+" form)";
	}

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
	private static String[] forms={"Rodent form",
								   "Feline form",
								   "K-9 form",
								   "Bird form",
								   "Snake form",
								   "Bear form",
								   "Insect form",
								   "Monkey form",
								   "Bovine form"};

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
			if(("AEIOU").indexOf(Character.toUpperCase(raceName.charAt(0)))>=0)
				affectableStats.setReplacementName("an "+raceName.toLowerCase());
			else
				affectableStats.setReplacementName("a "+raceName.toLowerCase());
			newRace.setHeightWeight(affectableStats,(char)((MOB)affected).charStats().getStat(CharStats.GENDER));
		}
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
		if((canBeUninvoked())&&(mob.location()!=null))
			mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"<S-NAME> revert(s) to "+mob.charStats().getMyRace().name().toLowerCase()+" form.");
	}

	public void setRaceName(MOB mob)
	{
		int raceLevel=4;
		int classLevel=CMAble.qualifyingClassLevel(mob,this)-CMAble.qualifyingLevel(mob,this);
		if(classLevel<6)
			raceLevel=0;
		else
		if(classLevel<12)
			raceLevel=1;
		else
		if(classLevel<18)
			raceLevel=2;
		else
		if(classLevel<24)
			raceLevel=3;
		raceName=shapes[raceLevel][myRaceCode];
		newRace=CMClass.getRace(races[raceLevel][myRaceCode]);
	}
						
	
	public static boolean isShapeShifted(MOB mob)
	{
		if(mob==null) return false;
		for(int a=0;a<mob.numAffects();a++)
		{
			Ability A=mob.fetchAffect(a);
			if((A!=null)&&(A instanceof Druid_ShapeShift))
				return true;
		}
		return false;
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		for(int a=mob.numAffects()-1;a>=0;a--)
		{
			Ability A=mob.fetchAffect(a);
			if((A!=null)&&(A instanceof Druid_ShapeShift))
			{
				A.unInvoke();
				return true;
			}
		}

		int[] racesTaken=new int[forms.length];
		Vector allShapeshifts=new Vector();
		if((myRaceCode>=0)&&(myRaceCode<racesTaken.length))
			racesTaken[myRaceCode]++;
		
		for(int a=0;a<mob.numAbilities();a++)
		{
			Ability A=mob.fetchAbility(a);
			if((A!=null)&&(A instanceof Druid_ShapeShift))
			{
				Druid_ShapeShift D=(Druid_ShapeShift)A;
				allShapeshifts.addElement(D);
				if((D.myRaceCode>=0)&&(D.myRaceCode<racesTaken.length))
					racesTaken[D.myRaceCode]++;
			}
		}
		
		if(myRaceCode<0)
		{
			if(mob.isMonster())
			{
				myRaceCode=Dice.roll(1,racesTaken.length,-1);
				while(racesTaken[myRaceCode]>0)
					myRaceCode=Dice.roll(1,racesTaken.length,-1);
			}
			else
			if(mob.isInCombat()) 
				return false;
			else
			{
				try{
				if(!mob.session().confirm("You have not yet chosen your form, would you like to now (Y/n)?","Y"))
					return false;
				StringBuffer str=new StringBuffer("Choose from the following:\n\r");
				StringBuffer choices=new StringBuffer("");
				for(int i=0;i<forms.length;i++)
				{
					if(racesTaken[i]==0)
					{
						str.append(Util.padLeft(""+(i+1),2)+") "+forms[i]+"\n\r");
						choices.append(""+(i+1));
					}
				}
				str.append("Please select: ");
				String choice=mob.session().choose(str.toString(),choices.toString(),"");
				myRaceCode=Util.s_int(choice)-1;
				}catch(Exception e){};
			}
		}
		
		if(myRaceCode<0) 
			return false;
		else
		{
			setMiscText(""+myRaceCode);
			setRaceName(mob);
		}

		// now check for alternate shapeshifts
		if((triggerStrings().length>0)&&(commands.size()>0)&&(allShapeshifts.size()>1))
		{
			Vector V=allShapeshifts;
			allShapeshifts=new Vector();
			while(V.size()>0)
			{
				Ability choice=null;
				int sortByLevel=Integer.MAX_VALUE;;
				for(int v=0;v<V.size();v++)
				{
					Ability A=(Ability)V.elementAt(v);
					int lvl=CMAble.qualifyingLevel(mob,A);
					if(lvl<=0) lvl=CMAble.lowestQualifyingLevel(A.ID());
					if(lvl<sortByLevel)
					{
						sortByLevel=lvl;
						choice=A;
					}
				}
				if(choice==null) break;
				allShapeshifts.addElement(choice);
				V.removeElement(choice);
			}
			String parm=Util.combine(commands,0);
			StringBuffer list=new StringBuffer("");
			for(int i=0;i<allShapeshifts.size();i++)
			{
				Druid_ShapeShift A=(Druid_ShapeShift)allShapeshifts.elementAt(i);
				if(A.myRaceCode>=0)
				{
					if((A.raceName==null)||(A.raceName.length()==0))
						A.setRaceName(mob);
					if((A.raceName==null)||(A.raceName.length()==0))
						list.append(Util.padLeft(""+(i+1),2)+") Not yet chosen.\n\r");
					else
					{
						list.append(Util.padLeft(""+(i+1),2)+") "+A.raceName+" ("+forms[A.myRaceCode]+")\n\r");
						if(CoffeeUtensils.containsString(A.raceName,parm))
							return A.invoke(mob,new Vector(),givenTarget,auto);
						if(CoffeeUtensils.containsString(forms[A.myRaceCode],parm))
							return A.invoke(mob,new Vector(),givenTarget,auto);
					}
				}
			}
			int iparm=Util.s_int(parm);
			if(iparm>0)
			{
				if(iparm<=allShapeshifts.size())
				{
					Ability A=(Ability)allShapeshifts.elementAt(iparm-1);
					return A.invoke(mob,new Vector(),givenTarget,auto);
				}
			}
			mob.tell("'"+parm+"' is an illegal form!\n\rValid forms include: \n\r"+list.toString());
			return false;
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
			FullMsg msg=new FullMsg(mob,null,this,Affect.MSG_OK_ACTION,null);
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob,Integer.MAX_VALUE);
				if(("AEIOU").indexOf(Character.toUpperCase(raceName.charAt(0)))>=0)
					raceName="An "+raceName;
				else
					raceName="A "+raceName;
				mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"<S-NAME> take(s) on "+raceName.toLowerCase()+" form.");
				mob.confirmWearability();
			}
		}
		else
			beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) to <S-HIM-HERSELF>, but nothing happens.");


		// return whether it worked
		return success;
	}
}