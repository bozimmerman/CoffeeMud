package com.planet_ink.coffee_mud.Items;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.*;

public class GenJournal extends StdJournal
{
	public String ID(){	return "GenJournal";}
	protected String readableText="";
	public GenJournal()
	{
		super();
		setDisplayText("a journal sits here.");
		setDescription("Enter `READ [NUMBER] [JOURNAL]` to read an entry.%0D%0AUse your WRITE skill to add new entries. ");
		isReadable=true;
		setMaterial(EnvResource.RESOURCE_PAPER);
	}

	public Environmental newInstance()
	{
		return new GenJournal();
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
	public String getStat(String code)
	{ return Generic.getGenItemStat(this,code);}
	public void setStat(String code, String val)
	{ Generic.setGenItemStat(this,code,val);}
	public String[] getStatCodes(){return Generic.GENITEMCODES;}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof GenJournal)) return false;
		for(int i=0;i<getStatCodes().length;i++)
			if(!E.getStat(getStatCodes()[i]).equals(getStat(getStatCodes()[i])))
				return false;
		return true;
	}
}
