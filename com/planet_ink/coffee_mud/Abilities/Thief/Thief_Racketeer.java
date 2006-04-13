package com.planet_ink.coffee_mud.Abilities.Thief;
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
   Copyright 2000-2006 Bo Zimmerman

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
public class Thief_Racketeer extends ThiefSkill
{
	public String ID() { return "Thief_Racketeer"; }
	public String name(){ return "Racketeer";}
	public String displayText(){return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	private static final String[] triggerStrings = {"RACKETEER"};
	public String[] triggerStrings(){return triggerStrings;}
	protected boolean disregardsArmorCheck(MOB mob){return true;}
	public Vector mobs=new Vector();

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if((commands.size()<1)&&(givenTarget==null))
		{
			mob.tell("Get protection money from whom?");
			return false;
		}
		MOB target=mob.location().fetchInhabitant(CMParms.combine(commands,0));
		if((target==null)&&(givenTarget!=null)&&(givenTarget instanceof MOB)) target=(MOB)givenTarget;
		if((target==null)||(target.amDead())||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			mob.tell("You don't see '"+CMParms.combine(commands,1)+"' here.");
			return false;
		}
		if((CMLib.coffeeShops().getShopKeeper(target)==null)
        &&(target.fetchBehavior("MoneyChanger")==null)
        &&(target.fetchBehavior("ItemMender")==null)
        &&(target.fetchBehavior("ItemIdentifier")==null)
        &&(target.fetchBehavior("ItemRefitter")==null))
		{
			mob.tell("You can't get protection money from "+target.name()+".");
			return false;
		}
		Ability A=target.fetchEffect(ID());
		if(A!=null)
		{
			if(A.invoker()==mob)
				mob.tell(target.name()+" has already been extracted from today.");
			else
			{
				mob.tell(target.name()+" is already under "+A.invoker().name()+"'s protection.");
				A.invoker().tell("Word on the street is that "+mob.name()+" is trying to push into your business with "+target.name()+".");
			}
			return false;
		}
		int levelDiff=target.envStats().level()-mob.envStats().level();

		if((!target.mayIFight(mob))||(levelDiff>15))
		{
			mob.tell("You cannot racketeer "+target.charStats().himher()+".");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		double amount=new Long(CMLib.dice().roll(profficiency(),target.envStats().level(),0)).doubleValue();
		boolean success=profficiencyCheck(mob,-(levelDiff),auto);
		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,(auto?CMMsg.MASK_ALWAYS:0)|CMMsg.MSG_THIEF_ACT,"<S-NAME> extract(s) "+CMLib.beanCounter().nameCurrencyShort(target,amount)+" of protection money from <T-NAME>.");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,new Long(((MudHost.TIME_MILIS_PER_MUDHOUR*mob.location().getArea().getTimeObj().getHoursInDay()*mob.location().getArea().getTimeObj().getDaysInMonth())/Tickable.TIME_TICK)).intValue());
				Coins C=CMLib.beanCounter().makeBestCurrency(mob,amount);
				if(C!=null)
				{
					mob.location().addItemRefuse(C,Item.REFUSE_PLAYER_DROP);
					CMLib.commands().postGet(mob,null,C,true);
				}
			}
		}
		else
			maliciousFizzle(mob,target,"<T-NAME> seem(s) unintimidated by <S-NAME>.");
		return success;
	}

}
