$(function () {
  $("#nav-placeholder").load("navbar.html");
});
let content = $(".content");
/**
 * Handle the items in item list
 * @param resultArray jsonObject, needs to be parsed to html
 */
function handleCartArray(resultArray) {
  let items;
  if (typeof resultArray !== "object") {
    items = JSON.parse(resultArray);
  } else {
    items = resultArray;
  }

  console.log("items", items);

  // // Populate the star table
  // // Find the empty table body by id "movie_table_body"
  let cartBodyElement = jQuery("#cart_body");
  // // Concatenate the html tags with resultData jsonObject to create table rows
  //
  if (items.length > 0) {
    $("#cart_body").empty();
    let rowHTML;
    for (const item in items) {
      rowHTML += `<tr id=${item}>`;
      rowHTML += '<th class="row-data">' + items[item]["name"] + "</th>";
      rowHTML +=
        '<th class="row-data">' +
        `<input type="number" oninput="handleCartInfo($(this))" class="quantity" value=${items[item]["quantity"]} name="quantity" min="1" max="10">` +
        "</th>";
      rowHTML += "<th>" + "$" + `${items[item]["price"]}` + "</th>";
      rowHTML +=
        '<th class="row-data">' +
        `<input class="delete btn btn-danger" onclick="handleDelete($(this))" type="button" value="Delete"/>` +
        "</th>";
    }
    rowHTML += "</tr>";
    $("#cart_body").empty();
    cartBodyElement.append(rowHTML);

    if (!document.getElementById("pay")) {
      content.append(
        `<button id="pay" onclick={window.location.replace("pay.html")} class="btn btn-primary">Proceed to payment</button>`
      );
    }
  } else {
    content.empty();
    content.append("<p>Your shopping cart is empty.</p>");
  }
}
/**
 * Submit form content with POST method
 * @param cartEvent
 */
function handleCartInfo(ev) {
  console.log(ev);
  var rowId = ev[0].parentNode.parentNode.id;
  var data = document.getElementById(rowId).querySelectorAll(".row-data");

  $.ajax("api/shopping-cart", {
    method: "PUT",
    data: JSON.stringify({
      item: `${data[0].innerHTML}`,
      quantity: `${ev[0].valueAsNumber}`,
    }),
    success: (resultDataString) => {
      let resultDataJson = JSON.parse(resultDataString);
      handleCartArray(resultDataJson);
    },
  });
}

function handleDelete(ev) {
  var rowId = ev[0].parentNode.parentNode.id;
  var data = document.getElementById(rowId).querySelectorAll(".row-data");
  $.ajax("api/shopping-cart", {
    method: "DELETE",
    data: JSON.stringify({ item: `${data[0].innerHTML}` }),
    success: (resultDataString) => {
      let resultDataJson = JSON.parse(resultDataString);
      handleCartArray(resultDataJson);
    },
  });
}

$.ajax("api/shopping-cart", {
  method: "GET",
  success: handleCartArray,
});
