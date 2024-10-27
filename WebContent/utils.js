function showTemporaryMessage(message) {
    // Create a div element for the message
    const messageDiv = $('<div></div>').text(message).addClass('temporary-message');

    // Style the message div
    messageDiv.css({
        position: 'fixed',
        top: '20%',
        left: '50%',
        transform: 'translate(-50%, -50%)',
        backgroundColor: '#333',
        color: '#fff',
        padding: '20px 40px',
        borderRadius: '8px',
        zIndex: 1000,
        opacity: 0.9,
        fontSize: '1.2rem',
        textAlign: 'center'
    });

    // Add the message div to the body
    $('body').append(messageDiv);

    // Set a timeout to remove the message after 1 second
    setTimeout(function() {
        messageDiv.fadeOut(300, function() {
            $(this).remove();
        });
    }, 1000); // Display for 1 second
}
