package com.planet_ink.grinder;
import java.util.Vector;
import com.planet_ink.coffee_mud.utils.XMLManager;

public class GenGrinder
{
	public final static int CAN_SEE=1;
	public final static int CAN_SEE_HIDDEN=2;
	public final static int CAN_SEE_INVISIBLE=4;
	public final static int CAN_SEE_EVIL=8;
	public final static int CAN_SEE_GOOD=16;
	public final static int CAN_SEE_SNEAKERS=32;
	public final static int CAN_SEE_BONUS=64;
	public final static int CAN_SEE_DARK=128;
	public final static int CAN_SEE_INFRARED=256;
	public final static int CAN_HEAR=512;
	public final static int CAN_MOVE=1024;
	public final static int CAN_SMELL=2048;
	public final static int CAN_TASTE=4096;
	public final static int CAN_SPEAK=8192;
	public final static int CAN_BREATHE=16384;
	
	public final static int ALLMASK=(int)Math.round((Integer.MAX_VALUE/2)-0.5);

	// dispositions
	public final static int IS_SEEN=1;
	public final static int IS_HIDDEN=2;
	public final static int IS_INVISIBLE=4;
	public final static int IS_EVIL=8;
	public final static int IS_GOOD=16;
	public final static int IS_SNEAKING=32;
	public final static int IS_BONUS=64;
	public final static int IS_DARK=128;
	public final static int IS_INFRARED=256;
	public final static int IS_SLEEPING=512;
	public final static int IS_SITTING=1024;
	public final static int IS_FLYING=2048;
	public final static int IS_SWIMMING=4096;
	public final static int IS_LIGHT=8192;
	
    public static int s_int(String i)
    {
        try
        {
            return Integer.parseInt(i);
        }
        catch(Exception e)
        {
            return 0;
        }
    }
    
    public static long s_long(String i)
    {
        try
        {
            return Long.parseLong(i);
        }
        catch(Exception e)
        {
            return 0;
        }
    }
    
	public static boolean get(int x, int m)
	{
		return (x&m)==m;
	}
	
	public static int flags(MapGrinder.GenGen E)
	{
		int f=0;
		if(E.isDroppable)
			f=f|1;
		if(E.isGettable)
			f=f|2;
		if(E.isReadable)
			f=f|4;
		if(E.isRemovable)
			f=f|8;
		if(E.isTrapped)
			f=f|16;
		if(E.hasADoor)
			f=f|32;
		if(E.hasALock)
			f=f|64;
		if(E.doorDefaultsClosed)
			f=f|128;
		if(E.doorDefaultsLocked)
			f=f|256;
		if(E.levelRestricted)
			f=f|512;
		if(E.classRestricted)
			f=f|1024;
		return f;
	}
	
	public static void setFlags(MapGrinder.GenGen E, int f)
	{
		E.isDroppable=get(f,1);
		E.isGettable=get(f,2);
		E.isReadable=get(f,4);
		E.isRemovable=get(f,8);
		E.isTrapped=get(f,16);
		E.hasADoor=get(f,32);
		E.hasALock=get(f,64);
		E.doorDefaultsClosed=get(f,128);
		E.doorDefaultsLocked=get(f,256);
		E.levelRestricted=get(f,512);
		E.classRestricted=get(f,1024);
	}
	
	public static String getPropertiesStr(Object obj, MapGrinder.GenGen E)
	{
		StringBuffer text=new StringBuffer("");
		
		text.append(getEnvPropertiesStr(E)
			+XMLManager.convertXMLtoTag("FLAG",flags(E)));
		
		text.append(
		 XMLManager.convertXMLtoTag("CLOSTX",E.closedText)
		+XMLManager.convertXMLtoTag("DOORNM",E.doorName)
		+XMLManager.convertXMLtoTag("OPENNM",E.openName)
		+XMLManager.convertXMLtoTag("CLOSNM",E.closeName)
		+XMLManager.convertXMLtoTag("KEYNM",E.keyName)
		+XMLManager.convertXMLtoTag("OPENTK",E.openDelayTicks));
		
		text.append(
			XMLManager.convertXMLtoTag("IDENT",E.secretIdentity)
		+XMLManager.convertXMLtoTag("VALUE",E.baseGoldValue)
		+XMLManager.convertXMLtoTag("MTRAL",E.materialCode)
    	+XMLManager.convertXMLtoTag("USES",E.uses));
		
		text.append(
		XMLManager.convertXMLtoTag("READ",E.readableText)
		+XMLManager.convertXMLtoTag("CAPA",E.capacity)
		+XMLManager.convertXMLtoTag("WORNL",E.logicalAnd)
		+XMLManager.convertXMLtoTag("WORNB",E.properLocationBitmap));
		
		if((obj instanceof MapGrinder.Item)&&(((MapGrinder.Item)obj).classID.equals("GenFood")))
    		text.append(XMLManager.convertXMLtoTag("CAPA2",E.nourishUse));
        else
    		text.append(XMLManager.convertXMLtoTag("CAPA2",E.nourishTotal));
    	text.append(XMLManager.convertXMLtoTag("DRINK",E.nourishUse));
		
		text.append(XMLManager.convertXMLtoTag("TYPE",E.weaponType));
		text.append(XMLManager.convertXMLtoTag("CLASS",E.weaponClassification));
		
		text.append(XMLManager.convertXMLtoTag("ALIG",E.alignment));
		text.append(XMLManager.convertXMLtoTag("MONEY",E.money));
		text.append(XMLManager.convertXMLtoTag("GENDER",""+(char)E.gender));
		
		StringBuffer abilitystr=new StringBuffer("");
		if(E.abilities!=null)
		    for(int b=0;b<E.abilities.size();b++)
			    abilitystr.append(E.abilities.elementAt(b)+";");
		
		text.append(XMLManager.convertXMLtoTag("ABLE",abilitystr.toString()));
			
		StringBuffer itemstr=new StringBuffer("");
		if(E.items!=null)
		    for(int b=0;b<E.items.size();b++)
			    itemstr.append(E.items.elementAt(b)+"/"+E.wornCodes.elementAt(b)+";");
		text.append(XMLManager.convertXMLtoTag("ITEM",itemstr.toString()));
			
		itemstr=new StringBuffer("");
		if(E.inventory!=null)
		    for(int b=0;b<E.inventory.size();b++)
			    itemstr.append(E.inventory.elementAt(b)+"/"+E.numItems.elementAt(b)+";");
		text.append(
			XMLManager.convertXMLtoTag("SELLCD",E.whatISell)
			+XMLManager.convertXMLtoTag("STORE",itemstr.toString()));
			
		return text.toString();
	}
	
	public static void setPropertiesStr(Object obj, MapGrinder.GenGen E, String buf)
	{
		setEnvProperties(E,buf);
		setFlags(E,s_int(XMLManager.returnXMLValue(buf,"FLAG")));
		
		E.closedText=XMLManager.returnXMLValue(buf,"CLOSTX");
		E.doorName=XMLManager.returnXMLValue(buf,"DOORNM");
		E.openName=XMLManager.returnXMLValue(buf,"OPENNM");
		E.closeName=XMLManager.returnXMLValue(buf,"CLOSNM");
		E.keyName=XMLManager.returnXMLValue(buf,"KEYNM");
		E.openDelayTicks=s_int(XMLManager.returnXMLValue(buf,"OPENTK"));
		
		E.secretIdentity=(XMLManager.returnXMLValue(buf,"IDENT"));
		E.baseGoldValue=(s_int(XMLManager.returnXMLValue(buf,"VALUE")));
		E.materialCode=(s_int(XMLManager.returnXMLValue(buf,"MTRAL")));
		E.uses=(s_int(XMLManager.returnXMLValue(buf,"USES")));
		E.capacity=(s_int(XMLManager.returnXMLValue(buf,"CAPA")));
		E.logicalAnd=(XMLManager.returnXMLBoolean(buf,"WORNL"));
		E.properLocationBitmap=(s_int(XMLManager.returnXMLValue(buf,"WORNB")));
		E.readableText=XMLManager.returnXMLValue(buf,"READ");
		if((obj instanceof MapGrinder.Item)&&(((MapGrinder.Item)obj).classID.equals("GenFood")))
    		E.nourishUse=s_int(XMLManager.returnXMLValue(buf,"CAPA2"));
        else
        {
    		E.nourishTotal=s_int(XMLManager.returnXMLValue(buf,"CAPA2"));
    		E.nourishUse=s_int(XMLManager.returnXMLValue(buf,"DRINK"));
    	}
		E.weaponType=s_int(XMLManager.returnXMLValue(buf,"TYPE"));
		E.weaponClassification=s_int(XMLManager.returnXMLValue(buf,"CLASS"));
		E.alignment=(s_int(XMLManager.returnXMLValue(buf,"ALIG")));
		E.money=(s_int(XMLManager.returnXMLValue(buf,"MONEY")));
		String g=XMLManager.returnXMLValue(buf,"GENDER");
		if(g.length()>0)
    		E.gender=(char)(g.charAt(0));
			
		String itemstr=XMLManager.returnXMLValue(buf,"ITEM");
		int y=itemstr.indexOf(";");
		E.items=null;
		E.wornCodes=null;
		while(y>=0)
		{
			String itemi=itemstr.substring(0,y);
			int wornCode=0;
			if(itemi.indexOf("/")>=0)
			{
				wornCode=s_int(itemi.substring(itemi.indexOf("/")+1));
				itemi=itemi.substring(0,itemi.indexOf("/"));
			}
			if(E.items==null)
			{
			    E.items=new Vector();
			    E.wornCodes=new Vector();
			}
			E.items.addElement(itemi);
			E.wornCodes.addElement(new Integer(wornCode));
			itemstr=itemstr.substring(y+1);
			y=itemstr.indexOf(";");
		}
		
		String abilitystr=XMLManager.returnXMLValue(buf,"ABLE");
		y=abilitystr.indexOf(";");
		E.abilities=null;
		while(y>=0)
		{
			String abilityi=abilitystr.substring(0,y);
			if(E.abilities==null)
			    E.abilities=new Vector();
			E.abilities.addElement(abilityi);
			abilitystr=abilitystr.substring(y+1);
			y=abilitystr.indexOf(";");
		}
			
		E.whatISell=s_int(XMLManager.returnXMLValue(buf,"SELLCD"));
		
		itemstr=XMLManager.returnXMLValue(buf,"STORE");
		y=itemstr.indexOf(";");
		E.inventory=null;
		E.numItems=null;
		while(y>=0)
		{
			String itemi=itemstr.substring(0,y);
			int number=1;
			if(itemi.indexOf("/")>=0)
			{
				number=s_int(itemi.substring(itemi.indexOf("/")+1));
				itemi=itemi.substring(0,itemi.indexOf("/"));
			}
			if(E.inventory==null)
			{
			    E.inventory=new Vector();
			    E.numItems=new Vector();
			}
			E.inventory.addElement(itemi);
			E.numItems.addElement(new Integer(number));
			itemstr=itemstr.substring(y+1);
			y=itemstr.indexOf(";");
		}
	}
	
	public static String getEnvPropertiesStr(MapGrinder.GenGen E)
	{
		StringBuffer text=new StringBuffer("");
		text.append(XMLManager.convertXMLtoTag("NAME",E.name));
		text.append(XMLManager.convertXMLtoTag("DESC",E.description));
		text.append(XMLManager.convertXMLtoTag("DISP",E.displayText));
		text.append(XMLManager.convertXMLtoTag("PROP",
			E.ability+";"+
			E.armor+";"+
			E.attack+";"+
			E.damage+";"+
			E.disposition+";"+
			E.level+";"+
			E.rejuv+";"+
			E.speed+";"+
			E.weight+";"+
			E.sensesMask+";"));
		
		StringBuffer behaviorstr=new StringBuffer("");
		if(E.behaviors!=null)
		    for(int b=0;b<E.behaviors.size();b++)
			    behaviorstr.append(E.behaviors.elementAt(b)+";");
		
		text.append(XMLManager.convertXMLtoTag("BEHAV",behaviorstr.toString()));
		
		return text.toString();
	}
	public static void setEnvProperties(MapGrinder.GenGen E, String buf)
	{
		E.name=(XMLManager.returnXMLValue(buf,"NAME"));
		E.description=(XMLManager.returnXMLValue(buf,"DESC"));
		E.displayText=(XMLManager.returnXMLValue(buf,"DISP"));
		String props=XMLManager.returnXMLValue(buf,"PROP");
		double[] nums=new double[11];
		int x=0;
		for(int y=props.indexOf(";");y>=0;y=props.indexOf(";"))
		{
			try
			{
				nums[x]=Double.parseDouble(props.substring(0,y));
			}
			catch(Exception e)
			{
				nums[x]=new Integer(s_int(props.substring(0,y))).doubleValue();
			}
			x++;
			props=props.substring(y+1);
		}
		E.ability=(int)Math.round(nums[0]);
		E.armor=(int)Math.round(nums[1]);
		E.attack=(int)Math.round(nums[2]);
		E.damage=(int)Math.round(nums[3]);
		E.disposition=(int)Math.round(nums[4]);
		E.level=(int)Math.round(nums[5]);
		//E.rejuv=(nums[6]);
		E.speed=(nums[7]);
		E.weight=(int)Math.round(nums[8]);
		E.sensesMask=(int)Math.round(nums[9]);
		
		String behaviorstr=XMLManager.returnXMLValue(buf,"BEHAV");
		
		x=behaviorstr.indexOf(";");
		E.behaviors=new Vector();
		while(x>=0)
		{
			String behaviori=behaviorstr.substring(0,x);
			E.behaviors.addElement(behaviori);
			behaviorstr=behaviorstr.substring(x+1);
			x=behaviorstr.indexOf(";");
		}
		
	}
	
}
