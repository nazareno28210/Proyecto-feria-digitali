// URLs del API
const API_URL = "http://localhost:8080/api/usuarios/current";
const LOGOUT_URL = "http://localhost:8080/api/logout";
const SOLICITUD_URL = "http://localhost:8080/api/solicitudes";
// Se usa POST para la actualización según la compatibilidad de tu backend
const UPDATE_URL = "http://localhost:8080/api/usuarios/current";

// Elementos de la Vista de Lectura
const spanEmail = document.getElementById("usuario-email");
const spanNombre = document.getElementById("usuario-nombre");
const spanApellido = document.getElementById("usuario-apellido");
const spanFecha = document.getElementById("usuario-fecha");

// Elementos de la Vista de Edición
const vistaLectura = document.getElementById("vista-lectura");
const formEdicion = document.getElementById("form-edicion");
const inputEmail = document.getElementById("edit-email");
const inputNombre = document.getElementById("edit-nombre");
const inputApellido = document.getElementById("edit-apellido");
const spanFechaDisplay = document.getElementById("usuario-fecha-display");

// Botones de acción
const btnEditarToggle = document.getElementById("btn-editar-toggle");
const btnCancelarEdicion = document.getElementById("btn-cancelar-edicion");

document.addEventListener("DOMContentLoaded", () => {
    // Carga inicial de datos desde el servidor
    cargarPerfil();

    // Listener para el botón de cerrar sesión
    document.getElementById("cerrarSesion").addEventListener("click", cerrarSesion);

    // --- Lógica para Editar Perfil ---

    // Al hacer clic en "Editar", pasamos los datos actuales a los campos de texto
    if (btnEditarToggle) {
        btnEditarToggle.addEventListener("click", () => {
            inputEmail.value = spanEmail.textContent;
            inputNombre.value = spanNombre.textContent;
            inputApellido.value = spanApellido.textContent;
            if (spanFechaDisplay) spanFechaDisplay.textContent = spanFecha.textContent;

            toggleModoEdicion(true);
        });
    }

    // Al hacer clic en "Cancelar", volvemos a la vista de lectura
    if (btnCancelarEdicion) {
        btnCancelarEdicion.addEventListener("click", () => toggleModoEdicion(false));
    }

    // Manejo del envío del formulario (Guardar Cambios)
    if (formEdicion) {
        formEdicion.addEventListener("submit", async (e) => {
            e.preventDefault();

            const btnGuardar = document.getElementById("btn-guardar-edicion");
            btnGuardar.disabled = true;
            btnGuardar.textContent = "Guardando...";

            const datosActualizados = {
                email: inputEmail.value,
                nombre: inputNombre.value,
                apellido: inputApellido.value
            };

            try {
                // Petición POST al servidor con los nuevos datos
                await axios.post(UPDATE_URL, datosActualizados, { withCredentials: true });

                showToast("¡Perfil actualizado con éxito!", "success");

                // Actualizamos los textos de la interfaz con los valores nuevos
                setText("usuario-email", datosActualizados.email);
                setText("usuario-nombre", datosActualizados.nombre);
                setText("usuario-apellido", datosActualizados.apellido);

                toggleModoEdicion(false);
            } catch (error) {
                console.error("Error al actualizar:", error);
                const msg = error.response?.data?.message || "Error del servidor al intentar actualizar.";
                showToast(msg, "error");
            } finally {
                btnGuardar.disabled = false;
                btnGuardar.textContent = "Guardar Cambios";
            }
        });
    }
});

// --- Funciones de Carga y Visualización ---

function cargarPerfil() {
    axios.get(API_URL, { withCredentials: true })
        .then(response => {
            const usuario = response.data;
            if (!usuario) return manejarError("No se pudo cargar la información.");

            // Llenamos los campos de la vista de lectura
            setText("usuario-email", usuario.email);
            setText("usuario-nombre", usuario.nombre);
            setText("usuario-apellido", usuario.apellido);
            setText("usuario-fecha", usuario.dayRegistrer);

            // Si el usuario es normal, verificamos si tiene solicitudes de feriante
            if (usuario.tipoUsuario === "NORMAL") {
                verificarSolicitudFeriante(usuario.email);
            }
        })
        .catch(error => {
            console.error("Error al cargar perfil:", error);
            manejarError("Error al cargar el perfil. Verifica tu sesión.");
        });
}

function toggleModoEdicion(activar) {
    if (activar) {
        vistaLectura.classList.add("hidden");
        formEdicion.classList.remove("hidden");
        btnEditarToggle.classList.add("hidden");
    } else {
        vistaLectura.classList.remove("hidden");
        formEdicion.classList.add("hidden");
        btnEditarToggle.classList.remove("hidden");
    }
}

async function verificarSolicitudFeriante(emailUsuario) {
    const container = document.getElementById("feriante-solicitud-container");
    if (!container) return;

    try {
        const res = await axios.get(`${SOLICITUD_URL}/pendientes`, { withCredentials: true });
        const pendientes = res.data;
        const tienePendiente = pendientes.some(s => s.emailUsuario === emailUsuario);

        if (tienePendiente) {
            container.innerHTML = `<div class="mensaje-pendiente">Tu solicitud para ser feriante está pendiente de aprobación.</div>`;
        } else {
            container.innerHTML = `
                <a href="/web/solicitud-feriante.html" class="accion-card accion-card--green">
                    <div class="icon">🚀</div>
                    <h3>Quiero ser Feriante</h3>
                    <p>Envía tu solicitud para crear un stand y vender tus productos.</p>
                </a>`;
        }
    } catch (error) {
        console.error("Error en solicitud:", error);
        container.innerHTML = `<p style="color: red;">No se pudo verificar el estado de tu solicitud.</p>`;
    }
}

// --- Utilidades Generales ---

function cerrarSesion() {
    axios.post(LOGOUT_URL, {}, { withCredentials: true })
        .then(() => {
            showToast("Sesión cerrada", "success");
            setTimeout(() => window.location.href = "/web/login.html", 1000);
        })
        .catch(() => manejarError("Error al cerrar sesión."));
}

function manejarError(mensaje) {
    showToast(mensaje, "error");
}

function setText(id, texto) {
    const el = document.getElementById(id);
    if (el) el.textContent = texto || "-";
}

function showToast(mensaje, tipo = "info") {
    let color;
    switch (tipo) {
        case "success": color = "linear-gradient(to right, #2ecc71, #27ae60)"; break;
        case "error": color = "linear-gradient(to right, #e74c3c, #c0392b)"; break;
        case "warning": color = "linear-gradient(to right, #f39c12, #e67e22)"; break;
        default: color = "linear-gradient(to right, #3498db, #2980b9)";
    }

    Toastify({
        text: mensaje,
        duration: 3000,
        gravity: "top",
        position: "right",
        style: { background: color }
    }).showToast();
}