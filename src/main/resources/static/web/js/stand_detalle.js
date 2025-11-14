// URL base del backend
const API_URL = "http://localhost:8080/api/stands";

// Obtener parámetro idStand de la URL
const params = new URLSearchParams(window.location.search);
const standId = params.get("idStand");

async function cargarStand() {
    try {
        // 1. CAMBIO: Usamos axios.get()
        const response = await axios.get(`${API_URL}/${standId}`);

        // 2. CAMBIO: Los datos están en 'response.data'
        //    Ya no se necesita 'response.ok' ni 'response.json()'
        const stand = response.data;

        // ----- Todo lo que sigue es exactamente igual -----

        // Mostrar info general del stand
        const infoStand = document.getElementById("info-stand");
        infoStand.innerHTML = `
            <p><strong>Nombre:</strong> ${stand.nombre}</p>
            <p><strong>Descripción:</strong> ${stand.descripcion}</p>
            <p><strong>Feriante:</strong> ${stand.feriante ?
        stand.feriante.nombreEmprendimiento : "No asignado"}</p>
        `;

        document.getElementById("nombre-stand").textContent = stand.nombre;
        // Mostrar productos
        const productosContainer = document.getElementById("productos-container");
        productosContainer.innerHTML = "";

        if (stand.productos && stand.productos.length > 0) {
            stand.productos.forEach(producto => {
                const div = document.createElement("div");
                div.classList.add("producto-card");

                // MODIFICADO: Se añade el div producto-card-content para manejar el scroll
                div.innerHTML = `
                    <img src="${producto.imagen || 'https://via.placeholder.com/150'}" alt="${producto.nombre}" style="width: 100%; height: 150px; object-fit: cover; border-radius: 8px 8px 0 0;">
                        <h3>${producto.nombre}</h3>
                    <div class="producto-card-content">
                        <p>${producto.descripcion}</p>
                        <p><strong>Precio:</strong> $${producto.precio}</p>
                        <p><strong>Categorías:</strong> ${producto.categorias.map(c => c.nombre).join(", ")}</p>
                    </div>
                `;
                productosContainer.appendChild(div);

            });
        } else {
            productosContainer.innerHTML = "<p>No hay productos registrados para este stand.</p>";
        }

    } catch (error) {
        // 3. CAMBIO: Este 'catch' ahora también captura errores 404 o 500
        console.error("Error al cargar el stand:", error);
        document.getElementById("info-stand").innerHTML = `<p>Error al cargar los datos. Revisa la Consola (F12) para detalles.</p>`;
    }
}

function volver() {
    window.history.back(); // Esto vuelve a la página anterior
}

// Cargar stand al iniciar la página
document.addEventListener("DOMContentLoaded", cargarStand);