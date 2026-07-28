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
    // 🔑 MODO 2: Hay un token en la URL -> Mostrar formulario de cambio de contraseña
    requestView.style.display = "none";
    resetView.style.display = "block";

    const resetForm = document.getElementById("resetPasswordForm");
    resetForm.addEventListener("submit", async (e) => {
      e.preventDefault();

      const password = document.getElementById("password").value;
      const confirmPassword = document.getElementById("confirmPassword").value;

      if (password !== confirmPassword) {
        showToast("⚠️ Las contraseñas no coinciden", "warning");
        return;
      }

      if (!esContrasenaSegura(password)) {
        showToast("⚠️ La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un símbolo.", "warning");
        return;
      }

      try {
        const response = await axios.post("/auth/reset-password", {
          token: token,
          nuevaPassword: password
        });

        showToast("✅ " + (response.data || "Contraseña actualizada correctamente"), "success");

        setTimeout(() => {
          window.location.href = "/web/login.html";
        }, 2000);

      } catch (error) {
        if (error.response && error.response.data) {
          showToast("❌ " + error.response.data, "error");
        } else {
          showToast("❌ Error al restablecer la contraseña", "error");
        }
      }
    });

  } else {
    // 📧 MODO 1: No hay token -> Mostrar formulario para solicitar correo de recuperación
    requestView.style.display = "block";
    resetView.style.display = "none";

    const requestForm = document.getElementById("requestResetForm");
    requestForm.addEventListener("submit", async (e) => {
      e.preventDefault();

      const email = document.getElementById("email").value;

      try {
        const response = await axios.post(`/auth/forgot-password?email=${encodeURIComponent(email)}`);

        showToast("✅ " + (response.data || "Si el correo existe, se enviará un enlace de recuperación."), "success");

        document.getElementById("email").value = "";

      } catch (error) {
        showToast("❌ Error al procesar la solicitud", "error");
      }
    });
  }
});
