/* ============================================================
   PRODUCTO-DETALLE.JS - Versión Final con Respuestas y Fechas
   ============================================================ */

let puntajeSeleccionado = 0;
let usuarioLogueadoId = null; 
let dueñoProductoId = null;   
let nombreStandActual = "";   

document.addEventListener("DOMContentLoaded", async () => {
    const urlParams = new URLSearchParams(window.location.search);
    const productoId = urlParams.get('id');

    if (!productoId) {
        window.location.href = "buscar.html";
        return;
    }

    // 1. Carga inicial sincronizada
    await obtenerUsuarioActual();
    await cargarDatosProducto(productoId); 
    cargarResenas(productoId); 
    configurarEstrellas();
});

// 0. Obtener sesión del usuario
async function obtenerUsuarioActual() {
    try {
        const res = await axios.get("/api/usuarios/current", { withCredentials: true });
        if (res.data) {
            usuarioLogueadoId = res.data.id;
            // Mostrar formulario de reseña solo si es cliente o feriante ajeno
            if (res.data.tipoUsuario === "NORMAL" || res.data.tipoUsuario === "FERIANTE") {
                const sec = document.getElementById("seccion-dejar-resena");
                if (sec) sec.style.display = "block";
            }
        }
    } catch (err) { console.log("Navegando como visitante."); }
}

// 1. Cargar Información del Producto
async function cargarDatosProducto(id) {
    try {
        const res = await axios.get(`/api/productos/${id}`);
        const p = res.data;
        dueñoProductoId = p.usuarioDueñoId;
        nombreStandActual = p.standNombre || "Feriante"; 

        document.getElementById("p-nombre").textContent = p.nombre;
        document.getElementById("p-precio").textContent = `$${p.precio.toLocaleString()}`;
        document.getElementById("p-unidad").textContent = p.tipoVenta === 'UNIDAD' ? '/ unidad' : `/ ${p.unidadMedida}`;
        document.getElementById("p-feria").textContent = p.feriaNombre || "Feria Local";
        document.getElementById("p-categoria").textContent = p.categoriaNombre;
        document.getElementById("p-stand").textContent = p.standNombre || "Stand Autorizado";
        document.getElementById("p-descripcion").textContent = p.descripcion || "Sin descripción.";
        document.getElementById("p-imagen").src = p.imagenUrl || "https://res.cloudinary.com/dklkf0fmq/image/upload/v1769030533/NOT_IMAGE_aypskv.png";

        renderizarEstrellasCabecera(p.promedioEstrellas, p.cantidadResenas);

        // Bloque de aviso para el dueño
        if (usuarioLogueadoId && dueñoProductoId === usuarioLogueadoId) {
            const formContainer = document.getElementById("seccion-dejar-resena");
            if (formContainer) {
                formContainer.innerHTML = `
                    <div class="alert alert-info border-0 shadow-sm rounded-4 p-4 text-center">
                        <i class="bi bi-person-badge fs-2"></i>
                        <p class="mt-2 mb-0 fw-bold">Estás viendo uno de tus productos. Respondé a tus clientes abajo.</p>
                    </div>`;
            }
        }
    } catch (err) { console.error("Error al cargar producto:", err); }
}

// 2. Cargar Reseñas y Respuestas (Con Fotos de Perfil de Usuario y Feriante)
async function cargarResenas(id) {
    const lista = document.getElementById("lista-resenas");
    try {
        const res = await axios.get(`/api/resenas/producto/${id}`);
        lista.innerHTML = "";

        if (res.data.length === 0) {
            lista.innerHTML = `<div class="text-muted small py-3"><i class="bi bi-info-circle"></i> Nadie ha calificado este producto aún.</div>`;
            return;
        }

        res.data.forEach(r => {
            const esMiProducto = usuarioLogueadoId === dueñoProductoId;
            const estrellas = "★".repeat(r.puntaje) + "☆".repeat(5 - r.puntaje);
            
            // Fotos con fallback a imagen por defecto
            const fotoUsuarioUrl = r.fotoPerfil || '/img/default-user.png';
            const fotoFerianteUrl = r.fotoFeriante || '/img/default-user.png';
            
            const labelRespuesta = esMiProducto ? "Tu respuesta:" : `Respuesta de ${nombreStandActual}:`;

            const fechaRespHtml = r.fechaRespuesta 
                ? `<div class="text-end opacity-50" style="font-size: 0.6rem; margin-top: -5px;">
                     Respondido el ${new Date(r.fechaRespuesta).toLocaleString([], {year: 'numeric', month: 'numeric', day: 'numeric', hour: '2-digit', minute:'2-digit'})}
                   </div>` 
                : "";

            const botonesDuenio = esMiProducto ? `
                <div class="mt-2 d-flex gap-2 opacity-75 border-top pt-2">
                    <button class="btn btn-sm btn-link p-0 text-decoration-none small text-primary" onclick="abrirEditorRespuesta(${r.id}, '${r.respuesta}')">
                        <i class="bi bi-pencil"></i> Editar
                    </button>
                    <span class="text-muted">|</span>
                    <button class="btn btn-sm btn-link p-0 text-decoration-none small text-danger" onclick="eliminarRespuesta(${r.id})">
                        <i class="bi bi-trash"></i> Borrar
                    </button>
                </div>` : "";

            const resenaDiv = document.createElement("div");
            resenaDiv.className = "resena-card shadow-sm p-3 mb-3 bg-white rounded-3";
            resenaDiv.innerHTML = `
                <div class="d-flex justify-content-between align-items-start">
                    <div class="d-flex align-items-center gap-2">
                        <img src="${fotoUsuarioUrl}" class="resena-avatar-mini" alt="Usuario">
                        <div class="d-flex flex-column">
                            <span class="fw-bold text-primary" style="line-height: 1;">${r.nombreUsuario}</span>
                            <span class="text-warning" style="font-size: 0.75rem;">${estrellas}</span>
                        </div>
                    </div>
                    <div class="text-end opacity-50" style="font-size: 0.65rem;">
                        ${new Date(r.fecha).toLocaleDateString()}
                    </div>
                </div>

                <p class="my-2 small text-dark ps-1">${r.comentario || 'Sin comentarios.'}</p>
                
                <div id="contenedor-respuesta-${r.id}">
                    ${r.respuesta 
                        ? `<div class="ms-4 p-3 bg-light border-start border-4 border-primary rounded mt-2">
                             <div class="d-flex align-items-center gap-2 mb-2">
                                <img src="${fotoFerianteUrl}" class="resena-avatar-mini" style="width: 30px; height: 30px;" alt="Feriante">
                                <small class="fw-bold text-primary">
                                    <i class="bi bi-reply-fill"></i> ${labelRespuesta}
                                </small>
                             </div>
                             <p class="mb-2 small fst-italic text-dark ps-1">${r.respuesta}</p>
                             ${fechaRespHtml} ${botonesDuenio}
                           </div>`
                        : (esMiProducto ? `<button class="btn btn-sm btn-outline-primary mt-1 ms-4 rounded-pill px-3" onclick="abrirEditorRespuesta(${r.id}, '')">Responder</button>` : "")
                    }
                </div>
            `;
            lista.appendChild(resenaDiv);
        });
    } catch (err) { lista.innerHTML = `<p class="text-danger small">Error de conexión.</p>`; }
}

// 3. Gestión de Respuestas (Responder, Editar, Eliminar)
function abrirEditorRespuesta(resenaId, textoPrevio) {
    const contenedor = document.getElementById(`contenedor-respuesta-${resenaId}`);
    contenedor.innerHTML = `
        <div class="mt-2 ms-4 p-3 bg-light rounded border border-primary">
            <label class="small fw-bold text-primary mb-2">Escribí tu respuesta:</label>
            <textarea id="input-respuesta-${resenaId}" class="form-control form-control-sm mb-2" rows="3">${textoPrevio}</textarea>
            <div class="d-flex gap-2 justify-content-end">
                <button class="btn btn-sm btn-light border" onclick="location.reload()">Cancelar</button>
                <button class="btn btn-sm btn-primary px-3 fw-bold" onclick="enviarRespuesta(${resenaId})">Guardar</button>
            </div>
        </div>
    `;
}

async function enviarRespuesta(resenaId) {
    const texto = document.getElementById(`input-respuesta-${resenaId}`).value.trim();
    if (!texto) {
        Toastify({ text: "⚠️ Escribí algo antes de guardar.", background: "#dc3545" }).showToast();
        return;
    }
    try {
        await axios.put(`/api/resenas/${resenaId}/responder`, texto, {
            headers: { 'Content-Type': 'text/plain' }, withCredentials: true
        });
        Toastify({ text: "✅ Respuesta guardada", background: "#1a3a5a" }).showToast();
        setTimeout(() => location.reload(), 1000);
    } catch (err) {
        Toastify({ text: "❌ Error al guardar respuesta", background: "#dc3545" }).showToast();
    }
}

async function eliminarRespuesta(resenaId) {
    if (!confirm("¿Seguro que querés eliminar tu respuesta?")) return;
    try {
        await axios.delete(`/api/resenas/${resenaId}/respuesta`, { withCredentials: true });
        Toastify({ text: "🗑️ Respuesta eliminada", background: "#dc3545" }).showToast();
        setTimeout(() => location.reload(), 1000);
    } catch (err) {
        Toastify({ text: "❌ No se pudo eliminar", background: "#dc3545" }).showToast();
    }
}

// 4. Estrellas y Visuales
function renderizarEstrellasCabecera(promedio, cantidad) {
    const contenedor = document.getElementById("promedio-estrellas-container");
    if (!contenedor || !cantidad) return;
    let estrellasHtml = "";
    for (let i = 1; i <= 5; i++) {
        if (i <= Math.floor(promedio)) estrellasHtml += '<i class="bi bi-star-fill text-warning me-1"></i>';
        else if (i - 0.5 <= promedio) estrellasHtml += '<i class="bi bi-star-half text-warning me-1"></i>';
        else estrellasHtml += '<i class="bi bi-star text-warning me-1"></i>';
    }
    contenedor.innerHTML = `
        <div class="d-flex align-items-center gap-2">
            <div class="fs-5">${estrellasHtml}</div>
            <span class="fw-bold mt-1">${promedio.toFixed(1)}</span>
            <span class="text-muted small mt-1">(${cantidad} opiniones)</span>
        </div>`;
}

function configurarEstrellas() {
    const stars = document.querySelectorAll(".star-btn");
    stars.forEach(s => {
        s.addEventListener("click", () => {
            puntajeSeleccionado = parseInt(s.dataset.value);
            document.querySelectorAll(".star-btn").forEach(st => {
                const val = parseInt(st.dataset.value);
                st.classList.toggle("bi-star-fill", val <= puntajeSeleccionado);
                st.classList.toggle("bi-star", val > puntajeSeleccionado);
            });
        });
    });
}

// 5. Enviar Reseña de Cliente
async function enviarResena() {
    const texto = document.getElementById("comentario-texto").value.trim();
    const urlParams = new URLSearchParams(window.location.search);
    const productoId = parseInt(urlParams.get('id'));
    if (puntajeSeleccionado === 0) {
        Toastify({ text: "¡Seleccioná las estrellas!", background: "#dc3545" }).showToast();
        return;
    }
    try {
        await axios.post("/api/resenas", {
            puntaje: puntajeSeleccionado, comentario: texto, producto: { id: productoId }
        }, { withCredentials: true });
        Toastify({ text: "¡Gracias por tu opinión!", background: "#198754" }).showToast();
        setTimeout(() => location.reload(), 1500);
    } catch (err) {
        const errorMsg = err.response ? err.response.data : "Error al publicar.";
        Toastify({ text: errorMsg, background: "#dc3545" }).showToast();
    }
}