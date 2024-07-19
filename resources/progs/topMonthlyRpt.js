/**
 * This script dumps the previous months top records as xml 
 * to the /resources/sys_reports directory.  
 * To use, create a cron job as follows:
 * create cron "monthlytops" "monthly + (2 hours)"
 * and then modify the cron job to JRUN this script.
 */

var CMLib=Packages.com.planet_ink.coffee_mud.core.CMLib;
var CMClass=Packages.com.planet_ink.coffee_mud.core.CMClass;
var CMFile=Packages.com.planet_ink.coffee_mud.core.CMFile;
var CMStrings=Packages.com.planet_ink.coffee_mud.core.CMStrings;
var Log=Packages.com.planet_ink.coffee_mud.core.Log;
var System=Packages.java.lang.System;
var Calendar = Packages.java.util.Calendar;
var e;

var topCmd=CMClass.getCommand("Top");
var calC=Calendar.getInstance();
var dir="::/resources/sys_reports/";
var dirF=new CMFile(dir, null);
if(!dirF.exists())
	dirF.mkdir();
	
for(e=CMLib.libraries(Packages.com.planet_ink.coffee_mud.core.CMLib.Library.PLAYERS);e.hasMoreElements();)
{
	var playerLib = e.nextElement();
	var threadName = playerLib.name();
	var filename = "::/resources/sys_reports/"+threadName+"_top_report_"+calC.get(Calendar.YEAR)+"-"+(calC.get(Calendar.MONTH)+1)+"-"+calC.get(Calendar.DAY_OF_MONTH)+".xml";
	var F=new CMFile(filename, null);
	if(!F.exists())
	{
		var mob=CMLib.map().deity();
		try
		{
			var T = Packages.java.lang.Boolean.TRUE;
			o = topCmd.executeInternal(mob, 0, T, T, playerLib);
			var str=CMStrings.removeColors(o.toString());
			F.saveText(str);
		}
		catch (e)
		{
			Log.debugOut(threadName+": Failed to Save");
			Log.errOut(e);
		}
	}
	else
	if(debugTopThread)
		Log.debugOut(threadName+": Won't Save");
}
