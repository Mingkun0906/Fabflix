function formatDate(dateStr) {
    const options = {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    };
    return new Date(dateStr).toLocaleDateString('en-US', options);
}

/**
 * Calculates total amount from cart items
 * @param items Array of cart items
 * @returns {number} Total amount
 */
function calculateTotal(items) {
    return items.reduce((total, item) => {
        return total + (item.price * item.quantity);
    }, 0);
}

/**
 * Populates the order details in the confirmation page
 * @param orderData Order information from the server
 */
function populateOrderDetails(orderData) {
    $('#order-date').text(formatDate(orderData.orderDate));
    $('#order-id').text(`Sale #${orderData.orderId}`);
    $('#order-items').empty();

    orderData.items.forEach(item => {
        const subtotal = item.price * item.quantity;
        const row = `
            <tr>
                <td>${item.movie_title}</td>
                <td>${item.quantity}</td>
                <td>$${item.price.toFixed(2)}</td>
                <td>$${subtotal.toFixed(2)}</td>
            </tr>
        `;
        $('#order-items').append(row);
    });

    // Calculate and update total
    const total = calculateTotal(orderData.items);
    $('#order-total').text(total.toFixed(2));
}

// When the page loads, fetch order details
$(document).ready(() => {
    $.ajax({
        url: 'api/confirmation',
        method: 'GET',
        success: (response) => {
            populateOrderDetails(response);
        },
        error: (xhr, status, error) => {
            console.error('Error fetching order details:', error);
            alert('Error loading order details. Please try again later.');
            // Redirect to main page after error
            window.location.href = 'main.html';
        }
    });
});