package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class Spell_WardArea extends Spell implements Trap
{
	public String ID() { return "Spell_WardArea"; }
	public String name(){return "Ward Area";}
	public String displayText(){return "(Ward Area spell)";}
	protected int canAffectCode(){return CAN_ROOMS;}
	protected int canTargetCode(){return CAN_ROOMS;}
	public int quality(){ return MALICIOUS;}
	private Ability shooter=null;
	private Vector parameters=null;
	public int classificationCode(){	return Ability.SPELL|Ability.DOMAIN_EVOCATION;}
	private boolean sprung=false;

	public MOB theInvoker()
	{
		if(invoker()!=null) return invoker();
		if(text().length()>0)
			invoker=(MOB)CMMap.getPlayer(text());
		return invoker();
	}

	public boolean isABomb(){return false;}
	public void activateBomb(){}
	public boolean disabled(){return sprung;}
	public void disable(){unInvoke();}
	public void setReset(int Reset){}
	public int getReset(){return 0;}
	public boolean maySetTrap(MOB mob, int asLevel){return false;}
	public boolean canSetTrapOn(MOB mob, Environmental E){return false;}
	public String requiresToSet(){return "";}
	public Trap setTrap(MOB mob, Environmental E, int classLevel, int qualifyingClassLevel)
	{beneficialAffect(mob,E,0);return (Trap)E.fetchEffect(ID());}

	public boolean sprung(){return sprung;}
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(sprung) return super.okMessage(myHost,msg);
		if(!super.okMessage(myHost,msg))
			return false;

		if((msg.amITarget(affected))
		&&(!msg.amISource(invoker())))
		{
			if((msg.targetMinor()==CMMsg.TYP_ENTER)
			||(msg.targetMinor()==CMMsg.TYP_LEAVE)
			||(msg.targetMinor()==CMMsg.TYP_FLEE))
			{
				if(msg.targetMinor()==CMMsg.TYP_LEAVE)
					return true;
				else
				{
					spring(msg.source());
					return false;
				}
			}
		}
		return true;
	}


	public void spring(MOB mob)
	{
		if(affected==null)
			return;
		if(!(affected instanceof Room))
			return;
		if((shooter==null)||(parameters==null))
			return;
		if(Dice.rollPercentage()<mob.charStats().getSave(CharStats.SAVE_TRAPS))
			mob.location().show(mob,affected,this,CMMsg.MSG_OK_ACTION,"<S-NAME> avoid(s) a magical ward trap.");
		else
		{
			MOB newCaster=CMClass.getMOB("StdMOB");
			newCaster.setName("the thin air");
			newCaster.setDescription(" ");
			newCaster.setDisplayText(" ");
			if(invoker()!=null)
				newCaster.baseEnvStats().setLevel(invoker.envStats().level());
			else
				newCaster.baseEnvStats().setLevel(10);
			newCaster.recoverEnvStats();
			newCaster.recoverCharStats();
			if(invoker()!=null)
				newCaster.setLiegeID(invoker().Name());
			newCaster.setLocation((Room)affected);
			try
			{
				shooter.invoke(newCaster,parameters,mob,true);
			}
			catch(Exception e){Log.errOut("WARD/"+Util.combine(parameters,0),e);}
			newCaster.setLocation(null);
			newCaster.destroy();
		}
		unInvoke();
		sprung=true;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(sprung)
			return;

		if((msg.amITarget(affected))
		&&(!msg.amISource(invoker())))
		{
			if(msg.targetMinor()==CMMsg.TYP_LEAVE)
				spring(msg.source());
		}
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		super.unInvoke();
		if(canBeUninvoked())
		{
			shooter=null;
			parameters=null;
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()<1)
		{
			mob.tell("You must specify what arcane spell to set, and any necessary parameters.");
			return false;
		}
		commands.insertElementAt("CAST",0);
		shooter=EnglishParser.getToEvoke(mob,commands);
		parameters=commands;
		if((shooter==null)||((shooter.classificationCode()&Ability.ALL_CODES)!=Ability.SPELL))
		{
			parameters=null;
			shooter=null;
			mob.tell("You don't know any arcane spell by that name.");
			return false;
		}

		if(shooter.quality()==Ability.MALICIOUS)
		for(int m=0;m<mob.location().numInhabitants();m++)
		{
			MOB M=mob.location().fetchInhabitant(m);
			if((M!=null)&&(M!=mob)&&(!M.mayIFight(mob)))
			{
				mob.tell("You cannot set that spell here -- there are other players present!");
				return false;
			}
		}
		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		Environmental target = mob.location();
		if((target.fetchEffect(this.ID())!=null)||(givenTarget!=null))
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"A ward trap has already been set here!");
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
			return false;
		}

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.

			FullMsg msg = new FullMsg(mob, target, this, affectType(auto), auto?"":"^S<S-NAME> set(s) a magical trap.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				setMiscText(mob.Name());
				if(CoffeeUtensils.doesOwnThisProperty(mob,mob.location()))
				{
					mob.location().addNonUninvokableEffect((Ability)copyOf());
					CMClass.DBEngine().DBUpdateRoom(mob.location());
				}
				else
					beneficialAffect(mob,mob.location(),9999);
				shooter=null;
				parameters=null;
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> attempt(s) to set a magic trap, but fail(s).");

		// return whether it worked
		return success;
	}
}
