package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Locales.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;


public class Spell_Shelter extends Spell
	implements InvocationDevotion
{

	public Room previousLocation=null;
	public Room shelter=null;

	public Spell_Shelter()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Shelter";
		displayText="(In a shelter)";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(12);

		addQualifyingClass(new Mage().ID(),12);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Shelter();
	}
	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		int i=0;
		while(i<shelter.numInhabitants())
		{
			mob=shelter.fetchInhabitant(0);
			mob.tell(mob,null,"You return to your previous location.");

			FullMsg enterMsg=new FullMsg(mob,previousLocation,null,Affect.MOVE_ENTER,null,Affect.MOVE_ENTER,null,Affect.MOVE_ENTER,"<S-NAME> appears out of nowhere!");
			shelter.delInhabitant(mob);
			mob.setLocation(previousLocation);
			previousLocation.addInhabitant(mob);
			previousLocation.send(mob,enterMsg);
			BasicSenses.look(mob,null,true);
		}
		shelter=null;
		previousLocation=null;

		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		if(!super.invoke(mob,commands))
			return false;

		if(mob.fetchAffect(this.ID())!=null)
		{
			mob.tell("You are already in the shelter.");
			return false;
		}

		boolean success=profficiencyCheck(0);

		FullMsg msg=new FullMsg(mob,null,this,Affect.SOUND_MAGIC,Affect.VISUAL_WNOISE,Affect.VISUAL_WNOISE,"<S-NAME> wave(s) <S-HIS-HER> arms, and suddenly vanish(es)!");
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			Hashtable h=Grouping.getAllFollowers(mob);
			if(h.get(mob.ID())==null)
			   h.put(mob.ID(),mob);

			Room thisRoom=mob.location();
			previousLocation=thisRoom;
			shelter=new MagicShelter();
			Room newRoom=shelter;
			for(Enumeration f=h.elements();f.hasMoreElements();)
			{
				MOB follower=(MOB)f.nextElement();
				if((follower.isMonster())||(follower==mob))
				{
					FullMsg enterMsg=new FullMsg(follower,newRoom,this,Affect.MOVE_ENTER,null,Affect.MOVE_ENTER,null,Affect.MOVE_ENTER,"<S-NAME> appears out of nowhere.");
					FullMsg leaveMsg=new FullMsg(follower,thisRoom,this,Affect.HANDS_RECALL,Affect.HANDS_RECALL,Affect.HANDS_RECALL,"<S-NAME> disappear(s) into oblivion.");
					if(thisRoom.okAffect(leaveMsg)&&newRoom.okAffect(enterMsg))
					{
						if(follower.isInCombat())
						{
							Movement.flee(follower,"NOWHERE");
							follower.makePeace();
						}
						thisRoom.delInhabitant(follower);
						thisRoom.send(follower,leaveMsg);

						follower.setLocation(newRoom);
						newRoom.addInhabitant(follower);
						newRoom.send(follower,enterMsg);
						follower.tell("\n\r\n\r\n\r");
						BasicSenses.look(follower,null,true);
						if(follower==mob)
							beneficialAffect(mob,mob,999999);
					}
				}
			}
		}
		else
			beneficialFizzle(mob,null,"<S-NAME> wave(s) <S-HIS-HER> arms, but nothing happens.");

		return success;
	}
}