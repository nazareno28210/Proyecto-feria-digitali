// URLs del API
const API_URL = "http://localhost:8080/api/usuarios/current";
const LOGOUT_URL = "http://localhost:8080/api/logout";

document.addEventListener("DOMContentLoaded", () => {
    // Carga inicial
    cargarPerfil();

    // Listener del botón de logout
    document.getElementById("cerrarSesion").addEventListener("click", cerrarSesion);
});

// ========================================================
// CARGAR PERFIL (Llenar VISTA)
// ========================================================
function cargarPerfil() {
    axios.get(API_URL, { withCredentials: true })
        .then(response => {
            const usuario = response.data; // Obtenemos el UsuarioDTO

            if (!usuario) {
                return manejarError("No se pudo cargar la información del usuario.");
            }

            // 1. Llenar Datos de Usuario
            setText("usuario-email", usuario.email);
            setText("usuario-nombre", usuario.nombre);
            setText("usuario-apellido", usuario.apellido);
            setText("usuario-fecha", usuario.dayRegistrer); // Nota: El DTO usa dayRegistrer

        })
        .catch(error => {
            console.error("Error al obtener perfil:", error);
            manejarError("Error al cargar el perfil. Verifica tu sesión.");
        });
}

// ========================================================
// NAVEGACIÓN Y UTILIDADES
// (Funciones copiadas de perfil.js)
// ========================================================

function cerrarSesion() {
    axios.post(LOGOUT_URL, {}, { withCredentials: true })
        .then(() => {
            showToast("Sesión cerrada", "success");
            setTimeout(() => window.location.href = "/web/login.html", 1000);
        })
        .catch(() => {
            manejarError("Error al cerrar sesión.");
        });
}

function manejarError(mensaje) {
    // Usamos Toast en lugar de alert para errores de sesión
    showToast(mensaje, "error");
    setTimeout(() => window.location.href = "/web/login.html", 2000);
}

// --- Funciones Helpers para DOM ---
function setText(id, texto) {
    if (document.getElementById(id)) {
        document.getElementById(id).textContent = texto || "-";
    }
}

// --- Función Helper para Toasts ---
function showToast(mensaje, tipo = "info") {
    const color = tipo === "success"
        ? "linear-gradient(to right, #2ecc71, #27ae60)" // Verde
        : "linear-gradient(to right, #e74c3c, #c0392b)"; // Rojo

    Toastify({
        text: mensaje,
        duration: 3000,
        gravity: "top",
        position: "right",
        style: {
            background: tipo === "info" ? "#0078d4" : color,
        }
    }).showToast();
}