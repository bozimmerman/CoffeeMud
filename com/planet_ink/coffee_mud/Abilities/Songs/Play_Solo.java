package com.planet_ink.coffee_mud.Abilities.Songs;

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
public class Play_Solo extends Play
{
	public String ID() { return "Play_Solo"; }
	public String name(){ return "Solo";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected boolean persistantSong(){return false;}
	protected boolean skipStandardSongTick(){return true;}
	protected String songOf(){return "a "+name();}

	public boolean okMessage(Environmental E, CMMsg msg)
	{
		if(!super.okMessage(E,msg)) return false;
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB myChar=(MOB)affected;
			if(!msg.amISource(myChar)
			&&(msg.tool()!=null)
			&&(!msg.tool().ID().equals(ID()))
			&&(msg.tool() instanceof Ability)
			&&(((((Ability)msg.tool()).classificationCode()&Ability.ALL_CODES)==Ability.SONG)))
			{
				MOB otherBard=msg.source();
				if(((otherBard.envStats().level()+Dice.roll(1,30,0))>(myChar.envStats().level()+Dice.roll(1,20,0)))
				&&(otherBard.location()!=null))
				{
					if(otherBard.location().show(otherBard,myChar,null,CMMsg.MSG_OK_ACTION,"<S-NAME> upstage(s) <T-NAMESELF>, stopping <T-HIS-HER> solo!"))
						unplay(myChar,null,false);
				}
				else
				if(otherBard.location()!=null)
				{
					otherBard.tell("You can't seem to upstage "+myChar.name()+"'s solo.");
					if(!invoker().curState().adjMana(-10,invoker().maxState()))
						unplay(myChar,null,false);
					return false;
				}
			}
		}
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		unplay(mob,mob,true);
		if(success)
		{
			String str=auto?"^S"+songOf()+" begins to play!^?":"^S<S-NAME> begin(s) to play "+songOf()+" on "+instrumentName()+".^?";
			if((!auto)&&(mob.fetchEffect(this.ID())!=null))
				str="^S<S-NAME> start(s) playing "+songOf()+" on "+instrumentName()+" again.^?";

			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),str);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				Play newOne=(Play)this.copyOf();

				Vector songsToCancel=new Vector();
				for(int i=0;i<mob.location().numInhabitants();i++)
				{
					MOB M=mob.location().fetchInhabitant(i);
					if(M!=null)
					for(int a=0;a<M.numEffects();a++)
					{
						Ability A=M.fetchEffect(a);
						if((A!=null)
						&&(A.invoker()!=mob)
						&&((A.classificationCode()&Ability.ALL_CODES)==Ability.SONG))
							songsToCancel.addElement(A);
					}
				}
				int reqMana=songsToCancel.size()*10;
				if(mob.curState().getMana()<reqMana)
				{
					mob.tell("You needed "+reqMana+" mana to play this solo!");
					return false;
				}
				mob.curState().adjMana(-reqMana,mob.maxState());
				for(int i=0;i<songsToCancel.size();i++)
				{
					Ability A=(Ability)songsToCancel.elementAt(i);
					if((A.affecting()!=null)
					&&(A.affecting() instanceof MOB))
					{
						MOB M=(MOB)A.affecting();
						if(A instanceof Song) ((Song)A).unsing(M,null,false);
						else
						if(A instanceof Dance) ((Dance)A).undance(M,null,false);
						else
						if(A instanceof Play) ((Play)A).unplay(M,null,false);
						else
							A.unInvoke();
					}
					else
						A.unInvoke();
				}
				mob.addEffect(newOne);
				mob.location().recoverRoomStats();
			}
		}
		else
			mob.location().show(mob,null,CMMsg.MSG_NOISE,"<S-NAME> hit(s) a foul note.");

		return success;
	}
}
