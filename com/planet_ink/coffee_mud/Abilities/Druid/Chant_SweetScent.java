package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_SweetScent extends Chant
{
	public String ID() { return "Chant_SweetScent"; }
	public String name(){ return "Sweet Scent";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	protected int canTargetCode(){return Ability.CAN_ITEMS;}
	public Environmental newInstance(){	return new Chant_SweetScent();}


	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if((affected!=null)&&(affected instanceof Item))
		{
			Item I=(Item)affected;
			if(I.owner() instanceof Room)
			{
				Room room=(Room)I.owner();
				Vector rooms=new Vector();
				MUDTracker.getRadiantRooms(room,rooms,true,true,false,null,10);
				for(int i=0;i<room.numInhabitants();i++)
				{
					MOB M=room.fetchInhabitant(i);
					if((M!=null)
					&&(Sense.isAnimalIntelligence(M))
					&&(Sense.canSmell(M)))
						M.tell(M,I,null,"<T-NAME> smell(s) absolutely intoxicating!");
				}
				for(int r=0;r<rooms.size();r++)
				{
					Room R=(Room)rooms.elementAt(r);
					if(R!=room)
					{
						int dir=MUDTracker.radiatesFromDir(R,rooms);
						if(dir>=0)
						{
							for(int i=0;i<R.numInhabitants();i++)
							{
								MOB M=R.fetchInhabitant(i);
								if((M!=null)
								&&(Sense.isAnimalIntelligence(M))
								&&(!M.isInCombat())
								&&((!M.isMonster())||(Sense.isMobile(M)))
								&&(Sense.canSmell(M)))
								{
									M.tell(M,null,null,"You smell something irresistable "+Directions.getInDirectionName(dir)+".");
									if(Dice.rollPercentage()>M.charStats().getSave(CharStats.SAVE_MIND))
										MUDTracker.move(M,dir,false,false);
								}
							}
						}
					}

				}
			}
		}
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(
		  (mob.location().domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
		||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_AIR)
		||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE)
		||(mob.location().domainType()==Room.DOMAIN_INDOORS_UNDERWATER)
		||(mob.location().domainType()==Room.DOMAIN_INDOORS_AIR)
		||(mob.location().domainType()==Room.DOMAIN_INDOORS_WATERSURFACE)
		   )
		{
			mob.tell("This magic will not work here.");
			return false;
		}

		Item target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_UNWORNONLY);
		if(target==null) return false;
		if(!Druid_MyPlants.isMyPlant(target,mob))
		{
			mob.tell(target.name()+" is not one of your plants!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) to the <T-NAMESELF>, but nothing happens.");

		// return whether it worked
		return success;
	}
}