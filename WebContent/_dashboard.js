let admin_form = $("#admin-login");
$(document).ready(function () {
  $("#schema").click(getTableSchema);
});

$(function () {
  $("#star-placeholder").load("star-modal.html");
});

$(function () {
  $("#movie-placeholder").load("add-movie.html");
});

/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */
function handleLoginDashboard(resultDataString) {
  console.log("dashboard logged in");
  let resultDataJson = JSON.parse(resultDataString);
  // If login succeeds, it will redirect the user to index.html
  if (resultDataJson["status"] === "success") {
    $("#admin-login-form").attr("style", "display: none");
    $("#dashboard").attr("style", "display: inline");
  } else {
    // If login fails, the web page will display
    // error messages on <div> with id "login_error_message"
    $("#login_error_message").text(resultDataJson["message"]);
  }
}

function showSchema(schemaJSON) {
  if (schemaJSON) {
    let movieTableBodyElement = jQuery("#schema-holder");
    movieTableBodyElement.empty();
    schemaJSON.forEach((schema) => {
      let headHtml = "<div class='table table-striped'><thead><tr>";
      let bodyHtml = `<tbody><tr>`;
      schema["attributeList"].forEach((attribute) => {
        headHtml += `<th> ${attribute["field"]} </th>`;
        bodyHtml += `<th> ${attribute["type"]} </th>`;
      });
      headHtml += `</tr></thead>`;
      bodyHtml += `</tr></tbody>`;
      let finalHtml = headHtml + bodyHtml + "</div>";
      let headerHtml = `<h2>Table: ${schema["table"]}</h2>`;
      movieTableBodyElement.append(headerHtml);
      movieTableBodyElement.append($(finalHtml));
    });
  }
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function getTableSchema() {
  console.log("getting table schemma");
  $.ajax("api/schema", {
    method: "GET",
    // Serialize the login form to the data sent by POST request
    success: showSchema,
  });
}

function submitAdminForm(formSubmitEvent) {
  console.log("submit login form");
  /**
   * When users click the submit button, the browser will not direct
   * users to the url defined in HTML form. Instead, it will call this
   * event handler when the event is triggered.
   */
  formSubmitEvent.preventDefault();

  $.ajax("api/dashboard-login", {
    method: "POST",
    // Serialize the login form to the data sent by POST request
    data: admin_form.serialize(),
    success: handleLoginDashboard,
  });
}

// Bind the submit action of the form to a handler function
admin_form.submit(submitAdminForm);
