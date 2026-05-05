/*
 * ====================================
 * ASIGNAR-STANDS.JS (FLUJO DE 3 PASOS)
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
    const inputMonto = document.getElementById("pago-monto"); // 🟢 Agregamos referencia clara
    const selectEstado = document.getElementById("pago-estado"); // 🟢 Agregamos referencia clara

    // ========================================================
    // INICIALIZACIÓN
    // ========================================================

async function init() {
    await cargarFerias();
    feriaSelect.addEventListener("change", cargarParticipantes);
    configurarAutomatizacionPago(); 
}

// 🤖 AYUDANTE: Cambia el estado automáticamente según el monto
function configurarAutomatizacionPago() {
    // Verificamos que los elementos existan antes de colgarles el listener
    if (inputMonto && selectEstado) {
        inputMonto.addEventListener("input", (e) => {
            const monto = parseFloat(e.target.value) || 0;
            const estadoActual = selectEstado.value;

            // Si pone plata y dice DEBE, lo sugerimos como SEÑADO
            if (monto > 0 && estadoActual === "DEBE") {
                selectEstado.value = "SENADO"; 
            } else if (monto === 0) {
                selectEstado.value = "DEBE";
            }
        });
    }
}
    async function cargarFerias() {
        try {
            const res = await axios.get("/api/ferias");
            feriaSelect.innerHTML = '<option value="">Selecciona una feria...</option>';
            res.data.forEach(feria => {
                const estadoIcon = feria.estado === 'Activa' ? '🟢' : '🔴';
                feriaSelect.innerHTML += `
                    <option value="${feria.id}">${estadoIcon} ${feria.nombre} (${feria.estado})</option>
                `;
            });
        } catch (error) {
            showToast("Error al cargar ferias", "error");
        }
    }

    // ========================================================
    // LÓGICA DE FILTRADO Y RENDERIZADO
    // ========================================================

async function cargarParticipantes() {
        const feriaId = feriaSelect.value;
        if (!feriaId) {
            gestionContainer.style.display = "none";
            return;
        }

        try {
            const res = await axios.get(`/api/participaciones/feria/${feriaId}`);

            // 🟢 Corregido: Una sola declaración que ya trae los datos filtrados
            const participaciones = res.data.filter(p => p.estado !== 'CANCELADO');

            // 1. Solicitudes (Aún no aprobadas)
            const pendientes = participaciones.filter(p => p.estado === 'PENDIENTE');

            // 2. Caja (Aprobados que no han pagado nada)
            const paraCobrar = participaciones.filter(p => p.estado === 'CONFIRMADO' && p.estadoPago === 'DEBE');

            // 3. Distribución (Aprobados que ya señaron o pagaron total)
            const paraDistribuir = participaciones.filter(p => p.estado === 'CONFIRMADO' && p.estadoPago !== 'DEBE');

            renderPendientes(pendientes);
            renderCobros(paraCobrar);
            renderDistribucion(paraDistribuir);

            gestionContainer.style.display = "block";
        } catch (error) {
            showToast("Error al cargar participantes", "error");
        }
    }

    function renderPendientes(lista) {
        tbodyPendientes.innerHTML = "";
        if (lista.length === 0) {
            tbodyPendientes.innerHTML = "<tr><td colspan='3' style='text-align:center;'>No hay solicitudes pendientes.</td></tr>";
            return;
        }

        lista.forEach(p => {
            tbodyPendientes.innerHTML += `
                <tr>
                    <td><strong>${p.stand}</strong></td>
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
            tbodyCobros.innerHTML += `
                <tr>
                    <td><strong>${p.stand}</strong></td>
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

  // 🟢 2. Agregamos el botón "Quitar" en el render de Distribución
  function renderDistribucion(lista) {
      tbodyDistribucion.innerHTML = lista.length === 0 ? "<tr><td colspan='4' style='text-align:center;'>Nadie listo para ubicar.</td></tr>" : "";
      lista.forEach(p => {
          let badgeClass = p.estadoPago === "SENADO" ? "badge-senado" : "badge-pagado";
          let textoPago = p.estadoPago === "SENADO" ? "Señado" : "Pagado";
          const ubicacionTexto = p.numeroStand ? `Mesa ${p.numeroStand}` : `<span style="color:#f59e0b;">Sin asignar</span>`;

          tbodyDistribucion.innerHTML += `
              <tr>
                  <td><strong>${p.stand}</strong></td>
                  <td><span class="${badgeClass}">${textoPago} ($${p.montoAbonado})</span></td>
                  <td>${ubicacionTexto}</td>
                  <td>
                      <!-- Pasamos 'true' al final para que muestre el campo de mesa -->
                      <button class="btn-cobrar" onclick="abrirModalPago(${p.id}, '${p.estadoPago}', ${p.montoAbonado || 0}, '${p.numeroStand || ''}', true)">
                          <i class="fas fa-map-marker-alt"></i> Ubicar
                      </button>
                      <!-- BOTÓN QUITAR: Lo vuelve a poner en PENDIENTE o lo CANCELA -->
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
            showToast("Error al actualizar estado", "error");
        }
    };

    window.cambiarTab = (tabName) => {
        document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
        document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));

        document.getElementById(`btn-tab-${tabName}`).classList.add('active');
        document.getElementById(`tab-${tabName}`).classList.add('active');
    };

   // 🟢 1. Modificamos abrirModalPago para que acepte un nuevo parámetro: "esDistribucion"
   window.abrirModalPago = (id, estadoPago, monto, ubicacion, esDistribucion = false) => {
       document.getElementById("pago-participacion-id").value = id;
       document.getElementById("pago-estado").value = estadoPago || "DEBE";
       document.getElementById("pago-monto").value = monto;
       document.getElementById("pago-ubicacion").value = ubicacion;

       // Si estamos en la Tab 3 (Distribución), mostramos el campo de mesa. Si no, lo ocultamos.
       const grupoUbicacion = document.getElementById("grupo-ubicacion");
       grupoUbicacion.style.display = esDistribucion ? "block" : "none";

       modalPago.style.display = "block";
   };

   // 🟢 3. Función para "bajar" a un feriante de la feria
   window.quitarDeDistribucion = async (id) => {
       if(!confirm("¿Estás seguro de quitar a este feriante de la feria? Volverá a estar disponible para postularse.")) return;

       try {
           // Al pasarlo a CANCELADO, el sistema lo saca de todas las tablas y libera el cupo
           await axios.patch(`/api/participaciones/${id}/estado-asistencia?estado=CANCELADO`);
           showToast("Feriante quitado de la feria", "info");
           cargarParticipantes();
       } catch (error) {
           showToast("Error al quitar feriante", "error");
       }
   }

window.cerrarModalPago = () => { modalPago.style.display = "none"; };

// 🟢 SUBMIT CON DOBLE VALIDACIÓN (Frontend + Backend)
formPago.addEventListener("submit", async (e) => {
    e.preventDefault();

    const id = document.getElementById("pago-participacion-id").value;
    const monto = parseFloat(document.getElementById("pago-monto").value) || 0;
    const estado = document.getElementById("pago-estado").value;
    const ubicacion = document.getElementById("pago-ubicacion").value;

    // 🛡️ 1. Validación de consistencia lógica
    if (monto > 0 && estado === "DEBE") {
        return showToast("Si hay un monto abonado, el estado no puede ser 'DEBE'.", "error");
    }
    
    if (monto === 0 && estado !== "DEBE") {
        return showToast("Para estados 'SEÑADO' o 'PAGADO', el monto debe ser mayor a 0.", "error");
    }

    // 🛡️ 2. NUEVA: Validación de "Operación Nula"
    // Si intenta guardar (DEBE + 0) y no está asignando una mesa, no tiene sentido el guardado
    if (monto === 0 && estado === "DEBE" && (!ubicacion || ubicacion.trim() === "")) {
        return showToast("No se han registrado cambios. Ingrese un monto o asigne una mesa.", "warning");
    }

    const payload = {
        estadoPago: estado,
        montoAbonado: monto,
        numeroStand: ubicacion
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