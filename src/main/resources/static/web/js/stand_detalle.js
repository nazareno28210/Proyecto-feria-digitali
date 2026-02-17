/*
 * ====================================
 * STAND-DETALLE.JS (Versión con Calificaciones)
 * ====================================
 */

let puntajeStand = 0; // Variable global para el selector de estrellas

function showToast(message, type = "info") {
    let color;
    switch (type) {
        case "success": color = "linear-gradient(to right, #1a3a5a, #3b82f6)"; break;
        case "error": color = "linear-gradient(to right, #ef4444, #b91c1c)"; break; 
        case "warning": color = "linear-gradient(to right, #3b82f6, #67e8f9)"; break; 
        default: color = "linear-gradient(to right, #3b82f6, #67e8f9)"; 
    }
    Toastify({
        text: message,
        duration: 4000,
        gravity: "top",
        position: "right",
        style: { background: color },
        stopOnFocus: true,
    }).showToast(); 
}

const API_URL = "http://localhost:8080/api/stands"; 
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
                div.style.cursor = "pointer";
                div.onclick = () => window.location.href = `producto-detalle.html?id=${producto.id}`;

                const imagenUrl = producto.imagenUrl ? producto.imagenUrl : "https://res.cloudinary.com/dklkf0fmq/image/upload/v1769030533/NOT_IMAGE_aypskv.png"; 
                
                div.innerHTML = `
                    <img src="${imagenUrl}" alt="${producto.nombre}" style="width: 100%; height: 150px; object-fit: cover; border-radius: 8px 8px 0 0;">
                    <div class="producto-card-content">
                        <h3>${producto.nombre}</h3>
                        <p>${producto.descripcion || "Sin descripción"}</p>
                        <p><strong>Precio:</strong> $${producto.precio.toFixed(2)}</p>
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
        showToast("❌ Error al cargar los datos del stand.", "error"); 
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

// ⭐ Enviar la calificación al servidor
async function enviarCalificacionStand() {
    if (puntajeStand === 0) {
        showToast("⚠️ Por favor, seleccioná un puntaje.", "warning");
        return;
    }

    try {
        await axios.post("/api/resenas", {
            puntaje: puntajeStand,
            stand: { id: parseInt(standId) }
        }, { withCredentials: true });

        showToast("⭐ ¡Gracias por tu calificación!", "success");
        setTimeout(() => location.reload(), 1500);
    } catch (err) {
        const msg = err.response ? err.response.data : "Error al calificar.";
        showToast(`❌ ${msg}`, "error");
    }
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

function volver() { window.history.back(); }

document.addEventListener("DOMContentLoaded", cargarStand);