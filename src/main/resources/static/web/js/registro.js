function showToast(message, type = "info") {
  let color;
  switch (type) {
    case "success":
      color = "linear-gradient(to right, #166534, #22c55e)"; 
      break;
    case "error":
      color = "linear-gradient(to right, #ef4444, #b91c1c)"; 
      break;
    case "warning":
      color = "linear-gradient(to right, #f59e0b, #d97706)";
      break;
    default:
      color = "linear-gradient(to right, #22c55e, #86efac)"; 
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

function setLoading(isLoading) {
  const btn = document.getElementById("submitBtn");
  if (isLoading) {
    btn.classList.add("loading");
    btn.disabled = true;
  } else {
    btn.classList.remove("loading");
    btn.disabled = false;
  }
}

document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("registerForm");

  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const nombre = document.getElementById("nombre").value;
    const apellido = document.getElementById("apellido").value;
    const email = document.getElementById("email").value;
    const contrasena = document.getElementById("password").value;
    const confirmContrasena = document.getElementById("confirmPassword").value;
    const terms = document.getElementById("terms").checked;

    if (!terms) {
      showToast("Debes aceptar los terminos de servicio", "warning");
      return;
    }

    if (contrasena !== confirmContrasena) {
      showToast("Las contrasenas no coinciden", "warning");
      return;
    }

    if (contrasena.length < 8) {
      showToast("La contrasena debe tener al menos 8 caracteres", "warning");
      return;
    }

    setLoading(true);

    try {
      await axios.post("/api/usuarios", {
        nombre,
        apellido,
        email,
        contrasena,
        confirmContrasena
      });

      showToast("Cuenta creada correctamente", "success");

      setTimeout(() => {
        window.location.href = "/web/login.html";
      }, 1500);

    } catch (error) {
      setLoading(false);
      if (error.response?.status === 409) {
        showToast("El correo ya esta registrado", "warning");
      } else if (error.response?.status === 400) {
        showToast(error.response.data || "Error en los datos", "warning");
      } else {
        showToast("Error al registrar usuario", "error");
      }
    }
  });
});
