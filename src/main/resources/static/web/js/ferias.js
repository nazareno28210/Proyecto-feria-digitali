const API_URL = "http://localhost:8080/api/ferias";
const AUTH_URL = "http://localhost:8080/api/usuarios/current"; // Endpoint para verificar sesión
const LOGOUT_URL = "http://localhost:8080/api/logout"; // Endpoint para cerrar sesión

let feriasGlobal = [];

// 🔹 INIT: Se ejecuta al cargar el DOM
document.addEventListener("DOMContentLoaded", () => {
  cargarFerias();
  verificarSesion(); // 👈 NUEVO: Verificamos si ya está logueado

  // Listener para el buscador
  const inputBusqueda = document.getElementById("busqueda");
  inputBusqueda.addEventListener("input", () => {
    const texto = inputBusqueda.value.toLowerCase();
    const filtradas = feriasGlobal.filter((f) =>
      f.nombre.toLowerCase().includes(texto)
    );
    mostrarFerias(filtradas);
  });
});

// --- FUNCIONES DE FERIAS (Sin cambios) ---
async function cargarFerias() {
  try {
    const response = await axios.get(API_URL);
    feriasGlobal = response.data;
    mostrarFerias(feriasGlobal);
  } catch (error) {
    console.error("Error al cargar las ferias:", error);
  }
}

function mostrarFerias(lista) {
  const container = document.getElementById("ferias-container");
  container.innerHTML = "";

  lista.forEach((feria) => {
    const card = document.createElement("div");
    card.classList.add("card");
    // ... (resto del renderizado de la card igual que antes)
    card.innerHTML = `
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

// ======================================================
// 🆕 GESTIÓN DE SESIÓN Y ROLES (Actualizado)
// ======================================================

async function verificarSesion() {
  try {
    const response = await axios.get(AUTH_URL, { withCredentials: true });

    if (response.status === 200 && response.data) {
      console.log("Usuario autenticado:", response.data);
      // Pasamos los datos completos del usuario para ver su rol
      mostrarOpcionesUsuario(response.data);
    }
  } catch (error) {
    console.log("Usuario no autenticado (modo visitante)");
    // No hace falta hacer nada, el botón de "Iniciar sesión" ya está por defecto en el HTML
  }
}

function mostrarOpcionesUsuario(usuario) {
  const container = document.getElementById("user-actions");
  container.innerHTML = ""; // Limpiamos el botón de "Iniciar sesión"

  // 1. DETECCIÓN DE ROL: Si es 'USUARIO', mostramos el botón de feriante.
  // ⚠️ IMPORTANTE: Verifica si tu backend envía el campo como 'rol', 'role' o 'tipo'.
  // Ajusta 'usuario.rol' según corresponda a tu JSON.
  if (usuario.rol === "USUARIO") {
      const btnFeriante = document.createElement("a");
      btnFeriante.href = "solicitud_feriante.html"; // Asegúrate de que este nombre coincida con tu archivo HTML real
      btnFeriante.className = "btn-feriante";
      btnFeriante.textContent = "Quiero ser feriante";
      container.appendChild(btnFeriante);
  }

  // 2. Botón de Cerrar Sesión (siempre visible si está logueado)
  const btnLogout = document.createElement("button");
  btnLogout.id = "btn-logout";
  btnLogout.className = "btn-logout";
  btnLogout.textContent = "Cerrar sesión";
  btnLogout.addEventListener("click", cerrarSesion);

  container.appendChild(btnLogout);
}

async function cerrarSesion() {
  try {
    await axios.post(LOGOUT_URL, {}, { withCredentials: true });
    window.location.reload();
  } catch (error) {
    console.error("Error al cerrar sesión:", error);
    alert("No se pudo cerrar la sesión correctamente.");
  }
}