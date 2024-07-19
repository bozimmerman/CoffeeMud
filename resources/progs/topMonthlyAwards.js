/**
 * This script goes through the winners of the previous
 * months top awards and gives them an award.
 * Args are: 
 * 1:  [top number winners - default 3]
 * 2:  [list of awards as per rewards in achievements.ini]
 * 
 * To use, create a cron job as follows:
 * create cron "monthlytopawards" "monthly + (2 hours)"
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

var topNum = 3;
var awardsStr = ""
if(numParms() > 0)
	topNum = Number(""+getParm(0));
if(numParms() > 1)
	awardsStr = getParms(1)
var MONTHLY = Packages.com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod.MONTH;
var prideStats = Packages.com.planet_ink.coffee_mud.Common.interfaces.AccountStats.PrideStat;

for(e=CMLib.libraries(Packages.com.planet_ink.coffee_mud.core.CMLib.Library.PLAYERS);e.hasMoreElements();)
{
	var playerLib = e.nextElement();
	var pi;
	for(pi=0;pi<prideStats.values().length;pi++)
	{
		var prideStat = prideStats.values()[pi];
		var statName = prideStat.name().replace('_',' ').toLowerCase();
		var winners = playerLib.getTopPridePlayers(MONTHLY, prideStat);
		while(winners.size()>topNum)
			winners.remove(winners.size()-1);
		var wi;
		for(wi=0;wi<winners.size();wi++)
		{
			var winnerM = playerLib.getLoadPlayer(winners.get(wi).first);
			if(winnerM != null)
			{
				if(awardsStr.length > 0)
				{
					var msg = 'For placing '+(wi+1)+' in the monthly prize for '+statName+': ';
					winnerM.tell(msg + CMLib.achievements.giveAwards(winnerM, awardsStr));
				}
			}
		}
	}
}
