const API_URL = "/api/productos";
const FERIAS_URL = "/api/ferias/lista-select";
const CAT_URL = "/api/categorias";

let productosActuales = [];

document.addEventListener("DOMContentLoaded", () => {
    cargarSelectores();
    ejecutarBusqueda();

    // Eventos de entrada para actualización instantánea
    ["inputNombre", "minPrecio", "maxPrecio"].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.addEventListener("input", ejecutarBusqueda);
    });

    ["selectFeria", "selectCategoria", "checkFeriasActivas"].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.addEventListener("change", ejecutarBusqueda);
    });
});

async function cargarSelectores() {
    try {
        const [resFerias, resCats] = await Promise.all([
            axios.get(FERIAS_URL),
            axios.get(CAT_URL)
        ]);

        const selFeria = document.getElementById("selectFeria");
        if (selFeria) {
            selFeria.innerHTML = '<option value="">Todas las ferias</option>';
            resFerias.data.forEach(f => {
                selFeria.innerHTML += `<option value="${f.id}">${escapeHtml(f.nombre)}</option>`;
            });
        }

        const selCat = document.getElementById("selectCategoria");
        if (selCat) {
            selCat.innerHTML = '<option value="">Todas las categorías</option>';
            resCats.data.forEach(c => {
                selCat.innerHTML += `<option value="${c.id}">${escapeHtml(c.nombre)}</option>`;
            });
        }
    } catch (err) {
        console.error("Error cargando filtros", err);
    }
}

async function ejecutarBusqueda() {
    const inputNombre = document.getElementById("inputNombre");
    const selectCategoria = document.getElementById("selectCategoria");
    const selectFeria = document.getElementById("selectFeria");
    const minPrecio = document.getElementById("minPrecio");
    const maxPrecio = document.getElementById("maxPrecio");
    const checkFeriasActivas = document.getElementById("checkFeriasActivas");

    const params = {
        nombre: inputNombre ? inputNombre.value.trim() : "",
        categoriaId: selectCategoria && selectCategoria.value ? selectCategoria.value : null,
        feriaId: selectFeria && selectFeria.value ? selectFeria.value : null,
        minPrecio: minPrecio && minPrecio.value ? minPrecio.value : null,
        maxPrecio: maxPrecio && maxPrecio.value ? maxPrecio.value : null,
        soloFeriasActivas: checkFeriasActivas ? checkFeriasActivas.checked : true
    };

    try {
        const res = await axios.get(`${API_URL}/buscar`, { params });
        productosActuales = res.data || [];
        
        const selectOrden = document.getElementById("selectOrden");
        if (selectOrden && selectOrden.value) {
            ordenarProductos(selectOrden.value, false);
        } else {
            dibujarProductos(productosActuales);
        }
    } catch (err) {
        console.error("Error en búsqueda", err);
        const contenedor = document.getElementById("contenedor-productos");
        if (contenedor) {
            contenedor.innerHTML = `
                <div class="empty-results">
                    <i class="bi bi-exclamation-triangle-fill text-warning"></i>
                    <h4>Error al conectar con el servidor</h4>
                    <p>Verifica tu conexión e intenta nuevamente.</p>
                </div>
            `;
        }
    }
}

function dibujarProductos(productos) {
    const contenedor = document.getElementById("contenedor-productos");
    if (!contenedor) return;

    contenedor.innerHTML = "";
    
    const contadorEl = document.getElementById("contador-resultados");
    if (contadorEl) contadorEl.textContent = productos.length;

    if (productos.length === 0) {
        contenedor.innerHTML = `
            <div class="empty-results">
                <i class="bi bi-search"></i>
                <h4>No se encontraron productos</h4>
                <p>Intenta ajustar los criterios de búsqueda o limpiar los filtros.</p>
            </div>
        `;
        return;
    }

    productos.forEach(p => {
        const img = p.imagenUrl || "https://res.cloudinary.com/dklkf0fmq/image/upload/v1769030533/NOT_IMAGE_aypskv.png";
        const precioFormatted = Number(p.precio || 0).toLocaleString("es-AR", {
            minimumFractionDigits: 0,
            maximumFractionDigits: 2
        });

        const card = document.createElement("div");
        card.className = "producto-card";
        card.onclick = () => {
            window.location.href = `producto-detalle.html?id=${p.id}`;
        };

        card.innerHTML = `
            <div class="producto-card-img-wrapper">
                <img src="${escapeHtml(img)}" class="producto-card-img" alt="${escapeHtml(p.nombre)}" loading="lazy">
                ${p.categoriaNombre ? `<span class="badge-categoria-tag">${escapeHtml(p.categoriaNombre)}</span>` : ""}
            </div>
            
            <div class="producto-card-body">
                <h3 class="producto-title">${escapeHtml(p.nombre)}</h3>
                <p class="producto-desc">${escapeHtml(p.descripcion || 'Sin descripción disponible.')}</p>
                
                <div class="producto-card-footer">
                    <span class="producto-precio">$${precioFormatted}</span>
                    <span class="feria-badge-link">
                        <i class="bi bi-shop"></i> ${escapeHtml(p.feriaNombre || 'Feria')}
                    </span>
                </div>
            </div>
        `;
        contenedor.appendChild(card);
    });
}

function ordenarProductos(criterio, render = true) {
    if (criterio === 'menor') {
        productosActuales.sort((a, b) => a.precio - b.precio);
    } else if (criterio === 'mayor') {
        productosActuales.sort((a, b) => b.precio - a.precio);
    }
    if (render) {
        dibujarProductos(productosActuales);
    }
}

function limpiarFiltros() {
    ["inputNombre", "selectFeria", "selectCategoria", "minPrecio", "maxPrecio"].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.value = "";
    });

    const checkActivas = document.getElementById("checkFeriasActivas");
    if (checkActivas) checkActivas.checked = true;

    const selectOrden = document.getElementById("selectOrden");
    if (selectOrden) selectOrden.value = "";

    ejecutarBusqueda();
    
    if (typeof mostrarNotificacion === "function") {
        mostrarNotificacion("Filtros limpiados.", "info");
    }
}

function escapeHtml(text) {
    if (!text) return "";
    return String(text)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}