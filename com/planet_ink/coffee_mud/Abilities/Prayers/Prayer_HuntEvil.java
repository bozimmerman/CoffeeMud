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
public class Prayer_HuntEvil extends Prayer
{
	public String ID() { return "Prayer_HuntEvil"; }
	public String name(){ return "Hunt Evil";}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_TRACKING;}
	public String displayText(){return "(Hunting Evil)";}
	protected String word(){return "evil";}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_NEUTRALIZATION;}
    public int abstractQuality(){ return Ability.QUALITY_OK_SELF;}

	protected Vector theTrail=null;
	public int nextDirection=-2;

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(tickID==Tickable.TICKID_MOB)
		{
			if(nextDirection==-999)
				return true;

			if((theTrail==null)
			||(affected == null)
			||(!(affected instanceof MOB)))
				return false;

			MOB mob=(MOB)affected;

			if(nextDirection==999)
			{
				mob.tell("The hunt seems to pause here.");
				nextDirection=-2;
				unInvoke();
			}
			else
			if(nextDirection==-1)
			{
				mob.tell("The hunt dries up here.");
				nextDirection=-999;
				unInvoke();
			}
			else
			if(nextDirection>=0)
			{
				mob.tell("The hunt seems to continue "+Directions.getDirectionName(nextDirection)+".");
				nextDirection=-2;
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
			nextDirection=CMLib.tracking().trackNextDirectionFromHere(theTrail,mob.location(),false);
	}

	protected MOB gameHere(Room room)
	{
		if(room==null) return null;
		for(int i=0;i<room.numInhabitants();i++)
		{
			MOB mob=room.fetchInhabitant(i);
			if(CMLib.flags().isEvil(mob))
				return mob;
		}
		return null;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(mob.fetchEffect(this.ID())!=null)
		{
			mob.tell("You are already trying to hunt "+word()+".");
			return false;
		}
		Vector V=CMLib.flags().flaggedAffects(mob,Ability.FLAG_TRACKING);
		for(int v=0;v<V.size();v++)	((Ability)V.elementAt(v)).unInvoke();

		theTrail=null;
		nextDirection=-2;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if(gameHere(mob.location())!=null)
		{
			mob.tell("Try 'look'.");
			return false;
		}

		boolean success=proficiencyCheck(mob,0,auto);

		Vector rooms=new Vector();
		TrackingLibrary.TrackingFlags flags=new TrackingLibrary.TrackingFlags();
		Vector checkSet=CMLib.tracking().getRadiantRooms(mob.location(),flags,50);
		for(Enumeration r=checkSet.elements();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			if(gameHere(R)!=null)
				rooms.addElement(R);
		}

		if(rooms.size()>0)
			theTrail=CMLib.tracking().findBastardTheBestWay(mob.location(),rooms,flags,50);

		MOB target=null;
		if((theTrail!=null)&&(theTrail.size()>0))
			target=gameHere((Room)theTrail.firstElement());

		if((success)&&(theTrail!=null)&&(target!=null))
		{
			theTrail.addElement(mob.location());
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),"^S<S-NAME> "+prayWord(mob)+" for the trail to "+word()+".^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				Prayer_HuntEvil newOne=(Prayer_HuntEvil)this.copyOf();
				if(mob.fetchEffect(newOne.ID())==null)
					mob.addEffect(newOne);
				mob.recoverEnvStats();
				newOne.nextDirection=CMLib.tracking().trackNextDirectionFromHere(newOne.theTrail,mob.location(),false);
			}
		}
		else
			return beneficialVisualFizzle(mob,null,"<S-NAME> "+prayWord(mob)+" for the trail to "+word()+", but nothing happens.");


		// return whether it worked
		return success;
	}
}
