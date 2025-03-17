let cart = [];

function addToCart(itemName, price) {
    let existingItem = cart.find(item => item.itemName === itemName);
    if (existingItem) {
        existingItem.quantity += 1; // Increment quantity if item exists
    } else {
        cart.push({ itemName, price, quantity: 1 }); // Set quantity to 1 if new item
    }
    updateCart();
}

function changeQuantity(itemName, price, change) {
    let item = cart.find(item => item.itemName === itemName);
    if (item) {
        item.quantity += change; // Increase or decrease quantity
        if (item.quantity <= 0) {
            cart = cart.filter(i => i.itemName !== itemName); // Remove item if quantity is 0
        }
    }
    updateCart();
}

function updateCart() {
    let cartSummary = document.getElementById('cart-summary');
    let cartTotal = 0;
    let cartItems = '';

    cart.forEach(item => {
        let quantity = item.quantity || 0; // Ensure quantity is never undefined
        let itemTotal = item.price * quantity;
        cartTotal += itemTotal;
        
        cartItems += `
            <div>
                <p>${item.itemName} x${quantity} - ₹${itemTotal}</p>
                <button onclick="changeQuantity('${item.itemName}', ${item.price}, 1)">+</button>
                <button onclick="changeQuantity('${item.itemName}', ${item.price}, -1)">-</button>
            </div>
        `;
    });

    cartSummary.innerHTML = `
        <h3>Your Cart</h3>
        ${cartItems || "<p>Cart is empty</p>"}
        <p><strong>Total: ₹${cartTotal}</strong></p>
        <label for="coupon">Enter Coupon Code:</label>
        <input type="text" id="coupon" />
        <button id="checkout-button" onclick="checkout()">Checkout</button>
    `;

    generateQRCode(cartTotal);
}

function checkout() {
    if (cart.length === 0) {
        alert("Your cart is empty!");
        return;
    }
    alert('Proceeding to checkout!');
}

function generateQRCode(total) {
    let qrCodeContainer = document.getElementById('qrcode');
    qrCodeContainer.innerHTML = ""; // Clear previous QR code
    QRCode.toCanvas(qrCodeContainer, `Pay ₹${total} to the restaurant`, function (error) {
        if (error) console.error(error);
    });
}
