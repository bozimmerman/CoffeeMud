window.isElectron = typeof window.process === 'object' && window.process.versions && window.process.versions.electron;
var Siplet =
{
	VERSION_MAJOR: '3.2',
	VERSION_MINOR: '5',
	COFFEE_MUD: false,
	NAME: window.isElectron?'Sip MUD Client':'Siplet',
	R: /^win\.[\w]+(\.[\w]+)*$/
};

window.configlisteners = {};
window.sipConfigName = 'config1.1';
if(Siplet.COFFEE_MUD)
	window.sipConfigName = 'cmconfig1.1';
window.config = localStorage.getItem(window.sipConfigName);
if(window.config) 
	window.config = JSON.parse(window.config);
else
	window.config = {};
window.phonebook = [];
window.defAliases = [];
window.defScripts = [];
window.defTimers = [];
window.defPlugins = [];
window.defEntities = {
	"nbsp": "&nbsp;",
	"lt": "&lt;",
	"gt": "&gt;",
	"quot": "&quot;",
	"amp": "&amp;"
};
window.defTriggers = [
	{
		name: "Phonebook Account Name",
		allowed: 'win.pb && win.pb.accountName',
		regex: true,
		once: true,
		pattern: 'Account name:(?![\\s\\S]*\\n)',
		action: 'win.submitInput(win.pb.accountName)'
	},
	{
		name: "Phonebook Account Character",
		allowed: 'win.pb && win.pb.accountName && win.pb.user',
		regex: true,
		once: true,
		pattern: 'Command or Name \\(\\?\\):(?!\\[\\s\\S\\]*\\n)',
		action: 'win.submitInput(win.pb.user)'
	},
	{
		name: "Phonebook Character",
		allowed: 'win.pb && !win.pb.accountName && win.pb.user',
		regex: true,
		once: true,
		pattern: 'name:(?!\\[\\s\\S\\]*\\n)',
		action: 'win.submitInput(win.pb.user)'
	},
	{
		name: "Phonebook Password",
		allowed: 'win.pb && win.pb.user',
		regex: true,
		once: true,
		pattern: 'Password:(?![\\s\\S]*\\n)',
		action: 'win.submitHidden(win.pb.password)'
	}
];

function getConfigPath(path)
{
	path = path.split('/');
	var c = window.config;
	for(var i=0;i<path.length-1;i++)
	{
		var p = path[i];
		if(p.length > 0)
		{
			if(!(p in c))
				c[p] = {};
			c=c[p];
		}
	}
	return [c, path[path.length-1]];
}

function getConfig(path, def)
{
	var c = getConfigPath(path);
	var p = c[1];
	c = c[0];
	if(p in c)
		return c[p];
	if(def === undefined)
		return null;
	c[p] = def;
	localStorage.setItem(window.sipConfigName, JSON.stringify(window.config));
	return def;
}

function setConfig(path, val)
{
	var c = getConfigPath(path);
	var p = c[1];
	c = c[0];
	c[p] = val;
	if(path in window.configlisteners)
	{
		var lst = window.configlisteners[path];
		for(var i=0;i<lst.length;i++)
			lst[i](path, val);
	}
	localStorage.setItem(window.sipConfigName, JSON.stringify(window.config));
}

function addConfigListener(path, func)
{
	if(!(path in window.configlisteners))
		window.configlisteners[path] = [];
	window.configlisteners[path].push(func);
}

function VersionArray(str)
{
	var parts = String(str).split('.');
	var ver = [];
	for(var i=0;i<parts.length;i++) 
	{
		var n = parseInt(parts[i]);
		if (isNaN(n))
			n = 0;
		ver.push(n);
	}
	return ver;
}

function CompareVersion(comp)
{
	var cparts = VersionArray(comp);
	var parts = VersionArray(Siplet.VERSION_MAJOR+'.'+Siplet.VERSION_MINOR);
	var i=0;
	for(i=0;i<cparts.length && i<parts.length;i++)
	{
		if (parts[i]<cparts[i])
			return 1;
		if (parts[i]>cparts[i])
			return -1;
	}
	if (i<cparts.length)
		return 1;
	if (i<parts.length)
		return -1;
	return 0;
}

function FindAScript(scripts, value, ci)
{
	if((!scripts)||(!Array.isArray(scripts)))
		return null;
	for(var i=0;i<scripts.length;i++)
	{
		var script = scripts[i];
		if(ci)
		{
			if(script.name.toLowerCase() == value.toLowerCase())
				return script.text;
		}
		else
		if(script.name == value)
			return script.text;
	}
	return null;
};

function LoadGlobalPhonebook()
{
	window.phonebook = [];
	if(isElectron)
	{
		var savedPhonebook = getConfig('/phonebook/dial', []);
		if(Array.isArray(savedPhonebook) && (savedPhonebook.length == 0))
		{
			savedPhonebook.push({
				"name": "CoffeeMUD",
				"host": "coffeemud.net",
				"port": "23",
				"user": "",
				"accountName": "",
				"password": "",
				"disableInput": false,
				"bsCode": 8,
				"pb": true
			});
			setConfig('/phonebook/dial', savedPhonebook);
			if(Siplet.COFFEE_MUD)
				setConfig('/phonebook/auto','0');
		}
		setTimeout(function() { AutoConnect(); },100);
		return;
	}
	// NON-ELECTRON
	var xhr = new XMLHttpRequest();
	xhr.open('GET', '/MudPhonebook', true);
	xhr.onreadystatechange = function()
	{
		if((xhr.readyState === 4)&&(xhr.status === 200)) 
		{
			var pb = JSON.parse(xhr.responseText);
			if("phonebook" in pb)
			{
				var entries = pb["phonebook"];
				if(Array.isArray(entries) && (entries.length > 0))
				{
					window.phonebook = entries;
					if(getConfig('/phonebook/auto')==null)
						setConfig('/phonebook/auto','g0');
				}
			}
			AutoConnect();
		}
	};
	xhr.onerror = function() { AutoConnect(); };
	xhr.send();
}

function ParseTriggers(baseTriggers)
{
	if(Array.isArray(baseTriggers))
	{
		var triggers = [];
		for(var i=0;i<baseTriggers.length;i++)
		{
			var rawTrig=baseTriggers[i];
			var trigCopy = JSON.parse(JSON.stringify(rawTrig));
			trigCopy["prev"]=0;
			trigCopy["disabled"]=false;
			if ((trigCopy.regex) && (typeof trigCopy.pattern === 'string')) {
				trigCopy.pattern = new RegExp(trigCopy.pattern,'gi');
			}
			triggers.push(trigCopy);
		}
		return triggers;
	}
	return [];
}

function ParseAliases(baseAliases)
{
	if(Array.isArray(baseAliases))
	{
		var aliases = [];
		for(var i=0;i<baseAliases.length;i++)
		{
			var rawAliases=baseAliases[i];
			var aliasCopy = JSON.parse(JSON.stringify(rawAliases));
			if ((aliasCopy.regex) && (typeof aliasCopy.pattern === 'string')) {
				aliasCopy.pattern = new RegExp(aliasCopy.pattern,'gi');
			}
			aliases.push(aliasCopy);
		}
		return aliases;
	}
	return [];
}

function GetGlobalTriggers()
{
	var rawTriggers = getConfig('/global/triggers', window.defTriggers);
	return ParseTriggers(JSON.parse(JSON.stringify(rawTriggers)));
}

function GetGlobalAliases()
{
	var rawAliases = getConfig('/global/aliases', window.defAliases);
	return ParseAliases(JSON.parse(JSON.stringify(rawAliases)));
}

function GetGlobalScripts()
{
	return getConfig('/global/scripts', window.defScripts);
}

function GetGlobalTimers()
{
	return JSON.parse(JSON.stringify(getConfig('/global/timers', window.defTimers)));
}

function GetGlobalEntities()
{
	return JSON.parse(JSON.stringify(getConfig('/global/entities', window.defEntities)));
}

function GetGlobalElements()
{
	return getConfig('/global/elements', '');
}

function GetGlobalPlugins()
{
	return JSON.parse(JSON.stringify(getConfig('/global/plugins', window.defPlugins)));
}
