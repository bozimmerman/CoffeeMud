package com.planet_ink.coffee_mud.Abilities.Paladin;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Paladin_Goodness extends Paladin
{
	public String ID() { return "Paladin_Goodness"; }
	public String name(){ return "Paladin`s Goodness";}
	private boolean tickTock=false;
	public Paladin_Goodness()
	{
		super();
		paladinsGroup=new Vector();
	}
	public Environmental newInstance(){	return new Paladin_Goodness();}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		tickTock=!tickTock;
		if(tickTock)
		{
			MOB mob=(MOB)invoker;
			for(int m=0;m<mob.location().numInhabitants();m++)
			{
				MOB target=mob.location().fetchInhabitant(m);
				if((target!=null)
				&&(target.getAlignment()<350)
				&&((paladinsGroup.contains(target))
					||((target.getVictim()==invoker)&&(target.rangeToTarget()==0)))
			    &&((invoker==null)||(invoker.fetchAbility(ID())==null)||profficiencyCheck(0,false)))
				{
					int harming=Dice.roll(1,15,0);
					if(target.getAlignment()<350)
						ExternalPlay.postDamage(invoker,target,this,harming,CMMsg.MASK_EYES|CMMsg.MASK_MALICIOUS|CMMsg.TYP_UNDEAD,Weapon.TYPE_BURSTING,"^SThe aura of goodness around <S-NAME> <DAMAGE> <T-NAME>!^?");
				}
			}
		}
		return true;
	}

}
