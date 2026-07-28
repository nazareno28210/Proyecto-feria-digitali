function showToast(message, type = "info") {
  let color;
  switch (type) {
    case "success":
      color = "linear-gradient(to right, #1a3a5a, #3b82f6)"; 
      break;
    case "error":
      color = "linear-gradient(to right, #ef4444, #b91c1c)"; 
      break;
    case "warning":
      color = "linear-gradient(to right, #3b82f6, #67e8f9)";
      break;
    default:
      color = "linear-gradient(to right, #3b82f6, #67e8f9)"; 
  }

  Toastify({
    text: message,
    duration: 4000,
    gravity: "top",
    position: "right",
    backgroundColor: color,
    stopOnFocus: true,
  }).showToast();
}

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
      showToast("⚠️ Las contraseñas no coinciden", "warning");
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

      // Transición hacia la vista de confirmación de correo enviado
      sentEmailDisplay.innerText = email;
      registerView.style.display = "none";
      successView.style.display = "block";

    } catch (error) {
      if (error.response?.status === 409) {
        showToast("⚠️ El correo ya está registrado", "warning");
      } else if (error.response?.status === 400) {
        showToast("⚠️ " + (error.response.data || "Error en los datos"), "warning");
      } else {
        showToast("❌ Error al registrar usuario", "error");
      }
    }
  });
});