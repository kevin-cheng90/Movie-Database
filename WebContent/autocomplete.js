/*
 * This Javascript code uses this library: https://github.com/devbridge/jQuery-Autocomplete
 *
 * This example implements the basic features of the autocomplete search, features that are
 *   not implemented are mostly marked as "TODO" in the codebase as a suggestion of how to implement them.
 *
 * To read this code, start from the line "$('#autocomplete').autocomplete" and follow the callback functions.
 *
 */

/*
 * This function is called by the library when it needs to lookup a query.
 *
 * The parameter query is the query string.
 * The doneCallback is a callback function provided by the library, after you get the
 *   suggestion list from AJAX, you need to call this function to let the library know.
 */

function handleLookup(query, doneCallback) {
  console.log("autocomplete initiated");

  let cachedSuggestions = JSON.parse(sessionStorage.getItem(query));
  if (cachedSuggestions) {
    console.log("Retrieving from Cache...");
    console.log("Suggestion List from Cache", cachedSuggestions);
    doneCallback({ suggestions: cachedSuggestions });
  } else {
    console.log("Retrieving from AJAX...");
    jQuery.ajax({
      method: "GET",
      // generate the request url from the query.
      // escape the query string to avoid errors caused by special characters
      url: "api/autocomplete?query=" + escape(query),
      success: function (data) {
        // pass the data, query, and doneCallback function into the success handler
        handleLookupAjaxSuccess(data, query, doneCallback);
      },
      error: function (errorData) {
        console.log("lookup ajax error", errorData);
      },
    });
  }

  // sending the HTTP GET request to the Java Servlet endpoint hero-suggestion
  // with the query data
}

/*
 * This function is used to handle the ajax success callback function.
 * It is called by our own code upon the success of the AJAX request
 *
 * data is the JSON data string you get from your Java Servlet
 *
 */
function handleLookupAjaxSuccess(data, query, doneCallback) {
  suggestionList = data.map((item) => {
    return { value: item.title, data: item.id };
  });
  console.log("Suggestion List from AJAX", suggestionList);
  sessionStorage.setItem(query, JSON.stringify(suggestionList));
  doneCallback({ suggestions: suggestionList });
}

/*
 * This function is the select suggestion handler function.
 * When a suggestion is selected, this function is called by the library.
 *
 * You can redirect to the page you want using the suggestion data.
 */
function handleSelectSuggestion(suggestion) {
  window.location.href = "single-movie.html?id=" + suggestion.data;
}

/*
 * This statement binds the autocomplete library with the input box element and
 *   sets necessary parameters of the library.
 *
 * The library documentation can be find here:
 *   https://github.com/devbridge/jQuery-Autocomplete
 *   https://www.devbridge.com/sourcery/components/jquery-autocomplete/
 *
 */

// $('#autocomplete') is to find element by the ID "autocomplete"
$("#autocomplete").autocomplete({
  // documentation of the lookup function can be found under the "Custom lookup function" section
  lookup: function (query, doneCallback) {
    handleLookup(query, doneCallback);
  },
  onSelect: function (suggestion) {
    handleSelectSuggestion(suggestion);
  },
  // set delay time
  deferRequestBy: 300,
  noCache: false,
  minChars: 3,
  // there are some other parameters that you might want to use to satisfy all the requirements
});
