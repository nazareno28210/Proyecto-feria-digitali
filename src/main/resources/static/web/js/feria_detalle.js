/*
 * ====================================
 * FERIA-DETALLE.JS 
 * ====================================
 */



// 🟢 1. Apuntamos al nuevo endpoint de ediciones
const API_URL = "/api/ediciones";
const params = new URLSearchParams(window.location.search);
const edicionId = params.get("id"); 

document.addEventListener("DOMContentLoaded", () => {
    cargarFeria();
    verificarAccesoVoto(); 
});

async function cargarFeria() {
  try {
    const response = await axios.get(`${API_URL}/${edicionId}`);
    const edicion = response.data;

    // 🟢 2. Renderizado de Info de la Edición (Usando tu misma estructura HTML)
    const infoGrid = document.getElementById("info-feria-grid");
    
    // Formatear horas ("14:00:00" -> "14:00")
    const horaApertura = edicion.horaInicio ? edicion.horaInicio.substring(0, 5) : "??:??";
    const horaCierre = edicion.horaFin ? edicion.horaFin.substring(0, 5) : "??:??";

    infoGrid.innerHTML = `
      <div class="info-item">
        <i class="fas fa-map-marker-alt"></i>
        <div class="info-item-content">
          <strong>Lugar</strong>
          <span>${edicion.feriaLugar || "Lugar a definir"}</span>
        </div>
      </div>
      <div class="info-item">
        <i class="fas fa-calendar-check"></i>
        <div class="info-item-content">
          <strong>Fecha inicio</strong>
          <span>${edicion.fechaInicio}</span>
        </div>
      </div>
      <div class="info-item">
        <i class="fas fa-calendar-times"></i>
        <div class="info-item-content">
          <strong>Fecha fin</strong>
          <span>${edicion.fechaFinal ?? "..."}</span>
        </div>
      </div>
      <div class="info-item">
        <i class="fas fa-clock"></i>
        <div class="info-item-content">
          <strong>Horarios</strong>
          <span>${horaApertura} a ${horaCierre} hs</span>
        </div>
      </div>
    `;

    const infoDesc = document.getElementById("info-feria-desc");
    infoDesc.innerHTML = `<p><strong>Descripción:</strong></p><p>${edicion.feriaDescripcion || ""}</p>`;

    // 🟢 Título con nombre de molde y nombre de edición
    document.getElementById("nombre-feria").innerHTML = `${edicion.feriaNombre || "Feria General"} <br><span style="font-size:0.6em; color:#e67e22;">(${edicion.nombreEdicion})</span>`;

    // Pasamos el ID del molde para los votos
    renderizarAprobacionFeria(edicion.feriaId);

    // 🔔 Lógica de Recordatorio para ferias en estado PROXIMA
    const seccionRecordatorio = document.getElementById("seccion-recordatorio-feria");
    if (seccionRecordatorio) {
        if (edicion.estado && edicion.estado.toUpperCase() === "PROXIMA") {
            seccionRecordatorio.style.display = "block";
            verificarEstadoRecordatorio(edicionId);
        } else {
            seccionRecordatorio.style.display = "none";
        }
    }

    // 🟢 3. Lógica de Stands: Ahora buscamos las participaciones de esta edición
    const standsContainer = document.getElementById("stands-container");
    standsContainer.innerHTML = "";
    
    try {
        const participacionesRes = await axios.get(`/api/participaciones/edicion/${edicionId}`);
        const participacionesConfirmadas = participacionesRes.data.filter(p => p.estado === "CONFIRMADO");

        if (participacionesConfirmadas.length > 0) {
            participacionesConfirmadas.forEach((participacion) => {
                const standId = participacion.standId;
                const standNombre = participacion.stand || "Emprendimiento / Stand";
                const standDesc = participacion.standDescripcion || "Emprendimiento participante con variedad de productos de excelente calidad.";
                const ferianteNombre = participacion.ferianteNombre || standNombre;
                const imagenUrl = participacion.standImagenUrl;
                const activo = participacion.standActivo !== false;

                if (standId && activo) {
                    const div = document.createElement("div");
                    div.classList.add("stand-card");
                    
                    const imagenHtml = (imagenUrl && imagenUrl.trim() !== "")
                        ? `<div class="stand-image-container"><img src="${escapeHtml(imagenUrl)}" alt="Logo de ${escapeHtml(standNombre)}" loading="lazy"></div>`
                        : `<div class="stand-image-container"><div class="stand-image-placeholder"><i class="bi bi-shop"></i><span>Stand Feriante</span></div></div>`;

                    div.innerHTML = `
                        ${imagenHtml} 
                        <div class="stand-content">
                            <h3>${escapeHtml(standNombre)}</h3>
                            <p class="stand-desc">${escapeHtml(standDesc)}</p>
                            
                            <div class="stand-feriante-badge">
                                <i class="bi bi-person-badge-fill"></i>
                                <span>${escapeHtml(ferianteNombre)}</span>
                            </div>

                            <button type="button" class="btn-stand" onclick="verProductos(event, ${standId})">
                                <i class="bi bi-box-seam-fill"></i> Ver catálogo de productos
                            </button>
                        </div>
                    `; 
                    standsContainer.appendChild(div);
                }
            });
        } else {
            standsContainer.innerHTML = "<p class='no-stands-msg'>Actualmente no hay stands disponibles para visitar en esta feria.</p>";
        }

    } catch(e) {
        console.error("Error al obtener participaciones de la edición:", e);
        standsContainer.innerHTML = "<p class='no-stands-msg'>No se pudieron cargar los stands.</p>";
    }

  } catch (error) {
    console.error("Error al cargar la feria:", error);
    mostrarNotificacion("Error al cargar los datos.", "error"); 
  }
}

// ⭐ Dibuja el porcentaje de aprobación (busca directo de la feria molde)
async function renderizarAprobacionFeria(feriaId) {
    const contenedor = document.getElementById("aprobacion-header");
    if (!contenedor || !feriaId) return;

    try {
        const res = await axios.get(`/api/ferias/${feriaId}`);
        const porcentaje = res.data.porcentajeAprobacion;
        const total = res.data.totalVotos;

        if (!total || total === 0) {
            contenedor.innerHTML = '<span class="badge bg-secondary opacity-75">Sin votos aún</span>';
            return;
        }

        let colorClase = "bg-success";
        if (porcentaje < 70) colorClase = "bg-warning text-dark";
        if (porcentaje < 40) colorClase = "bg-danger";

        contenedor.innerHTML = `
            <span class="badge ${colorClase} shadow-sm">
                <i class="bi bi-hand-thumbs-up-fill me-1"></i> 
                ${porcentaje}% lo recomienda (${total} votos)
            </span>
        `;
    } catch(err) {
         contenedor.innerHTML = '<span class="badge bg-secondary opacity-75">Votos no disponibles</span>';
    }
}

// ⭐ Envía el voto a la feria (esPositivo: boolean)
async function enviarVotoFeria(esPositivo) {
    try {
        const responseEdicion = await axios.get(`${API_URL}/${edicionId}`);
        const realFeriaId = responseEdicion.data.feriaId;

        await axios.post("/api/votos-feria", {
            feria_id: realFeriaId,
            esPositivo: esPositivo
        }, { withCredentials: true });

        if (typeof Swal !== 'undefined') {
            Swal.fire({
                icon: 'success',
                title: '¡Gracias por tu voto!',
                text: 'Tu opinión fue registrada con éxito.',
                timer: 2000,
                showConfirmButton: false
            });
        } else {
            mostrarNotificacion("Gracias por tu voto.", "success");
        }
        renderizarAprobacionFeria(realFeriaId);
    } catch (err) {
        if (err.response && err.response.status === 403) {
            const msg = typeof err.response.data === 'string' ? err.response.data : 'Los administradores no pueden emitir votos.';
            if (typeof Swal !== 'undefined') Swal.fire('Acceso Restringido', msg, 'warning');
            else mostrarNotificacion(msg, 'warning');
        } else if (err.response && err.response.status === 409) {
            if (typeof Swal !== 'undefined') Swal.fire('Voto Duplicado', 'Ya votaste en esta Feria.', 'info');
            else mostrarNotificacion('Ya votaste en esta Feria.', 'info');
        } else {
            const msg = (err.response && err.response.data) ? err.response.data : "Error al votar.";
            if (typeof Swal !== 'undefined') Swal.fire('Error', typeof msg === 'string' ? msg : 'Error al registrar el voto.', 'error');
            else mostrarNotificacion(typeof msg === 'string' ? msg : 'Error al votar.', 'error');
        }
    }
}

async function votarFeria(valor) {
    const esPositivo = (valor === true || valor === 5);
    await enviarVotoFeria(esPositivo);
}


async function verificarAccesoVoto() {
    try {
        const res = await axios.get("/api/usuarios/current", { withCredentials: true });
        if (res.data) {
            document.getElementById("seccion-voto-feria").style.display = "block";
        }
    } catch (e) {
        console.log("Visitante: sección de voto oculta.");
    }
}

function volver() { window.location.href = "ferias.html"; }
function verProductos(event, standIdParam) {
    if (event && event.preventDefault) event.preventDefault();
    const standId = typeof event === 'number' ? event : standIdParam;
    window.location.href = `stand_detalle.html?idStand=${standId}`;
}

function escapeHtml(text) {
    if (!text) return "";
    return String(text)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

// 🔔 Verificar estado de recordatorio para esta edición
async function verificarEstadoRecordatorio(idEdicion) {
    const btn = document.getElementById("btn-recordatorio-feria");
    if (!btn || !idEdicion) return;
    try {
        const response = await axios.get(`/api/recordatorios/edicion/${idEdicion}/estado`, { withCredentials: true });
        if (response.data && response.data.activo) {
            btn.classList.add("activo");
            btn.innerHTML = `<i class="bi bi-bell-fill"></i> Recordatorio activado`;
        } else {
            btn.classList.remove("activo");
            btn.innerHTML = `<i class="bi bi-bell"></i> Recordarme feria`;
        }
    } catch (e) {
        console.log("No se pudo verificar estado de recordatorio:", e);
    }
}

// 🔔 Alternar suscripción a recordatorio de feria próxima
async function alternarRecordatorioFeria() {
    const btn = document.getElementById("btn-recordatorio-feria");
    if (!edicionId) return;

    try {
        const response = await axios.post(`/api/recordatorios/edicion/${edicionId}`, {}, { withCredentials: true });
        const data = response.data;
        if (data.activo) {
            if (btn) {
                btn.classList.add("activo");
                btn.innerHTML = `<i class="bi bi-bell-fill"></i> Recordatorio activado`;
            }
            if (typeof Swal !== 'undefined') {
                Swal.fire({
                    icon: 'success',
                    title: '¡Recordatorio Activado!',
                    text: data.mensaje || 'Esta feria será notificada por correo antes de su apertura.',
                    confirmButtonColor: '#f59e0b'
                });
            } else {
                mostrarNotificacion(data.mensaje || "Esta feria será notificada por correo antes de su apertura.", "success");
            }
        } else {
            if (btn) {
                btn.classList.remove("activo");
                btn.innerHTML = `<i class="bi bi-bell"></i> Recordarme feria`;
            }
            if (typeof Swal !== 'undefined') {
                Swal.fire({
                    icon: 'info',
                    title: 'Recordatorio Cancelado',
                    text: data.mensaje || 'El recordatorio para esta feria ha sido cancelado.',
                    confirmButtonColor: '#3b82f6'
                });
            } else {
                mostrarNotificacion(data.mensaje || "El recordatorio para esta feria ha sido cancelado.", "info");
            }
        }
    } catch (err) {
        if (err.response && err.response.status === 401) {
            const msg = "Debes iniciar sesión para activar el recordatorio por correo.";
            if (typeof Swal !== 'undefined') {
                Swal.fire({
                    icon: 'warning',
                    title: 'Iniciar Sesión Requerido',
                    text: msg,
                    confirmButtonText: 'Ir a Iniciar Sesión',
                    showCancelButton: true,
                    cancelButtonText: 'Cancelar',
                    confirmButtonColor: '#3b82f6'
                }).then((result) => {
                    if (result.isConfirmed) {
                        window.location.href = "login.html";
                    }
                });
            } else {
                mostrarNotificacion(msg, "warning");
            }
        } else {
            const msg = (err.response && err.response.data) ? (typeof err.response.data === 'string' ? err.response.data : err.response.data.mensaje) : "Error al procesar el recordatorio.";
            if (typeof Swal !== 'undefined') Swal.fire('Atención', msg || 'Error al procesar el recordatorio.', 'info');
            else mostrarNotificacion(msg || 'Error al procesar el recordatorio.', 'info');
        }
    }
}
