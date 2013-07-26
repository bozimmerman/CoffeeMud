package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.database.DBConnection;
import com.planet_ink.coffee_mud.core.database.DBConnector;
import com.planet_ink.coffee_mud.core.database.DBInterface;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/* 
   Copyright 2000-2013 Bo Zimmerman

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
public class Merge extends StdCommand
{
	public Merge(){}

	private final String[] access={"MERGE"};
	public String[] getAccessWords(){return access;}

	public static String getStat(Environmental E, String stat)
	{
		if((stat!=null)
		&&(stat.length()>0)
		&&(stat.equalsIgnoreCase("REJUV"))
		&&(E instanceof Physical))
		{
			if(((Physical)E).basePhyStats().rejuv()==PhyStats.NO_REJUV)
				return "0";
			return ""+((Physical)E).basePhyStats().rejuv();
		}
		return ((Physical)E).getStat(stat);
	}

	public static void setStat(Environmental E, String stat, String value)
	{
		if((stat!=null)
		&&(stat.length()>0)
		&&(stat.equalsIgnoreCase("REJUV"))
		&&(E instanceof Physical))
			((Physical)E).basePhyStats().setRejuv(CMath.s_int(value));
		else
			E.setStat(stat,value);
	}

	public static void mergedebugtell(MOB mob, String msg)
	{
		if(mob!=null) mob.tell(msg);
		Log.sysOut("MERGE",msg);
	}

	protected static boolean tryMerge(MOB mob,
									  Room room,
									  Environmental E,
									  List things,
									  List<String> changes,
									  List<String> onfields,
									  List<String> ignore,
									  boolean noisy)
	{
		boolean didAnything=false;
		List<String> efields=new Vector();
		List<String> allMyFields=new Vector();
		String[] EFIELDS=E.getStatCodes();
		for(int i=0;i<EFIELDS.length;i++)
			if(!efields.contains(EFIELDS[i]))
				efields.add(EFIELDS[i]);
		efields.add("REJUV");
		allMyFields=new XVector<String>(efields);
		for(int v=0;v<ignore.size();v++)
			if(efields.contains(ignore.get(v)))
				efields.remove(ignore.get(v));
		for(int v=0;v<changes.size();v++)
			if(efields.contains(changes.get(v)))
				efields.remove(changes.get(v));
		if(noisy) mergedebugtell(mob,"AllMy-"+CMParms.toStringList(allMyFields));
		if(noisy) mergedebugtell(mob,"efields-"+CMParms.toStringList(efields));
		for(int t=0;t<things.size();t++)
		{
			Environmental E2=(Environmental)things.get(t);
			if(noisy) mergedebugtell(mob,E.name()+"/"+E2.name()+"/"+CMClass.classID(E)+"/"+CMClass.classID(E2));
			if(CMClass.classID(E).equals(CMClass.classID(E2)))
			{
				Vector fieldsToCheck=null;
				if(onfields.size()>0)
				{
					fieldsToCheck=new Vector();
					for(int v=0;v<onfields.size();v++)
						if(efields.contains(onfields.get(v)))
							fieldsToCheck.addElement(onfields.get(v));
				}
				else
					fieldsToCheck=new XVector<String>(efields);

				boolean checkedOut=fieldsToCheck.size()>0;
				if(noisy) mergedebugtell(mob,"fieldsToCheck-"+CMParms.toStringList(fieldsToCheck));
				if(checkedOut)
				for(int i=0;i<fieldsToCheck.size();i++)
				{
					String field=(String)fieldsToCheck.elementAt(i);
					if(noisy) mergedebugtell(mob,field+"/"+getStat(E,field)+"/"+getStat(E2,field)+"/"+getStat(E,field).equals(getStat(E2,field)));
					if(!getStat(E,field).equals(getStat(E2,field)))
					{ checkedOut=false; break;}
				}
				if(checkedOut)
				{
					List<String> fieldsToChange=null;
					if(changes.size()==0)
						fieldsToChange=new XVector<String>(allMyFields);
					else
					{
						fieldsToChange=new Vector();
						for(int v=0;v<changes.size();v++)
							if(allMyFields.contains(changes.get(v)))
								fieldsToChange.add(changes.get(v));
					}
					if(noisy) mergedebugtell(mob,"fieldsToChange-"+CMParms.toStringList(fieldsToChange));
					for(int i=0;i<fieldsToChange.size();i++)
					{
						String field=fieldsToChange.get(i);
						if(noisy) mergedebugtell(mob,E.name()+" wants to change "+field+" value "+getStat(E,field)+" to "+getStat(E2,field)+"/"+(!getStat(E,field).equals(getStat(E2,field))));
						if(!getStat(E,field).equals(getStat(E2,field)))
						{
							setStat(E,field,getStat(E2,field));
							Log.sysOut("Merge","The "+CMStrings.capitalizeAndLower(field)+" field on "+E.Name()+" in "+room.roomID()+" was changed to "+getStat(E2,field)+".");
							didAnything=true;
						}
					}
				}
			}
		}
		if(didAnything)
		{
			if(E instanceof Physical)
				((Physical)E).recoverPhyStats();
			if(E instanceof MOB)
			{
				((MOB)E).recoverCharStats();
				((MOB)E).recoverMaxState();
			}
			E.text();
		}
		return didAnything;
	}
	
	public void sortEnumeratedList(Enumeration e, List<String> allKnownFields, StringBuffer allFieldsMsg)
	{
		for(;e.hasMoreElements();)
		{
			Environmental E=(Environmental)e.nextElement();
			String[] fields=E.getStatCodes();
			for(int x=0;x<fields.length;x++)
				if(!allKnownFields.contains(fields[x]))
				{
					allKnownFields.add(fields[x]);
					allFieldsMsg.append(fields[x]+" ");
				}
		}
	}

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		boolean noisy=CMSecurity.isDebugging(CMSecurity.DbgFlag.MERGE);
		Vector placesToDo=new Vector();
		commands.removeElementAt(0);
		if(commands.size()==0)
		{
			mob.tell("Merge what? Try DATABASE or a filename");
			return false;
		}
		if(mob.isMonster())
		{
			mob.tell("No can do.");
			return false;
		}
		if((commands.size()>0)&&
		   ((String)commands.elementAt(0)).equalsIgnoreCase("noprompt"))
			commands.removeElementAt(0);
		
		if((commands.size()>0)&&
		   ((String)commands.elementAt(0)).equalsIgnoreCase("?"))
		{
			StringBuffer allFieldsMsg=new StringBuffer("");
			Vector allKnownFields=new Vector();
			sortEnumeratedList(CMClass.mobTypes(),allKnownFields,allFieldsMsg);
			sortEnumeratedList(CMClass.basicItems(),allKnownFields,allFieldsMsg);
			sortEnumeratedList(CMClass.weapons(),allKnownFields,allFieldsMsg);
			sortEnumeratedList(CMClass.armor(),allKnownFields,allFieldsMsg);
			sortEnumeratedList(CMClass.clanItems(),allKnownFields,allFieldsMsg);
			sortEnumeratedList(CMClass.miscMagic(),allKnownFields,allFieldsMsg);
			sortEnumeratedList(CMClass.miscTech(),allKnownFields,allFieldsMsg);
			mob.tell("Valid field names are "+allFieldsMsg.toString());
			return false;
		}
		String scope="WORLD";
		if((commands.size()>0)&&
		   ((String)commands.elementAt(0)).equalsIgnoreCase("room"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.MERGE))
			{
				mob.tell("You are not allowed to do that here.");
				return false;
			}
			commands.removeElementAt(0);
			placesToDo.addElement(mob.location());
			scope="ROOM";
		}
		if((commands.size()>0)&&
		   ((String)commands.elementAt(0)).equalsIgnoreCase("area"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.MERGE))
			{
				mob.tell("You are not allowed to do that here.");
				return false;
			}
			commands.removeElementAt(0);
			placesToDo.addElement(mob.location().getArea());
			scope="AREA";
		}
		if((commands.size()>0)&&
		   ((String)commands.elementAt(0)).equalsIgnoreCase("world"))
		{
			if(!CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.MERGE))
			{
				mob.tell("You are not allowed to do that.");
				return false;
			}
			commands.removeElementAt(0);
			placesToDo=new Vector();
			scope="WORLD";
		}
		if(commands.size()==0)
		{
			mob.tell("Merge what? DATABASE or filename");
			return false;
		}
		String firstWord=(String)commands.firstElement();
		if(firstWord.equalsIgnoreCase("DATABASE"))
		{
			commands.removeElementAt(0);
			if(commands.size()==0)
			{
				mob.tell("Merge parameters missing: DBCLASS, DBSERVICE, DBUSER, DBPASS");
				return false;
			}
			firstWord=(String)commands.firstElement();
			return doArchonDBCompare(mob, scope, firstWord, commands);
		}
		String filename=(String)commands.lastElement();
		commands.remove(filename);
		StringBuffer buf=new CMFile(filename,mob,CMFile.FLAG_LOGERRORS).text();
		if((buf==null)||(buf.length()==0))
		{
			mob.tell("File not found at: '"+filename+"'!");
			return false;
		}

		List<String> changes=new Vector();
		List<String> onfields=new Vector();
		List<String> ignore=new Vector();
		List<String> use=null;
		List<String> allKnownFields=new Vector();
		List things=new Vector();
		boolean aremobs=false;
		if((buf.length()>20)&&(buf.substring(0,20).indexOf("<MOBS>")>=0))
		{
			if(mob.session()!=null)
				mob.session().rawPrint("Unpacking mobs from file: '"+filename+"'...");
			String error=CMLib.coffeeMaker().addMOBsFromXML(buf.toString(),things,mob.session());
			if(mob.session()!=null)    mob.session().rawPrintln("!");
			if(error.length()>0)
			{
				mob.tell("An error occurred on merge: "+error);
				mob.tell("Please correct the problem and try the import again.");
				return false;
			}
			aremobs=true;
		}
		else
		if((buf.length()>20)&&(buf.substring(0,20).indexOf("<ITEMS>")>=0))
		{
			if(mob.session()!=null)
				mob.session().rawPrint("Unpacking items from file: '"+filename+"'...");
			String error=CMLib.coffeeMaker().addItemsFromXML(buf.toString(),things,mob.session());
			if(mob.session()!=null)    mob.session().rawPrintln("!");
			if(error.length()>0)
			{
				mob.tell("An error occurred on merge: "+error);
				mob.tell("Please correct the problem and try the import again.");
				return false;
			}
		}
		else
		{
			mob.tell("Files of this type are not yet supported by MERGE.  You must merge an ITEMS or MOBS file at this time.");
			return false;
		}
		if(things.size()==0)
		{
			mob.tell("Nothing was found in the file to merge!");
			return false;
		}
		StringBuffer allFieldsMsg=new StringBuffer("");
		if(aremobs)
			sortEnumeratedList(CMClass.mobTypes(),allKnownFields,allFieldsMsg);
		else
		{
			sortEnumeratedList(CMClass.basicItems(),allKnownFields,allFieldsMsg);
			sortEnumeratedList(CMClass.weapons(),allKnownFields,allFieldsMsg);
			sortEnumeratedList(CMClass.armor(),allKnownFields,allFieldsMsg);
			sortEnumeratedList(CMClass.clanItems(),allKnownFields,allFieldsMsg);
			sortEnumeratedList(CMClass.miscMagic(),allKnownFields,allFieldsMsg);
			sortEnumeratedList(CMClass.miscTech(),allKnownFields,allFieldsMsg);
		}

		allKnownFields.add("REJUV");
		allFieldsMsg.append("REJUV ");

		for(int i=0;i<commands.size();i++)
		{
			String str=((String)commands.elementAt(i)).toUpperCase();
			if(str.startsWith("CHANGE="))
			{
				use=changes;
				str=str.substring(7).trim();
			}
			if(str.startsWith("ON="))
			{
				use=onfields;
				str=str.substring(3).trim();
			}
			if(str.startsWith("IGNORE="))
			{
				use=ignore;
				str=str.substring(7).trim();
			}
			int x=str.indexOf(',');
			while(x>=0)
			{
				String s=str.substring(0,x).trim();
				if(s.length()>0)
				{
					if(use==null)
					{
						mob.tell("'"+str+"' is an unknown parameter!");
						return false;
					}
					if(allKnownFields.contains(s))
						use.add(s);
					else
					{
						mob.tell("'"+s+"' is an unknown field name.  Valid fields include: "+allFieldsMsg.toString());
						return false;
					}
				}
				str=str.substring(x+1).trim();
				x=str.indexOf(',');
			}
			if(str.length()>0)
			{
				if(use==null)
				{
					mob.tell("'"+str+"' is an unknown parameter!");
					return false;
				}
				if(allKnownFields.contains(str))
					use.add(str);
				else
				{
					mob.tell("'"+str+"' is an unknown field name.  Valid fields include: "+allFieldsMsg.toString());
					return false;
				}
			}
		}
		if((onfields.size()==0)&&(ignore.size()==0)&&(changes.size()==0))
		{
			mob.tell("You must specify either an ON, CHANGES, or IGNORE parameter for valid matches to be made.");
			return false;
		}
		if(placesToDo.size()==0)
		for(Enumeration a=CMLib.map().areas();a.hasMoreElements();)
		{
			Area A=(Area)a.nextElement();
			if(A.getCompleteMap().hasMoreElements()
			&&CMSecurity.isAllowed(mob,(A.getCompleteMap().nextElement()),CMSecurity.SecFlag.MERGE))
				placesToDo.addElement(A);
		}
		if(placesToDo.size()==0)
		{
			mob.tell("There are no rooms to merge into!");
			return false;
		}
		for(int i=placesToDo.size()-1;i>=0;i--)
		{
			if(placesToDo.elementAt(i) instanceof Area)
			{
				Area A=(Area)placesToDo.elementAt(i);
				placesToDo.removeElement(A);
				for(Enumeration r=A.getCompleteMap();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					if(CMSecurity.isAllowed(mob,R,CMSecurity.SecFlag.MERGE))
						placesToDo.addElement(R);
				}
			}
			else
			if(placesToDo.elementAt(i) instanceof Room)
				if(mob.session()!=null)    mob.session().rawPrint(".");
			else
				return false;
		}
		// now do the merge...
		if(mob.session()!=null)
			mob.session().rawPrint("Merging and saving...");
		if(noisy) mergedebugtell(mob,"Rooms to do: "+placesToDo.size());
		if(noisy) mergedebugtell(mob,"Things loaded: "+things.size());
		if(noisy) mergedebugtell(mob,"On fields="+CMParms.toStringList(onfields));
		if(noisy) mergedebugtell(mob,"Ignore fields="+CMParms.toStringList(ignore));
		if(noisy) mergedebugtell(mob,"Change fields="+CMParms.toStringList(changes));
		Log.sysOut("Import",mob.Name()+" merge '"+filename+"'.");
		for(int r=0;r<placesToDo.size();r++)
		{
			Room R=(Room)placesToDo.elementAt(r);
			if(!CMSecurity.isAllowed(mob,R,CMSecurity.SecFlag.MERGE))
				continue;
			if(R.roomID().length()==0) continue;
			synchronized(("SYNC"+R.roomID()).intern())
			{
				R=CMLib.map().getRoom(R);
				Area.State oldFlags=R.getArea().getAreaState();
				R.getArea().setAreaState(Area.State.FROZEN);
				CMLib.map().resetRoom(R);
				boolean savemobs=false;
				boolean saveitems=false;
				if(aremobs)
				{
					for(int m=0;m<R.numInhabitants();m++)
					{
						MOB M=R.fetchInhabitant(m);
						if((M!=null)&&(M.isSavable()))
							if(tryMerge(mob,R,M,things,changes,onfields,ignore,noisy))
								savemobs=true;
					}
				}
				else
				{
					for(int i=0;i<R.numItems();i++)
					{
						Item I=R.getItem(i);
						if((I!=null)&&(tryMerge(mob,R,I,things,changes,onfields,ignore,noisy)))
							saveitems=true;
					}
					for(int m=0;m<R.numInhabitants();m++)
					{
						MOB M=R.fetchInhabitant(m);
						if((M!=null)&&(M.isSavable()))
						{
							for(int i=0;i<M.numItems();i++)
							{
								Item I=M.getItem(i);
								if((I!=null)&&(tryMerge(mob,R,I,things,changes,onfields,ignore,noisy)))
									savemobs=true;
							}
							ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(M);
							if(SK!=null)
							{
								for(Iterator<Environmental> i=SK.getShop().getStoreInventory();i.hasNext();)
								{
									Environmental E=i.next();
									if(E instanceof Item)
									{
										Item I=(Item)E;
										if(tryMerge(mob,R,I,things,changes,onfields,ignore,noisy))
											savemobs=true;
									}
								}
							}
						}
					}
				}
				if(saveitems) CMLib.database().DBUpdateItems(R);
				if(savemobs) CMLib.database().DBUpdateMOBs(R);
				if(mob.session()!=null)    mob.session().rawPrint(".");
				R.getArea().setAreaState(oldFlags);
			}
		}

		if(mob.session()!=null)    mob.session().rawPrintln("!\n\rDone!");
		Area A=null;
		for(int i=0;i<placesToDo.size();i++)
		{
			A=((Room)placesToDo.elementAt(i)).getArea();
			if((A!=null)&&(A.getAreaState()!=Area.State.ACTIVE))
				A.setAreaState(Area.State.ACTIVE);
		}
		return false;
	}
	
	private static final SHashtable<String,CMClass.CMObjectType> OBJECT_TYPES=new SHashtable<String,CMClass.CMObjectType>(new Object[][]{
			{"MOBS",CMClass.CMObjectType.MOB},
			{"ROOMS",CMClass.CMObjectType.LOCALE},
			{"ITEMS",CMClass.CMObjectType.ITEM},
			{"WEAPON",CMClass.CMObjectType.WEAPON},
			{"ARMOR",CMClass.CMObjectType.ARMOR},
	});
	
	private boolean amMergingType(CMClass.CMObjectType doType, Environmental E)
	{
		if(doType==null) 
			return true;
		switch(doType)
		{
		case LOCALE: return true;
		case MOB: return E instanceof MOB;
		case ITEM: return E instanceof Item;
		case WEAPON: return E instanceof Weapon;
		case ARMOR: return E instanceof Armor;
		default: return false;
		}
	}
	
	private boolean amMerging(CMClass.CMObjectType doType, MaskingLibrary.CompiledZapperMask mask, Environmental E)
	{
		if(amMergingType(doType,E))
		{
			if(mask==null) return true;
			return CMLib.masking().maskCheck(mask, E, true);
		}
		return false;
	}
	
	public boolean dbMerge(MOB mob, String name, Modifiable dbM, Modifiable M, Set<String> ignores) throws java.io.IOException, CMException
	{
		if((M instanceof Physical) && (dbM instanceof Physical))
		{
			Physical PM=(Physical)M;
			Physical dbPM=(Physical)dbM;
			if(CMLib.flags().isCataloged(PM))
			{
				mob.tell("^H**Warning: Changes will remove this object from the catalog.");
				PM.basePhyStats().setDisposition(CMath.unsetb(PM.basePhyStats().disposition(),PhyStats.IS_CATALOGED));
			}
			if(CMLib.flags().isCataloged(dbPM))
				dbPM.basePhyStats().setDisposition(CMath.unsetb(dbPM.basePhyStats().disposition(),PhyStats.IS_CATALOGED));
			PM.image();
			dbPM.image();
		}
		
		String[] statCodes = dbM.getStatCodes();
		int showFlag=-1;
		if(CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0)
			showFlag=-999;
		boolean ok=false;
		boolean didSomething=false;
		while(!ok)
		{
			int showNumber=0;
			mob.tell(name);
			for(int i=0;i<statCodes.length;i++)
			{
				String statCode = M.getStatCodes()[i];
				if(ignores.contains(statCode)||((M instanceof MOB)&&statCode.equalsIgnoreCase("INVENTORY"))) 
					continue;
				String promptStr = CMStrings.capitalizeAndLower(M.getStatCodes()[i]);
				String dbVal = dbM.getStat(statCode);
				String loVal = M.getStat(statCode);
				if(dbVal.equals(loVal))
					continue;
				++showNumber;
				if((showFlag>0)&&(showFlag!=showNumber)) continue;
				mob.tell("^H"+showNumber+". "+promptStr+"\n\rValue: ^W'"+loVal+"'\n\r^HDBVal: ^N'"+dbVal+"'");
				if((showFlag!=showNumber)&&(showFlag>-999)) continue;
				String res=mob.session().choose("D)atabase Value, E)dit Value, or N)o Change, or Q)uit All: ","DENQ", "N");
				if(res.trim().equalsIgnoreCase("N")) continue;
				if(res.trim().equalsIgnoreCase("Q")) throw new CMException("Cancelled by user.");
				didSomething=true;
				if(res.trim().equalsIgnoreCase("D"))
				{
					M.setStat(statCode,dbVal);
					continue;
				}
				M.setStat(statCode,CMLib.genEd().prompt(mob,M.getStat(statCode),++showNumber,showFlag,promptStr));
			}
			if(showNumber==0) return didSomething;
			if(showFlag<-900){ ok=true; break;}
			if(showFlag>0){ showFlag=-1; continue;}
			showFlag=CMath.s_int(mob.session().prompt("Edit which? ",""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
			}
		}
		return didSomething;
	}
	
	public boolean doArchonDBCompare(MOB mob, String scope, String firstWord, Vector commands) throws java.io.IOException
	{
		CMClass.CMObjectType doType = OBJECT_TYPES.get(firstWord.toUpperCase());
		if(doType==null) doType = OBJECT_TYPES.get(firstWord.toUpperCase()+"S");
		if(doType!=null)
			commands.remove(0);
		else
			doType=CMClass.CMObjectType.LOCALE;
		
		String theRest = CMParms.combineWithQuotes(commands, 0);
		DBConnector dbConnector=null;
		String dbClass=CMParms.getParmStr(theRest,"DBCLASS","");
		String dbService=CMParms.getParmStr(theRest,"DBSERVICE","");
		String dbUser=CMParms.getParmStr(theRest,"DBUSER","");
		String dbPass=CMParms.getParmStr(theRest,"DBPASS","");
		int dbConns=CMParms.getParmInt(theRest,"DBCONNECTIONS",3);
		int dbPingIntMins=CMParms.getParmInt(theRest,"DBPINGINTERVALMINS",30);
		boolean dbReuse=CMParms.getParmBool(theRest,"DBREUSE",true);
		String ignore=CMParms.getParmStr(theRest,"IGNORE","");
		String maskStr=CMParms.getParmStr(theRest,"MASK","");
		Set<String> ignores=new SHashSet(CMParms.parseCommas(ignore.toUpperCase(),true));
		MaskingLibrary.CompiledZapperMask mask=CMLib.masking().maskCompile(maskStr);
		if(dbClass.length()==0)
		{
			mob.tell("This command requires DBCLASS= to be set.");
			return false;
		}
		if(dbService.length()==0)
		{
			mob.tell("This command requires DBSERVICE= to be set.");
			return false;
		}
		if(dbUser.length()==0)
		{
			mob.tell("This command requires DBUSER= to be set.");
			return false;
		}
		if(dbPass.length()==0)
		{
			mob.tell("This command requires DBPASS= to be set.");
			return false;
		}
		
		dbConnector=new DBConnector(dbClass,dbService,dbUser,dbPass,dbConns,dbPingIntMins,dbReuse,false,false);
		dbConnector.reconnect();
		DBInterface dbInterface = new DBInterface(dbConnector,null);
		
		DBConnection DBTEST=dbConnector.DBFetch();
		if(DBTEST!=null) dbConnector.DBDone(DBTEST);
		mob.tell("Loading database rooms...");
		List<Room> rooms = new LinkedList<Room>();
		if((!dbConnector.amIOk())||(!dbInterface.isConnected()))
		{
			mob.tell("Failed to connect to database.");
			return false;
		}
		if(scope.equalsIgnoreCase("AREA"))
			rooms.addAll(Arrays.asList(dbInterface.DBReadRoomObjects(mob.location().getArea().Name(), false)));
		else
		if(scope.equalsIgnoreCase("ROOM"))
		{
			Room R=dbInterface.DBReadRoomObject(mob.location().roomID(), false);
			if(R!=null)
				rooms.add(R);
		}
		else
		for(Enumeration<Area> e=CMLib.map().areas();e.hasMoreElements();)
			rooms.addAll(Arrays.asList(dbInterface.DBReadRoomObjects(e.nextElement().Name(), false)));
		if(rooms.size()==0)
		{
			mob.tell("No rooms found.");
			return false;
		}
		for(Room R : rooms)
			dbInterface.DBReadContent(R.roomID(),R,false);
		mob.tell("Data loaded, starting scan.");
		Comparator<MOB> convM=new Comparator<MOB>() {
			public int compare(MOB arg0, MOB arg1) {
				int x=arg0.ID().compareTo(arg1.ID());
				return(x!=0)?x:arg0.Name().compareTo(arg1.Name());
			}
		};
		Comparator<Item> convI=new Comparator<Item>() {
			public int compare(Item arg0, Item arg1) {
				int x=arg0.ID().compareTo(arg1.ID());
				return(x!=0)?x:arg0.Name().compareTo(arg1.Name());
			}
		};
		try {
		for(Room dbR : rooms)
		{
			Room R=CMLib.map().getRoom(dbR.roomID());
			if(R==null)
			{
				if(doType==CMClass.CMObjectType.LOCALE)
					Log.sysOut("Merge",dbR.roomID()+" not in database");
				// import, including exits!
				continue;
			}
			synchronized(("SYNC"+dbR.roomID()).intern())
			{
				Area.State oldFlags=R.getArea().getAreaState();
				R.getArea().setAreaState(Area.State.FROZEN);
				
				boolean updateMobs=false;
				boolean updateItems=false;
				boolean updateRoom=false;
				R=CMLib.map().getRoom(R);
				CMLib.map().resetRoom(R);
				List<MOB> mobSetL=new Vector<MOB>();
				for(Enumeration<MOB> e=dbR.inhabitants();e.hasMoreElements();)
					mobSetL.add(e.nextElement());
				MOB[] mobSet=mobSetL.toArray(new MOB[0]);
				Arrays.sort(mobSet, convM);
				String lastName="";
				int ct=1;
				HashSet<MOB> doneM=new HashSet<MOB>();
				for(MOB dbM : mobSet)
				{
					if(!lastName.equals(dbM.Name()))
						ct=1;
					else
						ct++;
					String rName=dbM.Name()+"."+ct;
					MOB M=null;
					int ctr=ct;
					for(Enumeration<MOB> m = R.inhabitants();m.hasMoreElements();)
					{
						MOB M1=m.nextElement();
						if(M1.Name().equalsIgnoreCase(dbM.Name())&&((--ctr)<=0))
						{ M=M1; break;}
					}
					if(M==null)
					{
						if(amMerging(doType,mask,dbM)&&(!ignore.contains("MISSING")))
						{
							if(mob.session().confirm("MOB: "+dbR.roomID()+"."+rName+" not in local room.\n\rWould you like to add it (y/N)?", "N"))
							{
								M=(MOB)dbM.copyOf();
								M.bringToLife(R, true);
								doneM.add(M);
								updateMobs=true;
								Log.sysOut("Merge",mob.Name()+" added mob "+dbR.roomID()+"."+rName);
							}
						}
					}
					else
					{
						doneM.add(M);
						if(amMerging(doType,mask,dbM))
						{
							if(!dbM.sameAs(M))
							{
								MOB oldM=(MOB)M.copyOf();
								if((dbMerge(mob,"^MMOB "+dbR.roomID()+"."+rName+"^N",dbM,M, ignores))
								&&(!oldM.sameAs(M)))
								{
									Log.sysOut("Merge",mob.Name()+" modified mob "+dbR.roomID()+"."+rName);
									updateMobs=true;
								}
							}
						}
						STreeSet<Item> itemSetL=new STreeSet<Item>(convI);
						for(Enumeration<Item> e=dbM.items();e.hasMoreElements();)
							itemSetL.add(e.nextElement());
						Item[] itemSet=itemSetL.toArray(new Item[0]);
						Arrays.sort(itemSet, convI);
						String lastIName="";
						int ict=1;
						HashSet<Item> doneI=new HashSet<Item>();
						for(Item dbI : itemSet)
						{
							if(!lastIName.equals(dbI.Name()))
								ict=1;
							else
								ict++;
							String rIName=dbI.Name()+"."+ict;
							Item I=null;
							ctr=ict;
							for(Enumeration<Item> i = M.items();i.hasMoreElements();)
							{
								Item I1=i.nextElement();
								if(I1.Name().equalsIgnoreCase(dbI.Name())&&((--ctr)<=0))
								{ I=I1; break;}
							}
							if(I==null)
							{
								if(amMerging(doType,mask,dbI)&&(!ignore.contains("MISSING")))
								{
									if(mob.session().confirm("Item: "+dbR.roomID()+"."+dbM.Name()+"."+rIName+" not in local room.\n\rWould you like to add it (y/N)?", "N"))
									{
										I=(Item)dbI.copyOf();
										M.addItem(I);
										doneI.add(I);
										Item cI=(dbI.container()==null)?null:M.findItem(dbI.container().Name());
										if(cI instanceof Container)
											I.setContainer((Container)cI);
										updateMobs=true;
										Log.sysOut("Merge",mob.Name()+" added item "+dbR.roomID()+"."+dbM.Name()+"."+rIName);
									}
								}
							}
							else
							if(amMerging(doType,mask,dbI))
							{
								doneI.add(I);
								if(!dbI.sameAs(I))
								{
									Item oldI=(Item)I.copyOf();
									if((dbMerge(mob,"^IITEM ^M"+dbR.roomID()+"."+dbM.Name()+"."+rIName+"^N",dbI,I, ignores))
									&&(!oldI.sameAs(I)))
									{
										Log.sysOut("Merge",mob.Name()+" modified item "+dbR.roomID()+"."+dbM.Name()+"."+rIName);
										updateMobs=true;
									}
								}
							}
							lastIName=dbI.Name();
						}
						for(Enumeration<Item> i=M.items();i.hasMoreElements();)
						{
							Item I=i.nextElement();
							if(amMerging(doType,mask,I)&&(!doneI.contains(I))&&(!ignore.contains("EXTRA")))
							{
								if(mob.session().confirm("Item: "+R.roomID()+"."+M.Name()+"."+I.Name()+" not in database.\n\rWould you like to delete it (y/N)?", "N"))
								{
									M.delItem(I);
									updateMobs=true;
									Log.sysOut("Merge",mob.Name()+" deleted item "+R.roomID()+"."+M.Name()+"."+I.Name());
								}
							}
						}
					}
					lastName=dbM.Name();
				}
				for(Enumeration<MOB> r=R.inhabitants();r.hasMoreElements();)
				{
					MOB M=r.nextElement();
					if(amMerging(doType,mask,M)&&(!doneM.contains(M))&&(M.isMonster())&&(!ignore.contains("EXTRA")))
					{
						if(mob.session().confirm("MOB: "+R.roomID()+"."+M.Name()+" not in database.\n\rWould you like to delete it (y/N)?", "N"))
						{
							R.delInhabitant(M);
							updateMobs=true;
							Log.sysOut("Merge",mob.Name()+" deleted mob "+R.roomID()+"."+M.Name());
						}
					}
				}
				
				STreeSet<Item> itemSetL=new STreeSet<Item>(convI);
				for(Enumeration<Item> e=dbR.items();e.hasMoreElements();)
					itemSetL.add(e.nextElement());
				Item[] itemSet=itemSetL.toArray(new Item[0]);
				Arrays.sort(itemSet, convI);
				lastName="";
				ct=1;
				HashSet<Item> doneI=new HashSet<Item>();
				for(Item dbI : itemSet)
				{
					if(!lastName.equals(dbI.Name()))
						ct=1;
					else
						ct++;
					String rName=dbI.Name()+"."+ct;
					Item I=null;
					int ctr=ct;
					for(Enumeration<Item> i = R.items();i.hasMoreElements();)
					{
						Item I1=i.nextElement();
						if(I1.Name().equalsIgnoreCase(dbI.Name())&&((--ctr)<=0))
						{ I=I1; break;}
					}
					if(I==null)
					{
						if(amMerging(doType,mask,dbI)&&(!ignore.contains("MISSING")))
						{
							if(mob.session().confirm("Item: "+dbR.roomID()+"."+rName+" not in local room.\n\rWould you like to add it (y/N)?", "N"))
							{
								I=(Item)dbI.copyOf();
								R.addItem(I);
								doneI.add(I);
								Item cI=(dbI.container()==null)?null:R.findItem(dbI.container().Name());
								if(cI instanceof Container)
									I.setContainer((Container)cI);
								updateItems=true;
								Log.sysOut("Merge",mob.Name()+" added item "+dbR.roomID()+"."+rName);
							}
						}
					}
					else
					if(amMerging(doType,mask,dbI))
					{
						doneI.add(I);
						if(!dbI.sameAs(I))
						{
							Item oldI=(Item)I.copyOf();
							if((dbMerge(mob,"^IITEM "+dbR.roomID()+"."+rName+"^N",dbI,I, ignores))
							&&(!oldI.sameAs(I)))
							{
								Log.sysOut("Merge",mob.Name()+" modified item "+dbR.roomID()+"."+rName);
								updateItems=true;
							}
						}
					}
					lastName=dbI.Name();
				}
				for(Enumeration<Item> i=R.items();i.hasMoreElements();)
				{
					Item I=i.nextElement();
					if(amMerging(doType,mask,I)&&(!doneI.contains(I))&&(!ignore.contains("EXTRA")))
					{
						if(mob.session().confirm("Item: "+R.roomID()+"."+I.Name()+" not in database.\n\rWould you like to delete it (y/N)?", "N"))
						{
							R.delItem(I);
							updateItems=true;
							Log.sysOut("Merge",mob.Name()+" deleted item "+R.roomID()+"."+I.Name());
						}
					}
				}
				if(updateRoom) CMLib.database().DBUpdateRoom(R);
				if(updateItems) CMLib.database().DBUpdateItems(R);
				if(updateMobs) CMLib.database().DBUpdateMOBs(R);
				CMLib.map().resetRoom(R);
				R.getArea().setAreaState(oldFlags);
			}
			dbR.destroy();
		}
		mob.tell("Done");
		}catch(CMException cme){
			mob.tell("Cancelled.");
		}
		dbInterface.shutdown();
		return true;
	}
	
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowedAnywhere(mob,CMSecurity.SecFlag.MERGE);}

	
}
