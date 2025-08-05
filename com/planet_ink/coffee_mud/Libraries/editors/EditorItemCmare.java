package com.planet_ink.coffee_mud.Libraries.editors;
import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.AbilityMapping;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityParameters.*;
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
import com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.RawMaterial.*;
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
public class EditorItemCmare extends AbilityParmEditorImpl
{
	public EditorItemCmare()
	{
		super("ITEM_CMARE",CMLib.lang().L("Items"),ParmType.SPECIAL);
	}

	@Override
	public void createChoices()
	{
	}

	@Override
	public String commandLineValue(final String oldVal)
	{
		if(oldVal.trim().startsWith("<"))
		{
			final List<XMLLibrary.XMLTag> xml = CMLib.xml().parseAllXML(oldVal);
			if(xml.size()>0)
			{
				final String nameXML=CMLib.xml().getValFromPieces(xml.get(0).contents(), "ITEXT");
				final List<XMLLibrary.XMLTag> nameTags=CMLib.xml().parseAllXML(CMLib.xml().restoreAngleBrackets(nameXML));
				final String name=CMLib.xml().getValFromPieces(nameTags, "NAME");
				final String classID=CMLib.xml().getValFromPieces(xml.get(0).contents(), "ICLAS");
				return name+" ("+classID+")";
			}
		}
		return oldVal;
	}

	@Override
	public String defaultValue()
	{
		return "<ITEM></ITEM>";
	}

	@Override
	public String convertFromItem(final ItemCraftor A, final Item I)
	{
		if(I==null)
			return defaultValue();
		return CMLib.coffeeMaker().getItemXML(I);
	}

	@Override
	public String prompt()
	{
		return "Item";
	}

	@Override
	public boolean confirmValue(final String oldVal)
	{
		return ((oldVal != null)
				&&(oldVal.length()>0)
				&&(oldVal.trim().startsWith("<")));
	}

	@Override
	public String webTableField(final HTTPRequest httpReq, final java.util.Map<String, String> parms, final String oldVal)
	{
		final String val=oldVal;

		if((val != null)
		&&(val.length()>0)
		&&(val.trim().startsWith("<")))
		{
			final List<XMLLibrary.XMLTag> xml = CMLib.xml().parseAllXML(oldVal);
			if(xml.size()>0)
			{
				final String nameXML=CMLib.xml().getValFromPieces(xml.get(0).contents(), "ITEXT");
				final List<XMLLibrary.XMLTag> nameTags=CMLib.xml().parseAllXML(CMLib.xml().restoreAngleBrackets(nameXML));
				final String name=CMLib.xml().getValFromPieces(nameTags, "NAME");
				final String classID=CMLib.xml().getValFromPieces(xml.get(0).contents(), "ICLAS");
				return name+" ("+classID+")";
			}
		}
		return oldVal;
	}

	@Override
	public String webValue(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
	{
		if(fieldName.equalsIgnoreCase("NEWCLASSFIELD"))
		{
			return "";
		}
		else
		if(fieldName.equalsIgnoreCase("CLASSFIELD"))
		{
			if(oldVal.trim().startsWith("<"))
			{
				final List<XMLLibrary.XMLTag> pieces=CMLib.xml().parseAllXML(oldVal);
				if(pieces.size()>0)
				{
					final XMLLibrary.XMLTag tag = CMLib.xml().getPieceFromPieces(pieces, "ITEM");
					if(tag != null)
					{
						String realClassID=CMLib.xml().getValFromPieces(tag.contents(), "ICLAS");
						if(realClassID == null)
							realClassID=CMLib.xml().getValFromPieces(tag.contents(), "CLASSID");
						if((realClassID!=null)
						&&(realClassID.length()>0))
							return realClassID;
					}
				}
			}
			return oldVal;
		}
		else
		if(fieldName.startsWith("DATA_"))
		{
			if(httpReq.isUrlParameter("ITEM"))
			{
				final String itemID = httpReq.getUrlParameter("ITEM");
				final Item I=CMLib.webMacroFilter().getItemFromWebCache(itemID);
				if(I!=null)
					return CMLib.coffeeMaker().getItemXML(I);
			}
			String rowNum=fieldName.substring(5);
			final int x=rowNum.indexOf('_');
			if(x>0)
				rowNum=rowNum.substring(0,x);
			Item I=null;
			if(oldVal.trim().startsWith("<"))
			{
				final List<Item> madeItem=new ArrayList<Item>(1);
				final String error=CMLib.coffeeMaker().addItemsFromXML("<ITEMS>"+oldVal+"</ITEMS>", madeItem, null);
				if(((error == null)||(error.length()==0))
				&&(madeItem.size()>0))
				{
					I=madeItem.get(0);
					CMLib.threads().unTickAll(I);
				}
			}
			else
				I=CMClass.getItem(oldVal);
			if(I!=null)
			{
				CMLib.webMacroFilter().contributeItemsToWebCache(new XVector<Item>(I));
				final String cachedID = CMLib.webMacroFilter().findItemWebCacheCode(I);
				final StringBuilder data = new StringBuilder("");
				data.append("<FONT COLOR=WHITE>"+I.Name()+" ("+I.ID()+")</FONT>&nbsp;&nbsp;");
				data.append("<INPUT TYPE=HIDDEN NAME=\""+fieldName+"\" VALUE=\""+cachedID+"\">");
				httpReq.addFakeUrlParameter("ONMODIFY", "SwitchToItemEditor('"+cachedID+"','"+rowNum+"')");
				httpReq.addFakeUrlParameter("HIDESAVEOPTION", "true");
				//data.append("<a href=\"javascript:SwitchToItemEditor('"+cachedID+"','"+rowNum+"');\">Edit Item</a>\n\r ");
				I.destroy();
				return data.toString();
			}
		}

		Log.debugOut("AbilityEditor:ITEM_CMARE:webValue: "+fieldName+": "+oldVal);
		return oldVal;
	}

	@Override
	public String webField(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
	{
		String value=webValue(httpReq,parms,oldVal,fieldName);
		if(value.endsWith("$"))
			value = value.substring(0,oldVal.length()-1);
		value = value.trim();
		if(fieldName.equalsIgnoreCase("NEWCLASSFIELD"))
		{
			final StringBuilder html = new StringBuilder("");
			final List<String> sortMe=new Vector<String>();
			CMClass.addAllItemClassNames(sortMe,true,false,true,Area.THEME_ALLTHEMES);
			Collections.sort(sortMe);
			html.append("<SELECT NAME="+fieldName+" ID="+fieldName+">");
			for (final Object element : sortMe)
				html.append("<OPTION VALUE=\""+(String)element+"\">"+(String)element);
			html.append("</SELECT>");
			return html.toString();
		}
		else
		if(fieldName.equalsIgnoreCase("CLASSFIELD"))
		{
			return "*dunno*";
		}
		Log.debugOut("AbilityEditor:ITEM_CMARE:webField: "+fieldName+": "+oldVal);
		return value.toString();
	}

	@Override
	public String[] fakeUserInput(final String oldVal)
	{
		return new String[] { oldVal };
	}

	@Override
	public String commandLinePrompt(final MOB mob, final String oldVal, final int[] showNumber, final int showFlag) throws java.io.IOException
	{
		++showNumber[0];
		String str = oldVal;
		Item I = null;
		if(oldVal.trim().startsWith("<"))
		{
			final List<Item> madeItem=new ArrayList<Item>(1);
			final String error=CMLib.coffeeMaker().addItemsFromXML("<ITEMS>"+oldVal+"</ITEMS>", madeItem, null);
			if(((error == null)||(error.length()==0))
			&&(madeItem.size()>0))
			{
				I=madeItem.get(0);
				CMLib.threads().unTickAll(I);
			}
		}
		else
			I=CMClass.getItem(oldVal);
		while((mob.session()!=null)&&(!mob.session().isStopped()))
		{
			final String showVal = (I==null)?"None?!":(I.Name()+" ("+I.ID()+")");
			str=CMLib.genEd().prompt(mob,showVal,showNumber[0],showFlag,prompt(),true,"").trim();
			if(str.equals(oldVal)||(str.equals(showVal))||(str.trim().length()==0))
				return oldVal;
			final Item newI = mob.location().findItem(str);
			if(newI == null)
				mob.tell(L("No item '@x1' found in @x2",str,mob.location().displayText(mob)));
			else
				return CMLib.coffeeMaker().getItemXML(newI);
		}
		if(I!=null)
			I.destroy();
		return str;
	}
}
