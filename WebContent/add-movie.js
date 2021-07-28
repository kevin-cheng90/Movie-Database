let add_form = $("#add-form");

/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */
function handleSubmitResult(resultDataString) {
  let resultDataJson = JSON.parse(resultDataString);

  console.log("handle movie response");

  $("#result_message").text(resultDataJson["message"]);
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitNewStar(formSubmitEvent) {
  console.log("submit add form");
  /**
   * When users click the submit button, the browser will not direct
   * users to the url defined in HTML form. Instead, it will call this
   * event handler when the event is triggered.
   */
  formSubmitEvent.preventDefault();

  $.ajax("api/add-movie", {
    method: "POST",
    // Serialize the login form to the data sent by POST request
    data: add_form.serialize(),
    success: handleSubmitResult,
    error: handleSubmitResult,
  });
}

// Bind the submit action of the form to a handler function
add_form.submit(submitNewStar);
