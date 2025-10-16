var menuAreaHeight = 20;
var menuArea = null;
var menuBackgroundColor = "#105010";
var menuForegroundColor = "Yellow";
var menuWindow = null;
var menuTemp = null;
var menuFontSize=12;

var menuData = [
	{"Window": [
		{"n":"Connect",
		 "a":"javascript:menuConnect();",
		 "e":""},
		{"n":"Triggers",
		 "e":"window.currWin!=null && window.currWin.pb && window.currWin.pb.pb",
		 "a":"javascript:menuTriggers('local');"},
		{"n":"Aliases",
		 "e":"window.currWin!=null && window.currWin.pb && window.currWin.pb.pb",
		 "a":"javascript:menuAliases('local');"},
		{"n":"Scripts",
		 "e":"window.currWin!=null && window.currWin.pb && window.currWin.pb.pb",
		 "a":"javascript:menuScripts('local');"},
		{"n":"Timers",
		 "e":"window.currWin!=null && window.currWin.pb && window.currWin.pb.pb",
		 "a":"javascript:menuTimers('local');"},
		{"n":"Entities",
		 "e":"window.currWin!=null && window.currWin.pb && window.currWin.pb.pb",
		 "a":"javascript:menuEntities('local');"},
		{"n":"Plugins",
		 "e":"window.currWin!=null && window.currWin.pb && window.currWin.pb.pb",
		 "a":"javascript:menuPlugins('local');"},
		{"n":"Capture",
		 "v":"window.isElectron",
		 "e":"window.currWin && window.currWin.wsopened",
		 "a":"javascript:menuCapture();"},
		{"n":"Reconnect",
		 "e":"window.currWin!=null && !window.currWin.wsopened",
		 "a":"javascript:menuReconnect();"},
		{"n":"Disconnect",
		 "e":"window.currWin!=null && window.currWin.wsopened",
		 "a":"javascript:menuDisconnect();"}
	]},
	{"Global Options": [
		{"n":"Windows",
		 "a":"javascript:menuWindows();"},
		{"n":"Triggers",
		 "a":"javascript:menuTriggers('global');"},
		{"n":"Aliases",
		 "a":"javascript:menuAliases('global');"},
		{"n":"Scripts",
		 "a":"javascript:menuScripts('global');"},
		{"n":"Timers",
		 "a":"javascript:menuTimers('global');"},
		{"n":"Entities",
		 "a":"javascript:menuEntities('global');"},
		{"n":"Custom Media",
		 "a":"javascript:menuMedia();"},
		{"n":"Plugins",
		 "a":"javascript:menuPlugins('global');"}
	]},
	{"Help": [
		{"n":"About","a":"javascript:menuAbout()"},
		{"n":"Input",
		 "a":"javascript:menuHelp('Input');"},
		{"n":"Windows",
		 "a":"javascript:menuHelp('Windows');"},
		{"n":"Triggers",
		 "a":"javascript:menuHelp('Triggers');"},
		{"n":"Aliases",
		 "a":"javascript:menuHelp('Aliases');"},
		{"n":"Scripts",
		 "a":"javascript:menuHelp('Scripts');"},
		{"n":"Timers",
		 "a":"javascript:menuHelp('Timers');"},
		{"n":"Entities",
		 "a":"javascript:menuHelp('Entities');"},
		{"n":"Media",
		 "a":"javascript:menuHelp('Media');"},
		{"n":"Plugins",
		 "a":"javascript:menuHelp('Plugins');"}
	]}
];

function ReConfigureTopMenu(sipwin)
{
	if(sipwin && Object.keys(sipwin.menus()).length > 0)
		menuTemp =  sipwin.menus();
	else
	if(menuTemp == null)
		return; // no change
	else
		menuTemp = null;
	var html = '';
	html += '<TABLE style="border: 1px solid white; border-collapse: collapse; height: '+menuAreaHeight+'px; '
		 				 +'table-layout: fixed; width: 100%;cursor: pointer;">';
	html += '<TR style="height: ' + menuAreaHeight + 'px;" >';
	var alreadyDone = {};
	var tdWidth = 100;
	var totalTDWidth = 0;
	for(var to=0;to<menuData.length;to++)
	{
		totalTDWidth += tdWidth;
		var topO = menuData[to];
		var topN = Object.keys(topO)[0];
		html += '<TD style="border: 1px solid white; padding: 0;width:'+tdWidth+'px;" tabindex="0"';
		html += ' ONCLICK="DropDownMenu(event,this.offsetLeft,this.offsetTop+'+menuAreaHeight+',150,'+menuFontSize+','+to+')" ';
		html += '><FONT style="font-size: '+menuFontSize+'" COLOR="'+menuForegroundColor+'"><B>&nbsp;&nbsp;';
		html += topN + '</FONT></TD>';
		alreadyDone[topN] = true;
	}
	if(menuTemp)
	{
		for(var key in menuTemp)
		{
			if(!alreadyDone[topN])
			{
				totalTDWidth += tdWidth;
				html += '<TD style="border: 1px solid white; padding: 0;width:'+tdWidth+'px;" tabindex="0"';
				html += ' ONCLICK="DropDownMenu(event,this.offsetLeft,this.offsetTop+'+menuAreaHeight+',150,'+menuFontSize+','+key+')" ';
				html += '><FONT style="font-size: '+menuFontSize+'" COLOR="'+menuForegroundColor+'"><B>&nbsp;&nbsp;';
				html += key + '</FONT></TD>';
			}
		}
	}
	html += '<TD style="border: 1px solid white; padding: 0; width: calc(100% - '+totalTDWidth+'px);">&nbsp;</TD>';
	html += '</TR></TABLE>';
	menuArea.innerHTML = html;
	menuArea.lookupKeys = {};
	var entries = menuArea.querySelectorAll('td');
	if(entries == null)
		return;
	for(var e=0;e<entries.length;e++)
	{
		var td = entries[e];
		var font = td.querySelector('font');
		if(!font || (!font.textContent))
			continue;
		var text = font.textContent.trim();
		for(var i=0;i<text.length;i++)
		{
			var c=text[i].toLowerCase();
			if(!(c in menuArea.lookupKeys))
			{
				menuArea.lookupKeys[c]=td;
				font.innerHTML='';
				var beforeText=text.substring(0,i);
				var charText=text.substring(i,i+1);
				var afterText = text.substring(i+1);
				if(beforeText)
					font.appendChild(document.createTextNode(beforeText));
				var u=document.createElement('u');
				u.appendChild(document.createTextNode(charText));
				font.appendChild(u);
				if(afterText)
					font.appendChild(document.createTextNode(afterText));
				break;
			}
		}
	}
}

function ConfigureTopMenu(obj)
{
	menuTemp = [];
	menuArea=obj;
	menuArea.style.position='fixed';
	menuArea.style.top=0;
	menuArea.style.width='100%';
	menuArea.style.height=menuAreaHeight+'px';
	menuArea.style.background=menuBackgroundColor;
	menuArea.style.color=menuForegroundColor;
	ReConfigureTopMenu();
}

function DropDownMenu(e, left, top, width, fontSize, to, subMenu) 
{
	if(subMenu === undefined)
		ContextHideAll();
	var subList;
	if(typeof to === 'number')
	{
		var topO = menuData[to];
		var topN = Object.keys(topO)[0];
		subList = topO[topN];
		if(menuTemp && menuTemp[topN])
			subList = subList.concat(menuTemp[topN]);
	}
	else
	if(typeof to === 'string' && menuTemp)
		subList = menuTemp[to];
	else
		subList = to;
	var m;
	if(subMenu === true)
		m = ContextSubMenuOpen(e, subList, left, top, width, 5);
	else
		m = ContextMenuOpen(e, subList, left, top, width, 5);
	m.topId = e.currentTarget;
	m.style.background = menuBackgroundColor;
	m.style.color = menuForegroundColor;
	m.lookupKeys = {};
	var as = Array.from(m.getElementsByTagName("A"));
	for(var a=0;a<as.length;a++)
	{
		as[a].style.color=menuForegroundColor;
		as[a].style.fontSize=fontSize;
		as[a].style.textDecoration='none';
		(function(link)
		{
			link.addEventListener('mouseenter', function() {
				link.focus();
			});
			link.addEventListener('mouseleave', function() {
				link.blur();
			});
			link.addEventListener('focus', function() {
				this.style.color = menuBackgroundColor;
				this.style.backgroundColor = menuForegroundColor;
			});
			link.addEventListener('blur', function() {
				this.style.color = menuForegroundColor;
				this.style.backgroundColor = menuBackgroundColor;
			});
			if(link.onclick)
			{
				var font = link.querySelector('font');
				if(!font)
					font = link;
				var text = font.textContent;
				for(var i=0;i<text.length;i++)
				{
					var c = text[i].toLowerCase();
					if(!(c in m.lookupKeys))
					{
						m.lookupKeys[c]=font;
						font.innerHTML='';
						var beforeText=text.substring(0,i);
						var charText=text.substring(i,i+1);
						var afterText=text.substring(i+1);
						if(beforeText)
							font.appendChild(document.createTextNode(beforeText));
						var u = document.createElement('u');
						u.appendChild(document.createTextNode(charText));
						font.appendChild(u);
						if(afterText)
							font.appendChild(document.createTextNode(afterText));
						break;
					}
				}
			}
		})(as[a]);
	}
	m.addEventListener('keydown', function (event) 
	{
		if (!m || !m.contains(document.activeElement)) 
			return;
		var currentIndex = as.indexOf(document.activeElement);
		if (currentIndex < 0) 
			return;
		if(event.key === 'ArrowDown')
		{
			currentIndex = (currentIndex + 1) % as.length;
			event.preventDefault();
			as[currentIndex].focus();
			return;
		}
		else 
		if(event.key === 'ArrowUp')
		{
			currentIndex = (currentIndex - 1 + as.length) % as.length;
			event.preventDefault();
			as[currentIndex].focus();
			return;
		}
		if(event.key === 'ArrowLeft')
		{
			event.preventDefault();
			topMenuFocusMove(m.topId,-1);
			return;
		}
		else 
		if(event.key === 'ArrowRight')
		{
			event.preventDefault();
			topMenuFocusMove(m.topId,1);
			return;
		}
		if(event.key.length == 1)
		{
			var k = (''+event.key).toLowerCase(); 
			if(k in m.lookupKeys)
			{
				var a = m.lookupKeys[k];
				a.focus();
				setTimeout(function(){
					a.click();
				},1);
			} 
		}
	});
	setTimeout(function(){
		var menu = document.getElementById('ctxmenu');
		if(menu == m)
		{
			var first = menu.querySelectorAll('a');
			if(first && first.length)
				first[0].focus();
		}
	},1);
	return m;
}

function hideOptionWindow()
{
	if(menuWindow != null)
	{
		menuWindow.style.visibility = 'hidden';
		menuWindow.onclick = function() {};
		var contentWindow = menuWindow.getElementsByTagName('div')[1];
		contentWindow.innerHTML = '';
	}
}

function optionWindowFocus()
{
	if((menuWindow != null)
	&&(menuWindow.style.visibility == 'visible'))
		focusFirstFocusable(menuWindow);
}

function optionWindowTab(e, shift)
{
	if((menuWindow != null)
	&&(menuWindow.style.visibility == 'visible'))
	{
		var elems = getTabbableElements(menuWindow);
		if(elems && elems.length)
		{
			var x = Array.from(elems).indexOf(document.activeElement);
			if(shift && document.activeElement == elems[0])
			{
				e.preventDefault();
				elems[elems.length-1].focus();
				return;
			}
			if(!shift && document.activeElement == elems[elems.length-1])
			{
				e.preventDefault();
				elems[0].focus();
				return;
			}
		}
	}
}

function topMenuFocusMove(td, move)
{
	var mv = (move === undefined)?0:move;
	var menu = document.getElementById('ctxmenu');
	if(!menu)
		return;
	
	var cols = menuArea.querySelectorAll('tr');
	if(cols && (cols.length > 0))
	{
		var rows = cols[0].querySelectorAll('td');
		if((!rows) || (!rows.length))
			return;
		ContextHideAll();
		for(var r=0;r<rows.length-1;r++)
		{
			if(td == rows[r])
			{
				var nr = r + mv;
				if(nr < 0)
					nr = rows.length-2;
				else
				if(nr >= rows.length-1)
					nr = 0;
				rows[nr].focus();
				rows[nr].click();
				return;
			}
		}
	}
}

function topMenuFocus(key)
{
	ContextHideAll();
	var cols = menuArea.querySelectorAll('tr');
	if(cols && (cols.length > 0))
	{
		var rows = cols[0].querySelectorAll('td');
		if((!rows) || (!rows.length))
			return;
		for(var r=0;r<rows.length;r++)
		{
			if(!rows[r].textContent)
				continue;
			var k = (''+key).toLowerCase(); 
			if(k in menuArea.lookupKeys)
			{
				var a = menuArea.lookupKeys[k];
				a.focus();
				a.click();
				return;
			}
		}
	}
}

function getOptionWindow(heading, w, h)
{
	if(menuWindow == null)
	{
		menuWindow = document.createElement('div');
		menuWindow.style.cssText = "position:absolute;top:20%;left:10%;height:60%;width:80%;z-index:99;";
		menuWindow.style.cssText += "border-style:solid;border-width:5px;border-color:white;cursor:default;";
		menuWindow.style.backgroundColor = 'darkgreen';
		menuWindow.style.visibility = 'visible';
		menuWindow.style.color = 'black';
		document.body.appendChild(menuWindow);
		var titleBar = document.createElement('div');
		titleBar.style.cssText = "position:absolute;top:0%;left:0%;height:20px;width:100%;";
		titleBar.style.backgroundColor = 'white';
		titleBar.style.color = 'black';
		var contentWindow = document.createElement('div');
		contentWindow.style.cssText = "position:absolute;top:20px;left:0%;height:calc(100% - 20px);width:100%;";
		contentWindow.style.backgroundColor = 'lightgray';
		contentWindow.style.color = 'black';
		contentWindow.style.overflowY = 'auto';
		contentWindow.style.overflowX = 'hidden';
		menuWindow.appendChild(titleBar);
		menuWindow.appendChild(contentWindow);
		MakeDraggable(menuWindow, titleBar);
	}
	menuWindow.onclick = function() {};
	var titleBar = menuWindow.getElementsByTagName('div')[0];
	var contentWindow = menuWindow.getElementsByTagName('div')[1];
	contentWindow.innerHTML = '';
	menuWindow.style.top = 'calc(' + (((100-h)/2)+'%') + ' - '+inputAreaHeight+'px);';
	menuWindow.style.left = ((100-w)/2)+'%';
	menuWindow.style.height = h+'%';
	menuWindow.style.width = w+'%';
	menuWindow.style.visibility = 'visible';
	titleBar.innerHTML = '<FONT COLOR=BLACK>'+heading+'</FONT>'+
		'<IMG alt="Close" style="float: right; width: 16px; height: 16px;"  tabindex="0" '
		+'ONCLICK="hideOptionWindow();" '
		+'onkeydown=\"if(event.key === \'Enter\' || event.key === \' \') { event.preventDefault(); this.click(); }\" '
		+'SRC="images/close.gif">';
	setTimeout(optionWindowFocus,250);
	return contentWindow;
}

function isDialogOpen()
{
	if(menuWindow != null)
	{
		if(menuWindow.style.visibility == 'visible')
			return true;
	}
	return false;
}

function menuAbout()
{
	var content = getOptionWindow("About",60,40);
	populateDivFromUrl(content, 'dialogs/about.htm');
	this.menuWindow.onclick = hideOptionWindow;
}

function menuWindows()
{
	var content = getOptionWindow("Window Options",60,40);
	populateDivFromUrl(content, 'dialogs/window.htm');
}

function menuMedia()
{
	var content = getOptionWindow("Custom Media",60,40);
	populateDivFromUrl(content, 'dialogs/media.htm');
}

function menuDisconnect()
{
	if(window.currWin != null)
	{
		window.currWin.closeSocket();
	}
}

function menuReconnect()
{
	if(window.currWin != null)
	{
		window.currWin.closeSocket();
		window.currWin.reset();
		window.currWin.connect(window.currWin.url);
	}
}

function menuConnect()
{
	var content = getOptionWindow("Connect",60,40);
	if(window.isElectron)
		populateDivFromUrl(content, 'dialogs/connecta.htm');
	else
		populateDivFromUrl(content, 'dialogs/connect.htm');
}

function menuTriggers(value)
{
	var which = 'Global';
	if((value != 'global')&&(window.currWin != null)&&(window.currWin.pb))
		which = window.currWin.pb.name;
	var content = getOptionWindow(which + " Triggers",60,40);
	content.which = value;
	content.triggers = getConfig('/global/triggers', window.defTriggers); // dont parse them
	if((value != 'global')&&(window.currWin != null)&&(window.currWin.pb))
		content.triggers = window.currWin.localTriggers();
	populateDivFromUrl(content, 'dialogs/triggers.htm');
}

function menuAliases(value)
{
	var which = 'Global';
	if((value != 'global')&&(window.currWin != null)&&(window.currWin.pb))
		which = window.currWin.pb.name;
	var content = getOptionWindow(which + " Aliases",60,40);
	content.which = value;
	content.aliases = getConfig('/global/aliases', window.defAliases); // dont parse them
	if((value != 'global')&&(window.currWin != null)&&(window.currWin.pb))
		content.aliases = window.currWin.localAliases();
	populateDivFromUrl(content, 'dialogs/aliases.htm');
}

function menuScripts(value)
{
	var which = 'Global';
	if((value != 'global')&&(window.currWin != null)&&(window.currWin.pb))
		which = window.currWin.pb.name;
	var content = getOptionWindow(which + " Scripts",60,40);
	content.which = value;
	content.scripts = getConfig('/global/scripts', window.defScripts); // dont parse them
	if((value != 'global')&&(window.currWin != null)&&(window.currWin.pb))
		content.scripts = window.currWin.localScripts();
	populateDivFromUrl(content, 'dialogs/scripts.htm');
}

function menuPlugins(value, page)
{
	var which = 'Global';
	if((value != 'global')&&(window.currWin != null)&&(window.currWin.pb))
		which = window.currWin.pb.name;
	var content = getOptionWindow(which + " Plugins",60,45);
	content.which = value;
	content.plugins = getConfig('/global/plugins', window.defPlugins); // dont parse them
	if((value != 'global')&&(window.currWin != null)&&(window.currWin.pb))
	{
		if(!window.currWin.pb.plugins)
			window.currWin.pb.plugins = [];
		content.plugins = JSON.parse(JSON.stringify(window.currWin.pb.plugins));
	}
	if(page === undefined)
		populateDivFromUrl(content, 'dialogs/plugins.htm');
	else
		populateDivFromUrl(content, page);
}

function menuTimers(value)
{
	var which = 'Global';
	if((value != 'global')&&(window.currWin != null)&&(window.currWin.pb))
		which = window.currWin.pb.name;
	var content = getOptionWindow(which + " Timers",60,40);
	content.which = value;
	content.timers = getConfig('/global/timers', window.defTimers); // dont parse them
	if((value != 'global')&&(window.currWin != null)&&(window.currWin.pb))
		content.timers = window.currWin.localTimers();
	populateDivFromUrl(content, 'dialogs/timers.htm');
}

function menuEntities(value)
{
	var which = 'Global';
	if((value != 'global')&&(window.currWin != null)&&(window.currWin.pb))
		which = window.currWin.pb.name;
	var content = getOptionWindow(which + " Entities (Variables)",60,40);
	content.which = value;
	content.entities = getConfig('/global/entities', window.defEntities); // dont parse them
	if((value != 'global')&&(window.currWin != null)&&(window.currWin.pb))
		content.entities = window.currWin.mxp.entities;
	content.mxp = getConfig('/global/elements', '');
	if((value != 'global')&&(window.currWin != null)&&(window.currWin.pb))
		content.mxp = window.currWin.pb.elements?window.currWin.pb.elements:'';
	populateDivFromUrl(content, 'dialogs/entities.htm');
}

function menuCapture()
{
	var content = getOptionWindow("Capture Log",60,40);
	content.currWin = window.currWin;
	if(content.currWin)
		populateDivFromUrl(content, 'dialogs/capture.htm');
}

function menuHelp(f)
{
	var addBack = '';
	if((window.menuWindow != null)
	&&(window.menuWindow.style.visibility == 'visible'))
	{
		var titleBar = menuWindow.getElementsByTagName('div')[0];
		var x = titleBar.innerHTML.indexOf('Help ');
		if(x > 0)
		{
			var y = titleBar.innerHTML.indexOf("<",x+1);
			if(y>x)
			{
				addBack = titleBar.innerHTML.substr(x+5,y-(x+5))+'/';
				while(addBack.endsWith(f+'/'))
					addBack = addBack.substr(0,addBack.length-(f.length+1));
			}
		}
	}
	if((f == '<')&&(addBack != ''))
	{
		var parts = addBack.substr(0,addBack.length-1).split('/');
		if(parts.length > 1)
		{
			f=parts[parts.length-2];
			parts.splice(parts.length-2,2);
			addBack=parts.length>0?(parts.join('/')+'/'):'';
		}
		else
		{
			f=parts[0];
			addBack='';
		}
	}
	var content = getOptionWindow("Help "+addBack+f,60,40);
	f = 'help_' + f.toLowerCase() + '.htm';
	populateDivFromUrl(content, 'help/'+f,function()
	{
		content.lastElementChild.style.cssText = 
			"background-color:black;"
			+"position:absolute;"
			+"color:white;"
			+"font-size:14;"
			+"overflowX:auto;"
			+"overflowY:auto;"
			+"min-height:100%;"
			+"width:100%;"
			+"height: auto;";
		if(addBack != '')
		{
			var bb = document.createElement('img');
			bb.onclick = function(e) { menuHelp('<'); };
			bb.src="images/docback.gif";
			bb.align="right";
			content.lastElementChild.insertBefore(bb,content.lastElementChild.firstChild);
		}
	});
}

function MakeDraggable(div, titlebar) 
{
	var dragWidget = (titlebar === undefined)?div:titlebar;
	dragWidget.style.cursor = 'move';

	let isDragging = false;
	let resizeClick = false;
	let isResizing = false;
	let startX, startY, initialLeft, initialTop, initialWidth, initialHeight;
	var moveThreshold = 5;
	
	function isDraggable(e)
	{
		return (e.target === div)
				||((!e.target.onclick) 
					&&(!e.target.onchange) 
					&&(!e.target.oninput)
					&&(getComputedStyle(e.target).pointerEvents !== 'none')
					&&(!['input', 'select', 'textarea', 'button', 'a'].includes(e.target.tagName.toLowerCase())));
		
	}
	
	function onMouseDown(e, chkResize)
	{
		if(!isDraggable(e))
			return;
		if((e.target === div)||(!e.target.onclick))
		{
			if((!window.currWin)||(!window.currWin.topWindow)) 
				return;
			var style = getComputedStyle(div);
			e.preventDefault();
			startX = e.clientX;
			startY = e.clientY;
			initialLeft = parseFloat(style.left) || 0;
			initialTop = parseFloat(style.top) || 0;
			initialWidth = parseFloat(style.width) || 0;
			initialHeight = parseFloat(style.height) || 0;
			resizeClick = false;
			if((startX > initialLeft + initialWidth - 20)
			&&(startY > initialTop + initialHeight - 20))
			{
				resizeClick = true;
				isResizing = true;
				document.addEventListener('mousemove', onMouseMove);
				document.addEventListener('mouseup', onMouseUp);
			}
			else
			if(!chkResize)
			{
				isDragging = true;
				document.addEventListener('mousemove', onMouseMove);
				document.addEventListener('mouseup', onMouseUp);
			}
		}
	}

	function onMouseMove(e)
	{
		if ((!window.currWin)||(!window.currWin.topWindow))
			return;
		var dx = e.clientX - startX;
		var dy = e.clientY - startY;
		if((!isDragging) 
		&&(!isResizing)
		&&((Math.abs(dx) > moveThreshold) 
			||(Math.abs(dy) > moveThreshold)))
		{
			if(resizeClick)
				isResizing = true;
			else
				isDragging = true;
		}
		if(isDragging)
		{
			var rect = window.currWin.topWindow.getBoundingClientRect();
			var width = div.offsetWidth;
			var height = div.offsetHeight;
			var newX = Math.max(0, Math.min(rect.width - width, initialLeft + dx));
			var newY = Math.max(0, Math.min(rect.height - height, initialTop + dy));
			div.style.left = `${newX}px`;
			div.style.top = `${newY}px`;
		}
		else
		if(isResizing)
		{
			var rect = window.currWin.topWindow.getBoundingClientRect();
			var width = div.offsetWidth;
			var height = div.offsetHeight;
			var newWidth = initialWidth + dx;
			var newHeight = initialHeight + dy;
			div.style.width = `${newWidth}px`;
			div.style.height = `${newHeight}px`;
			div.dispatchEvent(new Event('resize'));
		}
	}

	function onMouseUp()
	{
		document.removeEventListener('mousemove', onMouseMove);
		document.removeEventListener('mouseup', onMouseUp);
		isDragging = false;
		resizeClick = false;
		isResizing = false;
		div.style.cursor='default';
	}
	
	div.addEventListener('mousemove', function(e) 
	{
		if ((!window.currWin)||(!window.currWin.topWindow))
			return;
		if((!isDragging)&&(!isResizing)) 
		{
			if(!isDraggable(e))
				return;
			var style = getComputedStyle(div);
			var left = parseFloat(style.left) || 0;
			var top = parseFloat(style.top) || 0;
			var width = parseFloat(style.width) || 0;
			var height = parseFloat(style.height) || 0;
			if((e.clientX > left + width - 20)
			&&(e.clientY > top + height - 20))
				div.style.cursor = 'se-resize';
			else
				div.style.cursor = 'default';
		}
	});
	dragWidget.addEventListener('mousedown', onMouseDown);
	if(dragWidget != div)
		div.addEventListener('mousedown', function(e) { onMouseDown(e,true);});
}

