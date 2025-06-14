var SipletActions = {
	"win.submitInput": { text:"Send Command", args: 1},
	"win.displayText": { text:"Display Text", args: 1},
	"win.playSound": { text:"Play Sound", args: 1},
	"win.setVariable": { text:"Set Variable", args: 2},
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
	this.addPluginFrame = function(pluginName, pluginCode)
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
			    'setVariable', 'runScript', 'enableTrigger', 'sendGMCP', 'sendMSDP', 'disableTrigger',
			    'startTimer', 'clearTimer', 'displayAt', 'getVariable', 'process'];
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
					this.addPluginFrame(global[i].name,global[i].code);
				this.plugins.push(JSON.parse(JSON.stringify(global[i])));
			}
		}
		if(sipwin.pb && sipwin.pb.plugins && Array.isArray(sipwin.pb.plugins)) {
			for(var i=0;i<sipwin.pb.plugins.length;i++)
			{
				if(sipwin.pb.plugins[i].code)
					this.addPluginFrame(sipwin.pb.plugins[i].name,sipwin.pb.plugins[i].code);
				this.plugins.push(JSON.parse(JSON.stringify(sipwin.pb.plugins[i])));
			}
		}
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
						var req = ['name','regex','pattern','replace','action'];
						var r=0;
						for(r=0;r<req.length;r++)
							if(!(req[r] in palias))
							{
								console.log("Missing "+req[r]+" from plugin "+this.plugins[i].name+" alias");
								break;
							}
						if(r<req.length)
							continue;
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
								this.aliasList.push(newOne);
							}
							else
								console.log("Bad action in plugin "+this.plugins[i].name+" alias");
						}
						else
							console.log("Missing action in plugin "+this.plugins[i].name+" alias");
					}
				}
			this.aliasList = ParseAliases(this.aliasList);
		}
		return this.aliasList;
	};
	
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
						var req = ['name','regex','pattern','once','action'];
						var r=0;
						for(r=0;r<req.length;r++)
							if(!(req[r] in ptrigger))
							{
								console.log("Missing "+req[r]+" from plugin "+this.plugins[i].name+" trigger");
								break;
							}
						if(r<req.length)
							continue;
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
								this.triggerList.push(newOne);
							}
							else
								console.log("Bad action in plugin "+this.plugins[i].name+" trigger");
						}
						else
							console.log("Missing action in plugin "+this.plugins[i].name+" trigger");
					}
				}
			this.triggerList = ParseTriggers(this.triggerList);
		}
		return this.triggerList;
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
						var req = ['name','delay','option','trigger','action'];
						var r=0;
						for(r=0;r<req.length;r++)
							if(!(req[r] in ptimer))
							{
								console.log("Missing "+req[r]+" from plugin "+this.plugins[i].name+" timer");
								break;
							}
						if(r<req.length)
							continue;
						if('action' in ptimer)
						{
							if(!isValidAction(ptimer.action))
								console.log("Bad action in plugin "+this.plugins[i].name+" timer");
							else
							if(!isNumber(ptimer.delay))
								console.log("Bad delay in plugin "+this.plugins[i].name+" timer");
							else
							if(['repeat','multiple','once'].indexOf(ptimer.option)<0)
								console.log("Bad option in plugin "+this.plugins[i].name+" timer (repeat, multiple, once)");
							{
								var newOne = {
									name: ptimer.name,
									delay: Number(ptimer.delay),
									option: ptimer.option,
									trigger: (''+ptimer.trigger).toLowerCase()=='true',
									action: ptimer.action,
									allowed: true
								};
								this.timerList.push(newOne);
							}
						}
						else
							console.log("Bad action in plugin "+this.plugins[i].name+" timer");
					}
				}
		}
		return this.timerList;
	};
}
