var ctx = "";
$(document).ready(function() {
  var contextPath = $('#contextPath');
  if (contextPath) {
    if (contextPath.attr('src') && contextPath.attr('src') != '') {
      ctx = contextPath.attr('src');
    }
  }
});

function submitPut(url, redirectUrl) {
    var csrf_token = $("meta[name='_csrf']").attr("content");
	$.ajax({
        xhrFields: {
            withCredentials: true
         },
         headers: {
             "X-CSRF-TOKEN" : csrf_token
         },
		  type: 'PUT',
		  accepts: 'text/html',
		  url: url,
		  success: function(data){
			  document.location.href = redirectUrl;
		  },
	      error : function(request, msg, error) {
	        throwError(request);
	      }
		});}

function createEntity(id, contentModelId, label, tags, parentId) {
	var tagList = null;
	if (tags != null) {
	    tagList = tags.split(',');
	    for (var i = 0; i< tagList.length;i++) {
	        tagList[i] = $.trim(tagList[i]);
	    }
	}
    var entity = {
        'id' : id,
        'contentModelId' : contentModelId,
        'label' : label,
        'parentId' : parentId,
        'tags' : tagList
    };
    var csrf_token = $("meta[name='_csrf']").attr("content");
    $.ajax ({
        xhrFields: {
           withCredentials: true
        },
        headers: {
            "X-CSRF-TOKEN" : csrf_token
        },
        url: ctx + "/entity",
        type: "POST",
        data: JSON.stringify(entity),
        dataType: "text",
        contentType: "application/json; charset=utf-8",
        success: function(createdId){
            document.location.href = ctx + '/entity/' + createdId;
        },
        error : function(request, msg, error) {
            throwError(request);
        }
    });
}

function deleteUser(name) {
   $.ajax ({
        xhrFields: {
           withCredentials: true
        },
        headers: {
            "X-CSRF-TOKEN" : $("meta[name='_csrf']").attr("content")
        },
        url: ctx + "/user/" + name,
        type: "DELETE",
        success: function(createdId){
           location.reload(false);
        },
        error : function(request, msg, error) {
            throwError(request);
        }
    });
}

function deleteEntity(id) {
	   $.ajax ({
	        xhrFields: {
	           withCredentials: true
	        },
	        headers: {
	            "X-CSRF-TOKEN" : $("meta[name='_csrf']").attr("content")
	        },
	        url: ctx + "/entity/" + id,
	        type: "DELETE",
	        success: function(createdId){
	        	if (ctx != '') {
		            document.location.href = ctx;
	        	} else {
		            document.location.href = "/";
	        	}
	        },
	        error : function(request, msg, error) {
	            throwError(request);
	        }
	    });
	}

function deleteBinary(entityId, name) {
	   $.ajax ({
	        xhrFields: {
	           withCredentials: true
	        },
	        headers: {
	            "X-CSRF-TOKEN" : $("meta[name='_csrf']").attr("content")
	        },
	        url: ctx + "/entity/" + entityId + "/binary/" + name,
	        type: "DELETE",
	        success: function(createdId){
		        document.location.href = ctx + "/entity/" + entityId;
	        },
	        error : function(request, msg, error) {
	            throwError(request);
	        }
	    });
	}

function deleteMetadata(entityId, name) {
	   $.ajax ({
	        xhrFields: {
	           withCredentials: true
	        },
	        headers: {
	            "X-CSRF-TOKEN" : $("meta[name='_csrf']").attr("content")
	        },
	        url: ctx + "/entity/" + entityId + "/metadata/" + name,
	        type: "DELETE",
	        success: function(createdId){
		        document.location.href = ctx + "/entity/" + entityId;
	        },
	        error : function(request, msg, error) {
	            throwError(request);
	        }
	    });
	}

function deleteBinaryMetadata(entityId, binaryName, name) {
	   $.ajax ({
	        xhrFields: {
	           withCredentials: true
	        },
	        headers: {
	            "X-CSRF-TOKEN" : $("meta[name='_csrf']").attr("content")
	        },
	        url: ctx + "/entity/" + entityId + "/binary/" + binaryName + "/metadata/" + name,
	        type: "DELETE",
	        success: function(createdId){
		        document.location.href = ctx + "/entity/" + entityId + "/binary/" + binaryName;
	        },
	        error : function(request, msg, error) {
	            throwError(request);
	        }
	    });
	}

function deleteIdentifier(entityId, type, value) {
	   $.ajax ({
	        xhrFields: {
	           withCredentials: true
	        },
	        headers: {
	            "X-CSRF-TOKEN" : $("meta[name='_csrf']").attr("content")
	        },
	        url: ctx + "/entity/" + entityId + "/identifier/" + type + "/" + value,
	        type: "DELETE",
	        success: function(createdId){
		        document.location.href = ctx + "/entity/" + entityId;
	        },
	        error : function(request, msg, error) {
	            throwError(request);
	        }
	    });
	}

function deleteRelation(entityId, predicate, object) {
	   $.ajax ({
	        xhrFields: {
	           withCredentials: true
	        },
	        headers: {
	            "X-CSRF-TOKEN" : $("meta[name='_csrf']").attr("content")
	        },
	        url: ctx + "/entity/" + entityId + "/relation/" + predicate + "/" + object,
	        type: "DELETE",
	        success: function(createdId){
		        document.location.href = ctx + "/entity/" + entityId;
	        },
	        error : function(request, msg, error) {
	            throwError(request);
	        }
	    });
	}

function createRight(username, rolename, anchorId, rolerights) {
    var csrf_token = $("meta[name='_csrf']").attr("content");
    $.ajax ({
        xhrFields: {
           withCredentials: true
        },
        headers: {
            "X-CSRF-TOKEN" : csrf_token
        },
        url: ctx + "/user/" + username + "/role/" + rolename + "/rights/" + anchorId,
        type: "POST",
        data: JSON.stringify(rolerights),
        dataType: "text",
        contentType: "application/json; charset=utf-8",
        success: function(){
            document.location.href = ctx + '/user/' + username;
        },
        error : function(request, msg, error) {
            throwError(request);
        }
    });
}

function openUser(name) {
    document.location.href = ctx + '/user/' + name;
}

function edit(td, name) {
    var currentValue = $(td).html();
    $(td).unbind('click');
    $(td).removeAttr('onclick');
    $(td).html('<input id="__edit" type="text" name="' + name + '" value="' + currentValue + '"/>');
    $(td).append('<input type="hidden" id="' + name + 'OriginalValue" value="' + currentValue + '"/>');
    $('#__edit').focus();
    $('#__edit').blur(function() {
        stopEdit(td, name);
    });
}

function stopEdit(td, name) {
    var original = $('#' + name + 'OriginalValue').val();
    var newValue = $('#__edit').val();
    $('#__edit').unbind('blur');
    if (original != newValue) {
        $('#updateButton').unbind('click');
        $('#updateButton').removeClass('hidden');
        $('#updateButton').click(function () {
            patchEntity();
        });
        $('#cancelUpdateButton').unbind('click');
        $('#cancelUpdateButton').removeClass('hidden');
        $('#cancelUpdateButton').click(function() {
            document.location.href = document.location.href;
        });
        $(td).addClass('changed');
    }
    $(td).html(newValue);
    $(td).click(function() {
        edit(td, name);
    });
    patch[name] = newValue;
}

function patchEntity() {
    $.ajax({
        url : ctx + "/entity/" + $('#entityId').html(),
        type : "PATCH",
        data : JSON.stringify(patch),
        contentType : "application/json",
        success : function() {
            document.location.href = document.location.href;
        },
        error : function(request, msg, error) {
            throwError(request);
        }
    });
}
    
function searchEntities(all, contentModelId, id, label, parent, level1, level2, tags, state, version) {
	var query = "";
	query = appendToQuery("_all", all, query);
	query = appendToQuery("contentModelId", contentModelId, query);
	query = appendToQuery("id", id, query);
	query = appendToQuery("label", label, query);
	query = appendToQuery("parentId", parent, query);
	query = appendToQuery("level1", level1, query);
	query = appendToQuery("level2", level2, query);
	query = appendToQuery("tags", tags, query);
	query = appendToQuery("state", state, query);
	query = appendToQuery("version", version, query);
	query = encodeURIComponent(query);
	document.location.href = ctx + "/search?query=" + query;
}

function searchUsers(all, name, firstname, lastname, email) {
	var query = "";
	query = appendToQuery("_all", all, query);
	query = appendToQuery("name", name, query);
	query = appendToQuery("firstName", firstname, query);
	query = appendToQuery("lastName", lastname, query);
	query = appendToQuery("email", email, query);
	query = encodeURIComponent(query);
	document.location.href = ctx + "/search/users?query=" + query;
}

function appendToQuery(name, value, query) {
	if (value != null && value != "") {
		if (query.length > 0) {
			query += " AND ";
		}
		var parts = value.trim().split(" ");
		if (parts.length > 1) {
			var sub = "";
			sub += "(";
			for (var i = 0; i < parts.length; i++) {
			    if (sub.length > 1) {
			    	sub += " OR ";
			    }
			    sub += name + ":" + parts[i];
			}
			sub += ")";
			query += sub;
		} else {
			query += name + ":" + value;
		}
	}
	return query;
}
    
function loadRights(rolename) {
    $.ajax({
        url : ctx + "/role/" + rolename + "/rights",
        type : "GET",
        contentType : "application/json",
        success : function(json) {
        	$("#ceRoleRights").empty();
        	$('#ceRoleRights').attr('size', json.length)
           $.each(json, function(idx, right){
        	   $("#ceRoleRights").prepend('<option label="' + right + '">' + right + '</option>');
           });
        },
        error : function(request, msg, error) {
            throwError(request);
        }
    });
}
    
function checkAuth(url, type, idsToHide) {
    var csrf_token = $("meta[name='_csrf']").attr("content");
    $.ajax({
        xhrFields: {
            withCredentials: true
         },
         headers: {
             "X-CSRF-TOKEN" : csrf_token
         },
        url : ctx + "/authorize" + url,
        type : type,
        data : "",
        contentType : "application/json",
        error : function() {
        	for	(i = 0; i < idsToHide.length; i++) {
            	$('#' + idsToHide[i]).css('display', 'none');
        	} 
        }
    });
}
    
function checkAuthCreateEntity(contentModelId, parentId, idToHide) {
    var entity = {
            'contentModelId' : contentModelId,
            'label' : 'authtest',
            'parentId' : parentId
        };
    var csrf_token = $("meta[name='_csrf']").attr("content");
    $.ajax ({
        xhrFields: {
           withCredentials: true
        },
        headers: {
            "X-CSRF-TOKEN" : csrf_token
        },
        url: ctx + "/authorize/entity",
        type: "POST",
        data: JSON.stringify(entity),
        dataType: "text",
        contentType: "application/json; charset=utf-8",
        error : function() {
        	$('#' + idToHide).css('display', 'none');
        }
    });
}

function throwError(request) {
    var responseText = null;
    if (request != null && request.responseText != null && request.responseText.length > 0) {
        try {
            responseText = JSON.parse(request.responseText);
        } catch (e) {}
    }
    
    $('#errorform').attr("action", ctx + '/error-page');
    if (responseText != null) {
        if (responseText.status != null) {
        	$('#errorform').append('<input type="hidden" name="status" value="' + responseText.status + '">');
        }
        if (responseText.message != null && responseText.message.length > 0) {
        	$('#errorform').append('<input type="hidden" name="message" value="' + responseText.message + '">');
        }
        if (responseText.path != null && responseText.path.length > 0) {
        	$('#errorform').append('<input type="hidden" name="path" value="' + responseText.path + '">');
        }
        if (responseText.error != null && responseText.error.length > 0) {
        	$('#errorform').append('<input type="hidden" name="error" value="' + responseText.error + '">');
        }
        if (responseText.exception != null && responseText.exception.length > 0) {
        	$('#errorform').append('<input type="hidden" name="exception" value="' + responseText.exception + '">');
        }
        if (responseText.timestamp != null && responseText.timestamp.length > 0) {
        	$('#errorform').append('<input type="hidden" name="timestamp" value="' + responseText.timestamp + '">');
        }
    }
    $('#errorform').submit();
}