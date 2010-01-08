package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
public class Soiled extends StdAbility
{
	public String ID() { return "Soiled"; }
	public String name(){ return "Soiled";}
	public String displayText(){ return "(Soiled)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	private static final String[] triggerStrings = {"SOIL"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.ACODE_SKILL;}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(affected==null) return;
		affectableStats.setStat(CharStats.STAT_CHARISMA,affectableStats.getStat(CharStats.STAT_CHARISMA)/2);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		Environmental E=affected;
		if(E==null) return;
		super.unInvoke();
		if(canBeUninvoked())
		{
		    if(E instanceof MOB)
		    {
		        MOB mob=(MOB)E;
				mob.tell("You are no longer soiled.");
				MOB following=((MOB)E).amFollowing();
				if((following!=null)
				&&(following.location()==mob.location())
                &&(CMLib.flags().isInTheGame(E,true))
			    &&(CMLib.flags().canBeSeenBy(mob,following)))
					following.tell(E.name()+" is no longer soiled.");
		    }
		    else
			if((E instanceof Item)&&(((Item)E).owner() instanceof MOB))
				((MOB)((Item)E).owner()).tell(E.name()+" is no longer soiled.");
		}
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
	    if(((msg.source()==affected)
        ||((affected instanceof Item)
            &&(((Item)affected).owner()==msg.source()))))
	    {
		    if((CMath.bset(msg.sourceMajor(),CMMsg.MASK_MOVE))
		    &&(msg.source().riding()==null)
		    &&(msg.source().location()!=null)
		    &&((msg.source().location().domainType()==Room.DOMAIN_INDOORS_WATERSURFACE)
	            ||(msg.source().location().domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE)
	            ||(msg.source().location().domainType()==Room.DOMAIN_INDOORS_UNDERWATER)
	            ||(msg.source().location().domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)))
		        unInvoke();
		    else
		    if((CMath.bset(msg.sourceMajor(),CMMsg.MASK_MOVE))
		    &&(msg.source().riding() instanceof Drink)
		    &&(((Drink)msg.source().riding()).containsDrink()))
		        unInvoke();
		    else
		    if((affected instanceof Item)
		    &&(((Item)affected).container() instanceof Drink)
		    &&(msg.target()==affected)
		    &&(msg.targetMinor()==CMMsg.TYP_PUT)
		    &&(((Drink)((Item)affected).container()).containsDrink()))
		        unInvoke();
	    }
	    if((msg.target()==affected)
	    &&(msg.targetMinor()==CMMsg.TYP_SNIFF))
	    {
	        String smell=null;
	        switch(CMLib.dice().roll(1,5,0))
	        {
		        case 1: smell="<T-NAME> is stinky!"; break;
		        case 2: smell="<T-NAME> smells like poo."; break;
		        case 3: smell="<T-NAME> has soiled a diaper."; break;
		        case 4: smell="Whew! <T-NAME> stinks!"; break;
		        case 5: smell="<T-NAME> must have let one go!"; break;
	        }
	        if((CMLib.flags().canSmell(msg.source()))&&(smell!=null))
	            msg.source().tell(msg.source(),affected,null,smell);
	    }
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
	    if(affected!=null)
	    if(CMLib.dice().rollPercentage()==1)
	    {
	        Environmental E=affected;
	        Room R=CMLib.map().roomLocation(E);
	        if(R!=null)
	        {
	            MOB M=(E instanceof MOB)?(MOB)E:null;
                boolean killmob=false;
	            if(M==null)
	            {
					M=CMClass.getMOB("StdMOB");
					M.setName(affected.name());
					M.setDisplayText(affected.name()+" is here.");
					M.setDescription("");
					if(M.location()!=R)
						M.setLocation(R);
                    killmob=true;
	            }
	            else
	            if((M.playerStats()!=null)&&(M.playerStats().getHygiene()<10000))
	            {
	                M.playerStats().setHygiene(10000);
	                M.recoverCharStats();
	            }
	            String smell=null;
	            switch(CMLib.dice().roll(1,5,0))
	            {
	            case 1: smell="<S-NAME> <S-IS-ARE> stinky!"; break;
	            case 2: smell="<S-NAME> smells like poo."; break;
	            case 3: smell="<S-NAME> has soiled a diaper."; break;
	            case 4: smell="Whew! <S-NAME> stinks!"; break;
	            case 5: smell="<S-NAME> must have let one go!"; break;
	            }
	            if((smell!=null)
	            &&(CMLib.flags().isInTheGame(M,true)))
	            {
	                CMMsg msg=CMClass.getMsg(M,null,null,CMMsg.TYP_EMOTE|CMMsg.MASK_ALWAYS,smell);
	                if(R.okMessage(M,msg))
		            for(int m=0;m<R.numInhabitants();m++)
		            {
		                MOB mob=R.fetchInhabitant(m);
		                if(CMLib.flags().canSmell(mob))
		                    mob.executeMsg(M,msg);
		            }
	            }
                if(killmob) M.destroy();
	        }
	    }
	    return super.tick(ticking,tickID);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Environmental target=getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_ANY);
		if((target==null)||(target.fetchEffect(ID())!=null)) 
		    return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// it worked, so build a copy of this ability,
		// and add it to the affects list of the
		// affected MOB.  Then tell everyone else
		// what happened.
	    Ability A=(Ability)copyOf();
	    A.startTickDown(mob,target,Integer.MAX_VALUE/2);
        Environmental msgTarget=target;
        if(target instanceof CagedAnimal) msgTarget=((CagedAnimal)target).unCageMe();
		mob.location().show(mob,msgTarget,CMMsg.MSG_OK_VISUAL,"<T-NAME> has soiled <T-HIM-HERSELF>!");
        if(target instanceof MOB)
        {
            Item pants=((MOB)target).fetchFirstWornItem(Wearable.WORN_WAIST);
            if((pants!=null)&&(pants.fetchEffect(ID())==null))
            {
			    A=(Ability)copyOf();
			    A.startTickDown((MOB)target,pants,Integer.MAX_VALUE/2);
            }
        }
		return true;
	}
}