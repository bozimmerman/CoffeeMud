package com.planet_ink.coffee_mud.WebMacros;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_web.interfaces.*;

import java.util.*;

/*
   Copyright 2008-2018 Bo Zimmerman

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
@SuppressWarnings({"unchecked","rawtypes"})
public class AbilityRecipeData extends StdWebMacro
{
	@Override
	public String name()
	{
		return "AbilityRecipeData";
	}

	// valid parms include help, ranges, quality, target, alignment, domain,
	// qualifyQ, auto
	@Override
	public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);

		final String replaceCommand=httpReq.getUrlParameter("REPLACE");
		if((replaceCommand != null)
		&& (replaceCommand.length()>0)
		&& (replaceCommand.indexOf('=')>0))
		{
			final int eq=replaceCommand.indexOf('=');
			final String field=replaceCommand.substring(0,eq);
			final String value=replaceCommand.substring(eq+1);
			httpReq.addFakeUrlParameter(field, value);
			httpReq.addFakeUrlParameter("REPLACE","");
		}

		final String last=httpReq.getUrlParameter("ABILITY");
		if(last==null)
			return " @break@";

		final String rownum=httpReq.getUrlParameter("ABILITYRECIPEROW");

		if(last.length()>0)
		{
			final Ability A=CMClass.getAbility(last);
			if((A!=null)
			&&(A instanceof CraftorAbility)
			&&(((CraftorAbility)A).parametersFile()!=null)
			&&(((CraftorAbility)A).parametersFile().length()>0)
			&&(((CraftorAbility)A).parametersFormat()!=null)
			&&(((CraftorAbility)A).parametersFormat().length()>0))
			{
				AbilityParameters.AbilityRecipeData recipeData =
					(AbilityParameters.AbilityRecipeData)httpReq.getRequestObjects().get("ABILITYRECIPEDATA-"+last);
				if(recipeData == null)
				{
					recipeData = CMLib.ableParms().parseRecipe(((CraftorAbility)A).parametersFile(),((CraftorAbility)A).parametersFormat());
					if(recipeData.parseError() != null)
					{
						Log.errOut(ID(),recipeData.parseError());
						return " @break@";
					}
					httpReq.getRequestObjects().put("ABILITYRECIPEDATA-"+last,recipeData);
				}
				final StringBuffer str=new StringBuffer("");
				final String sfont=(parms.containsKey("FONT"))?("<FONT "+(parms.get("FONT"))+">"):"";
				final String efont=(parms.containsKey("FONT"))?"</FONT>":"";
				final String hsfont=(parms.containsKey("HFONT"))?("<FONT "+(parms.get("HFONT"))+">"):"";
				final String hefont=(parms.containsKey("HFONT"))?"</FONT>":"";

				if(parms.containsKey("SAVETOVFS"))
				{
					if(httpReq.isUrlParameter("SAVETOVFS"))
						str.append(CMath.s_bool(httpReq.getUrlParameter("SAVETOVFS"))?"CHECKED":"");
					else
						str.append(recipeData.wasVFS()?"CHECKED":"");
				}
				else
				if(parms.containsKey("ROWTABLE")&&(CMath.isInteger(rownum)))
				{
					final int row = CMath.s_int(rownum);
					DVector dataRow = null;
					final int classFieldIndex = recipeData.getClassFieldIndex();
					if((row>0)&&((row-1)<recipeData.dataRows().size()))
						dataRow = recipeData.dataRows().get(row-1);
					else
						dataRow=recipeData.newRow(httpReq.getUrlParameter("CLASSFIELD"));
					str.append("\n\r<TABLE WIDTH=100% BORDER=1 CELLSPACING=0 CELLPADDING=0>");
					for(int c=0;c<dataRow.size();c++)
					{
						final AbilityParameters.AbilityParmEditor editor =
							CMLib.ableParms().getEditors().get(dataRow.elementAt(c,1));
						final String oldVal = (String)dataRow.elementAt(c,2);
						if(!editor.ID().equalsIgnoreCase("N_A"))
						{
							str.append("\n\r<TR>");
							str.append("<TD WIDTH=20%>" + hsfont + editor.prompt() + hefont + "</TD>");
							if(c==classFieldIndex)
								str.append("<TD>" + sfont + editor.webValue(httpReq,parms,oldVal,"DATA_"+row+"_"+c) + efont + "</TD>");
							else
								str.append("<TD>" + sfont + editor.webField(httpReq,parms,oldVal,"DATA_"+row+"_"+c) + efont + "</TD>");
							str.append("</TR>");
						}
					}
					str.append("\n\r</TABLE>");
					if(classFieldIndex>=0)
					{
						final String oldVal = (String)dataRow.elementAt(classFieldIndex,2);
						final AbilityParameters.AbilityParmEditor editor =
							CMLib.ableParms().getEditors().get(dataRow.elementAt(classFieldIndex,1));
						str.append("<INPUT TYPE=HIDDEN NAME=CLASSFIELD VALUE=\""+editor.webValue(httpReq,parms,oldVal,"CLASSFIELD")+"\">");
					}
				}
				else
				if(parms.containsKey("ADDROW"))
				{
					AbilityParameters.AbilityParmEditor classFieldEditor = null;
					final int cfIndex = recipeData.getClassFieldIndex();
					if(recipeData.dataRows().size()==0)
					{
						final DVector editRow = new DVector(2);
						for(int c=0;c<recipeData.columns().size();c++)
						{
							if(recipeData.columns().get(c) instanceof List)
								editRow.addElement(recipeData.columns().get(c),"");
						}
						final List<String> o=(List)editRow.elementAt(cfIndex,1);
						classFieldEditor = CMLib.ableParms().getEditors().get(o.get(0).toString());
					}
					else
					for(int row=0;row<recipeData.dataRows().size();row++)
					{
						if(cfIndex>=0)
						{
							final DVector dataRow = recipeData.dataRows().get(row);
							classFieldEditor = CMLib.ableParms().getEditors().get(dataRow.elementAt(cfIndex,1));
						}
					}
					if(classFieldEditor != null)
						str.append(classFieldEditor.webField(httpReq,parms,classFieldEditor.defaultValue(),"NEWCLASSFIELD"));
				}
				else
				if(parms.containsKey("SAVEROW")&&(CMath.isInteger(rownum)))
				{
					DVector dataRow = null;
					final int row = CMath.s_int(rownum);
					if((row-1>=0)&&(row-1<recipeData.dataRows().size()))
						dataRow = recipeData.dataRows().get(row-1);
					else
					{
						dataRow=recipeData.newRow(httpReq.getUrlParameter("CLASSFIELD"));
						recipeData.dataRows().add(dataRow);
					}
					for(int c=0;c<dataRow.size();c++)
					{
						final AbilityParameters.AbilityParmEditor editor =
							CMLib.ableParms().getEditors().get(dataRow.elementAt(c,1));
						final String oldVal = (String)dataRow.elementAt(c,2);
						String newVal = editor.webValue(httpReq,parms,oldVal,"DATA_"+row+"_"+c);
						if(newVal != null)
							newVal = newVal.replace('\'', '`');
						dataRow.setElementAt(c,2,newVal);
					}
					final MOB M = Authenticate.getAuthenticatedMob(httpReq);
					if(M==null)
						return " @break@";
					final boolean saveToVFS = CMath.s_bool(httpReq.getUrlParameter("SAVETOVFS"));
					if(CMSecurity.isAllowedAnywhere(M,CMSecurity.SecFlag.CMDRECIPES))
						CMLib.ableParms().resaveRecipeFile(M,recipeData.recipeFilename(),recipeData.dataRows(),recipeData.columns(), saveToVFS);
					else
						return " @break@";
				}
				else
				if(parms.containsKey("DELROW")&&(CMath.isInteger(rownum)))
				{
					final int row = CMath.s_int(rownum);
					if((row-1>=0)&&(row-1<recipeData.dataRows().size()))
						recipeData.dataRows().remove(row-1);
					else
						return " @break@";
					final MOB M = Authenticate.getAuthenticatedMob(httpReq);
					if(M==null)
						return " @break@";
					final boolean saveToVFS = CMath.s_bool(httpReq.getUrlParameter("SAVETOVFS"));
					if(CMSecurity.isAllowedAnywhere(M,CMSecurity.SecFlag.CMDRECIPES))
						CMLib.ableParms().resaveRecipeFile(M,recipeData.recipeFilename(),recipeData.dataRows(),recipeData.columns(), saveToVFS);
					else
						return " @break@";
				}
				else
				if(parms.containsKey("TABLE"))
				{
					str.append("\n\r<TABLE WIDTH=100% BORDER=1 CELLSPACING=0 CELLPADDING=0>");
					//int currLenTotal = 0;
					//for(int l=0;l<recipeData.columnLengths().length;l++)
					//    currLenTotal+=recipeData.columnLengths()[l];
					str.append("\n\r<TR>");
					str.append("<TD WIDTH=1%>" + hsfont + "#" + hefont + "</TD>");
					for(int c=0;c<recipeData.columnHeaders().length;c++)
					{
						str.append("<TD WIDTH="+Math.round(CMath.div(recipeData.columnLengths()[c],72) * 100.0)+"%>");
						str.append(hsfont + recipeData.columnHeaders()[c] + hefont);
						str.append("</TD>");
					}
					str.append("</TR>");
					for(int r=0;r<recipeData.dataRows().size();r++)
					{
						final DVector dataRow = recipeData.dataRows().get(r);
						str.append("\n\r<TR>");
						str.append("<TD>");
						str.append("<A HREF=\"javascript:Select("+(r+1)+")\">" + sfont + "<B><FONT COLOR=YELLOW>"+(r+1)+"</FONT></B>");
						str.append(efont + "</A>");
						str.append("</TD>");
						for(int c=0;c<dataRow.size();c++)
						{
							str.append("<TD>" + sfont);
							String val = (String)dataRow.elementAt(c,2);
							final AbilityParameters.AbilityParmEditor editor =
								CMLib.ableParms().getEditors().get(dataRow.elementAt(c,1));
							val = editor.webTableField(httpReq, parms, val);
							str.append(CMStrings.limit(val,(int)Math.round(CMath.div(recipeData.columnLengths()[c],36) * 100.0)));
							str.append(efont + "</TD>");
						}
						str.append("</A></TR>");
					}
					str.append("\n\r</TABLE>");
				}
				String strstr=str.toString();
				if(strstr.endsWith(", "))
					strstr=strstr.substring(0,strstr.length()-2);
				return clearWebMacros(strstr);
			}
		}
		return "";
	}
}
