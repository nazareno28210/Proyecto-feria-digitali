function showToast(message, type = "info") {
  let color;
   switch (type) {
    case "success":
      // Gradiente del azul oscuro al medio
      color = "linear-gradient(to right, #1a3a5a, #3b82f6)"; 
      break;
    case "error":
      // Gradiente de rojos
      color = "linear-gradient(to right, #ef4444, #b91c1c)"; 
      break;
    case "warning":
      // Gradiente de naranjas/ámbar
      color = "linear-gradient(to right, #3b82f6, #67e8f9)";
      break;
    default:
      // Gradiente del azul medio al cian
      color = "linear-gradient(to right, #3b82f6, #67e8f9)"; 
  }

  Toastify({
    text: message,
    duration: 4000,
    gravity: "top", // top or bottom
    position: "right", // left, center or right
    backgroundColor: color,
    stopOnFocus: true,
  }).showToast();
}

document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("loginForm");

  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const email = document.getElementById("email").value;
    const contrasena = document.getElementById("contrasena").value;

    try {
      // Enviar login
      await axios.post(
        "/api/login",
        new URLSearchParams({ email, password: contrasena }),
        {
          headers: { "Content-Type": "application/x-www-form-urlencoded" },
          withCredentials: true,
        }
      );

      // Obtener usuario actual
      const res = await axios.get("/api/usuarios/current", {
        withCredentials: true,
      });
      const usuario = res.data;

      if (!usuario || !usuario.tipoUsuario) {
        showToast("⚠️ No se pudo obtener el tipo de usuario.", "warning");
        return;
      }

      showToast("✅ Sesión iniciada correctamente", "success");

      // Redirigir según tipo de usuario
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
          showToast("Tipo de usuario desconocido: " + usuario.tipoUsuario, "warning");
      }
    } catch (error) {
      if (error.response?.status === 401)
        showToast("❌ Credenciales incorrectas", "error");
      else
        showToast("❌ Error en el servidor", "error");
    }
  });
});
