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
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    console.log("handleResult: populating star info from resultData");

    // populate the star info h3
    // find the empty h3 body by id "star_info"
    let starInfoElement = jQuery("#star_info")

    let starName = resultData[0]["star_name"];
    document.querySelector("h1").textContent = starName;
    let starDob = resultData[0]["star_dob"];
    starDob = (starDob === null) ? "N/A" : starDob;

    starInfoElement.append(
        "<p>Star Name: " + starName + "</p>" +
        "<p>Date Of Birth: " + starDob + "</p>"
    );

    console.log("handleResult: populating movie table from resultData");

    // Populate the star table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Concatenate the html tags with resultData jsonObject to create table rows
    for (let i = 0; i < resultData.length; i++) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<td><a href='single-movie.html?id=" + resultData[i]["movie_id"] + "'>"
            + resultData[i]["movie_title"] + "</a></td>";
        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }
}

document.addEventListener('DOMContentLoaded', function() {
    // Add "Back to Movie List" button functionality
    const backButton = document.createElement('button');
    backButton.id = 'back-to-movie-list';
    backButton.textContent = 'Back to Movie List';
    backButton.classList.add('btn', 'btn-primary', 'mt-3'); // Add Bootstrap classes for styling

    document.querySelector('.container').appendChild(backButton);

    backButton.addEventListener('click', function() {
        const state = JSON.parse(sessionStorage.getItem('movieListState'));
        if (state) {
            window.location.href = `movie-list.html?${state.searchParams}&page=${state.page}`;
        } else {
            window.location.href = 'movie-list.html';
        }
    });
});

// Get id from URL
let starId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-star?id=" + starId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});