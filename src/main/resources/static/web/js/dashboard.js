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
    inicializarMenuPerfil();
    document.getElementById("btn-logout").addEventListener("click", cerrarSesion);
});

function inicializarMenuPerfil() {
    const profileBtn = document.getElementById("btn-profile-menu");
    const profileDropdown = document.getElementById("profile-dropdown");

    if (!profileBtn || !profileDropdown) return;

    profileBtn.addEventListener("click", (e) => {
        e.stopPropagation();
        const isHidden = profileDropdown.classList.contains("hidden");
        
        if (isHidden) {
            profileDropdown.classList.remove("hidden");
            profileBtn.classList.add("active");
            profileBtn.setAttribute("aria-expanded", "true");
        } else {
            profileDropdown.classList.add("hidden");
            profileBtn.classList.remove("active");
            profileBtn.setAttribute("aria-expanded", "false");
        }
    });

    // Cerrar el menú si se hace clic fuera de él
    document.addEventListener("click", (e) => {
        if (!profileDropdown.contains(e.target) && !profileBtn.contains(e.target)) {
            profileDropdown.classList.add("hidden");
            profileBtn.classList.remove("active");
            profileBtn.setAttribute("aria-expanded", "false");
        }
    });
}

async function verificarAdmin() {
    try {
        const response = await axios.get(AUTH_URL, { withCredentials: true });
        const usuario = response.data;

        if (!usuario || usuario.tipoUsuario !== 'ADMINISTRADOR') {
            if (typeof mostrarNotificacion === "function") {
                mostrarNotificacion("Acceso denegado. Zona exclusiva para administradores.", "error");
            } else if (typeof showToast === "function") {
                showToast("Acceso denegado. Zona exclusiva para administradores.", "error");
            }
            setTimeout(() => {
                window.location.href = "/web/ferias.html"; // Lo mandamos fuera
            }, 1500);
            return;
        }

        const nombreMostrar = usuario.nombre || usuario.email;
        document.getElementById("bienvenida").textContent = `Bienvenido, ${nombreMostrar}`;
        
        const dropdownUserName = document.getElementById("dropdown-user-name");
        if (dropdownUserName) {
            dropdownUserName.textContent = nombreMostrar;
        }

        const avatarImg = document.getElementById("header-user-avatar");
        if (avatarImg && usuario.imagenUrl && usuario.imagenUrl.trim() !== "") {
            avatarImg.src = usuario.imagenUrl;
        }

    } catch (error) {
        console.error("Error de autenticación:", error);
        if (typeof mostrarNotificacion === "function") {
            mostrarNotificacion("No estás autenticado. Redirigiendo...", "error");
        } else if (typeof showToast === "function") {
            showToast("No estás autenticado. Redirigiendo...", "error");
        }
        setTimeout(() => {
            window.location.href = "/web/login.html";
        }, 1500);
    }
}

async function cerrarSesion() {
    try {
        await axios.post(LOGOUT_URL, {}, { withCredentials: true });
        if (typeof mostrarNotificacion === "function") {
            mostrarNotificacion("Sesión cerrada correctamente.", "success");
        } else if (typeof showToast === "function") {
            showToast("Sesión cerrada correctamente.", "success");
        }
        setTimeout(() => {
            window.location.href = "/web/login.html";
        }, 1500);
    } catch (error) {
        console.error("Error al cerrar sesión:", error);
        if (typeof mostrarNotificacion === "function") {
            mostrarNotificacion("Error al cerrar sesión.", "error");
        } else if (typeof showToast === "function") {
            showToast("Error al cerrar sesión.", "error");
        }
    }
}