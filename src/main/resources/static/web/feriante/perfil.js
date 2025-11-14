// URLs del API
const API_URL = "http://localhost:8080/api/feriantes/current";
const FERIAS_URL = "http://localhost:8080/api/ferias";
const FERIANTE_UPDATE_URL = "http://localhost:8080/api/feriantes/current";
const STAND_UPDATE_URL = "http://localhost:8080/api/stands/mi-stand";
const LOGOUT_URL = "http://localhost:8080/api/logout";

// Almacenamos los datos actuales para poder "Cancelar" la edición
let ferianteActual = null;
let todasLasFerias = [];

document.addEventListener("DOMContentLoaded", () => {
    // Carga inicial
    cargarPerfil();

    // Listeners de botones
    document.getElementById("cerrarSesion").addEventListener("click", cerrarSesion);

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

    const getFeriante = axios.get(API_URL, { withCredentials: true });
    const getFerias = axios.get(FERIAS_URL);

    axios.all([getFeriante, getFerias])
        .then(axios.spread((resFeriante, resFerias) => {

            ferianteActual = resFeriante.data;
            todasLasFerias = resFerias.data;

            if (!ferianteActual || !ferianteActual.usuario) {
                return manejarError("No se pudo cargar la información.");
            }

            // 1. Llenar Datos de Usuario
            const usuario = ferianteActual.usuario;
            setText("usuario-email", usuario.email);
            setText("usuario-nombre", usuario.nombre);
            setText("usuario-apellido", usuario.apellido);
            setText("usuario-fecha", usuario.fechaRegistro);

            // 2. Llenar Datos de Feriante (Vista y Edición)
            setText("feriante-nombre", ferianteActual.nombreEmprendimiento);
            setText("feriante-desc", ferianteActual.descripcion);
            setText("feriante-tel", ferianteActual.telefono);
            setText("feriante-email", ferianteActual.emailEmprendimiento);

            setValue("edit-feriante-nombre", ferianteActual.nombreEmprendimiento);
            setValue("edit-feriante-desc", ferianteActual.descripcion);
            setValue("edit-feriante-tel", ferianteActual.telefono);
            setValue("edit-feriante-email", ferianteActual.emailEmprendimiento);

            // 3. Llenar Datos de Stand (Vista y Edición)
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

                document.getElementById("btn-edit-stand").style.display = 'block';
            } else {
                standViewContainer.innerHTML = `<p>Aún no tienes un stand asignado.</p>`;
                document.getElementById("btn-edit-stand").style.display = 'none';
                document.getElementById("stand-edit").style.display = 'none';
            }

            // 4. Llenar Tarjeta de Ferias Asignadas
            renderFeriasAsignadas(stand);

        }))
        .catch(error => {
            console.error("Error al obtener perfil:", error);
            manejarError("Error al cargar el perfil. Verificá tu sesión.");
        });
}

// 🟢 CAMBIO: Esta función ahora también añade el Href a la tarjeta 🟢
function renderFeriasAsignadas(stand) {
    const feriasCard = document.getElementById("card-mis-ferias"); // El <a>
    const feriasCardBody = document.getElementById("ferias-card-body"); // El <div> interno

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
            // Añadimos el enlace a la tarjeta
            feriasCard.href = `/web/feria_detalle.html?id=${miFeria.id}`;
            // Nos aseguramos de que parezca un enlace (cursor pointer)
            feriasCard.style.cursor = "pointer";
        } else {
            feriasCardBody.innerHTML = `
                <p>Tu stand está asignado a una feria (ID: ${stand.feriaId}) que no se pudo encontrar.</p>
            `;
            feriasCard.style.cursor = "default"; // No parece un enlace
            // 🟢 AÑADIDO: Quitamos el enlace y prevenimos el clic
            feriasCard.removeAttribute("href");
            feriasCard.addEventListener('click', (e) => e.preventDefault());
        }
    } else {
        feriasCardBody.innerHTML = `
            <p>Tu stand aún no ha sido asignado a ninguna feria por un administrador.</p>
        `;
        feriasCard.style.cursor = "default"; // No parece un enlace
        // 🟢 AÑADIDO: Quitamos el enlace y prevenimos el clic
        feriasCard.removeAttribute("href");
        feriasCard.addEventListener('click', (e) => e.preventDefault());
    }
}


// ========================================================
// LÓGICA DE EDICIÓN (Feriante) - (Sin cambios)
// ========================================================

function toggleEditFeriante(modoEdicion) {
    document.getElementById("feriante-view").style.display = modoEdicion ? 'none' : 'block';
    document.getElementById("feriante-edit").style.display = modoEdicion ? 'block' : 'none';
    document.getElementById("btn-edit-feriante").style.display = modoEdicion ? 'none' : 'block';

    if (!modoEdicion) {
        setValue("edit-feriante-nombre", ferianteActual.nombreEmprendimiento);
        setValue("edit-feriante-desc", ferianteActual.descripcion);
        setValue("edit-feriante-tel", ferianteActual.telefono);
        setValue("edit-feriante-email", ferianteActual.emailEmprendimiento);
    }
}

async function guardarFeriante() {
    const data = {
        nombreEmprendimiento: getValue("edit-feriante-nombre"),
        descripcion: getValue("edit-feriante-desc"),
        telefono: getValue("edit-feriante-tel"),
        emailEmprendimiento: getValue("edit-feriante-email")
    };

    try {
        await axios.put(FERIANTE_UPDATE_URL, data, { withCredentials: true });
        showToast("Perfil de feriante actualizado", "success");
        cargarPerfil();
        toggleEditFeriante(false);
    } catch (error) {
        console.error("Error al guardar feriante:", error);
        showToast("Error al guardar. " + (error.response?.data?.error || "Intente de nuevo."), "error");
    }
}

// ========================================================
// LÓGICA DE EDICIÓN (Stand) - (Sin cambios)
// ========================================================

function toggleEditStand(modoEdicion) {
    document.getElementById("stand-view").style.display = modoEdicion ? 'none' : 'block';
    document.getElementById("stand-edit").style.display = modoEdicion ? 'block' : 'none';
    document.getElementById("btn-edit-stand").style.display = modoEdicion ? 'none' : 'block';

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
        cargarPerfil();
        toggleEditStand(false);
    } catch (error) {
        console.error("Error al guardar stand:", error);
        showToast("Error al guardar el stand.", "error");
    }
}


// ========================================================
// NAVEGACIÓN Y UTILIDADES - (Sin cambios)
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