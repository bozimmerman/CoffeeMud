package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.miniweb.interfaces.*;
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
import java.util.*;

/* 
   Copyright 2000-2014 Bo Zimmerman

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
public class CatalogMobNext extends StdWebMacro
{
	public String name() { return "CatalogMobNext"; }
	public boolean isAdminMacro()   {return true;}

	final static String[] DATA={
						"CATALOG_MOB_USAGE"
					   ,"CATALOG_MOB_NAME"
					   ,"CATALOG_MOB_AREA"
					   ,"CATALOG_MOB_RACE"
					   ,"CATALOG_MOB_GENDER"
					   ,"CATALOG_MOB_LEVEL"
					   ,"CATALOG_MOB_CLASS"
	};
	
	public static String getCataStat(MOB M, CatalogLibrary.CataData data, int x, String optionalColumn)
	{
		if((M==null)||(data==null)) 
			return "";
		switch(x)
		{
		case 0: return ""+data.numReferences();
		case 1: return M.name();
		case 2: return ""+data.mostPopularArea();
		case 3: return M.baseCharStats().raceName();
		case 4: return M.baseCharStats().genderName();
		case 5: return ""+M.basePhyStats().level();
		case 6: return M.ID();
		default:
			if((optionalColumn!=null)&&(optionalColumn.length()>0))
			{
				if(M.isStat(optionalColumn))
					return M.getStat(optionalColumn);
				if(M.basePhyStats().isStat(optionalColumn))
					return M.basePhyStats().getStat(optionalColumn);
				if(M.baseCharStats().isStat(optionalColumn))
					return M.baseCharStats().getStat(optionalColumn);
				if(M.baseState().isStat(optionalColumn))
					return M.baseState().getStat(optionalColumn);
			}
			break;
		}
		return "";
	}
		
	public String runMacro(HTTPRequest httpReq, String parm)
	{
		java.util.Map<String,String> parms=parseParms(parm);
		String last=httpReq.getUrlParameter("MOB");
		String catagory=httpReq.getUrlParameter("CATACAT");
		if(catagory!=null)
		{
			if(catagory.equalsIgnoreCase("UNCATEGORIZED"))
				catagory="";
			else
			if(catagory.length()==0)
				catagory=null;
			else
				catagory=catagory.toUpperCase().trim();
		}
		String optCol=httpReq.getUrlParameter("OPTIONALCOLUMN");
		final String optionalColumn;
		if(optCol==null)
			optionalColumn="";
		else
			optionalColumn=optCol.trim().toUpperCase();
		if(parms.containsKey("RESET"))
		{   
			if(last!=null)
				httpReq.removeUrlParameter("MOB");
			for(int d=0;d<DATA.length;d++)
				httpReq.removeUrlParameter(DATA[d]);
			if(optionalColumn.length()>0)
				httpReq.removeUrlParameter("CATALOG_MOB_"+optionalColumn);
			return "";
		}
		String lastID="";
		MOB M=null;
		String name=null;
		CatalogLibrary.CataData data=null;
		String[] names=CMLib.catalog().getCatalogMobNames(catagory);
		String sortBy=httpReq.getUrlParameter("SORTBY");
		if((sortBy!=null)&&(sortBy.length()>0))
		{
			final int sortIndex=CMParms.indexOf(DATA, "CATALOG_MOB_"+sortBy.toUpperCase());
			if((sortIndex>=0)||(sortBy.equalsIgnoreCase(optionalColumn)))
			{
				String[] sortedNames=(String[])httpReq.getRequestObjects().get("CATALOG_MOB_"+catagory+"_"+sortBy.toUpperCase());
				if(sortedNames!=null)
					names=sortedNames;
				else
				{
					Object[] sortifiable=new Object[names.length];
					for(int s=0;s<names.length;s++)
						sortifiable[s]=new Object[]{
							names[s], 
							CMLib.catalog().getCatalogMob(names[s]), 
							CMLib.catalog().getCatalogMobData(names[s])};
					Arrays.sort(sortifiable,new Comparator(){
						public int compare(Object o1, Object o2) {
							Object[] O1=(Object[])o1;
							Object[] O2=(Object[])o2;
							String s1=getCataStat((MOB)O1[1],(CatalogLibrary.CataData)O1[2],sortIndex,optionalColumn);
							String s2=getCataStat((MOB)O2[1],(CatalogLibrary.CataData)O2[2],sortIndex,optionalColumn);
							if(CMath.isNumber(s1)&&CMath.isNumber(s2))
								return Double.valueOf(CMath.s_double(s1)).compareTo(Double.valueOf(CMath.s_double(s2)));
							else
								return s1.toLowerCase().compareTo(s2.toLowerCase());
						}
					});
					for(int s=0;s<names.length;s++)
						names[s]=(String)((Object[])sortifiable[s])[0];
					httpReq.getRequestObjects().put("CATALOG_MOB_"+catagory+"_"+sortBy.toUpperCase(),names);
				}
			}
		}
		for(int s=0;s<names.length;s++)
		{
			name="CATALOG-"+names[s].toUpperCase().trim();
			if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!name.equalsIgnoreCase(lastID))))
			{
				data=CMLib.catalog().getCatalogMobData(names[s]);
				M=CMLib.catalog().getCatalogMob(names[s]);
				if(M==null) continue;
				httpReq.addFakeUrlParameter("MOB",name);
				for(int d=0;d<DATA.length;d++)
					httpReq.addFakeUrlParameter(DATA[d],getCataStat(M,data,d,null));
				if(optionalColumn.length()>0)
					httpReq.addFakeUrlParameter("CATALOG_MOB_"+optionalColumn,getCataStat(M,data,-1,optionalColumn));
				return "";
			}
			lastID=name;
		}
		httpReq.addFakeUrlParameter("MOB","");
		for(int d=0;d<DATA.length;d++)
			httpReq.addFakeUrlParameter(DATA[d],"");
		if(optionalColumn.length()>0)
			httpReq.addFakeUrlParameter("CATALOG_MOB_"+optionalColumn,"");
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		return " @break@";
	}
}
