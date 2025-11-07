const tablaBody = document.getElementById("tabla-body");
const mensaje = document.getElementById("mensaje");

// 🔹 Cargar solicitudes al iniciar
document.addEventListener("DOMContentLoaded", cargarSolicitudes);

// =========================================
// FUNCIÓN PARA CARGAR LISTA (GET)
// =========================================
async function cargarSolicitudes() {
  try {
    // Axios automáticamente lanza error si el status no es 200 OK
    const response = await axios.get("/api/solicitudes/pendientes");
    const solicitudes = response.data;

    tablaBody.innerHTML = ""; // Limpiar tabla antes de recargar

    if (solicitudes.length === 0) {
        mensaje.textContent = "No hay solicitudes pendientes de revisión.";
        return;
    }

    // Limpiamos mensaje si hubo uno antes
    mensaje.textContent = "";

    // Renderizamos las filas usando los datos "aplanados" del DTO
    solicitudes.forEach(s => {
      const fila = document.createElement("tr");
      fila.innerHTML = `
        <td>${s.id}</td>
        <td>${s.nombreUsuario}</td>
        <td>${s.apellidoUsuario}</td>
        <td>${s.emailUsuario}</td>
        <td>${s.nombreEmprendimiento}</td>
        <td>
            <button class="btn-aprobar" onclick="aprobarSolicitud(${s.id})">
                ✅ Aprobar
            </button>
        </td>
      `;
      tablaBody.appendChild(fila);
    });

  } catch (error) {
    console.error("Error cargando solicitudes:", error);
    mensaje.style.color = "red";
    mensaje.textContent = "Error al conectar con el servidor. Intenta más tarde.";
  }
}

// =========================================
// FUNCIÓN PARA APROBAR (POST)
// =========================================
async function aprobarSolicitud(id) {
  // Confirmación simple antes de enviar
  if (!confirm("¿Estás seguro de que deseas aprobar a este usuario como Feriante?")) {
      return;
  }

  try {
    const response = await axios.post(`/api/solicitudes/aprobar/${id}`);

    // Si llega aquí, es que todo salió bien (status 200)
    alert("¡Éxito! " + response.data);

    // Recargamos la tabla para que desaparezca la solicitud aprobada
    cargarSolicitudes();

  } catch (error) {
    console.error("Error al aprobar:", error);
    // Intentamos mostrar el mensaje exacto que envió el backend si existe
    const errorMsg = error.response && error.response.data ? error.response.data : "No se pudo aprobar la solicitud.";
    alert("❌ Error: " + errorMsg);
  }
}