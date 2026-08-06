/*
 * ====================================
 * ASIGNAR-STANDS.JS (ACTUALIZADO CON FIX DE NOMBRES Y ESTADOS)
 * ====================================
 */

// Cache de espacios disponibles por edicion (Bug #1: para resolver nombres de preferencia)
let _espaciosCache = [];

async function cargarEspaciosDisponibles(edicionId, espacioSeleccionadoId = null) {
    const selectUbicacion = document.getElementById("pago-ubicacion");
    selectUbicacion.innerHTML = '<option value="">Seleccione un stand (Opcional)...</option>';
    try {
        const res = await axios.get(`/api/espacios/edicion/${edicionId}`);
        const espacios = res.data.filter(e => e.estado !== 'ELIMINADO');
        _espaciosCache = espacios; // guardar para resolver nombres de preferencia
        if (espacios.length === 0) {
            selectUbicacion.innerHTML = '<option value="">⚠️ No hay stands configurados</option>';
            return;
        }
        espacios.forEach(esp => {
            const esDisponible = esp.estado === 'DISPONIBLE';
            const esActual = esp.id == espacioSeleccionadoId;
            // Ignorar stands que no están disponibles y no son el stand actual del usuario
            if (!esDisponible && !esActual) return;
            const estadoMayusc = esp.estado ? esp.estado.toUpperCase() : 'DISPONIBLE';
            selectUbicacion.innerHTML += `<option value="${esp.id}">${esp.nombre} $${esp.precio} (${estadoMayusc})</option>`;
        });
        if (espacioSeleccionadoId) selectUbicacion.value = espacioSeleccionadoId;
    } catch (error) {
        console.error("Error al cargar espacios:", error);
        selectUbicacion.innerHTML = '<option value="">Error al cargar stands</option>';
    }
}

// Bug #1: Resuelve el nombre del espacio preferido a partir del ID
function resolverNombrePreferido(numeroStandPreferido, espacios) {
    if (!numeroStandPreferido) return null;
    // Buscar en el cache de espacios disponibles
    const encontrado = espacios.find(e => e.id == numeroStandPreferido);
    return encontrado ? encontrado.nombre : `ID #${numeroStandPreferido}`;
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

            const activas  = validas.filter(e => e.estado === 'ACTIVA');
            const proximas  = validas.filter(e => e.estado === 'PROXIMA');
            const inactivas = validas.filter(e => e.estado !== 'ACTIVA' && e.estado !== 'PROXIMA');

            // 1. Activas
            if (activas.length > 0) {
                const groupActivas = document.createElement('optgroup');
                groupActivas.label = "🟢 EDICIONES ACTIVAS (Moderar hoy)";
                activas.forEach(edicion => {
                    const nombreCompleto = `${edicion.feriaNombre} - ${edicion.nombreEdicion}`;
                    groupActivas.innerHTML += `<option value="${edicion.id}">${nombreCompleto}</option>`;
                });
                feriaSelect.appendChild(groupActivas);
            }

            // 2. Próximas
            if (proximas.length > 0) {
                const groupProximas = document.createElement('optgroup');
                groupProximas.label = "🔵 EDICIONES PRÓXIMAS (Por iniciar)";
                proximas.forEach(edicion => {
                    const nombreCompleto = `${edicion.feriaNombre} - ${edicion.nombreEdicion}`;
                    groupProximas.innerHTML += `<option value="${edicion.id}">${nombreCompleto}</option>`;
                });
                feriaSelect.appendChild(groupProximas);
            }

            // 3. Historial
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
            mostrarNotificacion("Error al cargar las ediciones", "error");
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
            // Bug #1: Cargamos espacios primero para tener el cache de nombres
            try {
                const resEsp = await axios.get(`/api/espacios/edicion/${edicionId}`);
                _espaciosCache = resEsp.data.filter(e => e.estado !== 'ELIMINADO');
            } catch (e) {
                _espaciosCache = [];
            }

            const res = await axios.get(`/api/participaciones/edicion/${edicionId}`);
            
            const participaciones = res.data.filter(p => p.estado !== 'CANCELADO');
            
            const pendientes = participaciones.filter(p => p.estado === 'PENDIENTE');
            const confirmados = participaciones.filter(p => p.estado === 'CONFIRMADO');
            const enEspera = participaciones.filter(p => p.estado === 'EN_ESPERA').sort((a, b) => a.id - b.id);

            renderPendientes(pendientes);
            renderGestion(confirmados);
            renderEspera(enEspera);

            // Badge del tab de espera
            const badge = document.getElementById('badge-espera-tab');
            if (badge) {
                badge.textContent = enEspera.length;
                badge.style.display = enEspera.length > 0 ? 'flex' : 'none';
            }

            gestionContainer.style.display = "block";
        } catch (error) {
            mostrarNotificacion("Error al cargar participantes", "error");
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
                        <button type="button" class="btn-aceptar" onclick="cambiarEstadoAsistencia(event, ${p.id}, 'CONFIRMADO')"><i class="fas fa-check"></i> Aceptar</button>
                        <button type="button" class="btn-rechazar" onclick="cambiarEstadoAsistencia(event, ${p.id}, 'RECHAZADO')"><i class="fas fa-times"></i> Rechazar</button>
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

            // Detecta si tiene stand REAL asignado (solo espacioId/espacioNombre del DTO, nunca la preferencia)
            const tieneStand = !!(p.espacioId || p.espacioNombre);

            if (tieneStand) {
                standsOcupados++;
            }

            // Bug #4: Siempre calculamos Por Cobrar dinámicamente (incluye SENADO)
            let precioStand = 0;
            if (p.espacio && p.espacio.precio) {
                precioStand = p.espacio.precio;
            } else if (p.espacioPrecio) {
                precioStand = p.espacioPrecio;
            } else if (p.precio) {
                precioStand = p.precio;
            }
            if (precioStand > 0) {
                let deuda = precioStand - monto;
                if (deuda > 0) totalPorCobrar += deuda;
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

            // 2. Lógica de Ubicación con Soft-Booking visual
            let ubicacionTexto = `<span style="color:#94a3b8; font-style:italic;">Sin asignar</span>`;
            if (p.espacioNombre) {
                if (estadoDB === 'DEBE') {
                    ubicacionTexto = `<span style="color:#64748b; font-style:italic;">${p.espacioNombre} (Pre-asignado)</span>`;
                } else {
                    ubicacionTexto = `<span style="font-weight:bold; color:#15803d;">${p.espacioNombre}</span>`;
                }
            }

            // Bug #1: Lógica de Preferencia — resuelve nombre real del espacio preferido
            let nombrePreferido = null;
            if (p.numeroStandPreferido) {
                nombrePreferido = resolverNombrePreferido(p.numeroStandPreferido, _espaciosCache);
            }
            const sugerencia = nombrePreferido
                ? `<span style="background: #e0e7ff; color: #4338ca; padding: 4px 8px; border-radius: 12px; font-size: 0.85em; font-weight: 500;">${nombrePreferido}</span>`
                : `<small style="color:gray;">Sin preferencia</small>`;

            // 4. Renderizado de Fila — Bug #5: data-attributes en lugar de onclick inline con parámetros complejos
            tbodyGestion.innerHTML += `
                <tr>
                    <td><strong>${nombreStand}</strong></td>
                    <td><span class="${badgeClass}">${estadoMostrar} ($${monto})</span></td>
                    <td>${sugerencia}</td>
                    <td>${ubicacionTexto}</td>
                    <td>
                        <button type="button" class="btn-cobrar btn-gestionar"
                            data-id="${p.id}" data-estado="${estadoDB}" data-monto="${monto}" data-espacio-id="${p.espacioId || ''}"
                        ><i class="fas fa-edit"></i> Gestionar</button>
                        <button type="button" class="btn-rechazar btn-quitar"
                            data-id="${p.id}" data-monto="${monto}"
                        ><i class="fas fa-undo"></i> Quitar</button>
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

    window.cambiarEstadoAsistencia = async (event, participacionIdParam, nuevoEstadoParam) => {
        if (event && event.preventDefault) event.preventDefault();
        const participacionId = typeof event === 'number' ? event : participacionIdParam;
        const nuevoEstado = typeof event === 'number' ? participacionIdParam : nuevoEstadoParam;
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
                return mostrarNotificacion("Operación cancelada.", "warning");
            }

            motivo = result.value;
        }

        try {
            const url = `/api/participaciones/${participacionId}/estado-asistencia?estado=${nuevoEstado}&motivo=${encodeURIComponent(motivo)}`;

            await axios.patch(url, {}, { withCredentials: true });

            mostrarNotificacion(`Estado actualizado a ${nuevoEstado}`, "success");

            cargarParticipantes();
        } catch (error) {
            console.error("Error al cambiar estado:", error);
            mostrarNotificacion(obtenerMensajeError(error, "Error al procesar la solicitud"), "error");
        }
    };

    window.cambiarTab = (tabName) => {
        document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
        document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));

        const btn = document.getElementById(`btn-tab-${tabName}`);
        const tab = document.getElementById(`tab-${tabName}`);
        if (btn) btn.classList.add('active');
        if (tab) tab.classList.add('active');
    };

    // Bug #5: Delegación de eventos desde el tbody estático para botones dinámicos
    tbodyGestion.addEventListener('click', function(e) {
        // Botón Gestionar
        const btnGestionar = e.target.closest('.btn-gestionar');
        if (btnGestionar) {
            e.preventDefault();
            const id = parseInt(btnGestionar.dataset.id);
            const estado = btnGestionar.dataset.estado;
            const monto = parseFloat(btnGestionar.dataset.monto) || 0;
            const espacioId = btnGestionar.dataset.espacioId || null;
            window.abrirModalPago(id, estado, monto, espacioId);
            return;
        }
        // Botón Quitar
        const btnQuitar = e.target.closest('.btn-quitar');
        if (btnQuitar) {
            e.preventDefault();
            const id = parseInt(btnQuitar.dataset.id);
            const monto = parseFloat(btnQuitar.dataset.monto) || 0;
            window.quitarDeDistribucion(null, id, monto);
            return;
        }
    });

    // Bug #3: abrirModalPago acepta parámetros directos (sin event), llamado desde delegación de eventos
    window.abrirModalPago = async (id, estadoPago, monto, ubicacionId) => {
        document.getElementById("pago-participacion-id").value = id;
        
        const estadoSeguro = estadoPago ? estadoPago.toUpperCase() : "DEBE";
        const selectEstado = document.getElementById("pago-estado");
        selectEstado.value = estadoSeguro;
        selectEstado.disabled = true;
        document.getElementById("pago-monto").value = monto || 0;

        // Cargar espacios disponibles (actualiza _espaciosCache)
        const edicionId = document.getElementById("feria-select").value;
        await cargarEspaciosDisponibles(edicionId, (ubicacionId && ubicacionId !== 'null') ? ubicacionId : null);

        // Poblar el select con _espaciosCache (incluyendo el espacio ya asignado aunque esté OCUPADO)
        const selectUbicacion = document.getElementById("pago-ubicacion");
        selectUbicacion.innerHTML = '<option value="">-- Sin asignar --</option>';
        const espacioAsignadoId = (ubicacionId && ubicacionId !== 'null' && ubicacionId !== '') ? String(ubicacionId) : '';
        _espaciosCache.forEach(esp => {
            const selected = String(esp.id) === espacioAsignadoId ? 'selected' : '';
            selectUbicacion.innerHTML += `<option value="${esp.id}" ${selected}>${esp.nombre} — $${esp.precio} (${esp.estado})</option>`;
        });
        // Si el espacio asignado no está en el cache (ej: OCUPADO por otro), igual lo mostramos
        if (espacioAsignadoId && !_espaciosCache.find(e => String(e.id) === espacioAsignadoId)) {
            selectUbicacion.innerHTML += `<option value="${espacioAsignadoId}" selected>Stand actual (ID ${espacioAsignadoId})</option>`;
        }

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
            mostrarNotificacion(`Se aplicó la sugerencia: Mesa ${num}`, "info");
        }
    };

    window.quitarDeDistribucion = async (event, idParam, montoAbonadoParam) => {
        if (event && event.preventDefault) event.preventDefault();
        const id = typeof event === 'number' ? event : idParam;
        const montoAbonado = typeof event === 'number' ? idParam : montoAbonadoParam;
        if (montoAbonado > 0) {
            mostrarNotificacion(`Bloqueo Contable: El feriante tiene un saldo a favor de $${montoAbonado}. Ingresa a 'Gestionar' y deja el monto en $0 para devolver el dinero antes de quitarlo.`, "error");
            return;
        }

        await window.cambiarEstadoAsistencia(event, id, 'CANCELADO');
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
            return mostrarNotificacion("El monto no puede ser negativo.", "error");
        }

        // Validación: si hay stand seleccionado, el monto debe ser > 0
        if (ubicacionValue !== "" && monto <= 0) {
            await Swal.fire({
                title: "Monto requerido",
                text: "Debes ingresar un monto mayor a $0 para confirmar un stand.",
                icon: "error",
                confirmButtonColor: "#ef4444"
            });
            return;
        }

        // Bug #4: Validar sobrepago contra el precio del stand seleccionado
        if (ubicacionValue !== "" && monto > 0) {
            const espacioSeleccionado = _espaciosCache.find(e => String(e.id) === ubicacionValue);
            if (espacioSeleccionado && monto > espacioSeleccionado.precio) {
                await Swal.fire({
                    title: "Monto inválido",
                    text: `El monto ($${monto}) no puede superar el valor del stand ($${espacioSeleccionado.precio}).`,
                    icon: "error",
                    confirmButtonColor: "#ef4444"
                });
                return;
            }
        }

        // Construir payload: espacioId es null si no se seleccionó ninguno
        const payload = {
            montoAbonado: monto,
            espacioId: ubicacionValue !== "" ? parseInt(ubicacionValue, 10) : null
        };

        try {
            await axios.patch(`/api/participaciones/${id}/pago`, payload);
            mostrarNotificacion("Datos actualizados correctamente", "success");
            cerrarModalPago();
            cargarParticipantes();
        } catch (error) {
            mostrarNotificacion(obtenerMensajeError(error, "Error al guardar cambios"), "error");
        }
    });

    // ============================================================
    // LISTA DE ESPERA — Tab dedicado
    // ============================================================

    function renderEspera(lista) {
        const tbody = document.getElementById('tbody-espera');
        if (!tbody) return;

        if (lista.length === 0) {
            tbody.innerHTML = `<tr><td colspan='3' style='text-align:center; padding:20px; color:#94a3b8;'>🎉 No hay feriantes en lista de espera.</td></tr>`;
            return;
        }

        tbody.innerHTML = lista.map((p, i) => {
            const nombre = obtenerNombreStand(p);
            return `<tr>
                <td style="font-weight:700; color:#f59e0b;">#${i + 1}</td>
                <td><strong>${nombre}</strong></td>
                <td>
                    <button type="button" class="btn-aceptar" onclick="aprobarDesdeEspera(event, ${p.id})" style="background:linear-gradient(135deg,#f59e0b,#d97706);">
                        <i class="fas fa-arrow-up"></i> Aprobar → Pendiente
                    </button>
                </td>
            </tr>`;
        }).join('');
    }

    window.aprobarDesdeEspera = async (event, participacionIdParam) => {
        if (event && event.preventDefault) event.preventDefault();
        const participacionId = typeof event === 'number' ? event : participacionIdParam;
        const result = await Swal.fire({
            title: '¿Aprobar este feriante?',
            text: 'Pasará de Lista de Espera a PENDIENTE y aparecerá en Solicitudes para asignarle un lote.',
            icon: 'question',
            showCancelButton: true,
            confirmButtonColor: '#f59e0b',
            cancelButtonColor: '#94a3b8',
            confirmButtonText: 'Sí, aprobar',
            cancelButtonText: 'Cancelar',
        });

        if (!result.isConfirmed) return;

        try {
            await axios.patch(`/api/participaciones/${participacionId}/estado-asistencia?estado=PENDIENTE`, {}, { withCredentials: true });
            mostrarNotificacion('¡Feriante aprobado! Ahora está en Solicitudes.', 'success');
            await cargarParticipantes();
        } catch (err) {
            const errorMsg = obtenerMensajeError(err, 'Error al aprobar el feriante.');
            if (err.response && err.response.status === 400) {
                Swal.fire({
                    title: 'No se puede aprobar',
                    text: errorMsg,
                    icon: 'error',
                    confirmButtonColor: '#ef4444'
                });
            } else {
                mostrarNotificacion(errorMsg, 'error');
            }
        }
    };

    init();
});