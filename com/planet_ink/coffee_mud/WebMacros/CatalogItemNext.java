package com.planet_ink.coffee_mud.WebMacros;
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
public class CatalogItemNext extends StdWebMacro
{
    public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
    public boolean isAdminMacro()   {return true;}
    static final String[] DATA={
					            "CATALOG_ITEM_NAME",
					            "CATALOG_ITEM_USAGE",
					            "CATALOG_ITEM_LEVEL",
					            "CATALOG_ITEM_CLASS",
					            "CATALOG_ITEM_VALUE",
					            "CATALOG_ITEM_RATE",
					            "CATALOG_ITEM_MASK",
					            "CATALOG_ITEM_LIVE",
					            "CATALOG_ITEM_AREA",
    };

    public static String getCataStat(Item I, CatalogLibrary.CataData data, int x, String optionalColumn)
    {
    	if((I==null)||(data==null)) return "";
        boolean dataRate=(data.getRate()>0.0);
    	switch(x)
    	{
    	case 0: return I.Name();
    	case 1: return ""+data.numReferences();
    	case 2: return ""+I.baseEnvStats().level();
    	case 3: return I.ID();
    	case 4: return ""+I.baseGoldValue();
    	case 5: return (dataRate)?CMath.toPct(data.getRate()):"";
    	case 6: return (dataRate)?(data.getMaskStr()==null?"":data.getMaskStr()):"";
    	case 7: return (dataRate)?(""+data.getWhenLive()):"";
    	case 8: return ""+data.mostPopularArea();
    	default:
    		if((optionalColumn!=null)&&(optionalColumn.length()>0))
    		{
    			if(I.isStat(optionalColumn))
    				return I.getStat(optionalColumn);
    			if(I.baseEnvStats().isStat(optionalColumn))
    				return I.baseEnvStats().getStat(optionalColumn);
    		}
    		break;
    	}
    	return "";
    }
    
    public String runMacro(ExternalHTTPRequests httpReq, String parm)
    {
        Hashtable parms=parseParms(parm);
        String last=httpReq.getRequestParameter("ITEM");
        String optCol=httpReq.getRequestParameter("OPTIONALCOLUMN");
        final String optionalColumn;
        if(optCol==null)
        	optionalColumn="";
        else
        	optionalColumn=optCol.trim().toUpperCase();
        if(parms.containsKey("RESET"))
        {   
            if(last!=null) httpReq.removeRequestParameter("ITEM");
            for(int d=0;d<DATA.length;d++)
	            httpReq.removeRequestParameter(DATA[d]);
            if(optionalColumn.length()>0)
	            httpReq.removeRequestParameter("CATALOG_ITEM_"+optionalColumn);
            return "";
        }
        String lastID="";
        Item I=null;
        String name=null;
        CatalogLibrary.CataData data=null;
        String[] names=CMLib.catalog().getCatalogItemNames();
        String sortBy=httpReq.getRequestParameter("SORTBY");
        if((sortBy!=null)&&(sortBy.length()>0))
        {
        	String[] sortedNames=(String[])httpReq.getRequestObjects().get("CATALOG_ITEM_"+sortBy.toUpperCase());
        	if(sortedNames!=null)
        		names=sortedNames;
        	else
        	{
	        	final int sortIndex=CMParms.indexOf(DATA, "CATALOG_ITEM_"+sortBy.toUpperCase());
	        	if((sortIndex>=0)||(sortBy.equalsIgnoreCase(optionalColumn)))
	        	{
	        		Object[] sortifiable=new Object[names.length];
	        		for(int s=0;s<names.length;s++)
	        			sortifiable[s]=new Object[]{
	        				names[s], 
	        				CMLib.catalog().getCatalogItem(names[s]), 
	        				CMLib.catalog().getCatalogItemData(names[s])};
	            	Arrays.sort(sortifiable,new Comparator() {
						public int compare(Object o1, Object o2) {
							Object[] O1=(Object[])o1;
							Object[] O2=(Object[])o2;
							String s1=getCataStat((Item)O1[1],(CatalogLibrary.CataData)O1[2],sortIndex, optionalColumn);
							String s2=getCataStat((Item)O2[1],(CatalogLibrary.CataData)O2[2],sortIndex, optionalColumn);
							if(CMath.isNumber(s1)&&CMath.isNumber(s2))
								return Double.valueOf(CMath.s_double(s1)).compareTo(Double.valueOf(CMath.s_double(s2)));
							else
								return s1.toLowerCase().compareTo(s2.toLowerCase());
						}
	            	});
	            	for(int s=0;s<names.length;s++)
	            		names[s]=(String)((Object[])sortifiable[s])[0];
	            	httpReq.getRequestObjects().put("CATALOG_ITEM_"+sortBy.toUpperCase(),names);
	        	}
        	}
        }
        for(int s=0;s<names.length;s++)
        {
            name="CATALOG-"+names[s].toUpperCase().trim();
            if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!name.equalsIgnoreCase(lastID))))
            {
                data=CMLib.catalog().getCatalogItemData(names[s]);
                I=CMLib.catalog().getCatalogItem(names[s]);
                if(I==null) continue;
                httpReq.addRequestParameters("ITEM",name);
                for(int d=0;d<DATA.length;d++)
    	            httpReq.addRequestParameters(DATA[d],getCataStat(I,data,d,null));
                if(optionalColumn.length()>0)
                    httpReq.addRequestParameters("CATALOG_ITEM_"+optionalColumn,getCataStat(I,data,-1,optionalColumn));
                return "";
            }
            lastID=name;
        }
        httpReq.addRequestParameters("ITEM","");
        for(int d=0;d<DATA.length;d++)
            httpReq.addRequestParameters(DATA[d],"");
        if(optionalColumn.length()>0)
            httpReq.addRequestParameters("CATALOG_ITEM_"+optionalColumn,"");
        if(parms.containsKey("EMPTYOK"))
            return "<!--EMPTY-->";
        return " @break@";
    }
}