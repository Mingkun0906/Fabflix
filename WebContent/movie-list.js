/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */


/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleStarResult(resultData) {
    console.log("handleMovieResult: populating Movie table from resultData");
    console.log("end");

    // Populate the star table
    // Find the empty table body by id "star_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");
    // Iterate through resultData, no more than 10 entries
    for (let i = 0; i < resultData.length; i++) {
        console.log(resultData[i]["movie_rating"])
        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "<tr>";
        rowHTML += '<td><a href="single-movie.html?id=' + resultData[i]['movie_id'] + '">'
            + resultData[i]["movie_title"] +     // Change "movie_name" to "movie_title"
            '</a></td>';
        rowHTML += "<td>" + resultData[i]["movie_year"] + "</td>";
        rowHTML += "<td>" + resultData[i]["movie_director"] + "</td>";

        rowHTML += "<td>";
        if (resultData[i]["movie_genres"]) {
            let genres = resultData[i]["movie_genres"].split(', ');
            for (let j = 0; j < genres.length; j++) {
                if (j > 0) rowHTML += ", ";
                rowHTML += genres[j];
            }
        }
        rowHTML += "</td>";


        rowHTML += "<td>";
        if (resultData[i]["stars_info"]) {
            let stars = resultData[i]["stars_info"].split(', ');
            for (let j = 0; j < stars.length; j++) {
                if (j > 0) rowHTML += ", ";
                let [starId, starName] = stars[j].split('::');
                rowHTML += '<a href="single-star.html?id=' + encodeURIComponent(starId) + '">' + starName + '</a>';
            }
        }
        rowHTML += "</td>";
        rowHTML += "<td>" + resultData[i]["movie_rating"] + "</td>";
        rowHTML += "</tr>";  // End of the row


        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }
}


const urlParams = new URLSearchParams(window.location.search);

// Construct the API URL with query parameters
let apiUrl = "api/movies";

// Check if there are any query parameters
if (urlParams.toString()) {
    apiUrl += "?" + urlParams.toString();
}

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: apiUrl, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleStarResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});