document.getElementById("form-feriante").addEventListener("submit", async function (e) {
  e.preventDefault();

  const mensaje = document.getElementById("mensaje");
  mensaje.textContent = "";

  try {
    // Obtener usuario logueado
    const userRes = await axios.get("/api/usuarios/current", { withCredentials: true });
    const usuario = userRes.data;

    // Validar que el usuario esté logueado
    if (!usuario || !usuario.id) {
      mensaje.style.color = "red";
      mensaje.textContent = "❌ Debe iniciar sesión antes de enviar la solicitud.";
      return;
    }

    // Enviar solicitud al backend
    const res = await axios.post(`/api/solicitudes/crear/${usuario.id}`);

    // Mostrar mensaje de éxito
    mensaje.style.color = "green";

    // Si el backend devuelve un texto
    if (typeof res.data === "string") {
      mensaje.textContent = "✅ " + res.data;
    }
    // Si devuelve un objeto JSON
    else if (typeof res.data === "object" && res.data !== null) {
      mensaje.textContent = "✅ Solicitud enviada correctamente. Será revisada por un administrador.";
    }
    // Caso por defecto
    else {
      mensaje.textContent = "✅ Solicitud enviada correctamente.";
    }

    // Limpiar formulario
    document.getElementById("form-feriante").reset();

  } catch (error) {
    console.error("Error al enviar la solicitud:", error);
    mensaje.style.color = "red";

    if (error.response && typeof error.response.data === "string") {
      mensaje.textContent = "❌ " + error.response.data;
    } else {
      mensaje.textContent = "❌ Error al enviar la solicitud.";
    }
  }
});
