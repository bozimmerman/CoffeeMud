package com.planet_ink.coffee_mud.Abilities.Common;
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
public class AnimalTrading extends CommonSkill
{
	public String ID() { return "AnimalTrading"; }
	public String name(){ return "Animal Trading";}
	private static final String[] triggerStrings = {"ANIMALTRADING","ANIMALTRADE","ANIMALSELL","ASELL"};
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
    public int classificationCode() {   return Ability.ACODE_COMMON_SKILL|Ability.DOMAIN_ANIMALAFFINITY; }
    protected Vector recentlyTraded=new Vector();

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Environmental taming=null;
		Item cage=null;

		commands.insertElementAt("SELL",0);
        Environmental shopkeeper=CMLib.english().parseShopkeeper(mob,commands,"Sell what to whom?");
		if(shopkeeper==null) return false;
		if(commands.size()==0)
		{
			commonTell(mob,"Sell what?");
			return false;
		}

		String str=CMParms.combine(commands,0);
		MOB M=mob.location().fetchInhabitant(str);
		if(M!=null)
		{
			if(!CMLib.flags().canBeSeenBy(M,mob))
			{
				commonTell(mob,"You don't see anyone called '"+str+"' here.");
				return false;
			}
			if((!M.isMonster())||(!CMLib.flags().isAnimalIntelligence(M)))
			{
				commonTell(mob,"You can't sell "+M.name()+".");
				return false;
			}
			if((CMLib.flags().canMove(M))&&(!CMLib.flags().isBoundOrHeld(M)))
			{
				commonTell(mob,M.name()+" doesn't seem willing to cooperate.  You need to bind the animal before you can sell it.");
				return false;
			}
			taming=M;
		}
		else
		if(mob.location()!=null)
		{
			for(int i=0;i<mob.location().numItems();i++)
			{
				Item I=mob.location().fetchItem(i);
				if((I!=null)
				&&(I instanceof Container)
				&&((((Container)I).containTypes()&Container.CONTAIN_CAGED)==Container.CONTAIN_CAGED))
				{ cage=I; break;}
			}
			if(cage==null)
			for(int i=0;i<mob.inventorySize();i++)
			{
				Item I=mob.fetchInventory(i);
				if((I!=null)
				&&(I instanceof Container)
				&&((((Container)I).containTypes()&Container.CONTAIN_CAGED)==Container.CONTAIN_CAGED))
				{ cage=I; break;}
			}
			if(commands.size()>0)
			{
				String last=(String)commands.lastElement();
				Environmental E=mob.location().fetchFromMOBRoomFavorsItems(mob,null,last,Wearable.FILTER_ANY);
				if((E!=null)
				&&(E instanceof Item)
				&&(E instanceof Container)
				&&((((Container)E).containTypes()&Container.CONTAIN_CAGED)==Container.CONTAIN_CAGED))
				{
					cage=(Item)E;
					commands.removeElement(last);
				}
			}
			if(cage==null)
			{
				commonTell(mob,"You don't see anyone called '"+str+"' here.");
				return false;
			}
			taming=mob.location().fetchFromMOBRoomFavorsItems(mob,cage,CMParms.combine(commands,0),Wearable.FILTER_ANY);
			if((taming==null)||(!CMLib.flags().canBeSeenBy(taming,mob))||(!(taming instanceof CagedAnimal)))
			{
				commonTell(mob,"You don't see any creatures in "+cage.name()+" called '"+CMParms.combine(commands,0)+"'.");
				return false;
			}
			M=((CagedAnimal)taming).unCageMe();
		}
		else
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		if(proficiencyCheck(mob,0,auto))
		{
			CMMsg msg=CMClass.getMsg(mob,shopkeeper,M,CMMsg.MSG_SELL,"<S-NAME> sell(s) <O-NAME> to <T-NAME>.");
            CMMsg msg2=CMClass.getMsg(mob,M,this,CMMsg.MSG_NOISYMOVEMENT,null);
            if(!recentlyTraded.contains(mob.Name()))
            {
                while(recentlyTraded.size()>30)
                    recentlyTraded.removeElementAt(0);
                recentlyTraded.addElement(M.Name());
                msg2.setValue(-1);
            }
			if((mob.location().okMessage(mob,msg))&&(mob.location().okMessage(mob,msg2)))
			{
				mob.location().send(mob,msg);
                mob.location().send(mob,msg2);
				if(taming instanceof Item)
					((Item)taming).destroy();
			}
		}
		else
			beneficialWordsFizzle(mob,shopkeeper,"<S-NAME> <S-IS-ARE>n't able to strike a deal with <T-NAME>.");
		return true;
	}
}
