const API_URL = "http://localhost:8080/api/ediciones/activas";
const AUTH_URL = "http://localhost:8080/api/usuarios/current";
const LOGOUT_URL = "http://localhost:8080/api/logout";
const SOLICITUD_URL = "http://localhost:8080/api/solicitudes";

let feriasGlobal = [];
let filtroActivo = "TODAS"; // Estado del filtro actual

// Variables globales para el Mapa
const RIO_GRANDE_COORDS = [-53.7860, -67.7070];
let mapa;
let markersGroup;

// =================== INIT ===================

document.addEventListener("DOMContentLoaded", () => {
    inicializarMapa();
    cargarDatosFerias();
    verificarSesion();

    const inputBusqueda = document.getElementById("busqueda");
    inputBusqueda.addEventListener("input", () => {
        aplicarFiltros();
    });

    // Botones de filtro
    document.getElementById("filtro-todas").addEventListener("click", () => setFiltro("TODAS"));
    document.getElementById("filtro-activa").addEventListener("click", () => setFiltro("ACTIVA"));
    document.getElementById("filtro-proxima").addEventListener("click", () => setFiltro("PROXIMA"));
});

// ========================= FILTROS =========================

function setFiltro(estado) {
    filtroActivo = estado;

    document.querySelectorAll(".filtro-btn").forEach(btn => btn.classList.remove("active"));
    document.getElementById(`filtro-${estado.toLowerCase()}`).classList.add("active");

    aplicarFiltros();
}

function aplicarFiltros() {
    const texto = document.getElementById("busqueda").value.toLowerCase();

    let resultado = feriasGlobal.filter(f => {
        const coincideTexto =
            (f.feriaNombre || "").toLowerCase().includes(texto) ||
            (f.nombreEdicion || "").toLowerCase().includes(texto) ||
            (f.feriaLugar || "").toLowerCase().includes(texto);

        if (!coincideTexto) return false;

        if (filtroActivo === "TODAS") return true;

        const estadoStr = (f.estado || "ACTIVA").toUpperCase();
        if (filtroActivo === "ACTIVA") return estadoStr === "ACTIVA";
        if (filtroActivo === "PROXIMA") return estadoStr.includes("PROXIM");

        return true;
    });

    mostrarFerias(resultado);
    actualizarMarcadoresMapa(resultado);
}

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
                    <small style="color: #64748b; display: block; margin-bottom: 6px;">${lugarBase}</small>
                    <small style="color: #1e293b; font-weight: 600; display: block; margin-bottom: 8px;">${horaApertura} a ${horaCierre} hs</small>
                    <button type="button" onclick="verDetalles(event, ${edicion.id})" 
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
        mostrarNotificacion("Error al cargar las ferias.", "error");
    }
}

function mostrarFerias(lista) {
  const container = document.getElementById("ferias-container");
  container.innerHTML = "";
  
  if (lista.length === 0) {
      container.innerHTML = `<div class='no-ferias-msg'><i class="bi bi-search" style="font-size: 2rem; display: block; margin-bottom: 10px;"></i>No se encontraron ferias que coincidan con la busqueda.</div>`;
      return;
  }

  lista.forEach((edicion) => {
    const card = document.createElement("div");
    card.classList.add("card-feria");
    
    const imagenBase = edicion.feriaImagenUrl || "";
    const nombreBase = edicion.feriaNombre || "Feria General";
    const lugarBase = edicion.feriaLugar || "Lugar a definir";
    const descBase = edicion.feriaDescripcion || "Sin descripcion disponible.";
    const estadoStr = (edicion.estado || "ACTIVA").toUpperCase();

    // Badge de estado
    let badgeClass = "badge-activa";
    let badgeLabel = "En transcurso";
    if (estadoStr.includes("PROXIM")) {
        badgeClass = "badge-proxima";
        badgeLabel = "Proxima";
    } else if (estadoStr.includes("FINALIZADA") || estadoStr.includes("INACTIVA")) {
        badgeClass = "badge-finalizada";
        badgeLabel = "Finalizada";
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
        <span class="card-status-badge ${badgeClass}"><i class="bi bi-record-fill"></i> ${badgeLabel}</span>
      </div>
      <div class="card-content-body">
        <h3 class="card-feria-title">${nombreBase}</h3>
        <span class="card-edition-title"><i class="bi bi-stars"></i> ${edicion.nombreEdicion}</span>
        
        <div class="card-meta-list">
          <div class="card-meta-item">
            <i class="bi bi-geo-alt-fill text-danger"></i>
            <span><strong>Ubicacion:</strong> ${lugarBase}</span>
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
        <button type="button" class="btn-card-action" onclick="verDetalles(event, ${edicion.id})">
          <i class="bi bi-arrow-right-circle-fill"></i> Ver Feria y Stands
        </button>
      </div>
    `;
    container.appendChild(card);
  });
}

function verDetalles(event, idParam) {
  if (event && event.preventDefault) event.preventDefault();
  const id = typeof event === 'number' ? event : idParam;
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
      <a href="/web/login.html" class="btn btn-header"><i class="bi bi-box-arrow-in-right"></i> Iniciar sesion</a>
    `;
  }
}

async function mostrarOpcionesUsuario(usuario) {
  const container = document.getElementById("user-actions");
  container.innerHTML = "";

  // Botón Buscar Productos
  const btnBuscar = document.createElement("a");
  btnBuscar.href = "buscar.html";
  btnBuscar.className = "btn btn-header";
  btnBuscar.innerHTML = '<i class="bi bi-cart-fill"></i> Buscar Productos';
  container.appendChild(btnBuscar);

  // Avatar e imagen precargada
  const imagenUrl = (usuario.imagenUrl && usuario.imagenUrl.trim() !== "") 
    ? usuario.imagenUrl 
    : "/web/assets/logo.png";
    
  const nombreMostrar = usuario.nombre 
    ? (usuario.nombre + " " + (usuario.apellido || ""))
    : usuario.email;

  let roleLabel = '<i class="bi bi-person-fill"></i> Usuario General';
  let profileLink = "/web/usuario-perfil.html";

  if (usuario.tipoUsuario === "FERIANTE") {
    roleLabel = '<i class="bi bi-shop"></i> Feriante';
    profileLink = "/web/feriante/perfil.html";
  } else if (usuario.tipoUsuario === "ADMINISTRADOR") {
    roleLabel = '<i class="bi bi-shield-lock-fill text-primary"></i> Administrador';
    profileLink = "/web/usuario-perfil.html";
  }

  // Opción Panel Admin si es Administrador
  const adminOptionHtml = (usuario.tipoUsuario === "ADMINISTRADOR")
    ? `<a href="/web/admin/dashboard.html" class="dropdown-item">
         <i class="bi bi-speedometer2 text-primary"></i> Panel de Administración
       </a>`
    : '';

  // Menú Desplegable Completo
  const dropdownContainer = document.createElement("div");
  dropdownContainer.className = "profile-dropdown-container";
  dropdownContainer.innerHTML = `
    <button id="btn-profile-menu-ferias" class="profile-avatar-btn" aria-haspopup="true" aria-expanded="false" title="Opciones de perfil">
      <img src="${escapeHtml(imagenUrl)}" alt="Foto de perfil" class="avatar-img" onerror="this.src='/web/assets/logo.png';" />
      <i class="bi bi-chevron-down dropdown-arrow"></i>
    </button>

    <div id="profile-dropdown-ferias" class="profile-dropdown-menu hidden">
      <div class="dropdown-header">
        <span class="user-name">${escapeHtml(nombreMostrar)}</span>
        <span class="user-role">${roleLabel}</span>
      </div>
      <hr class="dropdown-divider">
      
      <a href="${profileLink}" class="dropdown-item">
        <i class="bi bi-person-gear"></i> Mi Perfil
      </a>

      ${adminOptionHtml}

      <hr class="dropdown-divider">
      
      <button id="btn-dropdown-logout" class="dropdown-item logout-item">
        <i class="bi bi-box-arrow-right"></i> Cerrar sesión
      </button>
    </div>
  `;

  container.appendChild(dropdownContainer);

  // Event listeners para abrir/cerrar el desplegable
  const profileBtn = document.getElementById("btn-profile-menu-ferias");
  const profileDropdown = document.getElementById("profile-dropdown-ferias");

  if (profileBtn && profileDropdown) {
    profileBtn.addEventListener("click", (e) => {
      e.stopPropagation();
      const isHidden = profileDropdown.classList.contains("hidden");
      if (isHidden) {
        profileDropdown.classList.remove("hidden");
        profileBtn.classList.add("active");
        profileBtn.setAttribute("aria-expanded", "true");
      } else {
        profileDropdown.classList.add("hidden");
        profileBtn.classList.remove("active");
        profileBtn.setAttribute("aria-expanded", "false");
      }
    });

    document.addEventListener("click", (e) => {
      if (!profileDropdown.contains(e.target) && !profileBtn.contains(e.target)) {
        profileDropdown.classList.add("hidden");
        profileBtn.classList.remove("active");
        profileBtn.setAttribute("aria-expanded", "false");
      }
    });
  }

  const btnLogoutDrop = document.getElementById("btn-dropdown-logout");
  if (btnLogoutDrop) {
    btnLogoutDrop.addEventListener("click", cerrarSesion);
  }
}

async function cerrarSesion() {
  try {
    await axios.post(LOGOUT_URL, {}, { withCredentials: true });
    mostrarNotificacion("Sesión cerrada correctamente.", "success");
    verificarSesion();
  } catch (error) {
    console.error("Error al cerrar sesión:", error);
    mostrarNotificacion("No se pudo cerrar la sesión.", "error");
  }
}

function escapeHtml(text) {
    if (!text) return "";
    return String(text)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}