package com.planet_ink.coffee_mud.Items.ClanItems;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.*;

public class StdClanCommonContainer extends StdClanContainer
{
	public String ID(){	return "StdClanCommonContainer";}
	private int workDown=0;
	public StdClanCommonContainer()
	{
		super();

		setName("a clan workers container");
		baseEnvStats.setWeight(1);
		setDisplayText("an workers container belonging to a clan is here.");
		setDescription("");
		secretIdentity="";
		baseGoldValue=1;
		capacity=100;
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
