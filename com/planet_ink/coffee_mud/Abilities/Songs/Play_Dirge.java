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
public class Play_Dirge extends Play
{
	public String ID() { return "Play_Dirge"; }
	public String name(){ return "Dirge";}
	protected int canAffectCode(){return 0;}
	public int abstractQuality(){ return Ability.QUALITY_BENEFICIAL_OTHERS;}
	protected boolean persistantSong(){return false;}
	protected boolean skipStandardSongTick(){return true;}
	protected String songOf(){return "a "+name();}
    protected boolean HAS_QUANTITATIVE_ASPECT(){return false;}

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
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_UNWORNONLY);
		if(target==null) return false;

		if((!(target instanceof DeadBody))||(target.rawSecretIdentity().toUpperCase().indexOf("FAKE")>=0))
		{
			mob.tell("You may only play this for the dead.");
			return false;
		}

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
					HashSet h=super.sendMsgAndGetTargets(mob, R, msg, givenTarget, auto);
					if(h==null) continue;
	
					for(Iterator f=h.iterator();f.hasNext();)
					{
						MOB follower=(MOB)f.next();
	
						double exp=10.0;
						int levelLimit=CMProps.getIntVar(CMProps.SYSTEMI_EXPRATE);
						int levelDiff=follower.envStats().level()-target.envStats().level();
						if(levelDiff>levelLimit) exp=0.0;
						int expGained=(int)Math.round(exp);
	
						// malicious songs must not affect the invoker!
						if(CMLib.flags().canBeHeardBy(invoker,follower)&&(expGained>0))
							CMLib.leveler().postExperience(follower,null,null,expGained,false);
					}
					R.recoverRoomStats();
					R.showHappens(CMMsg.MSG_OK_VISUAL,target.name()+" fades away.");
					target.destroy();
				}
			}
		}
		else
			mob.location().show(mob,null,CMMsg.MSG_NOISE,"<S-NAME> hit(s) a foul note.");

		return success;
	}
}
