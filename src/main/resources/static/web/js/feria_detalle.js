// Tu archivo /web/js/feria_detalle.js (actualizado)

const API_URL = "http://localhost:8080/api/ferias";
const params = new URLSearchParams(window.location.search);
const feriaId = params.get("id");

async function cargarFeria() {
  try {
    // 1. CAMBIO: Usamos axios.get() en lugar de fetch()
    const response = await axios.get(`${API_URL}/${feriaId}`);

    // 2. CAMBIO: Los datos están en 'response.data'
    //    Ya no se necesita 'response.ok' ni 'response.json()'
    const feria = response.data;

    // ----- Todo lo que sigue es exactamente igual -----

    // Mostrar info general (ocultando estado visualmente)
    const infoFeria = document.getElementById("info-feria");
    infoFeria.innerHTML = `
      <p><strong>Lugar:</strong> ${feria.lugar}</p>
      <p><strong>Fecha inicio:</strong> ${feria.fechaInicio}</p>
      <p><strong>Fecha fin:</strong> ${feria.fechaFinal}</p>
      <p><strong>Descripción:</strong> ${feria.descripcion}</p>
      <p hidden><strong>Estado:</strong> ${feria.estado}</p> `;

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
    // 3. CAMBIO: Este 'catch' ahora también captura errores 404 o 500
    //    (Axios los lanza automáticamente)
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