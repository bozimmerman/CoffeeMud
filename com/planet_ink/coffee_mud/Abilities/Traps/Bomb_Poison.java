package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Bomb_Poison extends StdBomb
{
	public String ID() { return "Bomb_Poison"; }
	public String name(){ return "poison gas bomb";}
	protected int trapLevel(){return 5;}
	public String requiresToSet(){return "some poison";}
	public Environmental newInstance(){	return new Bomb_Poison();}
	
	public Vector returnOffensiveAffects(Environmental fromMe)
	{
		Vector offenders=new Vector();

		for(int a=0;a<fromMe.numAffects();a++)
		{
			Ability A=fromMe.fetchAffect(a);
			if((A!=null)&&(A.classificationCode()==Ability.POISON))
				offenders.addElement(A);
		}
		return offenders;
	}
	
	public boolean canSetTrapOn(MOB mob, Environmental E)
	{
		if(!super.canSetTrapOn(mob,E)) return false;
		Vector V=returnOffensiveAffects(E);
		if((!(E instanceof Drink))||(V.size()==0))
		{
			mob.tell("You need some poison to make this out of.");
			return false;
		}
		return true;
	}
	public Trap setTrap(MOB mob, Environmental E, int classLevel, int qualifyingClassLevel)
	{
		if(E==null) return null;
		Vector V=returnOffensiveAffects(E);
		if(V.size()>0)
			setMiscText(((Ability)V.firstElement()).ID());
		return super.setTrap(mob,E,classLevel,qualifyingClassLevel);
	}
	
	public void spring(MOB target)
	{
		if(target.location()!=null)
		{
			if((target==invoker())||(Dice.rollPercentage()<=target.charStats().getSave(CharStats.SAVE_TRAPS)))
				target.location().show(target,null,null,Affect.MASK_GENERAL|Affect.MSG_NOISE,"<S-NAME> avoid(s) the poison gas!");
			else
			if(invoker().mayIFight(target))
				if(target.location().show(invoker(),target,this,Affect.MASK_GENERAL|Affect.MSG_NOISE,affected.displayName()+" spews poison gas all over <T-NAME>!"))
				{
					super.spring(target);
					Ability A=CMClass.getAbility(text());
					if(A==null) A=CMClass.getAbility("Poison");
					if(A!=null) A.invoke(invoker(),target,true);
				}
		}
	}
	
}