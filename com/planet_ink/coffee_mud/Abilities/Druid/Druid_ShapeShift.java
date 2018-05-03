package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
   Copyright 2002-2018 Bo Zimmerman

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

public class Druid_ShapeShift extends StdAbility
{
	@Override
	public String ID()
	{
		return "Druid_ShapeShift";
	}

	private final static String	localizedName	= CMLib.lang().L("Shape Shift");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_SHAPE_SHIFTING;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	private static final String[]	triggerStrings	= I(new String[] { "SHAPESHIFT" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	public int		myRaceCode	= -1;
	public int		myRaceLevel	= -1;
	public Race		newRace		= null;
	public String	raceName	= "";

	@Override
	public String displayText()
	{
		if((myRaceCode<0)||(newRace==null))
			return super.displayText();
		return "(in "+newRace.name().toLowerCase()+" form)";
	}

	private static String[][] shapes={
	{"Mouse",   "Kitten",   "Puppy",	"Robin",  "Garden Snake", "Cub",	"Grasshopper","Spider Monkey","Calf",	 "Clown Fish",	"Seal"},
	{"Rat", 	"Cat",  	"Dog",  	"Owl",    "Snake",     "Young Bear","Centipede",  "Chimp",  	  "Cow",	 "Angelfish",	"Walrus"},
	{"Dire Rat","Puma", 	"Wolf", 	"Hawk",   "Python",    "Brown Bear","Tarantula",  "Ape",		  "Buffalo", "Swordfish",	"Dolphin"},
	{"WereRat", "Lion", 	"Dire Wolf","Eagle",  "Cobra",     "Black Bear","Scarab",     "Gorilla",	  "Bull",	 "Shark",		"Whale"},
	{"WereBat", "Manticore","WereWolf", "Harpy",  "Naga",      "WereBear",  "ManScorpion","Sasquatch",    "Minotaur","Merfolk",		"Selkie"}
	};
	
	private static String[][] races={
	{"Mouse",  "Kitten",   "Puppy",   "Robin",  "GardenSnake","Cub",	 "Grasshopper","Monkey",   "Calf",	  "ClownFish",	"Seal"},
	{"Rat",    "Cat",      "Dog",     "Owl",	"Snake",	  "Cub",	 "Centipede",  "Chimp",    "Cow",	  "AngelFish",	"Walrus"},
	{"DireRat","Puma",     "Wolf",    "Hawk",   "Python",     "Bear",    "Tarantula",  "Ape",      "Buffalo", "Swordfish",	"Dolphin"},
	{"WereRat","Lion",     "DireWolf","Eagle",  "Cobra",	  "Bear",    "Scarab",     "Gorilla",  "Bull",	  "Shark",		"Whale"},
	{"WereBat","Manticore","WereWolf","Harpy",  "Naga", 	  "WereBear","ManScorpion","Sasquatch","Minotaur","Merfolk",	"Selkie"}
	};
	private static double[]   attadj=
	{.7		  ,1.0		  ,1.0		 ,.2	   ,.3			  ,1.2  	,.7 		 ,1.0   	   ,.7 		   ,0.3			,1.0};
	private static double[]   dmgadj=
	{.4		  ,.6   	  ,.8   	 ,1.0	   ,.4			  ,.6   	,.6 		  ,.8   	    ,1.0   	   ,0.4			,.5};
	private static double[]   armadj=
	{1.0	  ,.5   	  ,.4   	 ,.5	   ,1.0			  ,.3   	,1.0		  ,.2   	    ,.2   	   ,0.5			,.3};
	private static double[]   spdadj=
	{0.2	  ,.0   	  ,.0   	 ,.0	   ,0.0			  ,.0   	,0.3		  ,.0   	    ,.0   	   ,1.0   		,0.2};

	private static String[] forms={"Rodent form",
								   "Feline form",
								   "Canine form",
								   "Bird form",
								   "Reptile form",
								   "Ursine form",
								   "Insect form",
								   "Primate form",
								   "Bovine form",
								   "Fish form",
								   "Sea Mammal form"};

	@Override
	public void setMiscText(String newText)
	{
		if(newText.length()>0)
			myRaceCode=CMath.s_int(newText);
		super.setMiscText(newText);
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if((newRace!=null)&&(affected instanceof MOB)&&(myRaceCode>=0))
		{
			final PhyStats stats = affectableStats;
			final int xlvl=getXLEVELLevel(invoker());
			affectableStats.setName(CMLib.english().startWithAorAn(raceName.toLowerCase()));
			final int oldAdd=affectableStats.weight()-affected.basePhyStats().weight();
			final int raceCode = getRaceCode();
			final int maxRaceLevel = getMaxCharLevel(myRaceLevel);
			final int adjustedLevel = ((maxRaceLevel<affectableStats.level()) ? maxRaceLevel : affectableStats.level()) + xlvl; 
			newRace.setHeightWeight(stats,(char)((MOB)affected).charStats().getStat(CharStats.STAT_GENDER));
			if(oldAdd>0)
				stats.setWeight(stats.weight()+oldAdd);
			stats.setAttackAdjustment(stats.attackAdjustment()+(int)Math.round(CMath.mul(adjustedLevel,attadj[raceCode])/2.0));
			stats.setArmor(stats.armor()-(int)Math.round(CMath.mul(adjustedLevel,armadj[raceCode])/2.0));
			stats.setDamage(stats.damage()+(int)Math.round(CMath.mul(adjustedLevel,dmgadj[raceCode])/2.0));
			stats.setSpeed(stats.speed()+(spdadj[raceCode] * (1.0+(xlvl/3.0))));
			//stats.setSensesMask(stats.sensesMask()|PhyStats.CAN_GRUNT_WHEN_STUPID);
		}
	}

	@Override
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(newRace!=null)
		{
			final int oldCat=affected.baseCharStats().ageCategory();
			affectableStats.setMyRace(newRace);
			affectableStats.setWearableRestrictionsBitmap(affectableStats.getWearableRestrictionsBitmap()|affectableStats.getMyRace().forbiddenWornBits());
			if(affected.baseCharStats().getStat(CharStats.STAT_AGE)>0)
				affectableStats.setStat(CharStats.STAT_AGE,newRace.getAgingChart()[oldCat]);
		}
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		super.unInvoke();
		if((canBeUninvoked())&&(mob.location()!=null))
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> revert(s) to @x1 form.",mob.charStats().raceName().toLowerCase()));
	}

	public int getClassLevel(MOB mob)
	{
		final int qualClassLevel=CMLib.ableMapper().qualifyingClassLevel(mob,this)+(2*getXLEVELLevel(mob));
		int classLevel=qualClassLevel-CMLib.ableMapper().qualifyingLevel(mob,this);
		if(qualClassLevel<0)
			classLevel=30;
		return classLevel;
	}

	public void setRaceName(MOB mob)
	{
		final int classLevel=getClassLevel(mob);
		raceName=getRaceName(classLevel,myRaceCode);
		newRace=getRace(classLevel,myRaceCode);
	}

	public int getMaxRaceLevel(int classLevel)
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

	public int getMaxCharLevel(int raceLevel)
	{
		switch(raceLevel)
		{
		case 0:
			return 5;
		case 1:
			return 11;
		case 2:
			return 17;
		case 3:
			return 24;
		default:
			return 31;
		}
	}

	public int getRaceLevel(MOB mob)
	{
		return getRaceLevel(getClassLevel(mob));
	}

	public int getRaceLevel(int classLevel)
	{
		final int maxLevel=getMaxRaceLevel(classLevel);
		if((myRaceLevel<0)||(myRaceLevel>maxLevel))
			return maxLevel;
		return myRaceLevel;
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
		if(mob==null)
			return false;
		for(final Enumeration<Ability> a=mob.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)&&(A instanceof Druid_ShapeShift))
				return true;
		}
		return false;
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(target instanceof MOB)
			{
				if((((MOB)target).isInCombat())
				&&(!Druid_ShapeShift.isShapeShifted((MOB)target)))
				{
					final int qualClassLevel=CMLib.ableMapper().qualifyingClassLevel(mob,this)+(2*getXLEVELLevel(mob));
					int classLevel=qualClassLevel-CMLib.ableMapper().qualifyingLevel(mob,this);
					if(qualClassLevel<0)
						classLevel=30;
					if(getRaceLevel(classLevel)>=3)
						return super.castingQuality(mob, target,Ability.QUALITY_BENEFICIAL_SELF);
				}
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		for(final Enumeration<Ability> a=mob.personalEffects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)&&(A instanceof Druid_ShapeShift))
			{
				A.unInvoke();
				return true;
			}
		}

		this.myRaceLevel=-1;
		final int[] racesTaken=new int[forms.length];
		Vector<Ability> allShapeshifts=new Vector<Ability>();
		if((myRaceCode>=0)&&(myRaceCode<racesTaken.length))
			racesTaken[myRaceCode]++;

		for(int a=0;a<mob.numAbilities();a++)
		{
			final Ability A=mob.fetchAbility(a);
			if((A!=null)&&(A instanceof Druid_ShapeShift))
			{
				final Druid_ShapeShift D=(Druid_ShapeShift)A;
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
				final long t=System.currentTimeMillis();
				while((racesTaken[myRaceCode]>0)&&((System.currentTimeMillis()-t)<10000))
					myRaceCode=CMLib.dice().roll(1,racesTaken.length,-1);
			}
			else
			if(mob.isInCombat())
				return false;
			else
			{
				try
				{
					if(!mob.session().confirm(L("You have not yet chosen your form, would you like to now (Y/n)?"),"Y"))
						return false;
					while(!mob.session().isStopped())
					{
						final StringBuffer str=new StringBuffer(L("Choose from the following:\n\r"));
						final List<String> formNames=new ArrayList<String>();
						final Map<String,Integer> formMap=new Hashtable<String,Integer>();
						for(int i=0;i<forms.length;i++)
						{
							if(racesTaken[i]==0)
							{
								str.append(CMStrings.padLeft(""+(i+1),2)+") "+forms[i]+"\n\r");
								formNames.add(forms[i].toLowerCase());
								formMap.put(forms[i].toLowerCase(), Integer.valueOf(i));
							}
						}
						str.append(L("Please select: "));
						final String choice=mob.session().prompt(str.toString(),"");
						if(choice.trim().length()==0)
						{
							mob.tell(L("Aborted."));
							return false;
						}
						if(CMath.isInteger(choice))
						{
							final int x=CMath.s_int(choice)-1;
							boolean found=false;
							for(final String key : formMap.keySet())
							{
								final Integer num=formMap.get(key);
								if(x == num.intValue())
								{
									myRaceCode = num.intValue();
									found=true;
									break;
								}
							}
							if(found)
								break;
						}
						else
						{
							int x=CMParms.indexOf(formNames.toArray(new String[0]), choice.toLowerCase().trim());
							if(x<0)
								x=CMParms.indexOfStartsWith(formNames.toArray(new String[0]), choice.toLowerCase().trim());
							if(x>=0)
							{
								myRaceCode = formMap.get(formNames.get(x)).intValue();
								break;
							}
						}
					}
				}
				catch (final Exception e)
				{
				}
			}
		}

		if(myRaceCode<0)
			return false;

		String parm=CMParms.combine(commands,0);
		if(parm.length()>0)
		{
			final int raceLevel=getRaceLevel(mob);
			for(int i1=raceLevel;i1>=0;i1--)
			{
				if(shapes[i1][myRaceCode].equalsIgnoreCase(parm))
				{
					parm="";
					this.myRaceLevel=i1;
				}
			}
			if(parm.length()>0)
			{
				for(int i1=raceLevel;i1>=0;i1--)
				{
					if(CMLib.english().containsString(shapes[i1][myRaceCode],parm))
					{
						parm="";
						this.myRaceLevel=i1;
					}
				}
			}
		}
		setMiscText(""+myRaceCode);
		setRaceName(mob);

		// now check for alternate shapeshifts
		if((triggerStrings().length>0)
		&&(parm.length()>0))
		{
			final Vector<Ability> V=allShapeshifts;
			allShapeshifts=new Vector<Ability>();
			while(V.size()>0)
			{
				Ability choice=null;
				int sortByLevel=Integer.MAX_VALUE;
				for(int v=0;v<V.size();v++)
				{
					final Ability A=V.elementAt(v);
					int lvl=CMLib.ableMapper().qualifyingLevel(mob,A);
					if(lvl<=0)
						lvl=CMLib.ableMapper().lowestQualifyingLevel(A.ID());
					lvl+=getXLEVELLevel(mob);
					if(lvl<sortByLevel)
					{
						sortByLevel=lvl;
						choice=A;
					}
				}
				if(choice==null)
					break;
				allShapeshifts.addElement(choice);
				V.removeElement(choice);
			}
			final StringBuffer list=new StringBuffer("");
			for(int i=0;i<allShapeshifts.size();i++)
			{
				final Druid_ShapeShift A=(Druid_ShapeShift)allShapeshifts.elementAt(i);
				if(A.myRaceCode>=0)
				{
					if((A.raceName==null)||(A.raceName.length()==0))
						A.setRaceName(mob);
					if((A.raceName==null)||(A.raceName.length()==0))
						list.append(CMStrings.padLeft(""+(i+1),2)+") Not yet chosen.\n\r");
					else
					{
						list.append(CMStrings.padLeft(""+(i+1),2)+") "+forms[A.myRaceCode]+": ");
						final int raceLevel=A.getRaceLevel(mob);
						for(int i1=raceLevel;i1>=0;i1--)
						{
							list.append(shapes[i1][A.myRaceCode]);
							if(i1!=0)
								list.append(", ");
						}
						list.append("\n\r");
						if(CMLib.english().containsString(A.raceName,parm))
							return A.invoke(mob,new Vector<String>(),givenTarget,auto,asLevel);
						if(CMLib.english().containsString(forms[A.myRaceCode],parm))
							return A.invoke(mob,new Vector<String>(),givenTarget,auto,asLevel);
						for(int i1=raceLevel;i1>=0;i1--)
						{
							if(CMLib.english().containsString(shapes[i1][A.myRaceCode],parm))
								return A.invoke(mob,new XVector<String>(parm),givenTarget,auto,asLevel);
						}
					}
				}
			}
			final int iparm=CMath.s_int(parm);
			if(iparm>0)
			{
				if(iparm<=allShapeshifts.size())
				{
					final Ability A=allShapeshifts.elementAt(iparm-1);
					return A.invoke(mob,new Vector<String>(),givenTarget,auto,asLevel);
				}
			}
			if(parm.equalsIgnoreCase("LIST"))
				mob.tell(L("Valid forms include: \n\r@x1",list.toString()));
			else
				mob.tell(L("'@x1' is an illegal form!\n\rValid forms include: \n\r@x2",parm,list.toString()));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if((!appropriateToMyFactions(mob))&&(!auto))
		{
			if((CMLib.dice().rollPercentage()<50))
			{
				mob.tell(L("Extreme emotions disrupt your change."));
				return false;
			}
		}

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_OK_ACTION,null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> take(s) on @x1 form.",raceName.toLowerCase()));
				beneficialAffect(mob,mob,asLevel,Ability.TICKS_FOREVER);
				raceName=CMStrings.capitalizeAndLower(CMLib.english().startWithAorAn(raceName.toLowerCase()));
				CMLib.utensils().confirmWearability(mob);
			}
		}
		else
			beneficialWordsFizzle(mob,null,L("<S-NAME> chant(s) to <S-HIM-HERSELF>, but nothing happens."));

		// return whether it worked
		return success;
	}
}
