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

// 🔹 Cargar productos al iniciar
async function cargarProductos() {
    try {
        const res = await axios.get(`${API_URL}/mios`, { withCredentials: true });
        const productos = res.data;
        const contenedor = document.getElementById("productos");
        contenedor.innerHTML = "";

        if (productos.length === 0) {
            contenedor.innerHTML = `<p class="text-center text-muted">No tienes productos cargados.</p>`;
            return;
        }

        productos.forEach(p => {
            const cardInactiva = p.activo ? "" : "producto-inactivo";
            // Usamos p.categoriaNombre que viene del nuevo DTO 
            const categoriaLabel = p.categoriaNombre || "Sin categoría";

            contenedor.innerHTML += `
                <div class="col-md-4 mb-4">
                    <div class="card h-100 sombra ${cardInactiva}">
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
                                        ${p.activo ? 'Desactivar' : 'Activar'}
                                    </button>
                                </div>
                                <button class="btn btn-danger btn-sm" onclick="eliminarProducto(${p.id})">Eliminar</button>
                            </div>
                        </div>
                    </div>
                </div>
            `;
        });
    } catch (err) {
        showToast("Error al cargar los productos", "error");
    }
}

// 🔹 Crear producto (Modificado para FormData y Categoría)
async function crearProducto() {
    const nombre = document.getElementById("nombre").value.trim();
    const descripcion = document.getElementById("descripcion").value.trim();
    const precio = document.getElementById("precio").value;
    const categoriaId = document.getElementById("categoriaId").value;
    const imagenFile = document.getElementById("imagenFile").files[0];

    if (!nombre || !descripcion || !precio || !categoriaId) {
        showToast("Por favor, completa todos los campos obligatorios.", "error");
        return;
    }

    const formData = new FormData();
    formData.append("nombre", nombre);
    formData.append("descripcion", descripcion);
    formData.append("precio", precio);
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
async function toggleEstado(id) {
    try {
        await axios.put(`${API_URL}/${id}/estado`, {}, { withCredentials: true });
        showToast("Estado actualizado");
        cargarProductos();
    } catch (err) {
        showToast("Error al cambiar estado", "error");
    }
}

// 🔹 Eliminar producto (Borrado Lógico)
async function eliminarProducto(id) {
    if (!confirm("¿Seguro que querés eliminar este producto?")) return;
    try {
        await axios.put(`${API_URL}/${id}/eliminar`, {}, { withCredentials: true });
        showToast("🗑️ Producto eliminado");
        cargarProductos();
    } catch (err) {
        showToast("❌ Error al eliminar el producto", "error");
    }
}

function showToast(mensaje, tipo = "success") {
    Toastify({
        text: mensaje,
        duration: 3000,
        gravity: "top",
        position: "right",
        style: {
            background: tipo === "success" ? "#198754" : "#dc3545"
        }
    }).showToast();
}

function limpiarCampos() {
    document.getElementById("nombre").value = "";
    document.getElementById("descripcion").value = "";
    document.getElementById("precio").value = "";
    document.getElementById("categoriaId").value = "";
    document.getElementById("imagenFile").value = "";
}