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
   Copyright 2000-2006 Bo Zimmerman

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
public class Prayer_Sanctum extends Prayer
{
	public String ID() { return "Prayer_Sanctum"; }
	public String name(){return "Sanctum";}
	public String displayText(){return "(Sanctum)";}
	public int abstractQuality(){ return OK_OTHERS;}
	protected int canAffectCode(){return CAN_ROOMS;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(affected==null)
			return super.okMessage(myHost,msg);

		Room R=(Room)affected;
		if((msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(msg.target()==R)
		&&(!msg.source().Name().equals(text()))
		&&((msg.source().amFollowing()==null)||(!msg.source().amFollowing().Name().equals(text())))
		&&(!CMLib.utensils().doesHavePriviledgesHere(msg.source(),R)))
		{
			msg.source().tell("You feel your muscles unwilling to cooperate.");
			return false;
		}
		if((CMath.bset(msg.sourceCode(),CMMsg.MASK_MALICIOUS))
		||(CMath.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
		||(CMath.bset(msg.othersCode(),CMMsg.MASK_MALICIOUS)))
		{
			if((msg.source()!=null)
			&&(msg.target()!=null)
			&&(msg.source()!=affected)
			&&(msg.source()!=msg.target()))
			{
				if(affected instanceof MOB)
				{
					MOB mob=(MOB)affected;
					if((CMLib.flags().aliveAwakeMobile(mob,true))
					&&(!mob.isInCombat()))
					{
						String t="No fighting!";
						if(text().indexOf(";")>0)
						{
							Vector V=CMParms.parseSemicolons(text(),true);
							t=(String)V.elementAt(CMLib.dice().roll(1,V.size(),-1));
						}
						CMLib.commands().postSay(mob,msg.source(),t,false,false);
					}
					else
						return super.okMessage(myHost,msg);
				}
				else
				{
					String t="You feel too peaceful here.";
                    if(text().indexOf(";")>0)
					{
						Vector V=CMParms.parseSemicolons(text(),true);
						t=(String)V.elementAt(CMLib.dice().roll(1,V.size(),-1));
					}
					msg.source().tell(t);
				}
				if(msg.source().getVictim()!=null)
					msg.source().getVictim().makePeace();
				msg.source().makePeace();
				msg.modify(msg.source(),msg.target(),msg.tool(),CMMsg.NO_EFFECT,"",CMMsg.NO_EFFECT,"",CMMsg.NO_EFFECT,"");
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Environmental target=mob.location();
		if(target==null) return false;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell("This place is already a sanctum.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,somanticCastCode(mob,target,auto),auto?"":"^S<S-NAME> "+prayForWord(mob)+" to make this place a sanctum.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				setMiscText(mob.Name());
				if((target instanceof Room)
				&&(CMLib.utensils().doesOwnThisProperty(mob,((Room)target))))
				{
					target.addNonUninvokableEffect((Ability)this.copyOf());
					CMLib.database().DBUpdateRoom((Room)target);
				}
				else
					beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> "+prayForWord(mob)+" to make this place a sanctum, but <S-IS-ARE> not answered.");

		return success;
	}
}
