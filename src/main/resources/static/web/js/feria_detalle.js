/*
 * ====================================
 * FERIA-DETALLE.JS (Actualizado: Filtro de Stands Activos)
 * ====================================
 */

// 1. Función Toastify (Mantenida) [cite: 168-173]
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
    duration: 2000,
    gravity: "top", 
    position: "right", 
    style: {
        background: color,
    },
    stopOnFocus: true,
  }).showToast();
}

const API_URL = "http://localhost:8080/api/ferias";
const params = new URLSearchParams(window.location.search);
const feriaId = params.get("id");

document.addEventListener("DOMContentLoaded", cargarFeria);

async function cargarFeria() {
  try {
    const response = await axios.get(`${API_URL}/${feriaId}`);
    const feria = response.data; 

    // ===================================
    // Renderizado de Info de la Feria [cite: 176-179]
    // ===================================
    const infoGrid = document.getElementById("info-feria-grid");
    infoGrid.innerHTML = `
      <div class="info-item">
        <i class="fas fa-map-marker-alt"></i>
        <div class="info-item-content">
          <strong>Lugar</strong>
          <span>${feria.lugar}</span>
        </div>
      </div>
      <div class="info-item">
        <i class="fas fa-calendar-check"></i>
        <div class="info-item-content">
          <strong>Fecha inicio</strong>
          <span>${feria.fechaInicio}</span>
        </div>
      </div>
      <div class="info-item">
        <i class="fas fa-calendar-times"></i>
        <div class="info-item-content">
          <strong>Fecha fin</strong>
          <span>${feria.fechaFinal}</span>
        </div>
      </div>
    `;

    const infoDesc = document.getElementById("info-feria-desc");
    infoDesc.innerHTML = `
      <p><strong>Descripción:</strong></p>
      <p>${feria.descripcion}</p>
    `;

    document.getElementById("nombre-feria").textContent = feria.nombre; 

    // ===================================
    // Lógica de Stands (CON FILTRO DE ACTIVACIÓN) 
    // ===================================
    const standsContainer = document.getElementById("stands-container");
    standsContainer.innerHTML = "";
    
    // 🟢 CAMBIO CLAVE: Filtramos la lista para mostrar solo stands que tengan activo: true
    const standsVisibles = (feria.stands || []).filter(stand => stand.activo === true);

    if (standsVisibles.length > 0) {
      standsVisibles.forEach((stand) => {
        const div = document.createElement("div");
        div.classList.add("stand-card");

        const imagenHtml = stand.imagenUrl
          ? `<div class="stand-image-container">
               <img src="${stand.imagenUrl}" alt="Logo de ${stand.nombre}">
             </div>`
          : ''; 

        div.innerHTML = `
          ${imagenHtml} 
          <div class="stand-content">
            <h3>${stand.nombre}</h3>
            <p>${stand.descripcion ?? "Sin descripción"}</p>
            <p><strong>Feriante:</strong> ${
              stand.feriante ? stand.feriante.nombreEmprendimiento : "No asignado"
            }</p>
          </div>
          <button class="btn-stand" onclick="verProductos(${stand.id})">Ver productos</button>
        `; 
        standsContainer.appendChild(div);
      });
    } else {
      // Si la feria tiene stands asignados pero todos están "Cerrados", mostramos este mensaje 
      standsContainer.innerHTML =
        "<p class='no-stands-msg'>Actualmente no hay stands disponibles para visitar en esta feria.</p>";
    }
  } catch (error) {
    console.error("Error al cargar la feria:", error); 
    showToast("❌ Error al cargar los datos de la feria.", "error"); 
    document.getElementById("info-feria-grid").innerHTML =
      "<p>Error al cargar los datos.</p>"; 
  }
}

// Funciones de navegación (Sin cambios) [cite: 188-189]
function volver() {
  window.location.href = "ferias.html";
}

function verProductos(standId) {
  window.location.href = `stand_detalle.html?idStand=${standId}`;
}