package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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

public class Fighter_SmokeSignals extends StdAbility
{
	public String ID() { return "Fighter_SmokeSignals"; }
	public String name(){ return "Smoke Signals";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int classificationCode(){return Ability.SKILL;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public int usageType(){return USAGE_MOVEMENT;}
	private static final String[] triggerStrings = {"SMOKESIGNALS","SMOKESIGNAL"};
	public int quality(){return Ability.INDIFFERENT;}
	public String[] triggerStrings(){return triggerStrings;}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if((affected instanceof MOB)
		&&(!msg.amISource((MOB)affected))
		&&(msg.tool() instanceof Ability)
		&&(msg.tool().ID().equals("Fighter_SmokeSignals"))
		&&(msg.sourceCode()==CMMsg.NO_EFFECT)
		&&(msg.targetCode()==CMMsg.NO_EFFECT)
		&&(msg.targetMessage()!=null)
		&&(msg.othersMessage()!=null))
			msg.addTrailerMsg(new FullMsg((MOB)affected,null,null,CMMsg.MSG_OK_VISUAL,"The smoke signals seem to say '"+msg.targetMessage()+"'.",CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
		super.executeMsg(myHost,msg);
	}

	public Item getRequiredFire(MOB mob)
	{
		Item fire=null;
		for(int i=0;i<mob.location().numItems();i++)
		{
			Item I2=mob.location().fetchItem(i);
			if((I2!=null)&&(I2.container()==null)&&(Sense.isOnFire(I2)))
			{
				fire=I2;
				break;
			}
		}
		if((fire==null)||(!mob.location().isContent(fire)))
		{
			mob.tell("A fire will need to be built first.");
			return null;
		}
		return fire;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if((!auto)&&(mob.isInCombat()))
		{
			mob.tell("Not while you are fighting!");
			return false;
		}

		if(getRequiredFire(mob)==null) return false;
		Room R=mob.location();
		int weather=R.getArea().getClimateObj().weatherType(R);

		if(((R.domainType()&Room.INDOORS)==Room.INDOORS)
		||(R.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER))
		{
			mob.tell("You can't signal anyone from here.");
			return false;
		}
		else
		if((weather==Climate.WEATHER_BLIZZARD)
		||(weather==Climate.WEATHER_DUSTSTORM)
		||(weather==Climate.WEATHER_HAIL)
		||(weather==Climate.WEATHER_RAIN)
		||(weather==Climate.WEATHER_SLEET)
		||(weather==Climate.WEATHER_SNOW)
		||(weather==Climate.WEATHER_THUNDERSTORM))
		{
			mob.tell("You won't be able to get a signal up in these weather conditions.");
			return false;
		}


		if(commands.size()==0)
		{
			mob.tell("You need to specify the message to send up in the smoke signals.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,CMMsg.MSG_NOISYMOVEMENT,auto?"<T-NAME> begin(s) smoking uncontrollably!":"<S-NAME> puff(s) up a mighty series of smoke signals!");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				String str=Util.combine(commands,0);
				FullMsg msg2=new FullMsg(mob,null,this,CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,str,CMMsg.MSG_OK_VISUAL,"You see some smoke signals in the distance.");
				for(Enumeration e=mob.location().getArea().getMap();e.hasMoreElements();)
				{
					R=(Room)e.nextElement();
					weather=R.getArea().getClimateObj().weatherType(R);
					if((R!=mob.location())
					&&((R.domainType()&Room.INDOORS)==0)
					&&(R.domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)
					&&(weather!=Climate.WEATHER_BLIZZARD)
					&&(weather!=Climate.WEATHER_DUSTSTORM)
					&&(weather!=Climate.WEATHER_HAIL)
					&&(weather!=Climate.WEATHER_RAIN)
					&&(weather!=Climate.WEATHER_SLEET)
					&&(weather!=Climate.WEATHER_SNOW)
					&&(weather!=Climate.WEATHER_THUNDERSTORM)
					&&(R.okMessage(mob,msg2)))
					   R.sendOthers(msg.source(),msg2);
				}
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to send a smoke signal, but goof(s) it up.");
		return success;
	}
}