// URLs del API
const API_URL = "http://localhost:8080/api/usuarios/current";
const LOGOUT_URL = "http://localhost:8080/api/logout";
const SOLICITUD_URL = "http://localhost:8080/api/solicitudes";
const UPDATE_URL = "http://localhost:8080/api/usuarios/current";
const IMAGE_UPLOAD_URL = "http://localhost:8080/api/usuarios/current/imagen";

const DEFAULT_IMAGE = "https://res.cloudinary.com/demo/image/upload/d_avatar.png/non_existing_id.png";

// Elementos de la Vista
const imgFotoPerfil = document.getElementById("fotoPerfil"); 
const inputFoto = document.getElementById("inputFotoPerfil"); 

// Elementos del Modal Cropper
const modalCropper = document.getElementById("modal-cropper");
const imageToCrop = document.getElementById("image-to-crop");
const btnConfirmCrop = document.getElementById("btn-confirm-crop");
const btnCancelCrop = document.getElementById("btn-cancel-crop");

let cropper = null; // Variable para la instancia de Cropper

document.addEventListener("DOMContentLoaded", () => {
    cargarPerfil();
    document.getElementById("cerrarSesion").addEventListener("click", cerrarSesion);

    // 1. Al seleccionar un archivo: Abrir Modal y preparar Cropper
    if (inputFoto) {
        inputFoto.addEventListener("change", (e) => {
            const archivo = e.target.files[0];
            if (!archivo) return;

            // Validación de tipo de archivo (solo imágenes)
            if (!archivo.type.startsWith('image/')) {
                return showToast("Por favor, selecciona un archivo de imagen.", "error");
            }

            const reader = new FileReader();
            reader.onload = (event) => {
                imageToCrop.src = event.target.result;
                modalCropper.classList.remove("hidden"); // Mostrar el modal

                // Destruir cropper previo si existe
                if (cropper) cropper.destroy();

                // Inicializar Cropper en el modal
                cropper = new Cropper(imageToCrop, {
                    aspectRatio: 1, // Proporción cuadrada perfecta 1:1
                    viewMode: 1, // Asegura que no se salga de la imagen
                    dragMode: 'move', // Mover la imagen
                    autoCropArea: 0.8, // Tamaño inicial del recorte
                    cropBoxMovable: false, // Caja de recorte fija
                    cropBoxResizable: false, // No se puede cambiar el tamaño del cuadrado
                });
            };
            reader.readAsDataURL(archivo);
        });
    }

    // 2. Botón CANCELAR dentro del modal
    if (btnCancelCrop) {
        btnCancelCrop.addEventListener("click", () => {
            cerrarModalYLimpiar();
        });
    }

    // 3. Botón GUARDAR dentro del modal (Recortar y Subir)
    if (btnConfirmCrop) {
    document.getElementById("btn-confirm-crop").addEventListener("click", () => {
        if (!cropper) return;

        const canvas = cropper.getCroppedCanvas({ width: 500, height: 500 });
        
        canvas.toBlob(async (blob) => {
            const formData = new FormData();
            formData.append("imagen", blob, "perfil.jpg"); // Coincide con @RequestParam("imagen") [cite: 68]

            try {
                showToast("Actualizando foto...", "info"); 
                modalCropper.classList.add("hidden"); 

                // 1. Enviamos a Cloudinary [cite: 127]
                const response = await axios.patch(IMAGE_UPLOAD_URL, formData, {
                    headers: { "Content-Type": "multipart/form-data" },
                    withCredentials: true
                });

                // 2. ACTUALIZACIÓN INMEDIATA 
                if (imgFotoPerfil && response.data) {
                    // Forzamos al navegador a cargar la nueva imagen ignorando el caché
                    imgFotoPerfil.src = response.data + "?t=" + new Date().getTime(); 
                }
                
                showToast("¡Foto de perfil actualizada!", "success"); 

                // Opcional: Recargar el perfil completo para sincronizar otros datos si fuera necesario
                cargarPerfil(); 

            } catch (error) {
                console.error("Error al subir:", error); 
                showToast("Error al subir la imagen", "error"); 
            } finally {
                if (cropper) cropper.destroy(); 
                inputFoto.value = ""; 
            }
        }, 'image/jpeg');
    });
}

    // --- Lógica de Edición de Datos de Texto ---
    const btnEditarToggle = document.getElementById("btn-editar-toggle");
    const btnCancelarEdicion = document.getElementById("btn-cancelar-edicion");
    const formEdicion = document.getElementById("form-edicion");

    if (btnEditarToggle) {
        btnEditarToggle.addEventListener("click", () => {
            document.getElementById("edit-email").value = document.getElementById("usuario-email").textContent;
            document.getElementById("edit-nombre").value = document.getElementById("usuario-nombre").textContent;
            document.getElementById("edit-apellido").value = document.getElementById("usuario-apellido").textContent;
            toggleModoEdicion(true);
        });
    }

    if (btnCancelarEdicion) {
        btnCancelarEdicion.addEventListener("click", () => toggleModoEdicion(false));
    }

    if (formEdicion) {
    formEdicion.addEventListener("submit", async (e) => {
        e.preventDefault();
        const btnGuardar = document.getElementById("btn-guardar-edicion");
        btnGuardar.disabled = true;
        btnGuardar.textContent = "Guardando...";

        const datosActualizados = {
            email: document.getElementById("edit-email").value,
            nombre: document.getElementById("edit-nombre").value,
            apellido: document.getElementById("edit-apellido").value
        };

        try {
            // 1. Actualiza nombre, apellido, etc.
            await axios.post(UPDATE_URL, datosActualizados, { withCredentials: true });
            
            showToast("¡Datos actualizados!", "success");
            
            // 2. Recargamos el perfil para ver los cambios de texto
            cargarPerfil(); 
            
            toggleModoEdicion(false);
        } catch (error) {
            console.error("Error al actualizar:", error);
            showToast("Error al actualizar el perfil.", "error");
        } finally {
            btnGuardar.disabled = false;
            btnGuardar.textContent = "Guardar Cambios";
        }
    });
 }
});

// --- Funciones de Utilidad ---

function cerrarModalYLimpiar() {
    modalCropper.classList.add("hidden");
    if (cropper) {
        cropper.destroy();
        cropper = null;
    }
    inputFoto.value = ""; // Limpiar el input para permitir volver a elegir el mismo archivo
}

function cargarPerfil() {
    axios.get(API_URL, { withCredentials: true })
        .then(response => {
            const usuario = response.data;
            if (!usuario) return;

            setText("usuario-email", usuario.email);
            setText("usuario-nombre", usuario.nombre);
            setText("usuario-apellido", usuario.apellido);
            setText("usuario-fecha", usuario.dayRegistrer);

            if (imgFotoPerfil) {
                if (usuario.imagenUrl) {
                    imgFotoPerfil.src = usuario.imagenUrl;
                } else {
                    imgFotoPerfil.src = "/img/default-user.png";
                    imgFotoPerfil.onerror = () => { imgFotoPerfil.src = DEFAULT_IMAGE; };
                }
            }
            if (usuario.tipoUsuario === "NORMAL") verificarSolicitudFeriante(usuario.email);
        })
        .catch(err => console.error("Error al cargar perfil:", err));
}

function toggleModoEdicion(activar) {
    document.getElementById("vista-lectura").classList.toggle("hidden", activar);
    document.getElementById("form-edicion").classList.toggle("hidden", !activar);
    document.getElementById("btn-editar-toggle").classList.toggle("hidden", activar);
}

async function verificarSolicitudFeriante(emailUsuario) {
    const container = document.getElementById("feriante-solicitud-container");
    if (!container) return;
    try {
        const res = await axios.get(`${SOLICITUD_URL}/pendientes`, { withCredentials: true });
        const tienePendiente = res.data.some(s => s.emailUsuario === emailUsuario);
        
        container.innerHTML = tienePendiente 
            ? `<div class="mensaje-pendiente">Tu solicitud para ser feriante está pendiente.</div>`
            : `<a href="/web/solicitud-feriante.html" class="accion-card accion-card--green">
                  <div class="icon">🚀</div>
                  <h3>Quiero ser Feriante</h3>
                  <p>Envía tu solicitud para crear un stand.</p>
               </a>`;
    } catch (e) { console.error(e); }
}

function cerrarSesion() {
    axios.post(LOGOUT_URL, {}, { withCredentials: true })
        .then(() => {
            showToast("Sesión cerrada", "success");
            setTimeout(() => window.location.href = "/web/login.html", 1000);
        });
}

function setText(id, texto) {
    const el = document.getElementById(id);
    if (el) el.textContent = texto || "-";
}

function showToast(mensaje, tipo = "info") {
    let color = tipo === "success" ? "#2ecc71" : (tipo === "error" ? "#e74c3c" : "#3498db");
    Toastify({ text: mensaje, duration: 3000, gravity: "top", position: "right", style: { background: color } }).showToast();
}