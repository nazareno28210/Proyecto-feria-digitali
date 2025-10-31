
document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("loginForm");

  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    try {
      //Enviar login con credenciales habilitadas
      await axios.post("/api/login", new URLSearchParams({ email, password }),
        {
          headers: { "Content-Type": "application/x-www-form-urlencoded" },
          withCredentials: true,
        }
      );

      //Obtener usuario actual
      const res = await axios.get("/api/usuarios/current", {
        withCredentials: true, // <-- también aquí
      });
      const usuario = res.data;

      console.log("✅ Usuario logueado:", usuario);

      if (!usuario || !usuario.tipoUsuario) {
        alert("No se pudo obtener el tipo de usuario. Revisa el backend.");
        return;
      }

      // 🔹 3. Redirigir según tipo de usuario
      switch (usuario.tipoUsuario) {
        case "ADMINISTRADOR":
          window.location.href = "/web/admin.html";
          break;
        case "FERIANTE":
          window.location.href = "/web/feriante.html";
          break;
        case "NORMAL":
          window.location.href = "/web/ferias.html";
          break;
        default:
          alert("Tipo de usuario desconocido: " + usuario.tipoUsuario);
      }

    } catch (error) {
      console.error("❌ Error al iniciar sesión:", error);
      if (error.response) {
        console.log("Detalles del error:", error.response.data);
      }
      alert("Credenciales incorrectas o error en el servidor");
    }
  });
});
