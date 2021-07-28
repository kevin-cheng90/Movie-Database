let payment_form = $("#payment_form");
$(function () {
  $("#nav-placeholder").load("navbar.html");
});
/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */
function handlePaymentResponse(resultDataString) {
  let resultDataJson = JSON.parse(resultDataString);

  console.log("handle pay response");
  console.log(resultDataJson);
  console.log(resultDataJson["status"]);

  // If login succeeds, it will redirect the user to index.html
  if (resultDataJson["status"] === "success") {
    window.location.replace("confirmation.html");
  } else {
    // If login fails, the web page will display
    // error messages on <div> with id "login_error_message"
    console.log(resultDataJson["message"]);
    let message =
      `<div class="col-md-12 error form-group">` +
      `<div id="pay_error_message" class="alert-danger alert">` +
      `${resultDataJson["message"]}` +
      `</div></div>`;

    console.log("mess", message);

    $("#err").append(message);
  }
}

function handleGetResponse(res) {
  console.log("res", res);
  $(".amount").text(res);
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitPaymentForm(formSubmitEvent) {
  console.log("submit payment form");
  /**
   * When users click the submit button, the browser will not direct
   * users to the url defined in HTML form. Instead, it will call this
   * event handler when the event is triggered.
   */
  formSubmitEvent.preventDefault();

  $.ajax("api/pay", {
    method: "POST",
    // Serialize the login form to the data sent by POST request
    data: payment_form.serialize(),
    success: handlePaymentResponse,
  });
}

$.ajax("api/pay", {
  method: "GET",
  success: handleGetResponse,
});

// Bind the submit action of the form to a handler function
payment_form.submit(submitPaymentForm);
