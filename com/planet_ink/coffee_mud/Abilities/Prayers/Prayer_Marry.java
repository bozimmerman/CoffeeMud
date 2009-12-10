package com.planet_ink.coffee_mud.Abilities.Prayers;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
public class Prayer_Marry extends Prayer
{
	public String ID() { return "Prayer_Marry"; }
	public String name(){ return "Marry";}
	public long flags(){return Ability.FLAG_HOLY;}
	public int abstractQuality(){return Ability.QUALITY_OK_OTHERS;}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_BLESSING;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()<2)
		{
			mob.tell("Whom to whom?");
			return false;
		}
		String name1=(String)commands.lastElement();
		String name2=CMParms.combine(commands,0,commands.size()-1);
		MOB husband=mob.location().fetchInhabitant(name1);
		if((husband==null)||(!CMLib.flags().canBeSeenBy(mob,husband)))
		{
			mob.tell("You don't see "+name1+" here!");
			return false;
		}
		MOB wife=mob.location().fetchInhabitant(name2);
		if((wife==null)||(!CMLib.flags().canBeSeenBy(mob,wife)))
		{
			mob.tell("You don't see "+name2+" here!");
			return false;
		}
		if(wife.charStats().getStat(CharStats.STAT_GENDER)=='M')
		{
			MOB M=wife;
			wife=husband;
			husband=M;
		}
		if(wife.isMarriedToLiege())
		{
			mob.tell(wife.name()+" is already married!!");
			return false;
		}
		if(husband.isMarriedToLiege())
		{
			mob.tell(husband.name()+" is already married!!");
			return false;
		}
		if(wife.getLiegeID().length()>0)
		{
			mob.tell(wife.name()+" is lieged to "+wife.getLiegeID()+", and cannot marry.");
			return false;
		}
		if(husband.getLiegeID().length()>0)
		{
			mob.tell(husband.name()+" is lieged to "+husband.getLiegeID()+", and cannot marry.");
			return false;
		}
		if((wife.isMonster())||(wife.playerStats()==null))
		{
			mob.tell(wife.name()+" must be a player to marry.");
			return false;
		}
		if((husband.isMonster())||(husband.playerStats()==null))
		{
			mob.tell(wife.name()+" must be a player to marry.");
			return false;
		}
		CMLib.coffeeTables().bump(husband,CoffeeTableRow.STAT_BIRTHS);
		Item I=husband.fetchWornItem("wedding band");
		if(I==null)
		{
			mob.tell(husband.name()+" isn't wearing a wedding band!");
			return false;
		}
		I=wife.fetchWornItem("wedding band");
		if(I==null)
		{
			mob.tell(wife.name()+" isn't wearing a wedding band!");
			return false;
		}
		MOB witness=null;
		for(int i=0;i<mob.location().numInhabitants();i++)
		{
			MOB M=mob.location().fetchInhabitant(i);
			if((M!=null)
			&&(M!=mob)
			&&(M!=husband)
			&&(M!=wife))
				witness=M;
		}
		if(witness==null)
		{
			mob.tell("You need a witness present.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":"^S<S-NAME> "+prayForWord(mob)+" to bless the holy union between "+husband.name()+" and "+wife.name()+".^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				husband.setLiegeID(wife.Name());
				wife.setLiegeID(husband.Name());
				CMLib.coffeeTables().bump(husband,CoffeeTableRow.STAT_MARRIAGES);
				CMLib.commands().postSay(mob,husband,"You may kiss your bride!",false,false);
                Vector channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.MARRIAGES);
                for(int i=0;i<channels.size();i++)
                    CMLib.commands().postChannel((String)channels.elementAt(i),husband.getClanID(),husband.name()+" and "+wife.name()+" were just joined in holy matrimony!",true);
             }
		}
		else
			beneficialWordsFizzle(mob,null,"<S-NAME> start(s) 'Dearly beloved', and then clear(s) <S-HIS-HER> throat.");

		return success;
	}
}
