function showToast(message, type = "info") {
  let color;
  // Colores actualizados a la paleta de la app
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
  const form = document.getElementById("registerForm");

  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    // 1. Obtenemos todos los valores del formulario
    const nombre = document.getElementById("nombre").value;
    const apellido = document.getElementById("apellido").value;
    const email = document.getElementById("email").value;
    const contrasena = document.getElementById("password").value;
    const confirmContrasena = document.getElementById("confirmPassword").value;

    // 2. Validación RÁPIDA en el frontend (para mejor UX)
    if (contrasena !== confirmContrasena) {
      showToast("⚠️ Las contraseñas no coinciden", "warning");
      return; // Detiene el envío
    }

    try {
      // 3. Enviamos el objeto completo al backend (coincide con RegistroDTO)
      await axios.post("/api/usuarios", {
        nombre,
        apellido,
        email,
        contrasena,
        confirmContrasena // Se envía también la confirmación
      });

      // 4. Éxito
      showToast("✅ Usuario registrado correctamente", "success");

      setTimeout(() => {
        window.location.href = "/web/login.html";
      }, 1500);

    } catch (error) {
      // 5. Manejo de errores (ahora lee el mensaje del backend)
      if (error.response?.status === 409) {
        // Conflicto (Email ya existe)
        showToast("⚠️ El correo ya está registrado", "warning");
      } else if (error.response?.status === 400) {
        // Bad Request (Contraseñas no coinciden, o no es segura)
        // Usamos el mensaje específico que envía el backend
        showToast("⚠️ " + (error.response.data || "Error en los datos"), "warning");
      } else {
        // Otro error
        showToast("❌ Error al registrar usuario", "error");
      }
    }
  });
});