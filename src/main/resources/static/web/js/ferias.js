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
            // 🟢 Busca tanto por el nombre de la feria como por la edición
            (f.nombre || "").toLowerCase().includes(texto) || 
            (f.nombreEdicion || "").toLowerCase().includes(texto)
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
        // 🟢 Leemos directo desde el DTO plano
        const lat = edicion.latitud;
        const lng = edicion.longitud;
        
        if (lat && lng) {
            const marcador = L.marker([lat, lng]);
            
            const horaApertura = edicion.horaInicio ? edicion.horaInicio.substring(0, 5) : "??:??";
            const horaCierre = edicion.horaFin ? edicion.horaFin.substring(0, 5) : "??:??";
            
            const nombreBase = edicion.feriaNombre || "Feria General";
            const lugarBase = edicion.feriaLugar || "Lugar a definir";

            marcador.bindPopup(`
                <div style="text-align: center; font-family: sans-serif;">
                    <strong style="color: #1a3a5a; font-size: 1.1rem;">${nombreBase}</strong><br>
                    <span style="color: #e67e22; font-size: 0.9rem; font-weight: bold;">${edicion.nombreEdicion}</span><br>
                    <small style="color: #666;">${lugarBase}</small><br>
                    <small style="color: #2c3e50; font-weight: bold;">⏰ ${horaApertura} a ${horaCierre} hs</small><br>
                    <button onclick="verDetalles(${edicion.id})" 
                            style="margin-top: 10px; background: #1a3a5a; color: white; border: none; padding: 5px 10px; border-radius: 5px; cursor: pointer;">
                        Ver detalles
                    </button>
                </div>
            `);
            
            marcador.on('click', function() {
                mapa.flyTo([lat, lng], 16, { animate: true, duration: 1.5 });
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
      container.innerHTML = "<p class='no-ferias-msg'>No se encontraron ferias activas.</p>";
      return;
  }

  lista.forEach((edicion) => {
    const card = document.createElement("div");
    card.classList.add("card");
    
    // 🟢 Leemos directo desde el DTO plano
    const imagenBase = edicion.feriaImagenUrl || "";
    const nombreBase = edicion.feriaNombre || "Feria General";
    const lugarBase = edicion.feriaLugar || "Lugar a definir";
    const descBase = edicion.feriaDescripcion || "";

    const imagenHtml = imagenBase
      ? `<div class="card-image-container">
           <img src="${imagenBase}" alt="Imagen de ${nombreBase}">
         </div>`
      : '';

    const horaApertura = edicion.horaInicio ? edicion.horaInicio.substring(0, 5) : "A definir";
    const horaCierre = edicion.horaFin ? edicion.horaFin.substring(0, 5) : "A definir";

    card.innerHTML = `
      ${imagenHtml} 
      <div class="card-content">
        <h2>${nombreBase} <span style="font-size: 0.8em; color: #666;">(${edicion.nombreEdicion})</span></h2>
        <p><strong>Lugar:</strong> ${lugarBase}</p>
        <p><strong>Fecha inicio:</strong> ${edicion.fechaInicio}</p>
        <p><strong>Fecha fin:</strong> ${edicion.fechaFinal ?? "Sin definir"}</p>
        <p><strong>Horario:</strong> ${horaApertura} a ${horaCierre} hs</p>
        <p>${descBase}</p>
      </div>
      <button onclick="verDetalles(${edicion.id})">Ver detalles</button>
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
      <a href="buscar.html" class="btn btn-header" style="margin-right: 10px;">
        <i class="bi bi-cart-fill"></i> Buscar Productos
      </a>
      <a href="/web/login.html" class="btn btn-header">Iniciar sesión</a>
    `;
  }
}

async function mostrarOpcionesUsuario(usuario) {
  const container = document.getElementById("user-actions");
  container.innerHTML = ""; 

  const btnBuscar = document.createElement("a");
  btnBuscar.href = "buscar.html";
  btnBuscar.className = "btn btn-header"; 
  btnBuscar.style.marginRight = "10px"; 
  btnBuscar.innerHTML = '<i class="bi bi-cart-fill"></i> Buscar Productos';
  container.appendChild(btnBuscar);

  const btnLogout = document.createElement("button");
  btnLogout.id = "btn-logout";
  btnLogout.className = "btn btn-logout";
  btnLogout.textContent = "Cerrar sesión";
  btnLogout.addEventListener("click", cerrarSesion);

  if (usuario.tipoUsuario === "NORMAL") {
    const btnPerfil = document.createElement("a");
    btnPerfil.href = "/web/usuario-perfil.html";
    btnPerfil.className = "btn btn-header";
    btnPerfil.style.marginRight = "10px";
    btnPerfil.textContent = "Mi Perfil";
    container.appendChild(btnPerfil);
  }

  if (usuario.tipoUsuario === "FERIANTE") {
    const btnPerfil = document.createElement("a");
    btnPerfil.href = "/web/feriante/perfil.html";
    btnPerfil.className = "btn btn-header";
    btnPerfil.style.marginRight = "10px";
    btnPerfil.textContent = "Mi Perfil";
    container.appendChild(btnPerfil);
  }

  if (usuario.tipoUsuario === "ADMINISTRADOR") {
    const btnAdmin = document.createElement("a");
    btnAdmin.href = "/web/admin/dashboard.html";
    btnAdmin.className = "btn btn-admin";
    btnAdmin.style.marginRight = "10px";
    btnAdmin.textContent = "Panel de administrador";
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