package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class SpecialistMage extends Mage
{
	public String ID(){return "SpecialistMage";}
	public String name(){return "Specialist Mage";}
	public String baseClass(){return "Mage";}
	public int domain(){return Ability.DOMAIN_ABJURATION;}
	public int opposed(){return Ability.DOMAIN_ENCHANTMENT;}
	
	public void cloneFix(CharClass C)
	{
		super.cloneFix(C);
		for(int a=0;a<CMClass.abilities.size();a++)
		{
			Ability A=(Ability)CMClass.abilities.elementAt(a);
			if(A!=null)
			{
				int level=CMAble.getQualifyingLevel(ID(),A.ID());
				if((!CMAble.getDefaultGain(baseClass(),A.ID()))
				&&(level>0)
				&&((A.classificationCode()&Ability.ALL_CODES)==Ability.SPELL))
				{
					if((A.classificationCode()&Ability.ALL_DOMAINS)==opposed())
					{
						if(CMAble.getDefaultGain(baseClass(),A.ID()))
							CMAble.addCharAbilityMapping(ID(),level,A.ID(),0,false);
						else
							CMAble.delCharAbilityMapping(ID(),A.ID());
					}
					else
					if((A.classificationCode()&Ability.ALL_DOMAINS)==domain())
						CMAble.addCharAbilityMapping(ID(),level,A.ID(),25,true);
					else
						CMAble.addCharAbilityMapping(ID(),level,A.ID(),0,false);
				}
			}
		}
	}
	
	public boolean playerSelectable(){	return false;}
	public String otherBonuses()
	{
		String chosen=Util.capitalize(Ability.DOMAIN_DESCS[domain()>>5]);
		String opposed=Util.capitalize(Ability.DOMAIN_DESCS[opposed()>>5]);
		return "At 5th level, receives 2%/lvl bonus damage from "+chosen+".  At 10th level, receives double duration on your "+chosen+" magic, and half duration from malicious "+chosen+" magic.";
	}
	public String otherLimitations()
	{
		String chosen=Util.capitalize(Ability.DOMAIN_DESCS[domain()>>5]);
		String opposed=Util.capitalize(Ability.DOMAIN_DESCS[opposed()>>5]);
		return "Unable to cast "+opposed+" spells.  Receives 2%/lvl penalty damage from "+opposed+".  Receives double duration from malicious "+opposed+" magic, half duration on other "+opposed+" effects.";
	}

	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		return super.qualifiesForThisClass(mob,quiet);
	}

	public boolean okAffect(MOB myChar, Affect affect)
	{
		if((affect.tool()==null)||(!(affect.tool() instanceof Ability)))
		   return super.okAffect(myChar,affect);
		int domain=((Ability)affect.tool()).classificationCode()&Ability.ALL_DOMAINS;
		if(affect.amISource(myChar)
		&&(myChar.isMine(affect.tool())))
		{
			if((affect.sourceMinor()==Affect.TYP_CAST_SPELL)
			   &&(domain==opposed())
			   &&(!CMAble.getDefaultGain(ID(),affect.tool().ID())))
			{
				if(Dice.rollPercentage()>(myChar.charStats().getStat(CharStats.INTELLIGENCE)/2))
				{
					myChar.location().show(myChar,null,Affect.MSG_OK_VISUAL,"<S-NAME> fizzle(s) a spell.");
					return false;
				}
			}
			if(((affect.targetCode()&Affect.MASK_HURT)>0)
			&&(myChar.charStats().getClassLevel(this)>=5)
			&&((((Ability)affect.tool()).classificationCode()&Ability.ALL_DOMAINS)==domain()))
			{
				int recovery=(int)Math.round(Util.mul((affect.targetCode()-Affect.MASK_HURT),Util.mul(0.02,myChar.charStats().getClassLevel(this))));
				affect.modify(affect.source(),affect.target(),affect.tool(),affect.sourceCode(),affect.sourceMessage(),affect.targetCode()-recovery,affect.targetMessage(),affect.othersCode(),affect.othersMessage());
			}
		}
		else
		if((affect.amITarget(myChar))
		&&(Util.bset(affect.targetCode(),Affect.MASK_HURT))
		&&(affect.tool()!=null)
		&&(affect.tool() instanceof Ability))
		{
			if((domain==domain())
			&&(myChar.charStats().getClassLevel(this)>=5))
			{
				int recovery=(int)Math.round(Util.div((affect.targetCode()-Affect.MASK_HURT),1.0+Util.mul(0.02,myChar.charStats().getClassLevel(this))));
				affect.modify(affect.source(),affect.target(),affect.tool(),affect.sourceCode(),affect.sourceMessage(),affect.targetCode()-recovery,affect.targetMessage(),affect.othersCode(),affect.othersMessage());
			}
			else
			if(domain==opposed())
			{
				int recovery=(int)Math.round(Util.mul((affect.targetCode()-Affect.MASK_HURT),1.0+Util.mul(0.02,30-myChar.charStats().getClassLevel(this))));
				affect.modify(affect.source(),affect.target(),affect.tool(),affect.sourceCode(),affect.sourceMessage(),affect.targetCode()-recovery,affect.targetMessage(),affect.othersCode(),affect.othersMessage());
			}
		}
		
		return super.okAffect(myChar,affect);
	}
	
	public int classDurationModifier(MOB myChar, 
									 Ability skill, 
									 int duration)
	{
		if(myChar==null) return duration;
		boolean lessTen=myChar.charStats().getClassLevel(this)<10;
		   
		int domain=skill.classificationCode()&Ability.ALL_DOMAINS;
		if((skill.invoker()==myChar)
		||(skill.quality()!=Ability.MALICIOUS))
		{
			if(domain==opposed())
				return duration/2;
			else
			if(domain==domain())
			{
				if(!lessTen)
				{
					if(duration>=(Integer.MAX_VALUE/2)) 
						return duration;
					else
						return duration*2;
				}
			}
		}
		else
		{
			if(domain==opposed())
			{
				if(duration>=(Integer.MAX_VALUE/2)) 
					return duration;
				else
					return duration*2;
			}
			else
			if(domain==domain())
			{
				if(!lessTen)
					return duration/2;
			}
		}
		return duration;
	}
}