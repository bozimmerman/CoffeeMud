package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Merge extends StdCommand
{
	public Merge(){}

	private String[] access={"MERGE"};
	public String[] getAccessWords(){return access;}

	public static String getStat(Environmental E, String stat)
	{
		if((stat!=null)&&(stat.length()>0)&&(stat.equalsIgnoreCase("REJUV")))
		{
			if(E.baseEnvStats().rejuv()==Integer.MAX_VALUE)
				return "0";
			else
				return ""+E.baseEnvStats().rejuv();
		}
		else
			return E.getStat(stat);
	}

	public static void setStat(Environmental E, String stat, String value)
	{
		if((stat!=null)&&(stat.length()>0)&&(stat.equalsIgnoreCase("REJUV")))
			E.baseEnvStats().setRejuv(Util.s_int(value));
		else
			E.setStat(stat,value);
	}

	public static void mergedebugtell(MOB mob, String msg)
	{
		if(mob!=null) mob.tell(msg);
		Log.sysOut("MERGE",msg);
	}

	private static boolean tryMerge(MOB mob,
									Room room,
									Environmental E,
									Vector things,
									Vector changes,
									Vector onfields,
									Vector ignore,
									boolean noisy)
	{
		boolean didAnything=false;
		Vector efields=new Vector();
		Vector allMyFields=new Vector();
		String[] EFIELDS=E.getStatCodes();
		for(int i=0;i<EFIELDS.length;i++)
			if(!efields.contains(EFIELDS[i]))
				efields.addElement(EFIELDS[i]);
		efields.addElement("REJUV");
		allMyFields=(Vector)efields.clone();
		for(int v=0;v<ignore.size();v++)
			if(efields.contains(ignore.elementAt(v)))
				efields.removeElement(ignore.elementAt(v));
		for(int v=0;v<changes.size();v++)
			if(efields.contains(changes.elementAt(v)))
				efields.removeElement(changes.elementAt(v));
		if(noisy) mergedebugtell(mob,"AllMy-"+Util.toStringList(allMyFields));
		if(noisy) mergedebugtell(mob,"efields-"+Util.toStringList(efields));
		for(int t=0;t<things.size();t++)
		{
			Environmental E2=(Environmental)things.elementAt(t);
			if(noisy) mergedebugtell(mob,E.name()+"/"+E2.name()+"/"+CMClass.className(E)+"/"+CMClass.className(E2));
			if(CMClass.className(E).equals(CMClass.className(E2)))
			{
				Vector fieldsToCheck=null;
				if(onfields.size()>0)
				{
					fieldsToCheck=new Vector();
					for(int v=0;v<onfields.size();v++)
						if(efields.contains(onfields.elementAt(v)))
							fieldsToCheck.addElement(onfields.elementAt(v));
				}
				else
					fieldsToCheck=(Vector)efields.clone();

				boolean checkedOut=fieldsToCheck.size()>0;
				if(noisy) mergedebugtell(mob,"fieldsToCheck-"+Util.toStringList(fieldsToCheck));
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
					Vector fieldsToChange=null;
					if(changes.size()==0)
						fieldsToChange=(Vector)allMyFields.clone();
					else
					{
						fieldsToChange=new Vector();
						for(int v=0;v<changes.size();v++)
							if(allMyFields.contains(changes.elementAt(v)))
								fieldsToChange.addElement(changes.elementAt(v));
					}
					if(noisy) mergedebugtell(mob,"fieldsToChange-"+Util.toStringList(fieldsToChange));
					for(int i=0;i<fieldsToChange.size();i++)
					{
						String field=(String)fieldsToChange.elementAt(i);
						if(noisy) mergedebugtell(mob,E.name()+" wants to change "+field+" value "+getStat(E,field)+" to "+getStat(E2,field)+"/"+(!getStat(E,field).equals(getStat(E2,field))));
						if(!getStat(E,field).equals(getStat(E2,field)))
						{
							setStat(E,field,getStat(E2,field));
							Log.sysOut("Merge","The "+Util.capitalize(field)+" field on "+E.Name()+" in "+room.roomID()+" was changed to "+getStat(E2,field)+".");
							didAnything=true;
						}
					}
				}
			}
		}
		if(didAnything)
		{
			E.recoverEnvStats();
			if(E instanceof MOB)
			{
				((MOB)E).recoverCharStats();
				((MOB)E).recoverMaxState();
			}
			E.text();
		}
		return didAnything;
	}
	
	public void sortEnumeratedList(Enumeration e, Vector allKnownFields, StringBuffer allFieldsMsg)
	{
		for(;e.hasMoreElements();)
		{
			Environmental E=(Environmental)e.nextElement();
			String[] fields=E.getStatCodes();
			for(int x=0;x<fields.length;x++)
				if(!allKnownFields.contains(fields[x]))
				{
					allKnownFields.addElement(fields[x]);
					allFieldsMsg.append(fields[x]+" ");
				}
		}
	}

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(!mob.isASysOp(null))
		{
			mob.tell("Only the Archons can do that.");
			return false;
		}
		boolean noisy=false;
		Vector placesToDo=new Vector();
		commands.removeElementAt(0);
		if(commands.size()==0)
		{
			mob.tell("Merge what file?");
			return false;
		}
		if(mob.isMonster())
		{
			mob.tell("No can do.");
			return false;
		}
		boolean prompt=true;
		if((commands.size()>0)&&
		   ((String)commands.elementAt(0)).equalsIgnoreCase("noprompt"))
		{
			commands.removeElementAt(0);
			prompt=false;
		}
		if((commands.size()>0)&&
		   ((String)commands.elementAt(0)).equalsIgnoreCase("?"))
		{
			StringBuffer allFieldsMsg=new StringBuffer("");
			Vector allKnownFields=new Vector();
			sortEnumeratedList(CMClass.mobTypes(),allKnownFields,allFieldsMsg);
			sortEnumeratedList(CMClass.items(),allKnownFields,allFieldsMsg);
			sortEnumeratedList(CMClass.weapons(),allKnownFields,allFieldsMsg);
			sortEnumeratedList(CMClass.armor(),allKnownFields,allFieldsMsg);
			sortEnumeratedList(CMClass.clanItems(),allKnownFields,allFieldsMsg);
			sortEnumeratedList(CMClass.miscMagic(),allKnownFields,allFieldsMsg);
			sortEnumeratedList(CMClass.miscTech(),allKnownFields,allFieldsMsg);
			mob.tell("Valid field names are "+allFieldsMsg.toString());
			return false;
		}
		if((commands.size()>0)&&
		   ((String)commands.elementAt(0)).equalsIgnoreCase("room"))
		{
			commands.removeElementAt(0);
			placesToDo.addElement(mob.location());
		}
		if((commands.size()>0)&&
		   ((String)commands.elementAt(0)).equalsIgnoreCase("area"))
		{
			commands.removeElementAt(0);
			placesToDo.addElement(mob.location().getArea());
		}
		if((commands.size()>0)&&
		   ((String)commands.elementAt(0)).equalsIgnoreCase("world"))
		{
			commands.removeElementAt(0);
			placesToDo=new Vector();
		}
		if(commands.size()==0)
		{
			mob.tell("Merge what file?");
			return false;
		}
		String filename=(String)commands.lastElement();
		commands.remove(filename);
		StringBuffer buf=Resources.getFile(filename);
		if(buf==null)
		{
			mob.tell("File not found at: '"+filename+"'!");
			return false;
		}

		Vector changes=new Vector();
		Vector onfields=new Vector();
		Vector ignore=new Vector();
		Vector use=null;
		Vector allKnownFields=new Vector();
		Vector things=new Vector();
		boolean aremobs=false;
		if((buf.length()>20)&&(buf.substring(0,20).indexOf("<MOBS>")>=0))
		{
			if(mob.session()!=null)
				mob.session().rawPrint("Unpacking mobs from file: '"+filename+"'...");
			String error=CoffeeMaker.addMOBsFromXML(buf.toString(),things,mob.session());
			if(mob.session()!=null)	mob.session().rawPrintln("!");
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
			String error=CoffeeMaker.addItemsFromXML(buf.toString(),things,mob.session());
			if(mob.session()!=null)	mob.session().rawPrintln("!");
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
			sortEnumeratedList(CMClass.items(),allKnownFields,allFieldsMsg);
			sortEnumeratedList(CMClass.weapons(),allKnownFields,allFieldsMsg);
			sortEnumeratedList(CMClass.armor(),allKnownFields,allFieldsMsg);
			sortEnumeratedList(CMClass.clanItems(),allKnownFields,allFieldsMsg);
			sortEnumeratedList(CMClass.miscMagic(),allKnownFields,allFieldsMsg);
			sortEnumeratedList(CMClass.miscTech(),allKnownFields,allFieldsMsg);
		}

		allKnownFields.addElement("REJUV");
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
			int x=str.indexOf(",");
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
						use.addElement(s);
					else
					{
						mob.tell("'"+s+"' is an unknown field name.  Valid fields include: "+allFieldsMsg.toString());
						return false;
					}
				}
				str=str.substring(x+1).trim();
				x=str.indexOf(",");
			}
			if(str.length()>0)
			{
				if(use==null)
				{
					mob.tell("'"+str+"' is an unknown parameter!");
					return false;
				}
				if(allKnownFields.contains(str))
					use.addElement(str);
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
		for(Enumeration a=CMMap.areas();a.hasMoreElements();)
		{
			Area A=(Area)a.nextElement();
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
				for(Enumeration r=A.getMap();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					placesToDo.addElement(R);
				}
			}
			else
			if(placesToDo.elementAt(i) instanceof Room)
				if(mob.session()!=null)	mob.session().rawPrint(".");
			else
				return false;
		}
		// now do the merge...
		if(mob.session()!=null)
			mob.session().rawPrint("Merging and saving...");
		if(noisy) mergedebugtell(mob,"Rooms to do: "+placesToDo.size());
		if(noisy) mergedebugtell(mob,"Things loaded: "+things.size());
		if(noisy) mergedebugtell(mob,"On fields="+Util.toStringList(onfields));
		if(noisy) mergedebugtell(mob,"Ignore fields="+Util.toStringList(ignore));
		if(noisy) mergedebugtell(mob,"Change fields="+Util.toStringList(changes));
		Log.sysOut("Import",mob.Name()+" merge '"+filename+"'.");
		for(int r=0;r<placesToDo.size();r++)
		{
			Room R=(Room)placesToDo.elementAt(r);
			if(R.roomID().length()==0) continue;
			boolean oldMobility=R.getArea().getMobility();
			R.getArea().toggleMobility(false);
			CoffeeUtensils.resetRoom(R);
			boolean savemobs=false;
			boolean saveitems=false;
			if(aremobs)
			{
				for(int m=0;m<R.numInhabitants();m++)
				{
					MOB M=R.fetchInhabitant(m);
					if((M!=null)&&(M.isEligibleMonster()))
						if(tryMerge(mob,R,M,things,changes,onfields,ignore,noisy))
							savemobs=true;
				}
			}
			else
			{
				for(int i=0;i<R.numItems();i++)
				{
					Item I=R.fetchItem(i);
					if((I!=null)&&(tryMerge(mob,R,I,things,changes,onfields,ignore,noisy)))
						saveitems=true;
				}
				for(int m=0;m<R.numInhabitants();m++)
				{
					MOB M=R.fetchInhabitant(m);
					if((M!=null)&&(M.isEligibleMonster()))
					{
						for(int i=0;i<M.inventorySize();i++)
						{
							Item I=M.fetchInventory(i);
							if((I!=null)&&(tryMerge(mob,R,I,things,changes,onfields,ignore,noisy)))
								savemobs=true;
						}
						if(CoffeeUtensils.getShopKeeper(M)!=null)
						{
							Vector V=CoffeeUtensils.getShopKeeper(M).getUniqueStoreInventory();
							for(int i=0;i<V.size();i++)
							{
								if(V.elementAt(i) instanceof Item)
								{
									Item I=(Item)V.elementAt(i);
									if((I!=null)&&(tryMerge(mob,R,I,things,changes,onfields,ignore,noisy)))
										savemobs=true;
								}
							}
						}
					}
				}
			}
			if(saveitems) CMClass.DBEngine().DBUpdateItems(R);
			if(savemobs) CMClass.DBEngine().DBUpdateMOBs(R);
			if(mob.session()!=null)	mob.session().rawPrint(".");
			R.getArea().toggleMobility(oldMobility);
		}

		if(mob.session()!=null)	mob.session().rawPrintln("!\n\rDone!");
		for(int i=0;i<placesToDo.size();i++)
			((Room)placesToDo.elementAt(i)).getArea().toggleMobility(true);
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	public boolean arcCommand(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
