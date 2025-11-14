const API_URL = "/api/productos/mis-productos";

// 🔹 Cargar productos al iniciar
async function cargarProductos() {
  try {
    const res = await axios.get(API_URL);
    const productos = res.data;
    const contenedor = document.getElementById("productos");
    contenedor.innerHTML = "";

    if (productos.length === 0) {
      contenedor.innerHTML = `<p class="text-center text-muted">No tienes productos cargados.</p>`;
      return;
    }

    productos.forEach(p => {
      contenedor.innerHTML += `
        <div class="col-md-4 mb-4">
          <div class="card h-100 sombra">
            <img src="${p.imagen || 'https://via.placeholder.com/300x200?text=Sin+Imagen'}" class="card-img-top" alt="${p.nombre}" style="height: 200px; object-fit: cover;">
            <div class="card-body">
              <h5 class="card-title">${p.nombre}</h5>
              <p class="card-text text-muted">${p.descripcion}</p>
              <p><strong>$${p.precio.toFixed(2)}</strong></p>
              <div class="d-flex justify-content-between">
                <button class="btn btn-primary btn-sm" onclick="abrirModalEditar(${p.id}, '${p.nombre}', '${p.descripcion}', ${p.precio}, '${p.imagen || ''}')">Editar</button>
                <button class="btn btn-danger btn-sm" onclick="eliminarProducto(${p.id})">Eliminar</button>
              </div>
            </div>
          </div>
        </div>
      `;
    });
  } catch (err) {
    console.error(err);
    alert("Error al cargar los productos.");
  }
}

// 🔹 Crear producto
async function crearProducto() {
  const nombre = document.getElementById("nombre").value.trim();
  const descripcion = document.getElementById("descripcion").value.trim();
  const precio = parseFloat(document.getElementById("precio").value);
  const imagen = document.getElementById("imagenUrl").value.trim();

  if (!nombre || !descripcion || isNaN(precio)) {
    alert("Por favor, completá todos los campos.");
    return;
  }

  try {
    await axios.post("/api/productos", { nombre, descripcion, precio, imagen });
    alert("✅ Producto agregado correctamente");
    limpiarCampos();
    cargarProductos();
  } catch (err) {
    console.error(err);
    alert("❌ Error al agregar producto");
  }
}

// 🔹 Eliminar producto
async function eliminarProducto(id) {
  if (!confirm("¿Seguro que querés eliminar este producto?")) return;

  try {
    await axios.delete(`/api/productos/${id}`);
    alert("🗑️ Producto eliminado");
    cargarProductos();
  } catch (err) {
    console.error(err);
    alert("❌ Error al eliminar el producto");
  }
}

// 🔹 Abrir modal de edición
function abrirModalEditar(id, nombre, descripcion, precio, imagen) {
  document.getElementById("edit-id").value = id;
  document.getElementById("edit-nombre").value = nombre;
  document.getElementById("edit-descripcion").value = descripcion;
  document.getElementById("edit-precio").value = precio;
  document.getElementById("edit-imagenUrl").value = imagen; // Carga la URL actual en el input

  const modal = new bootstrap.Modal(document.getElementById("modalEditar"));
  modal.show(); // FALTABA ESTA LÍNEA Y LA LLAVE DE CIERRE
}

// 🔹 Guardar cambios del modal
async function guardarEdicion() {
  const id = document.getElementById("edit-id").value;
  const nombre = document.getElementById("edit-nombre").value.trim();
  const descripcion = document.getElementById("edit-descripcion").value.trim();
  const precio = parseFloat(document.getElementById("edit-precio").value);
  const imagen = document.getElementById("edit-imagenUrl").value.trim();

  if (!nombre || !descripcion || isNaN(precio)) {
    alert("Completá todos los campos antes de guardar.");
    return;
  }

  try {
    await axios.put(`/api/productos/${id}`, { nombre, descripcion, precio, imagen });
    alert("✅ Producto actualizado correctamente");
    // Truco para cerrar el modal si la instancia bootstrap da problemas al re-instanciar
    const modalEl = document.getElementById('modalEditar');
    const modal = bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl);
    modal.hide();
    cargarProductos();
  } catch (err) {
    console.error(err);
    alert("❌ Error al actualizar el producto");
  }
}

// 🔹 Limpiar formulario
function limpiarCampos() {
  document.getElementById("nombre").value = "";
  document.getElementById("descripcion").value = "";
  document.getElementById("precio").value = "";
  document.getElementById("imagenUrl").value = "";
}

// Cargar productos al iniciar
window.onload = cargarProductos;