document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("form-feriante");
  const mensaje = document.getElementById("mensaje");

  form.addEventListener("submit", async function (e) {
    e.preventDefault();
    mensaje.textContent = "";

    try {
      // 1️⃣ Obtener usuario logueado
      const userRes = await axios.get("/api/usuarios/current", { withCredentials: true });
      const usuario = userRes.data;

      if (!usuario || !usuario.id) {
        mensaje.style.color = "red";
        mensaje.textContent = "⚠️ No se pudo identificar al usuario. Inicie sesión nuevamente.";
        return;
      }

      // 2️⃣ Capturar datos del formulario
      const datosFormulario = {
        nombreEmprendimiento: document.getElementById("nombreEmprendimiento").value,
        descripcion: document.getElementById("descripcion").value,
        telefono: document.getElementById("telefono").value,
        emailEmprendimiento: document.getElementById("email").value
      };

      // 3️⃣ Enviar solicitud
      const res = await axios.post(
        `/api/solicitudes/crear/${usuario.id}`,
        datosFormulario,
        { withCredentials: true }
      );

      mensaje.style.color = "green";
      mensaje.textContent = res.data || "✅ Solicitud enviada correctamente.";
      form.reset();

    } catch (error) {
      console.error("Error al enviar la solicitud:", error);
      mensaje.style.color = "red";

      if (error.response) {
        mensaje.textContent = "❌ " + (error.response.data || "Error en el envío de la solicitud.");
      } else {
        mensaje.textContent = "Error al conectar con el servidor. Intenta más tarde.";
      }
    }
  });
});
