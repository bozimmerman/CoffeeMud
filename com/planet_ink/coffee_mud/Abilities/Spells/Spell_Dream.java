package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Dream extends Spell
{
	public String ID() { return "Spell_Dream"; }
	public String name(){return "Dream";}
	public Environmental newInstance(){	return new Spell_Dream();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ILLUSION;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{

		if(commands.size()<1)
		{
			mob.tell("Invoke a dream about what?");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),"^S<S-NAME> invoke(s) a dreamy spell.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				for(Iterator r=CMMap.rooms();r.hasNext();)
				{
					Room R=(Room)r.next();
					for(int i=0;i<R.numInhabitants();i++)
					{
						MOB inhab=R.fetchInhabitant(i);
						if((inhab!=null)&&(Sense.isSleeping(inhab)))
						{
							msg=new FullMsg(mob,inhab,this,affectType(auto),null);
							if(R.okAffect(msg))
								inhab.tell("You dream "+Util.combine(commands,0)+".");
						}
					}
				}
			}

		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to invoke a dream, but fizzle(s) the spell.");


		// return whether it worked
		return success;
	}
}
