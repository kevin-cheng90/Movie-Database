let content = $(".content");
$(function () {
  $("#nav-placeholder").load("navbar.html");
});
/**
 * Handle the items in item list
 * @param resultArray jsonObject, needs to be parsed to html
 */
function handleCartArray(resultArray) {
  let items = JSON.parse(resultArray);
  console.log("items", items);
  // Populate the star table
  // Find the empty table body by id "movie_table_body"
  let confirmBodyElement = jQuery("#confirm_body");
  // // Concatenate the html tags with resultData jsonObject to create table rows
  //

  let rowHTML;
  let sum = 0;
  for (const item in items) {
    let quantity = items[item]["quantity"];
    rowHTML += `<tr>
    <td class="left strong">${items[item]["salesID"]}</td>
    <td class="left">${items[item]["title"]}</td>
    <td class="right">${items[item]["quantity"]}</td>
    <td class="right">$${quantity * 10}</td>
  </tr>`;

    sum += quantity * 10;
  }
  rowHTML += "</tr>";
  $("#confirm_body").empty();
  confirmBodyElement.append(rowHTML);
  $("#final").text(sum);
}

$.ajax("api/confirmation", {
  method: "GET",
  success: handleCartArray,
});
