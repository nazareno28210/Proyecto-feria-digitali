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
  const form = document.getElementById("loginForm");

  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const email = document.getElementById("email").value;
    const contrasena = document.getElementById("contrasena").value;

    setLoading(true);

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
        showToast("No se pudo obtener el tipo de usuario.", "warning");
        setLoading(false);
        return;
      }

      showToast("Sesion iniciada correctamente", "success");

      setTimeout(() => {
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
      }, 1000);
    } catch (error) {
      setLoading(false);
      if (error.response?.status === 401)
        showToast("Credenciales incorrectas", "error");
      else
        showToast("Error en el servidor", "error");
    }
  });
});
