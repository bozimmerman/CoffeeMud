package com.planet_ink.coffee_mud.Items.Weapons;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenLasso extends StdLasso
{
	public String ID(){	return "GenLasso";}
	protected String	readableText="";
	public GenLasso()
	{
		super();

		name="a generic lasso";
		displayText="a generic lasso sits here.";
		description="";
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new GenLasso();
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

