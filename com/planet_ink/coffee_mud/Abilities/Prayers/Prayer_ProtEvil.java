package com.planet_ink.coffee_mud.Abilities.Prayers;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/*
   Copyright 2000-2010 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
@SuppressWarnings("unchecked")
public class Prayer_ProtEvil extends Prayer
{
	public String ID() { return "Prayer_ProtEvil"; }
	public String name(){ return "Protection Evil";}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_HOLYPROTECTION;}
	public String displayText(){ return "(Protection from Evil)";}
	public int abstractQuality(){ return Ability.QUALITY_OK_SELF;}
	public long flags(){return Ability.FLAG_HOLY;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return false;

		if(invoker==null)
			return false;

		MOB mob=(MOB)affected;

		if(CMLib.flags().isEvil(mob))
		{
			int damage=(int)Math.round(CMath.div(mob.envStats().level()+(2*super.getXLEVELLevel(invoker())),3.0));
			CMLib.combat().postDamage(invoker,mob,this,damage,CMMsg.MASK_ALWAYS|CMMsg.TYP_JUSTICE,Weapon.TYPE_BURSTING,"<T-HIS-HER> protective aura <DAMAGE> <T-NAME>!");
		}
		return super.tick(ticking,tickID);
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if(invoker==null) return true;
		if(affected==null) return true;
		if(!(affected instanceof MOB)) return true;

		if((msg.target()==invoker)&&(msg.source()!=invoker))
		{
			if((CMath.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
			&&(msg.targetMinor()==CMMsg.TYP_CAST_SPELL)
			&&(msg.tool()!=null)
			&&(msg.tool() instanceof Ability)
			&&(!CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_HOLY))
			&&(CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_UNHOLY)))
			{
				msg.source().location().show(invoker,null,CMMsg.MSG_OK_VISUAL,"The holy field around <S-NAME> protect(s) <S-HIM-HER> from the evil magic attack of "+msg.source().name()+".");
				return false;
			}

		}
		return true;
	}


	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==null) return;
		if(!(affected instanceof MOB)) return;
		MOB mob=(MOB)affected;

		if(mob.isInCombat())
		{
			MOB victim=mob.getVictim();
			if(CMLib.flags().isEvil(victim))
				affectableStats.setArmor(affectableStats.armor()-10-(2*super.getXLEVELLevel(invoker())));
		}
	}



	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("Your protection from evil fades.");
	}

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            MOB victim=mob.getVictim();
            if((victim!=null)&&CMLib.flags().isEvil(victim)&&(!CMLib.flags().isEvil(mob)))
                return super.castingQuality(mob, target,Ability.QUALITY_BENEFICIAL_SELF);
        }
        return super.castingQuality(mob,target);
    }
    
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
        Environmental target=mob;
        if((auto)&&(givenTarget!=null)) target=givenTarget;
        if(target.fetchEffect(this.ID())!=null)
		{
            mob.tell(mob,target,null,"<T-NAME> <T-IS-ARE> already affected by "+name()+".");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"<T-NAME> become(s) protected from evil.":"^S<S-NAME> "+prayWord(mob)+" for protection from evil.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> "+prayWord(mob)+" for protection, but there is no answer.");


		// return whether it worked
		return success;
	}
}
