/*
 * ====================================
 * STAND-DETALLE.JS (Versión con Calificaciones)
 * ====================================
 */

let puntajeStand = 0; // Variable global para el selector de estrellas

const API_URL = "/api/stands"; 
const params = new URLSearchParams(window.location.search); 
const standId = params.get("idStand"); 

async function cargarStand() {
    try {
        const response = await axios.get(`${API_URL}/${standId}`);
        const stand = response.data;

        // 1. Llenar la sección de información del Stand
        const infoStand = document.getElementById("info-stand");
        if (infoStand) {
            infoStand.innerHTML = `
                <div class="info-item">
                    <i class="fas fa-store"></i>
                    <div class="info-item-content">
                        <strong>Nombre del Stand</strong>
                        <span>${stand.nombre}</span>
                    </div>
                </div>
                <div class="info-item">
                    <i class="fas fa-user-tag"></i>
                    <div class="info-item-content">
                        <strong>Feriante</strong>
                        <span>${stand.feriante ? stand.feriante.nombreEmprendimiento : "No asignado"}</span>
                    </div>
                </div>
                <div class="info-item">
                    <i class="fas fa-info-circle"></i>
                    <div class="info-item-content">
                        <strong>Descripción</strong>
                        <span>${stand.descripcion || "Sin descripción"}</span>
                    </div>
                </div>
            `; 
        }

        const nombreHeader = document.getElementById("nombre-stand");
        if (nombreHeader) nombreHeader.textContent = stand.nombre;

        // ⭐ NUEVO: Renderizar promedio de estrellas en la cabecera
        renderizarPromedioStand(stand.promedioEstrellas, stand.cantidadResenas);

        // 🟢 NUEVO: Configurar el selector de estrellas y permisos
        configurarEstrellasStand();
        verificarAccesoCalificacion(stand.usuarioDueñoId);

        const productosContainer = document.getElementById("productos-container"); 
        if (!productosContainer) return;
        productosContainer.innerHTML = ""; 

        // 🟢 LÓGICA DE PROTECCIÓN: Stand desactivado
        if (!stand.activo) {
            productosContainer.innerHTML = `
                <div class="mensaje-cerrado" style="grid-column: 1 / -1; text-align: center; padding: 50px; background: #fff5f5; border: 2px dashed #feb2b2; border-radius: 15px; color: #c53030; margin-top: 20px;">
                    <i class="fas fa-store-slash" style="font-size: 3rem; margin-bottom: 15px;"></i>
                    <h3 style="margin-bottom: 10px; font-size: 1.5rem;">Este stand se encuentra cerrado temporalmente</h3>
                    <p style="font-size: 1.1rem;">El feriante ha pausado la visibilidad de sus productos.</p>
                </div>
            `;
            return; 
        }

        // 2. Renderizar los productos si el stand está activo 
        if (stand.productos && stand.productos.length > 0) {
        stand.productos.forEach(producto => {
            const div = document.createElement("div");
            div.classList.add("producto-card"); 
            div.onclick = () => window.location.href = `producto-detalle.html?id=${producto.id}`;

            const imagenUrl = producto.imagenUrl || "https://res.cloudinary.com/dklkf0fmq/image/upload/v1769030533/NOT_IMAGE_aypskv.png"; 
            
            div.innerHTML = `
                <img src="${imagenUrl}" alt="${producto.nombre}" class="producto-img">
                <div class="producto-card-content">
                    <div class="producto-header-row">
                        <h3 class="producto-titulo">${producto.nombre}</h3>
                        <div class="badges-container">
                            <span class="badge-categoria">${producto.categoriaNombre || "Sin categoría"}</span>
                            <span class="badge-unidad">${producto.tipoVenta || "UNIDAD"}</span>
                        </div>
                    </div>
                    <p class="producto-desc">${producto.descripcion || "Sin descripción"}</p>
                    <div class="producto-precio-info">
                        <strong class="precio-texto">$${producto.precio.toFixed(2)}</strong>
                    </div>
                </div>
            `; 
            productosContainer.appendChild(div); 
    });
        } else {
            productosContainer.innerHTML = `
                <div class="no-products-container" style="grid-column: 1 / -1; text-align: center; padding: 50px;">
                    <p class='no-products-msg' style="font-size: 1.2rem; color: #666;">
                        Este feriante no tiene productos disponibles actualmente.
                    </p>
                </div>`; 
        }

    } catch (error) {
        console.error("Error al cargar el stand:", error); 
        mostrarNotificacion("Error al cargar los datos del stand.", "error");
    }
}

// ⭐ Función para dibujar las estrellas del promedio en el Header
function renderizarPromedioStand(promedio, cantidad) {
    const contenedor = document.getElementById("promedio-estrellas-stand");
    if (!contenedor) return;

    if (!cantidad || cantidad === 0) {
        contenedor.innerHTML = '<span class="text-muted small" style="color: #eee !important;">Sin calificaciones aún</span>';
        return;
    }

    let estrellasHtml = "";
    for (let i = 1; i <= 5; i++) {
        if (i <= Math.floor(promedio)) {
            estrellasHtml += '<i class="bi bi-star-fill text-warning me-1"></i>';
        } else if (i - 0.5 <= promedio) {
            estrellasHtml += '<i class="bi bi-star-half text-warning me-1"></i>';
        } else {
            estrellasHtml += '<i class="bi bi-star text-warning me-1"></i>';
        }
    }

    contenedor.innerHTML = `
        <div class="d-flex align-items-center gap-2">
            <div>${estrellasHtml}</div>
            <span class="fw-bold text-white">${promedio.toFixed(1)}</span>
            <span class="text-white small opacity-75">(${cantidad} votos)</span>
        </div>`;
}

// ⭐ Manejo interactivo de las estrellas para votar
function configurarEstrellasStand() {
    const stars = document.querySelectorAll(".star-stand-btn");
    stars.forEach(s => {
        s.addEventListener("click", () => {
            puntajeStand = parseInt(s.dataset.value);
            stars.forEach(st => {
                const val = parseInt(st.dataset.value);
                st.classList.toggle("bi-star-fill", val <= puntajeStand);
                st.classList.toggle("bi-star", val > puntajeStand);
            });
        });
    });
}

// ⭐ Enviar la reseña de Stand al servidor
async function enviarResenaStand() {
    if (puntajeStand === 0) {
        mostrarNotificacion("Por favor, selecciona un puntaje.", "warning");
        return;
    }

    try {
        await axios.post("/api/resenas-stand", {
            stand_id: parseInt(standId),
            puntaje: puntajeStand
        }, { withCredentials: true });

        if (typeof Swal !== 'undefined') {
            Swal.fire({
                icon: 'success',
                title: '¡Gracias por tu calificación!',
                text: 'Tu puntaje fue registrado.',
                timer: 2000,
                showConfirmButton: false
            });
        } else {
            mostrarNotificacion("Gracias por tu calificación.", "success");
        }
        cargarStand();
    } catch (err) {
        if (err.response && err.response.status === 403) {
            const msg = typeof err.response.data === 'string' ? err.response.data : "No podés calificar tu propio Stand.";
            if (typeof Swal !== 'undefined') Swal.fire('Acceso Restringido', msg, 'warning');
            else mostrarNotificacion(msg, 'warning');
        } else if (err.response && err.response.status === 409) {
            if (typeof Swal !== 'undefined') Swal.fire('Calificación Duplicada', 'Ya calificaste este Stand.', 'info');
            else mostrarNotificacion('Ya calificaste este Stand.', 'info');
        } else {
            const msg = obtenerMensajeError(err, "Error al calificar.");
            if (typeof Swal !== 'undefined') Swal.fire('Error', msg, 'error');
            else mostrarNotificacion(msg, 'error');
        }
    }
}

async function enviarCalificacionStand() {
    await enviarResenaStand();
}


// ⭐ Verificar si el usuario puede calificar (logueado y no es dueño)
async function verificarAccesoCalificacion(usuarioDueñoId) {
    try {
        const res = await axios.get("/api/usuarios/current", { withCredentials: true });
        const user = res.data;

        if (user) {
            const seccion = document.getElementById("seccion-calificar-stand");
            if (user.id === usuarioDueñoId) {
                seccion.style.display = "block";
                seccion.innerHTML = `
                    <div class="alert alert-light border text-center p-3">
                        <i class="fas fa-user-cog"></i> Estás viendo tu propio stand.
                    </div>`;
            } else {
                seccion.style.display = "block";
            }
        }
    } catch (e) {
        console.log("Modo visitante: formulario oculto.");
    }
}

function volver() {
    if (document.referrer && document.referrer.includes(window.location.host)) {
        window.history.back();
    } else {
        window.location.href = "/web/ferias.html";
    }
}

document.addEventListener("DOMContentLoaded", cargarStand);