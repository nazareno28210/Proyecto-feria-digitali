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

            for (const edicion of feriasActivas) {
                const row = document.createElement("tr");
                if (edicion.estado === "INACTIVA") row.classList.add("fila-inactiva");
                
                const horaApertura = edicion.horaInicio ? edicion.horaInicio.substring(0, 5) : "--:--";
                const horaCierre = edicion.horaFin ? edicion.horaFin.substring(0, 5) : "--:--";
                
                const nombreBase = edicion.feriaNombre || "Feria General";
                const lugarBase = edicion.feriaLugar || "Lugar a definir";
                const cupo = edicion.feriaCapacidad || edicion.capacidad || (edicion.feria ? edicion.feria.capacidad : null) || 'Sin límite';

                let ocupados = 0;
                try {
                    const resEspacios = await axios.get(`${API_ESPACIOS_URL}/edicion/${edicion.id}`);
                    if (resEspacios.data && Array.isArray(resEspacios.data)) {
                        ocupados = resEspacios.data.filter(e => e.estado === 'OCUPADO').length;
                    }
                } catch (e) {
                    console.error("Error al obtener espacios de la edición " + edicion.id, e);
                }

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
                    <td>
                        <strong>${ocupados} / ${cupo}</strong><br>
                        <span style="font-size:0.8em; color: gray;">Stands ocupados</span>
                    </td> 
                    <td><span class="badge-${edicion.estado.toLowerCase()}">${edicion.estado}</span></td>
                    <td>
                        <!-- NUEVO BOTÓN: Lanzar Nueva Edición -->
                        <button class="btn-nueva-edicion" onclick='abrirModalNuevaEdicion(${edicion.feriaId}, ${JSON.stringify(nombreBase)})' style="background-color: #8b5cf6; color: white; border: none; padding: 6px 10px; border-radius: 4px; cursor: pointer; margin-right: 5px;" title="Lanzar Nueva Edición">
                            <i class="fas fa-calendar-plus"></i>
                        </button>
                        
                        <!-- BOTÓN: Configurar Espacios/Lotes -->
                        <button class="btn-espacios" onclick='abrirModalEspacios(${edicion.id}, "${edicion.nombreEdicion}")' style="background-color: #0ea5e9; color: white; border: none; padding: 6px 10px; border-radius: 4px; cursor: pointer; margin-right: 5px;" title="Configurar Espacios">
                            <i class="fas fa-map-marked-alt"></i>
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
            }
        } catch (err) {
            showToast("Error al cargar las ferias registradas", "error");
        }
    }

    if (formCrear) {
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

            const payloadEdicion = new FormData();
            payloadEdicion.append("feriaId", parseInt(feriaId));
            payloadEdicion.append("nombreEdicion", nombreEdicion);
            payloadEdicion.append("fechaInicio", fInicio);
            payloadEdicion.append("fechaFinal", fFinal ? fFinal : "");
            payloadEdicion.append("horaInicio", horaInicio);
            payloadEdicion.append("horaFin", horaFin);
            
            // Adjuntamos el mapa del predio si el usuario seleccionó uno
            const inputMapaCrear = document.getElementById("input-mapa-nueva-feria");
            if (inputMapaCrear && inputMapaCrear.files[0]) {
                payloadEdicion.append("mapa", inputMapaCrear.files[0]);
            }
            
            try {
                // 2. Guardamos la edición
                await axios.post(API_EDICIONES_URL, payloadEdicion, { 
                    headers: { "Content-Type": "multipart/form-data" },
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
                if (edicionError.response && edicionError.response.status === 400) {
                    showToast(edicionError.response.data?.error || "Fechas inválidas", "error");
                    return;
                }
                // CORRECCIÓN: Usamos PUT y agregamos /eliminar
                await axios.put(`${API_FERIAS_URL}/${feriaId}/eliminar`, {}, { withCredentials: true })
                    .catch(e => console.log("Rollback falló", e));
                throw edicionError; 
            }

        } catch (err) {
            console.error(err);
            showToast("Error al crear la Feria base.", "error");
        }
    }); // end formCrear submit
    } // end if (formCrear)

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

    const formEditar = document.getElementById("form-editar");
    if (formEditar) {
        formEditar.addEventListener("submit", async (e) => {
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

            const payloadEdicionEdit = new FormData();
            payloadEdicionEdit.append("feriaId", parseInt(feriaId));
            payloadEdicionEdit.append("nombreEdicion", nombreEdicionEdit);
            payloadEdicionEdit.append("fechaInicio", fInicio);
            payloadEdicionEdit.append("fechaFinal", fFinal ? fFinal : "");
            payloadEdicionEdit.append("horaInicio", hInicio);
            payloadEdicionEdit.append("horaFin", hFin);

            const inputMapaEdit = document.getElementById("input-edit-mapa-edicion");
            if (inputMapaEdit && inputMapaEdit.files[0]) {
                payloadEdicionEdit.append("mapa", inputMapaEdit.files[0]);
            }

            try {
                showToast("Actualizando datos...", "info");
                
                await axios.put(`${API_FERIAS_URL}/${feriaId}`, formData, {
                    headers: { "Content-Type": "multipart/form-data" },
                    withCredentials: true
                });

                await axios.put(`${API_EDICIONES_URL}/${edicionId}`, payloadEdicionEdit, {
                    headers: { "Content-Type": "multipart/form-data" },
                    withCredentials: true
                });

                showToast("Feria actualizada correctamente", "success");
                cerrarModal();
                cargarFerias();
            } catch (err) { 
                console.error(err);
                if (err.response && err.response.status === 400) {
                    showToast(err.response.data?.error || "Fechas inválidas", "error");
                } else {
                    showToast("Error al actualizar. Verifica el servidor.", "error");
                }
            }
        });
    }

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

    const formNuevaEdicion = document.getElementById("form-nueva-edicion");
    if (formNuevaEdicion) {
        formNuevaEdicion.addEventListener("submit", async (e) => {
            e.preventDefault();

            const feriaId = document.getElementById("new-feria-id").value;
            const nombreEdicion = document.getElementById("new-nombre-edicion").value;
            const fInicio = document.getElementById("new-fechaInicio").value;
            const fFinal = document.getElementById("new-fechaFinal").value;
            const hInicio = document.getElementById("new-horaInicio").value + ":00";
            const hFin = document.getElementById("new-horaFin").value + ":00";

            if (!validarFechas(fInicio, fFinal)) return;

            const payloadNuevaEdicion = new FormData();
            payloadNuevaEdicion.append("feriaId", parseInt(feriaId));
            payloadNuevaEdicion.append("nombreEdicion", nombreEdicion);
            payloadNuevaEdicion.append("fechaInicio", fInicio);
            payloadNuevaEdicion.append("fechaFinal", fFinal ? fFinal : "");
            payloadNuevaEdicion.append("horaInicio", hInicio);
            payloadNuevaEdicion.append("horaFin", hFin);

            const inputMapaNueva = document.getElementById("input-new-mapa-edicion");
            if (inputMapaNueva && inputMapaNueva.files[0]) {
                payloadNuevaEdicion.append("mapa", inputMapaNueva.files[0]);
            }

            try {
                showToast("Creando nueva edición...", "info");
                
                await axios.post(API_EDICIONES_URL, payloadNuevaEdicion, {
                    headers: { "Content-Type": "multipart/form-data" },
                    withCredentials: true
                });

                showToast("¡Nueva edición lanzada con éxito!", "success");
                cerrarModalNuevaEdicion();
                cargarFerias();
            } catch (err) {
                console.error(err);
                if (err.response && err.response.status === 400) {
                    showToast(err.response.data?.error || "Fechas inválidas", "error");
                } else {
                    showToast("Error al crear la nueva edición.", "error");
                }
            }
        });
    }

    const buscador = document.getElementById("buscador-ferias");
    if (buscador) {
        buscador.addEventListener("keyup", () => {
            const query = buscador.value.toLowerCase().trim();
            const filas = tbody.querySelectorAll("tr");
            filas.forEach(row => {
                const texto = (row.innerText || row.textContent || "").toLowerCase();
                row.style.display = texto.includes(query) ? "" : "none";
            });
        });
    }

    cargarFerias();
});

// ============================================================
// GESTIÓN DE ESPACIOS / LOTES (fuera del DOMContentLoaded)
// ============================================================
const API_ESPACIOS_URL = "http://localhost:8080/api/espacios";
let _edicionIdActiva = null;

window.abrirModalEspacios = async (edicionId, nombreEdicion) => {
    _edicionIdActiva = edicionId;
    document.getElementById("espacios-edicion-nombre").textContent = nombreEdicion;
    document.getElementById("nuevo-espacio-nombre").value = "";
    document.getElementById("nuevo-espacio-precio").value = "";
    document.getElementById("modal-espacios").style.display = "block";
    await cargarEspacios();
};

window.cerrarModalEspacios = () => {
    document.getElementById("modal-espacios").style.display = "none";
    _edicionIdActiva = null;
};

let _espaciosCargados = [];

async function cargarEspacios() {
    const tbody = document.getElementById("tbody-espacios");
    tbody.innerHTML = `<tr><td colspan="4" style="text-align:center; padding:20px; color:#94a3b8;">Cargando...</td></tr>`;
    try {
        const res = await axios.get(`${API_ESPACIOS_URL}/edicion/${_edicionIdActiva}`);
        const espacios = res.data;
        _espaciosCargados = espacios;
        console.log("Espacios recibidos:", espacios);
        if (!espacios.length) {
            tbody.innerHTML = `<tr><td colspan="4" style="text-align:center; padding:20px; color:#94a3b8;">Sin espacios registrados aún.</td></tr>`;
            return;
        }
        tbody.innerHTML = espacios.map(e => {
            const colorMap = { OCUPADO: '#ef4444', DISPONIBLE: '#10b981', MANTENIMIENTO: '#f59e0b', RESERVADO: '#3b82f6' };
            const estadoColor = colorMap[e.estado] || '#94a3b8';
            const bloqueado = e.estado === 'OCUPADO';
            const disabledAttr = bloqueado ? 'disabled' : '';
            const btnEditarStyle = bloqueado ? 'background:#94a3b8; cursor:not-allowed;' : 'background:#f59e0b; color:white; border:none; padding:5px 10px; border-radius:4px; cursor:pointer; margin-right:4px;';
            const btnEliminarStyle = bloqueado ? 'background:#94a3b8; cursor:not-allowed;' : 'background:#ef4444; color:white; border:none; padding:5px 10px; border-radius:4px; cursor:pointer; margin-right:4px;';

            let botonEstadoHtml = '';
            if (e.estado === 'DISPONIBLE') {
                botonEstadoHtml = `
                    <button onclick="enviarAMantenimiento(${e.id})"
                        style="background:#ea580c; color:white; border:none; padding:5px 10px; border-radius:4px; cursor:pointer; margin-right:4px;" title="Enviar a Mantenimiento">
                        <i class="fas fa-wrench"></i>
                    </button>`;
            } else if (e.estado === 'MANTENIMIENTO') {
                botonEstadoHtml = `
                    <button onclick="liberarEspacio(${e.id})"
                        style="background:#10b981; color:white; border:none; padding:5px 10px; border-radius:4px; cursor:pointer; margin-right:4px;" title="Liberar Espacio">
                        <i class="fas fa-check-circle"></i> Liberar
                    </button>`;
            }

            const infoMotivo = (e.estado === 'MANTENIMIENTO' && e.motivoMantenimiento)
                ? ` <i class="fas fa-info-circle" title="Motivo: ${e.motivoMantenimiento.replace(/"/g, '&quot;')}" style="cursor:pointer; color:#ea580c; margin-left:4px;"></i>`
                : '';

            return `<tr style="border-bottom:1px solid #f1f5f9;">
                <td style="padding:10px 12px;">${e.nombre}</td>
                <td style="padding:10px 12px;">$${Number(e.precio).toFixed(2)}</td>
                <td style="padding:10px 12px;"><span style="color:${estadoColor}; font-weight:600;">${e.estado}${infoMotivo}</span></td>
                <td style="padding:10px 12px; text-align:center;">
                    ${botonEstadoHtml}
                    <button ${disabledAttr} onclick="editarEspacio(${e.id}, this.dataset.nombre, ${e.precio})" data-nombre="${e.nombre.replace(/"/g, '&quot;')}"
                        style="${btnEditarStyle}" title="Editar">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button ${disabledAttr} onclick="eliminarEspacio(${e.id})"
                        style="${btnEliminarStyle}" title="Eliminar">
                        <i class="fas fa-trash"></i>
                    </button>
                </td>
            </tr>`;
        }).join("");
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="4" style="text-align:center; padding:20px; color:#ef4444;">Error al cargar espacios.</td></tr>`;
        console.error(err);
    }
}

window.liberarEspacio = async (id) => {
    const result = await Swal.fire({
        title: 'Liberar espacio',
        text: '¿Estás seguro de liberar este espacio para que vuelva a estar DISPONIBLE?',
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#10b981',
        cancelButtonColor: '#94a3b8',
        confirmButtonText: 'Sí, liberar',
        cancelButtonText: 'Cancelar'
    });

    if (!result.isConfirmed) return;

    try {
        await axios.put(`${API_ESPACIOS_URL}/${id}/estado`, { estado: 'DISPONIBLE' }, { withCredentials: true });
        showToast('Espacio liberado a DISPONIBLE', 'success');
        await cargarEspacios();
    } catch (err) {
        showToast(err.response?.data?.error || 'Error al liberar espacio', 'error');
    }
};

window.agregarEspacio = async () => {
    const nombreZona = document.getElementById("nuevo-espacio-nombre").value.trim();
    const precio = parseFloat(document.getElementById("nuevo-espacio-precio").value);
    const desde = parseInt(document.getElementById("nuevo-espacio-desde").value);
    const hasta = parseInt(document.getElementById("nuevo-espacio-hasta").value);
    if (!nombreZona) { showToast("El nombre de zona es obligatorio", "warning"); return; }
    if (isNaN(precio) || precio < 0) { showToast("Ingresa un precio válido", "warning"); return; }
    if (isNaN(desde) || isNaN(hasta) || desde < 1 || hasta < desde) { showToast("Rango de stands inválido", "warning"); return; }
    try {
        await axios.post(API_ESPACIOS_URL, {
            nombreZona, precio, desde, hasta, edicionId: _edicionIdActiva
        }, { withCredentials: true });
        document.getElementById("nuevo-espacio-nombre").value = "";
        document.getElementById("nuevo-espacio-precio").value = "";
        document.getElementById("nuevo-espacio-desde").value = "";
        document.getElementById("nuevo-espacio-hasta").value = "";
        showToast("Espacios agregados", "success");
        await cargarEspacios();
    } catch (err) {
        showToast(err.response?.data?.error || "Error al agregar espacios", "error");
    }
};

let _espacioEditandoId = null;

window.editarEspacio = (id, nombreOBtn, precioActual) => {
    _espacioEditandoId = id;
    // Si se pasa el elemento button (this.dataset.nombre), extraemos el nombre del dataset
    const nombreActual = (typeof nombreOBtn === 'object' && nombreOBtn !== null)
        ? (nombreOBtn.dataset?.nombre || '')
        : (nombreOBtn || '');
    const inputNombre = document.getElementById("input-nuevo-nombre-espacio");
    const inputPrecio = document.getElementById("input-nuevo-precio");
    if (inputNombre) inputNombre.value = nombreActual;
    if (inputPrecio) inputPrecio.value = precioActual;
    document.getElementById("modal-editar-precio").style.display = "block";
};

// Mantener compatibilidad con llamadas antiguas
window.editarPrecioEspacio = (id, precioActual) => {
    window.editarEspacio(id, '', precioActual);
};

window.guardarNuevoPrecio = async () => {
    const inputNombre = document.getElementById("input-nuevo-nombre-espacio");
    const nombre = inputNombre ? inputNombre.value.trim() : '';
    const precio = parseFloat(document.getElementById("input-nuevo-precio").value);
    if (isNaN(precio) || precio < 0) { showToast("Precio inválido", "warning"); return; }
    const payload = { precio };
    if (nombre) payload.nombre = nombre;
    try {
        await axios.put(`${API_ESPACIOS_URL}/${_espacioEditandoId}`, payload, { withCredentials: true });
        document.getElementById("modal-editar-precio").style.display = "none";
        _espacioEditandoId = null;
        showToast("Espacio actualizado", "success");
        await cargarEspacios();
    } catch (err) {
        showToast(err.response?.data?.error || "Error al actualizar", "error");
    }
};



window.actualizarPrecioZona = async () => {
    const nombreZona = document.getElementById("zona-actualizar-nombre").value.trim();
    const nuevoPrecio = parseFloat(document.getElementById("zona-actualizar-precio").value);
    if (!nombreZona) { showToast("Ingresa el nombre de la zona", "warning"); return; }
    if (isNaN(nuevoPrecio) || nuevoPrecio < 0) { showToast("Ingresa un precio válido", "warning"); return; }

    const encontrados = _espaciosCargados.filter(e => e.nombre && e.nombre.toLowerCase().includes(nombreZona.toLowerCase()) && e.estado === 'DISPONIBLE');

    if (!encontrados.length) {
        showToast(`No se encontraron lotes que coincidan con "${nombreZona}"`, "warning");
        return;
    }

    let checkboxesHtml = '<div style="text-align: left; max-height: 200px; overflow-y: auto; padding: 10px; border: 1px solid #e2e8f0; border-radius: 6px; margin-top: 10px;">';
    encontrados.forEach(e => {
        checkboxesHtml += `
            <label style="display: block; margin-bottom: 6px; font-size: 0.95em; cursor: pointer;">
                <input type="checkbox" class="swal-lote-checkbox" value="${e.id}" checked> ${e.nombre} ($${Number(e.precio).toFixed(2)})
            </label>`;
    });
    checkboxesHtml += '</div>';

    const result = await Swal.fire({
        title: 'Lotes encontrados. Desmarca las excepciones:',
        html: `<p style="font-size:0.9em; color:#64748b; margin-bottom:5px;">Nuevo precio a aplicar: <strong>$${nuevoPrecio}</strong></p>${checkboxesHtml}`,
        showCancelButton: true,
        confirmButtonText: 'Aplicar Cambio',
        cancelButtonText: 'Cancelar',
        confirmButtonColor: '#f59e0b',
        cancelButtonColor: '#94a3b8',
        preConfirm: () => {
            const checkedNodes = document.querySelectorAll('.swal-lote-checkbox:checked');
            const ids = Array.from(checkedNodes).map(chk => parseInt(chk.value, 10));
            if (ids.length === 0) {
                Swal.showValidationMessage('Debes seleccionar al menos un lote para actualizar');
                return false;
            }
            return ids;
        }
    });

    if (!result.isConfirmed || !result.value) return;

    try {
        const res = await axios.patch(`${API_ESPACIOS_URL}/actualizar-precio-lote`, {
            espaciosIds: result.value,
            nuevoPrecio: nuevoPrecio
        }, { withCredentials: true });

        document.getElementById("zona-actualizar-nombre").value = "";
        document.getElementById("zona-actualizar-precio").value = "";
        showToast(res.data.mensaje || "Precios actualizados con éxito", "success");
        await cargarEspacios();
    } catch (err) {
        showToast(err.response?.data?.error || "Error al actualizar la zona", "error");
    }
};

window.eliminarEspacio = async (id) => {
    const result = await Swal.fire({
        title: '¿Estás seguro?',
        text: "El lote pasará a estado ELIMINADO y no estará disponible para los feriantes.",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#475569',
        confirmButtonText: 'Sí, eliminar',
        cancelButtonText: 'Cancelar'
    });

    if (!result.isConfirmed) return;

    try {
        await axios.delete(`${API_ESPACIOS_URL}/${id}`, { withCredentials: true });
        showToast("Espacio eliminado", "success");
        await cargarEspacios();
    } catch (err) {
        showToast(err.response?.data?.error || "Error al eliminar", "error");
    }
};

window.enviarAMantenimiento = async (espacioId) => {
    const result = await Swal.fire({
        title: '🔧 Enviar a Mantenimiento',
        text: '¿Por qué se enviará este lote a mantenimiento?',
        input: 'textarea',
        inputPlaceholder: 'Ej: Daño estructural, limpieza programada...',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#ea580c',
        cancelButtonColor: '#94a3b8',
        confirmButtonText: 'Confirmar y Bloquear',
        cancelButtonText: 'Cancelar',
        inputValidator: (value) => {
            if (!value || value.trim() === '') {
                return '¡Debes ingresar un motivo válido!';
            }
        }
    });

    if (!result.isConfirmed) return;

    try {
        await axios.put(`${API_ESPACIOS_URL}/${espacioId}/estado`, {
            estado: 'MANTENIMIENTO',
            motivo: result.value.trim()
        }, { withCredentials: true });
        showToast(`Lote bloqueado: ${result.value}`, 'warning');
        await cargarEspacios();
    } catch (err) {
        showToast(err.response?.data?.error || 'Error al enviar a mantenimiento', 'error');
    }
};

window.crearLoteIndividual = async (edicionIdParam) => {
    const edicionId = edicionIdParam || _edicionIdActiva;
    if (!edicionId) {
        showToast("No hay una edición seleccionada", "warning");
        return;
    }

    const { value: formValues } = await Swal.fire({
        title: '➕ Nuevo Lote Individual',
        html: `
            <div style="text-align: left; margin-top: 10px;">
                <label style="display: block; font-weight: bold; margin-bottom: 5px; color: #334155;">Nombre del Lote</label>
                <input id="swal-lote-nombre" class="swal2-input" placeholder="Ej: Stand VIP" style="width: 100%; box-sizing: border-box; margin: 0 0 15px 0;">
                
                <label style="display: block; font-weight: bold; margin-bottom: 5px; color: #334155;">Precio ($)</label>
                <input id="swal-lote-precio" type="number" step="0.01" min="0" class="swal2-input" placeholder="Ej: 15000" style="width: 100%; box-sizing: border-box; margin: 0;">
            </div>
        `,
        focusConfirm: false,
        showCancelButton: true,
        confirmButtonText: 'Crear Lote',
        cancelButtonText: 'Cancelar',
        confirmButtonColor: '#10b981',
        cancelButtonColor: '#94a3b8',
        preConfirm: () => {
            const nombreInput = document.getElementById('swal-lote-nombre');
            const precioInput = document.getElementById('swal-lote-precio');

            const nombre = nombreInput ? nombreInput.value.trim() : '';
            const precioVal = precioInput ? precioInput.value.trim() : '';

            if (!nombre || !precioVal) {
                Swal.showValidationMessage('Ambos campos (Nombre y Precio) son obligatorios');
                return false;
            }

            const precio = parseFloat(precioVal);
            if (isNaN(precio) || precio < 0) {
                Swal.showValidationMessage('Ingresa un precio válido');
                return false;
            }

            return { nombre, precio };
        }
    });

    if (!formValues) return;

    try {
        await axios.post(API_ESPACIOS_URL, {
            nombre: formValues.nombre,
            precio: formValues.precio,
            edicionId: Number(edicionId)
        }, { withCredentials: true });

        showToast("Lote individual creado con éxito", "success");
        await cargarEspacios();
    } catch (err) {
        showToast(err.response?.data?.error || "Error al crear lote individual", "error");
    }
};