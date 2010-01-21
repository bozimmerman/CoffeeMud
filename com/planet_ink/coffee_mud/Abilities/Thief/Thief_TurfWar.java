package com.planet_ink.coffee_mud.Abilities.Thief;
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
public class Thief_TurfWar extends ThiefSkill
{
	public String ID() { return "Thief_TurfWar"; }
	public String name(){ return "Turf War";}
	public String displayText(){return "(Turf War)";}
	protected int canAffectCode(){return CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	private static final String[] triggerStrings = {"TURFWAR"};
	public String[] triggerStrings(){return triggerStrings;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
    public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_STREETSMARTS;}
    public static Ability sparringRoomA=null;
    protected MOB defender=null;
    protected long defenderPKILLMask=0;
    protected long timeToNextCast = 0;
	public int getTicksBetweenCasts() { return CMProps.getIntVar(CMProps.SYSTEMI_TICKSPERMUDMONTH);}
	public long getTimeOfNextCast(){ return timeToNextCast; }
	public void setTimeOfNextCast(long absoluteTime) { timeToNextCast=absoluteTime;}

    public static synchronized Ability getSparringRoom()
    {
		if(sparringRoomA==null)
		{
			sparringRoomA=CMClass.getAbility("Prop_SparringRoom");
			if(sparringRoomA==null)
			{
				Log.errOut("Thief_TurfWar","Unable to load ability: Prop_SparringRoom");
				return null;
			}
		}
		return sparringRoomA;
    }
    
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if(!super.okMessage(host,msg))
			return false;
		Ability A=getSparringRoom();
		if(A==null) return true;
		if(!A.okMessage(host, msg))
			return false;
		return true;
	}

	public boolean isADefender(Room R, MOB M)
	{
		if(R==null) return false;
        Ability A=R.fetchEffect("Thief_TagTurf");
		if(A==null) return false;
        return ((A.text().equals(M.Name())
				||((M.getClanID().length()>0)&&(M.getClanID().equals(A.text()))&&(M.getClanRole()>0))));
	}
	
	public void executeMsg(Environmental host, CMMsg msg)
	{
		Ability A=getSparringRoom();
		if(A!=null) A.executeMsg(host, msg);
		super.executeMsg(host,msg);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if(!(affected instanceof Room))
			return false;
		Room R=(Room)affected;
		if(R==null) return false;
		MOB attacker=invoker();
		if(attacker==null)
			return false;
		if(attacker.location()!=R)
		{
			// failure in offensive
			unInvoke();
			return false;
		}
		if(defender==null)
		{
			for(int m=0;m<R.numInhabitants();m++)
			{
				MOB M=R.fetchInhabitant(m);
				if((M!=null)&&(!M.isMonster())&&(M!=attacker)&&(isADefender(R,M)))
				{
					defender=M;
					R.showHappens(CMMsg.MSG_OK_ACTION,M.name()+" arrives to defend this turf! Let the war begin!");
					defenderPKILLMask=defender.getBitmap() & MOB.ATT_PLAYERKILL;
					defender.setBitmap(defender.getBitmap() | MOB.ATT_PLAYERKILL);
					attacker.setVictim(defender);
					defender.setVictim(attacker);
					// this is a safe fight, so nothing matters but the blows.
				}
			}
		}
		else
		if(defender!=null)
		{
			if(defender.location()!=R)
			{
				unInvoke();
				return false;
				// failure in defense!
			}
			if(attacker.isInCombat() && defender.isInCombat() && (getTickDownRemaining() < 10))
				setTickDownRemaining(10);
		}
		return true;
	}
	
	public void unInvoke()
	{
		MOB attacker=invoker();
		if(attacker!=null)
			attacker.makePeace();
		if(defender!=null)
		{
			defender.makePeace();
			if(defenderPKILLMask==0)
				defender.setBitmap(CMath.unsetb(defender.getBitmap(),MOB.ATT_PLAYERKILL));
		}
		
		if(affected instanceof Room)
		{
			Room R=(Room)affected;
			if((attacker!=null)&&(attacker.location()==R)
			&&((defender==null)||(defender.location()!=R)))
			{
				R.showHappens(CMMsg.MSG_OK_ACTION,attacker.Name()+" has won the turf war!");
				Ability A=R.fetchEffect("Thief_TagTurf");
				if(A!=null) A.unInvoke();
			}
			else
			if((attacker!=null)&&(attacker.location()!=R)
			&&((defender!=null)&&defender.location()==R))
				R.showHappens(CMMsg.MSG_OK_ACTION,defender.Name()+" has won the turf war!");
		}
		super.unInvoke();
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		Room target=mob.location();
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof Room))
			target=(Room)givenTarget;
		Ability A=target.fetchEffect(ID());
		if(A!=null)
		{
			mob.tell("A turf war is already underway here.");
			return false;
		}
		
		if(!CMath.bset(mob.getBitmap(),MOB.ATT_PLAYERKILL))
		{
			mob.tell("You must turn on your playerkill flag first.");
			return false;
		}
		
        A=target.fetchEffect("Thief_TagTurf");
        Clan turfC=null;
        MOB turfM=null;
		if(A!=null)
		{
            if((A.text().equals(mob.Name())
        		||((mob.getClanID().length()>0)&&(mob.getClanID().equals(A.text()))&&(mob.getClanRole()>0))))
            {
                mob.tell("You can't declare war on your own turf!");
                return true;
            }
            turfC=CMLib.clans().getClan(A.text());
            if(turfC==null)
            	turfM=CMLib.players().getLoadPlayer(A.text());
            if(turfM==null)
            {
            	A.unInvoke();
                mob.tell("This turf is untagged.");
                return true;
            }
		}
		else
		{
			mob.tell("This turf is not tagged by anyone.");
			return false;
		}

		Room R=(Room)target;
		boolean success=proficiencyCheck(mob,0,auto);

		CMMsg msg=CMClass.getMsg(mob,target,this,auto?CMMsg.MASK_ALWAYS:CMMsg.MSG_DELICATE_HANDS_ACT,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,auto?"":"<S-NAME> declare(s) a turf war!");
		if(!success)
		{
			return beneficialVisualFizzle(mob,target,auto?"":"<S-NAME> attempt(s) to declare a turf war, but can't get started.");
		}
		else
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,target,asLevel,(CMProps.getIntVar(CMProps.SYSTEMI_TICKSPERMUDMONTH)));
			if(target.fetchEffect(ID())!=null)
			{
				for(Enumeration s=CMLib.sessions().sessions();s.hasMoreElements();)
				{
					Session S=(Session)s.nextElement();
					if((S.mob()!=null)&&(S.mob()!=mob)&&(isADefender(R,S.mob())))
						S.mob().tell(mob.displayName(mob)+" has declared a turf war at '"+R.displayText()+"'.  You must immediately go and defend it to keep your tag.");
				}
				setTimeOfNextCast(mob);
			}
		}
		return success;
	}
}
