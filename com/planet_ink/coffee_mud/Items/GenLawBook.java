package com.planet_ink.coffee_mud.Items;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.*;

public class GenLawBook extends StdLawBook
{
	public String ID(){	return "GenLawBook";}
	protected String	readableText="";
	public GenLawBook()
	{
		super();
		setName("a law book");
		setDisplayText("a law book sits here.");
		setDescription("Enter `READ [PAGE NUMBER] \"law book\"` to read an entry.%0D%0AUse your WRITE skill to add new entries. ");
		material=EnvResource.RESOURCE_PAPER;
		isReadable=true;
	}

	public Environmental newInstance()
	{
		return new GenLawBook();
	}
	public boolean isGeneric(){return true;}
	public String text()
	{
		return CoffeeMaker.getPropertiesStr(this,false);
	}

	public String readableText(){return readableText;}
	public void setReadableText(String text){readableText=text;}
	public void setMiscText(String newText)
	{
		miscText="";
		CoffeeMaker.setPropertiesStr(this,newText,false);
		recoverEnvStats();
	}

	public String getStat(String code)
	{ return CoffeeMaker.getGenItemStat(this,code);}
	public void setStat(String code, String val)
	{ CoffeeMaker.setGenItemStat(this,code,val);}
	public String[] getStatCodes(){return CoffeeMaker.GENITEMCODES;}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof GenItem)) return false;
		for(int i=0;i<getStatCodes().length;i++)
			if(!E.getStat(getStatCodes()[i]).equals(getStat(getStatCodes()[i])))
				return false;
		return true;
	}
}
