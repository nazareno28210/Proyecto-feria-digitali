/*
 * ====================================
 * SOLICITUDES.JS
 * ====================================
 */

const tablaBody = document.getElementById("tabla-body");
const mensaje = document.getElementById("mensaje");

document.addEventListener("DOMContentLoaded", cargarSolicitudes);

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
          <button type="button" class="btn-aprobar" onclick="aprobarSolicitud(event, ${s.id})">Aprobar</button>
          <button type="button" class="btn-rechazar" onclick="rechazarSolicitud(event, ${s.id})">Rechazar</button>
        </td>
      `;
      tablaBody.appendChild(fila);
    });
  } catch (error) {
    console.error("Error cargando solicitudes:", error);
    mostrarNotificacion("Error al conectar con el servidor.", "error");
    mensaje.textContent = "Error al cargar los datos.";
  }
}

async function aprobarSolicitud(event, id) {
  if (event) event.preventDefault();

  const resultado = await Swal.fire({
    title: "Confirmar aprobacion",
    text: "¿Deseas aprobar esta solicitud y crearle el perfil de Feriante?",
    icon: "question",
    showCancelButton: true,
    confirmButtonText: "Si, aprobar",
    cancelButtonText: "Cancelar",
    confirmButtonColor: "#16a34a",
    cancelButtonColor: "#6b7280",
  });
  if (!resultado.isConfirmed) return;

  try {
    const response = await axios.post(`/api/solicitudes/aprobar/${id}`);
    mostrarNotificacion(response.data?.mensaje || response.data || "Feriante aprobado correctamente.", "success");
    
    // Mutación directa del DOM sin recargar la tabla entera
    const fila = event?.target ? event.target.closest("tr") : null;
    if (fila) fila.remove();
    if (tablaBody.children.length === 0) {
      mensaje.textContent = "No hay solicitudes pendientes.";
    }
  } catch (error) {
    console.error("Error al aprobar:", error);
    mostrarNotificacion(obtenerMensajeError(error, "No se pudo aprobar la solicitud."), "error");
  }
}

async function rechazarSolicitud(event, id) {
  if (event) event.preventDefault();

  const resultado = await Swal.fire({
    title: "Confirmar rechazo",
    text: "¿Deseas rechazar esta solicitud?",
    icon: "warning",
    showCancelButton: true,
    confirmButtonText: "Si, rechazar",
    cancelButtonText: "Cancelar",
    confirmButtonColor: "#dc2626",
    cancelButtonColor: "#6b7280",
  });
  if (!resultado.isConfirmed) return;

  try {
    const response = await axios.post(`/api/solicitudes/rechazar/${id}`);
    mostrarNotificacion(response.data?.mensaje || response.data || "Solicitud rechazada.", "info");
    
    // Mutación directa del DOM sin recargar la tabla entera
    const fila = event?.target ? event.target.closest("tr") : null;
    if (fila) fila.remove();
    if (tablaBody.children.length === 0) {
      mensaje.textContent = "No hay solicitudes pendientes.";
    }
  } catch (error) {
    console.error("Error al rechazar:", error);
    mostrarNotificacion(obtenerMensajeError(error, "No se pudo rechazar la solicitud."), "error");
  }
}