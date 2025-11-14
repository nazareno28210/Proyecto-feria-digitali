// Tu archivo /web/js/feria_detalle.js (actualizado)

const API_URL = "http://localhost:8080/api/ferias";
const params = new URLSearchParams(window.location.search);
const feriaId = params.get("id");

// 1. LLAMAR A LA FUNCIÓN AL CARGAR EL DOM
document.addEventListener("DOMContentLoaded", cargarFeria);

async function cargarFeria() {
  try {
    const response = await axios.get(`${API_URL}/${feriaId}`);
    const feria = response.data;

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

        // 1. Crear el HTML de la imagen (solo si existe)
        const imagenHtml = stand.imagenUrl
          ? `<div class="stand-image-container">
               <img src="${stand.imagenUrl}" alt="Logo de ${stand.nombre}">
             </div>`
          : '';

        // 2. Construir la tarjeta (con la imagen arriba)
        div.innerHTML = `
          ${imagenHtml} 
          <div class="stand-content">
            <h3>${stand.nombre}</h3>
            <p>${stand.descripcion ?? "Sin descripción"}</p>
            <p><strong>Feriante:</strong> ${
              stand.feriante ? stand.feriante.nombreEmprendimiento : "No asignado"
            }</p>
          </div>

          <button class="btn btn-primary" onclick="verProductos(${stand.id})">Ver productos</button>
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

// 2. DEFINIR LA FUNCIÓN 'volver'
function volver() {
  window.location.href = "ferias.html";
}

// 3. DEFINIR LA FUNCIÓN 'verProductos'
function verProductos(standId) {
  window.location.href = `stand_detalle.html?idStand=${standId}`;
}