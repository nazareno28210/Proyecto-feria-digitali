document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("registerForm");

  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const nombre = document.getElementById("nombre").value;
    const apellido = document.getElementById("apellido").value;
    const email = document.getElementById("email").value;
    const contrasena = document.getElementById("password").value;

    try {
      // Enviar el usuario al backend
      const res = await axios.post("/api/usuarios", {
        nombre,
        apellido,
        email,
        contrasena, // 👈 debe llamarse igual que en la entidad Java
      });

      alert("✅ Usuario registrado correctamente");
      // Redirige al login una vez creado
      window.location.href = "/web/login.html";

    } catch (error) {

      if (error.response && error.response.status === 409) {
        alert("⚠️ El correo ya está registrado");
      }
      else if (error.response && error.response.status === 400) {
              alert("⚠️ La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un símbolo @,&");
            }
      else {
                    alert("❌ Error al registrar usuario. Revisa el servidor.");
                  }
    }
  });
});
