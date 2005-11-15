package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class Spell_PolymorphSelf extends Spell
{
	public String ID() { return "Spell_PolymorphSelf"; }
	public String name(){return "Polymorph Self";}
	public String displayText(){return "(Polymorph Self)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_TRANSMUTATION;}

	Race newRace=null;

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(newRace!=null)
		{
			if(affected.name().indexOf(" ")>0)
				affectableStats.setName("a "+newRace.name()+" called "+affected.name());
			else
				affectableStats.setName(affected.name()+" the "+newRace.name());
			int oldAdd=affectableStats.weight()-affected.baseEnvStats().weight();
			newRace.setHeightWeight(affectableStats,'M');
			if(oldAdd>0) affectableStats.setWeight(affectableStats.weight()+oldAdd);
		}
	}
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(newRace!=null)
		{
		    int oldCat=affected.baseCharStats().ageCategory();
			affectableStats.setMyRace(newRace);
			if(affected.baseCharStats().getStat(CharStats.AGE)>0)
				affectableStats.setStat(CharStats.AGE,newRace.getAgingChart()[oldCat]);
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
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> morph(s) back into <S-HIM-HERSELF> again.");
	}



	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if((auto||mob.isMonster())&&((commands.size()<1)||(((String)commands.firstElement()).equals(mob.name()))))
		{
			commands.clear();
			Vector V=Util.denumerate(CMClass.races());
			for(int v=V.size()-1;v>=0;v--)
				if(!Util.bset(((Race)V.elementAt(v)).availabilityCode(),Area.THEME_FANTASY))
					V.removeElementAt(v);
			if(V.size()>0)
				commands.addElement(((Race)V.elementAt(Dice.roll(1,V.size(),-1))).name());
		}
		if(commands.size()==0)
		{
			mob.tell("You need to specify what to turn yourself into!");
			return false;
		}
		String race=Util.combine(commands,0);
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		Race R=CMClass.getRace(race);
		if((R==null)||(!Util.bset(R.availabilityCode(),Area.THEME_FANTASY)))
		{
			mob.tell("You can't turn yourself into a '"+race+"'!");
			return false;
		}
		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> <S-IS-ARE> already polymorphed.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int mobStatTotal=0;
		for(int s=0;s<CharStats.NUM_BASE_STATS;s++)
			mobStatTotal+=mob.baseCharStats().getStat(s);

		MOB fakeMOB=CMClass.getMOB("StdMOB");
		for(int s=0;s<CharStats.NUM_BASE_STATS;s++)
			fakeMOB.baseCharStats().setStat(s,mob.baseCharStats().getStat(s));
		fakeMOB.baseCharStats().setMyRace(R);
		fakeMOB.recoverCharStats();
		fakeMOB.recoverEnvStats();
		fakeMOB.recoverMaxState();
		int fakeStatTotal=0;
		for(int s=0;s<CharStats.NUM_BASE_STATS;s++)
			fakeStatTotal+=fakeMOB.charStats().getStat(s);

        fakeMOB.destroy();
		int statDiff=mobStatTotal-fakeStatTotal;
		boolean success=profficiencyCheck(mob,-(statDiff*5),auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> whisper(s) to <T-NAMESELF> about "+R.name()+"s.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					newRace=R;
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> become(s) a "+newRace.name()+"!");
					success=beneficialAffect(mob,target,asLevel,0);
					target.recoverCharStats();
					target.confirmWearability();
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> whisper(s) to <T-NAMESELF>, but the spell fizzles.");

		// return whether it worked
		return success;
	}
}
