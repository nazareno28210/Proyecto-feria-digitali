const RIO_GRANDE_COORDS = [-53.7860, -67.7070];
let mapaCrear, mapaEditar;
let marcadorCrear, marcadorEditar;

function showToast(message, type = "info") {
    let color;
    switch (type) {
        case "success": color = "linear-gradient(to right, #1a3a5a, #3b82f6)"; break;
        case "error": color = "linear-gradient(to right, #ef4444, #b91c1c)"; break;
        case "warning": color = "linear-gradient(to right, #f59e0b, #d97706)"; break;
        default: color = "linear-gradient(to right, #3b82f6, #67e8f9)";
    }
    Toastify({
        text: message,
        duration: 3000,
        gravity: "top",
        position: "right",
        style: { background: color },
    }).showToast();
}

// --- NUEVA FUNCIÓN: Previsualización de Imagen ---
function previewImagen(event, idImg, idContainer) {
    const reader = new FileReader();
    reader.onload = function() {
        const preview = document.getElementById(idImg);
        const container = document.getElementById(idContainer);
        if(preview) preview.src = reader.result;
        if(container) container.style.display = 'block';
    }
    if(event.target.files[0]) reader.readAsDataURL(event.target.files[0]);
}

// 🟢 FUNCIÓN DE VALIDACIÓN DE FECHAS
function validarFechas(inicio, fin) {
    const hoy = new Date();
    hoy.setHours(0, 0, 0, 0); 
    const fechaInicio = new Date(inicio + "T00:00:00"); 
    const fechaFin = fin ? new Date(fin + "T00:00:00") : null;

    if (fechaInicio < hoy) {
        showToast("La fecha de inicio no puede ser anterior a hoy", "error");
        return false;
    }
    if (fechaFin && fechaFin < fechaInicio) {
        showToast("La fecha final no puede ser anterior a la de inicio", "error");
        return false;
    }

    // 🟡 ADVERTENCIA DE AUTO-CIERRE: Si la fecha fin ya pasó respecto a hoy
    if (fechaFin && fechaFin < hoy) {
        const confirmar = confirm("Atención: La fecha de finalización ya pasó. El sistema marcará esta feria como 'Inactiva' automáticamente. ¿Deseas continuar?");
        if (!confirmar) return false;
    }

    return true;
}

// 🟢 FUNCIÓN DE VALIDACIÓN DE UBICACIÓN
function validarUbicacion(lat, lng) {
    if (!lat || !lng || isNaN(lat) || isNaN(lng)) {
        showToast("¡Atención! Debes marcar la ubicación en el mapa", "warning");
        return false;
    }
    return true;
}

// 🟢 FUNCIÓN DE VALIDACIÓN DE LONGITUD DE TEXTO
function validarLongitudTexto(nombre, descripcion) {
    if (nombre.trim().length < 3 || nombre.trim().length > 75) {
        showToast("El nombre debe tener entre 3 y 75 caracteres", "error");
        return false;
    }
    if (descripcion.trim().length > 300) {
        showToast("La descripción no puede superar los 300 caracteres", "error");
        return false;
    }
    return true;
}

document.addEventListener("DOMContentLoaded", () => {
    const formCrear = document.getElementById("form-feria");
    const tbody = document.querySelector("#tabla-ferias tbody");
    const API_BASE_URL = "http://localhost:8080/api/ferias";

    const hoyInput = new Date().toISOString().split('T')[0];
    const dateInputs = ["fechaInicio", "fechaFinal", "edit-fechaInicio", "edit-fechaFinal"];
    dateInputs.forEach(id => {
        const el = document.getElementById(id);
        if(el) el.setAttribute("min", hoyInput);
    });

    // --- LÓGICA DE MAPAS ---
    mapaCrear = L.map('mapa-crear').setView(RIO_GRANDE_COORDS, 13);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors'
    }).addTo(mapaCrear);

    mapaCrear.on('click', function(e) {
        const { lat, lng } = e.latlng;
        document.getElementById('latitud').value = lat.toFixed(6);
        document.getElementById('longitud').value = lng.toFixed(6);
        
        if (marcadorCrear) mapaCrear.removeLayer(marcadorCrear);
        marcadorCrear = L.marker([lat, lng]).addTo(mapaCrear);
    });

    mapaEditar = L.map('mapa-editar').setView(RIO_GRANDE_COORDS, 13);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors'
    }).addTo(mapaEditar);

    mapaEditar.on('click', function(e) {
        const { lat, lng } = e.latlng;
        document.getElementById('edit-latitud').value = lat.toFixed(6);
        document.getElementById('edit-longitud').value = lng.toFixed(6);
        
        if (marcadorEditar) mapaEditar.removeLayer(marcadorEditar);
        marcadorEditar = L.marker([lat, lng]).addTo(mapaEditar);
    });

    async function cargarFerias() {
        try {
            // El backend ahora ejecuta obtenerFeriasActualizadas() internamente al llamar a este GET
            const res = await axios.get(API_BASE_URL);
            tbody.innerHTML = "";
            res.data.forEach(f => {
                const row = document.createElement("tr");
                // Si el backend la marcó como Inactiva por fecha, se verá gris automáticamente
                if (f.estado === "Inactiva") row.classList.add("fila-inactiva");
                
                row.innerHTML = `
                    <td>${f.nombre}</td>
                    <td>${f.lugar}</td>
                    <td>${f.fechaInicio} / ${f.fechaFinal || 'Sin fecha fin'}</td>
                    <td>${f.capacidad ? f.capacidad : 'Sin límite'}</td> <td><span class="badge-${f.estado.toLowerCase()}">${f.estado}</span></td>
                    <td>
                        <button class="btn-editar" onclick='abrirModalEditar(${JSON.stringify(f)})'>
                            <i class="fas fa-edit"></i>
                        </button>
                        <button class="${f.estado === 'Activa' ? 'btn-baja' : 'btn-activar'}" 
                                onclick="${f.estado === 'Activa' ? 'darBaja' : 'activar'}(${f.id})">
                            ${f.estado === 'Activa' ? 'Desactivar' : 'Activar'}
                        </button>
                        <button class="btn-eliminar" onclick="eliminar(${f.id})">
                            <i class="fas fa-trash"></i>
                        </button>
                    </td>
                `;
                tbody.appendChild(row);
            });
        } catch (err) {
            showToast("Error al cargar ferias", "error");
        }
    }

    formCrear.addEventListener("submit", async e => {
        e.preventDefault();
        
        const nombre = document.getElementById("nombre").value;
        const desc = document.getElementById("descripcion").value;
        const fInicio = document.getElementById("fechaInicio").value;
        const fFinal = document.getElementById("fechaFinal").value;
        const lat = document.getElementById("latitud").value;
        const lng = document.getElementById("longitud").value;
        const capacidad = document.getElementById("capacidad").value; // 🟢 CAPTURAMOS LA CAPACIDAD

        if (!validarFechas(fInicio, fFinal)) return;
        if (!validarUbicacion(lat, lng)) return;
        if (!validarLongitudTexto(nombre, desc)) return;
        if (capacidad < 1) {
            showToast("La capacidad debe ser de al menos 1 stand", "error");
            return;
        }

        // --- CAMBIO AQUÍ: Envío con FormData ---
        const formData = new FormData();
        formData.append("nombre", nombre);
        formData.append("lugar", document.getElementById("lugar").value);
        formData.append("latitud", parseFloat(lat));
        formData.append("longitud", parseFloat(lng));
        formData.append("fechaInicio", fInicio);
        if (fFinal) formData.append("fechaFinal", fFinal);
        formData.append("descripcion", desc);
        formData.append("capacidad", parseInt(capacidad)); // 🟢 AGREGAMOS AL FORM DATA

        const inputImagen = document.getElementById("input-feria-imagen");
        if (inputImagen && inputImagen.files[0]) {
            formData.append("imagen", inputImagen.files[0]);
        }
            
        try {
            showToast("Creando feria...", "info");
            await axios.post(API_BASE_URL, formData, {
                headers: { "Content-Type": "multipart/form-data" },
                withCredentials: true
            });
            showToast("Feria creada correctamente", "success");
            
            formCrear.reset();
            const previewCont = document.getElementById('preview-crear-container');
            if(previewCont) previewCont.style.display = 'none';
            if (marcadorCrear) mapaCrear.removeLayer(marcadorCrear);
            
            cargarFerias();
        } catch (err) {
            showToast(err.response?.data || "Error al crear", "error");
        }
    });

    window.activar = async (id) => {
        try {
            // La petición ahora puede fallar si el backend detecta fecha vencida
            const response = await axios.patch(`${API_BASE_URL}/${id}/activar`);
            showToast("Feria activada correctamente", "success");
            cargarFerias();
        } catch (err) {
            // Si el servidor devuelve el error de fecha pasada (400), lo mostramos aquí
            const mensajeError = err.response?.data || "Error al activar la feria";
            showToast(mensajeError, "error");
            console.error("Error al activar:", err);
        }
    };

    window.darBaja = async (id) => {
        try {
            await axios.patch(`${API_BASE_URL}/${id}/baja`);
            showToast("Feria desactivada", "warning");
            cargarFerias();
        } catch (err) { showToast("Error al desactivar", "error"); }
    };

    window.eliminar = async (id) => {
        if (!confirm("¿Deseas eliminar esta feria?")) return;
        try {
            await axios.put(`${API_BASE_URL}/${id}/eliminar`);
            showToast("Feria eliminada", "success");
            cargarFerias();
        } catch (err) { showToast("Error al eliminar", "error"); }
    };

    window.abrirModalEditar = (feria) => {
        document.getElementById("edit-id").value = feria.id;
        document.getElementById("edit-nombre").value = feria.nombre;
        document.getElementById("edit-lugar").value = feria.lugar;
        document.getElementById("edit-latitud").value = feria.latitud;
        document.getElementById("edit-longitud").value = feria.longitud;
        document.getElementById("edit-fechaInicio").value = feria.fechaInicio;
        document.getElementById("edit-fechaFinal").value = feria.fechaFinal;
        document.getElementById("edit-descripcion").value = feria.descripcion;
        
        // 🟢 NUEVO: Auto-completamos el cupo en el modal de edición
        document.getElementById("edit-capacidad").value = feria.capacidad || ''; 
        
        // Reset preview al abrir
        const previewCont = document.getElementById('preview-edit-container');
        if(previewCont) previewCont.style.display = 'none';
        const fileInput = document.getElementById('input-edit-feria-imagen');
        if(fileInput) fileInput.value = "";
        
        document.getElementById("modal-editar").style.display = "block";
        
        setTimeout(() => {
            mapaEditar.invalidateSize();
            const pos = [feria.latitud, feria.longitud];
            mapaEditar.setView(pos, 15);
            if (marcadorEditar) mapaEditar.removeLayer(marcadorEditar);
            marcadorEditar = L.marker(pos).addTo(mapaEditar);
        }, 200);
    };

    window.cerrarModal = () => {
        document.getElementById("modal-editar").style.display = "none";
    };

    document.getElementById("form-editar").addEventListener("submit", async (e) => {
        e.preventDefault();
        
        const nombreEdit = document.getElementById("edit-nombre").value;
        const descEdit = document.getElementById("edit-descripcion").value;
        const fInicio = document.getElementById("edit-fechaInicio").value;
        const fFinal = document.getElementById("edit-fechaFinal").value;
        const lat = document.getElementById("edit-latitud").value;
        const lng = document.getElementById("edit-longitud").value;
        const capacidadEdit = document.getElementById("edit-capacidad").value; // 🟢 CAPTURAMOS LA EDICIÓN DE CAPACIDAD

        if (!validarFechas(fInicio, fFinal)) return;
        if (!validarUbicacion(lat, lng)) return;
        if (!validarLongitudTexto(nombreEdit, descEdit)) return;
        if (capacidadEdit < 1) {
            showToast("La capacidad debe ser de al menos 1 stand", "error");
            return;
        }

        const id = document.getElementById("edit-id").value;
        
        // --- CAMBIO AQUÍ: Envío con FormData ---
        const formData = new FormData();
        formData.append("nombre", nombreEdit);
        formData.append("lugar", document.getElementById("edit-lugar").value);
        formData.append("latitud", parseFloat(lat));
        formData.append("longitud", parseFloat(lng));
        formData.append("fechaInicio", fInicio);
        if (fFinal) formData.append("fechaFinal", fFinal);
        formData.append("descripcion", descEdit);
        formData.append("capacidad", parseInt(capacidadEdit)); // 🟢 AGREGAMOS AL FORM DATA EN EDICIÓN

        const inputImagenEdit = document.getElementById("input-edit-feria-imagen");
        if (inputImagenEdit && inputImagenEdit.files[0]) {
            formData.append("imagen", inputImagenEdit.files[0]);
        }

        try {
            showToast("Actualizando feria...", "info");
            await axios.put(`${API_BASE_URL}/${id}`, formData, {
                headers: { "Content-Type": "multipart/form-data" },
                withCredentials: true
            });
            showToast("Feria actualizada", "success");
            cerrarModal();
            cargarFerias();
        } catch (err) { 
            showToast(err.response?.data || "Error al actualizar", "error"); 
        }
    });

    cargarFerias();
});