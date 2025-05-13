window.configlisteners = {};
window.config = localStorage.getItem('config1');
if(window.config) 
	window.config = JSON.parse(window.config);
else
	window.config = {};
window.phonebook = [];
window.defTriggers = [
	{
		allowed: 'win.pbentry && win.pbentry.account',
		regex: true,
		once: true,
		pattern: 'Account name:(?![\\s\\S]*\\n)',
		action: 'win.submitInput(win.pbentry.accountName)'
	},
	{
		allowed: 'win.pbentry && win.pbentry.account && win.pbentry.user',
		regex: true,
		once: true,
		pattern: 'Command or Name \\(\\?\\):(?!\\[\\s\\S\\]*\\n)',
		action: 'win.submitInput(win.pbentry.user)'
	},
	{
		allowed: 'win.pbentry && !win.pbentry.account && win.pbentry.user',
		regex: true,
		once: true,
		pattern: 'name:(?!\\[\\s\\S\\]*\\n)',
		action: 'win.submitInput(win.pbentry.user)'
	},
	{
		allowed: 'win.pbentry',
		regex: true,
		once: true,
		pattern: 'Password:(?![\\s\\S]*\\n)',
		action: 'win.submitInput(win.pbentry.password)'
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

function LoadGlobalPhonebook()
{
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

function GetGlobalTriggers()
{
	setConfig('/global/triggers', window.defTriggers); //TODO:DELME
	var rawTriggers = getConfig('/global/triggers', window.defTriggers);
	if(Array.isArray(rawTriggers))
	{
		var globalTriggers = [];
		for(var i=0;i<rawTriggers.length;i++)
		{
			var rawTrig=rawTriggers[i];
			var trigCopy = JSON.parse(JSON.stringify(rawTrig));
			trigCopy["prev"]=0;
			if ((trigCopy.regex) && (typeof trigCopy.pattern === 'string')) {
				trigCopy.pattern = new RegExp(trigCopy.pattern,'g');
			}
			globalTriggers.push(trigCopy);
		}
		return globalTriggers;
	}
	return [];
}
