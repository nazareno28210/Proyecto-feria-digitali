// login.js — usa mostrarNotificacion global (notificaciones.js)

document.addEventListener("DOMContentLoaded", () => {
  const urlParams = new URLSearchParams(window.location.search);
  if (urlParams.get("verificado") === "true") {
    mostrarNotificacion("Tu cuenta ha sido activada con exito. Ya puedes iniciar sesion.", "success");
  } else if (urlParams.get("errorVerificacion") === "true") {
    mostrarNotificacion("El enlace de verificacion es invalido o ya ha expirado.", "error");
  }

  const form = document.getElementById("loginForm");

  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const email = document.getElementById("email").value;
    const contrasena = document.getElementById("contrasena").value;

    try {
      await axios.post(
        "/api/login",
        new URLSearchParams({ email, password: contrasena }),
        {
          headers: { "Content-Type": "application/x-www-form-urlencoded" },
          withCredentials: true,
        }
      );

      const res = await axios.get("/api/usuarios/current", {
        withCredentials: true,
      });
      const usuario = res.data;

      if (!usuario || !usuario.tipoUsuario) {
        mostrarNotificacion("No se pudo obtener el tipo de usuario.", "warning");
        return;
      }

      mostrarNotificacion("Sesion iniciada correctamente.", "success");

      switch (usuario.tipoUsuario) {
        case "ADMINISTRADOR":
          window.location.href = "/web/admin/dashboard.html";
          break;
        case "FERIANTE":
          window.location.href = "/web/feriante/perfil.html";
          break;
        case "NORMAL":
          window.location.href = "/web/ferias.html";
          break;
        default:
          mostrarNotificacion("Tipo de usuario desconocido: " + usuario.tipoUsuario, "warning");
      }
    } catch (error) {
      if (error.response?.status === 403) {
        const msg = obtenerMensajeError(error, "Debes ingresar a tu correo electronico y verificar tu cuenta para activarla.");
        mostrarNotificacion(msg, "warning");
      } else if (error.response?.status === 401) {
        const msg = obtenerMensajeError(error, "Correo o contrasena incorrectos.");
        mostrarNotificacion(msg, "error");
      } else {
        mostrarNotificacion(obtenerMensajeError(error, "Error en el servidor."), "error");
      }
    }
  });
});
