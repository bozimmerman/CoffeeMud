package com.planet_ink.coffee_mud.Items.ClanItems;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.StdItem;
import java.util.*;


public class StdClanItem extends StdItem implements ClanItem
{
	public String ID(){	return "StdClanItem";}
	public Environmental newInstance(){ return new StdClanItem();}
	private String myClan="";
	public int ciType(){return 0;}
	
	public StdClanItem()
	{
		super();

		setName("a clan item");
		baseEnvStats.setWeight(1);
		setDisplayText("an item belonging to a clan is here.");
		setDescription("");
		secretIdentity="";
		baseGoldValue=1;
		material=EnvResource.RESOURCE_OAK;
		recoverEnvStats();
	}
	
	public String clanID(){return myClan;}
	public void setClanID(String ID){myClan=ID;}
}
