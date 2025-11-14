/*
 * ====================================
 * SOLICITUD-FERIANTE.JS (con Toastify y Redirección)
 * ====================================
 */

// 1. AÑADIDA: Función Toastify
function showToast(message, type = "info") {
  let color;
  switch (type) {
    case "success":
      color = "linear-gradient(to right, #1a3a5a, #3b82f6)"; 
      break;
    case "error":
      color = "linear-gradient(to right, #ef4444, #b91c1c)"; 
      break;
    case "warning":
      color = "linear-gradient(to right, #f59e0b, #d97706)"; 
      break;
    default:
      color = "linear-gradient(to right, #3b82f6, #67e8f9)"; 
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

document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("form-feriante");
  // const mensaje = document.getElementById("mensaje"); // Ya no se usa

  form.addEventListener("submit", async function (e) {
    e.preventDefault();
    // mensaje.textContent = ""; // Ya no se usa

    try {
      // 1️⃣ Obtener usuario logueado
      const userRes = await axios.get("/api/usuarios/current", { withCredentials: true });
      const usuario = userRes.data;

      if (!usuario || !usuario.id) {
        // CAMBIO: Reemplazado por Toast
        showToast("⚠️ No se pudo identificar al usuario. Inicie sesión nuevamente.", "warning");
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

      // 4. CAMBIOS EN ÉXITO
      showToast(res.data || "✅ Solicitud enviada correctamente.", "success");
      
      // Deshabilitar el botón para evitar doble envío
      form.querySelector('button[type="submit"]').disabled = true;

      // Redirigir a ferias.html después de 1.5s
      setTimeout(() => {
          window.location.href = "/web/ferias.html";
      }, 1500);

    } catch (error) {
      console.error("Error al enviar la solicitud:", error); 
      
      // 5. CAMBIOS EN ERROR
      if (error.response) {
        // Muestra el mensaje de error específico del backend (ej: "Ya tienes una solicitud")
        showToast("❌ " + (error.response.data || "Error en el envío de la solicitud."), "error"); 
      } else {
        showToast("Error al conectar con el servidor. Intenta más tarde.", "error"); 
      }
    }
  });
});