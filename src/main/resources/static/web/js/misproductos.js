const API_URL = "/api/productos";

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
            contenedor.innerHTML += `
                <div class="col-md-4 mb-4">
                    <div class="card h-100 sombra ${cardInactiva}">
                        <img src="${p.imagen || 'https://via.placeholder.com/300x200?text=Sin+Imagen'}" 
                             class="card-img-top" alt="${p.nombre}" style="height: 200px; object-fit: cover;">
                        <div class="card-body">
                            <h5 class="card-title">${p.nombre}</h5>
                            <p class="card-text text-muted small">${p.descripcion}</p>
                            <p><strong>$${p.precio.toFixed(2)}</strong></p>
                            <div class="d-flex flex-column gap-2">
                                <div class="d-flex justify-content-between">
                                    <button class="btn btn-primary btn-sm" onclick="abrirModalEditar(${p.id}, '${p.nombre}', '${p.descripcion}', ${p.precio}, '${p.imagen || ''}')">Editar</button>
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

// 🔹 Crear producto
async function crearProducto() {
    const nombre = document.getElementById("nombre").value.trim();
    const descripcion = document.getElementById("descripcion").value.trim();
    const precio = parseFloat(document.getElementById("precio").value);
    const imagen = document.getElementById("imagenUrl").value.trim();

    if (!nombre || !descripcion || isNaN(precio)) {
        showToast("Por favor, completa todos los campos.", "error");
        return;
    }

    try {
        await axios.post(API_URL, { nombre, descripcion, precio, imagen }, { withCredentials: true });
        showToast("✅ Producto agregado correctamente", "success");
        limpiarCampos();
        cargarProductos();
    } catch (err) {
        showToast("❌ Error al agregar producto", "error");
    }
}

// 🔹 Activar/Desactivar Producto
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
    if (!confirm("¿Seguro que querés eliminar este producto? Se ocultará de tu lista.")) return;
    try {
        await axios.put(`${API_URL}/${id}/eliminar`, {}, { withCredentials: true });
        showToast("🗑️ Producto eliminado");
        cargarProductos();
    } catch (err) {
        showToast("❌ Error al eliminar el producto", "error");
    }
}

// 🔹 Abrir modal de edición
function abrirModalEditar(id, nombre, descripcion, precio, imagen) {
    document.getElementById("edit-id").value = id;
    document.getElementById("edit-nombre").value = nombre;
    document.getElementById("edit-descripcion").value = descripcion;
    document.getElementById("edit-precio").value = precio;
    document.getElementById("edit-imagenUrl").value = imagen;

    const modal = new bootstrap.Modal(document.getElementById("modalEditar"));
    modal.show();
}

// 🔹 Guardar edición
async function guardarEdicion() {
    const id = document.getElementById("edit-id").value;
    const nombre = document.getElementById("edit-nombre").value.trim();
    const descripcion = document.getElementById("edit-descripcion").value.trim();
    const precio = parseFloat(document.getElementById("edit-precio").value);
    const imagen = document.getElementById("edit-imagenUrl").value.trim();

    if (!nombre || !descripcion || isNaN(precio)) {
        showToast("Completa los campos antes de guardar.", "error");
        return;
    }

    try {
        await axios.put(`${API_URL}/${id}`, { nombre, descripcion, precio, imagen }, { withCredentials: true });
        showToast("✅ Producto actualizado correctamente", "success");
        
        const modalEl = document.getElementById('modalEditar');
        const modal = bootstrap.Modal.getInstance(modalEl);
        modal.hide();
        cargarProductos();
    } catch (err) {
        showToast("❌ Error al actualizar el producto", "error");
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
    document.getElementById("imagenUrl").value = "";
}

window.onload = cargarProductos;