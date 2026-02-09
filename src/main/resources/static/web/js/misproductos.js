const API_URL = "/api/productos";
const CATEGORIAS_URL = "/api/categorias"; // 🟢 Asegúrate de tener este endpoint en tu CategoriaController

// 🔹 Cargar productos y categorías al iniciar
document.addEventListener("DOMContentLoaded", () => {
    cargarCategorias(); // Cargar el select primero
    cargarProductos();
});

// 🟢 NUEVO: Cargar categorías en los Selects (Creación y Edición)
async function cargarCategorias() {
    try {
        const res = await axios.get(CATEGORIAS_URL);
        const categorias = res.data;
        
        const selectCrear = document.getElementById("categoriaId");
        const selectEditar = document.getElementById("edit-categoriaId");

        // Limpiar y llenar selects
        [selectCrear, selectEditar].forEach(select => {
            select.innerHTML = '<option value="">Seleccione una categoría...</option>';
            categorias.forEach(cat => {
                const option = document.createElement("option");
                option.value = cat.id;
                option.textContent = cat.nombre;
                select.appendChild(option);
            });
        });
    } catch (err) {
        console.error("Error al cargar categorías:", err);
    }
}

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
<<<<<<< HEAD
            // Usamos p.categoriaNombre que viene del nuevo DTO 
            const categoriaLabel = p.categoriaNombre || "Sin categoría";
=======
            const textoMedida = p.tipoVenta === "UNIDAD" ? "unidad" : p.unidadMedida;
>>>>>>> 6c62a080a856646e9fd08cc848c39765c364ad7e

            contenedor.innerHTML += `
                <div class="col-md-4 mb-4">
                    <div class="card h-100 sombra ${cardInactiva}">
<<<<<<< HEAD
                        <img src="${p.imagenUrl || 'https://via.placeholder.com/300x200?text=Sin+Imagen'}" 
                             class="card-img-top" alt="${p.nombre}" style="height: 200px; object-fit: cover;">
                        <div class="card-body">
                            <div class="d-flex justify-content-between align-items-start">
                                <h5 class="card-title">${p.nombre}</h5>
                                <span class="badge bg-info text-dark" style="font-size: 0.7rem;">${categoriaLabel}</span>
                            </div>
                            <p class="card-text text-muted small">${p.descripcion}</p>
                            <p><strong>$${p.precio.toFixed(2)}</strong></p>
                            <div class="d-flex flex-column gap-2">
                                <div class="d-flex justify-content-between">
                                    <button class="btn btn-primary btn-sm" onclick="abrirModalEditar(${p.id}, '${p.nombre}', '${p.descripcion}', ${p.precio}, ${p.categoriaId || 0})">Editar</button>
                                    <button class="btn ${p.activo ? 'btn-warning' : 'btn-success'} btn-sm" onclick="toggleEstado(${p.id})">
=======
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
>>>>>>> 6c62a080a856646e9fd08cc848c39765c364ad7e
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

<<<<<<< HEAD
// 🔹 Crear producto (Modificado para FormData y Categoría)
=======
/* ===========================
   CREAR PRODUCTO
=========================== */
>>>>>>> 6c62a080a856646e9fd08cc848c39765c364ad7e
async function crearProducto() {
    const nombre = document.getElementById("nombre").value.trim();
    const descripcion = document.getElementById("descripcion").value.trim();
    const precio = document.getElementById("precio").value;
<<<<<<< HEAD
    const categoriaId = document.getElementById("categoriaId").value;
    const imagenFile = document.getElementById("imagenFile").files[0];

    if (!nombre || !descripcion || !precio || !categoriaId) {
        showToast("Por favor, completa todos los campos obligatorios.", "error");
=======
    const tipoVenta = document.getElementById("tipoVenta").value;
    const unidadMedida = document.getElementById("unidadMedida").value;
    const imagen = document.getElementById("imagen").files[0];

    if (!nombre || !precio) {
        showToast("Completa nombre y precio", "error");
>>>>>>> 6c62a080a856646e9fd08cc848c39765c364ad7e
        return;
    }

    const formData = new FormData();
    formData.append("nombre", nombre);
    formData.append("descripcion", descripcion);
    formData.append("precio", precio);
<<<<<<< HEAD
    formData.append("categoriaId", categoriaId); // 🟢 Enviamos el ID de la categoría
    if (imagenFile) formData.append("imagen", imagenFile);

    try {
        await axios.post(API_URL, formData, { 
            withCredentials: true,
            headers: { 'Content-Type': 'multipart/form-data' }
        });
        showToast("✅ Producto agregado correctamente", "success");
        limpiarCampos();
        cargarProductos();
    } catch (err) {
        showToast(err.response?.data || "❌ Error al agregar producto", "error");
    }
}

// 🔹 Abrir modal de edición
function abrirModalEditar(id, nombre, descripcion, precio, categoriaId) {
    document.getElementById("edit-id").value = id;
    document.getElementById("edit-nombre").value = nombre;
    document.getElementById("edit-descripcion").value = descripcion;
    document.getElementById("edit-precio").value = precio;
    document.getElementById("edit-categoriaId").value = categoriaId || "";

    const modal = new bootstrap.Modal(document.getElementById("modalEditar"));
    modal.show();
}

// 🔹 Guardar edición (Modificado para FormData)
async function guardarEdicion() {
    const id = document.getElementById("edit-id").value;
    const nombre = document.getElementById("edit-nombre").value.trim();
    const descripcion = document.getElementById("edit-descripcion").value.trim();
    const precio = document.getElementById("edit-precio").value;
    const categoriaId = document.getElementById("edit-categoriaId").value;
    const imagenFile = document.getElementById("edit-imagenFile").files[0];

    if (!nombre || !descripcion || !precio || !categoriaId) {
        showToast("Completa los campos antes de guardar.", "error");
        return;
    }

    const formData = new FormData();
    formData.append("nombre", nombre);
    formData.append("descripcion", descripcion);
    formData.append("precio", precio);
    formData.append("categoriaId", categoriaId);
    if (imagenFile) formData.append("imagen", imagenFile);

    try {
        // Usamos PUT como definiste en tu controlador [cite: 205-206]
        await axios.put(`${API_URL}/${id}`, formData, { 
            withCredentials: true,
            headers: { 'Content-Type': 'multipart/form-data' }
        });
        showToast("✅ Producto actualizado correctamente", "success");
        
        bootstrap.Modal.getInstance(document.getElementById('modalEditar')).hide();
        cargarProductos();
    } catch (err) {
        showToast("❌ Error al actualizar el producto", "error");
    }
}

// 🔹 Activar/Desactivar Producto (Mantenido)
=======
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
>>>>>>> 6c62a080a856646e9fd08cc848c39765c364ad7e
async function toggleEstado(id) {
    try {
        await axios.put(`${API_URL}/${id}/estado`, {}, { withCredentials: true });
        cargarProductos();
    } catch {
        showToast("Error al cambiar estado", "error");
    }
}

async function eliminarProducto(id) {
<<<<<<< HEAD
    if (!confirm("¿Seguro que querés eliminar este producto?")) return;
=======
    if (!confirm("¿Eliminar producto?")) return;
>>>>>>> 6c62a080a856646e9fd08cc848c39765c364ad7e
    try {
        await axios.put(`${API_URL}/${id}/eliminar`, {}, { withCredentials: true });
        showToast("🗑️ Producto eliminado", "success");
        cargarProductos();
    } catch {
        showToast("Error al eliminar", "error");
    }
}

<<<<<<< HEAD
function showToast(mensaje, tipo = "success") {
=======
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
>>>>>>> 6c62a080a856646e9fd08cc848c39765c364ad7e
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

<<<<<<< HEAD
function limpiarCampos() {
    document.getElementById("nombre").value = "";
    document.getElementById("descripcion").value = "";
    document.getElementById("precio").value = "";
    document.getElementById("categoriaId").value = "";
    document.getElementById("imagenFile").value = "";
}
=======
window.onload = cargarProductos;
>>>>>>> 6c62a080a856646e9fd08cc848c39765c364ad7e
