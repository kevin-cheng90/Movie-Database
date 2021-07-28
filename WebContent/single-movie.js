/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
 */

/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
  // Get request URL
  let url = window.location.href;
  // Encode target parameter name to url encoding
  target = target.replace(/[\[\]]/g, "\\$&");

  // Ues regular expression to find matched parameter value
  let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
    results = regex.exec(url);
  if (!results) return null;
  if (!results[2]) return "";

  // Return the decoded parameter value
  return decodeURIComponent(results[2].replace(/\+/g, " "));
}

$(function () {
  $("#nav-placeholder").load("navbar.html");
});

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {
  let stars = JSON.parse(resultData["stars"]);
  let genres = resultData["genres"].slice(1, -1);

  // populate the star info h3
  // find the empty h3 body by id "star_info"
  let movieInfoElement = jQuery("#movie_info");

  // append two html <p> created to the h3 body, which will refresh the page
  movieInfoElement.append(
    `<p id="movie-title">` +
      resultData["movie_title"] +
      "</p>" +
      "<p>Released: " +
      resultData["movie_year"] +
      "</p>"
  );

  console.log("handleResult: populating movie table from resultData");

  // Populate the star table
  // Find the empty table body by id "movie_table_body"
  let movieTableBodyElement = jQuery("#movie_table_body");
  // Concatenate the html tags with resultData jsonObject to create table rows

  let rowHTML = "";
  rowHTML += "<tr>";
  rowHTML += "<th>" + resultData["movie_director"] + "</th>";
  rowHTML += "<th>" + genres + "</th>";
  rowHTML += "<th>";
  for (const star in stars) {
    if (star > 0) {
      rowHTML += ",  ";
    }
    rowHTML += `<a href="single-star.html?id=${stars[star]["starId"]}">${stars[star]["name"]}</a>`;
  }
  rowHTML += "</th>";
  rowHTML += "<th>" + resultData["movie_rating"] + "</th>";
  rowHTML += "</tr>";

  // Append the row created to the table body, which will refresh the page
  movieTableBodyElement.append(rowHTML);
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let movieId = getParameterByName("id");

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
  dataType: "json", // Setting return data type
  method: "GET", // Setting request method
  url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by StarsServlet in Stars.java
  success: (resultData) => handleResult(resultData), // Setting callback function to handle data returned successfully by the SingleStarServlet
});

$(document).ready(function () {
  $("#add").bind("click", addMovie);

  function addMovie() {
    console.log("movie itle", document.getElementById("movie-title").innerHTML);
    $.ajax("api/add-cart", {
      method: "POST",
      data: JSON.stringify({
        name: document.getElementById("movie-title").innerHTML,
        id: movieId,
      }),
      success: (resultDataString) => {
        alert("Successfully Added the Movie to the Cart.");
      },
      error: (err) => {
        alert("An error occurred, try again shortly.");
      },
    });
  }
});
