package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2005-2018 Bo Zimmerman

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
public class DefaultMessage implements CMMsg
{
	@Override
	public String ID()
	{
		return "DefaultMessage";
	}

	@Override
	public String name()
	{
		return ID();
	}

	@Override
	public CMObject newInstance()
	{
		try
		{
			return getClass().newInstance();
		}
		catch (final Exception e)
		{
			return new DefaultMessage();
		}
	}

	@Override
	public void initializeClass()
	{
	}

	@Override
	public int compareTo(CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	protected int				targetMajorMask	= 0;
	protected int				sourceMajorMask	= 0;
	protected int				othersMajorMask	= 0;
	protected int				targetMinorType	= 0;
	protected int				sourceMinorType	= 0;
	protected int				othersMinorType	= 0;
	protected String			targetMsg		= null;
	protected String			othersMsg		= null;
	protected String			sourceMsg		= null;
	protected MOB				myAgent			= null;
	protected Environmental		myTarget		= null;
	protected Environmental		myTool			= null;
	protected int				value			= 0;
	protected List<CMMsg>		trailMsgs		= null;
	protected List<Runnable>	trailRunnables	= null;

	@Override
	public CMObject copyOf()
	{
		try
		{
			final DefaultMessage msg = (DefaultMessage)this.clone();
			if(msg.trailMsgs!=null)
			{
				msg.trailMsgs = new SLinkedList<CMMsg>();
				for(CMMsg msg2 : trailMsgs)
					msg.trailMsgs.add((CMMsg)msg2.copyOf());
			}
			if(msg.trailRunnables!=null)
			{
				msg.trailRunnables = new SLinkedList<Runnable>();
				for(Runnable r : trailRunnables)
					msg.trailRunnables.add(r);
			}
			return msg;
		}
		catch(final CloneNotSupportedException e)
		{
			return newInstance();
		}
	}

	@Override
	protected void finalize() throws Throwable
	{
		targetMajorMask=0;
		sourceMajorMask=0;
		othersMajorMask=0;
		targetMinorType=0;
		sourceMinorType=0;
		othersMinorType=0;
		targetMsg=null;
		othersMsg=null;
		sourceMsg=null;
		myAgent=null;
		myTarget=null;
		myTool=null;
		trailMsgs=null;
		trailRunnables=null;
		value=0;
		if(!CMClass.returnMsg(this))
			super.finalize();
	}

	@Override
	public CMMsg modify(final MOB source, final Environmental target, final int newAllCode, final String allMessage)
	{
		myAgent=source;
		myTarget=target;
		myTool=null;
		sourceMsg=allMessage;
		targetMsg=allMessage;
		targetMajorMask=newAllCode&CMMsg.MAJOR_MASK;
		sourceMajorMask=targetMajorMask;
		othersMajorMask=targetMajorMask;
		targetMinorType=newAllCode&CMMsg.MINOR_MASK;
		sourceMinorType=targetMinorType;
		othersMinorType=targetMinorType;
		othersMsg=allMessage;
		return this;
	}

	@Override
	public CMMsg modify(final String allMessage)
	{
		sourceMsg=allMessage;
		targetMsg=allMessage;
		othersMsg=allMessage;
		return this;
	}

	@Override
	public CMMsg modify(final MOB source, final int newAllCode, final String allMessage)
	{
		myAgent=source;
		myTarget=null;
		myTool=null;
		sourceMsg=allMessage;
		targetMsg=allMessage;
		targetMajorMask=newAllCode&CMMsg.MAJOR_MASK;
		sourceMajorMask=targetMajorMask;
		othersMajorMask=targetMajorMask;
		targetMinorType=newAllCode&CMMsg.MINOR_MASK;
		sourceMinorType=targetMinorType;
		othersMinorType=targetMinorType;
		othersMsg=allMessage;
		return this;
	}

	@Override
	public CMMsg modify(final MOB source, final int newAllCode, final String allMessage, final int newValue)
	{
		 myAgent=source;
		 myTarget=null;
		 myTool=null;
		 sourceMsg=allMessage;
		 targetMsg=allMessage;
		 targetMajorMask=newAllCode&CMMsg.MAJOR_MASK;
		 sourceMajorMask=targetMajorMask;
		 othersMajorMask=targetMajorMask;
		 targetMinorType=newAllCode&CMMsg.MINOR_MASK;
		 sourceMinorType=targetMinorType;
		 othersMinorType=targetMinorType;
		 othersMsg=allMessage;
		 value=newValue;
			return this;
	}

	@Override
	public CMMsg modify(final MOB source, final Environmental target, final Environmental tool,
						final int newAllCode, final String allMessage)
	{
		myAgent=source;
		myTarget=target;
		myTool=tool;
		sourceMsg=allMessage;
		targetMsg=allMessage;
		targetMajorMask=newAllCode&CMMsg.MAJOR_MASK;
		sourceMajorMask=targetMajorMask;
		othersMajorMask=targetMajorMask;
		targetMinorType=newAllCode&CMMsg.MINOR_MASK;
		sourceMinorType=targetMinorType;
		othersMinorType=targetMinorType;
		othersMsg=allMessage;
		return this;
	}

	@Override
	public CMMsg modify(final MOB source,
						final Environmental target,
						final Environmental tool,
						final int newAllCode,
						final String sourceMessage,
						final String targetMessage,
						final String othersMessage)
	{
		myAgent=source;
		myTarget=target;
		myTool=tool;
		sourceMsg=sourceMessage;
		targetMsg=targetMessage;
		targetMajorMask=newAllCode&CMMsg.MAJOR_MASK;
		sourceMajorMask=targetMajorMask;
		othersMajorMask=targetMajorMask;
		targetMinorType=newAllCode&CMMsg.MINOR_MASK;
		sourceMinorType=targetMinorType;
		othersMinorType=targetMinorType;
		othersMsg=othersMessage;
		return this;
	}

	@Override
	public CMMsg setSourceCode(final int code)
	{
		sourceMajorMask=code&CMMsg.MAJOR_MASK;
		sourceMinorType=code&CMMsg.MINOR_MASK;
		return this;
	}
	
	@Override
	public CMMsg setTargetCode(final int code)
	{
		targetMajorMask=code&CMMsg.MAJOR_MASK;
		targetMinorType=code&CMMsg.MINOR_MASK;
		return this;
	}
	
	@Override
	public CMMsg setOthersCode(final int code)
	{
		othersMajorMask=code&CMMsg.MAJOR_MASK;
		othersMinorType=code&CMMsg.MINOR_MASK;
		return this;
	}
	
	@Override 
	public CMMsg setSourceMessage(final String str)
	{
		sourceMsg=str;
		return this;
	}
	
	@Override 
	public CMMsg setTargetMessage(final String str)
	{
		targetMsg=str;
		return this;
	}
	
	@Override 
	public CMMsg setOthersMessage(final String str)
	{
		othersMsg=str;
		return this;
	}

	@Override 
	public int value()
	{
		return value;
	}
	
	@Override
	public CMMsg setValue(final int amount)
	{
		value=amount;
		return this;
	}

	@Override
	public List<CMMsg> trailerMsgs()
	{
		return trailMsgs;
	}

	@Override
	public List<Runnable> trailerRunnables()
	{
		return trailRunnables;
	}

	@Override
	public CMMsg addTrailerMsg(final CMMsg msg)
	{
		if(trailMsgs==null) 
			trailMsgs=new SLinkedList<CMMsg>();
		trailMsgs.add(msg);
		return this;
	}

	@Override
	public CMMsg addTrailerRunnable(final Runnable r)
	{
		if(trailRunnables==null) 
			trailRunnables=new SLinkedList<Runnable>();
		trailRunnables.add(r);
		return this;
	}
	
	@Override
	public CMMsg modify(final MOB source,
						final Environmental target,
						final Environmental tool,
						final int newSourceCode,
						final String sourceMessage,
						final int newTargetCode,
						final String targetMessage,
						final int newOthersCode,
						final String othersMessage)
	{
		myAgent=source;
		myTarget=target;
		myTool=tool;
		sourceMsg=sourceMessage;
		targetMsg=targetMessage;
		targetMajorMask=newTargetCode&CMMsg.MAJOR_MASK;
		sourceMajorMask=newSourceCode&CMMsg.MAJOR_MASK;
		othersMajorMask=newOthersCode&CMMsg.MAJOR_MASK;
		targetMinorType=newTargetCode&CMMsg.MINOR_MASK;
		sourceMinorType=newSourceCode&CMMsg.MINOR_MASK;
		othersMinorType=newOthersCode&CMMsg.MINOR_MASK;
		othersMsg=othersMessage;
		return this;
	}
	
	@Override
	public CMMsg modify(final MOB source,
						final Environmental target,
						final Environmental tool,
						final int newSourceCode,
						final int newTargetCode,
						final int newOthersCode,
						final String allMessage)
	{
		myAgent=source;
		myTarget=target;
		myTool=tool;
		targetMsg=allMessage;
		sourceMsg=allMessage;
		targetMajorMask=newTargetCode&CMMsg.MAJOR_MASK;
		sourceMajorMask=newSourceCode&CMMsg.MAJOR_MASK;
		othersMajorMask=newOthersCode&CMMsg.MAJOR_MASK;
		targetMinorType=newTargetCode&CMMsg.MINOR_MASK;
		sourceMinorType=newSourceCode&CMMsg.MINOR_MASK;
		othersMinorType=newOthersCode&CMMsg.MINOR_MASK;
		othersMsg=allMessage;
		return this;
	}
	
	@Override
	public CMMsg modify(int newAllCode, String allMessage)
	{
		targetMsg=allMessage;
		sourceMsg=allMessage;
		othersMsg=allMessage;
		sourceMajorMask=newAllCode&CMMsg.MAJOR_MASK;
		sourceMinorType=newAllCode&CMMsg.MINOR_MASK;
		targetMajorMask=sourceMajorMask;
		othersMajorMask=sourceMajorMask;
		targetMinorType=sourceMinorType;
		othersMinorType=sourceMinorType;
		return this;
	}

	@Override
	public CMMsg modify(int newSourceCode, String sourceMessage, int newTargetCode, String targetMessage, int newOthersCode, String othersMessage)
	{
		sourceMsg=sourceMessage;
		targetMsg=targetMessage;
		targetMajorMask=newTargetCode&CMMsg.MAJOR_MASK;
		sourceMajorMask=newSourceCode&CMMsg.MAJOR_MASK;
		othersMajorMask=newOthersCode&CMMsg.MAJOR_MASK;
		targetMinorType=newTargetCode&CMMsg.MINOR_MASK;
		sourceMinorType=newSourceCode&CMMsg.MINOR_MASK;
		othersMinorType=newOthersCode&CMMsg.MINOR_MASK;
		othersMsg=othersMessage;
		return this;
	}

	@Override 
	public final MOB source()
	{ 
		return myAgent; 
	}
	
	@Override 
	public final CMMsg setSource(final MOB mob)
	{
		myAgent=mob;
		return this;
	}
	
	@Override 
	public final Environmental target() 
	{ 
		return myTarget; 
	}
	
	@Override 
	public final CMMsg setTarget(final Environmental E)
	{
		myTarget=E;
		return this;
	}
	
	@Override 
	public final Environmental tool() 
	{ 
		return myTool; 
	}
	
	@Override 
	public final CMMsg setTool(final Environmental E)
	{
		myTool=E;
		return this;
	}
	
	@Override 
	public final int targetMajor() 
	{ 
		return targetMajorMask; 
	}
	
	@Override 
	public final int sourceMajor() 
	{ 
		return sourceMajorMask;
	}
	
	@Override 
	public final int othersMajor() 
	{ 
		return othersMajorMask; 
	}
	
	@Override 
	public final boolean targetMajor(final int bitMask) 
	{ 
		return (targetMajorMask&bitMask)==bitMask; 
	}
	
	@Override 
	public final int targetMinor() 
	{ 
		return targetMinorType; 
	}
	
	@Override 
	public final int targetCode() 
	{ 
		return targetMajorMask | targetMinorType; 
	}
	
	@Override 
	public final String targetMessage() 
	{ 
		return targetMsg;
	}
	
	@Override 
	public final int sourceCode() 
	{ 
		return sourceMajorMask | sourceMinorType; 
	}
	
	@Override 
	public final boolean sourceMajor(final int bitMask) 
	{ 
		return (sourceMajorMask&bitMask)==bitMask; 
	}
	
	@Override 
	public final int sourceMinor() 
	{ 
		return sourceMinorType;
	}
	
	@Override 
	public final String sourceMessage() 
	{ 
		return sourceMsg;
	}
	
	@Override 
	public final boolean othersMajor(final int bitMask) 
	{ 
		return (othersMajorMask&bitMask)==bitMask; 
	}
	
	@Override 
	public final int othersMinor() 
	{ 
		return othersMinorType; 
	}
	
	@Override 
	public final int othersCode() 
	{  
		return othersMajorMask | othersMinorType; 
	}
	
	@Override 
	public final String othersMessage() 
	{ 
		return othersMsg; 
	}
	
	@Override  
	public final boolean amITarget(final Environmental thisOne)
	{ 
		return ((thisOne!=null)&&(thisOne==target()));
	}

	@Override  
	public final boolean amISource(final MOB thisOne)
	{
		return ((thisOne!=null)&&(thisOne==source()));
	}

	@Override  
	public final boolean isTarget(final Environmental E)
	{
		return amITarget(E);
	}

	@Override  
	public final boolean isTarget(final int codeOrMask)
	{
		return matches(targetMajorMask, targetMinorType,codeOrMask);
	}

	@Override  
	public final boolean isTarget(final String codeOrMaskDesc)
	{
		return matches(targetMajorMask, targetMinorType,codeOrMaskDesc);
	}

	@Override  
	public final boolean isTargetMajor(final String codeOrMaskDesc)
	{
		return matches(targetMajorMask, -1,codeOrMaskDesc);
	}

	@Override  
	public final boolean isTargetMinor(final String codeOrMaskDesc)
	{
		return matches(0, targetMinorType,codeOrMaskDesc);
	}

	@Override  
	public final boolean isSource(final Environmental E)
	{
		return (E instanceof MOB)?amISource((MOB)E):false;
	}

	@Override  
	public final boolean isSource(final int codeOrMask)
	{
		return matches(sourceMajorMask, sourceMinorType, codeOrMask);
	}

	@Override  
	public final boolean isSource(final String codeOrMaskDesc)
	{
		return matches(sourceMajorMask, sourceMinorType,codeOrMaskDesc);
	}

	@Override  
	public final boolean isSourceMajor(final String codeOrMaskDesc)
	{
		return matches(sourceMajorMask, -1,codeOrMaskDesc);
	}

	@Override  
	public final boolean isSourceMinor(final String codeOrMaskDesc)
	{
		return matches(0, sourceMinorType,codeOrMaskDesc);
	}

	@Override  
	public final boolean isOthers(final Environmental E)
	{
		return (!isTarget(E))&&(!isSource(E));
	}

	@Override  
	public final boolean isOthers(final int codeOrMask)
	{
		return matches(othersMajorMask, othersMinorType, codeOrMask);
	}

	@Override  
	public final boolean isOthers(final String codeOrMaskDesc)
	{
		return matches(othersMajorMask, othersMinorType, codeOrMaskDesc);
	}

	@Override  
	public final boolean isOthersMajor(final String codeOrMaskDesc)
	{
		return matches(othersMajorMask, -1, codeOrMaskDesc);
	}

	@Override  
	public final boolean isOthersMinor(final String codeOrMaskDesc)
	{
		return matches(0, othersMinorType, codeOrMaskDesc);
	}

	protected static final boolean matches(final int major, final int minor, final int code)
	{
		return ((major & code)==code) || (minor == code);
	}
	
	protected static final boolean matches(final int major, final int minor, String code2)
	{
		Integer I;
		if(major <= 0)
		{
			int i=CMParms.indexOf(TYPE_DESCS, code2.toUpperCase());
			I=(i<0)?null:Integer.valueOf(i);
		}
		else
		if(minor < 0)
		{
			int i=CMParms.indexOf(MASK_DESCS, code2.toUpperCase());
			I=(i<0)?null:Integer.valueOf((int)CMath.pow(2,11+i));
		}
		else
			I=Desc.getMSGTYPE_DESCS().get(code2.toUpperCase());
		if(I==null)
		{
			code2=code2.toUpperCase();
			if(minor >= 0)
			{
				for(int i=0;i<TYPE_DESCS.length;i++)
				{
					if(code2.startsWith(TYPE_DESCS[i]))
					{ 
						I=Integer.valueOf(i); 
						break;
					}
				}
			}
			if((I==null)&&(minor >= 0))
			{
				for(int i=0;i<TYPE_DESCS.length;i++)
				{
					if(TYPE_DESCS[i].startsWith(code2))
					{ 
						I=Integer.valueOf(i); 
						break;
					}
				}
			}
			if((I==null)&&(major > 0))
			{
				for(int i=0;i<MASK_DESCS.length;i++)
				{
					if(code2.startsWith(MASK_DESCS[i]))
					{ 
						I=Integer.valueOf((int)CMath.pow(2,11+i)); 
						break;
					}
				}
			}
			if((I==null)&&(major > 0))
			{
				for(int i=0;i<MASK_DESCS.length;i++)
				{
					if(MASK_DESCS[i].startsWith(code2))
					{ 
						I=Integer.valueOf((int)CMath.pow(2,11+i)); 
						break;
					}
				}
			}
			if(I==null)
			{
				for (final Object[] element : MISC_DESCS)
					if(code2.startsWith((String)element[0]))
					{ 
						I=(Integer)element[1]; 
						break;
					}
			}
			if(I==null)
			{
				for (final Object[] element : MISC_DESCS)
					if(((String)element[0]).startsWith(code2))
					{ 
						I=(Integer)element[1]; 
						break;
					}
			}
			if(I==null)
				return false;
		}
		return matches(major, minor, I.intValue());
	}

	@Override
	public boolean equals(Object o)
	{
		if(o instanceof CMMsg)
		{
			final CMMsg m=(CMMsg)o;
			return (m.sourceCode()==sourceCode())
					&&(m.targetCode()==targetCode())
					&&(m.othersCode()==othersCode())
					&&(m.source()==source())
					&&(m.target()==target())
					&&(m.tool()==tool())
					&&((m.sourceMessage()==sourceMessage())||((sourceMessage()!=null)&&(sourceMessage().equals(m.sourceMessage()))))
					&&((m.targetMessage()==targetMessage())||((targetMessage()!=null)&&(targetMessage().equals(m.targetMessage()))))
					&&((m.othersMessage()==othersMessage())||((othersMessage()!=null)&&(othersMessage().equals(m.othersMessage()))));
		}
		else
			return super.equals(o);
	}

	@Override
	public int hashCode()
	{
		return super.hashCode();
	}

	@Override
	public String toFlatString()
	{
		final StringBuilder str=new StringBuilder("");
		str.append(targetMajorMask).append(",");
		str.append(sourceMajorMask).append(",");
		str.append(othersMajorMask).append(",");
		str.append(targetMinorType).append(",");
		str.append(sourceMinorType).append(",");
		str.append(othersMinorType).append(",");
		if(myAgent == null)
			str.append(",");
		else
			str.append(myAgent.ID()).append(":").append(CMStrings.replaceAll(myAgent.Name(),",","&comma;")).append(",");
		if(myTarget == null)
			str.append(",");
		else
			str.append(myTarget.ID()).append(":").append(CMStrings.replaceAll(myTarget.Name(),",","&comma;")).append(",");
		if(myTool == null)
			str.append(",");
		else
			str.append(myTool.ID()).append(":").append(CMStrings.replaceAll(myTool.Name(),",","&comma;")).append(",");
		str.append(sourceMsg != null ? CMStrings.replaceAll(sourceMsg,",","&comma;") : "&null;").append(",");
		str.append(targetMsg != null ? CMStrings.replaceAll(targetMsg,",","&comma;") : "&null;").append(",");
		str.append(othersMsg != null ? CMStrings.replaceAll(othersMsg,",","&comma;") : "&null;");
		return str.toString();
	}

	protected CMObject parseFlatObject(final String part, final CMClass.CMObjectType preferClass)
	{
		if((part==null)||(part.length()==0))
			return null;
		final String[] subParts=part.split(":",2);
		if(subParts.length < 2)
			return null;
		if(subParts[0].equals("StdMOB") || subParts[0].equals("StdRideable"))
		{
			final MOB M=CMLib.players().getLoadPlayer(subParts[1]);
			if(M != null)
				return M;
		}
		CMObject o;
		o = CMClass.getCommon(subParts[0]);
		if(o == null)
			o = CMClass.getByType(subParts[0], preferClass);
		if(o == null)
			o = CMClass.getUnknown(subParts[0]);
		if((o == null) || (o instanceof MOB))
			o = CMClass.getFactoryMOB();
		if(o instanceof Social)
			o = CMLib.socials().fetchSocial(subParts[1], true);
		else
		if((o instanceof Ability) && (myAgent != null))
		{
			Ability eA=myAgent.fetchEffect(subParts[1]);
			Ability A = null;
			if((eA != null) && (eA.invoker() == myAgent))
				o = eA;
			else
			{
				A = myAgent.fetchAbility(subParts[1]);
				if(A != null)
					o = A;
				else
				if(eA != null)
					o = eA;
			}
		}
		else
		if((o instanceof Environmental)&&(!o.name().equals(subParts[1])))
			((Environmental)o).setName(subParts[1]);
		if((o instanceof MOB)&&(((MOB)o).location()==null))
			((MOB)o).setLocation(CMLib.map().getRandomRoom());
		return o;
	}
	
	@Override
	public void parseFlatString(final String flat)
	{
		final String[] parts=flat.split(",");
		if(parts.length < 12 )
			throw new IllegalArgumentException("Wrong number of commas in argument: "+flat);
		targetMajorMask = CMath.s_int(parts[0]);
		sourceMajorMask = CMath.s_int(parts[1]);
		othersMajorMask = CMath.s_int(parts[2]);
		targetMinorType = CMath.s_int(parts[3]);
		sourceMinorType = CMath.s_int(parts[4]);
		othersMinorType = CMath.s_int(parts[5]);
		CMObject o;
		o = parseFlatObject(parts[6], CMClass.CMObjectType.MOB);
		if(o instanceof MOB)
			myAgent = (MOB)o;
		else
			throw new IllegalArgumentException("Agent is not a MOB: "+parts[6]);
		o = parseFlatObject(parts[7], CMClass.CMObjectType.MOB);
		if((o==null) || (o instanceof Environmental))
			myTarget = (Environmental)o;
		else
			throw new IllegalArgumentException("Target is not an Environmental: "+parts[7]);
		o = parseFlatObject(parts[8], CMClass.CMObjectType.ABILITY);
		if((o==null) || (o instanceof Environmental))
			myTool = (Environmental)o;
		else
			throw new IllegalArgumentException("Tool is not an Environmental: "+parts[8]);
		sourceMsg=parts[9].equals("&null;") ? null : CMStrings.replaceAll(parts[9],"&comma;",",");
		targetMsg=parts[10].equals("&null;") ? null : CMStrings.replaceAll(parts[10],"&comma;",",");
		othersMsg=parts[11].equals("&null;") ? null : CMStrings.replaceAll(parts[11],"&comma;",",");
	}
	
	@Override
	public boolean sameAs(CMMsg E)
	{
		if(E==null)
			return false;
		if((E.source() != source())
		||(E.target() != target())
		||(E.tool() != tool())
		||(E.sourceCode()!=sourceCode())
		||(E.targetCode()!=targetCode())
		||(E.othersCode()!=othersCode())
		||(!(""+E.sourceMessage()).equals(sourceMessage()))
		||(!(""+E.targetMessage()).equals(targetMessage()))
		||(!(""+E.othersMessage()).equals(othersMessage())))
			return false;
		return true;
	}
}
