document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("loginForm");

  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    try {
      // 🔹 1. Enviar login
      await axios.post("/api/login", 
        new URLSearchParams({ email, password }),
        { headers: { "Content-Type": "application/x-www-form-urlencoded" } }
      );

      // 🔹 2. Obtener usuario actual
      const res = await axios.get("/api/usuarios/current");
      const usuario = res.data;

      console.log("Usuario logueado:", usuario);

      // 🔹 3. Redirigir según tipo de usuario
      switch (usuario.tipoUsuario) {
        case "ADMINISTRADOR":
          window.location.href = "/web/admin.html";
          break;
        case "FERIANTE":
          window.location.href = "/web/feriante.html";
          break;
        case "NORMAL":
          window.location.href = "/web/usuario.html";
          break;
        default:
          alert("Tipo de usuario desconocido");
      }

    } catch (error) {
      console.error("Error al iniciar sesión:", error);
      alert("Credenciales incorrectas o usuario no encontrado");
    }
  });
});
