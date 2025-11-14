/*
 * ====================================
 * SOLICITUD-FERIANTE.JS (Corregido)
 * ====================================
 */

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
      color = "linear-gradient(to right, #3b82f6, #67e8f9)"; 
      break;
    default:
      color = "linear-gradient(to right, #3b82f6, #67e8f9)"; 
  }
  Toastify({
    text: message,
    duration: 4000,
    gravity: "top", 
    position: "right", 
    // CAMBIO: Arreglo del warning de Toastify
    style: {
        background: color,
    },
    stopOnFocus: true,
  }).showToast();
}

document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("form-feriante");

  form.addEventListener("submit", async function (e) {
    e.preventDefault();
    
    try {
      // 1️⃣ Obtener usuario logueado
      const userRes = await axios.get("/api/usuarios/current", { withCredentials: true });
      
      // !! LÍNEA CORREGIDA: Esta línea faltaba en mi código anterior !!
      const usuario = userRes.data; 

      if (!usuario || !usuario.id) {
        showToast("⚠️ No se pudo identificar al usuario. Inicie sesión nuevamente.", "warning");
        return;
      }

      // 2️⃣ Capturar datos del formulario
      const nombreEmprendimiento = document.getElementById("nombreEmprendimiento").value;
      const descripcion = document.getElementById("descripcion").value;
      const telefono = document.getElementById("telefono").value; 
      const emailEmprendimiento = document.getElementById("email").value;

      // ===================================
      //   VALIDACIÓN DE TELÉFONO
      // ===================================
      // Esta RegEx permite solo números, espacios, +, -, ( y )
      const telefonoRegex = /^[0-9\s+\-()]*$/; 

      if (!telefonoRegex.test(telefono)) {
        showToast("❌ El teléfono solo puede contener números.", "error");
        return; // Detiene el envío
      }
      // ===================================

      const datosFormulario = {
        nombreEmprendimiento: nombreEmprendimiento,
        descripcion: descripcion,
        telefono: telefono,
        emailEmprendimiento: emailEmprendimiento
      };

      // 3️⃣ Enviar solicitud
      const res = await axios.post(
        `/api/solicitudes/crear/${usuario.id}`, // Esta línea ahora funcionará
        datosFormulario,
        { withCredentials: true }
      );

      // 4. ÉXITO
      showToast(res.data || "✅ Solicitud enviada correctamente.", "success");
      
      form.querySelector('button[type="submit"]').disabled = true;

      // Redirigir a ferias.html después de 1.5s
      setTimeout(() => {
          window.location.href = "/web/ferias.html";
      }, 1500);

    } catch (error) {
      console.error("Error al enviar la solicitud:", error);
      
      // 5. MANEJO DE ERROR
      if (error.response) {
        // Error del backend (ej: "Teléfono inválido", "Email ya existe")
        showToast("❌ " + (error.response.data || "Error en el envío de la solicitud."), "error");
      } else {
        // Error de JS (como el ReferenceError) o de red
        showToast("Error al conectar con el servidor. Intenta más tarde.", "error");
        // Imprimimos el error real en la consola para depurar
        console.error("El error real es:", error); 
      }
    }
  });
});