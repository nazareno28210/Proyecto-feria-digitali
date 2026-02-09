/*
 * ========================================================
 * JS DE PERFIL DEL FERIANTE (Versión Integrada)
 * ========================================================
 */

// 1. URLs DEL API [cite: 143-144]
const API_URL = "http://localhost:8080/api/feriantes/current";
const FERIAS_URL = "http://localhost:8080/api/ferias";
const FERIANTE_UPDATE_URL = "http://localhost:8080/api/feriantes/current";
const STAND_UPDATE_URL = "http://localhost:8080/api/stands/mi-stand";
const STAND_TOGGLE_URL = "http://localhost:8080/api/stands/mi-stand/toggle-activo"; // 🟢 Nuevo endpoint
const LOGOUT_URL = "http://localhost:8080/api/logout";

// Variables globales para el estado de la página [cite: 145]
let ferianteActual = null;
let todasLasFerias = [];

document.addEventListener("DOMContentLoaded", () => {
    // Carga inicial de datos [cite: 146]
    cargarPerfil();

    // Listener para cerrar sesión [cite: 146]
    document.getElementById("cerrarSesion").addEventListener("click", cerrarSesion);
    document.getElementById("inputFotoPerfil").addEventListener("change", subirFotoPerfil);

    // --- Listeners para la edición de Feriante --- [cite: 146]
    document.getElementById("btn-edit-feriante").addEventListener("click", () => toggleEditFeriante(true));
    document.getElementById("btn-cancel-feriante").addEventListener("click", () => toggleEditFeriante(false));
    document.getElementById("btn-save-feriante").addEventListener("click", guardarFeriante);

    // --- Listeners para la edición del Stand --- [cite: 146]
    document.getElementById("btn-edit-stand").addEventListener("click", () => toggleEditStand(true));
    document.getElementById("btn-cancel-stand").addEventListener("click", () => toggleEditStand(false));
    document.getElementById("btn-save-stand").addEventListener("click", guardarStand);

    // --- Listener para el Toggle de Activación de Stand ---
    const toggleActivo = document.getElementById("toggle-stand-activo");
    if (toggleActivo) {
        toggleActivo.addEventListener("change", toggleEstadoStand);
    }
});

// ========================================================
// CARGAR PERFIL (Llenar VISTA y EDICIÓN) [cite: 147]
// ========================================================
function cargarPerfil() {
    const getFeriante = axios.get(API_URL, { withCredentials: true });
    const getFerias = axios.get(FERIAS_URL);

    axios.all([getFeriante, getFerias])
        .then(axios.spread((resFeriante, resFerias) => {
            ferianteActual = resFeriante.data;
            todasLasFerias = resFerias.data;

            if (!ferianteActual || !ferianteActual.usuario) {
                return manejarError("No se pudo cargar la información del perfil.");
            }

            // 1. Llenar Datos de Usuario [cite: 149]
            const usuario = ferianteActual.usuario;
            if (usuario.imagenUrl) {
                document.getElementById("fotoPerfil").src = usuario.imagenUrl;
            }
            setText("usuario-email", usuario.email);
            setText("usuario-nombre", usuario.nombre);
            setText("usuario-apellido", usuario.apellido);
            setText("usuario-fecha", usuario.fechaRegistro);

            // 2. Llenar Datos de Feriante [cite: 150-151]
            setText("feriante-nombre", ferianteActual.nombreEmprendimiento);
            setText("feriante-desc", ferianteActual.descripcion);
            setText("feriante-tel", ferianteActual.telefono);
            setText("feriante-email", ferianteActual.emailEmprendimiento);
            setValue("edit-feriante-nombre", ferianteActual.nombreEmprendimiento);
            setValue("edit-feriante-desc", ferianteActual.descripcion);
            setValue("edit-feriante-tel", ferianteActual.telefono);
            setValue("edit-feriante-email", ferianteActual.emailEmprendimiento);

            // 3. Llenar Datos de Stand e Interruptor 
            const stand = ferianteActual.stand;
            const standViewContainer = document.getElementById("stand-view");
            const toggleInput = document.getElementById("toggle-stand-activo");

            if (stand) {
                standViewContainer.innerHTML = `
                    <p><strong>Nombre Stand:</strong> <span id="stand-nombre">${stand.nombre}</span></p>
                    <p><strong>Descripción Stand:</strong> <span id="stand-desc">${stand.descripcion}</span></p>
                `;
                // Sincronizar el interruptor visual con el estado de la base de datos
                if (toggleInput) {
                    toggleInput.checked = stand.activo;
                    actualizarUIEstado(stand.activo);
                }

                setValue("edit-stand-nombre", stand.nombre);
                setValue("edit-stand-desc", stand.descripcion);
                document.getElementById("btn-edit-stand").style.display = 'block';
            } else {
                standViewContainer.innerHTML = `<p>Aún no tienes un stand asignado.</p>`;
                document.getElementById("btn-edit-stand").style.display = 'none';
                document.getElementById("stand-edit").style.display = 'none';
            }

            // 4. Llenar Tarjeta de Ferias Asignadas [cite: 154]
            renderFeriasAsignadas(stand);
        }))
        .catch(error => {
            console.error("Error al obtener perfil:", error);
            manejarError("Error al cargar el perfil. Verifica tu sesión.");
        });
}

// ========================================================
// ACCIONES DE STAND (Toggle y Guardado) [cite: 108-112, 168-171]
// ========================================================

async function toggleEstadoStand() {
    try {
        // Llamada al nuevo endpoint PATCH para activar/desactivar 
        const res = await axios.patch(STAND_TOGGLE_URL, {}, { withCredentials: true });
        const estaActivo = res.data.activo;
        
        actualizarUIEstado(estaActivo);
        showToast(res.data.mensaje, "success");
    } catch (error) {
        // Si falla, revertimos el interruptor visualmente
        this.checked = !this.checked;
        showToast("Error al cambiar la visibilidad del stand", "error");
    }
}

function actualizarUIEstado(activo) {
    const label = document.getElementById("stand-status-label");
    if (!label) return;
    
    if (activo) {
        label.textContent = "Stand Abierto (Público)";
        label.className = "status-badge status-open";
    } else {
        label.textContent = "Stand Cerrado (Privado)";
        label.className = "status-badge status-closed";
    }
}

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
// LÓGICA DE FERIANTE Y FERIAS [cite: 157-168]
// ========================================================

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
            feriasCard.href = `/web/feria_detalle.html?id=${miFeria.id}`; 
            feriasCard.style.cursor = "pointer"; 
        }
    } else {
        feriasCardBody.innerHTML = `<p>Tu stand aún no ha sido asignado a ninguna feria.</p>`; 
        feriasCard.removeAttribute("href"); 
        feriasCard.style.cursor = "default";
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
        await axios.post(FERIANTE_UPDATE_URL, data, { withCredentials: true }); 
        showToast("Perfil de feriante actualizado", "success"); 
        cargarPerfil();
        toggleEditFeriante(false);
    } catch (error) {
        const errorMsg = error.response?.data?.error || "Error al guardar."; 
    }
}

// ========================================================
// NAVEGACIÓN Y UTILIDADES [cite: 171-190]
// ========================================================

function toggleEditFeriante(modoEdicion) {
    document.getElementById("feriante-view").style.display = modoEdicion ? 'none' : 'block'; 
    document.getElementById("feriante-edit").style.display = modoEdicion ? 'block' : 'none'; 
    document.getElementById("btn-edit-feriante").style.display = modoEdicion ? 'none' : 'block';
}

function toggleEditStand(modoEdicion) {
    document.getElementById("stand-view").style.display = modoEdicion ? 'none' : 'block'; 
    document.getElementById("stand-edit").style.display = modoEdicion ? 'block' : 'none'; 
    document.getElementById("btn-edit-stand").style.display = modoEdicion ? 'none' : 'block'; 
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
}

// --- Helpers DOM --- [cite: 179-182]
function setText(id, texto) { const el = document.getElementById(id); if (el) el.textContent = texto || "-"; }
function setValue(id, valor) { const el = document.getElementById(id); if (el) el.value = valor || ""; }
function getValue(id) { const el = document.getElementById(id); return el ? el.value : ""; }

// --- Toasts con Colores --- 
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
