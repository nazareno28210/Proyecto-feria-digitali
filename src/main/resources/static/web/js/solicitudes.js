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
          <button class="btn-aprobar" onclick="aprobarSolicitud(${s.id})">✅ Aprobar</button>
          <button class="btn-rechazar" onclick="rechazarSolicitud(${s.id})">❌ Rechazar</button>
        </td>
      `;
      tablaBody.appendChild(fila);
    });

  } catch (error) {
    console.error("Error cargando solicitudes:", error);
    mensaje.style.color = "red";
    mensaje.textContent = "Error al conectar con el servidor.";
  }
}

// =========================================================
// FUNCIÓN PARA APROBAR SOLICITUD
// =========================================================
async function aprobarSolicitud(id) {
  if (!confirm("¿Seguro deseas aprobar esta solicitud?")) return;

  try {
    const response = await axios.post(`/api/solicitudes/aprobar/${id}`);
    alert("✅ " + response.data);
    cargarSolicitudes();
  } catch (error) {
    console.error("Error al aprobar:", error);
    alert("❌ " + (error.response?.data || "No se pudo aprobar la solicitud."));
  }
}

// =========================================================
// FUNCIÓN PARA RECHAZAR SOLICITUD
// =========================================================
async function rechazarSolicitud(id) {
  if (!confirm("¿Seguro deseas rechazar esta solicitud?")) return;

  try {
    const response = await axios.post(`/api/solicitudes/rechazar/${id}`);
    alert("❌ " + response.data);
    cargarSolicitudes();
  } catch (error) {
    console.error("Error al rechazar:", error);
    alert("⚠️ " + (error.response?.data || "No se pudo rechazar la solicitud."));
  }
}
