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
public class Prayer_GuardianHearth extends Prayer
{
	public String ID() { return "Prayer_GuardianHearth"; }
	public String name(){return "Guardian Hearth";}
	public String displayText(){return "(Guardian Hearth)";}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_WARDING;}
	public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}
	protected int canAffectCode(){return CAN_ROOMS;}
	protected int canTargetCode(){return CAN_ROOMS;}
	protected int overrideMana(){return Integer.MAX_VALUE;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}
	protected static HashSet prots=null;

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof Room)))
			return super.okMessage(myHost,msg);

		if(prots==null)
		{
			prots=new HashSet();
			int[] CMMSGMAP=CharStats.CODES.CMMSGMAP();
			for(int i : CharStats.CODES.SAVING_THROWS())
				if(CMMSGMAP[i]>=0)
				   prots.add(Integer.valueOf(CMMSGMAP[i]));
		}
		Room R=(Room)affected;
		if(((msg.tool() instanceof Trap)
		||(prots.contains(Integer.valueOf(msg.sourceMinor())))
		||(prots.contains(Integer.valueOf(msg.targetMinor()))))
		   &&(msg.target() instanceof MOB)
		   &&((msg.source()!=msg.target())||(CMath.bset(msg.sourceMajor(),CMMsg.MASK_ALWAYS))))
		{
			HashSet H=((MOB)msg.target()).getGroupMembers(new HashSet());
			for(Iterator e=H.iterator();e.hasNext();)
            {
                MOB M=(MOB)e.next();
                if((CMLib.law().doesHavePriviledgesHere(M,R))
                ||((text().length()>0)
                    &&((M.Name().equals(text()))
                        ||(M.getClanID().equals(text())))))
                {
					R.show(((MOB)msg.target()),null,this,CMMsg.MSG_OK_VISUAL,"The guardian hearth protect(s) <S-NAME>!");
					break;
				}
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
			mob.tell("This place is already a guarded hearth.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> "+prayForWord(mob)+" to guard this place.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				setMiscText(mob.Name());
				if((target instanceof Room)
				&&(CMLib.law().doesOwnThisProperty(mob,((Room)target))))
				{
					String clanID=mob.getClanID();
					if((mob.amFollowing()!=null)&&(clanID.length()==0))
						clanID=mob.amFollowing().getClanID();
                	if((clanID.length()>0)
                    &&(CMLib.law().doesOwnThisProperty(clanID,((Room)target))))
                        setMiscText(clanID);
					target.addNonUninvokableEffect((Ability)this.copyOf());
					CMLib.database().DBUpdateRoom((Room)target);
				}
				else
					beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> "+prayForWord(mob)+" to guard this place, but <S-IS-ARE> not answered.");

		return success;
	}
}
