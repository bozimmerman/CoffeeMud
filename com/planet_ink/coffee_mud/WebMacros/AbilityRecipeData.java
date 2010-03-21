package com.planet_ink.coffee_mud.WebMacros;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
public class AbilityRecipeData extends StdWebMacro
{
    public String name()    {return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

    // valid parms include help, ranges, quality, target, alignment, domain,
    // qualifyQ, auto
    public String runMacro(ExternalHTTPRequests httpReq, String parm)
    {
        Hashtable parms=parseParms(parm);
        
        String replaceCommand=httpReq.getRequestParameter("REPLACE");
        if((replaceCommand != null) 
        && (replaceCommand.length()>0)
        && (replaceCommand.indexOf('=')>0))
        {
            int eq=replaceCommand.indexOf('=');
            String field=replaceCommand.substring(0,eq);
            String value=replaceCommand.substring(eq+1);
            httpReq.addRequestParameters(field, value);
            httpReq.addRequestParameters("REPLACE","");
        }
        
        String last=httpReq.getRequestParameter("ABILITY");
        if(last==null) return " @break@";
        
        String rownum=httpReq.getRequestParameter("ABILITYRECIPEROW");
        
        if(last.length()>0)
        {
            Ability A=CMClass.getAbility(last);
            if((A!=null)
            &&(A instanceof ItemCraftor)
            &&(((ItemCraftor)A).parametersFile()!=null)
            &&(((ItemCraftor)A).parametersFile().length()>0)
            &&(((ItemCraftor)A).parametersFormat()!=null)
            &&(((ItemCraftor)A).parametersFormat().length()>0))
            {
                AbilityParameters.AbilityRecipeData recipeData = 
                    (AbilityParameters.AbilityRecipeData)httpReq.getRequestObjects().get("ABILITYRECIPEDATA-"+last);
                if(recipeData == null)
                {
                    recipeData = CMLib.ableParms().parseRecipe(((ItemCraftor)A).parametersFile(),((ItemCraftor)A).parametersFormat());
                    if(recipeData.parseError() != null)
                    {
                        Log.errOut(ID(),recipeData.parseError());
                        return " @break@";
                    }
                    httpReq.getRequestObjects().put("ABILITYRECIPEDATA-"+last,recipeData);
                }
                StringBuffer str=new StringBuffer("");
                String font = null;
                if(font==null) font="<FONT COLOR=WHITE>";
                String sfont=(parms.containsKey("FONT"))?("<FONT "+((String)parms.get("FONT"))+">"):"";
                String efont=(parms.containsKey("FONT"))?"</FONT>":"";
                String hfont = null;
                if(hfont==null) hfont="<FONT COLOR=WHITE><B>";
                String hsfont=(parms.containsKey("HFONT"))?("<FONT "+((String)parms.get("HFONT"))+">"):"";
                String hefont=(parms.containsKey("HFONT"))?"</FONT>":"";
                
                if(parms.containsKey("SAVETOVFS"))
                {
                    if(httpReq.isRequestParameter("SAVETOVFS"))
                        str.append(CMath.s_bool(httpReq.getRequestParameter("SAVETOVFS"))?"CHECKED":"");
                    else
                        str.append(recipeData.wasVFS()?"CHECKED":"");
                }
                else
                if(parms.containsKey("ROWTABLE")&&(CMath.isInteger(rownum)))
                {
                    int row = CMath.s_int(rownum);
                    DVector dataRow = null;
                    int classFieldIndex = recipeData.getClassFieldIndex();
                    if((row>0)&&((row-1)<recipeData.dataRows().size()))
                        dataRow = (DVector)recipeData.dataRows().elementAt(row-1);
                    else
                        dataRow=recipeData.newRow(httpReq.getRequestParameter("CLASSFIELD"));
                    str.append("\n\r<TABLE WIDTH=100% BORDER=1 CELLSPACING=0 CELLPADDING=0>");
                    for(int c=0;c<dataRow.size();c++)
                    {
                        AbilityParameters.AbilityParmEditor editor =
                            (AbilityParameters.AbilityParmEditor)CMLib.ableParms().getEditors().get((String)dataRow.elementAt(c,1));
                        String oldVal = (String)dataRow.elementAt(c,2);
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
                        String oldVal = (String)dataRow.elementAt(classFieldIndex,2);
                        AbilityParameters.AbilityParmEditor editor =
                            (AbilityParameters.AbilityParmEditor)CMLib.ableParms().getEditors().get((String)dataRow.elementAt(classFieldIndex,1));
                        str.append("<INPUT TYPE=HIDDEN NAME=CLASSFIELD VALUE=\""+editor.webValue(httpReq,parms,oldVal,"CLASSFIELD")+"\">");
                    }
                }
                else
                if(parms.containsKey("ADDROW"))
                {
                    AbilityParameters.AbilityParmEditor classFieldEditor = null;
                    int cfIndex = recipeData.getClassFieldIndex(); 
                    for(int row=0;row<recipeData.dataRows().size();row++)
                    {
                        if(cfIndex>=0)
                        {   
                            DVector dataRow = (DVector)recipeData.dataRows().elementAt(row);
                            classFieldEditor = (AbilityParameters.AbilityParmEditor)CMLib.ableParms().getEditors().get((String)dataRow.elementAt(cfIndex,1));
                        }
                    }
                    if(classFieldEditor != null)
                        str.append(classFieldEditor.webField(httpReq,parms,classFieldEditor.defaultValue(),"NEWCLASSFIELD"));
                }
                else
                if(parms.containsKey("SAVEROW")&&(CMath.isInteger(rownum)))
                {
                    DVector dataRow = null;
                    int row = CMath.s_int(rownum);
                    if((row-1>=0)&&(row-1<recipeData.dataRows().size()))
                        dataRow = (DVector)recipeData.dataRows().elementAt(row-1);
                    else
                    {
                        dataRow=recipeData.newRow(httpReq.getRequestParameter("CLASSFIELD"));
                        recipeData.dataRows().addElement(dataRow);
                    }
                    for(int c=0;c<dataRow.size();c++)
                    {
                        AbilityParameters.AbilityParmEditor editor =
                            (AbilityParameters.AbilityParmEditor)CMLib.ableParms().getEditors().get((String)dataRow.elementAt(c,1));
                        String oldVal = (String)dataRow.elementAt(c,2);
                        dataRow.setElementAt(c,2,editor.webValue(httpReq,parms,oldVal,"DATA_"+row+"_"+c));
                    }
            		MOB M = Authenticate.getAuthenticatedMob(httpReq);
                    if(M==null) return " @break@";
                    boolean saveToVFS = CMath.s_bool(httpReq.getRequestParameter("SAVETOVFS"));
                    if(CMSecurity.isAllowedAnywhere(M,"CMDRECIPES"))
                        CMLib.ableParms().resaveRecipeFile(M,recipeData.recipeFilename(),recipeData.dataRows(),recipeData.columns(), saveToVFS);
                    else
                        return " @break@";
                }
                else
                if(parms.containsKey("DELROW")&&(CMath.isInteger(rownum)))
                {
                    int row = CMath.s_int(rownum);
                    if((row-1>=0)&&(row-1<recipeData.dataRows().size()))
                        recipeData.dataRows().removeElementAt(row-1);
                    else
                        return " @break@";
            		MOB M = Authenticate.getAuthenticatedMob(httpReq);
                    if(M==null) return " @break@";
                    boolean saveToVFS = CMath.s_bool(httpReq.getRequestParameter("SAVETOVFS"));
                    if(CMSecurity.isAllowedAnywhere(M,"CMDRECIPES"))
                        CMLib.ableParms().resaveRecipeFile(M,recipeData.recipeFilename(),recipeData.dataRows(),recipeData.columns(), saveToVFS);
                    else
                        return " @break@";
                }
                else
                if(parms.containsKey("TABLE"))
                {
                    str.append("\n\r<TABLE WIDTH=100% BORDER=1 CELLSPACING=0 CELLPADDING=0>");
                    int currLenTotal = 0;
                    for(int l=0;l<recipeData.columnLengths().length;l++)
                        currLenTotal+=recipeData.columnLengths()[l];
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
                        DVector dataRow = (DVector)recipeData.dataRows().elementAt(r);
                        str.append("\n\r<TR>");
                        str.append("<TD>");
                        str.append("<A HREF=\"javascript:Select("+(r+1)+")\">" + sfont + "<B><FONT COLOR=YELLOW>"+(r+1)+"</FONT></B>");
                        str.append(efont + "</A>");
                        str.append("</TD>");
                        for(int c=0;c<dataRow.size();c++)
                        {
                            str.append("<TD>" + sfont);
                            String val = (String)dataRow.elementAt(c,2);
                            AbilityParameters.AbilityParmEditor editor =
                                (AbilityParameters.AbilityParmEditor)CMLib.ableParms().getEditors().get((String)dataRow.elementAt(c,1));
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
