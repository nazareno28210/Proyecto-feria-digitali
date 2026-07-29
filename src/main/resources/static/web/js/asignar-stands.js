/*
 * ====================================
 * ASIGNAR-STANDS.JS (ACTUALIZADO CON FIX DE NOMBRES Y ESTADOS)
 * ====================================
 */

async function cargarEspaciosDisponibles(edicionId, espacioSeleccionadoId = null) {
    const selectUbicacion = document.getElementById("pago-ubicacion");
    selectUbicacion.innerHTML = '<option value="">Seleccione un stand (Opcional)...</option>';
    try {
        const res = await axios.get(`/api/espacios/edicion/${edicionId}`);
        const disponibles = res.data.filter(e => e.estado === 'DISPONIBLE' || e.id == espacioSeleccionadoId);
        if (disponibles.length === 0) {
            selectUbicacion.innerHTML = '<option value="">⚠️ No hay stands disponibles</option>';
            return;
        }
        disponibles.forEach(esp => {
            selectUbicacion.innerHTML += `<option value="${esp.id}">${esp.nombre} - $${esp.precio}</option>`;
        });
        if (espacioSeleccionadoId) selectUbicacion.value = espacioSeleccionadoId;
    } catch (error) {
        console.error("Error al cargar espacios:", error);
        selectUbicacion.innerHTML = '<option value="">Error al cargar stands</option>';
    }
}

document.addEventListener("DOMContentLoaded", () => {
    // Referencias al DOM
    const feriaSelect = document.getElementById("feria-select");
    const gestionContainer = document.getElementById("gestion-stands");

    // Referencias a los 3 cuerpos de tabla
    const tbodyPendientes = document.querySelector("#tabla-pendientes tbody");
    const tbodyGestion = document.querySelector("#tabla-gestion tbody");

    const modalPago = document.getElementById("modal-pago");
    const formPago = document.getElementById("form-pago");
    const inputMonto = document.getElementById("pago-monto");
    const selectEstado = document.getElementById("pago-estado");

    // ========================================================
    // INICIALIZACIÓN
    // ========================================================

    async function init() {
        await cargarFerias();
        feriaSelect.addEventListener("change", cargarParticipantes);
    }

    // 🟢 1. TRAEMOS LAS EDICIONES Y LAS AGRUPAMOS
    async function cargarFerias() {
        try {
            const res = await axios.get("/api/ediciones"); 
            feriaSelect.innerHTML = '<option value="">Selecciona una edición...</option>';
            
            // Filtramos las eliminadas para que ni aparezcan
            const validas = res.data.filter(e => e.estado !== 'ELIMINADO');
            
            // Separamos las activas de las inactivas (historial)
            const activas = validas.filter(e => e.estado === 'ACTIVA');
            const inactivas = validas.filter(e => e.estado !== 'ACTIVA');

            // Renderizamos el grupo de Activas primero
            if (activas.length > 0) {
                const groupActivas = document.createElement('optgroup');
                groupActivas.label = "🟢 EDICIONES ACTIVAS (Moderar hoy)";
                activas.forEach(edicion => {
                    const nombreCompleto = `${edicion.feriaNombre} - ${edicion.nombreEdicion}`;
                    groupActivas.innerHTML += `<option value="${edicion.id}">${nombreCompleto}</option>`;
                });
                feriaSelect.appendChild(groupActivas);
            }

            // Renderizamos el grupo de Historial abajo
            if (inactivas.length > 0) {
                const groupInactivas = document.createElement('optgroup');
                groupInactivas.label = "🔴 HISTORIAL (Ediciones cerradas)";
                inactivas.forEach(edicion => {
                    const nombreCompleto = `${edicion.feriaNombre} - ${edicion.nombreEdicion}`;
                    groupInactivas.innerHTML += `<option value="${edicion.id}">${nombreCompleto}</option>`;
                });
                feriaSelect.appendChild(groupInactivas);
            }

        } catch (error) {
            showToast("Error al cargar las ediciones", "error");
        }
    }

    // ========================================================
    // LÓGICA DE FILTRADO Y RENDERIZADO
    // ========================================================

    async function cargarParticipantes() {
        const edicionId = feriaSelect.value;
        if (!edicionId) {
            gestionContainer.style.display = "none";
            return;
        }

        try {
            const res = await axios.get(`/api/participaciones/edicion/${edicionId}`);
            
            const participaciones = res.data.filter(p => p.estado !== 'CANCELADO');
            
            const pendientes = participaciones.filter(p => p.estado === 'PENDIENTE');
            const confirmados = participaciones.filter(p => p.estado === 'CONFIRMADO');

            renderPendientes(pendientes);
            renderGestion(confirmados);

            gestionContainer.style.display = "block";
        } catch (error) {
            showToast("Error al cargar participantes", "error");
        }
    }

// 🟢 3. BUSCADOR ROBUSTO DE NOMBRES (Corregido para leer String)
    function obtenerNombreStand(p) {
        // Tu DTO manda la variable "stand" como un String directo con el nombre
        if (p.stand && typeof p.stand === 'string') {
            return p.stand;
        }

        // Mantenemos estas opciones extra como red de seguridad
        if (p.nombreEmprendimiento) return p.nombreEmprendimiento;
        if (p.emprendimiento && p.emprendimiento.nombre) return p.emprendimiento.nombre;
        if (p.stand && p.stand.nombre) return p.stand.nombre;
        
        return "Emprendimiento sin nombre";
    }

    function renderPendientes(lista) {
        tbodyPendientes.innerHTML = "";
        if (lista.length === 0) {
            tbodyPendientes.innerHTML = "<tr><td colspan='3' style='text-align:center;'>No hay solicitudes pendientes.</td></tr>";
            return;
        }

        lista.forEach(p => {
            const nombreStand = obtenerNombreStand(p);
            tbodyPendientes.innerHTML += `
                <tr>
                    <td><strong>${nombreStand}</strong></td>
                    <td><span class="badge-debe">Pendiente</span></td>
                    <td>
                        <button class="btn-aceptar" onclick="cambiarEstadoAsistencia(${p.id}, 'CONFIRMADO')"><i class="fas fa-check"></i> Aceptar</button>
                        <button class="btn-rechazar" onclick="cambiarEstadoAsistencia(${p.id}, 'RECHAZADO')"><i class="fas fa-times"></i> Rechazar</button>
                    </td>
                </tr>
            `;
        });
    }

    function renderGestion(lista) {
        let totalRecaudado = 0;
        let totalPorCobrar = 0;
        let standsOcupados = 0;

        tbodyGestion.innerHTML = lista.length === 0 ? "<tr><td colspan='5' style='text-align:center;'>No hay feriantes confirmados.</td></tr>" : "";

        lista.forEach(p => {
            const monto = p.montoAbonado || 0;
            totalRecaudado += monto;

            // Detecta si tiene stand sin importar cómo venga estructurado el JSON
            const tieneStand = p.espacioId || p.espacioNombre || p.numeroStand || (p.espacio && p.espacio.id);

            if (tieneStand) {
                standsOcupados++;

                // Busca el precio del stand en el JSON
                let precioStand = 0;
                if (p.espacio && p.espacio.precio) {
                    precioStand = p.espacio.precio;
                } else if (p.espacioPrecio) {
                    precioStand = p.espacioPrecio;
                } else if (p.precio) {
                    precioStand = p.precio; // Fallback por si el backend lo manda directo
                }

                if (precioStand > 0) {
                    let deuda = precioStand - monto;
                    if (deuda > 0) totalPorCobrar += deuda;
                }
            }
            const nombreStand = obtenerNombreStand(p);

            // 1. Lógica de Estados
            const estadoDB = p.estadoPago ? p.estadoPago.toUpperCase() : "DEBE";
            let estadoMostrar = "Debe";
            let badgeClass = "badge-debe";

            if (estadoDB === "PAGADO") {
                estadoMostrar = "Pagado";
                badgeClass = "badge-pagado";
            } else if (estadoDB === "SENADO") {
                estadoMostrar = "Señado";
                badgeClass = "badge-senado";
            }

            // 2. Lógica de Ubicación
            let ubicacionTexto = `<span style="color:#f59e0b;">Sin asignar</span>`;
            if (p.espacio && p.espacio.nombre) {
                ubicacionTexto = p.espacio.nombre;
            } else if (p.espacioNombre) {
                ubicacionTexto = p.espacioNombre;
            } else if (p.numeroStand) {
                ubicacionTexto = `Mesa ${p.numeroStand}`;
            }

            // 3. Lógica de Preferencia
            const sugerencia = p.numeroStandPreferido ?
                `<span style="background: #e0e7ff; color: #4338ca; padding: 4px 8px; border-radius: 12px; font-size: 0.85em; font-weight: 500;">Mesa ${p.numeroStandPreferido}</span>` :
                `<small style="color:gray;">Sin preferencia</small>`;

            // 4. Renderizado de Fila
            tbodyGestion.innerHTML += `
                <tr>
                    <td><strong>${nombreStand}</strong></td>
                    <td><span class="${badgeClass}">${estadoMostrar} ($${p.montoAbonado || 0})</span></td>
                    <td>${sugerencia}</td>
                    <td>${ubicacionTexto}</td>
                    <td>
                        <button class="btn-cobrar" onclick="abrirModalPago(${p.id}, '${estadoDB}', ${p.montoAbonado || 0}, ${p.espacioId || 'null'})">
                            <i class="fas fa-edit"></i> Gestionar
                        </button>
                        <button class="btn-rechazar" onclick="quitarDeDistribucion(${p.id}, ${p.montoAbonado || 0})">
                            <i class="fas fa-undo"></i> Quitar
                        </button>
                    </td>
                </tr>
            `;
        });

        document.getElementById("dash-recaudado").innerText = `$${totalRecaudado}`;
        document.getElementById("dash-por-cobrar").innerText = `$${totalPorCobrar}`;
        document.getElementById("dash-ocupacion").innerText = `${standsOcupados}`;
    }

    // ========================================================
    // ACCIONES GLOBALES
    // ========================================================

    window.cambiarEstadoAsistencia = async (participacionId, nuevoEstado) => {
        let motivo = "";

        if (nuevoEstado === 'RECHAZADO' || nuevoEstado === 'CANCELADO') {
            const result = await Swal.fire({
                title: 'Motivo requerido',
                text: 'Ingresa el motivo que verá el feriante en su perfil:',
                input: 'textarea',
                inputPlaceholder: 'Escribe el motivo aquí...',
                icon: 'warning',
                showCancelButton: true,
                confirmButtonColor: '#e74c3c',
                cancelButtonColor: '#95a5a6',
                confirmButtonText: 'Confirmar y Enviar',
                cancelButtonText: 'Cancelar',
                inputValidator: (value) => {
                    if (!value || value.trim() === "") {
                        return '¡Debes ingresar un motivo válido!';
                    }
                }
            });

            if (!result.isConfirmed) {
                return showToast("Operación cancelada.", "warning");
            }

            motivo = result.value;
        }

        try {
            const url = `/api/participaciones/${participacionId}/estado-asistencia?estado=${nuevoEstado}&motivo=${encodeURIComponent(motivo)}`;

            await axios.patch(url, {}, { withCredentials: true });

            showToast(`Estado actualizado a ${nuevoEstado}`, "success");

            cargarParticipantes();
        } catch (error) {
            console.error("Error al cambiar estado:", error);
            const msg = error.response?.data?.error || "Error al procesar la solicitud";
            showToast(msg, "error");
        }
    };

    window.cambiarTab = (tabName) => {
        document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
        document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));

        document.getElementById(`btn-tab-${tabName}`).classList.add('active');
        document.getElementById(`tab-${tabName}`).classList.add('active');
    };

    window.abrirModalPago = async (id, estadoPago, monto, ubicacionId) => {
        document.getElementById("pago-participacion-id").value = id;
        
        const estadoSeguro = estadoPago ? estadoPago.toUpperCase() : "DEBE";
        document.getElementById("pago-estado").value = estadoSeguro; 
        document.getElementById("pago-estado").disabled = true;
        
        document.getElementById("pago-monto").value = monto;

        document.getElementById("grupo-ubicacion").style.display = "block";
        const edicionId = document.getElementById("feria-select").value;
        cargarEspaciosDisponibles(edicionId, ubicacionId);

        // Cargar mapa de la edición
        const mapaContainer = document.getElementById("mapa-asignacion-container");
        const mapaImg = document.getElementById("mapa-asignacion-img");
        if (mapaContainer && mapaImg) {
            try {
                const resEdicion = await axios.get(`/api/ediciones/${edicionId}`);
                const mapaUrl = resEdicion.data.mapaUrl || resEdicion.data.mapa_url;
                if (mapaUrl) {
                    mapaImg.src = mapaUrl;
                    mapaContainer.style.display = "block";
                } else {
                    mapaContainer.style.display = "none";
                }
            } catch (e) {
                mapaContainer.style.display = "none";
            }
        }

        const helpText = document.getElementById("ayuda-preferencia");
        if (helpText) helpText.innerHTML = "";

        modalPago.style.display = "block";
    };

    window.aplicarPreferencia = (num) => {
        const inputUbicacion = document.getElementById("pago-ubicacion");
        if (inputUbicacion) {
            inputUbicacion.value = num;
            showToast(`Se aplicó la sugerencia: Mesa ${num}`, "info");
        }
    };

    window.quitarDeDistribucion = async (id, montoAbonado) => {
        // Freno preventivo en el frontend
        if (montoAbonado > 0) {
            showToast(`Bloqueo Contable: El feriante tiene un saldo a favor de $${montoAbonado}. Ingresa a 'Gestionar' y deja el monto en $0 para devolver el dinero antes de quitarlo.`, "error");
            return;
        }

        await window.cambiarEstadoAsistencia(id, 'CANCELADO');
    };

    window.cerrarModalPago = () => {
        document.getElementById("pago-estado").disabled = false;
        modalPago.style.display = "none";
    };

    formPago.addEventListener("submit", async (e) => {
        e.preventDefault();

        const id = document.getElementById("pago-participacion-id").value;
        const monto = parseFloat(document.getElementById("pago-monto").value) || 0;
        const ubicacionValue = document.getElementById("pago-ubicacion").value.trim();

        if (monto < 0) {
            return showToast("El monto no puede ser negativo.", "error");
        }

        // Parseo seguro para el backend (Integer)
        const espacioId = ubicacionValue !== "" ? parseInt(ubicacionValue, 10) : null;

        const payload = {
            montoAbonado: monto,
            espacioId: espacioId
        };

        try {
            await axios.patch(`/api/participaciones/${id}/pago`, payload);
            showToast("Datos actualizados correctamente", "success");
            cerrarModalPago();
            cargarParticipantes();
        } catch (error) {
            const mensajeError = error.response?.data?.error || "Error al guardar cambios";
            showToast(mensajeError, "error");
        }
    });

    function showToast(message, type = "info") {
        let color = type === "success" ? "#10b981" : (type === "warning" ? "#f59e0b" : "#ef4444");
        Toastify({
            text: message,
            duration: 3000,
            gravity: "top",
            position: "right",
            style: { background: color },
        }).showToast();
    }

    init();
});