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

function formatStars(starsString) {
    let starsArray = starsString.split(', ');
    let starsHTML = "";

    starsArray.forEach(star => {
        let [starId, starName] = star.split('::');
        starsHTML += `<a href="single-star.html?id=${starId}">${starName}</a>, `;
    });

    // Remove last comma and return
    return starsHTML.slice(0, -2);
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    console.log("handleResult: populating movie info from resultData");

    let movieTitle = resultData[0]["movie_title"];
    document.querySelector("h1").textContent = movieTitle;
    let movieInfoElement = jQuery("#movie_info");
    const genresArray = resultData[0]["movie_genres"].split(',');
    const genresHTML = genresArray.map(genre =>
        `<a href="movie-list.html?genre=${encodeURIComponent(genre.trim())}" class="genre-link">${genre.trim()}</a>`
    ).join(', ');

    // append two html <p> created to the h3 body, which will refresh the page
    movieInfoElement.append(`
        <p><strong>Title:</strong> ${resultData[0]["movie_title"]}</p>
        <p><strong>Year Released:</strong> ${resultData[0]["movie_year"]}</p>
        <p><strong>Director:</strong> ${resultData[0]["movie_director"]}</p>
        <p><strong>Rating:</strong> ${resultData[0]["movie_rating"]}</p>
        <p><strong>Genres:</strong> ${genresHTML}</p>
        <p><strong>Stars:</strong> ${formatStars(resultData[0]["movie_stars"])}</p>
    `);

    console.log("handleResult: populating movie table from resultData");
}

document.addEventListener('DOMContentLoaded', function() {
    // Add "Back to Movie List" button functionality
    const backButton = document.getElementById('back-to-movie-list');
    backButton.addEventListener('click', function() {
        const state = JSON.parse(sessionStorage.getItem('movieListState'));
        if (state) {
            window.location.href = `movie-list.html?${state.searchParams}&page=${state.page}`;
        } else {
            window.location.href = 'movie-list.html';
        }
    });
});

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let movieId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});