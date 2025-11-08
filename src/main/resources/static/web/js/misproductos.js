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
            <div class="card-body">
              <h5 class="card-title">${p.nombre}</h5>
              <p class="card-text text-muted">${p.descripcion}</p>
              <p><strong>$${p.precio.toFixed(2)}</strong></p>
              <div class="d-flex justify-content-between">
                <button class="btn btn-primary btn-sm" onclick="abrirModalEditar(${p.id}, '${p.nombre}', '${p.descripcion}', ${p.precio})">Editar</button>
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

  if (!nombre || !descripcion || isNaN(precio)) {
    alert("Por favor, completá todos los campos.");
    return;
  }

  try {
    await axios.post("/api/productos", { nombre, descripcion, precio });
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
function abrirModalEditar(id, nombre, descripcion, precio) {
  document.getElementById("edit-id").value = id;
  document.getElementById("edit-nombre").value = nombre;
  document.getElementById("edit-descripcion").value = descripcion;
  document.getElementById("edit-precio").value = precio;

  const modal = new bootstrap.Modal(document.getElementById("modalEditar"));
  modal.show();
}

// 🔹 Guardar cambios del modal
async function guardarEdicion() {
  const id = document.getElementById("edit-id").value;
  const nombre = document.getElementById("edit-nombre").value.trim();
  const descripcion = document.getElementById("edit-descripcion").value.trim();
  const precio = parseFloat(document.getElementById("edit-precio").value);

  if (!nombre || !descripcion || isNaN(precio)) {
    alert("Completá todos los campos antes de guardar.");
    return;
  }

  try {
    await axios.put(`/api/productos/${id}`, { nombre, descripcion, precio });
    alert("✅ Producto actualizado correctamente");
    const modal = bootstrap.Modal.getInstance(document.getElementById("modalEditar"));
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
}

// Cargar productos al iniciar
window.onload = cargarProductos;
