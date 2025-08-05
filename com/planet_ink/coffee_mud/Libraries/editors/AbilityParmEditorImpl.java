package com.planet_ink.coffee_mud.Libraries.editors;

import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.AbilityMapping;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityParameters.AbilityParmEditor;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityParameters.ParmType;
import com.planet_ink.coffee_mud.core.exceptions.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent.CompConnector;
import com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent.CompLocation;
import com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent.CompType;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.RawMaterial.Material;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
   Copyright 2008-2025 Bo Zimmerman

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

public abstract class AbilityParmEditorImpl implements AbilityParmEditor
{
	private final String	ID;
	private final ParmType	fieldType;
	private String			prompt	= null;
	private String			header	= null;

	protected PairList<String, String>	choices	= null;

	public AbilityParmEditorImpl(final String fieldName, final String shortHeader, final ParmType type)
	{
		ID=fieldName;
		fieldType = type;
		header = shortHeader;
		prompt = CMStrings.capitalizeAndLower(CMStrings.replaceAll(ID,"_"," "));
		createChoices();
	}

	@Override
	public String ID()
	{
		return ID;
	}

	@Override
	public ParmType parmType()
	{
		return fieldType;
	}

	@Override
	public String prompt()
	{
		return prompt;
	}

	@Override
	public String colHeader()
	{
		return header;
	}

	@Override
	public int maxColWidth()
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public int minColWidth()
	{
		return 0;
	}

	@Override
	public boolean confirmValue(final String oldVal)
	{
		final boolean spaceOK = fieldType != ParmType.ONEWORD;
		boolean emptyOK = false;
		switch(fieldType)
		{
		case STRINGORNULL:
			emptyOK = true;
		//$FALL-THROUGH$
		case ONEWORD:
		case STRING:
		{
			if((!spaceOK) && (oldVal.indexOf(' ') >= 0))
				return false;
			return (emptyOK)||(oldVal.trim().length()>0);
		}
		case NUMBERORNULL:
			if(oldVal.length()==0)
				return true;
			//$FALL-THROUGH$
		case NUMBER:
			return CMath.isInteger(oldVal);
		case NUMBER_PAIR:
		{
			if(oldVal==null)
				return emptyOK;
			final int x=oldVal.indexOf(',');
			return CMath.isInteger(oldVal)
				||((x>0)&&(CMath.isInteger(oldVal.substring(0,x).trim()))&&(CMath.isInteger(oldVal.substring(x+1).trim())));
		}
		case CHOICES:
			if(!CMStrings.contains(choices.toArrayFirst(new String[0]),oldVal))
				return CMStrings.contains(choices.toArrayFirst(new String[0]),oldVal.toUpperCase().trim());
			return true;
		case MULTICHOICES:
			return CMath.isInteger(oldVal)||choices().containsFirst(oldVal);
		case SPECIAL:
			break;
		}
		return false;
	}

	@Override
	public String[] fakeUserInput(final String oldVal)
	{
		boolean emptyOK = false;
		switch(fieldType)
		{
		case STRINGORNULL:
			emptyOK = true;
		//$FALL-THROUGH$
		case ONEWORD:
		case STRING:
		{
			if(emptyOK && (oldVal.trim().length()==0))
				return new String[]{"NULL"};
			return new String[]{oldVal};
		}
		case NUMBERORNULL:
			if(oldVal.trim().length()==0)
				return new String[]{"NULL"};
			//$FALL-THROUGH$
		case NUMBER:
		case NUMBER_PAIR:
			return new String[]{oldVal};
		case CHOICES:
		{
			if(oldVal.trim().length()==0) return new String[]{"NULL"};
			final Vector<String> V = new XVector<String>(choices.toArrayFirst(new String[0]));
			for(int v=0;v<V.size();v++)
			{
				if(oldVal.equalsIgnoreCase(V.elementAt(v)))
					return new String[]{choices.get(v).second};
			}
			return new String[]{oldVal};
		}
		case MULTICHOICES:
			if(oldVal.trim().length()==0)
				return new String[]{"NULL"};
			if(!CMath.isInteger(oldVal))
			{
				final Vector<String> V = new XVector<String>(choices.toArrayFirst(new String[0]));
				for(int v=0;v<V.size();v++)
				{
					if(oldVal.equalsIgnoreCase(V.elementAt(v)))
						return new String[]{choices.get(v).second,""};
				}
			}
			else
			{
				final Vector<String> V = new Vector<String>();
				for(int c=0;c<choices.size();c++)
				{
					if(CMath.bset(CMath.s_int(oldVal),CMath.s_int(choices.get(c).first)))
					{
						V.addElement(choices.get(c).second);
						V.addElement(choices.get(c).second);
					}
				}
				if(V.size()>0)
				{
					V.addElement("");
					return CMParms.toStringArray(V);
				}
			}
			return new String[]{"NULL"};
		case SPECIAL:
			break;
		}
		return new String[]{};
	}

	@Override
	public String commandLinePrompt(final MOB mob, final String oldVal, final int[] showNumber, final int showFlag)
	throws java.io.IOException
	{
		String str = null;
		boolean emptyOK = false;
		final boolean spaceOK = fieldType != ParmType.ONEWORD;
		switch(fieldType)
		{
		case STRINGORNULL:
			emptyOK = true;
		//$FALL-THROUGH$
		case ONEWORD:
		case STRING:
		{
			++showNumber[0];
			boolean proceed = true;
			while(proceed&&(mob.session()!=null)&&(!mob.session().isStopped()))
			{
				str = CMLib.genEd().prompt(mob,oldVal,showNumber[0],showFlag,prompt(),emptyOK).trim();
				if((!spaceOK) && (str.indexOf(' ') >= 0))
					mob.tell(CMLib.lang().L("Spaces are not allowed here."));
				else
					proceed=false;
			}
			break;
		}
		case NUMBERORNULL:
		case NUMBER:
		{
			final String newStr=CMLib.genEd().prompt(mob,oldVal,++showNumber[0],showFlag,prompt(),true);
			if(newStr.trim().length()==0)
				str="";
			else
				str = Integer.toString(CMath.s_int(newStr));
			break;
		}
		case NUMBER_PAIR:
		{
			final String newStr=CMLib.genEd().prompt(mob,oldVal,++showNumber[0],showFlag,prompt(),true);
			if(newStr.trim().length()==0)
				str="";
			else
			if(CMath.isInteger(newStr))
				str = Integer.toString(CMath.s_int(newStr));
			else
			{
				final int x=newStr.indexOf(',');
				if(x>0)
				{
					str = Integer.toString(CMath.s_int(newStr.substring(0,x).trim()))
						+ Integer.toString(CMath.s_int(newStr.substring(x+1).trim()));
				}
			}
			break;
		}
		case CHOICES:
			str = CMLib.genEd().promptMultiOrExtra(mob,oldVal,++showNumber[0],showFlag,prompt(),choices);
			break;
		case MULTICHOICES:
			str = CMLib.genEd().promptMultiOrExtra(mob,oldVal,++showNumber[0],showFlag,prompt(),choices);
			if(CMath.isInteger(str))
				str = Integer.toString(CMath.s_int(str));
			break;
		case SPECIAL:
			break;
		}
		return str;
	}

	@Override
	public String webValue(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
	{
		final String webValue = httpReq.getUrlParameter(fieldName);
		switch(fieldType)
		{
		case ONEWORD:
		case STRINGORNULL:
		case STRING:
		case NUMBER:
		case NUMBERORNULL:
		case NUMBER_PAIR:
			return (webValue == null)?oldVal:webValue;
		case MULTICHOICES:
		{
			if(webValue == null)
				return oldVal;
			String id="";
			long num=0;
			int index=0;
			for(;httpReq.isUrlParameter(fieldName+id);id=""+(++index))
			{
				final String newVal = httpReq.getUrlParameter(fieldName+id);
				if(CMath.s_long(newVal)<=0)
					return newVal;
				num |= CMath.s_long(newVal);
			}
			return ""+num;
		}
		case CHOICES:
			return (webValue == null)?oldVal:webValue;
		case SPECIAL:
			break;
		}
		return "";
	}

	@Override
	public String webTableField(final HTTPRequest httpReq, final java.util.Map<String, String> parms, final String oldVal)
	{
		return oldVal;
	}

	@Override
	public String commandLineValue(final String oldVal)
	{
		return oldVal;
	}

	@Override
	public String webField(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
	{
		int textSize = 50;
		final String webValue = webValue(httpReq,parms,oldVal,fieldName);
		String onChange = null;
		final Vector<String> choiceValues = new Vector<String>();
		switch(fieldType)
		{
		case ONEWORD:
			textSize = 10;
		//$FALL-THROUGH$
		case STRINGORNULL:
		case STRING:
			return "\n\r<INPUT TYPE=TEXT NAME=" + fieldName + " SIZE=" + textSize + " VALUE=\"" + webValue + "\">";
		case NUMBER:
		case NUMBERORNULL:
		case NUMBER_PAIR:
			return "\n\r<INPUT TYPE=TEXT NAME=" + fieldName + " SIZE=10 VALUE=\"" + webValue + "\">";
		case MULTICHOICES:
		{
			onChange = " MULTIPLE ";
			if(!parms.containsKey("NOSELECT"))
				onChange+= "ONCHANGE=\"MultiSelect(this);\"";
			if(CMath.isInteger(webValue))
			{
				final int bits = CMath.s_int(webValue);
				for(int i=0;i<choices.size();i++)
				{
					final int bitVal =CMath.s_int(choices.get(i).first);
					if((bitVal>0)&&(CMath.bset(bits,bitVal)))
						choiceValues.addElement(choices.get(i).first);
				}
			}
		}
		//$FALL-THROUGH$
		case CHOICES:
		{
			if(choiceValues.size()==0)
				choiceValues.addElement(webValue);
			if((onChange == null)&&(!parms.containsKey("NOSELECT")))
				onChange = " ONCHANGE=\"Select(this);\"";
			else
			if(onChange==null)
				onChange="";
			final StringBuffer str= new StringBuffer("");
			str.append("\n\r<SELECT NAME="+fieldName+onChange+">");
			for(int i=0;i<choices.size();i++)
			{
				final String option = (choices.get(i).first);
				str.append("<OPTION VALUE=\""+option+"\" ");
				for(int c=0;c<choiceValues.size();c++)
				{
					if(option.equalsIgnoreCase(choiceValues.elementAt(c)))
						str.append("SELECTED");
				}
				str.append(">"+(choices.get(i).second));
			}
			return str.toString()+"</SELECT>";
		}
		case SPECIAL:
			break;
		}
		return "";
	}

	public abstract void createChoices();

	@Override
	public PairList<String,String> createChoices(final Enumeration<? extends Object> e)
	{
		if(choices != null)
			return choices;
		choices = new PairVector<String,String>();
		Object o = null;
		for(;e.hasMoreElements();)
		{
			o = e.nextElement();
			if(o instanceof String)
				choices.add((String)o,CMStrings.capitalizeAndLower((String)o));
			else
			if(o instanceof Ability)
				choices.add(((Ability)o).ID(),((Ability)o).name());
			else
			if(o instanceof Race)
				choices.add(((Race)o).ID(),((Race)o).name());
			else
			if(o instanceof Environmental)
				choices.add(((Environmental)o).ID(),((Environmental)o).ID());
		}
		return choices;
	}

	@SuppressWarnings("unchecked")
	@Override
	public PairList<String,String> createChoices(final List<? extends Object> V)
	{
		return createChoices(new IteratorEnumeration<Object>((Iterator<Object>)V.iterator()));
	}

	@Override
	public PairList<String,String> createChoices(final String[] S)
	{
		final XVector<String> X=new XVector<String>(S);
		Collections.sort(X);
		return createChoices(X.elements());
	}

	public PairList<String,String> createBinaryChoices(final String[] S)
	{
		if(choices != null)
			return choices;
		choices = createChoices(new XVector<String>(S).elements());
		for(int i=0;i<choices.size();i++)
		{
			if(i==0)
				choices.get(i).first =Integer.toString(0);
			else
				choices.get(i).first = Integer.toString(1<<(i-1));
		}
		return choices;
	}

	public PairList<String,String> createNumberedChoices(final String[] S)
	{
		if(choices != null)
			return choices;
		choices = createChoices(new XVector<String>(S).elements());
		for(int i=0;i<choices.size();i++)
			choices.get(i).first = Integer.toString(i);
		return choices;
	}

	@Override
	public PairList<String, String> choices()
	{
		return choices;
	}

	@Override
	public int appliesToClass(final Object o)
	{
		return 0;
	}

	@SuppressWarnings("unchecked")
	protected Pair<String[],String[]> getBuildingCodesNFlags()
	{
		Pair<String[],String[]> codesFlags = (Pair<String[],String[]>)Resources.getResource("BUILDING_SKILL_CODES_FLAGS");
		if(codesFlags == null)
		{
			RecipeDriven A=(RecipeDriven)CMClass.getAbility("Masonry");
			if(A==null)
				A=(RecipeDriven)CMClass.getAbility("Construction");
			if(A==null)
				A=(RecipeDriven)CMClass.getAbility("Excavation");
			if(A!=null)
				A.getRecipeFormat();
			codesFlags = (Pair<String[],String[]>)Resources.getResource("BUILDING_SKILL_CODES_FLAGS");
		}
		return codesFlags;
	}

	public String L(final String str, final String... xs)
	{
		return CMLib.lang().fullSessionTranslation(getClass(), str, xs);
	}

	public String L(final Class<?> clazz, final String str, final String ... xs)
	{
		return CMLib.lang().fullSessionTranslation(clazz, str, xs);
	}
}
