package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
   Copyright 2000-2010 Bo Zimmerman

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

@SuppressWarnings("unchecked")
public class Druid_ShapeShift extends StdAbility
{
	public String ID() { return "Druid_ShapeShift"; }
	public String name(){ return "Shape Shift";}
	public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_SHAPE_SHIFTING;}
	public int abstractQuality(){return Ability.QUALITY_OK_SELF;}
	private static final String[] triggerStrings = {"SHAPESHIFT"};
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}

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
	{"WereBat", "Manticore","WereWolf", "Harpy","Naga",      "WereBear",  "ManScorpion","Sasquatch",    "Minotaur"}
	};
	private static String[][] races={
	{"Mouse",  "Kitten",   "Puppy",   "Robin",  "GardenSnake","Cub",     "Grasshopper","Monkey",   "Calf"},
	{"Rat",    "Cat",      "Dog",     "Owl",    "Snake",      "Cub",     "Centipede",  "Chimp",    "Cow"},
	{"DireRat","Puma",     "Wolf",    "Hawk",   "Python",     "Bear",    "Tarantula",  "Ape",      "Buffalo"},
	{"WereRat","Lion",     "DireWolf","Eagle",  "Cobra",      "Bear",    "Scarab",     "Gorilla",  "Bull"},
	{"WereBat","Manticore","WereWolf","Harpy","Naga",       "WereBear","ManScorpion","Sasquatch","Minotaur"}
	};
	private static double[]   attadj=
	{.7	      ,1.0		  ,1.0		 ,.2	   ,.3			  ,1.2      ,.7          ,1.0        ,.7};
	private static double[]   dmgadj=
	{.4		  ,.6         ,.8        ,1.0	   ,.4			  ,.6       ,.6           ,.8         ,1.0};
	private static double[]   armadj=
	{1.0	  ,.5         ,.4        ,.5	   ,1.0			  ,.3       ,1.0          ,.2         ,.2};

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
			myRaceCode=CMath.s_int(newText);
		super.setMiscText(newText);
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if((newRace!=null)&&(affected instanceof MOB))
		{
            int xlvl=getXLEVELLevel(invoker());
			affectableStats.setName(CMLib.english().startWithAorAn(raceName.toLowerCase()));
			int oldAdd=affectableStats.weight()-affected.baseEnvStats().weight();
			newRace.setHeightWeight(affectableStats,(char)((MOB)affected).charStats().getStat(CharStats.STAT_GENDER));
			if(oldAdd>0) affectableStats.setWeight(affectableStats.weight()+oldAdd);
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+
												(int)Math.round(CMath.mul(affectableStats.level()+xlvl,attadj[getRaceCode()])/2.0));
			affectableStats.setArmor(affectableStats.armor()+
									(int)Math.round(CMath.mul(affectableStats.level()+xlvl,armadj[getRaceCode()])/2.0));
			affectableStats.setDamage(affectableStats.damage()+
									(int)Math.round(CMath.mul(affectableStats.level()+xlvl,dmgadj[getRaceCode()])/2.0));
		}
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(newRace!=null)
	    {
		    int oldCat=affected.baseCharStats().ageCategory();
			affectableStats.setMyRace(newRace);
			if(affected.baseCharStats().getStat(CharStats.STAT_AGE)>0)
				affectableStats.setStat(CharStats.STAT_AGE,newRace.getAgingChart()[oldCat]);
	    }
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();
		if((canBeUninvoked())&&(mob.location()!=null))
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> revert(s) to "+mob.charStats().raceName().toLowerCase()+" form.");
	}

	public void setRaceName(MOB mob)
	{
        int qualClassLevel=CMLib.ableMapper().qualifyingClassLevel(mob,this)+(2*getXLEVELLevel(mob));
		int classLevel=qualClassLevel-CMLib.ableMapper().qualifyingLevel(mob,this);
        if(qualClassLevel<0) classLevel=30;
		raceName=getRaceName(classLevel,myRaceCode);
		newRace=getRace(classLevel,myRaceCode);
	}
	public int getRaceLevel(int classLevel)
	{
		if(classLevel<6)
			return 0;
		else
		if(classLevel<12)
			return 1;
		else
		if(classLevel<18)
			return 2;
		else
		if(classLevel<24)
			return 3;
		else
			return 4;
	}
	public int getRaceCode()
	{
		if((myRaceCode<0)||
		(myRaceCode>attadj.length)) return 0;
		return myRaceCode;
	}
	public Race getRace(int classLevel, int raceCode)
	{
		return CMClass.getRace(races[getRaceLevel(classLevel)][myRaceCode]);
	}
	public String getRaceName(int classLevel, int raceCode)
	{
		return shapes[getRaceLevel(classLevel)][raceCode];
	}


	public static boolean isShapeShifted(MOB mob)
	{
		if(mob==null) return false;
		for(int a=0;a<mob.numAllEffects();a++)
		{
			Ability A=mob.fetchEffect(a);
			if((A!=null)&&(A instanceof Druid_ShapeShift))
				return true;
		}
		return false;
	}

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if(target instanceof MOB)
            {
                if((((MOB)target).isInCombat())
                &&(!Druid_ShapeShift.isShapeShifted((MOB)target)))
                {
                    int qualClassLevel=CMLib.ableMapper().qualifyingClassLevel(mob,this)+(2*getXLEVELLevel(mob));
                    int classLevel=qualClassLevel-CMLib.ableMapper().qualifyingLevel(mob,this);
                    if(qualClassLevel<0) classLevel=30;
                    if(getRaceLevel(classLevel)>=3)
                        return super.castingQuality(mob, target,Ability.QUALITY_BENEFICIAL_SELF);
                }
            }
        }
        return super.castingQuality(mob,target);
    }

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		for(int a=mob.numEffects()-1;a>=0;a--)
		{
			Ability A=mob.fetchEffect(a);
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

		for(int a=0;a<mob.numLearnedAbilities();a++)
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
				myRaceCode=CMLib.dice().roll(1,racesTaken.length,-1);
                long t=System.currentTimeMillis();
				while((racesTaken[myRaceCode]>0)&&((System.currentTimeMillis()-t)<10000))
					myRaceCode=CMLib.dice().roll(1,racesTaken.length,-1);
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
						str.append(CMStrings.padLeft(""+(i+1),2)+") "+forms[i]+"\n\r");
						choices.append(""+(i+1));
					}
				}
				str.append("Please select: ");
				String choice=mob.session().choose(str.toString(),choices.toString(),"");
				myRaceCode=CMath.s_int(choice)-1;
				}catch(Exception e){}
			}
		}

		if(myRaceCode<0)
			return false;
		setMiscText(""+myRaceCode);
		setRaceName(mob);

		// now check for alternate shapeshifts
		if((triggerStrings().length>0)&&(commands.size()>0)&&(allShapeshifts.size()>1))
		{
			Vector V=allShapeshifts;
			allShapeshifts=new Vector();
			while(V.size()>0)
			{
				Ability choice=null;
				int sortByLevel=Integer.MAX_VALUE;
				for(int v=0;v<V.size();v++)
				{
					Ability A=(Ability)V.elementAt(v);
					int lvl=CMLib.ableMapper().qualifyingLevel(mob,A);
					if(lvl<=0) lvl=CMLib.ableMapper().lowestQualifyingLevel(A.ID());
                    lvl+=getXLEVELLevel(mob);
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
			String parm=CMParms.combine(commands,0);
			StringBuffer list=new StringBuffer("");
			for(int i=0;i<allShapeshifts.size();i++)
			{
				Druid_ShapeShift A=(Druid_ShapeShift)allShapeshifts.elementAt(i);
				if(A.myRaceCode>=0)
				{
					if((A.raceName==null)||(A.raceName.length()==0))
						A.setRaceName(mob);
					if((A.raceName==null)||(A.raceName.length()==0))
						list.append(CMStrings.padLeft(""+(i+1),2)+") Not yet chosen.\n\r");
					else
					{
						list.append(CMStrings.padLeft(""+(i+1),2)+") "+A.raceName+" ("+forms[A.myRaceCode]+")\n\r");
						if(CMLib.english().containsString(A.raceName,parm))
							return A.invoke(mob,new Vector(),givenTarget,auto,asLevel);
						if(CMLib.english().containsString(forms[A.myRaceCode],parm))
							return A.invoke(mob,new Vector(),givenTarget,auto,asLevel);
					}
				}
			}
			int iparm=CMath.s_int(parm);
			if(iparm>0)
			{
				if(iparm<=allShapeshifts.size())
				{
					Ability A=(Ability)allShapeshifts.elementAt(iparm-1);
					return A.invoke(mob,new Vector(),givenTarget,auto,asLevel);
				}
			}
			mob.tell("'"+parm+"' is an illegal form!\n\rValid forms include: \n\r"+list.toString());
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if((!appropriateToMyFactions(mob))&&(!auto))
		{
			if((CMLib.dice().rollPercentage()<50))
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
			CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_OK_ACTION,null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> take(s) on "+raceName.toLowerCase()+" form.");
				beneficialAffect(mob,mob,asLevel,Integer.MAX_VALUE);
				raceName=CMStrings.capitalizeAndLower(CMLib.english().startWithAorAn(raceName.toLowerCase()));
				CMLib.utensils().confirmWearability(mob);
			}
		}
		else
			beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) to <S-HIM-HERSELF>, but nothing happens.");


		// return whether it worked
		return success;
	}
}
