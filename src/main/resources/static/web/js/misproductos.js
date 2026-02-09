const API_URL = "/api/productos";

/* ===========================
   CARGAR PRODUCTOS
=========================== */
async function cargarProductos() {
    try {
        const res = await axios.get(`${API_URL}/mios`, { withCredentials: true });
        const productos = res.data;
        const contenedor = document.getElementById("productos");
        contenedor.innerHTML = "";

        if (!productos || productos.length === 0) {
            contenedor.innerHTML = `<p class="text-center text-muted">No tienes productos cargados.</p>`;
            return;
        }

        productos.forEach(p => {
            const cardInactiva = p.activo ? "" : "producto-inactivo";
            const textoMedida = p.tipoVenta === "UNIDAD" ? "unidad" : p.unidadMedida;

            contenedor.innerHTML += `
                <div class="col-md-4 mb-4">
                    <div class="card h-100 sombra ${cardInactiva}">
                        <img src="${p.imagenUrl || 'https://res.cloudinary.com/dklkf0fmq/image/upload/v1770657381/300x200_mefv8r.svg'}"
                             class="card-img-top"
                             style="height:200px; object-fit:cover;">
                        <div class="card-body">
                            <div class="d-flex justify-content-between align-items-start">
                                <h5 class="card-title">${p.nombre}</h5>
                                <span class="badge bg-secondary">${p.tipoVenta}</span>
                            </div>
                            <p class="text-muted small">${p.descripcion || ""}</p>
                            <p>
                                <strong>$${p.precio.toFixed(2)}</strong>
                                <small class="text-muted">por ${textoMedida}</small>
                            </p>
                            <div class="d-flex flex-column gap-2">
                                <div class="d-flex justify-content-between">
                                    <button class="btn btn-primary btn-sm"
                                        onclick="abrirModalEditar(${p.id}, '${p.nombre}', '${p.descripcion}', ${p.precio}, '${p.tipoVenta}', '${p.unidadMedida}')">
                                        Editar
                                    </button>
                                    <button class="btn ${p.activo ? 'btn-warning' : 'btn-success'} btn-sm"
                                        onclick="toggleEstado(${p.id})">
                                        ${p.activo ? 'Desactivar' : 'Activar'}
                                    </button>
                                </div>
                                <button class="btn btn-danger btn-sm" onclick="eliminarProducto(${p.id})">
                                    Eliminar
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            `;
        });
    } catch (e) {
        showToast("Error al cargar productos", "error");
    }
}

/* ===========================
   CREAR PRODUCTO
=========================== */
async function crearProducto() {
    const nombre = document.getElementById("nombre").value.trim();
    const descripcion = document.getElementById("descripcion").value.trim();
    const precio = document.getElementById("precio").value;
    const tipoVenta = document.getElementById("tipoVenta").value;
    const unidadMedida = document.getElementById("unidadMedida").value;
    const imagen = document.getElementById("imagen").files[0];

    if (!nombre || !precio) {
        showToast("Completa nombre y precio", "error");
        return;
    }

    const formData = new FormData();
    formData.append("nombre", nombre);
    formData.append("descripcion", descripcion);
    formData.append("precio", precio);
    formData.append("tipoVenta", tipoVenta);

    // ⚠️ Si NO es UNIDAD, mandamos unidadMedida
    if (tipoVenta !== "UNIDAD") {
        formData.append("unidadMedida", unidadMedida);
    }

    if (imagen) {
        formData.append("imagen", imagen);
    }

    try {
        await axios.post(API_URL, formData, {
            withCredentials: true,
            headers: { "Content-Type": "multipart/form-data" }
        });
        showToast("✅ Producto creado", "success");
        limpiarCampos();
        cargarProductos();
    } catch (e) {
        showToast("❌ Error al crear producto", "error");
    }
}

/* ===========================
   MODAL EDITAR
=========================== */
function abrirModalEditar(id, nombre, descripcion, precio, tipoVenta, unidadMedida) {
    document.getElementById("edit-id").value = id;
    document.getElementById("edit-nombre").value = nombre;
    document.getElementById("edit-descripcion").value = descripcion || "";
    document.getElementById("edit-precio").value = precio;
    document.getElementById("edit-tipoVenta").value = tipoVenta;
    document.getElementById("edit-unidadMedida").value = unidadMedida || "";

    toggleMedida("edit-");

    new bootstrap.Modal(document.getElementById("modalEditar")).show();
}

/* ===========================
   GUARDAR EDICIÓN
=========================== */
async function guardarEdicion() {
    const id = document.getElementById("edit-id").value;
    const tipoVenta = document.getElementById("edit-tipoVenta").value;

    const formData = new FormData();
    formData.append("nombre", document.getElementById("edit-nombre").value.trim());
    formData.append("descripcion", document.getElementById("edit-descripcion").value.trim());
    formData.append("precio", document.getElementById("edit-precio").value);
    formData.append("tipoVenta", tipoVenta);

    if (tipoVenta !== "UNIDAD") {
        formData.append("unidadMedida", document.getElementById("edit-unidadMedida").value);
    }

    const imagen = document.getElementById("edit-imagen").files[0];
    if (imagen) {
        formData.append("imagen", imagen);
    }

    try {
        await axios.put(`${API_URL}/${id}`, formData, {
            withCredentials: true,
            headers: { "Content-Type": "multipart/form-data" }
        });
        showToast("✅ Producto actualizado", "success");
        bootstrap.Modal.getInstance(document.getElementById("modalEditar")).hide();
        cargarProductos();
    } catch (e) {
        showToast("❌ Error al actualizar", "error");
    }
}

/* ===========================
   TOGGLE MEDIDA
=========================== */
function toggleMedida(prefix = "") {
    const tipoVenta = document.getElementById(`${prefix}tipoVenta`).value;
    const divMedida = document.getElementById(`${prefix}divMedida`);
    const selectMedida = document.getElementById(`${prefix}unidadMedida`);

    // Limpiar opciones
    selectMedida.innerHTML = "";

    switch (tipoVenta) {
        case "UNIDAD":
            divMedida.style.display = "none";
            break;

        case "PESO":
            divMedida.style.display = "block";
            selectMedida.innerHTML = `
                <option value="kg">Kilogramo (kg)</option>
                <option value="g">Gramo (g)</option>
            `;
            break;

        case "METRO":
            divMedida.style.display = "block";
            selectMedida.innerHTML = `
                <option value="m">Metro (m)</option>
                <option value="cm">Centímetro (cm)</option>
                <option value="mm">Milímetro (mm)</option>
            `;
            break;

        case "VOLUMEN":
            divMedida.style.display = "block";
            selectMedida.innerHTML = `
                <option value="l">Litro (l)</option>
                <option value="ml">Mililitro (ml)</option>
            `;
            break;

        default:
            divMedida.style.display = "none";
    }
}


/* ===========================
   ESTADO / ELIMINAR
=========================== */
async function toggleEstado(id) {
    try {
        await axios.put(`${API_URL}/${id}/estado`, {}, { withCredentials: true });
        cargarProductos();
    } catch {
        showToast("Error al cambiar estado", "error");
    }
}

async function eliminarProducto(id) {
    if (!confirm("¿Eliminar producto?")) return;
    try {
        await axios.put(`${API_URL}/${id}/eliminar`, {}, { withCredentials: true });
        showToast("🗑️ Producto eliminado", "success");
        cargarProductos();
    } catch {
        showToast("Error al eliminar", "error");
    }
}

/* ===========================
   UTILIDADES
=========================== */
function limpiarCampos() {
    ["nombre", "descripcion", "precio", "unidadMedida", "imagen"].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.value = "";
    });
}

function showToast(texto, tipo = "success") {
    Toastify({
        text: texto,
        duration: 3000,
        gravity: "top",
        position: "right",
        style: {
            background: tipo === "success" ? "#198754" : "#dc3545"
        }
    }).showToast();
}

window.onload = cargarProductos;
