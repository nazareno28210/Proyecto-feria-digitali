document.getElementById("form-feriante").addEventListener("submit", async function(e) {
  e.preventDefault();

  const mensaje = document.getElementById("mensaje");
  mensaje.textContent = "";

  try {
    // 1. Obtener usuario logueado
    const userRes = await axios.get("/api/usuarios/current");
    const usuario = userRes.data;

    // 2. Capturar datos del formulario
    const datosFormulario = {
        nombreEmprendimiento: document.getElementById("nombreEmprendimiento").value,
        descripcion: document.getElementById("descripcion").value,
        telefono: document.getElementById("telefono").value,
        emailEmprendimiento: document.getElementById("email").value
    };

    // 3. Enviar solicitud CON los datos en el body (segundo parámetro de axios.post)
    // NOTA: asegúrate de que la URL de tu controlador coincida.
    const res = await axios.post(`/api/solicitudes/crear/${usuario.id}`, datosFormulario);

    mensaje.style.color = "green";
    mensaje.textContent = "Solicitud enviada correctamente. Será revisada por un administrador.";

  } catch (error) {
    console.error(error);
    mensaje.style.color = "red";
    if (error.response && error.response.data) {
      // Si el backend envía un mensaje de error simple, mostrarlo
      mensaje.textContent = "Error: " + error.response.data;
    } else {
      mensaje.textContent = "Error al enviar la solicitud.";
    }
  }
});