package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenItem extends StdItem
{
	public String ID(){	return "GenItem";}
	protected String	readableText="";
	public GenItem()
	{
		super();
		name="a generic item";
		baseEnvStats.setWeight(2);
		displayText="a generic item sits here.";
		description="";
		baseGoldValue=5;
		baseEnvStats().setLevel(1);
		recoverEnvStats();
		setMaterial(EnvResource.RESOURCE_OAK);
	}
	public Environmental newInstance()
	{
		return new GenItem();
	}
	public boolean isGeneric(){return true;}

	public String text()
	{
		return Generic.getPropertiesStr(this,false);
	}

	public String readableText(){return readableText;}
	public void setReadableText(String text){readableText=text;}
	public void setMiscText(String newText)
	{
		miscText="";
		Generic.setPropertiesStr(this,newText,false);
		recoverEnvStats();
	}
	
	protected String 	name="an ordinary item";
	protected String	displayText="a nondescript item sits here doing nothing.";
	protected String 	description="It looks like something.";
	protected Item 		myContainer=null;
	protected int 		myUses=Integer.MAX_VALUE;
	protected long 		myWornCode=Item.INVENTORY;
	protected String 	miscText="";
	protected String	secretIdentity=null;
	protected boolean	wornLogicalAnd=false;
	protected long 		properWornBitmap=Item.HELD;
	protected int		baseGoldValue=0;
	protected boolean	isReadable=false;
	protected boolean	isGettable=true;
	protected boolean	isDroppable=true;
	protected boolean	isRemovable=true;
	protected int		material=EnvResource.RESOURCE_COTTON;
	protected Environmental owner=null;
	protected long dispossessionTime=0;

	protected Vector affects=new Vector();
	protected Vector behaviors=new Vector();

	protected EnvStats envStats=new DefaultEnvStats();
	protected EnvStats baseEnvStats=new DefaultEnvStats();

	public String getStat(String code)
	{ return Generic.getGenItemStat(this,code);}
	public void setStat(String code, String val)
	{ Generic.setGenItemStat(this,code,val);}
	public String[] getStatCodes(){return Generic.GENITEMCODES;}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof GenItem)) return false;
		for(int i=0;i<getStatCodes().length;i++)
			if(!E.getStat(getStatCodes()[i]).equals(getStat(getStatCodes()[i])))
				return false;
		return true;
	}
}
