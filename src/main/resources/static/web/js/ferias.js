const API_URL = "http://localhost:8080/api/ferias";
const AUTH_URL = "http://localhost:8080/api/usuarios/current";
const LOGOUT_URL = "http://localhost:8080/api/logout";
const SOLICITUD_URL = "http://localhost:8080/api/solicitudes";

let feriasGlobal = [];

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
  }
}

function mostrarFerias(lista) {
  const container = document.getElementById("ferias-container");
  container.innerHTML = "";

  lista.forEach((feria) => {
    const card = document.createElement("div");
    card.classList.add("card");
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

// ========================= SESIÓN Y ROLES =========================

async function verificarSesion() {
  try {
    const response = await axios.get(AUTH_URL, { withCredentials: true });

    if (response.status === 200 && response.data) {
      console.log("Usuario autenticado:", response.data);
      mostrarOpcionesUsuario(response.data);
    }
  } catch (error) {
    console.log("Usuario no autenticado (modo visitante)");
  }
}

async function mostrarOpcionesUsuario(usuario) {
  const container = document.getElementById("user-actions");
  container.innerHTML = "";

  // Botón de logout
  const btnLogout = document.createElement("button");
  btnLogout.id = "btn-logout";
  btnLogout.className = "btn-logout";
  btnLogout.textContent = "Cerrar sesión";
  btnLogout.addEventListener("click", cerrarSesion);

  // 🔹 Si el usuario es NORMAL, verificamos si tiene solicitud pendiente
  if (usuario.tipoUsuario === "NORMAL") {
    try {
      const solicitudRes = await axios.get(`${SOLICITUD_URL}/pendientes`, { withCredentials: true });
      const pendientes = solicitudRes.data;
      const tienePendiente = pendientes.some(s => s.usuario.id === usuario.id);

      if (tienePendiente) {
        const msgPendiente = document.createElement("p");
        msgPendiente.textContent = "Solicitud pendiente de aprobación";
        msgPendiente.style.color = "white";
        container.appendChild(msgPendiente);
      } else {
        const btnFeriante = document.createElement("a");
        btnFeriante.href = "solicitud-feriante.html";
        btnFeriante.className = "btn-feriante";
        btnFeriante.textContent = "Deseo ser un Feriante";
        container.appendChild(btnFeriante);
      }
    } catch (error) {
      console.error("Error al verificar solicitud:", error);
    }
  }

  // 🔹 Si el usuario es FERIANTE
  if (usuario.tipoUsuario === "FERIANTE") {
    const msg = document.createElement("p");
    msg.textContent = "Eres feriante ";
    msg.style.color = "white";
    container.appendChild(msg);
  }

  // 🔹 Si el usuario es ADMINISTRADOR
  if (usuario.tipoUsuario === "ADMINISTRADOR") {
    const msg = document.createElement("p");
    msg.textContent = "Eres administrador ";
    msg.style.color = "white";
    container.appendChild(msg);
  }

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
