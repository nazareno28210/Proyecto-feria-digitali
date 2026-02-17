function getTokenFromURL() {
    const params = new URLSearchParams(window.location.search);
    return params.get("token");
}

async function cambiarPassword() {

    const password = document.getElementById("password").value;
    const confirmPassword = document.getElementById("confirmPassword").value;
    const mensaje = document.getElementById("mensaje");

    if (password !== confirmPassword) {
        mensaje.innerText = "Las contraseñas no coinciden";
        mensaje.style.color = "red";
        return;
    }

    const token = getTokenFromURL();

    const response = await fetch("/auth/reset-password", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            token: token,
            nuevaPassword: password
        })
    });

    const text = await response.text();

    if (response.ok) {
        mensaje.innerText = text;
        mensaje.style.color = "green";

        setTimeout(() => {
            window.location.href = "/web/login.html";
        }, 2000);
    } else {
        mensaje.innerText = text;
        mensaje.style.color = "red";
    }
}
