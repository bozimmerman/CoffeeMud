window.currWin = null;
window.siplets = [];
window.windowArea = null;
window.nextId = 0;

var Siplet =
{
	VERSION_MAJOR: '3.2',
	VERSION_MINOR: '0',
	NAME: window.isElectron?'Sip':'Siplet',
	R: /^win\.[\w]+(\.[\w]+)*$/
};

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

function SipletWindow(windowName)
{
	this.decoder = new TextDecoder("utf-8");
	this.siplet = Siplet;
	this.width = 80;
	this.height = 25;
	this.charWidth = parseFloat(getConfig('window/fontsize', '16px'));
	this.charHeight = parseFloat(getConfig('window/fontsize', '16px')) * 0.7;
	this.pixelWidth = 800;
	this.pixelHeight = 600;
	this.maxLines = getConfig('window/lines',1000);
	this.MSDPsupport = false;
	this.GMCPsupport = false;
	this.MSPsupport = false;
	this.MXPsupport = false;
	this.MCCPsupport = false;
	this.decompressor = null;
	this.wsopened = false;
	this.windowName = windowName;
	this.bin = new BPPARSE(this,false);
	this.ansi = new ANSISTACK(this);
	this.telnet = new TELNET(this);
	this.msp = new MSP(this);
	this.mxp = new MXP(this);
	this.plugins = new PLUGINS(this);
	this.mapper = new Mapper(this);
	this.text = new TEXT([this.mxp,this.msp]);
	this.topWindow = document.getElementById(windowName);
	if(this.topWindow) 
		this.topWindow.sipwin = this;
	this.wsocket = null;
	this.gauges=[];
	this.gaugeWindow = null;
	this.textBuffer = '';
	this.textBufferPruneIndex = 0;
	this.tempMenus  = null;
	this.globalTriggers = GetGlobalTriggers();
	this.triggers = null;
	this.tempTriggers  = null;
	this.globalAliases = GetGlobalAliases();
	this.aliases = null;
	this.tempAliases = null;
	this.globalTimers = GetGlobalTimers();
	this.timers = null;
	this.tempTimers = null;
	this.activeTimers = [];
	this.lastStyle = '';
	this.listeners = {};
	this.logStream = null;
	this.overflow = getConfig('window/overflow','WRAP');
	this.sipfs = new SipletFileSystem('SipletFileSystem', this);
	this.debugFlush = false;
	this.debugText = false;
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
	
	this.resizeTermWindow = function()
	{
		var fontFace = this.topWindow.style.fontFamily;
		var fontSize = this.topWindow.style.fontSize;
		fontSize = parseFloat(fontSize);
		
		var oldWidth = this.width;
		var oldHeight = this.height;
		var oldCharWidth = this.charWidth;
		var oldCharHeight = this.charHeight;
		var oldPixelWidth = this.pixelWidth;
		var oldPixelHeight = this.pixelHeight;
		
		var computedStyle = window.getComputedStyle(this.window);
		var pixelWidth = this.window.offsetWidth;
		var pixelHeight = this.window.offsetHeight;
		var paddingLeft = parseFloat(computedStyle.paddingLeft);
		var paddingRight = parseFloat(computedStyle.paddingRight);
		var borderLeft = parseFloat(computedStyle.borderLeftWidth);
		var borderRight = parseFloat(computedStyle.borderRightWidth);
		var contentWidth = pixelWidth - paddingLeft - paddingRight - borderLeft - borderRight - 17;
		var paddingTop = parseFloat(computedStyle.paddingTop);
		var paddingBottom = parseFloat(computedStyle.paddingBottom);
		var borderTop = parseFloat(computedStyle.borderTopWidth);
		var borderBottom = parseFloat(computedStyle.borderBottomWidth);
		var contentHeight = pixelHeight - paddingTop - paddingBottom - borderTop - borderBottom;
		// Measure character width
		var widthSpan = document.createElement('span');
		widthSpan.style.fontSize = fontSize + 'px';
		widthSpan.style.fontFamily = fontFace;
		widthSpan.style.position = 'absolute';
		widthSpan.style.visibility = 'hidden';
		widthSpan.style.whiteSpace = 'pre';
		widthSpan.textContent = 'M';
		this.window.appendChild(widthSpan);
		var charWidth = widthSpan.offsetWidth;
		var charHeight = widthSpan.offsetHeight;
		this.window.removeChild(widthSpan);
		var lineHeight = parseFloat(computedStyle.lineHeight) || fontSize * 1.2; // Fallback: 1.2 * fontSize
		this.width = Math.floor(contentWidth / charWidth);
		this.height = Math.floor(contentHeight / lineHeight);
		this.charWidth = charWidth;
		this.charHeight = charHeight;
		this.pixelWidth = contentWidth;
		this.pixelHeight = contentHeight;
		if((this.width != oldWidth)||(this.height != oldHeight))
		{
			this.telnet.sendNaws(true);
			this.ansi.sendCharsDimStr(true);
		}
		if((this.pixelWidth != oldPixelWidth)||(this.pixelHeight != oldPixelHeight))
			this.ansi.sendPixelsDimStr(true);
		if((this.charWidth != oldCharWidth)||(this.charHeight != oldCharHeight))
			this.ansi.sendCharDimStr(true);
		this.ansi.sendTermPosStr(true);
	};
	
	this.sendRaw = function(arr)
	{
		if (Array.isArray(arr) && (arr.length > 0) 
		&& this.wsopened && this.wsocket)
			this.wsocket.send(new Uint8Array(arr).buffer);
	};

	this.sendStr = function(str)
	{
		if ((typeof str === 'string')
		&& str && this.wsopened && this.wsocket)
			this.wsocket.send(str);
	};

	if(this.topWindow)
	{
		this.topWindow.onclick = function(e) {
			ContextDelayHide(); 
			if(e.target.tagName == 'INPUT')
				return;
			setInputBoxFocus(); 
		};
		var fontFace = getConfig('window/fontface', 'monospace');
		var fontSize = getConfig('window/fontsize', '16px');
		this.topWindow.style.fontFamily = fontFace;
		this.topWindow.style.fontSize = fontSize;
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
		this.resizeTermWindow(fontFace, fontSize);
		this.window.addEventListener('paste', function() {
			event.preventDefault();
			var text = (event.clipboardData || window.clipboardData).getData('text');
			addToPrompt(text,false);
		});
	}
	
	this.connect = function(url)
	{
		this.closeSocket();
		this.wsocket = new WebSocket(url);
		this.wsocket.binaryType = "arraybuffer";
		this.wsocket.onmessage = this.onReceive;
		if(this.tab)
		{
			this.tab.style.backgroundColor="yellow";
			this.tab.style.color="black";
		}
		this.wsocket.onopen = function(event)  
		{
			me.dispatchEvent({type: 'connect',data:url});
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
			me.closeLog();
			me.flushWindow();
			if(me.tab && me.tab.innerHTML.startsWith("Connecting"))
				me.tab.innerHTML = 'Failed connection to ' + url;
			me.dispatchEvent({type: 'closesock',data:url});
			me.wsopened=false; 
			me.tab.style.backgroundColor="#FF555B";
			me.tab.style.color="white";
			me.clearTimers();
		};
	};

	this.closeSocket = function()
	{
		if (this.wsocket && this.wsocket.readyState === WebSocket.OPEN)
			this.wsocket.close();
	};
	
	this.close = function()
	{
		this.dispatchEvent({type: 'close'});
		this.closeSocket();
		this.reset();
	};
	
	this.process = function(s)
	{
		if(!s) return s;
		try
		{
			var oldResume = this.text.resume;
			var oldFlush = this.flushWindow;
			var oldMXP = this.MXPsupport;
			var oldMSP = this.MSPsupport;
			this.flushWindow = function(){};
			this.text.resume = null;
			this.MXPsupport = true;
			this.MSPsupport = true;
			s=this.text.process(s)
			var startTime = Date.now();
			while(this.text.resume && ((Date.now() - startTime)<500))
				s+=this.text.process('')
			s += this.text.flush();
			if((!this.mxp.active())&&(this.fixVariables))
				s = this.fixVariables(s);
		}
		catch(e) { 
			console.warn(e);
		}
		this.flushWindow = oldFlush;
		this.text.resume = oldResume;
		this.MSPsupport = oldMSP;
		this.MXPsupport = oldMXP;
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
	if(this.topContainer)
		setTimeout(function() { me.mxpFix(); },1);

	this.reset = function()
	{
		this.closeLog();
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
		this.decompressor = null;
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
		this.tempTriggers = null;
		this.globalAliases = GetGlobalAliases();
		this.aliases = null;
		this.tempAliases = null;
		this.clearTimers();
		this.globalTimers = GetGlobalTimers();
		this.timers = null;
		this.tempTimers = null;
		this.tempMenus = null;
		this.listeners = {};
		this.resetTimers();
		this.mxpFix();
		this.fixOverflow();
	};
	
	this.htmlBuffer = '';
	this.numLines = 0;
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
			this.htmlBuffer = reprocess + this.htmlBuffer;
			this.numLines += brCount(this.htmlBuffer);
			if(this.debugFlush)
				console.log('Flush: '+this.htmlBuffer);
			span.innerHTML = this.htmlBuffer;
			updateMediaImagesInSpan(this.sipfs, span);
			this.window.appendChild(span);
			this.process(reprocess);
			this.htmlBuffer='';
			if(window.currWin != me)
			{
				this.tab.style.backgroundColor = "lightgreen";
				this.tab.style.color = "black";
			}
			while((this.numLines > me.maxLines)
			&&(this.window.childElementCount > 1))
			{
				var child = this.window.firstChild;
				this.numLines -= brCount(child.innerHTML);
				this.window.removeChild(child);
			}
			if(rescroll)
				this.scrollToBottom(this.window,0);
			DisplayFakeInput(null);
		}
	};
	
	this.onReceive = function(e)
	{
		var entries = me.bin.parse(e.data);
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
					if(!newText)
						continue; // nothing happened
					blk.data = newText; // convert to text-like
					if((!me.mxp.active()) // is there even a point?
					||((me.mxp.mode == MXPMODE.LINE_LOCKED) || (me.mxp.mode == MXPMODE.LOCK_LOCKED)))
					{
						me.htmlBuffer += newText;
						continue;
					}
				}
				else
				{
					try
					{
						newText += me.decoder.decode(new Uint8Array(blk.data).buffer);
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
					var plain = stripHtmlTags(newText.replace(/<br\s*\/?>/gi, '\n'));
					if(plain)
					{
						if(me.debugText)
							console.log('text: '+plain);
						me.textBuffer += plain;
						me.writeLog(plain);
					}
					if(newText.indexOf('<BR>')>=0)
					{
						if(me.htmlBuffer.length > 16384)
							me.flushWindow();
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
	
	this.executeAction = function(action)
	{
		action = this.fixVariables(action);
		var p = action.indexOf("(");
		try 
		{
			var act = SipletActions[action.substr(0,p)];
			var z = action.lastIndexOf(')');
			if(IsQuotedStringArgument(action.substring(p+1,z),act.args,Siplet.R))
			{
				var win = this;
		 		eval(action);
	 		}
		}
		catch(e)
		{
			 console.error(e);
		};
	};

	this.menus = function() {
		return this.plugins.menus(this.tempMenus)
	};
	
	this.evalTriggerGroup = function(triggers)
	{
		var win = this;
		for(var i=0;i<triggers.length;i++)
		{
			var trig = triggers[i];
			if(trig.pattern
			&& (!trig.disabled)
			&& (SafeEval(trig.allowed,{win:this})))
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
							this.setEntity('match'+i,match[i]);
						trig.prev = this.textBufferPruneIndex + match.index + 1;
						this.executeAction(trig.action);
						for(var i=0;i<match.length;i++)
							this.setEntity('match'+i,null);
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
						this.executeAction(trig.action);
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
		if(this.tempTriggers)
			this.evalTriggerGroup(this.tempTriggers);
	};
	
	this.addTempTrigger = function(trigger)
	{
		trigger = this.validatedTrigger('Temp', trigger);
		if(trigger)
		{
			if(this.tempTriggers==null)
				this.tempTriggers = [];
			this.tempTriggers.push(trigger);
			return true;
		}
		return false;
	}

	this.removeTempTrigger = function(name)
	{
		if(this.tempTriggers==null)
			return false;
		for(var i=0;i<this.tempTriggers.length;i++)
			if(this.tempTriggres[i].name == name)
			{
				this.tempTriggers.splice(i,1);
				if(this.tempTriggers.length == 0)
					this.tempTriggers = null;
				return true;
			}
		return false;
	}

	this.addTempMenu = function(top, menu)
	{
		menu = this.validatedMenu('Temp '+top, menu);
		if(menu && top)
		{
			if(this.tempMenus==null)
				this.tempMenus = {};
			this.tempMenus[top] = menu;
			return true;
		}
		return false;
	}

	this.removeTempMenu = function(name)
	{
		if(this.tempMenus==null)
			return false;
		if(name in this.tempMenus)
		{
			delete this.tempMenus[name];
			return true;
		}
		return false;
	}

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
		if(!event)
			return;
		if((typeof event === 'string')||(typeof event === 'number'))
			event = {'type': (''+event)};
		else
		if(!('type' in event))
			event.type = 'event';
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
					try {
						calls[i](event);
					} catch(e){console.error(e);}
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
							this.setEntity('match'+i,match[i]);
						txt = txt.replace(alias.pattern,this.fixVariables(alias.replace));
						this.executeAction(alias.action);
						for(var i=0;i<match.length;i++)
							this.setEntity('match'+i,null);
						return txt
					}
				}
				else
				if(txt.startsWith(alias.pattern))
				{
					this.executeAction(alias.action);
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
		if(this.tempAliases)
			x = this.evalAliasGroup(this.tempAliases, x);
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
	
	this.addTempAlias = function(alias)
	{
		alias = this.validatedAlias ('Temp', alias);
		if(alias)
		{
			if(this.tempAliases==null)
				this.tempAliases = [];
			this.tempAliases.push(alias);
			return true;
		}
		return false;
	};

	this.removeTempAlias = function(name)
	{
		if(this.tempAliases==null)
			return false;
		for(var i=0;i<this.tempAliases.length;i++)
			if(this.tempAliases[i].name == name)
			{
				this.tempAliases.splice(i,1);
				if(this.tempAliases.length == 0)
					this.tempAliases = null;
				return true;
			}
		return false;
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
		if(this.localTimers)
		{
			for(i=0;i<this.localTimers.length;i++)
				if((this.localTimers[i].name == name)
				||(ci && (this.localTimers[i].name.toLowerCase() == name.toLowerCase())))
					return this.localTimers[i];
		}
		return null;
	};

	
	this.addTempTimer = function(timer)
	{
		timer = this.validatedTimer('Temp', timer);
		if(timer)
		{
			if(this.tempTimers==null)
				this.tempTimers = [];
			this.tempTimers.push(timer);
			return true;
		}
		return false;
	};

	this.removeTempTimer = function(name)
	{
		if(this.tempTimers==null)
			return false;
		for(var i=0;i<this.tempTimers.length;i++)
			if(this.tempTimers[i].name == name)
			{
				this.tempTimers.splice(i,1);
				if(this.tempTimers.length == 0)
					this.tempTimers = null;
				return true;
			}
		return false;
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
		if(timer.option != 'multiple')
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
			me.executeAction(timer.action);
			if(timer.option == 'repeat')
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
			if(this.tempTimers)
				this.startTimers(this.tempTimers);
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
	
	this.createGauge = function(entity,maxEntity,bar,caption,color,value,max)
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
		gaugedata.lentity = entity.toLowerCase();
		gaugedata.maxEntity = maxEntity;
		gaugedata.lmaxEntity = (maxEntity==null)?null:maxEntity.toLowerCase();
		gaugedata.bar = bar;
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
			parent.style.top = 'calc(' + parent.style.top + ' + ' + gaugeHeight + 'px)';
			parent.style.height = 'calc(' + parent.style.height + ' - ' + gaugeHeight + 'px)';
		}
		this.modifyGauges();
		return gaugedata;
	};
	
	this.removeGauge = function(entity)
	{
		var index = this.gauges.indexOf(entity);
		if(index >=0)
		{
			this.gauges.splice(index);
			this.modifyGauges();
			if((this.gauges.length == 0) && (this.topWindow != null))
			{
				var mainWindow = this.gaugeWindow.parentNode.firstChild;
				mainWindow.style.top = this.gaugeWindow.top;
				if(this.gaugeWindow.parentNode)
					this.gaugeWindow.outerHTML = '';
				this.gaugeWindow = null;
			}
		}
	};
	
	this.modifyGauges = function()
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
				s+='<TD WIDTH='+cellwidth+'%>';
				if(gaugedata.bar)
				{
					s+='<FONT STYLE="color: '+gaugedata.color+'" SIZE=-2>'+gaugedata.caption+'</FONT><BR>';
					var fullwidth=100-gaugedata.value;
					var lesswidth=gaugedata.value;
					s+='<TABLE WIDTH=100% CELLPADDING=0 CELLSPACING=0 BORDER=0 HEIGHT=5><TR HEIGHT=5>';
					s+='<TD STYLE="background-color: '+gaugedata.color+'" WIDTH='+lesswidth+'%></TD>';
					s+='<TD STYLE="background-color: black" WIDTH='+fullwidth+'%></TD>';
					s+='</TR></TABLE>';
					s+='</TD>';
				}
				else
				{
					s+='<FONT SIZE=-2 COLOR=WHITE>';
					if(gaugedata.caption)
						s += gaugedata.caption+': ';
					if(gaugedata.max != null)
						s += gaugedata.value + '/' + gaugedata.max;
					else
						s += gaugedata.value;
					s += '</FONT> ';
				}
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
		if(this.telnet.localEcho)
			this.displayText(value);
		value = this.fixVariables(value);
		this.sendStr(value+'\n');
	};
	
	this.submitHidden = function(value)
	{
		if((value === undefined) || (value == null))
			return;
		value = this.fixVariables(value);
		this.sendStr(value+'\n');
	};
	
	this.isAtBottom = function()
	{
		var div = this.window;
		const distanceFromBottom = div.scrollHeight - div.scrollTop - div.clientHeight;
		const halfVisibleArea = div.clientHeight * 0.7;
		return distanceFromBottom < halfVisibleArea;
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
			var span = this.displayBit(value + '<BR>');
			updateMediaImagesInSpan(this.sipfs, span);
			DisplayFakeInput(null);
			return span;
		}
		return null;
	};

	this.displayBit = function(value)
	{
		if(value)
		{
			var rescroll = this.isAtBottom(-10);
			var span = document.createElement('span');
			this.writeLogHtml(value);
			span.innerHTML = value.replaceAll('\n','<BR>');
			this.window.appendChild(span);
			if(rescroll)
				this.scrollToBottom(this.window,0);
			return span;
		}
		return null;
	};

	this.displayAt = function(value, frame)
	{
		var framechoices = this.mxp.getFrameMap();
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

	this.setEntity = function(key, value)
	{
		if((key !== undefined)&&(value !== undefined)&&(key != null))
		{
			if(value == null)
				delete this.mxp.entities[key.toLowerCase()];
			else 
				this.mxp.modifyEntity(key, this.fixVariables(value));
			
		}
	};
	
	this.getEntity = function(key)
	{
		if((key === undefined)||(key == null))
			return '';
		if(key.toLowerCase() in this.mxp.entities)
			return this.fixVariables(this.mxp.entities[key.toLowerCase()]);
		return '';
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
			if(typeof event === 'object')
				json = event;
			else
			{
				try {
					json = JSON.parse(''+event);
				} catch(e) {
					json = {'type':'event', 'data': event};
				}
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
				console.error(e);
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
		me.sendRaw(response);
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
				console.error(e);
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
		me.sendRaw(response);
	};
	
	this.setTriggersStates = function(triggers, value, state)
	{
		if(triggers)
		{
			for(var i=0;i<triggers.length;i++)
				if(value == triggers[i].name)
					triggers[i].disabled = state;
		}
	};
	
	this.enableTrigger = function(value)
	{
		this.setTriggersStates(this.localTriggers(), value, false);
		this.setTriggersStates(this.globalTriggers, value, false);
		this.setTriggersStates(this.tempTriggers, value, false);
	};
	
	this.disableTrigger = function(value)
	{
		this.setTriggersStates(this.localTriggers(), value, true);
		this.setTriggersStates(this.globalTriggers, value, true);
		this.setTriggersStates(this.tempTriggers, value, true);
	};
	
	this.setColor = function(value)
	{
		this.DisplayText('</FONT><FONT COLOR="'+value+'">');
	};
	
	this.isConnected = function() 
	{
		return this.wsopened;
	};
	
	this.openLog = function(filePath) {
		try 
		{
			if(this.logStream)
				this.closeLog();
			const fs = require('fs');
			this.logStream = fs.createWriteStream(filePath, {flags: 'w'});
			this.tab.style.border='1px solid yellow';
		} catch(e) {
			this.tab.style.border='1px solid red';
			console.error(e);
			this.logStream = null;
		}
	};
	
	this.closeLog = function(filePath) {
		this.tab.style.border='1px solid white';
		if(this.logStream != null)
			this.logStream.end();
		this.logStream = null;
	};
	
	this.writeLogHtml = function(msg)
	{
		if(this.logStream != null)
		{
			var plain = stripHtmlTags(msg.replace(/<br\s*\/?>/gi, '\n'));
			if(plain)
				me.writeLog(plain);
		}
	};
	
	this.writeLog = function(msg) {
		if(this.logStream != null)
		{
			try {
				this.logStream.write(msg);
			} catch(e) {
			}
		}
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
	setInputBoxFocus();
	return siplet;
}

function AddNewSipletTabByPB(which)
{
	if(!which)
		return;
	var ogwhich = ''+which;
	var global = ogwhich.startsWith('g');
	var pb;
	var disableInput = getConfig('window/disableInput','')==''?false:true;
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
		if(pb.disableInput !== undefined)
			disableInput = pb.disableInput;
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
		siplet = AddNewSipletTab(defaultUrl,!disableInput);
	else
		siplet = AddNewSipletTab(defaultUrl+'?port='+port,!disableInput);
	if(window.isElectron)
		siplet.tabTitle = pb.name;
	else
	if(global)
		siplet.tabTitle = pb.name + '('+pb.port+')';
	else
		siplet.tabTitle = pb.user + '@' + pb.name + ' ('+pb.port+')';
	siplet.pb = pb;
	siplet.pbwhich = ogwhich;
	setInputBoxFocus();
	ReConfigureTopMenu(siplet);
	return siplet;
}

function AddNewSipletTab(url, ib)
{
	if((ib === undefined) || (ib == null))
		ib = (getConfig('window/disableInput','')==''?true:false);
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
	siplet.tab.siplet = siplet;
	siplet.url = url;
	window.siplets.push(siplet);
	siplet.connect(url);
	SetCurrentTab(window.siplets.length-1);
	setInputVisibility(ib);
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
				if(s.tab.style.backgroundColor !== 'lightgreen')
					s.tab.style.backgroundColor = "lightgray";
				s.tab.style.color = "black";
			}
		}
	}
	if(window.currWin.pb)
		setInputVisibility(!window.currWin.pb.disableInput);
	else
		setInputVisibility(getConfig('window/disableInput','')==''?true:false);
	setInputBoxFocus();
	ReConfigureTopMenu(window.currWin);
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

function ResizeAllSiplets()
{
	for(var i=0;i<window.siplets.length;i++)
	{
		var siplet = window.siplets[i];
		if(siplet.window && siplet.topWindow)
			siplet.resizeTermWindow();
	}
}

function AutoConnect()
{
	var auto = getConfig('/phonebook/auto','-2');
	if(auto === '-2')
		menuConnect();
	else
		AddNewSipletTabByPB(auto);
	setInputBoxFocus();
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

function PostGlobalEvent(event)
{
	for(var k in window.siplets)
	{
		try {
			window.siplets[k].dispatchEvent(event);
		} catch(e) {
			console.error(e);
		}
	}
}

setTimeout(function() {
	var updateSipletConfigs = function() {
		for(var i=0;i<window.siplets.length;i++)
		{
			var siplet = window.siplets[i];
			siplet.maxLines = getConfig('window/lines', '1000');
			siplet.overflow = getConfig('window/overflow','WRAP');
			siplet.fixOverflow();
		}
	};
	addConfigListener('window/lines', updateSipletConfigs);
	addConfigListener('window/overflow', updateSipletConfigs);

	var updateSipletWindows = function() {
		for(var i=0;i<window.siplets.length;i++)
		{
			var siplet = window.siplets[i];
			var fontFace = getConfig('window/fontface', siplet.topWindow.style.fontFamily);
			var fontSize = getConfig('window/fontsize', siplet.topWindow.style.fontSize);
			siplet.topWindow.style.fontFamily = fontFace;
			siplet.topWindow.style.fontSize = fontSize
			siplet.resizeTermWindow();
		}
	}
	addConfigListener('window/fontface', updateSipletWindows);
	addConfigListener('window/fontsize', updateSipletWindows);
},0);
