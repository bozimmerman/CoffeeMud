var CMLib=Packages.com.planet_ink.coffee_mud.core.CMLib;
var CMFile=Packages.com.planet_ink.coffee_mud.core.CMFile;
var System=Packages.java.lang.System;
/*
 * This script will show the AutoAward awards that the targeted
 * player is currently benefitting from.
 * JRUN /resources/progs/peekautoawards.js PLAYERNAME
 */


var aaobj = null;
var aahashlist = '';
var aalist = null;
var player = CMLib.players().getPlayer(getParms());
if(player == null)
	player = mob().location().fetchInhabitant(getParms());

if (player == null)
	mob().tell('No such pc/npc available as '+getParms());
else
{
	aaobj = player.fetchEffect('AutoAwards');
	if(aaobj == null)
		mob().tell('No auto awards found');
	else
	{
		aahashlist = aaobj.getStat('AUTOAWARDS');
		if(aahashlist.length()==0)
			mob().tell('Not under any auto awards ATM.');
		else
		{
			var aaliststr = aahashlist.split(';');
			var i;
			var p, P;
			aalist = [];
			for(i=0;i<aaliststr.length;i++)
			{
				for(p = CMLib.awards().getAutoProperties(); p.hasMoreElements();)
				{
					P=p.nextElement();
					if(P.hashCode() == aaliststr[i])
						aalist.push(P);
				}
			}
		}
	}
	if(aalist != null)
	{
		var i,x;
		for(i=0;i<aalist.length;i++)
		{
			var AA = aalist[i];
			for(x=0;x<AA.getProps().length;x++)
			{
				var PP = AA.getProps()[x];
				mob().tell(AA.getPlayerMask()+'/'+AA.getDateMask()+'/'+PP.first+'('+PP.second+')');
			}
		}
	}
}
