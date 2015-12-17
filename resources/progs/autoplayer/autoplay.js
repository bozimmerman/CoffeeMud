//include resources/progs/autoplayer/inc_stringfuncs.js
//include resources/progs/autoplayer/inc_levelup.js

var promptMarker=".*<(\\d+)Hp (\\d+)m (\\d+)mv>.*";
var hitpoints=20;
var mana=100;
var move=100;
var socialsCache = [];

var s;
var mudUsesAccountSystem = false;
if(!login())
{
	stderr("Unable to login!");
	AutoCrash___UNABLE_TO_LOGIN;
}

s = waitFor("(?>Name|Account name).*");
if(!startsWith(s.toLowerCase(),"name"))
	mudUsesAccountSystem = true;

writeLine(name());
if(mudUsesAccountSystem)
{
	s = waitFor("(?>Is this a new account you would like to create \\(y/N\\)\\?|Password).*");
	//stderr("Account System Not Supported Yet!");
	if(startsWith(s.toLowerCase(),"password"))
	{
		writeLine(name());
		s = waitFor("(?>Command or Name ).*");
		writeLine("L");
		s = waitFor("(?>"+name()+"|Command or Name).*");
		if(startsWith(s,"Command or Name"))
		{
			writeLine("NEW "+name());
			s = waitFor("(?>Create a new character called).*");
			writeLine("Y");
			finishCreateCharacter();
		}
		else
		{
			writeLine(name());
			startPlaying();
		}
	}
	else
	{
		writeLine("Y");
		s = waitFor("(?>Do you want ANSI colors \\(Y/n\\)\\?).*");
		writeLine("N");
		s = waitFor("(?>Enter an account password).*");
		writeLine(name());
		s = waitFor("(?>Enter your e-mail address).*");
		writeLine("someone@nowhere.com");
		s = waitFor("(?>Please enter a name for your character|Command or Name).*");
		if(startsWith(s.toLowerCase(),"command or name"))
			writeLine("NEW "+name());
		else
			writeLine(name());
		s = waitFor("(?>Create a new character called).*");
		writeLine("Y");
		finishCreateCharacter();
		
	}
}
else
{
	s = waitFor("(?>Is this a new character you would like to create \\(y/N\\)\\?|Password).*");
	if(startsWith(s.toLowerCase(),"password"))
	{
		writeLine(name());
		startPlaying();
	}
	else
	{
		writeLine("Y");
		s = waitFor("(?>Enter a password:).*");
		writeLine(name());
		s = waitFor("(?>Enter your e-mail address:).*");
		writeLine("someone@nowhere.com");
		s = waitFor("(?>Do you want ANSI colors \\(Y/n\\)\\?).*");
		writeLine("N");
		finishCreateCharacter();
	}
}

function doAnySocial()
{
	if(socialsCache.length == 0)
	{
		writeLine("socials");
		s = waitFor("Complete socials list:");
		clearOutbuffer();
		waitForPrompt();
		s = getAccumulated();
		var lines = s.split("\n");
		var i;
		for(i=0;i<lines.length;i++)
		{
			if(lines[i].length == 0)
				break;
			var set=splittrimnoempty(lines[i],' ');
			var x;
			for(x=0;x<set.length;x++)
			{
				if(set[x].length > 0)
					socialsCache.push(set[x]);
			}
		}
	}
	if(socialsCache.length == 0)
		writeLine("smile");
	else
		writeLine(socialsCache[rand(socialsCache.length)]);
}

function startPlaying()
{
	waitForPrompt();
	while(true)
	{
		doAnySocial();
		waitForPrompt();
	}
}

function waitForPrompt()
{
	var s=waitForMultiMatch(promptMarker,3);
	hitpoints=s[0];
	mana=s[1];
	move=s[2];
}

function finishCreateCharacter()
{
	var s;
	s = waitFor("(?>Please choose from the following races|Please select from the following: F).*");
	if(startsWith(s.toLowerCase(),"please select from"))
	{
		writeLine("F");
		s = waitFor("(?>Please choose from the following races).*");
	}
	
	s = waitFor(".*\\[(.*)\\].*");
	var races=splittrimnoempty(s,', ');
	var race=races[rand(races.length)];
	if(startsWith(race,"or "))
		race=race.substr(3);
	writeLine(race);
	s = waitFor(".*(correct \\(Y/n\\)\\?).*");
	writeLine("Y");
	s = waitFor("(?>What is your gender \\(M/F\\)\\?).*");
	if(rand(1)==1)
		writeLine("M");
	else
		writeLine("F");
	//s = waitFor(".*This would qualify you for (.*)\\..*");
	s = waitFor(".*(?>re\\-roll \\(y/N\\)\\?|.*R for random roll).*");
	if(s.indexOf("R for random roll")>0)
	{
		writeLine("R");
		s = waitFor(".*(?>re\\-roll \\(y/N\\)\\?|.*R for random roll).*");
		if(s.indexOf("R for random roll")>0)
			writeLine("");
	}
	else
		writeLine("N");
	s = waitFor("(?>Please choose from the following Classes).*");
	s = waitFor(".*\\[(.*)\\].*");
	var cclasses=splittrimnoempty(s,', ');
	var cclass=cclasses[rand(cclasses.length)];
	if(startsWith(cclass,"or "))
		cclass=cclass.substr(3);
	writeLine(cclass);
	s = waitFor(".*(correct \\(Y/n\\)\\?).*");
	writeLine("Y");
	s = waitFor(".*Select one: (.*)\\..*");
	var achoices=splittrimnoempty(s,', ');
	var achoice=achoices[rand(achoices.length)];
	if(startsWith(achoice,"or "))
		achoice=achoice.substr(3);
	writeLine(achoice);
	waitFor(".*Press Enter to begin:.*");
	writeLine("");
	startPlaying();
}