// Configuración de endpoints
const AUTH_URL = "http://localhost:8080/api/usuarios/current";
const LOGOUT_URL = "http://localhost:8080/api/logout";

document.addEventListener("DOMContentLoaded", () => {
    verificarAdmin();
    document.getElementById("btn-logout").addEventListener("click", cerrarSesion);
});

async function verificarAdmin() {
    try {
        // 1. Consultamos quién está logueado
        const response = await axios.get(AUTH_URL, { withCredentials: true });
        const usuario = response.data;

        // 2. Verificamos si existe y si es ADMINISTRADOR
        // IMPORTANTE: Asegúrate de que tu DTO de usuario devuelva 'tipoUsuario'
        if (!usuario || usuario.tipoUsuario !== 'ADMINISTRADOR') {
            alert("Acceso denegado. Zona exclusiva para administradores.");
            window.location.href = "/web/ferias.html"; // Lo mandamos fuera
            return;
        }

        // 3. Si es admin, mostramos la bienvenida
        const nombreMostrar = usuario.nombre || usuario.email;
        document.getElementById("bienvenida").textContent = `Bienvenido, ${nombreMostrar}`;

    } catch (error) {
        // Si da error 401/403, no está logueado
        console.error("Error de autenticación:", error);
        window.location.href = "/web/login.html";
    }
}

async function cerrarSesion() {
    try {
        await axios.post(LOGOUT_URL, {}, { withCredentials: true });
        window.location.href = "/web/login.html";
    } catch (error) {
        console.error("Error al cerrar sesión:", error);
        // Fallback por si falla axios
        window.location.href = "/web/login.html";
    }
}