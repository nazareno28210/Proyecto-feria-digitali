/*
 * ====================================
 * SOLICITUD-FERIANTE.JS
 * ====================================
 */

document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("form-feriante");

  form.addEventListener("submit", async function (e) {
    e.preventDefault();
    
    try {
      const userRes = await axios.get("/api/usuarios/current", { withCredentials: true });
      const usuario = userRes.data;

      if (!usuario || !usuario.id) {
        mostrarNotificacion("No se pudo identificar al usuario. Inicie sesion nuevamente.", "warning");
        return;
      }

      const nombreEmprendimiento = document.getElementById("nombreEmprendimiento").value;
      const descripcion = document.getElementById("descripcion").value;
      const telefono = document.getElementById("telefono").value;
      const emailEmprendimiento = document.getElementById("email").value;

      const telefonoRegex = /^[0-9\s+\-()]*$/;

      if (!telefonoRegex.test(telefono)) {
        mostrarNotificacion("El telefono solo puede contener numeros.", "error");
        return;
      }

      const datosFormulario = {
        nombreEmprendimiento,
        descripcion,
        telefono,
        emailEmprendimiento
      };

      const res = await axios.post(
        `/api/solicitudes/crear/${usuario.id}`,
        datosFormulario,
        { withCredentials: true }
      );

      mostrarNotificacion(res.data || "Solicitud enviada correctamente.", "success");
      form.querySelector('button[type="submit"]').disabled = true;

      setTimeout(() => {
        window.location.href = "/web/ferias.html";
      }, 1500);

    } catch (error) {
      console.error("Error al enviar la solicitud:", error);
      mostrarNotificacion(obtenerMensajeError(error, "Error al enviar la solicitud."), "error");
    }
  });
});