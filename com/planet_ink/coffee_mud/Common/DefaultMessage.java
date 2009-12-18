package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class DefaultMessage implements CMMsg
{
    public String ID(){return "DefaultMessage";}
    protected static final Hashtable MSGTYPE_DESCS=new Hashtable();
    public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new DefaultMessage();}}
    public void initializeClass(){}
    public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
    public CMObject copyOf()
    {
        try
        {
            return (DefaultMessage)this.clone();
        }
        catch(CloneNotSupportedException e)
        {
            return newInstance();
        }
    }
    
	protected int targetCode=0;
	protected int sourceCode=0;
	protected int othersCode=0;
	protected String targetMsg=null;
	protected String othersMsg=null;
	protected String sourceMsg=null;
	protected MOB myAgent=null;
	protected Environmental myTarget=null;
	protected Environmental myTool=null;
	protected int value=0;
	protected Vector trailMsgs=null;

    public void finalize() throws Throwable
    {
        targetCode=0;
        sourceCode=0;
        othersCode=0;
        targetMsg=null;
        othersMsg=null;
        sourceMsg=null;
        myAgent=null;
        myTarget=null;
        myTool=null;
        trailMsgs=null;
        value=0;
        if(!CMClass.returnMsg(this))
            super.finalize();
    }
    
	public void modify(MOB source, Environmental target, int newAllCode, String allMessage)
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
    
    public void modify(MOB source, int newAllCode, String allMessage)
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
    
    public void modify(MOB source, int newAllCode, String allMessage, int newValue)
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
    
    public void modify(MOB source, Environmental target, Environmental tool, int newAllCode, String allMessage)
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

    public void modify(MOB source,
    				   Environmental target,
    				   Environmental tool,
    				   int newAllCode,
    				   String sourceMessage,
    				   String targetMessage,
    				   String othersMessage)
	{
		myAgent=source;
		myTarget=target;
		myTool=tool;
		sourceMsg=sourceMessage;
		targetMsg=targetMessage;
		targetCode=newAllCode;
		sourceCode=newAllCode;
		othersCode=newAllCode;
		othersMsg=othersMessage;
	}

    protected static synchronized Hashtable getMSGTYPE_DESCS()
    {
        if(MSGTYPE_DESCS.size()!=0) return MSGTYPE_DESCS;
        for(int i=0;i<CMMsg.TYPE_DESCS.length;i++)
            MSGTYPE_DESCS.put(CMMsg.TYPE_DESCS[i],Integer.valueOf(i));
        for(int i=0;i<CMMsg.MASK_DESCS.length;i++)
            MSGTYPE_DESCS.put(CMMsg.MASK_DESCS[i],Integer.valueOf((int)CMath.pow(2,11+i)));
        for(int i=0;i<CMMsg.MISC_DESCS.length;i++)
            MSGTYPE_DESCS.put(CMMsg.MISC_DESCS[i][0],CMMsg.MISC_DESCS[i][1]);
        return MSGTYPE_DESCS;
    }
    
    public void setSourceCode(int code){sourceCode=code;}
    public void setTargetCode(int code){targetCode=code;}
    public void setOthersCode(int code){othersCode=code;}
    public void setSourceMessage(String str){sourceMsg=str;}
    public void setTargetMessage(String str){targetMsg=str;}
    public void setOthersMessage(String str){othersMsg=str;}

	public int value(){return value;}
	public void setValue(int amount)
    {
        value=amount;
    }
	
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
    public void modify(MOB source,
    				   Environmental target,
    				   Environmental tool,
    				   int newSourceCode,
    				   int newTargetCode,
    				   int newOthersCode,
    				   String allMessage)
	{
		myAgent=source;
		myTarget=target;
		myTool=tool;
		targetMsg=allMessage;
		sourceMsg=allMessage;
		targetCode=newTargetCode;
		sourceCode=newSourceCode;
		othersCode=newOthersCode;
		othersMsg=allMessage;
	}
	public MOB source(){ return myAgent; }
    public void setSource(MOB mob){myAgent=mob;}
	public Environmental target() { return myTarget; }
    public void setTarget(Environmental E){myTarget=E;}
	public Environmental tool() { return myTool; }
    public void setTool(Environmental E){myTool=E;}
	public int targetMajor() { return targetCode&CMMsg.MAJOR_MASK; }
	public int targetMinor() { return targetCode&CMMsg.MINOR_MASK; }
	public int targetCode() { return targetCode; }
	public String targetMessage() { return targetMsg;}
	public int sourceCode() { return sourceCode; }
	public int sourceMajor() { return sourceCode&CMMsg.MAJOR_MASK;}
	public int sourceMinor() { return sourceCode&CMMsg.MINOR_MASK;}
	public String sourceMessage() { return sourceMsg;}
	public int othersMajor() { return othersCode&CMMsg.MAJOR_MASK; }
	public int othersMinor() { return othersCode&CMMsg.MINOR_MASK; }
	public int othersCode() {  return othersCode; }
	public String othersMessage() { return othersMsg; }
	public boolean amITarget(Environmental thisOne){ return ((thisOne!=null)&&(thisOne==target()));}
	public boolean amISource(MOB thisOne){return ((thisOne!=null)&&(thisOne==source()));}
    public boolean isTarget(Environmental E){return amITarget(E);}
    public boolean isTarget(int codeOrMask){return matches(targetCode,codeOrMask);}
    public boolean isTarget(String codeOrMaskDesc){return matches(targetCode,codeOrMaskDesc);}
    public boolean isSource(Environmental E){return (E instanceof MOB)?amISource((MOB)E):false;}
    public boolean isSource(int codeOrMask){return matches(sourceCode,codeOrMask);}
    public boolean isSource(String codeOrMaskDesc){return matches(sourceCode,codeOrMaskDesc);}
    public boolean isOthers(Environmental E){return (!isTarget(E))&&(!isSource(E));}
    public boolean isOthers(int codeOrMask){return matches(othersCode,codeOrMask);}
    public boolean isOthers(String codeOrMaskDesc){return matches(othersCode,codeOrMaskDesc);}
    
    protected static boolean matches(int code1, int code2){return ((code1&CMMsg.MINOR_MASK)==code2)||((code1&CMMsg.MAJOR_MASK)==code2);}
    protected static boolean matches(int code1, String code2)
    {
        Integer I=(Integer)getMSGTYPE_DESCS().get(code2.toUpperCase());
        if(I==null)
        {
            code2=code2.toUpperCase();
            for(int i=0;i<TYPE_DESCS.length;i++)
                if(code2.startsWith(TYPE_DESCS[i]))
                { I=Integer.valueOf(i); break;}
            if(I==null)
            for(int i=0;i<TYPE_DESCS.length;i++)
                if(TYPE_DESCS[i].startsWith(code2))
                { I=Integer.valueOf(i); break;}
            if(I==null)
            for(int i=0;i<MASK_DESCS.length;i++)
                if(code2.startsWith(MASK_DESCS[i]))
                { I=Integer.valueOf((int)CMath.pow(2,11+i)); break;}
            if(I==null)
            for(int i=0;i<MASK_DESCS.length;i++)
                if(MASK_DESCS[i].startsWith(code2))
                { I=Integer.valueOf((int)CMath.pow(2,11+i)); break;}
            if(I==null)
            for(int i=0;i<MISC_DESCS.length;i++)
                if(code2.startsWith((String)MISC_DESCS[i][0]))
                { I=(Integer)MISC_DESCS[i][1]; break;}
            if(I==null)
            for(int i=0;i<MISC_DESCS.length;i++)
                if(((String)MISC_DESCS[i][0]).startsWith(code2))
                { I=(Integer)MISC_DESCS[i][1]; break;}
            if(I==null) return false;
        }
        return matches(code1,I.intValue());
    }
    
}
