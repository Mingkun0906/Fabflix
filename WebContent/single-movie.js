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
    if (!starsString) return ''; // Add null check

    let starsArray = starsString.split(', ');
    let starsHTML = "";

    starsArray.forEach(star => {
        let [starId, starName] = star.split('::');
        if (starId && starName) { // Add null check
            starsHTML += `<a href="single-star.html?id=${starId}">${starName}</a>, `;
        }
    });

    // Remove last comma and return
    return starsHTML.slice(0, -2);
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {
    console.log("handleResult: received data:", resultData);

    if (!resultData || resultData.length === 0) {
        console.error("No movie data received");
        jQuery("#movie_info").html('<p class="text-danger">Movie not found</p>');
        return;
    }

    let movieInfoElement = jQuery("#movie_info");

    try {
        // Extracting movie details
        const movieData = resultData[0];
        console.log("Processing movie data:", movieData);

        const movieTitle = movieData["movie_title"] || 'Unknown Title';
        const moviePrice = movieData["movie_price"] || 9.99;
        const genresArray = (movieData["movie_genres"] || '').split(',');
        const genresHTML = genresArray
            .filter(genre => genre.trim()) // Remove empty genres
            .map(genre =>
                `<a href="movie-list.html?genre=${encodeURIComponent(genre.trim())}" class="genre-link">${genre.trim()}</a>`
            ).join(', ');

        const starsHTML = formatStars(movieData["movie_stars"]);

        document.title = movieTitle;
        jQuery("h1").text(movieTitle);

        // Populate movie info
        movieInfoElement.html(`
            <p><strong>Title:</strong> ${movieTitle}</p>
            <p><strong>Year Released:</strong> ${movieData["movie_year"] || 'Unknown'}</p>
            <p><strong>Director:</strong> ${movieData["movie_director"] || 'Unknown'}</p>
            <p><strong>Stars:</strong> ${starsHTML || 'No stars listed'}</p>
            <p><strong>Rating:</strong> ${movieData["movie_rating"] || 'Not rated'}</p>
            <p><strong>Genres:</strong> ${genresHTML || 'No genres listed'}</p>
            <p><strong>Price:</strong> $${typeof moviePrice === 'number' ? moviePrice.toFixed(2) : '9.99'}</p>
            <button id="add-to-cart" class="btn btn-success mt-3" 
                data-id="${movieData['movie_id']}" 
                data-title="${movieTitle}" 
                data-price="${moviePrice}">
                Add to Cart
            </button>
            <button id="checkout" class="btn btn-primary mt-3 ml-2">Checkout</button>
        `);
    } catch (error) {
        console.error("Error processing movie data:", error);
        movieInfoElement.html('<p class="text-danger">Error displaying movie information</p>');
    }
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
            quantity: quantity,
            source: 'single_movie' // Set source to 'single_movie'
        },
        success: function() {
            showTemporaryMessage('Movie added to cart!');
        },
        error: function() {
            showTemporaryMessage('Failed to add movie to cart!');
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
        const savedState = JSON.parse(sessionStorage.getItem('movieListState'));
        if (savedState) {
            window.location.href = `movie-list.html?${savedState.searchParams}&page=${savedState.page}&limit=${savedState.limit}&sort=${savedState.sort}`;
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
console.log("Movie ID from URL:", movieId);

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/single-movie?id=" + movieId,
    success: (resultData) => handleResult(resultData),
    error: (xhr, status, error) => {
        console.error("AJAX Error:", status, error);
        jQuery("#movie_info").html('<p class="text-danger">Error loading movie data</p>');
    }
});