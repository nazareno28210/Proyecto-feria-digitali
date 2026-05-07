function getTokenFromURL() {
    const params = new URLSearchParams(window.location.search);
    return params.get("token");
}

function showToast(message, type) {
    Toastify({
        text: message,
        duration: 4000,
        gravity: "top",
        position: "right",
        style: {
            background: type === "success" 
                ? "linear-gradient(to right, #16a34a, #22c55e)" 
                : "linear-gradient(to right, #dc2626, #ef4444)",
            borderRadius: "8px",
            fontFamily: "Poppins, sans-serif",
            fontWeight: "500",
            boxShadow: "0 4px 15px rgba(0,0,0,0.2)"
        },
        stopOnFocus: true
    }).showToast();
}

function showMessage(text, type) {
    const mensaje = document.getElementById("mensaje");
    mensaje.textContent = text;
    mensaje.className = "show " + type;
}

function hideMessage() {
    const mensaje = document.getElementById("mensaje");
    mensaje.className = "";
    mensaje.textContent = "";
}

function setLoading(isLoading) {
    const btn = document.getElementById("submitBtn");
    btn.disabled = isLoading;
    if (isLoading) {
        btn.classList.add("loading");
    } else {
        btn.classList.remove("loading");
    }
}

async function cambiarPassword() {
    const password = document.getElementById("password").value;
    const confirmPassword = document.getElementById("confirmPassword").value;
    
    // Limpiar mensajes previos
    hideMessage();

    // Validar campos vacios
    if (!password || !confirmPassword) {
        showMessage("Por favor completa ambos campos", "error");
        showToast("Por favor completa ambos campos", "error");
        return;
    }

    // Validar que coincidan
    if (password !== confirmPassword) {
        showMessage("Las contrasenas no coinciden", "error");
        showToast("Las contrasenas no coinciden", "error");
        return;
    }

    // Validar longitud minima
    if (password.length < 8) {
        showMessage("La contrasena debe tener al menos 8 caracteres", "error");
        showToast("La contrasena debe tener al menos 8 caracteres", "error");
        return;
    }

    const token = getTokenFromURL();
    
    if (!token) {
        showMessage("Token no valido o expirado", "error");
        showToast("Token no valido o expirado", "error");
        return;
    }

    setLoading(true);

    try {
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
            showMessage(text || "Contrasena actualizada correctamente", "success");
            showToast("Contrasena actualizada. Redirigiendo al login...", "success");

            setTimeout(() => {
                window.location.href = "/web/login.html";
            }, 2500);
        } else {
            showMessage(text || "Error al cambiar la contrasena", "error");
            showToast(text || "Error al cambiar la contrasena", "error");
        }
    } catch (error) {
        showMessage("Error de conexion. Intenta nuevamente.", "error");
        showToast("Error de conexion. Intenta nuevamente.", "error");
    } finally {
        setLoading(false);
    }
}

// Permitir enviar con Enter
document.addEventListener("DOMContentLoaded", function() {
    const inputs = document.querySelectorAll("input");
    inputs.forEach(input => {
        input.addEventListener("keypress", function(e) {
            if (e.key === "Enter") {
                cambiarPassword();
            }
        });
    });
});
