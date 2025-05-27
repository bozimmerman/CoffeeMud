window.configlisteners = {};
window.config = localStorage.getItem('config1.1');
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
		allowed: 'win.pb',
		regex: true,
		once: true,
		pattern: 'Password:(?![\\s\\S]*\\n)',
		action: 'win.submitInput(win.pb.password)'
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
	localStorage.setItem('config1', JSON.stringify(window.config));
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
	localStorage.setItem('config1', JSON.stringify(window.config));
}

function addConfigListener(path, func)
{
	if(!(path in window.configlisteners))
		window.configlisteners[path] = [];
	window.configlisteners[path].push(func);
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
	if(isElectron)
	{
		window.phonebook.push({
			"name": "CoffeeMUD",
			"host": "coffeemud.net",
			"port": "23"});
		return;
	}
	window.phonebook.push({
		"name": "Default MUD",
		"port": "default"
	});
	var xhr = new XMLHttpRequest();
	xhr.open('GET', '/MudPhonebook', true);
	xhr.onreadystatechange = function() {
		if((xhr.readyState === 4)&&(xhr.status === 200)) 
		{
			var pb = JSON.parse(xhr.responseText);
			if("phonebook" in pb)
			{
				var entries = pb["phonebook"];
				if(Array.isArray(entries) && (entries.length > 0))
					window.phonebook = entries;
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
	return ParseTriggers(rawTriggers);
}

function GetGlobalAliases()
{
	var rawAliases = getConfig('/global/aliases', window.defAliases);
	return ParseAliases(rawAliases);
}

function GetGlobalScripts()
{
	return getConfig('/global/scripts', window.defScripts);
}

function GetGlobalTimers()
{
	return getConfig('/global/timers', window.defTimers);
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

