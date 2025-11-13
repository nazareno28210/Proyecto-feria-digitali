function showToast(message, type = "info") {
  let color;
  switch (type) {
    case "success":
      color = "linear-gradient(to right, #00b09b, #96c93d)";
      break;
    case "error":
      color = "linear-gradient(to right, #ff5f6d, #ffc371)";
      break;
    case "warning":
      color = "linear-gradient(to right, #f7971e, #ffd200)";
      break;
    default:
      color = "linear-gradient(to right, #2193b0, #6dd5ed)";
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

    const nombre = document.getElementById("nombre").value;
    const apellido = document.getElementById("apellido").value;
    const email = document.getElementById("email").value;
    const contrasena = document.getElementById("password").value;

    try {
      await axios.post("/api/usuarios", {
        nombre,
        apellido,
        email,
        contrasena,
      });

      showToast("✅ Usuario registrado correctamente", "success");

      setTimeout(() => {
        window.location.href = "/web/login.html";
      }, 1500);

    } catch (error) {
      if (error.response?.status === 409)
        showToast("⚠️ El correo ya está registrado", "warning");
      else if (error.response?.status === 400)
        showToast("⚠️ La contraseña no cumple los requisitos", "warning");
      else
        showToast("❌ Error al registrar usuario", "error");
    }
  });
});
