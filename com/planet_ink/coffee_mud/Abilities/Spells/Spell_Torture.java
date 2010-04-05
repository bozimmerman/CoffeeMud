package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.MemberRecord;
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
public class Spell_Torture extends Spell
{
	public String ID() { return "Spell_Torture"; }
	public String name(){return "Torture";}
	public String displayText(){return "(being tortured)";}
	public int maxRange(){return adjustedMaxInvokerRange(1);}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_ILLUSION;}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
		if((!mob.amDead())&&(mob.location()!=null))
			mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to survive the torture.");
	}

	public void cryOut(MOB mob)
	{
		if((text().length()>0)&&(!text().equalsIgnoreCase("HITONLY")))
        {
		    for(int s=0;s<CMLib.sessions().size();s++)
		        CMLib.sessions().elementAt(s).println(text());
		    setMiscText("");
		    return;
        }
        int roll=CMLib.dice().roll(1,16,0);
        boolean someoneelse=false;
        for(int i=0;i<mob.location().numInhabitants();i++)
        {
            MOB M=mob.location().fetchInhabitant(i);
            if((M!=null)&&(!M.isMonster())&&(M!=mob))
                someoneelse=true;
        }
        if(!someoneelse)
            roll=CMLib.dice().roll(1,10,0);
        else
        switch(roll)
        {
    	case 11:
			if(mob.getLiegeID().length()>0)
				mob.location().show(mob,null,CMMsg.MSG_SPEAK,
				"<S-NAME> admits that "+mob.getLiegeID()+" is <S-HIS-HER> liege.");
			else
	            roll=CMLib.dice().roll(1,10,0);
			break;
	    case 12:
	    {
			if(mob.getClanID().length()==0)
			    roll=CMLib.dice().roll(1,10,0);
			else
			{
		        Clan C=CMLib.clans().getClan(mob.getClanID());
				if(C==null)
				    roll=CMLib.dice().roll(1,10,0);
				else
				{
					List<MemberRecord> V=C.getMemberList();
					if(V.size()>0)
					{
					    String name=V.get(CMLib.dice().roll(1,V.size(),-1)).name;
					    if(name.equals(mob.Name()))
					        roll=CMLib.dice().roll(1,10,0);
					    else
							mob.location().show(mob,null,CMMsg.MSG_SPEAK,
							"<S-NAME> mutters that "+name+" is a part of his clan, called "+mob.getClanID()+".");
					}
				}
			}
	        break;
	    }
        case 13:
			if(mob.getClanID().length()==0)
			    roll=CMLib.dice().roll(1,10,0);
			else
			{
		        Clan C=CMLib.clans().getClan(mob.getClanID());
				if(C==null)
				    roll=CMLib.dice().roll(1,10,0);
				else
					mob.location().show(mob,null,CMMsg.MSG_SPEAK,
					"<S-NAME> mutters that "+mob.getClanID()+" has "+C.getExp()+" experience points.");
			}
	        break;
        case 14:
			if(!CMSecurity.isDisabled("EXPERIENCE")
			&&!mob.charStats().getCurrentClass().expless()
			&&!mob.charStats().getMyRace().expless())
				mob.location().show(mob,null,CMMsg.MSG_SPEAK,
				"<S-NAME> mutters that <S-HE-SHE> scored "+mob.getExperience()+" experience points.");
	        break;
        case 15:
        {
           StringBuffer str=new StringBuffer("");
           Command C=CMClass.getCommand("Affect");
           try{C.execute(mob,CMParms.makeVector(str),0);}catch(Exception e){}
			mob.location().show(mob,null,CMMsg.MSG_SPEAK,
			"<S-NAME> says OK! I am affected by:\n\r"+str.toString());
			break;
        }
        case 16:
			if(mob.numAbilities()<1)
			    roll=CMLib.dice().roll(1,10,0);
			else
			{
	           Ability A=mob.fetchAbility(CMLib.dice().roll(1,mob.numAbilities(),-1));
				mob.location().show(mob,null,CMMsg.MSG_SPEAK,
				"<S-NAME> admit(s) that <S-HE-SHE> knows "+A.name()+" at "+A.proficiency()+"%.");
	        }
			break;
        }
        
		switch(roll)
		{
		case 1:	mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,
			"<S-NAME> struggle(s) against the pain."); break;
		case 2:	mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,
			"<S-NAME> scream(s) in horror!"); break;
		case 3:	mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,
			"<S-NAME> beg(s) for mercy."); break;
		case 4:	mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,
			"<S-NAME> grab(s) <S-HIS-HER> head and cr(ys)."); break;
		case 5:	mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,
			"<S-NAME> whimper(s)."); break;
		case 6:	mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,
			"<S-NAME> look(s) terrified!"); break;
		case 7:	mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,
			"<S-NAME> shake(s) in pain from <S-HIS-HER> head to <S-HIS-HER> feet."); break;
		case 8:	mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,
			"<S-NAME> gasp(s) for air."); break;
		case 9:	mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,
			"<S-NAME> shiver(s) in pain."); break;
		case 10:mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,
			"<S-NAME> cr(ys) in anticipation of pain!"); break;
		}
	}
	
	public void executeMsg(Environmental host, CMMsg msg)
	{
	    if((msg.amITarget(affected))
	    &&(text().equalsIgnoreCase("HITONLY"))
	    &&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
	    &&(msg.value()>0))
	        cryOut((MOB)affected);
		super.executeMsg(host,msg);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
	    if((!text().equalsIgnoreCase("HITONLY"))
	    &&(affected instanceof MOB))
	        cryOut((MOB)affected);
		return super.tick(ticking,tickID);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;


		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> whisper(s) a torturous spell to <T-NAMESELF>.^?");
			CMMsg msg2=CMClass.getMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_MIND|(auto?CMMsg.MASK_ALWAYS:0),null);
			if((mob.location().okMessage(mob,msg))||(mob.location().okMessage(mob,msg2)))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
				if((msg.value()<=0)&&(msg2.value()<=0))
					maliciousAffect(mob,target,asLevel,0,-1);
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> whisper(s) a torturous spell to <T-NAMESELF>, but the spell fades.");

		// return whether it worked
		return success;
	}
}


