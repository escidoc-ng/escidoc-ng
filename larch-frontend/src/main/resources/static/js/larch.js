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
	$.ajax({
		  type: 'PUT',
		  accepts: 'text/html',
		  url: url,
		  success: function(data){
			  document.location.href = redirectUrl;
		  }
		});}

function createEntity(id, type, label, tags, parentId) {
	var tagList = null;
	if (tags != null) {
	    tagList = tags.split(',');
	    for (var i = 0; i< tagList.length;i++) {
	        tagList[i] = $.trim(tagList[i]);
	    }
	}
    var entity = {
        'id' : id,
        'type' : type,
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
    
function checkAuth(url, type, idToHide) {
    $.ajax({
        url : ctx + "/authorize" + url,
        type : type,
        data : data,
        contentType : "application/json",
        error : function() {
        	$('#' + idToHide).css('display', 'none');
        }
    });
}
    
function checkAuthCreateEntity(type, parentId, idToHide) {
    var entity = {
            'type' : type,
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