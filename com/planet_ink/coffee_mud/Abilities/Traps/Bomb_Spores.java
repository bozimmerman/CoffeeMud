package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Bomb_Spores extends StdBomb
{
	public String ID() { return "Bomb_Spores"; }
	public String name(){ return "spore bomb";}
	protected int trapLevel(){return 15;}
	public String requiresToSet(){return "some diseased meat";}
	public Environmental newInstance(){	return new Bomb_Spores();}

	public Vector returnOffensiveAffects(Environmental fromMe)
	{
		Vector offenders=new Vector();

		for(int a=0;a<fromMe.numEffects();a++)
		{
			Ability A=fromMe.fetchEffect(a);
			if((A!=null)&&(A.classificationCode()==Ability.DISEASE))
				offenders.addElement(A);
		}
		return offenders;
	}

	public boolean canSetTrapOn(MOB mob, Environmental E)
	{
		if(!super.canSetTrapOn(mob,E)) return false;
		Vector V=returnOffensiveAffects(E);
		if((!(E instanceof Food))||(V.size()==0))
		{
			if(mob!=null)
				mob.tell("You need some diseased meat to make this out of.");
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
			if((!invoker().mayIFight(target))||(target==invoker())||(Dice.rollPercentage()<=target.charStats().getSave(CharStats.SAVE_TRAPS)))
				target.location().show(target,null,null,CMMsg.MASK_GENERAL|CMMsg.MSG_NOISE,"<S-NAME> avoid(s) the poison gas!");
			else
			if(target.location().show(invoker(),target,this,CMMsg.MASK_GENERAL|CMMsg.MSG_NOISE,affected.name()+" spews poison gas all over <T-NAME>!"))
			{
				super.spring(target);
				Ability A=CMClass.getAbility(text());
				if(A==null) A=CMClass.getAbility("Disease_Cold");
				if(A!=null)
					A.invoke(invoker(),target,true);
			}
		}
	}

}