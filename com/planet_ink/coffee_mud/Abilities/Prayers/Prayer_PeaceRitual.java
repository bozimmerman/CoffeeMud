package com.planet_ink.coffee_mud.Abilities.Prayers;

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
public class Prayer_PeaceRitual extends Prayer
{
	public String ID() { return "Prayer_PeaceRitual"; }
	public String name(){ return "Peace Ritual";}
	public String displayText(){ return "(Peace Ritual)";}
	public int quality(){ return OK_SELF;}
	public long flags(){return Ability.FLAG_HOLY;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	public String clan1="";
	public String clan2="";

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return false;

		if(invoker==null)
			return false;

		MOB mob=(MOB)affected;
        Vector channels=ChannelSet.getFlaggedChannelNames("CLANINFO");
        for(int i=0;i<channels.size();i++)
    		CommonMsgs.channel((String)channels.elementAt(i),clan2,mob.name()+" located in '"+mob.location().displayText()+" is performing a peace ritual on behalf of "+clan2+".",false);
		return super.tick(ticking,tickID);
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if(invoker==null) return true;
		if(affected==null) return true;
		if(!(affected instanceof MOB)) return true;

		if((msg.target()==invoker)
		&&(msg.source()!=invoker)
		&&(Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS)))
		{
			msg.source().location().show(invoker,null,CMMsg.MSG_OK_VISUAL,"The peace ritual is disrupted!");
			clan1="";
			clan2="";
			unInvoke();
		}
		else
		if(msg.amISource((MOB)affected)
		&&((msg.targetMinor()==CMMsg.TYP_ENTER)||(msg.targetMinor()==CMMsg.TYP_LEAVE)))
		{
			msg.source().location().show(invoker,null,CMMsg.MSG_OK_VISUAL,"The peace ritual is disrupted!");
			clan1="";
			clan2="";
			unInvoke();
		}
		return true;
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		super.unInvoke();

		if((canBeUninvoked())&&(clan1.length()>0)&&(clan2.length()>0))
		{
			Clan C1=Clans.getClan(clan1);
			Clan C2=Clans.getClan(clan2);
			if((C1!=null)&&(C2!=null))
			{
				if(C1.getClanRelations(C2.ID())==Clan.REL_WAR)
				{
					C1.setClanRelations(C2.ID(),Clan.REL_HOSTILE,System.currentTimeMillis());
					C1.update();
				}
				if(C2.getClanRelations(C1.ID())==Clan.REL_WAR)
				{
					C2.setClanRelations(C1.ID(),Clan.REL_HOSTILE,System.currentTimeMillis());
					C2.update();
				}
                Vector channels=ChannelSet.getFlaggedChannelNames("CLANINFO");
                for(int i=0;i<channels.size();i++)
                    CommonMsgs.channel((String)channels.elementAt(i),"ALL","There is now peace between "+C1.name()+" and "+C2.name()+".",false);
			}
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
        Environmental target=mob;
        if((auto)&&(givenTarget!=null)) target=givenTarget;
        if(target.fetchEffect(this.ID())!=null)
		{
            mob.tell(mob,target,null,"<T-NAME> <T-IS-ARE> already affected by "+name()+".");
			return false;
		}
		if((mob.getClanID().length()==0)||(Clans.getClan(mob.getClanID())==null))
		{
			mob.tell("You must belong to a clan to use this prayer.");
			return false;
		}
		Clan myClan=Clans.getClan(mob.getClanID());
		clan1=myClan.ID();
		if(commands.size()<1)
		{
			mob.tell("You must specify the clan you wish to see peace with.");
			return false;
		}
		clan2=Util.combine(commands,0);
		Clan otherClan=Clans.findClan(clan2);
		if((otherClan==null)
		||((myClan.getClanRelations(clan2)!=Clan.REL_WAR)&&(otherClan.getClanRelations(clan1)!=Clan.REL_WAR)))
		{
			mob.tell("Your "+myClan.typeName()+" is not at war with "+clan2+"!");
			return false;
		}
		clan2=otherClan.ID();
		boolean found=false;
		for(Enumeration e=CMMap.players();e.hasMoreElements();)
		{
			MOB M=(MOB)e.nextElement();
			if(M.getClanID().equals(clan2))
			{ found=true; break;}
		}
		if(!found)
		{
			mob.tell("You must wait until a member of "+clan2+" is online before beginning the ritual.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> begin(s) a peace ritual.":"^S<S-NAME> "+prayWord(mob)+" for peace between "+myClan.name()+" and "+otherClan.name()+".^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,(int)MudHost.TICKS_PER_RLMIN*5);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> "+prayWord(mob)+" for peace between "+myClan.name()+" and "+otherClan.name()+", but there is no answer.");


		// return whether it worked
		return success;
	}
}
