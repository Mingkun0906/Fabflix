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
});