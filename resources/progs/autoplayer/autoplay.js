//include resources/progs/autoplayer/stringfuncs.js

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
	stderr("Account System Not Supported Yet!");
	AutoCrash___ACCOUNT_SYSTEM_NOT_SUPPORTED_YET;
}
else
{
	s = waitFor("(?>Is this a new character you would like to create \\(y/N\\)\\?|Password).*");
	if(startsWith(s.toLowerCase(),"password"))
	{
		writeLine(name());
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
		s = waitFor("(?>Please choose from the following races).*");
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
		s = waitFor(".*re\\-roll \\(y/N\\)\\?.*");
		writeLine("N");
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
		waitFor(".*Press >>ENTER<< to Begin!.*");
		//writeLine("");
		
		s = waitFor("(?>something).*");
	}
}
