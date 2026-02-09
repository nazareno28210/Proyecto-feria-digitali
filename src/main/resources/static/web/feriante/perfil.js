// URLs del API
const API_URL = "http://localhost:8080/api/feriantes/current";
const FERIAS_URL = "http://localhost:8080/api/ferias";
const FERIANTE_UPDATE_URL = "http://localhost:8080/api/feriantes/current";
const STAND_UPDATE_URL = "http://localhost:8080/api/stands/mi-stand";
const LOGOUT_URL = "http://localhost:8080/api/logout";

// Variables globales para el estado de la página
let ferianteActual = null;
let todasLasFerias = [];

document.addEventListener("DOMContentLoaded", () => {
    // Carga inicial de datos al abrir la página
    cargarPerfil();

    // Listener para cerrar sesión
    document.getElementById("cerrarSesion").addEventListener("click", cerrarSesion);
    document.getElementById("inputFotoPerfil").addEventListener("change", subirFotoPerfil);

    // --- Listeners para la edición de Feriante ---
    document.getElementById("btn-edit-feriante").addEventListener("click", () => toggleEditFeriante(true));
    document.getElementById("btn-cancel-feriante").addEventListener("click", () => toggleEditFeriante(false));
    document.getElementById("btn-save-feriante").addEventListener("click", guardarFeriante);

    // --- Listeners para la edición del Stand ---
    document.getElementById("btn-edit-stand").addEventListener("click", () => toggleEditStand(true));
    document.getElementById("btn-cancel-stand").addEventListener("click", () => toggleEditStand(false));
    document.getElementById("btn-save-stand").addEventListener("click", guardarStand);
});

// ========================================================
// CARGAR PERFIL (Llenar VISTA y EDICIÓN)
// ========================================================
function cargarPerfil() {
    // Realizamos ambas peticiones en paralelo (Feriante y todas las Ferias)
    const getFeriante = axios.get(API_URL, { withCredentials: true });
    const getFerias = axios.get(FERIAS_URL);

    axios.all([getFeriante, getFerias])
        .then(axios.spread((resFeriante, resFerias) => {
            ferianteActual = resFeriante.data;
            todasLasFerias = resFerias.data;

            if (!ferianteActual || !ferianteActual.usuario) {
                return manejarError("No se pudo cargar la información del perfil.");
            }

            // 1. Llenar Datos de Usuario (Solo lectura)
            const usuario = ferianteActual.usuario;
            if (usuario.imagenUrl) {
                document.getElementById("fotoPerfil").src = usuario.imagenUrl;
            }
            setText("usuario-email", usuario.email);
            setText("usuario-nombre", usuario.nombre);
            setText("usuario-apellido", usuario.apellido);
            setText("usuario-fecha", usuario.fechaRegistro);

            // 2. Llenar Datos de Feriante (Vista y Formulario de Edición)
            setText("feriante-nombre", ferianteActual.nombreEmprendimiento);
            setText("feriante-desc", ferianteActual.descripcion);
            setText("feriante-tel", ferianteActual.telefono);
            setText("feriante-email", ferianteActual.emailEmprendimiento);

            setValue("edit-feriante-nombre", ferianteActual.nombreEmprendimiento);
            setValue("edit-feriante-desc", ferianteActual.descripcion);
            setValue("edit-feriante-tel", ferianteActual.telefono);
            setValue("edit-feriante-email", ferianteActual.emailEmprendimiento);

            // 3. Llenar Datos de Stand (Vista y Formulario de Edición)
            const stand = ferianteActual.stand;
            const standViewContainer = document.getElementById("stand-view");

            if (stand) {
                standViewContainer.innerHTML = `
                    <p><strong>Nombre Stand:</strong> <span id="stand-nombre">${stand.nombre}</span></p>
                    <p><strong>Descripción Stand:</strong> <span id="stand-desc">${stand.descripcion}</span></p>
                `;
                
                setValue("edit-stand-nombre", stand.nombre);
                setValue("edit-stand-desc", stand.descripcion);
                document.getElementById("btn-edit-stand").style.display = 'block';
            } else {
                standViewContainer.innerHTML = `<p>Aún no tienes un stand asignado.</p>`;
                document.getElementById("btn-edit-stand").style.display = 'none';
                document.getElementById("stand-edit").style.display = 'none';
            }

            // 4. Llenar Tarjeta de Ferias Asignadas (Columna Derecha)
            renderFeriasAsignadas(stand);
        }))
        .catch(error => {
            console.error("Error al obtener perfil:", error);
            manejarError("Error al cargar el perfil. Verifica tu sesión.");
        });
}

/**
 * Renderiza la tarjeta de feria asignada y habilita el enlace si existe una feria.
 */
function renderFeriasAsignadas(stand) {
    const feriasCard = document.getElementById("card-mis-ferias");
    const feriasCardBody = document.getElementById("ferias-card-body");

    if (stand && stand.feriaId) {
        const miFeria = todasLasFerias.find(f => f.id === stand.feriaId);
        if (miFeria) {
            const estadoIcono = miFeria.estado === 'Activa' ? '🟢' : '🔴';
            feriasCardBody.innerHTML = `
                <p>Tu stand está asignado a:</p>
                <h3>${estadoIcono} ${miFeria.nombre}</h3>
                <p><strong>Lugar:</strong> ${miFeria.lugar}</p>
                <p><strong>Fechas:</strong> ${miFeria.fechaInicio} al ${miFeria.fechaFinal}</p>
            `;
            // Configuramos el enlace a los detalles de la feria
            feriasCard.href = `/web/feria_detalle.html?id=${miFeria.id}`;
            feriasCard.style.cursor = "pointer";
        }
    } else {
        feriasCardBody.innerHTML = `<p>Tu stand aún no ha sido asignado a ninguna feria por un administrador.</p>`;
        feriasCard.removeAttribute("href");
        feriasCard.style.cursor = "default";
    }
}

// ========================================================
// LÓGICA DE GUARDADO (POST para compatibilidad)
// ========================================================

/**
 * Guarda los cambios del perfil del feriante.
 */
async function guardarFeriante() {
    const data = {
        nombreEmprendimiento: getValue("edit-feriante-nombre"),
        descripcion: getValue("edit-feriante-desc"),
        telefono: getValue("edit-feriante-tel"),
        emailEmprendimiento: getValue("edit-feriante-email")
    };

    try {
        // Se usa POST para asegurar que el servidor reciba la actualización
        await axios.post(FERIANTE_UPDATE_URL, data, { withCredentials: true });
        showToast("Perfil de feriante actualizado", "success");
        cargarPerfil();
        toggleEditFeriante(false);
    } catch (error) {
        console.error("Error al guardar feriante:", error);
        const errorMsg = error.response?.data?.error || "Error al guardar. Intente de nuevo.";
        showToast(errorMsg, "error");
    }
}

/**
 * Guarda los cambios específicos del Stand.
 */
async function guardarStand() {
    const data = {
        nombre: getValue("edit-stand-nombre"),
        descripcion: getValue("edit-stand-desc")
    };

    try {
        await axios.post(STAND_UPDATE_URL, data, { withCredentials: true });
        showToast("Información del Stand actualizada", "success");
        cargarPerfil();
        toggleEditStand(false);
    } catch (error) {
        console.error("Error al guardar stand:", error);
        showToast("Error al guardar la información del stand.", "error");
    }
}

// ========================================================
// NAVEGACIÓN Y UTILIDADES DE INTERFAZ
// ========================================================

function toggleEditFeriante(modoEdicion) {
    document.getElementById("feriante-view").style.display = modoEdicion ? 'none' : 'block';
    document.getElementById("feriante-edit").style.display = modoEdicion ? 'block' : 'none';
    document.getElementById("btn-edit-feriante").style.display = modoEdicion ? 'none' : 'block';
    
    // Si cancelamos, restauramos los valores originales en los inputs
    if (!modoEdicion && ferianteActual) {
        setValue("edit-feriante-nombre", ferianteActual.nombreEmprendimiento);
        setValue("edit-feriante-desc", ferianteActual.descripcion);
        setValue("edit-feriante-tel", ferianteActual.telefono);
        setValue("edit-feriante-email", ferianteActual.emailEmprendimiento);
    }
}

function toggleEditStand(modoEdicion) {
    document.getElementById("stand-view").style.display = modoEdicion ? 'none' : 'block';
    document.getElementById("stand-edit").style.display = modoEdicion ? 'block' : 'none';
    document.getElementById("btn-edit-stand").style.display = modoEdicion ? 'none' : 'block';

    if (!modoEdicion && ferianteActual && ferianteActual.stand) {
        setValue("edit-stand-nombre", ferianteActual.stand.nombre);
        setValue("edit-stand-desc", ferianteActual.stand.descripcion);
    }
}

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
    // setTimeout(() => window.location.href = "/web/login.html", 2000);
}

// --- Funciones Helpers para DOM ---

function setText(id, texto) {
    const el = document.getElementById(id);
    if (el) el.textContent = texto || "-";
}

function setValue(id, valor) {
    const el = document.getElementById(id);
    if (el) el.value = valor || "";
}

function getValue(id) {
    const el = document.getElementById(id);
    return el ? el.value : "";
}

// --- Función Helper para Toasts ---

function showToast(mensaje, tipo = "info") {
    let color;
    switch (tipo) {
        case "success":
            color = "linear-gradient(to right, #2ecc71, #27ae60)"; // Verde
            break;
        case "error":
            color = "linear-gradient(to right, #e74c3c, #c0392b)"; // Rojo
            break;
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
async function subirFotoPerfil(e) {
    const file = e.target.files[0];
    if (!file) return;

    const formData = new FormData();
    formData.append("imagen", file);

    try {
        const res = await axios.patch(
            "/api/usuarios/current/imagen",
            formData,
            {
                withCredentials: true,
                headers: { "Content-Type": "multipart/form-data" }
            }
        );

        // Actualizar imagen en pantalla
        document.getElementById("fotoPerfil").src = res.data.imagenUrl;

        showToast("Foto de perfil actualizada", "success");
    } catch (error) {
        console.error("Error al subir imagen:", error);
        showToast("Error al subir la imagen", "error");
    }
}
