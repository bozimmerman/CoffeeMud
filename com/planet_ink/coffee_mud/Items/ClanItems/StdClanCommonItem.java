package com.planet_ink.coffee_mud.Items.ClanItems;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.*;

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
public class StdClanCommonItem extends StdClanItem
{
	public String ID(){	return "StdClanCommonItem";}
	private int workDown=0;
	public StdClanCommonItem()
	{
		super();

		setName("a clan workers item");
		baseEnvStats.setWeight(1);
		setDisplayText("an workers item belonging to a clan is here.");
		setDescription("");
		secretIdentity="";
		baseGoldValue=1;
		setCIType(ClanItem.CI_GATHERITEM);
		material=EnvResource.RESOURCE_OAK;
		recoverEnvStats();
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((tickID==MudHost.TICK_CLANITEM)
		&&(owner() instanceof MOB)
		&&(((MOB)owner()).isMonster())
		&&(readableText().length()>0)
		&&(((MOB)owner()).getClanID().equals(clanID()))
		&&((--workDown)<=0)
		&&(!Sense.isAnimalIntelligence((MOB)owner())))
		{
			workDown=Dice.roll(1,5,0);
			MOB M=(MOB)owner();
			if(M.fetchEffect(readableText())==null)
			{
				Ability A=CMClass.getAbility(readableText());
				if((A!=null)&&((A.classificationCode()&Ability.ALL_CODES)==Ability.COMMON_SKILL))
				{
					A.setProfficiency(100);
					if(M.inventorySize()>1)
					{
						Item I=null;
						int tries=0;
						while((I==null)&&((++tries)<20))
						{
							I=M.fetchInventory(Dice.roll(1,M.inventorySize(),-1));
							if((I==null)||(I==this)||(!I.amWearingAt(Item.INVENTORY)))
								I=null;
						}
						Vector V=new Vector();
						if(I!=null)	V.addElement(I.name());
						A.invoke(M,V,null,false);
					}
					else
						A.invoke(M,new Vector(),null,false);
				}

			}
		}
		return true;
	}
}
