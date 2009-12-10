package com.planet_ink.coffee_mud.Abilities.Spells;
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
public class Spell_Brainwash extends Spell
{
	public String ID() { return "Spell_Brainwash"; }
	public String name(){return "Brainwash";}
	public String displayText(){return "(brainwashed)";}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_ENCHANTMENT;}
	public Vector limbsToRemove=new Vector();
	
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if((msg.amISource((MOB)affected))
		&&(msg.sourceMessage()!=null)
		&&(msg.tool()==null)
		&&((msg.sourceMinor()==CMMsg.TYP_SPEAK)
		   ||(msg.sourceMinor()==CMMsg.TYP_TELL)
		   ||(CMath.bset(msg.sourceCode(),CMMsg.MASK_CHANNEL))))
		{
			String str=CMStrings.getSayFromMessage(msg.othersMessage());
			if(str==null) str=CMStrings.getSayFromMessage(msg.targetMessage());
			if(str!=null)
			{
				String smsg=CMStrings.getSayFromMessage(msg.sourceMessage());
				String lead=" ";
				switch(CMLib.dice().roll(1,6,0))
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
                          CMStrings.substituteSayInMessage(msg.sourceMessage(),smsg),
						  msg.targetCode(),
                          CMStrings.substituteSayInMessage(msg.targetMessage(),str),
						  msg.othersCode(),
                          CMStrings.substituteSayInMessage(msg.othersMessage(),str));
				helpProficiency((MOB)affected);
			}
		}
	    return super.okMessage(host,msg);
	}
	
    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if((mob.isInCombat())&&(mob.isMonster()))
                return Ability.QUALITY_INDIFFERENT;
            if(target instanceof MOB)
            {
                if(CMLib.flags().isAnimalIntelligence((MOB)target))
                    return Ability.QUALITY_INDIFFERENT;
            }
        }
        return super.castingQuality(mob,target);
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
	        message=CMParms.combine(commands,1);
	        commands=CMParms.makeVector(commands.firstElement());
	    }
	    else
	    if(text().length()>0)
	        message=text();
	    else
	        message=CMParms.combine(commands,0);
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(CMLib.flags().isAnimalIntelligence(target))
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
		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
		    MOB oldVictim=mob.getVictim();
		    MOB oldVictim2=target.getVictim();
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),(auto?"!":"^S<S-NAME> invoke(s) a spell upon the mind of <T-NAMESELF>, saying '"+message+"'.^?"));
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


