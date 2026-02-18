const API_URL = "http://localhost:8080/api/ferias/activas";
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
            f.nombre.toLowerCase().includes(texto)
        );
        mostrarFerias(filtradas);
        actualizarMarcadoresMapa(filtradas); // El mapa se filtra en tiempo real
    });
});

// ========================= SECCIÓN MAPAS =========================

function inicializarMapa() {
    // Inicializa el mapa centrado en Río Grande [cite: 85]
    mapa = L.map('mapa-ferias').setView(RIO_GRANDE_COORDS, 13);
    
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors'
    }).addTo(mapa);
    
    // Grupo para manejar los pines de forma independiente
    markersGroup = L.layerGroup().addTo(mapa);
}

function actualizarMarcadoresMapa(lista) {
    // Limpia los pines actuales antes de poner los nuevos para evitar duplicados al filtrar
    markersGroup.clearLayers();

    lista.forEach(feria => {
        // Verificamos que la feria tenga coordenadas válidas antes de intentar ubicarla [cite: 89, 90]
        if (feria.latitud && feria.longitud) {
            const marcador = L.marker([feria.latitud, feria.longitud]);
            
            // Configuramos el contenido del Popup con el estilo de tu proyecto [cite: 90, 91, 92]
            marcador.bindPopup(`
                <div style="text-align: center; font-family: sans-serif;">
                    <strong style="color: #1a3a5a; font-size: 1.1rem;">${feria.nombre}</strong><br>
                    <small style="color: #666;">${feria.lugar}</small><br>
                    <button onclick="verDetalles(${feria.id})" 
                            style="margin-top: 10px; background: #1a3a5a; color: white; border: none; padding: 5px 10px; border-radius: 5px; cursor: pointer;">
                        Ver detalles
                    </button>
                </div>
            `);
            
            // EFECTO VISUAL: Al hacer clic, el mapa se desplaza suavemente hacia la feria
            marcador.on('click', function() {
                mapa.flyTo([feria.latitud, feria.longitud], 16, {
                    animate: true,
                    duration: 1.5 // segundos que tarda el desplazamiento
                });
            });
            
            // Añadimos el marcador al grupo para que sea gestionable [cite: 89]
            markersGroup.addLayer(marcador);
        }
    });
}

// ========================= SECCIÓN FERIAS =========================

async function cargarDatosFerias() {
    try {
        const res = await axios.get(API_URL); 
        feriasGlobal = res.data;

        // Renderiza ambos componentes visuales
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
      container.innerHTML = "<p class='no-ferias-msg'>No se encontraron ferias que coincidan con la búsqueda.</p>";
      return;
  }

  lista.forEach((feria) => {
    const card = document.createElement("div");
    card.classList.add("card");
    
    const imagenHtml = feria.imagenUrl
      ? `<div class="card-image-container">
           <img src="${feria.imagenUrl}" alt="Imagen de ${feria.nombre}">
         </div>`
      : '';

    card.innerHTML = `
      ${imagenHtml} 
      <div class="card-content">
        <h2>${feria.nombre}</h2>
        <p><strong>Lugar:</strong> ${feria.lugar}</p>
        <p><strong>Fecha inicio:</strong> ${feria.fechaInicio}</p>
        <p><strong>Fecha fin:</strong> ${feria.fechaFinal ?? "Sin definir"}</p>
        <p>${feria.descripcion ?? ""}</p>
      </div>
      <button onclick="verDetalles(${feria.id})">Ver detalles</button>
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