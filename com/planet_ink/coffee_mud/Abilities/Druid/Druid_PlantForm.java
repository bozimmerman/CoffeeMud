package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Druid_PlantForm extends StdAbility
{
	public String ID() { return "Druid_PlantForm"; }
	public String name(){ return "Plant Form";}
	public int quality(){return Ability.OK_SELF;}
	private static final String[] triggerStrings = {"PLANTFORM"};
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}

	public Environmental newInstance(){	return new Druid_PlantForm();}
	public int classificationCode(){return Ability.SKILL;}

	public Race newRace=null;
	public String raceName="";
	
	public String displayText()
	{
		if(newRace==null)
		{
			unInvoke();
			return "";
		}
		return "(in "+newRace.name().toLowerCase()+" form)";
	}

	private static String[] shapes={
	"Flower",
	"Vine",
	"Tumbleweed",
	"Shambler"
	};
	private static String[] races={
	"Flower",
	"Vine",
	"Tumbleweed",
	"Shambler"
	};

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(((affect.targetCode()&Affect.MASK_MALICIOUS)>0)
		&&((affect.amITarget(affected))))
		{
			MOB target=(MOB)affect.target();
			if((!target.isInCombat())
			&&(affect.source().isMonster())
			&&(affect.source().getVictim()!=target))
			{
				affect.source().tell("Attack a plant?!");
				if(target.getVictim()==affect.source())
				{
					target.makePeace();
					target.setVictim(null);
				}
				return false;
			}
		}
		return super.okAffect(myHost,affect);
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if((newRace!=null)&&(affected instanceof MOB))
		{
			if(("AEIOU").indexOf(Character.toUpperCase(raceName.charAt(0)))>=0)
				affectableStats.setName("an "+raceName.toLowerCase());
			else
				affectableStats.setName("a "+raceName.toLowerCase());
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
			mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"<S-NAME> revert(s) to "+mob.charStats().raceName().toLowerCase()+" form.");
	}

	public void setRaceName(MOB mob)
	{
		int classLevel=CMAble.qualifyingClassLevel(mob,this)-CMAble.qualifyingLevel(mob,this);
		raceName=getRaceName(classLevel);
		newRace=getRace(classLevel);
	}
	public int getRaceLevel(int classLevel)
	{
		if(classLevel<5)
			return 0;
		else
		if(classLevel<10)
			return 1;
		else
		if(classLevel<15)
			return 2;
		else
			return 3;
	}
	public Race getRace(int classLevel)
	{
		return CMClass.getRace(races[getRaceLevel(classLevel)]);
	}
	public String getRaceName(int classLevel)
	{
		return shapes[getRaceLevel(classLevel)];
	}
						
	
	public static boolean isShapeShifted(MOB mob)
	{
		if(mob==null) return false;
		for(int a=0;a<mob.numAffects();a++)
		{
			Ability A=mob.fetchAffect(a);
			if((A!=null)&&(A instanceof Druid_PlantForm))
				return true;
		}
		return false;
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		for(int a=mob.numAffects()-1;a>=0;a--)
		{
			Ability A=mob.fetchAffect(a);
			if((A!=null)&&(A instanceof Druid_PlantForm))
			{
				A.unInvoke();
				return true;
			}
		}

		if((mob.location().domainType()&Room.INDOORS)>0)
		{
			mob.tell("You must be outdoors to take on your plant form.");
			return false;
		}
		if(mob.location().domainType()==Room.DOMAIN_OUTDOORS_CITY)
		{
			mob.tell("You must be in the wild to take on your plant form.");
			return false;
		}
		
		int classLevel=CMAble.qualifyingClassLevel(mob,this)-CMAble.qualifyingLevel(mob,this);
		String choice=Util.combine(commands,0);
		if(choice.trim().length()>0)
		{
			StringBuffer buf=new StringBuffer("Plant Forms:\n\r");
			Vector choices=new Vector();
			for(int i=0;i<classLevel;i++)
			{
				String s=getRaceName(i);
				if(!choices.contains(s))
				{
					choices.addElement(s);
					buf.append(s+"\n\r");
				}
				if(CoffeeUtensils.containsString(s,choice))
				{
					classLevel=i;
					break;
				}
			}
			if(choice.equalsIgnoreCase("list"))
			{
				mob.tell(buf.toString());
				return true;
			}
		}
		raceName=getRaceName(classLevel);
		newRace=getRace(classLevel);

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