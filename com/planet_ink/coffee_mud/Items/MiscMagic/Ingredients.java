package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import com.planet_ink.coffee_mud.system.*;

public class Ingrediants extends BagOfHolding
{
	public String ID(){	return "Ingrediants";}
	boolean alreadyFilled=false;
	public Ingrediants()
	{
		super();

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Ingrediants();
	}
	
	protected Item makeResource(String name, int type)
	{
		Item I=null;
		if(((type&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_FLESH)
		||((type&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_VEGETATION))
			I=CMClass.getItem("GenFoodResource");
		else
		if((type&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_LIQUID)
			I=CMClass.getItem("GenLiquidResource");
		else
			I=CMClass.getItem("GenResource");
		I.setName(name);
		I.setDisplayText(name+" has been left here.");
		I.setDescription("It looks like "+name);
		I.setMaterial(type);
		I.setBaseValue(EnvResource.RESOURCE_DATA[type&EnvResource.RESOURCE_MASK][1]);
		I.baseEnvStats().setWeight(1);
		I.recoverEnvStats();
		I.setContainer(this);
		if(owner() instanceof Room)
			((Room)owner()).addItem(I);
		else
		if(owner() instanceof MOB)
			((MOB)owner()).addInventory(I);
		return I;
	}
	
	public void affect(Affect affect)
	{
		if((!alreadyFilled)&&(owner()!=null))
		{
			alreadyFilled=true;
			if(getContents().size()==0)
			for(int i=0;i<EnvResource.RESOURCE_DATA.length;i++)
			{
				String name=EnvResource.RESOURCE_DESCS[i];
				makeResource(name.toLowerCase(),EnvResource.RESOURCE_DATA[i][0]);
				makeResource(name.toLowerCase(),EnvResource.RESOURCE_DATA[i][0]);
			}
		}
		super.affect(affect);
	}
}
