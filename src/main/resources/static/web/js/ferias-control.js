/*
 * ====================================
 * FERIAS-CONTROL.JS (CORREGIDO Y BLINDADO)
 * ====================================
 */

const RIO_GRANDE_COORDS = [-53.7860, -67.7070];
let mapaCrear, mapaEditar;
let marcadorCrear, marcadorEditar;

function showToast(message, type = "info") {
    let color;
    switch (type) {
        case "success": color = "linear-gradient(to right, #10b981, #059669)"; break;
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
    if (fechaFin && fechaFin < hoy) {
        const confirmar = confirm("Atención: La fecha de finalización ya pasó. El sistema marcará esta feria como 'Inactiva' automáticamente. ¿Deseas continuar?");
        if (!confirmar) return false;
    }
    return true;
}

function validarUbicacion(lat, lng) {
    if (!lat || !lng || isNaN(lat) || isNaN(lng)) {
        showToast("¡Atención! Debes marcar la ubicación en el mapa", "warning");
        return false;
    }
    return true;
}

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
    const API_FERIAS_URL = "http://localhost:8080/api/ferias";
    const API_EDICIONES_URL = "http://localhost:8080/api/ediciones";

    const hoyInput = new Date().toISOString().split('T')[0];
    const dateInputs = ["fechaInicio", "fechaFinal", "edit-fechaInicio", "edit-fechaFinal"];
    dateInputs.forEach(id => {
        const el = document.getElementById(id);
        if(el) el.setAttribute("min", hoyInput);
    });

    // Validaciones extra de fechas para el nuevo modal
    const inputNewInicio = document.getElementById("new-fechaInicio");
    const inputNewFinal = document.getElementById("new-fechaFinal");
    if(inputNewInicio) inputNewInicio.setAttribute("min", hoyInput);
    if(inputNewFinal) inputNewFinal.setAttribute("min", hoyInput);

    mapaCrear = L.map('mapa-crear').setView(RIO_GRANDE_COORDS, 13);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { attribution: '© OpenStreetMap contributors' }).addTo(mapaCrear);

    mapaCrear.on('click', function(e) {
        const { lat, lng } = e.latlng;
        document.getElementById('latitud').value = lat.toFixed(6);
        document.getElementById('longitud').value = lng.toFixed(6);
        if (marcadorCrear) mapaCrear.removeLayer(marcadorCrear);
        marcadorCrear = L.marker([lat, lng]).addTo(mapaCrear);
    });

    mapaEditar = L.map('mapa-editar').setView(RIO_GRANDE_COORDS, 13);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { attribution: '© OpenStreetMap contributors' }).addTo(mapaEditar);

    mapaEditar.on('click', function(e) {
        const { lat, lng } = e.latlng;
        document.getElementById('edit-latitud').value = lat.toFixed(6);
        document.getElementById('edit-longitud').value = lng.toFixed(6);
        if (marcadorEditar) mapaEditar.removeLayer(marcadorEditar);
        marcadorEditar = L.marker([lat, lng]).addTo(mapaEditar);
    });

    async function cargarFerias() {
        try {
            const res = await axios.get(API_EDICIONES_URL);
            tbody.innerHTML = "";
            
            const feriasActivas = res.data.filter(e => e.estado !== 'ELIMINADO');
            
            if (feriasActivas.length === 0) {
                tbody.innerHTML = "<tr><td colspan='6' style='text-align:center;'>No hay ferias registradas</td></tr>";
                return;
            }

            feriasActivas.forEach(edicion => {
                const row = document.createElement("tr");
                if (edicion.estado === "INACTIVA") row.classList.add("fila-inactiva");
                
                const horaApertura = edicion.horaInicio ? edicion.horaInicio.substring(0, 5) : "--:--";
                const horaCierre = edicion.horaFin ? edicion.horaFin.substring(0, 5) : "--:--";
                
                const nombreBase = edicion.feriaNombre || "Feria General";
                const lugarBase = edicion.feriaLugar || "Lugar a definir";
                const cupo = edicion.feriaCapacidad || edicion.capacidad || (edicion.feria ? edicion.feria.capacidad : null) || 'Sin límite';

                row.innerHTML = `
                    <td>
                        <div style="display: flex; align-items: center; gap: 10px;">
                            <div style="background: #eef2ff; padding: 10px; border-radius: 8px; color: #3b82f6;">
                                <i class="fas fa-store"></i>
                            </div>
                            <div>
                                <strong style="display: block; color: #1e293b;">${nombreBase}</strong>
                                <span style="font-size: 0.85em; color: #64748b;">${edicion.nombreEdicion}</span>
                            </div>
                        </div>
                    </td>
                    <td><span style="color: #64748b;"><i class="fas fa-map-marker-alt"></i> ${lugarBase}</span></td>
                    <td>
                        <div style="color: #1e293b;">${edicion.fechaInicio} al ${edicion.fechaFinal || ''}</div>
                        <div style="font-size: 0.85em; color: #64748b;">${horaApertura} - ${horaCierre} hs</div>
                    </td>
                    <td style="font-weight: bold; color: #1e293b;">${cupo}</td> 
                    <td><span class="badge-${edicion.estado.toLowerCase()}">${edicion.estado}</span></td>
                    <td>
                        <!-- NUEVO BOTÓN: Lanzar Nueva Edición -->
                        <button class="btn-nueva-edicion" onclick='abrirModalNuevaEdicion(${edicion.feriaId}, ${JSON.stringify(nombreBase)})' style="background-color: #8b5cf6; color: white; border: none; padding: 6px 10px; border-radius: 4px; cursor: pointer; margin-right: 5px;" title="Lanzar Nueva Edición">
                            <i class="fas fa-calendar-plus"></i>
                        </button>
                        
                        <button class="btn-editar" onclick='abrirModalEditar(${JSON.stringify(edicion)})' style="margin-right: 5px;" title="Editar">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button class="${edicion.estado === 'ACTIVA' ? 'btn-baja' : 'btn-activar'}" 
                                onclick="cambiarEstadoEdicion(${edicion.id}, '${edicion.estado === 'ACTIVA' ? 'INACTIVA' : 'ACTIVA'}')" style="margin-right: 5px;" title="${edicion.estado === 'ACTIVA' ? 'Desactivar' : 'Activar'}">
                            <i class="fas fa-power-off"></i>
                        </button>
                        <button class="btn-eliminar" onclick="eliminarEdicion(${edicion.id}, ${edicion.feriaId})" title="Eliminar">
                            <i class="fas fa-trash"></i>
                        </button>
                    </td>
                `;
                tbody.appendChild(row);
            });
        } catch (err) {
            showToast("Error al cargar las ferias registradas", "error");
        }
    }

    formCrear.addEventListener("submit", async e => {
        e.preventDefault();
        
        const nombre = document.getElementById("nombre").value;
        const nombreEdicion = document.getElementById("nombre-edicion").value;
        const desc = document.getElementById("descripcion").value;
        const fInicio = document.getElementById("fechaInicio").value;
        const fFinal = document.getElementById("fechaFinal").value;
        const horaInicio = document.getElementById("horaInicio").value + ":00";
        const horaFin = document.getElementById("horaFin").value + ":00";
        const lat = document.getElementById("latitud").value;
        const lng = document.getElementById("longitud").value;
        const capacidad = document.getElementById("capacidad").value; 

        if (!validarFechas(fInicio, fFinal)) return;
        if (!validarUbicacion(lat, lng)) return;
        if (!validarLongitudTexto(nombre, desc)) return;
        if (capacidad < 1) {
            showToast("La capacidad debe ser de al menos 1 stand", "error");
            return;
        }

        const formData = new FormData();
        formData.append("nombre", nombre);
        formData.append("lugar", document.getElementById("lugar").value);
        formData.append("latitud", parseFloat(lat));
        formData.append("longitud", parseFloat(lng));
        formData.append("fechaInicio", fInicio); 
        if (fFinal) formData.append("fechaFinal", fFinal);
        formData.append("descripcion", desc);
        formData.append("capacidad", parseInt(capacidad));

        const inputImagen = document.getElementById("input-feria-imagen");
        if (inputImagen && inputImagen.files[0]) {
            formData.append("imagen", inputImagen.files[0]);
        }
            
        try {
            showToast("Guardando evento...", "info");
            
            // 1. Guardamos el molde base
            const resFeria = await axios.post(API_FERIAS_URL, formData, {
                headers: { "Content-Type": "multipart/form-data" },
                withCredentials: true
            });
            
            const feriaCreada = resFeria.data; 
            console.log("🛠️ RESPUESTA DEL BACKEND AL CREAR FERIA:", feriaCreada);

            // Validamos que el backend haya devuelto un ID válido
            const feriaId = feriaCreada.id || feriaCreada.idFeria;
            if (!feriaId) {
                showToast("Fallo interno: El backend no devolvió el ID de la feria creada.", "error");
                console.error("El objeto devuelto no tiene un campo 'id':", feriaCreada);
                return; // Frenamos acá para no causar el Error 500
            }

            const payloadEdicion = {
                feriaId: parseInt(feriaId), 
                nombreEdicion: nombreEdicion,
                fechaInicio: fInicio,
                fechaFinal: fFinal ? fFinal : null, 
                horaInicio: horaInicio,
                horaFin: horaFin,
                estado: "ACTIVA" 
            };
            
            try {
                // 2. Guardamos la edición
                await axios.post(API_EDICIONES_URL, payloadEdicion, { 
                    headers: { "Content-Type": "application/json" },
                    withCredentials: true 
                });

                showToast("¡Feria y edición creadas exitosamente!", "success");
                
                formCrear.reset();
                const previewCont = document.getElementById('preview-crear-container');
                if(previewCont) previewCont.style.display = 'none';
                if (marcadorCrear) mapaCrear.removeLayer(marcadorCrear);
                
                cargarFerias();

            } catch (edicionError) {
                console.error("Error al crear Edición:", edicionError);
                // CORRECCIÓN: Usamos PUT y agregamos /eliminar
                await axios.put(`${API_FERIAS_URL}/${feriaId}/eliminar`, {}, { withCredentials: true })
                    .catch(e => console.log("Rollback falló", e));
                throw edicionError; 
            }

        } catch (err) {
            console.error(err);
            showToast("Error al crear la Feria base.", "error");
        }
    });

    window.cambiarEstadoEdicion = async (id, nuevoEstado) => {
        try {
            await axios.patch(`${API_EDICIONES_URL}/${id}/estado?nuevoEstado=${nuevoEstado}`);
            showToast(`Edición marcada como ${nuevoEstado}`, "success");
            cargarFerias();
        } catch (err) {
            showToast("Error al cambiar el estado", "error");
        }
    };

    window.eliminarEdicion = async (edicionId, feriaId) => {
        if (!confirm("¿Deseas eliminar permanentemente esta edición y su feria base?")) return;
        try {
            await axios.patch(`${API_EDICIONES_URL}/${edicionId}/estado?nuevoEstado=ELIMINADO`);
            
            if (feriaId) {
                // CORRECCIÓN: Usamos PUT y agregamos /eliminar al final de la URL
                await axios.put(`${API_FERIAS_URL}/${feriaId}/eliminar`, {}, { withCredentials: true }).catch(e => console.log("Omitiendo borrado de molde"));
            }

            showToast("Edición eliminada", "success");
            cargarFerias();
        } catch (err) { 
            showToast("Error al eliminar", "error"); 
        }
    };

    window.abrirModalEditar = (edicion) => {
        document.getElementById("edit-edicion-id").value = edicion.id;
        document.getElementById("edit-feria-id").value = edicion.feriaId;
        document.getElementById("edit-nombre").value = edicion.feriaNombre || '';
        document.getElementById("edit-nombre-edicion").value = edicion.nombreEdicion || '';
        document.getElementById("edit-lugar").value = edicion.feriaLugar || '';
        document.getElementById("edit-latitud").value = edicion.latitud || '';
        document.getElementById("edit-longitud").value = edicion.longitud || '';
        document.getElementById("edit-fechaInicio").value = edicion.fechaInicio || '';
        document.getElementById("edit-fechaFinal").value = edicion.fechaFinal || '';
        document.getElementById("edit-horaInicio").value = edicion.horaInicio ? edicion.horaInicio.substring(0, 5) : '';
        document.getElementById("edit-horaFin").value = edicion.horaFin ? edicion.horaFin.substring(0, 5) : '';
        document.getElementById("edit-descripcion").value = edicion.feriaDescripcion || '';
        document.getElementById("edit-capacidad").value = edicion.feriaCapacidad || edicion.capacidad || (edicion.feria ? edicion.feria.capacidad : '') || '';
        
        const previewCont = document.getElementById('preview-edit-container');
        if(previewCont) previewCont.style.display = 'none';
        const fileInput = document.getElementById('input-edit-feria-imagen');
        if(fileInput) fileInput.value = "";
        
        document.getElementById("modal-editar").style.display = "block";
        
        setTimeout(() => {
            mapaEditar.invalidateSize();
            const pos = [edicion.latitud, edicion.longitud];
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
        
        const edicionId = document.getElementById("edit-edicion-id").value;
        const feriaId = document.getElementById("edit-feria-id").value;

        const nombreEdit = document.getElementById("edit-nombre").value;
        const nombreEdicionEdit = document.getElementById("edit-nombre-edicion").value;
        const descEdit = document.getElementById("edit-descripcion").value;
        const fInicio = document.getElementById("edit-fechaInicio").value;
        const fFinal = document.getElementById("edit-fechaFinal").value;
        const hInicio = document.getElementById("edit-horaInicio").value + ":00";
        const hFin = document.getElementById("edit-horaFin").value + ":00";
        const lat = document.getElementById("edit-latitud").value;
        const lng = document.getElementById("edit-longitud").value;
        const capacidadEdit = document.getElementById("edit-capacidad").value; 

        if (!validarFechas(fInicio, fFinal)) return;
        if (!validarUbicacion(lat, lng)) return;
        if (!validarLongitudTexto(nombreEdit, descEdit)) return;
        if (capacidadEdit < 1) {
            showToast("La capacidad debe ser de al menos 1 stand", "error");
            return;
        }

        const formData = new FormData();
        formData.append("nombre", nombreEdit);
        formData.append("lugar", document.getElementById("edit-lugar").value);
        formData.append("latitud", parseFloat(lat));
        formData.append("longitud", parseFloat(lng));
        formData.append("descripcion", descEdit);
        formData.append("capacidad", parseInt(capacidadEdit)); 

        const inputImagenEdit = document.getElementById("input-edit-feria-imagen");
        if (inputImagenEdit && inputImagenEdit.files[0]) {
            formData.append("imagen", inputImagenEdit.files[0]);
        }

        const payloadEdicionEdit = {
            feriaId: parseInt(feriaId),
            nombreEdicion: nombreEdicionEdit,
            fechaInicio: fInicio,
            fechaFinal: fFinal ? fFinal : null,
            horaInicio: hInicio,
            horaFin: hFin
        };

        try {
            showToast("Actualizando datos...", "info");
            
            await axios.put(`${API_FERIAS_URL}/${feriaId}`, formData, {
                headers: { "Content-Type": "multipart/form-data" },
                withCredentials: true
            });

            await axios.put(`${API_EDICIONES_URL}/${edicionId}`, payloadEdicionEdit, {
                headers: { "Content-Type": "application/json" },
                withCredentials: true
            });

            showToast("Feria actualizada correctamente", "success");
            cerrarModal();
            cargarFerias();
        } catch (err) { 
            console.error(err);
            showToast("Error al actualizar. Verifica el servidor.", "error"); 
        }
    });

    // --- LÓGICA DE NUEVA EDICIÓN (HISTORIAL) ---

    window.abrirModalNuevaEdicion = (feriaId, nombreBase) => {
        document.getElementById("new-feria-id").value = feriaId;
        document.getElementById("new-nombre-base").textContent = nombreBase;
        document.getElementById("form-nueva-edicion").reset();
        document.getElementById("modal-nueva-edicion").style.display = "block";
    };

    window.cerrarModalNuevaEdicion = () => {
        document.getElementById("modal-nueva-edicion").style.display = "none";
    };

    document.getElementById("form-nueva-edicion").addEventListener("submit", async (e) => {
        e.preventDefault();

        const feriaId = document.getElementById("new-feria-id").value;
        const nombreEdicion = document.getElementById("new-nombre-edicion").value;
        const fInicio = document.getElementById("new-fechaInicio").value;
        const fFinal = document.getElementById("new-fechaFinal").value;
        const hInicio = document.getElementById("new-horaInicio").value + ":00";
        const hFin = document.getElementById("new-horaFin").value + ":00";

        if (!validarFechas(fInicio, fFinal)) return;

        const payloadNuevaEdicion = {
            feriaId: parseInt(feriaId),
            nombreEdicion: nombreEdicion,
            fechaInicio: fInicio,
            fechaFinal: fFinal ? fFinal : null,
            horaInicio: hInicio,
            horaFin: hFin,
            estado: "ACTIVA"
        };

        try {
            showToast("Creando nueva edición...", "info");
            
            await axios.post(API_EDICIONES_URL, payloadNuevaEdicion, {
                headers: { "Content-Type": "application/json" },
                withCredentials: true
            });

            showToast("¡Nueva edición lanzada con éxito!", "success");
            cerrarModalNuevaEdicion();
            cargarFerias();
        } catch (err) {
            console.error(err);
            showToast("Error al crear la nueva edición.", "error");
        }
    });

    cargarFerias();
});