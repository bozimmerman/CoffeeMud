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
public class Spell_Torture extends Spell
{
	public String ID() { return "Spell_Torture"; }
	public String name(){return "Torture";}
	public String displayText(){return "(being tortured)";}
	public int maxRange(){return 1;}
	public int quality(){return MALICIOUS;};
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ILLUSION;}

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
		    for(int s=0;s<Sessions.size();s++)
		        Sessions.elementAt(s).println(text());
		    setMiscText("");
		    return;
        }
        int roll=Dice.roll(1,16,0);
        boolean someoneelse=false;
        for(int i=0;i<mob.location().numInhabitants();i++)
        {
            MOB M=mob.location().fetchInhabitant(i);
            if((M!=null)&&(!M.isMonster())&&(M!=mob))
                someoneelse=true;
        }
        if(!someoneelse)
            roll=Dice.roll(1,10,0);
        else
        switch(roll)
        {
    	case 11:
			if(mob.getLiegeID().length()>0)
				mob.location().show(mob,null,CMMsg.MSG_SPEAK,
				"<S-NAME> admits that "+mob.getLiegeID()+" is <S-HIS-HER> leige.");
			else
	            roll=Dice.roll(1,10,0);
			break;
	    case 12:
	    {
			if(mob.getClanID().length()==0)
			    roll=Dice.roll(1,10,0);
			else
			{
		        Clan C=Clans.getClan(mob.getClanID());
				if(C==null)
				    roll=Dice.roll(1,10,0);
				else
				{
				    DVector V=C.getMemberList();
				    String name=(String)V.elementAt(Dice.roll(1,V.size(),-1),1);
				    if(name.equals(mob.Name()))
				        roll=Dice.roll(1,10,0);
				    else
						mob.location().show(mob,null,CMMsg.MSG_SPEAK,
						"<S-NAME> mutters that "+name+" is a part of his clan, called "+mob.getClanID()+".");
				}
			}
	        break;
	    }
        case 13:
			if(mob.getClanID().length()==0)
			    roll=Dice.roll(1,10,0);
			else
			{
		        Clan C=Clans.getClan(mob.getClanID());
				if(C==null)
				    roll=Dice.roll(1,10,0);
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
           try{C.execute(mob,Util.makeVector(str));}catch(Exception e){}
			mob.location().show(mob,null,CMMsg.MSG_SPEAK,
			"<S-NAME> says OK! I am affected by:\n\r"+str.toString());
			break;
        }
        case 16:
			if(mob.numAbilities()<1)
			    roll=Dice.roll(1,10,0);
			else
			{
	           Ability A=mob.fetchAbility(Dice.roll(1,mob.numAbilities(),-1));
				mob.location().show(mob,null,CMMsg.MSG_SPEAK,
				"<S-NAME> admit(s) that <S-HE-SHE> knows "+A.name()+" at "+A.profficiency()+"%.");
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


		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> whisper(s) a torturous spell to <T-NAMESELF>.^?");
			FullMsg msg2=new FullMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_MIND|(auto?CMMsg.MASK_GENERAL:0),null);
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

