const API_URL = "/api/productos";
const CATEGORIAS_URL = "/api/categorias";

// 🔹 Inicialización
document.addEventListener("DOMContentLoaded", () => {
    cargarCategorias(); 
    cargarProductos();
});

// 🔹 Cargar categorías en los Selects
async function cargarCategorias() {
    try {
        const res = await axios.get(CATEGORIAS_URL);
        const categorias = res.data;
        
        const selectCrear = document.getElementById("categoriaId");
        const selectEditar = document.getElementById("edit-categoriaId");

        [selectCrear, selectEditar].forEach(select => {
            if (select) {
                select.innerHTML = '<option value="">Seleccione una categoría...</option>';
                categorias.forEach(cat => {
                    const option = document.createElement("option");
                    option.value = cat.id;
                    option.textContent = cat.nombre;
                    select.appendChild(option);
                });
            }
        });
    } catch (err) {
        console.error("Error al cargar categorías:", err);
    }
}

// 🔹 Listar Productos (Versión Corregida)
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
            const categoriaLabel = p.categoriaNombre || "General";
            const tipoVentaLabel = p.tipoVenta || "Sin definir";

            // 🟢 Corregimos la URL de imagen fallida
            const imgUrl = p.imagenUrl || "https://res.cloudinary.com/dklkf0fmq/image/upload/v1769030533/NOT_IMAGE_aypskv.png";

            // 🟢 Lógica de precio protegida contra nulos 
            const precioSeguro = p.precio ? p.precio.toFixed(2) : "0.00";
            let precioHtml = `<strong>$${precioSeguro}</strong>`;
            
            if (p.tipoVenta !== 'UNIDAD' && p.unidadMedida && p.unidadMedida !== 'null') {
                precioHtml += ` <small class="text-muted">por ${p.unidadMedida}</small>`;
            }

            contenedor.innerHTML += `
                <div class="col-md-4 mb-4">
                    <div class="card h-100 sombra ${cardInactiva}">
                        <img src="${imgUrl}" class="card-img-top" style="height: 200px; object-fit: cover;">
                        <div class="card-body">
                            <div class="d-flex justify-content-between align-items-start mb-2">
                                <h5 class="card-title mb-0 text-truncate" style="max-width: 60%;">${p.nombre}</h5>
                                <div class="d-flex flex-column align-items-end gap-1">
                                    <span class="badge bg-primary" style="font-size: 0.7rem;">${categoriaLabel}</span>
                                    <span class="badge bg-secondary" style="font-size: 0.7rem;">${tipoVentaLabel}</span>
                                </div>
                            </div>
                            <p class="card-text text-muted small text-truncate">${p.descripcion || ""}</p>
                            <p class="mb-3">${precioHtml}</p>
                            <div class="d-flex flex-column gap-2">
                                <div class="d-flex justify-content-between">
                                    <button class="btn btn-primary btn-sm w-50 me-1" 
                                        onclick="abrirModalEditar(${p.id}, '${p.nombre}', '${p.descripcion || ''}', ${p.precio}, ${p.categoriaId}, '${p.tipoVenta}', '${p.unidadMedida || ''}')">
                                        Editar
                                    </button>
                                    <button class="btn ${p.activo ? 'btn-warning' : 'btn-success'} btn-sm w-50 ms-1" onclick="toggleEstado(${p.id})">
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
    } catch (e) {
        console.error("Error al cargar productos:", e);
    }
}

// 🟢 NUEVA FUNCIÓN: Recibe el objeto completo para que no se rompa nada
function prepararEdicion(producto) {
    abrirModalEditar(
        producto.id, 
        producto.nombre, 
        producto.descripcion, 
        producto.precio, 
        producto.categoriaId, 
        producto.tipoVenta, 
        producto.unidadMedida
    );
}

// 🔹 Crear Producto
async function crearProducto() {
    const formData = new FormData();
    formData.append("nombre", document.getElementById("nombre").value.trim());
    formData.append("descripcion", document.getElementById("descripcion").value.trim());
    formData.append("precio", document.getElementById("precio").value);
    formData.append("categoriaId", document.getElementById("categoriaId").value);
    formData.append("tipoVenta", document.getElementById("tipoVenta").value);
    formData.append("unidadMedida", document.getElementById("unidadMedida").value);

    const imagen = document.getElementById("imagen").files[0];
    if (imagen) formData.append("imagen", imagen);

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

// 🔹 Abrir Modal Editar
function abrirModalEditar(id, nombre, descripcion, precio, categoriaId, tipoVenta, unidadMedida) {
    // 1. Datos básicos
    document.getElementById("edit-id").value = id;
    document.getElementById("edit-nombre").value = nombre;
    document.getElementById("edit-descripcion").value = descripcion || "";
    document.getElementById("edit-precio").value = precio;
    document.getElementById("edit-categoriaId").value = categoriaId || "";
    
    // 2. Seteamos el tipo de venta PRIMERO
    document.getElementById("edit-tipoVenta").value = tipoVenta || "UNIDAD";
    
    // 3. Ejecutamos toggleMedida para que se "dibujen" las opciones (kg, m, cm, etc.)
    toggleMedida("edit-");
    
    // 4. RECIÉN AHORA seteamos la unidad de medida específica
    // (Ahora sí va a funcionar porque la opción "kg" o "m" ya existe en el select)
    document.getElementById("edit-unidadMedida").value = unidadMedida || "";

    new bootstrap.Modal(document.getElementById("modalEditar")).show();
}

// 🔹 Guardar Edición
async function guardarEdicion() {
    const id = document.getElementById("edit-id").value;
    const formData = new FormData();
    formData.append("nombre", document.getElementById("edit-nombre").value.trim());
    formData.append("descripcion", document.getElementById("edit-descripcion").value.trim());
    formData.append("precio", document.getElementById("edit-precio").value);
    formData.append("categoriaId", document.getElementById("edit-categoriaId").value);
    formData.append("tipoVenta", document.getElementById("edit-tipoVenta").value);
    formData.append("unidadMedida", document.getElementById("edit-unidadMedida").value);

    const imagen = document.getElementById("edit-imagen").files[0];
    if (imagen) formData.append("imagen", imagen);

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

// 🔹 Lógica de Unidades Dinámicas (Teammate's feature)
function toggleMedida(prefix = "") {
    // Capturamos el valor del tipo de venta (ej: METRO, PESO...)
    const tipoVenta = document.getElementById(`${prefix}tipoVenta`).value;
    const divMedida = document.getElementById(`${prefix}divMedida`);
    const selectMedida = document.getElementById(`${prefix}unidadMedida`);

    if (!divMedida || !selectMedida) return;

    // 1. Limpiamos las opciones que haya tenido antes
    selectMedida.innerHTML = "";

    // 2. Si es por Unidad, no mostramos nada más
    if (tipoVenta === "UNIDAD") {
        divMedida.style.display = "none";
    } else {
        // 3. Si es Peso, Metro o Volumen, mostramos el div y llenamos las opciones
        divMedida.style.display = "block";
        
        let opciones = "";

        switch (tipoVenta) {
            case "PESO":
                opciones = `
                    <option value="kg">Kilogramo (kg)</option>
                    <option value="g">Gramo (g)</option>
                `;
                break;
            case "METRO":
                opciones = `
                    <option value="m">Metro (m)</option>
                    <option value="cm">Centímetro (cm)</option>
                    <option value="mm">Milímetro (mm)</option>
                `;
                break;
            case "VOLUMEN":
                opciones = `
                    <option value="l">Litro (l)</option>
                    <option value="ml">Mililitro (ml)</option>
                `;
                break;
        }
        
        // 4. Inyectamos las opciones nuevas en el select
        selectMedida.innerHTML = opciones;
    }
}

// 🔹 Estado y Eliminar
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

// 🔹 Utilidades
function limpiarCampos() {
    ["nombre", "descripcion", "precio", "categoriaId", "tipoVenta", "unidadMedida", "imagen"].forEach(id => {
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
        style: { background: tipo === "success" ? "#198754" : "#dc3545" }
    }).showToast();
}