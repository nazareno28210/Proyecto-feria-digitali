const API_URL = "/api/productos";
const CATEGORIAS_URL = "/api/categorias";

// 🟢 Definición Global (Asegúrate de que el Public ID sea correcto en tu Cloudinary)
const imgPortadaDefault = "https://res.cloudinary.com/dklkf0fmq/image/upload/v1741823733/NOT_IMAGE_aypskv.png";

// 🔹 Inicialización
document.addEventListener("DOMContentLoaded", () => {
    cargarCategorias(); 
    cargarProductos();
});

let idsParaBorrar = []; // Lista temporal para fotos de la galería
let borrarPortadaActual = false; // 🟢 NUEVO: Rastrear si se marca la portada para borrar

// --- 🟢 NUEVA FUNCIÓN: Para el botón X de la portada actual ---
function eliminarPortadaExistente() {
    borrarPortadaActual = true;
    const imgPortada = document.getElementById("edit-img-portada-actual");
    if (imgPortada) {
        imgPortada.style.opacity = "0.3";
        imgPortada.style.filter = "grayscale(100%)";
    }
    showToast("📸 Portada marcada para eliminar. Debes subir una nueva.", "info");
}

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

// 🔹 Listar Productos con Scroll Horizontal de Imágenes (Versión Limpia)
async function cargarProductos() {
    try {
        const res = await axios.get(`${API_URL}/mios`, { withCredentials: true }); 
        const productos = res.data; // [cite: 12]
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

            // 📸 Usamos la constante global imgPortadaDefault definida al inicio del archivo
            const imgPortada = p.imagenUrl || imgPortadaDefault; // 
            
            let imagenesHtml = `<img src="${imgPortada}" class="card-img-top img-scroll-item" style="height: 200px; object-fit: cover;">`;
            
            // Renderizamos la galería extra desde los objetos del DTO
            if (p.galeria && p.galeria.length > 0) { // 
                p.galeria.forEach(imgObj => {
                    imagenesHtml += `<img src="${imgObj.url}" class="card-img-top img-scroll-item" style="height: 200px; object-fit: cover;">`; // 
                });
            }

            const precioSeguro = p.precio ? p.precio.toFixed(2) : "0.00"; // [cite: 18]
            let precioHtml = `<strong>$${precioSeguro}</strong>`;
            
            if (p.tipoVenta !== 'UNIDAD' && p.unidadMedida && p.unidadMedida !== 'null') {
                precioHtml += ` <small class="text-muted">por ${p.unidadMedida}</small>`; // [cite: 19]
            }

            contenedor.innerHTML += `
                <div class="col-md-4 mb-4">
                    <div class="card h-100 sombra ${cardInactiva}">
                        <div class="img-scroll-container d-flex overflow-x-auto" style="scroll-snap-type: x mandatory; -webkit-overflow-scrolling: touch;">
                            ${imagenesHtml}
                        </div>
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
                                        onclick='prepararEdicion(${JSON.stringify(p)})'>
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
        console.error("Error al cargar productos:", e); // [cite: 30]
    }
}

// 🟢 FUNCIÓN DE PREVISUALIZACIÓN UNIFICADA (Para nuevas fotos)
function previsualizarTodasLasFotos(accion) {
    const inputPortada = document.getElementById(accion === 'crear' ? 'imagen' : 'edit-imagen');
    const inputGaleria = document.getElementById(accion === 'crear' ? 'imagenesExtras' : 'edit-imagenesExtras');
    const contenedor = document.getElementById(accion === 'crear' ? 'preview-todas-fotos-crear' : 'preview-todas-fotos-editar');
    
    contenedor.innerHTML = "";
    let totalFotos = 0;

    if (inputPortada && inputPortada.files && inputPortada.files[0]) {
        totalFotos++;
        const reader = new FileReader();
        reader.onload = (e) => {
            const div = document.createElement("div");
            div.className = "posicion-relativa";
            div.innerHTML = `
                <img src="${e.target.result}" class="img-preview-galeria border-primary border-2">
                <span class="badge bg-primary posicion-absoluta top-0 start-0 m-1" style="font-size: 0.6rem;">Portada</span>
            `;
            contenedor.appendChild(div);
        }
        reader.readAsDataURL(inputPortada.files[0]);
    }

    if (inputGaleria && inputGaleria.files) {
        Array.from(inputGaleria.files).forEach(file => {
            totalFotos++;
            const reader = new FileReader();
            reader.onload = (e) => {
                const img = document.createElement("img");
                img.src = e.target.result;
                img.className = "img-preview-galeria";
                contenedor.appendChild(img);
            }
            reader.readAsDataURL(file);
        });
    }
}

// 🔹 Crear Producto (Con Validaciones)
async function crearProducto() {
    const nombre = document.getElementById("nombre").value.trim();
    const precio = document.getElementById("precio").value;
    const categoriaId = document.getElementById("categoriaId").value;
    const inputPortada = document.getElementById("imagen");
    const inputExtras = document.getElementById("imagenesExtras");

    // --- 🛑 VALIDACIONES ---
    if (!nombre || !precio || !categoriaId) {
        return showToast("⚠️ Nombre, Precio y Categoría son obligatorios", "error");
    }
    if (!inputPortada.files || !inputPortada.files[0]) {
        return showToast("📸 La Foto Principal (Portada) es obligatoria", "error");
    }
    if (inputExtras.files && inputExtras.files.length > 5) {
        return showToast("⚠️ Máximo 5 fotos extras permitidas", "error");
    }

    const formData = new FormData();
    formData.append("nombre", nombre);
    formData.append("descripcion", document.getElementById("descripcion").value.trim());
    formData.append("precio", precio);
    formData.append("categoriaId", categoriaId);
    formData.append("tipoVenta", document.getElementById("tipoVenta").value);
    formData.append("unidadMedida", document.getElementById("unidadMedida").value);

    formData.append("imagen", inputPortada.files[0]);

    if (inputExtras.files.length > 0) {
        Array.from(inputExtras.files).forEach(archivo => {
            formData.append("imagenesExtras", archivo);
        });
    }

    try {
        showToast("🚀 Creando producto...", "info");
        await axios.post(API_URL, formData, {
            withCredentials: true,
            headers: { "Content-Type": "multipart/form-data" }
        });
        showToast("✅ Producto creado con éxito", "success");
        limpiarCampos();
        cargarProductos();
    } catch (e) {
        showToast("❌ Error al crear producto", "error");
    }
}

// 🟢 Preparar Edición con Galería
function prepararEdicion(producto) {
    // Pasamos producto.galeria que ya contiene la lista de {id, url}
    abrirModalEditar(
        producto.id, 
        producto.nombre, 
        producto.descripcion, 
        producto.precio, 
        producto.categoriaId, 
        producto.tipoVenta, 
        producto.unidadMedida,
        producto.imagenUrl,
        producto.galeria 
    );
}

// 🔹 Abrir Modal Editar (Actualizado para gestionar Portada y Galería)
function abrirModalEditar(id, nombre, descripcion, precio, categoriaId, tipoVenta, unidadMedida, portadaUrl, galeria) {
    // 1. Cargar datos básicos en los inputs
    document.getElementById("edit-id").value = id;
    document.getElementById("edit-nombre").value = nombre;
    document.getElementById("edit-descripcion").value = descripcion || "";
    document.getElementById("edit-precio").value = precio;
    document.getElementById("edit-categoriaId").value = categoriaId || "";
    document.getElementById("edit-tipoVenta").value = tipoVenta || "UNIDAD";
    
    // 2. Configurar unidades de medida dinámicas
    toggleMedida("edit-");
    document.getElementById("edit-unidadMedida").value = unidadMedida || "";

    // 3. 🟢 RESETEAR ESTADOS DE BORRADO
    idsParaBorrar = []; // Limpiar lista de fotos de galería marcadas
    borrarPortadaActual = false; // Resetear bandera de borrado de portada

    // 4. 🟢 GESTIÓN DE LA PORTADA ACTUAL
    const imgPortadaActual = document.getElementById("edit-img-portada-actual");
    if (imgPortadaActual) {
        imgPortadaActual.src = portadaUrl || "";
        // Restaurar estilo visual (por si se marcó para borrar en la edición anterior)
        imgPortadaActual.style.opacity = "1";
        imgPortadaActual.style.filter = "none";
    }

    // 5. 🟢 GESTIÓN DE LA GALERÍA ACTUAL (Fotos con botón X)
    const contenedorGaleriaActual = document.getElementById("edit-galeria-actual");
    if (contenedorGaleriaActual) {
        contenedorGaleriaActual.innerHTML = ""; // Limpiar galería previa
        
        if (galeria && galeria.length > 0) {
            galeria.forEach(img => {
                const div = document.createElement("div");
                div.className = "posicion-relativa m-1";
                div.id = `contenedor-img-${img.id}`;
                div.innerHTML = `
                    <img src="${img.url}" class="img-preview-galeria">
                    <button type="button" class="btn btn-danger btn-sm posicion-absoluta top-0 end-0 m-1 btn-eliminar-foto" 
                            onclick="marcarParaBorrar(${img.id})" 
                            style="padding: 0px 5px; font-size: 12px; border-radius: 50%;">&times;</button>
                `;
                contenedorGaleriaActual.appendChild(div);
            });
        } else {
            contenedorGaleriaActual.innerHTML = '<p class="text-muted small w-100 text-center m-0">No hay fotos extras.</p>';
        }
    }

    // 6. 🟢 LIMPIAR INPUTS DE ARCHIVOS Y PREVIEWS NUEVOS
    document.getElementById("edit-imagen").value = "";
    document.getElementById("edit-imagenesExtras").value = "";
    
    const previewNuevas = document.getElementById("preview-todas-fotos-editar");
    if (previewNuevas) {
        previewNuevas.innerHTML = ""; // Limpiar miniaturas de la selección anterior
    }

    // 7. Mostrar el Modal
    new bootstrap.Modal(document.getElementById("modalEditar")).show();
}

// 🟢 Función para marcar fotos para borrar
function marcarParaBorrar(id) {
    if (!idsParaBorrar.includes(id)) {
        idsParaBorrar.push(id);
        const el = document.getElementById(`contenedor-img-${id}`);
        if (el) {
            el.style.opacity = "0.3";
            el.style.filter = "grayscale(100%)";
        }
    } else {
        idsParaBorrar = idsParaBorrar.filter(i => i !== id);
        const el = document.getElementById(`contenedor-img-${id}`);
        if (el) {
            el.style.opacity = "1";
            el.style.filter = "none";
        }
    }
}

// 🔹 Guardar Edición (Con Validaciones y Borrado de Portada)
async function guardarEdicion() {
    const id = document.getElementById("edit-id").value;
    const inputPortadaNueva = document.getElementById("edit-imagen");
    const inputExtrasNuevos = document.getElementById("edit-imagenesExtras");

    // --- 🛑 VALIDACIONES ---
    // Si borra la portada actual pero no selecciona una nueva en el input
    if (borrarPortadaActual && (!inputPortadaNueva.files || !inputPortadaNueva.files[0])) {
        return showToast("📸 No puedes dejar el producto sin portada", "error");
    }

    const formData = new FormData();
    formData.append("nombre", document.getElementById("edit-nombre").value.trim());
    formData.append("descripcion", document.getElementById("edit-descripcion").value.trim());
    formData.append("precio", document.getElementById("edit-precio").value);
    formData.append("categoriaId", document.getElementById("edit-categoriaId").value);
    formData.append("tipoVenta", document.getElementById("edit-tipoVenta").value);
    formData.append("unidadMedida", document.getElementById("edit-unidadMedida").value);

    // 🟢 Señal para el backend de borrar portada
    if (borrarPortadaActual) formData.append("borrarPortada", "true");

    if (idsParaBorrar.length > 0) {
        idsParaBorrar.forEach(idBorrar => formData.append("eliminarImagenIds", idBorrar));
    }

    if (inputPortadaNueva.files[0]) formData.append("imagen", inputPortadaNueva.files[0]);

    if (inputExtrasNuevos.files.length > 0) {
        Array.from(inputExtrasNuevos.files).forEach(f => formData.append("imagenesExtras", f));
    }

    try {
        showToast("🔄 Guardando cambios...", "info");
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

// 🔹 Lógica de Unidades Dinámicas
function toggleMedida(prefix = "") {
    const tipoVenta = document.getElementById(`${prefix}tipoVenta`).value;
    const divMedida = document.getElementById(`${prefix}divMedida`);
    const selectMedida = document.getElementById(`${prefix}unidadMedida`);

    if (!divMedida || !selectMedida) return;

    selectMedida.innerHTML = "";

    if (tipoVenta === "UNIDAD") {
        divMedida.style.display = "none";
    } else {
        divMedida.style.display = "block";
        let opciones = "";
        switch (tipoVenta) {
            case "PESO": opciones = `<option value="kg">kg</option><option value="g">g</option>`; break;
            case "METRO": opciones = `<option value="m">m</option><option value="cm">cm</option>`; break;
            case "VOLUMEN": opciones = `<option value="l">l</option><option value="ml">ml</option>`; break;
        }
        selectMedida.innerHTML = opciones;
    }
}

// 🔹 Utilidades
function limpiarCampos() {
    ["nombre", "descripcion", "precio", "categoriaId", "tipoVenta", "unidadMedida", "imagen", "imagenesExtras"].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.value = "";
    });
    const previewCrear = document.getElementById("preview-todas-fotos-crear");
    if (previewCrear) previewCrear.innerHTML = '<p class="text-muted small w-100 text-center m-0 align-self-center">Aquí se verán tus fotos...</p>';
}

function showToast(texto, tipo = "success") {
    Toastify({
        text: texto,
        duration: 3000,
        gravity: "top",
        position: "right",
        style: { background: tipo === "success" ? "#198754" : (tipo === "info" ? "#0dcaf0" : "#dc3545") }
    }).showToast();
}

async function toggleEstado(id) {
    try {
        await axios.put(`${API_URL}/${id}/estado`, {}, { withCredentials: true });
        cargarProductos();
    } catch { showToast("Error al cambiar estado", "error"); }
}

async function eliminarProducto(id) {
    if (!confirm("¿Eliminar producto?")) return;
    try {
        await axios.put(`${API_URL}/${id}/eliminar`, {}, { withCredentials: true });
        showToast("🗑️ Producto eliminado", "success");
        cargarProductos();
    } catch { showToast("Error al eliminar", "error"); }
}