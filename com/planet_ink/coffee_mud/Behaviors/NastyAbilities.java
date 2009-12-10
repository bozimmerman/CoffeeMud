package com.planet_ink.coffee_mud.Behaviors;
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
public class NastyAbilities extends ActiveTicker
{
	public String ID(){return "NastyAbilities";}
	protected int canImproveCode(){return Behavior.CAN_MOBS;}
	boolean fightok=false;

	public NastyAbilities()
	{
        super();
		minTicks=10; maxTicks=20; chance=100;
		tickReset();
	}

	public void setParms(String newParms)
	{
		super.setParms(newParms);
		fightok=newParms.toUpperCase().indexOf("FIGHTOK")>=0;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if((canAct(ticking,tickID))&&(ticking instanceof MOB))
		{
			MOB mob=(MOB)ticking;
			Room thisRoom=mob.location();
			if(thisRoom==null) return true;

			double aChance=CMath.div(mob.curState().getMana(),mob.maxState().getMana());
			if((Math.random()>aChance)||(mob.curState().getMana()<50))
				return true;

			MOB target=thisRoom.fetchInhabitant(CMLib.dice().roll(1,thisRoom.numInhabitants(),-1));
			int x=0;
			while(((target==null)||(target.getVictim()==mob)||(target==mob)||(target.isMonster()))&&((++x)<10))
				target=thisRoom.fetchInhabitant(CMLib.dice().roll(1,thisRoom.numInhabitants(),-1));

			int tries=0;
			Ability tryThisOne=null;
			while((tryThisOne==null)&&(tries<100)&&((mob.numAbilities())>0))
			{
				tryThisOne=mob.fetchAbility(CMLib.dice().roll(1,mob.numAbilities(),-1));
				if((tryThisOne!=null)
				   &&(mob.fetchEffect(tryThisOne.ID())==null)
				   &&(tryThisOne.castingQuality(mob,target)==Ability.QUALITY_MALICIOUS))
				{
					if((tryThisOne.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
					{
						if(!tryThisOne.appropriateToMyFactions(mob))
							tryThisOne=null;
					}
				}
				else
					tryThisOne=null;
				tries++;
			}
			if(tryThisOne!=null)
				if((target!=null)&&(target!=mob)&&(!target.isMonster()))
				{
					Hashtable H=new Hashtable();
					for(int i=0;i<thisRoom.numInhabitants();i++)
					{
						MOB M=thisRoom.fetchInhabitant(i);
						if((M!=null)&&(M.getVictim()!=null))
							H.put(M,M.getVictim());
					}
					tryThisOne.setProficiency(CMLib.ableMapper().getMaxProficiency(mob,true,tryThisOne.ID()));
					Vector V=new Vector();
					V.addElement(target.name());
					if((tryThisOne.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SONG)
						tryThisOne.invoke(mob,new Vector(),null,false,0);
					else
						tryThisOne.invoke(mob,V,target,false,0);

					if(!fightok)
					for(int i=0;i<thisRoom.numInhabitants();i++)
					{
						MOB M=thisRoom.fetchInhabitant(i);
						if(H.containsKey(M))
							M.setVictim((MOB)H.get(M));
						else
							M.setVictim(null);
					}
				}
		}
		return true;
	}
}
