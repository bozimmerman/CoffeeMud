package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_Familiar extends Property
{
	private final static int DOG=0;
	private final static int TURTLE=1;
	private final static int CAT=2;
	private final static int BAT=3;
	private final static int RAT=4;
	private final static int SNAKE=5;
	private final static int OWL=6;
	private final static int RABBIT=7;
	private final static int RAVEN=8;
	private final static String[] names={"dog","turtle","cat","bat","rat","snake",
										 "owl","rabbit","raven"};
	
	private MOB familiarTo=null;
	private MOB familiarWith=null;
	private boolean imthedaddy=false;
	private int familiarType=0;

	public Prop_Familiar()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Find Familiar Property";
		canAffectCode=Ability.CAN_MOBS;
	}

	public Environmental newInstance()
	{
		Prop_Familiar BOB=new Prop_Familiar();
		BOB.setMiscText(text());
		return BOB;
	}

	public String accountForYourself()
	{
		return "is a familiar MOB";
	}
	
	public boolean removeMeFromFamiliarTo()
	{
		if(familiarTo!=null)
		{
			Ability A=familiarTo.fetchAffect(ID());
			if(A!=null) 
			{
				familiarTo.delAffect(A);
				/*if(!familiarTo.amDead())
				{
					familiarTo.charStats().getMyClass().loseExperience(familiarTo,50);
					familiarTo.tell("You`ve just lost 50 experience points for losing your familiar");
				}*/
				familiarTo.recoverCharStats();
				familiarTo.recoverEnvStats();
			}
		}
		if(familiarWith!=null)
		{
			Ability A=familiarWith.fetchAffect(ID());
			if(A!=null) 
			{	
				familiarWith.delAffect(A);
				familiarWith.recoverCharStats();
				familiarWith.recoverEnvStats();
			}
			if(familiarWith.amDead())
				familiarWith.setLocation(null);
			familiarWith.destroy();
			familiarWith.setLocation(null);
		}
		return false;
	}
	
	public boolean tick(int tickID)
	{
		if(tickID==Host.MOB_TICK)
		{
			if((affected==null)||(!(affected instanceof MOB)))
				return removeMeFromFamiliarTo();
			MOB familiar=(MOB)affected;
			if(familiar.amDead()) 
				return removeMeFromFamiliarTo();
			if((!imthedaddy)
			   &&(familiarTo==null)
			   &&(familiarWith==null)
			   &&(((MOB)affected).amFollowing()!=null))
			{
				familiarWith=(MOB)affected;
				familiarTo=familiarWith.amFollowing();
				Prop_Familiar F=(Prop_Familiar)copyOf();
				F.imthedaddy=true;
				F.familiarWith=familiarWith;
				familiarTo.addAffect(F);
				familiarTo.recoverCharStats();
				familiarTo.recoverEnvStats();
			}
			if((familiarWith!=null)&&(familiarTo!=null))
			{
				if((familiarWith.amFollowing()==null)
				||(familiarWith.amDead())
				||(familiarTo.amDead())
				||(familiarWith.amFollowing()!=familiarTo))
					removeMeFromFamiliarTo();
			}
		}
		return super.tick(tickID);
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		switch(familiarType)
		{
		case DOG:
				affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_SEE_HIDDEN);
				break;
		case TURTLE: 
				if(((affectableStats.sensesMask()&EnvStats.CAN_BREATHE)>0)
				&&(affected instanceof MOB)
				&&(((MOB)affected).location()!=null)
				&&((((MOB)affected).location().domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)||(((MOB)affected).location().domainType()==Room.DOMAIN_INDOORS_UNDERWATER)))
					affectableStats.setSensesMask(affectableStats.sensesMask()-EnvStats.CAN_BREATHE);
				break;
		case CAT:
				break;
		case BAT:
				if(((affectableStats.sensesMask()&EnvStats.CAN_SEE)>0)&&(affected instanceof MOB))
					affectableStats.setSensesMask(affectableStats.sensesMask()-EnvStats.CAN_HEAR);
				break;
		case RAT:
				break;
		case SNAKE:
				break;
		case OWL:
				affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_SEE_INFRARED);
				break;
		case RABBIT:
				break;
		case RAVEN:
				break;
		}
	}
	
	public boolean okAffect(Affect affect)
	{
		if(((affect.targetCode()&Affect.MASK_MALICIOUS)>0)
		&&(familiarWith!=null)
		&&(familiarTo!=null)
		&&((affect.amITarget(familiarWith))||(affect.amITarget(familiarTo)))
		&&(familiarType==RABBIT))
		{
			MOB target=(MOB)affect.target();
			if((!target.isInCombat())&&(affect.source().getVictim()!=target))
			{
				affect.source().tell("You are too much in awe of "+target.name());
				if(familiarWith.getVictim()==affect.source())
					familiarWith.makePeace();
				if(familiarTo.getVictim()==affect.source())
					familiarTo.makePeace();
				return false;
			}
		}
		return super.okAffect(affect);
	}
	
	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		familiarType=Util.s_int(newText);
		if(newText.trim().length()>2)
		for(int i=0;i<names.length;i++)
			if(newText.trim().equalsIgnoreCase(names[i]))
			{ familiarType=i; break;}
		displayText="(familiarity with the "+names[familiarType]+")";
	}
	
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		switch(familiarType)
		{
		case DOG:
				affectableStats.setStat(CharStats.STRENGTH,affectableStats.getStat(CharStats.STRENGTH)+1);
				break;
		case TURTLE: 
				affectableStats.setStat(CharStats.STRENGTH,affectableStats.getStat(CharStats.STRENGTH)+1);
				break;
		case CAT:
				affectableStats.setStat(CharStats.SAVE_PARALYSIS,affectableStats.getStat(CharStats.SAVE_PARALYSIS)+1);
				break;
		case BAT:
				affectableStats.setStat(CharStats.DEXTERITY,affectableStats.getStat(CharStats.DEXTERITY)+1);
				break;
		case RAT:
				affectableStats.setStat(CharStats.SAVE_DISEASE,affectableStats.getStat(CharStats.SAVE_DISEASE)+100);
				affectableStats.setStat(CharStats.CONSTITUTION,affectableStats.getStat(CharStats.CONSTITUTION)+1);
				break;
		case SNAKE:
				affectableStats.setStat(CharStats.SAVE_POISON,affectableStats.getStat(CharStats.SAVE_POISON)+100);
				affectableStats.setStat(CharStats.CONSTITUTION,affectableStats.getStat(CharStats.CONSTITUTION)+1);
				break;
		case OWL:
				affectableStats.setStat(CharStats.WISDOM,affectableStats.getStat(CharStats.WISDOM)+1);
				break;
		case RABBIT:
				affectableStats.setStat(CharStats.CHARISMA,affectableStats.getStat(CharStats.CHARISMA)+1);
				break;
		case RAVEN:
				affectableStats.setStat(CharStats.SAVE_UNDEAD,affectableStats.getStat(CharStats.SAVE_UNDEAD)+100);
				affectableStats.setStat(CharStats.INTELLIGENCE,affectableStats.getStat(CharStats.INTELLIGENCE)+1);
				break;
		}
	}
}