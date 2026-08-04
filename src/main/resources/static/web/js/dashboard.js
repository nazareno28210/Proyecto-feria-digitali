/*
 * ====================================
 * DASHBOARD.JS
 * ====================================
 */



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
            mostrarNotificacion("Acceso denegado. Zona exclusiva para administradores.", "error");
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
        mostrarNotificacion("No estas autenticado. Redirigiendo...", "error");
        setTimeout(() => {
            window.location.href = "/web/login.html";
        }, 1500);
    }
}

async function cerrarSesion() {
    try {
        await axios.post(LOGOUT_URL, {}, { withCredentials: true });
        // CAMBIO: alert a toast
        mostrarNotificacion("Sesion cerrada correctamente.", "success");
        setTimeout(() => {
            window.location.href = "/web/login.html";
        }, 1500);
    } catch (error) {
        console.error("Error al cerrar sesión:", error);
        // CAMBIO: alert a toast
        mostrarNotificacion("Error al cerrar sesion.", "error");
    }
}