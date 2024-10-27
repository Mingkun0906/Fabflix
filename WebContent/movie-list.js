/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */

function saveMovieListState() {
    const state = {
        searchParams: urlParams.toString(),
        page: urlParams.get('page') || 1, // assuming there's a 'page' parameter
    };
    sessionStorage.setItem('movieListState', JSON.stringify(state));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleStarResult(resultData) {
    console.log("handleMovieResult: populating Movie table from resultData");
    totalItems = resultData[0].totalResults;
    updatePaginationInfo();

    let movieTableBodyElement = jQuery("#movie_table_body");
    movieTableBodyElement.empty();
    console.log(resultData.length)
    console.log(resultData)
    for (let i = 1; i < resultData.length; i++) {
        let rowHTML = "<tr>";
        rowHTML += '<td><a href="single-movie.html?id=' + resultData[i]['movie_id'] + '" onclick="saveMovieListState()">'
            + resultData[i]["movie_title"] +
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
                rowHTML += '<a href="single-star.html?id=' + encodeURIComponent(starId) + '" onclick="saveMovieListState()">' + starName + '</a>';
            }
        }
        rowHTML += "</td>";

        rowHTML += "<td>" + resultData[i]["movie_rating"] + "</td>";
        rowHTML += "<td>$" + resultData[i]["price"] + "</td>"; // Display price
        rowHTML += `<td><button class="btn btn-sm btn-primary add-to-cart" 
                      data-id="${resultData[i]['movie_id']}" 
                      data-title="${resultData[i]['movie_title']}" 
                      data-price="${resultData[i]['price']}">Add to Cart</button></td>`;
        rowHTML += "</tr>";

        movieTableBodyElement.append(rowHTML);
    }

    // Add click handler for "Add to Cart" buttons
    $('.add-to-cart').on('click', function() {
        const movieId = $(this).data('id');
        const title = $(this).data('title');
        const price = $(this).data('price');
        const quantity = 1; // Set the quantity to 1 for initial addition

        addToCart(movieId, title, price, quantity);
    });
}

function addToCart(movieId, title, price, quantity) {
    // Send an AJAX request to add the movie to the cart
    $.ajax({
        url: 'api/cart',
        method: 'POST',
        data: {
            id: movieId,
            title: title,
            price: price,
            quantity: quantity
        },
        success: function(response) {
            alert('Movie added to cart!');
        },
        error: function() {
            alert('Failed to add movie to cart!');
        }
    });
}

let currentPage = 1;
let itemsPerPage = 10;
let totalItems = 0;


function updatePaginationInfo() {
    const startIndex = (currentPage - 1) * itemsPerPage + 1;
    const endIndex = Math.min(startIndex + itemsPerPage - 1, totalItems);

//const urlParams = new URLSearchParams(window.location.search);


    $('#start-index').text(startIndex);
    $('#end-index').text(endIndex);
    $('#total-items').text(totalItems);

    $('#prev-button').prop('disabled', currentPage === 1);
    $('#next-button').prop('disabled', endIndex >= totalItems);

    const urlParams = new URLSearchParams(window.location.search);
    urlParams.set('page', currentPage);
    urlParams.set('limit', itemsPerPage);
    window.history.replaceState({}, '', `${window.location.pathname}?${urlParams.toString()}`);
}

function fetchMovies() {
    const urlParams = new URLSearchParams(window.location.search);

    urlParams.set('page', currentPage);
    urlParams.set('limit', itemsPerPage);

    const sortParam = urlParams.get('sort');
    if (sortParam) {
        urlParams.set('sort', sortParam);
    }
    console.log('send results to ', "api/movies?" + urlParams.toString());

    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: "api/movies?" + urlParams.toString(),
        success: (resultData) => handleStarResult(resultData)
    });
}

$(document).ready(function() {
    updateHeaderWithGenre();
    const urlParams = new URLSearchParams(window.location.search);
    currentPage = parseInt(urlParams.get('page')) || 1;
    itemsPerPage = parseInt(urlParams.get('limit')) || 10;

    $('#items-select').val(itemsPerPage);

    $('#items-select').change(function() {
        itemsPerPage = parseInt($(this).val());
        currentPage = 1;
        fetchMovies();
    });

    $('#prev-button').click(function() {
        if (currentPage > 1) {
            currentPage--;
            fetchMovies();
        }
    });

    $('#next-button').click(function() {
        if ((currentPage * itemsPerPage) < totalItems) {
            currentPage++;
            fetchMovies();
        }
    });

    fetchMovies();
});

jQuery(document).ready(function() {
    // Add click handlers
    jQuery("th.sortable-header").click(function() {
        const column = jQuery(this).text().trim().toLowerCase();
        handleSortClick(column);
    });

    updateSortVisuals();
});

function handleSortClick(clickedColumn) {
    const urlParams = new URLSearchParams(window.location.search);
    let sortParam = urlParams.get('sort') || 'rating,desc,title,asc';
    let [field1, order1, field2, order2] = sortParam.split(',');

    if (clickedColumn === field1) {
        order1 = order1 === 'asc' ? 'desc' : 'asc';
    } else {
        [field1, field2] = [clickedColumn, field1];
        [order1, order2] = ['asc', order1];
    }

    urlParams.set('sort', `${field1},${order1},${field2},${order2}`);
    urlParams.set('page', currentPage);
    urlParams.set('limit', itemsPerPage);
    window.location.search = urlParams.toString();
}

function updateSortVisuals() {
    // Clear all sort classes first
    jQuery('th.sortable-header').removeClass('sort-primary sort-secondary sort-asc sort-desc');

    const sortParam = new URLSearchParams(window.location.search).get('sort') || 'rating,desc,title,asc';
    const [field1, order1, field2, order2] = sortParam.split(',');

    // Update primary sort column
    const primaryHeader = jQuery(`th.sortable-header:contains(${capitalize(field1)})`);
    primaryHeader.addClass(`sort-primary sort-${order1}`);

    // Update secondary sort column
    const secondaryHeader = jQuery(`th.sortable-header:contains(${capitalize(field2)})`);
    secondaryHeader.addClass(`sort-secondary sort-${order2}`);
}

function capitalize(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
}

function updateHeaderWithGenre() {
    const genre = getParameterByName('genre');
    const title_start = getParameterByName('title_start');
    const star = getParameterByName('star');
    const title = getParameterByName('title');
    const year = getParameterByName('year');
    const director = getParameterByName('director');
    if (genre) {
        jQuery("h1").text(`Genre: ${genre}`);
    }
    else if (title_start)
    {
        jQuery("h1").text(`Movie title start at: ${title_start}`);
    }
    else if (star || title || year || director)
    {
        jQuery("h1").text(`Search result:`);
    }
    else
    {
        jQuery("h1").text("Movie List");
    }
}

function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to URL encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Use regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

let apiUrl = "api/movies";
const urlParams = new URLSearchParams(window.location.search);
if (!urlParams.has('sort')) {
    urlParams.set('sort', 'rating,desc,title,asc');
}
apiUrl += "?" + urlParams.toString();


jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: apiUrl,
    success: (resultData) => handleStarResult(resultData)
});

