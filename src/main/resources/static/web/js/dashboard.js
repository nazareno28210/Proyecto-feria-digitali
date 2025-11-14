/*
 * ====================================
 * DASHBOARD.JS (con Toastify)
 * ====================================
 */

// 1. AÑADIDA: Función Toastify
function showToast(message, type = "info") {
  let color;
  switch (type) {
    case "success":
      color = "linear-gradient(to right, #1a3a5a, #3b82f6)"; 
      break;
    case "error":
      color = "linear-gradient(to right, #ef4444, #b91c1c)"; 
      break;
    case "warning":
      color = "linear-gradient(to right, #3b82f6, #67e8f9)";
      break;
    default:
      color = "linear-gradient(to right, #3b82f6, #67e8f9)"; 
  }
  Toastify({
    text: message,
    duration: 4000,
    gravity: "top", 
    position: "right", 
    style: {
        background: color,
    },
    stopOnFocus: true,
  }).showToast();
}

// Configuración de endpoints (sin cambios)
const AUTH_URL = "http://localhost:8080/api/usuarios/current";
const LOGOUT_URL = "http://localhost:8080/api/logout";

document.addEventListener("DOMContentLoaded", () => {
    verificarAdmin();
    document.getElementById("btn-logout").addEventListener("click", cerrarSesion);
});

async function verificarAdmin() {
    try {
        const response = await axios.get(AUTH_URL, { withCredentials: true });
        const usuario = response.data;

        if (!usuario || usuario.tipoUsuario !== 'ADMINISTRADOR') {
            // CAMBIO: alert a toast
            showToast("❌ Acceso denegado. Zona exclusiva para administradores.", "error");
            setTimeout(() => {
                window.location.href = "/web/ferias.html"; // Lo mandamos fuera
            }, 1500);
            return;
        }

        const nombreMostrar = usuario.nombre || usuario.email;
        document.getElementById("bienvenida").textContent = `Bienvenido, ${nombreMostrar}`;

    } catch (error) {
        console.error("Error de autenticación:", error);
        // CAMBIO: alert a toast
        showToast("❌ No estás autenticado. Redirigiendo...", "error");
        setTimeout(() => {
            window.location.href = "/web/login.html";
        }, 1500);
    }
}

async function cerrarSesion() {
    try {
        await axios.post(LOGOUT_URL, {}, { withCredentials: true });
        // CAMBIO: alert a toast
        showToast("✅ Sesión cerrada correctamente.", "success");
        setTimeout(() => {
            window.location.href = "/web/login.html";
        }, 1500);
    } catch (error) {
        console.error("Error al cerrar sesión:", error);
        // CAMBIO: alert a toast
        showToast("❌ Error al cerrar sesión", "error");
    }
}