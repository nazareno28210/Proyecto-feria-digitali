// registro.js — usa mostrarNotificacion global (notificaciones.js)

document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("registerForm");
  const registerView = document.getElementById("registerView");
  const successView = document.getElementById("successView");
  const sentEmailDisplay = document.getElementById("sentEmailDisplay");

  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const nombre = document.getElementById("nombre").value;
    const apellido = document.getElementById("apellido").value;
    const email = document.getElementById("email").value;
    const contrasena = document.getElementById("password").value;
    const confirmContrasena = document.getElementById("confirmPassword").value;

    if (contrasena !== confirmContrasena) {
      mostrarNotificacion("Las contrasenas no coinciden.", "warning");
      return;
    }

    try {
      await axios.post("/api/usuarios", {
        nombre,
        apellido,
        email,
        contrasena,
        confirmContrasena
      });

      sentEmailDisplay.innerText = email;
      registerView.style.display = "none";
      successView.style.display = "block";

    } catch (error) {
      if (error.response?.status === 409) {
        mostrarNotificacion(obtenerMensajeError(error, "El correo ya esta registrado."), "warning");
      } else if (error.response?.status === 400) {
        mostrarNotificacion(obtenerMensajeError(error, "Error en los datos."), "warning");
      } else {
        mostrarNotificacion(obtenerMensajeError(error, "Error al registrar usuario."), "error");
      }
    }
  });
});