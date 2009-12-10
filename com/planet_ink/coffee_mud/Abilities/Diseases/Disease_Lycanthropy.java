package com.planet_ink.coffee_mud.Abilities.Diseases;
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
public class Disease_Lycanthropy extends Disease
{
	public String ID() { return "Disease_Lycanthropy"; }
	public String name(){ return "Lycanthropy";}
	public String displayText(){ return "(Lycanthropy)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	public boolean putInCommandlist(){return false;}

	protected int DISEASE_TICKS(){return 9999999;}
	protected int DISEASE_DELAY(){return 50;}
	protected String DISEASE_DONE(){return "Your lycanthropy is cured.";}
	protected String DISEASE_START(){return "^G<S-NAME> feel(s) different.^?";}
	protected String DISEASE_AFFECT(){return "";}
	protected boolean DISEASE_STD(){return false;}
	public int difficultyLevel(){return 8;}
	
	protected boolean changed=false;
	public int abilityCode(){return DiseaseAffect.SPREAD_CONSUMPTION|DiseaseAffect.SPREAD_DAMAGE;}
	protected Vector deathTrail=null;
	protected Race theRace=null;
	protected Race lycanRace(){
		if(!changed) return null;
		if(theRace==null) theRace=CMClass.getRace("WereWolf");
		return theRace;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(!(affected instanceof MOB)) return;
		if(lycanRace()!=null)
		{
			if(affected.name().indexOf(" ")>0)
				affectableStats.setName("a "+lycanRace().name()+" called "+affected.name());
			else
				affectableStats.setName(affected.name()+" the "+lycanRace().name());
			lycanRace().setHeightWeight(affectableStats,'M');
		}
	}
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(lycanRace()!=null)
		{
			affectableStats.setMyRace(lycanRace());
			if(affected.baseCharStats().getStat(CharStats.STAT_AGE)>0)
				affectableStats.setStat(CharStats.STAT_AGE,lycanRace().getAgingChart()[affected.baseCharStats().ageCategory()]);
		}
	}

	public MOB victimHere(Room room, MOB mob)
	{
		if(room==null) return null;
		if(mob==null) return null;
		for(int i=0;i<room.numInhabitants();i++)
		{
			MOB M=room.fetchInhabitant(i);
			if((M!=null)
			&&(M!=mob)
			&&(!CMLib.flags().isEvil(M))
			&&(mob.mayIFight(M))
			&&(M.envStats().level()<(mob.envStats().level()+5)))
				return M;
		}
		return null;
	}

	protected boolean findVictim(MOB mob, Room room, Vector rooms, int depth)
	{
		if(depth>5)
			return false;
		if(victimHere(room,mob)!=null)
		{
			rooms.addElement(room);
			return true;
		}
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			Room R=room.getRoomInDir(d);
			Exit E=room.getExitInDir(d);
			if((R!=null)&&(E!=null)&&(E.isOpen()))
			{
				if(findVictim(mob,R,rooms,depth+1))
				{
					rooms.addElement(R);
					return true;
				}
			}
		}
		return false;
	}

	public void tickLycanthropically(MOB mob)
	{
		if(mob==null) return;
		if(mob.location()==null) return;
		if(mob.isInCombat()) return;

		if((CMLib.dice().rollPercentage()<15)
		&&((mob.location().domainType()&Room.INDOORS)>0))
			mob.location().show(mob,null,CMMsg.MSG_NOISE,"<S-NAME> howl(s) at the moon! ARROOOOOOOO!!!!");
		// time to tick lycanthropically
		MOB M=victimHere(mob.location(),mob);
		if(M!=null)
		{
			deathTrail=null;
			CMLib.combat().postAttack(mob,M,mob.fetchWieldedItem());
			return;
		}
		if((deathTrail!=null)&&(!deathTrail.contains(mob.location())))
		   deathTrail=null;
		if(deathTrail==null)
		{
			Vector rooms=new Vector();
			if((findVictim(mob,mob.location(),rooms,0))&&(rooms.size()>0))
			{
				TrackingLibrary.TrackingFlags flags;
				flags = new TrackingLibrary.TrackingFlags()
						.add(TrackingLibrary.TrackingFlag.OPENONLY)
						.add(TrackingLibrary.TrackingFlag.AREAONLY)
						.add(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS)
						.add(TrackingLibrary.TrackingFlag.NOAIR)
						.add(TrackingLibrary.TrackingFlag.NOWATER);
				deathTrail=CMLib.tracking().findBastardTheBestWay(mob.location(),rooms,flags,50);
				if(deathTrail!=null)
					deathTrail.addElement(mob.location());
			}
		}
		if(deathTrail!=null)
		{
			int nextDirection=CMLib.tracking().trackNextDirectionFromHere(deathTrail,mob.location(),true);
			if((nextDirection==999)
			||(nextDirection==-1))
				deathTrail=null;
			else
			if(nextDirection>=0)
			{
				Room nextRoom=mob.location().getRoomInDir(nextDirection);
				if((nextRoom!=null)
				&&((nextRoom.getArea()==mob.location().getArea()))||(!mob.isMonster()))
				{
					if(!CMLib.tracking().move(mob,nextDirection,false,false))
						deathTrail=null;
					else
					if(CMLib.dice().rollPercentage()<15)
						mob.location().show(mob,null,CMMsg.MSG_NOISE,"<S-NAME> sniff(s) at the air.");

				}
				else
					deathTrail=null;
			}
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))	return false;
		if(affected==null) return false;
		if(!(affected instanceof MOB)) return true;

		MOB mob=(MOB)affected;
		if(mob.amDead()) return true;

		if(!changed)
		{
			if(mob.location()==null) return true;
			Area A=mob.location().getArea();
			if(((A.getTimeObj().getTODCode()==TimeClock.TIME_DUSK)||(A.getTimeObj().getTODCode()==TimeClock.TIME_NIGHT))
			&&(A.getTimeObj().getMoonPhase()==4))
			{
				changed=true;
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> turn(s) into a "+lycanRace().name()+"!");
				mob.recoverCharStats();
				mob.recoverEnvStats();
				mob.recoverMaxState();
				mob.location().recoverRoomStats();
			}
		}
		else
		{
			if(mob.location()==null) return true;
			Area A=mob.location().getArea();
			if(((A.getTimeObj().getTODCode()!=TimeClock.TIME_DUSK)&&(A.getTimeObj().getTODCode()!=TimeClock.TIME_NIGHT))
			||(A.getTimeObj().getMoonPhase()!=4))
			{
				changed=false;
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> revert(s) to normal.");
				mob.recoverCharStats();
				mob.recoverEnvStats();
				mob.recoverMaxState();
				mob.location().recoverRoomStats();
				return true;
			}
			tickLycanthropically(mob);
		}
		return true;
	}
}
