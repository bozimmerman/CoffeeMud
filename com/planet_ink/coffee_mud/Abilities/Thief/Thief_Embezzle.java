package com.planet_ink.coffee_mud.Abilities.Thief;

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
public class Thief_Embezzle extends ThiefSkill
{
	public String ID() { return "Thief_Embezzle"; }
	public String name(){ return "Embezzle";}
	public String displayText(){return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	private static final String[] triggerStrings = {"EMBEZZLE"};
	public String[] triggerStrings(){return triggerStrings;}
	protected boolean disregardsArmorCheck(MOB mob){return true;}
	public Vector mobs=new Vector();

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if((commands.size()<1)&&(givenTarget==null))
		{
			mob.tell("Embezzle money from whose accounts?");
			return false;
		}
		MOB target=mob.location().fetchInhabitant(Util.combine(commands,0));
		if((target==null)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if((target==null)||(target.amDead())||(!Sense.canBeSeenBy(target,mob)))
		{
			mob.tell("You don't see '"+Util.combine(commands,1)+"' here.");
			return false;
		}
		if(!(target instanceof Banker))
		{
			mob.tell("You can't embezzle from "+target.name()+"'s accounts.");
			return false;
		}
		Banker bank=(Banker)target;
		Ability A=target.fetchEffect(ID());
		if(A!=null)
		{
			mob.tell(target.name()+" is watching "+target.charStats().hisher()+" books too closely.");
			return false;
		}
		int levelDiff=target.envStats().level()-mob.envStats().level();


		if(((!target.mayIFight(mob))&&(levelDiff<10)))
		{
			mob.tell("You cannot embezzle from "+target.charStats().himher()+".");
			return false;
		}

		Item myCoins=bank.findDepositInventory(mob.Name(),"1");
		if((myCoins==null)||(!(myCoins instanceof Coins)))
		{
			mob.tell("You don't have your own account with "+target.name()+".");
			return false;
		}
		Vector accounts=bank.getAccountNames();
		String victim="";
		int tries=0;
		Coins hisCoins=null;
		int hisAmount=0;
		while((hisCoins==null)&&((++tries)<10))
		{
			String possVic=(String)accounts.elementAt(Dice.roll(1,accounts.size(),-1));
			Item C=bank.findDepositInventory(possVic,"1");
			if((C!=null)&&(C instanceof Coins)&&((((Coins)C).numberOfCoins()/50)>0))
			{
				hisCoins=(Coins)C;
				victim=possVic;
				hisAmount=hisCoins.numberOfCoins()/50;
				break;
			}
		}
		int classLevel=mob.charStats().getClassLevel("Burglar");
		if((classLevel>0)
		&&(hisAmount>(1000*classLevel)))
		   hisAmount=(1000*classLevel);

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,-(levelDiff),auto);
		if((success)&&(hisAmount>0)&&(hisCoins!=null))
		{
			FullMsg msg=new FullMsg(mob,target,this,(auto?CMMsg.MASK_GENERAL:0)|CMMsg.MSG_THIEF_ACT,"<S-NAME> embezzle(s) "+hisAmount+" gold from the "+victim+" account maintained by <T-NAME>.");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,new Long(((MudHost.TIME_MILIS_PER_MUDHOUR*mob.location().getArea().getTimeObj().getHoursInDay())/MudHost.TICK_TIME)).intValue());
				bank.delDepositInventory(victim,hisCoins);
				hisCoins.setNumberOfCoins(hisCoins.numberOfCoins()-hisAmount);
				bank.addDepositInventory(victim,hisCoins);
				bank.delDepositInventory(mob.Name(),myCoins);
				((Coins)myCoins).setNumberOfCoins(((Coins)myCoins).numberOfCoins()+hisAmount);
				bank.addDepositInventory(mob.Name(),myCoins);
			}
		}
		else
			maliciousFizzle(mob,target,"<T-NAME> catch(es) <S-NAME> trying to embezzle money!");
		return success;
	}

}
