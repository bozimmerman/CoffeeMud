package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Spell_Shelter extends Spell
{
	public String ID() { return "Spell_Shelter"; }
	public String name(){return "Shelter";}
	public String displayText(){return "(In a shelter)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Spell_Shelter();}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_CONJURATION;}

	public Room previousLocation=null;
	public Room shelter=null;

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		if(canBeUninvoked())
		{
			int i=0;
			while(i<shelter.numInhabitants())
			{
				mob=shelter.fetchInhabitant(0);
				if(mob==null) break;
				mob.tell(mob,null,"You return to your previous location.");

				FullMsg enterMsg=new FullMsg(mob,previousLocation,null,Affect.MSG_ENTER,null,Affect.MSG_ENTER,null,Affect.MSG_ENTER,"<S-NAME> appears out of nowhere!");
				shelter.delInhabitant(mob);
				mob.setLocation(previousLocation);
				previousLocation.addInhabitant(mob);
				previousLocation.send(mob,enterMsg);
				ExternalPlay.look(mob,null,true);
			}
			shelter=null;
			previousLocation=null;
		}
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		if(mob.fetchAffect(ID())!=null)
		{
			mob.fetchAffect(ID()).unInvoke();
			return false;
		}

		boolean success=profficiencyCheck(0,auto);

		FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> wave(s) <S-HIS-HER> arms, speak(s), and suddenly vanish(es)!^?");
		if(mob.location().okAffect(mob,msg))
		{
			mob.location().send(mob,msg);
			Hashtable h=ExternalPlay.properTargets(this,mob,false);
			if(h==null) return false;

			Room thisRoom=mob.location();
			previousLocation=thisRoom;
			shelter=CMClass.getLocale("MagicShelter");
			Room newRoom=shelter;
			shelter.setArea(mob.location().getArea());
			for(Enumeration f=h.elements();f.hasMoreElements();)
			{
				MOB follower=(MOB)f.nextElement();
				if((follower.isMonster())||(follower==mob))
				{
					FullMsg enterMsg=new FullMsg(follower,newRoom,null,Affect.MSG_ENTER,null,Affect.MSG_ENTER,null,Affect.MSG_ENTER,"<S-NAME> appears out of nowhere.");
					FullMsg leaveMsg=new FullMsg(follower,thisRoom,this,affectType(auto),"<S-NAME> disappear(s) into oblivion.");
					if(thisRoom.okAffect(follower,leaveMsg)&&newRoom.okAffect(follower,enterMsg))
					{
						if(follower.isInCombat())
						{
							ExternalPlay.flee(follower,"NOWHERE");
							follower.makePeace();
						}
						thisRoom.send(follower,leaveMsg);
						newRoom.bringMobHere(follower,false);
						newRoom.send(follower,enterMsg);
						follower.tell("\n\r\n\r");
						ExternalPlay.look(follower,null,true);
						if(follower==mob)
							beneficialAffect(mob,mob,999999);
					}
				}
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> wave(s) <S-HIS-HER> arms and and speak(s), but nothing happens.");

		return success;
	}
}