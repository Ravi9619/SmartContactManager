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

//first request to server to create order
const paymentStart = () => {
    console.log("payment started...");
    
    let amount = $("#payment_field").val();
    console.log(amount);

    if(amount =='' || amount == null)
    {
        alert("amount is required");
        return;
    }

    //ajax to send request to server to create order- jquery
    $.ajax({
        url:'/user/create_order',
        data:JSON.stringify({amount:amount,info:'order_request'}),
        contentType:'application/json',
        type:'POST',
        dataType:'json',
        success: function(response) {
            //invoke when success
            console.log(response);
            if(response.status == "created")
            {
                let options = {
                    key: 'rzp_test_HqqBjexmUyHVM9',
                    amount:response.amount,
                    currency:'INR',
                    name:'Contact Manager',
                    description: 'Donation',
                    image:"../image/contact.png",
                    order_id:response.id,
                    handler: function(response) {
                        console.log(response.razorpay_payment_id);
                        console.log(response.razorpay_order_id);
                        console.log(response.razorpay_signature);
                        console.log('payments successfull !!');
                        alert("Congrats ! payment successfull");
                    },
                    "prefill": {
                        "name": "", //your customer's name
                        "email": "",
                        "contact": ""
                    },
                    "notes": {
                        "address": "Ravi Solutionalist"
                    },
                    "theme": {
                        "color": "#3399cc"
                    },
                };

                let rzp = new Razorpay(options);

                rzp.on("payment.failed",function(response) {
                    console.log(response.error.code);
                    console.log(response.error.description);
                    console.log(response.error.source);
                    console.log(response.error.step);
                    console.log(response.error.reason);
                    console.log(response.error.metadata.order_id);
                    console.log(response.error.metadata.payment_id);
                    alert("Oops payment failed")
                });

                rzp.open();
            }
        },
        error:function(error) {
            //invoked when error
            console.log(error);
            alert("Something went wrong !!")

        }
    })

}