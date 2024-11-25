document.addEventListener('DOMContentLoaded', function() {
    // Regular Search Form Handler
    const searchForm = document.getElementById('search-form');
    searchForm.addEventListener('submit', function(event) {
        event.preventDefault();
        const title = document.getElementById('title').value;
        const year = document.getElementById('year').value;
        const director = document.getElementById('director').value;
        const star = document.getElementById('star').value;

        const queryParams = [];
        if (title) queryParams.push("title=" + encodeURIComponent(title));
        if (year) queryParams.push("year=" + encodeURIComponent(year));
        if (director) queryParams.push("director=" + encodeURIComponent(director));
        if (star) queryParams.push("star=" + encodeURIComponent(star));

        window.location.href = "movie-list.html?" + queryParams.join("&");
    });

    // Full-text Search Form Handler
    const fullTextSearchForm = document.getElementById('full-text-search-form');
    fullTextSearchForm.addEventListener('submit', function(event) {
        event.preventDefault();

        const searchQuery = document.getElementById('full-text-search-box').value;
        if (searchQuery.trim()) {
            // Split on any whitespace and clean the terms
            const keywords = searchQuery.trim()
                .split(/\s+/)
                .filter(word => word.length > 0)
                .map(word => word.replace(/[+*]/g, '')); // Remove any existing +* symbols

            // Create the search string with proper boolean operators
            const searchString = keywords
                .map(word => `+${word}*`)
                .join(' ');

            // Encode the entire search string
            window.location.href = "movie-list.html?fulltext=" + encodeURIComponent(searchString);
        }
    });

    // Genres fetching and rendering
    fetch('/cs122b-team-beef/api/genres')
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not OK');
            }
            return response.json();
        })
        .then(genres => {
            const genreListElement = document.getElementById('genre-list');
            genreListElement.innerHTML = '';

            genres.forEach(genre => {
                const genreLink = document.createElement('a');
                genreLink.href = "movie-list.html?genre=" + encodeURIComponent(genre.name);
                genreLink.textContent = genre.name;
                genreLink.classList.add('genre-link', 'm-2');
                genreListElement.appendChild(genreLink);
            });
        })
        .catch(error => console.error('Error fetching genres:', error));

    // Alphabet list rendering
    const alphabet = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ*'.split('');
    const alphabetListElement = document.getElementById('alphabet-list');
    alphabet.forEach(letter => {
        const letterLink = document.createElement('a');
        letterLink.href = "movie-list.html?title_start=" + encodeURIComponent(letter);
        letterLink.textContent = letter;
        letterLink.classList.add('alphabet-link', 'm-2');
        alphabetListElement.appendChild(letterLink);
    });

    // Checkout button handler
    const checkoutButton = document.getElementById('checkout-button');
    if (checkoutButton) {
        checkoutButton.addEventListener('click', function() {
            window.location.href = 'shopping-cart.html';
        });
    }

    let searchCache = {};
    let searchTimeout = null;
    let currentSelectedItem = null;

    // Prevent form submission on enter when autocomplete is active
    $('#full-text-search-form').on('submit', function(event) {
        const autocomplete = $('#full-text-search-box').data('ui-autocomplete');
        if (autocomplete && autocomplete.menu.active) {
            event.preventDefault();
            return false;
        }
    });

    $('#full-text-search-box').autocomplete({
        source: function(request, response) {
            clearTimeout(searchTimeout);

            if (request.term.length < 3) {
                console.log('Autocomplete: Query too short (< 3 characters)');
                response([]);
                return;
            }

            console.log(`Autocomplete query initiated for term: "${request.term}"`);

            const cacheKey = request.term.toLowerCase();
            if (searchCache[cacheKey]) {
                console.log(`Autocomplete: Using cached results for term: "${request.term}"`);
                response(searchCache[cacheKey]);
                console.log('Autocomplete: Suggestion list:', searchCache[cacheKey]);
                return;
            } else {
                console.log(`Autocomplete: Fetching results from the backend for term: "${request.term}"`);
            }

            searchTimeout = setTimeout(function() {
                $.ajax({
                    url: 'api/autocomplete',
                    dataType: 'json',
                    data: { query: request.term },
                    success: function(data) {
                        searchCache[cacheKey] = data;
                        console.log('Autocomplete: Received data:', data);
                        response(data);
                        console.log('Autocomplete: Suggestion list:', data);
                    },
                    error: function(xhr, status, error) {
                        console.error('Autocomplete: Error fetching results:', error);
                        response([]);
                    }
                });
            }, 300);
        },
        minLength: 3,
        select: function(event, ui) {
            console.log('Autocomplete: Select triggered with item:', ui.item);
            event.preventDefault();
            event.stopPropagation();

            if (ui.item.id) {
                window.location.href = 'single-movie.html?id=' + ui.item.id;
                return false;
            }
        },
        focus: function(event, ui) {
            console.log('Autocomplete: Focus triggered with item:', ui.item);
            event.preventDefault();
            currentSelectedItem = ui.item;
            $(this).val(ui.item.value);
            return false;
        }
    }).on('keydown', function(event) {
        const autocomplete = $(this).data('ui-autocomplete');
        const menu = autocomplete.menu;

        if (event.keyCode === $.ui.keyCode.ENTER) {
            console.log('Enter key pressed, menu active:', menu.active);

            if (menu.active) {
                event.preventDefault();
                event.stopPropagation();

                const selectedItem = menu.active.data('ui-autocomplete-item');
                console.log('Selected item:', selectedItem);

                if (selectedItem && selectedItem.id) {
                    $(this).val(selectedItem.value);
                    window.location.href = 'single-movie.html?id=' + selectedItem.id;
                    return false;
                }
            }
        }
    });

    $('#full-text-search-box').data('ui-autocomplete')._renderItem = function(ul, item) {
        return $('<li>')
            .data('ui-autocomplete-item', item)
            .append($('<div>')
                .text(item.value)
                .addClass('ui-menu-item-wrapper'))
            .appendTo(ul);
    };

});