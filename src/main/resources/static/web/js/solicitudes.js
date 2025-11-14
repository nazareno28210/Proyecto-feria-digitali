/*
 * ====================================
 * SOLICITUDES.JS (con Toastify)
 * ====================================
 */

// 1. AÑADIDA: Función Toastify
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

const tablaBody = document.getElementById("tabla-body");
const mensaje = document.getElementById("mensaje");

document.addEventListener("DOMContentLoaded", cargarSolicitudes);

// =========================================================
// FUNCIÓN PARA CARGAR TODAS LAS SOLICITUDES PENDIENTES
// =========================================================
async function cargarSolicitudes() {
  try {
    const response = await axios.get("/api/solicitudes/pendientes");
    const solicitudes = response.data;

    tablaBody.innerHTML = "";

    if (solicitudes.length === 0) {
      mensaje.textContent = "No hay solicitudes pendientes.";
      return;
    }

    mensaje.textContent = "";

    solicitudes.forEach(s => {
      const fila = document.createElement("tr");
      // CAMBIO: Se quitaron los emojis de los botones
      fila.innerHTML = `
        <td>${s.id}</td>
        <td>${s.nombreUsuario}</td>
        <td>${s.apellidoUsuario}</td>
        <td>${s.emailUsuario}</td>
        <td>${s.nombreEmprendimiento}</td>
        <td>${s.descripcion || "-"}</td>
        <td>${s.telefono || "-"}</td>
        <td>${s.emailEmprendimiento || "-"}</td>
        <td>
          <button class="btn-aprobar" onclick="aprobarSolicitud(${s.id})">Aprobar</button>
          <button class="btn-rechazar" onclick="rechazarSolicitud(${s.id})">Rechazar</button>
        </td>
      `;
      tablaBody.appendChild(fila);
    });
  } catch (error) {
    console.error("Error cargando solicitudes:", error);
    // CAMBIO: alert a toast
    showToast("Error al conectar con el servidor.", "error");
    mensaje.textContent = "Error al cargar los datos."; // Mantenemos el mensaje en la página
  }
}

// =========================================================
// FUNCIÓN PARA APROBAR SOLICITUD
// =========================================================
async function aprobarSolicitud(id) {
  // Mantenemos el confirm para seguridad
  if (!confirm("¿Seguro deseas aprobar esta solicitud?")) return;

  try {
    const response = await axios.post(`/api/solicitudes/aprobar/${id}`);
    // CAMBIO: alert a toast
    showToast(response.data || "✅ Solicitud aprobada", "success");
    cargarSolicitudes(); // Recarga la lista
  } catch (error) {
    console.error("Error al aprobar:", error);
    // CAMBIO: alert a toast
    showToast(error.response?.data || "❌ No se pudo aprobar la solicitud.", "error");
  }
}

// =========================================================
// FUNCIÓN PARA RECHAZAR SOLICITUD
// =========================================================
async function rechazarSolicitud(id) {
  // Mantenemos el confirm para seguridad
  if (!confirm("¿Seguro deseas rechazar esta solicitud?")) return;

  try {
    const response = await axios.post(`/api/solicitudes/rechazar/${id}`);
    // CAMBIO: alert a toast
    // Usamos 'success' porque la *acción de rechazar* fue exitosa
    showToast(response.data || "🗑️ Solicitud rechazada", "success");
    cargarSolicitudes(); // Recarga la lista
  } catch (error) {
    console.error("Error al rechazar:", error);
    // CAMBIO: alert a toast
    showToast(error.response?.data || "⚠️ No se pudo rechazar la solicitud.", "error");
  }
}