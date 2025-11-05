const API_URL = "http://localhost:8080/api/ferias";
const params = new URLSearchParams(window.location.search);
const feriaId = params.get("id");

async function cargarFeria() {
  try {
    const response = await fetch(`${API_URL}/${feriaId}`);
    if (!response.ok) throw new Error("Error al obtener la feria");

    const feria = await response.json();

    // Mostrar info general (ocultando estado visualmente)
    const infoFeria = document.getElementById("info-feria");
    infoFeria.innerHTML = `
      <p><strong>Lugar:</strong> ${feria.lugar}</p>
      <p><strong>Fecha inicio:</strong> ${feria.fechaInicio}</p>
      <p><strong>Fecha fin:</strong> ${feria.fechaFinal}</p>
      <p><strong>Descripción:</strong> ${feria.descripcion}</p>
      <p hidden><strong>Estado:</strong> ${feria.estado}</p> <!-- 🔹 oculto -->
    `;

    document.getElementById("nombre-feria").textContent = feria.nombre;

    const standsContainer = document.getElementById("stands-container");
    standsContainer.innerHTML = "";

    if (feria.stands && feria.stands.length > 0) {
      feria.stands.forEach((stand) => {
        const div = document.createElement("div");
        div.classList.add("stand-card");
        div.innerHTML = `
          <div class="stand-content">
            <h3>${stand.nombre}</h3>
            <p>${stand.descripcion ?? "Sin descripción"}</p>
            <p><strong>Feriante:</strong> ${
              stand.feriante ? stand.feriante.nombreEmprendimiento : "No asignado"
            }</p>
          </div>
          <button onclick="verProductos(${stand.id})">Ver productos</button>
        `;
        standsContainer.appendChild(div);
      });
    } else {
      standsContainer.innerHTML =
        "<p>No hay stands registrados para esta feria.</p>";
    }
  } catch (error) {
    console.error("Error al cargar la feria:", error);
    document.getElementById("info-feria").innerHTML =
      "<p>Error al cargar los datos.</p>";
  }
}

function volver() {
  window.location.href = "ferias.html";
}

function verProductos(standId) {
  window.location.href = `stand_detalle.html?idStand=${standId}`;
}

document.addEventListener("DOMContentLoaded", cargarFeria);
