/* URLs DEL API */
const API_URL = "http://localhost:8080/api/feriantes/current";
const FERIAS_URL = "http://localhost:8080/api/ferias";
const FERIANTE_UPDATE_URL = "http://localhost:8080/api/feriantes/current";
const STAND_UPDATE_URL = "http://localhost:8080/api/stands/mi-stand";
const STAND_TOGGLE_URL = "http://localhost:8080/api/stands/mi-stand/toggle-activo";
const USUARIO_UPDATE_URL = "http://localhost:8080/api/usuarios/current";
const LOGOUT_URL = "http://localhost:8080/api/logout";

let ferianteActual = null;
let todasLasFerias = [];

document.addEventListener("DOMContentLoaded", () => {
    cargarPerfil();

    // Listeners Usuario
    document.getElementById("btn-edit-usuario").addEventListener("click", () => toggleEditUsuario(true));
    document.getElementById("btn-cancel-usuario").addEventListener("click", () => toggleEditUsuario(false));
    document.getElementById("btn-save-usuario").addEventListener("click", guardarUsuario);

    // Listeners Feriante
    document.getElementById("btn-edit-feriante").addEventListener("click", () => toggleEditFeriante(true));
    document.getElementById("btn-cancel-feriante").addEventListener("click", () => toggleEditFeriante(false));
    document.getElementById("btn-save-feriante").addEventListener("click", guardarFeriante);

    // Listeners Stand
    document.getElementById("btn-edit-stand").addEventListener("click", () => toggleEditStand(true));
    document.getElementById("btn-cancel-stand").addEventListener("click", () => toggleEditStand(false));
    document.getElementById("btn-save-stand").addEventListener("click", guardarStand);

    document.getElementById("cerrarSesion").addEventListener("click", cerrarSesion);
    
    // El error de la línea 25 estaba aquí: aseguramos que la función exista
    document.getElementById("inputFotoPerfil").addEventListener("change", subirFotoPerfil);

    const toggleActivo = document.getElementById("toggle-stand-activo");
    if (toggleActivo) toggleActivo.addEventListener("change", toggleEstadoStand);
});

// Función para centralizar errores (Soluciona el error de la línea 100)
function manejarError(mensaje) {
    console.error(mensaje);
    showToast(mensaje, "error");
}

function cargarPerfil() {
    const getFeriante = axios.get(API_URL, { withCredentials: true });
    const getFerias = axios.get(FERIAS_URL);

    axios.all([getFeriante, getFerias])
        .then(axios.spread((resFeriante, resFerias) => {
            ferianteActual = resFeriante.data;
            todasLasFerias = resFerias.data;

            const u = ferianteActual.usuario;
            if (u.imagenUrl) document.getElementById("fotoPerfil").src = u.imagenUrl;
            
            setText("usuario-email", u.email);
            setText("usuario-nombre", u.nombre);
            setText("usuario-apellido", u.apellido);
            setText("usuario-fecha", u.fechaRegistro);
            
            setValue("edit-usuario-nombre", u.nombre);
            setValue("edit-usuario-apellido", u.apellido);
            setValue("edit-usuario-email", u.email);

            setText("feriante-nombre", ferianteActual.nombreEmprendimiento);
            setText("feriante-desc", ferianteActual.descripcion);
            setText("feriante-tel", ferianteActual.telefono);
            setText("feriante-email", ferianteActual.emailEmprendimiento);
            
            setValue("edit-feriante-nombre", ferianteActual.nombreEmprendimiento);
            setValue("edit-feriante-desc", ferianteActual.descripcion);
            setValue("edit-feriante-tel", ferianteActual.telefono);
            setValue("edit-feriante-email", ferianteActual.emailEmprendimiento);

            const s = ferianteActual.stand;
            const view = document.getElementById("stand-view");
            if (s) {
                view.innerHTML = `<p><strong>Nombre:</strong> ${s.nombre}</p><p><strong>Descripción:</strong> ${s.descripcion}</p>`;
                const toggle = document.getElementById("toggle-stand-activo");
                if (toggle) {
                    toggle.checked = s.activo;
                    actualizarUIEstado(s.activo);
                }
                setValue("edit-stand-nombre", s.nombre);
                setValue("edit-stand-desc", s.descripcion);
                document.getElementById("btn-edit-stand").style.display = 'block';
            } else {
                view.innerHTML = `<p>Aún no tienes un stand asignado.</p>`;
                document.getElementById("btn-edit-stand").style.display = 'none';
            }
            renderFeriasAsignadas(s);
        }))
        .catch(err => manejarError("Error al cargar el perfil. Verifica tu sesión."));
}

async function guardarUsuario() {
    const data = { 
        nombre: getValue("edit-usuario-nombre"), 
        apellido: getValue("edit-usuario-apellido"), 
        email: getValue("edit-usuario-email") 
    };
    try {
        await axios.post(USUARIO_UPDATE_URL, data, { withCredentials: true });
        showToast("Usuario actualizado", "success");
        cargarPerfil(); 
        toggleEditUsuario(false);
    } catch (e) { manejarError(e.response?.data || "Error al actualizar usuario"); }
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
        showToast("Feriante actualizado", "success");
        cargarPerfil(); 
        toggleEditFeriante(false);
    } catch (e) { manejarError("Error al guardar feriante"); }
}

async function guardarStand() {
    const data = { 
        nombre: getValue("edit-stand-nombre"), 
        descripcion: getValue("edit-stand-desc") 
    };
    try {
        await axios.put(STAND_UPDATE_URL, data, { withCredentials: true });
        showToast("Stand actualizado", "success");
        cargarPerfil(); 
        toggleEditStand(false);
    } catch (e) { manejarError("Error al guardar stand"); }
}

// TOGGLES DE VISTA
function toggleEditUsuario(m) { 
    document.getElementById("usuario-view").style.display = m ? 'none' : 'block'; 
    document.getElementById("usuario-edit").style.display = m ? 'block' : 'none'; 
    document.getElementById("btn-edit-usuario").style.display = m ? 'none' : 'block';
}
function toggleEditFeriante(m) { 
    document.getElementById("feriante-view").style.display = m ? 'none' : 'block'; 
    document.getElementById("feriante-edit").style.display = m ? 'block' : 'none'; 
    document.getElementById("btn-edit-feriante").style.display = m ? 'none' : 'block';
}
function toggleEditStand(m) { 
    document.getElementById("stand-view").style.display = m ? 'none' : 'block'; 
    document.getElementById("stand-edit").style.display = m ? 'block' : 'none'; 
    document.getElementById("btn-edit-stand").style.display = m ? 'none' : 'block';
}

async function toggleEstadoStand() {
    try {
        const res = await axios.patch(STAND_TOGGLE_URL, {}, { withCredentials: true });
        actualizarUIEstado(res.data.activo);
        showToast("Estado actualizado", "success");
    } catch (e) { 
        this.checked = !this.checked; 
        manejarError("Error al cambiar estado"); 
    }
}

function actualizarUIEstado(a) {
    const l = document.getElementById("stand-status-label");
    if (l) { 
        l.textContent = a ? "Stand Abierto (Público)" : "Stand Cerrado (Privado)"; 
        l.className = a ? "status-badge status-open" : "status-badge status-closed"; 
    }
}

function renderFeriasAsignadas(s) {
    const feriasCard = document.getElementById("card-mis-ferias");
    const body = document.getElementById("ferias-card-body");

    // Verificamos si el stand tiene una feria asignada [cite: 29]
    if (s && s.feriaId) {
        const f = todasLasFerias.find(f => f.id === s.feriaId);
        
        if (f) {
            // 1. Cambiamos el contenido visual [cite: 30]
            const icono = f.estado === 'Activa' ? '🟢' : '🔴';
            body.innerHTML = `
                <p>Tu stand está asignado a:</p>
                <h3>${icono} ${f.nombre}</h3>
                <p><strong>Lugar:</strong> ${f.lugar}</p>
                <p><strong>Fechas:</strong> ${f.fechaInicio} al ${f.fechaFinal}</p>
            `;

            // 2. HACEMOS QUE EL ENLACE FUNCIONE 
            // Ajusta la ruta /web/feria_detalle.html según como se llame tu archivo de destino
            feriasCard.href = `/web/feria_detalle.html?id=${f.id}`; 
            feriasCard.style.cursor = "pointer";
            feriasCard.style.opacity = "1";
        }
    } else {
        // Si no hay feria, desactivamos el enlace [cite: 32]
        body.innerHTML = `<p>Tu stand aún no ha sido asignado a ninguna feria.</p>`;
        feriasCard.removeAttribute("href");
        feriasCard.style.cursor = "default";
        feriasCard.style.opacity = "0.7";
    }
}

// DEFINICIÓN DE SUBIR FOTO (Soluciona el error ReferenceError)
async function subirFotoPerfil(e) {
    const file = e.target.files[0];
    if (!file) return;
    const formData = new FormData();
    formData.append("imagen", file);
    try {
        const res = await axios.patch("/api/usuarios/current/imagen", formData, { 
            withCredentials: true, 
            headers: { "Content-Type": "multipart/form-data" } 
        });
        document.getElementById("fotoPerfil").src = res.data.imagenUrl;
        showToast("Imagen actualizada", "success");
    } catch (err) { manejarError("Error al subir imagen"); }
}

function cerrarSesion() { 
    axios.post(LOGOUT_URL, {}, { withCredentials: true })
        .then(() => window.location.href = "/web/login.html")
        .catch(() => manejarError("Error al cerrar sesión"));
}

// HELPERS
function setText(id, t) { const el = document.getElementById(id); if (el) el.textContent = t || "-"; }
function setValue(id, v) { const el = document.getElementById(id); if (el) el.value = v || ""; }
function getValue(id) { const el = document.getElementById(id); return el ? el.value : ""; }

function showToast(m, t) { 
    if (typeof Toastify !== "undefined") {
        Toastify({ 
            text: m, 
            duration: 3000, 
            style: { background: t === "success" ? "linear-gradient(to right, #2ecc71, #27ae60)" : "linear-gradient(to right, #e74c3c, #c0392b)" } 
        }).showToast(); 
    } else {
        alert(m);
    }
}