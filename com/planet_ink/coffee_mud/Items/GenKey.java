package com.planet_ink.coffee_mud.Items;


import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenKey extends GenItem implements Key
{
	public GenKey()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a generic key thing";
		displayText="a generic key thing sits here.";
		description="Looks like something";
		isReadable=false;
		setMaterial(EnvResource.RESOURCE_IRON);
	}

	public Environmental newInstance()
	{
		return new GenKey();
	}
	public boolean isGeneric(){return true;}

	public void setKey(String keyName){readableText=keyName;}
	public String getKey(){return readableText;}
}
