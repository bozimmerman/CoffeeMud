package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Siphon extends Spell
{
   private Random randomizer = null;
   public Spell_Siphon()
   {
      randomizer = new Random(System.currentTimeMillis());
   }
	public String ID() { return "Spell_Siphon"; }
	public String name(){return "Siphon";}
	public String displayText(){return "(Siphon spell)";}
	public int maxRange(){return 1;}
	public int quality(){return MALICIOUS;};
	public Environmental newInstance(){return new Spell_Siphon();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_CONJURATION;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> feel(s) a thirst for energy.":"^S<S-NAME> invoke(s) an area deprived of energy around <T-NAMESELF>.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke immunizing protection, but fail(s).");

		return success;
	}
	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();

		mob.tell("You no longer feel a thirst for the energy of others.");
	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		if(affect.amITarget(mob))
		{
			if(affect.targetMinor()==Affect.TYP_WEAPONATTACK)
			{
            int maxManaRestore = (mob.maxState().getMana() - mob.curState().getMana()) * 100 / (25 + (randomizer.nextInt() % 5 + 1));
            MOB source = affect.source();
            int curSourceMana = source.curState().getMana();
            int manaDrain = 0;
            if(maxManaRestore <= curSourceMana)
            {
               manaDrain = maxManaRestore;
            }
            else
            {
               manaDrain = curSourceMana;
            }
            mob.curState().adjMana(manaDrain, mob.maxState());
            source.curState().adjMana(manaDrain * -1, source.maxState());
				FullMsg msg=new FullMsg(mob,affect.source(),null,Affect.MSG_QUIETMOVEMENT,"<S-NAME> siphons mana from <T-NAME>!");
			}
		}
		return super.okAffect(myHost, affect);
	}
}