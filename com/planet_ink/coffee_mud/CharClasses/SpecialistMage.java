package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class SpecialistMage extends Mage
{
	public String ID(){return "SpecialistMage";}
	public String name(){return "Specialist Mage";}
	public static int domain(){return Ability.DOMAIN_ABJURATION;}
	public static int opposed(){return Ability.DOMAIN_ENCHANTMENT;}
	public static boolean myAbilitiesLoaded=false;
	public SpecialistMage()
	{
		super();
		if((!myAbilitiesLoaded)&&(CMClass.abilities.size()>0))
		{
			boolean doneOne=false;
			myAbilitiesLoaded=true;
			for(int a=0;a<CMClass.abilities.size();a++)
			{
				Ability A=(Ability)CMClass.abilities.elementAt(a);
				int level=CMAble.getQualifyingLevel(baseClass(),A.ID());
				if((A!=null)
				&&(level>=0)   
				&&((A.classificationCode()&Ability.ALL_CODES)==Ability.SPELL)
				&&((A.classificationCode()&Ability.ALL_DOMAINS)!=opposed()))
				{
					if((A.classificationCode()&Ability.ALL_DOMAINS)==domain())
					{
						doneOne=true;
						CMAble.addCharAbilityMapping(ID(),level,A.ID(),25,false);
					}
					else
						CMAble.addCharAbilityMapping(ID(),level,A.ID(),0,false);
				}
			}
			if(!doneOne)
				myAbilitiesLoaded=false;
		}
	}


	public boolean playerSelectable(){	return false;}
	public String otherBonuses()
	{
		String chosen="chosen school";
		String opposed="opposed school";
		return "Unable to cast "+opposed+" spells.  Receives 2%/lvl bonus damage from "+chosen+", 2%/lvl penalty damage from "+opposed+".  Receives double duration on your "+chosen+" magic and from malicious "+opposed+" magic, half duration on other "+opposed+" effects.";}

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
			if((affect.sourceMinor()==Affect.TYP_CAST_SPELL)&&(domain==opposed()))
			{
				if(Dice.rollPercentage()>(myChar.charStats().getStat(CharStats.INTELLIGENCE)/2))
				{
					myChar.location().show(myChar,null,Affect.MSG_OK_VISUAL,"<S-NAME> fizzle(s) a spell.");
					return false;
				}
			}
			if(((affect.targetCode()&Affect.MASK_HURT)>0)
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
			if((((Ability)affect.tool()).classificationCode()&Ability.ALL_DOMAINS)==domain())
			{
				int recovery=(int)Math.round(Util.div((affect.targetCode()-Affect.MASK_HURT),1.0+Util.mul(0.02,myChar.charStats().getClassLevel(this))));
				affect.modify(affect.source(),affect.target(),affect.tool(),affect.sourceCode(),affect.sourceMessage(),affect.targetCode()-recovery,affect.targetMessage(),affect.othersCode(),affect.othersMessage());
			}
			else
			if((((Ability)affect.tool()).classificationCode()&Ability.ALL_DOMAINS)==opposed())
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
		int domain=skill.classificationCode()&Ability.ALL_DOMAINS;
		if((skill.invoker()==myChar)
		||(skill.quality()!=Ability.MALICIOUS))
		{
			if(domain==opposed())
				return duration/2;
			else
			if(domain==domain())
				return duration*2;
		}
		else
		{
			if(domain==opposed())
				return duration*2;
			else
			if(domain==domain())
				return duration/2;
		}
		return duration;
	}
	
	public void grantAbilities(MOB mob, boolean isBorrowedClass)
	{
		super.grantAbilities(mob,isBorrowedClass);
		Vector grantable=new Vector();
		
		int level=mob.charStats().getClassLevel(this);
		for(int a=0;a<CMClass.abilities.size();a++)
		{
			Ability A=(Ability)CMClass.abilities.elementAt(a);
			if((CMAble.getQualifyingLevel(ID(),A.ID())==level)
			&&((CMAble.getQualifyingLevel(ID(),A.ID())<=25)
			&&(!CMAble.getDefaultGain(ID(),A.ID()))
			&&((A.classificationCode()&Ability.ALL_CODES)==Ability.SPELL)
			&&((A.classificationCode()&Ability.ALL_DOMAINS)==domain())))
			{if (!grantable.contains(A.ID())) grantable.addElement(A.ID());}
		}
		int numSpells=1;
		for(int a=0;a<mob.numAbilities();a++)
		{
			Ability A=mob.fetchAbility(a);
			if(grantable.contains(A.ID()))
			{
				grantable.remove(A.ID());
				numSpells--;
			}
		}
		if(grantable.size()>0)
		for(int i=0;i<numSpells;i++)
		{
			String AID=(String)grantable.elementAt(Dice.roll(1,grantable.size(),-1));
			if(AID!=null)
			{
				grantable.removeElement(AID);
				giveMobAbility(mob,
							   CMClass.getAbility(AID),
							   CMAble.getDefaultProfficiency(ID(),AID),
							   CMAble.getDefaultParm(ID(),AID),
							   isBorrowedClass);
			}
		}
	}
}