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
public class Prayer_SanctifyRoom extends Prayer
{
	public String ID() { return "Prayer_SanctifyRoom"; }
	public String name(){return "Sanctify Room";}
	public String displayText(){return "(Sanctify Room)";}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_WARDING;}
	public int abstractQuality(){ return Ability.QUALITY_MALICIOUS;}
	protected int canAffectCode(){return CAN_ROOMS;}
	protected int canTargetCode(){return CAN_ROOMS;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}
    
    public static final HashSet MSG_CODESH=CMParms.makeHashSet(Integer.valueOf(CMMsg.TYP_GET),
                                                               Integer.valueOf(CMMsg.TYP_PULL),
                                                               Integer.valueOf(CMMsg.TYP_PUSH),
                                                               Integer.valueOf(CMMsg.TYP_CAST_SPELL));

    protected boolean inRoom(MOB mob, Room R)
    {
        boolean inRoom=((CMLib.law().doesHavePriviledgesHere(mob,R))
                        ||((text().length()>0)&&(mob.Name().equals(text())))
                        ||((text().length()>0)&&(mob.getClanID().equals(text()))));
        inRoom = inRoom || CMSecurity.isAllowed(mob, R,"CMDROOMS");
        inRoom = inRoom || CMSecurity.isAllowed(mob, R,"CMDITEMS");
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
        int targMinor=msg.targetMinor();
        if(((targMinor==CMMsg.TYP_GET)
            ||(targMinor==CMMsg.TYP_PULL)
            ||(targMinor==CMMsg.TYP_PUSH)
            ||(targMinor==CMMsg.TYP_CAST_SPELL))
        &&(msg.target() instanceof Item)
        &&((msg.targetMessage()==null)||(!msg.targetMessage().equalsIgnoreCase("GIVE")))
        &&(!msg.source().isMine(msg.target()))
        &&((!(msg.tool() instanceof Item))
            ||(!msg.source().isMine(msg.tool()))))
            return inRoom(msg.source(),R);
		return super.okMessage(myHost,msg);
	}

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if(mob.isInCombat())
                return Ability.QUALITY_INDIFFERENT;
            if(target instanceof MOB)
            {
            }
        }
        return super.castingQuality(mob,target);
    }
    
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Environmental target=mob.location();
		if(target==null) return false;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell("This place is already a sanctified place.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> "+prayForWord(mob)+" to sanctify this place.^?");
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
                    
                    if((clanID.length()>0)
                    &&(CMLib.law().doesOwnThisProperty(clanID,((Room)target)))
                    &&(CMLib.clans().getClan(clanID)!=null)
                    &&(!CMLib.clans().getClan(clanID).getMorgue().equals(CMLib.map().getExtendedRoomID((Room)target))))
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
			beneficialWordsFizzle(mob,target,"<S-NAME> "+prayForWord(mob)+" to sanctify this place, but <S-IS-ARE> not answered.");

		return success;
	}
}
