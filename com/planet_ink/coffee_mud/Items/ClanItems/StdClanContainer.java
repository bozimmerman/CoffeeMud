package com.planet_ink.coffee_mud.Items.ClanItems;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.StdContainer;
import java.util.*;


public class StdClanContainer extends StdContainer implements ClanItem
{
	public String ID(){	return "StdClanContainer";}
	public Environmental newInstance(){ return new StdClanContainer();}
	protected String myClan="";
	protected int ciType=0;
	public int ciType(){return ciType;}
	public void setCIType(int type){ ciType=type;}	
	public StdClanContainer()
	{
		super();

		setName("a clan container");
		baseEnvStats.setWeight(1);
		setDisplayText("an item belonging to a clan is here.");
		setDescription("");
		secretIdentity="";
		baseGoldValue=1;
		capacity=100;
		material=EnvResource.RESOURCE_OAK;
		recoverEnvStats();
	}
	
	public String clanID(){return myClan;}
	public void setClanID(String ID){myClan=ID;}
	
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(StdClanItem.stdExecuteMsg(this,msg))
			super.executeMsg(myHost,msg);
	}
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(StdClanItem.stdOkMessage(this,msg))
			return super.okMessage(myHost,msg);
		return false;
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!StdClanItem.standardTick(this,tickID))
			return false;
		return super.tick(ticking,tickID);
	}
}
