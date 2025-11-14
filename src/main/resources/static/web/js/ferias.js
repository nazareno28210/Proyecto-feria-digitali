const API_URL = "http://localhost:8080/api/ferias/activas";
const AUTH_URL = "http://localhost:8080/api/usuarios/current";
const LOGOUT_URL = "http://localhost:8080/api/logout";
const SOLICITUD_URL = "http://localhost:8080/api/solicitudes";

let feriasGlobal = [];

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

// 🔹 INIT 
document.addEventListener("DOMContentLoaded", () => {
  cargarFerias();
  verificarSesion();

  const inputBusqueda = document.getElementById("busqueda");
  inputBusqueda.addEventListener("input", () => {
    const texto = inputBusqueda.value.toLowerCase();
    const filtradas = feriasGlobal.filter((f) =>
      f.nombre.toLowerCase().includes(texto)
    );
    mostrarFerias(filtradas);
  });
});

// ========================= FERIAS =========================
async function cargarFerias() {
  try {
    const response = await axios.get(API_URL);
    feriasGlobal = response.data;
    mostrarFerias(feriasGlobal);
  } catch (error) {
    console.error("Error al cargar las ferias:", error);
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
  }
}

async function mostrarOpcionesUsuario(usuario) {
  const container = document.getElementById("user-actions");
  container.innerHTML = "";
  
  const btnLogout = document.createElement("button");
  btnLogout.id = "btn-logout";
  btnLogout.className = "btn btn-logout"; // Clase global
  btnLogout.textContent = "Cerrar sesión";
  btnLogout.addEventListener("click", cerrarSesion);

  // 🔹 Si el usuario es NORMAL 
  if (usuario.tipoUsuario === "NORMAL") {
    try {
      const solicitudRes = await axios.get(`${SOLICITUD_URL}/pendientes`, { withCredentials: true });
      const pendientes = solicitudRes.data;
      const tienePendiente = pendientes.some(s => s.emailUsuario === usuario.email);

      if (tienePendiente) {
        const msgPendiente = document.createElement("p");
        msgPendiente.textContent = "Solicitud pendiente";
        msgPendiente.style.color = "white";
        container.appendChild(msgPendiente);
      } else {
        const btnFeriante = document.createElement("a");
        btnFeriante.href = "solicitud-feriante.html";
        btnFeriante.className = "btn btn-feriante";
        btnFeriante.textContent = "Deseo ser un Feriante";
        container.appendChild(btnFeriante);
      }
    } catch (error) {
      console.error("Error al verificar solicitud:", error);
      showToast("⚠️ Error al verificar solicitud de feriante", "warning");
    }
  }

  // 🔹 Si el usuario es FERIANTE 
  if (usuario.tipoUsuario === "FERIANTE") {
    // (Sin texto de saludo)
  }

  // 🔹 Si el usuario es ADMINISTRADOR
  if (usuario.tipoUsuario === "ADMINISTRADOR") {
    const btnAdmin = document.createElement("a");
    btnAdmin.href = "/web/admin/dashboard.html"; // La URL que pediste
    btnAdmin.className = "btn btn-admin"; // Clase nueva para el CSS
    btnAdmin.textContent = "Panel de administrador";
    
    // Lo añadimos al contenedor
    container.appendChild(btnAdmin);
  }

  // Se añade el botón de cerrar sesión al final, al lado del botón de admin (si existe)
  container.appendChild(btnLogout);
}

// 🔹 Función cerrarSesion (sin cambios)
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