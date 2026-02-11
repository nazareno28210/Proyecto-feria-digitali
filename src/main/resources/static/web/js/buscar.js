const API_URL = "/api/productos";
const FERIAS_URL = "/api/ferias/lista-select";
const CAT_URL = "/api/categorias";

let productosActuales = [];

document.addEventListener("DOMContentLoaded", () => {
    cargarSelectores();
    ejecutarBusqueda();

    // Eventos de entrada para actualización instantánea
    ["inputNombre", "minPrecio", "maxPrecio"].forEach(id => {
        document.getElementById(id).addEventListener("input", ejecutarBusqueda);
    });

    ["selectFeria", "selectCategoria", "checkFeriasActivas"].forEach(id => {
        document.getElementById(id).addEventListener("change", ejecutarBusqueda);
    });
});

async function cargarSelectores() {
    try {
        const [resFerias, resCats] = await Promise.all([
            axios.get(FERIAS_URL),
            axios.get(CAT_URL)
        ]);

        const selFeria = document.getElementById("selectFeria");
        selFeria.innerHTML = '<option value="">Todas las ferias</option>';
        resFerias.data.forEach(f => selFeria.innerHTML += `<option value="${f.id}">${f.nombre}</option>`);

        const selCat = document.getElementById("selectCategoria");
        selCat.innerHTML = '<option value="">Todas las categorías</option>';
        resCats.data.forEach(c => selCat.innerHTML += `<option value="${c.id}">${c.nombre}</option>`);
    } catch (err) {
        console.error("Error cargando filtros", err);
    }
}

async function ejecutarBusqueda() {
    const params = {
        nombre: document.getElementById("inputNombre").value.trim(),
        categoriaId: document.getElementById("selectCategoria").value || null,
        feriaId: document.getElementById("selectFeria").value || null,
        minPrecio: document.getElementById("minPrecio").value || null,
        maxPrecio: document.getElementById("maxPrecio").value || null,
        soloFeriasActivas: document.getElementById("checkFeriasActivas").checked
    };

    try {
        const res = await axios.get(`${API_URL}/buscar`, { params });
        productosActuales = res.data;
        dibujarProductos(productosActuales);
    } catch (err) {
        console.error("Error en búsqueda", err);
    }
}

function dibujarProductos(productos) {
    const contenedor = document.getElementById("contenedor-productos");
    contenedor.innerHTML = "";
    document.getElementById("contador-resultados").textContent = productos.length;

    if (productos.length === 0) {
        contenedor.innerHTML = '<div class="col-12 text-center py-5 text-muted">No se encontraron productos.</div>';
        return;
    }

    productos.forEach(p => {
        const img = p.imagenUrl || "https://res.cloudinary.com/dklkf0fmq/image/upload/v1769030533/NOT_IMAGE_aypskv.png";
        contenedor.innerHTML += `
            <div class="col-md-3 mb-4">
                <div class="card h-100 producto-card shadow-sm">
                    <img src="${img}" class="card-img-top">
                    <div class="card-body d-flex flex-column">
                        <div class="mb-2"><span class="badge badge-categoria">${p.categoriaNombre}</span></div>
                        <h5 class="card-title h6 fw-bold mb-1">${p.nombre}</h5>
                        <p class="card-text text-muted small flex-grow-1">${p.descripcion || ''}</p>
                        <div class="mt-auto pt-2 border-top d-flex justify-content-between align-items-center">
                            <span class="fw-bold text-primary fs-5">$${p.precio.toFixed(0)}</span>
                            <span class="text-muted small"><i class="bi bi-shop"></i> ${p.feriaNombre || 'Feria'}</span>
                        </div>
                    </div>
                </div>
            </div>`;
    });
}

function ordenarProductos(criterio) {
    if (criterio === 'menor') productosActuales.sort((a, b) => a.precio - b.precio);
    else productosActuales.sort((a, b) => b.precio - a.precio);
    dibujarProductos(productosActuales);
}

function limpiarFiltros() {
    ["inputNombre", "selectFeria", "selectCategoria", "minPrecio", "maxPrecio"].forEach(id => document.getElementById(id).value = "");
    document.getElementById("checkFeriasActivas").checked = true;
    ejecutarBusqueda();
    Toastify({ text: "Filtros limpiados", duration: 2000, gravity: "bottom", style: { background: "#0d2c44" } }).showToast();
}