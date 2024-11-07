function showMessage(message, type, alertId) {
    const alert = document.getElementById(alertId);
    alert.textContent = message;
    alert.className = `alert alert-${type === 'success' ? 'success' : 'danger'}`;
    alert.style.display = 'block';


    setTimeout(() => {
        alert.style.display = 'none';
    }, 5000);
}

document.getElementById('addMovieForm').addEventListener('submit', async (event) => {
    event.preventDefault();

    const formData = new FormData();
    formData.append('title', document.getElementById('movieTitle').value);
    formData.append('year', document.getElementById('movieYear').value);
    formData.append('director', document.getElementById('movieDirector').value);
    formData.append('star_name', document.getElementById('starName').value);
    formData.append('genre_name', document.getElementById('genreName').value);

    try {
        const response = await fetch('_dashboard/add-movie', {
            method: 'POST',
            body: formData
        });

        const data = await response.json();
        showMessage(
            data.message,
            data.status === 'success' ? 'success' : 'error',
            'movieAlert'
        );

        if (data.status === 'success') {
            document.getElementById('addMovieForm').reset();
        }
    } catch (error) {
        showMessage('Error adding movie: ' + error.message, 'error', 'movieAlert');
    }
});

document.getElementById('add-star').addEventListener('click', async (event) => {
    event.preventDefault();

    const formData = {
        name: document.getElementById('starNameOnly').value,
        birthYear: document.getElementById('birthYear').value || ""
    };

    try {
        const response = await fetch('_dashboard/add-star', {
            method: 'POST',
            body: JSON.stringify(formData)
        });
        console.log(formData);
        const data = await response.json();
        showMessage(
            data.message,
            data.status === 'success' ? 'success' : 'error',
            'starAlert'
        );

        if (data.status === 'success') {
            document.getElementById('addStarForm').reset();
        }
    } catch (error) {
        showMessage('Error adding star: ' + error.message, 'error', 'starAlert');
    }
});

document.getElementById('loadMetadata').addEventListener('click', async () => {
    try {
        const response = await fetch('_dashboard/metadata');
        const data = await response.json();

        const tableBody = document.getElementById('metadataTableBody');
        tableBody.innerHTML = '';

        data.forEach(table => {
            table.columns.forEach(column => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${table.tableName}</td>
                    <td>${column.name}</td>
                    <td>${column.type}</td>
                `;
                tableBody.appendChild(row);
            });
        });
    } catch (error) {
        showMessage('Error loading metadata: ' + error.message, 'error', 'metadataContent');
    }
});


// document.addEventListener('DOMContentLoaded', async () => {
//     try {
//         const response = await fetch('_dashboard/check-session');
//         const data = await response.json();
//
//         if (!data.authenticated) {
//             window.location.href = 'login.html';
//         }
//     } catch (error) {
//         console.error('Session check failed:', error);
//     }
// });