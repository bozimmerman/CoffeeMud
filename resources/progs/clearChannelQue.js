var lib=Packages.com.planet_ink.coffee_mud.core.CMLib;
var parms=getParms().toUpperCase();
var channelInt=lib.channels().getChannelIndex(parms);
if(channelInt<0)
	mob().tell("The channel "+parms+" does not exist.");
else
{
	var que=lib.channels().getChannelQue(channelInt);
	if(que.size()==0)
		mob().tell("That channel has no entries.");
	else
	{
		que.clear();
		mob().tell("Channel "+parms+" que has been cleared.");
	}
}
