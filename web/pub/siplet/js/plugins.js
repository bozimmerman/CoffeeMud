var SipletActions = {
	"win.submitInput": { text:"Send Command", args: 1},
	"win.displayText": { text:"Display Text", args: 1},
	"win.playSound": { text:"Play Sound", args: 1},
	"win.setEntity": { text:"Set Entity", args: 2},
	"win.runScript": { text:"Run Script", args: 1},
	"win.enableTrigger": { text:"Enable Trigger", args: 1},
	"win.disableTrigger": { text:"Disable Trigger", args: 1},
	"win.startTimer": { text:"Start Timer", args: 1},
	"win.clearTimer": { text:"Stop Timer", args: 1},
	"win.displayAt": { text:"Send to Frame", args: 2},
	"win.sendPlugin": { text:"Plugin Event", args: 2},
	"win.submitHidden": { text:"Send Hidden", args: 1}
};

var PLUGINS = function(sipwin)
{
	this.plugins = [];
	this.framesMap = {};
	this.mxpText = null;
	this.aliasList = null;
	this.triggerList = null;
	this.timerList = null;
	this.menuList = null;

	this.addPluginFrame = function(pluginName, pluginCode, pluginJson)
	{
		var iframe = document.createElement("iframe");
		iframe.setAttribute('id', "PLUGIN_" + pluginName.toUpperCase().replaceAll(' ','_'));
		iframe.setAttribute("sandbox", "allow-scripts allow-same-origin");
		iframe.style.display = "none";
		sipwin.topWindow.appendChild(iframe);
		//iframe.srcdoc = '<SCRIPT>' + pluginCode + '</SCRIPT>';
		iframe.srcdoc = '<SCRIPT>window.addEventListener(\'message\', function(event) {if (event.data.type === \'execute\') {' + pluginCode + '}});</SCRIPT>';
		iframe.onload = function() { // necessary for contentWindow to even exist
			iframe.contentWindow.onmessage=function(event){ 
				if(iframe.contentWindow.onevent && event.data.payload)
					iframe.contentWindow.onevent(event.data.payload);
			};
			iframe.contentWindow.send=function(event){ parent.postMessage(event,"*");};
			Object.defineProperty(iframe.contentWindow, 'localStorage', {
				value: {
					getItem: function(v) { return localStorage.getItem('plugin.'+pluginName+'.'+v);},
					removeItem: function(v) { return localStorage.removeItem('plugin.'+pluginName+'.'+v);},
					setItem: function(v,l) { return localStorage.setItem('plugin.'+pluginName+'.'+v,l);}
				},
				writeable: false
			});
			for(var key in pluginJson)
			{
				var pluginValue = pluginJson[key];
				Object.defineProperty(iframe.contentWindow, key, {
					value: pluginValue,
					writeable: false
				});
			}
			Object.defineProperty(iframe.contentWindow, 'url', {
				value: sipwin.url,
				writeable: false
			});
			var mapper = Object.create(null);
			for(var k in sipwin.mapper)
			{
				if ((k.indexOf('addMapEvent')<0)  && typeof sipwin.mapper[k] === 'function')
					mapper[k] = (function(method) { return function(...args){
						return sipwin.mapper[method](...args); 
					}})(k);
			}
			mapper['addMapEvent'] = function(...args){ return sipwin.mapper['restricted_addMapEvent'](...args);};
			Object.freeze(mapper);
			var win = Object.create(null);
			var sipwinMethods = ['submitInput', 'submitHidden', 'displayText', 'playSound',
			    'setEntity', 'runScript', 'enableTrigger', 'sendGMCP', 'sendMSDP', 'disableTrigger',
			    'startTimer', 'clearTimer', 'displayAt', 'getEntity', 'process'];
			for(var k =0;k<sipwinMethods.length;k++)
			{
				var method = sipwinMethods[k];
				win[method] = (function(method) { return function(...args){ 
					return sipwin[method](...args); 
				}})(method);
			}
			win.mapper = mapper;
			Object.freeze(win);
			iframe.contentWindow.win = win;
			iframe.setAttribute("sandbox", "allow-scripts");
			iframe.contentWindow.postMessage({ type: 'execute' }, '*');
		};
		this.framesMap[pluginName] =  iframe;
		this.aliasList = null;
		this.triggerList = null;
		this.timerList = null;
		this.menuList = null;
	};
	
	this.reset = function()
	{
		for(var k in this.framesMap)
			this.framesMap[k].remove();
		this.plugins = [];
		this.framesMap = {};
		if(!sipwin.topContainer)
			return;
		var global = GetGlobalPlugins();
		if(global && Array.isArray(global)) {
			for(var i=0;i<global.length;i++)
			{
				if(global[i].code)
					this.addPluginFrame(global[i].name,global[i].code, global[i]);
				this.plugins.push(JSON.parse(JSON.stringify(global[i])));
			}
		}
		if(sipwin.pb && sipwin.pb.plugins && Array.isArray(sipwin.pb.plugins)) {
			for(var i=0;i<sipwin.pb.plugins.length;i++)
			{
				if(sipwin.pb.plugins[i].code)
					this.addPluginFrame(sipwin.pb.plugins[i].name,sipwin.pb.plugins[i].code, sipwin.pb.plugins[i]);
				this.plugins.push(JSON.parse(JSON.stringify(sipwin.pb.plugins[i])));
			}
		}
		this.aliasList = null;
		this.triggerList = null;
		this.timerList = null;
		this.menuList = null;
	};
	
	this.postEvent = function(event) {
		for(var k in this.framesMap)
			if(this.framesMap[k].contentWindow)
				this.framesMap[k].contentWindow.postMessage({ type: 'message', payload: event}, '*');
	};
	
	this.postEventToPlugin = function(plugin, event) {
		if(plugin in this.framesMap)
			if(this.framesMap[plugin].contentWindow)
				this.framesMap[plugin].contentWindow.postMessage({ type: 'message', payload: event}, '*');
	}
	
	this.mxp = function()
	{
		if(this.mxpText == null)
		{
			var s = '';
			for(var i=0;i<this.plugins.length;i++)
				if('mxp' in this.plugins[i])
					s += '\n' + this.plugins[i].mxp;
			s=s.replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, '');
			this.mxpText = s;
		}
		return this.mxpText;
	};
	
	this.validatedAlias = function(source, palias)
	{
		if((!palias)||(!palias.name))
			return false;
		var req = ['name','regex','pattern','replace','action'];
		var r=0;
		for(r=0;r<req.length;r++)
			if(!(req[r] in palias))
			{
				console.warn("Missing "+req[r]+" from plugin "+source+" alias");
				break;
			}
		if(r<req.length)
			return false;
		if('action' in palias)
		{
			if(isValidAction(palias.action))
			{
				var newOne = {
					name: palias.name,
					regex: palias.regex,
					replace: palias.replace,
					pattern: palias.pattern,
					action: palias.action
				};
				return newOne;
			}
			else
				console.warn("Bad action in plugin "+source+" alias");
		}
		else
			console.warn("Missing action in plugin "+source+" alias");
		return false;
	};
	
	this.aliases = function()
	{
		if(this.aliasList == null)
		{
			this.aliasList = [];
			for(var i=0;i<this.plugins.length;i++)
				if(('aliases' in this.plugins[i]) && Array.isArray(this.plugins[i].aliases))
				{
					var paliases = this.plugins[i].aliases;
					for(var p=0;p<paliases.length;p++)
					{
						var palias=paliases[p];
						palias = this.validatedAlias(this.plugins[i].name, palias);
						if(palias)
							this.aliasList.push(palias);
					}
				}
			this.aliasList = ParseAliases(this.aliasList);
		}
		return this.aliasList;
	};
	
	this.validatedTrigger = function(source, ptrigger)
	{
		if((!ptrigger)||(!ptrigger.name))
			return false;
		var req = ['name','regex','pattern','once','action'];
		var r=0;
		for(r=0;r<req.length;r++)
			if(!(req[r] in ptrigger))
			{
				console.warn("Missing "+req[r]+" from plugin "+source+" trigger");
				break;
			}
		if(r<req.length)
			return false;
		if('action' in ptrigger)
		{
			if(isValidAction(ptrigger.action))
			{
				var newOne = {
					name: ptrigger.name,
					regex: ptrigger.regex,
					once: ptrigger.once,
					pattern: ptrigger.pattern,
					action: ptrigger.action,
					allowed: true
				};
				return newOne;
			}
			else
				console.warn("Bad action in plugin "+source+" trigger");
		}
		else
			console.warn("Missing action in plugin "+source+" trigger");
		return false;
	}
	
	this.triggers = function()
	{
		if(this.triggerList == null)
		{
			this.triggerList = [];
			for(var i=0;i<this.plugins.length;i++)
				if(('triggers' in this.plugins[i]) && Array.isArray(this.plugins[i].triggers))
				{
					var ptriggers = this.plugins[i].triggers;
					for(var p=0;p<ptriggers.length;p++)
					{
						var ptrigger=ptriggers[p];
						ptrigger = this.validatedTrigger(this.plugins[i].name, ptrigger);
						if(ptrigger)
							this.triggerList.push(ptrigger);
					}
				}
			this.triggerList = ParseTriggers(this.triggerList);
		}
		return this.triggerList;
	};
	
	this.validatedTimer = function(source, ptimer)
	{
		if((!ptimer)||(!ptimer.name))
			return false;
		var req = ['name','delay','option','trigger','action'];
		var r=0;
		for(r=0;r<req.length;r++)
			if(!(req[r] in ptimer))
			{
				console.warn("Missing "+req[r]+" from "+source+" timer");
				break;
			}
		if(r<req.length)
			return false;
		if('action' in ptimer)
		{
			if(!isValidAction(ptimer.action))
				console.warn("Bad action in "+source+" timer");
			else
			if(!isNumber(ptimer.delay))
				console.warn("Bad delay in "+source+" timer");
			else
			if(['repeat','multiple','once'].indexOf(ptimer.option)<0)
				console.warn("Bad option in "+source+" timer (repeat, multiple, once)");
			{
				var newOne = {
					name: ptimer.name,
					delay: Number(ptimer.delay),
					option: ptimer.option,
					trigger: (''+ptimer.trigger).toLowerCase()=='true',
					action: ptimer.action,
					allowed: true
				};
				return newOne;
			}
		}
		else
			console.warn("Bad action in "+source+" timer");
		return false;
	};
	
	this.timers = function()
	{
		if(this.timerList == null)
		{
			this.timerList = [];
			for(var i=0;i<this.plugins.length;i++)
				if(('timers' in this.plugins[i]) && Array.isArray(this.plugins[i].timers))
				{
					var ptimers = this.plugins[i].timers;
					for(var p=0;p<ptimers.length;p++)
					{
						var ptimer=ptimers[p];
						ptimer = this.validatedTimer(this.plugins[i].name, ptimer);
						if(ptimer)
							this.timerList.push(ptimer);
					}
				}
		}
		return this.timerList;
	};

	this.validatedMenu = function(source, pmenu)
	{
		if(!pmenu)
			return false;
		if(!Array.isArray(pmenu) || !pmenu.length)
		{
			console.warn("Menu option "+source+" was not an array with entries.");
			return false;
		}
		for(var i=0;i<pmenu.length;i++)
		{
			var opt = pmenu[i];
			if(!opt.n || (typeof opt.n !== 'string'))
			{
				console.warn("Menu option "+i+'#'+source+" missing 'n' (name) entry.");
				return false;
			}
			if(!opt.a || (typeof opt.a !== 'string'))
			{
				console.warn("Menu option "+i+'#'+source+" missing 'a' (action) string entry.");
				return false;
			}
			if(opt.e)
			{
				if(!(''+opt.e).trim().match(Siplet.R))
				{
					console.warn("Menu option "+i+'#'+source+" invalid 'e' entry.");
					return false;
				}
			}
			if(opt.v)
			{
				if(!(''+opt.v).trim().match(Siplet.R))
				{
					console.warn("Menu option "+i+'#'+source+" invalid 'v' entry.");
					return false;
				}
			}
			if(!isValidAction(opt.a))
			{
				console.warn("Menu option "+i+'#'+source+" invalid 'a' (action).");
				return false;
			}
		}
		return JSON.parse(JSON.stringify(pmenu));
	};
	
	this.menus = function(tempMenus)
	{
		if(this.menuList == null)
		{
			this.menuList = {};
			for(var i=0;i<this.plugins.length;i++)
				if('menus' in this.plugins[i])
				{
					var pmenus = this.plugins[i].menus;
					for(var key in pmenus)
					{
						var pmenu=pmenus[key];
						pmenu = this.validatedMenu(key+'@'+this.plugins[i].name, pmenu);
						if(pmenu)
						{
							for(var ii=0;ii<pmenu.length;ii++)
								pmenu[ii].a = 'javascript:var win=window.currWin;' + pmenu[ii].a;
							if(key in this.menuList)
								this.menuList[key] = this.menuList[key].concat(pmenu);
							else
								this.menuList[key] = pmenu;
						}
					}
				}
		}
		var menuList = this.menuList;
		if((tempMenus != null)&&(Object.keys(tempMenus).length))
		{
			menuList = JSON.parse(JSON.stringify(menuList));
			tempMenus = JSON.parse(JSON.stringify(tempMenus));
			for(var key in tempMenus)
			{
				var tmenu=tempMenus[key]
				for(var i=0;i<tmenu.length;i++)
					tmenu[i].a = 'javascript:var win=window.currWin;' + tmenu[i].a;
				if(key in menuList)
					menuList[key] = menuList[key].contat(tmenu);
				else
					menuList[key] = tmenu;
			}
		}
		return menuList;
	};
}
