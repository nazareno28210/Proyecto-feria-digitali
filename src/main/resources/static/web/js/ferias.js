// URL del endpoint del backend
const API_URL = "http://localhost:8080/api/ferias";

async function cargarFerias() {
    try {
        const response = await fetch(API_URL);
        const ferias = await response.json();

        const container = document.getElementById("ferias-container");
        container.innerHTML = ""; // Limpiar el contenedor

        ferias.forEach(feria => {
            const card = document.createElement("div");
            card.classList.add("card");

            card.innerHTML = `
                <h2>${feria.nombre}</h2>
                <p><strong>Lugar:</strong> ${feria.lugar}</p>
                <p><strong>Fecha inicio:</strong> ${feria.fechaInicio}</p>
                <p><strong>Fecha fin:</strong> ${feria.fechaFin}</p>
                <p>${feria.descripcion}</p>
                <button onclick="verDetalles(${feria.id})">Ver detalles</button>
            `;

            container.appendChild(card);
        });

    } catch (error) {
        console.error("Error al cargar las ferias:", error);
    }
}

function verDetalles(id) {
    window.location.href = `feria_detalle.html?id=${id}`;
}

// Cargar ferias al iniciar la página
document.addEventListener("DOMContentLoaded", cargarFerias);
