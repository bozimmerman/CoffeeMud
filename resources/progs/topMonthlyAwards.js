/**
 * This script goes through the winners of the previous
 * months top awards and gives them an award.
 * Args are: 
 * 1:  [top number winners - default 3]
 * 2:  [list of awards as per rewards in achievements.ini], 
 *     comma delimited to award based on rank 
 * 
 * To use, create a cron job as follows:
 * create cron "monthlytopawards" "monthly + (2 hours)"
 * and then modify the cron job to JRUN this script.
 */

var CMLib=Packages.com.planet_ink.coffee_mud.core.CMLib;
var CMClass=Packages.com.planet_ink.coffee_mud.core.CMClass;
var CMFile=Packages.com.planet_ink.coffee_mud.core.CMFile;
var CMStrings=Packages.com.planet_ink.coffee_mud.core.CMStrings;
var CMProps=Packages.com.planet_ink.coffee_mud.core.CMProps;
var CMParms=Packages.com.planet_ink.coffee_mud.core.CMParms;
var CMath=Packages.com.planet_ink.coffee_mud.core.CMath;
var Log=Packages.com.planet_ink.coffee_mud.core.Log;
var System=Packages.java.lang.System;
var Calendar = Packages.java.util.Calendar;
var e;

var topNum = 3;
var awardsStr = ""
if(numParms() > 0)
	topNum = Number(""+getParm(0));
if(numParms() > 1)
{
	awardsStr = getParm(1);
	for(e=2;e<numParms();e++)
		awardsStr += " "+getParm(e);
	awardsStr = awardsStr.toUpperCase().trim();
}
var awardsList = awardsStr.split(',');
var MONTHLY = Packages.com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod.MONTH;
var prideStats = Packages.com.planet_ink.coffee_mud.Common.interfaces.PrideStats.PrideStat;

for(e=CMLib.libraries(Packages.com.planet_ink.coffee_mud.core.CMLib.Library.PLAYERS);e.hasMoreElements();)
{
	var playerLib = e.nextElement();
	var pi;
	for(pi=0;pi<prideStats.values().length;pi++)
	{
		var prideStat = prideStats.values()[pi];
		var statName = prideStat.name().replace('_',' ').toLowerCase();
		var winners = playerLib.getPreviousTopPridePlayers(MONTHLY, prideStat);
		var accountsDone = [];
		var numDone = 0;
		for(var wi=0;wi<winners.size();wi++)
		{
			var winnerName = winners.get(wi).first;
			var winnerM = playerLib.getLoadPlayer(winnerName);
			// get the player mob himself
			if(winnerM != null)
			{
				// check if accounts exist on this player 
				if((winnerM.playerStats() != null) && (winnerM.playerStats().getAccount() != null))
				{
					var accountName = winnerM.playerStats().getAccount().name();
					// check if player is from an already awarded account,
					if(accountsDone.indexOf(accountName) >= 0)
						continue; // skip if player is from an awarded account
					accountsDone.push(accountName);
				}
				var subj = 'Monthly Top Player for '+statName;
				var from = 'noreply';
				var to = winnerM.Name();
				var msg = ' Congratulations! For placing '+(wi+1)+CMath.numAppendage(wi+1)+' in the monthly prize for '+statName+':  ';
				var award = awardsStr;
				if(awardsList.length > 1)
				{
					if(numDone < awardsList.length)
						award = awardsList[numDone];
					else
						award = awardsList[awardsList.length-1];
				}
				if(award.length > 0)
					msg += CMStrings.removeColors(CMLib.achievements().giveAwards(winnerM, award));
				else
					msg += ' You win this notification!';
				numDone++;
				CMLib.smtp().emailOrJournal(from, from, to, subj, msg);
				if(numDone >= topNum)
					break;
			}
		}
	}
}
