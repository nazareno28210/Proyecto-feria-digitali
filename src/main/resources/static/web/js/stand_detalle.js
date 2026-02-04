/*
 * ====================================
 * STAND-DETALLE.JS
 * ====================================
 */

// Función para mostrar notificaciones
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

        // 1. Llenar la sección de información del Stand
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

        // Actualizar el título en el header
        document.getElementById("nombre-stand").textContent = stand.nombre;

        // 2. Renderizar los productos
        const productosContainer = document.getElementById("productos-container");
        productosContainer.innerHTML = "";

        // VALIDACIÓN: Si el DTO filtró los productos y la lista está vacía
        if (stand.productos && stand.productos.length > 0) {
            stand.productos.forEach(producto => {
                const div = document.createElement("div");
                div.classList.add("producto-card");

                // Imagen con respaldo (placeholder) si no existe la URL
                const imagenUrl = producto.imagenUrl
                    ? producto.imagenUrl
                    : "https://placehold.co/300x200?text=Sin+Imagen";
                
                div.innerHTML = `
                    <img src="${imagenUrl}" alt="${producto.nombre}" style="width: 100%; height: 150px; object-fit: cover; border-radius: 8px 8px 0 0;">
                    <div class="producto-card-content">
                        <h3>${producto.nombre}</h3>
                        <p>${producto.descripcion}</p>
                        <p><strong>Precio:</strong> $${producto.precio.toFixed(2)}</p>
                        <p><strong>Categorías:</strong> ${
                            producto.categorias && producto.categorias.length > 0 
                            ? producto.categorias.map(c => c.nombre).join(", ") 
                            : "General"
                        }</p>
                    </div>
                `;
                productosContainer.appendChild(div);
            });
        } else {
            // Mensaje que se muestra cuando no hay productos activos o disponibles
            productosContainer.innerHTML = `
                <div class="no-products-container" style="grid-column: 1 / -1; text-align: center; padding: 50px;">
                    <p class='no-products-msg' style="font-size: 1.2rem; color: #666;">
                        Este feriante no tiene productos disponibles actualmente.
                    </p>
                </div>`;
        }

    } catch (error) {
        console.error("Error al cargar el stand:", error);
        showToast("❌ Error al cargar los datos del stand.", "error");
        const infoStand = document.getElementById("info-stand");
        if (infoStand) {
            infoStand.innerHTML = `<p class="text-danger">Error al cargar los datos. Por favor, reintente más tarde.</p>`;
        }
    }
}

function volver() {
    window.history.back();
}

// Inicializar la carga cuando el DOM esté listo
document.addEventListener("DOMContentLoaded", cargarStand);