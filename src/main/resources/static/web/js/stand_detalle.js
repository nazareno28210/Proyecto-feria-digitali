// URL base del backend
const API_URL = "http://localhost:8080/api/stands";

// Obtener parámetro idStand de la URL
const params = new URLSearchParams(window.location.search);
const standId = params.get("idStand");

async function cargarStand() {
    try {
        const response = await fetch(`${API_URL}/${standId}`);
        if (!response.ok) throw new Error("Error al obtener el stand: " + response.status); // Modificado para incluir el status

        const stand = await response.json();
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
        console.error("Error al cargar el stand:", error);
        document.getElementById("info-stand").innerHTML = `<p>Error al cargar los datos. Revisa la Consola (F12) para detalles.</p>`;
    }
}

function volver() {
    window.history.back();
}

// Cargar stand al iniciar la página
document.addEventListener("DOMContentLoaded", cargarStand);