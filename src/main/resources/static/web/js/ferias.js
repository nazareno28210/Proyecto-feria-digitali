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
    // No mostramos nada en user-actions si no está logueado
    document.getElementById("user-actions").innerHTML = `
      <a href="/web/login.html" class="btn btn-header">Iniciar sesión</a>
    `;
  }
}

// 🟢 CAMBIO: Esta función ha sido actualizada 🟢
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
    // Siempre muestra "Mi Perfil"
    const btnPerfil = document.createElement("a");
    btnPerfil.href = "/web/usuario-perfil.html"; // Enlace a su perfil de usuario
    btnPerfil.className = "btn btn-header"; // Estilo de botón de header
    btnPerfil.textContent = "Mi Perfil";
    container.appendChild(btnPerfil);
  }

  // 🔹 Si el usuario es FERIANTE
  if (usuario.tipoUsuario === "FERIANTE") {
    // Muestra "Mi Perfil" que enlaza al perfil de feriante
    const btnPerfil = document.createElement("a");
    btnPerfil.href = "/web/feriante/perfil.html"; // Enlace a su dashboard de feriante
    btnPerfil.className = "btn btn-header";
    btnPerfil.textContent = "Mi Perfil";
    container.appendChild(btnPerfil);
  }

  // 🔹 Si el usuario es ADMINISTRADOR
  if (usuario.tipoUsuario === "ADMINISTRADOR") {
    const btnAdmin = document.createElement("a");
    btnAdmin.href = "/web/admin/dashboard.html";
    btnAdmin.className = "btn btn-admin";
    btnAdmin.textContent = "Panel de administrador";
    container.appendChild(btnAdmin);
  }

  // Se añade el botón de cerrar sesión al final
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