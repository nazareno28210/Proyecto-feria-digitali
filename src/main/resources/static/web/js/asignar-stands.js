/*
 * ====================================
 * ASIGNAR-STANDS.JS (ACTUALIZADO CON FIX DE NOMBRES Y ESTADOS)
 * ====================================
 */

document.addEventListener("DOMContentLoaded", () => {
    // Referencias al DOM
    const feriaSelect = document.getElementById("feria-select");
    const gestionContainer = document.getElementById("gestion-stands");

    // Referencias a los 3 cuerpos de tabla
    const tbodyPendientes = document.querySelector("#tabla-pendientes tbody");
    const tbodyCobros = document.querySelector("#tabla-cobros tbody");
    const tbodyDistribucion = document.querySelector("#tabla-distribucion tbody");

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
            const paraCobrar = participaciones.filter(p => p.estado === 'CONFIRMADO' && p.estadoPago === 'DEBE');
            const paraDistribuir = participaciones.filter(p => p.estado === 'CONFIRMADO' && p.estadoPago !== 'DEBE');

            renderPendientes(pendientes);
            renderCobros(paraCobrar);
            renderDistribucion(paraDistribuir);

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
                        <button class="btn-rechazar" onclick="cambiarEstadoAsistencia(${p.id}, 'CANCELADO')"><i class="fas fa-times"></i> Rechazar</button>
                    </td>
                </tr>
            `;
        });
    }

    function renderCobros(lista) {
        tbodyCobros.innerHTML = "";
        if (lista.length === 0) {
            tbodyCobros.innerHTML = "<tr><td colspan='3' style='text-align:center;'>No hay cobros pendientes.</td></tr>";
            return;
        }

        lista.forEach(p => {
            const nombreStand = obtenerNombreStand(p);
            tbodyCobros.innerHTML += `
                <tr>
                    <td><strong>${nombreStand}</strong></td>
                    <td><span class="badge-debe">Debe Pago</span></td>
                    <td>
                        <button class="btn-cobrar" onclick="abrirModalPago(${p.id}, '${p.estadoPago}', ${p.montoAbonado || 0}, '${p.numeroStand || ''}')">
                            <i class="fas fa-dollar-sign"></i> Registrar Pago
                        </button>
                    </td>
                </tr>
            `;
        });
    }

    function renderDistribucion(lista) {
        tbodyDistribucion.innerHTML = lista.length === 0 ?
            "<tr><td colspan='5' style='text-align:center;'>Nadie listo para ubicar.</td></tr>" : "";

        lista.forEach(p => {
            const nombreStand = obtenerNombreStand(p);
            
            // Protección contra mayúsculas/minúsculas del backend
            const estadoStr = p.estadoPago ? p.estadoPago.toUpperCase() : "DEBE";
            
            let badgeClass = estadoStr === "SENADO" ? "badge-senado" : "badge-pagado";
            let textoPago = estadoStr === "SENADO" ? "Señado" : "Pagado";
            
            const ubicacionTexto = p.numeroStand ? `Mesa ${p.numeroStand}` : `<span style="color:#f59e0b;">Sin asignar</span>`;
            
            const sugerencia = p.numeroStandPreferido ? 
                `<span class="badge-preferencia">Mesa ${p.numeroStandPreferido}</span>` : 
                `<small style="color:gray;">Sin preferencia</small>`;

            tbodyDistribucion.innerHTML += `
                <tr>
                    <td><strong>${nombreStand}</strong></td>
                    <td><span class="${badgeClass}">${textoPago} ($${p.montoAbonado})</span></td>
                    <td>${sugerencia}</td> 
                    <td>${ubicacionTexto}</td>
                    <td>
                        <button class="btn-cobrar" onclick="abrirModalPago(${p.id}, '${estadoStr}', ${p.montoAbonado || 0}, '${p.numeroStand || ''}', true, ${p.numeroStandPreferido})">
                            <i class="fas fa-map-marker-alt"></i> Ubicar
                        </button>
                        <button class="btn-rechazar" onclick="quitarDeDistribucion(${p.id})">
                            <i class="fas fa-undo"></i> Quitar
                        </button>
                    </td>
                </tr>
            `;
        });
    }

    // ========================================================
    // ACCIONES GLOBALES
    // ========================================================

    window.cambiarEstadoAsistencia = async (participacionId, nuevoEstado) => {
        try {
            await axios.patch(`/api/participaciones/${participacionId}/estado-asistencia?estado=${nuevoEstado}`);
            showToast("Estado de solicitud actualizado", "success");
            cargarParticipantes();
        } catch (error) {
            const msg = error.response?.data?.error || "Error al actualizar estado";
            showToast(msg, "error");
        }
    };

    window.cambiarTab = (tabName) => {
        document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
        document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));

        document.getElementById(`btn-tab-${tabName}`).classList.add('active');
        document.getElementById(`tab-${tabName}`).classList.add('active');
    };

    window.abrirModalPago = (id, estadoPago, monto, ubicacion, esDistribucion = false, preferencia = null) => {
        document.getElementById("pago-participacion-id").value = id;
        
        // Forzamos la asignación correcta del estado
        const estadoSeguro = estadoPago ? estadoPago.toUpperCase() : "DEBE";
        document.getElementById("pago-estado").value = estadoSeguro; 
        
        document.getElementById("pago-monto").value = monto;
        document.getElementById("pago-ubicacion").value = ubicacion;

        const grupoUbicacion = document.getElementById("grupo-ubicacion"); 
        grupoUbicacion.style.display = esDistribucion ? "block" : "none";

        const helpText = document.getElementById("ayuda-preferencia");
        if (helpText) {
            if (preferencia && preferencia !== "null" && preferencia !== "undefined") {
                helpText.innerHTML = `Sugerido por feriante: <strong>Mesa ${preferencia}</strong> 
                    <a href="#" onclick="aplicarPreferencia(${preferencia}); return false;" style="margin-left:10px; color:#3b82f6;">[Usar esta]</a>`;
            } else {
                helpText.innerHTML = "";
            }
        }

        modalPago.style.display = "block";
    };

    window.aplicarPreferencia = (num) => {
        const inputUbicacion = document.getElementById("pago-ubicacion");
        if (inputUbicacion) {
            inputUbicacion.value = num;
            showToast(`Se aplicó la sugerencia: Mesa ${num}`, "info");
        }
    };

    window.quitarDeDistribucion = async (id) => {
        if(!confirm("¿Estás seguro de quitar a este feriante de la feria? Volverá a estar disponible para postularse.")) return;
        try {
            await axios.patch(`/api/participaciones/${id}/estado-asistencia?estado=CANCELADO`);
            showToast("Feriante quitado de la feria", "info");
            cargarParticipantes();
        } catch (error) {
            showToast("Error al quitar feriante", "error");
        }
    }

    window.cerrarModalPago = () => { modalPago.style.display = "none"; };

    formPago.addEventListener("submit", async (e) => {
        e.preventDefault();

        const id = document.getElementById("pago-participacion-id").value;
        const monto = parseFloat(document.getElementById("pago-monto").value) || 0;
        const estado = document.getElementById("pago-estado").value.toUpperCase();
        const ubicacionValue = document.getElementById("pago-ubicacion").value.trim();

        if (monto > 0 && estado === "DEBE") {
            return showToast("Si hay un monto abonado, el estado no puede ser 'DEBE'.", "error");
        }
        
        if (monto === 0 && estado !== "DEBE") {
            return showToast("Para estados 'SEÑADO' o 'PAGADO', el monto debe ser mayor a 0.", "error");
        }

        if (monto === 0 && estado === "DEBE" && ubicacionValue === "") {
            return showToast("No se han registrado cambios. Ingrese un monto o asigne una mesa.", "warning");
        }

        // Parseo seguro para el backend (Integer)
        const numeroStandInt = ubicacionValue !== "" ? parseInt(ubicacionValue, 10) : null;

        const payload = {
            estadoPago: estado,
            montoAbonado: monto,
            numeroStand: numeroStandInt
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