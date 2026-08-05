/*
 * ====================================
 * SOLICITUDES.JS (Responsive & Interactivo)
 * ====================================
 */

let solicitudesData = [];

// =========================================================
// NOTIFICACIONES (Toastify / Sistema de Alertas)
// =========================================================
function showToast(message, type = "info") {
  if (typeof mostrarNotificacion === "function") {
    mostrarNotificacion(message, type);
    return;
  }
  let color;
  switch (type) {
    case "success":
      color = "linear-gradient(135deg, #10b981, #059669)";
      break;
    case "error":
      color = "linear-gradient(135deg, #ef4444, #b91c1c)";
      break;
    case "warning":
      color = "linear-gradient(135deg, #f59e0b, #d97706)";
      break;
    default:
      color = "linear-gradient(135deg, #3b82f6, #1d4ed8)";
  }
  Toastify({
    text: message,
    duration: 3500,
    gravity: "top",
    position: "right",
    style: {
      background: color,
      borderRadius: "10px",
      boxShadow: "0 4px 12px rgba(0,0,0,0.15)",
      fontSize: "0.9rem",
      fontWeight: "600"
    },
    stopOnFocus: true,
  }).showToast();
}

// Elementos DOM
const tablaBody = document.getElementById("tabla-body");
const cardsContainer = document.getElementById("contenedor-cards");
const cantPendientesEl = document.getElementById("cant-pendientes");
const inputBuscar = document.getElementById("input-buscar");
const btnLimpiarBusqueda = document.getElementById("btn-limpiar-busqueda");
const mensajeVacio = document.getElementById("mensaje-vacio");
const emptyStateTitle = document.getElementById("empty-state-title");
const emptyStateText = document.getElementById("empty-state-text");
const contenedorTabla = document.getElementById("contenedor-tabla");

// Modal DOM
const modalDetalle = document.getElementById("modal-detalle");
const modalTitle = document.getElementById("modal-title");
const modalBodyContent = document.getElementById("modal-body-content");
const modalFooterActions = document.getElementById("modal-footer-actions");
const btnCerrarModal = document.getElementById("btn-cerrar-modal");

// Modal Confirmación DOM
const modalConfirmacion = document.getElementById("modal-confirmacion");
const confirmTitle = document.getElementById("confirm-title");
const confirmMessage = document.getElementById("confirm-message");
const confirmIconBadge = document.getElementById("confirm-icon-badge");
const confirmIcon = document.getElementById("confirm-icon");
const btnConfirmAceptar = document.getElementById("btn-confirm-aceptar");
const btnConfirmCancelar = document.getElementById("btn-confirm-cancelar");

let accionPendiente = null;

document.addEventListener("DOMContentLoaded", () => {
  cargarSolicitudes();
  inicializarEventos();
});

function inicializarEventos() {
  if (inputBuscar) {
    inputBuscar.addEventListener("input", (e) => {
      const query = e.target.value.toLowerCase().trim();
      if (query.length > 0) {
        btnLimpiarBusqueda.classList.remove("hidden");
      } else {
        btnLimpiarBusqueda.classList.add("hidden");
      }
      filtrarSolicitudes(query);
    });
  }

  if (btnLimpiarBusqueda) {
    btnLimpiarBusqueda.addEventListener("click", () => {
      inputBuscar.value = "";
      btnLimpiarBusqueda.classList.add("hidden");
      renderizarSolicitudes(solicitudesData);
    });
  }

  if (btnCerrarModal) {
    btnCerrarModal.addEventListener("click", cerrarModalDetalle);
  }

  if (modalDetalle) {
    modalDetalle.addEventListener("click", (e) => {
      if (e.target === modalDetalle) {
        cerrarModalDetalle();
      }
    });
  }

  if (btnConfirmCancelar) {
    btnConfirmCancelar.addEventListener("click", cerrarConfirmacion);
  }

  if (btnConfirmAceptar) {
    btnConfirmAceptar.addEventListener("click", () => {
      if (typeof accionPendiente === "function") {
        const cerrado = accionPendiente();
        if (cerrado) {
          cerrarConfirmacion();
        }
      }
    });
  }

  if (modalConfirmacion) {
    modalConfirmacion.addEventListener("click", (e) => {
      if (e.target === modalConfirmacion) {
        cerrarConfirmacion();
      }
    });
  }
}

// =========================================================
// CARGAR SOLICITUDES DESDE EL BACKEND
// =========================================================
async function cargarSolicitudes() {
  try {
    const response = await axios.get("/api/solicitudes/pendientes");
    solicitudesData = response.data || [];
    
    if (cantPendientesEl) {
      cantPendientesEl.textContent = solicitudesData.length;
    }
    renderizarSolicitudes(solicitudesData);

  } catch (error) {
    console.error("Error cargando solicitudes:", error);
    showToast("Error al conectar con el servidor.", "error");
    mostrarEstadoVacio(
      "Error de conexión",
      "No se pudieron obtener las solicitudes. Verifica tu conexión e intenta nuevamente."
    );
  }
}

// =========================================================
// FILTRADO DINÁMICO
// =========================================================
function filtrarSolicitudes(query) {
  if (!query) {
    renderizarSolicitudes(solicitudesData);
    return;
  }

  const filtradas = solicitudesData.filter(s => {
    const nombreCompleto = `${s.nombreUsuario || ""} ${s.apellidoUsuario || ""}`.toLowerCase();
    const emailUser = (s.emailUsuario || "").toLowerCase();
    const emprendimiento = (s.nombreEmprendimiento || "").toLowerCase();
    const descripcion = (s.descripcion || "").toLowerCase();

    return (
      nombreCompleto.includes(query) ||
      emailUser.includes(query) ||
      emprendimiento.includes(query) ||
      descripcion.includes(query)
    );
  });

  if (filtradas.length === 0) {
    mostrarEstadoVacio(
      "Sin resultados para tu búsqueda",
      `No se encontraron coincidencias para "${query}". Intenta con otros términos.`
    );
  } else {
    renderizarSolicitudes(filtradas, true);
  }
}

// =========================================================
// RENDERIZADO ADAPTATIVO (TABLA + TARJETAS MÓVILES)
// =========================================================
function renderizarSolicitudes(lista, esBusqueda = false) {
  if (tablaBody) tablaBody.innerHTML = "";
  if (cardsContainer) cardsContainer.innerHTML = "";

  if (lista.length === 0) {
    if (!esBusqueda) {
      mostrarEstadoVacio(
        "No hay solicitudes pendientes",
        "¡Excelente! No tienes postulaciones de feriantes pendientes por revisar."
      );
    }
    return;
  }

  ocultarEstadoVacio();

  lista.forEach(s => {
    const nombreCompleto = `${s.nombreUsuario || "-"} ${s.apellidoUsuario || ""}`.trim();
    const emailUsuario = s.emailUsuario || "-";
    const emprendimiento = s.nombreEmprendimiento || "Sin nombre";
    const descripcion = s.descripcion || "Sin descripción proporcionada.";
    const telefono = s.telefono || "-";
    const emailEmprendimiento = s.emailEmprendimiento || "-";

    // --------------------------------------------------
    // 1. FILA DE TABLA (ESCRITORIO)
    // --------------------------------------------------
    if (tablaBody) {
      const tr = document.createElement("tr");
      tr.innerHTML = `
        <td><strong>#${s.id}</strong></td>
        <td>
          <div class="solicitante-col">
            <span class="user-name-text">${escapeHtml(nombreCompleto)}</span>
            <span class="user-email-text">${escapeHtml(emailUsuario)}</span>
          </div>
        </td>
        <td>
          <span class="badge-emprendimiento">
            <i class="fa-solid fa-shop"></i> ${escapeHtml(emprendimiento)}
          </span>
        </td>
        <td>
          <div class="contacto-info">
            ${telefono !== "-" ? `<a href="tel:${escapeHtml(telefono)}" class="contact-link"><i class="fa-solid fa-phone"></i> ${escapeHtml(telefono)}</a>` : ""}
            ${emailEmprendimiento !== "-" ? `<a href="mailto:${escapeHtml(emailEmprendimiento)}" class="contact-link"><i class="fa-solid fa-envelope"></i> ${escapeHtml(emailEmprendimiento)}</a>` : ""}
            ${telefono === "-" && emailEmprendimiento === "-" ? `<span class="user-email-text">Sin contacto adicional</span>` : ""}
          </div>
        </td>
        <td>
          <div class="desc-preview" title="${escapeHtml(descripcion)}">
            ${escapeHtml(descripcion)}
          </div>
          <button class="btn-ver-mas" onclick="abrirModalDetalle(${s.id})">Ver detalle</button>
        </td>
        <td class="actions-cell">
          <div class="actions-group">
            <button class="btn-aprobar" onclick="aprobarSolicitud(${s.id})">
              <i class="fa-solid fa-check"></i> Aprobar
            </button>
            <button class="btn-rechazar" onclick="rechazarSolicitud(${s.id})">
              <i class="fa-solid fa-xmark"></i> Rechazar
            </button>
          </div>
        </td>
      `;
      tablaBody.appendChild(tr);
    }

    // --------------------------------------------------
    // 2. TARJETA MÓVIL (MÓVILES & TABLETS)
    // --------------------------------------------------
    if (cardsContainer) {
      const card = document.createElement("div");
      card.className = "solicitud-card";
      card.innerHTML = `
        <div class="card-top-header">
          <div>
            <div class="card-applicant-title">
              <h3>${escapeHtml(nombreCompleto)}</h3>
            </div>
            <span class="user-email-text"><i class="fa-solid fa-envelope"></i> ${escapeHtml(emailUsuario)}</span>
          </div>
          <span class="card-id-badge">#${s.id}</span>
        </div>

        <div class="card-business-name">
          <i class="fa-solid fa-store"></i> ${escapeHtml(emprendimiento)}
        </div>

        <div class="card-details-list">
          ${telefono !== "-" ? `
            <div class="card-detail-item">
              <i class="fa-solid fa-phone"></i>
              <span>Teléfono: <a href="tel:${escapeHtml(telefono)}" class="contact-link">${escapeHtml(telefono)}</a></span>
            </div>` : ""
          }
          <div class="card-detail-item">
            <i class="fa-solid fa-align-left"></i>
            <span>${escapeHtml(descripcion.substring(0, 100))}${descripcion.length > 100 ? "..." : ""}</span>
          </div>
        </div>

        <div class="card-actions-row">
          <button class="btn-detalle-card" onclick="abrirModalDetalle(${s.id})">
            <i class="fa-solid fa-eye"></i> Detalle
          </button>
          <button class="btn-aprobar" onclick="aprobarSolicitud(${s.id})">
            <i class="fa-solid fa-check"></i> Aprobar
          </button>
          <button class="btn-rechazar" onclick="rechazarSolicitud(${s.id})">
            <i class="fa-solid fa-xmark"></i> Rechazar
          </button>
        </div>
      `;
      cardsContainer.appendChild(card);
    }
  });
}

// =========================================================
// ESTADOS VACÍOS
// =========================================================
function mostrarEstadoVacio(titulo, mensaje) {
  if (contenedorTabla) contenedorTabla.classList.add("hidden");
  if (cardsContainer) cardsContainer.classList.add("hidden");
  if (mensajeVacio) {
    mensajeVacio.classList.remove("hidden");
    if (emptyStateTitle) emptyStateTitle.textContent = titulo;
    if (emptyStateText) emptyStateText.textContent = mensaje;
  }
}

function ocultarEstadoVacio() {
  if (mensajeVacio) mensajeVacio.classList.add("hidden");
  if (contenedorTabla) contenedorTabla.classList.remove("hidden");
  if (cardsContainer) cardsContainer.classList.remove("hidden");
}

// =========================================================
// MODAL DE DETALLE COMPLETO
// =========================================================
function abrirModalDetalle(id) {
  const solicitud = solicitudesData.find(s => s.id === id);
  if (!solicitud) return;

  const nombreCompleto = `${solicitud.nombreUsuario || "-"} ${solicitud.apellidoUsuario || ""}`.trim();
  const cleanPhone = (solicitud.telefono || "").replace(/\D/g, "");

  if (modalTitle) modalTitle.textContent = `Solicitud #${solicitud.id} - ${solicitud.nombreEmprendimiento}`;

  if (modalBodyContent) {
    modalBodyContent.innerHTML = `
      <div class="modal-detail-row">
        <label>Solicitante</label>
        <div class="modal-detail-value">
          <strong>${escapeHtml(nombreCompleto)}</strong> (${escapeHtml(solicitud.emailUsuario || "-")})
        </div>
      </div>

      <div class="modal-detail-row">
        <label>Emprendimiento</label>
        <div class="modal-detail-value">
          <span class="badge-emprendimiento">
            <i class="fa-solid fa-shop"></i> ${escapeHtml(solicitud.nombreEmprendimiento)}
          </span>
        </div>
      </div>

      <div class="modal-detail-row">
        <label>Canales de Contacto Directo</label>
        <div class="modal-detail-value contacto-info" style="gap: 8px;">
          ${solicitud.telefono ? `
            <a href="tel:${escapeHtml(solicitud.telefono)}" class="contact-link">
              <i class="fa-solid fa-phone"></i> Llamar: ${escapeHtml(solicitud.telefono)}
            </a>
            ${cleanPhone ? `
              <a href="https://wa.me/${cleanPhone}" target="_blank" class="contact-link" style="color: #25D366;">
                <i class="fa-brands fa-whatsapp"></i> Enviar WhatsApp
              </a>` : ""
            }
          ` : `<span class="user-email-text">No especificó teléfono.</span>`}
          
          ${solicitud.emailEmprendimiento ? `
            <a href="mailto:${escapeHtml(solicitud.emailEmprendimiento)}" class="contact-link">
              <i class="fa-solid fa-envelope"></i> Email Comercial: ${escapeHtml(solicitud.emailEmprendimiento)}
            </a>` : ""
          }
        </div>
      </div>

      <div class="modal-detail-row">
        <label>Descripción del Proyecto / Propuesta</label>
        <div class="modal-description-box">
          ${escapeHtml(solicitud.descripcion || "Sin descripción.")}
        </div>
      </div>
    `;
  }

  if (modalFooterActions) {
    modalFooterActions.innerHTML = `
      <button class="btn-rechazar" onclick="cerrarModalDetalle(); rechazarSolicitud(${solicitud.id})">
        <i class="fa-solid fa-xmark"></i> Rechazar Solicitud
      </button>
      <button class="btn-aprobar" onclick="cerrarModalDetalle(); aprobarSolicitud(${solicitud.id})">
        <i class="fa-solid fa-check"></i> Aprobar como Feriante
      </button>
    `;
  }

  if (modalDetalle) modalDetalle.classList.remove("hidden");
  document.body.style.overflow = "hidden";
}

function cerrarModalDetalle() {
  if (modalDetalle) modalDetalle.classList.add("hidden");
  document.body.style.overflow = "auto";
}

// =========================================================
// MODAL DE CONFIRMACIÓN PERSONALIZADO (CON MOTIVO DE RECHAZO)
// =========================================================
function mostrarConfirmacion({ titulo, mensaje, textoBoton, tipo, onAceptar }) {
  const wrapperMotivo = document.getElementById("wrapper-motivo-rechazo");
  const inputMotivo = document.getElementById("input-motivo-rechazo");
  const errorMotivo = document.getElementById("error-motivo-rechazo");

  if (confirmTitle) confirmTitle.textContent = titulo;
  if (confirmMessage) confirmMessage.textContent = mensaje;
  if (btnConfirmAceptar) {
    btnConfirmAceptar.innerHTML = tipo === "aprobar" 
      ? `<i class="fa-solid fa-check"></i> ${textoBoton}`
      : `<i class="fa-solid fa-xmark"></i> ${textoBoton}`;
  }

  if (tipo === "aprobar") {
    if (confirmIconBadge) confirmIconBadge.className = "confirm-icon-badge aprobar";
    if (confirmIcon) confirmIcon.className = "fa-solid fa-circle-check";
    if (btnConfirmAceptar) btnConfirmAceptar.className = "btn-aprobar";
    if (wrapperMotivo) wrapperMotivo.classList.add("hidden");
  } else {
    if (confirmIconBadge) confirmIconBadge.className = "confirm-icon-badge rechazar";
    if (confirmIcon) confirmIcon.className = "fa-solid fa-circle-xmark";
    if (btnConfirmAceptar) btnConfirmAceptar.className = "btn-rechazar";
    if (wrapperMotivo) wrapperMotivo.classList.remove("hidden");
    if (inputMotivo) inputMotivo.value = "";
    if (errorMotivo) errorMotivo.classList.add("hidden");
  }

  accionPendiente = () => {
    if (tipo === "rechazar") {
      const val = inputMotivo ? inputMotivo.value.trim() : "";
      if (val.length < 5) {
        if (errorMotivo) errorMotivo.classList.remove("hidden");
        if (inputMotivo) inputMotivo.focus();
        return false; // NO cerrar modal hasta ingresar motivo
      }
      if (errorMotivo) errorMotivo.classList.remove("hidden");
      onAceptar(val);
      return true; // cerrar modal
    } else {
      onAceptar();
      return true;
    }
  };

  if (modalConfirmacion) modalConfirmacion.classList.remove("hidden");
  document.body.style.overflow = "hidden";
}

function cerrarConfirmacion() {
  if (modalConfirmacion) modalConfirmacion.classList.add("hidden");
  document.body.style.overflow = "auto";
  accionPendiente = null;
}

// =========================================================
// ACCIÓN: APROBAR SOLICITUD
// =========================================================
function aprobarSolicitud(id) {
  const solicitud = solicitudesData.find(s => s.id === id);
  const nombre = solicitud ? solicitud.nombreEmprendimiento : `#${id}`;

  mostrarConfirmacion({
    titulo: "Aprobar Solicitud",
    mensaje: `¿Confirmas que deseas APROBAR la solicitud del emprendimiento "${nombre}" y otorgarle el perfil de Feriante?`,
    textoBoton: "Aprobar Feriante",
    tipo: "aprobar",
    onAceptar: async () => {
      try {
        const response = await axios.post(`/api/solicitudes/aprobar/${id}`);
        const mensajeExito = response.data?.mensaje || response.data || "✅ Feriante aprobado correctamente.";
        showToast(mensajeExito, "success");
        cargarSolicitudes();
      } catch (error) {
        console.error("Error al aprobar:", error);
        const mensajeError = error.response?.data?.error || error.response?.data || "❌ No se pudo aprobar la solicitud.";
        showToast(mensajeError, "error");
      }
    }
  });
}

// =========================================================
// ACCIÓN: RECHAZAR SOLICITUD (CON MOTIVO)
// =========================================================
function rechazarSolicitud(id) {
  const solicitud = solicitudesData.find(s => s.id === id);
  const nombre = solicitud ? solicitud.nombreEmprendimiento : `#${id}`;

  mostrarConfirmacion({
    titulo: "Rechazar Solicitud",
    mensaje: `Indica la razón por la cual desestimas la postulación del emprendimiento "${nombre}".`,
    textoBoton: "Rechazar Solicitud",
    tipo: "rechazar",
    onAceptar: async (motivo) => {
      try {
        const response = await axios.post(`/api/solicitudes/rechazar/${id}`, {
          motivoRechazo: motivo
        });
        const mensajeExito = response.data?.mensaje || response.data || "🗑️ Solicitud rechazada correctamente.";
        showToast(mensajeExito, "success");
        cargarSolicitudes();
      } catch (error) {
        console.error("Error al rechazar:", error);
        const mensajeError = error.response?.data?.error || error.response?.data || "⚠️ No se pudo rechazar la solicitud.";
        showToast(mensajeError, "error");
      }
    }
  });
}

// Helper para evitar inyección XSS
function escapeHtml(text) {
  if (!text) return "";
  return String(text)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}
