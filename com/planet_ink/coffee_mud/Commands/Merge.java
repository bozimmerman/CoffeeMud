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
   Copyright 2004-2018 Bo Zimmerman

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

	private final String[] access=I(new String[]{"MERGE"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

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
		if(mob!=null)
			mob.tell(msg);
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
		final List<String> efields=new Vector<String>();
		List<String> allMyFields=new Vector<String>();
		final String[] EFIELDS=E.getStatCodes();
		for(int i=0;i<EFIELDS.length;i++)
		{
			if(!efields.contains(EFIELDS[i]))
				efields.add(EFIELDS[i]);
		}
		efields.add("REJUV");
		allMyFields=new XVector<String>(efields);
		for(int v=0;v<ignore.size();v++)
		{
			if(efields.contains(ignore.get(v)))
				efields.remove(ignore.get(v));
		}
		for(int v=0;v<changes.size();v++)
		{
			if(efields.contains(changes.get(v)))
				efields.remove(changes.get(v));
		}
		if(noisy)
			mergedebugtell(mob,"AllMy-"+CMParms.toListString(allMyFields));
		if(noisy)
			mergedebugtell(mob,"efields-"+CMParms.toListString(efields));
		for(int t=0;t<things.size();t++)
		{
			final Environmental E2=(Environmental)things.get(t);
			if(noisy)
				mergedebugtell(mob,E.name()+"/"+E2.name()+"/"+CMClass.classID(E)+"/"+CMClass.classID(E2));
			if(CMClass.classID(E).equals(CMClass.classID(E2)))
			{
				Vector<String> fieldsToCheck=null;
				if(onfields.size()>0)
				{
					fieldsToCheck=new Vector<String>();
					for(int v=0;v<onfields.size();v++)
					{
						if(efields.contains(onfields.get(v)))
							fieldsToCheck.add(onfields.get(v));
					}
				}
				else
					fieldsToCheck=new XVector<String>(efields);

				boolean checkedOut=fieldsToCheck.size()>0;
				if(noisy)
					mergedebugtell(mob,"fieldsToCheck-"+CMParms.toListString(fieldsToCheck));
				if(checkedOut)
				for(int i=0;i<fieldsToCheck.size();i++)
				{
					final String field=fieldsToCheck.get(i);
					if(noisy)
						mergedebugtell(mob,field+"/"+getStat(E,field)+"/"+getStat(E2,field)+"/"+getStat(E,field).equals(getStat(E2,field)));
					if(!getStat(E,field).equals(getStat(E2,field)))
					{
						checkedOut = false;
						break;
					}
				}
				if(checkedOut)
				{
					List<String> fieldsToChange=null;
					if(changes.size()==0)
						fieldsToChange=new XVector<String>(allMyFields);
					else
					{
						fieldsToChange=new Vector<String>();
						for(int v=0;v<changes.size();v++)
						{
							if(allMyFields.contains(changes.get(v)))
								fieldsToChange.add(changes.get(v));
						}
					}
					if(noisy)
						mergedebugtell(mob,"fieldsToChange-"+CMParms.toListString(fieldsToChange));
					for(int i=0;i<fieldsToChange.size();i++)
					{
						final String field=fieldsToChange.get(i);
						if(noisy)
							mergedebugtell(mob,E.name()+" wants to change "+field+" value "+getStat(E,field)+" to "+getStat(E2,field)+"/"+(!getStat(E,field).equals(getStat(E2,field))));
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
			final Environmental E=(Environmental)e.nextElement();
			final String[] fields=E.getStatCodes();
			for(int x=0;x<fields.length;x++)
			{
				if(!allKnownFields.contains(fields[x]))
				{
					allKnownFields.add(fields[x]);
					allFieldsMsg.append(fields[x]+" ");
				}
			}
		}
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		final boolean noisy=CMSecurity.isDebugging(CMSecurity.DbgFlag.MERGE);
		Vector<Places> placesToDo=new Vector<Places>();
		commands.remove(0);
		if(commands.size()==0)
		{
			mob.tell(L("Merge what? Try DATABASE or a filename"));
			return false;
		}
		if(mob.isMonster())
		{
			mob.tell(L("No can do."));
			return false;
		}
		if((commands.size()>0)&&
			commands.get(0).equalsIgnoreCase("noprompt"))
			commands.remove(0);

		if((commands.size()>0)&&
			commands.get(0).equalsIgnoreCase("?"))
		{
			final StringBuffer allFieldsMsg=new StringBuffer("");
			final Vector<String> allKnownFields=new Vector<String>();
			sortEnumeratedList(CMClass.mobTypes(),allKnownFields,allFieldsMsg);
			sortEnumeratedList(CMClass.basicItems(),allKnownFields,allFieldsMsg);
			sortEnumeratedList(CMClass.weapons(),allKnownFields,allFieldsMsg);
			sortEnumeratedList(CMClass.armor(),allKnownFields,allFieldsMsg);
			sortEnumeratedList(CMClass.clanItems(),allKnownFields,allFieldsMsg);
			sortEnumeratedList(CMClass.miscMagic(),allKnownFields,allFieldsMsg);
			sortEnumeratedList(CMClass.tech(),allKnownFields,allFieldsMsg);
			mob.tell(L("Valid field names are @x1",allFieldsMsg.toString()));
			return false;
		}
		String scope="WORLD";
		if((commands.size()>0)
		&&commands.get(0).equalsIgnoreCase("room"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.MERGE))
			{
				mob.tell(L("You are not allowed to do that here."));
				return false;
			}
			commands.remove(0);
			placesToDo.add(mob.location());
			scope="ROOM";
		}
		if((commands.size()>0)&&
			commands.get(0).equalsIgnoreCase("area"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.MERGE))
			{
				mob.tell(L("You are not allowed to do that here."));
				return false;
			}
			commands.remove(0);
			placesToDo.add(mob.location().getArea());
			scope="AREA";
		}
		if((commands.size()>0)&&
			commands.get(0).equalsIgnoreCase("world"))
		{
			if(!CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.MERGE))
			{
				mob.tell(L("You are not allowed to do that."));
				return false;
			}
			commands.remove(0);
			placesToDo=new Vector<Places>();
			scope="WORLD";
		}
		if(commands.size()==0)
		{
			mob.tell(L("Merge what? DATABASE or filename"));
			return false;
		}
		String firstWord=commands.get(0);
		if(firstWord.equalsIgnoreCase("DATABASE"))
		{
			commands.remove(0);
			if(commands.size()==0)
			{
				mob.tell(L("Merge parameters missing: DBCLASS, DBSERVICE, DBUSER, DBPASS"));
				return false;
			}
			firstWord=commands.get(0);
			return doArchonDBCompare(mob, scope, firstWord, commands);
		}
		final String filename=commands.get(commands.size()-1);
		commands.remove(filename);
		final StringBuffer buf=new CMFile(filename,mob,CMFile.FLAG_LOGERRORS).text();
		if((buf==null)||(buf.length()==0))
		{
			mob.tell(L("File not found at: '@x1'!",filename));
			return false;
		}

		final List<String> changes=new Vector<String>();
		final List<String> onfields=new Vector<String>();
		final List<String> ignore=new Vector<String>();
		List<String> use=null;
		final List<String> allKnownFields=new Vector<String>();
		final List<Physical> things=new Vector<Physical>();
		boolean aremobs=false;
		if((buf.length()>20)&&(buf.substring(0,20).indexOf("<MOBS>")>=0))
		{
			if(mob.session()!=null)
				mob.session().rawPrint(L("Unpacking mobs from file: '@x1'...",filename));
			List<MOB> mobs=new Vector<MOB>();
			final String error=CMLib.coffeeMaker().addMOBsFromXML(buf.toString(),mobs,mob.session());
			things.addAll(mobs);
			mobs.clear();
			mobs=null;
			if(mob.session()!=null)
				mob.session().rawPrintln("!");
			if(error.length()>0)
			{
				mob.tell(L("An error occurred on merge: @x1",error));
				mob.tell(L("Please correct the problem and try the import again."));
				return false;
			}
			aremobs=true;
		}
		else
		if((buf.length()>20)&&(buf.substring(0,20).indexOf("<ITEMS>")>=0))
		{
			if(mob.session()!=null)
				mob.session().rawPrint(L("Unpacking items from file: '@x1'...",filename));
			List<Item> items=new Vector<Item>();
			final String error=CMLib.coffeeMaker().addItemsFromXML(buf.toString(),items,mob.session());
			things.addAll(items);
			items.clear();
			items=null;
			if(mob.session()!=null)
				mob.session().rawPrintln("!");
			if(error.length()>0)
			{
				mob.tell(L("An error occurred on merge: @x1",error));
				mob.tell(L("Please correct the problem and try the import again."));
				return false;
			}
		}
		else
		{
			mob.tell(L("Files of this type are not yet supported by MERGE.  You must merge an ITEMS or MOBS file at this time."));
			return false;
		}
		if(things.size()==0)
		{
			mob.tell(L("Nothing was found in the file to merge!"));
			return false;
		}
		final StringBuffer allFieldsMsg=new StringBuffer("");
		if(aremobs)
			sortEnumeratedList(CMClass.mobTypes(),allKnownFields,allFieldsMsg);
		else
		{
			sortEnumeratedList(CMClass.basicItems(),allKnownFields,allFieldsMsg);
			sortEnumeratedList(CMClass.weapons(),allKnownFields,allFieldsMsg);
			sortEnumeratedList(CMClass.armor(),allKnownFields,allFieldsMsg);
			sortEnumeratedList(CMClass.clanItems(),allKnownFields,allFieldsMsg);
			sortEnumeratedList(CMClass.miscMagic(),allKnownFields,allFieldsMsg);
			sortEnumeratedList(CMClass.tech(),allKnownFields,allFieldsMsg);
		}

		allKnownFields.add("REJUV");
		allFieldsMsg.append(L("REJUV "));

		for(int i=0;i<commands.size();i++)
		{
			String str=commands.get(i).toUpperCase();
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
				final String s=str.substring(0,x).trim();
				if(s.length()>0)
				{
					if(use==null)
					{
						mob.tell(L("'@x1' is an unknown parameter!",str));
						return false;
					}
					if(allKnownFields.contains(s))
						use.add(s);
					else
					{
						mob.tell(L("'@x1' is an unknown field name.  Valid fields include: @x2",s,allFieldsMsg.toString()));
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
					mob.tell(L("'@x1' is an unknown parameter!",str));
					return false;
				}
				if(allKnownFields.contains(str))
					use.add(str);
				else
				{
					mob.tell(L("'@x1' is an unknown field name.  Valid fields include: @x2",str,allFieldsMsg.toString()));
					return false;
				}
			}
		}
		if((onfields.size()==0)&&(ignore.size()==0)&&(changes.size()==0))
		{
			mob.tell(L("You must specify either an ON, CHANGES, or IGNORE parameter for valid matches to be made."));
			return false;
		}
		if(placesToDo.size()==0)
		for(final Enumeration<Area> a=CMLib.map().areas();a.hasMoreElements();)
		{
			final Area A=a.nextElement();
			if(A.getCompleteMap().hasMoreElements()
			&&CMSecurity.isAllowed(mob,(A.getCompleteMap().nextElement()),CMSecurity.SecFlag.MERGE))
				placesToDo.add(A);
		}
		if(placesToDo.size()==0)
		{
			mob.tell(L("There are no rooms to merge into!"));
			return false;
		}
		for(int i=placesToDo.size()-1;i>=0;i--)
		{
			if(placesToDo.get(i) instanceof Area)
			{
				final Area A=(Area)placesToDo.get(i);
				placesToDo.removeElement(A);
				for(final Enumeration r=A.getCompleteMap();r.hasMoreElements();)
				{
					final Room R=(Room)r.nextElement();
					if(CMSecurity.isAllowed(mob,R,CMSecurity.SecFlag.MERGE))
						placesToDo.add(R);
				}
			}
			else
			if(placesToDo.get(i) instanceof Room)
			{
				if(mob.session()!=null)
					mob.session().rawPrint(".");
			}
			else
				return false;
		}
		// now do the merge...
		if(mob.session()!=null)
			mob.session().rawPrint(L("Merging and saving..."));
		if(noisy)
			mergedebugtell(mob,"Rooms to do: "+placesToDo.size());
		if(noisy)
			mergedebugtell(mob,"Things loaded: "+things.size());
		if(noisy)
			mergedebugtell(mob,"On fields="+CMParms.toListString(onfields));
		if(noisy)
			mergedebugtell(mob,"Ignore fields="+CMParms.toListString(ignore));
		if(noisy)
			mergedebugtell(mob,"Change fields="+CMParms.toListString(changes));
		Log.sysOut("Merge",mob.Name()+" merge '"+filename+"'.");
		for(int r=0;r<placesToDo.size();r++)
		{
			Room R=(Room)placesToDo.get(r);
			if(!CMSecurity.isAllowed(mob,R,CMSecurity.SecFlag.MERGE))
				continue;
			if(R.roomID().length()==0)
				continue;
			synchronized(("SYNC"+R.roomID()).intern())
			{
				R=CMLib.map().getRoom(R);
				final Area.State oldFlags=R.getArea().getAreaState();
				R.getArea().setAreaState(Area.State.FROZEN);
				CMLib.map().resetRoom(R);
				boolean savemobs=false;
				boolean saveitems=false;
				if(aremobs)
				{
					for(int m=0;m<R.numInhabitants();m++)
					{
						final MOB M=R.fetchInhabitant(m);
						if((M!=null)&&(M.isSavable()))
							if(tryMerge(mob,R,M,things,changes,onfields,ignore,noisy))
								savemobs=true;
					}
				}
				else
				{
					for(int i=0;i<R.numItems();i++)
					{
						final Item I=R.getItem(i);
						if((I!=null)&&(tryMerge(mob,R,I,things,changes,onfields,ignore,noisy)))
							saveitems=true;
					}
					for(int m=0;m<R.numInhabitants();m++)
					{
						final MOB M=R.fetchInhabitant(m);
						if((M!=null)&&(M.isSavable()))
						{
							for(int i=0;i<M.numItems();i++)
							{
								final Item I=M.getItem(i);
								if((I!=null)&&(tryMerge(mob,R,I,things,changes,onfields,ignore,noisy)))
									savemobs=true;
							}
							final ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(M);
							if(SK!=null)
							{
								for(final Iterator<Environmental> i=SK.getShop().getStoreInventory();i.hasNext();)
								{
									final Environmental E=i.next();
									if(E instanceof Item)
									{
										final Item I=(Item)E;
										if(tryMerge(mob,R,I,things,changes,onfields,ignore,noisy))
											savemobs=true;
									}
								}
							}
						}
					}
				}
				if(saveitems)
					CMLib.database().DBUpdateItems(R);
				if(savemobs)
					CMLib.database().DBUpdateMOBs(R);
				if(mob.session()!=null)
					mob.session().rawPrint(".");
				R.getArea().setAreaState(oldFlags);
			}
		}

		if(mob.session()!=null)
			mob.session().safeRawPrintln(L("!\n\rDone!"));
		Area A=null;
		for(int i=0;i<placesToDo.size();i++)
		{
			A=((Room)placesToDo.get(i)).getArea();
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

	private boolean amMerging(CMClass.CMObjectType doType, MaskingLibrary.CompiledZMask mask, Environmental E)
	{
		if(amMergingType(doType,E))
		{
			if(mask==null)
				return true;
			return CMLib.masking().maskCheck(mask, E, true);
		}
		return false;
	}

	public boolean dbMerge(MOB mob, String name, Modifiable dbM, Modifiable M, Set<String> ignores) throws java.io.IOException, CMException
	{
		if((M instanceof Physical) && (dbM instanceof Physical))
		{
			final Physical PM=(Physical)M;
			final Physical dbPM=(Physical)dbM;
			if(CMLib.flags().isCataloged(PM))
			{
				mob.tell(L("^H**Warning: Changes will remove this object from the catalog."));
				PM.basePhyStats().setDisposition(CMath.unsetb(PM.basePhyStats().disposition(),PhyStats.IS_CATALOGED));
			}
			if(CMLib.flags().isCataloged(dbPM))
				dbPM.basePhyStats().setDisposition(CMath.unsetb(dbPM.basePhyStats().disposition(),PhyStats.IS_CATALOGED));
			PM.image();
			dbPM.image();
		}

		final String[] statCodes = dbM.getStatCodes();
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
				final String statCode = M.getStatCodes()[i];
				if(ignores.contains(statCode)||((M instanceof MOB)&&statCode.equalsIgnoreCase("INVENTORY")))
					continue;
				final String promptStr = CMStrings.capitalizeAndLower(M.getStatCodes()[i]);
				final String dbVal = dbM.getStat(statCode);
				final String loVal = M.getStat(statCode);
				if(dbVal.equals(loVal))
					continue;
				++showNumber;
				if((showFlag>0)&&(showFlag!=showNumber))
					continue;
				mob.tell(L("^H@x1. @x2\n\rValue: ^W'@x3'\n\r^HDBVal: ^N'@x4'",""+showNumber,promptStr,loVal,dbVal));
				if((showFlag!=showNumber)&&(showFlag>-999))
					continue;
				final String res=mob.session().choose(L("D)atabase Value, E)dit Value, or N)o Change, or Q)uit All: "),L("DENQ"), L("N"));
				if(res.trim().equalsIgnoreCase("N"))
					continue;
				if(res.trim().equalsIgnoreCase("Q"))
					throw new CMException("Cancelled by user.");
				didSomething=true;
				if(res.trim().equalsIgnoreCase("D"))
				{
					M.setStat(statCode,dbVal);
					continue;
				}
				M.setStat(statCode,CMLib.genEd().prompt(mob,M.getStat(statCode),++showNumber,showFlag,promptStr));
			}
			if(showNumber==0)
				return didSomething;
			if(showFlag<-900)
			{
				ok=true;
				break;
			}
			if(showFlag>0)
			{
				showFlag=-1;
				continue;
			}
			showFlag=CMath.s_int(mob.session().prompt(L("Edit which? "),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
			}
		}
		return didSomething;
	}

	public boolean doArchonDBCompare(MOB mob, String scope, String firstWord, List<String> commands) throws java.io.IOException
	{
		CMClass.CMObjectType doType = OBJECT_TYPES.get(firstWord.toUpperCase());
		if(doType==null)
			doType = OBJECT_TYPES.get(firstWord.toUpperCase()+"S");
		if(doType!=null)
			commands.remove(0);
		else
			doType=CMClass.CMObjectType.LOCALE;

		final String theRest = CMParms.combineQuoted(commands, 0);
		DBConnector dbConnector=null;
		final String dbClass=CMParms.getParmStr(theRest,"DBCLASS","");
		final String dbService=CMParms.getParmStr(theRest,"DBSERVICE","");
		final String dbUser=CMParms.getParmStr(theRest,"DBUSER","");
		final String dbPass=CMParms.getParmStr(theRest,"DBPASS","");
		final int dbConns=CMParms.getParmInt(theRest,"DBCONNECTIONS",3);
		final int dbPingIntMins=CMParms.getParmInt(theRest,"DBPINGINTERVALMINS",30);
		final boolean dbReuse=CMParms.getParmBool(theRest,"DBREUSE",true);
		final String ignore=CMParms.getParmStr(theRest,"IGNORE","");
		final String maskStr=CMParms.getParmStr(theRest,"MASK","");
		final Set<String> ignores=new SHashSet(CMParms.parseCommas(ignore.toUpperCase(),true));
		final MaskingLibrary.CompiledZMask mask=CMLib.masking().maskCompile(maskStr);
		if(dbClass.length()==0)
		{
			mob.tell(L("This command requires DBCLASS= to be set."));
			return false;
		}
		if(dbService.length()==0)
		{
			mob.tell(L("This command requires DBSERVICE= to be set."));
			return false;
		}
		if(dbUser.length()==0)
		{
			mob.tell(L("This command requires DBUSER= to be set."));
			return false;
		}
		if(dbPass.length()==0)
		{
			mob.tell(L("This command requires DBPASS= to be set."));
			return false;
		}

		dbConnector=new DBConnector(dbClass,dbService,dbUser,dbPass,dbConns,dbPingIntMins,dbReuse,false,false);
		dbConnector.reconnect();
		final DBInterface dbInterface = new DBInterface(dbConnector,null);

		final DBConnection DBTEST=dbConnector.DBFetch();
		if(DBTEST!=null)
			dbConnector.DBDone(DBTEST);
		mob.tell(L("Loading database rooms..."));
		final List<Room> rooms = new LinkedList<Room>();
		if((!dbConnector.amIOk())||(!dbInterface.isConnected()))
		{
			mob.tell(L("Failed to connect to database."));
			return false;
		}
		if(scope.equalsIgnoreCase("AREA"))
			rooms.addAll(Arrays.asList(dbInterface.DBReadRoomObjects(mob.location().getArea().Name(), false)));
		else
		if(scope.equalsIgnoreCase("ROOM"))
		{
			final Room R=dbInterface.DBReadRoomObject(mob.location().roomID(), false);
			if(R!=null)
				rooms.add(R);
		}
		else
		for(final Enumeration<Area> e=CMLib.map().areas();e.hasMoreElements();)
			rooms.addAll(Arrays.asList(dbInterface.DBReadRoomObjects(e.nextElement().Name(), false)));
		if(rooms.size()==0)
		{
			mob.tell(L("No rooms found."));
			return false;
		}
		for(final Room R : rooms)
			dbInterface.DBReadContent(R.roomID(),R,false);
		mob.tell(L("Data loaded, starting scan."));
		final Comparator<MOB> convM=new Comparator<MOB>()
		{
			@Override
			public int compare(MOB arg0, MOB arg1)
			{
				final int x=arg0.ID().compareTo(arg1.ID());
				return(x!=0)?x:arg0.Name().compareTo(arg1.Name());
			}
		};
		final Comparator<Item> convI=new Comparator<Item>()
		{
			@Override
			public int compare(Item arg0, Item arg1)
			{
				final int x=arg0.ID().compareTo(arg1.ID());
				return(x!=0)?x:arg0.Name().compareTo(arg1.Name());
			}
		};
		try
		{
		for(final Room dbR : rooms)
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
				final Area.State oldFlags=R.getArea().getAreaState();
				R.getArea().setAreaState(Area.State.FROZEN);

				boolean updateMobs=false;
				boolean updateItems=false;
				final boolean updateRoom=false;
				R=CMLib.map().getRoom(R);
				CMLib.map().resetRoom(R);
				final List<MOB> mobSetL=new Vector<MOB>();
				for(final Enumeration<MOB> e=dbR.inhabitants();e.hasMoreElements();)
					mobSetL.add(e.nextElement());
				final MOB[] mobSet=mobSetL.toArray(new MOB[0]);
				Arrays.sort(mobSet, convM);
				String lastName="";
				int ct=1;
				final HashSet<MOB> doneM=new HashSet<MOB>();
				for(final MOB dbM : mobSet)
				{
					if(!lastName.equals(dbM.Name()))
						ct=1;
					else
						ct++;
					final String rName=dbM.Name()+"."+ct;
					MOB M=null;
					int ctr=ct;
					for(final Enumeration<MOB> m = R.inhabitants();m.hasMoreElements();)
					{
						final MOB M1=m.nextElement();
						if(M1.Name().equalsIgnoreCase(dbM.Name())&&((--ctr)<=0))
						{
							M=M1;
							break;
						}
					}
					if(M==null)
					{
						if(amMerging(doType,mask,dbM)&&(!ignore.contains("MISSING")))
						{
							if(mob.session().confirm(L("MOB: @x1.@x2 not in local room.\n\rWould you like to add it (y/N)?",dbR.roomID(),rName), L("N")))
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
								final MOB oldM=(MOB)M.copyOf();
								if((dbMerge(mob,"^MMOB "+dbR.roomID()+"."+rName+"^N",dbM,M, ignores))
								&&(!oldM.sameAs(M)))
								{
									Log.sysOut("Merge",mob.Name()+" modified mob "+dbR.roomID()+"."+rName);
									updateMobs=true;
								}
							}
						}
						final STreeSet<Item> itemSetL=new STreeSet<Item>(convI);
						for(final Enumeration<Item> e=dbM.items();e.hasMoreElements();)
							itemSetL.add(e.nextElement());
						final Item[] itemSet=itemSetL.toArray(new Item[0]);
						Arrays.sort(itemSet, convI);
						String lastIName="";
						int ict=1;
						final HashSet<Item> doneI=new HashSet<Item>();
						for(final Item dbI : itemSet)
						{
							if(!lastIName.equals(dbI.Name()))
								ict=1;
							else
								ict++;
							final String rIName=dbI.Name()+"."+ict;
							Item I=null;
							ctr=ict;
							for(final Enumeration<Item> i = M.items();i.hasMoreElements();)
							{
								final Item I1=i.nextElement();
								if(I1.Name().equalsIgnoreCase(dbI.Name())&&((--ctr)<=0))
								{
									I=I1;
									break;
								}
							}
							if(I==null)
							{
								if(amMerging(doType,mask,dbI)&&(!ignore.contains("MISSING")))
								{
									if(mob.session().confirm(L("Item: @x1.@x2.@x3 not in local room.\n\rWould you like to add it (y/N)?",dbR.roomID(),dbM.Name(),rIName), L("N")))
									{
										I=(Item)dbI.copyOf();
										M.addItem(I);
										doneI.add(I);
										final Item cI=(dbI.container()==null)?null:M.findItem(dbI.container().Name());
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
									final Item oldI=(Item)I.copyOf();
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
						for(final Enumeration<Item> i=M.items();i.hasMoreElements();)
						{
							final Item I=i.nextElement();
							if(amMerging(doType,mask,I)&&(!doneI.contains(I))&&(!ignore.contains("EXTRA")))
							{
								if(mob.session().confirm(L("Item: @x1.@x2.@x3 not in database.\n\rWould you like to delete it (y/N)?",R.roomID(),M.Name(),I.Name()), L("N")))
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
				for(final Enumeration<MOB> r=R.inhabitants();r.hasMoreElements();)
				{
					final MOB M=r.nextElement();
					if(amMerging(doType,mask,M)&&(!doneM.contains(M))&&(M.isMonster())&&(!ignore.contains("EXTRA")))
					{
						if(mob.session().confirm(L("MOB: @x1.@x2 not in database.\n\rWould you like to delete it (y/N)?",R.roomID(),M.Name()), L("N")))
						{
							R.delInhabitant(M);
							updateMobs=true;
							Log.sysOut("Merge",mob.Name()+" deleted mob "+R.roomID()+"."+M.Name());
						}
					}
				}

				final STreeSet<Item> itemSetL=new STreeSet<Item>(convI);
				for(final Enumeration<Item> e=dbR.items();e.hasMoreElements();)
					itemSetL.add(e.nextElement());
				final Item[] itemSet=itemSetL.toArray(new Item[0]);
				Arrays.sort(itemSet, convI);
				lastName="";
				ct=1;
				final HashSet<Item> doneI=new HashSet<Item>();
				for(final Item dbI : itemSet)
				{
					if(!lastName.equals(dbI.Name()))
						ct=1;
					else
						ct++;
					final String rName=dbI.Name()+"."+ct;
					Item I=null;
					int ctr=ct;
					for(final Enumeration<Item> i = R.items();i.hasMoreElements();)
					{
						final Item I1=i.nextElement();
						if(I1.Name().equalsIgnoreCase(dbI.Name())&&((--ctr)<=0))
						{
							I=I1;
							break;
						}
					}
					if(I==null)
					{
						if(amMerging(doType,mask,dbI)&&(!ignore.contains("MISSING")))
						{
							if(mob.session().confirm(L("Item: @x1.@x2 not in local room.\n\rWould you like to add it (y/N)?",dbR.roomID(),rName), L("N")))
							{
								I=(Item)dbI.copyOf();
								R.addItem(I);
								doneI.add(I);
								final Item cI=(dbI.container()==null)?null:R.findItem(dbI.container().Name());
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
							final Item oldI=(Item)I.copyOf();
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
				for(final Enumeration<Item> i=R.items();i.hasMoreElements();)
				{
					final Item I=i.nextElement();
					if(amMerging(doType,mask,I)&&(!doneI.contains(I))&&(!ignore.contains("EXTRA")))
					{
						if(mob.session().confirm(L("Item: @x1.@x2 not in database.\n\rWould you like to delete it (y/N)?",R.roomID(),I.Name()), L("N")))
						{
							R.delItem(I);
							updateItems=true;
							Log.sysOut("Merge",mob.Name()+" deleted item "+R.roomID()+"."+I.Name());
						}
					}
				}
				if(updateRoom)
					CMLib.database().DBUpdateRoom(R);
				if(updateItems)
					CMLib.database().DBUpdateItems(R);
				if(updateMobs)
					CMLib.database().DBUpdateMOBs(R);
				CMLib.map().resetRoom(R);
				R.getArea().setAreaState(oldFlags);
			}
			dbR.destroy();
		}
		mob.tell(L("Done"));
		}
		catch(final CMException cme)
		{
			mob.tell(L("Cancelled."));
		}
		dbInterface.shutdown();
		return true;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

	@Override
	public boolean securityCheck(MOB mob)
	{
		return CMSecurity.isAllowedAnywhere(mob,CMSecurity.SecFlag.MERGE);
	}

}
