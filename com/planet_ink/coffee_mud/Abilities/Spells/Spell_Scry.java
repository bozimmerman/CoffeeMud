package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class Spell_Scry extends Spell
{
	public String ID() { return "Spell_Scry"; }
	public String name(){return "Scry";}
	public String displayText(){return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_DIVINATION;}
	public static final DVector scries=new DVector(2);

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked()) scries.removeElement(mob);
		if((canBeUninvoked())&&(invoker!=null))
			invoker.tell("Your knowledge of '"+mob.name()+"' fades.");
		super.unInvoke();

	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected instanceof MOB)
		&&(msg.amISource((MOB)affected))
		&&(msg.sourceMinor()==CMMsg.TYP_EXAMINESOMETHING)
		&&(invoker!=null)
		&&(msg.target()!=null)
		&&((invoker.location()!=((MOB)affected).location())||(!(msg.target() instanceof Room))))
		{
			FullMsg newAffect=new FullMsg(invoker,msg.target(),CMMsg.TYP_EXAMINESOMETHING,null);
			msg.target().executeMsg(msg.target(),newAffect);
		}
		else
		if((affected instanceof MOB)
		&&(invoker!=null)
		&&(msg.source() != invoker)
		&&(invoker.location()!=((MOB)affected).location())
		&&(msg.othersCode()!=CMMsg.NO_EFFECT)
		&&(msg.othersMessage()!=null))
			invoker.executeMsg(invoker,msg);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if((auto||mob.isMonster())&&((commands.size()<1)||(((String)commands.firstElement()).equals(mob.name()))))
		{
			commands.clear();
			MOB M=null;
			int tries=0;
			while(((++tries)<100)&&(M==null))
			{
				Room R=CMMap.getRandomRoom();
				if(R.numInhabitants()>0)
					M=R.fetchInhabitant(Dice.roll(1,R.numInhabitants(),-1));
				if(M.name().equals(mob.name()))
					M=null;
			}
			if(M!=null)
				commands.addElement(M.Name());
		}
		if(commands.size()<1)
		{
			StringBuffer scryList=new StringBuffer("");
			for(int e=0;e<scries.size();e++)
				if(scries.elementAt(e,2)==mob)
					scryList.append(((e>0)?", ":"")+((MOB)scries.elementAt(e,1)).name());
			if(scryList.length()>0)
				mob.tell("Cast on or revoke from whom?  You currently have "+name()+" on the following: "+scryList.toString()+".");
			else
				mob.tell("Cast on whom?");
			return false;
		}
		String mobName=Util.combine(commands,0).trim().toUpperCase();
		MOB target=null;
		if(givenTarget instanceof MOB)
			target=(MOB)givenTarget;
		if(target!=null)
			target=mob.location().fetchInhabitant(mobName);
		if(target==null)
		{
			for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
			{
				Room room=(Room)r.nextElement();
				if(Sense.canAccess(mob,room))
				{
					MOB mob2=room.fetchInhabitant(mobName);
					if(mob2!=null){ target=mob2; break;}
				}
			}
		}
		if(target instanceof Deity) target=null;
		Room newRoom=mob.location();
		if((target!=null)&&(target.amActive())&&(!target.amDead()))
			newRoom=target.location();
		else
		{
			mob.tell("You can't seem to focus on '"+mobName+"'.");
			return false;
		}

		Ability A=target.fetchEffect(ID());
		if((A!=null)&&(A.invoker()==mob))
		{
			A.unInvoke();
			return true;
		}
		else
		if((A!=null)||(scries.contains(target)))
		{
			mob.tell("You can't seem to focus on '"+mobName+"'.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> invoke(s) the name of '"+mobName+"'.^?");
			FullMsg msg2=new FullMsg(mob,target,this,affectType(auto),null);
			if((mob.location().okMessage(mob,msg))&&((newRoom==mob.location())||(newRoom.okMessage(mob,msg2))))
			{
				mob.location().send(mob,msg);
				if(newRoom!=mob.location()) newRoom.send(target,msg2);
				scries.addElement(target,mob);
				beneficialAffect(mob,target,asLevel,0);
			}

		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to invoke scrying, but fizzle(s) the spell.");


		// return whether it worked
		return success;
	}
}
