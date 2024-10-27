$(document).ready(function() {
    loadCart();

    function loadCart() {
        $.ajax({
            url: 'api/cart',
            method: 'GET',
            success: function(cart) {
                displayCart(cart);
            },
            error: function(xhr, status, error) {
                console.log('Load cart error:', error);
            }
        });
    }

    function calculateCartTotal() {
        let totalPrice = 0;
        $('.quantity-input').each(function() {
            const quantity = parseInt($(this).val());
            const price = parseFloat($(this).data('price'));
            totalPrice += quantity * price;
        });
        $('#total-price').text(`Total: $${totalPrice.toFixed(2)}`);
    }

    function displayCart(cart) {
        let cartTable = $('#cart-items');
        cartTable.empty();
        let totalPrice = 0;

        cart.forEach(item => {
            const totalItemPrice = item.price * item.quantity;
            totalPrice += totalItemPrice;

            // Use item.movie_title to display the title
            cartTable.append(`
            <tr>
                <td>${item.movie_title}</td> <!-- Display the movie title -->
                <td>
                    <input type="number" class="form-control quantity-input" 
                           value="${item.quantity}" 
                           data-id="${item.movie_id}" 
                           data-price="${item.price}"
                           data-title="${item.movie_title}">
                </td>
                <td>$${item.price}</td>
                <td class="total-item-price">$${totalItemPrice.toFixed(2)}</td>
                <td>
                    <button class="btn btn-sm btn-danger remove" data-id="${item.movie_id}">x</button>
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
        const price = parseFloat($(this).data('price'));
        const title = $(this).data('title');

        if (newQuantity > 0) {
            // Update the cart in the backend with the new quantity
            $.ajax({
                url: 'api/cart',
                method: 'POST',
                data: {
                    id: movieId,
                    title: title,
                    price: price,
                    quantity: newQuantity // Send the new quantity directly
                },
                success: function(cart) {
                    displayCart(cart); // Refresh cart display
                },
                error: function(xhr, status, error) {
                    console.log('Update cart error:', error);
                    loadCart();  // Reload the cart from server
                }
            });
        } else {
            alert('Quantity must be at least 1');
            $(this).val(1);
            calculateCartTotal();
        }
    });

    // Remove item
    $('#cart-items').on('click', '.remove', function() {
        const movieId = $(this).data('id');
        const title = $(this).closest('tr').find('.quantity-input').data('title');
        const price = $(this).closest('tr').find('.quantity-input').data('price');

        $.ajax({
            url: 'api/cart',
            method: 'POST',
            data: {
                id: movieId,
                title: title, // Ensure title is sent
                price: price,
                quantity: 0  // Set to 0 to remove
            },
            success: function(cart) {
                displayCart(cart);
            },
            error: function(xhr, status, error) {
                console.log('Remove item error:', error);
                alert('Failed to remove movie from cart');
            }
        });
    });

    // Proceed to payment
    $('#proceed-to-payment').click(function() {
        window.location.href = 'payment.html';
    });
});