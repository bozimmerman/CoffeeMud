package com.planet_ink.coffee_mud.Items.ClanItems;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.*;

public class StdClanCommonItem extends StdClanItem
{
	public String ID(){	return "StdClanCommonItem";}
	public Environmental newInstance(){ return new StdClanCommonItem();}
	private int workDown=0;
	public static final Vector empty=new Vector();
	public StdClanCommonItem()
	{
		super();

		setName("a clan workers item");
		baseEnvStats.setWeight(1);
		setDisplayText("an workers item belonging to a clan is here.");
		setDescription("");
		secretIdentity="";
		baseGoldValue=1;
		material=EnvResource.RESOURCE_OAK;
		recoverEnvStats();
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!StdClanItem.standardTick(this,tickID))
			return false;
		if((tickID==Host.TICK_CLANITEM)
		&&(owner() instanceof MOB)
		&&(((MOB)owner()).isMonster())
		&&(readableText().length()>0)
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
					empty.clear();
					A.setProfficiency(100);
					if(M.inventorySize()>0)
					{
						Item I=M.fetchInventory(Dice.roll(1,M.inventorySize(),-1));
						if(I!=null)
						{
							Vector V=new Vector();
							V.addElement(I.name());
						}
						else
							A.invoke(M,empty,null,false);
					}
					else
						A.invoke(M,empty,null,false);
				}

			}
		}
		return super.tick(ticking,tickID);
	}
}
