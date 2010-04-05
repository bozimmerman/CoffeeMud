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
public class Prayer_Sanctum extends Prayer
{
	public String ID() { return "Prayer_Sanctum"; }
	public String name(){return "Sanctum";}
	public String displayText(){return "(Sanctum)";}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_WARDING;}
	public int abstractQuality(){ return Ability.QUALITY_OK_OTHERS;}
	protected int canAffectCode(){return CAN_ROOMS;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}

    protected boolean inRoom(MOB mob, Room R)
    {
        boolean inRoom=((CMLib.law().doesHavePriviledgesHere(mob,R))
                ||((text().length()>0)&&(mob.Name().equals(text())))
                ||((text().length()>0)&&(mob.getClanID().equals(text()))&&(mob.getClanRole() != Clan.POS_APPLICANT)));
        for(int i=0;i<R.numInhabitants();i++)
        {
            MOB M=R.fetchInhabitant(i);
            if(CMLib.law().doesHavePriviledgesHere(M,R))
            { inRoom=true; break;}
            if((text().length()>0)&&(M.Name().equals(text())))
            { inRoom=true; break;}
            if((text().length()>0)&&(M.getClanID().equals(text())))
            { inRoom=true; break;}

        }
        if(!inRoom)
        {
            mob.tell("You feel your muscles unwilling to cooperate.");
            return false;
        }
        return true;
    }
    
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(affected==null)
			return super.okMessage(myHost,msg);

		Room R=(Room)affected;
		if((msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(msg.target()==R)
		&&(!msg.source().Name().equals(text()))
        &&((msg.source().getClanID().length()==0)||(!msg.source().getClanID().equals(text())))
		&&((msg.source().amFollowing()==null)
            ||((!msg.source().amFollowing().Name().equals(text()))
                &&(msg.source().amFollowing().getClanID().length()==0)||(!msg.source().amFollowing().getClanID().equals(text()))))
		&&(!CMLib.law().doesHavePriviledgesHere(msg.source(),R)))
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
				MOB victim=msg.source().getVictim();
				if(victim!=null) victim.makePeace();
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

		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> "+prayForWord(mob)+" to make this place a sanctum.^?");
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
                	
                    if((clanID.length()>0)&&(CMLib.law().doesOwnThisProperty(clanID,((Room)target))))
                        beneficialAffect(mob,target,asLevel,0);
                    else
                    {
    					target.addNonUninvokableEffect((Ability)this.copyOf());
    					CMLib.database().DBUpdateRoom((Room)target);
                    }
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
