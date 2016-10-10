//extends com.planet_ink.coffee_mud.Items.Software.GenSoftware
//implements com.planet_ink.coffee_mud.Items.interfaces.ArchonOnly

function ID() 
{
	return "ShipDiagProgram";
}

var lib=Packages.com.planet_ink.coffee_mud.core.CMLib;

function newInstance()
{
    var newOne=this.super$newInstance();
    newOne.setName("a diag program disk");
    newOne.setDisplayText("a small disk sits here.");
    newOne.setDescription("It appears to be a diagnostics terminal program.");
	return newOne;
}

function getParentMenu() 
{
	return "";
}

function getInternalName() 
{
	return "SCRIPTDIAG";
}

function isActivationString(word)
{
	return this.isCommandString(word, false);
}

function isDeActivationString(word)
{
	return this.isCommandString(word, false);
}

function isCommandString(word, isActive)
{
	if(!isActive)
	{
		return word.toUpperCase() == "DIAGTERM";
	}
	else
	{
		return true;
	}
}

function getActivationMenu()
{
	return "DIAGTERM: Internal computer systems diagnostics termanal";
}

function onActivate(mob, message)
{
	this.addScreenMessage("Diagnostics Computer Terminal Activated");
	this.setCurrentScreenDisplay("Diagnostics Computer Terminal Activated\n\rEnter a JavaScript command to use.");
}

function onTyping(mob, message)
{
	var cmd=''+message;
	var x=cmd.indexOf('`');
	while(x>=0) 
	{
		cmd = cmd.substr(0,x)+'\''+cmd.substr(x+1);
		x=cmd.indexOf('`')
	}
	this.addScreenMessage("Input : "+cmd);
	try 
	{
		this.addScreenMessage("Output: "+eval(cmd));
	}
	catch(err)
	{
		this.addScreenMessage("Error : "+(err ? err.message : '?'));
	}
}
