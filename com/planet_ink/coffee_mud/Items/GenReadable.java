package com.planet_ink.coffee_mud.Items;


import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class GenReadable extends GenItem
{
	protected String	readableText="";
	public GenReadable()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a generic readable thing";
		displayText="a generic readable thing sits here.";
		description="Looks like something";
		isReadable=true;
	}
	
	public GenReadable(String newName, 
					   String newDisplayText, 
					   String newDescription,
					   boolean newIsDroppable,
					   boolean newIsGettable,
					   boolean newIsRemovable,
					   int newBaseGoldValue,
					   int newWeight,
					   String newReadableText)
	{
		super(newName,
			  newDisplayText,
			  newDescription,
			  newIsDroppable,
			  newIsGettable,
			  newIsRemovable,
			  newBaseGoldValue,
			  false,
			  newWeight);
		readableText=newReadableText;
		recoverEnvStats();
	}
	
	public Environmental newInstance()
	{
		return new GenReadable();
	}
	
	public String text()
	{
		return Generic.getPropertiesStr(this);
	}
	
	public void setMiscText(String newText)
	{
		miscText="";
		Generic.setPropertiesStr(this,newText);
		recoverEnvStats();
	}
	public String readableText(){return readableText;}
	public void setReadableText(String text){readableText=text;}
}
