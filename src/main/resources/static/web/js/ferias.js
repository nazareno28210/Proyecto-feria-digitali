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
// 🆕 NUEVAS FUNCIONES DE SESIÓN (Usando AXIOS)
// ======================================================

async function verificarSesion() {
  try {
    // Intentamos obtener el usuario actual.
    // 'withCredentials: true' es VITAL para enviar la cookie de sesión al backend.
    const response = await axios.get(AUTH_URL, { withCredentials: true });

    // Si el backend responde con éxito (status 200) y hay datos de usuario:
    if (response.status === 200 && response.data) {
      console.log("Usuario autenticado:", response.data.email);
      mostrarBotonLogout(response.data.nombre || "Usuario"); // Opcional: pasar el nombre para mostrarlo
    }
  } catch (error) {
    // Si da error 401 o 403, significa que no está logueado.
    // No hacemos nada, dejamos el botón de "Iniciar sesión" por defecto.
    console.log("Usuario no autenticado (modo visitante)");
  }
}

function mostrarBotonLogout(nombreUsuario) {
  const container = document.getElementById("user-actions");

  // Usamos la nueva clase 'btn-logout' y quitamos el 'style' inline
  container.innerHTML = `
    <button id="btn-logout" class="btn-logout">
      Cerrar sesión
    </button>
  `;

  document.getElementById("btn-logout").addEventListener("click", cerrarSesion);
}

async function cerrarSesion() {
  try {
    // Petición POST para cerrar sesión.
    // IMPORTANTE: Asegúrate de que tu backend espera el logout en /api/logout
    await axios.post(LOGOUT_URL, {}, { withCredentials: true });

    // Si el logout es exitoso, recargamos la página para volver al estado inicial
    window.location.reload();
  } catch (error) {
    console.error("Error al cerrar sesión:", error);
    alert("No se pudo cerrar la sesión correctamente.");
  }
}