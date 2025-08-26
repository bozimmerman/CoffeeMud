function ContextDelayHide()
{
	setTimeout(ContextHideAll,250);
}

function ContextDelaySubHide()
{
	setTimeout(ContextHideSub,250);
}

function ContextHideAll()
{
	ContextHideSub();
	var menu = document.getElementById('ctxmenu');
	if(menu != null)
		menu.outerHTML='';
}

function ContextHideSub()
{
	var menu = document.getElementById('ctxsubmenu');
	if(menu != null)
		menu.outerHTML='';
}

function IsContextHover(e, menuName)
{
	if(menuName === undefined)
		return IsContextHover(e, 'ctxmenu') || IsContextHover(e, 'ctxsubmenu'); 
	var div = document.getElementById(menuName);
	if (!div) 
		return false;
	var x = e.clientX || 0;
	var y = e.clientY || 0;
	var elementAtPoint = document.elementFromPoint(x, y);
	return elementAtPoint === div || div.contains(elementAtPoint);
}

function IsContextMenuHover(e)
{
	return IsContextHover(e, 'ctxmenu');
}

function IsContextSubMenuHover(e)
{
	return IsContextHover(e, 'ctxsubmenu');
}

function ContextMenuOpen(e, menu, x, y, width, marginBottom) 
{
	if (window.event) 
		window.event.cancelBubble=true;
	else
	if (e && e.stopPropagation) 
		e.stopPropagation();
	if(e && e.preventDefault)
		e.preventDefault();
	var menuelements = BuildContextMenuEntries(menu);
	var menuDiv = CreateContextDiv('ctxmenu',x,y,width);
	menuDiv.onmouseleave = function(e) {
		if(!IsContextHover(e))
			ContextDelayHide();
	}
	menuDiv.onclick = function() {
		ContextDelayHide();
	};
	var pstyle = document.createElement('p');
	pstyle.style.cssText = 'padding: 0 1rem; margin: 0;';
	menuDiv.appendChild(pstyle);
	for(var i=0;i<menuelements.length;i++)
	{
		menuelements[i].style.display = 'block';
		if(marginBottom !== undefined)
			menuelements[i].style.marginBottom = marginBottom+'px';
		pstyle.appendChild(menuelements[i]);
	}
	return menuDiv;
}

function CreateContextDiv(id, x, y, width)
{
	var menu = document.createElement("div");
	menu.id = id;
	menu.style.cssText = "top:" + y+"px;"
					   + "left:" + x+"px;"
					   + "font-family: monospace;"
					   + "font-size: 12px;"
					   + "position: fixed;"
					   + "background: black;"
					   + "color: yellow;"
					   + "cursor: pointer;"
					   + "z-order: 999;"
					   + "z-index: 999;"
					   + "border: 1px black solid";
	if(width === 'auto')
	{
		delete menu.style.width;
		menu.style.display = 'inline-block';
		menu.style.whiteSpace = 'nowrap';
	}
	else
		menu.style.width = width+'px';
	document.body.appendChild(menu);
	return menu;
}

function ContextSubMenuOpen(e, menu, x,y,width) {
	if (window.event) 
		window.event.cancelBubble=true;
	else
	if (e && e.stopPropagation) 
		e.stopPropagation();
	if(e && e.preventDefault)
		e.preventDefault();
	ContextHideSub();
	var menuelements = BuildContextMenuEntries(menu);
	var menuDiv = CreateContextDiv('ctxsubmenu',x,y,width);
	menuDiv.onmouseleave = function(e) {
		if(!IsContextHover(e))
			ContextDelayHide();
		else
		if(!IsContextSubMenuHover(e))
			ContextDelaySubHide();
	}
	menuDiv.onclick = ContextDelayHide;
	var pstyle = document.createElement('p');
	pstyle.style.cssText = 'padding: 0 1rem; margin: 0;';
	menuDiv.appendChild(pstyle);
	for(var i=0;i<menuelements.length;i++)
	{
		pstyle.appendChild(menuelements[i]);
		pstyle.appendChild(document.createElement('br'));
	}
	return menuDiv;
}

function BuildContextMenuEntries(menuObj)
{
	var entries = [];
	for(var i=0;i<menuObj.length;i++)
	{
		var obj = menuObj[i];
		if(obj.n && obj.a)
		{
			if(('v' in obj)&&(obj['v'])&&(!eval(obj['v'])))
				continue;
			var show = (obj.e === undefined) || (!obj.e) || eval(obj.e);
			if((typeof obj.a === 'string') && (!obj.a))
				show = false;
			var entry;
			if(!show)
			{
				entry = document.createElement('a');
				entry.style.color='lightgray';
				entry.onclick=null;
				entry.style.cursor = 'default';
				entry.href='#';
				var fontColoring = document.createElement('font');
				fontColoring.color = 'lightgray';
				fontColoring.textContent = obj.n;
				entry.appendChild(fontColoring);
			}
			else
			if(typeof obj.a === 'string')
			{
				entry = document.createElement('a');
				entry.href='#';
				if(obj.a.startsWith("javascript:"))
					entry.onclick = new Function(obj.a.substr(11));
				else
					entry.onclick = (function(a,f){ 
						return function() {
							addToPrompt (a, f);};})(obj.a,obj.sf);
				entry.textContent = obj.n;
			}
			else
			if(typeof obj.a === 'function')
			{
				entry = document.createElement('a');
				entry.onclick = obj.a;
				entry.textContent = obj.n;
			}
			else { // reserve for submenus maybe?
				console.warn("Unknown menu action: "+obj.a);
				continue;
			}
			entries.push(entry);
		}
	}
	return entries;
}

function ContextHelp(obj, e,title)
{
	ContextHideAll();
	var content = ContextMenuOpen(e, [], 0, 20, 400)
	var f = 'help_' + title.toLowerCase() + '.htm';
	content.style.height = '400px';
	populateDivFromUrl(content, 'help/'+f,function(){
		content.lastElementChild.style.cssText = 
			"background-color:black;"
			+"position:absolute;"
			+"color:white;"
			+"font-size:14;"
			+"overflow-x:auto;"
			+"overflow-y:auto;"
			+"overflow:auto;"
			+"height: 400px;";
	});
}

function MXPContextMenu(obj, e, href, hint, prompt) 
{
	var menuObj = ParseMXPContextMenu(obj, href, hint, prompt);
	var menuDiv= ContextMenuOpen(e, menuObj, e.pageX-40, e.pageY-10, 200);
	menuDiv.style.border = "1px solid";
	menuDiv.style.borderColor = "white";
	menuDiv.style.left = (parseInt(menuDiv.style.left || "0") + 10) + "px";
	return menuDiv;
};

function ParseMXPContextMenu(titleSet,menu,hints,submitFlag)
{
	var mmenu=[]
	if(menu.length==0) 
		return mmenu;
	var menuBits=menu.split("|");
	var hintBits=hints.split("|");
	if(hintBits.length>menuBits.length)
	{
		if(titleSet)
			titleSet.title=hintBits[0];
		hintBits.unshift();
	}
	for(var i=0;i<menuBits.length && i<hintBits.length;i++)
	{
		var menuBit = menuBits[i];
		var hintBit = hintBits[i];
		var entry = {};
		entry.n = hintBit;
		entry.a = menuBit;
		entry.sf = submitFlag;
		mmenu.push(entry)
	}
	return mmenu;
}
