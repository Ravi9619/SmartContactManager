console.log("js working...")

const toggleSideBar =() => {
	
    if($('.sidebar').is(":visible"))
    {
        $(".sidebar").css("display","none");
        $(".content").css("margin-left","0");
    }else {
        $(".sidebar").css("display","block");
        $(".content").css("margin-left","20%");
    }
}

const search = () => {
    let query = $("#search-input").val();
    console.log(query);

    if(query=="")
    {
        $(".search-result").hide();
        
    }else {
             
        $(".search-result").show();
        
        //sending request to server
        let url = `http://localhost:8282/search/${query}`;
        fetch(url).then((response) => {
			return response.json();
		})
		.then((data) => {
			//data
			//console.log(data);
			
			let text = `<div class='list-group>'`;
			
			data.forEach((contact) => {
					text+=`<a class='list-group-item list-group-item-action' href='/user/${contact.cid}/contact'><span>${contact.name}</span></a></li>`
					
				});
			text += `</div>`;
			
			$(".search-result").html(text);
			$(".search-result").show()
		})
    }
}