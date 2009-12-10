package com.planet_ink.coffee_mud.Abilities.Songs;
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
public class Play_Reveille extends Play
{
	public String ID() { return "Play_Reveille"; }
	public String name(){ return "Reveille";}
	public int abstractQuality(){ return Ability.QUALITY_BENEFICIAL_OTHERS;}
	protected int canAffectCode(){return 0;}
	protected boolean skipStandardSongTick(){return true;}

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if(mob.isInCombat())
                return Ability.QUALITY_INDIFFERENT;
        }
        return super.castingQuality(mob,target);
    }
    
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		steadyDown=-1;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		boolean success=proficiencyCheck(mob,0,auto);
		unplay(mob,mob,true);
		if(success)
		{
			invoker=mob;
			originRoom=mob.location();
			commonRoomSet=getInvokerScopeRoomSet(null);
			String str=auto?"^S"+songOf()+" begins to play!^?":"^S<S-NAME> begin(s) to play "+songOf()+" on "+instrumentName()+".^?";
			if((!auto)&&(mob.fetchEffect(this.ID())!=null))
				str="^S<S-NAME> start(s) playing "+songOf()+" on "+instrumentName()+" again.^?";

			for(int v=0;v<commonRoomSet.size();v++)
			{
				Room R=(Room)commonRoomSet.elementAt(v);
				String msgStr=getCorrectMsgString(R,str,v);
				CMMsg msg=CMClass.getMsg(mob,null,this,somanticCastCode(mob,null,auto),msgStr);
				if(R.okMessage(mob,msg))
				{
					if(originRoom==R)
						R.send(mob,msg);
					else
						R.sendOthers(mob,msg);
					invoker=mob;
					Hashtable h=new Hashtable();
					for(int i=0;i<R.numInhabitants();i++)
					{ MOB M=R.fetchInhabitant(i); h.put(M,M);}
					
					for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
					{
						Room R3=R.getRoomInDir(d);
						if((R3!=null)&&(!commonRoomSet.contains(R3)))
							for(int i=0;i<R3.numInhabitants();i++)
							{ MOB M=R3.fetchInhabitant(i); h.put(M,M);}
					}
	
					for(Enumeration f=h.elements();f.hasMoreElements();)
					{
						MOB follower=(MOB)f.nextElement();
						Room R2=follower.location();
	
						// malicious songs must not affect the invoker!
						int affectType=CMMsg.MSG_CAST_SOMANTIC_SPELL;
						if(auto) affectType=affectType|CMMsg.MASK_ALWAYS;
						if((CMLib.flags().canBeHeardBy(invoker,follower)&&(follower.fetchEffect(this.ID())==null)))
						{
							CMMsg msg2=CMClass.getMsg(mob,follower,this,affectType,null);
							if(R2.okMessage(mob,msg2))
							{
								R2.send(follower,msg2);
								if(CMLib.flags().isSleeping(follower))
								{
									follower.doCommand(CMParms.parse("WAKE"),Command.METAFLAG_FORCED);
									if(!CMLib.flags().isSleeping(follower))
									{
										Ability A=CMClass.getAbility("Searching");
										if(A!=null)	A.invoke(follower,null,true,asLevel);
									}
								}
							}
						}
					}
					R.recoverRoomStats();
				}
			}
		}
		else
			mob.location().show(mob,null,CMMsg.MSG_NOISE,"<S-NAME> hit(s) a foul note.");

		return success;
	}
}
