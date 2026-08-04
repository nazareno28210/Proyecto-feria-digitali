/**
 * notificaciones.js — Módulo global de notificaciones (SweetAlert2 Toast)
 * Uso: mostrarNotificacion("Mensaje", "success" | "error" | "info" | "warning")
 */

const ToastSwal = Swal.mixin({
  toast: true,
  position: "top-end",
  showConfirmButton: false,
  timerProgressBar: true,
});

/**
 * Muestra una notificación tipo Toast estandarizada.
 * Configuración dinámica de duraciones y pausa al hacer hover.
 * @param {string} mensaje - Texto del mensaje (sin emojis).
 * @param {"success"|"error"|"info"|"warning"} tipo - Tipo de notificación.
 */
function mostrarNotificacion(mensaje, tipo = "info") {
  const tipoValido = ["success", "error", "info", "warning"].includes(tipo) ? tipo : "info";

  // Duración según el tipo
  let timerMs = 4000;
  if (tipoValido === "error" || tipoValido === "warning") {
    timerMs = 5000;
  } else if (tipoValido === "success") {
    timerMs = 2500;
  } else if (tipoValido === "info") {
    timerMs = 4000;
  }

  ToastSwal.fire({
    icon: tipoValido,
    title: mensaje,
    timer: timerMs,
    didOpen: (toast) => {
      toast.addEventListener("mouseenter", Swal.stopTimer);
      toast.addEventListener("mouseleave", Swal.resumeTimer);
    },
    customClass: {
      popup: "toast-fd-popup toast-offset toast-tipo-" + tipoValido,
      title: "toast-fd-title",
      timerProgressBar: "toast-fd-progress",
    },
  });
}

/**
 * Extrae el mensaje de error real devuelto por el backend de Spring Boot.
 * Prioriza message, error, mensaje o texto crudo antes que un mensaje genérico.
 * @param {Object} err - Objeto de error de Axios o JavaScript.
 * @param {string} mensajeDefault - Mensaje de respaldo en caso de no hallar detalle.
 * @returns {string} Texto formateado para mostrar al usuario.
 */
function obtenerMensajeError(err, mensajeDefault = "Error interno del servidor") {
  if (!err) return mensajeDefault;
  const resData = err.response?.data;
  if (!resData) return err.message || mensajeDefault;
  if (typeof resData === "string" && resData.trim() !== "") return resData;
  return resData.message || resData.error || resData.mensaje || mensajeDefault;
}
