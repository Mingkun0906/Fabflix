document.addEventListener('DOMContentLoaded', function() {
    const searchForm = document.getElementById('search-form');

    searchForm.addEventListener('submit', function(event) {
        event.preventDefault(); // Prevent the default form submission

        // Get the form data
        const title = document.getElementById('title').value;
        const year = document.getElementById('year').value;
        const director = document.getElementById('director').value;
        const star = document.getElementById('star').value;

        // Build the query string
        const queryParams = [];
        if (title) queryParams.push("title=" + encodeURIComponent(title));
        if (year) queryParams.push("year=" + encodeURIComponent(year));
        if (director) queryParams.push("director=" + encodeURIComponent(director));
        if (star) queryParams.push("star=" + encodeURIComponent(star));

        // Redirect to the movie list page with the search parameters
        window.location.href = "movie-list.html?" + queryParams.join("&");
    });

    fetch('/cs122b_project1_api_example_war/api/genres')
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not OK');
            }
            return response.json();
        })
        .then(genres => {
            const genreListElement = document.getElementById('genre-list');
            genreListElement.innerHTML = ''; // Clear existing genres, if any

            genres.forEach(genre => {
                const genreLink = document.createElement('a');
                genreLink.href = "movie-list.html?genre=" + encodeURIComponent(genre.name);
                genreLink.textContent = genre.name;
                genreLink.classList.add('genre-link', 'm-2'); // Add margin for spacing
                genreListElement.appendChild(genreLink);
            });
        })
        .catch(error => console.error('Error fetching genres:', error));

    // Render alphabet list for browsing by title
    const alphabet = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ*'.split('');
    const alphabetListElement = document.getElementById('alphabet-list');
    alphabet.forEach(letter => {
        const letterLink = document.createElement('a');
        letterLink.href = "movie-list.html?title_start=" + encodeURIComponent(letter);
        letterLink.textContent = letter;
        letterLink.classList.add('alphabet-link', 'm-2'); // Add margin for spacing
        alphabetListElement.appendChild(letterLink);
    });
});