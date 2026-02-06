$(document).ready(function () {
    // Update quantity
    $('.quantity').change(function () {
        let quantity = $(this).val();
        let id = $(this).attr('data-id');
        let row = $(this).closest('tr');
        let pricePerItem = parseFloat(row.find('.book-price').first().text().replace(/[^\d]/g, ''));

        $.ajax({
            url: '/cart/updateCart/' + id + '/' + quantity,
            type: 'GET',
            success: function (data) {
                if (data.success) {
                    // Update item total in this row
                    let itemTotal = pricePerItem * quantity;
                    row.find('.book-price').last().html(formatNumber(itemTotal) + 'đ');

                    // Update cart summary total
                    $('.cart-total span, .cart-summary .cart-total span').text(formatNumber(data.cartTotal));

                    // Update header cart badge and total
                    updateHeaderCart(data.cartCount, data.cartTotal);
                }
            }
        });
    });

    // Remove item - convert to AJAX
    $('.btn-danger[href*="removeFromCart"]').click(function(e) {
        e.preventDefault();
        let url = $(this).attr('href');
        let row = $(this).closest('tr');

        if (confirm('Xóa sách này khỏi giỏ hàng?')) {
            $.ajax({
                url: url,
                type: 'GET',
                success: function(data) {
                    if (data.success) {
                        // Remove the row with animation
                        row.fadeOut(300, function() {
                            $(this).remove();

                            // Update header cart
                            updateHeaderCart(data.cartCount, data.cartTotal);

                            // Update cart summary total
                            $('.cart-total span, .cart-summary .cart-total span').text(formatNumber(data.cartTotal));

                            // If cart is empty, reload page to show empty state
                            if (data.cartCount === 0) {
                                location.reload();
                            }
                        });
                    }
                }
            });
        }
    });
});

// Format number with thousand separators
function formatNumber(num) {
    return new Intl.NumberFormat('vi-VN').format(num);
}

// Update header cart badge and total
function updateHeaderCart(count, total) {
    // Update badge
    let badge = $('.cart-badge');
    if (badge.length) {
        badge.text(count);
        badge.addClass('cart-badge-animate');
        setTimeout(() => badge.removeClass('cart-badge-animate'), 300);
    }

    // Update total in header
    let headerTotal = $('.header-cart .cart-total, .action-value.cart-total');
    if (headerTotal.length) {
        headerTotal.text(formatNumber(total) + 'đ');
    }
}
