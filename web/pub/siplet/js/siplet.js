window.currWin = null;
window.siplets = [];
window.windowArea = null;
window.nextId = 0;

var Siplet =
{
	VERSION_MAJOR: 3.0,
	VERSION_MINOR: 1,
	NAME: window.isElectron?'Sip':'Siplet'
};

function SipletWindow(windowName)
{
	this.decoder = new TextDecoder("utf-8");
	this.siplet = Siplet;
	this.width = getConfig('window/width',80);
	this.height = getConfig('window/height',25);;
	this.maxLines = getConfig('window/lines',5000);;
	this.MSPsupport = false;
	this.MSDPsupport = false;
	this.GMCPsupport = false;
	this.MXPsupport = false;
	this.MCCPsupport = false;
	this.wsopened = false;
	this.windowName = windowName;
	this.bin = new BPPARSE(false);
	this.ansi = new ANSISTACK();
	this.telnet = new TELNET(this);
	this.msp = new MSP(this);
	this.mxp = new MXP(this);
	this.plugins = new PLUGINS(this);
	this.text = new TEXT([this.mxp,this.msp]);
	this.topWindow = document.getElementById(windowName);
	this.wsocket = null;
	this.gauges=[];
	this.gaugeWindow = null;
	this.textBuffer = '';
	this.textBufferPruneIndex = 0;
	this.globalTriggers = GetGlobalTriggers();
	this.triggers = null;
	this.globalAliases = GetGlobalAliases();
	this.aliases = null;
	this.globalTimers = JSON.parse(JSON.stringify(GetGlobalTimers()));
	this.timers = null;
	this.activeTimers = [];
	this.lastStyle = '';
	this.listeners = {};
	this.overflow = getConfig('window/overflow','');
	var me = this;
	
	this.fixOverflow = function()
	{
		if(!this.topContainer)
			return;
		var window = this.topContainer.firstChild; 
	    if(this.overflow == '')
	    	window.style.overflowX = 'auto';
	    else
	    {
	    	window.style.overflowX = 'hidden';
			window.style.overflowWrap = 'break-word';
			window.style.wordWrap = 'break-word';
			window.style.whiteSpace = 'pre-wrap';
		}
	};
	
	if(this.topWindow)
	{
		this.topWindow.onclick = function() { delayhidemenu(); boxFocus(); };
	    this.topWindow.style.fontFamily = getConfig('window/fontface', 'monospace');
	    this.topWindow.style.fontSize = getConfig('window/fontsize', '16px');
		this.topWindow.style.whiteSpace = 'pre'; // Preserve spaces as-is
	    this.topContainer = this.topWindow.firstChild;
		this.window = this.topContainer.firstChild;
		[this.topWindow, this.topContainer, this.window].forEach(function(o)
		{
			o.style.position='absolute';
			o.style.top = '0%';
			o.style.height = '100%';
			o.style.width = '100%';
			o.style.left = '0%';
		});
	    this.window.style.overflowY = 'auto';
    	this.window.style.overflowX = 'auto';
    	this.fixOverflow();
		this.plugins.reset();
	}
	
	this.connect = function(url)
	{
		this.wsocket = new WebSocket(url);
		this.wsocket.binaryType = "arraybuffer";
		this.wsocket.onmessage = this.onReceive;
		this.wsocket.onopen = function(event)  
		{
			me.plugins.postEvent({type: 'connect',data:url});
			me.wsopened=true; 
			me.tab.style.backgroundColor="green";
			me.tab.style.color="white";
			me.tab.innerHTML=(me.tabTitle)?(me.tabTitle):'';
			me.window.style.backgroundColor="black";
			me.window.style.color="white";
			me.startTimers();
		};
		this.wsocket.onclose = function(event)  
		{ 
			me.flushWindow();
			if(me.tab && me.tab.innerHTML.startsWith("Connecting"))
				me.tab.innerHTML = 'Failed connection to ' + url;
			me.plugins.postEvent({type: 'closesock',data:url});
			me.wsopened=false; 
			me.tab.style.backgroundColor="#FF555B";
			me.tab.style.color="white";
			me.clearTimers();
		};
	};

	this.closeSocket = function()
	{
	    if (this.wsocket && this.wsocket.readyState === WebSocket.OPEN) {
	    	this.wsocket.close();
	    }
	};
	
	this.close = function()
	{
		this.plugins.postEvent({type: 'close'});
		this.closeSocket();
		this.reset();
	};
	
	this.process = function(s)
	{
		if(!s) return s;
		var oldResume = this.text.resume;
		this.text.resume = null;
		s=this.text.process(s) + this.text.flush();
		if((!this.mxp.active())&&(this.fixVariables))
			s = this.fixVariables(s);
		this.text.resume = oldResume;
		return s;
	};

	this.mxpFix = function()
	{
		this.mxp.defBitmap = 256; // no override
		var oldActive = this.mxp.active;
		this.mxp.active = function() { return true; };
		this.process(GetGlobalElements());
		if(this.pb && this.pb.elements)
			this.process(this.pb.elements);
		if(this.plugins.mxp())
			this.process(this.plugins.mxp());
		this.mxp.active = oldActive;
		this.mxp.defBitmap = 0; // normal operation
	};
	this.mxpFix();

	this.reset = function()
	{
		while (this.topWindow.children.length > 1) {
			this.topWindow.removeChild(this.topWindow.lastChild);
		}
		this.topContainer = this.topWindow.firstChild;
		this.topContainer.style.cssText = 'position:absolute;top:0%;left:0%;width:100%;height:100%;'
		while (this.topContainer.children.length > 1) {
			this.topContainer.removeChild(this.topContainer.lastChild);
		}
		this.window = this.topContainer.firstChild;
		this.window.style.cssText = 'position:absolute;top:0%;left:0%;width:100%;height:100%;'
		this.window.style.overflowY = 'auto';
		this.window.style.overflowX = 'hidden';
  		this.MSPsupport = false;
		this.MSDPsupport = false;
		this.GMCPsupport = false;
		this.MXPsupport = false;
		this.MCCPsupport = false;
		this.wsopened = false;
		this.bin.reset();
		this.ansi.reset();
		this.telnet.reset();
		this.msp.reset();
		this.mxp.reset();
		this.text.reset();
		this.plugins.reset();
		this.gauges=[];
		this.gaugeWindow = null;
		this.textBuffer = '';
		this.textBufferPruneIndex = 0;
		this.globalTriggers = GetGlobalTriggers();
		this.triggers = null;
		this.globalAliases = GetGlobalAliases();
		this.aliases = null;
		this.listeners = {};
		this.resetTimers();
		this.mxpFix();
		this.fixOverflow();
	};
	
	this.htmlBuffer = '';
	this.flushWindow = function() {
		if(this.htmlBuffer.length > 0)
		{
			var rescroll = this.isAtBottom(-10);
			var span = document.createElement('span');
			var reprocess = '';
			if(this.mxp.openElements.length)
			{
				for(var i=0;i<this.mxp.openElements.length;i++)
				{
					var elem = this.mxp.openElements[i];
					if((elem.bitmap & MXPBIT.HTML)>0)
						reprocess += elem.rawText;
				}
			}
			span.innerHTML = reprocess + this.htmlBuffer;
			this.window.appendChild(span);
			this.process(reprocess);
			this.htmlBuffer='';
			if(window.currWin != me)
			{
				this.tab.style.backgroundColor = "lightgreen";
				this.tab.style.color = "black";
			}
			if(rescroll)
				this.scrollToBottom(this.window,0);
		}
	};

	this.onReceive = function(e)
	{
		var entries = me.bin.parse(e.data);
		while (me.window.childNodes.length > me.maxLines)
			me.window.removeChild(me.window.firstChild);
		me.htmlBuffer = '';
		while(entries.length > 0)
		{
			if((entries.length == 1)
			&&(!entries[0].done))
				break;
			var blk = entries.shift();
			if(blk.data.length==0)
				continue;
			if(blk.type == BPTYPE.TELNET)
			{
				var buffer = me.telnet.process(blk.data);
				if ((buffer.byteLength > 0) && (me.wsopened))
					me.wsocket.send(buffer);
			}
			else
			{
				var newText = '';
				if(blk.type == BPTYPE.ANSI)
				{
					newText += me.ansi.process(blk.data);
					if(!me.mxp.active())
					{
						me.htmlBuffer += newText;
						continue;
					}
					var s = me.mxp.process(blk.data);
					if (s != null)
						newText += s;
					blk.data = newText; // convert to text-like
				}
				else
				{
					try
					{
						newText += TextDecoder.decode(blk.data);
					}
					catch(e)
					{
						for (var i=0; i < blk.data.length; i++) {
							newText += String.fromCharCode( blk.data[i]);
						}
					}
				}
				newText = me.text.process(newText);
				if(newText.length>0)
				{
					me.htmlBuffer += newText;
					me.textBuffer += stripHtmlTags(newText);
					if(newText.indexOf('<BR>')>=0)
					{
						me.triggerCheck();
						while(me.textBuffer.length > 1024)
						{
							var x = me.textBuffer.indexOf('\n');
							if(x<0)
							{
								me.textBufferPruneIndex+=me.textBuffer.length;
								me.textBuffer = '';
							}
							else
							{
								me.textBuffer = me.textBuffer.substr(0,x+1);
								me.textBufferPruneIndex+=x;
							}
						}
					}
				}
			}
		}
		me.flushWindow();
		me.triggerCheck();
	};

	this.evalTriggerGroup = function(triggers)
	{
		var win = this;
		for(var i=0;i<triggers.length;i++)
		{
			var trig = triggers[i];
			if(eval(trig.allowed) && trig.pattern
			&& (!trig.disabled))
			{
				var prev = trig.prev - this.textBufferPruneIndex;
				if(prev < 0)
					prev = 0;
				if(trig.regex && trig.pattern && trig.pattern.exec)
				{
					trig.pattern.lastIndex = prev;
					var match = trig.pattern.exec(this.textBuffer);
					while(match !== null)
					{
						for(var i=0;i<match.length;i++)
							this.setVariable('match'+i,match[i]);
						trig.prev = this.textBufferPruneIndex + match.index + 1;
						var action = this.fixVariables(trig.action.replaceAll('\\n','\n'));
						try { eval(action); } catch(e) {console.log(e);};
						for(var i=0;i<match.length;i++)
							this.setVariable('match'+i,null);
						match = trig.pattern.exec(this.textBuffer);
						if(trig.once)
							trig.disabled=true;
					}
				}
				else
				{
					var x = this.textBuffer.indexOf(trig.pattern,prev);
					while(x>=0)
					{
						prev += x + trig.pattern.length + 1;
						trig.prev = this.textBufferPruneIndex + x + trig.pattern.length + 1;
						var action = this.fixVariables(trig.action.replaceAll('\\n','\n'));
						try { eval(action); } catch(e) {console.log(e);};
						if(trig.once)
							trig.disabled=true;
						x = this.textBuffer.indexOf(trig.pattern,prev);
					}
				}
			}
		}
	};
	
	this.localTriggers = function()
	{
		if(this.triggers == null)
		{
			if((!this.pb)
			||(!this.pb.triggers)
			||(!Array.isArray(this.pb.triggers)))
			{
				this.triggers=[];
				return [];
			}
			this.triggers = ParseTriggers(this.pb.triggers);
		}
		return this.triggers;
	};

	this.triggerCheck = function()
	{
		this.evalTriggerGroup(this.globalTriggers);
		this.evalTriggerGroup(this.localTriggers());
		if(this.plugins.triggers())
			this.evalTriggerGroup(this.plugins.triggers());
	};

	this.addEventListener = function(type, func)
	{
		if(type && func)
		{
			if(!(type in this.listeners))
				this.listeners[type] = [];
			this.listeners[type].push(func);
		}
	};

	this.removeEventListener = function(type, func)
	{
		if(type && func)
		{
			if(!(type in this.listeners))
				return;
			var x = this.listeners[type].indexOf(func);
			if(x<0)
				return;
			this.listeners[type].splice(x,1);
			if(this.listeners[type].length==0)
				delete this.listeners[type];
		}
	};

	this.dispatchEvent = function(event)
	{
		if(event && event.type 
		&& (event.type in this.listeners))
		{
			event.cancelBubble=false;
			event.stopPropagation = function () { event.cancelBubble=true;};
			event.preventDefault = function () { event.cancelBubble=true;};
			var calls = this.listeners[event.type];
			if(Array.isArray(calls))
			{
				for(var i=0;i<calls.length;i++)
				{
					calls[i](event);
					if(event.cancelBubble)
						return;
				}
			}
			delete event.stopPropagation;
			delete event.preventDefault;
		};
		this.plugins.postEvent(event);
	};

	this.evalAliasGroup = function(aliases, txt)
	{
		var win = this;
		for(var i=0;i<aliases.length;i++)
		{
			var alias = aliases[i];
			if(!alias.disabled)
			{
				if(alias.regex && alias.pattern && alias.pattern.test)
				{
					var match = alias.pattern.exec(txt);
					if(match !== null)
					{
						for(var i=0;i<match.length;i++)
							this.setVariable('match'+i,match[i]);
						txt = txt.replace(alias.pattern,this.fixVariables(alias.replace));
						var action = this.fixVariables(alias.action.replaceAll('\\n','\n'));
						try { eval(action); } catch(e) {console.log(e);};
						for(var i=0;i<match.length;i++)
							this.setVariable('match'+i,null);
						return txt
					}
				}
				else
				if(txt.startsWith(alias.pattern))
				{
					var action = this.fixVariables(alias.action.replaceAll('\\n','\n'));
					try { eval(action); } catch(e) {console.log(e);};
					return alias.replace + txt.substr(alias.pattern.length);
				}
			}
		}
		return txt;
	};

	this.aliasProcess = function(x)
	{
		var oldx = x;
		x = this.evalAliasGroup(this.globalAliases, x);
		if(x == oldx)
			x = this.evalAliasGroup(this.localAliases(), x);
		if(this.plugins.aliases())
			x = this.evalAliasGroup(this.plugins.aliases(), x);
		return x;
	};

	this.localAliases = function()
	{
		if(this.aliases == null)
		{
			if((!this.pb)
			||(!this.pb.aliases)
			||(!Array.isArray(this.pb.aliases)))
			{
				this.aliases=[];
				return [];
			}
			this.aliases = ParseAliases(this.pb.aliases);
		}
		return this.aliases;
	};
	
	this.localScripts = function()
	{
		if(this.scripts == null)
		{
			if((!this.pb)
			||(!this.pb.scripts)
			||(!Array.isArray(this.pb.scripts)))
			{
				this.scripts=[];
				return [];
			}
			this.scripts = this.pb.scripts;
		}
		return this.scripts;
	};
	
	this.localTimers = function()
	{
		if(this.timers == null)
		{
			if((!this.pb)
			||(!this.pb.timers)
			||(!Array.isArray(this.pb.timers)))
			{
				this.timers=[];
				return [];
			}
			this.timers = this.pb.timers;
		}
		return this.timers;
	};

	this.findTimerByName = function(name, ci)
	{
		var i;
		for(i=0;i<this.localTimers().length;i++)
			if((this.localTimers()[i].name == name)
			||(ci && (this.localTimers()[i].name.toLowerCase() == name.toLowerCase())))
				return this.localTimers()[i];
		for(i=0;i<this.globalTimers.length;i++)
			if((this.globalTimers[i].name == name)
			||(ci && (this.globalTimers[i].name.toLowerCase() == name.toLowerCase())))
				return this.globalTimers[i];
		if(this.plugins.timers() && Array.isArray(this.plugins.timers()))
		{
			for(i=0;i<this.plugins.timers().length;i++)
				if((this.plugins.timers()[i].name == name)
				||(ci && (this.plugins.timers()[i].name.toLowerCase() == name.toLowerCase())))
					return this.plugins.timers()[i];
		}
		return null;
	};

	this.startTimer = function(timer)
	{
		if(timer == null)
			return;
		if(typeof timer === "string")
		{
			this.startTimer(this.findTimerByName(timer));
			return;
		}
		if(!timer.multiple)
		{
			for(var i=0;i < this.activeTimers.length; i++)
			{
				var atimer = this.activeTimers[i];
				if(atimer.timerId == timer.timerId)
					return;
				if(timer.name == atimer.name)
				{
					timer.timerId = atimer.timerId;
					return;
				}
			}
		}
		var timerCopy = JSON.parse(JSON.stringify(timer));
		var me = this;
		var execute = function() {
			var action = me.fixVariables(timer.action.replaceAll('\\n','\n'));
			var win = me;
			try { eval(action); } catch(e) {console.log(e);};
			if(timer.repeat)
			{
				timer.timerId = setTimeout(execute, timer.delay);
				timerCopy.timerId = timer.timerId;
			}
			else
			{
				var x = me.activeTimers.indexOf(timerCopy);
				if(x >= 0 )
				{
					me.activeTimers.splice(x,1);
					if(timer.timerid)
						delete timer.timerId;
				}
			}
		}
		timer.timerId = setTimeout(execute, timer.delay);
		timerCopy.timerId = timer.timerId;
		this.activeTimers.push(timerCopy);
	};

	this.startTimers = function(timers)
	{
		if(!this.wsopened)
			return;
		if((timers == null)||(timers==undefined)||(!Array.isArray(timers)))
		{
			this.startTimers(this.globalTimers);
			this.startTimers(this.localTimers());
			if(this.plugins.timers())
				this.startTimers(this.plugins.timers());
		}
		else
		{
			for(var i=0;i < timers.length; i++)
			{
				var timer = timers[i];
				if((timer.timerId == undefined)
				&&(timer.trigger))
					this.startTimer(timer);
			}
		}
	};
	
	this.clearTimer = function(name)
	{
		var timer = this.findTimerByName(name);
		if(timer !== null)
		{
			for(var i=this.activeTimers.length-1;i>=0;i--)
			{
				if((this.activeTimers[i].timerId == timer.timerId)
				||(this.activeTimers[i].name == timer.name))
				{
					clearTimeout(this.activeTimers[i].timerId);
					if(timer.timerId !== undefined)
						delete timer.timerId;
					this.activeTimers.splice(i,1);
				}
			}
		}
	};
	
	this.clearTimers = function(timers)
	{
		if((timers == null)||(timers==undefined)||(!Array.isArray(timers)))
		{
			if(this.activeTimers && Array.isArray(this.activeTimers))
				this.clearTimers(this.activeTimers);
			this.activeTimers = [];
			if(this.globalTimers && Array.isArray(this.globalTimers))
				this.clearTimers(this.globalTimers);
			if(this.timers && Array.isArray(this.timers))
				this.clearTimers(this.timers);
		}
		else
		{
			for(var i=0;i < timers.length; i++)
			{
				var timer = timers[i];
				if(timer.timerId !== undefined)
					clearTimeout(timer.timerId);
				delete timer.timerId;
			}
		}
	};
	
	this.resetTimers = function()
	{
		this.clearTimers();
		this.startTimers();
	};
	
	this.createGauge = function(entity,caption,color,value,max)
	{
		var gaugedata = {};
		var add = true;
		for(var i=0;i<this.gauges.length;i++)
			if((this.gauges[i].caption == caption)
			&&(this.gauges[i].entity == entity))
			{
				add=false;
				gaugedata = this.gauges[i];
			}
		gaugedata.entity=entity;
		gaugedata.caption=caption;
		gaugedata.color=color;
		gaugedata.value=value;
		gaugedata.max=max;
		if(add)
			this.gauges.push(gaugedata);
		var gaugeHeight = 20;
		if(this.gaugeWindow == null)
		{
			this.gaugeWindow = document.createElement('div');
			this.gaugeWindow.innerHTML = '';
			this.gaugeWindow.style.position = 'absolute';
			this.gaugeWindow.style.top = '0%';
			this.gaugeWindow.style.width = '100%';
			this.gaugeWindow.style.height = gaugeHeight+'px';
			this.gaugeWindow.style.background = 'black';
			this.topWindow.appendChild(this.gaugeWindow);
			var parent = this.window.parentNode;
			parent.style.top = 'calc(' +parent.style.top + ' + '+gaugeHeight + 'px)';
			parent.style.height = 'calc(' +parent.style.height + ' - '+gaugeHeight + 'px)';
		}
		this.modifyGauge(entity,value,max);
	};
	
	this.removeGauge = function(entity)
	{
		var index = this.gauges.indexOf(entity);
		if(index >=0)
		{
			this.gauges.splice(index);
			this.modifyGauge(entity,-1,-1);
			if((this.gauges.length == 0) && (this.topWindow != null))
			{
				
				var mainWindow = this.gaugeWindow.parentNode.firstChild;
				mainWindow.style.top = this.gaugeWindow.top;
				this.gaugeWindow.outerHTML = '';
				this.gaugeWindow = null;
			}
		}
	};
	
	this.modifyGauge = function(entity,value,max)
	{
		var div=this.gaugeWindow;
		if(div == null)
			return;
		if(this.gauges.length==0)
			div.innerHTML='';
		else
		{
			var s='<TABLE WIDTH=100% CELLPADDING=0 CELLSPACING=0 BORDER=1><TR>';
			var i=0;
			var cellwidth=100/this.gauges.length;
			for(i=0;i<this.gauges.length;i++)
			{
				var gaugedata=this.gauges[i];
				if(gaugedata.entity==entity)
				{
					gaugedata.value=value;
					gaugedata.max=max;
				}
			}
			for(i=0;i<this.gauges.length;i++)
			{
				var gaugedata=this.gauges[i];
				s+='<TD WIDTH='+cellwidth+'%>';
				s+='<FONT STYLE="color: '+gaugedata.color+'" SIZE=-2>'+gaugedata.caption+'</FONT><BR>';
				var fullwidth=100-gaugedata.value;
				var lesswidth=gaugedata.value;
				s+='<TABLE WIDTH=100% CELLPADDING=0 CELLSPACING=0 BORDER=0 HEIGHT=5><TR HEIGHT=5>';
				s+='<TD STYLE="background-color: '+gaugedata.color+'" WIDTH='+lesswidth+'%></TD>';
				s+='<TD STYLE="background-color: black" WIDTH='+fullwidth+'%></TD>';
				s+='</TR></TABLE>';
				s+='</TD>';
			}
			s+='</TR></TABLE>'
			div.innerHTML=s;
		}
	};
	
	this.submitInput = function(value)
	{
		if((value === undefined) || (value == null))
			return;
		this.displayText(value);
		value = this.fixVariables(value);
		this.wsocket.send(value+'\n');
	};
	
	this.isAtBottom = function(diff)
	{
		return this.window.scrollTop + this.window.clientHeight >= this.window.scrollHeight + diff;
	};
	
	this.scrollToBottom = function(rewin, tries)
	{
		if(tries > 10)
			return;
		var me = this;
		setTimeout(function(){
			rewin.scrollTop = rewin.scrollHeight - rewin.clientHeight;
			me.scrollToBottom(rewin,++tries);
		},50);
	};
	
	this.displayText = function(value)
	{
		value = me.process(value);
		if(value)
		{
			var rescroll = this.isAtBottom(-10);
			var span = document.createElement('span');
			span.innerHTML = value.replaceAll('\n','<BR>') + '<BR>';
			this.window.appendChild(span);
			if(rescroll)
				this.scrollToBottom(this.window,0);
		}
	};

	this.displayAt = function(value, frame)
	{
		var framechoices = mxp.getFrameMap();
		if(!(frame in framechoices))
			return;
		var frame = framechoices[frame];
		if(frame.firstChild)
			frame = frame.firstChild;
		var oldWindow = this.window;
		this.flushWindow();
		this.window=frame;
		this.displayText(value);
		this.flushWindow();
		this.window=oldWindow;
	};

	this.playSound = function(file)
	{
		if(!file)
			return;
		var x = file.lastIndexOf('/');
		var url;
		var key;
		if(x>0)
		{
			key=file.substr(x+1);
			url=file.substr(0,x+1);
		}
		else
		{
			url = window.location.protocol + '//' + window.location.host + '/sounds/';
			key = file;
		}
		this.msp.PlaySound(key,url,0,50,50);
	};

	this.setVariable = function(key, value)
	{
		if((key !== undefined)&&(value !== undefined)&&(key != null))
		{
			key = this.fixVariables(key);
			if(value == null)
				delete this.mxp.entities[key];
			else 
				this.mxp.entities[key] = this.fixVariables(value);
			
		}
	};
	
	this.getVariable = function(key)
	{
		if(key in this.mxp.entities)
			return this.fixVariables(this.mxp.entities[key]);
		return '';
	};
	
	this.fetchVariable = function(plugin, key)
	{
		var value = '';
		if(key in this.mxp.entities)
			value = this.fixVariables(this.mxp.entities[key]);
		this.sendPlugin(plugin, {type: 'variable', key: key, data: value});
	};
	
	this.fixVariables = function(s)
	{
		if(!s || (s===undefined))
			return s;
		s=s.replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, '');
		var x = s.indexOf('&');
		while(x>=0)
		{
			var y = s.indexOf(';',x+1);
			if(y>x)
			{
				var v = s.substr(x+1,y-(x+1));
				if(v in this.mxp.entities)
					s = s.substr(0,x) + this.mxp.entities[v] + s.substr(y+1);
			}
			if(x+1>=s.length)
				break;
			x=s.indexOf('&',x+1);
		}
		return s;
	};

	this.sendPlugin = function(plugin, event)
	{
		if(plugin && event)
		{
			var json = {};
			try {
				json = JSON.parse(event);
			} catch(e) {
				json = {'type':'event', 'data': event};
			}
			if(!('type' in json))
				json['type'] = 'event';
			var doc = this.fixVariables(JSON.stringify(json));
			this.plugins.postEventToPlugin(plugin, JSON.parse(doc));	
		}
	}

	this.findLocalScript = function(value)
	{
		var globalScripts = GetGlobalScripts();
		var run = FindAScript(this.localScripts(),value,false);
		if(run == null) 
			run = FindAScript(globalScripts,value,false);
		if(run == null) 
			run = FindAScript(this.localScripts(),value,true);
		if(run == null) 
			run = FindAScript(globalScripts,value,true);
		return run;
	};
	
	this.runScript = function(value)
	{
		var run = this.findLocalScript(value);
		if(run != null)
		{
			var win = this;
			eval(this.fixVariables(run));
		}
		else
			console.log("Unable to find script '"+value+"'");
	};
	
	this.sendGMCP = function(command, json)
	{
		if(!this.wsopened)
			return;
		if(typeof json === "string")
		{
			try {
				json = JSON.parse(json);
			} catch(e) {
				console.log(e);
				return;
			}
		}
		var response = [TELOPT.IAC, TELOPT.SB, TELOPT.GMCP];
		for(var i=0;i<command.length;i++)
			response.push(command.charCodeAt(i));
		response.push(' '.charCodeAt(0));
		var jsonStr = JSON.stringify(json);
		for(var i=0;i<jsonStr.length;i++)
			response.push(jsonStr.charCodeAt(i));
		response.push(TELOPT.IAC);
		response.push(TELOPT.SE);
		var buffer = new Uint8Array(response).buffer;
		if ((buffer.byteLength > 0) && (me.wsopened))
			me.wsocket.send(buffer);
	};
	
	this.sendMSDP = function(json)
	{
		if(!this.wsopened)
			return;
		if(typeof json === "string")
		{
			try {
				json = JSON.parse(json);
			} catch(e) {
				console.log(e);
				return;
			}
		}
		var isMSDPJSONObj = function(j)
		{
			if((j === null)
			||(typeof j !== 'object')
			||(j.constructor !== Object)
			||(Array.isArray(j)))
				return false;
			return true;
		};
		if(!isMSDPJSONObj(json))
		{
			console.log('MSDP json is not a JSON object.');
			return;
		}
		var addMSDPStr = function(response, val) {
			for(var i=0;i<val.length;i++)
				response.push(val.charCodeAt(i));
		}
		var addMSDP = function(response, val) {
			if(Array.isArray(val)) {
				response.push(MSDPOP.MSDP_ARRAY_OPEN);
				for(var i=0;i<val.length;i++)
					addMSDP(response, val[i]);
				response.push(MSDPOP.MSDP_ARRAY_CLOSE);
				return;
			}
			if(isMSDPJSONObj(val)) {
				response.push(MSDPOP.MSDP_TABLE_OPEN);
				for(var key in val) {
					response.push(MSDPOP.MSDP_VAR);
					addMSDPStr(response, key);
					response.push(MSDPOP.MSDP_VAL);
					addMSDP(response, val[key]);
				}
				response.push(MSDPOP.MSDP_TABLE_CLOSE);
				return;
			}
			addMSDPStr(response, ''+val);
		};
		var response = [TELOPT.IAC, TELOPT.SB, TELOPT.MSDP];
		for(var key in json) {
			response.push(MSDPOP.MSDP_VAR);
			addMSDPStr(response, key);
			response.push(MSDPOP.MSDP_VAL);
			if(json[key] != null)
				addMSDP(response, json[key]);
		}
		response.push(TELOPT.IAC);
		response.push(TELOPT.SE);
		var buffer = new Uint8Array(response).buffer;
		if ((buffer.byteLength > 0) && (me.wsopened))
			me.wsocket.send(buffer);
	};
	
	this.enableTrigger = function(value)
	{
		var trigs = this.localTriggers();
		for(var i=0;i<trigs.length;i++)
			if(value == trigs[i].name)
				trigs[i].disabled = false;
		trigs = this.globalTriggers;
		for(var i=0;i<trigs.length;i++)
			if(value == trigs[i].name)
				trigs[i].disabled = false;
	};
	
	this.disableTrigger = function(value)
	{
		var trigs = this.localTriggers();
		for(var i=0;i<trigs.length;i++)
			if(value == trigs[i].name)
				trigs[i].disabled = true;
		trigs = this.globalTriggers;
		for(var i=0;i<trigs.length;i++)
			if(value == trigs[i].name)
				trigs[i].disabled = true;
	};
	
	this.setColor = function(value)
	{
		this.DisplayText('</FONT><FONT COLOR="'+value+'">');
	};
	
	this.isConnected = function() 
	{
		return this.wsopened;
	};
}

function AddNewSipletTabByHostNPort(host, port)
{
	if(!host || !port)
		return;
	var protocol = 'ws:';
	var defaultUrl = protocol + '//' + host;
	var siplet = AddNewSipletTab(defaultUrl+'?port='+port);
	siplet.tabTitle = host + '('+port+')';
	siplet.pb = {
		name: host + '('+port+')',
		host: host,
		port: port
	};
	siplet.pbwhich = '';
	boxFocus();
	return siplet;
}

function AddNewSipletTabByPB(which)
{
	if(which=='')
		return;
	var ogwhich = ''+which;
	var global = ogwhich.startsWith('g');
	var pb;
	if(global)
	{
		which = Number(ogwhich.substr(1));
		if((which<0) || (which > window.phonebook.length))
			return;
		pb = window.phonebook[which];
	}
	else
	{
		var phonebook = getConfig('/phonebook/dial',[]);
		which = Number(ogwhich);
		if((which<0) || (which > phonebook.length))
			return;
		pb = phonebook[which];
	}
	var port = pb.port;
	var defaultUrl;
	if(window.isElectron)
	{
		if(port == 'default')
			port = 23;
		defaultUrl = 'ws://' + pb.host;
	}
	else
	{
		var protocol = (window.location.protocol === 'https:') ? 'wss:' : 'ws:';
		defaultUrl = protocol + '//' + window.location.host + '/WebSock';
	}
	var siplet;
	if(port === 'default')
		siplet = AddNewSipletTab(defaultUrl);
	else
		siplet = AddNewSipletTab(defaultUrl+'?port='+port);
	if(window.isElectron)
		siplet.tabTitle = pb.name;
	else
	if(global)
		siplet.tabTitle = pb.name + '('+pb.port+')';
	else
		siplet.tabTitle = pb.user + '@' + pb.name + ' ('+pb.port+')';
	siplet.pb = pb;
	siplet.pbwhich = ogwhich;
	boxFocus();
	return siplet;
}

function AddNewSipletTab(url)
{
	var windowName = 'W'+window.nextId;
	window.nextId++;
	var newTopElement=document.createElement('DIV');
	var newWinContainer=document.createElement('DIV');
	var newWindow=document.createElement('DIV');
	newTopElement.id = windowName;
	window.windowArea.appendChild(newTopElement);
	newTopElement.appendChild(newWinContainer);
	newWinContainer.appendChild(newWindow);
	newWinContainer.style.cssText = 'position:absolute;top:0%;left:0%;width:100%;height:100%;'
	var siplet = new SipletWindow(windowName); // makes a deep copy
	siplet.tab = AddNewTab();
	siplet.tab.innerHTML = 'Connecting to ' + url + '...';
	siplet.url = url;
	window.siplets.push(siplet);
	siplet.connect(url);
	SetCurrentTab(window.siplets.length-1);
	return siplet;
}

function SetCurrentTabByTab(me)
{
	for(var i=0;i<window.siplets.length;i++)
		if(window.siplets[i].tab == me)
			return SetCurrentTab(i);
	return null;
}

function SetCurrentTab(which)
{
	window.currWin=window.siplets[which];
	for(var i=0;i<window.siplets.length;i++)
	{
		var s = window.siplets[i];
		if(i == which)
		{
			s.topWindow.style.visibility = "visible";
			if(s.wsopened)
			{
				s.tab.style.backgroundColor = "green";
				s.tab.style.color = "white";
			}
		}
		else
		{
			s.topWindow.style.visibility = "hidden";
			if(s.wsopened)
			{
				s.tab.style.backgroundColor = "lightgray";
				s.tab.style.color = "black";
			}
		}
	}
	boxFocus();
	return window.currWin;
}

function CloseAllSiplets()
{
	for(var i=0;i<window.siplets.length;i++)
	{
		var siplet = window.siplets[i];
		siplet.closeSocket();
	}
}

function AutoConnect()
{
	AddNewSipletTabByPB(getConfig('/phonebook/auto','g0'));
	boxFocus();
}

function PBSameAs(pb1, pb2)
{
	if(pb1 === pb2)
		return true;
	return ((window.isElectron)||(pb1.account == pb2.account))
		&&(pb1.name == pb2.name)
		&&(pb1.port == pb2.port)
		&&((!window.isElectron)||(pb1.host == pb2.host))
		&&(pb1.accountName == pb2.accountName)
		&&(pb1.user == pb2.user)
		&&(pb1.password == pb2.password);
}

function FindSipletsByPB(pb)
{
	var found = [];
	for(var i=0;i<window.siplets.length;i++)
	{
		var siplet = window.siplets[i];
		var p = siplet.pb;
		if(PBSameAs(pb,p))
			found.push(siplet);
	}
	return found;
}

function UpdateSipetTabsByPBIndex(which)
{
	if((which+'').startsWith('g'))
		return;
	var phonebook = getConfig('/phonebook/dial',[]);
	which = Number(which);
	if((which<0) || (which > phonebook.length))
		return;
	var pb = phonebook[which];
	for(var i=0;i<window.siplets.length;i++)
	{
		var siplet = window.siplets[i];
		if(Number(siplet.pbwhich) == which)
		{
			siplet.tabTitle = pb.user + '@' + pb.name + ' ('+pb.port+')';
			if(siplet.tab)
				siplet.tab.innerHTML = siplet.tabTitle;
			siplet.pb = pb;
			if(siplet.triggers != null)
				siplet.triggers = null;
		}
	}
}

function PostEvent(event)
{
	for(var k in window.siplets)
	{
		try {
			window.siplets[k].plugins.postEvent(event);
		} catch(e) {
			console.log(e);
		}
	}
}

setTimeout(function() {
	window.sampleSiplet = new SipletWindow('sample');
	var updateSipletConfigs = function() {
		for(var i=0;i<window.siplets.length;i++)
		{
			var siplet = window.siplets[i];
			siplet.width = getConfig('window/width','80');
			siplet.height = getConfig('window/height','25');
			siplet.maxLines = getConfig('window/lines','5000');
			siplet.overflow = getConfig('window/overflow','');
			siplet.fixOverflow();
		}
	};
	addConfigListener('window/width', updateSipletConfigs);
	addConfigListener('window/height', updateSipletConfigs);
	addConfigListener('window/lines', updateSipletConfigs);
	addConfigListener('window/overflow', updateSipletConfigs);
	var updateSipletWindows = function() {
		for(var i=0;i<window.siplets.length;i++)
		{
			var siplet = window.siplets[i];
		    siplet.topWindow.style.fontFamily = getConfig('window/fontface', siplet.topWindow.style.fontFamily);
		    siplet.topWindow.style.fontSize = getConfig('window/fontsize', siplet.topWindow.style.fontSize);
	    }
	}
	addConfigListener('window/fontface', updateSipletWindows);
	addConfigListener('window/fontsize', updateSipletWindows);
},0);
