//给发布按钮绑定一个事件
$("#publishBtn").click(function () {
	//当点击发布按钮，立刻隐藏发帖框
	$("#publishModal").modal("hide");

	//获取输入的标题和内容
	var title = $("#recipient-name").val();
	var content = $("#message-text").val();
	//发送异步请求
	$.ajax({
		url:CONTEXT_PATH + "/discuss/add",
		dataType:"json",
		async:false,
		data:{"title":title,"content":content},
		type:"POST",
		success:function(data) {
			if (data.code == 0) {
				console.log(data);
				console.log(data.map);
				console.log(data.code);
				console.log(data.msg);
				//在提示框中显示返回消息
				$("#hintBody").text(data.msg);
				//显示提示框
				$("#hintModal").modal("show");
				//过两秒后就把提示框隐藏
				setTimeout(function () {
					$("#hintModal").modal("hide");
					//刷新页面
					window.location.reload();
				}, 2000);
			} else {
				$("#hintBody").text(data.msg);
				//显示提示框
				$("#hintModal").modal("show");
				//两秒后隐藏提示框，并显示发帖框
				setTimeout(function () {
					$("#hintModal").modal("hide");
					$("#publishModal").modal("show");
				}, 2000);
			}
		},
		error:function () {
			alert("异常");
		}
	})

})