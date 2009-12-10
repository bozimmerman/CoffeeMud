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
public class Thief_Assassinate extends ThiefSkill
{
	public String ID() { return "Thief_Assassinate"; }
	public String name(){ return "Assassinate";}
	protected String displayText="(Tracking)";
	public String displayText(){ return displayText;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_OK_OTHERS;}
	private static final String[] triggerStrings = {"ASSASSINATE"};
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
	public String[] triggerStrings(){return triggerStrings;}
	public long flags(){return Ability.FLAG_TRACKING;}
    public int classificationCode() {   return Ability.ACODE_SKILL|Ability.DOMAIN_DIRTYFIGHTING; }
	protected Vector theTrail=null;
	public int nextDirection=-2;
	protected MOB tracking=null;

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
			if((mob.isInCombat())
            &&(mob.isMonster())
            &&(!CMLib.flags().isMobile(mob)))
                return true;
            
			Room room=mob.location();
			if(room==null) return false;
			if(room.isInhabitant(tracking))
			{
				if(CMLib.flags().isHidden(mob))
				{
					Ability A=mob.fetchAbility("Thief_BackStab");
					if(A!=null)
					{
						A.setAbilityCode(5);
						A.invoke(mob,tracking,false,0);
						A.setAbilityCode(0);
					}
				}
				else
					CMLib.combat().postAttack(mob,tracking,mob.fetchWieldedItem());
                if((!mob.isMonster())||(CMLib.flags().isMobile(mob)))
                    return false;
				return true;
			}

			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			{
				Room nextRoom=room.getRoomInDir(d);
				Exit nextExit=room.getExitInDir(d);
				if((nextRoom!=null)
				   &&(nextExit!=null)
				   &&(nextExit.isOpen())
				   &&(nextRoom.isInhabitant(tracking)))
				{
					nextDirection=d; break;
				}
			}

			if(nextDirection==999)
			{
				mob.tell("The trail seems to pause here.");
				nextDirection=-2;
				unInvoke();
			}
			else
			if(nextDirection==-1)
			{
				mob.tell("The trail dries up here.");
				nextDirection=-999;
				unInvoke();
			}
			else
			if(nextDirection>=0)
			{
				mob.tell("The trail seems to continue "+Directions.getDirectionName(nextDirection)+".");
				if(mob.isMonster())
				{
					Room nextRoom=room.getRoomInDir(nextDirection);
					if((nextRoom!=null)&&(nextRoom.getArea()==room.getArea()))
					{
						if(!nextRoom.isInhabitant(tracking))
						{
							Ability A=mob.fetchAbility("Thief_Sneak");
							if(A!=null)
							{
								int dir=nextDirection;
								nextDirection=-2;
								Vector V=new Vector();
								V.addElement(Directions.getDirectionName(dir));
								A.invoke(mob,V,null,false,0);
							}
							else
							{
								int dir=nextDirection;
								nextDirection=-2;
								CMLib.tracking().move(mob,dir,false,false);
							}
						}
						else
						{
							int dir=nextDirection;
							nextDirection=-2;
							CMLib.tracking().move(mob,dir,false,false);
						}
					}
					else
                    {
						unInvoke();
                    }
				}
				else
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
			nextDirection=CMLib.tracking().trackNextDirectionFromHere(theTrail,mob.location(),true);
	}

    public void unInvoke()
    {
        MOB mob=(affected instanceof MOB)?(MOB)affected:null;
        super.unInvoke();
        if((mob!=null)
        &&(!mob.amDead())
        &&(mob.isMonster())
        &&(!CMLib.flags().isMobile(mob))
        &&(mob.getStartRoom()!=null)
        &&(mob.location()!=mob.getStartRoom()))
            CMLib.tracking().wanderAway(mob,false,true);
    }
    
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!CMLib.flags().aliveAwakeMobileUnbound(mob,false))
			return false;

		if((!auto)&&(!CMLib.flags().canBeSeenBy(mob.location(),mob)))
		{
			mob.tell("You can't see anything to track!");
			return false;
		}

		Vector V=CMLib.flags().flaggedAffects(mob,Ability.FLAG_TRACKING);
		for(int v=0;v<V.size();v++)	((Ability)V.elementAt(v)).unInvoke();
		if(V.size()>0)
		{
			mob.tell("You stop tracking.");
			if(commands.size()==0) return true;
		}

		theTrail=null;
		nextDirection=-2;

		tracking=null;
		String mobName="";
		if((!mob.isMonster())&&(mob.fetchEffect("Thief_Mark")!=null))
		{
			Thief_Mark A=(Thief_Mark)mob.fetchEffect("Thief_Mark");
			if(A!=null) tracking=A.mark;
			if(tracking==null)
			{
				mob.tell("You'll need to Mark someone first.");
				return false;
			}
		}
		else
		{
			if(givenTarget!=null)
				mobName=givenTarget.name();
			else
				mobName=CMParms.combine(commands,0);
            if(givenTarget instanceof MOB)
                tracking=(MOB)givenTarget;
			if(mobName.length()==0)
			{
				mob.tell("Assassinate whom?");
				return false;
			}
			MOB M=((givenTarget instanceof MOB)&&(((MOB)givenTarget).location()==mob.location()))?
                    (MOB)givenTarget:
                    mob.location().fetchInhabitant(mobName);
			if(M!=null)
			{
				CMLib.combat().postAttack(mob,M,mob.fetchWieldedItem());
				return false;
			}
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;


		boolean success=proficiencyCheck(mob,0,auto);

		Vector rooms=new Vector();
		if(tracking!=null)
		{
			Room R=tracking.location();
			if((R!=null)&&(R.isInhabitant(tracking))&&(CMLib.flags().canAccess(mob,R)))
				rooms.addElement(R);
		}
		else
		if(mobName.length()>0)
		{
		    try
		    {
				TrackingLibrary.TrackingFlags flags=new TrackingLibrary.TrackingFlags();
				if(givenTarget!=null&&auto&&mob.isMonster())
					flags.add(TrackingLibrary.TrackingFlag.AREAONLY);
				flags.add(TrackingLibrary.TrackingFlag.OPENONLY)
					 .add(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS)
					 .add(TrackingLibrary.TrackingFlag.NOAIR)
					 .add(TrackingLibrary.TrackingFlag.NOWATER);
				Vector checkSet=CMLib.tracking().getRadiantRooms(mob.location(),flags,50+(2*getXLEVELLevel(mob)));
				for(Enumeration r=checkSet.elements();r.hasMoreElements();)
				{
					Room R=CMLib.map().getRoom((Room)r.nextElement());
					if(R.fetchInhabitant(mobName)!=null)
						rooms.addElement(R);
				}
		    }catch(NoSuchElementException nse){}
		}

		TrackingLibrary.TrackingFlags flags=new TrackingLibrary.TrackingFlags();
		flags.add(TrackingLibrary.TrackingFlag.OPENONLY)
			 .add(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS)
			 .add(TrackingLibrary.TrackingFlag.NOAIR)
			 .add(TrackingLibrary.TrackingFlag.NOWATER);
		if(givenTarget!=null&&auto&&mob.isMonster())
			flags.add(TrackingLibrary.TrackingFlag.AREAONLY);
		if(rooms.size()>0)
			theTrail=CMLib.tracking().findBastardTheBestWay(mob.location(),rooms,flags,50+(2*getXLEVELLevel(mob)));

		if((tracking==null)&&(theTrail!=null)&&(theTrail.size()>0))
			tracking=((Room)theTrail.firstElement()).fetchInhabitant(mobName);

		if((success)&&(theTrail!=null)&&(tracking!=null))
		{
			theTrail.addElement(mob.location());

			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,tracking,this,CMMsg.MSG_THIEF_ACT,mob.isMonster()?null:"<S-NAME> begin(s) to track <T-NAMESELF> for assassination.",CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null);
			if((mob.location().okMessage(mob,msg))
            &&(tracking.okMessage(tracking,msg)))
			{
				mob.location().send(mob,msg);
				tracking.executeMsg(tracking,msg);
				invoker=mob;
				displayText="(tracking "+tracking.name()+")";
				Thief_Assassinate newOne=(Thief_Assassinate)this.copyOf();
				if(mob.fetchEffect(newOne.ID())==null)
					mob.addEffect(newOne);
				mob.recoverEnvStats();
				newOne.nextDirection=CMLib.tracking().trackNextDirectionFromHere(theTrail,mob.location(),true);
			}
		}
		else
			return beneficialVisualFizzle(mob,tracking,"<S-NAME> attempt(s) to track <T-NAMESELF> for assassination, but fail(s).");


		// return whether it worked
		return success;
	}
}
