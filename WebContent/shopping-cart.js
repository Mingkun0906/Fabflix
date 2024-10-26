$(document).ready(function() {
    loadCart();

    function loadCart() {
        $.ajax({
            url: 'api/cart',
            method: 'GET',
            success: function(cart) {
                displayCart(cart);
            }
        });
    }

    function displayCart(cart) {
        let cartTable = $('#cart-items');
        cartTable.empty();
        let totalPrice = 0;

        cart.forEach(item => {
            const totalItemPrice = item.price * item.quantity;
            totalPrice += totalItemPrice;

            cartTable.append(`
                <tr>
                    <td>${item.title}</td>
                    <td>
                        <input type="number" class="form-control quantity-input" value="${item.quantity}" data-id="${item.movieId}">
                    </td>
                    <td>$${item.price}</td>
                    <td>$${totalItemPrice.toFixed(2)}</td>
                    <td>
                        <button class="btn btn-sm btn-danger remove" data-id="${item.movieId}">x</button>
                    </td>
                </tr>
            `);
        });

        $('#total-price').text(`Total: $${totalPrice.toFixed(2)}`);
    }

    // Modify quantity
    $('#cart-items').on('change', '.quantity-input', function() {
        const movieId = $(this).data('id');
        const newQuantity = parseInt($(this).val());
        updateCart(movieId, newQuantity);
    });

    // Remove item
    $('#cart-items').on('click', '.remove', function() {
        const movieId = $(this).data('id');
        updateCart(movieId, 0); // Set quantity to zero to remove
    });

    // Update cart function
    function updateCart(movieId, newQuantity) {
        $.ajax({
            url: 'api/cart',
            method: 'POST',
            data: { movieId: movieId, quantity: newQuantity },
            success: function(cart) {
                displayCart(cart);
            }
        });
    }

    // Proceed to payment
    $('#proceed-to-payment').click(function() {
        window.location.href = 'payment-page.html';
    });
});
