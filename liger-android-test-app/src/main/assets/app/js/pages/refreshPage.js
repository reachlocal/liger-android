console.log("refresh.js");

PAGE.refreshPage = function(){
    REFRESHPAGES.initialize();
    return true;
}

PAGE.closeDialogArguments = function(args){
	$('#args').append(JSON.stringify(args));
}

PAGE.childUpdates = function(args){
	$('#args').append(JSON.stringify(args));
}

PAGE.headerButtonTapped = function(button){
    console.log("Received Button: " + button);
	if(button === "refresh"){
	    $('#refreshes').append('*');
	}
}

var REFRESHPAGES = {
	
	initialize: function(){
		var me = this;

        me.addBindings();
		
		$('#inits').append('*');
		$('#args').append(JSON.stringify(PAGE.args));
	},

	addBindings: function(){
		$("#closePage, #closeDialog").unbind();

		$("#closePage").click(function(){
			PAGE.closePage();
			return false;
		});

		$("#closeDialog").click(function(){
			PAGE.closeDialog({'dialog': ['closed', 'arguments', 'array']});
			return false;
		});		
	}
}
