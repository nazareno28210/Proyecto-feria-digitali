let puntajeSeleccionado = 0;

document.addEventListener("DOMContentLoaded", async () => {
    const urlParams = new URLSearchParams(window.location.search);
    const productoId = urlParams.get('id');

    if (!productoId) {
        window.location.href = "buscar.html";
        return;
    }

    // Ejecutar carga inicial
    cargarDatosProducto(productoId);
    cargarResenas(productoId);
    verificarPermisoComentar();
    configurarEstrellas();
});

// 1. Cargar Info del Producto
async function cargarDatosProducto(id) {
    try {
        const res = await axios.get(`/api/productos/${id}`);
        const p = res.data;

        // 1. Llenamos el HTML
        document.getElementById("p-nombre").textContent = p.nombre;
        document.getElementById("p-precio").textContent = `$${p.precio.toLocaleString()}`;
        document.getElementById("p-unidad").textContent = p.tipoVenta === 'UNIDAD' ? '/ unidad' : `/ ${p.unidadMedida}`;
        document.getElementById("p-feria").textContent = p.feriaNombre || "Feria Local";
        document.getElementById("p-categoria").textContent = p.categoriaNombre;
        document.getElementById("p-stand").textContent = p.standNombre || "Stand Autorizado";
        document.getElementById("p-descripcion").textContent = p.descripcion || "Sin descripción.";
        document.getElementById("p-imagen").src = p.imagenUrl || "https://res.cloudinary.com/dklkf0fmq/image/upload/v1769030533/NOT_IMAGE_aypskv.png";

        // ⭐ DIBUJAR PROMEDIO (Esto arregla la visual de la cabecera)
        renderizarEstrellasCabecera(p.promedioEstrellas, p.cantidadResenas);

        // 2. Verificar dueño para el formulario
        const userRes = await axios.get("/api/usuarios/current", { withCredentials: true });
        const user = userRes.data;

        if (user && p.usuarioDueñoId === user.id) {
            const formContainer = document.getElementById("seccion-dejar-resena");
            formContainer.style.display = "block"; 
            formContainer.innerHTML = `
                <div class="alert alert-info border-0 shadow-sm rounded-4 p-4 text-center">
                    <i class="bi bi-person-badge fs-2"></i>
                    <p class="mt-2 mb-0 fw-bold">Estás viendo uno de tus productos.</p>
                </div>`;
        }
    } catch (err) {
        console.error("Error al cargar datos:", err);
    }
}

// ⭐ Función para las estrellas del promedio
function renderizarEstrellasCabecera(promedio, cantidad) {
    const contenedor = document.getElementById("promedio-estrellas-container");
    if (!contenedor || !cantidad) {
        contenedor.innerHTML = '<span class="text-muted small">Sin calificaciones aún</span>';
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
            <span class="fw-bold mt-1">${promedio.toFixed(1)}</span>
            <span class="text-muted small mt-1">(${cantidad} opiniones)</span>
        </div>`;
}

// ⭐ NUEVA FUNCIÓN: Dibuja las estrellas de calificación general
function renderizarEstrellasCabecera(promedio, cantidad) {
    const contenedor = document.getElementById("promedio-estrellas-container");
    if (!contenedor) return;

    if (!cantidad || cantidad === 0) {
        contenedor.innerHTML = '<span class="text-muted small">Sin calificaciones aún</span>';
        return;
    }

    let estrellasHtml = "";
    // Lógica para estrellas llenas, medias o vacías
    for (let i = 1; i <= 5; i++) {
        if (i <= Math.floor(promedio)) {
            // Estrella llena
            estrellasHtml += '<i class="bi bi-star-fill text-warning me-1"></i>';
        } else if (i - 0.5 <= promedio) {
            // Media estrella
            estrellasHtml += '<i class="bi bi-star-half text-warning me-1"></i>';
        } else {
            // Estrella vacía
            estrellasHtml += '<i class="bi bi-star text-warning me-1"></i>';
        }
    }

    contenedor.innerHTML = `
        <div class="d-flex align-items-center gap-2">
            <div class="fs-5">${estrellasHtml}</div>
            <span class="fw-bold mt-1">${promedio.toFixed(1)}</span>
            <span class="text-muted small mt-1">(${cantidad} ${cantidad === 1 ? 'opinión' : 'opiniones'})</span>
        </div>
    `;
}

// 2. Sistema de Calificación Visual (Selector para nuevas reseñas)
function configurarEstrellas() {
    const stars = document.querySelectorAll(".star-btn");
    stars.forEach(s => {
        s.addEventListener("click", () => {
            puntajeSeleccionado = parseInt(s.dataset.value);
            actualizarEstrellas(puntajeSeleccionado);
        });
    });
}

function actualizarEstrellas(valor) {
    document.querySelectorAll(".star-btn").forEach(s => {
        const val = parseInt(s.dataset.value);
        s.classList.toggle("bi-star-fill", val <= valor);
        s.classList.toggle("bi-star", val > valor);
    });
}

// 3. Verificación de Rol
async function verificarPermisoComentar() {
    try {
        const res = await axios.get("/api/usuarios/current", { withCredentials: true });
        const user = res.data;
        
        // Si es NORMAL o FERIANTE (y no el dueño), mostramos el formulario
        if (user && (user.tipoUsuario === "NORMAL" || user.tipoUsuario === "FERIANTE")) {
            // El display block aquí es genérico, la lógica de "dueño" dentro de cargarDatosProducto tiene prioridad
            document.getElementById("seccion-dejar-resena").style.display = "block";
        }
    } catch (err) {
        console.log("Modo visitante o error de sesión.");
    }
}

// 4. Cargar Reseñas Existentes (Listado inferior)
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
            const estrellas = "★".repeat(r.puntaje) + "☆".repeat(5 - r.puntaje);
            lista.innerHTML += `
                <div class="resena-card shadow-sm p-3 mb-3">
                    <div class="d-flex justify-content-between">
                        <span class="fw-bold text-primary">${r.nombreUsuario}</span>
                        <span class="text-warning small">${estrellas}</span>
                    </div>
                    <p class="my-2 small text-dark">${r.comentario || 'Sin comentarios adicionales.'}</p>
                    <div class="text-end opacity-50" style="font-size: 0.65rem;">
                        ${new Date(r.fecha).toLocaleDateString()}
                    </div>
                </div>`;
        });
    } catch (err) {
        lista.innerHTML = `<p class="text-danger small">Error al conectar con el servidor de reseñas.</p>`;
    }
}

// 5. Enviar Nueva Reseña
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
            puntaje: puntajeSeleccionado,
            comentario: texto,
            producto: { id: productoId }
        }, { withCredentials: true });

        Toastify({ text: "¡Gracias por tu opinión!", background: "#198754" }).showToast();
        setTimeout(() => location.reload(), 1500);
    } catch (err) {
        // Capturamos el mensaje de error que viene del Backend (malas palabras, autoreseña, etc)
        const errorMsg = err.response ? err.response.data : "Error al publicar.";
        Toastify({ text: errorMsg, background: "#dc3545" }).showToast();
    }
}