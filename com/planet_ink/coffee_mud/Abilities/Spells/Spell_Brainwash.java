package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.Misc.Amputation;
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
public class Spell_Brainwash extends Spell
{
	public String ID() { return "Spell_Brainwash"; }
	public String name(){return "Brainwash";}
	public String displayText(){return "(brainwashed)";}
	public int quality(){return MALICIOUS;};
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ENCHANTMENT;}
	public Vector limbsToRemove=new Vector();
	
	protected String getMsgFromAffect(String msg)
	{
		if(msg==null) return null;
		int start=msg.indexOf("'");
		int end=msg.lastIndexOf("'");
		if((start>0)&&(end>start))
			return msg.substring(start+1,end);
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
	
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if((msg.amISource((MOB)affected))
		&&(msg.sourceMessage()!=null)
		&&(msg.tool()==null)
		&&((msg.sourceMinor()==CMMsg.TYP_SPEAK)
		   ||(msg.sourceMinor()==CMMsg.TYP_TELL)
		   ||(Util.bset(msg.sourceCode(),CMMsg.MASK_CHANNEL))))
		{
			String str=getMsgFromAffect(msg.othersMessage());
			if(str==null) str=getMsgFromAffect(msg.targetMessage());
			if(str!=null)
			{
				String smsg=getMsgFromAffect(msg.sourceMessage());
				String lead=" ";
				switch(Dice.roll(1,6,0))
				{
				case 1:	lead=" And, by the way, "; break;
				case 2:	lead=" Also, "; break;
				case 3:	lead=" Did you know that "; break;
				case 4:	lead=" In case I didn't mention it already, "; break;
				case 5:	lead=" You might also be curious to know that "; break;
				case 6: lead=" "; break;
				}
				if(smsg!=null) smsg=smsg+lead+text();
				str=str+lead+text();
				msg.modify(msg.source(),
						  msg.target(),
						  this,
						  msg.sourceCode(),
						  subStitute(msg.sourceMessage(),smsg),
						  msg.targetCode(),
						  subStitute(msg.targetMessage(),str),
						  msg.othersCode(),
						  subStitute(msg.othersMessage(),str));
				helpProfficiency((MOB)affected);
			}
		}
	    return super.okMessage(host,msg);
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
	    String message="";
	    if(givenTarget==null)
	    {
	        if(commands.size()<2)
	        {
	            mob.tell("You must specify your target, followed by the message they will believe.");
	            return false;
	        }
	        message=Util.combine(commands,1);
	        commands=Util.makeVector(commands.firstElement());
	    }
	    else
	    if(text().length()>0)
	        message=text();
	    else
	        message=Util.combine(commands,0);
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(Sense.isAnimalIntelligence(target))
		{
			if(!auto)
				mob.tell(target.name()+" doesn't have much to wash.");
			return false;
		}
		
		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
		    MOB oldVictim=mob.getVictim();
		    MOB oldVictim2=target.getVictim();
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),(auto?"!":"^S<S-NAME> invoke(s) a spell upon the mind of <T-NAMESELF>, saying '"+message+"'.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				super.maliciousAffect(mob,target,asLevel,0,CMMsg.MASK_MALICIOUS|CMMsg.TYP_MIND);
				Ability A=target.fetchEffect(ID());
				if(A!=null) A.setMiscText(message);
				if(mob.getVictim()!=oldVictim)
				    mob.setVictim(oldVictim);
				if(target.getVictim()!=oldVictim2)
				    target.setVictim(oldVictim2);
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> incant(s) at <T-NAMESELF>, but flub(s) the spell.");


		// return whether it worked
		return success;
	}
}

