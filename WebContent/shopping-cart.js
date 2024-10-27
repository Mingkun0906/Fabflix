$(document).ready(function() {
    loadCart();
    $('#back-to-movie-list').click(function() {
        // Retrieve the saved state from sessionStorage
        const state = JSON.parse(sessionStorage.getItem('movieListState'));
        if (state && state.searchParams) {
            // Redirect to the saved movie list page with search parameters
            window.location.href = `movie-list.html?${state.searchParams}&page=${state.page}`;
        } else {
            // If no state is found, go to the main movie list page
            window.location.href = 'movie-list.html';
        }
    });

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
                <td>${item.movie_title}</td>
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

    $('#cart-items').on('change', '.quantity-input', function() {
        const movieId = $(this).data('id');
        const newQuantity = parseInt($(this).val());

        if (newQuantity > 0) {
            $.ajax({
                url: 'api/cart',
                method: 'POST',
                data: {
                    id: movieId,
                    quantity: newQuantity,
                    source: 'shopping_cart'
                },
                success: function(cart) {
                    displayCart(cart);
                },
                error: function() {
                    showTemporaryMessage('Failed to update cart!');
                }
            });
        } else {
            alert('Quantity must be at least 1');
            $(this).val(1);
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
                title: title,
                price: price,
                quantity: 0
            },
            success: function(cart) {
                displayCart(cart);
            },
            error: function(xhr, status, error) {
                console.log('Remove item error:', error);
                showTemporaryMessage('Failed to remove movie from cart');
            }
        });
    });

    // Proceed to payment
    $('#proceed-to-payment').click(function() {
        window.location.href = 'payment.html';
    });
});