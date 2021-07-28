let searchCache = {};

$(function () {
  $("#nav-placeholder").load("navbar.html");
});

function addMovie(ev) {
  var rowId = ev.parentNode.parentNode.id;
  var data = document.getElementById(rowId).querySelectorAll(".row-data");
  $.ajax("api/add-cart", {
    method: "POST",
    data: JSON.stringify({
      name: data[rowId].innerText,
      id: data[rowId].firstElementChild.getAttribute("id"),
    }),
    success: (resultDataString) => {
      alert("Successfully Added the Movie to the Cart.");
    },
    error: (err) => {
      alert("An error occurred, try again shortly.");
    },
  });
}

function removeParameter(params, url) {
  // Removes a parameter from the url
  // ex. param = "search"  &  url = "?page=2&search=the&category=title"
  // result = "?page=2&category=title"
  let regex = new RegExp("(" + params + ".*?)[&]");
  var results;
  results = regex.exec(url);
  if (results == null) {
    // ?page=1&search=the
    var regex2 = new RegExp("[&](" + params + ".*)");
    results = regex2.exec(url);
    // ?search=the

    if (results == null) {
      regex3 = new RegExp("(" + params + ".*)");
      results = regex3.exec(url);
    }
  }
  if (results != null) {
    newstr = url.replace(results[0], "");
    return newstr;
  } else {
    return url;
  }
}

function handleMovieResult(resultData) {
  console.log("handleMovieResult: populating movie table from resultData[0]");
  let movieTableBodyElement = jQuery("#movie_table_body");
  console.log(resultData);
  for (let i = 0; i < resultData[0].length; i++) {
    // Title, Year, Director, Genres, Stars, Rating
    let rowHTML = "";
    rowHTML += "<tr>";
    // Add Title and link to single-movie page
    rowHTML +=
      "<th class='row-data'>" +
      '<a href="single-movie.html?id=' +
      resultData[0][i]["movie_id"] +
      '">' +
      resultData[0][i]["movie_title"] +
      "</a></th>";
    // Add Year
    rowHTML += "<th>" + resultData[0][i]["movie_year"] + "</th>";
    // Add Director
    rowHTML += "<th>" + resultData[0][i]["movie_director"] + "</th>";
    // Add Genres
    rowHTML += "<th>";
    for (let j = 0; j < resultData[0][i]["genres"].sort().length; j++) {
      rowHTML +=
        '<a href = "movies.html?genreBrowse=' +
        resultData[0][i]["genres"][j] +
        "&" +
        removeParameter(
          "category",
          removeParameter(
            "search",
            removeParameter(
              "pages",
              removeParameter("genreBrowse", window.location.search.substr(1))
            )
          )
        ) +
        '">' +
        resultData[0][i]["genres"][j] +
        "</a>";
      rowHTML += "<br>";
    }
    rowHTML += "</th>";
    // Add Stars
    rowHTML += "<th>";
    for (let k = 0; k < resultData[0][i]["stars"].length; k++) {
      rowHTML +=
        '<a href="single-star.html?id=' +
        resultData[0][i]["stars_id"][k] +
        window.location.search.substr(1) +
        '">';
      rowHTML += resultData[0][i]["stars"][k] + "</a>";
      rowHTML += "<br>";
    }
    rowHTML += "</th>";
    // Add Rating
    rowHTML += "<th>" + resultData[0][i]["rating"] + "</th>";
    rowHTML +=
      "<th>" +
      "<button  class='btn btn-primary' onclick='addMovie(this)'>Add to Cart</button>" +
      "</th>";

    rowHTML += "</tr>";
    movieTableBodyElement.append(rowHTML);
  }
  let page = jQuery("#pages");
  let sort = jQuery("#sorting");
  let listing = jQuery("#listing");
  let browseGenre = jQuery("#browseGenre");
  let browseAlpha = jQuery("browseAlpha");
  let pageNum = parseInt(resultData[1][0]["page"]);
  let N = resultData[0].length;
  let sortBy = resultData[1][0]["sort"];
  let orderFirst = resultData[1][0]["orderFirst"];
  let orderSecond = resultData[1][0]["orderSecond"];
  let listOptions = [10, 25, 50, 100];

  // Creating page buttons at the bottom
  if (pageNum > 1) {
    // movies.html?page= _ ?N= _ ?sort= _ ?order= _
    let urlStr =
      "&" + removeParameter("page", window.location.search.substr(1));
    page.append(
      '<li class="page-item"><a class="page-link" href="movies.html?page=' +
        String(pageNum - 1) +
        urlStr +
        '">Previous</a></li>'
    );
  } else {
    page.append(
      '<li class="page-item disabled"><a class="page-link" href="#">Previous</a></li>'
    );
  }
  if (listOptions.includes(N)) {
    let urlStr =
      "&" + removeParameter("page", window.location.search.substr(1));
    page.append(
      '<li class="page-item"><a class="page-link" href="movies.html?page=' +
        String(pageNum + 1) +
        urlStr +
        '">Next</a></li>'
    );
  }

  // Creating buttons at top to sort by and order by
  if (sortBy == "rating") {
    sort.append("Sort First: <b>Rating</b>");
  } else {
    sort.append("Sort First: <b>Title</b>");
  }

  if (orderFirst == "descending") {
    sort.append(
      ' Order by: <b>Descending</b> <a href="movies.html?' +
        removeParameter(
          "page",
          removeParameter("orderFirst", window.location.search.substr(1))
        ) +
        "&orderFirst=ascending" +
        '">Ascending</a><br>'
    );
  } else if (orderFirst == "ascending") {
    sort.append(
      ' Order by: <b>Ascending</b> <a href="movies.html?' +
        removeParameter(
          "page",
          removeParameter("orderFirst", window.location.search.substr(1))
        ) +
        "&orderFirst=descending" +
        '">Descending</a><br>'
    );
  }
  if (sortBy == "rating") {
    // Maintains the sort order. So if Title was sorted by rating, it will remain sorted as rating
    sort.append(
      'Sort Second: <a href="movies.html?' +
        removeParameter(
          "page",
          removeParameter("sort", window.location.search.substr(1))
        ) +
        "&sort=title" +
        '">Title</a>'
    );
  } else if (sortBy == "title") {
    sort.append(
      'Sort Second: <a href="movies.html?' +
        removeParameter(
          "page",
          removeParameter("sort", window.location.search.substr(1))
        ) +
        "&sort=rating" +
        '">Rating</a>'
    );
  }

  if (orderSecond == "descending") {
    sort.append(
      ' Order by: <b>Descending</b> <a href="movies.html?' +
        removeParameter(
          "page",
          removeParameter("orderSecond", window.location.search.substr(1))
        ) +
        "&orderSecond=ascending" +
        '">Ascending</a><br>'
    );
  } else if (orderSecond == "ascending") {
    sort.append(
      ' Order by: <b>Ascending</b> <a href="movies.html?' +
        removeParameter(
          "page",
          removeParameter("orderSecond", window.location.search.substr(1))
        ) +
        "&orderSecond=descending" +
        '">Descending</a><br>'
    );
  }
  // bind pressing enter key to a handler function
  $("#autocomplete").keypress(function (event) {
    // keyCode 13 is the enter key
    if (event.keyCode == 13) {
      // pass the value of the input box to the handler function
      handleSubmit();
    }
  });

  $("#submit").click(handleSubmit);

  function handleSubmit() {
    let text = $(".form1").val();
    if (/\S/.test(text)) {
      text = text.split(" ").join("+");
      console.log(text);
      let category = $("#category").val();
      window.location.replace(
        "movies.html?" +
          removeParameter(
            "genreBrowse",
            removeParameter(
              "page",
              removeParameter(
                "category",
                removeParameter("search", window.location.search.substr(1))
              )
            )
          ) +
          "&category=" +
          category +
          "&search=" +
          text
      );
    }
  }

  // append closes my tags automatically so I need a long string to append
  let listString = "";
  listString +=
    '<nav aria-label="Page listings">' +
    '<ul class="pagination justify-content-end">Items per page: ';

  //sort.append('<ul class="pagination justify-content-end">')
  for (let i = 0; i < listOptions.length; i++) {
    if (N < listOptions.length[i]) {
      listString +=
        '<li class="page-item disabled"><a class="page-link">' +
        String(listOptions[i]) +
        "</a></li>";
    } else {
      if (listOptions[i] == N) {
        listString +=
          '<li class="page-item active"><a class="page-link">' +
          String(listOptions[i]) +
          "</a></li>";
      } else {
        listString +=
          '<li class="page-item"><a class="page-link" href="' +
          "movies.html?" +
          removeParameter(
            "page",
            removeParameter("N", window.location.search.substr(1))
          ) +
          "&N=" +
          String(listOptions[i]) +
          '">' +
          String(listOptions[i]) +
          "</a></li>";
      }
    }
  }
  listString += "</ul></nav>";
  listing.append(listString);

  let browseGenreString = "";
  browseGenreString +=
    '<div class="input-group"><select name="genreString" id="genreString" onchange="' +
    "location='movies.html?genreBrowse=' + this.value + '&" +
    removeParameter(
      "category",
      removeParameter(
        "search",
        removeParameter("genreBrowse", window.location.search.substr(1))
      )
    ) +
    "'" +
    ';">';
  browseGenreString += "<option>Select Here</option>";
  for (let i = 0; i < resultData[1][0]["browseGenres"].length; i++) {
    browseGenreString +=
      '<option value="' +
      resultData[1][0]["browseGenres"][i] +
      '">' +
      '<a href="' +
      String(i) +
      '">' +
      resultData[1][0]["browseGenres"][i] +
      "</a></option>";
  }
  browseGenreString += "</select></div>";
  browseGenre.append(browseGenreString);
}

jQuery.ajax({
  dataType: "json", // Setting return data type
  method: "GET", // Setting request method
  url: "api/movies" + window.location.search, // Setting request url, which is mapped by StarsServlet in Stars.java
  success: (resultData) => handleMovieResult(resultData), // Setting callback function to handle data returned successfully by the StarsServlet
});
