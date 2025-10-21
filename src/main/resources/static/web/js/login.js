document.getElementById("loginForm").addEventListener("submit", async (e) => {
  e.preventDefault();

  const email = document.getElementById("email").value;
  const password = document.getElementById("password").value;

  const formData = new URLSearchParams();
  formData.append("email", email);
  formData.append("password", password);

  try {
    const response = await fetch("/api/login", {
      method: "POST",
      body: formData,
      credentials: "include"
    });

    if (response.redirected) {
      // Si el backend redirige, seguimos la redirección
      window.location.href = response.url;
    } else if (response.ok) {
      window.location.href = "/web/index.html";
    } else {
      alert("Email o contraseña incorrectos");
    }
  } catch (err) {
    alert("Error en la conexión con el servidor");
  }
});
