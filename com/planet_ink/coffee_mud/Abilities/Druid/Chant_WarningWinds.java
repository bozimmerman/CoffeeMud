package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Chant_WarningWinds extends Chant
{
	public String ID() { return "Chant_WarningWinds"; }
	public String name(){ return "Warning Winds";}
	public String displayText(){return "(Warning Winds)";}
	Room lastRoom=null;
	public Environmental newInstance(){	return new Chant_WarningWinds();}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			lastRoom=null;
		super.unInvoke();
		if(canBeUninvoked())
			mob.tell("Your senses are no longer attuned to the winds.");
	}
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((tickID==Host.MOB_TICK)
		&&(affected!=null)
		&&(affected instanceof MOB)
		&&(((MOB)affected).location()!=null)
		&&(((MOB)affected).location()!=lastRoom)
		&&((((MOB)affected).location().domainType()&Room.INDOORS)==0)
		&&(((MOB)affected).location().domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER))
		{
			lastRoom=((MOB)affected).location();
			Vector V=new Vector();
			ExternalPlay.getRadiantRooms(lastRoom,V,false,2);
			StringBuffer str=new StringBuffer("");
			boolean fighting=false;
			boolean enemy=false;
			for(int r=0;r<V.size();r++)
			{
				Room R=(Room)V.elementAt(r);
				fighting=false;
				enemy=false;
				if(R!=lastRoom)
				{
					for(int i=0;i<R.numInhabitants();i++)
					{
						MOB M=(MOB)R.fetchInhabitant(i);
						if((M!=null)&&(M!=affected))
						{
							if(M.isInCombat()) fighting=true;
							for(int b=0;b<M.numBehaviors();b++)
							{
								Behavior B=M.fetchBehavior(b);
								if((B!=null)&&(B.grantsAggressivenessTo(M)))
									enemy=true;
							}
						}
						if(enemy||fighting) break;
					}
				}
				if(enemy||fighting)
				{
					int dir=ExternalPlay.radiatesFromDir(R,V);
					if(dir>=0)
					{
						String far="far ";
						for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
							if(lastRoom.getRoomInDir(d)==R) far="";
						dir=Directions.getOpDirectionCode(dir);
						if(fighting)
							((MOB)affected).tell("The winds tell of fighting "+far+Directions.getInDirectionName(dir)+".");
						else
						if(enemy)
							((MOB)affected).tell("The winds tell of enemies "+far+Directions.getInDirectionName(dir)+".");
					}
				}
			}
		}
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		
		if(target.fetchAffect(this.ID())!=null)
		{
			target.tell("You are already attuned to the winds.");
			return false;
		}

		if(((target.location().domainType()&Room.INDOORS)>0)&&(!auto))
		{
			target.tell("You must be outdoors for this chant to work.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> gain(s) a sense of the winds!":"^S<S-NAME> chant(s) for a sense of the winds!^?");
			if(mob.location().okAffect(mob,msg))
			{
				lastRoom=null;
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> chant(s) into the air, but the magic fizzles.");

		return success;
	}
}