document.getElementById("form-feriante").addEventListener("submit", async function(e) {
  e.preventDefault();

  const mensaje = document.getElementById("mensaje");
  mensaje.textContent = "";

  try {
    // Obtener usuario logueado
    const userRes = await axios.get("/api/usuarios/current");
    const usuario = userRes.data;

    // Enviar solicitud con ID del usuario
    const res = await axios.post(`/api/solicitudes/crear/${usuario.id}`);

    mensaje.style.color = "green";
    mensaje.textContent = "Solicitud enviada correctamente. Será revisada por un administrador.";

  } catch (error) {
    mensaje.style.color = "red";
    if (error.response && error.response.data) {
      mensaje.textContent = " " + error.response.data;
    } else {
      mensaje.textContent = "Error al enviar la solicitud.";
    }
  }
});


document.getElementById("form-feriante").addEventListener("submit", async function(e) {
  e.preventDefault();

  const mensaje = document.getElementById("mensaje");
  mensaje.textContent = "";

  try {
    // Obtener usuario logueado
    const userRes = await axios.get("/api/usuarios/current");
    const usuario = userRes.data;

    // Enviar solicitud con ID del usuario
    const res = await axios.post(`/api/solicitudes/crear/${usuario.id}`);

    mensaje.style.color = "green";
    mensaje.textContent = "Solicitud enviada correctamente. Será revisada por un administrador.";

  } catch (error) {
    mensaje.style.color = "red";
    if (error.response && error.response.data) {
      mensaje.textContent = " " + error.response.data;
    } else {
      mensaje.textContent = "Error al enviar la solicitud.";
    }
  }
});