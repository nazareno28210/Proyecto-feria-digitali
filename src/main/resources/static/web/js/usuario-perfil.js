// URLs del API
const API_URL = "http://localhost:8080/api/usuarios/current";
const LOGOUT_URL = "http://localhost:8080/api/logout";
const SOLICITUD_URL = "http://localhost:8080/api/solicitudes";


document.addEventListener("DOMContentLoaded", () => {
    // Carga inicial
    cargarPerfil();

    // Listener del botón de logout
    document.getElementById("cerrarSesion").addEventListener("click", cerrarSesion);

    // 🟢 AÑADIDO: Listener para el botón de cambiar contraseña
    document.getElementById("btn-cambiar-pass").addEventListener("click", (e) => {
        e.preventDefault(); // Previene que el enlace "#" mueva la página
        showToast("Función aún no implementada.", "warning");
    });
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
            setText("usuario-fecha", usuario.dayRegistrer);

            // 2. Verificar si debe mostrar la tarjeta de "Ser Feriante"
            if (usuario.tipoUsuario === "NORMAL") {
                verificarSolicitudFeriante(usuario.email);
            }

        })
        .catch(error => {
            console.error("Error al obtener perfil:", error);
            manejarError("Error al cargar el perfil. Verifica tu sesión.");
        });
}

// ========================================================
// VERIFICAR SOLICITUD PENDIENTE (Actualizado)
// ========================================================
async function verificarSolicitudFeriante(emailUsuario) {
    // 🟢 CAMBIO: Apunta al nuevo contenedor dinámico
    const container = document.getElementById("feriante-solicitud-container");
    if (!container) return;

    try {
        const solicitudRes = await axios.get(`${SOLICITUD_URL}/pendientes`, { withCredentials: true });
        const pendientes = solicitudRes.data;
        const tienePendiente = pendientes.some(s => s.emailUsuario === emailUsuario);

        if (tienePendiente) {
            // Si tiene una solicitud, muestra un mensaje
            container.innerHTML = `
                <div class="mensaje-pendiente">
                    Tu solicitud para ser feriante está pendiente de aprobación.
                </div>
            `;
        } else {
            // Si NO tiene solicitud, muestra la tarjeta de acción (VERDE)
            container.innerHTML = `
                <!-- 🟢 CAMBIO: Añadida la clase "accion-card--green" 🟢 -->
                <a href="/web/solicitud-feriante.html" class="accion-card accion-card--green">
                    <div class="icon">🚀</div>
                    <h3>Quiero ser Feriante</h3>
                    <p>Envía tu solicitud para crear un stand y vender tus productos.</p>
                </a>
            `;
        }
    } catch (error) {
        console.error("Error al verificar solicitud:", error);
        container.innerHTML = `<p style="color: red;">No se pudo verificar el estado de tu solicitud.</p>`;
    }
}


// ========================================================
// NAVEGACIÓN Y UTILIDADES
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
    let color;
    switch (type) {
        case "success":
            color = "linear-gradient(to right, #2ecc71, #27ae60)"; // Verde
            break;
        case "error":
            color = "linear-gradient(to right, #e74c3c, #c0392b)"; // Rojo
            break;
        // 🟢 AÑADIDO: Color "warning" para la función no implementada
        case "warning":
            color = "linear-gradient(to right, #f39c12, #e67e22)"; // Naranja
            break;
        default:
            color = "linear-gradient(to right, #3498db, #2980b9)"; // Azul
    }

    Toastify({
        text: mensaje,
        duration: 3000,
        gravity: "top",
        position: "right",
        style: {
            background: color,
        }
    }).showToast();
}