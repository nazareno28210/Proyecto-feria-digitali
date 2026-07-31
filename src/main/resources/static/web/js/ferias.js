const API_URL = "http://localhost:8080/api/ediciones/activas"; // 🟢 Endpoint actualizado
const AUTH_URL = "http://localhost:8080/api/usuarios/current";
const LOGOUT_URL = "http://localhost:8080/api/logout";
const SOLICITUD_URL = "http://localhost:8080/api/solicitudes";

let feriasGlobal = [];

// Variables globales para el Mapa
const RIO_GRANDE_COORDS = [-53.7860, -67.7070];
let mapa;
let markersGroup;

// 🔹 Función Toastify 
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

// 🔹 INIT: Configuración inicial al cargar la página
document.addEventListener("DOMContentLoaded", () => {
    inicializarMapa();
    cargarDatosFerias(); // Carga datos, tarjetas y marcadores
    verificarSesion();

    const inputBusqueda = document.getElementById("busqueda");
    inputBusqueda.addEventListener("input", () => {
        const texto = inputBusqueda.value.toLowerCase();
        const filtradas = feriasGlobal.filter((f) =>
            (f.feriaNombre || "").toLowerCase().includes(texto) || 
            (f.nombreEdicion || "").toLowerCase().includes(texto) ||
            (f.feriaLugar || "").toLowerCase().includes(texto)
        );
        mostrarFerias(filtradas);
        actualizarMarcadoresMapa(filtradas); // El mapa se filtra en tiempo real
    });
});

// ========================= SECCIÓN MAPAS =========================

function inicializarMapa() {
    mapa = L.map('mapa-ferias').setView(RIO_GRANDE_COORDS, 13);
    
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors'
    }).addTo(mapa);
    
    markersGroup = L.layerGroup().addTo(mapa);
}

function actualizarMarcadoresMapa(lista) {
    markersGroup.clearLayers();

    lista.forEach(edicion => {
        const lat = edicion.latitud;
        const lng = edicion.longitud;
        
        if (lat && lng) {
            const marcador = L.marker([lat, lng]);
            
            const horaApertura = edicion.horaInicio ? edicion.horaInicio.substring(0, 5) : "??:??";
            const horaCierre = edicion.horaFin ? edicion.horaFin.substring(0, 5) : "??:??";
            
            const nombreBase = edicion.feriaNombre || "Feria General";
            const lugarBase = edicion.feriaLugar || "Lugar a definir";

            marcador.bindPopup(`
                <div style="text-align: center; font-family: sans-serif; padding: 4px;">
                    <strong style="color: #1a3a5a; font-size: 1.05rem; display: block; margin-bottom: 2px;">${nombreBase}</strong>
                    <span style="color: #2563eb; font-size: 0.85rem; font-weight: 700; display: block; margin-bottom: 4px;">${edicion.nombreEdicion}</span>
                    <small style="color: #64748b; display: block; margin-bottom: 6px;">📍 ${lugarBase}</small>
                    <small style="color: #1e293b; font-weight: 600; display: block; margin-bottom: 8px;">⏰ ${horaApertura} a ${horaCierre} hs</small>
                    <button onclick="verDetalles(${edicion.id})" 
                            style="width: 100%; background: linear-gradient(135deg, #1a3a5a, #2563eb); color: white; border: none; padding: 6px 12px; border-radius: 8px; cursor: pointer; font-weight: 600; font-size: 0.85rem;">
                        <i class="bi bi-eye-fill"></i> Ver Feria y Stands
                    </button>
                </div>
            `);
            
            marcador.on('click', function() {
                mapa.flyTo([lat, lng], 15, { animate: true, duration: 1.2 });
            });
            
            markersGroup.addLayer(marcador);
        }
    });
}

// ========================= SECCIÓN FERIAS =========================

async function cargarDatosFerias() {
    try {
        const res = await axios.get(API_URL); 
        feriasGlobal = res.data;
        mostrarFerias(feriasGlobal);
        actualizarMarcadoresMapa(feriasGlobal);
    } catch (err) {
        console.error("Error al cargar las ferias:", err);
        showToast("❌ Error al cargar las ferias", "error");
    }
}

function mostrarFerias(lista) {
  const container = document.getElementById("ferias-container");
  container.innerHTML = "";
  
  if (lista.length === 0) {
      container.innerHTML = `<div class='no-ferias-msg'><i class="bi bi-search" style="font-size: 2rem; display: block; margin-bottom: 10px;"></i>No se encontraron ferias activas que coincidan con la búsqueda.</div>`;
      return;
  }

  lista.forEach((edicion) => {
    const card = document.createElement("div");
    card.classList.add("card-feria");
    
    const imagenBase = edicion.feriaImagenUrl || "";
    const nombreBase = edicion.feriaNombre || "Feria General";
    const lugarBase = edicion.feriaLugar || "Lugar a definir";
    const descBase = edicion.feriaDescripcion || "Sin descripción disponible.";
    const estadoStr = (edicion.estado || "ACTIVA").toUpperCase();

    let badgeClass = "badge-activa";
    if (estadoStr.includes("PROXIMA") || estadoStr.includes("PRÓXIMA")) {
        badgeClass = "badge-proxima";
    } else if (estadoStr.includes("FINALIZADA") || estadoStr.includes("INACTIVA")) {
        badgeClass = "badge-finalizada";
    }

    const imagenHtml = imagenBase
      ? `<img src="${imagenBase}" alt="${nombreBase}" onerror="this.src='/web/assets/logo.png'; this.style.objectFit='contain'; this.style.padding='20px';">`
      : `<img src="/web/assets/logo.png" alt="${nombreBase}" style="object-fit: contain; padding: 25px;">`;

    const horaApertura = edicion.horaInicio ? edicion.horaInicio.substring(0, 5) : "A definir";
    const horaCierre = edicion.horaFin ? edicion.horaFin.substring(0, 5) : "A definir";

    const fechaInicioStr = edicion.fechaInicio ? edicion.fechaInicio : "Por definir";
    const fechaFinStr = edicion.fechaFinal ? edicion.fechaFinal : fechaInicioStr;

    card.innerHTML = `
      <div class="card-image-wrapper">
        ${imagenHtml}
        <span class="card-status-badge ${badgeClass}"><i class="bi bi-record-fill"></i> ${estadoStr}</span>
      </div>
      <div class="card-content-body">
        <h3 class="card-feria-title">${nombreBase}</h3>
        <span class="card-edition-title"><i class="bi bi-stars"></i> ${edicion.nombreEdicion}</span>
        
        <div class="card-meta-list">
          <div class="card-meta-item">
            <i class="bi bi-geo-alt-fill text-danger"></i>
            <span><strong>Ubicación:</strong> ${lugarBase}</span>
          </div>
          <div class="card-meta-item">
            <i class="bi bi-calendar-event-fill text-primary"></i>
            <span><strong>Fechas:</strong> ${fechaInicioStr} al ${fechaFinStr}</span>
          </div>
          <div class="card-meta-item">
            <i class="bi bi-clock-fill text-warning"></i>
            <span><strong>Horario:</strong> ${horaApertura} a ${horaCierre} hs</span>
          </div>
        </div>

        <p class="card-description">${descBase}</p>
      </div>

      <div class="card-action-bar">
        <button class="btn-card-action" onclick="verDetalles(${edicion.id})">
          <i class="bi bi-arrow-right-circle-fill"></i> Ver Feria y Stands
        </button>
      </div>
    `;
    container.appendChild(card);
  }); 
}

function verDetalles(id) {
  window.location.href = `feria_detalle.html?id=${id}`;
}

// ========================= SESIÓN Y ROLES =========================

async function verificarSesion() {
  try {
    const response = await axios.get(AUTH_URL, { withCredentials: true });
    if (response.status === 200 && response.data) {
      mostrarOpcionesUsuario(response.data);
    }
  } catch (error) {
    console.log("Usuario no autenticado (modo visitante)");
    
    document.getElementById("user-actions").innerHTML = `
      <a href="buscar.html" class="btn btn-header">
        <i class="bi bi-cart-fill"></i> Buscar Productos
      </a>
      <a href="/web/login.html" class="btn btn-header"><i class="bi bi-box-arrow-in-right"></i> Iniciar sesión</a>
    `;
  }
}

async function mostrarOpcionesUsuario(usuario) {
  const container = document.getElementById("user-actions");
  container.innerHTML = ""; 

  const btnBuscar = document.createElement("a");
  btnBuscar.href = "buscar.html";
  btnBuscar.className = "btn btn-header"; 
  btnBuscar.innerHTML = '<i class="bi bi-cart-fill"></i> Buscar Productos';
  container.appendChild(btnBuscar);

  const btnLogout = document.createElement("button");
  btnLogout.id = "btn-logout";
  btnLogout.className = "btn btn-logout";
  btnLogout.innerHTML = '<i class="bi bi-box-arrow-right"></i> Cerrar sesión';
  btnLogout.addEventListener("click", cerrarSesion);

  if (usuario.tipoUsuario === "NORMAL") {
    const btnPerfil = document.createElement("a");
    btnPerfil.href = "/web/usuario-perfil.html";
    btnPerfil.className = "btn btn-header";
    btnPerfil.innerHTML = '<i class="bi bi-person-circle"></i> Mi Perfil';
    container.appendChild(btnPerfil);
  }

  if (usuario.tipoUsuario === "FERIANTE") {
    const btnPerfil = document.createElement("a");
    btnPerfil.href = "/web/feriante/perfil.html";
    btnPerfil.className = "btn btn-header";
    btnPerfil.innerHTML = '<i class="bi bi-shop"></i> Mi Perfil';
    container.appendChild(btnPerfil);
  }

  if (usuario.tipoUsuario === "ADMINISTRADOR") {
    const btnAdmin = document.createElement("a");
    btnAdmin.href = "/web/admin/dashboard.html";
    btnAdmin.className = "btn btn-admin";
    btnAdmin.innerHTML = '<i class="bi bi-speedometer2"></i> Panel Admin';
    container.appendChild(btnAdmin);
  }

  container.appendChild(btnLogout);
}

async function cerrarSesion() {
  try {
    await axios.post(LOGOUT_URL, {}, { withCredentials: true });
    showToast("✅ Sesión cerrada correctamente", "success");
    setTimeout(() => {
        window.location.reload();
    }, 1500); 
  } catch (error) {
    console.error("Error al cerrar sesión:", error);
    showToast("❌ No se pudo cerrar la sesión", "error");
  }
}