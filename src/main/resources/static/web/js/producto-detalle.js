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
        // 1. Traemos los datos del producto
        const res = await axios.get(`/api/productos/${id}`);
        const p = res.data;

        // 2. Llenamos el HTML (esto quita el "Cargando...")
        document.getElementById("p-nombre").textContent = p.nombre;
        document.getElementById("p-precio").textContent = `$${p.precio.toLocaleString()}`;
        document.getElementById("p-unidad").textContent = p.tipoVenta === 'UNIDAD' ? '/ unidad' : `/ ${p.unidadMedida}`;
        document.getElementById("p-feria").textContent = p.feriaNombre || "Feria Local";
        document.getElementById("p-categoria").textContent = p.categoriaNombre;
        document.getElementById("p-stand").textContent = p.standNombre || "Stand Autorizado";
        document.getElementById("p-descripcion").textContent = p.descripcion || "Sin descripción.";
        document.getElementById("p-imagen").src = p.imagenUrl || "https://res.cloudinary.com/dklkf0fmq/image/upload/v1769030533/NOT_IMAGE_aypskv.png";

        // 3. Verificamos si es el dueño para ocultar el formulario
        const userRes = await axios.get("/api/usuarios/current", { withCredentials: true });
        const user = userRes.data;

        if (user && p.usuarioDueñoId === user.id) {
            const formContainer = document.getElementById("seccion-dejar-resena");
            formContainer.style.display = "block"; // Lo mostramos pero lo cambiamos
            formContainer.innerHTML = `
                <div class="alert alert-info border-0 shadow-sm rounded-4 p-4 text-center">
                    <i class="bi bi-person-badge fs-2"></i>
                    <p class="mt-2 mb-0 fw-bold">Estás viendo uno de tus productos.</p>
                    <small>No podés calificar tus propios artículos.</small>
                </div>`;
        } else if (user && (user.tipoUsuario === "NORMAL" || user.tipoUsuario === "FERIANTE")) {
            // Si no es el dueño pero es usuario, mostramos el formulario normal
            document.getElementById("seccion-dejar-resena").style.display = "block";
        }

    } catch (err) {
        console.error("Error al cargar datos:", err);
    }
}

// 2. Sistema de Calificación Visual
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

// 3. Verificación de Rol (Solo NORMAL comenta)
async function verificarPermisoComentar() {
    try {
        const res = await axios.get("/api/usuarios/current", { withCredentials: true });
        const user = res.data;
        
        // 🟢 AHORA: Si es NORMAL o FERIANTE, puede ver el formulario
        if (user && (user.tipoUsuario === "NORMAL" || user.tipoUsuario === "FERIANTE")) {
            document.getElementById("seccion-dejar-resena").style.display = "block";
        }
    } catch (err) {
        console.log("Modo visitante o error: formulario oculto.");
    }
}

// 4. Cargar Reseñas Existentes
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
    const productoId = parseInt(urlParams.get('id')); // 🟢 Convertimos a número entero

    if (puntajeSeleccionado === 0) {
        Toastify({ text: "¡Seleccioná las estrellas!", background: "#dc3545" }).showToast();
        return;
    }

    try {
        // 🟢 Solo enviamos puntaje, comentario y el ID del producto
        await axios.post("/api/resenas", {
            puntaje: puntajeSeleccionado,
            comentario: texto,
            producto: { id: productoId }
        }, { withCredentials: true });

        Toastify({ text: "¡Gracias por tu opinión!", background: "#198754" }).showToast();
        setTimeout(() => location.reload(), 1500);
    } catch (err) {
        // Ahora el error 500 mostrará un mensaje más claro si lo configuramos en el backend
        console.error(err.response.data);
        Toastify({ text: "Error al publicar. Intentá de nuevo.", background: "#dc3545" }).showToast();
    }
}