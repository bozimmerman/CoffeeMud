package com.planet_ink.coffee_mud.Items;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.*;

public class GenJournal extends StdJournal
{
	protected String readableText="";
	public GenJournal()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		displayText="a journal sits here.";
		description="Use the READ command to read the journal, and WRITE to add your own entries.";
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
}
