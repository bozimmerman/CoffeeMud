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
public class Spell_ComprehendLangs extends Spell
{
	public String ID() { return "Spell_ComprehendLangs"; }
	public String name(){return "Comprehend Languages";}
	public String displayText(){return "(Comprehend Languages)";}
	public int quality(){return BENEFICIAL_SELF;};
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){	return Ability.SPELL|Ability.DOMAIN_DIVINATION;}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("You no longer feel so comprehensive.");
	}

	protected String getMsgFromAffect(String msg)
	{
		if(msg==null) return null;
		int start=msg.indexOf("'");
		int end=msg.lastIndexOf("'");
		if((start>0)&&(end>start))
			return msg.substring(start+1,end).trim();
		return null;
	}
	protected String subStitute(String affmsg, String msg)
	{
		if(affmsg==null) return null;
		int start=affmsg.indexOf("'");
		int end=affmsg.lastIndexOf("'");
		if((start>0)&&(end>start))
			return affmsg.substring(0,start+1)+msg+affmsg.substring(end);
		return affmsg;
	}
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected instanceof MOB)
		&&(!msg.amISource((MOB)affected))
		&&(msg.tool() instanceof Ability))
		{
			if((msg.tool().ID().equals("Fighter_SmokeSignals"))
			&&(msg.sourceCode()==CMMsg.NO_EFFECT)
			&&(msg.targetCode()==CMMsg.NO_EFFECT)
			&&(msg.othersMessage()!=null))
				msg.addTrailerMsg(new FullMsg(msg.source(),null,null,CMMsg.MSG_OK_VISUAL,"The smoke signals seem to say '"+msg.othersMessage()+"'.",CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
			else
			if(((msg.sourceMinor()==CMMsg.TYP_SPEAK)
			   ||(msg.sourceMinor()==CMMsg.TYP_TELL)
			   ||(Util.bset(msg.sourceCode(),CMMsg.MASK_CHANNEL)))
			&&(msg.sourceMessage()!=null)
			&&(((Ability)msg.tool()).classificationCode()==Ability.LANGUAGE)
			&&(((MOB)affected).fetchEffect(msg.tool().ID())==null))
			{
				String str=this.getMsgFromAffect(msg.sourceMessage());
				if(str!=null)
				{
					if(Util.bset(msg.sourceCode(),CMMsg.MASK_CHANNEL))
						msg.addTrailerMsg(new FullMsg(msg.source(),null,null,CMMsg.NO_EFFECT,CMMsg.NO_EFFECT,msg.othersCode(),this.subStitute(msg.othersMessage(),str)+" (translated from "+msg.tool().name()+")"));
					else
					if(msg.amITarget(affected)&&(msg.targetMessage()!=null))
						msg.addTrailerMsg(new FullMsg(msg.source(),affected,null,CMMsg.NO_EFFECT,msg.targetCode(),CMMsg.NO_EFFECT,this.subStitute(msg.targetMessage(),str)+" (translated from "+msg.tool().name()+")"));
					else
					if((msg.othersMessage()!=null)&&(msg.othersMessage().indexOf("'")>0))
					{
						String otherMes=msg.othersMessage();
						if(msg.target()!=null)
							otherMes=CoffeeFilter.fullOutFilter(((MOB)affected).session(),(MOB)affected,msg.source(),msg.target(),msg.tool(),otherMes,false);
						msg.addTrailerMsg(new FullMsg(msg.source(),affected,null,CMMsg.NO_EFFECT,msg.othersCode(),CMMsg.NO_EFFECT,this.subStitute(otherMes,str)+" (translated from "+msg.tool().name()+")"));
					}
				}
			}
		}
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> already <S-HAS-HAVE> comprehension.");
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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> feel(s) more comprehending.":"^S<S-NAME> invoke(s) the power of comprehension!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke a spell, but fail(s) miserably.");

		// return whether it worked
		return success;
	}
}
