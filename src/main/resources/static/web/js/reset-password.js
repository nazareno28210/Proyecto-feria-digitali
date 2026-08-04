// reset-password.js — usa mostrarNotificacion global (notificaciones.js)

function esContrasenaSegura(contrasena) {
  const patron = /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!¿?.,;:_-]).{8,}$/;
  return patron.test(contrasena);
}

document.addEventListener("DOMContentLoaded", () => {
  const urlParams = new URLSearchParams(window.location.search);
  const token = urlParams.get("token");

  const requestView = document.getElementById("requestView");
  const resetView = document.getElementById("resetView");

  if (token) {
    requestView.style.display = "none";
    resetView.style.display = "block";

    const resetForm = document.getElementById("resetPasswordForm");
    resetForm.addEventListener("submit", async (e) => {
      e.preventDefault();

      const password = document.getElementById("password").value;
      const confirmPassword = document.getElementById("confirmPassword").value;

      if (password !== confirmPassword) {
        mostrarNotificacion("Las contrasenas no coinciden.", "warning");
        return;
      }

      if (!esContrasenaSegura(password)) {
        mostrarNotificacion("La contrasena debe tener al menos 8 caracteres, una mayuscula, una minuscula, un numero y un simbolo.", "warning");
        return;
      }

      try {
        const response = await axios.post("/auth/reset-password", {
          token: token,
          nuevaPassword: password
        });

        mostrarNotificacion(response.data || "Contrasena actualizada correctamente.", "success");

        setTimeout(() => {
          window.location.href = "/web/login.html";
        }, 2000);

      } catch (error) {
        mostrarNotificacion(obtenerMensajeError(error, "Error al restablecer la contrasena."), "error");
      }
    });

  } else {
    requestView.style.display = "block";
    resetView.style.display = "none";

    const requestForm = document.getElementById("requestResetForm");
    requestForm.addEventListener("submit", async (e) => {
      e.preventDefault();

      const email = document.getElementById("email").value;

      try {
        const response = await axios.post(`/auth/forgot-password?email=${encodeURIComponent(email)}`);
        mostrarNotificacion(response.data || "Si el correo existe, se enviara un enlace de recuperacion.", "success");
        document.getElementById("email").value = "";

      } catch (error) {
        mostrarNotificacion(obtenerMensajeError(error, "Error al procesar la solicitud."), "error");
      }
    });
  }
});
