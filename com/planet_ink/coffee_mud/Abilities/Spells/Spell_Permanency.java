package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Permanency extends Spell
{
	public StdAbility permanentAbility=null;
	public int oldTicksRemaining=0;
	public Spell_Permanency()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Permanency";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(22);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Permanency();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_ENCHANTMENT;
	}
	
	public void unInvoke()
	{
		if(permanentAbility!=null)
			permanentAbility.setTickDownRemaining(oldTicksRemaining);
		permanentAbility=null;
		super.unInvoke();
		
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Environmental target=getAnyTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(mob.curState().getMana()<mob.maxState().getMana())
		{
			mob.tell("You need to be at full mana to cast this.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		mob.charStats().getMyClass().loseExperience(mob,50);
		mob.curState().setMana(0);

		boolean success=profficiencyCheck(0,auto);
		if(target.fetchAffect(ID())!=null)
		{
			mob.tell("Permanency has already been cast on "+target.name()+".");
			return false;
		}

		
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"":"<S-NAME> encant(s) to <T-NAMESELF>.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				StdAbility theOne=null;
				for(int a=target.numAffects()-1;a>=0;a--)
				{
					Ability A=target.fetchAffect(a);
					if((A.invoker()==mob)
					 &&(!A.isAnAutoEffect())
					 &&(!A.canBeUninvoked())
					 &&(A instanceof StdAbility)
					 &&((A.classificationCode()&Ability.ALL_CODES)!=Ability.PROPERTY)
					 &&(((StdAbility)A).getTickDownRemaining()>0)
					 &&(((StdAbility)A).getTickDownRemaining()<10000))
					{
						theOne=(StdAbility)A;
						break;
					}
				}
				if(theOne==null)
				{
					mob.tell("There does not appear to be any of your spells on "+target.name()+" which can be made permanent.");
					return false;
				}
				else
				{
					oldTicksRemaining=theOne.getTickDownRemaining();
					permanentAbility=theOne;
					beneficialAffect(mob,target,Integer.MAX_VALUE);
					if(target.fetchAffect(ID())!=null)
						permanentAbility.makeLongLasting();
					mob.location().show(mob,target,Affect.MSG_OK_VISUAL,"The quality of "+theOne.name()+" inside <T-NAME> glows!");
				}
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> encant(s) to <T-NAMESELF>, but loses patience.");


		// return whether it worked
		return success;
	}
}