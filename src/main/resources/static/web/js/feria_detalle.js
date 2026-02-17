/*
 * ====================================
 * FERIA-DETALLE.JS (Versión con Votación de Pulgares)
 * ====================================
 */

function showToast(message, type = "info") {
  let color;
  switch (type) {
    case "success": color = "linear-gradient(to right, #1a3a5a, #3b82f6)"; break;
    case "error": color = "linear-gradient(to right, #ef4444, #b91c1c)"; break;
    case "warning": color = "linear-gradient(to right, #3b82f6, #67e8f9)"; break;
    default: color = "linear-gradient(to right, #3b82f6, #67e8f9)";
  }
  Toastify({
    text: message,
    duration: 2000,
    gravity: "top", 
    position: "right", 
    style: { background: color },
    stopOnFocus: true,
  }).showToast();
}

const API_URL = "http://localhost:8080/api/ferias";
const params = new URLSearchParams(window.location.search);
const feriaId = params.get("id");

document.addEventListener("DOMContentLoaded", () => {
    cargarFeria();
    verificarAccesoVoto(); // Verificamos si el usuario puede votar
});

async function cargarFeria() {
  try {
    const response = await axios.get(`${API_URL}/${feriaId}`);
    const feria = response.data; 

    // 1. Renderizado de Info de la Feria
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
    infoDesc.innerHTML = `<p><strong>Descripción:</strong></p><p>${feria.descripcion}</p>`;

    document.getElementById("nombre-feria").textContent = feria.nombre; 

    // ⭐ NUEVO: Renderizar el porcentaje de aprobación en la cabecera
    renderizarAprobacionFeria(feria.porcentajeAprobacion, feria.totalVotos);

    // 2. Lógica de Stands
    const standsContainer = document.getElementById("stands-container");
    standsContainer.innerHTML = "";
    
    const standsVisibles = (feria.stands || []).filter(stand => stand.activo === true);

    if (standsVisibles.length > 0) {
      standsVisibles.forEach((stand) => {
        const div = document.createElement("div");
        div.classList.add("stand-card");
        const imagenHtml = stand.imagenUrl
          ? `<div class="stand-image-container"><img src="${stand.imagenUrl}" alt="Logo de ${stand.nombre}"></div>`
          : ''; 

        div.innerHTML = `
          ${imagenHtml} 
          <div class="stand-content">
            <h3>${stand.nombre}</h3>
            <p>${stand.descripcion ?? "Sin descripción"}</p>
            <p><strong>Feriante:</strong> ${stand.feriante ? stand.feriante.nombreEmprendimiento : "No asignado"}</p>
          </div>
          <button class="btn-stand" onclick="verProductos(${stand.id})">Ver productos</button>
        `; 
        standsContainer.appendChild(div);
      });
    } else {
      standsContainer.innerHTML = "<p class='no-stands-msg'>Actualmente no hay stands disponibles para visitar en esta feria.</p>";
    }
  } catch (error) {
    console.error("Error al cargar la feria:", error); 
    showToast("❌ Error al cargar los datos.", "error"); 
  }
}

// ⭐ NUEVA FUNCIÓN: Dibuja el porcentaje de aprobación
function renderizarAprobacionFeria(porcentaje, total) {
    const contenedor = document.getElementById("aprobacion-header");
    if (!contenedor) return;

    if (!total || total === 0) {
        contenedor.innerHTML = '<span class="badge bg-secondary opacity-75">Sin votos aún</span>';
        return;
    }

    // Color del badge según aprobación
    let colorClase = "bg-success";
    if (porcentaje < 70) colorClase = "bg-warning text-dark";
    if (porcentaje < 40) colorClase = "bg-danger";

    contenedor.innerHTML = `
        <span class="badge ${colorClase} shadow-sm">
            <i class="bi bi-hand-thumbs-up-fill me-1"></i> 
            ${porcentaje}% lo recomienda (${total} votos)
        </span>
    `;
}

// ⭐ NUEVA FUNCIÓN: Envía el voto (5 para SI, 1 para NO)
async function votarFeria(valor) {
    try {
        await axios.post("/api/resenas", {
            puntaje: valor,
            feria: { id: parseInt(feriaId) }
        }, { withCredentials: true });

        showToast("👍 ¡Gracias por tu voto!", "success");
        setTimeout(() => location.reload(), 1200);
    } catch (err) {
        const msg = err.response ? err.response.data : "Error al votar.";
        showToast(`❌ ${msg}`, "error");
    }
}

// ⭐ NUEVA FUNCIÓN: Muestra la sección de voto solo a usuarios logueados
async function verificarAccesoVoto() {
    try {
        const res = await axios.get("/api/usuarios/current", { withCredentials: true });
        if (res.data) {
            document.getElementById("seccion-voto-feria").style.display = "block";
        }
    } catch (e) {
        console.log("Visitante: sección de voto oculta.");
    }
}

function volver() { window.location.href = "ferias.html"; }
function verProductos(standId) { window.location.href = `stand_detalle.html?idStand=${standId}`; }