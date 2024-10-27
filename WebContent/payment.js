

function getCartTotal(callback) {
    $.ajax({
        url: 'api/cart',
        method: 'GET',
        success: function(cart) {
            let totalPrice = 0;
            // Loop through each item in the cart and calculate the total price
            cart.forEach(item => {
                totalPrice += item.price * item.quantity;
            });
            callback(totalPrice);
        },
        error: function(xhr, status, error) {
            console.error('Error fetching cart:', error);
            callback(0); // Set total to 0 if there's an error
        }
    });
}

// Update the displayed total
function updateCartTotal() {
    getCartTotal(function(total) {
        document.getElementById('cart-total').textContent = total.toFixed(2);
    });
}

// Handle form submission
function handleSubmit(event) {
    event.preventDefault();

    const submitButton = document.querySelector('.place-order-btn');
    submitButton.disabled = true;
    submitButton.textContent = 'Processing...';

    const formData = {
        firstName: document.getElementById('firstName').value.trim(),
        lastName: document.getElementById('lastName').value.trim(),
        creditCard: document.getElementById('creditCard').value,
        expirationMonth: document.getElementById('expirationMonth').value,
        expirationYear: document.getElementById('expirationYear').value
    };

    $.ajax({
        method: "POST",
        url: "api/payment",
        data: formData,
        success: function(response) {
            // need to add with queries
            window.location.href = 'confirmation.html';
        },
        error: function(xhr, status, error) {
            alert(xhr.responseJSON.message || 'Payment failed. Please check your information.');
            submitButton.disabled = false;
            submitButton.textContent = 'Place Order';
        }
    });
}

// Initialize the page
document.addEventListener('DOMContentLoaded', function() {
    updateCartTotal();

    document.getElementById('payment-form').addEventListener('submit', handleSubmit);
});