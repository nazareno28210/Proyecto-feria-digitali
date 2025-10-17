// Usuarios de ejemplo (FRONTEND DEMO ONLY).
// En producción validar contra un backend y no guardar contraseñas en claro.
const USERS = [
  { username: "feriante", password: "123", role: "feriante" },
  { username: "usuario",  password: "123", role: "usuario" },
  { username: "admin",    password: "123", role: "administrador" }
];

const form = document.getElementById("login-form");
const usernameInput = document.getElementById("username");
const passwordInput = document.getElementById("password");
const errorEl = document.getElementById("error");
const loginSection = document.getElementById("login-section");
const dashboardSection = document.getElementById("dashboard-section");
const welcomeEl = document.getElementById("welcome");
const roleInfoEl = document.getElementById("role-info");
const logoutBtn = document.getElementById("logout-btn");

function findUser(username, password) {
  return USERS.find(u => u.username === username && u.password === password) || null;
}

function showError(msg){
  errorEl.textContent = msg;
}

function clearError(){
  errorEl.textContent = "";
}

function showDashboard(user){
  // Ocultar login
  loginSection.classList.add("hidden");
  // Preparar dashboard
  welcomeEl.textContent = `¡Hola, ${user.username}!`;
  const badgeClass = user.role === "feriante" ? "feriante" :
                     user.role === "administrador" ? "administrador" : "usuario";

  roleInfoEl.innerHTML = `
    <div>
      Rol actual: <span class="badge ${badgeClass}">${user.role}</span>
    </div>
  `;

  // Contenido específico por rol
  const extra = document.createElement("div");
  extra.className = "role-card";
  if(user.role === "feriante"){
    extra.innerHTML = "<strong>Panel de Feriante</strong><p>Ver y gestionar tus puestos, productos y horarios.</p>";
  } else if(user.role === "administrador"){
    extra.innerHTML = "<strong>Panel de Administrador</strong><p>Acceso a estadísticas, gestión de usuarios y moderación.</p>";
  } else {
    extra.innerHTML = "<strong>Panel de Usuario</strong><p>Explora ferias, compra y deja reseñas.</p>";
  }
  roleInfoEl.appendChild(extra);

  dashboardSection.classList.remove("hidden");

  // Guardar sesión de demo en localStorage
  try {
    localStorage.setItem("demo_logged_user", JSON.stringify(user));
  } catch(e){}
}

function hideDashboard(){
  dashboardSection.classList.add("hidden");
  loginSection.classList.remove("hidden");
  roleInfoEl.innerHTML = "";
  welcomeEl.textContent = "";
  usernameInput.value = "";
  passwordInput.value = "";
  clearError();
  usernameInput.focus();
  try {
    localStorage.removeItem("demo_logged_user");
  } catch(e){}
}

form.addEventListener("submit", e => {
  e.preventDefault();
  clearError();
  const username = usernameInput.value.trim();
  const password = passwordInput.value;

  if(!username || !password){
    showError("Completa usuario y contraseña.");
    return;
  }

  const user = findUser(username, password);
  if(!user){
    showError("Usuario o contraseña incorrectos.");
    return;
  }

  // Mostrar dashboard con el rol detectado
  showDashboard(user);
});

// Soporte para mantener sesión en refrescos (demo)
document.addEventListener("DOMContentLoaded", () => {
  try {
    const saved = localStorage.getItem("demo_logged_user");
    if(saved){
      const user = JSON.parse(saved);
      // Verificamos contra la lista para evitar datos manip.
      const valid = USERS.find(u => u.username === user.username && u.role === user.role);
      if(valid) showDashboard(valid);
    }
  } catch(e){}
});

logoutBtn.addEventListener("click", () => {
  hideDashboard();
});