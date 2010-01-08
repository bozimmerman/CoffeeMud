package com.planet_ink.coffee_mud.Abilities.Druid;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
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
public class Chant_FindMate extends Chant
{
	public String ID() { return "Chant_FindMate"; }
	public String name(){ return "Find Mate";}
	protected String displayText="(Tracking a mate)";
	public String displayText(){ return displayText;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
    public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_BREEDING;}
	public int abstractQuality(){return Ability.QUALITY_OK_OTHERS;}
	public long flags(){return Ability.FLAG_TRACKING;}

	protected Vector theTrail=null;
	public int nextDirection=-2;

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(tickID==Tickable.TICKID_MOB)
		{
			if((theTrail==null)
			||(affected == null)
			||(!(affected instanceof MOB)))
				return false;

			MOB mob=(MOB)affected;
			if(mob.location()!=null)
			{
				MOB mate=null;
				for(int i=0;i<mob.location().numInhabitants();i++)
				{
					MOB M=mob.location().fetchInhabitant(i);
					if(isSuitableMate(M,mob))
					{ mate=M; break;}
				}
				if(mate!=null)
				{
					mob.tell("You peer longingly at "+mate.name()+".");

					Item I=mob.fetchFirstWornItem(Wearable.WORN_WAIST);
					if(I!=null)	CMLib.commands().postRemove(mob,I,false);
					I=mob.fetchFirstWornItem(Wearable.WORN_LEGS);
					if(I!=null)	CMLib.commands().postRemove(mob,I,false);

					if((mob.fetchFirstWornItem(Wearable.WORN_WAIST)!=null)
					||(mob.fetchFirstWornItem(Wearable.WORN_LEGS)!=null))
						unInvoke();
					mob.doCommand(CMParms.parse("MATE \""+mate.name()+"$\""),Command.METAFLAG_FORCED);
					unInvoke();
				}
			}

			if(nextDirection==-999)
				return true;

			if(nextDirection==999)
			{
				mob.tell("Your yearning for a mate seems to fade.");
				nextDirection=-2;
				unInvoke();
			}
			else
			if(nextDirection==-1)
			{
				mob.tell("You no longer want to continue.");
				nextDirection=-999;
				unInvoke();
			}
			else
			if(nextDirection>=0)
			{
				mob.tell("You want to continue "+Directions.getDirectionName(nextDirection)+".");
				Room nextRoom=mob.location().getRoomInDir(nextDirection);
				if((nextRoom!=null)&&(nextRoom.getArea()==mob.location().getArea()))
				{
					int dir=nextDirection;
					nextDirection=-2;
					CMLib.tracking().move(mob,dir,false,false);
				}
				else
					unInvoke();
			}
		}
		return true;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		if((affected==null)||(!(affected instanceof MOB)))
			return;

		MOB mob=(MOB)affected;
		if((msg.amISource(mob))
		&&(msg.amITarget(mob.location()))
		&&(CMLib.flags().canBeSeenBy(mob.location(),mob))
		&&(msg.targetMinor()==CMMsg.TYP_LOOK))
			nextDirection=CMLib.tracking().trackNextDirectionFromHere(theTrail,mob.location(),true);
	}

	public boolean isSuitableMate(MOB mate, MOB forMe)
	{
		if(mate==forMe) return false;
		if((mate==null)||(forMe==null)) return false;
		if(mate.charStats().getStat(CharStats.STAT_GENDER)==forMe.charStats().getStat(CharStats.STAT_GENDER))
			return false;
		if((mate.charStats().getStat(CharStats.STAT_GENDER)!='M')
		&&(mate.charStats().getStat(CharStats.STAT_GENDER)!='F'))
			return false;
		String materace=mate.charStats().getMyRace().ID();
		String merace=mate.charStats().getMyRace().ID();
		if(((merace.equals("Human"))
		   ||(materace.equals("Human"))
		   ||(merace.equals(materace)))
		&&(mate.fetchWornItems(Wearable.WORN_LEGS|Wearable.WORN_WAIST,(short)-2048,(short)0).size()==0)
		&&(CMLib.flags().canBeSeenBy(mate,forMe)))
			return true;
		return false;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if((target.charStats().getStat(CharStats.STAT_GENDER)!='M')
		&&(target.charStats().getStat(CharStats.STAT_GENDER)!='F'))
		{
			mob.tell(target.name()+" is incapable of mating!");
			return false;
		}

		Vector V=CMLib.flags().flaggedAffects(mob,Ability.FLAG_TRACKING);
		for(int v=0;v<V.size();v++)	((Ability)V.elementAt(v)).unInvoke();
		if(V.size()>0)
		{
			target.tell("You stop tracking.");
			return true;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		TrackingLibrary.TrackingFlags flags;
		flags = new TrackingLibrary.TrackingFlags()
				.add(TrackingLibrary.TrackingFlag.OPENONLY);
		Vector rooms=new Vector();
		Vector checkSet=CMLib.tracking().getRadiantRooms(mob.location(),flags,50);
		for(Enumeration r=checkSet.elements();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			if(R!=null)
			for(int i=0;i<R.numInhabitants();i++)
			{
				MOB M=R.fetchInhabitant(i);
				if(isSuitableMate(M,target))
				{ rooms.addElement(R); break;}
			}
		}
		checkSet=null;
		//TrackingLibrary.TrackingFlags flags;
		flags = new TrackingLibrary.TrackingFlags()
				.add(TrackingLibrary.TrackingFlag.OPENONLY)
				.add(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS)
				.add(TrackingLibrary.TrackingFlag.NOAIR)
				.add(TrackingLibrary.TrackingFlag.NOWATER);
		if(rooms.size()>0)
			theTrail=CMLib.tracking().findBastardTheBestWay(mob.location(),rooms,flags,50);

		if((success)&&(theTrail!=null))
		{
			theTrail.addElement(mob.location());

			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?null:"^S<S-NAME> chant(s) to <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
				Chant_FindMate A=(Chant_FindMate)target.fetchEffect(ID());
				if(A!=null)
				{
					target.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> yearn(s) for a mate!");
					A.makeLongLasting();
					A.nextDirection=CMLib.tracking().trackNextDirectionFromHere(theTrail,mob.location(),true);
					target.recoverEnvStats();
				}
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) to <T-NAMESELF>, but nothing happen(s).");


		// return whether it worked
		return success;
	}
}
