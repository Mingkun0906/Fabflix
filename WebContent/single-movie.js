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

    let movieInfoElement = jQuery("#movie_info");

    // Extracting movie details
    const movieTitle = resultData[0]["movie_title"];
    const moviePrice = resultData[0]["movie_price"];
    const genresArray = resultData[0]["movie_genres"].split(',');
    const genresHTML = genresArray.map(genre =>
        `<a href="movie-list.html?genre=${encodeURIComponent(genre.trim())}" class="genre-link">${genre.trim()}</a>`
    ).join(', ');

    document.title = movieTitle;
    jQuery("h1").text(movieTitle);

    // Populate movie info
    movieInfoElement.append(`
        <p><strong>Title:</strong> ${movieTitle}</p>
        <p><strong>Year Released:</strong> ${resultData[0]["movie_year"]}</p>
        <p><strong>Director:</strong> ${resultData[0]["movie_director"]}</p>
        <p><strong>Rating:</strong> ${resultData[0]["movie_rating"]}</p>
        <p><strong>Genres:</strong> ${genresHTML}</p>
        <p><strong>Price:</strong> $${moviePrice.toFixed(2)}</p>
        <button id="add-to-cart" class="btn btn-success mt-3" 
            data-id="${resultData[0]['movie_id']}" 
            data-title="${movieTitle}" 
            data-price="${moviePrice}">
            Add to Cart
        </button>
        <button id="checkout" class="btn btn-primary mt-3">Checkout</button>
    `);
}

$(document).on('click', '#add-to-cart', function() {
    const movieId = $(this).data('id');
    const title = $(this).data('title');
    const price = $(this).data('price');
    const quantity = 1;

    $.ajax({
        url: 'api/cart',
        method: 'POST',
        data: {
            id: movieId,
            title: title,
            price: price,
            quantity: quantity
        },
        success: function() {
            alert('Movie added to cart!');
        },
        error: function() {
            alert('Failed to add movie to cart!');
        }
    });
});

$(document).on('click', '#checkout', function() {
    window.location.href = 'shopping-cart.html';
});

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