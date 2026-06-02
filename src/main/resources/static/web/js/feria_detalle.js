/*
 * ====================================
 * FERIA-DETALLE.JS 
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

// 🟢 1. Apuntamos al nuevo endpoint de ediciones
const API_URL = "http://localhost:8080/api/ediciones";
const params = new URLSearchParams(window.location.search);
const edicionId = params.get("id"); 

document.addEventListener("DOMContentLoaded", () => {
    cargarFeria();
    verificarAccesoVoto(); 
});

async function cargarFeria() {
  try {
    const response = await axios.get(`${API_URL}/${edicionId}`);
    const edicion = response.data;

    // 🟢 2. Renderizado de Info de la Edición (Usando tu misma estructura HTML)
    const infoGrid = document.getElementById("info-feria-grid");
    
    // Formatear horas ("14:00:00" -> "14:00")
    const horaApertura = edicion.horaInicio ? edicion.horaInicio.substring(0, 5) : "??:??";
    const horaCierre = edicion.horaFin ? edicion.horaFin.substring(0, 5) : "??:??";

    infoGrid.innerHTML = `
      <div class="info-item">
        <i class="fas fa-map-marker-alt"></i>
        <div class="info-item-content">
          <strong>Lugar</strong>
          <span>${edicion.feriaLugar || "Lugar a definir"}</span>
        </div>
      </div>
      <div class="info-item">
        <i class="fas fa-calendar-check"></i>
        <div class="info-item-content">
          <strong>Fecha inicio</strong>
          <span>${edicion.fechaInicio}</span>
        </div>
      </div>
      <div class="info-item">
        <i class="fas fa-calendar-times"></i>
        <div class="info-item-content">
          <strong>Fecha fin</strong>
          <span>${edicion.fechaFinal ?? "..."}</span>
        </div>
      </div>
      <div class="info-item">
        <i class="fas fa-clock"></i>
        <div class="info-item-content">
          <strong>Horarios</strong>
          <span>${horaApertura} a ${horaCierre} hs</span>
        </div>
      </div>
    `;

    const infoDesc = document.getElementById("info-feria-desc");
    infoDesc.innerHTML = `<p><strong>Descripción:</strong></p><p>${edicion.feriaDescripcion || ""}</p>`;

    // 🟢 Título con nombre de molde y nombre de edición
    document.getElementById("nombre-feria").innerHTML = `${edicion.feriaNombre || "Feria General"} <br><span style="font-size:0.6em; color:#e67e22;">(${edicion.nombreEdicion})</span>`;

    // Pasamos el ID del molde para los votos
    renderizarAprobacionFeria(edicion.feriaId);

    // 🟢 3. Lógica de Stands: Ahora buscamos las participaciones de esta edición
    const standsContainer = document.getElementById("stands-container");
    standsContainer.innerHTML = "";
    
    try {
        const participacionesRes = await axios.get(`http://localhost:8080/api/participaciones/edicion/${edicionId}`);
        const participacionesConfirmadas = participacionesRes.data.filter(p => p.estado === "CONFIRMADO");

        if (participacionesConfirmadas.length > 0) {
            participacionesConfirmadas.forEach((participacion) => {
                const stand = participacion.stand;
                if (stand && stand.activo) {
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
                }
            });
        } else {
            standsContainer.innerHTML = "<p class='no-stands-msg'>Actualmente no hay stands disponibles para visitar en esta feria.</p>";
        }
    } catch(e) {
        standsContainer.innerHTML = "<p class='no-stands-msg'>No se pudieron cargar los stands.</p>";
    }
  } catch (error) {
    console.error("Error al cargar la feria:", error);
    showToast("❌ Error al cargar los datos.", "error"); 
  }
}

// ⭐ Dibuja el porcentaje de aprobación (busca directo de la feria molde)
async function renderizarAprobacionFeria(feriaId) {
    const contenedor = document.getElementById("aprobacion-header");
    if (!contenedor || !feriaId) return;

    try {
        const res = await axios.get(`http://localhost:8080/api/ferias/${feriaId}`);
        const porcentaje = res.data.porcentajeAprobacion;
        const total = res.data.totalVotos;

        if (!total || total === 0) {
            contenedor.innerHTML = '<span class="badge bg-secondary opacity-75">Sin votos aún</span>';
            return;
        }

        let colorClase = "bg-success";
        if (porcentaje < 70) colorClase = "bg-warning text-dark";
        if (porcentaje < 40) colorClase = "bg-danger";

        contenedor.innerHTML = `
            <span class="badge ${colorClase} shadow-sm">
                <i class="bi bi-hand-thumbs-up-fill me-1"></i> 
                ${porcentaje}% lo recomienda (${total} votos)
            </span>
        `;
    } catch(err) {
         contenedor.innerHTML = '<span class="badge bg-secondary opacity-75">Votos no disponibles</span>';
    }
}

// ⭐ Envía el voto (5 para SI, 1 para NO) a la feria molde
async function votarFeria(valor) {
    try {
        const responseEdicion = await axios.get(`${API_URL}/${edicionId}`);
        const realFeriaId = responseEdicion.data.feriaId;

        await axios.post("/api/resenas", {
            puntaje: valor,
            feria: { id: realFeriaId }
        }, { withCredentials: true });

        showToast("👍 ¡Gracias por tu voto!", "success");
        setTimeout(() => location.reload(), 1200);
    } catch (err) {
        const msg = err.response ? err.response.data : "Error al votar.";
        showToast(`❌ ${msg}`, "error");
    }
}

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