/* ============================================================
   PRODUCTO-DETALLE.JS - Galería Dinámica con Zoom Pro y Panning
   ============================================================ */

let puntajeSeleccionado = 0;
let usuarioLogueadoId = null; 
let dueñoProductoId = null;   
let nombreStandActual = "";   
let imagenesTotales = []; // 🟢 Array global para navegar las fotos [cite: 2]

// Placeholder oficial para Feria Digital si la imagen falla [cite: 3]
const imgPortadaDefault = "https://res.cloudinary.com/dklkf0fmq/image/upload/v1741823733/NOT_IMAGE_aypskv.png";

document.addEventListener("DOMContentLoaded", async () => {
    const urlParams = new URLSearchParams(window.location.search);
    const productoId = urlParams.get('id');

    if (!productoId) {
        window.location.href = "buscar.html";
        return;
    }

    // Inicializamos el Modal de Bootstrap para el Zoom
    const miModalZoom = new bootstrap.Modal(document.getElementById('modalZoom'));

    await obtenerUsuarioActual();
    await cargarDatosProducto(productoId); 
    cargarResenas(productoId); 
    configurarEstrellas();

    // 🟢 LÓGICA DE ZOOM INTERACTIVO Y PANNING
    const imgPrincipal = document.getElementById("p-imagen");
    const imgZoom = document.getElementById("img-zoom");

    // Variables de estado del zoom
    let isZoomed = false;
    let isDragging = false;
    let startX, startY;
    let currentTranslateX = 0, currentTranslateY = 0;
    let lastTranslateX = 0, lastTranslateY = 0;
    const zoomScale = 1.8; // Escala de zoom (e.g., 1.8x)

    // 1. Abrir Modal en el clic [cite: 23-25]
    imgPrincipal.onclick = function() {
        const urlActual = this.src;
        
        // Evitamos abrir el zoom si es la imagen por defecto
        if(urlActual && !urlActual.includes("NOT_IMAGE")) {
            imgZoom.src = urlActual; // Pasamos la URL al modal
            
            // Reiniciamos estado antes de mostrar
            isZoomed = false;
            currentTranslateX = 0; currentTranslateY = 0;
            lastTranslateX = 0; lastTranslateY = 0;
            imgZoom.style.transformOrigin = "center center";
            imgZoom.style.transform = "translate(0, 0) scale(1)";
            
            miModalZoom.show();      // Mostramos el modal
        } else {
            showToast("📸 No hay una imagen disponible para ampliar", "info");
        }
    };

    // 2. Lógica de Zoom Interactivo en clic dentro del Modal
    imgZoom.onclick = function(event) {
        isZoomed = !isZoomed;

        if (isZoomed) {
            // Calculamos el origen de transformación dinámico basado en el clic
            const { offsetX, offsetY } = event;
            const originX = (offsetX / this.width) * 100;
            const originY = (offsetY / this.height) * 100;

            // 

            // Aplicamos zoom en el punto del clic
            this.style.transformOrigin = `${originX}% ${originY}%`;
            this.style.transform = `scale(${zoomScale})`;
            this.style.cursor = "zoom-out";
            
            currentTranslateX = 0; currentTranslateY = 0; // Reiniciamos tras zoom
            showToast("💡 Podés arrastrar la imagen para moverte", "success");
        } else {
            // Zoom out: reset transformaciones
            this.style.transformOrigin = "center center";
            this.style.transform = "translate(0, 0) scale(1)";
            this.style.cursor = "zoom-in";
            
            currentTranslateX = 0; currentTranslateY = 0;
            lastTranslateX = 0; lastTranslateY = 0;
        }
    };

    // 3. Lógica de Panning (Arrastrar) cuando hay zoom
    imgZoom.onmousedown = function(event) {
        if (!isZoomed) return; // Solo si hay zoom
        
        isDragging = true;
        this.style.cursor = "grabbing";
        
        // Coordenadas iniciales del clic
        startX = event.clientX;
        startY = event.clientY;
        
        // Posición translate actual
        lastTranslateX = currentTranslateX;
        lastTranslateY = currentTranslateY;
    };

    imgZoom.onmousemove = function(event) {
        if (!isDragging) return; // Solo si estamos arrastrando
        
        // Evitamos comportamiento por defecto (selección de texto, etc.)
        event.preventDefault();
        
        const deltaX = event.clientX - startX;
        const deltaY = event.clientY - startY;
        
        // Actualizamos translate
        currentTranslateX = lastTranslateX + deltaX;
        currentTranslateY = lastTranslateY + deltaY;

        // 

        // --- 🟢 LÓGICA DE LÍMITES DE DESPLAZAMIENTO ---
        // Obtenemos dimensiones escaladas de la imagen y del contenedor
        const scaledWidth = this.naturalWidth * zoomScale;
        const scaledHeight = this.naturalHeight * zoomScale;
        const containerWidth = this.parentElement.offsetWidth;
        const containerHeight = this.parentElement.offsetHeight;

        // Calculamos los límites máximos de desplazamiento
        // Si el contenedor es mayor que la imagen escalada (no debería pasar con object-fit), no limitamos
        const maxXTranslate = Math.max(0, (scaledWidth - containerWidth) / 2);
        const maxYTranslate = Math.max(0, (scaledHeight - containerHeight) / 2);

        // Clampeamos el desplazamiento dentro de los límites
        currentTranslateX = Math.max(-maxXTranslate, Math.min(maxXTranslate, currentTranslateX));
        currentTranslateY = Math.max(-maxYTranslate, Math.min(maxYTranslate, currentTranslateY));

        // Aplicamos el translate y mantenemos la escala
        this.style.transform = `translate(${currentTranslateX}px, ${currentTranslateY}px) scale(${zoomScale})`;
    };

    imgZoom.onmouseup = imgZoom.onmouseleave = function() {
        if (!isZoomed || !isDragging) return;
        
        isDragging = false;
        this.style.cursor = "zoom-out";
    };

});

// 0. Obtener sesión del usuario (Se mantiene igual)
async function obtenerUsuarioActual() {
    try {
        const res = await axios.get("/api/usuarios/current", { withCredentials: true });
        if (res.data) {
            usuarioLogueadoId = res.data.id;
            // Mostrar formulario solo a clientes o feriantes ajenos
            if (res.data.tipoUsuario === "NORMAL" || res.data.tipoUsuario === "FERIANTE") {
                const sec = document.getElementById("seccion-dejar-resena");
                if (sec) sec.style.display = "block";
            }
        }
    } catch (err) { console.log("Navegando como visitante."); }
}

// 1. Cargar Información del Producto (Se mantiene igual) [cite: 50]
async function cargarDatosProducto(id) {
    try {
        const res = await axios.get(`/api/productos/${id}`);
        const p = res.data;
        dueñoProductoId = p.usuarioDueñoId;
        nombreStandActual = p.standNombre || "Feriante"; 

        // Inyección de textos básicos [cite: 51-52]
        document.getElementById("p-nombre").textContent = p.nombre;
        document.getElementById("p-precio").textContent = `$${p.precio.toLocaleString()}`;
        document.getElementById("p-unidad").textContent = p.tipoVenta === 'UNIDAD' ? '/ unidad' : `/ ${p.unidadMedida}`;
        document.getElementById("p-feria").textContent = p.feriaNombre || "Feria Local";
        document.getElementById("p-categoria").textContent = p.categoriaNombre;
        document.getElementById("p-stand").textContent = p.standNombre || "Stand Autorizado";
        document.getElementById("p-descripcion").textContent = p.descripcion || "Sin descripción.";

        // 📸 GESTIÓN DE GALERÍA (ESTILO MERCADO LIBRE) [cite: 58-61]
        // Armamos el array único: Portada + Fotos de la Galería
        imagenesTotales = [p.imagenUrl || imgPortadaDefault];
        if (p.galeria && p.galeria.length > 0) {
            p.galeria.forEach(imgObj => imagenesTotales.push(imgObj.url));
        }

        renderizarGaleria();
        renderizarEstrellasCabecera(p.promedioEstrellas, p.cantidadResenas);

        // Bloque de aviso para el dueño [cite: 63]
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

// 🟢 Dibuja las miniaturas y configura el visor [cite: 71-92] (Se mantiene igual)
function renderizarGaleria() {
    const contenedorMiniaturas = document.getElementById("p-galeria-miniaturas");
    const imgPrincipal = document.getElementById("p-imagen");
    const indicador = document.getElementById("indicador-foto");
    const totalFotosLabel = document.getElementById("foto-total");

    contenedorMiniaturas.innerHTML = "";
    imgPrincipal.src = imagenesTotales[0]; 

    if (imagenesTotales.length > 1) {
        indicador.style.display = "block";
        totalFotosLabel.textContent = imagenesTotales.length;

        imagenesTotales.forEach((url, index) => {
            const div = document.createElement("div");
            div.className = `miniatura-item ${index === 0 ? 'active' : ''}`;
            div.innerHTML = `<img src="${url}" alt="Foto ${index + 1}">`;
            
            // Al hacer clic, cambia la imagen grande
            div.onclick = () => cambiarImagenPrincipal(url, index, div);
            contenedorMiniaturas.appendChild(div);
        });
    } else {
        indicador.style.display = "none";
    }
}

// 🟢 Cambia la imagen con efecto de opacidad (Se mantiene igual)
function cambiarImagenPrincipal(url, index, elementoClicado) {
    const imgPrincipal = document.getElementById("p-imagen");
    const fotoActualLabel = document.getElementById("foto-actual");

    imgPrincipal.style.opacity = "0.5"; // Inicio de transición
    
    setTimeout(() => {
        imgPrincipal.src = url;
        imgPrincipal.style.opacity = "1";
        fotoActualLabel.textContent = index + 1;
    }, 150);

    // Actualizar clase 'active' para resaltar la miniatura seleccionada
    document.querySelectorAll(".miniatura-item").forEach(item => item.classList.remove("active"));
    elementoClicado.classList.add("active");
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