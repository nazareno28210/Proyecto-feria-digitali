/*
 * ====================================
 * STAND-DETALLE.JS (con Toastify y Rediseño)
 * ====================================
 */

// 1. AÑADIDA: Función Toastify
function showToast(message, type = "info") {
  let color;
  switch (type) {
    case "success":
      color = "linear-gradient(to right, #1a3a5a, #3b82f6)"; 
      break;
    case "error":
      color = "linear-gradient(to right, #ef4444, #b91c1c)"; 
      break;
    case "warning":
      color = "linear-gradient(to right, #3b82f6, #67e8f9)";
      break;
    default:
      color = "linear-gradient(to right, #3b82f6, #67e8f9)"; 
  }
  Toastify({
    text: message,
    duration: 4000,
    gravity: "top", 
    position: "right", 
    style: {
        background: color,
    },
    stopOnFocus: true,
  }).showToast();
}


const API_URL = "http://localhost:8080/api/stands";
const params = new URLSearchParams(window.location.search);
const standId = params.get("idStand");

async function cargarStand() {
    try {
        const response = await axios.get(`${API_URL}/${standId}`);
        const stand = response.data;

        // ===================================
        // CAMBIO: Llenar la nueva sección de Info
        // ===================================
        const infoStand = document.getElementById("info-stand");
        infoStand.innerHTML = `
            <div class="info-item">
                <i class="fas fa-store"></i>
                <div class="info-item-content">
                    <strong>Nombre del Stand</strong>
                    <span>${stand.nombre}</span>
                </div>
            </div>
            <div class="info-item">
                <i class="fas fa-user-tag"></i>
                <div class="info-item-content">
                    <strong>Feriante</strong>
                    <span>${stand.feriante ? stand.feriante.nombreEmprendimiento : "No asignado"}</span>
                </div>
            </div>
            <div class="info-item">
                <i class="fas fa-info-circle"></i>
                <div class="info-item-content">
                    <strong>Descripción</strong>
                    <span>${stand.descripcion}</span>
                </div>
            </div>
        `;

        // Pone el nombre del stand en el H1 del header
        document.getElementById("nombre-stand").textContent = stand.nombre;

        // Mostrar productos (sin cambios en la lógica)
        const productosContainer = document.getElementById("productos-container");
        productosContainer.innerHTML = "";

        if (stand.productos && stand.productos.length > 0) {
            stand.productos.forEach(producto => {
                const div = document.createElement("div");
                div.classList.add("producto-card");

                // La imagen (con 'placeholder' si no existe)
                const imagenHtml = producto.imagen
                    ? `<img src="${producto.imagen}" alt="${producto.nombre}" style="width: 100%; height: 150px; object-fit: cover; border-radius: 8px 8px 0 0;">`
                    : `<img src="https://via.placeholder.com/220x150?text=Sin+Imagen" alt="${producto.nombre}" style="width: 100%; height: 150px; object-fit: cover; border-radius: 8px 8px 0 0;">`;
                
                div.innerHTML = `
                    ${imagenHtml}
                    <div class="producto-card-content">
                        <h3>${producto.nombre}</h3>
                        <p>${producto.descripcion}</p>
                        <p><strong>Precio:</strong> $${producto.precio}</p>
                        <p><strong>Categorías:</strong> ${producto.categorias.map(c => c.nombre).join(", ")}</p>
                    </div>
                `;
                productosContainer.appendChild(div);
            });
        } else {
            // CAMBIO: Mensaje de "no hay productos" con clase
            productosContainer.innerHTML = "<p class='no-products-msg'>No hay productos registrados para este stand.</p>";
        }

    } catch (error) {
        console.error("Error al cargar el stand:", error);
        // CAMBIO: alert a toast
        showToast("❌ Error al cargar los datos del stand.", "error");
        document.getElementById("info-stand").innerHTML = `<p>Error al cargar los datos.</p>`;
    }
}

function volver() {
    window.history.back(); // Vuelve a la página anterior
}

// Cargar stand al iniciar la página
document.addEventListener("DOMContentLoaded", cargarStand);