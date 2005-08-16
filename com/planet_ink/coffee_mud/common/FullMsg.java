package com.planet_ink.coffee_mud.common;
import com.planet_ink.coffee_mud.interfaces.*;
import java.util.Vector;

/* 
   Copyright 2000-2005 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class FullMsg implements CMMsg
{
	private int targetCode=0;
	private int sourceCode=0;
	private int othersCode=0;
	private String targetMsg=null;
	private String othersMsg=null;
	private String sourceMsg=null;
	private MOB myAgent=null;
	private Environmental myTarget=null;
	private Environmental myTool=null;
	private int value=0;
	private Vector trailMsgs=null;

	public FullMsg(MOB source,
				   Environmental target,
				   int newAllCode,
				   String allMessage)
	{
		myAgent=source;
		myTarget=target;
		myTool=null;
		sourceMsg=allMessage;
		targetMsg=allMessage;
		targetCode=newAllCode;
		sourceCode=newAllCode;
		othersCode=newAllCode;
		othersMsg=allMessage;
	}
    
    public FullMsg(MOB source,
                   int newAllCode,
                   String allMessage)
    {
        myAgent=source;
        myTarget=null;
        myTool=null;
        sourceMsg=allMessage;
        targetMsg=allMessage;
        targetCode=newAllCode;
        sourceCode=newAllCode;
        othersCode=newAllCode;
        othersMsg=allMessage;
    }
    
    public FullMsg(MOB source,
                   int newAllCode,
                   String allMessage,
                   int newValue)
    {
         myAgent=source;
         myTarget=null;
         myTool=null;
         sourceMsg=allMessage;
         targetMsg=allMessage;
         targetCode=newAllCode;
         sourceCode=newAllCode;
         othersCode=newAllCode;
         othersMsg=allMessage;
         value=newValue;
    }
    
	public FullMsg(MOB source,
				   Environmental target,
				   Environmental tool,
				   int newAllCode,
				   String allMessage)
	{
		myAgent=source;
		myTarget=target;
		myTool=tool;
		sourceMsg=allMessage;
		targetMsg=allMessage;
		targetCode=newAllCode;
		sourceCode=newAllCode;
		othersCode=newAllCode;
		othersMsg=allMessage;
	}
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
		targetCode=newTargetCode;
		sourceCode=newSourceCode;
		othersCode=newOthersCode;
		othersMsg=othersMessage;
	}

	public FullMsg(MOB source,
				   Environmental target,
				   Environmental tool,
				   int newSourceCode,
				   String sourceMessage,
				   String targetMessage,
				   String othersMessage)
	{
		myAgent=source;
		myTarget=target;
		myTool=tool;
		sourceMsg=sourceMessage;
		targetMsg=targetMessage;
		targetCode=newSourceCode;
		sourceCode=newSourceCode;
		othersCode=newSourceCode;
		othersMsg=othersMessage;
	}

	public CMMsg copyOf()
	{
		return new FullMsg(source(),target(),tool(),sourceCode(),sourceMessage(),targetCode(),targetMessage(),othersCode(),othersMessage());
	}

	public int value(){return value;}
	public void setValue(int amount){value=amount;}
	
	public Vector trailerMsgs()
	{	return trailMsgs;}
	public void addTrailerMsg(CMMsg msg)
	{
		if(trailMsgs==null) trailMsgs=new Vector();
		trailMsgs.addElement(msg);
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
		targetCode=newTargetCode;
		sourceCode=newSourceCode;
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
		targetCode=newTargetCode;
		sourceCode=newSourceCode;
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
	public int targetMajor()
	{
		return targetCode&CMMsg.MAJOR_MASK;
	}
	public int targetMinor()
	{
		return targetCode&CMMsg.MINOR_MASK;
	}
	public int targetCode()
	{
		return targetCode;
	}
	public String targetMessage()
	{
		return targetMsg;
	}

	public int sourceCode()
	{
		return sourceCode;
	}
	public int sourceMajor()
	{
		return sourceCode&CMMsg.MAJOR_MASK;
	}
	public int sourceMinor()
	{
		return sourceCode&CMMsg.MINOR_MASK;
	}
	public String sourceMessage()
	{
		return sourceMsg;
	}


	public int othersMajor()
	{
		return othersCode&CMMsg.MAJOR_MASK;
	}
	public int othersMinor()
	{
		return othersCode&CMMsg.MINOR_MASK;
	}
	public int othersCode()
	{
		return othersCode;
	}
	public String othersMessage()
	{
		return othersMsg;
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
