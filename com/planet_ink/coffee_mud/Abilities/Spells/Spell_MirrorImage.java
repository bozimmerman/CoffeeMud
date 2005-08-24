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
public class Spell_MirrorImage extends Spell
{
	public String ID() { return "Spell_MirrorImage"; }
	public String name(){return "Mirror Image";}
	public String displayText(){return "(Mirror Image spell)";}
	public int quality(){return BENEFICIAL_SELF;};
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ILLUSION;}

	private	Random randomizer = new Random(System.currentTimeMillis());
	private int numberOfImages = 0;
	private boolean notAgain=false;


	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		if((msg.amITarget(mob))&&(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK))
		{
			if(invoker()!=null)
			{
				if(numberOfImages <= 0)
				{
					unInvoke();
					return true;
				}
				int numberOfTargets = numberOfImages + 1;
				if(randomizer.nextInt() % numberOfTargets == 0)
				{
					if(mob.location().show(mob,msg.source(),CMMsg.MSG_NOISYMOVEMENT,"<T-NAME> attack(s) a mirrored image!"))
						numberOfImages--;
					return false;
				}
			}
		}
		return true;
	}
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		if((affected==null)||(!(affected instanceof MOB)))
			return;

		if(notAgain) return;

		MOB mob=(MOB)affected;
		if(msg.amISource(mob))
		{
			if((
				(Util.bset(msg.othersCode(),CMMsg.MASK_EYES))
				||(Util.bset(msg.othersCode(),CMMsg.MASK_MOVE))
				||(Util.bset(msg.othersCode(),CMMsg.MASK_MOUTH))
				||(Util.bset(msg.othersCode(),CMMsg.MASK_HANDS)))
			&&(msg.othersMessage()!=null)
			&&(msg.targetMinor()!=CMMsg.TYP_DAMAGE)
			&&(msg.othersMessage().length()>0))
			{
				notAgain=true;
				if(numberOfImages<=0)
					unInvoke();
				else
					for(int x=0;x<numberOfImages;x++)
						msg.addTrailerMsg(new FullMsg(mob,msg.target(),msg.tool(),CMMsg.MSG_OK_VISUAL,msg.othersMessage()));
			}
		}
		else
		if((msg.amITarget(mob.location())&&(!msg.amISource(mob))&&(msg.targetMinor()==CMMsg.TYP_LOOK))
		&&((Sense.canBeSeenBy(mob,msg.source()))&&(mob.displayText(msg.source()).length()>0)))
		{
			StringBuffer Say=new StringBuffer("");
			for(int i=0;i<numberOfImages;i++)
			{
				Say.append("^M");
				if(mob.displayText(msg.source()).length()>0)
					Say.append(mob.displayText(msg.source()));
				else
					Say.append(mob.name());
				Say.append(Sense.colorCodes(mob,msg.source())+"^N\n\r");
			}
			if(Say.toString().length()>0)
			{
				FullMsg msg2=new FullMsg(msg.source(),null,this,CMMsg.MSG_OK_VISUAL,Say.toString(),CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null);
				msg.addTrailerMsg(msg2);
			}
		}
		notAgain=false;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setArmor(affectableStats.armor() - 5);
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			numberOfImages=0;
		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("Your mirror images fade away.");
	}



	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> already <S-HAS-HAVE> mirror images.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			numberOfImages = Dice.roll(1,(int)(Math.round(Util.div(adjustedLevel(mob,asLevel),3.0))),2);
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),(auto?"A spell forms around":"^S<S-NAME> incant(s) the reflective spell of")+" <T-NAME>, and suddenly " + numberOfImages + " copies appear.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
		{
			numberOfImages = 0;
			return beneficialWordsFizzle(mob,target,"<S-NAME> speak(s) reflectively, but nothing more happens.");
		}
		// return whether it worked
		return success;
	}
}
