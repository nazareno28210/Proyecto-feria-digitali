/* URLs DEL API */
const API_URL = "/api/feriantes/current";
const FERIAS_URL = "/api/ferias";
const EDICIONES_URL = "/api/ediciones"; 
const PARTICIPACIONES_URL = "/api/participaciones";
const FERIANTE_UPDATE_URL = "/api/feriantes/current";
const EDICIONES_ACTIVAS_URL = "/api/ediciones/activas"; // 🟢 NUEVA 
const STAND_UPDATE_URL = "/api/stands/mi-stand";
const STAND_TOGGLE_URL = "/api/stands/mi-stand/toggle-activo";
const USUARIO_UPDATE_URL = "/api/usuarios/current";
const LOGOUT_URL = "/api/logout";
const IMAGE_UPLOAD_URL = "/api/feriantes/current/imagen";

let ferianteActual = null;
let todasLasEdiciones = [];
let cropper = null;

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

    // Listener para subir foto (abre modal cropper)
    document.getElementById("inputFotoPerfil").addEventListener("change", prepararRecorte);

    // Listeners para botones del Modal Cropper
    document.getElementById("btn-confirm-crop").addEventListener("click", ejecutarRecorteYSubir);
    document.getElementById("btn-cancel-crop").addEventListener("click", cerrarModalYLimpiar);

    const toggleActivo = document.getElementById("toggle-stand-activo");
    if (toggleActivo) toggleActivo.addEventListener("change", toggleEstadoStand);
});

// --- FUNCIONES DE CROPPER ---

function prepararRecorte(e) {
    const archivo = e.target.files[0];
    if (!archivo) return;

    if (!archivo.type.startsWith('image/')) {
        return mostrarNotificacion("Por favor, selecciona una imagen válida.", "error");
    }

    const reader = new FileReader();
    reader.onload = (event) => {
        const imageToCrop = document.getElementById("image-to-crop");
        const modalCropper = document.getElementById("modal-cropper");

        imageToCrop.src = event.target.result;
        modalCropper.classList.remove("hidden");

        if (cropper) cropper.destroy();
        cropper = new Cropper(imageToCrop, {
            aspectRatio: 1, // Proporción cuadrada perfecta 1:1
            viewMode: 1, // Asegura que no se salga de la imagen
            dragMode: 'move', // Mover la imagen
            autoCropArea: 0.8, // Tamaño inicial del recorte
            cropBoxMovable: false, // Caja de recorte fija
            cropBoxResizable: false, // No se puede cambiar el tamaño del cuadrado
        });
    };
    reader.readAsDataURL(archivo);
}

async function ejecutarRecorteYSubir() {
    if (!cropper) return;
    const canvas = cropper.getCroppedCanvas({ width: 500, height: 500 });

    canvas.toBlob(async (blob) => {
        const formData = new FormData();
        formData.append("imagen", blob, "perfil.jpg");

        try {
            mostrarNotificacion("Actualizando foto...", "info");

            const res = await axios.patch(IMAGE_UPLOAD_URL, formData, {
                withCredentials: true,
                headers: { "Content-Type": "multipart/form-data" }
            });

            // 1. Nueva URL del backend
            const nuevaFotoUrl = res.data.url || res.data;

            // 2. Actualizar imagen visible en el HTML (cache-bust)
            const imgElement = document.getElementById("fotoPerfil");
            if (imgElement) imgElement.src = nuevaFotoUrl + "?t=" + Date.now();

            // 3. Actualizar variable local para que la validación de "Postularme" detecte la foto
            if (ferianteActual && ferianteActual.usuario) {
                ferianteActual.usuario.imagenUrl = nuevaFotoUrl;
            }

            mostrarNotificacion("¡Foto actualizada!", "success");
            cerrarModalYLimpiar();
        } catch (err) {
            manejarError("Error al subir la imagen recortada");
        }
    }, 'image/jpeg');
}

function cerrarModalYLimpiar() {
    document.getElementById("modal-cropper").classList.add("hidden");
    if (cropper) {
        cropper.destroy();
        cropper = null;
    }
    document.getElementById("inputFotoPerfil").value = "";
}

// --- LÓGICA DE PERFIL Y DATOS ---

function manejarError(mensaje) {
    console.error(mensaje);
    mostrarNotificacion(mensaje, "error");
}

function cargarPerfil() {
    const getFeriante = axios.get(API_URL, { withCredentials: true });
    const getEdiciones = axios.get(EDICIONES_URL);

    axios.all([getFeriante, getEdiciones])
        .then(axios.spread(async (resFeriante, resEdiciones) => {
            ferianteActual = resFeriante.data;
            todasLasEdiciones = resEdiciones.data;

            const u = ferianteActual.usuario;
            if (u.imagenUrl) document.getElementById("fotoPerfil").src = u.imagenUrl;

            setText("usuario-email", u.email);
            setText("usuario-nombre", u.nombre);
            setText("usuario-apellido", u.apellido);
            setText("usuario-fecha", u.fechaRegistro);

            setValue("edit-usuario-nombre", u.nombre);
            setValue("edit-usuario-apellido", u.apellido);
            setValue("edit-usuario-email", u.email);

            setText("feriante-tel", ferianteActual.telefono);
            setText("feriante-email", ferianteActual.emailEmprendimiento);

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

                // Cargar participaciones del stand para la tarjeta del dashboard
                try {
                    const resParticipaciones = await axios.get(`${PARTICIPACIONES_URL}/stand/${s.id}`);
                    renderFeriasAsignadas(resParticipaciones.data);
                } catch (error) {
                    console.error("Error cargando participaciones:", error);
                    renderFeriasAsignadas([]);
                }
            } else {
                view.innerHTML = `<p>Aún no tienes un stand asignado.</p>`;
                document.getElementById("btn-edit-stand").style.display = 'none';
                renderFeriasAsignadas([]);
            }
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
        mostrarNotificacion("Usuario actualizado", "success");
        cargarPerfil();
        toggleEditUsuario(false);
    } catch (e) { manejarError("Error al actualizar usuario"); }
}

async function guardarFeriante() {
    const data = {
        telefono: getValue("edit-feriante-tel"),
        emailEmprendimiento: getValue("edit-feriante-email")
    };
    try {
        await axios.put(FERIANTE_UPDATE_URL, data, { withCredentials: true });
        mostrarNotificacion("Feriante actualizado", "success");
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
        mostrarNotificacion("Stand actualizado", "success");
        cargarPerfil();
        toggleEditStand(false);
    } catch (e) { manejarError("Error al guardar stand"); }
}

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
        mostrarNotificacion("Estado actualizado", "success");
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

function renderFeriasAsignadas(misParticipaciones) {
    const feriasCard = document.getElementById("card-mis-ferias");
    const body = document.getElementById("ferias-card-body");

    if (!misParticipaciones || misParticipaciones.length === 0) {
        body.innerHTML = `<p>Tu stand aún no tiene participaciones registradas.</p>`;
        feriasCard.removeAttribute("href");
        feriasCard.style.cursor = "default";
        return;
    }

    // 1. Contenedor con altura máxima y scroll
    let htmlContent = `<div style="max-height: 280px; overflow-y: auto; padding-right: 8px;">`;

    // 2. Invertimos el array para ver lo más reciente arriba
    const participacionesOrdenadas = [...misParticipaciones].reverse();

    for (const p of participacionesOrdenadas) {
        const edicion = todasLasEdiciones.find(e => e.id === p.edicionId);
        if (!edicion) continue;

        htmlContent += `<div style="border-bottom: 1px solid #e2e8f0; padding-bottom: 12px; margin-bottom: 12px;">`;

        if (p.estado === 'RECHAZADO' || p.estado === 'CANCELADO') {
            htmlContent += `
                <div style="background-color: #fee2e2; border-left: 4px solid #ef4444; padding: 8px; border-radius: 4px; margin-bottom: 8px;">
                    <p style="color: #b91c1c; margin: 0; font-size: 0.9em;"><strong>🔴 Solicitud ${p.estado}</strong></p>
                    <p style="color: #7f1d1d; font-size: 0.85em; margin-top: 4px; margin-bottom: 0;">
                        <strong>Motivo:</strong> ${p.motivoRechazo || 'No se especificó.'}
                    </p>
                </div>
                <h4 style="margin: 0 0 4px 0; font-size: 1em;">${edicion.nombreEdicion || 'Edición'}</h4>
                <p style="font-size: 0.85em; color: #666; margin: 0;">No puedes volver a postularte.</p>
            `;
        } else {
            const iconoEstado = (p.estado === 'APROBADA' || p.estado === 'CONFIRMADO')
                ? '🟢 Confirmada'
                : p.estado === 'EN_ESPERA'
                    ? '🟠 En Lista de Espera'
                    : '🟡 Pendiente';
            htmlContent += `
                <p style="margin: 0 0 4px 0; font-size: 0.85em;">Estado: <strong>${iconoEstado}</strong></p>
                <h4 style="margin: 0 0 6px 0; font-size: 1em;">
                    <a href="/web/feria_detalle.html?id=${edicion.feriaId || edicion.id}" style="color: #2563eb; text-decoration: none;">
                        ${edicion.nombreEdicion || 'Edición'} - ${edicion.feriaNombre || 'Feria'}
                    </a>
                </h4>
                <p style="margin: 0; font-size: 0.85em; color: #475569;"><strong>Fecha:</strong> ${edicion.fechaInicio}</p>
                ${p.numeroStand ? `<p style="margin: 2px 0 0 0; font-size: 0.85em; color: #475569;"><strong>Mesa:</strong> #${p.numeroStand}</p>` : ''}
            `;
        }
        htmlContent += `</div>`;
    }

    htmlContent += `</div>`;
    body.innerHTML = htmlContent;

    feriasCard.removeAttribute("href");
    feriasCard.style.cursor = "default";
}

function cerrarSesion() {
    axios.post(LOGOUT_URL, {}, { withCredentials: true })
        .then(() => window.location.href = "/web/login.html")
        .catch(() => manejarError("Error al cerrar sesión"));
}

function setText(id, t) { const el = document.getElementById(id); if (el) el.textContent = t || "-"; }
function setValue(id, v) { const el = document.getElementById(id); if (el) el.value = v || ""; }
function getValue(id) { const el = document.getElementById(id); return el ? el.value : ""; }



// --- LÓGICA DE POSTULACIÓN A FERIAS (AHORA EDICIONES) ---

// --- LÓGICA DE POSTULACIÓN A FERIAS (AHORA EDICIONES) ---

async function abrirModalPostulacion() {
    if (!ferianteActual || !ferianteActual.stand) {
        return mostrarNotificacion("Necesitas tener un stand asignado para postularte.", "error");
    }

    // VALIDACIÓN ESTRICTA DE FOTO DE PERFIL
    const fotoUrl = ferianteActual.usuario?.imagenUrl;
    if (!fotoUrl || fotoUrl === "" || fotoUrl.includes("default")) {
        mostrarNotificacion("Para postularte, primero subí una foto de perfil.", "error");
        return;
    }

    if (!ferianteActual.stand.descripcion || ferianteActual.stand.descripcion.trim() === "" || !ferianteActual.stand.imagenUrl) {
        return mostrarNotificacion("Debes completar la descripción y foto de tu emprendimiento en 'Mi Perfil' para postularte.", "warning");
    }

    const modal = document.getElementById("modal-postulacion");
    const contenedor = document.getElementById("lista-ferias-disponibles");

    modal.classList.remove("hidden");
    contenedor.innerHTML = "<p>Buscando ediciones disponibles y lugares libres...</p>";

    try {
        const [resEdiciones, resMisParticipaciones] = await Promise.all([
            axios.get(EDICIONES_ACTIVAS_URL),
            axios.get(`${PARTICIPACIONES_URL}/stand/${ferianteActual.stand.id}`)
        ]);

        const edicionesActivas = Array.from(new Map(resEdiciones.data.map(e => [e.id, e])).values());
        const misParticipaciones = resMisParticipaciones.data;

        const hoy = new Date();
        hoy.setHours(0, 0, 0, 0);

        const IDsParticipando = misParticipaciones.map(p => p.edicionId);

        const disponibles = edicionesActivas.filter(e => {
            if (!e.fechaInicio) return false;
            const fechaEdicion = new Date(e.fechaInicio + "T00:00:00");
            fechaEdicion.setHours(0, 0, 0, 0);
            return (!IDsParticipando.includes(e.id) && fechaEdicion >= hoy);
        });

        // FILTRO BRUTAL ANTI-DUPLICADOS (Fuerza 1 solo registro por ID)
        const mapaEdiciones = new Map();
        disponibles.forEach(e => mapaEdiciones.set(e.id, e));
        const edicionesUnicas = Array.from(mapaEdiciones.values());

        contenedor.innerHTML = "";

        if (edicionesUnicas.length === 0) {
            contenedor.innerHTML = "<p>No hay ediciones nuevas disponibles por el momento.</p>";
            return;
        }

        for (const e of edicionesUnicas) {
            // 2. AUDITORÍA EN CONSOLA (Presiona F12 en el navegador para ver cómo llega el mapa)
            console.log("Datos de la edición recibida:", e);

            let opcionesEspacios = '<option value="">-- Selecciona un lote --</option>';
            let todosLosEspacios = [];
            let espaciosDisponibles = [];

            try {
                const resEspacios = await axios.get(`/api/espacios/edicion/${e.id}`);
                todosLosEspacios = resEspacios.data;
                espaciosDisponibles = todosLosEspacios.filter(esp => esp.estado === 'DISPONIBLE');

                if (todosLosEspacios.length === 0) {
                    opcionesEspacios = '<option value="">-- Sin preferencias --</option><option value="" disabled>Agotado (Sin lugares)</option>';
                } else {
                    todosLosEspacios.forEach(esp => {
                        const disabledAttr = esp.estado !== 'DISPONIBLE' ? ' disabled' : '';
                        opcionesEspacios += `<option value="${esp.id}"${disabledAttr}>${esp.nombre} - $${esp.precio} (${esp.estado})</option>`;
                    });
                }
            } catch (error) {
                console.error("Error cargando espacios:", error);
                opcionesEspacios = '<option value="" disabled>Error al cargar lugares</option>';
            }

            // Bug #1: Detectar si hay espacios disponibles
            const hayEspaciosDisponibles = espaciosDisponibles.length > 0;

            // 3. CAPTURA DE IMAGEN MÁS ROBUSTA (Busca en camelCase y en snake_case)
            const urlDelMapa = e.mapaUrl || e.mapa_url;
            const mapaHtml = urlDelMapa
                ? `<img src="${urlDelMapa}" alt="Mapa de la feria" style="width: 100%; border-radius: 8px; margin-bottom: 10px; max-height: 200px; object-fit: contain; background-color: #f8fafc; border: 1px solid #ccc;">`
                : `<p style="font-size: 0.85em; color: #666; font-style: italic; text-align: center;">(No hay mapa disponible para esta edición)</p>`;

            const div = document.createElement("div");
            div.className = "feria-item-modal";

            // 🟡 DETECCIÓN DE CUPO LLENO O SIN ESPACIOS
            const capacidad = e.feriaCapacidad || null;
            const cuposOcupados = e.cuposOcupados || 0;
            const cupoLleno = capacidad !== null && cuposOcupados >= capacidad;
            const esListaEspera = cupoLleno || !hayEspaciosDisponibles;

            const btnTexto = esListaEspera ? "Anotarse en Lista de Espera" : "Inscribirme";
            const btnEstilo = esListaEspera
                ? "background: linear-gradient(135deg, #f59e0b, #d97706); color: #fff; border: none; padding: 10px 20px; border-radius: 8px; font-weight: bold; cursor: pointer;"
                : "background: var(--color-dark-blue, #0f172a); color: #fff; border: none; padding: 10px 20px; border-radius: 8px; font-weight: bold; cursor: pointer;";
            const btnClase = esListaEspera ? "btn btn-warning" : "btn btn-primary btn-solicitar";

            const alertaEspera = esListaEspera
                ? `<div style="margin-top: 10px; padding: 10px 14px; background: #fef3c7; border-left: 4px solid #f59e0b; border-radius: 6px; font-size: 0.88em; color: #92400e;">
                        ⚠️ <strong>Cupos agotados.</strong> Si te postulas, entrarás en la lista de espera por si se libera un espacio.
                   </div>`
                : "";

            // Bug #1: Ocultar select de ubicación si es lista de espera
            const preferenciaHtml = esListaEspera
                ? `<p style="font-size:0.85em; color:#92400e; font-style:italic;">Sin disponibilidad — se registrará en lista de espera.</p>`
                : `<label style="display:block; font-size: 0.9em; color: #334155; margin-bottom: 5px;"><strong>Reserva tu ubicación:</strong></label>
                   <select id="select-preferencia-${e.id}" class="input-select" style="width: 100%; padding: 8px;">
                       ${opcionesEspacios}
                   </select>`;

            div.innerHTML = `
                <div class="feria-item-info">
                    <h4>${e.nombreEdicion || 'Edición'} - ${e.feriaNombre || ''}</h4>
                    <p><strong>Descripción:</strong> ${e.feriaDescripcion || 'Feria local'}</p>
                    <p><i class="fas fa-map-marker-alt"></i> ${e.feriaLugar}</p>
                    <p><i class="fas fa-calendar"></i> ${e.fechaInicio} al ${e.fechaFinal || 'N/A'}</p>
                    
                    <div class="preferencia-container" style="margin-top: 15px; padding: 10px; background: #f8fafc; border-radius: 8px; border: 1px solid #e2e8f0;">
                        ${mapaHtml}
                        ${preferenciaHtml}
                    </div>
                </div>
                ${alertaEspera}
                <div class="modal-footer" style="margin-top: 15px; display: flex; justify-content: flex-end; align-items: center; width: 100%; border-top: 1px solid #e2e8f0; padding-top: 12px;">
                    <button class="${btnClase}" onclick="enviarSolicitudConPreferencia(${e.id}, ${esListaEspera})" style="${btnEstilo}">${btnTexto}</button>
                </div>
            `;

            contenedor.appendChild(div);
        }

    } catch (error) {
        console.error("Error en postulación:", error);
        mostrarNotificacion("Error al cargar ediciones disponibles.", "error");
        cerrarModalPostulacion();
    }
}

function cerrarModalPostulacion() {
    document.getElementById("modal-postulacion").classList.add("hidden");
}

async function enviarSolicitudConPreferencia(edicionId, esListaEspera = false) {
    const select = document.getElementById(`select-preferencia-${edicionId}`);
    const espacioId = select ? select.value : "";



    try {
        const payload = {
            edicionId: edicionId,
            standId: ferianteActual.stand.id
        };
        // Solo enviar espacioId si el feriante seleccionó uno (no es lista de espera)
        if (!esListaEspera && espacioId) {
            payload.espacioId = parseInt(espacioId);
        }

        const res = await axios.post(`${PARTICIPACIONES_URL}/inscribir`, payload);

        const msg = res.data?.mensaje || "¡Inscripción enviada!";
        mostrarNotificacion(msg, "success");

        cerrarModalPostulacion();
        cargarPerfil();
    } catch (error) {
        const msg = error.response?.data?.error || "Error al enviar la solicitud";
        mostrarNotificacion(msg, "error");
    }
}