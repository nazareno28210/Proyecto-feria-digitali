document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('forgotPasswordForm');
    const emailInput = document.getElementById('email');
    const submitBtn = document.getElementById('submitBtn');
    const btnText = document.getElementById('btnText');
    const btnLoading = document.getElementById('btnLoading');

    form.addEventListener('submit', async function(event) {
        event.preventDefault();

        const email = emailInput.value.trim();

        if (!email) {
            showToast('Por favor ingresa tu correo electronico', 'error');
            return;
        }

        // Mostrar estado de carga
        setLoading(true);

        try {
            const response = await fetch(`/auth/forgot-password?email=${encodeURIComponent(email)}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                showToast('Si el correo existe, recibiras un enlace de recuperacion', 'success');
                // Limpiar el campo
                emailInput.value = '';
            } else {
                const errorText = await response.text();
                showToast(errorText || 'Error al procesar la solicitud', 'error');
            }
        } catch (error) {
            console.error('Error de conexion:', error);
            showToast('Error de conexion. Verifica tu internet.', 'error');
        } finally {
            setLoading(false);
        }
    });

    function setLoading(isLoading) {
        submitBtn.disabled = isLoading;
        btnText.style.display = isLoading ? 'none' : 'inline';
        btnLoading.style.display = isLoading ? 'inline' : 'none';
    }

    function showToast(message, type) {
        const backgroundColor = type === 'success' 
            ? 'linear-gradient(to right, #16a34a, #22c55e)' 
            : 'linear-gradient(to right, #dc2626, #ef4444)';

        Toastify({
            text: message,
            duration: 4000,
            gravity: 'top',
            position: 'right',
            style: {
                background: backgroundColor,
                borderRadius: '8px',
                fontFamily: 'Poppins, sans-serif',
                fontSize: '0.95rem',
                padding: '12px 20px'
            }
        }).showToast();
    }
});
