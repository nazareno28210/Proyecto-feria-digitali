/*
 * ====================================
 * SOLICITUDES.JS (Restaurado para Cuentas Nuevas)
 * ====================================
 */

function showToast(message, type = "info") {
  let color;
  switch (type) {
    case "success":
      color = "linear-gradient(to right, #10b981, #059669)"; 
      break;
    case "error":
      color = "linear-gradient(to right, #ef4444, #b91c1c)"; 
      break;
    case "warning":
      color = "linear-gradient(to right, #f59e0b, #d97706)"; 
      break;
    default:
      color = "linear-gradient(to right, #3b82f6, #67e8f9)"; 
  }
  Toastify({
    text: message,
    duration: 3000,
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
// FUNCIÓN PARA CARGAR SOLICITUDES DE NUEVOS FERIANTES
// =========================================================
async function cargarSolicitudes() {
  try {
    // 🟢 Volvemos al endpoint original que lee de la tabla que me mostraste en la imagen
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
      
      fila.innerHTML = `
        <td>${s.id}</td>
        <td>${s.nombreUsuario || "-"}</td>
        <td>${s.apellidoUsuario || "-"}</td>
        <td>${s.emailUsuario || "-"}</td>
        <td><strong>${s.nombreEmprendimiento}</strong></td>
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
    showToast("Error al conectar con el servidor.", "error");
    mensaje.textContent = "Error al cargar los datos."; 
  }
}

// =========================================================
// FUNCIÓN PARA APROBAR SOLICITUD
// =========================================================
async function aprobarSolicitud(id) {
  if (!confirm("¿Seguro deseas aprobar esta solicitud y crearle el perfil de Feriante?")) return;

  try {
    const response = await axios.post(`/api/solicitudes/aprobar/${id}`);
    showToast(response.data?.mensaje || response.data || "✅ Feriante aprobado", "success");
    cargarSolicitudes(); 
  } catch (error) {
    console.error("Error al aprobar:", error);
    showToast(error.response?.data?.error || "❌ No se pudo aprobar la solicitud.", "error");
  }
}

// =========================================================
// FUNCIÓN PARA RECHAZAR SOLICITUD
// =========================================================
async function rechazarSolicitud(id) {
  if (!confirm("¿Seguro deseas rechazar esta solicitud?")) return;
  try {
    const response = await axios.post(`/api/solicitudes/rechazar/${id}`);
    showToast(response.data?.mensaje || response.data || "🗑️ Solicitud rechazada", "success");
    cargarSolicitudes(); 
  } catch (error) {
    console.error("Error al rechazar:", error);
    showToast(error.response?.data?.error || "⚠️ No se pudo rechazar la solicitud.", "error");
  }
}