package com.planet_ink.coffee_mud.Items.MiscTech;
import com.planet_ink.coffee_mud.Items.StdContainer;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class GenSSPanel extends StdContainer implements ShipComponent
{
	public String ID(){	return "GenSSPanel";}
	private String readableText = "";
	private int componentType=ShipComponent.COMPONENT_MISC;
	public GenSSPanel()
	{
		super();
		setName("a generic space ship panel");
		baseEnvStats.setWeight(2);
		setDescription("");
		baseGoldValue=5;
		containType=Container.CONTAIN_SSCOMPONENTS;
		setLidsNLocks(true,true,false,false);
		capacity=500;
		setMaterial(EnvResource.RESOURCE_STEEL);
		recoverEnvStats();
	}
	
	public String displayText(){
		if(isOpen())
			return name()+" is opened here.";
		else
			return "";
	}
	public String readableText(){return readableText;}
	public void setReadableText(String text){readableText=text;}
	public String keyName()
	{
		return readableText;
	}
	public boolean canContain(Environmental E)
	{
		if(!super.canContain(E)) return false;
		if(E instanceof ShipComponent)
		{
			int myType=((ShipComponent)E).componentType();
			switch(componentType())
			{
			case ShipComponent.COMPONENT_PANEL_ANY:
				return true;
			case ShipComponent.COMPONENT_PANEL_ENGINE:
				return myType==ShipComponent.COMPONENT_ENGINE;
			case ShipComponent.COMPONENT_PANEL_POWER:
				return myType==ShipComponent.COMPONENT_POWER;
			case ShipComponent.COMPONENT_PANEL_SENSOR:
				return myType==ShipComponent.COMPONENT_SENSOR;
			case ShipComponent.COMPONENT_PANEL_WEAPON:
				return myType==ShipComponent.COMPONENT_WEAPON;
			case ShipComponent.COMPONENT_PANEL_COMPUTER:
				return myType==ShipComponent.COMPONENT_COMPUTER;
			}
		}
		return true;
	}
	public void setKeyName(String newKeyName)
	{
		readableText=newKeyName;
	}
	public Environmental newInstance()
	{
		return new GenSSPanel();
	}
	public boolean isGeneric(){return true;}

	public String text()
	{
		return CoffeeMaker.getPropertiesStr(this,false);
	}

	public void setMiscText(String newText)
	{
		miscText="";
		CoffeeMaker.setPropertiesStr(this,newText,false);
		recoverEnvStats();
	}
	public int componentType(){return componentType;}
	public void setComponentType(int type){componentType=type;}
	
	private static String[] MYCODES={"HASLOCK","HASLID","CAPACITY","CONTAINTYPES","COMPONENTTYPE"};
	public String getStat(String code)
	{
		if(CoffeeMaker.getGenItemCodeNum(code)>=0)
			return CoffeeMaker.getGenItemStat(this,code);
		else
		switch(getCodeNum(code))
		{
		case 0: return ""+hasALock();
		case 1: return ""+hasALid();
		case 2: return ""+capacity();
		case 3: return ""+containTypes();
		case 4: return ""+componentType();
		}
		return "";
	}
	public void setStat(String code, String val)
	{
		if(CoffeeMaker.getGenItemCodeNum(code)>=0)
			CoffeeMaker.setGenItemStat(this,code,val);
		else
		switch(getCodeNum(code))
		{
		case 0: setLidsNLocks(hasALid(),isOpen(),Util.s_bool(val),false); break;
		case 1: setLidsNLocks(Util.s_bool(val),isOpen(),hasALock(),false); break;
		case 2: setCapacity(Util.s_int(val)); break;
		case 3: setContainTypes(Util.s_long(val)); break;
		case 4: setComponentType(Util.s_int(val)); break;
		}
	}
	protected int getCodeNum(String code){
		for(int i=0;i<MYCODES.length;i++)
			if(code.equalsIgnoreCase(MYCODES[i])) return i;
		return -1;
	}
	private static String[] codes=null;
	public String[] getStatCodes()
	{
		if(codes!=null) return codes;
		String[] superCodes=CoffeeMaker.GENITEMCODES;
		codes=new String[superCodes.length+MYCODES.length];
		int i=0;
		for(;i<superCodes.length;i++)
			codes[i]=superCodes[i];
		for(int x=0;x<MYCODES.length;i++,x++)
			codes[i]=MYCODES[x];
		return codes;
	}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof GenSSPanel)) return false;
		String[] codes=getStatCodes();
		for(int i=0;i<codes.length;i++)
			if(!E.getStat(codes[i]).equals(getStat(codes[i])))
				return false;
		return true;
	}
}