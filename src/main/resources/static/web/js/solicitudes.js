const tablaBody = document.getElementById("tabla-body");
const mensaje = document.getElementById("mensaje");

// 🔹 Función para cargar las solicitudes pendientes
async function cargarSolicitudes() {
  try {
    const res = await axios.get("/api/solicitudes/pendientes");
    const solicitudes = res.data;

    tablaBody.innerHTML = ""; // limpia la tabla
    if (solicitudes.length === 0) {
      mensaje.textContent = "No hay solicitudes pendientes.";
      return;
    } else {
      mensaje.textContent = "";
    }

    solicitudes.forEach(s => {
      const fila = document.createElement("tr");

      fila.innerHTML = `
        <td>${s.id}</td>
        <td>${s.usuario.nombre}</td>
        <td>${s.usuario.apellido}</td>
        <td>${s.usuario.email}</td>
        <td>${s.fechaSolicitud}</td>
        <td><button onclick="aprobarSolicitud(${s.id})">Aprobar</button></td>
      `;

      tablaBody.appendChild(fila);
    });
  } catch (error) {
    console.error("Error al cargar las solicitudes:", error);
    mensaje.textContent = "Error al cargar las solicitudes.";
  }
}

// 🔹 Función para aprobar una solicitud
async function aprobarSolicitud(id) {
  const confirmar = confirm("¿Deseas aprobar esta solicitud?");
  if (!confirmar) return;

  try {
    // Aquí se usa 'axios.post'
    const res = await axios.post(`/api/solicitudes/aprobar/${id}`);
    alert(res.data || "Solicitud aprobada correctamente ✅");
    cargarSolicitudes(); // recarga tabla
  } catch (error) {
    console.error("Error al aprobar:", error);
    const msg = error.response?.data || "Error al aprobar la solicitud.";
    alert(msg);
  }
}

// 🔹 Al cargar la página
document.addEventListener("DOMContentLoaded", cargarSolicitudes);