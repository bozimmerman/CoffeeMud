package com.planet_ink.coffee_mud.core.database;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/*
   Copyright 2014-2018 Bo Zimmerman

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
public class DBUpgradeConversions
{

	private static void pl(PrintStream out, String str)
	{
		if(out!=null) 
			out.println(str);
	}

	private static void p(PrintStream out, String str)
	{
		if(out!=null) 
			out.print(str);
	}
	
	public static void DBUpgradeConversionV1(
											Map<String,List<String>> oldTables,
											Map<String,List<String>> newTables,
											Map<String,List<List<String>>> data,
											PrintStream out)
	{
		// first, look for the CLAN conversion
		if(newTables.containsKey("CMCHCL") && (!oldTables.containsKey("CMCHCL")))
		{
			final List<List<String>> charRows=data.get("CMCHAR");
			if(charRows != null)
			{
				List<List<String>> cmchclRows=data.get("CMCHCL");
				if(cmchclRows==null)
				{
					cmchclRows=new Vector<List<String>>();
					data.put("CMCHCL", cmchclRows);
				}
				pl(out," ");
				pl(out," ");
				p(out,"Making CMCHCL conversion: ");
				final List<String> ofields=oldTables.get("CMCHAR");
				final List<String> nfields=newTables.get("CMCHCL");
				final int cmUserIDIndex=ofields.indexOf("$CMUSERID");
				final int cmClanIndex=ofields.indexOf("$CMCLAN");
				final int cmClRoIndex=ofields.indexOf("#CMCLRO");
				final int cm2UserIDIndex=nfields.indexOf("$CMUSERID");
				final int cm2ClanIndex=nfields.indexOf("$CMCLAN");
				final int cm2ClRoIndex=nfields.indexOf("#CMCLRO");
				for(int r=0;r<charRows.size();r++)
				{
					final List<String> row=charRows.get(r);
					final String userID=row.get(cmUserIDIndex);
					final String clanID=row.get(cmClanIndex);
					final String clanRo=row.get(cmClRoIndex);
					if((clanID==null)||(clanID.length()==0))
						continue;
					final Vector<String> newRow=new Vector<String>(3);
					newRow.add(""); newRow.add(""); newRow.add("");
					newRow.add(cm2UserIDIndex,userID);
					newRow.add(cm2ClanIndex,clanID);
					newRow.add(cm2ClRoIndex,clanRo);
					cmchclRows.add(newRow);
				}
				if(cmchclRows.size()>0)
					oldTables.put("CMCHCL", newTables.get("CMCHCL"));
			}
		}
		
		// now look for cmchid insertion
		if(newTables.containsKey("CMCHAR") && (oldTables.containsKey("CMCHAR")))
		{
			final List<List<String>> charRows=data.get("CMCHAR");
			if(charRows != null)
			{
				final List<String> ofields=oldTables.get("CMCHAR");
				final List<String> nfields=newTables.get("CMCHAR");
				if(nfields.contains("$CMCHID") && (!ofields.contains("$CMCHID")))
				{
					ofields.add("$CMCHID");
					pl(out," ");
					pl(out," ");
					p(out,"Making CMCHID conversion: ");
					for(int r=0;r<charRows.size();r++)
					{
						final List<String> row=charRows.get(r);
						row.add("StdMOB");
					}
				}
			}
		}
		
	}
}
