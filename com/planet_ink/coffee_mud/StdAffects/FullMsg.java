package com.planet_ink.coffee_mud.StdAffects;

import com.planet_ink.coffee_mud.interfaces.*;

public class FullMsg implements Affect
{
	private int targetType=0;
	private int targetCode=0;
	private int sourceType=0;
	private int sourceCode=0;
	private int othersType=0;
	private int othersCode=0;
	private String targetMsg=null;
	private String othersMsg=null;
	private String sourceMsg=null;
	private MOB myAgent=null;
	private Environmental myTarget=null;
	private Environmental myTool=null;
	private boolean modified=false;
	
	public FullMsg(MOB source,
				   Environmental target,
				   Environmental tool,
				   int newSourceCode, 
				   String sourceMessage,
				   int newTargetCode, 
				   String targetMessage,
				   int newOthersCode, 
				   String othersMessage)
	{
		myAgent=source;
		myTarget=target;
		myTool=tool;
		sourceMsg=sourceMessage;
		targetMsg=targetMessage;
		if(newTargetCode>0)
			targetType=newTargetCode & Affect.COMBINED;
		if(newTargetCode>0)
			targetCode=newTargetCode;
		if(newSourceCode>0)
			sourceType=newSourceCode & Affect.COMBINED;
		if(newSourceCode>0)
			sourceCode=newSourceCode;
		if(newOthersCode>0)
			othersType=newOthersCode & Affect.COMBINED;
		if(newOthersCode>0)
			othersCode=newOthersCode;
		othersMsg=othersMessage;
	}
	
	
	public boolean wasModified()
	{
		return modified;
	}
	public void tagModified(boolean newStatus)
	{
		modified=newStatus;
	}
	
	public void modify(MOB source,
						Environmental target,
						Environmental tool,
						int newSourceCode, 
						String sourceMessage,
						int newTargetCode, 
						String targetMessage,
						int newOthersCode, 
						String othersMessage)
	{
		myAgent=source;
		myTarget=target;
		myTool=tool;
		sourceMsg=sourceMessage;
		targetMsg=targetMessage;
		if(newTargetCode>0)
			targetType=newTargetCode & Affect.COMBINED;
		if(newTargetCode>0)
			targetCode=newTargetCode;
		if(newSourceCode>0)
			sourceType=newSourceCode & Affect.COMBINED;
		if(newSourceCode>0)
			sourceCode=newSourceCode;
		if(newOthersCode>0)
			othersType=newOthersCode & Affect.COMBINED;
		if(newOthersCode>0)
			othersCode=newOthersCode;
		othersMsg=othersMessage;
	}
	public FullMsg(MOB source,
				   Environmental target,
				   Environmental tool,
				   int newSourceCode, 
				   int newTargetCode, 
				   int newOthersCode, 
				   String Message)
	{
		myAgent=source;
		myTarget=target;
		myTool=tool;
		targetMsg=Message;
		sourceMsg=Message;
		if(newTargetCode>0)
			targetType=newTargetCode & Affect.COMBINED;
		if(newTargetCode>0)
			targetCode=newTargetCode;
		if(newSourceCode>0)
			sourceType=newSourceCode & Affect.COMBINED;
		if(newSourceCode>0)
			sourceCode=newSourceCode;
		if(newOthersCode>0)
			othersType=newOthersCode & Affect.COMBINED;
		if(newOthersCode>0)
			othersCode=newOthersCode;
		othersMsg=Message;
	}
	public MOB source()
	{
		return myAgent;
	}
	public Environmental target()
	{
		return myTarget;
	}
	public Environmental tool()
	{
		return myTool;
	}
	public int targetType()
	{
		return targetType;
	}
	public int targetCode()
	{
		return targetCode;
	}
	public int sourceType()
	{
		return sourceType;
	}
	public int sourceCode()
	{
		return sourceCode;
	}
	public String targetMessage()
	{
		return targetMsg;
	}
	
	public int othersType()
	{
		return othersType;
	}
	public int othersCode()
	{
		return othersCode;
	}
	public String othersMessage()
	{
		return othersMsg;
	}
	public String sourceMessage()
	{
		return sourceMsg;
	}
	public boolean amITarget(Environmental thisOne)
	{
		if((target()!=null)
		   &&(target()==thisOne))
			return true;
		return false;
		
	}
	public boolean amISource(MOB thisOne)
	{
		if((source()!=null)
		   &&(source()==thisOne))
			return true;
		return false;
		
	}
}
