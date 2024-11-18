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
});