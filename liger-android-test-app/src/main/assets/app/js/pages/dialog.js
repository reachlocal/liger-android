PAGE.dialog = function(){
    DIALOG.initialize();
    return true;
}

PAGE.closeDialogArguments = function(args){
	$('#args').append(JSON.stringify(args));
}

PAGE.childUpdates = function(args){
	$('#args').append(JSON.stringify(args));
}

var DIALOG = {
	
	initialize: function(){
		var me = this;

        me.addBindings();
		
		$('#inits').append('*');
		$('#args').append(JSON.stringify(PAGE.args));
	},

	addBindings: function(){
		$("#openPage, #closePage, #closeToPage, #updateParent, #updateParentPage, #closeDialog, #closeDialogReset").unbind();
        $("#openPage").click(function(){
			PAGE.openPage('Page', 'pages', {'test1': 'test2'});
			return false;
        });

		$("#closePage").click(function(){
			PAGE.closePage();
			return false;
		});
		
		
		$("#updateParent").click(function(){
			PAGE.updateParent({'child1': 'child2'});
			return false;
		});
		
		$("#updateParentPage").click(function(){
			PAGE.updateParentPage('pages', {'child3': 'child4'});
			return false;
		});

		
		$("#closeDialog").click(function(){
			PAGE.closeDialog({'dialog': ['closed', 'arguments', 'array']});
			return false;
		});
		
		$("#closeDialogReset").click(function(){
			PAGE.closeDialog({"resetApp": true});
			return false;
		});
		
	}
}
