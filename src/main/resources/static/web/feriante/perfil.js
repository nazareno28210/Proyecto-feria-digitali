// URLs del API
const API_URL = "http://localhost:8080/api/feriantes/current";
const FERIANTE_UPDATE_URL = "http://localhost:8080/api/feriantes/current";
const STAND_UPDATE_URL = "http://localhost:8080/api/stands/mi-stand";
const LOGOUT_URL = "http://localhost:8080/api/logout";

// Almacenamos los datos actuales para poder "Cancelar" la edición
let ferianteActual = null;

document.addEventListener("DOMContentLoaded", () => {
    // Carga inicial
    cargarPerfil();

    // Listeners de botones
    document.getElementById("cerrarSesion").addEventListener("click", cerrarSesion);
    document.getElementById("btn-productos").addEventListener("click", irAProductos);

    // --- Listeners para Feriante ---
    document.getElementById("btn-edit-feriante").addEventListener("click", () => toggleEditFeriante(true));
    document.getElementById("btn-cancel-feriante").addEventListener("click", () => toggleEditFeriante(false));
    document.getElementById("btn-save-feriante").addEventListener("click", guardarFeriante);

    // --- Listeners para Stand ---
    document.getElementById("btn-edit-stand").addEventListener("click", () => toggleEditStand(true));
    document.getElementById("btn-cancel-stand").addEventListener("click", () => toggleEditStand(false));
    document.getElementById("btn-save-stand").addEventListener("click", guardarStand);
});

// ========================================================
// CARGAR PERFIL (Llenar VISTA y EDICIÓN)
// ========================================================
function cargarPerfil() {
    axios.get(API_URL, { withCredentials: true })
        .then(response => {
            ferianteActual = response.data; // Guardamos los datos globalmente

            if (!ferianteActual || !ferianteActual.usuario) {
                return manejarError("No se pudo cargar la información.");
            }

            // 1. Llenar Datos de Usuario
            const usuario = ferianteActual.usuario;
            setText("usuario-email", usuario.email);
            setText("usuario-nombre", usuario.nombre);
            setText("usuario-apellido", usuario.apellido);
            setText("usuario-fecha", usuario.dayRegistrer);

            // 2. Llenar Datos de Feriante (Modo Vista y Modo Edición)
            setText("feriante-nombre", ferianteActual.nombreEmprendimiento);
            setText("feriante-desc", ferianteActual.descripcion);
            setText("feriante-tel", ferianteActual.telefono);
            setText("feriante-email", ferianteActual.emailEmprendimiento);

            setValue("edit-feriante-nombre", ferianteActual.nombreEmprendimiento);
            setValue("edit-feriante-desc", ferianteActual.descripcion);
            setValue("edit-feriante-tel", ferianteActual.telefono);
            setValue("edit-feriante-email", ferianteActual.emailEmprendimiento);


            // 3. Llenar Datos de Stand (Modo Vista y Modo Edición)
            const stand = ferianteActual.stand;
            const standViewContainer = document.getElementById("stand-view");

            if (stand) {
                // Modo Vista
                standViewContainer.innerHTML = `
                    <p><strong>Nombre Stand:</strong> <span id="stand-nombre">${stand.nombre}</span></p>
                    <p><strong>Descripción Stand:</strong> <span id="stand-desc">${stand.descripcion}</span></p>
                `;
                // Modo Edición
                setValue("edit-stand-nombre", stand.nombre);
                setValue("edit-stand-desc", stand.descripcion);

                // Habilitamos el botón de editar stand
                document.getElementById("btn-edit-stand").style.display = 'block';
            } else {
                standViewContainer.innerHTML = `<p>Aún no tienes un stand asignado.</p>`;
                // Ocultamos el botón de editar stand
                document.getElementById("btn-edit-stand").style.display = 'none';
                document.getElementById("stand-edit").style.display = 'none';
            }
        })
        .catch(error => {
            console.error("Error al obtener perfil:", error);
            manejarError("Error al cargar el perfil. Verificá tu sesión.");
        });
}

// ========================================================
// LÓGICA DE EDICIÓN (Feriante)
// ========================================================

function toggleEditFeriante(modoEdicion) {
    document.getElementById("feriante-view").style.display = modoEdicion ? 'none' : 'block';
    document.getElementById("feriante-edit").style.display = modoEdicion ? 'block' : 'none';
    document.getElementById("btn-edit-feriante").style.display = modoEdicion ? 'none' : 'block';

    // Si cancelamos, reseteamos los valores a los originales
    if (!modoEdicion) {
        setValue("edit-feriante-nombre", ferianteActual.nombreEmprendimiento);
        setValue("edit-feriante-desc", ferianteActual.descripcion);
        setValue("edit-feriante-tel", ferianteActual.telefono);
        setValue("edit-feriante-email", ferianteActual.emailEmprendimiento);
    }
}

async function guardarFeriante() {
    // 1. Recolectar datos del formulario de edición
    const data = {
        nombreEmprendimiento: getValue("edit-feriante-nombre"),
        descripcion: getValue("edit-feriante-desc"),
        telefono: getValue("edit-feriante-tel"),
        emailEmprendimiento: getValue("edit-feriante-email")
    };

    try {
        // 2. Enviar al backend
        await axios.put(FERIANTE_UPDATE_URL, data, { withCredentials: true });

        // 3. Éxito: Recargar el perfil y salir del modo edición
        showToast("Perfil de feriante actualizado", "success");
        cargarPerfil();
        toggleEditFeriante(false);

    } catch (error) {
        console.error("Error al guardar feriante:", error);
        showToast("Error al guardar. " + (error.response?.data?.error || "Intente de nuevo."), "error");
    }
}

// ========================================================
// LÓGICA DE EDICIÓN (Stand)
// ========================================================

function toggleEditStand(modoEdicion) {
    document.getElementById("stand-view").style.display = modoEdicion ? 'none' : 'block';
    document.getElementById("stand-edit").style.display = modoEdicion ? 'block' : 'none';
    document.getElementById("btn-edit-stand").style.display = modoEdicion ? 'none' : 'block';

    // Si cancelamos, reseteamos
    if (!modoEdicion && ferianteActual.stand) {
        setValue("edit-stand-nombre", ferianteActual.stand.nombre);
        setValue("edit-stand-desc", ferianteActual.stand.descripcion);
    }
}

async function guardarStand() {
    const data = {
        nombre: getValue("edit-stand-nombre"),
        descripcion: getValue("edit-stand-desc")
    };

    try {
        await axios.put(STAND_UPDATE_URL, data, { withCredentials: true });
        showToast("Información del Stand actualizada", "success");
        cargarPerfil(); // Recargamos todo
        toggleEditStand(false);

    } catch (error) {
        console.error("Error al guardar stand:", error);
        showToast("Error al guardar el stand.", "error");
    }
}


// ========================================================
// NAVEGACIÓN Y UTILIDADES
// ========================================================

function irAProductos() {
    window.location.href = "/web/feriante/misproductos.html";
}

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
    alert(mensaje);
    window.location.href = "/web/login.html";
}

// --- Funciones Helpers para DOM ---
function setText(id, texto) {
    document.getElementById(id).textContent = texto || "-";
}

function setValue(id, valor) {
    document.getElementById(id).value = valor || "";
}

function getValue(id) {
    return document.getElementById(id).value;
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